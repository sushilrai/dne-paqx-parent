/**
 * <p>
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries.  All Rights Reserved.
 * Dell EMC Confidential/Proprietary Information
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

        try {
//            String nodeId = ((FirstAvailableDiscoveredNodeResponse)job.getTaskResponseMap().get("findAvailableNodes")).getNodeInfo().getNodeId();
            String nodeId = "";
            if (job.getTaskResponseMap() != null) {
                FirstAvailableDiscoveredNodeResponse taskResponse = (FirstAvailableDiscoveredNodeResponse)job.getTaskResponseMap().get("findAvailableNodes");
                if (taskResponse != null && taskResponse.getNodeInfo() != null && taskResponse.getNodeInfo().getNodeId() != null) {
                    nodeId = taskResponse.getNodeInfo().getNodeId();
                }
                else
                {
                    LOGGER.info("Node id not found in the task response map.");
                    response.setWorkFlowTaskStatus(Status.FAILED);
                    response.addError("Node id not found in the task response.");
                    return false;
                }
            }
            else
            {
                LOGGER.info("Node id not found in the task response.");
                response.setWorkFlowTaskStatus(Status.FAILED);
                response.addError("Node id not found in the task response.");
                return false;
            }

            String ipAddress = job.getInputParams().getIdracIpAddress();
            String gatewayIpAddress = job.getInputParams().getIdracGatewayIpAddress();
            String subnetMask = job.getInputParams().getIdracSubnetMask();

            LOGGER.info("NodeId:" + nodeId);
            LOGGER.info("Idrac input request parameters: " + job.getInputParams().toString());


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
            LOGGER.error("Error configuring idrac network settings: ", e);
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
