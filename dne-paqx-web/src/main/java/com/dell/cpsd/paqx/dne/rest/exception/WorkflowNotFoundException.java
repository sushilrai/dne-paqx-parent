/**
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries.  All Rights Reserved.
 * VCE Confidential/Proprietary Information
 */

package com.dell.cpsd.paqx.dne.rest.exception;

import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = org.springframework.http.HttpStatus.NOT_FOUND, reason = "No workflow found.")
public class WorkflowNotFoundException extends NodeExpansionException
{
    /*
     * The serial version uuid
     */
    private static final long serialVersionUID = 8678731346050136776L;

    
    /**
     * WorkflowNotFoundException constructor.
     * 
     * @since   1.0
     */
    public WorkflowNotFoundException()
    {
        this("NOT_FOUND", "No workflow found.");
    }


    /**
     * WorkflowNotFoundException constructor.
     * 
     * @param   message The message
     * 
     * @since   1.0
     */
    public WorkflowNotFoundException(final String message)
    {
        this("NOT_FOUND", message);
    }

    
    /**
     * WorkflowNotFoundException constructor.
     * 
     * @param   code    The error code
     * @param   message The message
     * 
     * @since   1.0
     */
    public WorkflowNotFoundException(final String code, final String message)
    {
        super(code, message);
    }

    
    /**
     * WorkflowNotFoundException constructor.
     * 
     * @param   code    The error code
     * @param   message The message
     * @param   cause   The cause
     * 
     * @since   1.0
     */
    public WorkflowNotFoundException(final String code, final String message, final Throwable cause)
    {
        super(code, message, cause);
    }
}
