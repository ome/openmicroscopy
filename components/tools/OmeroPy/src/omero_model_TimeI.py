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

from omero.conversions import Add  # nopep8
from omero.conversions import Int  # nopep8
from omero.conversions import Mul  # nopep8
from omero.conversions import Pow  # nopep8
from omero.conversions import Rat  # nopep8
from omero.conversions import Sym  # nopep8


class TimeI(_omero_model.Time, UnitBase):

    try:
        UNIT_VALUES = sorted(UnitsTime._enumerators.values())
    except:
        # TODO: this occurs on Ice 3.4 and can be removed
        # once it has been dropped.
        UNIT_VALUES = [x for x in sorted(UnitsTime._names)]
        UNIT_VALUES = [getattr(UnitsTime, x) for x in UNIT_VALUES]
    CONVERSIONS = dict()
    for val in UNIT_VALUES:
        CONVERSIONS[val] = dict()
    CONVERSIONS[UnitsTime.ATTOSECOND][UnitsTime.CENTISECOND] = \
        Mul(Pow(10, 16), Sym("attos"))  # nopep8
    CONVERSIONS[UnitsTime.ATTOSECOND][UnitsTime.DAY] = \
        Mul(Mul(Int(864), Pow(10, 20)), Sym("attos"))  # nopep8
    CONVERSIONS[UnitsTime.ATTOSECOND][UnitsTime.DECASECOND] = \
        Mul(Pow(10, 19), Sym("attos"))  # nopep8
    CONVERSIONS[UnitsTime.ATTOSECOND][UnitsTime.DECISECOND] = \
        Mul(Pow(10, 17), Sym("attos"))  # nopep8
    CONVERSIONS[UnitsTime.ATTOSECOND][UnitsTime.EXASECOND] = \
        Mul(Pow(10, 36), Sym("attos"))  # nopep8
    CONVERSIONS[UnitsTime.ATTOSECOND][UnitsTime.FEMTOSECOND] = \
        Mul(Int(1000), Sym("attos"))  # nopep8
    CONVERSIONS[UnitsTime.ATTOSECOND][UnitsTime.GIGASECOND] = \
        Mul(Pow(10, 27), Sym("attos"))  # nopep8
    CONVERSIONS[UnitsTime.ATTOSECOND][UnitsTime.HECTOSECOND] = \
        Mul(Pow(10, 20), Sym("attos"))  # nopep8
    CONVERSIONS[UnitsTime.ATTOSECOND][UnitsTime.HOUR] = \
        Mul(Mul(Int(36), Pow(10, 20)), Sym("attos"))  # nopep8
    CONVERSIONS[UnitsTime.ATTOSECOND][UnitsTime.KILOSECOND] = \
        Mul(Pow(10, 21), Sym("attos"))  # nopep8
    CONVERSIONS[UnitsTime.ATTOSECOND][UnitsTime.MEGASECOND] = \
        Mul(Pow(10, 24), Sym("attos"))  # nopep8
    CONVERSIONS[UnitsTime.ATTOSECOND][UnitsTime.MICROSECOND] = \
        Mul(Pow(10, 12), Sym("attos"))  # nopep8
    CONVERSIONS[UnitsTime.ATTOSECOND][UnitsTime.MILLISECOND] = \
        Mul(Pow(10, 15), Sym("attos"))  # nopep8
    CONVERSIONS[UnitsTime.ATTOSECOND][UnitsTime.MINUTE] = \
        Mul(Mul(Int(6), Pow(10, 19)), Sym("attos"))  # nopep8
    CONVERSIONS[UnitsTime.ATTOSECOND][UnitsTime.NANOSECOND] = \
        Mul(Pow(10, 9), Sym("attos"))  # nopep8
    CONVERSIONS[UnitsTime.ATTOSECOND][UnitsTime.PETASECOND] = \
        Mul(Pow(10, 33), Sym("attos"))  # nopep8
    CONVERSIONS[UnitsTime.ATTOSECOND][UnitsTime.PICOSECOND] = \
        Mul(Pow(10, 6), Sym("attos"))  # nopep8
    CONVERSIONS[UnitsTime.ATTOSECOND][UnitsTime.SECOND] = \
        Mul(Pow(10, 18), Sym("attos"))  # nopep8
    CONVERSIONS[UnitsTime.ATTOSECOND][UnitsTime.TERASECOND] = \
        Mul(Pow(10, 30), Sym("attos"))  # nopep8
    CONVERSIONS[UnitsTime.ATTOSECOND][UnitsTime.YOCTOSECOND] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("attos"))  # nopep8
    CONVERSIONS[UnitsTime.ATTOSECOND][UnitsTime.YOTTASECOND] = \
        Mul(Pow(10, 42), Sym("attos"))  # nopep8
    CONVERSIONS[UnitsTime.ATTOSECOND][UnitsTime.ZEPTOSECOND] = \
        Mul(Rat(Int(1), Int(1000)), Sym("attos"))  # nopep8
    CONVERSIONS[UnitsTime.ATTOSECOND][UnitsTime.ZETTASECOND] = \
        Mul(Pow(10, 39), Sym("attos"))  # nopep8
    CONVERSIONS[UnitsTime.CENTISECOND][UnitsTime.ATTOSECOND] = \
        Mul(Rat(Int(1), Pow(10, 16)), Sym("centis"))  # nopep8
    CONVERSIONS[UnitsTime.CENTISECOND][UnitsTime.DAY] = \
        Mul(Mul(Int(864), Pow(10, 4)), Sym("centis"))  # nopep8
    CONVERSIONS[UnitsTime.CENTISECOND][UnitsTime.DECASECOND] = \
        Mul(Int(1000), Sym("centis"))  # nopep8
    CONVERSIONS[UnitsTime.CENTISECOND][UnitsTime.DECISECOND] = \
        Mul(Int(10), Sym("centis"))  # nopep8
    CONVERSIONS[UnitsTime.CENTISECOND][UnitsTime.EXASECOND] = \
        Mul(Pow(10, 20), Sym("centis"))  # nopep8
    CONVERSIONS[UnitsTime.CENTISECOND][UnitsTime.FEMTOSECOND] = \
        Mul(Rat(Int(1), Pow(10, 13)), Sym("centis"))  # nopep8
    CONVERSIONS[UnitsTime.CENTISECOND][UnitsTime.GIGASECOND] = \
        Mul(Pow(10, 11), Sym("centis"))  # nopep8
    CONVERSIONS[UnitsTime.CENTISECOND][UnitsTime.HECTOSECOND] = \
        Mul(Pow(10, 4), Sym("centis"))  # nopep8
    CONVERSIONS[UnitsTime.CENTISECOND][UnitsTime.HOUR] = \
        Mul(Mul(Int(36), Pow(10, 4)), Sym("centis"))  # nopep8
    CONVERSIONS[UnitsTime.CENTISECOND][UnitsTime.KILOSECOND] = \
        Mul(Pow(10, 5), Sym("centis"))  # nopep8
    CONVERSIONS[UnitsTime.CENTISECOND][UnitsTime.MEGASECOND] = \
        Mul(Pow(10, 8), Sym("centis"))  # nopep8
    CONVERSIONS[UnitsTime.CENTISECOND][UnitsTime.MICROSECOND] = \
        Mul(Rat(Int(1), Pow(10, 4)), Sym("centis"))  # nopep8
    CONVERSIONS[UnitsTime.CENTISECOND][UnitsTime.MILLISECOND] = \
        Mul(Rat(Int(1), Int(10)), Sym("centis"))  # nopep8
    CONVERSIONS[UnitsTime.CENTISECOND][UnitsTime.MINUTE] = \
        Mul(Int(6000), Sym("centis"))  # nopep8
    CONVERSIONS[UnitsTime.CENTISECOND][UnitsTime.NANOSECOND] = \
        Mul(Rat(Int(1), Pow(10, 7)), Sym("centis"))  # nopep8
    CONVERSIONS[UnitsTime.CENTISECOND][UnitsTime.PETASECOND] = \
        Mul(Pow(10, 17), Sym("centis"))  # nopep8
    CONVERSIONS[UnitsTime.CENTISECOND][UnitsTime.PICOSECOND] = \
        Mul(Rat(Int(1), Pow(10, 10)), Sym("centis"))  # nopep8
    CONVERSIONS[UnitsTime.CENTISECOND][UnitsTime.SECOND] = \
        Mul(Int(100), Sym("centis"))  # nopep8
    CONVERSIONS[UnitsTime.CENTISECOND][UnitsTime.TERASECOND] = \
        Mul(Pow(10, 14), Sym("centis"))  # nopep8
    CONVERSIONS[UnitsTime.CENTISECOND][UnitsTime.YOCTOSECOND] = \
        Mul(Rat(Int(1), Pow(10, 22)), Sym("centis"))  # nopep8
    CONVERSIONS[UnitsTime.CENTISECOND][UnitsTime.YOTTASECOND] = \
        Mul(Pow(10, 26), Sym("centis"))  # nopep8
    CONVERSIONS[UnitsTime.CENTISECOND][UnitsTime.ZEPTOSECOND] = \
        Mul(Rat(Int(1), Pow(10, 19)), Sym("centis"))  # nopep8
    CONVERSIONS[UnitsTime.CENTISECOND][UnitsTime.ZETTASECOND] = \
        Mul(Pow(10, 23), Sym("centis"))  # nopep8
    CONVERSIONS[UnitsTime.DAY][UnitsTime.ATTOSECOND] = \
        Mul(Rat(Int(1), Mul(Int(864), Pow(10, 20))), Sym("d"))  # nopep8
    CONVERSIONS[UnitsTime.DAY][UnitsTime.CENTISECOND] = \
        Mul(Rat(Int(1), Mul(Int(864), Pow(10, 4))), Sym("d"))  # nopep8
    CONVERSIONS[UnitsTime.DAY][UnitsTime.DECASECOND] = \
        Mul(Rat(Int(1), Int(8640)), Sym("d"))  # nopep8
    CONVERSIONS[UnitsTime.DAY][UnitsTime.DECISECOND] = \
        Mul(Rat(Int(1), Int(864000)), Sym("d"))  # nopep8
    CONVERSIONS[UnitsTime.DAY][UnitsTime.EXASECOND] = \
        Mul(Rat(Mul(Int(3125), Pow(10, 11)), Int(27)), Sym("d"))  # nopep8
    CONVERSIONS[UnitsTime.DAY][UnitsTime.FEMTOSECOND] = \
        Mul(Rat(Int(1), Mul(Int(864), Pow(10, 17))), Sym("d"))  # nopep8
    CONVERSIONS[UnitsTime.DAY][UnitsTime.GIGASECOND] = \
        Mul(Rat(Int(312500), Int(27)), Sym("d"))  # nopep8
    CONVERSIONS[UnitsTime.DAY][UnitsTime.HECTOSECOND] = \
        Mul(Rat(Int(1), Int(864)), Sym("d"))  # nopep8
    CONVERSIONS[UnitsTime.DAY][UnitsTime.HOUR] = \
        Mul(Rat(Int(1), Int(24)), Sym("d"))  # nopep8
    CONVERSIONS[UnitsTime.DAY][UnitsTime.KILOSECOND] = \
        Mul(Rat(Int(5), Int(432)), Sym("d"))  # nopep8
    CONVERSIONS[UnitsTime.DAY][UnitsTime.MEGASECOND] = \
        Mul(Rat(Int(625), Int(54)), Sym("d"))  # nopep8
    CONVERSIONS[UnitsTime.DAY][UnitsTime.MICROSECOND] = \
        Mul(Rat(Int(1), Mul(Int(864), Pow(10, 8))), Sym("d"))  # nopep8
    CONVERSIONS[UnitsTime.DAY][UnitsTime.MILLISECOND] = \
        Mul(Rat(Int(1), Mul(Int(864), Pow(10, 5))), Sym("d"))  # nopep8
    CONVERSIONS[UnitsTime.DAY][UnitsTime.MINUTE] = \
        Mul(Rat(Int(1), Int(1440)), Sym("d"))  # nopep8
    CONVERSIONS[UnitsTime.DAY][UnitsTime.NANOSECOND] = \
        Mul(Rat(Int(1), Mul(Int(864), Pow(10, 11))), Sym("d"))  # nopep8
    CONVERSIONS[UnitsTime.DAY][UnitsTime.PETASECOND] = \
        Mul(Rat(Mul(Int(3125), Pow(10, 8)), Int(27)), Sym("d"))  # nopep8
    CONVERSIONS[UnitsTime.DAY][UnitsTime.PICOSECOND] = \
        Mul(Rat(Int(1), Mul(Int(864), Pow(10, 14))), Sym("d"))  # nopep8
    CONVERSIONS[UnitsTime.DAY][UnitsTime.SECOND] = \
        Mul(Rat(Int(1), Int(86400)), Sym("d"))  # nopep8
    CONVERSIONS[UnitsTime.DAY][UnitsTime.TERASECOND] = \
        Mul(Rat(Mul(Int(3125), Pow(10, 5)), Int(27)), Sym("d"))  # nopep8
    CONVERSIONS[UnitsTime.DAY][UnitsTime.YOCTOSECOND] = \
        Mul(Rat(Int(1), Mul(Int(864), Pow(10, 26))), Sym("d"))  # nopep8
    CONVERSIONS[UnitsTime.DAY][UnitsTime.YOTTASECOND] = \
        Mul(Rat(Mul(Int(3125), Pow(10, 17)), Int(27)), Sym("d"))  # nopep8
    CONVERSIONS[UnitsTime.DAY][UnitsTime.ZEPTOSECOND] = \
        Mul(Rat(Int(1), Mul(Int(864), Pow(10, 23))), Sym("d"))  # nopep8
    CONVERSIONS[UnitsTime.DAY][UnitsTime.ZETTASECOND] = \
        Mul(Rat(Mul(Int(3125), Pow(10, 14)), Int(27)), Sym("d"))  # nopep8
    CONVERSIONS[UnitsTime.DECASECOND][UnitsTime.ATTOSECOND] = \
        Mul(Rat(Int(1), Pow(10, 19)), Sym("decas"))  # nopep8
    CONVERSIONS[UnitsTime.DECASECOND][UnitsTime.CENTISECOND] = \
        Mul(Rat(Int(1), Int(1000)), Sym("decas"))  # nopep8
    CONVERSIONS[UnitsTime.DECASECOND][UnitsTime.DAY] = \
        Mul(Int(8640), Sym("decas"))  # nopep8
    CONVERSIONS[UnitsTime.DECASECOND][UnitsTime.DECISECOND] = \
        Mul(Rat(Int(1), Int(100)), Sym("decas"))  # nopep8
    CONVERSIONS[UnitsTime.DECASECOND][UnitsTime.EXASECOND] = \
        Mul(Pow(10, 17), Sym("decas"))  # nopep8
    CONVERSIONS[UnitsTime.DECASECOND][UnitsTime.FEMTOSECOND] = \
        Mul(Rat(Int(1), Pow(10, 16)), Sym("decas"))  # nopep8
    CONVERSIONS[UnitsTime.DECASECOND][UnitsTime.GIGASECOND] = \
        Mul(Pow(10, 8), Sym("decas"))  # nopep8
    CONVERSIONS[UnitsTime.DECASECOND][UnitsTime.HECTOSECOND] = \
        Mul(Int(10), Sym("decas"))  # nopep8
    CONVERSIONS[UnitsTime.DECASECOND][UnitsTime.HOUR] = \
        Mul(Int(360), Sym("decas"))  # nopep8
    CONVERSIONS[UnitsTime.DECASECOND][UnitsTime.KILOSECOND] = \
        Mul(Int(100), Sym("decas"))  # nopep8
    CONVERSIONS[UnitsTime.DECASECOND][UnitsTime.MEGASECOND] = \
        Mul(Pow(10, 5), Sym("decas"))  # nopep8
    CONVERSIONS[UnitsTime.DECASECOND][UnitsTime.MICROSECOND] = \
        Mul(Rat(Int(1), Pow(10, 7)), Sym("decas"))  # nopep8
    CONVERSIONS[UnitsTime.DECASECOND][UnitsTime.MILLISECOND] = \
        Mul(Rat(Int(1), Pow(10, 4)), Sym("decas"))  # nopep8
    CONVERSIONS[UnitsTime.DECASECOND][UnitsTime.MINUTE] = \
        Mul(Int(6), Sym("decas"))  # nopep8
    CONVERSIONS[UnitsTime.DECASECOND][UnitsTime.NANOSECOND] = \
        Mul(Rat(Int(1), Pow(10, 10)), Sym("decas"))  # nopep8
    CONVERSIONS[UnitsTime.DECASECOND][UnitsTime.PETASECOND] = \
        Mul(Pow(10, 14), Sym("decas"))  # nopep8
    CONVERSIONS[UnitsTime.DECASECOND][UnitsTime.PICOSECOND] = \
        Mul(Rat(Int(1), Pow(10, 13)), Sym("decas"))  # nopep8
    CONVERSIONS[UnitsTime.DECASECOND][UnitsTime.SECOND] = \
        Mul(Rat(Int(1), Int(10)), Sym("decas"))  # nopep8
    CONVERSIONS[UnitsTime.DECASECOND][UnitsTime.TERASECOND] = \
        Mul(Pow(10, 11), Sym("decas"))  # nopep8
    CONVERSIONS[UnitsTime.DECASECOND][UnitsTime.YOCTOSECOND] = \
        Mul(Rat(Int(1), Pow(10, 25)), Sym("decas"))  # nopep8
    CONVERSIONS[UnitsTime.DECASECOND][UnitsTime.YOTTASECOND] = \
        Mul(Pow(10, 23), Sym("decas"))  # nopep8
    CONVERSIONS[UnitsTime.DECASECOND][UnitsTime.ZEPTOSECOND] = \
        Mul(Rat(Int(1), Pow(10, 22)), Sym("decas"))  # nopep8
    CONVERSIONS[UnitsTime.DECASECOND][UnitsTime.ZETTASECOND] = \
        Mul(Pow(10, 20), Sym("decas"))  # nopep8
    CONVERSIONS[UnitsTime.DECISECOND][UnitsTime.ATTOSECOND] = \
        Mul(Rat(Int(1), Pow(10, 17)), Sym("decis"))  # nopep8
    CONVERSIONS[UnitsTime.DECISECOND][UnitsTime.CENTISECOND] = \
        Mul(Rat(Int(1), Int(10)), Sym("decis"))  # nopep8
    CONVERSIONS[UnitsTime.DECISECOND][UnitsTime.DAY] = \
        Mul(Int(864000), Sym("decis"))  # nopep8
    CONVERSIONS[UnitsTime.DECISECOND][UnitsTime.DECASECOND] = \
        Mul(Int(100), Sym("decis"))  # nopep8
    CONVERSIONS[UnitsTime.DECISECOND][UnitsTime.EXASECOND] = \
        Mul(Pow(10, 19), Sym("decis"))  # nopep8
    CONVERSIONS[UnitsTime.DECISECOND][UnitsTime.FEMTOSECOND] = \
        Mul(Rat(Int(1), Pow(10, 14)), Sym("decis"))  # nopep8
    CONVERSIONS[UnitsTime.DECISECOND][UnitsTime.GIGASECOND] = \
        Mul(Pow(10, 10), Sym("decis"))  # nopep8
    CONVERSIONS[UnitsTime.DECISECOND][UnitsTime.HECTOSECOND] = \
        Mul(Int(1000), Sym("decis"))  # nopep8
    CONVERSIONS[UnitsTime.DECISECOND][UnitsTime.HOUR] = \
        Mul(Int(36000), Sym("decis"))  # nopep8
    CONVERSIONS[UnitsTime.DECISECOND][UnitsTime.KILOSECOND] = \
        Mul(Pow(10, 4), Sym("decis"))  # nopep8
    CONVERSIONS[UnitsTime.DECISECOND][UnitsTime.MEGASECOND] = \
        Mul(Pow(10, 7), Sym("decis"))  # nopep8
    CONVERSIONS[UnitsTime.DECISECOND][UnitsTime.MICROSECOND] = \
        Mul(Rat(Int(1), Pow(10, 5)), Sym("decis"))  # nopep8
    CONVERSIONS[UnitsTime.DECISECOND][UnitsTime.MILLISECOND] = \
        Mul(Rat(Int(1), Int(100)), Sym("decis"))  # nopep8
    CONVERSIONS[UnitsTime.DECISECOND][UnitsTime.MINUTE] = \
        Mul(Int(600), Sym("decis"))  # nopep8
    CONVERSIONS[UnitsTime.DECISECOND][UnitsTime.NANOSECOND] = \
        Mul(Rat(Int(1), Pow(10, 8)), Sym("decis"))  # nopep8
    CONVERSIONS[UnitsTime.DECISECOND][UnitsTime.PETASECOND] = \
        Mul(Pow(10, 16), Sym("decis"))  # nopep8
    CONVERSIONS[UnitsTime.DECISECOND][UnitsTime.PICOSECOND] = \
        Mul(Rat(Int(1), Pow(10, 11)), Sym("decis"))  # nopep8
    CONVERSIONS[UnitsTime.DECISECOND][UnitsTime.SECOND] = \
        Mul(Int(10), Sym("decis"))  # nopep8
    CONVERSIONS[UnitsTime.DECISECOND][UnitsTime.TERASECOND] = \
        Mul(Pow(10, 13), Sym("decis"))  # nopep8
    CONVERSIONS[UnitsTime.DECISECOND][UnitsTime.YOCTOSECOND] = \
        Mul(Rat(Int(1), Pow(10, 23)), Sym("decis"))  # nopep8
    CONVERSIONS[UnitsTime.DECISECOND][UnitsTime.YOTTASECOND] = \
        Mul(Pow(10, 25), Sym("decis"))  # nopep8
    CONVERSIONS[UnitsTime.DECISECOND][UnitsTime.ZEPTOSECOND] = \
        Mul(Rat(Int(1), Pow(10, 20)), Sym("decis"))  # nopep8
    CONVERSIONS[UnitsTime.DECISECOND][UnitsTime.ZETTASECOND] = \
        Mul(Pow(10, 22), Sym("decis"))  # nopep8
    CONVERSIONS[UnitsTime.EXASECOND][UnitsTime.ATTOSECOND] = \
        Mul(Rat(Int(1), Pow(10, 36)), Sym("exas"))  # nopep8
    CONVERSIONS[UnitsTime.EXASECOND][UnitsTime.CENTISECOND] = \
        Mul(Rat(Int(1), Pow(10, 20)), Sym("exas"))  # nopep8
    CONVERSIONS[UnitsTime.EXASECOND][UnitsTime.DAY] = \
        Mul(Rat(Int(27), Mul(Int(3125), Pow(10, 11))), Sym("exas"))  # nopep8
    CONVERSIONS[UnitsTime.EXASECOND][UnitsTime.DECASECOND] = \
        Mul(Rat(Int(1), Pow(10, 17)), Sym("exas"))  # nopep8
    CONVERSIONS[UnitsTime.EXASECOND][UnitsTime.DECISECOND] = \
        Mul(Rat(Int(1), Pow(10, 19)), Sym("exas"))  # nopep8
    CONVERSIONS[UnitsTime.EXASECOND][UnitsTime.FEMTOSECOND] = \
        Mul(Rat(Int(1), Pow(10, 33)), Sym("exas"))  # nopep8
    CONVERSIONS[UnitsTime.EXASECOND][UnitsTime.GIGASECOND] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("exas"))  # nopep8
    CONVERSIONS[UnitsTime.EXASECOND][UnitsTime.HECTOSECOND] = \
        Mul(Rat(Int(1), Pow(10, 16)), Sym("exas"))  # nopep8
    CONVERSIONS[UnitsTime.EXASECOND][UnitsTime.HOUR] = \
        Mul(Rat(Int(9), Mul(Int(25), Pow(10, 14))), Sym("exas"))  # nopep8
    CONVERSIONS[UnitsTime.EXASECOND][UnitsTime.KILOSECOND] = \
        Mul(Rat(Int(1), Pow(10, 15)), Sym("exas"))  # nopep8
    CONVERSIONS[UnitsTime.EXASECOND][UnitsTime.MEGASECOND] = \
        Mul(Rat(Int(1), Pow(10, 12)), Sym("exas"))  # nopep8
    CONVERSIONS[UnitsTime.EXASECOND][UnitsTime.MICROSECOND] = \
        Mul(Rat(Int(1), Pow(10, 24)), Sym("exas"))  # nopep8
    CONVERSIONS[UnitsTime.EXASECOND][UnitsTime.MILLISECOND] = \
        Mul(Rat(Int(1), Pow(10, 21)), Sym("exas"))  # nopep8
    CONVERSIONS[UnitsTime.EXASECOND][UnitsTime.MINUTE] = \
        Mul(Rat(Int(3), Mul(Int(5), Pow(10, 16))), Sym("exas"))  # nopep8
    CONVERSIONS[UnitsTime.EXASECOND][UnitsTime.NANOSECOND] = \
        Mul(Rat(Int(1), Pow(10, 27)), Sym("exas"))  # nopep8
    CONVERSIONS[UnitsTime.EXASECOND][UnitsTime.PETASECOND] = \
        Mul(Rat(Int(1), Int(1000)), Sym("exas"))  # nopep8
    CONVERSIONS[UnitsTime.EXASECOND][UnitsTime.PICOSECOND] = \
        Mul(Rat(Int(1), Pow(10, 30)), Sym("exas"))  # nopep8
    CONVERSIONS[UnitsTime.EXASECOND][UnitsTime.SECOND] = \
        Mul(Rat(Int(1), Pow(10, 18)), Sym("exas"))  # nopep8
    CONVERSIONS[UnitsTime.EXASECOND][UnitsTime.TERASECOND] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("exas"))  # nopep8
    CONVERSIONS[UnitsTime.EXASECOND][UnitsTime.YOCTOSECOND] = \
        Mul(Rat(Int(1), Pow(10, 42)), Sym("exas"))  # nopep8
    CONVERSIONS[UnitsTime.EXASECOND][UnitsTime.YOTTASECOND] = \
        Mul(Pow(10, 6), Sym("exas"))  # nopep8
    CONVERSIONS[UnitsTime.EXASECOND][UnitsTime.ZEPTOSECOND] = \
        Mul(Rat(Int(1), Pow(10, 39)), Sym("exas"))  # nopep8
    CONVERSIONS[UnitsTime.EXASECOND][UnitsTime.ZETTASECOND] = \
        Mul(Int(1000), Sym("exas"))  # nopep8
    CONVERSIONS[UnitsTime.FEMTOSECOND][UnitsTime.ATTOSECOND] = \
        Mul(Rat(Int(1), Int(1000)), Sym("femtos"))  # nopep8
    CONVERSIONS[UnitsTime.FEMTOSECOND][UnitsTime.CENTISECOND] = \
        Mul(Pow(10, 13), Sym("femtos"))  # nopep8
    CONVERSIONS[UnitsTime.FEMTOSECOND][UnitsTime.DAY] = \
        Mul(Mul(Int(864), Pow(10, 17)), Sym("femtos"))  # nopep8
    CONVERSIONS[UnitsTime.FEMTOSECOND][UnitsTime.DECASECOND] = \
        Mul(Pow(10, 16), Sym("femtos"))  # nopep8
    CONVERSIONS[UnitsTime.FEMTOSECOND][UnitsTime.DECISECOND] = \
        Mul(Pow(10, 14), Sym("femtos"))  # nopep8
    CONVERSIONS[UnitsTime.FEMTOSECOND][UnitsTime.EXASECOND] = \
        Mul(Pow(10, 33), Sym("femtos"))  # nopep8
    CONVERSIONS[UnitsTime.FEMTOSECOND][UnitsTime.GIGASECOND] = \
        Mul(Pow(10, 24), Sym("femtos"))  # nopep8
    CONVERSIONS[UnitsTime.FEMTOSECOND][UnitsTime.HECTOSECOND] = \
        Mul(Pow(10, 17), Sym("femtos"))  # nopep8
    CONVERSIONS[UnitsTime.FEMTOSECOND][UnitsTime.HOUR] = \
        Mul(Mul(Int(36), Pow(10, 17)), Sym("femtos"))  # nopep8
    CONVERSIONS[UnitsTime.FEMTOSECOND][UnitsTime.KILOSECOND] = \
        Mul(Pow(10, 18), Sym("femtos"))  # nopep8
    CONVERSIONS[UnitsTime.FEMTOSECOND][UnitsTime.MEGASECOND] = \
        Mul(Pow(10, 21), Sym("femtos"))  # nopep8
    CONVERSIONS[UnitsTime.FEMTOSECOND][UnitsTime.MICROSECOND] = \
        Mul(Pow(10, 9), Sym("femtos"))  # nopep8
    CONVERSIONS[UnitsTime.FEMTOSECOND][UnitsTime.MILLISECOND] = \
        Mul(Pow(10, 12), Sym("femtos"))  # nopep8
    CONVERSIONS[UnitsTime.FEMTOSECOND][UnitsTime.MINUTE] = \
        Mul(Mul(Int(6), Pow(10, 16)), Sym("femtos"))  # nopep8
    CONVERSIONS[UnitsTime.FEMTOSECOND][UnitsTime.NANOSECOND] = \
        Mul(Pow(10, 6), Sym("femtos"))  # nopep8
    CONVERSIONS[UnitsTime.FEMTOSECOND][UnitsTime.PETASECOND] = \
        Mul(Pow(10, 30), Sym("femtos"))  # nopep8
    CONVERSIONS[UnitsTime.FEMTOSECOND][UnitsTime.PICOSECOND] = \
        Mul(Int(1000), Sym("femtos"))  # nopep8
    CONVERSIONS[UnitsTime.FEMTOSECOND][UnitsTime.SECOND] = \
        Mul(Pow(10, 15), Sym("femtos"))  # nopep8
    CONVERSIONS[UnitsTime.FEMTOSECOND][UnitsTime.TERASECOND] = \
        Mul(Pow(10, 27), Sym("femtos"))  # nopep8
    CONVERSIONS[UnitsTime.FEMTOSECOND][UnitsTime.YOCTOSECOND] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("femtos"))  # nopep8
    CONVERSIONS[UnitsTime.FEMTOSECOND][UnitsTime.YOTTASECOND] = \
        Mul(Pow(10, 39), Sym("femtos"))  # nopep8
    CONVERSIONS[UnitsTime.FEMTOSECOND][UnitsTime.ZEPTOSECOND] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("femtos"))  # nopep8
    CONVERSIONS[UnitsTime.FEMTOSECOND][UnitsTime.ZETTASECOND] = \
        Mul(Pow(10, 36), Sym("femtos"))  # nopep8
    CONVERSIONS[UnitsTime.GIGASECOND][UnitsTime.ATTOSECOND] = \
        Mul(Rat(Int(1), Pow(10, 27)), Sym("gigas"))  # nopep8
    CONVERSIONS[UnitsTime.GIGASECOND][UnitsTime.CENTISECOND] = \
        Mul(Rat(Int(1), Pow(10, 11)), Sym("gigas"))  # nopep8
    CONVERSIONS[UnitsTime.GIGASECOND][UnitsTime.DAY] = \
        Mul(Rat(Int(27), Int(312500)), Sym("gigas"))  # nopep8
    CONVERSIONS[UnitsTime.GIGASECOND][UnitsTime.DECASECOND] = \
        Mul(Rat(Int(1), Pow(10, 8)), Sym("gigas"))  # nopep8
    CONVERSIONS[UnitsTime.GIGASECOND][UnitsTime.DECISECOND] = \
        Mul(Rat(Int(1), Pow(10, 10)), Sym("gigas"))  # nopep8
    CONVERSIONS[UnitsTime.GIGASECOND][UnitsTime.EXASECOND] = \
        Mul(Pow(10, 9), Sym("gigas"))  # nopep8
    CONVERSIONS[UnitsTime.GIGASECOND][UnitsTime.FEMTOSECOND] = \
        Mul(Rat(Int(1), Pow(10, 24)), Sym("gigas"))  # nopep8
    CONVERSIONS[UnitsTime.GIGASECOND][UnitsTime.HECTOSECOND] = \
        Mul(Rat(Int(1), Pow(10, 7)), Sym("gigas"))  # nopep8
    CONVERSIONS[UnitsTime.GIGASECOND][UnitsTime.HOUR] = \
        Mul(Rat(Int(9), Mul(Int(25), Pow(10, 5))), Sym("gigas"))  # nopep8
    CONVERSIONS[UnitsTime.GIGASECOND][UnitsTime.KILOSECOND] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("gigas"))  # nopep8
    CONVERSIONS[UnitsTime.GIGASECOND][UnitsTime.MEGASECOND] = \
        Mul(Rat(Int(1), Int(1000)), Sym("gigas"))  # nopep8
    CONVERSIONS[UnitsTime.GIGASECOND][UnitsTime.MICROSECOND] = \
        Mul(Rat(Int(1), Pow(10, 15)), Sym("gigas"))  # nopep8
    CONVERSIONS[UnitsTime.GIGASECOND][UnitsTime.MILLISECOND] = \
        Mul(Rat(Int(1), Pow(10, 12)), Sym("gigas"))  # nopep8
    CONVERSIONS[UnitsTime.GIGASECOND][UnitsTime.MINUTE] = \
        Mul(Rat(Int(3), Mul(Int(5), Pow(10, 7))), Sym("gigas"))  # nopep8
    CONVERSIONS[UnitsTime.GIGASECOND][UnitsTime.NANOSECOND] = \
        Mul(Rat(Int(1), Pow(10, 18)), Sym("gigas"))  # nopep8
    CONVERSIONS[UnitsTime.GIGASECOND][UnitsTime.PETASECOND] = \
        Mul(Pow(10, 6), Sym("gigas"))  # nopep8
    CONVERSIONS[UnitsTime.GIGASECOND][UnitsTime.PICOSECOND] = \
        Mul(Rat(Int(1), Pow(10, 21)), Sym("gigas"))  # nopep8
    CONVERSIONS[UnitsTime.GIGASECOND][UnitsTime.SECOND] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("gigas"))  # nopep8
    CONVERSIONS[UnitsTime.GIGASECOND][UnitsTime.TERASECOND] = \
        Mul(Int(1000), Sym("gigas"))  # nopep8
    CONVERSIONS[UnitsTime.GIGASECOND][UnitsTime.YOCTOSECOND] = \
        Mul(Rat(Int(1), Pow(10, 33)), Sym("gigas"))  # nopep8
    CONVERSIONS[UnitsTime.GIGASECOND][UnitsTime.YOTTASECOND] = \
        Mul(Pow(10, 15), Sym("gigas"))  # nopep8
    CONVERSIONS[UnitsTime.GIGASECOND][UnitsTime.ZEPTOSECOND] = \
        Mul(Rat(Int(1), Pow(10, 30)), Sym("gigas"))  # nopep8
    CONVERSIONS[UnitsTime.GIGASECOND][UnitsTime.ZETTASECOND] = \
        Mul(Pow(10, 12), Sym("gigas"))  # nopep8
    CONVERSIONS[UnitsTime.HECTOSECOND][UnitsTime.ATTOSECOND] = \
        Mul(Rat(Int(1), Pow(10, 20)), Sym("hectos"))  # nopep8
    CONVERSIONS[UnitsTime.HECTOSECOND][UnitsTime.CENTISECOND] = \
        Mul(Rat(Int(1), Pow(10, 4)), Sym("hectos"))  # nopep8
    CONVERSIONS[UnitsTime.HECTOSECOND][UnitsTime.DAY] = \
        Mul(Int(864), Sym("hectos"))  # nopep8
    CONVERSIONS[UnitsTime.HECTOSECOND][UnitsTime.DECASECOND] = \
        Mul(Rat(Int(1), Int(10)), Sym("hectos"))  # nopep8
    CONVERSIONS[UnitsTime.HECTOSECOND][UnitsTime.DECISECOND] = \
        Mul(Rat(Int(1), Int(1000)), Sym("hectos"))  # nopep8
    CONVERSIONS[UnitsTime.HECTOSECOND][UnitsTime.EXASECOND] = \
        Mul(Pow(10, 16), Sym("hectos"))  # nopep8
    CONVERSIONS[UnitsTime.HECTOSECOND][UnitsTime.FEMTOSECOND] = \
        Mul(Rat(Int(1), Pow(10, 17)), Sym("hectos"))  # nopep8
    CONVERSIONS[UnitsTime.HECTOSECOND][UnitsTime.GIGASECOND] = \
        Mul(Pow(10, 7), Sym("hectos"))  # nopep8
    CONVERSIONS[UnitsTime.HECTOSECOND][UnitsTime.HOUR] = \
        Mul(Int(36), Sym("hectos"))  # nopep8
    CONVERSIONS[UnitsTime.HECTOSECOND][UnitsTime.KILOSECOND] = \
        Mul(Int(10), Sym("hectos"))  # nopep8
    CONVERSIONS[UnitsTime.HECTOSECOND][UnitsTime.MEGASECOND] = \
        Mul(Pow(10, 4), Sym("hectos"))  # nopep8
    CONVERSIONS[UnitsTime.HECTOSECOND][UnitsTime.MICROSECOND] = \
        Mul(Rat(Int(1), Pow(10, 8)), Sym("hectos"))  # nopep8
    CONVERSIONS[UnitsTime.HECTOSECOND][UnitsTime.MILLISECOND] = \
        Mul(Rat(Int(1), Pow(10, 5)), Sym("hectos"))  # nopep8
    CONVERSIONS[UnitsTime.HECTOSECOND][UnitsTime.MINUTE] = \
        Mul(Rat(Int(3), Int(5)), Sym("hectos"))  # nopep8
    CONVERSIONS[UnitsTime.HECTOSECOND][UnitsTime.NANOSECOND] = \
        Mul(Rat(Int(1), Pow(10, 11)), Sym("hectos"))  # nopep8
    CONVERSIONS[UnitsTime.HECTOSECOND][UnitsTime.PETASECOND] = \
        Mul(Pow(10, 13), Sym("hectos"))  # nopep8
    CONVERSIONS[UnitsTime.HECTOSECOND][UnitsTime.PICOSECOND] = \
        Mul(Rat(Int(1), Pow(10, 14)), Sym("hectos"))  # nopep8
    CONVERSIONS[UnitsTime.HECTOSECOND][UnitsTime.SECOND] = \
        Mul(Rat(Int(1), Int(100)), Sym("hectos"))  # nopep8
    CONVERSIONS[UnitsTime.HECTOSECOND][UnitsTime.TERASECOND] = \
        Mul(Pow(10, 10), Sym("hectos"))  # nopep8
    CONVERSIONS[UnitsTime.HECTOSECOND][UnitsTime.YOCTOSECOND] = \
        Mul(Rat(Int(1), Pow(10, 26)), Sym("hectos"))  # nopep8
    CONVERSIONS[UnitsTime.HECTOSECOND][UnitsTime.YOTTASECOND] = \
        Mul(Pow(10, 22), Sym("hectos"))  # nopep8
    CONVERSIONS[UnitsTime.HECTOSECOND][UnitsTime.ZEPTOSECOND] = \
        Mul(Rat(Int(1), Pow(10, 23)), Sym("hectos"))  # nopep8
    CONVERSIONS[UnitsTime.HECTOSECOND][UnitsTime.ZETTASECOND] = \
        Mul(Pow(10, 19), Sym("hectos"))  # nopep8
    CONVERSIONS[UnitsTime.HOUR][UnitsTime.ATTOSECOND] = \
        Mul(Rat(Int(1), Mul(Int(36), Pow(10, 20))), Sym("h"))  # nopep8
    CONVERSIONS[UnitsTime.HOUR][UnitsTime.CENTISECOND] = \
        Mul(Rat(Int(1), Mul(Int(36), Pow(10, 4))), Sym("h"))  # nopep8
    CONVERSIONS[UnitsTime.HOUR][UnitsTime.DAY] = \
        Mul(Int(24), Sym("h"))  # nopep8
    CONVERSIONS[UnitsTime.HOUR][UnitsTime.DECASECOND] = \
        Mul(Rat(Int(1), Int(360)), Sym("h"))  # nopep8
    CONVERSIONS[UnitsTime.HOUR][UnitsTime.DECISECOND] = \
        Mul(Rat(Int(1), Int(36000)), Sym("h"))  # nopep8
    CONVERSIONS[UnitsTime.HOUR][UnitsTime.EXASECOND] = \
        Mul(Rat(Mul(Int(25), Pow(10, 14)), Int(9)), Sym("h"))  # nopep8
    CONVERSIONS[UnitsTime.HOUR][UnitsTime.FEMTOSECOND] = \
        Mul(Rat(Int(1), Mul(Int(36), Pow(10, 17))), Sym("h"))  # nopep8
    CONVERSIONS[UnitsTime.HOUR][UnitsTime.GIGASECOND] = \
        Mul(Rat(Mul(Int(25), Pow(10, 5)), Int(9)), Sym("h"))  # nopep8
    CONVERSIONS[UnitsTime.HOUR][UnitsTime.HECTOSECOND] = \
        Mul(Rat(Int(1), Int(36)), Sym("h"))  # nopep8
    CONVERSIONS[UnitsTime.HOUR][UnitsTime.KILOSECOND] = \
        Mul(Rat(Int(5), Int(18)), Sym("h"))  # nopep8
    CONVERSIONS[UnitsTime.HOUR][UnitsTime.MEGASECOND] = \
        Mul(Rat(Int(2500), Int(9)), Sym("h"))  # nopep8
    CONVERSIONS[UnitsTime.HOUR][UnitsTime.MICROSECOND] = \
        Mul(Rat(Int(1), Mul(Int(36), Pow(10, 8))), Sym("h"))  # nopep8
    CONVERSIONS[UnitsTime.HOUR][UnitsTime.MILLISECOND] = \
        Mul(Rat(Int(1), Mul(Int(36), Pow(10, 5))), Sym("h"))  # nopep8
    CONVERSIONS[UnitsTime.HOUR][UnitsTime.MINUTE] = \
        Mul(Rat(Int(1), Int(60)), Sym("h"))  # nopep8
    CONVERSIONS[UnitsTime.HOUR][UnitsTime.NANOSECOND] = \
        Mul(Rat(Int(1), Mul(Int(36), Pow(10, 11))), Sym("h"))  # nopep8
    CONVERSIONS[UnitsTime.HOUR][UnitsTime.PETASECOND] = \
        Mul(Rat(Mul(Int(25), Pow(10, 11)), Int(9)), Sym("h"))  # nopep8
    CONVERSIONS[UnitsTime.HOUR][UnitsTime.PICOSECOND] = \
        Mul(Rat(Int(1), Mul(Int(36), Pow(10, 14))), Sym("h"))  # nopep8
    CONVERSIONS[UnitsTime.HOUR][UnitsTime.SECOND] = \
        Mul(Rat(Int(1), Int(3600)), Sym("h"))  # nopep8
    CONVERSIONS[UnitsTime.HOUR][UnitsTime.TERASECOND] = \
        Mul(Rat(Mul(Int(25), Pow(10, 8)), Int(9)), Sym("h"))  # nopep8
    CONVERSIONS[UnitsTime.HOUR][UnitsTime.YOCTOSECOND] = \
        Mul(Rat(Int(1), Mul(Int(36), Pow(10, 26))), Sym("h"))  # nopep8
    CONVERSIONS[UnitsTime.HOUR][UnitsTime.YOTTASECOND] = \
        Mul(Rat(Mul(Int(25), Pow(10, 20)), Int(9)), Sym("h"))  # nopep8
    CONVERSIONS[UnitsTime.HOUR][UnitsTime.ZEPTOSECOND] = \
        Mul(Rat(Int(1), Mul(Int(36), Pow(10, 23))), Sym("h"))  # nopep8
    CONVERSIONS[UnitsTime.HOUR][UnitsTime.ZETTASECOND] = \
        Mul(Rat(Mul(Int(25), Pow(10, 17)), Int(9)), Sym("h"))  # nopep8
    CONVERSIONS[UnitsTime.KILOSECOND][UnitsTime.ATTOSECOND] = \
        Mul(Rat(Int(1), Pow(10, 21)), Sym("kilos"))  # nopep8
    CONVERSIONS[UnitsTime.KILOSECOND][UnitsTime.CENTISECOND] = \
        Mul(Rat(Int(1), Pow(10, 5)), Sym("kilos"))  # nopep8
    CONVERSIONS[UnitsTime.KILOSECOND][UnitsTime.DAY] = \
        Mul(Rat(Int(432), Int(5)), Sym("kilos"))  # nopep8
    CONVERSIONS[UnitsTime.KILOSECOND][UnitsTime.DECASECOND] = \
        Mul(Rat(Int(1), Int(100)), Sym("kilos"))  # nopep8
    CONVERSIONS[UnitsTime.KILOSECOND][UnitsTime.DECISECOND] = \
        Mul(Rat(Int(1), Pow(10, 4)), Sym("kilos"))  # nopep8
    CONVERSIONS[UnitsTime.KILOSECOND][UnitsTime.EXASECOND] = \
        Mul(Pow(10, 15), Sym("kilos"))  # nopep8
    CONVERSIONS[UnitsTime.KILOSECOND][UnitsTime.FEMTOSECOND] = \
        Mul(Rat(Int(1), Pow(10, 18)), Sym("kilos"))  # nopep8
    CONVERSIONS[UnitsTime.KILOSECOND][UnitsTime.GIGASECOND] = \
        Mul(Pow(10, 6), Sym("kilos"))  # nopep8
    CONVERSIONS[UnitsTime.KILOSECOND][UnitsTime.HECTOSECOND] = \
        Mul(Rat(Int(1), Int(10)), Sym("kilos"))  # nopep8
    CONVERSIONS[UnitsTime.KILOSECOND][UnitsTime.HOUR] = \
        Mul(Rat(Int(18), Int(5)), Sym("kilos"))  # nopep8
    CONVERSIONS[UnitsTime.KILOSECOND][UnitsTime.MEGASECOND] = \
        Mul(Int(1000), Sym("kilos"))  # nopep8
    CONVERSIONS[UnitsTime.KILOSECOND][UnitsTime.MICROSECOND] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("kilos"))  # nopep8
    CONVERSIONS[UnitsTime.KILOSECOND][UnitsTime.MILLISECOND] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("kilos"))  # nopep8
    CONVERSIONS[UnitsTime.KILOSECOND][UnitsTime.MINUTE] = \
        Mul(Rat(Int(3), Int(50)), Sym("kilos"))  # nopep8
    CONVERSIONS[UnitsTime.KILOSECOND][UnitsTime.NANOSECOND] = \
        Mul(Rat(Int(1), Pow(10, 12)), Sym("kilos"))  # nopep8
    CONVERSIONS[UnitsTime.KILOSECOND][UnitsTime.PETASECOND] = \
        Mul(Pow(10, 12), Sym("kilos"))  # nopep8
    CONVERSIONS[UnitsTime.KILOSECOND][UnitsTime.PICOSECOND] = \
        Mul(Rat(Int(1), Pow(10, 15)), Sym("kilos"))  # nopep8
    CONVERSIONS[UnitsTime.KILOSECOND][UnitsTime.SECOND] = \
        Mul(Rat(Int(1), Int(1000)), Sym("kilos"))  # nopep8
    CONVERSIONS[UnitsTime.KILOSECOND][UnitsTime.TERASECOND] = \
        Mul(Pow(10, 9), Sym("kilos"))  # nopep8
    CONVERSIONS[UnitsTime.KILOSECOND][UnitsTime.YOCTOSECOND] = \
        Mul(Rat(Int(1), Pow(10, 27)), Sym("kilos"))  # nopep8
    CONVERSIONS[UnitsTime.KILOSECOND][UnitsTime.YOTTASECOND] = \
        Mul(Pow(10, 21), Sym("kilos"))  # nopep8
    CONVERSIONS[UnitsTime.KILOSECOND][UnitsTime.ZEPTOSECOND] = \
        Mul(Rat(Int(1), Pow(10, 24)), Sym("kilos"))  # nopep8
    CONVERSIONS[UnitsTime.KILOSECOND][UnitsTime.ZETTASECOND] = \
        Mul(Pow(10, 18), Sym("kilos"))  # nopep8
    CONVERSIONS[UnitsTime.MEGASECOND][UnitsTime.ATTOSECOND] = \
        Mul(Rat(Int(1), Pow(10, 24)), Sym("megas"))  # nopep8
    CONVERSIONS[UnitsTime.MEGASECOND][UnitsTime.CENTISECOND] = \
        Mul(Rat(Int(1), Pow(10, 8)), Sym("megas"))  # nopep8
    CONVERSIONS[UnitsTime.MEGASECOND][UnitsTime.DAY] = \
        Mul(Rat(Int(54), Int(625)), Sym("megas"))  # nopep8
    CONVERSIONS[UnitsTime.MEGASECOND][UnitsTime.DECASECOND] = \
        Mul(Rat(Int(1), Pow(10, 5)), Sym("megas"))  # nopep8
    CONVERSIONS[UnitsTime.MEGASECOND][UnitsTime.DECISECOND] = \
        Mul(Rat(Int(1), Pow(10, 7)), Sym("megas"))  # nopep8
    CONVERSIONS[UnitsTime.MEGASECOND][UnitsTime.EXASECOND] = \
        Mul(Pow(10, 12), Sym("megas"))  # nopep8
    CONVERSIONS[UnitsTime.MEGASECOND][UnitsTime.FEMTOSECOND] = \
        Mul(Rat(Int(1), Pow(10, 21)), Sym("megas"))  # nopep8
    CONVERSIONS[UnitsTime.MEGASECOND][UnitsTime.GIGASECOND] = \
        Mul(Int(1000), Sym("megas"))  # nopep8
    CONVERSIONS[UnitsTime.MEGASECOND][UnitsTime.HECTOSECOND] = \
        Mul(Rat(Int(1), Pow(10, 4)), Sym("megas"))  # nopep8
    CONVERSIONS[UnitsTime.MEGASECOND][UnitsTime.HOUR] = \
        Mul(Rat(Int(9), Int(2500)), Sym("megas"))  # nopep8
    CONVERSIONS[UnitsTime.MEGASECOND][UnitsTime.KILOSECOND] = \
        Mul(Rat(Int(1), Int(1000)), Sym("megas"))  # nopep8
    CONVERSIONS[UnitsTime.MEGASECOND][UnitsTime.MICROSECOND] = \
        Mul(Rat(Int(1), Pow(10, 12)), Sym("megas"))  # nopep8
    CONVERSIONS[UnitsTime.MEGASECOND][UnitsTime.MILLISECOND] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("megas"))  # nopep8
    CONVERSIONS[UnitsTime.MEGASECOND][UnitsTime.MINUTE] = \
        Mul(Rat(Int(3), Mul(Int(5), Pow(10, 4))), Sym("megas"))  # nopep8
    CONVERSIONS[UnitsTime.MEGASECOND][UnitsTime.NANOSECOND] = \
        Mul(Rat(Int(1), Pow(10, 15)), Sym("megas"))  # nopep8
    CONVERSIONS[UnitsTime.MEGASECOND][UnitsTime.PETASECOND] = \
        Mul(Pow(10, 9), Sym("megas"))  # nopep8
    CONVERSIONS[UnitsTime.MEGASECOND][UnitsTime.PICOSECOND] = \
        Mul(Rat(Int(1), Pow(10, 18)), Sym("megas"))  # nopep8
    CONVERSIONS[UnitsTime.MEGASECOND][UnitsTime.SECOND] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("megas"))  # nopep8
    CONVERSIONS[UnitsTime.MEGASECOND][UnitsTime.TERASECOND] = \
        Mul(Pow(10, 6), Sym("megas"))  # nopep8
    CONVERSIONS[UnitsTime.MEGASECOND][UnitsTime.YOCTOSECOND] = \
        Mul(Rat(Int(1), Pow(10, 30)), Sym("megas"))  # nopep8
    CONVERSIONS[UnitsTime.MEGASECOND][UnitsTime.YOTTASECOND] = \
        Mul(Pow(10, 18), Sym("megas"))  # nopep8
    CONVERSIONS[UnitsTime.MEGASECOND][UnitsTime.ZEPTOSECOND] = \
        Mul(Rat(Int(1), Pow(10, 27)), Sym("megas"))  # nopep8
    CONVERSIONS[UnitsTime.MEGASECOND][UnitsTime.ZETTASECOND] = \
        Mul(Pow(10, 15), Sym("megas"))  # nopep8
    CONVERSIONS[UnitsTime.MICROSECOND][UnitsTime.ATTOSECOND] = \
        Mul(Rat(Int(1), Pow(10, 12)), Sym("micros"))  # nopep8
    CONVERSIONS[UnitsTime.MICROSECOND][UnitsTime.CENTISECOND] = \
        Mul(Pow(10, 4), Sym("micros"))  # nopep8
    CONVERSIONS[UnitsTime.MICROSECOND][UnitsTime.DAY] = \
        Mul(Mul(Int(864), Pow(10, 8)), Sym("micros"))  # nopep8
    CONVERSIONS[UnitsTime.MICROSECOND][UnitsTime.DECASECOND] = \
        Mul(Pow(10, 7), Sym("micros"))  # nopep8
    CONVERSIONS[UnitsTime.MICROSECOND][UnitsTime.DECISECOND] = \
        Mul(Pow(10, 5), Sym("micros"))  # nopep8
    CONVERSIONS[UnitsTime.MICROSECOND][UnitsTime.EXASECOND] = \
        Mul(Pow(10, 24), Sym("micros"))  # nopep8
    CONVERSIONS[UnitsTime.MICROSECOND][UnitsTime.FEMTOSECOND] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("micros"))  # nopep8
    CONVERSIONS[UnitsTime.MICROSECOND][UnitsTime.GIGASECOND] = \
        Mul(Pow(10, 15), Sym("micros"))  # nopep8
    CONVERSIONS[UnitsTime.MICROSECOND][UnitsTime.HECTOSECOND] = \
        Mul(Pow(10, 8), Sym("micros"))  # nopep8
    CONVERSIONS[UnitsTime.MICROSECOND][UnitsTime.HOUR] = \
        Mul(Mul(Int(36), Pow(10, 8)), Sym("micros"))  # nopep8
    CONVERSIONS[UnitsTime.MICROSECOND][UnitsTime.KILOSECOND] = \
        Mul(Pow(10, 9), Sym("micros"))  # nopep8
    CONVERSIONS[UnitsTime.MICROSECOND][UnitsTime.MEGASECOND] = \
        Mul(Pow(10, 12), Sym("micros"))  # nopep8
    CONVERSIONS[UnitsTime.MICROSECOND][UnitsTime.MILLISECOND] = \
        Mul(Int(1000), Sym("micros"))  # nopep8
    CONVERSIONS[UnitsTime.MICROSECOND][UnitsTime.MINUTE] = \
        Mul(Mul(Int(6), Pow(10, 7)), Sym("micros"))  # nopep8
    CONVERSIONS[UnitsTime.MICROSECOND][UnitsTime.NANOSECOND] = \
        Mul(Rat(Int(1), Int(1000)), Sym("micros"))  # nopep8
    CONVERSIONS[UnitsTime.MICROSECOND][UnitsTime.PETASECOND] = \
        Mul(Pow(10, 21), Sym("micros"))  # nopep8
    CONVERSIONS[UnitsTime.MICROSECOND][UnitsTime.PICOSECOND] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("micros"))  # nopep8
    CONVERSIONS[UnitsTime.MICROSECOND][UnitsTime.SECOND] = \
        Mul(Pow(10, 6), Sym("micros"))  # nopep8
    CONVERSIONS[UnitsTime.MICROSECOND][UnitsTime.TERASECOND] = \
        Mul(Pow(10, 18), Sym("micros"))  # nopep8
    CONVERSIONS[UnitsTime.MICROSECOND][UnitsTime.YOCTOSECOND] = \
        Mul(Rat(Int(1), Pow(10, 18)), Sym("micros"))  # nopep8
    CONVERSIONS[UnitsTime.MICROSECOND][UnitsTime.YOTTASECOND] = \
        Mul(Pow(10, 30), Sym("micros"))  # nopep8
    CONVERSIONS[UnitsTime.MICROSECOND][UnitsTime.ZEPTOSECOND] = \
        Mul(Rat(Int(1), Pow(10, 15)), Sym("micros"))  # nopep8
    CONVERSIONS[UnitsTime.MICROSECOND][UnitsTime.ZETTASECOND] = \
        Mul(Pow(10, 27), Sym("micros"))  # nopep8
    CONVERSIONS[UnitsTime.MILLISECOND][UnitsTime.ATTOSECOND] = \
        Mul(Rat(Int(1), Pow(10, 15)), Sym("millis"))  # nopep8
    CONVERSIONS[UnitsTime.MILLISECOND][UnitsTime.CENTISECOND] = \
        Mul(Int(10), Sym("millis"))  # nopep8
    CONVERSIONS[UnitsTime.MILLISECOND][UnitsTime.DAY] = \
        Mul(Mul(Int(864), Pow(10, 5)), Sym("millis"))  # nopep8
    CONVERSIONS[UnitsTime.MILLISECOND][UnitsTime.DECASECOND] = \
        Mul(Pow(10, 4), Sym("millis"))  # nopep8
    CONVERSIONS[UnitsTime.MILLISECOND][UnitsTime.DECISECOND] = \
        Mul(Int(100), Sym("millis"))  # nopep8
    CONVERSIONS[UnitsTime.MILLISECOND][UnitsTime.EXASECOND] = \
        Mul(Pow(10, 21), Sym("millis"))  # nopep8
    CONVERSIONS[UnitsTime.MILLISECOND][UnitsTime.FEMTOSECOND] = \
        Mul(Rat(Int(1), Pow(10, 12)), Sym("millis"))  # nopep8
    CONVERSIONS[UnitsTime.MILLISECOND][UnitsTime.GIGASECOND] = \
        Mul(Pow(10, 12), Sym("millis"))  # nopep8
    CONVERSIONS[UnitsTime.MILLISECOND][UnitsTime.HECTOSECOND] = \
        Mul(Pow(10, 5), Sym("millis"))  # nopep8
    CONVERSIONS[UnitsTime.MILLISECOND][UnitsTime.HOUR] = \
        Mul(Mul(Int(36), Pow(10, 5)), Sym("millis"))  # nopep8
    CONVERSIONS[UnitsTime.MILLISECOND][UnitsTime.KILOSECOND] = \
        Mul(Pow(10, 6), Sym("millis"))  # nopep8
    CONVERSIONS[UnitsTime.MILLISECOND][UnitsTime.MEGASECOND] = \
        Mul(Pow(10, 9), Sym("millis"))  # nopep8
    CONVERSIONS[UnitsTime.MILLISECOND][UnitsTime.MICROSECOND] = \
        Mul(Rat(Int(1), Int(1000)), Sym("millis"))  # nopep8
    CONVERSIONS[UnitsTime.MILLISECOND][UnitsTime.MINUTE] = \
        Mul(Mul(Int(6), Pow(10, 4)), Sym("millis"))  # nopep8
    CONVERSIONS[UnitsTime.MILLISECOND][UnitsTime.NANOSECOND] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("millis"))  # nopep8
    CONVERSIONS[UnitsTime.MILLISECOND][UnitsTime.PETASECOND] = \
        Mul(Pow(10, 18), Sym("millis"))  # nopep8
    CONVERSIONS[UnitsTime.MILLISECOND][UnitsTime.PICOSECOND] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("millis"))  # nopep8
    CONVERSIONS[UnitsTime.MILLISECOND][UnitsTime.SECOND] = \
        Mul(Int(1000), Sym("millis"))  # nopep8
    CONVERSIONS[UnitsTime.MILLISECOND][UnitsTime.TERASECOND] = \
        Mul(Pow(10, 15), Sym("millis"))  # nopep8
    CONVERSIONS[UnitsTime.MILLISECOND][UnitsTime.YOCTOSECOND] = \
        Mul(Rat(Int(1), Pow(10, 21)), Sym("millis"))  # nopep8
    CONVERSIONS[UnitsTime.MILLISECOND][UnitsTime.YOTTASECOND] = \
        Mul(Pow(10, 27), Sym("millis"))  # nopep8
    CONVERSIONS[UnitsTime.MILLISECOND][UnitsTime.ZEPTOSECOND] = \
        Mul(Rat(Int(1), Pow(10, 18)), Sym("millis"))  # nopep8
    CONVERSIONS[UnitsTime.MILLISECOND][UnitsTime.ZETTASECOND] = \
        Mul(Pow(10, 24), Sym("millis"))  # nopep8
    CONVERSIONS[UnitsTime.MINUTE][UnitsTime.ATTOSECOND] = \
        Mul(Rat(Int(1), Mul(Int(6), Pow(10, 19))), Sym("m"))  # nopep8
    CONVERSIONS[UnitsTime.MINUTE][UnitsTime.CENTISECOND] = \
        Mul(Rat(Int(1), Int(6000)), Sym("m"))  # nopep8
    CONVERSIONS[UnitsTime.MINUTE][UnitsTime.DAY] = \
        Mul(Int(1440), Sym("m"))  # nopep8
    CONVERSIONS[UnitsTime.MINUTE][UnitsTime.DECASECOND] = \
        Mul(Rat(Int(1), Int(6)), Sym("m"))  # nopep8
    CONVERSIONS[UnitsTime.MINUTE][UnitsTime.DECISECOND] = \
        Mul(Rat(Int(1), Int(600)), Sym("m"))  # nopep8
    CONVERSIONS[UnitsTime.MINUTE][UnitsTime.EXASECOND] = \
        Mul(Rat(Mul(Int(5), Pow(10, 16)), Int(3)), Sym("m"))  # nopep8
    CONVERSIONS[UnitsTime.MINUTE][UnitsTime.FEMTOSECOND] = \
        Mul(Rat(Int(1), Mul(Int(6), Pow(10, 16))), Sym("m"))  # nopep8
    CONVERSIONS[UnitsTime.MINUTE][UnitsTime.GIGASECOND] = \
        Mul(Rat(Mul(Int(5), Pow(10, 7)), Int(3)), Sym("m"))  # nopep8
    CONVERSIONS[UnitsTime.MINUTE][UnitsTime.HECTOSECOND] = \
        Mul(Rat(Int(5), Int(3)), Sym("m"))  # nopep8
    CONVERSIONS[UnitsTime.MINUTE][UnitsTime.HOUR] = \
        Mul(Int(60), Sym("m"))  # nopep8
    CONVERSIONS[UnitsTime.MINUTE][UnitsTime.KILOSECOND] = \
        Mul(Rat(Int(50), Int(3)), Sym("m"))  # nopep8
    CONVERSIONS[UnitsTime.MINUTE][UnitsTime.MEGASECOND] = \
        Mul(Rat(Mul(Int(5), Pow(10, 4)), Int(3)), Sym("m"))  # nopep8
    CONVERSIONS[UnitsTime.MINUTE][UnitsTime.MICROSECOND] = \
        Mul(Rat(Int(1), Mul(Int(6), Pow(10, 7))), Sym("m"))  # nopep8
    CONVERSIONS[UnitsTime.MINUTE][UnitsTime.MILLISECOND] = \
        Mul(Rat(Int(1), Mul(Int(6), Pow(10, 4))), Sym("m"))  # nopep8
    CONVERSIONS[UnitsTime.MINUTE][UnitsTime.NANOSECOND] = \
        Mul(Rat(Int(1), Mul(Int(6), Pow(10, 10))), Sym("m"))  # nopep8
    CONVERSIONS[UnitsTime.MINUTE][UnitsTime.PETASECOND] = \
        Mul(Rat(Mul(Int(5), Pow(10, 13)), Int(3)), Sym("m"))  # nopep8
    CONVERSIONS[UnitsTime.MINUTE][UnitsTime.PICOSECOND] = \
        Mul(Rat(Int(1), Mul(Int(6), Pow(10, 13))), Sym("m"))  # nopep8
    CONVERSIONS[UnitsTime.MINUTE][UnitsTime.SECOND] = \
        Mul(Rat(Int(1), Int(60)), Sym("m"))  # nopep8
    CONVERSIONS[UnitsTime.MINUTE][UnitsTime.TERASECOND] = \
        Mul(Rat(Mul(Int(5), Pow(10, 10)), Int(3)), Sym("m"))  # nopep8
    CONVERSIONS[UnitsTime.MINUTE][UnitsTime.YOCTOSECOND] = \
        Mul(Rat(Int(1), Mul(Int(6), Pow(10, 25))), Sym("m"))  # nopep8
    CONVERSIONS[UnitsTime.MINUTE][UnitsTime.YOTTASECOND] = \
        Mul(Rat(Mul(Int(5), Pow(10, 22)), Int(3)), Sym("m"))  # nopep8
    CONVERSIONS[UnitsTime.MINUTE][UnitsTime.ZEPTOSECOND] = \
        Mul(Rat(Int(1), Mul(Int(6), Pow(10, 22))), Sym("m"))  # nopep8
    CONVERSIONS[UnitsTime.MINUTE][UnitsTime.ZETTASECOND] = \
        Mul(Rat(Mul(Int(5), Pow(10, 19)), Int(3)), Sym("m"))  # nopep8
    CONVERSIONS[UnitsTime.NANOSECOND][UnitsTime.ATTOSECOND] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("nanos"))  # nopep8
    CONVERSIONS[UnitsTime.NANOSECOND][UnitsTime.CENTISECOND] = \
        Mul(Pow(10, 7), Sym("nanos"))  # nopep8
    CONVERSIONS[UnitsTime.NANOSECOND][UnitsTime.DAY] = \
        Mul(Mul(Int(864), Pow(10, 11)), Sym("nanos"))  # nopep8
    CONVERSIONS[UnitsTime.NANOSECOND][UnitsTime.DECASECOND] = \
        Mul(Pow(10, 10), Sym("nanos"))  # nopep8
    CONVERSIONS[UnitsTime.NANOSECOND][UnitsTime.DECISECOND] = \
        Mul(Pow(10, 8), Sym("nanos"))  # nopep8
    CONVERSIONS[UnitsTime.NANOSECOND][UnitsTime.EXASECOND] = \
        Mul(Pow(10, 27), Sym("nanos"))  # nopep8
    CONVERSIONS[UnitsTime.NANOSECOND][UnitsTime.FEMTOSECOND] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("nanos"))  # nopep8
    CONVERSIONS[UnitsTime.NANOSECOND][UnitsTime.GIGASECOND] = \
        Mul(Pow(10, 18), Sym("nanos"))  # nopep8
    CONVERSIONS[UnitsTime.NANOSECOND][UnitsTime.HECTOSECOND] = \
        Mul(Pow(10, 11), Sym("nanos"))  # nopep8
    CONVERSIONS[UnitsTime.NANOSECOND][UnitsTime.HOUR] = \
        Mul(Mul(Int(36), Pow(10, 11)), Sym("nanos"))  # nopep8
    CONVERSIONS[UnitsTime.NANOSECOND][UnitsTime.KILOSECOND] = \
        Mul(Pow(10, 12), Sym("nanos"))  # nopep8
    CONVERSIONS[UnitsTime.NANOSECOND][UnitsTime.MEGASECOND] = \
        Mul(Pow(10, 15), Sym("nanos"))  # nopep8
    CONVERSIONS[UnitsTime.NANOSECOND][UnitsTime.MICROSECOND] = \
        Mul(Int(1000), Sym("nanos"))  # nopep8
    CONVERSIONS[UnitsTime.NANOSECOND][UnitsTime.MILLISECOND] = \
        Mul(Pow(10, 6), Sym("nanos"))  # nopep8
    CONVERSIONS[UnitsTime.NANOSECOND][UnitsTime.MINUTE] = \
        Mul(Mul(Int(6), Pow(10, 10)), Sym("nanos"))  # nopep8
    CONVERSIONS[UnitsTime.NANOSECOND][UnitsTime.PETASECOND] = \
        Mul(Pow(10, 24), Sym("nanos"))  # nopep8
    CONVERSIONS[UnitsTime.NANOSECOND][UnitsTime.PICOSECOND] = \
        Mul(Rat(Int(1), Int(1000)), Sym("nanos"))  # nopep8
    CONVERSIONS[UnitsTime.NANOSECOND][UnitsTime.SECOND] = \
        Mul(Pow(10, 9), Sym("nanos"))  # nopep8
    CONVERSIONS[UnitsTime.NANOSECOND][UnitsTime.TERASECOND] = \
        Mul(Pow(10, 21), Sym("nanos"))  # nopep8
    CONVERSIONS[UnitsTime.NANOSECOND][UnitsTime.YOCTOSECOND] = \
        Mul(Rat(Int(1), Pow(10, 15)), Sym("nanos"))  # nopep8
    CONVERSIONS[UnitsTime.NANOSECOND][UnitsTime.YOTTASECOND] = \
        Mul(Pow(10, 33), Sym("nanos"))  # nopep8
    CONVERSIONS[UnitsTime.NANOSECOND][UnitsTime.ZEPTOSECOND] = \
        Mul(Rat(Int(1), Pow(10, 12)), Sym("nanos"))  # nopep8
    CONVERSIONS[UnitsTime.NANOSECOND][UnitsTime.ZETTASECOND] = \
        Mul(Pow(10, 30), Sym("nanos"))  # nopep8
    CONVERSIONS[UnitsTime.PETASECOND][UnitsTime.ATTOSECOND] = \
        Mul(Rat(Int(1), Pow(10, 33)), Sym("petas"))  # nopep8
    CONVERSIONS[UnitsTime.PETASECOND][UnitsTime.CENTISECOND] = \
        Mul(Rat(Int(1), Pow(10, 17)), Sym("petas"))  # nopep8
    CONVERSIONS[UnitsTime.PETASECOND][UnitsTime.DAY] = \
        Mul(Rat(Int(27), Mul(Int(3125), Pow(10, 8))), Sym("petas"))  # nopep8
    CONVERSIONS[UnitsTime.PETASECOND][UnitsTime.DECASECOND] = \
        Mul(Rat(Int(1), Pow(10, 14)), Sym("petas"))  # nopep8
    CONVERSIONS[UnitsTime.PETASECOND][UnitsTime.DECISECOND] = \
        Mul(Rat(Int(1), Pow(10, 16)), Sym("petas"))  # nopep8
    CONVERSIONS[UnitsTime.PETASECOND][UnitsTime.EXASECOND] = \
        Mul(Int(1000), Sym("petas"))  # nopep8
    CONVERSIONS[UnitsTime.PETASECOND][UnitsTime.FEMTOSECOND] = \
        Mul(Rat(Int(1), Pow(10, 30)), Sym("petas"))  # nopep8
    CONVERSIONS[UnitsTime.PETASECOND][UnitsTime.GIGASECOND] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("petas"))  # nopep8
    CONVERSIONS[UnitsTime.PETASECOND][UnitsTime.HECTOSECOND] = \
        Mul(Rat(Int(1), Pow(10, 13)), Sym("petas"))  # nopep8
    CONVERSIONS[UnitsTime.PETASECOND][UnitsTime.HOUR] = \
        Mul(Rat(Int(9), Mul(Int(25), Pow(10, 11))), Sym("petas"))  # nopep8
    CONVERSIONS[UnitsTime.PETASECOND][UnitsTime.KILOSECOND] = \
        Mul(Rat(Int(1), Pow(10, 12)), Sym("petas"))  # nopep8
    CONVERSIONS[UnitsTime.PETASECOND][UnitsTime.MEGASECOND] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("petas"))  # nopep8
    CONVERSIONS[UnitsTime.PETASECOND][UnitsTime.MICROSECOND] = \
        Mul(Rat(Int(1), Pow(10, 21)), Sym("petas"))  # nopep8
    CONVERSIONS[UnitsTime.PETASECOND][UnitsTime.MILLISECOND] = \
        Mul(Rat(Int(1), Pow(10, 18)), Sym("petas"))  # nopep8
    CONVERSIONS[UnitsTime.PETASECOND][UnitsTime.MINUTE] = \
        Mul(Rat(Int(3), Mul(Int(5), Pow(10, 13))), Sym("petas"))  # nopep8
    CONVERSIONS[UnitsTime.PETASECOND][UnitsTime.NANOSECOND] = \
        Mul(Rat(Int(1), Pow(10, 24)), Sym("petas"))  # nopep8
    CONVERSIONS[UnitsTime.PETASECOND][UnitsTime.PICOSECOND] = \
        Mul(Rat(Int(1), Pow(10, 27)), Sym("petas"))  # nopep8
    CONVERSIONS[UnitsTime.PETASECOND][UnitsTime.SECOND] = \
        Mul(Rat(Int(1), Pow(10, 15)), Sym("petas"))  # nopep8
    CONVERSIONS[UnitsTime.PETASECOND][UnitsTime.TERASECOND] = \
        Mul(Rat(Int(1), Int(1000)), Sym("petas"))  # nopep8
    CONVERSIONS[UnitsTime.PETASECOND][UnitsTime.YOCTOSECOND] = \
        Mul(Rat(Int(1), Pow(10, 39)), Sym("petas"))  # nopep8
    CONVERSIONS[UnitsTime.PETASECOND][UnitsTime.YOTTASECOND] = \
        Mul(Pow(10, 9), Sym("petas"))  # nopep8
    CONVERSIONS[UnitsTime.PETASECOND][UnitsTime.ZEPTOSECOND] = \
        Mul(Rat(Int(1), Pow(10, 36)), Sym("petas"))  # nopep8
    CONVERSIONS[UnitsTime.PETASECOND][UnitsTime.ZETTASECOND] = \
        Mul(Pow(10, 6), Sym("petas"))  # nopep8
    CONVERSIONS[UnitsTime.PICOSECOND][UnitsTime.ATTOSECOND] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("picos"))  # nopep8
    CONVERSIONS[UnitsTime.PICOSECOND][UnitsTime.CENTISECOND] = \
        Mul(Pow(10, 10), Sym("picos"))  # nopep8
    CONVERSIONS[UnitsTime.PICOSECOND][UnitsTime.DAY] = \
        Mul(Mul(Int(864), Pow(10, 14)), Sym("picos"))  # nopep8
    CONVERSIONS[UnitsTime.PICOSECOND][UnitsTime.DECASECOND] = \
        Mul(Pow(10, 13), Sym("picos"))  # nopep8
    CONVERSIONS[UnitsTime.PICOSECOND][UnitsTime.DECISECOND] = \
        Mul(Pow(10, 11), Sym("picos"))  # nopep8
    CONVERSIONS[UnitsTime.PICOSECOND][UnitsTime.EXASECOND] = \
        Mul(Pow(10, 30), Sym("picos"))  # nopep8
    CONVERSIONS[UnitsTime.PICOSECOND][UnitsTime.FEMTOSECOND] = \
        Mul(Rat(Int(1), Int(1000)), Sym("picos"))  # nopep8
    CONVERSIONS[UnitsTime.PICOSECOND][UnitsTime.GIGASECOND] = \
        Mul(Pow(10, 21), Sym("picos"))  # nopep8
    CONVERSIONS[UnitsTime.PICOSECOND][UnitsTime.HECTOSECOND] = \
        Mul(Pow(10, 14), Sym("picos"))  # nopep8
    CONVERSIONS[UnitsTime.PICOSECOND][UnitsTime.HOUR] = \
        Mul(Mul(Int(36), Pow(10, 14)), Sym("picos"))  # nopep8
    CONVERSIONS[UnitsTime.PICOSECOND][UnitsTime.KILOSECOND] = \
        Mul(Pow(10, 15), Sym("picos"))  # nopep8
    CONVERSIONS[UnitsTime.PICOSECOND][UnitsTime.MEGASECOND] = \
        Mul(Pow(10, 18), Sym("picos"))  # nopep8
    CONVERSIONS[UnitsTime.PICOSECOND][UnitsTime.MICROSECOND] = \
        Mul(Pow(10, 6), Sym("picos"))  # nopep8
    CONVERSIONS[UnitsTime.PICOSECOND][UnitsTime.MILLISECOND] = \
        Mul(Pow(10, 9), Sym("picos"))  # nopep8
    CONVERSIONS[UnitsTime.PICOSECOND][UnitsTime.MINUTE] = \
        Mul(Mul(Int(6), Pow(10, 13)), Sym("picos"))  # nopep8
    CONVERSIONS[UnitsTime.PICOSECOND][UnitsTime.NANOSECOND] = \
        Mul(Int(1000), Sym("picos"))  # nopep8
    CONVERSIONS[UnitsTime.PICOSECOND][UnitsTime.PETASECOND] = \
        Mul(Pow(10, 27), Sym("picos"))  # nopep8
    CONVERSIONS[UnitsTime.PICOSECOND][UnitsTime.SECOND] = \
        Mul(Pow(10, 12), Sym("picos"))  # nopep8
    CONVERSIONS[UnitsTime.PICOSECOND][UnitsTime.TERASECOND] = \
        Mul(Pow(10, 24), Sym("picos"))  # nopep8
    CONVERSIONS[UnitsTime.PICOSECOND][UnitsTime.YOCTOSECOND] = \
        Mul(Rat(Int(1), Pow(10, 12)), Sym("picos"))  # nopep8
    CONVERSIONS[UnitsTime.PICOSECOND][UnitsTime.YOTTASECOND] = \
        Mul(Pow(10, 36), Sym("picos"))  # nopep8
    CONVERSIONS[UnitsTime.PICOSECOND][UnitsTime.ZEPTOSECOND] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("picos"))  # nopep8
    CONVERSIONS[UnitsTime.PICOSECOND][UnitsTime.ZETTASECOND] = \
        Mul(Pow(10, 33), Sym("picos"))  # nopep8
    CONVERSIONS[UnitsTime.SECOND][UnitsTime.ATTOSECOND] = \
        Mul(Rat(Int(1), Pow(10, 18)), Sym("s"))  # nopep8
    CONVERSIONS[UnitsTime.SECOND][UnitsTime.CENTISECOND] = \
        Mul(Rat(Int(1), Int(100)), Sym("s"))  # nopep8
    CONVERSIONS[UnitsTime.SECOND][UnitsTime.DAY] = \
        Mul(Int(86400), Sym("s"))  # nopep8
    CONVERSIONS[UnitsTime.SECOND][UnitsTime.DECASECOND] = \
        Mul(Int(10), Sym("s"))  # nopep8
    CONVERSIONS[UnitsTime.SECOND][UnitsTime.DECISECOND] = \
        Mul(Rat(Int(1), Int(10)), Sym("s"))  # nopep8
    CONVERSIONS[UnitsTime.SECOND][UnitsTime.EXASECOND] = \
        Mul(Pow(10, 18), Sym("s"))  # nopep8
    CONVERSIONS[UnitsTime.SECOND][UnitsTime.FEMTOSECOND] = \
        Mul(Rat(Int(1), Pow(10, 15)), Sym("s"))  # nopep8
    CONVERSIONS[UnitsTime.SECOND][UnitsTime.GIGASECOND] = \
        Mul(Pow(10, 9), Sym("s"))  # nopep8
    CONVERSIONS[UnitsTime.SECOND][UnitsTime.HECTOSECOND] = \
        Mul(Int(100), Sym("s"))  # nopep8
    CONVERSIONS[UnitsTime.SECOND][UnitsTime.HOUR] = \
        Mul(Int(3600), Sym("s"))  # nopep8
    CONVERSIONS[UnitsTime.SECOND][UnitsTime.KILOSECOND] = \
        Mul(Int(1000), Sym("s"))  # nopep8
    CONVERSIONS[UnitsTime.SECOND][UnitsTime.MEGASECOND] = \
        Mul(Pow(10, 6), Sym("s"))  # nopep8
    CONVERSIONS[UnitsTime.SECOND][UnitsTime.MICROSECOND] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("s"))  # nopep8
    CONVERSIONS[UnitsTime.SECOND][UnitsTime.MILLISECOND] = \
        Mul(Rat(Int(1), Int(1000)), Sym("s"))  # nopep8
    CONVERSIONS[UnitsTime.SECOND][UnitsTime.MINUTE] = \
        Mul(Int(60), Sym("s"))  # nopep8
    CONVERSIONS[UnitsTime.SECOND][UnitsTime.NANOSECOND] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("s"))  # nopep8
    CONVERSIONS[UnitsTime.SECOND][UnitsTime.PETASECOND] = \
        Mul(Pow(10, 15), Sym("s"))  # nopep8
    CONVERSIONS[UnitsTime.SECOND][UnitsTime.PICOSECOND] = \
        Mul(Rat(Int(1), Pow(10, 12)), Sym("s"))  # nopep8
    CONVERSIONS[UnitsTime.SECOND][UnitsTime.TERASECOND] = \
        Mul(Pow(10, 12), Sym("s"))  # nopep8
    CONVERSIONS[UnitsTime.SECOND][UnitsTime.YOCTOSECOND] = \
        Mul(Rat(Int(1), Pow(10, 24)), Sym("s"))  # nopep8
    CONVERSIONS[UnitsTime.SECOND][UnitsTime.YOTTASECOND] = \
        Mul(Pow(10, 24), Sym("s"))  # nopep8
    CONVERSIONS[UnitsTime.SECOND][UnitsTime.ZEPTOSECOND] = \
        Mul(Rat(Int(1), Pow(10, 21)), Sym("s"))  # nopep8
    CONVERSIONS[UnitsTime.SECOND][UnitsTime.ZETTASECOND] = \
        Mul(Pow(10, 21), Sym("s"))  # nopep8
    CONVERSIONS[UnitsTime.TERASECOND][UnitsTime.ATTOSECOND] = \
        Mul(Rat(Int(1), Pow(10, 30)), Sym("teras"))  # nopep8
    CONVERSIONS[UnitsTime.TERASECOND][UnitsTime.CENTISECOND] = \
        Mul(Rat(Int(1), Pow(10, 14)), Sym("teras"))  # nopep8
    CONVERSIONS[UnitsTime.TERASECOND][UnitsTime.DAY] = \
        Mul(Rat(Int(27), Mul(Int(3125), Pow(10, 5))), Sym("teras"))  # nopep8
    CONVERSIONS[UnitsTime.TERASECOND][UnitsTime.DECASECOND] = \
        Mul(Rat(Int(1), Pow(10, 11)), Sym("teras"))  # nopep8
    CONVERSIONS[UnitsTime.TERASECOND][UnitsTime.DECISECOND] = \
        Mul(Rat(Int(1), Pow(10, 13)), Sym("teras"))  # nopep8
    CONVERSIONS[UnitsTime.TERASECOND][UnitsTime.EXASECOND] = \
        Mul(Pow(10, 6), Sym("teras"))  # nopep8
    CONVERSIONS[UnitsTime.TERASECOND][UnitsTime.FEMTOSECOND] = \
        Mul(Rat(Int(1), Pow(10, 27)), Sym("teras"))  # nopep8
    CONVERSIONS[UnitsTime.TERASECOND][UnitsTime.GIGASECOND] = \
        Mul(Rat(Int(1), Int(1000)), Sym("teras"))  # nopep8
    CONVERSIONS[UnitsTime.TERASECOND][UnitsTime.HECTOSECOND] = \
        Mul(Rat(Int(1), Pow(10, 10)), Sym("teras"))  # nopep8
    CONVERSIONS[UnitsTime.TERASECOND][UnitsTime.HOUR] = \
        Mul(Rat(Int(9), Mul(Int(25), Pow(10, 8))), Sym("teras"))  # nopep8
    CONVERSIONS[UnitsTime.TERASECOND][UnitsTime.KILOSECOND] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("teras"))  # nopep8
    CONVERSIONS[UnitsTime.TERASECOND][UnitsTime.MEGASECOND] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("teras"))  # nopep8
    CONVERSIONS[UnitsTime.TERASECOND][UnitsTime.MICROSECOND] = \
        Mul(Rat(Int(1), Pow(10, 18)), Sym("teras"))  # nopep8
    CONVERSIONS[UnitsTime.TERASECOND][UnitsTime.MILLISECOND] = \
        Mul(Rat(Int(1), Pow(10, 15)), Sym("teras"))  # nopep8
    CONVERSIONS[UnitsTime.TERASECOND][UnitsTime.MINUTE] = \
        Mul(Rat(Int(3), Mul(Int(5), Pow(10, 10))), Sym("teras"))  # nopep8
    CONVERSIONS[UnitsTime.TERASECOND][UnitsTime.NANOSECOND] = \
        Mul(Rat(Int(1), Pow(10, 21)), Sym("teras"))  # nopep8
    CONVERSIONS[UnitsTime.TERASECOND][UnitsTime.PETASECOND] = \
        Mul(Int(1000), Sym("teras"))  # nopep8
    CONVERSIONS[UnitsTime.TERASECOND][UnitsTime.PICOSECOND] = \
        Mul(Rat(Int(1), Pow(10, 24)), Sym("teras"))  # nopep8
    CONVERSIONS[UnitsTime.TERASECOND][UnitsTime.SECOND] = \
        Mul(Rat(Int(1), Pow(10, 12)), Sym("teras"))  # nopep8
    CONVERSIONS[UnitsTime.TERASECOND][UnitsTime.YOCTOSECOND] = \
        Mul(Rat(Int(1), Pow(10, 36)), Sym("teras"))  # nopep8
    CONVERSIONS[UnitsTime.TERASECOND][UnitsTime.YOTTASECOND] = \
        Mul(Pow(10, 12), Sym("teras"))  # nopep8
    CONVERSIONS[UnitsTime.TERASECOND][UnitsTime.ZEPTOSECOND] = \
        Mul(Rat(Int(1), Pow(10, 33)), Sym("teras"))  # nopep8
    CONVERSIONS[UnitsTime.TERASECOND][UnitsTime.ZETTASECOND] = \
        Mul(Pow(10, 9), Sym("teras"))  # nopep8
    CONVERSIONS[UnitsTime.YOCTOSECOND][UnitsTime.ATTOSECOND] = \
        Mul(Pow(10, 6), Sym("yoctos"))  # nopep8
    CONVERSIONS[UnitsTime.YOCTOSECOND][UnitsTime.CENTISECOND] = \
        Mul(Pow(10, 22), Sym("yoctos"))  # nopep8
    CONVERSIONS[UnitsTime.YOCTOSECOND][UnitsTime.DAY] = \
        Mul(Mul(Int(864), Pow(10, 26)), Sym("yoctos"))  # nopep8
    CONVERSIONS[UnitsTime.YOCTOSECOND][UnitsTime.DECASECOND] = \
        Mul(Pow(10, 25), Sym("yoctos"))  # nopep8
    CONVERSIONS[UnitsTime.YOCTOSECOND][UnitsTime.DECISECOND] = \
        Mul(Pow(10, 23), Sym("yoctos"))  # nopep8
    CONVERSIONS[UnitsTime.YOCTOSECOND][UnitsTime.EXASECOND] = \
        Mul(Pow(10, 42), Sym("yoctos"))  # nopep8
    CONVERSIONS[UnitsTime.YOCTOSECOND][UnitsTime.FEMTOSECOND] = \
        Mul(Pow(10, 9), Sym("yoctos"))  # nopep8
    CONVERSIONS[UnitsTime.YOCTOSECOND][UnitsTime.GIGASECOND] = \
        Mul(Pow(10, 33), Sym("yoctos"))  # nopep8
    CONVERSIONS[UnitsTime.YOCTOSECOND][UnitsTime.HECTOSECOND] = \
        Mul(Pow(10, 26), Sym("yoctos"))  # nopep8
    CONVERSIONS[UnitsTime.YOCTOSECOND][UnitsTime.HOUR] = \
        Mul(Mul(Int(36), Pow(10, 26)), Sym("yoctos"))  # nopep8
    CONVERSIONS[UnitsTime.YOCTOSECOND][UnitsTime.KILOSECOND] = \
        Mul(Pow(10, 27), Sym("yoctos"))  # nopep8
    CONVERSIONS[UnitsTime.YOCTOSECOND][UnitsTime.MEGASECOND] = \
        Mul(Pow(10, 30), Sym("yoctos"))  # nopep8
    CONVERSIONS[UnitsTime.YOCTOSECOND][UnitsTime.MICROSECOND] = \
        Mul(Pow(10, 18), Sym("yoctos"))  # nopep8
    CONVERSIONS[UnitsTime.YOCTOSECOND][UnitsTime.MILLISECOND] = \
        Mul(Pow(10, 21), Sym("yoctos"))  # nopep8
    CONVERSIONS[UnitsTime.YOCTOSECOND][UnitsTime.MINUTE] = \
        Mul(Mul(Int(6), Pow(10, 25)), Sym("yoctos"))  # nopep8
    CONVERSIONS[UnitsTime.YOCTOSECOND][UnitsTime.NANOSECOND] = \
        Mul(Pow(10, 15), Sym("yoctos"))  # nopep8
    CONVERSIONS[UnitsTime.YOCTOSECOND][UnitsTime.PETASECOND] = \
        Mul(Pow(10, 39), Sym("yoctos"))  # nopep8
    CONVERSIONS[UnitsTime.YOCTOSECOND][UnitsTime.PICOSECOND] = \
        Mul(Pow(10, 12), Sym("yoctos"))  # nopep8
    CONVERSIONS[UnitsTime.YOCTOSECOND][UnitsTime.SECOND] = \
        Mul(Pow(10, 24), Sym("yoctos"))  # nopep8
    CONVERSIONS[UnitsTime.YOCTOSECOND][UnitsTime.TERASECOND] = \
        Mul(Pow(10, 36), Sym("yoctos"))  # nopep8
    CONVERSIONS[UnitsTime.YOCTOSECOND][UnitsTime.YOTTASECOND] = \
        Mul(Pow(10, 48), Sym("yoctos"))  # nopep8
    CONVERSIONS[UnitsTime.YOCTOSECOND][UnitsTime.ZEPTOSECOND] = \
        Mul(Int(1000), Sym("yoctos"))  # nopep8
    CONVERSIONS[UnitsTime.YOCTOSECOND][UnitsTime.ZETTASECOND] = \
        Mul(Pow(10, 45), Sym("yoctos"))  # nopep8
    CONVERSIONS[UnitsTime.YOTTASECOND][UnitsTime.ATTOSECOND] = \
        Mul(Rat(Int(1), Pow(10, 42)), Sym("yottas"))  # nopep8
    CONVERSIONS[UnitsTime.YOTTASECOND][UnitsTime.CENTISECOND] = \
        Mul(Rat(Int(1), Pow(10, 26)), Sym("yottas"))  # nopep8
    CONVERSIONS[UnitsTime.YOTTASECOND][UnitsTime.DAY] = \
        Mul(Rat(Int(27), Mul(Int(3125), Pow(10, 17))), Sym("yottas"))  # nopep8
    CONVERSIONS[UnitsTime.YOTTASECOND][UnitsTime.DECASECOND] = \
        Mul(Rat(Int(1), Pow(10, 23)), Sym("yottas"))  # nopep8
    CONVERSIONS[UnitsTime.YOTTASECOND][UnitsTime.DECISECOND] = \
        Mul(Rat(Int(1), Pow(10, 25)), Sym("yottas"))  # nopep8
    CONVERSIONS[UnitsTime.YOTTASECOND][UnitsTime.EXASECOND] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("yottas"))  # nopep8
    CONVERSIONS[UnitsTime.YOTTASECOND][UnitsTime.FEMTOSECOND] = \
        Mul(Rat(Int(1), Pow(10, 39)), Sym("yottas"))  # nopep8
    CONVERSIONS[UnitsTime.YOTTASECOND][UnitsTime.GIGASECOND] = \
        Mul(Rat(Int(1), Pow(10, 15)), Sym("yottas"))  # nopep8
    CONVERSIONS[UnitsTime.YOTTASECOND][UnitsTime.HECTOSECOND] = \
        Mul(Rat(Int(1), Pow(10, 22)), Sym("yottas"))  # nopep8
    CONVERSIONS[UnitsTime.YOTTASECOND][UnitsTime.HOUR] = \
        Mul(Rat(Int(9), Mul(Int(25), Pow(10, 20))), Sym("yottas"))  # nopep8
    CONVERSIONS[UnitsTime.YOTTASECOND][UnitsTime.KILOSECOND] = \
        Mul(Rat(Int(1), Pow(10, 21)), Sym("yottas"))  # nopep8
    CONVERSIONS[UnitsTime.YOTTASECOND][UnitsTime.MEGASECOND] = \
        Mul(Rat(Int(1), Pow(10, 18)), Sym("yottas"))  # nopep8
    CONVERSIONS[UnitsTime.YOTTASECOND][UnitsTime.MICROSECOND] = \
        Mul(Rat(Int(1), Pow(10, 30)), Sym("yottas"))  # nopep8
    CONVERSIONS[UnitsTime.YOTTASECOND][UnitsTime.MILLISECOND] = \
        Mul(Rat(Int(1), Pow(10, 27)), Sym("yottas"))  # nopep8
    CONVERSIONS[UnitsTime.YOTTASECOND][UnitsTime.MINUTE] = \
        Mul(Rat(Int(3), Mul(Int(5), Pow(10, 22))), Sym("yottas"))  # nopep8
    CONVERSIONS[UnitsTime.YOTTASECOND][UnitsTime.NANOSECOND] = \
        Mul(Rat(Int(1), Pow(10, 33)), Sym("yottas"))  # nopep8
    CONVERSIONS[UnitsTime.YOTTASECOND][UnitsTime.PETASECOND] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("yottas"))  # nopep8
    CONVERSIONS[UnitsTime.YOTTASECOND][UnitsTime.PICOSECOND] = \
        Mul(Rat(Int(1), Pow(10, 36)), Sym("yottas"))  # nopep8
    CONVERSIONS[UnitsTime.YOTTASECOND][UnitsTime.SECOND] = \
        Mul(Rat(Int(1), Pow(10, 24)), Sym("yottas"))  # nopep8
    CONVERSIONS[UnitsTime.YOTTASECOND][UnitsTime.TERASECOND] = \
        Mul(Rat(Int(1), Pow(10, 12)), Sym("yottas"))  # nopep8
    CONVERSIONS[UnitsTime.YOTTASECOND][UnitsTime.YOCTOSECOND] = \
        Mul(Rat(Int(1), Pow(10, 48)), Sym("yottas"))  # nopep8
    CONVERSIONS[UnitsTime.YOTTASECOND][UnitsTime.ZEPTOSECOND] = \
        Mul(Rat(Int(1), Pow(10, 45)), Sym("yottas"))  # nopep8
    CONVERSIONS[UnitsTime.YOTTASECOND][UnitsTime.ZETTASECOND] = \
        Mul(Rat(Int(1), Int(1000)), Sym("yottas"))  # nopep8
    CONVERSIONS[UnitsTime.ZEPTOSECOND][UnitsTime.ATTOSECOND] = \
        Mul(Int(1000), Sym("zeptos"))  # nopep8
    CONVERSIONS[UnitsTime.ZEPTOSECOND][UnitsTime.CENTISECOND] = \
        Mul(Pow(10, 19), Sym("zeptos"))  # nopep8
    CONVERSIONS[UnitsTime.ZEPTOSECOND][UnitsTime.DAY] = \
        Mul(Mul(Int(864), Pow(10, 23)), Sym("zeptos"))  # nopep8
    CONVERSIONS[UnitsTime.ZEPTOSECOND][UnitsTime.DECASECOND] = \
        Mul(Pow(10, 22), Sym("zeptos"))  # nopep8
    CONVERSIONS[UnitsTime.ZEPTOSECOND][UnitsTime.DECISECOND] = \
        Mul(Pow(10, 20), Sym("zeptos"))  # nopep8
    CONVERSIONS[UnitsTime.ZEPTOSECOND][UnitsTime.EXASECOND] = \
        Mul(Pow(10, 39), Sym("zeptos"))  # nopep8
    CONVERSIONS[UnitsTime.ZEPTOSECOND][UnitsTime.FEMTOSECOND] = \
        Mul(Pow(10, 6), Sym("zeptos"))  # nopep8
    CONVERSIONS[UnitsTime.ZEPTOSECOND][UnitsTime.GIGASECOND] = \
        Mul(Pow(10, 30), Sym("zeptos"))  # nopep8
    CONVERSIONS[UnitsTime.ZEPTOSECOND][UnitsTime.HECTOSECOND] = \
        Mul(Pow(10, 23), Sym("zeptos"))  # nopep8
    CONVERSIONS[UnitsTime.ZEPTOSECOND][UnitsTime.HOUR] = \
        Mul(Mul(Int(36), Pow(10, 23)), Sym("zeptos"))  # nopep8
    CONVERSIONS[UnitsTime.ZEPTOSECOND][UnitsTime.KILOSECOND] = \
        Mul(Pow(10, 24), Sym("zeptos"))  # nopep8
    CONVERSIONS[UnitsTime.ZEPTOSECOND][UnitsTime.MEGASECOND] = \
        Mul(Pow(10, 27), Sym("zeptos"))  # nopep8
    CONVERSIONS[UnitsTime.ZEPTOSECOND][UnitsTime.MICROSECOND] = \
        Mul(Pow(10, 15), Sym("zeptos"))  # nopep8
    CONVERSIONS[UnitsTime.ZEPTOSECOND][UnitsTime.MILLISECOND] = \
        Mul(Pow(10, 18), Sym("zeptos"))  # nopep8
    CONVERSIONS[UnitsTime.ZEPTOSECOND][UnitsTime.MINUTE] = \
        Mul(Mul(Int(6), Pow(10, 22)), Sym("zeptos"))  # nopep8
    CONVERSIONS[UnitsTime.ZEPTOSECOND][UnitsTime.NANOSECOND] = \
        Mul(Pow(10, 12), Sym("zeptos"))  # nopep8
    CONVERSIONS[UnitsTime.ZEPTOSECOND][UnitsTime.PETASECOND] = \
        Mul(Pow(10, 36), Sym("zeptos"))  # nopep8
    CONVERSIONS[UnitsTime.ZEPTOSECOND][UnitsTime.PICOSECOND] = \
        Mul(Pow(10, 9), Sym("zeptos"))  # nopep8
    CONVERSIONS[UnitsTime.ZEPTOSECOND][UnitsTime.SECOND] = \
        Mul(Pow(10, 21), Sym("zeptos"))  # nopep8
    CONVERSIONS[UnitsTime.ZEPTOSECOND][UnitsTime.TERASECOND] = \
        Mul(Pow(10, 33), Sym("zeptos"))  # nopep8
    CONVERSIONS[UnitsTime.ZEPTOSECOND][UnitsTime.YOCTOSECOND] = \
        Mul(Rat(Int(1), Int(1000)), Sym("zeptos"))  # nopep8
    CONVERSIONS[UnitsTime.ZEPTOSECOND][UnitsTime.YOTTASECOND] = \
        Mul(Pow(10, 45), Sym("zeptos"))  # nopep8
    CONVERSIONS[UnitsTime.ZEPTOSECOND][UnitsTime.ZETTASECOND] = \
        Mul(Pow(10, 42), Sym("zeptos"))  # nopep8
    CONVERSIONS[UnitsTime.ZETTASECOND][UnitsTime.ATTOSECOND] = \
        Mul(Rat(Int(1), Pow(10, 39)), Sym("zettas"))  # nopep8
    CONVERSIONS[UnitsTime.ZETTASECOND][UnitsTime.CENTISECOND] = \
        Mul(Rat(Int(1), Pow(10, 23)), Sym("zettas"))  # nopep8
    CONVERSIONS[UnitsTime.ZETTASECOND][UnitsTime.DAY] = \
        Mul(Rat(Int(27), Mul(Int(3125), Pow(10, 14))), Sym("zettas"))  # nopep8
    CONVERSIONS[UnitsTime.ZETTASECOND][UnitsTime.DECASECOND] = \
        Mul(Rat(Int(1), Pow(10, 20)), Sym("zettas"))  # nopep8
    CONVERSIONS[UnitsTime.ZETTASECOND][UnitsTime.DECISECOND] = \
        Mul(Rat(Int(1), Pow(10, 22)), Sym("zettas"))  # nopep8
    CONVERSIONS[UnitsTime.ZETTASECOND][UnitsTime.EXASECOND] = \
        Mul(Rat(Int(1), Int(1000)), Sym("zettas"))  # nopep8
    CONVERSIONS[UnitsTime.ZETTASECOND][UnitsTime.FEMTOSECOND] = \
        Mul(Rat(Int(1), Pow(10, 36)), Sym("zettas"))  # nopep8
    CONVERSIONS[UnitsTime.ZETTASECOND][UnitsTime.GIGASECOND] = \
        Mul(Rat(Int(1), Pow(10, 12)), Sym("zettas"))  # nopep8
    CONVERSIONS[UnitsTime.ZETTASECOND][UnitsTime.HECTOSECOND] = \
        Mul(Rat(Int(1), Pow(10, 19)), Sym("zettas"))  # nopep8
    CONVERSIONS[UnitsTime.ZETTASECOND][UnitsTime.HOUR] = \
        Mul(Rat(Int(9), Mul(Int(25), Pow(10, 17))), Sym("zettas"))  # nopep8
    CONVERSIONS[UnitsTime.ZETTASECOND][UnitsTime.KILOSECOND] = \
        Mul(Rat(Int(1), Pow(10, 18)), Sym("zettas"))  # nopep8
    CONVERSIONS[UnitsTime.ZETTASECOND][UnitsTime.MEGASECOND] = \
        Mul(Rat(Int(1), Pow(10, 15)), Sym("zettas"))  # nopep8
    CONVERSIONS[UnitsTime.ZETTASECOND][UnitsTime.MICROSECOND] = \
        Mul(Rat(Int(1), Pow(10, 27)), Sym("zettas"))  # nopep8
    CONVERSIONS[UnitsTime.ZETTASECOND][UnitsTime.MILLISECOND] = \
        Mul(Rat(Int(1), Pow(10, 24)), Sym("zettas"))  # nopep8
    CONVERSIONS[UnitsTime.ZETTASECOND][UnitsTime.MINUTE] = \
        Mul(Rat(Int(3), Mul(Int(5), Pow(10, 19))), Sym("zettas"))  # nopep8
    CONVERSIONS[UnitsTime.ZETTASECOND][UnitsTime.NANOSECOND] = \
        Mul(Rat(Int(1), Pow(10, 30)), Sym("zettas"))  # nopep8
    CONVERSIONS[UnitsTime.ZETTASECOND][UnitsTime.PETASECOND] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("zettas"))  # nopep8
    CONVERSIONS[UnitsTime.ZETTASECOND][UnitsTime.PICOSECOND] = \
        Mul(Rat(Int(1), Pow(10, 33)), Sym("zettas"))  # nopep8
    CONVERSIONS[UnitsTime.ZETTASECOND][UnitsTime.SECOND] = \
        Mul(Rat(Int(1), Pow(10, 21)), Sym("zettas"))  # nopep8
    CONVERSIONS[UnitsTime.ZETTASECOND][UnitsTime.TERASECOND] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("zettas"))  # nopep8
    CONVERSIONS[UnitsTime.ZETTASECOND][UnitsTime.YOCTOSECOND] = \
        Mul(Rat(Int(1), Pow(10, 45)), Sym("zettas"))  # nopep8
    CONVERSIONS[UnitsTime.ZETTASECOND][UnitsTime.YOTTASECOND] = \
        Mul(Int(1000), Sym("zettas"))  # nopep8
    CONVERSIONS[UnitsTime.ZETTASECOND][UnitsTime.ZEPTOSECOND] = \
        Mul(Rat(Int(1), Pow(10, 42)), Sym("zettas"))  # nopep8

    SYMBOLS = dict()
    SYMBOLS["ATTOSECOND"] = "as"
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
            targetUnit = getattr(UnitsTime, str(target))
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
        return TimeI.SYMBOLS.get(str(unit))

    def setUnit(self, unit, current=None):
        self._unit = unit

    def setValue(self, value, current=None):
        self._value = value

    def __str__(self):
        return self._base_string(self.getValue(), self.getUnit())

_omero_model.TimeI = TimeI
