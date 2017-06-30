/**
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries.  All Rights Reserved.
 * Dell EMC Confidential/Proprietary Information
 */

package com.dell.cpsd.paqx.dne.service.amqp;

import com.dell.converged.capabilities.compute.discovered.nodes.api.CompleteNodeAllocationRequestMessage;
import com.dell.converged.capabilities.compute.discovered.nodes.api.CompleteNodeAllocationResponseMessage;
import com.dell.converged.capabilities.compute.discovered.nodes.api.ListNodes;
import com.dell.converged.capabilities.compute.discovered.nodes.api.MessageProperties;
import com.dell.converged.capabilities.compute.discovered.nodes.api.NodesListed;
import com.dell.cpsd.common.logging.ILogger;
import com.dell.cpsd.paqx.dne.amqp.producer.DneProducer;
import com.dell.cpsd.paqx.dne.service.NodeService;
import com.dell.cpsd.paqx.dne.service.amqp.adapter.ClustersListedResponseAdapter;
import com.dell.cpsd.paqx.dne.service.amqp.adapter.CompleteNodeAllocationResponseAdapter;
import com.dell.cpsd.paqx.dne.service.amqp.adapter.IdracConfigResponseAdapter;
import com.dell.cpsd.paqx.dne.service.amqp.adapter.NodesListedResponseAdapter;
import com.dell.cpsd.paqx.dne.service.model.DiscoveredNode;
import com.dell.cpsd.paqx.dne.service.model.IdracInfo;
import com.dell.cpsd.paqx.dne.service.model.IdracNetworkSettingsRequest;
import com.dell.cpsd.paqx.dne.service.model.VirtualizationCluster;
import com.dell.cpsd.rackhd.adapter.model.idrac.IdracNetworkSettings;
import com.dell.cpsd.rackhd.adapter.model.idrac.IdracNetworkSettingsRequestMessage;
import com.dell.cpsd.rackhd.adapter.model.idrac.IdracNetworkSettingsResponseMessage;
import com.dell.cpsd.service.common.client.callback.ServiceResponse;
import com.dell.cpsd.service.common.client.exception.ServiceExecutionException;
import com.dell.cpsd.service.common.client.exception.ServiceTimeoutException;
import com.dell.cpsd.service.common.client.rpc.AbstractServiceClient;
import com.dell.cpsd.service.common.client.rpc.DelegatingMessageConsumer;
import com.dell.cpsd.service.common.client.rpc.ServiceRequestCallback;
import com.dell.cpsd.virtualization.capabilities.api.ClusterInfo;
import com.dell.cpsd.virtualization.capabilities.api.DiscoverClusterRequestInfoMessage;
import com.dell.cpsd.virtualization.capabilities.api.DiscoverClusterResponseInfo;
import com.dell.cpsd.virtualization.capabilities.api.DiscoverClusterResponseInfoMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * <p>
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries. All Rights Reserved. Dell EMC Confidential/Proprietary Information
 * </p>
 * 
 * @since 1.0
 */
public class AmqpNodeService extends AbstractServiceClient implements NodeService
{
    /*
     * The logger instance
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(AmqpNodeService.class);
    
    /*
     * The <code>DelegatingMessageConsumer</code>
     */
    private final DelegatingMessageConsumer consumer;
    
    /*
     * The <code>DneProducer</code>
     */
    private final DneProducer producer;
    
    /*
     * The replyTo queue name
     */
    private final String replyTo;

    /**
     * AmqpNodeService constructor.
     * 
     * @param logger - The logger instance.
     * @param consumer - The <code>DelegatingMessageConsumer</code> instance.
     * @param producer - The <code>DneProducer</code> instance.
     * @param replyTo - The replyTo queue name.
     * 
     * @since 1.0
     */
    public AmqpNodeService(ILogger logger, DelegatingMessageConsumer consumer, DneProducer producer, String replyTo)
    {
        super(logger);

        this.consumer = consumer;
        this.producer = producer;
        this.replyTo = replyTo;

        initCallbacks();
    }

    /*
     * Initialize message consumer adapters.
     * 
     * @since 1.0
     */
    private void initCallbacks()
    {
        this.consumer.addAdapter(new NodesListedResponseAdapter(this));
        this.consumer.addAdapter(new ClustersListedResponseAdapter(this));
        this.consumer.addAdapter(new CompleteNodeAllocationResponseAdapter(this));
        this.consumer.addAdapter(new IdracConfigResponseAdapter(this));
    }

    @Override
    public IdracInfo idracNetworkSettings(IdracNetworkSettingsRequest idracNetworkSettingsRequest) throws ServiceTimeoutException, ServiceExecutionException
    {
        IdracInfo idracInfo = new IdracInfo();

        try {
            IdracNetworkSettingsRequestMessage idracNetworkSettingsRequestMessage =new IdracNetworkSettingsRequestMessage();
            com.dell.cpsd.rackhd.adapter.rabbitmq.MessageProperties messageProperties = new com.dell.cpsd.rackhd.adapter.rabbitmq.MessageProperties();
            messageProperties.setCorrelationId(UUID.randomUUID().toString());
            messageProperties.setTimestamp(Calendar.getInstance().getTime());
            messageProperties.setReplyTo(replyTo);

            idracNetworkSettingsRequestMessage.setMessageProperties(messageProperties);

            IdracNetworkSettings idracNetworkSettings = new IdracNetworkSettings();

            idracNetworkSettings.setNodeId(idracNetworkSettingsRequest.getNodeId());
            idracNetworkSettings.setIpAddress(idracNetworkSettingsRequest.getIdracIpAddress());
            idracNetworkSettings.setGateway(idracNetworkSettingsRequest.getIdracGatewayIpAddress());
            idracNetworkSettings.setNetmask(idracNetworkSettingsRequest.getIdracSubnetMask());

            idracNetworkSettingsRequestMessage.setIdracNetworkSettings(idracNetworkSettings);

            ServiceResponse<?> response = processRequest(10000L, new ServiceRequestCallback()
            {
                @Override
                public String getRequestId()
                {
                    return messageProperties.getCorrelationId();
                }

                @Override
                public void executeRequest(String requestId) throws Exception
                {
                    producer.publishIdracNetwokSettings(idracNetworkSettingsRequestMessage);
                }
            });
            IdracNetworkSettingsResponseMessage resp = processResponse(response, IdracNetworkSettingsResponseMessage.class);

            if (resp != null)
            {
                if (resp.getMessageProperties() != null)
                {
                    if (resp.getIdracNetworkSettingsResponse() != null)
                    {
                        LOGGER.info("Response from amqp ipAddress: " + resp.getIdracNetworkSettingsResponse().getIpAddress());
                        LOGGER.info("Response from amqp subnet: " + resp.getIdracNetworkSettingsResponse().getNetmask());
                        LOGGER.info("Response from amqp gateway: " + resp.getIdracNetworkSettingsResponse().getGateway());
                        LOGGER.info("Response from amqp nodeId: " +resp.getIdracNetworkSettingsResponse().getNodeId());

                        if ("SUCCESS".equalsIgnoreCase(resp.getIdracNetworkSettingsResponse().getMessage()))
                        {
                            idracInfo.setIdracIpAddress(resp.getIdracNetworkSettingsResponse().getIpAddress());
                            idracInfo.setIdracSubnetMask(resp.getIdracNetworkSettingsResponse().getNetmask());
                            idracInfo.setIdracGatewayIpAddress(resp.getIdracNetworkSettingsResponse().getGateway());
                            idracInfo.setNodeId(resp.getIdracNetworkSettingsResponse().getNodeId());

                        }
                        else
                        {
                            LOGGER.error("Error response from configure idrac settings: " + resp.getIdracNetworkSettingsResponse().getMessage());
    					}
                        idracInfo.setMessage(resp.getIdracNetworkSettingsResponse().getMessage());
                    }
                }
            }
        }
        catch(Exception e) {
            LOGGER.error("Exception in idracNetworkSettings: ", e);
        }

        return idracInfo;
    }

    /**
     * List the discovered nodes.
     * 
     * @throws ServiceTimeoutException.
     * @throws ServiceExecutionException.
     * 
     * @return <code>List<DiscoveredNode></code>.
     * 
     * @since 1.0
     */
    @Override
    public List<DiscoveredNode> listDiscoveredNodes() throws ServiceTimeoutException, ServiceExecutionException
    {
        MessageProperties messageProperties = new MessageProperties();
        messageProperties.setCorrelationId(UUID.randomUUID().toString());
        messageProperties.setTimestamp(Calendar.getInstance().getTime());
        messageProperties.setReplyTo(replyTo);

        ListNodes request = new ListNodes(messageProperties, Collections.emptyList());
        ServiceResponse<?> response = processRequest(10000L, new ServiceRequestCallback()
        {
            @Override
            public String getRequestId()
            {
                return messageProperties.getCorrelationId();
            }

            @Override
            public void executeRequest(String requestId) throws Exception
            {
                producer.publishListNodes(request);
            }
        });

        NodesListed nodes = processResponse(response, NodesListed.class);
        if (nodes != null)
        {
            if (nodes.getDiscoveredNodes() != null)
            {
                return nodes.getDiscoveredNodes().stream()
                        .map(d -> new DiscoveredNode(d.getConvergedUuid(), d.getNodeId(), d.getAllocationStatus()))
                        .collect(Collectors.toList());
            }
        }

        return Collections.emptyList();
    }

    /**
     * List the virtualization clusters.
     * 
     * @throws ServiceTimeoutException.
     * @throws ServiceExecutionException.
     * 
     * @return <code>List<VirtualizationCluster></code>.
     * 
     * @since 1.0
     */
    @Override
    public List<VirtualizationCluster> listClusters() throws ServiceTimeoutException, ServiceExecutionException
    {
        com.dell.cpsd.virtualization.capabilities.api.MessageProperties messageProperties = new com.dell.cpsd.virtualization.capabilities.api.MessageProperties();
        messageProperties.setCorrelationId(UUID.randomUUID().toString());
        messageProperties.setTimestamp(Calendar.getInstance().getTime());
        messageProperties.setReplyTo(replyTo);

        DiscoverClusterRequestInfoMessage request = new DiscoverClusterRequestInfoMessage();
        ServiceResponse<?> response = processRequest(10000L, new ServiceRequestCallback()
        {
            @Override
            public String getRequestId()
            {
                return messageProperties.getCorrelationId();
            }

            @Override
            public void executeRequest(String requestId) throws Exception
            {
                LOGGER.info("publish list cluster request message");
                producer.publishDiscoverClusters(request);
            }
        });

        DiscoverClusterResponseInfoMessage responseInfo = processResponse(response, DiscoverClusterResponseInfoMessage.class);
        if (responseInfo != null)
        {
            DiscoverClusterResponseInfo clusterResponseInfo = responseInfo.getDiscoverClusterResponseInfo();
            if (clusterResponseInfo != null)
            {
                List<ClusterInfo> clusters = clusterResponseInfo.getClusters();
                if (clusters != null)
                {
                    return clusters.stream()
                            .map(c -> new VirtualizationCluster(c.getName(), c.getNumberOfHosts()))
                            .collect(Collectors.toList());
                }
            }
            return Collections.emptyList();
        }

        return Collections.emptyList();
    }

    /**
     * Send a <code>CompleteNodeAllocationRequestMessage</code> to the node discovery service.
     * 
     * @param elementIdentifier - THe element identifier.
     * 
     * @throws ServiceTimeoutException.
     * @throws ServiceExecutionException.
     * 
     * @since 1.0
     */
    @Override
    public void notifyNodeAllocationComplete(String elementIdentifier) throws ServiceTimeoutException, ServiceExecutionException
    {
        MessageProperties messageProperties = new MessageProperties();
        messageProperties.setCorrelationId(UUID.randomUUID().toString());
        messageProperties.setTimestamp(Calendar.getInstance().getTime());
        messageProperties.setReplyTo(replyTo);

        CompleteNodeAllocationRequestMessage request = new CompleteNodeAllocationRequestMessage(messageProperties, elementIdentifier);

        ServiceResponse<?> response = processRequest(10000L, new ServiceRequestCallback()
        {
            @Override
            public String getRequestId()
            {
                return messageProperties.getCorrelationId();
            }

            @Override
            public void executeRequest(String requestId) throws Exception
            {
                producer.publishCompleteNodeAllocation(request);
            }
        });

        // TODO: Not sure what to do with the response, if anything...
        processResponse(response, CompleteNodeAllocationResponseMessage.class);
    }

    /**
     * Process a RPC response message.
     * 
     * @param response - The <code>ServiceResponse</code> to process.
     * @param expectedResponse - The expected response <code>Class</code>
     * 
     * @throws ServiceExecutionException
     * 
     * @return The response.
     * 
     * @since 1.0
     */
    private <R> R processResponse(ServiceResponse<?> response, Class<R> expectedResponse) throws ServiceExecutionException
    {
        Object responseMessage = response.getResponse();
        if (responseMessage == null)
        {
            return null;
        }

        if (expectedResponse.isAssignableFrom(responseMessage.getClass()))
        {
            return (R)responseMessage;
        }
        else
        {
            throw new UnsupportedOperationException("Unexpected response message: " + responseMessage);
        }
    }
}
