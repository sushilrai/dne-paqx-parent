/**
 * <p>
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries.  All Rights Reserved.
 * Dell EMC Confidential/Proprietary Information
 * </p>
 */

package com.dell.cpsd.paqx.dne.service.workflow.addnode;

import com.dell.cpsd.paqx.dne.domain.Job;
import com.dell.cpsd.paqx.dne.domain.WorkflowTask;
import com.dell.cpsd.paqx.dne.repository.DataServiceRepository;
import com.dell.cpsd.paqx.dne.service.BaseService;
import com.dell.cpsd.paqx.dne.service.NodeService;
import com.dell.cpsd.paqx.dne.service.WorkflowService;
import com.dell.cpsd.paqx.dne.service.model.NodeExpansionResponse;
import com.dell.cpsd.paqx.dne.service.task.handler.addnode.AddHostToProtectionDomainTaskHandler;
import com.dell.cpsd.paqx.dne.service.task.handler.addnode.AddNodeToSystemDefinitionTaskHandler;
import com.dell.cpsd.paqx.dne.service.task.handler.addnode.ConfigurePxeBootTaskHandler;
import com.dell.cpsd.paqx.dne.service.task.handler.addnode.NotifyNodeDiscoveryToUpdateStatusTaskHandler;
import com.dell.cpsd.paqx.dne.transformers.HostToInstallEsxiRequestTransformer;
import com.dell.cpsd.sdk.AMQPClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Add node workflow service.
 * <p>
 * <p>
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries. All Rights Reserved. Dell EMC Confidential/Proprietary Information
 * </p>
 *
 * @version 1.0
 * @since 1.0
 */
@Service
public class AddNodeService extends BaseService implements IAddNodeService
{
    private static final Logger LOGGER = LoggerFactory.getLogger(AddNodeService.class);

    @Autowired
    private NodeService nodeService;

    @Autowired
    private AMQPClient sdkAMQPClient;

    @Autowired
    @Qualifier("addNodeWorkflowService")
    private WorkflowService workflowService;

    @Autowired
    private HostToInstallEsxiRequestTransformer hostToInstallEsxiRequestTransformer;

    @Autowired
    private DataServiceRepository repository;

    @Value("${rackhd.sdc.vib.install.repo.url}")
    private String sdcVibUrl;

    private static final long DEPLOY_SVM_WAIT_TIME              = TimeUnit.SECONDS.toMillis(30L);
    private static final long CHANGE_SVM_CREDENTIALS_WAIT_TIME  = TimeUnit.SECONDS.toMillis(75L);
    private static final long SDC_PERF_PROFILE_UPDATE_WAIT_TIME = TimeUnit.SECONDS.toMillis(15L);
    private static final long ESXI_HOST_PING_TIMEOUT            = TimeUnit.SECONDS.toMillis(240L);

    @Override
    public Job createWorkflow(final String workflowType, final String startingStep, final String currentStatus)
    {
        return workflowService.createWorkflow(workflowType, startingStep, currentStatus, addNodeWorkflowTasks());
    }

    @Bean("addNodesWorkflowTasks")
    public Map<String, WorkflowTask> addNodeWorkflowTasks()
    {
        final Map<String, WorkflowTask> workflowTasks = new HashMap<>();

        /*workflowTasks.put("retrieveEsxiDefaultCredentialDetails", esxiCredentialDetailsTask());
        workflowTasks.put("addHostToVcenter", addHostToVcenterTask());
        workflowTasks.put("applyEsxiLicense", applyEsxiLicenseTask());
        workflowTasks.put("updateSoftwareAcceptance", updateSoftwareAcceptanceTask());
        workflowTasks.put("installScaleIoVib", installScaleIoVibTask());
        workflowTasks.put("configureScaleIoVib", configureScaleIoVibTask());
        workflowTasks.put("enterHostMaintenanceMode", enterHostMaintenanceModeTask());
        workflowTasks.put("addHostToDvSwitch", addHostToDvSwitchTask());
        workflowTasks.put("deploySVM", deploySVMTask());
        workflowTasks.put("powerOnSVM", powerOnSVMTask());
        workflowTasks.put("enablePciPassthroughHost", enablePciPassthroughHostTask());
        workflowTasks.put("rebootHost", rebootHostTask());
        workflowTasks.put("exitHostMaintenanceMode1", exitHostMaintenanceModeTask());
        workflowTasks.put("exitHostMaintenanceMode2", exitHostMaintenanceModeTask());
        workflowTasks.put("setPciPassthroughSioVm", setPciPassthroughSioVmTask());*/
        /*workflowTasks.put("installScaleIOSDC", null);
        workflowTasks.put("addNewHostToScaleIO", null);*/
        /*workflowTasks.put("datastoreRename", datastoreRenameTask());
        workflowTasks.put("configureVmNetworkSettings", configureVmNetworkSettingsTask());
        workflowTasks.put("changeSvmCredentials", changeSvmCredentialsTask());
        workflowTasks.put("installSvmPackages", installSvmPackagesTask());
        workflowTasks.put("performanceTuneSvm", performanceTuneSvmTask());
        workflowTasks.put("addHostToProtectionDomain", addHostToProtectionDomainTask());*/
        //workflowTasks.put("updateSdcPerformanceProfile", updateSdcPerformanceProfileTask());
        workflowTasks.put("configurePxeBoot", configurePxeBootTask());
        workflowTasks.put("updateSystemDefinition", updateSystemDefinitionTask());
        workflowTasks.put("notifyNodeDiscoveryToUpdateStatus", notifyNodeDiscoveryToUpdateStatusTask());

        return workflowTasks;
    }

//    @Bean("addHostToProtectionDomain")
//    private WorkflowTask addHostToProtectionDomainTask()
//    {
//        return createTask("Add host to protection domain", new AddHostToProtectionDomainTaskHandler(nodeService, repository));
//    }

    @Bean("updateSystemDefinitionTask")
    private WorkflowTask updateSystemDefinitionTask()
    {
        return createTask("Update System Definition", new AddNodeToSystemDefinitionTaskHandler(this.sdkAMQPClient, repository));
    }

    @Bean("notifyNodeDiscoveryToUpdateStatusTask")
    private WorkflowTask notifyNodeDiscoveryToUpdateStatusTask()
    {
        return createTask("Notify node discovery to update status", new NotifyNodeDiscoveryToUpdateStatusTaskHandler(this.nodeService,"Completed"));
    }

    @Bean("configurePxeBootTask")
    private WorkflowTask configurePxeBootTask()
    {
        return createTask("Configure PXE boot", new ConfigurePxeBootTaskHandler(nodeService));
    }

    public Job findJob(UUID jobId)
    {
        return workflowService.findJob(jobId);
    }

    public NodeExpansionResponse makeNodeExpansionResponse(final Job job)
    {
        return makeNodeExpansionResponse(job, workflowService);
    }

    public WorkflowService getWorkflowService()
    {
        return workflowService;
    }

    public void setWorkflowService(WorkflowService workflowService)
    {
        this.workflowService = workflowService;
    }
}
