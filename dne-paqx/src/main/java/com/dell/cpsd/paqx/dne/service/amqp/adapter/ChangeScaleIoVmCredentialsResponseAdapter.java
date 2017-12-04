/**
 * <p>
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries. All Rights Reserved. Dell EMC Confidential/Proprietary Information
 * </p>
 */

package com.dell.cpsd.paqx.dne.service.amqp.adapter;

import com.dell.cpsd.service.common.client.rpc.ServiceCallbackRegistry;
import org.camunda.bpm.engine.RuntimeService;

/**
 * Install ScaleIo Vm Packages response adapter
 *
 * <p/>
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries. All Rights Reserved. Dell EMC Confidential/Proprietary Information
 * <p/>
 *
 * @version 1.0
 * @since 1.0
 */
public class ChangeScaleIoVmCredentialsResponseAdapter extends AsyncRemoteCommandExecutionResponseAdapter
{
    public ChangeScaleIoVmCredentialsResponseAdapter(final ServiceCallbackRegistry serviceCallbackRegistry, final RuntimeService runtimeService)
    {
        super(serviceCallbackRegistry, runtimeService);
    }
}