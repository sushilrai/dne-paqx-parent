/**
 * <p>
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries. All Rights Reserved. Dell EMC Confidential/Proprietary Information
 * </p>
 */

package com.dell.cpsd.paqx.dne.service.task.handler.addnode;

import com.dell.cpsd.paqx.dne.repository.DataServiceRepository;
import com.dell.cpsd.paqx.dne.service.NodeService;

/**
 * Task handler for entering the host from the maintenance mode
 *
 * <p>
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries. All Rights Reserved. Dell EMC Confidential/Proprietary Information
 * </p>
 *
 * @version 1.0
 * @since 1.0
 */
public class EnterHostMaintenanceModeTaskHandler extends AbstractHostMaintenanceModeTaskHandler
{
    public EnterHostMaintenanceModeTaskHandler(final NodeService nodeService, final DataServiceRepository repository)
    {
        super(nodeService, repository, "Enter Host Maintenance mode");
    }

    @Override
    protected boolean getMaintenanceModeEnable()
    {
        return true;
    }
}
