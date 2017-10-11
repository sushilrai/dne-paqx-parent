/**
 * <p>
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries. All Rights Reserved. Dell EMC Confidential/Proprietary Information
 * </p>
 */

package com.dell.cpsd.paqx.dne.transformers;

import com.dell.cpsd.paqx.dne.domain.scaleio.ScaleIODevice;
import com.dell.cpsd.paqx.dne.domain.scaleio.ScaleIOSDS;
import com.dell.cpsd.paqx.dne.domain.scaleio.ScaleIOStoragePool;
import com.dell.cpsd.service.engineering.standards.EssValidateStoragePoolRequestMessage;
import com.dell.cpsd.service.engineering.standards.StoragePool;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import com.dell.cpsd.paqx.dne.domain.vcenter.Host;
import com.dell.cpsd.paqx.dne.domain.vcenter.HostStorageDevice;

import java.util.ArrayList;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;

import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.containsString;

/**
 * Unit tests for storage transformer.
 * <p>
 * <p>
 * <p>
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries.  All Rights Reserved.
 * Dell EMC Confidential/Proprietary Information
 * </p>
 *
 * @since 1.0
 */

public class StoragePoolEssRequestTransformerTest
{

    private StoragePoolEssRequestTransformer transformer;
    private List<ScaleIOStoragePool>         listScaleIOStoragePool;
    private List<ScaleIODevice>              listDevices;

    @Before
    public void setUp() throws Exception
    {
        transformer = new StoragePoolEssRequestTransformer();
    }

    @Test
    public void getHostToStorageDeviceMap_success()
    {

        List<Host> hostList = new ArrayList<>();
        Host host1 = buildHost("fpr1-h13.lab.vce.com", 4, true);
        hostList.add(host1);
        Host host2 = buildHost("fpr1-h11.lab.vce.com", 5, false);
        hostList.add(host2);

        Map<String, Map<String, HostStorageDevice>> map = transformer.getHostToStorageDeviceMap(hostList);
        assertNotNull(map.get("fpr1-h11.lab.vce.com-ESX"));
        assertNotNull(map.get("fpr1-h13.lab.vce.com-ESX"));
        assertEquals(5, map.get("fpr1-h11.lab.vce.com-ESX").size());
        assertEquals(4, map.get("fpr1-h13.lab.vce.com-ESX").size());

        map.get("fpr1-h13.lab.vce.com-ESX").forEach((key, value) -> {
            assertThat(key, containsString("displayName"));
            assertEquals(key, value.getDisplayName());
        });

        map.get("fpr1-h11.lab.vce.com-ESX").forEach((key, value) -> {
            assertThat(key, containsString("displayName"));
            assertEquals(key, value.getDisplayName());
        });

    }

    @Test
    public void getHostToStorageDeviceMap_emptyHosts()
    {

        List<Host> hostList = new ArrayList<>();

        Map<String, Map<String, HostStorageDevice>> map = transformer.getHostToStorageDeviceMap(hostList);
        assertNotNull(map);
        assertEquals(0, map.size());
    }

    @Test
    public void correlateScaleIOAndVcenterData_success()
    {
        Map<String, Map<String, HostStorageDevice>> hostToStorageDeviceMap = new HashMap<>();

        Map<String, HostStorageDevice> hostStorageDeviceMap1 = new HashMap<>();
        hostStorageDeviceMap1.put("displayName1", buildHostStorageDevice("1", true));
        hostStorageDeviceMap1.put("displayName2", buildHostStorageDevice("2", true));
        hostStorageDeviceMap1.put("displayName3", buildHostStorageDevice("3", true));
        hostToStorageDeviceMap.put("fpr1-h11.lab.vce.com", hostStorageDeviceMap1);

        Map<String, HostStorageDevice> hostStorageDeviceMap2 = new HashMap<>();
        hostStorageDeviceMap2.put("displayName4", buildHostStorageDevice("4", true));
        hostStorageDeviceMap2.put("displayName5", buildHostStorageDevice("5", true));
        hostStorageDeviceMap2.put("displayName6", buildHostStorageDevice("6", true));
        hostToStorageDeviceMap.put("fpr1-h12.lab.vce.com", hostStorageDeviceMap2);

        Map<String, HostStorageDevice> hostStorageDeviceMap3 = new HashMap<>();
        hostStorageDeviceMap3.put("displayName7", buildHostStorageDevice("7", true));
        hostStorageDeviceMap3.put("displayName8", buildHostStorageDevice("8", true));
        hostStorageDeviceMap3.put("displayName9", buildHostStorageDevice("9", true));
        hostToStorageDeviceMap.put("fpr1-h13.lab.vce.com", hostStorageDeviceMap3);

        ScaleIOSDS sds = buildScaleIOSDS("fpr1-h12.lab.vce.com-ESX");

        Map<String, HostStorageDevice> actual = transformer.correlateScaleIOAndVcenterData(sds, hostToStorageDeviceMap);

        assertNotNull(actual);
        assertEquals(3, actual.size());
        actual.forEach((key, value) -> {
            assertThat(key, anyOf(is("displayName4"), is("displayName5"), is("displayName6")));
        });
    }

    @Test
    public void correlateScaleIOAndVcenterData_notFound()
    {
        Map<String, Map<String, HostStorageDevice>> hostToStorageDeviceMap = new HashMap<>();

        Map<String, HostStorageDevice> hostStorageDeviceMap1 = new HashMap<>();
        hostStorageDeviceMap1.put("displayName1", buildHostStorageDevice("1", true));
        hostStorageDeviceMap1.put("displayName2", buildHostStorageDevice("2", true));
        hostStorageDeviceMap1.put("displayName3", buildHostStorageDevice("3", true));
        hostToStorageDeviceMap.put("fpr1-h11.lab.vce.com", hostStorageDeviceMap1);


        ScaleIOSDS sds = buildScaleIOSDS("fpr1-h12.lab.vce.com-ESX");

        try {
            Map<String, HostStorageDevice> actual = transformer.correlateScaleIOAndVcenterData(sds, hostToStorageDeviceMap);
            fail("Expecting error but did not error.");
        } catch (NoSuchElementException ex) {
            assertEquals("No value present", ex.getLocalizedMessage());
        }
    }

    private ScaleIOStoragePool buildScaleIOStoragePool(String id, int numberOfDevices)
    {
        ScaleIOStoragePool storagePool = new ScaleIOStoragePool();
        storagePool.setId(id);
        storagePool.setName("SP-" + id);
        List<ScaleIODevice> devices = new ArrayList<>();
        ScaleIODevice device = null;
        for (int i = 0; i < numberOfDevices; i++)
        {
            device = new ScaleIODevice("" + i, "Dv-" + i, null, "Normal");
            devices.add(device);
        }

        return storagePool;
    }

    private Host buildHost(String name, int numberOfDevices, boolean allFlash)
    {
        Host host = new Host();
        host.setName(name + "-ESX");

        List<HostStorageDevice> hostStorageDeviceList = new ArrayList<>();
        HostStorageDevice hostStorageDevice = null;
        for (int i = 0; i < numberOfDevices; i++)
        {
            hostStorageDeviceList.add(buildHostStorageDevice(""+i, allFlash));
        }
        host.setHostStorageDeviceList(hostStorageDeviceList);
        return host;
    }

    private HostStorageDevice buildHostStorageDevice(String id, boolean ssd)
    {
        return new HostStorageDevice("displayName" + id, ssd, "sn" + id, null);
    }

    private ScaleIOSDS buildScaleIOSDS(String id)
    {
        return new ScaleIOSDS("id-"+id, id, null, 8800);
    }

}
