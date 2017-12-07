/**
 * <p>
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries. All Rights Reserved. Dell EMC Confidential/Proprietary Information
 * </p>
 */

package com.dell.cpsd.paqx.dne.service.delegates;

import com.dell.cpsd.paqx.dne.service.NodeService;
import com.dell.cpsd.paqx.dne.service.delegates.model.DelegateRequestModel;
import com.dell.cpsd.paqx.dne.service.delegates.model.NodeDetail;
import com.dell.cpsd.paqx.dne.transformers.ConfigureVmNetworkSettingsRequestTransformer;
import com.dell.cpsd.virtualization.capabilities.api.ConfigureVmNetworkSettingsRequestMessage;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import static com.dell.cpsd.paqx.dne.service.delegates.utils.DelegateConstants.CONFIGURE_VM_NETWORK_SETTINGS;
import static com.dell.cpsd.paqx.dne.service.delegates.utils.DelegateConstants.NODE_DETAIL;

/**
 * Configure virtual machine network settings.
 * <p>
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries. All Rights Reserved. Dell EMC Confidential/Proprietary Information
 * </p>
 *
 * @version 1.0
 * @since 1.0
 */
@Component
@Scope("prototype")
@Qualifier("configureVMNetworkSettings")
public class ConfigureVMNetworkSettings extends BaseWorkflowDelegate
{
    /**
     * The <code>Logger</code> instance
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigureVMNetworkSettings.class);

    /**
     * The <code>NodeService</code> instance
     */
    private final NodeService                                  nodeService;
    private final ConfigureVmNetworkSettingsRequestTransformer requestTransformer;

    /**
     * ConfigureVMNetworkSettings constructor.
     *
     * @param nodeService        - The <code>NodeService</code> instance
     * @param requestTransformer
     */
    @Autowired
    public ConfigureVMNetworkSettings(final NodeService nodeService, final ConfigureVmNetworkSettingsRequestTransformer requestTransformer)
    {
        super(LOGGER, "Configure VM Network Settings");
        this.nodeService = nodeService;
        this.requestTransformer = requestTransformer;
    }

    @Override
    public void delegateExecute(final DelegateExecution delegateExecution)
    {
        NodeDetail nodeDetail = (NodeDetail) delegateExecution.getVariable(NODE_DETAIL);
        updateDelegateStatus("Attempting " + this.taskName + " on Node " + nodeDetail.getServiceTag() + ".");

        try
        {
            final DelegateRequestModel<ConfigureVmNetworkSettingsRequestMessage> delegateRequestModel = requestTransformer
                    .buildConfigureVmNetworkSettingsRequest(delegateExecution);
            this.nodeService.requestConfigureVmNetworkSettings(delegateRequestModel.getRequestMessage());

            final String returnMessage = taskName + " on Node " + nodeDetail.getServiceTag() + " was successful.";
            updateDelegateStatus(returnMessage);
        }
        catch (Exception ex)
        {
            String errorMessage = "An unexpected exception occurred attempting to " + taskName + " on Node " + nodeDetail.getServiceTag() + ". Reason: ";
            updateDelegateStatus(errorMessage, ex);
            throw new BpmnError(CONFIGURE_VM_NETWORK_SETTINGS, errorMessage + ex.getMessage());
        }
    }
}
