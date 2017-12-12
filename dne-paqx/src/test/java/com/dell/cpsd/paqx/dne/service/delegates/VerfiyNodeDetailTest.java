/**
 * <p>
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries. All Rights Reserved. Dell EMC Confidential/Proprietary Information
 * </p>
 */

package com.dell.cpsd.paqx.dne.service.delegates;

import com.dell.cpsd.paqx.dne.repository.DataServiceRepository;
import com.dell.cpsd.paqx.dne.service.NodeService;
import com.dell.cpsd.paqx.dne.service.delegates.model.NodeDetail;
import com.dell.cpsd.paqx.dne.service.delegates.utils.DelegateConstants;
import com.dell.cpsd.paqx.dne.service.IpAddressValidator;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class VerfiyNodeDetailTest
{
    private VerifyNodeDetail      verifyNodeDetail;
    private NodeService           nodeService;
    private DataServiceRepository repository;
    private IpAddressValidator    validator;
    private DelegateExecution     delegateExecution;
    private NodeDetail            nodeDetail;

    @Before
    public void setUp() throws Exception
    {
        nodeService = mock(NodeService.class);
        repository = mock(DataServiceRepository.class);
        validator = mock(IpAddressValidator.class);
        verifyNodeDetail = new VerifyNodeDetail(repository, validator);
        delegateExecution = mock(DelegateExecution.class);

        nodeDetail = new NodeDetail();
        nodeDetail.setServiceTag("abc");
        nodeDetail.setvMotionManagementIpAddress("1.2.3.4");
        nodeDetail.setvMotionManagementSubnetMask("255.255.255.224");
        nodeDetail.setId("abc");
        nodeDetail.setIdracIpAddress("1.2.3.4.6");
        nodeDetail.setIdracGatewayIpAddress("1.2.3.4.7");
        nodeDetail.setIdracSubnetMask("255.255.255.224");
        nodeDetail.setEsxiManagementIpAddress("1.2.3.4.8");
        nodeDetail.setEsxiManagementGatewayIpAddress("1.2.3.4.9");
        nodeDetail.setEsxiManagementSubnetMask("255.255.255.224");
        nodeDetail.setEsxiManagementHostname("abc");
        nodeDetail.setScaleIoData1SvmIpAddress("1.2.3.4.10");

        nodeDetail.setScaleIoData2SvmIpAddress("1.2.3.4.11");
        nodeDetail.setClusterName("abc");
        nodeDetail.setProtectionDomainId("abc");
        nodeDetail.setProtectionDomainName("pd-abc");

        nodeDetail.setScaleIoData1SvmSubnetMask("255.255.255.224");
        nodeDetail.setScaleIoData2SvmSubnetMask("255.255.255.224");
        nodeDetail.setScaleIoData1EsxIpAddress("1.2.3.4.12");
        nodeDetail.setScaleIoData1EsxSubnetMask("255.255.255.224");
        nodeDetail.setScaleIoData2EsxIpAddress("1.2.3.4.13");
        nodeDetail.setScaleIoData2EsxSubnetMask("255.255.255.224");
        nodeDetail.setScaleIoSvmManagementIpAddress("1.2.3.4.14");
        nodeDetail.setScaleIoSvmManagementGatewayAddress("1.2.3.4.15");
        nodeDetail.setScaleIoSvmManagementSubnetMask("255.255.255.224");
    }

    @Test
    public void testNodeDetailsNull() throws Exception
    {
        try
        {
            nodeDetail = null;
            when(delegateExecution.getVariable(DelegateConstants.NODE_DETAIL)).thenReturn(nodeDetail);
            verifyNodeDetail.delegateExecute(delegateExecution);
        }
        catch (BpmnError error)
        {
            assertTrue(error.getErrorCode().equals(DelegateConstants.VERIFY_NODE_DETAIL_FAILED));
            assertTrue(error.getMessage().contains("Node details were not found!  Please add Node details and try again."));
        }
    }

    @Test
    public void testNodeDetailsMissing() throws Exception
    {
        try
        {
            nodeDetail = new NodeDetail();
            when(delegateExecution.getVariable(DelegateConstants.NODE_DETAIL)).thenReturn(nodeDetail);
            verifyNodeDetail.delegateExecute(delegateExecution);
        }
        catch (BpmnError error)
        {
            assertTrue(error.getErrorCode().equals(DelegateConstants.VERIFY_NODE_DETAIL_FAILED));
            assertTrue(error.getMessage()
                    .contains("Node details are incomplete!  Please update Node details with the following information and try again."));
        }
    }

    @Test
    public void testNodeDetailIpAddressFormatError() throws Exception
    {
        try
        {
            when(delegateExecution.getVariable(DelegateConstants.NODE_DETAIL)).thenReturn(nodeDetail);
            when(validator.isNotIpv4Format(any())).thenReturn(true);

            verifyNodeDetail.delegateExecute(delegateExecution);
        }
        catch (BpmnError error)
        {
            assertTrue(error.getErrorCode().equals(DelegateConstants.VERIFY_NODE_DETAIL_FAILED));
            assertThat(error.getMessage(), containsString("IP Address formats not valid"));
        }
    }

    @Test
    public void testNodeDetailIpAddressDuplicatedError() throws Exception
    {
        try
        {
            when(delegateExecution.getVariable(DelegateConstants.NODE_DETAIL)).thenReturn(nodeDetail);
            nodeDetail.setEsxiManagementIpAddress("1.1.1.1");
            nodeDetail.setScaleIoSvmManagementIpAddress("1.1.1.1");

            verifyNodeDetail.delegateExecute(delegateExecution);
        }
        catch (BpmnError error)
        {
            assertTrue(error.getErrorCode().equals(DelegateConstants.VERIFY_NODE_DETAIL_FAILED));
            assertThat(error.getMessage(), containsString("IP Addresses duplicated"));
        }
    }

    @Test
    public void testNodeDetailIpAddressNotInRangeError() throws Exception
    {
        try
        {
            when(delegateExecution.getVariable(DelegateConstants.NODE_DETAIL)).thenReturn(nodeDetail);
            when(validator.isNotInRange(any())).thenReturn(true);

            verifyNodeDetail.delegateExecute(delegateExecution);
        }
        catch (BpmnError error)
        {
            assertTrue(error.getErrorCode().equals(DelegateConstants.VERIFY_NODE_DETAIL_FAILED));
            assertThat(error.getMessage(), containsString("IP Address not in subnet range"));
        }
    }

    @Test
    public void testNodeDetailIpAddressAlreadyInUseError() throws Exception
    {
        try
        {
            when(delegateExecution.getVariable(DelegateConstants.NODE_DETAIL)).thenReturn(nodeDetail);
            when(validator.isInUse(any())).thenReturn(true);

            verifyNodeDetail.delegateExecute(delegateExecution);
        }
        catch (BpmnError error)
        {
            assertTrue(error.getErrorCode().equals(DelegateConstants.VERIFY_NODE_DETAIL_FAILED));
            assertThat(error.getMessage(), containsString("IP Address already in use"));
        }
    }

    @Test
    public void testVerifiedSuccess() throws Exception
    {
        when(delegateExecution.getVariable(DelegateConstants.NODE_DETAIL)).thenReturn(nodeDetail);
        final VerifyNodeDetail verifyNodeDetailSpy = spy(new VerifyNodeDetail(repository, validator));

        verifyNodeDetailSpy.delegateExecute(delegateExecution);

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(verifyNodeDetailSpy, times(2)).updateDelegateStatus(captor.capture());
        assertThat(captor.getValue(), containsString("was successful"));
    }
}
