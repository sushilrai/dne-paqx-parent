/**
 * <p>
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries. All Rights Reserved. Dell EMC Confidential/Proprietary Information
 * </p>
 */

package com.dell.cpsd.paqx.dne.service.delegates;

import com.dell.cpsd.paqx.dne.service.NodeService;
import com.dell.cpsd.paqx.dne.service.delegates.model.DelegateRequestModel;
import com.dell.cpsd.paqx.dne.service.delegates.model.NodeDetail;
import com.dell.cpsd.paqx.dne.transformers.PciPassThroughRequestTransformer;
import com.dell.cpsd.virtualization.capabilities.api.UpdatePCIPassthruSVMRequestMessage;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import static com.dell.cpsd.paqx.dne.service.delegates.utils.DelegateConstants.NODE_DETAIL;
import static com.dell.cpsd.paqx.dne.service.delegates.utils.DelegateConstants.UPDATE_PCI_PASSTHROUGH;

/**
 * Update PCI Passthrough Controller for ScaleIo Vm.
 * <p>
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries. All Rights Reserved. Dell EMC Confidential/Proprietary Information
 * </p>
 *
 * @version 1.0
 * @since 1.0
 */
@Component
@Scope("prototype")
@Qualifier("updatePCIPassThrough")
public class UpdatePCIPassThrough extends BaseWorkflowDelegate
{
    /**
     * The logger instance
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(UpdatePCIPassThrough.class);

    /**
     * The <code>NodeService</code> instance
     */
    private final NodeService                      nodeService;
    private final PciPassThroughRequestTransformer pciPassThroughRequestTransformer;

    @Autowired
    public UpdatePCIPassThrough(final NodeService nodeService, final PciPassThroughRequestTransformer pciPassThroughRequestTransformer)
    {
        super(LOGGER, "Update PCI pass through for ScaleIO VM");
        this.nodeService = nodeService;
        this.pciPassThroughRequestTransformer = pciPassThroughRequestTransformer;
    }

    @Override
    public void delegateExecute(final DelegateExecution delegateExecution)
    {
        NodeDetail nodeDetail = (NodeDetail) delegateExecution.getVariable(NODE_DETAIL);
        updateDelegateStatus("Attempting " + this.taskName + " on Node " + nodeDetail.getServiceTag() + ".");
        try
        {
            final DelegateRequestModel<UpdatePCIPassthruSVMRequestMessage> delegateRequestModel = pciPassThroughRequestTransformer
                    .buildUpdatePciPassThroughRequest(delegateExecution);
            this.nodeService.requestSetPciPassThrough(delegateRequestModel.getRequestMessage());

            updateDelegateStatus(taskName + " on Node " + nodeDetail.getServiceTag() + " was successful.");
        }
        catch (Exception ex)
        {
            String errorMessage = "An unexpected exception occurred attempting to request " + taskName + " on Node " + nodeDetail.getServiceTag()+ ". Reason: ";
            updateDelegateStatus(errorMessage, ex);
            throw new BpmnError(UPDATE_PCI_PASSTHROUGH, errorMessage + ex.getMessage());
        }
    }
}
