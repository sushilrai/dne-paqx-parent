/**
 * <p>
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries. All Rights Reserved. Dell EMC Confidential/Proprietary Information
 * </p>
 */

package com.dell.cpsd.paqx.dne.service.task.handler.preprocess;

import com.dell.cpsd.paqx.dne.domain.Job;
import com.dell.cpsd.paqx.dne.domain.node.NodeInventory;
import com.dell.cpsd.paqx.dne.domain.scaleio.ScaleIOData;
import com.dell.cpsd.paqx.dne.domain.scaleio.ScaleIOProtectionDomain;
import com.dell.cpsd.paqx.dne.domain.scaleio.ScaleIOStoragePool;
import com.dell.cpsd.paqx.dne.repository.InMemoryJobRepository;
import com.dell.cpsd.paqx.dne.service.NodeService;
import com.dell.cpsd.paqx.dne.service.WorkflowService;
import com.dell.cpsd.paqx.dne.service.WorkflowServiceImpl;
import com.dell.cpsd.paqx.dne.service.model.NodeExpansionRequest;
import com.dell.cpsd.paqx.dne.service.model.NodeInfo;
import com.dell.cpsd.paqx.dne.service.model.NodeStatus;
import com.dell.cpsd.paqx.dne.service.model.TaskResponse;
import com.dell.cpsd.paqx.dne.service.workflow.preprocess.PreProcessService;
import com.dell.cpsd.paqx.dne.service.workflow.preprocess.PreProcessTaskConfig;
import com.dell.cpsd.service.common.client.exception.ServiceExecutionException;
import com.dell.cpsd.service.common.client.exception.ServiceTimeoutException;
import com.dell.cpsd.service.engineering.standards.Error;
import com.dell.cpsd.service.engineering.standards.EssValidateStoragePoolResponseMessage;
import com.dell.cpsd.service.engineering.standards.Warning;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Uni test for find scaleio task.
 *
 * <p>
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries.  All Rights Reserved.
 * Dell EMC Confidential/Proprietary Information
 * </p>
 *
 * @since 1.0
 */

@RunWith(MockitoJUnitRunner.class)
public class FindScaleIOTaskHandlerTest {

    @Mock
    private NodeService nodeService = null;



    /*
     * The job running the add node to system definition task handler.
     */
    private Job job         = null;

    private String NODE_INVENTORY_JSON;

    @Before
    public void setUp() throws IOException
    {
        //read json string from file
        BufferedReader reader = new BufferedReader(new FileReader("src/test/resources/node_inventory.json"));
        String         line = null;
        StringBuilder  stringBuilder = new StringBuilder();

        try {
            while((line = reader.readLine()) != null) {
                stringBuilder.append(line);
            }

            NODE_INVENTORY_JSON = stringBuilder.toString();
        } finally {
            reader.close();
        }


        PreProcessTaskConfig preprocessConfig = new PreProcessTaskConfig();
        WorkflowService workflowService = new WorkflowServiceImpl(new InMemoryJobRepository(), preprocessConfig.preProcessWorkflowSteps());

        PreProcessService preprocessService = new PreProcessService();
        preprocessService.setWorkflowService(workflowService);

        this.job = preprocessService.createWorkflow("preProcessWorkflow", "startPreProcessWorkflow", "submitted");

        NodeExpansionRequest nodeExpansionRequest = new NodeExpansionRequest("idracIpAddress", "idracGatewayIpAddress", "idracSubnetMask",
                "managementIpAddress", "esxiKernelIpAddress1", "esxiKernelIpAddress2", "esxiManagementHostname", "scaleIoData1SvmIpAddress",
                "scaleIoData1SvmGatewayAddress",
                "scaleIoData1KernelIpAddress", "scaleIoData1KernelAndSvmSubnetMask", "scaleIOSVMDataIpAddress2", "scaleIoData2KernelIpAddress",
                "scaleIoData2KernelAndSvmSubnetMask", "scaleIOSVMManagementIpAddress", "scaleIoSvmManagementSubnetMask", "symphonyUuid", "clausterName",
                "vMotionManagementIpAddress", "vMotionManagementSubnetMask");
        this.job.setInputParams(nodeExpansionRequest);

        TaskResponse response = new TaskResponse();
        NodeInfo nodeInfo = new NodeInfo("symphonyUuid", NodeStatus.DISCOVERED);
        Map<String, String> results = new HashMap<>();

        results.put("symphonyUUID", nodeInfo.getSymphonyUuid());
        results.put("nodeStatus", nodeInfo.getNodeStatus().toString());

        response.setResults(results);

        this.job.addTaskResponse("findAvailableNodes", response);

        this.job.changeToNextStep("findScaleIO");
    }

    @Test
    public void testExecuteTask_successful_case() throws ServiceTimeoutException, ServiceExecutionException
    {
        FindScaleIOTaskHandler handler = new FindScaleIOTaskHandler(this.nodeService);
        NodeInventory nodeInventory = new NodeInventory();
        nodeInventory.setSymphonyUUID("symphonyUUID");
        nodeInventory.setNodeInventory(NODE_INVENTORY_JSON);

        EssValidateStoragePoolResponseMessage storageResponseMessage = new EssValidateStoragePoolResponseMessage();
        Map<String, String> deviceToPoolMap = new HashMap<>();
        deviceToPoolMap.put("device1","pool1");
        storageResponseMessage.setDeviceToStoragePoolMap(deviceToPoolMap);
        storageResponseMessage.setWarnings(Arrays.asList(new Warning("1","No message")));

        ScaleIOData scaleIOData =  new ScaleIOData();
        ScaleIOStoragePool scaleIOStoragePool = new ScaleIOStoragePool();
        ScaleIOProtectionDomain scaleIOProtectionDomain = new ScaleIOProtectionDomain();
        scaleIOStoragePool.setId("1");
        scaleIOStoragePool.setName("Sp-1");
        scaleIOProtectionDomain.addStoragePool(scaleIOStoragePool);
        scaleIOData.setProtectionDomains(Arrays.asList(scaleIOProtectionDomain));

        when(this.nodeService.getNodeInventoryData(job)).thenReturn(NODE_INVENTORY_JSON);
        when(this.nodeService.listScaleIOData()).thenReturn(Arrays.asList(scaleIOData));
        when(this.nodeService.validateStoragePools(anyList(), anyList(), anyMap())).thenReturn(storageResponseMessage);

        boolean expectedResult = true;
        boolean actualResult = handler.executeTask(job);

        assertEquals(expectedResult,actualResult);
        assertEquals("SUCCEEDED", job.getTaskResponseMap().get(job.getStep()).getWorkFlowTaskStatus().toString());
    }

    @Test
    public void testExecuteTask_error_case() throws ServiceTimeoutException, ServiceExecutionException
    {
        FindScaleIOTaskHandler handler = new FindScaleIOTaskHandler(this.nodeService);
        NodeInventory nodeInventory = new NodeInventory();
        nodeInventory.setSymphonyUUID("symphonyUUID");
        nodeInventory.setNodeInventory(NODE_INVENTORY_JSON);

        EssValidateStoragePoolResponseMessage storageResponseMessage = new EssValidateStoragePoolResponseMessage();
        storageResponseMessage.setErrors(Arrays.asList(new Error("TypeError","No storage pool found containing all SSDs.")));

        ScaleIOData scaleIOData =  new ScaleIOData();
        ScaleIOStoragePool scaleIOStoragePool = new ScaleIOStoragePool();
        ScaleIOProtectionDomain scaleIOProtectionDomain = new ScaleIOProtectionDomain();
        scaleIOStoragePool.setId("1");
        scaleIOStoragePool.setName("Sp-1");
        scaleIOProtectionDomain.addStoragePool(scaleIOStoragePool);
        scaleIOData.setProtectionDomains(Arrays.asList(scaleIOProtectionDomain));

        when(this.nodeService.getNodeInventoryData(job)).thenReturn(NODE_INVENTORY_JSON);
        when(this.nodeService.listScaleIOData()).thenReturn(Arrays.asList(scaleIOData));
        when(this.nodeService.validateStoragePools(anyList(), anyList(), anyMap())).thenReturn(storageResponseMessage);

        boolean expectedResult = false;
        boolean actualResult = handler.executeTask(job);

        assertEquals(expectedResult,actualResult);
        assertEquals("FAILED", job.getTaskResponseMap().get(job.getStep()).getWorkFlowTaskStatus().toString());
    }
}
