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

public class InventoryVCenterTest
{
    private InventoryVCenter      inventoryVCenter;
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
        inventoryVCenter = new InventoryVCenter(nodeService, repository);
        componentEndpointIds = new ComponentEndpointIds("abc", "abc", "abc", "abc");
    }

    @Test
    public void vcInventoryFailed() throws Exception
    {
        try
        {
            inventoryVCenter = new InventoryVCenter(nodeService, null);
            inventoryVCenter.delegateExecute(delegateExecution);
        }
        catch (BpmnError error)
        {
            assertTrue(error.getErrorCode().equals(DelegateConstants.INVENTORY_VCENTER_FAILED));
            assertTrue(error.getMessage().contains("An Unexpected Exception occurred attempting to retrieve vCenter Component Endpoints. Reason: null"));
        }
    }

    @Test
    public void scaleIoInventoryNotSaved() throws Exception
    {
        try
        {
            when(repository.getVCenterComponentEndpointIdsByEndpointType("VCENTER-CUSTOMER")).thenReturn(null);
            inventoryVCenter.delegateExecute(delegateExecution);
        }
        catch (BpmnError error)
        {
            assertTrue(error.getErrorCode().equals(DelegateConstants.VCENTER_INFORMATION_NOT_FOUND));
            assertTrue(error.getMessage().contains("VCenter Endpoints not found."));
        }
    }

    @Test
    public void scaleIoDiscoveryException() throws Exception
    {
        try
        {
            when(repository.getVCenterComponentEndpointIdsByEndpointType("VCENTER-CUSTOMER")).thenReturn(componentEndpointIds);
            given(delegateExecution.getProcessInstanceId()).willThrow(new NullPointerException());
            inventoryVCenter.delegateExecute(delegateExecution);
        }
        catch (BpmnError error)
        {
            assertTrue(error.getErrorCode().equals(DelegateConstants.INVENTORY_VCENTER_FAILED));
            assertTrue(error.getMessage().contains("An Unexpected Exception occurred attempting to Inventory VCenter. Reason: null"));
        }
    }

    @Test
    public void scaleIoDiscoveryFailed() throws Exception
    {
        try
        {
            when(repository.getVCenterComponentEndpointIdsByEndpointType("VCENTER-CUSTOMER")).thenReturn(componentEndpointIds);
            when(delegateExecution.getProcessInstanceId()).thenReturn("1");
            doNothing().when(nodeService).requestDiscoverVCenter(any(), any());
            inventoryVCenter.delegateExecute(delegateExecution);
        }
        catch (BpmnError error)
        {
            assertTrue(error.getErrorCode().equals(DelegateConstants.INVENTORY_VCENTER_FAILED));
            assertTrue(error.getMessage().contains("Inventory request for vCenter Failed."));
        }
    }

    @Test
    public void scaleIoDiscoverySuccess() throws Exception
    {
        when(repository.getVCenterComponentEndpointIdsByEndpointType("VCENTER-CUSTOMER")).thenReturn(componentEndpointIds);
        when(delegateExecution.getProcessInstanceId()).thenReturn("1");
        doNothing().when(nodeService).requestDiscoverVCenter(any(), any());
        final InventoryVCenter c = spy(new InventoryVCenter(nodeService, repository));
        c.delegateExecute(delegateExecution);
        verify(c).updateDelegateStatus("Inventory request for vCenter completed successfully.");
    }
}
