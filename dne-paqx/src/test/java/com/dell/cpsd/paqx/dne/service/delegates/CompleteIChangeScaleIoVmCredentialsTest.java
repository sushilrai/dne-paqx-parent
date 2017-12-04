/**
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries.  All Rights Reserved.
 * Dell EMC Confidential/Proprietary Information
 */

package com.dell.cpsd.paqx.dne.service.delegates;

import com.dell.cpsd.paqx.dne.amqp.callback.AsynchronousNodeServiceCallback;
import com.dell.cpsd.paqx.dne.service.AsynchronousNodeService;
import com.dell.cpsd.paqx.dne.service.delegates.CompleteChangeScaleIoVmCredentials;
import com.dell.cpsd.paqx.dne.service.delegates.model.NodeDetail;
import com.dell.cpsd.paqx.dne.service.delegates.utils.DelegateConstants;
import com.dell.cpsd.service.common.client.exception.ServiceExecutionException;
import com.dell.cpsd.virtualization.capabilities.api.RemoteCommandExecutionResponseMessage;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static com.dell.cpsd.paqx.dne.service.delegates.utils.DelegateConstants.CHANGE_SCALEIO_VM_CREDENTIALS_MESSAGE_ID;
import static com.dell.cpsd.paqx.dne.service.delegates.utils.DelegateConstants.NODE_DETAIL;
import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class CompleteIChangeScaleIoVmCredentialsTest
{
    private CompleteChangeScaleIoVmCredentials completeChangeScaleIoVmCredentials;
    private NodeDetail                         nodeDetail;

    @Mock
    private AsynchronousNodeService asynchronousNodeService;

    @Mock
    private DelegateExecution delegateExecution;

    @Mock
    private AsynchronousNodeServiceCallback<?> asynchronousNodeServiceCallback;

    @Before
    public void setUp()
    {
        completeChangeScaleIoVmCredentials = new CompleteChangeScaleIoVmCredentials(asynchronousNodeService);

        doReturn(true).when(asynchronousNodeServiceCallback).isDone();

        nodeDetail = new NodeDetail();
        nodeDetail.setId("1");
        nodeDetail.setIdracIpAddress("1");
        nodeDetail.setIdracGatewayIpAddress("1");
        nodeDetail.setIdracSubnetMask("1");
        nodeDetail.setServiceTag("abc");
        nodeDetail.setEsxiManagementHostname("hostName");
        doReturn(nodeDetail).when(delegateExecution).getVariable(NODE_DETAIL);

        doReturn(asynchronousNodeServiceCallback).when(this.delegateExecution).getVariable(CHANGE_SCALEIO_VM_CREDENTIALS_MESSAGE_ID);
    }

    @Test
    public void testSuccess() throws Exception
    {
        doReturn(RemoteCommandExecutionResponseMessage.Status.SUCCESS).when(asynchronousNodeService)
                .processRemoteCommandResponse(asynchronousNodeServiceCallback);

        CompleteChangeScaleIoVmCredentials completeChangeScaleIoVmCredentialsSpy = spy(completeChangeScaleIoVmCredentials);

        completeChangeScaleIoVmCredentialsSpy.delegateExecute(delegateExecution);

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(completeChangeScaleIoVmCredentialsSpy).updateDelegateStatus(captor.capture());
        assertThat(captor.getValue(), containsString("was successful"));
    }

    @Test
    public void testException() throws Exception
    {
        String errorMessage = "Fake Exception";
        doThrow(new ServiceExecutionException(errorMessage)).when(asynchronousNodeService)
                .processRemoteCommandResponse(asynchronousNodeServiceCallback);
        try
        {
            completeChangeScaleIoVmCredentials.delegateExecute(delegateExecution);
        }
        catch (BpmnError error)
        {
            assertTrue(error.getErrorCode().equals(DelegateConstants.COMPLETE_CHANGE_SCALEIO_VM_CREDENTIALS_FAILED));
            assertTrue((error.getMessage().contains(errorMessage)));
        }
    }

    @Test
    public void testFailed() throws Exception
    {
        doReturn(RemoteCommandExecutionResponseMessage.Status.FAILED).when(asynchronousNodeService).processRemoteCommandResponse(asynchronousNodeServiceCallback);
        try
        {
            completeChangeScaleIoVmCredentials.delegateExecute(delegateExecution);
        }
        catch (BpmnError error)
        {
            assertTrue(error.getErrorCode().equals(DelegateConstants.COMPLETE_CHANGE_SCALEIO_VM_CREDENTIALS_FAILED));
            assertTrue((error.getMessage().equals("Complete Change ScaleIo Vm Credentials on Node abc failed!")));
        }
    }
}
