/**
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries.  All Rights Reserved. Dell EMC Confidential/Proprietary Information
 */

package com.dell.cpsd.paqx.dne.service.amqp.adapter;

import com.dell.cpsd.service.common.client.callback.ServiceResponse;
import com.dell.cpsd.service.common.client.rpc.ServiceCallbackAdapter;
import com.dell.cpsd.storage.capabilities.api.CreateStoragePoolResponseMessage;
import com.dell.cpsd.storage.capabilities.api.MessageProperties;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

/**
 * The tests for CreateStoragePoolAdapter class.
 *
 * <p>
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries. All Rights Reserved. Dell EMC Confidential/Proprietary Information
 * </p>
 *
 * @since 1.0
 */
public class CreateStoragePoolAdapterTest extends BaseResponseAdapterTest<CreateStoragePoolResponseMessage>
{
    @Override
    protected ServiceCallbackAdapter<CreateStoragePoolResponseMessage, ServiceResponse<CreateStoragePoolResponseMessage>> createTestable()
    {
        return new CreateStoragePoolAdapter(this.registry);
    }

    @Override
    protected CreateStoragePoolResponseMessage createResponseMessageSpy()
    {
        CreateStoragePoolResponseMessage theSpy = spy(CreateStoragePoolResponseMessage.class);
        theSpy.setMessageProperties(mock(MessageProperties.class));
        return theSpy;
    }
}