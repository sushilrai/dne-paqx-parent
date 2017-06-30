/**
 * <p>
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries. All Rights Reserved. Dell EMC Confidential/Proprietary Information
 * </p>
 */
package com.dell.cpsd.paqx.dne.service.task.handler.addnode;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.dell.cpsd.paqx.dne.domain.Job;
import com.dell.cpsd.paqx.dne.repository.InMemoryJobRepository;
import com.dell.cpsd.paqx.dne.service.NodeService;
import com.dell.cpsd.paqx.dne.service.WorkflowService;
import com.dell.cpsd.paqx.dne.service.WorkflowServiceImpl;
import com.dell.cpsd.paqx.dne.service.model.FirstAvailableDiscoveredNodeResponse;
import com.dell.cpsd.paqx.dne.service.model.NodeInfo;
import com.dell.cpsd.paqx.dne.service.model.NodeStatus;
import com.dell.cpsd.paqx.dne.service.model.TaskResponse;
import com.dell.cpsd.paqx.dne.service.workflow.addnode.AddNodeService;
import com.dell.cpsd.paqx.dne.service.workflow.addnode.AddNodeTaskConfig;
import com.dell.cpsd.service.common.client.exception.ServiceExecutionException;
import com.dell.cpsd.service.common.client.exception.ServiceTimeoutException;

/**
 * The tests for the notify node discovery service node allocation complete task handler
 * <p>
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries.  All Rights Reserved.
 * Dell EMC Confidential/Proprietary Information
 * </p>
 *
 * @since 1.0
 */
@RunWith(MockitoJUnitRunner.class)
public class NotifyNodeDiscoveryNodeAllocationCompletedTaskHandlerTest {
    
    /**
     * The <code>NodeService</code> instance
     * 
     * @since   1.0
     */
    @Mock
    private NodeService nodeService = null;
    
    /*
     * The job running the notify node discovery service node allocation complete task handler
     */
    private Job job = null;
    
    /**
     * The test setup.
     * 
     * @since   1.0
     */
    @Before
    public void setUp() {
        AddNodeTaskConfig addNodeConfig = new AddNodeTaskConfig();
        WorkflowService workflowService = new WorkflowServiceImpl(new InMemoryJobRepository(), addNodeConfig.addNodeWorkflowSteps());
        
        AddNodeService addNodeService = new AddNodeService();
        addNodeService.setWorkflowService(workflowService);
        
        this.job = addNodeService.createWorkflow("addNode", "startAddNodeWorkflow", "submitted");
        
        FirstAvailableDiscoveredNodeResponse response = new FirstAvailableDiscoveredNodeResponse();
        response.setNodeInfo(new NodeInfo("symphonyUuid", "nodeId", NodeStatus.DISCOVERED));
        this.job.addTaskResponse("findAvailableNodes", response);
        
        this.job.changeToNextStep("notifyNodeDiscovery");
    }

    /**
     * Test successful execution of NotifyNodeDiscoveryNodeAllocationCompletedTaskHandler.executeTask() method
     * 
     * @throws ServiceExecutionException 
     * @throws ServiceTimeoutException 
     * 
     * @since   1.0
     */
    @Test
    public void testExecuteTask_successful_case() throws ServiceTimeoutException, ServiceExecutionException {
        ArgumentCaptor<String> savedCaptor = ArgumentCaptor.forClass(String.class);
        
        NotifyNodeDiscoveryNodeAllocationCompletedTaskHandler instance = new NotifyNodeDiscoveryNodeAllocationCompletedTaskHandler(this.nodeService);
        boolean expectedResult = true;
        boolean actualResult = instance.executeTask(this.job);
        
        assertEquals(expectedResult, actualResult);
        verify(this.nodeService, times(1)).notifyNodeAllocationComplete(savedCaptor.capture());
    }
    
    /**
     * Test error execution of NotifyNodeDiscoveryNodeAllocationCompletedTaskHandler.executeTask() method
     *  - test error case where no discovered node instance is present.
     *  
     * @throws ServiceExecutionException 
     * @throws ServiceTimeoutException 
     * 
     * @since   1.0
     */
    @Test
    public void testExecuteTask_no_discovered_node() throws ServiceTimeoutException, ServiceExecutionException {
        Map<String, TaskResponse> taskResponse = this.job.getTaskResponseMap();
        FirstAvailableDiscoveredNodeResponse response = (FirstAvailableDiscoveredNodeResponse)taskResponse.get("findAvailableNodes");
        response.setNodeInfo(null);
                
        NotifyNodeDiscoveryNodeAllocationCompletedTaskHandler instance = new NotifyNodeDiscoveryNodeAllocationCompletedTaskHandler(this.nodeService);
        boolean expectedResult = false;
        boolean actualResult = instance.executeTask(this.job);
        assertEquals(expectedResult, actualResult);
        
        ArgumentCaptor<String> savedCaptor = ArgumentCaptor.forClass(String.class);
        verify(this.nodeService, times(0)).notifyNodeAllocationComplete(savedCaptor.capture());
    }
}
