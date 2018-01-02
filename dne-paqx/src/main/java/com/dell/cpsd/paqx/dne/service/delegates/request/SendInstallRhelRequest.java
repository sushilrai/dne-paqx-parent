/**
 * <p>
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries. All Rights Reserved. Dell EMC Confidential/Proprietary Information
 * </p>
 */
package com.dell.cpsd.paqx.dne.service.delegates.request;

import com.dell.cpsd.RhelInstallationInfo;
import com.dell.cpsd.paqx.dne.amqp.callback.AsynchronousNodeServiceCallback;
import com.dell.cpsd.paqx.dne.service.AsynchronousNodeService;
import com.dell.cpsd.paqx.dne.service.delegates.BaseWorkflowDelegate;
import com.dell.cpsd.paqx.dne.service.delegates.model.NodeDetail;
import com.dell.cpsd.paqx.dne.service.model.IpV4Configuration;
import com.dell.cpsd.paqx.dne.transformers.HostToInstallRhelRequestTransformer;
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
@Qualifier("sendInstallRhelRequest")
public class SendInstallRhelRequest extends BaseWorkflowDelegate
{
    /**
     * The logger instance
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(SendInstallRhelRequest.class);

    /**
     * The <code>NodeService</code> instance
     */
    private final AsynchronousNodeService asynchronousNodeService;

    private final HostToInstallRhelRequestTransformer hostToInstallRhelRequestTransformer;


    @Autowired
    public SendInstallRhelRequest(final AsynchronousNodeService asynchronousNodeService,
                                  final HostToInstallRhelRequestTransformer hostToInstallRhelRequestTransformer)
    {
        super( LOGGER, "Send Install RHEL Request");
        this.asynchronousNodeService = asynchronousNodeService;
        this.hostToInstallRhelRequestTransformer = hostToInstallRhelRequestTransformer;
    }

    /*
    * Auto generates the hostname using the RHEL Management IP Address.
    */
    private String generateHostname(final String rhelManagementIpAddress)
    {
        StringBuilder builder = new StringBuilder();
        builder.append("StorageOnly");
        builder.append("-");
        builder.append(rhelManagementIpAddress.replaceAll("\\.", "-"));
        return builder.toString();
    }

    @Override
    public void delegateExecute(final DelegateExecution delegateExecution)
    {

        NodeDetail nodeDetail = (NodeDetail) delegateExecution.getVariable(NODE_DETAIL);
        updateDelegateStatus("Attempting to " + this.taskName + " on Node " + nodeDetail.getServiceTag() + ".");

        final String symphonyUuid = nodeDetail.getId();

        final String rhelManagementIpAddress = nodeDetail.getStorageOnlyManagementIpAddress();

        final String rhelManagementGatewayIpAddress = nodeDetail.getStorageOnlyManagementGateway();

        final String rhelManagementSubnetMask = nodeDetail.getStorageOnlyManagementSubnetMask();

        String rhelManagementHostname = nodeDetail.getStorageOnlyManagementHostname();
        if (StringUtils.isEmpty(rhelManagementHostname))
        {
            LOGGER.warn("Storage Only Node Management hostname is null, will auto generate hostname");
            rhelManagementHostname = this.generateHostname(rhelManagementIpAddress);
            nodeDetail.setStorageOnlyManagementHostname(rhelManagementHostname);
            delegateExecution.setVariable(NODE_DETAIL, nodeDetail);
            LOGGER.info("Auto generated ESXi Management hostname is " + rhelManagementHostname);
        }


        final IpV4Configuration ipV4Configuration = new IpV4Configuration();
        ipV4Configuration.setStorageOnlyManagementIpAddress(rhelManagementIpAddress);
        ipV4Configuration.setStorageOnlyManagementGateway(rhelManagementGatewayIpAddress);
        ipV4Configuration.setStorageOnlyManagementNetworkMask(rhelManagementSubnetMask);

        final RhelInstallationInfo rhelInstallationInfo = hostToInstallRhelRequestTransformer.transformInstallRhelData(rhelManagementHostname,
                                                                                                                       symphonyUuid,
                                                                                                                       ipV4Configuration);

        AsynchronousNodeServiceCallback<?> requestCallback = null;
        try
        {
            requestCallback = this.asynchronousNodeService.requestInstallRhel(delegateExecution.getProcessInstanceId(),
                                                                              "receiveInstallRhelResponse",
                                                                              INSTALL_RHEL_MESSAGE_ID,
                                                                              rhelInstallationInfo);
        }
        catch (Exception e)
        {
            final String message =
                    "An Unexpected Exception Occurred attempting to  request Install RHEL on Node " + nodeDetail.getServiceTag() + ". Reason: ";
            updateDelegateStatus(message, e);
            throw new BpmnError(SEND_INSTALL_RHEL_FAILED,message + e.getMessage());
        }

        if (requestCallback != null)
        {
            updateDelegateStatus(taskName + " on Node " + nodeDetail.getServiceTag() + " was successful");
        }
        else
        {
            final String message = "Failed to send the request for Install RHEL on Node " + nodeDetail.getServiceTag();
            updateDelegateStatus(message);
            throw new BpmnError(SEND_INSTALL_RHEL_FAILED, message);
        }
    }
}
