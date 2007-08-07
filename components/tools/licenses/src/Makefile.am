#
#   Copyright 2007 Glencoe Software, Inc. All rights reserved.
#   Use is subject to license terms supplied in LICENSE.txt
#

AUTOMAKE_OPTIONS = foreign subdir-objects

DIST_DIR ?= ../../../../dist
TOOLS_DIR ?= ../../target
SLICE_INCLUDES ?= -I@SLICE_DIR@ -I$(DIST_DIR)/include -I$(TOOLS_DIR)/include -I../target
OUTPUT_DIR ?= slice_generated

INCLUDES = -I$(OUTPUT_DIR) -I$(DIST_DIR)/include -I$(TOOLS_DIR)/include
LIBS = -lstdc++ -lOMERO_client -lIce -lIceUtil -lGlacier2

all: slice libOMERO_licenses.la

#.PHONY: clean check run slice

slice:
	mkdir -p $(OUTPUT_DIR)/OMERO && \
	slice2cpp $(SLICE_INCLUDES) --include-dir=OMERO --output-dir=$(OUTPUT_DIR)/OMERO/ ../resources/OMERO/LicensesAPI.ice

lib_LTLIBRARIES              = libOMERO_licenses.la
libOMERO_licenses_la_SOURCES = slice_generated/OMERO/LicensesAPI.cpp
libOMERO_licenses_la_LDFLAGS = -L$(TOOLS_DIR)/lib

