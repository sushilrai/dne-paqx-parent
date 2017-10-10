/**
 * <p>
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries. All Rights Reserved.
 * Dell EMC Confidential/Proprietary Information
 * </p>
 */

package com.dell.cpsd.paqx.dne.service.task.handler.addnode;

import com.dell.cpsd.paqx.dne.domain.Job;
import com.dell.cpsd.paqx.dne.domain.WorkflowTask;
import com.dell.cpsd.paqx.dne.repository.DataServiceRepository;
import com.dell.cpsd.paqx.dne.service.NodeService;
import com.dell.cpsd.paqx.dne.service.model.ComponentEndpointIds;
import com.dell.cpsd.paqx.dne.service.model.DeployScaleIoVmTaskResponse;
import com.dell.cpsd.paqx.dne.service.model.EnablePciPassThroughTaskResponse;
import com.dell.cpsd.paqx.dne.service.model.InstallEsxiTaskResponse;
import com.dell.cpsd.paqx.dne.service.model.Status;
import com.dell.cpsd.paqx.dne.service.model.TaskResponse;
import com.dell.cpsd.paqx.dne.service.model.UpdatePciPassThroughTaskResponse;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Map;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

/**
 * Update PCI PassThrough Task Handler Test
 * <p>
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries. All Rights Reserved.
 * Dell EMC Confidential/Proprietary Information
 * </p>
 *
 * @version 1.0
 * @since 1.0
 */
@RunWith(MockitoJUnitRunner.class)
public class UpdatePciPassThroughTaskHandlerTest
{
    @Mock
    private WorkflowTask task;

    @Mock
    private NodeService service;

    @Mock
    private DataServiceRepository repository;

    @Mock
    private Job job;

    @Mock
    private UpdatePciPassThroughTaskResponse response;

    @Mock
    private InstallEsxiTaskResponse installEsxiTaskResponse;

    @Mock
    private EnablePciPassThroughTaskResponse enablePciPassThroughTaskResponse;

    @Mock
    private DeployScaleIoVmTaskResponse deployScaleIoVmTaskResponse;

    @Mock
    private ComponentEndpointIds componentEndpointIds;

    @Mock
    private Map<String, TaskResponse> taskResponseMap;

    private UpdatePciPassThroughTaskHandler handler;

    private static final String hostname = "hostname_1.2.3.4";
    private static final String hostPciDeviceId = "0000:00:000:0";
    private static final String newVmName = "hello-vm-1";

    /**
     * The test setup.
     *
     * @since 1.0
     */
    @Before
    public void setUp() throws Exception
    {
        this.handler = spy(new UpdatePciPassThroughTaskHandler(this.service, this.repository));
    }

    @Test
    public void executeTask_successful_case() throws Exception
    {
        doReturn(this.response).when(this.handler).initializeResponse(this.job);

        doReturn(this.componentEndpointIds).when(this.repository).getVCenterComponentEndpointIdsByEndpointType(anyString());
        doReturn(this.taskResponseMap).when(this.job).getTaskResponseMap();
        doReturn(this.installEsxiTaskResponse).when(this.taskResponseMap).get("installEsxi");
        doReturn(this.hostname).when(this.installEsxiTaskResponse).getHostname();
        doReturn(this.enablePciPassThroughTaskResponse).when(this.taskResponseMap).get("enablePciPassthroughHost");
        doReturn(this.hostPciDeviceId).when(this.enablePciPassThroughTaskResponse).getHostPciDeviceId();
        doReturn(this.deployScaleIoVmTaskResponse).when(this.taskResponseMap).get("deploySVM");
        doReturn(this.newVmName).when(this.deployScaleIoVmTaskResponse).getNewVMName();
        doReturn(true).when(this.service).requestSetPciPassThrough(any());

        boolean result = this.handler.executeTask(this.job);

        assertThat(result, is(true));
        verify(this.response).setWorkFlowTaskStatus(Status.SUCCEEDED);
        verify(this.response, never()).addError(anyString());
    }

    @Test
    public void executeTask_no_vcenter_components() throws Exception
    {
        final ComponentEndpointIds nullComponentEndpointIds = null;

        doReturn(this.response).when(this.handler).initializeResponse(this.job);
        doReturn(nullComponentEndpointIds).when(this.repository).getVCenterComponentEndpointIdsByEndpointType(anyString());

        boolean result = this.handler.executeTask(this.job);

        assertThat(result, is(false));
        verify(this.response).setWorkFlowTaskStatus(Status.FAILED);
        verify(this.response).addError(anyString());
    }

    @Test
    public void executeTask_no_task_response() throws Exception
    {
        final InstallEsxiTaskResponse nullInstallEsxiTaskResponse = null;

        doReturn(this.response).when(this.handler).initializeResponse(this.job);
        doReturn(this.componentEndpointIds).when(this.repository).getVCenterComponentEndpointIdsByEndpointType(anyString());
        doReturn(this.taskResponseMap).when(this.job).getTaskResponseMap();
        doReturn(nullInstallEsxiTaskResponse).when(this.taskResponseMap).get(anyString());

        boolean result = this.handler.executeTask(this.job);

        assertThat(result, is(false));
        verify(this.response).setWorkFlowTaskStatus(Status.FAILED);
        verify(this.response).addError(anyString());
    }

    @Test
    public void executeTask_no_hostname() throws Exception
    {
        final String nullHostname = null;

        doReturn(this.response).when(this.handler).initializeResponse(this.job);
        doReturn(this.componentEndpointIds).when(this.repository).getVCenterComponentEndpointIdsByEndpointType(anyString());
        doReturn(this.taskResponseMap).when(this.job).getTaskResponseMap();
        doReturn(this.installEsxiTaskResponse).when(this.taskResponseMap).get(anyString());
        doReturn(nullHostname).when(this.installEsxiTaskResponse).getHostname();

        boolean result = this.handler.executeTask(this.job);

        assertThat(result, is(false));
        verify(this.response).setWorkFlowTaskStatus(Status.FAILED);
        verify(this.response).addError(anyString());
    }

    @Test
    public void executeTask_failed_updatePciPassThrough_request() throws Exception
    {
        doReturn(this.response).when(this.handler).initializeResponse(this.job);
        doReturn(this.componentEndpointIds).when(this.repository).getVCenterComponentEndpointIdsByEndpointType(anyString());
        doReturn(this.taskResponseMap).when(this.job).getTaskResponseMap();
        doReturn(this.installEsxiTaskResponse).when(this.taskResponseMap).get("installEsxi");
        doReturn(this.hostname).when(this.installEsxiTaskResponse).getHostname();
        doReturn(this.enablePciPassThroughTaskResponse).when(this.taskResponseMap).get("enablePciPassthroughHost");
        doReturn(this.hostPciDeviceId).when(this.enablePciPassThroughTaskResponse).getHostPciDeviceId();
        doReturn(this.deployScaleIoVmTaskResponse).when(this.taskResponseMap).get("deploySVM");
        doReturn(this.newVmName).when(this.deployScaleIoVmTaskResponse).getNewVMName();
        doReturn(false).when(this.service).requestSetPciPassThrough(any());

        boolean result = this.handler.executeTask(this.job);

        assertThat(result, is(false));
        verify(this.response).setWorkFlowTaskStatus(Status.FAILED);
        verify(this.response).addError(anyString());
    }

    @Test
    public void initializeResponse() throws Exception
    {
        doReturn(this.task).when(this.job).getCurrentTask();
        String taskName = "updatePciPassthroughTask";
        doReturn(taskName).when(this.task).getTaskName();
        String stepName = "updatePciPassthroughStep";
        doReturn(stepName).when(this.job).getStep();

        final UpdatePciPassThroughTaskResponse response = this.handler.initializeResponse(this.job);

        assertNotNull(response);
        assertEquals(taskName, response.getWorkFlowTaskName());
        assertEquals(Status.IN_PROGRESS, response.getWorkFlowTaskStatus());
    }
}
