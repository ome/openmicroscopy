#!/usr/bin/env bash

set -e
set -u
set -x


MERGE_SCRIPT=$foo

cd $1
git clone git://github.com/ome/omero-gradle-plugins

RUN cd /tmp/omero-gradle-plugins && git submodule update --init
RUN cd /tmp/omero-gradle-plugins && ./build.sh

# Build components
RUN git clone git://github.com/ome/omero-build /tmp/omero-build
WORKDIR /tmp/omero-build
RUN git submodule update --init
RUN ./build.sh

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
RUN components/tools/travis-build

# Package for docker
FROM ${RUN_IMAGE} as run
RUN rm -rf /opt/omero/server/OMERO.server
COPY --chown=omero-server:omero-server --from=build /src/dist /opt/omero/server/OMERO.server
USER root
RUN yum install -y git
USER omero-server
WORKDIR /opt/omero/server/OMERO.server
