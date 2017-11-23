/**
 * <p>
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries. All Rights Reserved. Dell EMC Confidential/Proprietary Information
 * </p>
 */

package com.dell.cpsd.paqx.dne.service.delegates;

import com.dell.cpsd.paqx.dne.exception.TaskResponseFailureException;
import com.dell.cpsd.paqx.dne.service.NodeService;
import com.dell.cpsd.paqx.dne.service.delegates.model.DelegateRequestModel;
import com.dell.cpsd.paqx.dne.service.delegates.utils.DelegateConstants;
import com.dell.cpsd.paqx.dne.transformers.HostPowerOperationsTransformer;
import com.dell.cpsd.virtualization.capabilities.api.HostPowerOperationRequestMessage;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.BDDMockito.when;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class RebootHostTest
{
    @Mock
    private HostPowerOperationsTransformer hostPowerOperationsRequestTransformer;

    @Mock
    private DelegateExecution delegateExecution;

    @Mock
    private NodeService nodeService;

    @Mock
    private DelegateRequestModel<HostPowerOperationRequestMessage> requestModel;

    private RebootHost rebootHost;

    @Before
    public void setUp() throws Exception
    {
        rebootHost = new RebootHost(nodeService, hostPowerOperationsRequestTransformer);
    }

    @Test
    public void testTaskResponseFailureException() throws Exception
    {
        final String exceptionMsg = "request failed";

        try
        {
            when(hostPowerOperationsRequestTransformer.buildHostPowerOperationsRequestMessage(any(), any())).thenReturn(requestModel);
            willThrow(new TaskResponseFailureException(1, exceptionMsg)).given(nodeService).requestHostReboot(any());

            rebootHost.delegateExecute(delegateExecution);

            fail("Expected exception to be thrown but was not");
        }
        catch (BpmnError error)
        {
            assertTrue(error.getErrorCode().equals(DelegateConstants.REBOOT_HOST_FAILED));
            assertThat(error.getMessage(), containsString(exceptionMsg));

        }
    }

    @Test
    public void testGeneralException() throws Exception
    {
        try
        {
            when(hostPowerOperationsRequestTransformer.buildHostPowerOperationsRequestMessage(any(), any()))
                    .thenThrow(new NullPointerException());

            rebootHost.delegateExecute(delegateExecution);

            fail("Expected exception to be thrown but was not");
        }
        catch (BpmnError error)
        {
            assertTrue(error.getErrorCode().equals(DelegateConstants.REBOOT_HOST_FAILED));
            assertThat(error.getMessage(), containsString("An Unexpected Exception occurred attempting to request Reboot Host"));
        }
    }

    @Test
    public void testSuccess() throws Exception
    {
        when(hostPowerOperationsRequestTransformer.buildHostPowerOperationsRequestMessage(any(), any())).thenReturn(requestModel);
        final RebootHost rebootHostSpy = spy(new RebootHost(nodeService, hostPowerOperationsRequestTransformer));

        rebootHostSpy.delegateExecute(delegateExecution);

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(rebootHostSpy).updateDelegateStatus(captor.capture());
        assertThat(captor.getValue(), CoreMatchers.containsString("was successful"));
    }
}
