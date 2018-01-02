/**
 * <p>
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries. All Rights Reserved. Dell EMC Confidential/Proprietary Information
 * </p>
 */

package com.dell.cpsd.paqx.dne.service.amqp.adapter;

import com.dell.cpsd.InstallRHELResponseMessage;
import com.dell.cpsd.paqx.dne.amqp.callback.AsynchronousNodeServiceCallback;
import com.dell.cpsd.service.common.client.callback.IServiceCallback;
import com.dell.cpsd.service.common.client.callback.ServiceResponse;
import com.dell.cpsd.service.common.client.rpc.ServiceCallbackAdapter;
import com.dell.cpsd.service.common.client.rpc.ServiceCallbackRegistry;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.runtime.Execution;

/**
 * Install RHEL response adapter
 *
 * <p/>
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries. All Rights Reserved. Dell EMC Confidential/Proprietary Information
 * <p/>
 *
 * @version 1.0
 * @since 1.0
 */
public class InstallRhelResponseAdapter
        implements ServiceCallbackAdapter<InstallRHELResponseMessage, ServiceResponse<InstallRHELResponseMessage>>
{
    private final ServiceCallbackRegistry serviceCallbackRegistry;
    private final RuntimeService runtimeService;

    public InstallRhelResponseAdapter(final ServiceCallbackRegistry serviceCallbackRegistry, final RuntimeService runtimeService)
    {
        this.serviceCallbackRegistry = serviceCallbackRegistry;
        this.runtimeService = runtimeService;
    }

    @Override
    public ServiceResponse<InstallRHELResponseMessage> transform(final InstallRHELResponseMessage responseMessage)
    {
        return new ServiceResponse<>(responseMessage.getMessageProperties().getCorrelationId(), responseMessage, null);
    }

    @Override
    public void consume(final IServiceCallback callback, final ServiceResponse<InstallRHELResponseMessage> responseMessage)
    {
        callback.handleServiceResponse(responseMessage);
        if (runtimeService != null && callback instanceof AsynchronousNodeServiceCallback)
        {
            AsynchronousNodeServiceCallback<?> async = (AsynchronousNodeServiceCallback<?>) callback;
            if (async.getProcessInstanceId() != null)
            {
                Execution execution = runtimeService.createExecutionQuery().processInstanceId(
                        async.getProcessInstanceId()).activityId(async.getActivityId()).singleResult();
                if (execution != null)
                {
                    runtimeService.setVariable(execution.getId(), async.getMessageId(), callback);
                    runtimeService.messageEventReceived(async.getMessageId(), execution.getId());
                }
            }
        }
    }

    @Override
    public IServiceCallback take(final InstallRHELResponseMessage installESXiResponseMessage)
    {
        return serviceCallbackRegistry.removeServiceCallback(installESXiResponseMessage.getMessageProperties().getCorrelationId());
    }

    @Override
    public Class<InstallRHELResponseMessage> getSourceClass()
    {
        return InstallRHELResponseMessage.class;
    }
}