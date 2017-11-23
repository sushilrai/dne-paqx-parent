/**
 * <p>
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries. All Rights Reserved. Dell EMC Confidential/Proprietary Information
 * </p>
 */

package com.dell.cpsd.paqx.dne.service.delegates;

import com.dell.cpsd.paqx.dne.exception.TaskResponseFailureException;
import com.dell.cpsd.paqx.dne.service.NodeService;
import com.dell.cpsd.paqx.dne.service.delegates.model.DelegateRequestModel;
import com.dell.cpsd.paqx.dne.transformers.PciPassThroughRequestTransformer;
import com.dell.cpsd.virtualization.capabilities.api.EnablePCIPassthroughRequestMessage;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static com.dell.cpsd.paqx.dne.service.delegates.utils.DelegateConstants.ENABLE_PCI_PASSTHROUGH_FAILED;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class EnablePCIPassThroughTest
{
    @Mock
    private NodeService nodeService;

    @Mock
    private PciPassThroughRequestTransformer requestTransformer;

    @Mock
    private DelegateExecution delegateExecution;

    @Mock
    private DelegateRequestModel<EnablePCIPassthroughRequestMessage> requestModel;

    private EnablePCIPassThrough delegate;
    private final String serviceTag      = "service-tag";
    private final String hostPciDeviceId = "0000:02:00.0";
    private final String taskMessage     = "Enable PCI pass through for ESXi host";

    @Before
    public void setup() throws Exception
    {
        delegate = new EnablePCIPassThrough(nodeService, requestTransformer);
    }

    @Test
    public void unknownExceptionThrownResultsInBpmnError() throws Exception
    {
        final String errorMessage = "Illegal state exception";
        when(requestTransformer.buildEnablePciPassThroughRequest(delegateExecution)).thenThrow(new IllegalStateException(errorMessage));
        final EnablePCIPassThrough spy = spy(delegate);

        try
        {
            spy.delegateExecute(delegateExecution);
        }
        catch (BpmnError error)
        {
            assertThat(error.getMessage(), containsString("An unexpected exception occurred"));
            assertThat(error.getMessage(), containsString(errorMessage));
            assertThat(error.getMessage(), containsString(taskMessage));
            assertTrue(error.getErrorCode().equals(ENABLE_PCI_PASSTHROUGH_FAILED));
        }

        verify(spy).updateDelegateStatus(
                "An unexpected exception occurred attempting to request " + taskMessage + ". Reason: " + errorMessage);
    }

    @Test
    public void taskResponseFailureExceptionThrownDueToServiceTimeoutOrExecution() throws Exception
    {
        final EnablePCIPassthroughRequestMessage mockRequestMessage = mock(EnablePCIPassthroughRequestMessage.class);
        final String errorMessage = "Service timeout";
        when(requestModel.getRequestMessage()).thenReturn(mockRequestMessage);
        when(requestTransformer.buildEnablePciPassThroughRequest(delegateExecution)).thenReturn(requestModel);
        doThrow(new TaskResponseFailureException(1, errorMessage)).when(nodeService).requestEnablePciPassThrough(mockRequestMessage);

        final EnablePCIPassThrough spy = spy(delegate);

        try
        {
            spy.delegateExecute(delegateExecution);
        }
        catch (BpmnError error)
        {
            assertThat(error.getMessage(), containsString("Exception Code: " + 1 + "::" + errorMessage));
            assertTrue(error.getErrorCode().equals(ENABLE_PCI_PASSTHROUGH_FAILED));
        }

        verify(spy).updateDelegateStatus(errorMessage);
    }

    @Test
    public void enablePciPassThroughSuccessUpdatesTheDelegateStatus() throws Exception
    {
        final EnablePCIPassthroughRequestMessage mockRequestMessage = mock(EnablePCIPassthroughRequestMessage.class);

        when(requestModel.getRequestMessage()).thenReturn(mockRequestMessage);
        when(mockRequestMessage.getHostPciDeviceId()).thenReturn(hostPciDeviceId);
        when(requestModel.getServiceTag()).thenReturn(serviceTag);
        when(requestTransformer.buildEnablePciPassThroughRequest(delegateExecution)).thenReturn(requestModel);
        doNothing().when(nodeService).requestEnablePciPassThrough(mockRequestMessage);

        final EnablePCIPassThrough spy = spy(delegate);

        spy.delegateExecute(delegateExecution);

        verify(spy).updateDelegateStatus(taskMessage + " on Node " + serviceTag + " was successful.");
        final ArgumentCaptor<String> setVariableCaptor = ArgumentCaptor.forClass(String.class);
        verify(delegateExecution).setVariable(anyString(), setVariableCaptor.capture());
        assertEquals(hostPciDeviceId, setVariableCaptor.getValue());
    }
}
