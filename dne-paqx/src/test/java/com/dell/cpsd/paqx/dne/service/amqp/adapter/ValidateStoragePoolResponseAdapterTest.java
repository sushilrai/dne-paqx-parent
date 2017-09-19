/**
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries.  All Rights Reserved. Dell EMC Confidential/Proprietary Information
 *
 */

package com.dell.cpsd.paqx.dne.service.amqp.adapter;

import com.dell.cpsd.service.common.client.callback.ServiceResponse;
import com.dell.cpsd.service.common.client.rpc.ServiceCallbackAdapter;
import com.dell.cpsd.service.engineering.standards.EssValidateStoragePoolResponseMessage;
import com.dell.cpsd.service.engineering.standards.MessageProperties;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

/**
 * The tests for ValidateStoragePoolResponseAdapter class.
 *
 * <p>
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries. All Rights Reserved. Dell EMC Confidential/Proprietary Information
 * </p>
 *
 * @since 1.0
 */
public class ValidateStoragePoolResponseAdapterTest extends BaseResponseAdapterTest<EssValidateStoragePoolResponseMessage>
{
    @Override
    protected ServiceCallbackAdapter<EssValidateStoragePoolResponseMessage, ServiceResponse<EssValidateStoragePoolResponseMessage>> createTestable()
    {
        return new ValidateStoragePoolResponseAdapter(this.registry);
    }

    @Override
    protected EssValidateStoragePoolResponseMessage createResponseMessageSpy()
    {
        EssValidateStoragePoolResponseMessage theSpy = spy(EssValidateStoragePoolResponseMessage.class);
        theSpy.setMessageProperties(mock(MessageProperties.class));
        return theSpy;
    }
}