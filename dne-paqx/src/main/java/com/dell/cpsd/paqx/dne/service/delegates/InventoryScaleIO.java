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

import static com.dell.cpsd.paqx.dne.service.delegates.utils.DelegateConstants.INVENTORY_SCALE_IO_FAILED;
import static com.dell.cpsd.paqx.dne.service.delegates.utils.DelegateConstants.SCALE_IO_INFORMATION_NOT_FOUND;

@Component
@Scope("prototype")
@Qualifier("inventoryScaleIO")
public class InventoryScaleIO extends BaseWorkflowDelegate
{
    /**
     * The logger instance
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(InventoryScaleIO.class);

    /**
     * The <code>NodeService</code> instance
     */
    private final NodeService           nodeService;
    private final DataServiceRepository repository;

    @Autowired
    public InventoryScaleIO(final NodeService nodeService, final DataServiceRepository repository)
    {
        super(LOGGER, "Inventory Scale IO");
        this.nodeService = nodeService;
        this.repository = repository;
    }

    @Override
    public void delegateExecute(final DelegateExecution delegateExecution)
    {
        ComponentEndpointIds componentEndpointIds = null;
        try
        {
            componentEndpointIds = repository.getComponentEndpointIds("SCALEIO-CLUSTER");
        }
        catch (Exception e)
        {
            final String message = "An Unexpected Exception occurred attempting to retrieve Scale IO Component Endpoints. Reason:";
            updateDelegateStatus(message, e);
            throw new BpmnError(INVENTORY_SCALE_IO_FAILED,
                    message + e.getMessage());
        }
        if (componentEndpointIds == null)
        {
            final String message = "Scale IO Endpoints not found.";
            updateDelegateStatus(message);
            throw new BpmnError(SCALE_IO_INFORMATION_NOT_FOUND, message);
        }

        try
        {
            this.nodeService.requestDiscoverScaleIo(componentEndpointIds, delegateExecution.getProcessInstanceId());
            updateDelegateStatus("Inventory request for Scale IO completed successfully.");
        }
        catch (Exception e)
        {
            final String message = "An Unexpected Exception occurred attempting to inventory Scale IO.  Reason: ";
            updateDelegateStatus(message, e);
            throw new BpmnError(INVENTORY_SCALE_IO_FAILED,
                    message + e.getMessage());
        }
    }
}
