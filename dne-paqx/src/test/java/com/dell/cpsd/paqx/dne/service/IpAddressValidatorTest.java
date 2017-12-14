/**
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries.  All Rights Reserved.
 * Dell EMC Confidential/Proprietary Information
 */

package com.dell.cpsd.paqx.dne.service;

import com.dell.cpsd.paqx.dne.repository.DataServiceRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class IpAddressValidatorTest
{
    @Mock
    private DataServiceRepository repository;

    private IpAddressValidator validator;

    @Before
    public void setUp() throws Exception
    {
        validator = new IpAddressValidator(repository);
    }

    @Test
    public void testIsNotIpv4Format() throws Exception
    {
        IpAddressValidator.IpAddress badIp = new IpAddressValidator.IpAddress("1234.2.3.4", "Bad Ip Address");
        IpAddressValidator.IpAddress goodIp = new IpAddressValidator.IpAddress("1.2.3.4", "Good Ip Address");

        assertTrue(validator.isNotIpv4Format(badIp));
        assertFalse(validator.isNotIpv4Format(goodIp));
    }

    @Test
    public void testIsNotInRange() throws Exception
    {
        IpAddressValidator.IpAddress subnsetMask = new IpAddressValidator.IpAddress("255.255.255.0", "Subnet Mask");

        List<IpAddressValidator.IpAddress> goodIpAddresses = new ArrayList<>();
        goodIpAddresses.add(new IpAddressValidator.IpAddress("10.10.10.20", "Good Ip Address", subnsetMask));
        goodIpAddresses.add(new IpAddressValidator.IpAddress("192.168.160.35", "Good Ip Address", subnsetMask));

        goodIpAddresses.forEach(ip -> assertFalse(validator.isNotInRange(ip)));

        List<IpAddressValidator.IpAddress> badIpAddresses = new ArrayList<>();
        badIpAddresses.add(new IpAddressValidator.IpAddress("10.10.10.255", "Bad Ip Address", subnsetMask));
        badIpAddresses.add(new IpAddressValidator.IpAddress("192.168.160.255", "Bad Ip Address", subnsetMask));

        badIpAddresses.forEach(ip -> assertTrue(validator.isNotInRange(ip)));
    }

    @Test
    public void testIsInUse() throws Exception
    {
        IpAddressValidator.IpAddress ipNotInUse = new IpAddressValidator.IpAddress("1.2.3.4", "Ip Address Not in use");
        IpAddressValidator.IpAddress ipInUse = new IpAddressValidator.IpAddress("1.2.3.5", "Ip Address in use");

        when(repository.getAllIpAddresses()).thenReturn(Collections.singletonList("1.2.3.5"));

        assertFalse(validator.isInUse(ipNotInUse));
        assertTrue(validator.isInUse(ipInUse));
    }
}