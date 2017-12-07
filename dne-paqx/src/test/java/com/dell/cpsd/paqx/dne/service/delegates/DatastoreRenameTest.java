/**
 * <p>
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries. All Rights Reserved. Dell EMC Confidential/Proprietary Information
 * </p>
 */

package com.dell.cpsd.paqx.dne.service.delegates;

import com.dell.cpsd.paqx.dne.exception.TaskResponseFailureException;
import com.dell.cpsd.paqx.dne.service.NodeService;
import com.dell.cpsd.paqx.dne.service.delegates.model.DelegateRequestModel;
import com.dell.cpsd.paqx.dne.service.delegates.model.NodeDetail;
import com.dell.cpsd.paqx.dne.transformers.DatastoreRenameRequestTransformer;
import com.dell.cpsd.virtualization.capabilities.api.DatastoreRenameRequestMessage;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static com.dell.cpsd.paqx.dne.service.delegates.utils.DelegateConstants.DATASTORE_RENAME_FAILED;
import static com.dell.cpsd.paqx.dne.service.delegates.utils.DelegateConstants.NODE_DETAIL;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Datastore rename delegate test class
 * <p>
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries. All Rights Reserved.
 * Dell EMC Confidential/Proprietary Information
 * </p>
 *
 * @version 1.0
 * @since 1.0
 */
@RunWith(MockitoJUnitRunner.class)
public class DatastoreRenameTest
{
    @Mock
    private NodeService nodeService;

    @Mock
    private DatastoreRenameRequestTransformer requestTransformer;

    @Mock
    private DelegateExecution delegateExecution;

    @Mock
    private DelegateRequestModel<DatastoreRenameRequestMessage> requestModel;

    private DatastoreRename delegate;
    private final String serviceTag    = "service-tag";
    private final String datastoreName = "DASXX";
    private final String taskMessage   = "Rename datastore for ESXi host";

    @Before
    public void setup() throws Exception
    {
        delegate = new DatastoreRename(nodeService, requestTransformer);
        NodeDetail nodeDetail = new NodeDetail("1", serviceTag);
        doReturn(nodeDetail).when(delegateExecution).getVariable(NODE_DETAIL);
    }

    @Test
    public void unknownExceptionThrownResultsInBpmnError() throws Exception
    {
        final String errorMessage = "Illegal state exception";
        when(requestTransformer.buildDatastoreRenameRequest(delegateExecution)).thenThrow(new IllegalStateException(errorMessage));
        final DatastoreRename spy = spy(delegate);

        try
        {
            spy.delegateExecute(delegateExecution);
        }
        catch (BpmnError error)
        {
            assertThat(error.getMessage(), containsString("An unexpected exception occurred attempting to Rename Datastore for ESXi Host on Node service-tag. Reason: Illegal state exception"));
            assertTrue(error.getErrorCode().equals(DATASTORE_RENAME_FAILED));
        }

        verify(spy).updateDelegateStatus(
                "Attempting Rename Datastore for ESXi Host on Node service-tag.");
    }

    @Test
    public void taskResponseFailureExceptionThrownDueToServiceTimeoutOrExecution() throws Exception
    {
        final DatastoreRenameRequestMessage mockRequestMessage = mock(DatastoreRenameRequestMessage.class);
        final String errorMessage = "Service timeout";
        when(requestModel.getRequestMessage()).thenReturn(mockRequestMessage);
        when(requestTransformer.buildDatastoreRenameRequest(delegateExecution)).thenReturn(requestModel);
        doThrow(new TaskResponseFailureException(1, errorMessage)).when(nodeService).requestDatastoreRename(mockRequestMessage);
        final DatastoreRename spy = spy(delegate);

        try
        {
            spy.delegateExecute(delegateExecution);
        }
        catch (BpmnError error)
        {
            assertThat(error.getMessage(), containsString("An unexpected exception occurred attempting to Rename Datastore for ESXi Host on Node service-tag. Reason: Service timeout"));
            assertTrue(error.getErrorCode().equals(DATASTORE_RENAME_FAILED));
        }

        verify(spy).updateDelegateStatus("Attempting Rename Datastore for ESXi Host on Node service-tag.");
    }

    @Test
    public void datastoreRenameSuccessUpdatesTheDelegateStatus() throws Exception
    {
        final DatastoreRenameRequestMessage mockRequestMessage = mock(DatastoreRenameRequestMessage.class);

        when(requestModel.getRequestMessage()).thenReturn(mockRequestMessage);
        when(requestTransformer.buildDatastoreRenameRequest(delegateExecution)).thenReturn(requestModel);
        when(nodeService.requestDatastoreRename(mockRequestMessage)).thenReturn(datastoreName);
        final DatastoreRename spy = spy(delegate);

        spy.delegateExecute(delegateExecution);

        verify(spy).updateDelegateStatus("Attempting Rename Datastore for ESXi Host on Node service-tag.");
        final ArgumentCaptor<String> setVariableCaptor = ArgumentCaptor.forClass(String.class);
        verify(delegateExecution).setVariable(anyString(), setVariableCaptor.capture());
        assertEquals(datastoreName, setVariableCaptor.getValue());
    }
}