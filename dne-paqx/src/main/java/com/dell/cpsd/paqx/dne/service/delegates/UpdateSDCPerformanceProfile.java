/**
 * <p>
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries. All Rights Reserved. Dell EMC Confidential/Proprietary Information
 * </p>
 */

package com.dell.cpsd.paqx.dne.service.delegates;

import com.dell.cpsd.paqx.dne.exception.TaskResponseFailureException;
import com.dell.cpsd.paqx.dne.service.NodeService;
import com.dell.cpsd.paqx.dne.service.delegates.model.DelegateRequestModel;
import com.dell.cpsd.paqx.dne.transformers.SdcPerformanceProfileRequestTransformer;
import com.dell.cpsd.storage.capabilities.api.SioSdcUpdatePerformanceProfileRequestMessage;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import static com.dell.cpsd.paqx.dne.service.delegates.utils.DelegateConstants.UPDATE_SDC_PERFORMANCE_PROFILE_FAILED;

/**
 * Update ScaleIo Data Client (SDC) performance profile.
 * <p>
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries. All Rights Reserved. Dell EMC Confidential/Proprietary Information
 * </p>
 *
 * @version 1.0
 * @since 1.0
 */
@Component
@Scope("prototype")
@Qualifier("updateSDCPerformanceProfile")
public class UpdateSDCPerformanceProfile extends BaseWorkflowDelegate
{
    /*
     * The logger instance
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(UpdateSDCPerformanceProfile.class);

    /*
     * The <code>NodeService</code> instance
     */
    private final NodeService                             nodeService;
    private final SdcPerformanceProfileRequestTransformer sdcPerformanceProfileRequestTransformer;

    /**
     * UpdateSdcPerformanceProfile constructor
     *
     * @param nodeService                             - The <code>NodeService</code> instance
     * @param sdcPerformanceProfileRequestTransformer The request building transformer
     */
    @Autowired
    public UpdateSDCPerformanceProfile(final NodeService nodeService,
            final SdcPerformanceProfileRequestTransformer sdcPerformanceProfileRequestTransformer)
    {
        this.nodeService = nodeService;
        this.sdcPerformanceProfileRequestTransformer = sdcPerformanceProfileRequestTransformer;
    }

    @Override
    public void delegateExecute(final DelegateExecution delegateExecution)
    {
        LOGGER.info("Execute UpdateSDCPerformanceProfile");
        final String taskMessage = "Update ScaleIO SDC Performance Profile";

        try
        {
            final DelegateRequestModel<SioSdcUpdatePerformanceProfileRequestMessage> delegateRequestModel = sdcPerformanceProfileRequestTransformer
                    .buildSdcPerformanceProfileRequest(delegateExecution);
            this.nodeService.requestUpdateSdcPerformanceProfile(delegateRequestModel.getRequestMessage());

            final String returnMessage = taskMessage + " on Node " + delegateRequestModel.getServiceTag() + " was successful.";
            LOGGER.info(returnMessage);
            updateDelegateStatus(returnMessage);
        }
        catch (TaskResponseFailureException ex)
        {
            updateDelegateStatus(ex.getMessage());
            throw new BpmnError(UPDATE_SDC_PERFORMANCE_PROFILE_FAILED, "Exception Code: " + ex.getCode() + "::" + ex.getMessage());
        }
        catch (Exception ex)
        {
            final String errorMessage = "An unexpected exception occurred attempting to request " + taskMessage + ". Reason: ";
            LOGGER.error(errorMessage, ex);
            updateDelegateStatus(errorMessage + ex.getMessage());
            throw new BpmnError(UPDATE_SDC_PERFORMANCE_PROFILE_FAILED, errorMessage + ex.getMessage());
        }
    }
}
