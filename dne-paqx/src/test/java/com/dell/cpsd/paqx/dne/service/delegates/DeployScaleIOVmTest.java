/**
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries.  All Rights Reserved.
 * Dell EMC Confidential/Proprietary Information
 */

package com.dell.cpsd.paqx.dne.service.delegates;

import com.dell.cpsd.paqx.dne.exception.TaskResponseFailureException;
import com.dell.cpsd.paqx.dne.service.NodeService;
import com.dell.cpsd.paqx.dne.service.delegates.model.DelegateRequestModel;
import com.dell.cpsd.paqx.dne.transformers.DeployScaleIoVmRequestTransformer;
import com.dell.cpsd.virtualization.capabilities.api.DeployVMFromTemplateRequestMessage;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static com.dell.cpsd.paqx.dne.service.delegates.utils.DelegateConstants.DEPLOY_SCALEIO_VM_FAILED;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DeployScaleIOVmTest
{
    @Mock
    private NodeService nodeService;

    @Mock
    private DeployScaleIoVmRequestTransformer requestTransformer;

    @Mock
    private DelegateExecution delegateExecution;

    @Mock
    private DelegateRequestModel<DeployVMFromTemplateRequestMessage> requestModel;

    private DeployScaleIOVm delegate;
    private final String serviceTag  = "service-tag";
    private final String vmName      = "vm-name";
    private final String taskMessage = "Deploy ScaleIo Vm";

    @Before
    public void setup() throws Exception
    {
        delegate = new DeployScaleIOVm(nodeService, requestTransformer);
    }

    @Test
    public void unknownExceptionThrownResultsInBpmnError() throws Exception
    {
        final String errorMessage = "Illegal state exception";
        when(requestTransformer.buildDeployVmRequest(delegateExecution)).thenThrow(new IllegalStateException(errorMessage));
        final DeployScaleIOVm spy = spy(delegate);

        try
        {
            spy.delegateExecute(delegateExecution);
        }
        catch (BpmnError error)
        {
            assertThat(error.getMessage(), containsString("An unexpected exception occurred"));
            assertThat(error.getMessage(), containsString(errorMessage));
            assertThat(error.getMessage(), containsString(taskMessage));
            assertTrue(error.getErrorCode().equals(DEPLOY_SCALEIO_VM_FAILED));
        }

        verify(spy).updateDelegateStatus(
                "An unexpected exception occurred attempting to request " + taskMessage + ". Reason: " + errorMessage);
    }

    @Test
    public void taskResponseFailureExceptionThrownDueToServiceTimeoutOrExecution() throws Exception
    {
        final DeployVMFromTemplateRequestMessage mockRequestMessage = mock(DeployVMFromTemplateRequestMessage.class);
        final String errorMessage = "Service timeout";
        when(requestModel.getRequestMessage()).thenReturn(mockRequestMessage);
        when(requestTransformer.buildDeployVmRequest(delegateExecution)).thenReturn(requestModel);
        doThrow(new TaskResponseFailureException(1, errorMessage)).when(nodeService).requestDeployScaleIoVm(mockRequestMessage);

        final DeployScaleIOVm spy = spy(delegate);

        try
        {
            spy.delegateExecute(delegateExecution);
        }
        catch (BpmnError error)
        {
            assertThat(error.getMessage(), containsString("Exception Code: " + 1 + "::" + errorMessage));
            assertTrue(error.getErrorCode().equals(DEPLOY_SCALEIO_VM_FAILED));
        }

        verify(spy).updateDelegateStatus(errorMessage);
    }

    @Test
    public void deployScaleIoVmSuccessUpdatesTheDelegateStatus() throws Exception
    {
        final DeployVMFromTemplateRequestMessage mockRequestMessage = mock(DeployVMFromTemplateRequestMessage.class);

        when(requestModel.getRequestMessage()).thenReturn(mockRequestMessage);
        when(mockRequestMessage.getNewVMName()).thenReturn(vmName);
        when(requestModel.getServiceTag()).thenReturn(serviceTag);
        when(requestTransformer.buildDeployVmRequest(delegateExecution)).thenReturn(requestModel);
        doNothing().when(nodeService).requestDeployScaleIoVm(mockRequestMessage);

        final DeployScaleIOVm spy = spy(delegate);

        spy.delegateExecute(delegateExecution);

        verify(spy).updateDelegateStatus(taskMessage + " on Node " + serviceTag + " was successful.");
        final ArgumentCaptor<String> setVariableCaptor = ArgumentCaptor.forClass(String.class);
        verify(delegateExecution).setVariable(anyString(), setVariableCaptor.capture());
        assertEquals(vmName, setVariableCaptor.getValue());
    }
}
