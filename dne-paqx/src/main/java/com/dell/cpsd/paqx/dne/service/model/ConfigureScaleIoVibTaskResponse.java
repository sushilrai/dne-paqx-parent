/**
 * <p>
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries. All Rights Reserved. Dell EMC Confidential/Proprietary Information
 * </p>
 */
package com.dell.cpsd.paqx.dne.service.model;

/**
 * TODO: Document Usage
 * <p/>
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries. All Rights Reserved.
 * Dell EMC Confidential/Proprietary Information
 * <p/>
 *
 * @version 1.0
 * @since 1.0
 */
public class ConfigureScaleIoVibTaskResponse extends TaskResponse
{
    private String ioctlIniGuidStr;

    public String getIoctlIniGuidStr()
    {
        return this.ioctlIniGuidStr;
    }

    public void setIoctlIniGuidStr(final String ioctlIniGuidStr)
    {
        this.ioctlIniGuidStr = ioctlIniGuidStr;
    }
}
