/**
 * This is the resource bundle for DNE PAQX web service.
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries.  All Rights Reserved.
 * Dell EMC Confidential/Proprietary Information
 */

package com.dell.cpsd.paqx.dne.rest.i18n;

import java.util.ListResourceBundle;

public class DnepMessageBundle extends ListResourceBundle
{
    /*
     * The content of this message resource bundle.
     */
    private static final Object[][] CONTENTS = {
        {"DNEP3000E", "DNEP3000E No workflow with identifier [{0}] was found."},
        {"DNEP3001U", "DNEP3001U"},
    };

    
    /**
     * DnepMessageBundle constructor.
     *
     * @since   1.0
     */
    public DnepMessageBundle()
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
