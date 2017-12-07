/**
 * <p>
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries. All Rights Reserved. Dell EMC Confidential/Proprietary Information
 * </p>
 */

package com.dell.cpsd.paqx.dne.service.delegates;

import com.dell.cpsd.paqx.dne.service.NodeService;
import com.dell.cpsd.paqx.dne.service.delegates.model.DelegateRequestModel;
import com.dell.cpsd.paqx.dne.service.delegates.model.NodeDetail;
import com.dell.cpsd.paqx.dne.transformers.HostMaintenanceRequestTransformer;
import com.dell.cpsd.virtualization.capabilities.api.HostMaintenanceModeRequestMessage;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.dell.cpsd.paqx.dne.service.delegates.utils.DelegateConstants.ESXI_HOST_MAINTENANCE_MODE_FAILED;
import static com.dell.cpsd.paqx.dne.service.delegates.utils.DelegateConstants.NODE_DETAIL;

public abstract class AbstractHostMaintenanceMode extends BaseWorkflowDelegate
{
    /*
     * The logger instance
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractHostMaintenanceMode.class);

    /*
     * The <code>NodeService</code> instance
     */
    private final NodeService nodeService;

    private final HostMaintenanceRequestTransformer requestTransformer;

    /*
    * The task name
    */
    private final String taskName;

    /**
     * AbstractHostMaintenanceMode constructor
     *
     * @param nodeService        - The <code>NodeService</code> instance
     * @param requestTransformer
     * @param taskName           - The task name  @since 1.0
     */
    public AbstractHostMaintenanceMode(final NodeService nodeService, final HostMaintenanceRequestTransformer requestTransformer,
            final String taskName)
    {
        super(LOGGER, taskName);
        this.nodeService = nodeService;
        this.requestTransformer = requestTransformer;
        this.taskName = taskName;
    }

    /**
     * Subclasses hould override to set the desired maintenance mode state.
     *
     * @return True to enter maintenance mode, false to exit maintenance mode
     * @since 1.0
     */
    protected abstract boolean getMaintenanceModeEnable();

    @Override
    public void delegateExecute(final DelegateExecution delegateExecution)
    {
        NodeDetail nodeDetail = (NodeDetail) delegateExecution.getVariable(NODE_DETAIL);
        updateDelegateStatus("Attempting to " + taskName + " on Node " + nodeDetail.getServiceTag()+".");
        final boolean maintenanceModeEnable = this.getMaintenanceModeEnable();
        try
        {
            final DelegateRequestModel<HostMaintenanceModeRequestMessage> delegateRequestModel = requestTransformer
                    .buildHostMaintenanceRequest(delegateExecution, maintenanceModeEnable);
            this.nodeService.requestHostMaintenanceMode(delegateRequestModel.getRequestMessage());

            final String returnMessage = taskName + " on Node " + nodeDetail.getServiceTag() + " was successful.";
            updateDelegateStatus(returnMessage);
        }
        catch (Exception ex)
        {
            String errorMessage = "An unexpected exception occurred attempting to request " + taskName + " on Node " + nodeDetail.getServiceTag() + ". Reason: ";
            updateDelegateStatus(errorMessage, ex);
            throw new BpmnError(ESXI_HOST_MAINTENANCE_MODE_FAILED, errorMessage + ex.getMessage());
        }
    }
}
