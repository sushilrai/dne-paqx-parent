#!/bin/bash
#
# Copyright (c) 2017 Dell Inc. or its subsidiaries.  All Rights Reserved.
# Dell EMC Confidential/Proprietary Information
#

RETVAL=0

echo "Loading Dell Inc. DNE PAQX docker image."

SCRIPT_PATH=$(dirname "$0")

docker load -i $SCRIPT_PATH/IMAGE_NAME_STANDIN.tar

#if -s, skip running the docker image. If not, run the image automatically

case "$1" in
-s)
exit 0
;;
*)
export HOSTNAME=$HOSTNAME
/usr/bin/docker-compose -f $SCRIPT_PATH/docker-compose.yml up -d
;;
esac

echo "Dell Inc. DNE PAQX docker image loaded successfully."

exit $RETVAL
