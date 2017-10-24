/**
 * <p>
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries. All Rights Reserved. Dell EMC Confidential/Proprietary Information
 * </p>
 */

package com.dell.cpsd.paqx.dne.service.task.handler.addnode;

import com.dell.cpsd.paqx.dne.domain.IWorkflowTaskHandler;
import com.dell.cpsd.paqx.dne.domain.Job;
import com.dell.cpsd.paqx.dne.repository.DataServiceRepository;
import com.dell.cpsd.paqx.dne.service.NodeService;
import com.dell.cpsd.paqx.dne.service.model.ComponentEndpointIds;
import com.dell.cpsd.paqx.dne.service.model.HostMaintenanceModeTaskResponse;
import com.dell.cpsd.paqx.dne.service.model.InstallEsxiTaskResponse;
import com.dell.cpsd.paqx.dne.service.model.Status;
import com.dell.cpsd.paqx.dne.service.task.handler.BaseTaskHandler;
import com.dell.cpsd.virtualization.capabilities.api.Credentials;
import com.dell.cpsd.virtualization.capabilities.api.HostMaintenanceModeRequestMessage;
import com.dell.cpsd.virtualization.capabilities.api.MaintenanceModeRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract task handler for managing maintenance mode operations
 *
 * <p>
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries. All Rights Reserved. Dell EMC Confidential/Proprietary Information
 * </p>
 *
 * @version 1.0
 * @since 1.0
 */
public abstract class AbstractHostMaintenanceModeTaskHandler extends BaseTaskHandler implements IWorkflowTaskHandler
{
    /*
     * The logger instance
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractHostMaintenanceModeTaskHandler.class);

    /*
     * The <code>NodeService</code> instance
     */
    private final NodeService nodeService;

    /*
     * The <code>DataServiceRepository</code> instance
     */
    private final DataServiceRepository repository;

    /*
    * The task name
    */
    private final String taskName;

    /**
     * AbstractHostMaintenanceModeTaskHandler constructor
     *
     * @param nodeService - The <code>NodeService</code> instance
     * @param repository - The <code>DataServiceRepository</code> instance
     * @param taskName - The task name
     *
     * @since 1.0
     */
    AbstractHostMaintenanceModeTaskHandler(final NodeService nodeService, final DataServiceRepository repository, final String taskName)
    {
        this.nodeService = nodeService;
        this.repository = repository;
        this.taskName = taskName;
    }

    /**
     * Subclasses hould override to set the desired maintenance mode state.
     *
     * @return True to enter maintenance mode, false to exit maintenance mode
     *
     * @since 1.0
     */
    protected abstract boolean getMaintenanceModeEnable();

    @Override
    public boolean executeTask(final Job job)
    {
        LOGGER.info("Execute {} task", this.taskName);

        final HostMaintenanceModeTaskResponse response = initializeResponse(job);

        try
        {
            final ComponentEndpointIds componentEndpointIds = repository.getVCenterComponentEndpointIdsByEndpointType("VCENTER-CUSTOMER");

            if (componentEndpointIds == null)
            {
                throw new IllegalStateException("No VCenter components found.");
            }

            final InstallEsxiTaskResponse installEsxiTaskResponse = (InstallEsxiTaskResponse) job.getTaskResponseMap().get("installEsxi");

            if (installEsxiTaskResponse == null)
            {
                throw new IllegalStateException("No install ESXi task response found");
            }

            final String hostname = installEsxiTaskResponse.getHostname();

            if (hostname == null)
            {
                throw new IllegalStateException("Hostname is null");
            }

            final boolean maintenanceModeEnable = this.getMaintenanceModeEnable();

            final HostMaintenanceModeRequestMessage requestMessage = getHostMaintenanceModeRequestMessage(componentEndpointIds, hostname, maintenanceModeEnable);

            final boolean success = this.nodeService.requestHostMaintenanceMode(requestMessage);

            if (!success)
            {
                throw new IllegalStateException(String.format("%s task failed", this.taskName));
            }

            response.setWorkFlowTaskStatus(Status.SUCCEEDED);
            return true;
        }
        catch (Exception e)
        {
            LOGGER.error("Exception occurred", e);
            response.addError(e.getMessage());
        }

        response.setWorkFlowTaskStatus(Status.FAILED);
        return false;
    }

    @Override
    public HostMaintenanceModeTaskResponse initializeResponse(Job job)
    {
        HostMaintenanceModeTaskResponse response = new HostMaintenanceModeTaskResponse();
        setupResponse(job, response);
        return response;
    }

    private HostMaintenanceModeRequestMessage getHostMaintenanceModeRequestMessage(final ComponentEndpointIds componentEndpointIds,
            final String hostname, final boolean maintenanceModeEnable)
    {
        final HostMaintenanceModeRequestMessage requestMessage = new HostMaintenanceModeRequestMessage();
        final MaintenanceModeRequest maintenanceModeRequest = new MaintenanceModeRequest();
        maintenanceModeRequest.setMaintenanceModeEnable(maintenanceModeEnable);
        maintenanceModeRequest.setHostName(hostname);
        requestMessage.setMaintenanceModeRequest(maintenanceModeRequest);
        requestMessage.setCredentials(new Credentials(componentEndpointIds.getEndpointUrl(), null, null));
        requestMessage.setComponentEndpointIds(
                new com.dell.cpsd.virtualization.capabilities.api.ComponentEndpointIds(componentEndpointIds.getComponentUuid(),
                        componentEndpointIds.getEndpointUuid(), componentEndpointIds.getCredentialUuid()));
        return requestMessage;
    }
}
