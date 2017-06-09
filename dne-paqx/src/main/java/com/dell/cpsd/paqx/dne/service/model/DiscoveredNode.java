/**
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries.  All Rights Reserved.
 * Dell EMC Confidential/Proprietary Information
 */

package com.dell.cpsd.paqx.dne.service.model;

public class DiscoveredNode
{
    private final String convergedUuid;
    private final String nodeId;
    private final com.dell.converged.capabilities.compute.discovered.nodes.api.DiscoveredNode.AllocationStatus nodeStatus;

    public DiscoveredNode(String convergedUuid, String nodeId, com.dell.converged.capabilities.compute.discovered.nodes.api.DiscoveredNode.AllocationStatus nodeStatus)
    {
        this.convergedUuid = convergedUuid;
        this.nodeId = nodeId;
        this.nodeStatus = nodeStatus;
    }

    public String getConvergedUuid()
    {
        return convergedUuid;
    }

    public String getNodeId()
    {
        return nodeId;
    }

    public com.dell.converged.capabilities.compute.discovered.nodes.api.DiscoveredNode.AllocationStatus getNodeStatus() {
        return nodeStatus;
    }
}
