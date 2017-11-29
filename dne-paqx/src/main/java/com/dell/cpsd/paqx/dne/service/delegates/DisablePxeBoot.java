/**
 * <p>
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries. All Rights Reserved. Dell EMC Confidential/Proprietary Information
 * </p>
 */

package com.dell.cpsd.paqx.dne.service.delegates;

import com.dell.cpsd.paqx.dne.amqp.callback.AsynchronousNodeServiceCallback;
import com.dell.cpsd.paqx.dne.exception.TaskResponseFailureException;
import com.dell.cpsd.paqx.dne.service.AsynchronousNodeService;
import com.dell.cpsd.paqx.dne.service.delegates.model.NodeDetail;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import static com.dell.cpsd.paqx.dne.service.delegates.utils.DelegateConstants.CONFIGURE_PXE_BOOT_MESSAGE_ID;
import static com.dell.cpsd.paqx.dne.service.delegates.utils.DelegateConstants.CONFIGURE_PXE_FAILED;
import static com.dell.cpsd.paqx.dne.service.delegates.utils.DelegateConstants.NODE_DETAIL;

/**
 * Task responsible for handling Boot Order Sequence
 * <p>
 * <p>
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries. All Rights Reserved. Dell EMC Confidential/Proprietary Information
 * </p>
 *
 * @since 1.0
 */
@Component
@Scope("prototype")
@Qualifier("disablePxeBoot")
public class DisablePxeBoot extends BaseWorkflowDelegate
{
    /**
     * The logger instance
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(DisablePxeBoot.class);

    /**
     * The <code>AsynchronousNodeService</code> instance
     */
    private final AsynchronousNodeService asynchronousNodeService;

    public DisablePxeBoot(final AsynchronousNodeService asynchronousNodeService)
    {
        this.asynchronousNodeService = asynchronousNodeService;
    }

    @Override
    public void delegateExecute(final DelegateExecution delegateExecution)
    {
        LOGGER.info("Execute configure PXE task");
        final String taskMessage = "Configure PXE boot";

        try
        {
            final NodeDetail nodeDetail = (NodeDetail) delegateExecution.getVariable(NODE_DETAIL);
            final AsynchronousNodeServiceCallback<?> responseCallback = (AsynchronousNodeServiceCallback<?>) delegateExecution
                    .getVariable(CONFIGURE_PXE_BOOT_MESSAGE_ID);
            this.asynchronousNodeService.processConfigurePxeBootResponse(responseCallback);

            final String returnMessage = taskMessage + " on Node " + nodeDetail.getServiceTag() + " was successful.";
            LOGGER.info(returnMessage);
            updateDelegateStatus(returnMessage);
        }
        catch (TaskResponseFailureException ex)
        {
            final String errorMessage = "Exception Code: " + ex.getCode() + "::" + ex.getMessage();
            updateDelegateStatus(ex.getMessage());
            LOGGER.error(errorMessage);
            throw new BpmnError(CONFIGURE_PXE_FAILED, errorMessage);
        }
        catch (Exception ex)
        {
            String errorMessage = "An Unexpected Exception occurred attempting to request " + taskMessage + ". Reason: ";
            LOGGER.error(errorMessage, ex);
            updateDelegateStatus(errorMessage + ex.getMessage());
            throw new BpmnError(CONFIGURE_PXE_FAILED, errorMessage + ex.getMessage());
        }
    }
}
