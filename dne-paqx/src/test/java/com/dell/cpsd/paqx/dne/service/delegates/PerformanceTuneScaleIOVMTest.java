/**
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries.  All Rights Reserved.
 * Dell EMC Confidential/Proprietary Information
 */

package com.dell.cpsd.paqx.dne.service.delegates;

import com.dell.cpsd.paqx.dne.exception.TaskResponseFailureException;
import com.dell.cpsd.paqx.dne.service.NodeService;
import com.dell.cpsd.paqx.dne.service.delegates.model.DelegateRequestModel;
import com.dell.cpsd.paqx.dne.transformers.RemoteCommandExecutionRequestTransformer;
import com.dell.cpsd.virtualization.capabilities.api.RemoteCommandExecutionRequestMessage;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static com.dell.cpsd.paqx.dne.service.delegates.utils.DelegateConstants.PERFORMANCE_TUNE_SCALEIO_VM;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PerformanceTuneScaleIOVMTest
{
    @Mock
    private NodeService nodeService;

    @Mock
    private RemoteCommandExecutionRequestTransformer requestTransformer;

    @Mock
    private DelegateExecution delegateExecution;

    @Mock
    private DelegateRequestModel<RemoteCommandExecutionRequestMessage> requestModel;

    private PerformanceTuneScaleIOVM delegate;
    private final String serviceTag  = "service-tag";
    private final String taskMessage = "Performance Tune Scale IO VM";

    @Before
    public void setup() throws Exception
    {
        delegate = new PerformanceTuneScaleIOVM(nodeService, requestTransformer);
    }

    @Test
    public void unknownExceptionThrownResultsInBpmnError() throws Exception
    {
        final String errorMessage = "Illegal state exception";
        when(requestTransformer.buildRemoteCodeExecutionRequest(delegateExecution,
                RemoteCommandExecutionRequestMessage.RemoteCommand.PERFORMANCE_TUNING_SVM))
                .thenThrow(new IllegalStateException(errorMessage));

        final PerformanceTuneScaleIOVM spy = spy(delegate);
        try
        {
            spy.delegateExecute(delegateExecution);
        }
        catch (BpmnError error)
        {
            assertThat(error.getMessage(), containsString("An unexpected exception occurred"));
            assertThat(error.getMessage(), containsString(errorMessage));
            assertThat(error.getMessage(), containsString(taskMessage));
            assertTrue(error.getErrorCode().equals(PERFORMANCE_TUNE_SCALEIO_VM));
        }

        verify(spy).updateDelegateStatus(
                "An unexpected exception occurred attempting to request " + taskMessage + ". Reason: " + errorMessage);
    }

    @Test
    public void taskResponseFailureExceptionThrownDueToServiceTimeoutOrExecution() throws Exception
    {
        final RemoteCommandExecutionRequestMessage mockRequestMessage = mock(RemoteCommandExecutionRequestMessage.class);
        final String errorMessage = "Service timeout";
        when(requestModel.getRequestMessage()).thenReturn(mockRequestMessage);
        when(requestTransformer.buildRemoteCodeExecutionRequest(delegateExecution,
                RemoteCommandExecutionRequestMessage.RemoteCommand.PERFORMANCE_TUNING_SVM)).thenReturn(requestModel);
        doThrow(new TaskResponseFailureException(1, errorMessage)).when(nodeService).requestRemoteCommandExecution(mockRequestMessage);

        final PerformanceTuneScaleIOVM spy = spy(delegate);
        try
        {
            spy.delegateExecute(delegateExecution);
        }
        catch (BpmnError error)
        {
            assertThat(error.getMessage(), containsString("Exception Code: " + 1 + "::" + errorMessage));
            assertTrue(error.getErrorCode().equals(PERFORMANCE_TUNE_SCALEIO_VM));
        }

        verify(spy).updateDelegateStatus(errorMessage);
    }

    @Test
    public void performanceTuneScaleIoVmSuccessUpdatesTheDelegateStatus() throws Exception
    {
        final RemoteCommandExecutionRequestMessage mockRequestMessage = mock(RemoteCommandExecutionRequestMessage.class);

        when(requestModel.getRequestMessage()).thenReturn(mockRequestMessage);
        when(requestModel.getServiceTag()).thenReturn(serviceTag);
        when(requestTransformer.buildRemoteCodeExecutionRequest(delegateExecution,
                RemoteCommandExecutionRequestMessage.RemoteCommand.PERFORMANCE_TUNING_SVM)).thenReturn(requestModel);
        doNothing().when(nodeService).requestRemoteCommandExecution(mockRequestMessage);

        final PerformanceTuneScaleIOVM spy = spy(delegate);
        spy.delegateExecute(delegateExecution);

        verify(spy).updateDelegateStatus(taskMessage + " on Node " + serviceTag + " was successful.");
    }
}
