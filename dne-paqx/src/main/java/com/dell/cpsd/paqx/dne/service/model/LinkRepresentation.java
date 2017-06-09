/**
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries.  All Rights Reserved.
 * Dell EMC Confidential/Proprietary Information
 */

package com.dell.cpsd.paqx.dne.service.model;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class LinkRepresentation
{
    /*
     * The relationship
     */
    private String rel  = "";
    
    /*
     * The URI for the link
     */
    private String href = "";
    
    /*
     * The content type
     */
    private String type = "";
    
    /*
     * The HTTP method
     */
    private String method = "";


    /**
     * LinkRepresentation constructor.
     * 
     * @param   rel     The relationship
     * @param   href    The URI for the link
     * @param   type    The content type
     * @param   method  The HTTP method.
     * 
     * @since   1.0
     */
    public LinkRepresentation(final String rel, final String href, final String type, final String httpMethod)
    {
        this.rel = rel;
        this.type = type;
        this.href = href;
        this.method = httpMethod;
    }

    
    /**
     * This returns the relationship.
     * 
     * @return  The relationship.
     * 
     * @since   1.0
     */
    public String getRel()
    {
        return rel;
    }

    
    /**
     * This sets the relationship.
     * 
     * @param   rel  The relationship.
     * 
     * @since   1.0
     */
    public void setRel(final String rel)
    {
        this.rel = rel;
    }

    
    /**
     * This returns the link URI.
     * 
     * @return  The link URI.
     * 
     * @since   1.0
     */
    public String getHref()
    {
        return href;
    }

    
    /**
     * This sets the link URI.
     * 
     * @param   href  The link URI.
     * 
     * @since   1.0
     */
    public void setHref(final String href)
    {
        this.href = href;
    }

    
    /**
     * This returns the content type.
     * 
     * @return  The content type.
     * 
     * @since   1.0
     */
    public String getType()
    {
        return type;
    }

    
    /**
     * This sets the content type.
     * 
     * @param   type  The content type.
     * 
     * @since   1.0
     */
    public void setType(final String type)
    {
        this.type = type;
    }

    
    /**
     * This returns the HTTP method.
     * 
     * @return  The HTTP method.
     * 
     * @since   1.0
     */
    public String getMethod()
    {
        return method;
    }

 
    /**
     * This sets the HTTP method.
     * 
     * @param   method  The HTTP method.
     * 
     * @since   1.0
     */
    public void setMethod(final String method)
    {
        this.method = method;
    }

    
    /**
     * {@inheritDoc}
     */
    @Override 
    public boolean equals(final Object o)
    {
        if (this == o)
        {
            return true;
        }

        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        final LinkRepresentation that = (LinkRepresentation) o;

        return new EqualsBuilder()
                .append(rel, that.rel)
                .append(type, that.type)
                .append(href, that.href)
                .append(method, that.method)
                .isEquals();
    }

    /**
     * {@inheritDoc}
     */
    @Override 
    public int hashCode()
    {
        return new HashCodeBuilder(17, 37)
                .append(rel)
                .append(type)
                .append(href)
                .append(method)
                .toHashCode();
    }
}
