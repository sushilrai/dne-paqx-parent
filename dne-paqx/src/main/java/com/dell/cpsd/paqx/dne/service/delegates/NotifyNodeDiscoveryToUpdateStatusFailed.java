/**
 * <p>
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries. All Rights Reserved. Dell EMC Confidential/Proprietary Information
 * </p>
 */

package com.dell.cpsd.paqx.dne.service.delegates;

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

import static com.dell.cpsd.paqx.dne.service.delegates.utils.DelegateConstants.FAILED;
import static com.dell.cpsd.paqx.dne.service.delegates.utils.DelegateConstants.NODE_DETAIL;
import static com.dell.cpsd.paqx.dne.service.delegates.utils.DelegateConstants.NOTIFY_NODE_STATUS_UPDATE_FAILED;

@Component
@Scope("prototype")
@Qualifier("notifyNodeDiscoveryToUpdateStatusFailed")
public class NotifyNodeDiscoveryToUpdateStatusFailed extends BaseWorkflowDelegate
{
    /*
     * The logger instance
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(NotifyNodeDiscoveryToUpdateStatusFailed.class);

    /*
     * The <code>NodeService</code> instance
     */
    private final NodeService nodeService;

    /**
     * NotifyNodeDiscoveryToUpdateStatusFailed constructor.
     *
     * @param nodeService - The <code>NodeService</code> instance.
     * @since 1.0
     */
    @Autowired
    public NotifyNodeDiscoveryToUpdateStatusFailed(NodeService nodeService)
    {
        super(LOGGER, "Update Node Status to Failed");
        this.nodeService = nodeService;
    }

    @Override
    public void delegateExecute(final DelegateExecution delegateExecution)
    {
        final NodeDetail nd = (NodeDetail) delegateExecution.getVariable(NODE_DETAIL);
        updateDelegateStatus("Attempting " + this.taskName + " on Node " + nd.getServiceTag() + ".");

        boolean succeeded;
        try
        {
            succeeded = this.nodeService.notifyNodeAllocationStatus(nd.getId(), FAILED);
        }
        catch (Exception e)
        {
            final String message =
                    "An Unexpected Exception occurred attempting to " + nd.getServiceTag() + " on Node " + nd.getServiceTag()
                    + ". Reason: ";
            updateDelegateStatus(message, e);
            throw new BpmnError(NOTIFY_NODE_STATUS_UPDATE_FAILED, message + e.getMessage());
        }
        if (!succeeded)
        {
            final String message = "Updating Node Status on Node " + nd.getServiceTag() + " failed.";
            LOGGER.error(message);
            updateDelegateStatus(message);
            throw new BpmnError(NOTIFY_NODE_STATUS_UPDATE_FAILED, message);
        }

        updateDelegateStatus(taskName + " on Node " + nd.getServiceTag() + " was successful.");
    }
}
