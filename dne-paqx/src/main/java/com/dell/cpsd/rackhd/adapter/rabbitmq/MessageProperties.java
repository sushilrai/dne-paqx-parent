/**
 * <p>
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries.  All Rights Reserved.
 * Dell EMC Confidential/Proprietary Information
 * </p>
 */

package com.dell.cpsd.rackhd.adapter.rabbitmq;

import com.dell.cpsd.common.rabbitmq.message.MessagePropertiesContainer;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import java.util.Date;

/**
 * AMQP properties properties
 * <p>
 * AMQP properties.
 *
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "timestamp",
        "correlationId",
        "replyTo"
})
public class MessageProperties implements MessagePropertiesContainer {

    /**
     * Message timestamp
     * <p>
     * Message creation timestamp.
     * (Required)
     *
     */
    @JsonProperty("timestamp")
    @JsonPropertyDescription("Message creation timestamp.")
    private Date timestamp;
    /**
     * Correlation ID
     * <p>
     * Messages chain reference.
     * (Required)
     *
     */
    @JsonProperty("correlationId")
    @JsonPropertyDescription("Messages chain reference.")
    private String correlationId;
    /**
     * Reply-to identifier.
     * <p>
     * Reply-to identifier
     *
     */
    @JsonProperty("replyTo")
    @JsonPropertyDescription("Reply-to identifier")
    private String replyTo;

    /**
     * No args constructor for use in serialization
     *
     */
    public MessageProperties() {
    }

    /**
     *
     * @param replyTo
     * @param correlationId
     * @param timestamp
     */
    public MessageProperties(Date timestamp, String correlationId, String replyTo) {
        super();
        this.timestamp = timestamp;
        this.correlationId = correlationId;
        this.replyTo = replyTo;
    }

    /**
     * Message timestamp
     * <p>
     * Message creation timestamp.
     * (Required)
     *
     */
    @JsonProperty("timestamp")
    public Date getTimestamp() {
        return timestamp;
    }

    /**
     * Message timestamp
     * <p>
     * Message creation timestamp.
     * (Required)
     *
     */
    @JsonProperty("timestamp")
    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public MessageProperties withTimestamp(Date timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    /**
     * Correlation ID
     * <p>
     * Messages chain reference.
     * (Required)
     *
     */
    @JsonProperty("correlationId")
    public String getCorrelationId() {
        return correlationId;
    }

    /**
     * Correlation ID
     * <p>
     * Messages chain reference.
     * (Required)
     *
     */
    @JsonProperty("correlationId")
    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }

    public MessageProperties withCorrelationId(String correlationId) {
        this.correlationId = correlationId;
        return this;
    }

    /**
     * Reply-to identifier.
     * <p>
     * Reply-to identifier
     *
     */
    @JsonProperty("replyTo")
    public String getReplyTo() {
        return replyTo;
    }

    /**
     * Reply-to identifier.
     * <p>
     * Reply-to identifier
     *
     */
    @JsonProperty("replyTo")
    public void setReplyTo(String replyTo) {
        this.replyTo = replyTo;
    }

    public MessageProperties withReplyTo(String replyTo) {
        this.replyTo = replyTo;
        return this;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(timestamp).append(correlationId).append(replyTo).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof MessageProperties) == false) {
            return false;
        }
        MessageProperties rhs = ((MessageProperties) other);
        return new EqualsBuilder().append(timestamp, rhs.timestamp).append(correlationId, rhs.correlationId).append(replyTo, rhs.replyTo).isEquals();
    }
}
