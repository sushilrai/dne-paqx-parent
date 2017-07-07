/**
 * <p>
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries. All Rights Reserved. Dell EMC Confidential/Proprietary Information
 * </p>
 */

package com.dell.cpsd.paqx.dne.service.task.handler.addnode;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dell.cpsd.paqx.dne.domain.IWorkflowTaskHandler;
import com.dell.cpsd.paqx.dne.domain.Job;
import com.dell.cpsd.paqx.dne.service.NodeService;
import com.dell.cpsd.paqx.dne.service.model.FirstAvailableDiscoveredNodeResponse;
import com.dell.cpsd.paqx.dne.service.model.NodeInfo;
import com.dell.cpsd.paqx.dne.service.model.Status;
import com.dell.cpsd.paqx.dne.service.model.TaskResponse;
import com.dell.cpsd.paqx.dne.service.task.handler.BaseTaskHandler;

/**
 * Task responsible for notifying the node discovery service that node
 * allocation has completed and to update its status.
 * 
 * <p>
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries. All Rights Reserved. Dell EMC Confidential/Proprietary Information
 * </p>
 * 
 * @since 1.0
 */
public class NotifyNodeDiscoveryToUpdateStatusTaskHandler extends BaseTaskHandler implements IWorkflowTaskHandler
{
    /*
     * The logger instance
     */
    private static final Logger LOGGER = 
            LoggerFactory.getLogger(NotifyNodeDiscoveryToUpdateStatusTaskHandler.class);

    /*
     * The <code>NodeService</code> instance
     */
    private final NodeService nodeService;
    
    /**
     * NotifyNodeDiscoveryToUpdateStatusTaskHandler constructor.
     * 
     * @param nodeService
     *            - The <code>NodeService</code> instance.
     * 
     * @since 1.0
     */
    public NotifyNodeDiscoveryToUpdateStatusTaskHandler(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    /**
     * Perform the task of notifying the node discovery service that node
     * allocation has completed and to update its status.
     * 
     * @param job - The <code>Job</code> this task is part of.
     * 
     * @since   1.0
     */
    @Override
    public boolean executeTask(Job job)
    {
        LOGGER.info("Execute NotifyNodeDiscoveryToUpdateStatusTaskHandler task");

        TaskResponse response = initializeResponse(job);

        try
        {
            Map<String, TaskResponse> responseMap = job.getTaskResponseMap();
            FirstAvailableDiscoveredNodeResponse findNodeTask = (FirstAvailableDiscoveredNodeResponse)responseMap.get("findAvailableNodes");
            if (findNodeTask == null)
            {
                throw new IllegalStateException("No discovered node task found.");
            }
            
            NodeInfo nodeInfo = findNodeTask.getNodeInfo();
            if (nodeInfo == null)
            {
                throw new IllegalStateException("No discovered node info found.");
            }
            
            boolean result = this.nodeService.notifyNodeAllocationComplete(nodeInfo.getSymphonyUuid());
            
            if (result)
            {
                response.setWorkFlowTaskStatus(Status.SUCCEEDED);
                return true;
            }
        }
        catch (Exception e)
        {
            LOGGER.error("Error notifying node discovery service", e);
            response.addError(e.toString());
        }

        response.setWorkFlowTaskStatus(Status.FAILED);
        return false;
    }
}
