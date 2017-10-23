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
import com.dell.cpsd.paqx.dne.domain.scaleio.ScaleIOStoragePool;
import com.dell.cpsd.paqx.dne.domain.vcenter.HostStorageDevice;
import com.dell.cpsd.paqx.dne.repository.DataServiceRepository;
import com.dell.cpsd.paqx.dne.service.NodeService;
import com.dell.cpsd.paqx.dne.service.model.ComponentEndpointIds;
import com.dell.cpsd.paqx.dne.service.model.FindScaleIOResponse;
import com.dell.cpsd.paqx.dne.service.model.Status;
import com.dell.cpsd.paqx.dne.service.model.TaskResponse;
import com.dell.cpsd.paqx.dne.service.task.handler.BaseTaskHandler;
import com.dell.cpsd.paqx.dne.util.NodeInventoryParsingUtil;
import com.dell.cpsd.service.common.client.exception.ServiceExecutionException;
import com.dell.cpsd.service.common.client.exception.ServiceTimeoutException;
import com.dell.cpsd.service.engineering.standards.Device;
import com.dell.cpsd.service.engineering.standards.EssValidateStoragePoolResponseMessage;
import com.dell.cpsd.storage.capabilities.api.CreateStoragePoolRequestMessage;
import com.dell.cpsd.storage.capabilities.api.CreateStoragePoolResponseMessage;
import com.dell.cpsd.storage.capabilities.api.StoragePoolSpec;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * This class implements the logic to find or create a valid Storage pool.
 * <p>
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries.  All Rights Reserved.
 * Dell EMC Confidential/Proprietary Information
 * </p>
 *
 * @since 1.0
 */

public class FindOrCreateValidStoragePoolTaskHandler extends BaseTaskHandler implements IWorkflowTaskHandler
{
    /*
     * The logger instance
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(FindOrCreateValidStoragePoolTaskHandler.class);

    /*
     * Node Service instance used to find and validate Storage pool entries
     */
    private final NodeService nodeService;

    /*
     * The <code>DataServiceRepository</code> instance
     */
    private final DataServiceRepository repository;

    /*
    * ScaleIO gateway credential components
    */
    private static final String COMPONENT_TYPE = "SCALEIO-CLUSTER";

    /*
     * Default name for new storage pool
     */
    private static final String DEFAULT_STORAGE_POOL_NAME = "temp";

    /**
     * Construct an instance based on nodeservice reference
     *
     * @param nodeService
     */
    public FindOrCreateValidStoragePoolTaskHandler(NodeService nodeService, final DataServiceRepository repository)
    {
        this.nodeService = nodeService;
        this.repository = repository;
    }

    @Override
    public boolean executeTask(final Job job)
    {
        LOGGER.info("Execute FindOrCreateValidStoragePoolTaskHandler task");

        FindScaleIOResponse response = initializeResponse(job);

        try
        {
            List<Device> newDevices = NodeInventoryParsingUtil.parseNewDevices(nodeService.getNodeInventoryData(job));

            if (CollectionUtils.isEmpty(newDevices))
            {
                response.addError("No disks found in the node inventory data.");
                return false;
            }

            // retrieve scale IO data
            List<ScaleIOData> scaleIODataList = nodeService.listScaleIOData();

            // retrieve vCenter data
            Map<String, Map<String, HostStorageDevice>> hostToStorageDeviceMap = nodeService
                    .getHostToStorageDeviceMap(nodeService.findVcenterHosts());

            ScaleIOData scaleIOData = scaleIODataList.get(0);

            List<ScaleIOProtectionDomain> protectionDomains = scaleIOData.getProtectionDomains();
            if (protectionDomains != null)
            {
                validateStoragePoolsAndSetResponse(response, newDevices, hostToStorageDeviceMap, protectionDomains, job);
            }

            return CollectionUtils.isEmpty(response.getErrors());
        }
        catch (Exception ex)
        {
            LOGGER.error("Error finding or creating a valid storage pool", ex);
            response.addError(ex.getMessage());
        }

        return false;
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

    private void validateStoragePoolsAndSetResponse(final FindScaleIOResponse response, final List<Device> newDevices,
            final Map<String, Map<String, HostStorageDevice>> hostToStorageDeviceMap, final List<ScaleIOProtectionDomain> protectionDomains,
            final Job job) throws ServiceTimeoutException, ServiceExecutionException
    {
        final TaskResponse findProtectionDomainTaskResponse = job.getTaskResponseMap().get("findProtectionDomain");

        if (findProtectionDomainTaskResponse == null)
        {
            throw new IllegalStateException("No Find Protection Domain task response found");
        }

        final Map<String, String> findProtectionDomainTaskResponseResults = findProtectionDomainTaskResponse.getResults();

        if (findProtectionDomainTaskResponseResults == null)
        {
            throw new IllegalStateException("No Find Protection Domain task response results found");
        }

        final String protectionDomainId = findProtectionDomainTaskResponseResults.get("protectionDomainId");

        if (StringUtils.isEmpty(protectionDomainId))
        {
            throw new IllegalStateException("Protection domain id is null");
        }

        final ScaleIOProtectionDomain scaleIOProtectionDomain = protectionDomains.stream().filter(Objects::nonNull)
                .filter(protectionDomain -> protectionDomain.getId().equals(protectionDomainId)).findFirst().orElse(null);

        if (scaleIOProtectionDomain == null)
        {
            throw new IllegalStateException("Could not find a valid protection domain");
        }

        if (!findValidStoragePool(response, newDevices, hostToStorageDeviceMap, scaleIOProtectionDomain))
        {
            // Go through the scaleio adapter to create new storage pool
            final CreateStoragePoolResponseMessage validStoragePool = createValidStoragePool(protectionDomainId);

            // Sync up the same storage pool into H2 db
            ScaleIOStoragePool newlyCreatedStoragePool = nodeService
                    .createStoragePool(DEFAULT_STORAGE_POOL_NAME, validStoragePool.getStoragePoolId(), protectionDomainId);
            scaleIOProtectionDomain.addStoragePool(newlyCreatedStoragePool);

            if (!findValidStoragePool(response, newDevices, hostToStorageDeviceMap, scaleIOProtectionDomain))
            {
                throw new IllegalStateException("Unable to find or create a valid storage pool");
            }
        }
    }

    private boolean findValidStoragePool(final FindScaleIOResponse response, final List<Device> newDevices,
            final Map<String, Map<String, HostStorageDevice>> hostToStorageDeviceMap, ScaleIOProtectionDomain protectionDomain)
            throws ServiceTimeoutException, ServiceExecutionException
    {
        // as this method is called twice, clear any previous errors or warnings
        response.getErrors().clear();
        response.getWarnings().clear();

        EssValidateStoragePoolResponseMessage storageResponseMessage = nodeService
                .validateStoragePools(protectionDomain.getStoragePools(), newDevices, hostToStorageDeviceMap);

        storageResponseMessage.getWarnings().forEach(f -> response.addWarning(f.getMessage()));

        // if all devices are allocated to existing pools or some of the disks already allocated and no errors
        if (storageResponseMessage.getDeviceToStoragePoolMap().size() == newDevices.size() || CollectionUtils
                .isEmpty(storageResponseMessage.getErrors()))
        {
            LOGGER.info("Storage pool validated successfully.");
            response.setDeviceToStoragePoolMap(storageResponseMessage.getDeviceToStoragePoolMap());
            response.setWorkFlowTaskStatus(Status.SUCCEEDED);
            return true;
        }

        storageResponseMessage.getErrors().forEach(f -> response.addError(f.getMessage()));
        response.setWorkFlowTaskStatus(Status.FAILED);
        return false;
    }

    private CreateStoragePoolResponseMessage createValidStoragePool(final String protectionDomainId)
            throws ServiceTimeoutException, ServiceExecutionException
    {
        final ComponentEndpointIds componentEndpointIds = repository.getComponentEndpointIds(COMPONENT_TYPE);

        if (componentEndpointIds == null)
        {
            throw new IllegalStateException("No component ids found.");
        }

        // create a pool for any other error, it can be due to not having enough remaining slots in the pool
        // or unavailable pool itself
        CreateStoragePoolRequestMessage requestMessage = new CreateStoragePoolRequestMessage();
        requestMessage.setEndpointUrl("https://" + componentEndpointIds.getEndpointUrl());
        requestMessage.setComponentEndpointIds(
                new com.dell.cpsd.storage.capabilities.api.ComponentEndpointIds(componentEndpointIds.getComponentUuid(),
                        componentEndpointIds.getEndpointUuid(), componentEndpointIds.getCredentialUuid()));

        StoragePoolSpec storagePoolSpec = new StoragePoolSpec();
        storagePoolSpec.setProtectionDomainId(protectionDomainId);
        storagePoolSpec.setRmCacheWriteHandlingMode(StoragePoolSpec.RmCacheWriteHandlingMode.PASSTHROUGH);
        storagePoolSpec.setStoragePoolName(DEFAULT_STORAGE_POOL_NAME);
        storagePoolSpec.setUseRmcache(false);
        storagePoolSpec.setZeroPaddingEnabled(true);
        requestMessage.setStoragePoolSpec(storagePoolSpec);

        CreateStoragePoolResponseMessage responseMessage = nodeService.createStoragePool(requestMessage);

        if (responseMessage.getStatus().equals(CreateStoragePoolResponseMessage.Status.FAILED))
        {
            throw new IllegalStateException("Create storage pool request failed");
        }

        return responseMessage;
    }

}
