#
#   $Id$
#
#   Copyright 2007 Glencoe Software, Inc. All rights reserved.
#   Use is subject to license terms supplied in LICENSE.txt
#
#

AUTOMAKE_OPTIONS = foreign

SLICE_DIR=@SLICE_DIR@
SLICE_OME=../../resources/slice
SLICE_GEN=../../target/generated/resources
SLICE_INCLUDES=-I$(SLICE_DIR) -I$(SLICE_OME)  -I$(SLICE_GEN)

CPP_SRC=.
CPP_GEN=generated

AM_CXXFLAGS=-O0 -Wall
INCLUDES=-I$(CPP_GEN) -I$(CPP_SRC)
LIBS=-lstdc++ -lIce -lIceUtil -lSlice -lGlacier2

default:
	echo "Nothing"

slice: 
	mkdir -p $(CPP_GEN) && \
	@SLICE2CPP@ $(SLICE_INCLUDES) --output-dir=$(CPP_GEN) $(SLICE_GEN)/*.ice $(SLICE_GEN)/OMERO/*.ice $(SLICE_OME)/*.ice

lib_LTLIBRARIES     	= libOMERO_common.la libOMERO_client.la
libOMERO_common_la_SOURCES = ./generated/OMERO/common.cpp \
	./generated/omero.cpp \
	./generated/ice.cpp \
	./generated/API.cpp \
	./generated/Constants.cpp \
	./generated/Error.cpp \
	./generated/IObject.cpp \
	./generated/ROMIO.cpp \
	./generated/RTypes.cpp \
	./generated/System.cpp

#libOMERO_common_a_LDFLAGS = -no-undefined -version-info 0:0:0

libOMERO_client_la_SOURCES= ./OMERO/client.cpp
libOMERO_client_la_LIBADD = libOMERO_common.la
#libOMERO_client_a_LDFLAGS= -no-undefined -version-info 0:0:0

bin_PROGRAMS            = OMERO.demo
OMERO_demo_SOURCES     	= main.cpp
OMERO_demo_LDADD   	= libOMERO_client.la


