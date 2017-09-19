/**
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries.  All Rights Reserved. Dell EMC Confidential/Proprietary Information
 *
 */

package com.dell.cpsd.paqx.dne.service.amqp.adapter;

import com.dell.cpsd.MessageProperties;
import com.dell.cpsd.SetObmSettingsResponseMessage;
import com.dell.cpsd.service.common.client.callback.ServiceResponse;
import com.dell.cpsd.service.common.client.rpc.ServiceCallbackAdapter;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

/**
 * The tests for ConfigureObmSettingsResponseAdapter class.
 *
 * <p>
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries. All Rights Reserved. Dell EMC Confidential/Proprietary Information
 * </p>
 *
 * @since 1.0
 */
public class ConfigureObmSettingsResponseAdapterTest extends BaseResponseAdapterTest<SetObmSettingsResponseMessage>
{
    @Override
    protected ServiceCallbackAdapter<SetObmSettingsResponseMessage, ServiceResponse<SetObmSettingsResponseMessage>> createTestable()
    {
        return new ConfigureObmSettingsResponseAdapter(this.registry);
    }

    @Override
    protected SetObmSettingsResponseMessage createResponseMessageSpy()
    {
        SetObmSettingsResponseMessage theSpy = spy(SetObmSettingsResponseMessage.class);
        theSpy.setMessageProperties(mock(MessageProperties.class));
        return theSpy;
    }
}