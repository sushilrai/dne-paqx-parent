/**
 * <p>
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries. All Rights Reserved. Dell EMC Confidential/Proprietary Information
 * </p>
 */

package com.dell.cpsd.paqx.dne.service.delegates;

import com.dell.cpsd.paqx.dne.service.NodeService;
import com.dell.cpsd.paqx.dne.transformers.HostMaintenanceRequestTransformer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
@Qualifier("enterHostMaintenanceMode")
public class EnterHostMaintenanceMode extends AbstractHostMaintenanceMode
{
    @Autowired
    public EnterHostMaintenanceMode(final NodeService nodeService, final HostMaintenanceRequestTransformer requestTransformer)
    {
        super(nodeService, requestTransformer, "Enter Host Maintenance Mode");
    }

    @Override
    protected boolean getMaintenanceModeEnable()
    {
        return true;
    }
}
