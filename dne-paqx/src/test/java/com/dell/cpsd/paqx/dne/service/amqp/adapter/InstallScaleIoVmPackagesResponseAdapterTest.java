/**
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries.  All Rights Reserved. Dell EMC Confidential/Proprietary Information
 */

package com.dell.cpsd.paqx.dne.service.amqp.adapter;

import com.dell.cpsd.service.common.client.callback.ServiceResponse;
import com.dell.cpsd.service.common.client.rpc.ServiceCallbackAdapter;
import com.dell.cpsd.virtualization.capabilities.api.MessageProperties;
import com.dell.cpsd.virtualization.capabilities.api.RemoteCommandExecutionResponseMessage;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

public class InstallScaleIoVmPackagesResponseAdapterTest extends BaseAsynchronousResponseAdapterTest<RemoteCommandExecutionResponseMessage>
{
    @Override
    protected ServiceCallbackAdapter<RemoteCommandExecutionResponseMessage, ServiceResponse<RemoteCommandExecutionResponseMessage>> createTestable()
    {
        return new InstallScaleIoVmPackagesResponseAdapter(this.registry, this.runtimeService);
    }

    @Override
    protected RemoteCommandExecutionResponseMessage createResponseMessageSpy()
    {
        RemoteCommandExecutionResponseMessage theSpy = spy(RemoteCommandExecutionResponseMessage.class);
        theSpy.setMessageProperties(mock(MessageProperties.class));
        return theSpy;
    }
}
