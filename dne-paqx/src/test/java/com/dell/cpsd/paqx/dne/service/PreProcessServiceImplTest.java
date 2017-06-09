/**
 * <p>
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries.  All Rights Reserved.
 * Dell EMC Confidential/Proprietary Information
 * </p>
 */

package com.dell.cpsd.paqx.dne.service;

import com.dell.cpsd.paqx.dne.domain.Job;
import com.dell.cpsd.paqx.dne.repository.InMemoryJobRepository;
import com.dell.cpsd.paqx.dne.service.model.Step;
import com.dell.cpsd.paqx.dne.service.preProcess.PreProcessService;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class PreProcessServiceImplTest {

    private WorkflowServiceImpl workflowServiceUnderTest;
    private PreProcessService   preProcessServiceUnderTest;
    @Before
    public void setUp() throws Exception
    {
        final InMemoryJobRepository inMemoryJobRepository = new InMemoryJobRepository();

        final Map<String, Step> workflowSteps = new HashMap<>();

        workflowSteps.put("startPreProcessWorkflow", new Step("completed", true));
        workflowSteps.put("completed", null);

        this.workflowServiceUnderTest =
                new WorkflowServiceImpl(inMemoryJobRepository, workflowSteps);

        this.preProcessServiceUnderTest = new PreProcessService();
        preProcessServiceUnderTest.setWorkflowService(workflowServiceUnderTest);
    }

    @Test
    public void testCreateWorkflow()
    {

        Job initialJob = preProcessServiceUnderTest.createWorkflow(
                "PreProcess", "startPreProcessWorkflow", "status1");

        assertNotNull(initialJob);
        assertNotNull(initialJob.getId());
    }

    @Test
    public void testFindJob()
    {
        Job initialJob = preProcessServiceUnderTest.createWorkflow(
                "PreProcess", "startPreProcessWorkflow", "status1");

        Job foundJob = preProcessServiceUnderTest.findJob(initialJob.getId());
        assertNotNull(initialJob);
        assertNotNull(foundJob);
        assertEquals(initialJob, foundJob);
    }

}
