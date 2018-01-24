/**
 * <p>
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries.  All Rights Reserved.
 * Dell EMC Confidential/Proprietary Information
 * </p>
 */

package com.dell.cpsd.paqx.dne.util;

import com.dell.cpsd.paqx.dne.domain.node.DiscoveredNodeInfo;
import com.dell.cpsd.service.engineering.standards.Device;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * NodeInventoryParsingUtil.
 * <p>
 * <p>
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries. All Rights Reserved. Dell EMC Confidential/Proprietary Information
 * </p>
 *
 * @version 1.0
 * @since 1.0
 */
public class NodeInventoryParsingUtil
{
    private static final Logger LOGGER = LoggerFactory.getLogger(NodeInventoryParsingUtil.class);

    private static final String DMI_FIELD          = "dmi";
    private static final String SMART_SOURCE       = "smart";
    private static final String OHAI_SOURCE       = "ohai";
    private static final String SOLID_STATE_DEVICE = "Solid State Device";
    private static final String DEVICE_PATH_BY_ID  = "/dev/disk/by-id/wwn-";

    /**
     * Given a JSON string representation of a a node's <code>NodeInventory</code>, parses the JSON for the node product,
     * family and serial number and returns this information as a <code>DiscoveredNodeInfo</code> instance.
     *
     * @param nodeInventoryJson The <code>NodeInventory</code> JSON string representation
     * @param nodeId            The node identifier
     * @return <code>DiscoveredNodeInfo</code>
     */
    public static DiscoveredNodeInfo parseDiscoveredNodeInfo(String nodeInventoryJson, String nodeId)
    {
        if (nodeInventoryJson != null)
        {
            try
            {
                DocumentContext context = JsonPath.parse(nodeInventoryJson.toLowerCase());
                int length = context.read("$.length()");
                String source;

                for (int iCount = 0; iCount < length; iCount++)
                {
                    source = context.read("$[" + iCount + "]['source']", String.class);

                    if (DMI_FIELD.equalsIgnoreCase(source))
                    {
                        String product = context.read("$[" + iCount + "]['data']['system information']['product name']", String.class);
                        // family is currently not specified in the inventory, so leave out for now...
                        //String family = context.read("$[" + iCount + "]['data']['system information']['family']", String.class);
                        String serialNumber = context
                                .read("$[" + iCount + "]['data']['system information']['serial number']", String.class);
                        return new DiscoveredNodeInfo(product, null, product, null, serialNumber, nodeId);
                    }
                }
            }
            catch (Exception ex)
            {
                LOGGER.error("Error reading system information for node with id " + nodeId, ex);
            }
        }

        return null;
    }

    /**
     * Get the device path by ID.
     *
     * @return THe device path by ID
     */
    public static String getDevicePathById()
    {
        return DEVICE_PATH_BY_ID;
    }

    /**
     * Given a JSON string representation of node inventory, parses the JSON for the for new devices.
     *
     * @param jsonString The JSON string
     * @return <code>List<Device></code>
     */
    public static List<Device> parseNewDevices(String jsonString)
    {
        List<Device> newDevices = null;
        if (jsonString != null)
        {
            newDevices = new ArrayList<>();
            DocumentContext context = JsonPath.parse(jsonString.toLowerCase());
            int length = context.read("$.length()");
            String source;
            int dataLen;
            String deviceId;
            String deviceName;
            String ssdDeviceType;
            String serialNumber;
            String devicePath = null;
            String capacity;
            for (int iCount = 0; iCount < length; iCount++)
            {
                source = context.read("$[" + iCount + "]['source']", String.class);
                if (SMART_SOURCE.equalsIgnoreCase(source))
                {
                    dataLen = context.read("$[" + iCount + "]['data'].length()");
                    for (int iDataCount = 0; iDataCount < dataLen; iDataCount++)
                    {
                        try
                        {
                            // reset device id
                            deviceId = null;
                            ssdDeviceType = context
                                    .read("$[" + iCount + "]['data'][" + iDataCount + "]['smart']['identity']['rotation rate']",
                                            String.class);
                            if (SOLID_STATE_DEVICE.equalsIgnoreCase(ssdDeviceType))
                            {
                                deviceName = context.read("$[" + iCount + "]['data'][" + iDataCount + "]['os device name']", String.class);
                                serialNumber = context
                                        .read("$[" + iCount + "]['data'][" + iDataCount + "]['smart']['identity']['serial number']",
                                                String.class);
                                capacity = context
                                        .read("$[" + iCount + "]['data'][" + iDataCount + "]['smart']['identity']['user capacity']",
                                                String.class);
                                capacity = capacity == null ? null : capacity.split("bytes")[0].replaceAll(",", "").trim();

                                // skip SATADOM-ML drives, they are not scaleio data disks
                                try
                                {
                                    deviceId = context
                                            .read("$[" + iCount + "]['data'][" + iDataCount + "]['smart']['identity']['logical unit id']",
                                                    String.class);

                                    //It has to include 0x
                                    devicePath = DEVICE_PATH_BY_ID + deviceId;

                                    // remove 0x prefix
                                    deviceId = deviceId == null ? null : deviceId.substring(2);
                                }
                                catch (PathNotFoundException e)
                                {
                                    try
                                    {
                                        deviceId = context.read("$[" + iCount + "]['data'][" + iDataCount
                                                + "]['smart']['identity']['lu wwn device id']", String.class);
                                        // remove spaces in between
                                        deviceId = deviceId == null ? null : deviceId.replaceAll("\\s", "");

                                        devicePath = DEVICE_PATH_BY_ID + deviceId;
                                    }
                                    catch (PathNotFoundException ex)
                                    {
                                        LOGGER.info("No device id found for device name: " + deviceName);
                                    }
                                }

                                LOGGER.info("Device name " + deviceName + " has device path as " + devicePath);

                                if (deviceId != null)
                                {
                                    Device newDevice = new Device(deviceId, deviceName, null, serialNumber, null, devicePath,
                                            Device.Type.SSD, capacity);
                                    newDevices.add(newDevice);
                                }
                            }
                        }
                        catch (PathNotFoundException e)
                        {
                            //Do nothing and continue
                            LOGGER.error("Could not parse node inventory field : ", e);
                        }
                    }
                    break;
                }
            }

        }

        return newDevices;
    }

    public static String parseToFetchMacAddress(String jsonString)
    {
        String macAddress = null;
        if (jsonString != null)
        {
            DocumentContext context = JsonPath.parse(jsonString.toLowerCase());
            int length = context.read("$.length()");
            String source;
            for (int iCount = 0; iCount < length; iCount++)
            {
                source = context.read("$[" + iCount + "]['source']", String.class);
                if (OHAI_SOURCE.equalsIgnoreCase(source))
                {
                    try
                    {
                        macAddress = context.read("$[" + iCount + "]['data']['macaddress']", String.class);
                        break;
                    }
                    catch (PathNotFoundException e)
                    {
                        LOGGER.error("Could not parse node inventory field : ", e);
                    }
                }
            }
        }
        return macAddress;
    }
}
