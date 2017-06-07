package com.dell.cpsd.paqx.dne.service;

import com.dell.cpsd.paqx.dne.domain.Job;
import com.dell.cpsd.paqx.dne.repository.InMemoryJobRepository;
import com.dell.cpsd.paqx.dne.service.WorkflowServiceImpl;
import com.dell.cpsd.paqx.dne.service.addNode.AddNodeService;
import com.dell.cpsd.paqx.dne.service.model.Step;
import com.dell.cpsd.paqx.dne.service.preProcess.PreProcessService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * <p>
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries.  All Rights Reserved.
 * Dell EMC Confidential/Proprietary Information
 * </p>
 */
public class AddNodeServiceImplTest {
    private WorkflowServiceImpl workflowServiceUnderTest;
    private AddNodeService addNodeServiceUnderTest;
    @Before
    public void setUp() throws Exception
    {
        final InMemoryJobRepository inMemoryJobRepository = new InMemoryJobRepository();

        final Map<String, Step> workflowSteps = new HashMap<>();

        workflowSteps.put("startAddNodeWorkflow", new Step("completed", true));
        workflowSteps.put("completed", null);

        this.workflowServiceUnderTest =
                new WorkflowServiceImpl(inMemoryJobRepository, workflowSteps);

        this.addNodeServiceUnderTest = new AddNodeService();
        addNodeServiceUnderTest.setWorkflowService(workflowServiceUnderTest);
    }

    @Test
    public void testCreateWorkflow()
    {

        Job initialJob = addNodeServiceUnderTest.createWorkflow(
                "PreProcess", "startPreProcessWorkflow", "status1");

        Assert.assertNotNull(initialJob);
        Assert.assertNotNull(initialJob.getId());
    }

    @Test
    public void testFindJob()
    {
        Job initialJob = addNodeServiceUnderTest.createWorkflow(
                "PreProcess", "startPreProcessWorkflow", "status1");

        Job foundJob = addNodeServiceUnderTest.findJob(initialJob.getId());
        Assert.assertNotNull(initialJob);
        Assert.assertNotNull(foundJob);
        Assert.assertEquals(initialJob, foundJob);
    }
}
