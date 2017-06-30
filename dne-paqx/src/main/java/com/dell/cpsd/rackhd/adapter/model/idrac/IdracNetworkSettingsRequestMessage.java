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
@Message(value = "com.dell.cpsd.rackhd.adapter.model.idrac.IdracNetworkSettingsRequestMessage", version = "1.0")
@JsonPropertyOrder({"messageProperties", "idracNetworkSettings"})
public class IdracNetworkSettingsRequestMessage implements HasMessageProperties<MessageProperties> {

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

    /**
     * IDRAC network settings
     * <p>
     * IDRAC network settings
     * (Required)
     *
     */
    @JsonProperty("idracNetworkSettings")
    @JsonPropertyDescription("Network Settings request.")
    private IdracNetworkSettings idracNetworkSettings;

    /**
     * No args constructor for use in serialization
     *
     */
    public IdracNetworkSettingsRequestMessage()
    {
    }

    /**
     *
     * @param messageProperties
     * @param idracNetworkSettings
     */
    public IdracNetworkSettingsRequestMessage(MessageProperties messageProperties, IdracNetworkSettings idracNetworkSettings)
    {
        this.messageProperties = messageProperties;
        this.idracNetworkSettings = idracNetworkSettings;
    }


    /**
     * AMQP properties
     * <p>
     * AMQP properties.
     * (Required)
     *
     */
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

    @JsonProperty("idracNetworkSettings")
    public IdracNetworkSettings getIdracNetworkSettings() {
        return idracNetworkSettings;
    }

    @JsonProperty("idracNetworkSettings")
    public void setIdracNetworkSettings(IdracNetworkSettings idracNetworkSettings) {
        this.idracNetworkSettings = idracNetworkSettings;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(messageProperties).append(idracNetworkSettings).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof IdracNetworkSettingsRequestMessage) == false) {
            return false;
        }
        IdracNetworkSettingsRequestMessage rhs = ((IdracNetworkSettingsRequestMessage) other);
        return new EqualsBuilder().append(messageProperties, rhs.messageProperties).append(idracNetworkSettings, rhs.idracNetworkSettings).isEquals();
    }
}
