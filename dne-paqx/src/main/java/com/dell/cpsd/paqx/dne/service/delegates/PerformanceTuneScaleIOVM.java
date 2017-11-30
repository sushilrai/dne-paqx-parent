/**
 * <p>
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries. All Rights Reserved. Dell EMC Confidential/Proprietary Information
 * </p>
 */

package com.dell.cpsd.paqx.dne.service.delegates;

import com.dell.cpsd.paqx.dne.exception.TaskResponseFailureException;
import com.dell.cpsd.paqx.dne.service.NodeService;
import com.dell.cpsd.paqx.dne.service.delegates.model.DelegateRequestModel;
import com.dell.cpsd.paqx.dne.transformers.RemoteCommandExecutionRequestTransformer;
import com.dell.cpsd.virtualization.capabilities.api.RemoteCommandExecutionRequestMessage;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import static com.dell.cpsd.paqx.dne.service.delegates.utils.DelegateConstants.PERFORMANCE_TUNE_SCALEIO_VM;

/**
 * Performance tune on ScaleIo VM.
 * <p>
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries. All Rights Reserved. Dell EMC Confidential/Proprietary Information
 * </p>
 *
 * @version 1.0
 * @since 1.0
 */
@Component
@Scope("prototype")
@Qualifier("performanceTuneScaleIOVM")
public class PerformanceTuneScaleIOVM extends BaseWorkflowDelegate
{
    /*
     * The logger instance
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(PerformanceTuneScaleIOVM.class);

    /*
     * The <code>NodeService</code> instance
     */
    private final NodeService                              nodeService;
    private final RemoteCommandExecutionRequestTransformer remoteCommandExecutionRequestTransformer;

    /**
     * PerformanceTuneScaleIOVM constructor.
     *
     * @param nodeService                              - The <code>NodeService</code> instance
     * @param remoteCommandExecutionRequestTransformer
     */
    @Autowired
    public PerformanceTuneScaleIOVM(final NodeService nodeService,
            final RemoteCommandExecutionRequestTransformer remoteCommandExecutionRequestTransformer)
    {
        this.nodeService = nodeService;
        this.remoteCommandExecutionRequestTransformer = remoteCommandExecutionRequestTransformer;
    }

    @Override
    public void delegateExecute(final DelegateExecution delegateExecution)
    {
        LOGGER.info("Execute PerformanceTuneSvm task");
        final String taskMessage = "Performance Tune Scale IO VM";

        try
        {
            final DelegateRequestModel<RemoteCommandExecutionRequestMessage> delegateRequestModel = remoteCommandExecutionRequestTransformer
                    .buildRemoteCodeExecutionRequest(delegateExecution,
                            RemoteCommandExecutionRequestMessage.RemoteCommand.PERFORMANCE_TUNING_SVM);
            this.nodeService.requestRemoteCommandExecution(delegateRequestModel.getRequestMessage());

            final String returnMessage = taskMessage + " on Node " + delegateRequestModel.getServiceTag() + " was successful.";
            LOGGER.info(returnMessage);
            updateDelegateStatus(returnMessage);
        }
        catch (TaskResponseFailureException ex)
        {
            updateDelegateStatus(ex.getMessage());
            throw new BpmnError(PERFORMANCE_TUNE_SCALEIO_VM, "Exception Code: " + ex.getCode() + "::" + ex.getMessage());
        }
        catch (Exception ex)
        {
            String errorMessage = "An unexpected exception occurred attempting to request " + taskMessage + ". Reason: ";
            LOGGER.error(errorMessage, ex);
            updateDelegateStatus(errorMessage + ex.getMessage());
            throw new BpmnError(PERFORMANCE_TUNE_SCALEIO_VM, errorMessage + ex.getMessage());
        }
    }
}
