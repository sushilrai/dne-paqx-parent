/**
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries.  All Rights Reserved.
 */

package com.dell.cpsd.paqx.dne.service.model;

public class VirtualizationCluster
{
    private String name;
    private Integer numberOfHosts;

    public VirtualizationCluster(String name, Integer numberOfHosts)
    {
        this.name = name;
        this.numberOfHosts = numberOfHosts;
    }

    public String getName()
    {
        return name;
    }

    public Integer getNumberOfHosts()
    {
        return numberOfHosts;
    }
}
