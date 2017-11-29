/**
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries.  All Rights Reserved.
 * Dell EMC Confidential/Proprietary Information
 */

package com.dell.cpsd.paqx.dne.exception;

/**
 * Task response failure exception code
 * <p>
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries. All Rights Reserved.
 * Dell EMC Confidential/Proprietary Information
 * </p>
 *
 * @version 1.0
 * @since 1.0
 */
public enum TaskResponseExceptionCode
{
    LIST_SCALEIO_COMPONENTS(1001),
    LIST_VCENTER_COMPONENTS(1002),
    DISCOVER_SCALEIO(1003),
    DISCOVER_VCENTER(1004),
    ADD_HOST_TO_VCENTER_CLUSTER(1005),
    ADD_SDS_NODE_TO_PROTECTION_DOMAIN(1006),
    INSTALL_SDC_VIB(1007),
    CONFIGURE_SDC_VIB(1008),
    ADD_HOST_TO_DV_SWITCH(1009),
    DEPLOY_VM_FROM_TEMPLATE(1010),
    ENABLE_PCI_PASSTHROUGH(1011),
    REBOOT_HOST(1012),
    CONFIGURE_PCI_PASSTHROUGH_SCALEIO_VM(1013),
    APPLY_ESXI_HOST_LICENSE(1014),
    LIST_ESXI_DEFAULT_CREDENTIALS(1015),
    HOST_MAINTENANCE_MODE(1016),
    DATASTORE_RENAME(1017),
    UPDATE_SOFTWARE_ACCEPTANCE(1018),
    VM_POWER_OPERATIONS(1019),
    CONFIGURE_VM_NETWORK_SETTINGS(1020),
    REMOTE_COMMAND_EXECUTION(1021),
    UPDATE_SDC_PERFORMANCE_PROFILE(1022),
    CREATE_STORAGE_POOL(1023),
    CREATE_PROTECTION_DOMAIN(1024),
    CONFIGURE_PXE_BOOT(1025);

    private final int code;

    TaskResponseExceptionCode(final int code)
    {
        this.code = code;
    }

    public int getCode()
    {
        return code;
    }
}
