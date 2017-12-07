/**
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries.  All Rights Reserved.
 * Dell EMC Confidential/Proprietary Information
 **/

package com.dell.cpsd.paqx.dne.service.delegates.request;

import com.dell.cpsd.ConfigurePxeBootRequestMessage;
import com.dell.cpsd.paqx.dne.amqp.callback.AsynchronousNodeServiceCallback;
import com.dell.cpsd.paqx.dne.service.AsynchronousNodeService;
import com.dell.cpsd.paqx.dne.service.delegates.model.DelegateRequestModel;
import com.dell.cpsd.paqx.dne.service.delegates.model.NodeDetail;
import com.dell.cpsd.paqx.dne.service.delegates.utils.DelegateConstants;
import com.dell.cpsd.paqx.dne.transformers.ConfigurePxeBootRequestTransformer;
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
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Test class for send disable PXE boot request
 * <p>
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries. All Rights Reserved.
 * Dell EMC Confidential/Proprietary Information
 * </p>
 *
 * @version 1.0
 * @since 1.0
 */
@RunWith(MockitoJUnitRunner.class)
public class SendDisablePxeBootRequestTest
{
    @Mock
    private AsynchronousNodeService asynchronousNodeService;

    @Mock
    private ConfigurePxeBootRequestTransformer requestTransformer;

    @Mock
    private DelegateExecution delegateExecution;

    @Mock
    private DelegateRequestModel<ConfigurePxeBootRequestMessage> requestModel;

    private SendDisablePxeBootRequest      sendDisablePxeBootRequest;
    private ConfigurePxeBootRequestMessage requestMessage;

    @Before
    public void setup() throws Exception
    {
        sendDisablePxeBootRequest = new SendDisablePxeBootRequest(asynchronousNodeService, requestTransformer);
        requestMessage = new ConfigurePxeBootRequestMessage();
        NodeDetail nodeDetail = new NodeDetail("1", "service-tag");
        doReturn(nodeDetail).when(delegateExecution).getVariable(NODE_DETAIL);
    }

    @Test
    public void exceptionThrownWhileBuildingRequestMessageThrowsBpmnError() throws Exception
    {
        final String errorMessage = "Something bad happened";
        when(requestTransformer.buildConfigurePxeBootRequest(delegateExecution)).thenThrow(new IllegalStateException(errorMessage));
        try
        {
            sendDisablePxeBootRequest.delegateExecute(delegateExecution);
        }
        catch (BpmnError error)
        {
            assertTrue(error.getErrorCode().equals(DelegateConstants.CONFIGURE_PXE_FAILED));
            assertThat(error.getMessage(), containsString("An Unexpected Exception occurred attempting to request"));
            assertThat(error.getMessage(), containsString(errorMessage));
        }
    }

    @Test
    public void exceptionThrownResultsInNullServiceCallBackResultsInBpmnError() throws Exception
    {
        when(requestTransformer.buildConfigurePxeBootRequest(delegateExecution)).thenReturn(requestModel);
        try
        {
            sendDisablePxeBootRequest.delegateExecute(delegateExecution);
        }
        catch (BpmnError error)
        {
            assertTrue(error.getErrorCode().equals(DelegateConstants.CONFIGURE_PXE_FAILED));
            assertThat(error.getMessage(), containsString("Send Disable PXE Boot on Node service-tag failed."));
        }
    }

    @Test
    public void configurePxeBootSuccessful() throws Exception
    {
        final AsynchronousNodeServiceCallback mockRequestCallback = mock(AsynchronousNodeServiceCallback.class);
        when(requestModel.getRequestMessage()).thenReturn(requestMessage);
        when(delegateExecution.getProcessInstanceId()).thenReturn("123");
        when(requestTransformer.buildConfigurePxeBootRequest(delegateExecution)).thenReturn(requestModel);
        doReturn(mockRequestCallback).when(asynchronousNodeService)
                .sendConfigurePxeBootRequest("123", "receiveDisablePxeBootResponse", "disablePxeResponseReceived", requestMessage);

        final SendDisablePxeBootRequest spy = spy(sendDisablePxeBootRequest);

        spy.delegateExecute(delegateExecution);

        final ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(spy, times(2)).updateDelegateStatus(captor.capture());

        assertThat(captor.getValue(), containsString("Send Disable PXE Boot on Node service-tag was successful."));
    }
}
