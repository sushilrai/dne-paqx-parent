/**
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries.  All Rights Reserved.
 * Dell EMC Confidential/Proprietary Information
 **/

package com.dell.cpsd.paqx.dne.transformers;

import com.dell.cpsd.ConfigurePxeBootRequestMessage;
import com.dell.cpsd.PxeBootConfig;
import com.dell.cpsd.paqx.dne.service.delegates.model.DelegateRequestModel;
import com.dell.cpsd.paqx.dne.service.delegates.model.NodeDetail;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Arrays;

import static com.dell.cpsd.paqx.dne.service.delegates.utils.DelegateConstants.NODE_DETAIL;

/**
 * Configure PXE boot request transformer
 * <p>
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries. All Rights Reserved.
 * Dell EMC Confidential/Proprietary Information
 * </p>
 *
 * @version 1.0
 * @since 1.0
 */
@Component
public class ConfigurePxeBootRequestTransformer
{
    private final String   shareName;
    private final Integer  shareType;
    private final String[] fqdds;
    private final String   bootProtoName;
    private final String   bootProtoValue;

    public ConfigurePxeBootRequestTransformer(@Value("${rackhd.boot.proto.share.name}") final String shareName,
            @Value("${rackhd.boot.proto.share.type}") final Integer shareType, @Value("${rackhd.boot.proto.fqdds}") final String[] fqdds,
            @Value("${rackhd.boot.proto.name}") final String bootProtoName,
            @Value("${rackhd.boot.proto.value}") final String bootProtoValue)
    {
        this.shareName = shareName;
        this.shareType = shareType;
        this.fqdds = fqdds;
        this.bootProtoName = bootProtoName;
        this.bootProtoValue = bootProtoValue;
    }

    public DelegateRequestModel<ConfigurePxeBootRequestMessage> buildConfigurePxeBootRequest(final DelegateExecution delegateExecution)
    {
        final NodeDetail nodeDetail = (NodeDetail) delegateExecution.getVariable(NODE_DETAIL);

        final ConfigurePxeBootRequestMessage requestMessage = new ConfigurePxeBootRequestMessage();

        requestMessage.setUuid(nodeDetail.getId());
        requestMessage.setIpAddress(nodeDetail.getIdracIpAddress());

        final PxeBootConfig pxeBootConfig = new PxeBootConfig();
        pxeBootConfig.setProtoValue(bootProtoValue);
        pxeBootConfig.setShareType(shareType);
        pxeBootConfig.setShareName(shareName);
        pxeBootConfig.setProtoName(bootProtoName);
        pxeBootConfig.setNicFqdds(Arrays.asList(fqdds));

        requestMessage.setPxeBootConfig(pxeBootConfig);

        return new DelegateRequestModel<>(requestMessage, nodeDetail.getServiceTag());
    }
}
