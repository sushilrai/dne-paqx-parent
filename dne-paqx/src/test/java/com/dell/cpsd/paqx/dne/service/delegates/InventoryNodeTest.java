/**
 * <p>
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries. All Rights Reserved. Dell EMC Confidential/Proprietary Information
 * </p>
 */

package com.dell.cpsd.paqx.dne.service.delegates;

import com.dell.cpsd.paqx.dne.repository.DataServiceRepository;
import com.dell.cpsd.paqx.dne.service.NodeService;
import com.dell.cpsd.paqx.dne.service.delegates.model.NodeDetail;
import com.dell.cpsd.service.common.client.exception.ServiceExecutionException;
import com.dell.cpsd.service.common.client.exception.ServiceTimeoutException;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.dell.cpsd.paqx.dne.service.delegates.utils.DelegateConstants.NODE_DETAILS;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class InventoryNodeTest
{
    @Mock
    private NodeService nodeService;

    @Mock
    private DataServiceRepository dataServiceRepository;

    @Mock
    private DelegateExecution delegateExecution;

    private InventoryNode inventoryNode;

    private List<NodeDetail> nodeDetails = new ArrayList<>();
    private Map<String, Object> nodeInventoryResponse;

    @Before
    public void setUp() throws Exception
    {
        inventoryNode = spy(new InventoryNode(nodeService, dataServiceRepository));

        nodeInventoryResponse = new HashMap<>();
        nodeInventoryResponse.put("1", "{\"name\":\"name-1\"}");

        NodeDetail nodeDetail1 = new NodeDetail("1", "abc");
        nodeDetails.add(nodeDetail1);
        NodeDetail nodeDetail2 = new NodeDetail("2", "pqr");
        nodeDetails.add(nodeDetail2);

        doReturn(nodeInventoryResponse).when(nodeService).listNodeInventory(null);

        doReturn(true).when(dataServiceRepository).saveNodeInventory(any());

    }

    @Test
    public void testSuccessful() throws Exception
    {
        inventoryNode.delegateExecute(delegateExecution);
    }

    @Test
    public void testServiceExecutionException() throws Exception
    {
        Exception e = new ServiceExecutionException("ServiceExecutionException");
        doThrow(e).when(nodeService).listNodeInventory(null);
        try
        {
            inventoryNode.delegateExecute(delegateExecution);
        }
        catch (BpmnError er)
        {
            verify(inventoryNode).updateDelegateStatus("Requesting Update Node Inventory.");
            verify(inventoryNode).updateDelegateStatus("Update Node Inventory request failed.", e);
        }
    }

    @Test
    public void testServiceTimeoutException() throws Exception
    {
        Exception e = new ServiceTimeoutException("ServiceTimeoutException");
        doThrow(e).when(nodeService).listNodeInventory(null);
        try
        {
            inventoryNode.delegateExecute(delegateExecution);
        }
        catch (BpmnError er)
        {
            verify(inventoryNode).updateDelegateStatus("Requesting Update Node Inventory.");
            verify(inventoryNode).updateDelegateStatus("Update Node Inventory request failed.", e);
        }
    }

    @Test
    public void testNodeInventorySaveFailed() throws Exception
    {
        doReturn(false).when(dataServiceRepository).saveNodeInventory(any());
        try
        {
            inventoryNode.delegateExecute(delegateExecution);
        }
        catch (BpmnError e)
        {
            verify(inventoryNode).updateDelegateStatus("Requesting Update Node Inventory.");
            verify(inventoryNode).updateDelegateStatus("Failed to update Node Inventory on Node(s) with uuid 1");
        }
    }

    @Test
    public void testJsonProcessingException() throws Exception
    {
        Map<String, Object> nodeInventoryResponse = new HashMap<>();
        nodeInventoryResponse.put("1", new Object());
        doReturn(nodeInventoryResponse).when(nodeService).listNodeInventory(null);

        try
        {
            inventoryNode.delegateExecute(delegateExecution);
        }
        catch (BpmnError e)
        {
            verify(inventoryNode).updateDelegateStatus("Requesting Update Node Inventory.");
            verify(inventoryNode).updateDelegateStatus("Update Node Inventory failed due to unrecognized response for Node(s) with uuid 1");
        }
    }
}
