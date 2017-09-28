/**
 * <p>
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries. All Rights Reserved. Dell EMC Confidential/Proprietary Information
 * </p>
 */

package com.dell.cpsd.paqx.dne.service.task.handler.addnode;

import com.dell.cpsd.paqx.dne.domain.Job;
import com.dell.cpsd.paqx.dne.domain.WorkflowTask;
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
import com.dell.cpsd.paqx.dne.service.model.TaskResponse;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

/**
 * The tests for the DeployScaleIoVmTaskHandler class.
 *
 * <p>
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries. All Rights Reserved. Dell EMC Confidential/Proprietary Information
 * </p>
 *
 * @since 1.0
 */
@RunWith(MockitoJUnitRunner.class)
public class DeployScaleIoVmTaskHandlerTest
{
    @Mock
    private WorkflowTask task;

    @Mock
    private NodeService service;

    @Mock
    private DataServiceRepository repository;

    @Mock
    private Job job;

    @Mock
    private DeployScaleIoVmTaskResponse response;

    @Mock
    private NodeExpansionRequest request;

    @Mock
    private InstallEsxiTaskResponse installEsxiTaskResponse;

    @Mock
    private DatastoreRenameTaskResponse datastoreRenameTaskResponse;

    @Mock
    private ComponentEndpointIds componentEndpointIds;

    @Mock
    private Map<String, TaskResponse> taskResponseMap;

    @Mock
    private Host host;

    @Mock
    private HostDnsConfig hostDnsConfig;

    private DeployScaleIoVmTaskHandler handler;

    private String hostname = "hostname_1.2.3.4";
    private String datastoreName = "datastore_01";
    private String domainName = "example.com";
    private String clusterName = "cluster_01";
    private String dataCenterName = "datacenter_01";
    private String scaleIOSVMManagementIpAddress = "1.2.3.4";
    private String taskName = "deployScaleIoVmTask";
    private String stepName = "deployScaleIoVmStep";
    private String esxiManagementIpAddress = "3.3.3.3";
    private String esxiManagementGatewayIpAddress = "4.3.2.1";
    private String esxiManagementSubnetMask = "255.255.255.0";

    private List<String> dnsConfigIps;

    @Before
    public void setUp() throws Exception
    {
        this.handler = spy(new DeployScaleIoVmTaskHandler(this.service, this.repository));

        this.dnsConfigIps = new ArrayList<>();
        this.dnsConfigIps.add("1.1.1.1");
        this.dnsConfigIps.add("2.2.2.2");
    }

    /**
     * {@link com.dell.cpsd.paqx.dne.service.task.handler.addnode.DeployScaleIoVmTaskHandler#executeTask(com.dell.cpsd.paqx.dne.domain.Job)}.
     *
     * @throws Exception
     */
    @Test
    public void executeTask_should_successfully_deploy_the_scaleio_vm() throws Exception
    {
        doReturn(this.response).when(this.handler).initializeResponse(this.job);
        doReturn(this.componentEndpointIds).when(this.repository).getVCenterComponentEndpointIdsByEndpointType(anyString());
        doReturn(this.taskResponseMap).when(this.job).getTaskResponseMap();
        doReturn(this.installEsxiTaskResponse, this.datastoreRenameTaskResponse).when(this.taskResponseMap).get(anyString());
        doReturn(this.hostname).when(this.installEsxiTaskResponse).getHostname();
        doReturn(this.datastoreName).when(this.datastoreRenameTaskResponse).getDatastoreName();
        doReturn(this.request).when(this.job).getInputParams();
        doReturn(this.clusterName).when(this.request).getClusterName();
        doReturn(this.scaleIOSVMManagementIpAddress).when(this.request).getScaleIoSvmManagementIpAddress();
        doReturn(this.dataCenterName).when(this.repository).getDataCenterName(anyString());
        doReturn(this.domainName).when(this.repository).getDomainName();
        doReturn(this.host).when(this.repository).getExistingVCenterHost();
        doReturn(this.hostDnsConfig).when(this.host).getHostDnsConfig();
        doReturn(this.dnsConfigIps).when(this.hostDnsConfig).getDnsConfigIPs();
        doReturn(this.esxiManagementIpAddress).when(this.request).getEsxiManagementIpAddress();
        doReturn(this.esxiManagementGatewayIpAddress).when(this.request).getEsxiManagementGatewayIpAddress();
        doReturn(this.esxiManagementSubnetMask).when(this.request).getEsxiManagementSubnetMask();
        doReturn(true).when(this.service).requestDeployScaleIoVm(any());

        boolean result  = this.handler.executeTask(this.job);

        assertThat(result, is(true));
        verify(this.response).setWorkFlowTaskStatus(Status.SUCCEEDED);
        verify(this.response).setNewVMName(argThat(value -> value.contains(this.scaleIOSVMManagementIpAddress)));
        verify(this.response, never()).addError(anyString());
    }

    /**
     * {@link com.dell.cpsd.paqx.dne.service.task.handler.addnode.DeployScaleIoVmTaskHandler#executeTask(com.dell.cpsd.paqx.dne.domain.Job)}.
     *
     * @throws Exception
     */
    @Test
    public void executeTask_should_fail_the_workflow_when_there_are_no_vcenter_components() throws Exception
    {
        ComponentEndpointIds nullComponentEndpointIds = null;

        doReturn(this.response).when(this.handler).initializeResponse(this.job);
        doReturn(nullComponentEndpointIds).when(this.repository).getVCenterComponentEndpointIdsByEndpointType(anyString());

        boolean result  = this.handler.executeTask(this.job);

        assertThat(result, is(false));
        verify(this.response).setWorkFlowTaskStatus(Status.FAILED);
        verify(this.response, never()).setNewVMName(anyString());
        verify(this.response).addError(anyString());
    }

    /**
     * {@link com.dell.cpsd.paqx.dne.service.task.handler.addnode.DeployScaleIoVmTaskHandler#executeTask(com.dell.cpsd.paqx.dne.domain.Job)}.
     *
     * @throws Exception
     */
    @Test
    public void executeTask_should_fail_the_workflow_when_there_is_no_esxi_task_response() throws Exception
    {
        InstallEsxiTaskResponse nullInstallEsxiTaskResponse = null;

        doReturn(this.response).when(this.handler).initializeResponse(this.job);
        doReturn(this.componentEndpointIds).when(this.repository).getVCenterComponentEndpointIdsByEndpointType(anyString());
        doReturn(this.taskResponseMap).when(this.job).getTaskResponseMap();
        doReturn(nullInstallEsxiTaskResponse).when(this.taskResponseMap).get(anyString());

        boolean result  = this.handler.executeTask(this.job);

        assertThat(result, is(false));
        verify(this.response).setWorkFlowTaskStatus(Status.FAILED);
        verify(this.response, never()).setNewVMName(anyString());
        verify(this.response).addError(anyString());
    }

    /**
     * {@link com.dell.cpsd.paqx.dne.service.task.handler.addnode.DeployScaleIoVmTaskHandler#executeTask(com.dell.cpsd.paqx.dne.domain.Job)}.
     *
     * @throws Exception
     */
    @Test
    public void executeTask_should_fail_the_workflow_when_there_is_no_hostname() throws Exception
    {
        String nullHostname = null;

        doReturn(this.response).when(this.handler).initializeResponse(this.job);
        doReturn(this.componentEndpointIds).when(this.repository).getVCenterComponentEndpointIdsByEndpointType(anyString());
        doReturn(this.taskResponseMap).when(this.job).getTaskResponseMap();
        doReturn(this.installEsxiTaskResponse).when(this.taskResponseMap).get(anyString());
        doReturn(nullHostname).when(this.installEsxiTaskResponse).getHostname();

        boolean result  = this.handler.executeTask(this.job);

        assertThat(result, is(false));
        verify(this.response).setWorkFlowTaskStatus(Status.FAILED);
        verify(this.response, never()).setNewVMName(anyString());
        verify(this.response).addError(anyString());
    }

    /**
     * {@link com.dell.cpsd.paqx.dne.service.task.handler.addnode.DeployScaleIoVmTaskHandler#executeTask(com.dell.cpsd.paqx.dne.domain.Job)}.
     *
     * @throws Exception
     */
    @Test
    public void executeTask_should_fail_the_workflow_when_there_is_no_datastore_rename_task_response() throws Exception
    {
        DatastoreRenameTaskResponse nullDatastoreRenameTaskResponse = null;

        doReturn(this.response).when(this.handler).initializeResponse(this.job);
        doReturn(this.componentEndpointIds).when(this.repository).getVCenterComponentEndpointIdsByEndpointType(anyString());
        doReturn(this.taskResponseMap).when(this.job).getTaskResponseMap();
        doReturn(this.installEsxiTaskResponse, nullDatastoreRenameTaskResponse).when(this.taskResponseMap).get(anyString());
        doReturn(this.hostname).when(this.installEsxiTaskResponse).getHostname();

        boolean result  = this.handler.executeTask(this.job);

        assertThat(result, is(false));
        verify(this.response).setWorkFlowTaskStatus(Status.FAILED);
        verify(this.response, never()).setNewVMName(anyString());
        verify(this.response).addError(anyString());
    }

    /**
     * {@link com.dell.cpsd.paqx.dne.service.task.handler.addnode.DeployScaleIoVmTaskHandler#executeTask(com.dell.cpsd.paqx.dne.domain.Job)}.
     *
     * @throws Exception
     */
    @Test
    public void executeTask_should_fail_the_workflow_when_there_is_no_datastore_name() throws Exception
    {
        String nullDatastoreName = null;

        doReturn(this.response).when(this.handler).initializeResponse(this.job);
        doReturn(this.componentEndpointIds).when(this.repository).getVCenterComponentEndpointIdsByEndpointType(anyString());
        doReturn(this.taskResponseMap).when(this.job).getTaskResponseMap();
        doReturn(this.installEsxiTaskResponse, this.datastoreRenameTaskResponse).when(this.taskResponseMap).get(anyString());
        doReturn(this.hostname).when(this.installEsxiTaskResponse).getHostname();
        doReturn(nullDatastoreName).when(this.datastoreRenameTaskResponse).getDatastoreName();

        boolean result  = this.handler.executeTask(this.job);

        assertThat(result, is(false));
        verify(this.response).setWorkFlowTaskStatus(Status.FAILED);
        verify(this.response, never()).setNewVMName(anyString());
        verify(this.response).addError(anyString());
    }

    /**
     * {@link com.dell.cpsd.paqx.dne.service.task.handler.addnode.DeployScaleIoVmTaskHandler#executeTask(com.dell.cpsd.paqx.dne.domain.Job)}.
     *
     * @throws Exception
     */
    @Test
    public void executeTask_should_fail_the_workflow_when_there_is_no_NodeExpansionRequest_object() throws Exception
    {
        NodeExpansionRequest nullRequest = null;

        doReturn(this.response).when(this.handler).initializeResponse(this.job);
        doReturn(this.componentEndpointIds).when(this.repository).getVCenterComponentEndpointIdsByEndpointType(anyString());
        doReturn(this.taskResponseMap).when(this.job).getTaskResponseMap();
        doReturn(this.installEsxiTaskResponse, this.datastoreRenameTaskResponse).when(this.taskResponseMap).get(anyString());
        doReturn(this.hostname).when(this.installEsxiTaskResponse).getHostname();
        doReturn(this.datastoreName).when(this.datastoreRenameTaskResponse).getDatastoreName();
        doReturn(nullRequest).when(this.job).getInputParams();

        boolean result  = this.handler.executeTask(this.job);

        assertThat(result, is(false));
        verify(this.response).setWorkFlowTaskStatus(Status.FAILED);
        verify(this.response, never()).setNewVMName(anyString());
        verify(this.response).addError(anyString());
    }

    /**
     * {@link com.dell.cpsd.paqx.dne.service.task.handler.addnode.DeployScaleIoVmTaskHandler#executeTask(com.dell.cpsd.paqx.dne.domain.Job)}.
     *
     * @throws Exception
     */
    @Test
    public void executeTask_should_fail_the_workflow_when_there_is_no_clustername() throws Exception
    {
        String nullClusterName = null;

        doReturn(this.response).when(this.handler).initializeResponse(this.job);
        doReturn(this.componentEndpointIds).when(this.repository).getVCenterComponentEndpointIdsByEndpointType(anyString());
        doReturn(this.taskResponseMap).when(this.job).getTaskResponseMap();
        doReturn(this.installEsxiTaskResponse, this.datastoreRenameTaskResponse).when(this.taskResponseMap).get(anyString());
        doReturn(this.hostname).when(this.installEsxiTaskResponse).getHostname();
        doReturn(this.datastoreName).when(this.datastoreRenameTaskResponse).getDatastoreName();
        doReturn(this.request).when(this.job).getInputParams();
        doReturn(nullClusterName).when(this.request).getClusterName();

        boolean result  = this.handler.executeTask(this.job);

        assertThat(result, is(false));
        verify(this.response).setWorkFlowTaskStatus(Status.FAILED);
        verify(this.response, never()).setNewVMName(anyString());
        verify(this.response).addError(anyString());
    }

    /**
     * {@link com.dell.cpsd.paqx.dne.service.task.handler.addnode.DeployScaleIoVmTaskHandler#executeTask(com.dell.cpsd.paqx.dne.domain.Job)}.
     *
     * @throws Exception
     */
    @Test
    public void executeTask_should_fail_the_workflow_when_there_is_no_datacenter_name() throws Exception
    {
        String nullDataCenterName = null;

        doReturn(this.response).when(this.handler).initializeResponse(this.job);
        doReturn(this.componentEndpointIds).when(this.repository).getVCenterComponentEndpointIdsByEndpointType(anyString());
        doReturn(this.taskResponseMap).when(this.job).getTaskResponseMap();
        doReturn(this.installEsxiTaskResponse, this.datastoreRenameTaskResponse).when(this.taskResponseMap).get(anyString());
        doReturn(this.hostname).when(this.installEsxiTaskResponse).getHostname();
        doReturn(this.datastoreName).when(this.datastoreRenameTaskResponse).getDatastoreName();
        doReturn(this.request).when(this.job).getInputParams();
        doReturn(this.clusterName).when(this.request).getClusterName();
        doReturn(nullDataCenterName).when(this.repository).getDataCenterName(anyString());

        boolean result  = this.handler.executeTask(this.job);

        assertThat(result, is(false));
        verify(this.response).setWorkFlowTaskStatus(Status.FAILED);
        verify(this.response, never()).setNewVMName(anyString());
        verify(this.response).addError(anyString());
    }

    /**
     * {@link com.dell.cpsd.paqx.dne.service.task.handler.addnode.DeployScaleIoVmTaskHandler#executeTask(com.dell.cpsd.paqx.dne.domain.Job)}.
     *
     * @throws Exception
     */
    @Test
    public void executeTask_should_fail_the_workflow_when_there_is_no_scaleIOSVMManagementIpAddress() throws Exception
    {
        String nullScaleIOSVMManagementIpAddress = null;

        doReturn(this.response).when(this.handler).initializeResponse(this.job);
        doReturn(this.componentEndpointIds).when(this.repository).getVCenterComponentEndpointIdsByEndpointType(anyString());
        doReturn(this.taskResponseMap).when(this.job).getTaskResponseMap();
        doReturn(this.installEsxiTaskResponse, this.datastoreRenameTaskResponse).when(this.taskResponseMap).get(anyString());
        doReturn(this.hostname).when(this.installEsxiTaskResponse).getHostname();
        doReturn(this.datastoreName).when(this.datastoreRenameTaskResponse).getDatastoreName();
        doReturn(this.request).when(this.job).getInputParams();
        doReturn(this.clusterName).when(this.request).getClusterName();
        doReturn(this.dataCenterName).when(this.repository).getDataCenterName(anyString());
        doReturn(nullScaleIOSVMManagementIpAddress).when(this.request).getScaleIoSvmManagementIpAddress();

        boolean result  = this.handler.executeTask(this.job);

        assertThat(result, is(false));
        verify(this.response).setWorkFlowTaskStatus(Status.FAILED);
        verify(this.response, never()).setNewVMName(anyString());
        verify(this.response).addError(anyString());
    }

    /**
     * {@link com.dell.cpsd.paqx.dne.service.task.handler.addnode.DeployScaleIoVmTaskHandler#executeTask(com.dell.cpsd.paqx.dne.domain.Job)}.
     *
     * @throws Exception
     */
    @Test
    public void executeTask_should_fail_the_workflow_when_there_is_no_domainname() throws Exception
    {
        String nullDomainName = null;

        doReturn(this.response).when(this.handler).initializeResponse(this.job);
        doReturn(this.componentEndpointIds).when(this.repository).getVCenterComponentEndpointIdsByEndpointType(anyString());
        doReturn(this.taskResponseMap).when(this.job).getTaskResponseMap();
        doReturn(this.installEsxiTaskResponse, this.datastoreRenameTaskResponse).when(this.taskResponseMap).get(anyString());
        doReturn(this.hostname).when(this.installEsxiTaskResponse).getHostname();
        doReturn(this.datastoreName).when(this.datastoreRenameTaskResponse).getDatastoreName();
        doReturn(this.request).when(this.job).getInputParams();
        doReturn(this.clusterName).when(this.request).getClusterName();
        doReturn(this.scaleIOSVMManagementIpAddress).when(this.request).getScaleIoSvmManagementIpAddress();
        doReturn(this.dataCenterName).when(this.repository).getDataCenterName(anyString());
        doReturn(nullDomainName).when(this.repository).getDomainName();

        boolean result  = this.handler.executeTask(this.job);

        assertThat(result, is(false));
        verify(this.response).setWorkFlowTaskStatus(Status.FAILED);
        verify(this.response, never()).setNewVMName(anyString());
        verify(this.response).addError(anyString());
    }

    /**
     * {@link com.dell.cpsd.paqx.dne.service.task.handler.addnode.DeployScaleIoVmTaskHandler#executeTask(com.dell.cpsd.paqx.dne.domain.Job)}.
     *
     * @throws Exception
     */
    @Test
    public void executeTask_should_fail_the_workflow_when_there_is_no_dns_ips() throws Exception
    {
        doReturn(this.response).when(this.handler).initializeResponse(this.job);
        doReturn(this.componentEndpointIds).when(this.repository).getVCenterComponentEndpointIdsByEndpointType(anyString());
        doReturn(this.taskResponseMap).when(this.job).getTaskResponseMap();
        doReturn(this.installEsxiTaskResponse, this.datastoreRenameTaskResponse).when(this.taskResponseMap).get(anyString());
        doReturn(this.hostname).when(this.installEsxiTaskResponse).getHostname();
        doReturn(this.datastoreName).when(this.datastoreRenameTaskResponse).getDatastoreName();
        doReturn(this.request).when(this.job).getInputParams();
        doReturn(this.clusterName).when(this.request).getClusterName();
        doReturn(this.scaleIOSVMManagementIpAddress).when(this.request).getScaleIoSvmManagementIpAddress();
        doReturn(this.dataCenterName).when(this.repository).getDataCenterName(anyString());
        doReturn(this.domainName).when(this.repository).getDomainName();
        doReturn(this.host).when(this.repository).getExistingVCenterHost();
        doReturn(this.hostDnsConfig).when(this.host).getHostDnsConfig();
        doReturn(Collections.emptyList()).when(this.hostDnsConfig).getDnsConfigIPs();

        boolean result  = this.handler.executeTask(this.job);

        assertThat(result, is(false));
        verify(this.response).setWorkFlowTaskStatus(Status.FAILED);
        verify(this.response, never()).setNewVMName(anyString());
        verify(this.response).addError(anyString());
    }

    /**
     * {@link com.dell.cpsd.paqx.dne.service.task.handler.addnode.DeployScaleIoVmTaskHandler#executeTask(com.dell.cpsd.paqx.dne.domain.Job)}.
     *
     * @throws Exception
     */
    @Test
    public void executeTask_should_fail_the_workflow_when_there_is_no_esxi_management_ip_address() throws Exception
    {
        String nullEsxiManagementIpAddress = null;

        doReturn(this.response).when(this.handler).initializeResponse(this.job);
        doReturn(this.componentEndpointIds).when(this.repository).getVCenterComponentEndpointIdsByEndpointType(anyString());
        doReturn(this.taskResponseMap).when(this.job).getTaskResponseMap();
        doReturn(this.installEsxiTaskResponse, this.datastoreRenameTaskResponse).when(this.taskResponseMap).get(anyString());
        doReturn(this.hostname).when(this.installEsxiTaskResponse).getHostname();
        doReturn(this.datastoreName).when(this.datastoreRenameTaskResponse).getDatastoreName();
        doReturn(this.request).when(this.job).getInputParams();
        doReturn(this.clusterName).when(this.request).getClusterName();
        doReturn(this.scaleIOSVMManagementIpAddress).when(this.request).getScaleIoSvmManagementIpAddress();
        doReturn(this.dataCenterName).when(this.repository).getDataCenterName(anyString());
        doReturn(this.domainName).when(this.repository).getDomainName();
        doReturn(this.host).when(this.repository).getExistingVCenterHost();
        doReturn(this.hostDnsConfig).when(this.host).getHostDnsConfig();
        doReturn(this.dnsConfigIps).when(this.hostDnsConfig).getDnsConfigIPs();
        doReturn(nullEsxiManagementIpAddress).when(this.request).getEsxiManagementIpAddress();

        boolean result  = this.handler.executeTask(this.job);

        assertThat(result, is(false));
        verify(this.response).setWorkFlowTaskStatus(Status.FAILED);
        verify(this.response, never()).setNewVMName(anyString());
        verify(this.response).addError(anyString());
    }

    /**
     * {@link com.dell.cpsd.paqx.dne.service.task.handler.addnode.DeployScaleIoVmTaskHandler#executeTask(com.dell.cpsd.paqx.dne.domain.Job)}.
     *
     * @throws Exception
     */
    @Test
    public void executeTask_should_fail_the_workflow_when_there_is_no_esxi_management_gateway_ip_address() throws Exception
    {
        String nullEsxiManagementGatewayIpAddress = null;

        doReturn(this.response).when(this.handler).initializeResponse(this.job);
        doReturn(this.componentEndpointIds).when(this.repository).getVCenterComponentEndpointIdsByEndpointType(anyString());
        doReturn(this.taskResponseMap).when(this.job).getTaskResponseMap();
        doReturn(this.installEsxiTaskResponse, this.datastoreRenameTaskResponse).when(this.taskResponseMap).get(anyString());
        doReturn(this.hostname).when(this.installEsxiTaskResponse).getHostname();
        doReturn(this.datastoreName).when(this.datastoreRenameTaskResponse).getDatastoreName();
        doReturn(this.request).when(this.job).getInputParams();
        doReturn(this.clusterName).when(this.request).getClusterName();
        doReturn(this.scaleIOSVMManagementIpAddress).when(this.request).getScaleIoSvmManagementIpAddress();
        doReturn(this.dataCenterName).when(this.repository).getDataCenterName(anyString());
        doReturn(this.domainName).when(this.repository).getDomainName();
        doReturn(this.host).when(this.repository).getExistingVCenterHost();
        doReturn(this.hostDnsConfig).when(this.host).getHostDnsConfig();
        doReturn(this.dnsConfigIps).when(this.hostDnsConfig).getDnsConfigIPs();
        doReturn(this.esxiManagementIpAddress).when(this.request).getEsxiManagementIpAddress();
        doReturn(nullEsxiManagementGatewayIpAddress).when(this.request).getEsxiManagementGatewayIpAddress();

        boolean result  = this.handler.executeTask(this.job);

        assertThat(result, is(false));
        verify(this.response).setWorkFlowTaskStatus(Status.FAILED);
        verify(this.response, never()).setNewVMName(anyString());
        verify(this.response).addError(anyString());
    }

    /**
     * {@link com.dell.cpsd.paqx.dne.service.task.handler.addnode.DeployScaleIoVmTaskHandler#executeTask(com.dell.cpsd.paqx.dne.domain.Job)}.
     *
     * @throws Exception
     */
    @Test
    public void executeTask_should_fail_the_workflow_when_there_is_no_esxi_management_subnet_mask() throws Exception
    {
        String nullEsxiManagementSubnetMask = null;

        doReturn(this.response).when(this.handler).initializeResponse(this.job);
        doReturn(this.componentEndpointIds).when(this.repository).getVCenterComponentEndpointIdsByEndpointType(anyString());
        doReturn(this.taskResponseMap).when(this.job).getTaskResponseMap();
        doReturn(this.installEsxiTaskResponse, this.datastoreRenameTaskResponse).when(this.taskResponseMap).get(anyString());
        doReturn(this.hostname).when(this.installEsxiTaskResponse).getHostname();
        doReturn(this.datastoreName).when(this.datastoreRenameTaskResponse).getDatastoreName();
        doReturn(this.request).when(this.job).getInputParams();
        doReturn(this.clusterName).when(this.request).getClusterName();
        doReturn(this.scaleIOSVMManagementIpAddress).when(this.request).getScaleIoSvmManagementIpAddress();
        doReturn(this.dataCenterName).when(this.repository).getDataCenterName(anyString());
        doReturn(this.domainName).when(this.repository).getDomainName();
        doReturn(this.host).when(this.repository).getExistingVCenterHost();
        doReturn(this.hostDnsConfig).when(this.host).getHostDnsConfig();
        doReturn(this.dnsConfigIps).when(this.hostDnsConfig).getDnsConfigIPs();
        doReturn(this.esxiManagementIpAddress).when(this.request).getEsxiManagementIpAddress();
        doReturn(this.esxiManagementGatewayIpAddress).when(this.request).getEsxiManagementGatewayIpAddress();
        doReturn(nullEsxiManagementSubnetMask).when(this.request).getEsxiManagementSubnetMask();

        boolean result  = this.handler.executeTask(this.job);

        assertThat(result, is(false));
        verify(this.response).setWorkFlowTaskStatus(Status.FAILED);
        verify(this.response, never()).setNewVMName(anyString());
        verify(this.response).addError(anyString());
    }

    /**
     * {@link com.dell.cpsd.paqx.dne.service.task.handler.addnode.DeployScaleIoVmTaskHandler#executeTask(com.dell.cpsd.paqx.dne.domain.Job)}.
     *
     * @throws Exception
     */
    @Test
    public void executeTask_should_fail_the_workflow_when_the_deploy_scaleio_vm_request_fails() throws Exception
    {
        doReturn(this.response).when(this.handler).initializeResponse(this.job);
        doReturn(this.componentEndpointIds).when(this.repository).getVCenterComponentEndpointIdsByEndpointType(anyString());
        doReturn(this.taskResponseMap).when(this.job).getTaskResponseMap();
        doReturn(this.installEsxiTaskResponse, this.datastoreRenameTaskResponse).when(this.taskResponseMap).get(anyString());
        doReturn(this.hostname).when(this.installEsxiTaskResponse).getHostname();
        doReturn(this.datastoreName).when(this.datastoreRenameTaskResponse).getDatastoreName();
        doReturn(this.request).when(this.job).getInputParams();
        doReturn(this.clusterName).when(this.request).getClusterName();
        doReturn(this.scaleIOSVMManagementIpAddress).when(this.request).getScaleIoSvmManagementIpAddress();
        doReturn(this.dataCenterName).when(this.repository).getDataCenterName(anyString());
        doReturn(this.domainName).when(this.repository).getDomainName();
        doReturn(this.host).when(this.repository).getExistingVCenterHost();
        doReturn(this.hostDnsConfig).when(this.host).getHostDnsConfig();
        doReturn(this.dnsConfigIps).when(this.hostDnsConfig).getDnsConfigIPs();
        doReturn(this.esxiManagementIpAddress).when(this.request).getEsxiManagementIpAddress();
        doReturn(this.esxiManagementGatewayIpAddress).when(this.request).getEsxiManagementGatewayIpAddress();
        doReturn(this.esxiManagementSubnetMask).when(this.request).getEsxiManagementSubnetMask();
        doReturn(false).when(this.service).requestDeployScaleIoVm(any());

        boolean result  = this.handler.executeTask(this.job);

        assertThat(result, is(false));
        verify(this.response).setWorkFlowTaskStatus(Status.FAILED);
        verify(this.response, never()).setNewVMName(anyString());
        verify(this.response).addError(anyString());
    }

    /**
     * {@link com.dell.cpsd.paqx.dne.service.task.handler.addnode.DeployScaleIoVmTaskHandler#initializeResponse(com.dell.cpsd.paqx.dne.domain.Job)}.
     *
     * @throws Exception
     */
    @Test
    public void initializeResponse() throws Exception
    {
        doReturn(this.task).when(this.job).getCurrentTask();
        doReturn(this.taskName).when(this.task).getTaskName();
        doReturn(this.stepName).when(this.job).getStep();

        DeployScaleIoVmTaskResponse response = this.handler.initializeResponse(this.job);

        assertNotNull(response);
        assertEquals(this.taskName, response.getWorkFlowTaskName());
        assertEquals(Status.IN_PROGRESS, response.getWorkFlowTaskStatus());
    }

}