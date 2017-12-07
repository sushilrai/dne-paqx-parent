/**
 * <p>
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries. All Rights Reserved. Dell EMC Confidential/Proprietary Information
 * </p>
 */

package com.dell.cpsd.paqx.dne.service.delegates;

import com.dell.cpsd.paqx.dne.service.NodeService;
import com.dell.cpsd.paqx.dne.service.delegates.model.DelegateRequestModel;
import com.dell.cpsd.paqx.dne.service.delegates.model.NodeDetail;
import com.dell.cpsd.paqx.dne.transformers.ConfigureDvSwitchesTransformer;
import com.dell.cpsd.virtualization.capabilities.api.AddHostToDvSwitchRequestMessage;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import static com.dell.cpsd.paqx.dne.service.delegates.utils.DelegateConstants.ADD_HOST_TO_DV_SWITCH_FAILED;
import static com.dell.cpsd.paqx.dne.service.delegates.utils.DelegateConstants.NODE_DETAIL;

@Component
@Scope("prototype")
@Qualifier("addHostToDvSwitch")
public class AddHostToDvSwitch extends BaseWorkflowDelegate
{
    /**
     * The logger instance
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(AddHostToDvSwitch.class);

    /**
     * The <code>NodeService</code> instance
     */
    private final NodeService                    nodeService;
    private final ConfigureDvSwitchesTransformer requestTransformer;

    public AddHostToDvSwitch(final NodeService nodeService, final ConfigureDvSwitchesTransformer requestTransformer)
    {
        super(LOGGER, "Add Host to DV Switch");
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
            final DelegateRequestModel<AddHostToDvSwitchRequestMessage> delegateRequestModel = requestTransformer
                    .buildAddHostToDvSwitchRequest(delegateExecution);
            this.nodeService.requestAddHostToDvSwitch(delegateRequestModel.getRequestMessage());

            final String returnMessage = this.taskName + " on Node " + delegateRequestModel.getServiceTag() + " was successful.";
            updateDelegateStatus(returnMessage);
        }
        catch (Exception ex)
        {
            final String errorMessage = "An unexpected exception occurred attempting to request " + this.taskName + ". Reason: ";
            updateDelegateStatus(errorMessage, ex);
            throw new BpmnError(ADD_HOST_TO_DV_SWITCH_FAILED, errorMessage + ex.getMessage());
        }
    }
}
