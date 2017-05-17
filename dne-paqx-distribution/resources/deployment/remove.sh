#!/bin/bash
#
# Copyright (c) 2017 Dell Inc. or its subsidiaries.  All Rights Reserved.
# Dell EMC Confidential/Proprietary Information
#

RETVAL=0

echo "Removing Dell Inc. DNE PAQX docker image."

docker rmi IMAGE_NAME_STANDIN:IMAGE_TAG_STANDIN

echo "Dell Inc. DNE PAQX docker image removed successfully."

exit $RETVAL