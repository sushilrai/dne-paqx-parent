/**
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries.  All Rights Reserved.
 * Dell EMC Confidential/Proprietary Information
 */
package com.dell.cpsd.paqx.dne.amqp.config;

import com.dell.cpsd.hdp.capability.registry.client.binder.CapabilityBinder;
import com.dell.cpsd.paqx.dne.amqp.producer.AmqpDneProducer;
import com.dell.cpsd.paqx.dne.amqp.producer.DneProducer;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(CapabilityConfig.class)
public class ProducerConfig
{

    @Bean
    public DneProducer dneProducer(@Autowired RabbitTemplate rabbitTemplate, @Autowired CapabilityBinder capabilityBinder)
    {
        return new AmqpDneProducer(rabbitTemplate, capabilityBinder);
    }
}
