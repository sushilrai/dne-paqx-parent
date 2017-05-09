#!/bin/sh
# Copyright © 2017 Dell Inc. or its subsidiaries.  All Rights Reserved 
CONTAINERID=$(basename "$(cat /proc/1/cpuset)" | cut -c 1-12)
java -jar -Xms64m -Xmx192m -Dcontainer.id=$CONTAINERID /opt/dell/cpsd/dne-paqx/lib/dne-paqx-web.jar
