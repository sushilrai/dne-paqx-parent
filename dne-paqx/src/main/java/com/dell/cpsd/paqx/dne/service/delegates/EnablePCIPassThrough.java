/**
 * <p>
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries. All Rights Reserved. Dell EMC Confidential/Proprietary Information
 * </p>
 */

package com.dell.cpsd.paqx.dne.service.delegates;

import com.dell.cpsd.paqx.dne.service.NodeService;
import com.dell.cpsd.paqx.dne.service.delegates.model.DelegateRequestModel;
import com.dell.cpsd.paqx.dne.service.delegates.model.NodeDetail;
import com.dell.cpsd.paqx.dne.service.delegates.utils.DelegateConstants;
import com.dell.cpsd.paqx.dne.transformers.PciPassThroughRequestTransformer;
import com.dell.cpsd.virtualization.capabilities.api.EnablePCIPassthroughRequestMessage;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import static com.dell.cpsd.paqx.dne.service.delegates.utils.DelegateConstants.ENABLE_PCI_PASSTHROUGH_FAILED;
import static com.dell.cpsd.paqx.dne.service.delegates.utils.DelegateConstants.NODE_DETAIL;

@Component
@Scope("prototype")
@Qualifier("enablePCIPassThrough")
public class EnablePCIPassThrough extends BaseWorkflowDelegate
{
    /**
     * The logger instance
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(EnablePCIPassThrough.class);

    /**
     * The <code>NodeService</code> instance
     */
    private final NodeService                      nodeService;
    private final PciPassThroughRequestTransformer pciPassThroughRequestTransformer;

    @Autowired
    public EnablePCIPassThrough(final NodeService nodeService, final PciPassThroughRequestTransformer pciPassThroughRequestTransformer)
    {
        super(LOGGER, "Enable PCI Pass Through for ESXi Host");
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
            final DelegateRequestModel<EnablePCIPassthroughRequestMessage> delegateRequestModel = pciPassThroughRequestTransformer
                    .buildEnablePciPassThroughRequest(delegateExecution);
            this.nodeService.requestEnablePciPassThrough(delegateRequestModel.getRequestMessage());

            delegateExecution
                    .setVariable(DelegateConstants.HOST_PCI_DEVICE_ID, delegateRequestModel.getRequestMessage().getHostPciDeviceId());
            updateDelegateStatus(taskName + " on Node " + nodeDetail.getServiceTag() + " was successful.");
        }
        catch (Exception ex)
        {
            String errorMessage = "An unexpected exception occurred attempting to " + taskName + " on Node " + nodeDetail.getServiceTag() + ". Reason: ";
            updateDelegateStatus(errorMessage, ex);
            throw new BpmnError(ENABLE_PCI_PASSTHROUGH_FAILED, errorMessage + ex.getMessage());
        }
    }
}
