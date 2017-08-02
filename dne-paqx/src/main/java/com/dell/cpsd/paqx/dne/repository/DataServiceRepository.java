package com.dell.cpsd.paqx.dne.repository;

import com.dell.cpsd.paqx.dne.service.model.ComponentEndpointDetails;

import java.util.List; /**
 * TODO: Document Usage
 * <p>
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries.  All Rights Reserved.
 * </p>
 *
 * @version 1.0
 * @since 1.0
 */
public interface DataServiceRepository
{
    void saveScaleIoComponentDetails(List<ComponentEndpointDetails> componentEndpointDetailsList);

    void saveVCenterComponentDetails(List<ComponentEndpointDetails> componentEndpointDetailsList);

    //TODO: Define this
    void saveVCenterData();

    //TODO: Define this
    void saveScaleIoData();
}
