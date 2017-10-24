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

public class ConfigureBootDeviceIdracTaskResponse extends TaskResponse
{
    private BootDeviceIdracStatus bootOrderStatus;

    public BootDeviceIdracStatus getBootOrderStatus()
    {
        return bootOrderStatus;
    }

    public void setBootOrderStatus(BootDeviceIdracStatus bootOrderStatus)
    {
        this.bootOrderStatus = bootOrderStatus;
    }
}
