/**
 * <p>
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries. All Rights Reserved. Dell EMC Confidential/Proprietary Information
 * </p>
 */

package com.dell.cpsd.paqx.dne.service.task.handler.addnode;

import com.dell.cpsd.paqx.dne.domain.IWorkflowTaskHandler;
import com.dell.cpsd.paqx.dne.domain.Job;
import com.dell.cpsd.paqx.dne.domain.vcenter.Host;
import com.dell.cpsd.paqx.dne.domain.vcenter.HostDnsConfig;
import com.dell.cpsd.paqx.dne.repository.DataServiceRepository;
import com.dell.cpsd.paqx.dne.service.NodeService;
import com.dell.cpsd.paqx.dne.service.model.ComponentEndpointIds;
import com.dell.cpsd.paqx.dne.service.model.DatastoreRenameTaskResponse;
import com.dell.cpsd.paqx.dne.service.model.DeployScaleIoVmTaskResponse;
import com.dell.cpsd.paqx.dne.service.model.InstallEsxiTaskResponse;
import com.dell.cpsd.paqx.dne.service.model.NodeExpansionRequest;
import com.dell.cpsd.paqx.dne.service.model.Status;
import com.dell.cpsd.paqx.dne.service.task.handler.BaseTaskHandler;
import com.dell.cpsd.virtualization.capabilities.api.Credentials;
import com.dell.cpsd.virtualization.capabilities.api.DeployVMFromTemplateRequestMessage;
import com.dell.cpsd.virtualization.capabilities.api.NicSetting;
import com.dell.cpsd.virtualization.capabilities.api.VirtualMachineCloneSpec;
import com.dell.cpsd.virtualization.capabilities.api.VirtualMachineConfigSpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.List;

/**
 * TODO: Document Usage
 *
 * <p>
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries. All Rights Reserved. Dell EMC Confidential/Proprietary Information
 * </p>
 *
 * @version 1.0
 * @since 1.0
 */
public class DeployScaleIoVmTaskHandler extends BaseTaskHandler implements IWorkflowTaskHandler
{
    /**
     * The logger instance
     */
    private static final Logger LOGGER                   = LoggerFactory.getLogger(DeployScaleIoVmTaskHandler.class);
    private static final String SCALEIO_VM_PREFIX        = "ScaleIO-";
    private static final String SCALEIO_TEMPLATE_VM_NAME = "EMC ScaleIO SVM Template.*";
    private static final int    SCALEIO_VM_NUM_CPU       = 8;
    private static final int    SCALEIO_VM_RAM           = 8192;

    /**
     * The <code>NodeService</code> instance
     */
    private final NodeService           nodeService;
    private final DataServiceRepository repository;

    public DeployScaleIoVmTaskHandler(final NodeService nodeService, final DataServiceRepository repository)
    {
        this.nodeService = nodeService;
        this.repository = repository;
    }

    @Override
    public boolean executeTask(final Job job)
    {
        LOGGER.info("Execute Deploy ScaleIO VM From Template task");

        final DeployScaleIoVmTaskResponse response = initializeResponse(job);

        try
        {
            final Validate validate = new Validate(job).invoke();
            final ComponentEndpointIds componentEndpointIds = validate.getComponentEndpointIds();
            final String hostname = validate.getHostname();
            final String dataCenterName = validate.getDataCenterName();
            final String newScaleIoVmName = validate.getNewScaleIoVmName();
            final String newScaleIoVmHostname = validate.getNewScaleIoVmHostname();
            final String domainName = validate.getNewScaleIoVmDomainName();
            final List<String> dnsServers = validate.getNewScaleIoVmDnsServers();
            final List<NicSetting> nicSettings = validate.getNewScaleIoVmNicSettings();

            final VirtualMachineCloneSpec virtualMachineCloneSpec = new VirtualMachineCloneSpec();
            virtualMachineCloneSpec.setPoweredOn(false);
            virtualMachineCloneSpec.setTemplate(false);
            virtualMachineCloneSpec.setDomain(domainName);

            final VirtualMachineConfigSpec virtualMachineConfigSpec = new VirtualMachineConfigSpec();
            virtualMachineConfigSpec.setHostName(newScaleIoVmHostname);
            virtualMachineConfigSpec.setNumCPUs(SCALEIO_VM_NUM_CPU);
            virtualMachineConfigSpec.setMemoryMB(SCALEIO_VM_RAM);
            virtualMachineConfigSpec.setDnsServerList(dnsServers);
            virtualMachineConfigSpec.setNicSettings(nicSettings);
            virtualMachineCloneSpec.setVirtualMachineConfigSpec(virtualMachineConfigSpec);

            final DeployVMFromTemplateRequestMessage requestMessage = new DeployVMFromTemplateRequestMessage();
            requestMessage.setCredentials(new Credentials(componentEndpointIds.getEndpointUrl(), null, null));
            requestMessage.setComponentEndpointIds(
                    new com.dell.cpsd.virtualization.capabilities.api.ComponentEndpointIds(componentEndpointIds.getComponentUuid(),
                            componentEndpointIds.getEndpointUuid(), componentEndpointIds.getCredentialUuid()));
            requestMessage.setHostName(hostname);
            requestMessage.setTemplateName(SCALEIO_TEMPLATE_VM_NAME);
            requestMessage.setDatacenterName(dataCenterName);
            requestMessage.setNewVMName(newScaleIoVmName);
            requestMessage.setVirtualMachineCloneSpec(virtualMachineCloneSpec);

            final boolean succeeded = this.nodeService.requestDeployScaleIoVm(requestMessage);

            if (!succeeded)
            {
                throw new IllegalStateException("Request deploy ScaleIO VM failed");
            }

            response.setWorkFlowTaskStatus(Status.SUCCEEDED);
            response.setNewVMName(newScaleIoVmName);
            return true;
        }
        catch (Exception e)
        {
            LOGGER.error("Error deploying ScaleIO VM", e);
            response.addError(e.toString());
        }

        response.setWorkFlowTaskStatus(Status.FAILED);
        return false;
    }

    @Override
    public DeployScaleIoVmTaskResponse initializeResponse(Job job)
    {
        final DeployScaleIoVmTaskResponse response = new DeployScaleIoVmTaskResponse();
        response.setWorkFlowTaskName(job.getCurrentTask().getTaskName());
        response.setWorkFlowTaskStatus(Status.IN_PROGRESS);
        job.addTaskResponse(job.getStep(), response);

        return response;
    }

    private class Validate
    {
        private final Job                  job;
        private       ComponentEndpointIds componentEndpointIds;
        private       String               hostname;
        private       String               datastoreName;
        private       String               dataCenterName;
        private       String               newScaleIoVmIpAddress;
        private       String               newScaleIoVmName;
        private       String               newScaleIoVmHostname;
        private       String               newScaleIoVmDomainName;
        private       List<String>         newScaleIoVmDnsServers;
        private       List<NicSetting>     newScaleIoVmNicSettings;

        Validate(final Job job)
        {
            this.job = job;
        }

        Validate invoke()
        {
            componentEndpointIds = repository.getVCenterComponentEndpointIdsByEndpointType("VCENTER-CUSTOMER");

            if (componentEndpointIds == null)
            {
                throw new IllegalStateException("No VCenter components found.");
            }

            final InstallEsxiTaskResponse installEsxiTaskResponse = (InstallEsxiTaskResponse)job.getTaskResponseMap().get("installEsxi");

            if (installEsxiTaskResponse == null)
            {
                throw new IllegalStateException("No Install ESXi task response found");
            }

            hostname = installEsxiTaskResponse.getHostname();

            if (StringUtils.isEmpty(hostname))
            {
                throw new IllegalStateException("Host name is null");
            }

            final DatastoreRenameTaskResponse datastoreRenameTaskResponse = (DatastoreRenameTaskResponse) job.getTaskResponseMap()
                    .get("datastoreRename");

            if (datastoreRenameTaskResponse == null)
            {
                throw new IllegalStateException("No Datastore Rename task response found");
            }

            datastoreName = datastoreRenameTaskResponse.getDatastoreName();

            if (StringUtils.isEmpty(datastoreName))
            {
                throw new IllegalStateException("Datastore name is null");
            }

            final NodeExpansionRequest inputParams = job.getInputParams();

            if (inputParams == null)
            {
                throw new IllegalStateException("Job Input Params are null");
            }

            final String clusterName = inputParams.getClusterName();

            if (StringUtils.isEmpty(clusterName))
            {
                throw new IllegalStateException("Cluster Name is null");
            }

            dataCenterName = repository.getDataCenterName(clusterName);

            if (StringUtils.isEmpty(dataCenterName))
            {
                throw new IllegalStateException("DataCenter name is null");
            }

            final String scaleIOSVMManagementIpAddress = inputParams.getScaleIoSvmManagementIpAddress();

            if (StringUtils.isEmpty(scaleIOSVMManagementIpAddress))
            {
                throw new IllegalStateException("ScaleIO Management IP Address is null");
            }

            newScaleIoVmIpAddress = scaleIOSVMManagementIpAddress;
            newScaleIoVmName = SCALEIO_VM_PREFIX + newScaleIoVmIpAddress;
            newScaleIoVmHostname = newScaleIoVmName.replace(".", "-");

            newScaleIoVmDomainName = repository.getDomainName();

            if (StringUtils.isEmpty(newScaleIoVmDomainName))
            {
                throw new IllegalStateException("Domain name is null");
            }

            final Host existingVCenterHost = repository.getExistingVCenterHost();
            final HostDnsConfig hostDnsConfig = existingVCenterHost.getHostDnsConfig();
            newScaleIoVmDnsServers = hostDnsConfig.getDnsConfigIPs();

            if (CollectionUtils.isEmpty(newScaleIoVmDnsServers))
            {
                throw new IllegalStateException("No DNS config IPs");
            }

            final String esxiManagementIpAddress = inputParams.getEsxiManagementIpAddress();

            if (StringUtils.isEmpty(esxiManagementIpAddress))
            {
                throw new IllegalStateException("ESXi Management IP Address is null");
            }

            final String esxiManagementGatewayIpAddress = inputParams.getEsxiManagementGatewayIpAddress();

            if (StringUtils.isEmpty(esxiManagementGatewayIpAddress))
            {
                throw new IllegalStateException("ESXi Management Gateway IP Address is null");
            }

            final String esxiManagementSubnetMask = inputParams.getEsxiManagementSubnetMask();

            if (StringUtils.isEmpty(esxiManagementSubnetMask))
            {
                throw new IllegalStateException("ESXi Management Subnet Mask is null");
            }

            NicSetting nicSetting = new NicSetting();
            nicSetting.setIpAddress(newScaleIoVmIpAddress);
            nicSetting.setGateway(Arrays.asList(esxiManagementGatewayIpAddress));
            nicSetting.setSubnetMask(esxiManagementSubnetMask);
            newScaleIoVmNicSettings = Arrays.asList(nicSetting, nicSetting, nicSetting);

            return this;
        }

        ComponentEndpointIds getComponentEndpointIds()
        {
            return componentEndpointIds;
        }

        String getHostname()
        {
            return hostname;
        }

        String getDataCenterName()
        {
            return dataCenterName;
        }

        String getNewScaleIoVmName() { return newScaleIoVmName; }

        String getNewScaleIoVmHostname() { return newScaleIoVmHostname; }

        String getNewScaleIoVmDomainName() { return newScaleIoVmDomainName; }

        List<String> getNewScaleIoVmDnsServers() { return newScaleIoVmDnsServers; }

        List<NicSetting> getNewScaleIoVmNicSettings() { return newScaleIoVmNicSettings; }
    }
}
