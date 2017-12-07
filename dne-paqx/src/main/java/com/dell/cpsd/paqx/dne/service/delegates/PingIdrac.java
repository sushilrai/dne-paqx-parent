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

        super(LOGGER, "Ping iDrac");
        this.pingIdracWaitTime = pingIdracWaitTime;
    }

    @Override
    public void delegateExecute(final DelegateExecution delegateExecution)
    {
        NodeDetail nodeDetail = (NodeDetail) delegateExecution.getVariable(NODE_DETAIL);
        updateDelegateStatus("Attempting " + this.taskName + " on Node " + nodeDetail.getServiceTag() + ".");

        String idracIpAddress = nodeDetail.getIdracIpAddress();
        boolean isReachableResult = false;

        try
        {
            isReachableResult = isIdracReachable(idracIpAddress);
        }
        catch (UnknownHostException e)
        {
            final String message = "Unable to determine iDRAC IP Address for Node " + nodeDetail.getServiceTag() + ". Reason: ";
            updateDelegateStatus(message, e);
            throw new BpmnError(PING_IP_ADDRESS_FAILED, message + e.getMessage());
        }
        catch (IOException e)
        {
            final String message = "Unable to reach iDRAC IP Address for Node " + nodeDetail.getServiceTag() + ". Reason: ";
            updateDelegateStatus(message, e);
            throw new BpmnError(PING_IP_ADDRESS_FAILED, message + e.getMessage());
        }
        catch (Exception e)
        {
            final String message =
                    "An unexpected exception occurred attempting to ping iDRAC IP Address for Node " + nodeDetail.getServiceTag() + ". Reason: ";
            updateDelegateStatus(message, e);
            throw new BpmnError(PING_IP_ADDRESS_FAILED, message + e.getMessage());
        }

        if (!isReachableResult)
        {
            final String message = "Unable to contact iDRAC IP Address for Node " + nodeDetail.getServiceTag() + ".";
            updateDelegateStatus(message);
            throw new BpmnError(PING_IP_ADDRESS_FAILED, message);
        }

        updateDelegateStatus("Ping iDRAC IP Address for Node " + nodeDetail.getServiceTag() + " was successful.");
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
