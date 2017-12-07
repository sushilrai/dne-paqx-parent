/**
 * <p>
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries. All Rights Reserved. Dell EMC Confidential/Proprietary Information
 * </p>
 */

package com.dell.cpsd.paqx.dne.service.delegates;

import com.dell.cpsd.paqx.dne.service.NodeService;
import com.dell.cpsd.paqx.dne.service.delegates.model.NodeDetail;
import com.dell.cpsd.paqx.dne.service.model.ChangeIdracCredentialsResponse;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import static com.dell.cpsd.paqx.dne.service.delegates.utils.DelegateConstants.INSTALL_ESXI_FAILED;
import static com.dell.cpsd.paqx.dne.service.delegates.utils.DelegateConstants.NODE_DETAIL;

@Component
@Scope("prototype")
@Qualifier("configureNodeCredentials")
public class ConfigureNodeCredentials extends BaseWorkflowDelegate
{
    /*
     * The logger instance
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigureNodeCredentials.class);

    /*
     * The <code>NodeService</code> instance
     */
    private NodeService nodeService;

    /**
     * ConfigureNodeCredentials constructor.
     *
     * @param nodeService - The <code>NodeService</code> instance.
     * @since 1.0
     */
    @Autowired
    public ConfigureNodeCredentials(NodeService nodeService)
    {
        super(LOGGER, "Configure Node Credentials");
        this.nodeService = nodeService;
    }

    @Override
    public void delegateExecute(final DelegateExecution delegateExecution)
    {
        NodeDetail nodeDetail = (NodeDetail) delegateExecution.getVariable(NODE_DETAIL);
        updateDelegateStatus("Attempting " + this.taskName + " on Node " + nodeDetail.getServiceTag() + ".");

        final String symphonuUuid = nodeDetail.getId();
        ChangeIdracCredentialsResponse responseMessage = null;
        try
        {
            responseMessage = this.nodeService.changeIdracCredentials(symphonuUuid);
        }
        catch (Exception e)
        {
            final String message = "An Unexpected Exception Occurred attempting to Change iDrac Credentials on Node " +
                                   nodeDetail.getServiceTag() + " failed.  Reason: ";
            updateDelegateStatus(message, e);
            throw new BpmnError(INSTALL_ESXI_FAILED, message + e.getMessage());
        }
        if (responseMessage != null && !"SUCCESS".equalsIgnoreCase(responseMessage.getMessage()))
        {
            final String message = taskName + " on Node " + nodeDetail.getServiceTag() + " failed!";
            updateDelegateStatus(message);
            throw new BpmnError(INSTALL_ESXI_FAILED,
                                message);
        }

        updateDelegateStatus(taskName + " on Node " + nodeDetail.getServiceTag() + " was successful.");
    }
}
