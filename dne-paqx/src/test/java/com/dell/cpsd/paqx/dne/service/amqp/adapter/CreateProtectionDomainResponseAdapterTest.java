/**
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries.  All Rights Reserved. Dell EMC Confidential/Proprietary Information
 */

package com.dell.cpsd.paqx.dne.service.amqp.adapter;

import com.dell.cpsd.service.common.client.callback.ServiceResponse;
import com.dell.cpsd.service.common.client.rpc.ServiceCallbackAdapter;
import com.dell.cpsd.storage.capabilities.api.CreateProtectionDomainResponseMessage;
import com.dell.cpsd.storage.capabilities.api.MessageProperties;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

/**
 * The tests for CreateProtectionDomainResponseAdapter class.
 *
 * <p>
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries. All Rights Reserved. Dell EMC Confidential/Proprietary Information
 * </p>
 *
 * @since 1.0
 */
public class CreateProtectionDomainResponseAdapterTest extends BaseResponseAdapterTest<CreateProtectionDomainResponseMessage>
{
    @Override
    protected ServiceCallbackAdapter<CreateProtectionDomainResponseMessage, ServiceResponse<CreateProtectionDomainResponseMessage>> createTestable()
    {
        return new CreateProtectionDomainResponseAdapter(this.registry);
    }

    @Override
    protected CreateProtectionDomainResponseMessage createResponseMessageSpy()
    {
        CreateProtectionDomainResponseMessage theSpy = spy(CreateProtectionDomainResponseMessage.class);
        theSpy.setMessageProperties(mock(MessageProperties.class));
        return theSpy;
    }
}