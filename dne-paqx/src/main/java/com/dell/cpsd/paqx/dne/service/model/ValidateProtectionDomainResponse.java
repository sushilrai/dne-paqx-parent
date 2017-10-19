/**
 * <p>
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries. All Rights Reserved. Dell EMC Confidential/Proprietary Information
 * </p>
 */

package com.dell.cpsd.paqx.dne.service.model;

/**
 * <p>
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries. All Rights Reserved. Dell EMC Confidential/Proprietary Information
 * </p>
 *
 * @version 1.0
 * @since 1.0
 */
public class ValidateProtectionDomainResponse extends TaskResponse
{

    private String protectionDomainName;
    private String protectionDomainId;

    public ValidateProtectionDomainResponse()
    {
    }

    public ValidateProtectionDomainResponse(String protectionDomainName, String protectionDomainId)
    {
        this.protectionDomainName = protectionDomainName;
        this.protectionDomainId = protectionDomainId;
    }

    public String getProtectionDomainName()
    {
        return protectionDomainName;
    }

    public void setProtectionDomainName(String protectionDomainName)
    {
        this.protectionDomainName = protectionDomainName;
    }

    public String getProtectionDomainId()
    {
        return protectionDomainId;
    }

    public void setProtectionDomainId(final String protectionDomainId)
    {
        this.protectionDomainId = protectionDomainId;
    }

    @Override
    public String toString()
    {
        return "ProtectionDomain{" + "protectionDomainName='" + protectionDomainName + '\'' + ", protectionDomainId='" + protectionDomainId
                + '\'' + '}';
    }
}
