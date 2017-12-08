/**
 * <p>
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries. All Rights Reserved. Dell EMC Confidential/Proprietary Information
 * </p>
 */
package com.dell.cpsd.paqx.dne.service.delegates;

import org.apache.commons.collections.CollectionUtils;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.slf4j.Logger;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static com.dell.cpsd.paqx.dne.service.delegates.utils.DelegateConstants.DELEGATE_STATUS_VARIABLE;

/**
 * Created by Amy_Reed on 8/15/2017.
 */
public abstract class BaseWorkflowDelegate implements JavaDelegate
{

    private Logger logger;
    protected String taskName;
    private List<String> delegateStatus = new CopyOnWriteArrayList<>();

    public BaseWorkflowDelegate(final Logger logger, final String taskName)
    {
        this.logger = logger;
        this.taskName = taskName;
    }

    @Override
    public void execute(final DelegateExecution delegateExecution) throws Exception
    {
        final RepositoryService repositoryService = delegateExecution.getProcessEngineServices().getRepositoryService();
        final String jobId = delegateExecution.getBusinessKey() != null ? delegateExecution.getBusinessKey() : "";
        final String processInstanceId = delegateExecution.getProcessInstanceId();
        final String workflowName = repositoryService.createProcessDefinitionQuery().processDefinitionId(delegateExecution.getProcessDefinitionId()).singleResult().getName();
        
        logger.info("$$$$$$ Starting Execution on Job Id: " + jobId + " Process Instance Id: "+ processInstanceId + " Workflow: " + workflowName + " Step: " + taskName + " $$$$$$");
        preExecute(delegateExecution);
        try
        {
            delegateExecute(delegateExecution);
        }
        catch (BpmnError bpmnError)
        {
            throw bpmnError;
        }
        catch (Exception e)
        {
            final String errorMessage = "An Unexpected Exception occurred in the workflow.";
            logger.error("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%");
            logger.error(errorMessage, e);
            logger.error("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%");
            throw e;
        }
        finally
        {
            postExecute(delegateExecution);
            logger.info("$$$$$$ Completing Execution on Job Id: " + jobId + " Process Instance Id: "+ processInstanceId + " Workflow: " + workflowName + " Step: " + taskName + " $$$$$$");            
        }
    }

    public abstract void delegateExecute(DelegateExecution delegateExecution);

    /**
     * PreExecute functionality to allow for updating status information before execution.
     * @param delegateExecution
     */
    public void preExecute(final DelegateExecution delegateExecution) {
    }

    public void postExecute(final DelegateExecution delegateExecution)
    {
        if (CollectionUtils.isNotEmpty(delegateStatus))
        {
            List<String> currentDelegateStatus = (List<String>) delegateExecution.getVariable(DELEGATE_STATUS_VARIABLE);
            if (currentDelegateStatus == null)
            {
                currentDelegateStatus = delegateStatus;
            }
            else
            {
                currentDelegateStatus.addAll(delegateStatus);
            }
            delegateExecution.setVariable(DELEGATE_STATUS_VARIABLE, currentDelegateStatus);
        }
    }

    public void updateDelegateStatus(final String statusString) {
        updateDelegateStatus(statusString, null);
    }

    public void updateDelegateStatus(final String statusString, final Exception exception) {
        if (exception != null) {
            logger.error(statusString, exception);
        } else
        {
            logger.info(statusString);
        }
        delegateStatus.add(statusString);
    }
}
