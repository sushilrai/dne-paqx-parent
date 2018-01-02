/**
 * <p>
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries. All Rights Reserved. Dell EMC Confidential/Proprietary Information
 * </p>
 */
package com.dell.cpsd.paqx.dne.service.model;

/**
 * IPV4 Configuration for Dell Node
 * <p>
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries. All Rights Reserved.
 * Dell EMC Confidential/Proprietary Information
 * </p>
 *
 * @version 1.0
 * @since 1.0
 */
public class IpV4Configuration
{
    private String esxiManagementIpAddress;

    private String esxiManagementGateway;

    private String esxiManagementNetworkMask;

    private String storageOnlyManagementIpAddress;
    private String storageOnlyManagementNetworkMask;
    private String storageOnlyManagementGateway;
    private Integer storageOnlyManagementVlan;

    public String getEsxiManagementIpAddress()
    {
        return esxiManagementIpAddress;
    }

    public void setEsxiManagementIpAddress(final String esxiManagementIpAddress)
    {
        this.esxiManagementIpAddress = esxiManagementIpAddress;
    }

    public String getEsxiManagementGateway()
    {
        return esxiManagementGateway;
    }

    public void setEsxiManagementGateway(final String esxiManagementGateway)
    {
        this.esxiManagementGateway = esxiManagementGateway;
    }

    public String getEsxiManagementNetworkMask()
    {
        return esxiManagementNetworkMask;
    }

    public void setEsxiManagementNetworkMask(final String esxiManagementNetworkMask)
    {
        this.esxiManagementNetworkMask = esxiManagementNetworkMask;
    }

    public String getStorageOnlyManagementIpAddress()
    {
        return storageOnlyManagementIpAddress;
    }

    public String getStorageOnlyManagementNetworkMask() {
        return storageOnlyManagementNetworkMask;
    }

    public String getStorageOnlyManagementGateway() {
        return storageOnlyManagementGateway;
    }

    public void setStorageOnlyManagementIpAddress(final String storageOnlyManagementIpAddress) {
        this.storageOnlyManagementIpAddress = storageOnlyManagementIpAddress;
    }

    public void setStorageOnlyManagementNetworkMask(final String storageOnlyManagementNetworkMask) {
        this.storageOnlyManagementNetworkMask = storageOnlyManagementNetworkMask;
    }

    public void setStorageOnlyManagementGateway(final String storageOnlyManagementGateway) {
        this.storageOnlyManagementGateway = storageOnlyManagementGateway;
    }

    public Integer getStorageOnlyManagementVlan() {
        return storageOnlyManagementVlan;
    }

    public void setStorageOnlyManagementVlan(final Integer storageOnlyManagementVlan) {
        this.storageOnlyManagementVlan = storageOnlyManagementVlan;
    }
}
