/**
 * <p>
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries. All Rights Reserved.
 * Dell EMC Confidential/Proprietary Information
 * </p>
 */

package com.dell.cpsd.paqx.dne.service.task.handler.addnode;

import com.dell.cpsd.paqx.dne.domain.Job;
import com.dell.cpsd.paqx.dne.domain.WorkflowTask;
import com.dell.cpsd.paqx.dne.domain.scaleio.ScaleIOData;
import com.dell.cpsd.paqx.dne.domain.scaleio.ScaleIOIP;
import com.dell.cpsd.paqx.dne.domain.scaleio.ScaleIOMdmCluster;
import com.dell.cpsd.paqx.dne.domain.scaleio.ScaleIOSDSElementInfo;
import com.dell.cpsd.paqx.dne.repository.DataServiceRepository;
import com.dell.cpsd.paqx.dne.service.NodeService;
import com.dell.cpsd.paqx.dne.service.model.ComponentEndpointIds;
import com.dell.cpsd.paqx.dne.service.model.ConfigureScaleIoVibTaskResponse;
import com.dell.cpsd.paqx.dne.service.model.InstallEsxiTaskResponse;
import com.dell.cpsd.paqx.dne.service.model.ListESXiCredentialDetailsTaskResponse;
import com.dell.cpsd.paqx.dne.service.model.Status;
import com.dell.cpsd.paqx.dne.service.model.TaskResponse;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

/**
 * Configure Software VIB Task Handler Test
 * <p>
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries. All Rights Reserved.
 * Dell EMC Confidential/Proprietary Information
 * </p>
 *
 * @version 1.0
 * @since 1.0
 */
@RunWith(MockitoJUnitRunner.class)
public class ConfigureSoftwareVibTaskHandlerTest
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
    private ConfigureScaleIoVibTaskResponse response;

    @Mock
    private InstallEsxiTaskResponse installEsxiTaskResponse;

    @Mock
    private ComponentEndpointIds componentEndpointIds;

    @Mock
    private ListESXiCredentialDetailsTaskResponse listESXiCredentialDetailsTaskResponse;

    @Mock
    private ScaleIOData scaleIOData;

    @Mock
    private ScaleIOMdmCluster scaleIOMdmCluster;

    @Mock
    private ScaleIOIP masterScaleIOIP;

    @Mock
    private ScaleIOIP slaveScaleIOIP;

    @Mock
    private ScaleIOSDSElementInfo masterScaleIOSDSElementInfo;

    @Mock
    private ScaleIOSDSElementInfo slaveScaleIOSDSElementInfo;

    @Mock
    private Map<String, TaskResponse> taskResponseMap;

    private String hostname = "hostname_1.2.3.4";

    private ConfigureScaleIoVibTaskHandler handler;

    @Before
    public void setUp() throws Exception
    {
        this.handler = spy(new ConfigureScaleIoVibTaskHandler(this.service, this.repository));
    }

    @Test
    public void executeTask_successful_case() throws Exception
    {
        doReturn(this.response).when(this.handler).initializeResponse(this.job);
        doReturn(this.componentEndpointIds).when(this.repository).getVCenterComponentEndpointIdsByEndpointType(anyString());
        doReturn(this.taskResponseMap).when(this.job).getTaskResponseMap();
        doReturn(this.installEsxiTaskResponse).when(this.taskResponseMap).get("installEsxi");
        doReturn(this.hostname).when(this.installEsxiTaskResponse).getHostname();
        doReturn(this.listESXiCredentialDetailsTaskResponse).when(this.taskResponseMap).get("retrieveEsxiDefaultCredentialDetails");
        doReturn(this.scaleIOData).when(this.repository).getScaleIoData();
        doReturn(this.scaleIOMdmCluster).when(this.scaleIOData).getMdmCluster();
        doReturn(Arrays.asList(this.masterScaleIOSDSElementInfo)).when(this.scaleIOMdmCluster).getMasterElementInfo();
        doReturn(Arrays.asList(this.masterScaleIOIP)).when(this.masterScaleIOSDSElementInfo).getIps();
        doReturn(this.masterScaleIOSDSElementInfo).when(this.masterScaleIOIP).getSdsElementInfo();
        doReturn("master").when(this.masterScaleIOSDSElementInfo).getRole();
        doReturn(Arrays.asList(this.slaveScaleIOSDSElementInfo)).when(this.scaleIOMdmCluster).getSlaveElementInfo();
        doReturn(Arrays.asList(this.slaveScaleIOIP)).when(this.slaveScaleIOSDSElementInfo).getIps();
        doReturn(this.slaveScaleIOSDSElementInfo).when(this.slaveScaleIOIP).getSdsElementInfo();
        doReturn("slave").when(this.slaveScaleIOSDSElementInfo).getRole();
        doReturn(true).when(this.service).requestConfigureScaleIoVib(any());

        boolean result = this.handler.executeTask(this.job);

        assertThat(result, is(true));
        verify(this.response).setWorkFlowTaskStatus(Status.SUCCEEDED);
        verify(this.response, never()).addError(anyString());
        verify(this.response).setIoctlIniGuidStr(anyString());
    }

    @Test
    public void executeTask_no_vcenter_components() throws Exception
    {
        final ComponentEndpointIds nullComponentEndpointIds = null;

        doReturn(this.response).when(this.handler).initializeResponse(this.job);
        doReturn(nullComponentEndpointIds).when(this.repository).getVCenterComponentEndpointIdsByEndpointType(anyString());

        boolean result = this.handler.executeTask(this.job);

        assertThat(result, is(false));
        verify(this.response).setWorkFlowTaskStatus(Status.FAILED);
        verify(this.response).addError(anyString());
    }

    @Test
    public void executeTask_no_esxi_task_response() throws Exception
    {
        final InstallEsxiTaskResponse nullInstallEsxiTaskResponse = null;

        doReturn(this.response).when(this.handler).initializeResponse(this.job);
        doReturn(this.componentEndpointIds).when(this.repository).getVCenterComponentEndpointIdsByEndpointType(anyString());
        doReturn(this.taskResponseMap).when(this.job).getTaskResponseMap();
        doReturn(nullInstallEsxiTaskResponse).when(this.taskResponseMap).get(anyString());

        boolean result = this.handler.executeTask(this.job);

        assertThat(result, is(false));
        verify(this.response).setWorkFlowTaskStatus(Status.FAILED);
        verify(this.response).addError(anyString());
    }

    @Test
    public void executeTask_no_hostname() throws Exception
    {
        final String nullHostname = null;

        doReturn(this.response).when(this.handler).initializeResponse(this.job);
        doReturn(this.componentEndpointIds).when(this.repository).getVCenterComponentEndpointIdsByEndpointType(anyString());
        doReturn(this.taskResponseMap).when(this.job).getTaskResponseMap();
        doReturn(this.installEsxiTaskResponse).when(this.taskResponseMap).get(anyString());
        doReturn(nullHostname).when(this.installEsxiTaskResponse).getHostname();

        boolean result = this.handler.executeTask(this.job);

        assertThat(result, is(false));
        verify(this.response).setWorkFlowTaskStatus(Status.FAILED);
        verify(this.response).addError(anyString());
    }

    @Test
    public void executeTask_no_esxi_credentials_task_response() throws Exception
    {
        final ListESXiCredentialDetailsTaskResponse nullListESXiCredentialDetailsTaskResponse = null;

        doReturn(this.response).when(this.handler).initializeResponse(this.job);
        doReturn(this.componentEndpointIds).when(this.repository).getVCenterComponentEndpointIdsByEndpointType(anyString());
        doReturn(this.taskResponseMap).when(this.job).getTaskResponseMap();
        doReturn(this.installEsxiTaskResponse).when(this.taskResponseMap).get(anyString());
        doReturn(this.hostname).when(this.installEsxiTaskResponse).getHostname();
        doReturn(nullListESXiCredentialDetailsTaskResponse).when(this.taskResponseMap).get("retrieveEsxiDefaultCredentialDetails");

        boolean result = this.handler.executeTask(this.job);

        assertThat(result, is(false));
        verify(this.response).setWorkFlowTaskStatus(Status.FAILED);
        verify(this.response).addError(anyString());
    }

    @Test
    public void executeTask_no_scaleio_data() throws Exception
    {
        final ScaleIOData nullScaleIOData = null;

        doReturn(this.response).when(this.handler).initializeResponse(this.job);
        doReturn(this.componentEndpointIds).when(this.repository).getVCenterComponentEndpointIdsByEndpointType(anyString());
        doReturn(this.taskResponseMap).when(this.job).getTaskResponseMap();
        doReturn(this.installEsxiTaskResponse).when(this.taskResponseMap).get("installEsxi");
        doReturn(this.hostname).when(this.installEsxiTaskResponse).getHostname();
        doReturn(this.listESXiCredentialDetailsTaskResponse).when(this.taskResponseMap).get("retrieveEsxiDefaultCredentialDetails");
        doReturn(nullScaleIOData).when(this.repository).getScaleIoData();

        boolean result = this.handler.executeTask(this.job);

        assertThat(result, is(false));
        verify(this.response).setWorkFlowTaskStatus(Status.FAILED);
        verify(this.response).addError(anyString());
    }

    @Test
    public void executeTask_failed_configureSoftwareVib_request() throws Exception
    {
        doReturn(this.response).when(this.handler).initializeResponse(this.job);
        doReturn(this.componentEndpointIds).when(this.repository).getVCenterComponentEndpointIdsByEndpointType(anyString());
        doReturn(this.taskResponseMap).when(this.job).getTaskResponseMap();
        doReturn(this.installEsxiTaskResponse).when(this.taskResponseMap).get("installEsxi");
        doReturn(this.hostname).when(this.installEsxiTaskResponse).getHostname();
        doReturn(this.listESXiCredentialDetailsTaskResponse).when(this.taskResponseMap).get("retrieveEsxiDefaultCredentialDetails");
        doReturn(this.scaleIOData).when(this.repository).getScaleIoData();
        doReturn(false).when(this.service).requestConfigureScaleIoVib(any());

        boolean result = this.handler.executeTask(this.job);

        assertThat(result, is(false));
        verify(this.response).setWorkFlowTaskStatus(Status.FAILED);
        verify(this.response).addError(anyString());
        verify(this.response, never()).setIoctlIniGuidStr(anyString());
    }

    @Test
    public void initializeResponse() throws Exception
    {
        doReturn(this.task).when(this.job).getCurrentTask();
        String taskName = "configureSoftwareVibTask";
        doReturn(taskName).when(this.task).getTaskName();
        String stepName = "configureSoftwareVibStep";
        doReturn(stepName).when(this.job).getStep();

        final ConfigureScaleIoVibTaskResponse response = this.handler.initializeResponse(this.job);

        assertNotNull(response);
        assertEquals(taskName, response.getWorkFlowTaskName());
        assertEquals(Status.IN_PROGRESS, response.getWorkFlowTaskStatus());
    }
}
