/**
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries.
 * All Rights Reserved. Dell EMC Confidential/Proprietary Information
 */

package com.dell.cpsd.paqx.dne.service.amqp.adapter;

import com.dell.cpsd.service.common.client.callback.IServiceCallback;
import com.dell.cpsd.service.common.client.callback.ServiceResponse;
import com.dell.cpsd.service.common.client.rpc.ServiceCallbackAdapter;
import com.dell.cpsd.service.common.client.rpc.ServiceCallbackRegistry;
import com.dell.cpsd.storage.capabilities.api.CreateProtectionDomainResponseMessage;

/**
 * Create Protection Domain Response Adapter
 * <p>
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries. All Rights Reserved.
 * Dell EMC Confidential/Proprietary Information
 * </p>
 *
 * @version 1.0
 * @since 1.0
 */
public class CreateProtectionDomainResponseAdapter
        implements ServiceCallbackAdapter<CreateProtectionDomainResponseMessage, ServiceResponse<CreateProtectionDomainResponseMessage>>
{
    private final ServiceCallbackRegistry serviceCallbackRegistry;

    public CreateProtectionDomainResponseAdapter(final ServiceCallbackRegistry serviceCallbackRegistry)
    {
        this.serviceCallbackRegistry = serviceCallbackRegistry;
    }

    @Override
    public ServiceResponse<CreateProtectionDomainResponseMessage> transform(final CreateProtectionDomainResponseMessage responseMessage)
    {
        return new ServiceResponse<>(responseMessage.getMessageProperties().getCorrelationId(), responseMessage, null);
    }

    @Override
    public void consume(final IServiceCallback iServiceCallback,
            final ServiceResponse<CreateProtectionDomainResponseMessage> createProtectionDomainResponseMessageServiceResponse)
    {
        iServiceCallback.handleServiceResponse(createProtectionDomainResponseMessageServiceResponse);
    }

    @Override
    public IServiceCallback take(final CreateProtectionDomainResponseMessage responseMessage)
    {
        return serviceCallbackRegistry.removeServiceCallback(responseMessage.getMessageProperties().getCorrelationId());
    }

    @Override
    public Class<CreateProtectionDomainResponseMessage> getSourceClass()
    {
        return CreateProtectionDomainResponseMessage.class;
    }
}
