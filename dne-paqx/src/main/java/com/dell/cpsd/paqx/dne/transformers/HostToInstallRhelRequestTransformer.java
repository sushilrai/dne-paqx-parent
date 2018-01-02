package com.dell.cpsd.paqx.dne.transformers;

import com.dell.cpsd.BootImageNetworkAddressV4;
import com.dell.cpsd.BootImageNetworkDevice;

import com.dell.cpsd.RhelInstallationInfo;
import com.dell.cpsd.includes.BondedNetworkDevice;
import com.dell.cpsd.includes.BondVlanInterface;
import com.dell.cpsd.paqx.dne.domain.vcenter.Host;
import com.dell.cpsd.paqx.dne.domain.vcenter.HostDnsConfig;
import com.dell.cpsd.paqx.dne.repository.DataServiceRepository;
import com.dell.cpsd.paqx.dne.service.model.IpV4Configuration;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.persistence.NoResultException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Install RHEL Request Info Transformer class.
 * <p/>
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries. All Rights Reserved. Dell EMC Confidential/Proprietary Information
 * <p/>
 *
 * @version 1.0
 * @since 1.0
 */
@Component
public class HostToInstallRhelRequestTransformer
{
    private static final Logger LOG             = LoggerFactory.getLogger(HostToInstallRhelRequestTransformer.class);
    private static final String DELL_NODE_KARGS = "netdevice=em1";
    private final DataServiceRepository dataServiceRepository;

    @Value("${rackhd.rhel.install.repo.url}")
    private String rackhdRhelRepoUrl;

    @Value("${rackhd.rhel.install.repo.version}")
    private String rackhdRhelRepoVersion;

    public HostToInstallRhelRequestTransformer(final DataServiceRepository dataServiceRepository)
    {
        this.dataServiceRepository = dataServiceRepository;
    }

    public RhelInstallationInfo transformInstallRhelData(final String rhelManagementHostName, final String symphonyUuid,
            final IpV4Configuration ipv4Configuration) throws IllegalArgumentException
    {
        if (rhelManagementHostName == null)
        {
            LOG.error("Hostname is null");
            throw new IllegalArgumentException("Hostname is null");
        }

        if (symphonyUuid == null)
        {
            LOG.error("Symphony Id is null");
            throw new IllegalArgumentException("Symphony Id is null");
        }

        /* Assuming new node needs to be added to a ESXi based ScaleIO environment
        * Will reuse infrastructure defaults from vCenter hosts */
        Host host;

        try
        {
            host = dataServiceRepository.getExistingVCenterHost();
        }
        catch (NoResultException e)
        {
            LOG.error("No Host found");
            throw new IllegalArgumentException("No Host found");
        }

        return buildRhelInstallData(host, symphonyUuid, ipv4Configuration, rhelManagementHostName);
    }

    protected RhelInstallationInfo buildRhelInstallData(final Host host,
                                                        final String symphonyUuid,
                                                        final IpV4Configuration ipv4Configuration,
                                                        final String rhelManagementHostName)
    {
        final RhelInstallationInfo rhelInstallationInfo = new RhelInstallationInfo();

        rhelInstallationInfo.setRepo(rackhdRhelRepoUrl);

        rhelInstallationInfo.setIdentifier(symphonyUuid);

        //Specific to Dell Node
        rhelInstallationInfo.setKargs(DELL_NODE_KARGS);
        rhelInstallationInfo.setVersion(rackhdRhelRepoVersion);

        // Based on any existing host in the vcenter
        transformHostDnsConfig(rhelInstallationInfo, host.getHostDnsConfig(), rhelManagementHostName);

        /* Will configure the data networks once we finalize the input parameters */
        //transformNetworkDevices(rhelInstallationInfo, ipv4Configuration);
        transformBondDevice(rhelInstallationInfo, ipv4Configuration);
        transformPackages(rhelInstallationInfo);
        transformDisableServices(rhelInstallationInfo);

        return rhelInstallationInfo;
    }

    protected void transformHostDnsConfig(final RhelInstallationInfo rhelInstallationInfo,
                                          final HostDnsConfig hostDnsConfig,
                                          final String rhelManagementHostName)

    {
        if (hostDnsConfig != null)
        {
            rhelInstallationInfo.setHostname(rhelManagementHostName);

            final List<String> searchDomains = hostDnsConfig.getSearchDomains();

            if (CollectionUtils.isNotEmpty(searchDomains))
            {
                rhelInstallationInfo.setDomain(searchDomains.get(0));
            }
            else
            {
                rhelInstallationInfo.setDomain(hostDnsConfig.getDomainName());
            }

            rhelInstallationInfo.setDnsServers(hostDnsConfig.getDnsConfigIPs());
        }
    }

    protected void transformNetworkDevices(final RhelInstallationInfo rhelInstallationInfo,
                                           final IpV4Configuration ipv4Configuration)

    {
        final BootImageNetworkDevice bootImageNetworkDevice1 = new BootImageNetworkDevice();
        bootImageNetworkDevice1.setDevice("p1p1");

        final BootImageNetworkAddressV4 bootImageNetworkAddressV4 = new BootImageNetworkAddressV4();
        bootImageNetworkAddressV4.setIpAddr(ipv4Configuration.getStorageOnlyManagementIpAddress());
        bootImageNetworkAddressV4.setGateway(ipv4Configuration.getStorageOnlyManagementGateway());
        bootImageNetworkAddressV4.setNetmask(ipv4Configuration.getStorageOnlyManagementNetworkMask());
        bootImageNetworkAddressV4.setMtu(9000);
        bootImageNetworkDevice1.setBootImageNetworkAddressV4(bootImageNetworkAddressV4);

        rhelInstallationInfo
                .setNetworkDevices(Arrays.asList(bootImageNetworkDevice1));

    }

    protected void transformBondDevice(final RhelInstallationInfo rhelInstallationInfo,
                                           final IpV4Configuration ipv4Configuration)

    {
        final BondedNetworkDevice bondInterface = new BondedNetworkDevice();
        final BondVlanInterface bondVlanInterface = new BondVlanInterface();
        bondInterface.setNics(Arrays.asList("em2", "p1p2"));
        bondInterface.setDevice("bond0");

        final BootImageNetworkAddressV4 bootImageNetworkAddressV4 = new BootImageNetworkAddressV4();
        bootImageNetworkAddressV4.setIpAddr(ipv4Configuration.getStorageOnlyManagementIpAddress());
        bootImageNetworkAddressV4.setGateway(ipv4Configuration.getStorageOnlyManagementGateway());
        bootImageNetworkAddressV4.setNetmask(ipv4Configuration.getStorageOnlyManagementNetworkMask());

        bondVlanInterface.setVlanid(ipv4Configuration.getStorageOnlyManagementVlan());
        bondVlanInterface.setBootImageNetworkAddressV4(bootImageNetworkAddressV4);
        bondInterface.setBondVlanInterface(bondVlanInterface);

        rhelInstallationInfo.setBonds(Arrays.asList(bondInterface));
    }

    protected List<BigDecimal> transformNetworkDeviceVlanId()
    {

        final String vlanIdVmk0 = dataServiceRepository.getVlanIdVmk0();

        if (vlanIdVmk0 != null)
        {
            return new ArrayList<>(Collections.singletonList(new BigDecimal(vlanIdVmk0)));
        }

        return null;
    }

    protected void transformPackages(final RhelInstallationInfo rhelInstallationInfo) {
        rhelInstallationInfo.setPackages(Arrays.asList("@^infrastructure-server-environment"));
    }

    protected void transformDisableServices(final RhelInstallationInfo rhelInstallationInfo) {
        rhelInstallationInfo.setDisableServices(Arrays.asList("kdump," +
                "firewalld",
                "NetworkManager"));
    }
}
