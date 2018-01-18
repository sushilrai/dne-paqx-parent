/**
 * <p>
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries. All Rights Reserved. Dell EMC Confidential/Proprietary Information
 * </p>
 */

package com.dell.cpsd.paqx.dne.service.delegates;

import com.dell.cpsd.paqx.dne.domain.node.DiscoveredNodeInfo;
import com.dell.cpsd.paqx.dne.service.NodeService;
import com.dell.cpsd.paqx.dne.service.delegates.model.NodeDetail;
import com.dell.cpsd.paqx.dne.service.delegates.utils.DelegateConstants;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import java.util.*;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class NotifyNodeDiscoveryToUpdateStatusStartedTest
{

    private NotifyNodeDiscoveryToUpdateStatusStarted notifyNodeDiscoveryToUpdateStatusStarted;
    private NodeService                              nodeService;
    private DelegateExecution                        delegateExecution;
    private NodeDetail                               nodeDetail;
    private List<DiscoveredNodeInfo>                 discoveredNodeInfoList;

    @Before
    public void setUp() throws Exception
    {
        nodeService = mock(NodeService.class);
        delegateExecution = mock(DelegateExecution.class);
        notifyNodeDiscoveryToUpdateStatusStarted = new NotifyNodeDiscoveryToUpdateStatusStarted(nodeService);
        nodeDetail = new NodeDetail();
        nodeDetail.setId("abc");
        nodeDetail.setServiceTag("test.ServiceTag");

        discoveredNodeInfoList = new ArrayList<>();
        DiscoveredNodeInfo discoveredNodeInfo1 = new DiscoveredNodeInfo("M-1", "MF-1", "P-1", "PF-1", "SN-1", "abc");
        discoveredNodeInfoList.add(discoveredNodeInfo1);
        DiscoveredNodeInfo discoveredNodeInfo2 = new DiscoveredNodeInfo("M-2", "MF-2", "P-2", "PF-2", "SN-2", "UUID-2");
        discoveredNodeInfoList.add(discoveredNodeInfo2);
    }

    @Test
    public void testNodeStatusChangeFailed() throws Exception
    {
        try
        {
            when(delegateExecution.getVariable(DelegateConstants.NODE_DETAIL)).thenReturn(nodeDetail);
            when(nodeService.notifyNodeAllocationStatus(anyString(), anyString())).thenReturn(false);

            notifyNodeDiscoveryToUpdateStatusStarted.delegateExecute(delegateExecution);

            fail("Should be an exception.");
        }
        catch (BpmnError ex)
        {
            assertTrue(ex.getErrorCode().equals(DelegateConstants.NOTIFY_NODE_STATUS_STARTED_FAILED));
            assertTrue(ex.getMessage().contains("Updating Node Status on Node test.ServiceTag failed."));
        }

    }

    @Test
    public void testGeneralException() throws Exception
    {
        try
        {
            when(nodeService.notifyNodeAllocationStatus(anyString(), anyString())).thenThrow(new NullPointerException());
            when(delegateExecution.getVariable(DelegateConstants.NODE_DETAIL)).thenReturn(nodeDetail);

            notifyNodeDiscoveryToUpdateStatusStarted.delegateExecute(delegateExecution);

            fail("Should be an exception.");
        }
        catch (BpmnError error)
        {
            assertTrue(error.getErrorCode().equals(DelegateConstants.NOTIFY_NODE_STATUS_STARTED_FAILED));
            assertThat(error.getMessage(), containsString("Updating Node Status on Node test.ServiceTag failed."));
        }
    }

    @Test
    public void testNodeStatusChangeSuccess() throws Exception
    {
        final NotifyNodeDiscoveryToUpdateStatusStarted nus = spy(new NotifyNodeDiscoveryToUpdateStatusStarted(nodeService));
        when(delegateExecution.getVariable(DelegateConstants.NODE_DETAIL)).thenReturn(nodeDetail);
        when(nodeService.listDiscoveredNodeInfo()).thenReturn(discoveredNodeInfoList);
        when(nodeService.notifyNodeAllocationStatus(anyString(), anyString())).thenReturn(true);

        nus.delegateExecute(delegateExecution);

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(nus, times(2)).updateDelegateStatus(captor.capture());
        assertThat(captor.getValue(), CoreMatchers.containsString("was successful"));
    }

}
