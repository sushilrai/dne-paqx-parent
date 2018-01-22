/**
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries.  All Rights Reserved.
 * Dell EMC Confidential/Proprietary Information
 */

package com.dell.cpsd.paqx.dne.amqp.config;

import com.dell.cpsd.service.common.client.context.ConsumerContextConfig;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DneContextConfig extends ConsumerContextConfig {
    private static final String CONSUMER_NAME = "dell-cpsd-dne-node-expansion-service";

    /**
     * DneContextConfig constructor.
     *
     * @since 1.0
     */
    public DneContextConfig() {
        super(CONSUMER_NAME, true);
    }
}
