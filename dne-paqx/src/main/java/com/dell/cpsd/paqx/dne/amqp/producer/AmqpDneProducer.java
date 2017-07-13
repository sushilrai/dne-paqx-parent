/**
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries.  All Rights Reserved.
 * Dell EMC Confidential/Proprietary Information
 */

package com.dell.cpsd.paqx.dne.amqp.producer;

import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import com.dell.converged.capabilities.compute.discovered.nodes.api.ChangeIdracCredentialsRequestMessage;
import com.dell.converged.capabilities.compute.discovered.nodes.api.CompleteNodeAllocationRequestMessage;
import com.dell.converged.capabilities.compute.discovered.nodes.api.ListNodes;
import com.dell.cpsd.common.rabbitmq.annotation.Message;
import com.dell.cpsd.hdp.capability.registry.api.ProviderEndpoint;
import com.dell.cpsd.hdp.capability.registry.client.binder.CapabilityBinder;
import com.dell.cpsd.hdp.capability.registry.client.binder.CapabilityData;
import com.dell.cpsd.hdp.capability.registry.client.helper.AmqpProviderEndpointHelper;
import com.dell.cpsd.rackhd.adapter.model.idrac.IdracNetworkSettingsRequestMessage;
import com.dell.cpsd.virtualization.capabilities.api.DiscoverClusterRequestInfoMessage;

/**
 * <p>
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries.  All Rights Reserved.
 * Dell EMC Confidential/Proprietary Information
 * </p>
 *
 * @since 1.0
 */

public class AmqpDneProducer implements DneProducer
{
    private static final Logger LOGGER = LoggerFactory.getLogger(AmqpDneProducer.class);
    private final RabbitTemplate   rabbitTemplate;
    private final CapabilityBinder capabilityBinder;

    public AmqpDneProducer(RabbitTemplate rabbitTemplate, CapabilityBinder capabilityBinder)
    {
        this.rabbitTemplate = rabbitTemplate;
        this.capabilityBinder = capabilityBinder;
    }

    @Override
    public void publishIdracNetwokSettings(IdracNetworkSettingsRequestMessage request)
    {
        Collection<CapabilityData> capabilityDatas = capabilityBinder.getCurrentCapabilities();
        LOGGER.info("publishIdracNetwokSettings: found list of capablities with size {}", capabilityDatas.size());
        for (CapabilityData capabilityData : capabilityDatas)
        {
            ProviderEndpoint endpoint = capabilityData.getCapability().getProviderEndpoint();
            AmqpProviderEndpointHelper endpointHelper = new AmqpProviderEndpointHelper(endpoint);
            if (messageType(IdracNetworkSettingsRequestMessage.class).equals(endpointHelper.getRequestMessageType()))
            {
                LOGGER.info("Publish idrac network settings request message from DNE paqx.");
                rabbitTemplate.convertAndSend(endpointHelper.getRequestExchange(), endpointHelper.getRequestRoutingKey(), request);
            }
        }
    }

    @Override
    public void publishListNodes(ListNodes request)
    {
        Collection<CapabilityData> capabilityDatas = capabilityBinder.getCurrentCapabilities();
        LOGGER.info("publishListNodes: found list of capablities with size {}", capabilityDatas.size());
        for (CapabilityData capabilityData : capabilityDatas)
        {
            ProviderEndpoint endpoint = capabilityData.getCapability().getProviderEndpoint();
            AmqpProviderEndpointHelper endpointHelper = new AmqpProviderEndpointHelper(endpoint);
            if (messageType(ListNodes.class).equals(endpointHelper.getRequestMessageType()))
            {
                LOGGER.info("Send node discovery request message from DNE paqx.");
                rabbitTemplate.convertAndSend(endpointHelper.getRequestExchange(), endpointHelper.getRequestRoutingKey(), request);
            }
        }
    }

    @Override
    public void publishDiscoverClusters(DiscoverClusterRequestInfoMessage request)
    {
        Collection<CapabilityData> capabilityDatas = capabilityBinder.getCurrentCapabilities();
        LOGGER.info("publishDiscoverClusters: found list of capablities with size {}", capabilityDatas.size());
        for (CapabilityData capabilityData : capabilityDatas)
        {
            ProviderEndpoint endpoint = capabilityData.getCapability().getProviderEndpoint();
            AmqpProviderEndpointHelper endpointHelper = new AmqpProviderEndpointHelper(endpoint);
            if (messageType(DiscoverClusterRequestInfoMessage.class).equals(endpointHelper.getRequestMessageType()))
            {
                LOGGER.info("Send discover cluster request message from DNE paqx.");
                rabbitTemplate.convertAndSend(endpointHelper.getRequestExchange(), endpointHelper.getRequestRoutingKey(), request);
            }
        }
    }

    /**
     * Send the <code>CompleteNodeAllocationRequestMessage</code> to the node 
     * discovery service.
     * 
     * @param request - The <code>CompleteNodeAllocationRequestMessage</code> instance
     */
    @Override
    public void publishCompleteNodeAllocation(CompleteNodeAllocationRequestMessage request)
    {
        CapabilityData capabilityData = capabilityBinder.getCurrentCapabilities()
                .stream()
                .filter((data) -> "manage-node-allocation".equals(data.getCapability().getProfile()))
                .findFirst()
                .orElse(null);
        
        if (capabilityData != null) 
        {
            ProviderEndpoint endpoint = capabilityData.getCapability().getProviderEndpoint();
            AmqpProviderEndpointHelper endpointHelper = new AmqpProviderEndpointHelper(endpoint);
                    
            LOGGER.info("Send complete node allocation request message from DNE paqx.");
            rabbitTemplate.convertAndSend(endpointHelper.getRequestExchange(), endpointHelper.getRequestRoutingKey(), request);
        }
    }

    private String messageType(Class messageClass)
    {
        Message messageAnnotation = (Message)messageClass.getAnnotation(Message.class);
        return messageAnnotation.value();
    }

    @Override
    /**
     * Send the <code>ChangeIdracCredentialsRequestMessage</code> to the node 
     * discovery service.
     * 
     * @param request - The <code>ChangeIdracCredentialsRequestMessage</code> instance
     */
    public void publishChangeIdracCredentials(ChangeIdracCredentialsRequestMessage request)
    {
        Collection<CapabilityData> capabilityDatas = capabilityBinder.getCurrentCapabilities();
        LOGGER.info("publishChangeIdracCrdentials: found list of capablities with size {}", capabilityDatas.size());
        for (CapabilityData capabilityData : capabilityDatas)
        {
            ProviderEndpoint endpoint = capabilityData.getCapability().getProviderEndpoint();
            AmqpProviderEndpointHelper endpointHelper = new AmqpProviderEndpointHelper(endpoint);
            if (messageType(ChangeIdracCredentialsRequestMessage.class).equals(endpointHelper.getRequestMessageType()))
            {
                LOGGER.info("Publish change idrac credentials request message from DNE paqx.");
                rabbitTemplate.convertAndSend(endpointHelper.getRequestExchange(), endpointHelper.getRequestRoutingKey(), request);
            }
        }
    }
}
