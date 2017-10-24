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

public class IdracNetworkSettingsTaskResponse extends TaskResponse
{
    private IdracInfo idracInfo;

    public IdracInfo getIdracInfo()
    {
        return idracInfo;
    }

    public void setIdracInfo(IdracInfo idracInfo)
    {
        this.idracInfo = idracInfo;
    }
}
