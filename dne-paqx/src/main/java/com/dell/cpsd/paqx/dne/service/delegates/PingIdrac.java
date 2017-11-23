/**
 * <p>
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries. All Rights Reserved. Dell EMC Confidential/Proprietary Information
 * </p>
 */

package com.dell.cpsd.paqx.dne.service.delegates;

import com.dell.cpsd.paqx.dne.service.delegates.model.NodeDetail;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import static com.dell.cpsd.paqx.dne.service.delegates.utils.DelegateConstants.NODE_DETAIL;
import static com.dell.cpsd.paqx.dne.service.delegates.utils.DelegateConstants.PING_IP_ADDRESS_FAILED;

@Component
@Scope("prototype")
@Qualifier("pingIdrac")
public class PingIdrac extends BaseWorkflowDelegate
{
    /**
     * The <code>Logger</code> instance
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(PingIdrac.class);

    /**
     * Ping response wait time
     */
    private final int pingIdracWaitTime;

    /**
     * Default constructor
     */
    public PingIdrac()
    {
        this(120000);// 120 seconds
    }

    /**
     * @param pingIdracWaitTime
     */
    public PingIdrac(int pingIdracWaitTime)
    {
        this.pingIdracWaitTime = pingIdracWaitTime;
    }

    @Override
    public void delegateExecute(final DelegateExecution delegateExecution)
    {
        LOGGER.info("Execute Ping iDrac");
        updateDelegateStatus("Attempting to Ping iDrac");

        NodeDetail nodeDetail = (NodeDetail) delegateExecution.getVariable(NODE_DETAIL);
        String idracIpAddress = nodeDetail.getIdracIpAddress();
        boolean isReachableResult = false;

        try
        {
            isReachableResult = isIdracReachable(idracIpAddress);
        }
        catch (UnknownHostException e)
        {
            final String message = "Unable to determine iDRAC IP Address for Node " + nodeDetail.getServiceTag() + ".";
            LOGGER.error(message, e);
            updateDelegateStatus(message);
            throw new BpmnError(PING_IP_ADDRESS_FAILED, message + " Reason: " + e.getMessage());
        }
        catch (IOException e)
        {
            final String message = "Unable to reach iDRAC IP Address for Node " + nodeDetail.getServiceTag() + ".";
            LOGGER.error(message, e);
            updateDelegateStatus(message);
            throw new BpmnError(PING_IP_ADDRESS_FAILED, message + " Reason: " + e.getMessage());
        }
        catch (Exception e)
        {
            final String message =
                    "An unexpected exception occurred attempting to ping iDRAC IP Address for Node " + nodeDetail.getServiceTag();
            LOGGER.error(message, e);
            updateDelegateStatus(message);
            throw new BpmnError(PING_IP_ADDRESS_FAILED, message);
        }

        if (!isReachableResult)
        {
            final String message = "Unable to contact iDRAC IP Address for Node " + nodeDetail.getServiceTag() + ".";
            LOGGER.error(message);
            updateDelegateStatus(message);
            throw new BpmnError(PING_IP_ADDRESS_FAILED, message);
        }

        final String message = "Ping iDRAC IP Address for Node " + nodeDetail.getServiceTag() + " was successful.";
        LOGGER.info(message);
        updateDelegateStatus(message);
    }

    /**
     * @param idracIpAddress
     * @return
     */
    protected boolean isIdracReachable(String idracIpAddress) throws IOException
    {
        return InetAddress.getByName(idracIpAddress).isReachable(pingIdracWaitTime);
    }
}
