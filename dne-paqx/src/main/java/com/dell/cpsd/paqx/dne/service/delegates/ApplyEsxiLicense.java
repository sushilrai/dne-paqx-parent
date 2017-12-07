/**
 * <p>
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries. All Rights Reserved. Dell EMC Confidential/Proprietary Information
 * </p>
 */

package com.dell.cpsd.paqx.dne.service.delegates;

import com.dell.cpsd.paqx.dne.service.NodeService;
import com.dell.cpsd.paqx.dne.service.delegates.model.DelegateRequestModel;
import com.dell.cpsd.paqx.dne.service.delegates.model.NodeDetail;
import com.dell.cpsd.paqx.dne.transformers.ApplyEsxiLicenseRequestTransformer;
import com.dell.cpsd.virtualization.capabilities.api.AddEsxiHostVSphereLicenseRequest;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import static com.dell.cpsd.paqx.dne.service.delegates.utils.DelegateConstants.APPLY_ESXI_LICENSE_FAILED;
import static com.dell.cpsd.paqx.dne.service.delegates.utils.DelegateConstants.NODE_DETAIL;

@Component
@Scope("prototype")
@Qualifier("applyEsxiLicense")
public class ApplyEsxiLicense extends BaseWorkflowDelegate
{
    /**
     * The logger instance
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(ApplyEsxiLicense.class);

    /**
     * The <code>NodeService</code> instance
     */
    private final NodeService                        nodeService;
    private final ApplyEsxiLicenseRequestTransformer requestTransformer;

    @Autowired
    public ApplyEsxiLicense(final NodeService nodeService, final ApplyEsxiLicenseRequestTransformer requestTransformer)
    {
        super(LOGGER, "Apply Esxi License");
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
            final DelegateRequestModel<AddEsxiHostVSphereLicenseRequest> delegateRequestModel = requestTransformer
                    .buildApplyEsxiLicenseRequest(delegateExecution);
            this.nodeService.requestInstallEsxiLicense(delegateRequestModel.getRequestMessage());

            updateDelegateStatus(taskName + " on Node " + nodeDetail.getServiceTag() + " was successful.");
        }
        catch (Exception ex)
        {
            String errorMessage = "An unexpected exception occurred attempting to request " + taskName + ". Reason: ";
            updateDelegateStatus(errorMessage, ex);
            throw new BpmnError(APPLY_ESXI_LICENSE_FAILED, errorMessage + ex.getMessage());
        }
    }
}
