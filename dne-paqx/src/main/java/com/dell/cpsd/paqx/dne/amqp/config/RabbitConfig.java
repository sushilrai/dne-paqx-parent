/**
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries.  All Rights Reserved.
 * Dell EMC Confidential/Proprietary Information
 */
package com.dell.cpsd.paqx.dne.amqp.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

import com.dell.converged.capabilities.compute.discovered.nodes.api.ChangeIdracCredentialsRequestMessage;
import com.dell.converged.capabilities.compute.discovered.nodes.api.ChangeIdracCredentialsResponseMessage;
import com.dell.converged.capabilities.compute.discovered.nodes.api.CompleteNodeAllocationRequestMessage;
import com.dell.converged.capabilities.compute.discovered.nodes.api.CompleteNodeAllocationResponseMessage;
import com.dell.converged.capabilities.compute.discovered.nodes.api.DiscoveredNodeError;
import com.dell.converged.capabilities.compute.discovered.nodes.api.ListNodes;
import com.dell.converged.capabilities.compute.discovered.nodes.api.NodesListed;
import com.dell.cpsd.common.rabbitmq.MessageAnnotationProcessor;
import com.dell.cpsd.common.rabbitmq.message.DefaultMessageConverterFactory;
import com.dell.cpsd.common.rabbitmq.retrypolicy.DefaultRetryPolicyFactory;
import com.dell.cpsd.virtualization.capabilities.api.DiscoverClusterRequestInfoMessage;
import com.dell.cpsd.virtualization.capabilities.api.DiscoverClusterResponseInfoMessage;

/**
 * This is the configuration for the RabbitMQ artifacts used by the service.
 * 
 * <p/>
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries.  All Rights Reserved.
 * Dell EMC Confidential/Proprietary Information
 * <p/>
 * 
 * @since    1.0
 */
@Configuration
@Import({ProductionConfig.class, PropertiesConfig.class})
public class RabbitConfig
{
    /*
     * The binding key to the dne service response message queue.
     */
    public static final String QUEUE_GENERAL_RESPONSE = "queue.dell.cpsd.dne-paqx.response";

    /*
     * The RabbitMQ connection factory
     */
    @Autowired
    @Qualifier("rabbitConnectionFactory")
    private ConnectionFactory rabbitConnectionFactory;

    /*
     * The configuration properties for the service.
     */
    @Autowired
    private PropertiesConfig propertiesConfig;
    
    /*
     * The support system-integration-sdk class mappings
     */
    @Autowired
    @Qualifier("supportedIdClassMapping")
    private Map<String, Class<?>> sdkSupportedIdClassMapping;

    /**
     * This returns the RabbitMQ template.
     *
     * @return  The <code>RabbitTemplate</code>.
     * 
     * @since   1.0
     */
    @Bean
    RabbitTemplate rabbitTemplate() {
        RabbitTemplate template = new RabbitTemplate(rabbitConnectionFactory);
        template.setMessageConverter(dneMessageConverter());
        template.setRetryTemplate(retryTemplate());
        return template;
    }

    /**
     * This returns the <code>RetryTemplate</code> for the <code>RabbitTemplate
     * </code>.
     *
     * @return  The <code>RetryTemplate</code>.
     * 
     * @since   1.0
     */
    @Bean
    RetryTemplate retryTemplate()
    {
        return DefaultRetryPolicyFactory.makeRabbitTemplateRetry();
    }

    /**
     * This returns the <code>MessageConverter</code> for the 
     * <code>RabbitTemplate</code>.
     * 
     * @return  The <code>MessageConverter</code> for the template.
     * 
     * @since   1.0
     */
    @Bean
    public MessageConverter dneMessageConverter()
    {
        return DefaultMessageConverterFactory.makeMessageConverter(classMapper());
    }

    /**
     * This returns the hostname.
     * 
     * @return The host name.
     */
    @Bean
    String hostName() {
        try {
            return System.getProperty("container.id");
        } catch (Exception e) {
            throw new RuntimeException("Unable to identify containerId", e);
        }
    }

    /**
     * This returns the name of the replyTo queue for RPC responses.
     * 
     * @return The name of the replyTo queue.
     */
    @Bean
    String replyTo() {
        return propertiesConfig.applicationName() + "." + hostName();
    }

    /**
     * This returns the <code>RabbitAdmin</code> instance.
     * 
     * @return  The <code>RabbitAdmin</code>.
     * 
     * @since   1.0
     */
    @Bean (name="nodeExpansionAmqpAdmin")
    AmqpAdmin nodeExpansionAmqpAdmin() {
        return new RabbitAdmin(rabbitConnectionFactory);
    }

    /**
     * This returns the <code>ClassMapper</code> for the message converter.
     * 
     * @return  The <code>ClassMapper</code> for the message converter.
     * 
     * @since   1.0
     */
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

        messageClasses.add(CompleteNodeAllocationRequestMessage.class);
        messageClasses.add(CompleteNodeAllocationResponseMessage.class);
        
        messageClasses.add(ChangeIdracCredentialsRequestMessage.class);
        messageClasses.add(ChangeIdracCredentialsResponseMessage.class);
        
        MessageAnnotationProcessor messageAnnotationProcessor = new MessageAnnotationProcessor();
        messageAnnotationProcessor.process(classMappings::put, messageClasses);
        
        if (this.sdkSupportedIdClassMapping != null)
        {
            classMappings.putAll(this.sdkSupportedIdClassMapping);
        }
        
        classMapper.setIdClassMapping(classMappings);
        
        return classMapper;
    }

    /**
     * This returns the name of the dne service response queue.
     * 
     * @return The name of the dne response queue.
     * 
     * @since   1.0
     */
    @Bean (name="nodeExpansionResponseQueue")
    Queue nodeExpansionResponseQueue() {
        return new Queue(QUEUE_GENERAL_RESPONSE);
    }
}
