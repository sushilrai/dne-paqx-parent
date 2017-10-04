/**
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries.  All Rights Reserved.
 * Dell EMC Confidential/Proprietary Information
 */

package com.dell.cpsd.paqx.dne.service.model;

import java.util.ArrayList;
import java.util.List;

import com.dell.cpsd.service.system.definition.api.Definition;
import com.dell.cpsd.service.system.definition.api.Identity;

/**
 * <p>
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries.  All Rights Reserved.
 * Dell EMC Confidential/Proprietary Information
 * </p>
 *
 * @since 1.0
*/
public class NodeInfo
{
    private String       symphonyUuid;
    private NodeStatus   nodeStatus;
    private String       serialNumber;

    public NodeInfo(String symphonyUuid, NodeStatus nodeStatus, String serialNumber)
    {
        this.nodeStatus = nodeStatus;
        this.symphonyUuid = symphonyUuid;
        this.serialNumber = serialNumber;
    }
    public NodeInfo(String symphonyUuid, NodeStatus nodeStatus)
    {
        this.nodeStatus = nodeStatus;
        this.symphonyUuid = symphonyUuid;
        this.serialNumber = "";
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public String getSymphonyUuid()
    {
        return symphonyUuid;
    }

    public NodeStatus getNodeStatus()
    {
        return nodeStatus;
    }

    @Override
    public String toString() {
        return "NodeInfo{" +
                "nodeStatus=" + nodeStatus +
                ", symphonyUuid='" + symphonyUuid + '\'' +
                ", serialNumber='" + serialNumber + '\'' +
                '}';
    }
}
