/**
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries.  All Rights Reserved.
 * Dell EMC Confidential/Proprietary Information
 */

package com.dell.cpsd.paqx.dne.repository;

import com.dell.cpsd.paqx.dne.domain.Job;
import org.junit.Before;
import org.junit.Test;

import java.util.UUID;

import static org.junit.Assert.*;

public class InMemoryJobRepositoryTest
{
    /*
     * The workflow service to test
     */
    private InMemoryJobRepository inMemoryJobRepository;
    

    /**
     * This sets up the test
     * 
     * @since   1.0
     */
    @Before
    public void setUp() throws Exception
    {
        inMemoryJobRepository = new InMemoryJobRepository();
    }
    
 
    /**
     * This tests the <code>save</code> with a valid job.
     * 
     * @since   1.0
     */
    @Test
    public void testSave()
    {
        final Job job = this.makeJob();

        this.inMemoryJobRepository.save(job);

        final Job foundJob = this.inMemoryJobRepository.find(job.getId());

        assertNotNull(foundJob);
    }
    
   
    /**
     * This tests the <code>save</code> with an null job.
     * 
     * @since   1.0
     */
    @Test(expected = IllegalArgumentException.class)
    public void testSaveNullJob()
    {
        this.inMemoryJobRepository.save(null);
    }
    
    
    /**
     * This tests the <code>findAll</code>.
     * 
     * @since   1.0
     */
    @Test
    public void testFinalAll() 
    {
        final Job job = this.makeJob();

        this.inMemoryJobRepository.save(job);

        final Job[] jobs = this.inMemoryJobRepository.findAll();

        assertNotNull(jobs);
        assertEquals(jobs.length, 1);
    }
    
    
    /**
     * This tests the <code>find</code> with a valid uuid
     * 
     * @since   1.0
     */
    @Test
    public void testFind() 
    {
        final Job job = this.makeJob();

        this.inMemoryJobRepository.save(job);

        final Job foundJob = this.inMemoryJobRepository.find(job.getId());

        assertNotNull(foundJob);
        assertEquals(foundJob, job);
    }
    
    
    /**
     * This tests the <code>find</code> with a non existant uuid
     * 
     * @since   1.0
     */
    @Test
    public void testFindNonExistant() 
    {
        final Job job = this.makeJob();

        this.inMemoryJobRepository.save(job);

        final Job foundJob = this.inMemoryJobRepository.find(UUID.randomUUID());

        assertNull(foundJob);
    }
    
    
    /**
     * This tests the <code>find</code> with a non existant uuid
     * 
     * @since   1.0
     */
    @Test
    public void testFindNullId() 
    {
        final Job job = this.makeJob();

        this.inMemoryJobRepository.save(job);

        final Job foundJob = this.inMemoryJobRepository.find(null);

        assertNull(foundJob);
    }
    
    
    /**
     * This creates a job that can be used in testing.
     * 
     * @return  A <code>Job</code> that can be used in testing.
     * 
     * @since   1.0
     */
    private Job makeJob()
    {
        return new Job(UUID.randomUUID(), "addNode", "startAddNodeWorkflow", "status1", null);
    }
}
