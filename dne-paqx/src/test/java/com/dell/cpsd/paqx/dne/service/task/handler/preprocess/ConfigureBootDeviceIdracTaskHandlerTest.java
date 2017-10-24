/**
 * <p>
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries. All Rights Reserved. Dell EMC Confidential/Proprietary Information
 * </p>
 */

package com.dell.cpsd.paqx.dne.service.task.handler.preprocess;

import com.dell.cpsd.paqx.dne.domain.Job;
import com.dell.cpsd.paqx.dne.domain.WorkflowTask;
import com.dell.cpsd.paqx.dne.service.NodeService;
import com.dell.cpsd.paqx.dne.service.model.BootDeviceIdracStatus;
import com.dell.cpsd.paqx.dne.service.model.ConfigureBootDeviceIdracTaskResponse;
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
 * The tests for ConfigureBootDeviceIdracTaskHandler.
 * <p>
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries. All Rights Reserved. Dell EMC Confidential/Proprietary Information
 * </p>
 *
 * @since 1.0
 */
@RunWith(MockitoJUnitRunner.class)
public class ConfigureBootDeviceIdracTaskHandlerTest
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
    private ConfigureBootDeviceIdracTaskResponse configureBootDeviceIdracTaskResponse;

    @Mock
    private BootDeviceIdracStatus bootDeviceIdracStatus;

    private ConfigureBootDeviceIdracTaskHandler handler;
    private final String symphonyUuid   = "symphonyuuid-1";
    private final String idracIpAddress = "1.1.1.1";

    @Before
    public void setUp()
    {
        this.handler = spy(new ConfigureBootDeviceIdracTaskHandler(this.nodeService));
    }

    @Test
    public void testExecuteTask_should_successfully_request_configure_idrac_boot_device()
            throws ServiceTimeoutException, ServiceExecutionException
    {
        doReturn(this.configureBootDeviceIdracTaskResponse).when(this.handler).initializeResponse(this.job);
        doReturn(this.nodeExpansionRequest).when(this.job).getInputParams();
        doReturn(this.idracIpAddress).when(this.nodeExpansionRequest).getIdracIpAddress();
        doReturn(this.symphonyUuid).when(this.nodeExpansionRequest).getSymphonyUuid();
        doReturn(this.bootDeviceIdracStatus).when(this.nodeService).bootDeviceIdracStatus(any());
        doReturn("SUCCESS").when(this.bootDeviceIdracStatus).getStatus();

        final boolean result = this.handler.executeTask(this.job);

        assertTrue(result);
        verify(this.configureBootDeviceIdracTaskResponse).setWorkFlowTaskStatus(Status.SUCCEEDED);
        verify(this.configureBootDeviceIdracTaskResponse, never()).addError(anyString());
    }

    @Test
    public void testExecuteTask_should_fail_the_work_flow_if_the_configure_idrac_boot_device_request_failed()
            throws ServiceTimeoutException, ServiceExecutionException
    {
        doReturn(this.configureBootDeviceIdracTaskResponse).when(this.handler).initializeResponse(this.job);
        doReturn(this.nodeExpansionRequest).when(this.job).getInputParams();
        doReturn(this.idracIpAddress).when(this.nodeExpansionRequest).getIdracIpAddress();
        doReturn(this.symphonyUuid).when(this.nodeExpansionRequest).getSymphonyUuid();
        doReturn(this.bootDeviceIdracStatus).when(this.nodeService).bootDeviceIdracStatus(any());
        doReturn("FAILED").when(this.bootDeviceIdracStatus).getStatus();

        final boolean result = this.handler.executeTask(this.job);

        assertFalse(result);
        verify(this.configureBootDeviceIdracTaskResponse).setWorkFlowTaskStatus(Status.FAILED);
        verify(this.configureBootDeviceIdracTaskResponse).addError(anyString());
    }

    @Test
    public void testExecuteTask_should_fail_the_work_flow_there_was_an_exception() throws ServiceTimeoutException, ServiceExecutionException
    {
        doReturn(this.configureBootDeviceIdracTaskResponse).when(this.handler).initializeResponse(this.job);
        doReturn(this.nodeExpansionRequest).when(this.job).getInputParams();
        doReturn(this.idracIpAddress).when(this.nodeExpansionRequest).getIdracIpAddress();
        doReturn(this.symphonyUuid).when(this.nodeExpansionRequest).getSymphonyUuid();
        doThrow(new IllegalStateException("some-error")).when(this.nodeService).bootDeviceIdracStatus(any());

        final boolean result = this.handler.executeTask(this.job);

        assertFalse(result);
        verify(this.configureBootDeviceIdracTaskResponse).setWorkFlowTaskStatus(Status.FAILED);
        verify(this.configureBootDeviceIdracTaskResponse).addError(anyString());
    }

    @Test
    public void testInitializeResponse_should_create_the_task_response_object() throws ServiceTimeoutException, ServiceExecutionException
    {
        doReturn(this.task).when(this.job).getCurrentTask();
        doReturn("configureBootDeviceIdracTask").when(this.task).getTaskName();
        doReturn("configureBootDeviceIdracStep").when(this.job).getStep();

        final ConfigureBootDeviceIdracTaskResponse result = this.handler.initializeResponse(this.job);

        assertNotNull(result);
    }
}
