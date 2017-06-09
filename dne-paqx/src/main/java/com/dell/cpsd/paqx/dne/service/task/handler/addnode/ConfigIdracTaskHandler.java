/**
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries.  All Rights Reserved.
 * VCE Confidential/Proprietary Information
 */

package com.dell.cpsd.paqx.dne.service.task.handler.addnode;

import com.dell.cpsd.paqx.dne.domain.IWorkflowTaskHandler;
import com.dell.cpsd.paqx.dne.domain.Job;
import com.dell.cpsd.paqx.dne.service.model.Status;
import com.dell.cpsd.paqx.dne.service.model.TaskResponse;
import com.dell.cpsd.paqx.dne.service.task.handler.BaseTaskHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfigIdracTaskHandler extends BaseTaskHandler implements IWorkflowTaskHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigIdracTaskHandler.class);

    @Override
    public boolean executeTask(Job job)
    {
        LOGGER.info("Execute ConfigIdracTaskHandler task");
        TaskResponse response = initializeResponse(job);
        LOGGER.info("Idrac input parameters are:");
        LOGGER.info("Idrac IP Address:" + job.getInputParams().getIdracIpAddress());
        LOGGER.info("Idrac Gateway Address:" + job.getInputParams().getIdracGatewayIpAddress());
        LOGGER.info("Idrac Subnet Mask:" + job.getInputParams().getIdracSubnetMask());
        try {
            Thread.sleep(20000);
        }
        catch(Exception e){}
        response.setWorkFlowTaskStatus(Status.SUCCEEDED);
        return true;
    }
}
