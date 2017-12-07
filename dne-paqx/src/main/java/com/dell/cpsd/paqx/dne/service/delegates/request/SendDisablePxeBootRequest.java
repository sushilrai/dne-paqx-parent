/**
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries.  All Rights Reserved.
 * Dell EMC Confidential/Proprietary Information
 **/

package com.dell.cpsd.paqx.dne.service.delegates.request;

import com.dell.cpsd.ConfigurePxeBootRequestMessage;
import com.dell.cpsd.paqx.dne.amqp.callback.AsynchronousNodeServiceCallback;
import com.dell.cpsd.paqx.dne.service.AsynchronousNodeService;
import com.dell.cpsd.paqx.dne.service.delegates.BaseWorkflowDelegate;
import com.dell.cpsd.paqx.dne.service.delegates.model.DelegateRequestModel;
import com.dell.cpsd.paqx.dne.service.delegates.model.NodeDetail;
import com.dell.cpsd.paqx.dne.transformers.ConfigurePxeBootRequestTransformer;
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
 * Send disable PXE boot request
 * <p>
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries. All Rights Reserved.
 * Dell EMC Confidential/Proprietary Information
 * </p>
 *
 * @version 1.0
 * @since 1.0
 */
@Component
@Scope("prototype")
@Qualifier("sendDisablePxeBootRequest")
public class SendDisablePxeBootRequest extends BaseWorkflowDelegate
{
    private static final Logger LOGGER = LoggerFactory.getLogger(SendDisablePxeBootRequest.class);

    private static final String DISABLE_PXE_BOOT_ACTIVITY_ID = "receiveDisablePxeBootResponse";

    private final AsynchronousNodeService            asynchronousNodeService;
    private final ConfigurePxeBootRequestTransformer requestTransformer;

    public SendDisablePxeBootRequest(final AsynchronousNodeService asynchronousNodeService,
            final ConfigurePxeBootRequestTransformer requestTransformer)
    {
        super(LOGGER, "Send Disable PXE Boot");
        this.asynchronousNodeService = asynchronousNodeService;
        this.requestTransformer = requestTransformer;
    }

    @Override
    public void delegateExecute(final DelegateExecution delegateExecution)
    {
        NodeDetail nodeDetail = (NodeDetail) delegateExecution.getVariable(NODE_DETAIL);
        updateDelegateStatus("Attempting " + this.taskName + " on Node " + nodeDetail.getServiceTag() + ".");

        AsynchronousNodeServiceCallback<?> requestCallback = null;
        try
        {
            final DelegateRequestModel<ConfigurePxeBootRequestMessage> delegateRequestModel = requestTransformer
                    .buildConfigurePxeBootRequest(delegateExecution);

            requestCallback = this.asynchronousNodeService
                    .sendConfigurePxeBootRequest(delegateExecution.getProcessInstanceId(), DISABLE_PXE_BOOT_ACTIVITY_ID,
                            CONFIGURE_PXE_BOOT_MESSAGE_ID, delegateRequestModel.getRequestMessage());

        }
        catch (Exception ex)
        {
            String errorMessage = "An Unexpected Exception occurred attempting to request " + taskName + " on Node " + nodeDetail.getServiceTag() + ". Reason: ";
            updateDelegateStatus(errorMessage, ex);
            throw new BpmnError(CONFIGURE_PXE_FAILED, errorMessage + ex.getMessage());
        }
            if (requestCallback == null)
            {
                String errorMessage = taskName + " on Node " + nodeDetail.getServiceTag() + " failed.";
                updateDelegateStatus(errorMessage);
                throw new BpmnError(CONFIGURE_PXE_FAILED, errorMessage);
            }

            updateDelegateStatus(taskName + " on Node " + nodeDetail.getServiceTag() + " was successful.");

    }
}
