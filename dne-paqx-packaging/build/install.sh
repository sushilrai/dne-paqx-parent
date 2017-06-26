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

RABBITMQ_ENV=/etc/rabbitmq/cpsd-rabbitmq-env.conf

if [ ! -f "$RABBITMQ_ENV" ]; then

    touch "$RABBITMQ_ENV"

    /usr/bin/env echo "CREDENTIALS=/etc/rabbitmq/credentials.properties" >> "$RABBITMQ_ENV"
    /usr/bin/env echo "PASSPHRASES=/etc/rabbitmq/client/passphrases.properties" >>  "$RABBITMQ_ENV"
    /usr/bin/env echo "KEYSTOREPATH=/etc/rabbitmq/certs/client/keycert.p12" >> "$RABBITMQ_ENV"
    /usr/bin/env echo "TRUSTSTOREPATH=/etc/rabbitmq/certs/rabbitstore" >> "$RABBITMQ_ENV"


    chmod a+r "$RABBITMQ_ENV"
else
    echo "Found $RABBITMQ_ENV"
fi

export HOSTNAME=$(hostname -f)
export CREDENTIALS=/etc/rabbitmq/credentials.properties
export PASSPHRASES=/etc/rabbitmq/client/passphrases.properties
export KEYSTOREPATH=/etc/rabbitmq/certs/client/keycert.p12
export TRUSTSTOREPATH=/etc/rabbitmq/certs/rabbitstore


pushd /opt/dell/cpsd/dne-paqx/install/dell-dne-paqx-web/
docker-compose create
docker start symphony-dne-paqx
popd

pushd /opt/dell/cpsd/dne-paqx/install/dell-dne-paqx-ess/
docker-compose create
docker start symphony-engineering-standards-service
popd


echo "Dell Inc. DNE PAQX components install has completed successfully."

exit 0
