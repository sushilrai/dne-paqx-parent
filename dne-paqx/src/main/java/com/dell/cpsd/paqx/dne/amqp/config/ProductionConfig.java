/**
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries.  All Rights Reserved.
 * Dell EMC Confidential/Proprietary Information
 */

package com.dell.cpsd.paqx.dne.amqp.config;

import com.dell.cpsd.common.rabbitmq.connectors.RabbitMQCachingConnectionFactory;
import com.dell.cpsd.common.rabbitmq.connectors.RabbitMQTLSFactoryBean;
import com.rabbitmq.client.DefaultSaslConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * <p>
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries.  All Rights Reserved.
 * Dell EMC Confidential/Proprietary Information
 * </p>
 *
 * @since 1.0
 */

@Configuration
@Import({DneRabbitMQPropertiesConfig.class})
public class ProductionConfig
{
    private static final Logger LOGGER = LoggerFactory.getLogger(ProductionConfig.class);

    @Autowired
    private DneRabbitMQPropertiesConfig dneRabbitMQPropertiesConfig;

    @Bean
    @Qualifier("rabbitConnectionFactory")
    public ConnectionFactory productionCachingConnectionFactory()
    {

        RabbitMQCachingConnectionFactory cachingCF = null;
        com.rabbitmq.client.ConnectionFactory connectionFactory;

        LOGGER.info("Rabbit connection properties : sslEnabled: [{}], host: [{}], port: [{}], tlsVersion:[{}]",
                dneRabbitMQPropertiesConfig.isSslEnabled(), dneRabbitMQPropertiesConfig.rabbitHostname(), dneRabbitMQPropertiesConfig.rabbitPort(),
                dneRabbitMQPropertiesConfig.tlsVersion());
        try
        {
            if (dneRabbitMQPropertiesConfig.isSslEnabled())
            {
                RabbitMQTLSFactoryBean rabbitMQTLSFactoryBean = new RabbitMQTLSFactoryBean(dneRabbitMQPropertiesConfig);

                connectionFactory = rabbitMQTLSFactoryBean.getObject();

                cachingCF = new RabbitMQCachingConnectionFactory(connectionFactory, dneRabbitMQPropertiesConfig);
                cachingCF.getRabbitConnectionFactory().setSaslConfig(DefaultSaslConfig.EXTERNAL);
            }
            else
            {
                connectionFactory = new com.rabbitmq.client.ConnectionFactory();
                cachingCF = new RabbitMQCachingConnectionFactory(connectionFactory, dneRabbitMQPropertiesConfig);
            }
        }
        catch (Exception e)
        {
            LOGGER.error("Exception retrieving connection factory", e);
        }

        return cachingCF;
    }
}
