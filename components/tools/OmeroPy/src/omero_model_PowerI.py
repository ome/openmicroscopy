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

from omero.model.conversions import Add  # nopep8
from omero.model.conversions import Int  # nopep8
from omero.model.conversions import Mul  # nopep8
from omero.model.conversions import Pow  # nopep8
from omero.model.conversions import Rat  # nopep8
from omero.model.conversions import Sym  # nopep8


class PowerI(_omero_model.Power, UnitBase):

    CONVERSIONS = dict()
    CONVERSIONS["ATTOWATT:CENTIWATT"] = \
        Mul(Pow(10, 16), Sym("attow"))  # nopep8
    CONVERSIONS["ATTOWATT:DECAWATT"] = \
        Mul(Pow(10, 19), Sym("attow"))  # nopep8
    CONVERSIONS["ATTOWATT:DECIWATT"] = \
        Mul(Pow(10, 17), Sym("attow"))  # nopep8
    CONVERSIONS["ATTOWATT:EXAWATT"] = \
        Mul(Pow(10, 36), Sym("attow"))  # nopep8
    CONVERSIONS["ATTOWATT:FEMTOWATT"] = \
        Mul(Int(1000), Sym("attow"))  # nopep8
    CONVERSIONS["ATTOWATT:GIGAWATT"] = \
        Mul(Pow(10, 27), Sym("attow"))  # nopep8
    CONVERSIONS["ATTOWATT:HECTOWATT"] = \
        Mul(Pow(10, 20), Sym("attow"))  # nopep8
    CONVERSIONS["ATTOWATT:KILOWATT"] = \
        Mul(Pow(10, 21), Sym("attow"))  # nopep8
    CONVERSIONS["ATTOWATT:MEGAWATT"] = \
        Mul(Pow(10, 24), Sym("attow"))  # nopep8
    CONVERSIONS["ATTOWATT:MICROWATT"] = \
        Mul(Pow(10, 12), Sym("attow"))  # nopep8
    CONVERSIONS["ATTOWATT:MILLIWATT"] = \
        Mul(Pow(10, 15), Sym("attow"))  # nopep8
    CONVERSIONS["ATTOWATT:NANOWATT"] = \
        Mul(Pow(10, 9), Sym("attow"))  # nopep8
    CONVERSIONS["ATTOWATT:PETAWATT"] = \
        Mul(Pow(10, 33), Sym("attow"))  # nopep8
    CONVERSIONS["ATTOWATT:PICOWATT"] = \
        Mul(Pow(10, 6), Sym("attow"))  # nopep8
    CONVERSIONS["ATTOWATT:TERAWATT"] = \
        Mul(Pow(10, 30), Sym("attow"))  # nopep8
    CONVERSIONS["ATTOWATT:WATT"] = \
        Mul(Pow(10, 18), Sym("attow"))  # nopep8
    CONVERSIONS["ATTOWATT:YOCTOWATT"] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("attow"))  # nopep8
    CONVERSIONS["ATTOWATT:YOTTAWATT"] = \
        Mul(Pow(10, 42), Sym("attow"))  # nopep8
    CONVERSIONS["ATTOWATT:ZEPTOWATT"] = \
        Mul(Rat(Int(1), Int(1000)), Sym("attow"))  # nopep8
    CONVERSIONS["ATTOWATT:ZETTAWATT"] = \
        Mul(Pow(10, 39), Sym("attow"))  # nopep8
    CONVERSIONS["CENTIWATT:ATTOWATT"] = \
        Mul(Rat(Int(1), Pow(10, 16)), Sym("centiw"))  # nopep8
    CONVERSIONS["CENTIWATT:DECAWATT"] = \
        Mul(Int(1000), Sym("centiw"))  # nopep8
    CONVERSIONS["CENTIWATT:DECIWATT"] = \
        Mul(Int(10), Sym("centiw"))  # nopep8
    CONVERSIONS["CENTIWATT:EXAWATT"] = \
        Mul(Pow(10, 20), Sym("centiw"))  # nopep8
    CONVERSIONS["CENTIWATT:FEMTOWATT"] = \
        Mul(Rat(Int(1), Pow(10, 13)), Sym("centiw"))  # nopep8
    CONVERSIONS["CENTIWATT:GIGAWATT"] = \
        Mul(Pow(10, 11), Sym("centiw"))  # nopep8
    CONVERSIONS["CENTIWATT:HECTOWATT"] = \
        Mul(Pow(10, 4), Sym("centiw"))  # nopep8
    CONVERSIONS["CENTIWATT:KILOWATT"] = \
        Mul(Pow(10, 5), Sym("centiw"))  # nopep8
    CONVERSIONS["CENTIWATT:MEGAWATT"] = \
        Mul(Pow(10, 8), Sym("centiw"))  # nopep8
    CONVERSIONS["CENTIWATT:MICROWATT"] = \
        Mul(Rat(Int(1), Pow(10, 4)), Sym("centiw"))  # nopep8
    CONVERSIONS["CENTIWATT:MILLIWATT"] = \
        Mul(Rat(Int(1), Int(10)), Sym("centiw"))  # nopep8
    CONVERSIONS["CENTIWATT:NANOWATT"] = \
        Mul(Rat(Int(1), Pow(10, 7)), Sym("centiw"))  # nopep8
    CONVERSIONS["CENTIWATT:PETAWATT"] = \
        Mul(Pow(10, 17), Sym("centiw"))  # nopep8
    CONVERSIONS["CENTIWATT:PICOWATT"] = \
        Mul(Rat(Int(1), Pow(10, 10)), Sym("centiw"))  # nopep8
    CONVERSIONS["CENTIWATT:TERAWATT"] = \
        Mul(Pow(10, 14), Sym("centiw"))  # nopep8
    CONVERSIONS["CENTIWATT:WATT"] = \
        Mul(Int(100), Sym("centiw"))  # nopep8
    CONVERSIONS["CENTIWATT:YOCTOWATT"] = \
        Mul(Rat(Int(1), Pow(10, 22)), Sym("centiw"))  # nopep8
    CONVERSIONS["CENTIWATT:YOTTAWATT"] = \
        Mul(Pow(10, 26), Sym("centiw"))  # nopep8
    CONVERSIONS["CENTIWATT:ZEPTOWATT"] = \
        Mul(Rat(Int(1), Pow(10, 19)), Sym("centiw"))  # nopep8
    CONVERSIONS["CENTIWATT:ZETTAWATT"] = \
        Mul(Pow(10, 23), Sym("centiw"))  # nopep8
    CONVERSIONS["DECAWATT:ATTOWATT"] = \
        Mul(Rat(Int(1), Pow(10, 19)), Sym("decaw"))  # nopep8
    CONVERSIONS["DECAWATT:CENTIWATT"] = \
        Mul(Rat(Int(1), Int(1000)), Sym("decaw"))  # nopep8
    CONVERSIONS["DECAWATT:DECIWATT"] = \
        Mul(Rat(Int(1), Int(100)), Sym("decaw"))  # nopep8
    CONVERSIONS["DECAWATT:EXAWATT"] = \
        Mul(Pow(10, 17), Sym("decaw"))  # nopep8
    CONVERSIONS["DECAWATT:FEMTOWATT"] = \
        Mul(Rat(Int(1), Pow(10, 16)), Sym("decaw"))  # nopep8
    CONVERSIONS["DECAWATT:GIGAWATT"] = \
        Mul(Pow(10, 8), Sym("decaw"))  # nopep8
    CONVERSIONS["DECAWATT:HECTOWATT"] = \
        Mul(Int(10), Sym("decaw"))  # nopep8
    CONVERSIONS["DECAWATT:KILOWATT"] = \
        Mul(Int(100), Sym("decaw"))  # nopep8
    CONVERSIONS["DECAWATT:MEGAWATT"] = \
        Mul(Pow(10, 5), Sym("decaw"))  # nopep8
    CONVERSIONS["DECAWATT:MICROWATT"] = \
        Mul(Rat(Int(1), Pow(10, 7)), Sym("decaw"))  # nopep8
    CONVERSIONS["DECAWATT:MILLIWATT"] = \
        Mul(Rat(Int(1), Pow(10, 4)), Sym("decaw"))  # nopep8
    CONVERSIONS["DECAWATT:NANOWATT"] = \
        Mul(Rat(Int(1), Pow(10, 10)), Sym("decaw"))  # nopep8
    CONVERSIONS["DECAWATT:PETAWATT"] = \
        Mul(Pow(10, 14), Sym("decaw"))  # nopep8
    CONVERSIONS["DECAWATT:PICOWATT"] = \
        Mul(Rat(Int(1), Pow(10, 13)), Sym("decaw"))  # nopep8
    CONVERSIONS["DECAWATT:TERAWATT"] = \
        Mul(Pow(10, 11), Sym("decaw"))  # nopep8
    CONVERSIONS["DECAWATT:WATT"] = \
        Mul(Rat(Int(1), Int(10)), Sym("decaw"))  # nopep8
    CONVERSIONS["DECAWATT:YOCTOWATT"] = \
        Mul(Rat(Int(1), Pow(10, 25)), Sym("decaw"))  # nopep8
    CONVERSIONS["DECAWATT:YOTTAWATT"] = \
        Mul(Pow(10, 23), Sym("decaw"))  # nopep8
    CONVERSIONS["DECAWATT:ZEPTOWATT"] = \
        Mul(Rat(Int(1), Pow(10, 22)), Sym("decaw"))  # nopep8
    CONVERSIONS["DECAWATT:ZETTAWATT"] = \
        Mul(Pow(10, 20), Sym("decaw"))  # nopep8
    CONVERSIONS["DECIWATT:ATTOWATT"] = \
        Mul(Rat(Int(1), Pow(10, 17)), Sym("deciw"))  # nopep8
    CONVERSIONS["DECIWATT:CENTIWATT"] = \
        Mul(Rat(Int(1), Int(10)), Sym("deciw"))  # nopep8
    CONVERSIONS["DECIWATT:DECAWATT"] = \
        Mul(Int(100), Sym("deciw"))  # nopep8
    CONVERSIONS["DECIWATT:EXAWATT"] = \
        Mul(Pow(10, 19), Sym("deciw"))  # nopep8
    CONVERSIONS["DECIWATT:FEMTOWATT"] = \
        Mul(Rat(Int(1), Pow(10, 14)), Sym("deciw"))  # nopep8
    CONVERSIONS["DECIWATT:GIGAWATT"] = \
        Mul(Pow(10, 10), Sym("deciw"))  # nopep8
    CONVERSIONS["DECIWATT:HECTOWATT"] = \
        Mul(Int(1000), Sym("deciw"))  # nopep8
    CONVERSIONS["DECIWATT:KILOWATT"] = \
        Mul(Pow(10, 4), Sym("deciw"))  # nopep8
    CONVERSIONS["DECIWATT:MEGAWATT"] = \
        Mul(Pow(10, 7), Sym("deciw"))  # nopep8
    CONVERSIONS["DECIWATT:MICROWATT"] = \
        Mul(Rat(Int(1), Pow(10, 5)), Sym("deciw"))  # nopep8
    CONVERSIONS["DECIWATT:MILLIWATT"] = \
        Mul(Rat(Int(1), Int(100)), Sym("deciw"))  # nopep8
    CONVERSIONS["DECIWATT:NANOWATT"] = \
        Mul(Rat(Int(1), Pow(10, 8)), Sym("deciw"))  # nopep8
    CONVERSIONS["DECIWATT:PETAWATT"] = \
        Mul(Pow(10, 16), Sym("deciw"))  # nopep8
    CONVERSIONS["DECIWATT:PICOWATT"] = \
        Mul(Rat(Int(1), Pow(10, 11)), Sym("deciw"))  # nopep8
    CONVERSIONS["DECIWATT:TERAWATT"] = \
        Mul(Pow(10, 13), Sym("deciw"))  # nopep8
    CONVERSIONS["DECIWATT:WATT"] = \
        Mul(Int(10), Sym("deciw"))  # nopep8
    CONVERSIONS["DECIWATT:YOCTOWATT"] = \
        Mul(Rat(Int(1), Pow(10, 23)), Sym("deciw"))  # nopep8
    CONVERSIONS["DECIWATT:YOTTAWATT"] = \
        Mul(Pow(10, 25), Sym("deciw"))  # nopep8
    CONVERSIONS["DECIWATT:ZEPTOWATT"] = \
        Mul(Rat(Int(1), Pow(10, 20)), Sym("deciw"))  # nopep8
    CONVERSIONS["DECIWATT:ZETTAWATT"] = \
        Mul(Pow(10, 22), Sym("deciw"))  # nopep8
    CONVERSIONS["EXAWATT:ATTOWATT"] = \
        Mul(Rat(Int(1), Pow(10, 36)), Sym("exaw"))  # nopep8
    CONVERSIONS["EXAWATT:CENTIWATT"] = \
        Mul(Rat(Int(1), Pow(10, 20)), Sym("exaw"))  # nopep8
    CONVERSIONS["EXAWATT:DECAWATT"] = \
        Mul(Rat(Int(1), Pow(10, 17)), Sym("exaw"))  # nopep8
    CONVERSIONS["EXAWATT:DECIWATT"] = \
        Mul(Rat(Int(1), Pow(10, 19)), Sym("exaw"))  # nopep8
    CONVERSIONS["EXAWATT:FEMTOWATT"] = \
        Mul(Rat(Int(1), Pow(10, 33)), Sym("exaw"))  # nopep8
    CONVERSIONS["EXAWATT:GIGAWATT"] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("exaw"))  # nopep8
    CONVERSIONS["EXAWATT:HECTOWATT"] = \
        Mul(Rat(Int(1), Pow(10, 16)), Sym("exaw"))  # nopep8
    CONVERSIONS["EXAWATT:KILOWATT"] = \
        Mul(Rat(Int(1), Pow(10, 15)), Sym("exaw"))  # nopep8
    CONVERSIONS["EXAWATT:MEGAWATT"] = \
        Mul(Rat(Int(1), Pow(10, 12)), Sym("exaw"))  # nopep8
    CONVERSIONS["EXAWATT:MICROWATT"] = \
        Mul(Rat(Int(1), Pow(10, 24)), Sym("exaw"))  # nopep8
    CONVERSIONS["EXAWATT:MILLIWATT"] = \
        Mul(Rat(Int(1), Pow(10, 21)), Sym("exaw"))  # nopep8
    CONVERSIONS["EXAWATT:NANOWATT"] = \
        Mul(Rat(Int(1), Pow(10, 27)), Sym("exaw"))  # nopep8
    CONVERSIONS["EXAWATT:PETAWATT"] = \
        Mul(Rat(Int(1), Int(1000)), Sym("exaw"))  # nopep8
    CONVERSIONS["EXAWATT:PICOWATT"] = \
        Mul(Rat(Int(1), Pow(10, 30)), Sym("exaw"))  # nopep8
    CONVERSIONS["EXAWATT:TERAWATT"] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("exaw"))  # nopep8
    CONVERSIONS["EXAWATT:WATT"] = \
        Mul(Rat(Int(1), Pow(10, 18)), Sym("exaw"))  # nopep8
    CONVERSIONS["EXAWATT:YOCTOWATT"] = \
        Mul(Rat(Int(1), Pow(10, 42)), Sym("exaw"))  # nopep8
    CONVERSIONS["EXAWATT:YOTTAWATT"] = \
        Mul(Pow(10, 6), Sym("exaw"))  # nopep8
    CONVERSIONS["EXAWATT:ZEPTOWATT"] = \
        Mul(Rat(Int(1), Pow(10, 39)), Sym("exaw"))  # nopep8
    CONVERSIONS["EXAWATT:ZETTAWATT"] = \
        Mul(Int(1000), Sym("exaw"))  # nopep8
    CONVERSIONS["FEMTOWATT:ATTOWATT"] = \
        Mul(Rat(Int(1), Int(1000)), Sym("femtow"))  # nopep8
    CONVERSIONS["FEMTOWATT:CENTIWATT"] = \
        Mul(Pow(10, 13), Sym("femtow"))  # nopep8
    CONVERSIONS["FEMTOWATT:DECAWATT"] = \
        Mul(Pow(10, 16), Sym("femtow"))  # nopep8
    CONVERSIONS["FEMTOWATT:DECIWATT"] = \
        Mul(Pow(10, 14), Sym("femtow"))  # nopep8
    CONVERSIONS["FEMTOWATT:EXAWATT"] = \
        Mul(Pow(10, 33), Sym("femtow"))  # nopep8
    CONVERSIONS["FEMTOWATT:GIGAWATT"] = \
        Mul(Pow(10, 24), Sym("femtow"))  # nopep8
    CONVERSIONS["FEMTOWATT:HECTOWATT"] = \
        Mul(Pow(10, 17), Sym("femtow"))  # nopep8
    CONVERSIONS["FEMTOWATT:KILOWATT"] = \
        Mul(Pow(10, 18), Sym("femtow"))  # nopep8
    CONVERSIONS["FEMTOWATT:MEGAWATT"] = \
        Mul(Pow(10, 21), Sym("femtow"))  # nopep8
    CONVERSIONS["FEMTOWATT:MICROWATT"] = \
        Mul(Pow(10, 9), Sym("femtow"))  # nopep8
    CONVERSIONS["FEMTOWATT:MILLIWATT"] = \
        Mul(Pow(10, 12), Sym("femtow"))  # nopep8
    CONVERSIONS["FEMTOWATT:NANOWATT"] = \
        Mul(Pow(10, 6), Sym("femtow"))  # nopep8
    CONVERSIONS["FEMTOWATT:PETAWATT"] = \
        Mul(Pow(10, 30), Sym("femtow"))  # nopep8
    CONVERSIONS["FEMTOWATT:PICOWATT"] = \
        Mul(Int(1000), Sym("femtow"))  # nopep8
    CONVERSIONS["FEMTOWATT:TERAWATT"] = \
        Mul(Pow(10, 27), Sym("femtow"))  # nopep8
    CONVERSIONS["FEMTOWATT:WATT"] = \
        Mul(Pow(10, 15), Sym("femtow"))  # nopep8
    CONVERSIONS["FEMTOWATT:YOCTOWATT"] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("femtow"))  # nopep8
    CONVERSIONS["FEMTOWATT:YOTTAWATT"] = \
        Mul(Pow(10, 39), Sym("femtow"))  # nopep8
    CONVERSIONS["FEMTOWATT:ZEPTOWATT"] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("femtow"))  # nopep8
    CONVERSIONS["FEMTOWATT:ZETTAWATT"] = \
        Mul(Pow(10, 36), Sym("femtow"))  # nopep8
    CONVERSIONS["GIGAWATT:ATTOWATT"] = \
        Mul(Rat(Int(1), Pow(10, 27)), Sym("gigaw"))  # nopep8
    CONVERSIONS["GIGAWATT:CENTIWATT"] = \
        Mul(Rat(Int(1), Pow(10, 11)), Sym("gigaw"))  # nopep8
    CONVERSIONS["GIGAWATT:DECAWATT"] = \
        Mul(Rat(Int(1), Pow(10, 8)), Sym("gigaw"))  # nopep8
    CONVERSIONS["GIGAWATT:DECIWATT"] = \
        Mul(Rat(Int(1), Pow(10, 10)), Sym("gigaw"))  # nopep8
    CONVERSIONS["GIGAWATT:EXAWATT"] = \
        Mul(Pow(10, 9), Sym("gigaw"))  # nopep8
    CONVERSIONS["GIGAWATT:FEMTOWATT"] = \
        Mul(Rat(Int(1), Pow(10, 24)), Sym("gigaw"))  # nopep8
    CONVERSIONS["GIGAWATT:HECTOWATT"] = \
        Mul(Rat(Int(1), Pow(10, 7)), Sym("gigaw"))  # nopep8
    CONVERSIONS["GIGAWATT:KILOWATT"] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("gigaw"))  # nopep8
    CONVERSIONS["GIGAWATT:MEGAWATT"] = \
        Mul(Rat(Int(1), Int(1000)), Sym("gigaw"))  # nopep8
    CONVERSIONS["GIGAWATT:MICROWATT"] = \
        Mul(Rat(Int(1), Pow(10, 15)), Sym("gigaw"))  # nopep8
    CONVERSIONS["GIGAWATT:MILLIWATT"] = \
        Mul(Rat(Int(1), Pow(10, 12)), Sym("gigaw"))  # nopep8
    CONVERSIONS["GIGAWATT:NANOWATT"] = \
        Mul(Rat(Int(1), Pow(10, 18)), Sym("gigaw"))  # nopep8
    CONVERSIONS["GIGAWATT:PETAWATT"] = \
        Mul(Pow(10, 6), Sym("gigaw"))  # nopep8
    CONVERSIONS["GIGAWATT:PICOWATT"] = \
        Mul(Rat(Int(1), Pow(10, 21)), Sym("gigaw"))  # nopep8
    CONVERSIONS["GIGAWATT:TERAWATT"] = \
        Mul(Int(1000), Sym("gigaw"))  # nopep8
    CONVERSIONS["GIGAWATT:WATT"] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("gigaw"))  # nopep8
    CONVERSIONS["GIGAWATT:YOCTOWATT"] = \
        Mul(Rat(Int(1), Pow(10, 33)), Sym("gigaw"))  # nopep8
    CONVERSIONS["GIGAWATT:YOTTAWATT"] = \
        Mul(Pow(10, 15), Sym("gigaw"))  # nopep8
    CONVERSIONS["GIGAWATT:ZEPTOWATT"] = \
        Mul(Rat(Int(1), Pow(10, 30)), Sym("gigaw"))  # nopep8
    CONVERSIONS["GIGAWATT:ZETTAWATT"] = \
        Mul(Pow(10, 12), Sym("gigaw"))  # nopep8
    CONVERSIONS["HECTOWATT:ATTOWATT"] = \
        Mul(Rat(Int(1), Pow(10, 20)), Sym("hectow"))  # nopep8
    CONVERSIONS["HECTOWATT:CENTIWATT"] = \
        Mul(Rat(Int(1), Pow(10, 4)), Sym("hectow"))  # nopep8
    CONVERSIONS["HECTOWATT:DECAWATT"] = \
        Mul(Rat(Int(1), Int(10)), Sym("hectow"))  # nopep8
    CONVERSIONS["HECTOWATT:DECIWATT"] = \
        Mul(Rat(Int(1), Int(1000)), Sym("hectow"))  # nopep8
    CONVERSIONS["HECTOWATT:EXAWATT"] = \
        Mul(Pow(10, 16), Sym("hectow"))  # nopep8
    CONVERSIONS["HECTOWATT:FEMTOWATT"] = \
        Mul(Rat(Int(1), Pow(10, 17)), Sym("hectow"))  # nopep8
    CONVERSIONS["HECTOWATT:GIGAWATT"] = \
        Mul(Pow(10, 7), Sym("hectow"))  # nopep8
    CONVERSIONS["HECTOWATT:KILOWATT"] = \
        Mul(Int(10), Sym("hectow"))  # nopep8
    CONVERSIONS["HECTOWATT:MEGAWATT"] = \
        Mul(Pow(10, 4), Sym("hectow"))  # nopep8
    CONVERSIONS["HECTOWATT:MICROWATT"] = \
        Mul(Rat(Int(1), Pow(10, 8)), Sym("hectow"))  # nopep8
    CONVERSIONS["HECTOWATT:MILLIWATT"] = \
        Mul(Rat(Int(1), Pow(10, 5)), Sym("hectow"))  # nopep8
    CONVERSIONS["HECTOWATT:NANOWATT"] = \
        Mul(Rat(Int(1), Pow(10, 11)), Sym("hectow"))  # nopep8
    CONVERSIONS["HECTOWATT:PETAWATT"] = \
        Mul(Pow(10, 13), Sym("hectow"))  # nopep8
    CONVERSIONS["HECTOWATT:PICOWATT"] = \
        Mul(Rat(Int(1), Pow(10, 14)), Sym("hectow"))  # nopep8
    CONVERSIONS["HECTOWATT:TERAWATT"] = \
        Mul(Pow(10, 10), Sym("hectow"))  # nopep8
    CONVERSIONS["HECTOWATT:WATT"] = \
        Mul(Rat(Int(1), Int(100)), Sym("hectow"))  # nopep8
    CONVERSIONS["HECTOWATT:YOCTOWATT"] = \
        Mul(Rat(Int(1), Pow(10, 26)), Sym("hectow"))  # nopep8
    CONVERSIONS["HECTOWATT:YOTTAWATT"] = \
        Mul(Pow(10, 22), Sym("hectow"))  # nopep8
    CONVERSIONS["HECTOWATT:ZEPTOWATT"] = \
        Mul(Rat(Int(1), Pow(10, 23)), Sym("hectow"))  # nopep8
    CONVERSIONS["HECTOWATT:ZETTAWATT"] = \
        Mul(Pow(10, 19), Sym("hectow"))  # nopep8
    CONVERSIONS["KILOWATT:ATTOWATT"] = \
        Mul(Rat(Int(1), Pow(10, 21)), Sym("kilow"))  # nopep8
    CONVERSIONS["KILOWATT:CENTIWATT"] = \
        Mul(Rat(Int(1), Pow(10, 5)), Sym("kilow"))  # nopep8
    CONVERSIONS["KILOWATT:DECAWATT"] = \
        Mul(Rat(Int(1), Int(100)), Sym("kilow"))  # nopep8
    CONVERSIONS["KILOWATT:DECIWATT"] = \
        Mul(Rat(Int(1), Pow(10, 4)), Sym("kilow"))  # nopep8
    CONVERSIONS["KILOWATT:EXAWATT"] = \
        Mul(Pow(10, 15), Sym("kilow"))  # nopep8
    CONVERSIONS["KILOWATT:FEMTOWATT"] = \
        Mul(Rat(Int(1), Pow(10, 18)), Sym("kilow"))  # nopep8
    CONVERSIONS["KILOWATT:GIGAWATT"] = \
        Mul(Pow(10, 6), Sym("kilow"))  # nopep8
    CONVERSIONS["KILOWATT:HECTOWATT"] = \
        Mul(Rat(Int(1), Int(10)), Sym("kilow"))  # nopep8
    CONVERSIONS["KILOWATT:MEGAWATT"] = \
        Mul(Int(1000), Sym("kilow"))  # nopep8
    CONVERSIONS["KILOWATT:MICROWATT"] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("kilow"))  # nopep8
    CONVERSIONS["KILOWATT:MILLIWATT"] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("kilow"))  # nopep8
    CONVERSIONS["KILOWATT:NANOWATT"] = \
        Mul(Rat(Int(1), Pow(10, 12)), Sym("kilow"))  # nopep8
    CONVERSIONS["KILOWATT:PETAWATT"] = \
        Mul(Pow(10, 12), Sym("kilow"))  # nopep8
    CONVERSIONS["KILOWATT:PICOWATT"] = \
        Mul(Rat(Int(1), Pow(10, 15)), Sym("kilow"))  # nopep8
    CONVERSIONS["KILOWATT:TERAWATT"] = \
        Mul(Pow(10, 9), Sym("kilow"))  # nopep8
    CONVERSIONS["KILOWATT:WATT"] = \
        Mul(Rat(Int(1), Int(1000)), Sym("kilow"))  # nopep8
    CONVERSIONS["KILOWATT:YOCTOWATT"] = \
        Mul(Rat(Int(1), Pow(10, 27)), Sym("kilow"))  # nopep8
    CONVERSIONS["KILOWATT:YOTTAWATT"] = \
        Mul(Pow(10, 21), Sym("kilow"))  # nopep8
    CONVERSIONS["KILOWATT:ZEPTOWATT"] = \
        Mul(Rat(Int(1), Pow(10, 24)), Sym("kilow"))  # nopep8
    CONVERSIONS["KILOWATT:ZETTAWATT"] = \
        Mul(Pow(10, 18), Sym("kilow"))  # nopep8
    CONVERSIONS["MEGAWATT:ATTOWATT"] = \
        Mul(Rat(Int(1), Pow(10, 24)), Sym("megaw"))  # nopep8
    CONVERSIONS["MEGAWATT:CENTIWATT"] = \
        Mul(Rat(Int(1), Pow(10, 8)), Sym("megaw"))  # nopep8
    CONVERSIONS["MEGAWATT:DECAWATT"] = \
        Mul(Rat(Int(1), Pow(10, 5)), Sym("megaw"))  # nopep8
    CONVERSIONS["MEGAWATT:DECIWATT"] = \
        Mul(Rat(Int(1), Pow(10, 7)), Sym("megaw"))  # nopep8
    CONVERSIONS["MEGAWATT:EXAWATT"] = \
        Mul(Pow(10, 12), Sym("megaw"))  # nopep8
    CONVERSIONS["MEGAWATT:FEMTOWATT"] = \
        Mul(Rat(Int(1), Pow(10, 21)), Sym("megaw"))  # nopep8
    CONVERSIONS["MEGAWATT:GIGAWATT"] = \
        Mul(Int(1000), Sym("megaw"))  # nopep8
    CONVERSIONS["MEGAWATT:HECTOWATT"] = \
        Mul(Rat(Int(1), Pow(10, 4)), Sym("megaw"))  # nopep8
    CONVERSIONS["MEGAWATT:KILOWATT"] = \
        Mul(Rat(Int(1), Int(1000)), Sym("megaw"))  # nopep8
    CONVERSIONS["MEGAWATT:MICROWATT"] = \
        Mul(Rat(Int(1), Pow(10, 12)), Sym("megaw"))  # nopep8
    CONVERSIONS["MEGAWATT:MILLIWATT"] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("megaw"))  # nopep8
    CONVERSIONS["MEGAWATT:NANOWATT"] = \
        Mul(Rat(Int(1), Pow(10, 15)), Sym("megaw"))  # nopep8
    CONVERSIONS["MEGAWATT:PETAWATT"] = \
        Mul(Pow(10, 9), Sym("megaw"))  # nopep8
    CONVERSIONS["MEGAWATT:PICOWATT"] = \
        Mul(Rat(Int(1), Pow(10, 18)), Sym("megaw"))  # nopep8
    CONVERSIONS["MEGAWATT:TERAWATT"] = \
        Mul(Pow(10, 6), Sym("megaw"))  # nopep8
    CONVERSIONS["MEGAWATT:WATT"] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("megaw"))  # nopep8
    CONVERSIONS["MEGAWATT:YOCTOWATT"] = \
        Mul(Rat(Int(1), Pow(10, 30)), Sym("megaw"))  # nopep8
    CONVERSIONS["MEGAWATT:YOTTAWATT"] = \
        Mul(Pow(10, 18), Sym("megaw"))  # nopep8
    CONVERSIONS["MEGAWATT:ZEPTOWATT"] = \
        Mul(Rat(Int(1), Pow(10, 27)), Sym("megaw"))  # nopep8
    CONVERSIONS["MEGAWATT:ZETTAWATT"] = \
        Mul(Pow(10, 15), Sym("megaw"))  # nopep8
    CONVERSIONS["MICROWATT:ATTOWATT"] = \
        Mul(Rat(Int(1), Pow(10, 12)), Sym("microw"))  # nopep8
    CONVERSIONS["MICROWATT:CENTIWATT"] = \
        Mul(Pow(10, 4), Sym("microw"))  # nopep8
    CONVERSIONS["MICROWATT:DECAWATT"] = \
        Mul(Pow(10, 7), Sym("microw"))  # nopep8
    CONVERSIONS["MICROWATT:DECIWATT"] = \
        Mul(Pow(10, 5), Sym("microw"))  # nopep8
    CONVERSIONS["MICROWATT:EXAWATT"] = \
        Mul(Pow(10, 24), Sym("microw"))  # nopep8
    CONVERSIONS["MICROWATT:FEMTOWATT"] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("microw"))  # nopep8
    CONVERSIONS["MICROWATT:GIGAWATT"] = \
        Mul(Pow(10, 15), Sym("microw"))  # nopep8
    CONVERSIONS["MICROWATT:HECTOWATT"] = \
        Mul(Pow(10, 8), Sym("microw"))  # nopep8
    CONVERSIONS["MICROWATT:KILOWATT"] = \
        Mul(Pow(10, 9), Sym("microw"))  # nopep8
    CONVERSIONS["MICROWATT:MEGAWATT"] = \
        Mul(Pow(10, 12), Sym("microw"))  # nopep8
    CONVERSIONS["MICROWATT:MILLIWATT"] = \
        Mul(Int(1000), Sym("microw"))  # nopep8
    CONVERSIONS["MICROWATT:NANOWATT"] = \
        Mul(Rat(Int(1), Int(1000)), Sym("microw"))  # nopep8
    CONVERSIONS["MICROWATT:PETAWATT"] = \
        Mul(Pow(10, 21), Sym("microw"))  # nopep8
    CONVERSIONS["MICROWATT:PICOWATT"] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("microw"))  # nopep8
    CONVERSIONS["MICROWATT:TERAWATT"] = \
        Mul(Pow(10, 18), Sym("microw"))  # nopep8
    CONVERSIONS["MICROWATT:WATT"] = \
        Mul(Pow(10, 6), Sym("microw"))  # nopep8
    CONVERSIONS["MICROWATT:YOCTOWATT"] = \
        Mul(Rat(Int(1), Pow(10, 18)), Sym("microw"))  # nopep8
    CONVERSIONS["MICROWATT:YOTTAWATT"] = \
        Mul(Pow(10, 30), Sym("microw"))  # nopep8
    CONVERSIONS["MICROWATT:ZEPTOWATT"] = \
        Mul(Rat(Int(1), Pow(10, 15)), Sym("microw"))  # nopep8
    CONVERSIONS["MICROWATT:ZETTAWATT"] = \
        Mul(Pow(10, 27), Sym("microw"))  # nopep8
    CONVERSIONS["MILLIWATT:ATTOWATT"] = \
        Mul(Rat(Int(1), Pow(10, 15)), Sym("milliw"))  # nopep8
    CONVERSIONS["MILLIWATT:CENTIWATT"] = \
        Mul(Int(10), Sym("milliw"))  # nopep8
    CONVERSIONS["MILLIWATT:DECAWATT"] = \
        Mul(Pow(10, 4), Sym("milliw"))  # nopep8
    CONVERSIONS["MILLIWATT:DECIWATT"] = \
        Mul(Int(100), Sym("milliw"))  # nopep8
    CONVERSIONS["MILLIWATT:EXAWATT"] = \
        Mul(Pow(10, 21), Sym("milliw"))  # nopep8
    CONVERSIONS["MILLIWATT:FEMTOWATT"] = \
        Mul(Rat(Int(1), Pow(10, 12)), Sym("milliw"))  # nopep8
    CONVERSIONS["MILLIWATT:GIGAWATT"] = \
        Mul(Pow(10, 12), Sym("milliw"))  # nopep8
    CONVERSIONS["MILLIWATT:HECTOWATT"] = \
        Mul(Pow(10, 5), Sym("milliw"))  # nopep8
    CONVERSIONS["MILLIWATT:KILOWATT"] = \
        Mul(Pow(10, 6), Sym("milliw"))  # nopep8
    CONVERSIONS["MILLIWATT:MEGAWATT"] = \
        Mul(Pow(10, 9), Sym("milliw"))  # nopep8
    CONVERSIONS["MILLIWATT:MICROWATT"] = \
        Mul(Rat(Int(1), Int(1000)), Sym("milliw"))  # nopep8
    CONVERSIONS["MILLIWATT:NANOWATT"] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("milliw"))  # nopep8
    CONVERSIONS["MILLIWATT:PETAWATT"] = \
        Mul(Pow(10, 18), Sym("milliw"))  # nopep8
    CONVERSIONS["MILLIWATT:PICOWATT"] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("milliw"))  # nopep8
    CONVERSIONS["MILLIWATT:TERAWATT"] = \
        Mul(Pow(10, 15), Sym("milliw"))  # nopep8
    CONVERSIONS["MILLIWATT:WATT"] = \
        Mul(Int(1000), Sym("milliw"))  # nopep8
    CONVERSIONS["MILLIWATT:YOCTOWATT"] = \
        Mul(Rat(Int(1), Pow(10, 21)), Sym("milliw"))  # nopep8
    CONVERSIONS["MILLIWATT:YOTTAWATT"] = \
        Mul(Pow(10, 27), Sym("milliw"))  # nopep8
    CONVERSIONS["MILLIWATT:ZEPTOWATT"] = \
        Mul(Rat(Int(1), Pow(10, 18)), Sym("milliw"))  # nopep8
    CONVERSIONS["MILLIWATT:ZETTAWATT"] = \
        Mul(Pow(10, 24), Sym("milliw"))  # nopep8
    CONVERSIONS["NANOWATT:ATTOWATT"] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("nanow"))  # nopep8
    CONVERSIONS["NANOWATT:CENTIWATT"] = \
        Mul(Pow(10, 7), Sym("nanow"))  # nopep8
    CONVERSIONS["NANOWATT:DECAWATT"] = \
        Mul(Pow(10, 10), Sym("nanow"))  # nopep8
    CONVERSIONS["NANOWATT:DECIWATT"] = \
        Mul(Pow(10, 8), Sym("nanow"))  # nopep8
    CONVERSIONS["NANOWATT:EXAWATT"] = \
        Mul(Pow(10, 27), Sym("nanow"))  # nopep8
    CONVERSIONS["NANOWATT:FEMTOWATT"] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("nanow"))  # nopep8
    CONVERSIONS["NANOWATT:GIGAWATT"] = \
        Mul(Pow(10, 18), Sym("nanow"))  # nopep8
    CONVERSIONS["NANOWATT:HECTOWATT"] = \
        Mul(Pow(10, 11), Sym("nanow"))  # nopep8
    CONVERSIONS["NANOWATT:KILOWATT"] = \
        Mul(Pow(10, 12), Sym("nanow"))  # nopep8
    CONVERSIONS["NANOWATT:MEGAWATT"] = \
        Mul(Pow(10, 15), Sym("nanow"))  # nopep8
    CONVERSIONS["NANOWATT:MICROWATT"] = \
        Mul(Int(1000), Sym("nanow"))  # nopep8
    CONVERSIONS["NANOWATT:MILLIWATT"] = \
        Mul(Pow(10, 6), Sym("nanow"))  # nopep8
    CONVERSIONS["NANOWATT:PETAWATT"] = \
        Mul(Pow(10, 24), Sym("nanow"))  # nopep8
    CONVERSIONS["NANOWATT:PICOWATT"] = \
        Mul(Rat(Int(1), Int(1000)), Sym("nanow"))  # nopep8
    CONVERSIONS["NANOWATT:TERAWATT"] = \
        Mul(Pow(10, 21), Sym("nanow"))  # nopep8
    CONVERSIONS["NANOWATT:WATT"] = \
        Mul(Pow(10, 9), Sym("nanow"))  # nopep8
    CONVERSIONS["NANOWATT:YOCTOWATT"] = \
        Mul(Rat(Int(1), Pow(10, 15)), Sym("nanow"))  # nopep8
    CONVERSIONS["NANOWATT:YOTTAWATT"] = \
        Mul(Pow(10, 33), Sym("nanow"))  # nopep8
    CONVERSIONS["NANOWATT:ZEPTOWATT"] = \
        Mul(Rat(Int(1), Pow(10, 12)), Sym("nanow"))  # nopep8
    CONVERSIONS["NANOWATT:ZETTAWATT"] = \
        Mul(Pow(10, 30), Sym("nanow"))  # nopep8
    CONVERSIONS["PETAWATT:ATTOWATT"] = \
        Mul(Rat(Int(1), Pow(10, 33)), Sym("petaw"))  # nopep8
    CONVERSIONS["PETAWATT:CENTIWATT"] = \
        Mul(Rat(Int(1), Pow(10, 17)), Sym("petaw"))  # nopep8
    CONVERSIONS["PETAWATT:DECAWATT"] = \
        Mul(Rat(Int(1), Pow(10, 14)), Sym("petaw"))  # nopep8
    CONVERSIONS["PETAWATT:DECIWATT"] = \
        Mul(Rat(Int(1), Pow(10, 16)), Sym("petaw"))  # nopep8
    CONVERSIONS["PETAWATT:EXAWATT"] = \
        Mul(Int(1000), Sym("petaw"))  # nopep8
    CONVERSIONS["PETAWATT:FEMTOWATT"] = \
        Mul(Rat(Int(1), Pow(10, 30)), Sym("petaw"))  # nopep8
    CONVERSIONS["PETAWATT:GIGAWATT"] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("petaw"))  # nopep8
    CONVERSIONS["PETAWATT:HECTOWATT"] = \
        Mul(Rat(Int(1), Pow(10, 13)), Sym("petaw"))  # nopep8
    CONVERSIONS["PETAWATT:KILOWATT"] = \
        Mul(Rat(Int(1), Pow(10, 12)), Sym("petaw"))  # nopep8
    CONVERSIONS["PETAWATT:MEGAWATT"] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("petaw"))  # nopep8
    CONVERSIONS["PETAWATT:MICROWATT"] = \
        Mul(Rat(Int(1), Pow(10, 21)), Sym("petaw"))  # nopep8
    CONVERSIONS["PETAWATT:MILLIWATT"] = \
        Mul(Rat(Int(1), Pow(10, 18)), Sym("petaw"))  # nopep8
    CONVERSIONS["PETAWATT:NANOWATT"] = \
        Mul(Rat(Int(1), Pow(10, 24)), Sym("petaw"))  # nopep8
    CONVERSIONS["PETAWATT:PICOWATT"] = \
        Mul(Rat(Int(1), Pow(10, 27)), Sym("petaw"))  # nopep8
    CONVERSIONS["PETAWATT:TERAWATT"] = \
        Mul(Rat(Int(1), Int(1000)), Sym("petaw"))  # nopep8
    CONVERSIONS["PETAWATT:WATT"] = \
        Mul(Rat(Int(1), Pow(10, 15)), Sym("petaw"))  # nopep8
    CONVERSIONS["PETAWATT:YOCTOWATT"] = \
        Mul(Rat(Int(1), Pow(10, 39)), Sym("petaw"))  # nopep8
    CONVERSIONS["PETAWATT:YOTTAWATT"] = \
        Mul(Pow(10, 9), Sym("petaw"))  # nopep8
    CONVERSIONS["PETAWATT:ZEPTOWATT"] = \
        Mul(Rat(Int(1), Pow(10, 36)), Sym("petaw"))  # nopep8
    CONVERSIONS["PETAWATT:ZETTAWATT"] = \
        Mul(Pow(10, 6), Sym("petaw"))  # nopep8
    CONVERSIONS["PICOWATT:ATTOWATT"] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("picow"))  # nopep8
    CONVERSIONS["PICOWATT:CENTIWATT"] = \
        Mul(Pow(10, 10), Sym("picow"))  # nopep8
    CONVERSIONS["PICOWATT:DECAWATT"] = \
        Mul(Pow(10, 13), Sym("picow"))  # nopep8
    CONVERSIONS["PICOWATT:DECIWATT"] = \
        Mul(Pow(10, 11), Sym("picow"))  # nopep8
    CONVERSIONS["PICOWATT:EXAWATT"] = \
        Mul(Pow(10, 30), Sym("picow"))  # nopep8
    CONVERSIONS["PICOWATT:FEMTOWATT"] = \
        Mul(Rat(Int(1), Int(1000)), Sym("picow"))  # nopep8
    CONVERSIONS["PICOWATT:GIGAWATT"] = \
        Mul(Pow(10, 21), Sym("picow"))  # nopep8
    CONVERSIONS["PICOWATT:HECTOWATT"] = \
        Mul(Pow(10, 14), Sym("picow"))  # nopep8
    CONVERSIONS["PICOWATT:KILOWATT"] = \
        Mul(Pow(10, 15), Sym("picow"))  # nopep8
    CONVERSIONS["PICOWATT:MEGAWATT"] = \
        Mul(Pow(10, 18), Sym("picow"))  # nopep8
    CONVERSIONS["PICOWATT:MICROWATT"] = \
        Mul(Pow(10, 6), Sym("picow"))  # nopep8
    CONVERSIONS["PICOWATT:MILLIWATT"] = \
        Mul(Pow(10, 9), Sym("picow"))  # nopep8
    CONVERSIONS["PICOWATT:NANOWATT"] = \
        Mul(Int(1000), Sym("picow"))  # nopep8
    CONVERSIONS["PICOWATT:PETAWATT"] = \
        Mul(Pow(10, 27), Sym("picow"))  # nopep8
    CONVERSIONS["PICOWATT:TERAWATT"] = \
        Mul(Pow(10, 24), Sym("picow"))  # nopep8
    CONVERSIONS["PICOWATT:WATT"] = \
        Mul(Pow(10, 12), Sym("picow"))  # nopep8
    CONVERSIONS["PICOWATT:YOCTOWATT"] = \
        Mul(Rat(Int(1), Pow(10, 12)), Sym("picow"))  # nopep8
    CONVERSIONS["PICOWATT:YOTTAWATT"] = \
        Mul(Pow(10, 36), Sym("picow"))  # nopep8
    CONVERSIONS["PICOWATT:ZEPTOWATT"] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("picow"))  # nopep8
    CONVERSIONS["PICOWATT:ZETTAWATT"] = \
        Mul(Pow(10, 33), Sym("picow"))  # nopep8
    CONVERSIONS["TERAWATT:ATTOWATT"] = \
        Mul(Rat(Int(1), Pow(10, 30)), Sym("teraw"))  # nopep8
    CONVERSIONS["TERAWATT:CENTIWATT"] = \
        Mul(Rat(Int(1), Pow(10, 14)), Sym("teraw"))  # nopep8
    CONVERSIONS["TERAWATT:DECAWATT"] = \
        Mul(Rat(Int(1), Pow(10, 11)), Sym("teraw"))  # nopep8
    CONVERSIONS["TERAWATT:DECIWATT"] = \
        Mul(Rat(Int(1), Pow(10, 13)), Sym("teraw"))  # nopep8
    CONVERSIONS["TERAWATT:EXAWATT"] = \
        Mul(Pow(10, 6), Sym("teraw"))  # nopep8
    CONVERSIONS["TERAWATT:FEMTOWATT"] = \
        Mul(Rat(Int(1), Pow(10, 27)), Sym("teraw"))  # nopep8
    CONVERSIONS["TERAWATT:GIGAWATT"] = \
        Mul(Rat(Int(1), Int(1000)), Sym("teraw"))  # nopep8
    CONVERSIONS["TERAWATT:HECTOWATT"] = \
        Mul(Rat(Int(1), Pow(10, 10)), Sym("teraw"))  # nopep8
    CONVERSIONS["TERAWATT:KILOWATT"] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("teraw"))  # nopep8
    CONVERSIONS["TERAWATT:MEGAWATT"] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("teraw"))  # nopep8
    CONVERSIONS["TERAWATT:MICROWATT"] = \
        Mul(Rat(Int(1), Pow(10, 18)), Sym("teraw"))  # nopep8
    CONVERSIONS["TERAWATT:MILLIWATT"] = \
        Mul(Rat(Int(1), Pow(10, 15)), Sym("teraw"))  # nopep8
    CONVERSIONS["TERAWATT:NANOWATT"] = \
        Mul(Rat(Int(1), Pow(10, 21)), Sym("teraw"))  # nopep8
    CONVERSIONS["TERAWATT:PETAWATT"] = \
        Mul(Int(1000), Sym("teraw"))  # nopep8
    CONVERSIONS["TERAWATT:PICOWATT"] = \
        Mul(Rat(Int(1), Pow(10, 24)), Sym("teraw"))  # nopep8
    CONVERSIONS["TERAWATT:WATT"] = \
        Mul(Rat(Int(1), Pow(10, 12)), Sym("teraw"))  # nopep8
    CONVERSIONS["TERAWATT:YOCTOWATT"] = \
        Mul(Rat(Int(1), Pow(10, 36)), Sym("teraw"))  # nopep8
    CONVERSIONS["TERAWATT:YOTTAWATT"] = \
        Mul(Pow(10, 12), Sym("teraw"))  # nopep8
    CONVERSIONS["TERAWATT:ZEPTOWATT"] = \
        Mul(Rat(Int(1), Pow(10, 33)), Sym("teraw"))  # nopep8
    CONVERSIONS["TERAWATT:ZETTAWATT"] = \
        Mul(Pow(10, 9), Sym("teraw"))  # nopep8
    CONVERSIONS["WATT:ATTOWATT"] = \
        Mul(Rat(Int(1), Pow(10, 18)), Sym("w"))  # nopep8
    CONVERSIONS["WATT:CENTIWATT"] = \
        Mul(Rat(Int(1), Int(100)), Sym("w"))  # nopep8
    CONVERSIONS["WATT:DECAWATT"] = \
        Mul(Int(10), Sym("w"))  # nopep8
    CONVERSIONS["WATT:DECIWATT"] = \
        Mul(Rat(Int(1), Int(10)), Sym("w"))  # nopep8
    CONVERSIONS["WATT:EXAWATT"] = \
        Mul(Pow(10, 18), Sym("w"))  # nopep8
    CONVERSIONS["WATT:FEMTOWATT"] = \
        Mul(Rat(Int(1), Pow(10, 15)), Sym("w"))  # nopep8
    CONVERSIONS["WATT:GIGAWATT"] = \
        Mul(Pow(10, 9), Sym("w"))  # nopep8
    CONVERSIONS["WATT:HECTOWATT"] = \
        Mul(Int(100), Sym("w"))  # nopep8
    CONVERSIONS["WATT:KILOWATT"] = \
        Mul(Int(1000), Sym("w"))  # nopep8
    CONVERSIONS["WATT:MEGAWATT"] = \
        Mul(Pow(10, 6), Sym("w"))  # nopep8
    CONVERSIONS["WATT:MICROWATT"] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("w"))  # nopep8
    CONVERSIONS["WATT:MILLIWATT"] = \
        Mul(Rat(Int(1), Int(1000)), Sym("w"))  # nopep8
    CONVERSIONS["WATT:NANOWATT"] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("w"))  # nopep8
    CONVERSIONS["WATT:PETAWATT"] = \
        Mul(Pow(10, 15), Sym("w"))  # nopep8
    CONVERSIONS["WATT:PICOWATT"] = \
        Mul(Rat(Int(1), Pow(10, 12)), Sym("w"))  # nopep8
    CONVERSIONS["WATT:TERAWATT"] = \
        Mul(Pow(10, 12), Sym("w"))  # nopep8
    CONVERSIONS["WATT:YOCTOWATT"] = \
        Mul(Rat(Int(1), Pow(10, 24)), Sym("w"))  # nopep8
    CONVERSIONS["WATT:YOTTAWATT"] = \
        Mul(Pow(10, 24), Sym("w"))  # nopep8
    CONVERSIONS["WATT:ZEPTOWATT"] = \
        Mul(Rat(Int(1), Pow(10, 21)), Sym("w"))  # nopep8
    CONVERSIONS["WATT:ZETTAWATT"] = \
        Mul(Pow(10, 21), Sym("w"))  # nopep8
    CONVERSIONS["YOCTOWATT:ATTOWATT"] = \
        Mul(Pow(10, 6), Sym("yoctow"))  # nopep8
    CONVERSIONS["YOCTOWATT:CENTIWATT"] = \
        Mul(Pow(10, 22), Sym("yoctow"))  # nopep8
    CONVERSIONS["YOCTOWATT:DECAWATT"] = \
        Mul(Pow(10, 25), Sym("yoctow"))  # nopep8
    CONVERSIONS["YOCTOWATT:DECIWATT"] = \
        Mul(Pow(10, 23), Sym("yoctow"))  # nopep8
    CONVERSIONS["YOCTOWATT:EXAWATT"] = \
        Mul(Pow(10, 42), Sym("yoctow"))  # nopep8
    CONVERSIONS["YOCTOWATT:FEMTOWATT"] = \
        Mul(Pow(10, 9), Sym("yoctow"))  # nopep8
    CONVERSIONS["YOCTOWATT:GIGAWATT"] = \
        Mul(Pow(10, 33), Sym("yoctow"))  # nopep8
    CONVERSIONS["YOCTOWATT:HECTOWATT"] = \
        Mul(Pow(10, 26), Sym("yoctow"))  # nopep8
    CONVERSIONS["YOCTOWATT:KILOWATT"] = \
        Mul(Pow(10, 27), Sym("yoctow"))  # nopep8
    CONVERSIONS["YOCTOWATT:MEGAWATT"] = \
        Mul(Pow(10, 30), Sym("yoctow"))  # nopep8
    CONVERSIONS["YOCTOWATT:MICROWATT"] = \
        Mul(Pow(10, 18), Sym("yoctow"))  # nopep8
    CONVERSIONS["YOCTOWATT:MILLIWATT"] = \
        Mul(Pow(10, 21), Sym("yoctow"))  # nopep8
    CONVERSIONS["YOCTOWATT:NANOWATT"] = \
        Mul(Pow(10, 15), Sym("yoctow"))  # nopep8
    CONVERSIONS["YOCTOWATT:PETAWATT"] = \
        Mul(Pow(10, 39), Sym("yoctow"))  # nopep8
    CONVERSIONS["YOCTOWATT:PICOWATT"] = \
        Mul(Pow(10, 12), Sym("yoctow"))  # nopep8
    CONVERSIONS["YOCTOWATT:TERAWATT"] = \
        Mul(Pow(10, 36), Sym("yoctow"))  # nopep8
    CONVERSIONS["YOCTOWATT:WATT"] = \
        Mul(Pow(10, 24), Sym("yoctow"))  # nopep8
    CONVERSIONS["YOCTOWATT:YOTTAWATT"] = \
        Mul(Pow(10, 48), Sym("yoctow"))  # nopep8
    CONVERSIONS["YOCTOWATT:ZEPTOWATT"] = \
        Mul(Int(1000), Sym("yoctow"))  # nopep8
    CONVERSIONS["YOCTOWATT:ZETTAWATT"] = \
        Mul(Pow(10, 45), Sym("yoctow"))  # nopep8
    CONVERSIONS["YOTTAWATT:ATTOWATT"] = \
        Mul(Rat(Int(1), Pow(10, 42)), Sym("yottaw"))  # nopep8
    CONVERSIONS["YOTTAWATT:CENTIWATT"] = \
        Mul(Rat(Int(1), Pow(10, 26)), Sym("yottaw"))  # nopep8
    CONVERSIONS["YOTTAWATT:DECAWATT"] = \
        Mul(Rat(Int(1), Pow(10, 23)), Sym("yottaw"))  # nopep8
    CONVERSIONS["YOTTAWATT:DECIWATT"] = \
        Mul(Rat(Int(1), Pow(10, 25)), Sym("yottaw"))  # nopep8
    CONVERSIONS["YOTTAWATT:EXAWATT"] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("yottaw"))  # nopep8
    CONVERSIONS["YOTTAWATT:FEMTOWATT"] = \
        Mul(Rat(Int(1), Pow(10, 39)), Sym("yottaw"))  # nopep8
    CONVERSIONS["YOTTAWATT:GIGAWATT"] = \
        Mul(Rat(Int(1), Pow(10, 15)), Sym("yottaw"))  # nopep8
    CONVERSIONS["YOTTAWATT:HECTOWATT"] = \
        Mul(Rat(Int(1), Pow(10, 22)), Sym("yottaw"))  # nopep8
    CONVERSIONS["YOTTAWATT:KILOWATT"] = \
        Mul(Rat(Int(1), Pow(10, 21)), Sym("yottaw"))  # nopep8
    CONVERSIONS["YOTTAWATT:MEGAWATT"] = \
        Mul(Rat(Int(1), Pow(10, 18)), Sym("yottaw"))  # nopep8
    CONVERSIONS["YOTTAWATT:MICROWATT"] = \
        Mul(Rat(Int(1), Pow(10, 30)), Sym("yottaw"))  # nopep8
    CONVERSIONS["YOTTAWATT:MILLIWATT"] = \
        Mul(Rat(Int(1), Pow(10, 27)), Sym("yottaw"))  # nopep8
    CONVERSIONS["YOTTAWATT:NANOWATT"] = \
        Mul(Rat(Int(1), Pow(10, 33)), Sym("yottaw"))  # nopep8
    CONVERSIONS["YOTTAWATT:PETAWATT"] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("yottaw"))  # nopep8
    CONVERSIONS["YOTTAWATT:PICOWATT"] = \
        Mul(Rat(Int(1), Pow(10, 36)), Sym("yottaw"))  # nopep8
    CONVERSIONS["YOTTAWATT:TERAWATT"] = \
        Mul(Rat(Int(1), Pow(10, 12)), Sym("yottaw"))  # nopep8
    CONVERSIONS["YOTTAWATT:WATT"] = \
        Mul(Rat(Int(1), Pow(10, 24)), Sym("yottaw"))  # nopep8
    CONVERSIONS["YOTTAWATT:YOCTOWATT"] = \
        Mul(Rat(Int(1), Pow(10, 48)), Sym("yottaw"))  # nopep8
    CONVERSIONS["YOTTAWATT:ZEPTOWATT"] = \
        Mul(Rat(Int(1), Pow(10, 45)), Sym("yottaw"))  # nopep8
    CONVERSIONS["YOTTAWATT:ZETTAWATT"] = \
        Mul(Rat(Int(1), Int(1000)), Sym("yottaw"))  # nopep8
    CONVERSIONS["ZEPTOWATT:ATTOWATT"] = \
        Mul(Int(1000), Sym("zeptow"))  # nopep8
    CONVERSIONS["ZEPTOWATT:CENTIWATT"] = \
        Mul(Pow(10, 19), Sym("zeptow"))  # nopep8
    CONVERSIONS["ZEPTOWATT:DECAWATT"] = \
        Mul(Pow(10, 22), Sym("zeptow"))  # nopep8
    CONVERSIONS["ZEPTOWATT:DECIWATT"] = \
        Mul(Pow(10, 20), Sym("zeptow"))  # nopep8
    CONVERSIONS["ZEPTOWATT:EXAWATT"] = \
        Mul(Pow(10, 39), Sym("zeptow"))  # nopep8
    CONVERSIONS["ZEPTOWATT:FEMTOWATT"] = \
        Mul(Pow(10, 6), Sym("zeptow"))  # nopep8
    CONVERSIONS["ZEPTOWATT:GIGAWATT"] = \
        Mul(Pow(10, 30), Sym("zeptow"))  # nopep8
    CONVERSIONS["ZEPTOWATT:HECTOWATT"] = \
        Mul(Pow(10, 23), Sym("zeptow"))  # nopep8
    CONVERSIONS["ZEPTOWATT:KILOWATT"] = \
        Mul(Pow(10, 24), Sym("zeptow"))  # nopep8
    CONVERSIONS["ZEPTOWATT:MEGAWATT"] = \
        Mul(Pow(10, 27), Sym("zeptow"))  # nopep8
    CONVERSIONS["ZEPTOWATT:MICROWATT"] = \
        Mul(Pow(10, 15), Sym("zeptow"))  # nopep8
    CONVERSIONS["ZEPTOWATT:MILLIWATT"] = \
        Mul(Pow(10, 18), Sym("zeptow"))  # nopep8
    CONVERSIONS["ZEPTOWATT:NANOWATT"] = \
        Mul(Pow(10, 12), Sym("zeptow"))  # nopep8
    CONVERSIONS["ZEPTOWATT:PETAWATT"] = \
        Mul(Pow(10, 36), Sym("zeptow"))  # nopep8
    CONVERSIONS["ZEPTOWATT:PICOWATT"] = \
        Mul(Pow(10, 9), Sym("zeptow"))  # nopep8
    CONVERSIONS["ZEPTOWATT:TERAWATT"] = \
        Mul(Pow(10, 33), Sym("zeptow"))  # nopep8
    CONVERSIONS["ZEPTOWATT:WATT"] = \
        Mul(Pow(10, 21), Sym("zeptow"))  # nopep8
    CONVERSIONS["ZEPTOWATT:YOCTOWATT"] = \
        Mul(Rat(Int(1), Int(1000)), Sym("zeptow"))  # nopep8
    CONVERSIONS["ZEPTOWATT:YOTTAWATT"] = \
        Mul(Pow(10, 45), Sym("zeptow"))  # nopep8
    CONVERSIONS["ZEPTOWATT:ZETTAWATT"] = \
        Mul(Pow(10, 42), Sym("zeptow"))  # nopep8
    CONVERSIONS["ZETTAWATT:ATTOWATT"] = \
        Mul(Rat(Int(1), Pow(10, 39)), Sym("zettaw"))  # nopep8
    CONVERSIONS["ZETTAWATT:CENTIWATT"] = \
        Mul(Rat(Int(1), Pow(10, 23)), Sym("zettaw"))  # nopep8
    CONVERSIONS["ZETTAWATT:DECAWATT"] = \
        Mul(Rat(Int(1), Pow(10, 20)), Sym("zettaw"))  # nopep8
    CONVERSIONS["ZETTAWATT:DECIWATT"] = \
        Mul(Rat(Int(1), Pow(10, 22)), Sym("zettaw"))  # nopep8
    CONVERSIONS["ZETTAWATT:EXAWATT"] = \
        Mul(Rat(Int(1), Int(1000)), Sym("zettaw"))  # nopep8
    CONVERSIONS["ZETTAWATT:FEMTOWATT"] = \
        Mul(Rat(Int(1), Pow(10, 36)), Sym("zettaw"))  # nopep8
    CONVERSIONS["ZETTAWATT:GIGAWATT"] = \
        Mul(Rat(Int(1), Pow(10, 12)), Sym("zettaw"))  # nopep8
    CONVERSIONS["ZETTAWATT:HECTOWATT"] = \
        Mul(Rat(Int(1), Pow(10, 19)), Sym("zettaw"))  # nopep8
    CONVERSIONS["ZETTAWATT:KILOWATT"] = \
        Mul(Rat(Int(1), Pow(10, 18)), Sym("zettaw"))  # nopep8
    CONVERSIONS["ZETTAWATT:MEGAWATT"] = \
        Mul(Rat(Int(1), Pow(10, 15)), Sym("zettaw"))  # nopep8
    CONVERSIONS["ZETTAWATT:MICROWATT"] = \
        Mul(Rat(Int(1), Pow(10, 27)), Sym("zettaw"))  # nopep8
    CONVERSIONS["ZETTAWATT:MILLIWATT"] = \
        Mul(Rat(Int(1), Pow(10, 24)), Sym("zettaw"))  # nopep8
    CONVERSIONS["ZETTAWATT:NANOWATT"] = \
        Mul(Rat(Int(1), Pow(10, 30)), Sym("zettaw"))  # nopep8
    CONVERSIONS["ZETTAWATT:PETAWATT"] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("zettaw"))  # nopep8
    CONVERSIONS["ZETTAWATT:PICOWATT"] = \
        Mul(Rat(Int(1), Pow(10, 33)), Sym("zettaw"))  # nopep8
    CONVERSIONS["ZETTAWATT:TERAWATT"] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("zettaw"))  # nopep8
    CONVERSIONS["ZETTAWATT:WATT"] = \
        Mul(Rat(Int(1), Pow(10, 21)), Sym("zettaw"))  # nopep8
    CONVERSIONS["ZETTAWATT:YOCTOWATT"] = \
        Mul(Rat(Int(1), Pow(10, 45)), Sym("zettaw"))  # nopep8
    CONVERSIONS["ZETTAWATT:YOTTAWATT"] = \
        Mul(Int(1000), Sym("zettaw"))  # nopep8
    CONVERSIONS["ZETTAWATT:ZEPTOWATT"] = \
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
                self.setUnit(getattr(UnitsPower, str(target)))
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
