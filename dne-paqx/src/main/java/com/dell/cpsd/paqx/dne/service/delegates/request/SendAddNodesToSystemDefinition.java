/**
 * <p>
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries. All Rights Reserved. Dell EMC Confidential/Proprietary Information
 * </p>
 */

package com.dell.cpsd.paqx.dne.service.delegates.request;

import com.dell.cpsd.paqx.dne.domain.node.NodeInventory;
import com.dell.cpsd.paqx.dne.repository.DataServiceRepository;
import com.dell.cpsd.paqx.dne.service.NodeService;
import com.dell.cpsd.paqx.dne.service.delegates.BaseWorkflowDelegate;
import com.dell.cpsd.paqx.dne.service.delegates.model.NodeDetail;
import com.dell.cpsd.paqx.dne.service.delegates.runnable.AddNodesToSystemDefinitionRunnable;
import com.dell.cpsd.paqx.dne.util.NodeInventoryParsingUtil;
import com.dell.cpsd.sdk.AMQPClient;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

import static com.dell.cpsd.paqx.dne.service.delegates.utils.DelegateConstants.ADD_NODES_TO_SYSTEM_DEFINITION_ACTIVITY_ID;
import static com.dell.cpsd.paqx.dne.service.delegates.utils.DelegateConstants.ADD_NODES_TO_SYSTEM_DEFINITION_MESSAGE_ID;
import static com.dell.cpsd.paqx.dne.service.delegates.utils.DelegateConstants.NODE_DETAILS;
import static com.dell.cpsd.paqx.dne.service.delegates.utils.DelegateConstants.SEND_ADD_NODES_TO_SYSTEM_DEFINITION_FAILED;

@org.springframework.stereotype.Component
@Scope("prototype")
@Qualifier("sendAddNodesToSystemDefinition")
public class SendAddNodesToSystemDefinition extends BaseWorkflowDelegate
{
    private static final Logger LOGGER = LoggerFactory.getLogger(SendAddNodesToSystemDefinition.class);

    private final RuntimeService        runtimeService;
    private final AMQPClient            sdkAMQPClient;
    private final DataServiceRepository repository;
    private final ExecutorService       taskExecutorService;
    private final NodeService           nodeService;

    /**
     * SendAddNodesToSystemDefinition constructor.
     *
     * @param runtimeService      - The <code>RuntimeService</code> instance.
     * @param sdkAMQPClient       - The <code>AMQPClient</code> instance.
     * @param repository          - The <code>DataServiceRepository</code> instance.
     * @param taskExecutorService - The <code>ExecutorService</code> instance.
     * @since 1.0
     */
    @Autowired
    public SendAddNodesToSystemDefinition(final RuntimeService runtimeService, final AMQPClient sdkAMQPClient,
            final DataServiceRepository repository,
            @Qualifier("addNodesToSystemDefinitionTaskExecutorService") final ExecutorService taskExecutorService,
            NodeService nodeService)
    {
        super(LOGGER, "Send Add Nodes To System Definition");
        this.runtimeService = runtimeService;
        this.sdkAMQPClient = sdkAMQPClient;
        this.repository = repository;
        this.taskExecutorService = taskExecutorService;
        this.nodeService = nodeService;
    }

    @Override
    public void delegateExecute(final DelegateExecution delegateExecution)
    {
        final List<NodeDetail> nodeDetails = (List<NodeDetail>) delegateExecution.getVariable(NODE_DETAILS);

        if (CollectionUtils.isEmpty(nodeDetails))
        {
            final String message = this.taskName + " no completed nodes to add to System Definition";
            updateDelegateStatus(message);
            throw new BpmnError(SEND_ADD_NODES_TO_SYSTEM_DEFINITION_FAILED, message);
        }

        updateDelegateStatus("Attempting " + this.taskName + " on the following Nodes " + StringUtils
                .join(nodeDetails.stream().map(NodeDetail::getServiceTag).collect(Collectors.toList()) + "."));

        try
        {
            // As system definition creates a new UUID when node is added, it does not match the uuid on node
            // discovery side. So mac address is used to correlate a given node in node discovery and system
            // definition. Once the defect is resolved where the two uuids match, node can correlated using uuid.
            //
            // Also, mac address is used by RCM to populate the current (firmware) versions in
            // RCM collections job. So mac address is required.
            nodeDetails.stream().filter(Objects::nonNull).forEach(node -> {
                String macAddress = NodeInventoryParsingUtil.parseToFetchMacAddress(nodeService.getNodeInventoryData(node.getId()));
                node.setMacAddress(macAddress);
            });

            AddNodesToSystemDefinitionRunnable runnable = new AddNodesToSystemDefinitionRunnable(delegateExecution.getProcessInstanceId(),
                    ADD_NODES_TO_SYSTEM_DEFINITION_ACTIVITY_ID, ADD_NODES_TO_SYSTEM_DEFINITION_MESSAGE_ID, nodeDetails,
                    runtimeService, sdkAMQPClient, repository);

            taskExecutorService.execute(runnable);

        }
        catch (Exception ex)
        {
            final String message = this.taskName + " failed! Reason: ";
            updateDelegateStatus(message, ex);
            throw new BpmnError(SEND_ADD_NODES_TO_SYSTEM_DEFINITION_FAILED, message + ex.getMessage());
        }

        final String message = this.taskName + " was successful.";
        updateDelegateStatus(message);
    }

}
