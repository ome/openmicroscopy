#!/bin/sh
#
# Installation instructions for Ubuntu Gutsy Gibbon
#
# This Ubuntu Gutsy Gibbon (7.10) install was tested within a Virtual Box image.
# The first instructions are for interacting with VirtualBox and can be ignored
# if you already have a working Ubuntu Gutsy installation.


##################################################################
#
# * Download the minimal gutsy image from:
#   https://help.ubuntu.com/community/Installation/MinimalCD
#
#    wget http://archive.ubuntu.com/ubuntu/dists/gutsy/main/installer-i386/current/images/netboot/mini.iso
#
# * Creating a new Virtual Box image with a new, empty disk
# * Using the downloaded .iso file as the installation medium
# * Accepting the defaults of the installer, either during install or as root:
#
#    adduser omero
#    printf "omero\nomero\n" | passwd omero
#
##################################################################

set -e
set -x
set -u

#
# You now have a working Ubuntu installation with a user omero
# with sudo privileges.
#

sudo apt-get install sun-java6-jdk unzip python-zeroc-ice icegrid postgresql

#
# Add omero user as a database user, and create database
#
sudo su -c postgres -c "createuser omero"
createuser omero
createdb omero3

#
# Configure postgres to accept connections from a server running as omero
#
sudo cp pg_hba.conf /etc/postgresql/8.2/main/
sudo cp postgresql.conf /etc/postgresql/8.2/main/
sudo /etc/init.d/postgresql restart

#
# Download an OMERO server bundle for Beta3, and unpackage
#
unzip OMERO.server-Beta3.zip
chmod a+x OMERO.server-Beta3/bin/omero
ln -s OMERO.server-build1013/bin/omero .

#
# Configuring the server.
#
cp local.properties OMERO.server*/etc
cp omero.properties OMERO.server*/etc
cp templates.xml OMERO.server*/etc/grid
mkdir ~/OMERO

#
# Starting the server for the first time
#
cd OMERO.server*
java omero setup-db
java omero update
sudo /etc/init.d/omero-admin start
sudo /etc/init.d/omero-admin deploy

#
# Optional:
#
sudo ln -s /home/omero/omero /etc/init.d/omero-admin
sudo update-rc.d omero-admin defaults
ln -s OMERO.server-build1013/var/log/master.out OmeroGrid.log



