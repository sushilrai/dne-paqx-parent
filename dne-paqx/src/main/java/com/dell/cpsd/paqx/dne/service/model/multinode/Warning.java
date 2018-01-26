/**
 * <p>
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries.  All Rights Reserved.
 * Dell EMC Confidential/Proprietary Information
 * </p>
 */

package com.dell.cpsd.paqx.dne.service.model.multinode;

public class Warning
{
    private String warningCode;
    private String warningMessage;

    public String getWarningCode()
    {
        return warningCode;
    }

    public void setWarningCode(final String warningCode)
    {
        this.warningCode = warningCode;
    }

    public String getWarningMessage()
    {
        return warningMessage;
    }

    public void setWarningMessage(final String warningMessage)
    {
        this.warningMessage = warningMessage;
    }

    @Override
    public boolean equals(final Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (!(o instanceof Warning))
        {
            return false;
        }

        final Warning error = (Warning) o;

        if (getWarningCode() != null ? !getWarningCode().equals(error.getWarningCode()) : error.getWarningCode() != null)
        {
            return false;
        }
        return getWarningMessage() != null ? getWarningMessage().equals(error.getWarningMessage()) : error.getWarningMessage() == null;
    }

    @Override
    public int hashCode()
    {
        int result = getWarningCode() != null ? getWarningCode().hashCode() : 0;
        result = 31 * result + (getWarningMessage() != null ? getWarningMessage().hashCode() : 0);
        return result;
    }
}
