/**
 * <p>
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries.  All Rights Reserved.
 * Dell EMC Confidential/Proprietary Information
 * </p>
 */

package com.dell.cpsd.paqx.dne.service.model;

import com.dell.cpsd.virtualization.capabilities.api.ClusterInfo;

import java.util.List;

public class VClusterTaskResponse extends TaskResponse {
    private List<ClusterInfo> clusterInfo;

    public List<ClusterInfo> getClusterInfo(){ return clusterInfo; }
    public void setClusterInfo(List<ClusterInfo> list) { clusterInfo = list; }
}
