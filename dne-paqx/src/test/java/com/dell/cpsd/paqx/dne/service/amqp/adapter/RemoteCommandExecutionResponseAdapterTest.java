/**
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries.  All Rights Reserved. Dell EMC Confidential/Proprietary Information
 *
 */

package com.dell.cpsd.paqx.dne.service.amqp.adapter;

import com.dell.cpsd.service.common.client.callback.ServiceResponse;
import com.dell.cpsd.service.common.client.rpc.ServiceCallbackAdapter;
import com.dell.cpsd.virtualization.capabilities.api.ConfigureVmNetworkSettingsResponseMessage;
import com.dell.cpsd.virtualization.capabilities.api.MessageProperties;
import com.dell.cpsd.virtualization.capabilities.api.RemoteCommandExecutionResponseMessage;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

/**
 * The tests for RemoteCommandExecutionResponseAdapter class.
 *
 * <p>
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries. All Rights Reserved. Dell EMC Confidential/Proprietary Information
 * </p>
 *
 * @since 1.0
 */
public class RemoteCommandExecutionResponseAdapterTest extends BaseResponseAdapterTest<RemoteCommandExecutionResponseMessage>
{
    @Override
    protected ServiceCallbackAdapter<RemoteCommandExecutionResponseMessage, ServiceResponse<RemoteCommandExecutionResponseMessage>> createTestable()
    {
        return new RemoteCommandExecutionResponseAdapter(this.registry);
    }

    @Override
    protected RemoteCommandExecutionResponseMessage createResponseMessageSpy()
    {
        RemoteCommandExecutionResponseMessage theSpy = spy(RemoteCommandExecutionResponseMessage.class);
        theSpy.setMessageProperties(mock(MessageProperties.class));
        return theSpy;
    }
}