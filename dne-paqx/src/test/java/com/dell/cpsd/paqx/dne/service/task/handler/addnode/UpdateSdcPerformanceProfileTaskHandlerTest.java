/**
 * <p>
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries. All Rights Reserved.
 * Dell EMC Confidential/Proprietary Information
 * </p>
 */

package com.dell.cpsd.paqx.dne.service.task.handler.addnode;

import com.dell.cpsd.paqx.dne.domain.Job;
import com.dell.cpsd.paqx.dne.repository.DataServiceRepository;
import com.dell.cpsd.paqx.dne.service.NodeService;
import com.dell.cpsd.paqx.dne.service.model.ComponentEndpointIds;
import com.dell.cpsd.paqx.dne.service.model.ConfigureScaleIoVibTaskResponse;
import com.dell.cpsd.paqx.dne.service.model.NodeExpansionRequest;
import com.dell.cpsd.paqx.dne.service.model.Status;
import com.dell.cpsd.paqx.dne.service.model.TaskResponse;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Map;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.any;

/**
 * Update SDC Performance Profile Task Handler Test
 * <p>
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries. All Rights Reserved.
 * Dell EMC Confidential/Proprietary Information
 * </p>
 *
 * @version 1.0
 * @since 1.0
 */
@RunWith(MockitoJUnitRunner.class)
public class UpdateSdcPerformanceProfileTaskHandlerTest
{
    @Mock
    private Job job;

    @Mock
    private TaskResponse taskResponse;

    @Mock
    private NodeService nodeService;

    @Mock
    private DataServiceRepository repository;

    @Mock
    private ComponentEndpointIds componentEndpointIds;

    @Mock
    private NodeExpansionRequest nodeExpansionRequest;

    @Mock
    private Map<String, TaskResponse> taskResponseMap;

    @Mock
    private ConfigureScaleIoVibTaskResponse configureScaleIoVibTaskResponse;

    private UpdateSdcPerformanceProfileTaskHandler handler;
    private String scaleIoSdcIpAddress = "1.2.3.4";
    private String sdcGUID             = "sdc_guid_1";

    @Before
    public void setUp()
    {
        this.handler = spy(new UpdateSdcPerformanceProfileTaskHandler(this.nodeService, this.repository));
    }

    @Test
    public void executeTask_should_successfully_request_the_sdc_performance_profile_update() throws Exception
    {
        doReturn(this.taskResponse).when(this.handler).initializeResponse(any());
        when(this.repository.getComponentEndpointIds(anyString())).thenReturn(this.componentEndpointIds);
        when(this.job.getInputParams()).thenReturn(this.nodeExpansionRequest);
        when(this.nodeExpansionRequest.getEsxiManagementIpAddress()).thenReturn(this.scaleIoSdcIpAddress);
        when(this.job.getTaskResponseMap()).thenReturn(this.taskResponseMap);
        when(this.taskResponseMap.get(anyString())).thenReturn(this.configureScaleIoVibTaskResponse);
        when(this.configureScaleIoVibTaskResponse.getIoctlIniGuidStr()).thenReturn(this.sdcGUID);
        when(this.nodeService.requestUpdateSdcPerformanceProfile(any())).thenReturn(true);

        final boolean result = this.handler.executeTask(this.job);

        assertThat(result, is(true));
        verify(this.taskResponse).setWorkFlowTaskStatus(Status.SUCCEEDED);
        verify(this.taskResponse, never()).addError(anyString());
    }

    @Test
    public void executeTask_should_fail_the_workflow_if_there_are_no_component_ids() throws Exception
    {
        doReturn(this.taskResponse).when(this.handler).initializeResponse(any());
        when(this.repository.getComponentEndpointIds(anyString())).thenReturn(null);

        final boolean result = this.handler.executeTask(this.job);

        assertThat(result, is(false));
        verify(this.taskResponse).setWorkFlowTaskStatus(Status.FAILED);
        verify(this.taskResponse).addError(anyString());
    }

    @Test
    public void executeTask_should_fail_the_workflow_if_the_node_expansion_request_is_null() throws Exception
    {
        doReturn(this.taskResponse).when(this.handler).initializeResponse(any());
        when(this.repository.getComponentEndpointIds(anyString())).thenReturn(this.componentEndpointIds);
        when(this.job.getInputParams()).thenReturn(null);

        final boolean result = this.handler.executeTask(this.job);

        assertThat(result, is(false));
        verify(this.taskResponse).setWorkFlowTaskStatus(Status.FAILED);
        verify(this.taskResponse).addError(anyString());
    }

    @Test
    public void executeTask_should_fail_the_workflow_if_the_scaleio_sdc_ip_address_is_empty() throws Exception
    {
        doReturn(this.taskResponse).when(this.handler).initializeResponse(any());
        when(this.repository.getComponentEndpointIds(anyString())).thenReturn(this.componentEndpointIds);
        when(this.job.getInputParams()).thenReturn(this.nodeExpansionRequest);
        when(this.nodeExpansionRequest.getEsxiManagementIpAddress()).thenReturn(null);

        final boolean result = this.handler.executeTask(this.job);

        assertThat(result, is(false));
        verify(this.taskResponse).setWorkFlowTaskStatus(Status.FAILED);
        verify(this.taskResponse).addError(anyString());
    }

    @Test
    public void executeTask_should_fail_the_workflow_if_the_configure_scaleIo_vib_task_response_is_null() throws Exception
    {
        doReturn(this.taskResponse).when(this.handler).initializeResponse(any());
        when(this.repository.getComponentEndpointIds(anyString())).thenReturn(this.componentEndpointIds);
        when(this.job.getInputParams()).thenReturn(this.nodeExpansionRequest);
        when(this.nodeExpansionRequest.getEsxiManagementIpAddress()).thenReturn(this.scaleIoSdcIpAddress);
        when(this.job.getTaskResponseMap()).thenReturn(this.taskResponseMap);
        when(this.taskResponseMap.get(anyString())).thenReturn(null);

        final boolean result = this.handler.executeTask(this.job);

        assertThat(result, is(false));
        verify(this.taskResponse).setWorkFlowTaskStatus(Status.FAILED);
        verify(this.taskResponse).addError(anyString());
    }

    @Test
    public void executeTask_should_fail_the_workflow_if_the_sdc_guid_is_empty() throws Exception
    {
        doReturn(this.taskResponse).when(this.handler).initializeResponse(any());
        when(this.repository.getComponentEndpointIds(anyString())).thenReturn(this.componentEndpointIds);
        when(this.job.getInputParams()).thenReturn(this.nodeExpansionRequest);
        when(this.nodeExpansionRequest.getEsxiManagementIpAddress()).thenReturn(this.scaleIoSdcIpAddress);
        when(this.job.getTaskResponseMap()).thenReturn(this.taskResponseMap);
        when(this.taskResponseMap.get(anyString())).thenReturn(this.configureScaleIoVibTaskResponse);
        when(this.configureScaleIoVibTaskResponse.getIoctlIniGuidStr()).thenReturn(null);

        final boolean result = this.handler.executeTask(this.job);

        assertThat(result, is(false));
        verify(this.taskResponse).setWorkFlowTaskStatus(Status.FAILED);
        verify(this.taskResponse).addError(anyString());
    }

    @Test
    public void executeTask_should_fail_the_workflow_if_the_sdc_performance_profile_update_request_fails() throws Exception
    {
        doReturn(this.taskResponse).when(this.handler).initializeResponse(any());
        when(this.repository.getComponentEndpointIds(anyString())).thenReturn(this.componentEndpointIds);
        when(this.job.getInputParams()).thenReturn(this.nodeExpansionRequest);
        when(this.nodeExpansionRequest.getEsxiManagementIpAddress()).thenReturn(this.scaleIoSdcIpAddress);
        when(this.job.getTaskResponseMap()).thenReturn(this.taskResponseMap);
        when(this.taskResponseMap.get(anyString())).thenReturn(this.configureScaleIoVibTaskResponse);
        when(this.configureScaleIoVibTaskResponse.getIoctlIniGuidStr()).thenReturn(this.sdcGUID);
        when(this.nodeService.requestUpdateSdcPerformanceProfile(any())).thenReturn(false);

        final boolean result = this.handler.executeTask(this.job);

        assertThat(result, is(false));
        verify(this.taskResponse).setWorkFlowTaskStatus(Status.FAILED);
        verify(this.taskResponse).addError(anyString());
    }
}