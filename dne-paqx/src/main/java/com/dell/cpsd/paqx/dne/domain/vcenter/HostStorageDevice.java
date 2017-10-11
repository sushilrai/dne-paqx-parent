package com.dell.cpsd.paqx.dne.domain.vcenter;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "HOST_STORAGE_DEVICE")
public class HostStorageDevice
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "UUID", unique = true, nullable = false)
    private Long uuid;

    @Column(name = "DISPLAY_NAME")
    private String displayName;

    @Column(name = "ssd")
    private boolean ssd;

    @Column(name = "SERIAL_NUMBER")
    private String serialNumber;

    @ManyToOne(cascade = CascadeType.ALL)
    private Host host;

    @Column(name = "CANONICAL_NAME")
    private String canonicalName;

    public HostStorageDevice()
    {

    }

    public HostStorageDevice(final String displayName, final boolean ssd, final String serialNumber, final String canonicalName)
    {
        this.displayName = displayName;
        this.ssd = ssd;
        this.serialNumber = serialNumber;
        this.canonicalName = canonicalName;
    }

    public Long getUuid()
    {
        return uuid;
    }

    public void setUuid(final Long uuid)
    {
        this.uuid = uuid;
    }

    public String getDisplayName()
    {
        return displayName;
    }

    public void setDisplayName(final String displayName)
    {
        this.displayName = displayName;
    }

    public boolean isSsd()
    {
        return ssd;
    }

    public void setSsd(final boolean ssd)
    {
        this.ssd = ssd;
    }

    public String getSerialNumber()
    {
        return serialNumber;
    }

    public void setSerialNumber(final String serialNumber)
    {
        this.serialNumber = serialNumber;
    }

    public Host getHost()
    {
        return host;
    }

    public void setHost(final Host host)
    {
        this.host = host;
    }

    public String getCanonicalName()
    {
        return canonicalName;
    }

    public void setCanonicalName(final String canonicalName)
    {
        this.canonicalName = canonicalName;
    }
}
