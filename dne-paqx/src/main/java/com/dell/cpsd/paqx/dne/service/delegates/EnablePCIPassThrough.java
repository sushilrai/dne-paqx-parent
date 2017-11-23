/**
 * <p>
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries. All Rights Reserved. Dell EMC Confidential/Proprietary Information
 * </p>
 */

package com.dell.cpsd.paqx.dne.service.delegates;

import com.dell.cpsd.paqx.dne.exception.TaskResponseFailureException;
import com.dell.cpsd.paqx.dne.service.NodeService;
import com.dell.cpsd.paqx.dne.service.delegates.model.DelegateRequestModel;
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
        this.nodeService = nodeService;
        this.pciPassThroughRequestTransformer = pciPassThroughRequestTransformer;
    }

    @Override
    public void delegateExecute(final DelegateExecution delegateExecution)
    {
        LOGGER.info("Execute Enable PCI Pass through task");
        final String taskMessage = "Enable PCI pass through for ESXi host";

        try
        {
            final DelegateRequestModel<EnablePCIPassthroughRequestMessage> delegateRequestModel = pciPassThroughRequestTransformer
                    .buildEnablePciPassThroughRequest(delegateExecution);
            this.nodeService.requestEnablePciPassThrough(delegateRequestModel.getRequestMessage());

            delegateExecution
                    .setVariable(DelegateConstants.HOST_PCI_DEVICE_ID, delegateRequestModel.getRequestMessage().getHostPciDeviceId());
            final String returnMessage = taskMessage + " on Node " + delegateRequestModel.getServiceTag() + " was successful.";
            LOGGER.info(returnMessage);
            updateDelegateStatus(returnMessage);
        }
        catch (TaskResponseFailureException ex)
        {
            updateDelegateStatus(ex.getMessage());
            throw new BpmnError(ENABLE_PCI_PASSTHROUGH_FAILED, "Exception Code: " + ex.getCode() + "::" + ex.getMessage());
        }
        catch (Exception ex)
        {
            String errorMessage = "An unexpected exception occurred attempting to request " + taskMessage + ". Reason: ";
            LOGGER.error(errorMessage, ex);
            updateDelegateStatus(errorMessage + ex.getMessage());
            throw new BpmnError(ENABLE_PCI_PASSTHROUGH_FAILED, errorMessage + ex.getMessage());
        }
    }
}
