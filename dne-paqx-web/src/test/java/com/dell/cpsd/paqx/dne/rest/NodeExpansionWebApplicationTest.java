/**
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries.  All Rights Reserved.
 * Dell EMC Confidential/Proprietary Information
 */
package com.dell.cpsd.paqx.dne.rest;

import com.dell.cpsd.paqx.dne.amqp.config.ServiceConfig;
import com.dell.cpsd.paqx.dne.domain.Job;
import com.dell.cpsd.paqx.dne.rest.controller.NodeExpansionController;
import com.dell.cpsd.paqx.dne.service.NodeService;
import com.dell.cpsd.paqx.dne.service.WorkflowService;
import com.dell.cpsd.paqx.dne.service.model.NodeExpansionRequest;
import com.dell.cpsd.paqx.dne.service.workflow.addnode.IAddNodeService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.HashMap;
import java.util.UUID;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.spy;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {TestContext.class})
@WebMvcTest(value = NodeExpansionController.class, secure = false)
public class NodeExpansionWebApplicationTest
{


    @Mock
    RabbitTemplate rabbitTemplate;

    @Mock
    NodeService nodeService;

    @Mock
    IAddNodeService addNodeService;

    @Spy
    NodeExpansionController controller;

    /*
     * The reference to the workflow service.
     */
    private WorkflowService workflowService;

    private MockMvc mockMvc;


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

        mockMvc = MockMvcBuilders
                .standaloneSetup(NodeExpansionController.class)
                //.addFilters(new CORSFilter())
                .build();
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

    @Test
    public void postInvalidNodeJobId() throws Exception {

        Mockito.when(addNodeService.findJob(Mockito.any(UUID.class))).thenReturn(null);

        UUID jobId = UUID.randomUUID();
        RequestBuilder requestBuilder = MockMvcRequestBuilders.get(
                "/nodes/" + jobId).accept(MediaType.APPLICATION_JSON);

        MvcResult result = mockMvc.perform(requestBuilder).andReturn();

        System.out.println(result.getResponse());
        String expected = "{id:Course1,name:Spring,description:10 Steps}";

        JSONAssert.assertEquals(expected, result.getResponse()
                .getContentAsString(), false);
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