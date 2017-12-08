/**
 * <p>
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries. All Rights Reserved. Dell EMC Confidential/Proprietary Information
 * </p>
 */

package com.dell.cpsd.paqx.dne.service.delegates;

import com.dell.cpsd.paqx.dne.repository.DataServiceRepository;
import com.dell.cpsd.paqx.dne.service.NodeService;
import com.dell.cpsd.paqx.dne.service.model.ComponentEndpointIds;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import static com.dell.cpsd.paqx.dne.service.delegates.utils.DelegateConstants.INVENTORY_VCENTER_FAILED;
import static com.dell.cpsd.paqx.dne.service.delegates.utils.DelegateConstants.VCENTER_INFORMATION_NOT_FOUND;

@Component
@Scope("prototype")
@Qualifier("inventoryVCenter")
public class InventoryVCenter extends BaseWorkflowDelegate
{
    /**
     * The logger instance
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(InventoryVCenter.class);

    /**
     * The <code>NodeService</code> instance
     */
    private final NodeService           nodeService;
    private final DataServiceRepository repository;

    @Autowired
    public InventoryVCenter(final NodeService nodeService, final DataServiceRepository repository)
    {
        super(LOGGER, "Inventory VCenter");
        this.nodeService = nodeService;
        this.repository = repository;
    }

    @Override
    public void delegateExecute(final DelegateExecution delegateExecution)
    {
        ComponentEndpointIds componentEndpointIds = null;
        try
        {
            componentEndpointIds = repository.getVCenterComponentEndpointIdsByEndpointType("VCENTER-CUSTOMER");
        }
        catch (Exception e)
        {
            final String message = "An Unexpected Exception occurred attempting to retrieve vCenter Component Endpoints. Reason: ";
            updateDelegateStatus(message, e);
            throw new BpmnError(INVENTORY_VCENTER_FAILED,
                    message + e.getMessage());
        }
        if (componentEndpointIds == null)
        {
            final String message = "VCenter Endpoints not found.";
            updateDelegateStatus(message);
            throw new BpmnError(VCENTER_INFORMATION_NOT_FOUND, message);
        }

        try
        {
            this.nodeService.requestDiscoverVCenter(componentEndpointIds, delegateExecution.getProcessInstanceId());

            updateDelegateStatus("Inventory request for vCenter completed successfully.");
        }
        catch (Exception e)
        {
            final String message = "An Unexpected Exception occurred attempting to Inventory VCenter. Reason: ";
            updateDelegateStatus(message, e);
            throw new BpmnError(INVENTORY_VCENTER_FAILED,
                    message + e.getMessage());
        }
    }
}
