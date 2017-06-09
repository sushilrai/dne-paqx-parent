/**
 * <p>
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries.  All Rights Reserved.
 * Dell EMC Confidential/Proprietary Information
 * </p>
 */

package com.dell.cpsd.paqx.dne.service.model;

import java.util.List;

public class DiscoveredNodesResponse extends TaskResponse {
    private List<NodeInfo> nodesInfo;

    public List<NodeInfo> getNodesInfo(){ return nodesInfo; }
    public void setNodesInfo(List<NodeInfo> list) { nodesInfo = list; }
}
