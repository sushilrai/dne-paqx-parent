/**
 * <p>
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries.  All Rights Reserved.
 * Dell EMC Confidential/Proprietary Information
 * </p>
 */
package com.dell.cpsd.paqx.dne.domain;

import com.dell.cpsd.paqx.dne.domain.scaleio.ScaleIOData;
import com.dell.cpsd.paqx.dne.domain.vcenter.VCenter;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * <p>
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries.  All Rights Reserved.
 * Dell EMC Confidential/Proprietary Information
 * </p>
 */
public class DneJobTest
{
    @Test
    public void testDneJob()
    {
        String id="id";
        ScaleIOData scaleIOData=  new ScaleIOData();
        VCenter vCenterData=  new VCenter();
        DneJob job = new DneJob("id", scaleIOData, vCenterData);

        assertTrue(job.getId()==id);
        assertTrue(job.getVcenter()==vCenterData);
        assertTrue(job.getScaleIO()==scaleIOData);

        //Test 2nd one
        String id2="id2";
        ScaleIOData scaleIOData2=  new ScaleIOData();
        VCenter vCenterData2=  new VCenter();
        DneJob job2 = new DneJob();
        job2.setId(id2);
        job2.setVcenter(vCenterData2);
        job2.setScaleIO(scaleIOData2);

        assertTrue(job2.getId()==id2);
        assertTrue(job2.getVcenter()==vCenterData2);
        assertTrue(job2.getScaleIO()==scaleIOData2);
    }
}
