package com.dell.cpsd.paqx.dne.service.amqp.adapter;

import com.dell.cpsd.service.common.client.callback.ServiceResponse;
import com.dell.cpsd.service.common.client.rpc.ServiceCallbackAdapter;
import com.dell.cpsd.storage.capabilities.api.AddHostToProtectionDomainResponseMessage;
import com.dell.cpsd.storage.capabilities.api.MessageProperties;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

public class AddHostToProtectionDomainResponseAdapterTest extends BaseResponseAdapterTest<AddHostToProtectionDomainResponseMessage>
{
    @Override
    protected ServiceCallbackAdapter<AddHostToProtectionDomainResponseMessage, ServiceResponse<AddHostToProtectionDomainResponseMessage>> createTestable()
    {
        return new AddHostToProtectionDomainResponseAdapter(this.registry);
    }

    @Override
    protected AddHostToProtectionDomainResponseMessage createResponseMessageSpy()
    {
        AddHostToProtectionDomainResponseMessage theSpy = spy(AddHostToProtectionDomainResponseMessage.class);
        theSpy.setMessageProperties(mock(MessageProperties.class));
        return theSpy;
    }
}
