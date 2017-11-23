/**
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries.  All Rights Reserved.
 * Dell EMC Confidential/Proprietary Information
 **/

package com.dell.cpsd.paqx.dne.transformers;

import com.dell.cpsd.paqx.dne.service.delegates.model.DelegateRequestModel;
import com.dell.cpsd.paqx.dne.service.delegates.model.NodeDetail;
import com.dell.cpsd.paqx.dne.service.model.ComponentEndpointIds;
import com.dell.cpsd.virtualization.capabilities.api.HostPowerOperationRequestMessage;
import com.dell.cpsd.virtualization.capabilities.api.PowerOperationRequest;
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
 * Unit tests for reboot host request transformer
 * <p>
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries. All Rights Reserved.
 * Dell EMC Confidential/Proprietary Information
 * </p>
 *
 * @version 1.0
 * @since 1.0
 */
@RunWith(MockitoJUnitRunner.class)
public class HostPowerOperationsTransformerTest
{
    private final String hostname = "hostname";

    @Mock
    private ComponentIdsTransformer componentIdsTransformer;

    @Mock
    private DelegateExecution delegateExecution;

    @Mock
    private ComponentEndpointIds componentEndpointIds;

    @Mock
    private NodeDetail nodeDetail;

    private HostPowerOperationsTransformer hostPowerOperationsTransformer;
    private final String serviceTag = "service-tag";

    @Before
    public void setup() throws Exception
    {
        hostPowerOperationsTransformer = new HostPowerOperationsTransformer(componentIdsTransformer);
    }

    @Test
    public void testRebootHostBuildRequestMessageIsValid() throws Exception
    {
        when(delegateExecution.getVariable(HOSTNAME)).thenReturn(hostname);
        when(componentIdsTransformer.getVCenterComponentEndpointIdsByEndpointType("VCENTER-CUSTOMER")).thenReturn(componentEndpointIds);
        when(delegateExecution.getVariable(NODE_DETAIL)).thenReturn(nodeDetail);
        when(nodeDetail.getServiceTag()).thenReturn(serviceTag);

        final DelegateRequestModel<HostPowerOperationRequestMessage> requestModel = hostPowerOperationsTransformer
                .buildHostPowerOperationsRequestMessage(delegateExecution, PowerOperationRequest.PowerOperation.REBOOT);
        assertNotNull(requestModel);

        final HostPowerOperationRequestMessage requestMessage = requestModel.getRequestMessage();

        assertNotNull(requestMessage);

        final PowerOperationRequest powerOperationRequest = requestMessage.getPowerOperationRequest();

        assertNotNull(powerOperationRequest);
        assertNotNull(requestMessage.getComponentEndpointIds());
        assertEquals(hostname, powerOperationRequest.getHostName());
        assertEquals(PowerOperationRequest.PowerOperation.REBOOT, powerOperationRequest.getPowerOperation());
        assertEquals(serviceTag, nodeDetail.getServiceTag());
    }
}
