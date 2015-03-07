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
Code-generated omero.model.Pressure implementation,
based on omero.model.PermissionsI
"""


import Ice
import IceImport
IceImport.load("omero_model_Pressure_ice")
_omero = Ice.openModule("omero")
_omero_model = Ice.openModule("omero.model")
__name__ = "omero.model"

from omero_model_UnitBase import UnitBase
from omero.model.enums import UnitsPressure

from omero.conversions import Add  # nopep8
from omero.conversions import Int  # nopep8
from omero.conversions import Mul  # nopep8
from omero.conversions import Pow  # nopep8
from omero.conversions import Rat  # nopep8
from omero.conversions import Sym  # nopep8


class PressureI(_omero_model.Pressure, UnitBase):

    try:
        UNIT_VALUES = sorted(UnitsPressure._enumerators.values())
    except:
        # TODO: this occurs on Ice 3.4 and can be removed
        # once it has been dropped.
        UNIT_VALUES = [x for x in sorted(UnitsPressure._names)]
        UNIT_VALUES = [getattr(UnitsPressure, x) for x in UNIT_VALUES]
    CONVERSIONS = dict()
    for val in UNIT_VALUES:
        CONVERSIONS[val] = dict()
    CONVERSIONS[UnitsPressure.ATMOSPHERE][UnitsPressure.ATTOPASCAL] = \
        Mul(Mul(Int(101325), Pow(10, 18)), Sym("atm"))  # nopep8
    CONVERSIONS[UnitsPressure.ATMOSPHERE][UnitsPressure.BAR] = \
        Mul(Rat(Int(4053), Int(4000)), Sym("atm"))  # nopep8
    CONVERSIONS[UnitsPressure.ATMOSPHERE][UnitsPressure.CENTIBAR] = \
        Mul(Rat(Int(4053), Int(40)), Sym("atm"))  # nopep8
    CONVERSIONS[UnitsPressure.ATMOSPHERE][UnitsPressure.CENTIPASCAL] = \
        Mul(Int(10132500), Sym("atm"))  # nopep8
    CONVERSIONS[UnitsPressure.ATMOSPHERE][UnitsPressure.DECAPASCAL] = \
        Mul(Rat(Int(20265), Int(2)), Sym("atm"))  # nopep8
    CONVERSIONS[UnitsPressure.ATMOSPHERE][UnitsPressure.DECIBAR] = \
        Mul(Rat(Int(4053), Int(400)), Sym("atm"))  # nopep8
    CONVERSIONS[UnitsPressure.ATMOSPHERE][UnitsPressure.DECIPASCAL] = \
        Mul(Int(1013250), Sym("atm"))  # nopep8
    CONVERSIONS[UnitsPressure.ATMOSPHERE][UnitsPressure.EXAPASCAL] = \
        Mul(Rat(Int(4053), Mul(Int(4), Pow(10, 16))), Sym("atm"))  # nopep8
    CONVERSIONS[UnitsPressure.ATMOSPHERE][UnitsPressure.FEMTOPASCAL] = \
        Mul(Mul(Int(101325), Pow(10, 15)), Sym("atm"))  # nopep8
    CONVERSIONS[UnitsPressure.ATMOSPHERE][UnitsPressure.GIGAPASCAL] = \
        Mul(Rat(Int(4053), Mul(Int(4), Pow(10, 7))), Sym("atm"))  # nopep8
    CONVERSIONS[UnitsPressure.ATMOSPHERE][UnitsPressure.HECTOPASCAL] = \
        Mul(Rat(Int(4053), Int(4)), Sym("atm"))  # nopep8
    CONVERSIONS[UnitsPressure.ATMOSPHERE][UnitsPressure.KILOBAR] = \
        Mul(Rat(Int(4053), Mul(Int(4), Pow(10, 6))), Sym("atm"))  # nopep8
    CONVERSIONS[UnitsPressure.ATMOSPHERE][UnitsPressure.KILOPASCAL] = \
        Mul(Rat(Int(4053), Int(40)), Sym("atm"))  # nopep8
    CONVERSIONS[UnitsPressure.ATMOSPHERE][UnitsPressure.MEGABAR] = \
        Mul(Rat(Int(4053), Mul(Int(4), Pow(10, 9))), Sym("atm"))  # nopep8
    CONVERSIONS[UnitsPressure.ATMOSPHERE][UnitsPressure.MEGAPASCAL] = \
        Mul(Rat(Int(4053), Mul(Int(4), Pow(10, 4))), Sym("atm"))  # nopep8
    CONVERSIONS[UnitsPressure.ATMOSPHERE][UnitsPressure.MICROPASCAL] = \
        Mul(Mul(Int(101325), Pow(10, 6)), Sym("atm"))  # nopep8
    CONVERSIONS[UnitsPressure.ATMOSPHERE][UnitsPressure.MILLIBAR] = \
        Mul(Rat(Int(4053), Int(4)), Sym("atm"))  # nopep8
    CONVERSIONS[UnitsPressure.ATMOSPHERE][UnitsPressure.MILLIPASCAL] = \
        Mul(Int(101325000), Sym("atm"))  # nopep8
    CONVERSIONS[UnitsPressure.ATMOSPHERE][UnitsPressure.MILLITORR] = \
        Mul(Rat(Int(19), Int(25)), Sym("atm"))  # nopep8
    CONVERSIONS[UnitsPressure.ATMOSPHERE][UnitsPressure.MMHG] = \
        Mul(Rat(Mul(Int(965), Pow(10, 9)), Int(1269737023)), Sym("atm"))  # nopep8
    CONVERSIONS[UnitsPressure.ATMOSPHERE][UnitsPressure.NANOPASCAL] = \
        Mul(Mul(Int(101325), Pow(10, 9)), Sym("atm"))  # nopep8
    CONVERSIONS[UnitsPressure.ATMOSPHERE][UnitsPressure.PETAPASCAL] = \
        Mul(Rat(Int(4053), Mul(Int(4), Pow(10, 13))), Sym("atm"))  # nopep8
    CONVERSIONS[UnitsPressure.ATMOSPHERE][UnitsPressure.PICOPASCAL] = \
        Mul(Mul(Int(101325), Pow(10, 12)), Sym("atm"))  # nopep8
    CONVERSIONS[UnitsPressure.ATMOSPHERE][UnitsPressure.PSI] = \
        Mul(Rat(Mul(Int(120625), Pow(10, 9)), Int("8208044396629")), Sym("atm"))  # nopep8
    CONVERSIONS[UnitsPressure.ATMOSPHERE][UnitsPressure.Pascal] = \
        Mul(Int(101325), Sym("atm"))  # nopep8
    CONVERSIONS[UnitsPressure.ATMOSPHERE][UnitsPressure.TERAPASCAL] = \
        Mul(Rat(Int(4053), Mul(Int(4), Pow(10, 10))), Sym("atm"))  # nopep8
    CONVERSIONS[UnitsPressure.ATMOSPHERE][UnitsPressure.TORR] = \
        Mul(Int(760), Sym("atm"))  # nopep8
    CONVERSIONS[UnitsPressure.ATMOSPHERE][UnitsPressure.YOCTOPASCAL] = \
        Mul(Mul(Int(101325), Pow(10, 24)), Sym("atm"))  # nopep8
    CONVERSIONS[UnitsPressure.ATMOSPHERE][UnitsPressure.YOTTAPASCAL] = \
        Mul(Rat(Int(4053), Mul(Int(4), Pow(10, 22))), Sym("atm"))  # nopep8
    CONVERSIONS[UnitsPressure.ATMOSPHERE][UnitsPressure.ZEPTOPASCAL] = \
        Mul(Mul(Int(101325), Pow(10, 21)), Sym("atm"))  # nopep8
    CONVERSIONS[UnitsPressure.ATMOSPHERE][UnitsPressure.ZETTAPASCAL] = \
        Mul(Rat(Int(4053), Mul(Int(4), Pow(10, 19))), Sym("atm"))  # nopep8
    CONVERSIONS[UnitsPressure.ATTOPASCAL][UnitsPressure.ATMOSPHERE] = \
        Mul(Rat(Int(1), Mul(Int(101325), Pow(10, 18))), Sym("attopa"))  # nopep8
    CONVERSIONS[UnitsPressure.ATTOPASCAL][UnitsPressure.BAR] = \
        Mul(Rat(Int(1), Pow(10, 23)), Sym("attopa"))  # nopep8
    CONVERSIONS[UnitsPressure.ATTOPASCAL][UnitsPressure.CENTIBAR] = \
        Mul(Rat(Int(1), Pow(10, 21)), Sym("attopa"))  # nopep8
    CONVERSIONS[UnitsPressure.ATTOPASCAL][UnitsPressure.CENTIPASCAL] = \
        Mul(Rat(Int(1), Pow(10, 16)), Sym("attopa"))  # nopep8
    CONVERSIONS[UnitsPressure.ATTOPASCAL][UnitsPressure.DECAPASCAL] = \
        Mul(Rat(Int(1), Pow(10, 19)), Sym("attopa"))  # nopep8
    CONVERSIONS[UnitsPressure.ATTOPASCAL][UnitsPressure.DECIBAR] = \
        Mul(Rat(Int(1), Pow(10, 22)), Sym("attopa"))  # nopep8
    CONVERSIONS[UnitsPressure.ATTOPASCAL][UnitsPressure.DECIPASCAL] = \
        Mul(Rat(Int(1), Pow(10, 17)), Sym("attopa"))  # nopep8
    CONVERSIONS[UnitsPressure.ATTOPASCAL][UnitsPressure.EXAPASCAL] = \
        Mul(Rat(Int(1), Pow(10, 36)), Sym("attopa"))  # nopep8
    CONVERSIONS[UnitsPressure.ATTOPASCAL][UnitsPressure.FEMTOPASCAL] = \
        Mul(Rat(Int(1), Int(1000)), Sym("attopa"))  # nopep8
    CONVERSIONS[UnitsPressure.ATTOPASCAL][UnitsPressure.GIGAPASCAL] = \
        Mul(Rat(Int(1), Pow(10, 27)), Sym("attopa"))  # nopep8
    CONVERSIONS[UnitsPressure.ATTOPASCAL][UnitsPressure.HECTOPASCAL] = \
        Mul(Rat(Int(1), Pow(10, 20)), Sym("attopa"))  # nopep8
    CONVERSIONS[UnitsPressure.ATTOPASCAL][UnitsPressure.KILOBAR] = \
        Mul(Rat(Int(1), Pow(10, 26)), Sym("attopa"))  # nopep8
    CONVERSIONS[UnitsPressure.ATTOPASCAL][UnitsPressure.KILOPASCAL] = \
        Mul(Rat(Int(1), Pow(10, 21)), Sym("attopa"))  # nopep8
    CONVERSIONS[UnitsPressure.ATTOPASCAL][UnitsPressure.MEGABAR] = \
        Mul(Rat(Int(1), Pow(10, 29)), Sym("attopa"))  # nopep8
    CONVERSIONS[UnitsPressure.ATTOPASCAL][UnitsPressure.MEGAPASCAL] = \
        Mul(Rat(Int(1), Pow(10, 24)), Sym("attopa"))  # nopep8
    CONVERSIONS[UnitsPressure.ATTOPASCAL][UnitsPressure.MICROPASCAL] = \
        Mul(Rat(Int(1), Pow(10, 12)), Sym("attopa"))  # nopep8
    CONVERSIONS[UnitsPressure.ATTOPASCAL][UnitsPressure.MILLIBAR] = \
        Mul(Rat(Int(1), Pow(10, 20)), Sym("attopa"))  # nopep8
    CONVERSIONS[UnitsPressure.ATTOPASCAL][UnitsPressure.MILLIPASCAL] = \
        Mul(Rat(Int(1), Pow(10, 15)), Sym("attopa"))  # nopep8
    CONVERSIONS[UnitsPressure.ATTOPASCAL][UnitsPressure.MILLITORR] = \
        Mul(Rat(Int(19), Mul(Int(2533125), Pow(10, 18))), Sym("attopa"))  # nopep8
    CONVERSIONS[UnitsPressure.ATTOPASCAL][UnitsPressure.MMHG] = \
        Mul(Rat(Int(1), Mul(Int("133322387415"), Pow(10, 9))), Sym("attopa"))  # nopep8
    CONVERSIONS[UnitsPressure.ATTOPASCAL][UnitsPressure.NANOPASCAL] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("attopa"))  # nopep8
    CONVERSIONS[UnitsPressure.ATTOPASCAL][UnitsPressure.PETAPASCAL] = \
        Mul(Rat(Int(1), Pow(10, 33)), Sym("attopa"))  # nopep8
    CONVERSIONS[UnitsPressure.ATTOPASCAL][UnitsPressure.PICOPASCAL] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("attopa"))  # nopep8
    CONVERSIONS[UnitsPressure.ATTOPASCAL][UnitsPressure.PSI] = \
        Mul(Rat(Int(1), Mul(Int("689475729316836"), Pow(10, 7))), Sym("attopa"))  # nopep8
    CONVERSIONS[UnitsPressure.ATTOPASCAL][UnitsPressure.Pascal] = \
        Mul(Rat(Int(1), Pow(10, 18)), Sym("attopa"))  # nopep8
    CONVERSIONS[UnitsPressure.ATTOPASCAL][UnitsPressure.TERAPASCAL] = \
        Mul(Rat(Int(1), Pow(10, 30)), Sym("attopa"))  # nopep8
    CONVERSIONS[UnitsPressure.ATTOPASCAL][UnitsPressure.TORR] = \
        Mul(Rat(Int(19), Mul(Int(2533125), Pow(10, 15))), Sym("attopa"))  # nopep8
    CONVERSIONS[UnitsPressure.ATTOPASCAL][UnitsPressure.YOCTOPASCAL] = \
        Mul(Pow(10, 6), Sym("attopa"))  # nopep8
    CONVERSIONS[UnitsPressure.ATTOPASCAL][UnitsPressure.YOTTAPASCAL] = \
        Mul(Rat(Int(1), Pow(10, 42)), Sym("attopa"))  # nopep8
    CONVERSIONS[UnitsPressure.ATTOPASCAL][UnitsPressure.ZEPTOPASCAL] = \
        Mul(Int(1000), Sym("attopa"))  # nopep8
    CONVERSIONS[UnitsPressure.ATTOPASCAL][UnitsPressure.ZETTAPASCAL] = \
        Mul(Rat(Int(1), Pow(10, 39)), Sym("attopa"))  # nopep8
    CONVERSIONS[UnitsPressure.BAR][UnitsPressure.ATMOSPHERE] = \
        Mul(Rat(Int(4000), Int(4053)), Sym("bar"))  # nopep8
    CONVERSIONS[UnitsPressure.BAR][UnitsPressure.ATTOPASCAL] = \
        Mul(Pow(10, 23), Sym("bar"))  # nopep8
    CONVERSIONS[UnitsPressure.BAR][UnitsPressure.CENTIBAR] = \
        Mul(Int(100), Sym("bar"))  # nopep8
    CONVERSIONS[UnitsPressure.BAR][UnitsPressure.CENTIPASCAL] = \
        Mul(Pow(10, 7), Sym("bar"))  # nopep8
    CONVERSIONS[UnitsPressure.BAR][UnitsPressure.DECAPASCAL] = \
        Mul(Pow(10, 4), Sym("bar"))  # nopep8
    CONVERSIONS[UnitsPressure.BAR][UnitsPressure.DECIBAR] = \
        Mul(Int(10), Sym("bar"))  # nopep8
    CONVERSIONS[UnitsPressure.BAR][UnitsPressure.DECIPASCAL] = \
        Mul(Pow(10, 6), Sym("bar"))  # nopep8
    CONVERSIONS[UnitsPressure.BAR][UnitsPressure.EXAPASCAL] = \
        Mul(Rat(Int(1), Pow(10, 13)), Sym("bar"))  # nopep8
    CONVERSIONS[UnitsPressure.BAR][UnitsPressure.FEMTOPASCAL] = \
        Mul(Pow(10, 20), Sym("bar"))  # nopep8
    CONVERSIONS[UnitsPressure.BAR][UnitsPressure.GIGAPASCAL] = \
        Mul(Rat(Int(1), Pow(10, 4)), Sym("bar"))  # nopep8
    CONVERSIONS[UnitsPressure.BAR][UnitsPressure.HECTOPASCAL] = \
        Mul(Int(1000), Sym("bar"))  # nopep8
    CONVERSIONS[UnitsPressure.BAR][UnitsPressure.KILOBAR] = \
        Mul(Rat(Int(1), Int(1000)), Sym("bar"))  # nopep8
    CONVERSIONS[UnitsPressure.BAR][UnitsPressure.KILOPASCAL] = \
        Mul(Int(100), Sym("bar"))  # nopep8
    CONVERSIONS[UnitsPressure.BAR][UnitsPressure.MEGABAR] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("bar"))  # nopep8
    CONVERSIONS[UnitsPressure.BAR][UnitsPressure.MEGAPASCAL] = \
        Mul(Rat(Int(1), Int(10)), Sym("bar"))  # nopep8
    CONVERSIONS[UnitsPressure.BAR][UnitsPressure.MICROPASCAL] = \
        Mul(Pow(10, 11), Sym("bar"))  # nopep8
    CONVERSIONS[UnitsPressure.BAR][UnitsPressure.MILLIBAR] = \
        Mul(Int(1000), Sym("bar"))  # nopep8
    CONVERSIONS[UnitsPressure.BAR][UnitsPressure.MILLIPASCAL] = \
        Mul(Pow(10, 8), Sym("bar"))  # nopep8
    CONVERSIONS[UnitsPressure.BAR][UnitsPressure.MILLITORR] = \
        Mul(Rat(Int(3040), Int(4053)), Sym("bar"))  # nopep8
    CONVERSIONS[UnitsPressure.BAR][UnitsPressure.MMHG] = \
        Mul(Rat(Mul(Int(2), Pow(10, 13)), Int("26664477483")), Sym("bar"))  # nopep8
    CONVERSIONS[UnitsPressure.BAR][UnitsPressure.NANOPASCAL] = \
        Mul(Pow(10, 14), Sym("bar"))  # nopep8
    CONVERSIONS[UnitsPressure.BAR][UnitsPressure.PETAPASCAL] = \
        Mul(Rat(Int(1), Pow(10, 10)), Sym("bar"))  # nopep8
    CONVERSIONS[UnitsPressure.BAR][UnitsPressure.PICOPASCAL] = \
        Mul(Pow(10, 17), Sym("bar"))  # nopep8
    CONVERSIONS[UnitsPressure.BAR][UnitsPressure.PSI] = \
        Mul(Rat(Mul(Int(25), Pow(10, 14)), Int("172368932329209")), Sym("bar"))  # nopep8
    CONVERSIONS[UnitsPressure.BAR][UnitsPressure.Pascal] = \
        Mul(Pow(10, 5), Sym("bar"))  # nopep8
    CONVERSIONS[UnitsPressure.BAR][UnitsPressure.TERAPASCAL] = \
        Mul(Rat(Int(1), Pow(10, 7)), Sym("bar"))  # nopep8
    CONVERSIONS[UnitsPressure.BAR][UnitsPressure.TORR] = \
        Mul(Rat(Mul(Int(304), Pow(10, 4)), Int(4053)), Sym("bar"))  # nopep8
    CONVERSIONS[UnitsPressure.BAR][UnitsPressure.YOCTOPASCAL] = \
        Mul(Pow(10, 29), Sym("bar"))  # nopep8
    CONVERSIONS[UnitsPressure.BAR][UnitsPressure.YOTTAPASCAL] = \
        Mul(Rat(Int(1), Pow(10, 19)), Sym("bar"))  # nopep8
    CONVERSIONS[UnitsPressure.BAR][UnitsPressure.ZEPTOPASCAL] = \
        Mul(Pow(10, 26), Sym("bar"))  # nopep8
    CONVERSIONS[UnitsPressure.BAR][UnitsPressure.ZETTAPASCAL] = \
        Mul(Rat(Int(1), Pow(10, 16)), Sym("bar"))  # nopep8
    CONVERSIONS[UnitsPressure.CENTIBAR][UnitsPressure.ATMOSPHERE] = \
        Mul(Rat(Int(40), Int(4053)), Sym("cbar"))  # nopep8
    CONVERSIONS[UnitsPressure.CENTIBAR][UnitsPressure.ATTOPASCAL] = \
        Mul(Pow(10, 21), Sym("cbar"))  # nopep8
    CONVERSIONS[UnitsPressure.CENTIBAR][UnitsPressure.BAR] = \
        Mul(Rat(Int(1), Int(100)), Sym("cbar"))  # nopep8
    CONVERSIONS[UnitsPressure.CENTIBAR][UnitsPressure.CENTIPASCAL] = \
        Mul(Pow(10, 5), Sym("cbar"))  # nopep8
    CONVERSIONS[UnitsPressure.CENTIBAR][UnitsPressure.DECAPASCAL] = \
        Mul(Int(100), Sym("cbar"))  # nopep8
    CONVERSIONS[UnitsPressure.CENTIBAR][UnitsPressure.DECIBAR] = \
        Mul(Rat(Int(1), Int(10)), Sym("cbar"))  # nopep8
    CONVERSIONS[UnitsPressure.CENTIBAR][UnitsPressure.DECIPASCAL] = \
        Mul(Pow(10, 4), Sym("cbar"))  # nopep8
    CONVERSIONS[UnitsPressure.CENTIBAR][UnitsPressure.EXAPASCAL] = \
        Mul(Rat(Int(1), Pow(10, 15)), Sym("cbar"))  # nopep8
    CONVERSIONS[UnitsPressure.CENTIBAR][UnitsPressure.FEMTOPASCAL] = \
        Mul(Pow(10, 18), Sym("cbar"))  # nopep8
    CONVERSIONS[UnitsPressure.CENTIBAR][UnitsPressure.GIGAPASCAL] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("cbar"))  # nopep8
    CONVERSIONS[UnitsPressure.CENTIBAR][UnitsPressure.HECTOPASCAL] = \
        Mul(Int(10), Sym("cbar"))  # nopep8
    CONVERSIONS[UnitsPressure.CENTIBAR][UnitsPressure.KILOBAR] = \
        Mul(Rat(Int(1), Pow(10, 5)), Sym("cbar"))  # nopep8
    CONVERSIONS[UnitsPressure.CENTIBAR][UnitsPressure.KILOPASCAL] = \
        Sym("cbar")  # nopep8
    CONVERSIONS[UnitsPressure.CENTIBAR][UnitsPressure.MEGABAR] = \
        Mul(Rat(Int(1), Pow(10, 8)), Sym("cbar"))  # nopep8
    CONVERSIONS[UnitsPressure.CENTIBAR][UnitsPressure.MEGAPASCAL] = \
        Mul(Rat(Int(1), Int(1000)), Sym("cbar"))  # nopep8
    CONVERSIONS[UnitsPressure.CENTIBAR][UnitsPressure.MICROPASCAL] = \
        Mul(Pow(10, 9), Sym("cbar"))  # nopep8
    CONVERSIONS[UnitsPressure.CENTIBAR][UnitsPressure.MILLIBAR] = \
        Mul(Int(10), Sym("cbar"))  # nopep8
    CONVERSIONS[UnitsPressure.CENTIBAR][UnitsPressure.MILLIPASCAL] = \
        Mul(Pow(10, 6), Sym("cbar"))  # nopep8
    CONVERSIONS[UnitsPressure.CENTIBAR][UnitsPressure.MILLITORR] = \
        Mul(Rat(Int(152), Int(20265)), Sym("cbar"))  # nopep8
    CONVERSIONS[UnitsPressure.CENTIBAR][UnitsPressure.MMHG] = \
        Mul(Rat(Mul(Int(2), Pow(10, 11)), Int("26664477483")), Sym("cbar"))  # nopep8
    CONVERSIONS[UnitsPressure.CENTIBAR][UnitsPressure.NANOPASCAL] = \
        Mul(Pow(10, 12), Sym("cbar"))  # nopep8
    CONVERSIONS[UnitsPressure.CENTIBAR][UnitsPressure.PETAPASCAL] = \
        Mul(Rat(Int(1), Pow(10, 12)), Sym("cbar"))  # nopep8
    CONVERSIONS[UnitsPressure.CENTIBAR][UnitsPressure.PICOPASCAL] = \
        Mul(Pow(10, 15), Sym("cbar"))  # nopep8
    CONVERSIONS[UnitsPressure.CENTIBAR][UnitsPressure.PSI] = \
        Mul(Rat(Mul(Int(25), Pow(10, 12)), Int("172368932329209")), Sym("cbar"))  # nopep8
    CONVERSIONS[UnitsPressure.CENTIBAR][UnitsPressure.Pascal] = \
        Mul(Int(1000), Sym("cbar"))  # nopep8
    CONVERSIONS[UnitsPressure.CENTIBAR][UnitsPressure.TERAPASCAL] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("cbar"))  # nopep8
    CONVERSIONS[UnitsPressure.CENTIBAR][UnitsPressure.TORR] = \
        Mul(Rat(Int(30400), Int(4053)), Sym("cbar"))  # nopep8
    CONVERSIONS[UnitsPressure.CENTIBAR][UnitsPressure.YOCTOPASCAL] = \
        Mul(Pow(10, 27), Sym("cbar"))  # nopep8
    CONVERSIONS[UnitsPressure.CENTIBAR][UnitsPressure.YOTTAPASCAL] = \
        Mul(Rat(Int(1), Pow(10, 21)), Sym("cbar"))  # nopep8
    CONVERSIONS[UnitsPressure.CENTIBAR][UnitsPressure.ZEPTOPASCAL] = \
        Mul(Pow(10, 24), Sym("cbar"))  # nopep8
    CONVERSIONS[UnitsPressure.CENTIBAR][UnitsPressure.ZETTAPASCAL] = \
        Mul(Rat(Int(1), Pow(10, 18)), Sym("cbar"))  # nopep8
    CONVERSIONS[UnitsPressure.CENTIPASCAL][UnitsPressure.ATMOSPHERE] = \
        Mul(Rat(Int(1), Int(10132500)), Sym("centipa"))  # nopep8
    CONVERSIONS[UnitsPressure.CENTIPASCAL][UnitsPressure.ATTOPASCAL] = \
        Mul(Pow(10, 16), Sym("centipa"))  # nopep8
    CONVERSIONS[UnitsPressure.CENTIPASCAL][UnitsPressure.BAR] = \
        Mul(Rat(Int(1), Pow(10, 7)), Sym("centipa"))  # nopep8
    CONVERSIONS[UnitsPressure.CENTIPASCAL][UnitsPressure.CENTIBAR] = \
        Mul(Rat(Int(1), Pow(10, 5)), Sym("centipa"))  # nopep8
    CONVERSIONS[UnitsPressure.CENTIPASCAL][UnitsPressure.DECAPASCAL] = \
        Mul(Rat(Int(1), Int(1000)), Sym("centipa"))  # nopep8
    CONVERSIONS[UnitsPressure.CENTIPASCAL][UnitsPressure.DECIBAR] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("centipa"))  # nopep8
    CONVERSIONS[UnitsPressure.CENTIPASCAL][UnitsPressure.DECIPASCAL] = \
        Mul(Rat(Int(1), Int(10)), Sym("centipa"))  # nopep8
    CONVERSIONS[UnitsPressure.CENTIPASCAL][UnitsPressure.EXAPASCAL] = \
        Mul(Rat(Int(1), Pow(10, 20)), Sym("centipa"))  # nopep8
    CONVERSIONS[UnitsPressure.CENTIPASCAL][UnitsPressure.FEMTOPASCAL] = \
        Mul(Pow(10, 13), Sym("centipa"))  # nopep8
    CONVERSIONS[UnitsPressure.CENTIPASCAL][UnitsPressure.GIGAPASCAL] = \
        Mul(Rat(Int(1), Pow(10, 11)), Sym("centipa"))  # nopep8
    CONVERSIONS[UnitsPressure.CENTIPASCAL][UnitsPressure.HECTOPASCAL] = \
        Mul(Rat(Int(1), Pow(10, 4)), Sym("centipa"))  # nopep8
    CONVERSIONS[UnitsPressure.CENTIPASCAL][UnitsPressure.KILOBAR] = \
        Mul(Rat(Int(1), Pow(10, 10)), Sym("centipa"))  # nopep8
    CONVERSIONS[UnitsPressure.CENTIPASCAL][UnitsPressure.KILOPASCAL] = \
        Mul(Rat(Int(1), Pow(10, 5)), Sym("centipa"))  # nopep8
    CONVERSIONS[UnitsPressure.CENTIPASCAL][UnitsPressure.MEGABAR] = \
        Mul(Rat(Int(1), Pow(10, 13)), Sym("centipa"))  # nopep8
    CONVERSIONS[UnitsPressure.CENTIPASCAL][UnitsPressure.MEGAPASCAL] = \
        Mul(Rat(Int(1), Pow(10, 8)), Sym("centipa"))  # nopep8
    CONVERSIONS[UnitsPressure.CENTIPASCAL][UnitsPressure.MICROPASCAL] = \
        Mul(Pow(10, 4), Sym("centipa"))  # nopep8
    CONVERSIONS[UnitsPressure.CENTIPASCAL][UnitsPressure.MILLIBAR] = \
        Mul(Rat(Int(1), Pow(10, 4)), Sym("centipa"))  # nopep8
    CONVERSIONS[UnitsPressure.CENTIPASCAL][UnitsPressure.MILLIPASCAL] = \
        Mul(Int(10), Sym("centipa"))  # nopep8
    CONVERSIONS[UnitsPressure.CENTIPASCAL][UnitsPressure.MILLITORR] = \
        Mul(Rat(Int(19), Int(253312500)), Sym("centipa"))  # nopep8
    CONVERSIONS[UnitsPressure.CENTIPASCAL][UnitsPressure.MMHG] = \
        Mul(Rat(Mul(Int(2), Pow(10, 6)), Int("26664477483")), Sym("centipa"))  # nopep8
    CONVERSIONS[UnitsPressure.CENTIPASCAL][UnitsPressure.NANOPASCAL] = \
        Mul(Pow(10, 7), Sym("centipa"))  # nopep8
    CONVERSIONS[UnitsPressure.CENTIPASCAL][UnitsPressure.PETAPASCAL] = \
        Mul(Rat(Int(1), Pow(10, 17)), Sym("centipa"))  # nopep8
    CONVERSIONS[UnitsPressure.CENTIPASCAL][UnitsPressure.PICOPASCAL] = \
        Mul(Pow(10, 10), Sym("centipa"))  # nopep8
    CONVERSIONS[UnitsPressure.CENTIPASCAL][UnitsPressure.PSI] = \
        Mul(Rat(Mul(Int(25), Pow(10, 7)), Int("172368932329209")), Sym("centipa"))  # nopep8
    CONVERSIONS[UnitsPressure.CENTIPASCAL][UnitsPressure.Pascal] = \
        Mul(Rat(Int(1), Int(100)), Sym("centipa"))  # nopep8
    CONVERSIONS[UnitsPressure.CENTIPASCAL][UnitsPressure.TERAPASCAL] = \
        Mul(Rat(Int(1), Pow(10, 14)), Sym("centipa"))  # nopep8
    CONVERSIONS[UnitsPressure.CENTIPASCAL][UnitsPressure.TORR] = \
        Mul(Rat(Int(38), Int(506625)), Sym("centipa"))  # nopep8
    CONVERSIONS[UnitsPressure.CENTIPASCAL][UnitsPressure.YOCTOPASCAL] = \
        Mul(Pow(10, 22), Sym("centipa"))  # nopep8
    CONVERSIONS[UnitsPressure.CENTIPASCAL][UnitsPressure.YOTTAPASCAL] = \
        Mul(Rat(Int(1), Pow(10, 26)), Sym("centipa"))  # nopep8
    CONVERSIONS[UnitsPressure.CENTIPASCAL][UnitsPressure.ZEPTOPASCAL] = \
        Mul(Pow(10, 19), Sym("centipa"))  # nopep8
    CONVERSIONS[UnitsPressure.CENTIPASCAL][UnitsPressure.ZETTAPASCAL] = \
        Mul(Rat(Int(1), Pow(10, 23)), Sym("centipa"))  # nopep8
    CONVERSIONS[UnitsPressure.DECAPASCAL][UnitsPressure.ATMOSPHERE] = \
        Mul(Rat(Int(2), Int(20265)), Sym("decapa"))  # nopep8
    CONVERSIONS[UnitsPressure.DECAPASCAL][UnitsPressure.ATTOPASCAL] = \
        Mul(Pow(10, 19), Sym("decapa"))  # nopep8
    CONVERSIONS[UnitsPressure.DECAPASCAL][UnitsPressure.BAR] = \
        Mul(Rat(Int(1), Pow(10, 4)), Sym("decapa"))  # nopep8
    CONVERSIONS[UnitsPressure.DECAPASCAL][UnitsPressure.CENTIBAR] = \
        Mul(Rat(Int(1), Int(100)), Sym("decapa"))  # nopep8
    CONVERSIONS[UnitsPressure.DECAPASCAL][UnitsPressure.CENTIPASCAL] = \
        Mul(Int(1000), Sym("decapa"))  # nopep8
    CONVERSIONS[UnitsPressure.DECAPASCAL][UnitsPressure.DECIBAR] = \
        Mul(Rat(Int(1), Int(1000)), Sym("decapa"))  # nopep8
    CONVERSIONS[UnitsPressure.DECAPASCAL][UnitsPressure.DECIPASCAL] = \
        Mul(Int(100), Sym("decapa"))  # nopep8
    CONVERSIONS[UnitsPressure.DECAPASCAL][UnitsPressure.EXAPASCAL] = \
        Mul(Rat(Int(1), Pow(10, 17)), Sym("decapa"))  # nopep8
    CONVERSIONS[UnitsPressure.DECAPASCAL][UnitsPressure.FEMTOPASCAL] = \
        Mul(Pow(10, 16), Sym("decapa"))  # nopep8
    CONVERSIONS[UnitsPressure.DECAPASCAL][UnitsPressure.GIGAPASCAL] = \
        Mul(Rat(Int(1), Pow(10, 8)), Sym("decapa"))  # nopep8
    CONVERSIONS[UnitsPressure.DECAPASCAL][UnitsPressure.HECTOPASCAL] = \
        Mul(Rat(Int(1), Int(10)), Sym("decapa"))  # nopep8
    CONVERSIONS[UnitsPressure.DECAPASCAL][UnitsPressure.KILOBAR] = \
        Mul(Rat(Int(1), Pow(10, 7)), Sym("decapa"))  # nopep8
    CONVERSIONS[UnitsPressure.DECAPASCAL][UnitsPressure.KILOPASCAL] = \
        Mul(Rat(Int(1), Int(100)), Sym("decapa"))  # nopep8
    CONVERSIONS[UnitsPressure.DECAPASCAL][UnitsPressure.MEGABAR] = \
        Mul(Rat(Int(1), Pow(10, 10)), Sym("decapa"))  # nopep8
    CONVERSIONS[UnitsPressure.DECAPASCAL][UnitsPressure.MEGAPASCAL] = \
        Mul(Rat(Int(1), Pow(10, 5)), Sym("decapa"))  # nopep8
    CONVERSIONS[UnitsPressure.DECAPASCAL][UnitsPressure.MICROPASCAL] = \
        Mul(Pow(10, 7), Sym("decapa"))  # nopep8
    CONVERSIONS[UnitsPressure.DECAPASCAL][UnitsPressure.MILLIBAR] = \
        Mul(Rat(Int(1), Int(10)), Sym("decapa"))  # nopep8
    CONVERSIONS[UnitsPressure.DECAPASCAL][UnitsPressure.MILLIPASCAL] = \
        Mul(Pow(10, 4), Sym("decapa"))  # nopep8
    CONVERSIONS[UnitsPressure.DECAPASCAL][UnitsPressure.MILLITORR] = \
        Mul(Rat(Int(38), Int(506625)), Sym("decapa"))  # nopep8
    CONVERSIONS[UnitsPressure.DECAPASCAL][UnitsPressure.MMHG] = \
        Mul(Rat(Mul(Int(2), Pow(10, 9)), Int("26664477483")), Sym("decapa"))  # nopep8
    CONVERSIONS[UnitsPressure.DECAPASCAL][UnitsPressure.NANOPASCAL] = \
        Mul(Pow(10, 10), Sym("decapa"))  # nopep8
    CONVERSIONS[UnitsPressure.DECAPASCAL][UnitsPressure.PETAPASCAL] = \
        Mul(Rat(Int(1), Pow(10, 14)), Sym("decapa"))  # nopep8
    CONVERSIONS[UnitsPressure.DECAPASCAL][UnitsPressure.PICOPASCAL] = \
        Mul(Pow(10, 13), Sym("decapa"))  # nopep8
    CONVERSIONS[UnitsPressure.DECAPASCAL][UnitsPressure.PSI] = \
        Mul(Rat(Mul(Int(25), Pow(10, 10)), Int("172368932329209")), Sym("decapa"))  # nopep8
    CONVERSIONS[UnitsPressure.DECAPASCAL][UnitsPressure.Pascal] = \
        Mul(Int(10), Sym("decapa"))  # nopep8
    CONVERSIONS[UnitsPressure.DECAPASCAL][UnitsPressure.TERAPASCAL] = \
        Mul(Rat(Int(1), Pow(10, 11)), Sym("decapa"))  # nopep8
    CONVERSIONS[UnitsPressure.DECAPASCAL][UnitsPressure.TORR] = \
        Mul(Rat(Int(304), Int(4053)), Sym("decapa"))  # nopep8
    CONVERSIONS[UnitsPressure.DECAPASCAL][UnitsPressure.YOCTOPASCAL] = \
        Mul(Pow(10, 25), Sym("decapa"))  # nopep8
    CONVERSIONS[UnitsPressure.DECAPASCAL][UnitsPressure.YOTTAPASCAL] = \
        Mul(Rat(Int(1), Pow(10, 23)), Sym("decapa"))  # nopep8
    CONVERSIONS[UnitsPressure.DECAPASCAL][UnitsPressure.ZEPTOPASCAL] = \
        Mul(Pow(10, 22), Sym("decapa"))  # nopep8
    CONVERSIONS[UnitsPressure.DECAPASCAL][UnitsPressure.ZETTAPASCAL] = \
        Mul(Rat(Int(1), Pow(10, 20)), Sym("decapa"))  # nopep8
    CONVERSIONS[UnitsPressure.DECIBAR][UnitsPressure.ATMOSPHERE] = \
        Mul(Rat(Int(400), Int(4053)), Sym("dbar"))  # nopep8
    CONVERSIONS[UnitsPressure.DECIBAR][UnitsPressure.ATTOPASCAL] = \
        Mul(Pow(10, 22), Sym("dbar"))  # nopep8
    CONVERSIONS[UnitsPressure.DECIBAR][UnitsPressure.BAR] = \
        Mul(Rat(Int(1), Int(10)), Sym("dbar"))  # nopep8
    CONVERSIONS[UnitsPressure.DECIBAR][UnitsPressure.CENTIBAR] = \
        Mul(Int(10), Sym("dbar"))  # nopep8
    CONVERSIONS[UnitsPressure.DECIBAR][UnitsPressure.CENTIPASCAL] = \
        Mul(Pow(10, 6), Sym("dbar"))  # nopep8
    CONVERSIONS[UnitsPressure.DECIBAR][UnitsPressure.DECAPASCAL] = \
        Mul(Int(1000), Sym("dbar"))  # nopep8
    CONVERSIONS[UnitsPressure.DECIBAR][UnitsPressure.DECIPASCAL] = \
        Mul(Pow(10, 5), Sym("dbar"))  # nopep8
    CONVERSIONS[UnitsPressure.DECIBAR][UnitsPressure.EXAPASCAL] = \
        Mul(Rat(Int(1), Pow(10, 14)), Sym("dbar"))  # nopep8
    CONVERSIONS[UnitsPressure.DECIBAR][UnitsPressure.FEMTOPASCAL] = \
        Mul(Pow(10, 19), Sym("dbar"))  # nopep8
    CONVERSIONS[UnitsPressure.DECIBAR][UnitsPressure.GIGAPASCAL] = \
        Mul(Rat(Int(1), Pow(10, 5)), Sym("dbar"))  # nopep8
    CONVERSIONS[UnitsPressure.DECIBAR][UnitsPressure.HECTOPASCAL] = \
        Mul(Int(100), Sym("dbar"))  # nopep8
    CONVERSIONS[UnitsPressure.DECIBAR][UnitsPressure.KILOBAR] = \
        Mul(Rat(Int(1), Pow(10, 4)), Sym("dbar"))  # nopep8
    CONVERSIONS[UnitsPressure.DECIBAR][UnitsPressure.KILOPASCAL] = \
        Mul(Int(10), Sym("dbar"))  # nopep8
    CONVERSIONS[UnitsPressure.DECIBAR][UnitsPressure.MEGABAR] = \
        Mul(Rat(Int(1), Pow(10, 7)), Sym("dbar"))  # nopep8
    CONVERSIONS[UnitsPressure.DECIBAR][UnitsPressure.MEGAPASCAL] = \
        Mul(Rat(Int(1), Int(100)), Sym("dbar"))  # nopep8
    CONVERSIONS[UnitsPressure.DECIBAR][UnitsPressure.MICROPASCAL] = \
        Mul(Pow(10, 10), Sym("dbar"))  # nopep8
    CONVERSIONS[UnitsPressure.DECIBAR][UnitsPressure.MILLIBAR] = \
        Mul(Int(100), Sym("dbar"))  # nopep8
    CONVERSIONS[UnitsPressure.DECIBAR][UnitsPressure.MILLIPASCAL] = \
        Mul(Pow(10, 7), Sym("dbar"))  # nopep8
    CONVERSIONS[UnitsPressure.DECIBAR][UnitsPressure.MILLITORR] = \
        Mul(Rat(Int(304), Int(4053)), Sym("dbar"))  # nopep8
    CONVERSIONS[UnitsPressure.DECIBAR][UnitsPressure.MMHG] = \
        Mul(Rat(Mul(Int(2), Pow(10, 12)), Int("26664477483")), Sym("dbar"))  # nopep8
    CONVERSIONS[UnitsPressure.DECIBAR][UnitsPressure.NANOPASCAL] = \
        Mul(Pow(10, 13), Sym("dbar"))  # nopep8
    CONVERSIONS[UnitsPressure.DECIBAR][UnitsPressure.PETAPASCAL] = \
        Mul(Rat(Int(1), Pow(10, 11)), Sym("dbar"))  # nopep8
    CONVERSIONS[UnitsPressure.DECIBAR][UnitsPressure.PICOPASCAL] = \
        Mul(Pow(10, 16), Sym("dbar"))  # nopep8
    CONVERSIONS[UnitsPressure.DECIBAR][UnitsPressure.PSI] = \
        Mul(Rat(Mul(Int(25), Pow(10, 13)), Int("172368932329209")), Sym("dbar"))  # nopep8
    CONVERSIONS[UnitsPressure.DECIBAR][UnitsPressure.Pascal] = \
        Mul(Pow(10, 4), Sym("dbar"))  # nopep8
    CONVERSIONS[UnitsPressure.DECIBAR][UnitsPressure.TERAPASCAL] = \
        Mul(Rat(Int(1), Pow(10, 8)), Sym("dbar"))  # nopep8
    CONVERSIONS[UnitsPressure.DECIBAR][UnitsPressure.TORR] = \
        Mul(Rat(Int(304000), Int(4053)), Sym("dbar"))  # nopep8
    CONVERSIONS[UnitsPressure.DECIBAR][UnitsPressure.YOCTOPASCAL] = \
        Mul(Pow(10, 28), Sym("dbar"))  # nopep8
    CONVERSIONS[UnitsPressure.DECIBAR][UnitsPressure.YOTTAPASCAL] = \
        Mul(Rat(Int(1), Pow(10, 20)), Sym("dbar"))  # nopep8
    CONVERSIONS[UnitsPressure.DECIBAR][UnitsPressure.ZEPTOPASCAL] = \
        Mul(Pow(10, 25), Sym("dbar"))  # nopep8
    CONVERSIONS[UnitsPressure.DECIBAR][UnitsPressure.ZETTAPASCAL] = \
        Mul(Rat(Int(1), Pow(10, 17)), Sym("dbar"))  # nopep8
    CONVERSIONS[UnitsPressure.DECIPASCAL][UnitsPressure.ATMOSPHERE] = \
        Mul(Rat(Int(1), Int(1013250)), Sym("decipa"))  # nopep8
    CONVERSIONS[UnitsPressure.DECIPASCAL][UnitsPressure.ATTOPASCAL] = \
        Mul(Pow(10, 17), Sym("decipa"))  # nopep8
    CONVERSIONS[UnitsPressure.DECIPASCAL][UnitsPressure.BAR] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("decipa"))  # nopep8
    CONVERSIONS[UnitsPressure.DECIPASCAL][UnitsPressure.CENTIBAR] = \
        Mul(Rat(Int(1), Pow(10, 4)), Sym("decipa"))  # nopep8
    CONVERSIONS[UnitsPressure.DECIPASCAL][UnitsPressure.CENTIPASCAL] = \
        Mul(Int(10), Sym("decipa"))  # nopep8
    CONVERSIONS[UnitsPressure.DECIPASCAL][UnitsPressure.DECAPASCAL] = \
        Mul(Rat(Int(1), Int(100)), Sym("decipa"))  # nopep8
    CONVERSIONS[UnitsPressure.DECIPASCAL][UnitsPressure.DECIBAR] = \
        Mul(Rat(Int(1), Pow(10, 5)), Sym("decipa"))  # nopep8
    CONVERSIONS[UnitsPressure.DECIPASCAL][UnitsPressure.EXAPASCAL] = \
        Mul(Rat(Int(1), Pow(10, 19)), Sym("decipa"))  # nopep8
    CONVERSIONS[UnitsPressure.DECIPASCAL][UnitsPressure.FEMTOPASCAL] = \
        Mul(Pow(10, 14), Sym("decipa"))  # nopep8
    CONVERSIONS[UnitsPressure.DECIPASCAL][UnitsPressure.GIGAPASCAL] = \
        Mul(Rat(Int(1), Pow(10, 10)), Sym("decipa"))  # nopep8
    CONVERSIONS[UnitsPressure.DECIPASCAL][UnitsPressure.HECTOPASCAL] = \
        Mul(Rat(Int(1), Int(1000)), Sym("decipa"))  # nopep8
    CONVERSIONS[UnitsPressure.DECIPASCAL][UnitsPressure.KILOBAR] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("decipa"))  # nopep8
    CONVERSIONS[UnitsPressure.DECIPASCAL][UnitsPressure.KILOPASCAL] = \
        Mul(Rat(Int(1), Pow(10, 4)), Sym("decipa"))  # nopep8
    CONVERSIONS[UnitsPressure.DECIPASCAL][UnitsPressure.MEGABAR] = \
        Mul(Rat(Int(1), Pow(10, 12)), Sym("decipa"))  # nopep8
    CONVERSIONS[UnitsPressure.DECIPASCAL][UnitsPressure.MEGAPASCAL] = \
        Mul(Rat(Int(1), Pow(10, 7)), Sym("decipa"))  # nopep8
    CONVERSIONS[UnitsPressure.DECIPASCAL][UnitsPressure.MICROPASCAL] = \
        Mul(Pow(10, 5), Sym("decipa"))  # nopep8
    CONVERSIONS[UnitsPressure.DECIPASCAL][UnitsPressure.MILLIBAR] = \
        Mul(Rat(Int(1), Int(1000)), Sym("decipa"))  # nopep8
    CONVERSIONS[UnitsPressure.DECIPASCAL][UnitsPressure.MILLIPASCAL] = \
        Mul(Int(100), Sym("decipa"))  # nopep8
    CONVERSIONS[UnitsPressure.DECIPASCAL][UnitsPressure.MILLITORR] = \
        Mul(Rat(Int(19), Int(25331250)), Sym("decipa"))  # nopep8
    CONVERSIONS[UnitsPressure.DECIPASCAL][UnitsPressure.MMHG] = \
        Mul(Rat(Mul(Int(2), Pow(10, 7)), Int("26664477483")), Sym("decipa"))  # nopep8
    CONVERSIONS[UnitsPressure.DECIPASCAL][UnitsPressure.NANOPASCAL] = \
        Mul(Pow(10, 8), Sym("decipa"))  # nopep8
    CONVERSIONS[UnitsPressure.DECIPASCAL][UnitsPressure.PETAPASCAL] = \
        Mul(Rat(Int(1), Pow(10, 16)), Sym("decipa"))  # nopep8
    CONVERSIONS[UnitsPressure.DECIPASCAL][UnitsPressure.PICOPASCAL] = \
        Mul(Pow(10, 11), Sym("decipa"))  # nopep8
    CONVERSIONS[UnitsPressure.DECIPASCAL][UnitsPressure.PSI] = \
        Mul(Rat(Mul(Int(25), Pow(10, 8)), Int("172368932329209")), Sym("decipa"))  # nopep8
    CONVERSIONS[UnitsPressure.DECIPASCAL][UnitsPressure.Pascal] = \
        Mul(Rat(Int(1), Int(10)), Sym("decipa"))  # nopep8
    CONVERSIONS[UnitsPressure.DECIPASCAL][UnitsPressure.TERAPASCAL] = \
        Mul(Rat(Int(1), Pow(10, 13)), Sym("decipa"))  # nopep8
    CONVERSIONS[UnitsPressure.DECIPASCAL][UnitsPressure.TORR] = \
        Mul(Rat(Int(76), Int(101325)), Sym("decipa"))  # nopep8
    CONVERSIONS[UnitsPressure.DECIPASCAL][UnitsPressure.YOCTOPASCAL] = \
        Mul(Pow(10, 23), Sym("decipa"))  # nopep8
    CONVERSIONS[UnitsPressure.DECIPASCAL][UnitsPressure.YOTTAPASCAL] = \
        Mul(Rat(Int(1), Pow(10, 25)), Sym("decipa"))  # nopep8
    CONVERSIONS[UnitsPressure.DECIPASCAL][UnitsPressure.ZEPTOPASCAL] = \
        Mul(Pow(10, 20), Sym("decipa"))  # nopep8
    CONVERSIONS[UnitsPressure.DECIPASCAL][UnitsPressure.ZETTAPASCAL] = \
        Mul(Rat(Int(1), Pow(10, 22)), Sym("decipa"))  # nopep8
    CONVERSIONS[UnitsPressure.EXAPASCAL][UnitsPressure.ATMOSPHERE] = \
        Mul(Rat(Mul(Int(4), Pow(10, 16)), Int(4053)), Sym("exapa"))  # nopep8
    CONVERSIONS[UnitsPressure.EXAPASCAL][UnitsPressure.ATTOPASCAL] = \
        Mul(Pow(10, 36), Sym("exapa"))  # nopep8
    CONVERSIONS[UnitsPressure.EXAPASCAL][UnitsPressure.BAR] = \
        Mul(Pow(10, 13), Sym("exapa"))  # nopep8
    CONVERSIONS[UnitsPressure.EXAPASCAL][UnitsPressure.CENTIBAR] = \
        Mul(Pow(10, 15), Sym("exapa"))  # nopep8
    CONVERSIONS[UnitsPressure.EXAPASCAL][UnitsPressure.CENTIPASCAL] = \
        Mul(Pow(10, 20), Sym("exapa"))  # nopep8
    CONVERSIONS[UnitsPressure.EXAPASCAL][UnitsPressure.DECAPASCAL] = \
        Mul(Pow(10, 17), Sym("exapa"))  # nopep8
    CONVERSIONS[UnitsPressure.EXAPASCAL][UnitsPressure.DECIBAR] = \
        Mul(Pow(10, 14), Sym("exapa"))  # nopep8
    CONVERSIONS[UnitsPressure.EXAPASCAL][UnitsPressure.DECIPASCAL] = \
        Mul(Pow(10, 19), Sym("exapa"))  # nopep8
    CONVERSIONS[UnitsPressure.EXAPASCAL][UnitsPressure.FEMTOPASCAL] = \
        Mul(Pow(10, 33), Sym("exapa"))  # nopep8
    CONVERSIONS[UnitsPressure.EXAPASCAL][UnitsPressure.GIGAPASCAL] = \
        Mul(Pow(10, 9), Sym("exapa"))  # nopep8
    CONVERSIONS[UnitsPressure.EXAPASCAL][UnitsPressure.HECTOPASCAL] = \
        Mul(Pow(10, 16), Sym("exapa"))  # nopep8
    CONVERSIONS[UnitsPressure.EXAPASCAL][UnitsPressure.KILOBAR] = \
        Mul(Pow(10, 10), Sym("exapa"))  # nopep8
    CONVERSIONS[UnitsPressure.EXAPASCAL][UnitsPressure.KILOPASCAL] = \
        Mul(Pow(10, 15), Sym("exapa"))  # nopep8
    CONVERSIONS[UnitsPressure.EXAPASCAL][UnitsPressure.MEGABAR] = \
        Mul(Pow(10, 7), Sym("exapa"))  # nopep8
    CONVERSIONS[UnitsPressure.EXAPASCAL][UnitsPressure.MEGAPASCAL] = \
        Mul(Pow(10, 12), Sym("exapa"))  # nopep8
    CONVERSIONS[UnitsPressure.EXAPASCAL][UnitsPressure.MICROPASCAL] = \
        Mul(Pow(10, 24), Sym("exapa"))  # nopep8
    CONVERSIONS[UnitsPressure.EXAPASCAL][UnitsPressure.MILLIBAR] = \
        Mul(Pow(10, 16), Sym("exapa"))  # nopep8
    CONVERSIONS[UnitsPressure.EXAPASCAL][UnitsPressure.MILLIPASCAL] = \
        Mul(Pow(10, 21), Sym("exapa"))  # nopep8
    CONVERSIONS[UnitsPressure.EXAPASCAL][UnitsPressure.MILLITORR] = \
        Mul(Rat(Mul(Int(304), Pow(10, 14)), Int(4053)), Sym("exapa"))  # nopep8
    CONVERSIONS[UnitsPressure.EXAPASCAL][UnitsPressure.MMHG] = \
        Mul(Rat(Mul(Int(2), Pow(10, 26)), Int("26664477483")), Sym("exapa"))  # nopep8
    CONVERSIONS[UnitsPressure.EXAPASCAL][UnitsPressure.NANOPASCAL] = \
        Mul(Pow(10, 27), Sym("exapa"))  # nopep8
    CONVERSIONS[UnitsPressure.EXAPASCAL][UnitsPressure.PETAPASCAL] = \
        Mul(Int(1000), Sym("exapa"))  # nopep8
    CONVERSIONS[UnitsPressure.EXAPASCAL][UnitsPressure.PICOPASCAL] = \
        Mul(Pow(10, 30), Sym("exapa"))  # nopep8
    CONVERSIONS[UnitsPressure.EXAPASCAL][UnitsPressure.PSI] = \
        Mul(Rat(Mul(Int(25), Pow(10, 27)), Int("172368932329209")), Sym("exapa"))  # nopep8
    CONVERSIONS[UnitsPressure.EXAPASCAL][UnitsPressure.Pascal] = \
        Mul(Pow(10, 18), Sym("exapa"))  # nopep8
    CONVERSIONS[UnitsPressure.EXAPASCAL][UnitsPressure.TERAPASCAL] = \
        Mul(Pow(10, 6), Sym("exapa"))  # nopep8
    CONVERSIONS[UnitsPressure.EXAPASCAL][UnitsPressure.TORR] = \
        Mul(Rat(Mul(Int(304), Pow(10, 17)), Int(4053)), Sym("exapa"))  # nopep8
    CONVERSIONS[UnitsPressure.EXAPASCAL][UnitsPressure.YOCTOPASCAL] = \
        Mul(Pow(10, 42), Sym("exapa"))  # nopep8
    CONVERSIONS[UnitsPressure.EXAPASCAL][UnitsPressure.YOTTAPASCAL] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("exapa"))  # nopep8
    CONVERSIONS[UnitsPressure.EXAPASCAL][UnitsPressure.ZEPTOPASCAL] = \
        Mul(Pow(10, 39), Sym("exapa"))  # nopep8
    CONVERSIONS[UnitsPressure.EXAPASCAL][UnitsPressure.ZETTAPASCAL] = \
        Mul(Rat(Int(1), Int(1000)), Sym("exapa"))  # nopep8
    CONVERSIONS[UnitsPressure.FEMTOPASCAL][UnitsPressure.ATMOSPHERE] = \
        Mul(Rat(Int(1), Mul(Int(101325), Pow(10, 15))), Sym("femtopa"))  # nopep8
    CONVERSIONS[UnitsPressure.FEMTOPASCAL][UnitsPressure.ATTOPASCAL] = \
        Mul(Int(1000), Sym("femtopa"))  # nopep8
    CONVERSIONS[UnitsPressure.FEMTOPASCAL][UnitsPressure.BAR] = \
        Mul(Rat(Int(1), Pow(10, 20)), Sym("femtopa"))  # nopep8
    CONVERSIONS[UnitsPressure.FEMTOPASCAL][UnitsPressure.CENTIBAR] = \
        Mul(Rat(Int(1), Pow(10, 18)), Sym("femtopa"))  # nopep8
    CONVERSIONS[UnitsPressure.FEMTOPASCAL][UnitsPressure.CENTIPASCAL] = \
        Mul(Rat(Int(1), Pow(10, 13)), Sym("femtopa"))  # nopep8
    CONVERSIONS[UnitsPressure.FEMTOPASCAL][UnitsPressure.DECAPASCAL] = \
        Mul(Rat(Int(1), Pow(10, 16)), Sym("femtopa"))  # nopep8
    CONVERSIONS[UnitsPressure.FEMTOPASCAL][UnitsPressure.DECIBAR] = \
        Mul(Rat(Int(1), Pow(10, 19)), Sym("femtopa"))  # nopep8
    CONVERSIONS[UnitsPressure.FEMTOPASCAL][UnitsPressure.DECIPASCAL] = \
        Mul(Rat(Int(1), Pow(10, 14)), Sym("femtopa"))  # nopep8
    CONVERSIONS[UnitsPressure.FEMTOPASCAL][UnitsPressure.EXAPASCAL] = \
        Mul(Rat(Int(1), Pow(10, 33)), Sym("femtopa"))  # nopep8
    CONVERSIONS[UnitsPressure.FEMTOPASCAL][UnitsPressure.GIGAPASCAL] = \
        Mul(Rat(Int(1), Pow(10, 24)), Sym("femtopa"))  # nopep8
    CONVERSIONS[UnitsPressure.FEMTOPASCAL][UnitsPressure.HECTOPASCAL] = \
        Mul(Rat(Int(1), Pow(10, 17)), Sym("femtopa"))  # nopep8
    CONVERSIONS[UnitsPressure.FEMTOPASCAL][UnitsPressure.KILOBAR] = \
        Mul(Rat(Int(1), Pow(10, 23)), Sym("femtopa"))  # nopep8
    CONVERSIONS[UnitsPressure.FEMTOPASCAL][UnitsPressure.KILOPASCAL] = \
        Mul(Rat(Int(1), Pow(10, 18)), Sym("femtopa"))  # nopep8
    CONVERSIONS[UnitsPressure.FEMTOPASCAL][UnitsPressure.MEGABAR] = \
        Mul(Rat(Int(1), Pow(10, 26)), Sym("femtopa"))  # nopep8
    CONVERSIONS[UnitsPressure.FEMTOPASCAL][UnitsPressure.MEGAPASCAL] = \
        Mul(Rat(Int(1), Pow(10, 21)), Sym("femtopa"))  # nopep8
    CONVERSIONS[UnitsPressure.FEMTOPASCAL][UnitsPressure.MICROPASCAL] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("femtopa"))  # nopep8
    CONVERSIONS[UnitsPressure.FEMTOPASCAL][UnitsPressure.MILLIBAR] = \
        Mul(Rat(Int(1), Pow(10, 17)), Sym("femtopa"))  # nopep8
    CONVERSIONS[UnitsPressure.FEMTOPASCAL][UnitsPressure.MILLIPASCAL] = \
        Mul(Rat(Int(1), Pow(10, 12)), Sym("femtopa"))  # nopep8
    CONVERSIONS[UnitsPressure.FEMTOPASCAL][UnitsPressure.MILLITORR] = \
        Mul(Rat(Int(19), Mul(Int(2533125), Pow(10, 15))), Sym("femtopa"))  # nopep8
    CONVERSIONS[UnitsPressure.FEMTOPASCAL][UnitsPressure.MMHG] = \
        Mul(Rat(Int(1), Mul(Int("133322387415"), Pow(10, 6))), Sym("femtopa"))  # nopep8
    CONVERSIONS[UnitsPressure.FEMTOPASCAL][UnitsPressure.NANOPASCAL] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("femtopa"))  # nopep8
    CONVERSIONS[UnitsPressure.FEMTOPASCAL][UnitsPressure.PETAPASCAL] = \
        Mul(Rat(Int(1), Pow(10, 30)), Sym("femtopa"))  # nopep8
    CONVERSIONS[UnitsPressure.FEMTOPASCAL][UnitsPressure.PICOPASCAL] = \
        Mul(Rat(Int(1), Int(1000)), Sym("femtopa"))  # nopep8
    CONVERSIONS[UnitsPressure.FEMTOPASCAL][UnitsPressure.PSI] = \
        Mul(Rat(Int(1), Mul(Int("689475729316836"), Pow(10, 4))), Sym("femtopa"))  # nopep8
    CONVERSIONS[UnitsPressure.FEMTOPASCAL][UnitsPressure.Pascal] = \
        Mul(Rat(Int(1), Pow(10, 15)), Sym("femtopa"))  # nopep8
    CONVERSIONS[UnitsPressure.FEMTOPASCAL][UnitsPressure.TERAPASCAL] = \
        Mul(Rat(Int(1), Pow(10, 27)), Sym("femtopa"))  # nopep8
    CONVERSIONS[UnitsPressure.FEMTOPASCAL][UnitsPressure.TORR] = \
        Mul(Rat(Int(19), Mul(Int(2533125), Pow(10, 12))), Sym("femtopa"))  # nopep8
    CONVERSIONS[UnitsPressure.FEMTOPASCAL][UnitsPressure.YOCTOPASCAL] = \
        Mul(Pow(10, 9), Sym("femtopa"))  # nopep8
    CONVERSIONS[UnitsPressure.FEMTOPASCAL][UnitsPressure.YOTTAPASCAL] = \
        Mul(Rat(Int(1), Pow(10, 39)), Sym("femtopa"))  # nopep8
    CONVERSIONS[UnitsPressure.FEMTOPASCAL][UnitsPressure.ZEPTOPASCAL] = \
        Mul(Pow(10, 6), Sym("femtopa"))  # nopep8
    CONVERSIONS[UnitsPressure.FEMTOPASCAL][UnitsPressure.ZETTAPASCAL] = \
        Mul(Rat(Int(1), Pow(10, 36)), Sym("femtopa"))  # nopep8
    CONVERSIONS[UnitsPressure.GIGAPASCAL][UnitsPressure.ATMOSPHERE] = \
        Mul(Rat(Mul(Int(4), Pow(10, 7)), Int(4053)), Sym("gigapa"))  # nopep8
    CONVERSIONS[UnitsPressure.GIGAPASCAL][UnitsPressure.ATTOPASCAL] = \
        Mul(Pow(10, 27), Sym("gigapa"))  # nopep8
    CONVERSIONS[UnitsPressure.GIGAPASCAL][UnitsPressure.BAR] = \
        Mul(Pow(10, 4), Sym("gigapa"))  # nopep8
    CONVERSIONS[UnitsPressure.GIGAPASCAL][UnitsPressure.CENTIBAR] = \
        Mul(Pow(10, 6), Sym("gigapa"))  # nopep8
    CONVERSIONS[UnitsPressure.GIGAPASCAL][UnitsPressure.CENTIPASCAL] = \
        Mul(Pow(10, 11), Sym("gigapa"))  # nopep8
    CONVERSIONS[UnitsPressure.GIGAPASCAL][UnitsPressure.DECAPASCAL] = \
        Mul(Pow(10, 8), Sym("gigapa"))  # nopep8
    CONVERSIONS[UnitsPressure.GIGAPASCAL][UnitsPressure.DECIBAR] = \
        Mul(Pow(10, 5), Sym("gigapa"))  # nopep8
    CONVERSIONS[UnitsPressure.GIGAPASCAL][UnitsPressure.DECIPASCAL] = \
        Mul(Pow(10, 10), Sym("gigapa"))  # nopep8
    CONVERSIONS[UnitsPressure.GIGAPASCAL][UnitsPressure.EXAPASCAL] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("gigapa"))  # nopep8
    CONVERSIONS[UnitsPressure.GIGAPASCAL][UnitsPressure.FEMTOPASCAL] = \
        Mul(Pow(10, 24), Sym("gigapa"))  # nopep8
    CONVERSIONS[UnitsPressure.GIGAPASCAL][UnitsPressure.HECTOPASCAL] = \
        Mul(Pow(10, 7), Sym("gigapa"))  # nopep8
    CONVERSIONS[UnitsPressure.GIGAPASCAL][UnitsPressure.KILOBAR] = \
        Mul(Int(10), Sym("gigapa"))  # nopep8
    CONVERSIONS[UnitsPressure.GIGAPASCAL][UnitsPressure.KILOPASCAL] = \
        Mul(Pow(10, 6), Sym("gigapa"))  # nopep8
    CONVERSIONS[UnitsPressure.GIGAPASCAL][UnitsPressure.MEGABAR] = \
        Mul(Rat(Int(1), Int(100)), Sym("gigapa"))  # nopep8
    CONVERSIONS[UnitsPressure.GIGAPASCAL][UnitsPressure.MEGAPASCAL] = \
        Mul(Int(1000), Sym("gigapa"))  # nopep8
    CONVERSIONS[UnitsPressure.GIGAPASCAL][UnitsPressure.MICROPASCAL] = \
        Mul(Pow(10, 15), Sym("gigapa"))  # nopep8
    CONVERSIONS[UnitsPressure.GIGAPASCAL][UnitsPressure.MILLIBAR] = \
        Mul(Pow(10, 7), Sym("gigapa"))  # nopep8
    CONVERSIONS[UnitsPressure.GIGAPASCAL][UnitsPressure.MILLIPASCAL] = \
        Mul(Pow(10, 12), Sym("gigapa"))  # nopep8
    CONVERSIONS[UnitsPressure.GIGAPASCAL][UnitsPressure.MILLITORR] = \
        Mul(Rat(Mul(Int(304), Pow(10, 5)), Int(4053)), Sym("gigapa"))  # nopep8
    CONVERSIONS[UnitsPressure.GIGAPASCAL][UnitsPressure.MMHG] = \
        Mul(Rat(Mul(Int(2), Pow(10, 17)), Int("26664477483")), Sym("gigapa"))  # nopep8
    CONVERSIONS[UnitsPressure.GIGAPASCAL][UnitsPressure.NANOPASCAL] = \
        Mul(Pow(10, 18), Sym("gigapa"))  # nopep8
    CONVERSIONS[UnitsPressure.GIGAPASCAL][UnitsPressure.PETAPASCAL] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("gigapa"))  # nopep8
    CONVERSIONS[UnitsPressure.GIGAPASCAL][UnitsPressure.PICOPASCAL] = \
        Mul(Pow(10, 21), Sym("gigapa"))  # nopep8
    CONVERSIONS[UnitsPressure.GIGAPASCAL][UnitsPressure.PSI] = \
        Mul(Rat(Mul(Int(25), Pow(10, 18)), Int("172368932329209")), Sym("gigapa"))  # nopep8
    CONVERSIONS[UnitsPressure.GIGAPASCAL][UnitsPressure.Pascal] = \
        Mul(Pow(10, 9), Sym("gigapa"))  # nopep8
    CONVERSIONS[UnitsPressure.GIGAPASCAL][UnitsPressure.TERAPASCAL] = \
        Mul(Rat(Int(1), Int(1000)), Sym("gigapa"))  # nopep8
    CONVERSIONS[UnitsPressure.GIGAPASCAL][UnitsPressure.TORR] = \
        Mul(Rat(Mul(Int(304), Pow(10, 8)), Int(4053)), Sym("gigapa"))  # nopep8
    CONVERSIONS[UnitsPressure.GIGAPASCAL][UnitsPressure.YOCTOPASCAL] = \
        Mul(Pow(10, 33), Sym("gigapa"))  # nopep8
    CONVERSIONS[UnitsPressure.GIGAPASCAL][UnitsPressure.YOTTAPASCAL] = \
        Mul(Rat(Int(1), Pow(10, 15)), Sym("gigapa"))  # nopep8
    CONVERSIONS[UnitsPressure.GIGAPASCAL][UnitsPressure.ZEPTOPASCAL] = \
        Mul(Pow(10, 30), Sym("gigapa"))  # nopep8
    CONVERSIONS[UnitsPressure.GIGAPASCAL][UnitsPressure.ZETTAPASCAL] = \
        Mul(Rat(Int(1), Pow(10, 12)), Sym("gigapa"))  # nopep8
    CONVERSIONS[UnitsPressure.HECTOPASCAL][UnitsPressure.ATMOSPHERE] = \
        Mul(Rat(Int(4), Int(4053)), Sym("hectopa"))  # nopep8
    CONVERSIONS[UnitsPressure.HECTOPASCAL][UnitsPressure.ATTOPASCAL] = \
        Mul(Pow(10, 20), Sym("hectopa"))  # nopep8
    CONVERSIONS[UnitsPressure.HECTOPASCAL][UnitsPressure.BAR] = \
        Mul(Rat(Int(1), Int(1000)), Sym("hectopa"))  # nopep8
    CONVERSIONS[UnitsPressure.HECTOPASCAL][UnitsPressure.CENTIBAR] = \
        Mul(Rat(Int(1), Int(10)), Sym("hectopa"))  # nopep8
    CONVERSIONS[UnitsPressure.HECTOPASCAL][UnitsPressure.CENTIPASCAL] = \
        Mul(Pow(10, 4), Sym("hectopa"))  # nopep8
    CONVERSIONS[UnitsPressure.HECTOPASCAL][UnitsPressure.DECAPASCAL] = \
        Mul(Int(10), Sym("hectopa"))  # nopep8
    CONVERSIONS[UnitsPressure.HECTOPASCAL][UnitsPressure.DECIBAR] = \
        Mul(Rat(Int(1), Int(100)), Sym("hectopa"))  # nopep8
    CONVERSIONS[UnitsPressure.HECTOPASCAL][UnitsPressure.DECIPASCAL] = \
        Mul(Int(1000), Sym("hectopa"))  # nopep8
    CONVERSIONS[UnitsPressure.HECTOPASCAL][UnitsPressure.EXAPASCAL] = \
        Mul(Rat(Int(1), Pow(10, 16)), Sym("hectopa"))  # nopep8
    CONVERSIONS[UnitsPressure.HECTOPASCAL][UnitsPressure.FEMTOPASCAL] = \
        Mul(Pow(10, 17), Sym("hectopa"))  # nopep8
    CONVERSIONS[UnitsPressure.HECTOPASCAL][UnitsPressure.GIGAPASCAL] = \
        Mul(Rat(Int(1), Pow(10, 7)), Sym("hectopa"))  # nopep8
    CONVERSIONS[UnitsPressure.HECTOPASCAL][UnitsPressure.KILOBAR] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("hectopa"))  # nopep8
    CONVERSIONS[UnitsPressure.HECTOPASCAL][UnitsPressure.KILOPASCAL] = \
        Mul(Rat(Int(1), Int(10)), Sym("hectopa"))  # nopep8
    CONVERSIONS[UnitsPressure.HECTOPASCAL][UnitsPressure.MEGABAR] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("hectopa"))  # nopep8
    CONVERSIONS[UnitsPressure.HECTOPASCAL][UnitsPressure.MEGAPASCAL] = \
        Mul(Rat(Int(1), Pow(10, 4)), Sym("hectopa"))  # nopep8
    CONVERSIONS[UnitsPressure.HECTOPASCAL][UnitsPressure.MICROPASCAL] = \
        Mul(Pow(10, 8), Sym("hectopa"))  # nopep8
    CONVERSIONS[UnitsPressure.HECTOPASCAL][UnitsPressure.MILLIBAR] = \
        Sym("hectopa")  # nopep8
    CONVERSIONS[UnitsPressure.HECTOPASCAL][UnitsPressure.MILLIPASCAL] = \
        Mul(Pow(10, 5), Sym("hectopa"))  # nopep8
    CONVERSIONS[UnitsPressure.HECTOPASCAL][UnitsPressure.MILLITORR] = \
        Mul(Rat(Int(76), Int(101325)), Sym("hectopa"))  # nopep8
    CONVERSIONS[UnitsPressure.HECTOPASCAL][UnitsPressure.MMHG] = \
        Mul(Rat(Mul(Int(2), Pow(10, 10)), Int("26664477483")), Sym("hectopa"))  # nopep8
    CONVERSIONS[UnitsPressure.HECTOPASCAL][UnitsPressure.NANOPASCAL] = \
        Mul(Pow(10, 11), Sym("hectopa"))  # nopep8
    CONVERSIONS[UnitsPressure.HECTOPASCAL][UnitsPressure.PETAPASCAL] = \
        Mul(Rat(Int(1), Pow(10, 13)), Sym("hectopa"))  # nopep8
    CONVERSIONS[UnitsPressure.HECTOPASCAL][UnitsPressure.PICOPASCAL] = \
        Mul(Pow(10, 14), Sym("hectopa"))  # nopep8
    CONVERSIONS[UnitsPressure.HECTOPASCAL][UnitsPressure.PSI] = \
        Mul(Rat(Mul(Int(25), Pow(10, 11)), Int("172368932329209")), Sym("hectopa"))  # nopep8
    CONVERSIONS[UnitsPressure.HECTOPASCAL][UnitsPressure.Pascal] = \
        Mul(Int(100), Sym("hectopa"))  # nopep8
    CONVERSIONS[UnitsPressure.HECTOPASCAL][UnitsPressure.TERAPASCAL] = \
        Mul(Rat(Int(1), Pow(10, 10)), Sym("hectopa"))  # nopep8
    CONVERSIONS[UnitsPressure.HECTOPASCAL][UnitsPressure.TORR] = \
        Mul(Rat(Int(3040), Int(4053)), Sym("hectopa"))  # nopep8
    CONVERSIONS[UnitsPressure.HECTOPASCAL][UnitsPressure.YOCTOPASCAL] = \
        Mul(Pow(10, 26), Sym("hectopa"))  # nopep8
    CONVERSIONS[UnitsPressure.HECTOPASCAL][UnitsPressure.YOTTAPASCAL] = \
        Mul(Rat(Int(1), Pow(10, 22)), Sym("hectopa"))  # nopep8
    CONVERSIONS[UnitsPressure.HECTOPASCAL][UnitsPressure.ZEPTOPASCAL] = \
        Mul(Pow(10, 23), Sym("hectopa"))  # nopep8
    CONVERSIONS[UnitsPressure.HECTOPASCAL][UnitsPressure.ZETTAPASCAL] = \
        Mul(Rat(Int(1), Pow(10, 19)), Sym("hectopa"))  # nopep8
    CONVERSIONS[UnitsPressure.KILOBAR][UnitsPressure.ATMOSPHERE] = \
        Mul(Rat(Mul(Int(4), Pow(10, 6)), Int(4053)), Sym("kbar"))  # nopep8
    CONVERSIONS[UnitsPressure.KILOBAR][UnitsPressure.ATTOPASCAL] = \
        Mul(Pow(10, 26), Sym("kbar"))  # nopep8
    CONVERSIONS[UnitsPressure.KILOBAR][UnitsPressure.BAR] = \
        Mul(Int(1000), Sym("kbar"))  # nopep8
    CONVERSIONS[UnitsPressure.KILOBAR][UnitsPressure.CENTIBAR] = \
        Mul(Pow(10, 5), Sym("kbar"))  # nopep8
    CONVERSIONS[UnitsPressure.KILOBAR][UnitsPressure.CENTIPASCAL] = \
        Mul(Pow(10, 10), Sym("kbar"))  # nopep8
    CONVERSIONS[UnitsPressure.KILOBAR][UnitsPressure.DECAPASCAL] = \
        Mul(Pow(10, 7), Sym("kbar"))  # nopep8
    CONVERSIONS[UnitsPressure.KILOBAR][UnitsPressure.DECIBAR] = \
        Mul(Pow(10, 4), Sym("kbar"))  # nopep8
    CONVERSIONS[UnitsPressure.KILOBAR][UnitsPressure.DECIPASCAL] = \
        Mul(Pow(10, 9), Sym("kbar"))  # nopep8
    CONVERSIONS[UnitsPressure.KILOBAR][UnitsPressure.EXAPASCAL] = \
        Mul(Rat(Int(1), Pow(10, 10)), Sym("kbar"))  # nopep8
    CONVERSIONS[UnitsPressure.KILOBAR][UnitsPressure.FEMTOPASCAL] = \
        Mul(Pow(10, 23), Sym("kbar"))  # nopep8
    CONVERSIONS[UnitsPressure.KILOBAR][UnitsPressure.GIGAPASCAL] = \
        Mul(Rat(Int(1), Int(10)), Sym("kbar"))  # nopep8
    CONVERSIONS[UnitsPressure.KILOBAR][UnitsPressure.HECTOPASCAL] = \
        Mul(Pow(10, 6), Sym("kbar"))  # nopep8
    CONVERSIONS[UnitsPressure.KILOBAR][UnitsPressure.KILOPASCAL] = \
        Mul(Pow(10, 5), Sym("kbar"))  # nopep8
    CONVERSIONS[UnitsPressure.KILOBAR][UnitsPressure.MEGABAR] = \
        Mul(Rat(Int(1), Int(1000)), Sym("kbar"))  # nopep8
    CONVERSIONS[UnitsPressure.KILOBAR][UnitsPressure.MEGAPASCAL] = \
        Mul(Int(100), Sym("kbar"))  # nopep8
    CONVERSIONS[UnitsPressure.KILOBAR][UnitsPressure.MICROPASCAL] = \
        Mul(Pow(10, 14), Sym("kbar"))  # nopep8
    CONVERSIONS[UnitsPressure.KILOBAR][UnitsPressure.MILLIBAR] = \
        Mul(Pow(10, 6), Sym("kbar"))  # nopep8
    CONVERSIONS[UnitsPressure.KILOBAR][UnitsPressure.MILLIPASCAL] = \
        Mul(Pow(10, 11), Sym("kbar"))  # nopep8
    CONVERSIONS[UnitsPressure.KILOBAR][UnitsPressure.MILLITORR] = \
        Mul(Rat(Mul(Int(304), Pow(10, 4)), Int(4053)), Sym("kbar"))  # nopep8
    CONVERSIONS[UnitsPressure.KILOBAR][UnitsPressure.MMHG] = \
        Mul(Rat(Mul(Int(2), Pow(10, 16)), Int("26664477483")), Sym("kbar"))  # nopep8
    CONVERSIONS[UnitsPressure.KILOBAR][UnitsPressure.NANOPASCAL] = \
        Mul(Pow(10, 17), Sym("kbar"))  # nopep8
    CONVERSIONS[UnitsPressure.KILOBAR][UnitsPressure.PETAPASCAL] = \
        Mul(Rat(Int(1), Pow(10, 7)), Sym("kbar"))  # nopep8
    CONVERSIONS[UnitsPressure.KILOBAR][UnitsPressure.PICOPASCAL] = \
        Mul(Pow(10, 20), Sym("kbar"))  # nopep8
    CONVERSIONS[UnitsPressure.KILOBAR][UnitsPressure.PSI] = \
        Mul(Rat(Mul(Int(25), Pow(10, 17)), Int("172368932329209")), Sym("kbar"))  # nopep8
    CONVERSIONS[UnitsPressure.KILOBAR][UnitsPressure.Pascal] = \
        Mul(Pow(10, 8), Sym("kbar"))  # nopep8
    CONVERSIONS[UnitsPressure.KILOBAR][UnitsPressure.TERAPASCAL] = \
        Mul(Rat(Int(1), Pow(10, 4)), Sym("kbar"))  # nopep8
    CONVERSIONS[UnitsPressure.KILOBAR][UnitsPressure.TORR] = \
        Mul(Rat(Mul(Int(304), Pow(10, 7)), Int(4053)), Sym("kbar"))  # nopep8
    CONVERSIONS[UnitsPressure.KILOBAR][UnitsPressure.YOCTOPASCAL] = \
        Mul(Pow(10, 32), Sym("kbar"))  # nopep8
    CONVERSIONS[UnitsPressure.KILOBAR][UnitsPressure.YOTTAPASCAL] = \
        Mul(Rat(Int(1), Pow(10, 16)), Sym("kbar"))  # nopep8
    CONVERSIONS[UnitsPressure.KILOBAR][UnitsPressure.ZEPTOPASCAL] = \
        Mul(Pow(10, 29), Sym("kbar"))  # nopep8
    CONVERSIONS[UnitsPressure.KILOBAR][UnitsPressure.ZETTAPASCAL] = \
        Mul(Rat(Int(1), Pow(10, 13)), Sym("kbar"))  # nopep8
    CONVERSIONS[UnitsPressure.KILOPASCAL][UnitsPressure.ATMOSPHERE] = \
        Mul(Rat(Int(40), Int(4053)), Sym("kilopa"))  # nopep8
    CONVERSIONS[UnitsPressure.KILOPASCAL][UnitsPressure.ATTOPASCAL] = \
        Mul(Pow(10, 21), Sym("kilopa"))  # nopep8
    CONVERSIONS[UnitsPressure.KILOPASCAL][UnitsPressure.BAR] = \
        Mul(Rat(Int(1), Int(100)), Sym("kilopa"))  # nopep8
    CONVERSIONS[UnitsPressure.KILOPASCAL][UnitsPressure.CENTIBAR] = \
        Sym("kilopa")  # nopep8
    CONVERSIONS[UnitsPressure.KILOPASCAL][UnitsPressure.CENTIPASCAL] = \
        Mul(Pow(10, 5), Sym("kilopa"))  # nopep8
    CONVERSIONS[UnitsPressure.KILOPASCAL][UnitsPressure.DECAPASCAL] = \
        Mul(Int(100), Sym("kilopa"))  # nopep8
    CONVERSIONS[UnitsPressure.KILOPASCAL][UnitsPressure.DECIBAR] = \
        Mul(Rat(Int(1), Int(10)), Sym("kilopa"))  # nopep8
    CONVERSIONS[UnitsPressure.KILOPASCAL][UnitsPressure.DECIPASCAL] = \
        Mul(Pow(10, 4), Sym("kilopa"))  # nopep8
    CONVERSIONS[UnitsPressure.KILOPASCAL][UnitsPressure.EXAPASCAL] = \
        Mul(Rat(Int(1), Pow(10, 15)), Sym("kilopa"))  # nopep8
    CONVERSIONS[UnitsPressure.KILOPASCAL][UnitsPressure.FEMTOPASCAL] = \
        Mul(Pow(10, 18), Sym("kilopa"))  # nopep8
    CONVERSIONS[UnitsPressure.KILOPASCAL][UnitsPressure.GIGAPASCAL] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("kilopa"))  # nopep8
    CONVERSIONS[UnitsPressure.KILOPASCAL][UnitsPressure.HECTOPASCAL] = \
        Mul(Int(10), Sym("kilopa"))  # nopep8
    CONVERSIONS[UnitsPressure.KILOPASCAL][UnitsPressure.KILOBAR] = \
        Mul(Rat(Int(1), Pow(10, 5)), Sym("kilopa"))  # nopep8
    CONVERSIONS[UnitsPressure.KILOPASCAL][UnitsPressure.MEGABAR] = \
        Mul(Rat(Int(1), Pow(10, 8)), Sym("kilopa"))  # nopep8
    CONVERSIONS[UnitsPressure.KILOPASCAL][UnitsPressure.MEGAPASCAL] = \
        Mul(Rat(Int(1), Int(1000)), Sym("kilopa"))  # nopep8
    CONVERSIONS[UnitsPressure.KILOPASCAL][UnitsPressure.MICROPASCAL] = \
        Mul(Pow(10, 9), Sym("kilopa"))  # nopep8
    CONVERSIONS[UnitsPressure.KILOPASCAL][UnitsPressure.MILLIBAR] = \
        Mul(Int(10), Sym("kilopa"))  # nopep8
    CONVERSIONS[UnitsPressure.KILOPASCAL][UnitsPressure.MILLIPASCAL] = \
        Mul(Pow(10, 6), Sym("kilopa"))  # nopep8
    CONVERSIONS[UnitsPressure.KILOPASCAL][UnitsPressure.MILLITORR] = \
        Mul(Rat(Int(152), Int(20265)), Sym("kilopa"))  # nopep8
    CONVERSIONS[UnitsPressure.KILOPASCAL][UnitsPressure.MMHG] = \
        Mul(Rat(Mul(Int(2), Pow(10, 11)), Int("26664477483")), Sym("kilopa"))  # nopep8
    CONVERSIONS[UnitsPressure.KILOPASCAL][UnitsPressure.NANOPASCAL] = \
        Mul(Pow(10, 12), Sym("kilopa"))  # nopep8
    CONVERSIONS[UnitsPressure.KILOPASCAL][UnitsPressure.PETAPASCAL] = \
        Mul(Rat(Int(1), Pow(10, 12)), Sym("kilopa"))  # nopep8
    CONVERSIONS[UnitsPressure.KILOPASCAL][UnitsPressure.PICOPASCAL] = \
        Mul(Pow(10, 15), Sym("kilopa"))  # nopep8
    CONVERSIONS[UnitsPressure.KILOPASCAL][UnitsPressure.PSI] = \
        Mul(Rat(Mul(Int(25), Pow(10, 12)), Int("172368932329209")), Sym("kilopa"))  # nopep8
    CONVERSIONS[UnitsPressure.KILOPASCAL][UnitsPressure.Pascal] = \
        Mul(Int(1000), Sym("kilopa"))  # nopep8
    CONVERSIONS[UnitsPressure.KILOPASCAL][UnitsPressure.TERAPASCAL] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("kilopa"))  # nopep8
    CONVERSIONS[UnitsPressure.KILOPASCAL][UnitsPressure.TORR] = \
        Mul(Rat(Int(30400), Int(4053)), Sym("kilopa"))  # nopep8
    CONVERSIONS[UnitsPressure.KILOPASCAL][UnitsPressure.YOCTOPASCAL] = \
        Mul(Pow(10, 27), Sym("kilopa"))  # nopep8
    CONVERSIONS[UnitsPressure.KILOPASCAL][UnitsPressure.YOTTAPASCAL] = \
        Mul(Rat(Int(1), Pow(10, 21)), Sym("kilopa"))  # nopep8
    CONVERSIONS[UnitsPressure.KILOPASCAL][UnitsPressure.ZEPTOPASCAL] = \
        Mul(Pow(10, 24), Sym("kilopa"))  # nopep8
    CONVERSIONS[UnitsPressure.KILOPASCAL][UnitsPressure.ZETTAPASCAL] = \
        Mul(Rat(Int(1), Pow(10, 18)), Sym("kilopa"))  # nopep8
    CONVERSIONS[UnitsPressure.MEGABAR][UnitsPressure.ATMOSPHERE] = \
        Mul(Rat(Mul(Int(4), Pow(10, 9)), Int(4053)), Sym("megabar"))  # nopep8
    CONVERSIONS[UnitsPressure.MEGABAR][UnitsPressure.ATTOPASCAL] = \
        Mul(Pow(10, 29), Sym("megabar"))  # nopep8
    CONVERSIONS[UnitsPressure.MEGABAR][UnitsPressure.BAR] = \
        Mul(Pow(10, 6), Sym("megabar"))  # nopep8
    CONVERSIONS[UnitsPressure.MEGABAR][UnitsPressure.CENTIBAR] = \
        Mul(Pow(10, 8), Sym("megabar"))  # nopep8
    CONVERSIONS[UnitsPressure.MEGABAR][UnitsPressure.CENTIPASCAL] = \
        Mul(Pow(10, 13), Sym("megabar"))  # nopep8
    CONVERSIONS[UnitsPressure.MEGABAR][UnitsPressure.DECAPASCAL] = \
        Mul(Pow(10, 10), Sym("megabar"))  # nopep8
    CONVERSIONS[UnitsPressure.MEGABAR][UnitsPressure.DECIBAR] = \
        Mul(Pow(10, 7), Sym("megabar"))  # nopep8
    CONVERSIONS[UnitsPressure.MEGABAR][UnitsPressure.DECIPASCAL] = \
        Mul(Pow(10, 12), Sym("megabar"))  # nopep8
    CONVERSIONS[UnitsPressure.MEGABAR][UnitsPressure.EXAPASCAL] = \
        Mul(Rat(Int(1), Pow(10, 7)), Sym("megabar"))  # nopep8
    CONVERSIONS[UnitsPressure.MEGABAR][UnitsPressure.FEMTOPASCAL] = \
        Mul(Pow(10, 26), Sym("megabar"))  # nopep8
    CONVERSIONS[UnitsPressure.MEGABAR][UnitsPressure.GIGAPASCAL] = \
        Mul(Int(100), Sym("megabar"))  # nopep8
    CONVERSIONS[UnitsPressure.MEGABAR][UnitsPressure.HECTOPASCAL] = \
        Mul(Pow(10, 9), Sym("megabar"))  # nopep8
    CONVERSIONS[UnitsPressure.MEGABAR][UnitsPressure.KILOBAR] = \
        Mul(Int(1000), Sym("megabar"))  # nopep8
    CONVERSIONS[UnitsPressure.MEGABAR][UnitsPressure.KILOPASCAL] = \
        Mul(Pow(10, 8), Sym("megabar"))  # nopep8
    CONVERSIONS[UnitsPressure.MEGABAR][UnitsPressure.MEGAPASCAL] = \
        Mul(Pow(10, 5), Sym("megabar"))  # nopep8
    CONVERSIONS[UnitsPressure.MEGABAR][UnitsPressure.MICROPASCAL] = \
        Mul(Pow(10, 17), Sym("megabar"))  # nopep8
    CONVERSIONS[UnitsPressure.MEGABAR][UnitsPressure.MILLIBAR] = \
        Mul(Pow(10, 9), Sym("megabar"))  # nopep8
    CONVERSIONS[UnitsPressure.MEGABAR][UnitsPressure.MILLIPASCAL] = \
        Mul(Pow(10, 14), Sym("megabar"))  # nopep8
    CONVERSIONS[UnitsPressure.MEGABAR][UnitsPressure.MILLITORR] = \
        Mul(Rat(Mul(Int(304), Pow(10, 7)), Int(4053)), Sym("megabar"))  # nopep8
    CONVERSIONS[UnitsPressure.MEGABAR][UnitsPressure.MMHG] = \
        Mul(Rat(Mul(Int(2), Pow(10, 19)), Int("26664477483")), Sym("megabar"))  # nopep8
    CONVERSIONS[UnitsPressure.MEGABAR][UnitsPressure.NANOPASCAL] = \
        Mul(Pow(10, 20), Sym("megabar"))  # nopep8
    CONVERSIONS[UnitsPressure.MEGABAR][UnitsPressure.PETAPASCAL] = \
        Mul(Rat(Int(1), Pow(10, 4)), Sym("megabar"))  # nopep8
    CONVERSIONS[UnitsPressure.MEGABAR][UnitsPressure.PICOPASCAL] = \
        Mul(Pow(10, 23), Sym("megabar"))  # nopep8
    CONVERSIONS[UnitsPressure.MEGABAR][UnitsPressure.PSI] = \
        Mul(Rat(Mul(Int(25), Pow(10, 20)), Int("172368932329209")), Sym("megabar"))  # nopep8
    CONVERSIONS[UnitsPressure.MEGABAR][UnitsPressure.Pascal] = \
        Mul(Pow(10, 11), Sym("megabar"))  # nopep8
    CONVERSIONS[UnitsPressure.MEGABAR][UnitsPressure.TERAPASCAL] = \
        Mul(Rat(Int(1), Int(10)), Sym("megabar"))  # nopep8
    CONVERSIONS[UnitsPressure.MEGABAR][UnitsPressure.TORR] = \
        Mul(Rat(Mul(Int(304), Pow(10, 10)), Int(4053)), Sym("megabar"))  # nopep8
    CONVERSIONS[UnitsPressure.MEGABAR][UnitsPressure.YOCTOPASCAL] = \
        Mul(Pow(10, 35), Sym("megabar"))  # nopep8
    CONVERSIONS[UnitsPressure.MEGABAR][UnitsPressure.YOTTAPASCAL] = \
        Mul(Rat(Int(1), Pow(10, 13)), Sym("megabar"))  # nopep8
    CONVERSIONS[UnitsPressure.MEGABAR][UnitsPressure.ZEPTOPASCAL] = \
        Mul(Pow(10, 32), Sym("megabar"))  # nopep8
    CONVERSIONS[UnitsPressure.MEGABAR][UnitsPressure.ZETTAPASCAL] = \
        Mul(Rat(Int(1), Pow(10, 10)), Sym("megabar"))  # nopep8
    CONVERSIONS[UnitsPressure.MEGAPASCAL][UnitsPressure.ATMOSPHERE] = \
        Mul(Rat(Mul(Int(4), Pow(10, 4)), Int(4053)), Sym("megapa"))  # nopep8
    CONVERSIONS[UnitsPressure.MEGAPASCAL][UnitsPressure.ATTOPASCAL] = \
        Mul(Pow(10, 24), Sym("megapa"))  # nopep8
    CONVERSIONS[UnitsPressure.MEGAPASCAL][UnitsPressure.BAR] = \
        Mul(Int(10), Sym("megapa"))  # nopep8
    CONVERSIONS[UnitsPressure.MEGAPASCAL][UnitsPressure.CENTIBAR] = \
        Mul(Int(1000), Sym("megapa"))  # nopep8
    CONVERSIONS[UnitsPressure.MEGAPASCAL][UnitsPressure.CENTIPASCAL] = \
        Mul(Pow(10, 8), Sym("megapa"))  # nopep8
    CONVERSIONS[UnitsPressure.MEGAPASCAL][UnitsPressure.DECAPASCAL] = \
        Mul(Pow(10, 5), Sym("megapa"))  # nopep8
    CONVERSIONS[UnitsPressure.MEGAPASCAL][UnitsPressure.DECIBAR] = \
        Mul(Int(100), Sym("megapa"))  # nopep8
    CONVERSIONS[UnitsPressure.MEGAPASCAL][UnitsPressure.DECIPASCAL] = \
        Mul(Pow(10, 7), Sym("megapa"))  # nopep8
    CONVERSIONS[UnitsPressure.MEGAPASCAL][UnitsPressure.EXAPASCAL] = \
        Mul(Rat(Int(1), Pow(10, 12)), Sym("megapa"))  # nopep8
    CONVERSIONS[UnitsPressure.MEGAPASCAL][UnitsPressure.FEMTOPASCAL] = \
        Mul(Pow(10, 21), Sym("megapa"))  # nopep8
    CONVERSIONS[UnitsPressure.MEGAPASCAL][UnitsPressure.GIGAPASCAL] = \
        Mul(Rat(Int(1), Int(1000)), Sym("megapa"))  # nopep8
    CONVERSIONS[UnitsPressure.MEGAPASCAL][UnitsPressure.HECTOPASCAL] = \
        Mul(Pow(10, 4), Sym("megapa"))  # nopep8
    CONVERSIONS[UnitsPressure.MEGAPASCAL][UnitsPressure.KILOBAR] = \
        Mul(Rat(Int(1), Int(100)), Sym("megapa"))  # nopep8
    CONVERSIONS[UnitsPressure.MEGAPASCAL][UnitsPressure.KILOPASCAL] = \
        Mul(Int(1000), Sym("megapa"))  # nopep8
    CONVERSIONS[UnitsPressure.MEGAPASCAL][UnitsPressure.MEGABAR] = \
        Mul(Rat(Int(1), Pow(10, 5)), Sym("megapa"))  # nopep8
    CONVERSIONS[UnitsPressure.MEGAPASCAL][UnitsPressure.MICROPASCAL] = \
        Mul(Pow(10, 12), Sym("megapa"))  # nopep8
    CONVERSIONS[UnitsPressure.MEGAPASCAL][UnitsPressure.MILLIBAR] = \
        Mul(Pow(10, 4), Sym("megapa"))  # nopep8
    CONVERSIONS[UnitsPressure.MEGAPASCAL][UnitsPressure.MILLIPASCAL] = \
        Mul(Pow(10, 9), Sym("megapa"))  # nopep8
    CONVERSIONS[UnitsPressure.MEGAPASCAL][UnitsPressure.MILLITORR] = \
        Mul(Rat(Int(30400), Int(4053)), Sym("megapa"))  # nopep8
    CONVERSIONS[UnitsPressure.MEGAPASCAL][UnitsPressure.MMHG] = \
        Mul(Rat(Mul(Int(2), Pow(10, 14)), Int("26664477483")), Sym("megapa"))  # nopep8
    CONVERSIONS[UnitsPressure.MEGAPASCAL][UnitsPressure.NANOPASCAL] = \
        Mul(Pow(10, 15), Sym("megapa"))  # nopep8
    CONVERSIONS[UnitsPressure.MEGAPASCAL][UnitsPressure.PETAPASCAL] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("megapa"))  # nopep8
    CONVERSIONS[UnitsPressure.MEGAPASCAL][UnitsPressure.PICOPASCAL] = \
        Mul(Pow(10, 18), Sym("megapa"))  # nopep8
    CONVERSIONS[UnitsPressure.MEGAPASCAL][UnitsPressure.PSI] = \
        Mul(Rat(Mul(Int(25), Pow(10, 15)), Int("172368932329209")), Sym("megapa"))  # nopep8
    CONVERSIONS[UnitsPressure.MEGAPASCAL][UnitsPressure.Pascal] = \
        Mul(Pow(10, 6), Sym("megapa"))  # nopep8
    CONVERSIONS[UnitsPressure.MEGAPASCAL][UnitsPressure.TERAPASCAL] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("megapa"))  # nopep8
    CONVERSIONS[UnitsPressure.MEGAPASCAL][UnitsPressure.TORR] = \
        Mul(Rat(Mul(Int(304), Pow(10, 5)), Int(4053)), Sym("megapa"))  # nopep8
    CONVERSIONS[UnitsPressure.MEGAPASCAL][UnitsPressure.YOCTOPASCAL] = \
        Mul(Pow(10, 30), Sym("megapa"))  # nopep8
    CONVERSIONS[UnitsPressure.MEGAPASCAL][UnitsPressure.YOTTAPASCAL] = \
        Mul(Rat(Int(1), Pow(10, 18)), Sym("megapa"))  # nopep8
    CONVERSIONS[UnitsPressure.MEGAPASCAL][UnitsPressure.ZEPTOPASCAL] = \
        Mul(Pow(10, 27), Sym("megapa"))  # nopep8
    CONVERSIONS[UnitsPressure.MEGAPASCAL][UnitsPressure.ZETTAPASCAL] = \
        Mul(Rat(Int(1), Pow(10, 15)), Sym("megapa"))  # nopep8
    CONVERSIONS[UnitsPressure.MICROPASCAL][UnitsPressure.ATMOSPHERE] = \
        Mul(Rat(Int(1), Mul(Int(101325), Pow(10, 6))), Sym("micropa"))  # nopep8
    CONVERSIONS[UnitsPressure.MICROPASCAL][UnitsPressure.ATTOPASCAL] = \
        Mul(Pow(10, 12), Sym("micropa"))  # nopep8
    CONVERSIONS[UnitsPressure.MICROPASCAL][UnitsPressure.BAR] = \
        Mul(Rat(Int(1), Pow(10, 11)), Sym("micropa"))  # nopep8
    CONVERSIONS[UnitsPressure.MICROPASCAL][UnitsPressure.CENTIBAR] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("micropa"))  # nopep8
    CONVERSIONS[UnitsPressure.MICROPASCAL][UnitsPressure.CENTIPASCAL] = \
        Mul(Rat(Int(1), Pow(10, 4)), Sym("micropa"))  # nopep8
    CONVERSIONS[UnitsPressure.MICROPASCAL][UnitsPressure.DECAPASCAL] = \
        Mul(Rat(Int(1), Pow(10, 7)), Sym("micropa"))  # nopep8
    CONVERSIONS[UnitsPressure.MICROPASCAL][UnitsPressure.DECIBAR] = \
        Mul(Rat(Int(1), Pow(10, 10)), Sym("micropa"))  # nopep8
    CONVERSIONS[UnitsPressure.MICROPASCAL][UnitsPressure.DECIPASCAL] = \
        Mul(Rat(Int(1), Pow(10, 5)), Sym("micropa"))  # nopep8
    CONVERSIONS[UnitsPressure.MICROPASCAL][UnitsPressure.EXAPASCAL] = \
        Mul(Rat(Int(1), Pow(10, 24)), Sym("micropa"))  # nopep8
    CONVERSIONS[UnitsPressure.MICROPASCAL][UnitsPressure.FEMTOPASCAL] = \
        Mul(Pow(10, 9), Sym("micropa"))  # nopep8
    CONVERSIONS[UnitsPressure.MICROPASCAL][UnitsPressure.GIGAPASCAL] = \
        Mul(Rat(Int(1), Pow(10, 15)), Sym("micropa"))  # nopep8
    CONVERSIONS[UnitsPressure.MICROPASCAL][UnitsPressure.HECTOPASCAL] = \
        Mul(Rat(Int(1), Pow(10, 8)), Sym("micropa"))  # nopep8
    CONVERSIONS[UnitsPressure.MICROPASCAL][UnitsPressure.KILOBAR] = \
        Mul(Rat(Int(1), Pow(10, 14)), Sym("micropa"))  # nopep8
    CONVERSIONS[UnitsPressure.MICROPASCAL][UnitsPressure.KILOPASCAL] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("micropa"))  # nopep8
    CONVERSIONS[UnitsPressure.MICROPASCAL][UnitsPressure.MEGABAR] = \
        Mul(Rat(Int(1), Pow(10, 17)), Sym("micropa"))  # nopep8
    CONVERSIONS[UnitsPressure.MICROPASCAL][UnitsPressure.MEGAPASCAL] = \
        Mul(Rat(Int(1), Pow(10, 12)), Sym("micropa"))  # nopep8
    CONVERSIONS[UnitsPressure.MICROPASCAL][UnitsPressure.MILLIBAR] = \
        Mul(Rat(Int(1), Pow(10, 8)), Sym("micropa"))  # nopep8
    CONVERSIONS[UnitsPressure.MICROPASCAL][UnitsPressure.MILLIPASCAL] = \
        Mul(Rat(Int(1), Int(1000)), Sym("micropa"))  # nopep8
    CONVERSIONS[UnitsPressure.MICROPASCAL][UnitsPressure.MILLITORR] = \
        Mul(Rat(Int(19), Mul(Int(2533125), Pow(10, 6))), Sym("micropa"))  # nopep8
    CONVERSIONS[UnitsPressure.MICROPASCAL][UnitsPressure.MMHG] = \
        Mul(Rat(Int(200), Int("26664477483")), Sym("micropa"))  # nopep8
    CONVERSIONS[UnitsPressure.MICROPASCAL][UnitsPressure.NANOPASCAL] = \
        Mul(Int(1000), Sym("micropa"))  # nopep8
    CONVERSIONS[UnitsPressure.MICROPASCAL][UnitsPressure.PETAPASCAL] = \
        Mul(Rat(Int(1), Pow(10, 21)), Sym("micropa"))  # nopep8
    CONVERSIONS[UnitsPressure.MICROPASCAL][UnitsPressure.PICOPASCAL] = \
        Mul(Pow(10, 6), Sym("micropa"))  # nopep8
    CONVERSIONS[UnitsPressure.MICROPASCAL][UnitsPressure.PSI] = \
        Mul(Rat(Int(25000), Int("172368932329209")), Sym("micropa"))  # nopep8
    CONVERSIONS[UnitsPressure.MICROPASCAL][UnitsPressure.Pascal] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("micropa"))  # nopep8
    CONVERSIONS[UnitsPressure.MICROPASCAL][UnitsPressure.TERAPASCAL] = \
        Mul(Rat(Int(1), Pow(10, 18)), Sym("micropa"))  # nopep8
    CONVERSIONS[UnitsPressure.MICROPASCAL][UnitsPressure.TORR] = \
        Mul(Rat(Int(19), Int("2533125000")), Sym("micropa"))  # nopep8
    CONVERSIONS[UnitsPressure.MICROPASCAL][UnitsPressure.YOCTOPASCAL] = \
        Mul(Pow(10, 18), Sym("micropa"))  # nopep8
    CONVERSIONS[UnitsPressure.MICROPASCAL][UnitsPressure.YOTTAPASCAL] = \
        Mul(Rat(Int(1), Pow(10, 30)), Sym("micropa"))  # nopep8
    CONVERSIONS[UnitsPressure.MICROPASCAL][UnitsPressure.ZEPTOPASCAL] = \
        Mul(Pow(10, 15), Sym("micropa"))  # nopep8
    CONVERSIONS[UnitsPressure.MICROPASCAL][UnitsPressure.ZETTAPASCAL] = \
        Mul(Rat(Int(1), Pow(10, 27)), Sym("micropa"))  # nopep8
    CONVERSIONS[UnitsPressure.MILLIBAR][UnitsPressure.ATMOSPHERE] = \
        Mul(Rat(Int(4), Int(4053)), Sym("mbar"))  # nopep8
    CONVERSIONS[UnitsPressure.MILLIBAR][UnitsPressure.ATTOPASCAL] = \
        Mul(Pow(10, 20), Sym("mbar"))  # nopep8
    CONVERSIONS[UnitsPressure.MILLIBAR][UnitsPressure.BAR] = \
        Mul(Rat(Int(1), Int(1000)), Sym("mbar"))  # nopep8
    CONVERSIONS[UnitsPressure.MILLIBAR][UnitsPressure.CENTIBAR] = \
        Mul(Rat(Int(1), Int(10)), Sym("mbar"))  # nopep8
    CONVERSIONS[UnitsPressure.MILLIBAR][UnitsPressure.CENTIPASCAL] = \
        Mul(Pow(10, 4), Sym("mbar"))  # nopep8
    CONVERSIONS[UnitsPressure.MILLIBAR][UnitsPressure.DECAPASCAL] = \
        Mul(Int(10), Sym("mbar"))  # nopep8
    CONVERSIONS[UnitsPressure.MILLIBAR][UnitsPressure.DECIBAR] = \
        Mul(Rat(Int(1), Int(100)), Sym("mbar"))  # nopep8
    CONVERSIONS[UnitsPressure.MILLIBAR][UnitsPressure.DECIPASCAL] = \
        Mul(Int(1000), Sym("mbar"))  # nopep8
    CONVERSIONS[UnitsPressure.MILLIBAR][UnitsPressure.EXAPASCAL] = \
        Mul(Rat(Int(1), Pow(10, 16)), Sym("mbar"))  # nopep8
    CONVERSIONS[UnitsPressure.MILLIBAR][UnitsPressure.FEMTOPASCAL] = \
        Mul(Pow(10, 17), Sym("mbar"))  # nopep8
    CONVERSIONS[UnitsPressure.MILLIBAR][UnitsPressure.GIGAPASCAL] = \
        Mul(Rat(Int(1), Pow(10, 7)), Sym("mbar"))  # nopep8
    CONVERSIONS[UnitsPressure.MILLIBAR][UnitsPressure.HECTOPASCAL] = \
        Sym("mbar")  # nopep8
    CONVERSIONS[UnitsPressure.MILLIBAR][UnitsPressure.KILOBAR] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("mbar"))  # nopep8
    CONVERSIONS[UnitsPressure.MILLIBAR][UnitsPressure.KILOPASCAL] = \
        Mul(Rat(Int(1), Int(10)), Sym("mbar"))  # nopep8
    CONVERSIONS[UnitsPressure.MILLIBAR][UnitsPressure.MEGABAR] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("mbar"))  # nopep8
    CONVERSIONS[UnitsPressure.MILLIBAR][UnitsPressure.MEGAPASCAL] = \
        Mul(Rat(Int(1), Pow(10, 4)), Sym("mbar"))  # nopep8
    CONVERSIONS[UnitsPressure.MILLIBAR][UnitsPressure.MICROPASCAL] = \
        Mul(Pow(10, 8), Sym("mbar"))  # nopep8
    CONVERSIONS[UnitsPressure.MILLIBAR][UnitsPressure.MILLIPASCAL] = \
        Mul(Pow(10, 5), Sym("mbar"))  # nopep8
    CONVERSIONS[UnitsPressure.MILLIBAR][UnitsPressure.MILLITORR] = \
        Mul(Rat(Int(76), Int(101325)), Sym("mbar"))  # nopep8
    CONVERSIONS[UnitsPressure.MILLIBAR][UnitsPressure.MMHG] = \
        Mul(Rat(Mul(Int(2), Pow(10, 10)), Int("26664477483")), Sym("mbar"))  # nopep8
    CONVERSIONS[UnitsPressure.MILLIBAR][UnitsPressure.NANOPASCAL] = \
        Mul(Pow(10, 11), Sym("mbar"))  # nopep8
    CONVERSIONS[UnitsPressure.MILLIBAR][UnitsPressure.PETAPASCAL] = \
        Mul(Rat(Int(1), Pow(10, 13)), Sym("mbar"))  # nopep8
    CONVERSIONS[UnitsPressure.MILLIBAR][UnitsPressure.PICOPASCAL] = \
        Mul(Pow(10, 14), Sym("mbar"))  # nopep8
    CONVERSIONS[UnitsPressure.MILLIBAR][UnitsPressure.PSI] = \
        Mul(Rat(Mul(Int(25), Pow(10, 11)), Int("172368932329209")), Sym("mbar"))  # nopep8
    CONVERSIONS[UnitsPressure.MILLIBAR][UnitsPressure.Pascal] = \
        Mul(Int(100), Sym("mbar"))  # nopep8
    CONVERSIONS[UnitsPressure.MILLIBAR][UnitsPressure.TERAPASCAL] = \
        Mul(Rat(Int(1), Pow(10, 10)), Sym("mbar"))  # nopep8
    CONVERSIONS[UnitsPressure.MILLIBAR][UnitsPressure.TORR] = \
        Mul(Rat(Int(3040), Int(4053)), Sym("mbar"))  # nopep8
    CONVERSIONS[UnitsPressure.MILLIBAR][UnitsPressure.YOCTOPASCAL] = \
        Mul(Pow(10, 26), Sym("mbar"))  # nopep8
    CONVERSIONS[UnitsPressure.MILLIBAR][UnitsPressure.YOTTAPASCAL] = \
        Mul(Rat(Int(1), Pow(10, 22)), Sym("mbar"))  # nopep8
    CONVERSIONS[UnitsPressure.MILLIBAR][UnitsPressure.ZEPTOPASCAL] = \
        Mul(Pow(10, 23), Sym("mbar"))  # nopep8
    CONVERSIONS[UnitsPressure.MILLIBAR][UnitsPressure.ZETTAPASCAL] = \
        Mul(Rat(Int(1), Pow(10, 19)), Sym("mbar"))  # nopep8
    CONVERSIONS[UnitsPressure.MILLIPASCAL][UnitsPressure.ATMOSPHERE] = \
        Mul(Rat(Int(1), Int(101325000)), Sym("millipa"))  # nopep8
    CONVERSIONS[UnitsPressure.MILLIPASCAL][UnitsPressure.ATTOPASCAL] = \
        Mul(Pow(10, 15), Sym("millipa"))  # nopep8
    CONVERSIONS[UnitsPressure.MILLIPASCAL][UnitsPressure.BAR] = \
        Mul(Rat(Int(1), Pow(10, 8)), Sym("millipa"))  # nopep8
    CONVERSIONS[UnitsPressure.MILLIPASCAL][UnitsPressure.CENTIBAR] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("millipa"))  # nopep8
    CONVERSIONS[UnitsPressure.MILLIPASCAL][UnitsPressure.CENTIPASCAL] = \
        Mul(Rat(Int(1), Int(10)), Sym("millipa"))  # nopep8
    CONVERSIONS[UnitsPressure.MILLIPASCAL][UnitsPressure.DECAPASCAL] = \
        Mul(Rat(Int(1), Pow(10, 4)), Sym("millipa"))  # nopep8
    CONVERSIONS[UnitsPressure.MILLIPASCAL][UnitsPressure.DECIBAR] = \
        Mul(Rat(Int(1), Pow(10, 7)), Sym("millipa"))  # nopep8
    CONVERSIONS[UnitsPressure.MILLIPASCAL][UnitsPressure.DECIPASCAL] = \
        Mul(Rat(Int(1), Int(100)), Sym("millipa"))  # nopep8
    CONVERSIONS[UnitsPressure.MILLIPASCAL][UnitsPressure.EXAPASCAL] = \
        Mul(Rat(Int(1), Pow(10, 21)), Sym("millipa"))  # nopep8
    CONVERSIONS[UnitsPressure.MILLIPASCAL][UnitsPressure.FEMTOPASCAL] = \
        Mul(Pow(10, 12), Sym("millipa"))  # nopep8
    CONVERSIONS[UnitsPressure.MILLIPASCAL][UnitsPressure.GIGAPASCAL] = \
        Mul(Rat(Int(1), Pow(10, 12)), Sym("millipa"))  # nopep8
    CONVERSIONS[UnitsPressure.MILLIPASCAL][UnitsPressure.HECTOPASCAL] = \
        Mul(Rat(Int(1), Pow(10, 5)), Sym("millipa"))  # nopep8
    CONVERSIONS[UnitsPressure.MILLIPASCAL][UnitsPressure.KILOBAR] = \
        Mul(Rat(Int(1), Pow(10, 11)), Sym("millipa"))  # nopep8
    CONVERSIONS[UnitsPressure.MILLIPASCAL][UnitsPressure.KILOPASCAL] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("millipa"))  # nopep8
    CONVERSIONS[UnitsPressure.MILLIPASCAL][UnitsPressure.MEGABAR] = \
        Mul(Rat(Int(1), Pow(10, 14)), Sym("millipa"))  # nopep8
    CONVERSIONS[UnitsPressure.MILLIPASCAL][UnitsPressure.MEGAPASCAL] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("millipa"))  # nopep8
    CONVERSIONS[UnitsPressure.MILLIPASCAL][UnitsPressure.MICROPASCAL] = \
        Mul(Int(1000), Sym("millipa"))  # nopep8
    CONVERSIONS[UnitsPressure.MILLIPASCAL][UnitsPressure.MILLIBAR] = \
        Mul(Rat(Int(1), Pow(10, 5)), Sym("millipa"))  # nopep8
    CONVERSIONS[UnitsPressure.MILLIPASCAL][UnitsPressure.MILLITORR] = \
        Mul(Rat(Int(19), Int("2533125000")), Sym("millipa"))  # nopep8
    CONVERSIONS[UnitsPressure.MILLIPASCAL][UnitsPressure.MMHG] = \
        Mul(Rat(Mul(Int(2), Pow(10, 5)), Int("26664477483")), Sym("millipa"))  # nopep8
    CONVERSIONS[UnitsPressure.MILLIPASCAL][UnitsPressure.NANOPASCAL] = \
        Mul(Pow(10, 6), Sym("millipa"))  # nopep8
    CONVERSIONS[UnitsPressure.MILLIPASCAL][UnitsPressure.PETAPASCAL] = \
        Mul(Rat(Int(1), Pow(10, 18)), Sym("millipa"))  # nopep8
    CONVERSIONS[UnitsPressure.MILLIPASCAL][UnitsPressure.PICOPASCAL] = \
        Mul(Pow(10, 9), Sym("millipa"))  # nopep8
    CONVERSIONS[UnitsPressure.MILLIPASCAL][UnitsPressure.PSI] = \
        Mul(Rat(Mul(Int(25), Pow(10, 6)), Int("172368932329209")), Sym("millipa"))  # nopep8
    CONVERSIONS[UnitsPressure.MILLIPASCAL][UnitsPressure.Pascal] = \
        Mul(Rat(Int(1), Int(1000)), Sym("millipa"))  # nopep8
    CONVERSIONS[UnitsPressure.MILLIPASCAL][UnitsPressure.TERAPASCAL] = \
        Mul(Rat(Int(1), Pow(10, 15)), Sym("millipa"))  # nopep8
    CONVERSIONS[UnitsPressure.MILLIPASCAL][UnitsPressure.TORR] = \
        Mul(Rat(Int(19), Int(2533125)), Sym("millipa"))  # nopep8
    CONVERSIONS[UnitsPressure.MILLIPASCAL][UnitsPressure.YOCTOPASCAL] = \
        Mul(Pow(10, 21), Sym("millipa"))  # nopep8
    CONVERSIONS[UnitsPressure.MILLIPASCAL][UnitsPressure.YOTTAPASCAL] = \
        Mul(Rat(Int(1), Pow(10, 27)), Sym("millipa"))  # nopep8
    CONVERSIONS[UnitsPressure.MILLIPASCAL][UnitsPressure.ZEPTOPASCAL] = \
        Mul(Pow(10, 18), Sym("millipa"))  # nopep8
    CONVERSIONS[UnitsPressure.MILLIPASCAL][UnitsPressure.ZETTAPASCAL] = \
        Mul(Rat(Int(1), Pow(10, 24)), Sym("millipa"))  # nopep8
    CONVERSIONS[UnitsPressure.MILLITORR][UnitsPressure.ATMOSPHERE] = \
        Mul(Rat(Int(25), Int(19)), Sym("mtorr"))  # nopep8
    CONVERSIONS[UnitsPressure.MILLITORR][UnitsPressure.ATTOPASCAL] = \
        Mul(Rat(Mul(Int(2533125), Pow(10, 18)), Int(19)), Sym("mtorr"))  # nopep8
    CONVERSIONS[UnitsPressure.MILLITORR][UnitsPressure.BAR] = \
        Mul(Rat(Int(4053), Int(3040)), Sym("mtorr"))  # nopep8
    CONVERSIONS[UnitsPressure.MILLITORR][UnitsPressure.CENTIBAR] = \
        Mul(Rat(Int(20265), Int(152)), Sym("mtorr"))  # nopep8
    CONVERSIONS[UnitsPressure.MILLITORR][UnitsPressure.CENTIPASCAL] = \
        Mul(Rat(Int(253312500), Int(19)), Sym("mtorr"))  # nopep8
    CONVERSIONS[UnitsPressure.MILLITORR][UnitsPressure.DECAPASCAL] = \
        Mul(Rat(Int(506625), Int(38)), Sym("mtorr"))  # nopep8
    CONVERSIONS[UnitsPressure.MILLITORR][UnitsPressure.DECIBAR] = \
        Mul(Rat(Int(4053), Int(304)), Sym("mtorr"))  # nopep8
    CONVERSIONS[UnitsPressure.MILLITORR][UnitsPressure.DECIPASCAL] = \
        Mul(Rat(Int(25331250), Int(19)), Sym("mtorr"))  # nopep8
    CONVERSIONS[UnitsPressure.MILLITORR][UnitsPressure.EXAPASCAL] = \
        Mul(Rat(Int(4053), Mul(Int(304), Pow(10, 14))), Sym("mtorr"))  # nopep8
    CONVERSIONS[UnitsPressure.MILLITORR][UnitsPressure.FEMTOPASCAL] = \
        Mul(Rat(Mul(Int(2533125), Pow(10, 15)), Int(19)), Sym("mtorr"))  # nopep8
    CONVERSIONS[UnitsPressure.MILLITORR][UnitsPressure.GIGAPASCAL] = \
        Mul(Rat(Int(4053), Mul(Int(304), Pow(10, 5))), Sym("mtorr"))  # nopep8
    CONVERSIONS[UnitsPressure.MILLITORR][UnitsPressure.HECTOPASCAL] = \
        Mul(Rat(Int(101325), Int(76)), Sym("mtorr"))  # nopep8
    CONVERSIONS[UnitsPressure.MILLITORR][UnitsPressure.KILOBAR] = \
        Mul(Rat(Int(4053), Mul(Int(304), Pow(10, 4))), Sym("mtorr"))  # nopep8
    CONVERSIONS[UnitsPressure.MILLITORR][UnitsPressure.KILOPASCAL] = \
        Mul(Rat(Int(20265), Int(152)), Sym("mtorr"))  # nopep8
    CONVERSIONS[UnitsPressure.MILLITORR][UnitsPressure.MEGABAR] = \
        Mul(Rat(Int(4053), Mul(Int(304), Pow(10, 7))), Sym("mtorr"))  # nopep8
    CONVERSIONS[UnitsPressure.MILLITORR][UnitsPressure.MEGAPASCAL] = \
        Mul(Rat(Int(4053), Int(30400)), Sym("mtorr"))  # nopep8
    CONVERSIONS[UnitsPressure.MILLITORR][UnitsPressure.MICROPASCAL] = \
        Mul(Rat(Mul(Int(2533125), Pow(10, 6)), Int(19)), Sym("mtorr"))  # nopep8
    CONVERSIONS[UnitsPressure.MILLITORR][UnitsPressure.MILLIBAR] = \
        Mul(Rat(Int(101325), Int(76)), Sym("mtorr"))  # nopep8
    CONVERSIONS[UnitsPressure.MILLITORR][UnitsPressure.MILLIPASCAL] = \
        Mul(Rat(Int("2533125000"), Int(19)), Sym("mtorr"))  # nopep8
    CONVERSIONS[UnitsPressure.MILLITORR][UnitsPressure.MMHG] = \
        Mul(Rat(Mul(Int(24125), Pow(10, 9)), Int("24125003437")), Sym("mtorr"))  # nopep8
    CONVERSIONS[UnitsPressure.MILLITORR][UnitsPressure.NANOPASCAL] = \
        Mul(Rat(Mul(Int(2533125), Pow(10, 9)), Int(19)), Sym("mtorr"))  # nopep8
    CONVERSIONS[UnitsPressure.MILLITORR][UnitsPressure.PETAPASCAL] = \
        Mul(Rat(Int(4053), Mul(Int(304), Pow(10, 11))), Sym("mtorr"))  # nopep8
    CONVERSIONS[UnitsPressure.MILLITORR][UnitsPressure.PICOPASCAL] = \
        Mul(Rat(Mul(Int(2533125), Pow(10, 12)), Int(19)), Sym("mtorr"))  # nopep8
    CONVERSIONS[UnitsPressure.MILLITORR][UnitsPressure.PSI] = \
        Mul(Rat(Mul(Int(3015625), Pow(10, 9)), Int("155952843535951")), Sym("mtorr"))  # nopep8
    CONVERSIONS[UnitsPressure.MILLITORR][UnitsPressure.Pascal] = \
        Mul(Rat(Int(2533125), Int(19)), Sym("mtorr"))  # nopep8
    CONVERSIONS[UnitsPressure.MILLITORR][UnitsPressure.TERAPASCAL] = \
        Mul(Rat(Int(4053), Mul(Int(304), Pow(10, 8))), Sym("mtorr"))  # nopep8
    CONVERSIONS[UnitsPressure.MILLITORR][UnitsPressure.TORR] = \
        Mul(Int(1000), Sym("mtorr"))  # nopep8
    CONVERSIONS[UnitsPressure.MILLITORR][UnitsPressure.YOCTOPASCAL] = \
        Mul(Rat(Mul(Int(2533125), Pow(10, 24)), Int(19)), Sym("mtorr"))  # nopep8
    CONVERSIONS[UnitsPressure.MILLITORR][UnitsPressure.YOTTAPASCAL] = \
        Mul(Rat(Int(4053), Mul(Int(304), Pow(10, 20))), Sym("mtorr"))  # nopep8
    CONVERSIONS[UnitsPressure.MILLITORR][UnitsPressure.ZEPTOPASCAL] = \
        Mul(Rat(Mul(Int(2533125), Pow(10, 21)), Int(19)), Sym("mtorr"))  # nopep8
    CONVERSIONS[UnitsPressure.MILLITORR][UnitsPressure.ZETTAPASCAL] = \
        Mul(Rat(Int(4053), Mul(Int(304), Pow(10, 17))), Sym("mtorr"))  # nopep8
    CONVERSIONS[UnitsPressure.MMHG][UnitsPressure.ATMOSPHERE] = \
        Mul(Rat(Int(1269737023), Mul(Int(965), Pow(10, 9))), Sym("mmhg"))  # nopep8
    CONVERSIONS[UnitsPressure.MMHG][UnitsPressure.ATTOPASCAL] = \
        Mul(Mul(Int("133322387415"), Pow(10, 9)), Sym("mmhg"))  # nopep8
    CONVERSIONS[UnitsPressure.MMHG][UnitsPressure.BAR] = \
        Mul(Rat(Int("26664477483"), Mul(Int(2), Pow(10, 13))), Sym("mmhg"))  # nopep8
    CONVERSIONS[UnitsPressure.MMHG][UnitsPressure.CENTIBAR] = \
        Mul(Rat(Int("26664477483"), Mul(Int(2), Pow(10, 11))), Sym("mmhg"))  # nopep8
    CONVERSIONS[UnitsPressure.MMHG][UnitsPressure.CENTIPASCAL] = \
        Mul(Rat(Int("26664477483"), Mul(Int(2), Pow(10, 6))), Sym("mmhg"))  # nopep8
    CONVERSIONS[UnitsPressure.MMHG][UnitsPressure.DECAPASCAL] = \
        Mul(Rat(Int("26664477483"), Mul(Int(2), Pow(10, 9))), Sym("mmhg"))  # nopep8
    CONVERSIONS[UnitsPressure.MMHG][UnitsPressure.DECIBAR] = \
        Mul(Rat(Int("26664477483"), Mul(Int(2), Pow(10, 12))), Sym("mmhg"))  # nopep8
    CONVERSIONS[UnitsPressure.MMHG][UnitsPressure.DECIPASCAL] = \
        Mul(Rat(Int("26664477483"), Mul(Int(2), Pow(10, 7))), Sym("mmhg"))  # nopep8
    CONVERSIONS[UnitsPressure.MMHG][UnitsPressure.EXAPASCAL] = \
        Mul(Rat(Int("26664477483"), Mul(Int(2), Pow(10, 26))), Sym("mmhg"))  # nopep8
    CONVERSIONS[UnitsPressure.MMHG][UnitsPressure.FEMTOPASCAL] = \
        Mul(Mul(Int("133322387415"), Pow(10, 6)), Sym("mmhg"))  # nopep8
    CONVERSIONS[UnitsPressure.MMHG][UnitsPressure.GIGAPASCAL] = \
        Mul(Rat(Int("26664477483"), Mul(Int(2), Pow(10, 17))), Sym("mmhg"))  # nopep8
    CONVERSIONS[UnitsPressure.MMHG][UnitsPressure.HECTOPASCAL] = \
        Mul(Rat(Int("26664477483"), Mul(Int(2), Pow(10, 10))), Sym("mmhg"))  # nopep8
    CONVERSIONS[UnitsPressure.MMHG][UnitsPressure.KILOBAR] = \
        Mul(Rat(Int("26664477483"), Mul(Int(2), Pow(10, 16))), Sym("mmhg"))  # nopep8
    CONVERSIONS[UnitsPressure.MMHG][UnitsPressure.KILOPASCAL] = \
        Mul(Rat(Int("26664477483"), Mul(Int(2), Pow(10, 11))), Sym("mmhg"))  # nopep8
    CONVERSIONS[UnitsPressure.MMHG][UnitsPressure.MEGABAR] = \
        Mul(Rat(Int("26664477483"), Mul(Int(2), Pow(10, 19))), Sym("mmhg"))  # nopep8
    CONVERSIONS[UnitsPressure.MMHG][UnitsPressure.MEGAPASCAL] = \
        Mul(Rat(Int("26664477483"), Mul(Int(2), Pow(10, 14))), Sym("mmhg"))  # nopep8
    CONVERSIONS[UnitsPressure.MMHG][UnitsPressure.MICROPASCAL] = \
        Mul(Rat(Int("26664477483"), Int(200)), Sym("mmhg"))  # nopep8
    CONVERSIONS[UnitsPressure.MMHG][UnitsPressure.MILLIBAR] = \
        Mul(Rat(Int("26664477483"), Mul(Int(2), Pow(10, 10))), Sym("mmhg"))  # nopep8
    CONVERSIONS[UnitsPressure.MMHG][UnitsPressure.MILLIPASCAL] = \
        Mul(Rat(Int("26664477483"), Mul(Int(2), Pow(10, 5))), Sym("mmhg"))  # nopep8
    CONVERSIONS[UnitsPressure.MMHG][UnitsPressure.MILLITORR] = \
        Mul(Rat(Int("24125003437"), Mul(Int(24125), Pow(10, 9))), Sym("mmhg"))  # nopep8
    CONVERSIONS[UnitsPressure.MMHG][UnitsPressure.NANOPASCAL] = \
        Mul(Int("133322387415"), Sym("mmhg"))  # nopep8
    CONVERSIONS[UnitsPressure.MMHG][UnitsPressure.PETAPASCAL] = \
        Mul(Rat(Int("26664477483"), Mul(Int(2), Pow(10, 23))), Sym("mmhg"))  # nopep8
    CONVERSIONS[UnitsPressure.MMHG][UnitsPressure.PICOPASCAL] = \
        Mul(Int("133322387415000"), Sym("mmhg"))  # nopep8
    CONVERSIONS[UnitsPressure.MMHG][UnitsPressure.PSI] = \
        Mul(Rat(Int("158717127875"), Int("8208044396629")), Sym("mmhg"))  # nopep8
    CONVERSIONS[UnitsPressure.MMHG][UnitsPressure.Pascal] = \
        Mul(Rat(Int("26664477483"), Mul(Int(2), Pow(10, 8))), Sym("mmhg"))  # nopep8
    CONVERSIONS[UnitsPressure.MMHG][UnitsPressure.TERAPASCAL] = \
        Mul(Rat(Int("26664477483"), Mul(Int(2), Pow(10, 20))), Sym("mmhg"))  # nopep8
    CONVERSIONS[UnitsPressure.MMHG][UnitsPressure.TORR] = \
        Mul(Rat(Int("24125003437"), Mul(Int(24125), Pow(10, 6))), Sym("mmhg"))  # nopep8
    CONVERSIONS[UnitsPressure.MMHG][UnitsPressure.YOCTOPASCAL] = \
        Mul(Mul(Int("133322387415"), Pow(10, 15)), Sym("mmhg"))  # nopep8
    CONVERSIONS[UnitsPressure.MMHG][UnitsPressure.YOTTAPASCAL] = \
        Mul(Rat(Int("26664477483"), Mul(Int(2), Pow(10, 32))), Sym("mmhg"))  # nopep8
    CONVERSIONS[UnitsPressure.MMHG][UnitsPressure.ZEPTOPASCAL] = \
        Mul(Mul(Int("133322387415"), Pow(10, 12)), Sym("mmhg"))  # nopep8
    CONVERSIONS[UnitsPressure.MMHG][UnitsPressure.ZETTAPASCAL] = \
        Mul(Rat(Int("26664477483"), Mul(Int(2), Pow(10, 29))), Sym("mmhg"))  # nopep8
    CONVERSIONS[UnitsPressure.NANOPASCAL][UnitsPressure.ATMOSPHERE] = \
        Mul(Rat(Int(1), Mul(Int(101325), Pow(10, 9))), Sym("nanopa"))  # nopep8
    CONVERSIONS[UnitsPressure.NANOPASCAL][UnitsPressure.ATTOPASCAL] = \
        Mul(Pow(10, 9), Sym("nanopa"))  # nopep8
    CONVERSIONS[UnitsPressure.NANOPASCAL][UnitsPressure.BAR] = \
        Mul(Rat(Int(1), Pow(10, 14)), Sym("nanopa"))  # nopep8
    CONVERSIONS[UnitsPressure.NANOPASCAL][UnitsPressure.CENTIBAR] = \
        Mul(Rat(Int(1), Pow(10, 12)), Sym("nanopa"))  # nopep8
    CONVERSIONS[UnitsPressure.NANOPASCAL][UnitsPressure.CENTIPASCAL] = \
        Mul(Rat(Int(1), Pow(10, 7)), Sym("nanopa"))  # nopep8
    CONVERSIONS[UnitsPressure.NANOPASCAL][UnitsPressure.DECAPASCAL] = \
        Mul(Rat(Int(1), Pow(10, 10)), Sym("nanopa"))  # nopep8
    CONVERSIONS[UnitsPressure.NANOPASCAL][UnitsPressure.DECIBAR] = \
        Mul(Rat(Int(1), Pow(10, 13)), Sym("nanopa"))  # nopep8
    CONVERSIONS[UnitsPressure.NANOPASCAL][UnitsPressure.DECIPASCAL] = \
        Mul(Rat(Int(1), Pow(10, 8)), Sym("nanopa"))  # nopep8
    CONVERSIONS[UnitsPressure.NANOPASCAL][UnitsPressure.EXAPASCAL] = \
        Mul(Rat(Int(1), Pow(10, 27)), Sym("nanopa"))  # nopep8
    CONVERSIONS[UnitsPressure.NANOPASCAL][UnitsPressure.FEMTOPASCAL] = \
        Mul(Pow(10, 6), Sym("nanopa"))  # nopep8
    CONVERSIONS[UnitsPressure.NANOPASCAL][UnitsPressure.GIGAPASCAL] = \
        Mul(Rat(Int(1), Pow(10, 18)), Sym("nanopa"))  # nopep8
    CONVERSIONS[UnitsPressure.NANOPASCAL][UnitsPressure.HECTOPASCAL] = \
        Mul(Rat(Int(1), Pow(10, 11)), Sym("nanopa"))  # nopep8
    CONVERSIONS[UnitsPressure.NANOPASCAL][UnitsPressure.KILOBAR] = \
        Mul(Rat(Int(1), Pow(10, 17)), Sym("nanopa"))  # nopep8
    CONVERSIONS[UnitsPressure.NANOPASCAL][UnitsPressure.KILOPASCAL] = \
        Mul(Rat(Int(1), Pow(10, 12)), Sym("nanopa"))  # nopep8
    CONVERSIONS[UnitsPressure.NANOPASCAL][UnitsPressure.MEGABAR] = \
        Mul(Rat(Int(1), Pow(10, 20)), Sym("nanopa"))  # nopep8
    CONVERSIONS[UnitsPressure.NANOPASCAL][UnitsPressure.MEGAPASCAL] = \
        Mul(Rat(Int(1), Pow(10, 15)), Sym("nanopa"))  # nopep8
    CONVERSIONS[UnitsPressure.NANOPASCAL][UnitsPressure.MICROPASCAL] = \
        Mul(Rat(Int(1), Int(1000)), Sym("nanopa"))  # nopep8
    CONVERSIONS[UnitsPressure.NANOPASCAL][UnitsPressure.MILLIBAR] = \
        Mul(Rat(Int(1), Pow(10, 11)), Sym("nanopa"))  # nopep8
    CONVERSIONS[UnitsPressure.NANOPASCAL][UnitsPressure.MILLIPASCAL] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("nanopa"))  # nopep8
    CONVERSIONS[UnitsPressure.NANOPASCAL][UnitsPressure.MILLITORR] = \
        Mul(Rat(Int(19), Mul(Int(2533125), Pow(10, 9))), Sym("nanopa"))  # nopep8
    CONVERSIONS[UnitsPressure.NANOPASCAL][UnitsPressure.MMHG] = \
        Mul(Rat(Int(1), Int("133322387415")), Sym("nanopa"))  # nopep8
    CONVERSIONS[UnitsPressure.NANOPASCAL][UnitsPressure.PETAPASCAL] = \
        Mul(Rat(Int(1), Pow(10, 24)), Sym("nanopa"))  # nopep8
    CONVERSIONS[UnitsPressure.NANOPASCAL][UnitsPressure.PICOPASCAL] = \
        Mul(Int(1000), Sym("nanopa"))  # nopep8
    CONVERSIONS[UnitsPressure.NANOPASCAL][UnitsPressure.PSI] = \
        Mul(Rat(Int(25), Int("172368932329209")), Sym("nanopa"))  # nopep8
    CONVERSIONS[UnitsPressure.NANOPASCAL][UnitsPressure.Pascal] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("nanopa"))  # nopep8
    CONVERSIONS[UnitsPressure.NANOPASCAL][UnitsPressure.TERAPASCAL] = \
        Mul(Rat(Int(1), Pow(10, 21)), Sym("nanopa"))  # nopep8
    CONVERSIONS[UnitsPressure.NANOPASCAL][UnitsPressure.TORR] = \
        Mul(Rat(Int(19), Mul(Int(2533125), Pow(10, 6))), Sym("nanopa"))  # nopep8
    CONVERSIONS[UnitsPressure.NANOPASCAL][UnitsPressure.YOCTOPASCAL] = \
        Mul(Pow(10, 15), Sym("nanopa"))  # nopep8
    CONVERSIONS[UnitsPressure.NANOPASCAL][UnitsPressure.YOTTAPASCAL] = \
        Mul(Rat(Int(1), Pow(10, 33)), Sym("nanopa"))  # nopep8
    CONVERSIONS[UnitsPressure.NANOPASCAL][UnitsPressure.ZEPTOPASCAL] = \
        Mul(Pow(10, 12), Sym("nanopa"))  # nopep8
    CONVERSIONS[UnitsPressure.NANOPASCAL][UnitsPressure.ZETTAPASCAL] = \
        Mul(Rat(Int(1), Pow(10, 30)), Sym("nanopa"))  # nopep8
    CONVERSIONS[UnitsPressure.PETAPASCAL][UnitsPressure.ATMOSPHERE] = \
        Mul(Rat(Mul(Int(4), Pow(10, 13)), Int(4053)), Sym("petapa"))  # nopep8
    CONVERSIONS[UnitsPressure.PETAPASCAL][UnitsPressure.ATTOPASCAL] = \
        Mul(Pow(10, 33), Sym("petapa"))  # nopep8
    CONVERSIONS[UnitsPressure.PETAPASCAL][UnitsPressure.BAR] = \
        Mul(Pow(10, 10), Sym("petapa"))  # nopep8
    CONVERSIONS[UnitsPressure.PETAPASCAL][UnitsPressure.CENTIBAR] = \
        Mul(Pow(10, 12), Sym("petapa"))  # nopep8
    CONVERSIONS[UnitsPressure.PETAPASCAL][UnitsPressure.CENTIPASCAL] = \
        Mul(Pow(10, 17), Sym("petapa"))  # nopep8
    CONVERSIONS[UnitsPressure.PETAPASCAL][UnitsPressure.DECAPASCAL] = \
        Mul(Pow(10, 14), Sym("petapa"))  # nopep8
    CONVERSIONS[UnitsPressure.PETAPASCAL][UnitsPressure.DECIBAR] = \
        Mul(Pow(10, 11), Sym("petapa"))  # nopep8
    CONVERSIONS[UnitsPressure.PETAPASCAL][UnitsPressure.DECIPASCAL] = \
        Mul(Pow(10, 16), Sym("petapa"))  # nopep8
    CONVERSIONS[UnitsPressure.PETAPASCAL][UnitsPressure.EXAPASCAL] = \
        Mul(Rat(Int(1), Int(1000)), Sym("petapa"))  # nopep8
    CONVERSIONS[UnitsPressure.PETAPASCAL][UnitsPressure.FEMTOPASCAL] = \
        Mul(Pow(10, 30), Sym("petapa"))  # nopep8
    CONVERSIONS[UnitsPressure.PETAPASCAL][UnitsPressure.GIGAPASCAL] = \
        Mul(Pow(10, 6), Sym("petapa"))  # nopep8
    CONVERSIONS[UnitsPressure.PETAPASCAL][UnitsPressure.HECTOPASCAL] = \
        Mul(Pow(10, 13), Sym("petapa"))  # nopep8
    CONVERSIONS[UnitsPressure.PETAPASCAL][UnitsPressure.KILOBAR] = \
        Mul(Pow(10, 7), Sym("petapa"))  # nopep8
    CONVERSIONS[UnitsPressure.PETAPASCAL][UnitsPressure.KILOPASCAL] = \
        Mul(Pow(10, 12), Sym("petapa"))  # nopep8
    CONVERSIONS[UnitsPressure.PETAPASCAL][UnitsPressure.MEGABAR] = \
        Mul(Pow(10, 4), Sym("petapa"))  # nopep8
    CONVERSIONS[UnitsPressure.PETAPASCAL][UnitsPressure.MEGAPASCAL] = \
        Mul(Pow(10, 9), Sym("petapa"))  # nopep8
    CONVERSIONS[UnitsPressure.PETAPASCAL][UnitsPressure.MICROPASCAL] = \
        Mul(Pow(10, 21), Sym("petapa"))  # nopep8
    CONVERSIONS[UnitsPressure.PETAPASCAL][UnitsPressure.MILLIBAR] = \
        Mul(Pow(10, 13), Sym("petapa"))  # nopep8
    CONVERSIONS[UnitsPressure.PETAPASCAL][UnitsPressure.MILLIPASCAL] = \
        Mul(Pow(10, 18), Sym("petapa"))  # nopep8
    CONVERSIONS[UnitsPressure.PETAPASCAL][UnitsPressure.MILLITORR] = \
        Mul(Rat(Mul(Int(304), Pow(10, 11)), Int(4053)), Sym("petapa"))  # nopep8
    CONVERSIONS[UnitsPressure.PETAPASCAL][UnitsPressure.MMHG] = \
        Mul(Rat(Mul(Int(2), Pow(10, 23)), Int("26664477483")), Sym("petapa"))  # nopep8
    CONVERSIONS[UnitsPressure.PETAPASCAL][UnitsPressure.NANOPASCAL] = \
        Mul(Pow(10, 24), Sym("petapa"))  # nopep8
    CONVERSIONS[UnitsPressure.PETAPASCAL][UnitsPressure.PICOPASCAL] = \
        Mul(Pow(10, 27), Sym("petapa"))  # nopep8
    CONVERSIONS[UnitsPressure.PETAPASCAL][UnitsPressure.PSI] = \
        Mul(Rat(Mul(Int(25), Pow(10, 24)), Int("172368932329209")), Sym("petapa"))  # nopep8
    CONVERSIONS[UnitsPressure.PETAPASCAL][UnitsPressure.Pascal] = \
        Mul(Pow(10, 15), Sym("petapa"))  # nopep8
    CONVERSIONS[UnitsPressure.PETAPASCAL][UnitsPressure.TERAPASCAL] = \
        Mul(Int(1000), Sym("petapa"))  # nopep8
    CONVERSIONS[UnitsPressure.PETAPASCAL][UnitsPressure.TORR] = \
        Mul(Rat(Mul(Int(304), Pow(10, 14)), Int(4053)), Sym("petapa"))  # nopep8
    CONVERSIONS[UnitsPressure.PETAPASCAL][UnitsPressure.YOCTOPASCAL] = \
        Mul(Pow(10, 39), Sym("petapa"))  # nopep8
    CONVERSIONS[UnitsPressure.PETAPASCAL][UnitsPressure.YOTTAPASCAL] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("petapa"))  # nopep8
    CONVERSIONS[UnitsPressure.PETAPASCAL][UnitsPressure.ZEPTOPASCAL] = \
        Mul(Pow(10, 36), Sym("petapa"))  # nopep8
    CONVERSIONS[UnitsPressure.PETAPASCAL][UnitsPressure.ZETTAPASCAL] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("petapa"))  # nopep8
    CONVERSIONS[UnitsPressure.PICOPASCAL][UnitsPressure.ATMOSPHERE] = \
        Mul(Rat(Int(1), Mul(Int(101325), Pow(10, 12))), Sym("picopa"))  # nopep8
    CONVERSIONS[UnitsPressure.PICOPASCAL][UnitsPressure.ATTOPASCAL] = \
        Mul(Pow(10, 6), Sym("picopa"))  # nopep8
    CONVERSIONS[UnitsPressure.PICOPASCAL][UnitsPressure.BAR] = \
        Mul(Rat(Int(1), Pow(10, 17)), Sym("picopa"))  # nopep8
    CONVERSIONS[UnitsPressure.PICOPASCAL][UnitsPressure.CENTIBAR] = \
        Mul(Rat(Int(1), Pow(10, 15)), Sym("picopa"))  # nopep8
    CONVERSIONS[UnitsPressure.PICOPASCAL][UnitsPressure.CENTIPASCAL] = \
        Mul(Rat(Int(1), Pow(10, 10)), Sym("picopa"))  # nopep8
    CONVERSIONS[UnitsPressure.PICOPASCAL][UnitsPressure.DECAPASCAL] = \
        Mul(Rat(Int(1), Pow(10, 13)), Sym("picopa"))  # nopep8
    CONVERSIONS[UnitsPressure.PICOPASCAL][UnitsPressure.DECIBAR] = \
        Mul(Rat(Int(1), Pow(10, 16)), Sym("picopa"))  # nopep8
    CONVERSIONS[UnitsPressure.PICOPASCAL][UnitsPressure.DECIPASCAL] = \
        Mul(Rat(Int(1), Pow(10, 11)), Sym("picopa"))  # nopep8
    CONVERSIONS[UnitsPressure.PICOPASCAL][UnitsPressure.EXAPASCAL] = \
        Mul(Rat(Int(1), Pow(10, 30)), Sym("picopa"))  # nopep8
    CONVERSIONS[UnitsPressure.PICOPASCAL][UnitsPressure.FEMTOPASCAL] = \
        Mul(Int(1000), Sym("picopa"))  # nopep8
    CONVERSIONS[UnitsPressure.PICOPASCAL][UnitsPressure.GIGAPASCAL] = \
        Mul(Rat(Int(1), Pow(10, 21)), Sym("picopa"))  # nopep8
    CONVERSIONS[UnitsPressure.PICOPASCAL][UnitsPressure.HECTOPASCAL] = \
        Mul(Rat(Int(1), Pow(10, 14)), Sym("picopa"))  # nopep8
    CONVERSIONS[UnitsPressure.PICOPASCAL][UnitsPressure.KILOBAR] = \
        Mul(Rat(Int(1), Pow(10, 20)), Sym("picopa"))  # nopep8
    CONVERSIONS[UnitsPressure.PICOPASCAL][UnitsPressure.KILOPASCAL] = \
        Mul(Rat(Int(1), Pow(10, 15)), Sym("picopa"))  # nopep8
    CONVERSIONS[UnitsPressure.PICOPASCAL][UnitsPressure.MEGABAR] = \
        Mul(Rat(Int(1), Pow(10, 23)), Sym("picopa"))  # nopep8
    CONVERSIONS[UnitsPressure.PICOPASCAL][UnitsPressure.MEGAPASCAL] = \
        Mul(Rat(Int(1), Pow(10, 18)), Sym("picopa"))  # nopep8
    CONVERSIONS[UnitsPressure.PICOPASCAL][UnitsPressure.MICROPASCAL] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("picopa"))  # nopep8
    CONVERSIONS[UnitsPressure.PICOPASCAL][UnitsPressure.MILLIBAR] = \
        Mul(Rat(Int(1), Pow(10, 14)), Sym("picopa"))  # nopep8
    CONVERSIONS[UnitsPressure.PICOPASCAL][UnitsPressure.MILLIPASCAL] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("picopa"))  # nopep8
    CONVERSIONS[UnitsPressure.PICOPASCAL][UnitsPressure.MILLITORR] = \
        Mul(Rat(Int(19), Mul(Int(2533125), Pow(10, 12))), Sym("picopa"))  # nopep8
    CONVERSIONS[UnitsPressure.PICOPASCAL][UnitsPressure.MMHG] = \
        Mul(Rat(Int(1), Int("133322387415000")), Sym("picopa"))  # nopep8
    CONVERSIONS[UnitsPressure.PICOPASCAL][UnitsPressure.NANOPASCAL] = \
        Mul(Rat(Int(1), Int(1000)), Sym("picopa"))  # nopep8
    CONVERSIONS[UnitsPressure.PICOPASCAL][UnitsPressure.PETAPASCAL] = \
        Mul(Rat(Int(1), Pow(10, 27)), Sym("picopa"))  # nopep8
    CONVERSIONS[UnitsPressure.PICOPASCAL][UnitsPressure.PSI] = \
        Mul(Rat(Int(1), Int("6894757293168360")), Sym("picopa"))  # nopep8
    CONVERSIONS[UnitsPressure.PICOPASCAL][UnitsPressure.Pascal] = \
        Mul(Rat(Int(1), Pow(10, 12)), Sym("picopa"))  # nopep8
    CONVERSIONS[UnitsPressure.PICOPASCAL][UnitsPressure.TERAPASCAL] = \
        Mul(Rat(Int(1), Pow(10, 24)), Sym("picopa"))  # nopep8
    CONVERSIONS[UnitsPressure.PICOPASCAL][UnitsPressure.TORR] = \
        Mul(Rat(Int(19), Mul(Int(2533125), Pow(10, 9))), Sym("picopa"))  # nopep8
    CONVERSIONS[UnitsPressure.PICOPASCAL][UnitsPressure.YOCTOPASCAL] = \
        Mul(Pow(10, 12), Sym("picopa"))  # nopep8
    CONVERSIONS[UnitsPressure.PICOPASCAL][UnitsPressure.YOTTAPASCAL] = \
        Mul(Rat(Int(1), Pow(10, 36)), Sym("picopa"))  # nopep8
    CONVERSIONS[UnitsPressure.PICOPASCAL][UnitsPressure.ZEPTOPASCAL] = \
        Mul(Pow(10, 9), Sym("picopa"))  # nopep8
    CONVERSIONS[UnitsPressure.PICOPASCAL][UnitsPressure.ZETTAPASCAL] = \
        Mul(Rat(Int(1), Pow(10, 33)), Sym("picopa"))  # nopep8
    CONVERSIONS[UnitsPressure.PSI][UnitsPressure.ATMOSPHERE] = \
        Mul(Rat(Int("8208044396629"), Mul(Int(120625), Pow(10, 9))), Sym("psi"))  # nopep8
    CONVERSIONS[UnitsPressure.PSI][UnitsPressure.ATTOPASCAL] = \
        Mul(Mul(Int("689475729316836"), Pow(10, 7)), Sym("psi"))  # nopep8
    CONVERSIONS[UnitsPressure.PSI][UnitsPressure.BAR] = \
        Mul(Rat(Int("172368932329209"), Mul(Int(25), Pow(10, 14))), Sym("psi"))  # nopep8
    CONVERSIONS[UnitsPressure.PSI][UnitsPressure.CENTIBAR] = \
        Mul(Rat(Int("172368932329209"), Mul(Int(25), Pow(10, 12))), Sym("psi"))  # nopep8
    CONVERSIONS[UnitsPressure.PSI][UnitsPressure.CENTIPASCAL] = \
        Mul(Rat(Int("172368932329209"), Mul(Int(25), Pow(10, 7))), Sym("psi"))  # nopep8
    CONVERSIONS[UnitsPressure.PSI][UnitsPressure.DECAPASCAL] = \
        Mul(Rat(Int("172368932329209"), Mul(Int(25), Pow(10, 10))), Sym("psi"))  # nopep8
    CONVERSIONS[UnitsPressure.PSI][UnitsPressure.DECIBAR] = \
        Mul(Rat(Int("172368932329209"), Mul(Int(25), Pow(10, 13))), Sym("psi"))  # nopep8
    CONVERSIONS[UnitsPressure.PSI][UnitsPressure.DECIPASCAL] = \
        Mul(Rat(Int("172368932329209"), Mul(Int(25), Pow(10, 8))), Sym("psi"))  # nopep8
    CONVERSIONS[UnitsPressure.PSI][UnitsPressure.EXAPASCAL] = \
        Mul(Rat(Int("172368932329209"), Mul(Int(25), Pow(10, 27))), Sym("psi"))  # nopep8
    CONVERSIONS[UnitsPressure.PSI][UnitsPressure.FEMTOPASCAL] = \
        Mul(Mul(Int("689475729316836"), Pow(10, 4)), Sym("psi"))  # nopep8
    CONVERSIONS[UnitsPressure.PSI][UnitsPressure.GIGAPASCAL] = \
        Mul(Rat(Int("172368932329209"), Mul(Int(25), Pow(10, 18))), Sym("psi"))  # nopep8
    CONVERSIONS[UnitsPressure.PSI][UnitsPressure.HECTOPASCAL] = \
        Mul(Rat(Int("172368932329209"), Mul(Int(25), Pow(10, 11))), Sym("psi"))  # nopep8
    CONVERSIONS[UnitsPressure.PSI][UnitsPressure.KILOBAR] = \
        Mul(Rat(Int("172368932329209"), Mul(Int(25), Pow(10, 17))), Sym("psi"))  # nopep8
    CONVERSIONS[UnitsPressure.PSI][UnitsPressure.KILOPASCAL] = \
        Mul(Rat(Int("172368932329209"), Mul(Int(25), Pow(10, 12))), Sym("psi"))  # nopep8
    CONVERSIONS[UnitsPressure.PSI][UnitsPressure.MEGABAR] = \
        Mul(Rat(Int("172368932329209"), Mul(Int(25), Pow(10, 20))), Sym("psi"))  # nopep8
    CONVERSIONS[UnitsPressure.PSI][UnitsPressure.MEGAPASCAL] = \
        Mul(Rat(Int("172368932329209"), Mul(Int(25), Pow(10, 15))), Sym("psi"))  # nopep8
    CONVERSIONS[UnitsPressure.PSI][UnitsPressure.MICROPASCAL] = \
        Mul(Rat(Int("172368932329209"), Int(25000)), Sym("psi"))  # nopep8
    CONVERSIONS[UnitsPressure.PSI][UnitsPressure.MILLIBAR] = \
        Mul(Rat(Int("172368932329209"), Mul(Int(25), Pow(10, 11))), Sym("psi"))  # nopep8
    CONVERSIONS[UnitsPressure.PSI][UnitsPressure.MILLIPASCAL] = \
        Mul(Rat(Int("172368932329209"), Mul(Int(25), Pow(10, 6))), Sym("psi"))  # nopep8
    CONVERSIONS[UnitsPressure.PSI][UnitsPressure.MILLITORR] = \
        Mul(Rat(Int("155952843535951"), Mul(Int(3015625), Pow(10, 9))), Sym("psi"))  # nopep8
    CONVERSIONS[UnitsPressure.PSI][UnitsPressure.MMHG] = \
        Mul(Rat(Int("8208044396629"), Int("158717127875")), Sym("psi"))  # nopep8
    CONVERSIONS[UnitsPressure.PSI][UnitsPressure.NANOPASCAL] = \
        Mul(Rat(Int("172368932329209"), Int(25)), Sym("psi"))  # nopep8
    CONVERSIONS[UnitsPressure.PSI][UnitsPressure.PETAPASCAL] = \
        Mul(Rat(Int("172368932329209"), Mul(Int(25), Pow(10, 24))), Sym("psi"))  # nopep8
    CONVERSIONS[UnitsPressure.PSI][UnitsPressure.PICOPASCAL] = \
        Mul(Int("6894757293168360"), Sym("psi"))  # nopep8
    CONVERSIONS[UnitsPressure.PSI][UnitsPressure.Pascal] = \
        Mul(Rat(Int("172368932329209"), Mul(Int(25), Pow(10, 9))), Sym("psi"))  # nopep8
    CONVERSIONS[UnitsPressure.PSI][UnitsPressure.TERAPASCAL] = \
        Mul(Rat(Int("172368932329209"), Mul(Int(25), Pow(10, 21))), Sym("psi"))  # nopep8
    CONVERSIONS[UnitsPressure.PSI][UnitsPressure.TORR] = \
        Mul(Rat(Int("155952843535951"), Mul(Int(3015625), Pow(10, 6))), Sym("psi"))  # nopep8
    CONVERSIONS[UnitsPressure.PSI][UnitsPressure.YOCTOPASCAL] = \
        Mul(Mul(Int("689475729316836"), Pow(10, 13)), Sym("psi"))  # nopep8
    CONVERSIONS[UnitsPressure.PSI][UnitsPressure.YOTTAPASCAL] = \
        Mul(Rat(Int("172368932329209"), Mul(Int(25), Pow(10, 33))), Sym("psi"))  # nopep8
    CONVERSIONS[UnitsPressure.PSI][UnitsPressure.ZEPTOPASCAL] = \
        Mul(Mul(Int("689475729316836"), Pow(10, 10)), Sym("psi"))  # nopep8
    CONVERSIONS[UnitsPressure.PSI][UnitsPressure.ZETTAPASCAL] = \
        Mul(Rat(Int("172368932329209"), Mul(Int(25), Pow(10, 30))), Sym("psi"))  # nopep8
    CONVERSIONS[UnitsPressure.Pascal][UnitsPressure.ATMOSPHERE] = \
        Mul(Rat(Int(1), Int(101325)), Sym("pa"))  # nopep8
    CONVERSIONS[UnitsPressure.Pascal][UnitsPressure.ATTOPASCAL] = \
        Mul(Pow(10, 18), Sym("pa"))  # nopep8
    CONVERSIONS[UnitsPressure.Pascal][UnitsPressure.BAR] = \
        Mul(Rat(Int(1), Pow(10, 5)), Sym("pa"))  # nopep8
    CONVERSIONS[UnitsPressure.Pascal][UnitsPressure.CENTIBAR] = \
        Mul(Rat(Int(1), Int(1000)), Sym("pa"))  # nopep8
    CONVERSIONS[UnitsPressure.Pascal][UnitsPressure.CENTIPASCAL] = \
        Mul(Int(100), Sym("pa"))  # nopep8
    CONVERSIONS[UnitsPressure.Pascal][UnitsPressure.DECAPASCAL] = \
        Mul(Rat(Int(1), Int(10)), Sym("pa"))  # nopep8
    CONVERSIONS[UnitsPressure.Pascal][UnitsPressure.DECIBAR] = \
        Mul(Rat(Int(1), Pow(10, 4)), Sym("pa"))  # nopep8
    CONVERSIONS[UnitsPressure.Pascal][UnitsPressure.DECIPASCAL] = \
        Mul(Int(10), Sym("pa"))  # nopep8
    CONVERSIONS[UnitsPressure.Pascal][UnitsPressure.EXAPASCAL] = \
        Mul(Rat(Int(1), Pow(10, 18)), Sym("pa"))  # nopep8
    CONVERSIONS[UnitsPressure.Pascal][UnitsPressure.FEMTOPASCAL] = \
        Mul(Pow(10, 15), Sym("pa"))  # nopep8
    CONVERSIONS[UnitsPressure.Pascal][UnitsPressure.GIGAPASCAL] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("pa"))  # nopep8
    CONVERSIONS[UnitsPressure.Pascal][UnitsPressure.HECTOPASCAL] = \
        Mul(Rat(Int(1), Int(100)), Sym("pa"))  # nopep8
    CONVERSIONS[UnitsPressure.Pascal][UnitsPressure.KILOBAR] = \
        Mul(Rat(Int(1), Pow(10, 8)), Sym("pa"))  # nopep8
    CONVERSIONS[UnitsPressure.Pascal][UnitsPressure.KILOPASCAL] = \
        Mul(Rat(Int(1), Int(1000)), Sym("pa"))  # nopep8
    CONVERSIONS[UnitsPressure.Pascal][UnitsPressure.MEGABAR] = \
        Mul(Rat(Int(1), Pow(10, 11)), Sym("pa"))  # nopep8
    CONVERSIONS[UnitsPressure.Pascal][UnitsPressure.MEGAPASCAL] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("pa"))  # nopep8
    CONVERSIONS[UnitsPressure.Pascal][UnitsPressure.MICROPASCAL] = \
        Mul(Pow(10, 6), Sym("pa"))  # nopep8
    CONVERSIONS[UnitsPressure.Pascal][UnitsPressure.MILLIBAR] = \
        Mul(Rat(Int(1), Int(100)), Sym("pa"))  # nopep8
    CONVERSIONS[UnitsPressure.Pascal][UnitsPressure.MILLIPASCAL] = \
        Mul(Int(1000), Sym("pa"))  # nopep8
    CONVERSIONS[UnitsPressure.Pascal][UnitsPressure.MILLITORR] = \
        Mul(Rat(Int(19), Int(2533125)), Sym("pa"))  # nopep8
    CONVERSIONS[UnitsPressure.Pascal][UnitsPressure.MMHG] = \
        Mul(Rat(Mul(Int(2), Pow(10, 8)), Int("26664477483")), Sym("pa"))  # nopep8
    CONVERSIONS[UnitsPressure.Pascal][UnitsPressure.NANOPASCAL] = \
        Mul(Pow(10, 9), Sym("pa"))  # nopep8
    CONVERSIONS[UnitsPressure.Pascal][UnitsPressure.PETAPASCAL] = \
        Mul(Rat(Int(1), Pow(10, 15)), Sym("pa"))  # nopep8
    CONVERSIONS[UnitsPressure.Pascal][UnitsPressure.PICOPASCAL] = \
        Mul(Pow(10, 12), Sym("pa"))  # nopep8
    CONVERSIONS[UnitsPressure.Pascal][UnitsPressure.PSI] = \
        Mul(Rat(Mul(Int(25), Pow(10, 9)), Int("172368932329209")), Sym("pa"))  # nopep8
    CONVERSIONS[UnitsPressure.Pascal][UnitsPressure.TERAPASCAL] = \
        Mul(Rat(Int(1), Pow(10, 12)), Sym("pa"))  # nopep8
    CONVERSIONS[UnitsPressure.Pascal][UnitsPressure.TORR] = \
        Mul(Rat(Int(152), Int(20265)), Sym("pa"))  # nopep8
    CONVERSIONS[UnitsPressure.Pascal][UnitsPressure.YOCTOPASCAL] = \
        Mul(Pow(10, 24), Sym("pa"))  # nopep8
    CONVERSIONS[UnitsPressure.Pascal][UnitsPressure.YOTTAPASCAL] = \
        Mul(Rat(Int(1), Pow(10, 24)), Sym("pa"))  # nopep8
    CONVERSIONS[UnitsPressure.Pascal][UnitsPressure.ZEPTOPASCAL] = \
        Mul(Pow(10, 21), Sym("pa"))  # nopep8
    CONVERSIONS[UnitsPressure.Pascal][UnitsPressure.ZETTAPASCAL] = \
        Mul(Rat(Int(1), Pow(10, 21)), Sym("pa"))  # nopep8
    CONVERSIONS[UnitsPressure.TERAPASCAL][UnitsPressure.ATMOSPHERE] = \
        Mul(Rat(Mul(Int(4), Pow(10, 10)), Int(4053)), Sym("terapa"))  # nopep8
    CONVERSIONS[UnitsPressure.TERAPASCAL][UnitsPressure.ATTOPASCAL] = \
        Mul(Pow(10, 30), Sym("terapa"))  # nopep8
    CONVERSIONS[UnitsPressure.TERAPASCAL][UnitsPressure.BAR] = \
        Mul(Pow(10, 7), Sym("terapa"))  # nopep8
    CONVERSIONS[UnitsPressure.TERAPASCAL][UnitsPressure.CENTIBAR] = \
        Mul(Pow(10, 9), Sym("terapa"))  # nopep8
    CONVERSIONS[UnitsPressure.TERAPASCAL][UnitsPressure.CENTIPASCAL] = \
        Mul(Pow(10, 14), Sym("terapa"))  # nopep8
    CONVERSIONS[UnitsPressure.TERAPASCAL][UnitsPressure.DECAPASCAL] = \
        Mul(Pow(10, 11), Sym("terapa"))  # nopep8
    CONVERSIONS[UnitsPressure.TERAPASCAL][UnitsPressure.DECIBAR] = \
        Mul(Pow(10, 8), Sym("terapa"))  # nopep8
    CONVERSIONS[UnitsPressure.TERAPASCAL][UnitsPressure.DECIPASCAL] = \
        Mul(Pow(10, 13), Sym("terapa"))  # nopep8
    CONVERSIONS[UnitsPressure.TERAPASCAL][UnitsPressure.EXAPASCAL] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("terapa"))  # nopep8
    CONVERSIONS[UnitsPressure.TERAPASCAL][UnitsPressure.FEMTOPASCAL] = \
        Mul(Pow(10, 27), Sym("terapa"))  # nopep8
    CONVERSIONS[UnitsPressure.TERAPASCAL][UnitsPressure.GIGAPASCAL] = \
        Mul(Int(1000), Sym("terapa"))  # nopep8
    CONVERSIONS[UnitsPressure.TERAPASCAL][UnitsPressure.HECTOPASCAL] = \
        Mul(Pow(10, 10), Sym("terapa"))  # nopep8
    CONVERSIONS[UnitsPressure.TERAPASCAL][UnitsPressure.KILOBAR] = \
        Mul(Pow(10, 4), Sym("terapa"))  # nopep8
    CONVERSIONS[UnitsPressure.TERAPASCAL][UnitsPressure.KILOPASCAL] = \
        Mul(Pow(10, 9), Sym("terapa"))  # nopep8
    CONVERSIONS[UnitsPressure.TERAPASCAL][UnitsPressure.MEGABAR] = \
        Mul(Int(10), Sym("terapa"))  # nopep8
    CONVERSIONS[UnitsPressure.TERAPASCAL][UnitsPressure.MEGAPASCAL] = \
        Mul(Pow(10, 6), Sym("terapa"))  # nopep8
    CONVERSIONS[UnitsPressure.TERAPASCAL][UnitsPressure.MICROPASCAL] = \
        Mul(Pow(10, 18), Sym("terapa"))  # nopep8
    CONVERSIONS[UnitsPressure.TERAPASCAL][UnitsPressure.MILLIBAR] = \
        Mul(Pow(10, 10), Sym("terapa"))  # nopep8
    CONVERSIONS[UnitsPressure.TERAPASCAL][UnitsPressure.MILLIPASCAL] = \
        Mul(Pow(10, 15), Sym("terapa"))  # nopep8
    CONVERSIONS[UnitsPressure.TERAPASCAL][UnitsPressure.MILLITORR] = \
        Mul(Rat(Mul(Int(304), Pow(10, 8)), Int(4053)), Sym("terapa"))  # nopep8
    CONVERSIONS[UnitsPressure.TERAPASCAL][UnitsPressure.MMHG] = \
        Mul(Rat(Mul(Int(2), Pow(10, 20)), Int("26664477483")), Sym("terapa"))  # nopep8
    CONVERSIONS[UnitsPressure.TERAPASCAL][UnitsPressure.NANOPASCAL] = \
        Mul(Pow(10, 21), Sym("terapa"))  # nopep8
    CONVERSIONS[UnitsPressure.TERAPASCAL][UnitsPressure.PETAPASCAL] = \
        Mul(Rat(Int(1), Int(1000)), Sym("terapa"))  # nopep8
    CONVERSIONS[UnitsPressure.TERAPASCAL][UnitsPressure.PICOPASCAL] = \
        Mul(Pow(10, 24), Sym("terapa"))  # nopep8
    CONVERSIONS[UnitsPressure.TERAPASCAL][UnitsPressure.PSI] = \
        Mul(Rat(Mul(Int(25), Pow(10, 21)), Int("172368932329209")), Sym("terapa"))  # nopep8
    CONVERSIONS[UnitsPressure.TERAPASCAL][UnitsPressure.Pascal] = \
        Mul(Pow(10, 12), Sym("terapa"))  # nopep8
    CONVERSIONS[UnitsPressure.TERAPASCAL][UnitsPressure.TORR] = \
        Mul(Rat(Mul(Int(304), Pow(10, 11)), Int(4053)), Sym("terapa"))  # nopep8
    CONVERSIONS[UnitsPressure.TERAPASCAL][UnitsPressure.YOCTOPASCAL] = \
        Mul(Pow(10, 36), Sym("terapa"))  # nopep8
    CONVERSIONS[UnitsPressure.TERAPASCAL][UnitsPressure.YOTTAPASCAL] = \
        Mul(Rat(Int(1), Pow(10, 12)), Sym("terapa"))  # nopep8
    CONVERSIONS[UnitsPressure.TERAPASCAL][UnitsPressure.ZEPTOPASCAL] = \
        Mul(Pow(10, 33), Sym("terapa"))  # nopep8
    CONVERSIONS[UnitsPressure.TERAPASCAL][UnitsPressure.ZETTAPASCAL] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("terapa"))  # nopep8
    CONVERSIONS[UnitsPressure.TORR][UnitsPressure.ATMOSPHERE] = \
        Mul(Rat(Int(1), Int(760)), Sym("torr"))  # nopep8
    CONVERSIONS[UnitsPressure.TORR][UnitsPressure.ATTOPASCAL] = \
        Mul(Rat(Mul(Int(2533125), Pow(10, 15)), Int(19)), Sym("torr"))  # nopep8
    CONVERSIONS[UnitsPressure.TORR][UnitsPressure.BAR] = \
        Mul(Rat(Int(4053), Mul(Int(304), Pow(10, 4))), Sym("torr"))  # nopep8
    CONVERSIONS[UnitsPressure.TORR][UnitsPressure.CENTIBAR] = \
        Mul(Rat(Int(4053), Int(30400)), Sym("torr"))  # nopep8
    CONVERSIONS[UnitsPressure.TORR][UnitsPressure.CENTIPASCAL] = \
        Mul(Rat(Int(506625), Int(38)), Sym("torr"))  # nopep8
    CONVERSIONS[UnitsPressure.TORR][UnitsPressure.DECAPASCAL] = \
        Mul(Rat(Int(4053), Int(304)), Sym("torr"))  # nopep8
    CONVERSIONS[UnitsPressure.TORR][UnitsPressure.DECIBAR] = \
        Mul(Rat(Int(4053), Int(304000)), Sym("torr"))  # nopep8
    CONVERSIONS[UnitsPressure.TORR][UnitsPressure.DECIPASCAL] = \
        Mul(Rat(Int(101325), Int(76)), Sym("torr"))  # nopep8
    CONVERSIONS[UnitsPressure.TORR][UnitsPressure.EXAPASCAL] = \
        Mul(Rat(Int(4053), Mul(Int(304), Pow(10, 17))), Sym("torr"))  # nopep8
    CONVERSIONS[UnitsPressure.TORR][UnitsPressure.FEMTOPASCAL] = \
        Mul(Rat(Mul(Int(2533125), Pow(10, 12)), Int(19)), Sym("torr"))  # nopep8
    CONVERSIONS[UnitsPressure.TORR][UnitsPressure.GIGAPASCAL] = \
        Mul(Rat(Int(4053), Mul(Int(304), Pow(10, 8))), Sym("torr"))  # nopep8
    CONVERSIONS[UnitsPressure.TORR][UnitsPressure.HECTOPASCAL] = \
        Mul(Rat(Int(4053), Int(3040)), Sym("torr"))  # nopep8
    CONVERSIONS[UnitsPressure.TORR][UnitsPressure.KILOBAR] = \
        Mul(Rat(Int(4053), Mul(Int(304), Pow(10, 7))), Sym("torr"))  # nopep8
    CONVERSIONS[UnitsPressure.TORR][UnitsPressure.KILOPASCAL] = \
        Mul(Rat(Int(4053), Int(30400)), Sym("torr"))  # nopep8
    CONVERSIONS[UnitsPressure.TORR][UnitsPressure.MEGABAR] = \
        Mul(Rat(Int(4053), Mul(Int(304), Pow(10, 10))), Sym("torr"))  # nopep8
    CONVERSIONS[UnitsPressure.TORR][UnitsPressure.MEGAPASCAL] = \
        Mul(Rat(Int(4053), Mul(Int(304), Pow(10, 5))), Sym("torr"))  # nopep8
    CONVERSIONS[UnitsPressure.TORR][UnitsPressure.MICROPASCAL] = \
        Mul(Rat(Int("2533125000"), Int(19)), Sym("torr"))  # nopep8
    CONVERSIONS[UnitsPressure.TORR][UnitsPressure.MILLIBAR] = \
        Mul(Rat(Int(4053), Int(3040)), Sym("torr"))  # nopep8
    CONVERSIONS[UnitsPressure.TORR][UnitsPressure.MILLIPASCAL] = \
        Mul(Rat(Int(2533125), Int(19)), Sym("torr"))  # nopep8
    CONVERSIONS[UnitsPressure.TORR][UnitsPressure.MILLITORR] = \
        Mul(Rat(Int(1), Int(1000)), Sym("torr"))  # nopep8
    CONVERSIONS[UnitsPressure.TORR][UnitsPressure.MMHG] = \
        Mul(Rat(Mul(Int(24125), Pow(10, 6)), Int("24125003437")), Sym("torr"))  # nopep8
    CONVERSIONS[UnitsPressure.TORR][UnitsPressure.NANOPASCAL] = \
        Mul(Rat(Mul(Int(2533125), Pow(10, 6)), Int(19)), Sym("torr"))  # nopep8
    CONVERSIONS[UnitsPressure.TORR][UnitsPressure.PETAPASCAL] = \
        Mul(Rat(Int(4053), Mul(Int(304), Pow(10, 14))), Sym("torr"))  # nopep8
    CONVERSIONS[UnitsPressure.TORR][UnitsPressure.PICOPASCAL] = \
        Mul(Rat(Mul(Int(2533125), Pow(10, 9)), Int(19)), Sym("torr"))  # nopep8
    CONVERSIONS[UnitsPressure.TORR][UnitsPressure.PSI] = \
        Mul(Rat(Mul(Int(3015625), Pow(10, 6)), Int("155952843535951")), Sym("torr"))  # nopep8
    CONVERSIONS[UnitsPressure.TORR][UnitsPressure.Pascal] = \
        Mul(Rat(Int(20265), Int(152)), Sym("torr"))  # nopep8
    CONVERSIONS[UnitsPressure.TORR][UnitsPressure.TERAPASCAL] = \
        Mul(Rat(Int(4053), Mul(Int(304), Pow(10, 11))), Sym("torr"))  # nopep8
    CONVERSIONS[UnitsPressure.TORR][UnitsPressure.YOCTOPASCAL] = \
        Mul(Rat(Mul(Int(2533125), Pow(10, 21)), Int(19)), Sym("torr"))  # nopep8
    CONVERSIONS[UnitsPressure.TORR][UnitsPressure.YOTTAPASCAL] = \
        Mul(Rat(Int(4053), Mul(Int(304), Pow(10, 23))), Sym("torr"))  # nopep8
    CONVERSIONS[UnitsPressure.TORR][UnitsPressure.ZEPTOPASCAL] = \
        Mul(Rat(Mul(Int(2533125), Pow(10, 18)), Int(19)), Sym("torr"))  # nopep8
    CONVERSIONS[UnitsPressure.TORR][UnitsPressure.ZETTAPASCAL] = \
        Mul(Rat(Int(4053), Mul(Int(304), Pow(10, 20))), Sym("torr"))  # nopep8
    CONVERSIONS[UnitsPressure.YOCTOPASCAL][UnitsPressure.ATMOSPHERE] = \
        Mul(Rat(Int(1), Mul(Int(101325), Pow(10, 24))), Sym("yoctopa"))  # nopep8
    CONVERSIONS[UnitsPressure.YOCTOPASCAL][UnitsPressure.ATTOPASCAL] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("yoctopa"))  # nopep8
    CONVERSIONS[UnitsPressure.YOCTOPASCAL][UnitsPressure.BAR] = \
        Mul(Rat(Int(1), Pow(10, 29)), Sym("yoctopa"))  # nopep8
    CONVERSIONS[UnitsPressure.YOCTOPASCAL][UnitsPressure.CENTIBAR] = \
        Mul(Rat(Int(1), Pow(10, 27)), Sym("yoctopa"))  # nopep8
    CONVERSIONS[UnitsPressure.YOCTOPASCAL][UnitsPressure.CENTIPASCAL] = \
        Mul(Rat(Int(1), Pow(10, 22)), Sym("yoctopa"))  # nopep8
    CONVERSIONS[UnitsPressure.YOCTOPASCAL][UnitsPressure.DECAPASCAL] = \
        Mul(Rat(Int(1), Pow(10, 25)), Sym("yoctopa"))  # nopep8
    CONVERSIONS[UnitsPressure.YOCTOPASCAL][UnitsPressure.DECIBAR] = \
        Mul(Rat(Int(1), Pow(10, 28)), Sym("yoctopa"))  # nopep8
    CONVERSIONS[UnitsPressure.YOCTOPASCAL][UnitsPressure.DECIPASCAL] = \
        Mul(Rat(Int(1), Pow(10, 23)), Sym("yoctopa"))  # nopep8
    CONVERSIONS[UnitsPressure.YOCTOPASCAL][UnitsPressure.EXAPASCAL] = \
        Mul(Rat(Int(1), Pow(10, 42)), Sym("yoctopa"))  # nopep8
    CONVERSIONS[UnitsPressure.YOCTOPASCAL][UnitsPressure.FEMTOPASCAL] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("yoctopa"))  # nopep8
    CONVERSIONS[UnitsPressure.YOCTOPASCAL][UnitsPressure.GIGAPASCAL] = \
        Mul(Rat(Int(1), Pow(10, 33)), Sym("yoctopa"))  # nopep8
    CONVERSIONS[UnitsPressure.YOCTOPASCAL][UnitsPressure.HECTOPASCAL] = \
        Mul(Rat(Int(1), Pow(10, 26)), Sym("yoctopa"))  # nopep8
    CONVERSIONS[UnitsPressure.YOCTOPASCAL][UnitsPressure.KILOBAR] = \
        Mul(Rat(Int(1), Pow(10, 32)), Sym("yoctopa"))  # nopep8
    CONVERSIONS[UnitsPressure.YOCTOPASCAL][UnitsPressure.KILOPASCAL] = \
        Mul(Rat(Int(1), Pow(10, 27)), Sym("yoctopa"))  # nopep8
    CONVERSIONS[UnitsPressure.YOCTOPASCAL][UnitsPressure.MEGABAR] = \
        Mul(Rat(Int(1), Pow(10, 35)), Sym("yoctopa"))  # nopep8
    CONVERSIONS[UnitsPressure.YOCTOPASCAL][UnitsPressure.MEGAPASCAL] = \
        Mul(Rat(Int(1), Pow(10, 30)), Sym("yoctopa"))  # nopep8
    CONVERSIONS[UnitsPressure.YOCTOPASCAL][UnitsPressure.MICROPASCAL] = \
        Mul(Rat(Int(1), Pow(10, 18)), Sym("yoctopa"))  # nopep8
    CONVERSIONS[UnitsPressure.YOCTOPASCAL][UnitsPressure.MILLIBAR] = \
        Mul(Rat(Int(1), Pow(10, 26)), Sym("yoctopa"))  # nopep8
    CONVERSIONS[UnitsPressure.YOCTOPASCAL][UnitsPressure.MILLIPASCAL] = \
        Mul(Rat(Int(1), Pow(10, 21)), Sym("yoctopa"))  # nopep8
    CONVERSIONS[UnitsPressure.YOCTOPASCAL][UnitsPressure.MILLITORR] = \
        Mul(Rat(Int(19), Mul(Int(2533125), Pow(10, 24))), Sym("yoctopa"))  # nopep8
    CONVERSIONS[UnitsPressure.YOCTOPASCAL][UnitsPressure.MMHG] = \
        Mul(Rat(Int(1), Mul(Int("133322387415"), Pow(10, 15))), Sym("yoctopa"))  # nopep8
    CONVERSIONS[UnitsPressure.YOCTOPASCAL][UnitsPressure.NANOPASCAL] = \
        Mul(Rat(Int(1), Pow(10, 15)), Sym("yoctopa"))  # nopep8
    CONVERSIONS[UnitsPressure.YOCTOPASCAL][UnitsPressure.PETAPASCAL] = \
        Mul(Rat(Int(1), Pow(10, 39)), Sym("yoctopa"))  # nopep8
    CONVERSIONS[UnitsPressure.YOCTOPASCAL][UnitsPressure.PICOPASCAL] = \
        Mul(Rat(Int(1), Pow(10, 12)), Sym("yoctopa"))  # nopep8
    CONVERSIONS[UnitsPressure.YOCTOPASCAL][UnitsPressure.PSI] = \
        Mul(Rat(Int(1), Mul(Int("689475729316836"), Pow(10, 13))), Sym("yoctopa"))  # nopep8
    CONVERSIONS[UnitsPressure.YOCTOPASCAL][UnitsPressure.Pascal] = \
        Mul(Rat(Int(1), Pow(10, 24)), Sym("yoctopa"))  # nopep8
    CONVERSIONS[UnitsPressure.YOCTOPASCAL][UnitsPressure.TERAPASCAL] = \
        Mul(Rat(Int(1), Pow(10, 36)), Sym("yoctopa"))  # nopep8
    CONVERSIONS[UnitsPressure.YOCTOPASCAL][UnitsPressure.TORR] = \
        Mul(Rat(Int(19), Mul(Int(2533125), Pow(10, 21))), Sym("yoctopa"))  # nopep8
    CONVERSIONS[UnitsPressure.YOCTOPASCAL][UnitsPressure.YOTTAPASCAL] = \
        Mul(Rat(Int(1), Pow(10, 48)), Sym("yoctopa"))  # nopep8
    CONVERSIONS[UnitsPressure.YOCTOPASCAL][UnitsPressure.ZEPTOPASCAL] = \
        Mul(Rat(Int(1), Int(1000)), Sym("yoctopa"))  # nopep8
    CONVERSIONS[UnitsPressure.YOCTOPASCAL][UnitsPressure.ZETTAPASCAL] = \
        Mul(Rat(Int(1), Pow(10, 45)), Sym("yoctopa"))  # nopep8
    CONVERSIONS[UnitsPressure.YOTTAPASCAL][UnitsPressure.ATMOSPHERE] = \
        Mul(Rat(Mul(Int(4), Pow(10, 22)), Int(4053)), Sym("yottapa"))  # nopep8
    CONVERSIONS[UnitsPressure.YOTTAPASCAL][UnitsPressure.ATTOPASCAL] = \
        Mul(Pow(10, 42), Sym("yottapa"))  # nopep8
    CONVERSIONS[UnitsPressure.YOTTAPASCAL][UnitsPressure.BAR] = \
        Mul(Pow(10, 19), Sym("yottapa"))  # nopep8
    CONVERSIONS[UnitsPressure.YOTTAPASCAL][UnitsPressure.CENTIBAR] = \
        Mul(Pow(10, 21), Sym("yottapa"))  # nopep8
    CONVERSIONS[UnitsPressure.YOTTAPASCAL][UnitsPressure.CENTIPASCAL] = \
        Mul(Pow(10, 26), Sym("yottapa"))  # nopep8
    CONVERSIONS[UnitsPressure.YOTTAPASCAL][UnitsPressure.DECAPASCAL] = \
        Mul(Pow(10, 23), Sym("yottapa"))  # nopep8
    CONVERSIONS[UnitsPressure.YOTTAPASCAL][UnitsPressure.DECIBAR] = \
        Mul(Pow(10, 20), Sym("yottapa"))  # nopep8
    CONVERSIONS[UnitsPressure.YOTTAPASCAL][UnitsPressure.DECIPASCAL] = \
        Mul(Pow(10, 25), Sym("yottapa"))  # nopep8
    CONVERSIONS[UnitsPressure.YOTTAPASCAL][UnitsPressure.EXAPASCAL] = \
        Mul(Pow(10, 6), Sym("yottapa"))  # nopep8
    CONVERSIONS[UnitsPressure.YOTTAPASCAL][UnitsPressure.FEMTOPASCAL] = \
        Mul(Pow(10, 39), Sym("yottapa"))  # nopep8
    CONVERSIONS[UnitsPressure.YOTTAPASCAL][UnitsPressure.GIGAPASCAL] = \
        Mul(Pow(10, 15), Sym("yottapa"))  # nopep8
    CONVERSIONS[UnitsPressure.YOTTAPASCAL][UnitsPressure.HECTOPASCAL] = \
        Mul(Pow(10, 22), Sym("yottapa"))  # nopep8
    CONVERSIONS[UnitsPressure.YOTTAPASCAL][UnitsPressure.KILOBAR] = \
        Mul(Pow(10, 16), Sym("yottapa"))  # nopep8
    CONVERSIONS[UnitsPressure.YOTTAPASCAL][UnitsPressure.KILOPASCAL] = \
        Mul(Pow(10, 21), Sym("yottapa"))  # nopep8
    CONVERSIONS[UnitsPressure.YOTTAPASCAL][UnitsPressure.MEGABAR] = \
        Mul(Pow(10, 13), Sym("yottapa"))  # nopep8
    CONVERSIONS[UnitsPressure.YOTTAPASCAL][UnitsPressure.MEGAPASCAL] = \
        Mul(Pow(10, 18), Sym("yottapa"))  # nopep8
    CONVERSIONS[UnitsPressure.YOTTAPASCAL][UnitsPressure.MICROPASCAL] = \
        Mul(Pow(10, 30), Sym("yottapa"))  # nopep8
    CONVERSIONS[UnitsPressure.YOTTAPASCAL][UnitsPressure.MILLIBAR] = \
        Mul(Pow(10, 22), Sym("yottapa"))  # nopep8
    CONVERSIONS[UnitsPressure.YOTTAPASCAL][UnitsPressure.MILLIPASCAL] = \
        Mul(Pow(10, 27), Sym("yottapa"))  # nopep8
    CONVERSIONS[UnitsPressure.YOTTAPASCAL][UnitsPressure.MILLITORR] = \
        Mul(Rat(Mul(Int(304), Pow(10, 20)), Int(4053)), Sym("yottapa"))  # nopep8
    CONVERSIONS[UnitsPressure.YOTTAPASCAL][UnitsPressure.MMHG] = \
        Mul(Rat(Mul(Int(2), Pow(10, 32)), Int("26664477483")), Sym("yottapa"))  # nopep8
    CONVERSIONS[UnitsPressure.YOTTAPASCAL][UnitsPressure.NANOPASCAL] = \
        Mul(Pow(10, 33), Sym("yottapa"))  # nopep8
    CONVERSIONS[UnitsPressure.YOTTAPASCAL][UnitsPressure.PETAPASCAL] = \
        Mul(Pow(10, 9), Sym("yottapa"))  # nopep8
    CONVERSIONS[UnitsPressure.YOTTAPASCAL][UnitsPressure.PICOPASCAL] = \
        Mul(Pow(10, 36), Sym("yottapa"))  # nopep8
    CONVERSIONS[UnitsPressure.YOTTAPASCAL][UnitsPressure.PSI] = \
        Mul(Rat(Mul(Int(25), Pow(10, 33)), Int("172368932329209")), Sym("yottapa"))  # nopep8
    CONVERSIONS[UnitsPressure.YOTTAPASCAL][UnitsPressure.Pascal] = \
        Mul(Pow(10, 24), Sym("yottapa"))  # nopep8
    CONVERSIONS[UnitsPressure.YOTTAPASCAL][UnitsPressure.TERAPASCAL] = \
        Mul(Pow(10, 12), Sym("yottapa"))  # nopep8
    CONVERSIONS[UnitsPressure.YOTTAPASCAL][UnitsPressure.TORR] = \
        Mul(Rat(Mul(Int(304), Pow(10, 23)), Int(4053)), Sym("yottapa"))  # nopep8
    CONVERSIONS[UnitsPressure.YOTTAPASCAL][UnitsPressure.YOCTOPASCAL] = \
        Mul(Pow(10, 48), Sym("yottapa"))  # nopep8
    CONVERSIONS[UnitsPressure.YOTTAPASCAL][UnitsPressure.ZEPTOPASCAL] = \
        Mul(Pow(10, 45), Sym("yottapa"))  # nopep8
    CONVERSIONS[UnitsPressure.YOTTAPASCAL][UnitsPressure.ZETTAPASCAL] = \
        Mul(Int(1000), Sym("yottapa"))  # nopep8
    CONVERSIONS[UnitsPressure.ZEPTOPASCAL][UnitsPressure.ATMOSPHERE] = \
        Mul(Rat(Int(1), Mul(Int(101325), Pow(10, 21))), Sym("zeptopa"))  # nopep8
    CONVERSIONS[UnitsPressure.ZEPTOPASCAL][UnitsPressure.ATTOPASCAL] = \
        Mul(Rat(Int(1), Int(1000)), Sym("zeptopa"))  # nopep8
    CONVERSIONS[UnitsPressure.ZEPTOPASCAL][UnitsPressure.BAR] = \
        Mul(Rat(Int(1), Pow(10, 26)), Sym("zeptopa"))  # nopep8
    CONVERSIONS[UnitsPressure.ZEPTOPASCAL][UnitsPressure.CENTIBAR] = \
        Mul(Rat(Int(1), Pow(10, 24)), Sym("zeptopa"))  # nopep8
    CONVERSIONS[UnitsPressure.ZEPTOPASCAL][UnitsPressure.CENTIPASCAL] = \
        Mul(Rat(Int(1), Pow(10, 19)), Sym("zeptopa"))  # nopep8
    CONVERSIONS[UnitsPressure.ZEPTOPASCAL][UnitsPressure.DECAPASCAL] = \
        Mul(Rat(Int(1), Pow(10, 22)), Sym("zeptopa"))  # nopep8
    CONVERSIONS[UnitsPressure.ZEPTOPASCAL][UnitsPressure.DECIBAR] = \
        Mul(Rat(Int(1), Pow(10, 25)), Sym("zeptopa"))  # nopep8
    CONVERSIONS[UnitsPressure.ZEPTOPASCAL][UnitsPressure.DECIPASCAL] = \
        Mul(Rat(Int(1), Pow(10, 20)), Sym("zeptopa"))  # nopep8
    CONVERSIONS[UnitsPressure.ZEPTOPASCAL][UnitsPressure.EXAPASCAL] = \
        Mul(Rat(Int(1), Pow(10, 39)), Sym("zeptopa"))  # nopep8
    CONVERSIONS[UnitsPressure.ZEPTOPASCAL][UnitsPressure.FEMTOPASCAL] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("zeptopa"))  # nopep8
    CONVERSIONS[UnitsPressure.ZEPTOPASCAL][UnitsPressure.GIGAPASCAL] = \
        Mul(Rat(Int(1), Pow(10, 30)), Sym("zeptopa"))  # nopep8
    CONVERSIONS[UnitsPressure.ZEPTOPASCAL][UnitsPressure.HECTOPASCAL] = \
        Mul(Rat(Int(1), Pow(10, 23)), Sym("zeptopa"))  # nopep8
    CONVERSIONS[UnitsPressure.ZEPTOPASCAL][UnitsPressure.KILOBAR] = \
        Mul(Rat(Int(1), Pow(10, 29)), Sym("zeptopa"))  # nopep8
    CONVERSIONS[UnitsPressure.ZEPTOPASCAL][UnitsPressure.KILOPASCAL] = \
        Mul(Rat(Int(1), Pow(10, 24)), Sym("zeptopa"))  # nopep8
    CONVERSIONS[UnitsPressure.ZEPTOPASCAL][UnitsPressure.MEGABAR] = \
        Mul(Rat(Int(1), Pow(10, 32)), Sym("zeptopa"))  # nopep8
    CONVERSIONS[UnitsPressure.ZEPTOPASCAL][UnitsPressure.MEGAPASCAL] = \
        Mul(Rat(Int(1), Pow(10, 27)), Sym("zeptopa"))  # nopep8
    CONVERSIONS[UnitsPressure.ZEPTOPASCAL][UnitsPressure.MICROPASCAL] = \
        Mul(Rat(Int(1), Pow(10, 15)), Sym("zeptopa"))  # nopep8
    CONVERSIONS[UnitsPressure.ZEPTOPASCAL][UnitsPressure.MILLIBAR] = \
        Mul(Rat(Int(1), Pow(10, 23)), Sym("zeptopa"))  # nopep8
    CONVERSIONS[UnitsPressure.ZEPTOPASCAL][UnitsPressure.MILLIPASCAL] = \
        Mul(Rat(Int(1), Pow(10, 18)), Sym("zeptopa"))  # nopep8
    CONVERSIONS[UnitsPressure.ZEPTOPASCAL][UnitsPressure.MILLITORR] = \
        Mul(Rat(Int(19), Mul(Int(2533125), Pow(10, 21))), Sym("zeptopa"))  # nopep8
    CONVERSIONS[UnitsPressure.ZEPTOPASCAL][UnitsPressure.MMHG] = \
        Mul(Rat(Int(1), Mul(Int("133322387415"), Pow(10, 12))), Sym("zeptopa"))  # nopep8
    CONVERSIONS[UnitsPressure.ZEPTOPASCAL][UnitsPressure.NANOPASCAL] = \
        Mul(Rat(Int(1), Pow(10, 12)), Sym("zeptopa"))  # nopep8
    CONVERSIONS[UnitsPressure.ZEPTOPASCAL][UnitsPressure.PETAPASCAL] = \
        Mul(Rat(Int(1), Pow(10, 36)), Sym("zeptopa"))  # nopep8
    CONVERSIONS[UnitsPressure.ZEPTOPASCAL][UnitsPressure.PICOPASCAL] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("zeptopa"))  # nopep8
    CONVERSIONS[UnitsPressure.ZEPTOPASCAL][UnitsPressure.PSI] = \
        Mul(Rat(Int(1), Mul(Int("689475729316836"), Pow(10, 10))), Sym("zeptopa"))  # nopep8
    CONVERSIONS[UnitsPressure.ZEPTOPASCAL][UnitsPressure.Pascal] = \
        Mul(Rat(Int(1), Pow(10, 21)), Sym("zeptopa"))  # nopep8
    CONVERSIONS[UnitsPressure.ZEPTOPASCAL][UnitsPressure.TERAPASCAL] = \
        Mul(Rat(Int(1), Pow(10, 33)), Sym("zeptopa"))  # nopep8
    CONVERSIONS[UnitsPressure.ZEPTOPASCAL][UnitsPressure.TORR] = \
        Mul(Rat(Int(19), Mul(Int(2533125), Pow(10, 18))), Sym("zeptopa"))  # nopep8
    CONVERSIONS[UnitsPressure.ZEPTOPASCAL][UnitsPressure.YOCTOPASCAL] = \
        Mul(Int(1000), Sym("zeptopa"))  # nopep8
    CONVERSIONS[UnitsPressure.ZEPTOPASCAL][UnitsPressure.YOTTAPASCAL] = \
        Mul(Rat(Int(1), Pow(10, 45)), Sym("zeptopa"))  # nopep8
    CONVERSIONS[UnitsPressure.ZEPTOPASCAL][UnitsPressure.ZETTAPASCAL] = \
        Mul(Rat(Int(1), Pow(10, 42)), Sym("zeptopa"))  # nopep8
    CONVERSIONS[UnitsPressure.ZETTAPASCAL][UnitsPressure.ATMOSPHERE] = \
        Mul(Rat(Mul(Int(4), Pow(10, 19)), Int(4053)), Sym("zettapa"))  # nopep8
    CONVERSIONS[UnitsPressure.ZETTAPASCAL][UnitsPressure.ATTOPASCAL] = \
        Mul(Pow(10, 39), Sym("zettapa"))  # nopep8
    CONVERSIONS[UnitsPressure.ZETTAPASCAL][UnitsPressure.BAR] = \
        Mul(Pow(10, 16), Sym("zettapa"))  # nopep8
    CONVERSIONS[UnitsPressure.ZETTAPASCAL][UnitsPressure.CENTIBAR] = \
        Mul(Pow(10, 18), Sym("zettapa"))  # nopep8
    CONVERSIONS[UnitsPressure.ZETTAPASCAL][UnitsPressure.CENTIPASCAL] = \
        Mul(Pow(10, 23), Sym("zettapa"))  # nopep8
    CONVERSIONS[UnitsPressure.ZETTAPASCAL][UnitsPressure.DECAPASCAL] = \
        Mul(Pow(10, 20), Sym("zettapa"))  # nopep8
    CONVERSIONS[UnitsPressure.ZETTAPASCAL][UnitsPressure.DECIBAR] = \
        Mul(Pow(10, 17), Sym("zettapa"))  # nopep8
    CONVERSIONS[UnitsPressure.ZETTAPASCAL][UnitsPressure.DECIPASCAL] = \
        Mul(Pow(10, 22), Sym("zettapa"))  # nopep8
    CONVERSIONS[UnitsPressure.ZETTAPASCAL][UnitsPressure.EXAPASCAL] = \
        Mul(Int(1000), Sym("zettapa"))  # nopep8
    CONVERSIONS[UnitsPressure.ZETTAPASCAL][UnitsPressure.FEMTOPASCAL] = \
        Mul(Pow(10, 36), Sym("zettapa"))  # nopep8
    CONVERSIONS[UnitsPressure.ZETTAPASCAL][UnitsPressure.GIGAPASCAL] = \
        Mul(Pow(10, 12), Sym("zettapa"))  # nopep8
    CONVERSIONS[UnitsPressure.ZETTAPASCAL][UnitsPressure.HECTOPASCAL] = \
        Mul(Pow(10, 19), Sym("zettapa"))  # nopep8
    CONVERSIONS[UnitsPressure.ZETTAPASCAL][UnitsPressure.KILOBAR] = \
        Mul(Pow(10, 13), Sym("zettapa"))  # nopep8
    CONVERSIONS[UnitsPressure.ZETTAPASCAL][UnitsPressure.KILOPASCAL] = \
        Mul(Pow(10, 18), Sym("zettapa"))  # nopep8
    CONVERSIONS[UnitsPressure.ZETTAPASCAL][UnitsPressure.MEGABAR] = \
        Mul(Pow(10, 10), Sym("zettapa"))  # nopep8
    CONVERSIONS[UnitsPressure.ZETTAPASCAL][UnitsPressure.MEGAPASCAL] = \
        Mul(Pow(10, 15), Sym("zettapa"))  # nopep8
    CONVERSIONS[UnitsPressure.ZETTAPASCAL][UnitsPressure.MICROPASCAL] = \
        Mul(Pow(10, 27), Sym("zettapa"))  # nopep8
    CONVERSIONS[UnitsPressure.ZETTAPASCAL][UnitsPressure.MILLIBAR] = \
        Mul(Pow(10, 19), Sym("zettapa"))  # nopep8
    CONVERSIONS[UnitsPressure.ZETTAPASCAL][UnitsPressure.MILLIPASCAL] = \
        Mul(Pow(10, 24), Sym("zettapa"))  # nopep8
    CONVERSIONS[UnitsPressure.ZETTAPASCAL][UnitsPressure.MILLITORR] = \
        Mul(Rat(Mul(Int(304), Pow(10, 17)), Int(4053)), Sym("zettapa"))  # nopep8
    CONVERSIONS[UnitsPressure.ZETTAPASCAL][UnitsPressure.MMHG] = \
        Mul(Rat(Mul(Int(2), Pow(10, 29)), Int("26664477483")), Sym("zettapa"))  # nopep8
    CONVERSIONS[UnitsPressure.ZETTAPASCAL][UnitsPressure.NANOPASCAL] = \
        Mul(Pow(10, 30), Sym("zettapa"))  # nopep8
    CONVERSIONS[UnitsPressure.ZETTAPASCAL][UnitsPressure.PETAPASCAL] = \
        Mul(Pow(10, 6), Sym("zettapa"))  # nopep8
    CONVERSIONS[UnitsPressure.ZETTAPASCAL][UnitsPressure.PICOPASCAL] = \
        Mul(Pow(10, 33), Sym("zettapa"))  # nopep8
    CONVERSIONS[UnitsPressure.ZETTAPASCAL][UnitsPressure.PSI] = \
        Mul(Rat(Mul(Int(25), Pow(10, 30)), Int("172368932329209")), Sym("zettapa"))  # nopep8
    CONVERSIONS[UnitsPressure.ZETTAPASCAL][UnitsPressure.Pascal] = \
        Mul(Pow(10, 21), Sym("zettapa"))  # nopep8
    CONVERSIONS[UnitsPressure.ZETTAPASCAL][UnitsPressure.TERAPASCAL] = \
        Mul(Pow(10, 9), Sym("zettapa"))  # nopep8
    CONVERSIONS[UnitsPressure.ZETTAPASCAL][UnitsPressure.TORR] = \
        Mul(Rat(Mul(Int(304), Pow(10, 20)), Int(4053)), Sym("zettapa"))  # nopep8
    CONVERSIONS[UnitsPressure.ZETTAPASCAL][UnitsPressure.YOCTOPASCAL] = \
        Mul(Pow(10, 45), Sym("zettapa"))  # nopep8
    CONVERSIONS[UnitsPressure.ZETTAPASCAL][UnitsPressure.YOTTAPASCAL] = \
        Mul(Rat(Int(1), Int(1000)), Sym("zettapa"))  # nopep8
    CONVERSIONS[UnitsPressure.ZETTAPASCAL][UnitsPressure.ZEPTOPASCAL] = \
        Mul(Pow(10, 42), Sym("zettapa"))  # nopep8

    SYMBOLS = dict()
    SYMBOLS["ATMOSPHERE"] = "atm"
    SYMBOLS["ATTOPASCAL"] = "aPa"
    SYMBOLS["BAR"] = "bar"
    SYMBOLS["CENTIBAR"] = "cbar"
    SYMBOLS["CENTIPASCAL"] = "cPa"
    SYMBOLS["DECAPASCAL"] = "daPa"
    SYMBOLS["DECIBAR"] = "dbar"
    SYMBOLS["DECIPASCAL"] = "dPa"
    SYMBOLS["EXAPASCAL"] = "EPa"
    SYMBOLS["FEMTOPASCAL"] = "fPa"
    SYMBOLS["GIGAPASCAL"] = "GPa"
    SYMBOLS["HECTOPASCAL"] = "hPa"
    SYMBOLS["KILOBAR"] = "kbar"
    SYMBOLS["KILOPASCAL"] = "kPa"
    SYMBOLS["MEGABAR"] = "Mbar"
    SYMBOLS["MEGAPASCAL"] = "MPa"
    SYMBOLS["MICROPASCAL"] = "µPa"
    SYMBOLS["MILLIBAR"] = "mbar"
    SYMBOLS["MILLIPASCAL"] = "mPa"
    SYMBOLS["MILLITORR"] = "mTorr"
    SYMBOLS["MMHG"] = "mm Hg"
    SYMBOLS["NANOPASCAL"] = "nPa"
    SYMBOLS["PETAPASCAL"] = "PPa"
    SYMBOLS["PICOPASCAL"] = "pPa"
    SYMBOLS["PSI"] = "psi"
    SYMBOLS["Pascal"] = "Pa"
    SYMBOLS["TERAPASCAL"] = "TPa"
    SYMBOLS["TORR"] = "Torr"
    SYMBOLS["YOCTOPASCAL"] = "yPa"
    SYMBOLS["YOTTAPASCAL"] = "YPa"
    SYMBOLS["ZEPTOPASCAL"] = "zPa"
    SYMBOLS["ZETTAPASCAL"] = "ZPa"

    def __init__(self, value=None, unit=None):
        _omero_model.Pressure.__init__(self)

        if isinstance(unit, UnitsPressure):
            target = unit
        elif isinstance(unit, (str, unicode)):
            target = getattr(UnitsPressure, unit)
        else:
            raise Exception("Unknown unit: %s (%s)" % (
                unit, type(unit)
            ))

        if isinstance(value, _omero_model.PressureI):
            # This is a copy-constructor call.

            source = value.getUnit()

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
        return PressureI.SYMBOLS.get(str(unit))

    def setUnit(self, unit, current=None):
        self._unit = unit

    def setValue(self, value, current=None):
        self._value = value

    def __str__(self):
        return self._base_string(self.getValue(), self.getUnit())

_omero_model.PressureI = PressureI
