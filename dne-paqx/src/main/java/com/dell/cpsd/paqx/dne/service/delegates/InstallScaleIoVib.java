/**
 * <p>
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries. All Rights Reserved. Dell EMC Confidential/Proprietary Information
 * </p>
 */

package com.dell.cpsd.paqx.dne.service.delegates;

import com.dell.cpsd.paqx.dne.service.NodeService;
import com.dell.cpsd.paqx.dne.service.delegates.model.DelegateRequestModel;
import com.dell.cpsd.paqx.dne.service.delegates.model.NodeDetail;
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
import static com.dell.cpsd.paqx.dne.service.delegates.utils.DelegateConstants.NODE_DETAIL;

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
@Qualifier("installScaleIoVib")
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
     * InstallScaleIoVib constructor
     *
     * @param nodeService                   - The <code>NodeService</code> instance
     * @param softwareVibRequestTransformer
     */
    public InstallScaleIoVib(final NodeService nodeService, final SoftwareVibRequestTransformer softwareVibRequestTransformer)
    {
        super(LOGGER, "Install ScaleIO Vib");
        this.nodeService = nodeService;
        this.softwareVibRequestTransformer = softwareVibRequestTransformer;
    }

    @Override
    public void delegateExecute(final DelegateExecution delegateExecution)
    {
        NodeDetail nodeDetail = (NodeDetail) delegateExecution.getVariable(NODE_DETAIL);
        updateDelegateStatus("Attempting " + this.taskName + " on Node " + nodeDetail.getServiceTag() + ".");

        try
        {
            final DelegateRequestModel<SoftwareVIBRequestMessage> delegateRequestModel = softwareVibRequestTransformer
                    .buildInstallSoftwareVibRequest(delegateExecution);
            this.nodeService.requestInstallSoftwareVib(delegateRequestModel.getRequestMessage());

            final String returnMessage = taskName + " on Node " + nodeDetail.getServiceTag() + " was successful.";
            updateDelegateStatus(returnMessage);
        }
        catch (Exception ex)
        {
            String errorMessage = "An unexpected exception occurred attempting to " + taskName + " on Node " + nodeDetail.getServiceTag() + ". Reason: ";
            updateDelegateStatus(errorMessage, ex);
            throw new BpmnError(INSTALL_SCALEIO_VIB_FAILED, errorMessage + ex.getMessage());
        }
    }
}
