/**
 * <p>
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries. All Rights Reserved. Dell EMC Confidential/Proprietary Information
 * </p>
 */

package com.dell.cpsd.paqx.dne.service.delegates;

import com.dell.cpsd.paqx.dne.domain.node.NodeInventory;
import com.dell.cpsd.paqx.dne.domain.scaleio.ScaleIOData;
import com.dell.cpsd.paqx.dne.domain.scaleio.ScaleIOProtectionDomain;
import com.dell.cpsd.paqx.dne.domain.scaleio.ScaleIOSDS;
import com.dell.cpsd.paqx.dne.domain.vcenter.Host;
import com.dell.cpsd.paqx.dne.repository.DataServiceRepository;
import com.dell.cpsd.paqx.dne.service.NodeService;
import com.dell.cpsd.paqx.dne.service.delegates.model.NodeDetail;
import com.dell.cpsd.service.engineering.standards.EssValidateProtectionDomainsRequestMessage;
import com.dell.cpsd.service.engineering.standards.EssValidateProtectionDomainsResponseMessage;
import com.dell.cpsd.service.engineering.standards.NodeData;
import com.dell.cpsd.service.engineering.standards.ProtectionDomain;
import com.dell.cpsd.service.engineering.standards.ScaleIODataServer;
import com.jayway.jsonpath.JsonPath;
import net.minidev.json.JSONArray;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.naming.InvalidNameException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.dell.cpsd.paqx.dne.service.delegates.utils.DelegateConstants.NODE_DETAIL;
import static com.dell.cpsd.paqx.dne.service.delegates.utils.DelegateConstants.SELECT_PROTECTION_DOMAINS_FAILED;

@Component
@Scope("prototype")
@Qualifier("selectProtectionDomains")
/**
 * <p>
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries. All Rights Reserved. Dell EMC Confidential/Proprietary Information
 * </p>
 */ public class SelectProtectionDomains extends BaseWorkflowDelegate
{

    private static final String R6_PRODUCT_NAME = "R6";
    private static final String R7_PRODUCT_NAME = "R7";
    private static final String NODE_TYPE_1U1N = "1U1N";
    private static final String NODE_TYPE_2U1N = "2U1N";

    private static final Logger LOGGER = LoggerFactory.getLogger(SelectProtectionDomains.class);

    private final NodeService nodeService;

    private final DataServiceRepository repository;

    @Autowired
    public SelectProtectionDomains(NodeService nodeService, DataServiceRepository repository)
    {
        super(LOGGER, "Select Protection Domains");
        this.nodeService = nodeService;
        this.repository = repository;
    }

    @Override
    public void delegateExecute(final DelegateExecution delegateExecution)
    {
        NodeDetail nodeDetail = (NodeDetail) delegateExecution.getVariable(NODE_DETAIL);
        updateDelegateStatus("Selecting Protection Domain for Node " + nodeDetail.getServiceTag() + ".");
        EssValidateProtectionDomainsResponseMessage protectionDomainResponse = null;
        final List<ProtectionDomain> protectionDomainList = new ArrayList<>();
        try
        {
            final NodeData nodeData = new NodeData();
            final String uuid = nodeDetail.getId();
            nodeData.setSymphonyUuid(uuid);
            nodeData.setType(getNodeType(uuid));
            final List<ScaleIOData> scaleIODataList = nodeService.listScaleIOData();

            final ScaleIOData scaleIo = scaleIODataList.get(0);

            final List<ScaleIOProtectionDomain> protectionDomains = scaleIo.getProtectionDomains();

            if (!org.springframework.util.CollectionUtils.isEmpty(protectionDomains))
            {
                protectionDomains.stream().filter(Objects::nonNull).forEach(pd -> {
                    addProtectionDomainRequestObjectToList(nodeData, nodeDetail, protectionDomainList, pd);
                });
            }

            final EssValidateProtectionDomainsRequestMessage requestMessage = new EssValidateProtectionDomainsRequestMessage();

            requestMessage.setNodeData(nodeData);
            requestMessage.setProtectionDomains(protectionDomainList);

            protectionDomainResponse = nodeService.validateProtectionDomains(requestMessage);

        }
        catch (Exception e)
        {
            final String message = "An Unexpected Exception occurred retrieving the Protection Domain on Node " +
                                   nodeDetail.getServiceTag() + ". Reason: ";
            updateDelegateStatus(message, e);
            throw new BpmnError(SELECT_PROTECTION_DOMAINS_FAILED, message + e.getMessage());
        }

        if (protectionDomainResponse == null || !org.springframework.util.CollectionUtils.isEmpty(
                protectionDomainResponse.getValidProtectionDomains()))
        {
            setResultsInFindProtectionDomainTaskResponse(nodeDetail, protectionDomainList, protectionDomainResponse);
            updateDelegateStatus("Set protection domain to: " + nodeDetail.getProtectionDomainName() + "(" +
                                 nodeDetail.getProtectionDomainId() + ") for Node " + nodeDetail.getServiceTag() + ".");
        }
        else
        {
            String error = "No protection domain found. Creation of protection domain is not required.";
            updateDelegateStatus(error);
            throw new BpmnError(SELECT_PROTECTION_DOMAINS_FAILED, error);
        }
        delegateExecution.setVariable(NODE_DETAIL, nodeDetail);

    }

    private void addProtectionDomainRequestObjectToList(final NodeData nodeData, final NodeDetail nodeDetail,
                                                        final List<ProtectionDomain> protectionDomainList,
                                                        final ScaleIOProtectionDomain pd)
    {
        final ProtectionDomain protectionDomainRequest = buildProtectionDomainRequestObject(
                repository.getScaleIoProtectionDomains().stream().filter(Objects::nonNull).findFirst().orElse(null),
                nodeDetail.getEsxiManagementHostname(), nodeDetail.getEsxiManagementIpAddress(),
                repository.getVCenterHosts(), nodeData, pd);

        protectionDomainList.add(protectionDomainRequest);
    }

    private ProtectionDomain buildProtectionDomainRequestObject(final ScaleIOProtectionDomain scaleIOProtectionDomain,
                                                                final String esxiManagementHostname,
                                                                final String esxiManagementIpAddress,
                                                                final List<Host> hosts, final NodeData nodeData,
                                                                final ScaleIOProtectionDomain pd)
    {
        final ProtectionDomain protectionDomainRequest = new ProtectionDomain();
        protectionDomainRequest.setId(pd.getId());
        protectionDomainRequest.setName(pd.getName());
        protectionDomainRequest.setState(pd.getProtectionDomainState());

        final List<ScaleIOSDS> sdsList = pd.getSdsList();

        if (!org.springframework.util.CollectionUtils.isEmpty(sdsList))
        {
            final List<ScaleIODataServer> scaleIODataServers = extractScaleIoDataServersForProtectionDomain(
                    scaleIOProtectionDomain, esxiManagementHostname, esxiManagementIpAddress, hosts, nodeData, sdsList);

            protectionDomainRequest.setScaleIODataServers(scaleIODataServers);
        }

        return protectionDomainRequest;
    }

    private List<ScaleIODataServer> extractScaleIoDataServersForProtectionDomain(
            final ScaleIOProtectionDomain scaleIOProtectionDomain, final String esxiManagementHostname,
            final String esxiManagementIpAddress, final List<Host> hosts, final NodeData nodeData,
            final List<ScaleIOSDS> sdsList)
    {
        final List<ScaleIODataServer> scaleIODataServers = new ArrayList<>();

        sdsList.stream().filter(Objects::nonNull).forEach(sds -> {
            createSdsInstance(hosts, scaleIODataServers, sds);

            setProtectionDomainIdInNodeData(scaleIOProtectionDomain, esxiManagementHostname, esxiManagementIpAddress,
                                            nodeData, sds);
        });

        return scaleIODataServers;
    }

    private void setResultsInFindProtectionDomainTaskResponse(final NodeDetail nodeDetail,
                                                              final List<ProtectionDomain> protectionDomainList,
                                                              final EssValidateProtectionDomainsResponseMessage protectionDomainResponse)
    {
        final String protectionDomainId = protectionDomainResponse.getValidProtectionDomains().get(0)
                                                                  .getProtectionDomainID();

        final List<String> protectionDomainNameList = protectionDomainList.stream().filter(Objects::nonNull).filter(
                p -> p.getId().equalsIgnoreCase(protectionDomainId)).map(ProtectionDomain::getName).collect(
                Collectors.toList());

        nodeDetail.setProtectionDomainId(protectionDomainId);
        nodeDetail.setProtectionDomainName(
                protectionDomainNameList.size() == 1 ? protectionDomainList.get(0).getName() : protectionDomainId);
    }

    private void createSdsInstance(final List<Host> hosts, final List<ScaleIODataServer> scaleIODataServers,
                                   final ScaleIOSDS sds)
    {
        final ScaleIODataServer scaleIODataServer = new ScaleIODataServer();
        scaleIODataServer.setType(getHostType(sds, hosts));
        scaleIODataServer.setId(sds.getId());
        scaleIODataServer.setName(sds.getName());
        scaleIODataServers.add(scaleIODataServer);
    }

    private String getHostType(ScaleIOSDS scaleIOSDS, List<Host> hosts)
    {
        String type = null;
        String hostDomain = repository.getDomainName(); //getting the host domain

        String scaleIoName = scaleIOSDS.getName();

        String scaleIONameSuffix = "-ESX";
        if (scaleIoName.endsWith(scaleIONameSuffix))
        {
            scaleIoName = scaleIoName.substring(0, scaleIoName.length() - scaleIONameSuffix.length());
        }

        for (Host host : hosts)
        {
            String hostName = host.getName();
            if (scaleIoName.equals(hostName))
            {
                type = host.getType();
                break;
            }
            else
            {
                //Try to find appending the domain
                String scaleIoNameWithDomain = scaleIoName + "." + hostDomain;
                if (scaleIoNameWithDomain.equals(hostName))
                {
                    type = host.getType();
                    break;
                }
                else
                {
                    LOGGER.warn("ScaleIO Sds name does not match with Host name");
                }
            }
        }

        return type;
    }

    private void setProtectionDomainIdInNodeData(final ScaleIOProtectionDomain scaleIOProtectionDomain,
                                                 final String esxiManagementHostname,
                                                 final String esxiManagementIpAddress, final NodeData nodeData,
                                                 final ScaleIOSDS sds)
    {
        if ((esxiManagementHostname != null && sds.getName().contains(esxiManagementHostname)) ||
            (esxiManagementIpAddress != null && sds.getName().contains(esxiManagementIpAddress)))
        {
            nodeData.setProtectionDomainId(scaleIOProtectionDomain.getId());
        }
        else
        {
            nodeData.setProtectionDomainId("");
        }
    }

    private String getNodeType(final String symphonyUuid)
    {
        final NodeInventory nodeInventory = repository.getNodeInventory(symphonyUuid);

        try
        {
            if (nodeInventory != null && nodeInventory.getNodeInventory() != null)
            {
                final String productName = extractProductNameFromProductNamesForNode(symphonyUuid, nodeInventory);

                return matchProductNameAndReturnNodeType(productName);
            }
        }
        catch (InvalidNameException e)
        {
            LOGGER.error("Error while getting the node inventory data", e);
        }

        return null;
    }

    private String extractProductNameFromProductNamesForNode(final String symphonyUuid,
                                                             final NodeInventory nodeInventory)
            throws InvalidNameException
    {
        final JSONArray productNames = JsonPath.read(nodeInventory.getNodeInventory(),
                                                     "$..data..['System Information']['Product Name']");

        if (org.springframework.util.CollectionUtils.isEmpty(productNames))
        {
            String errorMessage = "No product names found for node " + symphonyUuid;
            LOGGER.error(errorMessage);
            throw new InvalidNameException(errorMessage);
        }

        return (String) productNames.get(0);
    }

    private String matchProductNameAndReturnNodeType(final String productName)
    {
        if (productName.contains(R6_PRODUCT_NAME))
        {
            return NODE_TYPE_1U1N;
        }
        else if (productName.contains(R7_PRODUCT_NAME))
        {
            return NODE_TYPE_2U1N;
        }

        return null;
    }
}