/**
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries.  All Rights Reserved.
 * Dell EMC Confidential/Proprietary Information
 */

package com.dell.cpsd.paqx.dne.amqp.producer;

import com.dell.converged.capabilities.compute.discovered.nodes.api.ChangeIdracCredentialsRequestMessage;
import com.dell.converged.capabilities.compute.discovered.nodes.api.CompleteNodeAllocationRequestMessage;
import com.dell.converged.capabilities.compute.discovered.nodes.api.ListNodes;
import com.dell.cpsd.rackhd.adapter.model.idrac.IdracNetworkSettingsRequestMessage;
import com.dell.cpsd.virtualization.capabilities.api.DiscoverClusterRequestInfoMessage;

/**
 * <p>
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries.  All Rights Reserved.
 * Dell EMC Confidential/Proprietary Information
 * </p>
 *
 * @since 1.0
 */

public interface DneProducer
{
    /**
     * List the discovered nodes
     *
     * @param request
     */
    void publishListNodes(ListNodes request);

    /**
     * list the virtualisation clusters
     *
     * @param request
     */
    void publishDiscoverClusters(DiscoverClusterRequestInfoMessage request);

    /**
     * Complete node allocation
     * 
     * @param request
     */
    void publishCompleteNodeAllocation(CompleteNodeAllocationRequestMessage request);

    /**
     * list the idrac network settings
     *
     * @param request
     */
    void publishIdracNetwokSettings(IdracNetworkSettingsRequestMessage request);
    
    /**
     * change idrac credentials
     * 
     * @param request
     */
    void publishChangeIdracCredentials(ChangeIdracCredentialsRequestMessage request);
}
