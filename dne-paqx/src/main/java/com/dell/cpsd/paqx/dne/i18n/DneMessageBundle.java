/**
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries.  All Rights Reserved.
 * Dell EMC Confidential/Proprietary Information
 */

package com.dell.cpsd.paqx.dne.i18n;

import java.util.ListResourceBundle;

public class DneMessageBundle extends ListResourceBundle
{
    /*
     * The content of this message resource bundle.
     */
    private static final Object[][] CONTENTS = {
            {"DNEP1000E", "DNEP1000E Error. Reason [{0}]"},
            {"DNEP1001W", "DNEP1001W Warning. Reason [{0}]"},
        };

    /**
     * HDCRMessageBundle constructor.
     *
     * @since   1.0
     */
    public DneMessageBundle()
    {
        super();
    }

    /**
     * This returns the messages for this resource bundle.
     *
     * @return  The messages for this resource bundle.
     * 
     * @since   1.0
     */
    @Override
    protected Object[][] getContents()
    {
        return CONTENTS;
    }
}
