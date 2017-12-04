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

import static com.dell.cpsd.paqx.dne.service.delegates.utils.DelegateConstants.NODE_DETAIL;
import static com.dell.cpsd.paqx.dne.service.delegates.utils.DelegateConstants.PERFORMANCE_TUNE_SCALEIO_VM_MESSAGE_ID;
import static com.dell.cpsd.paqx.dne.service.delegates.utils.DelegateConstants.SEND_PERFORMANCE_TUNE_SCALEIO_VM_FAILED;

@Component
@Scope("prototype")
@Qualifier("sendPerformanceTuneScaleIoVm")
public class SendPerformanceTuneScaleIoVm extends BaseWorkflowDelegate
{
    private static final String ACTIVITY_ID = "receivePerformanceTuneScaleIoVm";

    /**
     * The logger instance
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(SendPerformanceTuneScaleIoVm.class);

    /**
     * The <code>NodeService</code> instance
     */
    private final AsynchronousNodeService                  asynchronousNodeService;
    private final RemoteCommandExecutionRequestTransformer remoteCommandExecutionRequestTransformer;

    @Autowired
    public SendPerformanceTuneScaleIoVm(AsynchronousNodeService asynchronousNodeService,
            RemoteCommandExecutionRequestTransformer remoteCommandExecutionRequestTransformer)
    {
        this.asynchronousNodeService = asynchronousNodeService;
        this.remoteCommandExecutionRequestTransformer = remoteCommandExecutionRequestTransformer;
    }

    @Override
    public void delegateExecute(final DelegateExecution delegateExecution)
    {
        LOGGER.info("Execute Send Performance Tune ScaleIo Vm task");
        final String taskMessage = "Send Performance Tune ScaleIo Vm";

        NodeDetail nodeDetail = (NodeDetail) delegateExecution.getVariable(NODE_DETAIL);

        AsynchronousNodeServiceCallback<?> requestCallback;

        final DelegateRequestModel<RemoteCommandExecutionRequestMessage> delegateRequestModel = remoteCommandExecutionRequestTransformer
                .buildRemoteCodeExecutionRequest(delegateExecution,
                        RemoteCommandExecutionRequestMessage.RemoteCommand.PERFORMANCE_TUNING_SVM);

        try
        {
            requestCallback = this.asynchronousNodeService
                    .executeRemoteCommand(delegateExecution.getProcessInstanceId(), ACTIVITY_ID,
                            PERFORMANCE_TUNE_SCALEIO_VM_MESSAGE_ID, delegateRequestModel.getRequestMessage());
        }
        catch (TaskResponseFailureException ex)
        {
            final String message =
                    "An Unexpected Exception Occurred attempting to Send Performance Tune ScaleIo Vm on Node " + nodeDetail.getServiceTag();
            LOGGER.error(message, ex);
            updateDelegateStatus(message);
            throw new BpmnError(SEND_PERFORMANCE_TUNE_SCALEIO_VM_FAILED,
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
            final String message = "Failed to send the request for Send Performance Tune ScaleIo Vm on Node " + nodeDetail.getServiceTag();
            LOGGER.error(message);
            updateDelegateStatus(message);
            throw new BpmnError(SEND_PERFORMANCE_TUNE_SCALEIO_VM_FAILED, message);
        }
    }
}
