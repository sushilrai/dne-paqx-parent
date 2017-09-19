/**
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries.  All Rights Reserved. Dell EMC Confidential/Proprietary Information
 *
 */

package com.dell.cpsd.paqx.dne.service.amqp.adapter;

import com.dell.cpsd.service.common.client.callback.ServiceResponse;
import com.dell.cpsd.service.common.client.rpc.ServiceCallbackAdapter;
import com.dell.cpsd.virtualization.capabilities.api.DatastoreRenameResponseMessage;
import com.dell.cpsd.virtualization.capabilities.api.MessageProperties;
import com.dell.cpsd.virtualization.capabilities.api.VCenterUpdateSoftwareAcceptanceResponseMessage;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

/**
 * The tests for VCenterUpdateSoftwareAcceptanceResponseAdapter class.
 *
 * <p>
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries. All Rights Reserved. Dell EMC Confidential/Proprietary Information
 * </p>
 *
 * @since 1.0
 */
public class VCenterUpdateSoftwareAcceptanceResponseAdapterTest extends BaseResponseAdapterTest<VCenterUpdateSoftwareAcceptanceResponseMessage>
{
    @Override
    protected ServiceCallbackAdapter<VCenterUpdateSoftwareAcceptanceResponseMessage, ServiceResponse<VCenterUpdateSoftwareAcceptanceResponseMessage>> createTestable()
    {
        return new VCenterUpdateSoftwareAcceptanceResponseAdapter(this.registry);
    }

    @Override
    protected VCenterUpdateSoftwareAcceptanceResponseMessage createResponseMessageSpy()
    {
        VCenterUpdateSoftwareAcceptanceResponseMessage theSpy = spy(VCenterUpdateSoftwareAcceptanceResponseMessage.class);
        theSpy.setMessageProperties(mock(MessageProperties.class));
        return theSpy;
    }
}