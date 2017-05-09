package com.dell.cpsd.paqx.dne.domain;

/**
 * Created by madenb on 4/28/2017.
 */
public interface IWorkflowTaskHandler {
    boolean executeTask(Job job);
    boolean preExecute(Job job);
    boolean postExecute(Job job);
}
