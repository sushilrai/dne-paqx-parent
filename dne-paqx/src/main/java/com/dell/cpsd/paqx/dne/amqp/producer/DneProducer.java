/**
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries.  All Rights Reserved.
 * Dell EMC Confidential/Proprietary Information
 */

package com.dell.cpsd.paqx.dne.amqp.producer;

import com.dell.converged.capabilities.compute.discovered.nodes.api.ListNodes;
import com.dell.cpsd.virtualization.capabilities.api.DiscoverClusterRequestInfoMessage;

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
}
