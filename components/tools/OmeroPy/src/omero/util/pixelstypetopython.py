#!/usr/bin/env python
# -*- coding: utf-8 -*-
#
# ------------------------------------------------------------------------------
#  Copyright (C) 2006-2008 University of Dundee. All rights reserved.
#
#
# 	This program is free software; you can redistribute it and/or modify
#  it under the terms of the GNU General Public License as published by
#  the Free Software Foundation; either version 2 of the License, or
#  (at your option) any later version.
#  This program is distributed in the hope that it will be useful,
#  but WITHOUT ANY WARRANTY; without even the implied warranty of
#  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
#  GNU General Public License for more details.
#
#  You should have received a copy of the GNU General Public License along
#  with this program; if not, write to the Free Software Foundation, Inc.,
#  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
#
# ------------------------------------------------------------------------------
#
# The Pixels object in omero, has a member pixelsType, this can be
#    INT_8 = "int8";
#    UINT_8 = "uint8";
#    INT_16 = "int16";
#    UINT_16 = "uint16";
#    INT_32 = "int32";
#    UINT_32 = "uint32";
#    FLOAT = "float";
#    DOUBLE = "double";
# we can convert these to the appropriate types in python.

from omero.model.enums import PixelsTypeint8, PixelsTypeuint8, PixelsTypeint16
from omero.model.enums import PixelsTypeuint16, PixelsTypeint32
from omero.model.enums import PixelsTypeuint32, PixelsTypefloat
from omero.model.enums import PixelsTypedouble

INT_8 = PixelsTypeint8
UINT_8 = PixelsTypeuint8
INT_16 = PixelsTypeint16
UINT_16 = PixelsTypeuint16
INT_32 = PixelsTypeint32
UINT_32 = PixelsTypeuint32
FLOAT = PixelsTypefloat
DOUBLE = PixelsTypedouble


def toPython(pixelType):
    if(pixelType == INT_8):
        return 'b'
    if(pixelType == UINT_8):
        return 'B'
    if(pixelType == INT_16):
        return 'h'
    if(pixelType == UINT_16):
        return 'H'
    if(pixelType == INT_32):
        return 'i'
    if(pixelType == UINT_32):
        return 'I'
    if(pixelType == FLOAT):
        return 'f'
    if(pixelType == DOUBLE):
        return 'd'


def toNumpy(pixelType):
    import numpy
    if(pixelType == INT_8):
        return numpy.int8
    if(pixelType == UINT_8):
        return numpy.uint8
    if(pixelType == INT_16):
        return numpy.int16
    if(pixelType == UINT_16):
        return numpy.uint16
    if(pixelType == INT_32):
        return numpy.int32
    if(pixelType == UINT_32):
        return numpy.uint32
    if(pixelType == FLOAT):
        return numpy.float
    if(pixelType == DOUBLE):
        return numpy.double


def toArray(pixelType):
    if(pixelType == INT_8):
        return 'b'
    if(pixelType == UINT_8):
        return 'B'
    if(pixelType == INT_16):
        return 'i2'
    if(pixelType == UINT_16):
        return 'H2'
    if(pixelType == INT_32):
        return 'i4'
    if(pixelType == UINT_32):
        return 'I4'
    if(pixelType == FLOAT):
        return 'f'
    if(pixelType == DOUBLE):
        return 'd'


def toPIL(pixelType):
    if(pixelType == INT_8):
        return 'L'
    if(pixelType == UINT_8):
        return 'L'
    if(pixelType == INT_16):
        return 'I;16'
    if(pixelType == UINT_16):
        return 'I;16'
    if(pixelType == INT_32):
        return 'I'
    if(pixelType == UINT_32):
        return 'I'
    if(pixelType == FLOAT):
        return 'F'
    if(pixelType == DOUBLE):
        return 'F'
