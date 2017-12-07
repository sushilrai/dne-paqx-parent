/**
 * <p>
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries. All Rights Reserved. Dell EMC Confidential/Proprietary Information
 * </p>
 */

package com.dell.cpsd.paqx.dne.service.delegates;

import com.dell.cpsd.paqx.dne.amqp.callback.AsynchronousNodeServiceCallback;
import com.dell.cpsd.paqx.dne.service.AsynchronousNodeService;
import com.dell.cpsd.paqx.dne.service.delegates.model.NodeDetail;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import static com.dell.cpsd.paqx.dne.service.delegates.utils.DelegateConstants.NODE_DETAIL;
import static com.dell.cpsd.paqx.dne.service.delegates.utils.DelegateConstants.REBOOT_HOST_FAILED;
import static com.dell.cpsd.paqx.dne.service.delegates.utils.DelegateConstants.REBOOT_HOST_MESSAGE_ID;

@Component
@Scope("prototype")
@Qualifier("rebootHost")
public class RebootHost extends BaseWorkflowDelegate
{
    /**
     * The logger instance
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(RebootHost.class);

    /**
     * The <code>AsynchronousNodeService</code> instance
     */
    private final AsynchronousNodeService asynchronousNodeService;

    public RebootHost(final AsynchronousNodeService asynchronousNodeService)
    {
        super(LOGGER, "Reboot Host");
        this.asynchronousNodeService = asynchronousNodeService;
    }

    @Override
    public void delegateExecute(final DelegateExecution delegateExecution)
    {
        try
        {
            final NodeDetail nodeDetail = (NodeDetail) delegateExecution.getVariable(NODE_DETAIL);
            updateDelegateStatus("Attempting " + this.taskName + " on Node " + nodeDetail.getServiceTag() + ".");

            final AsynchronousNodeServiceCallback<?> responseCallback = (AsynchronousNodeServiceCallback<?>) delegateExecution
                    .getVariable(REBOOT_HOST_MESSAGE_ID);
            this.asynchronousNodeService.processRebootHostResponse(responseCallback);

            updateDelegateStatus(taskName + " on Node " + nodeDetail.getServiceTag() + " was successful.");
        }
        catch (Exception ex)
        {
            String errorMessage = "An Unexpected Exception occurred attempting to request " + taskName + ". Reason: ";
            updateDelegateStatus(errorMessage, ex);
            throw new BpmnError(REBOOT_HOST_FAILED, errorMessage + ex.getMessage());
        }
    }
}
