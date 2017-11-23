/**
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries.  All Rights Reserved.
 * Dell EMC Confidential/Proprietary Information
 **/

package com.dell.cpsd.paqx.dne.transformers;

import com.dell.cpsd.paqx.dne.service.delegates.model.DelegateRequestModel;
import com.dell.cpsd.paqx.dne.service.delegates.model.NodeDetail;
import com.dell.cpsd.paqx.dne.service.model.ComponentEndpointIds;
import com.dell.cpsd.virtualization.capabilities.api.Credentials;
import com.dell.cpsd.virtualization.capabilities.api.HostMaintenanceModeRequestMessage;
import com.dell.cpsd.virtualization.capabilities.api.MaintenanceModeRequest;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.springframework.stereotype.Component;

import static com.dell.cpsd.paqx.dne.service.delegates.utils.DelegateConstants.HOSTNAME;
import static com.dell.cpsd.paqx.dne.service.delegates.utils.DelegateConstants.NODE_DETAIL;

/**
 * Host Maintenance request transformer.
 * <p>
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries. All Rights Reserved.
 * Dell EMC Confidential/Proprietary Information
 * </p>
 *
 * @version 1.0
 * @since 1.0
 */
@Component
public class HostMaintenanceRequestTransformer
{
    private static final String VCENTER_CUSTOMER_TYPE = "VCENTER-CUSTOMER";

    private final ComponentIdsTransformer componentIdsTransformer;

    public HostMaintenanceRequestTransformer(final ComponentIdsTransformer componentIdsTransformer)
    {
        this.componentIdsTransformer = componentIdsTransformer;
    }

    public DelegateRequestModel<HostMaintenanceModeRequestMessage> buildHostMaintenanceRequest(final DelegateExecution delegateExecution,
            final boolean isMaintenanceModeEnable)
    {
        final String hostname = (String) delegateExecution.getVariable(HOSTNAME);
        final NodeDetail nodeDetail = (NodeDetail) delegateExecution.getVariable(NODE_DETAIL);
        final ComponentEndpointIds componentEndpointIds = componentIdsTransformer
                .getVCenterComponentEndpointIdsByEndpointType(VCENTER_CUSTOMER_TYPE);

        final HostMaintenanceModeRequestMessage requestMessage = getHostMaintenanceModeRequestMessage(isMaintenanceModeEnable, hostname,
                componentEndpointIds);
        return new DelegateRequestModel<>(requestMessage, nodeDetail.getServiceTag());
    }

    private HostMaintenanceModeRequestMessage getHostMaintenanceModeRequestMessage(final boolean isMaintenanceModeEnable,
            final String hostname, final ComponentEndpointIds componentEndpointIds)
    {
        final HostMaintenanceModeRequestMessage requestMessage = new HostMaintenanceModeRequestMessage();
        final MaintenanceModeRequest maintenanceModeRequest = new MaintenanceModeRequest();
        maintenanceModeRequest.setMaintenanceModeEnable(isMaintenanceModeEnable);
        maintenanceModeRequest.setHostName(hostname);
        requestMessage.setMaintenanceModeRequest(maintenanceModeRequest);
        requestMessage.setCredentials(new Credentials(componentEndpointIds.getEndpointUrl(), null, null));
        requestMessage.setComponentEndpointIds(
                new com.dell.cpsd.virtualization.capabilities.api.ComponentEndpointIds(componentEndpointIds.getComponentUuid(),
                        componentEndpointIds.getEndpointUuid(), componentEndpointIds.getCredentialUuid()));
        return requestMessage;
    }
}
