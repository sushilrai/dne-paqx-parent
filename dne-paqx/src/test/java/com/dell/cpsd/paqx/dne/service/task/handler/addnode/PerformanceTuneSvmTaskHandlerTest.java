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
import com.dell.cpsd.paqx.dne.service.model.NodeExpansionRequest;
import com.dell.cpsd.paqx.dne.service.model.Status;
import com.dell.cpsd.paqx.dne.service.model.TaskResponse;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

/**
 * The tests for the PerformanceTuneSvmTaskHandler class.
 * <p>
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries. All Rights Reserved. Dell EMC Confidential/Proprietary Information
 * </p>
 *
 * @since 1.0
 */
@RunWith(MockitoJUnitRunner.class)
public class PerformanceTuneSvmTaskHandlerTest
{
    @Mock
    private Job job;

    @Mock
    private NodeService nodeService;

    @Mock
    private DataServiceRepository repository;

    @Mock
    private TaskResponse taskResponse;

    @Mock
    private ComponentEndpointIds componentEndpointIds;

    @Mock
    private NodeExpansionRequest nodeExpansionRequest;

    private PerformanceTuneSvmTaskHandler handler;
    private String scaleIoSvmManagementIpAddress = "1.2.3.4";

    @Before
    public void setUp()
    {
        this.handler = spy(new PerformanceTuneSvmTaskHandler(this.nodeService, this.repository));
    }

    @Test
    public void executeTask_should_successfully_request_the_svm_performance_tuning() throws Exception
    {
        doReturn(this.taskResponse).when(this.handler).initializeResponse(this.job);
        doReturn(this.componentEndpointIds).when(this.repository).getComponentEndpointIds(anyString(), anyString(), anyString());
        doReturn(this.nodeExpansionRequest).when(this.job).getInputParams();
        doReturn(this.scaleIoSvmManagementIpAddress).when(this.nodeExpansionRequest).getScaleIoSvmManagementIpAddress();
        doReturn(true).when(this.nodeService).requestRemoteCommandExecution(any());

        final boolean result = this.handler.executeTask(this.job);

        assertThat(result, is(true));
        verify(this.taskResponse).setWorkFlowTaskStatus(Status.SUCCEEDED);
        verify(this.taskResponse, never()).addError(anyString());
    }

    @Test
    public void executeTask_should_fail_the_work_flow_if_the_component_ids_are_null() throws Exception
    {
        doReturn(this.taskResponse).when(this.handler).initializeResponse(this.job);
        doReturn(null).when(this.repository).getComponentEndpointIds(anyString(), anyString(), anyString());

        final boolean result = this.handler.executeTask(this.job);

        assertThat(result, is(false));
        verify(this.taskResponse).setWorkFlowTaskStatus(Status.FAILED);
        verify(this.taskResponse).addError(anyString());
    }

    @Test
    public void executeTask_should_fail_the_work_flow_if_the_job_input_params_is_null() throws Exception
    {
        doReturn(this.taskResponse).when(this.handler).initializeResponse(this.job);
        doReturn(this.componentEndpointIds).when(this.repository).getComponentEndpointIds(anyString(), anyString(), anyString());
        doReturn(null).when(this.job).getInputParams();

        final boolean result = this.handler.executeTask(this.job);

        assertThat(result, is(false));
        verify(this.taskResponse).setWorkFlowTaskStatus(Status.FAILED);
        verify(this.taskResponse).addError(anyString());
    }

    @Test
    public void executeTask_should_fail_the_work_flow_if_the_scaleio_svm_management_ip_address_is_null() throws Exception
    {
        doReturn(this.taskResponse).when(this.handler).initializeResponse(this.job);
        doReturn(this.componentEndpointIds).when(this.repository).getComponentEndpointIds(anyString(), anyString(), anyString());
        doReturn(this.nodeExpansionRequest).when(this.job).getInputParams();
        doReturn(null).when(this.nodeExpansionRequest).getScaleIoSvmManagementIpAddress();

        final boolean result = this.handler.executeTask(this.job);

        assertThat(result, is(false));
        verify(this.taskResponse).setWorkFlowTaskStatus(Status.FAILED);
        verify(this.taskResponse).addError(anyString());
    }

    @Test
    public void executeTask_should_fail_the_work_flow_if_the_svm_performance_tuning_request_fails() throws Exception
    {
        doReturn(this.taskResponse).when(this.handler).initializeResponse(this.job);
        doReturn(this.componentEndpointIds).when(this.repository).getComponentEndpointIds(anyString(), anyString(), anyString());
        doReturn(this.nodeExpansionRequest).when(this.job).getInputParams();
        doReturn(this.scaleIoSvmManagementIpAddress).when(this.nodeExpansionRequest).getScaleIoSvmManagementIpAddress();
        doReturn(false).when(this.nodeService).requestRemoteCommandExecution(any());

        final boolean result = this.handler.executeTask(this.job);

        assertThat(result, is(false));
        verify(this.taskResponse).setWorkFlowTaskStatus(Status.FAILED);
        verify(this.taskResponse).addError(anyString());
    }
}
