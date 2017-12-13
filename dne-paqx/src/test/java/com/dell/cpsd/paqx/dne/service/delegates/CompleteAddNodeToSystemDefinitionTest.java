/**
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries.  All Rights Reserved.
 * Dell EMC Confidential/Proprietary Information
 */

package com.dell.cpsd.paqx.dne.service.delegates;

import com.dell.cpsd.paqx.dne.service.delegates.model.NodeDetail;
import com.dell.cpsd.paqx.dne.service.delegates.utils.DelegateConstants;
import com.dell.cpsd.sdk.AMQPClient;
import com.dell.cpsd.service.system.definition.api.Component;
import com.dell.cpsd.service.system.definition.api.ConvergedSystem;
import com.dell.cpsd.service.system.definition.api.Identity;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CompleteAddNodeToSystemDefinitionTest
{
    @Mock
    private AMQPClient sdkAMQPClient;

    @Mock
    private DelegateExecution delegateExecution;

    private CompleteAddNodeToSystemDefinition completeAddNodeToSystemDefinition;
    private NodeDetail                        nodeDetail;
    private ConvergedSystem                   convergedSystem;
    private Component                         component;

    @Before
    public void setUp() throws Exception
    {
        completeAddNodeToSystemDefinition = new CompleteAddNodeToSystemDefinition(sdkAMQPClient);

        nodeDetail = new NodeDetail();
        nodeDetail.setId("abc");
        nodeDetail.setServiceTag("abc");

        component = new Component();
        component.setIdentity(new Identity("SERVER", "abc", "abc", "abc", null));

        convergedSystem = new ConvergedSystem();
        convergedSystem.setUuid("abc");
        convergedSystem.setComponents(Collections.singletonList(component));
    }

    @Test
    public void testDelegateExecuteRunnableResultException() throws Exception
    {
        final String runnableResult = "AddNodeToSystemDefinitionRunnable failed";
        when(delegateExecution.getVariable(anyString())).thenReturn(nodeDetail, runnableResult);

        try
        {
            completeAddNodeToSystemDefinition.delegateExecute(delegateExecution);

            fail("Expected exception to be thrown here but was not");
        }
        catch (BpmnError error)
        {
            assertTrue(error.getErrorCode().equals(DelegateConstants.COMPLETE_ADD_NODE_TO_SYSTEM_DEFINITION_FAILED));
            assertThat(error.getMessage(), containsString(runnableResult));
        }

    }

    @Test
    public void testDelegateExecuteNoConvergedSystemsFoundException() throws Exception
    {
        when(delegateExecution.getVariable(anyString())).thenReturn(nodeDetail, DelegateConstants.SUCCEEDED);
        when(sdkAMQPClient.getConvergedSystems()).thenReturn(Collections.emptyList());

        try
        {
            completeAddNodeToSystemDefinition.delegateExecute(delegateExecution);

            fail("Expected exception to be thrown here but was not");
        }
        catch (BpmnError error)
        {
            assertTrue(error.getErrorCode().equals(DelegateConstants.COMPLETE_ADD_NODE_TO_SYSTEM_DEFINITION_FAILED));
            assertThat(error.getMessage(), containsString("failed"));
        }

    }

    @Test
    public void testDelegateExecuteDiscoveredNodeNotAddedToSystemDefinitionException() throws Exception
    {
        when(delegateExecution.getVariable(anyString())).thenReturn(nodeDetail, DelegateConstants.SUCCEEDED);
        when(sdkAMQPClient.getConvergedSystems()).thenReturn(Collections.singletonList(convergedSystem));
        when(sdkAMQPClient.getComponents(any())).thenReturn(Collections.singletonList(convergedSystem));
        component.getIdentity().setIdentifier("cba");

        try
        {
            completeAddNodeToSystemDefinition.delegateExecute(delegateExecution);

            fail("Expected exception to be thrown here but was not");
        }
        catch (BpmnError error)
        {
            assertTrue(error.getErrorCode().equals(DelegateConstants.COMPLETE_ADD_NODE_TO_SYSTEM_DEFINITION_FAILED));
            assertThat(error.getMessage(), containsString("discovered node was not added to the system definition"));
        }

    }

    @Test
    public void testDelegateExecuteSuccesses() throws Exception
    {
        when(delegateExecution.getVariable(anyString())).thenReturn(nodeDetail, DelegateConstants.SUCCEEDED);
        when(sdkAMQPClient.getConvergedSystems()).thenReturn(Collections.singletonList(convergedSystem));
        when(sdkAMQPClient.getComponents(any())).thenReturn(Collections.singletonList(convergedSystem));
        final CompleteAddNodeToSystemDefinition completeAddNodeToSystemDefinitionSpy = spy(completeAddNodeToSystemDefinition);

        completeAddNodeToSystemDefinitionSpy.delegateExecute(delegateExecution);

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(completeAddNodeToSystemDefinitionSpy, times(2)).updateDelegateStatus(captor.capture());
        assertThat(captor.getValue(), containsString("was successful"));
    }
}