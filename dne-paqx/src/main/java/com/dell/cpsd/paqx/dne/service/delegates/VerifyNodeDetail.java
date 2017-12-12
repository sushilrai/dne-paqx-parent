/**
 * <p>
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries. All Rights Reserved. Dell EMC Confidential/Proprietary Information
 * </p>
 */

package com.dell.cpsd.paqx.dne.service.delegates;

import com.dell.cpsd.paqx.dne.repository.DataServiceRepository;
import com.dell.cpsd.paqx.dne.service.delegates.model.NodeDetail;
import com.dell.cpsd.paqx.dne.service.IpAddressValidator;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.dell.cpsd.paqx.dne.service.delegates.utils.DelegateConstants.NODE_DETAIL;
import static com.dell.cpsd.paqx.dne.service.delegates.utils.DelegateConstants.VERIFY_NODE_DETAIL_FAILED;

@Component
@Scope("prototype")
@Qualifier("verifyNodeDetail")
public class VerifyNodeDetail extends BaseWorkflowDelegate
{
    private static final Logger LOGGER = LoggerFactory.getLogger(VerifyNodeDetail.class);

    private final DataServiceRepository repository;
    private final IpAddressValidator    validator;

    public VerifyNodeDetail(final DataServiceRepository repository, final IpAddressValidator validator)
    {
        super(LOGGER, "Verify Node Detail");

        this.repository = repository;
        this.validator = validator;
    }

    @Override
    public void delegateExecute(final DelegateExecution delegateExecution)
    {
        final NodeDetail nodeDetail = (NodeDetail) delegateExecution.getVariable(NODE_DETAIL);

        if (nodeDetail == null)
        {
            final String message = "Node details were not found!  Please add Node details and try again.";
            updateDelegateStatus(message);
            throw new BpmnError(VERIFY_NODE_DETAIL_FAILED, message);
        }

        updateDelegateStatus("Attempting " + this.taskName + " on Node " + nodeDetail.getServiceTag() + ".");
        List<String> missingFields = new ArrayList<>();

        if (StringUtils.isBlank(nodeDetail.getId()))
        {
            missingFields.add("id");
        }
        if (StringUtils.isBlank(nodeDetail.getServiceTag()))
        {
            missingFields.add("serviceTag");
        }
        if (StringUtils.isBlank(nodeDetail.getIdracIpAddress()))
        {
            missingFields.add("idracIpAddress");
        }
        if (StringUtils.isBlank(nodeDetail.getIdracGatewayIpAddress()))
        {
            missingFields.add("idracGatewayIpAddress");
        }
        if (StringUtils.isBlank(nodeDetail.getIdracSubnetMask()))
        {
            missingFields.add("idracSubnetMask");
        }
        if (StringUtils.isBlank(nodeDetail.getEsxiManagementIpAddress()))
        {
            missingFields.add("esxiManagementIpAddress");
        }
        if (StringUtils.isBlank(nodeDetail.getEsxiManagementGatewayIpAddress()))
        {
            missingFields.add("esxiManagementGatewayIpAddress");
        }
        if (StringUtils.isBlank(nodeDetail.getEsxiManagementSubnetMask()))
        {
            missingFields.add("esxiManagementSubnetMask");
        }
        if (StringUtils.isBlank(nodeDetail.getEsxiManagementHostname()))
        {
            missingFields.add("esxiManagementHostname");
        }
        if (StringUtils.isBlank(nodeDetail.getScaleIoData1SvmIpAddress()))
        {
            missingFields.add("scaleIoData1SvmIpAddress");
        }
        if (StringUtils.isBlank(nodeDetail.getScaleIoData1SvmSubnetMask()))
        {
            missingFields.add("scaleIoData1SvmSubnetMask");
        }
        if (StringUtils.isBlank(nodeDetail.getScaleIoData2SvmIpAddress()))
        {
            missingFields.add("scaleIoData2SvmIpAddress");
        }
        if (StringUtils.isBlank(nodeDetail.getScaleIoData2SvmSubnetMask()))
        {
            missingFields.add("scaleIoData2SvmSubnetMask");
        }
        if (StringUtils.isBlank(nodeDetail.getScaleIoData1EsxIpAddress()))
        {
            missingFields.add("scaleIoData1EsxIpAddress");
        }
        if (StringUtils.isBlank(nodeDetail.getScaleIoData1EsxSubnetMask()))
        {
            missingFields.add("scaleIoData1EsxSubnetMask");
        }
        if (StringUtils.isBlank(nodeDetail.getScaleIoData2EsxIpAddress()))
        {
            missingFields.add("scaleIoData2EsxIpAddress");
        }
        if (StringUtils.isBlank(nodeDetail.getScaleIoData2EsxSubnetMask()))
        {
            missingFields.add("scaleIoData2EsxSubnetMask");
        }
        if (StringUtils.isBlank(nodeDetail.getScaleIoSvmManagementIpAddress()))
        {
            missingFields.add("scaleIoSvmManagementIpAddress");
        }
        if (StringUtils.isBlank(nodeDetail.getScaleIoSvmManagementGatewayAddress()))
        {
            missingFields.add("scaleIoSvmManagementGatewayAddress");
        }
        if (StringUtils.isBlank(nodeDetail.getScaleIoSvmManagementSubnetMask()))
        {
            missingFields.add("scaleIoSvmManagementSubnetMask");
        }
        if (StringUtils.isBlank(nodeDetail.getClusterName()))
        {
            missingFields.add("clusterName");
        }
        if (StringUtils.isBlank(nodeDetail.getvMotionManagementIpAddress()))
        {
            missingFields.add("vMotionManagementIpAddress");
        }
        if (StringUtils.isBlank(nodeDetail.getvMotionManagementSubnetMask()))
        {
            missingFields.add("vMotionManagementSubnetMask");
        }

        if (CollectionUtils.isNotEmpty(missingFields))
        {
            final String message =
                    "Node details are incomplete!  Please update Node details with the following information and try again.  Missing values for "
                            + StringUtils.join(missingFields, ", ") + ".";
            updateDelegateStatus(message);
            throw new BpmnError(VERIFY_NODE_DETAIL_FAILED, message);
        }

        validateIpAddresses(nodeDetail);

        updateDelegateStatus("Verification of Details on Node " + nodeDetail.getServiceTag() + " was successful.");
    }

    private void validateIpAddresses(NodeDetail nodeDetail)
    {
        IpAddressValidator.IpAddress idracSubnetMask = new IpAddressValidator.IpAddress(nodeDetail.getIdracSubnetMask(),
                "iDRAC Subnet Mask");
        IpAddressValidator.IpAddress idracIpAddress = new IpAddressValidator.IpAddress(nodeDetail.getIdracIpAddress(), "iDRAC IP Address",
                idracSubnetMask);
        IpAddressValidator.IpAddress idracGateway = new IpAddressValidator.IpAddress(nodeDetail.getIdracGatewayIpAddress(),
                "iDRAC Gateway");

        IpAddressValidator.IpAddress esxiSubnetMask = new IpAddressValidator.IpAddress(nodeDetail.getEsxiManagementSubnetMask(),
                "ESXi Subnet Mask");
        IpAddressValidator.IpAddress esxiIpAddress = new IpAddressValidator.IpAddress(nodeDetail.getEsxiManagementIpAddress(),
                "ESXi IP Address", esxiSubnetMask);
        IpAddressValidator.IpAddress esxiGateway = new IpAddressValidator.IpAddress(nodeDetail.getEsxiManagementGatewayIpAddress(),
                "ESXi Gateway IP Address");

        IpAddressValidator.IpAddress scaleioSvmSubnetMask = new IpAddressValidator.IpAddress(nodeDetail.getScaleIoSvmManagementSubnetMask(),
                "ScaleIO SVM Subnet Mask");
        IpAddressValidator.IpAddress scaleioSvmIpAddress = new IpAddressValidator.IpAddress(nodeDetail.getScaleIoSvmManagementIpAddress(),
                "ScaleIO SVM IP Address", scaleioSvmSubnetMask);
        IpAddressValidator.IpAddress scaleioSvmGateway = new IpAddressValidator.IpAddress(
                nodeDetail.getScaleIoSvmManagementGatewayAddress(), "ScaleIO SVM Gateway IP Address");

        IpAddressValidator.IpAddress scaleioData1EsxSubnetMask = new IpAddressValidator.IpAddress(nodeDetail.getScaleIoData1EsxSubnetMask(),
                "ScaleIO Data1 ESX Subnet Mask");
        IpAddressValidator.IpAddress scaleioData1EsxIpAddress = new IpAddressValidator.IpAddress(nodeDetail.getScaleIoData1EsxIpAddress(),
                "ScaleIO Data1 ESX IP Address", scaleioData1EsxSubnetMask);

        IpAddressValidator.IpAddress scaleioData2EsxSubnetMask = new IpAddressValidator.IpAddress(nodeDetail.getScaleIoData2EsxSubnetMask(),
                "ScaleIO Data2 ESX Subnet Mask");
        IpAddressValidator.IpAddress scaleioData2EsxIpAddress = new IpAddressValidator.IpAddress(nodeDetail.getScaleIoData2EsxIpAddress(),
                "ScaleIO Data2 ESX IP Address", scaleioData2EsxSubnetMask);

        IpAddressValidator.IpAddress scaleioData1SvmSubnetMask = new IpAddressValidator.IpAddress(nodeDetail.getScaleIoData1SvmSubnetMask(),
                "ScaleIO Data1 SVM Subnet Mask");
        IpAddressValidator.IpAddress scaleioData1SvmIpAddress = new IpAddressValidator.IpAddress(nodeDetail.getScaleIoData1SvmIpAddress(),
                "ScaleIO Data1 SVM IP Address", scaleioData1SvmSubnetMask);

        IpAddressValidator.IpAddress scaleioData2SvmSubnetMask = new IpAddressValidator.IpAddress(nodeDetail.getScaleIoData2SvmSubnetMask(),
                "ScaleIO Data2 SVM Subnet Mask");
        IpAddressValidator.IpAddress scaleioData2SvmIpAddress = new IpAddressValidator.IpAddress(nodeDetail.getScaleIoData2SvmIpAddress(),
                "ScaleIO Data2 SVM IP Address", scaleioData2SvmSubnetMask);

        IpAddressValidator.IpAddress vMotionSubnetMask = new IpAddressValidator.IpAddress(nodeDetail.getvMotionManagementSubnetMask(),
                "vMotion Management Subnet Mask");
        IpAddressValidator.IpAddress vMotionIpAddress = new IpAddressValidator.IpAddress(nodeDetail.getvMotionManagementIpAddress(),
                "vMotion Management Ip Address", vMotionSubnetMask);

        validateIpAddressesFormat(idracSubnetMask, idracIpAddress, idracGateway, esxiSubnetMask, esxiIpAddress, esxiGateway,
                scaleioSvmSubnetMask, scaleioSvmIpAddress, scaleioSvmGateway, scaleioData1EsxSubnetMask, scaleioData1EsxIpAddress,
                scaleioData2EsxSubnetMask, scaleioData2EsxIpAddress, scaleioData1SvmSubnetMask, scaleioData1SvmIpAddress,
                scaleioData2SvmSubnetMask, scaleioData2SvmIpAddress, vMotionSubnetMask, vMotionIpAddress);

        validateIpAddressesNotDuplicated(idracIpAddress, esxiIpAddress, scaleioSvmIpAddress, scaleioData1EsxIpAddress,
                scaleioData2EsxIpAddress, scaleioData1SvmIpAddress, scaleioData2SvmIpAddress, vMotionIpAddress);

        validateIpAddressesAreInRange(idracIpAddress, esxiIpAddress, scaleioSvmIpAddress, scaleioData1EsxIpAddress,
                scaleioData2EsxIpAddress, scaleioData1SvmIpAddress, scaleioData2SvmIpAddress, vMotionIpAddress);

        validateIpAddressesNotAlreadyInUse(idracIpAddress, esxiIpAddress, scaleioSvmIpAddress, scaleioData1EsxIpAddress,
                scaleioData2EsxIpAddress, scaleioData1SvmIpAddress, scaleioData2SvmIpAddress, vMotionIpAddress);
    }

    private void validateIpAddressesFormat(IpAddressValidator.IpAddress... ipAddresses)
    {
        List<String> invalidAddresses = new ArrayList<>();

        for (IpAddressValidator.IpAddress ipAddress : ipAddresses)
        {
            if (validator.isNotIpv4Format(ipAddress))
            {
                invalidAddresses.add(ipAddress.getLabel());
            }
        }

        if (CollectionUtils.isNotEmpty(invalidAddresses))
        {
            final String message =
                    "Node details are invalid!  Please correct Node details with the following information and try again. IP Address formats not valid: "
                            + StringUtils.join(invalidAddresses, ", ") + ".";
            updateDelegateStatus(message);
            throw new BpmnError(VERIFY_NODE_DETAIL_FAILED, message);
        }
    }

    private void validateIpAddressesNotDuplicated(IpAddressValidator.IpAddress... ipAddresses)
    {
        Set<String> uniqueIpAddresses = new HashSet<>();
        List<String> invalidAddresses = new ArrayList<>();

        for (IpAddressValidator.IpAddress ipAddress : ipAddresses)
        {
            if (!uniqueIpAddresses.add(ipAddress.getIpAddress()))
            {
                invalidAddresses.add(ipAddress.getLabel());
            }
        }

        if (CollectionUtils.isNotEmpty(invalidAddresses))
        {
            final String message =
                    "Node details are invalid!  Please correct Node details with the following information and try again. IP Addresses duplicated: "
                            + StringUtils.join(invalidAddresses, ", ") + ".";
            updateDelegateStatus(message);
            throw new BpmnError(VERIFY_NODE_DETAIL_FAILED, message);
        }
    }

    private void validateIpAddressesAreInRange(IpAddressValidator.IpAddress... ipAddresses)
    {
        List<String> invalidAddresses = new ArrayList<>();

        for (IpAddressValidator.IpAddress ipAddress : ipAddresses)
        {
            if (validator.isNotInRange(ipAddress))
            {
                invalidAddresses.add(ipAddress.getLabel());
            }
        }

        if (CollectionUtils.isNotEmpty(invalidAddresses))
        {
            final String message =
                    "Node details are invalid!  Please correct Node details with the following information and try again. IP Address not in subnet range: "
                            + StringUtils.join(invalidAddresses, ", ") + ".";
            updateDelegateStatus(message);
            throw new BpmnError(VERIFY_NODE_DETAIL_FAILED, message);
        }
    }

    private void validateIpAddressesNotAlreadyInUse(IpAddressValidator.IpAddress... ipAddresses)
    {
        List<String> invalidAddresses = new ArrayList<>();

        for (IpAddressValidator.IpAddress ipAddress : ipAddresses)
        {
            if (validator.isInUse(ipAddress))
            {
                invalidAddresses.add(ipAddress.getLabel());
            }
        }

        if (CollectionUtils.isNotEmpty(invalidAddresses))
        {
            final String message =
                    "Node details are invalid!  Please correct Node details with the following information and try again. IP Address already in use: "
                            + StringUtils.join(invalidAddresses, ", ") + ".";
            updateDelegateStatus(message);
            throw new BpmnError(VERIFY_NODE_DETAIL_FAILED, message);
        }
    }
}
