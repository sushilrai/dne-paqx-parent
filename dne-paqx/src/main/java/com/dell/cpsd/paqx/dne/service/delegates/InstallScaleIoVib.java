/**
 * <p>
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries. All Rights Reserved. Dell EMC Confidential/Proprietary Information
 * </p>
 */

package com.dell.cpsd.paqx.dne.service.delegates;

import com.dell.cpsd.paqx.dne.exception.TaskResponseFailureException;
import com.dell.cpsd.paqx.dne.service.NodeService;
import com.dell.cpsd.paqx.dne.service.delegates.model.DelegateRequestModel;
import com.dell.cpsd.paqx.dne.transformers.SoftwareVibRequestTransformer;
import com.dell.cpsd.virtualization.capabilities.api.SoftwareVIBRequestMessage;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import static com.dell.cpsd.paqx.dne.service.delegates.utils.DelegateConstants.INSTALL_SCALEIO_VIB_FAILED;

/**
 * Install ScaleIo Data Client (SDC)
 * <p>
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries. All Rights Reserved. Dell EMC Confidential/Proprietary Information
 * </p>
 *
 * @version 1.0
 * @since 1.0
 */
@Component
@Scope("prototype")
@Qualifier("installScaleIOVib")
public class InstallScaleIoVib extends BaseWorkflowDelegate
{
    /*
     * The logger instance
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(InstallScaleIoVib.class);

    /*
     * The <code>NodeService</code> instance
     */
    private final NodeService                   nodeService;
    private final SoftwareVibRequestTransformer softwareVibRequestTransformer;

    /**
     * InstallScaleIoVibTaskHandler constructor
     *
     * @param nodeService                   - The <code>NodeService</code> instance
     * @param softwareVibRequestTransformer
     */
    public InstallScaleIoVib(final NodeService nodeService, final SoftwareVibRequestTransformer softwareVibRequestTransformer)
    {
        this.nodeService = nodeService;
        this.softwareVibRequestTransformer = softwareVibRequestTransformer;
    }

    @Override
    public void delegateExecute(final DelegateExecution delegateExecution)
    {
        LOGGER.info("Execute Install ScaleIO VIB task");
        final String taskMessage = "Install ScaleIO Vib";

        try
        {
            final DelegateRequestModel<SoftwareVIBRequestMessage> delegateRequestModel = softwareVibRequestTransformer
                    .buildInstallSoftwareVibRequest(delegateExecution);
            this.nodeService.requestInstallSoftwareVib(delegateRequestModel.getRequestMessage());

            final String returnMessage = taskMessage + " on Node " + delegateRequestModel.getServiceTag() + " was successful.";
            LOGGER.info(returnMessage);
            updateDelegateStatus(returnMessage);
        }
        catch (TaskResponseFailureException ex)
        {
            updateDelegateStatus(ex.getMessage());
            throw new BpmnError(INSTALL_SCALEIO_VIB_FAILED, "Exception Code: " + ex.getCode() + "::" + ex.getMessage());
        }
        catch (Exception ex)
        {
            String errorMessage = "An unexpected exception occurred attempting to request " + taskMessage + ". Reason: ";
            LOGGER.error(errorMessage, ex);
            updateDelegateStatus(errorMessage + ex.getMessage());
            throw new BpmnError(INSTALL_SCALEIO_VIB_FAILED, errorMessage + ex.getMessage());
        }
    }
}
