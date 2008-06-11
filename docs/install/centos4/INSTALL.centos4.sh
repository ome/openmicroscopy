#!/bin/sh
#
# Installation instructions for CentOS 4
#
# The CentOS 4 install was tested on an older VMWare image like
# the ones provided at:
#
#   http://dev.centos.org/~tru/vmware/
#
# using the binaries provided by ZeroC. If you would like to use the yum
# repository provided by ZeroC, see the instructions for the CentOS 5
# install.

set -e
set -x
set -u

wget http://zeroc.com/download/Ice/3.2/Ice-3.2.1-bin-rhel-i386.tar.gz
tar xzf Ice-3.2.1-bin-rhel-i386.tar.gz

export ICE_HOME=Ice-3.2.1
export PATH=$ICE_HOME/bin:$PATH
export LD_LIBRARY_PATH=$ICE_HOME/lib
export PYTHONPATH=$ICE_HOME/python

