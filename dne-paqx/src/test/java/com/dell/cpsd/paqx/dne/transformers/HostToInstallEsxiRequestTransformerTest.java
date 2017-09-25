/**
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries.  All Rights Reserved.
 * Dell EMC Confidential/Proprietary Information
 */

package com.dell.cpsd.paqx.dne.transformers;

import com.dell.cpsd.EsxiInstallationInfo;
import com.dell.cpsd.paqx.dne.domain.vcenter.Host;
import com.dell.cpsd.paqx.dne.domain.vcenter.HostDnsConfig;
import com.dell.cpsd.paqx.dne.domain.vcenter.HostIpRouteConfig;
import com.dell.cpsd.paqx.dne.repository.DataServiceRepository;
import com.dell.cpsd.paqx.dne.service.model.IpV4Configuration;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.persistence.NoResultException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

/**
 * The test class for testing the actual HostToInstallEsxiRequestTransformer class.
 * <p>
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries.  All Rights Reserved.
 * Dell EMC Confidential/Proprietary Information
 * </p>
 *
 * @version 1.0
 * @since 1.0
 */
@RunWith(MockitoJUnitRunner.class)
public class HostToInstallEsxiRequestTransformerTest
{
    @Mock
    private DataServiceRepository dataServiceRepository;

    @Mock
    private Host host;

    @Mock
    private HostDnsConfig hostDnsConfig;

    @Mock
    private HostIpRouteConfig hostIpRouteConfig;

    private HostToInstallEsxiRequestTransformer transformer;
    private String                              esxiManagementHostName;
    private String                              esxiManagementGateway;
    private String                              esxiManagementIpAddress;
    private String                              esxiManagementNetworkMask;
    private String                              symphonyUuid;
    private String                              vlanId;
    private IpV4Configuration                   ipv4Configuration;
    private List<String>                        ntpServers;
    private List<String>                        searchDomains;
    private List<String>                        dnsConfigIPs;

    @Before
    public void setUp() throws Exception
    {
        this.transformer = new HostToInstallEsxiRequestTransformer(this.dataServiceRepository);
        this.esxiManagementHostName = "fpr1-h17";
        this.esxiManagementGateway = "1.1.1.1";
        this.esxiManagementIpAddress = "1.2.3.4";
        this.esxiManagementNetworkMask = "255.255.255.224";
        this.symphonyUuid = UUID.randomUUID().toString();
        this.vlanId = "1628";
        this.ipv4Configuration = new IpV4Configuration();
        this.ipv4Configuration.setEsxiManagementNetworkMask(this.esxiManagementNetworkMask);
        this.ipv4Configuration.setEsxiManagementIpAddress(this.esxiManagementIpAddress);
        this.ipv4Configuration.setEsxiManagementGateway(this.esxiManagementGateway);
        this.ntpServers = new ArrayList<>();
        this.ntpServers.add("2.2.2.2");
        this.ntpServers.add("3.3.3.3");
        this.searchDomains = new ArrayList<>();
        this.searchDomains.add("example.com");
        this.searchDomains.add("foo.bar.com");
        this.dnsConfigIPs = new ArrayList<>();
        this.dnsConfigIPs.add("8.8.8.8");
        this.dnsConfigIPs.add("9.9.9.9");
    }

    @Test
    public void transformInstallEsxiData_should_successfully_transform_install_esxi_data() throws Exception
    {
        when(this.dataServiceRepository.getExistingVCenterHost()).thenReturn(this.host);
        when(this.host.getHostDnsConfig()).thenReturn(this.hostDnsConfig);
        when(this.host.getHostIpRouteConfig()).thenReturn(this.hostIpRouteConfig);
        when(this.host.getNtpServers()).thenReturn(this.ntpServers);
        when(this.hostDnsConfig.getSearchDomains()).thenReturn(this.searchDomains);
        when(this.hostDnsConfig.getDnsConfigIPs()).thenReturn(this.dnsConfigIPs);
        when(this.dataServiceRepository.getVlanIdVmk0()).thenReturn(this.vlanId);

        EsxiInstallationInfo result = this.transformer
                .transformInstallEsxiData(this.esxiManagementHostName, this.symphonyUuid, this.ipv4Configuration);

        assertNotNull(result);
        assertSame(result.getDnsServers(), this.dnsConfigIPs);
        assertSame(result.getDomain(), this.searchDomains.get(0));
        assertSame(result.getHostname(), this.esxiManagementHostName);
        assertSame(result.getNtpServers(), this.ntpServers);
        // TODO verify logical build guide specific configuration...
        assertThat(result.getKargs(), containsString("vmnic0"));
        assertSame(result.getNetworkDevices().get(0).getDevice(), "vmnic1");
        assertSame(result.getNetworkDevices().get(0).getEsxSwitchName(), "vSwitch0");
        assertSame(result.getNetworkDevices().get(1).getDevice(), "vmnic0");
        assertSame(result.getNetworkDevices().get(1).getEsxSwitchName(), "vSwitch1");
        assertSame(result.getSwitchDevices().get(0).getSwitchName(), "vSwitch0");
        assertSame(result.getSwitchDevices().get(0).getUplinks().get(0), "vmnic1");
        assertSame(result.getSwitchDevices().get(1).getSwitchName(), "vSwitch1");
        assertSame(result.getSwitchDevices().get(1).getUplinks().get(0), "vmnic0");
    }

    @Test
    public void transformInstallEsxiData_should_throw_exception_when_the_host_name_is_null() throws Exception
    {
        try
        {
            EsxiInstallationInfo result = this.transformer.transformInstallEsxiData(null, this.symphonyUuid, this.ipv4Configuration);
            fail("Expected exception to be thrown but wasn't");
        }
        catch (Exception ex)
        {
            assertThat(ex.getMessage().toLowerCase(), containsString("hostname is null"));
        }
    }

    @Test
    public void transformInstallEsxiData_should_throw_exception_when_the_symphonyuuid_is_null() throws Exception
    {
        try
        {
            EsxiInstallationInfo result = this.transformer
                    .transformInstallEsxiData(this.esxiManagementHostName, null, this.ipv4Configuration);
            fail("Expected exception to be thrown but wasn't");
        }
        catch (Exception ex)
        {
            assertThat(ex.getMessage().toLowerCase(), containsString("id is null"));
        }
    }

    @Test
    public void transformInstallEsxiData_should_throw_exception_when_no_vcenter_host_found() throws Exception
    {
        doThrow(new NoResultException("something_happened")).when(this.dataServiceRepository).getExistingVCenterHost();

        try
        {
            EsxiInstallationInfo result = this.transformer
                    .transformInstallEsxiData(this.esxiManagementHostName, this.symphonyUuid, this.ipv4Configuration);
            fail("Expected exception to be thrown but wasn't");
        }
        catch (Exception ex)
        {
            assertThat(ex.getMessage().toLowerCase(), containsString("no host found"));
        }
    }

}