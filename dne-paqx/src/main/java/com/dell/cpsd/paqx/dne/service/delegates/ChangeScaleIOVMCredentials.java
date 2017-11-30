/**
 * <p>
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries. All Rights Reserved. Dell EMC Confidential/Proprietary Information
 * </p>
 */

package com.dell.cpsd.paqx.dne.service.delegates;

import com.dell.cpsd.paqx.dne.exception.TaskResponseFailureException;
import com.dell.cpsd.paqx.dne.service.NodeService;
import com.dell.cpsd.paqx.dne.service.delegates.model.DelegateRequestModel;
import com.dell.cpsd.paqx.dne.transformers.RemoteCommandExecutionRequestTransformer;
import com.dell.cpsd.virtualization.capabilities.api.RemoteCommandExecutionRequestMessage;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import static com.dell.cpsd.paqx.dne.service.delegates.utils.DelegateConstants.CHANGE_SCALEIO_VM_CREDENTIALS_FAILED;

/**
 * Change ScaleIo VM credentials.
 * <p>
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries. All Rights Reserved. Dell EMC Confidential/Proprietary Information
 * </p>
 *
 * @version 1.0
 * @since 1.0
 */
@Component
@Scope("prototype")
@Qualifier("changeScaleIOVMCredentials")
public class ChangeScaleIOVMCredentials extends BaseWorkflowDelegate
{
    /*
     * The logger instance
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(ChangeScaleIOVMCredentials.class);

    /*
     * The <code>NodeService</code> instance
     */
    private final NodeService                              nodeService;
    private final RemoteCommandExecutionRequestTransformer remoteCommandExecutionRequestTransformer;

    /**
     * ChangeSvmCredentials constructor.
     *
     * @param nodeService                              - The <code>NodeService</code> instance
     * @param remoteCommandExecutionRequestTransformer
     */
    @Autowired
    public ChangeScaleIOVMCredentials(final NodeService nodeService,
            final RemoteCommandExecutionRequestTransformer remoteCommandExecutionRequestTransformer)
    {
        this.nodeService = nodeService;
        this.remoteCommandExecutionRequestTransformer = remoteCommandExecutionRequestTransformer;
    }

    @Override
    public void delegateExecute(final DelegateExecution delegateExecution)
    {
        LOGGER.info("Execute ChangeSvmCredentials task");
        final String taskMessage = "Change ScaleIO VM factory credentials";

        try
        {
            final DelegateRequestModel<RemoteCommandExecutionRequestMessage> delegateRequestModel = remoteCommandExecutionRequestTransformer
                    .buildRemoteCodeExecutionRequest(delegateExecution, RemoteCommandExecutionRequestMessage.RemoteCommand.CHANGE_PASSWORD);
            this.nodeService.requestRemoteCommandExecution(delegateRequestModel.getRequestMessage());

            final String returnMessage = taskMessage + " on Node " + delegateRequestModel.getServiceTag() + " was successful.";
            LOGGER.info(returnMessage);
            updateDelegateStatus(returnMessage);
        }
        catch (TaskResponseFailureException ex)
        {
            updateDelegateStatus(ex.getMessage());
            throw new BpmnError(CHANGE_SCALEIO_VM_CREDENTIALS_FAILED, "Exception Code: " + ex.getCode() + "::" + ex.getMessage());
        }
        catch (Exception ex)
        {
            String errorMessage = "An unexpected exception occurred attempting to request " + taskMessage + ". Reason: ";
            LOGGER.error(errorMessage, ex);
            updateDelegateStatus(errorMessage + ex.getMessage());
            throw new BpmnError(CHANGE_SCALEIO_VM_CREDENTIALS_FAILED, errorMessage + ex.getMessage());
        }
    }
}
