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
public class DiscoveredNode
{
    private final String convergedUuid;
    private final String nodeId;

    public DiscoveredNode(String convergedUuid, String nodeId)
    {
        this.convergedUuid = convergedUuid;
        this.nodeId = nodeId;
    }

    public String getConvergedUuid()
    {
        return convergedUuid;
    }

    public String getNodeId()
    {
        return nodeId;
    }
}
