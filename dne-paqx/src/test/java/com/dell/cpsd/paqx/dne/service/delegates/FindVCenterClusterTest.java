/**
 * <p>
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries. All Rights Reserved. Dell EMC Confidential/Proprietary Information
 * </p>
 */

package com.dell.cpsd.paqx.dne.service.delegates;

import com.dell.cpsd.paqx.dne.service.NodeService;
import com.dell.cpsd.paqx.dne.service.delegates.model.NodeDetail;
import com.dell.cpsd.service.common.client.exception.ServiceTimeoutException;
import com.dell.cpsd.virtualization.capabilities.api.MessageProperties;
import com.dell.cpsd.virtualization.capabilities.api.ValidateVcenterClusterResponseMessage;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.dell.cpsd.paqx.dne.service.delegates.utils.DelegateConstants.FIND_VCLUSTER_FAILED;
import static com.dell.cpsd.paqx.dne.service.delegates.utils.DelegateConstants.NODE_DETAILS;
import static junit.framework.TestCase.fail;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;

@RunWith(MockitoJUnitRunner.class)
public class FindVCenterClusterTest
{

    private FindVCenterCluster findVCenterCluster;
    private List<NodeDetail> nodeDetail;
    private ValidateVcenterClusterResponseMessage responseMessage;
    private Map<String, String> clusterMap;
    @Mock
    private NodeService nodeService;

    @Mock
    private DelegateExecution delegateExecution;

    @Mock
    private MessageProperties messageProperties;

    @Before
    public void setUp() throws Exception {
        findVCenterCluster = new FindVCenterCluster(nodeService);
        ValidateVcenterClusterResponseMessage responseMsg = new ValidateVcenterClusterResponseMessage();

        nodeDetail = new ArrayList<>();
        nodeDetail.add(new NodeDetail("1", "abc"));

        doReturn(nodeDetail). when(delegateExecution).getVariable(NODE_DETAILS);
        clusterMap = new HashMap<>();
        clusterMap.put("abc", "TestCluster");
        responseMsg.setClusters(clusterMap);
        responseMessage = new ValidateVcenterClusterResponseMessage(messageProperties, clusterMap, null, "Description" );
        doReturn(responseMessage).when(nodeService).validateClusters(any(), any());
    }

    @Test
    public void testSuccessful() {
        findVCenterCluster.delegateExecute(delegateExecution);
        assertEquals(nodeDetail.get(0).getClusterName(), "TestCluster");
    }

    @Test
    public void testRequestClusterException() throws Exception {
        try
        {
            doThrow(new ServiceTimeoutException("Timeout")).when(nodeService).listClusters();
            findVCenterCluster.delegateExecute(delegateExecution);
            fail("Should not get here!");
        } catch( BpmnError bpmnError) {
            assertEquals(bpmnError.getErrorCode(), FIND_VCLUSTER_FAILED);
            assertEquals(bpmnError.getMessage(), "An unexpected Exception occurred while retrieving the list of Clusters for selection.  Reason: Timeout");
        }
    }
}
