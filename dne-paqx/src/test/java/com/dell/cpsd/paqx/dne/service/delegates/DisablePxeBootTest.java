/**
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries.  All Rights Reserved.
 * Dell EMC Confidential/Proprietary Information
 **/

package com.dell.cpsd.paqx.dne.service.delegates;

import com.dell.cpsd.paqx.dne.amqp.callback.AsynchronousNodeServiceCallback;
import com.dell.cpsd.paqx.dne.exception.TaskResponseFailureException;
import com.dell.cpsd.paqx.dne.service.AsynchronousNodeService;
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

import static com.dell.cpsd.paqx.dne.service.delegates.utils.DelegateConstants.NODE_DETAIL;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Test class for disable PXE boot request
 * <p>
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries. All Rights Reserved.
 * Dell EMC Confidential/Proprietary Information
 * </p>
 *
 * @version 1.0
 * @since 1.0
 */
@RunWith(MockitoJUnitRunner.class)
public class DisablePxeBootTest
{
    @Mock
    private DelegateExecution delegateExecution;

    @Mock
    private AsynchronousNodeService asynchronousNodeService;

    private DisablePxeBoot disablePxeBoot;

    @Before
    public void setUp() throws Exception
    {
        disablePxeBoot = new DisablePxeBoot(asynchronousNodeService);
        NodeDetail nodeDetail = new NodeDetail("1", "service-tag");
        doReturn(nodeDetail).when(delegateExecution).getVariable(NODE_DETAIL);
    }

    @Test
    public void testTaskResponseFailureException() throws Exception
    {
        final String exceptionMsg = "request failed";

        try
        {
            willThrow(new TaskResponseFailureException(1, exceptionMsg)).given(asynchronousNodeService).processConfigurePxeBootResponse(any());

            disablePxeBoot.delegateExecute(delegateExecution);

            fail("Expected exception to be thrown but was not");
        }
        catch (BpmnError error)
        {
            assertTrue(error.getErrorCode().equals(DelegateConstants.CONFIGURE_PXE_FAILED));
            assertThat(error.getMessage(), containsString(exceptionMsg));
        }
    }

    @Test
    public void testGeneralException() throws Exception
    {
        try
        {
            willThrow(new NullPointerException("Test")).given(asynchronousNodeService).processConfigurePxeBootResponse(any());
            disablePxeBoot.delegateExecute(delegateExecution);

            fail("Expected exception to be thrown but was not");
        }
        catch (BpmnError error)
        {
            assertTrue(error.getErrorCode().equals(DelegateConstants.CONFIGURE_PXE_FAILED));
            assertThat(error.getMessage(), containsString("An Unexpected Exception occurred attempting to Configure PXE boot on Node service-tag. Reason: Test"));
        }
    }

    @Test
    public void testSuccess() throws Exception
    {
        final DisablePxeBoot spy = spy(new DisablePxeBoot(asynchronousNodeService));
        final NodeDetail mockNodeDetail = mock(NodeDetail.class);
        final AsynchronousNodeServiceCallback serviceCallback = mock(AsynchronousNodeServiceCallback.class);

        doNothing().when(asynchronousNodeService).processConfigurePxeBootResponse(any());
        when(mockNodeDetail.getServiceTag()).thenReturn("1234");
        when(delegateExecution.getVariable(DelegateConstants.NODE_DETAIL)).thenReturn(mockNodeDetail);
        when(delegateExecution.getVariable(DelegateConstants.CONFIGURE_PXE_BOOT_MESSAGE_ID)).thenReturn(serviceCallback);

        spy.delegateExecute(delegateExecution);

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(spy, times(2)).updateDelegateStatus(captor.capture());
        assertThat(captor.getValue(), CoreMatchers.containsString("was successful"));
    }
}