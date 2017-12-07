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
import com.dell.cpsd.paqx.dne.transformers.SdcPerformanceProfileRequestTransformer;
import com.dell.cpsd.storage.capabilities.api.SioSdcUpdatePerformanceProfileRequestMessage;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static com.dell.cpsd.paqx.dne.service.delegates.utils.DelegateConstants.NODE_DETAIL;
import static com.dell.cpsd.paqx.dne.service.delegates.utils.DelegateConstants.UPDATE_SDC_PERFORMANCE_PROFILE_FAILED;
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
public class UpdateSDCPerformanceProfileTest
{
    @Mock
    private NodeService nodeService;

    @Mock
    private SdcPerformanceProfileRequestTransformer sdcPerformanceProfileRequestTransformer;

    @Mock
    private DelegateExecution delegateExecution;

    @Mock
    private DelegateRequestModel<SioSdcUpdatePerformanceProfileRequestMessage> requestModel;

    private UpdateSDCPerformanceProfile updateSDCPerformanceProfile;
    private final String serviceTag = "service-tag";

    @Before
    public void setup() throws Exception
    {
        updateSDCPerformanceProfile = new UpdateSDCPerformanceProfile(nodeService, sdcPerformanceProfileRequestTransformer);
        NodeDetail nodeDetail = new NodeDetail("1", serviceTag);
        doReturn(nodeDetail).when(delegateExecution).getVariable(NODE_DETAIL);
    }

    @Test
    public void unknownExceptionThrownResultsInBpmnError() throws Exception
    {
        final String errorMessage = "Illegal state exception";
        when(sdcPerformanceProfileRequestTransformer.buildSdcPerformanceProfileRequest(delegateExecution))
                .thenThrow(new IllegalStateException(errorMessage));

        final UpdateSDCPerformanceProfile updateSDCPerformanceProfileSpy = spy(updateSDCPerformanceProfile);
        try
        {
            updateSDCPerformanceProfileSpy.delegateExecute(delegateExecution);
        }
        catch (BpmnError error)
        {
            assertThat(error.getMessage(), containsString("An unexpected exception occurred"));
            assertThat(error.getMessage(), containsString(errorMessage));
            assertThat(error.getMessage(), containsString("Update ScaleIO SDC Performance Profile"));
            assertTrue(error.getErrorCode().equals(UPDATE_SDC_PERFORMANCE_PROFILE_FAILED));
        }

        verify(updateSDCPerformanceProfileSpy).updateDelegateStatus(
                "Attempting Update ScaleIO SDC Performance Profile on Node service-tag.");
    }

    @Test
    public void taskResponseFailureExceptionThrownDueToServiceTimeoutOrExecution() throws Exception
    {
        final SioSdcUpdatePerformanceProfileRequestMessage mockRequestMessage = mock(SioSdcUpdatePerformanceProfileRequestMessage.class);
        final String errorMessage = "Service timeout";
        when(requestModel.getRequestMessage()).thenReturn(mockRequestMessage);
        when(sdcPerformanceProfileRequestTransformer.buildSdcPerformanceProfileRequest(delegateExecution)).thenReturn(requestModel);
        doThrow(new TaskResponseFailureException(1, errorMessage)).when(nodeService).requestUpdateSdcPerformanceProfile(mockRequestMessage);

        final UpdateSDCPerformanceProfile updateSDCPerformanceProfileSpy = spy(updateSDCPerformanceProfile);
        try
        {
            updateSDCPerformanceProfileSpy.delegateExecute(delegateExecution);
        }
        catch (BpmnError error)
        {
            assertThat(error.getMessage(), containsString("An unexpected exception occurred attempting to request Update ScaleIO SDC Performance Profile on Node service-tag. Reason: Service timeout"));
            assertTrue(error.getErrorCode().equals(UPDATE_SDC_PERFORMANCE_PROFILE_FAILED));
        }

        verify(updateSDCPerformanceProfileSpy).updateDelegateStatus("Attempting Update ScaleIO SDC Performance Profile on Node service-tag.");
    }

    @Test
    public void updateSdcPerformanceProfileSuccessUpdatesTheDelegateStatus() throws Exception
    {
        final SioSdcUpdatePerformanceProfileRequestMessage mockRequestMessage = mock(SioSdcUpdatePerformanceProfileRequestMessage.class);

        when(requestModel.getRequestMessage()).thenReturn(mockRequestMessage);
        when(sdcPerformanceProfileRequestTransformer.buildSdcPerformanceProfileRequest(delegateExecution)).thenReturn(requestModel);
        doNothing().when(nodeService).requestUpdateSdcPerformanceProfile(mockRequestMessage);

        final UpdateSDCPerformanceProfile updateSDCPerformanceProfileSpy = spy(updateSDCPerformanceProfile);
        updateSDCPerformanceProfileSpy.delegateExecute(delegateExecution);

        verify(updateSDCPerformanceProfileSpy)
                .updateDelegateStatus("Update ScaleIO SDC Performance Profile on Node " + serviceTag + " was successful.");
    }
}
