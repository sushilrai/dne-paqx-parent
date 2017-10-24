/**
 * <p>
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries. All Rights Reserved. Dell EMC Confidential/Proprietary Information
 * </p>
 */

package com.dell.cpsd.paqx.dne.service.task.handler.preprocess;

import com.dell.cpsd.paqx.dne.domain.Job;
import com.dell.cpsd.paqx.dne.service.NodeService;
import com.dell.cpsd.paqx.dne.service.model.NodeExpansionRequest;
import com.dell.cpsd.paqx.dne.service.model.Status;
import com.dell.cpsd.paqx.dne.service.model.TaskResponse;
import com.dell.cpsd.service.common.client.exception.ServiceExecutionException;
import com.dell.cpsd.service.common.client.exception.ServiceTimeoutException;
import com.dell.cpsd.virtualization.capabilities.api.ClusterInfo;
import com.dell.cpsd.virtualization.capabilities.api.ValidateVcenterClusterResponseMessage;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class FindVClusterTaskHandlerTest
{
    @Mock
    private NodeService nodeService;

    @Mock
    private Job job;

    @Mock
    private NodeExpansionRequest nodeExpansionRequest;

    @Mock
    private TaskResponse taskResponse;

    @Mock
    private ClusterInfo clusterInfo;

    @Mock
    private ValidateVcenterClusterResponseMessage validateVcenterClusterResponseMessage;

    private FindVClusterTaskHandler handler;

    @Before
    public void setUp()
    {
        this.handler = spy(new FindVClusterTaskHandler(this.nodeService));
    }

    @Test
    public void testExecuteTask_should_successfully_find_vcluster_data() throws ServiceTimeoutException, ServiceExecutionException
    {
        doReturn(this.taskResponse).when(this.handler).initializeResponse(this.job);
        doReturn(Arrays.asList(this.clusterInfo)).when(this.nodeService).listClusters();
        doReturn(this.validateVcenterClusterResponseMessage).when(this.nodeService).validateClusters(any());
        doReturn(Arrays.asList("cluster-1")).when(this.validateVcenterClusterResponseMessage).getClusters();

        final boolean result = this.handler.executeTask(this.job);

        assertTrue(result);
        verify(this.taskResponse).setWorkFlowTaskStatus(Status.SUCCEEDED);
        verify(this.taskResponse).setResults(any());
        verify(this.taskResponse, never()).addError(anyString());
    }

    @Test
    public void testExecuteTask_should_fail_the_work_flow_if_there_was_no_vclusters()
            throws ServiceTimeoutException, ServiceExecutionException
    {
        doReturn(this.taskResponse).when(this.handler).initializeResponse(this.job);
        doReturn(Arrays.asList(this.clusterInfo)).when(this.nodeService).listClusters();
        doReturn(this.validateVcenterClusterResponseMessage).when(this.nodeService).validateClusters(any());
        doReturn(Collections.emptyList()).when(this.validateVcenterClusterResponseMessage).getClusters();
        doReturn(Arrays.asList("No clusters found")).when(this.validateVcenterClusterResponseMessage).getFailedCluster();

        final boolean result = this.handler.executeTask(this.job);

        assertFalse(result);
        verify(this.taskResponse).setWorkFlowTaskStatus(Status.FAILED);
        verify(this.taskResponse, never()).setResults(any());
        verify(this.taskResponse).addError(anyString());
    }

    @Test
    public void testExecuteTask_should_fail_the_work_flow_if_there_is_an_exception()
            throws ServiceTimeoutException, ServiceExecutionException
    {
        doReturn(this.taskResponse).when(this.handler).initializeResponse(this.job);
        doThrow(new IllegalStateException("some-error")).when(this.nodeService).listClusters();

        final boolean result = this.handler.executeTask(this.job);

        assertFalse(result);
        verify(this.taskResponse).setWorkFlowTaskStatus(Status.FAILED);
        verify(this.taskResponse, never()).setResults(any());
        verify(this.taskResponse).addError(anyString());
    }
}
