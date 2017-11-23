/**
 * <p>
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries. All Rights Reserved. Dell EMC Confidential/Proprietary Information
 * </p>
 */

package com.dell.cpsd.paqx.dne.service.delegates;

import com.dell.cpsd.paqx.dne.exception.TaskResponseFailureException;
import com.dell.cpsd.paqx.dne.service.NodeService;
import com.dell.cpsd.paqx.dne.service.delegates.model.DelegateRequestModel;
import com.dell.cpsd.paqx.dne.transformers.ApplyEsxiLicenseRequestTransformer;
import com.dell.cpsd.virtualization.capabilities.api.AddEsxiHostVSphereLicenseRequest;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static com.dell.cpsd.paqx.dne.service.delegates.utils.DelegateConstants.APPLY_ESXI_LICENSE_FAILED;
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
public class ApplyEsxiLicenseTest
{
    @Mock
    private NodeService nodeService;

    @Mock
    private ApplyEsxiLicenseRequestTransformer requestTransformer;

    @Mock
    private DelegateExecution delegateExecution;

    @Mock
    private DelegateRequestModel<AddEsxiHostVSphereLicenseRequest> requestModel;

    private ApplyEsxiLicense delegate;
    private final String serviceTag = "service-tag";

    @Before
    public void setup() throws Exception
    {
        delegate = new ApplyEsxiLicense(nodeService, requestTransformer);
    }

    @Test
    public void unknownExceptionThrownResultsInBpmnError() throws Exception
    {
        final String errorMessage = "Illegal state exception";
        when(requestTransformer.buildApplyEsxiLicenseRequest(delegateExecution)).thenThrow(new IllegalStateException(errorMessage));

        final ApplyEsxiLicense spy = spy(delegate);
        try
        {
            spy.delegateExecute(delegateExecution);
        }
        catch (BpmnError error)
        {
            assertThat(error.getMessage(), containsString("An unexpected exception occurred"));
            assertThat(error.getMessage(), containsString(errorMessage));
            assertThat(error.getMessage(), containsString("Apply Esxi License"));
            assertTrue(error.getErrorCode().equals(APPLY_ESXI_LICENSE_FAILED));
        }

        verify(spy)
                .updateDelegateStatus("An unexpected exception occurred attempting to request Apply Esxi License. Reason: " + errorMessage);
    }

    @Test
    public void taskResponseFailureExceptionThrownDueToServiceTimeoutOrExecution() throws Exception
    {
        final AddEsxiHostVSphereLicenseRequest mockRequestMessage = mock(AddEsxiHostVSphereLicenseRequest.class);
        final String errorMessage = "Service timeout";
        when(requestModel.getRequestMessage()).thenReturn(mockRequestMessage);
        when(requestTransformer.buildApplyEsxiLicenseRequest(delegateExecution)).thenReturn(requestModel);
        doThrow(new TaskResponseFailureException(1, errorMessage)).when(nodeService).requestInstallEsxiLicense(mockRequestMessage);

        final ApplyEsxiLicense spy = spy(delegate);
        try
        {
            spy.delegateExecute(delegateExecution);
        }
        catch (BpmnError error)
        {
            assertThat(error.getMessage(), containsString("Exception Code: " + 1 + "::" + errorMessage));
            assertTrue(error.getErrorCode().equals(APPLY_ESXI_LICENSE_FAILED));
        }

        verify(spy).updateDelegateStatus(errorMessage);
    }

    @Test
    public void applyEsxiHostLicenseSuccessUpdatesTheDelegateStatus() throws Exception
    {
        final AddEsxiHostVSphereLicenseRequest mockRequestMessage = mock(AddEsxiHostVSphereLicenseRequest.class);

        when(requestModel.getRequestMessage()).thenReturn(mockRequestMessage);
        when(requestModel.getServiceTag()).thenReturn(serviceTag);
        when(requestTransformer.buildApplyEsxiLicenseRequest(delegateExecution)).thenReturn(requestModel);
        doNothing().when(nodeService).requestInstallEsxiLicense(mockRequestMessage);

        final ApplyEsxiLicense spy = spy(delegate);
        spy.delegateExecute(delegateExecution);

        verify(spy).updateDelegateStatus("Apply Esxi License on Node " + serviceTag + " was successful.");
    }
}
