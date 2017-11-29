/**
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries.  All Rights Reserved.
 * Dell EMC Confidential/Proprietary Information
 **/

package com.dell.cpsd.paqx.dne.transformers;

import com.dell.cpsd.ConfigurePxeBootRequestMessage;
import com.dell.cpsd.PxeBootConfig;
import com.dell.cpsd.paqx.dne.service.delegates.model.DelegateRequestModel;
import com.dell.cpsd.paqx.dne.service.delegates.model.NodeDetail;
import com.dell.cpsd.paqx.dne.service.delegates.utils.DelegateConstants;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

/**
 * Test class for configure PXE boot request transformer
 * <p>
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries. All Rights Reserved.
 * Dell EMC Confidential/Proprietary Information
 * </p>
 *
 * @version 1.0
 * @since 1.0
 */
@RunWith(MockitoJUnitRunner.class)
public class ConfigurePxeBootRequestTransformerTest
{
    @Mock
    private DelegateExecution delegateExecution;

    private ConfigurePxeBootRequestTransformer requestTransformer;

    private static final String     shareName      = "share-name";
    private static final Integer    shareType      = 1;
    private static final String[]   fqdds          = new String[] {"fqdd1", "fqdd2"};
    private static final String     bootProtoName  = "boot-proto-name";
    private static final String     bootProtoValue = "boot-proto-value";
    private static final String     serviceTag     = "serviceTag";
    private static final String     symphonyUuid   = "symphony-uuid";
    private static final String     idracIpAddress = "1.1.1.1";
    private final        NodeDetail nodeDetail     = new NodeDetail();

    @Before
    public void setup() throws Exception
    {
        requestTransformer = new ConfigurePxeBootRequestTransformer(shareName, shareType, fqdds, bootProtoName, bootProtoValue);
        nodeDetail.setServiceTag(serviceTag);
        nodeDetail.setId(symphonyUuid);
        nodeDetail.setIdracIpAddress(idracIpAddress);
    }

    @Test
    public void configurePxeBootRequestIsValid() throws Exception
    {
        when(delegateExecution.getVariable(DelegateConstants.NODE_DETAIL)).thenReturn(nodeDetail);

        final DelegateRequestModel<ConfigurePxeBootRequestMessage> requestModel = requestTransformer
                .buildConfigurePxeBootRequest(delegateExecution);

        assertNotNull(requestModel);

        final ConfigurePxeBootRequestMessage requestMessage = requestModel.getRequestMessage();

        assertNotNull(requestMessage);
        assertEquals(requestMessage.getUuid(), symphonyUuid);
        assertEquals(requestMessage.getIpAddress(), idracIpAddress);

        final PxeBootConfig pxeBootConfig = requestMessage.getPxeBootConfig();

        assertNotNull(pxeBootConfig);
        assertEquals(pxeBootConfig.getNicFqdds(), asList(fqdds));
        assertEquals(pxeBootConfig.getProtoName(), bootProtoName);
        assertEquals(pxeBootConfig.getProtoValue(), bootProtoValue);
        assertEquals(pxeBootConfig.getShareName(), shareName);
        assertEquals(pxeBootConfig.getShareType(), shareType);

        assertNotNull(requestModel.getServiceTag());
        assertEquals(requestModel.getServiceTag(), serviceTag);
    }
}