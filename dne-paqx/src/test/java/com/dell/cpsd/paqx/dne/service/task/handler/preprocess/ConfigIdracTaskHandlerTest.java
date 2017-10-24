/**
 * <p>
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries. All Rights Reserved. Dell EMC Confidential/Proprietary Information
 * </p>
 */

package com.dell.cpsd.paqx.dne.service.task.handler.preprocess;

import com.dell.cpsd.paqx.dne.domain.Job;
import com.dell.cpsd.paqx.dne.domain.WorkflowTask;
import com.dell.cpsd.paqx.dne.service.NodeService;
import com.dell.cpsd.paqx.dne.service.model.IdracInfo;
import com.dell.cpsd.paqx.dne.service.model.IdracNetworkSettingsTaskResponse;
import com.dell.cpsd.paqx.dne.service.model.NodeExpansionRequest;
import com.dell.cpsd.paqx.dne.service.model.Status;
import com.dell.cpsd.service.common.client.exception.ServiceExecutionException;
import com.dell.cpsd.service.common.client.exception.ServiceTimeoutException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

/**
 * The tests for ConfigIdracTaskHandler.
 * <p>
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries. All Rights Reserved. Dell EMC Confidential/Proprietary Information
 * </p>
 *
 * @since 1.0
 */
@RunWith(MockitoJUnitRunner.class)
public class ConfigIdracTaskHandlerTest
{
    @Mock
    private WorkflowTask task;

    @Mock
    private NodeService nodeService;

    @Mock
    private Job job;

    @Mock
    private NodeExpansionRequest nodeExpansionRequest;

    @Mock
    private IdracNetworkSettingsTaskResponse idracNetworkSettingsTaskResponse;

    @Mock
    private IdracInfo idracInfo;

    private ConfigIdracTaskHandler handler;
    private final String symphonyUuid          = "symphonyuuid-1";
    private final String idracIpAddress        = "1.1.1.1";
    private final String idracGatewayIpAddress = "2.2.2.2";
    private final String idracSubnetMask       = "3.3.3.3";

    @Before
    public void setUp()
    {
        this.handler = spy(new ConfigIdracTaskHandler(this.nodeService));
    }

    @Test
    public void testExecuteTask_should_successfully_configure_the_idrac() throws ServiceTimeoutException, ServiceExecutionException
    {
        doReturn(this.idracNetworkSettingsTaskResponse).when(this.handler).initializeResponse(this.job);
        doReturn(this.nodeExpansionRequest).when(this.job).getInputParams();
        doReturn(this.idracIpAddress).when(this.nodeExpansionRequest).getIdracIpAddress();
        doReturn(this.idracGatewayIpAddress).when(this.nodeExpansionRequest).getIdracGatewayIpAddress();
        doReturn(this.idracSubnetMask).when(this.nodeExpansionRequest).getIdracSubnetMask();
        doReturn(this.symphonyUuid).when(this.nodeExpansionRequest).getSymphonyUuid();
        doReturn(this.idracInfo).when(this.nodeService).idracNetworkSettings(any());
        doReturn(this.idracIpAddress).when(this.idracInfo).getIdracIpAddress();
        doReturn(this.idracGatewayIpAddress).when(this.idracInfo).getIdracGatewayIpAddress();
        doReturn(this.idracSubnetMask).when(this.idracInfo).getIdracSubnetMask();
        doReturn("SUCCESS").when(this.idracInfo).getMessage();

        final boolean result = this.handler.executeTask(this.job);

        assertTrue(result);
        verify(this.idracNetworkSettingsTaskResponse).setWorkFlowTaskStatus(Status.SUCCEEDED);
        verify(this.idracNetworkSettingsTaskResponse, never()).addError(anyString());
    }

    @Test
    public void testExecuteTask_should_fail_the_work_flow_if_the_configure_idrac_request_failed()
            throws ServiceTimeoutException, ServiceExecutionException
    {
        doReturn(this.idracNetworkSettingsTaskResponse).when(this.handler).initializeResponse(this.job);
        doReturn(this.nodeExpansionRequest).when(this.job).getInputParams();
        doReturn(this.idracIpAddress).when(this.nodeExpansionRequest).getIdracIpAddress();
        doReturn(this.idracGatewayIpAddress).when(this.nodeExpansionRequest).getIdracGatewayIpAddress();
        doReturn(this.idracSubnetMask).when(this.nodeExpansionRequest).getIdracSubnetMask();
        doReturn(this.symphonyUuid).when(this.nodeExpansionRequest).getSymphonyUuid();
        doReturn(this.idracInfo).when(this.nodeService).idracNetworkSettings(any());
        doReturn("FAILED").when(this.idracInfo).getMessage();

        final boolean result = this.handler.executeTask(this.job);

        assertFalse(result);
        verify(this.idracNetworkSettingsTaskResponse).setWorkFlowTaskStatus(Status.FAILED);
        verify(this.idracNetworkSettingsTaskResponse).addError(anyString());
    }

    @Test
    public void testExecuteTask_should_fail_the_work_flow_there_was_an_exception() throws ServiceTimeoutException, ServiceExecutionException
    {
        doReturn(this.idracNetworkSettingsTaskResponse).when(this.handler).initializeResponse(this.job);
        doReturn(this.nodeExpansionRequest).when(this.job).getInputParams();
        doReturn(this.idracIpAddress).when(this.nodeExpansionRequest).getIdracIpAddress();
        doReturn(this.idracGatewayIpAddress).when(this.nodeExpansionRequest).getIdracGatewayIpAddress();
        doReturn(this.idracSubnetMask).when(this.nodeExpansionRequest).getIdracSubnetMask();
        doReturn(this.symphonyUuid).when(this.nodeExpansionRequest).getSymphonyUuid();
        doThrow(new IllegalStateException("some-error")).when(this.nodeService).idracNetworkSettings(any());

        final boolean result = this.handler.executeTask(this.job);

        assertFalse(result);
        verify(this.idracNetworkSettingsTaskResponse).setWorkFlowTaskStatus(Status.FAILED);
        verify(this.idracNetworkSettingsTaskResponse).addError(anyString());
    }

    @Test
    public void testInitializeResponse_should_create_the_task_response_object() throws ServiceTimeoutException, ServiceExecutionException
    {
        doReturn(this.task).when(this.job).getCurrentTask();
        doReturn("configIdracTask").when(this.task).getTaskName();
        doReturn("configIdracStep").when(this.job).getStep();

        final IdracNetworkSettingsTaskResponse result = this.handler.initializeResponse(this.job);

        assertNotNull(result);
    }
}
