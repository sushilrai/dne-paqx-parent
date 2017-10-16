package com.dell.cpsd.paqx.dne.service.model;

import com.dell.cpsd.service.engineering.standards.DeviceAssignment;

import java.util.Map;

public class FindScaleIOResponse extends TaskResponse
{

    private Map<String, DeviceAssignment> deviceToStoragePoolMap;

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
}
