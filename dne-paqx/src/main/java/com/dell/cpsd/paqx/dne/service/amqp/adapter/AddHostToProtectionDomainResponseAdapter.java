/**
 * <p>
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries. All Rights Reserved. Dell EMC Confidential/Proprietary Information
 * </p>
 */
package com.dell.cpsd.paqx.dne.service.amqp.adapter;

import com.dell.cpsd.service.common.client.callback.IServiceCallback;
import com.dell.cpsd.service.common.client.callback.ServiceResponse;
import com.dell.cpsd.service.common.client.rpc.ServiceCallbackAdapter;
import com.dell.cpsd.service.common.client.rpc.ServiceCallbackRegistry;
import com.dell.cpsd.storage.capabilities.api.AddHostToProtectionDomainResponseMessage;

/**
 *
 * <p/>
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries. All Rights Reserved. Dell EMC Confidential/Proprietary Information
 * <p/>
 *
 * @version 1.0
 * @since 1.0
 */

public class AddHostToProtectionDomainResponseAdapter implements ServiceCallbackAdapter<AddHostToProtectionDomainResponseMessage, ServiceResponse<AddHostToProtectionDomainResponseMessage>> {
    private final ServiceCallbackRegistry serviceCallbackRegistry;

    public AddHostToProtectionDomainResponseAdapter(ServiceCallbackRegistry serviceCallbackRegistry)
    {
        this.serviceCallbackRegistry = serviceCallbackRegistry;
    }

    @Override
    public ServiceResponse<AddHostToProtectionDomainResponseMessage> transform(final AddHostToProtectionDomainResponseMessage responseMessage)
    {
        return new ServiceResponse<>(responseMessage.getMessageProperties().getCorrelationId(), responseMessage, null);
    }

    @Override
    public void consume(final IServiceCallback callback, final ServiceResponse<AddHostToProtectionDomainResponseMessage> responseMessage)
    {
        callback.handleServiceResponse(responseMessage);
    }

    @Override
    public IServiceCallback take(final AddHostToProtectionDomainResponseMessage addHostToProtectionDomainResponseMessage)
    {
        return serviceCallbackRegistry.removeServiceCallback(addHostToProtectionDomainResponseMessage.getMessageProperties().getCorrelationId());
    }

    @Override
    public Class<AddHostToProtectionDomainResponseMessage> getSourceClass()
    {
        return AddHostToProtectionDomainResponseMessage.class;
    }


}
