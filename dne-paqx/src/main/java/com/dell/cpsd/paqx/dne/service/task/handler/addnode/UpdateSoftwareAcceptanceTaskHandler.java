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
import com.dell.cpsd.paqx.dne.service.model.InstallEsxiTaskResponse;
import com.dell.cpsd.paqx.dne.service.model.Status;
import com.dell.cpsd.paqx.dne.service.model.TaskResponse;
import com.dell.cpsd.paqx.dne.service.task.handler.BaseTaskHandler;
import com.dell.cpsd.virtualization.capabilities.api.VCenterUpdateSoftwareAcceptanceRequestMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Task responsible for requesting an update to the software acceptance level for a ESX host.
 *
 * <p>
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries. All Rights Reserved. Dell EMC Confidential/Proprietary Information
 * </p>
 *
 * @version 1.0
 * @since 1.0
 */
public class UpdateSoftwareAcceptanceTaskHandler extends BaseTaskHandler implements IWorkflowTaskHandler
{
    /**
     * The logger instance
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(DatastoreRenameTaskHandler.class);

    /*
     * The <code>NodeService</code> instance
     */
    private final NodeService nodeService;

    /*
    * The <code>DataServiceRepository</code> instance
    */
    private final DataServiceRepository dataServiceRepository;

    /**
     * UpdateSoftwareAcceptanceTaskHandler constructor.
     *
     * @param nodeService
     *            - The <code>NodeService</code> instance.
     *
     * @since 1.0
     */
    public UpdateSoftwareAcceptanceTaskHandler(final NodeService nodeService, final DataServiceRepository dataServiceRepository)
    {
        this.nodeService = nodeService;
        this.dataServiceRepository = dataServiceRepository;
    }

    @Override
    public boolean executeTask(final Job job)
    {
        LOGGER.info("Execute update software acceptance task");

        final TaskResponse response = initializeResponse(job);

        try
        {
            final ComponentEndpointIds vCenterComponentEndpointIdsByEndpointType = this.dataServiceRepository
                    .getVCenterComponentEndpointIdsByEndpointType("VCENTER-CUSTOMER");

            if (vCenterComponentEndpointIdsByEndpointType == null)
            {
                throw new IllegalStateException("VCenter Component Endpoint Ids are null");
            }

            final InstallEsxiTaskResponse installEsxiTaskResponse = (InstallEsxiTaskResponse) job.getTaskResponseMap().get("installEsxi");

            if (installEsxiTaskResponse == null)
            {
                throw new IllegalStateException("No install ESXi task response found");
            }

            final String esxiManagementHostname = installEsxiTaskResponse.getHostname();

            if (esxiManagementHostname == null)
            {
                throw new IllegalStateException("Hostname is null");
            }

            VCenterUpdateSoftwareAcceptanceRequestMessage requestMessage = new VCenterUpdateSoftwareAcceptanceRequestMessage();
            requestMessage.setHostname(esxiManagementHostname);
            requestMessage.setSoftwareAcceptance(VCenterUpdateSoftwareAcceptanceRequestMessage.SoftwareAcceptance.PARTNER);
            requestMessage.setComponentEndpointIds(new com.dell.cpsd.virtualization.capabilities.api.ComponentEndpointIds(
                    vCenterComponentEndpointIdsByEndpointType.getComponentUuid(),
                    vCenterComponentEndpointIdsByEndpointType.getEndpointUuid(),
                    vCenterComponentEndpointIdsByEndpointType.getCredentialUuid()));

            boolean succeeded = this.nodeService.requestUpdateSoftwareAcceptance(requestMessage);

            if (!succeeded)
            {
                throw new IllegalStateException("Software acceptance update failed");
            }

            response.setWorkFlowTaskStatus(Status.SUCCEEDED);
            return true;
        }
        catch (Exception ex)
        {
            LOGGER.error("Error during update software acceptance", ex);
            response.addError(ex.toString());
        }

        response.setWorkFlowTaskStatus(Status.FAILED);
        return false;
    }
}
