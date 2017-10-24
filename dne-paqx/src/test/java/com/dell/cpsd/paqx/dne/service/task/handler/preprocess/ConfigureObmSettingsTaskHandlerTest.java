/**
 * <p>
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries. All Rights Reserved. Dell EMC Confidential/Proprietary Information
 * </p>
 */

package com.dell.cpsd.paqx.dne.service.task.handler.preprocess;

import com.dell.cpsd.paqx.dne.domain.Job;
import com.dell.cpsd.paqx.dne.domain.WorkflowTask;
import com.dell.cpsd.paqx.dne.service.NodeService;
import com.dell.cpsd.paqx.dne.service.model.NodeExpansionRequest;
import com.dell.cpsd.paqx.dne.service.model.ObmSettingsResponse;
import com.dell.cpsd.paqx.dne.service.model.ObmSettingsTaskResponse;
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
 * The tests for ConfigureObmSettingsTaskHandler.
 * <p>
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries. All Rights Reserved. Dell EMC Confidential/Proprietary Information
 * </p>
 *
 * @since 1.0
 */

@RunWith(MockitoJUnitRunner.class)
public class ConfigureObmSettingsTaskHandlerTest
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
    private ObmSettingsTaskResponse obmSettingsTaskResponse;

    @Mock
    private ObmSettingsResponse obmSettingsResponse;

    private ConfigureObmSettingsTaskHandler handler;
    private final String[] obmServices    = {"onmService1", "obmService2"};
    private final String   symphonyUuid   = "symphonyuuid-1";
    private final String   idracIpAddress = "1.1.1.1";

    @Before
    public void setUp()
    {
        this.handler = spy(new ConfigureObmSettingsTaskHandler(this.nodeService, this.obmServices));
    }

    @Test
    public void testExecuteTask_should_successfully_configure_obm_settings() throws ServiceTimeoutException, ServiceExecutionException
    {
        doReturn(this.obmSettingsTaskResponse).when(this.handler).initializeResponse(this.job);
        doReturn(this.nodeExpansionRequest).when(this.job).getInputParams();
        doReturn(this.symphonyUuid).when(this.nodeExpansionRequest).getSymphonyUuid();
        doReturn(this.idracIpAddress).when(this.nodeExpansionRequest).getIdracIpAddress();
        doReturn(this.obmSettingsResponse).when(this.nodeService).obmSettingsResponse(any());
        doReturn("SUCCESS").when(this.obmSettingsResponse).getStatus();

        final boolean result = this.handler.executeTask(this.job);

        assertTrue(result);
        verify(this.obmSettingsTaskResponse).setWorkFlowTaskStatus(Status.SUCCEEDED);
        verify(this.obmSettingsTaskResponse, never()).addError(anyString());
    }

    @Test
    public void testExecuteTask_should_fail_the_workflow_if_the_configure_obm_settings_request_fails()
            throws ServiceTimeoutException, ServiceExecutionException
    {
        doReturn(this.obmSettingsTaskResponse).when(this.handler).initializeResponse(this.job);
        doReturn(this.nodeExpansionRequest).when(this.job).getInputParams();
        doReturn(this.symphonyUuid).when(this.nodeExpansionRequest).getSymphonyUuid();
        doReturn(this.idracIpAddress).when(this.nodeExpansionRequest).getIdracIpAddress();
        doReturn(this.obmSettingsResponse).when(this.nodeService).obmSettingsResponse(any());
        doReturn("FAILED").when(this.obmSettingsResponse).getStatus();

        final boolean result = this.handler.executeTask(this.job);

        assertFalse(result);
        verify(this.obmSettingsTaskResponse).setWorkFlowTaskStatus(Status.FAILED);
        verify(this.obmSettingsTaskResponse).addError(anyString());
    }

    @Test
    public void testExecuteTask_should_fail_the_work_flow_there_was_an_exception() throws ServiceTimeoutException, ServiceExecutionException
    {
        doReturn(this.obmSettingsTaskResponse).when(this.handler).initializeResponse(this.job);
        doReturn(this.nodeExpansionRequest).when(this.job).getInputParams();
        doReturn(this.symphonyUuid).when(this.nodeExpansionRequest).getSymphonyUuid();
        doReturn(this.idracIpAddress).when(this.nodeExpansionRequest).getIdracIpAddress();
        doThrow(new IllegalStateException("some-error")).when(this.nodeService).obmSettingsResponse(any());

        final boolean result = this.handler.executeTask(this.job);

        assertFalse(result);
        verify(this.obmSettingsTaskResponse).setWorkFlowTaskStatus(Status.FAILED);
        verify(this.obmSettingsTaskResponse).addError(anyString());
    }

    @Test
    public void testInitializeResponse_should_create_the_task_response_object() throws ServiceTimeoutException, ServiceExecutionException
    {
        doReturn(this.task).when(this.job).getCurrentTask();
        doReturn("configureObmSettingsTask").when(this.task).getTaskName();
        doReturn("configureObmSettingsStep").when(this.job).getStep();

        final ObmSettingsTaskResponse result = this.handler.initializeResponse(this.job);

        assertNotNull(result);
    }
}
