/**
 * <p>
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries. All Rights Reserved. Dell EMC Confidential/Proprietary Information
 * </p>
 */
package com.dell.cpsd.paqx.dne.service.delegates;

import com.dell.cpsd.paqx.dne.domain.node.DiscoveredNodeInfo;
import com.dell.cpsd.paqx.dne.domain.scaleio.ScaleIOData;
import com.dell.cpsd.paqx.dne.domain.scaleio.ScaleIOProtectionDomain;
import com.dell.cpsd.paqx.dne.domain.vcenter.HostStorageDevice;
import com.dell.cpsd.paqx.dne.exception.TaskResponseFailureException;
import com.dell.cpsd.paqx.dne.service.NodeService;
import com.dell.cpsd.paqx.dne.service.delegates.model.NodeDetail;
import com.dell.cpsd.service.common.client.exception.ServiceExecutionException;
import com.dell.cpsd.service.common.client.exception.ServiceTimeoutException;
import com.dell.cpsd.service.engineering.standards.Device;
import com.dell.cpsd.service.engineering.standards.DeviceAssignment;
import com.dell.cpsd.service.engineering.standards.Error;
import com.dell.cpsd.service.engineering.standards.EssValidateStoragePoolResponseMessage;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.dell.cpsd.paqx.dne.service.delegates.utils.DelegateConstants.NODE_DETAIL;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;

@RunWith(MockitoJUnitRunner.class)
/**
 * <p>
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries. All Rights Reserved. Dell EMC Confidential/Proprietary Information
 * </p>
 */
public class SelectStoragePoolsTest
{
    private SelectStoragePools selectStoragePools;

    private NodeDetail nodeDetail;

    @Mock
    private NodeService nodeService;

    @Mock
    private DelegateExecution delegateExecution;

    private List<DiscoveredNodeInfo> discoveredNodeInfoList = new ArrayList<>();
    List<NodeDetail> nodeDetails = new ArrayList<>();
    List<Device> newDevices = new ArrayList<>();
    List<ScaleIOProtectionDomain> protectionDomains = new ArrayList<>();

    @Before
    public void setUp() throws Exception
    {
        selectStoragePools = Mockito.spy(new SelectStoragePools(nodeService));
        nodeDetail = new NodeDetail("1", "abc");
        nodeDetail.setProtectionDomainId("pdId1");
//        doReturn(nodeDetail).when(delegateExecution).getVariable(NODE_DETAIL);
        setupMockScaleIODataList();
        setupDiscoveredNodeInfo();
        setupNodeDetails();
        setupNewDevices();
        setupProtectionDomains();
    }



    private void setupMockScaleIODataList()
    {
        List<ScaleIOData> scaleIODataList = new ArrayList<>();
        ScaleIOData scaleIOData = new ScaleIOData("sio1", "name1", "installId", "mdmMode", "systemVersion", "clusterState", "version");

        ScaleIOProtectionDomain protectionDomain1 = new ScaleIOProtectionDomain("PD-1", "pdName1", "ACTIVE");
        scaleIOData.addProtectionDomain(protectionDomain1);
        protectionDomain1.setScaleIOData(scaleIOData);
        scaleIODataList.add(scaleIOData);
//        doReturn(scaleIODataList).when(nodeService).listScaleIOData();
    }

    private void setupDiscoveredNodeInfo() {
        List<DiscoveredNodeInfo> discoveredNodeInfoList = new ArrayList<>();
        DiscoveredNodeInfo discoveredNodeInfo1 = new DiscoveredNodeInfo("M-1", "MF-1", "P-1", "PF-1", "SN-1", "UUID-1");
        discoveredNodeInfoList.add(discoveredNodeInfo1);
        DiscoveredNodeInfo discoveredNodeInfo2 = new DiscoveredNodeInfo("M-2", "MF-2", "P-2", "PF-2", "SN-2", "UUID-2");
        discoveredNodeInfoList.add(discoveredNodeInfo2);
        this.discoveredNodeInfoList = discoveredNodeInfoList;
    }

    private void setupNodeDetails() {
        List<NodeDetail> nodeDetails = new ArrayList<>();
        NodeDetail nodeDetail1 = new NodeDetail("ND-1", "SN-1");
        nodeDetail1.setProtectionDomainId("PD-1");
        nodeDetails.add(nodeDetail1);
        NodeDetail nodeDetail2 = new NodeDetail("ND-2", "SN-2");
        nodeDetail2.setProtectionDomainId("PD-2");
        nodeDetails.add(nodeDetail2);
        this.nodeDetails = nodeDetails;
    }

    private void setupNewDevices()
    {
        List<Device> mockDevices = new ArrayList<>();
        Device device1 = new Device("dev1Id-1", "dev1Name-1", "active", "dev1Serial-1", "dev1CanName", "dev1LogicalName", Device.Type.SSD,
                "1800000000");
        mockDevices.add(device1);
        Device device2 = new Device("dev1Id-2", "dev1Name-2", "active", "dev1Serial-2", "dev1CanName", "dev1LogicalName", Device.Type.SSD,
                "1800000000");
        mockDevices.add(device2);
        Device device3 = new Device("dev1Id-3", "dev1Name-3", "active", "dev1Serial-3", "dev1CanName", "dev1LogicalName", Device.Type.SSD,
                "1800000000");
        mockDevices.add(device3);
        this.newDevices = mockDevices;
    }

    private void setupProtectionDomains() {
        List<ScaleIOProtectionDomain> protectionDomains = new ArrayList<>();
        ScaleIOProtectionDomain protectionDomain1 = new ScaleIOProtectionDomain("PD-1", "pdName1", "ACTIVE");
        protectionDomains.add(protectionDomain1);
        ScaleIOProtectionDomain protectionDomain2 = new ScaleIOProtectionDomain("PD-2", "pdName2", "ACTIVE");
        protectionDomains.add(protectionDomain2);
        this.protectionDomains = protectionDomains;
    }

    @Test
    public void populateDeviceMaps_success() {
        Map<String, List<Device>> nodeToDeviceMap = new HashMap<>();
        Map<String, List<Device>> protectionDomainToDevicesMap = new HashMap<>();
        doReturn(newDevices).when(selectStoragePools).getNewDevices("ND-1");
        doReturn(newDevices).when(selectStoragePools).getNewDevices("ND-2");
        selectStoragePools.populateDeviceMaps(this.nodeDetails, this.discoveredNodeInfoList, nodeToDeviceMap, protectionDomainToDevicesMap);
        assertNotNull(nodeToDeviceMap);
        assertNotNull(protectionDomainToDevicesMap);
        assertEquals(2, nodeToDeviceMap.size());
        assertEquals(2, protectionDomainToDevicesMap.size());
        assertEquals(3, nodeToDeviceMap.get("ND-1").size());
        assertEquals(3, nodeToDeviceMap.get("ND-2").size());
        assertEquals(3, protectionDomainToDevicesMap.get("PD-1").size());
        assertEquals(3, protectionDomainToDevicesMap.get("PD-2").size());
    }

    @Test
    public void populateDeviceMaps_noProtectionDomainId() {
        Map<String, List<Device>> nodeToDeviceMap = new HashMap<>();
        Map<String, List<Device>> protectionDomainToDevicesMap = new HashMap<>();
        doReturn(newDevices).when(selectStoragePools).getNewDevices("ND-1");
//        doReturn(newDevices).when(selectStoragePools).getNewDevices("ND-2");
        this.nodeDetails.get(1).setProtectionDomainId(null);
        try {
            selectStoragePools.populateDeviceMaps(this.nodeDetails, this.discoveredNodeInfoList, nodeToDeviceMap, protectionDomainToDevicesMap);
            fail("Expected IllegalStateException.");
        } catch(IllegalStateException e) {
            assertEquals("Could not find a valid protection domain for node: ND-2", e.getMessage());
        }
    }


    @Test
    public void validateStoragePoolsAndSetResponse_success() throws Exception {
        EssValidateStoragePoolResponseMessage storageResponseMessage = new EssValidateStoragePoolResponseMessage();
        storageResponseMessage.setWarnings(Collections.emptyList());
        storageResponseMessage.setErrors(Collections.emptyList());

        Map<String, DeviceAssignment> deviceToStoragePoolMap = new HashMap<>();
        deviceToStoragePoolMap.put("id-1", new DeviceAssignment("id-1", "sn-1", "ln-1", "dn-1", "spid-1", "spn-1"));
        deviceToStoragePoolMap.put("id-2", new DeviceAssignment("id-2", "sn-2", "ln-2", "dn-2", "spid-1", "spn-1"));
        storageResponseMessage.setDeviceToStoragePoolMap(deviceToStoragePoolMap);
        doReturn(storageResponseMessage).when(nodeService).validateStoragePools(anyList(), anyList(), anyMap());

        Map<String, DeviceAssignment> deviceMap = new HashMap<>();
        selectStoragePools.validateStoragePoolsAndSetResponse(deviceMap, newDevices, new HashMap<String, Map<String, HostStorageDevice>>(), protectionDomains, "PD-1");
        assertFalse(deviceMap.isEmpty());
        assertEquals(2, deviceMap.size());
    }

    /*@Test
    public void testSuccessful() throws ServiceTimeoutException, ServiceExecutionException
    {
        mockPopulatedNewDevices();
        mockFoundValidStoragePoolResponse();

        selectStoragePools.delegateExecute(delegateExecution);
        Map<String, DeviceAssignment> deviceAssignment = nodeDetail.getDeviceToDeviceStoragePool();
        assertFalse(deviceAssignment.isEmpty());
    }

    @Test(expected = BpmnError.class)
    public void testEmptyDisks() throws ServiceTimeoutException, ServiceExecutionException
    {
        mockUnpopulatedNewDevices();
        selectStoragePools.delegateExecute(delegateExecution);
    }

    @Test
    public void testNoValidStoragePoolsInitially() throws ServiceTimeoutException, ServiceExecutionException, TaskResponseFailureException
    {
        mockPopulatedNewDevices();
        setupInvalidStoragePoolMock();

        selectStoragePools.delegateExecute(delegateExecution);
        Map<String, DeviceAssignment> deviceAssignment = nodeDetail.getDeviceToDeviceStoragePool();
        assertFalse(deviceAssignment.isEmpty());
        deviceAssignment.values().stream().filter(Objects::nonNull)
                .forEach(da -> assertTrue(da.getStoragePoolId() == null && da.getStoragePoolName().equals("temp")));
    }

    private void setupInvalidStoragePoolMock() throws ServiceTimeoutException, ServiceExecutionException
    {
        EssValidateStoragePoolResponseMessage invalidStoragePoolResponse = setupInvalidStoragePoolResponse();

        doReturn(invalidStoragePoolResponse).when(nodeService)
                .validateStoragePools(Mockito.anyList(), Mockito.anyList(), Mockito.anyMap());
    }

    private EssValidateStoragePoolResponseMessage setupInvalidStoragePoolResponse()
    {
        Map<String, DeviceAssignment> emptyDeviceToStoragePoolMap = new HashMap<>();
        EssValidateStoragePoolResponseMessage invalidValidateStoragePoolsResponse = new EssValidateStoragePoolResponseMessage();
        invalidValidateStoragePoolsResponse.setDeviceToStoragePoolMap(emptyDeviceToStoragePoolMap);
        List<Error> errors = new ArrayList<>();
        errors.add(new Error());
        invalidValidateStoragePoolsResponse.setErrors(errors);
        return invalidValidateStoragePoolsResponse;
    }

    private EssValidateStoragePoolResponseMessage createValidStoragePoolResponse()
    {
        Map<String, DeviceAssignment> deviceToStoragePoolMap = new HashMap<>();
        deviceToStoragePoolMap.put("jack", new DeviceAssignment("id1", "ser1", "lname1", "dname", "spid", "spname"));
        EssValidateStoragePoolResponseMessage validateStoragePoolsResponse = new EssValidateStoragePoolResponseMessage();
        validateStoragePoolsResponse.setDeviceToStoragePoolMap(deviceToStoragePoolMap);
        return validateStoragePoolsResponse;
    }

    private void mockFoundValidStoragePoolResponse() throws ServiceTimeoutException, ServiceExecutionException
    {
        doReturn(createValidStoragePoolResponse()).when(nodeService)
                .validateStoragePools(Mockito.anyList(), Mockito.anyList(), Mockito.anyMap());
    }

    private void setupMockScaleIODataList()
    {
        List<ScaleIOData> scaleIODataList = new ArrayList<>();
        ScaleIOData scaleIOData = new ScaleIOData("sio1", "name1", "installId", "mdmMode", "systemVersion", "clusterState", "version");

        ScaleIOProtectionDomain protectionDomain1 = new ScaleIOProtectionDomain("pdId1", "pdName1", "ACTIVE");
        scaleIOData.addProtectionDomain(protectionDomain1);
        protectionDomain1.setScaleIOData(scaleIOData);
        scaleIODataList.add(scaleIOData);
        doReturn(scaleIODataList).when(nodeService).listScaleIOData();
    }

    private void mockPopulatedNewDevices()
    {
        List<Device> mockDevices = new ArrayList<>();
        Device device = new Device("dev1Id", "dev1Name", "active", "dev1Serial", "dev1CanName", "dev1LogicalName", Device.Type.SSD,
                "900000000");
        mockDevices.add(device);
        doReturn(mockDevices).when(selectStoragePools).getNewDevices(anyString());
    }

    private void mockUnpopulatedNewDevices()
    {
        List<Device> mockDevices = new ArrayList<>();
        doReturn(mockDevices).when(selectStoragePools).getNewDevices(anyString());
    }*/
}