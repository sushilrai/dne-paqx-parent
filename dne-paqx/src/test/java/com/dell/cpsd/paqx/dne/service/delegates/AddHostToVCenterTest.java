/**
 * <p>
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries. All Rights Reserved. Dell EMC Confidential/Proprietary Information
 * </p>
 */

package com.dell.cpsd.paqx.dne.service.delegates;

import com.dell.cpsd.paqx.dne.exception.TaskResponseFailureException;
import com.dell.cpsd.paqx.dne.service.NodeService;
import com.dell.cpsd.paqx.dne.service.delegates.model.DelegateRequestModel;
import com.dell.cpsd.paqx.dne.transformers.AddHostToVCenterClusterRequestTransformer;
import com.dell.cpsd.virtualization.capabilities.api.ClusterOperationRequestMessage;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static com.dell.cpsd.paqx.dne.service.delegates.utils.DelegateConstants.ADD_HOST_TO_CLUSTER_FAILED;
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
public class AddHostToVCenterTest
{
    @Mock
    private NodeService nodeService;

    @Mock
    private AddHostToVCenterClusterRequestTransformer requestTransformer;

    @Mock
    private DelegateExecution delegateExecution;

    @Mock
    private DelegateRequestModel<ClusterOperationRequestMessage> requestModel;

    private AddHostToVCenter delegate;
    private final String serviceTag = "service-tag";

    @Before
    public void setup() throws Exception
    {
        delegate = new AddHostToVCenter(nodeService, requestTransformer);
    }

    @Test
    public void unknownExceptionThrownResultsInBpmnError() throws Exception
    {
        final String errorMessage = "Illegal state exception";
        when(requestTransformer.buildAddHostToVCenterRequest(delegateExecution)).thenThrow(new IllegalStateException(errorMessage));

        final AddHostToVCenter spy = spy(delegate);
        try
        {
            spy.delegateExecute(delegateExecution);
        }
        catch (BpmnError error)
        {
            assertThat(error.getMessage(), containsString("An unexpected exception occurred"));
            assertThat(error.getMessage(), containsString(errorMessage));
            assertThat(error.getMessage(), containsString("Add ESXi host to vcenter cluster"));
            assertTrue(error.getErrorCode().equals(ADD_HOST_TO_CLUSTER_FAILED));
        }

        verify(spy).updateDelegateStatus(
                "An unexpected exception occurred attempting to request Add ESXi host to vcenter cluster. Reason: " + errorMessage);
    }

    @Test
    public void taskResponseFailureExceptionThrownDueToServiceTimeoutOrExecution() throws Exception
    {
        final ClusterOperationRequestMessage mockRequestMessage = mock(ClusterOperationRequestMessage.class);
        final String errorMessage = "Service timeout";
        when(requestModel.getRequestMessage()).thenReturn(mockRequestMessage);
        when(requestTransformer.buildAddHostToVCenterRequest(delegateExecution)).thenReturn(requestModel);
        doThrow(new TaskResponseFailureException(1, errorMessage)).when(nodeService).requestAddHostToVCenter(mockRequestMessage);

        final AddHostToVCenter spy = spy(delegate);
        try
        {
            spy.delegateExecute(delegateExecution);
        }
        catch (BpmnError error)
        {
            assertThat(error.getMessage(), containsString("Exception Code: " + 1 + "::" + errorMessage));
            assertTrue(error.getErrorCode().equals(ADD_HOST_TO_CLUSTER_FAILED));
        }

        verify(spy).updateDelegateStatus(errorMessage);
    }

    @Test
    public void addHostToVCenterClusterSuccessUpdatesTheDelegateStatus() throws Exception
    {
        final ClusterOperationRequestMessage mockRequestMessage = mock(ClusterOperationRequestMessage.class);

        when(requestModel.getRequestMessage()).thenReturn(mockRequestMessage);
        when(requestModel.getServiceTag()).thenReturn(serviceTag);
        when(requestTransformer.buildAddHostToVCenterRequest(delegateExecution)).thenReturn(requestModel);
        doNothing().when(nodeService).requestAddHostToVCenter(mockRequestMessage);

        final AddHostToVCenter spy = spy(delegate);
        spy.delegateExecute(delegateExecution);

        verify(spy).updateDelegateStatus("Add ESXi host to vcenter cluster on Node " + serviceTag + " was successful.");
    }
}
