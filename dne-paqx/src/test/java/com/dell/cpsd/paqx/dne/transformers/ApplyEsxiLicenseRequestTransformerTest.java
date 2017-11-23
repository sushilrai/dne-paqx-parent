/**
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries.  All Rights Reserved.
 * Dell EMC Confidential/Proprietary Information
 **/

package com.dell.cpsd.paqx.dne.transformers;

import com.dell.cpsd.paqx.dne.service.delegates.model.DelegateRequestModel;
import com.dell.cpsd.paqx.dne.service.delegates.model.NodeDetail;
import com.dell.cpsd.paqx.dne.service.model.ComponentEndpointIds;
import com.dell.cpsd.virtualization.capabilities.api.AddEsxiHostVSphereLicenseRequest;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static com.dell.cpsd.paqx.dne.service.delegates.utils.DelegateConstants.HOSTNAME;
import static com.dell.cpsd.paqx.dne.service.delegates.utils.DelegateConstants.NODE_DETAIL;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

/**
 * Unit test class for apply ESXi host license request transformer
 * <p>
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries. All Rights Reserved.
 * Dell EMC Confidential/Proprietary Information
 * </p>
 *
 * @version 1.0
 * @since 1.0
 */
@RunWith(MockitoJUnitRunner.class)
public class ApplyEsxiLicenseRequestTransformerTest
{
    private static final String VCENTER_CUSTOMER_TYPE = "VCENTER-CUSTOMER";
    private final        String serviceTag            = "service-tag";
    @Mock
    ComponentIdsTransformer componentIdsTransformer;
    @Mock
    private DelegateExecution delegateExecution;
    @Mock
    private ComponentEndpointIds componentEndpointIds;
    @Mock
    private NodeDetail nodeDetail;
    private ApplyEsxiLicenseRequestTransformer transformer;
    private              String hostName              = "hostname-1";

    @Before
    public void setUp() throws Exception
    {
        this.transformer = new ApplyEsxiLicenseRequestTransformer(this.componentIdsTransformer);
    }

    @Test
    public void testBuildApplyEsxiLicenseRequestIsValid() throws Exception
    {
        when(this.delegateExecution.getVariable(HOSTNAME)).thenReturn(this.hostName);
        when(delegateExecution.getVariable(NODE_DETAIL)).thenReturn(nodeDetail);
        when(nodeDetail.getServiceTag()).thenReturn(serviceTag);
        when(componentIdsTransformer.getVCenterComponentEndpointIdsByEndpointType(VCENTER_CUSTOMER_TYPE))
                .thenReturn(this.componentEndpointIds);

        final DelegateRequestModel<AddEsxiHostVSphereLicenseRequest> requestModel = this.transformer
                .buildApplyEsxiLicenseRequest(this.delegateExecution);

        assertNotNull(requestModel);

        final AddEsxiHostVSphereLicenseRequest addEsxiHostVSphereLicenseRequest = requestModel.getRequestMessage();

        assertNotNull(addEsxiHostVSphereLicenseRequest);
        assertNotNull(addEsxiHostVSphereLicenseRequest.getComponentEndpointIds());
        assertNotNull(addEsxiHostVSphereLicenseRequest.getCredentials());
        assertEquals(this.hostName, addEsxiHostVSphereLicenseRequest.getHostname());
        assertEquals(serviceTag, requestModel.getServiceTag());
    }
}