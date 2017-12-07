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
import com.dell.cpsd.paqx.dne.service.delegates.utils.DelegateConstants;
import com.dell.cpsd.paqx.dne.transformers.PciPassThroughRequestTransformer;
import com.dell.cpsd.virtualization.capabilities.api.UpdatePCIPassthruSVMRequestMessage;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static com.dell.cpsd.paqx.dne.service.delegates.utils.DelegateConstants.NODE_DETAIL;
import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class UpdatePCIPassThroughTest
{
    @Mock
    private NodeService nodeService;

    @Mock
    private PciPassThroughRequestTransformer pciPassThroughRequestTransformer;

    @Mock
    private DelegateExecution delegateExecution;

    @Mock
    private DelegateRequestModel<UpdatePCIPassthruSVMRequestMessage> requestModel;

    private UpdatePCIPassThrough updatePCIPassThrough;

    @Before
    public void setUp() throws Exception
    {
        updatePCIPassThrough = new UpdatePCIPassThrough(nodeService, pciPassThroughRequestTransformer);
        NodeDetail nodeDetail = new NodeDetail("1", "service-tag");
        doReturn(nodeDetail).when(delegateExecution).getVariable(NODE_DETAIL);
    }

    @Test
    public void testTaskResponseFailureException() throws Exception
    {
        final String exceptionMsg = "request failed";

        try
        {
            when(pciPassThroughRequestTransformer.buildUpdatePciPassThroughRequest(any())).thenReturn(requestModel);
            willThrow(new TaskResponseFailureException(1, exceptionMsg)).given(nodeService).requestSetPciPassThrough(any());

            updatePCIPassThrough.delegateExecute(delegateExecution);

            fail("Expected exception to be thrown but was not");
        }
        catch (BpmnError error)
        {
            assertTrue(error.getErrorCode().equals(DelegateConstants.UPDATE_PCI_PASSTHROUGH));
            assertThat(error.getMessage(), containsString(exceptionMsg));
        }
    }

    @Test
    public void testGeneralException() throws Exception
    {
        try
        {
            when(pciPassThroughRequestTransformer.buildUpdatePciPassThroughRequest(any())).thenThrow(new NullPointerException());

            updatePCIPassThrough.delegateExecute(delegateExecution);

            fail("Expected exception to be thrown but was not");
        }
        catch (BpmnError error)
        {
            assertTrue(error.getErrorCode().equals(DelegateConstants.UPDATE_PCI_PASSTHROUGH));
            assertThat(error.getMessage(), containsString("An unexpected exception occurred attempting to request"));
        }
    }

    @Test
    public void testSuccess() throws Exception
    {
        when(pciPassThroughRequestTransformer.buildUpdatePciPassThroughRequest(any())).thenReturn(requestModel);
        final UpdatePCIPassThrough updatePCIPassThroughSpy = spy(updatePCIPassThrough);

        updatePCIPassThroughSpy.delegateExecute(delegateExecution);

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(updatePCIPassThroughSpy, times(2)).updateDelegateStatus(captor.capture());
        assertThat(captor.getValue(), containsString("was successful"));
    }
}
