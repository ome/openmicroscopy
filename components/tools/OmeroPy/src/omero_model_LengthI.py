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

from omero.model.conversions import Add
from omero.model.conversions import Int
from omero.model.conversions import Mul
from omero.model.conversions import Pow
from omero.model.conversions import Rat
from omero.model.conversions import Sym


class LengthI(_omero_model.Length, UnitBase):

    CONVERSIONS = dict()
    CONVERSIONS["ANGSTROM:ASTRONMICALUNIT"] = \
        Mul(Mul(Int(1495978707), Pow(10, 12)), Sym("ang"))
    CONVERSIONS["ANGSTROM:ATTOMETER"] = \
        Mul(Rat(Int(1), Pow(10, 8)), Sym("ang"))
    CONVERSIONS["ANGSTROM:CENTIMETER"] = \
        Mul(Pow(10, 8), Sym("ang"))
    CONVERSIONS["ANGSTROM:DECAMETER"] = \
        Mul(Pow(10, 11), Sym("ang"))
    CONVERSIONS["ANGSTROM:DECIMETER"] = \
        Mul(Pow(10, 9), Sym("ang"))
    CONVERSIONS["ANGSTROM:EXAMETER"] = \
        Mul(Pow(10, 28), Sym("ang"))
    CONVERSIONS["ANGSTROM:FEMTOMETER"] = \
        Mul(Rat(Int(1), Pow(10, 5)), Sym("ang"))
    CONVERSIONS["ANGSTROM:FOOT"] = \
        Mul(Mul(Int(3048), Pow(10, 6)), Sym("ang"))
    CONVERSIONS["ANGSTROM:GIGAMETER"] = \
        Mul(Pow(10, 19), Sym("ang"))
    CONVERSIONS["ANGSTROM:HECTOMETER"] = \
        Mul(Pow(10, 12), Sym("ang"))
    CONVERSIONS["ANGSTROM:INCH"] = \
        Mul(Mul(Int(254), Pow(10, 6)), Sym("ang"))
    CONVERSIONS["ANGSTROM:KILOMETER"] = \
        Mul(Pow(10, 13), Sym("ang"))
    CONVERSIONS["ANGSTROM:LIGHTYEAR"] = \
        Mul(Mul(Int("94607304725808"), Pow(10, 12)), Sym("ang"))
    CONVERSIONS["ANGSTROM:LINE"] = \
        Mul(Rat(Mul(Int(635), Pow(10, 5)), Int(3)), Sym("ang"))
    CONVERSIONS["ANGSTROM:MEGAMETER"] = \
        Mul(Pow(10, 16), Sym("ang"))
    CONVERSIONS["ANGSTROM:METER"] = \
        Mul(Pow(10, 10), Sym("ang"))
    CONVERSIONS["ANGSTROM:MICROMETER"] = \
        Mul(Pow(10, 4), Sym("ang"))
    CONVERSIONS["ANGSTROM:MILE"] = \
        Mul(Mul(Int(1609344), Pow(10, 7)), Sym("ang"))
    CONVERSIONS["ANGSTROM:MILLIMETER"] = \
        Mul(Pow(10, 7), Sym("ang"))
    CONVERSIONS["ANGSTROM:NANOMETER"] = \
        Mul(Int(10), Sym("ang"))
    CONVERSIONS["ANGSTROM:PARSEC"] = \
        Mul(Mul(Int(30856776), Pow(10, 19)), Sym("ang"))
    CONVERSIONS["ANGSTROM:PETAMETER"] = \
        Mul(Pow(10, 25), Sym("ang"))
    CONVERSIONS["ANGSTROM:PICOMETER"] = \
        Mul(Rat(Int(1), Int(100)), Sym("ang"))
    CONVERSIONS["ANGSTROM:POINT"] = \
        Mul(Rat(Mul(Int(3175), Pow(10, 4)), Int(9)), Sym("ang"))
    CONVERSIONS["ANGSTROM:TERAMETER"] = \
        Mul(Pow(10, 22), Sym("ang"))
    CONVERSIONS["ANGSTROM:THOU"] = \
        Mul(Int(254000), Sym("ang"))
    CONVERSIONS["ANGSTROM:YARD"] = \
        Mul(Mul(Int(9144), Pow(10, 6)), Sym("ang"))
    CONVERSIONS["ANGSTROM:YOCTOMETER"] = \
        Mul(Rat(Int(1), Pow(10, 14)), Sym("ang"))
    CONVERSIONS["ANGSTROM:YOTTAMETER"] = \
        Mul(Pow(10, 34), Sym("ang"))
    CONVERSIONS["ANGSTROM:ZEPTOMETER"] = \
        Mul(Rat(Int(1), Pow(10, 11)), Sym("ang"))
    CONVERSIONS["ANGSTROM:ZETTAMETER"] = \
        Mul(Pow(10, 31), Sym("ang"))
    CONVERSIONS["ASTRONMICALUNIT:ANGSTROM"] = \
        Mul(Rat(Int(1), Mul(Int(1495978707), Pow(10, 12))), Sym("ua"))
    CONVERSIONS["ASTRONMICALUNIT:ATTOMETER"] = \
        Mul(Rat(Int(1), Mul(Int(1495978707), Pow(10, 20))), Sym("ua"))
    CONVERSIONS["ASTRONMICALUNIT:CENTIMETER"] = \
        Mul(Rat(Int(1), Mul(Int(1495978707), Pow(10, 4))), Sym("ua"))
    CONVERSIONS["ASTRONMICALUNIT:DECAMETER"] = \
        Mul(Rat(Int(1), Int("14959787070")), Sym("ua"))
    CONVERSIONS["ASTRONMICALUNIT:DECIMETER"] = \
        Mul(Rat(Int(1), Int("1495978707000")), Sym("ua"))
    CONVERSIONS["ASTRONMICALUNIT:EXAMETER"] = \
        Mul(Rat(Pow(10, 16), Int(1495978707)), Sym("ua"))
    CONVERSIONS["ASTRONMICALUNIT:FEMTOMETER"] = \
        Mul(Rat(Int(1), Mul(Int(1495978707), Pow(10, 17))), Sym("ua"))
    CONVERSIONS["ASTRONMICALUNIT:FOOT"] = \
        Mul(Rat(Int(127), Int("62332446125000")), Sym("ua"))
    CONVERSIONS["ASTRONMICALUNIT:GIGAMETER"] = \
        Mul(Rat(Pow(10, 7), Int(1495978707)), Sym("ua"))
    CONVERSIONS["ASTRONMICALUNIT:HECTOMETER"] = \
        Mul(Rat(Int(1), Int(1495978707)), Sym("ua"))
    CONVERSIONS["ASTRONMICALUNIT:INCH"] = \
        Mul(Rat(Int(127), Mul(Int("7479893535"), Pow(10, 5))), Sym("ua"))
    CONVERSIONS["ASTRONMICALUNIT:KILOMETER"] = \
        Mul(Rat(Int(10), Int(1495978707)), Sym("ua"))
    CONVERSIONS["ASTRONMICALUNIT:LIGHTYEAR"] = \
        Mul(Rat(Int("431996825232"), Int(6830953)), Sym("ua"))
    CONVERSIONS["ASTRONMICALUNIT:LINE"] = \
        Mul(Rat(Int(127), Mul(Int("8975872242"), Pow(10, 6))), Sym("ua"))
    CONVERSIONS["ASTRONMICALUNIT:MEGAMETER"] = \
        Mul(Rat(Pow(10, 4), Int(1495978707)), Sym("ua"))
    CONVERSIONS["ASTRONMICALUNIT:METER"] = \
        Mul(Rat(Int(1), Int("149597870700")), Sym("ua"))
    CONVERSIONS["ASTRONMICALUNIT:MICROMETER"] = \
        Mul(Rat(Int(1), Mul(Int(1495978707), Pow(10, 8))), Sym("ua"))
    CONVERSIONS["ASTRONMICALUNIT:MILE"] = \
        Mul(Rat(Int(16764), Int("1558311153125")), Sym("ua"))
    CONVERSIONS["ASTRONMICALUNIT:MILLIMETER"] = \
        Mul(Rat(Int(1), Mul(Int(1495978707), Pow(10, 5))), Sym("ua"))
    CONVERSIONS["ASTRONMICALUNIT:NANOMETER"] = \
        Mul(Rat(Int(1), Mul(Int(1495978707), Pow(10, 11))), Sym("ua"))
    CONVERSIONS["ASTRONMICALUNIT:PARSEC"] = \
        Mul(Rat(Mul(Int(10285592), Pow(10, 7)), Int(498659569)), Sym("ua"))
    CONVERSIONS["ASTRONMICALUNIT:PETAMETER"] = \
        Mul(Rat(Pow(10, 13), Int(1495978707)), Sym("ua"))
    CONVERSIONS["ASTRONMICALUNIT:PICOMETER"] = \
        Mul(Rat(Int(1), Mul(Int(1495978707), Pow(10, 14))), Sym("ua"))
    CONVERSIONS["ASTRONMICALUNIT:POINT"] = \
        Mul(Rat(Int(127), Mul(Int("53855233452"), Pow(10, 6))), Sym("ua"))
    CONVERSIONS["ASTRONMICALUNIT:TERAMETER"] = \
        Mul(Rat(Pow(10, 10), Int(1495978707)), Sym("ua"))
    CONVERSIONS["ASTRONMICALUNIT:THOU"] = \
        Mul(Rat(Int(127), Mul(Int("7479893535"), Pow(10, 8))), Sym("ua"))
    CONVERSIONS["ASTRONMICALUNIT:YARD"] = \
        Mul(Rat(Int(381), Int("62332446125000")), Sym("ua"))
    CONVERSIONS["ASTRONMICALUNIT:YOCTOMETER"] = \
        Mul(Rat(Int(1), Mul(Int(1495978707), Pow(10, 26))), Sym("ua"))
    CONVERSIONS["ASTRONMICALUNIT:YOTTAMETER"] = \
        Mul(Rat(Pow(10, 22), Int(1495978707)), Sym("ua"))
    CONVERSIONS["ASTRONMICALUNIT:ZEPTOMETER"] = \
        Mul(Rat(Int(1), Mul(Int(1495978707), Pow(10, 23))), Sym("ua"))
    CONVERSIONS["ASTRONMICALUNIT:ZETTAMETER"] = \
        Mul(Rat(Pow(10, 19), Int(1495978707)), Sym("ua"))
    CONVERSIONS["ATTOMETER:ANGSTROM"] = \
        Mul(Pow(10, 8), Sym("attom"))
    CONVERSIONS["ATTOMETER:ASTRONMICALUNIT"] = \
        Mul(Mul(Int(1495978707), Pow(10, 20)), Sym("attom"))
    CONVERSIONS["ATTOMETER:CENTIMETER"] = \
        Mul(Pow(10, 16), Sym("attom"))
    CONVERSIONS["ATTOMETER:DECAMETER"] = \
        Mul(Pow(10, 19), Sym("attom"))
    CONVERSIONS["ATTOMETER:DECIMETER"] = \
        Mul(Pow(10, 17), Sym("attom"))
    CONVERSIONS["ATTOMETER:EXAMETER"] = \
        Mul(Pow(10, 36), Sym("attom"))
    CONVERSIONS["ATTOMETER:FEMTOMETER"] = \
        Mul(Int(1000), Sym("attom"))
    CONVERSIONS["ATTOMETER:FOOT"] = \
        Mul(Mul(Int(3048), Pow(10, 14)), Sym("attom"))
    CONVERSIONS["ATTOMETER:GIGAMETER"] = \
        Mul(Pow(10, 27), Sym("attom"))
    CONVERSIONS["ATTOMETER:HECTOMETER"] = \
        Mul(Pow(10, 20), Sym("attom"))
    CONVERSIONS["ATTOMETER:INCH"] = \
        Mul(Mul(Int(254), Pow(10, 14)), Sym("attom"))
    CONVERSIONS["ATTOMETER:KILOMETER"] = \
        Mul(Pow(10, 21), Sym("attom"))
    CONVERSIONS["ATTOMETER:LIGHTYEAR"] = \
        Mul(Mul(Int("94607304725808"), Pow(10, 20)), Sym("attom"))
    CONVERSIONS["ATTOMETER:LINE"] = \
        Mul(Rat(Mul(Int(635), Pow(10, 13)), Int(3)), Sym("attom"))
    CONVERSIONS["ATTOMETER:MEGAMETER"] = \
        Mul(Pow(10, 24), Sym("attom"))
    CONVERSIONS["ATTOMETER:METER"] = \
        Mul(Pow(10, 18), Sym("attom"))
    CONVERSIONS["ATTOMETER:MICROMETER"] = \
        Mul(Pow(10, 12), Sym("attom"))
    CONVERSIONS["ATTOMETER:MILE"] = \
        Mul(Mul(Int(1609344), Pow(10, 15)), Sym("attom"))
    CONVERSIONS["ATTOMETER:MILLIMETER"] = \
        Mul(Pow(10, 15), Sym("attom"))
    CONVERSIONS["ATTOMETER:NANOMETER"] = \
        Mul(Pow(10, 9), Sym("attom"))
    CONVERSIONS["ATTOMETER:PARSEC"] = \
        Mul(Mul(Int(30856776), Pow(10, 27)), Sym("attom"))
    CONVERSIONS["ATTOMETER:PETAMETER"] = \
        Mul(Pow(10, 33), Sym("attom"))
    CONVERSIONS["ATTOMETER:PICOMETER"] = \
        Mul(Pow(10, 6), Sym("attom"))
    CONVERSIONS["ATTOMETER:POINT"] = \
        Mul(Rat(Mul(Int(3175), Pow(10, 12)), Int(9)), Sym("attom"))
    CONVERSIONS["ATTOMETER:TERAMETER"] = \
        Mul(Pow(10, 30), Sym("attom"))
    CONVERSIONS["ATTOMETER:THOU"] = \
        Mul(Mul(Int(254), Pow(10, 11)), Sym("attom"))
    CONVERSIONS["ATTOMETER:YARD"] = \
        Mul(Mul(Int(9144), Pow(10, 14)), Sym("attom"))
    CONVERSIONS["ATTOMETER:YOCTOMETER"] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("attom"))
    CONVERSIONS["ATTOMETER:YOTTAMETER"] = \
        Mul(Pow(10, 42), Sym("attom"))
    CONVERSIONS["ATTOMETER:ZEPTOMETER"] = \
        Mul(Rat(Int(1), Int(1000)), Sym("attom"))
    CONVERSIONS["ATTOMETER:ZETTAMETER"] = \
        Mul(Pow(10, 39), Sym("attom"))
    CONVERSIONS["CENTIMETER:ANGSTROM"] = \
        Mul(Rat(Int(1), Pow(10, 8)), Sym("centim"))
    CONVERSIONS["CENTIMETER:ASTRONMICALUNIT"] = \
        Mul(Mul(Int(1495978707), Pow(10, 4)), Sym("centim"))
    CONVERSIONS["CENTIMETER:ATTOMETER"] = \
        Mul(Rat(Int(1), Pow(10, 16)), Sym("centim"))
    CONVERSIONS["CENTIMETER:DECAMETER"] = \
        Mul(Int(1000), Sym("centim"))
    CONVERSIONS["CENTIMETER:DECIMETER"] = \
        Mul(Int(10), Sym("centim"))
    CONVERSIONS["CENTIMETER:EXAMETER"] = \
        Mul(Pow(10, 20), Sym("centim"))
    CONVERSIONS["CENTIMETER:FEMTOMETER"] = \
        Mul(Rat(Int(1), Pow(10, 13)), Sym("centim"))
    CONVERSIONS["CENTIMETER:FOOT"] = \
        Mul(Rat(Int(762), Int(25)), Sym("centim"))
    CONVERSIONS["CENTIMETER:GIGAMETER"] = \
        Mul(Pow(10, 11), Sym("centim"))
    CONVERSIONS["CENTIMETER:HECTOMETER"] = \
        Mul(Pow(10, 4), Sym("centim"))
    CONVERSIONS["CENTIMETER:INCH"] = \
        Mul(Rat(Int(127), Int(50)), Sym("centim"))
    CONVERSIONS["CENTIMETER:KILOMETER"] = \
        Mul(Pow(10, 5), Sym("centim"))
    CONVERSIONS["CENTIMETER:LIGHTYEAR"] = \
        Mul(Mul(Int("94607304725808"), Pow(10, 4)), Sym("centim"))
    CONVERSIONS["CENTIMETER:LINE"] = \
        Mul(Rat(Int(127), Int(600)), Sym("centim"))
    CONVERSIONS["CENTIMETER:MEGAMETER"] = \
        Mul(Pow(10, 8), Sym("centim"))
    CONVERSIONS["CENTIMETER:METER"] = \
        Mul(Int(100), Sym("centim"))
    CONVERSIONS["CENTIMETER:MICROMETER"] = \
        Mul(Rat(Int(1), Pow(10, 4)), Sym("centim"))
    CONVERSIONS["CENTIMETER:MILE"] = \
        Mul(Rat(Int(804672), Int(5)), Sym("centim"))
    CONVERSIONS["CENTIMETER:MILLIMETER"] = \
        Mul(Rat(Int(1), Int(10)), Sym("centim"))
    CONVERSIONS["CENTIMETER:NANOMETER"] = \
        Mul(Rat(Int(1), Pow(10, 7)), Sym("centim"))
    CONVERSIONS["CENTIMETER:PARSEC"] = \
        Mul(Mul(Int(30856776), Pow(10, 11)), Sym("centim"))
    CONVERSIONS["CENTIMETER:PETAMETER"] = \
        Mul(Pow(10, 17), Sym("centim"))
    CONVERSIONS["CENTIMETER:PICOMETER"] = \
        Mul(Rat(Int(1), Pow(10, 10)), Sym("centim"))
    CONVERSIONS["CENTIMETER:POINT"] = \
        Mul(Rat(Int(127), Int(3600)), Sym("centim"))
    CONVERSIONS["CENTIMETER:TERAMETER"] = \
        Mul(Pow(10, 14), Sym("centim"))
    CONVERSIONS["CENTIMETER:THOU"] = \
        Mul(Rat(Int(127), Mul(Int(5), Pow(10, 4))), Sym("centim"))
    CONVERSIONS["CENTIMETER:YARD"] = \
        Mul(Rat(Int(2286), Int(25)), Sym("centim"))
    CONVERSIONS["CENTIMETER:YOCTOMETER"] = \
        Mul(Rat(Int(1), Pow(10, 22)), Sym("centim"))
    CONVERSIONS["CENTIMETER:YOTTAMETER"] = \
        Mul(Pow(10, 26), Sym("centim"))
    CONVERSIONS["CENTIMETER:ZEPTOMETER"] = \
        Mul(Rat(Int(1), Pow(10, 19)), Sym("centim"))
    CONVERSIONS["CENTIMETER:ZETTAMETER"] = \
        Mul(Pow(10, 23), Sym("centim"))
    CONVERSIONS["DECAMETER:ANGSTROM"] = \
        Mul(Rat(Int(1), Pow(10, 11)), Sym("decam"))
    CONVERSIONS["DECAMETER:ASTRONMICALUNIT"] = \
        Mul(Int("14959787070"), Sym("decam"))
    CONVERSIONS["DECAMETER:ATTOMETER"] = \
        Mul(Rat(Int(1), Pow(10, 19)), Sym("decam"))
    CONVERSIONS["DECAMETER:CENTIMETER"] = \
        Mul(Rat(Int(1), Int(1000)), Sym("decam"))
    CONVERSIONS["DECAMETER:DECIMETER"] = \
        Mul(Rat(Int(1), Int(100)), Sym("decam"))
    CONVERSIONS["DECAMETER:EXAMETER"] = \
        Mul(Pow(10, 17), Sym("decam"))
    CONVERSIONS["DECAMETER:FEMTOMETER"] = \
        Mul(Rat(Int(1), Pow(10, 16)), Sym("decam"))
    CONVERSIONS["DECAMETER:FOOT"] = \
        Mul(Rat(Int(381), Int(12500)), Sym("decam"))
    CONVERSIONS["DECAMETER:GIGAMETER"] = \
        Mul(Pow(10, 8), Sym("decam"))
    CONVERSIONS["DECAMETER:HECTOMETER"] = \
        Mul(Int(10), Sym("decam"))
    CONVERSIONS["DECAMETER:INCH"] = \
        Mul(Rat(Int(127), Mul(Int(5), Pow(10, 4))), Sym("decam"))
    CONVERSIONS["DECAMETER:KILOMETER"] = \
        Mul(Int(100), Sym("decam"))
    CONVERSIONS["DECAMETER:LIGHTYEAR"] = \
        Mul(Int("946073047258080"), Sym("decam"))
    CONVERSIONS["DECAMETER:LINE"] = \
        Mul(Rat(Int(127), Mul(Int(6), Pow(10, 5))), Sym("decam"))
    CONVERSIONS["DECAMETER:MEGAMETER"] = \
        Mul(Pow(10, 5), Sym("decam"))
    CONVERSIONS["DECAMETER:METER"] = \
        Mul(Rat(Int(1), Int(10)), Sym("decam"))
    CONVERSIONS["DECAMETER:MICROMETER"] = \
        Mul(Rat(Int(1), Pow(10, 7)), Sym("decam"))
    CONVERSIONS["DECAMETER:MILE"] = \
        Mul(Rat(Int(100584), Int(625)), Sym("decam"))
    CONVERSIONS["DECAMETER:MILLIMETER"] = \
        Mul(Rat(Int(1), Pow(10, 4)), Sym("decam"))
    CONVERSIONS["DECAMETER:NANOMETER"] = \
        Mul(Rat(Int(1), Pow(10, 10)), Sym("decam"))
    CONVERSIONS["DECAMETER:PARSEC"] = \
        Mul(Mul(Int(30856776), Pow(10, 8)), Sym("decam"))
    CONVERSIONS["DECAMETER:PETAMETER"] = \
        Mul(Pow(10, 14), Sym("decam"))
    CONVERSIONS["DECAMETER:PICOMETER"] = \
        Mul(Rat(Int(1), Pow(10, 13)), Sym("decam"))
    CONVERSIONS["DECAMETER:POINT"] = \
        Mul(Rat(Int(127), Mul(Int(36), Pow(10, 5))), Sym("decam"))
    CONVERSIONS["DECAMETER:TERAMETER"] = \
        Mul(Pow(10, 11), Sym("decam"))
    CONVERSIONS["DECAMETER:THOU"] = \
        Mul(Rat(Int(127), Mul(Int(5), Pow(10, 7))), Sym("decam"))
    CONVERSIONS["DECAMETER:YARD"] = \
        Mul(Rat(Int(1143), Int(12500)), Sym("decam"))
    CONVERSIONS["DECAMETER:YOCTOMETER"] = \
        Mul(Rat(Int(1), Pow(10, 25)), Sym("decam"))
    CONVERSIONS["DECAMETER:YOTTAMETER"] = \
        Mul(Pow(10, 23), Sym("decam"))
    CONVERSIONS["DECAMETER:ZEPTOMETER"] = \
        Mul(Rat(Int(1), Pow(10, 22)), Sym("decam"))
    CONVERSIONS["DECAMETER:ZETTAMETER"] = \
        Mul(Pow(10, 20), Sym("decam"))
    CONVERSIONS["DECIMETER:ANGSTROM"] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("decim"))
    CONVERSIONS["DECIMETER:ASTRONMICALUNIT"] = \
        Mul(Int("1495978707000"), Sym("decim"))
    CONVERSIONS["DECIMETER:ATTOMETER"] = \
        Mul(Rat(Int(1), Pow(10, 17)), Sym("decim"))
    CONVERSIONS["DECIMETER:CENTIMETER"] = \
        Mul(Rat(Int(1), Int(10)), Sym("decim"))
    CONVERSIONS["DECIMETER:DECAMETER"] = \
        Mul(Int(100), Sym("decim"))
    CONVERSIONS["DECIMETER:EXAMETER"] = \
        Mul(Pow(10, 19), Sym("decim"))
    CONVERSIONS["DECIMETER:FEMTOMETER"] = \
        Mul(Rat(Int(1), Pow(10, 14)), Sym("decim"))
    CONVERSIONS["DECIMETER:FOOT"] = \
        Mul(Rat(Int(381), Int(125)), Sym("decim"))
    CONVERSIONS["DECIMETER:GIGAMETER"] = \
        Mul(Pow(10, 10), Sym("decim"))
    CONVERSIONS["DECIMETER:HECTOMETER"] = \
        Mul(Int(1000), Sym("decim"))
    CONVERSIONS["DECIMETER:INCH"] = \
        Mul(Rat(Int(127), Int(500)), Sym("decim"))
    CONVERSIONS["DECIMETER:KILOMETER"] = \
        Mul(Pow(10, 4), Sym("decim"))
    CONVERSIONS["DECIMETER:LIGHTYEAR"] = \
        Mul(Int("94607304725808000"), Sym("decim"))
    CONVERSIONS["DECIMETER:LINE"] = \
        Mul(Rat(Int(127), Int(6000)), Sym("decim"))
    CONVERSIONS["DECIMETER:MEGAMETER"] = \
        Mul(Pow(10, 7), Sym("decim"))
    CONVERSIONS["DECIMETER:METER"] = \
        Mul(Int(10), Sym("decim"))
    CONVERSIONS["DECIMETER:MICROMETER"] = \
        Mul(Rat(Int(1), Pow(10, 5)), Sym("decim"))
    CONVERSIONS["DECIMETER:MILE"] = \
        Mul(Rat(Int(402336), Int(25)), Sym("decim"))
    CONVERSIONS["DECIMETER:MILLIMETER"] = \
        Mul(Rat(Int(1), Int(100)), Sym("decim"))
    CONVERSIONS["DECIMETER:NANOMETER"] = \
        Mul(Rat(Int(1), Pow(10, 8)), Sym("decim"))
    CONVERSIONS["DECIMETER:PARSEC"] = \
        Mul(Mul(Int(30856776), Pow(10, 10)), Sym("decim"))
    CONVERSIONS["DECIMETER:PETAMETER"] = \
        Mul(Pow(10, 16), Sym("decim"))
    CONVERSIONS["DECIMETER:PICOMETER"] = \
        Mul(Rat(Int(1), Pow(10, 11)), Sym("decim"))
    CONVERSIONS["DECIMETER:POINT"] = \
        Mul(Rat(Int(127), Int(36000)), Sym("decim"))
    CONVERSIONS["DECIMETER:TERAMETER"] = \
        Mul(Pow(10, 13), Sym("decim"))
    CONVERSIONS["DECIMETER:THOU"] = \
        Mul(Rat(Int(127), Mul(Int(5), Pow(10, 5))), Sym("decim"))
    CONVERSIONS["DECIMETER:YARD"] = \
        Mul(Rat(Int(1143), Int(125)), Sym("decim"))
    CONVERSIONS["DECIMETER:YOCTOMETER"] = \
        Mul(Rat(Int(1), Pow(10, 23)), Sym("decim"))
    CONVERSIONS["DECIMETER:YOTTAMETER"] = \
        Mul(Pow(10, 25), Sym("decim"))
    CONVERSIONS["DECIMETER:ZEPTOMETER"] = \
        Mul(Rat(Int(1), Pow(10, 20)), Sym("decim"))
    CONVERSIONS["DECIMETER:ZETTAMETER"] = \
        Mul(Pow(10, 22), Sym("decim"))
    CONVERSIONS["EXAMETER:ANGSTROM"] = \
        Mul(Rat(Int(1), Pow(10, 28)), Sym("exam"))
    CONVERSIONS["EXAMETER:ASTRONMICALUNIT"] = \
        Mul(Rat(Int(1495978707), Pow(10, 16)), Sym("exam"))
    CONVERSIONS["EXAMETER:ATTOMETER"] = \
        Mul(Rat(Int(1), Pow(10, 36)), Sym("exam"))
    CONVERSIONS["EXAMETER:CENTIMETER"] = \
        Mul(Rat(Int(1), Pow(10, 20)), Sym("exam"))
    CONVERSIONS["EXAMETER:DECAMETER"] = \
        Mul(Rat(Int(1), Pow(10, 17)), Sym("exam"))
    CONVERSIONS["EXAMETER:DECIMETER"] = \
        Mul(Rat(Int(1), Pow(10, 19)), Sym("exam"))
    CONVERSIONS["EXAMETER:FEMTOMETER"] = \
        Mul(Rat(Int(1), Pow(10, 33)), Sym("exam"))
    CONVERSIONS["EXAMETER:FOOT"] = \
        Mul(Rat(Int(381), Mul(Int(125), Pow(10, 19))), Sym("exam"))
    CONVERSIONS["EXAMETER:GIGAMETER"] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("exam"))
    CONVERSIONS["EXAMETER:HECTOMETER"] = \
        Mul(Rat(Int(1), Pow(10, 16)), Sym("exam"))
    CONVERSIONS["EXAMETER:INCH"] = \
        Mul(Rat(Int(127), Mul(Int(5), Pow(10, 21))), Sym("exam"))
    CONVERSIONS["EXAMETER:KILOMETER"] = \
        Mul(Rat(Int(1), Pow(10, 15)), Sym("exam"))
    CONVERSIONS["EXAMETER:LIGHTYEAR"] = \
        Mul(Rat(Int("5912956545363"), Mul(Int(625), Pow(10, 12))), Sym("exam"))
    CONVERSIONS["EXAMETER:LINE"] = \
        Mul(Rat(Int(127), Mul(Int(6), Pow(10, 22))), Sym("exam"))
    CONVERSIONS["EXAMETER:MEGAMETER"] = \
        Mul(Rat(Int(1), Pow(10, 12)), Sym("exam"))
    CONVERSIONS["EXAMETER:METER"] = \
        Mul(Rat(Int(1), Pow(10, 18)), Sym("exam"))
    CONVERSIONS["EXAMETER:MICROMETER"] = \
        Mul(Rat(Int(1), Pow(10, 24)), Sym("exam"))
    CONVERSIONS["EXAMETER:MILE"] = \
        Mul(Rat(Int(12573), Mul(Int(78125), Pow(10, 14))), Sym("exam"))
    CONVERSIONS["EXAMETER:MILLIMETER"] = \
        Mul(Rat(Int(1), Pow(10, 21)), Sym("exam"))
    CONVERSIONS["EXAMETER:NANOMETER"] = \
        Mul(Rat(Int(1), Pow(10, 27)), Sym("exam"))
    CONVERSIONS["EXAMETER:PARSEC"] = \
        Mul(Rat(Int(3857097), Mul(Int(125), Pow(10, 6))), Sym("exam"))
    CONVERSIONS["EXAMETER:PETAMETER"] = \
        Mul(Rat(Int(1), Int(1000)), Sym("exam"))
    CONVERSIONS["EXAMETER:PICOMETER"] = \
        Mul(Rat(Int(1), Pow(10, 30)), Sym("exam"))
    CONVERSIONS["EXAMETER:POINT"] = \
        Mul(Rat(Int(127), Mul(Int(36), Pow(10, 22))), Sym("exam"))
    CONVERSIONS["EXAMETER:TERAMETER"] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("exam"))
    CONVERSIONS["EXAMETER:THOU"] = \
        Mul(Rat(Int(127), Mul(Int(5), Pow(10, 24))), Sym("exam"))
    CONVERSIONS["EXAMETER:YARD"] = \
        Mul(Rat(Int(1143), Mul(Int(125), Pow(10, 19))), Sym("exam"))
    CONVERSIONS["EXAMETER:YOCTOMETER"] = \
        Mul(Rat(Int(1), Pow(10, 42)), Sym("exam"))
    CONVERSIONS["EXAMETER:YOTTAMETER"] = \
        Mul(Pow(10, 6), Sym("exam"))
    CONVERSIONS["EXAMETER:ZEPTOMETER"] = \
        Mul(Rat(Int(1), Pow(10, 39)), Sym("exam"))
    CONVERSIONS["EXAMETER:ZETTAMETER"] = \
        Mul(Int(1000), Sym("exam"))
    CONVERSIONS["FEMTOMETER:ANGSTROM"] = \
        Mul(Pow(10, 5), Sym("femtom"))
    CONVERSIONS["FEMTOMETER:ASTRONMICALUNIT"] = \
        Mul(Mul(Int(1495978707), Pow(10, 17)), Sym("femtom"))
    CONVERSIONS["FEMTOMETER:ATTOMETER"] = \
        Mul(Rat(Int(1), Int(1000)), Sym("femtom"))
    CONVERSIONS["FEMTOMETER:CENTIMETER"] = \
        Mul(Pow(10, 13), Sym("femtom"))
    CONVERSIONS["FEMTOMETER:DECAMETER"] = \
        Mul(Pow(10, 16), Sym("femtom"))
    CONVERSIONS["FEMTOMETER:DECIMETER"] = \
        Mul(Pow(10, 14), Sym("femtom"))
    CONVERSIONS["FEMTOMETER:EXAMETER"] = \
        Mul(Pow(10, 33), Sym("femtom"))
    CONVERSIONS["FEMTOMETER:FOOT"] = \
        Mul(Mul(Int(3048), Pow(10, 11)), Sym("femtom"))
    CONVERSIONS["FEMTOMETER:GIGAMETER"] = \
        Mul(Pow(10, 24), Sym("femtom"))
    CONVERSIONS["FEMTOMETER:HECTOMETER"] = \
        Mul(Pow(10, 17), Sym("femtom"))
    CONVERSIONS["FEMTOMETER:INCH"] = \
        Mul(Mul(Int(254), Pow(10, 11)), Sym("femtom"))
    CONVERSIONS["FEMTOMETER:KILOMETER"] = \
        Mul(Pow(10, 18), Sym("femtom"))
    CONVERSIONS["FEMTOMETER:LIGHTYEAR"] = \
        Mul(Mul(Int("94607304725808"), Pow(10, 17)), Sym("femtom"))
    CONVERSIONS["FEMTOMETER:LINE"] = \
        Mul(Rat(Mul(Int(635), Pow(10, 10)), Int(3)), Sym("femtom"))
    CONVERSIONS["FEMTOMETER:MEGAMETER"] = \
        Mul(Pow(10, 21), Sym("femtom"))
    CONVERSIONS["FEMTOMETER:METER"] = \
        Mul(Pow(10, 15), Sym("femtom"))
    CONVERSIONS["FEMTOMETER:MICROMETER"] = \
        Mul(Pow(10, 9), Sym("femtom"))
    CONVERSIONS["FEMTOMETER:MILE"] = \
        Mul(Mul(Int(1609344), Pow(10, 12)), Sym("femtom"))
    CONVERSIONS["FEMTOMETER:MILLIMETER"] = \
        Mul(Pow(10, 12), Sym("femtom"))
    CONVERSIONS["FEMTOMETER:NANOMETER"] = \
        Mul(Pow(10, 6), Sym("femtom"))
    CONVERSIONS["FEMTOMETER:PARSEC"] = \
        Mul(Mul(Int(30856776), Pow(10, 24)), Sym("femtom"))
    CONVERSIONS["FEMTOMETER:PETAMETER"] = \
        Mul(Pow(10, 30), Sym("femtom"))
    CONVERSIONS["FEMTOMETER:PICOMETER"] = \
        Mul(Int(1000), Sym("femtom"))
    CONVERSIONS["FEMTOMETER:POINT"] = \
        Mul(Rat(Mul(Int(3175), Pow(10, 9)), Int(9)), Sym("femtom"))
    CONVERSIONS["FEMTOMETER:TERAMETER"] = \
        Mul(Pow(10, 27), Sym("femtom"))
    CONVERSIONS["FEMTOMETER:THOU"] = \
        Mul(Mul(Int(254), Pow(10, 8)), Sym("femtom"))
    CONVERSIONS["FEMTOMETER:YARD"] = \
        Mul(Mul(Int(9144), Pow(10, 11)), Sym("femtom"))
    CONVERSIONS["FEMTOMETER:YOCTOMETER"] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("femtom"))
    CONVERSIONS["FEMTOMETER:YOTTAMETER"] = \
        Mul(Pow(10, 39), Sym("femtom"))
    CONVERSIONS["FEMTOMETER:ZEPTOMETER"] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("femtom"))
    CONVERSIONS["FEMTOMETER:ZETTAMETER"] = \
        Mul(Pow(10, 36), Sym("femtom"))
    CONVERSIONS["FOOT:ANGSTROM"] = \
        Mul(Rat(Int(1), Mul(Int(3048), Pow(10, 6))), Sym("ft"))
    CONVERSIONS["FOOT:ASTRONMICALUNIT"] = \
        Mul(Rat(Int("62332446125000"), Int(127)), Sym("ft"))
    CONVERSIONS["FOOT:ATTOMETER"] = \
        Mul(Rat(Int(1), Mul(Int(3048), Pow(10, 14))), Sym("ft"))
    CONVERSIONS["FOOT:CENTIMETER"] = \
        Mul(Rat(Int(25), Int(762)), Sym("ft"))
    CONVERSIONS["FOOT:DECAMETER"] = \
        Mul(Rat(Int(12500), Int(381)), Sym("ft"))
    CONVERSIONS["FOOT:DECIMETER"] = \
        Mul(Rat(Int(125), Int(381)), Sym("ft"))
    CONVERSIONS["FOOT:EXAMETER"] = \
        Mul(Rat(Mul(Int(125), Pow(10, 19)), Int(381)), Sym("ft"))
    CONVERSIONS["FOOT:FEMTOMETER"] = \
        Mul(Rat(Int(1), Mul(Int(3048), Pow(10, 11))), Sym("ft"))
    CONVERSIONS["FOOT:GIGAMETER"] = \
        Mul(Rat(Mul(Int(125), Pow(10, 10)), Int(381)), Sym("ft"))
    CONVERSIONS["FOOT:HECTOMETER"] = \
        Mul(Rat(Int(125000), Int(381)), Sym("ft"))
    CONVERSIONS["FOOT:INCH"] = \
        Mul(Rat(Int(1), Int(12)), Sym("ft"))
    CONVERSIONS["FOOT:KILOMETER"] = \
        Mul(Rat(Mul(Int(125), Pow(10, 4)), Int(381)), Sym("ft"))
    CONVERSIONS["FOOT:LIGHTYEAR"] = \
        Mul(Rat(Mul(Int("3941971030242"), Pow(10, 6)), Int(127)), Sym("ft"))
    CONVERSIONS["FOOT:LINE"] = \
        Mul(Rat(Int(1), Int(144)), Sym("ft"))
    CONVERSIONS["FOOT:MEGAMETER"] = \
        Mul(Rat(Mul(Int(125), Pow(10, 7)), Int(381)), Sym("ft"))
    CONVERSIONS["FOOT:METER"] = \
        Mul(Rat(Int(1250), Int(381)), Sym("ft"))
    CONVERSIONS["FOOT:MICROMETER"] = \
        Mul(Rat(Int(1), Int(304800)), Sym("ft"))
    CONVERSIONS["FOOT:MILE"] = \
        Mul(Int(5280), Sym("ft"))
    CONVERSIONS["FOOT:MILLIMETER"] = \
        Mul(Rat(Int(5), Int(1524)), Sym("ft"))
    CONVERSIONS["FOOT:NANOMETER"] = \
        Mul(Rat(Int(1), Mul(Int(3048), Pow(10, 5))), Sym("ft"))
    CONVERSIONS["FOOT:PARSEC"] = \
        Mul(Rat(Mul(Int(1285699), Pow(10, 13)), Int(127)), Sym("ft"))
    CONVERSIONS["FOOT:PETAMETER"] = \
        Mul(Rat(Mul(Int(125), Pow(10, 16)), Int(381)), Sym("ft"))
    CONVERSIONS["FOOT:PICOMETER"] = \
        Mul(Rat(Int(1), Mul(Int(3048), Pow(10, 8))), Sym("ft"))
    CONVERSIONS["FOOT:POINT"] = \
        Mul(Rat(Int(1), Int(864)), Sym("ft"))
    CONVERSIONS["FOOT:TERAMETER"] = \
        Mul(Rat(Mul(Int(125), Pow(10, 13)), Int(381)), Sym("ft"))
    CONVERSIONS["FOOT:THOU"] = \
        Mul(Rat(Int(1), Int(12000)), Sym("ft"))
    CONVERSIONS["FOOT:YARD"] = \
        Mul(Int(3), Sym("ft"))
    CONVERSIONS["FOOT:YOCTOMETER"] = \
        Mul(Rat(Int(1), Mul(Int(3048), Pow(10, 20))), Sym("ft"))
    CONVERSIONS["FOOT:YOTTAMETER"] = \
        Mul(Rat(Mul(Int(125), Pow(10, 25)), Int(381)), Sym("ft"))
    CONVERSIONS["FOOT:ZEPTOMETER"] = \
        Mul(Rat(Int(1), Mul(Int(3048), Pow(10, 17))), Sym("ft"))
    CONVERSIONS["FOOT:ZETTAMETER"] = \
        Mul(Rat(Mul(Int(125), Pow(10, 22)), Int(381)), Sym("ft"))
    CONVERSIONS["GIGAMETER:ANGSTROM"] = \
        Mul(Rat(Int(1), Pow(10, 19)), Sym("gigam"))
    CONVERSIONS["GIGAMETER:ASTRONMICALUNIT"] = \
        Mul(Rat(Int(1495978707), Pow(10, 7)), Sym("gigam"))
    CONVERSIONS["GIGAMETER:ATTOMETER"] = \
        Mul(Rat(Int(1), Pow(10, 27)), Sym("gigam"))
    CONVERSIONS["GIGAMETER:CENTIMETER"] = \
        Mul(Rat(Int(1), Pow(10, 11)), Sym("gigam"))
    CONVERSIONS["GIGAMETER:DECAMETER"] = \
        Mul(Rat(Int(1), Pow(10, 8)), Sym("gigam"))
    CONVERSIONS["GIGAMETER:DECIMETER"] = \
        Mul(Rat(Int(1), Pow(10, 10)), Sym("gigam"))
    CONVERSIONS["GIGAMETER:EXAMETER"] = \
        Mul(Pow(10, 9), Sym("gigam"))
    CONVERSIONS["GIGAMETER:FEMTOMETER"] = \
        Mul(Rat(Int(1), Pow(10, 24)), Sym("gigam"))
    CONVERSIONS["GIGAMETER:FOOT"] = \
        Mul(Rat(Int(381), Mul(Int(125), Pow(10, 10))), Sym("gigam"))
    CONVERSIONS["GIGAMETER:HECTOMETER"] = \
        Mul(Rat(Int(1), Pow(10, 7)), Sym("gigam"))
    CONVERSIONS["GIGAMETER:INCH"] = \
        Mul(Rat(Int(127), Mul(Int(5), Pow(10, 12))), Sym("gigam"))
    CONVERSIONS["GIGAMETER:KILOMETER"] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("gigam"))
    CONVERSIONS["GIGAMETER:LIGHTYEAR"] = \
        Mul(Rat(Int("5912956545363"), Int(625000)), Sym("gigam"))
    CONVERSIONS["GIGAMETER:LINE"] = \
        Mul(Rat(Int(127), Mul(Int(6), Pow(10, 13))), Sym("gigam"))
    CONVERSIONS["GIGAMETER:MEGAMETER"] = \
        Mul(Rat(Int(1), Int(1000)), Sym("gigam"))
    CONVERSIONS["GIGAMETER:METER"] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("gigam"))
    CONVERSIONS["GIGAMETER:MICROMETER"] = \
        Mul(Rat(Int(1), Pow(10, 15)), Sym("gigam"))
    CONVERSIONS["GIGAMETER:MILE"] = \
        Mul(Rat(Int(12573), Mul(Int(78125), Pow(10, 5))), Sym("gigam"))
    CONVERSIONS["GIGAMETER:MILLIMETER"] = \
        Mul(Rat(Int(1), Pow(10, 12)), Sym("gigam"))
    CONVERSIONS["GIGAMETER:NANOMETER"] = \
        Mul(Rat(Int(1), Pow(10, 18)), Sym("gigam"))
    CONVERSIONS["GIGAMETER:PARSEC"] = \
        Mul(Int(30856776), Sym("gigam"))
    CONVERSIONS["GIGAMETER:PETAMETER"] = \
        Mul(Pow(10, 6), Sym("gigam"))
    CONVERSIONS["GIGAMETER:PICOMETER"] = \
        Mul(Rat(Int(1), Pow(10, 21)), Sym("gigam"))
    CONVERSIONS["GIGAMETER:POINT"] = \
        Mul(Rat(Int(127), Mul(Int(36), Pow(10, 13))), Sym("gigam"))
    CONVERSIONS["GIGAMETER:TERAMETER"] = \
        Mul(Int(1000), Sym("gigam"))
    CONVERSIONS["GIGAMETER:THOU"] = \
        Mul(Rat(Int(127), Mul(Int(5), Pow(10, 15))), Sym("gigam"))
    CONVERSIONS["GIGAMETER:YARD"] = \
        Mul(Rat(Int(1143), Mul(Int(125), Pow(10, 10))), Sym("gigam"))
    CONVERSIONS["GIGAMETER:YOCTOMETER"] = \
        Mul(Rat(Int(1), Pow(10, 33)), Sym("gigam"))
    CONVERSIONS["GIGAMETER:YOTTAMETER"] = \
        Mul(Pow(10, 15), Sym("gigam"))
    CONVERSIONS["GIGAMETER:ZEPTOMETER"] = \
        Mul(Rat(Int(1), Pow(10, 30)), Sym("gigam"))
    CONVERSIONS["GIGAMETER:ZETTAMETER"] = \
        Mul(Pow(10, 12), Sym("gigam"))
    CONVERSIONS["HECTOMETER:ANGSTROM"] = \
        Mul(Rat(Int(1), Pow(10, 12)), Sym("hectom"))
    CONVERSIONS["HECTOMETER:ASTRONMICALUNIT"] = \
        Mul(Int(1495978707), Sym("hectom"))
    CONVERSIONS["HECTOMETER:ATTOMETER"] = \
        Mul(Rat(Int(1), Pow(10, 20)), Sym("hectom"))
    CONVERSIONS["HECTOMETER:CENTIMETER"] = \
        Mul(Rat(Int(1), Pow(10, 4)), Sym("hectom"))
    CONVERSIONS["HECTOMETER:DECAMETER"] = \
        Mul(Rat(Int(1), Int(10)), Sym("hectom"))
    CONVERSIONS["HECTOMETER:DECIMETER"] = \
        Mul(Rat(Int(1), Int(1000)), Sym("hectom"))
    CONVERSIONS["HECTOMETER:EXAMETER"] = \
        Mul(Pow(10, 16), Sym("hectom"))
    CONVERSIONS["HECTOMETER:FEMTOMETER"] = \
        Mul(Rat(Int(1), Pow(10, 17)), Sym("hectom"))
    CONVERSIONS["HECTOMETER:FOOT"] = \
        Mul(Rat(Int(381), Int(125000)), Sym("hectom"))
    CONVERSIONS["HECTOMETER:GIGAMETER"] = \
        Mul(Pow(10, 7), Sym("hectom"))
    CONVERSIONS["HECTOMETER:INCH"] = \
        Mul(Rat(Int(127), Mul(Int(5), Pow(10, 5))), Sym("hectom"))
    CONVERSIONS["HECTOMETER:KILOMETER"] = \
        Mul(Int(10), Sym("hectom"))
    CONVERSIONS["HECTOMETER:LIGHTYEAR"] = \
        Mul(Int("94607304725808"), Sym("hectom"))
    CONVERSIONS["HECTOMETER:LINE"] = \
        Mul(Rat(Int(127), Mul(Int(6), Pow(10, 6))), Sym("hectom"))
    CONVERSIONS["HECTOMETER:MEGAMETER"] = \
        Mul(Pow(10, 4), Sym("hectom"))
    CONVERSIONS["HECTOMETER:METER"] = \
        Mul(Rat(Int(1), Int(100)), Sym("hectom"))
    CONVERSIONS["HECTOMETER:MICROMETER"] = \
        Mul(Rat(Int(1), Pow(10, 8)), Sym("hectom"))
    CONVERSIONS["HECTOMETER:MILE"] = \
        Mul(Rat(Int(50292), Int(3125)), Sym("hectom"))
    CONVERSIONS["HECTOMETER:MILLIMETER"] = \
        Mul(Rat(Int(1), Pow(10, 5)), Sym("hectom"))
    CONVERSIONS["HECTOMETER:NANOMETER"] = \
        Mul(Rat(Int(1), Pow(10, 11)), Sym("hectom"))
    CONVERSIONS["HECTOMETER:PARSEC"] = \
        Mul(Mul(Int(30856776), Pow(10, 7)), Sym("hectom"))
    CONVERSIONS["HECTOMETER:PETAMETER"] = \
        Mul(Pow(10, 13), Sym("hectom"))
    CONVERSIONS["HECTOMETER:PICOMETER"] = \
        Mul(Rat(Int(1), Pow(10, 14)), Sym("hectom"))
    CONVERSIONS["HECTOMETER:POINT"] = \
        Mul(Rat(Int(127), Mul(Int(36), Pow(10, 6))), Sym("hectom"))
    CONVERSIONS["HECTOMETER:TERAMETER"] = \
        Mul(Pow(10, 10), Sym("hectom"))
    CONVERSIONS["HECTOMETER:THOU"] = \
        Mul(Rat(Int(127), Mul(Int(5), Pow(10, 8))), Sym("hectom"))
    CONVERSIONS["HECTOMETER:YARD"] = \
        Mul(Rat(Int(1143), Int(125000)), Sym("hectom"))
    CONVERSIONS["HECTOMETER:YOCTOMETER"] = \
        Mul(Rat(Int(1), Pow(10, 26)), Sym("hectom"))
    CONVERSIONS["HECTOMETER:YOTTAMETER"] = \
        Mul(Pow(10, 22), Sym("hectom"))
    CONVERSIONS["HECTOMETER:ZEPTOMETER"] = \
        Mul(Rat(Int(1), Pow(10, 23)), Sym("hectom"))
    CONVERSIONS["HECTOMETER:ZETTAMETER"] = \
        Mul(Pow(10, 19), Sym("hectom"))
    CONVERSIONS["INCH:ANGSTROM"] = \
        Mul(Rat(Int(1), Mul(Int(254), Pow(10, 6))), Sym("in"))
    CONVERSIONS["INCH:ASTRONMICALUNIT"] = \
        Mul(Rat(Mul(Int("7479893535"), Pow(10, 5)), Int(127)), Sym("in"))
    CONVERSIONS["INCH:ATTOMETER"] = \
        Mul(Rat(Int(1), Mul(Int(254), Pow(10, 14))), Sym("in"))
    CONVERSIONS["INCH:CENTIMETER"] = \
        Mul(Rat(Int(50), Int(127)), Sym("in"))
    CONVERSIONS["INCH:DECAMETER"] = \
        Mul(Rat(Mul(Int(5), Pow(10, 4)), Int(127)), Sym("in"))
    CONVERSIONS["INCH:DECIMETER"] = \
        Mul(Rat(Int(500), Int(127)), Sym("in"))
    CONVERSIONS["INCH:EXAMETER"] = \
        Mul(Rat(Mul(Int(5), Pow(10, 21)), Int(127)), Sym("in"))
    CONVERSIONS["INCH:FEMTOMETER"] = \
        Mul(Rat(Int(1), Mul(Int(254), Pow(10, 11))), Sym("in"))
    CONVERSIONS["INCH:FOOT"] = \
        Mul(Int(12), Sym("in"))
    CONVERSIONS["INCH:GIGAMETER"] = \
        Mul(Rat(Mul(Int(5), Pow(10, 12)), Int(127)), Sym("in"))
    CONVERSIONS["INCH:HECTOMETER"] = \
        Mul(Rat(Mul(Int(5), Pow(10, 5)), Int(127)), Sym("in"))
    CONVERSIONS["INCH:KILOMETER"] = \
        Mul(Rat(Mul(Int(5), Pow(10, 6)), Int(127)), Sym("in"))
    CONVERSIONS["INCH:LIGHTYEAR"] = \
        Mul(Rat(Mul(Int("47303652362904"), Pow(10, 6)), Int(127)), Sym("in"))
    CONVERSIONS["INCH:LINE"] = \
        Mul(Rat(Int(1), Int(12)), Sym("in"))
    CONVERSIONS["INCH:MEGAMETER"] = \
        Mul(Rat(Mul(Int(5), Pow(10, 9)), Int(127)), Sym("in"))
    CONVERSIONS["INCH:METER"] = \
        Mul(Rat(Int(5000), Int(127)), Sym("in"))
    CONVERSIONS["INCH:MICROMETER"] = \
        Mul(Rat(Int(1), Int(25400)), Sym("in"))
    CONVERSIONS["INCH:MILE"] = \
        Mul(Int(63360), Sym("in"))
    CONVERSIONS["INCH:MILLIMETER"] = \
        Mul(Rat(Int(5), Int(127)), Sym("in"))
    CONVERSIONS["INCH:NANOMETER"] = \
        Mul(Rat(Int(1), Mul(Int(254), Pow(10, 5))), Sym("in"))
    CONVERSIONS["INCH:PARSEC"] = \
        Mul(Rat(Mul(Int(15428388), Pow(10, 13)), Int(127)), Sym("in"))
    CONVERSIONS["INCH:PETAMETER"] = \
        Mul(Rat(Mul(Int(5), Pow(10, 18)), Int(127)), Sym("in"))
    CONVERSIONS["INCH:PICOMETER"] = \
        Mul(Rat(Int(1), Mul(Int(254), Pow(10, 8))), Sym("in"))
    CONVERSIONS["INCH:POINT"] = \
        Mul(Rat(Int(1), Int(72)), Sym("in"))
    CONVERSIONS["INCH:TERAMETER"] = \
        Mul(Rat(Mul(Int(5), Pow(10, 15)), Int(127)), Sym("in"))
    CONVERSIONS["INCH:THOU"] = \
        Mul(Rat(Int(1), Int(1000)), Sym("in"))
    CONVERSIONS["INCH:YARD"] = \
        Mul(Int(36), Sym("in"))
    CONVERSIONS["INCH:YOCTOMETER"] = \
        Mul(Rat(Int(1), Mul(Int(254), Pow(10, 20))), Sym("in"))
    CONVERSIONS["INCH:YOTTAMETER"] = \
        Mul(Rat(Mul(Int(5), Pow(10, 27)), Int(127)), Sym("in"))
    CONVERSIONS["INCH:ZEPTOMETER"] = \
        Mul(Rat(Int(1), Mul(Int(254), Pow(10, 17))), Sym("in"))
    CONVERSIONS["INCH:ZETTAMETER"] = \
        Mul(Rat(Mul(Int(5), Pow(10, 24)), Int(127)), Sym("in"))
    CONVERSIONS["KILOMETER:ANGSTROM"] = \
        Mul(Rat(Int(1), Pow(10, 13)), Sym("kilom"))
    CONVERSIONS["KILOMETER:ASTRONMICALUNIT"] = \
        Mul(Rat(Int(1495978707), Int(10)), Sym("kilom"))
    CONVERSIONS["KILOMETER:ATTOMETER"] = \
        Mul(Rat(Int(1), Pow(10, 21)), Sym("kilom"))
    CONVERSIONS["KILOMETER:CENTIMETER"] = \
        Mul(Rat(Int(1), Pow(10, 5)), Sym("kilom"))
    CONVERSIONS["KILOMETER:DECAMETER"] = \
        Mul(Rat(Int(1), Int(100)), Sym("kilom"))
    CONVERSIONS["KILOMETER:DECIMETER"] = \
        Mul(Rat(Int(1), Pow(10, 4)), Sym("kilom"))
    CONVERSIONS["KILOMETER:EXAMETER"] = \
        Mul(Pow(10, 15), Sym("kilom"))
    CONVERSIONS["KILOMETER:FEMTOMETER"] = \
        Mul(Rat(Int(1), Pow(10, 18)), Sym("kilom"))
    CONVERSIONS["KILOMETER:FOOT"] = \
        Mul(Rat(Int(381), Mul(Int(125), Pow(10, 4))), Sym("kilom"))
    CONVERSIONS["KILOMETER:GIGAMETER"] = \
        Mul(Pow(10, 6), Sym("kilom"))
    CONVERSIONS["KILOMETER:HECTOMETER"] = \
        Mul(Rat(Int(1), Int(10)), Sym("kilom"))
    CONVERSIONS["KILOMETER:INCH"] = \
        Mul(Rat(Int(127), Mul(Int(5), Pow(10, 6))), Sym("kilom"))
    CONVERSIONS["KILOMETER:LIGHTYEAR"] = \
        Mul(Rat(Int("47303652362904"), Int(5)), Sym("kilom"))
    CONVERSIONS["KILOMETER:LINE"] = \
        Mul(Rat(Int(127), Mul(Int(6), Pow(10, 7))), Sym("kilom"))
    CONVERSIONS["KILOMETER:MEGAMETER"] = \
        Mul(Int(1000), Sym("kilom"))
    CONVERSIONS["KILOMETER:METER"] = \
        Mul(Rat(Int(1), Int(1000)), Sym("kilom"))
    CONVERSIONS["KILOMETER:MICROMETER"] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("kilom"))
    CONVERSIONS["KILOMETER:MILE"] = \
        Mul(Rat(Int(25146), Int(15625)), Sym("kilom"))
    CONVERSIONS["KILOMETER:MILLIMETER"] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("kilom"))
    CONVERSIONS["KILOMETER:NANOMETER"] = \
        Mul(Rat(Int(1), Pow(10, 12)), Sym("kilom"))
    CONVERSIONS["KILOMETER:PARSEC"] = \
        Mul(Mul(Int(30856776), Pow(10, 6)), Sym("kilom"))
    CONVERSIONS["KILOMETER:PETAMETER"] = \
        Mul(Pow(10, 12), Sym("kilom"))
    CONVERSIONS["KILOMETER:PICOMETER"] = \
        Mul(Rat(Int(1), Pow(10, 15)), Sym("kilom"))
    CONVERSIONS["KILOMETER:POINT"] = \
        Mul(Rat(Int(127), Mul(Int(36), Pow(10, 7))), Sym("kilom"))
    CONVERSIONS["KILOMETER:TERAMETER"] = \
        Mul(Pow(10, 9), Sym("kilom"))
    CONVERSIONS["KILOMETER:THOU"] = \
        Mul(Rat(Int(127), Mul(Int(5), Pow(10, 9))), Sym("kilom"))
    CONVERSIONS["KILOMETER:YARD"] = \
        Mul(Rat(Int(1143), Mul(Int(125), Pow(10, 4))), Sym("kilom"))
    CONVERSIONS["KILOMETER:YOCTOMETER"] = \
        Mul(Rat(Int(1), Pow(10, 27)), Sym("kilom"))
    CONVERSIONS["KILOMETER:YOTTAMETER"] = \
        Mul(Pow(10, 21), Sym("kilom"))
    CONVERSIONS["KILOMETER:ZEPTOMETER"] = \
        Mul(Rat(Int(1), Pow(10, 24)), Sym("kilom"))
    CONVERSIONS["KILOMETER:ZETTAMETER"] = \
        Mul(Pow(10, 18), Sym("kilom"))
    CONVERSIONS["LIGHTYEAR:ANGSTROM"] = \
        Mul(Rat(Int(1), Mul(Int("94607304725808"), Pow(10, 12))), Sym("ly"))
    CONVERSIONS["LIGHTYEAR:ASTRONMICALUNIT"] = \
        Mul(Rat(Int(6830953), Int("431996825232")), Sym("ly"))
    CONVERSIONS["LIGHTYEAR:ATTOMETER"] = \
        Mul(Rat(Int(1), Mul(Int("94607304725808"), Pow(10, 20))), Sym("ly"))
    CONVERSIONS["LIGHTYEAR:CENTIMETER"] = \
        Mul(Rat(Int(1), Mul(Int("94607304725808"), Pow(10, 4))), Sym("ly"))
    CONVERSIONS["LIGHTYEAR:DECAMETER"] = \
        Mul(Rat(Int(1), Int("946073047258080")), Sym("ly"))
    CONVERSIONS["LIGHTYEAR:DECIMETER"] = \
        Mul(Rat(Int(1), Int("94607304725808000")), Sym("ly"))
    CONVERSIONS["LIGHTYEAR:EXAMETER"] = \
        Mul(Rat(Mul(Int(625), Pow(10, 12)), Int("5912956545363")), Sym("ly"))
    CONVERSIONS["LIGHTYEAR:FEMTOMETER"] = \
        Mul(Rat(Int(1), Mul(Int("94607304725808"), Pow(10, 17))), Sym("ly"))
    CONVERSIONS["LIGHTYEAR:FOOT"] = \
        Mul(Rat(Int(127), Mul(Int("3941971030242"), Pow(10, 6))), Sym("ly"))
    CONVERSIONS["LIGHTYEAR:GIGAMETER"] = \
        Mul(Rat(Int(625000), Int("5912956545363")), Sym("ly"))
    CONVERSIONS["LIGHTYEAR:HECTOMETER"] = \
        Mul(Rat(Int(1), Int("94607304725808")), Sym("ly"))
    CONVERSIONS["LIGHTYEAR:INCH"] = \
        Mul(Rat(Int(127), Mul(Int("47303652362904"), Pow(10, 6))), Sym("ly"))
    CONVERSIONS["LIGHTYEAR:KILOMETER"] = \
        Mul(Rat(Int(5), Int("47303652362904")), Sym("ly"))
    CONVERSIONS["LIGHTYEAR:LINE"] = \
        Mul(Rat(Int(127), Mul(Int("567643828354848"), Pow(10, 6))), Sym("ly"))
    CONVERSIONS["LIGHTYEAR:MEGAMETER"] = \
        Mul(Rat(Int(625), Int("5912956545363")), Sym("ly"))
    CONVERSIONS["LIGHTYEAR:METER"] = \
        Mul(Rat(Int(1), Int("9460730472580800")), Sym("ly"))
    CONVERSIONS["LIGHTYEAR:MICROMETER"] = \
        Mul(Rat(Int(1), Mul(Int("94607304725808"), Pow(10, 8))), Sym("ly"))
    CONVERSIONS["LIGHTYEAR:MILE"] = \
        Mul(Rat(Int(1397), Int("8212439646337500")), Sym("ly"))
    CONVERSIONS["LIGHTYEAR:MILLIMETER"] = \
        Mul(Rat(Int(1), Mul(Int("94607304725808"), Pow(10, 5))), Sym("ly"))
    CONVERSIONS["LIGHTYEAR:NANOMETER"] = \
        Mul(Rat(Int(1), Mul(Int("94607304725808"), Pow(10, 11))), Sym("ly"))
    CONVERSIONS["LIGHTYEAR:PARSEC"] = \
        Mul(Rat(Mul(Int(6428495), Pow(10, 6)), Int("1970985515121")), Sym("ly"))
    CONVERSIONS["LIGHTYEAR:PETAMETER"] = \
        Mul(Rat(Mul(Int(625), Pow(10, 9)), Int("5912956545363")), Sym("ly"))
    CONVERSIONS["LIGHTYEAR:PICOMETER"] = \
        Mul(Rat(Int(1), Mul(Int("94607304725808"), Pow(10, 14))), Sym("ly"))
    CONVERSIONS["LIGHTYEAR:POINT"] = \
        Mul(Rat(Int(127), Mul(Int("3405862970129088"), Pow(10, 6))), Sym("ly"))
    CONVERSIONS["LIGHTYEAR:TERAMETER"] = \
        Mul(Rat(Mul(Int(625), Pow(10, 6)), Int("5912956545363")), Sym("ly"))
    CONVERSIONS["LIGHTYEAR:THOU"] = \
        Mul(Rat(Int(127), Mul(Int("47303652362904"), Pow(10, 9))), Sym("ly"))
    CONVERSIONS["LIGHTYEAR:YARD"] = \
        Mul(Rat(Int(127), Mul(Int("1313990343414"), Pow(10, 6))), Sym("ly"))
    CONVERSIONS["LIGHTYEAR:YOCTOMETER"] = \
        Mul(Rat(Int(1), Mul(Int("94607304725808"), Pow(10, 26))), Sym("ly"))
    CONVERSIONS["LIGHTYEAR:YOTTAMETER"] = \
        Mul(Rat(Mul(Int(625), Pow(10, 18)), Int("5912956545363")), Sym("ly"))
    CONVERSIONS["LIGHTYEAR:ZEPTOMETER"] = \
        Mul(Rat(Int(1), Mul(Int("94607304725808"), Pow(10, 23))), Sym("ly"))
    CONVERSIONS["LIGHTYEAR:ZETTAMETER"] = \
        Mul(Rat(Mul(Int(625), Pow(10, 15)), Int("5912956545363")), Sym("ly"))
    CONVERSIONS["LINE:ANGSTROM"] = \
        Mul(Rat(Int(3), Mul(Int(635), Pow(10, 5))), Sym("li"))
    CONVERSIONS["LINE:ASTRONMICALUNIT"] = \
        Mul(Rat(Mul(Int("8975872242"), Pow(10, 6)), Int(127)), Sym("li"))
    CONVERSIONS["LINE:ATTOMETER"] = \
        Mul(Rat(Int(3), Mul(Int(635), Pow(10, 13))), Sym("li"))
    CONVERSIONS["LINE:CENTIMETER"] = \
        Mul(Rat(Int(600), Int(127)), Sym("li"))
    CONVERSIONS["LINE:DECAMETER"] = \
        Mul(Rat(Mul(Int(6), Pow(10, 5)), Int(127)), Sym("li"))
    CONVERSIONS["LINE:DECIMETER"] = \
        Mul(Rat(Int(6000), Int(127)), Sym("li"))
    CONVERSIONS["LINE:EXAMETER"] = \
        Mul(Rat(Mul(Int(6), Pow(10, 22)), Int(127)), Sym("li"))
    CONVERSIONS["LINE:FEMTOMETER"] = \
        Mul(Rat(Int(3), Mul(Int(635), Pow(10, 10))), Sym("li"))
    CONVERSIONS["LINE:FOOT"] = \
        Mul(Int(144), Sym("li"))
    CONVERSIONS["LINE:GIGAMETER"] = \
        Mul(Rat(Mul(Int(6), Pow(10, 13)), Int(127)), Sym("li"))
    CONVERSIONS["LINE:HECTOMETER"] = \
        Mul(Rat(Mul(Int(6), Pow(10, 6)), Int(127)), Sym("li"))
    CONVERSIONS["LINE:INCH"] = \
        Mul(Int(12), Sym("li"))
    CONVERSIONS["LINE:KILOMETER"] = \
        Mul(Rat(Mul(Int(6), Pow(10, 7)), Int(127)), Sym("li"))
    CONVERSIONS["LINE:LIGHTYEAR"] = \
        Mul(Rat(Mul(Int("567643828354848"), Pow(10, 6)), Int(127)), Sym("li"))
    CONVERSIONS["LINE:MEGAMETER"] = \
        Mul(Rat(Mul(Int(6), Pow(10, 10)), Int(127)), Sym("li"))
    CONVERSIONS["LINE:METER"] = \
        Mul(Rat(Mul(Int(6), Pow(10, 4)), Int(127)), Sym("li"))
    CONVERSIONS["LINE:MICROMETER"] = \
        Mul(Rat(Int(3), Int(6350)), Sym("li"))
    CONVERSIONS["LINE:MILE"] = \
        Mul(Int(760320), Sym("li"))
    CONVERSIONS["LINE:MILLIMETER"] = \
        Mul(Rat(Int(60), Int(127)), Sym("li"))
    CONVERSIONS["LINE:NANOMETER"] = \
        Mul(Rat(Int(3), Mul(Int(635), Pow(10, 4))), Sym("li"))
    CONVERSIONS["LINE:PARSEC"] = \
        Mul(Rat(Mul(Int(185140656), Pow(10, 13)), Int(127)), Sym("li"))
    CONVERSIONS["LINE:PETAMETER"] = \
        Mul(Rat(Mul(Int(6), Pow(10, 19)), Int(127)), Sym("li"))
    CONVERSIONS["LINE:PICOMETER"] = \
        Mul(Rat(Int(3), Mul(Int(635), Pow(10, 7))), Sym("li"))
    CONVERSIONS["LINE:POINT"] = \
        Mul(Rat(Int(1), Int(6)), Sym("li"))
    CONVERSIONS["LINE:TERAMETER"] = \
        Mul(Rat(Mul(Int(6), Pow(10, 16)), Int(127)), Sym("li"))
    CONVERSIONS["LINE:THOU"] = \
        Mul(Rat(Int(3), Int(250)), Sym("li"))
    CONVERSIONS["LINE:YARD"] = \
        Mul(Int(432), Sym("li"))
    CONVERSIONS["LINE:YOCTOMETER"] = \
        Mul(Rat(Int(3), Mul(Int(635), Pow(10, 19))), Sym("li"))
    CONVERSIONS["LINE:YOTTAMETER"] = \
        Mul(Rat(Mul(Int(6), Pow(10, 28)), Int(127)), Sym("li"))
    CONVERSIONS["LINE:ZEPTOMETER"] = \
        Mul(Rat(Int(3), Mul(Int(635), Pow(10, 16))), Sym("li"))
    CONVERSIONS["LINE:ZETTAMETER"] = \
        Mul(Rat(Mul(Int(6), Pow(10, 25)), Int(127)), Sym("li"))
    CONVERSIONS["MEGAMETER:ANGSTROM"] = \
        Mul(Rat(Int(1), Pow(10, 16)), Sym("megam"))
    CONVERSIONS["MEGAMETER:ASTRONMICALUNIT"] = \
        Mul(Rat(Int(1495978707), Pow(10, 4)), Sym("megam"))
    CONVERSIONS["MEGAMETER:ATTOMETER"] = \
        Mul(Rat(Int(1), Pow(10, 24)), Sym("megam"))
    CONVERSIONS["MEGAMETER:CENTIMETER"] = \
        Mul(Rat(Int(1), Pow(10, 8)), Sym("megam"))
    CONVERSIONS["MEGAMETER:DECAMETER"] = \
        Mul(Rat(Int(1), Pow(10, 5)), Sym("megam"))
    CONVERSIONS["MEGAMETER:DECIMETER"] = \
        Mul(Rat(Int(1), Pow(10, 7)), Sym("megam"))
    CONVERSIONS["MEGAMETER:EXAMETER"] = \
        Mul(Pow(10, 12), Sym("megam"))
    CONVERSIONS["MEGAMETER:FEMTOMETER"] = \
        Mul(Rat(Int(1), Pow(10, 21)), Sym("megam"))
    CONVERSIONS["MEGAMETER:FOOT"] = \
        Mul(Rat(Int(381), Mul(Int(125), Pow(10, 7))), Sym("megam"))
    CONVERSIONS["MEGAMETER:GIGAMETER"] = \
        Mul(Int(1000), Sym("megam"))
    CONVERSIONS["MEGAMETER:HECTOMETER"] = \
        Mul(Rat(Int(1), Pow(10, 4)), Sym("megam"))
    CONVERSIONS["MEGAMETER:INCH"] = \
        Mul(Rat(Int(127), Mul(Int(5), Pow(10, 9))), Sym("megam"))
    CONVERSIONS["MEGAMETER:KILOMETER"] = \
        Mul(Rat(Int(1), Int(1000)), Sym("megam"))
    CONVERSIONS["MEGAMETER:LIGHTYEAR"] = \
        Mul(Rat(Int("5912956545363"), Int(625)), Sym("megam"))
    CONVERSIONS["MEGAMETER:LINE"] = \
        Mul(Rat(Int(127), Mul(Int(6), Pow(10, 10))), Sym("megam"))
    CONVERSIONS["MEGAMETER:METER"] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("megam"))
    CONVERSIONS["MEGAMETER:MICROMETER"] = \
        Mul(Rat(Int(1), Pow(10, 12)), Sym("megam"))
    CONVERSIONS["MEGAMETER:MILE"] = \
        Mul(Rat(Int(12573), Int(7812500)), Sym("megam"))
    CONVERSIONS["MEGAMETER:MILLIMETER"] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("megam"))
    CONVERSIONS["MEGAMETER:NANOMETER"] = \
        Mul(Rat(Int(1), Pow(10, 15)), Sym("megam"))
    CONVERSIONS["MEGAMETER:PARSEC"] = \
        Mul(Int("30856776000"), Sym("megam"))
    CONVERSIONS["MEGAMETER:PETAMETER"] = \
        Mul(Pow(10, 9), Sym("megam"))
    CONVERSIONS["MEGAMETER:PICOMETER"] = \
        Mul(Rat(Int(1), Pow(10, 18)), Sym("megam"))
    CONVERSIONS["MEGAMETER:POINT"] = \
        Mul(Rat(Int(127), Mul(Int(36), Pow(10, 10))), Sym("megam"))
    CONVERSIONS["MEGAMETER:TERAMETER"] = \
        Mul(Pow(10, 6), Sym("megam"))
    CONVERSIONS["MEGAMETER:THOU"] = \
        Mul(Rat(Int(127), Mul(Int(5), Pow(10, 12))), Sym("megam"))
    CONVERSIONS["MEGAMETER:YARD"] = \
        Mul(Rat(Int(1143), Mul(Int(125), Pow(10, 7))), Sym("megam"))
    CONVERSIONS["MEGAMETER:YOCTOMETER"] = \
        Mul(Rat(Int(1), Pow(10, 30)), Sym("megam"))
    CONVERSIONS["MEGAMETER:YOTTAMETER"] = \
        Mul(Pow(10, 18), Sym("megam"))
    CONVERSIONS["MEGAMETER:ZEPTOMETER"] = \
        Mul(Rat(Int(1), Pow(10, 27)), Sym("megam"))
    CONVERSIONS["MEGAMETER:ZETTAMETER"] = \
        Mul(Pow(10, 15), Sym("megam"))
    CONVERSIONS["METER:ANGSTROM"] = \
        Mul(Rat(Int(1), Pow(10, 10)), Sym("m"))
    CONVERSIONS["METER:ASTRONMICALUNIT"] = \
        Mul(Int("149597870700"), Sym("m"))
    CONVERSIONS["METER:ATTOMETER"] = \
        Mul(Rat(Int(1), Pow(10, 18)), Sym("m"))
    CONVERSIONS["METER:CENTIMETER"] = \
        Mul(Rat(Int(1), Int(100)), Sym("m"))
    CONVERSIONS["METER:DECAMETER"] = \
        Mul(Int(10), Sym("m"))
    CONVERSIONS["METER:DECIMETER"] = \
        Mul(Rat(Int(1), Int(10)), Sym("m"))
    CONVERSIONS["METER:EXAMETER"] = \
        Mul(Pow(10, 18), Sym("m"))
    CONVERSIONS["METER:FEMTOMETER"] = \
        Mul(Rat(Int(1), Pow(10, 15)), Sym("m"))
    CONVERSIONS["METER:FOOT"] = \
        Mul(Rat(Int(381), Int(1250)), Sym("m"))
    CONVERSIONS["METER:GIGAMETER"] = \
        Mul(Pow(10, 9), Sym("m"))
    CONVERSIONS["METER:HECTOMETER"] = \
        Mul(Int(100), Sym("m"))
    CONVERSIONS["METER:INCH"] = \
        Mul(Rat(Int(127), Int(5000)), Sym("m"))
    CONVERSIONS["METER:KILOMETER"] = \
        Mul(Int(1000), Sym("m"))
    CONVERSIONS["METER:LIGHTYEAR"] = \
        Mul(Int("9460730472580800"), Sym("m"))
    CONVERSIONS["METER:LINE"] = \
        Mul(Rat(Int(127), Mul(Int(6), Pow(10, 4))), Sym("m"))
    CONVERSIONS["METER:MEGAMETER"] = \
        Mul(Pow(10, 6), Sym("m"))
    CONVERSIONS["METER:MICROMETER"] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("m"))
    CONVERSIONS["METER:MILE"] = \
        Mul(Rat(Int(201168), Int(125)), Sym("m"))
    CONVERSIONS["METER:MILLIMETER"] = \
        Mul(Rat(Int(1), Int(1000)), Sym("m"))
    CONVERSIONS["METER:NANOMETER"] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("m"))
    CONVERSIONS["METER:PARSEC"] = \
        Mul(Mul(Int(30856776), Pow(10, 9)), Sym("m"))
    CONVERSIONS["METER:PETAMETER"] = \
        Mul(Pow(10, 15), Sym("m"))
    CONVERSIONS["METER:PICOMETER"] = \
        Mul(Rat(Int(1), Pow(10, 12)), Sym("m"))
    CONVERSIONS["METER:POINT"] = \
        Mul(Rat(Int(127), Mul(Int(36), Pow(10, 4))), Sym("m"))
    CONVERSIONS["METER:TERAMETER"] = \
        Mul(Pow(10, 12), Sym("m"))
    CONVERSIONS["METER:THOU"] = \
        Mul(Rat(Int(127), Mul(Int(5), Pow(10, 6))), Sym("m"))
    CONVERSIONS["METER:YARD"] = \
        Mul(Rat(Int(1143), Int(1250)), Sym("m"))
    CONVERSIONS["METER:YOCTOMETER"] = \
        Mul(Rat(Int(1), Pow(10, 24)), Sym("m"))
    CONVERSIONS["METER:YOTTAMETER"] = \
        Mul(Pow(10, 24), Sym("m"))
    CONVERSIONS["METER:ZEPTOMETER"] = \
        Mul(Rat(Int(1), Pow(10, 21)), Sym("m"))
    CONVERSIONS["METER:ZETTAMETER"] = \
        Mul(Pow(10, 21), Sym("m"))
    CONVERSIONS["MICROMETER:ANGSTROM"] = \
        Mul(Rat(Int(1), Pow(10, 4)), Sym("microm"))
    CONVERSIONS["MICROMETER:ASTRONMICALUNIT"] = \
        Mul(Mul(Int(1495978707), Pow(10, 8)), Sym("microm"))
    CONVERSIONS["MICROMETER:ATTOMETER"] = \
        Mul(Rat(Int(1), Pow(10, 12)), Sym("microm"))
    CONVERSIONS["MICROMETER:CENTIMETER"] = \
        Mul(Pow(10, 4), Sym("microm"))
    CONVERSIONS["MICROMETER:DECAMETER"] = \
        Mul(Pow(10, 7), Sym("microm"))
    CONVERSIONS["MICROMETER:DECIMETER"] = \
        Mul(Pow(10, 5), Sym("microm"))
    CONVERSIONS["MICROMETER:EXAMETER"] = \
        Mul(Pow(10, 24), Sym("microm"))
    CONVERSIONS["MICROMETER:FEMTOMETER"] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("microm"))
    CONVERSIONS["MICROMETER:FOOT"] = \
        Mul(Int(304800), Sym("microm"))
    CONVERSIONS["MICROMETER:GIGAMETER"] = \
        Mul(Pow(10, 15), Sym("microm"))
    CONVERSIONS["MICROMETER:HECTOMETER"] = \
        Mul(Pow(10, 8), Sym("microm"))
    CONVERSIONS["MICROMETER:INCH"] = \
        Mul(Int(25400), Sym("microm"))
    CONVERSIONS["MICROMETER:KILOMETER"] = \
        Mul(Pow(10, 9), Sym("microm"))
    CONVERSIONS["MICROMETER:LIGHTYEAR"] = \
        Mul(Mul(Int("94607304725808"), Pow(10, 8)), Sym("microm"))
    CONVERSIONS["MICROMETER:LINE"] = \
        Mul(Rat(Int(6350), Int(3)), Sym("microm"))
    CONVERSIONS["MICROMETER:MEGAMETER"] = \
        Mul(Pow(10, 12), Sym("microm"))
    CONVERSIONS["MICROMETER:METER"] = \
        Mul(Pow(10, 6), Sym("microm"))
    CONVERSIONS["MICROMETER:MILE"] = \
        Mul(Int(1609344000), Sym("microm"))
    CONVERSIONS["MICROMETER:MILLIMETER"] = \
        Mul(Int(1000), Sym("microm"))
    CONVERSIONS["MICROMETER:NANOMETER"] = \
        Mul(Rat(Int(1), Int(1000)), Sym("microm"))
    CONVERSIONS["MICROMETER:PARSEC"] = \
        Mul(Mul(Int(30856776), Pow(10, 15)), Sym("microm"))
    CONVERSIONS["MICROMETER:PETAMETER"] = \
        Mul(Pow(10, 21), Sym("microm"))
    CONVERSIONS["MICROMETER:PICOMETER"] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("microm"))
    CONVERSIONS["MICROMETER:POINT"] = \
        Mul(Rat(Int(3175), Int(9)), Sym("microm"))
    CONVERSIONS["MICROMETER:TERAMETER"] = \
        Mul(Pow(10, 18), Sym("microm"))
    CONVERSIONS["MICROMETER:THOU"] = \
        Mul(Rat(Int(127), Int(5)), Sym("microm"))
    CONVERSIONS["MICROMETER:YARD"] = \
        Mul(Int(914400), Sym("microm"))
    CONVERSIONS["MICROMETER:YOCTOMETER"] = \
        Mul(Rat(Int(1), Pow(10, 18)), Sym("microm"))
    CONVERSIONS["MICROMETER:YOTTAMETER"] = \
        Mul(Pow(10, 30), Sym("microm"))
    CONVERSIONS["MICROMETER:ZEPTOMETER"] = \
        Mul(Rat(Int(1), Pow(10, 15)), Sym("microm"))
    CONVERSIONS["MICROMETER:ZETTAMETER"] = \
        Mul(Pow(10, 27), Sym("microm"))
    CONVERSIONS["MILE:ANGSTROM"] = \
        Mul(Rat(Int(1), Mul(Int(1609344), Pow(10, 7))), Sym("mi"))
    CONVERSIONS["MILE:ASTRONMICALUNIT"] = \
        Mul(Rat(Int("1558311153125"), Int(16764)), Sym("mi"))
    CONVERSIONS["MILE:ATTOMETER"] = \
        Mul(Rat(Int(1), Mul(Int(1609344), Pow(10, 15))), Sym("mi"))
    CONVERSIONS["MILE:CENTIMETER"] = \
        Mul(Rat(Int(5), Int(804672)), Sym("mi"))
    CONVERSIONS["MILE:DECAMETER"] = \
        Mul(Rat(Int(625), Int(100584)), Sym("mi"))
    CONVERSIONS["MILE:DECIMETER"] = \
        Mul(Rat(Int(25), Int(402336)), Sym("mi"))
    CONVERSIONS["MILE:EXAMETER"] = \
        Mul(Rat(Mul(Int(78125), Pow(10, 14)), Int(12573)), Sym("mi"))
    CONVERSIONS["MILE:FEMTOMETER"] = \
        Mul(Rat(Int(1), Mul(Int(1609344), Pow(10, 12))), Sym("mi"))
    CONVERSIONS["MILE:FOOT"] = \
        Mul(Rat(Int(1), Int(5280)), Sym("mi"))
    CONVERSIONS["MILE:GIGAMETER"] = \
        Mul(Rat(Mul(Int(78125), Pow(10, 5)), Int(12573)), Sym("mi"))
    CONVERSIONS["MILE:HECTOMETER"] = \
        Mul(Rat(Int(3125), Int(50292)), Sym("mi"))
    CONVERSIONS["MILE:INCH"] = \
        Mul(Rat(Int(1), Int(63360)), Sym("mi"))
    CONVERSIONS["MILE:KILOMETER"] = \
        Mul(Rat(Int(15625), Int(25146)), Sym("mi"))
    CONVERSIONS["MILE:LIGHTYEAR"] = \
        Mul(Rat(Int("8212439646337500"), Int(1397)), Sym("mi"))
    CONVERSIONS["MILE:LINE"] = \
        Mul(Rat(Int(1), Int(760320)), Sym("mi"))
    CONVERSIONS["MILE:MEGAMETER"] = \
        Mul(Rat(Int(7812500), Int(12573)), Sym("mi"))
    CONVERSIONS["MILE:METER"] = \
        Mul(Rat(Int(125), Int(201168)), Sym("mi"))
    CONVERSIONS["MILE:MICROMETER"] = \
        Mul(Rat(Int(1), Int(1609344000)), Sym("mi"))
    CONVERSIONS["MILE:MILLIMETER"] = \
        Mul(Rat(Int(1), Int(1609344)), Sym("mi"))
    CONVERSIONS["MILE:NANOMETER"] = \
        Mul(Rat(Int(1), Mul(Int(1609344), Pow(10, 6))), Sym("mi"))
    CONVERSIONS["MILE:PARSEC"] = \
        Mul(Rat(Mul(Int(803561875), Pow(10, 8)), Int(4191)), Sym("mi"))
    CONVERSIONS["MILE:PETAMETER"] = \
        Mul(Rat(Mul(Int(78125), Pow(10, 11)), Int(12573)), Sym("mi"))
    CONVERSIONS["MILE:PICOMETER"] = \
        Mul(Rat(Int(1), Mul(Int(1609344), Pow(10, 9))), Sym("mi"))
    CONVERSIONS["MILE:POINT"] = \
        Mul(Rat(Int(1), Int(4561920)), Sym("mi"))
    CONVERSIONS["MILE:TERAMETER"] = \
        Mul(Rat(Mul(Int(78125), Pow(10, 8)), Int(12573)), Sym("mi"))
    CONVERSIONS["MILE:THOU"] = \
        Mul(Rat(Int(1), Mul(Int(6336), Pow(10, 4))), Sym("mi"))
    CONVERSIONS["MILE:YARD"] = \
        Mul(Rat(Int(1), Int(1760)), Sym("mi"))
    CONVERSIONS["MILE:YOCTOMETER"] = \
        Mul(Rat(Int(1), Mul(Int(1609344), Pow(10, 21))), Sym("mi"))
    CONVERSIONS["MILE:YOTTAMETER"] = \
        Mul(Rat(Mul(Int(78125), Pow(10, 20)), Int(12573)), Sym("mi"))
    CONVERSIONS["MILE:ZEPTOMETER"] = \
        Mul(Rat(Int(1), Mul(Int(1609344), Pow(10, 18))), Sym("mi"))
    CONVERSIONS["MILE:ZETTAMETER"] = \
        Mul(Rat(Mul(Int(78125), Pow(10, 17)), Int(12573)), Sym("mi"))
    CONVERSIONS["MILLIMETER:ANGSTROM"] = \
        Mul(Rat(Int(1), Pow(10, 7)), Sym("millim"))
    CONVERSIONS["MILLIMETER:ASTRONMICALUNIT"] = \
        Mul(Mul(Int(1495978707), Pow(10, 5)), Sym("millim"))
    CONVERSIONS["MILLIMETER:ATTOMETER"] = \
        Mul(Rat(Int(1), Pow(10, 15)), Sym("millim"))
    CONVERSIONS["MILLIMETER:CENTIMETER"] = \
        Mul(Int(10), Sym("millim"))
    CONVERSIONS["MILLIMETER:DECAMETER"] = \
        Mul(Pow(10, 4), Sym("millim"))
    CONVERSIONS["MILLIMETER:DECIMETER"] = \
        Mul(Int(100), Sym("millim"))
    CONVERSIONS["MILLIMETER:EXAMETER"] = \
        Mul(Pow(10, 21), Sym("millim"))
    CONVERSIONS["MILLIMETER:FEMTOMETER"] = \
        Mul(Rat(Int(1), Pow(10, 12)), Sym("millim"))
    CONVERSIONS["MILLIMETER:FOOT"] = \
        Mul(Rat(Int(1524), Int(5)), Sym("millim"))
    CONVERSIONS["MILLIMETER:GIGAMETER"] = \
        Mul(Pow(10, 12), Sym("millim"))
    CONVERSIONS["MILLIMETER:HECTOMETER"] = \
        Mul(Pow(10, 5), Sym("millim"))
    CONVERSIONS["MILLIMETER:INCH"] = \
        Mul(Rat(Int(127), Int(5)), Sym("millim"))
    CONVERSIONS["MILLIMETER:KILOMETER"] = \
        Mul(Pow(10, 6), Sym("millim"))
    CONVERSIONS["MILLIMETER:LIGHTYEAR"] = \
        Mul(Mul(Int("94607304725808"), Pow(10, 5)), Sym("millim"))
    CONVERSIONS["MILLIMETER:LINE"] = \
        Mul(Rat(Int(127), Int(60)), Sym("millim"))
    CONVERSIONS["MILLIMETER:MEGAMETER"] = \
        Mul(Pow(10, 9), Sym("millim"))
    CONVERSIONS["MILLIMETER:METER"] = \
        Mul(Int(1000), Sym("millim"))
    CONVERSIONS["MILLIMETER:MICROMETER"] = \
        Mul(Rat(Int(1), Int(1000)), Sym("millim"))
    CONVERSIONS["MILLIMETER:MILE"] = \
        Mul(Int(1609344), Sym("millim"))
    CONVERSIONS["MILLIMETER:NANOMETER"] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("millim"))
    CONVERSIONS["MILLIMETER:PARSEC"] = \
        Mul(Mul(Int(30856776), Pow(10, 12)), Sym("millim"))
    CONVERSIONS["MILLIMETER:PETAMETER"] = \
        Mul(Pow(10, 18), Sym("millim"))
    CONVERSIONS["MILLIMETER:PICOMETER"] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("millim"))
    CONVERSIONS["MILLIMETER:POINT"] = \
        Mul(Rat(Int(127), Int(360)), Sym("millim"))
    CONVERSIONS["MILLIMETER:TERAMETER"] = \
        Mul(Pow(10, 15), Sym("millim"))
    CONVERSIONS["MILLIMETER:THOU"] = \
        Mul(Rat(Int(127), Int(5000)), Sym("millim"))
    CONVERSIONS["MILLIMETER:YARD"] = \
        Mul(Rat(Int(4572), Int(5)), Sym("millim"))
    CONVERSIONS["MILLIMETER:YOCTOMETER"] = \
        Mul(Rat(Int(1), Pow(10, 21)), Sym("millim"))
    CONVERSIONS["MILLIMETER:YOTTAMETER"] = \
        Mul(Pow(10, 27), Sym("millim"))
    CONVERSIONS["MILLIMETER:ZEPTOMETER"] = \
        Mul(Rat(Int(1), Pow(10, 18)), Sym("millim"))
    CONVERSIONS["MILLIMETER:ZETTAMETER"] = \
        Mul(Pow(10, 24), Sym("millim"))
    CONVERSIONS["NANOMETER:ANGSTROM"] = \
        Mul(Rat(Int(1), Int(10)), Sym("nanom"))
    CONVERSIONS["NANOMETER:ASTRONMICALUNIT"] = \
        Mul(Mul(Int(1495978707), Pow(10, 11)), Sym("nanom"))
    CONVERSIONS["NANOMETER:ATTOMETER"] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("nanom"))
    CONVERSIONS["NANOMETER:CENTIMETER"] = \
        Mul(Pow(10, 7), Sym("nanom"))
    CONVERSIONS["NANOMETER:DECAMETER"] = \
        Mul(Pow(10, 10), Sym("nanom"))
    CONVERSIONS["NANOMETER:DECIMETER"] = \
        Mul(Pow(10, 8), Sym("nanom"))
    CONVERSIONS["NANOMETER:EXAMETER"] = \
        Mul(Pow(10, 27), Sym("nanom"))
    CONVERSIONS["NANOMETER:FEMTOMETER"] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("nanom"))
    CONVERSIONS["NANOMETER:FOOT"] = \
        Mul(Mul(Int(3048), Pow(10, 5)), Sym("nanom"))
    CONVERSIONS["NANOMETER:GIGAMETER"] = \
        Mul(Pow(10, 18), Sym("nanom"))
    CONVERSIONS["NANOMETER:HECTOMETER"] = \
        Mul(Pow(10, 11), Sym("nanom"))
    CONVERSIONS["NANOMETER:INCH"] = \
        Mul(Mul(Int(254), Pow(10, 5)), Sym("nanom"))
    CONVERSIONS["NANOMETER:KILOMETER"] = \
        Mul(Pow(10, 12), Sym("nanom"))
    CONVERSIONS["NANOMETER:LIGHTYEAR"] = \
        Mul(Mul(Int("94607304725808"), Pow(10, 11)), Sym("nanom"))
    CONVERSIONS["NANOMETER:LINE"] = \
        Mul(Rat(Mul(Int(635), Pow(10, 4)), Int(3)), Sym("nanom"))
    CONVERSIONS["NANOMETER:MEGAMETER"] = \
        Mul(Pow(10, 15), Sym("nanom"))
    CONVERSIONS["NANOMETER:METER"] = \
        Mul(Pow(10, 9), Sym("nanom"))
    CONVERSIONS["NANOMETER:MICROMETER"] = \
        Mul(Int(1000), Sym("nanom"))
    CONVERSIONS["NANOMETER:MILE"] = \
        Mul(Mul(Int(1609344), Pow(10, 6)), Sym("nanom"))
    CONVERSIONS["NANOMETER:MILLIMETER"] = \
        Mul(Pow(10, 6), Sym("nanom"))
    CONVERSIONS["NANOMETER:PARSEC"] = \
        Mul(Mul(Int(30856776), Pow(10, 18)), Sym("nanom"))
    CONVERSIONS["NANOMETER:PETAMETER"] = \
        Mul(Pow(10, 24), Sym("nanom"))
    CONVERSIONS["NANOMETER:PICOMETER"] = \
        Mul(Rat(Int(1), Int(1000)), Sym("nanom"))
    CONVERSIONS["NANOMETER:POINT"] = \
        Mul(Rat(Int(3175000), Int(9)), Sym("nanom"))
    CONVERSIONS["NANOMETER:TERAMETER"] = \
        Mul(Pow(10, 21), Sym("nanom"))
    CONVERSIONS["NANOMETER:THOU"] = \
        Mul(Int(25400), Sym("nanom"))
    CONVERSIONS["NANOMETER:YARD"] = \
        Mul(Mul(Int(9144), Pow(10, 5)), Sym("nanom"))
    CONVERSIONS["NANOMETER:YOCTOMETER"] = \
        Mul(Rat(Int(1), Pow(10, 15)), Sym("nanom"))
    CONVERSIONS["NANOMETER:YOTTAMETER"] = \
        Mul(Pow(10, 33), Sym("nanom"))
    CONVERSIONS["NANOMETER:ZEPTOMETER"] = \
        Mul(Rat(Int(1), Pow(10, 12)), Sym("nanom"))
    CONVERSIONS["NANOMETER:ZETTAMETER"] = \
        Mul(Pow(10, 30), Sym("nanom"))
    CONVERSIONS["PARSEC:ANGSTROM"] = \
        Mul(Rat(Int(1), Mul(Int(30856776), Pow(10, 19))), Sym("pc"))
    CONVERSIONS["PARSEC:ASTRONMICALUNIT"] = \
        Mul(Rat(Int(498659569), Mul(Int(10285592), Pow(10, 7))), Sym("pc"))
    CONVERSIONS["PARSEC:ATTOMETER"] = \
        Mul(Rat(Int(1), Mul(Int(30856776), Pow(10, 27))), Sym("pc"))
    CONVERSIONS["PARSEC:CENTIMETER"] = \
        Mul(Rat(Int(1), Mul(Int(30856776), Pow(10, 11))), Sym("pc"))
    CONVERSIONS["PARSEC:DECAMETER"] = \
        Mul(Rat(Int(1), Mul(Int(30856776), Pow(10, 8))), Sym("pc"))
    CONVERSIONS["PARSEC:DECIMETER"] = \
        Mul(Rat(Int(1), Mul(Int(30856776), Pow(10, 10))), Sym("pc"))
    CONVERSIONS["PARSEC:EXAMETER"] = \
        Mul(Rat(Mul(Int(125), Pow(10, 6)), Int(3857097)), Sym("pc"))
    CONVERSIONS["PARSEC:FEMTOMETER"] = \
        Mul(Rat(Int(1), Mul(Int(30856776), Pow(10, 24))), Sym("pc"))
    CONVERSIONS["PARSEC:FOOT"] = \
        Mul(Rat(Int(127), Mul(Int(1285699), Pow(10, 13))), Sym("pc"))
    CONVERSIONS["PARSEC:GIGAMETER"] = \
        Mul(Rat(Int(1), Int(30856776)), Sym("pc"))
    CONVERSIONS["PARSEC:HECTOMETER"] = \
        Mul(Rat(Int(1), Mul(Int(30856776), Pow(10, 7))), Sym("pc"))
    CONVERSIONS["PARSEC:INCH"] = \
        Mul(Rat(Int(127), Mul(Int(15428388), Pow(10, 13))), Sym("pc"))
    CONVERSIONS["PARSEC:KILOMETER"] = \
        Mul(Rat(Int(1), Mul(Int(30856776), Pow(10, 6))), Sym("pc"))
    CONVERSIONS["PARSEC:LIGHTYEAR"] = \
        Mul(Rat(Int("1970985515121"), Mul(Int(6428495), Pow(10, 6))), Sym("pc"))
    CONVERSIONS["PARSEC:LINE"] = \
        Mul(Rat(Int(127), Mul(Int(185140656), Pow(10, 13))), Sym("pc"))
    CONVERSIONS["PARSEC:MEGAMETER"] = \
        Mul(Rat(Int(1), Int("30856776000")), Sym("pc"))
    CONVERSIONS["PARSEC:METER"] = \
        Mul(Rat(Int(1), Mul(Int(30856776), Pow(10, 9))), Sym("pc"))
    CONVERSIONS["PARSEC:MICROMETER"] = \
        Mul(Rat(Int(1), Mul(Int(30856776), Pow(10, 15))), Sym("pc"))
    CONVERSIONS["PARSEC:MILE"] = \
        Mul(Rat(Int(4191), Mul(Int(803561875), Pow(10, 8))), Sym("pc"))
    CONVERSIONS["PARSEC:MILLIMETER"] = \
        Mul(Rat(Int(1), Mul(Int(30856776), Pow(10, 12))), Sym("pc"))
    CONVERSIONS["PARSEC:NANOMETER"] = \
        Mul(Rat(Int(1), Mul(Int(30856776), Pow(10, 18))), Sym("pc"))
    CONVERSIONS["PARSEC:PETAMETER"] = \
        Mul(Rat(Int(125000), Int(3857097)), Sym("pc"))
    CONVERSIONS["PARSEC:PICOMETER"] = \
        Mul(Rat(Int(1), Mul(Int(30856776), Pow(10, 21))), Sym("pc"))
    CONVERSIONS["PARSEC:POINT"] = \
        Mul(Rat(Int(127), Mul(Int(1110843936), Pow(10, 13))), Sym("pc"))
    CONVERSIONS["PARSEC:TERAMETER"] = \
        Mul(Rat(Int(125), Int(3857097)), Sym("pc"))
    CONVERSIONS["PARSEC:THOU"] = \
        Mul(Rat(Int(127), Mul(Int(15428388), Pow(10, 16))), Sym("pc"))
    CONVERSIONS["PARSEC:YARD"] = \
        Mul(Rat(Int(381), Mul(Int(1285699), Pow(10, 13))), Sym("pc"))
    CONVERSIONS["PARSEC:YOCTOMETER"] = \
        Mul(Rat(Int(1), Mul(Int(30856776), Pow(10, 33))), Sym("pc"))
    CONVERSIONS["PARSEC:YOTTAMETER"] = \
        Mul(Rat(Mul(Int(125), Pow(10, 12)), Int(3857097)), Sym("pc"))
    CONVERSIONS["PARSEC:ZEPTOMETER"] = \
        Mul(Rat(Int(1), Mul(Int(30856776), Pow(10, 30))), Sym("pc"))
    CONVERSIONS["PARSEC:ZETTAMETER"] = \
        Mul(Rat(Mul(Int(125), Pow(10, 9)), Int(3857097)), Sym("pc"))
    CONVERSIONS["PETAMETER:ANGSTROM"] = \
        Mul(Rat(Int(1), Pow(10, 25)), Sym("petam"))
    CONVERSIONS["PETAMETER:ASTRONMICALUNIT"] = \
        Mul(Rat(Int(1495978707), Pow(10, 13)), Sym("petam"))
    CONVERSIONS["PETAMETER:ATTOMETER"] = \
        Mul(Rat(Int(1), Pow(10, 33)), Sym("petam"))
    CONVERSIONS["PETAMETER:CENTIMETER"] = \
        Mul(Rat(Int(1), Pow(10, 17)), Sym("petam"))
    CONVERSIONS["PETAMETER:DECAMETER"] = \
        Mul(Rat(Int(1), Pow(10, 14)), Sym("petam"))
    CONVERSIONS["PETAMETER:DECIMETER"] = \
        Mul(Rat(Int(1), Pow(10, 16)), Sym("petam"))
    CONVERSIONS["PETAMETER:EXAMETER"] = \
        Mul(Int(1000), Sym("petam"))
    CONVERSIONS["PETAMETER:FEMTOMETER"] = \
        Mul(Rat(Int(1), Pow(10, 30)), Sym("petam"))
    CONVERSIONS["PETAMETER:FOOT"] = \
        Mul(Rat(Int(381), Mul(Int(125), Pow(10, 16))), Sym("petam"))
    CONVERSIONS["PETAMETER:GIGAMETER"] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("petam"))
    CONVERSIONS["PETAMETER:HECTOMETER"] = \
        Mul(Rat(Int(1), Pow(10, 13)), Sym("petam"))
    CONVERSIONS["PETAMETER:INCH"] = \
        Mul(Rat(Int(127), Mul(Int(5), Pow(10, 18))), Sym("petam"))
    CONVERSIONS["PETAMETER:KILOMETER"] = \
        Mul(Rat(Int(1), Pow(10, 12)), Sym("petam"))
    CONVERSIONS["PETAMETER:LIGHTYEAR"] = \
        Mul(Rat(Int("5912956545363"), Mul(Int(625), Pow(10, 9))), Sym("petam"))
    CONVERSIONS["PETAMETER:LINE"] = \
        Mul(Rat(Int(127), Mul(Int(6), Pow(10, 19))), Sym("petam"))
    CONVERSIONS["PETAMETER:MEGAMETER"] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("petam"))
    CONVERSIONS["PETAMETER:METER"] = \
        Mul(Rat(Int(1), Pow(10, 15)), Sym("petam"))
    CONVERSIONS["PETAMETER:MICROMETER"] = \
        Mul(Rat(Int(1), Pow(10, 21)), Sym("petam"))
    CONVERSIONS["PETAMETER:MILE"] = \
        Mul(Rat(Int(12573), Mul(Int(78125), Pow(10, 11))), Sym("petam"))
    CONVERSIONS["PETAMETER:MILLIMETER"] = \
        Mul(Rat(Int(1), Pow(10, 18)), Sym("petam"))
    CONVERSIONS["PETAMETER:NANOMETER"] = \
        Mul(Rat(Int(1), Pow(10, 24)), Sym("petam"))
    CONVERSIONS["PETAMETER:PARSEC"] = \
        Mul(Rat(Int(3857097), Int(125000)), Sym("petam"))
    CONVERSIONS["PETAMETER:PICOMETER"] = \
        Mul(Rat(Int(1), Pow(10, 27)), Sym("petam"))
    CONVERSIONS["PETAMETER:POINT"] = \
        Mul(Rat(Int(127), Mul(Int(36), Pow(10, 19))), Sym("petam"))
    CONVERSIONS["PETAMETER:TERAMETER"] = \
        Mul(Rat(Int(1), Int(1000)), Sym("petam"))
    CONVERSIONS["PETAMETER:THOU"] = \
        Mul(Rat(Int(127), Mul(Int(5), Pow(10, 21))), Sym("petam"))
    CONVERSIONS["PETAMETER:YARD"] = \
        Mul(Rat(Int(1143), Mul(Int(125), Pow(10, 16))), Sym("petam"))
    CONVERSIONS["PETAMETER:YOCTOMETER"] = \
        Mul(Rat(Int(1), Pow(10, 39)), Sym("petam"))
    CONVERSIONS["PETAMETER:YOTTAMETER"] = \
        Mul(Pow(10, 9), Sym("petam"))
    CONVERSIONS["PETAMETER:ZEPTOMETER"] = \
        Mul(Rat(Int(1), Pow(10, 36)), Sym("petam"))
    CONVERSIONS["PETAMETER:ZETTAMETER"] = \
        Mul(Pow(10, 6), Sym("petam"))
    CONVERSIONS["PICOMETER:ANGSTROM"] = \
        Mul(Int(100), Sym("picom"))
    CONVERSIONS["PICOMETER:ASTRONMICALUNIT"] = \
        Mul(Mul(Int(1495978707), Pow(10, 14)), Sym("picom"))
    CONVERSIONS["PICOMETER:ATTOMETER"] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("picom"))
    CONVERSIONS["PICOMETER:CENTIMETER"] = \
        Mul(Pow(10, 10), Sym("picom"))
    CONVERSIONS["PICOMETER:DECAMETER"] = \
        Mul(Pow(10, 13), Sym("picom"))
    CONVERSIONS["PICOMETER:DECIMETER"] = \
        Mul(Pow(10, 11), Sym("picom"))
    CONVERSIONS["PICOMETER:EXAMETER"] = \
        Mul(Pow(10, 30), Sym("picom"))
    CONVERSIONS["PICOMETER:FEMTOMETER"] = \
        Mul(Rat(Int(1), Int(1000)), Sym("picom"))
    CONVERSIONS["PICOMETER:FOOT"] = \
        Mul(Mul(Int(3048), Pow(10, 8)), Sym("picom"))
    CONVERSIONS["PICOMETER:GIGAMETER"] = \
        Mul(Pow(10, 21), Sym("picom"))
    CONVERSIONS["PICOMETER:HECTOMETER"] = \
        Mul(Pow(10, 14), Sym("picom"))
    CONVERSIONS["PICOMETER:INCH"] = \
        Mul(Mul(Int(254), Pow(10, 8)), Sym("picom"))
    CONVERSIONS["PICOMETER:KILOMETER"] = \
        Mul(Pow(10, 15), Sym("picom"))
    CONVERSIONS["PICOMETER:LIGHTYEAR"] = \
        Mul(Mul(Int("94607304725808"), Pow(10, 14)), Sym("picom"))
    CONVERSIONS["PICOMETER:LINE"] = \
        Mul(Rat(Mul(Int(635), Pow(10, 7)), Int(3)), Sym("picom"))
    CONVERSIONS["PICOMETER:MEGAMETER"] = \
        Mul(Pow(10, 18), Sym("picom"))
    CONVERSIONS["PICOMETER:METER"] = \
        Mul(Pow(10, 12), Sym("picom"))
    CONVERSIONS["PICOMETER:MICROMETER"] = \
        Mul(Pow(10, 6), Sym("picom"))
    CONVERSIONS["PICOMETER:MILE"] = \
        Mul(Mul(Int(1609344), Pow(10, 9)), Sym("picom"))
    CONVERSIONS["PICOMETER:MILLIMETER"] = \
        Mul(Pow(10, 9), Sym("picom"))
    CONVERSIONS["PICOMETER:NANOMETER"] = \
        Mul(Int(1000), Sym("picom"))
    CONVERSIONS["PICOMETER:PARSEC"] = \
        Mul(Mul(Int(30856776), Pow(10, 21)), Sym("picom"))
    CONVERSIONS["PICOMETER:PETAMETER"] = \
        Mul(Pow(10, 27), Sym("picom"))
    CONVERSIONS["PICOMETER:POINT"] = \
        Mul(Rat(Mul(Int(3175), Pow(10, 6)), Int(9)), Sym("picom"))
    CONVERSIONS["PICOMETER:TERAMETER"] = \
        Mul(Pow(10, 24), Sym("picom"))
    CONVERSIONS["PICOMETER:THOU"] = \
        Mul(Mul(Int(254), Pow(10, 5)), Sym("picom"))
    CONVERSIONS["PICOMETER:YARD"] = \
        Mul(Mul(Int(9144), Pow(10, 8)), Sym("picom"))
    CONVERSIONS["PICOMETER:YOCTOMETER"] = \
        Mul(Rat(Int(1), Pow(10, 12)), Sym("picom"))
    CONVERSIONS["PICOMETER:YOTTAMETER"] = \
        Mul(Pow(10, 36), Sym("picom"))
    CONVERSIONS["PICOMETER:ZEPTOMETER"] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("picom"))
    CONVERSIONS["PICOMETER:ZETTAMETER"] = \
        Mul(Pow(10, 33), Sym("picom"))
    CONVERSIONS["POINT:ANGSTROM"] = \
        Mul(Rat(Int(9), Mul(Int(3175), Pow(10, 4))), Sym("pt"))
    CONVERSIONS["POINT:ASTRONMICALUNIT"] = \
        Mul(Rat(Mul(Int("53855233452"), Pow(10, 6)), Int(127)), Sym("pt"))
    CONVERSIONS["POINT:ATTOMETER"] = \
        Mul(Rat(Int(9), Mul(Int(3175), Pow(10, 12))), Sym("pt"))
    CONVERSIONS["POINT:CENTIMETER"] = \
        Mul(Rat(Int(3600), Int(127)), Sym("pt"))
    CONVERSIONS["POINT:DECAMETER"] = \
        Mul(Rat(Mul(Int(36), Pow(10, 5)), Int(127)), Sym("pt"))
    CONVERSIONS["POINT:DECIMETER"] = \
        Mul(Rat(Int(36000), Int(127)), Sym("pt"))
    CONVERSIONS["POINT:EXAMETER"] = \
        Mul(Rat(Mul(Int(36), Pow(10, 22)), Int(127)), Sym("pt"))
    CONVERSIONS["POINT:FEMTOMETER"] = \
        Mul(Rat(Int(9), Mul(Int(3175), Pow(10, 9))), Sym("pt"))
    CONVERSIONS["POINT:FOOT"] = \
        Mul(Int(864), Sym("pt"))
    CONVERSIONS["POINT:GIGAMETER"] = \
        Mul(Rat(Mul(Int(36), Pow(10, 13)), Int(127)), Sym("pt"))
    CONVERSIONS["POINT:HECTOMETER"] = \
        Mul(Rat(Mul(Int(36), Pow(10, 6)), Int(127)), Sym("pt"))
    CONVERSIONS["POINT:INCH"] = \
        Mul(Int(72), Sym("pt"))
    CONVERSIONS["POINT:KILOMETER"] = \
        Mul(Rat(Mul(Int(36), Pow(10, 7)), Int(127)), Sym("pt"))
    CONVERSIONS["POINT:LIGHTYEAR"] = \
        Mul(Rat(Mul(Int("3405862970129088"), Pow(10, 6)), Int(127)), Sym("pt"))
    CONVERSIONS["POINT:LINE"] = \
        Mul(Int(6), Sym("pt"))
    CONVERSIONS["POINT:MEGAMETER"] = \
        Mul(Rat(Mul(Int(36), Pow(10, 10)), Int(127)), Sym("pt"))
    CONVERSIONS["POINT:METER"] = \
        Mul(Rat(Mul(Int(36), Pow(10, 4)), Int(127)), Sym("pt"))
    CONVERSIONS["POINT:MICROMETER"] = \
        Mul(Rat(Int(9), Int(3175)), Sym("pt"))
    CONVERSIONS["POINT:MILE"] = \
        Mul(Int(4561920), Sym("pt"))
    CONVERSIONS["POINT:MILLIMETER"] = \
        Mul(Rat(Int(360), Int(127)), Sym("pt"))
    CONVERSIONS["POINT:NANOMETER"] = \
        Mul(Rat(Int(9), Int(3175000)), Sym("pt"))
    CONVERSIONS["POINT:PARSEC"] = \
        Mul(Rat(Mul(Int(1110843936), Pow(10, 13)), Int(127)), Sym("pt"))
    CONVERSIONS["POINT:PETAMETER"] = \
        Mul(Rat(Mul(Int(36), Pow(10, 19)), Int(127)), Sym("pt"))
    CONVERSIONS["POINT:PICOMETER"] = \
        Mul(Rat(Int(9), Mul(Int(3175), Pow(10, 6))), Sym("pt"))
    CONVERSIONS["POINT:TERAMETER"] = \
        Mul(Rat(Mul(Int(36), Pow(10, 16)), Int(127)), Sym("pt"))
    CONVERSIONS["POINT:THOU"] = \
        Mul(Rat(Int(9), Int(125)), Sym("pt"))
    CONVERSIONS["POINT:YARD"] = \
        Mul(Int(2592), Sym("pt"))
    CONVERSIONS["POINT:YOCTOMETER"] = \
        Mul(Rat(Int(9), Mul(Int(3175), Pow(10, 18))), Sym("pt"))
    CONVERSIONS["POINT:YOTTAMETER"] = \
        Mul(Rat(Mul(Int(36), Pow(10, 28)), Int(127)), Sym("pt"))
    CONVERSIONS["POINT:ZEPTOMETER"] = \
        Mul(Rat(Int(9), Mul(Int(3175), Pow(10, 15))), Sym("pt"))
    CONVERSIONS["POINT:ZETTAMETER"] = \
        Mul(Rat(Mul(Int(36), Pow(10, 25)), Int(127)), Sym("pt"))
    CONVERSIONS["TERAMETER:ANGSTROM"] = \
        Mul(Rat(Int(1), Pow(10, 22)), Sym("teram"))
    CONVERSIONS["TERAMETER:ASTRONMICALUNIT"] = \
        Mul(Rat(Int(1495978707), Pow(10, 10)), Sym("teram"))
    CONVERSIONS["TERAMETER:ATTOMETER"] = \
        Mul(Rat(Int(1), Pow(10, 30)), Sym("teram"))
    CONVERSIONS["TERAMETER:CENTIMETER"] = \
        Mul(Rat(Int(1), Pow(10, 14)), Sym("teram"))
    CONVERSIONS["TERAMETER:DECAMETER"] = \
        Mul(Rat(Int(1), Pow(10, 11)), Sym("teram"))
    CONVERSIONS["TERAMETER:DECIMETER"] = \
        Mul(Rat(Int(1), Pow(10, 13)), Sym("teram"))
    CONVERSIONS["TERAMETER:EXAMETER"] = \
        Mul(Pow(10, 6), Sym("teram"))
    CONVERSIONS["TERAMETER:FEMTOMETER"] = \
        Mul(Rat(Int(1), Pow(10, 27)), Sym("teram"))
    CONVERSIONS["TERAMETER:FOOT"] = \
        Mul(Rat(Int(381), Mul(Int(125), Pow(10, 13))), Sym("teram"))
    CONVERSIONS["TERAMETER:GIGAMETER"] = \
        Mul(Rat(Int(1), Int(1000)), Sym("teram"))
    CONVERSIONS["TERAMETER:HECTOMETER"] = \
        Mul(Rat(Int(1), Pow(10, 10)), Sym("teram"))
    CONVERSIONS["TERAMETER:INCH"] = \
        Mul(Rat(Int(127), Mul(Int(5), Pow(10, 15))), Sym("teram"))
    CONVERSIONS["TERAMETER:KILOMETER"] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("teram"))
    CONVERSIONS["TERAMETER:LIGHTYEAR"] = \
        Mul(Rat(Int("5912956545363"), Mul(Int(625), Pow(10, 6))), Sym("teram"))
    CONVERSIONS["TERAMETER:LINE"] = \
        Mul(Rat(Int(127), Mul(Int(6), Pow(10, 16))), Sym("teram"))
    CONVERSIONS["TERAMETER:MEGAMETER"] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("teram"))
    CONVERSIONS["TERAMETER:METER"] = \
        Mul(Rat(Int(1), Pow(10, 12)), Sym("teram"))
    CONVERSIONS["TERAMETER:MICROMETER"] = \
        Mul(Rat(Int(1), Pow(10, 18)), Sym("teram"))
    CONVERSIONS["TERAMETER:MILE"] = \
        Mul(Rat(Int(12573), Mul(Int(78125), Pow(10, 8))), Sym("teram"))
    CONVERSIONS["TERAMETER:MILLIMETER"] = \
        Mul(Rat(Int(1), Pow(10, 15)), Sym("teram"))
    CONVERSIONS["TERAMETER:NANOMETER"] = \
        Mul(Rat(Int(1), Pow(10, 21)), Sym("teram"))
    CONVERSIONS["TERAMETER:PARSEC"] = \
        Mul(Rat(Int(3857097), Int(125)), Sym("teram"))
    CONVERSIONS["TERAMETER:PETAMETER"] = \
        Mul(Int(1000), Sym("teram"))
    CONVERSIONS["TERAMETER:PICOMETER"] = \
        Mul(Rat(Int(1), Pow(10, 24)), Sym("teram"))
    CONVERSIONS["TERAMETER:POINT"] = \
        Mul(Rat(Int(127), Mul(Int(36), Pow(10, 16))), Sym("teram"))
    CONVERSIONS["TERAMETER:THOU"] = \
        Mul(Rat(Int(127), Mul(Int(5), Pow(10, 18))), Sym("teram"))
    CONVERSIONS["TERAMETER:YARD"] = \
        Mul(Rat(Int(1143), Mul(Int(125), Pow(10, 13))), Sym("teram"))
    CONVERSIONS["TERAMETER:YOCTOMETER"] = \
        Mul(Rat(Int(1), Pow(10, 36)), Sym("teram"))
    CONVERSIONS["TERAMETER:YOTTAMETER"] = \
        Mul(Pow(10, 12), Sym("teram"))
    CONVERSIONS["TERAMETER:ZEPTOMETER"] = \
        Mul(Rat(Int(1), Pow(10, 33)), Sym("teram"))
    CONVERSIONS["TERAMETER:ZETTAMETER"] = \
        Mul(Pow(10, 9), Sym("teram"))
    CONVERSIONS["THOU:ANGSTROM"] = \
        Mul(Rat(Int(1), Int(254000)), Sym("thou"))
    CONVERSIONS["THOU:ASTRONMICALUNIT"] = \
        Mul(Rat(Mul(Int("7479893535"), Pow(10, 8)), Int(127)), Sym("thou"))
    CONVERSIONS["THOU:ATTOMETER"] = \
        Mul(Rat(Int(1), Mul(Int(254), Pow(10, 11))), Sym("thou"))
    CONVERSIONS["THOU:CENTIMETER"] = \
        Mul(Rat(Mul(Int(5), Pow(10, 4)), Int(127)), Sym("thou"))
    CONVERSIONS["THOU:DECAMETER"] = \
        Mul(Rat(Mul(Int(5), Pow(10, 7)), Int(127)), Sym("thou"))
    CONVERSIONS["THOU:DECIMETER"] = \
        Mul(Rat(Mul(Int(5), Pow(10, 5)), Int(127)), Sym("thou"))
    CONVERSIONS["THOU:EXAMETER"] = \
        Mul(Rat(Mul(Int(5), Pow(10, 24)), Int(127)), Sym("thou"))
    CONVERSIONS["THOU:FEMTOMETER"] = \
        Mul(Rat(Int(1), Mul(Int(254), Pow(10, 8))), Sym("thou"))
    CONVERSIONS["THOU:FOOT"] = \
        Mul(Int(12000), Sym("thou"))
    CONVERSIONS["THOU:GIGAMETER"] = \
        Mul(Rat(Mul(Int(5), Pow(10, 15)), Int(127)), Sym("thou"))
    CONVERSIONS["THOU:HECTOMETER"] = \
        Mul(Rat(Mul(Int(5), Pow(10, 8)), Int(127)), Sym("thou"))
    CONVERSIONS["THOU:INCH"] = \
        Mul(Int(1000), Sym("thou"))
    CONVERSIONS["THOU:KILOMETER"] = \
        Mul(Rat(Mul(Int(5), Pow(10, 9)), Int(127)), Sym("thou"))
    CONVERSIONS["THOU:LIGHTYEAR"] = \
        Mul(Rat(Mul(Int("47303652362904"), Pow(10, 9)), Int(127)), Sym("thou"))
    CONVERSIONS["THOU:LINE"] = \
        Mul(Rat(Int(250), Int(3)), Sym("thou"))
    CONVERSIONS["THOU:MEGAMETER"] = \
        Mul(Rat(Mul(Int(5), Pow(10, 12)), Int(127)), Sym("thou"))
    CONVERSIONS["THOU:METER"] = \
        Mul(Rat(Mul(Int(5), Pow(10, 6)), Int(127)), Sym("thou"))
    CONVERSIONS["THOU:MICROMETER"] = \
        Mul(Rat(Int(5), Int(127)), Sym("thou"))
    CONVERSIONS["THOU:MILE"] = \
        Mul(Mul(Int(6336), Pow(10, 4)), Sym("thou"))
    CONVERSIONS["THOU:MILLIMETER"] = \
        Mul(Rat(Int(5000), Int(127)), Sym("thou"))
    CONVERSIONS["THOU:NANOMETER"] = \
        Mul(Rat(Int(1), Int(25400)), Sym("thou"))
    CONVERSIONS["THOU:PARSEC"] = \
        Mul(Rat(Mul(Int(15428388), Pow(10, 16)), Int(127)), Sym("thou"))
    CONVERSIONS["THOU:PETAMETER"] = \
        Mul(Rat(Mul(Int(5), Pow(10, 21)), Int(127)), Sym("thou"))
    CONVERSIONS["THOU:PICOMETER"] = \
        Mul(Rat(Int(1), Mul(Int(254), Pow(10, 5))), Sym("thou"))
    CONVERSIONS["THOU:POINT"] = \
        Mul(Rat(Int(125), Int(9)), Sym("thou"))
    CONVERSIONS["THOU:TERAMETER"] = \
        Mul(Rat(Mul(Int(5), Pow(10, 18)), Int(127)), Sym("thou"))
    CONVERSIONS["THOU:YARD"] = \
        Mul(Int(36000), Sym("thou"))
    CONVERSIONS["THOU:YOCTOMETER"] = \
        Mul(Rat(Int(1), Mul(Int(254), Pow(10, 17))), Sym("thou"))
    CONVERSIONS["THOU:YOTTAMETER"] = \
        Mul(Rat(Mul(Int(5), Pow(10, 30)), Int(127)), Sym("thou"))
    CONVERSIONS["THOU:ZEPTOMETER"] = \
        Mul(Rat(Int(1), Mul(Int(254), Pow(10, 14))), Sym("thou"))
    CONVERSIONS["THOU:ZETTAMETER"] = \
        Mul(Rat(Mul(Int(5), Pow(10, 27)), Int(127)), Sym("thou"))
    CONVERSIONS["YARD:ANGSTROM"] = \
        Mul(Rat(Int(1), Mul(Int(9144), Pow(10, 6))), Sym("yd"))
    CONVERSIONS["YARD:ASTRONMICALUNIT"] = \
        Mul(Rat(Int("62332446125000"), Int(381)), Sym("yd"))
    CONVERSIONS["YARD:ATTOMETER"] = \
        Mul(Rat(Int(1), Mul(Int(9144), Pow(10, 14))), Sym("yd"))
    CONVERSIONS["YARD:CENTIMETER"] = \
        Mul(Rat(Int(25), Int(2286)), Sym("yd"))
    CONVERSIONS["YARD:DECAMETER"] = \
        Mul(Rat(Int(12500), Int(1143)), Sym("yd"))
    CONVERSIONS["YARD:DECIMETER"] = \
        Mul(Rat(Int(125), Int(1143)), Sym("yd"))
    CONVERSIONS["YARD:EXAMETER"] = \
        Mul(Rat(Mul(Int(125), Pow(10, 19)), Int(1143)), Sym("yd"))
    CONVERSIONS["YARD:FEMTOMETER"] = \
        Mul(Rat(Int(1), Mul(Int(9144), Pow(10, 11))), Sym("yd"))
    CONVERSIONS["YARD:FOOT"] = \
        Mul(Rat(Int(1), Int(3)), Sym("yd"))
    CONVERSIONS["YARD:GIGAMETER"] = \
        Mul(Rat(Mul(Int(125), Pow(10, 10)), Int(1143)), Sym("yd"))
    CONVERSIONS["YARD:HECTOMETER"] = \
        Mul(Rat(Int(125000), Int(1143)), Sym("yd"))
    CONVERSIONS["YARD:INCH"] = \
        Mul(Rat(Int(1), Int(36)), Sym("yd"))
    CONVERSIONS["YARD:KILOMETER"] = \
        Mul(Rat(Mul(Int(125), Pow(10, 4)), Int(1143)), Sym("yd"))
    CONVERSIONS["YARD:LIGHTYEAR"] = \
        Mul(Rat(Mul(Int("1313990343414"), Pow(10, 6)), Int(127)), Sym("yd"))
    CONVERSIONS["YARD:LINE"] = \
        Mul(Rat(Int(1), Int(432)), Sym("yd"))
    CONVERSIONS["YARD:MEGAMETER"] = \
        Mul(Rat(Mul(Int(125), Pow(10, 7)), Int(1143)), Sym("yd"))
    CONVERSIONS["YARD:METER"] = \
        Mul(Rat(Int(1250), Int(1143)), Sym("yd"))
    CONVERSIONS["YARD:MICROMETER"] = \
        Mul(Rat(Int(1), Int(914400)), Sym("yd"))
    CONVERSIONS["YARD:MILE"] = \
        Mul(Int(1760), Sym("yd"))
    CONVERSIONS["YARD:MILLIMETER"] = \
        Mul(Rat(Int(5), Int(4572)), Sym("yd"))
    CONVERSIONS["YARD:NANOMETER"] = \
        Mul(Rat(Int(1), Mul(Int(9144), Pow(10, 5))), Sym("yd"))
    CONVERSIONS["YARD:PARSEC"] = \
        Mul(Rat(Mul(Int(1285699), Pow(10, 13)), Int(381)), Sym("yd"))
    CONVERSIONS["YARD:PETAMETER"] = \
        Mul(Rat(Mul(Int(125), Pow(10, 16)), Int(1143)), Sym("yd"))
    CONVERSIONS["YARD:PICOMETER"] = \
        Mul(Rat(Int(1), Mul(Int(9144), Pow(10, 8))), Sym("yd"))
    CONVERSIONS["YARD:POINT"] = \
        Mul(Rat(Int(1), Int(2592)), Sym("yd"))
    CONVERSIONS["YARD:TERAMETER"] = \
        Mul(Rat(Mul(Int(125), Pow(10, 13)), Int(1143)), Sym("yd"))
    CONVERSIONS["YARD:THOU"] = \
        Mul(Rat(Int(1), Int(36000)), Sym("yd"))
    CONVERSIONS["YARD:YOCTOMETER"] = \
        Mul(Rat(Int(1), Mul(Int(9144), Pow(10, 20))), Sym("yd"))
    CONVERSIONS["YARD:YOTTAMETER"] = \
        Mul(Rat(Mul(Int(125), Pow(10, 25)), Int(1143)), Sym("yd"))
    CONVERSIONS["YARD:ZEPTOMETER"] = \
        Mul(Rat(Int(1), Mul(Int(9144), Pow(10, 17))), Sym("yd"))
    CONVERSIONS["YARD:ZETTAMETER"] = \
        Mul(Rat(Mul(Int(125), Pow(10, 22)), Int(1143)), Sym("yd"))
    CONVERSIONS["YOCTOMETER:ANGSTROM"] = \
        Mul(Pow(10, 14), Sym("yoctom"))
    CONVERSIONS["YOCTOMETER:ASTRONMICALUNIT"] = \
        Mul(Mul(Int(1495978707), Pow(10, 26)), Sym("yoctom"))
    CONVERSIONS["YOCTOMETER:ATTOMETER"] = \
        Mul(Pow(10, 6), Sym("yoctom"))
    CONVERSIONS["YOCTOMETER:CENTIMETER"] = \
        Mul(Pow(10, 22), Sym("yoctom"))
    CONVERSIONS["YOCTOMETER:DECAMETER"] = \
        Mul(Pow(10, 25), Sym("yoctom"))
    CONVERSIONS["YOCTOMETER:DECIMETER"] = \
        Mul(Pow(10, 23), Sym("yoctom"))
    CONVERSIONS["YOCTOMETER:EXAMETER"] = \
        Mul(Pow(10, 42), Sym("yoctom"))
    CONVERSIONS["YOCTOMETER:FEMTOMETER"] = \
        Mul(Pow(10, 9), Sym("yoctom"))
    CONVERSIONS["YOCTOMETER:FOOT"] = \
        Mul(Mul(Int(3048), Pow(10, 20)), Sym("yoctom"))
    CONVERSIONS["YOCTOMETER:GIGAMETER"] = \
        Mul(Pow(10, 33), Sym("yoctom"))
    CONVERSIONS["YOCTOMETER:HECTOMETER"] = \
        Mul(Pow(10, 26), Sym("yoctom"))
    CONVERSIONS["YOCTOMETER:INCH"] = \
        Mul(Mul(Int(254), Pow(10, 20)), Sym("yoctom"))
    CONVERSIONS["YOCTOMETER:KILOMETER"] = \
        Mul(Pow(10, 27), Sym("yoctom"))
    CONVERSIONS["YOCTOMETER:LIGHTYEAR"] = \
        Mul(Mul(Int("94607304725808"), Pow(10, 26)), Sym("yoctom"))
    CONVERSIONS["YOCTOMETER:LINE"] = \
        Mul(Rat(Mul(Int(635), Pow(10, 19)), Int(3)), Sym("yoctom"))
    CONVERSIONS["YOCTOMETER:MEGAMETER"] = \
        Mul(Pow(10, 30), Sym("yoctom"))
    CONVERSIONS["YOCTOMETER:METER"] = \
        Mul(Pow(10, 24), Sym("yoctom"))
    CONVERSIONS["YOCTOMETER:MICROMETER"] = \
        Mul(Pow(10, 18), Sym("yoctom"))
    CONVERSIONS["YOCTOMETER:MILE"] = \
        Mul(Mul(Int(1609344), Pow(10, 21)), Sym("yoctom"))
    CONVERSIONS["YOCTOMETER:MILLIMETER"] = \
        Mul(Pow(10, 21), Sym("yoctom"))
    CONVERSIONS["YOCTOMETER:NANOMETER"] = \
        Mul(Pow(10, 15), Sym("yoctom"))
    CONVERSIONS["YOCTOMETER:PARSEC"] = \
        Mul(Mul(Int(30856776), Pow(10, 33)), Sym("yoctom"))
    CONVERSIONS["YOCTOMETER:PETAMETER"] = \
        Mul(Pow(10, 39), Sym("yoctom"))
    CONVERSIONS["YOCTOMETER:PICOMETER"] = \
        Mul(Pow(10, 12), Sym("yoctom"))
    CONVERSIONS["YOCTOMETER:POINT"] = \
        Mul(Rat(Mul(Int(3175), Pow(10, 18)), Int(9)), Sym("yoctom"))
    CONVERSIONS["YOCTOMETER:TERAMETER"] = \
        Mul(Pow(10, 36), Sym("yoctom"))
    CONVERSIONS["YOCTOMETER:THOU"] = \
        Mul(Mul(Int(254), Pow(10, 17)), Sym("yoctom"))
    CONVERSIONS["YOCTOMETER:YARD"] = \
        Mul(Mul(Int(9144), Pow(10, 20)), Sym("yoctom"))
    CONVERSIONS["YOCTOMETER:YOTTAMETER"] = \
        Mul(Pow(10, 48), Sym("yoctom"))
    CONVERSIONS["YOCTOMETER:ZEPTOMETER"] = \
        Mul(Int(1000), Sym("yoctom"))
    CONVERSIONS["YOCTOMETER:ZETTAMETER"] = \
        Mul(Pow(10, 45), Sym("yoctom"))
    CONVERSIONS["YOTTAMETER:ANGSTROM"] = \
        Mul(Rat(Int(1), Pow(10, 34)), Sym("yottam"))
    CONVERSIONS["YOTTAMETER:ASTRONMICALUNIT"] = \
        Mul(Rat(Int(1495978707), Pow(10, 22)), Sym("yottam"))
    CONVERSIONS["YOTTAMETER:ATTOMETER"] = \
        Mul(Rat(Int(1), Pow(10, 42)), Sym("yottam"))
    CONVERSIONS["YOTTAMETER:CENTIMETER"] = \
        Mul(Rat(Int(1), Pow(10, 26)), Sym("yottam"))
    CONVERSIONS["YOTTAMETER:DECAMETER"] = \
        Mul(Rat(Int(1), Pow(10, 23)), Sym("yottam"))
    CONVERSIONS["YOTTAMETER:DECIMETER"] = \
        Mul(Rat(Int(1), Pow(10, 25)), Sym("yottam"))
    CONVERSIONS["YOTTAMETER:EXAMETER"] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("yottam"))
    CONVERSIONS["YOTTAMETER:FEMTOMETER"] = \
        Mul(Rat(Int(1), Pow(10, 39)), Sym("yottam"))
    CONVERSIONS["YOTTAMETER:FOOT"] = \
        Mul(Rat(Int(381), Mul(Int(125), Pow(10, 25))), Sym("yottam"))
    CONVERSIONS["YOTTAMETER:GIGAMETER"] = \
        Mul(Rat(Int(1), Pow(10, 15)), Sym("yottam"))
    CONVERSIONS["YOTTAMETER:HECTOMETER"] = \
        Mul(Rat(Int(1), Pow(10, 22)), Sym("yottam"))
    CONVERSIONS["YOTTAMETER:INCH"] = \
        Mul(Rat(Int(127), Mul(Int(5), Pow(10, 27))), Sym("yottam"))
    CONVERSIONS["YOTTAMETER:KILOMETER"] = \
        Mul(Rat(Int(1), Pow(10, 21)), Sym("yottam"))
    CONVERSIONS["YOTTAMETER:LIGHTYEAR"] = \
        Mul(Rat(Int("5912956545363"), Mul(Int(625), Pow(10, 18))), Sym("yottam"))
    CONVERSIONS["YOTTAMETER:LINE"] = \
        Mul(Rat(Int(127), Mul(Int(6), Pow(10, 28))), Sym("yottam"))
    CONVERSIONS["YOTTAMETER:MEGAMETER"] = \
        Mul(Rat(Int(1), Pow(10, 18)), Sym("yottam"))
    CONVERSIONS["YOTTAMETER:METER"] = \
        Mul(Rat(Int(1), Pow(10, 24)), Sym("yottam"))
    CONVERSIONS["YOTTAMETER:MICROMETER"] = \
        Mul(Rat(Int(1), Pow(10, 30)), Sym("yottam"))
    CONVERSIONS["YOTTAMETER:MILE"] = \
        Mul(Rat(Int(12573), Mul(Int(78125), Pow(10, 20))), Sym("yottam"))
    CONVERSIONS["YOTTAMETER:MILLIMETER"] = \
        Mul(Rat(Int(1), Pow(10, 27)), Sym("yottam"))
    CONVERSIONS["YOTTAMETER:NANOMETER"] = \
        Mul(Rat(Int(1), Pow(10, 33)), Sym("yottam"))
    CONVERSIONS["YOTTAMETER:PARSEC"] = \
        Mul(Rat(Int(3857097), Mul(Int(125), Pow(10, 12))), Sym("yottam"))
    CONVERSIONS["YOTTAMETER:PETAMETER"] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("yottam"))
    CONVERSIONS["YOTTAMETER:PICOMETER"] = \
        Mul(Rat(Int(1), Pow(10, 36)), Sym("yottam"))
    CONVERSIONS["YOTTAMETER:POINT"] = \
        Mul(Rat(Int(127), Mul(Int(36), Pow(10, 28))), Sym("yottam"))
    CONVERSIONS["YOTTAMETER:TERAMETER"] = \
        Mul(Rat(Int(1), Pow(10, 12)), Sym("yottam"))
    CONVERSIONS["YOTTAMETER:THOU"] = \
        Mul(Rat(Int(127), Mul(Int(5), Pow(10, 30))), Sym("yottam"))
    CONVERSIONS["YOTTAMETER:YARD"] = \
        Mul(Rat(Int(1143), Mul(Int(125), Pow(10, 25))), Sym("yottam"))
    CONVERSIONS["YOTTAMETER:YOCTOMETER"] = \
        Mul(Rat(Int(1), Pow(10, 48)), Sym("yottam"))
    CONVERSIONS["YOTTAMETER:ZEPTOMETER"] = \
        Mul(Rat(Int(1), Pow(10, 45)), Sym("yottam"))
    CONVERSIONS["YOTTAMETER:ZETTAMETER"] = \
        Mul(Rat(Int(1), Int(1000)), Sym("yottam"))
    CONVERSIONS["ZEPTOMETER:ANGSTROM"] = \
        Mul(Pow(10, 11), Sym("zeptom"))
    CONVERSIONS["ZEPTOMETER:ASTRONMICALUNIT"] = \
        Mul(Mul(Int(1495978707), Pow(10, 23)), Sym("zeptom"))
    CONVERSIONS["ZEPTOMETER:ATTOMETER"] = \
        Mul(Int(1000), Sym("zeptom"))
    CONVERSIONS["ZEPTOMETER:CENTIMETER"] = \
        Mul(Pow(10, 19), Sym("zeptom"))
    CONVERSIONS["ZEPTOMETER:DECAMETER"] = \
        Mul(Pow(10, 22), Sym("zeptom"))
    CONVERSIONS["ZEPTOMETER:DECIMETER"] = \
        Mul(Pow(10, 20), Sym("zeptom"))
    CONVERSIONS["ZEPTOMETER:EXAMETER"] = \
        Mul(Pow(10, 39), Sym("zeptom"))
    CONVERSIONS["ZEPTOMETER:FEMTOMETER"] = \
        Mul(Pow(10, 6), Sym("zeptom"))
    CONVERSIONS["ZEPTOMETER:FOOT"] = \
        Mul(Mul(Int(3048), Pow(10, 17)), Sym("zeptom"))
    CONVERSIONS["ZEPTOMETER:GIGAMETER"] = \
        Mul(Pow(10, 30), Sym("zeptom"))
    CONVERSIONS["ZEPTOMETER:HECTOMETER"] = \
        Mul(Pow(10, 23), Sym("zeptom"))
    CONVERSIONS["ZEPTOMETER:INCH"] = \
        Mul(Mul(Int(254), Pow(10, 17)), Sym("zeptom"))
    CONVERSIONS["ZEPTOMETER:KILOMETER"] = \
        Mul(Pow(10, 24), Sym("zeptom"))
    CONVERSIONS["ZEPTOMETER:LIGHTYEAR"] = \
        Mul(Mul(Int("94607304725808"), Pow(10, 23)), Sym("zeptom"))
    CONVERSIONS["ZEPTOMETER:LINE"] = \
        Mul(Rat(Mul(Int(635), Pow(10, 16)), Int(3)), Sym("zeptom"))
    CONVERSIONS["ZEPTOMETER:MEGAMETER"] = \
        Mul(Pow(10, 27), Sym("zeptom"))
    CONVERSIONS["ZEPTOMETER:METER"] = \
        Mul(Pow(10, 21), Sym("zeptom"))
    CONVERSIONS["ZEPTOMETER:MICROMETER"] = \
        Mul(Pow(10, 15), Sym("zeptom"))
    CONVERSIONS["ZEPTOMETER:MILE"] = \
        Mul(Mul(Int(1609344), Pow(10, 18)), Sym("zeptom"))
    CONVERSIONS["ZEPTOMETER:MILLIMETER"] = \
        Mul(Pow(10, 18), Sym("zeptom"))
    CONVERSIONS["ZEPTOMETER:NANOMETER"] = \
        Mul(Pow(10, 12), Sym("zeptom"))
    CONVERSIONS["ZEPTOMETER:PARSEC"] = \
        Mul(Mul(Int(30856776), Pow(10, 30)), Sym("zeptom"))
    CONVERSIONS["ZEPTOMETER:PETAMETER"] = \
        Mul(Pow(10, 36), Sym("zeptom"))
    CONVERSIONS["ZEPTOMETER:PICOMETER"] = \
        Mul(Pow(10, 9), Sym("zeptom"))
    CONVERSIONS["ZEPTOMETER:POINT"] = \
        Mul(Rat(Mul(Int(3175), Pow(10, 15)), Int(9)), Sym("zeptom"))
    CONVERSIONS["ZEPTOMETER:TERAMETER"] = \
        Mul(Pow(10, 33), Sym("zeptom"))
    CONVERSIONS["ZEPTOMETER:THOU"] = \
        Mul(Mul(Int(254), Pow(10, 14)), Sym("zeptom"))
    CONVERSIONS["ZEPTOMETER:YARD"] = \
        Mul(Mul(Int(9144), Pow(10, 17)), Sym("zeptom"))
    CONVERSIONS["ZEPTOMETER:YOCTOMETER"] = \
        Mul(Rat(Int(1), Int(1000)), Sym("zeptom"))
    CONVERSIONS["ZEPTOMETER:YOTTAMETER"] = \
        Mul(Pow(10, 45), Sym("zeptom"))
    CONVERSIONS["ZEPTOMETER:ZETTAMETER"] = \
        Mul(Pow(10, 42), Sym("zeptom"))
    CONVERSIONS["ZETTAMETER:ANGSTROM"] = \
        Mul(Rat(Int(1), Pow(10, 31)), Sym("zettam"))
    CONVERSIONS["ZETTAMETER:ASTRONMICALUNIT"] = \
        Mul(Rat(Int(1495978707), Pow(10, 19)), Sym("zettam"))
    CONVERSIONS["ZETTAMETER:ATTOMETER"] = \
        Mul(Rat(Int(1), Pow(10, 39)), Sym("zettam"))
    CONVERSIONS["ZETTAMETER:CENTIMETER"] = \
        Mul(Rat(Int(1), Pow(10, 23)), Sym("zettam"))
    CONVERSIONS["ZETTAMETER:DECAMETER"] = \
        Mul(Rat(Int(1), Pow(10, 20)), Sym("zettam"))
    CONVERSIONS["ZETTAMETER:DECIMETER"] = \
        Mul(Rat(Int(1), Pow(10, 22)), Sym("zettam"))
    CONVERSIONS["ZETTAMETER:EXAMETER"] = \
        Mul(Rat(Int(1), Int(1000)), Sym("zettam"))
    CONVERSIONS["ZETTAMETER:FEMTOMETER"] = \
        Mul(Rat(Int(1), Pow(10, 36)), Sym("zettam"))
    CONVERSIONS["ZETTAMETER:FOOT"] = \
        Mul(Rat(Int(381), Mul(Int(125), Pow(10, 22))), Sym("zettam"))
    CONVERSIONS["ZETTAMETER:GIGAMETER"] = \
        Mul(Rat(Int(1), Pow(10, 12)), Sym("zettam"))
    CONVERSIONS["ZETTAMETER:HECTOMETER"] = \
        Mul(Rat(Int(1), Pow(10, 19)), Sym("zettam"))
    CONVERSIONS["ZETTAMETER:INCH"] = \
        Mul(Rat(Int(127), Mul(Int(5), Pow(10, 24))), Sym("zettam"))
    CONVERSIONS["ZETTAMETER:KILOMETER"] = \
        Mul(Rat(Int(1), Pow(10, 18)), Sym("zettam"))
    CONVERSIONS["ZETTAMETER:LIGHTYEAR"] = \
        Mul(Rat(Int("5912956545363"), Mul(Int(625), Pow(10, 15))), Sym("zettam"))
    CONVERSIONS["ZETTAMETER:LINE"] = \
        Mul(Rat(Int(127), Mul(Int(6), Pow(10, 25))), Sym("zettam"))
    CONVERSIONS["ZETTAMETER:MEGAMETER"] = \
        Mul(Rat(Int(1), Pow(10, 15)), Sym("zettam"))
    CONVERSIONS["ZETTAMETER:METER"] = \
        Mul(Rat(Int(1), Pow(10, 21)), Sym("zettam"))
    CONVERSIONS["ZETTAMETER:MICROMETER"] = \
        Mul(Rat(Int(1), Pow(10, 27)), Sym("zettam"))
    CONVERSIONS["ZETTAMETER:MILE"] = \
        Mul(Rat(Int(12573), Mul(Int(78125), Pow(10, 17))), Sym("zettam"))
    CONVERSIONS["ZETTAMETER:MILLIMETER"] = \
        Mul(Rat(Int(1), Pow(10, 24)), Sym("zettam"))
    CONVERSIONS["ZETTAMETER:NANOMETER"] = \
        Mul(Rat(Int(1), Pow(10, 30)), Sym("zettam"))
    CONVERSIONS["ZETTAMETER:PARSEC"] = \
        Mul(Rat(Int(3857097), Mul(Int(125), Pow(10, 9))), Sym("zettam"))
    CONVERSIONS["ZETTAMETER:PETAMETER"] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("zettam"))
    CONVERSIONS["ZETTAMETER:PICOMETER"] = \
        Mul(Rat(Int(1), Pow(10, 33)), Sym("zettam"))
    CONVERSIONS["ZETTAMETER:POINT"] = \
        Mul(Rat(Int(127), Mul(Int(36), Pow(10, 25))), Sym("zettam"))
    CONVERSIONS["ZETTAMETER:TERAMETER"] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("zettam"))
    CONVERSIONS["ZETTAMETER:THOU"] = \
        Mul(Rat(Int(127), Mul(Int(5), Pow(10, 27))), Sym("zettam"))
    CONVERSIONS["ZETTAMETER:YARD"] = \
        Mul(Rat(Int(1143), Mul(Int(125), Pow(10, 22))), Sym("zettam"))
    CONVERSIONS["ZETTAMETER:YOCTOMETER"] = \
        Mul(Rat(Int(1), Pow(10, 45)), Sym("zettam"))
    CONVERSIONS["ZETTAMETER:YOTTAMETER"] = \
        Mul(Int(1000), Sym("zettam"))
    CONVERSIONS["ZETTAMETER:ZEPTOMETER"] = \
        Mul(Rat(Int(1), Pow(10, 42)), Sym("zettam"))

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
