/**
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries.  All Rights Reserved. Dell EMC Confidential/Proprietary Information
 *
 */

package com.dell.cpsd.paqx.dne.service.amqp.adapter;

import com.dell.cpsd.virtualization.capabilities.api.MessageProperties;
import com.dell.cpsd.service.common.client.callback.ServiceResponse;
import com.dell.cpsd.service.common.client.rpc.ServiceCallbackAdapter;
import com.dell.cpsd.virtualization.capabilities.api.ConfigureVmNetworkSettingsResponseMessage;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

/**
 * The tests for ConfigureVmNetworkSettingsResponseAdapter class.
 *
 * <p>
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries. All Rights Reserved. Dell EMC Confidential/Proprietary Information
 * </p>
 *
 * @since 1.0
 */
public class ConfigureVmNetworkSettingsResponseAdapterTest extends BaseResponseAdapterTest<ConfigureVmNetworkSettingsResponseMessage>
{
    @Override
    protected ServiceCallbackAdapter<ConfigureVmNetworkSettingsResponseMessage, ServiceResponse<ConfigureVmNetworkSettingsResponseMessage>> createTestable()
    {
        return new ConfigureVmNetworkSettingsResponseAdapter(this.registry);
    }

    @Override
    protected ConfigureVmNetworkSettingsResponseMessage createResponseMessageSpy()
    {
        ConfigureVmNetworkSettingsResponseMessage theSpy = spy(ConfigureVmNetworkSettingsResponseMessage.class);
        theSpy.setMessageProperties(mock(MessageProperties.class));
        return theSpy;
    }
}