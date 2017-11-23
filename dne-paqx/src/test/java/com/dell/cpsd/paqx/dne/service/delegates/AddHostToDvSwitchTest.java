/**
 * <p>
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries. All Rights Reserved. Dell EMC Confidential/Proprietary Information
 * </p>
 */

package com.dell.cpsd.paqx.dne.service.delegates;

import com.dell.cpsd.paqx.dne.exception.TaskResponseFailureException;
import com.dell.cpsd.paqx.dne.service.NodeService;
import com.dell.cpsd.paqx.dne.service.delegates.model.DelegateRequestModel;
import com.dell.cpsd.paqx.dne.transformers.ConfigureDvSwitchesTransformer;
import com.dell.cpsd.virtualization.capabilities.api.AddHostToDvSwitchRequestMessage;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static com.dell.cpsd.paqx.dne.service.delegates.utils.DelegateConstants.ADD_HOST_TO_DV_SWITCH_FAILED;
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
public class AddHostToDvSwitchTest
{
    @Mock
    private NodeService nodeService;

    @Mock
    private ConfigureDvSwitchesTransformer configureDvSwitchesTransformer;

    @Mock
    private DelegateExecution delegateExecution;

    @Mock
    private DelegateRequestModel<AddHostToDvSwitchRequestMessage> requestModel;

    private AddHostToDvSwitch addHostToDvSwitch;
    private final String serviceTag = "service-tag";

    @Before
    public void setup() throws Exception
    {
        addHostToDvSwitch = new AddHostToDvSwitch(nodeService, configureDvSwitchesTransformer);
    }

    @Test
    public void unknownExceptionThrownResultsInBpmnError() throws Exception
    {
        final String errorMessage = "Illegal state exception";
        when(configureDvSwitchesTransformer.buildAddHostToDvSwitchRequest(delegateExecution))
                .thenThrow(new IllegalStateException(errorMessage));

        final AddHostToDvSwitch spy = spy(addHostToDvSwitch);
        try
        {
            spy.delegateExecute(delegateExecution);
        }
        catch (BpmnError error)
        {
            assertThat(error.getMessage(), containsString("An unexpected exception occurred"));
            assertThat(error.getMessage(), containsString(errorMessage));
            assertThat(error.getMessage(), containsString("Add Host To DV Switch"));
            assertTrue(error.getErrorCode().equals(ADD_HOST_TO_DV_SWITCH_FAILED));
        }

        verify(spy).updateDelegateStatus(
                "An unexpected exception occurred attempting to request Add Host To DV Switch. Reason: " + errorMessage);
    }

    @Test
    public void taskResponseFailureExceptionThrownDueToServiceTimeoutOrExecution() throws Exception
    {
        final AddHostToDvSwitchRequestMessage mockRequestMessage = mock(AddHostToDvSwitchRequestMessage.class);
        final String errorMessage = "Service timeout";
        when(requestModel.getRequestMessage()).thenReturn(mockRequestMessage);
        when(configureDvSwitchesTransformer.buildAddHostToDvSwitchRequest(delegateExecution)).thenReturn(requestModel);
        doThrow(new TaskResponseFailureException(1, errorMessage)).when(nodeService).requestAddHostToDvSwitch(mockRequestMessage);

        final AddHostToDvSwitch spy = spy(addHostToDvSwitch);
        try
        {
            spy.delegateExecute(delegateExecution);
        }
        catch (BpmnError error)
        {
            assertThat(error.getMessage(), containsString("Exception Code: " + 1 + "::" + errorMessage));
            assertTrue(error.getErrorCode().equals(ADD_HOST_TO_DV_SWITCH_FAILED));
        }

        verify(spy).updateDelegateStatus(errorMessage);
    }

    @Test
    public void addHostToDvSwitchSuccessUpdatesTheDelegateStatus() throws Exception
    {
        final AddHostToDvSwitchRequestMessage mockRequestMessage = mock(AddHostToDvSwitchRequestMessage.class);

        when(requestModel.getRequestMessage()).thenReturn(mockRequestMessage);
        when(requestModel.getServiceTag()).thenReturn(serviceTag);
        when(configureDvSwitchesTransformer.buildAddHostToDvSwitchRequest(delegateExecution)).thenReturn(requestModel);
        doNothing().when(nodeService).requestAddHostToDvSwitch(mockRequestMessage);

        final AddHostToDvSwitch spy = spy(addHostToDvSwitch);
        spy.delegateExecute(delegateExecution);

        verify(spy).updateDelegateStatus("Add Host To DV Switch on Node " + serviceTag + " was successful.");
    }
}
