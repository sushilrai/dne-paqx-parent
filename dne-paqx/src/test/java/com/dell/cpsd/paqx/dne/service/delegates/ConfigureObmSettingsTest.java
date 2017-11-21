
/**
 * <p>
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries. All Rights Reserved. Dell EMC Confidential/Proprietary Information
 * </p>
 */

package com.dell.cpsd.paqx.dne.service.delegates;

import com.dell.cpsd.paqx.dne.service.NodeService;
import com.dell.cpsd.paqx.dne.service.delegates.model.NodeDetail;
import com.dell.cpsd.paqx.dne.service.delegates.utils.DelegateConstants;
import com.dell.cpsd.paqx.dne.service.model.DiscoveredNode;
import com.dell.cpsd.paqx.dne.service.model.IdracNetworkSettingsRequest;
import com.dell.cpsd.paqx.dne.service.model.ObmSettingsResponse;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Properties;

import static com.dell.cpsd.paqx.dne.service.delegates.utils.DelegateConstants.NODE_DETAIL;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ConfigureObmSettingsTest {

    private ConfigureObmSettings configureObmSettings;
    private String[] obmServices = new String[2];
    private NodeDetail nodeDetail;
    private ObmSettingsResponse obmSettingsResponse;

    @Mock
    private NodeService nodeService;

    @Mock
    private DelegateExecution delegateExecution;

    @Mock
    private IdracNetworkSettingsRequest idracNetworkSettingsRequest;


    @BeforeClass
    public static void configureSystemProperties() {
        System.setProperty("obm.services", "dell-wsman-obm-service, ipmi-obm-service");
    }

    @Before
    public void setUp() throws Exception
    {
        obmServices[0] = "abc";
        obmServices[1] = "xyz";
        configureObmSettings = new ConfigureObmSettings(nodeService);
        ReflectionTestUtils.setField(configureObmSettings, "obmServices", obmServices);

        obmSettingsResponse = new ObmSettingsResponse();
        obmSettingsResponse.setStatus("SUCCESS");

        nodeDetail = new NodeDetail();
        nodeDetail.setId("1");
        nodeDetail.setServiceTag("abc");
        nodeDetail.setIdracIpAddress("1");
        nodeDetail.setIdracGatewayIpAddress("1");
        nodeDetail.setIdracSubnetMask("1");

        doReturn(nodeDetail).when(delegateExecution).getVariable(NODE_DETAIL);
        when(nodeService.obmSettingsResponse(any())).thenReturn(obmSettingsResponse);
    }

    @Test
    public void testException() {
        try {
            when(nodeService.obmSettingsResponse(any())).thenThrow(new NullPointerException());
            configureObmSettings.delegateExecute(delegateExecution);
            fail("Should Not Get Here!");
        } catch (BpmnError error) {
            assertTrue(error.getErrorCode().equals(DelegateConstants.CONFIGURE_OBM_SETTINGS_FAILED));
            assertTrue(error.getMessage().contains("An Unexpected Exception occurred while attempting to Configure the Obm Settings on Node abc"));
        }
    }

    @Test
    public void testSuccess() {
        configureObmSettings.delegateExecute(delegateExecution);
    }

    @Test
    public void testFailure() {
        try {
            obmSettingsResponse.setStatus("FAIL");
            configureObmSettings.delegateExecute(delegateExecution);
            fail("Shoudl Not Get Here!");
        } catch (BpmnError error) {
            assertTrue(error.getErrorCode().equals(DelegateConstants.CONFIGURE_OBM_SETTINGS_FAILED));
            assertTrue(error.getMessage().equalsIgnoreCase("Obm Settings on Node abc were not configured."));
        }
    }

}
