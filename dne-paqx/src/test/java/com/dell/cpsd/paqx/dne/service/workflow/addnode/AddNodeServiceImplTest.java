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
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

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

        assertNotNull(initialJob);
        assertNotNull(initialJob.getId());
    }

    @Test
    public void testFindJob()
    {
        Job initialJob = addNodeServiceUnderTest.createWorkflow("addNode", "startAddNodeWorkflow", "status1");

        Job foundJob = addNodeServiceUnderTest.findJob(initialJob.getId());
        assertNotNull(initialJob);
        assertNotNull(foundJob);
        assertEquals(initialJob, foundJob);
    }

    @Test
    public void testAddNodeWorkFlowTasks_setup()
    {
        assertNotNull(workFlowTasks);
        assertThat(workFlowTasks.size(), greaterThan(0));
    }

    @Test
    public void testTaskName_addHostToProtectionDomain()
    {
        assertEquals("Add host to protection domain", workFlowTasks.get("addHostToProtectionDomain").getTaskName());
    }

    @Test
    public void testTaskName_findConfigurePxeBoot()
    {
        assertEquals("Configure PXE boot", workFlowTasks.get("configurePxeBoot").getTaskName());
    }

    @Test
    public void testTaskName_updateSystemDefinitionTask()
    {
        assertEquals("Update System Definition", workFlowTasks.get("updateSystemDefinition").getTaskName());
    }

    @Test
    public void testTaskName_notifyNodeDiscoveryToUpdateStatus()
    {
        assertEquals("Notify node discovery to update status", workFlowTasks.get("notifyNodeDiscoveryToUpdateStatus").getTaskName());
    }
}
