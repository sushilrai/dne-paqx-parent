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
import com.dell.cpsd.paqx.dne.service.model.ConfigureScaleIoVibTaskResponse;
import com.dell.cpsd.paqx.dne.service.model.NodeExpansionRequest;
import com.dell.cpsd.paqx.dne.service.model.Status;
import com.dell.cpsd.paqx.dne.service.model.TaskResponse;
import com.dell.cpsd.paqx.dne.service.task.handler.BaseTaskHandler;
import com.dell.cpsd.storage.capabilities.api.PerformanceProfileRequest;
import com.dell.cpsd.storage.capabilities.api.SioSdcUpdatePerformanceProfileRequestMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

/**
 * Task responsible for updating/setting the SDC performance profile.
 * <p>
 * <p>
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries. All Rights Reserved. Dell EMC Confidential/Proprietary Information
 * </p>
 *
 * @version 1.0
 * @since 1.0
 */
public class UpdateSdcPerformanceProfileTaskHandler extends BaseTaskHandler implements IWorkflowTaskHandler
{
    /*
     * The logger instance
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(UpdateSdcPerformanceProfileTaskHandler.class);

    /*
     * The <code>NodeService</code> instance
     */
    private final NodeService nodeService;

    /*
     * The <code>DataServiceRepository</code> instance
     */
    private final DataServiceRepository repository;

    private static final String COMPONENT_TYPE = "SCALEIO-CLUSTER";

    /**
     * UpdateSdcPerformanceProfileTaskHandler constructor
     *
     * @param nodeService - The <code>NodeService</code> instance
     * @param repository  - The <code>DataServiceRepository</code> instance
     */
    public UpdateSdcPerformanceProfileTaskHandler(final NodeService nodeService, final DataServiceRepository repository)
    {
        this.nodeService = nodeService;
        this.repository = repository;
    }

    @Override
    public boolean executeTask(final Job job)
    {
        LOGGER.info("Execute UpdateSdcPerformanceProfileTaskHandler task");

        TaskResponse response = initializeResponse(job);

        try
        {
            final ComponentEndpointIds componentEndpointIds = repository.getComponentEndpointIds(COMPONENT_TYPE);

            if (componentEndpointIds == null)
            {
                throw new IllegalStateException("No component ids found.");
            }

            final NodeExpansionRequest nodeExpansionRequest = job.getInputParams();

            if (nodeExpansionRequest == null)
            {
                throw new IllegalStateException("Job input parameters are null");
            }

            final String scaleIoSdcIpAddress = nodeExpansionRequest.getEsxiManagementIpAddress();

            if (StringUtils.isEmpty(scaleIoSdcIpAddress))
            {
                throw new IllegalStateException("ScaleIO SDC IP Address is null");
            }

            final ConfigureScaleIoVibTaskResponse configureScaleIoVibTaskResponse = (ConfigureScaleIoVibTaskResponse) job
                    .getTaskResponseMap().get("configureScaleIoVib");

            if (configureScaleIoVibTaskResponse == null)
            {
                throw new IllegalStateException("No Configure ScaleIO VIB response found");
            }

            final String sdcGUID = configureScaleIoVibTaskResponse.getIoctlIniGuidStr();

            if (StringUtils.isEmpty(sdcGUID))
            {
                throw new IllegalStateException("ScaleIO SDC GUID is null");
            }

            final PerformanceProfileRequest performanceProfileRequest = new PerformanceProfileRequest();
            performanceProfileRequest.setSdcIp(scaleIoSdcIpAddress);
            performanceProfileRequest.setSdcGuid(sdcGUID);
            performanceProfileRequest.setPerfProfile(PerformanceProfileRequest.PerfProfile.HIGH_PERFORMANCE);

            final SioSdcUpdatePerformanceProfileRequestMessage requestMessage = new SioSdcUpdatePerformanceProfileRequestMessage();
            requestMessage.setPerformanceProfileRequest(performanceProfileRequest);
            requestMessage.setEndpointUrl("https://" + componentEndpointIds.getEndpointUrl());
            requestMessage.setComponentEndpointIds(
                    new com.dell.cpsd.storage.capabilities.api.ComponentEndpointIds(componentEndpointIds.getComponentUuid(),
                            componentEndpointIds.getEndpointUuid(), componentEndpointIds.getCredentialUuid()));

            final boolean succeeded = this.nodeService.requestUpdateSdcPerformanceProfile(requestMessage);

            if (!succeeded)
            {
                throw new IllegalStateException("Update ScaleIO SDC performance profile request failed");
            }

            response.setWorkFlowTaskStatus(Status.SUCCEEDED);
            return true;
        }
        catch (Exception ex)
        {
            LOGGER.error("Error while updating ScaleIO SDC performance profile", ex);
            response.addError(ex.toString());
        }

        response.setWorkFlowTaskStatus(Status.FAILED);
        return false;
    }
}
