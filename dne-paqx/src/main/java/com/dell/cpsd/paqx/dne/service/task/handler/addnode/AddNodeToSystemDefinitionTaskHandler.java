/**
 * <p>
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries. All Rights Reserved. Dell EMC Confidential/Proprietary Information
 * </p>
 */

package com.dell.cpsd.paqx.dne.service.task.handler.addnode;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dell.cpsd.paqx.dne.domain.IWorkflowTaskHandler;
import com.dell.cpsd.paqx.dne.domain.Job;
import com.dell.cpsd.paqx.dne.service.model.FirstAvailableDiscoveredNodeResponse;
import com.dell.cpsd.paqx.dne.service.model.NodeInfo;
import com.dell.cpsd.paqx.dne.service.model.Status;
import com.dell.cpsd.paqx.dne.service.model.TaskResponse;
import com.dell.cpsd.paqx.dne.service.task.handler.BaseTaskHandler;
import com.dell.cpsd.sdk.AMQPClient;
import com.dell.cpsd.service.system.definition.api.Component;
import com.dell.cpsd.service.system.definition.api.ComponentsFilter;
import com.dell.cpsd.service.system.definition.api.ConvergedSystem;
import com.dell.cpsd.service.system.definition.api.ConvergedSystemAddition;
import com.dell.cpsd.service.system.definition.api.Endpoint;
import com.dell.cpsd.service.system.definition.api.Group;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Task responsible for adding a discovered node to the system definition.
 * 
 * <p>
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries. All Rights Reserved. Dell EMC Confidential/Proprietary Information
 * </p>
 * 
 * @since 1.0
 */
public class AddNodeToSystemDefinitionTaskHandler extends BaseTaskHandler implements IWorkflowTaskHandler
{
    /*
     * The logger instance
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(AddNodeToSystemDefinitionTaskHandler.class);

    /*
     * The <code>AMQPClient</code> instance
     */
    private final AMQPClient    sdkAMQPClient;

    /**
     * AddNodeToSystemDefinitionTaskHandler constructor.
     * 
     * @param sdkAMQPClient
     *            - The <code>AMQPClient</code> instance.
     * 
     * @since 1.0
     */
    public AddNodeToSystemDefinitionTaskHandler(AMQPClient sdkAMQPClient)
    {
        this.sdkAMQPClient = sdkAMQPClient;
    }

    /**
     * Perform the task of adding a discovered node to the system definition.
     * 
     * @param job
     *            - The <code>Job</code> this task is part of.
     * 
     * @since 1.0
     */
    @Override
    public boolean executeTask(Job job)
    {
        LOGGER.info("Execute AddNodeToSystemDefinitionTaskHandler task");

        TaskResponse response = initializeResponse(job);

        try
        {
            Map<String, TaskResponse> responseMap = job.getTaskResponseMap();
            FirstAvailableDiscoveredNodeResponse findNodeTask = (FirstAvailableDiscoveredNodeResponse) responseMap
                    .get("findAvailableNodes");
            if (findNodeTask == null)
            {
                throw new IllegalStateException("No discovered node task found.");
            }

            NodeInfo nodeInfo = findNodeTask.getNodeInfo();
            if (nodeInfo == null)
            {
                throw new IllegalStateException("No discovered node info found.");
            }

            List<ConvergedSystem> allConvergedSystems = this.sdkAMQPClient.getConvergedSystems();
            if (CollectionUtils.isEmpty(allConvergedSystems))
            {
                throw new IllegalStateException("No converged systems found.");
            }

            ConvergedSystem system = allConvergedSystems.get(0);
            ComponentsFilter componentsFilter = new ComponentsFilter();
            componentsFilter.setSystemUuid(system.getUuid());

            List<ConvergedSystem> systemDetails = this.sdkAMQPClient.getComponents(componentsFilter);
            if (CollectionUtils.isEmpty(systemDetails))
            {
                throw new IllegalStateException("No converged system found.");
            }

            this.addNewNode(systemDetails.get(0), nodeInfo);

            response.setWorkFlowTaskStatus(Status.SUCCEEDED);
            return true;
        }
        catch (Exception e)
        {
            LOGGER.error("Error adding node to the system definition", e);
            response.addError(e.toString());
        }

        response.setWorkFlowTaskStatus(Status.FAILED);
        return false;
    }

    /*
     * Add a <code>Component</code> instance to the <code>ConvergedSystem</code>.
     * 
     * @param systemToUpdate
     *            - The <code>ConvergedSystem</code> to which the new <code>Component</code> will be added.
     * @param nodeInfo
     *            - The <code>NodeInfo</code> from which the new <code>Component</code> will be created.
     * 
     * @throws JsonProcessingException
     * 
     * @since 1.0
     */
    private void addNewNode(ConvergedSystem systemToUpdate, NodeInfo nodeInfo) throws JsonProcessingException
    {
        Component newNode = new Component();
        newNode.setUuid(nodeInfo.getSymphonyUuid());
        newNode.setIdentity(nodeInfo.getIdentity());
        newNode.setDefinition(nodeInfo.getDefinition());
        newNode.setParentGroupUuids(this.mapGroupNamesToUUIDs(nodeInfo.getParentGroups(), systemToUpdate.getGroups()));
        newNode.setEndpoints(this.mapEndpointNamestoUUIDs(nodeInfo.getEndpoints(), systemToUpdate.getEndpoints()));

        boolean okToAdd = true;

        for (Component component : systemToUpdate.getComponents())
        {
            if (component.getIdentity().getIdentifier().equals(newNode.getIdentity().getIdentifier()))
            {
                okToAdd = false;
                break;

            }
        }

        if (okToAdd)
        {
            LOGGER.info("Discovered node does not exist in system definition - adding it");
            systemToUpdate.getComponents().add(newNode);
        }
        else
        {
            LOGGER.info("Discovered node already exists in system definition: " + newNode.getIdentity());
        }

        ConvergedSystemAddition result = this.sdkAMQPClient.createOrUpdateConvergedSystem(systemToUpdate, null);

        ObjectMapper mapper = new ObjectMapper();
        LOGGER.info("Successfully updated converged system: " + mapper.writeValueAsString(result));
    }

    /*
     * Given a list of group names map each one to its corresponding UUID.
     * 
     * @param groupNames
     *            - The <code>List<String></code> of names to map to UUIDs.
     * @param groups
     *            - The <code>List<Group></code> of group objects from which the UUID will be mapped.
     * 
     * @return List<String>.
     * 
     * @since 1.0
     */
    private List<String> mapGroupNamesToUUIDs(List<String> groupNames, List<Group> groups)
    {
        List<String> groupUuids = new ArrayList<>();
        groups.forEach(group -> {
            if (groupNames.contains(group.getName()))
            {
                groupUuids.add(group.getUuid());
            }
        });
        return groupUuids;
    }

    /*
     * Given a list of endpoint names map each one to its corresponding UUID.
     * 
     * @param endpointNames
     *            - The <code>List<String></code> of names to map to UUIDs.
     * @param endpoints
     *            - The <code>List<Endpoint></code> of group objects from which the UUID will be mapped.
     * 
     * @return List<String>.
     * 
     * @since 1.0
     */
    private List<String> mapEndpointNamestoUUIDs(List<String> endpointNames, List<Endpoint> endpoints)
    {
        List<String> endpointUuids = new ArrayList<>();
        endpoints.forEach(endpoint -> {
            if (endpointNames.contains(endpoint.getType().toUpperCase()))
            {
                endpointUuids.add(endpoint.getUuid());
            }
        });
        return endpointUuids;
    }
}
