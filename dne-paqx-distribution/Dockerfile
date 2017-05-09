#
# Copyright (c) 2017 Dell Inc. or its subsidiaries.  All Rights Reserved.
# Dell EMC Confidential/Proprietary Information
#
#  USAGE: (from .. dir)
#    docker build -f dne-paqx-distribution/Dockerfile --tag=dne-paqx dne-paqx
#

# Start from base image with Java 8
FROM java:8-jre-alpine

RUN mkdir -p /opt/dell/cpsd/dne-paqx/bin /opt/dell/cpsd/dne-paqx/logs /opt/dell/cpsd/dne-paqx/lib /opt/dell/cpsd/dne-paqx/install

COPY target/dependency/lib/dne-paqx-web*.jar /opt/dell/cpsd/dne-paqx/lib/dne-paqx-web.jar
COPY docker/image/bin/run.sh /opt/dell/cpsd/dne-paqx/bin/

RUN chmod 755 /opt/dell/cpsd/dne-paqx/bin/run.sh

EXPOSE 8071

ENTRYPOINT ["/opt/dell/cpsd/dne-paqx/bin/run.sh"]
