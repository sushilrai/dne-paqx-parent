/**
 * <p>
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries. All Rights Reserved. Dell EMC Confidential/Proprietary Information
 * </p>
 */

package com.dell.cpsd.paqx.dne.service.task.handler.addnode;

import com.dell.cpsd.paqx.dne.domain.Job;
import com.dell.cpsd.paqx.dne.domain.WorkflowTask;
import com.dell.cpsd.paqx.dne.service.NodeService;
import com.dell.cpsd.paqx.dne.service.model.ChangeIdracCredentialsResponse;
import com.dell.cpsd.paqx.dne.service.model.NodeExpansionRequest;
import com.dell.cpsd.paqx.dne.service.model.Status;
import com.dell.cpsd.paqx.dne.service.model.TaskResponse;
import com.dell.cpsd.service.common.client.exception.ServiceExecutionException;
import com.dell.cpsd.service.common.client.exception.ServiceTimeoutException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
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
@RunWith(MockitoJUnitRunner.class)
public class ChangeIdracCredentialsTaskHandlerTest
{
    @Mock
    private WorkflowTask task;

    @Mock
    private NodeService  nodeService;

    @Mock
    private Job job;

    @Mock
    private ChangeIdracCredentialsResponse response;

    @Mock
    private NodeExpansionRequest request;

    private ChangeIdracCredentialsTaskHandler handler;
    private String symphonyUuid = "symphonyUuid";
    private String taskName = "changeIdracCredentialsTask";
    private String stepName = "changeIdracCredentialsStep";

    /**
     * The test setup.
     * 
     * @since 1.0
     */
    @Before
    public void setUp()
    {
        this.handler = spy(new ChangeIdracCredentialsTaskHandler(this.nodeService));
    }

    @Test
    public void testExecuteTask_should_successfully_change_the_idrac_credentials() throws Exception
    {
        doReturn(this.response).when(this.handler).initializeResponse(this.job);
        doReturn(this.request).when(this.job).getInputParams();
        doReturn(this.symphonyUuid).when(this.request).getSymphonyUuid();
        doReturn(this.response).when(this.nodeService).changeIdracCredentials(anyString());
        doReturn("SUCCESS").when(this.response).getMessage();

        boolean result  = this.handler.executeTask(this.job);

        assertThat(result, is(true));
        verify(this.response).setWorkFlowTaskStatus(Status.SUCCEEDED);
        verify(this.response, never()).addError(anyString());
    }

    @Test()
    public void testExecuteTask_should_fail_when_no_input_params() throws ServiceTimeoutException, ServiceExecutionException
    {
        doReturn(this.response).when(this.handler).initializeResponse(this.job);
        doReturn(null).when(this.job).getInputParams();

        boolean result  = this.handler.executeTask(this.job);

        assertThat(result, is(false));
        verify(this.response).setWorkFlowTaskStatus(Status.FAILED);
        verify(this.response).addError(anyString());
    }

    @Test()
    public void testExecuteTask_should_fail_when_no_symhpony_uuid() throws ServiceTimeoutException, ServiceExecutionException
    {
        doReturn(this.response).when(this.handler).initializeResponse(this.job);
        doReturn(this.request).when(this.job).getInputParams();
        doReturn(null).when(this.request).getSymphonyUuid();

        boolean result  = this.handler.executeTask(this.job);

        assertThat(result, is(false));
        verify(this.response).setWorkFlowTaskStatus(Status.FAILED);
        verify(this.response).addError(anyString());
    }

    @Test()
    public void testExecuteTask_should_fail_when_the_request_to_the_node_service_fails() throws ServiceTimeoutException, ServiceExecutionException
    {
        doReturn(this.response).when(this.handler).initializeResponse(this.job);
        doReturn(this.request).when(this.job).getInputParams();
        doReturn(this.symphonyUuid).when(this.request).getSymphonyUuid();
        doReturn(this.response).when(this.nodeService).changeIdracCredentials(anyString());
        doReturn("FAILED").when(this.response).getMessage();

        boolean result  = this.handler.executeTask(this.job);

        assertThat(result, is(false));
        verify(this.response).setWorkFlowTaskStatus(Status.FAILED);
        verify(this.response).addError(anyString());
    }

    @Test
    public void testInitializeResponse_should_successfully_create_the_response()
    {
        doReturn(this.task).when(this.job).getCurrentTask();
        doReturn(this.taskName).when(this.task).getTaskName();
        doReturn(this.stepName).when(this.job).getStep();

        TaskResponse response = this.handler.initializeResponse(this.job);

        assertNotNull(response);
        assertEquals(this.taskName, response.getWorkFlowTaskName());
        assertEquals(Status.IN_PROGRESS, response.getWorkFlowTaskStatus());
    }
}
