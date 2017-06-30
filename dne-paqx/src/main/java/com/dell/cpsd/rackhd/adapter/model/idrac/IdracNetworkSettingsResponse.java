/**
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries.  All Rights Reserved.
 * Dell EMC Confidential/Proprietary Information
 */

package com.dell.cpsd.rackhd.adapter.model.idrac;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
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
public class IdracNetworkSettingsResponse extends IdracNetworkSettings {

    @JsonProperty("message")
    @NotNull
    private String message;

    /**
     * No args constructor for use in serialization
     *
     */
    public IdracNetworkSettingsResponse()
    {

    }

    /**
     *
     * @param message
     * @param nodeId
     * @param ipAddress
     * @param netmask
     * @param gateway
     */
    public IdracNetworkSettingsResponse(String message, String nodeId, String ipAddress, String netmask, String gateway)
    {
        this.message = message;
        this.setNodeId(nodeId);
        this.setIpAddress(ipAddress);
        this.setNetmask(netmask);
        this.setGateway(gateway);
    }

    @JsonProperty("message")
    public String getMessage()
    {
        return message;
    }

    @JsonProperty("message")
    public void setMessage(String message)
    {
        this.message = message;
    }

    @Override
    public String toString()
    {
        return ToStringBuilder.reflectionToString(this);
    }

    @Override
    public int hashCode()
    {
        return new HashCodeBuilder().append(this.getNodeId()).append(this.getIpAddress()).append(this.getNetmask())
                .append(this.getGateway()).append(message).toHashCode();
    }

    @Override
    public boolean equals(Object other)
    {
        if (other == this)
        {
            return true;
        }
        if (other instanceof IdracNetworkSettingsResponse)
        {
            return false;
        }
        IdracNetworkSettingsResponse rhs = ((IdracNetworkSettingsResponse) other);
        return new EqualsBuilder().append(this.getNodeId(), rhs.getNodeId()).append(this.getIpAddress(), rhs.getIpAddress())
                .append(this.getNetmask(), rhs.getNetmask()).append(this.getGateway(), rhs.getGateway())
                .append(message, rhs.message).isEquals();
    }
}
