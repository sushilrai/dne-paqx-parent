/**
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries.
 * All Rights Reserved. Dell EMC Confidential/Proprietary Information
 */

package com.dell.cpsd.paqx.dne.service.model;

/**
 * Find Protection Domain Task Response
 * <p>
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries. All Rights Reserved.
 * Dell EMC Confidential/Proprietary Information
 * </p>
 *
 * @version 1.0
 * @since 1.0
 */
public class FindProtectionDomainTaskResponse extends TaskResponse
{
    private String protectionDomainId;

    private String protectionDomainName;

    public String getProtectionDomainId()
    {
        return protectionDomainId;
    }

    public void setProtectionDomainId(final String protectionDomainId)
    {
        this.protectionDomainId = protectionDomainId;
    }

    public String getProtectionDomainName()
    {
        return protectionDomainName;
    }

    public void setProtectionDomainName(final String protectionDomainName)
    {
        this.protectionDomainName = protectionDomainName;
    }
}
