package com.dell.cpsd.paqx.dne.service.delegates;

import com.dell.cpsd.paqx.dne.domain.scaleio.ScaleIOStoragePool;
import com.dell.cpsd.paqx.dne.exception.TaskResponseFailureException;
import com.dell.cpsd.paqx.dne.service.NodeService;
import com.dell.cpsd.paqx.dne.service.delegates.model.NodeDetail;
import com.dell.cpsd.paqx.dne.service.model.ComponentEndpointIds;
import com.dell.cpsd.service.engineering.standards.DeviceAssignment;
import com.dell.cpsd.storage.capabilities.api.CreateStoragePoolRequestMessage;
import com.dell.cpsd.storage.capabilities.api.StoragePoolSpec;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import static com.dell.cpsd.paqx.dne.service.delegates.utils.DelegateConstants.NODE_DETAILS;

@RunWith(MockitoJUnitRunner.class)
public class ConfigureStoragePoolsTest
{
    private ConfigureStoragePools configureStoragePools;

    @Mock
    private NodeService nodeService;

    @Mock
    private DelegateExecution delegateExecution;

    private List<NodeDetail> nodeDetails = new ArrayList<>();

    @Before
    public void setUp() throws Exception
    {
        configureStoragePools = Mockito.spy(new ConfigureStoragePools(nodeService));

        NodeDetail nodeDetail1 = new NodeDetail();
        nodeDetail1.setProtectionDomainId("PD-1");
        Map<String, DeviceAssignment> deviceMap1 = new HashMap<>();
        deviceMap1.put("500003976c89b061", new DeviceAssignment("500003976c89b061", "y6j0a07htg3d", "/dev/disk/by-id/wwn-0x500003976c89b061", "/dev/bus/0 megaraid,5", "0e392a4100000001", "temp-1"));
        deviceMap1.put("5000039785987735", new DeviceAssignment("5000039785987735", "7610b01pvg3d", "/dev/disk/by-id/wwn-0x5000039785987735", "/dev/bus/0 megaraid,6", "0e392a4100000001", "temp-1"));
        deviceMap1.put("50000397859876b9", new DeviceAssignment("50000397859876b9", "7610b00svg3d", "/dev/disk/by-id/wwn-0x50000397859876b9", "/dev/bus/0 megaraid,0", "0e392a4100000001", "temp-1"));
        deviceMap1.put("500003976c89f94d", new DeviceAssignment("500003976c89f94d", "y6k0a017tg3d", "/dev/disk/by-id/wwn-0x500003976c89f94d", "/dev/bus/0 megaraid,2", "0e392a4100000001", "temp-1"));
        nodeDetail1.setDeviceToDeviceStoragePool(deviceMap1);

        NodeDetail nodeDetail2 = new NodeDetail();
        nodeDetail2.setProtectionDomainId("PD-2");
        Map<String, DeviceAssignment> deviceMap2 = new HashMap<>();
        deviceMap2.put("500003976c89b062", new DeviceAssignment("500003976c89b062", "y6j0a07htg3e", "/dev/disk/by-id/wwn-0x500003976c89b062", "/dev/bus/0 megaraid,6", "0e392a4100000002", "temp-2"));
        deviceMap2.put("5000039785987733", new DeviceAssignment("5000039785987733", "7610b01pvg3e", "/dev/disk/by-id/wwn-0x5000039785987736", "/dev/bus/0 megaraid,7", "0e392a4100000002", "temp-2"));
        deviceMap2.put("50000397859876b0", new DeviceAssignment("50000397859876b0", "7610b00svg3e", "/dev/disk/by-id/wwn-0x50000397859876b0", "/dev/bus/0 megaraid,1", "0e392a4100000002", "temp-2"));
        deviceMap2.put("500003976c89f94e", new DeviceAssignment("500003976c89f94e", "y6k0a017tg3e", "/dev/disk/by-id/wwn-0x500003976c89f94e", "/dev/bus/0 megaraid,3", "0e392a4100000002", "temp-2"));
        nodeDetail2.setDeviceToDeviceStoragePool(deviceMap2);
        this.nodeDetails.add(nodeDetail1);
        this.nodeDetails.add(nodeDetail2);
    }

    @Test
    public void createValidStoragePool_success() throws TaskResponseFailureException {
        Set<String> storagePoolNames = new HashSet<>();
        storagePoolNames.add("SP-1");
        storagePoolNames.add("SP-2");

        ComponentEndpointIds componentEndpointIds = new ComponentEndpointIds("c_uuid", "e_uuid", "e_url", "c_uuid");

        CreateStoragePoolRequestMessage requestMessage = new CreateStoragePoolRequestMessage();
        requestMessage.setEndpointUrl("https://" + componentEndpointIds.getEndpointUrl());
        requestMessage.setComponentEndpointIds(
                new com.dell.cpsd.storage.capabilities.api.ComponentEndpointIds(componentEndpointIds.getComponentUuid(),
                        componentEndpointIds.getEndpointUuid(), componentEndpointIds.getCredentialUuid()));

        StoragePoolSpec storagePoolSpec = new StoragePoolSpec();
        storagePoolSpec.setProtectionDomainId("PD-1");
        storagePoolSpec.setRmCacheWriteHandlingMode(StoragePoolSpec.RmCacheWriteHandlingMode.PASSTHROUGH);
        storagePoolSpec.setStoragePoolName("SP-1");
        storagePoolSpec.setUseRmcache(false);
        storagePoolSpec.setZeroPaddingEnabled(true);
        requestMessage.setStoragePoolSpec(storagePoolSpec);

        CreateStoragePoolRequestMessage requestMessage1 = new CreateStoragePoolRequestMessage();
        requestMessage1.setEndpointUrl("https://" + componentEndpointIds.getEndpointUrl());
        requestMessage1.setComponentEndpointIds(
                new com.dell.cpsd.storage.capabilities.api.ComponentEndpointIds(componentEndpointIds.getComponentUuid(),
                        componentEndpointIds.getEndpointUuid(), componentEndpointIds.getCredentialUuid()));

        StoragePoolSpec storagePoolSpec1 = new StoragePoolSpec();
        storagePoolSpec1.setProtectionDomainId("PD-1");
        storagePoolSpec1.setRmCacheWriteHandlingMode(StoragePoolSpec.RmCacheWriteHandlingMode.PASSTHROUGH);
        storagePoolSpec1.setStoragePoolName("SP-2");
        storagePoolSpec1.setUseRmcache(false);
        storagePoolSpec1.setZeroPaddingEnabled(true);
        requestMessage1.setStoragePoolSpec(storagePoolSpec1);



        doReturn(componentEndpointIds).when(nodeService).getComponentEndpointIds("SCALEIO-CLUSTER");
        doReturn("SP-ID-1").when(nodeService).createStoragePool(requestMessage);
        doReturn("SP-ID-2").when(nodeService).createStoragePool(requestMessage1);
        doReturn(new ScaleIOStoragePool()).when(nodeService).createStoragePool("SP-1", "SP-ID-1", "PD-1");
        doReturn(new ScaleIOStoragePool()).when(nodeService).createStoragePool("SP-2", "SP-ID-2", "PD-1");

        Map<String, String> storagePoolNameToId = configureStoragePools.createValidStoragePool("PD-1", storagePoolNames);
        assertNotNull(storagePoolNameToId);
        assertEquals(2, storagePoolNameToId.size());
        assertEquals("SP-ID-1", storagePoolNameToId.get("SP-1"));
        assertEquals("SP-ID-2", storagePoolNameToId.get("SP-2"));
    }

    @Test
    public void createValidStoragePool_noComponentId() throws TaskResponseFailureException {
        doReturn(null).when(nodeService).getComponentEndpointIds("SCALEIO-CLUSTER");

        try {
            Map<String, String> storagePoolNameToId = configureStoragePools.createValidStoragePool("PD-1", new HashSet<>());
            fail("Expecting exception, received none.");
        } catch (IllegalStateException e) {
            assertEquals("No component ids found.", e.getMessage());
        } catch (Exception e) {
            fail("Unexpected exception thrown.");
        }
    }

    @Test
    public void createValidStoragePool_taskFailure() throws TaskResponseFailureException {
        Set<String> storagePoolNames = new HashSet<>();
        storagePoolNames.add("SP-1");
        storagePoolNames.add("SP-2");

        ComponentEndpointIds componentEndpointIds = new ComponentEndpointIds("c_uuid", "e_uuid", "e_url", "c_uuid");
        doReturn(componentEndpointIds).when(nodeService).getComponentEndpointIds("SCALEIO-CLUSTER");
        doThrow(TaskResponseFailureException.class).when(nodeService).createStoragePool(any());

        try {
            Map<String, String> storagePoolNameToId = configureStoragePools.createValidStoragePool("PD-1", storagePoolNames);
            fail("Expecting exception, got none.");
        } catch (IllegalStateException e) {
            assertEquals("Create storage pool request failed", e.getMessage());
        }
    }

    @Test
    public void delegateExecute_poolAvailable() throws Exception {
        doReturn(nodeDetails).when(delegateExecution).getVariable(NODE_DETAILS);
        configureStoragePools.delegateExecute(delegateExecution);
        verify(configureStoragePools, never()).createValidStoragePool(anyString(), any());
    }

    @Test
    public void delegateExecute_noNode() throws Exception {
        doReturn(null).when(delegateExecution).getVariable(NODE_DETAILS);
        try {
            configureStoragePools.delegateExecute(delegateExecution);
            fail("Expecting Bpmn error, got none.");
        } catch (BpmnError e) {
            assertEquals("The List of Node Detail was not found!  Please add at least one Node Detail and try again.", e.getMessage());
        } catch (Exception e) {
            fail("Unexpected error thrown.");
        }
    }

    @Test
    public void delegateExecute_newPoolCreated() throws Exception {
        nodeDetails.stream().forEach(nodeDetail -> {
            nodeDetail.getDeviceToDeviceStoragePool().entrySet().stream().forEach(entry -> {
                entry.getValue().setStoragePoolId(null);
            });
        });

        Map<String, String> storagePoolNameToId = new HashMap<>();
        storagePoolNameToId.put("temp-1", "0e392a4100000001");
        storagePoolNameToId.put("temp-2", "0e392a4100000002");

        doReturn(nodeDetails).when(delegateExecution).getVariable(NODE_DETAILS);
        doReturn(storagePoolNameToId).when(configureStoragePools).createValidStoragePool(anyString(), any());

        configureStoragePools.delegateExecute(delegateExecution);

        nodeDetails.stream().forEach(nodeDetail -> {
            nodeDetail.getDeviceToDeviceStoragePool().entrySet().stream().forEach(entry -> {
                if ("temp-1".equals(entry.getValue().getStoragePoolId())) {
                    assertEquals("0e392a4100000001", entry.getValue().getStoragePoolId());
                } else if ("temp-2".equals(entry.getValue().getStoragePoolId())) {
                    assertEquals("0e392a4100000002", entry.getValue().getStoragePoolId());
                }
            });
        });
    }
}
