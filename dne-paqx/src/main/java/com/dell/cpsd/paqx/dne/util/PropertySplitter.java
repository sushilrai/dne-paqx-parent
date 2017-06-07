package com.dell.cpsd.paqx.dne.util;

import com.google.common.base.Splitter;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * <p>
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries.  All Rights Reserved.
 * Dell EMC Confidential/Proprietary Information
 * </p>
 */
@Component("PropertySplitter")
public class PropertySplitter {
    public Map<String, String> map(String property) {
        return this.map(property, ",");
    }

    private Map<String, String> map(String property, String splitter) {
        return Splitter.on(splitter).omitEmptyStrings().trimResults().withKeyValueSeparator(":").split(property);
    }
}
