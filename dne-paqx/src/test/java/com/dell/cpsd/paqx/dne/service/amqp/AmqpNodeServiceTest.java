/**
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries.  All Rights Reserved.
 * Dell EMC Confidential/Proprietary Information
 */

package com.dell.cpsd.paqx.dne.service.amqp;

import com.dell.converged.capabilities.compute.discovered.nodes.api.NodesListed;

import com.dell.cpsd.paqx.dne.amqp.producer.DneProducer;
import com.dell.cpsd.paqx.dne.service.model.DiscoveredNode;
import com.dell.cpsd.paqx.dne.service.model.VirtualizationCluster;
import com.dell.cpsd.service.common.client.callback.ServiceCallback;
import com.dell.cpsd.service.common.client.callback.ServiceError;
import com.dell.cpsd.service.common.client.callback.ServiceResponse;
import com.dell.cpsd.service.common.client.exception.ServiceExecutionException;
import com.dell.cpsd.service.common.client.exception.ServiceTimeoutException;
import com.dell.cpsd.service.common.client.rpc.DefaultMessageConsumer;
import com.dell.cpsd.service.common.client.rpc.DelegatingMessageConsumer;
import com.dell.cpsd.virtualization.capabilities.api.ClusterInfo;
import com.dell.cpsd.virtualization.capabilities.api.DiscoverClusterResponseInfo;
import com.dell.cpsd.virtualization.capabilities.api.DiscoverClusterResponseInfoMessage;
import com.dell.cpsd.virtualization.capabilities.api.MessageProperties;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;

public class AmqpNodeServiceTest
{
    @Test(expected = ServiceTimeoutException.class)
    public void testListDiscoveredNodesTimeout() throws Exception
    {
        DelegatingMessageConsumer consumer = new DefaultMessageConsumer();
        DneProducer dneProducer = Mockito.mock(DneProducer.class);

        AmqpNodeService nodeService = new AmqpNodeService(null, consumer, dneProducer, "replyToMe")
        {
            @Override
            protected void waitForServiceCallback(ServiceCallback serviceCallback, String requestId,
                    long timeout) throws ServiceTimeoutException
            {
                throw new ServiceTimeoutException("blah");
            }
        };

        nodeService.listDiscoveredNodes();
    }

    @Test
    public void testListDiscoveredNodesSuccess() throws Exception
    {
        String convergedUuid = UUID.randomUUID().toString();
        String nodeId = UUID.randomUUID().toString();

        DelegatingMessageConsumer consumer = new DefaultMessageConsumer();
        DneProducer dneProducer = Mockito.mock(DneProducer.class);

        AmqpNodeService nodeService = new AmqpNodeService(null, consumer, dneProducer, "replyToMe")
        {
            @Override
            protected void waitForServiceCallback(ServiceCallback serviceCallback, String requestId,
                    long timeout) throws ServiceTimeoutException
            {
                // default status of discovered
                com.dell.converged.capabilities.compute.discovered.nodes.api.DiscoveredNode node =
                        new com.dell.converged.capabilities.compute.discovered.nodes.api.DiscoveredNode(nodeId, convergedUuid, 
                                com.dell.converged.capabilities.compute.discovered.nodes.api.DiscoveredNode.AllocationStatus.DISCOVERED);

                com.dell.converged.capabilities.compute.discovered.nodes.api.MessageProperties messageProperties =
                        new com.dell.converged.capabilities.compute.discovered.nodes.api.MessageProperties(Calendar.getInstance().getTime(),
                                UUID.randomUUID().toString(), "replyToMe");

                NodesListed listed = new NodesListed(messageProperties, Arrays.asList(node));
                serviceCallback.handleServiceResponse(new ServiceResponse<>(requestId, listed, null));
            }
        };

        List<DiscoveredNode> discovereds = nodeService.listDiscoveredNodes();
        Assert.assertEquals(1, discovereds.size());

        DiscoveredNode discovered = discovereds.get(0);
        Assert.assertEquals(convergedUuid, discovered.getConvergedUuid());
        Assert.assertEquals(nodeId, discovered.getNodeId());

        Mockito.verify(dneProducer, Mockito.times(1)).publishListNodes(Mockito.any());
    }

    @Test(expected = ServiceExecutionException.class)
    public void testListDiscoveredNodesError() throws Exception
    {
        DelegatingMessageConsumer consumer = new DefaultMessageConsumer();
        DneProducer dneProducer = Mockito.mock(DneProducer.class);
        AmqpNodeService nodeService = new AmqpNodeService(null, consumer, dneProducer, "replyToMe")
        {
            @Override
            protected void waitForServiceCallback(ServiceCallback serviceCallback, String requestId,
                    long timeout) throws ServiceTimeoutException
            {
                serviceCallback.handleServiceError(new ServiceError(requestId, "blah", "blah"));
            }
        };

        nodeService.listDiscoveredNodes();
    }

    @Test(expected = ServiceTimeoutException.class)
    public void testListClustersTimeout() throws Exception
    {
        DelegatingMessageConsumer consumer = new DefaultMessageConsumer();
        DneProducer dneProducer = Mockito.mock(DneProducer.class);

        AmqpNodeService nodeService = new AmqpNodeService(null, consumer, dneProducer, "replyToMe")
        {
            @Override
            protected void waitForServiceCallback(ServiceCallback serviceCallback, String requestId,
                    long timeout) throws ServiceTimeoutException
            {
                throw new ServiceTimeoutException("blah");
            }
        };

        nodeService.listClusters();
    }

    @Test
    public void testListClustersSuccess() throws Exception
    {
        String clusterName = UUID.randomUUID().toString();
        Integer numberOfHosts = 999;

        DelegatingMessageConsumer consumer = new DefaultMessageConsumer();
        DneProducer dneProducer = Mockito.mock(DneProducer.class);

        AmqpNodeService nodeService = new AmqpNodeService(null, consumer, dneProducer, "replyToMe")
        {
            @Override
            protected void waitForServiceCallback(ServiceCallback serviceCallback, String requestId,
                    long timeout) throws ServiceTimeoutException
            {
                DiscoverClusterResponseInfo responseInfo = new DiscoverClusterResponseInfo(Arrays.asList(
                        new ClusterInfo(clusterName, numberOfHosts)));

                MessageProperties messageProperties =
                        new MessageProperties(Calendar.getInstance().getTime(),
                                UUID.randomUUID().toString(), "replyToMe");

                DiscoverClusterResponseInfoMessage responseInfoMessage =
                        new DiscoverClusterResponseInfoMessage(messageProperties, responseInfo);
                serviceCallback.handleServiceResponse(new ServiceResponse<>(requestId, responseInfoMessage, null));
            }
        };

        List<VirtualizationCluster> discovereds = nodeService.listClusters();
        Assert.assertEquals(1, discovereds.size());

        VirtualizationCluster discovered = discovereds.get(0);
        Assert.assertEquals(clusterName, discovered.getName());
        Assert.assertEquals(numberOfHosts, discovered.getNumberOfHosts());

        Mockito.verify(dneProducer, Mockito.times(1)).publishDiscoverClusters(Mockito.any());
    }

    @Test(expected = ServiceExecutionException.class)
    public void testListClustersError() throws Exception
    {
        DelegatingMessageConsumer consumer = new DefaultMessageConsumer();
        DneProducer dneProducer = Mockito.mock(DneProducer.class);
        AmqpNodeService nodeService = new AmqpNodeService(null, consumer, dneProducer, "replyToMe")
        {
            @Override
            protected void waitForServiceCallback(ServiceCallback serviceCallback, String requestId,
                    long timeout) throws ServiceTimeoutException
            {
                serviceCallback.handleServiceError(new ServiceError(requestId, "blah", "blah"));
            }
        };

        nodeService.listDiscoveredNodes();
    }
}
