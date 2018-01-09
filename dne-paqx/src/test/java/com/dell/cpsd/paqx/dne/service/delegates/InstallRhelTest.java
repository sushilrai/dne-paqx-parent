/**
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries.  All Rights Reserved.
 * Dell EMC Confidential/Proprietary Information
 */

package com.dell.cpsd.paqx.dne.service.delegates;

import com.dell.cpsd.paqx.dne.amqp.callback.AsynchronousNodeServiceCallback;
import com.dell.cpsd.paqx.dne.exception.TaskResponseFailureException;
import com.dell.cpsd.paqx.dne.repository.DataServiceRepository;
import com.dell.cpsd.paqx.dne.service.AsynchronousNodeService;
import com.dell.cpsd.paqx.dne.service.delegates.model.NodeDetail;
import com.dell.cpsd.paqx.dne.service.delegates.utils.DelegateConstants;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static com.dell.cpsd.paqx.dne.service.delegates.utils.DelegateConstants.INSTALL_RHEL_MESSAGE_ID;
import static com.dell.cpsd.paqx.dne.service.delegates.utils.DelegateConstants.NODE_DETAIL;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class InstallRhelTest
{
    @Mock
    private DelegateExecution delegateExecution;

    @Mock
    private AsynchronousNodeService asynchronousNodeService;

    @Mock
    private DataServiceRepository repository;

    @Mock
    private AsynchronousNodeServiceCallback<?> asynchronousNodeServiceCallback;

    private InstallRhel installRhel;
    private NodeDetail  nodeDetail;

    @Before
    public void setUp() throws Exception
    {
        installRhel = new InstallRhel(asynchronousNodeService, repository);

        doReturn(true).when(asynchronousNodeServiceCallback).isDone();

        doReturn("domain").when(this.repository).getDomainName();

        nodeDetail = new NodeDetail();
        nodeDetail.setId("1");
        nodeDetail.setIdracIpAddress("1");
        nodeDetail.setIdracGatewayIpAddress("1");
        nodeDetail.setIdracSubnetMask("1");
        nodeDetail.setServiceTag("abc");
        nodeDetail.setStorageOnlyManagementHostname("hostName");
        doReturn(nodeDetail).when(delegateExecution).getVariable(NODE_DETAIL);

        doReturn(asynchronousNodeServiceCallback).when(this.delegateExecution).getVariable(INSTALL_RHEL_MESSAGE_ID);
    }

    @Test
    public void testTaskResponseFailureException() throws Exception
    {
        final String exceptionMsg = "request failed";

        try
        {
            willThrow(new TaskResponseFailureException(1, exceptionMsg)).given(asynchronousNodeService).requestInstallRhel(any());

            installRhel.delegateExecute(delegateExecution);

            fail("Expected exception to be thrown but was not");
        }
        catch (BpmnError error)
        {
            assertTrue(error.getErrorCode().equals(DelegateConstants.INSTALL_RHEL_FAILED));
            assertThat(error.getMessage(), containsString(exceptionMsg));
        }
    }

    @Test
    public void testGeneralException() throws Exception
    {
        try
        {
            installRhel.delegateExecute(delegateExecution);
        }
        catch (BpmnError error)
        {
            assertTrue(error.getErrorCode().equals(DelegateConstants.INSTALL_RHEL_FAILED));
            assertThat(error.getMessage(), containsString("Install RHEL on Node"));
            assertThat(error.getMessage(), containsString("failed!  Reason: "));
        }
    }

    @Test
    public void testSuccess() throws Exception
    {
        final InstallRhel spy = spy(new InstallRhel(asynchronousNodeService, repository));

        doNothing().when(asynchronousNodeService).requestInstallRhel(any());
        when(delegateExecution.getVariable(DelegateConstants.INSTALL_RHEL_MESSAGE_ID)).thenReturn(asynchronousNodeServiceCallback);

        spy.delegateExecute(delegateExecution);

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(spy, times(2)).updateDelegateStatus(captor.capture());
        assertThat(captor.getValue(), CoreMatchers.containsString("was successful"));
    }

}
