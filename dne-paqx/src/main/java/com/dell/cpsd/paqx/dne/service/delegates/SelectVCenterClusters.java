/**
 * <p>
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries. All Rights Reserved. Dell EMC Confidential/Proprietary Information
 * </p>
 */
package com.dell.cpsd.paqx.dne.service.delegates;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
@Qualifier("selectVCenterClusters")
public class SelectVCenterClusters extends BaseWorkflowDelegate
{

    private static final Logger LOGGER = LoggerFactory.getLogger(SelectVCenterClusters.class);

    @Override
    public void delegateExecute(final DelegateExecution delegateExecution)
    {

    }
}
