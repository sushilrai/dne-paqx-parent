/**
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries. All Rights Reserved. Dell EMC Confidential/Proprietary Information
 */

package com.dell.cpsd.paqx.dne.service.amqp.adapter;

import com.dell.cpsd.ConfigureRacadmFirmwareListCatalogResponseMessage;
import com.dell.cpsd.service.common.client.callback.IServiceCallback;
import com.dell.cpsd.service.common.client.callback.ServiceResponse;
import com.dell.cpsd.service.common.client.rpc.ServiceCallbackAdapter;
import com.dell.cpsd.service.common.client.rpc.ServiceCallbackRegistry;

/**
 * Response adapter to handle racadm-firmware-list-catalog response messages.
 * <p>
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries. All Rights Reserved. Dell EMC Confidential/Proprietary Information
 * </p>
 *
 * @since 1.0
 */
public class ConfigureRacadmFirmwareListCatalogResponseAdapter implements
        ServiceCallbackAdapter<ConfigureRacadmFirmwareListCatalogResponseMessage, ServiceResponse<ConfigureRacadmFirmwareListCatalogResponseMessage>>
{

    private final ServiceCallbackRegistry serviceCallbackRegistry;

    public ConfigureRacadmFirmwareListCatalogResponseAdapter(ServiceCallbackRegistry serviceCallbackRegistry)
    {
        this.serviceCallbackRegistry = serviceCallbackRegistry;
    }

    @Override
    public ServiceResponse<ConfigureRacadmFirmwareListCatalogResponseMessage> transform(
            final ConfigureRacadmFirmwareListCatalogResponseMessage responseMessage)
    {
        return new ServiceResponse<>(responseMessage.getMessageProperties().getCorrelationId(), responseMessage, null);
    }

    @Override
    public void consume(final IServiceCallback callback,
            final ServiceResponse<ConfigureRacadmFirmwareListCatalogResponseMessage> responseMessage)
    {
        callback.handleServiceResponse(responseMessage);
    }

    @Override
    public IServiceCallback take(final ConfigureRacadmFirmwareListCatalogResponseMessage responseMessage)
    {
        return serviceCallbackRegistry.removeServiceCallback(responseMessage.getMessageProperties().getCorrelationId());
    }

    @Override
    public Class<ConfigureRacadmFirmwareListCatalogResponseMessage> getSourceClass()
    {
        return ConfigureRacadmFirmwareListCatalogResponseMessage.class;
    }
}
