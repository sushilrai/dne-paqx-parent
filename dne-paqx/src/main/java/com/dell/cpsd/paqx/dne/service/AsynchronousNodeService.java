/**
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries.  All Rights Reserved.
 * Dell EMC Confidential/Proprietary Information
 */

package com.dell.cpsd.paqx.dne.service;

import com.dell.cpsd.ConfigurePxeBootRequestMessage;
import com.dell.cpsd.EsxiInstallationInfo;
import com.dell.cpsd.paqx.dne.amqp.callback.AsynchronousNodeServiceCallback;
import com.dell.cpsd.paqx.dne.exception.TaskResponseFailureException;
import com.dell.cpsd.paqx.dne.service.model.BootDeviceIdracStatus;
import com.dell.cpsd.paqx.dne.service.model.ConfigureBootDeviceIdracRequest;
import com.dell.cpsd.service.common.client.callback.ServiceResponse;
import com.dell.cpsd.service.common.client.exception.ServiceExecutionException;
import com.dell.cpsd.virtualization.capabilities.api.HostPowerOperationRequestMessage;
import com.dell.cpsd.virtualization.capabilities.api.RemoteCommandExecutionRequestMessage;
import com.dell.cpsd.virtualization.capabilities.api.RemoteCommandExecutionResponseMessage;

/**
 * Node service interface.
 * <p>
 * <p>
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries. All Rights Reserved. Dell EMC Confidential/Proprietary Information
 * </p>
 *
 * @version 1.0
 * @since 1.0
 */
public interface AsynchronousNodeService
{
    <T extends ServiceResponse<?>> AsynchronousNodeServiceCallback<?> bootDeviceIdracStatusRequest(String processId, String activityId,
            String messageId, ConfigureBootDeviceIdracRequest configureBootDeviceIdracRequest);

    BootDeviceIdracStatus bootDeviceIdracStatusResponse(AsynchronousNodeServiceCallback<?> serviceCallback)
            throws ServiceExecutionException;

    <T extends ServiceResponse<?>> AsynchronousNodeServiceCallback<?> requestInstallEsxi(String processId, String activityId,
            String messageId, EsxiInstallationInfo esxiInstallationInfo);

    void requestInstallEsxi(AsynchronousNodeServiceCallback<?> serviceCallback) throws TaskResponseFailureException;

    AsynchronousNodeServiceCallback<?> sendRebootHostRequest(String processId, String activityId, String messageId,
            HostPowerOperationRequestMessage requestMessage);

    void processRebootHostResponse(AsynchronousNodeServiceCallback<?> serviceCallback) throws TaskResponseFailureException;

    AsynchronousNodeServiceCallback<?> sendConfigurePxeBootRequest(String processId, String activityId, String messageId,
            ConfigurePxeBootRequestMessage requestMessage);

    void processConfigurePxeBootResponse(AsynchronousNodeServiceCallback<?> serviceCallback) throws TaskResponseFailureException;

    <T extends ServiceResponse<?>> AsynchronousNodeServiceCallback<?> executeRemoteCommand(String processId, String activityId,
            String messageId, RemoteCommandExecutionRequestMessage requestMessage) throws TaskResponseFailureException;

    RemoteCommandExecutionResponseMessage.Status processRemoteCommandResponse(AsynchronousNodeServiceCallback<?> serviceCallback)
            throws ServiceExecutionException;
}
