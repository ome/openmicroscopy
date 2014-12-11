#!/usr/bin/env python
# -*- coding: utf-8 -*-

#
# Copyright (C) 2014 Glencoe Software, Inc. All Rights Reserved.
# Use is subject to license terms supplied in LICENSE.txt
#
# This program is free software; you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation; either version 2 of the License, or
# (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License along
# with this program; if not, write to the Free Software Foundation, Inc.,
# 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.

"""
Code-generated omero.model.Frequency implementation,
based on omero.model.PermissionsI
"""


import Ice
import IceImport
IceImport.load("omero_model_Frequency_ice")
_omero = Ice.openModule("omero")
_omero_model = Ice.openModule("omero.model")
__name__ = "omero.model"

from omero_model_UnitBase import UnitBase
from omero.model.enums import UnitsFrequency


def noconversion(cfrom, cto):
    raise Exception(("Unsupported conversion: "
                     "%s:%s") % cfrom, cto)


class FrequencyI(_omero_model.Frequency, UnitBase):

    CONVERSIONS = dict()
    CONVERSIONS["AHZ:CHZ"] = \
        lambda value: (10 ** -16) * value
    CONVERSIONS["AHZ:DAHZ"] = \
        lambda value: (10 ** -19) * value
    CONVERSIONS["AHZ:DHZ"] = \
        lambda value: (10 ** -17) * value
    CONVERSIONS["AHZ:EXAHZ"] = \
        lambda value: (10 ** -36) * value
    CONVERSIONS["AHZ:FHZ"] = \
        lambda value: (10 ** -3) * value
    CONVERSIONS["AHZ:GIGAHZ"] = \
        lambda value: (10 ** -27) * value
    CONVERSIONS["AHZ:HHZ"] = \
        lambda value: (10 ** -20) * value
    CONVERSIONS["AHZ:HZ"] = \
        lambda value: (10 ** -18) * value
    CONVERSIONS["AHZ:KHZ"] = \
        lambda value: (10 ** -21) * value
    CONVERSIONS["AHZ:MEGAHZ"] = \
        lambda value: (10 ** -24) * value
    CONVERSIONS["AHZ:MHZ"] = \
        lambda value: (10 ** -15) * value
    CONVERSIONS["AHZ:MICROHZ"] = \
        lambda value: (10 ** -12) * value
    CONVERSIONS["AHZ:NHZ"] = \
        lambda value: (10 ** -9) * value
    CONVERSIONS["AHZ:PETAHZ"] = \
        lambda value: (10 ** -33) * value
    CONVERSIONS["AHZ:PHZ"] = \
        lambda value: (10 ** -6) * value
    CONVERSIONS["AHZ:TERAHZ"] = \
        lambda value: (10 ** -30) * value
    CONVERSIONS["AHZ:YHZ"] = \
        lambda value: (10 ** 6) * value
    CONVERSIONS["AHZ:YOTTAHZ"] = \
        lambda value: (10 ** -42) * value
    CONVERSIONS["AHZ:ZETTAHZ"] = \
        lambda value: (10 ** -39) * value
    CONVERSIONS["AHZ:ZHZ"] = \
        lambda value: (10 ** 3) * value
    CONVERSIONS["CHZ:AHZ"] = \
        lambda value: (10 ** 16) * value
    CONVERSIONS["CHZ:DAHZ"] = \
        lambda value: (10 ** -3) * value
    CONVERSIONS["CHZ:DHZ"] = \
        lambda value: (10 ** -1) * value
    CONVERSIONS["CHZ:EXAHZ"] = \
        lambda value: (10 ** -20) * value
    CONVERSIONS["CHZ:FHZ"] = \
        lambda value: (10 ** 13) * value
    CONVERSIONS["CHZ:GIGAHZ"] = \
        lambda value: (10 ** -11) * value
    CONVERSIONS["CHZ:HHZ"] = \
        lambda value: (10 ** -4) * value
    CONVERSIONS["CHZ:HZ"] = \
        lambda value: (10 ** -2) * value
    CONVERSIONS["CHZ:KHZ"] = \
        lambda value: (10 ** -5) * value
    CONVERSIONS["CHZ:MEGAHZ"] = \
        lambda value: (10 ** -8) * value
    CONVERSIONS["CHZ:MHZ"] = \
        lambda value: 10 * value
    CONVERSIONS["CHZ:MICROHZ"] = \
        lambda value: (10 ** 4) * value
    CONVERSIONS["CHZ:NHZ"] = \
        lambda value: (10 ** 7) * value
    CONVERSIONS["CHZ:PETAHZ"] = \
        lambda value: (10 ** -17) * value
    CONVERSIONS["CHZ:PHZ"] = \
        lambda value: (10 ** 10) * value
    CONVERSIONS["CHZ:TERAHZ"] = \
        lambda value: (10 ** -14) * value
    CONVERSIONS["CHZ:YHZ"] = \
        lambda value: (10 ** 22) * value
    CONVERSIONS["CHZ:YOTTAHZ"] = \
        lambda value: (10 ** -26) * value
    CONVERSIONS["CHZ:ZETTAHZ"] = \
        lambda value: (10 ** -23) * value
    CONVERSIONS["CHZ:ZHZ"] = \
        lambda value: (10 ** 19) * value
    CONVERSIONS["DAHZ:AHZ"] = \
        lambda value: (10 ** 19) * value
    CONVERSIONS["DAHZ:CHZ"] = \
        lambda value: (10 ** 3) * value
    CONVERSIONS["DAHZ:DHZ"] = \
        lambda value: (10 ** 2) * value
    CONVERSIONS["DAHZ:EXAHZ"] = \
        lambda value: (10 ** -17) * value
    CONVERSIONS["DAHZ:FHZ"] = \
        lambda value: (10 ** 16) * value
    CONVERSIONS["DAHZ:GIGAHZ"] = \
        lambda value: (10 ** -8) * value
    CONVERSIONS["DAHZ:HHZ"] = \
        lambda value: (10 ** -1) * value
    CONVERSIONS["DAHZ:HZ"] = \
        lambda value: 10 * value
    CONVERSIONS["DAHZ:KHZ"] = \
        lambda value: (10 ** -2) * value
    CONVERSIONS["DAHZ:MEGAHZ"] = \
        lambda value: (10 ** -5) * value
    CONVERSIONS["DAHZ:MHZ"] = \
        lambda value: (10 ** 4) * value
    CONVERSIONS["DAHZ:MICROHZ"] = \
        lambda value: (10 ** 7) * value
    CONVERSIONS["DAHZ:NHZ"] = \
        lambda value: (10 ** 10) * value
    CONVERSIONS["DAHZ:PETAHZ"] = \
        lambda value: (10 ** -14) * value
    CONVERSIONS["DAHZ:PHZ"] = \
        lambda value: (10 ** 13) * value
    CONVERSIONS["DAHZ:TERAHZ"] = \
        lambda value: (10 ** -11) * value
    CONVERSIONS["DAHZ:YHZ"] = \
        lambda value: (10 ** 25) * value
    CONVERSIONS["DAHZ:YOTTAHZ"] = \
        lambda value: (10 ** -23) * value
    CONVERSIONS["DAHZ:ZETTAHZ"] = \
        lambda value: (10 ** -20) * value
    CONVERSIONS["DAHZ:ZHZ"] = \
        lambda value: (10 ** 22) * value
    CONVERSIONS["DHZ:AHZ"] = \
        lambda value: (10 ** 17) * value
    CONVERSIONS["DHZ:CHZ"] = \
        lambda value: 10 * value
    CONVERSIONS["DHZ:DAHZ"] = \
        lambda value: (10 ** -2) * value
    CONVERSIONS["DHZ:EXAHZ"] = \
        lambda value: (10 ** -19) * value
    CONVERSIONS["DHZ:FHZ"] = \
        lambda value: (10 ** 14) * value
    CONVERSIONS["DHZ:GIGAHZ"] = \
        lambda value: (10 ** -10) * value
    CONVERSIONS["DHZ:HHZ"] = \
        lambda value: (10 ** -3) * value
    CONVERSIONS["DHZ:HZ"] = \
        lambda value: (10 ** -1) * value
    CONVERSIONS["DHZ:KHZ"] = \
        lambda value: (10 ** -4) * value
    CONVERSIONS["DHZ:MEGAHZ"] = \
        lambda value: (10 ** -7) * value
    CONVERSIONS["DHZ:MHZ"] = \
        lambda value: (10 ** 2) * value
    CONVERSIONS["DHZ:MICROHZ"] = \
        lambda value: (10 ** 5) * value
    CONVERSIONS["DHZ:NHZ"] = \
        lambda value: (10 ** 8) * value
    CONVERSIONS["DHZ:PETAHZ"] = \
        lambda value: (10 ** -16) * value
    CONVERSIONS["DHZ:PHZ"] = \
        lambda value: (10 ** 11) * value
    CONVERSIONS["DHZ:TERAHZ"] = \
        lambda value: (10 ** -13) * value
    CONVERSIONS["DHZ:YHZ"] = \
        lambda value: (10 ** 23) * value
    CONVERSIONS["DHZ:YOTTAHZ"] = \
        lambda value: (10 ** -25) * value
    CONVERSIONS["DHZ:ZETTAHZ"] = \
        lambda value: (10 ** -22) * value
    CONVERSIONS["DHZ:ZHZ"] = \
        lambda value: (10 ** 20) * value
    CONVERSIONS["EXAHZ:AHZ"] = \
        lambda value: (10 ** 36) * value
    CONVERSIONS["EXAHZ:CHZ"] = \
        lambda value: (10 ** 20) * value
    CONVERSIONS["EXAHZ:DAHZ"] = \
        lambda value: (10 ** 17) * value
    CONVERSIONS["EXAHZ:DHZ"] = \
        lambda value: (10 ** 19) * value
    CONVERSIONS["EXAHZ:FHZ"] = \
        lambda value: (10 ** 33) * value
    CONVERSIONS["EXAHZ:GIGAHZ"] = \
        lambda value: (10 ** 9) * value
    CONVERSIONS["EXAHZ:HHZ"] = \
        lambda value: (10 ** 16) * value
    CONVERSIONS["EXAHZ:HZ"] = \
        lambda value: (10 ** 18) * value
    CONVERSIONS["EXAHZ:KHZ"] = \
        lambda value: (10 ** 15) * value
    CONVERSIONS["EXAHZ:MEGAHZ"] = \
        lambda value: (10 ** 12) * value
    CONVERSIONS["EXAHZ:MHZ"] = \
        lambda value: (10 ** 21) * value
    CONVERSIONS["EXAHZ:MICROHZ"] = \
        lambda value: (10 ** 24) * value
    CONVERSIONS["EXAHZ:NHZ"] = \
        lambda value: (10 ** 27) * value
    CONVERSIONS["EXAHZ:PETAHZ"] = \
        lambda value: (10 ** 3) * value
    CONVERSIONS["EXAHZ:PHZ"] = \
        lambda value: (10 ** 30) * value
    CONVERSIONS["EXAHZ:TERAHZ"] = \
        lambda value: (10 ** 6) * value
    CONVERSIONS["EXAHZ:YHZ"] = \
        lambda value: (10 ** 42) * value
    CONVERSIONS["EXAHZ:YOTTAHZ"] = \
        lambda value: (10 ** -6) * value
    CONVERSIONS["EXAHZ:ZETTAHZ"] = \
        lambda value: (10 ** -3) * value
    CONVERSIONS["EXAHZ:ZHZ"] = \
        lambda value: (10 ** 39) * value
    CONVERSIONS["FHZ:AHZ"] = \
        lambda value: (10 ** 3) * value
    CONVERSIONS["FHZ:CHZ"] = \
        lambda value: (10 ** -13) * value
    CONVERSIONS["FHZ:DAHZ"] = \
        lambda value: (10 ** -16) * value
    CONVERSIONS["FHZ:DHZ"] = \
        lambda value: (10 ** -14) * value
    CONVERSIONS["FHZ:EXAHZ"] = \
        lambda value: (10 ** -33) * value
    CONVERSIONS["FHZ:GIGAHZ"] = \
        lambda value: (10 ** -24) * value
    CONVERSIONS["FHZ:HHZ"] = \
        lambda value: (10 ** -17) * value
    CONVERSIONS["FHZ:HZ"] = \
        lambda value: (10 ** -15) * value
    CONVERSIONS["FHZ:KHZ"] = \
        lambda value: (10 ** -18) * value
    CONVERSIONS["FHZ:MEGAHZ"] = \
        lambda value: (10 ** -21) * value
    CONVERSIONS["FHZ:MHZ"] = \
        lambda value: (10 ** -12) * value
    CONVERSIONS["FHZ:MICROHZ"] = \
        lambda value: (10 ** -9) * value
    CONVERSIONS["FHZ:NHZ"] = \
        lambda value: (10 ** -6) * value
    CONVERSIONS["FHZ:PETAHZ"] = \
        lambda value: (10 ** -30) * value
    CONVERSIONS["FHZ:PHZ"] = \
        lambda value: (10 ** -3) * value
    CONVERSIONS["FHZ:TERAHZ"] = \
        lambda value: (10 ** -27) * value
    CONVERSIONS["FHZ:YHZ"] = \
        lambda value: (10 ** 9) * value
    CONVERSIONS["FHZ:YOTTAHZ"] = \
        lambda value: (10 ** -39) * value
    CONVERSIONS["FHZ:ZETTAHZ"] = \
        lambda value: (10 ** -36) * value
    CONVERSIONS["FHZ:ZHZ"] = \
        lambda value: (10 ** 6) * value
    CONVERSIONS["GIGAHZ:AHZ"] = \
        lambda value: (10 ** 27) * value
    CONVERSIONS["GIGAHZ:CHZ"] = \
        lambda value: (10 ** 11) * value
    CONVERSIONS["GIGAHZ:DAHZ"] = \
        lambda value: (10 ** 8) * value
    CONVERSIONS["GIGAHZ:DHZ"] = \
        lambda value: (10 ** 10) * value
    CONVERSIONS["GIGAHZ:EXAHZ"] = \
        lambda value: (10 ** -9) * value
    CONVERSIONS["GIGAHZ:FHZ"] = \
        lambda value: (10 ** 24) * value
    CONVERSIONS["GIGAHZ:HHZ"] = \
        lambda value: (10 ** 7) * value
    CONVERSIONS["GIGAHZ:HZ"] = \
        lambda value: (10 ** 9) * value
    CONVERSIONS["GIGAHZ:KHZ"] = \
        lambda value: (10 ** 6) * value
    CONVERSIONS["GIGAHZ:MEGAHZ"] = \
        lambda value: (10 ** 3) * value
    CONVERSIONS["GIGAHZ:MHZ"] = \
        lambda value: (10 ** 12) * value
    CONVERSIONS["GIGAHZ:MICROHZ"] = \
        lambda value: (10 ** 15) * value
    CONVERSIONS["GIGAHZ:NHZ"] = \
        lambda value: (10 ** 18) * value
    CONVERSIONS["GIGAHZ:PETAHZ"] = \
        lambda value: (10 ** -6) * value
    CONVERSIONS["GIGAHZ:PHZ"] = \
        lambda value: (10 ** 21) * value
    CONVERSIONS["GIGAHZ:TERAHZ"] = \
        lambda value: (10 ** -3) * value
    CONVERSIONS["GIGAHZ:YHZ"] = \
        lambda value: (10 ** 33) * value
    CONVERSIONS["GIGAHZ:YOTTAHZ"] = \
        lambda value: (10 ** -15) * value
    CONVERSIONS["GIGAHZ:ZETTAHZ"] = \
        lambda value: (10 ** -12) * value
    CONVERSIONS["GIGAHZ:ZHZ"] = \
        lambda value: (10 ** 30) * value
    CONVERSIONS["HHZ:AHZ"] = \
        lambda value: (10 ** 20) * value
    CONVERSIONS["HHZ:CHZ"] = \
        lambda value: (10 ** 4) * value
    CONVERSIONS["HHZ:DAHZ"] = \
        lambda value: 10 * value
    CONVERSIONS["HHZ:DHZ"] = \
        lambda value: (10 ** 3) * value
    CONVERSIONS["HHZ:EXAHZ"] = \
        lambda value: (10 ** -16) * value
    CONVERSIONS["HHZ:FHZ"] = \
        lambda value: (10 ** 17) * value
    CONVERSIONS["HHZ:GIGAHZ"] = \
        lambda value: (10 ** -7) * value
    CONVERSIONS["HHZ:HZ"] = \
        lambda value: (10 ** 2) * value
    CONVERSIONS["HHZ:KHZ"] = \
        lambda value: (10 ** -1) * value
    CONVERSIONS["HHZ:MEGAHZ"] = \
        lambda value: (10 ** -4) * value
    CONVERSIONS["HHZ:MHZ"] = \
        lambda value: (10 ** 5) * value
    CONVERSIONS["HHZ:MICROHZ"] = \
        lambda value: (10 ** 8) * value
    CONVERSIONS["HHZ:NHZ"] = \
        lambda value: (10 ** 11) * value
    CONVERSIONS["HHZ:PETAHZ"] = \
        lambda value: (10 ** -13) * value
    CONVERSIONS["HHZ:PHZ"] = \
        lambda value: (10 ** 14) * value
    CONVERSIONS["HHZ:TERAHZ"] = \
        lambda value: (10 ** -10) * value
    CONVERSIONS["HHZ:YHZ"] = \
        lambda value: (10 ** 26) * value
    CONVERSIONS["HHZ:YOTTAHZ"] = \
        lambda value: (10 ** -22) * value
    CONVERSIONS["HHZ:ZETTAHZ"] = \
        lambda value: (10 ** -19) * value
    CONVERSIONS["HHZ:ZHZ"] = \
        lambda value: (10 ** 23) * value
    CONVERSIONS["HZ:AHZ"] = \
        lambda value: (10 ** 18) * value
    CONVERSIONS["HZ:CHZ"] = \
        lambda value: (10 ** 2) * value
    CONVERSIONS["HZ:DAHZ"] = \
        lambda value: (10 ** -1) * value
    CONVERSIONS["HZ:DHZ"] = \
        lambda value: 10 * value
    CONVERSIONS["HZ:EXAHZ"] = \
        lambda value: (10 ** -18) * value
    CONVERSIONS["HZ:FHZ"] = \
        lambda value: (10 ** 15) * value
    CONVERSIONS["HZ:GIGAHZ"] = \
        lambda value: (10 ** -9) * value
    CONVERSIONS["HZ:HHZ"] = \
        lambda value: (10 ** -2) * value
    CONVERSIONS["HZ:KHZ"] = \
        lambda value: (10 ** -3) * value
    CONVERSIONS["HZ:MEGAHZ"] = \
        lambda value: (10 ** -6) * value
    CONVERSIONS["HZ:MHZ"] = \
        lambda value: (10 ** 3) * value
    CONVERSIONS["HZ:MICROHZ"] = \
        lambda value: (10 ** 6) * value
    CONVERSIONS["HZ:NHZ"] = \
        lambda value: (10 ** 9) * value
    CONVERSIONS["HZ:PETAHZ"] = \
        lambda value: (10 ** -15) * value
    CONVERSIONS["HZ:PHZ"] = \
        lambda value: (10 ** 12) * value
    CONVERSIONS["HZ:TERAHZ"] = \
        lambda value: (10 ** -12) * value
    CONVERSIONS["HZ:YHZ"] = \
        lambda value: (10 ** 24) * value
    CONVERSIONS["HZ:YOTTAHZ"] = \
        lambda value: (10 ** -24) * value
    CONVERSIONS["HZ:ZETTAHZ"] = \
        lambda value: (10 ** -21) * value
    CONVERSIONS["HZ:ZHZ"] = \
        lambda value: (10 ** 21) * value
    CONVERSIONS["KHZ:AHZ"] = \
        lambda value: (10 ** 21) * value
    CONVERSIONS["KHZ:CHZ"] = \
        lambda value: (10 ** 5) * value
    CONVERSIONS["KHZ:DAHZ"] = \
        lambda value: (10 ** 2) * value
    CONVERSIONS["KHZ:DHZ"] = \
        lambda value: (10 ** 4) * value
    CONVERSIONS["KHZ:EXAHZ"] = \
        lambda value: (10 ** -15) * value
    CONVERSIONS["KHZ:FHZ"] = \
        lambda value: (10 ** 18) * value
    CONVERSIONS["KHZ:GIGAHZ"] = \
        lambda value: (10 ** -6) * value
    CONVERSIONS["KHZ:HHZ"] = \
        lambda value: 10 * value
    CONVERSIONS["KHZ:HZ"] = \
        lambda value: (10 ** 3) * value
    CONVERSIONS["KHZ:MEGAHZ"] = \
        lambda value: (10 ** -3) * value
    CONVERSIONS["KHZ:MHZ"] = \
        lambda value: (10 ** 6) * value
    CONVERSIONS["KHZ:MICROHZ"] = \
        lambda value: (10 ** 9) * value
    CONVERSIONS["KHZ:NHZ"] = \
        lambda value: (10 ** 12) * value
    CONVERSIONS["KHZ:PETAHZ"] = \
        lambda value: (10 ** -12) * value
    CONVERSIONS["KHZ:PHZ"] = \
        lambda value: (10 ** 15) * value
    CONVERSIONS["KHZ:TERAHZ"] = \
        lambda value: (10 ** -9) * value
    CONVERSIONS["KHZ:YHZ"] = \
        lambda value: (10 ** 27) * value
    CONVERSIONS["KHZ:YOTTAHZ"] = \
        lambda value: (10 ** -21) * value
    CONVERSIONS["KHZ:ZETTAHZ"] = \
        lambda value: (10 ** -18) * value
    CONVERSIONS["KHZ:ZHZ"] = \
        lambda value: (10 ** 24) * value
    CONVERSIONS["MEGAHZ:AHZ"] = \
        lambda value: (10 ** 24) * value
    CONVERSIONS["MEGAHZ:CHZ"] = \
        lambda value: (10 ** 8) * value
    CONVERSIONS["MEGAHZ:DAHZ"] = \
        lambda value: (10 ** 5) * value
    CONVERSIONS["MEGAHZ:DHZ"] = \
        lambda value: (10 ** 7) * value
    CONVERSIONS["MEGAHZ:EXAHZ"] = \
        lambda value: (10 ** -12) * value
    CONVERSIONS["MEGAHZ:FHZ"] = \
        lambda value: (10 ** 21) * value
    CONVERSIONS["MEGAHZ:GIGAHZ"] = \
        lambda value: (10 ** -3) * value
    CONVERSIONS["MEGAHZ:HHZ"] = \
        lambda value: (10 ** 4) * value
    CONVERSIONS["MEGAHZ:HZ"] = \
        lambda value: (10 ** 6) * value
    CONVERSIONS["MEGAHZ:KHZ"] = \
        lambda value: (10 ** 3) * value
    CONVERSIONS["MEGAHZ:MHZ"] = \
        lambda value: (10 ** 9) * value
    CONVERSIONS["MEGAHZ:MICROHZ"] = \
        lambda value: (10 ** 12) * value
    CONVERSIONS["MEGAHZ:NHZ"] = \
        lambda value: (10 ** 15) * value
    CONVERSIONS["MEGAHZ:PETAHZ"] = \
        lambda value: (10 ** -9) * value
    CONVERSIONS["MEGAHZ:PHZ"] = \
        lambda value: (10 ** 18) * value
    CONVERSIONS["MEGAHZ:TERAHZ"] = \
        lambda value: (10 ** -6) * value
    CONVERSIONS["MEGAHZ:YHZ"] = \
        lambda value: (10 ** 30) * value
    CONVERSIONS["MEGAHZ:YOTTAHZ"] = \
        lambda value: (10 ** -18) * value
    CONVERSIONS["MEGAHZ:ZETTAHZ"] = \
        lambda value: (10 ** -15) * value
    CONVERSIONS["MEGAHZ:ZHZ"] = \
        lambda value: (10 ** 27) * value
    CONVERSIONS["MHZ:AHZ"] = \
        lambda value: (10 ** 15) * value
    CONVERSIONS["MHZ:CHZ"] = \
        lambda value: (10 ** -1) * value
    CONVERSIONS["MHZ:DAHZ"] = \
        lambda value: (10 ** -4) * value
    CONVERSIONS["MHZ:DHZ"] = \
        lambda value: (10 ** -2) * value
    CONVERSIONS["MHZ:EXAHZ"] = \
        lambda value: (10 ** -21) * value
    CONVERSIONS["MHZ:FHZ"] = \
        lambda value: (10 ** 12) * value
    CONVERSIONS["MHZ:GIGAHZ"] = \
        lambda value: (10 ** -12) * value
    CONVERSIONS["MHZ:HHZ"] = \
        lambda value: (10 ** -5) * value
    CONVERSIONS["MHZ:HZ"] = \
        lambda value: (10 ** -3) * value
    CONVERSIONS["MHZ:KHZ"] = \
        lambda value: (10 ** -6) * value
    CONVERSIONS["MHZ:MEGAHZ"] = \
        lambda value: (10 ** -9) * value
    CONVERSIONS["MHZ:MICROHZ"] = \
        lambda value: (10 ** 3) * value
    CONVERSIONS["MHZ:NHZ"] = \
        lambda value: (10 ** 6) * value
    CONVERSIONS["MHZ:PETAHZ"] = \
        lambda value: (10 ** -18) * value
    CONVERSIONS["MHZ:PHZ"] = \
        lambda value: (10 ** 9) * value
    CONVERSIONS["MHZ:TERAHZ"] = \
        lambda value: (10 ** -15) * value
    CONVERSIONS["MHZ:YHZ"] = \
        lambda value: (10 ** 21) * value
    CONVERSIONS["MHZ:YOTTAHZ"] = \
        lambda value: (10 ** -27) * value
    CONVERSIONS["MHZ:ZETTAHZ"] = \
        lambda value: (10 ** -24) * value
    CONVERSIONS["MHZ:ZHZ"] = \
        lambda value: (10 ** 18) * value
    CONVERSIONS["MICROHZ:AHZ"] = \
        lambda value: (10 ** 12) * value
    CONVERSIONS["MICROHZ:CHZ"] = \
        lambda value: (10 ** -4) * value
    CONVERSIONS["MICROHZ:DAHZ"] = \
        lambda value: (10 ** -7) * value
    CONVERSIONS["MICROHZ:DHZ"] = \
        lambda value: (10 ** -5) * value
    CONVERSIONS["MICROHZ:EXAHZ"] = \
        lambda value: (10 ** -24) * value
    CONVERSIONS["MICROHZ:FHZ"] = \
        lambda value: (10 ** 9) * value
    CONVERSIONS["MICROHZ:GIGAHZ"] = \
        lambda value: (10 ** -15) * value
    CONVERSIONS["MICROHZ:HHZ"] = \
        lambda value: (10 ** -8) * value
    CONVERSIONS["MICROHZ:HZ"] = \
        lambda value: (10 ** -6) * value
    CONVERSIONS["MICROHZ:KHZ"] = \
        lambda value: (10 ** -9) * value
    CONVERSIONS["MICROHZ:MEGAHZ"] = \
        lambda value: (10 ** -12) * value
    CONVERSIONS["MICROHZ:MHZ"] = \
        lambda value: (10 ** -3) * value
    CONVERSIONS["MICROHZ:NHZ"] = \
        lambda value: (10 ** 3) * value
    CONVERSIONS["MICROHZ:PETAHZ"] = \
        lambda value: (10 ** -21) * value
    CONVERSIONS["MICROHZ:PHZ"] = \
        lambda value: (10 ** 6) * value
    CONVERSIONS["MICROHZ:TERAHZ"] = \
        lambda value: (10 ** -18) * value
    CONVERSIONS["MICROHZ:YHZ"] = \
        lambda value: (10 ** 18) * value
    CONVERSIONS["MICROHZ:YOTTAHZ"] = \
        lambda value: (10 ** -30) * value
    CONVERSIONS["MICROHZ:ZETTAHZ"] = \
        lambda value: (10 ** -27) * value
    CONVERSIONS["MICROHZ:ZHZ"] = \
        lambda value: (10 ** 15) * value
    CONVERSIONS["NHZ:AHZ"] = \
        lambda value: (10 ** 9) * value
    CONVERSIONS["NHZ:CHZ"] = \
        lambda value: (10 ** -7) * value
    CONVERSIONS["NHZ:DAHZ"] = \
        lambda value: (10 ** -10) * value
    CONVERSIONS["NHZ:DHZ"] = \
        lambda value: (10 ** -8) * value
    CONVERSIONS["NHZ:EXAHZ"] = \
        lambda value: (10 ** -27) * value
    CONVERSIONS["NHZ:FHZ"] = \
        lambda value: (10 ** 6) * value
    CONVERSIONS["NHZ:GIGAHZ"] = \
        lambda value: (10 ** -18) * value
    CONVERSIONS["NHZ:HHZ"] = \
        lambda value: (10 ** -11) * value
    CONVERSIONS["NHZ:HZ"] = \
        lambda value: (10 ** -9) * value
    CONVERSIONS["NHZ:KHZ"] = \
        lambda value: (10 ** -12) * value
    CONVERSIONS["NHZ:MEGAHZ"] = \
        lambda value: (10 ** -15) * value
    CONVERSIONS["NHZ:MHZ"] = \
        lambda value: (10 ** -6) * value
    CONVERSIONS["NHZ:MICROHZ"] = \
        lambda value: (10 ** -3) * value
    CONVERSIONS["NHZ:PETAHZ"] = \
        lambda value: (10 ** -24) * value
    CONVERSIONS["NHZ:PHZ"] = \
        lambda value: (10 ** 3) * value
    CONVERSIONS["NHZ:TERAHZ"] = \
        lambda value: (10 ** -21) * value
    CONVERSIONS["NHZ:YHZ"] = \
        lambda value: (10 ** 15) * value
    CONVERSIONS["NHZ:YOTTAHZ"] = \
        lambda value: (10 ** -33) * value
    CONVERSIONS["NHZ:ZETTAHZ"] = \
        lambda value: (10 ** -30) * value
    CONVERSIONS["NHZ:ZHZ"] = \
        lambda value: (10 ** 12) * value
    CONVERSIONS["PETAHZ:AHZ"] = \
        lambda value: (10 ** 33) * value
    CONVERSIONS["PETAHZ:CHZ"] = \
        lambda value: (10 ** 17) * value
    CONVERSIONS["PETAHZ:DAHZ"] = \
        lambda value: (10 ** 14) * value
    CONVERSIONS["PETAHZ:DHZ"] = \
        lambda value: (10 ** 16) * value
    CONVERSIONS["PETAHZ:EXAHZ"] = \
        lambda value: (10 ** -3) * value
    CONVERSIONS["PETAHZ:FHZ"] = \
        lambda value: (10 ** 30) * value
    CONVERSIONS["PETAHZ:GIGAHZ"] = \
        lambda value: (10 ** 6) * value
    CONVERSIONS["PETAHZ:HHZ"] = \
        lambda value: (10 ** 13) * value
    CONVERSIONS["PETAHZ:HZ"] = \
        lambda value: (10 ** 15) * value
    CONVERSIONS["PETAHZ:KHZ"] = \
        lambda value: (10 ** 12) * value
    CONVERSIONS["PETAHZ:MEGAHZ"] = \
        lambda value: (10 ** 9) * value
    CONVERSIONS["PETAHZ:MHZ"] = \
        lambda value: (10 ** 18) * value
    CONVERSIONS["PETAHZ:MICROHZ"] = \
        lambda value: (10 ** 21) * value
    CONVERSIONS["PETAHZ:NHZ"] = \
        lambda value: (10 ** 24) * value
    CONVERSIONS["PETAHZ:PHZ"] = \
        lambda value: (10 ** 27) * value
    CONVERSIONS["PETAHZ:TERAHZ"] = \
        lambda value: (10 ** 3) * value
    CONVERSIONS["PETAHZ:YHZ"] = \
        lambda value: (10 ** 39) * value
    CONVERSIONS["PETAHZ:YOTTAHZ"] = \
        lambda value: (10 ** -9) * value
    CONVERSIONS["PETAHZ:ZETTAHZ"] = \
        lambda value: (10 ** -6) * value
    CONVERSIONS["PETAHZ:ZHZ"] = \
        lambda value: (10 ** 36) * value
    CONVERSIONS["PHZ:AHZ"] = \
        lambda value: (10 ** 6) * value
    CONVERSIONS["PHZ:CHZ"] = \
        lambda value: (10 ** -10) * value
    CONVERSIONS["PHZ:DAHZ"] = \
        lambda value: (10 ** -13) * value
    CONVERSIONS["PHZ:DHZ"] = \
        lambda value: (10 ** -11) * value
    CONVERSIONS["PHZ:EXAHZ"] = \
        lambda value: (10 ** -30) * value
    CONVERSIONS["PHZ:FHZ"] = \
        lambda value: (10 ** 3) * value
    CONVERSIONS["PHZ:GIGAHZ"] = \
        lambda value: (10 ** -21) * value
    CONVERSIONS["PHZ:HHZ"] = \
        lambda value: (10 ** -14) * value
    CONVERSIONS["PHZ:HZ"] = \
        lambda value: (10 ** -12) * value
    CONVERSIONS["PHZ:KHZ"] = \
        lambda value: (10 ** -15) * value
    CONVERSIONS["PHZ:MEGAHZ"] = \
        lambda value: (10 ** -18) * value
    CONVERSIONS["PHZ:MHZ"] = \
        lambda value: (10 ** -9) * value
    CONVERSIONS["PHZ:MICROHZ"] = \
        lambda value: (10 ** -6) * value
    CONVERSIONS["PHZ:NHZ"] = \
        lambda value: (10 ** -3) * value
    CONVERSIONS["PHZ:PETAHZ"] = \
        lambda value: (10 ** -27) * value
    CONVERSIONS["PHZ:TERAHZ"] = \
        lambda value: (10 ** -24) * value
    CONVERSIONS["PHZ:YHZ"] = \
        lambda value: (10 ** 12) * value
    CONVERSIONS["PHZ:YOTTAHZ"] = \
        lambda value: (10 ** -36) * value
    CONVERSIONS["PHZ:ZETTAHZ"] = \
        lambda value: (10 ** -33) * value
    CONVERSIONS["PHZ:ZHZ"] = \
        lambda value: (10 ** 9) * value
    CONVERSIONS["TERAHZ:AHZ"] = \
        lambda value: (10 ** 30) * value
    CONVERSIONS["TERAHZ:CHZ"] = \
        lambda value: (10 ** 14) * value
    CONVERSIONS["TERAHZ:DAHZ"] = \
        lambda value: (10 ** 11) * value
    CONVERSIONS["TERAHZ:DHZ"] = \
        lambda value: (10 ** 13) * value
    CONVERSIONS["TERAHZ:EXAHZ"] = \
        lambda value: (10 ** -6) * value
    CONVERSIONS["TERAHZ:FHZ"] = \
        lambda value: (10 ** 27) * value
    CONVERSIONS["TERAHZ:GIGAHZ"] = \
        lambda value: (10 ** 3) * value
    CONVERSIONS["TERAHZ:HHZ"] = \
        lambda value: (10 ** 10) * value
    CONVERSIONS["TERAHZ:HZ"] = \
        lambda value: (10 ** 12) * value
    CONVERSIONS["TERAHZ:KHZ"] = \
        lambda value: (10 ** 9) * value
    CONVERSIONS["TERAHZ:MEGAHZ"] = \
        lambda value: (10 ** 6) * value
    CONVERSIONS["TERAHZ:MHZ"] = \
        lambda value: (10 ** 15) * value
    CONVERSIONS["TERAHZ:MICROHZ"] = \
        lambda value: (10 ** 18) * value
    CONVERSIONS["TERAHZ:NHZ"] = \
        lambda value: (10 ** 21) * value
    CONVERSIONS["TERAHZ:PETAHZ"] = \
        lambda value: (10 ** -3) * value
    CONVERSIONS["TERAHZ:PHZ"] = \
        lambda value: (10 ** 24) * value
    CONVERSIONS["TERAHZ:YHZ"] = \
        lambda value: (10 ** 36) * value
    CONVERSIONS["TERAHZ:YOTTAHZ"] = \
        lambda value: (10 ** -12) * value
    CONVERSIONS["TERAHZ:ZETTAHZ"] = \
        lambda value: (10 ** -9) * value
    CONVERSIONS["TERAHZ:ZHZ"] = \
        lambda value: (10 ** 33) * value
    CONVERSIONS["YHZ:AHZ"] = \
        lambda value: (10 ** -6) * value
    CONVERSIONS["YHZ:CHZ"] = \
        lambda value: (10 ** -22) * value
    CONVERSIONS["YHZ:DAHZ"] = \
        lambda value: (10 ** -25) * value
    CONVERSIONS["YHZ:DHZ"] = \
        lambda value: (10 ** -23) * value
    CONVERSIONS["YHZ:EXAHZ"] = \
        lambda value: (10 ** -42) * value
    CONVERSIONS["YHZ:FHZ"] = \
        lambda value: (10 ** -9) * value
    CONVERSIONS["YHZ:GIGAHZ"] = \
        lambda value: (10 ** -33) * value
    CONVERSIONS["YHZ:HHZ"] = \
        lambda value: (10 ** -26) * value
    CONVERSIONS["YHZ:HZ"] = \
        lambda value: (10 ** -24) * value
    CONVERSIONS["YHZ:KHZ"] = \
        lambda value: (10 ** -27) * value
    CONVERSIONS["YHZ:MEGAHZ"] = \
        lambda value: (10 ** -30) * value
    CONVERSIONS["YHZ:MHZ"] = \
        lambda value: (10 ** -21) * value
    CONVERSIONS["YHZ:MICROHZ"] = \
        lambda value: (10 ** -18) * value
    CONVERSIONS["YHZ:NHZ"] = \
        lambda value: (10 ** -15) * value
    CONVERSIONS["YHZ:PETAHZ"] = \
        lambda value: (10 ** -39) * value
    CONVERSIONS["YHZ:PHZ"] = \
        lambda value: (10 ** -12) * value
    CONVERSIONS["YHZ:TERAHZ"] = \
        lambda value: (10 ** -36) * value
    CONVERSIONS["YHZ:YOTTAHZ"] = \
        lambda value: (10 ** -48) * value
    CONVERSIONS["YHZ:ZETTAHZ"] = \
        lambda value: (10 ** -45) * value
    CONVERSIONS["YHZ:ZHZ"] = \
        lambda value: (10 ** -3) * value
    CONVERSIONS["YOTTAHZ:AHZ"] = \
        lambda value: (10 ** 42) * value
    CONVERSIONS["YOTTAHZ:CHZ"] = \
        lambda value: (10 ** 26) * value
    CONVERSIONS["YOTTAHZ:DAHZ"] = \
        lambda value: (10 ** 23) * value
    CONVERSIONS["YOTTAHZ:DHZ"] = \
        lambda value: (10 ** 25) * value
    CONVERSIONS["YOTTAHZ:EXAHZ"] = \
        lambda value: (10 ** 6) * value
    CONVERSIONS["YOTTAHZ:FHZ"] = \
        lambda value: (10 ** 39) * value
    CONVERSIONS["YOTTAHZ:GIGAHZ"] = \
        lambda value: (10 ** 15) * value
    CONVERSIONS["YOTTAHZ:HHZ"] = \
        lambda value: (10 ** 22) * value
    CONVERSIONS["YOTTAHZ:HZ"] = \
        lambda value: (10 ** 24) * value
    CONVERSIONS["YOTTAHZ:KHZ"] = \
        lambda value: (10 ** 21) * value
    CONVERSIONS["YOTTAHZ:MEGAHZ"] = \
        lambda value: (10 ** 18) * value
    CONVERSIONS["YOTTAHZ:MHZ"] = \
        lambda value: (10 ** 27) * value
    CONVERSIONS["YOTTAHZ:MICROHZ"] = \
        lambda value: (10 ** 30) * value
    CONVERSIONS["YOTTAHZ:NHZ"] = \
        lambda value: (10 ** 33) * value
    CONVERSIONS["YOTTAHZ:PETAHZ"] = \
        lambda value: (10 ** 9) * value
    CONVERSIONS["YOTTAHZ:PHZ"] = \
        lambda value: (10 ** 36) * value
    CONVERSIONS["YOTTAHZ:TERAHZ"] = \
        lambda value: (10 ** 12) * value
    CONVERSIONS["YOTTAHZ:YHZ"] = \
        lambda value: (10 ** 48) * value
    CONVERSIONS["YOTTAHZ:ZETTAHZ"] = \
        lambda value: (10 ** 3) * value
    CONVERSIONS["YOTTAHZ:ZHZ"] = \
        lambda value: (10 ** 45) * value
    CONVERSIONS["ZETTAHZ:AHZ"] = \
        lambda value: (10 ** 39) * value
    CONVERSIONS["ZETTAHZ:CHZ"] = \
        lambda value: (10 ** 23) * value
    CONVERSIONS["ZETTAHZ:DAHZ"] = \
        lambda value: (10 ** 20) * value
    CONVERSIONS["ZETTAHZ:DHZ"] = \
        lambda value: (10 ** 22) * value
    CONVERSIONS["ZETTAHZ:EXAHZ"] = \
        lambda value: (10 ** 3) * value
    CONVERSIONS["ZETTAHZ:FHZ"] = \
        lambda value: (10 ** 36) * value
    CONVERSIONS["ZETTAHZ:GIGAHZ"] = \
        lambda value: (10 ** 12) * value
    CONVERSIONS["ZETTAHZ:HHZ"] = \
        lambda value: (10 ** 19) * value
    CONVERSIONS["ZETTAHZ:HZ"] = \
        lambda value: (10 ** 21) * value
    CONVERSIONS["ZETTAHZ:KHZ"] = \
        lambda value: (10 ** 18) * value
    CONVERSIONS["ZETTAHZ:MEGAHZ"] = \
        lambda value: (10 ** 15) * value
    CONVERSIONS["ZETTAHZ:MHZ"] = \
        lambda value: (10 ** 24) * value
    CONVERSIONS["ZETTAHZ:MICROHZ"] = \
        lambda value: (10 ** 27) * value
    CONVERSIONS["ZETTAHZ:NHZ"] = \
        lambda value: (10 ** 30) * value
    CONVERSIONS["ZETTAHZ:PETAHZ"] = \
        lambda value: (10 ** 6) * value
    CONVERSIONS["ZETTAHZ:PHZ"] = \
        lambda value: (10 ** 33) * value
    CONVERSIONS["ZETTAHZ:TERAHZ"] = \
        lambda value: (10 ** 9) * value
    CONVERSIONS["ZETTAHZ:YHZ"] = \
        lambda value: (10 ** 45) * value
    CONVERSIONS["ZETTAHZ:YOTTAHZ"] = \
        lambda value: (10 ** -3) * value
    CONVERSIONS["ZETTAHZ:ZHZ"] = \
        lambda value: (10 ** 42) * value
    CONVERSIONS["ZHZ:AHZ"] = \
        lambda value: (10 ** -3) * value
    CONVERSIONS["ZHZ:CHZ"] = \
        lambda value: (10 ** -19) * value
    CONVERSIONS["ZHZ:DAHZ"] = \
        lambda value: (10 ** -22) * value
    CONVERSIONS["ZHZ:DHZ"] = \
        lambda value: (10 ** -20) * value
    CONVERSIONS["ZHZ:EXAHZ"] = \
        lambda value: (10 ** -39) * value
    CONVERSIONS["ZHZ:FHZ"] = \
        lambda value: (10 ** -6) * value
    CONVERSIONS["ZHZ:GIGAHZ"] = \
        lambda value: (10 ** -30) * value
    CONVERSIONS["ZHZ:HHZ"] = \
        lambda value: (10 ** -23) * value
    CONVERSIONS["ZHZ:HZ"] = \
        lambda value: (10 ** -21) * value
    CONVERSIONS["ZHZ:KHZ"] = \
        lambda value: (10 ** -24) * value
    CONVERSIONS["ZHZ:MEGAHZ"] = \
        lambda value: (10 ** -27) * value
    CONVERSIONS["ZHZ:MHZ"] = \
        lambda value: (10 ** -18) * value
    CONVERSIONS["ZHZ:MICROHZ"] = \
        lambda value: (10 ** -15) * value
    CONVERSIONS["ZHZ:NHZ"] = \
        lambda value: (10 ** -12) * value
    CONVERSIONS["ZHZ:PETAHZ"] = \
        lambda value: (10 ** -36) * value
    CONVERSIONS["ZHZ:PHZ"] = \
        lambda value: (10 ** -9) * value
    CONVERSIONS["ZHZ:TERAHZ"] = \
        lambda value: (10 ** -33) * value
    CONVERSIONS["ZHZ:YHZ"] = \
        lambda value: (10 ** 3) * value
    CONVERSIONS["ZHZ:YOTTAHZ"] = \
        lambda value: (10 ** -45) * value
    CONVERSIONS["ZHZ:ZETTAHZ"] = \
        lambda value: (10 ** -42) * value

    SYMBOLS = dict()
    SYMBOLS["AHZ"] = "aHz"
    SYMBOLS["CHZ"] = "cHz"
    SYMBOLS["DAHZ"] = "daHz"
    SYMBOLS["DHZ"] = "dHz"
    SYMBOLS["EXAHZ"] = "EHz"
    SYMBOLS["FHZ"] = "fHz"
    SYMBOLS["GIGAHZ"] = "GHz"
    SYMBOLS["HHZ"] = "hHz"
    SYMBOLS["HZ"] = "Hz"
    SYMBOLS["KHZ"] = "kHz"
    SYMBOLS["MEGAHZ"] = "MHz"
    SYMBOLS["MHZ"] = "mHz"
    SYMBOLS["MICROHZ"] = "µHz"
    SYMBOLS["NHZ"] = "nHz"
    SYMBOLS["PETAHZ"] = "PHz"
    SYMBOLS["PHZ"] = "pHz"
    SYMBOLS["TERAHZ"] = "THz"
    SYMBOLS["YHZ"] = "yHz"
    SYMBOLS["YOTTAHZ"] = "YHz"
    SYMBOLS["ZETTAHZ"] = "ZHz"
    SYMBOLS["ZHZ"] = "zHz"

    def __init__(self, value=None, unit=None):
        _omero_model.Frequency.__init__(self)
        if isinstance(value, _omero_model.FrequencyI):
            # This is a copy-constructor call.
            target = str(unit)
            source = str(value.getUnit())
            if target == source:
                self.setValue(value.getValue())
                self.setUnit(value.getUnit())
            else:
                c = self.CONVERSIONS.get("%s:%s" % (source, target))
                if c is None:
                    t = (value.getValue(), value.getUnit(), target)
                    msg = "%s %s cannot be converted to %s" % t
                    raise Exception(msg)
                self.setValue(c(value.getValue()))
                self.setUnit(getattr(UnitsFrequency, str(target)))
        else:
            self.setValue(value)
            self.setUnit(unit)

    def getUnit(self, current=None):
        return self._unit

    def getValue(self, current=None):
        return self._value

    def getSymbol(self, current=None):
        return self.SYMBOLS.get(str(self.getUnit()))

    @staticmethod
    def lookupSymbol(unit):
        return FrequencyI.SYMBOLS.get(str(unit))

    def setUnit(self, unit, current=None):
        self._unit = unit

    def setValue(self, value, current=None):
        self._value = value

    def __str__(self):
        return self._base_string(self.getValue(), self.getUnit())

_omero_model.FrequencyI = FrequencyI
