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
Code-generated omero.model.Time implementation,
based on omero.model.PermissionsI
"""


import Ice
import IceImport
IceImport.load("omero_model_Time_ice")
_omero = Ice.openModule("omero")
_omero_model = Ice.openModule("omero.model")
__name__ = "omero.model"

from omero_model_UnitBase import UnitBase
from omero.model.enums import UnitsTime

from omero.model.conversions import Add
from omero.model.conversions import Int
from omero.model.conversions import Mul
from omero.model.conversions import Pow
from omero.model.conversions import Rat
from omero.model.conversions import Sym


class TimeI(_omero_model.Time, UnitBase):

    CONVERSIONS = dict()
    CONVERSIONS["ATTOSECOND:CENTISECOND"] = \
        Mul(Pow(10, 16), Sym("attos"))
    CONVERSIONS["ATTOSECOND:DAY"] = \
        Mul(Mul(Int(864), Pow(10, 20)), Sym("attos"))
    CONVERSIONS["ATTOSECOND:DECASECOND"] = \
        Mul(Pow(10, 19), Sym("attos"))
    CONVERSIONS["ATTOSECOND:DECISECOND"] = \
        Mul(Pow(10, 17), Sym("attos"))
    CONVERSIONS["ATTOSECOND:EXASECOND"] = \
        Mul(Pow(10, 36), Sym("attos"))
    CONVERSIONS["ATTOSECOND:FEMTOSECOND"] = \
        Mul(Int(1000), Sym("attos"))
    CONVERSIONS["ATTOSECOND:GIGASECOND"] = \
        Mul(Pow(10, 27), Sym("attos"))
    CONVERSIONS["ATTOSECOND:HECTOSECOND"] = \
        Mul(Pow(10, 20), Sym("attos"))
    CONVERSIONS["ATTOSECOND:HOUR"] = \
        Mul(Mul(Int(36), Pow(10, 20)), Sym("attos"))
    CONVERSIONS["ATTOSECOND:KILOSECOND"] = \
        Mul(Pow(10, 21), Sym("attos"))
    CONVERSIONS["ATTOSECOND:MEGASECOND"] = \
        Mul(Pow(10, 24), Sym("attos"))
    CONVERSIONS["ATTOSECOND:MICROSECOND"] = \
        Mul(Pow(10, 12), Sym("attos"))
    CONVERSIONS["ATTOSECOND:MILLISECOND"] = \
        Mul(Pow(10, 15), Sym("attos"))
    CONVERSIONS["ATTOSECOND:MINUTE"] = \
        Mul(Mul(Int(6), Pow(10, 19)), Sym("attos"))
    CONVERSIONS["ATTOSECOND:NANOSECOND"] = \
        Mul(Pow(10, 9), Sym("attos"))
    CONVERSIONS["ATTOSECOND:PETASECOND"] = \
        Mul(Pow(10, 33), Sym("attos"))
    CONVERSIONS["ATTOSECOND:PICOSECOND"] = \
        Mul(Pow(10, 6), Sym("attos"))
    CONVERSIONS["ATTOSECOND:SECOND"] = \
        Mul(Pow(10, 18), Sym("attos"))
    CONVERSIONS["ATTOSECOND:TERASECOND"] = \
        Mul(Pow(10, 30), Sym("attos"))
    CONVERSIONS["ATTOSECOND:YOCTOSECOND"] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("attos"))
    CONVERSIONS["ATTOSECOND:YOTTASECOND"] = \
        Mul(Pow(10, 42), Sym("attos"))
    CONVERSIONS["ATTOSECOND:ZEPTOSECOND"] = \
        Mul(Rat(Int(1), Int(1000)), Sym("attos"))
    CONVERSIONS["ATTOSECOND:ZETTASECOND"] = \
        Mul(Pow(10, 39), Sym("attos"))
    CONVERSIONS["CENTISECOND:ATTOSECOND"] = \
        Mul(Rat(Int(1), Pow(10, 16)), Sym("centis"))
    CONVERSIONS["CENTISECOND:DAY"] = \
        Mul(Mul(Int(864), Pow(10, 4)), Sym("centis"))
    CONVERSIONS["CENTISECOND:DECASECOND"] = \
        Mul(Int(1000), Sym("centis"))
    CONVERSIONS["CENTISECOND:DECISECOND"] = \
        Mul(Int(10), Sym("centis"))
    CONVERSIONS["CENTISECOND:EXASECOND"] = \
        Mul(Pow(10, 20), Sym("centis"))
    CONVERSIONS["CENTISECOND:FEMTOSECOND"] = \
        Mul(Rat(Int(1), Pow(10, 13)), Sym("centis"))
    CONVERSIONS["CENTISECOND:GIGASECOND"] = \
        Mul(Pow(10, 11), Sym("centis"))
    CONVERSIONS["CENTISECOND:HECTOSECOND"] = \
        Mul(Pow(10, 4), Sym("centis"))
    CONVERSIONS["CENTISECOND:HOUR"] = \
        Mul(Mul(Int(36), Pow(10, 4)), Sym("centis"))
    CONVERSIONS["CENTISECOND:KILOSECOND"] = \
        Mul(Pow(10, 5), Sym("centis"))
    CONVERSIONS["CENTISECOND:MEGASECOND"] = \
        Mul(Pow(10, 8), Sym("centis"))
    CONVERSIONS["CENTISECOND:MICROSECOND"] = \
        Mul(Rat(Int(1), Pow(10, 4)), Sym("centis"))
    CONVERSIONS["CENTISECOND:MILLISECOND"] = \
        Mul(Rat(Int(1), Int(10)), Sym("centis"))
    CONVERSIONS["CENTISECOND:MINUTE"] = \
        Mul(Int(6000), Sym("centis"))
    CONVERSIONS["CENTISECOND:NANOSECOND"] = \
        Mul(Rat(Int(1), Pow(10, 7)), Sym("centis"))
    CONVERSIONS["CENTISECOND:PETASECOND"] = \
        Mul(Pow(10, 17), Sym("centis"))
    CONVERSIONS["CENTISECOND:PICOSECOND"] = \
        Mul(Rat(Int(1), Pow(10, 10)), Sym("centis"))
    CONVERSIONS["CENTISECOND:SECOND"] = \
        Mul(Int(100), Sym("centis"))
    CONVERSIONS["CENTISECOND:TERASECOND"] = \
        Mul(Pow(10, 14), Sym("centis"))
    CONVERSIONS["CENTISECOND:YOCTOSECOND"] = \
        Mul(Rat(Int(1), Pow(10, 22)), Sym("centis"))
    CONVERSIONS["CENTISECOND:YOTTASECOND"] = \
        Mul(Pow(10, 26), Sym("centis"))
    CONVERSIONS["CENTISECOND:ZEPTOSECOND"] = \
        Mul(Rat(Int(1), Pow(10, 19)), Sym("centis"))
    CONVERSIONS["CENTISECOND:ZETTASECOND"] = \
        Mul(Pow(10, 23), Sym("centis"))
    CONVERSIONS["DAY:ATTOSECOND"] = \
        Mul(Rat(Int(1), Mul(Int(864), Pow(10, 20))), Sym("d"))
    CONVERSIONS["DAY:CENTISECOND"] = \
        Mul(Rat(Int(1), Mul(Int(864), Pow(10, 4))), Sym("d"))
    CONVERSIONS["DAY:DECASECOND"] = \
        Mul(Rat(Int(1), Int(8640)), Sym("d"))
    CONVERSIONS["DAY:DECISECOND"] = \
        Mul(Rat(Int(1), Int(864000)), Sym("d"))
    CONVERSIONS["DAY:EXASECOND"] = \
        Mul(Rat(Mul(Int(3125), Pow(10, 11)), Int(27)), Sym("d"))
    CONVERSIONS["DAY:FEMTOSECOND"] = \
        Mul(Rat(Int(1), Mul(Int(864), Pow(10, 17))), Sym("d"))
    CONVERSIONS["DAY:GIGASECOND"] = \
        Mul(Rat(Int(312500), Int(27)), Sym("d"))
    CONVERSIONS["DAY:HECTOSECOND"] = \
        Mul(Rat(Int(1), Int(864)), Sym("d"))
    CONVERSIONS["DAY:HOUR"] = \
        Mul(Rat(Int(1), Int(24)), Sym("d"))
    CONVERSIONS["DAY:KILOSECOND"] = \
        Mul(Rat(Int(5), Int(432)), Sym("d"))
    CONVERSIONS["DAY:MEGASECOND"] = \
        Mul(Rat(Int(625), Int(54)), Sym("d"))
    CONVERSIONS["DAY:MICROSECOND"] = \
        Mul(Rat(Int(1), Mul(Int(864), Pow(10, 8))), Sym("d"))
    CONVERSIONS["DAY:MILLISECOND"] = \
        Mul(Rat(Int(1), Mul(Int(864), Pow(10, 5))), Sym("d"))
    CONVERSIONS["DAY:MINUTE"] = \
        Mul(Rat(Int(1), Int(1440)), Sym("d"))
    CONVERSIONS["DAY:NANOSECOND"] = \
        Mul(Rat(Int(1), Mul(Int(864), Pow(10, 11))), Sym("d"))
    CONVERSIONS["DAY:PETASECOND"] = \
        Mul(Rat(Mul(Int(3125), Pow(10, 8)), Int(27)), Sym("d"))
    CONVERSIONS["DAY:PICOSECOND"] = \
        Mul(Rat(Int(1), Mul(Int(864), Pow(10, 14))), Sym("d"))
    CONVERSIONS["DAY:SECOND"] = \
        Mul(Rat(Int(1), Int(86400)), Sym("d"))
    CONVERSIONS["DAY:TERASECOND"] = \
        Mul(Rat(Mul(Int(3125), Pow(10, 5)), Int(27)), Sym("d"))
    CONVERSIONS["DAY:YOCTOSECOND"] = \
        Mul(Rat(Int(1), Mul(Int(864), Pow(10, 26))), Sym("d"))
    CONVERSIONS["DAY:YOTTASECOND"] = \
        Mul(Rat(Mul(Int(3125), Pow(10, 17)), Int(27)), Sym("d"))
    CONVERSIONS["DAY:ZEPTOSECOND"] = \
        Mul(Rat(Int(1), Mul(Int(864), Pow(10, 23))), Sym("d"))
    CONVERSIONS["DAY:ZETTASECOND"] = \
        Mul(Rat(Mul(Int(3125), Pow(10, 14)), Int(27)), Sym("d"))
    CONVERSIONS["DECASECOND:ATTOSECOND"] = \
        Mul(Rat(Int(1), Pow(10, 19)), Sym("decas"))
    CONVERSIONS["DECASECOND:CENTISECOND"] = \
        Mul(Rat(Int(1), Int(1000)), Sym("decas"))
    CONVERSIONS["DECASECOND:DAY"] = \
        Mul(Int(8640), Sym("decas"))
    CONVERSIONS["DECASECOND:DECISECOND"] = \
        Mul(Rat(Int(1), Int(100)), Sym("decas"))
    CONVERSIONS["DECASECOND:EXASECOND"] = \
        Mul(Pow(10, 17), Sym("decas"))
    CONVERSIONS["DECASECOND:FEMTOSECOND"] = \
        Mul(Rat(Int(1), Pow(10, 16)), Sym("decas"))
    CONVERSIONS["DECASECOND:GIGASECOND"] = \
        Mul(Pow(10, 8), Sym("decas"))
    CONVERSIONS["DECASECOND:HECTOSECOND"] = \
        Mul(Int(10), Sym("decas"))
    CONVERSIONS["DECASECOND:HOUR"] = \
        Mul(Int(360), Sym("decas"))
    CONVERSIONS["DECASECOND:KILOSECOND"] = \
        Mul(Int(100), Sym("decas"))
    CONVERSIONS["DECASECOND:MEGASECOND"] = \
        Mul(Pow(10, 5), Sym("decas"))
    CONVERSIONS["DECASECOND:MICROSECOND"] = \
        Mul(Rat(Int(1), Pow(10, 7)), Sym("decas"))
    CONVERSIONS["DECASECOND:MILLISECOND"] = \
        Mul(Rat(Int(1), Pow(10, 4)), Sym("decas"))
    CONVERSIONS["DECASECOND:MINUTE"] = \
        Mul(Int(6), Sym("decas"))
    CONVERSIONS["DECASECOND:NANOSECOND"] = \
        Mul(Rat(Int(1), Pow(10, 10)), Sym("decas"))
    CONVERSIONS["DECASECOND:PETASECOND"] = \
        Mul(Pow(10, 14), Sym("decas"))
    CONVERSIONS["DECASECOND:PICOSECOND"] = \
        Mul(Rat(Int(1), Pow(10, 13)), Sym("decas"))
    CONVERSIONS["DECASECOND:SECOND"] = \
        Mul(Rat(Int(1), Int(10)), Sym("decas"))
    CONVERSIONS["DECASECOND:TERASECOND"] = \
        Mul(Pow(10, 11), Sym("decas"))
    CONVERSIONS["DECASECOND:YOCTOSECOND"] = \
        Mul(Rat(Int(1), Pow(10, 25)), Sym("decas"))
    CONVERSIONS["DECASECOND:YOTTASECOND"] = \
        Mul(Pow(10, 23), Sym("decas"))
    CONVERSIONS["DECASECOND:ZEPTOSECOND"] = \
        Mul(Rat(Int(1), Pow(10, 22)), Sym("decas"))
    CONVERSIONS["DECASECOND:ZETTASECOND"] = \
        Mul(Pow(10, 20), Sym("decas"))
    CONVERSIONS["DECISECOND:ATTOSECOND"] = \
        Mul(Rat(Int(1), Pow(10, 17)), Sym("decis"))
    CONVERSIONS["DECISECOND:CENTISECOND"] = \
        Mul(Rat(Int(1), Int(10)), Sym("decis"))
    CONVERSIONS["DECISECOND:DAY"] = \
        Mul(Int(864000), Sym("decis"))
    CONVERSIONS["DECISECOND:DECASECOND"] = \
        Mul(Int(100), Sym("decis"))
    CONVERSIONS["DECISECOND:EXASECOND"] = \
        Mul(Pow(10, 19), Sym("decis"))
    CONVERSIONS["DECISECOND:FEMTOSECOND"] = \
        Mul(Rat(Int(1), Pow(10, 14)), Sym("decis"))
    CONVERSIONS["DECISECOND:GIGASECOND"] = \
        Mul(Pow(10, 10), Sym("decis"))
    CONVERSIONS["DECISECOND:HECTOSECOND"] = \
        Mul(Int(1000), Sym("decis"))
    CONVERSIONS["DECISECOND:HOUR"] = \
        Mul(Int(36000), Sym("decis"))
    CONVERSIONS["DECISECOND:KILOSECOND"] = \
        Mul(Pow(10, 4), Sym("decis"))
    CONVERSIONS["DECISECOND:MEGASECOND"] = \
        Mul(Pow(10, 7), Sym("decis"))
    CONVERSIONS["DECISECOND:MICROSECOND"] = \
        Mul(Rat(Int(1), Pow(10, 5)), Sym("decis"))
    CONVERSIONS["DECISECOND:MILLISECOND"] = \
        Mul(Rat(Int(1), Int(100)), Sym("decis"))
    CONVERSIONS["DECISECOND:MINUTE"] = \
        Mul(Int(600), Sym("decis"))
    CONVERSIONS["DECISECOND:NANOSECOND"] = \
        Mul(Rat(Int(1), Pow(10, 8)), Sym("decis"))
    CONVERSIONS["DECISECOND:PETASECOND"] = \
        Mul(Pow(10, 16), Sym("decis"))
    CONVERSIONS["DECISECOND:PICOSECOND"] = \
        Mul(Rat(Int(1), Pow(10, 11)), Sym("decis"))
    CONVERSIONS["DECISECOND:SECOND"] = \
        Mul(Int(10), Sym("decis"))
    CONVERSIONS["DECISECOND:TERASECOND"] = \
        Mul(Pow(10, 13), Sym("decis"))
    CONVERSIONS["DECISECOND:YOCTOSECOND"] = \
        Mul(Rat(Int(1), Pow(10, 23)), Sym("decis"))
    CONVERSIONS["DECISECOND:YOTTASECOND"] = \
        Mul(Pow(10, 25), Sym("decis"))
    CONVERSIONS["DECISECOND:ZEPTOSECOND"] = \
        Mul(Rat(Int(1), Pow(10, 20)), Sym("decis"))
    CONVERSIONS["DECISECOND:ZETTASECOND"] = \
        Mul(Pow(10, 22), Sym("decis"))
    CONVERSIONS["EXASECOND:ATTOSECOND"] = \
        Mul(Rat(Int(1), Pow(10, 36)), Sym("exas"))
    CONVERSIONS["EXASECOND:CENTISECOND"] = \
        Mul(Rat(Int(1), Pow(10, 20)), Sym("exas"))
    CONVERSIONS["EXASECOND:DAY"] = \
        Mul(Rat(Int(27), Mul(Int(3125), Pow(10, 11))), Sym("exas"))
    CONVERSIONS["EXASECOND:DECASECOND"] = \
        Mul(Rat(Int(1), Pow(10, 17)), Sym("exas"))
    CONVERSIONS["EXASECOND:DECISECOND"] = \
        Mul(Rat(Int(1), Pow(10, 19)), Sym("exas"))
    CONVERSIONS["EXASECOND:FEMTOSECOND"] = \
        Mul(Rat(Int(1), Pow(10, 33)), Sym("exas"))
    CONVERSIONS["EXASECOND:GIGASECOND"] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("exas"))
    CONVERSIONS["EXASECOND:HECTOSECOND"] = \
        Mul(Rat(Int(1), Pow(10, 16)), Sym("exas"))
    CONVERSIONS["EXASECOND:HOUR"] = \
        Mul(Rat(Int(9), Mul(Int(25), Pow(10, 14))), Sym("exas"))
    CONVERSIONS["EXASECOND:KILOSECOND"] = \
        Mul(Rat(Int(1), Pow(10, 15)), Sym("exas"))
    CONVERSIONS["EXASECOND:MEGASECOND"] = \
        Mul(Rat(Int(1), Pow(10, 12)), Sym("exas"))
    CONVERSIONS["EXASECOND:MICROSECOND"] = \
        Mul(Rat(Int(1), Pow(10, 24)), Sym("exas"))
    CONVERSIONS["EXASECOND:MILLISECOND"] = \
        Mul(Rat(Int(1), Pow(10, 21)), Sym("exas"))
    CONVERSIONS["EXASECOND:MINUTE"] = \
        Mul(Rat(Int(3), Mul(Int(5), Pow(10, 16))), Sym("exas"))
    CONVERSIONS["EXASECOND:NANOSECOND"] = \
        Mul(Rat(Int(1), Pow(10, 27)), Sym("exas"))
    CONVERSIONS["EXASECOND:PETASECOND"] = \
        Mul(Rat(Int(1), Int(1000)), Sym("exas"))
    CONVERSIONS["EXASECOND:PICOSECOND"] = \
        Mul(Rat(Int(1), Pow(10, 30)), Sym("exas"))
    CONVERSIONS["EXASECOND:SECOND"] = \
        Mul(Rat(Int(1), Pow(10, 18)), Sym("exas"))
    CONVERSIONS["EXASECOND:TERASECOND"] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("exas"))
    CONVERSIONS["EXASECOND:YOCTOSECOND"] = \
        Mul(Rat(Int(1), Pow(10, 42)), Sym("exas"))
    CONVERSIONS["EXASECOND:YOTTASECOND"] = \
        Mul(Pow(10, 6), Sym("exas"))
    CONVERSIONS["EXASECOND:ZEPTOSECOND"] = \
        Mul(Rat(Int(1), Pow(10, 39)), Sym("exas"))
    CONVERSIONS["EXASECOND:ZETTASECOND"] = \
        Mul(Int(1000), Sym("exas"))
    CONVERSIONS["FEMTOSECOND:ATTOSECOND"] = \
        Mul(Rat(Int(1), Int(1000)), Sym("femtos"))
    CONVERSIONS["FEMTOSECOND:CENTISECOND"] = \
        Mul(Pow(10, 13), Sym("femtos"))
    CONVERSIONS["FEMTOSECOND:DAY"] = \
        Mul(Mul(Int(864), Pow(10, 17)), Sym("femtos"))
    CONVERSIONS["FEMTOSECOND:DECASECOND"] = \
        Mul(Pow(10, 16), Sym("femtos"))
    CONVERSIONS["FEMTOSECOND:DECISECOND"] = \
        Mul(Pow(10, 14), Sym("femtos"))
    CONVERSIONS["FEMTOSECOND:EXASECOND"] = \
        Mul(Pow(10, 33), Sym("femtos"))
    CONVERSIONS["FEMTOSECOND:GIGASECOND"] = \
        Mul(Pow(10, 24), Sym("femtos"))
    CONVERSIONS["FEMTOSECOND:HECTOSECOND"] = \
        Mul(Pow(10, 17), Sym("femtos"))
    CONVERSIONS["FEMTOSECOND:HOUR"] = \
        Mul(Mul(Int(36), Pow(10, 17)), Sym("femtos"))
    CONVERSIONS["FEMTOSECOND:KILOSECOND"] = \
        Mul(Pow(10, 18), Sym("femtos"))
    CONVERSIONS["FEMTOSECOND:MEGASECOND"] = \
        Mul(Pow(10, 21), Sym("femtos"))
    CONVERSIONS["FEMTOSECOND:MICROSECOND"] = \
        Mul(Pow(10, 9), Sym("femtos"))
    CONVERSIONS["FEMTOSECOND:MILLISECOND"] = \
        Mul(Pow(10, 12), Sym("femtos"))
    CONVERSIONS["FEMTOSECOND:MINUTE"] = \
        Mul(Mul(Int(6), Pow(10, 16)), Sym("femtos"))
    CONVERSIONS["FEMTOSECOND:NANOSECOND"] = \
        Mul(Pow(10, 6), Sym("femtos"))
    CONVERSIONS["FEMTOSECOND:PETASECOND"] = \
        Mul(Pow(10, 30), Sym("femtos"))
    CONVERSIONS["FEMTOSECOND:PICOSECOND"] = \
        Mul(Int(1000), Sym("femtos"))
    CONVERSIONS["FEMTOSECOND:SECOND"] = \
        Mul(Pow(10, 15), Sym("femtos"))
    CONVERSIONS["FEMTOSECOND:TERASECOND"] = \
        Mul(Pow(10, 27), Sym("femtos"))
    CONVERSIONS["FEMTOSECOND:YOCTOSECOND"] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("femtos"))
    CONVERSIONS["FEMTOSECOND:YOTTASECOND"] = \
        Mul(Pow(10, 39), Sym("femtos"))
    CONVERSIONS["FEMTOSECOND:ZEPTOSECOND"] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("femtos"))
    CONVERSIONS["FEMTOSECOND:ZETTASECOND"] = \
        Mul(Pow(10, 36), Sym("femtos"))
    CONVERSIONS["GIGASECOND:ATTOSECOND"] = \
        Mul(Rat(Int(1), Pow(10, 27)), Sym("gigas"))
    CONVERSIONS["GIGASECOND:CENTISECOND"] = \
        Mul(Rat(Int(1), Pow(10, 11)), Sym("gigas"))
    CONVERSIONS["GIGASECOND:DAY"] = \
        Mul(Rat(Int(27), Int(312500)), Sym("gigas"))
    CONVERSIONS["GIGASECOND:DECASECOND"] = \
        Mul(Rat(Int(1), Pow(10, 8)), Sym("gigas"))
    CONVERSIONS["GIGASECOND:DECISECOND"] = \
        Mul(Rat(Int(1), Pow(10, 10)), Sym("gigas"))
    CONVERSIONS["GIGASECOND:EXASECOND"] = \
        Mul(Pow(10, 9), Sym("gigas"))
    CONVERSIONS["GIGASECOND:FEMTOSECOND"] = \
        Mul(Rat(Int(1), Pow(10, 24)), Sym("gigas"))
    CONVERSIONS["GIGASECOND:HECTOSECOND"] = \
        Mul(Rat(Int(1), Pow(10, 7)), Sym("gigas"))
    CONVERSIONS["GIGASECOND:HOUR"] = \
        Mul(Rat(Int(9), Mul(Int(25), Pow(10, 5))), Sym("gigas"))
    CONVERSIONS["GIGASECOND:KILOSECOND"] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("gigas"))
    CONVERSIONS["GIGASECOND:MEGASECOND"] = \
        Mul(Rat(Int(1), Int(1000)), Sym("gigas"))
    CONVERSIONS["GIGASECOND:MICROSECOND"] = \
        Mul(Rat(Int(1), Pow(10, 15)), Sym("gigas"))
    CONVERSIONS["GIGASECOND:MILLISECOND"] = \
        Mul(Rat(Int(1), Pow(10, 12)), Sym("gigas"))
    CONVERSIONS["GIGASECOND:MINUTE"] = \
        Mul(Rat(Int(3), Mul(Int(5), Pow(10, 7))), Sym("gigas"))
    CONVERSIONS["GIGASECOND:NANOSECOND"] = \
        Mul(Rat(Int(1), Pow(10, 18)), Sym("gigas"))
    CONVERSIONS["GIGASECOND:PETASECOND"] = \
        Mul(Pow(10, 6), Sym("gigas"))
    CONVERSIONS["GIGASECOND:PICOSECOND"] = \
        Mul(Rat(Int(1), Pow(10, 21)), Sym("gigas"))
    CONVERSIONS["GIGASECOND:SECOND"] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("gigas"))
    CONVERSIONS["GIGASECOND:TERASECOND"] = \
        Mul(Int(1000), Sym("gigas"))
    CONVERSIONS["GIGASECOND:YOCTOSECOND"] = \
        Mul(Rat(Int(1), Pow(10, 33)), Sym("gigas"))
    CONVERSIONS["GIGASECOND:YOTTASECOND"] = \
        Mul(Pow(10, 15), Sym("gigas"))
    CONVERSIONS["GIGASECOND:ZEPTOSECOND"] = \
        Mul(Rat(Int(1), Pow(10, 30)), Sym("gigas"))
    CONVERSIONS["GIGASECOND:ZETTASECOND"] = \
        Mul(Pow(10, 12), Sym("gigas"))
    CONVERSIONS["HECTOSECOND:ATTOSECOND"] = \
        Mul(Rat(Int(1), Pow(10, 20)), Sym("hectos"))
    CONVERSIONS["HECTOSECOND:CENTISECOND"] = \
        Mul(Rat(Int(1), Pow(10, 4)), Sym("hectos"))
    CONVERSIONS["HECTOSECOND:DAY"] = \
        Mul(Int(864), Sym("hectos"))
    CONVERSIONS["HECTOSECOND:DECASECOND"] = \
        Mul(Rat(Int(1), Int(10)), Sym("hectos"))
    CONVERSIONS["HECTOSECOND:DECISECOND"] = \
        Mul(Rat(Int(1), Int(1000)), Sym("hectos"))
    CONVERSIONS["HECTOSECOND:EXASECOND"] = \
        Mul(Pow(10, 16), Sym("hectos"))
    CONVERSIONS["HECTOSECOND:FEMTOSECOND"] = \
        Mul(Rat(Int(1), Pow(10, 17)), Sym("hectos"))
    CONVERSIONS["HECTOSECOND:GIGASECOND"] = \
        Mul(Pow(10, 7), Sym("hectos"))
    CONVERSIONS["HECTOSECOND:HOUR"] = \
        Mul(Int(36), Sym("hectos"))
    CONVERSIONS["HECTOSECOND:KILOSECOND"] = \
        Mul(Int(10), Sym("hectos"))
    CONVERSIONS["HECTOSECOND:MEGASECOND"] = \
        Mul(Pow(10, 4), Sym("hectos"))
    CONVERSIONS["HECTOSECOND:MICROSECOND"] = \
        Mul(Rat(Int(1), Pow(10, 8)), Sym("hectos"))
    CONVERSIONS["HECTOSECOND:MILLISECOND"] = \
        Mul(Rat(Int(1), Pow(10, 5)), Sym("hectos"))
    CONVERSIONS["HECTOSECOND:MINUTE"] = \
        Mul(Rat(Int(3), Int(5)), Sym("hectos"))
    CONVERSIONS["HECTOSECOND:NANOSECOND"] = \
        Mul(Rat(Int(1), Pow(10, 11)), Sym("hectos"))
    CONVERSIONS["HECTOSECOND:PETASECOND"] = \
        Mul(Pow(10, 13), Sym("hectos"))
    CONVERSIONS["HECTOSECOND:PICOSECOND"] = \
        Mul(Rat(Int(1), Pow(10, 14)), Sym("hectos"))
    CONVERSIONS["HECTOSECOND:SECOND"] = \
        Mul(Rat(Int(1), Int(100)), Sym("hectos"))
    CONVERSIONS["HECTOSECOND:TERASECOND"] = \
        Mul(Pow(10, 10), Sym("hectos"))
    CONVERSIONS["HECTOSECOND:YOCTOSECOND"] = \
        Mul(Rat(Int(1), Pow(10, 26)), Sym("hectos"))
    CONVERSIONS["HECTOSECOND:YOTTASECOND"] = \
        Mul(Pow(10, 22), Sym("hectos"))
    CONVERSIONS["HECTOSECOND:ZEPTOSECOND"] = \
        Mul(Rat(Int(1), Pow(10, 23)), Sym("hectos"))
    CONVERSIONS["HECTOSECOND:ZETTASECOND"] = \
        Mul(Pow(10, 19), Sym("hectos"))
    CONVERSIONS["HOUR:ATTOSECOND"] = \
        Mul(Rat(Int(1), Mul(Int(36), Pow(10, 20))), Sym("h"))
    CONVERSIONS["HOUR:CENTISECOND"] = \
        Mul(Rat(Int(1), Mul(Int(36), Pow(10, 4))), Sym("h"))
    CONVERSIONS["HOUR:DAY"] = \
        Mul(Int(24), Sym("h"))
    CONVERSIONS["HOUR:DECASECOND"] = \
        Mul(Rat(Int(1), Int(360)), Sym("h"))
    CONVERSIONS["HOUR:DECISECOND"] = \
        Mul(Rat(Int(1), Int(36000)), Sym("h"))
    CONVERSIONS["HOUR:EXASECOND"] = \
        Mul(Rat(Mul(Int(25), Pow(10, 14)), Int(9)), Sym("h"))
    CONVERSIONS["HOUR:FEMTOSECOND"] = \
        Mul(Rat(Int(1), Mul(Int(36), Pow(10, 17))), Sym("h"))
    CONVERSIONS["HOUR:GIGASECOND"] = \
        Mul(Rat(Mul(Int(25), Pow(10, 5)), Int(9)), Sym("h"))
    CONVERSIONS["HOUR:HECTOSECOND"] = \
        Mul(Rat(Int(1), Int(36)), Sym("h"))
    CONVERSIONS["HOUR:KILOSECOND"] = \
        Mul(Rat(Int(5), Int(18)), Sym("h"))
    CONVERSIONS["HOUR:MEGASECOND"] = \
        Mul(Rat(Int(2500), Int(9)), Sym("h"))
    CONVERSIONS["HOUR:MICROSECOND"] = \
        Mul(Rat(Int(1), Mul(Int(36), Pow(10, 8))), Sym("h"))
    CONVERSIONS["HOUR:MILLISECOND"] = \
        Mul(Rat(Int(1), Mul(Int(36), Pow(10, 5))), Sym("h"))
    CONVERSIONS["HOUR:MINUTE"] = \
        Mul(Rat(Int(1), Int(60)), Sym("h"))
    CONVERSIONS["HOUR:NANOSECOND"] = \
        Mul(Rat(Int(1), Mul(Int(36), Pow(10, 11))), Sym("h"))
    CONVERSIONS["HOUR:PETASECOND"] = \
        Mul(Rat(Mul(Int(25), Pow(10, 11)), Int(9)), Sym("h"))
    CONVERSIONS["HOUR:PICOSECOND"] = \
        Mul(Rat(Int(1), Mul(Int(36), Pow(10, 14))), Sym("h"))
    CONVERSIONS["HOUR:SECOND"] = \
        Mul(Rat(Int(1), Int(3600)), Sym("h"))
    CONVERSIONS["HOUR:TERASECOND"] = \
        Mul(Rat(Mul(Int(25), Pow(10, 8)), Int(9)), Sym("h"))
    CONVERSIONS["HOUR:YOCTOSECOND"] = \
        Mul(Rat(Int(1), Mul(Int(36), Pow(10, 26))), Sym("h"))
    CONVERSIONS["HOUR:YOTTASECOND"] = \
        Mul(Rat(Mul(Int(25), Pow(10, 20)), Int(9)), Sym("h"))
    CONVERSIONS["HOUR:ZEPTOSECOND"] = \
        Mul(Rat(Int(1), Mul(Int(36), Pow(10, 23))), Sym("h"))
    CONVERSIONS["HOUR:ZETTASECOND"] = \
        Mul(Rat(Mul(Int(25), Pow(10, 17)), Int(9)), Sym("h"))
    CONVERSIONS["KILOSECOND:ATTOSECOND"] = \
        Mul(Rat(Int(1), Pow(10, 21)), Sym("kilos"))
    CONVERSIONS["KILOSECOND:CENTISECOND"] = \
        Mul(Rat(Int(1), Pow(10, 5)), Sym("kilos"))
    CONVERSIONS["KILOSECOND:DAY"] = \
        Mul(Rat(Int(432), Int(5)), Sym("kilos"))
    CONVERSIONS["KILOSECOND:DECASECOND"] = \
        Mul(Rat(Int(1), Int(100)), Sym("kilos"))
    CONVERSIONS["KILOSECOND:DECISECOND"] = \
        Mul(Rat(Int(1), Pow(10, 4)), Sym("kilos"))
    CONVERSIONS["KILOSECOND:EXASECOND"] = \
        Mul(Pow(10, 15), Sym("kilos"))
    CONVERSIONS["KILOSECOND:FEMTOSECOND"] = \
        Mul(Rat(Int(1), Pow(10, 18)), Sym("kilos"))
    CONVERSIONS["KILOSECOND:GIGASECOND"] = \
        Mul(Pow(10, 6), Sym("kilos"))
    CONVERSIONS["KILOSECOND:HECTOSECOND"] = \
        Mul(Rat(Int(1), Int(10)), Sym("kilos"))
    CONVERSIONS["KILOSECOND:HOUR"] = \
        Mul(Rat(Int(18), Int(5)), Sym("kilos"))
    CONVERSIONS["KILOSECOND:MEGASECOND"] = \
        Mul(Int(1000), Sym("kilos"))
    CONVERSIONS["KILOSECOND:MICROSECOND"] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("kilos"))
    CONVERSIONS["KILOSECOND:MILLISECOND"] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("kilos"))
    CONVERSIONS["KILOSECOND:MINUTE"] = \
        Mul(Rat(Int(3), Int(50)), Sym("kilos"))
    CONVERSIONS["KILOSECOND:NANOSECOND"] = \
        Mul(Rat(Int(1), Pow(10, 12)), Sym("kilos"))
    CONVERSIONS["KILOSECOND:PETASECOND"] = \
        Mul(Pow(10, 12), Sym("kilos"))
    CONVERSIONS["KILOSECOND:PICOSECOND"] = \
        Mul(Rat(Int(1), Pow(10, 15)), Sym("kilos"))
    CONVERSIONS["KILOSECOND:SECOND"] = \
        Mul(Rat(Int(1), Int(1000)), Sym("kilos"))
    CONVERSIONS["KILOSECOND:TERASECOND"] = \
        Mul(Pow(10, 9), Sym("kilos"))
    CONVERSIONS["KILOSECOND:YOCTOSECOND"] = \
        Mul(Rat(Int(1), Pow(10, 27)), Sym("kilos"))
    CONVERSIONS["KILOSECOND:YOTTASECOND"] = \
        Mul(Pow(10, 21), Sym("kilos"))
    CONVERSIONS["KILOSECOND:ZEPTOSECOND"] = \
        Mul(Rat(Int(1), Pow(10, 24)), Sym("kilos"))
    CONVERSIONS["KILOSECOND:ZETTASECOND"] = \
        Mul(Pow(10, 18), Sym("kilos"))
    CONVERSIONS["MEGASECOND:ATTOSECOND"] = \
        Mul(Rat(Int(1), Pow(10, 24)), Sym("megas"))
    CONVERSIONS["MEGASECOND:CENTISECOND"] = \
        Mul(Rat(Int(1), Pow(10, 8)), Sym("megas"))
    CONVERSIONS["MEGASECOND:DAY"] = \
        Mul(Rat(Int(54), Int(625)), Sym("megas"))
    CONVERSIONS["MEGASECOND:DECASECOND"] = \
        Mul(Rat(Int(1), Pow(10, 5)), Sym("megas"))
    CONVERSIONS["MEGASECOND:DECISECOND"] = \
        Mul(Rat(Int(1), Pow(10, 7)), Sym("megas"))
    CONVERSIONS["MEGASECOND:EXASECOND"] = \
        Mul(Pow(10, 12), Sym("megas"))
    CONVERSIONS["MEGASECOND:FEMTOSECOND"] = \
        Mul(Rat(Int(1), Pow(10, 21)), Sym("megas"))
    CONVERSIONS["MEGASECOND:GIGASECOND"] = \
        Mul(Int(1000), Sym("megas"))
    CONVERSIONS["MEGASECOND:HECTOSECOND"] = \
        Mul(Rat(Int(1), Pow(10, 4)), Sym("megas"))
    CONVERSIONS["MEGASECOND:HOUR"] = \
        Mul(Rat(Int(9), Int(2500)), Sym("megas"))
    CONVERSIONS["MEGASECOND:KILOSECOND"] = \
        Mul(Rat(Int(1), Int(1000)), Sym("megas"))
    CONVERSIONS["MEGASECOND:MICROSECOND"] = \
        Mul(Rat(Int(1), Pow(10, 12)), Sym("megas"))
    CONVERSIONS["MEGASECOND:MILLISECOND"] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("megas"))
    CONVERSIONS["MEGASECOND:MINUTE"] = \
        Mul(Rat(Int(3), Mul(Int(5), Pow(10, 4))), Sym("megas"))
    CONVERSIONS["MEGASECOND:NANOSECOND"] = \
        Mul(Rat(Int(1), Pow(10, 15)), Sym("megas"))
    CONVERSIONS["MEGASECOND:PETASECOND"] = \
        Mul(Pow(10, 9), Sym("megas"))
    CONVERSIONS["MEGASECOND:PICOSECOND"] = \
        Mul(Rat(Int(1), Pow(10, 18)), Sym("megas"))
    CONVERSIONS["MEGASECOND:SECOND"] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("megas"))
    CONVERSIONS["MEGASECOND:TERASECOND"] = \
        Mul(Pow(10, 6), Sym("megas"))
    CONVERSIONS["MEGASECOND:YOCTOSECOND"] = \
        Mul(Rat(Int(1), Pow(10, 30)), Sym("megas"))
    CONVERSIONS["MEGASECOND:YOTTASECOND"] = \
        Mul(Pow(10, 18), Sym("megas"))
    CONVERSIONS["MEGASECOND:ZEPTOSECOND"] = \
        Mul(Rat(Int(1), Pow(10, 27)), Sym("megas"))
    CONVERSIONS["MEGASECOND:ZETTASECOND"] = \
        Mul(Pow(10, 15), Sym("megas"))
    CONVERSIONS["MICROSECOND:ATTOSECOND"] = \
        Mul(Rat(Int(1), Pow(10, 12)), Sym("micros"))
    CONVERSIONS["MICROSECOND:CENTISECOND"] = \
        Mul(Pow(10, 4), Sym("micros"))
    CONVERSIONS["MICROSECOND:DAY"] = \
        Mul(Mul(Int(864), Pow(10, 8)), Sym("micros"))
    CONVERSIONS["MICROSECOND:DECASECOND"] = \
        Mul(Pow(10, 7), Sym("micros"))
    CONVERSIONS["MICROSECOND:DECISECOND"] = \
        Mul(Pow(10, 5), Sym("micros"))
    CONVERSIONS["MICROSECOND:EXASECOND"] = \
        Mul(Pow(10, 24), Sym("micros"))
    CONVERSIONS["MICROSECOND:FEMTOSECOND"] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("micros"))
    CONVERSIONS["MICROSECOND:GIGASECOND"] = \
        Mul(Pow(10, 15), Sym("micros"))
    CONVERSIONS["MICROSECOND:HECTOSECOND"] = \
        Mul(Pow(10, 8), Sym("micros"))
    CONVERSIONS["MICROSECOND:HOUR"] = \
        Mul(Mul(Int(36), Pow(10, 8)), Sym("micros"))
    CONVERSIONS["MICROSECOND:KILOSECOND"] = \
        Mul(Pow(10, 9), Sym("micros"))
    CONVERSIONS["MICROSECOND:MEGASECOND"] = \
        Mul(Pow(10, 12), Sym("micros"))
    CONVERSIONS["MICROSECOND:MILLISECOND"] = \
        Mul(Int(1000), Sym("micros"))
    CONVERSIONS["MICROSECOND:MINUTE"] = \
        Mul(Mul(Int(6), Pow(10, 7)), Sym("micros"))
    CONVERSIONS["MICROSECOND:NANOSECOND"] = \
        Mul(Rat(Int(1), Int(1000)), Sym("micros"))
    CONVERSIONS["MICROSECOND:PETASECOND"] = \
        Mul(Pow(10, 21), Sym("micros"))
    CONVERSIONS["MICROSECOND:PICOSECOND"] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("micros"))
    CONVERSIONS["MICROSECOND:SECOND"] = \
        Mul(Pow(10, 6), Sym("micros"))
    CONVERSIONS["MICROSECOND:TERASECOND"] = \
        Mul(Pow(10, 18), Sym("micros"))
    CONVERSIONS["MICROSECOND:YOCTOSECOND"] = \
        Mul(Rat(Int(1), Pow(10, 18)), Sym("micros"))
    CONVERSIONS["MICROSECOND:YOTTASECOND"] = \
        Mul(Pow(10, 30), Sym("micros"))
    CONVERSIONS["MICROSECOND:ZEPTOSECOND"] = \
        Mul(Rat(Int(1), Pow(10, 15)), Sym("micros"))
    CONVERSIONS["MICROSECOND:ZETTASECOND"] = \
        Mul(Pow(10, 27), Sym("micros"))
    CONVERSIONS["MILLISECOND:ATTOSECOND"] = \
        Mul(Rat(Int(1), Pow(10, 15)), Sym("millis"))
    CONVERSIONS["MILLISECOND:CENTISECOND"] = \
        Mul(Int(10), Sym("millis"))
    CONVERSIONS["MILLISECOND:DAY"] = \
        Mul(Mul(Int(864), Pow(10, 5)), Sym("millis"))
    CONVERSIONS["MILLISECOND:DECASECOND"] = \
        Mul(Pow(10, 4), Sym("millis"))
    CONVERSIONS["MILLISECOND:DECISECOND"] = \
        Mul(Int(100), Sym("millis"))
    CONVERSIONS["MILLISECOND:EXASECOND"] = \
        Mul(Pow(10, 21), Sym("millis"))
    CONVERSIONS["MILLISECOND:FEMTOSECOND"] = \
        Mul(Rat(Int(1), Pow(10, 12)), Sym("millis"))
    CONVERSIONS["MILLISECOND:GIGASECOND"] = \
        Mul(Pow(10, 12), Sym("millis"))
    CONVERSIONS["MILLISECOND:HECTOSECOND"] = \
        Mul(Pow(10, 5), Sym("millis"))
    CONVERSIONS["MILLISECOND:HOUR"] = \
        Mul(Mul(Int(36), Pow(10, 5)), Sym("millis"))
    CONVERSIONS["MILLISECOND:KILOSECOND"] = \
        Mul(Pow(10, 6), Sym("millis"))
    CONVERSIONS["MILLISECOND:MEGASECOND"] = \
        Mul(Pow(10, 9), Sym("millis"))
    CONVERSIONS["MILLISECOND:MICROSECOND"] = \
        Mul(Rat(Int(1), Int(1000)), Sym("millis"))
    CONVERSIONS["MILLISECOND:MINUTE"] = \
        Mul(Mul(Int(6), Pow(10, 4)), Sym("millis"))
    CONVERSIONS["MILLISECOND:NANOSECOND"] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("millis"))
    CONVERSIONS["MILLISECOND:PETASECOND"] = \
        Mul(Pow(10, 18), Sym("millis"))
    CONVERSIONS["MILLISECOND:PICOSECOND"] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("millis"))
    CONVERSIONS["MILLISECOND:SECOND"] = \
        Mul(Int(1000), Sym("millis"))
    CONVERSIONS["MILLISECOND:TERASECOND"] = \
        Mul(Pow(10, 15), Sym("millis"))
    CONVERSIONS["MILLISECOND:YOCTOSECOND"] = \
        Mul(Rat(Int(1), Pow(10, 21)), Sym("millis"))
    CONVERSIONS["MILLISECOND:YOTTASECOND"] = \
        Mul(Pow(10, 27), Sym("millis"))
    CONVERSIONS["MILLISECOND:ZEPTOSECOND"] = \
        Mul(Rat(Int(1), Pow(10, 18)), Sym("millis"))
    CONVERSIONS["MILLISECOND:ZETTASECOND"] = \
        Mul(Pow(10, 24), Sym("millis"))
    CONVERSIONS["MINUTE:ATTOSECOND"] = \
        Mul(Rat(Int(1), Mul(Int(6), Pow(10, 19))), Sym("m"))
    CONVERSIONS["MINUTE:CENTISECOND"] = \
        Mul(Rat(Int(1), Int(6000)), Sym("m"))
    CONVERSIONS["MINUTE:DAY"] = \
        Mul(Int(1440), Sym("m"))
    CONVERSIONS["MINUTE:DECASECOND"] = \
        Mul(Rat(Int(1), Int(6)), Sym("m"))
    CONVERSIONS["MINUTE:DECISECOND"] = \
        Mul(Rat(Int(1), Int(600)), Sym("m"))
    CONVERSIONS["MINUTE:EXASECOND"] = \
        Mul(Rat(Mul(Int(5), Pow(10, 16)), Int(3)), Sym("m"))
    CONVERSIONS["MINUTE:FEMTOSECOND"] = \
        Mul(Rat(Int(1), Mul(Int(6), Pow(10, 16))), Sym("m"))
    CONVERSIONS["MINUTE:GIGASECOND"] = \
        Mul(Rat(Mul(Int(5), Pow(10, 7)), Int(3)), Sym("m"))
    CONVERSIONS["MINUTE:HECTOSECOND"] = \
        Mul(Rat(Int(5), Int(3)), Sym("m"))
    CONVERSIONS["MINUTE:HOUR"] = \
        Mul(Int(60), Sym("m"))
    CONVERSIONS["MINUTE:KILOSECOND"] = \
        Mul(Rat(Int(50), Int(3)), Sym("m"))
    CONVERSIONS["MINUTE:MEGASECOND"] = \
        Mul(Rat(Mul(Int(5), Pow(10, 4)), Int(3)), Sym("m"))
    CONVERSIONS["MINUTE:MICROSECOND"] = \
        Mul(Rat(Int(1), Mul(Int(6), Pow(10, 7))), Sym("m"))
    CONVERSIONS["MINUTE:MILLISECOND"] = \
        Mul(Rat(Int(1), Mul(Int(6), Pow(10, 4))), Sym("m"))
    CONVERSIONS["MINUTE:NANOSECOND"] = \
        Mul(Rat(Int(1), Mul(Int(6), Pow(10, 10))), Sym("m"))
    CONVERSIONS["MINUTE:PETASECOND"] = \
        Mul(Rat(Mul(Int(5), Pow(10, 13)), Int(3)), Sym("m"))
    CONVERSIONS["MINUTE:PICOSECOND"] = \
        Mul(Rat(Int(1), Mul(Int(6), Pow(10, 13))), Sym("m"))
    CONVERSIONS["MINUTE:SECOND"] = \
        Mul(Rat(Int(1), Int(60)), Sym("m"))
    CONVERSIONS["MINUTE:TERASECOND"] = \
        Mul(Rat(Mul(Int(5), Pow(10, 10)), Int(3)), Sym("m"))
    CONVERSIONS["MINUTE:YOCTOSECOND"] = \
        Mul(Rat(Int(1), Mul(Int(6), Pow(10, 25))), Sym("m"))
    CONVERSIONS["MINUTE:YOTTASECOND"] = \
        Mul(Rat(Mul(Int(5), Pow(10, 22)), Int(3)), Sym("m"))
    CONVERSIONS["MINUTE:ZEPTOSECOND"] = \
        Mul(Rat(Int(1), Mul(Int(6), Pow(10, 22))), Sym("m"))
    CONVERSIONS["MINUTE:ZETTASECOND"] = \
        Mul(Rat(Mul(Int(5), Pow(10, 19)), Int(3)), Sym("m"))
    CONVERSIONS["NANOSECOND:ATTOSECOND"] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("nanos"))
    CONVERSIONS["NANOSECOND:CENTISECOND"] = \
        Mul(Pow(10, 7), Sym("nanos"))
    CONVERSIONS["NANOSECOND:DAY"] = \
        Mul(Mul(Int(864), Pow(10, 11)), Sym("nanos"))
    CONVERSIONS["NANOSECOND:DECASECOND"] = \
        Mul(Pow(10, 10), Sym("nanos"))
    CONVERSIONS["NANOSECOND:DECISECOND"] = \
        Mul(Pow(10, 8), Sym("nanos"))
    CONVERSIONS["NANOSECOND:EXASECOND"] = \
        Mul(Pow(10, 27), Sym("nanos"))
    CONVERSIONS["NANOSECOND:FEMTOSECOND"] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("nanos"))
    CONVERSIONS["NANOSECOND:GIGASECOND"] = \
        Mul(Pow(10, 18), Sym("nanos"))
    CONVERSIONS["NANOSECOND:HECTOSECOND"] = \
        Mul(Pow(10, 11), Sym("nanos"))
    CONVERSIONS["NANOSECOND:HOUR"] = \
        Mul(Mul(Int(36), Pow(10, 11)), Sym("nanos"))
    CONVERSIONS["NANOSECOND:KILOSECOND"] = \
        Mul(Pow(10, 12), Sym("nanos"))
    CONVERSIONS["NANOSECOND:MEGASECOND"] = \
        Mul(Pow(10, 15), Sym("nanos"))
    CONVERSIONS["NANOSECOND:MICROSECOND"] = \
        Mul(Int(1000), Sym("nanos"))
    CONVERSIONS["NANOSECOND:MILLISECOND"] = \
        Mul(Pow(10, 6), Sym("nanos"))
    CONVERSIONS["NANOSECOND:MINUTE"] = \
        Mul(Mul(Int(6), Pow(10, 10)), Sym("nanos"))
    CONVERSIONS["NANOSECOND:PETASECOND"] = \
        Mul(Pow(10, 24), Sym("nanos"))
    CONVERSIONS["NANOSECOND:PICOSECOND"] = \
        Mul(Rat(Int(1), Int(1000)), Sym("nanos"))
    CONVERSIONS["NANOSECOND:SECOND"] = \
        Mul(Pow(10, 9), Sym("nanos"))
    CONVERSIONS["NANOSECOND:TERASECOND"] = \
        Mul(Pow(10, 21), Sym("nanos"))
    CONVERSIONS["NANOSECOND:YOCTOSECOND"] = \
        Mul(Rat(Int(1), Pow(10, 15)), Sym("nanos"))
    CONVERSIONS["NANOSECOND:YOTTASECOND"] = \
        Mul(Pow(10, 33), Sym("nanos"))
    CONVERSIONS["NANOSECOND:ZEPTOSECOND"] = \
        Mul(Rat(Int(1), Pow(10, 12)), Sym("nanos"))
    CONVERSIONS["NANOSECOND:ZETTASECOND"] = \
        Mul(Pow(10, 30), Sym("nanos"))
    CONVERSIONS["PETASECOND:ATTOSECOND"] = \
        Mul(Rat(Int(1), Pow(10, 33)), Sym("petas"))
    CONVERSIONS["PETASECOND:CENTISECOND"] = \
        Mul(Rat(Int(1), Pow(10, 17)), Sym("petas"))
    CONVERSIONS["PETASECOND:DAY"] = \
        Mul(Rat(Int(27), Mul(Int(3125), Pow(10, 8))), Sym("petas"))
    CONVERSIONS["PETASECOND:DECASECOND"] = \
        Mul(Rat(Int(1), Pow(10, 14)), Sym("petas"))
    CONVERSIONS["PETASECOND:DECISECOND"] = \
        Mul(Rat(Int(1), Pow(10, 16)), Sym("petas"))
    CONVERSIONS["PETASECOND:EXASECOND"] = \
        Mul(Int(1000), Sym("petas"))
    CONVERSIONS["PETASECOND:FEMTOSECOND"] = \
        Mul(Rat(Int(1), Pow(10, 30)), Sym("petas"))
    CONVERSIONS["PETASECOND:GIGASECOND"] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("petas"))
    CONVERSIONS["PETASECOND:HECTOSECOND"] = \
        Mul(Rat(Int(1), Pow(10, 13)), Sym("petas"))
    CONVERSIONS["PETASECOND:HOUR"] = \
        Mul(Rat(Int(9), Mul(Int(25), Pow(10, 11))), Sym("petas"))
    CONVERSIONS["PETASECOND:KILOSECOND"] = \
        Mul(Rat(Int(1), Pow(10, 12)), Sym("petas"))
    CONVERSIONS["PETASECOND:MEGASECOND"] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("petas"))
    CONVERSIONS["PETASECOND:MICROSECOND"] = \
        Mul(Rat(Int(1), Pow(10, 21)), Sym("petas"))
    CONVERSIONS["PETASECOND:MILLISECOND"] = \
        Mul(Rat(Int(1), Pow(10, 18)), Sym("petas"))
    CONVERSIONS["PETASECOND:MINUTE"] = \
        Mul(Rat(Int(3), Mul(Int(5), Pow(10, 13))), Sym("petas"))
    CONVERSIONS["PETASECOND:NANOSECOND"] = \
        Mul(Rat(Int(1), Pow(10, 24)), Sym("petas"))
    CONVERSIONS["PETASECOND:PICOSECOND"] = \
        Mul(Rat(Int(1), Pow(10, 27)), Sym("petas"))
    CONVERSIONS["PETASECOND:SECOND"] = \
        Mul(Rat(Int(1), Pow(10, 15)), Sym("petas"))
    CONVERSIONS["PETASECOND:TERASECOND"] = \
        Mul(Rat(Int(1), Int(1000)), Sym("petas"))
    CONVERSIONS["PETASECOND:YOCTOSECOND"] = \
        Mul(Rat(Int(1), Pow(10, 39)), Sym("petas"))
    CONVERSIONS["PETASECOND:YOTTASECOND"] = \
        Mul(Pow(10, 9), Sym("petas"))
    CONVERSIONS["PETASECOND:ZEPTOSECOND"] = \
        Mul(Rat(Int(1), Pow(10, 36)), Sym("petas"))
    CONVERSIONS["PETASECOND:ZETTASECOND"] = \
        Mul(Pow(10, 6), Sym("petas"))
    CONVERSIONS["PICOSECOND:ATTOSECOND"] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("picos"))
    CONVERSIONS["PICOSECOND:CENTISECOND"] = \
        Mul(Pow(10, 10), Sym("picos"))
    CONVERSIONS["PICOSECOND:DAY"] = \
        Mul(Mul(Int(864), Pow(10, 14)), Sym("picos"))
    CONVERSIONS["PICOSECOND:DECASECOND"] = \
        Mul(Pow(10, 13), Sym("picos"))
    CONVERSIONS["PICOSECOND:DECISECOND"] = \
        Mul(Pow(10, 11), Sym("picos"))
    CONVERSIONS["PICOSECOND:EXASECOND"] = \
        Mul(Pow(10, 30), Sym("picos"))
    CONVERSIONS["PICOSECOND:FEMTOSECOND"] = \
        Mul(Rat(Int(1), Int(1000)), Sym("picos"))
    CONVERSIONS["PICOSECOND:GIGASECOND"] = \
        Mul(Pow(10, 21), Sym("picos"))
    CONVERSIONS["PICOSECOND:HECTOSECOND"] = \
        Mul(Pow(10, 14), Sym("picos"))
    CONVERSIONS["PICOSECOND:HOUR"] = \
        Mul(Mul(Int(36), Pow(10, 14)), Sym("picos"))
    CONVERSIONS["PICOSECOND:KILOSECOND"] = \
        Mul(Pow(10, 15), Sym("picos"))
    CONVERSIONS["PICOSECOND:MEGASECOND"] = \
        Mul(Pow(10, 18), Sym("picos"))
    CONVERSIONS["PICOSECOND:MICROSECOND"] = \
        Mul(Pow(10, 6), Sym("picos"))
    CONVERSIONS["PICOSECOND:MILLISECOND"] = \
        Mul(Pow(10, 9), Sym("picos"))
    CONVERSIONS["PICOSECOND:MINUTE"] = \
        Mul(Mul(Int(6), Pow(10, 13)), Sym("picos"))
    CONVERSIONS["PICOSECOND:NANOSECOND"] = \
        Mul(Int(1000), Sym("picos"))
    CONVERSIONS["PICOSECOND:PETASECOND"] = \
        Mul(Pow(10, 27), Sym("picos"))
    CONVERSIONS["PICOSECOND:SECOND"] = \
        Mul(Pow(10, 12), Sym("picos"))
    CONVERSIONS["PICOSECOND:TERASECOND"] = \
        Mul(Pow(10, 24), Sym("picos"))
    CONVERSIONS["PICOSECOND:YOCTOSECOND"] = \
        Mul(Rat(Int(1), Pow(10, 12)), Sym("picos"))
    CONVERSIONS["PICOSECOND:YOTTASECOND"] = \
        Mul(Pow(10, 36), Sym("picos"))
    CONVERSIONS["PICOSECOND:ZEPTOSECOND"] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("picos"))
    CONVERSIONS["PICOSECOND:ZETTASECOND"] = \
        Mul(Pow(10, 33), Sym("picos"))
    CONVERSIONS["SECOND:ATTOSECOND"] = \
        Mul(Rat(Int(1), Pow(10, 18)), Sym("s"))
    CONVERSIONS["SECOND:CENTISECOND"] = \
        Mul(Rat(Int(1), Int(100)), Sym("s"))
    CONVERSIONS["SECOND:DAY"] = \
        Mul(Int(86400), Sym("s"))
    CONVERSIONS["SECOND:DECASECOND"] = \
        Mul(Int(10), Sym("s"))
    CONVERSIONS["SECOND:DECISECOND"] = \
        Mul(Rat(Int(1), Int(10)), Sym("s"))
    CONVERSIONS["SECOND:EXASECOND"] = \
        Mul(Pow(10, 18), Sym("s"))
    CONVERSIONS["SECOND:FEMTOSECOND"] = \
        Mul(Rat(Int(1), Pow(10, 15)), Sym("s"))
    CONVERSIONS["SECOND:GIGASECOND"] = \
        Mul(Pow(10, 9), Sym("s"))
    CONVERSIONS["SECOND:HECTOSECOND"] = \
        Mul(Int(100), Sym("s"))
    CONVERSIONS["SECOND:HOUR"] = \
        Mul(Int(3600), Sym("s"))
    CONVERSIONS["SECOND:KILOSECOND"] = \
        Mul(Int(1000), Sym("s"))
    CONVERSIONS["SECOND:MEGASECOND"] = \
        Mul(Pow(10, 6), Sym("s"))
    CONVERSIONS["SECOND:MICROSECOND"] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("s"))
    CONVERSIONS["SECOND:MILLISECOND"] = \
        Mul(Rat(Int(1), Int(1000)), Sym("s"))
    CONVERSIONS["SECOND:MINUTE"] = \
        Mul(Int(60), Sym("s"))
    CONVERSIONS["SECOND:NANOSECOND"] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("s"))
    CONVERSIONS["SECOND:PETASECOND"] = \
        Mul(Pow(10, 15), Sym("s"))
    CONVERSIONS["SECOND:PICOSECOND"] = \
        Mul(Rat(Int(1), Pow(10, 12)), Sym("s"))
    CONVERSIONS["SECOND:TERASECOND"] = \
        Mul(Pow(10, 12), Sym("s"))
    CONVERSIONS["SECOND:YOCTOSECOND"] = \
        Mul(Rat(Int(1), Pow(10, 24)), Sym("s"))
    CONVERSIONS["SECOND:YOTTASECOND"] = \
        Mul(Pow(10, 24), Sym("s"))
    CONVERSIONS["SECOND:ZEPTOSECOND"] = \
        Mul(Rat(Int(1), Pow(10, 21)), Sym("s"))
    CONVERSIONS["SECOND:ZETTASECOND"] = \
        Mul(Pow(10, 21), Sym("s"))
    CONVERSIONS["TERASECOND:ATTOSECOND"] = \
        Mul(Rat(Int(1), Pow(10, 30)), Sym("teras"))
    CONVERSIONS["TERASECOND:CENTISECOND"] = \
        Mul(Rat(Int(1), Pow(10, 14)), Sym("teras"))
    CONVERSIONS["TERASECOND:DAY"] = \
        Mul(Rat(Int(27), Mul(Int(3125), Pow(10, 5))), Sym("teras"))
    CONVERSIONS["TERASECOND:DECASECOND"] = \
        Mul(Rat(Int(1), Pow(10, 11)), Sym("teras"))
    CONVERSIONS["TERASECOND:DECISECOND"] = \
        Mul(Rat(Int(1), Pow(10, 13)), Sym("teras"))
    CONVERSIONS["TERASECOND:EXASECOND"] = \
        Mul(Pow(10, 6), Sym("teras"))
    CONVERSIONS["TERASECOND:FEMTOSECOND"] = \
        Mul(Rat(Int(1), Pow(10, 27)), Sym("teras"))
    CONVERSIONS["TERASECOND:GIGASECOND"] = \
        Mul(Rat(Int(1), Int(1000)), Sym("teras"))
    CONVERSIONS["TERASECOND:HECTOSECOND"] = \
        Mul(Rat(Int(1), Pow(10, 10)), Sym("teras"))
    CONVERSIONS["TERASECOND:HOUR"] = \
        Mul(Rat(Int(9), Mul(Int(25), Pow(10, 8))), Sym("teras"))
    CONVERSIONS["TERASECOND:KILOSECOND"] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("teras"))
    CONVERSIONS["TERASECOND:MEGASECOND"] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("teras"))
    CONVERSIONS["TERASECOND:MICROSECOND"] = \
        Mul(Rat(Int(1), Pow(10, 18)), Sym("teras"))
    CONVERSIONS["TERASECOND:MILLISECOND"] = \
        Mul(Rat(Int(1), Pow(10, 15)), Sym("teras"))
    CONVERSIONS["TERASECOND:MINUTE"] = \
        Mul(Rat(Int(3), Mul(Int(5), Pow(10, 10))), Sym("teras"))
    CONVERSIONS["TERASECOND:NANOSECOND"] = \
        Mul(Rat(Int(1), Pow(10, 21)), Sym("teras"))
    CONVERSIONS["TERASECOND:PETASECOND"] = \
        Mul(Int(1000), Sym("teras"))
    CONVERSIONS["TERASECOND:PICOSECOND"] = \
        Mul(Rat(Int(1), Pow(10, 24)), Sym("teras"))
    CONVERSIONS["TERASECOND:SECOND"] = \
        Mul(Rat(Int(1), Pow(10, 12)), Sym("teras"))
    CONVERSIONS["TERASECOND:YOCTOSECOND"] = \
        Mul(Rat(Int(1), Pow(10, 36)), Sym("teras"))
    CONVERSIONS["TERASECOND:YOTTASECOND"] = \
        Mul(Pow(10, 12), Sym("teras"))
    CONVERSIONS["TERASECOND:ZEPTOSECOND"] = \
        Mul(Rat(Int(1), Pow(10, 33)), Sym("teras"))
    CONVERSIONS["TERASECOND:ZETTASECOND"] = \
        Mul(Pow(10, 9), Sym("teras"))
    CONVERSIONS["YOCTOSECOND:ATTOSECOND"] = \
        Mul(Pow(10, 6), Sym("yoctos"))
    CONVERSIONS["YOCTOSECOND:CENTISECOND"] = \
        Mul(Pow(10, 22), Sym("yoctos"))
    CONVERSIONS["YOCTOSECOND:DAY"] = \
        Mul(Mul(Int(864), Pow(10, 26)), Sym("yoctos"))
    CONVERSIONS["YOCTOSECOND:DECASECOND"] = \
        Mul(Pow(10, 25), Sym("yoctos"))
    CONVERSIONS["YOCTOSECOND:DECISECOND"] = \
        Mul(Pow(10, 23), Sym("yoctos"))
    CONVERSIONS["YOCTOSECOND:EXASECOND"] = \
        Mul(Pow(10, 42), Sym("yoctos"))
    CONVERSIONS["YOCTOSECOND:FEMTOSECOND"] = \
        Mul(Pow(10, 9), Sym("yoctos"))
    CONVERSIONS["YOCTOSECOND:GIGASECOND"] = \
        Mul(Pow(10, 33), Sym("yoctos"))
    CONVERSIONS["YOCTOSECOND:HECTOSECOND"] = \
        Mul(Pow(10, 26), Sym("yoctos"))
    CONVERSIONS["YOCTOSECOND:HOUR"] = \
        Mul(Mul(Int(36), Pow(10, 26)), Sym("yoctos"))
    CONVERSIONS["YOCTOSECOND:KILOSECOND"] = \
        Mul(Pow(10, 27), Sym("yoctos"))
    CONVERSIONS["YOCTOSECOND:MEGASECOND"] = \
        Mul(Pow(10, 30), Sym("yoctos"))
    CONVERSIONS["YOCTOSECOND:MICROSECOND"] = \
        Mul(Pow(10, 18), Sym("yoctos"))
    CONVERSIONS["YOCTOSECOND:MILLISECOND"] = \
        Mul(Pow(10, 21), Sym("yoctos"))
    CONVERSIONS["YOCTOSECOND:MINUTE"] = \
        Mul(Mul(Int(6), Pow(10, 25)), Sym("yoctos"))
    CONVERSIONS["YOCTOSECOND:NANOSECOND"] = \
        Mul(Pow(10, 15), Sym("yoctos"))
    CONVERSIONS["YOCTOSECOND:PETASECOND"] = \
        Mul(Pow(10, 39), Sym("yoctos"))
    CONVERSIONS["YOCTOSECOND:PICOSECOND"] = \
        Mul(Pow(10, 12), Sym("yoctos"))
    CONVERSIONS["YOCTOSECOND:SECOND"] = \
        Mul(Pow(10, 24), Sym("yoctos"))
    CONVERSIONS["YOCTOSECOND:TERASECOND"] = \
        Mul(Pow(10, 36), Sym("yoctos"))
    CONVERSIONS["YOCTOSECOND:YOTTASECOND"] = \
        Mul(Pow(10, 48), Sym("yoctos"))
    CONVERSIONS["YOCTOSECOND:ZEPTOSECOND"] = \
        Mul(Int(1000), Sym("yoctos"))
    CONVERSIONS["YOCTOSECOND:ZETTASECOND"] = \
        Mul(Pow(10, 45), Sym("yoctos"))
    CONVERSIONS["YOTTASECOND:ATTOSECOND"] = \
        Mul(Rat(Int(1), Pow(10, 42)), Sym("yottas"))
    CONVERSIONS["YOTTASECOND:CENTISECOND"] = \
        Mul(Rat(Int(1), Pow(10, 26)), Sym("yottas"))
    CONVERSIONS["YOTTASECOND:DAY"] = \
        Mul(Rat(Int(27), Mul(Int(3125), Pow(10, 17))), Sym("yottas"))
    CONVERSIONS["YOTTASECOND:DECASECOND"] = \
        Mul(Rat(Int(1), Pow(10, 23)), Sym("yottas"))
    CONVERSIONS["YOTTASECOND:DECISECOND"] = \
        Mul(Rat(Int(1), Pow(10, 25)), Sym("yottas"))
    CONVERSIONS["YOTTASECOND:EXASECOND"] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("yottas"))
    CONVERSIONS["YOTTASECOND:FEMTOSECOND"] = \
        Mul(Rat(Int(1), Pow(10, 39)), Sym("yottas"))
    CONVERSIONS["YOTTASECOND:GIGASECOND"] = \
        Mul(Rat(Int(1), Pow(10, 15)), Sym("yottas"))
    CONVERSIONS["YOTTASECOND:HECTOSECOND"] = \
        Mul(Rat(Int(1), Pow(10, 22)), Sym("yottas"))
    CONVERSIONS["YOTTASECOND:HOUR"] = \
        Mul(Rat(Int(9), Mul(Int(25), Pow(10, 20))), Sym("yottas"))
    CONVERSIONS["YOTTASECOND:KILOSECOND"] = \
        Mul(Rat(Int(1), Pow(10, 21)), Sym("yottas"))
    CONVERSIONS["YOTTASECOND:MEGASECOND"] = \
        Mul(Rat(Int(1), Pow(10, 18)), Sym("yottas"))
    CONVERSIONS["YOTTASECOND:MICROSECOND"] = \
        Mul(Rat(Int(1), Pow(10, 30)), Sym("yottas"))
    CONVERSIONS["YOTTASECOND:MILLISECOND"] = \
        Mul(Rat(Int(1), Pow(10, 27)), Sym("yottas"))
    CONVERSIONS["YOTTASECOND:MINUTE"] = \
        Mul(Rat(Int(3), Mul(Int(5), Pow(10, 22))), Sym("yottas"))
    CONVERSIONS["YOTTASECOND:NANOSECOND"] = \
        Mul(Rat(Int(1), Pow(10, 33)), Sym("yottas"))
    CONVERSIONS["YOTTASECOND:PETASECOND"] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("yottas"))
    CONVERSIONS["YOTTASECOND:PICOSECOND"] = \
        Mul(Rat(Int(1), Pow(10, 36)), Sym("yottas"))
    CONVERSIONS["YOTTASECOND:SECOND"] = \
        Mul(Rat(Int(1), Pow(10, 24)), Sym("yottas"))
    CONVERSIONS["YOTTASECOND:TERASECOND"] = \
        Mul(Rat(Int(1), Pow(10, 12)), Sym("yottas"))
    CONVERSIONS["YOTTASECOND:YOCTOSECOND"] = \
        Mul(Rat(Int(1), Pow(10, 48)), Sym("yottas"))
    CONVERSIONS["YOTTASECOND:ZEPTOSECOND"] = \
        Mul(Rat(Int(1), Pow(10, 45)), Sym("yottas"))
    CONVERSIONS["YOTTASECOND:ZETTASECOND"] = \
        Mul(Rat(Int(1), Int(1000)), Sym("yottas"))
    CONVERSIONS["ZEPTOSECOND:ATTOSECOND"] = \
        Mul(Int(1000), Sym("zeptos"))
    CONVERSIONS["ZEPTOSECOND:CENTISECOND"] = \
        Mul(Pow(10, 19), Sym("zeptos"))
    CONVERSIONS["ZEPTOSECOND:DAY"] = \
        Mul(Mul(Int(864), Pow(10, 23)), Sym("zeptos"))
    CONVERSIONS["ZEPTOSECOND:DECASECOND"] = \
        Mul(Pow(10, 22), Sym("zeptos"))
    CONVERSIONS["ZEPTOSECOND:DECISECOND"] = \
        Mul(Pow(10, 20), Sym("zeptos"))
    CONVERSIONS["ZEPTOSECOND:EXASECOND"] = \
        Mul(Pow(10, 39), Sym("zeptos"))
    CONVERSIONS["ZEPTOSECOND:FEMTOSECOND"] = \
        Mul(Pow(10, 6), Sym("zeptos"))
    CONVERSIONS["ZEPTOSECOND:GIGASECOND"] = \
        Mul(Pow(10, 30), Sym("zeptos"))
    CONVERSIONS["ZEPTOSECOND:HECTOSECOND"] = \
        Mul(Pow(10, 23), Sym("zeptos"))
    CONVERSIONS["ZEPTOSECOND:HOUR"] = \
        Mul(Mul(Int(36), Pow(10, 23)), Sym("zeptos"))
    CONVERSIONS["ZEPTOSECOND:KILOSECOND"] = \
        Mul(Pow(10, 24), Sym("zeptos"))
    CONVERSIONS["ZEPTOSECOND:MEGASECOND"] = \
        Mul(Pow(10, 27), Sym("zeptos"))
    CONVERSIONS["ZEPTOSECOND:MICROSECOND"] = \
        Mul(Pow(10, 15), Sym("zeptos"))
    CONVERSIONS["ZEPTOSECOND:MILLISECOND"] = \
        Mul(Pow(10, 18), Sym("zeptos"))
    CONVERSIONS["ZEPTOSECOND:MINUTE"] = \
        Mul(Mul(Int(6), Pow(10, 22)), Sym("zeptos"))
    CONVERSIONS["ZEPTOSECOND:NANOSECOND"] = \
        Mul(Pow(10, 12), Sym("zeptos"))
    CONVERSIONS["ZEPTOSECOND:PETASECOND"] = \
        Mul(Pow(10, 36), Sym("zeptos"))
    CONVERSIONS["ZEPTOSECOND:PICOSECOND"] = \
        Mul(Pow(10, 9), Sym("zeptos"))
    CONVERSIONS["ZEPTOSECOND:SECOND"] = \
        Mul(Pow(10, 21), Sym("zeptos"))
    CONVERSIONS["ZEPTOSECOND:TERASECOND"] = \
        Mul(Pow(10, 33), Sym("zeptos"))
    CONVERSIONS["ZEPTOSECOND:YOCTOSECOND"] = \
        Mul(Rat(Int(1), Int(1000)), Sym("zeptos"))
    CONVERSIONS["ZEPTOSECOND:YOTTASECOND"] = \
        Mul(Pow(10, 45), Sym("zeptos"))
    CONVERSIONS["ZEPTOSECOND:ZETTASECOND"] = \
        Mul(Pow(10, 42), Sym("zeptos"))
    CONVERSIONS["ZETTASECOND:ATTOSECOND"] = \
        Mul(Rat(Int(1), Pow(10, 39)), Sym("zettas"))
    CONVERSIONS["ZETTASECOND:CENTISECOND"] = \
        Mul(Rat(Int(1), Pow(10, 23)), Sym("zettas"))
    CONVERSIONS["ZETTASECOND:DAY"] = \
        Mul(Rat(Int(27), Mul(Int(3125), Pow(10, 14))), Sym("zettas"))
    CONVERSIONS["ZETTASECOND:DECASECOND"] = \
        Mul(Rat(Int(1), Pow(10, 20)), Sym("zettas"))
    CONVERSIONS["ZETTASECOND:DECISECOND"] = \
        Mul(Rat(Int(1), Pow(10, 22)), Sym("zettas"))
    CONVERSIONS["ZETTASECOND:EXASECOND"] = \
        Mul(Rat(Int(1), Int(1000)), Sym("zettas"))
    CONVERSIONS["ZETTASECOND:FEMTOSECOND"] = \
        Mul(Rat(Int(1), Pow(10, 36)), Sym("zettas"))
    CONVERSIONS["ZETTASECOND:GIGASECOND"] = \
        Mul(Rat(Int(1), Pow(10, 12)), Sym("zettas"))
    CONVERSIONS["ZETTASECOND:HECTOSECOND"] = \
        Mul(Rat(Int(1), Pow(10, 19)), Sym("zettas"))
    CONVERSIONS["ZETTASECOND:HOUR"] = \
        Mul(Rat(Int(9), Mul(Int(25), Pow(10, 17))), Sym("zettas"))
    CONVERSIONS["ZETTASECOND:KILOSECOND"] = \
        Mul(Rat(Int(1), Pow(10, 18)), Sym("zettas"))
    CONVERSIONS["ZETTASECOND:MEGASECOND"] = \
        Mul(Rat(Int(1), Pow(10, 15)), Sym("zettas"))
    CONVERSIONS["ZETTASECOND:MICROSECOND"] = \
        Mul(Rat(Int(1), Pow(10, 27)), Sym("zettas"))
    CONVERSIONS["ZETTASECOND:MILLISECOND"] = \
        Mul(Rat(Int(1), Pow(10, 24)), Sym("zettas"))
    CONVERSIONS["ZETTASECOND:MINUTE"] = \
        Mul(Rat(Int(3), Mul(Int(5), Pow(10, 19))), Sym("zettas"))
    CONVERSIONS["ZETTASECOND:NANOSECOND"] = \
        Mul(Rat(Int(1), Pow(10, 30)), Sym("zettas"))
    CONVERSIONS["ZETTASECOND:PETASECOND"] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("zettas"))
    CONVERSIONS["ZETTASECOND:PICOSECOND"] = \
        Mul(Rat(Int(1), Pow(10, 33)), Sym("zettas"))
    CONVERSIONS["ZETTASECOND:SECOND"] = \
        Mul(Rat(Int(1), Pow(10, 21)), Sym("zettas"))
    CONVERSIONS["ZETTASECOND:TERASECOND"] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("zettas"))
    CONVERSIONS["ZETTASECOND:YOCTOSECOND"] = \
        Mul(Rat(Int(1), Pow(10, 45)), Sym("zettas"))
    CONVERSIONS["ZETTASECOND:YOTTASECOND"] = \
        Mul(Int(1000), Sym("zettas"))
    CONVERSIONS["ZETTASECOND:ZEPTOSECOND"] = \
        Mul(Rat(Int(1), Pow(10, 42)), Sym("zettas"))

    SYMBOLS = dict()
    SYMBOLS["ATOOSECOND"] = "as"
    SYMBOLS["CENTISECOND"] = "cs"
    SYMBOLS["DAY"] = "d"
    SYMBOLS["DECASECOND"] = "das"
    SYMBOLS["DECISECOND"] = "ds"
    SYMBOLS["EXASECOND"] = "Es"
    SYMBOLS["FEMTOSECOND"] = "fs"
    SYMBOLS["GIGASECOND"] = "Gs"
    SYMBOLS["HECTOSECOND"] = "hs"
    SYMBOLS["HOUR"] = "h"
    SYMBOLS["KILOSECOND"] = "ks"
    SYMBOLS["MEGASECOND"] = "Ms"
    SYMBOLS["MICROSECOND"] = "µs"
    SYMBOLS["MILLISECOND"] = "ms"
    SYMBOLS["MINUTE"] = "min"
    SYMBOLS["NANOSECOND"] = "ns"
    SYMBOLS["PETASECOND"] = "Ps"
    SYMBOLS["PICOSECOND"] = "ps"
    SYMBOLS["SECOND"] = "s"
    SYMBOLS["TERASECOND"] = "Ts"
    SYMBOLS["YOCTOSECOND"] = "ys"
    SYMBOLS["YOTTASECOND"] = "Ys"
    SYMBOLS["ZEPTOSECOND"] = "zs"
    SYMBOLS["ZETTASECOND"] = "Zs"

    def __init__(self, value=None, unit=None):
        _omero_model.Time.__init__(self)
        if isinstance(value, _omero_model.TimeI):
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
                self.setUnit(getattr(UnitsTime, str(target)))
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
        return TimeI.SYMBOLS.get(str(unit))

    def setUnit(self, unit, current=None):
        self._unit = unit

    def setValue(self, value, current=None):
        self._value = value

    def __str__(self):
        return self._base_string(self.getValue(), self.getUnit())

_omero_model.TimeI = TimeI
