/**
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries. All Rights Reserved. Dell EMC Confidential/Proprietary Information
 */

package com.dell.cpsd.paqx.dne.service.amqp.adapter;

import com.dell.cpsd.ConfigurePxeBootResponseMessage;
import com.dell.cpsd.paqx.dne.amqp.callback.AsynchronousNodeServiceCallback;
import com.dell.cpsd.service.common.client.callback.IServiceCallback;
import com.dell.cpsd.service.common.client.callback.ServiceResponse;
import com.dell.cpsd.service.common.client.rpc.ServiceCallbackAdapter;
import com.dell.cpsd.service.common.client.rpc.ServiceCallbackRegistry;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.runtime.Execution;

/**
 * <p>
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries. All Rights Reserved. Dell EMC Confidential/Proprietary Information
 * </p>
 *
 * @since 1.0
 */
public class ConfigurePxeBootResponseAdapter
        implements ServiceCallbackAdapter<ConfigurePxeBootResponseMessage, ServiceResponse<ConfigurePxeBootResponseMessage>>
{
    private ServiceCallbackRegistry serviceCallbackRegistry;
    private final RuntimeService runtimeService;

    public ConfigurePxeBootResponseAdapter(ServiceCallbackRegistry serviceCallbackRegistry, final RuntimeService runtimeService)
    {
        this.serviceCallbackRegistry = serviceCallbackRegistry;
        this.runtimeService = runtimeService;
    }

    @Override
    public ServiceResponse<ConfigurePxeBootResponseMessage> transform(ConfigurePxeBootResponseMessage configurePxeBootResponseMessage)
    {
        return new ServiceResponse<>(configurePxeBootResponseMessage.getMessageProperties().getCorrelationId(),
                configurePxeBootResponseMessage, null);
    }

    @Override
    public void consume(IServiceCallback callback, ServiceResponse<ConfigurePxeBootResponseMessage> configurePxeBootResponseMessage)
    {
        callback.handleServiceResponse(configurePxeBootResponseMessage);
        if (runtimeService != null && callback instanceof AsynchronousNodeServiceCallback)
        {
            final AsynchronousNodeServiceCallback<?> async = (AsynchronousNodeServiceCallback<?>) callback;
            if (async.getProcessInstanceId() != null)
            {
                final Execution execution = runtimeService.createExecutionQuery().processInstanceId(async.getProcessInstanceId())
                        .activityId(async.getActivityId()).singleResult();
                if (execution != null)
                {
                    runtimeService.setVariable(execution.getId(), async.getMessageId(), callback);
                    runtimeService.messageEventReceived(async.getMessageId(), execution.getId());
                }
            }
        }
    }

    @Override
    public IServiceCallback take(ConfigurePxeBootResponseMessage configurePxeBootResponseMessage)
    {
        return serviceCallbackRegistry.removeServiceCallback(configurePxeBootResponseMessage.getMessageProperties().getCorrelationId());
    }

    @Override
    public Class<ConfigurePxeBootResponseMessage> getSourceClass()
    {
        return ConfigurePxeBootResponseMessage.class;
    }
}
