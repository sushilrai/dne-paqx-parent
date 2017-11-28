/**
 * <p>
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries. All Rights Reserved. Dell EMC Confidential/Proprietary Information
 * </p>
 */

package com.dell.cpsd.paqx.dne.service.delegates;

import com.dell.cpsd.paqx.dne.service.NodeService;
import com.dell.cpsd.paqx.dne.service.delegates.model.NodeDetail;
import com.dell.cpsd.virtualization.capabilities.api.Cluster;
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

import java.util.*;

import static com.dell.cpsd.paqx.dne.service.delegates.utils.DelegateConstants.*;

@Component
@Scope("prototype")
@Qualifier("findVCenterCluster")
public class FindVCenterCluster extends BaseWorkflowDelegate
{

    private static final Logger LOGGER = LoggerFactory.getLogger(FindVCenterCluster.class);

    private NodeService nodeService;

    @Autowired
    public FindVCenterCluster(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    @Override
    public void delegateExecute(final DelegateExecution delegateExecution)
    {
        LOGGER.info("Execute FindVCenterCluster");
        List<NodeDetail> nodeDetail = (List<NodeDetail>) delegateExecution.getVariable(NODE_DETAILS);
        ValidateVcenterClusterResponseMessage responseMsg = null;
        List<ClusterInfo> clusterInfos = null;
        List<String> nodeIds = new ArrayList<>();
        try
        {
            clusterInfos =  (List<ClusterInfo>) delegateExecution.getVariable(CLUSTER_INFO_DETAILS);
            if (clusterInfos == null) {
                clusterInfos = nodeService.listClusters();
            }
            for (NodeDetail nd:nodeDetail)
            {
                nodeIds.add(nd.getServiceTag());
            }
            responseMsg = nodeService.validateClusters(clusterInfos, nodeIds);
        }
        catch (Exception e)
        {
            final String message = "An unexpected Exception occurred while retrieving the list of Clusters for selection.";
            LOGGER.error(message, e);
            updateDelegateStatus(message + "  Reason: " + e.getMessage());
            throw new BpmnError(FIND_VCLUSTER_FAILED, message + "  Reason: " + e.getMessage());
        }
        for(NodeDetail nd : nodeDetail) {
            updateDelegateStatus("Selecting VCenter Cluster for Node " + nd.getServiceTag());
            Map<String, String> clusterMap = new HashMap<>();
            if (responseMsg != null && responseMsg.getClusters() != null) {
                clusterMap = responseMsg.getClusters();
            }
            if (clusterMap == null) {
                final String message[] = {"Selecting VCenter Cluster Failed for Node " + nd.getServiceTag() + "."};
                if (responseMsg != null && CollectionUtils.isNotEmpty(responseMsg.getFailedCluster())) {
                    message[0] += " Reason: ";
                    responseMsg.getFailedCluster().forEach(failed -> {
                        message[0] += failed + " ";
                    });
                    LOGGER.error(message[0]);
                    updateDelegateStatus(message[0]);
                    throw new BpmnError(FIND_VCLUSTER_FAILED, message[0]);
                }
            }
            String clusterName = clusterMap.get(nd.getServiceTag());
            nd.setClusterName(clusterName);
            delegateExecution.setVariable(NODE_DETAILS, nodeDetail);
            final String finalClusterName = clusterName;
            Optional<ClusterInfo> updateCluster = clusterInfos.stream().filter(cluster -> finalClusterName
                    .equals(cluster.getName())).findFirst();
            if (updateCluster.isPresent()) {
                ClusterInfo cluster = updateCluster.get();
                cluster.setNumberOfHosts(cluster.getNumberOfHosts() + 1);
                delegateExecution.setVariable(CLUSTER_INFO_DETAILS, clusterInfos);
            }
            final String message = "VCenter Cluster " + clusterMap.get(nd.getServiceTag()) + " was selected for Node " + nd.getServiceTag();
            updateDelegateStatus(message);
            LOGGER.info(message);
        }
    }
}
