/**
 * <p>
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries. All Rights Reserved. Dell EMC Confidential/Proprietary Information
 * </p>
 */

package com.dell.cpsd.paqx.dne.service.delegates;

import com.dell.cpsd.paqx.dne.service.delegates.model.NodeDetail;
import com.dell.cpsd.paqx.dne.service.delegates.utils.DelegateConstants;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;
import java.net.UnknownHostException;

import static com.dell.cpsd.paqx.dne.service.delegates.utils.DelegateConstants.NODE_DETAIL;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PingIdracTest
{
    @Mock
    private DelegateExecution delegateExecution;

    private PingIdrac  pingIdrac;
    private NodeDetail nodeDetail;

    @Before
    public void setUp() throws Exception
    {
        pingIdrac = spy(new PingIdrac(10));

        nodeDetail = new NodeDetail();
        nodeDetail.setIdracIpAddress("1.2.3.4");
        nodeDetail.setServiceTag("abc");
    }

    @Test
    public void pingIdracUnknownHostException() throws Exception
    {
        try
        {
            when(delegateExecution.getVariable(NODE_DETAIL)).thenReturn(nodeDetail);
            when(pingIdrac.isIdracReachable(anyString())).thenThrow(new UnknownHostException());

            pingIdrac.delegateExecute(delegateExecution);

            fail("Expected exception to be thrown but was not");
        }
        catch (BpmnError error)
        {
            assertTrue(error.getErrorCode().equals(DelegateConstants.PING_IP_ADDRESS_FAILED));
            assertThat(error.getMessage(), containsString("Unable to determine iDRAC IP Address"));
        }
    }

    @Test
    public void pingIdracIOException() throws Exception
    {
        try
        {
            when(delegateExecution.getVariable(NODE_DETAIL)).thenReturn(nodeDetail);
            when(pingIdrac.isIdracReachable(anyString())).thenThrow(new IOException());

            pingIdrac.delegateExecute(delegateExecution);

            fail("Expected exception to be thrown but was not");
        }
        catch (BpmnError error)
        {
            assertTrue(error.getErrorCode().equals(DelegateConstants.PING_IP_ADDRESS_FAILED));
            assertThat(error.getMessage(), containsString("Unable to reach iDRAC IP Address"));
        }
    }

    @Test
    public void pingIdracGeneralException() throws Exception
    {
        try
        {
            when(delegateExecution.getVariable(NODE_DETAIL)).thenReturn(nodeDetail);
            when(pingIdrac.isIdracReachable(anyString())).thenThrow(new NullPointerException());

            pingIdrac.delegateExecute(delegateExecution);

            fail("Expected exception to be thrown but was not");
        }
        catch (BpmnError error)
        {
            assertTrue(error.getErrorCode().equals(DelegateConstants.PING_IP_ADDRESS_FAILED));
            assertThat(error.getMessage(), containsString("An unexpected exception occurred"));
        }
    }

    @Test
    public void pingIdracFailed() throws Exception
    {
        try
        {
            when(delegateExecution.getVariable(NODE_DETAIL)).thenReturn(nodeDetail);
            when(pingIdrac.isIdracReachable(anyString())).thenReturn(false);

            pingIdrac.delegateExecute(delegateExecution);

            fail("Expected exception to be thrown but was not");
        }
        catch (BpmnError error)
        {
            assertTrue(error.getErrorCode().equals(DelegateConstants.PING_IP_ADDRESS_FAILED));
            assertThat(error.getMessage(), containsString("Unable to contact iDRAC IP Address"));
        }
    }

    @Test
    public void pingIdracSuccess() throws Exception
    {
        when(delegateExecution.getVariable(NODE_DETAIL)).thenReturn(nodeDetail);
        when(pingIdrac.isIdracReachable(anyString())).thenReturn(true);

        pingIdrac.delegateExecute(delegateExecution);

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(pingIdrac, times(2)).updateDelegateStatus(captor.capture());
        assertThat(captor.getAllValues().get(0), CoreMatchers.containsString("Attempting to Ping iDrac"));
        assertThat(captor.getAllValues().get(1), CoreMatchers.containsString("was successful"));
    }
}
