/**
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries.  All Rights Reserved.
 * Dell EMC Confidential/Proprietary Information
 */

package com.dell.cpsd.paqx.dne.service;

import com.dell.cpsd.paqx.dne.service.model.DiscoveredNode;
import com.dell.cpsd.paqx.dne.service.model.IdracInfo;
import com.dell.cpsd.paqx.dne.service.model.IdracNetworkSettingsRequest;
import com.dell.cpsd.paqx.dne.service.model.VirtualizationCluster;
import com.dell.cpsd.service.common.client.exception.ServiceExecutionException;
import com.dell.cpsd.service.common.client.exception.ServiceTimeoutException;

import java.util.List;

public interface NodeService
{
    /**
     * List the discovered nodes
     *
     * @return
     * @throws ServiceTimeoutException
     * @throws ServiceExecutionException
     */
    List<DiscoveredNode> listDiscoveredNodes() throws ServiceTimeoutException, ServiceExecutionException;

    /**
     * List the virtualisation clusters
     *
     * @return
     * @throws ServiceTimeoutException
     * @throws ServiceExecutionException
     */
    List<VirtualizationCluster> listClusters() throws ServiceTimeoutException, ServiceExecutionException;

    /**
     * Notify the Node Discovery Service that node allocation is complete
     * 
     * @param elementIdentifier - The node identifier
     * @throws ServiceTimeoutException
     * @throws ServiceExecutionException
     */
    void notifyNodeAllocationComplete(String elementIdentifier) throws ServiceTimeoutException, ServiceExecutionException;

    /**
     * Configure the iDRAC network settings.
     * 
     * @param idracNetworkSettingsRequest - The <code>IdracNetworkSettingsRequest</code> instance.
     * 
     * @return
     * @throws ServiceTimeoutException
     * @throws ServiceExecutionException
     */
    IdracInfo idracNetworkSettings(IdracNetworkSettingsRequest idracNetworkSettingsRequest)
            throws ServiceTimeoutException, ServiceExecutionException;
}
