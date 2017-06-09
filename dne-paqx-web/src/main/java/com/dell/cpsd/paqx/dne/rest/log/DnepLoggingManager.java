/**
 * This is the logging manager for the DNE PAQX web service.
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries.  All Rights Reserved.
 * Dell EMC Confidential/Proprietary Information
 */

package com.dell.cpsd.paqx.dne.rest.log;

import com.dell.cpsd.common.logging.ILogger;
import com.dell.cpsd.common.logging.LoggingManager;
import com.dell.cpsd.paqx.dne.rest.i18n.DnepMessageBundle;
import java.util.ResourceBundle;

public class DnepLoggingManager
{
    /*
     * The message resource bundle used for logging.
     */
    private static ResourceBundle MESSAGE_BUNDLE = 
                ResourceBundle.getBundle(DnepMessageBundle.class.getName());

    /*
     * The logging manager.
     */
    private static LoggingManager LOGGING_MANAGER = new LoggingManager();

    
    /**
     * DnepLoggingManager constructor.
     *
     * @since   1.0
     */
    public DnepLoggingManager()
    {
        super();
    }

    
    /**
     * This returns an <code>ILogger</code> using the name of the specified
     * class as the logger name.
     *
     * @param   clazz  The <code>Class</code> used for the logger.
     * 
     * @return  The <code>ILogger</code> with the name of the class.
     * 
     * @throws  IllegalArgumentException Thrown if the class is null.
     * 
     * @since   1.0
     */
    public static ILogger getLogger(Class clazz)
    {
        return LOGGING_MANAGER.getLogger(clazz, MESSAGE_BUNDLE);
    }

    /**
     * This returns an <code>ILogger</code> using the specified name as the
     * logger name.
     *
     * @param   name   The name of the logger.
     * 
     * @return  The <code>ILogger</code> with the specified name.
     * 
     * @since   1.0
     */
    public static ILogger getLogger(String name)
    {
        return LOGGING_MANAGER.getLogger(name, MESSAGE_BUNDLE);
    }
}
