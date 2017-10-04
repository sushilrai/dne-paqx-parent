/**
 * <p>
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries.  All Rights Reserved.
 * Dell EMC Confidential/Proprietary Information
 * </p>
 */


package com.dell.cpsd.paqx.dne.util;

import com.dell.cpsd.paqx.dne.domain.node.DiscoveredNodeInfo;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NodeInventoryParsingUtil {
    private static final String DMI_FIELD = "dmi";
    private static final String SOURCE_FIELD = "source";
    private static final String DATA_FIELD = "data";
    private static final String SYSTEM_INFO_FIELD = "System Information";
    private static final String SERIAL_NUM_FIELD = "Serial Number";
    private static final String PRODUCT_FIELD = "Product Name";
    private static final String FAMILY_FIELD = "Family";

    public static DiscoveredNodeInfo getDiscoveredNodeInfo(Object nodeInventory, String uuid) {
        List inventoryList = (ArrayList)nodeInventory;
        for ( Object obj: inventoryList)
        {
            Map map = (HashMap)obj;
            if ( ((String)map.get(SOURCE_FIELD)).equalsIgnoreCase(DMI_FIELD))
            {
                Map data = (HashMap)map.get(DATA_FIELD);
                Map sysInfo = ( HashMap)data.get(SYSTEM_INFO_FIELD);
                String serialNumber = (String)sysInfo.get(SERIAL_NUM_FIELD);
                String product = (String)sysInfo.get(PRODUCT_FIELD);
                String family = (String)sysInfo.get(FAMILY_FIELD);
                DiscoveredNodeInfo nodeInfo = new DiscoveredNodeInfo(product, family, product, family, serialNumber, uuid);
                return nodeInfo;
            }
        }
        return null;
    }
}
