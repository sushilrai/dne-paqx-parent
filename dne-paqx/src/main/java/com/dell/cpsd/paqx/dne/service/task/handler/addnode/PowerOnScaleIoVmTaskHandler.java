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
import com.dell.cpsd.paqx.dne.service.model.DeployScaleIoVmTaskResponse;
import com.dell.cpsd.paqx.dne.service.model.InstallEsxiTaskResponse;
import com.dell.cpsd.paqx.dne.service.model.NodeExpansionRequest;
import com.dell.cpsd.paqx.dne.service.model.Status;
import com.dell.cpsd.paqx.dne.service.model.TaskResponse;
import com.dell.cpsd.paqx.dne.service.task.handler.BaseTaskHandler;
import com.dell.cpsd.virtualization.capabilities.api.VmPowerOperationRequest;
import com.dell.cpsd.virtualization.capabilities.api.VmPowerOperationsRequestMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;

/**
 * Task handler used for Powering on the ScaleIO VM.
 * <p>
 * <p>
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries. All Rights Reserved. Dell EMC Confidential/Proprietary Information
 * </p>
 *
 * @version 1.0
 * @since 1.0
 */
public class PowerOnScaleIoVmTaskHandler extends BaseTaskHandler implements IWorkflowTaskHandler
{
    /*
     * The logger instance
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(RebootHostTaskHandler.class);

    /*
     * The <code>NodeService</code> instance
     */
    private final NodeService nodeService;

    /*
     * The <code>DataServiceRepository</code> instance
     */
    private final DataServiceRepository repository;

    private final Long PING_TIMEOUT;

    public PowerOnScaleIoVmTaskHandler(final NodeService nodeService, final DataServiceRepository repository, final long ping_timeout)
    {
        this.nodeService = nodeService;
        this.repository = repository;
        PING_TIMEOUT = ping_timeout;
    }

    @Override
    public boolean executeTask(final Job job)
    {
        TaskResponse response = this.initializeResponse(job);

        try
        {
            final InstallEsxiTaskResponse installEsxiTaskResponse = (InstallEsxiTaskResponse) job.getTaskResponseMap().get("installEsxi");

            if (installEsxiTaskResponse == null)
            {
                throw new IllegalStateException("No Install ESXi task response found");
            }

            final DeployScaleIoVmTaskResponse deployScaleIoVmTaskResponse = (DeployScaleIoVmTaskResponse) job.getTaskResponseMap()
                    .get("deploySVM");

            if (deployScaleIoVmTaskResponse == null)
            {
                throw new IllegalStateException("No Deploy ScaleIO VM task response found");
            }

            final ComponentEndpointIds componentEndpointIds = repository.getVCenterComponentEndpointIdsByEndpointType("VCENTER-CUSTOMER");

            if (componentEndpointIds == null)
            {
                throw new IllegalStateException("No VCenter components found.");
            }

            final NodeExpansionRequest inputParams = job.getInputParams();
            final String esxiManagementIpAddress = inputParams.getEsxiManagementIpAddress();

            final InetAddress esxiHostIp = InetAddress.getByName(esxiManagementIpAddress);

            if (!esxiHostIp.isReachable(this.PING_TIMEOUT.intValue()))
            {
                throw new IllegalStateException("ESXi Host is not reachable");
            }

            final VmPowerOperationRequest powerOperationRequest = new VmPowerOperationRequest();
            powerOperationRequest.setHostname(installEsxiTaskResponse.getHostname());
            powerOperationRequest.setVmName(deployScaleIoVmTaskResponse.getNewVMName());
            powerOperationRequest.setPowerOperation(VmPowerOperationRequest.PowerOperation.POWER_ON);

            final VmPowerOperationsRequestMessage requestMessage = new VmPowerOperationsRequestMessage();
            requestMessage.setVmPowerOperationRequest(powerOperationRequest);
            requestMessage.setEndpointUrl(componentEndpointIds.getEndpointUrl());
            requestMessage.setComponentEndpointIds(new com.dell.cpsd.virtualization.capabilities.api.ComponentEndpointIds(componentEndpointIds.getComponentUuid(),
                    componentEndpointIds.getEndpointUuid(), componentEndpointIds.getCredentialUuid()));

            boolean succeeded = this.nodeService.requestVmPowerOperation(requestMessage);

            if (!succeeded)
            {
                throw new IllegalStateException("VM power operation request failed");
            }

            response.setWorkFlowTaskStatus(Status.SUCCEEDED);
            return true;
        }
        catch (Exception ex)
        {
            LOGGER.error("Error powering on ScaleIO VM", ex);
            response.addError(ex.getMessage());
        }

        response.setWorkFlowTaskStatus(Status.FAILED);
        return false;
    }
}
