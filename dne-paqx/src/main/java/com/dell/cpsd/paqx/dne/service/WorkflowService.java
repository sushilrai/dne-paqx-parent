/**
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries.  All Rights Reserved.
 * VCE Confidential/Proprietary Information
 */

package com.dell.cpsd.paqx.dne.service;

import com.dell.cpsd.paqx.dne.domain.Job;

import com.dell.cpsd.paqx.dne.domain.WorkflowTask;
import com.dell.cpsd.paqx.dne.service.model.Step;

import java.util.Map;
import java.util.UUID;

public interface WorkflowService
{
    /**
     * This creates a job for the specified workflow type.
     * 
     * @param   workflowType    The type of workflow.
     * @param   startingStep    The starting step in the workflow.
     * @param   startingStep     The current step in the workflow.
     * 
     * @return  The job for the specified workflow.
     * 
     * @since   1.0
     */
    Job createWorkflow(final String workflowType, final String startingStep, 
            final String currentStatus, Map<String, WorkflowTask> taskMap);

    
    /**
     * This returns the next step in the workflow, or null if there is none.
     * 
     * @param   workflowType    The type of workflow.
     * @param   currentStep     The current step in the workflow.
     * 
     * @return  The next step in the workflow, or null.
     * 
     * @since   1.0
     */
    Step findNextStep(final String workflowType, final String currentStep);

//    public Task getTask(String currentStep);
    
    /**
     * This returns the active workflow jobs.
     * 
     * @return  The active workflow jobs.
     * 
     * @since   1.0
     */
    Job[] findActiveJobs();

    
    /**
     * This returns the job with the specified job identifier.
     * 
     * @param   jobId   The job identifier.
     * 
     * @return  The job with the identifier, or null.
     * 
     * @since   1.0
     */
    Job findJob(final UUID jobId);

    void saveJob(final Job job);
    /**
     * This advances the job to the next step and assigns the specified status.
     * 
     * @param   job             The job to advance.
     * @param   currentStep     The current step.
     * @param   currentStatus   The status to assign to the job.
     * 
     * @return  The job advanced to the next step.
     * 
     * @since   1.0
     */
    Job advanceToNextStep(final Job job, final String currentStep, final String currentStatus);

    public Map<String, Step> getWorkflowSteps();
}
