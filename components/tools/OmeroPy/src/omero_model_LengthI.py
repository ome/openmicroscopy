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
Code-generated omero.model.Length implementation,
based on omero.model.PermissionsI
"""


import Ice
import IceImport
IceImport.load("omero_model_Length_ice")
_omero = Ice.openModule("omero")
_omero_model = Ice.openModule("omero.model")
__name__ = "omero.model"

from omero_model_UnitBase import UnitBase
from omero.model.enums import UnitsLength


def noconversion(cfrom, cto):
    raise Exception(("Unsupported conversion: "
                     "%s:%s") % cfrom, cto)


class LengthI(_omero_model.Length, UnitBase):

    CONVERSIONS = dict()
    CONVERSIONS["ANGSTROM:ASTRONOMICALUNIT"] = \
        lambda: noconversion("ANGSTROM", "ASTRONOMICALUNIT")
    CONVERSIONS["ANGSTROM:ATTOMETER"] = \
        lambda value: (10 ** 8) * value
    CONVERSIONS["ANGSTROM:CENTIMETER"] = \
        lambda value: (10 ** -8) * value
    CONVERSIONS["ANGSTROM:DECAMETER"] = \
        lambda value: (10 ** -11) * value
    CONVERSIONS["ANGSTROM:DECIMETER"] = \
        lambda value: (10 ** -9) * value
    CONVERSIONS["ANGSTROM:EXAMETER"] = \
        lambda value: (10 ** -28) * value
    CONVERSIONS["ANGSTROM:FEMTOMETER"] = \
        lambda value: (10 ** 5) * value
    CONVERSIONS["ANGSTROM:FT"] = \
        lambda: noconversion("ANGSTROM", "FT")
    CONVERSIONS["ANGSTROM:GIGAMETER"] = \
        lambda value: (10 ** -19) * value
    CONVERSIONS["ANGSTROM:HECTOMETER"] = \
        lambda value: (10 ** -12) * value
    CONVERSIONS["ANGSTROM:IN"] = \
        lambda: noconversion("ANGSTROM", "IN")
    CONVERSIONS["ANGSTROM:KILOMETER"] = \
        lambda value: (10 ** -13) * value
    CONVERSIONS["ANGSTROM:LI"] = \
        lambda: noconversion("ANGSTROM", "LI")
    CONVERSIONS["ANGSTROM:LIGHTYEAR"] = \
        lambda: noconversion("ANGSTROM", "LIGHTYEAR")
    CONVERSIONS["ANGSTROM:MEGAMETER"] = \
        lambda value: (10 ** -16) * value
    CONVERSIONS["ANGSTROM:METER"] = \
        lambda value: (10 ** -10) * value
    CONVERSIONS["ANGSTROM:MI"] = \
        lambda: noconversion("ANGSTROM", "MI")
    CONVERSIONS["ANGSTROM:MICROMETER"] = \
        lambda value: (10 ** -4) * value
    CONVERSIONS["ANGSTROM:MILLIMETER"] = \
        lambda value: (10 ** -7) * value
    CONVERSIONS["ANGSTROM:NANOMETER"] = \
        lambda value: (10 ** -1) * value
    CONVERSIONS["ANGSTROM:PARSEC"] = \
        lambda: noconversion("ANGSTROM", "PARSEC")
    CONVERSIONS["ANGSTROM:PETAMETER"] = \
        lambda value: (10 ** -25) * value
    CONVERSIONS["ANGSTROM:PICOMETER"] = \
        lambda value: (10 ** 2) * value
    CONVERSIONS["ANGSTROM:PIXEL"] = \
        lambda: noconversion("ANGSTROM", "PIXEL")
    CONVERSIONS["ANGSTROM:PT"] = \
        lambda: noconversion("ANGSTROM", "PT")
    CONVERSIONS["ANGSTROM:REFERENCEFRAME"] = \
        lambda: noconversion("ANGSTROM", "REFERENCEFRAME")
    CONVERSIONS["ANGSTROM:TERAMETER"] = \
        lambda value: (10 ** -22) * value
    CONVERSIONS["ANGSTROM:THOU"] = \
        lambda: noconversion("ANGSTROM", "THOU")
    CONVERSIONS["ANGSTROM:YD"] = \
        lambda: noconversion("ANGSTROM", "YD")
    CONVERSIONS["ANGSTROM:YOCTOMETER"] = \
        lambda value: (10 ** 14) * value
    CONVERSIONS["ANGSTROM:YOTTAMETER"] = \
        lambda value: (10 ** -34) * value
    CONVERSIONS["ANGSTROM:ZEPTOMETER"] = \
        lambda value: (10 ** 11) * value
    CONVERSIONS["ANGSTROM:ZETTAMETER"] = \
        lambda value: (10 ** -31) * value
    CONVERSIONS["ASTRONOMICALUNIT:ANGSTROM"] = \
        lambda: noconversion("ASTRONOMICALUNIT", "ANGSTROM")
    CONVERSIONS["ASTRONOMICALUNIT:ATTOMETER"] = \
        lambda: noconversion("ASTRONOMICALUNIT", "ATTOMETER")
    CONVERSIONS["ASTRONOMICALUNIT:CENTIMETER"] = \
        lambda: noconversion("ASTRONOMICALUNIT", "CENTIMETER")
    CONVERSIONS["ASTRONOMICALUNIT:DECAMETER"] = \
        lambda: noconversion("ASTRONOMICALUNIT", "DECAMETER")
    CONVERSIONS["ASTRONOMICALUNIT:DECIMETER"] = \
        lambda: noconversion("ASTRONOMICALUNIT", "DECIMETER")
    CONVERSIONS["ASTRONOMICALUNIT:EXAMETER"] = \
        lambda: noconversion("ASTRONOMICALUNIT", "EXAMETER")
    CONVERSIONS["ASTRONOMICALUNIT:FEMTOMETER"] = \
        lambda: noconversion("ASTRONOMICALUNIT", "FEMTOMETER")
    CONVERSIONS["ASTRONOMICALUNIT:FT"] = \
        lambda: noconversion("ASTRONOMICALUNIT", "FT")
    CONVERSIONS["ASTRONOMICALUNIT:GIGAMETER"] = \
        lambda: noconversion("ASTRONOMICALUNIT", "GIGAMETER")
    CONVERSIONS["ASTRONOMICALUNIT:HECTOMETER"] = \
        lambda: noconversion("ASTRONOMICALUNIT", "HECTOMETER")
    CONVERSIONS["ASTRONOMICALUNIT:IN"] = \
        lambda: noconversion("ASTRONOMICALUNIT", "IN")
    CONVERSIONS["ASTRONOMICALUNIT:KILOMETER"] = \
        lambda: noconversion("ASTRONOMICALUNIT", "KILOMETER")
    CONVERSIONS["ASTRONOMICALUNIT:LI"] = \
        lambda: noconversion("ASTRONOMICALUNIT", "LI")
    CONVERSIONS["ASTRONOMICALUNIT:LIGHTYEAR"] = \
        lambda: noconversion("ASTRONOMICALUNIT", "LIGHTYEAR")
    CONVERSIONS["ASTRONOMICALUNIT:MEGAMETER"] = \
        lambda: noconversion("ASTRONOMICALUNIT", "MEGAMETER")
    CONVERSIONS["ASTRONOMICALUNIT:METER"] = \
        lambda: noconversion("ASTRONOMICALUNIT", "METER")
    CONVERSIONS["ASTRONOMICALUNIT:MI"] = \
        lambda: noconversion("ASTRONOMICALUNIT", "MI")
    CONVERSIONS["ASTRONOMICALUNIT:MICROMETER"] = \
        lambda: noconversion("ASTRONOMICALUNIT", "MICROMETER")
    CONVERSIONS["ASTRONOMICALUNIT:MILLIMETER"] = \
        lambda: noconversion("ASTRONOMICALUNIT", "MILLIMETER")
    CONVERSIONS["ASTRONOMICALUNIT:NANOMETER"] = \
        lambda: noconversion("ASTRONOMICALUNIT", "NANOMETER")
    CONVERSIONS["ASTRONOMICALUNIT:PARSEC"] = \
        lambda: noconversion("ASTRONOMICALUNIT", "PARSEC")
    CONVERSIONS["ASTRONOMICALUNIT:PETAMETER"] = \
        lambda: noconversion("ASTRONOMICALUNIT", "PETAMETER")
    CONVERSIONS["ASTRONOMICALUNIT:PICOMETER"] = \
        lambda: noconversion("ASTRONOMICALUNIT", "PICOMETER")
    CONVERSIONS["ASTRONOMICALUNIT:PIXEL"] = \
        lambda: noconversion("ASTRONOMICALUNIT", "PIXEL")
    CONVERSIONS["ASTRONOMICALUNIT:PT"] = \
        lambda: noconversion("ASTRONOMICALUNIT", "PT")
    CONVERSIONS["ASTRONOMICALUNIT:REFERENCEFRAME"] = \
        lambda: noconversion("ASTRONOMICALUNIT", "REFERENCEFRAME")
    CONVERSIONS["ASTRONOMICALUNIT:TERAMETER"] = \
        lambda: noconversion("ASTRONOMICALUNIT", "TERAMETER")
    CONVERSIONS["ASTRONOMICALUNIT:THOU"] = \
        lambda: noconversion("ASTRONOMICALUNIT", "THOU")
    CONVERSIONS["ASTRONOMICALUNIT:YD"] = \
        lambda: noconversion("ASTRONOMICALUNIT", "YD")
    CONVERSIONS["ASTRONOMICALUNIT:YOCTOMETER"] = \
        lambda: noconversion("ASTRONOMICALUNIT", "YOCTOMETER")
    CONVERSIONS["ASTRONOMICALUNIT:YOTTAMETER"] = \
        lambda: noconversion("ASTRONOMICALUNIT", "YOTTAMETER")
    CONVERSIONS["ASTRONOMICALUNIT:ZEPTOMETER"] = \
        lambda: noconversion("ASTRONOMICALUNIT", "ZEPTOMETER")
    CONVERSIONS["ASTRONOMICALUNIT:ZETTAMETER"] = \
        lambda: noconversion("ASTRONOMICALUNIT", "ZETTAMETER")
    CONVERSIONS["ATTOMETER:ANGSTROM"] = \
        lambda value: (10 ** -8) * value
    CONVERSIONS["ATTOMETER:ASTRONOMICALUNIT"] = \
        lambda: noconversion("ATTOMETER", "ASTRONOMICALUNIT")
    CONVERSIONS["ATTOMETER:CENTIMETER"] = \
        lambda value: (10 ** -16) * value
    CONVERSIONS["ATTOMETER:DECAMETER"] = \
        lambda value: (10 ** -19) * value
    CONVERSIONS["ATTOMETER:DECIMETER"] = \
        lambda value: (10 ** -17) * value
    CONVERSIONS["ATTOMETER:EXAMETER"] = \
        lambda value: (10 ** -36) * value
    CONVERSIONS["ATTOMETER:FEMTOMETER"] = \
        lambda value: (10 ** -3) * value
    CONVERSIONS["ATTOMETER:FT"] = \
        lambda: noconversion("ATTOMETER", "FT")
    CONVERSIONS["ATTOMETER:GIGAMETER"] = \
        lambda value: (10 ** -27) * value
    CONVERSIONS["ATTOMETER:HECTOMETER"] = \
        lambda value: (10 ** -20) * value
    CONVERSIONS["ATTOMETER:IN"] = \
        lambda: noconversion("ATTOMETER", "IN")
    CONVERSIONS["ATTOMETER:KILOMETER"] = \
        lambda value: (10 ** -21) * value
    CONVERSIONS["ATTOMETER:LI"] = \
        lambda: noconversion("ATTOMETER", "LI")
    CONVERSIONS["ATTOMETER:LIGHTYEAR"] = \
        lambda: noconversion("ATTOMETER", "LIGHTYEAR")
    CONVERSIONS["ATTOMETER:MEGAMETER"] = \
        lambda value: (10 ** -24) * value
    CONVERSIONS["ATTOMETER:METER"] = \
        lambda value: (10 ** -18) * value
    CONVERSIONS["ATTOMETER:MI"] = \
        lambda: noconversion("ATTOMETER", "MI")
    CONVERSIONS["ATTOMETER:MICROMETER"] = \
        lambda value: (10 ** -12) * value
    CONVERSIONS["ATTOMETER:MILLIMETER"] = \
        lambda value: (10 ** -15) * value
    CONVERSIONS["ATTOMETER:NANOMETER"] = \
        lambda value: (10 ** -9) * value
    CONVERSIONS["ATTOMETER:PARSEC"] = \
        lambda: noconversion("ATTOMETER", "PARSEC")
    CONVERSIONS["ATTOMETER:PETAMETER"] = \
        lambda value: (10 ** -33) * value
    CONVERSIONS["ATTOMETER:PICOMETER"] = \
        lambda value: (10 ** -6) * value
    CONVERSIONS["ATTOMETER:PIXEL"] = \
        lambda: noconversion("ATTOMETER", "PIXEL")
    CONVERSIONS["ATTOMETER:PT"] = \
        lambda: noconversion("ATTOMETER", "PT")
    CONVERSIONS["ATTOMETER:REFERENCEFRAME"] = \
        lambda: noconversion("ATTOMETER", "REFERENCEFRAME")
    CONVERSIONS["ATTOMETER:TERAMETER"] = \
        lambda value: (10 ** -30) * value
    CONVERSIONS["ATTOMETER:THOU"] = \
        lambda: noconversion("ATTOMETER", "THOU")
    CONVERSIONS["ATTOMETER:YD"] = \
        lambda: noconversion("ATTOMETER", "YD")
    CONVERSIONS["ATTOMETER:YOCTOMETER"] = \
        lambda value: (10 ** 6) * value
    CONVERSIONS["ATTOMETER:YOTTAMETER"] = \
        lambda value: (10 ** -42) * value
    CONVERSIONS["ATTOMETER:ZEPTOMETER"] = \
        lambda value: (10 ** 3) * value
    CONVERSIONS["ATTOMETER:ZETTAMETER"] = \
        lambda value: (10 ** -39) * value
    CONVERSIONS["CENTIMETER:ANGSTROM"] = \
        lambda value: (10 ** 8) * value
    CONVERSIONS["CENTIMETER:ASTRONOMICALUNIT"] = \
        lambda: noconversion("CENTIMETER", "ASTRONOMICALUNIT")
    CONVERSIONS["CENTIMETER:ATTOMETER"] = \
        lambda value: (10 ** 16) * value
    CONVERSIONS["CENTIMETER:DECAMETER"] = \
        lambda value: (10 ** -3) * value
    CONVERSIONS["CENTIMETER:DECIMETER"] = \
        lambda value: (10 ** -1) * value
    CONVERSIONS["CENTIMETER:EXAMETER"] = \
        lambda value: (10 ** -20) * value
    CONVERSIONS["CENTIMETER:FEMTOMETER"] = \
        lambda value: (10 ** 13) * value
    CONVERSIONS["CENTIMETER:FT"] = \
        lambda: noconversion("CENTIMETER", "FT")
    CONVERSIONS["CENTIMETER:GIGAMETER"] = \
        lambda value: (10 ** -11) * value
    CONVERSIONS["CENTIMETER:HECTOMETER"] = \
        lambda value: (10 ** -4) * value
    CONVERSIONS["CENTIMETER:IN"] = \
        lambda: noconversion("CENTIMETER", "IN")
    CONVERSIONS["CENTIMETER:KILOMETER"] = \
        lambda value: (10 ** -5) * value
    CONVERSIONS["CENTIMETER:LI"] = \
        lambda: noconversion("CENTIMETER", "LI")
    CONVERSIONS["CENTIMETER:LIGHTYEAR"] = \
        lambda: noconversion("CENTIMETER", "LIGHTYEAR")
    CONVERSIONS["CENTIMETER:MEGAMETER"] = \
        lambda value: (10 ** -8) * value
    CONVERSIONS["CENTIMETER:METER"] = \
        lambda value: (10 ** -2) * value
    CONVERSIONS["CENTIMETER:MI"] = \
        lambda: noconversion("CENTIMETER", "MI")
    CONVERSIONS["CENTIMETER:MICROMETER"] = \
        lambda value: (10 ** 4) * value
    CONVERSIONS["CENTIMETER:MILLIMETER"] = \
        lambda value: 10 * value
    CONVERSIONS["CENTIMETER:NANOMETER"] = \
        lambda value: (10 ** 7) * value
    CONVERSIONS["CENTIMETER:PARSEC"] = \
        lambda: noconversion("CENTIMETER", "PARSEC")
    CONVERSIONS["CENTIMETER:PETAMETER"] = \
        lambda value: (10 ** -17) * value
    CONVERSIONS["CENTIMETER:PICOMETER"] = \
        lambda value: (10 ** 10) * value
    CONVERSIONS["CENTIMETER:PIXEL"] = \
        lambda: noconversion("CENTIMETER", "PIXEL")
    CONVERSIONS["CENTIMETER:PT"] = \
        lambda: noconversion("CENTIMETER", "PT")
    CONVERSIONS["CENTIMETER:REFERENCEFRAME"] = \
        lambda: noconversion("CENTIMETER", "REFERENCEFRAME")
    CONVERSIONS["CENTIMETER:TERAMETER"] = \
        lambda value: (10 ** -14) * value
    CONVERSIONS["CENTIMETER:THOU"] = \
        lambda: noconversion("CENTIMETER", "THOU")
    CONVERSIONS["CENTIMETER:YD"] = \
        lambda: noconversion("CENTIMETER", "YD")
    CONVERSIONS["CENTIMETER:YOCTOMETER"] = \
        lambda value: (10 ** 22) * value
    CONVERSIONS["CENTIMETER:YOTTAMETER"] = \
        lambda value: (10 ** -26) * value
    CONVERSIONS["CENTIMETER:ZEPTOMETER"] = \
        lambda value: (10 ** 19) * value
    CONVERSIONS["CENTIMETER:ZETTAMETER"] = \
        lambda value: (10 ** -23) * value
    CONVERSIONS["DECAMETER:ANGSTROM"] = \
        lambda value: (10 ** 11) * value
    CONVERSIONS["DECAMETER:ASTRONOMICALUNIT"] = \
        lambda: noconversion("DECAMETER", "ASTRONOMICALUNIT")
    CONVERSIONS["DECAMETER:ATTOMETER"] = \
        lambda value: (10 ** 19) * value
    CONVERSIONS["DECAMETER:CENTIMETER"] = \
        lambda value: (10 ** 3) * value
    CONVERSIONS["DECAMETER:DECIMETER"] = \
        lambda value: (10 ** 2) * value
    CONVERSIONS["DECAMETER:EXAMETER"] = \
        lambda value: (10 ** -17) * value
    CONVERSIONS["DECAMETER:FEMTOMETER"] = \
        lambda value: (10 ** 16) * value
    CONVERSIONS["DECAMETER:FT"] = \
        lambda: noconversion("DECAMETER", "FT")
    CONVERSIONS["DECAMETER:GIGAMETER"] = \
        lambda value: (10 ** -8) * value
    CONVERSIONS["DECAMETER:HECTOMETER"] = \
        lambda value: (10 ** -1) * value
    CONVERSIONS["DECAMETER:IN"] = \
        lambda: noconversion("DECAMETER", "IN")
    CONVERSIONS["DECAMETER:KILOMETER"] = \
        lambda value: (10 ** -2) * value
    CONVERSIONS["DECAMETER:LI"] = \
        lambda: noconversion("DECAMETER", "LI")
    CONVERSIONS["DECAMETER:LIGHTYEAR"] = \
        lambda: noconversion("DECAMETER", "LIGHTYEAR")
    CONVERSIONS["DECAMETER:MEGAMETER"] = \
        lambda value: (10 ** -5) * value
    CONVERSIONS["DECAMETER:METER"] = \
        lambda value: 10 * value
    CONVERSIONS["DECAMETER:MI"] = \
        lambda: noconversion("DECAMETER", "MI")
    CONVERSIONS["DECAMETER:MICROMETER"] = \
        lambda value: (10 ** 7) * value
    CONVERSIONS["DECAMETER:MILLIMETER"] = \
        lambda value: (10 ** 4) * value
    CONVERSIONS["DECAMETER:NANOMETER"] = \
        lambda value: (10 ** 10) * value
    CONVERSIONS["DECAMETER:PARSEC"] = \
        lambda: noconversion("DECAMETER", "PARSEC")
    CONVERSIONS["DECAMETER:PETAMETER"] = \
        lambda value: (10 ** -14) * value
    CONVERSIONS["DECAMETER:PICOMETER"] = \
        lambda value: (10 ** 13) * value
    CONVERSIONS["DECAMETER:PIXEL"] = \
        lambda: noconversion("DECAMETER", "PIXEL")
    CONVERSIONS["DECAMETER:PT"] = \
        lambda: noconversion("DECAMETER", "PT")
    CONVERSIONS["DECAMETER:REFERENCEFRAME"] = \
        lambda: noconversion("DECAMETER", "REFERENCEFRAME")
    CONVERSIONS["DECAMETER:TERAMETER"] = \
        lambda value: (10 ** -11) * value
    CONVERSIONS["DECAMETER:THOU"] = \
        lambda: noconversion("DECAMETER", "THOU")
    CONVERSIONS["DECAMETER:YD"] = \
        lambda: noconversion("DECAMETER", "YD")
    CONVERSIONS["DECAMETER:YOCTOMETER"] = \
        lambda value: (10 ** 25) * value
    CONVERSIONS["DECAMETER:YOTTAMETER"] = \
        lambda value: (10 ** -23) * value
    CONVERSIONS["DECAMETER:ZEPTOMETER"] = \
        lambda value: (10 ** 22) * value
    CONVERSIONS["DECAMETER:ZETTAMETER"] = \
        lambda value: (10 ** -20) * value
    CONVERSIONS["DECIMETER:ANGSTROM"] = \
        lambda value: (10 ** 9) * value
    CONVERSIONS["DECIMETER:ASTRONOMICALUNIT"] = \
        lambda: noconversion("DECIMETER", "ASTRONOMICALUNIT")
    CONVERSIONS["DECIMETER:ATTOMETER"] = \
        lambda value: (10 ** 17) * value
    CONVERSIONS["DECIMETER:CENTIMETER"] = \
        lambda value: 10 * value
    CONVERSIONS["DECIMETER:DECAMETER"] = \
        lambda value: (10 ** -2) * value
    CONVERSIONS["DECIMETER:EXAMETER"] = \
        lambda value: (10 ** -19) * value
    CONVERSIONS["DECIMETER:FEMTOMETER"] = \
        lambda value: (10 ** 14) * value
    CONVERSIONS["DECIMETER:FT"] = \
        lambda: noconversion("DECIMETER", "FT")
    CONVERSIONS["DECIMETER:GIGAMETER"] = \
        lambda value: (10 ** -10) * value
    CONVERSIONS["DECIMETER:HECTOMETER"] = \
        lambda value: (10 ** -3) * value
    CONVERSIONS["DECIMETER:IN"] = \
        lambda: noconversion("DECIMETER", "IN")
    CONVERSIONS["DECIMETER:KILOMETER"] = \
        lambda value: (10 ** -4) * value
    CONVERSIONS["DECIMETER:LI"] = \
        lambda: noconversion("DECIMETER", "LI")
    CONVERSIONS["DECIMETER:LIGHTYEAR"] = \
        lambda: noconversion("DECIMETER", "LIGHTYEAR")
    CONVERSIONS["DECIMETER:MEGAMETER"] = \
        lambda value: (10 ** -7) * value
    CONVERSIONS["DECIMETER:METER"] = \
        lambda value: (10 ** -1) * value
    CONVERSIONS["DECIMETER:MI"] = \
        lambda: noconversion("DECIMETER", "MI")
    CONVERSIONS["DECIMETER:MICROMETER"] = \
        lambda value: (10 ** 5) * value
    CONVERSIONS["DECIMETER:MILLIMETER"] = \
        lambda value: (10 ** 2) * value
    CONVERSIONS["DECIMETER:NANOMETER"] = \
        lambda value: (10 ** 8) * value
    CONVERSIONS["DECIMETER:PARSEC"] = \
        lambda: noconversion("DECIMETER", "PARSEC")
    CONVERSIONS["DECIMETER:PETAMETER"] = \
        lambda value: (10 ** -16) * value
    CONVERSIONS["DECIMETER:PICOMETER"] = \
        lambda value: (10 ** 11) * value
    CONVERSIONS["DECIMETER:PIXEL"] = \
        lambda: noconversion("DECIMETER", "PIXEL")
    CONVERSIONS["DECIMETER:PT"] = \
        lambda: noconversion("DECIMETER", "PT")
    CONVERSIONS["DECIMETER:REFERENCEFRAME"] = \
        lambda: noconversion("DECIMETER", "REFERENCEFRAME")
    CONVERSIONS["DECIMETER:TERAMETER"] = \
        lambda value: (10 ** -13) * value
    CONVERSIONS["DECIMETER:THOU"] = \
        lambda: noconversion("DECIMETER", "THOU")
    CONVERSIONS["DECIMETER:YD"] = \
        lambda: noconversion("DECIMETER", "YD")
    CONVERSIONS["DECIMETER:YOCTOMETER"] = \
        lambda value: (10 ** 23) * value
    CONVERSIONS["DECIMETER:YOTTAMETER"] = \
        lambda value: (10 ** -25) * value
    CONVERSIONS["DECIMETER:ZEPTOMETER"] = \
        lambda value: (10 ** 20) * value
    CONVERSIONS["DECIMETER:ZETTAMETER"] = \
        lambda value: (10 ** -22) * value
    CONVERSIONS["EXAMETER:ANGSTROM"] = \
        lambda value: (10 ** 28) * value
    CONVERSIONS["EXAMETER:ASTRONOMICALUNIT"] = \
        lambda: noconversion("EXAMETER", "ASTRONOMICALUNIT")
    CONVERSIONS["EXAMETER:ATTOMETER"] = \
        lambda value: (10 ** 36) * value
    CONVERSIONS["EXAMETER:CENTIMETER"] = \
        lambda value: (10 ** 20) * value
    CONVERSIONS["EXAMETER:DECAMETER"] = \
        lambda value: (10 ** 17) * value
    CONVERSIONS["EXAMETER:DECIMETER"] = \
        lambda value: (10 ** 19) * value
    CONVERSIONS["EXAMETER:FEMTOMETER"] = \
        lambda value: (10 ** 33) * value
    CONVERSIONS["EXAMETER:FT"] = \
        lambda: noconversion("EXAMETER", "FT")
    CONVERSIONS["EXAMETER:GIGAMETER"] = \
        lambda value: (10 ** 9) * value
    CONVERSIONS["EXAMETER:HECTOMETER"] = \
        lambda value: (10 ** 16) * value
    CONVERSIONS["EXAMETER:IN"] = \
        lambda: noconversion("EXAMETER", "IN")
    CONVERSIONS["EXAMETER:KILOMETER"] = \
        lambda value: (10 ** 15) * value
    CONVERSIONS["EXAMETER:LI"] = \
        lambda: noconversion("EXAMETER", "LI")
    CONVERSIONS["EXAMETER:LIGHTYEAR"] = \
        lambda: noconversion("EXAMETER", "LIGHTYEAR")
    CONVERSIONS["EXAMETER:MEGAMETER"] = \
        lambda value: (10 ** 12) * value
    CONVERSIONS["EXAMETER:METER"] = \
        lambda value: (10 ** 18) * value
    CONVERSIONS["EXAMETER:MI"] = \
        lambda: noconversion("EXAMETER", "MI")
    CONVERSIONS["EXAMETER:MICROMETER"] = \
        lambda value: (10 ** 24) * value
    CONVERSIONS["EXAMETER:MILLIMETER"] = \
        lambda value: (10 ** 21) * value
    CONVERSIONS["EXAMETER:NANOMETER"] = \
        lambda value: (10 ** 27) * value
    CONVERSIONS["EXAMETER:PARSEC"] = \
        lambda: noconversion("EXAMETER", "PARSEC")
    CONVERSIONS["EXAMETER:PETAMETER"] = \
        lambda value: (10 ** 3) * value
    CONVERSIONS["EXAMETER:PICOMETER"] = \
        lambda value: (10 ** 30) * value
    CONVERSIONS["EXAMETER:PIXEL"] = \
        lambda: noconversion("EXAMETER", "PIXEL")
    CONVERSIONS["EXAMETER:PT"] = \
        lambda: noconversion("EXAMETER", "PT")
    CONVERSIONS["EXAMETER:REFERENCEFRAME"] = \
        lambda: noconversion("EXAMETER", "REFERENCEFRAME")
    CONVERSIONS["EXAMETER:TERAMETER"] = \
        lambda value: (10 ** 6) * value
    CONVERSIONS["EXAMETER:THOU"] = \
        lambda: noconversion("EXAMETER", "THOU")
    CONVERSIONS["EXAMETER:YD"] = \
        lambda: noconversion("EXAMETER", "YD")
    CONVERSIONS["EXAMETER:YOCTOMETER"] = \
        lambda value: (10 ** 42) * value
    CONVERSIONS["EXAMETER:YOTTAMETER"] = \
        lambda value: (10 ** -6) * value
    CONVERSIONS["EXAMETER:ZEPTOMETER"] = \
        lambda value: (10 ** 39) * value
    CONVERSIONS["EXAMETER:ZETTAMETER"] = \
        lambda value: (10 ** -3) * value
    CONVERSIONS["FEMTOMETER:ANGSTROM"] = \
        lambda value: (10 ** -5) * value
    CONVERSIONS["FEMTOMETER:ASTRONOMICALUNIT"] = \
        lambda: noconversion("FEMTOMETER", "ASTRONOMICALUNIT")
    CONVERSIONS["FEMTOMETER:ATTOMETER"] = \
        lambda value: (10 ** 3) * value
    CONVERSIONS["FEMTOMETER:CENTIMETER"] = \
        lambda value: (10 ** -13) * value
    CONVERSIONS["FEMTOMETER:DECAMETER"] = \
        lambda value: (10 ** -16) * value
    CONVERSIONS["FEMTOMETER:DECIMETER"] = \
        lambda value: (10 ** -14) * value
    CONVERSIONS["FEMTOMETER:EXAMETER"] = \
        lambda value: (10 ** -33) * value
    CONVERSIONS["FEMTOMETER:FT"] = \
        lambda: noconversion("FEMTOMETER", "FT")
    CONVERSIONS["FEMTOMETER:GIGAMETER"] = \
        lambda value: (10 ** -24) * value
    CONVERSIONS["FEMTOMETER:HECTOMETER"] = \
        lambda value: (10 ** -17) * value
    CONVERSIONS["FEMTOMETER:IN"] = \
        lambda: noconversion("FEMTOMETER", "IN")
    CONVERSIONS["FEMTOMETER:KILOMETER"] = \
        lambda value: (10 ** -18) * value
    CONVERSIONS["FEMTOMETER:LI"] = \
        lambda: noconversion("FEMTOMETER", "LI")
    CONVERSIONS["FEMTOMETER:LIGHTYEAR"] = \
        lambda: noconversion("FEMTOMETER", "LIGHTYEAR")
    CONVERSIONS["FEMTOMETER:MEGAMETER"] = \
        lambda value: (10 ** -21) * value
    CONVERSIONS["FEMTOMETER:METER"] = \
        lambda value: (10 ** -15) * value
    CONVERSIONS["FEMTOMETER:MI"] = \
        lambda: noconversion("FEMTOMETER", "MI")
    CONVERSIONS["FEMTOMETER:MICROMETER"] = \
        lambda value: (10 ** -9) * value
    CONVERSIONS["FEMTOMETER:MILLIMETER"] = \
        lambda value: (10 ** -12) * value
    CONVERSIONS["FEMTOMETER:NANOMETER"] = \
        lambda value: (10 ** -6) * value
    CONVERSIONS["FEMTOMETER:PARSEC"] = \
        lambda: noconversion("FEMTOMETER", "PARSEC")
    CONVERSIONS["FEMTOMETER:PETAMETER"] = \
        lambda value: (10 ** -30) * value
    CONVERSIONS["FEMTOMETER:PICOMETER"] = \
        lambda value: (10 ** -3) * value
    CONVERSIONS["FEMTOMETER:PIXEL"] = \
        lambda: noconversion("FEMTOMETER", "PIXEL")
    CONVERSIONS["FEMTOMETER:PT"] = \
        lambda: noconversion("FEMTOMETER", "PT")
    CONVERSIONS["FEMTOMETER:REFERENCEFRAME"] = \
        lambda: noconversion("FEMTOMETER", "REFERENCEFRAME")
    CONVERSIONS["FEMTOMETER:TERAMETER"] = \
        lambda value: (10 ** -27) * value
    CONVERSIONS["FEMTOMETER:THOU"] = \
        lambda: noconversion("FEMTOMETER", "THOU")
    CONVERSIONS["FEMTOMETER:YD"] = \
        lambda: noconversion("FEMTOMETER", "YD")
    CONVERSIONS["FEMTOMETER:YOCTOMETER"] = \
        lambda value: (10 ** 9) * value
    CONVERSIONS["FEMTOMETER:YOTTAMETER"] = \
        lambda value: (10 ** -39) * value
    CONVERSIONS["FEMTOMETER:ZEPTOMETER"] = \
        lambda value: (10 ** 6) * value
    CONVERSIONS["FEMTOMETER:ZETTAMETER"] = \
        lambda value: (10 ** -36) * value
    CONVERSIONS["FT:ANGSTROM"] = \
        lambda: noconversion("FT", "ANGSTROM")
    CONVERSIONS["FT:ASTRONOMICALUNIT"] = \
        lambda: noconversion("FT", "ASTRONOMICALUNIT")
    CONVERSIONS["FT:ATTOMETER"] = \
        lambda: noconversion("FT", "ATTOMETER")
    CONVERSIONS["FT:CENTIMETER"] = \
        lambda: noconversion("FT", "CENTIMETER")
    CONVERSIONS["FT:DECAMETER"] = \
        lambda: noconversion("FT", "DECAMETER")
    CONVERSIONS["FT:DECIMETER"] = \
        lambda: noconversion("FT", "DECIMETER")
    CONVERSIONS["FT:EXAMETER"] = \
        lambda: noconversion("FT", "EXAMETER")
    CONVERSIONS["FT:FEMTOMETER"] = \
        lambda: noconversion("FT", "FEMTOMETER")
    CONVERSIONS["FT:GIGAMETER"] = \
        lambda: noconversion("FT", "GIGAMETER")
    CONVERSIONS["FT:HECTOMETER"] = \
        lambda: noconversion("FT", "HECTOMETER")
    CONVERSIONS["FT:IN"] = \
        lambda: noconversion("FT", "IN")
    CONVERSIONS["FT:KILOMETER"] = \
        lambda: noconversion("FT", "KILOMETER")
    CONVERSIONS["FT:LI"] = \
        lambda: noconversion("FT", "LI")
    CONVERSIONS["FT:LIGHTYEAR"] = \
        lambda: noconversion("FT", "LIGHTYEAR")
    CONVERSIONS["FT:MEGAMETER"] = \
        lambda: noconversion("FT", "MEGAMETER")
    CONVERSIONS["FT:METER"] = \
        lambda: noconversion("FT", "METER")
    CONVERSIONS["FT:MI"] = \
        lambda: noconversion("FT", "MI")
    CONVERSIONS["FT:MICROMETER"] = \
        lambda: noconversion("FT", "MICROMETER")
    CONVERSIONS["FT:MILLIMETER"] = \
        lambda: noconversion("FT", "MILLIMETER")
    CONVERSIONS["FT:NANOMETER"] = \
        lambda: noconversion("FT", "NANOMETER")
    CONVERSIONS["FT:PARSEC"] = \
        lambda: noconversion("FT", "PARSEC")
    CONVERSIONS["FT:PETAMETER"] = \
        lambda: noconversion("FT", "PETAMETER")
    CONVERSIONS["FT:PICOMETER"] = \
        lambda: noconversion("FT", "PICOMETER")
    CONVERSIONS["FT:PIXEL"] = \
        lambda: noconversion("FT", "PIXEL")
    CONVERSIONS["FT:PT"] = \
        lambda: noconversion("FT", "PT")
    CONVERSIONS["FT:REFERENCEFRAME"] = \
        lambda: noconversion("FT", "REFERENCEFRAME")
    CONVERSIONS["FT:TERAMETER"] = \
        lambda: noconversion("FT", "TERAMETER")
    CONVERSIONS["FT:THOU"] = \
        lambda: noconversion("FT", "THOU")
    CONVERSIONS["FT:YD"] = \
        lambda: noconversion("FT", "YD")
    CONVERSIONS["FT:YOCTOMETER"] = \
        lambda: noconversion("FT", "YOCTOMETER")
    CONVERSIONS["FT:YOTTAMETER"] = \
        lambda: noconversion("FT", "YOTTAMETER")
    CONVERSIONS["FT:ZEPTOMETER"] = \
        lambda: noconversion("FT", "ZEPTOMETER")
    CONVERSIONS["FT:ZETTAMETER"] = \
        lambda: noconversion("FT", "ZETTAMETER")
    CONVERSIONS["GIGAMETER:ANGSTROM"] = \
        lambda value: (10 ** 19) * value
    CONVERSIONS["GIGAMETER:ASTRONOMICALUNIT"] = \
        lambda: noconversion("GIGAMETER", "ASTRONOMICALUNIT")
    CONVERSIONS["GIGAMETER:ATTOMETER"] = \
        lambda value: (10 ** 27) * value
    CONVERSIONS["GIGAMETER:CENTIMETER"] = \
        lambda value: (10 ** 11) * value
    CONVERSIONS["GIGAMETER:DECAMETER"] = \
        lambda value: (10 ** 8) * value
    CONVERSIONS["GIGAMETER:DECIMETER"] = \
        lambda value: (10 ** 10) * value
    CONVERSIONS["GIGAMETER:EXAMETER"] = \
        lambda value: (10 ** -9) * value
    CONVERSIONS["GIGAMETER:FEMTOMETER"] = \
        lambda value: (10 ** 24) * value
    CONVERSIONS["GIGAMETER:FT"] = \
        lambda: noconversion("GIGAMETER", "FT")
    CONVERSIONS["GIGAMETER:HECTOMETER"] = \
        lambda value: (10 ** 7) * value
    CONVERSIONS["GIGAMETER:IN"] = \
        lambda: noconversion("GIGAMETER", "IN")
    CONVERSIONS["GIGAMETER:KILOMETER"] = \
        lambda value: (10 ** 6) * value
    CONVERSIONS["GIGAMETER:LI"] = \
        lambda: noconversion("GIGAMETER", "LI")
    CONVERSIONS["GIGAMETER:LIGHTYEAR"] = \
        lambda: noconversion("GIGAMETER", "LIGHTYEAR")
    CONVERSIONS["GIGAMETER:MEGAMETER"] = \
        lambda value: (10 ** 3) * value
    CONVERSIONS["GIGAMETER:METER"] = \
        lambda value: (10 ** 9) * value
    CONVERSIONS["GIGAMETER:MI"] = \
        lambda: noconversion("GIGAMETER", "MI")
    CONVERSIONS["GIGAMETER:MICROMETER"] = \
        lambda value: (10 ** 15) * value
    CONVERSIONS["GIGAMETER:MILLIMETER"] = \
        lambda value: (10 ** 12) * value
    CONVERSIONS["GIGAMETER:NANOMETER"] = \
        lambda value: (10 ** 18) * value
    CONVERSIONS["GIGAMETER:PARSEC"] = \
        lambda: noconversion("GIGAMETER", "PARSEC")
    CONVERSIONS["GIGAMETER:PETAMETER"] = \
        lambda value: (10 ** -6) * value
    CONVERSIONS["GIGAMETER:PICOMETER"] = \
        lambda value: (10 ** 21) * value
    CONVERSIONS["GIGAMETER:PIXEL"] = \
        lambda: noconversion("GIGAMETER", "PIXEL")
    CONVERSIONS["GIGAMETER:PT"] = \
        lambda: noconversion("GIGAMETER", "PT")
    CONVERSIONS["GIGAMETER:REFERENCEFRAME"] = \
        lambda: noconversion("GIGAMETER", "REFERENCEFRAME")
    CONVERSIONS["GIGAMETER:TERAMETER"] = \
        lambda value: (10 ** -3) * value
    CONVERSIONS["GIGAMETER:THOU"] = \
        lambda: noconversion("GIGAMETER", "THOU")
    CONVERSIONS["GIGAMETER:YD"] = \
        lambda: noconversion("GIGAMETER", "YD")
    CONVERSIONS["GIGAMETER:YOCTOMETER"] = \
        lambda value: (10 ** 33) * value
    CONVERSIONS["GIGAMETER:YOTTAMETER"] = \
        lambda value: (10 ** -15) * value
    CONVERSIONS["GIGAMETER:ZEPTOMETER"] = \
        lambda value: (10 ** 30) * value
    CONVERSIONS["GIGAMETER:ZETTAMETER"] = \
        lambda value: (10 ** -12) * value
    CONVERSIONS["HECTOMETER:ANGSTROM"] = \
        lambda value: (10 ** 12) * value
    CONVERSIONS["HECTOMETER:ASTRONOMICALUNIT"] = \
        lambda: noconversion("HECTOMETER", "ASTRONOMICALUNIT")
    CONVERSIONS["HECTOMETER:ATTOMETER"] = \
        lambda value: (10 ** 20) * value
    CONVERSIONS["HECTOMETER:CENTIMETER"] = \
        lambda value: (10 ** 4) * value
    CONVERSIONS["HECTOMETER:DECAMETER"] = \
        lambda value: 10 * value
    CONVERSIONS["HECTOMETER:DECIMETER"] = \
        lambda value: (10 ** 3) * value
    CONVERSIONS["HECTOMETER:EXAMETER"] = \
        lambda value: (10 ** -16) * value
    CONVERSIONS["HECTOMETER:FEMTOMETER"] = \
        lambda value: (10 ** 17) * value
    CONVERSIONS["HECTOMETER:FT"] = \
        lambda: noconversion("HECTOMETER", "FT")
    CONVERSIONS["HECTOMETER:GIGAMETER"] = \
        lambda value: (10 ** -7) * value
    CONVERSIONS["HECTOMETER:IN"] = \
        lambda: noconversion("HECTOMETER", "IN")
    CONVERSIONS["HECTOMETER:KILOMETER"] = \
        lambda value: (10 ** -1) * value
    CONVERSIONS["HECTOMETER:LI"] = \
        lambda: noconversion("HECTOMETER", "LI")
    CONVERSIONS["HECTOMETER:LIGHTYEAR"] = \
        lambda: noconversion("HECTOMETER", "LIGHTYEAR")
    CONVERSIONS["HECTOMETER:MEGAMETER"] = \
        lambda value: (10 ** -4) * value
    CONVERSIONS["HECTOMETER:METER"] = \
        lambda value: (10 ** 2) * value
    CONVERSIONS["HECTOMETER:MI"] = \
        lambda: noconversion("HECTOMETER", "MI")
    CONVERSIONS["HECTOMETER:MICROMETER"] = \
        lambda value: (10 ** 8) * value
    CONVERSIONS["HECTOMETER:MILLIMETER"] = \
        lambda value: (10 ** 5) * value
    CONVERSIONS["HECTOMETER:NANOMETER"] = \
        lambda value: (10 ** 11) * value
    CONVERSIONS["HECTOMETER:PARSEC"] = \
        lambda: noconversion("HECTOMETER", "PARSEC")
    CONVERSIONS["HECTOMETER:PETAMETER"] = \
        lambda value: (10 ** -13) * value
    CONVERSIONS["HECTOMETER:PICOMETER"] = \
        lambda value: (10 ** 14) * value
    CONVERSIONS["HECTOMETER:PIXEL"] = \
        lambda: noconversion("HECTOMETER", "PIXEL")
    CONVERSIONS["HECTOMETER:PT"] = \
        lambda: noconversion("HECTOMETER", "PT")
    CONVERSIONS["HECTOMETER:REFERENCEFRAME"] = \
        lambda: noconversion("HECTOMETER", "REFERENCEFRAME")
    CONVERSIONS["HECTOMETER:TERAMETER"] = \
        lambda value: (10 ** -10) * value
    CONVERSIONS["HECTOMETER:THOU"] = \
        lambda: noconversion("HECTOMETER", "THOU")
    CONVERSIONS["HECTOMETER:YD"] = \
        lambda: noconversion("HECTOMETER", "YD")
    CONVERSIONS["HECTOMETER:YOCTOMETER"] = \
        lambda value: (10 ** 26) * value
    CONVERSIONS["HECTOMETER:YOTTAMETER"] = \
        lambda value: (10 ** -22) * value
    CONVERSIONS["HECTOMETER:ZEPTOMETER"] = \
        lambda value: (10 ** 23) * value
    CONVERSIONS["HECTOMETER:ZETTAMETER"] = \
        lambda value: (10 ** -19) * value
    CONVERSIONS["IN:ANGSTROM"] = \
        lambda: noconversion("IN", "ANGSTROM")
    CONVERSIONS["IN:ASTRONOMICALUNIT"] = \
        lambda: noconversion("IN", "ASTRONOMICALUNIT")
    CONVERSIONS["IN:ATTOMETER"] = \
        lambda: noconversion("IN", "ATTOMETER")
    CONVERSIONS["IN:CENTIMETER"] = \
        lambda: noconversion("IN", "CENTIMETER")
    CONVERSIONS["IN:DECAMETER"] = \
        lambda: noconversion("IN", "DECAMETER")
    CONVERSIONS["IN:DECIMETER"] = \
        lambda: noconversion("IN", "DECIMETER")
    CONVERSIONS["IN:EXAMETER"] = \
        lambda: noconversion("IN", "EXAMETER")
    CONVERSIONS["IN:FEMTOMETER"] = \
        lambda: noconversion("IN", "FEMTOMETER")
    CONVERSIONS["IN:FT"] = \
        lambda: noconversion("IN", "FT")
    CONVERSIONS["IN:GIGAMETER"] = \
        lambda: noconversion("IN", "GIGAMETER")
    CONVERSIONS["IN:HECTOMETER"] = \
        lambda: noconversion("IN", "HECTOMETER")
    CONVERSIONS["IN:KILOMETER"] = \
        lambda: noconversion("IN", "KILOMETER")
    CONVERSIONS["IN:LI"] = \
        lambda: noconversion("IN", "LI")
    CONVERSIONS["IN:LIGHTYEAR"] = \
        lambda: noconversion("IN", "LIGHTYEAR")
    CONVERSIONS["IN:MEGAMETER"] = \
        lambda: noconversion("IN", "MEGAMETER")
    CONVERSIONS["IN:METER"] = \
        lambda: noconversion("IN", "METER")
    CONVERSIONS["IN:MI"] = \
        lambda: noconversion("IN", "MI")
    CONVERSIONS["IN:MICROMETER"] = \
        lambda: noconversion("IN", "MICROMETER")
    CONVERSIONS["IN:MILLIMETER"] = \
        lambda: noconversion("IN", "MILLIMETER")
    CONVERSIONS["IN:NANOMETER"] = \
        lambda: noconversion("IN", "NANOMETER")
    CONVERSIONS["IN:PARSEC"] = \
        lambda: noconversion("IN", "PARSEC")
    CONVERSIONS["IN:PETAMETER"] = \
        lambda: noconversion("IN", "PETAMETER")
    CONVERSIONS["IN:PICOMETER"] = \
        lambda: noconversion("IN", "PICOMETER")
    CONVERSIONS["IN:PIXEL"] = \
        lambda: noconversion("IN", "PIXEL")
    CONVERSIONS["IN:PT"] = \
        lambda: noconversion("IN", "PT")
    CONVERSIONS["IN:REFERENCEFRAME"] = \
        lambda: noconversion("IN", "REFERENCEFRAME")
    CONVERSIONS["IN:TERAMETER"] = \
        lambda: noconversion("IN", "TERAMETER")
    CONVERSIONS["IN:THOU"] = \
        lambda: noconversion("IN", "THOU")
    CONVERSIONS["IN:YD"] = \
        lambda: noconversion("IN", "YD")
    CONVERSIONS["IN:YOCTOMETER"] = \
        lambda: noconversion("IN", "YOCTOMETER")
    CONVERSIONS["IN:YOTTAMETER"] = \
        lambda: noconversion("IN", "YOTTAMETER")
    CONVERSIONS["IN:ZEPTOMETER"] = \
        lambda: noconversion("IN", "ZEPTOMETER")
    CONVERSIONS["IN:ZETTAMETER"] = \
        lambda: noconversion("IN", "ZETTAMETER")
    CONVERSIONS["KILOMETER:ANGSTROM"] = \
        lambda value: (10 ** 13) * value
    CONVERSIONS["KILOMETER:ASTRONOMICALUNIT"] = \
        lambda: noconversion("KILOMETER", "ASTRONOMICALUNIT")
    CONVERSIONS["KILOMETER:ATTOMETER"] = \
        lambda value: (10 ** 21) * value
    CONVERSIONS["KILOMETER:CENTIMETER"] = \
        lambda value: (10 ** 5) * value
    CONVERSIONS["KILOMETER:DECAMETER"] = \
        lambda value: (10 ** 2) * value
    CONVERSIONS["KILOMETER:DECIMETER"] = \
        lambda value: (10 ** 4) * value
    CONVERSIONS["KILOMETER:EXAMETER"] = \
        lambda value: (10 ** -15) * value
    CONVERSIONS["KILOMETER:FEMTOMETER"] = \
        lambda value: (10 ** 18) * value
    CONVERSIONS["KILOMETER:FT"] = \
        lambda: noconversion("KILOMETER", "FT")
    CONVERSIONS["KILOMETER:GIGAMETER"] = \
        lambda value: (10 ** -6) * value
    CONVERSIONS["KILOMETER:HECTOMETER"] = \
        lambda value: 10 * value
    CONVERSIONS["KILOMETER:IN"] = \
        lambda: noconversion("KILOMETER", "IN")
    CONVERSIONS["KILOMETER:LI"] = \
        lambda: noconversion("KILOMETER", "LI")
    CONVERSIONS["KILOMETER:LIGHTYEAR"] = \
        lambda: noconversion("KILOMETER", "LIGHTYEAR")
    CONVERSIONS["KILOMETER:MEGAMETER"] = \
        lambda value: (10 ** -3) * value
    CONVERSIONS["KILOMETER:METER"] = \
        lambda value: (10 ** 3) * value
    CONVERSIONS["KILOMETER:MI"] = \
        lambda: noconversion("KILOMETER", "MI")
    CONVERSIONS["KILOMETER:MICROMETER"] = \
        lambda value: (10 ** 9) * value
    CONVERSIONS["KILOMETER:MILLIMETER"] = \
        lambda value: (10 ** 6) * value
    CONVERSIONS["KILOMETER:NANOMETER"] = \
        lambda value: (10 ** 12) * value
    CONVERSIONS["KILOMETER:PARSEC"] = \
        lambda: noconversion("KILOMETER", "PARSEC")
    CONVERSIONS["KILOMETER:PETAMETER"] = \
        lambda value: (10 ** -12) * value
    CONVERSIONS["KILOMETER:PICOMETER"] = \
        lambda value: (10 ** 15) * value
    CONVERSIONS["KILOMETER:PIXEL"] = \
        lambda: noconversion("KILOMETER", "PIXEL")
    CONVERSIONS["KILOMETER:PT"] = \
        lambda: noconversion("KILOMETER", "PT")
    CONVERSIONS["KILOMETER:REFERENCEFRAME"] = \
        lambda: noconversion("KILOMETER", "REFERENCEFRAME")
    CONVERSIONS["KILOMETER:TERAMETER"] = \
        lambda value: (10 ** -9) * value
    CONVERSIONS["KILOMETER:THOU"] = \
        lambda: noconversion("KILOMETER", "THOU")
    CONVERSIONS["KILOMETER:YD"] = \
        lambda: noconversion("KILOMETER", "YD")
    CONVERSIONS["KILOMETER:YOCTOMETER"] = \
        lambda value: (10 ** 27) * value
    CONVERSIONS["KILOMETER:YOTTAMETER"] = \
        lambda value: (10 ** -21) * value
    CONVERSIONS["KILOMETER:ZEPTOMETER"] = \
        lambda value: (10 ** 24) * value
    CONVERSIONS["KILOMETER:ZETTAMETER"] = \
        lambda value: (10 ** -18) * value
    CONVERSIONS["LI:ANGSTROM"] = \
        lambda: noconversion("LI", "ANGSTROM")
    CONVERSIONS["LI:ASTRONOMICALUNIT"] = \
        lambda: noconversion("LI", "ASTRONOMICALUNIT")
    CONVERSIONS["LI:ATTOMETER"] = \
        lambda: noconversion("LI", "ATTOMETER")
    CONVERSIONS["LI:CENTIMETER"] = \
        lambda: noconversion("LI", "CENTIMETER")
    CONVERSIONS["LI:DECAMETER"] = \
        lambda: noconversion("LI", "DECAMETER")
    CONVERSIONS["LI:DECIMETER"] = \
        lambda: noconversion("LI", "DECIMETER")
    CONVERSIONS["LI:EXAMETER"] = \
        lambda: noconversion("LI", "EXAMETER")
    CONVERSIONS["LI:FEMTOMETER"] = \
        lambda: noconversion("LI", "FEMTOMETER")
    CONVERSIONS["LI:FT"] = \
        lambda: noconversion("LI", "FT")
    CONVERSIONS["LI:GIGAMETER"] = \
        lambda: noconversion("LI", "GIGAMETER")
    CONVERSIONS["LI:HECTOMETER"] = \
        lambda: noconversion("LI", "HECTOMETER")
    CONVERSIONS["LI:IN"] = \
        lambda: noconversion("LI", "IN")
    CONVERSIONS["LI:KILOMETER"] = \
        lambda: noconversion("LI", "KILOMETER")
    CONVERSIONS["LI:LIGHTYEAR"] = \
        lambda: noconversion("LI", "LIGHTYEAR")
    CONVERSIONS["LI:MEGAMETER"] = \
        lambda: noconversion("LI", "MEGAMETER")
    CONVERSIONS["LI:METER"] = \
        lambda: noconversion("LI", "METER")
    CONVERSIONS["LI:MI"] = \
        lambda: noconversion("LI", "MI")
    CONVERSIONS["LI:MICROMETER"] = \
        lambda: noconversion("LI", "MICROMETER")
    CONVERSIONS["LI:MILLIMETER"] = \
        lambda: noconversion("LI", "MILLIMETER")
    CONVERSIONS["LI:NANOMETER"] = \
        lambda: noconversion("LI", "NANOMETER")
    CONVERSIONS["LI:PARSEC"] = \
        lambda: noconversion("LI", "PARSEC")
    CONVERSIONS["LI:PETAMETER"] = \
        lambda: noconversion("LI", "PETAMETER")
    CONVERSIONS["LI:PICOMETER"] = \
        lambda: noconversion("LI", "PICOMETER")
    CONVERSIONS["LI:PIXEL"] = \
        lambda: noconversion("LI", "PIXEL")
    CONVERSIONS["LI:PT"] = \
        lambda: noconversion("LI", "PT")
    CONVERSIONS["LI:REFERENCEFRAME"] = \
        lambda: noconversion("LI", "REFERENCEFRAME")
    CONVERSIONS["LI:TERAMETER"] = \
        lambda: noconversion("LI", "TERAMETER")
    CONVERSIONS["LI:THOU"] = \
        lambda: noconversion("LI", "THOU")
    CONVERSIONS["LI:YD"] = \
        lambda: noconversion("LI", "YD")
    CONVERSIONS["LI:YOCTOMETER"] = \
        lambda: noconversion("LI", "YOCTOMETER")
    CONVERSIONS["LI:YOTTAMETER"] = \
        lambda: noconversion("LI", "YOTTAMETER")
    CONVERSIONS["LI:ZEPTOMETER"] = \
        lambda: noconversion("LI", "ZEPTOMETER")
    CONVERSIONS["LI:ZETTAMETER"] = \
        lambda: noconversion("LI", "ZETTAMETER")
    CONVERSIONS["LIGHTYEAR:ANGSTROM"] = \
        lambda: noconversion("LIGHTYEAR", "ANGSTROM")
    CONVERSIONS["LIGHTYEAR:ASTRONOMICALUNIT"] = \
        lambda: noconversion("LIGHTYEAR", "ASTRONOMICALUNIT")
    CONVERSIONS["LIGHTYEAR:ATTOMETER"] = \
        lambda: noconversion("LIGHTYEAR", "ATTOMETER")
    CONVERSIONS["LIGHTYEAR:CENTIMETER"] = \
        lambda: noconversion("LIGHTYEAR", "CENTIMETER")
    CONVERSIONS["LIGHTYEAR:DECAMETER"] = \
        lambda: noconversion("LIGHTYEAR", "DECAMETER")
    CONVERSIONS["LIGHTYEAR:DECIMETER"] = \
        lambda: noconversion("LIGHTYEAR", "DECIMETER")
    CONVERSIONS["LIGHTYEAR:EXAMETER"] = \
        lambda: noconversion("LIGHTYEAR", "EXAMETER")
    CONVERSIONS["LIGHTYEAR:FEMTOMETER"] = \
        lambda: noconversion("LIGHTYEAR", "FEMTOMETER")
    CONVERSIONS["LIGHTYEAR:FT"] = \
        lambda: noconversion("LIGHTYEAR", "FT")
    CONVERSIONS["LIGHTYEAR:GIGAMETER"] = \
        lambda: noconversion("LIGHTYEAR", "GIGAMETER")
    CONVERSIONS["LIGHTYEAR:HECTOMETER"] = \
        lambda: noconversion("LIGHTYEAR", "HECTOMETER")
    CONVERSIONS["LIGHTYEAR:IN"] = \
        lambda: noconversion("LIGHTYEAR", "IN")
    CONVERSIONS["LIGHTYEAR:KILOMETER"] = \
        lambda: noconversion("LIGHTYEAR", "KILOMETER")
    CONVERSIONS["LIGHTYEAR:LI"] = \
        lambda: noconversion("LIGHTYEAR", "LI")
    CONVERSIONS["LIGHTYEAR:MEGAMETER"] = \
        lambda: noconversion("LIGHTYEAR", "MEGAMETER")
    CONVERSIONS["LIGHTYEAR:METER"] = \
        lambda: noconversion("LIGHTYEAR", "METER")
    CONVERSIONS["LIGHTYEAR:MI"] = \
        lambda: noconversion("LIGHTYEAR", "MI")
    CONVERSIONS["LIGHTYEAR:MICROMETER"] = \
        lambda: noconversion("LIGHTYEAR", "MICROMETER")
    CONVERSIONS["LIGHTYEAR:MILLIMETER"] = \
        lambda: noconversion("LIGHTYEAR", "MILLIMETER")
    CONVERSIONS["LIGHTYEAR:NANOMETER"] = \
        lambda: noconversion("LIGHTYEAR", "NANOMETER")
    CONVERSIONS["LIGHTYEAR:PARSEC"] = \
        lambda: noconversion("LIGHTYEAR", "PARSEC")
    CONVERSIONS["LIGHTYEAR:PETAMETER"] = \
        lambda: noconversion("LIGHTYEAR", "PETAMETER")
    CONVERSIONS["LIGHTYEAR:PICOMETER"] = \
        lambda: noconversion("LIGHTYEAR", "PICOMETER")
    CONVERSIONS["LIGHTYEAR:PIXEL"] = \
        lambda: noconversion("LIGHTYEAR", "PIXEL")
    CONVERSIONS["LIGHTYEAR:PT"] = \
        lambda: noconversion("LIGHTYEAR", "PT")
    CONVERSIONS["LIGHTYEAR:REFERENCEFRAME"] = \
        lambda: noconversion("LIGHTYEAR", "REFERENCEFRAME")
    CONVERSIONS["LIGHTYEAR:TERAMETER"] = \
        lambda: noconversion("LIGHTYEAR", "TERAMETER")
    CONVERSIONS["LIGHTYEAR:THOU"] = \
        lambda: noconversion("LIGHTYEAR", "THOU")
    CONVERSIONS["LIGHTYEAR:YD"] = \
        lambda: noconversion("LIGHTYEAR", "YD")
    CONVERSIONS["LIGHTYEAR:YOCTOMETER"] = \
        lambda: noconversion("LIGHTYEAR", "YOCTOMETER")
    CONVERSIONS["LIGHTYEAR:YOTTAMETER"] = \
        lambda: noconversion("LIGHTYEAR", "YOTTAMETER")
    CONVERSIONS["LIGHTYEAR:ZEPTOMETER"] = \
        lambda: noconversion("LIGHTYEAR", "ZEPTOMETER")
    CONVERSIONS["LIGHTYEAR:ZETTAMETER"] = \
        lambda: noconversion("LIGHTYEAR", "ZETTAMETER")
    CONVERSIONS["MEGAMETER:ANGSTROM"] = \
        lambda value: (10 ** 16) * value
    CONVERSIONS["MEGAMETER:ASTRONOMICALUNIT"] = \
        lambda: noconversion("MEGAMETER", "ASTRONOMICALUNIT")
    CONVERSIONS["MEGAMETER:ATTOMETER"] = \
        lambda value: (10 ** 24) * value
    CONVERSIONS["MEGAMETER:CENTIMETER"] = \
        lambda value: (10 ** 8) * value
    CONVERSIONS["MEGAMETER:DECAMETER"] = \
        lambda value: (10 ** 5) * value
    CONVERSIONS["MEGAMETER:DECIMETER"] = \
        lambda value: (10 ** 7) * value
    CONVERSIONS["MEGAMETER:EXAMETER"] = \
        lambda value: (10 ** -12) * value
    CONVERSIONS["MEGAMETER:FEMTOMETER"] = \
        lambda value: (10 ** 21) * value
    CONVERSIONS["MEGAMETER:FT"] = \
        lambda: noconversion("MEGAMETER", "FT")
    CONVERSIONS["MEGAMETER:GIGAMETER"] = \
        lambda value: (10 ** -3) * value
    CONVERSIONS["MEGAMETER:HECTOMETER"] = \
        lambda value: (10 ** 4) * value
    CONVERSIONS["MEGAMETER:IN"] = \
        lambda: noconversion("MEGAMETER", "IN")
    CONVERSIONS["MEGAMETER:KILOMETER"] = \
        lambda value: (10 ** 3) * value
    CONVERSIONS["MEGAMETER:LI"] = \
        lambda: noconversion("MEGAMETER", "LI")
    CONVERSIONS["MEGAMETER:LIGHTYEAR"] = \
        lambda: noconversion("MEGAMETER", "LIGHTYEAR")
    CONVERSIONS["MEGAMETER:METER"] = \
        lambda value: (10 ** 6) * value
    CONVERSIONS["MEGAMETER:MI"] = \
        lambda: noconversion("MEGAMETER", "MI")
    CONVERSIONS["MEGAMETER:MICROMETER"] = \
        lambda value: (10 ** 12) * value
    CONVERSIONS["MEGAMETER:MILLIMETER"] = \
        lambda value: (10 ** 9) * value
    CONVERSIONS["MEGAMETER:NANOMETER"] = \
        lambda value: (10 ** 15) * value
    CONVERSIONS["MEGAMETER:PARSEC"] = \
        lambda: noconversion("MEGAMETER", "PARSEC")
    CONVERSIONS["MEGAMETER:PETAMETER"] = \
        lambda value: (10 ** -9) * value
    CONVERSIONS["MEGAMETER:PICOMETER"] = \
        lambda value: (10 ** 18) * value
    CONVERSIONS["MEGAMETER:PIXEL"] = \
        lambda: noconversion("MEGAMETER", "PIXEL")
    CONVERSIONS["MEGAMETER:PT"] = \
        lambda: noconversion("MEGAMETER", "PT")
    CONVERSIONS["MEGAMETER:REFERENCEFRAME"] = \
        lambda: noconversion("MEGAMETER", "REFERENCEFRAME")
    CONVERSIONS["MEGAMETER:TERAMETER"] = \
        lambda value: (10 ** -6) * value
    CONVERSIONS["MEGAMETER:THOU"] = \
        lambda: noconversion("MEGAMETER", "THOU")
    CONVERSIONS["MEGAMETER:YD"] = \
        lambda: noconversion("MEGAMETER", "YD")
    CONVERSIONS["MEGAMETER:YOCTOMETER"] = \
        lambda value: (10 ** 30) * value
    CONVERSIONS["MEGAMETER:YOTTAMETER"] = \
        lambda value: (10 ** -18) * value
    CONVERSIONS["MEGAMETER:ZEPTOMETER"] = \
        lambda value: (10 ** 27) * value
    CONVERSIONS["MEGAMETER:ZETTAMETER"] = \
        lambda value: (10 ** -15) * value
    CONVERSIONS["METER:ANGSTROM"] = \
        lambda value: (10 ** 10) * value
    CONVERSIONS["METER:ASTRONOMICALUNIT"] = \
        lambda: noconversion("METER", "ASTRONOMICALUNIT")
    CONVERSIONS["METER:ATTOMETER"] = \
        lambda value: (10 ** 18) * value
    CONVERSIONS["METER:CENTIMETER"] = \
        lambda value: (10 ** 2) * value
    CONVERSIONS["METER:DECAMETER"] = \
        lambda value: (10 ** -1) * value
    CONVERSIONS["METER:DECIMETER"] = \
        lambda value: 10 * value
    CONVERSIONS["METER:EXAMETER"] = \
        lambda value: (10 ** -18) * value
    CONVERSIONS["METER:FEMTOMETER"] = \
        lambda value: (10 ** 15) * value
    CONVERSIONS["METER:FT"] = \
        lambda: noconversion("METER", "FT")
    CONVERSIONS["METER:GIGAMETER"] = \
        lambda value: (10 ** -9) * value
    CONVERSIONS["METER:HECTOMETER"] = \
        lambda value: (10 ** -2) * value
    CONVERSIONS["METER:IN"] = \
        lambda: noconversion("METER", "IN")
    CONVERSIONS["METER:KILOMETER"] = \
        lambda value: (10 ** -3) * value
    CONVERSIONS["METER:LI"] = \
        lambda: noconversion("METER", "LI")
    CONVERSIONS["METER:LIGHTYEAR"] = \
        lambda: noconversion("METER", "LIGHTYEAR")
    CONVERSIONS["METER:MEGAMETER"] = \
        lambda value: (10 ** -6) * value
    CONVERSIONS["METER:MI"] = \
        lambda: noconversion("METER", "MI")
    CONVERSIONS["METER:MICROMETER"] = \
        lambda value: (10 ** 6) * value
    CONVERSIONS["METER:MILLIMETER"] = \
        lambda value: (10 ** 3) * value
    CONVERSIONS["METER:NANOMETER"] = \
        lambda value: (10 ** 9) * value
    CONVERSIONS["METER:PARSEC"] = \
        lambda: noconversion("METER", "PARSEC")
    CONVERSIONS["METER:PETAMETER"] = \
        lambda value: (10 ** -15) * value
    CONVERSIONS["METER:PICOMETER"] = \
        lambda value: (10 ** 12) * value
    CONVERSIONS["METER:PIXEL"] = \
        lambda: noconversion("METER", "PIXEL")
    CONVERSIONS["METER:PT"] = \
        lambda: noconversion("METER", "PT")
    CONVERSIONS["METER:REFERENCEFRAME"] = \
        lambda: noconversion("METER", "REFERENCEFRAME")
    CONVERSIONS["METER:TERAMETER"] = \
        lambda value: (10 ** -12) * value
    CONVERSIONS["METER:THOU"] = \
        lambda: noconversion("METER", "THOU")
    CONVERSIONS["METER:YD"] = \
        lambda: noconversion("METER", "YD")
    CONVERSIONS["METER:YOCTOMETER"] = \
        lambda value: (10 ** 24) * value
    CONVERSIONS["METER:YOTTAMETER"] = \
        lambda value: (10 ** -24) * value
    CONVERSIONS["METER:ZEPTOMETER"] = \
        lambda value: (10 ** 21) * value
    CONVERSIONS["METER:ZETTAMETER"] = \
        lambda value: (10 ** -21) * value
    CONVERSIONS["MI:ANGSTROM"] = \
        lambda: noconversion("MI", "ANGSTROM")
    CONVERSIONS["MI:ASTRONOMICALUNIT"] = \
        lambda: noconversion("MI", "ASTRONOMICALUNIT")
    CONVERSIONS["MI:ATTOMETER"] = \
        lambda: noconversion("MI", "ATTOMETER")
    CONVERSIONS["MI:CENTIMETER"] = \
        lambda: noconversion("MI", "CENTIMETER")
    CONVERSIONS["MI:DECAMETER"] = \
        lambda: noconversion("MI", "DECAMETER")
    CONVERSIONS["MI:DECIMETER"] = \
        lambda: noconversion("MI", "DECIMETER")
    CONVERSIONS["MI:EXAMETER"] = \
        lambda: noconversion("MI", "EXAMETER")
    CONVERSIONS["MI:FEMTOMETER"] = \
        lambda: noconversion("MI", "FEMTOMETER")
    CONVERSIONS["MI:FT"] = \
        lambda: noconversion("MI", "FT")
    CONVERSIONS["MI:GIGAMETER"] = \
        lambda: noconversion("MI", "GIGAMETER")
    CONVERSIONS["MI:HECTOMETER"] = \
        lambda: noconversion("MI", "HECTOMETER")
    CONVERSIONS["MI:IN"] = \
        lambda: noconversion("MI", "IN")
    CONVERSIONS["MI:KILOMETER"] = \
        lambda: noconversion("MI", "KILOMETER")
    CONVERSIONS["MI:LI"] = \
        lambda: noconversion("MI", "LI")
    CONVERSIONS["MI:LIGHTYEAR"] = \
        lambda: noconversion("MI", "LIGHTYEAR")
    CONVERSIONS["MI:MEGAMETER"] = \
        lambda: noconversion("MI", "MEGAMETER")
    CONVERSIONS["MI:METER"] = \
        lambda: noconversion("MI", "METER")
    CONVERSIONS["MI:MICROMETER"] = \
        lambda: noconversion("MI", "MICROMETER")
    CONVERSIONS["MI:MILLIMETER"] = \
        lambda: noconversion("MI", "MILLIMETER")
    CONVERSIONS["MI:NANOMETER"] = \
        lambda: noconversion("MI", "NANOMETER")
    CONVERSIONS["MI:PARSEC"] = \
        lambda: noconversion("MI", "PARSEC")
    CONVERSIONS["MI:PETAMETER"] = \
        lambda: noconversion("MI", "PETAMETER")
    CONVERSIONS["MI:PICOMETER"] = \
        lambda: noconversion("MI", "PICOMETER")
    CONVERSIONS["MI:PIXEL"] = \
        lambda: noconversion("MI", "PIXEL")
    CONVERSIONS["MI:PT"] = \
        lambda: noconversion("MI", "PT")
    CONVERSIONS["MI:REFERENCEFRAME"] = \
        lambda: noconversion("MI", "REFERENCEFRAME")
    CONVERSIONS["MI:TERAMETER"] = \
        lambda: noconversion("MI", "TERAMETER")
    CONVERSIONS["MI:THOU"] = \
        lambda: noconversion("MI", "THOU")
    CONVERSIONS["MI:YD"] = \
        lambda: noconversion("MI", "YD")
    CONVERSIONS["MI:YOCTOMETER"] = \
        lambda: noconversion("MI", "YOCTOMETER")
    CONVERSIONS["MI:YOTTAMETER"] = \
        lambda: noconversion("MI", "YOTTAMETER")
    CONVERSIONS["MI:ZEPTOMETER"] = \
        lambda: noconversion("MI", "ZEPTOMETER")
    CONVERSIONS["MI:ZETTAMETER"] = \
        lambda: noconversion("MI", "ZETTAMETER")
    CONVERSIONS["MICROMETER:ANGSTROM"] = \
        lambda value: (10 ** 4) * value
    CONVERSIONS["MICROMETER:ASTRONOMICALUNIT"] = \
        lambda: noconversion("MICROMETER", "ASTRONOMICALUNIT")
    CONVERSIONS["MICROMETER:ATTOMETER"] = \
        lambda value: (10 ** 12) * value
    CONVERSIONS["MICROMETER:CENTIMETER"] = \
        lambda value: (10 ** -4) * value
    CONVERSIONS["MICROMETER:DECAMETER"] = \
        lambda value: (10 ** -7) * value
    CONVERSIONS["MICROMETER:DECIMETER"] = \
        lambda value: (10 ** -5) * value
    CONVERSIONS["MICROMETER:EXAMETER"] = \
        lambda value: (10 ** -24) * value
    CONVERSIONS["MICROMETER:FEMTOMETER"] = \
        lambda value: (10 ** 9) * value
    CONVERSIONS["MICROMETER:FT"] = \
        lambda: noconversion("MICROMETER", "FT")
    CONVERSIONS["MICROMETER:GIGAMETER"] = \
        lambda value: (10 ** -15) * value
    CONVERSIONS["MICROMETER:HECTOMETER"] = \
        lambda value: (10 ** -8) * value
    CONVERSIONS["MICROMETER:IN"] = \
        lambda: noconversion("MICROMETER", "IN")
    CONVERSIONS["MICROMETER:KILOMETER"] = \
        lambda value: (10 ** -9) * value
    CONVERSIONS["MICROMETER:LI"] = \
        lambda: noconversion("MICROMETER", "LI")
    CONVERSIONS["MICROMETER:LIGHTYEAR"] = \
        lambda: noconversion("MICROMETER", "LIGHTYEAR")
    CONVERSIONS["MICROMETER:MEGAMETER"] = \
        lambda value: (10 ** -12) * value
    CONVERSIONS["MICROMETER:METER"] = \
        lambda value: (10 ** -6) * value
    CONVERSIONS["MICROMETER:MI"] = \
        lambda: noconversion("MICROMETER", "MI")
    CONVERSIONS["MICROMETER:MILLIMETER"] = \
        lambda value: (10 ** -3) * value
    CONVERSIONS["MICROMETER:NANOMETER"] = \
        lambda value: (10 ** 3) * value
    CONVERSIONS["MICROMETER:PARSEC"] = \
        lambda: noconversion("MICROMETER", "PARSEC")
    CONVERSIONS["MICROMETER:PETAMETER"] = \
        lambda value: (10 ** -21) * value
    CONVERSIONS["MICROMETER:PICOMETER"] = \
        lambda value: (10 ** 6) * value
    CONVERSIONS["MICROMETER:PIXEL"] = \
        lambda: noconversion("MICROMETER", "PIXEL")
    CONVERSIONS["MICROMETER:PT"] = \
        lambda: noconversion("MICROMETER", "PT")
    CONVERSIONS["MICROMETER:REFERENCEFRAME"] = \
        lambda: noconversion("MICROMETER", "REFERENCEFRAME")
    CONVERSIONS["MICROMETER:TERAMETER"] = \
        lambda value: (10 ** -18) * value
    CONVERSIONS["MICROMETER:THOU"] = \
        lambda: noconversion("MICROMETER", "THOU")
    CONVERSIONS["MICROMETER:YD"] = \
        lambda: noconversion("MICROMETER", "YD")
    CONVERSIONS["MICROMETER:YOCTOMETER"] = \
        lambda value: (10 ** 18) * value
    CONVERSIONS["MICROMETER:YOTTAMETER"] = \
        lambda value: (10 ** -30) * value
    CONVERSIONS["MICROMETER:ZEPTOMETER"] = \
        lambda value: (10 ** 15) * value
    CONVERSIONS["MICROMETER:ZETTAMETER"] = \
        lambda value: (10 ** -27) * value
    CONVERSIONS["MILLIMETER:ANGSTROM"] = \
        lambda value: (10 ** 7) * value
    CONVERSIONS["MILLIMETER:ASTRONOMICALUNIT"] = \
        lambda: noconversion("MILLIMETER", "ASTRONOMICALUNIT")
    CONVERSIONS["MILLIMETER:ATTOMETER"] = \
        lambda value: (10 ** 15) * value
    CONVERSIONS["MILLIMETER:CENTIMETER"] = \
        lambda value: (10 ** -1) * value
    CONVERSIONS["MILLIMETER:DECAMETER"] = \
        lambda value: (10 ** -4) * value
    CONVERSIONS["MILLIMETER:DECIMETER"] = \
        lambda value: (10 ** -2) * value
    CONVERSIONS["MILLIMETER:EXAMETER"] = \
        lambda value: (10 ** -21) * value
    CONVERSIONS["MILLIMETER:FEMTOMETER"] = \
        lambda value: (10 ** 12) * value
    CONVERSIONS["MILLIMETER:FT"] = \
        lambda: noconversion("MILLIMETER", "FT")
    CONVERSIONS["MILLIMETER:GIGAMETER"] = \
        lambda value: (10 ** -12) * value
    CONVERSIONS["MILLIMETER:HECTOMETER"] = \
        lambda value: (10 ** -5) * value
    CONVERSIONS["MILLIMETER:IN"] = \
        lambda: noconversion("MILLIMETER", "IN")
    CONVERSIONS["MILLIMETER:KILOMETER"] = \
        lambda value: (10 ** -6) * value
    CONVERSIONS["MILLIMETER:LI"] = \
        lambda: noconversion("MILLIMETER", "LI")
    CONVERSIONS["MILLIMETER:LIGHTYEAR"] = \
        lambda: noconversion("MILLIMETER", "LIGHTYEAR")
    CONVERSIONS["MILLIMETER:MEGAMETER"] = \
        lambda value: (10 ** -9) * value
    CONVERSIONS["MILLIMETER:METER"] = \
        lambda value: (10 ** -3) * value
    CONVERSIONS["MILLIMETER:MI"] = \
        lambda: noconversion("MILLIMETER", "MI")
    CONVERSIONS["MILLIMETER:MICROMETER"] = \
        lambda value: (10 ** 3) * value
    CONVERSIONS["MILLIMETER:NANOMETER"] = \
        lambda value: (10 ** 6) * value
    CONVERSIONS["MILLIMETER:PARSEC"] = \
        lambda: noconversion("MILLIMETER", "PARSEC")
    CONVERSIONS["MILLIMETER:PETAMETER"] = \
        lambda value: (10 ** -18) * value
    CONVERSIONS["MILLIMETER:PICOMETER"] = \
        lambda value: (10 ** 9) * value
    CONVERSIONS["MILLIMETER:PIXEL"] = \
        lambda: noconversion("MILLIMETER", "PIXEL")
    CONVERSIONS["MILLIMETER:PT"] = \
        lambda: noconversion("MILLIMETER", "PT")
    CONVERSIONS["MILLIMETER:REFERENCEFRAME"] = \
        lambda: noconversion("MILLIMETER", "REFERENCEFRAME")
    CONVERSIONS["MILLIMETER:TERAMETER"] = \
        lambda value: (10 ** -15) * value
    CONVERSIONS["MILLIMETER:THOU"] = \
        lambda: noconversion("MILLIMETER", "THOU")
    CONVERSIONS["MILLIMETER:YD"] = \
        lambda: noconversion("MILLIMETER", "YD")
    CONVERSIONS["MILLIMETER:YOCTOMETER"] = \
        lambda value: (10 ** 21) * value
    CONVERSIONS["MILLIMETER:YOTTAMETER"] = \
        lambda value: (10 ** -27) * value
    CONVERSIONS["MILLIMETER:ZEPTOMETER"] = \
        lambda value: (10 ** 18) * value
    CONVERSIONS["MILLIMETER:ZETTAMETER"] = \
        lambda value: (10 ** -24) * value
    CONVERSIONS["NANOMETER:ANGSTROM"] = \
        lambda value: 10 * value
    CONVERSIONS["NANOMETER:ASTRONOMICALUNIT"] = \
        lambda: noconversion("NANOMETER", "ASTRONOMICALUNIT")
    CONVERSIONS["NANOMETER:ATTOMETER"] = \
        lambda value: (10 ** 9) * value
    CONVERSIONS["NANOMETER:CENTIMETER"] = \
        lambda value: (10 ** -7) * value
    CONVERSIONS["NANOMETER:DECAMETER"] = \
        lambda value: (10 ** -10) * value
    CONVERSIONS["NANOMETER:DECIMETER"] = \
        lambda value: (10 ** -8) * value
    CONVERSIONS["NANOMETER:EXAMETER"] = \
        lambda value: (10 ** -27) * value
    CONVERSIONS["NANOMETER:FEMTOMETER"] = \
        lambda value: (10 ** 6) * value
    CONVERSIONS["NANOMETER:FT"] = \
        lambda: noconversion("NANOMETER", "FT")
    CONVERSIONS["NANOMETER:GIGAMETER"] = \
        lambda value: (10 ** -18) * value
    CONVERSIONS["NANOMETER:HECTOMETER"] = \
        lambda value: (10 ** -11) * value
    CONVERSIONS["NANOMETER:IN"] = \
        lambda: noconversion("NANOMETER", "IN")
    CONVERSIONS["NANOMETER:KILOMETER"] = \
        lambda value: (10 ** -12) * value
    CONVERSIONS["NANOMETER:LI"] = \
        lambda: noconversion("NANOMETER", "LI")
    CONVERSIONS["NANOMETER:LIGHTYEAR"] = \
        lambda: noconversion("NANOMETER", "LIGHTYEAR")
    CONVERSIONS["NANOMETER:MEGAMETER"] = \
        lambda value: (10 ** -15) * value
    CONVERSIONS["NANOMETER:METER"] = \
        lambda value: (10 ** -9) * value
    CONVERSIONS["NANOMETER:MI"] = \
        lambda: noconversion("NANOMETER", "MI")
    CONVERSIONS["NANOMETER:MICROMETER"] = \
        lambda value: (10 ** -3) * value
    CONVERSIONS["NANOMETER:MILLIMETER"] = \
        lambda value: (10 ** -6) * value
    CONVERSIONS["NANOMETER:PARSEC"] = \
        lambda: noconversion("NANOMETER", "PARSEC")
    CONVERSIONS["NANOMETER:PETAMETER"] = \
        lambda value: (10 ** -24) * value
    CONVERSIONS["NANOMETER:PICOMETER"] = \
        lambda value: (10 ** 3) * value
    CONVERSIONS["NANOMETER:PIXEL"] = \
        lambda: noconversion("NANOMETER", "PIXEL")
    CONVERSIONS["NANOMETER:PT"] = \
        lambda: noconversion("NANOMETER", "PT")
    CONVERSIONS["NANOMETER:REFERENCEFRAME"] = \
        lambda: noconversion("NANOMETER", "REFERENCEFRAME")
    CONVERSIONS["NANOMETER:TERAMETER"] = \
        lambda value: (10 ** -21) * value
    CONVERSIONS["NANOMETER:THOU"] = \
        lambda: noconversion("NANOMETER", "THOU")
    CONVERSIONS["NANOMETER:YD"] = \
        lambda: noconversion("NANOMETER", "YD")
    CONVERSIONS["NANOMETER:YOCTOMETER"] = \
        lambda value: (10 ** 15) * value
    CONVERSIONS["NANOMETER:YOTTAMETER"] = \
        lambda value: (10 ** -33) * value
    CONVERSIONS["NANOMETER:ZEPTOMETER"] = \
        lambda value: (10 ** 12) * value
    CONVERSIONS["NANOMETER:ZETTAMETER"] = \
        lambda value: (10 ** -30) * value
    CONVERSIONS["PARSEC:ANGSTROM"] = \
        lambda: noconversion("PARSEC", "ANGSTROM")
    CONVERSIONS["PARSEC:ASTRONOMICALUNIT"] = \
        lambda: noconversion("PARSEC", "ASTRONOMICALUNIT")
    CONVERSIONS["PARSEC:ATTOMETER"] = \
        lambda: noconversion("PARSEC", "ATTOMETER")
    CONVERSIONS["PARSEC:CENTIMETER"] = \
        lambda: noconversion("PARSEC", "CENTIMETER")
    CONVERSIONS["PARSEC:DECAMETER"] = \
        lambda: noconversion("PARSEC", "DECAMETER")
    CONVERSIONS["PARSEC:DECIMETER"] = \
        lambda: noconversion("PARSEC", "DECIMETER")
    CONVERSIONS["PARSEC:EXAMETER"] = \
        lambda: noconversion("PARSEC", "EXAMETER")
    CONVERSIONS["PARSEC:FEMTOMETER"] = \
        lambda: noconversion("PARSEC", "FEMTOMETER")
    CONVERSIONS["PARSEC:FT"] = \
        lambda: noconversion("PARSEC", "FT")
    CONVERSIONS["PARSEC:GIGAMETER"] = \
        lambda: noconversion("PARSEC", "GIGAMETER")
    CONVERSIONS["PARSEC:HECTOMETER"] = \
        lambda: noconversion("PARSEC", "HECTOMETER")
    CONVERSIONS["PARSEC:IN"] = \
        lambda: noconversion("PARSEC", "IN")
    CONVERSIONS["PARSEC:KILOMETER"] = \
        lambda: noconversion("PARSEC", "KILOMETER")
    CONVERSIONS["PARSEC:LI"] = \
        lambda: noconversion("PARSEC", "LI")
    CONVERSIONS["PARSEC:LIGHTYEAR"] = \
        lambda: noconversion("PARSEC", "LIGHTYEAR")
    CONVERSIONS["PARSEC:MEGAMETER"] = \
        lambda: noconversion("PARSEC", "MEGAMETER")
    CONVERSIONS["PARSEC:METER"] = \
        lambda: noconversion("PARSEC", "METER")
    CONVERSIONS["PARSEC:MI"] = \
        lambda: noconversion("PARSEC", "MI")
    CONVERSIONS["PARSEC:MICROMETER"] = \
        lambda: noconversion("PARSEC", "MICROMETER")
    CONVERSIONS["PARSEC:MILLIMETER"] = \
        lambda: noconversion("PARSEC", "MILLIMETER")
    CONVERSIONS["PARSEC:NANOMETER"] = \
        lambda: noconversion("PARSEC", "NANOMETER")
    CONVERSIONS["PARSEC:PETAMETER"] = \
        lambda: noconversion("PARSEC", "PETAMETER")
    CONVERSIONS["PARSEC:PICOMETER"] = \
        lambda: noconversion("PARSEC", "PICOMETER")
    CONVERSIONS["PARSEC:PIXEL"] = \
        lambda: noconversion("PARSEC", "PIXEL")
    CONVERSIONS["PARSEC:PT"] = \
        lambda: noconversion("PARSEC", "PT")
    CONVERSIONS["PARSEC:REFERENCEFRAME"] = \
        lambda: noconversion("PARSEC", "REFERENCEFRAME")
    CONVERSIONS["PARSEC:TERAMETER"] = \
        lambda: noconversion("PARSEC", "TERAMETER")
    CONVERSIONS["PARSEC:THOU"] = \
        lambda: noconversion("PARSEC", "THOU")
    CONVERSIONS["PARSEC:YD"] = \
        lambda: noconversion("PARSEC", "YD")
    CONVERSIONS["PARSEC:YOCTOMETER"] = \
        lambda: noconversion("PARSEC", "YOCTOMETER")
    CONVERSIONS["PARSEC:YOTTAMETER"] = \
        lambda: noconversion("PARSEC", "YOTTAMETER")
    CONVERSIONS["PARSEC:ZEPTOMETER"] = \
        lambda: noconversion("PARSEC", "ZEPTOMETER")
    CONVERSIONS["PARSEC:ZETTAMETER"] = \
        lambda: noconversion("PARSEC", "ZETTAMETER")
    CONVERSIONS["PETAMETER:ANGSTROM"] = \
        lambda value: (10 ** 25) * value
    CONVERSIONS["PETAMETER:ASTRONOMICALUNIT"] = \
        lambda: noconversion("PETAMETER", "ASTRONOMICALUNIT")
    CONVERSIONS["PETAMETER:ATTOMETER"] = \
        lambda value: (10 ** 33) * value
    CONVERSIONS["PETAMETER:CENTIMETER"] = \
        lambda value: (10 ** 17) * value
    CONVERSIONS["PETAMETER:DECAMETER"] = \
        lambda value: (10 ** 14) * value
    CONVERSIONS["PETAMETER:DECIMETER"] = \
        lambda value: (10 ** 16) * value
    CONVERSIONS["PETAMETER:EXAMETER"] = \
        lambda value: (10 ** -3) * value
    CONVERSIONS["PETAMETER:FEMTOMETER"] = \
        lambda value: (10 ** 30) * value
    CONVERSIONS["PETAMETER:FT"] = \
        lambda: noconversion("PETAMETER", "FT")
    CONVERSIONS["PETAMETER:GIGAMETER"] = \
        lambda value: (10 ** 6) * value
    CONVERSIONS["PETAMETER:HECTOMETER"] = \
        lambda value: (10 ** 13) * value
    CONVERSIONS["PETAMETER:IN"] = \
        lambda: noconversion("PETAMETER", "IN")
    CONVERSIONS["PETAMETER:KILOMETER"] = \
        lambda value: (10 ** 12) * value
    CONVERSIONS["PETAMETER:LI"] = \
        lambda: noconversion("PETAMETER", "LI")
    CONVERSIONS["PETAMETER:LIGHTYEAR"] = \
        lambda: noconversion("PETAMETER", "LIGHTYEAR")
    CONVERSIONS["PETAMETER:MEGAMETER"] = \
        lambda value: (10 ** 9) * value
    CONVERSIONS["PETAMETER:METER"] = \
        lambda value: (10 ** 15) * value
    CONVERSIONS["PETAMETER:MI"] = \
        lambda: noconversion("PETAMETER", "MI")
    CONVERSIONS["PETAMETER:MICROMETER"] = \
        lambda value: (10 ** 21) * value
    CONVERSIONS["PETAMETER:MILLIMETER"] = \
        lambda value: (10 ** 18) * value
    CONVERSIONS["PETAMETER:NANOMETER"] = \
        lambda value: (10 ** 24) * value
    CONVERSIONS["PETAMETER:PARSEC"] = \
        lambda: noconversion("PETAMETER", "PARSEC")
    CONVERSIONS["PETAMETER:PICOMETER"] = \
        lambda value: (10 ** 27) * value
    CONVERSIONS["PETAMETER:PIXEL"] = \
        lambda: noconversion("PETAMETER", "PIXEL")
    CONVERSIONS["PETAMETER:PT"] = \
        lambda: noconversion("PETAMETER", "PT")
    CONVERSIONS["PETAMETER:REFERENCEFRAME"] = \
        lambda: noconversion("PETAMETER", "REFERENCEFRAME")
    CONVERSIONS["PETAMETER:TERAMETER"] = \
        lambda value: (10 ** 3) * value
    CONVERSIONS["PETAMETER:THOU"] = \
        lambda: noconversion("PETAMETER", "THOU")
    CONVERSIONS["PETAMETER:YD"] = \
        lambda: noconversion("PETAMETER", "YD")
    CONVERSIONS["PETAMETER:YOCTOMETER"] = \
        lambda value: (10 ** 39) * value
    CONVERSIONS["PETAMETER:YOTTAMETER"] = \
        lambda value: (10 ** -9) * value
    CONVERSIONS["PETAMETER:ZEPTOMETER"] = \
        lambda value: (10 ** 36) * value
    CONVERSIONS["PETAMETER:ZETTAMETER"] = \
        lambda value: (10 ** -6) * value
    CONVERSIONS["PICOMETER:ANGSTROM"] = \
        lambda value: (10 ** -2) * value
    CONVERSIONS["PICOMETER:ASTRONOMICALUNIT"] = \
        lambda: noconversion("PICOMETER", "ASTRONOMICALUNIT")
    CONVERSIONS["PICOMETER:ATTOMETER"] = \
        lambda value: (10 ** 6) * value
    CONVERSIONS["PICOMETER:CENTIMETER"] = \
        lambda value: (10 ** -10) * value
    CONVERSIONS["PICOMETER:DECAMETER"] = \
        lambda value: (10 ** -13) * value
    CONVERSIONS["PICOMETER:DECIMETER"] = \
        lambda value: (10 ** -11) * value
    CONVERSIONS["PICOMETER:EXAMETER"] = \
        lambda value: (10 ** -30) * value
    CONVERSIONS["PICOMETER:FEMTOMETER"] = \
        lambda value: (10 ** 3) * value
    CONVERSIONS["PICOMETER:FT"] = \
        lambda: noconversion("PICOMETER", "FT")
    CONVERSIONS["PICOMETER:GIGAMETER"] = \
        lambda value: (10 ** -21) * value
    CONVERSIONS["PICOMETER:HECTOMETER"] = \
        lambda value: (10 ** -14) * value
    CONVERSIONS["PICOMETER:IN"] = \
        lambda: noconversion("PICOMETER", "IN")
    CONVERSIONS["PICOMETER:KILOMETER"] = \
        lambda value: (10 ** -15) * value
    CONVERSIONS["PICOMETER:LI"] = \
        lambda: noconversion("PICOMETER", "LI")
    CONVERSIONS["PICOMETER:LIGHTYEAR"] = \
        lambda: noconversion("PICOMETER", "LIGHTYEAR")
    CONVERSIONS["PICOMETER:MEGAMETER"] = \
        lambda value: (10 ** -18) * value
    CONVERSIONS["PICOMETER:METER"] = \
        lambda value: (10 ** -12) * value
    CONVERSIONS["PICOMETER:MI"] = \
        lambda: noconversion("PICOMETER", "MI")
    CONVERSIONS["PICOMETER:MICROMETER"] = \
        lambda value: (10 ** -6) * value
    CONVERSIONS["PICOMETER:MILLIMETER"] = \
        lambda value: (10 ** -9) * value
    CONVERSIONS["PICOMETER:NANOMETER"] = \
        lambda value: (10 ** -3) * value
    CONVERSIONS["PICOMETER:PARSEC"] = \
        lambda: noconversion("PICOMETER", "PARSEC")
    CONVERSIONS["PICOMETER:PETAMETER"] = \
        lambda value: (10 ** -27) * value
    CONVERSIONS["PICOMETER:PIXEL"] = \
        lambda: noconversion("PICOMETER", "PIXEL")
    CONVERSIONS["PICOMETER:PT"] = \
        lambda: noconversion("PICOMETER", "PT")
    CONVERSIONS["PICOMETER:REFERENCEFRAME"] = \
        lambda: noconversion("PICOMETER", "REFERENCEFRAME")
    CONVERSIONS["PICOMETER:TERAMETER"] = \
        lambda value: (10 ** -24) * value
    CONVERSIONS["PICOMETER:THOU"] = \
        lambda: noconversion("PICOMETER", "THOU")
    CONVERSIONS["PICOMETER:YD"] = \
        lambda: noconversion("PICOMETER", "YD")
    CONVERSIONS["PICOMETER:YOCTOMETER"] = \
        lambda value: (10 ** 12) * value
    CONVERSIONS["PICOMETER:YOTTAMETER"] = \
        lambda value: (10 ** -36) * value
    CONVERSIONS["PICOMETER:ZEPTOMETER"] = \
        lambda value: (10 ** 9) * value
    CONVERSIONS["PICOMETER:ZETTAMETER"] = \
        lambda value: (10 ** -33) * value
    CONVERSIONS["PIXEL:ANGSTROM"] = \
        lambda: noconversion("PIXEL", "ANGSTROM")
    CONVERSIONS["PIXEL:ASTRONOMICALUNIT"] = \
        lambda: noconversion("PIXEL", "ASTRONOMICALUNIT")
    CONVERSIONS["PIXEL:ATTOMETER"] = \
        lambda: noconversion("PIXEL", "ATTOMETER")
    CONVERSIONS["PIXEL:CENTIMETER"] = \
        lambda: noconversion("PIXEL", "CENTIMETER")
    CONVERSIONS["PIXEL:DECAMETER"] = \
        lambda: noconversion("PIXEL", "DECAMETER")
    CONVERSIONS["PIXEL:DECIMETER"] = \
        lambda: noconversion("PIXEL", "DECIMETER")
    CONVERSIONS["PIXEL:EXAMETER"] = \
        lambda: noconversion("PIXEL", "EXAMETER")
    CONVERSIONS["PIXEL:FEMTOMETER"] = \
        lambda: noconversion("PIXEL", "FEMTOMETER")
    CONVERSIONS["PIXEL:FT"] = \
        lambda: noconversion("PIXEL", "FT")
    CONVERSIONS["PIXEL:GIGAMETER"] = \
        lambda: noconversion("PIXEL", "GIGAMETER")
    CONVERSIONS["PIXEL:HECTOMETER"] = \
        lambda: noconversion("PIXEL", "HECTOMETER")
    CONVERSIONS["PIXEL:IN"] = \
        lambda: noconversion("PIXEL", "IN")
    CONVERSIONS["PIXEL:KILOMETER"] = \
        lambda: noconversion("PIXEL", "KILOMETER")
    CONVERSIONS["PIXEL:LI"] = \
        lambda: noconversion("PIXEL", "LI")
    CONVERSIONS["PIXEL:LIGHTYEAR"] = \
        lambda: noconversion("PIXEL", "LIGHTYEAR")
    CONVERSIONS["PIXEL:MEGAMETER"] = \
        lambda: noconversion("PIXEL", "MEGAMETER")
    CONVERSIONS["PIXEL:METER"] = \
        lambda: noconversion("PIXEL", "METER")
    CONVERSIONS["PIXEL:MI"] = \
        lambda: noconversion("PIXEL", "MI")
    CONVERSIONS["PIXEL:MICROMETER"] = \
        lambda: noconversion("PIXEL", "MICROMETER")
    CONVERSIONS["PIXEL:MILLIMETER"] = \
        lambda: noconversion("PIXEL", "MILLIMETER")
    CONVERSIONS["PIXEL:NANOMETER"] = \
        lambda: noconversion("PIXEL", "NANOMETER")
    CONVERSIONS["PIXEL:PARSEC"] = \
        lambda: noconversion("PIXEL", "PARSEC")
    CONVERSIONS["PIXEL:PETAMETER"] = \
        lambda: noconversion("PIXEL", "PETAMETER")
    CONVERSIONS["PIXEL:PICOMETER"] = \
        lambda: noconversion("PIXEL", "PICOMETER")
    CONVERSIONS["PIXEL:PT"] = \
        lambda: noconversion("PIXEL", "PT")
    CONVERSIONS["PIXEL:REFERENCEFRAME"] = \
        lambda: noconversion("PIXEL", "REFERENCEFRAME")
    CONVERSIONS["PIXEL:TERAMETER"] = \
        lambda: noconversion("PIXEL", "TERAMETER")
    CONVERSIONS["PIXEL:THOU"] = \
        lambda: noconversion("PIXEL", "THOU")
    CONVERSIONS["PIXEL:YD"] = \
        lambda: noconversion("PIXEL", "YD")
    CONVERSIONS["PIXEL:YOCTOMETER"] = \
        lambda: noconversion("PIXEL", "YOCTOMETER")
    CONVERSIONS["PIXEL:YOTTAMETER"] = \
        lambda: noconversion("PIXEL", "YOTTAMETER")
    CONVERSIONS["PIXEL:ZEPTOMETER"] = \
        lambda: noconversion("PIXEL", "ZEPTOMETER")
    CONVERSIONS["PIXEL:ZETTAMETER"] = \
        lambda: noconversion("PIXEL", "ZETTAMETER")
    CONVERSIONS["PT:ANGSTROM"] = \
        lambda: noconversion("PT", "ANGSTROM")
    CONVERSIONS["PT:ASTRONOMICALUNIT"] = \
        lambda: noconversion("PT", "ASTRONOMICALUNIT")
    CONVERSIONS["PT:ATTOMETER"] = \
        lambda: noconversion("PT", "ATTOMETER")
    CONVERSIONS["PT:CENTIMETER"] = \
        lambda: noconversion("PT", "CENTIMETER")
    CONVERSIONS["PT:DECAMETER"] = \
        lambda: noconversion("PT", "DECAMETER")
    CONVERSIONS["PT:DECIMETER"] = \
        lambda: noconversion("PT", "DECIMETER")
    CONVERSIONS["PT:EXAMETER"] = \
        lambda: noconversion("PT", "EXAMETER")
    CONVERSIONS["PT:FEMTOMETER"] = \
        lambda: noconversion("PT", "FEMTOMETER")
    CONVERSIONS["PT:FT"] = \
        lambda: noconversion("PT", "FT")
    CONVERSIONS["PT:GIGAMETER"] = \
        lambda: noconversion("PT", "GIGAMETER")
    CONVERSIONS["PT:HECTOMETER"] = \
        lambda: noconversion("PT", "HECTOMETER")
    CONVERSIONS["PT:IN"] = \
        lambda: noconversion("PT", "IN")
    CONVERSIONS["PT:KILOMETER"] = \
        lambda: noconversion("PT", "KILOMETER")
    CONVERSIONS["PT:LI"] = \
        lambda: noconversion("PT", "LI")
    CONVERSIONS["PT:LIGHTYEAR"] = \
        lambda: noconversion("PT", "LIGHTYEAR")
    CONVERSIONS["PT:MEGAMETER"] = \
        lambda: noconversion("PT", "MEGAMETER")
    CONVERSIONS["PT:METER"] = \
        lambda: noconversion("PT", "METER")
    CONVERSIONS["PT:MI"] = \
        lambda: noconversion("PT", "MI")
    CONVERSIONS["PT:MICROMETER"] = \
        lambda: noconversion("PT", "MICROMETER")
    CONVERSIONS["PT:MILLIMETER"] = \
        lambda: noconversion("PT", "MILLIMETER")
    CONVERSIONS["PT:NANOMETER"] = \
        lambda: noconversion("PT", "NANOMETER")
    CONVERSIONS["PT:PARSEC"] = \
        lambda: noconversion("PT", "PARSEC")
    CONVERSIONS["PT:PETAMETER"] = \
        lambda: noconversion("PT", "PETAMETER")
    CONVERSIONS["PT:PICOMETER"] = \
        lambda: noconversion("PT", "PICOMETER")
    CONVERSIONS["PT:PIXEL"] = \
        lambda: noconversion("PT", "PIXEL")
    CONVERSIONS["PT:REFERENCEFRAME"] = \
        lambda: noconversion("PT", "REFERENCEFRAME")
    CONVERSIONS["PT:TERAMETER"] = \
        lambda: noconversion("PT", "TERAMETER")
    CONVERSIONS["PT:THOU"] = \
        lambda: noconversion("PT", "THOU")
    CONVERSIONS["PT:YD"] = \
        lambda: noconversion("PT", "YD")
    CONVERSIONS["PT:YOCTOMETER"] = \
        lambda: noconversion("PT", "YOCTOMETER")
    CONVERSIONS["PT:YOTTAMETER"] = \
        lambda: noconversion("PT", "YOTTAMETER")
    CONVERSIONS["PT:ZEPTOMETER"] = \
        lambda: noconversion("PT", "ZEPTOMETER")
    CONVERSIONS["PT:ZETTAMETER"] = \
        lambda: noconversion("PT", "ZETTAMETER")
    CONVERSIONS["REFERENCEFRAME:ANGSTROM"] = \
        lambda: noconversion("REFERENCEFRAME", "ANGSTROM")
    CONVERSIONS["REFERENCEFRAME:ASTRONOMICALUNIT"] = \
        lambda: noconversion("REFERENCEFRAME", "ASTRONOMICALUNIT")
    CONVERSIONS["REFERENCEFRAME:ATTOMETER"] = \
        lambda: noconversion("REFERENCEFRAME", "ATTOMETER")
    CONVERSIONS["REFERENCEFRAME:CENTIMETER"] = \
        lambda: noconversion("REFERENCEFRAME", "CENTIMETER")
    CONVERSIONS["REFERENCEFRAME:DECAMETER"] = \
        lambda: noconversion("REFERENCEFRAME", "DECAMETER")
    CONVERSIONS["REFERENCEFRAME:DECIMETER"] = \
        lambda: noconversion("REFERENCEFRAME", "DECIMETER")
    CONVERSIONS["REFERENCEFRAME:EXAMETER"] = \
        lambda: noconversion("REFERENCEFRAME", "EXAMETER")
    CONVERSIONS["REFERENCEFRAME:FEMTOMETER"] = \
        lambda: noconversion("REFERENCEFRAME", "FEMTOMETER")
    CONVERSIONS["REFERENCEFRAME:FT"] = \
        lambda: noconversion("REFERENCEFRAME", "FT")
    CONVERSIONS["REFERENCEFRAME:GIGAMETER"] = \
        lambda: noconversion("REFERENCEFRAME", "GIGAMETER")
    CONVERSIONS["REFERENCEFRAME:HECTOMETER"] = \
        lambda: noconversion("REFERENCEFRAME", "HECTOMETER")
    CONVERSIONS["REFERENCEFRAME:IN"] = \
        lambda: noconversion("REFERENCEFRAME", "IN")
    CONVERSIONS["REFERENCEFRAME:KILOMETER"] = \
        lambda: noconversion("REFERENCEFRAME", "KILOMETER")
    CONVERSIONS["REFERENCEFRAME:LI"] = \
        lambda: noconversion("REFERENCEFRAME", "LI")
    CONVERSIONS["REFERENCEFRAME:LIGHTYEAR"] = \
        lambda: noconversion("REFERENCEFRAME", "LIGHTYEAR")
    CONVERSIONS["REFERENCEFRAME:MEGAMETER"] = \
        lambda: noconversion("REFERENCEFRAME", "MEGAMETER")
    CONVERSIONS["REFERENCEFRAME:METER"] = \
        lambda: noconversion("REFERENCEFRAME", "METER")
    CONVERSIONS["REFERENCEFRAME:MI"] = \
        lambda: noconversion("REFERENCEFRAME", "MI")
    CONVERSIONS["REFERENCEFRAME:MICROMETER"] = \
        lambda: noconversion("REFERENCEFRAME", "MICROMETER")
    CONVERSIONS["REFERENCEFRAME:MILLIMETER"] = \
        lambda: noconversion("REFERENCEFRAME", "MILLIMETER")
    CONVERSIONS["REFERENCEFRAME:NANOMETER"] = \
        lambda: noconversion("REFERENCEFRAME", "NANOMETER")
    CONVERSIONS["REFERENCEFRAME:PARSEC"] = \
        lambda: noconversion("REFERENCEFRAME", "PARSEC")
    CONVERSIONS["REFERENCEFRAME:PETAMETER"] = \
        lambda: noconversion("REFERENCEFRAME", "PETAMETER")
    CONVERSIONS["REFERENCEFRAME:PICOMETER"] = \
        lambda: noconversion("REFERENCEFRAME", "PICOMETER")
    CONVERSIONS["REFERENCEFRAME:PIXEL"] = \
        lambda: noconversion("REFERENCEFRAME", "PIXEL")
    CONVERSIONS["REFERENCEFRAME:PT"] = \
        lambda: noconversion("REFERENCEFRAME", "PT")
    CONVERSIONS["REFERENCEFRAME:TERAMETER"] = \
        lambda: noconversion("REFERENCEFRAME", "TERAMETER")
    CONVERSIONS["REFERENCEFRAME:THOU"] = \
        lambda: noconversion("REFERENCEFRAME", "THOU")
    CONVERSIONS["REFERENCEFRAME:YD"] = \
        lambda: noconversion("REFERENCEFRAME", "YD")
    CONVERSIONS["REFERENCEFRAME:YOCTOMETER"] = \
        lambda: noconversion("REFERENCEFRAME", "YOCTOMETER")
    CONVERSIONS["REFERENCEFRAME:YOTTAMETER"] = \
        lambda: noconversion("REFERENCEFRAME", "YOTTAMETER")
    CONVERSIONS["REFERENCEFRAME:ZEPTOMETER"] = \
        lambda: noconversion("REFERENCEFRAME", "ZEPTOMETER")
    CONVERSIONS["REFERENCEFRAME:ZETTAMETER"] = \
        lambda: noconversion("REFERENCEFRAME", "ZETTAMETER")
    CONVERSIONS["TERAMETER:ANGSTROM"] = \
        lambda value: (10 ** 22) * value
    CONVERSIONS["TERAMETER:ASTRONOMICALUNIT"] = \
        lambda: noconversion("TERAMETER", "ASTRONOMICALUNIT")
    CONVERSIONS["TERAMETER:ATTOMETER"] = \
        lambda value: (10 ** 30) * value
    CONVERSIONS["TERAMETER:CENTIMETER"] = \
        lambda value: (10 ** 14) * value
    CONVERSIONS["TERAMETER:DECAMETER"] = \
        lambda value: (10 ** 11) * value
    CONVERSIONS["TERAMETER:DECIMETER"] = \
        lambda value: (10 ** 13) * value
    CONVERSIONS["TERAMETER:EXAMETER"] = \
        lambda value: (10 ** -6) * value
    CONVERSIONS["TERAMETER:FEMTOMETER"] = \
        lambda value: (10 ** 27) * value
    CONVERSIONS["TERAMETER:FT"] = \
        lambda: noconversion("TERAMETER", "FT")
    CONVERSIONS["TERAMETER:GIGAMETER"] = \
        lambda value: (10 ** 3) * value
    CONVERSIONS["TERAMETER:HECTOMETER"] = \
        lambda value: (10 ** 10) * value
    CONVERSIONS["TERAMETER:IN"] = \
        lambda: noconversion("TERAMETER", "IN")
    CONVERSIONS["TERAMETER:KILOMETER"] = \
        lambda value: (10 ** 9) * value
    CONVERSIONS["TERAMETER:LI"] = \
        lambda: noconversion("TERAMETER", "LI")
    CONVERSIONS["TERAMETER:LIGHTYEAR"] = \
        lambda: noconversion("TERAMETER", "LIGHTYEAR")
    CONVERSIONS["TERAMETER:MEGAMETER"] = \
        lambda value: (10 ** 6) * value
    CONVERSIONS["TERAMETER:METER"] = \
        lambda value: (10 ** 12) * value
    CONVERSIONS["TERAMETER:MI"] = \
        lambda: noconversion("TERAMETER", "MI")
    CONVERSIONS["TERAMETER:MICROMETER"] = \
        lambda value: (10 ** 18) * value
    CONVERSIONS["TERAMETER:MILLIMETER"] = \
        lambda value: (10 ** 15) * value
    CONVERSIONS["TERAMETER:NANOMETER"] = \
        lambda value: (10 ** 21) * value
    CONVERSIONS["TERAMETER:PARSEC"] = \
        lambda: noconversion("TERAMETER", "PARSEC")
    CONVERSIONS["TERAMETER:PETAMETER"] = \
        lambda value: (10 ** -3) * value
    CONVERSIONS["TERAMETER:PICOMETER"] = \
        lambda value: (10 ** 24) * value
    CONVERSIONS["TERAMETER:PIXEL"] = \
        lambda: noconversion("TERAMETER", "PIXEL")
    CONVERSIONS["TERAMETER:PT"] = \
        lambda: noconversion("TERAMETER", "PT")
    CONVERSIONS["TERAMETER:REFERENCEFRAME"] = \
        lambda: noconversion("TERAMETER", "REFERENCEFRAME")
    CONVERSIONS["TERAMETER:THOU"] = \
        lambda: noconversion("TERAMETER", "THOU")
    CONVERSIONS["TERAMETER:YD"] = \
        lambda: noconversion("TERAMETER", "YD")
    CONVERSIONS["TERAMETER:YOCTOMETER"] = \
        lambda value: (10 ** 36) * value
    CONVERSIONS["TERAMETER:YOTTAMETER"] = \
        lambda value: (10 ** -12) * value
    CONVERSIONS["TERAMETER:ZEPTOMETER"] = \
        lambda value: (10 ** 33) * value
    CONVERSIONS["TERAMETER:ZETTAMETER"] = \
        lambda value: (10 ** -9) * value
    CONVERSIONS["THOU:ANGSTROM"] = \
        lambda: noconversion("THOU", "ANGSTROM")
    CONVERSIONS["THOU:ASTRONOMICALUNIT"] = \
        lambda: noconversion("THOU", "ASTRONOMICALUNIT")
    CONVERSIONS["THOU:ATTOMETER"] = \
        lambda: noconversion("THOU", "ATTOMETER")
    CONVERSIONS["THOU:CENTIMETER"] = \
        lambda: noconversion("THOU", "CENTIMETER")
    CONVERSIONS["THOU:DECAMETER"] = \
        lambda: noconversion("THOU", "DECAMETER")
    CONVERSIONS["THOU:DECIMETER"] = \
        lambda: noconversion("THOU", "DECIMETER")
    CONVERSIONS["THOU:EXAMETER"] = \
        lambda: noconversion("THOU", "EXAMETER")
    CONVERSIONS["THOU:FEMTOMETER"] = \
        lambda: noconversion("THOU", "FEMTOMETER")
    CONVERSIONS["THOU:FT"] = \
        lambda: noconversion("THOU", "FT")
    CONVERSIONS["THOU:GIGAMETER"] = \
        lambda: noconversion("THOU", "GIGAMETER")
    CONVERSIONS["THOU:HECTOMETER"] = \
        lambda: noconversion("THOU", "HECTOMETER")
    CONVERSIONS["THOU:IN"] = \
        lambda: noconversion("THOU", "IN")
    CONVERSIONS["THOU:KILOMETER"] = \
        lambda: noconversion("THOU", "KILOMETER")
    CONVERSIONS["THOU:LI"] = \
        lambda: noconversion("THOU", "LI")
    CONVERSIONS["THOU:LIGHTYEAR"] = \
        lambda: noconversion("THOU", "LIGHTYEAR")
    CONVERSIONS["THOU:MEGAMETER"] = \
        lambda: noconversion("THOU", "MEGAMETER")
    CONVERSIONS["THOU:METER"] = \
        lambda: noconversion("THOU", "METER")
    CONVERSIONS["THOU:MI"] = \
        lambda: noconversion("THOU", "MI")
    CONVERSIONS["THOU:MICROMETER"] = \
        lambda: noconversion("THOU", "MICROMETER")
    CONVERSIONS["THOU:MILLIMETER"] = \
        lambda: noconversion("THOU", "MILLIMETER")
    CONVERSIONS["THOU:NANOMETER"] = \
        lambda: noconversion("THOU", "NANOMETER")
    CONVERSIONS["THOU:PARSEC"] = \
        lambda: noconversion("THOU", "PARSEC")
    CONVERSIONS["THOU:PETAMETER"] = \
        lambda: noconversion("THOU", "PETAMETER")
    CONVERSIONS["THOU:PICOMETER"] = \
        lambda: noconversion("THOU", "PICOMETER")
    CONVERSIONS["THOU:PIXEL"] = \
        lambda: noconversion("THOU", "PIXEL")
    CONVERSIONS["THOU:PT"] = \
        lambda: noconversion("THOU", "PT")
    CONVERSIONS["THOU:REFERENCEFRAME"] = \
        lambda: noconversion("THOU", "REFERENCEFRAME")
    CONVERSIONS["THOU:TERAMETER"] = \
        lambda: noconversion("THOU", "TERAMETER")
    CONVERSIONS["THOU:YD"] = \
        lambda: noconversion("THOU", "YD")
    CONVERSIONS["THOU:YOCTOMETER"] = \
        lambda: noconversion("THOU", "YOCTOMETER")
    CONVERSIONS["THOU:YOTTAMETER"] = \
        lambda: noconversion("THOU", "YOTTAMETER")
    CONVERSIONS["THOU:ZEPTOMETER"] = \
        lambda: noconversion("THOU", "ZEPTOMETER")
    CONVERSIONS["THOU:ZETTAMETER"] = \
        lambda: noconversion("THOU", "ZETTAMETER")
    CONVERSIONS["YD:ANGSTROM"] = \
        lambda: noconversion("YD", "ANGSTROM")
    CONVERSIONS["YD:ASTRONOMICALUNIT"] = \
        lambda: noconversion("YD", "ASTRONOMICALUNIT")
    CONVERSIONS["YD:ATTOMETER"] = \
        lambda: noconversion("YD", "ATTOMETER")
    CONVERSIONS["YD:CENTIMETER"] = \
        lambda: noconversion("YD", "CENTIMETER")
    CONVERSIONS["YD:DECAMETER"] = \
        lambda: noconversion("YD", "DECAMETER")
    CONVERSIONS["YD:DECIMETER"] = \
        lambda: noconversion("YD", "DECIMETER")
    CONVERSIONS["YD:EXAMETER"] = \
        lambda: noconversion("YD", "EXAMETER")
    CONVERSIONS["YD:FEMTOMETER"] = \
        lambda: noconversion("YD", "FEMTOMETER")
    CONVERSIONS["YD:FT"] = \
        lambda: noconversion("YD", "FT")
    CONVERSIONS["YD:GIGAMETER"] = \
        lambda: noconversion("YD", "GIGAMETER")
    CONVERSIONS["YD:HECTOMETER"] = \
        lambda: noconversion("YD", "HECTOMETER")
    CONVERSIONS["YD:IN"] = \
        lambda: noconversion("YD", "IN")
    CONVERSIONS["YD:KILOMETER"] = \
        lambda: noconversion("YD", "KILOMETER")
    CONVERSIONS["YD:LI"] = \
        lambda: noconversion("YD", "LI")
    CONVERSIONS["YD:LIGHTYEAR"] = \
        lambda: noconversion("YD", "LIGHTYEAR")
    CONVERSIONS["YD:MEGAMETER"] = \
        lambda: noconversion("YD", "MEGAMETER")
    CONVERSIONS["YD:METER"] = \
        lambda: noconversion("YD", "METER")
    CONVERSIONS["YD:MI"] = \
        lambda: noconversion("YD", "MI")
    CONVERSIONS["YD:MICROMETER"] = \
        lambda: noconversion("YD", "MICROMETER")
    CONVERSIONS["YD:MILLIMETER"] = \
        lambda: noconversion("YD", "MILLIMETER")
    CONVERSIONS["YD:NANOMETER"] = \
        lambda: noconversion("YD", "NANOMETER")
    CONVERSIONS["YD:PARSEC"] = \
        lambda: noconversion("YD", "PARSEC")
    CONVERSIONS["YD:PETAMETER"] = \
        lambda: noconversion("YD", "PETAMETER")
    CONVERSIONS["YD:PICOMETER"] = \
        lambda: noconversion("YD", "PICOMETER")
    CONVERSIONS["YD:PIXEL"] = \
        lambda: noconversion("YD", "PIXEL")
    CONVERSIONS["YD:PT"] = \
        lambda: noconversion("YD", "PT")
    CONVERSIONS["YD:REFERENCEFRAME"] = \
        lambda: noconversion("YD", "REFERENCEFRAME")
    CONVERSIONS["YD:TERAMETER"] = \
        lambda: noconversion("YD", "TERAMETER")
    CONVERSIONS["YD:THOU"] = \
        lambda: noconversion("YD", "THOU")
    CONVERSIONS["YD:YOCTOMETER"] = \
        lambda: noconversion("YD", "YOCTOMETER")
    CONVERSIONS["YD:YOTTAMETER"] = \
        lambda: noconversion("YD", "YOTTAMETER")
    CONVERSIONS["YD:ZEPTOMETER"] = \
        lambda: noconversion("YD", "ZEPTOMETER")
    CONVERSIONS["YD:ZETTAMETER"] = \
        lambda: noconversion("YD", "ZETTAMETER")
    CONVERSIONS["YOCTOMETER:ANGSTROM"] = \
        lambda value: (10 ** -14) * value
    CONVERSIONS["YOCTOMETER:ASTRONOMICALUNIT"] = \
        lambda: noconversion("YOCTOMETER", "ASTRONOMICALUNIT")
    CONVERSIONS["YOCTOMETER:ATTOMETER"] = \
        lambda value: (10 ** -6) * value
    CONVERSIONS["YOCTOMETER:CENTIMETER"] = \
        lambda value: (10 ** -22) * value
    CONVERSIONS["YOCTOMETER:DECAMETER"] = \
        lambda value: (10 ** -25) * value
    CONVERSIONS["YOCTOMETER:DECIMETER"] = \
        lambda value: (10 ** -23) * value
    CONVERSIONS["YOCTOMETER:EXAMETER"] = \
        lambda value: (10 ** -42) * value
    CONVERSIONS["YOCTOMETER:FEMTOMETER"] = \
        lambda value: (10 ** -9) * value
    CONVERSIONS["YOCTOMETER:FT"] = \
        lambda: noconversion("YOCTOMETER", "FT")
    CONVERSIONS["YOCTOMETER:GIGAMETER"] = \
        lambda value: (10 ** -33) * value
    CONVERSIONS["YOCTOMETER:HECTOMETER"] = \
        lambda value: (10 ** -26) * value
    CONVERSIONS["YOCTOMETER:IN"] = \
        lambda: noconversion("YOCTOMETER", "IN")
    CONVERSIONS["YOCTOMETER:KILOMETER"] = \
        lambda value: (10 ** -27) * value
    CONVERSIONS["YOCTOMETER:LI"] = \
        lambda: noconversion("YOCTOMETER", "LI")
    CONVERSIONS["YOCTOMETER:LIGHTYEAR"] = \
        lambda: noconversion("YOCTOMETER", "LIGHTYEAR")
    CONVERSIONS["YOCTOMETER:MEGAMETER"] = \
        lambda value: (10 ** -30) * value
    CONVERSIONS["YOCTOMETER:METER"] = \
        lambda value: (10 ** -24) * value
    CONVERSIONS["YOCTOMETER:MI"] = \
        lambda: noconversion("YOCTOMETER", "MI")
    CONVERSIONS["YOCTOMETER:MICROMETER"] = \
        lambda value: (10 ** -18) * value
    CONVERSIONS["YOCTOMETER:MILLIMETER"] = \
        lambda value: (10 ** -21) * value
    CONVERSIONS["YOCTOMETER:NANOMETER"] = \
        lambda value: (10 ** -15) * value
    CONVERSIONS["YOCTOMETER:PARSEC"] = \
        lambda: noconversion("YOCTOMETER", "PARSEC")
    CONVERSIONS["YOCTOMETER:PETAMETER"] = \
        lambda value: (10 ** -39) * value
    CONVERSIONS["YOCTOMETER:PICOMETER"] = \
        lambda value: (10 ** -12) * value
    CONVERSIONS["YOCTOMETER:PIXEL"] = \
        lambda: noconversion("YOCTOMETER", "PIXEL")
    CONVERSIONS["YOCTOMETER:PT"] = \
        lambda: noconversion("YOCTOMETER", "PT")
    CONVERSIONS["YOCTOMETER:REFERENCEFRAME"] = \
        lambda: noconversion("YOCTOMETER", "REFERENCEFRAME")
    CONVERSIONS["YOCTOMETER:TERAMETER"] = \
        lambda value: (10 ** -36) * value
    CONVERSIONS["YOCTOMETER:THOU"] = \
        lambda: noconversion("YOCTOMETER", "THOU")
    CONVERSIONS["YOCTOMETER:YD"] = \
        lambda: noconversion("YOCTOMETER", "YD")
    CONVERSIONS["YOCTOMETER:YOTTAMETER"] = \
        lambda value: (10 ** -48) * value
    CONVERSIONS["YOCTOMETER:ZEPTOMETER"] = \
        lambda value: (10 ** -3) * value
    CONVERSIONS["YOCTOMETER:ZETTAMETER"] = \
        lambda value: (10 ** -45) * value
    CONVERSIONS["YOTTAMETER:ANGSTROM"] = \
        lambda value: (10 ** 34) * value
    CONVERSIONS["YOTTAMETER:ASTRONOMICALUNIT"] = \
        lambda: noconversion("YOTTAMETER", "ASTRONOMICALUNIT")
    CONVERSIONS["YOTTAMETER:ATTOMETER"] = \
        lambda value: (10 ** 42) * value
    CONVERSIONS["YOTTAMETER:CENTIMETER"] = \
        lambda value: (10 ** 26) * value
    CONVERSIONS["YOTTAMETER:DECAMETER"] = \
        lambda value: (10 ** 23) * value
    CONVERSIONS["YOTTAMETER:DECIMETER"] = \
        lambda value: (10 ** 25) * value
    CONVERSIONS["YOTTAMETER:EXAMETER"] = \
        lambda value: (10 ** 6) * value
    CONVERSIONS["YOTTAMETER:FEMTOMETER"] = \
        lambda value: (10 ** 39) * value
    CONVERSIONS["YOTTAMETER:FT"] = \
        lambda: noconversion("YOTTAMETER", "FT")
    CONVERSIONS["YOTTAMETER:GIGAMETER"] = \
        lambda value: (10 ** 15) * value
    CONVERSIONS["YOTTAMETER:HECTOMETER"] = \
        lambda value: (10 ** 22) * value
    CONVERSIONS["YOTTAMETER:IN"] = \
        lambda: noconversion("YOTTAMETER", "IN")
    CONVERSIONS["YOTTAMETER:KILOMETER"] = \
        lambda value: (10 ** 21) * value
    CONVERSIONS["YOTTAMETER:LI"] = \
        lambda: noconversion("YOTTAMETER", "LI")
    CONVERSIONS["YOTTAMETER:LIGHTYEAR"] = \
        lambda: noconversion("YOTTAMETER", "LIGHTYEAR")
    CONVERSIONS["YOTTAMETER:MEGAMETER"] = \
        lambda value: (10 ** 18) * value
    CONVERSIONS["YOTTAMETER:METER"] = \
        lambda value: (10 ** 24) * value
    CONVERSIONS["YOTTAMETER:MI"] = \
        lambda: noconversion("YOTTAMETER", "MI")
    CONVERSIONS["YOTTAMETER:MICROMETER"] = \
        lambda value: (10 ** 30) * value
    CONVERSIONS["YOTTAMETER:MILLIMETER"] = \
        lambda value: (10 ** 27) * value
    CONVERSIONS["YOTTAMETER:NANOMETER"] = \
        lambda value: (10 ** 33) * value
    CONVERSIONS["YOTTAMETER:PARSEC"] = \
        lambda: noconversion("YOTTAMETER", "PARSEC")
    CONVERSIONS["YOTTAMETER:PETAMETER"] = \
        lambda value: (10 ** 9) * value
    CONVERSIONS["YOTTAMETER:PICOMETER"] = \
        lambda value: (10 ** 36) * value
    CONVERSIONS["YOTTAMETER:PIXEL"] = \
        lambda: noconversion("YOTTAMETER", "PIXEL")
    CONVERSIONS["YOTTAMETER:PT"] = \
        lambda: noconversion("YOTTAMETER", "PT")
    CONVERSIONS["YOTTAMETER:REFERENCEFRAME"] = \
        lambda: noconversion("YOTTAMETER", "REFERENCEFRAME")
    CONVERSIONS["YOTTAMETER:TERAMETER"] = \
        lambda value: (10 ** 12) * value
    CONVERSIONS["YOTTAMETER:THOU"] = \
        lambda: noconversion("YOTTAMETER", "THOU")
    CONVERSIONS["YOTTAMETER:YD"] = \
        lambda: noconversion("YOTTAMETER", "YD")
    CONVERSIONS["YOTTAMETER:YOCTOMETER"] = \
        lambda value: (10 ** 48) * value
    CONVERSIONS["YOTTAMETER:ZEPTOMETER"] = \
        lambda value: (10 ** 45) * value
    CONVERSIONS["YOTTAMETER:ZETTAMETER"] = \
        lambda value: (10 ** 3) * value
    CONVERSIONS["ZEPTOMETER:ANGSTROM"] = \
        lambda value: (10 ** -11) * value
    CONVERSIONS["ZEPTOMETER:ASTRONOMICALUNIT"] = \
        lambda: noconversion("ZEPTOMETER", "ASTRONOMICALUNIT")
    CONVERSIONS["ZEPTOMETER:ATTOMETER"] = \
        lambda value: (10 ** -3) * value
    CONVERSIONS["ZEPTOMETER:CENTIMETER"] = \
        lambda value: (10 ** -19) * value
    CONVERSIONS["ZEPTOMETER:DECAMETER"] = \
        lambda value: (10 ** -22) * value
    CONVERSIONS["ZEPTOMETER:DECIMETER"] = \
        lambda value: (10 ** -20) * value
    CONVERSIONS["ZEPTOMETER:EXAMETER"] = \
        lambda value: (10 ** -39) * value
    CONVERSIONS["ZEPTOMETER:FEMTOMETER"] = \
        lambda value: (10 ** -6) * value
    CONVERSIONS["ZEPTOMETER:FT"] = \
        lambda: noconversion("ZEPTOMETER", "FT")
    CONVERSIONS["ZEPTOMETER:GIGAMETER"] = \
        lambda value: (10 ** -30) * value
    CONVERSIONS["ZEPTOMETER:HECTOMETER"] = \
        lambda value: (10 ** -23) * value
    CONVERSIONS["ZEPTOMETER:IN"] = \
        lambda: noconversion("ZEPTOMETER", "IN")
    CONVERSIONS["ZEPTOMETER:KILOMETER"] = \
        lambda value: (10 ** -24) * value
    CONVERSIONS["ZEPTOMETER:LI"] = \
        lambda: noconversion("ZEPTOMETER", "LI")
    CONVERSIONS["ZEPTOMETER:LIGHTYEAR"] = \
        lambda: noconversion("ZEPTOMETER", "LIGHTYEAR")
    CONVERSIONS["ZEPTOMETER:MEGAMETER"] = \
        lambda value: (10 ** -27) * value
    CONVERSIONS["ZEPTOMETER:METER"] = \
        lambda value: (10 ** -21) * value
    CONVERSIONS["ZEPTOMETER:MI"] = \
        lambda: noconversion("ZEPTOMETER", "MI")
    CONVERSIONS["ZEPTOMETER:MICROMETER"] = \
        lambda value: (10 ** -15) * value
    CONVERSIONS["ZEPTOMETER:MILLIMETER"] = \
        lambda value: (10 ** -18) * value
    CONVERSIONS["ZEPTOMETER:NANOMETER"] = \
        lambda value: (10 ** -12) * value
    CONVERSIONS["ZEPTOMETER:PARSEC"] = \
        lambda: noconversion("ZEPTOMETER", "PARSEC")
    CONVERSIONS["ZEPTOMETER:PETAMETER"] = \
        lambda value: (10 ** -36) * value
    CONVERSIONS["ZEPTOMETER:PICOMETER"] = \
        lambda value: (10 ** -9) * value
    CONVERSIONS["ZEPTOMETER:PIXEL"] = \
        lambda: noconversion("ZEPTOMETER", "PIXEL")
    CONVERSIONS["ZEPTOMETER:PT"] = \
        lambda: noconversion("ZEPTOMETER", "PT")
    CONVERSIONS["ZEPTOMETER:REFERENCEFRAME"] = \
        lambda: noconversion("ZEPTOMETER", "REFERENCEFRAME")
    CONVERSIONS["ZEPTOMETER:TERAMETER"] = \
        lambda value: (10 ** -33) * value
    CONVERSIONS["ZEPTOMETER:THOU"] = \
        lambda: noconversion("ZEPTOMETER", "THOU")
    CONVERSIONS["ZEPTOMETER:YD"] = \
        lambda: noconversion("ZEPTOMETER", "YD")
    CONVERSIONS["ZEPTOMETER:YOCTOMETER"] = \
        lambda value: (10 ** 3) * value
    CONVERSIONS["ZEPTOMETER:YOTTAMETER"] = \
        lambda value: (10 ** -45) * value
    CONVERSIONS["ZEPTOMETER:ZETTAMETER"] = \
        lambda value: (10 ** -42) * value
    CONVERSIONS["ZETTAMETER:ANGSTROM"] = \
        lambda value: (10 ** 31) * value
    CONVERSIONS["ZETTAMETER:ASTRONOMICALUNIT"] = \
        lambda: noconversion("ZETTAMETER", "ASTRONOMICALUNIT")
    CONVERSIONS["ZETTAMETER:ATTOMETER"] = \
        lambda value: (10 ** 39) * value
    CONVERSIONS["ZETTAMETER:CENTIMETER"] = \
        lambda value: (10 ** 23) * value
    CONVERSIONS["ZETTAMETER:DECAMETER"] = \
        lambda value: (10 ** 20) * value
    CONVERSIONS["ZETTAMETER:DECIMETER"] = \
        lambda value: (10 ** 22) * value
    CONVERSIONS["ZETTAMETER:EXAMETER"] = \
        lambda value: (10 ** 3) * value
    CONVERSIONS["ZETTAMETER:FEMTOMETER"] = \
        lambda value: (10 ** 36) * value
    CONVERSIONS["ZETTAMETER:FT"] = \
        lambda: noconversion("ZETTAMETER", "FT")
    CONVERSIONS["ZETTAMETER:GIGAMETER"] = \
        lambda value: (10 ** 12) * value
    CONVERSIONS["ZETTAMETER:HECTOMETER"] = \
        lambda value: (10 ** 19) * value
    CONVERSIONS["ZETTAMETER:IN"] = \
        lambda: noconversion("ZETTAMETER", "IN")
    CONVERSIONS["ZETTAMETER:KILOMETER"] = \
        lambda value: (10 ** 18) * value
    CONVERSIONS["ZETTAMETER:LI"] = \
        lambda: noconversion("ZETTAMETER", "LI")
    CONVERSIONS["ZETTAMETER:LIGHTYEAR"] = \
        lambda: noconversion("ZETTAMETER", "LIGHTYEAR")
    CONVERSIONS["ZETTAMETER:MEGAMETER"] = \
        lambda value: (10 ** 15) * value
    CONVERSIONS["ZETTAMETER:METER"] = \
        lambda value: (10 ** 21) * value
    CONVERSIONS["ZETTAMETER:MI"] = \
        lambda: noconversion("ZETTAMETER", "MI")
    CONVERSIONS["ZETTAMETER:MICROMETER"] = \
        lambda value: (10 ** 27) * value
    CONVERSIONS["ZETTAMETER:MILLIMETER"] = \
        lambda value: (10 ** 24) * value
    CONVERSIONS["ZETTAMETER:NANOMETER"] = \
        lambda value: (10 ** 30) * value
    CONVERSIONS["ZETTAMETER:PARSEC"] = \
        lambda: noconversion("ZETTAMETER", "PARSEC")
    CONVERSIONS["ZETTAMETER:PETAMETER"] = \
        lambda value: (10 ** 6) * value
    CONVERSIONS["ZETTAMETER:PICOMETER"] = \
        lambda value: (10 ** 33) * value
    CONVERSIONS["ZETTAMETER:PIXEL"] = \
        lambda: noconversion("ZETTAMETER", "PIXEL")
    CONVERSIONS["ZETTAMETER:PT"] = \
        lambda: noconversion("ZETTAMETER", "PT")
    CONVERSIONS["ZETTAMETER:REFERENCEFRAME"] = \
        lambda: noconversion("ZETTAMETER", "REFERENCEFRAME")
    CONVERSIONS["ZETTAMETER:TERAMETER"] = \
        lambda value: (10 ** 9) * value
    CONVERSIONS["ZETTAMETER:THOU"] = \
        lambda: noconversion("ZETTAMETER", "THOU")
    CONVERSIONS["ZETTAMETER:YD"] = \
        lambda: noconversion("ZETTAMETER", "YD")
    CONVERSIONS["ZETTAMETER:YOCTOMETER"] = \
        lambda value: (10 ** 45) * value
    CONVERSIONS["ZETTAMETER:YOTTAMETER"] = \
        lambda value: (10 ** -3) * value
    CONVERSIONS["ZETTAMETER:ZEPTOMETER"] = \
        lambda value: (10 ** 42) * value

    SYMBOLS = dict()
    SYMBOLS["ANGSTROM"] = "Å"
    SYMBOLS["ASTRONOMICALUNIT"] = "ua"
    SYMBOLS["ATTOMETER"] = "am"
    SYMBOLS["CENTIMETER"] = "cm"
    SYMBOLS["DECAMETER"] = "dam"
    SYMBOLS["DECIMETER"] = "dm"
    SYMBOLS["EXAMETER"] = "Em"
    SYMBOLS["FEMTOMETER"] = "fm"
    SYMBOLS["FOOT"] = "ft"
    SYMBOLS["GIGAMETER"] = "Gm"
    SYMBOLS["HECTOMETER"] = "hm"
    SYMBOLS["INCH"] = "in"
    SYMBOLS["KILOMETER"] = "km"
    SYMBOLS["LIGHTYEAR"] = "ly"
    SYMBOLS["LINE"] = "li"
    SYMBOLS["MEGAMETER"] = "Mm"
    SYMBOLS["METER"] = "m"
    SYMBOLS["MICROMETER"] = "µm"
    SYMBOLS["MILE"] = "mi"
    SYMBOLS["MILLIMETER"] = "mm"
    SYMBOLS["NANOMETER"] = "nm"
    SYMBOLS["PARSEC"] = "pc"
    SYMBOLS["PETAMETER"] = "Pm"
    SYMBOLS["PICOMETER"] = "pm"
    SYMBOLS["PIXEL"] = "pixel"
    SYMBOLS["POINT"] = "pt"
    SYMBOLS["REFERENCEFRAME"] = "reference frame"
    SYMBOLS["TERAMETER"] = "Tm"
    SYMBOLS["THOU"] = "thou"
    SYMBOLS["YARD"] = "yd"
    SYMBOLS["YOCTOMETER"] = "ym"
    SYMBOLS["YOTTAMETER"] = "Ym"
    SYMBOLS["ZEPTOMETER"] = "zm"
    SYMBOLS["ZETTAMETER"] = "Zm"

    def __init__(self, value=None, unit=None):
        _omero_model.Length.__init__(self)
        if isinstance(value, _omero_model.LengthI):
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
                self.setUnit(getattr(UnitsLength, str(target)))
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
        return LengthI.SYMBOLS.get(str(unit))

    def setUnit(self, unit, current=None):
        self._unit = unit

    def setValue(self, value, current=None):
        self._value = value

    def __str__(self):
        return self._base_string(self.getValue(), self.getUnit())

_omero_model.LengthI = LengthI
