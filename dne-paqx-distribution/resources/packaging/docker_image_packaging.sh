#!/usr/bin/env bash
#
# Copyright (c) 2017 Dell Inc. or its subsidiaries.  All Rights Reserved.
# Dell EMC Confidential/Proprietary Information
#
#call from pom
#step 1: make temporary directory, and target/deployment directory
mkdir target/temp_deployment
mkdir target/deployment

#save a tar of the docker image previously built to the temp directory with the other deployment files

if [ "${IMAGE_TAG}" != "latest" ]; then
docker tag ${IMAGE_NAME}:${IMAGE_TAG} ${IMAGE_NAME}:latest
docker save ${IMAGE_NAME}:latest > target/temp_deployment/${IMAGE_NAME}.tar
docker rmi ${IMAGE_NAME}:latest
else
docker save ${IMAGE_NAME}:latest > target/temp_deployment/${IMAGE_NAME}.tar
fi

# copy the docker compose file into the temp directory
cp -r build/install/* target/temp_deployment/

sed -i -- 's/IMAGE_NAME_STANDIN/'${IMAGE_NAME}'/g' target/temp_deployment/docker-compose.yml
sed -i -- 's/IMAGE_TAG_STANDIN/'latest'/g' target/temp_deployment/docker-compose.yml

#copy contents of deployment directory into temp directory
cp -r resources/deployment/* target/temp_deployment

#go into temp directory
cd target/temp_deployment

#change the permissions on necessary deployment scripts
chmod 755 install.sh
sed -i -- 's/IMAGE_NAME_STANDIN/'${IMAGE_NAME}'/g' install.sh


chmod 755 remove.sh
sed -i -- 's/IMAGE_NAME_STANDIN/'${IMAGE_NAME}'/g' remove.sh
sed -i -- 's/IMAGE_TAG_STANDIN/'latest'/g' remove.sh


chmod 755 ${IMAGE_NAME}.tar