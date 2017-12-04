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

import static com.dell.cpsd.paqx.dne.service.delegates.utils.DelegateConstants.COMPLETE_PERFORMANCE_TUNE_SCALEIO_VM_FAILED;
import static com.dell.cpsd.paqx.dne.service.delegates.utils.DelegateConstants.NODE_DETAIL;
import static com.dell.cpsd.paqx.dne.service.delegates.utils.DelegateConstants.PERFORMANCE_TUNE_SCALEIO_VM_MESSAGE_ID;

@Component
@Scope("prototype")
@Qualifier("completePerformanceTuneScaleIoVm")
public class CompletePerformanceTuneScaleIoVm extends BaseWorkflowDelegate
{
    private static final Logger LOGGER = LoggerFactory.getLogger(CompletePerformanceTuneScaleIoVm.class);

    private final AsynchronousNodeService asynchronousNodeService;

    @Autowired
    public CompletePerformanceTuneScaleIoVm(final AsynchronousNodeService asynchronousNodeService)
    {
        this.asynchronousNodeService = asynchronousNodeService;
    }

    @Override
    public void delegateExecute(final DelegateExecution delegateExecution)
    {
        String taskName = "Complete Performance Tune ScaleIo Vm";
        LOGGER.info(taskName);
        NodeDetail nodeDetail = (NodeDetail) delegateExecution.getVariable(NODE_DETAIL);

        AsynchronousNodeServiceCallback<?> responseCallback = (AsynchronousNodeServiceCallback<?>) delegateExecution
                .getVariable(PERFORMANCE_TUNE_SCALEIO_VM_MESSAGE_ID);
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
                LOGGER.error(message, e);
                updateDelegateStatus(message + e.getMessage());
                throw new BpmnError(COMPLETE_PERFORMANCE_TUNE_SCALEIO_VM_FAILED, message + e.getMessage());
            }
        }

        if (status == null || !"success".equalsIgnoreCase(status.toString()))
        {
            final String message = taskName + " on Node " + nodeDetail.getServiceTag() + " failed!";
            LOGGER.error(message);
            updateDelegateStatus(message);
            throw new BpmnError(COMPLETE_PERFORMANCE_TUNE_SCALEIO_VM_FAILED, message);
        }

        String returnMessage = taskName + " on Node " + nodeDetail.getServiceTag() + " was successful.";
        LOGGER.info(returnMessage);
        updateDelegateStatus(returnMessage);
    }
}
