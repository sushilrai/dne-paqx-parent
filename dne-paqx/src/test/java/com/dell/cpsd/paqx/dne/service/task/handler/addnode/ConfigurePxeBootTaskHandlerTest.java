/**
 * <p>
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries. All Rights Reserved. Dell EMC Confidential/Proprietary Information
 * </p>
 */
package com.dell.cpsd.paqx.dne.service.task.handler.addnode;

import com.dell.cpsd.paqx.dne.domain.Job;
import com.dell.cpsd.paqx.dne.domain.WorkflowTask;
import com.dell.cpsd.paqx.dne.service.NodeService;
import com.dell.cpsd.paqx.dne.service.model.BootDeviceIdracStatus;
import com.dell.cpsd.paqx.dne.service.model.ConfigureBootDeviceIdracResponse;
import com.dell.cpsd.paqx.dne.service.model.NodeExpansionRequest;
import com.dell.cpsd.paqx.dne.service.model.Status;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

/**
 * The tests for the ChangeIdracCredentialsTaskHandler class.
 * <p>
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries. All Rights Reserved. Dell EMC Confidential/Proprietary Information
 * </p>
 *
 * @since 1.0
 */
@SuppressWarnings("ALL")
@RunWith(MockitoJUnitRunner.class)
public class ConfigurePxeBootTaskHandlerTest
{
    @Mock
    private WorkflowTask task;

    @Mock
    private NodeService service;

    @Mock
    private Job job;

    @Mock
    private ConfigureBootDeviceIdracResponse response;

    @Mock
    private NodeExpansionRequest request;

    @Mock
    private BootDeviceIdracStatus bootDeviceIdracStatus;

    private ConfigurePxeBootTaskHandler handler;
    private String symphonyUuid = "symphonyuuid";
    private String idracIpAddress = "1.2.3.4";

    @Before
    public void setUp() throws Exception
    {
        this.handler = spy(new ConfigurePxeBootTaskHandler(this.service));
    }

    @Test
    public void executeTask_should_successfully_request_a_datastore_rename() throws Exception
    {
        doReturn(this.response).when(this.handler).initializeResponse(any());
        doReturn(this.request).when(this.job).getInputParams();
        doReturn(this.symphonyUuid).when(this.request).getSymphonyUuid();
        doReturn(this.idracIpAddress).when(this.request).getIdracIpAddress();
        doReturn(this.bootDeviceIdracStatus).when(this.service).configurePxeBoot(anyString(), anyString());
        doReturn("SUCCESS").when(this.bootDeviceIdracStatus).getStatus();

        boolean result = this.handler.executeTask(this.job);

        assertThat(result, is(true));
        verify(this.response).setWorkFlowTaskStatus(Status.SUCCEEDED);
        verify(this.response, never()).addError(anyString());
    }

    @Test
    public void executeTask_should_fail_the_workflow_when_the_configure_pxe_boot_request_fails() throws Exception
    {
        doReturn(this.response).when(this.handler).initializeResponse(any());
        doReturn(this.request).when(this.job).getInputParams();
        doReturn(this.symphonyUuid).when(this.request).getSymphonyUuid();
        doReturn(this.idracIpAddress).when(this.request).getIdracIpAddress();
        doReturn(this.bootDeviceIdracStatus).when(this.service).configurePxeBoot(anyString(), anyString());
        doReturn("FAIL").when(this.bootDeviceIdracStatus).getStatus();

        boolean result = this.handler.executeTask(this.job);

        assertThat(result, is(false));
        verify(this.response).setWorkFlowTaskStatus(Status.FAILED);
        verify(this.response).addError(anyString());
    }

    @Test
    public void executeTask_should_fail_the_workflow_when_an_exception_occurs() throws Exception
    {
        doReturn(this.response).when(this.handler).initializeResponse(any());
        doReturn(this.request).when(this.job).getInputParams();
        doReturn(this.symphonyUuid).when(this.request).getSymphonyUuid();
        doReturn(this.idracIpAddress).when(this.request).getIdracIpAddress();
        doThrow(new IllegalStateException("some-error")).when(this.service).configurePxeBoot(anyString(), anyString());

        boolean result = this.handler.executeTask(this.job);

        assertThat(result, is(false));
        verify(this.response).setWorkFlowTaskStatus(Status.FAILED);
        verify(this.response).addError(anyString());
    }

    @Test
    public void testInitializeResponse_should_successfully_create_the_response()
    {
        doReturn(this.task).when(this.job).getCurrentTask();
        String taskName = "configurePxeBootTask";
        doReturn(taskName).when(this.task).getTaskName();
        String stepName = "configurePxeBootStep";
        doReturn(stepName).when(this.job).getStep();

        ConfigureBootDeviceIdracResponse response = this.handler.initializeResponse(this.job);

        assertNotNull(response);
        assertEquals(taskName, response.getWorkFlowTaskName());
        assertEquals(Status.IN_PROGRESS, response.getWorkFlowTaskStatus());
    }

}