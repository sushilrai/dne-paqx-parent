/**
 * <p>
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries. All Rights Reserved. Dell EMC Confidential/Proprietary Information
 * </p>
 */

package com.dell.cpsd.paqx.dne.service.delegates;

import com.dell.cpsd.paqx.dne.amqp.callback.AsynchronousNodeServiceCallback;
import com.dell.cpsd.paqx.dne.service.AsynchronousNodeService;
import com.dell.cpsd.paqx.dne.service.delegates.model.NodeDetail;
import com.dell.cpsd.paqx.dne.service.model.BootDeviceIdracStatus;
import org.apache.commons.collections.CollectionUtils;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import static com.dell.cpsd.paqx.dne.service.delegates.utils.DelegateConstants.CONFIGURE_BOOT_DEVICE_FAILED;
import static com.dell.cpsd.paqx.dne.service.delegates.utils.DelegateConstants.CONFIGURE_BOOT_DEVICE_MESSAGE_ID;
import static com.dell.cpsd.paqx.dne.service.delegates.utils.DelegateConstants.NODE_DETAIL;

@Component
@Scope("prototype")
@Qualifier("configureBootDevice")
public class ConfigureBootDevice extends BaseWorkflowDelegate
{
    /**
     * The logger instance
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigureBootDevice.class);

    /**
     * The <code>AsynchronousNodeService</code> instance
     */
    private AsynchronousNodeService asynchronousNodeService;

    @Autowired
    public ConfigureBootDevice(AsynchronousNodeService asynchronousNodeService)
    {
        super(LOGGER, "Boot Device Configuration");
        this.asynchronousNodeService = asynchronousNodeService;
    }

    @Override
    public void delegateExecute(final DelegateExecution delegateExecution)
    {
        NodeDetail nodeDetail = (NodeDetail) delegateExecution.getVariable(NODE_DETAIL);
        updateDelegateStatus("Attempting " + this.taskName + " on Node " + nodeDetail.getServiceTag() + ".");

        AsynchronousNodeServiceCallback<?> responseCallback = (AsynchronousNodeServiceCallback<?>)
                delegateExecution.getVariable(CONFIGURE_BOOT_DEVICE_MESSAGE_ID);
        BootDeviceIdracStatus bootDeviceIdracStatusRequest = null;
        if (responseCallback != null && responseCallback.isDone())
        {
            try
            {
                bootDeviceIdracStatusRequest = asynchronousNodeService.bootDeviceIdracStatusResponse(responseCallback);
            }
            catch (Exception e)
            {
                final String message = "An Unexpected Exception Occurred attempting to Configure Boot Device on Node " +
                                       nodeDetail.getServiceTag();
                updateDelegateStatus(message, e);
                throw new BpmnError(CONFIGURE_BOOT_DEVICE_FAILED,
                                    message + e.getMessage());
            }
        }
        if (bootDeviceIdracStatusRequest == null || !"SUCCESS".equalsIgnoreCase(bootDeviceIdracStatusRequest.getStatus()))
        {
            final StringBuilder messageBuilder = new StringBuilder();
            messageBuilder.append(taskName + " was unsuccessful on Node " + nodeDetail.getServiceTag() +
                                      ". Please correct the following errors and try again.\n");
            if (bootDeviceIdracStatusRequest != null && CollectionUtils
                    .isNotEmpty(bootDeviceIdracStatusRequest.getErrors()))
            {
                bootDeviceIdracStatusRequest.getErrors().forEach(error -> messageBuilder.append(error).append("\n"));
            }
            final String message = messageBuilder.toString();
            updateDelegateStatus(message);
            throw new BpmnError(CONFIGURE_BOOT_DEVICE_FAILED, message);
        }
        updateDelegateStatus(taskName + " was successful on Node " + nodeDetail.getServiceTag());
    }
}
