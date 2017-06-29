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

public class IdracNetworkSettingsRequest {
    private String idracIpAddress           ;
    private String idracGatewayIpAddress    ;
    private String idracSubnetMask          ;
    private String nodeId                   ;

    public IdracNetworkSettingsRequest(){}

    public IdracNetworkSettingsRequest(String nodeId, String idracIpAddress, String idracGatewayIpAddress, String idracSubnetMask){
        this.idracIpAddress         = idracIpAddress;
        this.idracGatewayIpAddress  = idracGatewayIpAddress;
        this.idracSubnetMask        = idracSubnetMask;
        this.nodeId                 = nodeId;
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


}

