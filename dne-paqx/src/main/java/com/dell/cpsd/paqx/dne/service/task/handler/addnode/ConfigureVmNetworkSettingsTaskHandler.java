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
import com.dell.cpsd.paqx.dne.service.model.Status;
import com.dell.cpsd.paqx.dne.service.model.TaskResponse;
import com.dell.cpsd.paqx.dne.service.task.handler.BaseTaskHandler;
import com.dell.cpsd.virtualization.capabilities.api.ConfigureVmNetworkSettingsRequestMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.util.Map;

/**
 * Configure Virtual Machine network settings task handler.
 *
 * <p>
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries. All Rights Reserved. Dell EMC Confidential/Proprietary Information
 * </p>
 *
 * @version 1.0
 * @since 1.0
 */
public class ConfigureVmNetworkSettingsTaskHandler extends BaseTaskHandler implements IWorkflowTaskHandler
{
    /**
     * The <code>Logger</code> instance
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigureVmNetworkSettingsTaskHandler.class);

    /**
     * The <code>NodeService</code> instance
     */
    private final NodeService nodeService;

    /**
     * The <code>DataServiceRepository</code> instance
     */
    private final DataServiceRepository repository;

    /**
     * ConfigureVmNetworkSettingsTaskHandler constructor.
     *
     * @param nodeService - The <code>NodeService</code> instance
     * @param repository - The <code>DataServiceRepository</code> instance
     */
    public ConfigureVmNetworkSettingsTaskHandler(final NodeService nodeService, final DataServiceRepository repository)
    {
        this.nodeService = nodeService;
        this.repository = repository;
    }

    @Override
    public boolean executeTask(final Job job)
    {
        LOGGER.info("Execute Configure vm network settings task");

        final TaskResponse response = this.initializeResponse(job);

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
                throw new IllegalStateException("No Install ESXi task response found");
            }

            final String hostname = installEsxiTaskResponse.getHostname();

            if (StringUtils.isEmpty(hostname))
            {
                throw new IllegalStateException("Host name is null");
            }

            final DeployScaleIoVmTaskResponse deployScaleIoVmTaskResponse = (DeployScaleIoVmTaskResponse) job.getTaskResponseMap().get("deploySVM");

            if (deployScaleIoVmTaskResponse == null)
            {
                throw new IllegalStateException("No deploy ScaleIO vm task response found");
            }

            final String vmName = deployScaleIoVmTaskResponse.getNewVMName();

            if (StringUtils.isEmpty(vmName))
            {
                throw new IllegalStateException("VM name is null");
            }

            final Map<String, String> dvSwitchNames = repository.getDvSwitchNames();

            if (dvSwitchNames == null)
            {
                throw new IllegalStateException("DVSwitch Names Map is null");
            }

            final Map<String, String> scaleIoNetworkNamesMap = repository.getScaleIoNetworkNames(dvSwitchNames);

            if (scaleIoNetworkNamesMap == null)
            {
                throw new IllegalStateException("ScaleIO DVSwitch-Network Names Map is null");
            }

            final ConfigureVmNetworkSettingsRequestMessage requestMessage = new ConfigureVmNetworkSettingsRequestMessage();
            requestMessage.setHostname(hostname);
            requestMessage.setVmName(vmName);
            requestMessage.setNetworkSettingsMap(scaleIoNetworkNamesMap);
            requestMessage.setEndpointUrl(componentEndpointIds.getEndpointUrl());
            requestMessage.setComponentEndpointIds(
                    new com.dell.cpsd.virtualization.capabilities.api.ComponentEndpointIds(componentEndpointIds.getComponentUuid(),
                            componentEndpointIds.getEndpointUuid(), componentEndpointIds.getCredentialUuid()));

            final boolean succeeded = this.nodeService.requestConfigureVmNetworkSettings(requestMessage);

            if (!succeeded)
            {
                throw new IllegalStateException("Configure VM network settings request failed");
            }

            response.setWorkFlowTaskStatus(Status.SUCCEEDED);
            return true;
        }
        catch(Exception ex)
        {
            LOGGER.error("Exception occurred", ex);
            response.addError(ex.getMessage());
        }

        response.setWorkFlowTaskStatus(Status.FAILED);
        return false;
    }
}
