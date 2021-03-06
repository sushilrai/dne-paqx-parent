/**
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries.  All Rights Reserved.
 * VCE Confidential/Proprietary Information
 */

package com.dell.cpsd.paqx.dne.repository;

import com.dell.cpsd.paqx.dne.domain.Job;

import java.util.UUID;

/**
 * This interface should be implemented by a repository of workflow jobs.
 * 
 * Note: This is to enable initial integration and is expected to be replaced.
 * 
 * <p>
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries.  All Rights Reserved.
 * VCE Confidential/Proprietary Information
 * <p/>
 *
 * @since   1.0
  */
public interface JobRepository
{
    /**
     * This saves a job to the repository.
     * 
     * @param   job The job to save.
     * 
     * @since   1.0
     */
    void save(Job job);
    

    /**
     * This returns all the jobs in the repository.
     * 
     * @return  The jobs in the repository.
     * 
     * @since   1.0
     */
    Job[] findAll();

    
    /**
     * This finds the job with the specified identifier, or null.
     * 
     * @param   jobId   The job identifier.
     * 
     * @return  The job with the specified identifier, or null.
     * 
     * @since   1.0
     */
    Job find(UUID jobId);
}
