/**
 * <p>
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries. All Rights Reserved. Dell EMC Confidential/Proprietary Information
 * </p>
 */

package com.dell.cpsd.paqx.dne.service.task.handler.addnode;

import com.dell.converged.capabilities.compute.discovered.nodes.api.EsxiInstallationInfo;
import com.dell.cpsd.paqx.dne.domain.Job;
import com.dell.cpsd.paqx.dne.domain.WorkflowTask;
import com.dell.cpsd.paqx.dne.service.NodeService;
import com.dell.cpsd.paqx.dne.service.model.NodeExpansionRequest;
import com.dell.cpsd.paqx.dne.transformers.HostToInstallEsxiRequestTransformer;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import static org.junit.Assert.*;

/**
 * The tests for the InstallEsxiTaskHandler class.
 * <p>
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries. All Rights Reserved. Dell EMC Confidential/Proprietary Information
 * </p>
 *
 * @since 1.0
 */
@RunWith(MockitoJUnitRunner.class)
public class InstallEsxiTaskHandlerTest
{
    @Mock
    private WorkflowTask task;

    @Mock
    private Job job;

    @Mock
    private NodeExpansionRequest request;

    @Mock
    private NodeService service;

    @Mock
    private HostToInstallEsxiRequestTransformer transformer;

    @Mock
    private EsxiInstallationInfo esxiInstallInfo;

    private String taskName = "installEsxiTask";
    private String stepName = "installEsxiStep";
    private String nodeId   = "nodeId";
    private String hostname = "hostname";

    /**
     * {@link com.dell.cpsd.paqx.dne.service.task.handler.addnode.InstallEsxiTaskHandler#executeTask(com.dell.cpsd.paqx.dne.domain.Job)}.
     */
    @Test
    public void testExecuteTask_successful_case()
    {
        boolean success = true;

        doReturn(this.task).when(this.job).getCurrentTask();
        doReturn(this.taskName).when(this.task).getTaskName();
        doReturn(this.stepName).when(this.job).getStep();
        doReturn(this.request).when(this.job).getInputParams();
        doReturn(this.nodeId).when(this.request).getNodeId();
        doReturn(this.hostname).when(this.request).getHostname();
        doReturn(this.esxiInstallInfo).when(this.transformer).transformInstallEsxiData(anyString(), anyString());
        doReturn(success).when(this.service).requestInstallEsxi(any());

        InstallEsxiTaskHandler handler = new InstallEsxiTaskHandler(this.service, this.transformer);
        assertEquals(true, handler.executeTask(this.job));
        verify(this.transformer).transformInstallEsxiData(anyString(), anyString());
        verify(this.service).requestInstallEsxi(any());
    }

    /**
     * {@link com.dell.cpsd.paqx.dne.service.task.handler.addnode.InstallEsxiTaskHandler#executeTask(com.dell.cpsd.paqx.dne.domain.Job)}.
     */
    @Test
    public void testExecuteTask_node_expansion_request_is_null()
    {
        NodeExpansionRequest nullRequest = null;

        doReturn(this.task).when(this.job).getCurrentTask();
        doReturn(this.taskName).when(this.task).getTaskName();
        doReturn(this.stepName).when(this.job).getStep();
        doReturn(nullRequest).when(this.job).getInputParams();

        InstallEsxiTaskHandler handler = new InstallEsxiTaskHandler(this.service, this.transformer);
        assertEquals(false, handler.executeTask(this.job));
        verify(this.transformer, times(0)).transformInstallEsxiData(anyString(), anyString());
        verify(this.service, times(0)).requestInstallEsxi(any());
    }

    /**
     * {@link com.dell.cpsd.paqx.dne.service.task.handler.addnode.InstallEsxiTaskHandler#executeTask(com.dell.cpsd.paqx.dne.domain.Job)}.
     */
    @Test
    public void testExecuteTask_nodeId_is_null()
    {
        String nullINodeId = null;

        doReturn(this.task).when(this.job).getCurrentTask();
        doReturn(this.taskName).when(this.task).getTaskName();
        doReturn(this.stepName).when(this.job).getStep();
        doReturn(this.request).when(this.job).getInputParams();
        doReturn(nullINodeId).when(this.request).getNodeId();

        InstallEsxiTaskHandler handler = new InstallEsxiTaskHandler(this.service, this.transformer);
        assertEquals(false, handler.executeTask(this.job));
        verify(this.transformer, times(0)).transformInstallEsxiData(anyString(), anyString());
        verify(this.service, times(0)).requestInstallEsxi(any());
    }

    /**
     * {@link com.dell.cpsd.paqx.dne.service.task.handler.addnode.InstallEsxiTaskHandler#executeTask(com.dell.cpsd.paqx.dne.domain.Job)}.
     */
    @Test
    public void testExecuteTask_hostname_is_null()
    {
        String nullHostname = null;

        doReturn(this.task).when(this.job).getCurrentTask();
        doReturn(this.taskName).when(this.task).getTaskName();
        doReturn(this.stepName).when(this.job).getStep();
        doReturn(this.request).when(this.job).getInputParams();
        doReturn(this.nodeId).when(this.request).getNodeId();
        doReturn(nullHostname).when(this.request).getHostname();

        InstallEsxiTaskHandler handler = new InstallEsxiTaskHandler(this.service, this.transformer);
        assertEquals(false, handler.executeTask(this.job));
        verify(this.transformer, times(0)).transformInstallEsxiData(anyString(), anyString());
        verify(this.service, times(0)).requestInstallEsxi(any());
    }
}