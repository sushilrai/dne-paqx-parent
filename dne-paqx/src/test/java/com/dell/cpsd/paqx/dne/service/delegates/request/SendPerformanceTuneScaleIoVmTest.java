/**
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries.  All Rights Reserved.
 * Dell EMC Confidential/Proprietary Information
 */

package com.dell.cpsd.paqx.dne.service.delegates.request;

import com.dell.cpsd.paqx.dne.amqp.callback.AsynchronousNodeServiceCallback;
import com.dell.cpsd.paqx.dne.exception.TaskResponseFailureException;
import com.dell.cpsd.paqx.dne.service.AsynchronousNodeService;
import com.dell.cpsd.paqx.dne.service.delegates.model.DelegateRequestModel;
import com.dell.cpsd.paqx.dne.service.delegates.model.NodeDetail;
import com.dell.cpsd.paqx.dne.service.delegates.utils.DelegateConstants;
import com.dell.cpsd.paqx.dne.transformers.RemoteCommandExecutionRequestTransformer;
import com.dell.cpsd.service.common.client.callback.ServiceResponse;
import com.dell.cpsd.virtualization.capabilities.api.RemoteCommandExecutionRequestMessage;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static com.dell.cpsd.paqx.dne.service.delegates.utils.DelegateConstants.NODE_DETAIL;
import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class SendPerformanceTuneScaleIoVmTest
{
    private SendPerformanceTuneScaleIoVm sendPerformanceTuneScaleIoVm;

    @Mock
    private RemoteCommandExecutionRequestTransformer remoteCommandExecutionRequestTransformer;

    @Mock
    private AsynchronousNodeService asynchronousNodeService;

    @Mock
    private DelegateExecution delegateExecution;

    @Mock
    private DelegateRequestModel<RemoteCommandExecutionRequestMessage> requestModel;

    @Mock
    private AsynchronousNodeServiceCallback<ServiceResponse<?>> asynchronousNodeServiceCallback;

    private NodeDetail nodeDetail;

    @Before
    public void setUp() throws Exception
    {
        sendPerformanceTuneScaleIoVm = new SendPerformanceTuneScaleIoVm(asynchronousNodeService, remoteCommandExecutionRequestTransformer);

        doReturn(asynchronousNodeServiceCallback).when(asynchronousNodeService).executeRemoteCommand(any(), any(), any(), any());

        nodeDetail = new NodeDetail();
        nodeDetail.setId("1");
        nodeDetail.setIdracIpAddress("1");
        nodeDetail.setIdracGatewayIpAddress("1");
        nodeDetail.setIdracSubnetMask("1");
        nodeDetail.setServiceTag("abc");
        nodeDetail.setEsxiManagementHostname("hostName");

        doReturn(nodeDetail).when(delegateExecution).getVariable(NODE_DETAIL);
        doReturn("1").when(delegateExecution).getProcessInstanceId();

        doReturn(requestModel).when(remoteCommandExecutionRequestTransformer).buildRemoteCodeExecutionRequest(delegateExecution,
                RemoteCommandExecutionRequestMessage.RemoteCommand.PERFORMANCE_TUNING_SVM);
    }

    @Test
    public void delegateExecuteSuccess() throws Exception
    {
        SendPerformanceTuneScaleIoVm sendPerformanceTuneScaleIoVmSpy = spy(sendPerformanceTuneScaleIoVm);

        sendPerformanceTuneScaleIoVmSpy.delegateExecute(delegateExecution);

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(sendPerformanceTuneScaleIoVmSpy).updateDelegateStatus(captor.capture());
        assertThat(captor.getValue(), containsString("was successful"));
    }

    @Test
    public void delegateExecuteRequestUnsuccessful() throws Exception
    {

        doReturn(null).when(asynchronousNodeService).executeRemoteCommand(any(), any(), any(), any());
        try
        {
            sendPerformanceTuneScaleIoVm.delegateExecute(delegateExecution);
            fail("An exception was expected.");
        }
        catch (BpmnError error)
        {
            assertTrue(error.getErrorCode().equals(DelegateConstants.SEND_PERFORMANCE_TUNE_SCALEIO_VM_FAILED));
            assertThat(error.getMessage(), containsString("Failed to send the request"));
        }
    }

    @Test
    public void delegateRequestException()  throws Exception
    {
        String errorMessage = "Fake Exception";

        doThrow(new TaskResponseFailureException(1, errorMessage)).when(asynchronousNodeService)
                .executeRemoteCommand(any(), any(), any(), any());

        try
        {
            sendPerformanceTuneScaleIoVm.delegateExecute(delegateExecution);
        }
        catch (BpmnError error)
        {
            assertTrue(error.getErrorCode().equals(DelegateConstants.SEND_PERFORMANCE_TUNE_SCALEIO_VM_FAILED));
            assertThat(error.getMessage(), containsString(errorMessage));
        }

    }
}
