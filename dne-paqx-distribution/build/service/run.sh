#!/bin/sh
#
# Copyright (c) 2017 Dell Inc. or its subsidiaries.  All Rights Reserved.
# Dell EMC Confidential/Proprietary Information
#

CONTAINERID=$(basename "$(cat /proc/1/cpuset)" | cut -c 1-12)
LOCAL_DOMAIN=cpsd.dell
FQDN="$(hostname -s).${LOCAL_DOMAIN}"

BASE_CERTS_DIR=/usr/local/share/ca-certificates
CA_CERT_FILE=${BASE_CERTS_DIR}/${FQDN}.ca.crt
CERT_FILE=${BASE_CERTS_DIR}/${FQDN}.crt
KEY_FILE=${BASE_CERTS_DIR}/${FQDN}.key
JKS_FILE=${BASE_CERTS_DIR}/${FQDN}.jks
TAR_BUNDLE=bundle.tar.gz

TLS_URI=https://tls-service.${LOCAL_DOMAIN}:7001/tls-service
TLS_CA_CERT_URL=${TLS_URI}/v1/pki/ca
TLS_PUBLIC_KEY_URL=${TLS_URI}/v1/pki/public/key
TLS_TAR_BUNDLE_URL=${TLS_URI}/v1/pki/issue/${TAR_BUNDLE}

DB_OWNER="system-definition-service"
DB_NAME="system-definition-service"
DB_NAME_AS_IN_CONSUL="psql-system-definition-service"

CONSUL_URL="https://consul.${LOCAL_DOMAIN}:8500"
CONSUL_DB_URL="${CONSUL_URL}/v1/catalog/service/${DB_NAME_AS_IN_CONSUL}"
CONSUL_AMQP_STATUS_URL="${CONSUL_URL}/v1/health/service/amqp?passing"

PAM_URL="https://pam-service.${LOCAL_DOMAIN}:7002"
PAM_DATABASES_URL="${PAM_URL}/pam-service/v1/postgres/databases"
PAM_STATUS_URL="${PAM_URL}/pam-service/health"
PAM_AMQP_USERS_URL="${PAM_URL}/pam-service/v1/amqp/users"

CAPABILITY_STATUS_URL="https://capability-registry-service.${LOCAL_DOMAIN}:9050/health"
IDENTITY_STATUS_URL="https://identity-service.${LOCAL_DOMAIN}:9050/health"

POSTGRES_BASE_URL=jdbc:postgresql://postgres.${LOCAL_DOMAIN}:5432
MAX_SLEEP=300

touch -t 00:00:00 /tmp/_dt
FILE_COUNT=$(find $BASE_CERTS_DIR/ -type f -newer /tmp/_dt | grep $LOCAL_DOMAIN | wc -l)

echo "Waiting for TLS service to start up"
(
    if [ "${FILE_COUNT}" -gt 3 ]; then exit; fi

    for SLEEP_VAL in $(seq $MAX_SLEEP)
    do
        TLS_STATUS=$(curl -skL -w "%{http_code}\\n" "${TLS_URI}/health" -o /dev/null)
        if [ "${TLS_STATUS}" = "200" ]
        then
            exit 0
        fi
        echo " TLS: $SLEEP_VAL of $MAX_SLEEP"
        sleep 1
    done
    exit 4
) &
TLS_PID=$!

echo "Waiting for RabbitMQ to start up"
(
    for SLEEP_VAL in $(seq $MAX_SLEEP)
    do
        RABBIT_STATUS=$(curl -skL ${CONSUL_AMQP_STATUS_URL} | jq '. | length')

        if [ "${RABBIT_STATUS}" -gt 0 ]
        then
            exit 0
        fi
        echo "AMQP: $SLEEP_VAL of $MAX_SLEEP"
        sleep 1
    done
    exit 5
) &
RABBITMQ_PID=$!

echo "Waiting for PAM service to Start Up"
(
    for SLEEP_VAL in $(seq $MAX_SLEEP)
    do
        PAM_STATUS=$(curl -skL -w "%{http_code}\\n" "${PAM_STATUS_URL}" -o /dev/null)
        if [ "${PAM_STATUS}" = "200" ]
        then
            exit 0
        fi
        echo "PAM: $SLEEP_VAL of $MAX_SLEEP"
        sleep 1
    done
    exit 6
) &
PAM_PID=$!

echo "Waiting for Identity service to Start Up"
(
    for SLEEP_VAL in $(seq $MAX_SLEEP)
    do
        IDENTITY_STATUS=$(curl -skL -w "%{http_code}\\n" "${IDENTITY_STATUS_URL}" -o /dev/null)
        if [ "${IDENTITY_STATUS}" = "200" ]
        then
            exit 0
        fi
        echo "IDENTITY: $SLEEP_VAL of $MAX_SLEEP"
        sleep 1
    done
    exit 7
) &
IDENTITY_PID=$!


echo "Waiting for Capability Registry service to Start Up"
(
    for SLEEP_VAL in $(seq $MAX_SLEEP)
    do
        CAPABILITY_STATUS=$(curl -skL -w "%{http_code}\\n" "${CAPABILITY_STATUS_URL}" -o /dev/null)
        if [ "${CAPABILITY_STATUS}" = "200" ]
        then
            exit 0
        fi
        echo "CAPABILITY: $SLEEP_VAL of $MAX_SLEEP"
        sleep 1
    done
    exit 8
) &
CAPABILITY_PID=$!


wait $TLS_PID
if [ $? -eq 4 ];
then
    echo "ERROR: Failed to connect to TLS service"
    exit 1
fi

wait $RABBITMQ_PID
if [ $? -eq 5 ];
then
    echo "ERROR: Failed to find RabbitMQ in Consul"
    exit 1
fi

wait $PAM_PID
if [ $? -eq 6 ];
then
     echo "ERROR: Failed to connect to PAM service"
    exit 1
fi

wait $IDENTITY_PID
if [ $? -eq 7 ];
then
     echo "ERROR: Failed to connect to Identity service"
    exit 1
fi

wait $CAPABILITY_PID
if [ $? -eq 8 ];
then
     echo "ERROR: Failed to connect to Capability service"
    exit 1
fi


if [  "${FILE_COUNT}" -lt 3 ]
then
    curl -sk $TLS_CA_CERT_URL | tee ${CA_CERT_FILE}
    update-ca-certificates

    curl -O --header "Content-Type: application/json" \
      --request POST $TLS_TAR_BUNDLE_URL \
      --data-binary @- <<EOJ
{
    "common_name": "$FQDN",
    "require_jks": true
}
EOJ
    tar xvzf ${TAR_BUNDLE} -C ${BASE_CERTS_DIR}
    rm -rf ${TAR_BUNDLE}

    curl --cacert ${CA_CERT_FILE} \
         --cert ${CERT_FILE} \
         --key ${KEY_FILE} \
         -XPUT ${PAM_AMQP_USERS_URL}
fi

JKS_PASSPHRASE=$(cat ${BASE_CERTS_DIR}/${FQDN}.pwd)

java  -Xms64m -Xmx192m -Djavax.net.ssl.trustStore=${JKS_FILE} -Djavax.net.ssl.trustStorePassword=${JKS_PASSPHRASE} -Dspring.profiles.active=production -Dcontainer.id=$CONTAINERID -Dlog4j.configuration=file:/opt/dell/cpsd/dne-paqx/conf/dne-paqx-log4j.xml -jar /opt/dell/cpsd/dne-paqx/lib/dne-paqx-web.jar
