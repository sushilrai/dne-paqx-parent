#!/bin/bash
#
# Copyright (c) 2017 Dell Inc. or its subsidiaries.  All Rights Reserved.
# Dell EMC Confidential/Proprietary Information
#

echo "Installing Dell Inc. DNE PAQX components"

RETVAL=0

SERVICE_BASE=/opt/dell/cpsd/dne-paqx

echo "Installing Dell Inc. DNE PAQX components"

if [ ! -d "$SERVICE_BASE" ]; then
    echo "Could not find directory - [$SERVICE_BASE] does not exist."
    exit 1
fi

usermod -aG docker dnepx

/bin/sh /opt/dell/cpsd/dne-paqx/image/engineering-standards-service/install.sh -s
/bin/sh /opt/dell/cpsd/dne-paqx/image/dne-paqx-web/install.sh -s

systemctl enable dell-dne-paqx
systemctl enable dell-dne-paqx-ess
systemctl enable dell-dne-paqx-web

systemctl start dell-dne-paqx

echo "Dell Inc. DNE PAQX components install has completed successfully."

exit 0
