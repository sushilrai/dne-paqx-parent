/**
 * <p>
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries. All Rights Reserved. Dell EMC Confidential/Proprietary Information
 * </p>
 */

package com.dell.cpsd.paqx.dne.service.delegates;

import com.dell.cpsd.paqx.dne.exception.TaskResponseFailureException;
import com.dell.cpsd.paqx.dne.service.NodeService;
import com.dell.cpsd.paqx.dne.service.delegates.model.DelegateRequestModel;
import com.dell.cpsd.paqx.dne.service.delegates.model.NodeDetail;
import com.dell.cpsd.paqx.dne.transformers.ConfigureVmNetworkSettingsRequestTransformer;
import com.dell.cpsd.virtualization.capabilities.api.ConfigureVmNetworkSettingsRequestMessage;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static com.dell.cpsd.paqx.dne.service.delegates.utils.DelegateConstants.CONFIGURE_VM_NETWORK_SETTINGS;
import static com.dell.cpsd.paqx.dne.service.delegates.utils.DelegateConstants.NODE_DETAIL;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ConfigureVMNetworkSettingsTest
{
    @Mock
    private NodeService nodeService;

    @Mock
    private ConfigureVmNetworkSettingsRequestTransformer requestTransformer;

    @Mock
    private DelegateExecution delegateExecution;

    @Mock
    private DelegateRequestModel<ConfigureVmNetworkSettingsRequestMessage> requestModel;

    private ConfigureVMNetworkSettings delegate;
    private final String serviceTag  = "service-tag";
    private final String taskMessage = "Configure VM Network Settings";

    @Before
    public void setup() throws Exception
    {
        delegate = new ConfigureVMNetworkSettings(nodeService, requestTransformer);
        NodeDetail nodeDetail = new NodeDetail("1", serviceTag);
        doReturn(nodeDetail).when(delegateExecution).getVariable(NODE_DETAIL);
    }

    @Test
    public void unknownExceptionThrownResultsInBpmnError() throws Exception
    {
        final String errorMessage = "Illegal state exception";
        when(requestTransformer.buildConfigureVmNetworkSettingsRequest(delegateExecution))
                .thenThrow(new IllegalStateException(errorMessage));

        final ConfigureVMNetworkSettings spy = spy(delegate);
        try
        {
            spy.delegateExecute(delegateExecution);
        }
        catch (BpmnError error)
        {
            assertThat(error.getMessage(), containsString("An unexpected exception occurred"));
            assertThat(error.getMessage(), containsString(errorMessage));
            assertThat(error.getMessage(), containsString(taskMessage));
            assertTrue(error.getErrorCode().equals(CONFIGURE_VM_NETWORK_SETTINGS));
        }

        verify(spy).updateDelegateStatus(
                "Attempting Configure VM Network Settings on Node service-tag.");
    }

    @Test
    public void taskResponseFailureExceptionThrownDueToServiceTimeoutOrExecution() throws Exception
    {
        final ConfigureVmNetworkSettingsRequestMessage mockRequestMessage = mock(ConfigureVmNetworkSettingsRequestMessage.class);
        final String errorMessage = "Service timeout";
        when(requestModel.getRequestMessage()).thenReturn(mockRequestMessage);
        when(requestTransformer.buildConfigureVmNetworkSettingsRequest(delegateExecution)).thenReturn(requestModel);
        doThrow(new TaskResponseFailureException(1, errorMessage)).when(nodeService).requestConfigureVmNetworkSettings(mockRequestMessage);

        final ConfigureVMNetworkSettings spy = spy(delegate);
        try
        {
            spy.delegateExecute(delegateExecution);
        }
        catch (BpmnError error)
        {
            assertThat(error.getMessage(), containsString("An unexpected exception occurred attempting to Configure VM Network Settings on Node service-tag. Reason: Service timeout"));
            assertTrue(error.getErrorCode().equals(CONFIGURE_VM_NETWORK_SETTINGS));
        }

        verify(spy).updateDelegateStatus("Attempting Configure VM Network Settings on Node service-tag.");
    }

    @Test
    public void configureVmNetworkSettingsSuccessUpdatesTheDelegateStatus() throws Exception
    {
        final ConfigureVmNetworkSettingsRequestMessage mockRequestMessage = mock(ConfigureVmNetworkSettingsRequestMessage.class);

        when(requestModel.getRequestMessage()).thenReturn(mockRequestMessage);
        when(requestTransformer.buildConfigureVmNetworkSettingsRequest(delegateExecution)).thenReturn(requestModel);
        doNothing().when(nodeService).requestConfigureVmNetworkSettings(mockRequestMessage);

        final ConfigureVMNetworkSettings spy = spy(delegate);
        spy.delegateExecute(delegateExecution);

        verify(spy).updateDelegateStatus(taskMessage + " on Node " + serviceTag + " was successful.");
    }
}
