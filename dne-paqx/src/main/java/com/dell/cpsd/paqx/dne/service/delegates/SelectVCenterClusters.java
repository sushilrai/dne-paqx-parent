/**
 * <p>
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries. All Rights Reserved. Dell EMC Confidential/Proprietary Information
 * </p>
 */

package com.dell.cpsd.paqx.dne.service.delegates;

import com.dell.cpsd.paqx.dne.service.NodeService;
import com.dell.cpsd.paqx.dne.service.delegates.model.NodeDetail;
import com.dell.cpsd.virtualization.capabilities.api.ClusterInfo;
import com.dell.cpsd.virtualization.capabilities.api.ValidateVcenterClusterResponseMessage;
import org.apache.commons.collections.CollectionUtils;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.dell.cpsd.paqx.dne.service.delegates.utils.DelegateConstants.FIND_VCLUSTER_FAILED;
import static com.dell.cpsd.paqx.dne.service.delegates.utils.DelegateConstants.NODE_DETAILS;

@Component
@Scope("prototype")
@Qualifier("selectVCenterClusters")
public class SelectVCenterClusters extends BaseWorkflowDelegate
{

    private static final Logger LOGGER = LoggerFactory.getLogger(SelectVCenterClusters.class);

    private NodeService nodeService;

    @Autowired
    public SelectVCenterClusters(NodeService nodeService)
    {
        super(LOGGER, "Select vCenter Clusters");
        this.nodeService = nodeService;
    }

    @Override
    public void delegateExecute(final DelegateExecution delegateExecution)
    {
        List<NodeDetail> nodeDetails = (List<NodeDetail>) delegateExecution.getVariable(NODE_DETAILS);
        ValidateVcenterClusterResponseMessage responseMsg = null;
        List<String> nodeIds = new ArrayList<>();
        try
        {
            List<ClusterInfo> clusterInfos = nodeService.listClusters();
            for (NodeDetail nd : nodeDetails)
            {
                nodeIds.add(nd.getServiceTag());
            }
            responseMsg = nodeService.validateClusters(clusterInfos, nodeIds);
        }
        catch (Exception e)
        {
            final String message = "An unexpected Exception occurred while retrieving the list of Clusters for selection. Reason: ";
            updateDelegateStatus(message, e );
            throw new BpmnError(FIND_VCLUSTER_FAILED, message + e.getMessage());
        }
        Map<String, String> clusterMap = null;
        if (responseMsg != null && responseMsg.getClusters() != null)
        {
            clusterMap = responseMsg.getClusters();
        }
        if (clusterMap == null)
        {
            final StringBuilder messageBuilder = new StringBuilder("Selecting vCenter Clusters Failed.");
            if (responseMsg != null && CollectionUtils.isNotEmpty(responseMsg.getFailedCluster()))
            {
                messageBuilder.append(" Reason: ");
                responseMsg.getFailedCluster().forEach(failed -> {
                    messageBuilder.append(failed + " ");
                });
                final String message = messageBuilder.toString();
                updateDelegateStatus(message);
                throw new BpmnError(FIND_VCLUSTER_FAILED, message);
            }
        }
        for (NodeDetail nd : nodeDetails)
        {
            updateDelegateStatus("Selecting vCenter Cluster for Node " + nd.getServiceTag() + ".");
            String clusterName = clusterMap.get(nd.getServiceTag());
            if (clusterName == null)
            {
                final String message = "Selecting vCenter Cluster for Node " + nd.getServiceTag() + " Failed.";
                updateDelegateStatus(message);
                throw new BpmnError(FIND_VCLUSTER_FAILED, message);
            }
            nd.setClusterName(clusterName);
            updateDelegateStatus("VCenter Cluster " + clusterName + " was selected for Node " +
                                 nd.getServiceTag() + ".");
        }
        delegateExecution.setVariable(NODE_DETAILS, nodeDetails);
    }
}
