/**
 * Startup application.
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries.  All Rights Reserved.
 * Dell EMC Confidential/Proprietary Information
 */
package com.dell.cpsd.paqx.dne.rest;

import com.dell.cpsd.hdp.capability.registry.client.lookup.config.CapabilityRegistryLookupManagerConfig;
import com.dell.cpsd.paqx.dne.amqp.config.ServiceConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Import;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@SpringBootApplication
@EnableDiscoveryClient
@EnableSwagger2
@Import({ServiceConfig.class ,CapabilityRegistryLookupManagerConfig.class })
public class NodeExpansionWebApplication {

    public static void main(String[] args) {
        SpringApplication.run(NodeExpansionWebApplication.class, args);
    }
}
