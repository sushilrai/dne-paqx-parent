/**
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries.  All Rights Reserved.
 * Dell EMC Confidential/Proprietary Information
 */

package com.dell.cpsd.paqx.dne.rest.model;

public class AboutInfo {
    private String message;

    public AboutInfo(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}