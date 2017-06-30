/**
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries.  All Rights Reserved.
 * Dell EMC Confidential/Proprietary Information
 */

package com.dell.cpsd.rackhd.adapter.model.idrac;

import com.dell.cpsd.rackhd.adapter.rabbitmq.MessageProperties;
import com.dell.cpsd.common.rabbitmq.annotation.Message;
import com.dell.cpsd.common.rabbitmq.message.HasMessageProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * <p>
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries.  All Rights Reserved.
 * Dell EMC Confidential/Proprietary Information
 * </p>
 *
 * @since 1.0
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Message(value = "com.dell.cpsd.rackhd.adapter.model.idrac.IdracNetworkSettingsResponseMessage", version = "1.0")
@JsonPropertyOrder({"messageProperties", "idracNetworkSettings"})
public class IdracNetworkSettingsResponseMessage  implements HasMessageProperties<MessageProperties> {

    /**
     * AMQP properties
     * <p>
     * AMQP properties.
     * (Required)
     *
     */
    @JsonProperty("messageProperties")
    @JsonPropertyDescription("AMQP properties.")
    private MessageProperties messageProperties;

    @JsonProperty("idracNetworkSettingsResponse")
    @JsonPropertyDescription("Network Settings response.")
    private IdracNetworkSettingsResponse idracNetworkSettingsResponse;

    /**
     * No args constructor for use in serialization
     *
     */
    public IdracNetworkSettingsResponseMessage()
    {

    }

    /**
     *
     * @param messageProperties
     * @param idracNetworkSettingsResponse
     */
    public IdracNetworkSettingsResponseMessage(MessageProperties messageProperties, IdracNetworkSettingsResponse idracNetworkSettingsResponse)
    {
        this.messageProperties = messageProperties;
        this.idracNetworkSettingsResponse = idracNetworkSettingsResponse;
    }

    @JsonProperty("idracNetworkSettingsResponse")
    public IdracNetworkSettingsResponse getIdracNetworkSettingsResponse() {
        return idracNetworkSettingsResponse;
    }

    @JsonProperty("idracNetworkSettingsResponse")
    public void setIdracNetworkSettingsResponse(IdracNetworkSettingsResponse idracNetworkSettingsResponse) {
        this.idracNetworkSettingsResponse = idracNetworkSettingsResponse;
    }

    @JsonProperty("messageProperties")
    @Override
    public MessageProperties getMessageProperties() {
        return messageProperties;
    }

    @JsonProperty("messageProperties")
    @Override
    public void setMessageProperties(MessageProperties messageProperties) {
        this.messageProperties = messageProperties;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(messageProperties).append(idracNetworkSettingsResponse).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof IdracNetworkSettingsResponseMessage) == false) {
            return false;
        }
        IdracNetworkSettingsResponseMessage rhs = ((IdracNetworkSettingsResponseMessage) other);
        return new EqualsBuilder().append(messageProperties, rhs.messageProperties).append(idracNetworkSettingsResponse, rhs.idracNetworkSettingsResponse).isEquals();
    }
}
