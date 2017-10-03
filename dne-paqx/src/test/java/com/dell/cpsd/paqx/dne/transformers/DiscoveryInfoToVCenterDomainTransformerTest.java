/**
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries.  All Rights Reserved.
 * Dell EMC Confidential/Proprietary Information
 */

package com.dell.cpsd.paqx.dne.transformers;

import com.dell.cpsd.paqx.dne.domain.vcenter.VCenter;
import com.dell.cpsd.virtualization.capabilities.api.Cluster;
import com.dell.cpsd.virtualization.capabilities.api.DVPortSetting;
import com.dell.cpsd.virtualization.capabilities.api.DVPortgroupConfigInfo;
import com.dell.cpsd.virtualization.capabilities.api.DVSSecurityPolicy;
import com.dell.cpsd.virtualization.capabilities.api.Datacenter;
import com.dell.cpsd.virtualization.capabilities.api.Datastore;
import com.dell.cpsd.virtualization.capabilities.api.DatastoreSummary;
import com.dell.cpsd.virtualization.capabilities.api.DiscoveryResponseInfoMessage;
import com.dell.cpsd.virtualization.capabilities.api.DistributedVirtualSwicthPortConnection;
import com.dell.cpsd.virtualization.capabilities.api.DistributedVirtualSwitchHostMember;
import com.dell.cpsd.virtualization.capabilities.api.DistributedVirtualSwitchHostMemberConfigInfo;
import com.dell.cpsd.virtualization.capabilities.api.DistributedVirtualSwitchHostMemberPnicBacking;
import com.dell.cpsd.virtualization.capabilities.api.DistributedVirtualSwitchHostMemberPnicSpec;
import com.dell.cpsd.virtualization.capabilities.api.DvSwitch;
import com.dell.cpsd.virtualization.capabilities.api.GuestInfo;
import com.dell.cpsd.virtualization.capabilities.api.GuestNicInfo;
import com.dell.cpsd.virtualization.capabilities.api.HostConfigInfo;
import com.dell.cpsd.virtualization.capabilities.api.HostDateTimeInfo;
import com.dell.cpsd.virtualization.capabilities.api.HostDnsConfig;
import com.dell.cpsd.virtualization.capabilities.api.HostHardwareInfo;
import com.dell.cpsd.virtualization.capabilities.api.HostIpConfig;
import com.dell.cpsd.virtualization.capabilities.api.HostIpRouteConfig;
import com.dell.cpsd.virtualization.capabilities.api.HostNetworkInfo;
import com.dell.cpsd.virtualization.capabilities.api.HostNetworkPolicy;
import com.dell.cpsd.virtualization.capabilities.api.HostNetworkSecurityPolicy;
import com.dell.cpsd.virtualization.capabilities.api.HostPciDevice;
import com.dell.cpsd.virtualization.capabilities.api.HostSystem;
import com.dell.cpsd.virtualization.capabilities.api.HostSystemIdentificationInfo;
import com.dell.cpsd.virtualization.capabilities.api.HostSystemInfo;
import com.dell.cpsd.virtualization.capabilities.api.HostVirtualNic;
import com.dell.cpsd.virtualization.capabilities.api.HostVirtualNicSpec;
import com.dell.cpsd.virtualization.capabilities.api.HostVirtualSwitch;
import com.dell.cpsd.virtualization.capabilities.api.HostVirtualSwitchSpec;
import com.dell.cpsd.virtualization.capabilities.api.Network;
import com.dell.cpsd.virtualization.capabilities.api.PhysicalNic;
import com.dell.cpsd.virtualization.capabilities.api.PortGroup;
import com.dell.cpsd.virtualization.capabilities.api.VMwareDVSConfigInfo;
import com.dell.cpsd.virtualization.capabilities.api.VirtualMachine;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

/**
 * The test class for testing the actual DiscoveryInfoToVCenterDomainTransformer class.
 * <p>
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries.  All Rights Reserved.
 * Dell EMC Confidential/Proprietary Information
 * </p>
 *
 * @version 1.0
 * @since 1.0
 */
public class DiscoveryInfoToVCenterDomainTransformerTest
{
    private DiscoveryInfoToVCenterDomainTransformer transformer;

    @Before
    public void setUp() throws Exception
    {
        this.transformer = new DiscoveryInfoToVCenterDomainTransformer();
    }

    @Test
    public void transform_should_successfully_transform_a_DiscoveryResponseInfoMessage_into_a_VCenter_object()
    {
        final DiscoveryResponseInfoMessage discoveryResponseInfoMessage = makeDiscoveryResponseInfoMessage();

        final VCenter result = this.transformer.transform(discoveryResponseInfoMessage);

        assertNotNull(result);
        assertNotNull(result.getName());
        assertNotNull(result.getId());
        assertNotNull(result.getDataCenterList());
        assertThat(result.getDataCenterList().size(), is(greaterThan(0)));
        assertNotNull(result.getDataCenterList().get(0).getId());
        assertNotNull(result.getDataCenterList().get(0).getName());
        assertNotNull(result.getDataCenterList().get(0).getvCenter());
        assertThat(result.getDataCenterList().get(0).getvCenter(), is(result));
        assertNotNull(result.getDataCenterList().get(0).getClusterList());
        assertThat(result.getDataCenterList().get(0).getClusterList().size(), is(greaterThan(0)));
        assertNotNull(result.getDataCenterList().get(0).getDatastoreList());
        assertThat(result.getDataCenterList().get(0).getDatastoreList().size(), is(greaterThan(0)));
        assertNotNull(result.getDataCenterList().get(0).getDvSwitchList());
        assertThat(result.getDataCenterList().get(0).getDvSwitchList().size(), is(greaterThan(0)));
        assertNotNull(result.getDataCenterList().get(0).getNetworkList());
        assertThat(result.getDataCenterList().get(0).getNetworkList().size(), is(greaterThan(0)));
    }

    @Test
    public void transform_should_return_null_if_the_DiscoveryResponseInfoMessage_is_null()
    {
        final VCenter result = this.transformer.transform(null);

        assertNull(result);
    }

    @Test
    public void transform_should_filter_out_null_datacenter_objects()
    {
        final DiscoveryResponseInfoMessage discoveryResponseInfoMessage = makeDiscoveryResponseInfoMessage();
        discoveryResponseInfoMessage.getDatacenters().set(0, null);

        final VCenter result = this.transformer.transform(discoveryResponseInfoMessage);

        assertNotNull(result);
        assertThat(result.getDataCenterList(), is(empty()));
    }

    @Test
    public void transform_should_filter_out_null_virtual_machine_objects()
    {
        final DiscoveryResponseInfoMessage discoveryResponseInfoMessage = makeDiscoveryResponseInfoMessage();
        discoveryResponseInfoMessage.getDatacenters().get(0).getVms().keySet()
                .forEach(key -> discoveryResponseInfoMessage.getDatacenters().get(0).getVms().put(key, null));

        final VCenter result = this.transformer.transform(discoveryResponseInfoMessage);

        assertNotNull(result);
        assertThat(result.getDataCenterList().get(0).getDatastoreList().get(0).getVirtualMachineList(), is(empty()));
    }

    @Test
    public void transform_should_filter_out_null_datastore_objects()
    {
        final DiscoveryResponseInfoMessage discoveryResponseInfoMessage = makeDiscoveryResponseInfoMessage();
        discoveryResponseInfoMessage.getDatacenters().get(0).getDatastores().keySet()
                .forEach(key -> discoveryResponseInfoMessage.getDatacenters().get(0).getDatastores().put(key, null));

        final VCenter result = this.transformer.transform(discoveryResponseInfoMessage);

        assertNotNull(result);
        assertThat(result.getDataCenterList().get(0).getDatastoreList(), is(empty()));
    }

    @Test
    public void transform_should_filter_out_null_cluster_objects()
    {
        final DiscoveryResponseInfoMessage discoveryResponseInfoMessage = makeDiscoveryResponseInfoMessage();
        discoveryResponseInfoMessage.getDatacenters().get(0).getClusters().keySet()
                .forEach(key -> discoveryResponseInfoMessage.getDatacenters().get(0).getClusters().put(key, null));

        final VCenter result = this.transformer.transform(discoveryResponseInfoMessage);

        assertNotNull(result);
        assertThat(result.getDataCenterList().get(0).getClusterList(), is(empty()));
    }

    @Test
    public void transform_should_filter_out_null_network_objects()
    {
        final DiscoveryResponseInfoMessage discoveryResponseInfoMessage = makeDiscoveryResponseInfoMessage();
        discoveryResponseInfoMessage.getDatacenters().get(0).getNetworks().keySet()
                .forEach(key -> discoveryResponseInfoMessage.getDatacenters().get(0).getNetworks().put(key, null));

        final VCenter result = this.transformer.transform(discoveryResponseInfoMessage);

        assertNotNull(result);
        assertThat(result.getDataCenterList().get(0).getNetworkList(), is(empty()));
    }

    @Test
    public void transform_should_filter_out_null_dvswitch_objects()
    {
        final DiscoveryResponseInfoMessage discoveryResponseInfoMessage = makeDiscoveryResponseInfoMessage();
        discoveryResponseInfoMessage.getDatacenters().get(0).getDvSwitches().keySet()
                .forEach(key -> discoveryResponseInfoMessage.getDatacenters().get(0).getDvSwitches().put(key, null));

        final VCenter result = this.transformer.transform(discoveryResponseInfoMessage);

        assertNotNull(result);
        assertThat(result.getDataCenterList().get(0).getDvSwitchList(), is(empty()));
    }

    @Test
    public void transform_should_filter_out_null_portgroup_objects()
    {
        final DiscoveryResponseInfoMessage discoveryResponseInfoMessage = makeDiscoveryResponseInfoMessage();
        discoveryResponseInfoMessage.getDatacenters().get(0).getPortgroups().keySet()
                .forEach(key -> discoveryResponseInfoMessage.getDatacenters().get(0).getPortgroups().put(key, null));

        final VCenter result = this.transformer.transform(discoveryResponseInfoMessage);

        assertNotNull(result);
        assertThat(result.getDataCenterList().get(0).getDvSwitchList().get(0).getPortGroupList(), is(empty()));
    }

    @Test
    public void transform_should_filter_out_null_hostsystem_objects()
    {
        final DiscoveryResponseInfoMessage discoveryResponseInfoMessage = makeDiscoveryResponseInfoMessage();
        discoveryResponseInfoMessage.getDatacenters().get(0).getClusters().keySet().forEach(
                key -> discoveryResponseInfoMessage.getDatacenters().get(0).getClusters().get(key).getHosts().keySet().forEach(
                        hostKey -> discoveryResponseInfoMessage.getDatacenters().get(0).getClusters().get(key).getHosts()
                                .put(hostKey, null)));

        final VCenter result = this.transformer.transform(discoveryResponseInfoMessage);

        assertNotNull(result);
        assertThat(result.getDataCenterList().get(0).getClusterList().get(0).getHostList(), is(empty()));
    }

    @Test
    public void transform_should_filter_out_null_hostsystem_pci_device_objects()
    {
        final DiscoveryResponseInfoMessage discoveryResponseInfoMessage = makeDiscoveryResponseInfoMessage();
        discoveryResponseInfoMessage.getDatacenters().get(0).getClusters().keySet().forEach(
                key -> discoveryResponseInfoMessage.getDatacenters().get(0).getClusters().get(key).getHosts().keySet().forEach(
                        hostKey -> discoveryResponseInfoMessage.getDatacenters().get(0).getClusters().get(key).getHosts().get(hostKey)
                                .getHostHardwareInfo().getPciDevice().set(0, null)));

        final VCenter result = this.transformer.transform(discoveryResponseInfoMessage);

        assertNotNull(result);
        assertThat(result.getDataCenterList().get(0).getClusterList().get(0).getHostList().get(0).getPciDevices(), is(empty()));
    }

    @Test
    public void transform_should_filter_out_null_vswitch_objects()
    {
        final DiscoveryResponseInfoMessage discoveryResponseInfoMessage = makeDiscoveryResponseInfoMessage();
        discoveryResponseInfoMessage.getDatacenters().get(0).getClusters().keySet().forEach(
                key -> discoveryResponseInfoMessage.getDatacenters().get(0).getClusters().get(key).getHosts().keySet().forEach(
                        hostKey -> discoveryResponseInfoMessage.getDatacenters().get(0).getClusters().get(key).getHosts().get(hostKey)
                                .getHostConfigInfo().getHostNetworkInfo().getVswitchs().set(0, null)));

        final VCenter result = this.transformer.transform(discoveryResponseInfoMessage);

        assertNotNull(result);
        assertThat(result.getDataCenterList().get(0).getClusterList().get(0).getHostList().get(0).getvSwitchList(), is(empty()));
    }

    @Test
    public void transform_should_filter_out_null_vnic_objects()
    {
        final DiscoveryResponseInfoMessage discoveryResponseInfoMessage = makeDiscoveryResponseInfoMessage();
        discoveryResponseInfoMessage.getDatacenters().get(0).getClusters().keySet().forEach(
                key -> discoveryResponseInfoMessage.getDatacenters().get(0).getClusters().get(key).getHosts().keySet().forEach(
                        hostKey -> discoveryResponseInfoMessage.getDatacenters().get(0).getClusters().get(key).getHosts().get(hostKey)
                                .getHostConfigInfo().getHostNetworkInfo().getVnics().set(0, null)));

        final VCenter result = this.transformer.transform(discoveryResponseInfoMessage);

        assertNotNull(result);
        assertThat(result.getDataCenterList().get(0).getClusterList().get(0).getHostList().get(0).getVirtualNicList(), is(empty()));
    }

    @Test
    public void transform_should_filter_out_null_pnic_objects()
    {
        final DiscoveryResponseInfoMessage discoveryResponseInfoMessage = makeDiscoveryResponseInfoMessage();
        discoveryResponseInfoMessage.getDatacenters().get(0).getClusters().keySet().forEach(
                key -> discoveryResponseInfoMessage.getDatacenters().get(0).getClusters().get(key).getHosts().keySet().forEach(
                        hostKey -> discoveryResponseInfoMessage.getDatacenters().get(0).getClusters().get(key).getHosts().get(hostKey)
                                .getHostConfigInfo().getHostNetworkInfo().getPnics().set(0, null)));

        final VCenter result = this.transformer.transform(discoveryResponseInfoMessage);

        assertNotNull(result);
        assertThat(result.getDataCenterList().get(0).getClusterList().get(0).getHostList().get(0).getPhysicalNicList(), is(empty()));
    }

    @Test
    public void transform_should_filter_out_null_guest_nic_info_objects()
    {
        final DiscoveryResponseInfoMessage discoveryResponseInfoMessage = makeDiscoveryResponseInfoMessage();
        discoveryResponseInfoMessage.getDatacenters().get(0).getVms().keySet().forEach(
                key -> discoveryResponseInfoMessage.getDatacenters().get(0).getVms().get(key).getGuestInfo().getNet().set(0, null));

        final VCenter result = this.transformer.transform(discoveryResponseInfoMessage);

        assertNotNull(result);
        assertThat(result.getDataCenterList().get(0).getClusterList().get(0).getHostList().get(0).getVirtualMachineList().get(0).getVmNetworkList(), is(empty()));
    }

    @Test
    public void transform_should_filter_out_null_vm_ip_objects()
    {
        final DiscoveryResponseInfoMessage discoveryResponseInfoMessage = makeDiscoveryResponseInfoMessage();
        discoveryResponseInfoMessage.getDatacenters().get(0).getVms().keySet().forEach(
                key -> discoveryResponseInfoMessage.getDatacenters().get(0).getVms().get(key).getGuestInfo().getNet().get(0)
                        .getIpAddresses().set(0, null));

        final VCenter result = this.transformer.transform(discoveryResponseInfoMessage);

        assertNotNull(result);
        assertThat(result.getDataCenterList().get(0).getClusterList().get(0).getHostList().get(0).getVirtualMachineList().get(0)
                .getVmNetworkList().get(0).getVmip(), is(empty()));
    }

    private DiscoveryResponseInfoMessage makeDiscoveryResponseInfoMessage()
    {
        List<Datacenter> dataCenters = new ArrayList<>();
        dataCenters.add(makeDatacenter());

        DiscoveryResponseInfoMessage message = new DiscoveryResponseInfoMessage();
        message.setDatacenters(dataCenters);
        return message;
    }

    private Datacenter makeDatacenter()
    {
        Datacenter datacenter = new Datacenter();
        datacenter.setName("datacenter-1");
        datacenter.setId("datacenter-id-1");
        datacenter.setClusters(makeClusters());
        datacenter.setDatastores(makeDatastores());
        datacenter.setDvSwitches(makeDvSwitches());
        datacenter.setNetworks(makeNetworks());
        datacenter.setPortgroups(makePortGroups());
        datacenter.setVms(makeVms());

        return datacenter;
    }

    private Map<String, VirtualMachine> makeVms()
    {
        VirtualMachine vm = new VirtualMachine();
        vm.setId("vm-id-1");
        vm.setName("vm-1");
        vm.setGuestInfo(makeGuestInfo());

        Map<String, VirtualMachine> vms = new HashMap<>();
        vms.put("vm-1", vm);
        return vms;
    }

    private GuestInfo makeGuestInfo()
    {
        GuestInfo gi = new GuestInfo();
        gi.setGuestFullName("guestinfo-fullname-1");
        gi.setGuestId("guestinfo-id-1");
        gi.setHostName("hostname-1");
        gi.setNet(Arrays.asList(makeGuestNicInfo()));
        return gi;
    }

    private GuestNicInfo makeGuestNicInfo()
    {
        GuestNicInfo info = new GuestNicInfo();
        info.setConnected(true);
        info.setIpAddresses(Arrays.asList("1.1.1.1"));
        info.setMacAddress("0E-02-26-0E-D1-29");
        info.setNetworkId("network-id-1");
        return info;
    }

    private Map<String, PortGroup> makePortGroups()
    {
        PortGroup portGroup = new PortGroup();
        portGroup.setName("portgroup-1");
        portGroup.setId("portgroup-id-1");
        portGroup.setHostIds(Collections.singletonList("host-id-1"));
        portGroup.setPortKeys(Collections.singletonList("portkey-1"));
        portGroup.setVmIds(Collections.singletonList("vm-id-1"));
        portGroup.setDVPortgroupConfigInfo(makeDvPortGroupConfigInfo());

        Map<String, PortGroup> portGroups = new HashMap<>();
        portGroups.put("portgroup-1", portGroup);
        return portGroups;
    }

    private DVPortgroupConfigInfo makeDvPortGroupConfigInfo()
    {
        DVPortgroupConfigInfo info = new DVPortgroupConfigInfo();
        info.setDvSwitchId("dvswitch-id-1");
        info.setDVPortSetting(makeDvPortSetting());
        return info;
    }

    private DVPortSetting makeDvPortSetting()
    {
        DVPortSetting setting = new DVPortSetting();
        setting.setVlanId("vlan-id-1");
        setting.setDVSSecurityPolicy(makeDvsSecurityPolicy());
        return setting;
    }

    private DVSSecurityPolicy makeDvsSecurityPolicy()
    {
        DVSSecurityPolicy dvsSecurityPolicy = new DVSSecurityPolicy();
        dvsSecurityPolicy.setAllowPromicuous(true);
        return dvsSecurityPolicy;
    }

    private Map<String, Network> makeNetworks()
    {
        Network network = new Network();
        network.setId("network-id-1");
        network.setName("network-1");
        network.setVmIds(Collections.singletonList("vlan-id-1"));
        network.setHostIds(Collections.singletonList("host-id-1"));

        Map<String, Network> networks = new HashMap<>();
        networks.put("network-1", network);
        return networks;
    }

    private Map<String, DvSwitch> makeDvSwitches()
    {
        DvSwitch dvSwitch = new DvSwitch();
        dvSwitch.setId("dvswitch-id-1");
        dvSwitch.setName("dvswitch-1");
        dvSwitch.setPortGroupids(Collections.singletonList("portgroup-id-1"));
        dvSwitch.setVMwareDVSConfigInfo(makeVMWareDvsConfigInfo());

        Map<String, DvSwitch> dvSwitches = new HashMap<>();
        dvSwitches.put("dvswitch-1", dvSwitch);
        return dvSwitches;
    }

    private VMwareDVSConfigInfo makeVMWareDvsConfigInfo()
    {
        VMwareDVSConfigInfo vmwareDVSConfigInfo = new VMwareDVSConfigInfo();
        vmwareDVSConfigInfo.setDVPortSetting(makeDvPortSetting());
        vmwareDVSConfigInfo.setHostMembers(Collections.singletonList(makeDistributedVirtualSwitchHostMember()));
        return vmwareDVSConfigInfo;
    }

    private DistributedVirtualSwitchHostMember makeDistributedVirtualSwitchHostMember()
    {
        DistributedVirtualSwitchHostMember member = new DistributedVirtualSwitchHostMember();
        member.setDistributedVirtualSwitchHostMemberConfigInfo(makeDistributedVirtualSwitchHostMemberConfigInfo());
        return member;
    }

    private DistributedVirtualSwitchHostMemberConfigInfo makeDistributedVirtualSwitchHostMemberConfigInfo()
    {
        DistributedVirtualSwitchHostMemberConfigInfo info = new DistributedVirtualSwitchHostMemberConfigInfo();
        info.setHostId("host-id-1");
        info.setDistributedVirtualSwitchHostMemberPnicBacking(makesetDistributedVirtualSwitchHostMemberPnicBacking());
        return info;
    }

    private DistributedVirtualSwitchHostMemberPnicBacking makesetDistributedVirtualSwitchHostMemberPnicBacking()
    {
        DistributedVirtualSwitchHostMemberPnicBacking backing = new DistributedVirtualSwitchHostMemberPnicBacking();
        backing.setSpec(Collections.singletonList(makeDistributedVirtualSwitchHostMemberPnicSpec()));
        return backing;
    }

    private DistributedVirtualSwitchHostMemberPnicSpec makeDistributedVirtualSwitchHostMemberPnicSpec()
    {
        DistributedVirtualSwitchHostMemberPnicSpec spec = new DistributedVirtualSwitchHostMemberPnicSpec();
        spec.setPnicDevice("pnic-device-1");
        spec.setPortGroupId("portgroup-id-1");
        spec.setUplinkPortkey("portkey-1");
        return spec;
    }

    private Map<String, Datastore> makeDatastores()
    {
        Datastore ds = new Datastore();
        ds.setId("datastore-id-1");
        ds.setName("datastore-1");
        ds.setDatastoreSummary(makeDatastoreSummary());
        ds.setVmIds(Collections.singletonList("vm-id-1"));
        ds.setHostIds(Collections.singletonList("host-id-1"));

        Map<String, Datastore> datastores = new HashMap<>();
        datastores.put("datastore-1", ds);
        return datastores;
    }

    private DatastoreSummary makeDatastoreSummary()
    {
        DatastoreSummary summary = new DatastoreSummary();
        summary.setType("satastore-summary-type");
        summary.setUrl("datastore-url-1");
        return summary;
    }

    private Map<String, Cluster> makeClusters()
    {
        Cluster cluster = new Cluster();
        cluster.setId("cluster-id-1");
        cluster.setName("cluster-1");
        cluster.setHosts(makeHostSystems());

        Map<String, Cluster> clusters = new HashMap<>();
        clusters.put("cluster-1", cluster);
        return clusters;
    }

    private Map<String, HostSystem> makeHostSystems()
    {
        HostSystem system = new HostSystem();
        system.setId("host-id-1");
        system.setName("host-1");
        system.setMaintenanceMode(false);
        system.setPowerState("on");
        system.setConnectionState("connection-state");
        system.setHostConfigInfo(makeHostConfigInfo());
        system.setDatastoreIds(Collections.singletonList("datastore-id-1"));
        system.setNetworkIds(Collections.singletonList("network-id-1"));
        system.setVmIds(Collections.singletonList("vm-id-1"));
        system.setHostHardwareInfo(makeHostHardwareInfo());

        Map<String, HostSystem> hosts = new HashMap<>();
        hosts.put("hostsystem-1", system);
        return hosts;
    }

    private HostHardwareInfo makeHostHardwareInfo()
    {
        HostHardwareInfo info = new HostHardwareInfo();
        info.setHostSystemInfo(makeHostSystemInfo());
        info.setPciDevice(Arrays.asList(makeHostPciDevice()));
        return info;
    }

    private HostPciDevice makeHostPciDevice()
    {
        HostPciDevice device = new HostPciDevice();
        device.setId("0000:0002:000");
        device.setDeviceId("device-id-1");
        device.setDeviceName("device-1");
        device.setVendorId("vendor-id-1");
        device.setVendorName("vendor-1");
        device.setSubVendorId("sub-vendor-id-1");
        return device;
    }

    private HostSystemInfo makeHostSystemInfo()
    {
        HostSystemInfo info = new HostSystemInfo();
        info.setModel("R630");
        info.setVendor("DELL");
        info.setHostSystemIdentificationInfo(makeHostSystemIdentificationInfo());
        return info;
    }

    private HostSystemIdentificationInfo makeHostSystemIdentificationInfo()
    {
        HostSystemIdentificationInfo info = new HostSystemIdentificationInfo();
        info.setAssetTag("asset-tag-1");
        info.setServiceTag("service-tag-1");
        info.setOemSpecificString("oem-specific-string-1");
        return info;
    }

    private HostConfigInfo makeHostConfigInfo()
    {
        HostConfigInfo info = new HostConfigInfo();
        info.setHostDateTimeInfo(makeHostDateTimeInfo());
        info.setHostNetworkInfo(makeHostNetworkInfo());
        return info;
    }

    private HostNetworkInfo makeHostNetworkInfo()
    {
        HostNetworkInfo info = new HostNetworkInfo();
        info.setHostDnsConfig(makeHostDnsConfig());
        info.setHostIpRouteConfig(makeHostIpRouteConfig());
        info.setPnics(Arrays.asList(makePhysicalNic()));
        info.setVnics(Arrays.asList(makeHostVirtualNic()));
        info.setVswitchs(Arrays.asList(makeHostVirtualSwitch()));
        return info;
    }

    private HostVirtualSwitch makeHostVirtualSwitch()
    {
        HostVirtualSwitch hostVirtualSwitch = new HostVirtualSwitch();
        hostVirtualSwitch.setKey("host-virtual-switch-key-1");
        hostVirtualSwitch.setName("host-virtual-switch-key-1");
        hostVirtualSwitch.setHostVirtualSwitchSpec(makeHostVirtualSwitchSpec());
        return hostVirtualSwitch;
    }

    private HostVirtualSwitchSpec makeHostVirtualSwitchSpec()
    {
        HostVirtualSwitchSpec spec = new HostVirtualSwitchSpec();
        spec.setHostNetworkPolicy(makeHostNetworkPolicy());
        return spec;
    }

    private HostNetworkPolicy makeHostNetworkPolicy()
    {
        HostNetworkPolicy policy = new HostNetworkPolicy();
        policy.setHostNetworkSecurityPolicy(makeHostNetworkSecurityPolicy());
        return policy;
    }

    private HostNetworkSecurityPolicy makeHostNetworkSecurityPolicy()
    {
        HostNetworkSecurityPolicy policy = new HostNetworkSecurityPolicy();
        policy.setAllowPromiscuous(true);
        return policy;
    }

    private HostVirtualNic makeHostVirtualNic()
    {
        HostVirtualNic nic = new HostVirtualNic();
        nic.setDevice("device-1");
        nic.setPort("1234");
        nic.setPortGroup("portgroup-1");
        nic.setHostVirtualNicSpec(makeHostVirtualNicSpec());
        return nic;
    }

    private HostVirtualNicSpec makeHostVirtualNicSpec()
    {
        HostVirtualNicSpec spec = new HostVirtualNicSpec();
        spec.setMac("0E-02-26-0E-D1-31");
        spec.setHostIpConfig(makeHostIpConfig());
        spec.setDistributedVirtualSwicthPortConnection(makeDistributedVirtualSwicthPortConnection());
        return spec;
    }

    private DistributedVirtualSwicthPortConnection makeDistributedVirtualSwicthPortConnection()
    {
        DistributedVirtualSwicthPortConnection connection = new DistributedVirtualSwicthPortConnection();
        connection.setPortGroupId("portgroup-id-1");
        connection.setPortKey("portkey-1");
        return connection;
    }

    private HostIpConfig makeHostIpConfig()
    {
        HostIpConfig config = new HostIpConfig();
        config.setDhcp(true);
        config.setIpAddress("4.3.2.1");
        config.setSubnetMask("255.255.255.0");
        return config;
    }

    private PhysicalNic makePhysicalNic()
    {
        PhysicalNic nic = new PhysicalNic();
        nic.setDevice("device-1");
        nic.setDriver("driver-1");
        nic.setMac("0E-02-26-0E-D1-30");
        nic.setPci("pci-1");
        return nic;
    }

    private HostIpRouteConfig makeHostIpRouteConfig()
    {
        HostIpRouteConfig config = new HostIpRouteConfig();
        config.setDefaultGateway("10.1.2.3");
        config.setDefaultGatewayDevice("default-gateway-device");
        config.setIpV6DefaultGateway("fd81:154e:a529:7680");
        config.setIpV6DefaultGatewayDevice("default-gateway-ipv6-device");
        return config;
    }

    private HostDnsConfig makeHostDnsConfig()
    {
        HostDnsConfig config = new HostDnsConfig();
        config.setDhcp(true);
        config.setDomainName("example.com");
        config.setHostName("host-1");
        config.setIpAddresses(Collections.singletonList("1.2.3.4"));
        config.setSearchDomains(Collections.singletonList("search-domain-1"));
        return config;
    }

    private HostDateTimeInfo makeHostDateTimeInfo()
    {
        HostDateTimeInfo info = new HostDateTimeInfo();
        info.setNtpServers(Collections.singletonList("ntp-server-1"));
        return info;
    }

}