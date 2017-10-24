/**
 * <p>
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries. All Rights Reserved. Dell EMC Confidential/Proprietary Information
 * </p>
 */

package com.dell.cpsd.paqx.dne.service.task.handler.addnode;

import com.dell.cpsd.paqx.dne.domain.IWorkflowTaskHandler;
import com.dell.cpsd.paqx.dne.domain.Job;
import com.dell.cpsd.paqx.dne.service.NodeService;
import com.dell.cpsd.paqx.dne.service.model.*;
import com.dell.cpsd.paqx.dne.service.task.handler.BaseTaskHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Task responsible for handling Boot Order Sequence
 *
 * <p>
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries. All Rights Reserved. Dell EMC Confidential/Proprietary Information
 * </p>
 *
 * @since 1.0
 */
@Component
public class ConfigurePxeBootTaskHandler extends BaseTaskHandler implements IWorkflowTaskHandler
{
    /**
     * The logger instance
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigurePxeBootTaskHandler.class);

    /**
     * The <code>NodeService</code> instance
     */
    private final NodeService nodeService;

    /**
     *  SetBootOrderAndDisablePXETaskHandler constructor.
     *
     * @param nodeService
     *            - The <code>NodeService</code> instance.
     *
     * @since 1.0
     */
    public ConfigurePxeBootTaskHandler(NodeService nodeService){
        this.nodeService = nodeService;
    }

    /**
     * Perform the task of setting up boot order and disabling PXE
     *
     * @param job
     *            - The <code>Job</code> this task is part of.
     *
     * @since 1.0
     */
    @Override
    public boolean executeTask(Job job)
    {   LOGGER.info("Execute Config Pxe Boot Task");
        TaskResponse response = initializeResponse(job);

        try
        {
            String uuid = job.getInputParams().getSymphonyUuid();
            String ipAddress = job.getInputParams().getIdracIpAddress();

            LOGGER.info("uuid:" + uuid);
            LOGGER.info("ipAddress:" + ipAddress);

            BootDeviceIdracStatus bootDeviceIdracStatus = nodeService.configurePxeBoot(uuid, ipAddress);

            if (processSuccessOrFailure(bootDeviceIdracStatus.getStatus(),bootDeviceIdracStatus.getErrors(),response,"bootDeviceIdrac"))
            {
                return true;
            }
        }
        catch(Exception e)
        {
            LOGGER.error("Error showing boot order status", e);
            response.addError(e.toString());
        }
        response.setWorkFlowTaskStatus(Status.FAILED);
        return false;
    }

    /**
     * Create the <code>BootOrderSequenceResponse</code> instance and initialize it.
     *
     * @param job
     *            - The <code>Job</code> this task is part of.
     */
    @Override
    public ConfigureBootDeviceIdracResponse initializeResponse(Job job)
    {
        ConfigureBootDeviceIdracResponse response = new ConfigureBootDeviceIdracResponse();
        setupResponse(job, response);
        return response;
    }
}
