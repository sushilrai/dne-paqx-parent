/**
 * <p>
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries. All Rights Reserved. Dell EMC Confidential/Proprietary Information
 * </p>
 */

package com.dell.cpsd.paqx.dne.service.delegates;

import com.dell.cpsd.paqx.dne.service.NodeService;
import com.dell.cpsd.paqx.dne.service.delegates.model.NodeDetail;
import com.dell.cpsd.paqx.dne.service.delegates.utils.DelegateConstants;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class NotifyNodeDiscoveryToUpdateStatusCompletedTest
{

    private NotifyNodeDiscoveryToUpdateStatusCompleted notifyNodeDiscoveryToUpdateStatusCompleted;
    private NodeService                                nodeService;
    private DelegateExecution                          delegateExecution;
    private NodeDetail                                 nodeDetail;

    @Before
    public void setUp() throws Exception
    {
        nodeService = mock(NodeService.class);
        delegateExecution = mock(DelegateExecution.class);
        notifyNodeDiscoveryToUpdateStatusCompleted = new NotifyNodeDiscoveryToUpdateStatusCompleted(nodeService);
        nodeDetail = new NodeDetail();
        nodeDetail.setId("abc");
        nodeDetail.setServiceTag("test.ServiceTag");
    }

    @Test
    public void testNodeStatusChangeFailed() throws Exception
    {
        try
        {
            when(delegateExecution.getVariable(DelegateConstants.NODE_DETAIL)).thenReturn(nodeDetail);
            when(nodeService.notifyNodeAllocationStatus(anyString(), anyString())).thenReturn(false);

            notifyNodeDiscoveryToUpdateStatusCompleted.delegateExecute(delegateExecution);

            fail("Should be an exception.");
        }
        catch (BpmnError error)
        {
            assertTrue(error.getErrorCode().equals(DelegateConstants.NOTIFY_NODE_STATUS_COMPLETED_FAILED));
            assertTrue(error.getMessage().contains("Node Status was not updated to added."));
        }
    }

    @Test
    public void testGeneralException() throws Exception
    {
        try
        {
            when(delegateExecution.getVariable(DelegateConstants.NODE_DETAIL)).thenReturn(nodeDetail);
            when(nodeService.notifyNodeAllocationStatus(anyString(), anyString())).thenThrow(new NullPointerException());

            notifyNodeDiscoveryToUpdateStatusCompleted.delegateExecute(delegateExecution);

            fail("Should be an exception.");
        }
        catch (BpmnError error)
        {
            assertTrue(error.getErrorCode().equals(DelegateConstants.NOTIFY_NODE_STATUS_COMPLETED_FAILED));
            assertThat(error.getMessage(),
                    containsString("An unexpected exception occurred attempting to update the node status to completed"));
        }

    }

    @Test
    public void testNodeStatusChangeSuccess() throws Exception
    {
        final NotifyNodeDiscoveryToUpdateStatusCompleted nus = spy(new NotifyNodeDiscoveryToUpdateStatusCompleted(nodeService));
        when(delegateExecution.getVariable(DelegateConstants.NODE_DETAIL)).thenReturn(nodeDetail);
        when(nodeService.notifyNodeAllocationStatus(anyString(), anyString())).thenReturn(true);

        nus.delegateExecute(delegateExecution);

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(nus).updateDelegateStatus(captor.capture());
        assertThat(captor.getValue(), CoreMatchers.containsString("was successful"));
    }
}
