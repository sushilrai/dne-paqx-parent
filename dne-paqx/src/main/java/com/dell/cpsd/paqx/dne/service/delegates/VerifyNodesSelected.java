/**
 * <p>
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries. All Rights Reserved. Dell EMC Confidential/Proprietary Information
 * </p>
 */

package com.dell.cpsd.paqx.dne.service.delegates;

import com.dell.cpsd.paqx.dne.domain.node.DiscoveredNodeInfo;
import com.dell.cpsd.paqx.dne.domain.scaleio.ScaleIOSDC;
import com.dell.cpsd.paqx.dne.domain.scaleio.ScaleIOSDS;
import com.dell.cpsd.paqx.dne.domain.vcenter.Host;
import com.dell.cpsd.paqx.dne.repository.DataServiceRepository;
import com.dell.cpsd.paqx.dne.service.NodeService;
import com.dell.cpsd.paqx.dne.service.delegates.model.NodeDetail;
import com.dell.cpsd.paqx.dne.service.delegates.utils.DelegateConstants;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.dell.cpsd.paqx.dne.service.delegates.utils.DelegateConstants.VERIFY_NODES_SELECTED_FAILED;

@Component
@Scope("prototype")
@Qualifier("verifyNodesSelected")
public class VerifyNodesSelected extends BaseWorkflowDelegate
{
    private static final Logger LOGGER = LoggerFactory.getLogger(VerifyNodesSelected.class);

    private final NodeService nodeService;

    private final DataServiceRepository repository;

    @Autowired
    public VerifyNodesSelected(final NodeService nodeService, final DataServiceRepository repository)
    {
        super(LOGGER, "Verify Selected Nodes");
        this.nodeService = nodeService;
        this.repository = repository;
    }

    @Override
    public void delegateExecute(final DelegateExecution delegateExecution)
    {
        updateDelegateStatus("Attempting to verify selected Nodes are still available.");

        List<NodeDetail> nodeDetails = (List<NodeDetail>) delegateExecution.getVariable(DelegateConstants.NODE_DETAILS);
        if (CollectionUtils.isEmpty(nodeDetails))
        {
            final String message = "The List of Node Detail was not found!  Please add at least one Node Detail and try again.";
            updateDelegateStatus(message);
            throw new BpmnError(VERIFY_NODES_SELECTED_FAILED, message);
        }

        final List<NodeDetail> provisioned = new ArrayList<>();
        try
        {
            List<DiscoveredNodeInfo> discoveredNodes = nodeService.listDiscoveredNodeInfo();

            discoveredNodes.stream().forEach(discoveredNode -> {
                nodeDetails.stream().forEach(nodeDetail -> {
                    if (nodeDetail.getServiceTag().equalsIgnoreCase(discoveredNode.getSerialNumber()))
                    {
                        final List<Host> vCenterHosts = repository.getVCenterHosts();
                        final Optional<Host> foundHost = vCenterHosts.stream().filter(Objects::nonNull).filter(
                                host -> itemContainsString(host.getName(), nodeDetail.getEsxiManagementHostname()) ||
                                        itemContainsString(host.getName(), nodeDetail.getEsxiManagementIpAddress()))
                                                                     .findFirst();

                        if (foundHost.isPresent())
                        {
                            LOGGER.error("Node [{}] was found in VCenter hosts", nodeDetail.getServiceTag());
                            provisioned.add(nodeDetail);
                        }
                        else
                        {
                            final Optional<ScaleIOSDC> foundSdc = repository.getScaleIoData().getSdcList().stream()
                                                                            .filter(Objects::nonNull).filter(
                                            sdc -> itemContainsString(sdc.getName(),
                                                                      nodeDetail.getEsxiManagementHostname()) ||
                                                   itemContainsString(sdc.getName(),
                                                                      nodeDetail.getEsxiManagementIpAddress()))
                                                                            .findFirst();

                            if (foundSdc.isPresent())
                            {
                                LOGGER.error("Node [{}] was found in ScaleIO SDC Data", nodeDetail.getServiceTag());
                                provisioned.add(nodeDetail);
                            }
                            else
                            {
                                final Optional<ScaleIOSDS> foundSds = repository.getScaleIoData().getSdsList().stream()
                                                                                .filter(Objects::nonNull).filter(
                                                sds -> itemContainsString(sds.getName(),
                                                                          nodeDetail.getEsxiManagementHostname()) ||
                                                       itemContainsString(sds.getName(),
                                                                          nodeDetail.getEsxiManagementIpAddress()))
                                                                                .findFirst();

                                if (foundSds.isPresent())
                                {
                                    LOGGER.error("Node [{}] was found in ScaleIO SDS Data", nodeDetail.getServiceTag());
                                    provisioned.add(nodeDetail);
                                }
                            }
                        }
                    }
                });
            });
        }
        catch (Exception e)
        {
            final String message = "An unexpected exception occurred attempting to verify selected Nodes.";
            updateDelegateStatus(message, e);
            throw new BpmnError(VERIFY_NODES_SELECTED_FAILED, message);
        }
        if (CollectionUtils.isNotEmpty(provisioned))
        {
            final String message =
                    "The following Nodes have already been added.  Please remove the Nodes from the request and try again.  Nodes currently in use: " +
                    StringUtils.join(provisioned.stream().map(NodeDetail::getServiceTag).collect(Collectors.toList()),
                                     ", ");
            updateDelegateStatus(message);
            throw new BpmnError(VERIFY_NODES_SELECTED_FAILED, message);
        }

        updateDelegateStatus("All selected Nodes are available.");
    }

    protected boolean itemContainsString(String item, String substring)
    {
        return StringUtils.isNotEmpty(item) && StringUtils.isNotEmpty(substring) && item.matches(
                ".*\\b" + substring + "\\b.*");
    }
}
