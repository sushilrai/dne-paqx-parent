/**
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries.  All Rights Reserved.
 * Dell EMC Confidential/Proprietary Information
 */
package com.dell.cpsd.paqx.dne.amqp.config;

import com.dell.converged.capabilities.compute.discovered.nodes.api.DiscoveredNodeError;
import com.dell.converged.capabilities.compute.discovered.nodes.api.ListNodes;
import com.dell.converged.capabilities.compute.discovered.nodes.api.NodesListed;
import com.dell.cpsd.common.rabbitmq.MessageAnnotationProcessor;
import com.dell.cpsd.common.rabbitmq.message.DefaultMessageConverterFactory;
import com.dell.cpsd.common.rabbitmq.retrypolicy.DefaultRetryPolicyFactory;
import com.dell.cpsd.virtualization.capabilities.api.DiscoverClusterRequestInfoMessage;
import com.dell.cpsd.virtualization.capabilities.api.DiscoverClusterResponseInfoMessage;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.ClassMapper;
import org.springframework.amqp.support.converter.DefaultClassMapper;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.retry.support.RetryTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
@Import({ProductionConfig.class, PropertiesConfig.class})
public class RabbitConfig
{
    public static final String QUEUE_GENERAL_RESPONSE = "queue.dell.cpsd.dne-paqx.response";

    @Autowired
    @Qualifier("rabbitConnectionFactory")
    private ConnectionFactory rabbitConnectionFactory;

    /**
     * The configuration properties for the service.
     */
    @Autowired
    private PropertiesConfig propertiesConfig;

    @Bean
    RabbitTemplate rabbitTemplate() {
        RabbitTemplate template = new RabbitTemplate(rabbitConnectionFactory);
        template.setMessageConverter(messageConverter());
        template.setRetryTemplate(retryTemplate());
        return template;
    }

    @Bean
    RetryTemplate retryTemplate()
    {
        return DefaultRetryPolicyFactory.makeRabbitTemplateRetry();
    }

    @Bean
    public MessageConverter messageConverter()
    {
        return DefaultMessageConverterFactory.makeMessageConverter(classMapper());
    }

    @Bean
    String hostName() {
        try {
            return System.getProperty("container.id");
        } catch (Exception e) {
            throw new RuntimeException("Unable to identify containerId", e);
        }
    }

    @Bean
    String replyTo() {
        return propertiesConfig.applicationName() + "." + hostName();
    }

    @Bean (name="nodeExpansionAmqpAdmin")
    AmqpAdmin nodeExpansionAmqpAdmin() {
        return new RabbitAdmin(rabbitConnectionFactory);
    }

    @Bean
    ClassMapper classMapper() {
        DefaultClassMapper classMapper = new DefaultClassMapper();
        Map<String, Class<?>> classMappings = new HashMap<>();
        List<Class<?>> messageClasses = new ArrayList<>();

        messageClasses.add(ListNodes.class);
        messageClasses.add(NodesListed.class);
        messageClasses.add(DiscoveredNodeError.class);

        messageClasses.add(DiscoverClusterRequestInfoMessage.class);
        messageClasses.add(DiscoverClusterResponseInfoMessage.class);

        MessageAnnotationProcessor messageAnnotationProcessor = new MessageAnnotationProcessor();
        messageAnnotationProcessor.process(classMappings::put, messageClasses);
        classMapper.setIdClassMapping(classMappings);

        return classMapper;
    }

    @Bean (name="nodeExpansionResponseQueue")
    Queue nodeExpansionResponseQueue() {
        return new Queue(QUEUE_GENERAL_RESPONSE + "." + replyTo());
    }
}
