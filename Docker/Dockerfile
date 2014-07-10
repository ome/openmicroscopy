FROM omedocker/omero-build
MAINTAINER ome-devel@lists.openmicroscopy.org.uk

ADD openmicroscopy.zip /tmp/openmicroscopy.zip
ADD build.sh /tmp/build.sh
RUN bash /tmp/build.sh

USER root
RUN mv /tmp/dist /opt/omero && rm /tmp/build.sh
VOLUME /opt/omero

USER omero
CMD ["/bin/bash"]
