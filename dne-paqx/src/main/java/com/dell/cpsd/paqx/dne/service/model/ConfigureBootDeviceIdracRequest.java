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

public class ConfigureBootDeviceIdracRequest {
    private String nodeId;
    private String idracIpAddress;

    public ConfigureBootDeviceIdracRequest(){}

    public ConfigureBootDeviceIdracRequest(String nodeId, String idracIpAddress){
        this.nodeId = nodeId;
        this.idracIpAddress = idracIpAddress;
    }

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    public String getIdracIpAddress() {
        return idracIpAddress;
    }

    public void setIdracIpAddress(String idracIpAddress) {
        this.idracIpAddress = idracIpAddress;
    }

    @Override
    public String toString(){
        final StringBuilder builder = new StringBuilder();

        builder.append("BootOrderSequence {");
        builder.append("nodeId=").append(this.nodeId);
        builder.append("idracIpAddress=").append(this.idracIpAddress);
        builder.append("}");

        return builder.toString();
    }
}
