/**
 * <p>
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries. All Rights Reserved. Dell EMC Confidential/Proprietary Information
 * </p>
 */

package com.dell.cpsd.paqx.dne.service.task.handler.addnode;

import static org.mockito.Mockito.when;

import org.apache.commons.collections.CollectionUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import com.dell.cpsd.paqx.dne.domain.Job;
import com.dell.cpsd.paqx.dne.repository.InMemoryJobRepository;
import com.dell.cpsd.paqx.dne.service.NodeService;
import com.dell.cpsd.paqx.dne.service.WorkflowService;
import com.dell.cpsd.paqx.dne.service.WorkflowServiceImpl;
import com.dell.cpsd.paqx.dne.service.model.ChangeIdracCredentialsResponse;
import com.dell.cpsd.paqx.dne.service.model.FirstAvailableDiscoveredNodeResponse;
import com.dell.cpsd.paqx.dne.service.model.NodeInfo;
import com.dell.cpsd.paqx.dne.service.model.NodeStatus;
import com.dell.cpsd.paqx.dne.service.workflow.addnode.AddNodeService;
import com.dell.cpsd.paqx.dne.service.workflow.addnode.AddNodeTaskConfig;
import com.dell.cpsd.service.common.client.exception.ServiceExecutionException;
import com.dell.cpsd.service.common.client.exception.ServiceTimeoutException;

/**
 * The tests for the AddNodeToSystemDefinitionTaskHandler class.
 * <p>
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries. All Rights Reserved. Dell EMC Confidential/Proprietary Information
 * </p>
 *
 * @since 1.0
 */
@RunWith(MockitoJUnitRunner.class)
public class ChangeIdracCredentialsTaskHandlerTest
{
    /*
     * 
     */

    @Mock
    private NodeService                       nodeService;
    /*
     * The AMQPClient instance
     */

    /*
     * The job running the add node to system definition task handler
     */
    private Job                               job;

    @InjectMocks
    private ChangeIdracCredentialsTaskHandler classUnderTest = new ChangeIdracCredentialsTaskHandler(this.nodeService);

    /**
     * The test setup.
     * 
     * @since 1.0
     */
    @Before
    public void setUp()
    {

        AddNodeTaskConfig addNodeConfig = new AddNodeTaskConfig();
        WorkflowService workflowService = new WorkflowServiceImpl(new InMemoryJobRepository(), addNodeConfig.addNodeWorkflowSteps());

        AddNodeService addNodeService = new AddNodeService();
        addNodeService.setWorkflowService(workflowService);

        this.job = addNodeService.createWorkflow("addNode", "changeIdracCredentials", "submitted");

    }

    @Test
    public void testExecuteTask_success() throws ServiceTimeoutException, ServiceExecutionException
    {

        FirstAvailableDiscoveredNodeResponse nodeDiscoverResponse = new FirstAvailableDiscoveredNodeResponse();
        nodeDiscoverResponse.setNodeInfo(new NodeInfo("symphonyUuid", "DellNodeId", NodeStatus.DISCOVERED));
        this.job.addTaskResponse("findAvailableNodes", nodeDiscoverResponse);

        ChangeIdracCredentialsResponse responseMessage = new ChangeIdracCredentialsResponse();
        responseMessage.setMessage("SUCCESS");
        when(this.nodeService.changeIdracCredentials(Mockito.anyString())).thenReturn(responseMessage);

        boolean result = classUnderTest.executeTask(this.job);

        Assert.assertTrue(result);

    }

    @Test()
    public void testExecuteTask__no_task() throws ServiceTimeoutException, ServiceExecutionException
    {
        boolean result = classUnderTest.executeTask(this.job);
        Assert.assertFalse(result);
        Assert.assertFalse(CollectionUtils.isEmpty(job.getTaskResponseMap().get("changeIdracCredentials").getErrors()));
    }

    @Test()
    public void testExecuteTask__no_node_info() throws ServiceTimeoutException, ServiceExecutionException
    {
        FirstAvailableDiscoveredNodeResponse nodeDiscoverResponse = new FirstAvailableDiscoveredNodeResponse();
        this.job.addTaskResponse("findAvailableNodes", nodeDiscoverResponse);
        boolean result = classUnderTest.executeTask(this.job);
        Assert.assertFalse(result);
        Assert.assertFalse(CollectionUtils.isEmpty(job.getTaskResponseMap().get("changeIdracCredentials").getErrors()));
    }

    @Test()
    public void testExecuteTask__error_response() throws ServiceTimeoutException, ServiceExecutionException
    {
        FirstAvailableDiscoveredNodeResponse nodeDiscoverResponse = new FirstAvailableDiscoveredNodeResponse();
        nodeDiscoverResponse.setNodeInfo(new NodeInfo("symphonyUuid", "DellNodeId", NodeStatus.DISCOVERED));
        this.job.addTaskResponse("findAvailableNodes", nodeDiscoverResponse);

        ChangeIdracCredentialsResponse responseMessage = new ChangeIdracCredentialsResponse();
        responseMessage.setMessage("FAILED");
        when(this.nodeService.changeIdracCredentials(Mockito.anyString())).thenReturn(responseMessage);
        boolean result = classUnderTest.executeTask(this.job);
        Assert.assertFalse(result);
    }

}
