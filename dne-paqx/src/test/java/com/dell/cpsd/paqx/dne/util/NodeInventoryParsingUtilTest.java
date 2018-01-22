/**
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries.  All Rights Reserved.
 * Dell EMC Confidential/Proprietary Information
 */

package com.dell.cpsd.paqx.dne.util;

import com.dell.cpsd.paqx.dne.domain.node.DiscoveredNodeInfo;
import com.dell.cpsd.service.engineering.standards.Device;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class NodeInventoryParsingUtilTest
{
    private static String jsonString;

    @BeforeClass
    public static void setUpNodeInventory() throws IOException
    {
        //read json string from file
        jsonString = new String(Files.readAllBytes(Paths.get("src/test/resources/node_inventory.json")), StandardCharsets.UTF_8);
    }

    @Test
    public void testParseNewDevices()
    {
        List<Device> newDevices = NodeInventoryParsingUtil.parseNewDevices(jsonString);
        Assert.assertNotNull(newDevices);
        Assert.assertEquals(21, newDevices.size());
    }

    @Test
    public void testParseDiscoveredNodeInfoSuccess()
    {
        DiscoveredNodeInfo discoveredNodeInfo = NodeInventoryParsingUtil.parseDiscoveredNodeInfo(jsonString, "123456789abc");
        Assert.assertNotNull(discoveredNodeInfo);
    }

    @Test
    public void testParseDiscoveredNodeInfoException()
    {
        DiscoveredNodeInfo discoveredNodeInfo = NodeInventoryParsingUtil.parseDiscoveredNodeInfo("not a json string", "123456789abc");
        Assert.assertNull(discoveredNodeInfo);
    }
}
