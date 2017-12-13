/**
 * <p>
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries. All Rights Reserved. Dell EMC Confidential/Proprietary Information
 * </p>
 */

package com.dell.cpsd.paqx.dne.service.delegates.request;

import com.dell.cpsd.paqx.dne.repository.DataServiceRepository;
import com.dell.cpsd.paqx.dne.service.delegates.BaseWorkflowDelegate;
import com.dell.cpsd.paqx.dne.service.delegates.model.NodeDetail;
import com.dell.cpsd.paqx.dne.service.delegates.runnable.AddNodeToSystemDefinitionRunnable;
import com.dell.cpsd.sdk.AMQPClient;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;

import java.util.concurrent.ExecutorService;

import static com.dell.cpsd.paqx.dne.service.delegates.utils.DelegateConstants.ADD_NODE_TO_SYSTEM_DEFINITION_ACTIVITY_ID;
import static com.dell.cpsd.paqx.dne.service.delegates.utils.DelegateConstants.ADD_NODE_TO_SYSTEM_DEFINITION_MESSAGE_ID;
import static com.dell.cpsd.paqx.dne.service.delegates.utils.DelegateConstants.NODE_DETAIL;
import static com.dell.cpsd.paqx.dne.service.delegates.utils.DelegateConstants.SEND_ADD_NODE_TO_SYSTEM_DEFINITION_FAILED;

@org.springframework.stereotype.Component
@Scope("prototype")
@Qualifier("sendAddNodeToSystemDefinition")
public class SendAddNodeToSystemDefinition extends BaseWorkflowDelegate
{
    private static final Logger LOGGER = LoggerFactory.getLogger(SendAddNodeToSystemDefinition.class);

    private final RuntimeService        runtimeService;
    private final AMQPClient            sdkAMQPClient;
    private final DataServiceRepository repository;
    private final ExecutorService       taskExecutorService;

    /**
     * SendAddNodeToSystemDefinition constructor.
     *
     * @param runtimeService         - The <code>RuntimeService</code> instance.
     * @param sdkAMQPClient          - The <code>AMQPClient</code> instance.
     * @param repository             - The <code>DataServiceRepository</code> instance.
     * @param dneTaskExecutorService - The <code>ExecutorService</code> instance.
     * @since 1.0
     */
    @Autowired
    public SendAddNodeToSystemDefinition(final RuntimeService runtimeService, final AMQPClient sdkAMQPClient,
            final DataServiceRepository repository, final ExecutorService dneTaskExecutorService)
    {
        super(LOGGER, "Send Add Node To System Definition");
        this.runtimeService = runtimeService;
        this.sdkAMQPClient = sdkAMQPClient;
        this.repository = repository;
        this.taskExecutorService = dneTaskExecutorService;
    }

    @Override
    public void delegateExecute(final DelegateExecution delegateExecution)
    {
        final NodeDetail nodeDetail = (NodeDetail) delegateExecution.getVariable(NODE_DETAIL);
        updateDelegateStatus("Attempting " + this.taskName + " on Node " + nodeDetail.getServiceTag() + ".");

        try
        {
            AddNodeToSystemDefinitionRunnable runnable = new AddNodeToSystemDefinitionRunnable(delegateExecution.getProcessInstanceId(),
                    ADD_NODE_TO_SYSTEM_DEFINITION_ACTIVITY_ID, ADD_NODE_TO_SYSTEM_DEFINITION_MESSAGE_ID, nodeDetail, runtimeService,
                    sdkAMQPClient, repository);

            taskExecutorService.execute(runnable);
        }
        catch (Exception ex)
        {
            final String message = this.taskName + " on Node " + nodeDetail.getServiceTag() + " failed! Reason: ";
            updateDelegateStatus(message, ex);
            throw new BpmnError(SEND_ADD_NODE_TO_SYSTEM_DEFINITION_FAILED, message + ex.getMessage());
        }

        final String message = this.taskName + " on Node " + nodeDetail.getServiceTag() + " was successful.";
        updateDelegateStatus(message);
    }

}
