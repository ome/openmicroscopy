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

from omero.conversions import Add  # nopep8
from omero.conversions import Int  # nopep8
from omero.conversions import Mul  # nopep8
from omero.conversions import Pow  # nopep8
from omero.conversions import Rat  # nopep8
from omero.conversions import Sym  # nopep8


class LengthI(_omero_model.Length, UnitBase):

    try:
        UNIT_VALUES = sorted(UnitsLength._enumerators.values())
    except:
        # TODO: this occurs on Ice 3.4 and can be removed
        # once it has been dropped.
        UNIT_VALUES = [x for x in sorted(UnitsLength._names)]
        UNIT_VALUES = [getattr(UnitsLength, x) for x in UNIT_VALUES]
    CONVERSIONS = dict()
    for val in UNIT_VALUES:
        CONVERSIONS[val] = dict()
    CONVERSIONS[UnitsLength.ANGSTROM][UnitsLength.ASTRONOMICALUNIT] = \
        Mul(Rat(Int(1), Mul(Int(1495978707), Pow(10, 12))), Sym("ang"))  # nopep8
    CONVERSIONS[UnitsLength.ANGSTROM][UnitsLength.ATTOMETER] = \
        Mul(Pow(10, 8), Sym("ang"))  # nopep8
    CONVERSIONS[UnitsLength.ANGSTROM][UnitsLength.CENTIMETER] = \
        Mul(Rat(Int(1), Pow(10, 8)), Sym("ang"))  # nopep8
    CONVERSIONS[UnitsLength.ANGSTROM][UnitsLength.DECAMETER] = \
        Mul(Rat(Int(1), Pow(10, 11)), Sym("ang"))  # nopep8
    CONVERSIONS[UnitsLength.ANGSTROM][UnitsLength.DECIMETER] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("ang"))  # nopep8
    CONVERSIONS[UnitsLength.ANGSTROM][UnitsLength.EXAMETER] = \
        Mul(Rat(Int(1), Pow(10, 28)), Sym("ang"))  # nopep8
    CONVERSIONS[UnitsLength.ANGSTROM][UnitsLength.FEMTOMETER] = \
        Mul(Pow(10, 5), Sym("ang"))  # nopep8
    CONVERSIONS[UnitsLength.ANGSTROM][UnitsLength.FOOT] = \
        Mul(Rat(Int(393701), Mul(Int(12), Pow(10, 14))), Sym("ang"))  # nopep8
    CONVERSIONS[UnitsLength.ANGSTROM][UnitsLength.GIGAMETER] = \
        Mul(Rat(Int(1), Pow(10, 19)), Sym("ang"))  # nopep8
    CONVERSIONS[UnitsLength.ANGSTROM][UnitsLength.HECTOMETER] = \
        Mul(Rat(Int(1), Pow(10, 12)), Sym("ang"))  # nopep8
    CONVERSIONS[UnitsLength.ANGSTROM][UnitsLength.INCH] = \
        Mul(Rat(Int(393701), Pow(10, 14)), Sym("ang"))  # nopep8
    CONVERSIONS[UnitsLength.ANGSTROM][UnitsLength.KILOMETER] = \
        Mul(Rat(Int(1), Pow(10, 13)), Sym("ang"))  # nopep8
    CONVERSIONS[UnitsLength.ANGSTROM][UnitsLength.LIGHTYEAR] = \
        Mul(Rat(Int(1), Mul(Int("94607304725808"), Pow(10, 12))), Sym("ang"))  # nopep8
    CONVERSIONS[UnitsLength.ANGSTROM][UnitsLength.LINE] = \
        Mul(Rat(Int(1181103), Mul(Int(25), Pow(10, 12))), Sym("ang"))  # nopep8
    CONVERSIONS[UnitsLength.ANGSTROM][UnitsLength.MEGAMETER] = \
        Mul(Rat(Int(1), Pow(10, 16)), Sym("ang"))  # nopep8
    CONVERSIONS[UnitsLength.ANGSTROM][UnitsLength.METER] = \
        Mul(Rat(Int(1), Pow(10, 10)), Sym("ang"))  # nopep8
    CONVERSIONS[UnitsLength.ANGSTROM][UnitsLength.MICROMETER] = \
        Mul(Rat(Int(1), Pow(10, 4)), Sym("ang"))  # nopep8
    CONVERSIONS[UnitsLength.ANGSTROM][UnitsLength.MILE] = \
        Mul(Rat(Int(35791), Mul(Int(576), Pow(10, 15))), Sym("ang"))  # nopep8
    CONVERSIONS[UnitsLength.ANGSTROM][UnitsLength.MILLIMETER] = \
        Mul(Rat(Int(1), Pow(10, 7)), Sym("ang"))  # nopep8
    CONVERSIONS[UnitsLength.ANGSTROM][UnitsLength.NANOMETER] = \
        Mul(Rat(Int(1), Int(10)), Sym("ang"))  # nopep8
    CONVERSIONS[UnitsLength.ANGSTROM][UnitsLength.PARSEC] = \
        Mul(Rat(Int(1), Mul(Int(30856776), Pow(10, 19))), Sym("ang"))  # nopep8
    CONVERSIONS[UnitsLength.ANGSTROM][UnitsLength.PETAMETER] = \
        Mul(Rat(Int(1), Pow(10, 25)), Sym("ang"))  # nopep8
    CONVERSIONS[UnitsLength.ANGSTROM][UnitsLength.PICOMETER] = \
        Mul(Int(100), Sym("ang"))  # nopep8
    CONVERSIONS[UnitsLength.ANGSTROM][UnitsLength.POINT] = \
        Mul(Rat(Int(3543309), Mul(Int(125), Pow(10, 11))), Sym("ang"))  # nopep8
    CONVERSIONS[UnitsLength.ANGSTROM][UnitsLength.TERAMETER] = \
        Mul(Rat(Int(1), Pow(10, 22)), Sym("ang"))  # nopep8
    CONVERSIONS[UnitsLength.ANGSTROM][UnitsLength.THOU] = \
        Mul(Rat(Int(393701), Pow(10, 17)), Sym("ang"))  # nopep8
    CONVERSIONS[UnitsLength.ANGSTROM][UnitsLength.YARD] = \
        Mul(Rat(Int(393701), Mul(Int(36), Pow(10, 14))), Sym("ang"))  # nopep8
    CONVERSIONS[UnitsLength.ANGSTROM][UnitsLength.YOCTOMETER] = \
        Mul(Pow(10, 14), Sym("ang"))  # nopep8
    CONVERSIONS[UnitsLength.ANGSTROM][UnitsLength.YOTTAMETER] = \
        Mul(Rat(Int(1), Pow(10, 34)), Sym("ang"))  # nopep8
    CONVERSIONS[UnitsLength.ANGSTROM][UnitsLength.ZEPTOMETER] = \
        Mul(Pow(10, 11), Sym("ang"))  # nopep8
    CONVERSIONS[UnitsLength.ANGSTROM][UnitsLength.ZETTAMETER] = \
        Mul(Rat(Int(1), Pow(10, 31)), Sym("ang"))  # nopep8
    CONVERSIONS[UnitsLength.ASTRONOMICALUNIT][UnitsLength.ANGSTROM] = \
        Mul(Mul(Int(1495978707), Pow(10, 12)), Sym("ua"))  # nopep8
    CONVERSIONS[UnitsLength.ASTRONOMICALUNIT][UnitsLength.ATTOMETER] = \
        Mul(Mul(Int(1495978707), Pow(10, 20)), Sym("ua"))  # nopep8
    CONVERSIONS[UnitsLength.ASTRONOMICALUNIT][UnitsLength.CENTIMETER] = \
        Mul(Mul(Int(1495978707), Pow(10, 4)), Sym("ua"))  # nopep8
    CONVERSIONS[UnitsLength.ASTRONOMICALUNIT][UnitsLength.DECAMETER] = \
        Mul(Int("14959787070"), Sym("ua"))  # nopep8
    CONVERSIONS[UnitsLength.ASTRONOMICALUNIT][UnitsLength.DECIMETER] = \
        Mul(Int("1495978707000"), Sym("ua"))  # nopep8
    CONVERSIONS[UnitsLength.ASTRONOMICALUNIT][UnitsLength.EXAMETER] = \
        Mul(Rat(Int(1495978707), Pow(10, 16)), Sym("ua"))  # nopep8
    CONVERSIONS[UnitsLength.ASTRONOMICALUNIT][UnitsLength.FEMTOMETER] = \
        Mul(Mul(Int(1495978707), Pow(10, 17)), Sym("ua"))  # nopep8
    CONVERSIONS[UnitsLength.ASTRONOMICALUNIT][UnitsLength.FOOT] = \
        Mul(Rat(Int("196322770974869"), Int(400)), Sym("ua"))  # nopep8
    CONVERSIONS[UnitsLength.ASTRONOMICALUNIT][UnitsLength.GIGAMETER] = \
        Mul(Rat(Int(1495978707), Pow(10, 7)), Sym("ua"))  # nopep8
    CONVERSIONS[UnitsLength.ASTRONOMICALUNIT][UnitsLength.HECTOMETER] = \
        Mul(Int(1495978707), Sym("ua"))  # nopep8
    CONVERSIONS[UnitsLength.ASTRONOMICALUNIT][UnitsLength.INCH] = \
        Mul(Rat(Int("588968312924607"), Int(100)), Sym("ua"))  # nopep8
    CONVERSIONS[UnitsLength.ASTRONOMICALUNIT][UnitsLength.KILOMETER] = \
        Mul(Rat(Int(1495978707), Int(10)), Sym("ua"))  # nopep8
    CONVERSIONS[UnitsLength.ASTRONOMICALUNIT][UnitsLength.LIGHTYEAR] = \
        Mul(Rat(Int(6830953), Int("431996825232")), Sym("ua"))  # nopep8
    CONVERSIONS[UnitsLength.ASTRONOMICALUNIT][UnitsLength.LINE] = \
        Mul(Rat(Int("1766904938773821"), Int(25)), Sym("ua"))  # nopep8
    CONVERSIONS[UnitsLength.ASTRONOMICALUNIT][UnitsLength.MEGAMETER] = \
        Mul(Rat(Int(1495978707), Pow(10, 4)), Sym("ua"))  # nopep8
    CONVERSIONS[UnitsLength.ASTRONOMICALUNIT][UnitsLength.METER] = \
        Mul(Int("149597870700"), Sym("ua"))  # nopep8
    CONVERSIONS[UnitsLength.ASTRONOMICALUNIT][UnitsLength.MICROMETER] = \
        Mul(Mul(Int(1495978707), Pow(10, 8)), Sym("ua"))  # nopep8
    CONVERSIONS[UnitsLength.ASTRONOMICALUNIT][UnitsLength.MILE] = \
        Mul(Rat(Int("17847524634079"), Int(192000)), Sym("ua"))  # nopep8
    CONVERSIONS[UnitsLength.ASTRONOMICALUNIT][UnitsLength.MILLIMETER] = \
        Mul(Mul(Int(1495978707), Pow(10, 5)), Sym("ua"))  # nopep8
    CONVERSIONS[UnitsLength.ASTRONOMICALUNIT][UnitsLength.NANOMETER] = \
        Mul(Mul(Int(1495978707), Pow(10, 11)), Sym("ua"))  # nopep8
    CONVERSIONS[UnitsLength.ASTRONOMICALUNIT][UnitsLength.PARSEC] = \
        Mul(Rat(Int(498659569), Mul(Int(10285592), Pow(10, 7))), Sym("ua"))  # nopep8
    CONVERSIONS[UnitsLength.ASTRONOMICALUNIT][UnitsLength.PETAMETER] = \
        Mul(Rat(Int(1495978707), Pow(10, 13)), Sym("ua"))  # nopep8
    CONVERSIONS[UnitsLength.ASTRONOMICALUNIT][UnitsLength.PICOMETER] = \
        Mul(Mul(Int(1495978707), Pow(10, 14)), Sym("ua"))  # nopep8
    CONVERSIONS[UnitsLength.ASTRONOMICALUNIT][UnitsLength.POINT] = \
        Mul(Rat(Int("10601429632642926"), Int(25)), Sym("ua"))  # nopep8
    CONVERSIONS[UnitsLength.ASTRONOMICALUNIT][UnitsLength.TERAMETER] = \
        Mul(Rat(Int(1495978707), Pow(10, 10)), Sym("ua"))  # nopep8
    CONVERSIONS[UnitsLength.ASTRONOMICALUNIT][UnitsLength.THOU] = \
        Mul(Rat(Int("588968312924607"), Pow(10, 5)), Sym("ua"))  # nopep8
    CONVERSIONS[UnitsLength.ASTRONOMICALUNIT][UnitsLength.YARD] = \
        Mul(Rat(Int("196322770974869"), Int(1200)), Sym("ua"))  # nopep8
    CONVERSIONS[UnitsLength.ASTRONOMICALUNIT][UnitsLength.YOCTOMETER] = \
        Mul(Mul(Int(1495978707), Pow(10, 26)), Sym("ua"))  # nopep8
    CONVERSIONS[UnitsLength.ASTRONOMICALUNIT][UnitsLength.YOTTAMETER] = \
        Mul(Rat(Int(1495978707), Pow(10, 22)), Sym("ua"))  # nopep8
    CONVERSIONS[UnitsLength.ASTRONOMICALUNIT][UnitsLength.ZEPTOMETER] = \
        Mul(Mul(Int(1495978707), Pow(10, 23)), Sym("ua"))  # nopep8
    CONVERSIONS[UnitsLength.ASTRONOMICALUNIT][UnitsLength.ZETTAMETER] = \
        Mul(Rat(Int(1495978707), Pow(10, 19)), Sym("ua"))  # nopep8
    CONVERSIONS[UnitsLength.ATTOMETER][UnitsLength.ANGSTROM] = \
        Mul(Rat(Int(1), Pow(10, 8)), Sym("attom"))  # nopep8
    CONVERSIONS[UnitsLength.ATTOMETER][UnitsLength.ASTRONOMICALUNIT] = \
        Mul(Rat(Int(1), Mul(Int(1495978707), Pow(10, 20))), Sym("attom"))  # nopep8
    CONVERSIONS[UnitsLength.ATTOMETER][UnitsLength.CENTIMETER] = \
        Mul(Rat(Int(1), Pow(10, 16)), Sym("attom"))  # nopep8
    CONVERSIONS[UnitsLength.ATTOMETER][UnitsLength.DECAMETER] = \
        Mul(Rat(Int(1), Pow(10, 19)), Sym("attom"))  # nopep8
    CONVERSIONS[UnitsLength.ATTOMETER][UnitsLength.DECIMETER] = \
        Mul(Rat(Int(1), Pow(10, 17)), Sym("attom"))  # nopep8
    CONVERSIONS[UnitsLength.ATTOMETER][UnitsLength.EXAMETER] = \
        Mul(Rat(Int(1), Pow(10, 36)), Sym("attom"))  # nopep8
    CONVERSIONS[UnitsLength.ATTOMETER][UnitsLength.FEMTOMETER] = \
        Mul(Rat(Int(1), Int(1000)), Sym("attom"))  # nopep8
    CONVERSIONS[UnitsLength.ATTOMETER][UnitsLength.FOOT] = \
        Mul(Rat(Int(393701), Mul(Int(12), Pow(10, 22))), Sym("attom"))  # nopep8
    CONVERSIONS[UnitsLength.ATTOMETER][UnitsLength.GIGAMETER] = \
        Mul(Rat(Int(1), Pow(10, 27)), Sym("attom"))  # nopep8
    CONVERSIONS[UnitsLength.ATTOMETER][UnitsLength.HECTOMETER] = \
        Mul(Rat(Int(1), Pow(10, 20)), Sym("attom"))  # nopep8
    CONVERSIONS[UnitsLength.ATTOMETER][UnitsLength.INCH] = \
        Mul(Rat(Int(393701), Pow(10, 22)), Sym("attom"))  # nopep8
    CONVERSIONS[UnitsLength.ATTOMETER][UnitsLength.KILOMETER] = \
        Mul(Rat(Int(1), Pow(10, 21)), Sym("attom"))  # nopep8
    CONVERSIONS[UnitsLength.ATTOMETER][UnitsLength.LIGHTYEAR] = \
        Mul(Rat(Int(1), Mul(Int("94607304725808"), Pow(10, 20))), Sym("attom"))  # nopep8
    CONVERSIONS[UnitsLength.ATTOMETER][UnitsLength.LINE] = \
        Mul(Rat(Int(1181103), Mul(Int(25), Pow(10, 20))), Sym("attom"))  # nopep8
    CONVERSIONS[UnitsLength.ATTOMETER][UnitsLength.MEGAMETER] = \
        Mul(Rat(Int(1), Pow(10, 24)), Sym("attom"))  # nopep8
    CONVERSIONS[UnitsLength.ATTOMETER][UnitsLength.METER] = \
        Mul(Rat(Int(1), Pow(10, 18)), Sym("attom"))  # nopep8
    CONVERSIONS[UnitsLength.ATTOMETER][UnitsLength.MICROMETER] = \
        Mul(Rat(Int(1), Pow(10, 12)), Sym("attom"))  # nopep8
    CONVERSIONS[UnitsLength.ATTOMETER][UnitsLength.MILE] = \
        Mul(Rat(Int(35791), Mul(Int(576), Pow(10, 23))), Sym("attom"))  # nopep8
    CONVERSIONS[UnitsLength.ATTOMETER][UnitsLength.MILLIMETER] = \
        Mul(Rat(Int(1), Pow(10, 15)), Sym("attom"))  # nopep8
    CONVERSIONS[UnitsLength.ATTOMETER][UnitsLength.NANOMETER] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("attom"))  # nopep8
    CONVERSIONS[UnitsLength.ATTOMETER][UnitsLength.PARSEC] = \
        Mul(Rat(Int(1), Mul(Int(30856776), Pow(10, 27))), Sym("attom"))  # nopep8
    CONVERSIONS[UnitsLength.ATTOMETER][UnitsLength.PETAMETER] = \
        Mul(Rat(Int(1), Pow(10, 33)), Sym("attom"))  # nopep8
    CONVERSIONS[UnitsLength.ATTOMETER][UnitsLength.PICOMETER] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("attom"))  # nopep8
    CONVERSIONS[UnitsLength.ATTOMETER][UnitsLength.POINT] = \
        Mul(Rat(Int(3543309), Mul(Int(125), Pow(10, 19))), Sym("attom"))  # nopep8
    CONVERSIONS[UnitsLength.ATTOMETER][UnitsLength.TERAMETER] = \
        Mul(Rat(Int(1), Pow(10, 30)), Sym("attom"))  # nopep8
    CONVERSIONS[UnitsLength.ATTOMETER][UnitsLength.THOU] = \
        Mul(Rat(Int(393701), Pow(10, 25)), Sym("attom"))  # nopep8
    CONVERSIONS[UnitsLength.ATTOMETER][UnitsLength.YARD] = \
        Mul(Rat(Int(393701), Mul(Int(36), Pow(10, 22))), Sym("attom"))  # nopep8
    CONVERSIONS[UnitsLength.ATTOMETER][UnitsLength.YOCTOMETER] = \
        Mul(Pow(10, 6), Sym("attom"))  # nopep8
    CONVERSIONS[UnitsLength.ATTOMETER][UnitsLength.YOTTAMETER] = \
        Mul(Rat(Int(1), Pow(10, 42)), Sym("attom"))  # nopep8
    CONVERSIONS[UnitsLength.ATTOMETER][UnitsLength.ZEPTOMETER] = \
        Mul(Int(1000), Sym("attom"))  # nopep8
    CONVERSIONS[UnitsLength.ATTOMETER][UnitsLength.ZETTAMETER] = \
        Mul(Rat(Int(1), Pow(10, 39)), Sym("attom"))  # nopep8
    CONVERSIONS[UnitsLength.CENTIMETER][UnitsLength.ANGSTROM] = \
        Mul(Pow(10, 8), Sym("centim"))  # nopep8
    CONVERSIONS[UnitsLength.CENTIMETER][UnitsLength.ASTRONOMICALUNIT] = \
        Mul(Rat(Int(1), Mul(Int(1495978707), Pow(10, 4))), Sym("centim"))  # nopep8
    CONVERSIONS[UnitsLength.CENTIMETER][UnitsLength.ATTOMETER] = \
        Mul(Pow(10, 16), Sym("centim"))  # nopep8
    CONVERSIONS[UnitsLength.CENTIMETER][UnitsLength.DECAMETER] = \
        Mul(Rat(Int(1), Int(1000)), Sym("centim"))  # nopep8
    CONVERSIONS[UnitsLength.CENTIMETER][UnitsLength.DECIMETER] = \
        Mul(Rat(Int(1), Int(10)), Sym("centim"))  # nopep8
    CONVERSIONS[UnitsLength.CENTIMETER][UnitsLength.EXAMETER] = \
        Mul(Rat(Int(1), Pow(10, 20)), Sym("centim"))  # nopep8
    CONVERSIONS[UnitsLength.CENTIMETER][UnitsLength.FEMTOMETER] = \
        Mul(Pow(10, 13), Sym("centim"))  # nopep8
    CONVERSIONS[UnitsLength.CENTIMETER][UnitsLength.FOOT] = \
        Mul(Rat(Int(393701), Mul(Int(12), Pow(10, 6))), Sym("centim"))  # nopep8
    CONVERSIONS[UnitsLength.CENTIMETER][UnitsLength.GIGAMETER] = \
        Mul(Rat(Int(1), Pow(10, 11)), Sym("centim"))  # nopep8
    CONVERSIONS[UnitsLength.CENTIMETER][UnitsLength.HECTOMETER] = \
        Mul(Rat(Int(1), Pow(10, 4)), Sym("centim"))  # nopep8
    CONVERSIONS[UnitsLength.CENTIMETER][UnitsLength.INCH] = \
        Mul(Rat(Int(393701), Pow(10, 6)), Sym("centim"))  # nopep8
    CONVERSIONS[UnitsLength.CENTIMETER][UnitsLength.KILOMETER] = \
        Mul(Rat(Int(1), Pow(10, 5)), Sym("centim"))  # nopep8
    CONVERSIONS[UnitsLength.CENTIMETER][UnitsLength.LIGHTYEAR] = \
        Mul(Rat(Int(1), Mul(Int("94607304725808"), Pow(10, 4))), Sym("centim"))  # nopep8
    CONVERSIONS[UnitsLength.CENTIMETER][UnitsLength.LINE] = \
        Mul(Rat(Int(1181103), Mul(Int(25), Pow(10, 4))), Sym("centim"))  # nopep8
    CONVERSIONS[UnitsLength.CENTIMETER][UnitsLength.MEGAMETER] = \
        Mul(Rat(Int(1), Pow(10, 8)), Sym("centim"))  # nopep8
    CONVERSIONS[UnitsLength.CENTIMETER][UnitsLength.METER] = \
        Mul(Rat(Int(1), Int(100)), Sym("centim"))  # nopep8
    CONVERSIONS[UnitsLength.CENTIMETER][UnitsLength.MICROMETER] = \
        Mul(Pow(10, 4), Sym("centim"))  # nopep8
    CONVERSIONS[UnitsLength.CENTIMETER][UnitsLength.MILE] = \
        Mul(Rat(Int(35791), Mul(Int(576), Pow(10, 7))), Sym("centim"))  # nopep8
    CONVERSIONS[UnitsLength.CENTIMETER][UnitsLength.MILLIMETER] = \
        Mul(Int(10), Sym("centim"))  # nopep8
    CONVERSIONS[UnitsLength.CENTIMETER][UnitsLength.NANOMETER] = \
        Mul(Pow(10, 7), Sym("centim"))  # nopep8
    CONVERSIONS[UnitsLength.CENTIMETER][UnitsLength.PARSEC] = \
        Mul(Rat(Int(1), Mul(Int(30856776), Pow(10, 11))), Sym("centim"))  # nopep8
    CONVERSIONS[UnitsLength.CENTIMETER][UnitsLength.PETAMETER] = \
        Mul(Rat(Int(1), Pow(10, 17)), Sym("centim"))  # nopep8
    CONVERSIONS[UnitsLength.CENTIMETER][UnitsLength.PICOMETER] = \
        Mul(Pow(10, 10), Sym("centim"))  # nopep8
    CONVERSIONS[UnitsLength.CENTIMETER][UnitsLength.POINT] = \
        Mul(Rat(Int(3543309), Int(125000)), Sym("centim"))  # nopep8
    CONVERSIONS[UnitsLength.CENTIMETER][UnitsLength.TERAMETER] = \
        Mul(Rat(Int(1), Pow(10, 14)), Sym("centim"))  # nopep8
    CONVERSIONS[UnitsLength.CENTIMETER][UnitsLength.THOU] = \
        Mul(Rat(Int(393701), Pow(10, 9)), Sym("centim"))  # nopep8
    CONVERSIONS[UnitsLength.CENTIMETER][UnitsLength.YARD] = \
        Mul(Rat(Int(393701), Mul(Int(36), Pow(10, 6))), Sym("centim"))  # nopep8
    CONVERSIONS[UnitsLength.CENTIMETER][UnitsLength.YOCTOMETER] = \
        Mul(Pow(10, 22), Sym("centim"))  # nopep8
    CONVERSIONS[UnitsLength.CENTIMETER][UnitsLength.YOTTAMETER] = \
        Mul(Rat(Int(1), Pow(10, 26)), Sym("centim"))  # nopep8
    CONVERSIONS[UnitsLength.CENTIMETER][UnitsLength.ZEPTOMETER] = \
        Mul(Pow(10, 19), Sym("centim"))  # nopep8
    CONVERSIONS[UnitsLength.CENTIMETER][UnitsLength.ZETTAMETER] = \
        Mul(Rat(Int(1), Pow(10, 23)), Sym("centim"))  # nopep8
    CONVERSIONS[UnitsLength.DECAMETER][UnitsLength.ANGSTROM] = \
        Mul(Pow(10, 11), Sym("decam"))  # nopep8
    CONVERSIONS[UnitsLength.DECAMETER][UnitsLength.ASTRONOMICALUNIT] = \
        Mul(Rat(Int(1), Int("14959787070")), Sym("decam"))  # nopep8
    CONVERSIONS[UnitsLength.DECAMETER][UnitsLength.ATTOMETER] = \
        Mul(Pow(10, 19), Sym("decam"))  # nopep8
    CONVERSIONS[UnitsLength.DECAMETER][UnitsLength.CENTIMETER] = \
        Mul(Int(1000), Sym("decam"))  # nopep8
    CONVERSIONS[UnitsLength.DECAMETER][UnitsLength.DECIMETER] = \
        Mul(Int(100), Sym("decam"))  # nopep8
    CONVERSIONS[UnitsLength.DECAMETER][UnitsLength.EXAMETER] = \
        Mul(Rat(Int(1), Pow(10, 17)), Sym("decam"))  # nopep8
    CONVERSIONS[UnitsLength.DECAMETER][UnitsLength.FEMTOMETER] = \
        Mul(Pow(10, 16), Sym("decam"))  # nopep8
    CONVERSIONS[UnitsLength.DECAMETER][UnitsLength.FOOT] = \
        Mul(Rat(Int(393701), Int(12000)), Sym("decam"))  # nopep8
    CONVERSIONS[UnitsLength.DECAMETER][UnitsLength.GIGAMETER] = \
        Mul(Rat(Int(1), Pow(10, 8)), Sym("decam"))  # nopep8
    CONVERSIONS[UnitsLength.DECAMETER][UnitsLength.HECTOMETER] = \
        Mul(Rat(Int(1), Int(10)), Sym("decam"))  # nopep8
    CONVERSIONS[UnitsLength.DECAMETER][UnitsLength.INCH] = \
        Mul(Rat(Int(393701), Int(1000)), Sym("decam"))  # nopep8
    CONVERSIONS[UnitsLength.DECAMETER][UnitsLength.KILOMETER] = \
        Mul(Rat(Int(1), Int(100)), Sym("decam"))  # nopep8
    CONVERSIONS[UnitsLength.DECAMETER][UnitsLength.LIGHTYEAR] = \
        Mul(Rat(Int(1), Int("946073047258080")), Sym("decam"))  # nopep8
    CONVERSIONS[UnitsLength.DECAMETER][UnitsLength.LINE] = \
        Mul(Rat(Int(1181103), Int(250)), Sym("decam"))  # nopep8
    CONVERSIONS[UnitsLength.DECAMETER][UnitsLength.MEGAMETER] = \
        Mul(Rat(Int(1), Pow(10, 5)), Sym("decam"))  # nopep8
    CONVERSIONS[UnitsLength.DECAMETER][UnitsLength.METER] = \
        Mul(Int(10), Sym("decam"))  # nopep8
    CONVERSIONS[UnitsLength.DECAMETER][UnitsLength.MICROMETER] = \
        Mul(Pow(10, 7), Sym("decam"))  # nopep8
    CONVERSIONS[UnitsLength.DECAMETER][UnitsLength.MILE] = \
        Mul(Rat(Int(35791), Mul(Int(576), Pow(10, 4))), Sym("decam"))  # nopep8
    CONVERSIONS[UnitsLength.DECAMETER][UnitsLength.MILLIMETER] = \
        Mul(Pow(10, 4), Sym("decam"))  # nopep8
    CONVERSIONS[UnitsLength.DECAMETER][UnitsLength.NANOMETER] = \
        Mul(Pow(10, 10), Sym("decam"))  # nopep8
    CONVERSIONS[UnitsLength.DECAMETER][UnitsLength.PARSEC] = \
        Mul(Rat(Int(1), Mul(Int(30856776), Pow(10, 8))), Sym("decam"))  # nopep8
    CONVERSIONS[UnitsLength.DECAMETER][UnitsLength.PETAMETER] = \
        Mul(Rat(Int(1), Pow(10, 14)), Sym("decam"))  # nopep8
    CONVERSIONS[UnitsLength.DECAMETER][UnitsLength.PICOMETER] = \
        Mul(Pow(10, 13), Sym("decam"))  # nopep8
    CONVERSIONS[UnitsLength.DECAMETER][UnitsLength.POINT] = \
        Mul(Rat(Int(3543309), Int(125)), Sym("decam"))  # nopep8
    CONVERSIONS[UnitsLength.DECAMETER][UnitsLength.TERAMETER] = \
        Mul(Rat(Int(1), Pow(10, 11)), Sym("decam"))  # nopep8
    CONVERSIONS[UnitsLength.DECAMETER][UnitsLength.THOU] = \
        Mul(Rat(Int(393701), Pow(10, 6)), Sym("decam"))  # nopep8
    CONVERSIONS[UnitsLength.DECAMETER][UnitsLength.YARD] = \
        Mul(Rat(Int(393701), Int(36000)), Sym("decam"))  # nopep8
    CONVERSIONS[UnitsLength.DECAMETER][UnitsLength.YOCTOMETER] = \
        Mul(Pow(10, 25), Sym("decam"))  # nopep8
    CONVERSIONS[UnitsLength.DECAMETER][UnitsLength.YOTTAMETER] = \
        Mul(Rat(Int(1), Pow(10, 23)), Sym("decam"))  # nopep8
    CONVERSIONS[UnitsLength.DECAMETER][UnitsLength.ZEPTOMETER] = \
        Mul(Pow(10, 22), Sym("decam"))  # nopep8
    CONVERSIONS[UnitsLength.DECAMETER][UnitsLength.ZETTAMETER] = \
        Mul(Rat(Int(1), Pow(10, 20)), Sym("decam"))  # nopep8
    CONVERSIONS[UnitsLength.DECIMETER][UnitsLength.ANGSTROM] = \
        Mul(Pow(10, 9), Sym("decim"))  # nopep8
    CONVERSIONS[UnitsLength.DECIMETER][UnitsLength.ASTRONOMICALUNIT] = \
        Mul(Rat(Int(1), Int("1495978707000")), Sym("decim"))  # nopep8
    CONVERSIONS[UnitsLength.DECIMETER][UnitsLength.ATTOMETER] = \
        Mul(Pow(10, 17), Sym("decim"))  # nopep8
    CONVERSIONS[UnitsLength.DECIMETER][UnitsLength.CENTIMETER] = \
        Mul(Int(10), Sym("decim"))  # nopep8
    CONVERSIONS[UnitsLength.DECIMETER][UnitsLength.DECAMETER] = \
        Mul(Rat(Int(1), Int(100)), Sym("decim"))  # nopep8
    CONVERSIONS[UnitsLength.DECIMETER][UnitsLength.EXAMETER] = \
        Mul(Rat(Int(1), Pow(10, 19)), Sym("decim"))  # nopep8
    CONVERSIONS[UnitsLength.DECIMETER][UnitsLength.FEMTOMETER] = \
        Mul(Pow(10, 14), Sym("decim"))  # nopep8
    CONVERSIONS[UnitsLength.DECIMETER][UnitsLength.FOOT] = \
        Mul(Rat(Int(393701), Mul(Int(12), Pow(10, 5))), Sym("decim"))  # nopep8
    CONVERSIONS[UnitsLength.DECIMETER][UnitsLength.GIGAMETER] = \
        Mul(Rat(Int(1), Pow(10, 10)), Sym("decim"))  # nopep8
    CONVERSIONS[UnitsLength.DECIMETER][UnitsLength.HECTOMETER] = \
        Mul(Rat(Int(1), Int(1000)), Sym("decim"))  # nopep8
    CONVERSIONS[UnitsLength.DECIMETER][UnitsLength.INCH] = \
        Mul(Rat(Int(393701), Pow(10, 5)), Sym("decim"))  # nopep8
    CONVERSIONS[UnitsLength.DECIMETER][UnitsLength.KILOMETER] = \
        Mul(Rat(Int(1), Pow(10, 4)), Sym("decim"))  # nopep8
    CONVERSIONS[UnitsLength.DECIMETER][UnitsLength.LIGHTYEAR] = \
        Mul(Rat(Int(1), Int("94607304725808000")), Sym("decim"))  # nopep8
    CONVERSIONS[UnitsLength.DECIMETER][UnitsLength.LINE] = \
        Mul(Rat(Int(1181103), Int(25000)), Sym("decim"))  # nopep8
    CONVERSIONS[UnitsLength.DECIMETER][UnitsLength.MEGAMETER] = \
        Mul(Rat(Int(1), Pow(10, 7)), Sym("decim"))  # nopep8
    CONVERSIONS[UnitsLength.DECIMETER][UnitsLength.METER] = \
        Mul(Rat(Int(1), Int(10)), Sym("decim"))  # nopep8
    CONVERSIONS[UnitsLength.DECIMETER][UnitsLength.MICROMETER] = \
        Mul(Pow(10, 5), Sym("decim"))  # nopep8
    CONVERSIONS[UnitsLength.DECIMETER][UnitsLength.MILE] = \
        Mul(Rat(Int(35791), Mul(Int(576), Pow(10, 6))), Sym("decim"))  # nopep8
    CONVERSIONS[UnitsLength.DECIMETER][UnitsLength.MILLIMETER] = \
        Mul(Int(100), Sym("decim"))  # nopep8
    CONVERSIONS[UnitsLength.DECIMETER][UnitsLength.NANOMETER] = \
        Mul(Pow(10, 8), Sym("decim"))  # nopep8
    CONVERSIONS[UnitsLength.DECIMETER][UnitsLength.PARSEC] = \
        Mul(Rat(Int(1), Mul(Int(30856776), Pow(10, 10))), Sym("decim"))  # nopep8
    CONVERSIONS[UnitsLength.DECIMETER][UnitsLength.PETAMETER] = \
        Mul(Rat(Int(1), Pow(10, 16)), Sym("decim"))  # nopep8
    CONVERSIONS[UnitsLength.DECIMETER][UnitsLength.PICOMETER] = \
        Mul(Pow(10, 11), Sym("decim"))  # nopep8
    CONVERSIONS[UnitsLength.DECIMETER][UnitsLength.POINT] = \
        Mul(Rat(Int(3543309), Int(12500)), Sym("decim"))  # nopep8
    CONVERSIONS[UnitsLength.DECIMETER][UnitsLength.TERAMETER] = \
        Mul(Rat(Int(1), Pow(10, 13)), Sym("decim"))  # nopep8
    CONVERSIONS[UnitsLength.DECIMETER][UnitsLength.THOU] = \
        Mul(Rat(Int(393701), Pow(10, 8)), Sym("decim"))  # nopep8
    CONVERSIONS[UnitsLength.DECIMETER][UnitsLength.YARD] = \
        Mul(Rat(Int(393701), Mul(Int(36), Pow(10, 5))), Sym("decim"))  # nopep8
    CONVERSIONS[UnitsLength.DECIMETER][UnitsLength.YOCTOMETER] = \
        Mul(Pow(10, 23), Sym("decim"))  # nopep8
    CONVERSIONS[UnitsLength.DECIMETER][UnitsLength.YOTTAMETER] = \
        Mul(Rat(Int(1), Pow(10, 25)), Sym("decim"))  # nopep8
    CONVERSIONS[UnitsLength.DECIMETER][UnitsLength.ZEPTOMETER] = \
        Mul(Pow(10, 20), Sym("decim"))  # nopep8
    CONVERSIONS[UnitsLength.DECIMETER][UnitsLength.ZETTAMETER] = \
        Mul(Rat(Int(1), Pow(10, 22)), Sym("decim"))  # nopep8
    CONVERSIONS[UnitsLength.EXAMETER][UnitsLength.ANGSTROM] = \
        Mul(Pow(10, 28), Sym("exam"))  # nopep8
    CONVERSIONS[UnitsLength.EXAMETER][UnitsLength.ASTRONOMICALUNIT] = \
        Mul(Rat(Pow(10, 16), Int(1495978707)), Sym("exam"))  # nopep8
    CONVERSIONS[UnitsLength.EXAMETER][UnitsLength.ATTOMETER] = \
        Mul(Pow(10, 36), Sym("exam"))  # nopep8
    CONVERSIONS[UnitsLength.EXAMETER][UnitsLength.CENTIMETER] = \
        Mul(Pow(10, 20), Sym("exam"))  # nopep8
    CONVERSIONS[UnitsLength.EXAMETER][UnitsLength.DECAMETER] = \
        Mul(Pow(10, 17), Sym("exam"))  # nopep8
    CONVERSIONS[UnitsLength.EXAMETER][UnitsLength.DECIMETER] = \
        Mul(Pow(10, 19), Sym("exam"))  # nopep8
    CONVERSIONS[UnitsLength.EXAMETER][UnitsLength.FEMTOMETER] = \
        Mul(Pow(10, 33), Sym("exam"))  # nopep8
    CONVERSIONS[UnitsLength.EXAMETER][UnitsLength.FOOT] = \
        Mul(Rat(Mul(Int(9842525), Pow(10, 12)), Int(3)), Sym("exam"))  # nopep8
    CONVERSIONS[UnitsLength.EXAMETER][UnitsLength.GIGAMETER] = \
        Mul(Pow(10, 9), Sym("exam"))  # nopep8
    CONVERSIONS[UnitsLength.EXAMETER][UnitsLength.HECTOMETER] = \
        Mul(Pow(10, 16), Sym("exam"))  # nopep8
    CONVERSIONS[UnitsLength.EXAMETER][UnitsLength.INCH] = \
        Mul(Mul(Int(393701), Pow(10, 14)), Sym("exam"))  # nopep8
    CONVERSIONS[UnitsLength.EXAMETER][UnitsLength.KILOMETER] = \
        Mul(Pow(10, 15), Sym("exam"))  # nopep8
    CONVERSIONS[UnitsLength.EXAMETER][UnitsLength.LIGHTYEAR] = \
        Mul(Rat(Mul(Int(625), Pow(10, 12)), Int("5912956545363")), Sym("exam"))  # nopep8
    CONVERSIONS[UnitsLength.EXAMETER][UnitsLength.LINE] = \
        Mul(Mul(Int(4724412), Pow(10, 14)), Sym("exam"))  # nopep8
    CONVERSIONS[UnitsLength.EXAMETER][UnitsLength.MEGAMETER] = \
        Mul(Pow(10, 12), Sym("exam"))  # nopep8
    CONVERSIONS[UnitsLength.EXAMETER][UnitsLength.METER] = \
        Mul(Pow(10, 18), Sym("exam"))  # nopep8
    CONVERSIONS[UnitsLength.EXAMETER][UnitsLength.MICROMETER] = \
        Mul(Pow(10, 24), Sym("exam"))  # nopep8
    CONVERSIONS[UnitsLength.EXAMETER][UnitsLength.MILE] = \
        Mul(Rat(Mul(Int(559234375), Pow(10, 7)), Int(9)), Sym("exam"))  # nopep8
    CONVERSIONS[UnitsLength.EXAMETER][UnitsLength.MILLIMETER] = \
        Mul(Pow(10, 21), Sym("exam"))  # nopep8
    CONVERSIONS[UnitsLength.EXAMETER][UnitsLength.NANOMETER] = \
        Mul(Pow(10, 27), Sym("exam"))  # nopep8
    CONVERSIONS[UnitsLength.EXAMETER][UnitsLength.PARSEC] = \
        Mul(Rat(Mul(Int(125), Pow(10, 6)), Int(3857097)), Sym("exam"))  # nopep8
    CONVERSIONS[UnitsLength.EXAMETER][UnitsLength.PETAMETER] = \
        Mul(Int(1000), Sym("exam"))  # nopep8
    CONVERSIONS[UnitsLength.EXAMETER][UnitsLength.PICOMETER] = \
        Mul(Pow(10, 30), Sym("exam"))  # nopep8
    CONVERSIONS[UnitsLength.EXAMETER][UnitsLength.POINT] = \
        Mul(Mul(Int(28346472), Pow(10, 14)), Sym("exam"))  # nopep8
    CONVERSIONS[UnitsLength.EXAMETER][UnitsLength.TERAMETER] = \
        Mul(Pow(10, 6), Sym("exam"))  # nopep8
    CONVERSIONS[UnitsLength.EXAMETER][UnitsLength.THOU] = \
        Mul(Mul(Int(393701), Pow(10, 11)), Sym("exam"))  # nopep8
    CONVERSIONS[UnitsLength.EXAMETER][UnitsLength.YARD] = \
        Mul(Rat(Mul(Int(9842525), Pow(10, 12)), Int(9)), Sym("exam"))  # nopep8
    CONVERSIONS[UnitsLength.EXAMETER][UnitsLength.YOCTOMETER] = \
        Mul(Pow(10, 42), Sym("exam"))  # nopep8
    CONVERSIONS[UnitsLength.EXAMETER][UnitsLength.YOTTAMETER] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("exam"))  # nopep8
    CONVERSIONS[UnitsLength.EXAMETER][UnitsLength.ZEPTOMETER] = \
        Mul(Pow(10, 39), Sym("exam"))  # nopep8
    CONVERSIONS[UnitsLength.EXAMETER][UnitsLength.ZETTAMETER] = \
        Mul(Rat(Int(1), Int(1000)), Sym("exam"))  # nopep8
    CONVERSIONS[UnitsLength.FEMTOMETER][UnitsLength.ANGSTROM] = \
        Mul(Rat(Int(1), Pow(10, 5)), Sym("femtom"))  # nopep8
    CONVERSIONS[UnitsLength.FEMTOMETER][UnitsLength.ASTRONOMICALUNIT] = \
        Mul(Rat(Int(1), Mul(Int(1495978707), Pow(10, 17))), Sym("femtom"))  # nopep8
    CONVERSIONS[UnitsLength.FEMTOMETER][UnitsLength.ATTOMETER] = \
        Mul(Int(1000), Sym("femtom"))  # nopep8
    CONVERSIONS[UnitsLength.FEMTOMETER][UnitsLength.CENTIMETER] = \
        Mul(Rat(Int(1), Pow(10, 13)), Sym("femtom"))  # nopep8
    CONVERSIONS[UnitsLength.FEMTOMETER][UnitsLength.DECAMETER] = \
        Mul(Rat(Int(1), Pow(10, 16)), Sym("femtom"))  # nopep8
    CONVERSIONS[UnitsLength.FEMTOMETER][UnitsLength.DECIMETER] = \
        Mul(Rat(Int(1), Pow(10, 14)), Sym("femtom"))  # nopep8
    CONVERSIONS[UnitsLength.FEMTOMETER][UnitsLength.EXAMETER] = \
        Mul(Rat(Int(1), Pow(10, 33)), Sym("femtom"))  # nopep8
    CONVERSIONS[UnitsLength.FEMTOMETER][UnitsLength.FOOT] = \
        Mul(Rat(Int(393701), Mul(Int(12), Pow(10, 19))), Sym("femtom"))  # nopep8
    CONVERSIONS[UnitsLength.FEMTOMETER][UnitsLength.GIGAMETER] = \
        Mul(Rat(Int(1), Pow(10, 24)), Sym("femtom"))  # nopep8
    CONVERSIONS[UnitsLength.FEMTOMETER][UnitsLength.HECTOMETER] = \
        Mul(Rat(Int(1), Pow(10, 17)), Sym("femtom"))  # nopep8
    CONVERSIONS[UnitsLength.FEMTOMETER][UnitsLength.INCH] = \
        Mul(Rat(Int(393701), Pow(10, 19)), Sym("femtom"))  # nopep8
    CONVERSIONS[UnitsLength.FEMTOMETER][UnitsLength.KILOMETER] = \
        Mul(Rat(Int(1), Pow(10, 18)), Sym("femtom"))  # nopep8
    CONVERSIONS[UnitsLength.FEMTOMETER][UnitsLength.LIGHTYEAR] = \
        Mul(Rat(Int(1), Mul(Int("94607304725808"), Pow(10, 17))), Sym("femtom"))  # nopep8
    CONVERSIONS[UnitsLength.FEMTOMETER][UnitsLength.LINE] = \
        Mul(Rat(Int(1181103), Mul(Int(25), Pow(10, 17))), Sym("femtom"))  # nopep8
    CONVERSIONS[UnitsLength.FEMTOMETER][UnitsLength.MEGAMETER] = \
        Mul(Rat(Int(1), Pow(10, 21)), Sym("femtom"))  # nopep8
    CONVERSIONS[UnitsLength.FEMTOMETER][UnitsLength.METER] = \
        Mul(Rat(Int(1), Pow(10, 15)), Sym("femtom"))  # nopep8
    CONVERSIONS[UnitsLength.FEMTOMETER][UnitsLength.MICROMETER] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("femtom"))  # nopep8
    CONVERSIONS[UnitsLength.FEMTOMETER][UnitsLength.MILE] = \
        Mul(Rat(Int(35791), Mul(Int(576), Pow(10, 20))), Sym("femtom"))  # nopep8
    CONVERSIONS[UnitsLength.FEMTOMETER][UnitsLength.MILLIMETER] = \
        Mul(Rat(Int(1), Pow(10, 12)), Sym("femtom"))  # nopep8
    CONVERSIONS[UnitsLength.FEMTOMETER][UnitsLength.NANOMETER] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("femtom"))  # nopep8
    CONVERSIONS[UnitsLength.FEMTOMETER][UnitsLength.PARSEC] = \
        Mul(Rat(Int(1), Mul(Int(30856776), Pow(10, 24))), Sym("femtom"))  # nopep8
    CONVERSIONS[UnitsLength.FEMTOMETER][UnitsLength.PETAMETER] = \
        Mul(Rat(Int(1), Pow(10, 30)), Sym("femtom"))  # nopep8
    CONVERSIONS[UnitsLength.FEMTOMETER][UnitsLength.PICOMETER] = \
        Mul(Rat(Int(1), Int(1000)), Sym("femtom"))  # nopep8
    CONVERSIONS[UnitsLength.FEMTOMETER][UnitsLength.POINT] = \
        Mul(Rat(Int(3543309), Mul(Int(125), Pow(10, 16))), Sym("femtom"))  # nopep8
    CONVERSIONS[UnitsLength.FEMTOMETER][UnitsLength.TERAMETER] = \
        Mul(Rat(Int(1), Pow(10, 27)), Sym("femtom"))  # nopep8
    CONVERSIONS[UnitsLength.FEMTOMETER][UnitsLength.THOU] = \
        Mul(Rat(Int(393701), Pow(10, 22)), Sym("femtom"))  # nopep8
    CONVERSIONS[UnitsLength.FEMTOMETER][UnitsLength.YARD] = \
        Mul(Rat(Int(393701), Mul(Int(36), Pow(10, 19))), Sym("femtom"))  # nopep8
    CONVERSIONS[UnitsLength.FEMTOMETER][UnitsLength.YOCTOMETER] = \
        Mul(Pow(10, 9), Sym("femtom"))  # nopep8
    CONVERSIONS[UnitsLength.FEMTOMETER][UnitsLength.YOTTAMETER] = \
        Mul(Rat(Int(1), Pow(10, 39)), Sym("femtom"))  # nopep8
    CONVERSIONS[UnitsLength.FEMTOMETER][UnitsLength.ZEPTOMETER] = \
        Mul(Pow(10, 6), Sym("femtom"))  # nopep8
    CONVERSIONS[UnitsLength.FEMTOMETER][UnitsLength.ZETTAMETER] = \
        Mul(Rat(Int(1), Pow(10, 36)), Sym("femtom"))  # nopep8
    CONVERSIONS[UnitsLength.FOOT][UnitsLength.ANGSTROM] = \
        Mul(Rat(Mul(Int(12), Pow(10, 14)), Int(393701)), Sym("ft"))  # nopep8
    CONVERSIONS[UnitsLength.FOOT][UnitsLength.ASTRONOMICALUNIT] = \
        Mul(Rat(Int(400), Int("196322770974869")), Sym("ft"))  # nopep8
    CONVERSIONS[UnitsLength.FOOT][UnitsLength.ATTOMETER] = \
        Mul(Rat(Mul(Int(12), Pow(10, 22)), Int(393701)), Sym("ft"))  # nopep8
    CONVERSIONS[UnitsLength.FOOT][UnitsLength.CENTIMETER] = \
        Mul(Rat(Mul(Int(12), Pow(10, 6)), Int(393701)), Sym("ft"))  # nopep8
    CONVERSIONS[UnitsLength.FOOT][UnitsLength.DECAMETER] = \
        Mul(Rat(Int(12000), Int(393701)), Sym("ft"))  # nopep8
    CONVERSIONS[UnitsLength.FOOT][UnitsLength.DECIMETER] = \
        Mul(Rat(Mul(Int(12), Pow(10, 5)), Int(393701)), Sym("ft"))  # nopep8
    CONVERSIONS[UnitsLength.FOOT][UnitsLength.EXAMETER] = \
        Mul(Rat(Int(3), Mul(Int(9842525), Pow(10, 12))), Sym("ft"))  # nopep8
    CONVERSIONS[UnitsLength.FOOT][UnitsLength.FEMTOMETER] = \
        Mul(Rat(Mul(Int(12), Pow(10, 19)), Int(393701)), Sym("ft"))  # nopep8
    CONVERSIONS[UnitsLength.FOOT][UnitsLength.GIGAMETER] = \
        Mul(Rat(Int(3), Int("9842525000")), Sym("ft"))  # nopep8
    CONVERSIONS[UnitsLength.FOOT][UnitsLength.HECTOMETER] = \
        Mul(Rat(Int(1200), Int(393701)), Sym("ft"))  # nopep8
    CONVERSIONS[UnitsLength.FOOT][UnitsLength.INCH] = \
        Mul(Int(12), Sym("ft"))  # nopep8
    CONVERSIONS[UnitsLength.FOOT][UnitsLength.KILOMETER] = \
        Mul(Rat(Int(120), Int(393701)), Sym("ft"))  # nopep8
    CONVERSIONS[UnitsLength.FOOT][UnitsLength.LIGHTYEAR] = \
        Mul(Rat(Int(25), Int("775978968288652821")), Sym("ft"))  # nopep8
    CONVERSIONS[UnitsLength.FOOT][UnitsLength.LINE] = \
        Mul(Int(144), Sym("ft"))  # nopep8
    CONVERSIONS[UnitsLength.FOOT][UnitsLength.MEGAMETER] = \
        Mul(Rat(Int(3), Int(9842525)), Sym("ft"))  # nopep8
    CONVERSIONS[UnitsLength.FOOT][UnitsLength.METER] = \
        Mul(Rat(Mul(Int(12), Pow(10, 4)), Int(393701)), Sym("ft"))  # nopep8
    CONVERSIONS[UnitsLength.FOOT][UnitsLength.MICROMETER] = \
        Mul(Rat(Mul(Int(12), Pow(10, 10)), Int(393701)), Sym("ft"))  # nopep8
    CONVERSIONS[UnitsLength.FOOT][UnitsLength.MILE] = \
        Mul(Rat(Int(1), Int(5280)), Sym("ft"))  # nopep8
    CONVERSIONS[UnitsLength.FOOT][UnitsLength.MILLIMETER] = \
        Mul(Rat(Mul(Int(12), Pow(10, 7)), Int(393701)), Sym("ft"))  # nopep8
    CONVERSIONS[UnitsLength.FOOT][UnitsLength.NANOMETER] = \
        Mul(Rat(Mul(Int(12), Pow(10, 13)), Int(393701)), Sym("ft"))  # nopep8
    CONVERSIONS[UnitsLength.FOOT][UnitsLength.PARSEC] = \
        Mul(Rat(Int(1), Mul(Int("1012361963998"), Pow(10, 5))), Sym("ft"))  # nopep8
    CONVERSIONS[UnitsLength.FOOT][UnitsLength.PETAMETER] = \
        Mul(Rat(Int(3), Mul(Int(9842525), Pow(10, 9))), Sym("ft"))  # nopep8
    CONVERSIONS[UnitsLength.FOOT][UnitsLength.PICOMETER] = \
        Mul(Rat(Mul(Int(12), Pow(10, 16)), Int(393701)), Sym("ft"))  # nopep8
    CONVERSIONS[UnitsLength.FOOT][UnitsLength.POINT] = \
        Mul(Int(864), Sym("ft"))  # nopep8
    CONVERSIONS[UnitsLength.FOOT][UnitsLength.TERAMETER] = \
        Mul(Rat(Int(3), Mul(Int(9842525), Pow(10, 6))), Sym("ft"))  # nopep8
    CONVERSIONS[UnitsLength.FOOT][UnitsLength.THOU] = \
        Mul(Rat(Int(3), Int(250)), Sym("ft"))  # nopep8
    CONVERSIONS[UnitsLength.FOOT][UnitsLength.YARD] = \
        Mul(Rat(Int(1), Int(3)), Sym("ft"))  # nopep8
    CONVERSIONS[UnitsLength.FOOT][UnitsLength.YOCTOMETER] = \
        Mul(Rat(Mul(Int(12), Pow(10, 28)), Int(393701)), Sym("ft"))  # nopep8
    CONVERSIONS[UnitsLength.FOOT][UnitsLength.YOTTAMETER] = \
        Mul(Rat(Int(3), Mul(Int(9842525), Pow(10, 18))), Sym("ft"))  # nopep8
    CONVERSIONS[UnitsLength.FOOT][UnitsLength.ZEPTOMETER] = \
        Mul(Rat(Mul(Int(12), Pow(10, 25)), Int(393701)), Sym("ft"))  # nopep8
    CONVERSIONS[UnitsLength.FOOT][UnitsLength.ZETTAMETER] = \
        Mul(Rat(Int(3), Mul(Int(9842525), Pow(10, 15))), Sym("ft"))  # nopep8
    CONVERSIONS[UnitsLength.GIGAMETER][UnitsLength.ANGSTROM] = \
        Mul(Pow(10, 19), Sym("gigam"))  # nopep8
    CONVERSIONS[UnitsLength.GIGAMETER][UnitsLength.ASTRONOMICALUNIT] = \
        Mul(Rat(Pow(10, 7), Int(1495978707)), Sym("gigam"))  # nopep8
    CONVERSIONS[UnitsLength.GIGAMETER][UnitsLength.ATTOMETER] = \
        Mul(Pow(10, 27), Sym("gigam"))  # nopep8
    CONVERSIONS[UnitsLength.GIGAMETER][UnitsLength.CENTIMETER] = \
        Mul(Pow(10, 11), Sym("gigam"))  # nopep8
    CONVERSIONS[UnitsLength.GIGAMETER][UnitsLength.DECAMETER] = \
        Mul(Pow(10, 8), Sym("gigam"))  # nopep8
    CONVERSIONS[UnitsLength.GIGAMETER][UnitsLength.DECIMETER] = \
        Mul(Pow(10, 10), Sym("gigam"))  # nopep8
    CONVERSIONS[UnitsLength.GIGAMETER][UnitsLength.EXAMETER] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("gigam"))  # nopep8
    CONVERSIONS[UnitsLength.GIGAMETER][UnitsLength.FEMTOMETER] = \
        Mul(Pow(10, 24), Sym("gigam"))  # nopep8
    CONVERSIONS[UnitsLength.GIGAMETER][UnitsLength.FOOT] = \
        Mul(Rat(Int("9842525000"), Int(3)), Sym("gigam"))  # nopep8
    CONVERSIONS[UnitsLength.GIGAMETER][UnitsLength.HECTOMETER] = \
        Mul(Pow(10, 7), Sym("gigam"))  # nopep8
    CONVERSIONS[UnitsLength.GIGAMETER][UnitsLength.INCH] = \
        Mul(Mul(Int(393701), Pow(10, 5)), Sym("gigam"))  # nopep8
    CONVERSIONS[UnitsLength.GIGAMETER][UnitsLength.KILOMETER] = \
        Mul(Pow(10, 6), Sym("gigam"))  # nopep8
    CONVERSIONS[UnitsLength.GIGAMETER][UnitsLength.LIGHTYEAR] = \
        Mul(Rat(Int(625000), Int("5912956545363")), Sym("gigam"))  # nopep8
    CONVERSIONS[UnitsLength.GIGAMETER][UnitsLength.LINE] = \
        Mul(Mul(Int(4724412), Pow(10, 5)), Sym("gigam"))  # nopep8
    CONVERSIONS[UnitsLength.GIGAMETER][UnitsLength.MEGAMETER] = \
        Mul(Int(1000), Sym("gigam"))  # nopep8
    CONVERSIONS[UnitsLength.GIGAMETER][UnitsLength.METER] = \
        Mul(Pow(10, 9), Sym("gigam"))  # nopep8
    CONVERSIONS[UnitsLength.GIGAMETER][UnitsLength.MICROMETER] = \
        Mul(Pow(10, 15), Sym("gigam"))  # nopep8
    CONVERSIONS[UnitsLength.GIGAMETER][UnitsLength.MILE] = \
        Mul(Rat(Int(22369375), Int(36)), Sym("gigam"))  # nopep8
    CONVERSIONS[UnitsLength.GIGAMETER][UnitsLength.MILLIMETER] = \
        Mul(Pow(10, 12), Sym("gigam"))  # nopep8
    CONVERSIONS[UnitsLength.GIGAMETER][UnitsLength.NANOMETER] = \
        Mul(Pow(10, 18), Sym("gigam"))  # nopep8
    CONVERSIONS[UnitsLength.GIGAMETER][UnitsLength.PARSEC] = \
        Mul(Rat(Int(1), Int(30856776)), Sym("gigam"))  # nopep8
    CONVERSIONS[UnitsLength.GIGAMETER][UnitsLength.PETAMETER] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("gigam"))  # nopep8
    CONVERSIONS[UnitsLength.GIGAMETER][UnitsLength.PICOMETER] = \
        Mul(Pow(10, 21), Sym("gigam"))  # nopep8
    CONVERSIONS[UnitsLength.GIGAMETER][UnitsLength.POINT] = \
        Mul(Mul(Int(28346472), Pow(10, 5)), Sym("gigam"))  # nopep8
    CONVERSIONS[UnitsLength.GIGAMETER][UnitsLength.TERAMETER] = \
        Mul(Rat(Int(1), Int(1000)), Sym("gigam"))  # nopep8
    CONVERSIONS[UnitsLength.GIGAMETER][UnitsLength.THOU] = \
        Mul(Int(39370100), Sym("gigam"))  # nopep8
    CONVERSIONS[UnitsLength.GIGAMETER][UnitsLength.YARD] = \
        Mul(Rat(Int("9842525000"), Int(9)), Sym("gigam"))  # nopep8
    CONVERSIONS[UnitsLength.GIGAMETER][UnitsLength.YOCTOMETER] = \
        Mul(Pow(10, 33), Sym("gigam"))  # nopep8
    CONVERSIONS[UnitsLength.GIGAMETER][UnitsLength.YOTTAMETER] = \
        Mul(Rat(Int(1), Pow(10, 15)), Sym("gigam"))  # nopep8
    CONVERSIONS[UnitsLength.GIGAMETER][UnitsLength.ZEPTOMETER] = \
        Mul(Pow(10, 30), Sym("gigam"))  # nopep8
    CONVERSIONS[UnitsLength.GIGAMETER][UnitsLength.ZETTAMETER] = \
        Mul(Rat(Int(1), Pow(10, 12)), Sym("gigam"))  # nopep8
    CONVERSIONS[UnitsLength.HECTOMETER][UnitsLength.ANGSTROM] = \
        Mul(Pow(10, 12), Sym("hectom"))  # nopep8
    CONVERSIONS[UnitsLength.HECTOMETER][UnitsLength.ASTRONOMICALUNIT] = \
        Mul(Rat(Int(1), Int(1495978707)), Sym("hectom"))  # nopep8
    CONVERSIONS[UnitsLength.HECTOMETER][UnitsLength.ATTOMETER] = \
        Mul(Pow(10, 20), Sym("hectom"))  # nopep8
    CONVERSIONS[UnitsLength.HECTOMETER][UnitsLength.CENTIMETER] = \
        Mul(Pow(10, 4), Sym("hectom"))  # nopep8
    CONVERSIONS[UnitsLength.HECTOMETER][UnitsLength.DECAMETER] = \
        Mul(Int(10), Sym("hectom"))  # nopep8
    CONVERSIONS[UnitsLength.HECTOMETER][UnitsLength.DECIMETER] = \
        Mul(Int(1000), Sym("hectom"))  # nopep8
    CONVERSIONS[UnitsLength.HECTOMETER][UnitsLength.EXAMETER] = \
        Mul(Rat(Int(1), Pow(10, 16)), Sym("hectom"))  # nopep8
    CONVERSIONS[UnitsLength.HECTOMETER][UnitsLength.FEMTOMETER] = \
        Mul(Pow(10, 17), Sym("hectom"))  # nopep8
    CONVERSIONS[UnitsLength.HECTOMETER][UnitsLength.FOOT] = \
        Mul(Rat(Int(393701), Int(1200)), Sym("hectom"))  # nopep8
    CONVERSIONS[UnitsLength.HECTOMETER][UnitsLength.GIGAMETER] = \
        Mul(Rat(Int(1), Pow(10, 7)), Sym("hectom"))  # nopep8
    CONVERSIONS[UnitsLength.HECTOMETER][UnitsLength.INCH] = \
        Mul(Rat(Int(393701), Int(100)), Sym("hectom"))  # nopep8
    CONVERSIONS[UnitsLength.HECTOMETER][UnitsLength.KILOMETER] = \
        Mul(Rat(Int(1), Int(10)), Sym("hectom"))  # nopep8
    CONVERSIONS[UnitsLength.HECTOMETER][UnitsLength.LIGHTYEAR] = \
        Mul(Rat(Int(1), Int("94607304725808")), Sym("hectom"))  # nopep8
    CONVERSIONS[UnitsLength.HECTOMETER][UnitsLength.LINE] = \
        Mul(Rat(Int(1181103), Int(25)), Sym("hectom"))  # nopep8
    CONVERSIONS[UnitsLength.HECTOMETER][UnitsLength.MEGAMETER] = \
        Mul(Rat(Int(1), Pow(10, 4)), Sym("hectom"))  # nopep8
    CONVERSIONS[UnitsLength.HECTOMETER][UnitsLength.METER] = \
        Mul(Int(100), Sym("hectom"))  # nopep8
    CONVERSIONS[UnitsLength.HECTOMETER][UnitsLength.MICROMETER] = \
        Mul(Pow(10, 8), Sym("hectom"))  # nopep8
    CONVERSIONS[UnitsLength.HECTOMETER][UnitsLength.MILE] = \
        Mul(Rat(Int(35791), Int(576000)), Sym("hectom"))  # nopep8
    CONVERSIONS[UnitsLength.HECTOMETER][UnitsLength.MILLIMETER] = \
        Mul(Pow(10, 5), Sym("hectom"))  # nopep8
    CONVERSIONS[UnitsLength.HECTOMETER][UnitsLength.NANOMETER] = \
        Mul(Pow(10, 11), Sym("hectom"))  # nopep8
    CONVERSIONS[UnitsLength.HECTOMETER][UnitsLength.PARSEC] = \
        Mul(Rat(Int(1), Mul(Int(30856776), Pow(10, 7))), Sym("hectom"))  # nopep8
    CONVERSIONS[UnitsLength.HECTOMETER][UnitsLength.PETAMETER] = \
        Mul(Rat(Int(1), Pow(10, 13)), Sym("hectom"))  # nopep8
    CONVERSIONS[UnitsLength.HECTOMETER][UnitsLength.PICOMETER] = \
        Mul(Pow(10, 14), Sym("hectom"))  # nopep8
    CONVERSIONS[UnitsLength.HECTOMETER][UnitsLength.POINT] = \
        Mul(Rat(Int(7086618), Int(25)), Sym("hectom"))  # nopep8
    CONVERSIONS[UnitsLength.HECTOMETER][UnitsLength.TERAMETER] = \
        Mul(Rat(Int(1), Pow(10, 10)), Sym("hectom"))  # nopep8
    CONVERSIONS[UnitsLength.HECTOMETER][UnitsLength.THOU] = \
        Mul(Rat(Int(393701), Pow(10, 5)), Sym("hectom"))  # nopep8
    CONVERSIONS[UnitsLength.HECTOMETER][UnitsLength.YARD] = \
        Mul(Rat(Int(393701), Int(3600)), Sym("hectom"))  # nopep8
    CONVERSIONS[UnitsLength.HECTOMETER][UnitsLength.YOCTOMETER] = \
        Mul(Pow(10, 26), Sym("hectom"))  # nopep8
    CONVERSIONS[UnitsLength.HECTOMETER][UnitsLength.YOTTAMETER] = \
        Mul(Rat(Int(1), Pow(10, 22)), Sym("hectom"))  # nopep8
    CONVERSIONS[UnitsLength.HECTOMETER][UnitsLength.ZEPTOMETER] = \
        Mul(Pow(10, 23), Sym("hectom"))  # nopep8
    CONVERSIONS[UnitsLength.HECTOMETER][UnitsLength.ZETTAMETER] = \
        Mul(Rat(Int(1), Pow(10, 19)), Sym("hectom"))  # nopep8
    CONVERSIONS[UnitsLength.INCH][UnitsLength.ANGSTROM] = \
        Mul(Rat(Pow(10, 14), Int(393701)), Sym("in"))  # nopep8
    CONVERSIONS[UnitsLength.INCH][UnitsLength.ASTRONOMICALUNIT] = \
        Mul(Rat(Int(100), Int("588968312924607")), Sym("in"))  # nopep8
    CONVERSIONS[UnitsLength.INCH][UnitsLength.ATTOMETER] = \
        Mul(Rat(Pow(10, 22), Int(393701)), Sym("in"))  # nopep8
    CONVERSIONS[UnitsLength.INCH][UnitsLength.CENTIMETER] = \
        Mul(Rat(Pow(10, 6), Int(393701)), Sym("in"))  # nopep8
    CONVERSIONS[UnitsLength.INCH][UnitsLength.DECAMETER] = \
        Mul(Rat(Int(1000), Int(393701)), Sym("in"))  # nopep8
    CONVERSIONS[UnitsLength.INCH][UnitsLength.DECIMETER] = \
        Mul(Rat(Pow(10, 5), Int(393701)), Sym("in"))  # nopep8
    CONVERSIONS[UnitsLength.INCH][UnitsLength.EXAMETER] = \
        Mul(Rat(Int(1), Mul(Int(393701), Pow(10, 14))), Sym("in"))  # nopep8
    CONVERSIONS[UnitsLength.INCH][UnitsLength.FEMTOMETER] = \
        Mul(Rat(Pow(10, 19), Int(393701)), Sym("in"))  # nopep8
    CONVERSIONS[UnitsLength.INCH][UnitsLength.FOOT] = \
        Mul(Rat(Int(1), Int(12)), Sym("in"))  # nopep8
    CONVERSIONS[UnitsLength.INCH][UnitsLength.GIGAMETER] = \
        Mul(Rat(Int(1), Mul(Int(393701), Pow(10, 5))), Sym("in"))  # nopep8
    CONVERSIONS[UnitsLength.INCH][UnitsLength.HECTOMETER] = \
        Mul(Rat(Int(100), Int(393701)), Sym("in"))  # nopep8
    CONVERSIONS[UnitsLength.INCH][UnitsLength.KILOMETER] = \
        Mul(Rat(Int(10), Int(393701)), Sym("in"))  # nopep8
    CONVERSIONS[UnitsLength.INCH][UnitsLength.LIGHTYEAR] = \
        Mul(Rat(Int(25), Int("9311747619463833852")), Sym("in"))  # nopep8
    CONVERSIONS[UnitsLength.INCH][UnitsLength.LINE] = \
        Mul(Int(12), Sym("in"))  # nopep8
    CONVERSIONS[UnitsLength.INCH][UnitsLength.MEGAMETER] = \
        Mul(Rat(Int(1), Int(39370100)), Sym("in"))  # nopep8
    CONVERSIONS[UnitsLength.INCH][UnitsLength.METER] = \
        Mul(Rat(Pow(10, 4), Int(393701)), Sym("in"))  # nopep8
    CONVERSIONS[UnitsLength.INCH][UnitsLength.MICROMETER] = \
        Mul(Rat(Pow(10, 10), Int(393701)), Sym("in"))  # nopep8
    CONVERSIONS[UnitsLength.INCH][UnitsLength.MILE] = \
        Mul(Rat(Int(1), Int(63360)), Sym("in"))  # nopep8
    CONVERSIONS[UnitsLength.INCH][UnitsLength.MILLIMETER] = \
        Mul(Rat(Pow(10, 7), Int(393701)), Sym("in"))  # nopep8
    CONVERSIONS[UnitsLength.INCH][UnitsLength.NANOMETER] = \
        Mul(Rat(Pow(10, 13), Int(393701)), Sym("in"))  # nopep8
    CONVERSIONS[UnitsLength.INCH][UnitsLength.PARSEC] = \
        Mul(Rat(Int(1), Mul(Int("12148343567976"), Pow(10, 5))), Sym("in"))  # nopep8
    CONVERSIONS[UnitsLength.INCH][UnitsLength.PETAMETER] = \
        Mul(Rat(Int(1), Mul(Int(393701), Pow(10, 11))), Sym("in"))  # nopep8
    CONVERSIONS[UnitsLength.INCH][UnitsLength.PICOMETER] = \
        Mul(Rat(Pow(10, 16), Int(393701)), Sym("in"))  # nopep8
    CONVERSIONS[UnitsLength.INCH][UnitsLength.POINT] = \
        Mul(Int(72), Sym("in"))  # nopep8
    CONVERSIONS[UnitsLength.INCH][UnitsLength.TERAMETER] = \
        Mul(Rat(Int(1), Mul(Int(393701), Pow(10, 8))), Sym("in"))  # nopep8
    CONVERSIONS[UnitsLength.INCH][UnitsLength.THOU] = \
        Mul(Rat(Int(1), Int(1000)), Sym("in"))  # nopep8
    CONVERSIONS[UnitsLength.INCH][UnitsLength.YARD] = \
        Mul(Rat(Int(1), Int(36)), Sym("in"))  # nopep8
    CONVERSIONS[UnitsLength.INCH][UnitsLength.YOCTOMETER] = \
        Mul(Rat(Pow(10, 28), Int(393701)), Sym("in"))  # nopep8
    CONVERSIONS[UnitsLength.INCH][UnitsLength.YOTTAMETER] = \
        Mul(Rat(Int(1), Mul(Int(393701), Pow(10, 20))), Sym("in"))  # nopep8
    CONVERSIONS[UnitsLength.INCH][UnitsLength.ZEPTOMETER] = \
        Mul(Rat(Pow(10, 25), Int(393701)), Sym("in"))  # nopep8
    CONVERSIONS[UnitsLength.INCH][UnitsLength.ZETTAMETER] = \
        Mul(Rat(Int(1), Mul(Int(393701), Pow(10, 17))), Sym("in"))  # nopep8
    CONVERSIONS[UnitsLength.KILOMETER][UnitsLength.ANGSTROM] = \
        Mul(Pow(10, 13), Sym("kilom"))  # nopep8
    CONVERSIONS[UnitsLength.KILOMETER][UnitsLength.ASTRONOMICALUNIT] = \
        Mul(Rat(Int(10), Int(1495978707)), Sym("kilom"))  # nopep8
    CONVERSIONS[UnitsLength.KILOMETER][UnitsLength.ATTOMETER] = \
        Mul(Pow(10, 21), Sym("kilom"))  # nopep8
    CONVERSIONS[UnitsLength.KILOMETER][UnitsLength.CENTIMETER] = \
        Mul(Pow(10, 5), Sym("kilom"))  # nopep8
    CONVERSIONS[UnitsLength.KILOMETER][UnitsLength.DECAMETER] = \
        Mul(Int(100), Sym("kilom"))  # nopep8
    CONVERSIONS[UnitsLength.KILOMETER][UnitsLength.DECIMETER] = \
        Mul(Pow(10, 4), Sym("kilom"))  # nopep8
    CONVERSIONS[UnitsLength.KILOMETER][UnitsLength.EXAMETER] = \
        Mul(Rat(Int(1), Pow(10, 15)), Sym("kilom"))  # nopep8
    CONVERSIONS[UnitsLength.KILOMETER][UnitsLength.FEMTOMETER] = \
        Mul(Pow(10, 18), Sym("kilom"))  # nopep8
    CONVERSIONS[UnitsLength.KILOMETER][UnitsLength.FOOT] = \
        Mul(Rat(Int(393701), Int(120)), Sym("kilom"))  # nopep8
    CONVERSIONS[UnitsLength.KILOMETER][UnitsLength.GIGAMETER] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("kilom"))  # nopep8
    CONVERSIONS[UnitsLength.KILOMETER][UnitsLength.HECTOMETER] = \
        Mul(Int(10), Sym("kilom"))  # nopep8
    CONVERSIONS[UnitsLength.KILOMETER][UnitsLength.INCH] = \
        Mul(Rat(Int(393701), Int(10)), Sym("kilom"))  # nopep8
    CONVERSIONS[UnitsLength.KILOMETER][UnitsLength.LIGHTYEAR] = \
        Mul(Rat(Int(5), Int("47303652362904")), Sym("kilom"))  # nopep8
    CONVERSIONS[UnitsLength.KILOMETER][UnitsLength.LINE] = \
        Mul(Rat(Int(2362206), Int(5)), Sym("kilom"))  # nopep8
    CONVERSIONS[UnitsLength.KILOMETER][UnitsLength.MEGAMETER] = \
        Mul(Rat(Int(1), Int(1000)), Sym("kilom"))  # nopep8
    CONVERSIONS[UnitsLength.KILOMETER][UnitsLength.METER] = \
        Mul(Int(1000), Sym("kilom"))  # nopep8
    CONVERSIONS[UnitsLength.KILOMETER][UnitsLength.MICROMETER] = \
        Mul(Pow(10, 9), Sym("kilom"))  # nopep8
    CONVERSIONS[UnitsLength.KILOMETER][UnitsLength.MILE] = \
        Mul(Rat(Int(35791), Int(57600)), Sym("kilom"))  # nopep8
    CONVERSIONS[UnitsLength.KILOMETER][UnitsLength.MILLIMETER] = \
        Mul(Pow(10, 6), Sym("kilom"))  # nopep8
    CONVERSIONS[UnitsLength.KILOMETER][UnitsLength.NANOMETER] = \
        Mul(Pow(10, 12), Sym("kilom"))  # nopep8
    CONVERSIONS[UnitsLength.KILOMETER][UnitsLength.PARSEC] = \
        Mul(Rat(Int(1), Mul(Int(30856776), Pow(10, 6))), Sym("kilom"))  # nopep8
    CONVERSIONS[UnitsLength.KILOMETER][UnitsLength.PETAMETER] = \
        Mul(Rat(Int(1), Pow(10, 12)), Sym("kilom"))  # nopep8
    CONVERSIONS[UnitsLength.KILOMETER][UnitsLength.PICOMETER] = \
        Mul(Pow(10, 15), Sym("kilom"))  # nopep8
    CONVERSIONS[UnitsLength.KILOMETER][UnitsLength.POINT] = \
        Mul(Rat(Int(14173236), Int(5)), Sym("kilom"))  # nopep8
    CONVERSIONS[UnitsLength.KILOMETER][UnitsLength.TERAMETER] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("kilom"))  # nopep8
    CONVERSIONS[UnitsLength.KILOMETER][UnitsLength.THOU] = \
        Mul(Rat(Int(393701), Pow(10, 4)), Sym("kilom"))  # nopep8
    CONVERSIONS[UnitsLength.KILOMETER][UnitsLength.YARD] = \
        Mul(Rat(Int(393701), Int(360)), Sym("kilom"))  # nopep8
    CONVERSIONS[UnitsLength.KILOMETER][UnitsLength.YOCTOMETER] = \
        Mul(Pow(10, 27), Sym("kilom"))  # nopep8
    CONVERSIONS[UnitsLength.KILOMETER][UnitsLength.YOTTAMETER] = \
        Mul(Rat(Int(1), Pow(10, 21)), Sym("kilom"))  # nopep8
    CONVERSIONS[UnitsLength.KILOMETER][UnitsLength.ZEPTOMETER] = \
        Mul(Pow(10, 24), Sym("kilom"))  # nopep8
    CONVERSIONS[UnitsLength.KILOMETER][UnitsLength.ZETTAMETER] = \
        Mul(Rat(Int(1), Pow(10, 18)), Sym("kilom"))  # nopep8
    CONVERSIONS[UnitsLength.LIGHTYEAR][UnitsLength.ANGSTROM] = \
        Mul(Mul(Int("94607304725808"), Pow(10, 12)), Sym("ly"))  # nopep8
    CONVERSIONS[UnitsLength.LIGHTYEAR][UnitsLength.ASTRONOMICALUNIT] = \
        Mul(Rat(Int("431996825232"), Int(6830953)), Sym("ly"))  # nopep8
    CONVERSIONS[UnitsLength.LIGHTYEAR][UnitsLength.ATTOMETER] = \
        Mul(Mul(Int("94607304725808"), Pow(10, 20)), Sym("ly"))  # nopep8
    CONVERSIONS[UnitsLength.LIGHTYEAR][UnitsLength.CENTIMETER] = \
        Mul(Mul(Int("94607304725808"), Pow(10, 4)), Sym("ly"))  # nopep8
    CONVERSIONS[UnitsLength.LIGHTYEAR][UnitsLength.DECAMETER] = \
        Mul(Int("946073047258080"), Sym("ly"))  # nopep8
    CONVERSIONS[UnitsLength.LIGHTYEAR][UnitsLength.DECIMETER] = \
        Mul(Int("94607304725808000"), Sym("ly"))  # nopep8
    CONVERSIONS[UnitsLength.LIGHTYEAR][UnitsLength.EXAMETER] = \
        Mul(Rat(Int("5912956545363"), Mul(Int(625), Pow(10, 12))), Sym("ly"))  # nopep8
    CONVERSIONS[UnitsLength.LIGHTYEAR][UnitsLength.FEMTOMETER] = \
        Mul(Mul(Int("94607304725808"), Pow(10, 17)), Sym("ly"))  # nopep8
    CONVERSIONS[UnitsLength.LIGHTYEAR][UnitsLength.FOOT] = \
        Mul(Rat(Int("775978968288652821"), Int(25)), Sym("ly"))  # nopep8
    CONVERSIONS[UnitsLength.LIGHTYEAR][UnitsLength.GIGAMETER] = \
        Mul(Rat(Int("5912956545363"), Int(625000)), Sym("ly"))  # nopep8
    CONVERSIONS[UnitsLength.LIGHTYEAR][UnitsLength.HECTOMETER] = \
        Mul(Int("94607304725808"), Sym("ly"))  # nopep8
    CONVERSIONS[UnitsLength.LIGHTYEAR][UnitsLength.INCH] = \
        Mul(Rat(Int("9311747619463833852"), Int(25)), Sym("ly"))  # nopep8
    CONVERSIONS[UnitsLength.LIGHTYEAR][UnitsLength.KILOMETER] = \
        Mul(Rat(Int("47303652362904"), Int(5)), Sym("ly"))  # nopep8
    CONVERSIONS[UnitsLength.LIGHTYEAR][UnitsLength.LINE] = \
        Mul(Rat(Int("111740971433566006224"), Int(25)), Sym("ly"))  # nopep8
    CONVERSIONS[UnitsLength.LIGHTYEAR][UnitsLength.MEGAMETER] = \
        Mul(Rat(Int("5912956545363"), Int(625)), Sym("ly"))  # nopep8
    CONVERSIONS[UnitsLength.LIGHTYEAR][UnitsLength.METER] = \
        Mul(Int("9460730472580800"), Sym("ly"))  # nopep8
    CONVERSIONS[UnitsLength.LIGHTYEAR][UnitsLength.MICROMETER] = \
        Mul(Mul(Int("94607304725808"), Pow(10, 8)), Sym("ly"))  # nopep8
    CONVERSIONS[UnitsLength.LIGHTYEAR][UnitsLength.MILE] = \
        Mul(Rat(Int("23514514190565237"), Int(4000)), Sym("ly"))  # nopep8
    CONVERSIONS[UnitsLength.LIGHTYEAR][UnitsLength.MILLIMETER] = \
        Mul(Mul(Int("94607304725808"), Pow(10, 5)), Sym("ly"))  # nopep8
    CONVERSIONS[UnitsLength.LIGHTYEAR][UnitsLength.NANOMETER] = \
        Mul(Mul(Int("94607304725808"), Pow(10, 11)), Sym("ly"))  # nopep8
    CONVERSIONS[UnitsLength.LIGHTYEAR][UnitsLength.PARSEC] = \
        Mul(Rat(Int("1970985515121"), Mul(Int(6428495), Pow(10, 6))), Sym("ly"))  # nopep8
    CONVERSIONS[UnitsLength.LIGHTYEAR][UnitsLength.PETAMETER] = \
        Mul(Rat(Int("5912956545363"), Mul(Int(625), Pow(10, 9))), Sym("ly"))  # nopep8
    CONVERSIONS[UnitsLength.LIGHTYEAR][UnitsLength.PICOMETER] = \
        Mul(Mul(Int("94607304725808"), Pow(10, 14)), Sym("ly"))  # nopep8
    CONVERSIONS[UnitsLength.LIGHTYEAR][UnitsLength.POINT] = \
        Mul(Rat(Int("670445828601396037344"), Int(25)), Sym("ly"))  # nopep8
    CONVERSIONS[UnitsLength.LIGHTYEAR][UnitsLength.TERAMETER] = \
        Mul(Rat(Int("5912956545363"), Mul(Int(625), Pow(10, 6))), Sym("ly"))  # nopep8
    CONVERSIONS[UnitsLength.LIGHTYEAR][UnitsLength.THOU] = \
        Mul(Rat(Int("2327936904865958463"), Int(6250)), Sym("ly"))  # nopep8
    CONVERSIONS[UnitsLength.LIGHTYEAR][UnitsLength.YARD] = \
        Mul(Rat(Int("258659656096217607"), Int(25)), Sym("ly"))  # nopep8
    CONVERSIONS[UnitsLength.LIGHTYEAR][UnitsLength.YOCTOMETER] = \
        Mul(Mul(Int("94607304725808"), Pow(10, 26)), Sym("ly"))  # nopep8
    CONVERSIONS[UnitsLength.LIGHTYEAR][UnitsLength.YOTTAMETER] = \
        Mul(Rat(Int("5912956545363"), Mul(Int(625), Pow(10, 18))), Sym("ly"))  # nopep8
    CONVERSIONS[UnitsLength.LIGHTYEAR][UnitsLength.ZEPTOMETER] = \
        Mul(Mul(Int("94607304725808"), Pow(10, 23)), Sym("ly"))  # nopep8
    CONVERSIONS[UnitsLength.LIGHTYEAR][UnitsLength.ZETTAMETER] = \
        Mul(Rat(Int("5912956545363"), Mul(Int(625), Pow(10, 15))), Sym("ly"))  # nopep8
    CONVERSIONS[UnitsLength.LINE][UnitsLength.ANGSTROM] = \
        Mul(Rat(Mul(Int(25), Pow(10, 12)), Int(1181103)), Sym("li"))  # nopep8
    CONVERSIONS[UnitsLength.LINE][UnitsLength.ASTRONOMICALUNIT] = \
        Mul(Rat(Int(25), Int("1766904938773821")), Sym("li"))  # nopep8
    CONVERSIONS[UnitsLength.LINE][UnitsLength.ATTOMETER] = \
        Mul(Rat(Mul(Int(25), Pow(10, 20)), Int(1181103)), Sym("li"))  # nopep8
    CONVERSIONS[UnitsLength.LINE][UnitsLength.CENTIMETER] = \
        Mul(Rat(Mul(Int(25), Pow(10, 4)), Int(1181103)), Sym("li"))  # nopep8
    CONVERSIONS[UnitsLength.LINE][UnitsLength.DECAMETER] = \
        Mul(Rat(Int(250), Int(1181103)), Sym("li"))  # nopep8
    CONVERSIONS[UnitsLength.LINE][UnitsLength.DECIMETER] = \
        Mul(Rat(Int(25000), Int(1181103)), Sym("li"))  # nopep8
    CONVERSIONS[UnitsLength.LINE][UnitsLength.EXAMETER] = \
        Mul(Rat(Int(1), Mul(Int(4724412), Pow(10, 14))), Sym("li"))  # nopep8
    CONVERSIONS[UnitsLength.LINE][UnitsLength.FEMTOMETER] = \
        Mul(Rat(Mul(Int(25), Pow(10, 17)), Int(1181103)), Sym("li"))  # nopep8
    CONVERSIONS[UnitsLength.LINE][UnitsLength.FOOT] = \
        Mul(Rat(Int(1), Int(144)), Sym("li"))  # nopep8
    CONVERSIONS[UnitsLength.LINE][UnitsLength.GIGAMETER] = \
        Mul(Rat(Int(1), Mul(Int(4724412), Pow(10, 5))), Sym("li"))  # nopep8
    CONVERSIONS[UnitsLength.LINE][UnitsLength.HECTOMETER] = \
        Mul(Rat(Int(25), Int(1181103)), Sym("li"))  # nopep8
    CONVERSIONS[UnitsLength.LINE][UnitsLength.INCH] = \
        Mul(Rat(Int(1), Int(12)), Sym("li"))  # nopep8
    CONVERSIONS[UnitsLength.LINE][UnitsLength.KILOMETER] = \
        Mul(Rat(Int(5), Int(2362206)), Sym("li"))  # nopep8
    CONVERSIONS[UnitsLength.LINE][UnitsLength.LIGHTYEAR] = \
        Mul(Rat(Int(25), Int("111740971433566006224")), Sym("li"))  # nopep8
    CONVERSIONS[UnitsLength.LINE][UnitsLength.MEGAMETER] = \
        Mul(Rat(Int(1), Int(472441200)), Sym("li"))  # nopep8
    CONVERSIONS[UnitsLength.LINE][UnitsLength.METER] = \
        Mul(Rat(Int(2500), Int(1181103)), Sym("li"))  # nopep8
    CONVERSIONS[UnitsLength.LINE][UnitsLength.MICROMETER] = \
        Mul(Rat(Mul(Int(25), Pow(10, 8)), Int(1181103)), Sym("li"))  # nopep8
    CONVERSIONS[UnitsLength.LINE][UnitsLength.MILE] = \
        Mul(Rat(Int(1), Int(760320)), Sym("li"))  # nopep8
    CONVERSIONS[UnitsLength.LINE][UnitsLength.MILLIMETER] = \
        Mul(Rat(Mul(Int(25), Pow(10, 5)), Int(1181103)), Sym("li"))  # nopep8
    CONVERSIONS[UnitsLength.LINE][UnitsLength.NANOMETER] = \
        Mul(Rat(Mul(Int(25), Pow(10, 11)), Int(1181103)), Sym("li"))  # nopep8
    CONVERSIONS[UnitsLength.LINE][UnitsLength.PARSEC] = \
        Mul(Rat(Int(1), Mul(Int("145780122815712"), Pow(10, 5))), Sym("li"))  # nopep8
    CONVERSIONS[UnitsLength.LINE][UnitsLength.PETAMETER] = \
        Mul(Rat(Int(1), Mul(Int(4724412), Pow(10, 11))), Sym("li"))  # nopep8
    CONVERSIONS[UnitsLength.LINE][UnitsLength.PICOMETER] = \
        Mul(Rat(Mul(Int(25), Pow(10, 14)), Int(1181103)), Sym("li"))  # nopep8
    CONVERSIONS[UnitsLength.LINE][UnitsLength.POINT] = \
        Mul(Int(6), Sym("li"))  # nopep8
    CONVERSIONS[UnitsLength.LINE][UnitsLength.TERAMETER] = \
        Mul(Rat(Int(1), Mul(Int(4724412), Pow(10, 8))), Sym("li"))  # nopep8
    CONVERSIONS[UnitsLength.LINE][UnitsLength.THOU] = \
        Mul(Rat(Int(1), Int(12000)), Sym("li"))  # nopep8
    CONVERSIONS[UnitsLength.LINE][UnitsLength.YARD] = \
        Mul(Rat(Int(1), Int(432)), Sym("li"))  # nopep8
    CONVERSIONS[UnitsLength.LINE][UnitsLength.YOCTOMETER] = \
        Mul(Rat(Mul(Int(25), Pow(10, 26)), Int(1181103)), Sym("li"))  # nopep8
    CONVERSIONS[UnitsLength.LINE][UnitsLength.YOTTAMETER] = \
        Mul(Rat(Int(1), Mul(Int(4724412), Pow(10, 20))), Sym("li"))  # nopep8
    CONVERSIONS[UnitsLength.LINE][UnitsLength.ZEPTOMETER] = \
        Mul(Rat(Mul(Int(25), Pow(10, 23)), Int(1181103)), Sym("li"))  # nopep8
    CONVERSIONS[UnitsLength.LINE][UnitsLength.ZETTAMETER] = \
        Mul(Rat(Int(1), Mul(Int(4724412), Pow(10, 17))), Sym("li"))  # nopep8
    CONVERSIONS[UnitsLength.MEGAMETER][UnitsLength.ANGSTROM] = \
        Mul(Pow(10, 16), Sym("megam"))  # nopep8
    CONVERSIONS[UnitsLength.MEGAMETER][UnitsLength.ASTRONOMICALUNIT] = \
        Mul(Rat(Pow(10, 4), Int(1495978707)), Sym("megam"))  # nopep8
    CONVERSIONS[UnitsLength.MEGAMETER][UnitsLength.ATTOMETER] = \
        Mul(Pow(10, 24), Sym("megam"))  # nopep8
    CONVERSIONS[UnitsLength.MEGAMETER][UnitsLength.CENTIMETER] = \
        Mul(Pow(10, 8), Sym("megam"))  # nopep8
    CONVERSIONS[UnitsLength.MEGAMETER][UnitsLength.DECAMETER] = \
        Mul(Pow(10, 5), Sym("megam"))  # nopep8
    CONVERSIONS[UnitsLength.MEGAMETER][UnitsLength.DECIMETER] = \
        Mul(Pow(10, 7), Sym("megam"))  # nopep8
    CONVERSIONS[UnitsLength.MEGAMETER][UnitsLength.EXAMETER] = \
        Mul(Rat(Int(1), Pow(10, 12)), Sym("megam"))  # nopep8
    CONVERSIONS[UnitsLength.MEGAMETER][UnitsLength.FEMTOMETER] = \
        Mul(Pow(10, 21), Sym("megam"))  # nopep8
    CONVERSIONS[UnitsLength.MEGAMETER][UnitsLength.FOOT] = \
        Mul(Rat(Int(9842525), Int(3)), Sym("megam"))  # nopep8
    CONVERSIONS[UnitsLength.MEGAMETER][UnitsLength.GIGAMETER] = \
        Mul(Rat(Int(1), Int(1000)), Sym("megam"))  # nopep8
    CONVERSIONS[UnitsLength.MEGAMETER][UnitsLength.HECTOMETER] = \
        Mul(Pow(10, 4), Sym("megam"))  # nopep8
    CONVERSIONS[UnitsLength.MEGAMETER][UnitsLength.INCH] = \
        Mul(Int(39370100), Sym("megam"))  # nopep8
    CONVERSIONS[UnitsLength.MEGAMETER][UnitsLength.KILOMETER] = \
        Mul(Int(1000), Sym("megam"))  # nopep8
    CONVERSIONS[UnitsLength.MEGAMETER][UnitsLength.LIGHTYEAR] = \
        Mul(Rat(Int(625), Int("5912956545363")), Sym("megam"))  # nopep8
    CONVERSIONS[UnitsLength.MEGAMETER][UnitsLength.LINE] = \
        Mul(Int(472441200), Sym("megam"))  # nopep8
    CONVERSIONS[UnitsLength.MEGAMETER][UnitsLength.METER] = \
        Mul(Pow(10, 6), Sym("megam"))  # nopep8
    CONVERSIONS[UnitsLength.MEGAMETER][UnitsLength.MICROMETER] = \
        Mul(Pow(10, 12), Sym("megam"))  # nopep8
    CONVERSIONS[UnitsLength.MEGAMETER][UnitsLength.MILE] = \
        Mul(Rat(Int(178955), Int(288)), Sym("megam"))  # nopep8
    CONVERSIONS[UnitsLength.MEGAMETER][UnitsLength.MILLIMETER] = \
        Mul(Pow(10, 9), Sym("megam"))  # nopep8
    CONVERSIONS[UnitsLength.MEGAMETER][UnitsLength.NANOMETER] = \
        Mul(Pow(10, 15), Sym("megam"))  # nopep8
    CONVERSIONS[UnitsLength.MEGAMETER][UnitsLength.PARSEC] = \
        Mul(Rat(Int(1), Int("30856776000")), Sym("megam"))  # nopep8
    CONVERSIONS[UnitsLength.MEGAMETER][UnitsLength.PETAMETER] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("megam"))  # nopep8
    CONVERSIONS[UnitsLength.MEGAMETER][UnitsLength.PICOMETER] = \
        Mul(Pow(10, 18), Sym("megam"))  # nopep8
    CONVERSIONS[UnitsLength.MEGAMETER][UnitsLength.POINT] = \
        Mul(Int("2834647200"), Sym("megam"))  # nopep8
    CONVERSIONS[UnitsLength.MEGAMETER][UnitsLength.TERAMETER] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("megam"))  # nopep8
    CONVERSIONS[UnitsLength.MEGAMETER][UnitsLength.THOU] = \
        Mul(Rat(Int(393701), Int(10)), Sym("megam"))  # nopep8
    CONVERSIONS[UnitsLength.MEGAMETER][UnitsLength.YARD] = \
        Mul(Rat(Int(9842525), Int(9)), Sym("megam"))  # nopep8
    CONVERSIONS[UnitsLength.MEGAMETER][UnitsLength.YOCTOMETER] = \
        Mul(Pow(10, 30), Sym("megam"))  # nopep8
    CONVERSIONS[UnitsLength.MEGAMETER][UnitsLength.YOTTAMETER] = \
        Mul(Rat(Int(1), Pow(10, 18)), Sym("megam"))  # nopep8
    CONVERSIONS[UnitsLength.MEGAMETER][UnitsLength.ZEPTOMETER] = \
        Mul(Pow(10, 27), Sym("megam"))  # nopep8
    CONVERSIONS[UnitsLength.MEGAMETER][UnitsLength.ZETTAMETER] = \
        Mul(Rat(Int(1), Pow(10, 15)), Sym("megam"))  # nopep8
    CONVERSIONS[UnitsLength.METER][UnitsLength.ANGSTROM] = \
        Mul(Pow(10, 10), Sym("m"))  # nopep8
    CONVERSIONS[UnitsLength.METER][UnitsLength.ASTRONOMICALUNIT] = \
        Mul(Rat(Int(1), Int("149597870700")), Sym("m"))  # nopep8
    CONVERSIONS[UnitsLength.METER][UnitsLength.ATTOMETER] = \
        Mul(Pow(10, 18), Sym("m"))  # nopep8
    CONVERSIONS[UnitsLength.METER][UnitsLength.CENTIMETER] = \
        Mul(Int(100), Sym("m"))  # nopep8
    CONVERSIONS[UnitsLength.METER][UnitsLength.DECAMETER] = \
        Mul(Rat(Int(1), Int(10)), Sym("m"))  # nopep8
    CONVERSIONS[UnitsLength.METER][UnitsLength.DECIMETER] = \
        Mul(Int(10), Sym("m"))  # nopep8
    CONVERSIONS[UnitsLength.METER][UnitsLength.EXAMETER] = \
        Mul(Rat(Int(1), Pow(10, 18)), Sym("m"))  # nopep8
    CONVERSIONS[UnitsLength.METER][UnitsLength.FEMTOMETER] = \
        Mul(Pow(10, 15), Sym("m"))  # nopep8
    CONVERSIONS[UnitsLength.METER][UnitsLength.FOOT] = \
        Mul(Rat(Int(393701), Mul(Int(12), Pow(10, 4))), Sym("m"))  # nopep8
    CONVERSIONS[UnitsLength.METER][UnitsLength.GIGAMETER] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("m"))  # nopep8
    CONVERSIONS[UnitsLength.METER][UnitsLength.HECTOMETER] = \
        Mul(Rat(Int(1), Int(100)), Sym("m"))  # nopep8
    CONVERSIONS[UnitsLength.METER][UnitsLength.INCH] = \
        Mul(Rat(Int(393701), Pow(10, 4)), Sym("m"))  # nopep8
    CONVERSIONS[UnitsLength.METER][UnitsLength.KILOMETER] = \
        Mul(Rat(Int(1), Int(1000)), Sym("m"))  # nopep8
    CONVERSIONS[UnitsLength.METER][UnitsLength.LIGHTYEAR] = \
        Mul(Rat(Int(1), Int("9460730472580800")), Sym("m"))  # nopep8
    CONVERSIONS[UnitsLength.METER][UnitsLength.LINE] = \
        Mul(Rat(Int(1181103), Int(2500)), Sym("m"))  # nopep8
    CONVERSIONS[UnitsLength.METER][UnitsLength.MEGAMETER] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("m"))  # nopep8
    CONVERSIONS[UnitsLength.METER][UnitsLength.MICROMETER] = \
        Mul(Pow(10, 6), Sym("m"))  # nopep8
    CONVERSIONS[UnitsLength.METER][UnitsLength.MILE] = \
        Mul(Rat(Int(35791), Mul(Int(576), Pow(10, 5))), Sym("m"))  # nopep8
    CONVERSIONS[UnitsLength.METER][UnitsLength.MILLIMETER] = \
        Mul(Int(1000), Sym("m"))  # nopep8
    CONVERSIONS[UnitsLength.METER][UnitsLength.NANOMETER] = \
        Mul(Pow(10, 9), Sym("m"))  # nopep8
    CONVERSIONS[UnitsLength.METER][UnitsLength.PARSEC] = \
        Mul(Rat(Int(1), Mul(Int(30856776), Pow(10, 9))), Sym("m"))  # nopep8
    CONVERSIONS[UnitsLength.METER][UnitsLength.PETAMETER] = \
        Mul(Rat(Int(1), Pow(10, 15)), Sym("m"))  # nopep8
    CONVERSIONS[UnitsLength.METER][UnitsLength.PICOMETER] = \
        Mul(Pow(10, 12), Sym("m"))  # nopep8
    CONVERSIONS[UnitsLength.METER][UnitsLength.POINT] = \
        Mul(Rat(Int(3543309), Int(1250)), Sym("m"))  # nopep8
    CONVERSIONS[UnitsLength.METER][UnitsLength.TERAMETER] = \
        Mul(Rat(Int(1), Pow(10, 12)), Sym("m"))  # nopep8
    CONVERSIONS[UnitsLength.METER][UnitsLength.THOU] = \
        Mul(Rat(Int(393701), Pow(10, 7)), Sym("m"))  # nopep8
    CONVERSIONS[UnitsLength.METER][UnitsLength.YARD] = \
        Mul(Rat(Int(393701), Mul(Int(36), Pow(10, 4))), Sym("m"))  # nopep8
    CONVERSIONS[UnitsLength.METER][UnitsLength.YOCTOMETER] = \
        Mul(Pow(10, 24), Sym("m"))  # nopep8
    CONVERSIONS[UnitsLength.METER][UnitsLength.YOTTAMETER] = \
        Mul(Rat(Int(1), Pow(10, 24)), Sym("m"))  # nopep8
    CONVERSIONS[UnitsLength.METER][UnitsLength.ZEPTOMETER] = \
        Mul(Pow(10, 21), Sym("m"))  # nopep8
    CONVERSIONS[UnitsLength.METER][UnitsLength.ZETTAMETER] = \
        Mul(Rat(Int(1), Pow(10, 21)), Sym("m"))  # nopep8
    CONVERSIONS[UnitsLength.MICROMETER][UnitsLength.ANGSTROM] = \
        Mul(Pow(10, 4), Sym("microm"))  # nopep8
    CONVERSIONS[UnitsLength.MICROMETER][UnitsLength.ASTRONOMICALUNIT] = \
        Mul(Rat(Int(1), Mul(Int(1495978707), Pow(10, 8))), Sym("microm"))  # nopep8
    CONVERSIONS[UnitsLength.MICROMETER][UnitsLength.ATTOMETER] = \
        Mul(Pow(10, 12), Sym("microm"))  # nopep8
    CONVERSIONS[UnitsLength.MICROMETER][UnitsLength.CENTIMETER] = \
        Mul(Rat(Int(1), Pow(10, 4)), Sym("microm"))  # nopep8
    CONVERSIONS[UnitsLength.MICROMETER][UnitsLength.DECAMETER] = \
        Mul(Rat(Int(1), Pow(10, 7)), Sym("microm"))  # nopep8
    CONVERSIONS[UnitsLength.MICROMETER][UnitsLength.DECIMETER] = \
        Mul(Rat(Int(1), Pow(10, 5)), Sym("microm"))  # nopep8
    CONVERSIONS[UnitsLength.MICROMETER][UnitsLength.EXAMETER] = \
        Mul(Rat(Int(1), Pow(10, 24)), Sym("microm"))  # nopep8
    CONVERSIONS[UnitsLength.MICROMETER][UnitsLength.FEMTOMETER] = \
        Mul(Pow(10, 9), Sym("microm"))  # nopep8
    CONVERSIONS[UnitsLength.MICROMETER][UnitsLength.FOOT] = \
        Mul(Rat(Int(393701), Mul(Int(12), Pow(10, 10))), Sym("microm"))  # nopep8
    CONVERSIONS[UnitsLength.MICROMETER][UnitsLength.GIGAMETER] = \
        Mul(Rat(Int(1), Pow(10, 15)), Sym("microm"))  # nopep8
    CONVERSIONS[UnitsLength.MICROMETER][UnitsLength.HECTOMETER] = \
        Mul(Rat(Int(1), Pow(10, 8)), Sym("microm"))  # nopep8
    CONVERSIONS[UnitsLength.MICROMETER][UnitsLength.INCH] = \
        Mul(Rat(Int(393701), Pow(10, 10)), Sym("microm"))  # nopep8
    CONVERSIONS[UnitsLength.MICROMETER][UnitsLength.KILOMETER] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("microm"))  # nopep8
    CONVERSIONS[UnitsLength.MICROMETER][UnitsLength.LIGHTYEAR] = \
        Mul(Rat(Int(1), Mul(Int("94607304725808"), Pow(10, 8))), Sym("microm"))  # nopep8
    CONVERSIONS[UnitsLength.MICROMETER][UnitsLength.LINE] = \
        Mul(Rat(Int(1181103), Mul(Int(25), Pow(10, 8))), Sym("microm"))  # nopep8
    CONVERSIONS[UnitsLength.MICROMETER][UnitsLength.MEGAMETER] = \
        Mul(Rat(Int(1), Pow(10, 12)), Sym("microm"))  # nopep8
    CONVERSIONS[UnitsLength.MICROMETER][UnitsLength.METER] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("microm"))  # nopep8
    CONVERSIONS[UnitsLength.MICROMETER][UnitsLength.MILE] = \
        Mul(Rat(Int(35791), Mul(Int(576), Pow(10, 11))), Sym("microm"))  # nopep8
    CONVERSIONS[UnitsLength.MICROMETER][UnitsLength.MILLIMETER] = \
        Mul(Rat(Int(1), Int(1000)), Sym("microm"))  # nopep8
    CONVERSIONS[UnitsLength.MICROMETER][UnitsLength.NANOMETER] = \
        Mul(Int(1000), Sym("microm"))  # nopep8
    CONVERSIONS[UnitsLength.MICROMETER][UnitsLength.PARSEC] = \
        Mul(Rat(Int(1), Mul(Int(30856776), Pow(10, 15))), Sym("microm"))  # nopep8
    CONVERSIONS[UnitsLength.MICROMETER][UnitsLength.PETAMETER] = \
        Mul(Rat(Int(1), Pow(10, 21)), Sym("microm"))  # nopep8
    CONVERSIONS[UnitsLength.MICROMETER][UnitsLength.PICOMETER] = \
        Mul(Pow(10, 6), Sym("microm"))  # nopep8
    CONVERSIONS[UnitsLength.MICROMETER][UnitsLength.POINT] = \
        Mul(Rat(Int(3543309), Mul(Int(125), Pow(10, 7))), Sym("microm"))  # nopep8
    CONVERSIONS[UnitsLength.MICROMETER][UnitsLength.TERAMETER] = \
        Mul(Rat(Int(1), Pow(10, 18)), Sym("microm"))  # nopep8
    CONVERSIONS[UnitsLength.MICROMETER][UnitsLength.THOU] = \
        Mul(Rat(Int(393701), Pow(10, 13)), Sym("microm"))  # nopep8
    CONVERSIONS[UnitsLength.MICROMETER][UnitsLength.YARD] = \
        Mul(Rat(Int(393701), Mul(Int(36), Pow(10, 10))), Sym("microm"))  # nopep8
    CONVERSIONS[UnitsLength.MICROMETER][UnitsLength.YOCTOMETER] = \
        Mul(Pow(10, 18), Sym("microm"))  # nopep8
    CONVERSIONS[UnitsLength.MICROMETER][UnitsLength.YOTTAMETER] = \
        Mul(Rat(Int(1), Pow(10, 30)), Sym("microm"))  # nopep8
    CONVERSIONS[UnitsLength.MICROMETER][UnitsLength.ZEPTOMETER] = \
        Mul(Pow(10, 15), Sym("microm"))  # nopep8
    CONVERSIONS[UnitsLength.MICROMETER][UnitsLength.ZETTAMETER] = \
        Mul(Rat(Int(1), Pow(10, 27)), Sym("microm"))  # nopep8
    CONVERSIONS[UnitsLength.MILE][UnitsLength.ANGSTROM] = \
        Mul(Rat(Mul(Int(576), Pow(10, 15)), Int(35791)), Sym("mi"))  # nopep8
    CONVERSIONS[UnitsLength.MILE][UnitsLength.ASTRONOMICALUNIT] = \
        Mul(Rat(Int(192000), Int("17847524634079")), Sym("mi"))  # nopep8
    CONVERSIONS[UnitsLength.MILE][UnitsLength.ATTOMETER] = \
        Mul(Rat(Mul(Int(576), Pow(10, 23)), Int(35791)), Sym("mi"))  # nopep8
    CONVERSIONS[UnitsLength.MILE][UnitsLength.CENTIMETER] = \
        Mul(Rat(Mul(Int(576), Pow(10, 7)), Int(35791)), Sym("mi"))  # nopep8
    CONVERSIONS[UnitsLength.MILE][UnitsLength.DECAMETER] = \
        Mul(Rat(Mul(Int(576), Pow(10, 4)), Int(35791)), Sym("mi"))  # nopep8
    CONVERSIONS[UnitsLength.MILE][UnitsLength.DECIMETER] = \
        Mul(Rat(Mul(Int(576), Pow(10, 6)), Int(35791)), Sym("mi"))  # nopep8
    CONVERSIONS[UnitsLength.MILE][UnitsLength.EXAMETER] = \
        Mul(Rat(Int(9), Mul(Int(559234375), Pow(10, 7))), Sym("mi"))  # nopep8
    CONVERSIONS[UnitsLength.MILE][UnitsLength.FEMTOMETER] = \
        Mul(Rat(Mul(Int(576), Pow(10, 20)), Int(35791)), Sym("mi"))  # nopep8
    CONVERSIONS[UnitsLength.MILE][UnitsLength.FOOT] = \
        Mul(Int(5280), Sym("mi"))  # nopep8
    CONVERSIONS[UnitsLength.MILE][UnitsLength.GIGAMETER] = \
        Mul(Rat(Int(36), Int(22369375)), Sym("mi"))  # nopep8
    CONVERSIONS[UnitsLength.MILE][UnitsLength.HECTOMETER] = \
        Mul(Rat(Int(576000), Int(35791)), Sym("mi"))  # nopep8
    CONVERSIONS[UnitsLength.MILE][UnitsLength.INCH] = \
        Mul(Int(63360), Sym("mi"))  # nopep8
    CONVERSIONS[UnitsLength.MILE][UnitsLength.KILOMETER] = \
        Mul(Rat(Int(57600), Int(35791)), Sym("mi"))  # nopep8
    CONVERSIONS[UnitsLength.MILE][UnitsLength.LIGHTYEAR] = \
        Mul(Rat(Int(4000), Int("23514514190565237")), Sym("mi"))  # nopep8
    CONVERSIONS[UnitsLength.MILE][UnitsLength.LINE] = \
        Mul(Int(760320), Sym("mi"))  # nopep8
    CONVERSIONS[UnitsLength.MILE][UnitsLength.MEGAMETER] = \
        Mul(Rat(Int(288), Int(178955)), Sym("mi"))  # nopep8
    CONVERSIONS[UnitsLength.MILE][UnitsLength.METER] = \
        Mul(Rat(Mul(Int(576), Pow(10, 5)), Int(35791)), Sym("mi"))  # nopep8
    CONVERSIONS[UnitsLength.MILE][UnitsLength.MICROMETER] = \
        Mul(Rat(Mul(Int(576), Pow(10, 11)), Int(35791)), Sym("mi"))  # nopep8
    CONVERSIONS[UnitsLength.MILE][UnitsLength.MILLIMETER] = \
        Mul(Rat(Mul(Int(576), Pow(10, 8)), Int(35791)), Sym("mi"))  # nopep8
    CONVERSIONS[UnitsLength.MILE][UnitsLength.NANOMETER] = \
        Mul(Rat(Mul(Int(576), Pow(10, 14)), Int(35791)), Sym("mi"))  # nopep8
    CONVERSIONS[UnitsLength.MILE][UnitsLength.PARSEC] = \
        Mul(Rat(Int(3), Int("57520566136250")), Sym("mi"))  # nopep8
    CONVERSIONS[UnitsLength.MILE][UnitsLength.PETAMETER] = \
        Mul(Rat(Int(9), Mul(Int(559234375), Pow(10, 4))), Sym("mi"))  # nopep8
    CONVERSIONS[UnitsLength.MILE][UnitsLength.PICOMETER] = \
        Mul(Rat(Mul(Int(576), Pow(10, 17)), Int(35791)), Sym("mi"))  # nopep8
    CONVERSIONS[UnitsLength.MILE][UnitsLength.POINT] = \
        Mul(Int(4561920), Sym("mi"))  # nopep8
    CONVERSIONS[UnitsLength.MILE][UnitsLength.TERAMETER] = \
        Mul(Rat(Int(9), Int("5592343750")), Sym("mi"))  # nopep8
    CONVERSIONS[UnitsLength.MILE][UnitsLength.THOU] = \
        Mul(Rat(Int(1584), Int(25)), Sym("mi"))  # nopep8
    CONVERSIONS[UnitsLength.MILE][UnitsLength.YARD] = \
        Mul(Int(1760), Sym("mi"))  # nopep8
    CONVERSIONS[UnitsLength.MILE][UnitsLength.YOCTOMETER] = \
        Mul(Rat(Mul(Int(576), Pow(10, 29)), Int(35791)), Sym("mi"))  # nopep8
    CONVERSIONS[UnitsLength.MILE][UnitsLength.YOTTAMETER] = \
        Mul(Rat(Int(9), Mul(Int(559234375), Pow(10, 13))), Sym("mi"))  # nopep8
    CONVERSIONS[UnitsLength.MILE][UnitsLength.ZEPTOMETER] = \
        Mul(Rat(Mul(Int(576), Pow(10, 26)), Int(35791)), Sym("mi"))  # nopep8
    CONVERSIONS[UnitsLength.MILE][UnitsLength.ZETTAMETER] = \
        Mul(Rat(Int(9), Mul(Int(559234375), Pow(10, 10))), Sym("mi"))  # nopep8
    CONVERSIONS[UnitsLength.MILLIMETER][UnitsLength.ANGSTROM] = \
        Mul(Pow(10, 7), Sym("millim"))  # nopep8
    CONVERSIONS[UnitsLength.MILLIMETER][UnitsLength.ASTRONOMICALUNIT] = \
        Mul(Rat(Int(1), Mul(Int(1495978707), Pow(10, 5))), Sym("millim"))  # nopep8
    CONVERSIONS[UnitsLength.MILLIMETER][UnitsLength.ATTOMETER] = \
        Mul(Pow(10, 15), Sym("millim"))  # nopep8
    CONVERSIONS[UnitsLength.MILLIMETER][UnitsLength.CENTIMETER] = \
        Mul(Rat(Int(1), Int(10)), Sym("millim"))  # nopep8
    CONVERSIONS[UnitsLength.MILLIMETER][UnitsLength.DECAMETER] = \
        Mul(Rat(Int(1), Pow(10, 4)), Sym("millim"))  # nopep8
    CONVERSIONS[UnitsLength.MILLIMETER][UnitsLength.DECIMETER] = \
        Mul(Rat(Int(1), Int(100)), Sym("millim"))  # nopep8
    CONVERSIONS[UnitsLength.MILLIMETER][UnitsLength.EXAMETER] = \
        Mul(Rat(Int(1), Pow(10, 21)), Sym("millim"))  # nopep8
    CONVERSIONS[UnitsLength.MILLIMETER][UnitsLength.FEMTOMETER] = \
        Mul(Pow(10, 12), Sym("millim"))  # nopep8
    CONVERSIONS[UnitsLength.MILLIMETER][UnitsLength.FOOT] = \
        Mul(Rat(Int(393701), Mul(Int(12), Pow(10, 7))), Sym("millim"))  # nopep8
    CONVERSIONS[UnitsLength.MILLIMETER][UnitsLength.GIGAMETER] = \
        Mul(Rat(Int(1), Pow(10, 12)), Sym("millim"))  # nopep8
    CONVERSIONS[UnitsLength.MILLIMETER][UnitsLength.HECTOMETER] = \
        Mul(Rat(Int(1), Pow(10, 5)), Sym("millim"))  # nopep8
    CONVERSIONS[UnitsLength.MILLIMETER][UnitsLength.INCH] = \
        Mul(Rat(Int(393701), Pow(10, 7)), Sym("millim"))  # nopep8
    CONVERSIONS[UnitsLength.MILLIMETER][UnitsLength.KILOMETER] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("millim"))  # nopep8
    CONVERSIONS[UnitsLength.MILLIMETER][UnitsLength.LIGHTYEAR] = \
        Mul(Rat(Int(1), Mul(Int("94607304725808"), Pow(10, 5))), Sym("millim"))  # nopep8
    CONVERSIONS[UnitsLength.MILLIMETER][UnitsLength.LINE] = \
        Mul(Rat(Int(1181103), Mul(Int(25), Pow(10, 5))), Sym("millim"))  # nopep8
    CONVERSIONS[UnitsLength.MILLIMETER][UnitsLength.MEGAMETER] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("millim"))  # nopep8
    CONVERSIONS[UnitsLength.MILLIMETER][UnitsLength.METER] = \
        Mul(Rat(Int(1), Int(1000)), Sym("millim"))  # nopep8
    CONVERSIONS[UnitsLength.MILLIMETER][UnitsLength.MICROMETER] = \
        Mul(Int(1000), Sym("millim"))  # nopep8
    CONVERSIONS[UnitsLength.MILLIMETER][UnitsLength.MILE] = \
        Mul(Rat(Int(35791), Mul(Int(576), Pow(10, 8))), Sym("millim"))  # nopep8
    CONVERSIONS[UnitsLength.MILLIMETER][UnitsLength.NANOMETER] = \
        Mul(Pow(10, 6), Sym("millim"))  # nopep8
    CONVERSIONS[UnitsLength.MILLIMETER][UnitsLength.PARSEC] = \
        Mul(Rat(Int(1), Mul(Int(30856776), Pow(10, 12))), Sym("millim"))  # nopep8
    CONVERSIONS[UnitsLength.MILLIMETER][UnitsLength.PETAMETER] = \
        Mul(Rat(Int(1), Pow(10, 18)), Sym("millim"))  # nopep8
    CONVERSIONS[UnitsLength.MILLIMETER][UnitsLength.PICOMETER] = \
        Mul(Pow(10, 9), Sym("millim"))  # nopep8
    CONVERSIONS[UnitsLength.MILLIMETER][UnitsLength.POINT] = \
        Mul(Rat(Int(3543309), Mul(Int(125), Pow(10, 4))), Sym("millim"))  # nopep8
    CONVERSIONS[UnitsLength.MILLIMETER][UnitsLength.TERAMETER] = \
        Mul(Rat(Int(1), Pow(10, 15)), Sym("millim"))  # nopep8
    CONVERSIONS[UnitsLength.MILLIMETER][UnitsLength.THOU] = \
        Mul(Rat(Int(393701), Pow(10, 10)), Sym("millim"))  # nopep8
    CONVERSIONS[UnitsLength.MILLIMETER][UnitsLength.YARD] = \
        Mul(Rat(Int(393701), Mul(Int(36), Pow(10, 7))), Sym("millim"))  # nopep8
    CONVERSIONS[UnitsLength.MILLIMETER][UnitsLength.YOCTOMETER] = \
        Mul(Pow(10, 21), Sym("millim"))  # nopep8
    CONVERSIONS[UnitsLength.MILLIMETER][UnitsLength.YOTTAMETER] = \
        Mul(Rat(Int(1), Pow(10, 27)), Sym("millim"))  # nopep8
    CONVERSIONS[UnitsLength.MILLIMETER][UnitsLength.ZEPTOMETER] = \
        Mul(Pow(10, 18), Sym("millim"))  # nopep8
    CONVERSIONS[UnitsLength.MILLIMETER][UnitsLength.ZETTAMETER] = \
        Mul(Rat(Int(1), Pow(10, 24)), Sym("millim"))  # nopep8
    CONVERSIONS[UnitsLength.NANOMETER][UnitsLength.ANGSTROM] = \
        Mul(Int(10), Sym("nanom"))  # nopep8
    CONVERSIONS[UnitsLength.NANOMETER][UnitsLength.ASTRONOMICALUNIT] = \
        Mul(Rat(Int(1), Mul(Int(1495978707), Pow(10, 11))), Sym("nanom"))  # nopep8
    CONVERSIONS[UnitsLength.NANOMETER][UnitsLength.ATTOMETER] = \
        Mul(Pow(10, 9), Sym("nanom"))  # nopep8
    CONVERSIONS[UnitsLength.NANOMETER][UnitsLength.CENTIMETER] = \
        Mul(Rat(Int(1), Pow(10, 7)), Sym("nanom"))  # nopep8
    CONVERSIONS[UnitsLength.NANOMETER][UnitsLength.DECAMETER] = \
        Mul(Rat(Int(1), Pow(10, 10)), Sym("nanom"))  # nopep8
    CONVERSIONS[UnitsLength.NANOMETER][UnitsLength.DECIMETER] = \
        Mul(Rat(Int(1), Pow(10, 8)), Sym("nanom"))  # nopep8
    CONVERSIONS[UnitsLength.NANOMETER][UnitsLength.EXAMETER] = \
        Mul(Rat(Int(1), Pow(10, 27)), Sym("nanom"))  # nopep8
    CONVERSIONS[UnitsLength.NANOMETER][UnitsLength.FEMTOMETER] = \
        Mul(Pow(10, 6), Sym("nanom"))  # nopep8
    CONVERSIONS[UnitsLength.NANOMETER][UnitsLength.FOOT] = \
        Mul(Rat(Int(393701), Mul(Int(12), Pow(10, 13))), Sym("nanom"))  # nopep8
    CONVERSIONS[UnitsLength.NANOMETER][UnitsLength.GIGAMETER] = \
        Mul(Rat(Int(1), Pow(10, 18)), Sym("nanom"))  # nopep8
    CONVERSIONS[UnitsLength.NANOMETER][UnitsLength.HECTOMETER] = \
        Mul(Rat(Int(1), Pow(10, 11)), Sym("nanom"))  # nopep8
    CONVERSIONS[UnitsLength.NANOMETER][UnitsLength.INCH] = \
        Mul(Rat(Int(393701), Pow(10, 13)), Sym("nanom"))  # nopep8
    CONVERSIONS[UnitsLength.NANOMETER][UnitsLength.KILOMETER] = \
        Mul(Rat(Int(1), Pow(10, 12)), Sym("nanom"))  # nopep8
    CONVERSIONS[UnitsLength.NANOMETER][UnitsLength.LIGHTYEAR] = \
        Mul(Rat(Int(1), Mul(Int("94607304725808"), Pow(10, 11))), Sym("nanom"))  # nopep8
    CONVERSIONS[UnitsLength.NANOMETER][UnitsLength.LINE] = \
        Mul(Rat(Int(1181103), Mul(Int(25), Pow(10, 11))), Sym("nanom"))  # nopep8
    CONVERSIONS[UnitsLength.NANOMETER][UnitsLength.MEGAMETER] = \
        Mul(Rat(Int(1), Pow(10, 15)), Sym("nanom"))  # nopep8
    CONVERSIONS[UnitsLength.NANOMETER][UnitsLength.METER] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("nanom"))  # nopep8
    CONVERSIONS[UnitsLength.NANOMETER][UnitsLength.MICROMETER] = \
        Mul(Rat(Int(1), Int(1000)), Sym("nanom"))  # nopep8
    CONVERSIONS[UnitsLength.NANOMETER][UnitsLength.MILE] = \
        Mul(Rat(Int(35791), Mul(Int(576), Pow(10, 14))), Sym("nanom"))  # nopep8
    CONVERSIONS[UnitsLength.NANOMETER][UnitsLength.MILLIMETER] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("nanom"))  # nopep8
    CONVERSIONS[UnitsLength.NANOMETER][UnitsLength.PARSEC] = \
        Mul(Rat(Int(1), Mul(Int(30856776), Pow(10, 18))), Sym("nanom"))  # nopep8
    CONVERSIONS[UnitsLength.NANOMETER][UnitsLength.PETAMETER] = \
        Mul(Rat(Int(1), Pow(10, 24)), Sym("nanom"))  # nopep8
    CONVERSIONS[UnitsLength.NANOMETER][UnitsLength.PICOMETER] = \
        Mul(Int(1000), Sym("nanom"))  # nopep8
    CONVERSIONS[UnitsLength.NANOMETER][UnitsLength.POINT] = \
        Mul(Rat(Int(3543309), Mul(Int(125), Pow(10, 10))), Sym("nanom"))  # nopep8
    CONVERSIONS[UnitsLength.NANOMETER][UnitsLength.TERAMETER] = \
        Mul(Rat(Int(1), Pow(10, 21)), Sym("nanom"))  # nopep8
    CONVERSIONS[UnitsLength.NANOMETER][UnitsLength.THOU] = \
        Mul(Rat(Int(393701), Pow(10, 16)), Sym("nanom"))  # nopep8
    CONVERSIONS[UnitsLength.NANOMETER][UnitsLength.YARD] = \
        Mul(Rat(Int(393701), Mul(Int(36), Pow(10, 13))), Sym("nanom"))  # nopep8
    CONVERSIONS[UnitsLength.NANOMETER][UnitsLength.YOCTOMETER] = \
        Mul(Pow(10, 15), Sym("nanom"))  # nopep8
    CONVERSIONS[UnitsLength.NANOMETER][UnitsLength.YOTTAMETER] = \
        Mul(Rat(Int(1), Pow(10, 33)), Sym("nanom"))  # nopep8
    CONVERSIONS[UnitsLength.NANOMETER][UnitsLength.ZEPTOMETER] = \
        Mul(Pow(10, 12), Sym("nanom"))  # nopep8
    CONVERSIONS[UnitsLength.NANOMETER][UnitsLength.ZETTAMETER] = \
        Mul(Rat(Int(1), Pow(10, 30)), Sym("nanom"))  # nopep8
    CONVERSIONS[UnitsLength.PARSEC][UnitsLength.ANGSTROM] = \
        Mul(Mul(Int(30856776), Pow(10, 19)), Sym("pc"))  # nopep8
    CONVERSIONS[UnitsLength.PARSEC][UnitsLength.ASTRONOMICALUNIT] = \
        Mul(Rat(Mul(Int(10285592), Pow(10, 7)), Int(498659569)), Sym("pc"))  # nopep8
    CONVERSIONS[UnitsLength.PARSEC][UnitsLength.ATTOMETER] = \
        Mul(Mul(Int(30856776), Pow(10, 27)), Sym("pc"))  # nopep8
    CONVERSIONS[UnitsLength.PARSEC][UnitsLength.CENTIMETER] = \
        Mul(Mul(Int(30856776), Pow(10, 11)), Sym("pc"))  # nopep8
    CONVERSIONS[UnitsLength.PARSEC][UnitsLength.DECAMETER] = \
        Mul(Mul(Int(30856776), Pow(10, 8)), Sym("pc"))  # nopep8
    CONVERSIONS[UnitsLength.PARSEC][UnitsLength.DECIMETER] = \
        Mul(Mul(Int(30856776), Pow(10, 10)), Sym("pc"))  # nopep8
    CONVERSIONS[UnitsLength.PARSEC][UnitsLength.EXAMETER] = \
        Mul(Rat(Int(3857097), Mul(Int(125), Pow(10, 6))), Sym("pc"))  # nopep8
    CONVERSIONS[UnitsLength.PARSEC][UnitsLength.FEMTOMETER] = \
        Mul(Mul(Int(30856776), Pow(10, 24)), Sym("pc"))  # nopep8
    CONVERSIONS[UnitsLength.PARSEC][UnitsLength.FOOT] = \
        Mul(Mul(Int("1012361963998"), Pow(10, 5)), Sym("pc"))  # nopep8
    CONVERSIONS[UnitsLength.PARSEC][UnitsLength.GIGAMETER] = \
        Mul(Int(30856776), Sym("pc"))  # nopep8
    CONVERSIONS[UnitsLength.PARSEC][UnitsLength.HECTOMETER] = \
        Mul(Mul(Int(30856776), Pow(10, 7)), Sym("pc"))  # nopep8
    CONVERSIONS[UnitsLength.PARSEC][UnitsLength.INCH] = \
        Mul(Mul(Int("12148343567976"), Pow(10, 5)), Sym("pc"))  # nopep8
    CONVERSIONS[UnitsLength.PARSEC][UnitsLength.KILOMETER] = \
        Mul(Mul(Int(30856776), Pow(10, 6)), Sym("pc"))  # nopep8
    CONVERSIONS[UnitsLength.PARSEC][UnitsLength.LIGHTYEAR] = \
        Mul(Rat(Mul(Int(6428495), Pow(10, 6)), Int("1970985515121")), Sym("pc"))  # nopep8
    CONVERSIONS[UnitsLength.PARSEC][UnitsLength.LINE] = \
        Mul(Mul(Int("145780122815712"), Pow(10, 5)), Sym("pc"))  # nopep8
    CONVERSIONS[UnitsLength.PARSEC][UnitsLength.MEGAMETER] = \
        Mul(Int("30856776000"), Sym("pc"))  # nopep8
    CONVERSIONS[UnitsLength.PARSEC][UnitsLength.METER] = \
        Mul(Mul(Int(30856776), Pow(10, 9)), Sym("pc"))  # nopep8
    CONVERSIONS[UnitsLength.PARSEC][UnitsLength.MICROMETER] = \
        Mul(Mul(Int(30856776), Pow(10, 15)), Sym("pc"))  # nopep8
    CONVERSIONS[UnitsLength.PARSEC][UnitsLength.MILE] = \
        Mul(Rat(Int("57520566136250"), Int(3)), Sym("pc"))  # nopep8
    CONVERSIONS[UnitsLength.PARSEC][UnitsLength.MILLIMETER] = \
        Mul(Mul(Int(30856776), Pow(10, 12)), Sym("pc"))  # nopep8
    CONVERSIONS[UnitsLength.PARSEC][UnitsLength.NANOMETER] = \
        Mul(Mul(Int(30856776), Pow(10, 18)), Sym("pc"))  # nopep8
    CONVERSIONS[UnitsLength.PARSEC][UnitsLength.PETAMETER] = \
        Mul(Rat(Int(3857097), Int(125000)), Sym("pc"))  # nopep8
    CONVERSIONS[UnitsLength.PARSEC][UnitsLength.PICOMETER] = \
        Mul(Mul(Int(30856776), Pow(10, 21)), Sym("pc"))  # nopep8
    CONVERSIONS[UnitsLength.PARSEC][UnitsLength.POINT] = \
        Mul(Mul(Int("874680736894272"), Pow(10, 5)), Sym("pc"))  # nopep8
    CONVERSIONS[UnitsLength.PARSEC][UnitsLength.TERAMETER] = \
        Mul(Rat(Int(3857097), Int(125)), Sym("pc"))  # nopep8
    CONVERSIONS[UnitsLength.PARSEC][UnitsLength.THOU] = \
        Mul(Int("1214834356797600"), Sym("pc"))  # nopep8
    CONVERSIONS[UnitsLength.PARSEC][UnitsLength.YARD] = \
        Mul(Rat(Mul(Int("1012361963998"), Pow(10, 5)), Int(3)), Sym("pc"))  # nopep8
    CONVERSIONS[UnitsLength.PARSEC][UnitsLength.YOCTOMETER] = \
        Mul(Mul(Int(30856776), Pow(10, 33)), Sym("pc"))  # nopep8
    CONVERSIONS[UnitsLength.PARSEC][UnitsLength.YOTTAMETER] = \
        Mul(Rat(Int(3857097), Mul(Int(125), Pow(10, 12))), Sym("pc"))  # nopep8
    CONVERSIONS[UnitsLength.PARSEC][UnitsLength.ZEPTOMETER] = \
        Mul(Mul(Int(30856776), Pow(10, 30)), Sym("pc"))  # nopep8
    CONVERSIONS[UnitsLength.PARSEC][UnitsLength.ZETTAMETER] = \
        Mul(Rat(Int(3857097), Mul(Int(125), Pow(10, 9))), Sym("pc"))  # nopep8
    CONVERSIONS[UnitsLength.PETAMETER][UnitsLength.ANGSTROM] = \
        Mul(Pow(10, 25), Sym("petam"))  # nopep8
    CONVERSIONS[UnitsLength.PETAMETER][UnitsLength.ASTRONOMICALUNIT] = \
        Mul(Rat(Pow(10, 13), Int(1495978707)), Sym("petam"))  # nopep8
    CONVERSIONS[UnitsLength.PETAMETER][UnitsLength.ATTOMETER] = \
        Mul(Pow(10, 33), Sym("petam"))  # nopep8
    CONVERSIONS[UnitsLength.PETAMETER][UnitsLength.CENTIMETER] = \
        Mul(Pow(10, 17), Sym("petam"))  # nopep8
    CONVERSIONS[UnitsLength.PETAMETER][UnitsLength.DECAMETER] = \
        Mul(Pow(10, 14), Sym("petam"))  # nopep8
    CONVERSIONS[UnitsLength.PETAMETER][UnitsLength.DECIMETER] = \
        Mul(Pow(10, 16), Sym("petam"))  # nopep8
    CONVERSIONS[UnitsLength.PETAMETER][UnitsLength.EXAMETER] = \
        Mul(Rat(Int(1), Int(1000)), Sym("petam"))  # nopep8
    CONVERSIONS[UnitsLength.PETAMETER][UnitsLength.FEMTOMETER] = \
        Mul(Pow(10, 30), Sym("petam"))  # nopep8
    CONVERSIONS[UnitsLength.PETAMETER][UnitsLength.FOOT] = \
        Mul(Rat(Mul(Int(9842525), Pow(10, 9)), Int(3)), Sym("petam"))  # nopep8
    CONVERSIONS[UnitsLength.PETAMETER][UnitsLength.GIGAMETER] = \
        Mul(Pow(10, 6), Sym("petam"))  # nopep8
    CONVERSIONS[UnitsLength.PETAMETER][UnitsLength.HECTOMETER] = \
        Mul(Pow(10, 13), Sym("petam"))  # nopep8
    CONVERSIONS[UnitsLength.PETAMETER][UnitsLength.INCH] = \
        Mul(Mul(Int(393701), Pow(10, 11)), Sym("petam"))  # nopep8
    CONVERSIONS[UnitsLength.PETAMETER][UnitsLength.KILOMETER] = \
        Mul(Pow(10, 12), Sym("petam"))  # nopep8
    CONVERSIONS[UnitsLength.PETAMETER][UnitsLength.LIGHTYEAR] = \
        Mul(Rat(Mul(Int(625), Pow(10, 9)), Int("5912956545363")), Sym("petam"))  # nopep8
    CONVERSIONS[UnitsLength.PETAMETER][UnitsLength.LINE] = \
        Mul(Mul(Int(4724412), Pow(10, 11)), Sym("petam"))  # nopep8
    CONVERSIONS[UnitsLength.PETAMETER][UnitsLength.MEGAMETER] = \
        Mul(Pow(10, 9), Sym("petam"))  # nopep8
    CONVERSIONS[UnitsLength.PETAMETER][UnitsLength.METER] = \
        Mul(Pow(10, 15), Sym("petam"))  # nopep8
    CONVERSIONS[UnitsLength.PETAMETER][UnitsLength.MICROMETER] = \
        Mul(Pow(10, 21), Sym("petam"))  # nopep8
    CONVERSIONS[UnitsLength.PETAMETER][UnitsLength.MILE] = \
        Mul(Rat(Mul(Int(559234375), Pow(10, 4)), Int(9)), Sym("petam"))  # nopep8
    CONVERSIONS[UnitsLength.PETAMETER][UnitsLength.MILLIMETER] = \
        Mul(Pow(10, 18), Sym("petam"))  # nopep8
    CONVERSIONS[UnitsLength.PETAMETER][UnitsLength.NANOMETER] = \
        Mul(Pow(10, 24), Sym("petam"))  # nopep8
    CONVERSIONS[UnitsLength.PETAMETER][UnitsLength.PARSEC] = \
        Mul(Rat(Int(125000), Int(3857097)), Sym("petam"))  # nopep8
    CONVERSIONS[UnitsLength.PETAMETER][UnitsLength.PICOMETER] = \
        Mul(Pow(10, 27), Sym("petam"))  # nopep8
    CONVERSIONS[UnitsLength.PETAMETER][UnitsLength.POINT] = \
        Mul(Mul(Int(28346472), Pow(10, 11)), Sym("petam"))  # nopep8
    CONVERSIONS[UnitsLength.PETAMETER][UnitsLength.TERAMETER] = \
        Mul(Int(1000), Sym("petam"))  # nopep8
    CONVERSIONS[UnitsLength.PETAMETER][UnitsLength.THOU] = \
        Mul(Mul(Int(393701), Pow(10, 8)), Sym("petam"))  # nopep8
    CONVERSIONS[UnitsLength.PETAMETER][UnitsLength.YARD] = \
        Mul(Rat(Mul(Int(9842525), Pow(10, 9)), Int(9)), Sym("petam"))  # nopep8
    CONVERSIONS[UnitsLength.PETAMETER][UnitsLength.YOCTOMETER] = \
        Mul(Pow(10, 39), Sym("petam"))  # nopep8
    CONVERSIONS[UnitsLength.PETAMETER][UnitsLength.YOTTAMETER] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("petam"))  # nopep8
    CONVERSIONS[UnitsLength.PETAMETER][UnitsLength.ZEPTOMETER] = \
        Mul(Pow(10, 36), Sym("petam"))  # nopep8
    CONVERSIONS[UnitsLength.PETAMETER][UnitsLength.ZETTAMETER] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("petam"))  # nopep8
    CONVERSIONS[UnitsLength.PICOMETER][UnitsLength.ANGSTROM] = \
        Mul(Rat(Int(1), Int(100)), Sym("picom"))  # nopep8
    CONVERSIONS[UnitsLength.PICOMETER][UnitsLength.ASTRONOMICALUNIT] = \
        Mul(Rat(Int(1), Mul(Int(1495978707), Pow(10, 14))), Sym("picom"))  # nopep8
    CONVERSIONS[UnitsLength.PICOMETER][UnitsLength.ATTOMETER] = \
        Mul(Pow(10, 6), Sym("picom"))  # nopep8
    CONVERSIONS[UnitsLength.PICOMETER][UnitsLength.CENTIMETER] = \
        Mul(Rat(Int(1), Pow(10, 10)), Sym("picom"))  # nopep8
    CONVERSIONS[UnitsLength.PICOMETER][UnitsLength.DECAMETER] = \
        Mul(Rat(Int(1), Pow(10, 13)), Sym("picom"))  # nopep8
    CONVERSIONS[UnitsLength.PICOMETER][UnitsLength.DECIMETER] = \
        Mul(Rat(Int(1), Pow(10, 11)), Sym("picom"))  # nopep8
    CONVERSIONS[UnitsLength.PICOMETER][UnitsLength.EXAMETER] = \
        Mul(Rat(Int(1), Pow(10, 30)), Sym("picom"))  # nopep8
    CONVERSIONS[UnitsLength.PICOMETER][UnitsLength.FEMTOMETER] = \
        Mul(Int(1000), Sym("picom"))  # nopep8
    CONVERSIONS[UnitsLength.PICOMETER][UnitsLength.FOOT] = \
        Mul(Rat(Int(393701), Mul(Int(12), Pow(10, 16))), Sym("picom"))  # nopep8
    CONVERSIONS[UnitsLength.PICOMETER][UnitsLength.GIGAMETER] = \
        Mul(Rat(Int(1), Pow(10, 21)), Sym("picom"))  # nopep8
    CONVERSIONS[UnitsLength.PICOMETER][UnitsLength.HECTOMETER] = \
        Mul(Rat(Int(1), Pow(10, 14)), Sym("picom"))  # nopep8
    CONVERSIONS[UnitsLength.PICOMETER][UnitsLength.INCH] = \
        Mul(Rat(Int(393701), Pow(10, 16)), Sym("picom"))  # nopep8
    CONVERSIONS[UnitsLength.PICOMETER][UnitsLength.KILOMETER] = \
        Mul(Rat(Int(1), Pow(10, 15)), Sym("picom"))  # nopep8
    CONVERSIONS[UnitsLength.PICOMETER][UnitsLength.LIGHTYEAR] = \
        Mul(Rat(Int(1), Mul(Int("94607304725808"), Pow(10, 14))), Sym("picom"))  # nopep8
    CONVERSIONS[UnitsLength.PICOMETER][UnitsLength.LINE] = \
        Mul(Rat(Int(1181103), Mul(Int(25), Pow(10, 14))), Sym("picom"))  # nopep8
    CONVERSIONS[UnitsLength.PICOMETER][UnitsLength.MEGAMETER] = \
        Mul(Rat(Int(1), Pow(10, 18)), Sym("picom"))  # nopep8
    CONVERSIONS[UnitsLength.PICOMETER][UnitsLength.METER] = \
        Mul(Rat(Int(1), Pow(10, 12)), Sym("picom"))  # nopep8
    CONVERSIONS[UnitsLength.PICOMETER][UnitsLength.MICROMETER] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("picom"))  # nopep8
    CONVERSIONS[UnitsLength.PICOMETER][UnitsLength.MILE] = \
        Mul(Rat(Int(35791), Mul(Int(576), Pow(10, 17))), Sym("picom"))  # nopep8
    CONVERSIONS[UnitsLength.PICOMETER][UnitsLength.MILLIMETER] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("picom"))  # nopep8
    CONVERSIONS[UnitsLength.PICOMETER][UnitsLength.NANOMETER] = \
        Mul(Rat(Int(1), Int(1000)), Sym("picom"))  # nopep8
    CONVERSIONS[UnitsLength.PICOMETER][UnitsLength.PARSEC] = \
        Mul(Rat(Int(1), Mul(Int(30856776), Pow(10, 21))), Sym("picom"))  # nopep8
    CONVERSIONS[UnitsLength.PICOMETER][UnitsLength.PETAMETER] = \
        Mul(Rat(Int(1), Pow(10, 27)), Sym("picom"))  # nopep8
    CONVERSIONS[UnitsLength.PICOMETER][UnitsLength.POINT] = \
        Mul(Rat(Int(3543309), Mul(Int(125), Pow(10, 13))), Sym("picom"))  # nopep8
    CONVERSIONS[UnitsLength.PICOMETER][UnitsLength.TERAMETER] = \
        Mul(Rat(Int(1), Pow(10, 24)), Sym("picom"))  # nopep8
    CONVERSIONS[UnitsLength.PICOMETER][UnitsLength.THOU] = \
        Mul(Rat(Int(393701), Pow(10, 19)), Sym("picom"))  # nopep8
    CONVERSIONS[UnitsLength.PICOMETER][UnitsLength.YARD] = \
        Mul(Rat(Int(393701), Mul(Int(36), Pow(10, 16))), Sym("picom"))  # nopep8
    CONVERSIONS[UnitsLength.PICOMETER][UnitsLength.YOCTOMETER] = \
        Mul(Pow(10, 12), Sym("picom"))  # nopep8
    CONVERSIONS[UnitsLength.PICOMETER][UnitsLength.YOTTAMETER] = \
        Mul(Rat(Int(1), Pow(10, 36)), Sym("picom"))  # nopep8
    CONVERSIONS[UnitsLength.PICOMETER][UnitsLength.ZEPTOMETER] = \
        Mul(Pow(10, 9), Sym("picom"))  # nopep8
    CONVERSIONS[UnitsLength.PICOMETER][UnitsLength.ZETTAMETER] = \
        Mul(Rat(Int(1), Pow(10, 33)), Sym("picom"))  # nopep8
    CONVERSIONS[UnitsLength.POINT][UnitsLength.ANGSTROM] = \
        Mul(Rat(Mul(Int(125), Pow(10, 11)), Int(3543309)), Sym("pt"))  # nopep8
    CONVERSIONS[UnitsLength.POINT][UnitsLength.ASTRONOMICALUNIT] = \
        Mul(Rat(Int(25), Int("10601429632642926")), Sym("pt"))  # nopep8
    CONVERSIONS[UnitsLength.POINT][UnitsLength.ATTOMETER] = \
        Mul(Rat(Mul(Int(125), Pow(10, 19)), Int(3543309)), Sym("pt"))  # nopep8
    CONVERSIONS[UnitsLength.POINT][UnitsLength.CENTIMETER] = \
        Mul(Rat(Int(125000), Int(3543309)), Sym("pt"))  # nopep8
    CONVERSIONS[UnitsLength.POINT][UnitsLength.DECAMETER] = \
        Mul(Rat(Int(125), Int(3543309)), Sym("pt"))  # nopep8
    CONVERSIONS[UnitsLength.POINT][UnitsLength.DECIMETER] = \
        Mul(Rat(Int(12500), Int(3543309)), Sym("pt"))  # nopep8
    CONVERSIONS[UnitsLength.POINT][UnitsLength.EXAMETER] = \
        Mul(Rat(Int(1), Mul(Int(28346472), Pow(10, 14))), Sym("pt"))  # nopep8
    CONVERSIONS[UnitsLength.POINT][UnitsLength.FEMTOMETER] = \
        Mul(Rat(Mul(Int(125), Pow(10, 16)), Int(3543309)), Sym("pt"))  # nopep8
    CONVERSIONS[UnitsLength.POINT][UnitsLength.FOOT] = \
        Mul(Rat(Int(1), Int(864)), Sym("pt"))  # nopep8
    CONVERSIONS[UnitsLength.POINT][UnitsLength.GIGAMETER] = \
        Mul(Rat(Int(1), Mul(Int(28346472), Pow(10, 5))), Sym("pt"))  # nopep8
    CONVERSIONS[UnitsLength.POINT][UnitsLength.HECTOMETER] = \
        Mul(Rat(Int(25), Int(7086618)), Sym("pt"))  # nopep8
    CONVERSIONS[UnitsLength.POINT][UnitsLength.INCH] = \
        Mul(Rat(Int(1), Int(72)), Sym("pt"))  # nopep8
    CONVERSIONS[UnitsLength.POINT][UnitsLength.KILOMETER] = \
        Mul(Rat(Int(5), Int(14173236)), Sym("pt"))  # nopep8
    CONVERSIONS[UnitsLength.POINT][UnitsLength.LIGHTYEAR] = \
        Mul(Rat(Int(25), Int("670445828601396037344")), Sym("pt"))  # nopep8
    CONVERSIONS[UnitsLength.POINT][UnitsLength.LINE] = \
        Mul(Rat(Int(1), Int(6)), Sym("pt"))  # nopep8
    CONVERSIONS[UnitsLength.POINT][UnitsLength.MEGAMETER] = \
        Mul(Rat(Int(1), Int("2834647200")), Sym("pt"))  # nopep8
    CONVERSIONS[UnitsLength.POINT][UnitsLength.METER] = \
        Mul(Rat(Int(1250), Int(3543309)), Sym("pt"))  # nopep8
    CONVERSIONS[UnitsLength.POINT][UnitsLength.MICROMETER] = \
        Mul(Rat(Mul(Int(125), Pow(10, 7)), Int(3543309)), Sym("pt"))  # nopep8
    CONVERSIONS[UnitsLength.POINT][UnitsLength.MILE] = \
        Mul(Rat(Int(1), Int(4561920)), Sym("pt"))  # nopep8
    CONVERSIONS[UnitsLength.POINT][UnitsLength.MILLIMETER] = \
        Mul(Rat(Mul(Int(125), Pow(10, 4)), Int(3543309)), Sym("pt"))  # nopep8
    CONVERSIONS[UnitsLength.POINT][UnitsLength.NANOMETER] = \
        Mul(Rat(Mul(Int(125), Pow(10, 10)), Int(3543309)), Sym("pt"))  # nopep8
    CONVERSIONS[UnitsLength.POINT][UnitsLength.PARSEC] = \
        Mul(Rat(Int(1), Mul(Int("874680736894272"), Pow(10, 5))), Sym("pt"))  # nopep8
    CONVERSIONS[UnitsLength.POINT][UnitsLength.PETAMETER] = \
        Mul(Rat(Int(1), Mul(Int(28346472), Pow(10, 11))), Sym("pt"))  # nopep8
    CONVERSIONS[UnitsLength.POINT][UnitsLength.PICOMETER] = \
        Mul(Rat(Mul(Int(125), Pow(10, 13)), Int(3543309)), Sym("pt"))  # nopep8
    CONVERSIONS[UnitsLength.POINT][UnitsLength.TERAMETER] = \
        Mul(Rat(Int(1), Mul(Int(28346472), Pow(10, 8))), Sym("pt"))  # nopep8
    CONVERSIONS[UnitsLength.POINT][UnitsLength.THOU] = \
        Mul(Rat(Int(1), Int(72000)), Sym("pt"))  # nopep8
    CONVERSIONS[UnitsLength.POINT][UnitsLength.YARD] = \
        Mul(Rat(Int(1), Int(2592)), Sym("pt"))  # nopep8
    CONVERSIONS[UnitsLength.POINT][UnitsLength.YOCTOMETER] = \
        Mul(Rat(Mul(Int(125), Pow(10, 25)), Int(3543309)), Sym("pt"))  # nopep8
    CONVERSIONS[UnitsLength.POINT][UnitsLength.YOTTAMETER] = \
        Mul(Rat(Int(1), Mul(Int(28346472), Pow(10, 20))), Sym("pt"))  # nopep8
    CONVERSIONS[UnitsLength.POINT][UnitsLength.ZEPTOMETER] = \
        Mul(Rat(Mul(Int(125), Pow(10, 22)), Int(3543309)), Sym("pt"))  # nopep8
    CONVERSIONS[UnitsLength.POINT][UnitsLength.ZETTAMETER] = \
        Mul(Rat(Int(1), Mul(Int(28346472), Pow(10, 17))), Sym("pt"))  # nopep8
    CONVERSIONS[UnitsLength.TERAMETER][UnitsLength.ANGSTROM] = \
        Mul(Pow(10, 22), Sym("teram"))  # nopep8
    CONVERSIONS[UnitsLength.TERAMETER][UnitsLength.ASTRONOMICALUNIT] = \
        Mul(Rat(Pow(10, 10), Int(1495978707)), Sym("teram"))  # nopep8
    CONVERSIONS[UnitsLength.TERAMETER][UnitsLength.ATTOMETER] = \
        Mul(Pow(10, 30), Sym("teram"))  # nopep8
    CONVERSIONS[UnitsLength.TERAMETER][UnitsLength.CENTIMETER] = \
        Mul(Pow(10, 14), Sym("teram"))  # nopep8
    CONVERSIONS[UnitsLength.TERAMETER][UnitsLength.DECAMETER] = \
        Mul(Pow(10, 11), Sym("teram"))  # nopep8
    CONVERSIONS[UnitsLength.TERAMETER][UnitsLength.DECIMETER] = \
        Mul(Pow(10, 13), Sym("teram"))  # nopep8
    CONVERSIONS[UnitsLength.TERAMETER][UnitsLength.EXAMETER] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("teram"))  # nopep8
    CONVERSIONS[UnitsLength.TERAMETER][UnitsLength.FEMTOMETER] = \
        Mul(Pow(10, 27), Sym("teram"))  # nopep8
    CONVERSIONS[UnitsLength.TERAMETER][UnitsLength.FOOT] = \
        Mul(Rat(Mul(Int(9842525), Pow(10, 6)), Int(3)), Sym("teram"))  # nopep8
    CONVERSIONS[UnitsLength.TERAMETER][UnitsLength.GIGAMETER] = \
        Mul(Int(1000), Sym("teram"))  # nopep8
    CONVERSIONS[UnitsLength.TERAMETER][UnitsLength.HECTOMETER] = \
        Mul(Pow(10, 10), Sym("teram"))  # nopep8
    CONVERSIONS[UnitsLength.TERAMETER][UnitsLength.INCH] = \
        Mul(Mul(Int(393701), Pow(10, 8)), Sym("teram"))  # nopep8
    CONVERSIONS[UnitsLength.TERAMETER][UnitsLength.KILOMETER] = \
        Mul(Pow(10, 9), Sym("teram"))  # nopep8
    CONVERSIONS[UnitsLength.TERAMETER][UnitsLength.LIGHTYEAR] = \
        Mul(Rat(Mul(Int(625), Pow(10, 6)), Int("5912956545363")), Sym("teram"))  # nopep8
    CONVERSIONS[UnitsLength.TERAMETER][UnitsLength.LINE] = \
        Mul(Mul(Int(4724412), Pow(10, 8)), Sym("teram"))  # nopep8
    CONVERSIONS[UnitsLength.TERAMETER][UnitsLength.MEGAMETER] = \
        Mul(Pow(10, 6), Sym("teram"))  # nopep8
    CONVERSIONS[UnitsLength.TERAMETER][UnitsLength.METER] = \
        Mul(Pow(10, 12), Sym("teram"))  # nopep8
    CONVERSIONS[UnitsLength.TERAMETER][UnitsLength.MICROMETER] = \
        Mul(Pow(10, 18), Sym("teram"))  # nopep8
    CONVERSIONS[UnitsLength.TERAMETER][UnitsLength.MILE] = \
        Mul(Rat(Int("5592343750"), Int(9)), Sym("teram"))  # nopep8
    CONVERSIONS[UnitsLength.TERAMETER][UnitsLength.MILLIMETER] = \
        Mul(Pow(10, 15), Sym("teram"))  # nopep8
    CONVERSIONS[UnitsLength.TERAMETER][UnitsLength.NANOMETER] = \
        Mul(Pow(10, 21), Sym("teram"))  # nopep8
    CONVERSIONS[UnitsLength.TERAMETER][UnitsLength.PARSEC] = \
        Mul(Rat(Int(125), Int(3857097)), Sym("teram"))  # nopep8
    CONVERSIONS[UnitsLength.TERAMETER][UnitsLength.PETAMETER] = \
        Mul(Rat(Int(1), Int(1000)), Sym("teram"))  # nopep8
    CONVERSIONS[UnitsLength.TERAMETER][UnitsLength.PICOMETER] = \
        Mul(Pow(10, 24), Sym("teram"))  # nopep8
    CONVERSIONS[UnitsLength.TERAMETER][UnitsLength.POINT] = \
        Mul(Mul(Int(28346472), Pow(10, 8)), Sym("teram"))  # nopep8
    CONVERSIONS[UnitsLength.TERAMETER][UnitsLength.THOU] = \
        Mul(Mul(Int(393701), Pow(10, 5)), Sym("teram"))  # nopep8
    CONVERSIONS[UnitsLength.TERAMETER][UnitsLength.YARD] = \
        Mul(Rat(Mul(Int(9842525), Pow(10, 6)), Int(9)), Sym("teram"))  # nopep8
    CONVERSIONS[UnitsLength.TERAMETER][UnitsLength.YOCTOMETER] = \
        Mul(Pow(10, 36), Sym("teram"))  # nopep8
    CONVERSIONS[UnitsLength.TERAMETER][UnitsLength.YOTTAMETER] = \
        Mul(Rat(Int(1), Pow(10, 12)), Sym("teram"))  # nopep8
    CONVERSIONS[UnitsLength.TERAMETER][UnitsLength.ZEPTOMETER] = \
        Mul(Pow(10, 33), Sym("teram"))  # nopep8
    CONVERSIONS[UnitsLength.TERAMETER][UnitsLength.ZETTAMETER] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("teram"))  # nopep8
    CONVERSIONS[UnitsLength.THOU][UnitsLength.ANGSTROM] = \
        Mul(Rat(Pow(10, 17), Int(393701)), Sym("thou"))  # nopep8
    CONVERSIONS[UnitsLength.THOU][UnitsLength.ASTRONOMICALUNIT] = \
        Mul(Rat(Pow(10, 5), Int("588968312924607")), Sym("thou"))  # nopep8
    CONVERSIONS[UnitsLength.THOU][UnitsLength.ATTOMETER] = \
        Mul(Rat(Pow(10, 25), Int(393701)), Sym("thou"))  # nopep8
    CONVERSIONS[UnitsLength.THOU][UnitsLength.CENTIMETER] = \
        Mul(Rat(Pow(10, 9), Int(393701)), Sym("thou"))  # nopep8
    CONVERSIONS[UnitsLength.THOU][UnitsLength.DECAMETER] = \
        Mul(Rat(Pow(10, 6), Int(393701)), Sym("thou"))  # nopep8
    CONVERSIONS[UnitsLength.THOU][UnitsLength.DECIMETER] = \
        Mul(Rat(Pow(10, 8), Int(393701)), Sym("thou"))  # nopep8
    CONVERSIONS[UnitsLength.THOU][UnitsLength.EXAMETER] = \
        Mul(Rat(Int(1), Mul(Int(393701), Pow(10, 11))), Sym("thou"))  # nopep8
    CONVERSIONS[UnitsLength.THOU][UnitsLength.FEMTOMETER] = \
        Mul(Rat(Pow(10, 22), Int(393701)), Sym("thou"))  # nopep8
    CONVERSIONS[UnitsLength.THOU][UnitsLength.FOOT] = \
        Mul(Rat(Int(250), Int(3)), Sym("thou"))  # nopep8
    CONVERSIONS[UnitsLength.THOU][UnitsLength.GIGAMETER] = \
        Mul(Rat(Int(1), Int(39370100)), Sym("thou"))  # nopep8
    CONVERSIONS[UnitsLength.THOU][UnitsLength.HECTOMETER] = \
        Mul(Rat(Pow(10, 5), Int(393701)), Sym("thou"))  # nopep8
    CONVERSIONS[UnitsLength.THOU][UnitsLength.INCH] = \
        Mul(Int(1000), Sym("thou"))  # nopep8
    CONVERSIONS[UnitsLength.THOU][UnitsLength.KILOMETER] = \
        Mul(Rat(Pow(10, 4), Int(393701)), Sym("thou"))  # nopep8
    CONVERSIONS[UnitsLength.THOU][UnitsLength.LIGHTYEAR] = \
        Mul(Rat(Int(6250), Int("2327936904865958463")), Sym("thou"))  # nopep8
    CONVERSIONS[UnitsLength.THOU][UnitsLength.LINE] = \
        Mul(Int(12000), Sym("thou"))  # nopep8
    CONVERSIONS[UnitsLength.THOU][UnitsLength.MEGAMETER] = \
        Mul(Rat(Int(10), Int(393701)), Sym("thou"))  # nopep8
    CONVERSIONS[UnitsLength.THOU][UnitsLength.METER] = \
        Mul(Rat(Pow(10, 7), Int(393701)), Sym("thou"))  # nopep8
    CONVERSIONS[UnitsLength.THOU][UnitsLength.MICROMETER] = \
        Mul(Rat(Pow(10, 13), Int(393701)), Sym("thou"))  # nopep8
    CONVERSIONS[UnitsLength.THOU][UnitsLength.MILE] = \
        Mul(Rat(Int(25), Int(1584)), Sym("thou"))  # nopep8
    CONVERSIONS[UnitsLength.THOU][UnitsLength.MILLIMETER] = \
        Mul(Rat(Pow(10, 10), Int(393701)), Sym("thou"))  # nopep8
    CONVERSIONS[UnitsLength.THOU][UnitsLength.NANOMETER] = \
        Mul(Rat(Pow(10, 16), Int(393701)), Sym("thou"))  # nopep8
    CONVERSIONS[UnitsLength.THOU][UnitsLength.PARSEC] = \
        Mul(Rat(Int(1), Int("1214834356797600")), Sym("thou"))  # nopep8
    CONVERSIONS[UnitsLength.THOU][UnitsLength.PETAMETER] = \
        Mul(Rat(Int(1), Mul(Int(393701), Pow(10, 8))), Sym("thou"))  # nopep8
    CONVERSIONS[UnitsLength.THOU][UnitsLength.PICOMETER] = \
        Mul(Rat(Pow(10, 19), Int(393701)), Sym("thou"))  # nopep8
    CONVERSIONS[UnitsLength.THOU][UnitsLength.POINT] = \
        Mul(Int(72000), Sym("thou"))  # nopep8
    CONVERSIONS[UnitsLength.THOU][UnitsLength.TERAMETER] = \
        Mul(Rat(Int(1), Mul(Int(393701), Pow(10, 5))), Sym("thou"))  # nopep8
    CONVERSIONS[UnitsLength.THOU][UnitsLength.YARD] = \
        Mul(Rat(Int(250), Int(9)), Sym("thou"))  # nopep8
    CONVERSIONS[UnitsLength.THOU][UnitsLength.YOCTOMETER] = \
        Mul(Rat(Pow(10, 31), Int(393701)), Sym("thou"))  # nopep8
    CONVERSIONS[UnitsLength.THOU][UnitsLength.YOTTAMETER] = \
        Mul(Rat(Int(1), Mul(Int(393701), Pow(10, 17))), Sym("thou"))  # nopep8
    CONVERSIONS[UnitsLength.THOU][UnitsLength.ZEPTOMETER] = \
        Mul(Rat(Pow(10, 28), Int(393701)), Sym("thou"))  # nopep8
    CONVERSIONS[UnitsLength.THOU][UnitsLength.ZETTAMETER] = \
        Mul(Rat(Int(1), Mul(Int(393701), Pow(10, 14))), Sym("thou"))  # nopep8
    CONVERSIONS[UnitsLength.YARD][UnitsLength.ANGSTROM] = \
        Mul(Rat(Mul(Int(36), Pow(10, 14)), Int(393701)), Sym("yd"))  # nopep8
    CONVERSIONS[UnitsLength.YARD][UnitsLength.ASTRONOMICALUNIT] = \
        Mul(Rat(Int(1200), Int("196322770974869")), Sym("yd"))  # nopep8
    CONVERSIONS[UnitsLength.YARD][UnitsLength.ATTOMETER] = \
        Mul(Rat(Mul(Int(36), Pow(10, 22)), Int(393701)), Sym("yd"))  # nopep8
    CONVERSIONS[UnitsLength.YARD][UnitsLength.CENTIMETER] = \
        Mul(Rat(Mul(Int(36), Pow(10, 6)), Int(393701)), Sym("yd"))  # nopep8
    CONVERSIONS[UnitsLength.YARD][UnitsLength.DECAMETER] = \
        Mul(Rat(Int(36000), Int(393701)), Sym("yd"))  # nopep8
    CONVERSIONS[UnitsLength.YARD][UnitsLength.DECIMETER] = \
        Mul(Rat(Mul(Int(36), Pow(10, 5)), Int(393701)), Sym("yd"))  # nopep8
    CONVERSIONS[UnitsLength.YARD][UnitsLength.EXAMETER] = \
        Mul(Rat(Int(9), Mul(Int(9842525), Pow(10, 12))), Sym("yd"))  # nopep8
    CONVERSIONS[UnitsLength.YARD][UnitsLength.FEMTOMETER] = \
        Mul(Rat(Mul(Int(36), Pow(10, 19)), Int(393701)), Sym("yd"))  # nopep8
    CONVERSIONS[UnitsLength.YARD][UnitsLength.FOOT] = \
        Mul(Int(3), Sym("yd"))  # nopep8
    CONVERSIONS[UnitsLength.YARD][UnitsLength.GIGAMETER] = \
        Mul(Rat(Int(9), Int("9842525000")), Sym("yd"))  # nopep8
    CONVERSIONS[UnitsLength.YARD][UnitsLength.HECTOMETER] = \
        Mul(Rat(Int(3600), Int(393701)), Sym("yd"))  # nopep8
    CONVERSIONS[UnitsLength.YARD][UnitsLength.INCH] = \
        Mul(Int(36), Sym("yd"))  # nopep8
    CONVERSIONS[UnitsLength.YARD][UnitsLength.KILOMETER] = \
        Mul(Rat(Int(360), Int(393701)), Sym("yd"))  # nopep8
    CONVERSIONS[UnitsLength.YARD][UnitsLength.LIGHTYEAR] = \
        Mul(Rat(Int(25), Int("258659656096217607")), Sym("yd"))  # nopep8
    CONVERSIONS[UnitsLength.YARD][UnitsLength.LINE] = \
        Mul(Int(432), Sym("yd"))  # nopep8
    CONVERSIONS[UnitsLength.YARD][UnitsLength.MEGAMETER] = \
        Mul(Rat(Int(9), Int(9842525)), Sym("yd"))  # nopep8
    CONVERSIONS[UnitsLength.YARD][UnitsLength.METER] = \
        Mul(Rat(Mul(Int(36), Pow(10, 4)), Int(393701)), Sym("yd"))  # nopep8
    CONVERSIONS[UnitsLength.YARD][UnitsLength.MICROMETER] = \
        Mul(Rat(Mul(Int(36), Pow(10, 10)), Int(393701)), Sym("yd"))  # nopep8
    CONVERSIONS[UnitsLength.YARD][UnitsLength.MILE] = \
        Mul(Rat(Int(1), Int(1760)), Sym("yd"))  # nopep8
    CONVERSIONS[UnitsLength.YARD][UnitsLength.MILLIMETER] = \
        Mul(Rat(Mul(Int(36), Pow(10, 7)), Int(393701)), Sym("yd"))  # nopep8
    CONVERSIONS[UnitsLength.YARD][UnitsLength.NANOMETER] = \
        Mul(Rat(Mul(Int(36), Pow(10, 13)), Int(393701)), Sym("yd"))  # nopep8
    CONVERSIONS[UnitsLength.YARD][UnitsLength.PARSEC] = \
        Mul(Rat(Int(3), Mul(Int("1012361963998"), Pow(10, 5))), Sym("yd"))  # nopep8
    CONVERSIONS[UnitsLength.YARD][UnitsLength.PETAMETER] = \
        Mul(Rat(Int(9), Mul(Int(9842525), Pow(10, 9))), Sym("yd"))  # nopep8
    CONVERSIONS[UnitsLength.YARD][UnitsLength.PICOMETER] = \
        Mul(Rat(Mul(Int(36), Pow(10, 16)), Int(393701)), Sym("yd"))  # nopep8
    CONVERSIONS[UnitsLength.YARD][UnitsLength.POINT] = \
        Mul(Int(2592), Sym("yd"))  # nopep8
    CONVERSIONS[UnitsLength.YARD][UnitsLength.TERAMETER] = \
        Mul(Rat(Int(9), Mul(Int(9842525), Pow(10, 6))), Sym("yd"))  # nopep8
    CONVERSIONS[UnitsLength.YARD][UnitsLength.THOU] = \
        Mul(Rat(Int(9), Int(250)), Sym("yd"))  # nopep8
    CONVERSIONS[UnitsLength.YARD][UnitsLength.YOCTOMETER] = \
        Mul(Rat(Mul(Int(36), Pow(10, 28)), Int(393701)), Sym("yd"))  # nopep8
    CONVERSIONS[UnitsLength.YARD][UnitsLength.YOTTAMETER] = \
        Mul(Rat(Int(9), Mul(Int(9842525), Pow(10, 18))), Sym("yd"))  # nopep8
    CONVERSIONS[UnitsLength.YARD][UnitsLength.ZEPTOMETER] = \
        Mul(Rat(Mul(Int(36), Pow(10, 25)), Int(393701)), Sym("yd"))  # nopep8
    CONVERSIONS[UnitsLength.YARD][UnitsLength.ZETTAMETER] = \
        Mul(Rat(Int(9), Mul(Int(9842525), Pow(10, 15))), Sym("yd"))  # nopep8
    CONVERSIONS[UnitsLength.YOCTOMETER][UnitsLength.ANGSTROM] = \
        Mul(Rat(Int(1), Pow(10, 14)), Sym("yoctom"))  # nopep8
    CONVERSIONS[UnitsLength.YOCTOMETER][UnitsLength.ASTRONOMICALUNIT] = \
        Mul(Rat(Int(1), Mul(Int(1495978707), Pow(10, 26))), Sym("yoctom"))  # nopep8
    CONVERSIONS[UnitsLength.YOCTOMETER][UnitsLength.ATTOMETER] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("yoctom"))  # nopep8
    CONVERSIONS[UnitsLength.YOCTOMETER][UnitsLength.CENTIMETER] = \
        Mul(Rat(Int(1), Pow(10, 22)), Sym("yoctom"))  # nopep8
    CONVERSIONS[UnitsLength.YOCTOMETER][UnitsLength.DECAMETER] = \
        Mul(Rat(Int(1), Pow(10, 25)), Sym("yoctom"))  # nopep8
    CONVERSIONS[UnitsLength.YOCTOMETER][UnitsLength.DECIMETER] = \
        Mul(Rat(Int(1), Pow(10, 23)), Sym("yoctom"))  # nopep8
    CONVERSIONS[UnitsLength.YOCTOMETER][UnitsLength.EXAMETER] = \
        Mul(Rat(Int(1), Pow(10, 42)), Sym("yoctom"))  # nopep8
    CONVERSIONS[UnitsLength.YOCTOMETER][UnitsLength.FEMTOMETER] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("yoctom"))  # nopep8
    CONVERSIONS[UnitsLength.YOCTOMETER][UnitsLength.FOOT] = \
        Mul(Rat(Int(393701), Mul(Int(12), Pow(10, 28))), Sym("yoctom"))  # nopep8
    CONVERSIONS[UnitsLength.YOCTOMETER][UnitsLength.GIGAMETER] = \
        Mul(Rat(Int(1), Pow(10, 33)), Sym("yoctom"))  # nopep8
    CONVERSIONS[UnitsLength.YOCTOMETER][UnitsLength.HECTOMETER] = \
        Mul(Rat(Int(1), Pow(10, 26)), Sym("yoctom"))  # nopep8
    CONVERSIONS[UnitsLength.YOCTOMETER][UnitsLength.INCH] = \
        Mul(Rat(Int(393701), Pow(10, 28)), Sym("yoctom"))  # nopep8
    CONVERSIONS[UnitsLength.YOCTOMETER][UnitsLength.KILOMETER] = \
        Mul(Rat(Int(1), Pow(10, 27)), Sym("yoctom"))  # nopep8
    CONVERSIONS[UnitsLength.YOCTOMETER][UnitsLength.LIGHTYEAR] = \
        Mul(Rat(Int(1), Mul(Int("94607304725808"), Pow(10, 26))), Sym("yoctom"))  # nopep8
    CONVERSIONS[UnitsLength.YOCTOMETER][UnitsLength.LINE] = \
        Mul(Rat(Int(1181103), Mul(Int(25), Pow(10, 26))), Sym("yoctom"))  # nopep8
    CONVERSIONS[UnitsLength.YOCTOMETER][UnitsLength.MEGAMETER] = \
        Mul(Rat(Int(1), Pow(10, 30)), Sym("yoctom"))  # nopep8
    CONVERSIONS[UnitsLength.YOCTOMETER][UnitsLength.METER] = \
        Mul(Rat(Int(1), Pow(10, 24)), Sym("yoctom"))  # nopep8
    CONVERSIONS[UnitsLength.YOCTOMETER][UnitsLength.MICROMETER] = \
        Mul(Rat(Int(1), Pow(10, 18)), Sym("yoctom"))  # nopep8
    CONVERSIONS[UnitsLength.YOCTOMETER][UnitsLength.MILE] = \
        Mul(Rat(Int(35791), Mul(Int(576), Pow(10, 29))), Sym("yoctom"))  # nopep8
    CONVERSIONS[UnitsLength.YOCTOMETER][UnitsLength.MILLIMETER] = \
        Mul(Rat(Int(1), Pow(10, 21)), Sym("yoctom"))  # nopep8
    CONVERSIONS[UnitsLength.YOCTOMETER][UnitsLength.NANOMETER] = \
        Mul(Rat(Int(1), Pow(10, 15)), Sym("yoctom"))  # nopep8
    CONVERSIONS[UnitsLength.YOCTOMETER][UnitsLength.PARSEC] = \
        Mul(Rat(Int(1), Mul(Int(30856776), Pow(10, 33))), Sym("yoctom"))  # nopep8
    CONVERSIONS[UnitsLength.YOCTOMETER][UnitsLength.PETAMETER] = \
        Mul(Rat(Int(1), Pow(10, 39)), Sym("yoctom"))  # nopep8
    CONVERSIONS[UnitsLength.YOCTOMETER][UnitsLength.PICOMETER] = \
        Mul(Rat(Int(1), Pow(10, 12)), Sym("yoctom"))  # nopep8
    CONVERSIONS[UnitsLength.YOCTOMETER][UnitsLength.POINT] = \
        Mul(Rat(Int(3543309), Mul(Int(125), Pow(10, 25))), Sym("yoctom"))  # nopep8
    CONVERSIONS[UnitsLength.YOCTOMETER][UnitsLength.TERAMETER] = \
        Mul(Rat(Int(1), Pow(10, 36)), Sym("yoctom"))  # nopep8
    CONVERSIONS[UnitsLength.YOCTOMETER][UnitsLength.THOU] = \
        Mul(Rat(Int(393701), Pow(10, 31)), Sym("yoctom"))  # nopep8
    CONVERSIONS[UnitsLength.YOCTOMETER][UnitsLength.YARD] = \
        Mul(Rat(Int(393701), Mul(Int(36), Pow(10, 28))), Sym("yoctom"))  # nopep8
    CONVERSIONS[UnitsLength.YOCTOMETER][UnitsLength.YOTTAMETER] = \
        Mul(Rat(Int(1), Pow(10, 48)), Sym("yoctom"))  # nopep8
    CONVERSIONS[UnitsLength.YOCTOMETER][UnitsLength.ZEPTOMETER] = \
        Mul(Rat(Int(1), Int(1000)), Sym("yoctom"))  # nopep8
    CONVERSIONS[UnitsLength.YOCTOMETER][UnitsLength.ZETTAMETER] = \
        Mul(Rat(Int(1), Pow(10, 45)), Sym("yoctom"))  # nopep8
    CONVERSIONS[UnitsLength.YOTTAMETER][UnitsLength.ANGSTROM] = \
        Mul(Pow(10, 34), Sym("yottam"))  # nopep8
    CONVERSIONS[UnitsLength.YOTTAMETER][UnitsLength.ASTRONOMICALUNIT] = \
        Mul(Rat(Pow(10, 22), Int(1495978707)), Sym("yottam"))  # nopep8
    CONVERSIONS[UnitsLength.YOTTAMETER][UnitsLength.ATTOMETER] = \
        Mul(Pow(10, 42), Sym("yottam"))  # nopep8
    CONVERSIONS[UnitsLength.YOTTAMETER][UnitsLength.CENTIMETER] = \
        Mul(Pow(10, 26), Sym("yottam"))  # nopep8
    CONVERSIONS[UnitsLength.YOTTAMETER][UnitsLength.DECAMETER] = \
        Mul(Pow(10, 23), Sym("yottam"))  # nopep8
    CONVERSIONS[UnitsLength.YOTTAMETER][UnitsLength.DECIMETER] = \
        Mul(Pow(10, 25), Sym("yottam"))  # nopep8
    CONVERSIONS[UnitsLength.YOTTAMETER][UnitsLength.EXAMETER] = \
        Mul(Pow(10, 6), Sym("yottam"))  # nopep8
    CONVERSIONS[UnitsLength.YOTTAMETER][UnitsLength.FEMTOMETER] = \
        Mul(Pow(10, 39), Sym("yottam"))  # nopep8
    CONVERSIONS[UnitsLength.YOTTAMETER][UnitsLength.FOOT] = \
        Mul(Rat(Mul(Int(9842525), Pow(10, 18)), Int(3)), Sym("yottam"))  # nopep8
    CONVERSIONS[UnitsLength.YOTTAMETER][UnitsLength.GIGAMETER] = \
        Mul(Pow(10, 15), Sym("yottam"))  # nopep8
    CONVERSIONS[UnitsLength.YOTTAMETER][UnitsLength.HECTOMETER] = \
        Mul(Pow(10, 22), Sym("yottam"))  # nopep8
    CONVERSIONS[UnitsLength.YOTTAMETER][UnitsLength.INCH] = \
        Mul(Mul(Int(393701), Pow(10, 20)), Sym("yottam"))  # nopep8
    CONVERSIONS[UnitsLength.YOTTAMETER][UnitsLength.KILOMETER] = \
        Mul(Pow(10, 21), Sym("yottam"))  # nopep8
    CONVERSIONS[UnitsLength.YOTTAMETER][UnitsLength.LIGHTYEAR] = \
        Mul(Rat(Mul(Int(625), Pow(10, 18)), Int("5912956545363")), Sym("yottam"))  # nopep8
    CONVERSIONS[UnitsLength.YOTTAMETER][UnitsLength.LINE] = \
        Mul(Mul(Int(4724412), Pow(10, 20)), Sym("yottam"))  # nopep8
    CONVERSIONS[UnitsLength.YOTTAMETER][UnitsLength.MEGAMETER] = \
        Mul(Pow(10, 18), Sym("yottam"))  # nopep8
    CONVERSIONS[UnitsLength.YOTTAMETER][UnitsLength.METER] = \
        Mul(Pow(10, 24), Sym("yottam"))  # nopep8
    CONVERSIONS[UnitsLength.YOTTAMETER][UnitsLength.MICROMETER] = \
        Mul(Pow(10, 30), Sym("yottam"))  # nopep8
    CONVERSIONS[UnitsLength.YOTTAMETER][UnitsLength.MILE] = \
        Mul(Rat(Mul(Int(559234375), Pow(10, 13)), Int(9)), Sym("yottam"))  # nopep8
    CONVERSIONS[UnitsLength.YOTTAMETER][UnitsLength.MILLIMETER] = \
        Mul(Pow(10, 27), Sym("yottam"))  # nopep8
    CONVERSIONS[UnitsLength.YOTTAMETER][UnitsLength.NANOMETER] = \
        Mul(Pow(10, 33), Sym("yottam"))  # nopep8
    CONVERSIONS[UnitsLength.YOTTAMETER][UnitsLength.PARSEC] = \
        Mul(Rat(Mul(Int(125), Pow(10, 12)), Int(3857097)), Sym("yottam"))  # nopep8
    CONVERSIONS[UnitsLength.YOTTAMETER][UnitsLength.PETAMETER] = \
        Mul(Pow(10, 9), Sym("yottam"))  # nopep8
    CONVERSIONS[UnitsLength.YOTTAMETER][UnitsLength.PICOMETER] = \
        Mul(Pow(10, 36), Sym("yottam"))  # nopep8
    CONVERSIONS[UnitsLength.YOTTAMETER][UnitsLength.POINT] = \
        Mul(Mul(Int(28346472), Pow(10, 20)), Sym("yottam"))  # nopep8
    CONVERSIONS[UnitsLength.YOTTAMETER][UnitsLength.TERAMETER] = \
        Mul(Pow(10, 12), Sym("yottam"))  # nopep8
    CONVERSIONS[UnitsLength.YOTTAMETER][UnitsLength.THOU] = \
        Mul(Mul(Int(393701), Pow(10, 17)), Sym("yottam"))  # nopep8
    CONVERSIONS[UnitsLength.YOTTAMETER][UnitsLength.YARD] = \
        Mul(Rat(Mul(Int(9842525), Pow(10, 18)), Int(9)), Sym("yottam"))  # nopep8
    CONVERSIONS[UnitsLength.YOTTAMETER][UnitsLength.YOCTOMETER] = \
        Mul(Pow(10, 48), Sym("yottam"))  # nopep8
    CONVERSIONS[UnitsLength.YOTTAMETER][UnitsLength.ZEPTOMETER] = \
        Mul(Pow(10, 45), Sym("yottam"))  # nopep8
    CONVERSIONS[UnitsLength.YOTTAMETER][UnitsLength.ZETTAMETER] = \
        Mul(Int(1000), Sym("yottam"))  # nopep8
    CONVERSIONS[UnitsLength.ZEPTOMETER][UnitsLength.ANGSTROM] = \
        Mul(Rat(Int(1), Pow(10, 11)), Sym("zeptom"))  # nopep8
    CONVERSIONS[UnitsLength.ZEPTOMETER][UnitsLength.ASTRONOMICALUNIT] = \
        Mul(Rat(Int(1), Mul(Int(1495978707), Pow(10, 23))), Sym("zeptom"))  # nopep8
    CONVERSIONS[UnitsLength.ZEPTOMETER][UnitsLength.ATTOMETER] = \
        Mul(Rat(Int(1), Int(1000)), Sym("zeptom"))  # nopep8
    CONVERSIONS[UnitsLength.ZEPTOMETER][UnitsLength.CENTIMETER] = \
        Mul(Rat(Int(1), Pow(10, 19)), Sym("zeptom"))  # nopep8
    CONVERSIONS[UnitsLength.ZEPTOMETER][UnitsLength.DECAMETER] = \
        Mul(Rat(Int(1), Pow(10, 22)), Sym("zeptom"))  # nopep8
    CONVERSIONS[UnitsLength.ZEPTOMETER][UnitsLength.DECIMETER] = \
        Mul(Rat(Int(1), Pow(10, 20)), Sym("zeptom"))  # nopep8
    CONVERSIONS[UnitsLength.ZEPTOMETER][UnitsLength.EXAMETER] = \
        Mul(Rat(Int(1), Pow(10, 39)), Sym("zeptom"))  # nopep8
    CONVERSIONS[UnitsLength.ZEPTOMETER][UnitsLength.FEMTOMETER] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("zeptom"))  # nopep8
    CONVERSIONS[UnitsLength.ZEPTOMETER][UnitsLength.FOOT] = \
        Mul(Rat(Int(393701), Mul(Int(12), Pow(10, 25))), Sym("zeptom"))  # nopep8
    CONVERSIONS[UnitsLength.ZEPTOMETER][UnitsLength.GIGAMETER] = \
        Mul(Rat(Int(1), Pow(10, 30)), Sym("zeptom"))  # nopep8
    CONVERSIONS[UnitsLength.ZEPTOMETER][UnitsLength.HECTOMETER] = \
        Mul(Rat(Int(1), Pow(10, 23)), Sym("zeptom"))  # nopep8
    CONVERSIONS[UnitsLength.ZEPTOMETER][UnitsLength.INCH] = \
        Mul(Rat(Int(393701), Pow(10, 25)), Sym("zeptom"))  # nopep8
    CONVERSIONS[UnitsLength.ZEPTOMETER][UnitsLength.KILOMETER] = \
        Mul(Rat(Int(1), Pow(10, 24)), Sym("zeptom"))  # nopep8
    CONVERSIONS[UnitsLength.ZEPTOMETER][UnitsLength.LIGHTYEAR] = \
        Mul(Rat(Int(1), Mul(Int("94607304725808"), Pow(10, 23))), Sym("zeptom"))  # nopep8
    CONVERSIONS[UnitsLength.ZEPTOMETER][UnitsLength.LINE] = \
        Mul(Rat(Int(1181103), Mul(Int(25), Pow(10, 23))), Sym("zeptom"))  # nopep8
    CONVERSIONS[UnitsLength.ZEPTOMETER][UnitsLength.MEGAMETER] = \
        Mul(Rat(Int(1), Pow(10, 27)), Sym("zeptom"))  # nopep8
    CONVERSIONS[UnitsLength.ZEPTOMETER][UnitsLength.METER] = \
        Mul(Rat(Int(1), Pow(10, 21)), Sym("zeptom"))  # nopep8
    CONVERSIONS[UnitsLength.ZEPTOMETER][UnitsLength.MICROMETER] = \
        Mul(Rat(Int(1), Pow(10, 15)), Sym("zeptom"))  # nopep8
    CONVERSIONS[UnitsLength.ZEPTOMETER][UnitsLength.MILE] = \
        Mul(Rat(Int(35791), Mul(Int(576), Pow(10, 26))), Sym("zeptom"))  # nopep8
    CONVERSIONS[UnitsLength.ZEPTOMETER][UnitsLength.MILLIMETER] = \
        Mul(Rat(Int(1), Pow(10, 18)), Sym("zeptom"))  # nopep8
    CONVERSIONS[UnitsLength.ZEPTOMETER][UnitsLength.NANOMETER] = \
        Mul(Rat(Int(1), Pow(10, 12)), Sym("zeptom"))  # nopep8
    CONVERSIONS[UnitsLength.ZEPTOMETER][UnitsLength.PARSEC] = \
        Mul(Rat(Int(1), Mul(Int(30856776), Pow(10, 30))), Sym("zeptom"))  # nopep8
    CONVERSIONS[UnitsLength.ZEPTOMETER][UnitsLength.PETAMETER] = \
        Mul(Rat(Int(1), Pow(10, 36)), Sym("zeptom"))  # nopep8
    CONVERSIONS[UnitsLength.ZEPTOMETER][UnitsLength.PICOMETER] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("zeptom"))  # nopep8
    CONVERSIONS[UnitsLength.ZEPTOMETER][UnitsLength.POINT] = \
        Mul(Rat(Int(3543309), Mul(Int(125), Pow(10, 22))), Sym("zeptom"))  # nopep8
    CONVERSIONS[UnitsLength.ZEPTOMETER][UnitsLength.TERAMETER] = \
        Mul(Rat(Int(1), Pow(10, 33)), Sym("zeptom"))  # nopep8
    CONVERSIONS[UnitsLength.ZEPTOMETER][UnitsLength.THOU] = \
        Mul(Rat(Int(393701), Pow(10, 28)), Sym("zeptom"))  # nopep8
    CONVERSIONS[UnitsLength.ZEPTOMETER][UnitsLength.YARD] = \
        Mul(Rat(Int(393701), Mul(Int(36), Pow(10, 25))), Sym("zeptom"))  # nopep8
    CONVERSIONS[UnitsLength.ZEPTOMETER][UnitsLength.YOCTOMETER] = \
        Mul(Int(1000), Sym("zeptom"))  # nopep8
    CONVERSIONS[UnitsLength.ZEPTOMETER][UnitsLength.YOTTAMETER] = \
        Mul(Rat(Int(1), Pow(10, 45)), Sym("zeptom"))  # nopep8
    CONVERSIONS[UnitsLength.ZEPTOMETER][UnitsLength.ZETTAMETER] = \
        Mul(Rat(Int(1), Pow(10, 42)), Sym("zeptom"))  # nopep8
    CONVERSIONS[UnitsLength.ZETTAMETER][UnitsLength.ANGSTROM] = \
        Mul(Pow(10, 31), Sym("zettam"))  # nopep8
    CONVERSIONS[UnitsLength.ZETTAMETER][UnitsLength.ASTRONOMICALUNIT] = \
        Mul(Rat(Pow(10, 19), Int(1495978707)), Sym("zettam"))  # nopep8
    CONVERSIONS[UnitsLength.ZETTAMETER][UnitsLength.ATTOMETER] = \
        Mul(Pow(10, 39), Sym("zettam"))  # nopep8
    CONVERSIONS[UnitsLength.ZETTAMETER][UnitsLength.CENTIMETER] = \
        Mul(Pow(10, 23), Sym("zettam"))  # nopep8
    CONVERSIONS[UnitsLength.ZETTAMETER][UnitsLength.DECAMETER] = \
        Mul(Pow(10, 20), Sym("zettam"))  # nopep8
    CONVERSIONS[UnitsLength.ZETTAMETER][UnitsLength.DECIMETER] = \
        Mul(Pow(10, 22), Sym("zettam"))  # nopep8
    CONVERSIONS[UnitsLength.ZETTAMETER][UnitsLength.EXAMETER] = \
        Mul(Int(1000), Sym("zettam"))  # nopep8
    CONVERSIONS[UnitsLength.ZETTAMETER][UnitsLength.FEMTOMETER] = \
        Mul(Pow(10, 36), Sym("zettam"))  # nopep8
    CONVERSIONS[UnitsLength.ZETTAMETER][UnitsLength.FOOT] = \
        Mul(Rat(Mul(Int(9842525), Pow(10, 15)), Int(3)), Sym("zettam"))  # nopep8
    CONVERSIONS[UnitsLength.ZETTAMETER][UnitsLength.GIGAMETER] = \
        Mul(Pow(10, 12), Sym("zettam"))  # nopep8
    CONVERSIONS[UnitsLength.ZETTAMETER][UnitsLength.HECTOMETER] = \
        Mul(Pow(10, 19), Sym("zettam"))  # nopep8
    CONVERSIONS[UnitsLength.ZETTAMETER][UnitsLength.INCH] = \
        Mul(Mul(Int(393701), Pow(10, 17)), Sym("zettam"))  # nopep8
    CONVERSIONS[UnitsLength.ZETTAMETER][UnitsLength.KILOMETER] = \
        Mul(Pow(10, 18), Sym("zettam"))  # nopep8
    CONVERSIONS[UnitsLength.ZETTAMETER][UnitsLength.LIGHTYEAR] = \
        Mul(Rat(Mul(Int(625), Pow(10, 15)), Int("5912956545363")), Sym("zettam"))  # nopep8
    CONVERSIONS[UnitsLength.ZETTAMETER][UnitsLength.LINE] = \
        Mul(Mul(Int(4724412), Pow(10, 17)), Sym("zettam"))  # nopep8
    CONVERSIONS[UnitsLength.ZETTAMETER][UnitsLength.MEGAMETER] = \
        Mul(Pow(10, 15), Sym("zettam"))  # nopep8
    CONVERSIONS[UnitsLength.ZETTAMETER][UnitsLength.METER] = \
        Mul(Pow(10, 21), Sym("zettam"))  # nopep8
    CONVERSIONS[UnitsLength.ZETTAMETER][UnitsLength.MICROMETER] = \
        Mul(Pow(10, 27), Sym("zettam"))  # nopep8
    CONVERSIONS[UnitsLength.ZETTAMETER][UnitsLength.MILE] = \
        Mul(Rat(Mul(Int(559234375), Pow(10, 10)), Int(9)), Sym("zettam"))  # nopep8
    CONVERSIONS[UnitsLength.ZETTAMETER][UnitsLength.MILLIMETER] = \
        Mul(Pow(10, 24), Sym("zettam"))  # nopep8
    CONVERSIONS[UnitsLength.ZETTAMETER][UnitsLength.NANOMETER] = \
        Mul(Pow(10, 30), Sym("zettam"))  # nopep8
    CONVERSIONS[UnitsLength.ZETTAMETER][UnitsLength.PARSEC] = \
        Mul(Rat(Mul(Int(125), Pow(10, 9)), Int(3857097)), Sym("zettam"))  # nopep8
    CONVERSIONS[UnitsLength.ZETTAMETER][UnitsLength.PETAMETER] = \
        Mul(Pow(10, 6), Sym("zettam"))  # nopep8
    CONVERSIONS[UnitsLength.ZETTAMETER][UnitsLength.PICOMETER] = \
        Mul(Pow(10, 33), Sym("zettam"))  # nopep8
    CONVERSIONS[UnitsLength.ZETTAMETER][UnitsLength.POINT] = \
        Mul(Mul(Int(28346472), Pow(10, 17)), Sym("zettam"))  # nopep8
    CONVERSIONS[UnitsLength.ZETTAMETER][UnitsLength.TERAMETER] = \
        Mul(Pow(10, 9), Sym("zettam"))  # nopep8
    CONVERSIONS[UnitsLength.ZETTAMETER][UnitsLength.THOU] = \
        Mul(Mul(Int(393701), Pow(10, 14)), Sym("zettam"))  # nopep8
    CONVERSIONS[UnitsLength.ZETTAMETER][UnitsLength.YARD] = \
        Mul(Rat(Mul(Int(9842525), Pow(10, 15)), Int(9)), Sym("zettam"))  # nopep8
    CONVERSIONS[UnitsLength.ZETTAMETER][UnitsLength.YOCTOMETER] = \
        Mul(Pow(10, 45), Sym("zettam"))  # nopep8
    CONVERSIONS[UnitsLength.ZETTAMETER][UnitsLength.YOTTAMETER] = \
        Mul(Rat(Int(1), Int(1000)), Sym("zettam"))  # nopep8
    CONVERSIONS[UnitsLength.ZETTAMETER][UnitsLength.ZEPTOMETER] = \
        Mul(Pow(10, 42), Sym("zettam"))  # nopep8

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

        if unit is None:
            target = None
        elif isinstance(unit, UnitsLength):
            target = unit
        elif isinstance(unit, (str, unicode)):
            target = getattr(UnitsLength, unit)
        else:
            raise Exception("Unknown unit: %s (%s)" % (
                unit, type(unit)
            ))

        if isinstance(value, _omero_model.LengthI):
            # This is a copy-constructor call.

            source = value.getUnit()

            if target is None:
                raise Exception("Null target unit")
            if source is None:
                raise Exception("Null source unit")

            if target == source:
                self.setValue(value.getValue())
                self.setUnit(source)
            else:
                c = self.CONVERSIONS.get(source).get(target)
                if c is None:
                    t = (value.getValue(), source, target)
                    msg = "%s %s cannot be converted to %s" % t
                    raise Exception(msg)
                self.setValue(c(value.getValue()))
                self.setUnit(target)
        else:
            self.setValue(value)
            self.setUnit(target)

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
