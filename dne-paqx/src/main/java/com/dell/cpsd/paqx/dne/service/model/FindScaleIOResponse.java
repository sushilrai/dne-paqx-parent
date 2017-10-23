/**
 * <p>
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries. All Rights Reserved. Dell EMC Confidential/Proprietary Information
 * </p>
 */

package com.dell.cpsd.paqx.dne.service.model;

import com.dell.cpsd.service.engineering.standards.DeviceAssignment;

import java.util.Map;

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
public class FindScaleIOResponse extends TaskResponse
{
    private Map<String, DeviceAssignment> deviceToStoragePoolMap;
    private boolean isExistingValidStoragePool;

    public FindScaleIOResponse()
    {

    }

    public Map<String, DeviceAssignment> getDeviceToStoragePoolMap()
    {
        return deviceToStoragePoolMap;
    }

    public void setDeviceToStoragePoolMap(final Map<String, DeviceAssignment> deviceToStoragePoolMap)
    {
        this.deviceToStoragePoolMap = deviceToStoragePoolMap;
    }

    public boolean isExistingValidStoragePool()
    {
        return isExistingValidStoragePool;
    }

    public void setExistingValidStoragePool(final boolean existingValidStoragePool)
    {
        isExistingValidStoragePool = existingValidStoragePool;
    }
}
