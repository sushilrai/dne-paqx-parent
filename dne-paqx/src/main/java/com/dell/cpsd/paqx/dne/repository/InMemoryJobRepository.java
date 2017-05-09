/**
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries.  All Rights Reserved.
 * VCE Confidential/Proprietary Information
 */

package com.dell.cpsd.paqx.dne.repository;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.dell.cpsd.paqx.dne.domain.Job;

import org.springframework.stereotype.Repository;

/**
 * This is an in memory repository for jobs. 
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
@Repository
public class InMemoryJobRepository implements JobRepository
{
    /*
     * The map of jobs
     */
    private final Map<UUID, Job> jobs = new HashMap<>();

    /**
     * {@inheritDoc}
     */
    @Override 
    public void save(final Job job)
    {
        if (job == null)
        {
            throw new IllegalArgumentException("The job to save is null.");
        }
        
        jobs.put(job.getId(), job);
    }
    

    /**
     * {@inheritDoc}
     */
    @Override 
    public Job[] findAll()
    {
        Job[] results = new Job[jobs.size()];
        return jobs.values().toArray(results);
    }

    
    /**
     * {@inheritDoc}
     */
    @Override 
    public Job find(final UUID jobId)
    {
        return jobs.get(jobId);
    }
}
