/**
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries.  All Rights Reserved.
 * Dell EMC Confidential/Proprietary Information
 */

package com.dell.cpsd.paqx.dne;

import com.dell.cpsd.service.engineering.standards.DeviceAssignment;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Test Util class containing common methods.
 * <p>
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries. All Rights Reserved.
 * Dell EMC Confidential/Proprietary Information
 * </p>
 *
 * @version 1.0
 * @since 1.0
 */
public class TestUtil
{
    public static Message jsonMessage(final String typeId, final String contentFileName) throws Exception
    {
        final MessageProperties properties = new MessageProperties();
        properties.setContentType("application/json");
        properties.setHeader("__TypeId__", typeId);

        URL resource = TestUtil.class.getClassLoader().getResource(contentFileName);
        if ( resource != null )
            return new Message(IOUtils.toByteArray(resource), properties);

        // else return empty message.
        return new Message(new byte[0], properties);
    }

    public static Map<String,DeviceAssignment> createDeviceAssignmentMap()
    {
        Map<String, DeviceAssignment> deviceToDeviceAssignment = new HashMap<>();
        String deviceId="dev1Id";
        String deviceSerial="dev1Serial";
        String storagePoolId="sp1Id";
        String storagePoolName="sp1Name";
        String logicalPath="/dev/sda";

        DeviceAssignment deviceAssignment = new DeviceAssignment(deviceId, deviceSerial, logicalPath, storagePoolId, storagePoolName);

        deviceToDeviceAssignment.put(deviceId, deviceAssignment);

        return deviceToDeviceAssignment;
    }
}
