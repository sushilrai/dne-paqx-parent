/**
 * <p>
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries. All Rights Reserved. Dell EMC Confidential/Proprietary Information
 * </p>
 */
package com.dell.cpsd.paqx.dne.amqp.config;

import com.dell.cpsd.service.common.client.rpc.DelegatingMessageConsumer;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.support.converter.MessageConverter;

import static org.junit.Assert.assertNotNull;

/**
 * Test for {@link DneConsumerConfig}
 */
@RunWith(MockitoJUnitRunner.class)
public class DneConsumerConfigTest
{

    @Mock
    private ConnectionFactory rabbitConnectionFactory;

    @Mock
    private Queue responseQueue;

    @Mock
    private Queue essResponseQueue;

    @Mock
    private DelegatingMessageConsumer delegatingMessageConsumer;

    @Mock
    private MessageConverter messageConverter;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @InjectMocks
    private DneConsumerConfig dneConsumerConfig;


    @Test
    public void testDefaultMessageConsumer() {
        assertNotNull(dneConsumerConfig.defaultMessageConsumer());
    }

    @Test
    public void testDenListnerRetryPolicy() {
        assertNotNull(dneConsumerConfig.dneListenerRetryPolicy());
    }

    @Test
    public void testRequestListenerContainer() throws Exception {

        assertNotNull(dneConsumerConfig.requestListenerContainer(delegatingMessageConsumer, messageConverter));

    }
}
