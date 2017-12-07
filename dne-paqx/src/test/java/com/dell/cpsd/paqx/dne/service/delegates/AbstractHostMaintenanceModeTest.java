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
import com.dell.cpsd.paqx.dne.transformers.HostMaintenanceRequestTransformer;
import com.dell.cpsd.virtualization.capabilities.api.HostMaintenanceModeRequestMessage;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static com.dell.cpsd.paqx.dne.service.delegates.utils.DelegateConstants.ESXI_HOST_MAINTENANCE_MODE_FAILED;
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

/**
 * Abstract host maintenance mode test class
 * <p>
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries. All Rights Reserved.
 * Dell EMC Confidential/Proprietary Information
 * </p>
 *
 * @version 1.0
 * @since 1.0
 */
@RunWith(MockitoJUnitRunner.class)
public class AbstractHostMaintenanceModeTest
{
    @Mock
    private NodeService nodeService;

    @Mock
    private HostMaintenanceRequestTransformer hostMaintenanceRequestTransformer;

    @Mock
    private DelegateExecution delegateExecution;

    @Mock
    private DelegateRequestModel<HostMaintenanceModeRequestMessage> requestModel;

    private AbstractHostMaintenanceMode abstractHostMaintenanceModeEnable;
    private AbstractHostMaintenanceMode abstractHostMaintenanceModeExit;
    private final String serviceTag = "service-tag";

    @Before
    public void setup() throws Exception
    {
        abstractHostMaintenanceModeEnable = new EnterHostMaintenanceMode(nodeService, hostMaintenanceRequestTransformer);
        abstractHostMaintenanceModeExit = new ExitHostMaintenanceMode(nodeService, hostMaintenanceRequestTransformer);

        NodeDetail nodeDetail = new NodeDetail("1", serviceTag);
        doReturn(nodeDetail).when(delegateExecution).getVariable(NODE_DETAIL);
    }

    @Test
    public void unknownExceptionThrownResultsInBpmnErrorForEnterHostMaintenance() throws Exception
    {
        final String errorMessage = "Illegal state exception";
        when(hostMaintenanceRequestTransformer.buildHostMaintenanceRequest(delegateExecution, true))
                .thenThrow(new IllegalStateException(errorMessage));

        final AbstractHostMaintenanceMode abstractHostMaintenanceModeSpy = spy(abstractHostMaintenanceModeEnable);
        try
        {
            abstractHostMaintenanceModeSpy.delegateExecute(delegateExecution);
        }
        catch (BpmnError error)
        {
            assertThat(error.getMessage(), containsString("An unexpected exception occurred"));
            assertThat(error.getMessage(), containsString(errorMessage));
            assertThat(error.getMessage(), containsString("Enter Host Maintenance Mode"));
            assertTrue(error.getErrorCode().equals(ESXI_HOST_MAINTENANCE_MODE_FAILED));
        }

        verify(abstractHostMaintenanceModeSpy).updateDelegateStatus(
                "Attempting to Enter Host Maintenance Mode on Node service-tag.");
    }

    @Test
    public void unknownExceptionThrownResultsInBpmnErrorForExitHostMaintenance() throws Exception
    {
        final String errorMessage = "Illegal state exception";
        when(hostMaintenanceRequestTransformer.buildHostMaintenanceRequest(delegateExecution, false))
                .thenThrow(new IllegalStateException(errorMessage));

        final AbstractHostMaintenanceMode abstractHostMaintenanceModeSpy = spy(abstractHostMaintenanceModeExit);
        try
        {
            abstractHostMaintenanceModeSpy.delegateExecute(delegateExecution);
        }
        catch (BpmnError error)
        {
            assertThat(error.getMessage(), containsString("An unexpected exception occurred"));
            assertThat(error.getMessage(), containsString(errorMessage));
            assertThat(error.getMessage(), containsString("Exit Host Maintenance Mode"));
            assertTrue(error.getErrorCode().equals(ESXI_HOST_MAINTENANCE_MODE_FAILED));
        }

        verify(abstractHostMaintenanceModeSpy).updateDelegateStatus(
                "Attempting to Exit Host Maintenance Mode on Node service-tag.");
    }

    @Test
    public void taskResponseFailureExceptionThrownDueToServiceTimeoutOrExecutionForEnterHostMaintenance() throws Exception
    {
        final HostMaintenanceModeRequestMessage mockRequestMessage = mock(HostMaintenanceModeRequestMessage.class);
        final String errorMessage = "Service timeout";
        when(requestModel.getRequestMessage()).thenReturn(mockRequestMessage);
        when(hostMaintenanceRequestTransformer.buildHostMaintenanceRequest(delegateExecution, true)).thenReturn(requestModel);
        doThrow(new TaskResponseFailureException(1, errorMessage)).when(nodeService).requestHostMaintenanceMode(mockRequestMessage);

        final AbstractHostMaintenanceMode abstractHostMaintenanceModeSpy = spy(abstractHostMaintenanceModeEnable);
        try
        {
            abstractHostMaintenanceModeSpy.delegateExecute(delegateExecution);
        }
        catch (BpmnError error)
        {
            assertThat(error.getMessage(), containsString("An unexpected exception occurred attempting to request Enter Host Maintenance Mode on Node service-tag. Reason: Service timeout"));
            assertTrue(error.getErrorCode().equals(ESXI_HOST_MAINTENANCE_MODE_FAILED));
        }

        verify(abstractHostMaintenanceModeSpy).updateDelegateStatus( "Attempting to Enter Host Maintenance Mode on Node service-tag.");
    }

    @Test
    public void taskResponseFailureExceptionThrownDueToServiceTimeoutOrExecutionForExitHostMaintenance() throws Exception
    {
        final HostMaintenanceModeRequestMessage mockRequestMessage = mock(HostMaintenanceModeRequestMessage.class);
        final String errorMessage = "Service timeout";
        when(requestModel.getRequestMessage()).thenReturn(mockRequestMessage);
        when(hostMaintenanceRequestTransformer.buildHostMaintenanceRequest(delegateExecution, false)).thenReturn(requestModel);
        doThrow(new TaskResponseFailureException(1, errorMessage)).when(nodeService).requestHostMaintenanceMode(mockRequestMessage);

        final AbstractHostMaintenanceMode abstractHostMaintenanceModeSpy = spy(abstractHostMaintenanceModeExit);
        try
        {
            abstractHostMaintenanceModeSpy.delegateExecute(delegateExecution);
        }
        catch (BpmnError error)
        {
            assertThat(error.getMessage(), containsString("An unexpected exception occurred attempting to request Exit Host Maintenance Mode on Node service-tag. Reason: Service timeout"));
            assertTrue(error.getErrorCode().equals(ESXI_HOST_MAINTENANCE_MODE_FAILED));
        }

        verify(abstractHostMaintenanceModeSpy).updateDelegateStatus("Attempting to Exit Host Maintenance Mode on Node service-tag.");
    }

    @Test
    public void enterHostMaintenanceSuccessUpdatesTheDelegateStatus() throws Exception
    {
        final HostMaintenanceModeRequestMessage mockRequestMessage = mock(HostMaintenanceModeRequestMessage.class);
        when(requestModel.getRequestMessage()).thenReturn(mockRequestMessage);
        when(hostMaintenanceRequestTransformer.buildHostMaintenanceRequest(delegateExecution, true)).thenReturn(requestModel);
        doNothing().when(nodeService).requestHostMaintenanceMode(mockRequestMessage);

        final AbstractHostMaintenanceMode abstractHostMaintenanceModeSpy = spy(abstractHostMaintenanceModeEnable);

        abstractHostMaintenanceModeSpy.delegateExecute(delegateExecution);

        verify(abstractHostMaintenanceModeSpy)
                .updateDelegateStatus("Enter Host Maintenance Mode on Node " + serviceTag + " was successful.");
    }

    @Test
    public void exitHostMaintenanceSuccessUpdatesTheDelegateStatus() throws Exception
    {
        final HostMaintenanceModeRequestMessage mockRequestMessage = mock(HostMaintenanceModeRequestMessage.class);
        when(requestModel.getRequestMessage()).thenReturn(mockRequestMessage);
        when(hostMaintenanceRequestTransformer.buildHostMaintenanceRequest(delegateExecution, false)).thenReturn(requestModel);
        doNothing().when(nodeService).requestHostMaintenanceMode(mockRequestMessage);

        final AbstractHostMaintenanceMode abstractHostMaintenanceModeSpy = spy(abstractHostMaintenanceModeExit);

        abstractHostMaintenanceModeSpy.delegateExecute(delegateExecution);

        verify(abstractHostMaintenanceModeSpy)
                .updateDelegateStatus("Exit Host Maintenance Mode on Node " + serviceTag + " was successful.");
    }
}
