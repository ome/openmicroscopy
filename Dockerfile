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

# Requirements installation:
# BUILD_STYLE can be one of: release, latest, merge
#  - release: don't build any dependencies
#  - latest: checkout and the master branch of dependencies
#  - merge: additionally merge PRs before building
FROM ${BUILD_IMAGE} as build
COPY .omeroci/setup-ubuntu.sh /tmp/setup/
RUN /tmp/setup/setup-ubuntu.sh
RUN adduser omero
COPY .omeroci/ /tmp/setup/
ARG BUILD_STYLE
RUN echo ${BUILD_STYLE}
RUN exit 1
RUN su omero -c "/tmp/setup/build-requirements.sh ${BUILD_STYLE} omero-gradle-plugins /tmp"
RUN su omero -c "/tmp/setup/build-requirements.sh ${BUILD_STYLE} omero-build /tmp"

# Build distribution
COPY --chown=omero:omero . /src
USER omero
WORKDIR /src
ENV ICE_CONFIG=/src/etc/ice.config
RUN sed -i "s/^\(omero\.host\s*=\s*\).*\$/\1omero/" /src/etc/ice.config
WORKDIR /src
# The following may be necessary depending on
# which images you are using. See the following
# card for more info:
#
#     https://trello.com/c/rPstbt4z/216-open-ssl-110
#
# RUN sed -i 's/\("IceSSL.Ciphers".*ADH[^"]*\)/\1:@SECLEVEL=0/' /src/components/tools/OmeroPy/src/omero/clients.py /src/etc/templates/grid/templates.xml

# Reproduce jenkins build
RUN env BUILD_NUMBER=1 OMERO_BRANCH=develop bash docs/hudson/OMERO.sh

# Package for docker
FROM ${RUN_IMAGE} as run
RUN rm -rf /opt/omero/server/OMERO.server
COPY --chown=omero-server:omero-server --from=build /src/dist /opt/omero/server/OMERO.server
USER root
RUN yum install -y git
USER omero-server
WORKDIR /opt/omero/server/OMERO.server
