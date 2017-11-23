/**
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries.  All Rights Reserved.
 * Dell EMC Confidential/Proprietary Information
 **/

package com.dell.cpsd.paqx.dne.service.delegates.model;

import com.dell.cpsd.common.rabbitmq.message.HasMessageProperties;

/**
 * Delegate request model containing the request message and
 * service tag.
 * <p>
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries. All Rights Reserved.
 * Dell EMC Confidential/Proprietary Information
 * </p>
 *
 * @version 1.0
 * @since 1.0
 */
public class DelegateRequestModel<T extends HasMessageProperties>
{
    private final T requestMessage;
    private final String serviceTag;

    public DelegateRequestModel(final T requestMessage, final String serviceTag)
    {
        this.requestMessage = requestMessage;
        this.serviceTag = serviceTag;
    }

    public T getRequestMessage()
    {
        return requestMessage;
    }

    public String getServiceTag()
    {
        return serviceTag;
    }
}
