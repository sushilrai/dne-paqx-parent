/**
 * This is the message code enum for the DNE PAQX web service.
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries.  All Rights Reserved.
 * Dell EMC Confidential/Proprietary Information
 */

package com.dell.cpsd.paqx.dne.rest.log;

import com.dell.cpsd.paqx.dne.rest.i18n.DnepMessageBundle;

import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public enum DnepMessageCode
{
    WORKFLOW_NOT_FOUND_E(3000,            "DNEP3000E"),
    DNEP_3001_U(3001, "DNEP3001U");

    /*
     * The path to the resource bundle
     */
    private static ResourceBundle BUNDLE = 
                ResourceBundle.getBundle(DnepMessageBundle.class.getName());

    /*
     * The error code.
     */
    private final int errorCode;

    /*
     * The message code.
     */
    private final String messageCode;

    
    /**
     * DnepMessageCode constructor
     *
     * @param   errorCode   The error code.
     * @param   messageCode The message code.
     * 
     * @since   1.0
     */
    private DnepMessageCode(int errorCode, String messageCode)
    {
        this.errorCode = errorCode;
        this.messageCode = messageCode;
    }

    
    /**
     * This returns the message code.
     *
     * @return  The message code.
     * 
     * @since   1.0
     */
    public String getMessageCode()
    {
        return this.messageCode;
    }

    
    /**
     * This returns the error code.
     *
     * @return  The error code.
     * 
     * @since   1.0
     */
    public int getErrorCode()
    {
        return this.errorCode;
    }

    
    /**
     * This returns the error text.
     *
     * @return  The error text.
     * 
     * @since   1.0
     */
    public String getErrorText()
    {
        try
        {
            return BUNDLE.getString(this.messageCode);
        }
        catch (MissingResourceException exception)
        {
            return this.messageCode;
        }
    }
    

    /**
     * This formats the  message using the array of parameters.
     *
     * @param   params  The message parameters.
     * 
     * @return  The localized message populated with the parameters.
     * 
     * @since   1.0
     */
    public String getMessageText(Object[] params)
    {
        String message = null;

        try
        {
            message = BUNDLE.getString(this.messageCode);

        }
        catch (MissingResourceException exception)
        {
            return this.messageCode;
        }

        if ((params == null) || (params.length == 0))
        {
            return message;
        }

        return MessageFormat.format(message, params);
    }

}
