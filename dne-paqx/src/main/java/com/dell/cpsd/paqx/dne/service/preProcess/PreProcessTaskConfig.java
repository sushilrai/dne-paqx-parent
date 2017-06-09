/**
 * <p>
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries.  All Rights Reserved.
 * Dell EMC Confidential/Proprietary Information
 * </p>
 */

package com.dell.cpsd.paqx.dne.service.preProcess;

import com.dell.cpsd.paqx.dne.service.model.Step;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class PreProcessTaskConfig {
    @Bean("preProcessWorkflowSteps")
    public Map<String, Step> preProcessWorkflowSteps(){
        final Map<String, Step> workflowSteps = new HashMap<>();

        workflowSteps.put("startPreProcessWorkflow", new Step("findVCluster"));
        workflowSteps.put("findVCluster", new Step("findProtectionDomain"));
        workflowSteps.put("findProtectionDomain", new Step("findSystemData"));
        workflowSteps.put("findSystemData", new Step("assignDefaultHostName"));
        workflowSteps.put("assignDefaultHostName", new Step("assignDefaultCredentials"));
        workflowSteps.put("assignDefaultCredentials", new Step("completed", true));
        workflowSteps.put("completed", null);

        return workflowSteps;
    }
}
