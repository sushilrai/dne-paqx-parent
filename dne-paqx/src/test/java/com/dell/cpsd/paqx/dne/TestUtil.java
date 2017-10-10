/**
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries.  All Rights Reserved.
 * Dell EMC Confidential/Proprietary Information
 */

package com.dell.cpsd.paqx.dne;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import java.net.URL;

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
}
