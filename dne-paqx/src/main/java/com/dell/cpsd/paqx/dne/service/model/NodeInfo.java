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

    public NodeInfo(String symphonyUuid, String nodeId) {
        this.symphonyUuid = symphonyUuid;
        this.nodeId = nodeId;
    }

    public String getSymphonyUuid() {
        return symphonyUuid;
    }

    public String getNodeId() {
        return nodeId;
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
        
        builder.append("}");
        
        return builder.toString();
    }
}
