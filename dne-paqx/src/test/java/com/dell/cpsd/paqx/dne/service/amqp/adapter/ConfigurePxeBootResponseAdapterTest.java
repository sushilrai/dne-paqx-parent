/**
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries.  All Rights Reserved. Dell EMC Confidential/Proprietary Information
 *
 */

package com.dell.cpsd.paqx.dne.service.amqp.adapter;

import com.dell.cpsd.ConfigurePxeBootResponseMessage;
import com.dell.cpsd.MessageProperties;
import com.dell.cpsd.service.common.client.callback.ServiceResponse;
import com.dell.cpsd.service.common.client.rpc.ServiceCallbackAdapter;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

/**
 * The tests for ConfigurePxeBootResponseAdapter class.
 *
 * <p>
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries. All Rights Reserved. Dell EMC Confidential/Proprietary Information
 * </p>
 *
 * @since 1.0
 */
public class ConfigurePxeBootResponseAdapterTest extends BaseResponseAdapterTest<ConfigurePxeBootResponseMessage>
{
    @Override
    protected ServiceCallbackAdapter<ConfigurePxeBootResponseMessage, ServiceResponse<ConfigurePxeBootResponseMessage>> createTestable()
    {
        return new ConfigurePxeBootResponseAdapter(this.registry);
    }

    @Override
    protected ConfigurePxeBootResponseMessage createResponseMessageSpy()
    {
        ConfigurePxeBootResponseMessage theSpy = spy(ConfigurePxeBootResponseMessage.class);
        theSpy.setMessageProperties(mock(MessageProperties.class));
        return theSpy;
    }
}



