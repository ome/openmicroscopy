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

from omero.model.conversions import Add
from omero.model.conversions import Int
from omero.model.conversions import Mul
from omero.model.conversions import Pow
from omero.model.conversions import Rat
from omero.model.conversions import Sym


class PowerI(_omero_model.Power, UnitBase):

    CONVERSIONS = dict()
    CONVERSIONS["ATTOWATT:CENTIWATT"] = \
        Mul(Pow(10, 16), Sym("attow"))
    CONVERSIONS["ATTOWATT:DECAWATT"] = \
        Mul(Pow(10, 19), Sym("attow"))
    CONVERSIONS["ATTOWATT:DECIWATT"] = \
        Mul(Pow(10, 17), Sym("attow"))
    CONVERSIONS["ATTOWATT:EXAWATT"] = \
        Mul(Pow(10, 36), Sym("attow"))
    CONVERSIONS["ATTOWATT:FEMTOWATT"] = \
        Mul(Int(1000), Sym("attow"))
    CONVERSIONS["ATTOWATT:GIGAWATT"] = \
        Mul(Pow(10, 27), Sym("attow"))
    CONVERSIONS["ATTOWATT:HECTOWATT"] = \
        Mul(Pow(10, 20), Sym("attow"))
    CONVERSIONS["ATTOWATT:KILOWATT"] = \
        Mul(Pow(10, 21), Sym("attow"))
    CONVERSIONS["ATTOWATT:MEGAWATT"] = \
        Mul(Pow(10, 24), Sym("attow"))
    CONVERSIONS["ATTOWATT:MICROWATT"] = \
        Mul(Pow(10, 12), Sym("attow"))
    CONVERSIONS["ATTOWATT:MILLIWATT"] = \
        Mul(Pow(10, 15), Sym("attow"))
    CONVERSIONS["ATTOWATT:NANOWATT"] = \
        Mul(Pow(10, 9), Sym("attow"))
    CONVERSIONS["ATTOWATT:PETAWATT"] = \
        Mul(Pow(10, 33), Sym("attow"))
    CONVERSIONS["ATTOWATT:PICOWATT"] = \
        Mul(Pow(10, 6), Sym("attow"))
    CONVERSIONS["ATTOWATT:TERAWATT"] = \
        Mul(Pow(10, 30), Sym("attow"))
    CONVERSIONS["ATTOWATT:WATT"] = \
        Mul(Pow(10, 18), Sym("attow"))
    CONVERSIONS["ATTOWATT:YOCTOWATT"] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("attow"))
    CONVERSIONS["ATTOWATT:YOTTAWATT"] = \
        Mul(Pow(10, 42), Sym("attow"))
    CONVERSIONS["ATTOWATT:ZEPTOWATT"] = \
        Mul(Rat(Int(1), Int(1000)), Sym("attow"))
    CONVERSIONS["ATTOWATT:ZETTAWATT"] = \
        Mul(Pow(10, 39), Sym("attow"))
    CONVERSIONS["CENTIWATT:ATTOWATT"] = \
        Mul(Rat(Int(1), Pow(10, 16)), Sym("centiw"))
    CONVERSIONS["CENTIWATT:DECAWATT"] = \
        Mul(Int(1000), Sym("centiw"))
    CONVERSIONS["CENTIWATT:DECIWATT"] = \
        Mul(Int(10), Sym("centiw"))
    CONVERSIONS["CENTIWATT:EXAWATT"] = \
        Mul(Pow(10, 20), Sym("centiw"))
    CONVERSIONS["CENTIWATT:FEMTOWATT"] = \
        Mul(Rat(Int(1), Pow(10, 13)), Sym("centiw"))
    CONVERSIONS["CENTIWATT:GIGAWATT"] = \
        Mul(Pow(10, 11), Sym("centiw"))
    CONVERSIONS["CENTIWATT:HECTOWATT"] = \
        Mul(Pow(10, 4), Sym("centiw"))
    CONVERSIONS["CENTIWATT:KILOWATT"] = \
        Mul(Pow(10, 5), Sym("centiw"))
    CONVERSIONS["CENTIWATT:MEGAWATT"] = \
        Mul(Pow(10, 8), Sym("centiw"))
    CONVERSIONS["CENTIWATT:MICROWATT"] = \
        Mul(Rat(Int(1), Pow(10, 4)), Sym("centiw"))
    CONVERSIONS["CENTIWATT:MILLIWATT"] = \
        Mul(Rat(Int(1), Int(10)), Sym("centiw"))
    CONVERSIONS["CENTIWATT:NANOWATT"] = \
        Mul(Rat(Int(1), Pow(10, 7)), Sym("centiw"))
    CONVERSIONS["CENTIWATT:PETAWATT"] = \
        Mul(Pow(10, 17), Sym("centiw"))
    CONVERSIONS["CENTIWATT:PICOWATT"] = \
        Mul(Rat(Int(1), Pow(10, 10)), Sym("centiw"))
    CONVERSIONS["CENTIWATT:TERAWATT"] = \
        Mul(Pow(10, 14), Sym("centiw"))
    CONVERSIONS["CENTIWATT:WATT"] = \
        Mul(Int(100), Sym("centiw"))
    CONVERSIONS["CENTIWATT:YOCTOWATT"] = \
        Mul(Rat(Int(1), Pow(10, 22)), Sym("centiw"))
    CONVERSIONS["CENTIWATT:YOTTAWATT"] = \
        Mul(Pow(10, 26), Sym("centiw"))
    CONVERSIONS["CENTIWATT:ZEPTOWATT"] = \
        Mul(Rat(Int(1), Pow(10, 19)), Sym("centiw"))
    CONVERSIONS["CENTIWATT:ZETTAWATT"] = \
        Mul(Pow(10, 23), Sym("centiw"))
    CONVERSIONS["DECAWATT:ATTOWATT"] = \
        Mul(Rat(Int(1), Pow(10, 19)), Sym("decaw"))
    CONVERSIONS["DECAWATT:CENTIWATT"] = \
        Mul(Rat(Int(1), Int(1000)), Sym("decaw"))
    CONVERSIONS["DECAWATT:DECIWATT"] = \
        Mul(Rat(Int(1), Int(100)), Sym("decaw"))
    CONVERSIONS["DECAWATT:EXAWATT"] = \
        Mul(Pow(10, 17), Sym("decaw"))
    CONVERSIONS["DECAWATT:FEMTOWATT"] = \
        Mul(Rat(Int(1), Pow(10, 16)), Sym("decaw"))
    CONVERSIONS["DECAWATT:GIGAWATT"] = \
        Mul(Pow(10, 8), Sym("decaw"))
    CONVERSIONS["DECAWATT:HECTOWATT"] = \
        Mul(Int(10), Sym("decaw"))
    CONVERSIONS["DECAWATT:KILOWATT"] = \
        Mul(Int(100), Sym("decaw"))
    CONVERSIONS["DECAWATT:MEGAWATT"] = \
        Mul(Pow(10, 5), Sym("decaw"))
    CONVERSIONS["DECAWATT:MICROWATT"] = \
        Mul(Rat(Int(1), Pow(10, 7)), Sym("decaw"))
    CONVERSIONS["DECAWATT:MILLIWATT"] = \
        Mul(Rat(Int(1), Pow(10, 4)), Sym("decaw"))
    CONVERSIONS["DECAWATT:NANOWATT"] = \
        Mul(Rat(Int(1), Pow(10, 10)), Sym("decaw"))
    CONVERSIONS["DECAWATT:PETAWATT"] = \
        Mul(Pow(10, 14), Sym("decaw"))
    CONVERSIONS["DECAWATT:PICOWATT"] = \
        Mul(Rat(Int(1), Pow(10, 13)), Sym("decaw"))
    CONVERSIONS["DECAWATT:TERAWATT"] = \
        Mul(Pow(10, 11), Sym("decaw"))
    CONVERSIONS["DECAWATT:WATT"] = \
        Mul(Rat(Int(1), Int(10)), Sym("decaw"))
    CONVERSIONS["DECAWATT:YOCTOWATT"] = \
        Mul(Rat(Int(1), Pow(10, 25)), Sym("decaw"))
    CONVERSIONS["DECAWATT:YOTTAWATT"] = \
        Mul(Pow(10, 23), Sym("decaw"))
    CONVERSIONS["DECAWATT:ZEPTOWATT"] = \
        Mul(Rat(Int(1), Pow(10, 22)), Sym("decaw"))
    CONVERSIONS["DECAWATT:ZETTAWATT"] = \
        Mul(Pow(10, 20), Sym("decaw"))
    CONVERSIONS["DECIWATT:ATTOWATT"] = \
        Mul(Rat(Int(1), Pow(10, 17)), Sym("deciw"))
    CONVERSIONS["DECIWATT:CENTIWATT"] = \
        Mul(Rat(Int(1), Int(10)), Sym("deciw"))
    CONVERSIONS["DECIWATT:DECAWATT"] = \
        Mul(Int(100), Sym("deciw"))
    CONVERSIONS["DECIWATT:EXAWATT"] = \
        Mul(Pow(10, 19), Sym("deciw"))
    CONVERSIONS["DECIWATT:FEMTOWATT"] = \
        Mul(Rat(Int(1), Pow(10, 14)), Sym("deciw"))
    CONVERSIONS["DECIWATT:GIGAWATT"] = \
        Mul(Pow(10, 10), Sym("deciw"))
    CONVERSIONS["DECIWATT:HECTOWATT"] = \
        Mul(Int(1000), Sym("deciw"))
    CONVERSIONS["DECIWATT:KILOWATT"] = \
        Mul(Pow(10, 4), Sym("deciw"))
    CONVERSIONS["DECIWATT:MEGAWATT"] = \
        Mul(Pow(10, 7), Sym("deciw"))
    CONVERSIONS["DECIWATT:MICROWATT"] = \
        Mul(Rat(Int(1), Pow(10, 5)), Sym("deciw"))
    CONVERSIONS["DECIWATT:MILLIWATT"] = \
        Mul(Rat(Int(1), Int(100)), Sym("deciw"))
    CONVERSIONS["DECIWATT:NANOWATT"] = \
        Mul(Rat(Int(1), Pow(10, 8)), Sym("deciw"))
    CONVERSIONS["DECIWATT:PETAWATT"] = \
        Mul(Pow(10, 16), Sym("deciw"))
    CONVERSIONS["DECIWATT:PICOWATT"] = \
        Mul(Rat(Int(1), Pow(10, 11)), Sym("deciw"))
    CONVERSIONS["DECIWATT:TERAWATT"] = \
        Mul(Pow(10, 13), Sym("deciw"))
    CONVERSIONS["DECIWATT:WATT"] = \
        Mul(Int(10), Sym("deciw"))
    CONVERSIONS["DECIWATT:YOCTOWATT"] = \
        Mul(Rat(Int(1), Pow(10, 23)), Sym("deciw"))
    CONVERSIONS["DECIWATT:YOTTAWATT"] = \
        Mul(Pow(10, 25), Sym("deciw"))
    CONVERSIONS["DECIWATT:ZEPTOWATT"] = \
        Mul(Rat(Int(1), Pow(10, 20)), Sym("deciw"))
    CONVERSIONS["DECIWATT:ZETTAWATT"] = \
        Mul(Pow(10, 22), Sym("deciw"))
    CONVERSIONS["EXAWATT:ATTOWATT"] = \
        Mul(Rat(Int(1), Pow(10, 36)), Sym("exaw"))
    CONVERSIONS["EXAWATT:CENTIWATT"] = \
        Mul(Rat(Int(1), Pow(10, 20)), Sym("exaw"))
    CONVERSIONS["EXAWATT:DECAWATT"] = \
        Mul(Rat(Int(1), Pow(10, 17)), Sym("exaw"))
    CONVERSIONS["EXAWATT:DECIWATT"] = \
        Mul(Rat(Int(1), Pow(10, 19)), Sym("exaw"))
    CONVERSIONS["EXAWATT:FEMTOWATT"] = \
        Mul(Rat(Int(1), Pow(10, 33)), Sym("exaw"))
    CONVERSIONS["EXAWATT:GIGAWATT"] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("exaw"))
    CONVERSIONS["EXAWATT:HECTOWATT"] = \
        Mul(Rat(Int(1), Pow(10, 16)), Sym("exaw"))
    CONVERSIONS["EXAWATT:KILOWATT"] = \
        Mul(Rat(Int(1), Pow(10, 15)), Sym("exaw"))
    CONVERSIONS["EXAWATT:MEGAWATT"] = \
        Mul(Rat(Int(1), Pow(10, 12)), Sym("exaw"))
    CONVERSIONS["EXAWATT:MICROWATT"] = \
        Mul(Rat(Int(1), Pow(10, 24)), Sym("exaw"))
    CONVERSIONS["EXAWATT:MILLIWATT"] = \
        Mul(Rat(Int(1), Pow(10, 21)), Sym("exaw"))
    CONVERSIONS["EXAWATT:NANOWATT"] = \
        Mul(Rat(Int(1), Pow(10, 27)), Sym("exaw"))
    CONVERSIONS["EXAWATT:PETAWATT"] = \
        Mul(Rat(Int(1), Int(1000)), Sym("exaw"))
    CONVERSIONS["EXAWATT:PICOWATT"] = \
        Mul(Rat(Int(1), Pow(10, 30)), Sym("exaw"))
    CONVERSIONS["EXAWATT:TERAWATT"] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("exaw"))
    CONVERSIONS["EXAWATT:WATT"] = \
        Mul(Rat(Int(1), Pow(10, 18)), Sym("exaw"))
    CONVERSIONS["EXAWATT:YOCTOWATT"] = \
        Mul(Rat(Int(1), Pow(10, 42)), Sym("exaw"))
    CONVERSIONS["EXAWATT:YOTTAWATT"] = \
        Mul(Pow(10, 6), Sym("exaw"))
    CONVERSIONS["EXAWATT:ZEPTOWATT"] = \
        Mul(Rat(Int(1), Pow(10, 39)), Sym("exaw"))
    CONVERSIONS["EXAWATT:ZETTAWATT"] = \
        Mul(Int(1000), Sym("exaw"))
    CONVERSIONS["FEMTOWATT:ATTOWATT"] = \
        Mul(Rat(Int(1), Int(1000)), Sym("femtow"))
    CONVERSIONS["FEMTOWATT:CENTIWATT"] = \
        Mul(Pow(10, 13), Sym("femtow"))
    CONVERSIONS["FEMTOWATT:DECAWATT"] = \
        Mul(Pow(10, 16), Sym("femtow"))
    CONVERSIONS["FEMTOWATT:DECIWATT"] = \
        Mul(Pow(10, 14), Sym("femtow"))
    CONVERSIONS["FEMTOWATT:EXAWATT"] = \
        Mul(Pow(10, 33), Sym("femtow"))
    CONVERSIONS["FEMTOWATT:GIGAWATT"] = \
        Mul(Pow(10, 24), Sym("femtow"))
    CONVERSIONS["FEMTOWATT:HECTOWATT"] = \
        Mul(Pow(10, 17), Sym("femtow"))
    CONVERSIONS["FEMTOWATT:KILOWATT"] = \
        Mul(Pow(10, 18), Sym("femtow"))
    CONVERSIONS["FEMTOWATT:MEGAWATT"] = \
        Mul(Pow(10, 21), Sym("femtow"))
    CONVERSIONS["FEMTOWATT:MICROWATT"] = \
        Mul(Pow(10, 9), Sym("femtow"))
    CONVERSIONS["FEMTOWATT:MILLIWATT"] = \
        Mul(Pow(10, 12), Sym("femtow"))
    CONVERSIONS["FEMTOWATT:NANOWATT"] = \
        Mul(Pow(10, 6), Sym("femtow"))
    CONVERSIONS["FEMTOWATT:PETAWATT"] = \
        Mul(Pow(10, 30), Sym("femtow"))
    CONVERSIONS["FEMTOWATT:PICOWATT"] = \
        Mul(Int(1000), Sym("femtow"))
    CONVERSIONS["FEMTOWATT:TERAWATT"] = \
        Mul(Pow(10, 27), Sym("femtow"))
    CONVERSIONS["FEMTOWATT:WATT"] = \
        Mul(Pow(10, 15), Sym("femtow"))
    CONVERSIONS["FEMTOWATT:YOCTOWATT"] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("femtow"))
    CONVERSIONS["FEMTOWATT:YOTTAWATT"] = \
        Mul(Pow(10, 39), Sym("femtow"))
    CONVERSIONS["FEMTOWATT:ZEPTOWATT"] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("femtow"))
    CONVERSIONS["FEMTOWATT:ZETTAWATT"] = \
        Mul(Pow(10, 36), Sym("femtow"))
    CONVERSIONS["GIGAWATT:ATTOWATT"] = \
        Mul(Rat(Int(1), Pow(10, 27)), Sym("gigaw"))
    CONVERSIONS["GIGAWATT:CENTIWATT"] = \
        Mul(Rat(Int(1), Pow(10, 11)), Sym("gigaw"))
    CONVERSIONS["GIGAWATT:DECAWATT"] = \
        Mul(Rat(Int(1), Pow(10, 8)), Sym("gigaw"))
    CONVERSIONS["GIGAWATT:DECIWATT"] = \
        Mul(Rat(Int(1), Pow(10, 10)), Sym("gigaw"))
    CONVERSIONS["GIGAWATT:EXAWATT"] = \
        Mul(Pow(10, 9), Sym("gigaw"))
    CONVERSIONS["GIGAWATT:FEMTOWATT"] = \
        Mul(Rat(Int(1), Pow(10, 24)), Sym("gigaw"))
    CONVERSIONS["GIGAWATT:HECTOWATT"] = \
        Mul(Rat(Int(1), Pow(10, 7)), Sym("gigaw"))
    CONVERSIONS["GIGAWATT:KILOWATT"] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("gigaw"))
    CONVERSIONS["GIGAWATT:MEGAWATT"] = \
        Mul(Rat(Int(1), Int(1000)), Sym("gigaw"))
    CONVERSIONS["GIGAWATT:MICROWATT"] = \
        Mul(Rat(Int(1), Pow(10, 15)), Sym("gigaw"))
    CONVERSIONS["GIGAWATT:MILLIWATT"] = \
        Mul(Rat(Int(1), Pow(10, 12)), Sym("gigaw"))
    CONVERSIONS["GIGAWATT:NANOWATT"] = \
        Mul(Rat(Int(1), Pow(10, 18)), Sym("gigaw"))
    CONVERSIONS["GIGAWATT:PETAWATT"] = \
        Mul(Pow(10, 6), Sym("gigaw"))
    CONVERSIONS["GIGAWATT:PICOWATT"] = \
        Mul(Rat(Int(1), Pow(10, 21)), Sym("gigaw"))
    CONVERSIONS["GIGAWATT:TERAWATT"] = \
        Mul(Int(1000), Sym("gigaw"))
    CONVERSIONS["GIGAWATT:WATT"] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("gigaw"))
    CONVERSIONS["GIGAWATT:YOCTOWATT"] = \
        Mul(Rat(Int(1), Pow(10, 33)), Sym("gigaw"))
    CONVERSIONS["GIGAWATT:YOTTAWATT"] = \
        Mul(Pow(10, 15), Sym("gigaw"))
    CONVERSIONS["GIGAWATT:ZEPTOWATT"] = \
        Mul(Rat(Int(1), Pow(10, 30)), Sym("gigaw"))
    CONVERSIONS["GIGAWATT:ZETTAWATT"] = \
        Mul(Pow(10, 12), Sym("gigaw"))
    CONVERSIONS["HECTOWATT:ATTOWATT"] = \
        Mul(Rat(Int(1), Pow(10, 20)), Sym("hectow"))
    CONVERSIONS["HECTOWATT:CENTIWATT"] = \
        Mul(Rat(Int(1), Pow(10, 4)), Sym("hectow"))
    CONVERSIONS["HECTOWATT:DECAWATT"] = \
        Mul(Rat(Int(1), Int(10)), Sym("hectow"))
    CONVERSIONS["HECTOWATT:DECIWATT"] = \
        Mul(Rat(Int(1), Int(1000)), Sym("hectow"))
    CONVERSIONS["HECTOWATT:EXAWATT"] = \
        Mul(Pow(10, 16), Sym("hectow"))
    CONVERSIONS["HECTOWATT:FEMTOWATT"] = \
        Mul(Rat(Int(1), Pow(10, 17)), Sym("hectow"))
    CONVERSIONS["HECTOWATT:GIGAWATT"] = \
        Mul(Pow(10, 7), Sym("hectow"))
    CONVERSIONS["HECTOWATT:KILOWATT"] = \
        Mul(Int(10), Sym("hectow"))
    CONVERSIONS["HECTOWATT:MEGAWATT"] = \
        Mul(Pow(10, 4), Sym("hectow"))
    CONVERSIONS["HECTOWATT:MICROWATT"] = \
        Mul(Rat(Int(1), Pow(10, 8)), Sym("hectow"))
    CONVERSIONS["HECTOWATT:MILLIWATT"] = \
        Mul(Rat(Int(1), Pow(10, 5)), Sym("hectow"))
    CONVERSIONS["HECTOWATT:NANOWATT"] = \
        Mul(Rat(Int(1), Pow(10, 11)), Sym("hectow"))
    CONVERSIONS["HECTOWATT:PETAWATT"] = \
        Mul(Pow(10, 13), Sym("hectow"))
    CONVERSIONS["HECTOWATT:PICOWATT"] = \
        Mul(Rat(Int(1), Pow(10, 14)), Sym("hectow"))
    CONVERSIONS["HECTOWATT:TERAWATT"] = \
        Mul(Pow(10, 10), Sym("hectow"))
    CONVERSIONS["HECTOWATT:WATT"] = \
        Mul(Rat(Int(1), Int(100)), Sym("hectow"))
    CONVERSIONS["HECTOWATT:YOCTOWATT"] = \
        Mul(Rat(Int(1), Pow(10, 26)), Sym("hectow"))
    CONVERSIONS["HECTOWATT:YOTTAWATT"] = \
        Mul(Pow(10, 22), Sym("hectow"))
    CONVERSIONS["HECTOWATT:ZEPTOWATT"] = \
        Mul(Rat(Int(1), Pow(10, 23)), Sym("hectow"))
    CONVERSIONS["HECTOWATT:ZETTAWATT"] = \
        Mul(Pow(10, 19), Sym("hectow"))
    CONVERSIONS["KILOWATT:ATTOWATT"] = \
        Mul(Rat(Int(1), Pow(10, 21)), Sym("kilow"))
    CONVERSIONS["KILOWATT:CENTIWATT"] = \
        Mul(Rat(Int(1), Pow(10, 5)), Sym("kilow"))
    CONVERSIONS["KILOWATT:DECAWATT"] = \
        Mul(Rat(Int(1), Int(100)), Sym("kilow"))
    CONVERSIONS["KILOWATT:DECIWATT"] = \
        Mul(Rat(Int(1), Pow(10, 4)), Sym("kilow"))
    CONVERSIONS["KILOWATT:EXAWATT"] = \
        Mul(Pow(10, 15), Sym("kilow"))
    CONVERSIONS["KILOWATT:FEMTOWATT"] = \
        Mul(Rat(Int(1), Pow(10, 18)), Sym("kilow"))
    CONVERSIONS["KILOWATT:GIGAWATT"] = \
        Mul(Pow(10, 6), Sym("kilow"))
    CONVERSIONS["KILOWATT:HECTOWATT"] = \
        Mul(Rat(Int(1), Int(10)), Sym("kilow"))
    CONVERSIONS["KILOWATT:MEGAWATT"] = \
        Mul(Int(1000), Sym("kilow"))
    CONVERSIONS["KILOWATT:MICROWATT"] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("kilow"))
    CONVERSIONS["KILOWATT:MILLIWATT"] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("kilow"))
    CONVERSIONS["KILOWATT:NANOWATT"] = \
        Mul(Rat(Int(1), Pow(10, 12)), Sym("kilow"))
    CONVERSIONS["KILOWATT:PETAWATT"] = \
        Mul(Pow(10, 12), Sym("kilow"))
    CONVERSIONS["KILOWATT:PICOWATT"] = \
        Mul(Rat(Int(1), Pow(10, 15)), Sym("kilow"))
    CONVERSIONS["KILOWATT:TERAWATT"] = \
        Mul(Pow(10, 9), Sym("kilow"))
    CONVERSIONS["KILOWATT:WATT"] = \
        Mul(Rat(Int(1), Int(1000)), Sym("kilow"))
    CONVERSIONS["KILOWATT:YOCTOWATT"] = \
        Mul(Rat(Int(1), Pow(10, 27)), Sym("kilow"))
    CONVERSIONS["KILOWATT:YOTTAWATT"] = \
        Mul(Pow(10, 21), Sym("kilow"))
    CONVERSIONS["KILOWATT:ZEPTOWATT"] = \
        Mul(Rat(Int(1), Pow(10, 24)), Sym("kilow"))
    CONVERSIONS["KILOWATT:ZETTAWATT"] = \
        Mul(Pow(10, 18), Sym("kilow"))
    CONVERSIONS["MEGAWATT:ATTOWATT"] = \
        Mul(Rat(Int(1), Pow(10, 24)), Sym("megaw"))
    CONVERSIONS["MEGAWATT:CENTIWATT"] = \
        Mul(Rat(Int(1), Pow(10, 8)), Sym("megaw"))
    CONVERSIONS["MEGAWATT:DECAWATT"] = \
        Mul(Rat(Int(1), Pow(10, 5)), Sym("megaw"))
    CONVERSIONS["MEGAWATT:DECIWATT"] = \
        Mul(Rat(Int(1), Pow(10, 7)), Sym("megaw"))
    CONVERSIONS["MEGAWATT:EXAWATT"] = \
        Mul(Pow(10, 12), Sym("megaw"))
    CONVERSIONS["MEGAWATT:FEMTOWATT"] = \
        Mul(Rat(Int(1), Pow(10, 21)), Sym("megaw"))
    CONVERSIONS["MEGAWATT:GIGAWATT"] = \
        Mul(Int(1000), Sym("megaw"))
    CONVERSIONS["MEGAWATT:HECTOWATT"] = \
        Mul(Rat(Int(1), Pow(10, 4)), Sym("megaw"))
    CONVERSIONS["MEGAWATT:KILOWATT"] = \
        Mul(Rat(Int(1), Int(1000)), Sym("megaw"))
    CONVERSIONS["MEGAWATT:MICROWATT"] = \
        Mul(Rat(Int(1), Pow(10, 12)), Sym("megaw"))
    CONVERSIONS["MEGAWATT:MILLIWATT"] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("megaw"))
    CONVERSIONS["MEGAWATT:NANOWATT"] = \
        Mul(Rat(Int(1), Pow(10, 15)), Sym("megaw"))
    CONVERSIONS["MEGAWATT:PETAWATT"] = \
        Mul(Pow(10, 9), Sym("megaw"))
    CONVERSIONS["MEGAWATT:PICOWATT"] = \
        Mul(Rat(Int(1), Pow(10, 18)), Sym("megaw"))
    CONVERSIONS["MEGAWATT:TERAWATT"] = \
        Mul(Pow(10, 6), Sym("megaw"))
    CONVERSIONS["MEGAWATT:WATT"] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("megaw"))
    CONVERSIONS["MEGAWATT:YOCTOWATT"] = \
        Mul(Rat(Int(1), Pow(10, 30)), Sym("megaw"))
    CONVERSIONS["MEGAWATT:YOTTAWATT"] = \
        Mul(Pow(10, 18), Sym("megaw"))
    CONVERSIONS["MEGAWATT:ZEPTOWATT"] = \
        Mul(Rat(Int(1), Pow(10, 27)), Sym("megaw"))
    CONVERSIONS["MEGAWATT:ZETTAWATT"] = \
        Mul(Pow(10, 15), Sym("megaw"))
    CONVERSIONS["MICROWATT:ATTOWATT"] = \
        Mul(Rat(Int(1), Pow(10, 12)), Sym("microw"))
    CONVERSIONS["MICROWATT:CENTIWATT"] = \
        Mul(Pow(10, 4), Sym("microw"))
    CONVERSIONS["MICROWATT:DECAWATT"] = \
        Mul(Pow(10, 7), Sym("microw"))
    CONVERSIONS["MICROWATT:DECIWATT"] = \
        Mul(Pow(10, 5), Sym("microw"))
    CONVERSIONS["MICROWATT:EXAWATT"] = \
        Mul(Pow(10, 24), Sym("microw"))
    CONVERSIONS["MICROWATT:FEMTOWATT"] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("microw"))
    CONVERSIONS["MICROWATT:GIGAWATT"] = \
        Mul(Pow(10, 15), Sym("microw"))
    CONVERSIONS["MICROWATT:HECTOWATT"] = \
        Mul(Pow(10, 8), Sym("microw"))
    CONVERSIONS["MICROWATT:KILOWATT"] = \
        Mul(Pow(10, 9), Sym("microw"))
    CONVERSIONS["MICROWATT:MEGAWATT"] = \
        Mul(Pow(10, 12), Sym("microw"))
    CONVERSIONS["MICROWATT:MILLIWATT"] = \
        Mul(Int(1000), Sym("microw"))
    CONVERSIONS["MICROWATT:NANOWATT"] = \
        Mul(Rat(Int(1), Int(1000)), Sym("microw"))
    CONVERSIONS["MICROWATT:PETAWATT"] = \
        Mul(Pow(10, 21), Sym("microw"))
    CONVERSIONS["MICROWATT:PICOWATT"] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("microw"))
    CONVERSIONS["MICROWATT:TERAWATT"] = \
        Mul(Pow(10, 18), Sym("microw"))
    CONVERSIONS["MICROWATT:WATT"] = \
        Mul(Pow(10, 6), Sym("microw"))
    CONVERSIONS["MICROWATT:YOCTOWATT"] = \
        Mul(Rat(Int(1), Pow(10, 18)), Sym("microw"))
    CONVERSIONS["MICROWATT:YOTTAWATT"] = \
        Mul(Pow(10, 30), Sym("microw"))
    CONVERSIONS["MICROWATT:ZEPTOWATT"] = \
        Mul(Rat(Int(1), Pow(10, 15)), Sym("microw"))
    CONVERSIONS["MICROWATT:ZETTAWATT"] = \
        Mul(Pow(10, 27), Sym("microw"))
    CONVERSIONS["MILLIWATT:ATTOWATT"] = \
        Mul(Rat(Int(1), Pow(10, 15)), Sym("milliw"))
    CONVERSIONS["MILLIWATT:CENTIWATT"] = \
        Mul(Int(10), Sym("milliw"))
    CONVERSIONS["MILLIWATT:DECAWATT"] = \
        Mul(Pow(10, 4), Sym("milliw"))
    CONVERSIONS["MILLIWATT:DECIWATT"] = \
        Mul(Int(100), Sym("milliw"))
    CONVERSIONS["MILLIWATT:EXAWATT"] = \
        Mul(Pow(10, 21), Sym("milliw"))
    CONVERSIONS["MILLIWATT:FEMTOWATT"] = \
        Mul(Rat(Int(1), Pow(10, 12)), Sym("milliw"))
    CONVERSIONS["MILLIWATT:GIGAWATT"] = \
        Mul(Pow(10, 12), Sym("milliw"))
    CONVERSIONS["MILLIWATT:HECTOWATT"] = \
        Mul(Pow(10, 5), Sym("milliw"))
    CONVERSIONS["MILLIWATT:KILOWATT"] = \
        Mul(Pow(10, 6), Sym("milliw"))
    CONVERSIONS["MILLIWATT:MEGAWATT"] = \
        Mul(Pow(10, 9), Sym("milliw"))
    CONVERSIONS["MILLIWATT:MICROWATT"] = \
        Mul(Rat(Int(1), Int(1000)), Sym("milliw"))
    CONVERSIONS["MILLIWATT:NANOWATT"] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("milliw"))
    CONVERSIONS["MILLIWATT:PETAWATT"] = \
        Mul(Pow(10, 18), Sym("milliw"))
    CONVERSIONS["MILLIWATT:PICOWATT"] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("milliw"))
    CONVERSIONS["MILLIWATT:TERAWATT"] = \
        Mul(Pow(10, 15), Sym("milliw"))
    CONVERSIONS["MILLIWATT:WATT"] = \
        Mul(Int(1000), Sym("milliw"))
    CONVERSIONS["MILLIWATT:YOCTOWATT"] = \
        Mul(Rat(Int(1), Pow(10, 21)), Sym("milliw"))
    CONVERSIONS["MILLIWATT:YOTTAWATT"] = \
        Mul(Pow(10, 27), Sym("milliw"))
    CONVERSIONS["MILLIWATT:ZEPTOWATT"] = \
        Mul(Rat(Int(1), Pow(10, 18)), Sym("milliw"))
    CONVERSIONS["MILLIWATT:ZETTAWATT"] = \
        Mul(Pow(10, 24), Sym("milliw"))
    CONVERSIONS["NANOWATT:ATTOWATT"] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("nanow"))
    CONVERSIONS["NANOWATT:CENTIWATT"] = \
        Mul(Pow(10, 7), Sym("nanow"))
    CONVERSIONS["NANOWATT:DECAWATT"] = \
        Mul(Pow(10, 10), Sym("nanow"))
    CONVERSIONS["NANOWATT:DECIWATT"] = \
        Mul(Pow(10, 8), Sym("nanow"))
    CONVERSIONS["NANOWATT:EXAWATT"] = \
        Mul(Pow(10, 27), Sym("nanow"))
    CONVERSIONS["NANOWATT:FEMTOWATT"] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("nanow"))
    CONVERSIONS["NANOWATT:GIGAWATT"] = \
        Mul(Pow(10, 18), Sym("nanow"))
    CONVERSIONS["NANOWATT:HECTOWATT"] = \
        Mul(Pow(10, 11), Sym("nanow"))
    CONVERSIONS["NANOWATT:KILOWATT"] = \
        Mul(Pow(10, 12), Sym("nanow"))
    CONVERSIONS["NANOWATT:MEGAWATT"] = \
        Mul(Pow(10, 15), Sym("nanow"))
    CONVERSIONS["NANOWATT:MICROWATT"] = \
        Mul(Int(1000), Sym("nanow"))
    CONVERSIONS["NANOWATT:MILLIWATT"] = \
        Mul(Pow(10, 6), Sym("nanow"))
    CONVERSIONS["NANOWATT:PETAWATT"] = \
        Mul(Pow(10, 24), Sym("nanow"))
    CONVERSIONS["NANOWATT:PICOWATT"] = \
        Mul(Rat(Int(1), Int(1000)), Sym("nanow"))
    CONVERSIONS["NANOWATT:TERAWATT"] = \
        Mul(Pow(10, 21), Sym("nanow"))
    CONVERSIONS["NANOWATT:WATT"] = \
        Mul(Pow(10, 9), Sym("nanow"))
    CONVERSIONS["NANOWATT:YOCTOWATT"] = \
        Mul(Rat(Int(1), Pow(10, 15)), Sym("nanow"))
    CONVERSIONS["NANOWATT:YOTTAWATT"] = \
        Mul(Pow(10, 33), Sym("nanow"))
    CONVERSIONS["NANOWATT:ZEPTOWATT"] = \
        Mul(Rat(Int(1), Pow(10, 12)), Sym("nanow"))
    CONVERSIONS["NANOWATT:ZETTAWATT"] = \
        Mul(Pow(10, 30), Sym("nanow"))
    CONVERSIONS["PETAWATT:ATTOWATT"] = \
        Mul(Rat(Int(1), Pow(10, 33)), Sym("petaw"))
    CONVERSIONS["PETAWATT:CENTIWATT"] = \
        Mul(Rat(Int(1), Pow(10, 17)), Sym("petaw"))
    CONVERSIONS["PETAWATT:DECAWATT"] = \
        Mul(Rat(Int(1), Pow(10, 14)), Sym("petaw"))
    CONVERSIONS["PETAWATT:DECIWATT"] = \
        Mul(Rat(Int(1), Pow(10, 16)), Sym("petaw"))
    CONVERSIONS["PETAWATT:EXAWATT"] = \
        Mul(Int(1000), Sym("petaw"))
    CONVERSIONS["PETAWATT:FEMTOWATT"] = \
        Mul(Rat(Int(1), Pow(10, 30)), Sym("petaw"))
    CONVERSIONS["PETAWATT:GIGAWATT"] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("petaw"))
    CONVERSIONS["PETAWATT:HECTOWATT"] = \
        Mul(Rat(Int(1), Pow(10, 13)), Sym("petaw"))
    CONVERSIONS["PETAWATT:KILOWATT"] = \
        Mul(Rat(Int(1), Pow(10, 12)), Sym("petaw"))
    CONVERSIONS["PETAWATT:MEGAWATT"] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("petaw"))
    CONVERSIONS["PETAWATT:MICROWATT"] = \
        Mul(Rat(Int(1), Pow(10, 21)), Sym("petaw"))
    CONVERSIONS["PETAWATT:MILLIWATT"] = \
        Mul(Rat(Int(1), Pow(10, 18)), Sym("petaw"))
    CONVERSIONS["PETAWATT:NANOWATT"] = \
        Mul(Rat(Int(1), Pow(10, 24)), Sym("petaw"))
    CONVERSIONS["PETAWATT:PICOWATT"] = \
        Mul(Rat(Int(1), Pow(10, 27)), Sym("petaw"))
    CONVERSIONS["PETAWATT:TERAWATT"] = \
        Mul(Rat(Int(1), Int(1000)), Sym("petaw"))
    CONVERSIONS["PETAWATT:WATT"] = \
        Mul(Rat(Int(1), Pow(10, 15)), Sym("petaw"))
    CONVERSIONS["PETAWATT:YOCTOWATT"] = \
        Mul(Rat(Int(1), Pow(10, 39)), Sym("petaw"))
    CONVERSIONS["PETAWATT:YOTTAWATT"] = \
        Mul(Pow(10, 9), Sym("petaw"))
    CONVERSIONS["PETAWATT:ZEPTOWATT"] = \
        Mul(Rat(Int(1), Pow(10, 36)), Sym("petaw"))
    CONVERSIONS["PETAWATT:ZETTAWATT"] = \
        Mul(Pow(10, 6), Sym("petaw"))
    CONVERSIONS["PICOWATT:ATTOWATT"] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("picow"))
    CONVERSIONS["PICOWATT:CENTIWATT"] = \
        Mul(Pow(10, 10), Sym("picow"))
    CONVERSIONS["PICOWATT:DECAWATT"] = \
        Mul(Pow(10, 13), Sym("picow"))
    CONVERSIONS["PICOWATT:DECIWATT"] = \
        Mul(Pow(10, 11), Sym("picow"))
    CONVERSIONS["PICOWATT:EXAWATT"] = \
        Mul(Pow(10, 30), Sym("picow"))
    CONVERSIONS["PICOWATT:FEMTOWATT"] = \
        Mul(Rat(Int(1), Int(1000)), Sym("picow"))
    CONVERSIONS["PICOWATT:GIGAWATT"] = \
        Mul(Pow(10, 21), Sym("picow"))
    CONVERSIONS["PICOWATT:HECTOWATT"] = \
        Mul(Pow(10, 14), Sym("picow"))
    CONVERSIONS["PICOWATT:KILOWATT"] = \
        Mul(Pow(10, 15), Sym("picow"))
    CONVERSIONS["PICOWATT:MEGAWATT"] = \
        Mul(Pow(10, 18), Sym("picow"))
    CONVERSIONS["PICOWATT:MICROWATT"] = \
        Mul(Pow(10, 6), Sym("picow"))
    CONVERSIONS["PICOWATT:MILLIWATT"] = \
        Mul(Pow(10, 9), Sym("picow"))
    CONVERSIONS["PICOWATT:NANOWATT"] = \
        Mul(Int(1000), Sym("picow"))
    CONVERSIONS["PICOWATT:PETAWATT"] = \
        Mul(Pow(10, 27), Sym("picow"))
    CONVERSIONS["PICOWATT:TERAWATT"] = \
        Mul(Pow(10, 24), Sym("picow"))
    CONVERSIONS["PICOWATT:WATT"] = \
        Mul(Pow(10, 12), Sym("picow"))
    CONVERSIONS["PICOWATT:YOCTOWATT"] = \
        Mul(Rat(Int(1), Pow(10, 12)), Sym("picow"))
    CONVERSIONS["PICOWATT:YOTTAWATT"] = \
        Mul(Pow(10, 36), Sym("picow"))
    CONVERSIONS["PICOWATT:ZEPTOWATT"] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("picow"))
    CONVERSIONS["PICOWATT:ZETTAWATT"] = \
        Mul(Pow(10, 33), Sym("picow"))
    CONVERSIONS["TERAWATT:ATTOWATT"] = \
        Mul(Rat(Int(1), Pow(10, 30)), Sym("teraw"))
    CONVERSIONS["TERAWATT:CENTIWATT"] = \
        Mul(Rat(Int(1), Pow(10, 14)), Sym("teraw"))
    CONVERSIONS["TERAWATT:DECAWATT"] = \
        Mul(Rat(Int(1), Pow(10, 11)), Sym("teraw"))
    CONVERSIONS["TERAWATT:DECIWATT"] = \
        Mul(Rat(Int(1), Pow(10, 13)), Sym("teraw"))
    CONVERSIONS["TERAWATT:EXAWATT"] = \
        Mul(Pow(10, 6), Sym("teraw"))
    CONVERSIONS["TERAWATT:FEMTOWATT"] = \
        Mul(Rat(Int(1), Pow(10, 27)), Sym("teraw"))
    CONVERSIONS["TERAWATT:GIGAWATT"] = \
        Mul(Rat(Int(1), Int(1000)), Sym("teraw"))
    CONVERSIONS["TERAWATT:HECTOWATT"] = \
        Mul(Rat(Int(1), Pow(10, 10)), Sym("teraw"))
    CONVERSIONS["TERAWATT:KILOWATT"] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("teraw"))
    CONVERSIONS["TERAWATT:MEGAWATT"] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("teraw"))
    CONVERSIONS["TERAWATT:MICROWATT"] = \
        Mul(Rat(Int(1), Pow(10, 18)), Sym("teraw"))
    CONVERSIONS["TERAWATT:MILLIWATT"] = \
        Mul(Rat(Int(1), Pow(10, 15)), Sym("teraw"))
    CONVERSIONS["TERAWATT:NANOWATT"] = \
        Mul(Rat(Int(1), Pow(10, 21)), Sym("teraw"))
    CONVERSIONS["TERAWATT:PETAWATT"] = \
        Mul(Int(1000), Sym("teraw"))
    CONVERSIONS["TERAWATT:PICOWATT"] = \
        Mul(Rat(Int(1), Pow(10, 24)), Sym("teraw"))
    CONVERSIONS["TERAWATT:WATT"] = \
        Mul(Rat(Int(1), Pow(10, 12)), Sym("teraw"))
    CONVERSIONS["TERAWATT:YOCTOWATT"] = \
        Mul(Rat(Int(1), Pow(10, 36)), Sym("teraw"))
    CONVERSIONS["TERAWATT:YOTTAWATT"] = \
        Mul(Pow(10, 12), Sym("teraw"))
    CONVERSIONS["TERAWATT:ZEPTOWATT"] = \
        Mul(Rat(Int(1), Pow(10, 33)), Sym("teraw"))
    CONVERSIONS["TERAWATT:ZETTAWATT"] = \
        Mul(Pow(10, 9), Sym("teraw"))
    CONVERSIONS["WATT:ATTOWATT"] = \
        Mul(Rat(Int(1), Pow(10, 18)), Sym("w"))
    CONVERSIONS["WATT:CENTIWATT"] = \
        Mul(Rat(Int(1), Int(100)), Sym("w"))
    CONVERSIONS["WATT:DECAWATT"] = \
        Mul(Int(10), Sym("w"))
    CONVERSIONS["WATT:DECIWATT"] = \
        Mul(Rat(Int(1), Int(10)), Sym("w"))
    CONVERSIONS["WATT:EXAWATT"] = \
        Mul(Pow(10, 18), Sym("w"))
    CONVERSIONS["WATT:FEMTOWATT"] = \
        Mul(Rat(Int(1), Pow(10, 15)), Sym("w"))
    CONVERSIONS["WATT:GIGAWATT"] = \
        Mul(Pow(10, 9), Sym("w"))
    CONVERSIONS["WATT:HECTOWATT"] = \
        Mul(Int(100), Sym("w"))
    CONVERSIONS["WATT:KILOWATT"] = \
        Mul(Int(1000), Sym("w"))
    CONVERSIONS["WATT:MEGAWATT"] = \
        Mul(Pow(10, 6), Sym("w"))
    CONVERSIONS["WATT:MICROWATT"] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("w"))
    CONVERSIONS["WATT:MILLIWATT"] = \
        Mul(Rat(Int(1), Int(1000)), Sym("w"))
    CONVERSIONS["WATT:NANOWATT"] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("w"))
    CONVERSIONS["WATT:PETAWATT"] = \
        Mul(Pow(10, 15), Sym("w"))
    CONVERSIONS["WATT:PICOWATT"] = \
        Mul(Rat(Int(1), Pow(10, 12)), Sym("w"))
    CONVERSIONS["WATT:TERAWATT"] = \
        Mul(Pow(10, 12), Sym("w"))
    CONVERSIONS["WATT:YOCTOWATT"] = \
        Mul(Rat(Int(1), Pow(10, 24)), Sym("w"))
    CONVERSIONS["WATT:YOTTAWATT"] = \
        Mul(Pow(10, 24), Sym("w"))
    CONVERSIONS["WATT:ZEPTOWATT"] = \
        Mul(Rat(Int(1), Pow(10, 21)), Sym("w"))
    CONVERSIONS["WATT:ZETTAWATT"] = \
        Mul(Pow(10, 21), Sym("w"))
    CONVERSIONS["YOCTOWATT:ATTOWATT"] = \
        Mul(Pow(10, 6), Sym("yoctow"))
    CONVERSIONS["YOCTOWATT:CENTIWATT"] = \
        Mul(Pow(10, 22), Sym("yoctow"))
    CONVERSIONS["YOCTOWATT:DECAWATT"] = \
        Mul(Pow(10, 25), Sym("yoctow"))
    CONVERSIONS["YOCTOWATT:DECIWATT"] = \
        Mul(Pow(10, 23), Sym("yoctow"))
    CONVERSIONS["YOCTOWATT:EXAWATT"] = \
        Mul(Pow(10, 42), Sym("yoctow"))
    CONVERSIONS["YOCTOWATT:FEMTOWATT"] = \
        Mul(Pow(10, 9), Sym("yoctow"))
    CONVERSIONS["YOCTOWATT:GIGAWATT"] = \
        Mul(Pow(10, 33), Sym("yoctow"))
    CONVERSIONS["YOCTOWATT:HECTOWATT"] = \
        Mul(Pow(10, 26), Sym("yoctow"))
    CONVERSIONS["YOCTOWATT:KILOWATT"] = \
        Mul(Pow(10, 27), Sym("yoctow"))
    CONVERSIONS["YOCTOWATT:MEGAWATT"] = \
        Mul(Pow(10, 30), Sym("yoctow"))
    CONVERSIONS["YOCTOWATT:MICROWATT"] = \
        Mul(Pow(10, 18), Sym("yoctow"))
    CONVERSIONS["YOCTOWATT:MILLIWATT"] = \
        Mul(Pow(10, 21), Sym("yoctow"))
    CONVERSIONS["YOCTOWATT:NANOWATT"] = \
        Mul(Pow(10, 15), Sym("yoctow"))
    CONVERSIONS["YOCTOWATT:PETAWATT"] = \
        Mul(Pow(10, 39), Sym("yoctow"))
    CONVERSIONS["YOCTOWATT:PICOWATT"] = \
        Mul(Pow(10, 12), Sym("yoctow"))
    CONVERSIONS["YOCTOWATT:TERAWATT"] = \
        Mul(Pow(10, 36), Sym("yoctow"))
    CONVERSIONS["YOCTOWATT:WATT"] = \
        Mul(Pow(10, 24), Sym("yoctow"))
    CONVERSIONS["YOCTOWATT:YOTTAWATT"] = \
        Mul(Pow(10, 48), Sym("yoctow"))
    CONVERSIONS["YOCTOWATT:ZEPTOWATT"] = \
        Mul(Int(1000), Sym("yoctow"))
    CONVERSIONS["YOCTOWATT:ZETTAWATT"] = \
        Mul(Pow(10, 45), Sym("yoctow"))
    CONVERSIONS["YOTTAWATT:ATTOWATT"] = \
        Mul(Rat(Int(1), Pow(10, 42)), Sym("yottaw"))
    CONVERSIONS["YOTTAWATT:CENTIWATT"] = \
        Mul(Rat(Int(1), Pow(10, 26)), Sym("yottaw"))
    CONVERSIONS["YOTTAWATT:DECAWATT"] = \
        Mul(Rat(Int(1), Pow(10, 23)), Sym("yottaw"))
    CONVERSIONS["YOTTAWATT:DECIWATT"] = \
        Mul(Rat(Int(1), Pow(10, 25)), Sym("yottaw"))
    CONVERSIONS["YOTTAWATT:EXAWATT"] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("yottaw"))
    CONVERSIONS["YOTTAWATT:FEMTOWATT"] = \
        Mul(Rat(Int(1), Pow(10, 39)), Sym("yottaw"))
    CONVERSIONS["YOTTAWATT:GIGAWATT"] = \
        Mul(Rat(Int(1), Pow(10, 15)), Sym("yottaw"))
    CONVERSIONS["YOTTAWATT:HECTOWATT"] = \
        Mul(Rat(Int(1), Pow(10, 22)), Sym("yottaw"))
    CONVERSIONS["YOTTAWATT:KILOWATT"] = \
        Mul(Rat(Int(1), Pow(10, 21)), Sym("yottaw"))
    CONVERSIONS["YOTTAWATT:MEGAWATT"] = \
        Mul(Rat(Int(1), Pow(10, 18)), Sym("yottaw"))
    CONVERSIONS["YOTTAWATT:MICROWATT"] = \
        Mul(Rat(Int(1), Pow(10, 30)), Sym("yottaw"))
    CONVERSIONS["YOTTAWATT:MILLIWATT"] = \
        Mul(Rat(Int(1), Pow(10, 27)), Sym("yottaw"))
    CONVERSIONS["YOTTAWATT:NANOWATT"] = \
        Mul(Rat(Int(1), Pow(10, 33)), Sym("yottaw"))
    CONVERSIONS["YOTTAWATT:PETAWATT"] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("yottaw"))
    CONVERSIONS["YOTTAWATT:PICOWATT"] = \
        Mul(Rat(Int(1), Pow(10, 36)), Sym("yottaw"))
    CONVERSIONS["YOTTAWATT:TERAWATT"] = \
        Mul(Rat(Int(1), Pow(10, 12)), Sym("yottaw"))
    CONVERSIONS["YOTTAWATT:WATT"] = \
        Mul(Rat(Int(1), Pow(10, 24)), Sym("yottaw"))
    CONVERSIONS["YOTTAWATT:YOCTOWATT"] = \
        Mul(Rat(Int(1), Pow(10, 48)), Sym("yottaw"))
    CONVERSIONS["YOTTAWATT:ZEPTOWATT"] = \
        Mul(Rat(Int(1), Pow(10, 45)), Sym("yottaw"))
    CONVERSIONS["YOTTAWATT:ZETTAWATT"] = \
        Mul(Rat(Int(1), Int(1000)), Sym("yottaw"))
    CONVERSIONS["ZEPTOWATT:ATTOWATT"] = \
        Mul(Int(1000), Sym("zeptow"))
    CONVERSIONS["ZEPTOWATT:CENTIWATT"] = \
        Mul(Pow(10, 19), Sym("zeptow"))
    CONVERSIONS["ZEPTOWATT:DECAWATT"] = \
        Mul(Pow(10, 22), Sym("zeptow"))
    CONVERSIONS["ZEPTOWATT:DECIWATT"] = \
        Mul(Pow(10, 20), Sym("zeptow"))
    CONVERSIONS["ZEPTOWATT:EXAWATT"] = \
        Mul(Pow(10, 39), Sym("zeptow"))
    CONVERSIONS["ZEPTOWATT:FEMTOWATT"] = \
        Mul(Pow(10, 6), Sym("zeptow"))
    CONVERSIONS["ZEPTOWATT:GIGAWATT"] = \
        Mul(Pow(10, 30), Sym("zeptow"))
    CONVERSIONS["ZEPTOWATT:HECTOWATT"] = \
        Mul(Pow(10, 23), Sym("zeptow"))
    CONVERSIONS["ZEPTOWATT:KILOWATT"] = \
        Mul(Pow(10, 24), Sym("zeptow"))
    CONVERSIONS["ZEPTOWATT:MEGAWATT"] = \
        Mul(Pow(10, 27), Sym("zeptow"))
    CONVERSIONS["ZEPTOWATT:MICROWATT"] = \
        Mul(Pow(10, 15), Sym("zeptow"))
    CONVERSIONS["ZEPTOWATT:MILLIWATT"] = \
        Mul(Pow(10, 18), Sym("zeptow"))
    CONVERSIONS["ZEPTOWATT:NANOWATT"] = \
        Mul(Pow(10, 12), Sym("zeptow"))
    CONVERSIONS["ZEPTOWATT:PETAWATT"] = \
        Mul(Pow(10, 36), Sym("zeptow"))
    CONVERSIONS["ZEPTOWATT:PICOWATT"] = \
        Mul(Pow(10, 9), Sym("zeptow"))
    CONVERSIONS["ZEPTOWATT:TERAWATT"] = \
        Mul(Pow(10, 33), Sym("zeptow"))
    CONVERSIONS["ZEPTOWATT:WATT"] = \
        Mul(Pow(10, 21), Sym("zeptow"))
    CONVERSIONS["ZEPTOWATT:YOCTOWATT"] = \
        Mul(Rat(Int(1), Int(1000)), Sym("zeptow"))
    CONVERSIONS["ZEPTOWATT:YOTTAWATT"] = \
        Mul(Pow(10, 45), Sym("zeptow"))
    CONVERSIONS["ZEPTOWATT:ZETTAWATT"] = \
        Mul(Pow(10, 42), Sym("zeptow"))
    CONVERSIONS["ZETTAWATT:ATTOWATT"] = \
        Mul(Rat(Int(1), Pow(10, 39)), Sym("zettaw"))
    CONVERSIONS["ZETTAWATT:CENTIWATT"] = \
        Mul(Rat(Int(1), Pow(10, 23)), Sym("zettaw"))
    CONVERSIONS["ZETTAWATT:DECAWATT"] = \
        Mul(Rat(Int(1), Pow(10, 20)), Sym("zettaw"))
    CONVERSIONS["ZETTAWATT:DECIWATT"] = \
        Mul(Rat(Int(1), Pow(10, 22)), Sym("zettaw"))
    CONVERSIONS["ZETTAWATT:EXAWATT"] = \
        Mul(Rat(Int(1), Int(1000)), Sym("zettaw"))
    CONVERSIONS["ZETTAWATT:FEMTOWATT"] = \
        Mul(Rat(Int(1), Pow(10, 36)), Sym("zettaw"))
    CONVERSIONS["ZETTAWATT:GIGAWATT"] = \
        Mul(Rat(Int(1), Pow(10, 12)), Sym("zettaw"))
    CONVERSIONS["ZETTAWATT:HECTOWATT"] = \
        Mul(Rat(Int(1), Pow(10, 19)), Sym("zettaw"))
    CONVERSIONS["ZETTAWATT:KILOWATT"] = \
        Mul(Rat(Int(1), Pow(10, 18)), Sym("zettaw"))
    CONVERSIONS["ZETTAWATT:MEGAWATT"] = \
        Mul(Rat(Int(1), Pow(10, 15)), Sym("zettaw"))
    CONVERSIONS["ZETTAWATT:MICROWATT"] = \
        Mul(Rat(Int(1), Pow(10, 27)), Sym("zettaw"))
    CONVERSIONS["ZETTAWATT:MILLIWATT"] = \
        Mul(Rat(Int(1), Pow(10, 24)), Sym("zettaw"))
    CONVERSIONS["ZETTAWATT:NANOWATT"] = \
        Mul(Rat(Int(1), Pow(10, 30)), Sym("zettaw"))
    CONVERSIONS["ZETTAWATT:PETAWATT"] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("zettaw"))
    CONVERSIONS["ZETTAWATT:PICOWATT"] = \
        Mul(Rat(Int(1), Pow(10, 33)), Sym("zettaw"))
    CONVERSIONS["ZETTAWATT:TERAWATT"] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("zettaw"))
    CONVERSIONS["ZETTAWATT:WATT"] = \
        Mul(Rat(Int(1), Pow(10, 21)), Sym("zettaw"))
    CONVERSIONS["ZETTAWATT:YOCTOWATT"] = \
        Mul(Rat(Int(1), Pow(10, 45)), Sym("zettaw"))
    CONVERSIONS["ZETTAWATT:YOTTAWATT"] = \
        Mul(Int(1000), Sym("zettaw"))
    CONVERSIONS["ZETTAWATT:ZEPTOWATT"] = \
        Mul(Rat(Int(1), Pow(10, 42)), Sym("zettaw"))

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
