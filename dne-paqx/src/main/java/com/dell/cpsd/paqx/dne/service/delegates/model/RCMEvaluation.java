/**
 * <p>
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries. All Rights Reserved. Dell EMC Confidential/Proprietary Information
 * </p>
 */

package com.dell.cpsd.paqx.dne.service.delegates.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Request object for rcm evaluation.
 * <p>
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries. All Rights Reserved.
 * Dell EMC Confidential/Proprietary Information
 * </p>
 *
 * @version 1.0
 * @since 1.0
 */
public class RCMEvaluation
{
    @JsonProperty("rcmUuid")
    private String rcmUuid;

    @JsonProperty("deviceUuids")
    private List<String> deviceUuids;

    @JsonProperty("subComponentTypes")
    private List<String> subComponentTypes;

    @JsonProperty("timeout")
    private int timeout;

    public String getRcmUuid()
    {
        return rcmUuid;
    }

    public void setRcmUuid(final String rcmUuid)
    {
        this.rcmUuid = rcmUuid;
    }

    public List<String> getDeviceUuids()
    {
        return deviceUuids;
    }

    public void setDeviceUuids(final List<String> deviceUuids)
    {
        this.deviceUuids = deviceUuids;
    }

    public List<String> getSubComponentTypes()
    {
        return subComponentTypes;
    }

    public void setSubComponentTypes(final List<String> subComponentTypes)
    {
        this.subComponentTypes = subComponentTypes;
    }

    public int getTimeout()
    {
        return timeout;
    }

    public void setTimeout(final int timeout)
    {
        this.timeout = timeout;
    }
}
