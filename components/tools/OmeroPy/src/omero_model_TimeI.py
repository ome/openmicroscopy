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

from omero.model.conversions import Add  # nopep8
from omero.model.conversions import Int  # nopep8
from omero.model.conversions import Mul  # nopep8
from omero.model.conversions import Pow  # nopep8
from omero.model.conversions import Rat  # nopep8
from omero.model.conversions import Sym  # nopep8


class TimeI(_omero_model.Time, UnitBase):

    CONVERSIONS = dict()
    CONVERSIONS["ATTOSECOND:CENTISECOND"] = \
        Mul(Pow(10, 16), Sym("attos"))  # nopep8
    CONVERSIONS["ATTOSECOND:DAY"] = \
        Mul(Mul(Int(864), Pow(10, 20)), Sym("attos"))  # nopep8
    CONVERSIONS["ATTOSECOND:DECASECOND"] = \
        Mul(Pow(10, 19), Sym("attos"))  # nopep8
    CONVERSIONS["ATTOSECOND:DECISECOND"] = \
        Mul(Pow(10, 17), Sym("attos"))  # nopep8
    CONVERSIONS["ATTOSECOND:EXASECOND"] = \
        Mul(Pow(10, 36), Sym("attos"))  # nopep8
    CONVERSIONS["ATTOSECOND:FEMTOSECOND"] = \
        Mul(Int(1000), Sym("attos"))  # nopep8
    CONVERSIONS["ATTOSECOND:GIGASECOND"] = \
        Mul(Pow(10, 27), Sym("attos"))  # nopep8
    CONVERSIONS["ATTOSECOND:HECTOSECOND"] = \
        Mul(Pow(10, 20), Sym("attos"))  # nopep8
    CONVERSIONS["ATTOSECOND:HOUR"] = \
        Mul(Mul(Int(36), Pow(10, 20)), Sym("attos"))  # nopep8
    CONVERSIONS["ATTOSECOND:KILOSECOND"] = \
        Mul(Pow(10, 21), Sym("attos"))  # nopep8
    CONVERSIONS["ATTOSECOND:MEGASECOND"] = \
        Mul(Pow(10, 24), Sym("attos"))  # nopep8
    CONVERSIONS["ATTOSECOND:MICROSECOND"] = \
        Mul(Pow(10, 12), Sym("attos"))  # nopep8
    CONVERSIONS["ATTOSECOND:MILLISECOND"] = \
        Mul(Pow(10, 15), Sym("attos"))  # nopep8
    CONVERSIONS["ATTOSECOND:MINUTE"] = \
        Mul(Mul(Int(6), Pow(10, 19)), Sym("attos"))  # nopep8
    CONVERSIONS["ATTOSECOND:NANOSECOND"] = \
        Mul(Pow(10, 9), Sym("attos"))  # nopep8
    CONVERSIONS["ATTOSECOND:PETASECOND"] = \
        Mul(Pow(10, 33), Sym("attos"))  # nopep8
    CONVERSIONS["ATTOSECOND:PICOSECOND"] = \
        Mul(Pow(10, 6), Sym("attos"))  # nopep8
    CONVERSIONS["ATTOSECOND:SECOND"] = \
        Mul(Pow(10, 18), Sym("attos"))  # nopep8
    CONVERSIONS["ATTOSECOND:TERASECOND"] = \
        Mul(Pow(10, 30), Sym("attos"))  # nopep8
    CONVERSIONS["ATTOSECOND:YOCTOSECOND"] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("attos"))  # nopep8
    CONVERSIONS["ATTOSECOND:YOTTASECOND"] = \
        Mul(Pow(10, 42), Sym("attos"))  # nopep8
    CONVERSIONS["ATTOSECOND:ZEPTOSECOND"] = \
        Mul(Rat(Int(1), Int(1000)), Sym("attos"))  # nopep8
    CONVERSIONS["ATTOSECOND:ZETTASECOND"] = \
        Mul(Pow(10, 39), Sym("attos"))  # nopep8
    CONVERSIONS["CENTISECOND:ATTOSECOND"] = \
        Mul(Rat(Int(1), Pow(10, 16)), Sym("centis"))  # nopep8
    CONVERSIONS["CENTISECOND:DAY"] = \
        Mul(Mul(Int(864), Pow(10, 4)), Sym("centis"))  # nopep8
    CONVERSIONS["CENTISECOND:DECASECOND"] = \
        Mul(Int(1000), Sym("centis"))  # nopep8
    CONVERSIONS["CENTISECOND:DECISECOND"] = \
        Mul(Int(10), Sym("centis"))  # nopep8
    CONVERSIONS["CENTISECOND:EXASECOND"] = \
        Mul(Pow(10, 20), Sym("centis"))  # nopep8
    CONVERSIONS["CENTISECOND:FEMTOSECOND"] = \
        Mul(Rat(Int(1), Pow(10, 13)), Sym("centis"))  # nopep8
    CONVERSIONS["CENTISECOND:GIGASECOND"] = \
        Mul(Pow(10, 11), Sym("centis"))  # nopep8
    CONVERSIONS["CENTISECOND:HECTOSECOND"] = \
        Mul(Pow(10, 4), Sym("centis"))  # nopep8
    CONVERSIONS["CENTISECOND:HOUR"] = \
        Mul(Mul(Int(36), Pow(10, 4)), Sym("centis"))  # nopep8
    CONVERSIONS["CENTISECOND:KILOSECOND"] = \
        Mul(Pow(10, 5), Sym("centis"))  # nopep8
    CONVERSIONS["CENTISECOND:MEGASECOND"] = \
        Mul(Pow(10, 8), Sym("centis"))  # nopep8
    CONVERSIONS["CENTISECOND:MICROSECOND"] = \
        Mul(Rat(Int(1), Pow(10, 4)), Sym("centis"))  # nopep8
    CONVERSIONS["CENTISECOND:MILLISECOND"] = \
        Mul(Rat(Int(1), Int(10)), Sym("centis"))  # nopep8
    CONVERSIONS["CENTISECOND:MINUTE"] = \
        Mul(Int(6000), Sym("centis"))  # nopep8
    CONVERSIONS["CENTISECOND:NANOSECOND"] = \
        Mul(Rat(Int(1), Pow(10, 7)), Sym("centis"))  # nopep8
    CONVERSIONS["CENTISECOND:PETASECOND"] = \
        Mul(Pow(10, 17), Sym("centis"))  # nopep8
    CONVERSIONS["CENTISECOND:PICOSECOND"] = \
        Mul(Rat(Int(1), Pow(10, 10)), Sym("centis"))  # nopep8
    CONVERSIONS["CENTISECOND:SECOND"] = \
        Mul(Int(100), Sym("centis"))  # nopep8
    CONVERSIONS["CENTISECOND:TERASECOND"] = \
        Mul(Pow(10, 14), Sym("centis"))  # nopep8
    CONVERSIONS["CENTISECOND:YOCTOSECOND"] = \
        Mul(Rat(Int(1), Pow(10, 22)), Sym("centis"))  # nopep8
    CONVERSIONS["CENTISECOND:YOTTASECOND"] = \
        Mul(Pow(10, 26), Sym("centis"))  # nopep8
    CONVERSIONS["CENTISECOND:ZEPTOSECOND"] = \
        Mul(Rat(Int(1), Pow(10, 19)), Sym("centis"))  # nopep8
    CONVERSIONS["CENTISECOND:ZETTASECOND"] = \
        Mul(Pow(10, 23), Sym("centis"))  # nopep8
    CONVERSIONS["DAY:ATTOSECOND"] = \
        Mul(Rat(Int(1), Mul(Int(864), Pow(10, 20))), Sym("d"))  # nopep8
    CONVERSIONS["DAY:CENTISECOND"] = \
        Mul(Rat(Int(1), Mul(Int(864), Pow(10, 4))), Sym("d"))  # nopep8
    CONVERSIONS["DAY:DECASECOND"] = \
        Mul(Rat(Int(1), Int(8640)), Sym("d"))  # nopep8
    CONVERSIONS["DAY:DECISECOND"] = \
        Mul(Rat(Int(1), Int(864000)), Sym("d"))  # nopep8
    CONVERSIONS["DAY:EXASECOND"] = \
        Mul(Rat(Mul(Int(3125), Pow(10, 11)), Int(27)), Sym("d"))  # nopep8
    CONVERSIONS["DAY:FEMTOSECOND"] = \
        Mul(Rat(Int(1), Mul(Int(864), Pow(10, 17))), Sym("d"))  # nopep8
    CONVERSIONS["DAY:GIGASECOND"] = \
        Mul(Rat(Int(312500), Int(27)), Sym("d"))  # nopep8
    CONVERSIONS["DAY:HECTOSECOND"] = \
        Mul(Rat(Int(1), Int(864)), Sym("d"))  # nopep8
    CONVERSIONS["DAY:HOUR"] = \
        Mul(Rat(Int(1), Int(24)), Sym("d"))  # nopep8
    CONVERSIONS["DAY:KILOSECOND"] = \
        Mul(Rat(Int(5), Int(432)), Sym("d"))  # nopep8
    CONVERSIONS["DAY:MEGASECOND"] = \
        Mul(Rat(Int(625), Int(54)), Sym("d"))  # nopep8
    CONVERSIONS["DAY:MICROSECOND"] = \
        Mul(Rat(Int(1), Mul(Int(864), Pow(10, 8))), Sym("d"))  # nopep8
    CONVERSIONS["DAY:MILLISECOND"] = \
        Mul(Rat(Int(1), Mul(Int(864), Pow(10, 5))), Sym("d"))  # nopep8
    CONVERSIONS["DAY:MINUTE"] = \
        Mul(Rat(Int(1), Int(1440)), Sym("d"))  # nopep8
    CONVERSIONS["DAY:NANOSECOND"] = \
        Mul(Rat(Int(1), Mul(Int(864), Pow(10, 11))), Sym("d"))  # nopep8
    CONVERSIONS["DAY:PETASECOND"] = \
        Mul(Rat(Mul(Int(3125), Pow(10, 8)), Int(27)), Sym("d"))  # nopep8
    CONVERSIONS["DAY:PICOSECOND"] = \
        Mul(Rat(Int(1), Mul(Int(864), Pow(10, 14))), Sym("d"))  # nopep8
    CONVERSIONS["DAY:SECOND"] = \
        Mul(Rat(Int(1), Int(86400)), Sym("d"))  # nopep8
    CONVERSIONS["DAY:TERASECOND"] = \
        Mul(Rat(Mul(Int(3125), Pow(10, 5)), Int(27)), Sym("d"))  # nopep8
    CONVERSIONS["DAY:YOCTOSECOND"] = \
        Mul(Rat(Int(1), Mul(Int(864), Pow(10, 26))), Sym("d"))  # nopep8
    CONVERSIONS["DAY:YOTTASECOND"] = \
        Mul(Rat(Mul(Int(3125), Pow(10, 17)), Int(27)), Sym("d"))  # nopep8
    CONVERSIONS["DAY:ZEPTOSECOND"] = \
        Mul(Rat(Int(1), Mul(Int(864), Pow(10, 23))), Sym("d"))  # nopep8
    CONVERSIONS["DAY:ZETTASECOND"] = \
        Mul(Rat(Mul(Int(3125), Pow(10, 14)), Int(27)), Sym("d"))  # nopep8
    CONVERSIONS["DECASECOND:ATTOSECOND"] = \
        Mul(Rat(Int(1), Pow(10, 19)), Sym("decas"))  # nopep8
    CONVERSIONS["DECASECOND:CENTISECOND"] = \
        Mul(Rat(Int(1), Int(1000)), Sym("decas"))  # nopep8
    CONVERSIONS["DECASECOND:DAY"] = \
        Mul(Int(8640), Sym("decas"))  # nopep8
    CONVERSIONS["DECASECOND:DECISECOND"] = \
        Mul(Rat(Int(1), Int(100)), Sym("decas"))  # nopep8
    CONVERSIONS["DECASECOND:EXASECOND"] = \
        Mul(Pow(10, 17), Sym("decas"))  # nopep8
    CONVERSIONS["DECASECOND:FEMTOSECOND"] = \
        Mul(Rat(Int(1), Pow(10, 16)), Sym("decas"))  # nopep8
    CONVERSIONS["DECASECOND:GIGASECOND"] = \
        Mul(Pow(10, 8), Sym("decas"))  # nopep8
    CONVERSIONS["DECASECOND:HECTOSECOND"] = \
        Mul(Int(10), Sym("decas"))  # nopep8
    CONVERSIONS["DECASECOND:HOUR"] = \
        Mul(Int(360), Sym("decas"))  # nopep8
    CONVERSIONS["DECASECOND:KILOSECOND"] = \
        Mul(Int(100), Sym("decas"))  # nopep8
    CONVERSIONS["DECASECOND:MEGASECOND"] = \
        Mul(Pow(10, 5), Sym("decas"))  # nopep8
    CONVERSIONS["DECASECOND:MICROSECOND"] = \
        Mul(Rat(Int(1), Pow(10, 7)), Sym("decas"))  # nopep8
    CONVERSIONS["DECASECOND:MILLISECOND"] = \
        Mul(Rat(Int(1), Pow(10, 4)), Sym("decas"))  # nopep8
    CONVERSIONS["DECASECOND:MINUTE"] = \
        Mul(Int(6), Sym("decas"))  # nopep8
    CONVERSIONS["DECASECOND:NANOSECOND"] = \
        Mul(Rat(Int(1), Pow(10, 10)), Sym("decas"))  # nopep8
    CONVERSIONS["DECASECOND:PETASECOND"] = \
        Mul(Pow(10, 14), Sym("decas"))  # nopep8
    CONVERSIONS["DECASECOND:PICOSECOND"] = \
        Mul(Rat(Int(1), Pow(10, 13)), Sym("decas"))  # nopep8
    CONVERSIONS["DECASECOND:SECOND"] = \
        Mul(Rat(Int(1), Int(10)), Sym("decas"))  # nopep8
    CONVERSIONS["DECASECOND:TERASECOND"] = \
        Mul(Pow(10, 11), Sym("decas"))  # nopep8
    CONVERSIONS["DECASECOND:YOCTOSECOND"] = \
        Mul(Rat(Int(1), Pow(10, 25)), Sym("decas"))  # nopep8
    CONVERSIONS["DECASECOND:YOTTASECOND"] = \
        Mul(Pow(10, 23), Sym("decas"))  # nopep8
    CONVERSIONS["DECASECOND:ZEPTOSECOND"] = \
        Mul(Rat(Int(1), Pow(10, 22)), Sym("decas"))  # nopep8
    CONVERSIONS["DECASECOND:ZETTASECOND"] = \
        Mul(Pow(10, 20), Sym("decas"))  # nopep8
    CONVERSIONS["DECISECOND:ATTOSECOND"] = \
        Mul(Rat(Int(1), Pow(10, 17)), Sym("decis"))  # nopep8
    CONVERSIONS["DECISECOND:CENTISECOND"] = \
        Mul(Rat(Int(1), Int(10)), Sym("decis"))  # nopep8
    CONVERSIONS["DECISECOND:DAY"] = \
        Mul(Int(864000), Sym("decis"))  # nopep8
    CONVERSIONS["DECISECOND:DECASECOND"] = \
        Mul(Int(100), Sym("decis"))  # nopep8
    CONVERSIONS["DECISECOND:EXASECOND"] = \
        Mul(Pow(10, 19), Sym("decis"))  # nopep8
    CONVERSIONS["DECISECOND:FEMTOSECOND"] = \
        Mul(Rat(Int(1), Pow(10, 14)), Sym("decis"))  # nopep8
    CONVERSIONS["DECISECOND:GIGASECOND"] = \
        Mul(Pow(10, 10), Sym("decis"))  # nopep8
    CONVERSIONS["DECISECOND:HECTOSECOND"] = \
        Mul(Int(1000), Sym("decis"))  # nopep8
    CONVERSIONS["DECISECOND:HOUR"] = \
        Mul(Int(36000), Sym("decis"))  # nopep8
    CONVERSIONS["DECISECOND:KILOSECOND"] = \
        Mul(Pow(10, 4), Sym("decis"))  # nopep8
    CONVERSIONS["DECISECOND:MEGASECOND"] = \
        Mul(Pow(10, 7), Sym("decis"))  # nopep8
    CONVERSIONS["DECISECOND:MICROSECOND"] = \
        Mul(Rat(Int(1), Pow(10, 5)), Sym("decis"))  # nopep8
    CONVERSIONS["DECISECOND:MILLISECOND"] = \
        Mul(Rat(Int(1), Int(100)), Sym("decis"))  # nopep8
    CONVERSIONS["DECISECOND:MINUTE"] = \
        Mul(Int(600), Sym("decis"))  # nopep8
    CONVERSIONS["DECISECOND:NANOSECOND"] = \
        Mul(Rat(Int(1), Pow(10, 8)), Sym("decis"))  # nopep8
    CONVERSIONS["DECISECOND:PETASECOND"] = \
        Mul(Pow(10, 16), Sym("decis"))  # nopep8
    CONVERSIONS["DECISECOND:PICOSECOND"] = \
        Mul(Rat(Int(1), Pow(10, 11)), Sym("decis"))  # nopep8
    CONVERSIONS["DECISECOND:SECOND"] = \
        Mul(Int(10), Sym("decis"))  # nopep8
    CONVERSIONS["DECISECOND:TERASECOND"] = \
        Mul(Pow(10, 13), Sym("decis"))  # nopep8
    CONVERSIONS["DECISECOND:YOCTOSECOND"] = \
        Mul(Rat(Int(1), Pow(10, 23)), Sym("decis"))  # nopep8
    CONVERSIONS["DECISECOND:YOTTASECOND"] = \
        Mul(Pow(10, 25), Sym("decis"))  # nopep8
    CONVERSIONS["DECISECOND:ZEPTOSECOND"] = \
        Mul(Rat(Int(1), Pow(10, 20)), Sym("decis"))  # nopep8
    CONVERSIONS["DECISECOND:ZETTASECOND"] = \
        Mul(Pow(10, 22), Sym("decis"))  # nopep8
    CONVERSIONS["EXASECOND:ATTOSECOND"] = \
        Mul(Rat(Int(1), Pow(10, 36)), Sym("exas"))  # nopep8
    CONVERSIONS["EXASECOND:CENTISECOND"] = \
        Mul(Rat(Int(1), Pow(10, 20)), Sym("exas"))  # nopep8
    CONVERSIONS["EXASECOND:DAY"] = \
        Mul(Rat(Int(27), Mul(Int(3125), Pow(10, 11))), Sym("exas"))  # nopep8
    CONVERSIONS["EXASECOND:DECASECOND"] = \
        Mul(Rat(Int(1), Pow(10, 17)), Sym("exas"))  # nopep8
    CONVERSIONS["EXASECOND:DECISECOND"] = \
        Mul(Rat(Int(1), Pow(10, 19)), Sym("exas"))  # nopep8
    CONVERSIONS["EXASECOND:FEMTOSECOND"] = \
        Mul(Rat(Int(1), Pow(10, 33)), Sym("exas"))  # nopep8
    CONVERSIONS["EXASECOND:GIGASECOND"] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("exas"))  # nopep8
    CONVERSIONS["EXASECOND:HECTOSECOND"] = \
        Mul(Rat(Int(1), Pow(10, 16)), Sym("exas"))  # nopep8
    CONVERSIONS["EXASECOND:HOUR"] = \
        Mul(Rat(Int(9), Mul(Int(25), Pow(10, 14))), Sym("exas"))  # nopep8
    CONVERSIONS["EXASECOND:KILOSECOND"] = \
        Mul(Rat(Int(1), Pow(10, 15)), Sym("exas"))  # nopep8
    CONVERSIONS["EXASECOND:MEGASECOND"] = \
        Mul(Rat(Int(1), Pow(10, 12)), Sym("exas"))  # nopep8
    CONVERSIONS["EXASECOND:MICROSECOND"] = \
        Mul(Rat(Int(1), Pow(10, 24)), Sym("exas"))  # nopep8
    CONVERSIONS["EXASECOND:MILLISECOND"] = \
        Mul(Rat(Int(1), Pow(10, 21)), Sym("exas"))  # nopep8
    CONVERSIONS["EXASECOND:MINUTE"] = \
        Mul(Rat(Int(3), Mul(Int(5), Pow(10, 16))), Sym("exas"))  # nopep8
    CONVERSIONS["EXASECOND:NANOSECOND"] = \
        Mul(Rat(Int(1), Pow(10, 27)), Sym("exas"))  # nopep8
    CONVERSIONS["EXASECOND:PETASECOND"] = \
        Mul(Rat(Int(1), Int(1000)), Sym("exas"))  # nopep8
    CONVERSIONS["EXASECOND:PICOSECOND"] = \
        Mul(Rat(Int(1), Pow(10, 30)), Sym("exas"))  # nopep8
    CONVERSIONS["EXASECOND:SECOND"] = \
        Mul(Rat(Int(1), Pow(10, 18)), Sym("exas"))  # nopep8
    CONVERSIONS["EXASECOND:TERASECOND"] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("exas"))  # nopep8
    CONVERSIONS["EXASECOND:YOCTOSECOND"] = \
        Mul(Rat(Int(1), Pow(10, 42)), Sym("exas"))  # nopep8
    CONVERSIONS["EXASECOND:YOTTASECOND"] = \
        Mul(Pow(10, 6), Sym("exas"))  # nopep8
    CONVERSIONS["EXASECOND:ZEPTOSECOND"] = \
        Mul(Rat(Int(1), Pow(10, 39)), Sym("exas"))  # nopep8
    CONVERSIONS["EXASECOND:ZETTASECOND"] = \
        Mul(Int(1000), Sym("exas"))  # nopep8
    CONVERSIONS["FEMTOSECOND:ATTOSECOND"] = \
        Mul(Rat(Int(1), Int(1000)), Sym("femtos"))  # nopep8
    CONVERSIONS["FEMTOSECOND:CENTISECOND"] = \
        Mul(Pow(10, 13), Sym("femtos"))  # nopep8
    CONVERSIONS["FEMTOSECOND:DAY"] = \
        Mul(Mul(Int(864), Pow(10, 17)), Sym("femtos"))  # nopep8
    CONVERSIONS["FEMTOSECOND:DECASECOND"] = \
        Mul(Pow(10, 16), Sym("femtos"))  # nopep8
    CONVERSIONS["FEMTOSECOND:DECISECOND"] = \
        Mul(Pow(10, 14), Sym("femtos"))  # nopep8
    CONVERSIONS["FEMTOSECOND:EXASECOND"] = \
        Mul(Pow(10, 33), Sym("femtos"))  # nopep8
    CONVERSIONS["FEMTOSECOND:GIGASECOND"] = \
        Mul(Pow(10, 24), Sym("femtos"))  # nopep8
    CONVERSIONS["FEMTOSECOND:HECTOSECOND"] = \
        Mul(Pow(10, 17), Sym("femtos"))  # nopep8
    CONVERSIONS["FEMTOSECOND:HOUR"] = \
        Mul(Mul(Int(36), Pow(10, 17)), Sym("femtos"))  # nopep8
    CONVERSIONS["FEMTOSECOND:KILOSECOND"] = \
        Mul(Pow(10, 18), Sym("femtos"))  # nopep8
    CONVERSIONS["FEMTOSECOND:MEGASECOND"] = \
        Mul(Pow(10, 21), Sym("femtos"))  # nopep8
    CONVERSIONS["FEMTOSECOND:MICROSECOND"] = \
        Mul(Pow(10, 9), Sym("femtos"))  # nopep8
    CONVERSIONS["FEMTOSECOND:MILLISECOND"] = \
        Mul(Pow(10, 12), Sym("femtos"))  # nopep8
    CONVERSIONS["FEMTOSECOND:MINUTE"] = \
        Mul(Mul(Int(6), Pow(10, 16)), Sym("femtos"))  # nopep8
    CONVERSIONS["FEMTOSECOND:NANOSECOND"] = \
        Mul(Pow(10, 6), Sym("femtos"))  # nopep8
    CONVERSIONS["FEMTOSECOND:PETASECOND"] = \
        Mul(Pow(10, 30), Sym("femtos"))  # nopep8
    CONVERSIONS["FEMTOSECOND:PICOSECOND"] = \
        Mul(Int(1000), Sym("femtos"))  # nopep8
    CONVERSIONS["FEMTOSECOND:SECOND"] = \
        Mul(Pow(10, 15), Sym("femtos"))  # nopep8
    CONVERSIONS["FEMTOSECOND:TERASECOND"] = \
        Mul(Pow(10, 27), Sym("femtos"))  # nopep8
    CONVERSIONS["FEMTOSECOND:YOCTOSECOND"] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("femtos"))  # nopep8
    CONVERSIONS["FEMTOSECOND:YOTTASECOND"] = \
        Mul(Pow(10, 39), Sym("femtos"))  # nopep8
    CONVERSIONS["FEMTOSECOND:ZEPTOSECOND"] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("femtos"))  # nopep8
    CONVERSIONS["FEMTOSECOND:ZETTASECOND"] = \
        Mul(Pow(10, 36), Sym("femtos"))  # nopep8
    CONVERSIONS["GIGASECOND:ATTOSECOND"] = \
        Mul(Rat(Int(1), Pow(10, 27)), Sym("gigas"))  # nopep8
    CONVERSIONS["GIGASECOND:CENTISECOND"] = \
        Mul(Rat(Int(1), Pow(10, 11)), Sym("gigas"))  # nopep8
    CONVERSIONS["GIGASECOND:DAY"] = \
        Mul(Rat(Int(27), Int(312500)), Sym("gigas"))  # nopep8
    CONVERSIONS["GIGASECOND:DECASECOND"] = \
        Mul(Rat(Int(1), Pow(10, 8)), Sym("gigas"))  # nopep8
    CONVERSIONS["GIGASECOND:DECISECOND"] = \
        Mul(Rat(Int(1), Pow(10, 10)), Sym("gigas"))  # nopep8
    CONVERSIONS["GIGASECOND:EXASECOND"] = \
        Mul(Pow(10, 9), Sym("gigas"))  # nopep8
    CONVERSIONS["GIGASECOND:FEMTOSECOND"] = \
        Mul(Rat(Int(1), Pow(10, 24)), Sym("gigas"))  # nopep8
    CONVERSIONS["GIGASECOND:HECTOSECOND"] = \
        Mul(Rat(Int(1), Pow(10, 7)), Sym("gigas"))  # nopep8
    CONVERSIONS["GIGASECOND:HOUR"] = \
        Mul(Rat(Int(9), Mul(Int(25), Pow(10, 5))), Sym("gigas"))  # nopep8
    CONVERSIONS["GIGASECOND:KILOSECOND"] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("gigas"))  # nopep8
    CONVERSIONS["GIGASECOND:MEGASECOND"] = \
        Mul(Rat(Int(1), Int(1000)), Sym("gigas"))  # nopep8
    CONVERSIONS["GIGASECOND:MICROSECOND"] = \
        Mul(Rat(Int(1), Pow(10, 15)), Sym("gigas"))  # nopep8
    CONVERSIONS["GIGASECOND:MILLISECOND"] = \
        Mul(Rat(Int(1), Pow(10, 12)), Sym("gigas"))  # nopep8
    CONVERSIONS["GIGASECOND:MINUTE"] = \
        Mul(Rat(Int(3), Mul(Int(5), Pow(10, 7))), Sym("gigas"))  # nopep8
    CONVERSIONS["GIGASECOND:NANOSECOND"] = \
        Mul(Rat(Int(1), Pow(10, 18)), Sym("gigas"))  # nopep8
    CONVERSIONS["GIGASECOND:PETASECOND"] = \
        Mul(Pow(10, 6), Sym("gigas"))  # nopep8
    CONVERSIONS["GIGASECOND:PICOSECOND"] = \
        Mul(Rat(Int(1), Pow(10, 21)), Sym("gigas"))  # nopep8
    CONVERSIONS["GIGASECOND:SECOND"] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("gigas"))  # nopep8
    CONVERSIONS["GIGASECOND:TERASECOND"] = \
        Mul(Int(1000), Sym("gigas"))  # nopep8
    CONVERSIONS["GIGASECOND:YOCTOSECOND"] = \
        Mul(Rat(Int(1), Pow(10, 33)), Sym("gigas"))  # nopep8
    CONVERSIONS["GIGASECOND:YOTTASECOND"] = \
        Mul(Pow(10, 15), Sym("gigas"))  # nopep8
    CONVERSIONS["GIGASECOND:ZEPTOSECOND"] = \
        Mul(Rat(Int(1), Pow(10, 30)), Sym("gigas"))  # nopep8
    CONVERSIONS["GIGASECOND:ZETTASECOND"] = \
        Mul(Pow(10, 12), Sym("gigas"))  # nopep8
    CONVERSIONS["HECTOSECOND:ATTOSECOND"] = \
        Mul(Rat(Int(1), Pow(10, 20)), Sym("hectos"))  # nopep8
    CONVERSIONS["HECTOSECOND:CENTISECOND"] = \
        Mul(Rat(Int(1), Pow(10, 4)), Sym("hectos"))  # nopep8
    CONVERSIONS["HECTOSECOND:DAY"] = \
        Mul(Int(864), Sym("hectos"))  # nopep8
    CONVERSIONS["HECTOSECOND:DECASECOND"] = \
        Mul(Rat(Int(1), Int(10)), Sym("hectos"))  # nopep8
    CONVERSIONS["HECTOSECOND:DECISECOND"] = \
        Mul(Rat(Int(1), Int(1000)), Sym("hectos"))  # nopep8
    CONVERSIONS["HECTOSECOND:EXASECOND"] = \
        Mul(Pow(10, 16), Sym("hectos"))  # nopep8
    CONVERSIONS["HECTOSECOND:FEMTOSECOND"] = \
        Mul(Rat(Int(1), Pow(10, 17)), Sym("hectos"))  # nopep8
    CONVERSIONS["HECTOSECOND:GIGASECOND"] = \
        Mul(Pow(10, 7), Sym("hectos"))  # nopep8
    CONVERSIONS["HECTOSECOND:HOUR"] = \
        Mul(Int(36), Sym("hectos"))  # nopep8
    CONVERSIONS["HECTOSECOND:KILOSECOND"] = \
        Mul(Int(10), Sym("hectos"))  # nopep8
    CONVERSIONS["HECTOSECOND:MEGASECOND"] = \
        Mul(Pow(10, 4), Sym("hectos"))  # nopep8
    CONVERSIONS["HECTOSECOND:MICROSECOND"] = \
        Mul(Rat(Int(1), Pow(10, 8)), Sym("hectos"))  # nopep8
    CONVERSIONS["HECTOSECOND:MILLISECOND"] = \
        Mul(Rat(Int(1), Pow(10, 5)), Sym("hectos"))  # nopep8
    CONVERSIONS["HECTOSECOND:MINUTE"] = \
        Mul(Rat(Int(3), Int(5)), Sym("hectos"))  # nopep8
    CONVERSIONS["HECTOSECOND:NANOSECOND"] = \
        Mul(Rat(Int(1), Pow(10, 11)), Sym("hectos"))  # nopep8
    CONVERSIONS["HECTOSECOND:PETASECOND"] = \
        Mul(Pow(10, 13), Sym("hectos"))  # nopep8
    CONVERSIONS["HECTOSECOND:PICOSECOND"] = \
        Mul(Rat(Int(1), Pow(10, 14)), Sym("hectos"))  # nopep8
    CONVERSIONS["HECTOSECOND:SECOND"] = \
        Mul(Rat(Int(1), Int(100)), Sym("hectos"))  # nopep8
    CONVERSIONS["HECTOSECOND:TERASECOND"] = \
        Mul(Pow(10, 10), Sym("hectos"))  # nopep8
    CONVERSIONS["HECTOSECOND:YOCTOSECOND"] = \
        Mul(Rat(Int(1), Pow(10, 26)), Sym("hectos"))  # nopep8
    CONVERSIONS["HECTOSECOND:YOTTASECOND"] = \
        Mul(Pow(10, 22), Sym("hectos"))  # nopep8
    CONVERSIONS["HECTOSECOND:ZEPTOSECOND"] = \
        Mul(Rat(Int(1), Pow(10, 23)), Sym("hectos"))  # nopep8
    CONVERSIONS["HECTOSECOND:ZETTASECOND"] = \
        Mul(Pow(10, 19), Sym("hectos"))  # nopep8
    CONVERSIONS["HOUR:ATTOSECOND"] = \
        Mul(Rat(Int(1), Mul(Int(36), Pow(10, 20))), Sym("h"))  # nopep8
    CONVERSIONS["HOUR:CENTISECOND"] = \
        Mul(Rat(Int(1), Mul(Int(36), Pow(10, 4))), Sym("h"))  # nopep8
    CONVERSIONS["HOUR:DAY"] = \
        Mul(Int(24), Sym("h"))  # nopep8
    CONVERSIONS["HOUR:DECASECOND"] = \
        Mul(Rat(Int(1), Int(360)), Sym("h"))  # nopep8
    CONVERSIONS["HOUR:DECISECOND"] = \
        Mul(Rat(Int(1), Int(36000)), Sym("h"))  # nopep8
    CONVERSIONS["HOUR:EXASECOND"] = \
        Mul(Rat(Mul(Int(25), Pow(10, 14)), Int(9)), Sym("h"))  # nopep8
    CONVERSIONS["HOUR:FEMTOSECOND"] = \
        Mul(Rat(Int(1), Mul(Int(36), Pow(10, 17))), Sym("h"))  # nopep8
    CONVERSIONS["HOUR:GIGASECOND"] = \
        Mul(Rat(Mul(Int(25), Pow(10, 5)), Int(9)), Sym("h"))  # nopep8
    CONVERSIONS["HOUR:HECTOSECOND"] = \
        Mul(Rat(Int(1), Int(36)), Sym("h"))  # nopep8
    CONVERSIONS["HOUR:KILOSECOND"] = \
        Mul(Rat(Int(5), Int(18)), Sym("h"))  # nopep8
    CONVERSIONS["HOUR:MEGASECOND"] = \
        Mul(Rat(Int(2500), Int(9)), Sym("h"))  # nopep8
    CONVERSIONS["HOUR:MICROSECOND"] = \
        Mul(Rat(Int(1), Mul(Int(36), Pow(10, 8))), Sym("h"))  # nopep8
    CONVERSIONS["HOUR:MILLISECOND"] = \
        Mul(Rat(Int(1), Mul(Int(36), Pow(10, 5))), Sym("h"))  # nopep8
    CONVERSIONS["HOUR:MINUTE"] = \
        Mul(Rat(Int(1), Int(60)), Sym("h"))  # nopep8
    CONVERSIONS["HOUR:NANOSECOND"] = \
        Mul(Rat(Int(1), Mul(Int(36), Pow(10, 11))), Sym("h"))  # nopep8
    CONVERSIONS["HOUR:PETASECOND"] = \
        Mul(Rat(Mul(Int(25), Pow(10, 11)), Int(9)), Sym("h"))  # nopep8
    CONVERSIONS["HOUR:PICOSECOND"] = \
        Mul(Rat(Int(1), Mul(Int(36), Pow(10, 14))), Sym("h"))  # nopep8
    CONVERSIONS["HOUR:SECOND"] = \
        Mul(Rat(Int(1), Int(3600)), Sym("h"))  # nopep8
    CONVERSIONS["HOUR:TERASECOND"] = \
        Mul(Rat(Mul(Int(25), Pow(10, 8)), Int(9)), Sym("h"))  # nopep8
    CONVERSIONS["HOUR:YOCTOSECOND"] = \
        Mul(Rat(Int(1), Mul(Int(36), Pow(10, 26))), Sym("h"))  # nopep8
    CONVERSIONS["HOUR:YOTTASECOND"] = \
        Mul(Rat(Mul(Int(25), Pow(10, 20)), Int(9)), Sym("h"))  # nopep8
    CONVERSIONS["HOUR:ZEPTOSECOND"] = \
        Mul(Rat(Int(1), Mul(Int(36), Pow(10, 23))), Sym("h"))  # nopep8
    CONVERSIONS["HOUR:ZETTASECOND"] = \
        Mul(Rat(Mul(Int(25), Pow(10, 17)), Int(9)), Sym("h"))  # nopep8
    CONVERSIONS["KILOSECOND:ATTOSECOND"] = \
        Mul(Rat(Int(1), Pow(10, 21)), Sym("kilos"))  # nopep8
    CONVERSIONS["KILOSECOND:CENTISECOND"] = \
        Mul(Rat(Int(1), Pow(10, 5)), Sym("kilos"))  # nopep8
    CONVERSIONS["KILOSECOND:DAY"] = \
        Mul(Rat(Int(432), Int(5)), Sym("kilos"))  # nopep8
    CONVERSIONS["KILOSECOND:DECASECOND"] = \
        Mul(Rat(Int(1), Int(100)), Sym("kilos"))  # nopep8
    CONVERSIONS["KILOSECOND:DECISECOND"] = \
        Mul(Rat(Int(1), Pow(10, 4)), Sym("kilos"))  # nopep8
    CONVERSIONS["KILOSECOND:EXASECOND"] = \
        Mul(Pow(10, 15), Sym("kilos"))  # nopep8
    CONVERSIONS["KILOSECOND:FEMTOSECOND"] = \
        Mul(Rat(Int(1), Pow(10, 18)), Sym("kilos"))  # nopep8
    CONVERSIONS["KILOSECOND:GIGASECOND"] = \
        Mul(Pow(10, 6), Sym("kilos"))  # nopep8
    CONVERSIONS["KILOSECOND:HECTOSECOND"] = \
        Mul(Rat(Int(1), Int(10)), Sym("kilos"))  # nopep8
    CONVERSIONS["KILOSECOND:HOUR"] = \
        Mul(Rat(Int(18), Int(5)), Sym("kilos"))  # nopep8
    CONVERSIONS["KILOSECOND:MEGASECOND"] = \
        Mul(Int(1000), Sym("kilos"))  # nopep8
    CONVERSIONS["KILOSECOND:MICROSECOND"] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("kilos"))  # nopep8
    CONVERSIONS["KILOSECOND:MILLISECOND"] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("kilos"))  # nopep8
    CONVERSIONS["KILOSECOND:MINUTE"] = \
        Mul(Rat(Int(3), Int(50)), Sym("kilos"))  # nopep8
    CONVERSIONS["KILOSECOND:NANOSECOND"] = \
        Mul(Rat(Int(1), Pow(10, 12)), Sym("kilos"))  # nopep8
    CONVERSIONS["KILOSECOND:PETASECOND"] = \
        Mul(Pow(10, 12), Sym("kilos"))  # nopep8
    CONVERSIONS["KILOSECOND:PICOSECOND"] = \
        Mul(Rat(Int(1), Pow(10, 15)), Sym("kilos"))  # nopep8
    CONVERSIONS["KILOSECOND:SECOND"] = \
        Mul(Rat(Int(1), Int(1000)), Sym("kilos"))  # nopep8
    CONVERSIONS["KILOSECOND:TERASECOND"] = \
        Mul(Pow(10, 9), Sym("kilos"))  # nopep8
    CONVERSIONS["KILOSECOND:YOCTOSECOND"] = \
        Mul(Rat(Int(1), Pow(10, 27)), Sym("kilos"))  # nopep8
    CONVERSIONS["KILOSECOND:YOTTASECOND"] = \
        Mul(Pow(10, 21), Sym("kilos"))  # nopep8
    CONVERSIONS["KILOSECOND:ZEPTOSECOND"] = \
        Mul(Rat(Int(1), Pow(10, 24)), Sym("kilos"))  # nopep8
    CONVERSIONS["KILOSECOND:ZETTASECOND"] = \
        Mul(Pow(10, 18), Sym("kilos"))  # nopep8
    CONVERSIONS["MEGASECOND:ATTOSECOND"] = \
        Mul(Rat(Int(1), Pow(10, 24)), Sym("megas"))  # nopep8
    CONVERSIONS["MEGASECOND:CENTISECOND"] = \
        Mul(Rat(Int(1), Pow(10, 8)), Sym("megas"))  # nopep8
    CONVERSIONS["MEGASECOND:DAY"] = \
        Mul(Rat(Int(54), Int(625)), Sym("megas"))  # nopep8
    CONVERSIONS["MEGASECOND:DECASECOND"] = \
        Mul(Rat(Int(1), Pow(10, 5)), Sym("megas"))  # nopep8
    CONVERSIONS["MEGASECOND:DECISECOND"] = \
        Mul(Rat(Int(1), Pow(10, 7)), Sym("megas"))  # nopep8
    CONVERSIONS["MEGASECOND:EXASECOND"] = \
        Mul(Pow(10, 12), Sym("megas"))  # nopep8
    CONVERSIONS["MEGASECOND:FEMTOSECOND"] = \
        Mul(Rat(Int(1), Pow(10, 21)), Sym("megas"))  # nopep8
    CONVERSIONS["MEGASECOND:GIGASECOND"] = \
        Mul(Int(1000), Sym("megas"))  # nopep8
    CONVERSIONS["MEGASECOND:HECTOSECOND"] = \
        Mul(Rat(Int(1), Pow(10, 4)), Sym("megas"))  # nopep8
    CONVERSIONS["MEGASECOND:HOUR"] = \
        Mul(Rat(Int(9), Int(2500)), Sym("megas"))  # nopep8
    CONVERSIONS["MEGASECOND:KILOSECOND"] = \
        Mul(Rat(Int(1), Int(1000)), Sym("megas"))  # nopep8
    CONVERSIONS["MEGASECOND:MICROSECOND"] = \
        Mul(Rat(Int(1), Pow(10, 12)), Sym("megas"))  # nopep8
    CONVERSIONS["MEGASECOND:MILLISECOND"] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("megas"))  # nopep8
    CONVERSIONS["MEGASECOND:MINUTE"] = \
        Mul(Rat(Int(3), Mul(Int(5), Pow(10, 4))), Sym("megas"))  # nopep8
    CONVERSIONS["MEGASECOND:NANOSECOND"] = \
        Mul(Rat(Int(1), Pow(10, 15)), Sym("megas"))  # nopep8
    CONVERSIONS["MEGASECOND:PETASECOND"] = \
        Mul(Pow(10, 9), Sym("megas"))  # nopep8
    CONVERSIONS["MEGASECOND:PICOSECOND"] = \
        Mul(Rat(Int(1), Pow(10, 18)), Sym("megas"))  # nopep8
    CONVERSIONS["MEGASECOND:SECOND"] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("megas"))  # nopep8
    CONVERSIONS["MEGASECOND:TERASECOND"] = \
        Mul(Pow(10, 6), Sym("megas"))  # nopep8
    CONVERSIONS["MEGASECOND:YOCTOSECOND"] = \
        Mul(Rat(Int(1), Pow(10, 30)), Sym("megas"))  # nopep8
    CONVERSIONS["MEGASECOND:YOTTASECOND"] = \
        Mul(Pow(10, 18), Sym("megas"))  # nopep8
    CONVERSIONS["MEGASECOND:ZEPTOSECOND"] = \
        Mul(Rat(Int(1), Pow(10, 27)), Sym("megas"))  # nopep8
    CONVERSIONS["MEGASECOND:ZETTASECOND"] = \
        Mul(Pow(10, 15), Sym("megas"))  # nopep8
    CONVERSIONS["MICROSECOND:ATTOSECOND"] = \
        Mul(Rat(Int(1), Pow(10, 12)), Sym("micros"))  # nopep8
    CONVERSIONS["MICROSECOND:CENTISECOND"] = \
        Mul(Pow(10, 4), Sym("micros"))  # nopep8
    CONVERSIONS["MICROSECOND:DAY"] = \
        Mul(Mul(Int(864), Pow(10, 8)), Sym("micros"))  # nopep8
    CONVERSIONS["MICROSECOND:DECASECOND"] = \
        Mul(Pow(10, 7), Sym("micros"))  # nopep8
    CONVERSIONS["MICROSECOND:DECISECOND"] = \
        Mul(Pow(10, 5), Sym("micros"))  # nopep8
    CONVERSIONS["MICROSECOND:EXASECOND"] = \
        Mul(Pow(10, 24), Sym("micros"))  # nopep8
    CONVERSIONS["MICROSECOND:FEMTOSECOND"] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("micros"))  # nopep8
    CONVERSIONS["MICROSECOND:GIGASECOND"] = \
        Mul(Pow(10, 15), Sym("micros"))  # nopep8
    CONVERSIONS["MICROSECOND:HECTOSECOND"] = \
        Mul(Pow(10, 8), Sym("micros"))  # nopep8
    CONVERSIONS["MICROSECOND:HOUR"] = \
        Mul(Mul(Int(36), Pow(10, 8)), Sym("micros"))  # nopep8
    CONVERSIONS["MICROSECOND:KILOSECOND"] = \
        Mul(Pow(10, 9), Sym("micros"))  # nopep8
    CONVERSIONS["MICROSECOND:MEGASECOND"] = \
        Mul(Pow(10, 12), Sym("micros"))  # nopep8
    CONVERSIONS["MICROSECOND:MILLISECOND"] = \
        Mul(Int(1000), Sym("micros"))  # nopep8
    CONVERSIONS["MICROSECOND:MINUTE"] = \
        Mul(Mul(Int(6), Pow(10, 7)), Sym("micros"))  # nopep8
    CONVERSIONS["MICROSECOND:NANOSECOND"] = \
        Mul(Rat(Int(1), Int(1000)), Sym("micros"))  # nopep8
    CONVERSIONS["MICROSECOND:PETASECOND"] = \
        Mul(Pow(10, 21), Sym("micros"))  # nopep8
    CONVERSIONS["MICROSECOND:PICOSECOND"] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("micros"))  # nopep8
    CONVERSIONS["MICROSECOND:SECOND"] = \
        Mul(Pow(10, 6), Sym("micros"))  # nopep8
    CONVERSIONS["MICROSECOND:TERASECOND"] = \
        Mul(Pow(10, 18), Sym("micros"))  # nopep8
    CONVERSIONS["MICROSECOND:YOCTOSECOND"] = \
        Mul(Rat(Int(1), Pow(10, 18)), Sym("micros"))  # nopep8
    CONVERSIONS["MICROSECOND:YOTTASECOND"] = \
        Mul(Pow(10, 30), Sym("micros"))  # nopep8
    CONVERSIONS["MICROSECOND:ZEPTOSECOND"] = \
        Mul(Rat(Int(1), Pow(10, 15)), Sym("micros"))  # nopep8
    CONVERSIONS["MICROSECOND:ZETTASECOND"] = \
        Mul(Pow(10, 27), Sym("micros"))  # nopep8
    CONVERSIONS["MILLISECOND:ATTOSECOND"] = \
        Mul(Rat(Int(1), Pow(10, 15)), Sym("millis"))  # nopep8
    CONVERSIONS["MILLISECOND:CENTISECOND"] = \
        Mul(Int(10), Sym("millis"))  # nopep8
    CONVERSIONS["MILLISECOND:DAY"] = \
        Mul(Mul(Int(864), Pow(10, 5)), Sym("millis"))  # nopep8
    CONVERSIONS["MILLISECOND:DECASECOND"] = \
        Mul(Pow(10, 4), Sym("millis"))  # nopep8
    CONVERSIONS["MILLISECOND:DECISECOND"] = \
        Mul(Int(100), Sym("millis"))  # nopep8
    CONVERSIONS["MILLISECOND:EXASECOND"] = \
        Mul(Pow(10, 21), Sym("millis"))  # nopep8
    CONVERSIONS["MILLISECOND:FEMTOSECOND"] = \
        Mul(Rat(Int(1), Pow(10, 12)), Sym("millis"))  # nopep8
    CONVERSIONS["MILLISECOND:GIGASECOND"] = \
        Mul(Pow(10, 12), Sym("millis"))  # nopep8
    CONVERSIONS["MILLISECOND:HECTOSECOND"] = \
        Mul(Pow(10, 5), Sym("millis"))  # nopep8
    CONVERSIONS["MILLISECOND:HOUR"] = \
        Mul(Mul(Int(36), Pow(10, 5)), Sym("millis"))  # nopep8
    CONVERSIONS["MILLISECOND:KILOSECOND"] = \
        Mul(Pow(10, 6), Sym("millis"))  # nopep8
    CONVERSIONS["MILLISECOND:MEGASECOND"] = \
        Mul(Pow(10, 9), Sym("millis"))  # nopep8
    CONVERSIONS["MILLISECOND:MICROSECOND"] = \
        Mul(Rat(Int(1), Int(1000)), Sym("millis"))  # nopep8
    CONVERSIONS["MILLISECOND:MINUTE"] = \
        Mul(Mul(Int(6), Pow(10, 4)), Sym("millis"))  # nopep8
    CONVERSIONS["MILLISECOND:NANOSECOND"] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("millis"))  # nopep8
    CONVERSIONS["MILLISECOND:PETASECOND"] = \
        Mul(Pow(10, 18), Sym("millis"))  # nopep8
    CONVERSIONS["MILLISECOND:PICOSECOND"] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("millis"))  # nopep8
    CONVERSIONS["MILLISECOND:SECOND"] = \
        Mul(Int(1000), Sym("millis"))  # nopep8
    CONVERSIONS["MILLISECOND:TERASECOND"] = \
        Mul(Pow(10, 15), Sym("millis"))  # nopep8
    CONVERSIONS["MILLISECOND:YOCTOSECOND"] = \
        Mul(Rat(Int(1), Pow(10, 21)), Sym("millis"))  # nopep8
    CONVERSIONS["MILLISECOND:YOTTASECOND"] = \
        Mul(Pow(10, 27), Sym("millis"))  # nopep8
    CONVERSIONS["MILLISECOND:ZEPTOSECOND"] = \
        Mul(Rat(Int(1), Pow(10, 18)), Sym("millis"))  # nopep8
    CONVERSIONS["MILLISECOND:ZETTASECOND"] = \
        Mul(Pow(10, 24), Sym("millis"))  # nopep8
    CONVERSIONS["MINUTE:ATTOSECOND"] = \
        Mul(Rat(Int(1), Mul(Int(6), Pow(10, 19))), Sym("m"))  # nopep8
    CONVERSIONS["MINUTE:CENTISECOND"] = \
        Mul(Rat(Int(1), Int(6000)), Sym("m"))  # nopep8
    CONVERSIONS["MINUTE:DAY"] = \
        Mul(Int(1440), Sym("m"))  # nopep8
    CONVERSIONS["MINUTE:DECASECOND"] = \
        Mul(Rat(Int(1), Int(6)), Sym("m"))  # nopep8
    CONVERSIONS["MINUTE:DECISECOND"] = \
        Mul(Rat(Int(1), Int(600)), Sym("m"))  # nopep8
    CONVERSIONS["MINUTE:EXASECOND"] = \
        Mul(Rat(Mul(Int(5), Pow(10, 16)), Int(3)), Sym("m"))  # nopep8
    CONVERSIONS["MINUTE:FEMTOSECOND"] = \
        Mul(Rat(Int(1), Mul(Int(6), Pow(10, 16))), Sym("m"))  # nopep8
    CONVERSIONS["MINUTE:GIGASECOND"] = \
        Mul(Rat(Mul(Int(5), Pow(10, 7)), Int(3)), Sym("m"))  # nopep8
    CONVERSIONS["MINUTE:HECTOSECOND"] = \
        Mul(Rat(Int(5), Int(3)), Sym("m"))  # nopep8
    CONVERSIONS["MINUTE:HOUR"] = \
        Mul(Int(60), Sym("m"))  # nopep8
    CONVERSIONS["MINUTE:KILOSECOND"] = \
        Mul(Rat(Int(50), Int(3)), Sym("m"))  # nopep8
    CONVERSIONS["MINUTE:MEGASECOND"] = \
        Mul(Rat(Mul(Int(5), Pow(10, 4)), Int(3)), Sym("m"))  # nopep8
    CONVERSIONS["MINUTE:MICROSECOND"] = \
        Mul(Rat(Int(1), Mul(Int(6), Pow(10, 7))), Sym("m"))  # nopep8
    CONVERSIONS["MINUTE:MILLISECOND"] = \
        Mul(Rat(Int(1), Mul(Int(6), Pow(10, 4))), Sym("m"))  # nopep8
    CONVERSIONS["MINUTE:NANOSECOND"] = \
        Mul(Rat(Int(1), Mul(Int(6), Pow(10, 10))), Sym("m"))  # nopep8
    CONVERSIONS["MINUTE:PETASECOND"] = \
        Mul(Rat(Mul(Int(5), Pow(10, 13)), Int(3)), Sym("m"))  # nopep8
    CONVERSIONS["MINUTE:PICOSECOND"] = \
        Mul(Rat(Int(1), Mul(Int(6), Pow(10, 13))), Sym("m"))  # nopep8
    CONVERSIONS["MINUTE:SECOND"] = \
        Mul(Rat(Int(1), Int(60)), Sym("m"))  # nopep8
    CONVERSIONS["MINUTE:TERASECOND"] = \
        Mul(Rat(Mul(Int(5), Pow(10, 10)), Int(3)), Sym("m"))  # nopep8
    CONVERSIONS["MINUTE:YOCTOSECOND"] = \
        Mul(Rat(Int(1), Mul(Int(6), Pow(10, 25))), Sym("m"))  # nopep8
    CONVERSIONS["MINUTE:YOTTASECOND"] = \
        Mul(Rat(Mul(Int(5), Pow(10, 22)), Int(3)), Sym("m"))  # nopep8
    CONVERSIONS["MINUTE:ZEPTOSECOND"] = \
        Mul(Rat(Int(1), Mul(Int(6), Pow(10, 22))), Sym("m"))  # nopep8
    CONVERSIONS["MINUTE:ZETTASECOND"] = \
        Mul(Rat(Mul(Int(5), Pow(10, 19)), Int(3)), Sym("m"))  # nopep8
    CONVERSIONS["NANOSECOND:ATTOSECOND"] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("nanos"))  # nopep8
    CONVERSIONS["NANOSECOND:CENTISECOND"] = \
        Mul(Pow(10, 7), Sym("nanos"))  # nopep8
    CONVERSIONS["NANOSECOND:DAY"] = \
        Mul(Mul(Int(864), Pow(10, 11)), Sym("nanos"))  # nopep8
    CONVERSIONS["NANOSECOND:DECASECOND"] = \
        Mul(Pow(10, 10), Sym("nanos"))  # nopep8
    CONVERSIONS["NANOSECOND:DECISECOND"] = \
        Mul(Pow(10, 8), Sym("nanos"))  # nopep8
    CONVERSIONS["NANOSECOND:EXASECOND"] = \
        Mul(Pow(10, 27), Sym("nanos"))  # nopep8
    CONVERSIONS["NANOSECOND:FEMTOSECOND"] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("nanos"))  # nopep8
    CONVERSIONS["NANOSECOND:GIGASECOND"] = \
        Mul(Pow(10, 18), Sym("nanos"))  # nopep8
    CONVERSIONS["NANOSECOND:HECTOSECOND"] = \
        Mul(Pow(10, 11), Sym("nanos"))  # nopep8
    CONVERSIONS["NANOSECOND:HOUR"] = \
        Mul(Mul(Int(36), Pow(10, 11)), Sym("nanos"))  # nopep8
    CONVERSIONS["NANOSECOND:KILOSECOND"] = \
        Mul(Pow(10, 12), Sym("nanos"))  # nopep8
    CONVERSIONS["NANOSECOND:MEGASECOND"] = \
        Mul(Pow(10, 15), Sym("nanos"))  # nopep8
    CONVERSIONS["NANOSECOND:MICROSECOND"] = \
        Mul(Int(1000), Sym("nanos"))  # nopep8
    CONVERSIONS["NANOSECOND:MILLISECOND"] = \
        Mul(Pow(10, 6), Sym("nanos"))  # nopep8
    CONVERSIONS["NANOSECOND:MINUTE"] = \
        Mul(Mul(Int(6), Pow(10, 10)), Sym("nanos"))  # nopep8
    CONVERSIONS["NANOSECOND:PETASECOND"] = \
        Mul(Pow(10, 24), Sym("nanos"))  # nopep8
    CONVERSIONS["NANOSECOND:PICOSECOND"] = \
        Mul(Rat(Int(1), Int(1000)), Sym("nanos"))  # nopep8
    CONVERSIONS["NANOSECOND:SECOND"] = \
        Mul(Pow(10, 9), Sym("nanos"))  # nopep8
    CONVERSIONS["NANOSECOND:TERASECOND"] = \
        Mul(Pow(10, 21), Sym("nanos"))  # nopep8
    CONVERSIONS["NANOSECOND:YOCTOSECOND"] = \
        Mul(Rat(Int(1), Pow(10, 15)), Sym("nanos"))  # nopep8
    CONVERSIONS["NANOSECOND:YOTTASECOND"] = \
        Mul(Pow(10, 33), Sym("nanos"))  # nopep8
    CONVERSIONS["NANOSECOND:ZEPTOSECOND"] = \
        Mul(Rat(Int(1), Pow(10, 12)), Sym("nanos"))  # nopep8
    CONVERSIONS["NANOSECOND:ZETTASECOND"] = \
        Mul(Pow(10, 30), Sym("nanos"))  # nopep8
    CONVERSIONS["PETASECOND:ATTOSECOND"] = \
        Mul(Rat(Int(1), Pow(10, 33)), Sym("petas"))  # nopep8
    CONVERSIONS["PETASECOND:CENTISECOND"] = \
        Mul(Rat(Int(1), Pow(10, 17)), Sym("petas"))  # nopep8
    CONVERSIONS["PETASECOND:DAY"] = \
        Mul(Rat(Int(27), Mul(Int(3125), Pow(10, 8))), Sym("petas"))  # nopep8
    CONVERSIONS["PETASECOND:DECASECOND"] = \
        Mul(Rat(Int(1), Pow(10, 14)), Sym("petas"))  # nopep8
    CONVERSIONS["PETASECOND:DECISECOND"] = \
        Mul(Rat(Int(1), Pow(10, 16)), Sym("petas"))  # nopep8
    CONVERSIONS["PETASECOND:EXASECOND"] = \
        Mul(Int(1000), Sym("petas"))  # nopep8
    CONVERSIONS["PETASECOND:FEMTOSECOND"] = \
        Mul(Rat(Int(1), Pow(10, 30)), Sym("petas"))  # nopep8
    CONVERSIONS["PETASECOND:GIGASECOND"] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("petas"))  # nopep8
    CONVERSIONS["PETASECOND:HECTOSECOND"] = \
        Mul(Rat(Int(1), Pow(10, 13)), Sym("petas"))  # nopep8
    CONVERSIONS["PETASECOND:HOUR"] = \
        Mul(Rat(Int(9), Mul(Int(25), Pow(10, 11))), Sym("petas"))  # nopep8
    CONVERSIONS["PETASECOND:KILOSECOND"] = \
        Mul(Rat(Int(1), Pow(10, 12)), Sym("petas"))  # nopep8
    CONVERSIONS["PETASECOND:MEGASECOND"] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("petas"))  # nopep8
    CONVERSIONS["PETASECOND:MICROSECOND"] = \
        Mul(Rat(Int(1), Pow(10, 21)), Sym("petas"))  # nopep8
    CONVERSIONS["PETASECOND:MILLISECOND"] = \
        Mul(Rat(Int(1), Pow(10, 18)), Sym("petas"))  # nopep8
    CONVERSIONS["PETASECOND:MINUTE"] = \
        Mul(Rat(Int(3), Mul(Int(5), Pow(10, 13))), Sym("petas"))  # nopep8
    CONVERSIONS["PETASECOND:NANOSECOND"] = \
        Mul(Rat(Int(1), Pow(10, 24)), Sym("petas"))  # nopep8
    CONVERSIONS["PETASECOND:PICOSECOND"] = \
        Mul(Rat(Int(1), Pow(10, 27)), Sym("petas"))  # nopep8
    CONVERSIONS["PETASECOND:SECOND"] = \
        Mul(Rat(Int(1), Pow(10, 15)), Sym("petas"))  # nopep8
    CONVERSIONS["PETASECOND:TERASECOND"] = \
        Mul(Rat(Int(1), Int(1000)), Sym("petas"))  # nopep8
    CONVERSIONS["PETASECOND:YOCTOSECOND"] = \
        Mul(Rat(Int(1), Pow(10, 39)), Sym("petas"))  # nopep8
    CONVERSIONS["PETASECOND:YOTTASECOND"] = \
        Mul(Pow(10, 9), Sym("petas"))  # nopep8
    CONVERSIONS["PETASECOND:ZEPTOSECOND"] = \
        Mul(Rat(Int(1), Pow(10, 36)), Sym("petas"))  # nopep8
    CONVERSIONS["PETASECOND:ZETTASECOND"] = \
        Mul(Pow(10, 6), Sym("petas"))  # nopep8
    CONVERSIONS["PICOSECOND:ATTOSECOND"] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("picos"))  # nopep8
    CONVERSIONS["PICOSECOND:CENTISECOND"] = \
        Mul(Pow(10, 10), Sym("picos"))  # nopep8
    CONVERSIONS["PICOSECOND:DAY"] = \
        Mul(Mul(Int(864), Pow(10, 14)), Sym("picos"))  # nopep8
    CONVERSIONS["PICOSECOND:DECASECOND"] = \
        Mul(Pow(10, 13), Sym("picos"))  # nopep8
    CONVERSIONS["PICOSECOND:DECISECOND"] = \
        Mul(Pow(10, 11), Sym("picos"))  # nopep8
    CONVERSIONS["PICOSECOND:EXASECOND"] = \
        Mul(Pow(10, 30), Sym("picos"))  # nopep8
    CONVERSIONS["PICOSECOND:FEMTOSECOND"] = \
        Mul(Rat(Int(1), Int(1000)), Sym("picos"))  # nopep8
    CONVERSIONS["PICOSECOND:GIGASECOND"] = \
        Mul(Pow(10, 21), Sym("picos"))  # nopep8
    CONVERSIONS["PICOSECOND:HECTOSECOND"] = \
        Mul(Pow(10, 14), Sym("picos"))  # nopep8
    CONVERSIONS["PICOSECOND:HOUR"] = \
        Mul(Mul(Int(36), Pow(10, 14)), Sym("picos"))  # nopep8
    CONVERSIONS["PICOSECOND:KILOSECOND"] = \
        Mul(Pow(10, 15), Sym("picos"))  # nopep8
    CONVERSIONS["PICOSECOND:MEGASECOND"] = \
        Mul(Pow(10, 18), Sym("picos"))  # nopep8
    CONVERSIONS["PICOSECOND:MICROSECOND"] = \
        Mul(Pow(10, 6), Sym("picos"))  # nopep8
    CONVERSIONS["PICOSECOND:MILLISECOND"] = \
        Mul(Pow(10, 9), Sym("picos"))  # nopep8
    CONVERSIONS["PICOSECOND:MINUTE"] = \
        Mul(Mul(Int(6), Pow(10, 13)), Sym("picos"))  # nopep8
    CONVERSIONS["PICOSECOND:NANOSECOND"] = \
        Mul(Int(1000), Sym("picos"))  # nopep8
    CONVERSIONS["PICOSECOND:PETASECOND"] = \
        Mul(Pow(10, 27), Sym("picos"))  # nopep8
    CONVERSIONS["PICOSECOND:SECOND"] = \
        Mul(Pow(10, 12), Sym("picos"))  # nopep8
    CONVERSIONS["PICOSECOND:TERASECOND"] = \
        Mul(Pow(10, 24), Sym("picos"))  # nopep8
    CONVERSIONS["PICOSECOND:YOCTOSECOND"] = \
        Mul(Rat(Int(1), Pow(10, 12)), Sym("picos"))  # nopep8
    CONVERSIONS["PICOSECOND:YOTTASECOND"] = \
        Mul(Pow(10, 36), Sym("picos"))  # nopep8
    CONVERSIONS["PICOSECOND:ZEPTOSECOND"] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("picos"))  # nopep8
    CONVERSIONS["PICOSECOND:ZETTASECOND"] = \
        Mul(Pow(10, 33), Sym("picos"))  # nopep8
    CONVERSIONS["SECOND:ATTOSECOND"] = \
        Mul(Rat(Int(1), Pow(10, 18)), Sym("s"))  # nopep8
    CONVERSIONS["SECOND:CENTISECOND"] = \
        Mul(Rat(Int(1), Int(100)), Sym("s"))  # nopep8
    CONVERSIONS["SECOND:DAY"] = \
        Mul(Int(86400), Sym("s"))  # nopep8
    CONVERSIONS["SECOND:DECASECOND"] = \
        Mul(Int(10), Sym("s"))  # nopep8
    CONVERSIONS["SECOND:DECISECOND"] = \
        Mul(Rat(Int(1), Int(10)), Sym("s"))  # nopep8
    CONVERSIONS["SECOND:EXASECOND"] = \
        Mul(Pow(10, 18), Sym("s"))  # nopep8
    CONVERSIONS["SECOND:FEMTOSECOND"] = \
        Mul(Rat(Int(1), Pow(10, 15)), Sym("s"))  # nopep8
    CONVERSIONS["SECOND:GIGASECOND"] = \
        Mul(Pow(10, 9), Sym("s"))  # nopep8
    CONVERSIONS["SECOND:HECTOSECOND"] = \
        Mul(Int(100), Sym("s"))  # nopep8
    CONVERSIONS["SECOND:HOUR"] = \
        Mul(Int(3600), Sym("s"))  # nopep8
    CONVERSIONS["SECOND:KILOSECOND"] = \
        Mul(Int(1000), Sym("s"))  # nopep8
    CONVERSIONS["SECOND:MEGASECOND"] = \
        Mul(Pow(10, 6), Sym("s"))  # nopep8
    CONVERSIONS["SECOND:MICROSECOND"] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("s"))  # nopep8
    CONVERSIONS["SECOND:MILLISECOND"] = \
        Mul(Rat(Int(1), Int(1000)), Sym("s"))  # nopep8
    CONVERSIONS["SECOND:MINUTE"] = \
        Mul(Int(60), Sym("s"))  # nopep8
    CONVERSIONS["SECOND:NANOSECOND"] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("s"))  # nopep8
    CONVERSIONS["SECOND:PETASECOND"] = \
        Mul(Pow(10, 15), Sym("s"))  # nopep8
    CONVERSIONS["SECOND:PICOSECOND"] = \
        Mul(Rat(Int(1), Pow(10, 12)), Sym("s"))  # nopep8
    CONVERSIONS["SECOND:TERASECOND"] = \
        Mul(Pow(10, 12), Sym("s"))  # nopep8
    CONVERSIONS["SECOND:YOCTOSECOND"] = \
        Mul(Rat(Int(1), Pow(10, 24)), Sym("s"))  # nopep8
    CONVERSIONS["SECOND:YOTTASECOND"] = \
        Mul(Pow(10, 24), Sym("s"))  # nopep8
    CONVERSIONS["SECOND:ZEPTOSECOND"] = \
        Mul(Rat(Int(1), Pow(10, 21)), Sym("s"))  # nopep8
    CONVERSIONS["SECOND:ZETTASECOND"] = \
        Mul(Pow(10, 21), Sym("s"))  # nopep8
    CONVERSIONS["TERASECOND:ATTOSECOND"] = \
        Mul(Rat(Int(1), Pow(10, 30)), Sym("teras"))  # nopep8
    CONVERSIONS["TERASECOND:CENTISECOND"] = \
        Mul(Rat(Int(1), Pow(10, 14)), Sym("teras"))  # nopep8
    CONVERSIONS["TERASECOND:DAY"] = \
        Mul(Rat(Int(27), Mul(Int(3125), Pow(10, 5))), Sym("teras"))  # nopep8
    CONVERSIONS["TERASECOND:DECASECOND"] = \
        Mul(Rat(Int(1), Pow(10, 11)), Sym("teras"))  # nopep8
    CONVERSIONS["TERASECOND:DECISECOND"] = \
        Mul(Rat(Int(1), Pow(10, 13)), Sym("teras"))  # nopep8
    CONVERSIONS["TERASECOND:EXASECOND"] = \
        Mul(Pow(10, 6), Sym("teras"))  # nopep8
    CONVERSIONS["TERASECOND:FEMTOSECOND"] = \
        Mul(Rat(Int(1), Pow(10, 27)), Sym("teras"))  # nopep8
    CONVERSIONS["TERASECOND:GIGASECOND"] = \
        Mul(Rat(Int(1), Int(1000)), Sym("teras"))  # nopep8
    CONVERSIONS["TERASECOND:HECTOSECOND"] = \
        Mul(Rat(Int(1), Pow(10, 10)), Sym("teras"))  # nopep8
    CONVERSIONS["TERASECOND:HOUR"] = \
        Mul(Rat(Int(9), Mul(Int(25), Pow(10, 8))), Sym("teras"))  # nopep8
    CONVERSIONS["TERASECOND:KILOSECOND"] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("teras"))  # nopep8
    CONVERSIONS["TERASECOND:MEGASECOND"] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("teras"))  # nopep8
    CONVERSIONS["TERASECOND:MICROSECOND"] = \
        Mul(Rat(Int(1), Pow(10, 18)), Sym("teras"))  # nopep8
    CONVERSIONS["TERASECOND:MILLISECOND"] = \
        Mul(Rat(Int(1), Pow(10, 15)), Sym("teras"))  # nopep8
    CONVERSIONS["TERASECOND:MINUTE"] = \
        Mul(Rat(Int(3), Mul(Int(5), Pow(10, 10))), Sym("teras"))  # nopep8
    CONVERSIONS["TERASECOND:NANOSECOND"] = \
        Mul(Rat(Int(1), Pow(10, 21)), Sym("teras"))  # nopep8
    CONVERSIONS["TERASECOND:PETASECOND"] = \
        Mul(Int(1000), Sym("teras"))  # nopep8
    CONVERSIONS["TERASECOND:PICOSECOND"] = \
        Mul(Rat(Int(1), Pow(10, 24)), Sym("teras"))  # nopep8
    CONVERSIONS["TERASECOND:SECOND"] = \
        Mul(Rat(Int(1), Pow(10, 12)), Sym("teras"))  # nopep8
    CONVERSIONS["TERASECOND:YOCTOSECOND"] = \
        Mul(Rat(Int(1), Pow(10, 36)), Sym("teras"))  # nopep8
    CONVERSIONS["TERASECOND:YOTTASECOND"] = \
        Mul(Pow(10, 12), Sym("teras"))  # nopep8
    CONVERSIONS["TERASECOND:ZEPTOSECOND"] = \
        Mul(Rat(Int(1), Pow(10, 33)), Sym("teras"))  # nopep8
    CONVERSIONS["TERASECOND:ZETTASECOND"] = \
        Mul(Pow(10, 9), Sym("teras"))  # nopep8
    CONVERSIONS["YOCTOSECOND:ATTOSECOND"] = \
        Mul(Pow(10, 6), Sym("yoctos"))  # nopep8
    CONVERSIONS["YOCTOSECOND:CENTISECOND"] = \
        Mul(Pow(10, 22), Sym("yoctos"))  # nopep8
    CONVERSIONS["YOCTOSECOND:DAY"] = \
        Mul(Mul(Int(864), Pow(10, 26)), Sym("yoctos"))  # nopep8
    CONVERSIONS["YOCTOSECOND:DECASECOND"] = \
        Mul(Pow(10, 25), Sym("yoctos"))  # nopep8
    CONVERSIONS["YOCTOSECOND:DECISECOND"] = \
        Mul(Pow(10, 23), Sym("yoctos"))  # nopep8
    CONVERSIONS["YOCTOSECOND:EXASECOND"] = \
        Mul(Pow(10, 42), Sym("yoctos"))  # nopep8
    CONVERSIONS["YOCTOSECOND:FEMTOSECOND"] = \
        Mul(Pow(10, 9), Sym("yoctos"))  # nopep8
    CONVERSIONS["YOCTOSECOND:GIGASECOND"] = \
        Mul(Pow(10, 33), Sym("yoctos"))  # nopep8
    CONVERSIONS["YOCTOSECOND:HECTOSECOND"] = \
        Mul(Pow(10, 26), Sym("yoctos"))  # nopep8
    CONVERSIONS["YOCTOSECOND:HOUR"] = \
        Mul(Mul(Int(36), Pow(10, 26)), Sym("yoctos"))  # nopep8
    CONVERSIONS["YOCTOSECOND:KILOSECOND"] = \
        Mul(Pow(10, 27), Sym("yoctos"))  # nopep8
    CONVERSIONS["YOCTOSECOND:MEGASECOND"] = \
        Mul(Pow(10, 30), Sym("yoctos"))  # nopep8
    CONVERSIONS["YOCTOSECOND:MICROSECOND"] = \
        Mul(Pow(10, 18), Sym("yoctos"))  # nopep8
    CONVERSIONS["YOCTOSECOND:MILLISECOND"] = \
        Mul(Pow(10, 21), Sym("yoctos"))  # nopep8
    CONVERSIONS["YOCTOSECOND:MINUTE"] = \
        Mul(Mul(Int(6), Pow(10, 25)), Sym("yoctos"))  # nopep8
    CONVERSIONS["YOCTOSECOND:NANOSECOND"] = \
        Mul(Pow(10, 15), Sym("yoctos"))  # nopep8
    CONVERSIONS["YOCTOSECOND:PETASECOND"] = \
        Mul(Pow(10, 39), Sym("yoctos"))  # nopep8
    CONVERSIONS["YOCTOSECOND:PICOSECOND"] = \
        Mul(Pow(10, 12), Sym("yoctos"))  # nopep8
    CONVERSIONS["YOCTOSECOND:SECOND"] = \
        Mul(Pow(10, 24), Sym("yoctos"))  # nopep8
    CONVERSIONS["YOCTOSECOND:TERASECOND"] = \
        Mul(Pow(10, 36), Sym("yoctos"))  # nopep8
    CONVERSIONS["YOCTOSECOND:YOTTASECOND"] = \
        Mul(Pow(10, 48), Sym("yoctos"))  # nopep8
    CONVERSIONS["YOCTOSECOND:ZEPTOSECOND"] = \
        Mul(Int(1000), Sym("yoctos"))  # nopep8
    CONVERSIONS["YOCTOSECOND:ZETTASECOND"] = \
        Mul(Pow(10, 45), Sym("yoctos"))  # nopep8
    CONVERSIONS["YOTTASECOND:ATTOSECOND"] = \
        Mul(Rat(Int(1), Pow(10, 42)), Sym("yottas"))  # nopep8
    CONVERSIONS["YOTTASECOND:CENTISECOND"] = \
        Mul(Rat(Int(1), Pow(10, 26)), Sym("yottas"))  # nopep8
    CONVERSIONS["YOTTASECOND:DAY"] = \
        Mul(Rat(Int(27), Mul(Int(3125), Pow(10, 17))), Sym("yottas"))  # nopep8
    CONVERSIONS["YOTTASECOND:DECASECOND"] = \
        Mul(Rat(Int(1), Pow(10, 23)), Sym("yottas"))  # nopep8
    CONVERSIONS["YOTTASECOND:DECISECOND"] = \
        Mul(Rat(Int(1), Pow(10, 25)), Sym("yottas"))  # nopep8
    CONVERSIONS["YOTTASECOND:EXASECOND"] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("yottas"))  # nopep8
    CONVERSIONS["YOTTASECOND:FEMTOSECOND"] = \
        Mul(Rat(Int(1), Pow(10, 39)), Sym("yottas"))  # nopep8
    CONVERSIONS["YOTTASECOND:GIGASECOND"] = \
        Mul(Rat(Int(1), Pow(10, 15)), Sym("yottas"))  # nopep8
    CONVERSIONS["YOTTASECOND:HECTOSECOND"] = \
        Mul(Rat(Int(1), Pow(10, 22)), Sym("yottas"))  # nopep8
    CONVERSIONS["YOTTASECOND:HOUR"] = \
        Mul(Rat(Int(9), Mul(Int(25), Pow(10, 20))), Sym("yottas"))  # nopep8
    CONVERSIONS["YOTTASECOND:KILOSECOND"] = \
        Mul(Rat(Int(1), Pow(10, 21)), Sym("yottas"))  # nopep8
    CONVERSIONS["YOTTASECOND:MEGASECOND"] = \
        Mul(Rat(Int(1), Pow(10, 18)), Sym("yottas"))  # nopep8
    CONVERSIONS["YOTTASECOND:MICROSECOND"] = \
        Mul(Rat(Int(1), Pow(10, 30)), Sym("yottas"))  # nopep8
    CONVERSIONS["YOTTASECOND:MILLISECOND"] = \
        Mul(Rat(Int(1), Pow(10, 27)), Sym("yottas"))  # nopep8
    CONVERSIONS["YOTTASECOND:MINUTE"] = \
        Mul(Rat(Int(3), Mul(Int(5), Pow(10, 22))), Sym("yottas"))  # nopep8
    CONVERSIONS["YOTTASECOND:NANOSECOND"] = \
        Mul(Rat(Int(1), Pow(10, 33)), Sym("yottas"))  # nopep8
    CONVERSIONS["YOTTASECOND:PETASECOND"] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("yottas"))  # nopep8
    CONVERSIONS["YOTTASECOND:PICOSECOND"] = \
        Mul(Rat(Int(1), Pow(10, 36)), Sym("yottas"))  # nopep8
    CONVERSIONS["YOTTASECOND:SECOND"] = \
        Mul(Rat(Int(1), Pow(10, 24)), Sym("yottas"))  # nopep8
    CONVERSIONS["YOTTASECOND:TERASECOND"] = \
        Mul(Rat(Int(1), Pow(10, 12)), Sym("yottas"))  # nopep8
    CONVERSIONS["YOTTASECOND:YOCTOSECOND"] = \
        Mul(Rat(Int(1), Pow(10, 48)), Sym("yottas"))  # nopep8
    CONVERSIONS["YOTTASECOND:ZEPTOSECOND"] = \
        Mul(Rat(Int(1), Pow(10, 45)), Sym("yottas"))  # nopep8
    CONVERSIONS["YOTTASECOND:ZETTASECOND"] = \
        Mul(Rat(Int(1), Int(1000)), Sym("yottas"))  # nopep8
    CONVERSIONS["ZEPTOSECOND:ATTOSECOND"] = \
        Mul(Int(1000), Sym("zeptos"))  # nopep8
    CONVERSIONS["ZEPTOSECOND:CENTISECOND"] = \
        Mul(Pow(10, 19), Sym("zeptos"))  # nopep8
    CONVERSIONS["ZEPTOSECOND:DAY"] = \
        Mul(Mul(Int(864), Pow(10, 23)), Sym("zeptos"))  # nopep8
    CONVERSIONS["ZEPTOSECOND:DECASECOND"] = \
        Mul(Pow(10, 22), Sym("zeptos"))  # nopep8
    CONVERSIONS["ZEPTOSECOND:DECISECOND"] = \
        Mul(Pow(10, 20), Sym("zeptos"))  # nopep8
    CONVERSIONS["ZEPTOSECOND:EXASECOND"] = \
        Mul(Pow(10, 39), Sym("zeptos"))  # nopep8
    CONVERSIONS["ZEPTOSECOND:FEMTOSECOND"] = \
        Mul(Pow(10, 6), Sym("zeptos"))  # nopep8
    CONVERSIONS["ZEPTOSECOND:GIGASECOND"] = \
        Mul(Pow(10, 30), Sym("zeptos"))  # nopep8
    CONVERSIONS["ZEPTOSECOND:HECTOSECOND"] = \
        Mul(Pow(10, 23), Sym("zeptos"))  # nopep8
    CONVERSIONS["ZEPTOSECOND:HOUR"] = \
        Mul(Mul(Int(36), Pow(10, 23)), Sym("zeptos"))  # nopep8
    CONVERSIONS["ZEPTOSECOND:KILOSECOND"] = \
        Mul(Pow(10, 24), Sym("zeptos"))  # nopep8
    CONVERSIONS["ZEPTOSECOND:MEGASECOND"] = \
        Mul(Pow(10, 27), Sym("zeptos"))  # nopep8
    CONVERSIONS["ZEPTOSECOND:MICROSECOND"] = \
        Mul(Pow(10, 15), Sym("zeptos"))  # nopep8
    CONVERSIONS["ZEPTOSECOND:MILLISECOND"] = \
        Mul(Pow(10, 18), Sym("zeptos"))  # nopep8
    CONVERSIONS["ZEPTOSECOND:MINUTE"] = \
        Mul(Mul(Int(6), Pow(10, 22)), Sym("zeptos"))  # nopep8
    CONVERSIONS["ZEPTOSECOND:NANOSECOND"] = \
        Mul(Pow(10, 12), Sym("zeptos"))  # nopep8
    CONVERSIONS["ZEPTOSECOND:PETASECOND"] = \
        Mul(Pow(10, 36), Sym("zeptos"))  # nopep8
    CONVERSIONS["ZEPTOSECOND:PICOSECOND"] = \
        Mul(Pow(10, 9), Sym("zeptos"))  # nopep8
    CONVERSIONS["ZEPTOSECOND:SECOND"] = \
        Mul(Pow(10, 21), Sym("zeptos"))  # nopep8
    CONVERSIONS["ZEPTOSECOND:TERASECOND"] = \
        Mul(Pow(10, 33), Sym("zeptos"))  # nopep8
    CONVERSIONS["ZEPTOSECOND:YOCTOSECOND"] = \
        Mul(Rat(Int(1), Int(1000)), Sym("zeptos"))  # nopep8
    CONVERSIONS["ZEPTOSECOND:YOTTASECOND"] = \
        Mul(Pow(10, 45), Sym("zeptos"))  # nopep8
    CONVERSIONS["ZEPTOSECOND:ZETTASECOND"] = \
        Mul(Pow(10, 42), Sym("zeptos"))  # nopep8
    CONVERSIONS["ZETTASECOND:ATTOSECOND"] = \
        Mul(Rat(Int(1), Pow(10, 39)), Sym("zettas"))  # nopep8
    CONVERSIONS["ZETTASECOND:CENTISECOND"] = \
        Mul(Rat(Int(1), Pow(10, 23)), Sym("zettas"))  # nopep8
    CONVERSIONS["ZETTASECOND:DAY"] = \
        Mul(Rat(Int(27), Mul(Int(3125), Pow(10, 14))), Sym("zettas"))  # nopep8
    CONVERSIONS["ZETTASECOND:DECASECOND"] = \
        Mul(Rat(Int(1), Pow(10, 20)), Sym("zettas"))  # nopep8
    CONVERSIONS["ZETTASECOND:DECISECOND"] = \
        Mul(Rat(Int(1), Pow(10, 22)), Sym("zettas"))  # nopep8
    CONVERSIONS["ZETTASECOND:EXASECOND"] = \
        Mul(Rat(Int(1), Int(1000)), Sym("zettas"))  # nopep8
    CONVERSIONS["ZETTASECOND:FEMTOSECOND"] = \
        Mul(Rat(Int(1), Pow(10, 36)), Sym("zettas"))  # nopep8
    CONVERSIONS["ZETTASECOND:GIGASECOND"] = \
        Mul(Rat(Int(1), Pow(10, 12)), Sym("zettas"))  # nopep8
    CONVERSIONS["ZETTASECOND:HECTOSECOND"] = \
        Mul(Rat(Int(1), Pow(10, 19)), Sym("zettas"))  # nopep8
    CONVERSIONS["ZETTASECOND:HOUR"] = \
        Mul(Rat(Int(9), Mul(Int(25), Pow(10, 17))), Sym("zettas"))  # nopep8
    CONVERSIONS["ZETTASECOND:KILOSECOND"] = \
        Mul(Rat(Int(1), Pow(10, 18)), Sym("zettas"))  # nopep8
    CONVERSIONS["ZETTASECOND:MEGASECOND"] = \
        Mul(Rat(Int(1), Pow(10, 15)), Sym("zettas"))  # nopep8
    CONVERSIONS["ZETTASECOND:MICROSECOND"] = \
        Mul(Rat(Int(1), Pow(10, 27)), Sym("zettas"))  # nopep8
    CONVERSIONS["ZETTASECOND:MILLISECOND"] = \
        Mul(Rat(Int(1), Pow(10, 24)), Sym("zettas"))  # nopep8
    CONVERSIONS["ZETTASECOND:MINUTE"] = \
        Mul(Rat(Int(3), Mul(Int(5), Pow(10, 19))), Sym("zettas"))  # nopep8
    CONVERSIONS["ZETTASECOND:NANOSECOND"] = \
        Mul(Rat(Int(1), Pow(10, 30)), Sym("zettas"))  # nopep8
    CONVERSIONS["ZETTASECOND:PETASECOND"] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("zettas"))  # nopep8
    CONVERSIONS["ZETTASECOND:PICOSECOND"] = \
        Mul(Rat(Int(1), Pow(10, 33)), Sym("zettas"))  # nopep8
    CONVERSIONS["ZETTASECOND:SECOND"] = \
        Mul(Rat(Int(1), Pow(10, 21)), Sym("zettas"))  # nopep8
    CONVERSIONS["ZETTASECOND:TERASECOND"] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("zettas"))  # nopep8
    CONVERSIONS["ZETTASECOND:YOCTOSECOND"] = \
        Mul(Rat(Int(1), Pow(10, 45)), Sym("zettas"))  # nopep8
    CONVERSIONS["ZETTASECOND:YOTTASECOND"] = \
        Mul(Int(1000), Sym("zettas"))  # nopep8
    CONVERSIONS["ZETTASECOND:ZEPTOSECOND"] = \
        Mul(Rat(Int(1), Pow(10, 42)), Sym("zettas"))  # nopep8

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
