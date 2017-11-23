/**
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries.  All Rights Reserved.
 * Dell EMC Confidential/Proprietary Information
 */

package com.dell.cpsd.paqx.dne.exception;

/**
 * Task response failure exception is thrown when a response
 * is received from the other services with failed status. The
 * exception message will be the same as received in the response
 * message in the description property.
 * <p>
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries. All Rights Reserved.
 * Dell EMC Confidential/Proprietary Information
 * </p>
 *
 * @version 1.0
 * @since 1.0
 */
public class TaskResponseFailureException extends Exception
{
    private static final long serialVersionUID = 237177173229733497L;

    private int code;

    public TaskResponseFailureException(final int code, final String message)
    {
        super(message);
        this.code = code;
    }

    public TaskResponseFailureException(final int code, final String message, final Throwable cause)
    {
        super(message, cause);
        this.code = code;
    }

    /**
     * This returns the error code.
     *
     * @return The error code.
     * @since 1.0
     */
    public int getCode()
    {
        return code;
    }

    /**
     * This sets the error code.
     *
     * @param code The error code.
     * @since 1.0
     */
    public void setCode(final int code)
    {
        this.code = code;
    }
}
