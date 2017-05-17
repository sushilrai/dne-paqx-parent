#!/bin/sh
# Copyright © 2017 Dell Inc. or its subsidiaries.  All Rights Reserved 
CONTAINERID=$(basename "$(cat /proc/1/cpuset)" | cut -c 1-12)
java  -Xms64m -Xmx192m -Dspring.profiles.active=production -Dcontainer.id=$CONTAINERID -Dlog4j.configuration=file:/opt/dell/cpsd/dne-paqx/conf/dne-paqx-log4j.xml -jar /opt/dell/cpsd/dne-paqx/lib/dne-paqx-web.jar
