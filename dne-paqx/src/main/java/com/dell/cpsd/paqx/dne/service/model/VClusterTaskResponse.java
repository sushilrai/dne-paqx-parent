package com.dell.cpsd.paqx.dne.service.model;

import com.dell.cpsd.virtualization.capabilities.api.ClusterInfo;

import java.util.List;

/**
 * Created by madenb on 5/3/2017.
 */
public class VClusterTaskResponse extends TaskResponse {
    private List<ClusterInfo> clusterInfo;

    public List<ClusterInfo> getClusterInfo(){ return clusterInfo; }
    public void setClusterInfo(List<ClusterInfo> list) { clusterInfo = list; }
}
