/**
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries.  All Rights Reserved.
 * Dell EMC Confidential/Proprietary Information
 */
package com.dell.cpsd.paqx.dne.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.spy;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import com.dell.cpsd.paqx.dne.service.model.Step;
import org.junit.Test;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import javax.servlet.http.HttpServletRequest;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

import org.mockito.MockitoAnnotations;

import com.dell.cpsd.paqx.dne.rest.controller.NodeExpansionController;
import com.dell.cpsd.paqx.dne.service.model.NodeExpansionRequest;
import com.dell.cpsd.paqx.dne.service.model.NodeExpansionResponse;

import org.springframework.http.ResponseEntity;

import com.dell.cpsd.paqx.dne.service.WorkflowService;

import com.dell.cpsd.paqx.dne.amqp.config.ServiceConfig;

import com.dell.cpsd.paqx.dne.service.NodeService;
import org.springframework.mock.web.MockHttpServletRequest;

/**
 *
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries.  All Rights Reserved.
 * Dell EMC Confidential/Proprietary Information
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class NodeExpansionWebApplicationTest 
{
    @Mock
    RabbitTemplate rabbitTemplate;
    
    @Mock
    NodeService nodeService;

    @Spy
    NodeExpansionController controller;
    
    /*
     * The reference to the workflow service.
     */
    private WorkflowService workflowService;
    
    /**
     * This sets up the test.
     * 
     * @since   1.0
     */
    @Before
    public void setUp() throws Exception
    {
        MockitoAnnotations.initMocks(this);
        
        this.workflowService = makeWorkflowService();
        
        this.controller = spy(NodeExpansionController.class);
        
        this.controller.setNodeService(this.nodeService);
    }


    @Test
    public void verifyRequestParams(){
        NodeExpansionRequest params = new NodeExpansionRequest();

        params.setEsxiKernelIpAddress1("1.1.1.1");
        assertNotNull(params.getEsxiKernelIpAddress1());

        params.setEsxiKernelIpAddress2("1.1.1.1");
        assertNotNull(params.getEsxiKernelIpAddress2());

        params.setIdracIpAddress("1.1.1.1");
        assertNotNull(params.getIdracIpAddress());

        params.setManagementIpAddress("1.1.1.1");
        assertNotNull(params.getManagementIpAddress());

        params.setScaleIOSVMDataIpAddress1("1.1.1.1");
        assertNotNull(params.getScaleIOSVMDataIpAddress1());

        params.setScaleIOSVMDataIpAddress2("1.1.1.1");
        assertNotNull(params.getScaleIOSVMDataIpAddress2());

        params.setScaleIOSVMManagementIpAddress("1.1.1.1");
        assertNotNull(params.getScaleIOSVMManagementIpAddress());
    }
    
    
    /*
     * This returns the workflow service used for testing.
     * 
     * @return  The workflow service used for testing.
     * 
     * @since   1.0
     */
    private final WorkflowService makeWorkflowService()
    {
        final ServiceConfig serviceConfig = new ServiceConfig();

        final WorkflowService workflowService =
                                    serviceConfig.addNodeWorkflowService(new HashMap<>());

        return workflowService;
    }
}