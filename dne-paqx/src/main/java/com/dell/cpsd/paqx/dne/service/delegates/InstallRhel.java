/**
 * <p>
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries. All Rights Reserved. Dell EMC Confidential/Proprietary Information
 * </p>
 */

package com.dell.cpsd.paqx.dne.service.delegates;

import com.dell.cpsd.paqx.dne.amqp.callback.AsynchronousNodeServiceCallback;
import com.dell.cpsd.paqx.dne.repository.DataServiceRepository;
import com.dell.cpsd.paqx.dne.service.AsynchronousNodeService;
import com.dell.cpsd.paqx.dne.service.delegates.model.NodeDetail;
import org.apache.commons.lang3.StringUtils;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import static com.dell.cpsd.paqx.dne.service.delegates.utils.DelegateConstants.*;

@Component
@Scope("prototype")
@Qualifier("installRhel")
public class InstallRhel extends BaseWorkflowDelegate
{
    /**
     * The logger instance
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(InstallRhel.class);

    private final AsynchronousNodeService asynchronousNodeService;
    private final DataServiceRepository repository;

    @Autowired
    public InstallRhel(final AsynchronousNodeService asynchronousNodeService, final DataServiceRepository repository)
    {
        super(LOGGER, "Install RHEL");
        this.asynchronousNodeService = asynchronousNodeService;
        this.repository = repository;
    }

    /*
    * Builds the host fully qualified domain name (FQDN) from
    * the hostname and domain name, where the domain name is
    * retrieved from the database.
    */
    private String generateHostFqdn(String hostName)
    {
        final String domainName = this.repository.getDomainName();
        if (StringUtils.isEmpty(domainName))
        {
            return hostName;
        }

        final StringBuilder builder = new StringBuilder();
        builder.append(hostName);
        builder.append(".");
        builder.append(domainName);
        return builder.toString();
    }

    @Override
    public void delegateExecute(final DelegateExecution delegateExecution)
    {
        NodeDetail nodeDetail = (NodeDetail) delegateExecution.getVariable(NODE_DETAIL);
        updateDelegateStatus("Attempting " + this.taskName + " on Node " + nodeDetail.getServiceTag() + ".");

        AsynchronousNodeServiceCallback<?> responseCallback = (AsynchronousNodeServiceCallback<?>) delegateExecution
                .getVariable(INSTALL_RHEL_MESSAGE_ID);

        if (responseCallback != null && responseCallback.isDone())
        {
            try
            {
                this.asynchronousNodeService.requestInstallRhel(responseCallback);
            }
            catch (Exception e)
            {
                final String message = taskName + " on Node " + nodeDetail.getServiceTag() + " failed!  Reason: ";
                updateDelegateStatus(message, e);
                throw new BpmnError(INSTALL_RHEL_FAILED, message + e.getMessage());
            }

            String fqdn = this.generateHostFqdn(nodeDetail.getStorageOnlyManagementHostname());
            delegateExecution.setVariable(HOSTNAME, fqdn);
            updateDelegateStatus(taskName + " on Node " + nodeDetail.getServiceTag() + " was successful.  Host Name is being set to " + fqdn);

        } else {
            final String message = taskName + " on Node " + nodeDetail.getServiceTag() + " failed!";
            updateDelegateStatus(message);
            throw new BpmnError(INSTALL_RHEL_FAILED, message);
        }
    }
}
