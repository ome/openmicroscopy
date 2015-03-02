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
Code-generated omero.model.Power implementation,
based on omero.model.PermissionsI
"""


import Ice
import IceImport
IceImport.load("omero_model_Power_ice")
_omero = Ice.openModule("omero")
_omero_model = Ice.openModule("omero.model")
__name__ = "omero.model"

from omero_model_UnitBase import UnitBase
from omero.model.enums import UnitsPower

from omero.conversions import Add  # nopep8
from omero.conversions import Int  # nopep8
from omero.conversions import Mul  # nopep8
from omero.conversions import Pow  # nopep8
from omero.conversions import Rat  # nopep8
from omero.conversions import Sym  # nopep8


class PowerI(_omero_model.Power, UnitBase):

    try:
        UNIT_VALUES = sorted(UnitsPower._enumerators.values())
    except:
        # TODO: this occurs on Ice 3.4 and can be removed
        # once it has been dropped.
        UNIT_VALUES = [x for x in sorted(UnitsPower._names)]
        UNIT_VALUES = [getattr(UnitsPower, x) for x in UNIT_VALUES]
    CONVERSIONS = dict()
    for val in UNIT_VALUES:
        CONVERSIONS[val] = dict()
    CONVERSIONS[UnitsPower.ATTOWATT][UnitsPower.CENTIWATT] = \
        Mul(Pow(10, 16), Sym("attow"))  # nopep8
    CONVERSIONS[UnitsPower.ATTOWATT][UnitsPower.DECAWATT] = \
        Mul(Pow(10, 19), Sym("attow"))  # nopep8
    CONVERSIONS[UnitsPower.ATTOWATT][UnitsPower.DECIWATT] = \
        Mul(Pow(10, 17), Sym("attow"))  # nopep8
    CONVERSIONS[UnitsPower.ATTOWATT][UnitsPower.EXAWATT] = \
        Mul(Pow(10, 36), Sym("attow"))  # nopep8
    CONVERSIONS[UnitsPower.ATTOWATT][UnitsPower.FEMTOWATT] = \
        Mul(Int(1000), Sym("attow"))  # nopep8
    CONVERSIONS[UnitsPower.ATTOWATT][UnitsPower.GIGAWATT] = \
        Mul(Pow(10, 27), Sym("attow"))  # nopep8
    CONVERSIONS[UnitsPower.ATTOWATT][UnitsPower.HECTOWATT] = \
        Mul(Pow(10, 20), Sym("attow"))  # nopep8
    CONVERSIONS[UnitsPower.ATTOWATT][UnitsPower.KILOWATT] = \
        Mul(Pow(10, 21), Sym("attow"))  # nopep8
    CONVERSIONS[UnitsPower.ATTOWATT][UnitsPower.MEGAWATT] = \
        Mul(Pow(10, 24), Sym("attow"))  # nopep8
    CONVERSIONS[UnitsPower.ATTOWATT][UnitsPower.MICROWATT] = \
        Mul(Pow(10, 12), Sym("attow"))  # nopep8
    CONVERSIONS[UnitsPower.ATTOWATT][UnitsPower.MILLIWATT] = \
        Mul(Pow(10, 15), Sym("attow"))  # nopep8
    CONVERSIONS[UnitsPower.ATTOWATT][UnitsPower.NANOWATT] = \
        Mul(Pow(10, 9), Sym("attow"))  # nopep8
    CONVERSIONS[UnitsPower.ATTOWATT][UnitsPower.PETAWATT] = \
        Mul(Pow(10, 33), Sym("attow"))  # nopep8
    CONVERSIONS[UnitsPower.ATTOWATT][UnitsPower.PICOWATT] = \
        Mul(Pow(10, 6), Sym("attow"))  # nopep8
    CONVERSIONS[UnitsPower.ATTOWATT][UnitsPower.TERAWATT] = \
        Mul(Pow(10, 30), Sym("attow"))  # nopep8
    CONVERSIONS[UnitsPower.ATTOWATT][UnitsPower.WATT] = \
        Mul(Pow(10, 18), Sym("attow"))  # nopep8
    CONVERSIONS[UnitsPower.ATTOWATT][UnitsPower.YOCTOWATT] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("attow"))  # nopep8
    CONVERSIONS[UnitsPower.ATTOWATT][UnitsPower.YOTTAWATT] = \
        Mul(Pow(10, 42), Sym("attow"))  # nopep8
    CONVERSIONS[UnitsPower.ATTOWATT][UnitsPower.ZEPTOWATT] = \
        Mul(Rat(Int(1), Int(1000)), Sym("attow"))  # nopep8
    CONVERSIONS[UnitsPower.ATTOWATT][UnitsPower.ZETTAWATT] = \
        Mul(Pow(10, 39), Sym("attow"))  # nopep8
    CONVERSIONS[UnitsPower.CENTIWATT][UnitsPower.ATTOWATT] = \
        Mul(Rat(Int(1), Pow(10, 16)), Sym("centiw"))  # nopep8
    CONVERSIONS[UnitsPower.CENTIWATT][UnitsPower.DECAWATT] = \
        Mul(Int(1000), Sym("centiw"))  # nopep8
    CONVERSIONS[UnitsPower.CENTIWATT][UnitsPower.DECIWATT] = \
        Mul(Int(10), Sym("centiw"))  # nopep8
    CONVERSIONS[UnitsPower.CENTIWATT][UnitsPower.EXAWATT] = \
        Mul(Pow(10, 20), Sym("centiw"))  # nopep8
    CONVERSIONS[UnitsPower.CENTIWATT][UnitsPower.FEMTOWATT] = \
        Mul(Rat(Int(1), Pow(10, 13)), Sym("centiw"))  # nopep8
    CONVERSIONS[UnitsPower.CENTIWATT][UnitsPower.GIGAWATT] = \
        Mul(Pow(10, 11), Sym("centiw"))  # nopep8
    CONVERSIONS[UnitsPower.CENTIWATT][UnitsPower.HECTOWATT] = \
        Mul(Pow(10, 4), Sym("centiw"))  # nopep8
    CONVERSIONS[UnitsPower.CENTIWATT][UnitsPower.KILOWATT] = \
        Mul(Pow(10, 5), Sym("centiw"))  # nopep8
    CONVERSIONS[UnitsPower.CENTIWATT][UnitsPower.MEGAWATT] = \
        Mul(Pow(10, 8), Sym("centiw"))  # nopep8
    CONVERSIONS[UnitsPower.CENTIWATT][UnitsPower.MICROWATT] = \
        Mul(Rat(Int(1), Pow(10, 4)), Sym("centiw"))  # nopep8
    CONVERSIONS[UnitsPower.CENTIWATT][UnitsPower.MILLIWATT] = \
        Mul(Rat(Int(1), Int(10)), Sym("centiw"))  # nopep8
    CONVERSIONS[UnitsPower.CENTIWATT][UnitsPower.NANOWATT] = \
        Mul(Rat(Int(1), Pow(10, 7)), Sym("centiw"))  # nopep8
    CONVERSIONS[UnitsPower.CENTIWATT][UnitsPower.PETAWATT] = \
        Mul(Pow(10, 17), Sym("centiw"))  # nopep8
    CONVERSIONS[UnitsPower.CENTIWATT][UnitsPower.PICOWATT] = \
        Mul(Rat(Int(1), Pow(10, 10)), Sym("centiw"))  # nopep8
    CONVERSIONS[UnitsPower.CENTIWATT][UnitsPower.TERAWATT] = \
        Mul(Pow(10, 14), Sym("centiw"))  # nopep8
    CONVERSIONS[UnitsPower.CENTIWATT][UnitsPower.WATT] = \
        Mul(Int(100), Sym("centiw"))  # nopep8
    CONVERSIONS[UnitsPower.CENTIWATT][UnitsPower.YOCTOWATT] = \
        Mul(Rat(Int(1), Pow(10, 22)), Sym("centiw"))  # nopep8
    CONVERSIONS[UnitsPower.CENTIWATT][UnitsPower.YOTTAWATT] = \
        Mul(Pow(10, 26), Sym("centiw"))  # nopep8
    CONVERSIONS[UnitsPower.CENTIWATT][UnitsPower.ZEPTOWATT] = \
        Mul(Rat(Int(1), Pow(10, 19)), Sym("centiw"))  # nopep8
    CONVERSIONS[UnitsPower.CENTIWATT][UnitsPower.ZETTAWATT] = \
        Mul(Pow(10, 23), Sym("centiw"))  # nopep8
    CONVERSIONS[UnitsPower.DECAWATT][UnitsPower.ATTOWATT] = \
        Mul(Rat(Int(1), Pow(10, 19)), Sym("decaw"))  # nopep8
    CONVERSIONS[UnitsPower.DECAWATT][UnitsPower.CENTIWATT] = \
        Mul(Rat(Int(1), Int(1000)), Sym("decaw"))  # nopep8
    CONVERSIONS[UnitsPower.DECAWATT][UnitsPower.DECIWATT] = \
        Mul(Rat(Int(1), Int(100)), Sym("decaw"))  # nopep8
    CONVERSIONS[UnitsPower.DECAWATT][UnitsPower.EXAWATT] = \
        Mul(Pow(10, 17), Sym("decaw"))  # nopep8
    CONVERSIONS[UnitsPower.DECAWATT][UnitsPower.FEMTOWATT] = \
        Mul(Rat(Int(1), Pow(10, 16)), Sym("decaw"))  # nopep8
    CONVERSIONS[UnitsPower.DECAWATT][UnitsPower.GIGAWATT] = \
        Mul(Pow(10, 8), Sym("decaw"))  # nopep8
    CONVERSIONS[UnitsPower.DECAWATT][UnitsPower.HECTOWATT] = \
        Mul(Int(10), Sym("decaw"))  # nopep8
    CONVERSIONS[UnitsPower.DECAWATT][UnitsPower.KILOWATT] = \
        Mul(Int(100), Sym("decaw"))  # nopep8
    CONVERSIONS[UnitsPower.DECAWATT][UnitsPower.MEGAWATT] = \
        Mul(Pow(10, 5), Sym("decaw"))  # nopep8
    CONVERSIONS[UnitsPower.DECAWATT][UnitsPower.MICROWATT] = \
        Mul(Rat(Int(1), Pow(10, 7)), Sym("decaw"))  # nopep8
    CONVERSIONS[UnitsPower.DECAWATT][UnitsPower.MILLIWATT] = \
        Mul(Rat(Int(1), Pow(10, 4)), Sym("decaw"))  # nopep8
    CONVERSIONS[UnitsPower.DECAWATT][UnitsPower.NANOWATT] = \
        Mul(Rat(Int(1), Pow(10, 10)), Sym("decaw"))  # nopep8
    CONVERSIONS[UnitsPower.DECAWATT][UnitsPower.PETAWATT] = \
        Mul(Pow(10, 14), Sym("decaw"))  # nopep8
    CONVERSIONS[UnitsPower.DECAWATT][UnitsPower.PICOWATT] = \
        Mul(Rat(Int(1), Pow(10, 13)), Sym("decaw"))  # nopep8
    CONVERSIONS[UnitsPower.DECAWATT][UnitsPower.TERAWATT] = \
        Mul(Pow(10, 11), Sym("decaw"))  # nopep8
    CONVERSIONS[UnitsPower.DECAWATT][UnitsPower.WATT] = \
        Mul(Rat(Int(1), Int(10)), Sym("decaw"))  # nopep8
    CONVERSIONS[UnitsPower.DECAWATT][UnitsPower.YOCTOWATT] = \
        Mul(Rat(Int(1), Pow(10, 25)), Sym("decaw"))  # nopep8
    CONVERSIONS[UnitsPower.DECAWATT][UnitsPower.YOTTAWATT] = \
        Mul(Pow(10, 23), Sym("decaw"))  # nopep8
    CONVERSIONS[UnitsPower.DECAWATT][UnitsPower.ZEPTOWATT] = \
        Mul(Rat(Int(1), Pow(10, 22)), Sym("decaw"))  # nopep8
    CONVERSIONS[UnitsPower.DECAWATT][UnitsPower.ZETTAWATT] = \
        Mul(Pow(10, 20), Sym("decaw"))  # nopep8
    CONVERSIONS[UnitsPower.DECIWATT][UnitsPower.ATTOWATT] = \
        Mul(Rat(Int(1), Pow(10, 17)), Sym("deciw"))  # nopep8
    CONVERSIONS[UnitsPower.DECIWATT][UnitsPower.CENTIWATT] = \
        Mul(Rat(Int(1), Int(10)), Sym("deciw"))  # nopep8
    CONVERSIONS[UnitsPower.DECIWATT][UnitsPower.DECAWATT] = \
        Mul(Int(100), Sym("deciw"))  # nopep8
    CONVERSIONS[UnitsPower.DECIWATT][UnitsPower.EXAWATT] = \
        Mul(Pow(10, 19), Sym("deciw"))  # nopep8
    CONVERSIONS[UnitsPower.DECIWATT][UnitsPower.FEMTOWATT] = \
        Mul(Rat(Int(1), Pow(10, 14)), Sym("deciw"))  # nopep8
    CONVERSIONS[UnitsPower.DECIWATT][UnitsPower.GIGAWATT] = \
        Mul(Pow(10, 10), Sym("deciw"))  # nopep8
    CONVERSIONS[UnitsPower.DECIWATT][UnitsPower.HECTOWATT] = \
        Mul(Int(1000), Sym("deciw"))  # nopep8
    CONVERSIONS[UnitsPower.DECIWATT][UnitsPower.KILOWATT] = \
        Mul(Pow(10, 4), Sym("deciw"))  # nopep8
    CONVERSIONS[UnitsPower.DECIWATT][UnitsPower.MEGAWATT] = \
        Mul(Pow(10, 7), Sym("deciw"))  # nopep8
    CONVERSIONS[UnitsPower.DECIWATT][UnitsPower.MICROWATT] = \
        Mul(Rat(Int(1), Pow(10, 5)), Sym("deciw"))  # nopep8
    CONVERSIONS[UnitsPower.DECIWATT][UnitsPower.MILLIWATT] = \
        Mul(Rat(Int(1), Int(100)), Sym("deciw"))  # nopep8
    CONVERSIONS[UnitsPower.DECIWATT][UnitsPower.NANOWATT] = \
        Mul(Rat(Int(1), Pow(10, 8)), Sym("deciw"))  # nopep8
    CONVERSIONS[UnitsPower.DECIWATT][UnitsPower.PETAWATT] = \
        Mul(Pow(10, 16), Sym("deciw"))  # nopep8
    CONVERSIONS[UnitsPower.DECIWATT][UnitsPower.PICOWATT] = \
        Mul(Rat(Int(1), Pow(10, 11)), Sym("deciw"))  # nopep8
    CONVERSIONS[UnitsPower.DECIWATT][UnitsPower.TERAWATT] = \
        Mul(Pow(10, 13), Sym("deciw"))  # nopep8
    CONVERSIONS[UnitsPower.DECIWATT][UnitsPower.WATT] = \
        Mul(Int(10), Sym("deciw"))  # nopep8
    CONVERSIONS[UnitsPower.DECIWATT][UnitsPower.YOCTOWATT] = \
        Mul(Rat(Int(1), Pow(10, 23)), Sym("deciw"))  # nopep8
    CONVERSIONS[UnitsPower.DECIWATT][UnitsPower.YOTTAWATT] = \
        Mul(Pow(10, 25), Sym("deciw"))  # nopep8
    CONVERSIONS[UnitsPower.DECIWATT][UnitsPower.ZEPTOWATT] = \
        Mul(Rat(Int(1), Pow(10, 20)), Sym("deciw"))  # nopep8
    CONVERSIONS[UnitsPower.DECIWATT][UnitsPower.ZETTAWATT] = \
        Mul(Pow(10, 22), Sym("deciw"))  # nopep8
    CONVERSIONS[UnitsPower.EXAWATT][UnitsPower.ATTOWATT] = \
        Mul(Rat(Int(1), Pow(10, 36)), Sym("exaw"))  # nopep8
    CONVERSIONS[UnitsPower.EXAWATT][UnitsPower.CENTIWATT] = \
        Mul(Rat(Int(1), Pow(10, 20)), Sym("exaw"))  # nopep8
    CONVERSIONS[UnitsPower.EXAWATT][UnitsPower.DECAWATT] = \
        Mul(Rat(Int(1), Pow(10, 17)), Sym("exaw"))  # nopep8
    CONVERSIONS[UnitsPower.EXAWATT][UnitsPower.DECIWATT] = \
        Mul(Rat(Int(1), Pow(10, 19)), Sym("exaw"))  # nopep8
    CONVERSIONS[UnitsPower.EXAWATT][UnitsPower.FEMTOWATT] = \
        Mul(Rat(Int(1), Pow(10, 33)), Sym("exaw"))  # nopep8
    CONVERSIONS[UnitsPower.EXAWATT][UnitsPower.GIGAWATT] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("exaw"))  # nopep8
    CONVERSIONS[UnitsPower.EXAWATT][UnitsPower.HECTOWATT] = \
        Mul(Rat(Int(1), Pow(10, 16)), Sym("exaw"))  # nopep8
    CONVERSIONS[UnitsPower.EXAWATT][UnitsPower.KILOWATT] = \
        Mul(Rat(Int(1), Pow(10, 15)), Sym("exaw"))  # nopep8
    CONVERSIONS[UnitsPower.EXAWATT][UnitsPower.MEGAWATT] = \
        Mul(Rat(Int(1), Pow(10, 12)), Sym("exaw"))  # nopep8
    CONVERSIONS[UnitsPower.EXAWATT][UnitsPower.MICROWATT] = \
        Mul(Rat(Int(1), Pow(10, 24)), Sym("exaw"))  # nopep8
    CONVERSIONS[UnitsPower.EXAWATT][UnitsPower.MILLIWATT] = \
        Mul(Rat(Int(1), Pow(10, 21)), Sym("exaw"))  # nopep8
    CONVERSIONS[UnitsPower.EXAWATT][UnitsPower.NANOWATT] = \
        Mul(Rat(Int(1), Pow(10, 27)), Sym("exaw"))  # nopep8
    CONVERSIONS[UnitsPower.EXAWATT][UnitsPower.PETAWATT] = \
        Mul(Rat(Int(1), Int(1000)), Sym("exaw"))  # nopep8
    CONVERSIONS[UnitsPower.EXAWATT][UnitsPower.PICOWATT] = \
        Mul(Rat(Int(1), Pow(10, 30)), Sym("exaw"))  # nopep8
    CONVERSIONS[UnitsPower.EXAWATT][UnitsPower.TERAWATT] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("exaw"))  # nopep8
    CONVERSIONS[UnitsPower.EXAWATT][UnitsPower.WATT] = \
        Mul(Rat(Int(1), Pow(10, 18)), Sym("exaw"))  # nopep8
    CONVERSIONS[UnitsPower.EXAWATT][UnitsPower.YOCTOWATT] = \
        Mul(Rat(Int(1), Pow(10, 42)), Sym("exaw"))  # nopep8
    CONVERSIONS[UnitsPower.EXAWATT][UnitsPower.YOTTAWATT] = \
        Mul(Pow(10, 6), Sym("exaw"))  # nopep8
    CONVERSIONS[UnitsPower.EXAWATT][UnitsPower.ZEPTOWATT] = \
        Mul(Rat(Int(1), Pow(10, 39)), Sym("exaw"))  # nopep8
    CONVERSIONS[UnitsPower.EXAWATT][UnitsPower.ZETTAWATT] = \
        Mul(Int(1000), Sym("exaw"))  # nopep8
    CONVERSIONS[UnitsPower.FEMTOWATT][UnitsPower.ATTOWATT] = \
        Mul(Rat(Int(1), Int(1000)), Sym("femtow"))  # nopep8
    CONVERSIONS[UnitsPower.FEMTOWATT][UnitsPower.CENTIWATT] = \
        Mul(Pow(10, 13), Sym("femtow"))  # nopep8
    CONVERSIONS[UnitsPower.FEMTOWATT][UnitsPower.DECAWATT] = \
        Mul(Pow(10, 16), Sym("femtow"))  # nopep8
    CONVERSIONS[UnitsPower.FEMTOWATT][UnitsPower.DECIWATT] = \
        Mul(Pow(10, 14), Sym("femtow"))  # nopep8
    CONVERSIONS[UnitsPower.FEMTOWATT][UnitsPower.EXAWATT] = \
        Mul(Pow(10, 33), Sym("femtow"))  # nopep8
    CONVERSIONS[UnitsPower.FEMTOWATT][UnitsPower.GIGAWATT] = \
        Mul(Pow(10, 24), Sym("femtow"))  # nopep8
    CONVERSIONS[UnitsPower.FEMTOWATT][UnitsPower.HECTOWATT] = \
        Mul(Pow(10, 17), Sym("femtow"))  # nopep8
    CONVERSIONS[UnitsPower.FEMTOWATT][UnitsPower.KILOWATT] = \
        Mul(Pow(10, 18), Sym("femtow"))  # nopep8
    CONVERSIONS[UnitsPower.FEMTOWATT][UnitsPower.MEGAWATT] = \
        Mul(Pow(10, 21), Sym("femtow"))  # nopep8
    CONVERSIONS[UnitsPower.FEMTOWATT][UnitsPower.MICROWATT] = \
        Mul(Pow(10, 9), Sym("femtow"))  # nopep8
    CONVERSIONS[UnitsPower.FEMTOWATT][UnitsPower.MILLIWATT] = \
        Mul(Pow(10, 12), Sym("femtow"))  # nopep8
    CONVERSIONS[UnitsPower.FEMTOWATT][UnitsPower.NANOWATT] = \
        Mul(Pow(10, 6), Sym("femtow"))  # nopep8
    CONVERSIONS[UnitsPower.FEMTOWATT][UnitsPower.PETAWATT] = \
        Mul(Pow(10, 30), Sym("femtow"))  # nopep8
    CONVERSIONS[UnitsPower.FEMTOWATT][UnitsPower.PICOWATT] = \
        Mul(Int(1000), Sym("femtow"))  # nopep8
    CONVERSIONS[UnitsPower.FEMTOWATT][UnitsPower.TERAWATT] = \
        Mul(Pow(10, 27), Sym("femtow"))  # nopep8
    CONVERSIONS[UnitsPower.FEMTOWATT][UnitsPower.WATT] = \
        Mul(Pow(10, 15), Sym("femtow"))  # nopep8
    CONVERSIONS[UnitsPower.FEMTOWATT][UnitsPower.YOCTOWATT] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("femtow"))  # nopep8
    CONVERSIONS[UnitsPower.FEMTOWATT][UnitsPower.YOTTAWATT] = \
        Mul(Pow(10, 39), Sym("femtow"))  # nopep8
    CONVERSIONS[UnitsPower.FEMTOWATT][UnitsPower.ZEPTOWATT] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("femtow"))  # nopep8
    CONVERSIONS[UnitsPower.FEMTOWATT][UnitsPower.ZETTAWATT] = \
        Mul(Pow(10, 36), Sym("femtow"))  # nopep8
    CONVERSIONS[UnitsPower.GIGAWATT][UnitsPower.ATTOWATT] = \
        Mul(Rat(Int(1), Pow(10, 27)), Sym("gigaw"))  # nopep8
    CONVERSIONS[UnitsPower.GIGAWATT][UnitsPower.CENTIWATT] = \
        Mul(Rat(Int(1), Pow(10, 11)), Sym("gigaw"))  # nopep8
    CONVERSIONS[UnitsPower.GIGAWATT][UnitsPower.DECAWATT] = \
        Mul(Rat(Int(1), Pow(10, 8)), Sym("gigaw"))  # nopep8
    CONVERSIONS[UnitsPower.GIGAWATT][UnitsPower.DECIWATT] = \
        Mul(Rat(Int(1), Pow(10, 10)), Sym("gigaw"))  # nopep8
    CONVERSIONS[UnitsPower.GIGAWATT][UnitsPower.EXAWATT] = \
        Mul(Pow(10, 9), Sym("gigaw"))  # nopep8
    CONVERSIONS[UnitsPower.GIGAWATT][UnitsPower.FEMTOWATT] = \
        Mul(Rat(Int(1), Pow(10, 24)), Sym("gigaw"))  # nopep8
    CONVERSIONS[UnitsPower.GIGAWATT][UnitsPower.HECTOWATT] = \
        Mul(Rat(Int(1), Pow(10, 7)), Sym("gigaw"))  # nopep8
    CONVERSIONS[UnitsPower.GIGAWATT][UnitsPower.KILOWATT] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("gigaw"))  # nopep8
    CONVERSIONS[UnitsPower.GIGAWATT][UnitsPower.MEGAWATT] = \
        Mul(Rat(Int(1), Int(1000)), Sym("gigaw"))  # nopep8
    CONVERSIONS[UnitsPower.GIGAWATT][UnitsPower.MICROWATT] = \
        Mul(Rat(Int(1), Pow(10, 15)), Sym("gigaw"))  # nopep8
    CONVERSIONS[UnitsPower.GIGAWATT][UnitsPower.MILLIWATT] = \
        Mul(Rat(Int(1), Pow(10, 12)), Sym("gigaw"))  # nopep8
    CONVERSIONS[UnitsPower.GIGAWATT][UnitsPower.NANOWATT] = \
        Mul(Rat(Int(1), Pow(10, 18)), Sym("gigaw"))  # nopep8
    CONVERSIONS[UnitsPower.GIGAWATT][UnitsPower.PETAWATT] = \
        Mul(Pow(10, 6), Sym("gigaw"))  # nopep8
    CONVERSIONS[UnitsPower.GIGAWATT][UnitsPower.PICOWATT] = \
        Mul(Rat(Int(1), Pow(10, 21)), Sym("gigaw"))  # nopep8
    CONVERSIONS[UnitsPower.GIGAWATT][UnitsPower.TERAWATT] = \
        Mul(Int(1000), Sym("gigaw"))  # nopep8
    CONVERSIONS[UnitsPower.GIGAWATT][UnitsPower.WATT] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("gigaw"))  # nopep8
    CONVERSIONS[UnitsPower.GIGAWATT][UnitsPower.YOCTOWATT] = \
        Mul(Rat(Int(1), Pow(10, 33)), Sym("gigaw"))  # nopep8
    CONVERSIONS[UnitsPower.GIGAWATT][UnitsPower.YOTTAWATT] = \
        Mul(Pow(10, 15), Sym("gigaw"))  # nopep8
    CONVERSIONS[UnitsPower.GIGAWATT][UnitsPower.ZEPTOWATT] = \
        Mul(Rat(Int(1), Pow(10, 30)), Sym("gigaw"))  # nopep8
    CONVERSIONS[UnitsPower.GIGAWATT][UnitsPower.ZETTAWATT] = \
        Mul(Pow(10, 12), Sym("gigaw"))  # nopep8
    CONVERSIONS[UnitsPower.HECTOWATT][UnitsPower.ATTOWATT] = \
        Mul(Rat(Int(1), Pow(10, 20)), Sym("hectow"))  # nopep8
    CONVERSIONS[UnitsPower.HECTOWATT][UnitsPower.CENTIWATT] = \
        Mul(Rat(Int(1), Pow(10, 4)), Sym("hectow"))  # nopep8
    CONVERSIONS[UnitsPower.HECTOWATT][UnitsPower.DECAWATT] = \
        Mul(Rat(Int(1), Int(10)), Sym("hectow"))  # nopep8
    CONVERSIONS[UnitsPower.HECTOWATT][UnitsPower.DECIWATT] = \
        Mul(Rat(Int(1), Int(1000)), Sym("hectow"))  # nopep8
    CONVERSIONS[UnitsPower.HECTOWATT][UnitsPower.EXAWATT] = \
        Mul(Pow(10, 16), Sym("hectow"))  # nopep8
    CONVERSIONS[UnitsPower.HECTOWATT][UnitsPower.FEMTOWATT] = \
        Mul(Rat(Int(1), Pow(10, 17)), Sym("hectow"))  # nopep8
    CONVERSIONS[UnitsPower.HECTOWATT][UnitsPower.GIGAWATT] = \
        Mul(Pow(10, 7), Sym("hectow"))  # nopep8
    CONVERSIONS[UnitsPower.HECTOWATT][UnitsPower.KILOWATT] = \
        Mul(Int(10), Sym("hectow"))  # nopep8
    CONVERSIONS[UnitsPower.HECTOWATT][UnitsPower.MEGAWATT] = \
        Mul(Pow(10, 4), Sym("hectow"))  # nopep8
    CONVERSIONS[UnitsPower.HECTOWATT][UnitsPower.MICROWATT] = \
        Mul(Rat(Int(1), Pow(10, 8)), Sym("hectow"))  # nopep8
    CONVERSIONS[UnitsPower.HECTOWATT][UnitsPower.MILLIWATT] = \
        Mul(Rat(Int(1), Pow(10, 5)), Sym("hectow"))  # nopep8
    CONVERSIONS[UnitsPower.HECTOWATT][UnitsPower.NANOWATT] = \
        Mul(Rat(Int(1), Pow(10, 11)), Sym("hectow"))  # nopep8
    CONVERSIONS[UnitsPower.HECTOWATT][UnitsPower.PETAWATT] = \
        Mul(Pow(10, 13), Sym("hectow"))  # nopep8
    CONVERSIONS[UnitsPower.HECTOWATT][UnitsPower.PICOWATT] = \
        Mul(Rat(Int(1), Pow(10, 14)), Sym("hectow"))  # nopep8
    CONVERSIONS[UnitsPower.HECTOWATT][UnitsPower.TERAWATT] = \
        Mul(Pow(10, 10), Sym("hectow"))  # nopep8
    CONVERSIONS[UnitsPower.HECTOWATT][UnitsPower.WATT] = \
        Mul(Rat(Int(1), Int(100)), Sym("hectow"))  # nopep8
    CONVERSIONS[UnitsPower.HECTOWATT][UnitsPower.YOCTOWATT] = \
        Mul(Rat(Int(1), Pow(10, 26)), Sym("hectow"))  # nopep8
    CONVERSIONS[UnitsPower.HECTOWATT][UnitsPower.YOTTAWATT] = \
        Mul(Pow(10, 22), Sym("hectow"))  # nopep8
    CONVERSIONS[UnitsPower.HECTOWATT][UnitsPower.ZEPTOWATT] = \
        Mul(Rat(Int(1), Pow(10, 23)), Sym("hectow"))  # nopep8
    CONVERSIONS[UnitsPower.HECTOWATT][UnitsPower.ZETTAWATT] = \
        Mul(Pow(10, 19), Sym("hectow"))  # nopep8
    CONVERSIONS[UnitsPower.KILOWATT][UnitsPower.ATTOWATT] = \
        Mul(Rat(Int(1), Pow(10, 21)), Sym("kilow"))  # nopep8
    CONVERSIONS[UnitsPower.KILOWATT][UnitsPower.CENTIWATT] = \
        Mul(Rat(Int(1), Pow(10, 5)), Sym("kilow"))  # nopep8
    CONVERSIONS[UnitsPower.KILOWATT][UnitsPower.DECAWATT] = \
        Mul(Rat(Int(1), Int(100)), Sym("kilow"))  # nopep8
    CONVERSIONS[UnitsPower.KILOWATT][UnitsPower.DECIWATT] = \
        Mul(Rat(Int(1), Pow(10, 4)), Sym("kilow"))  # nopep8
    CONVERSIONS[UnitsPower.KILOWATT][UnitsPower.EXAWATT] = \
        Mul(Pow(10, 15), Sym("kilow"))  # nopep8
    CONVERSIONS[UnitsPower.KILOWATT][UnitsPower.FEMTOWATT] = \
        Mul(Rat(Int(1), Pow(10, 18)), Sym("kilow"))  # nopep8
    CONVERSIONS[UnitsPower.KILOWATT][UnitsPower.GIGAWATT] = \
        Mul(Pow(10, 6), Sym("kilow"))  # nopep8
    CONVERSIONS[UnitsPower.KILOWATT][UnitsPower.HECTOWATT] = \
        Mul(Rat(Int(1), Int(10)), Sym("kilow"))  # nopep8
    CONVERSIONS[UnitsPower.KILOWATT][UnitsPower.MEGAWATT] = \
        Mul(Int(1000), Sym("kilow"))  # nopep8
    CONVERSIONS[UnitsPower.KILOWATT][UnitsPower.MICROWATT] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("kilow"))  # nopep8
    CONVERSIONS[UnitsPower.KILOWATT][UnitsPower.MILLIWATT] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("kilow"))  # nopep8
    CONVERSIONS[UnitsPower.KILOWATT][UnitsPower.NANOWATT] = \
        Mul(Rat(Int(1), Pow(10, 12)), Sym("kilow"))  # nopep8
    CONVERSIONS[UnitsPower.KILOWATT][UnitsPower.PETAWATT] = \
        Mul(Pow(10, 12), Sym("kilow"))  # nopep8
    CONVERSIONS[UnitsPower.KILOWATT][UnitsPower.PICOWATT] = \
        Mul(Rat(Int(1), Pow(10, 15)), Sym("kilow"))  # nopep8
    CONVERSIONS[UnitsPower.KILOWATT][UnitsPower.TERAWATT] = \
        Mul(Pow(10, 9), Sym("kilow"))  # nopep8
    CONVERSIONS[UnitsPower.KILOWATT][UnitsPower.WATT] = \
        Mul(Rat(Int(1), Int(1000)), Sym("kilow"))  # nopep8
    CONVERSIONS[UnitsPower.KILOWATT][UnitsPower.YOCTOWATT] = \
        Mul(Rat(Int(1), Pow(10, 27)), Sym("kilow"))  # nopep8
    CONVERSIONS[UnitsPower.KILOWATT][UnitsPower.YOTTAWATT] = \
        Mul(Pow(10, 21), Sym("kilow"))  # nopep8
    CONVERSIONS[UnitsPower.KILOWATT][UnitsPower.ZEPTOWATT] = \
        Mul(Rat(Int(1), Pow(10, 24)), Sym("kilow"))  # nopep8
    CONVERSIONS[UnitsPower.KILOWATT][UnitsPower.ZETTAWATT] = \
        Mul(Pow(10, 18), Sym("kilow"))  # nopep8
    CONVERSIONS[UnitsPower.MEGAWATT][UnitsPower.ATTOWATT] = \
        Mul(Rat(Int(1), Pow(10, 24)), Sym("megaw"))  # nopep8
    CONVERSIONS[UnitsPower.MEGAWATT][UnitsPower.CENTIWATT] = \
        Mul(Rat(Int(1), Pow(10, 8)), Sym("megaw"))  # nopep8
    CONVERSIONS[UnitsPower.MEGAWATT][UnitsPower.DECAWATT] = \
        Mul(Rat(Int(1), Pow(10, 5)), Sym("megaw"))  # nopep8
    CONVERSIONS[UnitsPower.MEGAWATT][UnitsPower.DECIWATT] = \
        Mul(Rat(Int(1), Pow(10, 7)), Sym("megaw"))  # nopep8
    CONVERSIONS[UnitsPower.MEGAWATT][UnitsPower.EXAWATT] = \
        Mul(Pow(10, 12), Sym("megaw"))  # nopep8
    CONVERSIONS[UnitsPower.MEGAWATT][UnitsPower.FEMTOWATT] = \
        Mul(Rat(Int(1), Pow(10, 21)), Sym("megaw"))  # nopep8
    CONVERSIONS[UnitsPower.MEGAWATT][UnitsPower.GIGAWATT] = \
        Mul(Int(1000), Sym("megaw"))  # nopep8
    CONVERSIONS[UnitsPower.MEGAWATT][UnitsPower.HECTOWATT] = \
        Mul(Rat(Int(1), Pow(10, 4)), Sym("megaw"))  # nopep8
    CONVERSIONS[UnitsPower.MEGAWATT][UnitsPower.KILOWATT] = \
        Mul(Rat(Int(1), Int(1000)), Sym("megaw"))  # nopep8
    CONVERSIONS[UnitsPower.MEGAWATT][UnitsPower.MICROWATT] = \
        Mul(Rat(Int(1), Pow(10, 12)), Sym("megaw"))  # nopep8
    CONVERSIONS[UnitsPower.MEGAWATT][UnitsPower.MILLIWATT] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("megaw"))  # nopep8
    CONVERSIONS[UnitsPower.MEGAWATT][UnitsPower.NANOWATT] = \
        Mul(Rat(Int(1), Pow(10, 15)), Sym("megaw"))  # nopep8
    CONVERSIONS[UnitsPower.MEGAWATT][UnitsPower.PETAWATT] = \
        Mul(Pow(10, 9), Sym("megaw"))  # nopep8
    CONVERSIONS[UnitsPower.MEGAWATT][UnitsPower.PICOWATT] = \
        Mul(Rat(Int(1), Pow(10, 18)), Sym("megaw"))  # nopep8
    CONVERSIONS[UnitsPower.MEGAWATT][UnitsPower.TERAWATT] = \
        Mul(Pow(10, 6), Sym("megaw"))  # nopep8
    CONVERSIONS[UnitsPower.MEGAWATT][UnitsPower.WATT] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("megaw"))  # nopep8
    CONVERSIONS[UnitsPower.MEGAWATT][UnitsPower.YOCTOWATT] = \
        Mul(Rat(Int(1), Pow(10, 30)), Sym("megaw"))  # nopep8
    CONVERSIONS[UnitsPower.MEGAWATT][UnitsPower.YOTTAWATT] = \
        Mul(Pow(10, 18), Sym("megaw"))  # nopep8
    CONVERSIONS[UnitsPower.MEGAWATT][UnitsPower.ZEPTOWATT] = \
        Mul(Rat(Int(1), Pow(10, 27)), Sym("megaw"))  # nopep8
    CONVERSIONS[UnitsPower.MEGAWATT][UnitsPower.ZETTAWATT] = \
        Mul(Pow(10, 15), Sym("megaw"))  # nopep8
    CONVERSIONS[UnitsPower.MICROWATT][UnitsPower.ATTOWATT] = \
        Mul(Rat(Int(1), Pow(10, 12)), Sym("microw"))  # nopep8
    CONVERSIONS[UnitsPower.MICROWATT][UnitsPower.CENTIWATT] = \
        Mul(Pow(10, 4), Sym("microw"))  # nopep8
    CONVERSIONS[UnitsPower.MICROWATT][UnitsPower.DECAWATT] = \
        Mul(Pow(10, 7), Sym("microw"))  # nopep8
    CONVERSIONS[UnitsPower.MICROWATT][UnitsPower.DECIWATT] = \
        Mul(Pow(10, 5), Sym("microw"))  # nopep8
    CONVERSIONS[UnitsPower.MICROWATT][UnitsPower.EXAWATT] = \
        Mul(Pow(10, 24), Sym("microw"))  # nopep8
    CONVERSIONS[UnitsPower.MICROWATT][UnitsPower.FEMTOWATT] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("microw"))  # nopep8
    CONVERSIONS[UnitsPower.MICROWATT][UnitsPower.GIGAWATT] = \
        Mul(Pow(10, 15), Sym("microw"))  # nopep8
    CONVERSIONS[UnitsPower.MICROWATT][UnitsPower.HECTOWATT] = \
        Mul(Pow(10, 8), Sym("microw"))  # nopep8
    CONVERSIONS[UnitsPower.MICROWATT][UnitsPower.KILOWATT] = \
        Mul(Pow(10, 9), Sym("microw"))  # nopep8
    CONVERSIONS[UnitsPower.MICROWATT][UnitsPower.MEGAWATT] = \
        Mul(Pow(10, 12), Sym("microw"))  # nopep8
    CONVERSIONS[UnitsPower.MICROWATT][UnitsPower.MILLIWATT] = \
        Mul(Int(1000), Sym("microw"))  # nopep8
    CONVERSIONS[UnitsPower.MICROWATT][UnitsPower.NANOWATT] = \
        Mul(Rat(Int(1), Int(1000)), Sym("microw"))  # nopep8
    CONVERSIONS[UnitsPower.MICROWATT][UnitsPower.PETAWATT] = \
        Mul(Pow(10, 21), Sym("microw"))  # nopep8
    CONVERSIONS[UnitsPower.MICROWATT][UnitsPower.PICOWATT] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("microw"))  # nopep8
    CONVERSIONS[UnitsPower.MICROWATT][UnitsPower.TERAWATT] = \
        Mul(Pow(10, 18), Sym("microw"))  # nopep8
    CONVERSIONS[UnitsPower.MICROWATT][UnitsPower.WATT] = \
        Mul(Pow(10, 6), Sym("microw"))  # nopep8
    CONVERSIONS[UnitsPower.MICROWATT][UnitsPower.YOCTOWATT] = \
        Mul(Rat(Int(1), Pow(10, 18)), Sym("microw"))  # nopep8
    CONVERSIONS[UnitsPower.MICROWATT][UnitsPower.YOTTAWATT] = \
        Mul(Pow(10, 30), Sym("microw"))  # nopep8
    CONVERSIONS[UnitsPower.MICROWATT][UnitsPower.ZEPTOWATT] = \
        Mul(Rat(Int(1), Pow(10, 15)), Sym("microw"))  # nopep8
    CONVERSIONS[UnitsPower.MICROWATT][UnitsPower.ZETTAWATT] = \
        Mul(Pow(10, 27), Sym("microw"))  # nopep8
    CONVERSIONS[UnitsPower.MILLIWATT][UnitsPower.ATTOWATT] = \
        Mul(Rat(Int(1), Pow(10, 15)), Sym("milliw"))  # nopep8
    CONVERSIONS[UnitsPower.MILLIWATT][UnitsPower.CENTIWATT] = \
        Mul(Int(10), Sym("milliw"))  # nopep8
    CONVERSIONS[UnitsPower.MILLIWATT][UnitsPower.DECAWATT] = \
        Mul(Pow(10, 4), Sym("milliw"))  # nopep8
    CONVERSIONS[UnitsPower.MILLIWATT][UnitsPower.DECIWATT] = \
        Mul(Int(100), Sym("milliw"))  # nopep8
    CONVERSIONS[UnitsPower.MILLIWATT][UnitsPower.EXAWATT] = \
        Mul(Pow(10, 21), Sym("milliw"))  # nopep8
    CONVERSIONS[UnitsPower.MILLIWATT][UnitsPower.FEMTOWATT] = \
        Mul(Rat(Int(1), Pow(10, 12)), Sym("milliw"))  # nopep8
    CONVERSIONS[UnitsPower.MILLIWATT][UnitsPower.GIGAWATT] = \
        Mul(Pow(10, 12), Sym("milliw"))  # nopep8
    CONVERSIONS[UnitsPower.MILLIWATT][UnitsPower.HECTOWATT] = \
        Mul(Pow(10, 5), Sym("milliw"))  # nopep8
    CONVERSIONS[UnitsPower.MILLIWATT][UnitsPower.KILOWATT] = \
        Mul(Pow(10, 6), Sym("milliw"))  # nopep8
    CONVERSIONS[UnitsPower.MILLIWATT][UnitsPower.MEGAWATT] = \
        Mul(Pow(10, 9), Sym("milliw"))  # nopep8
    CONVERSIONS[UnitsPower.MILLIWATT][UnitsPower.MICROWATT] = \
        Mul(Rat(Int(1), Int(1000)), Sym("milliw"))  # nopep8
    CONVERSIONS[UnitsPower.MILLIWATT][UnitsPower.NANOWATT] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("milliw"))  # nopep8
    CONVERSIONS[UnitsPower.MILLIWATT][UnitsPower.PETAWATT] = \
        Mul(Pow(10, 18), Sym("milliw"))  # nopep8
    CONVERSIONS[UnitsPower.MILLIWATT][UnitsPower.PICOWATT] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("milliw"))  # nopep8
    CONVERSIONS[UnitsPower.MILLIWATT][UnitsPower.TERAWATT] = \
        Mul(Pow(10, 15), Sym("milliw"))  # nopep8
    CONVERSIONS[UnitsPower.MILLIWATT][UnitsPower.WATT] = \
        Mul(Int(1000), Sym("milliw"))  # nopep8
    CONVERSIONS[UnitsPower.MILLIWATT][UnitsPower.YOCTOWATT] = \
        Mul(Rat(Int(1), Pow(10, 21)), Sym("milliw"))  # nopep8
    CONVERSIONS[UnitsPower.MILLIWATT][UnitsPower.YOTTAWATT] = \
        Mul(Pow(10, 27), Sym("milliw"))  # nopep8
    CONVERSIONS[UnitsPower.MILLIWATT][UnitsPower.ZEPTOWATT] = \
        Mul(Rat(Int(1), Pow(10, 18)), Sym("milliw"))  # nopep8
    CONVERSIONS[UnitsPower.MILLIWATT][UnitsPower.ZETTAWATT] = \
        Mul(Pow(10, 24), Sym("milliw"))  # nopep8
    CONVERSIONS[UnitsPower.NANOWATT][UnitsPower.ATTOWATT] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("nanow"))  # nopep8
    CONVERSIONS[UnitsPower.NANOWATT][UnitsPower.CENTIWATT] = \
        Mul(Pow(10, 7), Sym("nanow"))  # nopep8
    CONVERSIONS[UnitsPower.NANOWATT][UnitsPower.DECAWATT] = \
        Mul(Pow(10, 10), Sym("nanow"))  # nopep8
    CONVERSIONS[UnitsPower.NANOWATT][UnitsPower.DECIWATT] = \
        Mul(Pow(10, 8), Sym("nanow"))  # nopep8
    CONVERSIONS[UnitsPower.NANOWATT][UnitsPower.EXAWATT] = \
        Mul(Pow(10, 27), Sym("nanow"))  # nopep8
    CONVERSIONS[UnitsPower.NANOWATT][UnitsPower.FEMTOWATT] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("nanow"))  # nopep8
    CONVERSIONS[UnitsPower.NANOWATT][UnitsPower.GIGAWATT] = \
        Mul(Pow(10, 18), Sym("nanow"))  # nopep8
    CONVERSIONS[UnitsPower.NANOWATT][UnitsPower.HECTOWATT] = \
        Mul(Pow(10, 11), Sym("nanow"))  # nopep8
    CONVERSIONS[UnitsPower.NANOWATT][UnitsPower.KILOWATT] = \
        Mul(Pow(10, 12), Sym("nanow"))  # nopep8
    CONVERSIONS[UnitsPower.NANOWATT][UnitsPower.MEGAWATT] = \
        Mul(Pow(10, 15), Sym("nanow"))  # nopep8
    CONVERSIONS[UnitsPower.NANOWATT][UnitsPower.MICROWATT] = \
        Mul(Int(1000), Sym("nanow"))  # nopep8
    CONVERSIONS[UnitsPower.NANOWATT][UnitsPower.MILLIWATT] = \
        Mul(Pow(10, 6), Sym("nanow"))  # nopep8
    CONVERSIONS[UnitsPower.NANOWATT][UnitsPower.PETAWATT] = \
        Mul(Pow(10, 24), Sym("nanow"))  # nopep8
    CONVERSIONS[UnitsPower.NANOWATT][UnitsPower.PICOWATT] = \
        Mul(Rat(Int(1), Int(1000)), Sym("nanow"))  # nopep8
    CONVERSIONS[UnitsPower.NANOWATT][UnitsPower.TERAWATT] = \
        Mul(Pow(10, 21), Sym("nanow"))  # nopep8
    CONVERSIONS[UnitsPower.NANOWATT][UnitsPower.WATT] = \
        Mul(Pow(10, 9), Sym("nanow"))  # nopep8
    CONVERSIONS[UnitsPower.NANOWATT][UnitsPower.YOCTOWATT] = \
        Mul(Rat(Int(1), Pow(10, 15)), Sym("nanow"))  # nopep8
    CONVERSIONS[UnitsPower.NANOWATT][UnitsPower.YOTTAWATT] = \
        Mul(Pow(10, 33), Sym("nanow"))  # nopep8
    CONVERSIONS[UnitsPower.NANOWATT][UnitsPower.ZEPTOWATT] = \
        Mul(Rat(Int(1), Pow(10, 12)), Sym("nanow"))  # nopep8
    CONVERSIONS[UnitsPower.NANOWATT][UnitsPower.ZETTAWATT] = \
        Mul(Pow(10, 30), Sym("nanow"))  # nopep8
    CONVERSIONS[UnitsPower.PETAWATT][UnitsPower.ATTOWATT] = \
        Mul(Rat(Int(1), Pow(10, 33)), Sym("petaw"))  # nopep8
    CONVERSIONS[UnitsPower.PETAWATT][UnitsPower.CENTIWATT] = \
        Mul(Rat(Int(1), Pow(10, 17)), Sym("petaw"))  # nopep8
    CONVERSIONS[UnitsPower.PETAWATT][UnitsPower.DECAWATT] = \
        Mul(Rat(Int(1), Pow(10, 14)), Sym("petaw"))  # nopep8
    CONVERSIONS[UnitsPower.PETAWATT][UnitsPower.DECIWATT] = \
        Mul(Rat(Int(1), Pow(10, 16)), Sym("petaw"))  # nopep8
    CONVERSIONS[UnitsPower.PETAWATT][UnitsPower.EXAWATT] = \
        Mul(Int(1000), Sym("petaw"))  # nopep8
    CONVERSIONS[UnitsPower.PETAWATT][UnitsPower.FEMTOWATT] = \
        Mul(Rat(Int(1), Pow(10, 30)), Sym("petaw"))  # nopep8
    CONVERSIONS[UnitsPower.PETAWATT][UnitsPower.GIGAWATT] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("petaw"))  # nopep8
    CONVERSIONS[UnitsPower.PETAWATT][UnitsPower.HECTOWATT] = \
        Mul(Rat(Int(1), Pow(10, 13)), Sym("petaw"))  # nopep8
    CONVERSIONS[UnitsPower.PETAWATT][UnitsPower.KILOWATT] = \
        Mul(Rat(Int(1), Pow(10, 12)), Sym("petaw"))  # nopep8
    CONVERSIONS[UnitsPower.PETAWATT][UnitsPower.MEGAWATT] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("petaw"))  # nopep8
    CONVERSIONS[UnitsPower.PETAWATT][UnitsPower.MICROWATT] = \
        Mul(Rat(Int(1), Pow(10, 21)), Sym("petaw"))  # nopep8
    CONVERSIONS[UnitsPower.PETAWATT][UnitsPower.MILLIWATT] = \
        Mul(Rat(Int(1), Pow(10, 18)), Sym("petaw"))  # nopep8
    CONVERSIONS[UnitsPower.PETAWATT][UnitsPower.NANOWATT] = \
        Mul(Rat(Int(1), Pow(10, 24)), Sym("petaw"))  # nopep8
    CONVERSIONS[UnitsPower.PETAWATT][UnitsPower.PICOWATT] = \
        Mul(Rat(Int(1), Pow(10, 27)), Sym("petaw"))  # nopep8
    CONVERSIONS[UnitsPower.PETAWATT][UnitsPower.TERAWATT] = \
        Mul(Rat(Int(1), Int(1000)), Sym("petaw"))  # nopep8
    CONVERSIONS[UnitsPower.PETAWATT][UnitsPower.WATT] = \
        Mul(Rat(Int(1), Pow(10, 15)), Sym("petaw"))  # nopep8
    CONVERSIONS[UnitsPower.PETAWATT][UnitsPower.YOCTOWATT] = \
        Mul(Rat(Int(1), Pow(10, 39)), Sym("petaw"))  # nopep8
    CONVERSIONS[UnitsPower.PETAWATT][UnitsPower.YOTTAWATT] = \
        Mul(Pow(10, 9), Sym("petaw"))  # nopep8
    CONVERSIONS[UnitsPower.PETAWATT][UnitsPower.ZEPTOWATT] = \
        Mul(Rat(Int(1), Pow(10, 36)), Sym("petaw"))  # nopep8
    CONVERSIONS[UnitsPower.PETAWATT][UnitsPower.ZETTAWATT] = \
        Mul(Pow(10, 6), Sym("petaw"))  # nopep8
    CONVERSIONS[UnitsPower.PICOWATT][UnitsPower.ATTOWATT] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("picow"))  # nopep8
    CONVERSIONS[UnitsPower.PICOWATT][UnitsPower.CENTIWATT] = \
        Mul(Pow(10, 10), Sym("picow"))  # nopep8
    CONVERSIONS[UnitsPower.PICOWATT][UnitsPower.DECAWATT] = \
        Mul(Pow(10, 13), Sym("picow"))  # nopep8
    CONVERSIONS[UnitsPower.PICOWATT][UnitsPower.DECIWATT] = \
        Mul(Pow(10, 11), Sym("picow"))  # nopep8
    CONVERSIONS[UnitsPower.PICOWATT][UnitsPower.EXAWATT] = \
        Mul(Pow(10, 30), Sym("picow"))  # nopep8
    CONVERSIONS[UnitsPower.PICOWATT][UnitsPower.FEMTOWATT] = \
        Mul(Rat(Int(1), Int(1000)), Sym("picow"))  # nopep8
    CONVERSIONS[UnitsPower.PICOWATT][UnitsPower.GIGAWATT] = \
        Mul(Pow(10, 21), Sym("picow"))  # nopep8
    CONVERSIONS[UnitsPower.PICOWATT][UnitsPower.HECTOWATT] = \
        Mul(Pow(10, 14), Sym("picow"))  # nopep8
    CONVERSIONS[UnitsPower.PICOWATT][UnitsPower.KILOWATT] = \
        Mul(Pow(10, 15), Sym("picow"))  # nopep8
    CONVERSIONS[UnitsPower.PICOWATT][UnitsPower.MEGAWATT] = \
        Mul(Pow(10, 18), Sym("picow"))  # nopep8
    CONVERSIONS[UnitsPower.PICOWATT][UnitsPower.MICROWATT] = \
        Mul(Pow(10, 6), Sym("picow"))  # nopep8
    CONVERSIONS[UnitsPower.PICOWATT][UnitsPower.MILLIWATT] = \
        Mul(Pow(10, 9), Sym("picow"))  # nopep8
    CONVERSIONS[UnitsPower.PICOWATT][UnitsPower.NANOWATT] = \
        Mul(Int(1000), Sym("picow"))  # nopep8
    CONVERSIONS[UnitsPower.PICOWATT][UnitsPower.PETAWATT] = \
        Mul(Pow(10, 27), Sym("picow"))  # nopep8
    CONVERSIONS[UnitsPower.PICOWATT][UnitsPower.TERAWATT] = \
        Mul(Pow(10, 24), Sym("picow"))  # nopep8
    CONVERSIONS[UnitsPower.PICOWATT][UnitsPower.WATT] = \
        Mul(Pow(10, 12), Sym("picow"))  # nopep8
    CONVERSIONS[UnitsPower.PICOWATT][UnitsPower.YOCTOWATT] = \
        Mul(Rat(Int(1), Pow(10, 12)), Sym("picow"))  # nopep8
    CONVERSIONS[UnitsPower.PICOWATT][UnitsPower.YOTTAWATT] = \
        Mul(Pow(10, 36), Sym("picow"))  # nopep8
    CONVERSIONS[UnitsPower.PICOWATT][UnitsPower.ZEPTOWATT] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("picow"))  # nopep8
    CONVERSIONS[UnitsPower.PICOWATT][UnitsPower.ZETTAWATT] = \
        Mul(Pow(10, 33), Sym("picow"))  # nopep8
    CONVERSIONS[UnitsPower.TERAWATT][UnitsPower.ATTOWATT] = \
        Mul(Rat(Int(1), Pow(10, 30)), Sym("teraw"))  # nopep8
    CONVERSIONS[UnitsPower.TERAWATT][UnitsPower.CENTIWATT] = \
        Mul(Rat(Int(1), Pow(10, 14)), Sym("teraw"))  # nopep8
    CONVERSIONS[UnitsPower.TERAWATT][UnitsPower.DECAWATT] = \
        Mul(Rat(Int(1), Pow(10, 11)), Sym("teraw"))  # nopep8
    CONVERSIONS[UnitsPower.TERAWATT][UnitsPower.DECIWATT] = \
        Mul(Rat(Int(1), Pow(10, 13)), Sym("teraw"))  # nopep8
    CONVERSIONS[UnitsPower.TERAWATT][UnitsPower.EXAWATT] = \
        Mul(Pow(10, 6), Sym("teraw"))  # nopep8
    CONVERSIONS[UnitsPower.TERAWATT][UnitsPower.FEMTOWATT] = \
        Mul(Rat(Int(1), Pow(10, 27)), Sym("teraw"))  # nopep8
    CONVERSIONS[UnitsPower.TERAWATT][UnitsPower.GIGAWATT] = \
        Mul(Rat(Int(1), Int(1000)), Sym("teraw"))  # nopep8
    CONVERSIONS[UnitsPower.TERAWATT][UnitsPower.HECTOWATT] = \
        Mul(Rat(Int(1), Pow(10, 10)), Sym("teraw"))  # nopep8
    CONVERSIONS[UnitsPower.TERAWATT][UnitsPower.KILOWATT] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("teraw"))  # nopep8
    CONVERSIONS[UnitsPower.TERAWATT][UnitsPower.MEGAWATT] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("teraw"))  # nopep8
    CONVERSIONS[UnitsPower.TERAWATT][UnitsPower.MICROWATT] = \
        Mul(Rat(Int(1), Pow(10, 18)), Sym("teraw"))  # nopep8
    CONVERSIONS[UnitsPower.TERAWATT][UnitsPower.MILLIWATT] = \
        Mul(Rat(Int(1), Pow(10, 15)), Sym("teraw"))  # nopep8
    CONVERSIONS[UnitsPower.TERAWATT][UnitsPower.NANOWATT] = \
        Mul(Rat(Int(1), Pow(10, 21)), Sym("teraw"))  # nopep8
    CONVERSIONS[UnitsPower.TERAWATT][UnitsPower.PETAWATT] = \
        Mul(Int(1000), Sym("teraw"))  # nopep8
    CONVERSIONS[UnitsPower.TERAWATT][UnitsPower.PICOWATT] = \
        Mul(Rat(Int(1), Pow(10, 24)), Sym("teraw"))  # nopep8
    CONVERSIONS[UnitsPower.TERAWATT][UnitsPower.WATT] = \
        Mul(Rat(Int(1), Pow(10, 12)), Sym("teraw"))  # nopep8
    CONVERSIONS[UnitsPower.TERAWATT][UnitsPower.YOCTOWATT] = \
        Mul(Rat(Int(1), Pow(10, 36)), Sym("teraw"))  # nopep8
    CONVERSIONS[UnitsPower.TERAWATT][UnitsPower.YOTTAWATT] = \
        Mul(Pow(10, 12), Sym("teraw"))  # nopep8
    CONVERSIONS[UnitsPower.TERAWATT][UnitsPower.ZEPTOWATT] = \
        Mul(Rat(Int(1), Pow(10, 33)), Sym("teraw"))  # nopep8
    CONVERSIONS[UnitsPower.TERAWATT][UnitsPower.ZETTAWATT] = \
        Mul(Pow(10, 9), Sym("teraw"))  # nopep8
    CONVERSIONS[UnitsPower.WATT][UnitsPower.ATTOWATT] = \
        Mul(Rat(Int(1), Pow(10, 18)), Sym("w"))  # nopep8
    CONVERSIONS[UnitsPower.WATT][UnitsPower.CENTIWATT] = \
        Mul(Rat(Int(1), Int(100)), Sym("w"))  # nopep8
    CONVERSIONS[UnitsPower.WATT][UnitsPower.DECAWATT] = \
        Mul(Int(10), Sym("w"))  # nopep8
    CONVERSIONS[UnitsPower.WATT][UnitsPower.DECIWATT] = \
        Mul(Rat(Int(1), Int(10)), Sym("w"))  # nopep8
    CONVERSIONS[UnitsPower.WATT][UnitsPower.EXAWATT] = \
        Mul(Pow(10, 18), Sym("w"))  # nopep8
    CONVERSIONS[UnitsPower.WATT][UnitsPower.FEMTOWATT] = \
        Mul(Rat(Int(1), Pow(10, 15)), Sym("w"))  # nopep8
    CONVERSIONS[UnitsPower.WATT][UnitsPower.GIGAWATT] = \
        Mul(Pow(10, 9), Sym("w"))  # nopep8
    CONVERSIONS[UnitsPower.WATT][UnitsPower.HECTOWATT] = \
        Mul(Int(100), Sym("w"))  # nopep8
    CONVERSIONS[UnitsPower.WATT][UnitsPower.KILOWATT] = \
        Mul(Int(1000), Sym("w"))  # nopep8
    CONVERSIONS[UnitsPower.WATT][UnitsPower.MEGAWATT] = \
        Mul(Pow(10, 6), Sym("w"))  # nopep8
    CONVERSIONS[UnitsPower.WATT][UnitsPower.MICROWATT] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("w"))  # nopep8
    CONVERSIONS[UnitsPower.WATT][UnitsPower.MILLIWATT] = \
        Mul(Rat(Int(1), Int(1000)), Sym("w"))  # nopep8
    CONVERSIONS[UnitsPower.WATT][UnitsPower.NANOWATT] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("w"))  # nopep8
    CONVERSIONS[UnitsPower.WATT][UnitsPower.PETAWATT] = \
        Mul(Pow(10, 15), Sym("w"))  # nopep8
    CONVERSIONS[UnitsPower.WATT][UnitsPower.PICOWATT] = \
        Mul(Rat(Int(1), Pow(10, 12)), Sym("w"))  # nopep8
    CONVERSIONS[UnitsPower.WATT][UnitsPower.TERAWATT] = \
        Mul(Pow(10, 12), Sym("w"))  # nopep8
    CONVERSIONS[UnitsPower.WATT][UnitsPower.YOCTOWATT] = \
        Mul(Rat(Int(1), Pow(10, 24)), Sym("w"))  # nopep8
    CONVERSIONS[UnitsPower.WATT][UnitsPower.YOTTAWATT] = \
        Mul(Pow(10, 24), Sym("w"))  # nopep8
    CONVERSIONS[UnitsPower.WATT][UnitsPower.ZEPTOWATT] = \
        Mul(Rat(Int(1), Pow(10, 21)), Sym("w"))  # nopep8
    CONVERSIONS[UnitsPower.WATT][UnitsPower.ZETTAWATT] = \
        Mul(Pow(10, 21), Sym("w"))  # nopep8
    CONVERSIONS[UnitsPower.YOCTOWATT][UnitsPower.ATTOWATT] = \
        Mul(Pow(10, 6), Sym("yoctow"))  # nopep8
    CONVERSIONS[UnitsPower.YOCTOWATT][UnitsPower.CENTIWATT] = \
        Mul(Pow(10, 22), Sym("yoctow"))  # nopep8
    CONVERSIONS[UnitsPower.YOCTOWATT][UnitsPower.DECAWATT] = \
        Mul(Pow(10, 25), Sym("yoctow"))  # nopep8
    CONVERSIONS[UnitsPower.YOCTOWATT][UnitsPower.DECIWATT] = \
        Mul(Pow(10, 23), Sym("yoctow"))  # nopep8
    CONVERSIONS[UnitsPower.YOCTOWATT][UnitsPower.EXAWATT] = \
        Mul(Pow(10, 42), Sym("yoctow"))  # nopep8
    CONVERSIONS[UnitsPower.YOCTOWATT][UnitsPower.FEMTOWATT] = \
        Mul(Pow(10, 9), Sym("yoctow"))  # nopep8
    CONVERSIONS[UnitsPower.YOCTOWATT][UnitsPower.GIGAWATT] = \
        Mul(Pow(10, 33), Sym("yoctow"))  # nopep8
    CONVERSIONS[UnitsPower.YOCTOWATT][UnitsPower.HECTOWATT] = \
        Mul(Pow(10, 26), Sym("yoctow"))  # nopep8
    CONVERSIONS[UnitsPower.YOCTOWATT][UnitsPower.KILOWATT] = \
        Mul(Pow(10, 27), Sym("yoctow"))  # nopep8
    CONVERSIONS[UnitsPower.YOCTOWATT][UnitsPower.MEGAWATT] = \
        Mul(Pow(10, 30), Sym("yoctow"))  # nopep8
    CONVERSIONS[UnitsPower.YOCTOWATT][UnitsPower.MICROWATT] = \
        Mul(Pow(10, 18), Sym("yoctow"))  # nopep8
    CONVERSIONS[UnitsPower.YOCTOWATT][UnitsPower.MILLIWATT] = \
        Mul(Pow(10, 21), Sym("yoctow"))  # nopep8
    CONVERSIONS[UnitsPower.YOCTOWATT][UnitsPower.NANOWATT] = \
        Mul(Pow(10, 15), Sym("yoctow"))  # nopep8
    CONVERSIONS[UnitsPower.YOCTOWATT][UnitsPower.PETAWATT] = \
        Mul(Pow(10, 39), Sym("yoctow"))  # nopep8
    CONVERSIONS[UnitsPower.YOCTOWATT][UnitsPower.PICOWATT] = \
        Mul(Pow(10, 12), Sym("yoctow"))  # nopep8
    CONVERSIONS[UnitsPower.YOCTOWATT][UnitsPower.TERAWATT] = \
        Mul(Pow(10, 36), Sym("yoctow"))  # nopep8
    CONVERSIONS[UnitsPower.YOCTOWATT][UnitsPower.WATT] = \
        Mul(Pow(10, 24), Sym("yoctow"))  # nopep8
    CONVERSIONS[UnitsPower.YOCTOWATT][UnitsPower.YOTTAWATT] = \
        Mul(Pow(10, 48), Sym("yoctow"))  # nopep8
    CONVERSIONS[UnitsPower.YOCTOWATT][UnitsPower.ZEPTOWATT] = \
        Mul(Int(1000), Sym("yoctow"))  # nopep8
    CONVERSIONS[UnitsPower.YOCTOWATT][UnitsPower.ZETTAWATT] = \
        Mul(Pow(10, 45), Sym("yoctow"))  # nopep8
    CONVERSIONS[UnitsPower.YOTTAWATT][UnitsPower.ATTOWATT] = \
        Mul(Rat(Int(1), Pow(10, 42)), Sym("yottaw"))  # nopep8
    CONVERSIONS[UnitsPower.YOTTAWATT][UnitsPower.CENTIWATT] = \
        Mul(Rat(Int(1), Pow(10, 26)), Sym("yottaw"))  # nopep8
    CONVERSIONS[UnitsPower.YOTTAWATT][UnitsPower.DECAWATT] = \
        Mul(Rat(Int(1), Pow(10, 23)), Sym("yottaw"))  # nopep8
    CONVERSIONS[UnitsPower.YOTTAWATT][UnitsPower.DECIWATT] = \
        Mul(Rat(Int(1), Pow(10, 25)), Sym("yottaw"))  # nopep8
    CONVERSIONS[UnitsPower.YOTTAWATT][UnitsPower.EXAWATT] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("yottaw"))  # nopep8
    CONVERSIONS[UnitsPower.YOTTAWATT][UnitsPower.FEMTOWATT] = \
        Mul(Rat(Int(1), Pow(10, 39)), Sym("yottaw"))  # nopep8
    CONVERSIONS[UnitsPower.YOTTAWATT][UnitsPower.GIGAWATT] = \
        Mul(Rat(Int(1), Pow(10, 15)), Sym("yottaw"))  # nopep8
    CONVERSIONS[UnitsPower.YOTTAWATT][UnitsPower.HECTOWATT] = \
        Mul(Rat(Int(1), Pow(10, 22)), Sym("yottaw"))  # nopep8
    CONVERSIONS[UnitsPower.YOTTAWATT][UnitsPower.KILOWATT] = \
        Mul(Rat(Int(1), Pow(10, 21)), Sym("yottaw"))  # nopep8
    CONVERSIONS[UnitsPower.YOTTAWATT][UnitsPower.MEGAWATT] = \
        Mul(Rat(Int(1), Pow(10, 18)), Sym("yottaw"))  # nopep8
    CONVERSIONS[UnitsPower.YOTTAWATT][UnitsPower.MICROWATT] = \
        Mul(Rat(Int(1), Pow(10, 30)), Sym("yottaw"))  # nopep8
    CONVERSIONS[UnitsPower.YOTTAWATT][UnitsPower.MILLIWATT] = \
        Mul(Rat(Int(1), Pow(10, 27)), Sym("yottaw"))  # nopep8
    CONVERSIONS[UnitsPower.YOTTAWATT][UnitsPower.NANOWATT] = \
        Mul(Rat(Int(1), Pow(10, 33)), Sym("yottaw"))  # nopep8
    CONVERSIONS[UnitsPower.YOTTAWATT][UnitsPower.PETAWATT] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("yottaw"))  # nopep8
    CONVERSIONS[UnitsPower.YOTTAWATT][UnitsPower.PICOWATT] = \
        Mul(Rat(Int(1), Pow(10, 36)), Sym("yottaw"))  # nopep8
    CONVERSIONS[UnitsPower.YOTTAWATT][UnitsPower.TERAWATT] = \
        Mul(Rat(Int(1), Pow(10, 12)), Sym("yottaw"))  # nopep8
    CONVERSIONS[UnitsPower.YOTTAWATT][UnitsPower.WATT] = \
        Mul(Rat(Int(1), Pow(10, 24)), Sym("yottaw"))  # nopep8
    CONVERSIONS[UnitsPower.YOTTAWATT][UnitsPower.YOCTOWATT] = \
        Mul(Rat(Int(1), Pow(10, 48)), Sym("yottaw"))  # nopep8
    CONVERSIONS[UnitsPower.YOTTAWATT][UnitsPower.ZEPTOWATT] = \
        Mul(Rat(Int(1), Pow(10, 45)), Sym("yottaw"))  # nopep8
    CONVERSIONS[UnitsPower.YOTTAWATT][UnitsPower.ZETTAWATT] = \
        Mul(Rat(Int(1), Int(1000)), Sym("yottaw"))  # nopep8
    CONVERSIONS[UnitsPower.ZEPTOWATT][UnitsPower.ATTOWATT] = \
        Mul(Int(1000), Sym("zeptow"))  # nopep8
    CONVERSIONS[UnitsPower.ZEPTOWATT][UnitsPower.CENTIWATT] = \
        Mul(Pow(10, 19), Sym("zeptow"))  # nopep8
    CONVERSIONS[UnitsPower.ZEPTOWATT][UnitsPower.DECAWATT] = \
        Mul(Pow(10, 22), Sym("zeptow"))  # nopep8
    CONVERSIONS[UnitsPower.ZEPTOWATT][UnitsPower.DECIWATT] = \
        Mul(Pow(10, 20), Sym("zeptow"))  # nopep8
    CONVERSIONS[UnitsPower.ZEPTOWATT][UnitsPower.EXAWATT] = \
        Mul(Pow(10, 39), Sym("zeptow"))  # nopep8
    CONVERSIONS[UnitsPower.ZEPTOWATT][UnitsPower.FEMTOWATT] = \
        Mul(Pow(10, 6), Sym("zeptow"))  # nopep8
    CONVERSIONS[UnitsPower.ZEPTOWATT][UnitsPower.GIGAWATT] = \
        Mul(Pow(10, 30), Sym("zeptow"))  # nopep8
    CONVERSIONS[UnitsPower.ZEPTOWATT][UnitsPower.HECTOWATT] = \
        Mul(Pow(10, 23), Sym("zeptow"))  # nopep8
    CONVERSIONS[UnitsPower.ZEPTOWATT][UnitsPower.KILOWATT] = \
        Mul(Pow(10, 24), Sym("zeptow"))  # nopep8
    CONVERSIONS[UnitsPower.ZEPTOWATT][UnitsPower.MEGAWATT] = \
        Mul(Pow(10, 27), Sym("zeptow"))  # nopep8
    CONVERSIONS[UnitsPower.ZEPTOWATT][UnitsPower.MICROWATT] = \
        Mul(Pow(10, 15), Sym("zeptow"))  # nopep8
    CONVERSIONS[UnitsPower.ZEPTOWATT][UnitsPower.MILLIWATT] = \
        Mul(Pow(10, 18), Sym("zeptow"))  # nopep8
    CONVERSIONS[UnitsPower.ZEPTOWATT][UnitsPower.NANOWATT] = \
        Mul(Pow(10, 12), Sym("zeptow"))  # nopep8
    CONVERSIONS[UnitsPower.ZEPTOWATT][UnitsPower.PETAWATT] = \
        Mul(Pow(10, 36), Sym("zeptow"))  # nopep8
    CONVERSIONS[UnitsPower.ZEPTOWATT][UnitsPower.PICOWATT] = \
        Mul(Pow(10, 9), Sym("zeptow"))  # nopep8
    CONVERSIONS[UnitsPower.ZEPTOWATT][UnitsPower.TERAWATT] = \
        Mul(Pow(10, 33), Sym("zeptow"))  # nopep8
    CONVERSIONS[UnitsPower.ZEPTOWATT][UnitsPower.WATT] = \
        Mul(Pow(10, 21), Sym("zeptow"))  # nopep8
    CONVERSIONS[UnitsPower.ZEPTOWATT][UnitsPower.YOCTOWATT] = \
        Mul(Rat(Int(1), Int(1000)), Sym("zeptow"))  # nopep8
    CONVERSIONS[UnitsPower.ZEPTOWATT][UnitsPower.YOTTAWATT] = \
        Mul(Pow(10, 45), Sym("zeptow"))  # nopep8
    CONVERSIONS[UnitsPower.ZEPTOWATT][UnitsPower.ZETTAWATT] = \
        Mul(Pow(10, 42), Sym("zeptow"))  # nopep8
    CONVERSIONS[UnitsPower.ZETTAWATT][UnitsPower.ATTOWATT] = \
        Mul(Rat(Int(1), Pow(10, 39)), Sym("zettaw"))  # nopep8
    CONVERSIONS[UnitsPower.ZETTAWATT][UnitsPower.CENTIWATT] = \
        Mul(Rat(Int(1), Pow(10, 23)), Sym("zettaw"))  # nopep8
    CONVERSIONS[UnitsPower.ZETTAWATT][UnitsPower.DECAWATT] = \
        Mul(Rat(Int(1), Pow(10, 20)), Sym("zettaw"))  # nopep8
    CONVERSIONS[UnitsPower.ZETTAWATT][UnitsPower.DECIWATT] = \
        Mul(Rat(Int(1), Pow(10, 22)), Sym("zettaw"))  # nopep8
    CONVERSIONS[UnitsPower.ZETTAWATT][UnitsPower.EXAWATT] = \
        Mul(Rat(Int(1), Int(1000)), Sym("zettaw"))  # nopep8
    CONVERSIONS[UnitsPower.ZETTAWATT][UnitsPower.FEMTOWATT] = \
        Mul(Rat(Int(1), Pow(10, 36)), Sym("zettaw"))  # nopep8
    CONVERSIONS[UnitsPower.ZETTAWATT][UnitsPower.GIGAWATT] = \
        Mul(Rat(Int(1), Pow(10, 12)), Sym("zettaw"))  # nopep8
    CONVERSIONS[UnitsPower.ZETTAWATT][UnitsPower.HECTOWATT] = \
        Mul(Rat(Int(1), Pow(10, 19)), Sym("zettaw"))  # nopep8
    CONVERSIONS[UnitsPower.ZETTAWATT][UnitsPower.KILOWATT] = \
        Mul(Rat(Int(1), Pow(10, 18)), Sym("zettaw"))  # nopep8
    CONVERSIONS[UnitsPower.ZETTAWATT][UnitsPower.MEGAWATT] = \
        Mul(Rat(Int(1), Pow(10, 15)), Sym("zettaw"))  # nopep8
    CONVERSIONS[UnitsPower.ZETTAWATT][UnitsPower.MICROWATT] = \
        Mul(Rat(Int(1), Pow(10, 27)), Sym("zettaw"))  # nopep8
    CONVERSIONS[UnitsPower.ZETTAWATT][UnitsPower.MILLIWATT] = \
        Mul(Rat(Int(1), Pow(10, 24)), Sym("zettaw"))  # nopep8
    CONVERSIONS[UnitsPower.ZETTAWATT][UnitsPower.NANOWATT] = \
        Mul(Rat(Int(1), Pow(10, 30)), Sym("zettaw"))  # nopep8
    CONVERSIONS[UnitsPower.ZETTAWATT][UnitsPower.PETAWATT] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("zettaw"))  # nopep8
    CONVERSIONS[UnitsPower.ZETTAWATT][UnitsPower.PICOWATT] = \
        Mul(Rat(Int(1), Pow(10, 33)), Sym("zettaw"))  # nopep8
    CONVERSIONS[UnitsPower.ZETTAWATT][UnitsPower.TERAWATT] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("zettaw"))  # nopep8
    CONVERSIONS[UnitsPower.ZETTAWATT][UnitsPower.WATT] = \
        Mul(Rat(Int(1), Pow(10, 21)), Sym("zettaw"))  # nopep8
    CONVERSIONS[UnitsPower.ZETTAWATT][UnitsPower.YOCTOWATT] = \
        Mul(Rat(Int(1), Pow(10, 45)), Sym("zettaw"))  # nopep8
    CONVERSIONS[UnitsPower.ZETTAWATT][UnitsPower.YOTTAWATT] = \
        Mul(Int(1000), Sym("zettaw"))  # nopep8
    CONVERSIONS[UnitsPower.ZETTAWATT][UnitsPower.ZEPTOWATT] = \
        Mul(Rat(Int(1), Pow(10, 42)), Sym("zettaw"))  # nopep8

    SYMBOLS = dict()
    SYMBOLS["ATTOWATT"] = "aW"
    SYMBOLS["CENTIWATT"] = "cW"
    SYMBOLS["DECAWATT"] = "daW"
    SYMBOLS["DECIWATT"] = "dW"
    SYMBOLS["EXAWATT"] = "EW"
    SYMBOLS["FEMTOWATT"] = "fW"
    SYMBOLS["GIGAWATT"] = "GW"
    SYMBOLS["HECTOWATT"] = "hW"
    SYMBOLS["KILOWATT"] = "kW"
    SYMBOLS["MEGAWATT"] = "MW"
    SYMBOLS["MICROWATT"] = "µW"
    SYMBOLS["MILLIWATT"] = "mW"
    SYMBOLS["NANOWATT"] = "nW"
    SYMBOLS["PETAWATT"] = "PW"
    SYMBOLS["PICOWATT"] = "pW"
    SYMBOLS["TERAWATT"] = "TW"
    SYMBOLS["WATT"] = "W"
    SYMBOLS["YOCTOWATT"] = "yW"
    SYMBOLS["YOTTAWATT"] = "YW"
    SYMBOLS["ZEPTOWATT"] = "zW"
    SYMBOLS["ZETTAWATT"] = "ZW"

    def __init__(self, value=None, unit=None):
        _omero_model.Power.__init__(self)
        if isinstance(value, _omero_model.PowerI):
            # This is a copy-constructor call.
            target = str(unit)
            targetUnit = getattr(UnitsPower, str(target))
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
        return PowerI.SYMBOLS.get(str(unit))

    def setUnit(self, unit, current=None):
        self._unit = unit

    def setValue(self, value, current=None):
        self._value = value

    def __str__(self):
        return self._base_string(self.getValue(), self.getUnit())

_omero_model.PowerI = PowerI
