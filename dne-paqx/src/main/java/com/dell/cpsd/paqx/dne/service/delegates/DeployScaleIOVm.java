/**
 * <p>
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries. All Rights Reserved. Dell EMC Confidential/Proprietary Information
 * </p>
 */

package com.dell.cpsd.paqx.dne.service.delegates;

import com.dell.cpsd.paqx.dne.exception.TaskResponseFailureException;
import com.dell.cpsd.paqx.dne.service.NodeService;
import com.dell.cpsd.paqx.dne.service.delegates.model.DelegateRequestModel;
import com.dell.cpsd.paqx.dne.transformers.DeployScaleIoVmRequestTransformer;
import com.dell.cpsd.virtualization.capabilities.api.DeployVMFromTemplateRequestMessage;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import static com.dell.cpsd.paqx.dne.service.delegates.utils.DelegateConstants.DEPLOY_SCALEIO_VM_FAILED;
import static com.dell.cpsd.paqx.dne.service.delegates.utils.DelegateConstants.VIRTUAL_MACHINE_NAME;

/**
 * Deploy ScaleIo virtual machine.
 * <p>
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries. All Rights Reserved. Dell EMC Confidential/Proprietary Information
 * </p>
 *
 * @version 1.0
 * @since 1.0
 */
@Component
@Scope("prototype")
@Qualifier("deployScaleIOVm")
public class DeployScaleIOVm extends BaseWorkflowDelegate
{
    /*
     * The logger instance
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(DeployScaleIOVm.class);

    /*
     * The <code>NodeService</code> instance
     */
    private final NodeService                       nodeService;
    private final DeployScaleIoVmRequestTransformer deployScaleIoVmRequestTransformer;

    /**
     * DeployScaleIoVm constructor
     *
     * @param nodeService                       - The <code>DataServiceRepository</code> instance
     * @param deployScaleIoVmRequestTransformer
     */
    @Autowired
    public DeployScaleIOVm(final NodeService nodeService, final DeployScaleIoVmRequestTransformer deployScaleIoVmRequestTransformer)
    {
        this.nodeService = nodeService;
        this.deployScaleIoVmRequestTransformer = deployScaleIoVmRequestTransformer;
    }

    @Override
    public void delegateExecute(final DelegateExecution delegateExecution)
    {
        LOGGER.info("Execute Deploy ScaleIO VM From Template");
        final String taskMessage = "Deploy ScaleIo Vm";

        try
        {
            final DelegateRequestModel<DeployVMFromTemplateRequestMessage> delegateRequestModel = deployScaleIoVmRequestTransformer
                    .buildDeployVmRequest(delegateExecution);
            this.nodeService.requestDeployScaleIoVm(delegateRequestModel.getRequestMessage());

            delegateExecution.setVariable(VIRTUAL_MACHINE_NAME, delegateRequestModel.getRequestMessage().getNewVMName());
            final String returnMessage = taskMessage + " on Node " + delegateRequestModel.getServiceTag() + " was successful.";
            LOGGER.info(returnMessage);
            updateDelegateStatus(returnMessage);
        }
        catch (TaskResponseFailureException ex)
        {
            String errorMsg = "Exception Code: ";
            LOGGER.error(errorMsg, ex);
            updateDelegateStatus(ex.getMessage());
            throw new BpmnError(DEPLOY_SCALEIO_VM_FAILED,  errorMsg + ex.getCode() + "::" + ex.getMessage());
        }
        catch (Exception ex)
        {
            String errorMessage = "An unexpected exception occurred attempting to request " + taskMessage + ". Reason: ";
            LOGGER.error(errorMessage, ex);
            updateDelegateStatus(errorMessage + ex.getMessage());
            throw new BpmnError(DEPLOY_SCALEIO_VM_FAILED, errorMessage + ex.getMessage());
        }
    }
}
