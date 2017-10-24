/**
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries.  All Rights Reserved. Dell EMC Confidential/Proprietary Information
 */

package com.dell.cpsd.paqx.dne.service.amqp.adapter;

import com.dell.cpsd.service.common.client.callback.ServiceResponse;
import com.dell.cpsd.service.common.client.rpc.ServiceCallbackAdapter;
import com.dell.cpsd.service.engineering.standards.EssValidateProtectionDomainsResponseMessage;
import com.dell.cpsd.service.engineering.standards.MessageProperties;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

/**
 * The tests for ValidateProtectionDomainResponseAdapter class.
 *
 * <p>
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries. All Rights Reserved. Dell EMC Confidential/Proprietary Information
 * </p>
 *
 * @since 1.0
 */
public class ValidateProtectionDomainResponseAdapterTest extends BaseResponseAdapterTest<EssValidateProtectionDomainsResponseMessage>
{
    @Override
    protected ServiceCallbackAdapter<EssValidateProtectionDomainsResponseMessage, ServiceResponse<EssValidateProtectionDomainsResponseMessage>> createTestable()
    {
        return new ValidateProtectionDomainResponseAdapter(this.registry);
    }

    @Override
    protected EssValidateProtectionDomainsResponseMessage createResponseMessageSpy()
    {
        EssValidateProtectionDomainsResponseMessage theSpy = spy(EssValidateProtectionDomainsResponseMessage.class);
        theSpy.setMessageProperties(mock(MessageProperties.class));
        return theSpy;
    }
}