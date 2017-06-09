/**
 * This is a service that manages the steps in a workflow.
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries.  All Rights Reserved.
 * VCE Confidential/Proprietary Information
 */

package com.dell.cpsd.paqx.dne.service;

import com.dell.cpsd.paqx.dne.domain.Job;
import com.dell.cpsd.paqx.dne.domain.WorkflowTask;
import com.dell.cpsd.paqx.dne.repository.JobRepository;
import com.dell.cpsd.paqx.dne.service.model.Step;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

@Service
public class WorkflowServiceImpl implements WorkflowService
{
    /*
     * The job repository.
     */
    private final JobRepository jobRepository;
    
    /*
     * The workflow steps.
     */
    private final Map<String, Step> workflowSteps;

    public Map<String, Step> getWorkflowSteps() {
        return workflowSteps;
    }

    /**
     * WorkflowServiceImpl constructor.
     * 
     * @param   jobRepository   The job repository.
     * @param   workflowSteps   The workflow steps.
     * 
     * @throws  IllegalArgumentException    Thrown if the arguments are invalid.
     * @since   1.0
     */
    public WorkflowServiceImpl(final JobRepository jobRepository, 
            final Map<String, Step> workflowSteps)
    {
        super();
        
        if (jobRepository == null)
        {
            throw new IllegalArgumentException("The job repository is null.");
        }
        
        if (workflowSteps == null)
        {
            throw new IllegalArgumentException("The workflow steps is null.");
        }
        
        this.jobRepository = jobRepository;
        this.workflowSteps = workflowSteps;
    }
    

    /**
     * {@inheritDoc}
     */
    @Override 
    public Job createWorkflow(final String workflowType, final String startingStep, final String currentStatus, Map<String, WorkflowTask>taskMap)
    {
        final Job job = new Job(UUID.randomUUID(), workflowType, startingStep, currentStatus, taskMap);
        //set the job correlationId to each task.
        taskMap.forEach((k,v) -> v.setJobCorrelationId(job.getId()));
        jobRepository.save(job);
        return job;
    }

    
    /**
     * {@inheritDoc}
     */
    @Override 
    public Step findNextStep(final String workflowType, final String currentStep)
    {
        return workflowSteps.get(currentStep);
    }

    
    /**
     * {@inheritDoc}
     */
    @Override 
    public Job[] findActiveJobs()
    {
        return jobRepository.findAll();
    }

    
    /**
     * {@inheritDoc}
     */
    @Override 
    public Job findJob(final UUID jobId)
    {
        return jobRepository.find(jobId);
    }

//    @Override
//    public Task getTask(String currentStep){
//        return workflowTasks.get(currentStep);
//    }
    /**
     * {@inheritDoc}
     */
    @Override 
    public Job advanceToNextStep(final Job job, String currentStep, final String currentStatus)
    {
        final Step nextStep = findNextStep(job.getWorkflow(), currentStep);
        
        if (nextStep != null)
        {
            job.changeToNextStep(nextStep.getNextStep());
        }
        
        job.setStateDetails(currentStatus);
        
        jobRepository.save(job);
        return job;
    }

    public void saveJob(final Job job){
        jobRepository.save(job);
    }
}
