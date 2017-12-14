/**
 * <p>
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries. All Rights Reserved. Dell EMC Confidential/Proprietary Information
 * </p>
 */

package com.dell.cpsd.paqx.dne.service;

import com.dell.cpsd.paqx.dne.repository.DataServiceRepository;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.net.util.SubnetUtils;
import org.apache.commons.validator.routines.InetAddressValidator;

import java.util.ArrayList;
import java.util.List;

/**
 * IP Address validator used to validate IP addresses.
 * <p>
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries. All Rights Reserved. Dell EMC Confidential/Proprietary Information
 * </p>
 *
 * @version 1.0
 * @since 1.0
 */
public class IpAddressValidator
{
    private final DataServiceRepository repository;

    private List<String> existingIpaddresses;

    public IpAddressValidator(final DataServiceRepository repository)
    {
        this.repository = repository;
        this.existingIpaddresses = new ArrayList<>();
    }

    /**
     * Check that the given value adheres to the IPv4 format.
     *
     * @param ipAddress - The IP Address to check
     * @return true if the format is not valid, true otherwise
     */
    public boolean isNotIpv4Format(final IpAddress ipAddress)
    {
        return !InetAddressValidator.getInstance().isValidInet4Address(ipAddress.getIpAddress());
    }

    /**
     * Check that the given ip address is in the subnet range.
     *
     * @param ipAddress - The ip address to check
     * @return true if the given ip address is not in range, true otherwise
     */
    public boolean isNotInRange(final IpAddress ipAddress)
    {
        SubnetUtils subnetUtils = new SubnetUtils(ipAddress.getIpAddress(), ipAddress.getSubnetMask().getIpAddress());

        SubnetUtils.SubnetInfo subnetInfo = subnetUtils.getInfo();

        return !subnetInfo.isInRange(ipAddress.getIpAddress());
    }

    /**
     * Check is the given ip address in use.
     *
     * @param ipAddress - The ip address to check
     * @return true if the given ip address is in use, false otherwise
     */
    public boolean isInUse(final IpAddress ipAddress)
    {
        if (CollectionUtils.isEmpty(existingIpaddresses))
        {
            existingIpaddresses = getExistingIpAddresses();
        }

        return existingIpaddresses.contains(ipAddress.getIpAddress());
    }

    /**
     * Get the list of existing IP Addresses as defined in the H2 database.
     *
     * @return The <code>List<String></code> of existing IP Addresses or an empty list.
     */
    private List<String> getExistingIpAddresses()
    {
        return repository.getAllIpAddresses();
    }

    public static class IpAddress
    {
        private final String    ipAddress;
        private final String    label;
        private final IpAddress subnetMask;

        public IpAddress(final String ipAddress, final String label)
        {
            this(ipAddress, label, null);
        }

        public IpAddress(final String ipAddress, final String label, final IpAddress subnetMask)
        {
            this.ipAddress = ipAddress;
            this.label = label;
            this.subnetMask = subnetMask;
        }

        public String getIpAddress()
        {
            return ipAddress;
        }

        public String getLabel()
        {
            return label;
        }

        public IpAddress getSubnetMask()
        {
            return subnetMask;
        }
    }
}
