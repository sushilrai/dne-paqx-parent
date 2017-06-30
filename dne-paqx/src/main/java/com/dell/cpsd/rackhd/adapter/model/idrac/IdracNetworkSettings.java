/**
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries.  All Rights Reserved.
 * Dell EMC Confidential/Proprietary Information
 */

package com.dell.cpsd.rackhd.adapter.model.idrac;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import javax.validation.constraints.NotNull;

/**
 * <p>
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries.  All Rights Reserved.
 * Dell EMC Confidential/Proprietary Information
 * </p>
 *
 * @since 1.0
 */

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"nodeId", "ipAddress", "netmask", "gateway"})
public class IdracNetworkSettings {

    @JsonProperty("nodeId")
    @NotNull
    private String nodeId;

    @JsonProperty("ipAddress")
    @NotNull
    private String ipAddress;

    @JsonProperty("netmask")
    @NotNull
    private String netmask;

    @JsonProperty("gateway")
    @NotNull
    private String gateway;

    public IdracNetworkSettings()
    {

    }

    /**
     *
     * @param nodeId
     * @param ipAddress
     * @param netmask
     * @param gateway
     */
    public IdracNetworkSettings(String nodeId, String ipAddress, String netmask, String gateway)
    {
        super();
        this.nodeId = nodeId;
        this.ipAddress = ipAddress;
        this.netmask = netmask;
        this.gateway = gateway;
    }
    @JsonProperty("nodeId")
    public String getNodeId()
    {
        return nodeId;
    }

    @JsonProperty("nodeId")
    public void setNodeId(String nodeId)
    {
        this.nodeId = nodeId;
    }

    @JsonProperty("ipAddress")
    public String getIpAddress()
    {
        return ipAddress;
    }

    @JsonProperty("ipAddress")
    public void setIpAddress(String ipAddress)
    {
        this.ipAddress = ipAddress;
    }

    @JsonProperty("netmask")
    public String getNetmask()
    {
        return netmask;
    }

    @JsonProperty("netmask")
    public void setNetmask(String netmask)
    {
        this.netmask = netmask;
    }

    @JsonProperty("gateway")
    public String getGateway()
    {
        return gateway;
    }

    @JsonProperty("gateway")
    public void setGateway(String gateway)
    {
        this.gateway = gateway;
    }

    @Override
    public String toString()
    {
        return ToStringBuilder.reflectionToString(this);
    }

    @Override
    public int hashCode()
    {
        return new HashCodeBuilder().append(nodeId).append(ipAddress).append(netmask).append(gateway).toHashCode();
    }

    @Override
    public boolean equals(Object other)
    {
        if (other == this)
        {
            return true;
        }
        if (other instanceof IdracNetworkSettings)
        {
            return false;
        }
        IdracNetworkSettings rhs = ((IdracNetworkSettings) other);
        return new EqualsBuilder().append(nodeId, rhs.nodeId).append(ipAddress, rhs.ipAddress).append(netmask, rhs.netmask).append(gateway, rhs.gateway)
                .isEquals();
    }
}
