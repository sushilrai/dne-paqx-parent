/**
 * <p>
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries. All Rights Reserved. Dell EMC Confidential/Proprietary Information
 * </p>
 */

package com.dell.cpsd.paqx.dne.service.delegates;

import com.dell.cpsd.paqx.dne.exception.TaskResponseFailureException;
import com.dell.cpsd.paqx.dne.service.NodeService;
import com.dell.cpsd.paqx.dne.service.delegates.model.DelegateRequestModel;
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
        this.nodeService = nodeService;
        this.requestTransformer = requestTransformer;
    }

    @Override
    public void delegateExecute(final DelegateExecution delegateExecution)
    {
        LOGGER.info("Execute Apply Esxi License");
        final String taskMessage = "Apply Esxi License";

        try
        {
            final DelegateRequestModel<AddEsxiHostVSphereLicenseRequest> delegateRequestModel = requestTransformer
                    .buildApplyEsxiLicenseRequest(delegateExecution);
            this.nodeService.requestInstallEsxiLicense(delegateRequestModel.getRequestMessage());

            final String returnMessage = taskMessage + " on Node " + delegateRequestModel.getServiceTag() + " was successful.";
            LOGGER.info(returnMessage);
            updateDelegateStatus(returnMessage);
        }
        catch (TaskResponseFailureException ex)
        {
            updateDelegateStatus(ex.getMessage());
            throw new BpmnError(APPLY_ESXI_LICENSE_FAILED, "Exception Code: " + ex.getCode() + "::" + ex.getMessage());
        }
        catch (Exception ex)
        {
            String errorMessage = "An unexpected exception occurred attempting to request " + taskMessage + ". Reason: ";
            LOGGER.error(errorMessage, ex);
            updateDelegateStatus(errorMessage + ex.getMessage());
            throw new BpmnError(APPLY_ESXI_LICENSE_FAILED, errorMessage + ex.getMessage());
        }
    }
}
