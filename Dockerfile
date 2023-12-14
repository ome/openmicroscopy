# Development Dockerfile for OMERO
# --------------------------------
# This dockerfile can be used to build an
# OMERO distribution which can then be run
# within a number of different Docker images.

# By default, building this dockerfile will use
# the IMAGE argument below for the runtime image.
ARG BUILD_IMAGE=eclipse-temurin:11-jdk-jammy

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

# From omero-install: step01_ubuntu2204_ice_deps.sh
RUN apt-get update && \
    apt-get install -y -q \
        build-essential \
        wget \
        db5.3-util \
        bzip2 \
        libdb++ \
        libexpat1 \
        libmcpp0 \
        openssl \
        mcpp \
        zlib1g \
 && cd /tmp \
 && wget -q https://github.com/glencoesoftware/zeroc-ice-ubuntu2204-x86_64/releases/download/20221004/Ice-3.6.5-ubuntu2204-x86_64.tar.gz \
 && tar xf Ice-3.6.5-ubuntu2204-x86_64.tar.gz \
 && rm Ice-3.6.5-ubuntu2204-x86_64.tar.gz \
 && mv Ice-3.6.5 /opt/ice-3.6.5 \
 && echo /opt/ice-3.6.5/lib64 > /etc/ld.so.conf.d/ice-x86_64.conf \
 && ldconfig

RUN apt-get update \
 && apt-get install -y ant git gradle maven python3 python3-pip python3-venv

ENV VIRTUAL_ENV=/opt/omero/server/venv3
ENV PATH=/opt/ice-3.6.5/bin:$VIRTUAL_ENV/bin/:$PATH
RUN mkdir -p /opt/omero/server/ \
 && python3 -m venv $VIRTUAL_ENV
RUN python -m pip install --upgrade pip setuptools pillow numpy sphinx
RUN python -m pip install https://github.com/glencoesoftware/zeroc-ice-py-ubuntu2204-x86_64/releases/download/20221004/zeroc_ice-3.6.5-cp310-cp310-linux_x86_64.whl
RUN python -m pip install flake8 future pytest
RUN python -m pip install 'omero-py>=5.17.0'
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
RUN ./build.py
FROM ${RUN_IMAGE} as run
RUN rm -rf /opt/omero/server/OMERO.server
COPY --chown=omero-server:omero-server --from=build /src/dist /opt/omero/server/OMERO.server
USER root
RUN yum install -y git

USER omero-server
WORKDIR /opt/omero/server/OMERO.server
ENV VIRTUAL_ENV=/opt/omero/server/venv3
ENV PATH="$VIRTUAL_ENV/bin:$PATH"
