/**
 * <p>
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries. All Rights Reserved. Dell EMC Confidential/Proprietary Information
 * </p>
 */

package com.dell.cpsd.paqx.dne.service.delegates;

import com.dell.cpsd.paqx.dne.service.NodeService;
import com.dell.cpsd.paqx.dne.service.delegates.model.DelegateRequestModel;
import com.dell.cpsd.paqx.dne.service.delegates.model.NodeDetail;
import com.dell.cpsd.paqx.dne.service.delegates.utils.DelegateConstants;
import com.dell.cpsd.paqx.dne.transformers.DatastoreRenameRequestTransformer;
import com.dell.cpsd.virtualization.capabilities.api.DatastoreRenameRequestMessage;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import static com.dell.cpsd.paqx.dne.service.delegates.utils.DelegateConstants.DATASTORE_RENAME_FAILED;
import static com.dell.cpsd.paqx.dne.service.delegates.utils.DelegateConstants.NODE_DETAIL;

@Component
@Scope("prototype")
@Qualifier("datastoreRename")
public class DatastoreRename extends BaseWorkflowDelegate
{
    /**
     * The logger instance
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(DatastoreRename.class);

    /*
     * The <code>NodeService</code> instance
     */
    private final NodeService                       nodeService;
    private final DatastoreRenameRequestTransformer datastoreRenameRequestTransformer;

    public DatastoreRename(final NodeService nodeService, final DatastoreRenameRequestTransformer datastoreRenameRequestTransformer)
    {
        super(LOGGER, "Rename Datastore for ESXi Host");
        this.nodeService = nodeService;
        this.datastoreRenameRequestTransformer = datastoreRenameRequestTransformer;
    }

    @Override
    public void delegateExecute(final DelegateExecution delegateExecution)
    {
        NodeDetail nodeDetail = (NodeDetail) delegateExecution.getVariable(NODE_DETAIL);
        updateDelegateStatus("Attempting " + this.taskName + " on Node " + nodeDetail.getServiceTag() + ".");
        try
        {
            final DelegateRequestModel<DatastoreRenameRequestMessage> delegateRequestModel = datastoreRenameRequestTransformer
                    .buildDatastoreRenameRequest(delegateExecution);
            final String newDatastoreName = this.nodeService.requestDatastoreRename(delegateRequestModel.getRequestMessage());

            delegateExecution.setVariable(DelegateConstants.DATASTORE_NAME, newDatastoreName);
            final String returnMessage = taskName + " on Node " + nodeDetail.getServiceTag() + " was successful.";
            updateDelegateStatus(returnMessage);
        }
        catch (Exception ex)
        {
            String errorMessage = "An unexpected exception occurred attempting to " + taskName + " on Node " + nodeDetail.getServiceTag() + ". Reason: ";
            updateDelegateStatus(errorMessage, ex );
            throw new BpmnError(DATASTORE_RENAME_FAILED, errorMessage + ex.getMessage());
        }
    }
}
