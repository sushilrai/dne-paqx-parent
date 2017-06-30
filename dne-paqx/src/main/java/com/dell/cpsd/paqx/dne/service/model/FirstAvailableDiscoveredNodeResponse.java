/**
 * <p>
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries.  All Rights Reserved.
 * Dell EMC Confidential/Proprietary Information
 * </p>
 */

package com.dell.cpsd.paqx.dne.service.model;

/**
 * <p>
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries.  All Rights Reserved.
 * Dell EMC Confidential/Proprietary Information
 * </p>
 *
 * @since 1.0
 */

public class FirstAvailableDiscoveredNodeResponse extends TaskResponse {
    private NodeInfo nodeInfo;

    public NodeInfo getNodeInfo(){ return nodeInfo; }
    public void setNodeInfo(NodeInfo nodeInfo) { this.nodeInfo = nodeInfo; }
}
