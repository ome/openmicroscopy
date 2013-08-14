#!/bin/bash

set -e -u -x

# Utilities
sudo apt-get -y install unzip git

# OMERO requirements
sudo apt-get -y install \
    python-{imaging,matplotlib,numpy,pip,scipy,tables,virtualenv} \
    openjdk-7-jre-headless \
    ice34-services python-zeroc-ice \
    postgresql \
    nginx \


