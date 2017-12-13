/**
 * <p>
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries. All Rights Reserved. Dell EMC Confidential/Proprietary Information
 * </p>
 */

package com.dell.cpsd.paqx.dne.service.delegates;

import com.dell.cpsd.paqx.dne.service.delegates.model.NodeDetail;
import com.dell.cpsd.sdk.AMQPClient;
import com.dell.cpsd.service.system.definition.api.ComponentsFilter;
import com.dell.cpsd.service.system.definition.api.ConvergedSystem;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.dell.cpsd.paqx.dne.service.delegates.utils.DelegateConstants.ADD_NODE_TO_SYSTEM_DEFINITION_MESSAGE_ID;
import static com.dell.cpsd.paqx.dne.service.delegates.utils.DelegateConstants.COMPLETE_ADD_NODE_TO_SYSTEM_DEFINITION_FAILED;
import static com.dell.cpsd.paqx.dne.service.delegates.utils.DelegateConstants.NODE_DETAIL;
import static com.dell.cpsd.paqx.dne.service.delegates.utils.DelegateConstants.SUCCEEDED;

@Component
@Scope("prototype")
@Qualifier("completeAddNodeToSystemDefinition")
public class CompleteAddNodeToSystemDefinition extends BaseWorkflowDelegate
{
    private static final Logger LOGGER = LoggerFactory.getLogger(CompleteAddNodeToSystemDefinition.class);

    private final AMQPClient sdkAMQPClient;

    @Autowired
    public CompleteAddNodeToSystemDefinition(final AMQPClient sdkAMQPClient)
    {
        super(LOGGER, "Complete Add Node To System Definition");
        this.sdkAMQPClient = sdkAMQPClient;
    }

    @Override
    public void delegateExecute(final DelegateExecution delegateExecution)
    {
        NodeDetail nodeDetail = (NodeDetail) delegateExecution.getVariable(NODE_DETAIL);
        updateDelegateStatus("Attempting " + this.taskName + " on Node " + nodeDetail.getServiceTag() + ".");

        try
        {
            String runnableResult = (String) delegateExecution.getVariable(ADD_NODE_TO_SYSTEM_DEFINITION_MESSAGE_ID);
            if (StringUtils.isEmpty(runnableResult) || !runnableResult.equals(SUCCEEDED))
            {
                throw new IllegalStateException(runnableResult);
            }

            final List<ConvergedSystem> allConvergedSystems = this.sdkAMQPClient.getConvergedSystems();
            if (CollectionUtils.isEmpty(allConvergedSystems))
            {
                throw new IllegalStateException("No converged systems found.");
            }

            final ConvergedSystem system = allConvergedSystems.get(0);
            final ComponentsFilter componentsFilter = new ComponentsFilter();
            componentsFilter.setSystemUuid(system.getUuid());

            // Need to make sure the node was really added.
            // The SDK has a habit of swallowing exceptions making it difficult to know
            // if there was an error...
            final List<ConvergedSystem> updateSystemDetails = this.sdkAMQPClient.getComponents(componentsFilter);
            final ConvergedSystem systemThatWasUpdated = updateSystemDetails.get(0);

            final com.dell.cpsd.service.system.definition.api.Component newComponent = systemThatWasUpdated.getComponents().stream()
                    .filter(c -> c.getIdentity().getIdentifier().equals(nodeDetail.getId())).findFirst().orElse(null);

            if (newComponent == null)
            {
                throw new IllegalStateException("The discovered node was not added to the system definition.");
            }
        }
        catch (Exception e)
        {
            final String message = taskName + " " + nodeDetail.getServiceTag() + " failed!  Reason: ";
            updateDelegateStatus(message, e);
            throw new BpmnError(COMPLETE_ADD_NODE_TO_SYSTEM_DEFINITION_FAILED, message + e.getMessage());
        }

        updateDelegateStatus(taskName + " on Node " + nodeDetail.getServiceTag() + " was successful.");
    }
}
