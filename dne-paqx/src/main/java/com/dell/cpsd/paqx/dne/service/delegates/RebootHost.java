/**
 * <p>
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries. All Rights Reserved. Dell EMC Confidential/Proprietary Information
 * </p>
 */

package com.dell.cpsd.paqx.dne.service.delegates;

import com.dell.cpsd.paqx.dne.exception.TaskResponseFailureException;
import com.dell.cpsd.paqx.dne.service.NodeService;
import com.dell.cpsd.paqx.dne.service.delegates.model.DelegateRequestModel;
import com.dell.cpsd.paqx.dne.transformers.HostPowerOperationsTransformer;
import com.dell.cpsd.virtualization.capabilities.api.HostPowerOperationRequestMessage;
import com.dell.cpsd.virtualization.capabilities.api.PowerOperationRequest;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import static com.dell.cpsd.paqx.dne.service.delegates.utils.DelegateConstants.REBOOT_HOST_FAILED;

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
     * The <code>NodeService</code> instance
     */
    private final NodeService                    nodeService;
    private final HostPowerOperationsTransformer hostPowerOperationsRequestTransformer;

    @Autowired
    public RebootHost(final NodeService nodeService, final HostPowerOperationsTransformer hostPowerOperationsRequestTransformer)
    {
        this.nodeService = nodeService;
        this.hostPowerOperationsRequestTransformer = hostPowerOperationsRequestTransformer;
    }

    @Override
    public void delegateExecute(final DelegateExecution delegateExecution)
    {
        LOGGER.info("Execute Reboot Host task");
        final String taskMessage = "Reboot Host";

        try
        {
            final DelegateRequestModel<HostPowerOperationRequestMessage> delegateRequestModel = hostPowerOperationsRequestTransformer
                    .buildHostPowerOperationsRequestMessage(delegateExecution, PowerOperationRequest.PowerOperation.REBOOT);
            this.nodeService.requestHostReboot(delegateRequestModel.getRequestMessage());

            final String returnMessage = taskMessage + " on Node " + delegateRequestModel.getServiceTag() + " was successful.";
            LOGGER.info(returnMessage);
            updateDelegateStatus(returnMessage);
        }
        catch (TaskResponseFailureException ex)
        {
            updateDelegateStatus(ex.getMessage());
            throw new BpmnError(REBOOT_HOST_FAILED, "Exception Code: " + ex.getCode() + "::" + ex.getMessage());
        }
        catch (Exception ex)
        {
            String errorMessage = "An Unexpected Exception occurred attempting to request " + taskMessage + ". Reason: ";
            LOGGER.error(errorMessage, ex);
            updateDelegateStatus(errorMessage + ex.getMessage());
            throw new BpmnError(REBOOT_HOST_FAILED, errorMessage + ex.getMessage());
        }
    }
}
