/**
 * <p>
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries. All Rights Reserved. Dell EMC Confidential/Proprietary Information
 * </p>
 */


package com.dell.cpsd.paqx.dne.amqp.config;

import com.dell.cpsd.hdp.capability.registry.client.CapabilityRegistryException;
import com.dell.cpsd.hdp.capability.registry.client.ICapabilityRegistryLookupManager;
import com.dell.cpsd.hdp.capability.registry.client.binder.CapabilityBinder;
import com.dell.cpsd.hdp.capability.registry.client.binder.CapabilityBindingService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Queue;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * The tests for the CapabilityConfig class.
 *
 * <p>
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries. All Rights Reserved. Dell EMC Confidential/Proprietary Information
 * </p>
 *
 * @since 1.0
 */
@RunWith(MockitoJUnitRunner.class)
public class CapabilityConfigTest
{
    @Mock
    private ICapabilityRegistryLookupManager capabilityRegistryLookupManager;

    @Mock
    private AmqpAdmin amqpAdmin;

    @Mock
    private Queue queue;

    @InjectMocks
    private CapabilityConfig capabilityConfig;

    @Mock
    private CapabilityBinder capabilityBinder;

    @Mock
    private ContextRefreshedEvent contextRefreshedEvent;

    @Test
    public void capabilityBindingServiceSuccess() throws Exception
    {
        CapabilityBindingService result = this.capabilityConfig.capabilityBindingService();

        assertNotNull(result);
    }

    @Test
    public void dneCapabilityBinderSuccess() throws Exception
    {
        CapabilityBinder result = this.capabilityConfig.dneCapabilityBinder();

        assertNotNull(result);
    }

    @Test
    public void contextRefreshedEventListenerSuccess() throws Exception
    {
        ApplicationListener<ContextRefreshedEvent> result = this.capabilityConfig.contextRefreshedEventListener(this.capabilityBinder);
        result.onApplicationEvent(this.contextRefreshedEvent);

        assertNotNull(result);
        verify(this.capabilityBinder).bind();
    }

    @Test
    public void contextRefreshedEventListenerException() throws Exception
    {
        doThrow(new CapabilityRegistryException("something_happened")).when(this.capabilityBinder).bind();

        ApplicationListener<ContextRefreshedEvent> result = this.capabilityConfig.contextRefreshedEventListener(this.capabilityBinder);
        result.onApplicationEvent(this.contextRefreshedEvent);

        assertNotNull(result);
        verify(this.capabilityBinder, never()).getCurrentCapabilities();
    }
}