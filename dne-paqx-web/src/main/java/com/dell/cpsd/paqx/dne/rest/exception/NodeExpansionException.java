/**
 * This is the node expansion base exception.
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries.  All Rights Reserved.
 * VCE Confidential/Proprietary Information
 */

package com.dell.cpsd.paqx.dne.rest.exception;

public class NodeExpansionException extends Exception
{
    /*
     * The serial version uuid
     */
    private static final long serialVersionUID = 965046523232760658L;
    
    /*
     * The error code.
     */
    private String            code;

    
    /**
     * NodeExpansionException constructor.
     * 
     * @param   code    The error code
     * @param   message The message
     * 
     * @since   1.0
     */
    public NodeExpansionException(final String code, final String message)
    {
        super(message);
        this.code = code;
    }

    
    /**
     * NodeExpansionException constructor.
     * 
     * @param   code    The error code
     * @param   message The message
     * @param   cause   The cause
     * 
     * @since   1.0
     */
    public NodeExpansionException(final String code, final String message, final Throwable cause)
    {
        super(message, cause);
        this.code = code;
    }

    /**
     * This returns the error code.
     * 
     * @return  The error code.
     * 
     * @since   1.0
     */
    public String getCode()
    {
        return code;
    }

    
    /**
     * This sets the error code.
     * 
     * @param   code    The error code.
     * 
     * @since   1.0
     */
    public void setCode(final String code)
    {
        this.code = code;
    }

    
    /**
     * {@inheritDoc}
     */
    @Override
    public String toString()
    {
        final StringBuilder builder = new StringBuilder();
        
        builder.append("NodeExpansionException [").append(code).append("]");
        builder.append(" : ").append(this.getMessage());
        
        return builder.toString();
    }

}
