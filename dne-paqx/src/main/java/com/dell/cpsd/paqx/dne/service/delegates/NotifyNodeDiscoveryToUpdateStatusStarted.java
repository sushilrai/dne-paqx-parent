/**
 * <p>
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries. All Rights Reserved. Dell EMC Confidential/Proprietary Information
 * </p>
 */

package com.dell.cpsd.paqx.dne.service.delegates;

import com.dell.cpsd.paqx.dne.domain.node.DiscoveredNodeInfo;
import com.dell.cpsd.paqx.dne.service.NodeService;
import com.dell.cpsd.paqx.dne.service.delegates.model.NodeDetail;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;
import static com.dell.cpsd.paqx.dne.service.delegates.utils.DelegateConstants.NODE_DETAIL;
import static com.dell.cpsd.paqx.dne.service.delegates.utils.DelegateConstants.NOTIFY_NODE_STATUS_STARTED_FAILED;
import static com.dell.cpsd.paqx.dne.service.delegates.utils.DelegateConstants.STARTED;

@Component
@Scope("prototype")
@Qualifier("notifyNodeDiscoveryToUpdateStatusStarted")
public class NotifyNodeDiscoveryToUpdateStatusStarted extends BaseWorkflowDelegate
{
    /*
     * The logger instance
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(NotifyNodeDiscoveryToUpdateStatusStarted.class);

    /*
     * The <code>NodeService</code> instance
     */
    private final NodeService nodeService;

    /**
     * NotifyNodeDiscoveryToUpdateStatusStarted constructor.
     *
     * @param nodeService - The <code>NodeService</code> instance.
     * @since 1.0
     */
    @Autowired
    public NotifyNodeDiscoveryToUpdateStatusStarted(NodeService nodeService)
    {
        super(LOGGER, "Update Node Status to Started");
        this.nodeService = nodeService;
    }

    @Override
    public void delegateExecute(final DelegateExecution delegateExecution)
    {
        final NodeDetail nd = (NodeDetail) delegateExecution.getVariable(NODE_DETAIL);
        updateDelegateStatus("Attempting " + this.taskName + " on Node " + nd.getServiceTag() + ".");

        boolean succeeded = false;
        try
        {
            List<DiscoveredNodeInfo> discoveredNodes = nodeService.listDiscoveredNodeInfo();
            List<String> nodes = discoveredNodes.stream().filter(dn -> dn.getSymphonyUuid().equals(nd.getId())).map(dn ->new String(dn.getSymphonyUuid())).collect(Collectors.toList());
            if(nodes!=null && nodes.contains(nd.getId())) {
                succeeded = this.nodeService.notifyNodeAllocationStatus(nd.getId(), STARTED);
            }
            else {
                updateDelegateStatus("Node Already added.");
            }
        }
        catch (Exception e)
        {
            final String message =
                    "An Unexpected Exception occurred attempting to " + nd.getServiceTag() + " on Node " + nd.getServiceTag()
                    + ". Reason: ";
            updateDelegateStatus(message, e);
            throw new BpmnError(NOTIFY_NODE_STATUS_STARTED_FAILED, message + e.getMessage());
        }
        if (!succeeded)
        {
            final String message = "Updating Node Status on Node " + nd.getServiceTag() + " failed.";
            updateDelegateStatus(message);
            throw new BpmnError(NOTIFY_NODE_STATUS_STARTED_FAILED, message);
        }

        updateDelegateStatus(taskName + " on Node " + nd.getServiceTag() + " was successful.");
    }
}
