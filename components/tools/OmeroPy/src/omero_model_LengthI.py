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

from omero.model.conversions import Add  # nopep8
from omero.model.conversions import Int  # nopep8
from omero.model.conversions import Mul  # nopep8
from omero.model.conversions import Pow  # nopep8
from omero.model.conversions import Rat  # nopep8
from omero.model.conversions import Sym  # nopep8


class LengthI(_omero_model.Length, UnitBase):

    CONVERSIONS = dict()
    CONVERSIONS["ANGSTROM:ASTRONOMICALUNIT"] = \
        Mul(Mul(Int(1495978707), Pow(10, 12)), Sym("ang"))  # nopep8
    CONVERSIONS["ANGSTROM:ATTOMETER"] = \
        Mul(Rat(Int(1), Pow(10, 8)), Sym("ang"))  # nopep8
    CONVERSIONS["ANGSTROM:CENTIMETER"] = \
        Mul(Pow(10, 8), Sym("ang"))  # nopep8
    CONVERSIONS["ANGSTROM:DECAMETER"] = \
        Mul(Pow(10, 11), Sym("ang"))  # nopep8
    CONVERSIONS["ANGSTROM:DECIMETER"] = \
        Mul(Pow(10, 9), Sym("ang"))  # nopep8
    CONVERSIONS["ANGSTROM:EXAMETER"] = \
        Mul(Pow(10, 28), Sym("ang"))  # nopep8
    CONVERSIONS["ANGSTROM:FEMTOMETER"] = \
        Mul(Rat(Int(1), Pow(10, 5)), Sym("ang"))  # nopep8
    CONVERSIONS["ANGSTROM:FOOT"] = \
        Mul(Mul(Int(3048), Pow(10, 6)), Sym("ang"))  # nopep8
    CONVERSIONS["ANGSTROM:GIGAMETER"] = \
        Mul(Pow(10, 19), Sym("ang"))  # nopep8
    CONVERSIONS["ANGSTROM:HECTOMETER"] = \
        Mul(Pow(10, 12), Sym("ang"))  # nopep8
    CONVERSIONS["ANGSTROM:INCH"] = \
        Mul(Mul(Int(254), Pow(10, 6)), Sym("ang"))  # nopep8
    CONVERSIONS["ANGSTROM:KILOMETER"] = \
        Mul(Pow(10, 13), Sym("ang"))  # nopep8
    CONVERSIONS["ANGSTROM:LIGHTYEAR"] = \
        Mul(Mul(Int("94607304725808"), Pow(10, 12)), Sym("ang"))  # nopep8
    CONVERSIONS["ANGSTROM:LINE"] = \
        Mul(Rat(Mul(Int(635), Pow(10, 5)), Int(3)), Sym("ang"))  # nopep8
    CONVERSIONS["ANGSTROM:MEGAMETER"] = \
        Mul(Pow(10, 16), Sym("ang"))  # nopep8
    CONVERSIONS["ANGSTROM:METER"] = \
        Mul(Pow(10, 10), Sym("ang"))  # nopep8
    CONVERSIONS["ANGSTROM:MICROMETER"] = \
        Mul(Pow(10, 4), Sym("ang"))  # nopep8
    CONVERSIONS["ANGSTROM:MILE"] = \
        Mul(Mul(Int(1609344), Pow(10, 7)), Sym("ang"))  # nopep8
    CONVERSIONS["ANGSTROM:MILLIMETER"] = \
        Mul(Pow(10, 7), Sym("ang"))  # nopep8
    CONVERSIONS["ANGSTROM:NANOMETER"] = \
        Mul(Int(10), Sym("ang"))  # nopep8
    CONVERSIONS["ANGSTROM:PARSEC"] = \
        Mul(Mul(Int(30856776), Pow(10, 19)), Sym("ang"))  # nopep8
    CONVERSIONS["ANGSTROM:PETAMETER"] = \
        Mul(Pow(10, 25), Sym("ang"))  # nopep8
    CONVERSIONS["ANGSTROM:PICOMETER"] = \
        Mul(Rat(Int(1), Int(100)), Sym("ang"))  # nopep8
    CONVERSIONS["ANGSTROM:POINT"] = \
        Mul(Rat(Mul(Int(3175), Pow(10, 4)), Int(9)), Sym("ang"))  # nopep8
    CONVERSIONS["ANGSTROM:TERAMETER"] = \
        Mul(Pow(10, 22), Sym("ang"))  # nopep8
    CONVERSIONS["ANGSTROM:THOU"] = \
        Mul(Int(254000), Sym("ang"))  # nopep8
    CONVERSIONS["ANGSTROM:YARD"] = \
        Mul(Mul(Int(9144), Pow(10, 6)), Sym("ang"))  # nopep8
    CONVERSIONS["ANGSTROM:YOCTOMETER"] = \
        Mul(Rat(Int(1), Pow(10, 14)), Sym("ang"))  # nopep8
    CONVERSIONS["ANGSTROM:YOTTAMETER"] = \
        Mul(Pow(10, 34), Sym("ang"))  # nopep8
    CONVERSIONS["ANGSTROM:ZEPTOMETER"] = \
        Mul(Rat(Int(1), Pow(10, 11)), Sym("ang"))  # nopep8
    CONVERSIONS["ANGSTROM:ZETTAMETER"] = \
        Mul(Pow(10, 31), Sym("ang"))  # nopep8
    CONVERSIONS["ASTRONOMICALUNIT:ANGSTROM"] = \
        Mul(Rat(Int(1), Mul(Int(1495978707), Pow(10, 12))), Sym("ua"))  # nopep8
    CONVERSIONS["ASTRONOMICALUNIT:ATTOMETER"] = \
        Mul(Rat(Int(1), Mul(Int(1495978707), Pow(10, 20))), Sym("ua"))  # nopep8
    CONVERSIONS["ASTRONOMICALUNIT:CENTIMETER"] = \
        Mul(Rat(Int(1), Mul(Int(1495978707), Pow(10, 4))), Sym("ua"))  # nopep8
    CONVERSIONS["ASTRONOMICALUNIT:DECAMETER"] = \
        Mul(Rat(Int(1), Int("14959787070")), Sym("ua"))  # nopep8
    CONVERSIONS["ASTRONOMICALUNIT:DECIMETER"] = \
        Mul(Rat(Int(1), Int("1495978707000")), Sym("ua"))  # nopep8
    CONVERSIONS["ASTRONOMICALUNIT:EXAMETER"] = \
        Mul(Rat(Pow(10, 16), Int(1495978707)), Sym("ua"))  # nopep8
    CONVERSIONS["ASTRONOMICALUNIT:FEMTOMETER"] = \
        Mul(Rat(Int(1), Mul(Int(1495978707), Pow(10, 17))), Sym("ua"))  # nopep8
    CONVERSIONS["ASTRONOMICALUNIT:FOOT"] = \
        Mul(Rat(Int(127), Int("62332446125000")), Sym("ua"))  # nopep8
    CONVERSIONS["ASTRONOMICALUNIT:GIGAMETER"] = \
        Mul(Rat(Pow(10, 7), Int(1495978707)), Sym("ua"))  # nopep8
    CONVERSIONS["ASTRONOMICALUNIT:HECTOMETER"] = \
        Mul(Rat(Int(1), Int(1495978707)), Sym("ua"))  # nopep8
    CONVERSIONS["ASTRONOMICALUNIT:INCH"] = \
        Mul(Rat(Int(127), Mul(Int("7479893535"), Pow(10, 5))), Sym("ua"))  # nopep8
    CONVERSIONS["ASTRONOMICALUNIT:KILOMETER"] = \
        Mul(Rat(Int(10), Int(1495978707)), Sym("ua"))  # nopep8
    CONVERSIONS["ASTRONOMICALUNIT:LIGHTYEAR"] = \
        Mul(Rat(Int("431996825232"), Int(6830953)), Sym("ua"))  # nopep8
    CONVERSIONS["ASTRONOMICALUNIT:LINE"] = \
        Mul(Rat(Int(127), Mul(Int("8975872242"), Pow(10, 6))), Sym("ua"))  # nopep8
    CONVERSIONS["ASTRONOMICALUNIT:MEGAMETER"] = \
        Mul(Rat(Pow(10, 4), Int(1495978707)), Sym("ua"))  # nopep8
    CONVERSIONS["ASTRONOMICALUNIT:METER"] = \
        Mul(Rat(Int(1), Int("149597870700")), Sym("ua"))  # nopep8
    CONVERSIONS["ASTRONOMICALUNIT:MICROMETER"] = \
        Mul(Rat(Int(1), Mul(Int(1495978707), Pow(10, 8))), Sym("ua"))  # nopep8
    CONVERSIONS["ASTRONOMICALUNIT:MILE"] = \
        Mul(Rat(Int(16764), Int("1558311153125")), Sym("ua"))  # nopep8
    CONVERSIONS["ASTRONOMICALUNIT:MILLIMETER"] = \
        Mul(Rat(Int(1), Mul(Int(1495978707), Pow(10, 5))), Sym("ua"))  # nopep8
    CONVERSIONS["ASTRONOMICALUNIT:NANOMETER"] = \
        Mul(Rat(Int(1), Mul(Int(1495978707), Pow(10, 11))), Sym("ua"))  # nopep8
    CONVERSIONS["ASTRONOMICALUNIT:PARSEC"] = \
        Mul(Rat(Mul(Int(10285592), Pow(10, 7)), Int(498659569)), Sym("ua"))  # nopep8
    CONVERSIONS["ASTRONOMICALUNIT:PETAMETER"] = \
        Mul(Rat(Pow(10, 13), Int(1495978707)), Sym("ua"))  # nopep8
    CONVERSIONS["ASTRONOMICALUNIT:PICOMETER"] = \
        Mul(Rat(Int(1), Mul(Int(1495978707), Pow(10, 14))), Sym("ua"))  # nopep8
    CONVERSIONS["ASTRONOMICALUNIT:POINT"] = \
        Mul(Rat(Int(127), Mul(Int("53855233452"), Pow(10, 6))), Sym("ua"))  # nopep8
    CONVERSIONS["ASTRONOMICALUNIT:TERAMETER"] = \
        Mul(Rat(Pow(10, 10), Int(1495978707)), Sym("ua"))  # nopep8
    CONVERSIONS["ASTRONOMICALUNIT:THOU"] = \
        Mul(Rat(Int(127), Mul(Int("7479893535"), Pow(10, 8))), Sym("ua"))  # nopep8
    CONVERSIONS["ASTRONOMICALUNIT:YARD"] = \
        Mul(Rat(Int(381), Int("62332446125000")), Sym("ua"))  # nopep8
    CONVERSIONS["ASTRONOMICALUNIT:YOCTOMETER"] = \
        Mul(Rat(Int(1), Mul(Int(1495978707), Pow(10, 26))), Sym("ua"))  # nopep8
    CONVERSIONS["ASTRONOMICALUNIT:YOTTAMETER"] = \
        Mul(Rat(Pow(10, 22), Int(1495978707)), Sym("ua"))  # nopep8
    CONVERSIONS["ASTRONOMICALUNIT:ZEPTOMETER"] = \
        Mul(Rat(Int(1), Mul(Int(1495978707), Pow(10, 23))), Sym("ua"))  # nopep8
    CONVERSIONS["ASTRONOMICALUNIT:ZETTAMETER"] = \
        Mul(Rat(Pow(10, 19), Int(1495978707)), Sym("ua"))  # nopep8
    CONVERSIONS["ATTOMETER:ANGSTROM"] = \
        Mul(Pow(10, 8), Sym("attom"))  # nopep8
    CONVERSIONS["ATTOMETER:ASTRONOMICALUNIT"] = \
        Mul(Mul(Int(1495978707), Pow(10, 20)), Sym("attom"))  # nopep8
    CONVERSIONS["ATTOMETER:CENTIMETER"] = \
        Mul(Pow(10, 16), Sym("attom"))  # nopep8
    CONVERSIONS["ATTOMETER:DECAMETER"] = \
        Mul(Pow(10, 19), Sym("attom"))  # nopep8
    CONVERSIONS["ATTOMETER:DECIMETER"] = \
        Mul(Pow(10, 17), Sym("attom"))  # nopep8
    CONVERSIONS["ATTOMETER:EXAMETER"] = \
        Mul(Pow(10, 36), Sym("attom"))  # nopep8
    CONVERSIONS["ATTOMETER:FEMTOMETER"] = \
        Mul(Int(1000), Sym("attom"))  # nopep8
    CONVERSIONS["ATTOMETER:FOOT"] = \
        Mul(Mul(Int(3048), Pow(10, 14)), Sym("attom"))  # nopep8
    CONVERSIONS["ATTOMETER:GIGAMETER"] = \
        Mul(Pow(10, 27), Sym("attom"))  # nopep8
    CONVERSIONS["ATTOMETER:HECTOMETER"] = \
        Mul(Pow(10, 20), Sym("attom"))  # nopep8
    CONVERSIONS["ATTOMETER:INCH"] = \
        Mul(Mul(Int(254), Pow(10, 14)), Sym("attom"))  # nopep8
    CONVERSIONS["ATTOMETER:KILOMETER"] = \
        Mul(Pow(10, 21), Sym("attom"))  # nopep8
    CONVERSIONS["ATTOMETER:LIGHTYEAR"] = \
        Mul(Mul(Int("94607304725808"), Pow(10, 20)), Sym("attom"))  # nopep8
    CONVERSIONS["ATTOMETER:LINE"] = \
        Mul(Rat(Mul(Int(635), Pow(10, 13)), Int(3)), Sym("attom"))  # nopep8
    CONVERSIONS["ATTOMETER:MEGAMETER"] = \
        Mul(Pow(10, 24), Sym("attom"))  # nopep8
    CONVERSIONS["ATTOMETER:METER"] = \
        Mul(Pow(10, 18), Sym("attom"))  # nopep8
    CONVERSIONS["ATTOMETER:MICROMETER"] = \
        Mul(Pow(10, 12), Sym("attom"))  # nopep8
    CONVERSIONS["ATTOMETER:MILE"] = \
        Mul(Mul(Int(1609344), Pow(10, 15)), Sym("attom"))  # nopep8
    CONVERSIONS["ATTOMETER:MILLIMETER"] = \
        Mul(Pow(10, 15), Sym("attom"))  # nopep8
    CONVERSIONS["ATTOMETER:NANOMETER"] = \
        Mul(Pow(10, 9), Sym("attom"))  # nopep8
    CONVERSIONS["ATTOMETER:PARSEC"] = \
        Mul(Mul(Int(30856776), Pow(10, 27)), Sym("attom"))  # nopep8
    CONVERSIONS["ATTOMETER:PETAMETER"] = \
        Mul(Pow(10, 33), Sym("attom"))  # nopep8
    CONVERSIONS["ATTOMETER:PICOMETER"] = \
        Mul(Pow(10, 6), Sym("attom"))  # nopep8
    CONVERSIONS["ATTOMETER:POINT"] = \
        Mul(Rat(Mul(Int(3175), Pow(10, 12)), Int(9)), Sym("attom"))  # nopep8
    CONVERSIONS["ATTOMETER:TERAMETER"] = \
        Mul(Pow(10, 30), Sym("attom"))  # nopep8
    CONVERSIONS["ATTOMETER:THOU"] = \
        Mul(Mul(Int(254), Pow(10, 11)), Sym("attom"))  # nopep8
    CONVERSIONS["ATTOMETER:YARD"] = \
        Mul(Mul(Int(9144), Pow(10, 14)), Sym("attom"))  # nopep8
    CONVERSIONS["ATTOMETER:YOCTOMETER"] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("attom"))  # nopep8
    CONVERSIONS["ATTOMETER:YOTTAMETER"] = \
        Mul(Pow(10, 42), Sym("attom"))  # nopep8
    CONVERSIONS["ATTOMETER:ZEPTOMETER"] = \
        Mul(Rat(Int(1), Int(1000)), Sym("attom"))  # nopep8
    CONVERSIONS["ATTOMETER:ZETTAMETER"] = \
        Mul(Pow(10, 39), Sym("attom"))  # nopep8
    CONVERSIONS["CENTIMETER:ANGSTROM"] = \
        Mul(Rat(Int(1), Pow(10, 8)), Sym("centim"))  # nopep8
    CONVERSIONS["CENTIMETER:ASTRONOMICALUNIT"] = \
        Mul(Mul(Int(1495978707), Pow(10, 4)), Sym("centim"))  # nopep8
    CONVERSIONS["CENTIMETER:ATTOMETER"] = \
        Mul(Rat(Int(1), Pow(10, 16)), Sym("centim"))  # nopep8
    CONVERSIONS["CENTIMETER:DECAMETER"] = \
        Mul(Int(1000), Sym("centim"))  # nopep8
    CONVERSIONS["CENTIMETER:DECIMETER"] = \
        Mul(Int(10), Sym("centim"))  # nopep8
    CONVERSIONS["CENTIMETER:EXAMETER"] = \
        Mul(Pow(10, 20), Sym("centim"))  # nopep8
    CONVERSIONS["CENTIMETER:FEMTOMETER"] = \
        Mul(Rat(Int(1), Pow(10, 13)), Sym("centim"))  # nopep8
    CONVERSIONS["CENTIMETER:FOOT"] = \
        Mul(Rat(Int(762), Int(25)), Sym("centim"))  # nopep8
    CONVERSIONS["CENTIMETER:GIGAMETER"] = \
        Mul(Pow(10, 11), Sym("centim"))  # nopep8
    CONVERSIONS["CENTIMETER:HECTOMETER"] = \
        Mul(Pow(10, 4), Sym("centim"))  # nopep8
    CONVERSIONS["CENTIMETER:INCH"] = \
        Mul(Rat(Int(127), Int(50)), Sym("centim"))  # nopep8
    CONVERSIONS["CENTIMETER:KILOMETER"] = \
        Mul(Pow(10, 5), Sym("centim"))  # nopep8
    CONVERSIONS["CENTIMETER:LIGHTYEAR"] = \
        Mul(Mul(Int("94607304725808"), Pow(10, 4)), Sym("centim"))  # nopep8
    CONVERSIONS["CENTIMETER:LINE"] = \
        Mul(Rat(Int(127), Int(600)), Sym("centim"))  # nopep8
    CONVERSIONS["CENTIMETER:MEGAMETER"] = \
        Mul(Pow(10, 8), Sym("centim"))  # nopep8
    CONVERSIONS["CENTIMETER:METER"] = \
        Mul(Int(100), Sym("centim"))  # nopep8
    CONVERSIONS["CENTIMETER:MICROMETER"] = \
        Mul(Rat(Int(1), Pow(10, 4)), Sym("centim"))  # nopep8
    CONVERSIONS["CENTIMETER:MILE"] = \
        Mul(Rat(Int(804672), Int(5)), Sym("centim"))  # nopep8
    CONVERSIONS["CENTIMETER:MILLIMETER"] = \
        Mul(Rat(Int(1), Int(10)), Sym("centim"))  # nopep8
    CONVERSIONS["CENTIMETER:NANOMETER"] = \
        Mul(Rat(Int(1), Pow(10, 7)), Sym("centim"))  # nopep8
    CONVERSIONS["CENTIMETER:PARSEC"] = \
        Mul(Mul(Int(30856776), Pow(10, 11)), Sym("centim"))  # nopep8
    CONVERSIONS["CENTIMETER:PETAMETER"] = \
        Mul(Pow(10, 17), Sym("centim"))  # nopep8
    CONVERSIONS["CENTIMETER:PICOMETER"] = \
        Mul(Rat(Int(1), Pow(10, 10)), Sym("centim"))  # nopep8
    CONVERSIONS["CENTIMETER:POINT"] = \
        Mul(Rat(Int(127), Int(3600)), Sym("centim"))  # nopep8
    CONVERSIONS["CENTIMETER:TERAMETER"] = \
        Mul(Pow(10, 14), Sym("centim"))  # nopep8
    CONVERSIONS["CENTIMETER:THOU"] = \
        Mul(Rat(Int(127), Mul(Int(5), Pow(10, 4))), Sym("centim"))  # nopep8
    CONVERSIONS["CENTIMETER:YARD"] = \
        Mul(Rat(Int(2286), Int(25)), Sym("centim"))  # nopep8
    CONVERSIONS["CENTIMETER:YOCTOMETER"] = \
        Mul(Rat(Int(1), Pow(10, 22)), Sym("centim"))  # nopep8
    CONVERSIONS["CENTIMETER:YOTTAMETER"] = \
        Mul(Pow(10, 26), Sym("centim"))  # nopep8
    CONVERSIONS["CENTIMETER:ZEPTOMETER"] = \
        Mul(Rat(Int(1), Pow(10, 19)), Sym("centim"))  # nopep8
    CONVERSIONS["CENTIMETER:ZETTAMETER"] = \
        Mul(Pow(10, 23), Sym("centim"))  # nopep8
    CONVERSIONS["DECAMETER:ANGSTROM"] = \
        Mul(Rat(Int(1), Pow(10, 11)), Sym("decam"))  # nopep8
    CONVERSIONS["DECAMETER:ASTRONOMICALUNIT"] = \
        Mul(Int("14959787070"), Sym("decam"))  # nopep8
    CONVERSIONS["DECAMETER:ATTOMETER"] = \
        Mul(Rat(Int(1), Pow(10, 19)), Sym("decam"))  # nopep8
    CONVERSIONS["DECAMETER:CENTIMETER"] = \
        Mul(Rat(Int(1), Int(1000)), Sym("decam"))  # nopep8
    CONVERSIONS["DECAMETER:DECIMETER"] = \
        Mul(Rat(Int(1), Int(100)), Sym("decam"))  # nopep8
    CONVERSIONS["DECAMETER:EXAMETER"] = \
        Mul(Pow(10, 17), Sym("decam"))  # nopep8
    CONVERSIONS["DECAMETER:FEMTOMETER"] = \
        Mul(Rat(Int(1), Pow(10, 16)), Sym("decam"))  # nopep8
    CONVERSIONS["DECAMETER:FOOT"] = \
        Mul(Rat(Int(381), Int(12500)), Sym("decam"))  # nopep8
    CONVERSIONS["DECAMETER:GIGAMETER"] = \
        Mul(Pow(10, 8), Sym("decam"))  # nopep8
    CONVERSIONS["DECAMETER:HECTOMETER"] = \
        Mul(Int(10), Sym("decam"))  # nopep8
    CONVERSIONS["DECAMETER:INCH"] = \
        Mul(Rat(Int(127), Mul(Int(5), Pow(10, 4))), Sym("decam"))  # nopep8
    CONVERSIONS["DECAMETER:KILOMETER"] = \
        Mul(Int(100), Sym("decam"))  # nopep8
    CONVERSIONS["DECAMETER:LIGHTYEAR"] = \
        Mul(Int("946073047258080"), Sym("decam"))  # nopep8
    CONVERSIONS["DECAMETER:LINE"] = \
        Mul(Rat(Int(127), Mul(Int(6), Pow(10, 5))), Sym("decam"))  # nopep8
    CONVERSIONS["DECAMETER:MEGAMETER"] = \
        Mul(Pow(10, 5), Sym("decam"))  # nopep8
    CONVERSIONS["DECAMETER:METER"] = \
        Mul(Rat(Int(1), Int(10)), Sym("decam"))  # nopep8
    CONVERSIONS["DECAMETER:MICROMETER"] = \
        Mul(Rat(Int(1), Pow(10, 7)), Sym("decam"))  # nopep8
    CONVERSIONS["DECAMETER:MILE"] = \
        Mul(Rat(Int(100584), Int(625)), Sym("decam"))  # nopep8
    CONVERSIONS["DECAMETER:MILLIMETER"] = \
        Mul(Rat(Int(1), Pow(10, 4)), Sym("decam"))  # nopep8
    CONVERSIONS["DECAMETER:NANOMETER"] = \
        Mul(Rat(Int(1), Pow(10, 10)), Sym("decam"))  # nopep8
    CONVERSIONS["DECAMETER:PARSEC"] = \
        Mul(Mul(Int(30856776), Pow(10, 8)), Sym("decam"))  # nopep8
    CONVERSIONS["DECAMETER:PETAMETER"] = \
        Mul(Pow(10, 14), Sym("decam"))  # nopep8
    CONVERSIONS["DECAMETER:PICOMETER"] = \
        Mul(Rat(Int(1), Pow(10, 13)), Sym("decam"))  # nopep8
    CONVERSIONS["DECAMETER:POINT"] = \
        Mul(Rat(Int(127), Mul(Int(36), Pow(10, 5))), Sym("decam"))  # nopep8
    CONVERSIONS["DECAMETER:TERAMETER"] = \
        Mul(Pow(10, 11), Sym("decam"))  # nopep8
    CONVERSIONS["DECAMETER:THOU"] = \
        Mul(Rat(Int(127), Mul(Int(5), Pow(10, 7))), Sym("decam"))  # nopep8
    CONVERSIONS["DECAMETER:YARD"] = \
        Mul(Rat(Int(1143), Int(12500)), Sym("decam"))  # nopep8
    CONVERSIONS["DECAMETER:YOCTOMETER"] = \
        Mul(Rat(Int(1), Pow(10, 25)), Sym("decam"))  # nopep8
    CONVERSIONS["DECAMETER:YOTTAMETER"] = \
        Mul(Pow(10, 23), Sym("decam"))  # nopep8
    CONVERSIONS["DECAMETER:ZEPTOMETER"] = \
        Mul(Rat(Int(1), Pow(10, 22)), Sym("decam"))  # nopep8
    CONVERSIONS["DECAMETER:ZETTAMETER"] = \
        Mul(Pow(10, 20), Sym("decam"))  # nopep8
    CONVERSIONS["DECIMETER:ANGSTROM"] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("decim"))  # nopep8
    CONVERSIONS["DECIMETER:ASTRONOMICALUNIT"] = \
        Mul(Int("1495978707000"), Sym("decim"))  # nopep8
    CONVERSIONS["DECIMETER:ATTOMETER"] = \
        Mul(Rat(Int(1), Pow(10, 17)), Sym("decim"))  # nopep8
    CONVERSIONS["DECIMETER:CENTIMETER"] = \
        Mul(Rat(Int(1), Int(10)), Sym("decim"))  # nopep8
    CONVERSIONS["DECIMETER:DECAMETER"] = \
        Mul(Int(100), Sym("decim"))  # nopep8
    CONVERSIONS["DECIMETER:EXAMETER"] = \
        Mul(Pow(10, 19), Sym("decim"))  # nopep8
    CONVERSIONS["DECIMETER:FEMTOMETER"] = \
        Mul(Rat(Int(1), Pow(10, 14)), Sym("decim"))  # nopep8
    CONVERSIONS["DECIMETER:FOOT"] = \
        Mul(Rat(Int(381), Int(125)), Sym("decim"))  # nopep8
    CONVERSIONS["DECIMETER:GIGAMETER"] = \
        Mul(Pow(10, 10), Sym("decim"))  # nopep8
    CONVERSIONS["DECIMETER:HECTOMETER"] = \
        Mul(Int(1000), Sym("decim"))  # nopep8
    CONVERSIONS["DECIMETER:INCH"] = \
        Mul(Rat(Int(127), Int(500)), Sym("decim"))  # nopep8
    CONVERSIONS["DECIMETER:KILOMETER"] = \
        Mul(Pow(10, 4), Sym("decim"))  # nopep8
    CONVERSIONS["DECIMETER:LIGHTYEAR"] = \
        Mul(Int("94607304725808000"), Sym("decim"))  # nopep8
    CONVERSIONS["DECIMETER:LINE"] = \
        Mul(Rat(Int(127), Int(6000)), Sym("decim"))  # nopep8
    CONVERSIONS["DECIMETER:MEGAMETER"] = \
        Mul(Pow(10, 7), Sym("decim"))  # nopep8
    CONVERSIONS["DECIMETER:METER"] = \
        Mul(Int(10), Sym("decim"))  # nopep8
    CONVERSIONS["DECIMETER:MICROMETER"] = \
        Mul(Rat(Int(1), Pow(10, 5)), Sym("decim"))  # nopep8
    CONVERSIONS["DECIMETER:MILE"] = \
        Mul(Rat(Int(402336), Int(25)), Sym("decim"))  # nopep8
    CONVERSIONS["DECIMETER:MILLIMETER"] = \
        Mul(Rat(Int(1), Int(100)), Sym("decim"))  # nopep8
    CONVERSIONS["DECIMETER:NANOMETER"] = \
        Mul(Rat(Int(1), Pow(10, 8)), Sym("decim"))  # nopep8
    CONVERSIONS["DECIMETER:PARSEC"] = \
        Mul(Mul(Int(30856776), Pow(10, 10)), Sym("decim"))  # nopep8
    CONVERSIONS["DECIMETER:PETAMETER"] = \
        Mul(Pow(10, 16), Sym("decim"))  # nopep8
    CONVERSIONS["DECIMETER:PICOMETER"] = \
        Mul(Rat(Int(1), Pow(10, 11)), Sym("decim"))  # nopep8
    CONVERSIONS["DECIMETER:POINT"] = \
        Mul(Rat(Int(127), Int(36000)), Sym("decim"))  # nopep8
    CONVERSIONS["DECIMETER:TERAMETER"] = \
        Mul(Pow(10, 13), Sym("decim"))  # nopep8
    CONVERSIONS["DECIMETER:THOU"] = \
        Mul(Rat(Int(127), Mul(Int(5), Pow(10, 5))), Sym("decim"))  # nopep8
    CONVERSIONS["DECIMETER:YARD"] = \
        Mul(Rat(Int(1143), Int(125)), Sym("decim"))  # nopep8
    CONVERSIONS["DECIMETER:YOCTOMETER"] = \
        Mul(Rat(Int(1), Pow(10, 23)), Sym("decim"))  # nopep8
    CONVERSIONS["DECIMETER:YOTTAMETER"] = \
        Mul(Pow(10, 25), Sym("decim"))  # nopep8
    CONVERSIONS["DECIMETER:ZEPTOMETER"] = \
        Mul(Rat(Int(1), Pow(10, 20)), Sym("decim"))  # nopep8
    CONVERSIONS["DECIMETER:ZETTAMETER"] = \
        Mul(Pow(10, 22), Sym("decim"))  # nopep8
    CONVERSIONS["EXAMETER:ANGSTROM"] = \
        Mul(Rat(Int(1), Pow(10, 28)), Sym("exam"))  # nopep8
    CONVERSIONS["EXAMETER:ASTRONOMICALUNIT"] = \
        Mul(Rat(Int(1495978707), Pow(10, 16)), Sym("exam"))  # nopep8
    CONVERSIONS["EXAMETER:ATTOMETER"] = \
        Mul(Rat(Int(1), Pow(10, 36)), Sym("exam"))  # nopep8
    CONVERSIONS["EXAMETER:CENTIMETER"] = \
        Mul(Rat(Int(1), Pow(10, 20)), Sym("exam"))  # nopep8
    CONVERSIONS["EXAMETER:DECAMETER"] = \
        Mul(Rat(Int(1), Pow(10, 17)), Sym("exam"))  # nopep8
    CONVERSIONS["EXAMETER:DECIMETER"] = \
        Mul(Rat(Int(1), Pow(10, 19)), Sym("exam"))  # nopep8
    CONVERSIONS["EXAMETER:FEMTOMETER"] = \
        Mul(Rat(Int(1), Pow(10, 33)), Sym("exam"))  # nopep8
    CONVERSIONS["EXAMETER:FOOT"] = \
        Mul(Rat(Int(381), Mul(Int(125), Pow(10, 19))), Sym("exam"))  # nopep8
    CONVERSIONS["EXAMETER:GIGAMETER"] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("exam"))  # nopep8
    CONVERSIONS["EXAMETER:HECTOMETER"] = \
        Mul(Rat(Int(1), Pow(10, 16)), Sym("exam"))  # nopep8
    CONVERSIONS["EXAMETER:INCH"] = \
        Mul(Rat(Int(127), Mul(Int(5), Pow(10, 21))), Sym("exam"))  # nopep8
    CONVERSIONS["EXAMETER:KILOMETER"] = \
        Mul(Rat(Int(1), Pow(10, 15)), Sym("exam"))  # nopep8
    CONVERSIONS["EXAMETER:LIGHTYEAR"] = \
        Mul(Rat(Int("5912956545363"), Mul(Int(625), Pow(10, 12))), Sym("exam"))  # nopep8
    CONVERSIONS["EXAMETER:LINE"] = \
        Mul(Rat(Int(127), Mul(Int(6), Pow(10, 22))), Sym("exam"))  # nopep8
    CONVERSIONS["EXAMETER:MEGAMETER"] = \
        Mul(Rat(Int(1), Pow(10, 12)), Sym("exam"))  # nopep8
    CONVERSIONS["EXAMETER:METER"] = \
        Mul(Rat(Int(1), Pow(10, 18)), Sym("exam"))  # nopep8
    CONVERSIONS["EXAMETER:MICROMETER"] = \
        Mul(Rat(Int(1), Pow(10, 24)), Sym("exam"))  # nopep8
    CONVERSIONS["EXAMETER:MILE"] = \
        Mul(Rat(Int(12573), Mul(Int(78125), Pow(10, 14))), Sym("exam"))  # nopep8
    CONVERSIONS["EXAMETER:MILLIMETER"] = \
        Mul(Rat(Int(1), Pow(10, 21)), Sym("exam"))  # nopep8
    CONVERSIONS["EXAMETER:NANOMETER"] = \
        Mul(Rat(Int(1), Pow(10, 27)), Sym("exam"))  # nopep8
    CONVERSIONS["EXAMETER:PARSEC"] = \
        Mul(Rat(Int(3857097), Mul(Int(125), Pow(10, 6))), Sym("exam"))  # nopep8
    CONVERSIONS["EXAMETER:PETAMETER"] = \
        Mul(Rat(Int(1), Int(1000)), Sym("exam"))  # nopep8
    CONVERSIONS["EXAMETER:PICOMETER"] = \
        Mul(Rat(Int(1), Pow(10, 30)), Sym("exam"))  # nopep8
    CONVERSIONS["EXAMETER:POINT"] = \
        Mul(Rat(Int(127), Mul(Int(36), Pow(10, 22))), Sym("exam"))  # nopep8
    CONVERSIONS["EXAMETER:TERAMETER"] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("exam"))  # nopep8
    CONVERSIONS["EXAMETER:THOU"] = \
        Mul(Rat(Int(127), Mul(Int(5), Pow(10, 24))), Sym("exam"))  # nopep8
    CONVERSIONS["EXAMETER:YARD"] = \
        Mul(Rat(Int(1143), Mul(Int(125), Pow(10, 19))), Sym("exam"))  # nopep8
    CONVERSIONS["EXAMETER:YOCTOMETER"] = \
        Mul(Rat(Int(1), Pow(10, 42)), Sym("exam"))  # nopep8
    CONVERSIONS["EXAMETER:YOTTAMETER"] = \
        Mul(Pow(10, 6), Sym("exam"))  # nopep8
    CONVERSIONS["EXAMETER:ZEPTOMETER"] = \
        Mul(Rat(Int(1), Pow(10, 39)), Sym("exam"))  # nopep8
    CONVERSIONS["EXAMETER:ZETTAMETER"] = \
        Mul(Int(1000), Sym("exam"))  # nopep8
    CONVERSIONS["FEMTOMETER:ANGSTROM"] = \
        Mul(Pow(10, 5), Sym("femtom"))  # nopep8
    CONVERSIONS["FEMTOMETER:ASTRONOMICALUNIT"] = \
        Mul(Mul(Int(1495978707), Pow(10, 17)), Sym("femtom"))  # nopep8
    CONVERSIONS["FEMTOMETER:ATTOMETER"] = \
        Mul(Rat(Int(1), Int(1000)), Sym("femtom"))  # nopep8
    CONVERSIONS["FEMTOMETER:CENTIMETER"] = \
        Mul(Pow(10, 13), Sym("femtom"))  # nopep8
    CONVERSIONS["FEMTOMETER:DECAMETER"] = \
        Mul(Pow(10, 16), Sym("femtom"))  # nopep8
    CONVERSIONS["FEMTOMETER:DECIMETER"] = \
        Mul(Pow(10, 14), Sym("femtom"))  # nopep8
    CONVERSIONS["FEMTOMETER:EXAMETER"] = \
        Mul(Pow(10, 33), Sym("femtom"))  # nopep8
    CONVERSIONS["FEMTOMETER:FOOT"] = \
        Mul(Mul(Int(3048), Pow(10, 11)), Sym("femtom"))  # nopep8
    CONVERSIONS["FEMTOMETER:GIGAMETER"] = \
        Mul(Pow(10, 24), Sym("femtom"))  # nopep8
    CONVERSIONS["FEMTOMETER:HECTOMETER"] = \
        Mul(Pow(10, 17), Sym("femtom"))  # nopep8
    CONVERSIONS["FEMTOMETER:INCH"] = \
        Mul(Mul(Int(254), Pow(10, 11)), Sym("femtom"))  # nopep8
    CONVERSIONS["FEMTOMETER:KILOMETER"] = \
        Mul(Pow(10, 18), Sym("femtom"))  # nopep8
    CONVERSIONS["FEMTOMETER:LIGHTYEAR"] = \
        Mul(Mul(Int("94607304725808"), Pow(10, 17)), Sym("femtom"))  # nopep8
    CONVERSIONS["FEMTOMETER:LINE"] = \
        Mul(Rat(Mul(Int(635), Pow(10, 10)), Int(3)), Sym("femtom"))  # nopep8
    CONVERSIONS["FEMTOMETER:MEGAMETER"] = \
        Mul(Pow(10, 21), Sym("femtom"))  # nopep8
    CONVERSIONS["FEMTOMETER:METER"] = \
        Mul(Pow(10, 15), Sym("femtom"))  # nopep8
    CONVERSIONS["FEMTOMETER:MICROMETER"] = \
        Mul(Pow(10, 9), Sym("femtom"))  # nopep8
    CONVERSIONS["FEMTOMETER:MILE"] = \
        Mul(Mul(Int(1609344), Pow(10, 12)), Sym("femtom"))  # nopep8
    CONVERSIONS["FEMTOMETER:MILLIMETER"] = \
        Mul(Pow(10, 12), Sym("femtom"))  # nopep8
    CONVERSIONS["FEMTOMETER:NANOMETER"] = \
        Mul(Pow(10, 6), Sym("femtom"))  # nopep8
    CONVERSIONS["FEMTOMETER:PARSEC"] = \
        Mul(Mul(Int(30856776), Pow(10, 24)), Sym("femtom"))  # nopep8
    CONVERSIONS["FEMTOMETER:PETAMETER"] = \
        Mul(Pow(10, 30), Sym("femtom"))  # nopep8
    CONVERSIONS["FEMTOMETER:PICOMETER"] = \
        Mul(Int(1000), Sym("femtom"))  # nopep8
    CONVERSIONS["FEMTOMETER:POINT"] = \
        Mul(Rat(Mul(Int(3175), Pow(10, 9)), Int(9)), Sym("femtom"))  # nopep8
    CONVERSIONS["FEMTOMETER:TERAMETER"] = \
        Mul(Pow(10, 27), Sym("femtom"))  # nopep8
    CONVERSIONS["FEMTOMETER:THOU"] = \
        Mul(Mul(Int(254), Pow(10, 8)), Sym("femtom"))  # nopep8
    CONVERSIONS["FEMTOMETER:YARD"] = \
        Mul(Mul(Int(9144), Pow(10, 11)), Sym("femtom"))  # nopep8
    CONVERSIONS["FEMTOMETER:YOCTOMETER"] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("femtom"))  # nopep8
    CONVERSIONS["FEMTOMETER:YOTTAMETER"] = \
        Mul(Pow(10, 39), Sym("femtom"))  # nopep8
    CONVERSIONS["FEMTOMETER:ZEPTOMETER"] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("femtom"))  # nopep8
    CONVERSIONS["FEMTOMETER:ZETTAMETER"] = \
        Mul(Pow(10, 36), Sym("femtom"))  # nopep8
    CONVERSIONS["FOOT:ANGSTROM"] = \
        Mul(Rat(Int(1), Mul(Int(3048), Pow(10, 6))), Sym("ft"))  # nopep8
    CONVERSIONS["FOOT:ASTRONOMICALUNIT"] = \
        Mul(Rat(Int("62332446125000"), Int(127)), Sym("ft"))  # nopep8
    CONVERSIONS["FOOT:ATTOMETER"] = \
        Mul(Rat(Int(1), Mul(Int(3048), Pow(10, 14))), Sym("ft"))  # nopep8
    CONVERSIONS["FOOT:CENTIMETER"] = \
        Mul(Rat(Int(25), Int(762)), Sym("ft"))  # nopep8
    CONVERSIONS["FOOT:DECAMETER"] = \
        Mul(Rat(Int(12500), Int(381)), Sym("ft"))  # nopep8
    CONVERSIONS["FOOT:DECIMETER"] = \
        Mul(Rat(Int(125), Int(381)), Sym("ft"))  # nopep8
    CONVERSIONS["FOOT:EXAMETER"] = \
        Mul(Rat(Mul(Int(125), Pow(10, 19)), Int(381)), Sym("ft"))  # nopep8
    CONVERSIONS["FOOT:FEMTOMETER"] = \
        Mul(Rat(Int(1), Mul(Int(3048), Pow(10, 11))), Sym("ft"))  # nopep8
    CONVERSIONS["FOOT:GIGAMETER"] = \
        Mul(Rat(Mul(Int(125), Pow(10, 10)), Int(381)), Sym("ft"))  # nopep8
    CONVERSIONS["FOOT:HECTOMETER"] = \
        Mul(Rat(Int(125000), Int(381)), Sym("ft"))  # nopep8
    CONVERSIONS["FOOT:INCH"] = \
        Mul(Rat(Int(1), Int(12)), Sym("ft"))  # nopep8
    CONVERSIONS["FOOT:KILOMETER"] = \
        Mul(Rat(Mul(Int(125), Pow(10, 4)), Int(381)), Sym("ft"))  # nopep8
    CONVERSIONS["FOOT:LIGHTYEAR"] = \
        Mul(Rat(Mul(Int("3941971030242"), Pow(10, 6)), Int(127)), Sym("ft"))  # nopep8
    CONVERSIONS["FOOT:LINE"] = \
        Mul(Rat(Int(1), Int(144)), Sym("ft"))  # nopep8
    CONVERSIONS["FOOT:MEGAMETER"] = \
        Mul(Rat(Mul(Int(125), Pow(10, 7)), Int(381)), Sym("ft"))  # nopep8
    CONVERSIONS["FOOT:METER"] = \
        Mul(Rat(Int(1250), Int(381)), Sym("ft"))  # nopep8
    CONVERSIONS["FOOT:MICROMETER"] = \
        Mul(Rat(Int(1), Int(304800)), Sym("ft"))  # nopep8
    CONVERSIONS["FOOT:MILE"] = \
        Mul(Int(5280), Sym("ft"))  # nopep8
    CONVERSIONS["FOOT:MILLIMETER"] = \
        Mul(Rat(Int(5), Int(1524)), Sym("ft"))  # nopep8
    CONVERSIONS["FOOT:NANOMETER"] = \
        Mul(Rat(Int(1), Mul(Int(3048), Pow(10, 5))), Sym("ft"))  # nopep8
    CONVERSIONS["FOOT:PARSEC"] = \
        Mul(Rat(Mul(Int(1285699), Pow(10, 13)), Int(127)), Sym("ft"))  # nopep8
    CONVERSIONS["FOOT:PETAMETER"] = \
        Mul(Rat(Mul(Int(125), Pow(10, 16)), Int(381)), Sym("ft"))  # nopep8
    CONVERSIONS["FOOT:PICOMETER"] = \
        Mul(Rat(Int(1), Mul(Int(3048), Pow(10, 8))), Sym("ft"))  # nopep8
    CONVERSIONS["FOOT:POINT"] = \
        Mul(Rat(Int(1), Int(864)), Sym("ft"))  # nopep8
    CONVERSIONS["FOOT:TERAMETER"] = \
        Mul(Rat(Mul(Int(125), Pow(10, 13)), Int(381)), Sym("ft"))  # nopep8
    CONVERSIONS["FOOT:THOU"] = \
        Mul(Rat(Int(1), Int(12000)), Sym("ft"))  # nopep8
    CONVERSIONS["FOOT:YARD"] = \
        Mul(Int(3), Sym("ft"))  # nopep8
    CONVERSIONS["FOOT:YOCTOMETER"] = \
        Mul(Rat(Int(1), Mul(Int(3048), Pow(10, 20))), Sym("ft"))  # nopep8
    CONVERSIONS["FOOT:YOTTAMETER"] = \
        Mul(Rat(Mul(Int(125), Pow(10, 25)), Int(381)), Sym("ft"))  # nopep8
    CONVERSIONS["FOOT:ZEPTOMETER"] = \
        Mul(Rat(Int(1), Mul(Int(3048), Pow(10, 17))), Sym("ft"))  # nopep8
    CONVERSIONS["FOOT:ZETTAMETER"] = \
        Mul(Rat(Mul(Int(125), Pow(10, 22)), Int(381)), Sym("ft"))  # nopep8
    CONVERSIONS["GIGAMETER:ANGSTROM"] = \
        Mul(Rat(Int(1), Pow(10, 19)), Sym("gigam"))  # nopep8
    CONVERSIONS["GIGAMETER:ASTRONOMICALUNIT"] = \
        Mul(Rat(Int(1495978707), Pow(10, 7)), Sym("gigam"))  # nopep8
    CONVERSIONS["GIGAMETER:ATTOMETER"] = \
        Mul(Rat(Int(1), Pow(10, 27)), Sym("gigam"))  # nopep8
    CONVERSIONS["GIGAMETER:CENTIMETER"] = \
        Mul(Rat(Int(1), Pow(10, 11)), Sym("gigam"))  # nopep8
    CONVERSIONS["GIGAMETER:DECAMETER"] = \
        Mul(Rat(Int(1), Pow(10, 8)), Sym("gigam"))  # nopep8
    CONVERSIONS["GIGAMETER:DECIMETER"] = \
        Mul(Rat(Int(1), Pow(10, 10)), Sym("gigam"))  # nopep8
    CONVERSIONS["GIGAMETER:EXAMETER"] = \
        Mul(Pow(10, 9), Sym("gigam"))  # nopep8
    CONVERSIONS["GIGAMETER:FEMTOMETER"] = \
        Mul(Rat(Int(1), Pow(10, 24)), Sym("gigam"))  # nopep8
    CONVERSIONS["GIGAMETER:FOOT"] = \
        Mul(Rat(Int(381), Mul(Int(125), Pow(10, 10))), Sym("gigam"))  # nopep8
    CONVERSIONS["GIGAMETER:HECTOMETER"] = \
        Mul(Rat(Int(1), Pow(10, 7)), Sym("gigam"))  # nopep8
    CONVERSIONS["GIGAMETER:INCH"] = \
        Mul(Rat(Int(127), Mul(Int(5), Pow(10, 12))), Sym("gigam"))  # nopep8
    CONVERSIONS["GIGAMETER:KILOMETER"] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("gigam"))  # nopep8
    CONVERSIONS["GIGAMETER:LIGHTYEAR"] = \
        Mul(Rat(Int("5912956545363"), Int(625000)), Sym("gigam"))  # nopep8
    CONVERSIONS["GIGAMETER:LINE"] = \
        Mul(Rat(Int(127), Mul(Int(6), Pow(10, 13))), Sym("gigam"))  # nopep8
    CONVERSIONS["GIGAMETER:MEGAMETER"] = \
        Mul(Rat(Int(1), Int(1000)), Sym("gigam"))  # nopep8
    CONVERSIONS["GIGAMETER:METER"] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("gigam"))  # nopep8
    CONVERSIONS["GIGAMETER:MICROMETER"] = \
        Mul(Rat(Int(1), Pow(10, 15)), Sym("gigam"))  # nopep8
    CONVERSIONS["GIGAMETER:MILE"] = \
        Mul(Rat(Int(12573), Mul(Int(78125), Pow(10, 5))), Sym("gigam"))  # nopep8
    CONVERSIONS["GIGAMETER:MILLIMETER"] = \
        Mul(Rat(Int(1), Pow(10, 12)), Sym("gigam"))  # nopep8
    CONVERSIONS["GIGAMETER:NANOMETER"] = \
        Mul(Rat(Int(1), Pow(10, 18)), Sym("gigam"))  # nopep8
    CONVERSIONS["GIGAMETER:PARSEC"] = \
        Mul(Int(30856776), Sym("gigam"))  # nopep8
    CONVERSIONS["GIGAMETER:PETAMETER"] = \
        Mul(Pow(10, 6), Sym("gigam"))  # nopep8
    CONVERSIONS["GIGAMETER:PICOMETER"] = \
        Mul(Rat(Int(1), Pow(10, 21)), Sym("gigam"))  # nopep8
    CONVERSIONS["GIGAMETER:POINT"] = \
        Mul(Rat(Int(127), Mul(Int(36), Pow(10, 13))), Sym("gigam"))  # nopep8
    CONVERSIONS["GIGAMETER:TERAMETER"] = \
        Mul(Int(1000), Sym("gigam"))  # nopep8
    CONVERSIONS["GIGAMETER:THOU"] = \
        Mul(Rat(Int(127), Mul(Int(5), Pow(10, 15))), Sym("gigam"))  # nopep8
    CONVERSIONS["GIGAMETER:YARD"] = \
        Mul(Rat(Int(1143), Mul(Int(125), Pow(10, 10))), Sym("gigam"))  # nopep8
    CONVERSIONS["GIGAMETER:YOCTOMETER"] = \
        Mul(Rat(Int(1), Pow(10, 33)), Sym("gigam"))  # nopep8
    CONVERSIONS["GIGAMETER:YOTTAMETER"] = \
        Mul(Pow(10, 15), Sym("gigam"))  # nopep8
    CONVERSIONS["GIGAMETER:ZEPTOMETER"] = \
        Mul(Rat(Int(1), Pow(10, 30)), Sym("gigam"))  # nopep8
    CONVERSIONS["GIGAMETER:ZETTAMETER"] = \
        Mul(Pow(10, 12), Sym("gigam"))  # nopep8
    CONVERSIONS["HECTOMETER:ANGSTROM"] = \
        Mul(Rat(Int(1), Pow(10, 12)), Sym("hectom"))  # nopep8
    CONVERSIONS["HECTOMETER:ASTRONOMICALUNIT"] = \
        Mul(Int(1495978707), Sym("hectom"))  # nopep8
    CONVERSIONS["HECTOMETER:ATTOMETER"] = \
        Mul(Rat(Int(1), Pow(10, 20)), Sym("hectom"))  # nopep8
    CONVERSIONS["HECTOMETER:CENTIMETER"] = \
        Mul(Rat(Int(1), Pow(10, 4)), Sym("hectom"))  # nopep8
    CONVERSIONS["HECTOMETER:DECAMETER"] = \
        Mul(Rat(Int(1), Int(10)), Sym("hectom"))  # nopep8
    CONVERSIONS["HECTOMETER:DECIMETER"] = \
        Mul(Rat(Int(1), Int(1000)), Sym("hectom"))  # nopep8
    CONVERSIONS["HECTOMETER:EXAMETER"] = \
        Mul(Pow(10, 16), Sym("hectom"))  # nopep8
    CONVERSIONS["HECTOMETER:FEMTOMETER"] = \
        Mul(Rat(Int(1), Pow(10, 17)), Sym("hectom"))  # nopep8
    CONVERSIONS["HECTOMETER:FOOT"] = \
        Mul(Rat(Int(381), Int(125000)), Sym("hectom"))  # nopep8
    CONVERSIONS["HECTOMETER:GIGAMETER"] = \
        Mul(Pow(10, 7), Sym("hectom"))  # nopep8
    CONVERSIONS["HECTOMETER:INCH"] = \
        Mul(Rat(Int(127), Mul(Int(5), Pow(10, 5))), Sym("hectom"))  # nopep8
    CONVERSIONS["HECTOMETER:KILOMETER"] = \
        Mul(Int(10), Sym("hectom"))  # nopep8
    CONVERSIONS["HECTOMETER:LIGHTYEAR"] = \
        Mul(Int("94607304725808"), Sym("hectom"))  # nopep8
    CONVERSIONS["HECTOMETER:LINE"] = \
        Mul(Rat(Int(127), Mul(Int(6), Pow(10, 6))), Sym("hectom"))  # nopep8
    CONVERSIONS["HECTOMETER:MEGAMETER"] = \
        Mul(Pow(10, 4), Sym("hectom"))  # nopep8
    CONVERSIONS["HECTOMETER:METER"] = \
        Mul(Rat(Int(1), Int(100)), Sym("hectom"))  # nopep8
    CONVERSIONS["HECTOMETER:MICROMETER"] = \
        Mul(Rat(Int(1), Pow(10, 8)), Sym("hectom"))  # nopep8
    CONVERSIONS["HECTOMETER:MILE"] = \
        Mul(Rat(Int(50292), Int(3125)), Sym("hectom"))  # nopep8
    CONVERSIONS["HECTOMETER:MILLIMETER"] = \
        Mul(Rat(Int(1), Pow(10, 5)), Sym("hectom"))  # nopep8
    CONVERSIONS["HECTOMETER:NANOMETER"] = \
        Mul(Rat(Int(1), Pow(10, 11)), Sym("hectom"))  # nopep8
    CONVERSIONS["HECTOMETER:PARSEC"] = \
        Mul(Mul(Int(30856776), Pow(10, 7)), Sym("hectom"))  # nopep8
    CONVERSIONS["HECTOMETER:PETAMETER"] = \
        Mul(Pow(10, 13), Sym("hectom"))  # nopep8
    CONVERSIONS["HECTOMETER:PICOMETER"] = \
        Mul(Rat(Int(1), Pow(10, 14)), Sym("hectom"))  # nopep8
    CONVERSIONS["HECTOMETER:POINT"] = \
        Mul(Rat(Int(127), Mul(Int(36), Pow(10, 6))), Sym("hectom"))  # nopep8
    CONVERSIONS["HECTOMETER:TERAMETER"] = \
        Mul(Pow(10, 10), Sym("hectom"))  # nopep8
    CONVERSIONS["HECTOMETER:THOU"] = \
        Mul(Rat(Int(127), Mul(Int(5), Pow(10, 8))), Sym("hectom"))  # nopep8
    CONVERSIONS["HECTOMETER:YARD"] = \
        Mul(Rat(Int(1143), Int(125000)), Sym("hectom"))  # nopep8
    CONVERSIONS["HECTOMETER:YOCTOMETER"] = \
        Mul(Rat(Int(1), Pow(10, 26)), Sym("hectom"))  # nopep8
    CONVERSIONS["HECTOMETER:YOTTAMETER"] = \
        Mul(Pow(10, 22), Sym("hectom"))  # nopep8
    CONVERSIONS["HECTOMETER:ZEPTOMETER"] = \
        Mul(Rat(Int(1), Pow(10, 23)), Sym("hectom"))  # nopep8
    CONVERSIONS["HECTOMETER:ZETTAMETER"] = \
        Mul(Pow(10, 19), Sym("hectom"))  # nopep8
    CONVERSIONS["INCH:ANGSTROM"] = \
        Mul(Rat(Int(1), Mul(Int(254), Pow(10, 6))), Sym("in"))  # nopep8
    CONVERSIONS["INCH:ASTRONOMICALUNIT"] = \
        Mul(Rat(Mul(Int("7479893535"), Pow(10, 5)), Int(127)), Sym("in"))  # nopep8
    CONVERSIONS["INCH:ATTOMETER"] = \
        Mul(Rat(Int(1), Mul(Int(254), Pow(10, 14))), Sym("in"))  # nopep8
    CONVERSIONS["INCH:CENTIMETER"] = \
        Mul(Rat(Int(50), Int(127)), Sym("in"))  # nopep8
    CONVERSIONS["INCH:DECAMETER"] = \
        Mul(Rat(Mul(Int(5), Pow(10, 4)), Int(127)), Sym("in"))  # nopep8
    CONVERSIONS["INCH:DECIMETER"] = \
        Mul(Rat(Int(500), Int(127)), Sym("in"))  # nopep8
    CONVERSIONS["INCH:EXAMETER"] = \
        Mul(Rat(Mul(Int(5), Pow(10, 21)), Int(127)), Sym("in"))  # nopep8
    CONVERSIONS["INCH:FEMTOMETER"] = \
        Mul(Rat(Int(1), Mul(Int(254), Pow(10, 11))), Sym("in"))  # nopep8
    CONVERSIONS["INCH:FOOT"] = \
        Mul(Int(12), Sym("in"))  # nopep8
    CONVERSIONS["INCH:GIGAMETER"] = \
        Mul(Rat(Mul(Int(5), Pow(10, 12)), Int(127)), Sym("in"))  # nopep8
    CONVERSIONS["INCH:HECTOMETER"] = \
        Mul(Rat(Mul(Int(5), Pow(10, 5)), Int(127)), Sym("in"))  # nopep8
    CONVERSIONS["INCH:KILOMETER"] = \
        Mul(Rat(Mul(Int(5), Pow(10, 6)), Int(127)), Sym("in"))  # nopep8
    CONVERSIONS["INCH:LIGHTYEAR"] = \
        Mul(Rat(Mul(Int("47303652362904"), Pow(10, 6)), Int(127)), Sym("in"))  # nopep8
    CONVERSIONS["INCH:LINE"] = \
        Mul(Rat(Int(1), Int(12)), Sym("in"))  # nopep8
    CONVERSIONS["INCH:MEGAMETER"] = \
        Mul(Rat(Mul(Int(5), Pow(10, 9)), Int(127)), Sym("in"))  # nopep8
    CONVERSIONS["INCH:METER"] = \
        Mul(Rat(Int(5000), Int(127)), Sym("in"))  # nopep8
    CONVERSIONS["INCH:MICROMETER"] = \
        Mul(Rat(Int(1), Int(25400)), Sym("in"))  # nopep8
    CONVERSIONS["INCH:MILE"] = \
        Mul(Int(63360), Sym("in"))  # nopep8
    CONVERSIONS["INCH:MILLIMETER"] = \
        Mul(Rat(Int(5), Int(127)), Sym("in"))  # nopep8
    CONVERSIONS["INCH:NANOMETER"] = \
        Mul(Rat(Int(1), Mul(Int(254), Pow(10, 5))), Sym("in"))  # nopep8
    CONVERSIONS["INCH:PARSEC"] = \
        Mul(Rat(Mul(Int(15428388), Pow(10, 13)), Int(127)), Sym("in"))  # nopep8
    CONVERSIONS["INCH:PETAMETER"] = \
        Mul(Rat(Mul(Int(5), Pow(10, 18)), Int(127)), Sym("in"))  # nopep8
    CONVERSIONS["INCH:PICOMETER"] = \
        Mul(Rat(Int(1), Mul(Int(254), Pow(10, 8))), Sym("in"))  # nopep8
    CONVERSIONS["INCH:POINT"] = \
        Mul(Rat(Int(1), Int(72)), Sym("in"))  # nopep8
    CONVERSIONS["INCH:TERAMETER"] = \
        Mul(Rat(Mul(Int(5), Pow(10, 15)), Int(127)), Sym("in"))  # nopep8
    CONVERSIONS["INCH:THOU"] = \
        Mul(Rat(Int(1), Int(1000)), Sym("in"))  # nopep8
    CONVERSIONS["INCH:YARD"] = \
        Mul(Int(36), Sym("in"))  # nopep8
    CONVERSIONS["INCH:YOCTOMETER"] = \
        Mul(Rat(Int(1), Mul(Int(254), Pow(10, 20))), Sym("in"))  # nopep8
    CONVERSIONS["INCH:YOTTAMETER"] = \
        Mul(Rat(Mul(Int(5), Pow(10, 27)), Int(127)), Sym("in"))  # nopep8
    CONVERSIONS["INCH:ZEPTOMETER"] = \
        Mul(Rat(Int(1), Mul(Int(254), Pow(10, 17))), Sym("in"))  # nopep8
    CONVERSIONS["INCH:ZETTAMETER"] = \
        Mul(Rat(Mul(Int(5), Pow(10, 24)), Int(127)), Sym("in"))  # nopep8
    CONVERSIONS["KILOMETER:ANGSTROM"] = \
        Mul(Rat(Int(1), Pow(10, 13)), Sym("kilom"))  # nopep8
    CONVERSIONS["KILOMETER:ASTRONOMICALUNIT"] = \
        Mul(Rat(Int(1495978707), Int(10)), Sym("kilom"))  # nopep8
    CONVERSIONS["KILOMETER:ATTOMETER"] = \
        Mul(Rat(Int(1), Pow(10, 21)), Sym("kilom"))  # nopep8
    CONVERSIONS["KILOMETER:CENTIMETER"] = \
        Mul(Rat(Int(1), Pow(10, 5)), Sym("kilom"))  # nopep8
    CONVERSIONS["KILOMETER:DECAMETER"] = \
        Mul(Rat(Int(1), Int(100)), Sym("kilom"))  # nopep8
    CONVERSIONS["KILOMETER:DECIMETER"] = \
        Mul(Rat(Int(1), Pow(10, 4)), Sym("kilom"))  # nopep8
    CONVERSIONS["KILOMETER:EXAMETER"] = \
        Mul(Pow(10, 15), Sym("kilom"))  # nopep8
    CONVERSIONS["KILOMETER:FEMTOMETER"] = \
        Mul(Rat(Int(1), Pow(10, 18)), Sym("kilom"))  # nopep8
    CONVERSIONS["KILOMETER:FOOT"] = \
        Mul(Rat(Int(381), Mul(Int(125), Pow(10, 4))), Sym("kilom"))  # nopep8
    CONVERSIONS["KILOMETER:GIGAMETER"] = \
        Mul(Pow(10, 6), Sym("kilom"))  # nopep8
    CONVERSIONS["KILOMETER:HECTOMETER"] = \
        Mul(Rat(Int(1), Int(10)), Sym("kilom"))  # nopep8
    CONVERSIONS["KILOMETER:INCH"] = \
        Mul(Rat(Int(127), Mul(Int(5), Pow(10, 6))), Sym("kilom"))  # nopep8
    CONVERSIONS["KILOMETER:LIGHTYEAR"] = \
        Mul(Rat(Int("47303652362904"), Int(5)), Sym("kilom"))  # nopep8
    CONVERSIONS["KILOMETER:LINE"] = \
        Mul(Rat(Int(127), Mul(Int(6), Pow(10, 7))), Sym("kilom"))  # nopep8
    CONVERSIONS["KILOMETER:MEGAMETER"] = \
        Mul(Int(1000), Sym("kilom"))  # nopep8
    CONVERSIONS["KILOMETER:METER"] = \
        Mul(Rat(Int(1), Int(1000)), Sym("kilom"))  # nopep8
    CONVERSIONS["KILOMETER:MICROMETER"] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("kilom"))  # nopep8
    CONVERSIONS["KILOMETER:MILE"] = \
        Mul(Rat(Int(25146), Int(15625)), Sym("kilom"))  # nopep8
    CONVERSIONS["KILOMETER:MILLIMETER"] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("kilom"))  # nopep8
    CONVERSIONS["KILOMETER:NANOMETER"] = \
        Mul(Rat(Int(1), Pow(10, 12)), Sym("kilom"))  # nopep8
    CONVERSIONS["KILOMETER:PARSEC"] = \
        Mul(Mul(Int(30856776), Pow(10, 6)), Sym("kilom"))  # nopep8
    CONVERSIONS["KILOMETER:PETAMETER"] = \
        Mul(Pow(10, 12), Sym("kilom"))  # nopep8
    CONVERSIONS["KILOMETER:PICOMETER"] = \
        Mul(Rat(Int(1), Pow(10, 15)), Sym("kilom"))  # nopep8
    CONVERSIONS["KILOMETER:POINT"] = \
        Mul(Rat(Int(127), Mul(Int(36), Pow(10, 7))), Sym("kilom"))  # nopep8
    CONVERSIONS["KILOMETER:TERAMETER"] = \
        Mul(Pow(10, 9), Sym("kilom"))  # nopep8
    CONVERSIONS["KILOMETER:THOU"] = \
        Mul(Rat(Int(127), Mul(Int(5), Pow(10, 9))), Sym("kilom"))  # nopep8
    CONVERSIONS["KILOMETER:YARD"] = \
        Mul(Rat(Int(1143), Mul(Int(125), Pow(10, 4))), Sym("kilom"))  # nopep8
    CONVERSIONS["KILOMETER:YOCTOMETER"] = \
        Mul(Rat(Int(1), Pow(10, 27)), Sym("kilom"))  # nopep8
    CONVERSIONS["KILOMETER:YOTTAMETER"] = \
        Mul(Pow(10, 21), Sym("kilom"))  # nopep8
    CONVERSIONS["KILOMETER:ZEPTOMETER"] = \
        Mul(Rat(Int(1), Pow(10, 24)), Sym("kilom"))  # nopep8
    CONVERSIONS["KILOMETER:ZETTAMETER"] = \
        Mul(Pow(10, 18), Sym("kilom"))  # nopep8
    CONVERSIONS["LIGHTYEAR:ANGSTROM"] = \
        Mul(Rat(Int(1), Mul(Int("94607304725808"), Pow(10, 12))), Sym("ly"))  # nopep8
    CONVERSIONS["LIGHTYEAR:ASTRONOMICALUNIT"] = \
        Mul(Rat(Int(6830953), Int("431996825232")), Sym("ly"))  # nopep8
    CONVERSIONS["LIGHTYEAR:ATTOMETER"] = \
        Mul(Rat(Int(1), Mul(Int("94607304725808"), Pow(10, 20))), Sym("ly"))  # nopep8
    CONVERSIONS["LIGHTYEAR:CENTIMETER"] = \
        Mul(Rat(Int(1), Mul(Int("94607304725808"), Pow(10, 4))), Sym("ly"))  # nopep8
    CONVERSIONS["LIGHTYEAR:DECAMETER"] = \
        Mul(Rat(Int(1), Int("946073047258080")), Sym("ly"))  # nopep8
    CONVERSIONS["LIGHTYEAR:DECIMETER"] = \
        Mul(Rat(Int(1), Int("94607304725808000")), Sym("ly"))  # nopep8
    CONVERSIONS["LIGHTYEAR:EXAMETER"] = \
        Mul(Rat(Mul(Int(625), Pow(10, 12)), Int("5912956545363")), Sym("ly"))  # nopep8
    CONVERSIONS["LIGHTYEAR:FEMTOMETER"] = \
        Mul(Rat(Int(1), Mul(Int("94607304725808"), Pow(10, 17))), Sym("ly"))  # nopep8
    CONVERSIONS["LIGHTYEAR:FOOT"] = \
        Mul(Rat(Int(127), Mul(Int("3941971030242"), Pow(10, 6))), Sym("ly"))  # nopep8
    CONVERSIONS["LIGHTYEAR:GIGAMETER"] = \
        Mul(Rat(Int(625000), Int("5912956545363")), Sym("ly"))  # nopep8
    CONVERSIONS["LIGHTYEAR:HECTOMETER"] = \
        Mul(Rat(Int(1), Int("94607304725808")), Sym("ly"))  # nopep8
    CONVERSIONS["LIGHTYEAR:INCH"] = \
        Mul(Rat(Int(127), Mul(Int("47303652362904"), Pow(10, 6))), Sym("ly"))  # nopep8
    CONVERSIONS["LIGHTYEAR:KILOMETER"] = \
        Mul(Rat(Int(5), Int("47303652362904")), Sym("ly"))  # nopep8
    CONVERSIONS["LIGHTYEAR:LINE"] = \
        Mul(Rat(Int(127), Mul(Int("567643828354848"), Pow(10, 6))), Sym("ly"))  # nopep8
    CONVERSIONS["LIGHTYEAR:MEGAMETER"] = \
        Mul(Rat(Int(625), Int("5912956545363")), Sym("ly"))  # nopep8
    CONVERSIONS["LIGHTYEAR:METER"] = \
        Mul(Rat(Int(1), Int("9460730472580800")), Sym("ly"))  # nopep8
    CONVERSIONS["LIGHTYEAR:MICROMETER"] = \
        Mul(Rat(Int(1), Mul(Int("94607304725808"), Pow(10, 8))), Sym("ly"))  # nopep8
    CONVERSIONS["LIGHTYEAR:MILE"] = \
        Mul(Rat(Int(1397), Int("8212439646337500")), Sym("ly"))  # nopep8
    CONVERSIONS["LIGHTYEAR:MILLIMETER"] = \
        Mul(Rat(Int(1), Mul(Int("94607304725808"), Pow(10, 5))), Sym("ly"))  # nopep8
    CONVERSIONS["LIGHTYEAR:NANOMETER"] = \
        Mul(Rat(Int(1), Mul(Int("94607304725808"), Pow(10, 11))), Sym("ly"))  # nopep8
    CONVERSIONS["LIGHTYEAR:PARSEC"] = \
        Mul(Rat(Mul(Int(6428495), Pow(10, 6)), Int("1970985515121")), Sym("ly"))  # nopep8
    CONVERSIONS["LIGHTYEAR:PETAMETER"] = \
        Mul(Rat(Mul(Int(625), Pow(10, 9)), Int("5912956545363")), Sym("ly"))  # nopep8
    CONVERSIONS["LIGHTYEAR:PICOMETER"] = \
        Mul(Rat(Int(1), Mul(Int("94607304725808"), Pow(10, 14))), Sym("ly"))  # nopep8
    CONVERSIONS["LIGHTYEAR:POINT"] = \
        Mul(Rat(Int(127), Mul(Int("3405862970129088"), Pow(10, 6))), Sym("ly"))  # nopep8
    CONVERSIONS["LIGHTYEAR:TERAMETER"] = \
        Mul(Rat(Mul(Int(625), Pow(10, 6)), Int("5912956545363")), Sym("ly"))  # nopep8
    CONVERSIONS["LIGHTYEAR:THOU"] = \
        Mul(Rat(Int(127), Mul(Int("47303652362904"), Pow(10, 9))), Sym("ly"))  # nopep8
    CONVERSIONS["LIGHTYEAR:YARD"] = \
        Mul(Rat(Int(127), Mul(Int("1313990343414"), Pow(10, 6))), Sym("ly"))  # nopep8
    CONVERSIONS["LIGHTYEAR:YOCTOMETER"] = \
        Mul(Rat(Int(1), Mul(Int("94607304725808"), Pow(10, 26))), Sym("ly"))  # nopep8
    CONVERSIONS["LIGHTYEAR:YOTTAMETER"] = \
        Mul(Rat(Mul(Int(625), Pow(10, 18)), Int("5912956545363")), Sym("ly"))  # nopep8
    CONVERSIONS["LIGHTYEAR:ZEPTOMETER"] = \
        Mul(Rat(Int(1), Mul(Int("94607304725808"), Pow(10, 23))), Sym("ly"))  # nopep8
    CONVERSIONS["LIGHTYEAR:ZETTAMETER"] = \
        Mul(Rat(Mul(Int(625), Pow(10, 15)), Int("5912956545363")), Sym("ly"))  # nopep8
    CONVERSIONS["LINE:ANGSTROM"] = \
        Mul(Rat(Int(3), Mul(Int(635), Pow(10, 5))), Sym("li"))  # nopep8
    CONVERSIONS["LINE:ASTRONOMICALUNIT"] = \
        Mul(Rat(Mul(Int("8975872242"), Pow(10, 6)), Int(127)), Sym("li"))  # nopep8
    CONVERSIONS["LINE:ATTOMETER"] = \
        Mul(Rat(Int(3), Mul(Int(635), Pow(10, 13))), Sym("li"))  # nopep8
    CONVERSIONS["LINE:CENTIMETER"] = \
        Mul(Rat(Int(600), Int(127)), Sym("li"))  # nopep8
    CONVERSIONS["LINE:DECAMETER"] = \
        Mul(Rat(Mul(Int(6), Pow(10, 5)), Int(127)), Sym("li"))  # nopep8
    CONVERSIONS["LINE:DECIMETER"] = \
        Mul(Rat(Int(6000), Int(127)), Sym("li"))  # nopep8
    CONVERSIONS["LINE:EXAMETER"] = \
        Mul(Rat(Mul(Int(6), Pow(10, 22)), Int(127)), Sym("li"))  # nopep8
    CONVERSIONS["LINE:FEMTOMETER"] = \
        Mul(Rat(Int(3), Mul(Int(635), Pow(10, 10))), Sym("li"))  # nopep8
    CONVERSIONS["LINE:FOOT"] = \
        Mul(Int(144), Sym("li"))  # nopep8
    CONVERSIONS["LINE:GIGAMETER"] = \
        Mul(Rat(Mul(Int(6), Pow(10, 13)), Int(127)), Sym("li"))  # nopep8
    CONVERSIONS["LINE:HECTOMETER"] = \
        Mul(Rat(Mul(Int(6), Pow(10, 6)), Int(127)), Sym("li"))  # nopep8
    CONVERSIONS["LINE:INCH"] = \
        Mul(Int(12), Sym("li"))  # nopep8
    CONVERSIONS["LINE:KILOMETER"] = \
        Mul(Rat(Mul(Int(6), Pow(10, 7)), Int(127)), Sym("li"))  # nopep8
    CONVERSIONS["LINE:LIGHTYEAR"] = \
        Mul(Rat(Mul(Int("567643828354848"), Pow(10, 6)), Int(127)), Sym("li"))  # nopep8
    CONVERSIONS["LINE:MEGAMETER"] = \
        Mul(Rat(Mul(Int(6), Pow(10, 10)), Int(127)), Sym("li"))  # nopep8
    CONVERSIONS["LINE:METER"] = \
        Mul(Rat(Mul(Int(6), Pow(10, 4)), Int(127)), Sym("li"))  # nopep8
    CONVERSIONS["LINE:MICROMETER"] = \
        Mul(Rat(Int(3), Int(6350)), Sym("li"))  # nopep8
    CONVERSIONS["LINE:MILE"] = \
        Mul(Int(760320), Sym("li"))  # nopep8
    CONVERSIONS["LINE:MILLIMETER"] = \
        Mul(Rat(Int(60), Int(127)), Sym("li"))  # nopep8
    CONVERSIONS["LINE:NANOMETER"] = \
        Mul(Rat(Int(3), Mul(Int(635), Pow(10, 4))), Sym("li"))  # nopep8
    CONVERSIONS["LINE:PARSEC"] = \
        Mul(Rat(Mul(Int(185140656), Pow(10, 13)), Int(127)), Sym("li"))  # nopep8
    CONVERSIONS["LINE:PETAMETER"] = \
        Mul(Rat(Mul(Int(6), Pow(10, 19)), Int(127)), Sym("li"))  # nopep8
    CONVERSIONS["LINE:PICOMETER"] = \
        Mul(Rat(Int(3), Mul(Int(635), Pow(10, 7))), Sym("li"))  # nopep8
    CONVERSIONS["LINE:POINT"] = \
        Mul(Rat(Int(1), Int(6)), Sym("li"))  # nopep8
    CONVERSIONS["LINE:TERAMETER"] = \
        Mul(Rat(Mul(Int(6), Pow(10, 16)), Int(127)), Sym("li"))  # nopep8
    CONVERSIONS["LINE:THOU"] = \
        Mul(Rat(Int(3), Int(250)), Sym("li"))  # nopep8
    CONVERSIONS["LINE:YARD"] = \
        Mul(Int(432), Sym("li"))  # nopep8
    CONVERSIONS["LINE:YOCTOMETER"] = \
        Mul(Rat(Int(3), Mul(Int(635), Pow(10, 19))), Sym("li"))  # nopep8
    CONVERSIONS["LINE:YOTTAMETER"] = \
        Mul(Rat(Mul(Int(6), Pow(10, 28)), Int(127)), Sym("li"))  # nopep8
    CONVERSIONS["LINE:ZEPTOMETER"] = \
        Mul(Rat(Int(3), Mul(Int(635), Pow(10, 16))), Sym("li"))  # nopep8
    CONVERSIONS["LINE:ZETTAMETER"] = \
        Mul(Rat(Mul(Int(6), Pow(10, 25)), Int(127)), Sym("li"))  # nopep8
    CONVERSIONS["MEGAMETER:ANGSTROM"] = \
        Mul(Rat(Int(1), Pow(10, 16)), Sym("megam"))  # nopep8
    CONVERSIONS["MEGAMETER:ASTRONOMICALUNIT"] = \
        Mul(Rat(Int(1495978707), Pow(10, 4)), Sym("megam"))  # nopep8
    CONVERSIONS["MEGAMETER:ATTOMETER"] = \
        Mul(Rat(Int(1), Pow(10, 24)), Sym("megam"))  # nopep8
    CONVERSIONS["MEGAMETER:CENTIMETER"] = \
        Mul(Rat(Int(1), Pow(10, 8)), Sym("megam"))  # nopep8
    CONVERSIONS["MEGAMETER:DECAMETER"] = \
        Mul(Rat(Int(1), Pow(10, 5)), Sym("megam"))  # nopep8
    CONVERSIONS["MEGAMETER:DECIMETER"] = \
        Mul(Rat(Int(1), Pow(10, 7)), Sym("megam"))  # nopep8
    CONVERSIONS["MEGAMETER:EXAMETER"] = \
        Mul(Pow(10, 12), Sym("megam"))  # nopep8
    CONVERSIONS["MEGAMETER:FEMTOMETER"] = \
        Mul(Rat(Int(1), Pow(10, 21)), Sym("megam"))  # nopep8
    CONVERSIONS["MEGAMETER:FOOT"] = \
        Mul(Rat(Int(381), Mul(Int(125), Pow(10, 7))), Sym("megam"))  # nopep8
    CONVERSIONS["MEGAMETER:GIGAMETER"] = \
        Mul(Int(1000), Sym("megam"))  # nopep8
    CONVERSIONS["MEGAMETER:HECTOMETER"] = \
        Mul(Rat(Int(1), Pow(10, 4)), Sym("megam"))  # nopep8
    CONVERSIONS["MEGAMETER:INCH"] = \
        Mul(Rat(Int(127), Mul(Int(5), Pow(10, 9))), Sym("megam"))  # nopep8
    CONVERSIONS["MEGAMETER:KILOMETER"] = \
        Mul(Rat(Int(1), Int(1000)), Sym("megam"))  # nopep8
    CONVERSIONS["MEGAMETER:LIGHTYEAR"] = \
        Mul(Rat(Int("5912956545363"), Int(625)), Sym("megam"))  # nopep8
    CONVERSIONS["MEGAMETER:LINE"] = \
        Mul(Rat(Int(127), Mul(Int(6), Pow(10, 10))), Sym("megam"))  # nopep8
    CONVERSIONS["MEGAMETER:METER"] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("megam"))  # nopep8
    CONVERSIONS["MEGAMETER:MICROMETER"] = \
        Mul(Rat(Int(1), Pow(10, 12)), Sym("megam"))  # nopep8
    CONVERSIONS["MEGAMETER:MILE"] = \
        Mul(Rat(Int(12573), Int(7812500)), Sym("megam"))  # nopep8
    CONVERSIONS["MEGAMETER:MILLIMETER"] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("megam"))  # nopep8
    CONVERSIONS["MEGAMETER:NANOMETER"] = \
        Mul(Rat(Int(1), Pow(10, 15)), Sym("megam"))  # nopep8
    CONVERSIONS["MEGAMETER:PARSEC"] = \
        Mul(Int("30856776000"), Sym("megam"))  # nopep8
    CONVERSIONS["MEGAMETER:PETAMETER"] = \
        Mul(Pow(10, 9), Sym("megam"))  # nopep8
    CONVERSIONS["MEGAMETER:PICOMETER"] = \
        Mul(Rat(Int(1), Pow(10, 18)), Sym("megam"))  # nopep8
    CONVERSIONS["MEGAMETER:POINT"] = \
        Mul(Rat(Int(127), Mul(Int(36), Pow(10, 10))), Sym("megam"))  # nopep8
    CONVERSIONS["MEGAMETER:TERAMETER"] = \
        Mul(Pow(10, 6), Sym("megam"))  # nopep8
    CONVERSIONS["MEGAMETER:THOU"] = \
        Mul(Rat(Int(127), Mul(Int(5), Pow(10, 12))), Sym("megam"))  # nopep8
    CONVERSIONS["MEGAMETER:YARD"] = \
        Mul(Rat(Int(1143), Mul(Int(125), Pow(10, 7))), Sym("megam"))  # nopep8
    CONVERSIONS["MEGAMETER:YOCTOMETER"] = \
        Mul(Rat(Int(1), Pow(10, 30)), Sym("megam"))  # nopep8
    CONVERSIONS["MEGAMETER:YOTTAMETER"] = \
        Mul(Pow(10, 18), Sym("megam"))  # nopep8
    CONVERSIONS["MEGAMETER:ZEPTOMETER"] = \
        Mul(Rat(Int(1), Pow(10, 27)), Sym("megam"))  # nopep8
    CONVERSIONS["MEGAMETER:ZETTAMETER"] = \
        Mul(Pow(10, 15), Sym("megam"))  # nopep8
    CONVERSIONS["METER:ANGSTROM"] = \
        Mul(Rat(Int(1), Pow(10, 10)), Sym("m"))  # nopep8
    CONVERSIONS["METER:ASTRONOMICALUNIT"] = \
        Mul(Int("149597870700"), Sym("m"))  # nopep8
    CONVERSIONS["METER:ATTOMETER"] = \
        Mul(Rat(Int(1), Pow(10, 18)), Sym("m"))  # nopep8
    CONVERSIONS["METER:CENTIMETER"] = \
        Mul(Rat(Int(1), Int(100)), Sym("m"))  # nopep8
    CONVERSIONS["METER:DECAMETER"] = \
        Mul(Int(10), Sym("m"))  # nopep8
    CONVERSIONS["METER:DECIMETER"] = \
        Mul(Rat(Int(1), Int(10)), Sym("m"))  # nopep8
    CONVERSIONS["METER:EXAMETER"] = \
        Mul(Pow(10, 18), Sym("m"))  # nopep8
    CONVERSIONS["METER:FEMTOMETER"] = \
        Mul(Rat(Int(1), Pow(10, 15)), Sym("m"))  # nopep8
    CONVERSIONS["METER:FOOT"] = \
        Mul(Rat(Int(381), Int(1250)), Sym("m"))  # nopep8
    CONVERSIONS["METER:GIGAMETER"] = \
        Mul(Pow(10, 9), Sym("m"))  # nopep8
    CONVERSIONS["METER:HECTOMETER"] = \
        Mul(Int(100), Sym("m"))  # nopep8
    CONVERSIONS["METER:INCH"] = \
        Mul(Rat(Int(127), Int(5000)), Sym("m"))  # nopep8
    CONVERSIONS["METER:KILOMETER"] = \
        Mul(Int(1000), Sym("m"))  # nopep8
    CONVERSIONS["METER:LIGHTYEAR"] = \
        Mul(Int("9460730472580800"), Sym("m"))  # nopep8
    CONVERSIONS["METER:LINE"] = \
        Mul(Rat(Int(127), Mul(Int(6), Pow(10, 4))), Sym("m"))  # nopep8
    CONVERSIONS["METER:MEGAMETER"] = \
        Mul(Pow(10, 6), Sym("m"))  # nopep8
    CONVERSIONS["METER:MICROMETER"] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("m"))  # nopep8
    CONVERSIONS["METER:MILE"] = \
        Mul(Rat(Int(201168), Int(125)), Sym("m"))  # nopep8
    CONVERSIONS["METER:MILLIMETER"] = \
        Mul(Rat(Int(1), Int(1000)), Sym("m"))  # nopep8
    CONVERSIONS["METER:NANOMETER"] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("m"))  # nopep8
    CONVERSIONS["METER:PARSEC"] = \
        Mul(Mul(Int(30856776), Pow(10, 9)), Sym("m"))  # nopep8
    CONVERSIONS["METER:PETAMETER"] = \
        Mul(Pow(10, 15), Sym("m"))  # nopep8
    CONVERSIONS["METER:PICOMETER"] = \
        Mul(Rat(Int(1), Pow(10, 12)), Sym("m"))  # nopep8
    CONVERSIONS["METER:POINT"] = \
        Mul(Rat(Int(127), Mul(Int(36), Pow(10, 4))), Sym("m"))  # nopep8
    CONVERSIONS["METER:TERAMETER"] = \
        Mul(Pow(10, 12), Sym("m"))  # nopep8
    CONVERSIONS["METER:THOU"] = \
        Mul(Rat(Int(127), Mul(Int(5), Pow(10, 6))), Sym("m"))  # nopep8
    CONVERSIONS["METER:YARD"] = \
        Mul(Rat(Int(1143), Int(1250)), Sym("m"))  # nopep8
    CONVERSIONS["METER:YOCTOMETER"] = \
        Mul(Rat(Int(1), Pow(10, 24)), Sym("m"))  # nopep8
    CONVERSIONS["METER:YOTTAMETER"] = \
        Mul(Pow(10, 24), Sym("m"))  # nopep8
    CONVERSIONS["METER:ZEPTOMETER"] = \
        Mul(Rat(Int(1), Pow(10, 21)), Sym("m"))  # nopep8
    CONVERSIONS["METER:ZETTAMETER"] = \
        Mul(Pow(10, 21), Sym("m"))  # nopep8
    CONVERSIONS["MICROMETER:ANGSTROM"] = \
        Mul(Rat(Int(1), Pow(10, 4)), Sym("microm"))  # nopep8
    CONVERSIONS["MICROMETER:ASTRONOMICALUNIT"] = \
        Mul(Mul(Int(1495978707), Pow(10, 8)), Sym("microm"))  # nopep8
    CONVERSIONS["MICROMETER:ATTOMETER"] = \
        Mul(Rat(Int(1), Pow(10, 12)), Sym("microm"))  # nopep8
    CONVERSIONS["MICROMETER:CENTIMETER"] = \
        Mul(Pow(10, 4), Sym("microm"))  # nopep8
    CONVERSIONS["MICROMETER:DECAMETER"] = \
        Mul(Pow(10, 7), Sym("microm"))  # nopep8
    CONVERSIONS["MICROMETER:DECIMETER"] = \
        Mul(Pow(10, 5), Sym("microm"))  # nopep8
    CONVERSIONS["MICROMETER:EXAMETER"] = \
        Mul(Pow(10, 24), Sym("microm"))  # nopep8
    CONVERSIONS["MICROMETER:FEMTOMETER"] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("microm"))  # nopep8
    CONVERSIONS["MICROMETER:FOOT"] = \
        Mul(Int(304800), Sym("microm"))  # nopep8
    CONVERSIONS["MICROMETER:GIGAMETER"] = \
        Mul(Pow(10, 15), Sym("microm"))  # nopep8
    CONVERSIONS["MICROMETER:HECTOMETER"] = \
        Mul(Pow(10, 8), Sym("microm"))  # nopep8
    CONVERSIONS["MICROMETER:INCH"] = \
        Mul(Int(25400), Sym("microm"))  # nopep8
    CONVERSIONS["MICROMETER:KILOMETER"] = \
        Mul(Pow(10, 9), Sym("microm"))  # nopep8
    CONVERSIONS["MICROMETER:LIGHTYEAR"] = \
        Mul(Mul(Int("94607304725808"), Pow(10, 8)), Sym("microm"))  # nopep8
    CONVERSIONS["MICROMETER:LINE"] = \
        Mul(Rat(Int(6350), Int(3)), Sym("microm"))  # nopep8
    CONVERSIONS["MICROMETER:MEGAMETER"] = \
        Mul(Pow(10, 12), Sym("microm"))  # nopep8
    CONVERSIONS["MICROMETER:METER"] = \
        Mul(Pow(10, 6), Sym("microm"))  # nopep8
    CONVERSIONS["MICROMETER:MILE"] = \
        Mul(Int(1609344000), Sym("microm"))  # nopep8
    CONVERSIONS["MICROMETER:MILLIMETER"] = \
        Mul(Int(1000), Sym("microm"))  # nopep8
    CONVERSIONS["MICROMETER:NANOMETER"] = \
        Mul(Rat(Int(1), Int(1000)), Sym("microm"))  # nopep8
    CONVERSIONS["MICROMETER:PARSEC"] = \
        Mul(Mul(Int(30856776), Pow(10, 15)), Sym("microm"))  # nopep8
    CONVERSIONS["MICROMETER:PETAMETER"] = \
        Mul(Pow(10, 21), Sym("microm"))  # nopep8
    CONVERSIONS["MICROMETER:PICOMETER"] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("microm"))  # nopep8
    CONVERSIONS["MICROMETER:POINT"] = \
        Mul(Rat(Int(3175), Int(9)), Sym("microm"))  # nopep8
    CONVERSIONS["MICROMETER:TERAMETER"] = \
        Mul(Pow(10, 18), Sym("microm"))  # nopep8
    CONVERSIONS["MICROMETER:THOU"] = \
        Mul(Rat(Int(127), Int(5)), Sym("microm"))  # nopep8
    CONVERSIONS["MICROMETER:YARD"] = \
        Mul(Int(914400), Sym("microm"))  # nopep8
    CONVERSIONS["MICROMETER:YOCTOMETER"] = \
        Mul(Rat(Int(1), Pow(10, 18)), Sym("microm"))  # nopep8
    CONVERSIONS["MICROMETER:YOTTAMETER"] = \
        Mul(Pow(10, 30), Sym("microm"))  # nopep8
    CONVERSIONS["MICROMETER:ZEPTOMETER"] = \
        Mul(Rat(Int(1), Pow(10, 15)), Sym("microm"))  # nopep8
    CONVERSIONS["MICROMETER:ZETTAMETER"] = \
        Mul(Pow(10, 27), Sym("microm"))  # nopep8
    CONVERSIONS["MILE:ANGSTROM"] = \
        Mul(Rat(Int(1), Mul(Int(1609344), Pow(10, 7))), Sym("mi"))  # nopep8
    CONVERSIONS["MILE:ASTRONOMICALUNIT"] = \
        Mul(Rat(Int("1558311153125"), Int(16764)), Sym("mi"))  # nopep8
    CONVERSIONS["MILE:ATTOMETER"] = \
        Mul(Rat(Int(1), Mul(Int(1609344), Pow(10, 15))), Sym("mi"))  # nopep8
    CONVERSIONS["MILE:CENTIMETER"] = \
        Mul(Rat(Int(5), Int(804672)), Sym("mi"))  # nopep8
    CONVERSIONS["MILE:DECAMETER"] = \
        Mul(Rat(Int(625), Int(100584)), Sym("mi"))  # nopep8
    CONVERSIONS["MILE:DECIMETER"] = \
        Mul(Rat(Int(25), Int(402336)), Sym("mi"))  # nopep8
    CONVERSIONS["MILE:EXAMETER"] = \
        Mul(Rat(Mul(Int(78125), Pow(10, 14)), Int(12573)), Sym("mi"))  # nopep8
    CONVERSIONS["MILE:FEMTOMETER"] = \
        Mul(Rat(Int(1), Mul(Int(1609344), Pow(10, 12))), Sym("mi"))  # nopep8
    CONVERSIONS["MILE:FOOT"] = \
        Mul(Rat(Int(1), Int(5280)), Sym("mi"))  # nopep8
    CONVERSIONS["MILE:GIGAMETER"] = \
        Mul(Rat(Mul(Int(78125), Pow(10, 5)), Int(12573)), Sym("mi"))  # nopep8
    CONVERSIONS["MILE:HECTOMETER"] = \
        Mul(Rat(Int(3125), Int(50292)), Sym("mi"))  # nopep8
    CONVERSIONS["MILE:INCH"] = \
        Mul(Rat(Int(1), Int(63360)), Sym("mi"))  # nopep8
    CONVERSIONS["MILE:KILOMETER"] = \
        Mul(Rat(Int(15625), Int(25146)), Sym("mi"))  # nopep8
    CONVERSIONS["MILE:LIGHTYEAR"] = \
        Mul(Rat(Int("8212439646337500"), Int(1397)), Sym("mi"))  # nopep8
    CONVERSIONS["MILE:LINE"] = \
        Mul(Rat(Int(1), Int(760320)), Sym("mi"))  # nopep8
    CONVERSIONS["MILE:MEGAMETER"] = \
        Mul(Rat(Int(7812500), Int(12573)), Sym("mi"))  # nopep8
    CONVERSIONS["MILE:METER"] = \
        Mul(Rat(Int(125), Int(201168)), Sym("mi"))  # nopep8
    CONVERSIONS["MILE:MICROMETER"] = \
        Mul(Rat(Int(1), Int(1609344000)), Sym("mi"))  # nopep8
    CONVERSIONS["MILE:MILLIMETER"] = \
        Mul(Rat(Int(1), Int(1609344)), Sym("mi"))  # nopep8
    CONVERSIONS["MILE:NANOMETER"] = \
        Mul(Rat(Int(1), Mul(Int(1609344), Pow(10, 6))), Sym("mi"))  # nopep8
    CONVERSIONS["MILE:PARSEC"] = \
        Mul(Rat(Mul(Int(803561875), Pow(10, 8)), Int(4191)), Sym("mi"))  # nopep8
    CONVERSIONS["MILE:PETAMETER"] = \
        Mul(Rat(Mul(Int(78125), Pow(10, 11)), Int(12573)), Sym("mi"))  # nopep8
    CONVERSIONS["MILE:PICOMETER"] = \
        Mul(Rat(Int(1), Mul(Int(1609344), Pow(10, 9))), Sym("mi"))  # nopep8
    CONVERSIONS["MILE:POINT"] = \
        Mul(Rat(Int(1), Int(4561920)), Sym("mi"))  # nopep8
    CONVERSIONS["MILE:TERAMETER"] = \
        Mul(Rat(Mul(Int(78125), Pow(10, 8)), Int(12573)), Sym("mi"))  # nopep8
    CONVERSIONS["MILE:THOU"] = \
        Mul(Rat(Int(1), Mul(Int(6336), Pow(10, 4))), Sym("mi"))  # nopep8
    CONVERSIONS["MILE:YARD"] = \
        Mul(Rat(Int(1), Int(1760)), Sym("mi"))  # nopep8
    CONVERSIONS["MILE:YOCTOMETER"] = \
        Mul(Rat(Int(1), Mul(Int(1609344), Pow(10, 21))), Sym("mi"))  # nopep8
    CONVERSIONS["MILE:YOTTAMETER"] = \
        Mul(Rat(Mul(Int(78125), Pow(10, 20)), Int(12573)), Sym("mi"))  # nopep8
    CONVERSIONS["MILE:ZEPTOMETER"] = \
        Mul(Rat(Int(1), Mul(Int(1609344), Pow(10, 18))), Sym("mi"))  # nopep8
    CONVERSIONS["MILE:ZETTAMETER"] = \
        Mul(Rat(Mul(Int(78125), Pow(10, 17)), Int(12573)), Sym("mi"))  # nopep8
    CONVERSIONS["MILLIMETER:ANGSTROM"] = \
        Mul(Rat(Int(1), Pow(10, 7)), Sym("millim"))  # nopep8
    CONVERSIONS["MILLIMETER:ASTRONOMICALUNIT"] = \
        Mul(Mul(Int(1495978707), Pow(10, 5)), Sym("millim"))  # nopep8
    CONVERSIONS["MILLIMETER:ATTOMETER"] = \
        Mul(Rat(Int(1), Pow(10, 15)), Sym("millim"))  # nopep8
    CONVERSIONS["MILLIMETER:CENTIMETER"] = \
        Mul(Int(10), Sym("millim"))  # nopep8
    CONVERSIONS["MILLIMETER:DECAMETER"] = \
        Mul(Pow(10, 4), Sym("millim"))  # nopep8
    CONVERSIONS["MILLIMETER:DECIMETER"] = \
        Mul(Int(100), Sym("millim"))  # nopep8
    CONVERSIONS["MILLIMETER:EXAMETER"] = \
        Mul(Pow(10, 21), Sym("millim"))  # nopep8
    CONVERSIONS["MILLIMETER:FEMTOMETER"] = \
        Mul(Rat(Int(1), Pow(10, 12)), Sym("millim"))  # nopep8
    CONVERSIONS["MILLIMETER:FOOT"] = \
        Mul(Rat(Int(1524), Int(5)), Sym("millim"))  # nopep8
    CONVERSIONS["MILLIMETER:GIGAMETER"] = \
        Mul(Pow(10, 12), Sym("millim"))  # nopep8
    CONVERSIONS["MILLIMETER:HECTOMETER"] = \
        Mul(Pow(10, 5), Sym("millim"))  # nopep8
    CONVERSIONS["MILLIMETER:INCH"] = \
        Mul(Rat(Int(127), Int(5)), Sym("millim"))  # nopep8
    CONVERSIONS["MILLIMETER:KILOMETER"] = \
        Mul(Pow(10, 6), Sym("millim"))  # nopep8
    CONVERSIONS["MILLIMETER:LIGHTYEAR"] = \
        Mul(Mul(Int("94607304725808"), Pow(10, 5)), Sym("millim"))  # nopep8
    CONVERSIONS["MILLIMETER:LINE"] = \
        Mul(Rat(Int(127), Int(60)), Sym("millim"))  # nopep8
    CONVERSIONS["MILLIMETER:MEGAMETER"] = \
        Mul(Pow(10, 9), Sym("millim"))  # nopep8
    CONVERSIONS["MILLIMETER:METER"] = \
        Mul(Int(1000), Sym("millim"))  # nopep8
    CONVERSIONS["MILLIMETER:MICROMETER"] = \
        Mul(Rat(Int(1), Int(1000)), Sym("millim"))  # nopep8
    CONVERSIONS["MILLIMETER:MILE"] = \
        Mul(Int(1609344), Sym("millim"))  # nopep8
    CONVERSIONS["MILLIMETER:NANOMETER"] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("millim"))  # nopep8
    CONVERSIONS["MILLIMETER:PARSEC"] = \
        Mul(Mul(Int(30856776), Pow(10, 12)), Sym("millim"))  # nopep8
    CONVERSIONS["MILLIMETER:PETAMETER"] = \
        Mul(Pow(10, 18), Sym("millim"))  # nopep8
    CONVERSIONS["MILLIMETER:PICOMETER"] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("millim"))  # nopep8
    CONVERSIONS["MILLIMETER:POINT"] = \
        Mul(Rat(Int(127), Int(360)), Sym("millim"))  # nopep8
    CONVERSIONS["MILLIMETER:TERAMETER"] = \
        Mul(Pow(10, 15), Sym("millim"))  # nopep8
    CONVERSIONS["MILLIMETER:THOU"] = \
        Mul(Rat(Int(127), Int(5000)), Sym("millim"))  # nopep8
    CONVERSIONS["MILLIMETER:YARD"] = \
        Mul(Rat(Int(4572), Int(5)), Sym("millim"))  # nopep8
    CONVERSIONS["MILLIMETER:YOCTOMETER"] = \
        Mul(Rat(Int(1), Pow(10, 21)), Sym("millim"))  # nopep8
    CONVERSIONS["MILLIMETER:YOTTAMETER"] = \
        Mul(Pow(10, 27), Sym("millim"))  # nopep8
    CONVERSIONS["MILLIMETER:ZEPTOMETER"] = \
        Mul(Rat(Int(1), Pow(10, 18)), Sym("millim"))  # nopep8
    CONVERSIONS["MILLIMETER:ZETTAMETER"] = \
        Mul(Pow(10, 24), Sym("millim"))  # nopep8
    CONVERSIONS["NANOMETER:ANGSTROM"] = \
        Mul(Rat(Int(1), Int(10)), Sym("nanom"))  # nopep8
    CONVERSIONS["NANOMETER:ASTRONOMICALUNIT"] = \
        Mul(Mul(Int(1495978707), Pow(10, 11)), Sym("nanom"))  # nopep8
    CONVERSIONS["NANOMETER:ATTOMETER"] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("nanom"))  # nopep8
    CONVERSIONS["NANOMETER:CENTIMETER"] = \
        Mul(Pow(10, 7), Sym("nanom"))  # nopep8
    CONVERSIONS["NANOMETER:DECAMETER"] = \
        Mul(Pow(10, 10), Sym("nanom"))  # nopep8
    CONVERSIONS["NANOMETER:DECIMETER"] = \
        Mul(Pow(10, 8), Sym("nanom"))  # nopep8
    CONVERSIONS["NANOMETER:EXAMETER"] = \
        Mul(Pow(10, 27), Sym("nanom"))  # nopep8
    CONVERSIONS["NANOMETER:FEMTOMETER"] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("nanom"))  # nopep8
    CONVERSIONS["NANOMETER:FOOT"] = \
        Mul(Mul(Int(3048), Pow(10, 5)), Sym("nanom"))  # nopep8
    CONVERSIONS["NANOMETER:GIGAMETER"] = \
        Mul(Pow(10, 18), Sym("nanom"))  # nopep8
    CONVERSIONS["NANOMETER:HECTOMETER"] = \
        Mul(Pow(10, 11), Sym("nanom"))  # nopep8
    CONVERSIONS["NANOMETER:INCH"] = \
        Mul(Mul(Int(254), Pow(10, 5)), Sym("nanom"))  # nopep8
    CONVERSIONS["NANOMETER:KILOMETER"] = \
        Mul(Pow(10, 12), Sym("nanom"))  # nopep8
    CONVERSIONS["NANOMETER:LIGHTYEAR"] = \
        Mul(Mul(Int("94607304725808"), Pow(10, 11)), Sym("nanom"))  # nopep8
    CONVERSIONS["NANOMETER:LINE"] = \
        Mul(Rat(Mul(Int(635), Pow(10, 4)), Int(3)), Sym("nanom"))  # nopep8
    CONVERSIONS["NANOMETER:MEGAMETER"] = \
        Mul(Pow(10, 15), Sym("nanom"))  # nopep8
    CONVERSIONS["NANOMETER:METER"] = \
        Mul(Pow(10, 9), Sym("nanom"))  # nopep8
    CONVERSIONS["NANOMETER:MICROMETER"] = \
        Mul(Int(1000), Sym("nanom"))  # nopep8
    CONVERSIONS["NANOMETER:MILE"] = \
        Mul(Mul(Int(1609344), Pow(10, 6)), Sym("nanom"))  # nopep8
    CONVERSIONS["NANOMETER:MILLIMETER"] = \
        Mul(Pow(10, 6), Sym("nanom"))  # nopep8
    CONVERSIONS["NANOMETER:PARSEC"] = \
        Mul(Mul(Int(30856776), Pow(10, 18)), Sym("nanom"))  # nopep8
    CONVERSIONS["NANOMETER:PETAMETER"] = \
        Mul(Pow(10, 24), Sym("nanom"))  # nopep8
    CONVERSIONS["NANOMETER:PICOMETER"] = \
        Mul(Rat(Int(1), Int(1000)), Sym("nanom"))  # nopep8
    CONVERSIONS["NANOMETER:POINT"] = \
        Mul(Rat(Int(3175000), Int(9)), Sym("nanom"))  # nopep8
    CONVERSIONS["NANOMETER:TERAMETER"] = \
        Mul(Pow(10, 21), Sym("nanom"))  # nopep8
    CONVERSIONS["NANOMETER:THOU"] = \
        Mul(Int(25400), Sym("nanom"))  # nopep8
    CONVERSIONS["NANOMETER:YARD"] = \
        Mul(Mul(Int(9144), Pow(10, 5)), Sym("nanom"))  # nopep8
    CONVERSIONS["NANOMETER:YOCTOMETER"] = \
        Mul(Rat(Int(1), Pow(10, 15)), Sym("nanom"))  # nopep8
    CONVERSIONS["NANOMETER:YOTTAMETER"] = \
        Mul(Pow(10, 33), Sym("nanom"))  # nopep8
    CONVERSIONS["NANOMETER:ZEPTOMETER"] = \
        Mul(Rat(Int(1), Pow(10, 12)), Sym("nanom"))  # nopep8
    CONVERSIONS["NANOMETER:ZETTAMETER"] = \
        Mul(Pow(10, 30), Sym("nanom"))  # nopep8
    CONVERSIONS["PARSEC:ANGSTROM"] = \
        Mul(Rat(Int(1), Mul(Int(30856776), Pow(10, 19))), Sym("pc"))  # nopep8
    CONVERSIONS["PARSEC:ASTRONOMICALUNIT"] = \
        Mul(Rat(Int(498659569), Mul(Int(10285592), Pow(10, 7))), Sym("pc"))  # nopep8
    CONVERSIONS["PARSEC:ATTOMETER"] = \
        Mul(Rat(Int(1), Mul(Int(30856776), Pow(10, 27))), Sym("pc"))  # nopep8
    CONVERSIONS["PARSEC:CENTIMETER"] = \
        Mul(Rat(Int(1), Mul(Int(30856776), Pow(10, 11))), Sym("pc"))  # nopep8
    CONVERSIONS["PARSEC:DECAMETER"] = \
        Mul(Rat(Int(1), Mul(Int(30856776), Pow(10, 8))), Sym("pc"))  # nopep8
    CONVERSIONS["PARSEC:DECIMETER"] = \
        Mul(Rat(Int(1), Mul(Int(30856776), Pow(10, 10))), Sym("pc"))  # nopep8
    CONVERSIONS["PARSEC:EXAMETER"] = \
        Mul(Rat(Mul(Int(125), Pow(10, 6)), Int(3857097)), Sym("pc"))  # nopep8
    CONVERSIONS["PARSEC:FEMTOMETER"] = \
        Mul(Rat(Int(1), Mul(Int(30856776), Pow(10, 24))), Sym("pc"))  # nopep8
    CONVERSIONS["PARSEC:FOOT"] = \
        Mul(Rat(Int(127), Mul(Int(1285699), Pow(10, 13))), Sym("pc"))  # nopep8
    CONVERSIONS["PARSEC:GIGAMETER"] = \
        Mul(Rat(Int(1), Int(30856776)), Sym("pc"))  # nopep8
    CONVERSIONS["PARSEC:HECTOMETER"] = \
        Mul(Rat(Int(1), Mul(Int(30856776), Pow(10, 7))), Sym("pc"))  # nopep8
    CONVERSIONS["PARSEC:INCH"] = \
        Mul(Rat(Int(127), Mul(Int(15428388), Pow(10, 13))), Sym("pc"))  # nopep8
    CONVERSIONS["PARSEC:KILOMETER"] = \
        Mul(Rat(Int(1), Mul(Int(30856776), Pow(10, 6))), Sym("pc"))  # nopep8
    CONVERSIONS["PARSEC:LIGHTYEAR"] = \
        Mul(Rat(Int("1970985515121"), Mul(Int(6428495), Pow(10, 6))), Sym("pc"))  # nopep8
    CONVERSIONS["PARSEC:LINE"] = \
        Mul(Rat(Int(127), Mul(Int(185140656), Pow(10, 13))), Sym("pc"))  # nopep8
    CONVERSIONS["PARSEC:MEGAMETER"] = \
        Mul(Rat(Int(1), Int("30856776000")), Sym("pc"))  # nopep8
    CONVERSIONS["PARSEC:METER"] = \
        Mul(Rat(Int(1), Mul(Int(30856776), Pow(10, 9))), Sym("pc"))  # nopep8
    CONVERSIONS["PARSEC:MICROMETER"] = \
        Mul(Rat(Int(1), Mul(Int(30856776), Pow(10, 15))), Sym("pc"))  # nopep8
    CONVERSIONS["PARSEC:MILE"] = \
        Mul(Rat(Int(4191), Mul(Int(803561875), Pow(10, 8))), Sym("pc"))  # nopep8
    CONVERSIONS["PARSEC:MILLIMETER"] = \
        Mul(Rat(Int(1), Mul(Int(30856776), Pow(10, 12))), Sym("pc"))  # nopep8
    CONVERSIONS["PARSEC:NANOMETER"] = \
        Mul(Rat(Int(1), Mul(Int(30856776), Pow(10, 18))), Sym("pc"))  # nopep8
    CONVERSIONS["PARSEC:PETAMETER"] = \
        Mul(Rat(Int(125000), Int(3857097)), Sym("pc"))  # nopep8
    CONVERSIONS["PARSEC:PICOMETER"] = \
        Mul(Rat(Int(1), Mul(Int(30856776), Pow(10, 21))), Sym("pc"))  # nopep8
    CONVERSIONS["PARSEC:POINT"] = \
        Mul(Rat(Int(127), Mul(Int(1110843936), Pow(10, 13))), Sym("pc"))  # nopep8
    CONVERSIONS["PARSEC:TERAMETER"] = \
        Mul(Rat(Int(125), Int(3857097)), Sym("pc"))  # nopep8
    CONVERSIONS["PARSEC:THOU"] = \
        Mul(Rat(Int(127), Mul(Int(15428388), Pow(10, 16))), Sym("pc"))  # nopep8
    CONVERSIONS["PARSEC:YARD"] = \
        Mul(Rat(Int(381), Mul(Int(1285699), Pow(10, 13))), Sym("pc"))  # nopep8
    CONVERSIONS["PARSEC:YOCTOMETER"] = \
        Mul(Rat(Int(1), Mul(Int(30856776), Pow(10, 33))), Sym("pc"))  # nopep8
    CONVERSIONS["PARSEC:YOTTAMETER"] = \
        Mul(Rat(Mul(Int(125), Pow(10, 12)), Int(3857097)), Sym("pc"))  # nopep8
    CONVERSIONS["PARSEC:ZEPTOMETER"] = \
        Mul(Rat(Int(1), Mul(Int(30856776), Pow(10, 30))), Sym("pc"))  # nopep8
    CONVERSIONS["PARSEC:ZETTAMETER"] = \
        Mul(Rat(Mul(Int(125), Pow(10, 9)), Int(3857097)), Sym("pc"))  # nopep8
    CONVERSIONS["PETAMETER:ANGSTROM"] = \
        Mul(Rat(Int(1), Pow(10, 25)), Sym("petam"))  # nopep8
    CONVERSIONS["PETAMETER:ASTRONOMICALUNIT"] = \
        Mul(Rat(Int(1495978707), Pow(10, 13)), Sym("petam"))  # nopep8
    CONVERSIONS["PETAMETER:ATTOMETER"] = \
        Mul(Rat(Int(1), Pow(10, 33)), Sym("petam"))  # nopep8
    CONVERSIONS["PETAMETER:CENTIMETER"] = \
        Mul(Rat(Int(1), Pow(10, 17)), Sym("petam"))  # nopep8
    CONVERSIONS["PETAMETER:DECAMETER"] = \
        Mul(Rat(Int(1), Pow(10, 14)), Sym("petam"))  # nopep8
    CONVERSIONS["PETAMETER:DECIMETER"] = \
        Mul(Rat(Int(1), Pow(10, 16)), Sym("petam"))  # nopep8
    CONVERSIONS["PETAMETER:EXAMETER"] = \
        Mul(Int(1000), Sym("petam"))  # nopep8
    CONVERSIONS["PETAMETER:FEMTOMETER"] = \
        Mul(Rat(Int(1), Pow(10, 30)), Sym("petam"))  # nopep8
    CONVERSIONS["PETAMETER:FOOT"] = \
        Mul(Rat(Int(381), Mul(Int(125), Pow(10, 16))), Sym("petam"))  # nopep8
    CONVERSIONS["PETAMETER:GIGAMETER"] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("petam"))  # nopep8
    CONVERSIONS["PETAMETER:HECTOMETER"] = \
        Mul(Rat(Int(1), Pow(10, 13)), Sym("petam"))  # nopep8
    CONVERSIONS["PETAMETER:INCH"] = \
        Mul(Rat(Int(127), Mul(Int(5), Pow(10, 18))), Sym("petam"))  # nopep8
    CONVERSIONS["PETAMETER:KILOMETER"] = \
        Mul(Rat(Int(1), Pow(10, 12)), Sym("petam"))  # nopep8
    CONVERSIONS["PETAMETER:LIGHTYEAR"] = \
        Mul(Rat(Int("5912956545363"), Mul(Int(625), Pow(10, 9))), Sym("petam"))  # nopep8
    CONVERSIONS["PETAMETER:LINE"] = \
        Mul(Rat(Int(127), Mul(Int(6), Pow(10, 19))), Sym("petam"))  # nopep8
    CONVERSIONS["PETAMETER:MEGAMETER"] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("petam"))  # nopep8
    CONVERSIONS["PETAMETER:METER"] = \
        Mul(Rat(Int(1), Pow(10, 15)), Sym("petam"))  # nopep8
    CONVERSIONS["PETAMETER:MICROMETER"] = \
        Mul(Rat(Int(1), Pow(10, 21)), Sym("petam"))  # nopep8
    CONVERSIONS["PETAMETER:MILE"] = \
        Mul(Rat(Int(12573), Mul(Int(78125), Pow(10, 11))), Sym("petam"))  # nopep8
    CONVERSIONS["PETAMETER:MILLIMETER"] = \
        Mul(Rat(Int(1), Pow(10, 18)), Sym("petam"))  # nopep8
    CONVERSIONS["PETAMETER:NANOMETER"] = \
        Mul(Rat(Int(1), Pow(10, 24)), Sym("petam"))  # nopep8
    CONVERSIONS["PETAMETER:PARSEC"] = \
        Mul(Rat(Int(3857097), Int(125000)), Sym("petam"))  # nopep8
    CONVERSIONS["PETAMETER:PICOMETER"] = \
        Mul(Rat(Int(1), Pow(10, 27)), Sym("petam"))  # nopep8
    CONVERSIONS["PETAMETER:POINT"] = \
        Mul(Rat(Int(127), Mul(Int(36), Pow(10, 19))), Sym("petam"))  # nopep8
    CONVERSIONS["PETAMETER:TERAMETER"] = \
        Mul(Rat(Int(1), Int(1000)), Sym("petam"))  # nopep8
    CONVERSIONS["PETAMETER:THOU"] = \
        Mul(Rat(Int(127), Mul(Int(5), Pow(10, 21))), Sym("petam"))  # nopep8
    CONVERSIONS["PETAMETER:YARD"] = \
        Mul(Rat(Int(1143), Mul(Int(125), Pow(10, 16))), Sym("petam"))  # nopep8
    CONVERSIONS["PETAMETER:YOCTOMETER"] = \
        Mul(Rat(Int(1), Pow(10, 39)), Sym("petam"))  # nopep8
    CONVERSIONS["PETAMETER:YOTTAMETER"] = \
        Mul(Pow(10, 9), Sym("petam"))  # nopep8
    CONVERSIONS["PETAMETER:ZEPTOMETER"] = \
        Mul(Rat(Int(1), Pow(10, 36)), Sym("petam"))  # nopep8
    CONVERSIONS["PETAMETER:ZETTAMETER"] = \
        Mul(Pow(10, 6), Sym("petam"))  # nopep8
    CONVERSIONS["PICOMETER:ANGSTROM"] = \
        Mul(Int(100), Sym("picom"))  # nopep8
    CONVERSIONS["PICOMETER:ASTRONOMICALUNIT"] = \
        Mul(Mul(Int(1495978707), Pow(10, 14)), Sym("picom"))  # nopep8
    CONVERSIONS["PICOMETER:ATTOMETER"] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("picom"))  # nopep8
    CONVERSIONS["PICOMETER:CENTIMETER"] = \
        Mul(Pow(10, 10), Sym("picom"))  # nopep8
    CONVERSIONS["PICOMETER:DECAMETER"] = \
        Mul(Pow(10, 13), Sym("picom"))  # nopep8
    CONVERSIONS["PICOMETER:DECIMETER"] = \
        Mul(Pow(10, 11), Sym("picom"))  # nopep8
    CONVERSIONS["PICOMETER:EXAMETER"] = \
        Mul(Pow(10, 30), Sym("picom"))  # nopep8
    CONVERSIONS["PICOMETER:FEMTOMETER"] = \
        Mul(Rat(Int(1), Int(1000)), Sym("picom"))  # nopep8
    CONVERSIONS["PICOMETER:FOOT"] = \
        Mul(Mul(Int(3048), Pow(10, 8)), Sym("picom"))  # nopep8
    CONVERSIONS["PICOMETER:GIGAMETER"] = \
        Mul(Pow(10, 21), Sym("picom"))  # nopep8
    CONVERSIONS["PICOMETER:HECTOMETER"] = \
        Mul(Pow(10, 14), Sym("picom"))  # nopep8
    CONVERSIONS["PICOMETER:INCH"] = \
        Mul(Mul(Int(254), Pow(10, 8)), Sym("picom"))  # nopep8
    CONVERSIONS["PICOMETER:KILOMETER"] = \
        Mul(Pow(10, 15), Sym("picom"))  # nopep8
    CONVERSIONS["PICOMETER:LIGHTYEAR"] = \
        Mul(Mul(Int("94607304725808"), Pow(10, 14)), Sym("picom"))  # nopep8
    CONVERSIONS["PICOMETER:LINE"] = \
        Mul(Rat(Mul(Int(635), Pow(10, 7)), Int(3)), Sym("picom"))  # nopep8
    CONVERSIONS["PICOMETER:MEGAMETER"] = \
        Mul(Pow(10, 18), Sym("picom"))  # nopep8
    CONVERSIONS["PICOMETER:METER"] = \
        Mul(Pow(10, 12), Sym("picom"))  # nopep8
    CONVERSIONS["PICOMETER:MICROMETER"] = \
        Mul(Pow(10, 6), Sym("picom"))  # nopep8
    CONVERSIONS["PICOMETER:MILE"] = \
        Mul(Mul(Int(1609344), Pow(10, 9)), Sym("picom"))  # nopep8
    CONVERSIONS["PICOMETER:MILLIMETER"] = \
        Mul(Pow(10, 9), Sym("picom"))  # nopep8
    CONVERSIONS["PICOMETER:NANOMETER"] = \
        Mul(Int(1000), Sym("picom"))  # nopep8
    CONVERSIONS["PICOMETER:PARSEC"] = \
        Mul(Mul(Int(30856776), Pow(10, 21)), Sym("picom"))  # nopep8
    CONVERSIONS["PICOMETER:PETAMETER"] = \
        Mul(Pow(10, 27), Sym("picom"))  # nopep8
    CONVERSIONS["PICOMETER:POINT"] = \
        Mul(Rat(Mul(Int(3175), Pow(10, 6)), Int(9)), Sym("picom"))  # nopep8
    CONVERSIONS["PICOMETER:TERAMETER"] = \
        Mul(Pow(10, 24), Sym("picom"))  # nopep8
    CONVERSIONS["PICOMETER:THOU"] = \
        Mul(Mul(Int(254), Pow(10, 5)), Sym("picom"))  # nopep8
    CONVERSIONS["PICOMETER:YARD"] = \
        Mul(Mul(Int(9144), Pow(10, 8)), Sym("picom"))  # nopep8
    CONVERSIONS["PICOMETER:YOCTOMETER"] = \
        Mul(Rat(Int(1), Pow(10, 12)), Sym("picom"))  # nopep8
    CONVERSIONS["PICOMETER:YOTTAMETER"] = \
        Mul(Pow(10, 36), Sym("picom"))  # nopep8
    CONVERSIONS["PICOMETER:ZEPTOMETER"] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("picom"))  # nopep8
    CONVERSIONS["PICOMETER:ZETTAMETER"] = \
        Mul(Pow(10, 33), Sym("picom"))  # nopep8
    CONVERSIONS["POINT:ANGSTROM"] = \
        Mul(Rat(Int(9), Mul(Int(3175), Pow(10, 4))), Sym("pt"))  # nopep8
    CONVERSIONS["POINT:ASTRONOMICALUNIT"] = \
        Mul(Rat(Mul(Int("53855233452"), Pow(10, 6)), Int(127)), Sym("pt"))  # nopep8
    CONVERSIONS["POINT:ATTOMETER"] = \
        Mul(Rat(Int(9), Mul(Int(3175), Pow(10, 12))), Sym("pt"))  # nopep8
    CONVERSIONS["POINT:CENTIMETER"] = \
        Mul(Rat(Int(3600), Int(127)), Sym("pt"))  # nopep8
    CONVERSIONS["POINT:DECAMETER"] = \
        Mul(Rat(Mul(Int(36), Pow(10, 5)), Int(127)), Sym("pt"))  # nopep8
    CONVERSIONS["POINT:DECIMETER"] = \
        Mul(Rat(Int(36000), Int(127)), Sym("pt"))  # nopep8
    CONVERSIONS["POINT:EXAMETER"] = \
        Mul(Rat(Mul(Int(36), Pow(10, 22)), Int(127)), Sym("pt"))  # nopep8
    CONVERSIONS["POINT:FEMTOMETER"] = \
        Mul(Rat(Int(9), Mul(Int(3175), Pow(10, 9))), Sym("pt"))  # nopep8
    CONVERSIONS["POINT:FOOT"] = \
        Mul(Int(864), Sym("pt"))  # nopep8
    CONVERSIONS["POINT:GIGAMETER"] = \
        Mul(Rat(Mul(Int(36), Pow(10, 13)), Int(127)), Sym("pt"))  # nopep8
    CONVERSIONS["POINT:HECTOMETER"] = \
        Mul(Rat(Mul(Int(36), Pow(10, 6)), Int(127)), Sym("pt"))  # nopep8
    CONVERSIONS["POINT:INCH"] = \
        Mul(Int(72), Sym("pt"))  # nopep8
    CONVERSIONS["POINT:KILOMETER"] = \
        Mul(Rat(Mul(Int(36), Pow(10, 7)), Int(127)), Sym("pt"))  # nopep8
    CONVERSIONS["POINT:LIGHTYEAR"] = \
        Mul(Rat(Mul(Int("3405862970129088"), Pow(10, 6)), Int(127)), Sym("pt"))  # nopep8
    CONVERSIONS["POINT:LINE"] = \
        Mul(Int(6), Sym("pt"))  # nopep8
    CONVERSIONS["POINT:MEGAMETER"] = \
        Mul(Rat(Mul(Int(36), Pow(10, 10)), Int(127)), Sym("pt"))  # nopep8
    CONVERSIONS["POINT:METER"] = \
        Mul(Rat(Mul(Int(36), Pow(10, 4)), Int(127)), Sym("pt"))  # nopep8
    CONVERSIONS["POINT:MICROMETER"] = \
        Mul(Rat(Int(9), Int(3175)), Sym("pt"))  # nopep8
    CONVERSIONS["POINT:MILE"] = \
        Mul(Int(4561920), Sym("pt"))  # nopep8
    CONVERSIONS["POINT:MILLIMETER"] = \
        Mul(Rat(Int(360), Int(127)), Sym("pt"))  # nopep8
    CONVERSIONS["POINT:NANOMETER"] = \
        Mul(Rat(Int(9), Int(3175000)), Sym("pt"))  # nopep8
    CONVERSIONS["POINT:PARSEC"] = \
        Mul(Rat(Mul(Int(1110843936), Pow(10, 13)), Int(127)), Sym("pt"))  # nopep8
    CONVERSIONS["POINT:PETAMETER"] = \
        Mul(Rat(Mul(Int(36), Pow(10, 19)), Int(127)), Sym("pt"))  # nopep8
    CONVERSIONS["POINT:PICOMETER"] = \
        Mul(Rat(Int(9), Mul(Int(3175), Pow(10, 6))), Sym("pt"))  # nopep8
    CONVERSIONS["POINT:TERAMETER"] = \
        Mul(Rat(Mul(Int(36), Pow(10, 16)), Int(127)), Sym("pt"))  # nopep8
    CONVERSIONS["POINT:THOU"] = \
        Mul(Rat(Int(9), Int(125)), Sym("pt"))  # nopep8
    CONVERSIONS["POINT:YARD"] = \
        Mul(Int(2592), Sym("pt"))  # nopep8
    CONVERSIONS["POINT:YOCTOMETER"] = \
        Mul(Rat(Int(9), Mul(Int(3175), Pow(10, 18))), Sym("pt"))  # nopep8
    CONVERSIONS["POINT:YOTTAMETER"] = \
        Mul(Rat(Mul(Int(36), Pow(10, 28)), Int(127)), Sym("pt"))  # nopep8
    CONVERSIONS["POINT:ZEPTOMETER"] = \
        Mul(Rat(Int(9), Mul(Int(3175), Pow(10, 15))), Sym("pt"))  # nopep8
    CONVERSIONS["POINT:ZETTAMETER"] = \
        Mul(Rat(Mul(Int(36), Pow(10, 25)), Int(127)), Sym("pt"))  # nopep8
    CONVERSIONS["TERAMETER:ANGSTROM"] = \
        Mul(Rat(Int(1), Pow(10, 22)), Sym("teram"))  # nopep8
    CONVERSIONS["TERAMETER:ASTRONOMICALUNIT"] = \
        Mul(Rat(Int(1495978707), Pow(10, 10)), Sym("teram"))  # nopep8
    CONVERSIONS["TERAMETER:ATTOMETER"] = \
        Mul(Rat(Int(1), Pow(10, 30)), Sym("teram"))  # nopep8
    CONVERSIONS["TERAMETER:CENTIMETER"] = \
        Mul(Rat(Int(1), Pow(10, 14)), Sym("teram"))  # nopep8
    CONVERSIONS["TERAMETER:DECAMETER"] = \
        Mul(Rat(Int(1), Pow(10, 11)), Sym("teram"))  # nopep8
    CONVERSIONS["TERAMETER:DECIMETER"] = \
        Mul(Rat(Int(1), Pow(10, 13)), Sym("teram"))  # nopep8
    CONVERSIONS["TERAMETER:EXAMETER"] = \
        Mul(Pow(10, 6), Sym("teram"))  # nopep8
    CONVERSIONS["TERAMETER:FEMTOMETER"] = \
        Mul(Rat(Int(1), Pow(10, 27)), Sym("teram"))  # nopep8
    CONVERSIONS["TERAMETER:FOOT"] = \
        Mul(Rat(Int(381), Mul(Int(125), Pow(10, 13))), Sym("teram"))  # nopep8
    CONVERSIONS["TERAMETER:GIGAMETER"] = \
        Mul(Rat(Int(1), Int(1000)), Sym("teram"))  # nopep8
    CONVERSIONS["TERAMETER:HECTOMETER"] = \
        Mul(Rat(Int(1), Pow(10, 10)), Sym("teram"))  # nopep8
    CONVERSIONS["TERAMETER:INCH"] = \
        Mul(Rat(Int(127), Mul(Int(5), Pow(10, 15))), Sym("teram"))  # nopep8
    CONVERSIONS["TERAMETER:KILOMETER"] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("teram"))  # nopep8
    CONVERSIONS["TERAMETER:LIGHTYEAR"] = \
        Mul(Rat(Int("5912956545363"), Mul(Int(625), Pow(10, 6))), Sym("teram"))  # nopep8
    CONVERSIONS["TERAMETER:LINE"] = \
        Mul(Rat(Int(127), Mul(Int(6), Pow(10, 16))), Sym("teram"))  # nopep8
    CONVERSIONS["TERAMETER:MEGAMETER"] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("teram"))  # nopep8
    CONVERSIONS["TERAMETER:METER"] = \
        Mul(Rat(Int(1), Pow(10, 12)), Sym("teram"))  # nopep8
    CONVERSIONS["TERAMETER:MICROMETER"] = \
        Mul(Rat(Int(1), Pow(10, 18)), Sym("teram"))  # nopep8
    CONVERSIONS["TERAMETER:MILE"] = \
        Mul(Rat(Int(12573), Mul(Int(78125), Pow(10, 8))), Sym("teram"))  # nopep8
    CONVERSIONS["TERAMETER:MILLIMETER"] = \
        Mul(Rat(Int(1), Pow(10, 15)), Sym("teram"))  # nopep8
    CONVERSIONS["TERAMETER:NANOMETER"] = \
        Mul(Rat(Int(1), Pow(10, 21)), Sym("teram"))  # nopep8
    CONVERSIONS["TERAMETER:PARSEC"] = \
        Mul(Rat(Int(3857097), Int(125)), Sym("teram"))  # nopep8
    CONVERSIONS["TERAMETER:PETAMETER"] = \
        Mul(Int(1000), Sym("teram"))  # nopep8
    CONVERSIONS["TERAMETER:PICOMETER"] = \
        Mul(Rat(Int(1), Pow(10, 24)), Sym("teram"))  # nopep8
    CONVERSIONS["TERAMETER:POINT"] = \
        Mul(Rat(Int(127), Mul(Int(36), Pow(10, 16))), Sym("teram"))  # nopep8
    CONVERSIONS["TERAMETER:THOU"] = \
        Mul(Rat(Int(127), Mul(Int(5), Pow(10, 18))), Sym("teram"))  # nopep8
    CONVERSIONS["TERAMETER:YARD"] = \
        Mul(Rat(Int(1143), Mul(Int(125), Pow(10, 13))), Sym("teram"))  # nopep8
    CONVERSIONS["TERAMETER:YOCTOMETER"] = \
        Mul(Rat(Int(1), Pow(10, 36)), Sym("teram"))  # nopep8
    CONVERSIONS["TERAMETER:YOTTAMETER"] = \
        Mul(Pow(10, 12), Sym("teram"))  # nopep8
    CONVERSIONS["TERAMETER:ZEPTOMETER"] = \
        Mul(Rat(Int(1), Pow(10, 33)), Sym("teram"))  # nopep8
    CONVERSIONS["TERAMETER:ZETTAMETER"] = \
        Mul(Pow(10, 9), Sym("teram"))  # nopep8
    CONVERSIONS["THOU:ANGSTROM"] = \
        Mul(Rat(Int(1), Int(254000)), Sym("thou"))  # nopep8
    CONVERSIONS["THOU:ASTRONOMICALUNIT"] = \
        Mul(Rat(Mul(Int("7479893535"), Pow(10, 8)), Int(127)), Sym("thou"))  # nopep8
    CONVERSIONS["THOU:ATTOMETER"] = \
        Mul(Rat(Int(1), Mul(Int(254), Pow(10, 11))), Sym("thou"))  # nopep8
    CONVERSIONS["THOU:CENTIMETER"] = \
        Mul(Rat(Mul(Int(5), Pow(10, 4)), Int(127)), Sym("thou"))  # nopep8
    CONVERSIONS["THOU:DECAMETER"] = \
        Mul(Rat(Mul(Int(5), Pow(10, 7)), Int(127)), Sym("thou"))  # nopep8
    CONVERSIONS["THOU:DECIMETER"] = \
        Mul(Rat(Mul(Int(5), Pow(10, 5)), Int(127)), Sym("thou"))  # nopep8
    CONVERSIONS["THOU:EXAMETER"] = \
        Mul(Rat(Mul(Int(5), Pow(10, 24)), Int(127)), Sym("thou"))  # nopep8
    CONVERSIONS["THOU:FEMTOMETER"] = \
        Mul(Rat(Int(1), Mul(Int(254), Pow(10, 8))), Sym("thou"))  # nopep8
    CONVERSIONS["THOU:FOOT"] = \
        Mul(Int(12000), Sym("thou"))  # nopep8
    CONVERSIONS["THOU:GIGAMETER"] = \
        Mul(Rat(Mul(Int(5), Pow(10, 15)), Int(127)), Sym("thou"))  # nopep8
    CONVERSIONS["THOU:HECTOMETER"] = \
        Mul(Rat(Mul(Int(5), Pow(10, 8)), Int(127)), Sym("thou"))  # nopep8
    CONVERSIONS["THOU:INCH"] = \
        Mul(Int(1000), Sym("thou"))  # nopep8
    CONVERSIONS["THOU:KILOMETER"] = \
        Mul(Rat(Mul(Int(5), Pow(10, 9)), Int(127)), Sym("thou"))  # nopep8
    CONVERSIONS["THOU:LIGHTYEAR"] = \
        Mul(Rat(Mul(Int("47303652362904"), Pow(10, 9)), Int(127)), Sym("thou"))  # nopep8
    CONVERSIONS["THOU:LINE"] = \
        Mul(Rat(Int(250), Int(3)), Sym("thou"))  # nopep8
    CONVERSIONS["THOU:MEGAMETER"] = \
        Mul(Rat(Mul(Int(5), Pow(10, 12)), Int(127)), Sym("thou"))  # nopep8
    CONVERSIONS["THOU:METER"] = \
        Mul(Rat(Mul(Int(5), Pow(10, 6)), Int(127)), Sym("thou"))  # nopep8
    CONVERSIONS["THOU:MICROMETER"] = \
        Mul(Rat(Int(5), Int(127)), Sym("thou"))  # nopep8
    CONVERSIONS["THOU:MILE"] = \
        Mul(Mul(Int(6336), Pow(10, 4)), Sym("thou"))  # nopep8
    CONVERSIONS["THOU:MILLIMETER"] = \
        Mul(Rat(Int(5000), Int(127)), Sym("thou"))  # nopep8
    CONVERSIONS["THOU:NANOMETER"] = \
        Mul(Rat(Int(1), Int(25400)), Sym("thou"))  # nopep8
    CONVERSIONS["THOU:PARSEC"] = \
        Mul(Rat(Mul(Int(15428388), Pow(10, 16)), Int(127)), Sym("thou"))  # nopep8
    CONVERSIONS["THOU:PETAMETER"] = \
        Mul(Rat(Mul(Int(5), Pow(10, 21)), Int(127)), Sym("thou"))  # nopep8
    CONVERSIONS["THOU:PICOMETER"] = \
        Mul(Rat(Int(1), Mul(Int(254), Pow(10, 5))), Sym("thou"))  # nopep8
    CONVERSIONS["THOU:POINT"] = \
        Mul(Rat(Int(125), Int(9)), Sym("thou"))  # nopep8
    CONVERSIONS["THOU:TERAMETER"] = \
        Mul(Rat(Mul(Int(5), Pow(10, 18)), Int(127)), Sym("thou"))  # nopep8
    CONVERSIONS["THOU:YARD"] = \
        Mul(Int(36000), Sym("thou"))  # nopep8
    CONVERSIONS["THOU:YOCTOMETER"] = \
        Mul(Rat(Int(1), Mul(Int(254), Pow(10, 17))), Sym("thou"))  # nopep8
    CONVERSIONS["THOU:YOTTAMETER"] = \
        Mul(Rat(Mul(Int(5), Pow(10, 30)), Int(127)), Sym("thou"))  # nopep8
    CONVERSIONS["THOU:ZEPTOMETER"] = \
        Mul(Rat(Int(1), Mul(Int(254), Pow(10, 14))), Sym("thou"))  # nopep8
    CONVERSIONS["THOU:ZETTAMETER"] = \
        Mul(Rat(Mul(Int(5), Pow(10, 27)), Int(127)), Sym("thou"))  # nopep8
    CONVERSIONS["YARD:ANGSTROM"] = \
        Mul(Rat(Int(1), Mul(Int(9144), Pow(10, 6))), Sym("yd"))  # nopep8
    CONVERSIONS["YARD:ASTRONOMICALUNIT"] = \
        Mul(Rat(Int("62332446125000"), Int(381)), Sym("yd"))  # nopep8
    CONVERSIONS["YARD:ATTOMETER"] = \
        Mul(Rat(Int(1), Mul(Int(9144), Pow(10, 14))), Sym("yd"))  # nopep8
    CONVERSIONS["YARD:CENTIMETER"] = \
        Mul(Rat(Int(25), Int(2286)), Sym("yd"))  # nopep8
    CONVERSIONS["YARD:DECAMETER"] = \
        Mul(Rat(Int(12500), Int(1143)), Sym("yd"))  # nopep8
    CONVERSIONS["YARD:DECIMETER"] = \
        Mul(Rat(Int(125), Int(1143)), Sym("yd"))  # nopep8
    CONVERSIONS["YARD:EXAMETER"] = \
        Mul(Rat(Mul(Int(125), Pow(10, 19)), Int(1143)), Sym("yd"))  # nopep8
    CONVERSIONS["YARD:FEMTOMETER"] = \
        Mul(Rat(Int(1), Mul(Int(9144), Pow(10, 11))), Sym("yd"))  # nopep8
    CONVERSIONS["YARD:FOOT"] = \
        Mul(Rat(Int(1), Int(3)), Sym("yd"))  # nopep8
    CONVERSIONS["YARD:GIGAMETER"] = \
        Mul(Rat(Mul(Int(125), Pow(10, 10)), Int(1143)), Sym("yd"))  # nopep8
    CONVERSIONS["YARD:HECTOMETER"] = \
        Mul(Rat(Int(125000), Int(1143)), Sym("yd"))  # nopep8
    CONVERSIONS["YARD:INCH"] = \
        Mul(Rat(Int(1), Int(36)), Sym("yd"))  # nopep8
    CONVERSIONS["YARD:KILOMETER"] = \
        Mul(Rat(Mul(Int(125), Pow(10, 4)), Int(1143)), Sym("yd"))  # nopep8
    CONVERSIONS["YARD:LIGHTYEAR"] = \
        Mul(Rat(Mul(Int("1313990343414"), Pow(10, 6)), Int(127)), Sym("yd"))  # nopep8
    CONVERSIONS["YARD:LINE"] = \
        Mul(Rat(Int(1), Int(432)), Sym("yd"))  # nopep8
    CONVERSIONS["YARD:MEGAMETER"] = \
        Mul(Rat(Mul(Int(125), Pow(10, 7)), Int(1143)), Sym("yd"))  # nopep8
    CONVERSIONS["YARD:METER"] = \
        Mul(Rat(Int(1250), Int(1143)), Sym("yd"))  # nopep8
    CONVERSIONS["YARD:MICROMETER"] = \
        Mul(Rat(Int(1), Int(914400)), Sym("yd"))  # nopep8
    CONVERSIONS["YARD:MILE"] = \
        Mul(Int(1760), Sym("yd"))  # nopep8
    CONVERSIONS["YARD:MILLIMETER"] = \
        Mul(Rat(Int(5), Int(4572)), Sym("yd"))  # nopep8
    CONVERSIONS["YARD:NANOMETER"] = \
        Mul(Rat(Int(1), Mul(Int(9144), Pow(10, 5))), Sym("yd"))  # nopep8
    CONVERSIONS["YARD:PARSEC"] = \
        Mul(Rat(Mul(Int(1285699), Pow(10, 13)), Int(381)), Sym("yd"))  # nopep8
    CONVERSIONS["YARD:PETAMETER"] = \
        Mul(Rat(Mul(Int(125), Pow(10, 16)), Int(1143)), Sym("yd"))  # nopep8
    CONVERSIONS["YARD:PICOMETER"] = \
        Mul(Rat(Int(1), Mul(Int(9144), Pow(10, 8))), Sym("yd"))  # nopep8
    CONVERSIONS["YARD:POINT"] = \
        Mul(Rat(Int(1), Int(2592)), Sym("yd"))  # nopep8
    CONVERSIONS["YARD:TERAMETER"] = \
        Mul(Rat(Mul(Int(125), Pow(10, 13)), Int(1143)), Sym("yd"))  # nopep8
    CONVERSIONS["YARD:THOU"] = \
        Mul(Rat(Int(1), Int(36000)), Sym("yd"))  # nopep8
    CONVERSIONS["YARD:YOCTOMETER"] = \
        Mul(Rat(Int(1), Mul(Int(9144), Pow(10, 20))), Sym("yd"))  # nopep8
    CONVERSIONS["YARD:YOTTAMETER"] = \
        Mul(Rat(Mul(Int(125), Pow(10, 25)), Int(1143)), Sym("yd"))  # nopep8
    CONVERSIONS["YARD:ZEPTOMETER"] = \
        Mul(Rat(Int(1), Mul(Int(9144), Pow(10, 17))), Sym("yd"))  # nopep8
    CONVERSIONS["YARD:ZETTAMETER"] = \
        Mul(Rat(Mul(Int(125), Pow(10, 22)), Int(1143)), Sym("yd"))  # nopep8
    CONVERSIONS["YOCTOMETER:ANGSTROM"] = \
        Mul(Pow(10, 14), Sym("yoctom"))  # nopep8
    CONVERSIONS["YOCTOMETER:ASTRONOMICALUNIT"] = \
        Mul(Mul(Int(1495978707), Pow(10, 26)), Sym("yoctom"))  # nopep8
    CONVERSIONS["YOCTOMETER:ATTOMETER"] = \
        Mul(Pow(10, 6), Sym("yoctom"))  # nopep8
    CONVERSIONS["YOCTOMETER:CENTIMETER"] = \
        Mul(Pow(10, 22), Sym("yoctom"))  # nopep8
    CONVERSIONS["YOCTOMETER:DECAMETER"] = \
        Mul(Pow(10, 25), Sym("yoctom"))  # nopep8
    CONVERSIONS["YOCTOMETER:DECIMETER"] = \
        Mul(Pow(10, 23), Sym("yoctom"))  # nopep8
    CONVERSIONS["YOCTOMETER:EXAMETER"] = \
        Mul(Pow(10, 42), Sym("yoctom"))  # nopep8
    CONVERSIONS["YOCTOMETER:FEMTOMETER"] = \
        Mul(Pow(10, 9), Sym("yoctom"))  # nopep8
    CONVERSIONS["YOCTOMETER:FOOT"] = \
        Mul(Mul(Int(3048), Pow(10, 20)), Sym("yoctom"))  # nopep8
    CONVERSIONS["YOCTOMETER:GIGAMETER"] = \
        Mul(Pow(10, 33), Sym("yoctom"))  # nopep8
    CONVERSIONS["YOCTOMETER:HECTOMETER"] = \
        Mul(Pow(10, 26), Sym("yoctom"))  # nopep8
    CONVERSIONS["YOCTOMETER:INCH"] = \
        Mul(Mul(Int(254), Pow(10, 20)), Sym("yoctom"))  # nopep8
    CONVERSIONS["YOCTOMETER:KILOMETER"] = \
        Mul(Pow(10, 27), Sym("yoctom"))  # nopep8
    CONVERSIONS["YOCTOMETER:LIGHTYEAR"] = \
        Mul(Mul(Int("94607304725808"), Pow(10, 26)), Sym("yoctom"))  # nopep8
    CONVERSIONS["YOCTOMETER:LINE"] = \
        Mul(Rat(Mul(Int(635), Pow(10, 19)), Int(3)), Sym("yoctom"))  # nopep8
    CONVERSIONS["YOCTOMETER:MEGAMETER"] = \
        Mul(Pow(10, 30), Sym("yoctom"))  # nopep8
    CONVERSIONS["YOCTOMETER:METER"] = \
        Mul(Pow(10, 24), Sym("yoctom"))  # nopep8
    CONVERSIONS["YOCTOMETER:MICROMETER"] = \
        Mul(Pow(10, 18), Sym("yoctom"))  # nopep8
    CONVERSIONS["YOCTOMETER:MILE"] = \
        Mul(Mul(Int(1609344), Pow(10, 21)), Sym("yoctom"))  # nopep8
    CONVERSIONS["YOCTOMETER:MILLIMETER"] = \
        Mul(Pow(10, 21), Sym("yoctom"))  # nopep8
    CONVERSIONS["YOCTOMETER:NANOMETER"] = \
        Mul(Pow(10, 15), Sym("yoctom"))  # nopep8
    CONVERSIONS["YOCTOMETER:PARSEC"] = \
        Mul(Mul(Int(30856776), Pow(10, 33)), Sym("yoctom"))  # nopep8
    CONVERSIONS["YOCTOMETER:PETAMETER"] = \
        Mul(Pow(10, 39), Sym("yoctom"))  # nopep8
    CONVERSIONS["YOCTOMETER:PICOMETER"] = \
        Mul(Pow(10, 12), Sym("yoctom"))  # nopep8
    CONVERSIONS["YOCTOMETER:POINT"] = \
        Mul(Rat(Mul(Int(3175), Pow(10, 18)), Int(9)), Sym("yoctom"))  # nopep8
    CONVERSIONS["YOCTOMETER:TERAMETER"] = \
        Mul(Pow(10, 36), Sym("yoctom"))  # nopep8
    CONVERSIONS["YOCTOMETER:THOU"] = \
        Mul(Mul(Int(254), Pow(10, 17)), Sym("yoctom"))  # nopep8
    CONVERSIONS["YOCTOMETER:YARD"] = \
        Mul(Mul(Int(9144), Pow(10, 20)), Sym("yoctom"))  # nopep8
    CONVERSIONS["YOCTOMETER:YOTTAMETER"] = \
        Mul(Pow(10, 48), Sym("yoctom"))  # nopep8
    CONVERSIONS["YOCTOMETER:ZEPTOMETER"] = \
        Mul(Int(1000), Sym("yoctom"))  # nopep8
    CONVERSIONS["YOCTOMETER:ZETTAMETER"] = \
        Mul(Pow(10, 45), Sym("yoctom"))  # nopep8
    CONVERSIONS["YOTTAMETER:ANGSTROM"] = \
        Mul(Rat(Int(1), Pow(10, 34)), Sym("yottam"))  # nopep8
    CONVERSIONS["YOTTAMETER:ASTRONOMICALUNIT"] = \
        Mul(Rat(Int(1495978707), Pow(10, 22)), Sym("yottam"))  # nopep8
    CONVERSIONS["YOTTAMETER:ATTOMETER"] = \
        Mul(Rat(Int(1), Pow(10, 42)), Sym("yottam"))  # nopep8
    CONVERSIONS["YOTTAMETER:CENTIMETER"] = \
        Mul(Rat(Int(1), Pow(10, 26)), Sym("yottam"))  # nopep8
    CONVERSIONS["YOTTAMETER:DECAMETER"] = \
        Mul(Rat(Int(1), Pow(10, 23)), Sym("yottam"))  # nopep8
    CONVERSIONS["YOTTAMETER:DECIMETER"] = \
        Mul(Rat(Int(1), Pow(10, 25)), Sym("yottam"))  # nopep8
    CONVERSIONS["YOTTAMETER:EXAMETER"] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("yottam"))  # nopep8
    CONVERSIONS["YOTTAMETER:FEMTOMETER"] = \
        Mul(Rat(Int(1), Pow(10, 39)), Sym("yottam"))  # nopep8
    CONVERSIONS["YOTTAMETER:FOOT"] = \
        Mul(Rat(Int(381), Mul(Int(125), Pow(10, 25))), Sym("yottam"))  # nopep8
    CONVERSIONS["YOTTAMETER:GIGAMETER"] = \
        Mul(Rat(Int(1), Pow(10, 15)), Sym("yottam"))  # nopep8
    CONVERSIONS["YOTTAMETER:HECTOMETER"] = \
        Mul(Rat(Int(1), Pow(10, 22)), Sym("yottam"))  # nopep8
    CONVERSIONS["YOTTAMETER:INCH"] = \
        Mul(Rat(Int(127), Mul(Int(5), Pow(10, 27))), Sym("yottam"))  # nopep8
    CONVERSIONS["YOTTAMETER:KILOMETER"] = \
        Mul(Rat(Int(1), Pow(10, 21)), Sym("yottam"))  # nopep8
    CONVERSIONS["YOTTAMETER:LIGHTYEAR"] = \
        Mul(Rat(Int("5912956545363"), Mul(Int(625), Pow(10, 18))), Sym("yottam"))  # nopep8
    CONVERSIONS["YOTTAMETER:LINE"] = \
        Mul(Rat(Int(127), Mul(Int(6), Pow(10, 28))), Sym("yottam"))  # nopep8
    CONVERSIONS["YOTTAMETER:MEGAMETER"] = \
        Mul(Rat(Int(1), Pow(10, 18)), Sym("yottam"))  # nopep8
    CONVERSIONS["YOTTAMETER:METER"] = \
        Mul(Rat(Int(1), Pow(10, 24)), Sym("yottam"))  # nopep8
    CONVERSIONS["YOTTAMETER:MICROMETER"] = \
        Mul(Rat(Int(1), Pow(10, 30)), Sym("yottam"))  # nopep8
    CONVERSIONS["YOTTAMETER:MILE"] = \
        Mul(Rat(Int(12573), Mul(Int(78125), Pow(10, 20))), Sym("yottam"))  # nopep8
    CONVERSIONS["YOTTAMETER:MILLIMETER"] = \
        Mul(Rat(Int(1), Pow(10, 27)), Sym("yottam"))  # nopep8
    CONVERSIONS["YOTTAMETER:NANOMETER"] = \
        Mul(Rat(Int(1), Pow(10, 33)), Sym("yottam"))  # nopep8
    CONVERSIONS["YOTTAMETER:PARSEC"] = \
        Mul(Rat(Int(3857097), Mul(Int(125), Pow(10, 12))), Sym("yottam"))  # nopep8
    CONVERSIONS["YOTTAMETER:PETAMETER"] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("yottam"))  # nopep8
    CONVERSIONS["YOTTAMETER:PICOMETER"] = \
        Mul(Rat(Int(1), Pow(10, 36)), Sym("yottam"))  # nopep8
    CONVERSIONS["YOTTAMETER:POINT"] = \
        Mul(Rat(Int(127), Mul(Int(36), Pow(10, 28))), Sym("yottam"))  # nopep8
    CONVERSIONS["YOTTAMETER:TERAMETER"] = \
        Mul(Rat(Int(1), Pow(10, 12)), Sym("yottam"))  # nopep8
    CONVERSIONS["YOTTAMETER:THOU"] = \
        Mul(Rat(Int(127), Mul(Int(5), Pow(10, 30))), Sym("yottam"))  # nopep8
    CONVERSIONS["YOTTAMETER:YARD"] = \
        Mul(Rat(Int(1143), Mul(Int(125), Pow(10, 25))), Sym("yottam"))  # nopep8
    CONVERSIONS["YOTTAMETER:YOCTOMETER"] = \
        Mul(Rat(Int(1), Pow(10, 48)), Sym("yottam"))  # nopep8
    CONVERSIONS["YOTTAMETER:ZEPTOMETER"] = \
        Mul(Rat(Int(1), Pow(10, 45)), Sym("yottam"))  # nopep8
    CONVERSIONS["YOTTAMETER:ZETTAMETER"] = \
        Mul(Rat(Int(1), Int(1000)), Sym("yottam"))  # nopep8
    CONVERSIONS["ZEPTOMETER:ANGSTROM"] = \
        Mul(Pow(10, 11), Sym("zeptom"))  # nopep8
    CONVERSIONS["ZEPTOMETER:ASTRONOMICALUNIT"] = \
        Mul(Mul(Int(1495978707), Pow(10, 23)), Sym("zeptom"))  # nopep8
    CONVERSIONS["ZEPTOMETER:ATTOMETER"] = \
        Mul(Int(1000), Sym("zeptom"))  # nopep8
    CONVERSIONS["ZEPTOMETER:CENTIMETER"] = \
        Mul(Pow(10, 19), Sym("zeptom"))  # nopep8
    CONVERSIONS["ZEPTOMETER:DECAMETER"] = \
        Mul(Pow(10, 22), Sym("zeptom"))  # nopep8
    CONVERSIONS["ZEPTOMETER:DECIMETER"] = \
        Mul(Pow(10, 20), Sym("zeptom"))  # nopep8
    CONVERSIONS["ZEPTOMETER:EXAMETER"] = \
        Mul(Pow(10, 39), Sym("zeptom"))  # nopep8
    CONVERSIONS["ZEPTOMETER:FEMTOMETER"] = \
        Mul(Pow(10, 6), Sym("zeptom"))  # nopep8
    CONVERSIONS["ZEPTOMETER:FOOT"] = \
        Mul(Mul(Int(3048), Pow(10, 17)), Sym("zeptom"))  # nopep8
    CONVERSIONS["ZEPTOMETER:GIGAMETER"] = \
        Mul(Pow(10, 30), Sym("zeptom"))  # nopep8
    CONVERSIONS["ZEPTOMETER:HECTOMETER"] = \
        Mul(Pow(10, 23), Sym("zeptom"))  # nopep8
    CONVERSIONS["ZEPTOMETER:INCH"] = \
        Mul(Mul(Int(254), Pow(10, 17)), Sym("zeptom"))  # nopep8
    CONVERSIONS["ZEPTOMETER:KILOMETER"] = \
        Mul(Pow(10, 24), Sym("zeptom"))  # nopep8
    CONVERSIONS["ZEPTOMETER:LIGHTYEAR"] = \
        Mul(Mul(Int("94607304725808"), Pow(10, 23)), Sym("zeptom"))  # nopep8
    CONVERSIONS["ZEPTOMETER:LINE"] = \
        Mul(Rat(Mul(Int(635), Pow(10, 16)), Int(3)), Sym("zeptom"))  # nopep8
    CONVERSIONS["ZEPTOMETER:MEGAMETER"] = \
        Mul(Pow(10, 27), Sym("zeptom"))  # nopep8
    CONVERSIONS["ZEPTOMETER:METER"] = \
        Mul(Pow(10, 21), Sym("zeptom"))  # nopep8
    CONVERSIONS["ZEPTOMETER:MICROMETER"] = \
        Mul(Pow(10, 15), Sym("zeptom"))  # nopep8
    CONVERSIONS["ZEPTOMETER:MILE"] = \
        Mul(Mul(Int(1609344), Pow(10, 18)), Sym("zeptom"))  # nopep8
    CONVERSIONS["ZEPTOMETER:MILLIMETER"] = \
        Mul(Pow(10, 18), Sym("zeptom"))  # nopep8
    CONVERSIONS["ZEPTOMETER:NANOMETER"] = \
        Mul(Pow(10, 12), Sym("zeptom"))  # nopep8
    CONVERSIONS["ZEPTOMETER:PARSEC"] = \
        Mul(Mul(Int(30856776), Pow(10, 30)), Sym("zeptom"))  # nopep8
    CONVERSIONS["ZEPTOMETER:PETAMETER"] = \
        Mul(Pow(10, 36), Sym("zeptom"))  # nopep8
    CONVERSIONS["ZEPTOMETER:PICOMETER"] = \
        Mul(Pow(10, 9), Sym("zeptom"))  # nopep8
    CONVERSIONS["ZEPTOMETER:POINT"] = \
        Mul(Rat(Mul(Int(3175), Pow(10, 15)), Int(9)), Sym("zeptom"))  # nopep8
    CONVERSIONS["ZEPTOMETER:TERAMETER"] = \
        Mul(Pow(10, 33), Sym("zeptom"))  # nopep8
    CONVERSIONS["ZEPTOMETER:THOU"] = \
        Mul(Mul(Int(254), Pow(10, 14)), Sym("zeptom"))  # nopep8
    CONVERSIONS["ZEPTOMETER:YARD"] = \
        Mul(Mul(Int(9144), Pow(10, 17)), Sym("zeptom"))  # nopep8
    CONVERSIONS["ZEPTOMETER:YOCTOMETER"] = \
        Mul(Rat(Int(1), Int(1000)), Sym("zeptom"))  # nopep8
    CONVERSIONS["ZEPTOMETER:YOTTAMETER"] = \
        Mul(Pow(10, 45), Sym("zeptom"))  # nopep8
    CONVERSIONS["ZEPTOMETER:ZETTAMETER"] = \
        Mul(Pow(10, 42), Sym("zeptom"))  # nopep8
    CONVERSIONS["ZETTAMETER:ANGSTROM"] = \
        Mul(Rat(Int(1), Pow(10, 31)), Sym("zettam"))  # nopep8
    CONVERSIONS["ZETTAMETER:ASTRONOMICALUNIT"] = \
        Mul(Rat(Int(1495978707), Pow(10, 19)), Sym("zettam"))  # nopep8
    CONVERSIONS["ZETTAMETER:ATTOMETER"] = \
        Mul(Rat(Int(1), Pow(10, 39)), Sym("zettam"))  # nopep8
    CONVERSIONS["ZETTAMETER:CENTIMETER"] = \
        Mul(Rat(Int(1), Pow(10, 23)), Sym("zettam"))  # nopep8
    CONVERSIONS["ZETTAMETER:DECAMETER"] = \
        Mul(Rat(Int(1), Pow(10, 20)), Sym("zettam"))  # nopep8
    CONVERSIONS["ZETTAMETER:DECIMETER"] = \
        Mul(Rat(Int(1), Pow(10, 22)), Sym("zettam"))  # nopep8
    CONVERSIONS["ZETTAMETER:EXAMETER"] = \
        Mul(Rat(Int(1), Int(1000)), Sym("zettam"))  # nopep8
    CONVERSIONS["ZETTAMETER:FEMTOMETER"] = \
        Mul(Rat(Int(1), Pow(10, 36)), Sym("zettam"))  # nopep8
    CONVERSIONS["ZETTAMETER:FOOT"] = \
        Mul(Rat(Int(381), Mul(Int(125), Pow(10, 22))), Sym("zettam"))  # nopep8
    CONVERSIONS["ZETTAMETER:GIGAMETER"] = \
        Mul(Rat(Int(1), Pow(10, 12)), Sym("zettam"))  # nopep8
    CONVERSIONS["ZETTAMETER:HECTOMETER"] = \
        Mul(Rat(Int(1), Pow(10, 19)), Sym("zettam"))  # nopep8
    CONVERSIONS["ZETTAMETER:INCH"] = \
        Mul(Rat(Int(127), Mul(Int(5), Pow(10, 24))), Sym("zettam"))  # nopep8
    CONVERSIONS["ZETTAMETER:KILOMETER"] = \
        Mul(Rat(Int(1), Pow(10, 18)), Sym("zettam"))  # nopep8
    CONVERSIONS["ZETTAMETER:LIGHTYEAR"] = \
        Mul(Rat(Int("5912956545363"), Mul(Int(625), Pow(10, 15))), Sym("zettam"))  # nopep8
    CONVERSIONS["ZETTAMETER:LINE"] = \
        Mul(Rat(Int(127), Mul(Int(6), Pow(10, 25))), Sym("zettam"))  # nopep8
    CONVERSIONS["ZETTAMETER:MEGAMETER"] = \
        Mul(Rat(Int(1), Pow(10, 15)), Sym("zettam"))  # nopep8
    CONVERSIONS["ZETTAMETER:METER"] = \
        Mul(Rat(Int(1), Pow(10, 21)), Sym("zettam"))  # nopep8
    CONVERSIONS["ZETTAMETER:MICROMETER"] = \
        Mul(Rat(Int(1), Pow(10, 27)), Sym("zettam"))  # nopep8
    CONVERSIONS["ZETTAMETER:MILE"] = \
        Mul(Rat(Int(12573), Mul(Int(78125), Pow(10, 17))), Sym("zettam"))  # nopep8
    CONVERSIONS["ZETTAMETER:MILLIMETER"] = \
        Mul(Rat(Int(1), Pow(10, 24)), Sym("zettam"))  # nopep8
    CONVERSIONS["ZETTAMETER:NANOMETER"] = \
        Mul(Rat(Int(1), Pow(10, 30)), Sym("zettam"))  # nopep8
    CONVERSIONS["ZETTAMETER:PARSEC"] = \
        Mul(Rat(Int(3857097), Mul(Int(125), Pow(10, 9))), Sym("zettam"))  # nopep8
    CONVERSIONS["ZETTAMETER:PETAMETER"] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("zettam"))  # nopep8
    CONVERSIONS["ZETTAMETER:PICOMETER"] = \
        Mul(Rat(Int(1), Pow(10, 33)), Sym("zettam"))  # nopep8
    CONVERSIONS["ZETTAMETER:POINT"] = \
        Mul(Rat(Int(127), Mul(Int(36), Pow(10, 25))), Sym("zettam"))  # nopep8
    CONVERSIONS["ZETTAMETER:TERAMETER"] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("zettam"))  # nopep8
    CONVERSIONS["ZETTAMETER:THOU"] = \
        Mul(Rat(Int(127), Mul(Int(5), Pow(10, 27))), Sym("zettam"))  # nopep8
    CONVERSIONS["ZETTAMETER:YARD"] = \
        Mul(Rat(Int(1143), Mul(Int(125), Pow(10, 22))), Sym("zettam"))  # nopep8
    CONVERSIONS["ZETTAMETER:YOCTOMETER"] = \
        Mul(Rat(Int(1), Pow(10, 45)), Sym("zettam"))  # nopep8
    CONVERSIONS["ZETTAMETER:YOTTAMETER"] = \
        Mul(Int(1000), Sym("zettam"))  # nopep8
    CONVERSIONS["ZETTAMETER:ZEPTOMETER"] = \
        Mul(Rat(Int(1), Pow(10, 42)), Sym("zettam"))  # nopep8

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
