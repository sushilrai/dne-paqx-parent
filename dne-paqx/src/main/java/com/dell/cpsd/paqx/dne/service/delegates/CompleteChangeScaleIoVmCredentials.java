/**
 * <p>
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries. All Rights Reserved. Dell EMC Confidential/Proprietary Information
 * </p>
 */

package com.dell.cpsd.paqx.dne.service.delegates;

import com.dell.cpsd.paqx.dne.amqp.callback.AsynchronousNodeServiceCallback;
import com.dell.cpsd.paqx.dne.service.AsynchronousNodeService;
import com.dell.cpsd.paqx.dne.service.delegates.model.NodeDetail;
import com.dell.cpsd.virtualization.capabilities.api.RemoteCommandExecutionResponseMessage;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import static com.dell.cpsd.paqx.dne.service.delegates.utils.DelegateConstants.CHANGE_SCALEIO_VM_CREDENTIALS_MESSAGE_ID;
import static com.dell.cpsd.paqx.dne.service.delegates.utils.DelegateConstants.COMPLETE_CHANGE_SCALEIO_VM_CREDENTIALS_FAILED;
import static com.dell.cpsd.paqx.dne.service.delegates.utils.DelegateConstants.NODE_DETAIL;

@Component
@Scope("prototype")
@Qualifier("completeChangeScaleIoVmCredentials")
public class CompleteChangeScaleIoVmCredentials extends BaseWorkflowDelegate
{
    private static final Logger LOGGER = LoggerFactory.getLogger(CompleteChangeScaleIoVmCredentials.class);

    private final AsynchronousNodeService asynchronousNodeService;

    @Autowired
    public CompleteChangeScaleIoVmCredentials(final AsynchronousNodeService asynchronousNodeService)
    {
        super(LOGGER, "Complete Change ScaleIo Vm Credentials");
        this.asynchronousNodeService = asynchronousNodeService;
    }

    @Override
    public void delegateExecute(final DelegateExecution delegateExecution)
    {
        NodeDetail nodeDetail = (NodeDetail) delegateExecution.getVariable(NODE_DETAIL);
        updateDelegateStatus("Attempting " + this.taskName + " on Node " + nodeDetail.getServiceTag() + ".");

        AsynchronousNodeServiceCallback<?> responseCallback = (AsynchronousNodeServiceCallback<?>) delegateExecution
                .getVariable(CHANGE_SCALEIO_VM_CREDENTIALS_MESSAGE_ID);
        RemoteCommandExecutionResponseMessage.Status status = null;
        if (responseCallback != null && responseCallback.isDone())
        {
            try
            {

                status = this.asynchronousNodeService.processRemoteCommandResponse(responseCallback);
            }
            catch (Exception e)
            {
                final String message = taskName + " " + nodeDetail.getServiceTag() + " failed!  Reason: ";
                updateDelegateStatus(message, e);
                throw new BpmnError(COMPLETE_CHANGE_SCALEIO_VM_CREDENTIALS_FAILED, message + e.getMessage());
            }
        }

        if (status == null || !"success".equalsIgnoreCase(status.toString()))
        {
            final String message = taskName + " on Node " + nodeDetail.getServiceTag() + " failed!";
            updateDelegateStatus(message);
            throw new BpmnError(COMPLETE_CHANGE_SCALEIO_VM_CREDENTIALS_FAILED, message);
        }

        String returnMessage = taskName + " on Node " + nodeDetail.getServiceTag() + " was successful.";
        updateDelegateStatus(returnMessage);
    }
}
