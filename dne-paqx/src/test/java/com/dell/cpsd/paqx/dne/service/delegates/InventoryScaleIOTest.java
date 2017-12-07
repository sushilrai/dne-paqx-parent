/**
 * <p>
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries. All Rights Reserved. Dell EMC Confidential/Proprietary Information
 * </p>
 */

package com.dell.cpsd.paqx.dne.service.delegates;

import com.dell.cpsd.paqx.dne.repository.DataServiceRepository;
import com.dell.cpsd.paqx.dne.service.NodeService;
import com.dell.cpsd.paqx.dne.service.delegates.utils.DelegateConstants;
import com.dell.cpsd.paqx.dne.service.model.ComponentEndpointIds;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class InventoryScaleIOTest
{
    private InventoryScaleIO      inventoryScaleIO;
    private NodeService           nodeService;
    private DelegateExecution     delegateExecution;
    private DataServiceRepository repository;
    private ComponentEndpointIds  componentEndpointIds;

    @Before
    public void setUp() throws Exception
    {
        nodeService = mock(NodeService.class);
        delegateExecution = mock(DelegateExecution.class);
        repository = mock(DataServiceRepository.class);
        inventoryScaleIO = new InventoryScaleIO(nodeService, repository);
        componentEndpointIds = new ComponentEndpointIds("abc", "abc", "abc", "abc");
    }

    @Test
    public void scaleIoInventoryFailed() throws Exception
    {
        try
        {
            inventoryScaleIO = new InventoryScaleIO(nodeService, null);
            inventoryScaleIO.delegateExecute(delegateExecution);
        }
        catch (BpmnError error)
        {
            assertTrue(error.getErrorCode().equals(DelegateConstants.INVENTORY_SCALE_IO_FAILED));
            assertTrue(error.getMessage().contains("An Unexpected Exception occurred attempting to retrieve Scale IO Component Endpoints. Reason:null"));
        }
    }

    @Test
    public void scaleIoInventoryNotSaved() throws Exception
    {
        try
        {
            when(repository.getComponentEndpointIds("SCALEIO-CLUSTER")).thenReturn(null);
            inventoryScaleIO.delegateExecute(delegateExecution);
        }
        catch (BpmnError error)
        {
            assertTrue(error.getErrorCode().equals(DelegateConstants.SCALE_IO_INFORMATION_NOT_FOUND));
            assertTrue(error.getMessage().contains("Scale IO Endpoints not found."));
        }
    }

    @Test
    public void scaleIoDiscoveryException() throws Exception
    {
        try
        {
            when(repository.getComponentEndpointIds("SCALEIO-CLUSTER")).thenReturn(componentEndpointIds);
            given(delegateExecution.getProcessInstanceId()).willThrow(new NullPointerException());
            inventoryScaleIO.delegateExecute(delegateExecution);
        }
        catch (BpmnError error)
        {
            assertTrue(error.getErrorCode().equals(DelegateConstants.INVENTORY_SCALE_IO_FAILED));
            assertTrue(error.getMessage().contains("An Unexpected Exception occurred attempting to inventory Scale IO."));
        }
    }

    @Test
    public void scaleIoDiscoveryFailed() throws Exception
    {
        try
        {
            when(repository.getComponentEndpointIds("SCALEIO-CLUSTER")).thenReturn(componentEndpointIds);
            when(delegateExecution.getProcessInstanceId()).thenReturn("1");
            doNothing().when(nodeService).requestDiscoverScaleIo(any(), any());
            inventoryScaleIO.delegateExecute(delegateExecution);
        }
        catch (BpmnError error)
        {
            assertTrue(error.getErrorCode().equals(DelegateConstants.INVENTORY_SCALE_IO_FAILED));
            assertTrue(error.getMessage().contains("Inventory request for Scale IO Failed."));
        }
    }

    @Test
    public void scaleIoDiscoverySuccess() throws Exception
    {
        when(repository.getComponentEndpointIds("SCALEIO-CLUSTER")).thenReturn(componentEndpointIds);
        when(delegateExecution.getProcessInstanceId()).thenReturn("1");
        doNothing().when(nodeService).requestDiscoverScaleIo(any(), any());
        final InventoryScaleIO c = spy(new InventoryScaleIO(nodeService, repository));
        c.delegateExecute(delegateExecution);
        verify(c).updateDelegateStatus("Inventory request for Scale IO completed successfully.");
    }
}
