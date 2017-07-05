/**
 * <p>
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries. All Rights Reserved. Dell EMC Confidential/Proprietary Information
 * </p>
 */

package com.dell.cpsd.paqx.dne.service.task.handler.addnode;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
    private static final Logger LOGGER = 
            LoggerFactory.getLogger(AddNodeToSystemDefinitionTaskHandler.class);

    /*
     * The <code>AMQPClient</code> instance
     */
    private final AMQPClient sdkAMQPClient;
    
    /**
     * AddNodeToSystemDefinitionTaskHandler constructor.
     * 
     * @param sdkAMQPClient - The <code>AMQPClient</code> instance.
     * 
     * @since   1.0
     */
    public AddNodeToSystemDefinitionTaskHandler(AMQPClient sdkAMQPClient)
    {
        this.sdkAMQPClient = sdkAMQPClient;
    }

    /**
     * Perform the task of adding a discovered node to the system definition.
     * 
     * @param job - The <code>Job</code> this task is part of.
     * 
     * @since   1.0
     */
    @Override
    public boolean executeTask(Job job)
    {
        LOGGER.info("Execute AddNodeToSystemDefinitionTaskHandler task");

        TaskResponse response = initializeResponse(job);

        try
        {
            Map<String, TaskResponse> responseMap = job.getTaskResponseMap();
            FirstAvailableDiscoveredNodeResponse findNodesTask = (FirstAvailableDiscoveredNodeResponse)responseMap.get("findAvailableNodes");
            NodeInfo nodeInfo = findNodesTask.getNodeInfo();
            //nodeInfo.getIdentity().setAddress(job.getInputParams().getIdracIpAddress());
            
            List<ConvergedSystem> allConvergedSystems = this.sdkAMQPClient.getConvergedSystems();
            ConvergedSystem system = allConvergedSystems.get(0);
            
            ComponentsFilter componentsFilter = new ComponentsFilter();
            componentsFilter.setSystemUuid(system.getUuid());
            List<ConvergedSystem> systemDetails = this.sdkAMQPClient.getComponents(componentsFilter);
            ConvergedSystem systemToUpdate = systemDetails.get(0);
            
            Component newNode = new Component();
            newNode.setUuid(nodeInfo.getSymphonyUuid());
            newNode.setIdentity(nodeInfo.getIdentity());
            newNode.setDefinition(nodeInfo.getDefinition());
            newNode.setEndpoints(this.mapEndpointNamestoUUIDs(nodeInfo.getEndpoints(), systemToUpdate.getEndpoints()));
            newNode.setParentGroupUuids(this.mapGroupNamesToUUIDs(nodeInfo.getParentGroups(), systemToUpdate.getGroups()));
            systemToUpdate.getComponents().add(newNode);
            
            ConvergedSystemAddition result = this.sdkAMQPClient.createOrUpdateConvergedSystem(systemToUpdate, null);
            
            ObjectMapper mapper = new ObjectMapper();
            LOGGER.info("Successfully updated converged system: " + mapper.writeValueAsString(result));
            
            response.setWorkFlowTaskStatus(Status.SUCCEEDED);
            
            return true;
        }
        catch (Exception e)
        {
            LOGGER.info(" ", e);
            response.setWorkFlowTaskStatus(Status.FAILED);
            response.addError(e.toString());
        }

        return false;
    }
    
    /**
     * Given a list of group names map each one to its corresponding UUID.
     * 
     * @param groupNames - The <code>List<String></code> of names to map to UUIDs.
     * @param groups - The <code>List<Group></code> of group objects from which the UUID will be mapped.
     * 
     * @return List<String>.
     * 
     * @since   1.0
     */
    private List<String> mapGroupNamesToUUIDs(List<String> groupNames, List<Group> groups)
    {
        List<String> groupUuids = new ArrayList<>();
        groups.forEach(group -> {
            if (groupNames.contains(group.getName())) {
                groupUuids.add(group.getUuid());
            }
        });
        return groupUuids;
    }
    
    /**
     * Given a list of endpoint names map each one to its corresponding UUID.
     * 
     * @param endpointNames - The <code>List<String></code> of names to map to UUIDs.
     * @param endpoints - The <code>List<Endpoint></code> of group objects from which the UUID will be mapped.
     * 
     * @return List<String>.
     * 
     * @since   1.0
     */
    private List<String> mapEndpointNamestoUUIDs(List<String> endpointNames, List<Endpoint> endpoints)
    {
        // TODO: this is a workaround until the endpoint name 
        // is supported and persisted...
        List<String> endpointUuids = new ArrayList<>();
        endpoints.forEach(endpoint -> {
            if ("RACKHD".equals(endpoint.getType().toUpperCase())) {
                endpointUuids.add(endpoint.getUuid());
            }
        });
        return endpointUuids;
    }
}
