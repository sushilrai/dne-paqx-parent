/**
 * <p>
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries. All Rights Reserved. Dell EMC Confidential/Proprietary Information
 * </p>
 */
package com.dell.cpsd.paqx.dne.amqp.config;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.junit.MockitoRule;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.support.converter.ClassMapper;
import org.springframework.amqp.support.converter.DefaultClassMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.env.Environment;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

/**
 * Test for {@link RabbitConfig}
 */
@RunWith(MockitoJUnitRunner.class)
public class RabbitConfigTest {
    @InjectMocks
    private RabbitConfig rabbitConfig;

    @Mock
    @Qualifier("rabbitConnectionFactory")
    private ConnectionFactory rabbitConnectionFactory;

    @Mock
    private PropertiesConfig propertiesConfig;

    @Mock
    @Qualifier("supportedIdClassMapping")
    private Map<String, Class<?>> sdkSupportedIdClassMapping;

    static {
        System.setProperty("container.id", "testRabbit");
    }

    @Before
    public void setup() {
        ReflectionTestUtils.setField(rabbitConfig, "essRequestExchange", "");
        ReflectionTestUtils.setField(rabbitConfig, "essReqRoutingKeyPrefix", "jdbc:h2:~/tmp/dnepaqxdb");
        ReflectionTestUtils.setField(rabbitConfig, "essResponseExchange", "org.h2.Driver");
        ReflectionTestUtils.setField(rabbitConfig, "essResQueue", "org.hibernate.dialect.H2Dialect");
        ReflectionTestUtils.setField(rabbitConfig, "essRespRoutingKeyPrefix", "false");
    }

    @Test
    public void testRabbitTemplate() throws Exception {
        assertNotNull(rabbitConfig.rabbitTemplate());
    }

    @Test
    public void testRetryTemplate() throws Exception {
        assertNotNull(rabbitConfig.retryTemplate());

    }

    @Test
    public void testDnemessageConverter() throws Exception {

        assertNotNull(rabbitConfig.dneMessageConverter());

    }

    @Test
    public void testHostName() throws Exception {
        assertTrue(rabbitConfig.hostName().equals("testRabbit"));
    }

    @Test
    public void testReplyTo() throws Exception {
        when(propertiesConfig.applicationName()).thenReturn("DNE");
        assertTrue(rabbitConfig.replyTo().equals("DNE.testRabbit"));
    }

    @Test
    public void testNodeExpansionAmqpAdmin() throws Exception {
        assertNotNull(rabbitConfig.nodeExpansionAmqpAdmin());
    }

    @Test
    public void testClassMapper() {
        assertNotNull(rabbitConfig.classMapper());

    }

    @Test
    public void testNodeExansionresponseQueue() throws Exception {
        assertNotNull(rabbitConfig.nodeExpansionResponseQueue());

    }

    @Test
    public void testEssRequestExchanges() throws Exception {
        assertNotNull(rabbitConfig.essRequestExchange());

    }

    @Test
    public void testEssReqRoutingKeyPrefxi() throws Exception {
        assertNotNull(rabbitConfig.essReqRoutingKeyPrefix());

    }

    @Test
    public void testEssResponseExchanges() throws Exception {
        assertNotNull(rabbitConfig.essRequestExchange());
    }

    @Test
    public void testEssResponseQueue() throws Exception {
        assertNotNull(rabbitConfig.essResponseQueue());
    }

    @Test
    public void tstEssBinding() throws Exception {
        assertNotNull(rabbitConfig.essBinding());
    }


}
