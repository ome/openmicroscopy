# Development Dockerfile for OMERO
# --------------------------------
# This dockerfile can be used to build an
# OMERO distribution which can then be run
# within a number of different Docker images.

# By default, building this dockerfile will use
# the IMAGE argument below for the runtime image.
ARG BUILD_IMAGE=adoptopenjdk:11-jdk-hotspot-bionic

# To build code with other runtimes
# pass a build argument, e.g.:
#
#   docker build --build-arg BUILD_IMAGE=openjdk:9 ...
#

# The produced /src directory will be copied the
# RUN_IMAGE for end-use. This value can also be
# set at build time with --build-arg RUN_IMAGE=...
ARG COMPONENT=server
ARG RUN_IMAGE=openmicroscopy/omero-${COMPONENT}:5.6


FROM ${BUILD_IMAGE} as build
USER root
ARG DEBIAN_FRONTEND=noninteractive

# From omero-install: step01_ubuntu1804_ice_deps.sh
RUN apt-get update && \
    apt-get install -y -q \
        build-essential \
        wget \
        db5.3-util \
        libbz2-dev \
        libdb++-dev \
        libdb-dev \
        libexpat-dev \
        libmcpp-dev \
        libssl-dev \
        mcpp \
        zlib1g-dev \
 && cd /tmp \
 && wget -q https://github.com/ome/zeroc-ice-ubuntu1804/releases/download/0.3.0/ice-3.6.5-0.3.0-ubuntu1804-amd64.tar.gz \
 && tar xf ice-3.6.5-0.3.0-ubuntu1804-amd64.tar.gz \
 && rm ice-3.6.5-0.3.0-ubuntu1804-amd64.tar.gz \
 && mv ice-3.6.5-0.3.0 /opt \
 && echo /opt/ice-3.6.5-0.3.0/lib/x86_64-linux-gnu > /etc/ld.so.conf.d/ice-x86_64.conf \
 && ldconfig

RUN apt-get update \
 && apt-get install -y ant git gradle maven python3 python3-pip python3-venv \
      python-pillow python-numpy python-sphinx

ENV VIRTUAL_ENV=/opt/omero/server/venv3
ENV PATH=/opt/ice-3.6.5-0.3.0/bin:$VIRTUAL_ENV/bin/:$PATH
RUN mkdir -p /opt/omero/server/ \
 && python3 -m venv $VIRTUAL_ENV
RUN python -m pip install --upgrade pip setuptools
RUN python -m pip install https://github.com/ome/zeroc-ice-ubuntu1804/releases/download/0.2.0/zeroc_ice-3.6.5-cp36-cp36m-linux_x86_64.whl
RUN python -m pip install flake8 future pytest
RUN python -m pip install 'omero-py>=5.6.0.dev10'
RUN id 1000 || useradd -u 1000 -ms /bin/bash build

# TODO: would be nice to not need to copy .git since it invalidates the build frequently and takes more time
COPY .git /src/.git

COPY build.py /src/
COPY build.xml /src/
COPY components /src/components
COPY docs /src/docs
COPY etc /src/etc
COPY ivy.xml /src/
COPY lib /src/lib
COPY luts /src/luts
COPY omero.class /src/
COPY setup.cfg /src/
COPY sql /src/sql
COPY test.xml /src/
COPY LICENSE.txt /src/
COPY history.rst /src/
RUN chown -R 1000 /src
USER 1000
WORKDIR /src
ENV ICE_CONFIG=/src/etc/ice.config
RUN sed -i "s/^\(omero\.host\s*=\s*\).*\$/\1omero/" /src/etc/ice.config

# The following may be necessary depending on
# which images you are using. See the following
# card for more info:
#
#     https://trello.com/c/rPstbt4z/216-open-ssl-110
#
# RUN sed -i 's/\("IceSSL.Ciphers".*ADH[^"]*\)/\1:@SECLEVEL=0/' /src/components/tools/OmeroPy/src/omero/clients.py /src/etc/templates/grid/templates.xml

# Reproduce jenkins build
RUN env BUILD_NUMBER=1 OMERO_BRANCH=develop bash docs/hudson/OMERO.sh

FROM ${RUN_IMAGE} as run
RUN rm -rf /opt/omero/server/OMERO.server
COPY --chown=omero-server:omero-server --from=build /src/dist /opt/omero/server/OMERO.server
USER root
RUN yum install -y git

USER omero-server
WORKDIR /opt/omero/server/OMERO.server
ENV VIRTUAL_ENV=/opt/omero/server/venv3
ENV PATH="$VIRTUAL_ENV/bin:$PATH"
