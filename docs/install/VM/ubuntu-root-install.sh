#!/bin/bash

OMERODIR=${OMERODIR:-"$1"}
OMERODIR=${OMERODIR:-"/OMERO"}

set -e
set -u
set -x

##
# Setup Java sources in Ubuntu
#
add_sources(){
    echo "$1" | sudo tee -a /etc/apt/sources.list
}
add_sources "deb http://archive.canonical.com/ubuntu lucid partner"
add_sources "deb-src http://archive.canonical.com/ubuntu lucid partner"

##
# Update core system and apt-cache
#
aptitude --assume-yes update
aptitude --assume-yes dist-upgrade

##
# Install Core applications
#
aptitude --assume-yes install build-essential
aptitude --assume-yes install subversion

##
# Intall Java
#
echo sun-java6-jdk shared/accepted-sun-dlj-v1-1 select true | /usr/bin/debconf-set-selections
echo sun-java6-jre shared/accepted-sun-dlj-v1-1 select true | /usr/bin/debconf-set-selections
aptitude --assume-yes install java-package sun-java6-bin sun-java6-demo sun-java6-fonts sun-java6-jdk sun-java6-plugin sun-java6-jre sun-java6-source
echo 'JAVA_HOME="/usr/lib/jvm/java-6-sun"' | sudo tee -a /etc/environment
echo 'LD_LIBRARY_PATH="/usr/lib/jvm/java-6-sun/jre/lib/amd64/server"' | sudo tee -a /etc/bash.bashrc
echo "export LD_LIBRARY_PATH" | sudo tee -a /etc/bash.bashrc


##
# Install Python
#
aptitude --assume-yes install cython python-numpy python-scipy python-setuptools python-numeric python-matplotlib python-wxgtk2.8 python-decorator python-mysqldb python-nose python-dev
apt-get --assume-yes install python-distutils-extra
apt-get --assume-yes install python-tables
apt-get --assume-yes install python-imaging

##
# Install Ice
#
apt-get --assume-yes install zeroc-ice33

##
# Install Postgres
#
apt-get --assume-yes install postgresql

##
# Install X and Gnome
#
apt-get --assume-yes install gdm
apt-get --assume-yes install xinit

##
# Install Apache, Django and webclient apps
#
apt-get --assume-yes install apache2
#apt-get --assume-yes install python-django
apt-get --assume-yes install libapache2-mod-fastcgi
apt-get --assume-yes install links

##
# Install OMERO
#


##
# Set up omero
#
mkdir -p $OMERODIR
mkdir -p /Client
mkdir -p /Server

chown -R omero $OMERODIR
chown -R omero /Client
chown -R omero /Server

