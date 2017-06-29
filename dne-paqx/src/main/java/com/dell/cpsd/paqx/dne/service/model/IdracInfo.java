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

public class IdracInfo {
    private String idracIpAddress;
    private String idracGatewayIpAddress;
    private String idracSubnetMask;
    private String nodeId;
    private String message;

    public IdracInfo() {
    }

    public IdracInfo(String nodeId, String idracIpAddress, String idracGatewayIpAddress, String idracSubnetMask, String message) {
        this.idracIpAddress = idracIpAddress;
        this.idracGatewayIpAddress = idracGatewayIpAddress;
        this.idracSubnetMask = idracSubnetMask;
        this.nodeId = nodeId;
        this.message = message;
    }

    public String getIdracIpAddress() {
        return idracIpAddress;
    }

    public String getIdracGatewayIpAddress() {
        return idracGatewayIpAddress;
    }

    public String getIdracSubnetMask() {
        return idracSubnetMask;
    }

    public String getNodeId() {
        return nodeId;
    }

    public void setIdracIpAddress(String idracIpAddress) {
        this.idracIpAddress = idracIpAddress;
    }

    public void setIdracGatewayIpAddress(String idracGatewayIpAddress) {this.idracGatewayIpAddress = idracGatewayIpAddress;}

    public void setIdracSubnetMask(String idracSubnetMask) {
        this.idracSubnetMask = idracSubnetMask;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();

        builder.append("IdracInfo {");
        builder.append("idracIpAddress=").append(this.idracIpAddress);
        builder.append(", idracGatewayIpAddress").append(this.idracGatewayIpAddress);
        builder.append(", idracSubnetMaskMask").append(this.idracSubnetMask);
        builder.append(", nodeId").append(this.nodeId);
        builder.append(", message").append(this.message);
        builder.append("}");

        return builder.toString();
    }
}
