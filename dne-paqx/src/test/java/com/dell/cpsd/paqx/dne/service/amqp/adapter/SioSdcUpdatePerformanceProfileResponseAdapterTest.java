/**
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries.  All Rights Reserved. Dell EMC Confidential/Proprietary Information
 */

package com.dell.cpsd.paqx.dne.service.amqp.adapter;

import com.dell.cpsd.service.common.client.callback.ServiceResponse;
import com.dell.cpsd.service.common.client.rpc.ServiceCallbackAdapter;
import com.dell.cpsd.storage.capabilities.api.MessageProperties;
import com.dell.cpsd.storage.capabilities.api.SioSdcUpdatePerformanceProfileResponseMessage;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

/**
 * The tests for SoftwareVibResponseAdapter class.
 * <p>
 * <p>
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries. All Rights Reserved. Dell EMC Confidential/Proprietary Information
 * </p>
 *
 * @since 1.0
 */
public class SioSdcUpdatePerformanceProfileResponseAdapterTest
        extends BaseResponseAdapterTest<SioSdcUpdatePerformanceProfileResponseMessage>
{
    @Override
    protected ServiceCallbackAdapter<SioSdcUpdatePerformanceProfileResponseMessage, ServiceResponse<SioSdcUpdatePerformanceProfileResponseMessage>> createTestable()
    {
        return new SioSdcUpdatePerformanceProfileResponseAdapter(this.registry);
    }

    @Override
    protected SioSdcUpdatePerformanceProfileResponseMessage createResponseMessageSpy()
    {
        SioSdcUpdatePerformanceProfileResponseMessage theSpy = spy(SioSdcUpdatePerformanceProfileResponseMessage.class);
        theSpy.setMessageProperties(mock(MessageProperties.class));
        return theSpy;
    }
}