/**
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries.  All Rights Reserved.
 * Dell EMC Confidential/Proprietary Information
 */

package com.dell.cpsd.paqx.dne.domain.inventory;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;

/**
 * Model class for node Rackhd node inventory
 * <p>
 * <p>
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries.  All Rights Reserved.
 * Dell EMC Confidential/Proprietary Information
 * </p>
 *
 * @since 1.0
 */
@Entity
public class NodeInventory
{
    @Id
    @Column(name = "SYMPHONY_UUID", unique = true, nullable = false)
    private String symphonyUUID;

    @Lob
    @Column(name = "NODE_INVENTORY")
    private String nodeInventory;

    public NodeInventory(){}

    public NodeInventory(String symphonyUUID, String nodeInventory)
    {
        this.symphonyUUID = symphonyUUID;
        this.nodeInventory = nodeInventory;
    }

    public String getSymphonyUUID()
    {
        return symphonyUUID;
    }

    public void setSymphonyUUID(final String symphonyUUID)
    {
        this.symphonyUUID = symphonyUUID;
    }

    public String getNodeInventory()
    {
        return nodeInventory;
    }

    public void setNodeInventory(final String nodeInventory)
    {
        this.nodeInventory = nodeInventory;
    }

    @Override
    public String toString()
    {
        return "NodeInventory{" + " symphonyUUID='" + symphonyUUID + '\'' + ", nodeInventory='" + nodeInventory + '\'' + '}';
    }
}
