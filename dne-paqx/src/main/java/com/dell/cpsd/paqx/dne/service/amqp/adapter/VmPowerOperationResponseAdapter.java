/**
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries. All Rights Reserved. Dell EMC Confidential/Proprietary Information
 */

package com.dell.cpsd.paqx.dne.service.amqp.adapter;

import com.dell.cpsd.service.common.client.callback.IServiceCallback;
import com.dell.cpsd.service.common.client.callback.ServiceResponse;
import com.dell.cpsd.service.common.client.rpc.ServiceCallbackAdapter;
import com.dell.cpsd.service.common.client.rpc.ServiceCallbackRegistry;
import com.dell.cpsd.virtualization.capabilities.api.VmPowerOperationsResponseMessage;

/**
 * Callback adapter class used to process the response message from the vm power operation request.
 *
 * <p>
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries. All Rights Reserved. Dell EMC Confidential/Proprietary Information
 * <p/>
 *
 * @version 1.0
 * @since 1.0
 */
public class VmPowerOperationResponseAdapter
        implements ServiceCallbackAdapter<VmPowerOperationsResponseMessage, ServiceResponse<VmPowerOperationsResponseMessage>>
{
    private final ServiceCallbackRegistry serviceCallbackRegistry;

    public VmPowerOperationResponseAdapter(ServiceCallbackRegistry serviceCallbackRegistry)
    {
        this.serviceCallbackRegistry = serviceCallbackRegistry;
    }

    @Override
    public ServiceResponse<VmPowerOperationsResponseMessage> transform(final VmPowerOperationsResponseMessage responseMessage)
    {
        return new ServiceResponse<>(responseMessage.getMessageProperties().getCorrelationId(), responseMessage, null);
    }

    @Override
    public void consume(final IServiceCallback callback, final ServiceResponse<VmPowerOperationsResponseMessage> responseMessage)
    {
        callback.handleServiceResponse(responseMessage);
    }

    @Override
    public IServiceCallback take(final VmPowerOperationsResponseMessage responseMessage)
    {
        return serviceCallbackRegistry.removeServiceCallback(responseMessage.getMessageProperties().getCorrelationId());
    }

    @Override
    public Class<VmPowerOperationsResponseMessage> getSourceClass()
    {
        return VmPowerOperationsResponseMessage.class;
    }
}