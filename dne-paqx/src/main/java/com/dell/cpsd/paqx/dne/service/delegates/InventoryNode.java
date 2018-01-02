/**
 * <p>
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries. All Rights Reserved. Dell EMC Confidential/Proprietary Information
 * </p>
 */

package com.dell.cpsd.paqx.dne.service.delegates;

import com.dell.cpsd.paqx.dne.domain.node.NodeInventory;
import com.dell.cpsd.paqx.dne.repository.DataServiceRepository;
import com.dell.cpsd.paqx.dne.service.NodeService;
import com.dell.cpsd.service.common.client.exception.ServiceExecutionException;
import com.dell.cpsd.service.common.client.exception.ServiceTimeoutException;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.dell.cpsd.paqx.dne.service.delegates.utils.DelegateConstants.INVENTORY_NODES_FAILED;

@Component
@Scope("prototype")
@Qualifier("inventoryNode")
public class InventoryNode extends BaseWorkflowDelegate
{
    /*
     * The logger instance
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(InventoryNode.class);

    /*
     * Node Service instance used to find and validate Storage pool entries
     */
    private NodeService nodeService;

    /*
     * Reference to H2 repository
     */
    private final DataServiceRepository repository;

    @Autowired
    public InventoryNode(final NodeService nodeService, final DataServiceRepository repository)
    {
        super(LOGGER, "Update Node Inventory");
        this.nodeService = nodeService;
        this.repository = repository;
    }

    @Override
    public void delegateExecute(final DelegateExecution delegateExecution)
    {

        Map<String, Object> nodeInventoryResponse = null;
        try
        {
            updateDelegateStatus("Requesting Update Node Inventory.");
            nodeInventoryResponse = nodeService.listNodeInventory(null);
        }
        catch (ServiceTimeoutException | ServiceExecutionException ex)
        {
            final String message = "Update Node Inventory request failed.";
            updateDelegateStatus(message, ex);
            throw new BpmnError(INVENTORY_NODES_FAILED, message);
        }

        if (MapUtils.isNotEmpty(nodeInventoryResponse))
        {
            List<String> nodeIds = new ArrayList<>();
            List<String> jsonProcessingExceptionNodes = new ArrayList<>();
            List<String> failedToSaveNodes = new ArrayList<>();
            String jpeErrorMessage = "Update Node Inventory failed due to unrecognized response for Node(s) with uuid ";
            String niErrorMessage = "Failed to update Node Inventory on Node(s) with uuid ";
            nodeInventoryResponse.entrySet().stream().filter(Objects::nonNull).forEach(entry -> {
                NodeInventory nodeInventory = null;
                try
                {
                    nodeInventory = new NodeInventory(entry.getKey(), entry.getValue());
                    nodeIds.add(entry.getKey());
                }
                catch (JsonProcessingException jpe)
                {
                    LOGGER.error("Error parsing node inventory with uuid " + entry.getKey(), jpe);
                    jsonProcessingExceptionNodes.add(entry.getKey());
                }

                boolean isNodeInventorySaved = false;
                if (nodeInventory != null)
                {
                    isNodeInventorySaved = repository.saveNodeInventory(nodeInventory);
                }
                if (!isNodeInventorySaved)
                {
                    failedToSaveNodes.add(entry.getKey());
                }
            });

            if (CollectionUtils.isNotEmpty(jsonProcessingExceptionNodes))
            {
                final String message = jpeErrorMessage + StringUtils.join(jsonProcessingExceptionNodes, ',');
                updateDelegateStatus(message);
                throw new BpmnError(INVENTORY_NODES_FAILED, message);
            }
            else if (CollectionUtils.isNotEmpty(failedToSaveNodes))
            {
                final String message = niErrorMessage + StringUtils.join(failedToSaveNodes, ',');
                updateDelegateStatus(message);
                throw new BpmnError(INVENTORY_NODES_FAILED, message);
            }
            else
            {
                updateDelegateStatus("Update Node Inventory was successful for Nodes " + Arrays.toString(nodeIds.toArray()));
            }
        }
        else
        {
            final String message = "Could not find any node inventory.";
            updateDelegateStatus(message);
            throw new BpmnError(INVENTORY_NODES_FAILED, message);
        }

    }
}
