/**
 * <p>
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries. All Rights Reserved. Dell EMC Confidential/Proprietary Information
 * </p>
 */

package com.dell.cpsd.paqx.dne.service.task.handler;

import com.dell.cpsd.paqx.dne.domain.Job;
import com.dell.cpsd.paqx.dne.service.model.Status;
import com.dell.cpsd.paqx.dne.service.model.TaskResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries. All Rights Reserved. Dell EMC Confidential/Proprietary Information
 * </p>
 *
 * @version 1.0
 * @since 1.0
 */
public abstract class BaseTaskHandler
{
    private static final Logger LOGGER = LoggerFactory.getLogger(BaseTaskHandler.class);

    public TaskResponse initializeResponse(Job job)
    {
        TaskResponse response = new TaskResponse();
        setupResponse(job, response);
        return response;
    }

    protected void setupResponse(Job job, TaskResponse response)
    {
        response.setWorkFlowTaskName(job.getCurrentTask().getTaskName());
        response.setWorkFlowTaskStatus(Status.IN_PROGRESS);
        job.addTaskResponse(job.getStep(), response);
    }

    public boolean preExecute(Job job)
    {
        LOGGER.info("preExecute: " + job.getCurrentTask().getTaskName());
        return true;
    }

    public boolean postExecute(Job job)
    {
        LOGGER.info("postExecute: " + job.getCurrentTask().getTaskName());
        return true;
    }

    protected boolean processSuccessOrFailure(final String status, List<String> errors, final TaskResponse response, String type)
    {
        boolean returnVal = true;
        if ("SUCCESS".equalsIgnoreCase(status))
        {
            response.setResults(buildResponseResult(status, errors, type));
            response.setWorkFlowTaskStatus(Status.SUCCEEDED);
        }
        else
        {
            response.addError(errors.toString());
            returnVal = false;
        }

        return returnVal;
    }

    /*
     * This method add all the node information to the response object
     */
    private Map<String, String> buildResponseResult(String status, List<String> errors, String type)
    {
        Map<String, String> result = new HashMap<>();

        if (status != null)
        {
            result.put(type + "Status", status);
        }

        if (errors != null)
        {
            result.put(type + "ErrorsList", errors.toString());
        }

        return result;
    }
}
