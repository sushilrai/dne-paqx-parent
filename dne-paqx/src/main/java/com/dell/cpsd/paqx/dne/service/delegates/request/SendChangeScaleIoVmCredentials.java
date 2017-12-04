/**
 * <p>
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries. All Rights Reserved. Dell EMC Confidential/Proprietary Information
 * </p>
 */

package com.dell.cpsd.paqx.dne.service.delegates.request;

import com.dell.cpsd.paqx.dne.amqp.callback.AsynchronousNodeServiceCallback;
import com.dell.cpsd.paqx.dne.exception.TaskResponseFailureException;
import com.dell.cpsd.paqx.dne.service.AsynchronousNodeService;
import com.dell.cpsd.paqx.dne.service.delegates.BaseWorkflowDelegate;
import com.dell.cpsd.paqx.dne.service.delegates.model.DelegateRequestModel;
import com.dell.cpsd.paqx.dne.service.delegates.model.NodeDetail;
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

import static com.dell.cpsd.paqx.dne.service.delegates.utils.DelegateConstants.CHANGE_SCALEIO_VM_CREDENTIALS_MESSAGE_ID;
import static com.dell.cpsd.paqx.dne.service.delegates.utils.DelegateConstants.NODE_DETAIL;
import static com.dell.cpsd.paqx.dne.service.delegates.utils.DelegateConstants.SEND_CHANGE_SCALEIO_VM_CREDENTIALS_FAILED;

@Component
@Scope("prototype")
@Qualifier("sendChangeScaleIoVmCredentials")
public class SendChangeScaleIoVmCredentials extends BaseWorkflowDelegate
{
    private static final String ACTIVITY_ID = "receiveChangeScaleIoVmCredentials";

    /**
     * The logger instance
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(SendChangeScaleIoVmCredentials.class);

    /**
     * The <code>NodeService</code> instance
     */
    private final AsynchronousNodeService                  asynchronousNodeService;
    private final RemoteCommandExecutionRequestTransformer remoteCommandExecutionRequestTransformer;

    @Autowired
    public SendChangeScaleIoVmCredentials(AsynchronousNodeService asynchronousNodeService,
            RemoteCommandExecutionRequestTransformer remoteCommandExecutionRequestTransformer)
    {
        this.asynchronousNodeService = asynchronousNodeService;
        this.remoteCommandExecutionRequestTransformer = remoteCommandExecutionRequestTransformer;
    }

    @Override
    public void delegateExecute(final DelegateExecution delegateExecution)
    {
        LOGGER.info("Execute Send Change ScaleIo Vm Credentials task");
        final String taskMessage = "Send Change ScaleIo Vm Credentials";

        NodeDetail nodeDetail = (NodeDetail) delegateExecution.getVariable(NODE_DETAIL);

        AsynchronousNodeServiceCallback<?> requestCallback;

        final DelegateRequestModel<RemoteCommandExecutionRequestMessage> delegateRequestModel = remoteCommandExecutionRequestTransformer
                .buildRemoteCodeExecutionRequest(delegateExecution,
                        RemoteCommandExecutionRequestMessage.RemoteCommand.CHANGE_PASSWORD);

        try
        {
            requestCallback = this.asynchronousNodeService
                    .executeRemoteCommand(delegateExecution.getProcessInstanceId(), ACTIVITY_ID,
                            CHANGE_SCALEIO_VM_CREDENTIALS_MESSAGE_ID, delegateRequestModel.getRequestMessage());
        }
        catch (TaskResponseFailureException ex)
        {
            final String message =
                    "An Unexpected Exception Occurred attempting to Send Change ScaleIo Vm Credentials on Node " + nodeDetail.getServiceTag();
            LOGGER.error(message, ex);
            updateDelegateStatus(message);
            throw new BpmnError(SEND_CHANGE_SCALEIO_VM_CREDENTIALS_FAILED,
                    taskMessage + " on Node " + nodeDetail.getServiceTag() + " failed!  Reason: " + ex.getMessage());
        }

        if (requestCallback != null)
        {
            final String message = taskMessage + " on Node " + nodeDetail.getServiceTag() + " was successful";
            LOGGER.info(message);
            updateDelegateStatus(message);
        }
        else
        {
            final String message = "Failed to send the request for Send Change ScaleIo Vm Credentials on Node " + nodeDetail.getServiceTag();
            LOGGER.error(message);
            updateDelegateStatus(message);
            throw new BpmnError(SEND_CHANGE_SCALEIO_VM_CREDENTIALS_FAILED, message);
        }
    }
}
