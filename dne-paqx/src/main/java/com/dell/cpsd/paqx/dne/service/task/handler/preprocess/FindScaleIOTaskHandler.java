/**
 * <p>
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries. All Rights Reserved. Dell EMC Confidential/Proprietary Information
 * </p>
 */

package com.dell.cpsd.paqx.dne.service.task.handler.preprocess;

import com.dell.cpsd.paqx.dne.domain.IWorkflowTaskHandler;
import com.dell.cpsd.paqx.dne.domain.Job;
import com.dell.cpsd.paqx.dne.domain.scaleio.ScaleIOData;
import com.dell.cpsd.paqx.dne.domain.scaleio.ScaleIOProtectionDomain;
import com.dell.cpsd.paqx.dne.domain.vcenter.Host;
import com.dell.cpsd.paqx.dne.domain.vcenter.HostStorageDevice;
import com.dell.cpsd.paqx.dne.service.NodeService;
import com.dell.cpsd.paqx.dne.service.model.FindScaleIOResponse;
import com.dell.cpsd.paqx.dne.service.model.IdracNetworkSettingsResponseInfo;
import com.dell.cpsd.paqx.dne.service.model.Status;
import com.dell.cpsd.paqx.dne.service.model.TaskResponse;
import com.dell.cpsd.paqx.dne.service.task.handler.BaseTaskHandler;
import com.dell.cpsd.paqx.dne.util.NodeInventoryParsingUtil;
import com.dell.cpsd.service.common.client.exception.ServiceExecutionException;
import com.dell.cpsd.service.common.client.exception.ServiceTimeoutException;

import com.dell.cpsd.service.engineering.standards.Device;
import com.dell.cpsd.service.engineering.standards.EssValidateStoragePoolResponseMessage;

import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;

/**
 * This class implements the logic to find Storage pool entries
 * <p>
 * <p>
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries.  All Rights Reserved.
 * Dell EMC Confidential/Proprietary Information
 * </p>
 *
 * @since 1.0
 */

public class FindScaleIOTaskHandler extends BaseTaskHandler implements IWorkflowTaskHandler
{
    /*
     * The logger instance
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(FindScaleIOTaskHandler.class);

    /*
     * Node Service instance used to find and validate Storage pool entries
     */
    private NodeService nodeService;

    /**
     * Construct an instance based on nodeservice reference
     *
     * @param nodeService
     */
    public FindScaleIOTaskHandler(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    @Override
    public boolean executeTask(final Job job)
    {
        LOGGER.info("Execute FindScaleIOTaskHandler task");
        FindScaleIOResponse response = initializeResponse(job);

        try
        {
            List<Device> newDevices = NodeInventoryParsingUtil.parseNewDevices(nodeService.getNodeInventoryData(job));

            // retrieve scale IO data
            List<ScaleIOData> scaleIODataList = nodeService.listScaleIOData();

            // retrieve vCenter data
            Map<String, Map<String, HostStorageDevice>> hostToStorageDeviceMap = nodeService
                    .getHostToStorageDeviceMap(nodeService.findVcenterHosts());

            if (!CollectionUtils.isEmpty(newDevices) && !CollectionUtils.isEmpty(scaleIODataList))
            {
                ScaleIOData scaleIOData = scaleIODataList.get(0);
                List<ScaleIOProtectionDomain> protectionDomains = scaleIOData.getProtectionDomains();
                if (protectionDomains != null)
                {
                    validateStoragePoolsAndSetResponse(response, newDevices, hostToStorageDeviceMap, protectionDomains);
                }

                if (CollectionUtils.isEmpty(response.getErrors()))
                {
                    return true;
                }
                return false;
            }
        }
        catch (ServiceTimeoutException | ServiceExecutionException exception)
        {
            LOGGER.error("Error listing scaleIO data.", exception);
        }
        catch (Exception e)
        {
            LOGGER.error("Error listing scaleIO data.", e);
        }

        return false;
    }

    private void validateStoragePoolsAndSetResponse(final FindScaleIOResponse response, final List<Device> newDevices,
            final Map<String, Map<String, HostStorageDevice>> hostToStorageDeviceMap, final List<ScaleIOProtectionDomain> protectionDomains)
            throws ServiceTimeoutException, ServiceExecutionException
    {
        for (ScaleIOProtectionDomain protectionDomain : protectionDomains)
        {
            EssValidateStoragePoolResponseMessage storageResponseMessage = nodeService
                    .validateStoragePools(protectionDomain.getStoragePools(), newDevices, hostToStorageDeviceMap);

            if (storageResponseMessage != null)
            {
                response.setWorkFlowTaskStatus(Status.SUCCEEDED);
                if (MapUtils.isNotEmpty(storageResponseMessage.getDeviceToStoragePoolMap()))
                {
                    LOGGER.info("Storage pool validated successfully.");
                    response.setDeviceToStoragePoolMap(storageResponseMessage.getDeviceToStoragePoolMap());
                }
                if (!CollectionUtils.isEmpty(storageResponseMessage.getWarnings()))
                {
                    storageResponseMessage.getWarnings().stream().forEach(f -> {
                        response.addWarning(f.getMessage());
                    });
                }
                if (!CollectionUtils.isEmpty(storageResponseMessage.getErrors()))
                {
                    response.setWorkFlowTaskStatus(Status.FAILED);
                    storageResponseMessage.getErrors().stream().forEach(f -> {
                        LOGGER.info("Storage pool validation error - " + f.getMessage());
                        response.addError(f.getMessage());
                    });
                }

            }
        }
    }

    @Override
    public FindScaleIOResponse initializeResponse(Job job)
    {
        FindScaleIOResponse response = new FindScaleIOResponse();
        response.setWorkFlowTaskName(job.getCurrentTask().getTaskName());
        response.setWorkFlowTaskStatus(Status.IN_PROGRESS);
        job.addTaskResponse(job.getStep(), response);
        return response;
    }

}
