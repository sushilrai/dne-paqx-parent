/**
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries.  All Rights Reserved.
 * Dell EMC Confidential/Proprietary Information
 */
package com.dell.cpsd.paqx.dne.service.model;

/**
 *
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries.  All Rights Reserved.
 * Dell EMC Confidential/Proprietary Information
 *
 * @author Connor Goulding
 */
public class NodeInfo {

    private String symphonyUuid;
    private String nodeId;
    private NodeStatus nodeStatus;

    public NodeInfo(String symphonyUuid, String nodeId, NodeStatus nodeStatus) {
        this.nodeId = nodeId;
        this.nodeStatus = nodeStatus;
        this.symphonyUuid = symphonyUuid;
    }

    public String getSymphonyUuid() {
        return symphonyUuid;
    }

    public String getNodeId() {
        return nodeId;
    }

    public NodeStatus getNodeStatus() {
        return nodeStatus;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() 
    {
        final StringBuilder builder = new StringBuilder();
        
        builder.append("NodeInfo {");
        
        builder.append("symphonyUuid=").append(this.symphonyUuid);
        builder.append(", nodeId=").append(this.nodeId);
        builder.append(", nodeStatus=").append(this.nodeStatus);
        
        builder.append("}");
        
        return builder.toString();
    }
}
