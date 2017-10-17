/**
 * <p>
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries. All Rights Reserved. Dell EMC Confidential/Proprietary Information
 * </p>
 */
package com.dell.cpsd.paqx.dne.amqp.config;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoJUnitRunner;

import static org.assertj.core.api.Java6Assertions.assertThat;

/**
 * Test for {@link ContextConfig}
 */
@RunWith(MockitoJUnitRunner.class)
public class ContextConfigTest {

    @Test
    public void testContextConfig()
    {
        ContextConfig contextConfig = new ContextConfig();
        assertThat( contextConfig.consumerName().equalsIgnoreCase("den-paqx"));
    }
}
