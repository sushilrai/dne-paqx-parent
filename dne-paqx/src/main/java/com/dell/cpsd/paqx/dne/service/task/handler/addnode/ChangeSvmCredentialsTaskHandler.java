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
import com.dell.cpsd.paqx.dne.service.model.NodeExpansionRequest;
import com.dell.cpsd.paqx.dne.service.model.Status;
import com.dell.cpsd.paqx.dne.service.model.TaskResponse;
import com.dell.cpsd.paqx.dne.service.task.handler.BaseTaskHandler;
import com.dell.cpsd.virtualization.capabilities.api.RemoteCommandExecutionRequestMessage;
import com.dell.cpsd.virtualization.capabilities.api.VmPasswordUpdateRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

/**
 * Task responsible for changing the scaleio vm credentials.
 * <p>
 * <p>
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries. All Rights Reserved. Dell EMC Confidential/Proprietary Information
 * </p>
 *
 * @since 1.0
 */
public class ChangeSvmCredentialsTaskHandler extends BaseTaskHandler implements IWorkflowTaskHandler
{
    /*
     * The logger instance
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(ChangeIdracCredentialsTaskHandler.class);

    /*
     * The <code>NodeService</code> instance
     */
    private final NodeService nodeService;

    /*
     * The <code>DataServiceRepository</code> instance
     */
    private final DataServiceRepository repository;

    /*
    * The time to wait before sending the request to change the svm credentials
    */
    private final long waitTime;

    /**
     * ScaleIO SVM credential components
     */
    private static final String COMPONENT_TYPE      = "COMMON-SERVER";
    private static final String ENDPOINT_TYPE       = "COMMON-SVM";
    private static final String FACTORY_CREDENTIALS = "SVM-FACTORY";
    private static final String COMMON_CREDENTIALS  = "SVM-COMMON";

    /**
     * ChangeSvmCredentialsTaskHandler constructor.
     *
     * @param nodeService - The <code>NodeService</code> instance
     * @param repository  - The <code>DataServiceRepository</code> instance
     * @param waitTime  - The time to wait before sending the request to change the SVM credentials.
     *                  We need this because after a SVM is deployed and powered up for the first time
     *                  it does a reboot, which makes any other solution for pinging the vm unreliable.
     */
    public ChangeSvmCredentialsTaskHandler(final NodeService nodeService, final DataServiceRepository repository, long waitTime)
    {
        this.nodeService = nodeService;
        this.repository = repository;
        this.waitTime = waitTime;
    }

    @Override
    public boolean executeTask(final Job job)
    {
        LOGGER.info("Execute ChangeSvmCredentialsTaskHandler task");

        TaskResponse response = initializeResponse(job);

        try
        {
            final ComponentEndpointIds factoryComponentEndpointIds = repository
                    .getComponentEndpointIds(COMPONENT_TYPE, ENDPOINT_TYPE, FACTORY_CREDENTIALS);

            if (factoryComponentEndpointIds == null)
            {
                throw new IllegalStateException("No factory component ids found.");
            }

            final ComponentEndpointIds commonComponentEndpointIds = repository
                    .getComponentEndpointIds(COMPONENT_TYPE, ENDPOINT_TYPE, COMMON_CREDENTIALS);

            if (commonComponentEndpointIds == null)
            {
                throw new IllegalStateException("No common component ids found.");
            }

            final NodeExpansionRequest nodeExpansionRequest = job.getInputParams();

            if (nodeExpansionRequest == null)
            {
                throw new IllegalStateException("Job input parameters are null");
            }

            final String scaleIoSvmManagementIpAddress = nodeExpansionRequest.getScaleIoSvmManagementIpAddress();

            if (StringUtils.isEmpty(scaleIoSvmManagementIpAddress))
            {
                throw new IllegalStateException("ScaleIO VM Management IP Address is null");
            }

            VmPasswordUpdateRequest vmPasswordUpdateRequest = new VmPasswordUpdateRequest();
            vmPasswordUpdateRequest.setCredentialName(VmPasswordUpdateRequest.CredentialName.SVM_FACTORY);
            vmPasswordUpdateRequest.setComponentEndpointIds(
                    new com.dell.cpsd.virtualization.capabilities.api.ComponentEndpointIds(factoryComponentEndpointIds.getComponentUuid(),
                            factoryComponentEndpointIds.getEndpointUuid(), factoryComponentEndpointIds.getCredentialUuid()));

            RemoteCommandExecutionRequestMessage requestMessage = new RemoteCommandExecutionRequestMessage();
            requestMessage.setVmPasswordUpdateRequest(vmPasswordUpdateRequest);
            requestMessage.setRemoteCommand(RemoteCommandExecutionRequestMessage.RemoteCommand.CHANGE_PASSWORD);
            requestMessage.setRemoteHost(scaleIoSvmManagementIpAddress);
            requestMessage.setOsType(RemoteCommandExecutionRequestMessage.OsType.LINUX);
            requestMessage.setComponentEndpointIds(
                    new com.dell.cpsd.virtualization.capabilities.api.ComponentEndpointIds(commonComponentEndpointIds.getComponentUuid(),
                            commonComponentEndpointIds.getEndpointUuid(), commonComponentEndpointIds.getCredentialUuid()));

            Thread.sleep(this.waitTime);

            final boolean succeeded = this.nodeService.requestRemoteCommandExecution(requestMessage);

            if (!succeeded)
            {
                throw new IllegalStateException("Change ScaleIO vm credentials request failed");
            }

            response.setWorkFlowTaskStatus(Status.SUCCEEDED);
            return true;
        }
        catch (Exception ex)
        {
            LOGGER.error("Error while changing the svm credentials", ex);
            response.addError(ex.toString());
        }

        response.setWorkFlowTaskStatus(Status.FAILED);
        return false;
    }
}
