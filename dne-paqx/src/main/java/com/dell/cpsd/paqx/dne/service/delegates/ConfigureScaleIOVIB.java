/**
 * <p>
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries. All Rights Reserved. Dell EMC Confidential/Proprietary Information
 * </p>
 */

package com.dell.cpsd.paqx.dne.service.delegates;

import com.dell.cpsd.paqx.dne.exception.TaskResponseFailureException;
import com.dell.cpsd.paqx.dne.service.NodeService;
import com.dell.cpsd.paqx.dne.service.delegates.model.DelegateRequestModel;
import com.dell.cpsd.paqx.dne.transformers.SoftwareVibRequestTransformer;
import com.dell.cpsd.virtualization.capabilities.api.SoftwareVIBConfigureRequestMessage;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.UUID;

import static com.dell.cpsd.paqx.dne.service.delegates.utils.DelegateConstants.CONFIGURE_SCALEIO_VIB_FAILED;
import static com.dell.cpsd.paqx.dne.service.delegates.utils.DelegateConstants.IOCTL_INI_GUI_STR;

/**
 * Configure ScaleIo Data Client (SDC)
 * <p>
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries. All Rights Reserved. Dell EMC Confidential/Proprietary Information
 * </p>
 *
 * @version 1.0
 * @since 1.0
 */
@Component
@Scope("prototype")
@Qualifier("configureScaleIOVIB")
public class ConfigureScaleIOVIB extends BaseWorkflowDelegate
{
    /**
     * The logger instance
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigureScaleIOVIB.class);

    /**
     * The <code>NodeService</code> instance
     */
    private final NodeService                   nodeService;
    private final SoftwareVibRequestTransformer softwareVibRequestTransformer;

    @Autowired
    public ConfigureScaleIOVIB(final NodeService nodeService, final SoftwareVibRequestTransformer softwareVibRequestTransformer)
    {
        this.nodeService = nodeService;
        this.softwareVibRequestTransformer = softwareVibRequestTransformer;
    }

    @Override
    public void delegateExecute(final DelegateExecution delegateExecution)
    {
        LOGGER.info("Execute Configure ScaleIO VIB task");
        final String taskMessage = "Configure ScaleIO Vib";

        try
        {
            final String ioctlIniGuidStr = UUID.randomUUID().toString();
            delegateExecution.setVariable(IOCTL_INI_GUI_STR, ioctlIniGuidStr);
            final DelegateRequestModel<SoftwareVIBConfigureRequestMessage> delegateRequestModel = softwareVibRequestTransformer
                    .buildConfigureSoftwareVibRequest(delegateExecution);

            this.nodeService.requestConfigureScaleIoVib(delegateRequestModel.getRequestMessage());

            final String returnMessage = taskMessage + " on Node " + delegateRequestModel.getServiceTag() + " was successful.";
            LOGGER.info(returnMessage);
            updateDelegateStatus(returnMessage);
        }
        catch (TaskResponseFailureException ex)
        {
            updateDelegateStatus(ex.getMessage());
            throw new BpmnError(CONFIGURE_SCALEIO_VIB_FAILED, "Exception Code: " + ex.getCode() + "::" + ex.getMessage());
        }
        catch (Exception ex)
        {
            String errorMessage = "An unexpected exception occurred attempting to request " + taskMessage + ". Reason: ";
            LOGGER.error(errorMessage, ex);
            updateDelegateStatus(errorMessage + ex.getMessage());
            throw new BpmnError(CONFIGURE_SCALEIO_VIB_FAILED, errorMessage + ex.getMessage());
        }
    }
}
