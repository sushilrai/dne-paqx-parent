/**
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries. All Rights Reserved. Dell EMC Confidential/Proprietary Information
 */
package com.dell.cpsd.paqx.dne.service.amqp.adapter;

import com.dell.cpsd.service.common.client.callback.IServiceCallback;
import com.dell.cpsd.service.common.client.callback.ServiceResponse;
import com.dell.cpsd.service.common.client.rpc.ServiceCallbackAdapter;
import com.dell.cpsd.service.common.client.rpc.ServiceCallbackRegistry;
import com.dell.cpsd.storage.capabilities.api.CreateStoragePoolResponseMessage;

/**
 * <p>
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries. All Rights Reserved. Dell EMC Confidential/Proprietary Information
 * </p>
 *
 * @since 1.0
 */
public class CreateStoragePoolAdapter implements ServiceCallbackAdapter<CreateStoragePoolResponseMessage, ServiceResponse<CreateStoragePoolResponseMessage>>
{

    private final ServiceCallbackRegistry serviceCallbackRegistry;

    public CreateStoragePoolAdapter(ServiceCallbackRegistry serviceCallbackRegistry)
    {
        this.serviceCallbackRegistry = serviceCallbackRegistry;
    }

    @Override
    public ServiceResponse<CreateStoragePoolResponseMessage> transform(final CreateStoragePoolResponseMessage responseMessage)
    {
        return new ServiceResponse<>(responseMessage.getMessageProperties().getCorrelationId(), responseMessage, null);
    }

    @Override
    public void consume(final IServiceCallback callback, final ServiceResponse<CreateStoragePoolResponseMessage> responseMessage)
    {
        callback.handleServiceResponse(responseMessage);
    }

    @Override
    public IServiceCallback take(final CreateStoragePoolResponseMessage addHostToDvSwitchResponseMessage)
    {
        return serviceCallbackRegistry.removeServiceCallback(addHostToDvSwitchResponseMessage.getMessageProperties().getCorrelationId());
    }

    @Override
    public Class<CreateStoragePoolResponseMessage> getSourceClass()
    {
        return CreateStoragePoolResponseMessage.class;
    }
}
