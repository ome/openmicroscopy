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
        Mul(Mul(Int(1495978707), Pow(10, 12)), Sym("ang"))  # nopep8
    CONVERSIONS[UnitsLength.ANGSTROM][UnitsLength.ATTOMETER] = \
        Mul(Rat(Int(1), Pow(10, 8)), Sym("ang"))  # nopep8
    CONVERSIONS[UnitsLength.ANGSTROM][UnitsLength.CENTIMETER] = \
        Mul(Pow(10, 8), Sym("ang"))  # nopep8
    CONVERSIONS[UnitsLength.ANGSTROM][UnitsLength.DECAMETER] = \
        Mul(Pow(10, 11), Sym("ang"))  # nopep8
    CONVERSIONS[UnitsLength.ANGSTROM][UnitsLength.DECIMETER] = \
        Mul(Pow(10, 9), Sym("ang"))  # nopep8
    CONVERSIONS[UnitsLength.ANGSTROM][UnitsLength.EXAMETER] = \
        Mul(Pow(10, 28), Sym("ang"))  # nopep8
    CONVERSIONS[UnitsLength.ANGSTROM][UnitsLength.FEMTOMETER] = \
        Mul(Rat(Int(1), Pow(10, 5)), Sym("ang"))  # nopep8
    CONVERSIONS[UnitsLength.ANGSTROM][UnitsLength.FOOT] = \
        Mul(Mul(Int(3048), Pow(10, 6)), Sym("ang"))  # nopep8
    CONVERSIONS[UnitsLength.ANGSTROM][UnitsLength.GIGAMETER] = \
        Mul(Pow(10, 19), Sym("ang"))  # nopep8
    CONVERSIONS[UnitsLength.ANGSTROM][UnitsLength.HECTOMETER] = \
        Mul(Pow(10, 12), Sym("ang"))  # nopep8
    CONVERSIONS[UnitsLength.ANGSTROM][UnitsLength.INCH] = \
        Mul(Mul(Int(254), Pow(10, 6)), Sym("ang"))  # nopep8
    CONVERSIONS[UnitsLength.ANGSTROM][UnitsLength.KILOMETER] = \
        Mul(Pow(10, 13), Sym("ang"))  # nopep8
    CONVERSIONS[UnitsLength.ANGSTROM][UnitsLength.LIGHTYEAR] = \
        Mul(Mul(Int("94607304725808"), Pow(10, 12)), Sym("ang"))  # nopep8
    CONVERSIONS[UnitsLength.ANGSTROM][UnitsLength.LINE] = \
        Mul(Rat(Mul(Int(635), Pow(10, 5)), Int(3)), Sym("ang"))  # nopep8
    CONVERSIONS[UnitsLength.ANGSTROM][UnitsLength.MEGAMETER] = \
        Mul(Pow(10, 16), Sym("ang"))  # nopep8
    CONVERSIONS[UnitsLength.ANGSTROM][UnitsLength.METER] = \
        Mul(Pow(10, 10), Sym("ang"))  # nopep8
    CONVERSIONS[UnitsLength.ANGSTROM][UnitsLength.MICROMETER] = \
        Mul(Pow(10, 4), Sym("ang"))  # nopep8
    CONVERSIONS[UnitsLength.ANGSTROM][UnitsLength.MILE] = \
        Mul(Mul(Int(1609344), Pow(10, 7)), Sym("ang"))  # nopep8
    CONVERSIONS[UnitsLength.ANGSTROM][UnitsLength.MILLIMETER] = \
        Mul(Pow(10, 7), Sym("ang"))  # nopep8
    CONVERSIONS[UnitsLength.ANGSTROM][UnitsLength.NANOMETER] = \
        Mul(Int(10), Sym("ang"))  # nopep8
    CONVERSIONS[UnitsLength.ANGSTROM][UnitsLength.PARSEC] = \
        Mul(Mul(Int(30856776), Pow(10, 19)), Sym("ang"))  # nopep8
    CONVERSIONS[UnitsLength.ANGSTROM][UnitsLength.PETAMETER] = \
        Mul(Pow(10, 25), Sym("ang"))  # nopep8
    CONVERSIONS[UnitsLength.ANGSTROM][UnitsLength.PICOMETER] = \
        Mul(Rat(Int(1), Int(100)), Sym("ang"))  # nopep8
    CONVERSIONS[UnitsLength.ANGSTROM][UnitsLength.POINT] = \
        Mul(Rat(Mul(Int(3175), Pow(10, 4)), Int(9)), Sym("ang"))  # nopep8
    CONVERSIONS[UnitsLength.ANGSTROM][UnitsLength.TERAMETER] = \
        Mul(Pow(10, 22), Sym("ang"))  # nopep8
    CONVERSIONS[UnitsLength.ANGSTROM][UnitsLength.THOU] = \
        Mul(Int(254000), Sym("ang"))  # nopep8
    CONVERSIONS[UnitsLength.ANGSTROM][UnitsLength.YARD] = \
        Mul(Mul(Int(9144), Pow(10, 6)), Sym("ang"))  # nopep8
    CONVERSIONS[UnitsLength.ANGSTROM][UnitsLength.YOCTOMETER] = \
        Mul(Rat(Int(1), Pow(10, 14)), Sym("ang"))  # nopep8
    CONVERSIONS[UnitsLength.ANGSTROM][UnitsLength.YOTTAMETER] = \
        Mul(Pow(10, 34), Sym("ang"))  # nopep8
    CONVERSIONS[UnitsLength.ANGSTROM][UnitsLength.ZEPTOMETER] = \
        Mul(Rat(Int(1), Pow(10, 11)), Sym("ang"))  # nopep8
    CONVERSIONS[UnitsLength.ANGSTROM][UnitsLength.ZETTAMETER] = \
        Mul(Pow(10, 31), Sym("ang"))  # nopep8
    CONVERSIONS[UnitsLength.ASTRONOMICALUNIT][UnitsLength.ANGSTROM] = \
        Mul(Rat(Int(1), Mul(Int(1495978707), Pow(10, 12))), Sym("ua"))  # nopep8
    CONVERSIONS[UnitsLength.ASTRONOMICALUNIT][UnitsLength.ATTOMETER] = \
        Mul(Rat(Int(1), Mul(Int(1495978707), Pow(10, 20))), Sym("ua"))  # nopep8
    CONVERSIONS[UnitsLength.ASTRONOMICALUNIT][UnitsLength.CENTIMETER] = \
        Mul(Rat(Int(1), Mul(Int(1495978707), Pow(10, 4))), Sym("ua"))  # nopep8
    CONVERSIONS[UnitsLength.ASTRONOMICALUNIT][UnitsLength.DECAMETER] = \
        Mul(Rat(Int(1), Int("14959787070")), Sym("ua"))  # nopep8
    CONVERSIONS[UnitsLength.ASTRONOMICALUNIT][UnitsLength.DECIMETER] = \
        Mul(Rat(Int(1), Int("1495978707000")), Sym("ua"))  # nopep8
    CONVERSIONS[UnitsLength.ASTRONOMICALUNIT][UnitsLength.EXAMETER] = \
        Mul(Rat(Pow(10, 16), Int(1495978707)), Sym("ua"))  # nopep8
    CONVERSIONS[UnitsLength.ASTRONOMICALUNIT][UnitsLength.FEMTOMETER] = \
        Mul(Rat(Int(1), Mul(Int(1495978707), Pow(10, 17))), Sym("ua"))  # nopep8
    CONVERSIONS[UnitsLength.ASTRONOMICALUNIT][UnitsLength.FOOT] = \
        Mul(Rat(Int(127), Int("62332446125000")), Sym("ua"))  # nopep8
    CONVERSIONS[UnitsLength.ASTRONOMICALUNIT][UnitsLength.GIGAMETER] = \
        Mul(Rat(Pow(10, 7), Int(1495978707)), Sym("ua"))  # nopep8
    CONVERSIONS[UnitsLength.ASTRONOMICALUNIT][UnitsLength.HECTOMETER] = \
        Mul(Rat(Int(1), Int(1495978707)), Sym("ua"))  # nopep8
    CONVERSIONS[UnitsLength.ASTRONOMICALUNIT][UnitsLength.INCH] = \
        Mul(Rat(Int(127), Mul(Int("7479893535"), Pow(10, 5))), Sym("ua"))  # nopep8
    CONVERSIONS[UnitsLength.ASTRONOMICALUNIT][UnitsLength.KILOMETER] = \
        Mul(Rat(Int(10), Int(1495978707)), Sym("ua"))  # nopep8
    CONVERSIONS[UnitsLength.ASTRONOMICALUNIT][UnitsLength.LIGHTYEAR] = \
        Mul(Rat(Int("431996825232"), Int(6830953)), Sym("ua"))  # nopep8
    CONVERSIONS[UnitsLength.ASTRONOMICALUNIT][UnitsLength.LINE] = \
        Mul(Rat(Int(127), Mul(Int("8975872242"), Pow(10, 6))), Sym("ua"))  # nopep8
    CONVERSIONS[UnitsLength.ASTRONOMICALUNIT][UnitsLength.MEGAMETER] = \
        Mul(Rat(Pow(10, 4), Int(1495978707)), Sym("ua"))  # nopep8
    CONVERSIONS[UnitsLength.ASTRONOMICALUNIT][UnitsLength.METER] = \
        Mul(Rat(Int(1), Int("149597870700")), Sym("ua"))  # nopep8
    CONVERSIONS[UnitsLength.ASTRONOMICALUNIT][UnitsLength.MICROMETER] = \
        Mul(Rat(Int(1), Mul(Int(1495978707), Pow(10, 8))), Sym("ua"))  # nopep8
    CONVERSIONS[UnitsLength.ASTRONOMICALUNIT][UnitsLength.MILE] = \
        Mul(Rat(Int(16764), Int("1558311153125")), Sym("ua"))  # nopep8
    CONVERSIONS[UnitsLength.ASTRONOMICALUNIT][UnitsLength.MILLIMETER] = \
        Mul(Rat(Int(1), Mul(Int(1495978707), Pow(10, 5))), Sym("ua"))  # nopep8
    CONVERSIONS[UnitsLength.ASTRONOMICALUNIT][UnitsLength.NANOMETER] = \
        Mul(Rat(Int(1), Mul(Int(1495978707), Pow(10, 11))), Sym("ua"))  # nopep8
    CONVERSIONS[UnitsLength.ASTRONOMICALUNIT][UnitsLength.PARSEC] = \
        Mul(Rat(Mul(Int(10285592), Pow(10, 7)), Int(498659569)), Sym("ua"))  # nopep8
    CONVERSIONS[UnitsLength.ASTRONOMICALUNIT][UnitsLength.PETAMETER] = \
        Mul(Rat(Pow(10, 13), Int(1495978707)), Sym("ua"))  # nopep8
    CONVERSIONS[UnitsLength.ASTRONOMICALUNIT][UnitsLength.PICOMETER] = \
        Mul(Rat(Int(1), Mul(Int(1495978707), Pow(10, 14))), Sym("ua"))  # nopep8
    CONVERSIONS[UnitsLength.ASTRONOMICALUNIT][UnitsLength.POINT] = \
        Mul(Rat(Int(127), Mul(Int("53855233452"), Pow(10, 6))), Sym("ua"))  # nopep8
    CONVERSIONS[UnitsLength.ASTRONOMICALUNIT][UnitsLength.TERAMETER] = \
        Mul(Rat(Pow(10, 10), Int(1495978707)), Sym("ua"))  # nopep8
    CONVERSIONS[UnitsLength.ASTRONOMICALUNIT][UnitsLength.THOU] = \
        Mul(Rat(Int(127), Mul(Int("7479893535"), Pow(10, 8))), Sym("ua"))  # nopep8
    CONVERSIONS[UnitsLength.ASTRONOMICALUNIT][UnitsLength.YARD] = \
        Mul(Rat(Int(381), Int("62332446125000")), Sym("ua"))  # nopep8
    CONVERSIONS[UnitsLength.ASTRONOMICALUNIT][UnitsLength.YOCTOMETER] = \
        Mul(Rat(Int(1), Mul(Int(1495978707), Pow(10, 26))), Sym("ua"))  # nopep8
    CONVERSIONS[UnitsLength.ASTRONOMICALUNIT][UnitsLength.YOTTAMETER] = \
        Mul(Rat(Pow(10, 22), Int(1495978707)), Sym("ua"))  # nopep8
    CONVERSIONS[UnitsLength.ASTRONOMICALUNIT][UnitsLength.ZEPTOMETER] = \
        Mul(Rat(Int(1), Mul(Int(1495978707), Pow(10, 23))), Sym("ua"))  # nopep8
    CONVERSIONS[UnitsLength.ASTRONOMICALUNIT][UnitsLength.ZETTAMETER] = \
        Mul(Rat(Pow(10, 19), Int(1495978707)), Sym("ua"))  # nopep8
    CONVERSIONS[UnitsLength.ATTOMETER][UnitsLength.ANGSTROM] = \
        Mul(Pow(10, 8), Sym("attom"))  # nopep8
    CONVERSIONS[UnitsLength.ATTOMETER][UnitsLength.ASTRONOMICALUNIT] = \
        Mul(Mul(Int(1495978707), Pow(10, 20)), Sym("attom"))  # nopep8
    CONVERSIONS[UnitsLength.ATTOMETER][UnitsLength.CENTIMETER] = \
        Mul(Pow(10, 16), Sym("attom"))  # nopep8
    CONVERSIONS[UnitsLength.ATTOMETER][UnitsLength.DECAMETER] = \
        Mul(Pow(10, 19), Sym("attom"))  # nopep8
    CONVERSIONS[UnitsLength.ATTOMETER][UnitsLength.DECIMETER] = \
        Mul(Pow(10, 17), Sym("attom"))  # nopep8
    CONVERSIONS[UnitsLength.ATTOMETER][UnitsLength.EXAMETER] = \
        Mul(Pow(10, 36), Sym("attom"))  # nopep8
    CONVERSIONS[UnitsLength.ATTOMETER][UnitsLength.FEMTOMETER] = \
        Mul(Int(1000), Sym("attom"))  # nopep8
    CONVERSIONS[UnitsLength.ATTOMETER][UnitsLength.FOOT] = \
        Mul(Mul(Int(3048), Pow(10, 14)), Sym("attom"))  # nopep8
    CONVERSIONS[UnitsLength.ATTOMETER][UnitsLength.GIGAMETER] = \
        Mul(Pow(10, 27), Sym("attom"))  # nopep8
    CONVERSIONS[UnitsLength.ATTOMETER][UnitsLength.HECTOMETER] = \
        Mul(Pow(10, 20), Sym("attom"))  # nopep8
    CONVERSIONS[UnitsLength.ATTOMETER][UnitsLength.INCH] = \
        Mul(Mul(Int(254), Pow(10, 14)), Sym("attom"))  # nopep8
    CONVERSIONS[UnitsLength.ATTOMETER][UnitsLength.KILOMETER] = \
        Mul(Pow(10, 21), Sym("attom"))  # nopep8
    CONVERSIONS[UnitsLength.ATTOMETER][UnitsLength.LIGHTYEAR] = \
        Mul(Mul(Int("94607304725808"), Pow(10, 20)), Sym("attom"))  # nopep8
    CONVERSIONS[UnitsLength.ATTOMETER][UnitsLength.LINE] = \
        Mul(Rat(Mul(Int(635), Pow(10, 13)), Int(3)), Sym("attom"))  # nopep8
    CONVERSIONS[UnitsLength.ATTOMETER][UnitsLength.MEGAMETER] = \
        Mul(Pow(10, 24), Sym("attom"))  # nopep8
    CONVERSIONS[UnitsLength.ATTOMETER][UnitsLength.METER] = \
        Mul(Pow(10, 18), Sym("attom"))  # nopep8
    CONVERSIONS[UnitsLength.ATTOMETER][UnitsLength.MICROMETER] = \
        Mul(Pow(10, 12), Sym("attom"))  # nopep8
    CONVERSIONS[UnitsLength.ATTOMETER][UnitsLength.MILE] = \
        Mul(Mul(Int(1609344), Pow(10, 15)), Sym("attom"))  # nopep8
    CONVERSIONS[UnitsLength.ATTOMETER][UnitsLength.MILLIMETER] = \
        Mul(Pow(10, 15), Sym("attom"))  # nopep8
    CONVERSIONS[UnitsLength.ATTOMETER][UnitsLength.NANOMETER] = \
        Mul(Pow(10, 9), Sym("attom"))  # nopep8
    CONVERSIONS[UnitsLength.ATTOMETER][UnitsLength.PARSEC] = \
        Mul(Mul(Int(30856776), Pow(10, 27)), Sym("attom"))  # nopep8
    CONVERSIONS[UnitsLength.ATTOMETER][UnitsLength.PETAMETER] = \
        Mul(Pow(10, 33), Sym("attom"))  # nopep8
    CONVERSIONS[UnitsLength.ATTOMETER][UnitsLength.PICOMETER] = \
        Mul(Pow(10, 6), Sym("attom"))  # nopep8
    CONVERSIONS[UnitsLength.ATTOMETER][UnitsLength.POINT] = \
        Mul(Rat(Mul(Int(3175), Pow(10, 12)), Int(9)), Sym("attom"))  # nopep8
    CONVERSIONS[UnitsLength.ATTOMETER][UnitsLength.TERAMETER] = \
        Mul(Pow(10, 30), Sym("attom"))  # nopep8
    CONVERSIONS[UnitsLength.ATTOMETER][UnitsLength.THOU] = \
        Mul(Mul(Int(254), Pow(10, 11)), Sym("attom"))  # nopep8
    CONVERSIONS[UnitsLength.ATTOMETER][UnitsLength.YARD] = \
        Mul(Mul(Int(9144), Pow(10, 14)), Sym("attom"))  # nopep8
    CONVERSIONS[UnitsLength.ATTOMETER][UnitsLength.YOCTOMETER] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("attom"))  # nopep8
    CONVERSIONS[UnitsLength.ATTOMETER][UnitsLength.YOTTAMETER] = \
        Mul(Pow(10, 42), Sym("attom"))  # nopep8
    CONVERSIONS[UnitsLength.ATTOMETER][UnitsLength.ZEPTOMETER] = \
        Mul(Rat(Int(1), Int(1000)), Sym("attom"))  # nopep8
    CONVERSIONS[UnitsLength.ATTOMETER][UnitsLength.ZETTAMETER] = \
        Mul(Pow(10, 39), Sym("attom"))  # nopep8
    CONVERSIONS[UnitsLength.CENTIMETER][UnitsLength.ANGSTROM] = \
        Mul(Rat(Int(1), Pow(10, 8)), Sym("centim"))  # nopep8
    CONVERSIONS[UnitsLength.CENTIMETER][UnitsLength.ASTRONOMICALUNIT] = \
        Mul(Mul(Int(1495978707), Pow(10, 4)), Sym("centim"))  # nopep8
    CONVERSIONS[UnitsLength.CENTIMETER][UnitsLength.ATTOMETER] = \
        Mul(Rat(Int(1), Pow(10, 16)), Sym("centim"))  # nopep8
    CONVERSIONS[UnitsLength.CENTIMETER][UnitsLength.DECAMETER] = \
        Mul(Int(1000), Sym("centim"))  # nopep8
    CONVERSIONS[UnitsLength.CENTIMETER][UnitsLength.DECIMETER] = \
        Mul(Int(10), Sym("centim"))  # nopep8
    CONVERSIONS[UnitsLength.CENTIMETER][UnitsLength.EXAMETER] = \
        Mul(Pow(10, 20), Sym("centim"))  # nopep8
    CONVERSIONS[UnitsLength.CENTIMETER][UnitsLength.FEMTOMETER] = \
        Mul(Rat(Int(1), Pow(10, 13)), Sym("centim"))  # nopep8
    CONVERSIONS[UnitsLength.CENTIMETER][UnitsLength.FOOT] = \
        Mul(Rat(Int(762), Int(25)), Sym("centim"))  # nopep8
    CONVERSIONS[UnitsLength.CENTIMETER][UnitsLength.GIGAMETER] = \
        Mul(Pow(10, 11), Sym("centim"))  # nopep8
    CONVERSIONS[UnitsLength.CENTIMETER][UnitsLength.HECTOMETER] = \
        Mul(Pow(10, 4), Sym("centim"))  # nopep8
    CONVERSIONS[UnitsLength.CENTIMETER][UnitsLength.INCH] = \
        Mul(Rat(Int(127), Int(50)), Sym("centim"))  # nopep8
    CONVERSIONS[UnitsLength.CENTIMETER][UnitsLength.KILOMETER] = \
        Mul(Pow(10, 5), Sym("centim"))  # nopep8
    CONVERSIONS[UnitsLength.CENTIMETER][UnitsLength.LIGHTYEAR] = \
        Mul(Mul(Int("94607304725808"), Pow(10, 4)), Sym("centim"))  # nopep8
    CONVERSIONS[UnitsLength.CENTIMETER][UnitsLength.LINE] = \
        Mul(Rat(Int(127), Int(600)), Sym("centim"))  # nopep8
    CONVERSIONS[UnitsLength.CENTIMETER][UnitsLength.MEGAMETER] = \
        Mul(Pow(10, 8), Sym("centim"))  # nopep8
    CONVERSIONS[UnitsLength.CENTIMETER][UnitsLength.METER] = \
        Mul(Int(100), Sym("centim"))  # nopep8
    CONVERSIONS[UnitsLength.CENTIMETER][UnitsLength.MICROMETER] = \
        Mul(Rat(Int(1), Pow(10, 4)), Sym("centim"))  # nopep8
    CONVERSIONS[UnitsLength.CENTIMETER][UnitsLength.MILE] = \
        Mul(Rat(Int(804672), Int(5)), Sym("centim"))  # nopep8
    CONVERSIONS[UnitsLength.CENTIMETER][UnitsLength.MILLIMETER] = \
        Mul(Rat(Int(1), Int(10)), Sym("centim"))  # nopep8
    CONVERSIONS[UnitsLength.CENTIMETER][UnitsLength.NANOMETER] = \
        Mul(Rat(Int(1), Pow(10, 7)), Sym("centim"))  # nopep8
    CONVERSIONS[UnitsLength.CENTIMETER][UnitsLength.PARSEC] = \
        Mul(Mul(Int(30856776), Pow(10, 11)), Sym("centim"))  # nopep8
    CONVERSIONS[UnitsLength.CENTIMETER][UnitsLength.PETAMETER] = \
        Mul(Pow(10, 17), Sym("centim"))  # nopep8
    CONVERSIONS[UnitsLength.CENTIMETER][UnitsLength.PICOMETER] = \
        Mul(Rat(Int(1), Pow(10, 10)), Sym("centim"))  # nopep8
    CONVERSIONS[UnitsLength.CENTIMETER][UnitsLength.POINT] = \
        Mul(Rat(Int(127), Int(3600)), Sym("centim"))  # nopep8
    CONVERSIONS[UnitsLength.CENTIMETER][UnitsLength.TERAMETER] = \
        Mul(Pow(10, 14), Sym("centim"))  # nopep8
    CONVERSIONS[UnitsLength.CENTIMETER][UnitsLength.THOU] = \
        Mul(Rat(Int(127), Mul(Int(5), Pow(10, 4))), Sym("centim"))  # nopep8
    CONVERSIONS[UnitsLength.CENTIMETER][UnitsLength.YARD] = \
        Mul(Rat(Int(2286), Int(25)), Sym("centim"))  # nopep8
    CONVERSIONS[UnitsLength.CENTIMETER][UnitsLength.YOCTOMETER] = \
        Mul(Rat(Int(1), Pow(10, 22)), Sym("centim"))  # nopep8
    CONVERSIONS[UnitsLength.CENTIMETER][UnitsLength.YOTTAMETER] = \
        Mul(Pow(10, 26), Sym("centim"))  # nopep8
    CONVERSIONS[UnitsLength.CENTIMETER][UnitsLength.ZEPTOMETER] = \
        Mul(Rat(Int(1), Pow(10, 19)), Sym("centim"))  # nopep8
    CONVERSIONS[UnitsLength.CENTIMETER][UnitsLength.ZETTAMETER] = \
        Mul(Pow(10, 23), Sym("centim"))  # nopep8
    CONVERSIONS[UnitsLength.DECAMETER][UnitsLength.ANGSTROM] = \
        Mul(Rat(Int(1), Pow(10, 11)), Sym("decam"))  # nopep8
    CONVERSIONS[UnitsLength.DECAMETER][UnitsLength.ASTRONOMICALUNIT] = \
        Mul(Int("14959787070"), Sym("decam"))  # nopep8
    CONVERSIONS[UnitsLength.DECAMETER][UnitsLength.ATTOMETER] = \
        Mul(Rat(Int(1), Pow(10, 19)), Sym("decam"))  # nopep8
    CONVERSIONS[UnitsLength.DECAMETER][UnitsLength.CENTIMETER] = \
        Mul(Rat(Int(1), Int(1000)), Sym("decam"))  # nopep8
    CONVERSIONS[UnitsLength.DECAMETER][UnitsLength.DECIMETER] = \
        Mul(Rat(Int(1), Int(100)), Sym("decam"))  # nopep8
    CONVERSIONS[UnitsLength.DECAMETER][UnitsLength.EXAMETER] = \
        Mul(Pow(10, 17), Sym("decam"))  # nopep8
    CONVERSIONS[UnitsLength.DECAMETER][UnitsLength.FEMTOMETER] = \
        Mul(Rat(Int(1), Pow(10, 16)), Sym("decam"))  # nopep8
    CONVERSIONS[UnitsLength.DECAMETER][UnitsLength.FOOT] = \
        Mul(Rat(Int(381), Int(12500)), Sym("decam"))  # nopep8
    CONVERSIONS[UnitsLength.DECAMETER][UnitsLength.GIGAMETER] = \
        Mul(Pow(10, 8), Sym("decam"))  # nopep8
    CONVERSIONS[UnitsLength.DECAMETER][UnitsLength.HECTOMETER] = \
        Mul(Int(10), Sym("decam"))  # nopep8
    CONVERSIONS[UnitsLength.DECAMETER][UnitsLength.INCH] = \
        Mul(Rat(Int(127), Mul(Int(5), Pow(10, 4))), Sym("decam"))  # nopep8
    CONVERSIONS[UnitsLength.DECAMETER][UnitsLength.KILOMETER] = \
        Mul(Int(100), Sym("decam"))  # nopep8
    CONVERSIONS[UnitsLength.DECAMETER][UnitsLength.LIGHTYEAR] = \
        Mul(Int("946073047258080"), Sym("decam"))  # nopep8
    CONVERSIONS[UnitsLength.DECAMETER][UnitsLength.LINE] = \
        Mul(Rat(Int(127), Mul(Int(6), Pow(10, 5))), Sym("decam"))  # nopep8
    CONVERSIONS[UnitsLength.DECAMETER][UnitsLength.MEGAMETER] = \
        Mul(Pow(10, 5), Sym("decam"))  # nopep8
    CONVERSIONS[UnitsLength.DECAMETER][UnitsLength.METER] = \
        Mul(Rat(Int(1), Int(10)), Sym("decam"))  # nopep8
    CONVERSIONS[UnitsLength.DECAMETER][UnitsLength.MICROMETER] = \
        Mul(Rat(Int(1), Pow(10, 7)), Sym("decam"))  # nopep8
    CONVERSIONS[UnitsLength.DECAMETER][UnitsLength.MILE] = \
        Mul(Rat(Int(100584), Int(625)), Sym("decam"))  # nopep8
    CONVERSIONS[UnitsLength.DECAMETER][UnitsLength.MILLIMETER] = \
        Mul(Rat(Int(1), Pow(10, 4)), Sym("decam"))  # nopep8
    CONVERSIONS[UnitsLength.DECAMETER][UnitsLength.NANOMETER] = \
        Mul(Rat(Int(1), Pow(10, 10)), Sym("decam"))  # nopep8
    CONVERSIONS[UnitsLength.DECAMETER][UnitsLength.PARSEC] = \
        Mul(Mul(Int(30856776), Pow(10, 8)), Sym("decam"))  # nopep8
    CONVERSIONS[UnitsLength.DECAMETER][UnitsLength.PETAMETER] = \
        Mul(Pow(10, 14), Sym("decam"))  # nopep8
    CONVERSIONS[UnitsLength.DECAMETER][UnitsLength.PICOMETER] = \
        Mul(Rat(Int(1), Pow(10, 13)), Sym("decam"))  # nopep8
    CONVERSIONS[UnitsLength.DECAMETER][UnitsLength.POINT] = \
        Mul(Rat(Int(127), Mul(Int(36), Pow(10, 5))), Sym("decam"))  # nopep8
    CONVERSIONS[UnitsLength.DECAMETER][UnitsLength.TERAMETER] = \
        Mul(Pow(10, 11), Sym("decam"))  # nopep8
    CONVERSIONS[UnitsLength.DECAMETER][UnitsLength.THOU] = \
        Mul(Rat(Int(127), Mul(Int(5), Pow(10, 7))), Sym("decam"))  # nopep8
    CONVERSIONS[UnitsLength.DECAMETER][UnitsLength.YARD] = \
        Mul(Rat(Int(1143), Int(12500)), Sym("decam"))  # nopep8
    CONVERSIONS[UnitsLength.DECAMETER][UnitsLength.YOCTOMETER] = \
        Mul(Rat(Int(1), Pow(10, 25)), Sym("decam"))  # nopep8
    CONVERSIONS[UnitsLength.DECAMETER][UnitsLength.YOTTAMETER] = \
        Mul(Pow(10, 23), Sym("decam"))  # nopep8
    CONVERSIONS[UnitsLength.DECAMETER][UnitsLength.ZEPTOMETER] = \
        Mul(Rat(Int(1), Pow(10, 22)), Sym("decam"))  # nopep8
    CONVERSIONS[UnitsLength.DECAMETER][UnitsLength.ZETTAMETER] = \
        Mul(Pow(10, 20), Sym("decam"))  # nopep8
    CONVERSIONS[UnitsLength.DECIMETER][UnitsLength.ANGSTROM] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("decim"))  # nopep8
    CONVERSIONS[UnitsLength.DECIMETER][UnitsLength.ASTRONOMICALUNIT] = \
        Mul(Int("1495978707000"), Sym("decim"))  # nopep8
    CONVERSIONS[UnitsLength.DECIMETER][UnitsLength.ATTOMETER] = \
        Mul(Rat(Int(1), Pow(10, 17)), Sym("decim"))  # nopep8
    CONVERSIONS[UnitsLength.DECIMETER][UnitsLength.CENTIMETER] = \
        Mul(Rat(Int(1), Int(10)), Sym("decim"))  # nopep8
    CONVERSIONS[UnitsLength.DECIMETER][UnitsLength.DECAMETER] = \
        Mul(Int(100), Sym("decim"))  # nopep8
    CONVERSIONS[UnitsLength.DECIMETER][UnitsLength.EXAMETER] = \
        Mul(Pow(10, 19), Sym("decim"))  # nopep8
    CONVERSIONS[UnitsLength.DECIMETER][UnitsLength.FEMTOMETER] = \
        Mul(Rat(Int(1), Pow(10, 14)), Sym("decim"))  # nopep8
    CONVERSIONS[UnitsLength.DECIMETER][UnitsLength.FOOT] = \
        Mul(Rat(Int(381), Int(125)), Sym("decim"))  # nopep8
    CONVERSIONS[UnitsLength.DECIMETER][UnitsLength.GIGAMETER] = \
        Mul(Pow(10, 10), Sym("decim"))  # nopep8
    CONVERSIONS[UnitsLength.DECIMETER][UnitsLength.HECTOMETER] = \
        Mul(Int(1000), Sym("decim"))  # nopep8
    CONVERSIONS[UnitsLength.DECIMETER][UnitsLength.INCH] = \
        Mul(Rat(Int(127), Int(500)), Sym("decim"))  # nopep8
    CONVERSIONS[UnitsLength.DECIMETER][UnitsLength.KILOMETER] = \
        Mul(Pow(10, 4), Sym("decim"))  # nopep8
    CONVERSIONS[UnitsLength.DECIMETER][UnitsLength.LIGHTYEAR] = \
        Mul(Int("94607304725808000"), Sym("decim"))  # nopep8
    CONVERSIONS[UnitsLength.DECIMETER][UnitsLength.LINE] = \
        Mul(Rat(Int(127), Int(6000)), Sym("decim"))  # nopep8
    CONVERSIONS[UnitsLength.DECIMETER][UnitsLength.MEGAMETER] = \
        Mul(Pow(10, 7), Sym("decim"))  # nopep8
    CONVERSIONS[UnitsLength.DECIMETER][UnitsLength.METER] = \
        Mul(Int(10), Sym("decim"))  # nopep8
    CONVERSIONS[UnitsLength.DECIMETER][UnitsLength.MICROMETER] = \
        Mul(Rat(Int(1), Pow(10, 5)), Sym("decim"))  # nopep8
    CONVERSIONS[UnitsLength.DECIMETER][UnitsLength.MILE] = \
        Mul(Rat(Int(402336), Int(25)), Sym("decim"))  # nopep8
    CONVERSIONS[UnitsLength.DECIMETER][UnitsLength.MILLIMETER] = \
        Mul(Rat(Int(1), Int(100)), Sym("decim"))  # nopep8
    CONVERSIONS[UnitsLength.DECIMETER][UnitsLength.NANOMETER] = \
        Mul(Rat(Int(1), Pow(10, 8)), Sym("decim"))  # nopep8
    CONVERSIONS[UnitsLength.DECIMETER][UnitsLength.PARSEC] = \
        Mul(Mul(Int(30856776), Pow(10, 10)), Sym("decim"))  # nopep8
    CONVERSIONS[UnitsLength.DECIMETER][UnitsLength.PETAMETER] = \
        Mul(Pow(10, 16), Sym("decim"))  # nopep8
    CONVERSIONS[UnitsLength.DECIMETER][UnitsLength.PICOMETER] = \
        Mul(Rat(Int(1), Pow(10, 11)), Sym("decim"))  # nopep8
    CONVERSIONS[UnitsLength.DECIMETER][UnitsLength.POINT] = \
        Mul(Rat(Int(127), Int(36000)), Sym("decim"))  # nopep8
    CONVERSIONS[UnitsLength.DECIMETER][UnitsLength.TERAMETER] = \
        Mul(Pow(10, 13), Sym("decim"))  # nopep8
    CONVERSIONS[UnitsLength.DECIMETER][UnitsLength.THOU] = \
        Mul(Rat(Int(127), Mul(Int(5), Pow(10, 5))), Sym("decim"))  # nopep8
    CONVERSIONS[UnitsLength.DECIMETER][UnitsLength.YARD] = \
        Mul(Rat(Int(1143), Int(125)), Sym("decim"))  # nopep8
    CONVERSIONS[UnitsLength.DECIMETER][UnitsLength.YOCTOMETER] = \
        Mul(Rat(Int(1), Pow(10, 23)), Sym("decim"))  # nopep8
    CONVERSIONS[UnitsLength.DECIMETER][UnitsLength.YOTTAMETER] = \
        Mul(Pow(10, 25), Sym("decim"))  # nopep8
    CONVERSIONS[UnitsLength.DECIMETER][UnitsLength.ZEPTOMETER] = \
        Mul(Rat(Int(1), Pow(10, 20)), Sym("decim"))  # nopep8
    CONVERSIONS[UnitsLength.DECIMETER][UnitsLength.ZETTAMETER] = \
        Mul(Pow(10, 22), Sym("decim"))  # nopep8
    CONVERSIONS[UnitsLength.EXAMETER][UnitsLength.ANGSTROM] = \
        Mul(Rat(Int(1), Pow(10, 28)), Sym("exam"))  # nopep8
    CONVERSIONS[UnitsLength.EXAMETER][UnitsLength.ASTRONOMICALUNIT] = \
        Mul(Rat(Int(1495978707), Pow(10, 16)), Sym("exam"))  # nopep8
    CONVERSIONS[UnitsLength.EXAMETER][UnitsLength.ATTOMETER] = \
        Mul(Rat(Int(1), Pow(10, 36)), Sym("exam"))  # nopep8
    CONVERSIONS[UnitsLength.EXAMETER][UnitsLength.CENTIMETER] = \
        Mul(Rat(Int(1), Pow(10, 20)), Sym("exam"))  # nopep8
    CONVERSIONS[UnitsLength.EXAMETER][UnitsLength.DECAMETER] = \
        Mul(Rat(Int(1), Pow(10, 17)), Sym("exam"))  # nopep8
    CONVERSIONS[UnitsLength.EXAMETER][UnitsLength.DECIMETER] = \
        Mul(Rat(Int(1), Pow(10, 19)), Sym("exam"))  # nopep8
    CONVERSIONS[UnitsLength.EXAMETER][UnitsLength.FEMTOMETER] = \
        Mul(Rat(Int(1), Pow(10, 33)), Sym("exam"))  # nopep8
    CONVERSIONS[UnitsLength.EXAMETER][UnitsLength.FOOT] = \
        Mul(Rat(Int(381), Mul(Int(125), Pow(10, 19))), Sym("exam"))  # nopep8
    CONVERSIONS[UnitsLength.EXAMETER][UnitsLength.GIGAMETER] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("exam"))  # nopep8
    CONVERSIONS[UnitsLength.EXAMETER][UnitsLength.HECTOMETER] = \
        Mul(Rat(Int(1), Pow(10, 16)), Sym("exam"))  # nopep8
    CONVERSIONS[UnitsLength.EXAMETER][UnitsLength.INCH] = \
        Mul(Rat(Int(127), Mul(Int(5), Pow(10, 21))), Sym("exam"))  # nopep8
    CONVERSIONS[UnitsLength.EXAMETER][UnitsLength.KILOMETER] = \
        Mul(Rat(Int(1), Pow(10, 15)), Sym("exam"))  # nopep8
    CONVERSIONS[UnitsLength.EXAMETER][UnitsLength.LIGHTYEAR] = \
        Mul(Rat(Int("5912956545363"), Mul(Int(625), Pow(10, 12))), Sym("exam"))  # nopep8
    CONVERSIONS[UnitsLength.EXAMETER][UnitsLength.LINE] = \
        Mul(Rat(Int(127), Mul(Int(6), Pow(10, 22))), Sym("exam"))  # nopep8
    CONVERSIONS[UnitsLength.EXAMETER][UnitsLength.MEGAMETER] = \
        Mul(Rat(Int(1), Pow(10, 12)), Sym("exam"))  # nopep8
    CONVERSIONS[UnitsLength.EXAMETER][UnitsLength.METER] = \
        Mul(Rat(Int(1), Pow(10, 18)), Sym("exam"))  # nopep8
    CONVERSIONS[UnitsLength.EXAMETER][UnitsLength.MICROMETER] = \
        Mul(Rat(Int(1), Pow(10, 24)), Sym("exam"))  # nopep8
    CONVERSIONS[UnitsLength.EXAMETER][UnitsLength.MILE] = \
        Mul(Rat(Int(12573), Mul(Int(78125), Pow(10, 14))), Sym("exam"))  # nopep8
    CONVERSIONS[UnitsLength.EXAMETER][UnitsLength.MILLIMETER] = \
        Mul(Rat(Int(1), Pow(10, 21)), Sym("exam"))  # nopep8
    CONVERSIONS[UnitsLength.EXAMETER][UnitsLength.NANOMETER] = \
        Mul(Rat(Int(1), Pow(10, 27)), Sym("exam"))  # nopep8
    CONVERSIONS[UnitsLength.EXAMETER][UnitsLength.PARSEC] = \
        Mul(Rat(Int(3857097), Mul(Int(125), Pow(10, 6))), Sym("exam"))  # nopep8
    CONVERSIONS[UnitsLength.EXAMETER][UnitsLength.PETAMETER] = \
        Mul(Rat(Int(1), Int(1000)), Sym("exam"))  # nopep8
    CONVERSIONS[UnitsLength.EXAMETER][UnitsLength.PICOMETER] = \
        Mul(Rat(Int(1), Pow(10, 30)), Sym("exam"))  # nopep8
    CONVERSIONS[UnitsLength.EXAMETER][UnitsLength.POINT] = \
        Mul(Rat(Int(127), Mul(Int(36), Pow(10, 22))), Sym("exam"))  # nopep8
    CONVERSIONS[UnitsLength.EXAMETER][UnitsLength.TERAMETER] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("exam"))  # nopep8
    CONVERSIONS[UnitsLength.EXAMETER][UnitsLength.THOU] = \
        Mul(Rat(Int(127), Mul(Int(5), Pow(10, 24))), Sym("exam"))  # nopep8
    CONVERSIONS[UnitsLength.EXAMETER][UnitsLength.YARD] = \
        Mul(Rat(Int(1143), Mul(Int(125), Pow(10, 19))), Sym("exam"))  # nopep8
    CONVERSIONS[UnitsLength.EXAMETER][UnitsLength.YOCTOMETER] = \
        Mul(Rat(Int(1), Pow(10, 42)), Sym("exam"))  # nopep8
    CONVERSIONS[UnitsLength.EXAMETER][UnitsLength.YOTTAMETER] = \
        Mul(Pow(10, 6), Sym("exam"))  # nopep8
    CONVERSIONS[UnitsLength.EXAMETER][UnitsLength.ZEPTOMETER] = \
        Mul(Rat(Int(1), Pow(10, 39)), Sym("exam"))  # nopep8
    CONVERSIONS[UnitsLength.EXAMETER][UnitsLength.ZETTAMETER] = \
        Mul(Int(1000), Sym("exam"))  # nopep8
    CONVERSIONS[UnitsLength.FEMTOMETER][UnitsLength.ANGSTROM] = \
        Mul(Pow(10, 5), Sym("femtom"))  # nopep8
    CONVERSIONS[UnitsLength.FEMTOMETER][UnitsLength.ASTRONOMICALUNIT] = \
        Mul(Mul(Int(1495978707), Pow(10, 17)), Sym("femtom"))  # nopep8
    CONVERSIONS[UnitsLength.FEMTOMETER][UnitsLength.ATTOMETER] = \
        Mul(Rat(Int(1), Int(1000)), Sym("femtom"))  # nopep8
    CONVERSIONS[UnitsLength.FEMTOMETER][UnitsLength.CENTIMETER] = \
        Mul(Pow(10, 13), Sym("femtom"))  # nopep8
    CONVERSIONS[UnitsLength.FEMTOMETER][UnitsLength.DECAMETER] = \
        Mul(Pow(10, 16), Sym("femtom"))  # nopep8
    CONVERSIONS[UnitsLength.FEMTOMETER][UnitsLength.DECIMETER] = \
        Mul(Pow(10, 14), Sym("femtom"))  # nopep8
    CONVERSIONS[UnitsLength.FEMTOMETER][UnitsLength.EXAMETER] = \
        Mul(Pow(10, 33), Sym("femtom"))  # nopep8
    CONVERSIONS[UnitsLength.FEMTOMETER][UnitsLength.FOOT] = \
        Mul(Mul(Int(3048), Pow(10, 11)), Sym("femtom"))  # nopep8
    CONVERSIONS[UnitsLength.FEMTOMETER][UnitsLength.GIGAMETER] = \
        Mul(Pow(10, 24), Sym("femtom"))  # nopep8
    CONVERSIONS[UnitsLength.FEMTOMETER][UnitsLength.HECTOMETER] = \
        Mul(Pow(10, 17), Sym("femtom"))  # nopep8
    CONVERSIONS[UnitsLength.FEMTOMETER][UnitsLength.INCH] = \
        Mul(Mul(Int(254), Pow(10, 11)), Sym("femtom"))  # nopep8
    CONVERSIONS[UnitsLength.FEMTOMETER][UnitsLength.KILOMETER] = \
        Mul(Pow(10, 18), Sym("femtom"))  # nopep8
    CONVERSIONS[UnitsLength.FEMTOMETER][UnitsLength.LIGHTYEAR] = \
        Mul(Mul(Int("94607304725808"), Pow(10, 17)), Sym("femtom"))  # nopep8
    CONVERSIONS[UnitsLength.FEMTOMETER][UnitsLength.LINE] = \
        Mul(Rat(Mul(Int(635), Pow(10, 10)), Int(3)), Sym("femtom"))  # nopep8
    CONVERSIONS[UnitsLength.FEMTOMETER][UnitsLength.MEGAMETER] = \
        Mul(Pow(10, 21), Sym("femtom"))  # nopep8
    CONVERSIONS[UnitsLength.FEMTOMETER][UnitsLength.METER] = \
        Mul(Pow(10, 15), Sym("femtom"))  # nopep8
    CONVERSIONS[UnitsLength.FEMTOMETER][UnitsLength.MICROMETER] = \
        Mul(Pow(10, 9), Sym("femtom"))  # nopep8
    CONVERSIONS[UnitsLength.FEMTOMETER][UnitsLength.MILE] = \
        Mul(Mul(Int(1609344), Pow(10, 12)), Sym("femtom"))  # nopep8
    CONVERSIONS[UnitsLength.FEMTOMETER][UnitsLength.MILLIMETER] = \
        Mul(Pow(10, 12), Sym("femtom"))  # nopep8
    CONVERSIONS[UnitsLength.FEMTOMETER][UnitsLength.NANOMETER] = \
        Mul(Pow(10, 6), Sym("femtom"))  # nopep8
    CONVERSIONS[UnitsLength.FEMTOMETER][UnitsLength.PARSEC] = \
        Mul(Mul(Int(30856776), Pow(10, 24)), Sym("femtom"))  # nopep8
    CONVERSIONS[UnitsLength.FEMTOMETER][UnitsLength.PETAMETER] = \
        Mul(Pow(10, 30), Sym("femtom"))  # nopep8
    CONVERSIONS[UnitsLength.FEMTOMETER][UnitsLength.PICOMETER] = \
        Mul(Int(1000), Sym("femtom"))  # nopep8
    CONVERSIONS[UnitsLength.FEMTOMETER][UnitsLength.POINT] = \
        Mul(Rat(Mul(Int(3175), Pow(10, 9)), Int(9)), Sym("femtom"))  # nopep8
    CONVERSIONS[UnitsLength.FEMTOMETER][UnitsLength.TERAMETER] = \
        Mul(Pow(10, 27), Sym("femtom"))  # nopep8
    CONVERSIONS[UnitsLength.FEMTOMETER][UnitsLength.THOU] = \
        Mul(Mul(Int(254), Pow(10, 8)), Sym("femtom"))  # nopep8
    CONVERSIONS[UnitsLength.FEMTOMETER][UnitsLength.YARD] = \
        Mul(Mul(Int(9144), Pow(10, 11)), Sym("femtom"))  # nopep8
    CONVERSIONS[UnitsLength.FEMTOMETER][UnitsLength.YOCTOMETER] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("femtom"))  # nopep8
    CONVERSIONS[UnitsLength.FEMTOMETER][UnitsLength.YOTTAMETER] = \
        Mul(Pow(10, 39), Sym("femtom"))  # nopep8
    CONVERSIONS[UnitsLength.FEMTOMETER][UnitsLength.ZEPTOMETER] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("femtom"))  # nopep8
    CONVERSIONS[UnitsLength.FEMTOMETER][UnitsLength.ZETTAMETER] = \
        Mul(Pow(10, 36), Sym("femtom"))  # nopep8
    CONVERSIONS[UnitsLength.FOOT][UnitsLength.ANGSTROM] = \
        Mul(Rat(Int(1), Mul(Int(3048), Pow(10, 6))), Sym("ft"))  # nopep8
    CONVERSIONS[UnitsLength.FOOT][UnitsLength.ASTRONOMICALUNIT] = \
        Mul(Rat(Int("62332446125000"), Int(127)), Sym("ft"))  # nopep8
    CONVERSIONS[UnitsLength.FOOT][UnitsLength.ATTOMETER] = \
        Mul(Rat(Int(1), Mul(Int(3048), Pow(10, 14))), Sym("ft"))  # nopep8
    CONVERSIONS[UnitsLength.FOOT][UnitsLength.CENTIMETER] = \
        Mul(Rat(Int(25), Int(762)), Sym("ft"))  # nopep8
    CONVERSIONS[UnitsLength.FOOT][UnitsLength.DECAMETER] = \
        Mul(Rat(Int(12500), Int(381)), Sym("ft"))  # nopep8
    CONVERSIONS[UnitsLength.FOOT][UnitsLength.DECIMETER] = \
        Mul(Rat(Int(125), Int(381)), Sym("ft"))  # nopep8
    CONVERSIONS[UnitsLength.FOOT][UnitsLength.EXAMETER] = \
        Mul(Rat(Mul(Int(125), Pow(10, 19)), Int(381)), Sym("ft"))  # nopep8
    CONVERSIONS[UnitsLength.FOOT][UnitsLength.FEMTOMETER] = \
        Mul(Rat(Int(1), Mul(Int(3048), Pow(10, 11))), Sym("ft"))  # nopep8
    CONVERSIONS[UnitsLength.FOOT][UnitsLength.GIGAMETER] = \
        Mul(Rat(Mul(Int(125), Pow(10, 10)), Int(381)), Sym("ft"))  # nopep8
    CONVERSIONS[UnitsLength.FOOT][UnitsLength.HECTOMETER] = \
        Mul(Rat(Int(125000), Int(381)), Sym("ft"))  # nopep8
    CONVERSIONS[UnitsLength.FOOT][UnitsLength.INCH] = \
        Mul(Rat(Int(1), Int(12)), Sym("ft"))  # nopep8
    CONVERSIONS[UnitsLength.FOOT][UnitsLength.KILOMETER] = \
        Mul(Rat(Mul(Int(125), Pow(10, 4)), Int(381)), Sym("ft"))  # nopep8
    CONVERSIONS[UnitsLength.FOOT][UnitsLength.LIGHTYEAR] = \
        Mul(Rat(Mul(Int("3941971030242"), Pow(10, 6)), Int(127)), Sym("ft"))  # nopep8
    CONVERSIONS[UnitsLength.FOOT][UnitsLength.LINE] = \
        Mul(Rat(Int(1), Int(144)), Sym("ft"))  # nopep8
    CONVERSIONS[UnitsLength.FOOT][UnitsLength.MEGAMETER] = \
        Mul(Rat(Mul(Int(125), Pow(10, 7)), Int(381)), Sym("ft"))  # nopep8
    CONVERSIONS[UnitsLength.FOOT][UnitsLength.METER] = \
        Mul(Rat(Int(1250), Int(381)), Sym("ft"))  # nopep8
    CONVERSIONS[UnitsLength.FOOT][UnitsLength.MICROMETER] = \
        Mul(Rat(Int(1), Int(304800)), Sym("ft"))  # nopep8
    CONVERSIONS[UnitsLength.FOOT][UnitsLength.MILE] = \
        Mul(Int(5280), Sym("ft"))  # nopep8
    CONVERSIONS[UnitsLength.FOOT][UnitsLength.MILLIMETER] = \
        Mul(Rat(Int(5), Int(1524)), Sym("ft"))  # nopep8
    CONVERSIONS[UnitsLength.FOOT][UnitsLength.NANOMETER] = \
        Mul(Rat(Int(1), Mul(Int(3048), Pow(10, 5))), Sym("ft"))  # nopep8
    CONVERSIONS[UnitsLength.FOOT][UnitsLength.PARSEC] = \
        Mul(Rat(Mul(Int(1285699), Pow(10, 13)), Int(127)), Sym("ft"))  # nopep8
    CONVERSIONS[UnitsLength.FOOT][UnitsLength.PETAMETER] = \
        Mul(Rat(Mul(Int(125), Pow(10, 16)), Int(381)), Sym("ft"))  # nopep8
    CONVERSIONS[UnitsLength.FOOT][UnitsLength.PICOMETER] = \
        Mul(Rat(Int(1), Mul(Int(3048), Pow(10, 8))), Sym("ft"))  # nopep8
    CONVERSIONS[UnitsLength.FOOT][UnitsLength.POINT] = \
        Mul(Rat(Int(1), Int(864)), Sym("ft"))  # nopep8
    CONVERSIONS[UnitsLength.FOOT][UnitsLength.TERAMETER] = \
        Mul(Rat(Mul(Int(125), Pow(10, 13)), Int(381)), Sym("ft"))  # nopep8
    CONVERSIONS[UnitsLength.FOOT][UnitsLength.THOU] = \
        Mul(Rat(Int(1), Int(12000)), Sym("ft"))  # nopep8
    CONVERSIONS[UnitsLength.FOOT][UnitsLength.YARD] = \
        Mul(Int(3), Sym("ft"))  # nopep8
    CONVERSIONS[UnitsLength.FOOT][UnitsLength.YOCTOMETER] = \
        Mul(Rat(Int(1), Mul(Int(3048), Pow(10, 20))), Sym("ft"))  # nopep8
    CONVERSIONS[UnitsLength.FOOT][UnitsLength.YOTTAMETER] = \
        Mul(Rat(Mul(Int(125), Pow(10, 25)), Int(381)), Sym("ft"))  # nopep8
    CONVERSIONS[UnitsLength.FOOT][UnitsLength.ZEPTOMETER] = \
        Mul(Rat(Int(1), Mul(Int(3048), Pow(10, 17))), Sym("ft"))  # nopep8
    CONVERSIONS[UnitsLength.FOOT][UnitsLength.ZETTAMETER] = \
        Mul(Rat(Mul(Int(125), Pow(10, 22)), Int(381)), Sym("ft"))  # nopep8
    CONVERSIONS[UnitsLength.GIGAMETER][UnitsLength.ANGSTROM] = \
        Mul(Rat(Int(1), Pow(10, 19)), Sym("gigam"))  # nopep8
    CONVERSIONS[UnitsLength.GIGAMETER][UnitsLength.ASTRONOMICALUNIT] = \
        Mul(Rat(Int(1495978707), Pow(10, 7)), Sym("gigam"))  # nopep8
    CONVERSIONS[UnitsLength.GIGAMETER][UnitsLength.ATTOMETER] = \
        Mul(Rat(Int(1), Pow(10, 27)), Sym("gigam"))  # nopep8
    CONVERSIONS[UnitsLength.GIGAMETER][UnitsLength.CENTIMETER] = \
        Mul(Rat(Int(1), Pow(10, 11)), Sym("gigam"))  # nopep8
    CONVERSIONS[UnitsLength.GIGAMETER][UnitsLength.DECAMETER] = \
        Mul(Rat(Int(1), Pow(10, 8)), Sym("gigam"))  # nopep8
    CONVERSIONS[UnitsLength.GIGAMETER][UnitsLength.DECIMETER] = \
        Mul(Rat(Int(1), Pow(10, 10)), Sym("gigam"))  # nopep8
    CONVERSIONS[UnitsLength.GIGAMETER][UnitsLength.EXAMETER] = \
        Mul(Pow(10, 9), Sym("gigam"))  # nopep8
    CONVERSIONS[UnitsLength.GIGAMETER][UnitsLength.FEMTOMETER] = \
        Mul(Rat(Int(1), Pow(10, 24)), Sym("gigam"))  # nopep8
    CONVERSIONS[UnitsLength.GIGAMETER][UnitsLength.FOOT] = \
        Mul(Rat(Int(381), Mul(Int(125), Pow(10, 10))), Sym("gigam"))  # nopep8
    CONVERSIONS[UnitsLength.GIGAMETER][UnitsLength.HECTOMETER] = \
        Mul(Rat(Int(1), Pow(10, 7)), Sym("gigam"))  # nopep8
    CONVERSIONS[UnitsLength.GIGAMETER][UnitsLength.INCH] = \
        Mul(Rat(Int(127), Mul(Int(5), Pow(10, 12))), Sym("gigam"))  # nopep8
    CONVERSIONS[UnitsLength.GIGAMETER][UnitsLength.KILOMETER] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("gigam"))  # nopep8
    CONVERSIONS[UnitsLength.GIGAMETER][UnitsLength.LIGHTYEAR] = \
        Mul(Rat(Int("5912956545363"), Int(625000)), Sym("gigam"))  # nopep8
    CONVERSIONS[UnitsLength.GIGAMETER][UnitsLength.LINE] = \
        Mul(Rat(Int(127), Mul(Int(6), Pow(10, 13))), Sym("gigam"))  # nopep8
    CONVERSIONS[UnitsLength.GIGAMETER][UnitsLength.MEGAMETER] = \
        Mul(Rat(Int(1), Int(1000)), Sym("gigam"))  # nopep8
    CONVERSIONS[UnitsLength.GIGAMETER][UnitsLength.METER] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("gigam"))  # nopep8
    CONVERSIONS[UnitsLength.GIGAMETER][UnitsLength.MICROMETER] = \
        Mul(Rat(Int(1), Pow(10, 15)), Sym("gigam"))  # nopep8
    CONVERSIONS[UnitsLength.GIGAMETER][UnitsLength.MILE] = \
        Mul(Rat(Int(12573), Mul(Int(78125), Pow(10, 5))), Sym("gigam"))  # nopep8
    CONVERSIONS[UnitsLength.GIGAMETER][UnitsLength.MILLIMETER] = \
        Mul(Rat(Int(1), Pow(10, 12)), Sym("gigam"))  # nopep8
    CONVERSIONS[UnitsLength.GIGAMETER][UnitsLength.NANOMETER] = \
        Mul(Rat(Int(1), Pow(10, 18)), Sym("gigam"))  # nopep8
    CONVERSIONS[UnitsLength.GIGAMETER][UnitsLength.PARSEC] = \
        Mul(Int(30856776), Sym("gigam"))  # nopep8
    CONVERSIONS[UnitsLength.GIGAMETER][UnitsLength.PETAMETER] = \
        Mul(Pow(10, 6), Sym("gigam"))  # nopep8
    CONVERSIONS[UnitsLength.GIGAMETER][UnitsLength.PICOMETER] = \
        Mul(Rat(Int(1), Pow(10, 21)), Sym("gigam"))  # nopep8
    CONVERSIONS[UnitsLength.GIGAMETER][UnitsLength.POINT] = \
        Mul(Rat(Int(127), Mul(Int(36), Pow(10, 13))), Sym("gigam"))  # nopep8
    CONVERSIONS[UnitsLength.GIGAMETER][UnitsLength.TERAMETER] = \
        Mul(Int(1000), Sym("gigam"))  # nopep8
    CONVERSIONS[UnitsLength.GIGAMETER][UnitsLength.THOU] = \
        Mul(Rat(Int(127), Mul(Int(5), Pow(10, 15))), Sym("gigam"))  # nopep8
    CONVERSIONS[UnitsLength.GIGAMETER][UnitsLength.YARD] = \
        Mul(Rat(Int(1143), Mul(Int(125), Pow(10, 10))), Sym("gigam"))  # nopep8
    CONVERSIONS[UnitsLength.GIGAMETER][UnitsLength.YOCTOMETER] = \
        Mul(Rat(Int(1), Pow(10, 33)), Sym("gigam"))  # nopep8
    CONVERSIONS[UnitsLength.GIGAMETER][UnitsLength.YOTTAMETER] = \
        Mul(Pow(10, 15), Sym("gigam"))  # nopep8
    CONVERSIONS[UnitsLength.GIGAMETER][UnitsLength.ZEPTOMETER] = \
        Mul(Rat(Int(1), Pow(10, 30)), Sym("gigam"))  # nopep8
    CONVERSIONS[UnitsLength.GIGAMETER][UnitsLength.ZETTAMETER] = \
        Mul(Pow(10, 12), Sym("gigam"))  # nopep8
    CONVERSIONS[UnitsLength.HECTOMETER][UnitsLength.ANGSTROM] = \
        Mul(Rat(Int(1), Pow(10, 12)), Sym("hectom"))  # nopep8
    CONVERSIONS[UnitsLength.HECTOMETER][UnitsLength.ASTRONOMICALUNIT] = \
        Mul(Int(1495978707), Sym("hectom"))  # nopep8
    CONVERSIONS[UnitsLength.HECTOMETER][UnitsLength.ATTOMETER] = \
        Mul(Rat(Int(1), Pow(10, 20)), Sym("hectom"))  # nopep8
    CONVERSIONS[UnitsLength.HECTOMETER][UnitsLength.CENTIMETER] = \
        Mul(Rat(Int(1), Pow(10, 4)), Sym("hectom"))  # nopep8
    CONVERSIONS[UnitsLength.HECTOMETER][UnitsLength.DECAMETER] = \
        Mul(Rat(Int(1), Int(10)), Sym("hectom"))  # nopep8
    CONVERSIONS[UnitsLength.HECTOMETER][UnitsLength.DECIMETER] = \
        Mul(Rat(Int(1), Int(1000)), Sym("hectom"))  # nopep8
    CONVERSIONS[UnitsLength.HECTOMETER][UnitsLength.EXAMETER] = \
        Mul(Pow(10, 16), Sym("hectom"))  # nopep8
    CONVERSIONS[UnitsLength.HECTOMETER][UnitsLength.FEMTOMETER] = \
        Mul(Rat(Int(1), Pow(10, 17)), Sym("hectom"))  # nopep8
    CONVERSIONS[UnitsLength.HECTOMETER][UnitsLength.FOOT] = \
        Mul(Rat(Int(381), Int(125000)), Sym("hectom"))  # nopep8
    CONVERSIONS[UnitsLength.HECTOMETER][UnitsLength.GIGAMETER] = \
        Mul(Pow(10, 7), Sym("hectom"))  # nopep8
    CONVERSIONS[UnitsLength.HECTOMETER][UnitsLength.INCH] = \
        Mul(Rat(Int(127), Mul(Int(5), Pow(10, 5))), Sym("hectom"))  # nopep8
    CONVERSIONS[UnitsLength.HECTOMETER][UnitsLength.KILOMETER] = \
        Mul(Int(10), Sym("hectom"))  # nopep8
    CONVERSIONS[UnitsLength.HECTOMETER][UnitsLength.LIGHTYEAR] = \
        Mul(Int("94607304725808"), Sym("hectom"))  # nopep8
    CONVERSIONS[UnitsLength.HECTOMETER][UnitsLength.LINE] = \
        Mul(Rat(Int(127), Mul(Int(6), Pow(10, 6))), Sym("hectom"))  # nopep8
    CONVERSIONS[UnitsLength.HECTOMETER][UnitsLength.MEGAMETER] = \
        Mul(Pow(10, 4), Sym("hectom"))  # nopep8
    CONVERSIONS[UnitsLength.HECTOMETER][UnitsLength.METER] = \
        Mul(Rat(Int(1), Int(100)), Sym("hectom"))  # nopep8
    CONVERSIONS[UnitsLength.HECTOMETER][UnitsLength.MICROMETER] = \
        Mul(Rat(Int(1), Pow(10, 8)), Sym("hectom"))  # nopep8
    CONVERSIONS[UnitsLength.HECTOMETER][UnitsLength.MILE] = \
        Mul(Rat(Int(50292), Int(3125)), Sym("hectom"))  # nopep8
    CONVERSIONS[UnitsLength.HECTOMETER][UnitsLength.MILLIMETER] = \
        Mul(Rat(Int(1), Pow(10, 5)), Sym("hectom"))  # nopep8
    CONVERSIONS[UnitsLength.HECTOMETER][UnitsLength.NANOMETER] = \
        Mul(Rat(Int(1), Pow(10, 11)), Sym("hectom"))  # nopep8
    CONVERSIONS[UnitsLength.HECTOMETER][UnitsLength.PARSEC] = \
        Mul(Mul(Int(30856776), Pow(10, 7)), Sym("hectom"))  # nopep8
    CONVERSIONS[UnitsLength.HECTOMETER][UnitsLength.PETAMETER] = \
        Mul(Pow(10, 13), Sym("hectom"))  # nopep8
    CONVERSIONS[UnitsLength.HECTOMETER][UnitsLength.PICOMETER] = \
        Mul(Rat(Int(1), Pow(10, 14)), Sym("hectom"))  # nopep8
    CONVERSIONS[UnitsLength.HECTOMETER][UnitsLength.POINT] = \
        Mul(Rat(Int(127), Mul(Int(36), Pow(10, 6))), Sym("hectom"))  # nopep8
    CONVERSIONS[UnitsLength.HECTOMETER][UnitsLength.TERAMETER] = \
        Mul(Pow(10, 10), Sym("hectom"))  # nopep8
    CONVERSIONS[UnitsLength.HECTOMETER][UnitsLength.THOU] = \
        Mul(Rat(Int(127), Mul(Int(5), Pow(10, 8))), Sym("hectom"))  # nopep8
    CONVERSIONS[UnitsLength.HECTOMETER][UnitsLength.YARD] = \
        Mul(Rat(Int(1143), Int(125000)), Sym("hectom"))  # nopep8
    CONVERSIONS[UnitsLength.HECTOMETER][UnitsLength.YOCTOMETER] = \
        Mul(Rat(Int(1), Pow(10, 26)), Sym("hectom"))  # nopep8
    CONVERSIONS[UnitsLength.HECTOMETER][UnitsLength.YOTTAMETER] = \
        Mul(Pow(10, 22), Sym("hectom"))  # nopep8
    CONVERSIONS[UnitsLength.HECTOMETER][UnitsLength.ZEPTOMETER] = \
        Mul(Rat(Int(1), Pow(10, 23)), Sym("hectom"))  # nopep8
    CONVERSIONS[UnitsLength.HECTOMETER][UnitsLength.ZETTAMETER] = \
        Mul(Pow(10, 19), Sym("hectom"))  # nopep8
    CONVERSIONS[UnitsLength.INCH][UnitsLength.ANGSTROM] = \
        Mul(Rat(Int(1), Mul(Int(254), Pow(10, 6))), Sym("in"))  # nopep8
    CONVERSIONS[UnitsLength.INCH][UnitsLength.ASTRONOMICALUNIT] = \
        Mul(Rat(Mul(Int("7479893535"), Pow(10, 5)), Int(127)), Sym("in"))  # nopep8
    CONVERSIONS[UnitsLength.INCH][UnitsLength.ATTOMETER] = \
        Mul(Rat(Int(1), Mul(Int(254), Pow(10, 14))), Sym("in"))  # nopep8
    CONVERSIONS[UnitsLength.INCH][UnitsLength.CENTIMETER] = \
        Mul(Rat(Int(50), Int(127)), Sym("in"))  # nopep8
    CONVERSIONS[UnitsLength.INCH][UnitsLength.DECAMETER] = \
        Mul(Rat(Mul(Int(5), Pow(10, 4)), Int(127)), Sym("in"))  # nopep8
    CONVERSIONS[UnitsLength.INCH][UnitsLength.DECIMETER] = \
        Mul(Rat(Int(500), Int(127)), Sym("in"))  # nopep8
    CONVERSIONS[UnitsLength.INCH][UnitsLength.EXAMETER] = \
        Mul(Rat(Mul(Int(5), Pow(10, 21)), Int(127)), Sym("in"))  # nopep8
    CONVERSIONS[UnitsLength.INCH][UnitsLength.FEMTOMETER] = \
        Mul(Rat(Int(1), Mul(Int(254), Pow(10, 11))), Sym("in"))  # nopep8
    CONVERSIONS[UnitsLength.INCH][UnitsLength.FOOT] = \
        Mul(Int(12), Sym("in"))  # nopep8
    CONVERSIONS[UnitsLength.INCH][UnitsLength.GIGAMETER] = \
        Mul(Rat(Mul(Int(5), Pow(10, 12)), Int(127)), Sym("in"))  # nopep8
    CONVERSIONS[UnitsLength.INCH][UnitsLength.HECTOMETER] = \
        Mul(Rat(Mul(Int(5), Pow(10, 5)), Int(127)), Sym("in"))  # nopep8
    CONVERSIONS[UnitsLength.INCH][UnitsLength.KILOMETER] = \
        Mul(Rat(Mul(Int(5), Pow(10, 6)), Int(127)), Sym("in"))  # nopep8
    CONVERSIONS[UnitsLength.INCH][UnitsLength.LIGHTYEAR] = \
        Mul(Rat(Mul(Int("47303652362904"), Pow(10, 6)), Int(127)), Sym("in"))  # nopep8
    CONVERSIONS[UnitsLength.INCH][UnitsLength.LINE] = \
        Mul(Rat(Int(1), Int(12)), Sym("in"))  # nopep8
    CONVERSIONS[UnitsLength.INCH][UnitsLength.MEGAMETER] = \
        Mul(Rat(Mul(Int(5), Pow(10, 9)), Int(127)), Sym("in"))  # nopep8
    CONVERSIONS[UnitsLength.INCH][UnitsLength.METER] = \
        Mul(Rat(Int(5000), Int(127)), Sym("in"))  # nopep8
    CONVERSIONS[UnitsLength.INCH][UnitsLength.MICROMETER] = \
        Mul(Rat(Int(1), Int(25400)), Sym("in"))  # nopep8
    CONVERSIONS[UnitsLength.INCH][UnitsLength.MILE] = \
        Mul(Int(63360), Sym("in"))  # nopep8
    CONVERSIONS[UnitsLength.INCH][UnitsLength.MILLIMETER] = \
        Mul(Rat(Int(5), Int(127)), Sym("in"))  # nopep8
    CONVERSIONS[UnitsLength.INCH][UnitsLength.NANOMETER] = \
        Mul(Rat(Int(1), Mul(Int(254), Pow(10, 5))), Sym("in"))  # nopep8
    CONVERSIONS[UnitsLength.INCH][UnitsLength.PARSEC] = \
        Mul(Rat(Mul(Int(15428388), Pow(10, 13)), Int(127)), Sym("in"))  # nopep8
    CONVERSIONS[UnitsLength.INCH][UnitsLength.PETAMETER] = \
        Mul(Rat(Mul(Int(5), Pow(10, 18)), Int(127)), Sym("in"))  # nopep8
    CONVERSIONS[UnitsLength.INCH][UnitsLength.PICOMETER] = \
        Mul(Rat(Int(1), Mul(Int(254), Pow(10, 8))), Sym("in"))  # nopep8
    CONVERSIONS[UnitsLength.INCH][UnitsLength.POINT] = \
        Mul(Rat(Int(1), Int(72)), Sym("in"))  # nopep8
    CONVERSIONS[UnitsLength.INCH][UnitsLength.TERAMETER] = \
        Mul(Rat(Mul(Int(5), Pow(10, 15)), Int(127)), Sym("in"))  # nopep8
    CONVERSIONS[UnitsLength.INCH][UnitsLength.THOU] = \
        Mul(Rat(Int(1), Int(1000)), Sym("in"))  # nopep8
    CONVERSIONS[UnitsLength.INCH][UnitsLength.YARD] = \
        Mul(Int(36), Sym("in"))  # nopep8
    CONVERSIONS[UnitsLength.INCH][UnitsLength.YOCTOMETER] = \
        Mul(Rat(Int(1), Mul(Int(254), Pow(10, 20))), Sym("in"))  # nopep8
    CONVERSIONS[UnitsLength.INCH][UnitsLength.YOTTAMETER] = \
        Mul(Rat(Mul(Int(5), Pow(10, 27)), Int(127)), Sym("in"))  # nopep8
    CONVERSIONS[UnitsLength.INCH][UnitsLength.ZEPTOMETER] = \
        Mul(Rat(Int(1), Mul(Int(254), Pow(10, 17))), Sym("in"))  # nopep8
    CONVERSIONS[UnitsLength.INCH][UnitsLength.ZETTAMETER] = \
        Mul(Rat(Mul(Int(5), Pow(10, 24)), Int(127)), Sym("in"))  # nopep8
    CONVERSIONS[UnitsLength.KILOMETER][UnitsLength.ANGSTROM] = \
        Mul(Rat(Int(1), Pow(10, 13)), Sym("kilom"))  # nopep8
    CONVERSIONS[UnitsLength.KILOMETER][UnitsLength.ASTRONOMICALUNIT] = \
        Mul(Rat(Int(1495978707), Int(10)), Sym("kilom"))  # nopep8
    CONVERSIONS[UnitsLength.KILOMETER][UnitsLength.ATTOMETER] = \
        Mul(Rat(Int(1), Pow(10, 21)), Sym("kilom"))  # nopep8
    CONVERSIONS[UnitsLength.KILOMETER][UnitsLength.CENTIMETER] = \
        Mul(Rat(Int(1), Pow(10, 5)), Sym("kilom"))  # nopep8
    CONVERSIONS[UnitsLength.KILOMETER][UnitsLength.DECAMETER] = \
        Mul(Rat(Int(1), Int(100)), Sym("kilom"))  # nopep8
    CONVERSIONS[UnitsLength.KILOMETER][UnitsLength.DECIMETER] = \
        Mul(Rat(Int(1), Pow(10, 4)), Sym("kilom"))  # nopep8
    CONVERSIONS[UnitsLength.KILOMETER][UnitsLength.EXAMETER] = \
        Mul(Pow(10, 15), Sym("kilom"))  # nopep8
    CONVERSIONS[UnitsLength.KILOMETER][UnitsLength.FEMTOMETER] = \
        Mul(Rat(Int(1), Pow(10, 18)), Sym("kilom"))  # nopep8
    CONVERSIONS[UnitsLength.KILOMETER][UnitsLength.FOOT] = \
        Mul(Rat(Int(381), Mul(Int(125), Pow(10, 4))), Sym("kilom"))  # nopep8
    CONVERSIONS[UnitsLength.KILOMETER][UnitsLength.GIGAMETER] = \
        Mul(Pow(10, 6), Sym("kilom"))  # nopep8
    CONVERSIONS[UnitsLength.KILOMETER][UnitsLength.HECTOMETER] = \
        Mul(Rat(Int(1), Int(10)), Sym("kilom"))  # nopep8
    CONVERSIONS[UnitsLength.KILOMETER][UnitsLength.INCH] = \
        Mul(Rat(Int(127), Mul(Int(5), Pow(10, 6))), Sym("kilom"))  # nopep8
    CONVERSIONS[UnitsLength.KILOMETER][UnitsLength.LIGHTYEAR] = \
        Mul(Rat(Int("47303652362904"), Int(5)), Sym("kilom"))  # nopep8
    CONVERSIONS[UnitsLength.KILOMETER][UnitsLength.LINE] = \
        Mul(Rat(Int(127), Mul(Int(6), Pow(10, 7))), Sym("kilom"))  # nopep8
    CONVERSIONS[UnitsLength.KILOMETER][UnitsLength.MEGAMETER] = \
        Mul(Int(1000), Sym("kilom"))  # nopep8
    CONVERSIONS[UnitsLength.KILOMETER][UnitsLength.METER] = \
        Mul(Rat(Int(1), Int(1000)), Sym("kilom"))  # nopep8
    CONVERSIONS[UnitsLength.KILOMETER][UnitsLength.MICROMETER] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("kilom"))  # nopep8
    CONVERSIONS[UnitsLength.KILOMETER][UnitsLength.MILE] = \
        Mul(Rat(Int(25146), Int(15625)), Sym("kilom"))  # nopep8
    CONVERSIONS[UnitsLength.KILOMETER][UnitsLength.MILLIMETER] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("kilom"))  # nopep8
    CONVERSIONS[UnitsLength.KILOMETER][UnitsLength.NANOMETER] = \
        Mul(Rat(Int(1), Pow(10, 12)), Sym("kilom"))  # nopep8
    CONVERSIONS[UnitsLength.KILOMETER][UnitsLength.PARSEC] = \
        Mul(Mul(Int(30856776), Pow(10, 6)), Sym("kilom"))  # nopep8
    CONVERSIONS[UnitsLength.KILOMETER][UnitsLength.PETAMETER] = \
        Mul(Pow(10, 12), Sym("kilom"))  # nopep8
    CONVERSIONS[UnitsLength.KILOMETER][UnitsLength.PICOMETER] = \
        Mul(Rat(Int(1), Pow(10, 15)), Sym("kilom"))  # nopep8
    CONVERSIONS[UnitsLength.KILOMETER][UnitsLength.POINT] = \
        Mul(Rat(Int(127), Mul(Int(36), Pow(10, 7))), Sym("kilom"))  # nopep8
    CONVERSIONS[UnitsLength.KILOMETER][UnitsLength.TERAMETER] = \
        Mul(Pow(10, 9), Sym("kilom"))  # nopep8
    CONVERSIONS[UnitsLength.KILOMETER][UnitsLength.THOU] = \
        Mul(Rat(Int(127), Mul(Int(5), Pow(10, 9))), Sym("kilom"))  # nopep8
    CONVERSIONS[UnitsLength.KILOMETER][UnitsLength.YARD] = \
        Mul(Rat(Int(1143), Mul(Int(125), Pow(10, 4))), Sym("kilom"))  # nopep8
    CONVERSIONS[UnitsLength.KILOMETER][UnitsLength.YOCTOMETER] = \
        Mul(Rat(Int(1), Pow(10, 27)), Sym("kilom"))  # nopep8
    CONVERSIONS[UnitsLength.KILOMETER][UnitsLength.YOTTAMETER] = \
        Mul(Pow(10, 21), Sym("kilom"))  # nopep8
    CONVERSIONS[UnitsLength.KILOMETER][UnitsLength.ZEPTOMETER] = \
        Mul(Rat(Int(1), Pow(10, 24)), Sym("kilom"))  # nopep8
    CONVERSIONS[UnitsLength.KILOMETER][UnitsLength.ZETTAMETER] = \
        Mul(Pow(10, 18), Sym("kilom"))  # nopep8
    CONVERSIONS[UnitsLength.LIGHTYEAR][UnitsLength.ANGSTROM] = \
        Mul(Rat(Int(1), Mul(Int("94607304725808"), Pow(10, 12))), Sym("ly"))  # nopep8
    CONVERSIONS[UnitsLength.LIGHTYEAR][UnitsLength.ASTRONOMICALUNIT] = \
        Mul(Rat(Int(6830953), Int("431996825232")), Sym("ly"))  # nopep8
    CONVERSIONS[UnitsLength.LIGHTYEAR][UnitsLength.ATTOMETER] = \
        Mul(Rat(Int(1), Mul(Int("94607304725808"), Pow(10, 20))), Sym("ly"))  # nopep8
    CONVERSIONS[UnitsLength.LIGHTYEAR][UnitsLength.CENTIMETER] = \
        Mul(Rat(Int(1), Mul(Int("94607304725808"), Pow(10, 4))), Sym("ly"))  # nopep8
    CONVERSIONS[UnitsLength.LIGHTYEAR][UnitsLength.DECAMETER] = \
        Mul(Rat(Int(1), Int("946073047258080")), Sym("ly"))  # nopep8
    CONVERSIONS[UnitsLength.LIGHTYEAR][UnitsLength.DECIMETER] = \
        Mul(Rat(Int(1), Int("94607304725808000")), Sym("ly"))  # nopep8
    CONVERSIONS[UnitsLength.LIGHTYEAR][UnitsLength.EXAMETER] = \
        Mul(Rat(Mul(Int(625), Pow(10, 12)), Int("5912956545363")), Sym("ly"))  # nopep8
    CONVERSIONS[UnitsLength.LIGHTYEAR][UnitsLength.FEMTOMETER] = \
        Mul(Rat(Int(1), Mul(Int("94607304725808"), Pow(10, 17))), Sym("ly"))  # nopep8
    CONVERSIONS[UnitsLength.LIGHTYEAR][UnitsLength.FOOT] = \
        Mul(Rat(Int(127), Mul(Int("3941971030242"), Pow(10, 6))), Sym("ly"))  # nopep8
    CONVERSIONS[UnitsLength.LIGHTYEAR][UnitsLength.GIGAMETER] = \
        Mul(Rat(Int(625000), Int("5912956545363")), Sym("ly"))  # nopep8
    CONVERSIONS[UnitsLength.LIGHTYEAR][UnitsLength.HECTOMETER] = \
        Mul(Rat(Int(1), Int("94607304725808")), Sym("ly"))  # nopep8
    CONVERSIONS[UnitsLength.LIGHTYEAR][UnitsLength.INCH] = \
        Mul(Rat(Int(127), Mul(Int("47303652362904"), Pow(10, 6))), Sym("ly"))  # nopep8
    CONVERSIONS[UnitsLength.LIGHTYEAR][UnitsLength.KILOMETER] = \
        Mul(Rat(Int(5), Int("47303652362904")), Sym("ly"))  # nopep8
    CONVERSIONS[UnitsLength.LIGHTYEAR][UnitsLength.LINE] = \
        Mul(Rat(Int(127), Mul(Int("567643828354848"), Pow(10, 6))), Sym("ly"))  # nopep8
    CONVERSIONS[UnitsLength.LIGHTYEAR][UnitsLength.MEGAMETER] = \
        Mul(Rat(Int(625), Int("5912956545363")), Sym("ly"))  # nopep8
    CONVERSIONS[UnitsLength.LIGHTYEAR][UnitsLength.METER] = \
        Mul(Rat(Int(1), Int("9460730472580800")), Sym("ly"))  # nopep8
    CONVERSIONS[UnitsLength.LIGHTYEAR][UnitsLength.MICROMETER] = \
        Mul(Rat(Int(1), Mul(Int("94607304725808"), Pow(10, 8))), Sym("ly"))  # nopep8
    CONVERSIONS[UnitsLength.LIGHTYEAR][UnitsLength.MILE] = \
        Mul(Rat(Int(1397), Int("8212439646337500")), Sym("ly"))  # nopep8
    CONVERSIONS[UnitsLength.LIGHTYEAR][UnitsLength.MILLIMETER] = \
        Mul(Rat(Int(1), Mul(Int("94607304725808"), Pow(10, 5))), Sym("ly"))  # nopep8
    CONVERSIONS[UnitsLength.LIGHTYEAR][UnitsLength.NANOMETER] = \
        Mul(Rat(Int(1), Mul(Int("94607304725808"), Pow(10, 11))), Sym("ly"))  # nopep8
    CONVERSIONS[UnitsLength.LIGHTYEAR][UnitsLength.PARSEC] = \
        Mul(Rat(Mul(Int(6428495), Pow(10, 6)), Int("1970985515121")), Sym("ly"))  # nopep8
    CONVERSIONS[UnitsLength.LIGHTYEAR][UnitsLength.PETAMETER] = \
        Mul(Rat(Mul(Int(625), Pow(10, 9)), Int("5912956545363")), Sym("ly"))  # nopep8
    CONVERSIONS[UnitsLength.LIGHTYEAR][UnitsLength.PICOMETER] = \
        Mul(Rat(Int(1), Mul(Int("94607304725808"), Pow(10, 14))), Sym("ly"))  # nopep8
    CONVERSIONS[UnitsLength.LIGHTYEAR][UnitsLength.POINT] = \
        Mul(Rat(Int(127), Mul(Int("3405862970129088"), Pow(10, 6))), Sym("ly"))  # nopep8
    CONVERSIONS[UnitsLength.LIGHTYEAR][UnitsLength.TERAMETER] = \
        Mul(Rat(Mul(Int(625), Pow(10, 6)), Int("5912956545363")), Sym("ly"))  # nopep8
    CONVERSIONS[UnitsLength.LIGHTYEAR][UnitsLength.THOU] = \
        Mul(Rat(Int(127), Mul(Int("47303652362904"), Pow(10, 9))), Sym("ly"))  # nopep8
    CONVERSIONS[UnitsLength.LIGHTYEAR][UnitsLength.YARD] = \
        Mul(Rat(Int(127), Mul(Int("1313990343414"), Pow(10, 6))), Sym("ly"))  # nopep8
    CONVERSIONS[UnitsLength.LIGHTYEAR][UnitsLength.YOCTOMETER] = \
        Mul(Rat(Int(1), Mul(Int("94607304725808"), Pow(10, 26))), Sym("ly"))  # nopep8
    CONVERSIONS[UnitsLength.LIGHTYEAR][UnitsLength.YOTTAMETER] = \
        Mul(Rat(Mul(Int(625), Pow(10, 18)), Int("5912956545363")), Sym("ly"))  # nopep8
    CONVERSIONS[UnitsLength.LIGHTYEAR][UnitsLength.ZEPTOMETER] = \
        Mul(Rat(Int(1), Mul(Int("94607304725808"), Pow(10, 23))), Sym("ly"))  # nopep8
    CONVERSIONS[UnitsLength.LIGHTYEAR][UnitsLength.ZETTAMETER] = \
        Mul(Rat(Mul(Int(625), Pow(10, 15)), Int("5912956545363")), Sym("ly"))  # nopep8
    CONVERSIONS[UnitsLength.LINE][UnitsLength.ANGSTROM] = \
        Mul(Rat(Int(3), Mul(Int(635), Pow(10, 5))), Sym("li"))  # nopep8
    CONVERSIONS[UnitsLength.LINE][UnitsLength.ASTRONOMICALUNIT] = \
        Mul(Rat(Mul(Int("8975872242"), Pow(10, 6)), Int(127)), Sym("li"))  # nopep8
    CONVERSIONS[UnitsLength.LINE][UnitsLength.ATTOMETER] = \
        Mul(Rat(Int(3), Mul(Int(635), Pow(10, 13))), Sym("li"))  # nopep8
    CONVERSIONS[UnitsLength.LINE][UnitsLength.CENTIMETER] = \
        Mul(Rat(Int(600), Int(127)), Sym("li"))  # nopep8
    CONVERSIONS[UnitsLength.LINE][UnitsLength.DECAMETER] = \
        Mul(Rat(Mul(Int(6), Pow(10, 5)), Int(127)), Sym("li"))  # nopep8
    CONVERSIONS[UnitsLength.LINE][UnitsLength.DECIMETER] = \
        Mul(Rat(Int(6000), Int(127)), Sym("li"))  # nopep8
    CONVERSIONS[UnitsLength.LINE][UnitsLength.EXAMETER] = \
        Mul(Rat(Mul(Int(6), Pow(10, 22)), Int(127)), Sym("li"))  # nopep8
    CONVERSIONS[UnitsLength.LINE][UnitsLength.FEMTOMETER] = \
        Mul(Rat(Int(3), Mul(Int(635), Pow(10, 10))), Sym("li"))  # nopep8
    CONVERSIONS[UnitsLength.LINE][UnitsLength.FOOT] = \
        Mul(Int(144), Sym("li"))  # nopep8
    CONVERSIONS[UnitsLength.LINE][UnitsLength.GIGAMETER] = \
        Mul(Rat(Mul(Int(6), Pow(10, 13)), Int(127)), Sym("li"))  # nopep8
    CONVERSIONS[UnitsLength.LINE][UnitsLength.HECTOMETER] = \
        Mul(Rat(Mul(Int(6), Pow(10, 6)), Int(127)), Sym("li"))  # nopep8
    CONVERSIONS[UnitsLength.LINE][UnitsLength.INCH] = \
        Mul(Int(12), Sym("li"))  # nopep8
    CONVERSIONS[UnitsLength.LINE][UnitsLength.KILOMETER] = \
        Mul(Rat(Mul(Int(6), Pow(10, 7)), Int(127)), Sym("li"))  # nopep8
    CONVERSIONS[UnitsLength.LINE][UnitsLength.LIGHTYEAR] = \
        Mul(Rat(Mul(Int("567643828354848"), Pow(10, 6)), Int(127)), Sym("li"))  # nopep8
    CONVERSIONS[UnitsLength.LINE][UnitsLength.MEGAMETER] = \
        Mul(Rat(Mul(Int(6), Pow(10, 10)), Int(127)), Sym("li"))  # nopep8
    CONVERSIONS[UnitsLength.LINE][UnitsLength.METER] = \
        Mul(Rat(Mul(Int(6), Pow(10, 4)), Int(127)), Sym("li"))  # nopep8
    CONVERSIONS[UnitsLength.LINE][UnitsLength.MICROMETER] = \
        Mul(Rat(Int(3), Int(6350)), Sym("li"))  # nopep8
    CONVERSIONS[UnitsLength.LINE][UnitsLength.MILE] = \
        Mul(Int(760320), Sym("li"))  # nopep8
    CONVERSIONS[UnitsLength.LINE][UnitsLength.MILLIMETER] = \
        Mul(Rat(Int(60), Int(127)), Sym("li"))  # nopep8
    CONVERSIONS[UnitsLength.LINE][UnitsLength.NANOMETER] = \
        Mul(Rat(Int(3), Mul(Int(635), Pow(10, 4))), Sym("li"))  # nopep8
    CONVERSIONS[UnitsLength.LINE][UnitsLength.PARSEC] = \
        Mul(Rat(Mul(Int(185140656), Pow(10, 13)), Int(127)), Sym("li"))  # nopep8
    CONVERSIONS[UnitsLength.LINE][UnitsLength.PETAMETER] = \
        Mul(Rat(Mul(Int(6), Pow(10, 19)), Int(127)), Sym("li"))  # nopep8
    CONVERSIONS[UnitsLength.LINE][UnitsLength.PICOMETER] = \
        Mul(Rat(Int(3), Mul(Int(635), Pow(10, 7))), Sym("li"))  # nopep8
    CONVERSIONS[UnitsLength.LINE][UnitsLength.POINT] = \
        Mul(Rat(Int(1), Int(6)), Sym("li"))  # nopep8
    CONVERSIONS[UnitsLength.LINE][UnitsLength.TERAMETER] = \
        Mul(Rat(Mul(Int(6), Pow(10, 16)), Int(127)), Sym("li"))  # nopep8
    CONVERSIONS[UnitsLength.LINE][UnitsLength.THOU] = \
        Mul(Rat(Int(3), Int(250)), Sym("li"))  # nopep8
    CONVERSIONS[UnitsLength.LINE][UnitsLength.YARD] = \
        Mul(Int(432), Sym("li"))  # nopep8
    CONVERSIONS[UnitsLength.LINE][UnitsLength.YOCTOMETER] = \
        Mul(Rat(Int(3), Mul(Int(635), Pow(10, 19))), Sym("li"))  # nopep8
    CONVERSIONS[UnitsLength.LINE][UnitsLength.YOTTAMETER] = \
        Mul(Rat(Mul(Int(6), Pow(10, 28)), Int(127)), Sym("li"))  # nopep8
    CONVERSIONS[UnitsLength.LINE][UnitsLength.ZEPTOMETER] = \
        Mul(Rat(Int(3), Mul(Int(635), Pow(10, 16))), Sym("li"))  # nopep8
    CONVERSIONS[UnitsLength.LINE][UnitsLength.ZETTAMETER] = \
        Mul(Rat(Mul(Int(6), Pow(10, 25)), Int(127)), Sym("li"))  # nopep8
    CONVERSIONS[UnitsLength.MEGAMETER][UnitsLength.ANGSTROM] = \
        Mul(Rat(Int(1), Pow(10, 16)), Sym("megam"))  # nopep8
    CONVERSIONS[UnitsLength.MEGAMETER][UnitsLength.ASTRONOMICALUNIT] = \
        Mul(Rat(Int(1495978707), Pow(10, 4)), Sym("megam"))  # nopep8
    CONVERSIONS[UnitsLength.MEGAMETER][UnitsLength.ATTOMETER] = \
        Mul(Rat(Int(1), Pow(10, 24)), Sym("megam"))  # nopep8
    CONVERSIONS[UnitsLength.MEGAMETER][UnitsLength.CENTIMETER] = \
        Mul(Rat(Int(1), Pow(10, 8)), Sym("megam"))  # nopep8
    CONVERSIONS[UnitsLength.MEGAMETER][UnitsLength.DECAMETER] = \
        Mul(Rat(Int(1), Pow(10, 5)), Sym("megam"))  # nopep8
    CONVERSIONS[UnitsLength.MEGAMETER][UnitsLength.DECIMETER] = \
        Mul(Rat(Int(1), Pow(10, 7)), Sym("megam"))  # nopep8
    CONVERSIONS[UnitsLength.MEGAMETER][UnitsLength.EXAMETER] = \
        Mul(Pow(10, 12), Sym("megam"))  # nopep8
    CONVERSIONS[UnitsLength.MEGAMETER][UnitsLength.FEMTOMETER] = \
        Mul(Rat(Int(1), Pow(10, 21)), Sym("megam"))  # nopep8
    CONVERSIONS[UnitsLength.MEGAMETER][UnitsLength.FOOT] = \
        Mul(Rat(Int(381), Mul(Int(125), Pow(10, 7))), Sym("megam"))  # nopep8
    CONVERSIONS[UnitsLength.MEGAMETER][UnitsLength.GIGAMETER] = \
        Mul(Int(1000), Sym("megam"))  # nopep8
    CONVERSIONS[UnitsLength.MEGAMETER][UnitsLength.HECTOMETER] = \
        Mul(Rat(Int(1), Pow(10, 4)), Sym("megam"))  # nopep8
    CONVERSIONS[UnitsLength.MEGAMETER][UnitsLength.INCH] = \
        Mul(Rat(Int(127), Mul(Int(5), Pow(10, 9))), Sym("megam"))  # nopep8
    CONVERSIONS[UnitsLength.MEGAMETER][UnitsLength.KILOMETER] = \
        Mul(Rat(Int(1), Int(1000)), Sym("megam"))  # nopep8
    CONVERSIONS[UnitsLength.MEGAMETER][UnitsLength.LIGHTYEAR] = \
        Mul(Rat(Int("5912956545363"), Int(625)), Sym("megam"))  # nopep8
    CONVERSIONS[UnitsLength.MEGAMETER][UnitsLength.LINE] = \
        Mul(Rat(Int(127), Mul(Int(6), Pow(10, 10))), Sym("megam"))  # nopep8
    CONVERSIONS[UnitsLength.MEGAMETER][UnitsLength.METER] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("megam"))  # nopep8
    CONVERSIONS[UnitsLength.MEGAMETER][UnitsLength.MICROMETER] = \
        Mul(Rat(Int(1), Pow(10, 12)), Sym("megam"))  # nopep8
    CONVERSIONS[UnitsLength.MEGAMETER][UnitsLength.MILE] = \
        Mul(Rat(Int(12573), Int(7812500)), Sym("megam"))  # nopep8
    CONVERSIONS[UnitsLength.MEGAMETER][UnitsLength.MILLIMETER] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("megam"))  # nopep8
    CONVERSIONS[UnitsLength.MEGAMETER][UnitsLength.NANOMETER] = \
        Mul(Rat(Int(1), Pow(10, 15)), Sym("megam"))  # nopep8
    CONVERSIONS[UnitsLength.MEGAMETER][UnitsLength.PARSEC] = \
        Mul(Int("30856776000"), Sym("megam"))  # nopep8
    CONVERSIONS[UnitsLength.MEGAMETER][UnitsLength.PETAMETER] = \
        Mul(Pow(10, 9), Sym("megam"))  # nopep8
    CONVERSIONS[UnitsLength.MEGAMETER][UnitsLength.PICOMETER] = \
        Mul(Rat(Int(1), Pow(10, 18)), Sym("megam"))  # nopep8
    CONVERSIONS[UnitsLength.MEGAMETER][UnitsLength.POINT] = \
        Mul(Rat(Int(127), Mul(Int(36), Pow(10, 10))), Sym("megam"))  # nopep8
    CONVERSIONS[UnitsLength.MEGAMETER][UnitsLength.TERAMETER] = \
        Mul(Pow(10, 6), Sym("megam"))  # nopep8
    CONVERSIONS[UnitsLength.MEGAMETER][UnitsLength.THOU] = \
        Mul(Rat(Int(127), Mul(Int(5), Pow(10, 12))), Sym("megam"))  # nopep8
    CONVERSIONS[UnitsLength.MEGAMETER][UnitsLength.YARD] = \
        Mul(Rat(Int(1143), Mul(Int(125), Pow(10, 7))), Sym("megam"))  # nopep8
    CONVERSIONS[UnitsLength.MEGAMETER][UnitsLength.YOCTOMETER] = \
        Mul(Rat(Int(1), Pow(10, 30)), Sym("megam"))  # nopep8
    CONVERSIONS[UnitsLength.MEGAMETER][UnitsLength.YOTTAMETER] = \
        Mul(Pow(10, 18), Sym("megam"))  # nopep8
    CONVERSIONS[UnitsLength.MEGAMETER][UnitsLength.ZEPTOMETER] = \
        Mul(Rat(Int(1), Pow(10, 27)), Sym("megam"))  # nopep8
    CONVERSIONS[UnitsLength.MEGAMETER][UnitsLength.ZETTAMETER] = \
        Mul(Pow(10, 15), Sym("megam"))  # nopep8
    CONVERSIONS[UnitsLength.METER][UnitsLength.ANGSTROM] = \
        Mul(Rat(Int(1), Pow(10, 10)), Sym("m"))  # nopep8
    CONVERSIONS[UnitsLength.METER][UnitsLength.ASTRONOMICALUNIT] = \
        Mul(Int("149597870700"), Sym("m"))  # nopep8
    CONVERSIONS[UnitsLength.METER][UnitsLength.ATTOMETER] = \
        Mul(Rat(Int(1), Pow(10, 18)), Sym("m"))  # nopep8
    CONVERSIONS[UnitsLength.METER][UnitsLength.CENTIMETER] = \
        Mul(Rat(Int(1), Int(100)), Sym("m"))  # nopep8
    CONVERSIONS[UnitsLength.METER][UnitsLength.DECAMETER] = \
        Mul(Int(10), Sym("m"))  # nopep8
    CONVERSIONS[UnitsLength.METER][UnitsLength.DECIMETER] = \
        Mul(Rat(Int(1), Int(10)), Sym("m"))  # nopep8
    CONVERSIONS[UnitsLength.METER][UnitsLength.EXAMETER] = \
        Mul(Pow(10, 18), Sym("m"))  # nopep8
    CONVERSIONS[UnitsLength.METER][UnitsLength.FEMTOMETER] = \
        Mul(Rat(Int(1), Pow(10, 15)), Sym("m"))  # nopep8
    CONVERSIONS[UnitsLength.METER][UnitsLength.FOOT] = \
        Mul(Rat(Int(381), Int(1250)), Sym("m"))  # nopep8
    CONVERSIONS[UnitsLength.METER][UnitsLength.GIGAMETER] = \
        Mul(Pow(10, 9), Sym("m"))  # nopep8
    CONVERSIONS[UnitsLength.METER][UnitsLength.HECTOMETER] = \
        Mul(Int(100), Sym("m"))  # nopep8
    CONVERSIONS[UnitsLength.METER][UnitsLength.INCH] = \
        Mul(Rat(Int(127), Int(5000)), Sym("m"))  # nopep8
    CONVERSIONS[UnitsLength.METER][UnitsLength.KILOMETER] = \
        Mul(Int(1000), Sym("m"))  # nopep8
    CONVERSIONS[UnitsLength.METER][UnitsLength.LIGHTYEAR] = \
        Mul(Int("9460730472580800"), Sym("m"))  # nopep8
    CONVERSIONS[UnitsLength.METER][UnitsLength.LINE] = \
        Mul(Rat(Int(127), Mul(Int(6), Pow(10, 4))), Sym("m"))  # nopep8
    CONVERSIONS[UnitsLength.METER][UnitsLength.MEGAMETER] = \
        Mul(Pow(10, 6), Sym("m"))  # nopep8
    CONVERSIONS[UnitsLength.METER][UnitsLength.MICROMETER] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("m"))  # nopep8
    CONVERSIONS[UnitsLength.METER][UnitsLength.MILE] = \
        Mul(Rat(Int(201168), Int(125)), Sym("m"))  # nopep8
    CONVERSIONS[UnitsLength.METER][UnitsLength.MILLIMETER] = \
        Mul(Rat(Int(1), Int(1000)), Sym("m"))  # nopep8
    CONVERSIONS[UnitsLength.METER][UnitsLength.NANOMETER] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("m"))  # nopep8
    CONVERSIONS[UnitsLength.METER][UnitsLength.PARSEC] = \
        Mul(Mul(Int(30856776), Pow(10, 9)), Sym("m"))  # nopep8
    CONVERSIONS[UnitsLength.METER][UnitsLength.PETAMETER] = \
        Mul(Pow(10, 15), Sym("m"))  # nopep8
    CONVERSIONS[UnitsLength.METER][UnitsLength.PICOMETER] = \
        Mul(Rat(Int(1), Pow(10, 12)), Sym("m"))  # nopep8
    CONVERSIONS[UnitsLength.METER][UnitsLength.POINT] = \
        Mul(Rat(Int(127), Mul(Int(36), Pow(10, 4))), Sym("m"))  # nopep8
    CONVERSIONS[UnitsLength.METER][UnitsLength.TERAMETER] = \
        Mul(Pow(10, 12), Sym("m"))  # nopep8
    CONVERSIONS[UnitsLength.METER][UnitsLength.THOU] = \
        Mul(Rat(Int(127), Mul(Int(5), Pow(10, 6))), Sym("m"))  # nopep8
    CONVERSIONS[UnitsLength.METER][UnitsLength.YARD] = \
        Mul(Rat(Int(1143), Int(1250)), Sym("m"))  # nopep8
    CONVERSIONS[UnitsLength.METER][UnitsLength.YOCTOMETER] = \
        Mul(Rat(Int(1), Pow(10, 24)), Sym("m"))  # nopep8
    CONVERSIONS[UnitsLength.METER][UnitsLength.YOTTAMETER] = \
        Mul(Pow(10, 24), Sym("m"))  # nopep8
    CONVERSIONS[UnitsLength.METER][UnitsLength.ZEPTOMETER] = \
        Mul(Rat(Int(1), Pow(10, 21)), Sym("m"))  # nopep8
    CONVERSIONS[UnitsLength.METER][UnitsLength.ZETTAMETER] = \
        Mul(Pow(10, 21), Sym("m"))  # nopep8
    CONVERSIONS[UnitsLength.MICROMETER][UnitsLength.ANGSTROM] = \
        Mul(Rat(Int(1), Pow(10, 4)), Sym("microm"))  # nopep8
    CONVERSIONS[UnitsLength.MICROMETER][UnitsLength.ASTRONOMICALUNIT] = \
        Mul(Mul(Int(1495978707), Pow(10, 8)), Sym("microm"))  # nopep8
    CONVERSIONS[UnitsLength.MICROMETER][UnitsLength.ATTOMETER] = \
        Mul(Rat(Int(1), Pow(10, 12)), Sym("microm"))  # nopep8
    CONVERSIONS[UnitsLength.MICROMETER][UnitsLength.CENTIMETER] = \
        Mul(Pow(10, 4), Sym("microm"))  # nopep8
    CONVERSIONS[UnitsLength.MICROMETER][UnitsLength.DECAMETER] = \
        Mul(Pow(10, 7), Sym("microm"))  # nopep8
    CONVERSIONS[UnitsLength.MICROMETER][UnitsLength.DECIMETER] = \
        Mul(Pow(10, 5), Sym("microm"))  # nopep8
    CONVERSIONS[UnitsLength.MICROMETER][UnitsLength.EXAMETER] = \
        Mul(Pow(10, 24), Sym("microm"))  # nopep8
    CONVERSIONS[UnitsLength.MICROMETER][UnitsLength.FEMTOMETER] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("microm"))  # nopep8
    CONVERSIONS[UnitsLength.MICROMETER][UnitsLength.FOOT] = \
        Mul(Int(304800), Sym("microm"))  # nopep8
    CONVERSIONS[UnitsLength.MICROMETER][UnitsLength.GIGAMETER] = \
        Mul(Pow(10, 15), Sym("microm"))  # nopep8
    CONVERSIONS[UnitsLength.MICROMETER][UnitsLength.HECTOMETER] = \
        Mul(Pow(10, 8), Sym("microm"))  # nopep8
    CONVERSIONS[UnitsLength.MICROMETER][UnitsLength.INCH] = \
        Mul(Int(25400), Sym("microm"))  # nopep8
    CONVERSIONS[UnitsLength.MICROMETER][UnitsLength.KILOMETER] = \
        Mul(Pow(10, 9), Sym("microm"))  # nopep8
    CONVERSIONS[UnitsLength.MICROMETER][UnitsLength.LIGHTYEAR] = \
        Mul(Mul(Int("94607304725808"), Pow(10, 8)), Sym("microm"))  # nopep8
    CONVERSIONS[UnitsLength.MICROMETER][UnitsLength.LINE] = \
        Mul(Rat(Int(6350), Int(3)), Sym("microm"))  # nopep8
    CONVERSIONS[UnitsLength.MICROMETER][UnitsLength.MEGAMETER] = \
        Mul(Pow(10, 12), Sym("microm"))  # nopep8
    CONVERSIONS[UnitsLength.MICROMETER][UnitsLength.METER] = \
        Mul(Pow(10, 6), Sym("microm"))  # nopep8
    CONVERSIONS[UnitsLength.MICROMETER][UnitsLength.MILE] = \
        Mul(Int(1609344000), Sym("microm"))  # nopep8
    CONVERSIONS[UnitsLength.MICROMETER][UnitsLength.MILLIMETER] = \
        Mul(Int(1000), Sym("microm"))  # nopep8
    CONVERSIONS[UnitsLength.MICROMETER][UnitsLength.NANOMETER] = \
        Mul(Rat(Int(1), Int(1000)), Sym("microm"))  # nopep8
    CONVERSIONS[UnitsLength.MICROMETER][UnitsLength.PARSEC] = \
        Mul(Mul(Int(30856776), Pow(10, 15)), Sym("microm"))  # nopep8
    CONVERSIONS[UnitsLength.MICROMETER][UnitsLength.PETAMETER] = \
        Mul(Pow(10, 21), Sym("microm"))  # nopep8
    CONVERSIONS[UnitsLength.MICROMETER][UnitsLength.PICOMETER] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("microm"))  # nopep8
    CONVERSIONS[UnitsLength.MICROMETER][UnitsLength.POINT] = \
        Mul(Rat(Int(3175), Int(9)), Sym("microm"))  # nopep8
    CONVERSIONS[UnitsLength.MICROMETER][UnitsLength.TERAMETER] = \
        Mul(Pow(10, 18), Sym("microm"))  # nopep8
    CONVERSIONS[UnitsLength.MICROMETER][UnitsLength.THOU] = \
        Mul(Rat(Int(127), Int(5)), Sym("microm"))  # nopep8
    CONVERSIONS[UnitsLength.MICROMETER][UnitsLength.YARD] = \
        Mul(Int(914400), Sym("microm"))  # nopep8
    CONVERSIONS[UnitsLength.MICROMETER][UnitsLength.YOCTOMETER] = \
        Mul(Rat(Int(1), Pow(10, 18)), Sym("microm"))  # nopep8
    CONVERSIONS[UnitsLength.MICROMETER][UnitsLength.YOTTAMETER] = \
        Mul(Pow(10, 30), Sym("microm"))  # nopep8
    CONVERSIONS[UnitsLength.MICROMETER][UnitsLength.ZEPTOMETER] = \
        Mul(Rat(Int(1), Pow(10, 15)), Sym("microm"))  # nopep8
    CONVERSIONS[UnitsLength.MICROMETER][UnitsLength.ZETTAMETER] = \
        Mul(Pow(10, 27), Sym("microm"))  # nopep8
    CONVERSIONS[UnitsLength.MILE][UnitsLength.ANGSTROM] = \
        Mul(Rat(Int(1), Mul(Int(1609344), Pow(10, 7))), Sym("mi"))  # nopep8
    CONVERSIONS[UnitsLength.MILE][UnitsLength.ASTRONOMICALUNIT] = \
        Mul(Rat(Int("1558311153125"), Int(16764)), Sym("mi"))  # nopep8
    CONVERSIONS[UnitsLength.MILE][UnitsLength.ATTOMETER] = \
        Mul(Rat(Int(1), Mul(Int(1609344), Pow(10, 15))), Sym("mi"))  # nopep8
    CONVERSIONS[UnitsLength.MILE][UnitsLength.CENTIMETER] = \
        Mul(Rat(Int(5), Int(804672)), Sym("mi"))  # nopep8
    CONVERSIONS[UnitsLength.MILE][UnitsLength.DECAMETER] = \
        Mul(Rat(Int(625), Int(100584)), Sym("mi"))  # nopep8
    CONVERSIONS[UnitsLength.MILE][UnitsLength.DECIMETER] = \
        Mul(Rat(Int(25), Int(402336)), Sym("mi"))  # nopep8
    CONVERSIONS[UnitsLength.MILE][UnitsLength.EXAMETER] = \
        Mul(Rat(Mul(Int(78125), Pow(10, 14)), Int(12573)), Sym("mi"))  # nopep8
    CONVERSIONS[UnitsLength.MILE][UnitsLength.FEMTOMETER] = \
        Mul(Rat(Int(1), Mul(Int(1609344), Pow(10, 12))), Sym("mi"))  # nopep8
    CONVERSIONS[UnitsLength.MILE][UnitsLength.FOOT] = \
        Mul(Rat(Int(1), Int(5280)), Sym("mi"))  # nopep8
    CONVERSIONS[UnitsLength.MILE][UnitsLength.GIGAMETER] = \
        Mul(Rat(Mul(Int(78125), Pow(10, 5)), Int(12573)), Sym("mi"))  # nopep8
    CONVERSIONS[UnitsLength.MILE][UnitsLength.HECTOMETER] = \
        Mul(Rat(Int(3125), Int(50292)), Sym("mi"))  # nopep8
    CONVERSIONS[UnitsLength.MILE][UnitsLength.INCH] = \
        Mul(Rat(Int(1), Int(63360)), Sym("mi"))  # nopep8
    CONVERSIONS[UnitsLength.MILE][UnitsLength.KILOMETER] = \
        Mul(Rat(Int(15625), Int(25146)), Sym("mi"))  # nopep8
    CONVERSIONS[UnitsLength.MILE][UnitsLength.LIGHTYEAR] = \
        Mul(Rat(Int("8212439646337500"), Int(1397)), Sym("mi"))  # nopep8
    CONVERSIONS[UnitsLength.MILE][UnitsLength.LINE] = \
        Mul(Rat(Int(1), Int(760320)), Sym("mi"))  # nopep8
    CONVERSIONS[UnitsLength.MILE][UnitsLength.MEGAMETER] = \
        Mul(Rat(Int(7812500), Int(12573)), Sym("mi"))  # nopep8
    CONVERSIONS[UnitsLength.MILE][UnitsLength.METER] = \
        Mul(Rat(Int(125), Int(201168)), Sym("mi"))  # nopep8
    CONVERSIONS[UnitsLength.MILE][UnitsLength.MICROMETER] = \
        Mul(Rat(Int(1), Int(1609344000)), Sym("mi"))  # nopep8
    CONVERSIONS[UnitsLength.MILE][UnitsLength.MILLIMETER] = \
        Mul(Rat(Int(1), Int(1609344)), Sym("mi"))  # nopep8
    CONVERSIONS[UnitsLength.MILE][UnitsLength.NANOMETER] = \
        Mul(Rat(Int(1), Mul(Int(1609344), Pow(10, 6))), Sym("mi"))  # nopep8
    CONVERSIONS[UnitsLength.MILE][UnitsLength.PARSEC] = \
        Mul(Rat(Mul(Int(803561875), Pow(10, 8)), Int(4191)), Sym("mi"))  # nopep8
    CONVERSIONS[UnitsLength.MILE][UnitsLength.PETAMETER] = \
        Mul(Rat(Mul(Int(78125), Pow(10, 11)), Int(12573)), Sym("mi"))  # nopep8
    CONVERSIONS[UnitsLength.MILE][UnitsLength.PICOMETER] = \
        Mul(Rat(Int(1), Mul(Int(1609344), Pow(10, 9))), Sym("mi"))  # nopep8
    CONVERSIONS[UnitsLength.MILE][UnitsLength.POINT] = \
        Mul(Rat(Int(1), Int(4561920)), Sym("mi"))  # nopep8
    CONVERSIONS[UnitsLength.MILE][UnitsLength.TERAMETER] = \
        Mul(Rat(Mul(Int(78125), Pow(10, 8)), Int(12573)), Sym("mi"))  # nopep8
    CONVERSIONS[UnitsLength.MILE][UnitsLength.THOU] = \
        Mul(Rat(Int(1), Mul(Int(6336), Pow(10, 4))), Sym("mi"))  # nopep8
    CONVERSIONS[UnitsLength.MILE][UnitsLength.YARD] = \
        Mul(Rat(Int(1), Int(1760)), Sym("mi"))  # nopep8
    CONVERSIONS[UnitsLength.MILE][UnitsLength.YOCTOMETER] = \
        Mul(Rat(Int(1), Mul(Int(1609344), Pow(10, 21))), Sym("mi"))  # nopep8
    CONVERSIONS[UnitsLength.MILE][UnitsLength.YOTTAMETER] = \
        Mul(Rat(Mul(Int(78125), Pow(10, 20)), Int(12573)), Sym("mi"))  # nopep8
    CONVERSIONS[UnitsLength.MILE][UnitsLength.ZEPTOMETER] = \
        Mul(Rat(Int(1), Mul(Int(1609344), Pow(10, 18))), Sym("mi"))  # nopep8
    CONVERSIONS[UnitsLength.MILE][UnitsLength.ZETTAMETER] = \
        Mul(Rat(Mul(Int(78125), Pow(10, 17)), Int(12573)), Sym("mi"))  # nopep8
    CONVERSIONS[UnitsLength.MILLIMETER][UnitsLength.ANGSTROM] = \
        Mul(Rat(Int(1), Pow(10, 7)), Sym("millim"))  # nopep8
    CONVERSIONS[UnitsLength.MILLIMETER][UnitsLength.ASTRONOMICALUNIT] = \
        Mul(Mul(Int(1495978707), Pow(10, 5)), Sym("millim"))  # nopep8
    CONVERSIONS[UnitsLength.MILLIMETER][UnitsLength.ATTOMETER] = \
        Mul(Rat(Int(1), Pow(10, 15)), Sym("millim"))  # nopep8
    CONVERSIONS[UnitsLength.MILLIMETER][UnitsLength.CENTIMETER] = \
        Mul(Int(10), Sym("millim"))  # nopep8
    CONVERSIONS[UnitsLength.MILLIMETER][UnitsLength.DECAMETER] = \
        Mul(Pow(10, 4), Sym("millim"))  # nopep8
    CONVERSIONS[UnitsLength.MILLIMETER][UnitsLength.DECIMETER] = \
        Mul(Int(100), Sym("millim"))  # nopep8
    CONVERSIONS[UnitsLength.MILLIMETER][UnitsLength.EXAMETER] = \
        Mul(Pow(10, 21), Sym("millim"))  # nopep8
    CONVERSIONS[UnitsLength.MILLIMETER][UnitsLength.FEMTOMETER] = \
        Mul(Rat(Int(1), Pow(10, 12)), Sym("millim"))  # nopep8
    CONVERSIONS[UnitsLength.MILLIMETER][UnitsLength.FOOT] = \
        Mul(Rat(Int(1524), Int(5)), Sym("millim"))  # nopep8
    CONVERSIONS[UnitsLength.MILLIMETER][UnitsLength.GIGAMETER] = \
        Mul(Pow(10, 12), Sym("millim"))  # nopep8
    CONVERSIONS[UnitsLength.MILLIMETER][UnitsLength.HECTOMETER] = \
        Mul(Pow(10, 5), Sym("millim"))  # nopep8
    CONVERSIONS[UnitsLength.MILLIMETER][UnitsLength.INCH] = \
        Mul(Rat(Int(127), Int(5)), Sym("millim"))  # nopep8
    CONVERSIONS[UnitsLength.MILLIMETER][UnitsLength.KILOMETER] = \
        Mul(Pow(10, 6), Sym("millim"))  # nopep8
    CONVERSIONS[UnitsLength.MILLIMETER][UnitsLength.LIGHTYEAR] = \
        Mul(Mul(Int("94607304725808"), Pow(10, 5)), Sym("millim"))  # nopep8
    CONVERSIONS[UnitsLength.MILLIMETER][UnitsLength.LINE] = \
        Mul(Rat(Int(127), Int(60)), Sym("millim"))  # nopep8
    CONVERSIONS[UnitsLength.MILLIMETER][UnitsLength.MEGAMETER] = \
        Mul(Pow(10, 9), Sym("millim"))  # nopep8
    CONVERSIONS[UnitsLength.MILLIMETER][UnitsLength.METER] = \
        Mul(Int(1000), Sym("millim"))  # nopep8
    CONVERSIONS[UnitsLength.MILLIMETER][UnitsLength.MICROMETER] = \
        Mul(Rat(Int(1), Int(1000)), Sym("millim"))  # nopep8
    CONVERSIONS[UnitsLength.MILLIMETER][UnitsLength.MILE] = \
        Mul(Int(1609344), Sym("millim"))  # nopep8
    CONVERSIONS[UnitsLength.MILLIMETER][UnitsLength.NANOMETER] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("millim"))  # nopep8
    CONVERSIONS[UnitsLength.MILLIMETER][UnitsLength.PARSEC] = \
        Mul(Mul(Int(30856776), Pow(10, 12)), Sym("millim"))  # nopep8
    CONVERSIONS[UnitsLength.MILLIMETER][UnitsLength.PETAMETER] = \
        Mul(Pow(10, 18), Sym("millim"))  # nopep8
    CONVERSIONS[UnitsLength.MILLIMETER][UnitsLength.PICOMETER] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("millim"))  # nopep8
    CONVERSIONS[UnitsLength.MILLIMETER][UnitsLength.POINT] = \
        Mul(Rat(Int(127), Int(360)), Sym("millim"))  # nopep8
    CONVERSIONS[UnitsLength.MILLIMETER][UnitsLength.TERAMETER] = \
        Mul(Pow(10, 15), Sym("millim"))  # nopep8
    CONVERSIONS[UnitsLength.MILLIMETER][UnitsLength.THOU] = \
        Mul(Rat(Int(127), Int(5000)), Sym("millim"))  # nopep8
    CONVERSIONS[UnitsLength.MILLIMETER][UnitsLength.YARD] = \
        Mul(Rat(Int(4572), Int(5)), Sym("millim"))  # nopep8
    CONVERSIONS[UnitsLength.MILLIMETER][UnitsLength.YOCTOMETER] = \
        Mul(Rat(Int(1), Pow(10, 21)), Sym("millim"))  # nopep8
    CONVERSIONS[UnitsLength.MILLIMETER][UnitsLength.YOTTAMETER] = \
        Mul(Pow(10, 27), Sym("millim"))  # nopep8
    CONVERSIONS[UnitsLength.MILLIMETER][UnitsLength.ZEPTOMETER] = \
        Mul(Rat(Int(1), Pow(10, 18)), Sym("millim"))  # nopep8
    CONVERSIONS[UnitsLength.MILLIMETER][UnitsLength.ZETTAMETER] = \
        Mul(Pow(10, 24), Sym("millim"))  # nopep8
    CONVERSIONS[UnitsLength.NANOMETER][UnitsLength.ANGSTROM] = \
        Mul(Rat(Int(1), Int(10)), Sym("nanom"))  # nopep8
    CONVERSIONS[UnitsLength.NANOMETER][UnitsLength.ASTRONOMICALUNIT] = \
        Mul(Mul(Int(1495978707), Pow(10, 11)), Sym("nanom"))  # nopep8
    CONVERSIONS[UnitsLength.NANOMETER][UnitsLength.ATTOMETER] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("nanom"))  # nopep8
    CONVERSIONS[UnitsLength.NANOMETER][UnitsLength.CENTIMETER] = \
        Mul(Pow(10, 7), Sym("nanom"))  # nopep8
    CONVERSIONS[UnitsLength.NANOMETER][UnitsLength.DECAMETER] = \
        Mul(Pow(10, 10), Sym("nanom"))  # nopep8
    CONVERSIONS[UnitsLength.NANOMETER][UnitsLength.DECIMETER] = \
        Mul(Pow(10, 8), Sym("nanom"))  # nopep8
    CONVERSIONS[UnitsLength.NANOMETER][UnitsLength.EXAMETER] = \
        Mul(Pow(10, 27), Sym("nanom"))  # nopep8
    CONVERSIONS[UnitsLength.NANOMETER][UnitsLength.FEMTOMETER] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("nanom"))  # nopep8
    CONVERSIONS[UnitsLength.NANOMETER][UnitsLength.FOOT] = \
        Mul(Mul(Int(3048), Pow(10, 5)), Sym("nanom"))  # nopep8
    CONVERSIONS[UnitsLength.NANOMETER][UnitsLength.GIGAMETER] = \
        Mul(Pow(10, 18), Sym("nanom"))  # nopep8
    CONVERSIONS[UnitsLength.NANOMETER][UnitsLength.HECTOMETER] = \
        Mul(Pow(10, 11), Sym("nanom"))  # nopep8
    CONVERSIONS[UnitsLength.NANOMETER][UnitsLength.INCH] = \
        Mul(Mul(Int(254), Pow(10, 5)), Sym("nanom"))  # nopep8
    CONVERSIONS[UnitsLength.NANOMETER][UnitsLength.KILOMETER] = \
        Mul(Pow(10, 12), Sym("nanom"))  # nopep8
    CONVERSIONS[UnitsLength.NANOMETER][UnitsLength.LIGHTYEAR] = \
        Mul(Mul(Int("94607304725808"), Pow(10, 11)), Sym("nanom"))  # nopep8
    CONVERSIONS[UnitsLength.NANOMETER][UnitsLength.LINE] = \
        Mul(Rat(Mul(Int(635), Pow(10, 4)), Int(3)), Sym("nanom"))  # nopep8
    CONVERSIONS[UnitsLength.NANOMETER][UnitsLength.MEGAMETER] = \
        Mul(Pow(10, 15), Sym("nanom"))  # nopep8
    CONVERSIONS[UnitsLength.NANOMETER][UnitsLength.METER] = \
        Mul(Pow(10, 9), Sym("nanom"))  # nopep8
    CONVERSIONS[UnitsLength.NANOMETER][UnitsLength.MICROMETER] = \
        Mul(Int(1000), Sym("nanom"))  # nopep8
    CONVERSIONS[UnitsLength.NANOMETER][UnitsLength.MILE] = \
        Mul(Mul(Int(1609344), Pow(10, 6)), Sym("nanom"))  # nopep8
    CONVERSIONS[UnitsLength.NANOMETER][UnitsLength.MILLIMETER] = \
        Mul(Pow(10, 6), Sym("nanom"))  # nopep8
    CONVERSIONS[UnitsLength.NANOMETER][UnitsLength.PARSEC] = \
        Mul(Mul(Int(30856776), Pow(10, 18)), Sym("nanom"))  # nopep8
    CONVERSIONS[UnitsLength.NANOMETER][UnitsLength.PETAMETER] = \
        Mul(Pow(10, 24), Sym("nanom"))  # nopep8
    CONVERSIONS[UnitsLength.NANOMETER][UnitsLength.PICOMETER] = \
        Mul(Rat(Int(1), Int(1000)), Sym("nanom"))  # nopep8
    CONVERSIONS[UnitsLength.NANOMETER][UnitsLength.POINT] = \
        Mul(Rat(Int(3175000), Int(9)), Sym("nanom"))  # nopep8
    CONVERSIONS[UnitsLength.NANOMETER][UnitsLength.TERAMETER] = \
        Mul(Pow(10, 21), Sym("nanom"))  # nopep8
    CONVERSIONS[UnitsLength.NANOMETER][UnitsLength.THOU] = \
        Mul(Int(25400), Sym("nanom"))  # nopep8
    CONVERSIONS[UnitsLength.NANOMETER][UnitsLength.YARD] = \
        Mul(Mul(Int(9144), Pow(10, 5)), Sym("nanom"))  # nopep8
    CONVERSIONS[UnitsLength.NANOMETER][UnitsLength.YOCTOMETER] = \
        Mul(Rat(Int(1), Pow(10, 15)), Sym("nanom"))  # nopep8
    CONVERSIONS[UnitsLength.NANOMETER][UnitsLength.YOTTAMETER] = \
        Mul(Pow(10, 33), Sym("nanom"))  # nopep8
    CONVERSIONS[UnitsLength.NANOMETER][UnitsLength.ZEPTOMETER] = \
        Mul(Rat(Int(1), Pow(10, 12)), Sym("nanom"))  # nopep8
    CONVERSIONS[UnitsLength.NANOMETER][UnitsLength.ZETTAMETER] = \
        Mul(Pow(10, 30), Sym("nanom"))  # nopep8
    CONVERSIONS[UnitsLength.PARSEC][UnitsLength.ANGSTROM] = \
        Mul(Rat(Int(1), Mul(Int(30856776), Pow(10, 19))), Sym("pc"))  # nopep8
    CONVERSIONS[UnitsLength.PARSEC][UnitsLength.ASTRONOMICALUNIT] = \
        Mul(Rat(Int(498659569), Mul(Int(10285592), Pow(10, 7))), Sym("pc"))  # nopep8
    CONVERSIONS[UnitsLength.PARSEC][UnitsLength.ATTOMETER] = \
        Mul(Rat(Int(1), Mul(Int(30856776), Pow(10, 27))), Sym("pc"))  # nopep8
    CONVERSIONS[UnitsLength.PARSEC][UnitsLength.CENTIMETER] = \
        Mul(Rat(Int(1), Mul(Int(30856776), Pow(10, 11))), Sym("pc"))  # nopep8
    CONVERSIONS[UnitsLength.PARSEC][UnitsLength.DECAMETER] = \
        Mul(Rat(Int(1), Mul(Int(30856776), Pow(10, 8))), Sym("pc"))  # nopep8
    CONVERSIONS[UnitsLength.PARSEC][UnitsLength.DECIMETER] = \
        Mul(Rat(Int(1), Mul(Int(30856776), Pow(10, 10))), Sym("pc"))  # nopep8
    CONVERSIONS[UnitsLength.PARSEC][UnitsLength.EXAMETER] = \
        Mul(Rat(Mul(Int(125), Pow(10, 6)), Int(3857097)), Sym("pc"))  # nopep8
    CONVERSIONS[UnitsLength.PARSEC][UnitsLength.FEMTOMETER] = \
        Mul(Rat(Int(1), Mul(Int(30856776), Pow(10, 24))), Sym("pc"))  # nopep8
    CONVERSIONS[UnitsLength.PARSEC][UnitsLength.FOOT] = \
        Mul(Rat(Int(127), Mul(Int(1285699), Pow(10, 13))), Sym("pc"))  # nopep8
    CONVERSIONS[UnitsLength.PARSEC][UnitsLength.GIGAMETER] = \
        Mul(Rat(Int(1), Int(30856776)), Sym("pc"))  # nopep8
    CONVERSIONS[UnitsLength.PARSEC][UnitsLength.HECTOMETER] = \
        Mul(Rat(Int(1), Mul(Int(30856776), Pow(10, 7))), Sym("pc"))  # nopep8
    CONVERSIONS[UnitsLength.PARSEC][UnitsLength.INCH] = \
        Mul(Rat(Int(127), Mul(Int(15428388), Pow(10, 13))), Sym("pc"))  # nopep8
    CONVERSIONS[UnitsLength.PARSEC][UnitsLength.KILOMETER] = \
        Mul(Rat(Int(1), Mul(Int(30856776), Pow(10, 6))), Sym("pc"))  # nopep8
    CONVERSIONS[UnitsLength.PARSEC][UnitsLength.LIGHTYEAR] = \
        Mul(Rat(Int("1970985515121"), Mul(Int(6428495), Pow(10, 6))), Sym("pc"))  # nopep8
    CONVERSIONS[UnitsLength.PARSEC][UnitsLength.LINE] = \
        Mul(Rat(Int(127), Mul(Int(185140656), Pow(10, 13))), Sym("pc"))  # nopep8
    CONVERSIONS[UnitsLength.PARSEC][UnitsLength.MEGAMETER] = \
        Mul(Rat(Int(1), Int("30856776000")), Sym("pc"))  # nopep8
    CONVERSIONS[UnitsLength.PARSEC][UnitsLength.METER] = \
        Mul(Rat(Int(1), Mul(Int(30856776), Pow(10, 9))), Sym("pc"))  # nopep8
    CONVERSIONS[UnitsLength.PARSEC][UnitsLength.MICROMETER] = \
        Mul(Rat(Int(1), Mul(Int(30856776), Pow(10, 15))), Sym("pc"))  # nopep8
    CONVERSIONS[UnitsLength.PARSEC][UnitsLength.MILE] = \
        Mul(Rat(Int(4191), Mul(Int(803561875), Pow(10, 8))), Sym("pc"))  # nopep8
    CONVERSIONS[UnitsLength.PARSEC][UnitsLength.MILLIMETER] = \
        Mul(Rat(Int(1), Mul(Int(30856776), Pow(10, 12))), Sym("pc"))  # nopep8
    CONVERSIONS[UnitsLength.PARSEC][UnitsLength.NANOMETER] = \
        Mul(Rat(Int(1), Mul(Int(30856776), Pow(10, 18))), Sym("pc"))  # nopep8
    CONVERSIONS[UnitsLength.PARSEC][UnitsLength.PETAMETER] = \
        Mul(Rat(Int(125000), Int(3857097)), Sym("pc"))  # nopep8
    CONVERSIONS[UnitsLength.PARSEC][UnitsLength.PICOMETER] = \
        Mul(Rat(Int(1), Mul(Int(30856776), Pow(10, 21))), Sym("pc"))  # nopep8
    CONVERSIONS[UnitsLength.PARSEC][UnitsLength.POINT] = \
        Mul(Rat(Int(127), Mul(Int(1110843936), Pow(10, 13))), Sym("pc"))  # nopep8
    CONVERSIONS[UnitsLength.PARSEC][UnitsLength.TERAMETER] = \
        Mul(Rat(Int(125), Int(3857097)), Sym("pc"))  # nopep8
    CONVERSIONS[UnitsLength.PARSEC][UnitsLength.THOU] = \
        Mul(Rat(Int(127), Mul(Int(15428388), Pow(10, 16))), Sym("pc"))  # nopep8
    CONVERSIONS[UnitsLength.PARSEC][UnitsLength.YARD] = \
        Mul(Rat(Int(381), Mul(Int(1285699), Pow(10, 13))), Sym("pc"))  # nopep8
    CONVERSIONS[UnitsLength.PARSEC][UnitsLength.YOCTOMETER] = \
        Mul(Rat(Int(1), Mul(Int(30856776), Pow(10, 33))), Sym("pc"))  # nopep8
    CONVERSIONS[UnitsLength.PARSEC][UnitsLength.YOTTAMETER] = \
        Mul(Rat(Mul(Int(125), Pow(10, 12)), Int(3857097)), Sym("pc"))  # nopep8
    CONVERSIONS[UnitsLength.PARSEC][UnitsLength.ZEPTOMETER] = \
        Mul(Rat(Int(1), Mul(Int(30856776), Pow(10, 30))), Sym("pc"))  # nopep8
    CONVERSIONS[UnitsLength.PARSEC][UnitsLength.ZETTAMETER] = \
        Mul(Rat(Mul(Int(125), Pow(10, 9)), Int(3857097)), Sym("pc"))  # nopep8
    CONVERSIONS[UnitsLength.PETAMETER][UnitsLength.ANGSTROM] = \
        Mul(Rat(Int(1), Pow(10, 25)), Sym("petam"))  # nopep8
    CONVERSIONS[UnitsLength.PETAMETER][UnitsLength.ASTRONOMICALUNIT] = \
        Mul(Rat(Int(1495978707), Pow(10, 13)), Sym("petam"))  # nopep8
    CONVERSIONS[UnitsLength.PETAMETER][UnitsLength.ATTOMETER] = \
        Mul(Rat(Int(1), Pow(10, 33)), Sym("petam"))  # nopep8
    CONVERSIONS[UnitsLength.PETAMETER][UnitsLength.CENTIMETER] = \
        Mul(Rat(Int(1), Pow(10, 17)), Sym("petam"))  # nopep8
    CONVERSIONS[UnitsLength.PETAMETER][UnitsLength.DECAMETER] = \
        Mul(Rat(Int(1), Pow(10, 14)), Sym("petam"))  # nopep8
    CONVERSIONS[UnitsLength.PETAMETER][UnitsLength.DECIMETER] = \
        Mul(Rat(Int(1), Pow(10, 16)), Sym("petam"))  # nopep8
    CONVERSIONS[UnitsLength.PETAMETER][UnitsLength.EXAMETER] = \
        Mul(Int(1000), Sym("petam"))  # nopep8
    CONVERSIONS[UnitsLength.PETAMETER][UnitsLength.FEMTOMETER] = \
        Mul(Rat(Int(1), Pow(10, 30)), Sym("petam"))  # nopep8
    CONVERSIONS[UnitsLength.PETAMETER][UnitsLength.FOOT] = \
        Mul(Rat(Int(381), Mul(Int(125), Pow(10, 16))), Sym("petam"))  # nopep8
    CONVERSIONS[UnitsLength.PETAMETER][UnitsLength.GIGAMETER] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("petam"))  # nopep8
    CONVERSIONS[UnitsLength.PETAMETER][UnitsLength.HECTOMETER] = \
        Mul(Rat(Int(1), Pow(10, 13)), Sym("petam"))  # nopep8
    CONVERSIONS[UnitsLength.PETAMETER][UnitsLength.INCH] = \
        Mul(Rat(Int(127), Mul(Int(5), Pow(10, 18))), Sym("petam"))  # nopep8
    CONVERSIONS[UnitsLength.PETAMETER][UnitsLength.KILOMETER] = \
        Mul(Rat(Int(1), Pow(10, 12)), Sym("petam"))  # nopep8
    CONVERSIONS[UnitsLength.PETAMETER][UnitsLength.LIGHTYEAR] = \
        Mul(Rat(Int("5912956545363"), Mul(Int(625), Pow(10, 9))), Sym("petam"))  # nopep8
    CONVERSIONS[UnitsLength.PETAMETER][UnitsLength.LINE] = \
        Mul(Rat(Int(127), Mul(Int(6), Pow(10, 19))), Sym("petam"))  # nopep8
    CONVERSIONS[UnitsLength.PETAMETER][UnitsLength.MEGAMETER] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("petam"))  # nopep8
    CONVERSIONS[UnitsLength.PETAMETER][UnitsLength.METER] = \
        Mul(Rat(Int(1), Pow(10, 15)), Sym("petam"))  # nopep8
    CONVERSIONS[UnitsLength.PETAMETER][UnitsLength.MICROMETER] = \
        Mul(Rat(Int(1), Pow(10, 21)), Sym("petam"))  # nopep8
    CONVERSIONS[UnitsLength.PETAMETER][UnitsLength.MILE] = \
        Mul(Rat(Int(12573), Mul(Int(78125), Pow(10, 11))), Sym("petam"))  # nopep8
    CONVERSIONS[UnitsLength.PETAMETER][UnitsLength.MILLIMETER] = \
        Mul(Rat(Int(1), Pow(10, 18)), Sym("petam"))  # nopep8
    CONVERSIONS[UnitsLength.PETAMETER][UnitsLength.NANOMETER] = \
        Mul(Rat(Int(1), Pow(10, 24)), Sym("petam"))  # nopep8
    CONVERSIONS[UnitsLength.PETAMETER][UnitsLength.PARSEC] = \
        Mul(Rat(Int(3857097), Int(125000)), Sym("petam"))  # nopep8
    CONVERSIONS[UnitsLength.PETAMETER][UnitsLength.PICOMETER] = \
        Mul(Rat(Int(1), Pow(10, 27)), Sym("petam"))  # nopep8
    CONVERSIONS[UnitsLength.PETAMETER][UnitsLength.POINT] = \
        Mul(Rat(Int(127), Mul(Int(36), Pow(10, 19))), Sym("petam"))  # nopep8
    CONVERSIONS[UnitsLength.PETAMETER][UnitsLength.TERAMETER] = \
        Mul(Rat(Int(1), Int(1000)), Sym("petam"))  # nopep8
    CONVERSIONS[UnitsLength.PETAMETER][UnitsLength.THOU] = \
        Mul(Rat(Int(127), Mul(Int(5), Pow(10, 21))), Sym("petam"))  # nopep8
    CONVERSIONS[UnitsLength.PETAMETER][UnitsLength.YARD] = \
        Mul(Rat(Int(1143), Mul(Int(125), Pow(10, 16))), Sym("petam"))  # nopep8
    CONVERSIONS[UnitsLength.PETAMETER][UnitsLength.YOCTOMETER] = \
        Mul(Rat(Int(1), Pow(10, 39)), Sym("petam"))  # nopep8
    CONVERSIONS[UnitsLength.PETAMETER][UnitsLength.YOTTAMETER] = \
        Mul(Pow(10, 9), Sym("petam"))  # nopep8
    CONVERSIONS[UnitsLength.PETAMETER][UnitsLength.ZEPTOMETER] = \
        Mul(Rat(Int(1), Pow(10, 36)), Sym("petam"))  # nopep8
    CONVERSIONS[UnitsLength.PETAMETER][UnitsLength.ZETTAMETER] = \
        Mul(Pow(10, 6), Sym("petam"))  # nopep8
    CONVERSIONS[UnitsLength.PICOMETER][UnitsLength.ANGSTROM] = \
        Mul(Int(100), Sym("picom"))  # nopep8
    CONVERSIONS[UnitsLength.PICOMETER][UnitsLength.ASTRONOMICALUNIT] = \
        Mul(Mul(Int(1495978707), Pow(10, 14)), Sym("picom"))  # nopep8
    CONVERSIONS[UnitsLength.PICOMETER][UnitsLength.ATTOMETER] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("picom"))  # nopep8
    CONVERSIONS[UnitsLength.PICOMETER][UnitsLength.CENTIMETER] = \
        Mul(Pow(10, 10), Sym("picom"))  # nopep8
    CONVERSIONS[UnitsLength.PICOMETER][UnitsLength.DECAMETER] = \
        Mul(Pow(10, 13), Sym("picom"))  # nopep8
    CONVERSIONS[UnitsLength.PICOMETER][UnitsLength.DECIMETER] = \
        Mul(Pow(10, 11), Sym("picom"))  # nopep8
    CONVERSIONS[UnitsLength.PICOMETER][UnitsLength.EXAMETER] = \
        Mul(Pow(10, 30), Sym("picom"))  # nopep8
    CONVERSIONS[UnitsLength.PICOMETER][UnitsLength.FEMTOMETER] = \
        Mul(Rat(Int(1), Int(1000)), Sym("picom"))  # nopep8
    CONVERSIONS[UnitsLength.PICOMETER][UnitsLength.FOOT] = \
        Mul(Mul(Int(3048), Pow(10, 8)), Sym("picom"))  # nopep8
    CONVERSIONS[UnitsLength.PICOMETER][UnitsLength.GIGAMETER] = \
        Mul(Pow(10, 21), Sym("picom"))  # nopep8
    CONVERSIONS[UnitsLength.PICOMETER][UnitsLength.HECTOMETER] = \
        Mul(Pow(10, 14), Sym("picom"))  # nopep8
    CONVERSIONS[UnitsLength.PICOMETER][UnitsLength.INCH] = \
        Mul(Mul(Int(254), Pow(10, 8)), Sym("picom"))  # nopep8
    CONVERSIONS[UnitsLength.PICOMETER][UnitsLength.KILOMETER] = \
        Mul(Pow(10, 15), Sym("picom"))  # nopep8
    CONVERSIONS[UnitsLength.PICOMETER][UnitsLength.LIGHTYEAR] = \
        Mul(Mul(Int("94607304725808"), Pow(10, 14)), Sym("picom"))  # nopep8
    CONVERSIONS[UnitsLength.PICOMETER][UnitsLength.LINE] = \
        Mul(Rat(Mul(Int(635), Pow(10, 7)), Int(3)), Sym("picom"))  # nopep8
    CONVERSIONS[UnitsLength.PICOMETER][UnitsLength.MEGAMETER] = \
        Mul(Pow(10, 18), Sym("picom"))  # nopep8
    CONVERSIONS[UnitsLength.PICOMETER][UnitsLength.METER] = \
        Mul(Pow(10, 12), Sym("picom"))  # nopep8
    CONVERSIONS[UnitsLength.PICOMETER][UnitsLength.MICROMETER] = \
        Mul(Pow(10, 6), Sym("picom"))  # nopep8
    CONVERSIONS[UnitsLength.PICOMETER][UnitsLength.MILE] = \
        Mul(Mul(Int(1609344), Pow(10, 9)), Sym("picom"))  # nopep8
    CONVERSIONS[UnitsLength.PICOMETER][UnitsLength.MILLIMETER] = \
        Mul(Pow(10, 9), Sym("picom"))  # nopep8
    CONVERSIONS[UnitsLength.PICOMETER][UnitsLength.NANOMETER] = \
        Mul(Int(1000), Sym("picom"))  # nopep8
    CONVERSIONS[UnitsLength.PICOMETER][UnitsLength.PARSEC] = \
        Mul(Mul(Int(30856776), Pow(10, 21)), Sym("picom"))  # nopep8
    CONVERSIONS[UnitsLength.PICOMETER][UnitsLength.PETAMETER] = \
        Mul(Pow(10, 27), Sym("picom"))  # nopep8
    CONVERSIONS[UnitsLength.PICOMETER][UnitsLength.POINT] = \
        Mul(Rat(Mul(Int(3175), Pow(10, 6)), Int(9)), Sym("picom"))  # nopep8
    CONVERSIONS[UnitsLength.PICOMETER][UnitsLength.TERAMETER] = \
        Mul(Pow(10, 24), Sym("picom"))  # nopep8
    CONVERSIONS[UnitsLength.PICOMETER][UnitsLength.THOU] = \
        Mul(Mul(Int(254), Pow(10, 5)), Sym("picom"))  # nopep8
    CONVERSIONS[UnitsLength.PICOMETER][UnitsLength.YARD] = \
        Mul(Mul(Int(9144), Pow(10, 8)), Sym("picom"))  # nopep8
    CONVERSIONS[UnitsLength.PICOMETER][UnitsLength.YOCTOMETER] = \
        Mul(Rat(Int(1), Pow(10, 12)), Sym("picom"))  # nopep8
    CONVERSIONS[UnitsLength.PICOMETER][UnitsLength.YOTTAMETER] = \
        Mul(Pow(10, 36), Sym("picom"))  # nopep8
    CONVERSIONS[UnitsLength.PICOMETER][UnitsLength.ZEPTOMETER] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("picom"))  # nopep8
    CONVERSIONS[UnitsLength.PICOMETER][UnitsLength.ZETTAMETER] = \
        Mul(Pow(10, 33), Sym("picom"))  # nopep8
    CONVERSIONS[UnitsLength.POINT][UnitsLength.ANGSTROM] = \
        Mul(Rat(Int(9), Mul(Int(3175), Pow(10, 4))), Sym("pt"))  # nopep8
    CONVERSIONS[UnitsLength.POINT][UnitsLength.ASTRONOMICALUNIT] = \
        Mul(Rat(Mul(Int("53855233452"), Pow(10, 6)), Int(127)), Sym("pt"))  # nopep8
    CONVERSIONS[UnitsLength.POINT][UnitsLength.ATTOMETER] = \
        Mul(Rat(Int(9), Mul(Int(3175), Pow(10, 12))), Sym("pt"))  # nopep8
    CONVERSIONS[UnitsLength.POINT][UnitsLength.CENTIMETER] = \
        Mul(Rat(Int(3600), Int(127)), Sym("pt"))  # nopep8
    CONVERSIONS[UnitsLength.POINT][UnitsLength.DECAMETER] = \
        Mul(Rat(Mul(Int(36), Pow(10, 5)), Int(127)), Sym("pt"))  # nopep8
    CONVERSIONS[UnitsLength.POINT][UnitsLength.DECIMETER] = \
        Mul(Rat(Int(36000), Int(127)), Sym("pt"))  # nopep8
    CONVERSIONS[UnitsLength.POINT][UnitsLength.EXAMETER] = \
        Mul(Rat(Mul(Int(36), Pow(10, 22)), Int(127)), Sym("pt"))  # nopep8
    CONVERSIONS[UnitsLength.POINT][UnitsLength.FEMTOMETER] = \
        Mul(Rat(Int(9), Mul(Int(3175), Pow(10, 9))), Sym("pt"))  # nopep8
    CONVERSIONS[UnitsLength.POINT][UnitsLength.FOOT] = \
        Mul(Int(864), Sym("pt"))  # nopep8
    CONVERSIONS[UnitsLength.POINT][UnitsLength.GIGAMETER] = \
        Mul(Rat(Mul(Int(36), Pow(10, 13)), Int(127)), Sym("pt"))  # nopep8
    CONVERSIONS[UnitsLength.POINT][UnitsLength.HECTOMETER] = \
        Mul(Rat(Mul(Int(36), Pow(10, 6)), Int(127)), Sym("pt"))  # nopep8
    CONVERSIONS[UnitsLength.POINT][UnitsLength.INCH] = \
        Mul(Int(72), Sym("pt"))  # nopep8
    CONVERSIONS[UnitsLength.POINT][UnitsLength.KILOMETER] = \
        Mul(Rat(Mul(Int(36), Pow(10, 7)), Int(127)), Sym("pt"))  # nopep8
    CONVERSIONS[UnitsLength.POINT][UnitsLength.LIGHTYEAR] = \
        Mul(Rat(Mul(Int("3405862970129088"), Pow(10, 6)), Int(127)), Sym("pt"))  # nopep8
    CONVERSIONS[UnitsLength.POINT][UnitsLength.LINE] = \
        Mul(Int(6), Sym("pt"))  # nopep8
    CONVERSIONS[UnitsLength.POINT][UnitsLength.MEGAMETER] = \
        Mul(Rat(Mul(Int(36), Pow(10, 10)), Int(127)), Sym("pt"))  # nopep8
    CONVERSIONS[UnitsLength.POINT][UnitsLength.METER] = \
        Mul(Rat(Mul(Int(36), Pow(10, 4)), Int(127)), Sym("pt"))  # nopep8
    CONVERSIONS[UnitsLength.POINT][UnitsLength.MICROMETER] = \
        Mul(Rat(Int(9), Int(3175)), Sym("pt"))  # nopep8
    CONVERSIONS[UnitsLength.POINT][UnitsLength.MILE] = \
        Mul(Int(4561920), Sym("pt"))  # nopep8
    CONVERSIONS[UnitsLength.POINT][UnitsLength.MILLIMETER] = \
        Mul(Rat(Int(360), Int(127)), Sym("pt"))  # nopep8
    CONVERSIONS[UnitsLength.POINT][UnitsLength.NANOMETER] = \
        Mul(Rat(Int(9), Int(3175000)), Sym("pt"))  # nopep8
    CONVERSIONS[UnitsLength.POINT][UnitsLength.PARSEC] = \
        Mul(Rat(Mul(Int(1110843936), Pow(10, 13)), Int(127)), Sym("pt"))  # nopep8
    CONVERSIONS[UnitsLength.POINT][UnitsLength.PETAMETER] = \
        Mul(Rat(Mul(Int(36), Pow(10, 19)), Int(127)), Sym("pt"))  # nopep8
    CONVERSIONS[UnitsLength.POINT][UnitsLength.PICOMETER] = \
        Mul(Rat(Int(9), Mul(Int(3175), Pow(10, 6))), Sym("pt"))  # nopep8
    CONVERSIONS[UnitsLength.POINT][UnitsLength.TERAMETER] = \
        Mul(Rat(Mul(Int(36), Pow(10, 16)), Int(127)), Sym("pt"))  # nopep8
    CONVERSIONS[UnitsLength.POINT][UnitsLength.THOU] = \
        Mul(Rat(Int(9), Int(125)), Sym("pt"))  # nopep8
    CONVERSIONS[UnitsLength.POINT][UnitsLength.YARD] = \
        Mul(Int(2592), Sym("pt"))  # nopep8
    CONVERSIONS[UnitsLength.POINT][UnitsLength.YOCTOMETER] = \
        Mul(Rat(Int(9), Mul(Int(3175), Pow(10, 18))), Sym("pt"))  # nopep8
    CONVERSIONS[UnitsLength.POINT][UnitsLength.YOTTAMETER] = \
        Mul(Rat(Mul(Int(36), Pow(10, 28)), Int(127)), Sym("pt"))  # nopep8
    CONVERSIONS[UnitsLength.POINT][UnitsLength.ZEPTOMETER] = \
        Mul(Rat(Int(9), Mul(Int(3175), Pow(10, 15))), Sym("pt"))  # nopep8
    CONVERSIONS[UnitsLength.POINT][UnitsLength.ZETTAMETER] = \
        Mul(Rat(Mul(Int(36), Pow(10, 25)), Int(127)), Sym("pt"))  # nopep8
    CONVERSIONS[UnitsLength.TERAMETER][UnitsLength.ANGSTROM] = \
        Mul(Rat(Int(1), Pow(10, 22)), Sym("teram"))  # nopep8
    CONVERSIONS[UnitsLength.TERAMETER][UnitsLength.ASTRONOMICALUNIT] = \
        Mul(Rat(Int(1495978707), Pow(10, 10)), Sym("teram"))  # nopep8
    CONVERSIONS[UnitsLength.TERAMETER][UnitsLength.ATTOMETER] = \
        Mul(Rat(Int(1), Pow(10, 30)), Sym("teram"))  # nopep8
    CONVERSIONS[UnitsLength.TERAMETER][UnitsLength.CENTIMETER] = \
        Mul(Rat(Int(1), Pow(10, 14)), Sym("teram"))  # nopep8
    CONVERSIONS[UnitsLength.TERAMETER][UnitsLength.DECAMETER] = \
        Mul(Rat(Int(1), Pow(10, 11)), Sym("teram"))  # nopep8
    CONVERSIONS[UnitsLength.TERAMETER][UnitsLength.DECIMETER] = \
        Mul(Rat(Int(1), Pow(10, 13)), Sym("teram"))  # nopep8
    CONVERSIONS[UnitsLength.TERAMETER][UnitsLength.EXAMETER] = \
        Mul(Pow(10, 6), Sym("teram"))  # nopep8
    CONVERSIONS[UnitsLength.TERAMETER][UnitsLength.FEMTOMETER] = \
        Mul(Rat(Int(1), Pow(10, 27)), Sym("teram"))  # nopep8
    CONVERSIONS[UnitsLength.TERAMETER][UnitsLength.FOOT] = \
        Mul(Rat(Int(381), Mul(Int(125), Pow(10, 13))), Sym("teram"))  # nopep8
    CONVERSIONS[UnitsLength.TERAMETER][UnitsLength.GIGAMETER] = \
        Mul(Rat(Int(1), Int(1000)), Sym("teram"))  # nopep8
    CONVERSIONS[UnitsLength.TERAMETER][UnitsLength.HECTOMETER] = \
        Mul(Rat(Int(1), Pow(10, 10)), Sym("teram"))  # nopep8
    CONVERSIONS[UnitsLength.TERAMETER][UnitsLength.INCH] = \
        Mul(Rat(Int(127), Mul(Int(5), Pow(10, 15))), Sym("teram"))  # nopep8
    CONVERSIONS[UnitsLength.TERAMETER][UnitsLength.KILOMETER] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("teram"))  # nopep8
    CONVERSIONS[UnitsLength.TERAMETER][UnitsLength.LIGHTYEAR] = \
        Mul(Rat(Int("5912956545363"), Mul(Int(625), Pow(10, 6))), Sym("teram"))  # nopep8
    CONVERSIONS[UnitsLength.TERAMETER][UnitsLength.LINE] = \
        Mul(Rat(Int(127), Mul(Int(6), Pow(10, 16))), Sym("teram"))  # nopep8
    CONVERSIONS[UnitsLength.TERAMETER][UnitsLength.MEGAMETER] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("teram"))  # nopep8
    CONVERSIONS[UnitsLength.TERAMETER][UnitsLength.METER] = \
        Mul(Rat(Int(1), Pow(10, 12)), Sym("teram"))  # nopep8
    CONVERSIONS[UnitsLength.TERAMETER][UnitsLength.MICROMETER] = \
        Mul(Rat(Int(1), Pow(10, 18)), Sym("teram"))  # nopep8
    CONVERSIONS[UnitsLength.TERAMETER][UnitsLength.MILE] = \
        Mul(Rat(Int(12573), Mul(Int(78125), Pow(10, 8))), Sym("teram"))  # nopep8
    CONVERSIONS[UnitsLength.TERAMETER][UnitsLength.MILLIMETER] = \
        Mul(Rat(Int(1), Pow(10, 15)), Sym("teram"))  # nopep8
    CONVERSIONS[UnitsLength.TERAMETER][UnitsLength.NANOMETER] = \
        Mul(Rat(Int(1), Pow(10, 21)), Sym("teram"))  # nopep8
    CONVERSIONS[UnitsLength.TERAMETER][UnitsLength.PARSEC] = \
        Mul(Rat(Int(3857097), Int(125)), Sym("teram"))  # nopep8
    CONVERSIONS[UnitsLength.TERAMETER][UnitsLength.PETAMETER] = \
        Mul(Int(1000), Sym("teram"))  # nopep8
    CONVERSIONS[UnitsLength.TERAMETER][UnitsLength.PICOMETER] = \
        Mul(Rat(Int(1), Pow(10, 24)), Sym("teram"))  # nopep8
    CONVERSIONS[UnitsLength.TERAMETER][UnitsLength.POINT] = \
        Mul(Rat(Int(127), Mul(Int(36), Pow(10, 16))), Sym("teram"))  # nopep8
    CONVERSIONS[UnitsLength.TERAMETER][UnitsLength.THOU] = \
        Mul(Rat(Int(127), Mul(Int(5), Pow(10, 18))), Sym("teram"))  # nopep8
    CONVERSIONS[UnitsLength.TERAMETER][UnitsLength.YARD] = \
        Mul(Rat(Int(1143), Mul(Int(125), Pow(10, 13))), Sym("teram"))  # nopep8
    CONVERSIONS[UnitsLength.TERAMETER][UnitsLength.YOCTOMETER] = \
        Mul(Rat(Int(1), Pow(10, 36)), Sym("teram"))  # nopep8
    CONVERSIONS[UnitsLength.TERAMETER][UnitsLength.YOTTAMETER] = \
        Mul(Pow(10, 12), Sym("teram"))  # nopep8
    CONVERSIONS[UnitsLength.TERAMETER][UnitsLength.ZEPTOMETER] = \
        Mul(Rat(Int(1), Pow(10, 33)), Sym("teram"))  # nopep8
    CONVERSIONS[UnitsLength.TERAMETER][UnitsLength.ZETTAMETER] = \
        Mul(Pow(10, 9), Sym("teram"))  # nopep8
    CONVERSIONS[UnitsLength.THOU][UnitsLength.ANGSTROM] = \
        Mul(Rat(Int(1), Int(254000)), Sym("thou"))  # nopep8
    CONVERSIONS[UnitsLength.THOU][UnitsLength.ASTRONOMICALUNIT] = \
        Mul(Rat(Mul(Int("7479893535"), Pow(10, 8)), Int(127)), Sym("thou"))  # nopep8
    CONVERSIONS[UnitsLength.THOU][UnitsLength.ATTOMETER] = \
        Mul(Rat(Int(1), Mul(Int(254), Pow(10, 11))), Sym("thou"))  # nopep8
    CONVERSIONS[UnitsLength.THOU][UnitsLength.CENTIMETER] = \
        Mul(Rat(Mul(Int(5), Pow(10, 4)), Int(127)), Sym("thou"))  # nopep8
    CONVERSIONS[UnitsLength.THOU][UnitsLength.DECAMETER] = \
        Mul(Rat(Mul(Int(5), Pow(10, 7)), Int(127)), Sym("thou"))  # nopep8
    CONVERSIONS[UnitsLength.THOU][UnitsLength.DECIMETER] = \
        Mul(Rat(Mul(Int(5), Pow(10, 5)), Int(127)), Sym("thou"))  # nopep8
    CONVERSIONS[UnitsLength.THOU][UnitsLength.EXAMETER] = \
        Mul(Rat(Mul(Int(5), Pow(10, 24)), Int(127)), Sym("thou"))  # nopep8
    CONVERSIONS[UnitsLength.THOU][UnitsLength.FEMTOMETER] = \
        Mul(Rat(Int(1), Mul(Int(254), Pow(10, 8))), Sym("thou"))  # nopep8
    CONVERSIONS[UnitsLength.THOU][UnitsLength.FOOT] = \
        Mul(Int(12000), Sym("thou"))  # nopep8
    CONVERSIONS[UnitsLength.THOU][UnitsLength.GIGAMETER] = \
        Mul(Rat(Mul(Int(5), Pow(10, 15)), Int(127)), Sym("thou"))  # nopep8
    CONVERSIONS[UnitsLength.THOU][UnitsLength.HECTOMETER] = \
        Mul(Rat(Mul(Int(5), Pow(10, 8)), Int(127)), Sym("thou"))  # nopep8
    CONVERSIONS[UnitsLength.THOU][UnitsLength.INCH] = \
        Mul(Int(1000), Sym("thou"))  # nopep8
    CONVERSIONS[UnitsLength.THOU][UnitsLength.KILOMETER] = \
        Mul(Rat(Mul(Int(5), Pow(10, 9)), Int(127)), Sym("thou"))  # nopep8
    CONVERSIONS[UnitsLength.THOU][UnitsLength.LIGHTYEAR] = \
        Mul(Rat(Mul(Int("47303652362904"), Pow(10, 9)), Int(127)), Sym("thou"))  # nopep8
    CONVERSIONS[UnitsLength.THOU][UnitsLength.LINE] = \
        Mul(Rat(Int(250), Int(3)), Sym("thou"))  # nopep8
    CONVERSIONS[UnitsLength.THOU][UnitsLength.MEGAMETER] = \
        Mul(Rat(Mul(Int(5), Pow(10, 12)), Int(127)), Sym("thou"))  # nopep8
    CONVERSIONS[UnitsLength.THOU][UnitsLength.METER] = \
        Mul(Rat(Mul(Int(5), Pow(10, 6)), Int(127)), Sym("thou"))  # nopep8
    CONVERSIONS[UnitsLength.THOU][UnitsLength.MICROMETER] = \
        Mul(Rat(Int(5), Int(127)), Sym("thou"))  # nopep8
    CONVERSIONS[UnitsLength.THOU][UnitsLength.MILE] = \
        Mul(Mul(Int(6336), Pow(10, 4)), Sym("thou"))  # nopep8
    CONVERSIONS[UnitsLength.THOU][UnitsLength.MILLIMETER] = \
        Mul(Rat(Int(5000), Int(127)), Sym("thou"))  # nopep8
    CONVERSIONS[UnitsLength.THOU][UnitsLength.NANOMETER] = \
        Mul(Rat(Int(1), Int(25400)), Sym("thou"))  # nopep8
    CONVERSIONS[UnitsLength.THOU][UnitsLength.PARSEC] = \
        Mul(Rat(Mul(Int(15428388), Pow(10, 16)), Int(127)), Sym("thou"))  # nopep8
    CONVERSIONS[UnitsLength.THOU][UnitsLength.PETAMETER] = \
        Mul(Rat(Mul(Int(5), Pow(10, 21)), Int(127)), Sym("thou"))  # nopep8
    CONVERSIONS[UnitsLength.THOU][UnitsLength.PICOMETER] = \
        Mul(Rat(Int(1), Mul(Int(254), Pow(10, 5))), Sym("thou"))  # nopep8
    CONVERSIONS[UnitsLength.THOU][UnitsLength.POINT] = \
        Mul(Rat(Int(125), Int(9)), Sym("thou"))  # nopep8
    CONVERSIONS[UnitsLength.THOU][UnitsLength.TERAMETER] = \
        Mul(Rat(Mul(Int(5), Pow(10, 18)), Int(127)), Sym("thou"))  # nopep8
    CONVERSIONS[UnitsLength.THOU][UnitsLength.YARD] = \
        Mul(Int(36000), Sym("thou"))  # nopep8
    CONVERSIONS[UnitsLength.THOU][UnitsLength.YOCTOMETER] = \
        Mul(Rat(Int(1), Mul(Int(254), Pow(10, 17))), Sym("thou"))  # nopep8
    CONVERSIONS[UnitsLength.THOU][UnitsLength.YOTTAMETER] = \
        Mul(Rat(Mul(Int(5), Pow(10, 30)), Int(127)), Sym("thou"))  # nopep8
    CONVERSIONS[UnitsLength.THOU][UnitsLength.ZEPTOMETER] = \
        Mul(Rat(Int(1), Mul(Int(254), Pow(10, 14))), Sym("thou"))  # nopep8
    CONVERSIONS[UnitsLength.THOU][UnitsLength.ZETTAMETER] = \
        Mul(Rat(Mul(Int(5), Pow(10, 27)), Int(127)), Sym("thou"))  # nopep8
    CONVERSIONS[UnitsLength.YARD][UnitsLength.ANGSTROM] = \
        Mul(Rat(Int(1), Mul(Int(9144), Pow(10, 6))), Sym("yd"))  # nopep8
    CONVERSIONS[UnitsLength.YARD][UnitsLength.ASTRONOMICALUNIT] = \
        Mul(Rat(Int("62332446125000"), Int(381)), Sym("yd"))  # nopep8
    CONVERSIONS[UnitsLength.YARD][UnitsLength.ATTOMETER] = \
        Mul(Rat(Int(1), Mul(Int(9144), Pow(10, 14))), Sym("yd"))  # nopep8
    CONVERSIONS[UnitsLength.YARD][UnitsLength.CENTIMETER] = \
        Mul(Rat(Int(25), Int(2286)), Sym("yd"))  # nopep8
    CONVERSIONS[UnitsLength.YARD][UnitsLength.DECAMETER] = \
        Mul(Rat(Int(12500), Int(1143)), Sym("yd"))  # nopep8
    CONVERSIONS[UnitsLength.YARD][UnitsLength.DECIMETER] = \
        Mul(Rat(Int(125), Int(1143)), Sym("yd"))  # nopep8
    CONVERSIONS[UnitsLength.YARD][UnitsLength.EXAMETER] = \
        Mul(Rat(Mul(Int(125), Pow(10, 19)), Int(1143)), Sym("yd"))  # nopep8
    CONVERSIONS[UnitsLength.YARD][UnitsLength.FEMTOMETER] = \
        Mul(Rat(Int(1), Mul(Int(9144), Pow(10, 11))), Sym("yd"))  # nopep8
    CONVERSIONS[UnitsLength.YARD][UnitsLength.FOOT] = \
        Mul(Rat(Int(1), Int(3)), Sym("yd"))  # nopep8
    CONVERSIONS[UnitsLength.YARD][UnitsLength.GIGAMETER] = \
        Mul(Rat(Mul(Int(125), Pow(10, 10)), Int(1143)), Sym("yd"))  # nopep8
    CONVERSIONS[UnitsLength.YARD][UnitsLength.HECTOMETER] = \
        Mul(Rat(Int(125000), Int(1143)), Sym("yd"))  # nopep8
    CONVERSIONS[UnitsLength.YARD][UnitsLength.INCH] = \
        Mul(Rat(Int(1), Int(36)), Sym("yd"))  # nopep8
    CONVERSIONS[UnitsLength.YARD][UnitsLength.KILOMETER] = \
        Mul(Rat(Mul(Int(125), Pow(10, 4)), Int(1143)), Sym("yd"))  # nopep8
    CONVERSIONS[UnitsLength.YARD][UnitsLength.LIGHTYEAR] = \
        Mul(Rat(Mul(Int("1313990343414"), Pow(10, 6)), Int(127)), Sym("yd"))  # nopep8
    CONVERSIONS[UnitsLength.YARD][UnitsLength.LINE] = \
        Mul(Rat(Int(1), Int(432)), Sym("yd"))  # nopep8
    CONVERSIONS[UnitsLength.YARD][UnitsLength.MEGAMETER] = \
        Mul(Rat(Mul(Int(125), Pow(10, 7)), Int(1143)), Sym("yd"))  # nopep8
    CONVERSIONS[UnitsLength.YARD][UnitsLength.METER] = \
        Mul(Rat(Int(1250), Int(1143)), Sym("yd"))  # nopep8
    CONVERSIONS[UnitsLength.YARD][UnitsLength.MICROMETER] = \
        Mul(Rat(Int(1), Int(914400)), Sym("yd"))  # nopep8
    CONVERSIONS[UnitsLength.YARD][UnitsLength.MILE] = \
        Mul(Int(1760), Sym("yd"))  # nopep8
    CONVERSIONS[UnitsLength.YARD][UnitsLength.MILLIMETER] = \
        Mul(Rat(Int(5), Int(4572)), Sym("yd"))  # nopep8
    CONVERSIONS[UnitsLength.YARD][UnitsLength.NANOMETER] = \
        Mul(Rat(Int(1), Mul(Int(9144), Pow(10, 5))), Sym("yd"))  # nopep8
    CONVERSIONS[UnitsLength.YARD][UnitsLength.PARSEC] = \
        Mul(Rat(Mul(Int(1285699), Pow(10, 13)), Int(381)), Sym("yd"))  # nopep8
    CONVERSIONS[UnitsLength.YARD][UnitsLength.PETAMETER] = \
        Mul(Rat(Mul(Int(125), Pow(10, 16)), Int(1143)), Sym("yd"))  # nopep8
    CONVERSIONS[UnitsLength.YARD][UnitsLength.PICOMETER] = \
        Mul(Rat(Int(1), Mul(Int(9144), Pow(10, 8))), Sym("yd"))  # nopep8
    CONVERSIONS[UnitsLength.YARD][UnitsLength.POINT] = \
        Mul(Rat(Int(1), Int(2592)), Sym("yd"))  # nopep8
    CONVERSIONS[UnitsLength.YARD][UnitsLength.TERAMETER] = \
        Mul(Rat(Mul(Int(125), Pow(10, 13)), Int(1143)), Sym("yd"))  # nopep8
    CONVERSIONS[UnitsLength.YARD][UnitsLength.THOU] = \
        Mul(Rat(Int(1), Int(36000)), Sym("yd"))  # nopep8
    CONVERSIONS[UnitsLength.YARD][UnitsLength.YOCTOMETER] = \
        Mul(Rat(Int(1), Mul(Int(9144), Pow(10, 20))), Sym("yd"))  # nopep8
    CONVERSIONS[UnitsLength.YARD][UnitsLength.YOTTAMETER] = \
        Mul(Rat(Mul(Int(125), Pow(10, 25)), Int(1143)), Sym("yd"))  # nopep8
    CONVERSIONS[UnitsLength.YARD][UnitsLength.ZEPTOMETER] = \
        Mul(Rat(Int(1), Mul(Int(9144), Pow(10, 17))), Sym("yd"))  # nopep8
    CONVERSIONS[UnitsLength.YARD][UnitsLength.ZETTAMETER] = \
        Mul(Rat(Mul(Int(125), Pow(10, 22)), Int(1143)), Sym("yd"))  # nopep8
    CONVERSIONS[UnitsLength.YOCTOMETER][UnitsLength.ANGSTROM] = \
        Mul(Pow(10, 14), Sym("yoctom"))  # nopep8
    CONVERSIONS[UnitsLength.YOCTOMETER][UnitsLength.ASTRONOMICALUNIT] = \
        Mul(Mul(Int(1495978707), Pow(10, 26)), Sym("yoctom"))  # nopep8
    CONVERSIONS[UnitsLength.YOCTOMETER][UnitsLength.ATTOMETER] = \
        Mul(Pow(10, 6), Sym("yoctom"))  # nopep8
    CONVERSIONS[UnitsLength.YOCTOMETER][UnitsLength.CENTIMETER] = \
        Mul(Pow(10, 22), Sym("yoctom"))  # nopep8
    CONVERSIONS[UnitsLength.YOCTOMETER][UnitsLength.DECAMETER] = \
        Mul(Pow(10, 25), Sym("yoctom"))  # nopep8
    CONVERSIONS[UnitsLength.YOCTOMETER][UnitsLength.DECIMETER] = \
        Mul(Pow(10, 23), Sym("yoctom"))  # nopep8
    CONVERSIONS[UnitsLength.YOCTOMETER][UnitsLength.EXAMETER] = \
        Mul(Pow(10, 42), Sym("yoctom"))  # nopep8
    CONVERSIONS[UnitsLength.YOCTOMETER][UnitsLength.FEMTOMETER] = \
        Mul(Pow(10, 9), Sym("yoctom"))  # nopep8
    CONVERSIONS[UnitsLength.YOCTOMETER][UnitsLength.FOOT] = \
        Mul(Mul(Int(3048), Pow(10, 20)), Sym("yoctom"))  # nopep8
    CONVERSIONS[UnitsLength.YOCTOMETER][UnitsLength.GIGAMETER] = \
        Mul(Pow(10, 33), Sym("yoctom"))  # nopep8
    CONVERSIONS[UnitsLength.YOCTOMETER][UnitsLength.HECTOMETER] = \
        Mul(Pow(10, 26), Sym("yoctom"))  # nopep8
    CONVERSIONS[UnitsLength.YOCTOMETER][UnitsLength.INCH] = \
        Mul(Mul(Int(254), Pow(10, 20)), Sym("yoctom"))  # nopep8
    CONVERSIONS[UnitsLength.YOCTOMETER][UnitsLength.KILOMETER] = \
        Mul(Pow(10, 27), Sym("yoctom"))  # nopep8
    CONVERSIONS[UnitsLength.YOCTOMETER][UnitsLength.LIGHTYEAR] = \
        Mul(Mul(Int("94607304725808"), Pow(10, 26)), Sym("yoctom"))  # nopep8
    CONVERSIONS[UnitsLength.YOCTOMETER][UnitsLength.LINE] = \
        Mul(Rat(Mul(Int(635), Pow(10, 19)), Int(3)), Sym("yoctom"))  # nopep8
    CONVERSIONS[UnitsLength.YOCTOMETER][UnitsLength.MEGAMETER] = \
        Mul(Pow(10, 30), Sym("yoctom"))  # nopep8
    CONVERSIONS[UnitsLength.YOCTOMETER][UnitsLength.METER] = \
        Mul(Pow(10, 24), Sym("yoctom"))  # nopep8
    CONVERSIONS[UnitsLength.YOCTOMETER][UnitsLength.MICROMETER] = \
        Mul(Pow(10, 18), Sym("yoctom"))  # nopep8
    CONVERSIONS[UnitsLength.YOCTOMETER][UnitsLength.MILE] = \
        Mul(Mul(Int(1609344), Pow(10, 21)), Sym("yoctom"))  # nopep8
    CONVERSIONS[UnitsLength.YOCTOMETER][UnitsLength.MILLIMETER] = \
        Mul(Pow(10, 21), Sym("yoctom"))  # nopep8
    CONVERSIONS[UnitsLength.YOCTOMETER][UnitsLength.NANOMETER] = \
        Mul(Pow(10, 15), Sym("yoctom"))  # nopep8
    CONVERSIONS[UnitsLength.YOCTOMETER][UnitsLength.PARSEC] = \
        Mul(Mul(Int(30856776), Pow(10, 33)), Sym("yoctom"))  # nopep8
    CONVERSIONS[UnitsLength.YOCTOMETER][UnitsLength.PETAMETER] = \
        Mul(Pow(10, 39), Sym("yoctom"))  # nopep8
    CONVERSIONS[UnitsLength.YOCTOMETER][UnitsLength.PICOMETER] = \
        Mul(Pow(10, 12), Sym("yoctom"))  # nopep8
    CONVERSIONS[UnitsLength.YOCTOMETER][UnitsLength.POINT] = \
        Mul(Rat(Mul(Int(3175), Pow(10, 18)), Int(9)), Sym("yoctom"))  # nopep8
    CONVERSIONS[UnitsLength.YOCTOMETER][UnitsLength.TERAMETER] = \
        Mul(Pow(10, 36), Sym("yoctom"))  # nopep8
    CONVERSIONS[UnitsLength.YOCTOMETER][UnitsLength.THOU] = \
        Mul(Mul(Int(254), Pow(10, 17)), Sym("yoctom"))  # nopep8
    CONVERSIONS[UnitsLength.YOCTOMETER][UnitsLength.YARD] = \
        Mul(Mul(Int(9144), Pow(10, 20)), Sym("yoctom"))  # nopep8
    CONVERSIONS[UnitsLength.YOCTOMETER][UnitsLength.YOTTAMETER] = \
        Mul(Pow(10, 48), Sym("yoctom"))  # nopep8
    CONVERSIONS[UnitsLength.YOCTOMETER][UnitsLength.ZEPTOMETER] = \
        Mul(Int(1000), Sym("yoctom"))  # nopep8
    CONVERSIONS[UnitsLength.YOCTOMETER][UnitsLength.ZETTAMETER] = \
        Mul(Pow(10, 45), Sym("yoctom"))  # nopep8
    CONVERSIONS[UnitsLength.YOTTAMETER][UnitsLength.ANGSTROM] = \
        Mul(Rat(Int(1), Pow(10, 34)), Sym("yottam"))  # nopep8
    CONVERSIONS[UnitsLength.YOTTAMETER][UnitsLength.ASTRONOMICALUNIT] = \
        Mul(Rat(Int(1495978707), Pow(10, 22)), Sym("yottam"))  # nopep8
    CONVERSIONS[UnitsLength.YOTTAMETER][UnitsLength.ATTOMETER] = \
        Mul(Rat(Int(1), Pow(10, 42)), Sym("yottam"))  # nopep8
    CONVERSIONS[UnitsLength.YOTTAMETER][UnitsLength.CENTIMETER] = \
        Mul(Rat(Int(1), Pow(10, 26)), Sym("yottam"))  # nopep8
    CONVERSIONS[UnitsLength.YOTTAMETER][UnitsLength.DECAMETER] = \
        Mul(Rat(Int(1), Pow(10, 23)), Sym("yottam"))  # nopep8
    CONVERSIONS[UnitsLength.YOTTAMETER][UnitsLength.DECIMETER] = \
        Mul(Rat(Int(1), Pow(10, 25)), Sym("yottam"))  # nopep8
    CONVERSIONS[UnitsLength.YOTTAMETER][UnitsLength.EXAMETER] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("yottam"))  # nopep8
    CONVERSIONS[UnitsLength.YOTTAMETER][UnitsLength.FEMTOMETER] = \
        Mul(Rat(Int(1), Pow(10, 39)), Sym("yottam"))  # nopep8
    CONVERSIONS[UnitsLength.YOTTAMETER][UnitsLength.FOOT] = \
        Mul(Rat(Int(381), Mul(Int(125), Pow(10, 25))), Sym("yottam"))  # nopep8
    CONVERSIONS[UnitsLength.YOTTAMETER][UnitsLength.GIGAMETER] = \
        Mul(Rat(Int(1), Pow(10, 15)), Sym("yottam"))  # nopep8
    CONVERSIONS[UnitsLength.YOTTAMETER][UnitsLength.HECTOMETER] = \
        Mul(Rat(Int(1), Pow(10, 22)), Sym("yottam"))  # nopep8
    CONVERSIONS[UnitsLength.YOTTAMETER][UnitsLength.INCH] = \
        Mul(Rat(Int(127), Mul(Int(5), Pow(10, 27))), Sym("yottam"))  # nopep8
    CONVERSIONS[UnitsLength.YOTTAMETER][UnitsLength.KILOMETER] = \
        Mul(Rat(Int(1), Pow(10, 21)), Sym("yottam"))  # nopep8
    CONVERSIONS[UnitsLength.YOTTAMETER][UnitsLength.LIGHTYEAR] = \
        Mul(Rat(Int("5912956545363"), Mul(Int(625), Pow(10, 18))), Sym("yottam"))  # nopep8
    CONVERSIONS[UnitsLength.YOTTAMETER][UnitsLength.LINE] = \
        Mul(Rat(Int(127), Mul(Int(6), Pow(10, 28))), Sym("yottam"))  # nopep8
    CONVERSIONS[UnitsLength.YOTTAMETER][UnitsLength.MEGAMETER] = \
        Mul(Rat(Int(1), Pow(10, 18)), Sym("yottam"))  # nopep8
    CONVERSIONS[UnitsLength.YOTTAMETER][UnitsLength.METER] = \
        Mul(Rat(Int(1), Pow(10, 24)), Sym("yottam"))  # nopep8
    CONVERSIONS[UnitsLength.YOTTAMETER][UnitsLength.MICROMETER] = \
        Mul(Rat(Int(1), Pow(10, 30)), Sym("yottam"))  # nopep8
    CONVERSIONS[UnitsLength.YOTTAMETER][UnitsLength.MILE] = \
        Mul(Rat(Int(12573), Mul(Int(78125), Pow(10, 20))), Sym("yottam"))  # nopep8
    CONVERSIONS[UnitsLength.YOTTAMETER][UnitsLength.MILLIMETER] = \
        Mul(Rat(Int(1), Pow(10, 27)), Sym("yottam"))  # nopep8
    CONVERSIONS[UnitsLength.YOTTAMETER][UnitsLength.NANOMETER] = \
        Mul(Rat(Int(1), Pow(10, 33)), Sym("yottam"))  # nopep8
    CONVERSIONS[UnitsLength.YOTTAMETER][UnitsLength.PARSEC] = \
        Mul(Rat(Int(3857097), Mul(Int(125), Pow(10, 12))), Sym("yottam"))  # nopep8
    CONVERSIONS[UnitsLength.YOTTAMETER][UnitsLength.PETAMETER] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("yottam"))  # nopep8
    CONVERSIONS[UnitsLength.YOTTAMETER][UnitsLength.PICOMETER] = \
        Mul(Rat(Int(1), Pow(10, 36)), Sym("yottam"))  # nopep8
    CONVERSIONS[UnitsLength.YOTTAMETER][UnitsLength.POINT] = \
        Mul(Rat(Int(127), Mul(Int(36), Pow(10, 28))), Sym("yottam"))  # nopep8
    CONVERSIONS[UnitsLength.YOTTAMETER][UnitsLength.TERAMETER] = \
        Mul(Rat(Int(1), Pow(10, 12)), Sym("yottam"))  # nopep8
    CONVERSIONS[UnitsLength.YOTTAMETER][UnitsLength.THOU] = \
        Mul(Rat(Int(127), Mul(Int(5), Pow(10, 30))), Sym("yottam"))  # nopep8
    CONVERSIONS[UnitsLength.YOTTAMETER][UnitsLength.YARD] = \
        Mul(Rat(Int(1143), Mul(Int(125), Pow(10, 25))), Sym("yottam"))  # nopep8
    CONVERSIONS[UnitsLength.YOTTAMETER][UnitsLength.YOCTOMETER] = \
        Mul(Rat(Int(1), Pow(10, 48)), Sym("yottam"))  # nopep8
    CONVERSIONS[UnitsLength.YOTTAMETER][UnitsLength.ZEPTOMETER] = \
        Mul(Rat(Int(1), Pow(10, 45)), Sym("yottam"))  # nopep8
    CONVERSIONS[UnitsLength.YOTTAMETER][UnitsLength.ZETTAMETER] = \
        Mul(Rat(Int(1), Int(1000)), Sym("yottam"))  # nopep8
    CONVERSIONS[UnitsLength.ZEPTOMETER][UnitsLength.ANGSTROM] = \
        Mul(Pow(10, 11), Sym("zeptom"))  # nopep8
    CONVERSIONS[UnitsLength.ZEPTOMETER][UnitsLength.ASTRONOMICALUNIT] = \
        Mul(Mul(Int(1495978707), Pow(10, 23)), Sym("zeptom"))  # nopep8
    CONVERSIONS[UnitsLength.ZEPTOMETER][UnitsLength.ATTOMETER] = \
        Mul(Int(1000), Sym("zeptom"))  # nopep8
    CONVERSIONS[UnitsLength.ZEPTOMETER][UnitsLength.CENTIMETER] = \
        Mul(Pow(10, 19), Sym("zeptom"))  # nopep8
    CONVERSIONS[UnitsLength.ZEPTOMETER][UnitsLength.DECAMETER] = \
        Mul(Pow(10, 22), Sym("zeptom"))  # nopep8
    CONVERSIONS[UnitsLength.ZEPTOMETER][UnitsLength.DECIMETER] = \
        Mul(Pow(10, 20), Sym("zeptom"))  # nopep8
    CONVERSIONS[UnitsLength.ZEPTOMETER][UnitsLength.EXAMETER] = \
        Mul(Pow(10, 39), Sym("zeptom"))  # nopep8
    CONVERSIONS[UnitsLength.ZEPTOMETER][UnitsLength.FEMTOMETER] = \
        Mul(Pow(10, 6), Sym("zeptom"))  # nopep8
    CONVERSIONS[UnitsLength.ZEPTOMETER][UnitsLength.FOOT] = \
        Mul(Mul(Int(3048), Pow(10, 17)), Sym("zeptom"))  # nopep8
    CONVERSIONS[UnitsLength.ZEPTOMETER][UnitsLength.GIGAMETER] = \
        Mul(Pow(10, 30), Sym("zeptom"))  # nopep8
    CONVERSIONS[UnitsLength.ZEPTOMETER][UnitsLength.HECTOMETER] = \
        Mul(Pow(10, 23), Sym("zeptom"))  # nopep8
    CONVERSIONS[UnitsLength.ZEPTOMETER][UnitsLength.INCH] = \
        Mul(Mul(Int(254), Pow(10, 17)), Sym("zeptom"))  # nopep8
    CONVERSIONS[UnitsLength.ZEPTOMETER][UnitsLength.KILOMETER] = \
        Mul(Pow(10, 24), Sym("zeptom"))  # nopep8
    CONVERSIONS[UnitsLength.ZEPTOMETER][UnitsLength.LIGHTYEAR] = \
        Mul(Mul(Int("94607304725808"), Pow(10, 23)), Sym("zeptom"))  # nopep8
    CONVERSIONS[UnitsLength.ZEPTOMETER][UnitsLength.LINE] = \
        Mul(Rat(Mul(Int(635), Pow(10, 16)), Int(3)), Sym("zeptom"))  # nopep8
    CONVERSIONS[UnitsLength.ZEPTOMETER][UnitsLength.MEGAMETER] = \
        Mul(Pow(10, 27), Sym("zeptom"))  # nopep8
    CONVERSIONS[UnitsLength.ZEPTOMETER][UnitsLength.METER] = \
        Mul(Pow(10, 21), Sym("zeptom"))  # nopep8
    CONVERSIONS[UnitsLength.ZEPTOMETER][UnitsLength.MICROMETER] = \
        Mul(Pow(10, 15), Sym("zeptom"))  # nopep8
    CONVERSIONS[UnitsLength.ZEPTOMETER][UnitsLength.MILE] = \
        Mul(Mul(Int(1609344), Pow(10, 18)), Sym("zeptom"))  # nopep8
    CONVERSIONS[UnitsLength.ZEPTOMETER][UnitsLength.MILLIMETER] = \
        Mul(Pow(10, 18), Sym("zeptom"))  # nopep8
    CONVERSIONS[UnitsLength.ZEPTOMETER][UnitsLength.NANOMETER] = \
        Mul(Pow(10, 12), Sym("zeptom"))  # nopep8
    CONVERSIONS[UnitsLength.ZEPTOMETER][UnitsLength.PARSEC] = \
        Mul(Mul(Int(30856776), Pow(10, 30)), Sym("zeptom"))  # nopep8
    CONVERSIONS[UnitsLength.ZEPTOMETER][UnitsLength.PETAMETER] = \
        Mul(Pow(10, 36), Sym("zeptom"))  # nopep8
    CONVERSIONS[UnitsLength.ZEPTOMETER][UnitsLength.PICOMETER] = \
        Mul(Pow(10, 9), Sym("zeptom"))  # nopep8
    CONVERSIONS[UnitsLength.ZEPTOMETER][UnitsLength.POINT] = \
        Mul(Rat(Mul(Int(3175), Pow(10, 15)), Int(9)), Sym("zeptom"))  # nopep8
    CONVERSIONS[UnitsLength.ZEPTOMETER][UnitsLength.TERAMETER] = \
        Mul(Pow(10, 33), Sym("zeptom"))  # nopep8
    CONVERSIONS[UnitsLength.ZEPTOMETER][UnitsLength.THOU] = \
        Mul(Mul(Int(254), Pow(10, 14)), Sym("zeptom"))  # nopep8
    CONVERSIONS[UnitsLength.ZEPTOMETER][UnitsLength.YARD] = \
        Mul(Mul(Int(9144), Pow(10, 17)), Sym("zeptom"))  # nopep8
    CONVERSIONS[UnitsLength.ZEPTOMETER][UnitsLength.YOCTOMETER] = \
        Mul(Rat(Int(1), Int(1000)), Sym("zeptom"))  # nopep8
    CONVERSIONS[UnitsLength.ZEPTOMETER][UnitsLength.YOTTAMETER] = \
        Mul(Pow(10, 45), Sym("zeptom"))  # nopep8
    CONVERSIONS[UnitsLength.ZEPTOMETER][UnitsLength.ZETTAMETER] = \
        Mul(Pow(10, 42), Sym("zeptom"))  # nopep8
    CONVERSIONS[UnitsLength.ZETTAMETER][UnitsLength.ANGSTROM] = \
        Mul(Rat(Int(1), Pow(10, 31)), Sym("zettam"))  # nopep8
    CONVERSIONS[UnitsLength.ZETTAMETER][UnitsLength.ASTRONOMICALUNIT] = \
        Mul(Rat(Int(1495978707), Pow(10, 19)), Sym("zettam"))  # nopep8
    CONVERSIONS[UnitsLength.ZETTAMETER][UnitsLength.ATTOMETER] = \
        Mul(Rat(Int(1), Pow(10, 39)), Sym("zettam"))  # nopep8
    CONVERSIONS[UnitsLength.ZETTAMETER][UnitsLength.CENTIMETER] = \
        Mul(Rat(Int(1), Pow(10, 23)), Sym("zettam"))  # nopep8
    CONVERSIONS[UnitsLength.ZETTAMETER][UnitsLength.DECAMETER] = \
        Mul(Rat(Int(1), Pow(10, 20)), Sym("zettam"))  # nopep8
    CONVERSIONS[UnitsLength.ZETTAMETER][UnitsLength.DECIMETER] = \
        Mul(Rat(Int(1), Pow(10, 22)), Sym("zettam"))  # nopep8
    CONVERSIONS[UnitsLength.ZETTAMETER][UnitsLength.EXAMETER] = \
        Mul(Rat(Int(1), Int(1000)), Sym("zettam"))  # nopep8
    CONVERSIONS[UnitsLength.ZETTAMETER][UnitsLength.FEMTOMETER] = \
        Mul(Rat(Int(1), Pow(10, 36)), Sym("zettam"))  # nopep8
    CONVERSIONS[UnitsLength.ZETTAMETER][UnitsLength.FOOT] = \
        Mul(Rat(Int(381), Mul(Int(125), Pow(10, 22))), Sym("zettam"))  # nopep8
    CONVERSIONS[UnitsLength.ZETTAMETER][UnitsLength.GIGAMETER] = \
        Mul(Rat(Int(1), Pow(10, 12)), Sym("zettam"))  # nopep8
    CONVERSIONS[UnitsLength.ZETTAMETER][UnitsLength.HECTOMETER] = \
        Mul(Rat(Int(1), Pow(10, 19)), Sym("zettam"))  # nopep8
    CONVERSIONS[UnitsLength.ZETTAMETER][UnitsLength.INCH] = \
        Mul(Rat(Int(127), Mul(Int(5), Pow(10, 24))), Sym("zettam"))  # nopep8
    CONVERSIONS[UnitsLength.ZETTAMETER][UnitsLength.KILOMETER] = \
        Mul(Rat(Int(1), Pow(10, 18)), Sym("zettam"))  # nopep8
    CONVERSIONS[UnitsLength.ZETTAMETER][UnitsLength.LIGHTYEAR] = \
        Mul(Rat(Int("5912956545363"), Mul(Int(625), Pow(10, 15))), Sym("zettam"))  # nopep8
    CONVERSIONS[UnitsLength.ZETTAMETER][UnitsLength.LINE] = \
        Mul(Rat(Int(127), Mul(Int(6), Pow(10, 25))), Sym("zettam"))  # nopep8
    CONVERSIONS[UnitsLength.ZETTAMETER][UnitsLength.MEGAMETER] = \
        Mul(Rat(Int(1), Pow(10, 15)), Sym("zettam"))  # nopep8
    CONVERSIONS[UnitsLength.ZETTAMETER][UnitsLength.METER] = \
        Mul(Rat(Int(1), Pow(10, 21)), Sym("zettam"))  # nopep8
    CONVERSIONS[UnitsLength.ZETTAMETER][UnitsLength.MICROMETER] = \
        Mul(Rat(Int(1), Pow(10, 27)), Sym("zettam"))  # nopep8
    CONVERSIONS[UnitsLength.ZETTAMETER][UnitsLength.MILE] = \
        Mul(Rat(Int(12573), Mul(Int(78125), Pow(10, 17))), Sym("zettam"))  # nopep8
    CONVERSIONS[UnitsLength.ZETTAMETER][UnitsLength.MILLIMETER] = \
        Mul(Rat(Int(1), Pow(10, 24)), Sym("zettam"))  # nopep8
    CONVERSIONS[UnitsLength.ZETTAMETER][UnitsLength.NANOMETER] = \
        Mul(Rat(Int(1), Pow(10, 30)), Sym("zettam"))  # nopep8
    CONVERSIONS[UnitsLength.ZETTAMETER][UnitsLength.PARSEC] = \
        Mul(Rat(Int(3857097), Mul(Int(125), Pow(10, 9))), Sym("zettam"))  # nopep8
    CONVERSIONS[UnitsLength.ZETTAMETER][UnitsLength.PETAMETER] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("zettam"))  # nopep8
    CONVERSIONS[UnitsLength.ZETTAMETER][UnitsLength.PICOMETER] = \
        Mul(Rat(Int(1), Pow(10, 33)), Sym("zettam"))  # nopep8
    CONVERSIONS[UnitsLength.ZETTAMETER][UnitsLength.POINT] = \
        Mul(Rat(Int(127), Mul(Int(36), Pow(10, 25))), Sym("zettam"))  # nopep8
    CONVERSIONS[UnitsLength.ZETTAMETER][UnitsLength.TERAMETER] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("zettam"))  # nopep8
    CONVERSIONS[UnitsLength.ZETTAMETER][UnitsLength.THOU] = \
        Mul(Rat(Int(127), Mul(Int(5), Pow(10, 27))), Sym("zettam"))  # nopep8
    CONVERSIONS[UnitsLength.ZETTAMETER][UnitsLength.YARD] = \
        Mul(Rat(Int(1143), Mul(Int(125), Pow(10, 22))), Sym("zettam"))  # nopep8
    CONVERSIONS[UnitsLength.ZETTAMETER][UnitsLength.YOCTOMETER] = \
        Mul(Rat(Int(1), Pow(10, 45)), Sym("zettam"))  # nopep8
    CONVERSIONS[UnitsLength.ZETTAMETER][UnitsLength.YOTTAMETER] = \
        Mul(Int(1000), Sym("zettam"))  # nopep8
    CONVERSIONS[UnitsLength.ZETTAMETER][UnitsLength.ZEPTOMETER] = \
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
            targetUnit = getattr(UnitsLength, str(target))
            sourceUnit = value.getUnit()
            source = str(sourceUnit)
            if target == source:
                self.setValue(value.getValue())
                self.setUnit(value.getUnit())
            else:
                c = self.CONVERSIONS.get(targetUnit).get(sourceUnit)
                if c is None:
                    t = (value.getValue(), value.getUnit(), target)
                    msg = "%s %s cannot be converted to %s" % t
                    raise Exception(msg)
                self.setValue(c(value.getValue()))
                self.setUnit(targetUnit)
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
