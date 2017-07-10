/**
 * <p>
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries. All Rights Reserved. Dell EMC Confidential/Proprietary Information
 * </p>
 */

package com.dell.cpsd.paqx.dne.service.task.handler.addnode;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.dell.cpsd.paqx.dne.domain.Job;
import com.dell.cpsd.paqx.dne.repository.InMemoryJobRepository;
import com.dell.cpsd.paqx.dne.service.WorkflowService;
import com.dell.cpsd.paqx.dne.service.WorkflowServiceImpl;
import com.dell.cpsd.paqx.dne.service.model.FirstAvailableDiscoveredNodeResponse;
import com.dell.cpsd.paqx.dne.service.model.NodeExpansionRequest;
import com.dell.cpsd.paqx.dne.service.model.NodeInfo;
import com.dell.cpsd.paqx.dne.service.model.NodeStatus;
import com.dell.cpsd.paqx.dne.service.model.TaskResponse;
import com.dell.cpsd.paqx.dne.service.workflow.addnode.AddNodeService;
import com.dell.cpsd.paqx.dne.service.workflow.addnode.AddNodeTaskConfig;
import com.dell.cpsd.sdk.AMQPClient;
import com.dell.cpsd.service.common.client.exception.ServiceExecutionException;
import com.dell.cpsd.service.common.client.exception.ServiceTimeoutException;
import com.dell.cpsd.service.system.definition.api.Component;
import com.dell.cpsd.service.system.definition.api.ComponentsFilter;
import com.dell.cpsd.service.system.definition.api.ConvergedSystem;
import com.dell.cpsd.service.system.definition.api.Group;

/**
 * The tests for the AddNodeToSystemDefinitionTaskHandler class.
 * <p>
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries. All Rights Reserved. Dell EMC Confidential/Proprietary Information
 * </p>
 *
 * @since 1.0
 */
@RunWith(MockitoJUnitRunner.class)
public class AddNodeToSystemDefinitionTaskHandlerTest
{

    /*
     * The AMQPClient instance
     */
    @Mock
    private AMQPClient client = null;

    /*
     * The job running the add node to system definition task handler
     */
    private Job        job    = null;

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

        this.job = addNodeService.createWorkflow("addNode", "startAddNodeWorkflow", "submitted");
        this.job.setInputParams(new NodeExpansionRequest("idracIpAddress", "idracGatewayIpAddress", "idracSubnetMask",
                "managementIpAddress", "esxiKernelIpAddress1", "esxiKernelIpAddress2", "scaleIOSVMDataIpAddress1",
                "scaleIOSVMDataIpAddress2", "scaleIOSVMManagementIpAddress"));

        FirstAvailableDiscoveredNodeResponse response = new FirstAvailableDiscoveredNodeResponse();
        response.setNodeInfo(new NodeInfo("symphonyUuid", "nodeId", NodeStatus.DISCOVERED));
        this.job.addTaskResponse("findAvailableNodes", response);

        this.job.changeToNextStep("updateSystemDefinition");
    }

    /**
     * Test successful execution of AddNodeToSystemDefinitionTaskHandler.executeTask() method
     * 
     * @since 1.0
     */
    @Test
    public void testExecuteTask_successful_case()
    {
        ConvergedSystem cs = new ConvergedSystem();
        cs.setUuid(UUID.randomUUID().toString());

        ComponentsFilter filter = new ComponentsFilter();
        filter.setSystemUuid(cs.getUuid());

        ConvergedSystem systemToUpdate = new ConvergedSystem();
        List<Group> groups = Arrays.asList(new Group("uuid1", "SystemCompute", Group.Type.COMPUTE, null, null, null));
        systemToUpdate.setGroups(groups);

        when(this.client.getConvergedSystems()).thenReturn(Arrays.asList(cs));
        when(this.client.getComponents(filter)).thenReturn(Arrays.asList(systemToUpdate));

        ArgumentCaptor<ConvergedSystem> csCaptor = ArgumentCaptor.forClass(ConvergedSystem.class);
        AddNodeToSystemDefinitionTaskHandler instance = new AddNodeToSystemDefinitionTaskHandler(this.client);
        boolean expectedResult = true;
        boolean actualResult = instance.executeTask(this.job);

        assertEquals(expectedResult, actualResult);
        verify(this.client, times(1)).createOrUpdateConvergedSystem(csCaptor.capture(), eq(null));
    }

    /**
     * Test error execution of AddNodeToSystemDefinitionTaskHandler.executeTask() method - test error case where no 
     * findAvailableNodes task response was set.
     * 
     * @throws ServiceExecutionException
     * @throws ServiceTimeoutException
     * 
     * @since 1.0
     */
    @Test
    public void testExecuteTask_no_find_nodes_response() throws ServiceTimeoutException, ServiceExecutionException
    {
        Map<String, TaskResponse> taskResponse = this.job.getTaskResponseMap();
        taskResponse.remove("findAvailableNodes");

        ArgumentCaptor<ConvergedSystem> csCaptor = ArgumentCaptor.forClass(ConvergedSystem.class);
        AddNodeToSystemDefinitionTaskHandler instance = new AddNodeToSystemDefinitionTaskHandler(this.client);
        boolean expectedResult = false;
        boolean actualResult = instance.executeTask(this.job);

        assertEquals(expectedResult, actualResult);
        verify(this.client, times(0)).createOrUpdateConvergedSystem(csCaptor.capture(), eq(null));
    }

    /**
     * Test error execution of AddNodeToSystemDefinitionTaskHandler.executeTask() method - test error case where no discovered node instance
     * is present.
     * 
     * @since 1.0
     */
    @Test
    public void testExecuteTask_no_discovered_node()
    {
        Map<String, TaskResponse> taskResponse = this.job.getTaskResponseMap();
        FirstAvailableDiscoveredNodeResponse response = (FirstAvailableDiscoveredNodeResponse) taskResponse.get("findAvailableNodes");
        response.setNodeInfo(null);

        ArgumentCaptor<ConvergedSystem> csCaptor = ArgumentCaptor.forClass(ConvergedSystem.class);
        AddNodeToSystemDefinitionTaskHandler instance = new AddNodeToSystemDefinitionTaskHandler(this.client);
        boolean expectedResult = false;
        boolean actualResult = instance.executeTask(this.job);

        assertEquals(expectedResult, actualResult);
        verify(this.client, times(0)).createOrUpdateConvergedSystem(csCaptor.capture(), eq(null));
    }
    
    /**
     * Test error execution of AddNodeToSystemDefinitionTaskHandler.executeTask() method - test error case where no converged systems 
     * are present.
     * 
     * @since 1.0
     */
    @Test
    public void testExecuteTask_no_converged_systems()
    {
        when(this.client.getConvergedSystems()).thenReturn(Collections.emptyList());

        ArgumentCaptor<ConvergedSystem> csCaptor = ArgumentCaptor.forClass(ConvergedSystem.class);
        AddNodeToSystemDefinitionTaskHandler instance = new AddNodeToSystemDefinitionTaskHandler(this.client);
        boolean expectedResult = false;
        boolean actualResult = instance.executeTask(this.job);

        assertEquals(expectedResult, actualResult);
        verify(this.client, times(0)).createOrUpdateConvergedSystem(csCaptor.capture(), eq(null));
    }

    /**
     * Test error execution of AddNodeToSystemDefinitionTaskHandler.executeTask() method - test error case where no converged system
     * instance is present.
     * 
     * @since 1.0
     */
    @Test
    public void testExecuteTask_no_converged_system()
    {
        when(this.client.getConvergedSystems()).thenReturn(new ArrayList<>());

        ArgumentCaptor<ConvergedSystem> csCaptor = ArgumentCaptor.forClass(ConvergedSystem.class);
        AddNodeToSystemDefinitionTaskHandler instance = new AddNodeToSystemDefinitionTaskHandler(this.client);
        boolean expectedResult = false;
        boolean actualResult = instance.executeTask(this.job);

        assertEquals(expectedResult, actualResult);
        verify(this.client, times(0)).createOrUpdateConvergedSystem(csCaptor.capture(), eq(null));
    }
    
    /**
     * Test execution of AddNodeToSystemDefinitionTaskHandler.executeTask() method - test case where a node that
     * already exists in the system definition is attempted to be added again.
     * 
     * @since 1.0
     */
    @Test
    public void testExecuteTask_no_duplicate_nodes()
    {
        ConvergedSystem cs = new ConvergedSystem();
        cs.setUuid(UUID.randomUUID().toString());

        NodeInfo nodeInfo = ((FirstAvailableDiscoveredNodeResponse)this.job.getTaskResponseMap().get("findAvailableNodes")).getNodeInfo();
        Component node = new Component();
        node.setIdentity(nodeInfo.getIdentity());

        ConvergedSystem system = new ConvergedSystem();
        system.setUuid(UUID.randomUUID().toString());
        system.getComponents().add(node);

        ComponentsFilter filter = new ComponentsFilter();
        filter.setSystemUuid(cs.getUuid());

        when(this.client.getConvergedSystems()).thenReturn(Arrays.asList(cs));
        when(this.client.getComponents(filter)).thenReturn(Arrays.asList(system));

        ArgumentCaptor<ConvergedSystem> csCaptor = ArgumentCaptor.forClass(ConvergedSystem.class);
        AddNodeToSystemDefinitionTaskHandler instance = new AddNodeToSystemDefinitionTaskHandler(this.client);
        boolean expectedResult = true;
        boolean actualResult = instance.executeTask(this.job);

        assertEquals(expectedResult, actualResult);
        verify(this.client, times(1)).createOrUpdateConvergedSystem(csCaptor.capture(), eq(null));
        assertEquals(1, csCaptor.getValue().getComponents().size());// Components list should be unaltered...
    }
}
