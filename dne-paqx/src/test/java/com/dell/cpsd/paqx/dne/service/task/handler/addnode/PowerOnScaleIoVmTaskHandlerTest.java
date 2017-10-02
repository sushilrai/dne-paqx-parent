/**
 * <p>
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries. All Rights Reserved. Dell EMC Confidential/Proprietary Information
 * </p>
 */

package com.dell.cpsd.paqx.dne.service.task.handler.addnode;

import com.dell.cpsd.paqx.dne.domain.Job;
import com.dell.cpsd.paqx.dne.repository.DataServiceRepository;
import com.dell.cpsd.paqx.dne.service.NodeService;
import com.dell.cpsd.paqx.dne.service.model.ComponentEndpointIds;
import com.dell.cpsd.paqx.dne.service.model.DeployScaleIoVmTaskResponse;
import com.dell.cpsd.paqx.dne.service.model.InstallEsxiTaskResponse;
import com.dell.cpsd.paqx.dne.service.model.Status;
import com.dell.cpsd.paqx.dne.service.model.TaskResponse;
import com.dell.cpsd.virtualization.capabilities.api.VmPowerOperationsRequestMessage;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Map;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

/**
 * The tests for the NotifyNodeDiscoveryToUpdateStatusTaskHandler.
 * <p>
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries. All Rights Reserved. Dell EMC Confidential/Proprietary Information
 * </p>
 *
 * @since 1.0
 */
@RunWith(MockitoJUnitRunner.class)
public class PowerOnScaleIoVmTaskHandlerTest
{
    @Mock
    private NodeService nodeService;

    @Mock
    private DataServiceRepository dataServiceRepository;

    @Mock
    private Job job;

    @Mock
    private TaskResponse taskResponse;

    @Mock
    private InstallEsxiTaskResponse installEsxiTaskResponse;

    @Mock
    private DeployScaleIoVmTaskResponse deployScaleIoVmTaskResponse;

    @Mock
    private Map<String, TaskResponse> taskResponseMap;

    @Mock
    private ComponentEndpointIds componentEndpointIds;

    private PowerOnScaleIoVmTaskHandler handler;
    private String hostName      = "the-host-1";
    private String scaleIoVmName = "scaleio-vm-1";

    @Before
    public void setUp() throws Exception
    {
        this.handler = spy(new PowerOnScaleIoVmTaskHandler(this.nodeService, this.dataServiceRepository));
    }

    @Test
    public void executeTask_should_successfully_power_on_the_scaleio_vm() throws Exception
    {
        doReturn(this.taskResponse).when(this.handler).initializeResponse(any());
        doReturn(this.taskResponseMap).when(this.job).getTaskResponseMap();
        doReturn(this.installEsxiTaskResponse, this.deployScaleIoVmTaskResponse).when(this.taskResponseMap).get(anyString());
        doReturn(this.componentEndpointIds).when(this.dataServiceRepository).getVCenterComponentEndpointIdsByEndpointType(anyString());
        doReturn(this.hostName).when(this.installEsxiTaskResponse).getHostname();
        doReturn(this.scaleIoVmName).when(this.deployScaleIoVmTaskResponse).getNewVMName();
        doReturn(true).when(this.nodeService).requestVmPowerOperation(any());

        boolean result = this.handler.executeTask(this.job);

        assertThat(result, is(true));
        verify(this.taskResponse).setWorkFlowTaskStatus(Status.SUCCEEDED);
        verify(this.taskResponse, never()).addError(anyString());
        ArgumentCaptor<VmPowerOperationsRequestMessage> requestCaptor = ArgumentCaptor.forClass(VmPowerOperationsRequestMessage.class);
        verify(this.nodeService).requestVmPowerOperation(requestCaptor.capture());
        assertSame(this.hostName, requestCaptor.getValue().getVmPowerOperationRequest().getHostname());
        assertSame(this.scaleIoVmName, requestCaptor.getValue().getVmPowerOperationRequest().getVmName());
    }

    @Test
    public void executeTask_should_fail_the_workflow_when_the_esxi_install_task_response_is_null() throws Exception
    {
        doReturn(this.taskResponse).when(this.handler).initializeResponse(any());
        doReturn(this.taskResponseMap).when(this.job).getTaskResponseMap();
        doReturn(null).when(this.taskResponseMap).get(anyString());

        boolean result = this.handler.executeTask(this.job);

        assertThat(result, is(false));
        verify(this.taskResponse).setWorkFlowTaskStatus(Status.FAILED);
        verify(this.taskResponse).addError(anyString());
        verify(this.nodeService, never()).requestVmPowerOperation(any());
    }

    @Test
    public void executeTask_should_fail_the_workflow_when_the_deploy_scaleio_vm_task_response_is_null() throws Exception
    {
        doReturn(this.taskResponse).when(this.handler).initializeResponse(any());
        doReturn(this.taskResponseMap).when(this.job).getTaskResponseMap();
        doReturn(this.installEsxiTaskResponse, null).when(this.taskResponseMap).get(anyString());

        boolean result = this.handler.executeTask(this.job);

        assertThat(result, is(false));
        verify(this.taskResponse).setWorkFlowTaskStatus(Status.FAILED);
        verify(this.taskResponse).addError(anyString());
        verify(this.nodeService, never()).requestVmPowerOperation(any());
    }

    @Test
    public void executeTask_should_fail_the_workflow_when_the_component_endpoint_ids_is_null() throws Exception
    {
        doReturn(this.taskResponse).when(this.handler).initializeResponse(any());
        doReturn(this.taskResponseMap).when(this.job).getTaskResponseMap();
        doReturn(this.installEsxiTaskResponse, this.deployScaleIoVmTaskResponse).when(this.taskResponseMap).get(anyString());
        doReturn(null).when(this.dataServiceRepository).getVCenterComponentEndpointIdsByEndpointType(anyString());

        boolean result = this.handler.executeTask(this.job);

        assertThat(result, is(false));
        verify(this.taskResponse).setWorkFlowTaskStatus(Status.FAILED);
        verify(this.taskResponse).addError(anyString());
        verify(this.nodeService, never()).requestVmPowerOperation(any());
    }

    @Test
    public void executeTask_should_fail_the_workflow_when_the_vm_power_operation_request_fails() throws Exception
    {
        doReturn(this.taskResponse).when(this.handler).initializeResponse(any());
        doReturn(this.taskResponseMap).when(this.job).getTaskResponseMap();
        doReturn(this.installEsxiTaskResponse, this.deployScaleIoVmTaskResponse).when(this.taskResponseMap).get(anyString());
        doReturn(this.componentEndpointIds).when(this.dataServiceRepository).getVCenterComponentEndpointIdsByEndpointType(anyString());
        doReturn(this.hostName).when(this.installEsxiTaskResponse).getHostname();
        doReturn(this.scaleIoVmName).when(this.deployScaleIoVmTaskResponse).getNewVMName();
        doReturn(false).when(this.nodeService).requestVmPowerOperation(any());

        boolean result = this.handler.executeTask(this.job);

        assertThat(result, is(false));
        verify(this.taskResponse).setWorkFlowTaskStatus(Status.FAILED);
        verify(this.taskResponse).addError(anyString());
        verify(this.nodeService).requestVmPowerOperation(any());
    }
}