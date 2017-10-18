/**
 * <p>
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries.  All Rights Reserved.
 * Dell EMC Confidential/Proprietary Information
 * </p>
 */

package com.dell.cpsd.paqx.dne.service.workflow.addnode;

import com.dell.cpsd.paqx.dne.domain.Job;
import com.dell.cpsd.paqx.dne.domain.WorkflowTask;
import com.dell.cpsd.paqx.dne.repository.InMemoryJobRepository;
import com.dell.cpsd.paqx.dne.service.WorkflowServiceImpl;
import com.dell.cpsd.paqx.dne.service.model.Step;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.greaterThan;

public class AddNodeServiceImplTest
{
    private AddNodeService            addNodeServiceUnderTest;
    private Map<String, WorkflowTask> workFlowTasks;

    @Before
    public void setUp()
    {
        final InMemoryJobRepository inMemoryJobRepository = new InMemoryJobRepository();

        final Map<String, Step> workflowSteps = new HashMap<>();

        workflowSteps.put("startAddNodeWorkflow", new Step("completed", true));
        workflowSteps.put("completed", null);

        WorkflowServiceImpl workflowServiceUnderTest = new WorkflowServiceImpl(inMemoryJobRepository, workflowSteps);

        this.addNodeServiceUnderTest = new AddNodeService();
        addNodeServiceUnderTest.setWorkflowService(workflowServiceUnderTest);

        workFlowTasks = addNodeServiceUnderTest.addNodeWorkflowTasks();
    }

    @Test
    public void testCreateWorkflow()
    {
        Job initialJob = addNodeServiceUnderTest.createWorkflow("addNode", "startAddNodeWorkflow", "status1");

        Assert.assertNotNull(initialJob);
        Assert.assertNotNull(initialJob.getId());
    }

    @Test
    public void testFindJob()
    {
        Job initialJob = addNodeServiceUnderTest.createWorkflow("addNode", "startAddNodeWorkflow", "status1");

        Job foundJob = addNodeServiceUnderTest.findJob(initialJob.getId());
        Assert.assertNotNull(initialJob);
        Assert.assertNotNull(foundJob);
        Assert.assertEquals(initialJob, foundJob);
    }

    @Test
    public void testAddNodeWorkFlowTasks_setup()
    {
        Assert.assertNotNull(workFlowTasks);
        Assert.assertThat(workFlowTasks.size(), greaterThan(0));
    }

    @Test
    public void testTaskName_updateSystemDefinitionTask()
    {
        Assert.assertEquals("Update System Definition", workFlowTasks.get("updateSystemDefinition").getTaskName());
    }

    @Test
    public void testTaskName_notifyNodeDiscoveryToUpdateStatus()
    {
        Assert.assertEquals("Notify Node Discovery To Update Status", workFlowTasks.get("notifyNodeDiscoveryToUpdateStatus").getTaskName());
    }

    @Test
    public void testTaskName_findConfigurePxeBoot()
    {
        Assert.assertEquals("Configure Pxe boot", workFlowTasks.get("configurePxeBoot").getTaskName());
    }
}
