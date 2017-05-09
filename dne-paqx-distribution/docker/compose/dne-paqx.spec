#
# Copyright (c) 2017 Dell Inc. or its subsidiaries.  All Rights Reserved.
# Dell EMC Confidential/Proprietary Information
#
Summary: Dell Inc. DNE PAQX
Name: dne-paqx
Version: %_version
Release: %_revision
License: Commercial
Vendor: Dell Inc.
Group: System Environment/Dell Inc. Applications
URL: http://www.dell.com
Requires: rcm-fitness-common

%define _use_internal_dependency_generator 0
%define __find_requires %{nil}

%description
Dell Inc. DNE PAQX


##############################################################################
# build
##############################################################################
%build

# Creates directory if it doesn't exist
# $1: Directory path
init_dir ()
{
    [ -d $1 ] || mkdir -p $1
}

# check and create the directories for the service
SERVICE_BUILD_ROOT=${RPM_BUILD_ROOT}/opt/dell/dne-paqx

init_dir ${SERVICE_BUILD_ROOT}
init_dir ${SERVICE_BUILD_ROOT}/install

# copy the install scripts
cp -rf ${RPM_SOURCE_DIR}/docker/compose/install/* ${SERVICE_BUILD_ROOT}/install

# copy systemd service definition
init_dir ${RPM_BUILD_ROOT}/usr/lib/systemd/system
cp -rf ${RPM_SOURCE_DIR}/docker/compose/systemd/dne-paqx.service ${RPM_BUILD_ROOT}/usr/lib/systemd/system/dne-paqx.service


##############################################################################
# pre
##############################################################################
%pre

exit 0


##############################################################################
# post
##############################################################################
%post
if [ $1 -eq 1 ];then
    /bin/sh /opt/dell/dne-paqx/install/dne-paqx-install.sh
elif [ $1 -eq 2 ];then
    /bin/sh /opt/dell/dne-paqx/install/dne-paqx-upgrade.sh
else
    echo "Unexpected argument passed to RPM %post script: [$1]"
    exit 1
fi
exit 0


##############################################################################
# preun
##############################################################################
%preun
if [ $1 -eq 0 ];then
    /bin/sh /opt/dell/dne-paqx/install/dne-paqx-remove.sh
fi
exit 0

##############################################################################
# configure directory and file permissions
##############################################################################
%files

%attr(644,root,root) /usr/lib/systemd/system/dne-paqx.service

%attr(0754,rcm,dell) /opt/dell/dne-paqx/
%attr(0754,rcm,dell) /opt/dell/dne-paqx/install/
