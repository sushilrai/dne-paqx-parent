/**
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries.  All Rights Reserved.
 * Dell EMC Confidential/Proprietary Information
 */

package com.dell.cpsd.paqx.dne.domain;

public interface IWorkflowTaskHandler {
    boolean executeTask(Job job);
    boolean preExecute(Job job);
    boolean postExecute(Job job);
}
