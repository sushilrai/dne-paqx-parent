#!/bin/bash
#
# Copyright (c) 2017 Dell Inc. or its subsidiaries.  All Rights Reserved.
# Dell EMC Confidential/Proprietary Information
#

echo "Removing Dell Inc. DNE PAQX components"

systemctl disable dne-paqx

echo "Dell Inc. DNE PAQX components removal has completed successfully."

exit 0
