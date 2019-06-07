# Development Dockerfile for OMERO
# --------------------------------
# This dockerfile can be used to build an
# OMERO distribution which can then be run
# within a number of different Docker images.

# By default, building this dockerfile will use
# the IMAGE argument below for the runtime image.
ARG BUILD_IMAGE=openjdk:8

# To build code with other runtimes
# pass a build argument, e.g.:
#
#   docker build --build-arg BUILD_IMAGE=openjdk:9 ...
#

# The produced /src directory will be copied the
# RUN_IMAGE for end-use. This value can also be
# set at build time with --build-arg RUN_IMAGE=...
ARG COMPONENT=server
ARG RUN_IMAGE=openmicroscopy/omero-${COMPONENT}:latest


FROM ${BUILD_IMAGE} as build
USER root
RUN apt-get update \
 && apt-get install -y ant \
      python-pip python-tables python-virtualenv python-yaml python-jinja2 \
      zlib1g-dev python-pillow python-numpy python-sphinx \
      libssl-dev libbz2-dev libmcpp-dev libdb++-dev libdb-dev \
      zeroc-ice-all-dev \
 && pip install --upgrade 'pip<10' setuptools flake8==2.4.0 pytest==2.7.3
# TODO: unpin pip when possible
# openjdk:8 is "stretch" or Debian 9
RUN pip install https://github.com/ome/zeroc-ice-py-debian9/releases/download/0.1.0/zeroc_ice-3.6.4-cp27-cp27mu-linux_x86_64.whl
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
