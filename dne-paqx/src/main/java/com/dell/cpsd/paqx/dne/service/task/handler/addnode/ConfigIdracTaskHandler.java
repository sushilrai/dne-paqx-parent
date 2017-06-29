/**
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries.  All Rights Reserved.
 * VCE Confidential/Proprietary Information
 */

package com.dell.cpsd.paqx.dne.service.task.handler.addnode;

import com.dell.cpsd.paqx.dne.domain.IWorkflowTaskHandler;
import com.dell.cpsd.paqx.dne.domain.Job;
import com.dell.cpsd.paqx.dne.service.NodeService;
import com.dell.cpsd.paqx.dne.service.model.*;
import com.dell.cpsd.paqx.dne.service.task.handler.BaseTaskHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries.  All Rights Reserved.
 * Dell EMC Confidential/Proprietary Information
 * </p>
 *
 * @since 1.0
 */

public class ConfigIdracTaskHandler extends BaseTaskHandler implements IWorkflowTaskHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigIdracTaskHandler.class);

    private NodeService nodeService;

    public ConfigIdracTaskHandler(NodeService nodeService){
        this.nodeService = nodeService;
    }

    @Override
    public boolean executeTask(Job job) {
        LOGGER.info("Execute ConfigIdracTaskHandler task");
        IdracNetworkSettingsResponseInfo response = initializeResponse(job);

        String nodeId = ((FirstAvailableDiscoveredNodeResponse)job.getTaskResponseMap().get("findAvailableNodes")).getNodeInfo().getNodeId();
        String ipAddress = job.getInputParams().getIdracIpAddress();
        String gatewayIpAddress = job.getInputParams().getIdracGatewayIpAddress();
        String subnetMask = job.getInputParams().getIdracSubnetMask();

        LOGGER.info("Idrac input parameters are:");
        LOGGER.info("NodeId:" + nodeId);
        LOGGER.info("Idrac IP Address:" + ipAddress);
        LOGGER.info("Idrac Gateway Address:" + gatewayIpAddress);
        LOGGER.info("Idrac Subnet Mask:" + subnetMask);

        try {
            IdracNetworkSettingsRequest idracNetworkSettingsRequest = new IdracNetworkSettingsRequest();
            idracNetworkSettingsRequest.setNodeId(nodeId);
            idracNetworkSettingsRequest.setIdracIpAddress(ipAddress);
            idracNetworkSettingsRequest.setIdracGatewayIpAddress(gatewayIpAddress);
            idracNetworkSettingsRequest.setIdracSubnetMask(subnetMask);

            IdracInfo idracInfo = nodeService.idracNetworkSettings(idracNetworkSettingsRequest);
            if(idracInfo != null) {
              response.setIdracInfo(idracInfo);
              response.setWorkFlowTaskStatus(Status.SUCCEEDED);

              return true;
          }
        }
        catch(Exception e) {
                LOGGER.info("", e);
            response.setWorkFlowTaskStatus(Status.FAILED);
            response.addError(e.toString());
        }
        return false;

    }

    @Override
    public IdracNetworkSettingsResponseInfo initializeResponse(Job job){
        IdracNetworkSettingsResponseInfo response = new IdracNetworkSettingsResponseInfo();
        response.setWorkFlowTaskName(job.getCurrentTask().getTaskName());
        response.setWorkFlowTaskStatus(Status.IN_PROGRESS);
        job.addTaskResponse(job.getStep(), response);
        return response;
    }
}
