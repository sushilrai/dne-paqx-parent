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
public class NodeExpansionRequest {

    private String idracIpAddress;
    private String idracGatewayIpAddress;
    private String idracSubnetMask;

    private String managementIpAddress;
    private String esxiKernelIpAddress1;
    private String esxiKernelIpAddress2;
    private String scaleIOSVMDataIpAddress1;
    private String scaleIOSVMDataIpAddress2;
    private String scaleIOSVMManagementIpAddress;

    public NodeExpansionRequest() {
    }

    public NodeExpansionRequest(String idracIpAddress, String idracGatewayIpAddress, String idracSubnetMask, String managementIpAddress, String esxiKernelIpAddress1, String esxiKernelIpAddress2, String scaleIOSVMDataIpAddress1, String scaleIOSVMDataIpAddress2, String scaleIOSVMManagementIpAddress) {
        this.idracIpAddress = idracIpAddress;
        this.idracGatewayIpAddress = idracGatewayIpAddress;
        this.idracSubnetMask = idracSubnetMask;
        this.managementIpAddress = managementIpAddress;
        this.esxiKernelIpAddress1 = esxiKernelIpAddress1;
        this.esxiKernelIpAddress2 = esxiKernelIpAddress2;
        this.scaleIOSVMDataIpAddress1 = scaleIOSVMDataIpAddress1;
        this.scaleIOSVMDataIpAddress2 = scaleIOSVMDataIpAddress2;
        this.scaleIOSVMManagementIpAddress = scaleIOSVMManagementIpAddress;
    }
    public String getIdracIpAddress() {
        return idracIpAddress;
    }

    public void setIdracIpAddress(String idracIpAddress) {
        this.idracIpAddress = idracIpAddress;
    }

    public String getIdracSubnetMask() {
        return idracSubnetMask;
    }

    public void setIdracSubnetMask(String idracSubnetMask) {
        this.idracSubnetMask = idracSubnetMask;
    }

    public String getIdracGatewayIpAddress() {
        return idracGatewayIpAddress;
    }

    public void setIdracGatewayIpAddress(String idracGatewayIpAddress) {
        this.idracGatewayIpAddress = idracGatewayIpAddress;
    }

    public String getManagementIpAddress() {
        return managementIpAddress;
    }

    public void setManagementIpAddress(String managementIpAddress) {
        this.managementIpAddress = managementIpAddress;
    }

    public String getEsxiKernelIpAddress1() {
        return esxiKernelIpAddress1;
    }

    public void setEsxiKernelIpAddress1(String esxiKernelIpAddress) {
        this.esxiKernelIpAddress1 = esxiKernelIpAddress;
    }

    public String getEsxiKernelIpAddress2() {
        return esxiKernelIpAddress2;
    }

    public void setEsxiKernelIpAddress2(String esxiKernelIpAddress) {
        this.esxiKernelIpAddress2 = esxiKernelIpAddress;
    }

    public String getScaleIOSVMDataIpAddress1() {
        return scaleIOSVMDataIpAddress1;
    }

    public void setScaleIOSVMDataIpAddress1(String scaleIOSVMDataIpAddress) {
        this.scaleIOSVMDataIpAddress1 = scaleIOSVMDataIpAddress;
    }

    public String getScaleIOSVMDataIpAddress2() {
        return scaleIOSVMDataIpAddress2;
    }

    public void setScaleIOSVMDataIpAddress2(String scaleIOSVMDataIpAddress) {
        this.scaleIOSVMDataIpAddress2 = scaleIOSVMDataIpAddress;
    }

    public String getScaleIOSVMManagementIpAddress() {
        return scaleIOSVMManagementIpAddress;
    }

    public void setScaleIOSVMManagementIpAddress(String scaleIOSVMManagementIpAddress) {
        this.scaleIOSVMManagementIpAddress = scaleIOSVMManagementIpAddress;
    }
}
