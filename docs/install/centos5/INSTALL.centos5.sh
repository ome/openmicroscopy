#!/bin/sh
#
# Installation instructions for CentOS 5
#
# The CentOS 5 install was tested with the VMWare image provided at
#
#   http://dev.centos.org/~tru/vmware/centos-5.20080523/
#
# using the Yum repository provided by ZeroC. If you would like to
# download and install the binaries yourself, see the instructions for
# the CentOS 4 install.

set -e
set -x
set -u

#
# Add the zeroc-ice.repo to your yum.d directory
#
cd /etc/yum.repos.d
wget http://zeroc.com/download/Ice/3.2/rhel4/zeroc-ice.repo
yum install ice-python

#
# Then it is necessary to install a JDK. One option is to
# download and install the Sun JDK.
#

PYTHONPATH=/usr/lib/Ice-3.2.1/python dist/bin/omero admin help

yum install postgresql-server
su - postgres -c "createuser omero"
su - omero -c "createdb omero3"
