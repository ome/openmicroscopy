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

from omero.model.conversions import Add
from omero.model.conversions import Int
from omero.model.conversions import Mul
from omero.model.conversions import Pow
from omero.model.conversions import Rat
from omero.model.conversions import Sym


class PressureI(_omero_model.Pressure, UnitBase):

    CONVERSIONS = dict()
    CONVERSIONS["ATHMOSPHERE:ATTOPASCAL"] = \
        Mul(Rat(Int(1), Mul(Int(101325), Pow(10, 18))), Sym("atm"))
    CONVERSIONS["ATHMOSPHERE:BAR"] = \
        Mul(Rat(Int(4000), Int(4053)), Sym("atm"))
    CONVERSIONS["ATHMOSPHERE:CENTIBAR"] = \
        Mul(Rat(Int(40), Int(4053)), Sym("atm"))
    CONVERSIONS["ATHMOSPHERE:CENTIPASCAL"] = \
        Mul(Rat(Int(1), Int(10132500)), Sym("atm"))
    CONVERSIONS["ATHMOSPHERE:DECAPASCAL"] = \
        Mul(Rat(Int(2), Int(20265)), Sym("atm"))
    CONVERSIONS["ATHMOSPHERE:DECIBAR"] = \
        Mul(Rat(Int(400), Int(4053)), Sym("atm"))
    CONVERSIONS["ATHMOSPHERE:DECIPASCAL"] = \
        Mul(Rat(Int(1), Int(1013250)), Sym("atm"))
    CONVERSIONS["ATHMOSPHERE:EXAPASCAL"] = \
        Mul(Rat(Mul(Int(4), Pow(10, 16)), Int(4053)), Sym("atm"))
    CONVERSIONS["ATHMOSPHERE:FEMTOPASCAL"] = \
        Mul(Rat(Int(1), Mul(Int(101325), Pow(10, 15))), Sym("atm"))
    CONVERSIONS["ATHMOSPHERE:GIGAPASCAL"] = \
        Mul(Rat(Mul(Int(4), Pow(10, 7)), Int(4053)), Sym("atm"))
    CONVERSIONS["ATHMOSPHERE:HECTOPASCAL"] = \
        Mul(Rat(Int(4), Int(4053)), Sym("atm"))
    CONVERSIONS["ATHMOSPHERE:KILOBAR"] = \
        Mul(Rat(Mul(Int(4), Pow(10, 6)), Int(4053)), Sym("atm"))
    CONVERSIONS["ATHMOSPHERE:KILOPASCAL"] = \
        Mul(Rat(Int(40), Int(4053)), Sym("atm"))
    CONVERSIONS["ATHMOSPHERE:MEGABAR"] = \
        Mul(Rat(Mul(Int(4), Pow(10, 9)), Int(4053)), Sym("atm"))
    CONVERSIONS["ATHMOSPHERE:MEGAPASCAL"] = \
        Mul(Rat(Mul(Int(4), Pow(10, 4)), Int(4053)), Sym("atm"))
    CONVERSIONS["ATHMOSPHERE:MICROPASCAL"] = \
        Mul(Rat(Int(1), Mul(Int(101325), Pow(10, 6))), Sym("atm"))
    CONVERSIONS["ATHMOSPHERE:MILLIBAR"] = \
        Mul(Rat(Int(4), Int(4053)), Sym("atm"))
    CONVERSIONS["ATHMOSPHERE:MILLIPASCAL"] = \
        Mul(Rat(Int(1), Int(101325000)), Sym("atm"))
    CONVERSIONS["ATHMOSPHERE:MILLITORR"] = \
        Mul(Rat(Int(1), Mul(Int(76), Pow(10, 4))), Sym("atm"))
    CONVERSIONS["ATHMOSPHERE:MMHG"] = \
        Mul(Rat(Int(1269737023), Mul(Int(965), Pow(10, 9))), Sym("atm"))
    CONVERSIONS["ATHMOSPHERE:NANOPASCAL"] = \
        Mul(Rat(Int(1), Mul(Int(101325), Pow(10, 9))), Sym("atm"))
    CONVERSIONS["ATHMOSPHERE:PASCAL"] = \
        Mul(Rat(Int(1), Int(101325)), Sym("atm"))
    CONVERSIONS["ATHMOSPHERE:PETAPASCAL"] = \
        Mul(Rat(Mul(Int(4), Pow(10, 13)), Int(4053)), Sym("atm"))
    CONVERSIONS["ATHMOSPHERE:PICOPASCAL"] = \
        Mul(Rat(Int(1), Mul(Int(101325), Pow(10, 12))), Sym("atm"))
    CONVERSIONS["ATHMOSPHERE:PSI"] = \
        Mul(Rat(Int("8208044396629"), Mul(Int(120625), Pow(10, 9))), Sym("atm"))
    CONVERSIONS["ATHMOSPHERE:TERAPASCAL"] = \
        Mul(Rat(Mul(Int(4), Pow(10, 10)), Int(4053)), Sym("atm"))
    CONVERSIONS["ATHMOSPHERE:TORR"] = \
        Mul(Rat(Int(1), Int(760)), Sym("atm"))
    CONVERSIONS["ATHMOSPHERE:YOCTOPASCAL"] = \
        Mul(Rat(Int(1), Mul(Int(101325), Pow(10, 24))), Sym("atm"))
    CONVERSIONS["ATHMOSPHERE:YOTTAPASCAL"] = \
        Mul(Rat(Mul(Int(4), Pow(10, 22)), Int(4053)), Sym("atm"))
    CONVERSIONS["ATHMOSPHERE:ZEPTOPASCAL"] = \
        Mul(Rat(Int(1), Mul(Int(101325), Pow(10, 21))), Sym("atm"))
    CONVERSIONS["ATHMOSPHERE:ZETTAPASCAL"] = \
        Mul(Rat(Mul(Int(4), Pow(10, 19)), Int(4053)), Sym("atm"))
    CONVERSIONS["ATTOPASCAL:ATHMOSPHERE"] = \
        Mul(Mul(Int(101325), Pow(10, 18)), Sym("attopa"))
    CONVERSIONS["ATTOPASCAL:BAR"] = \
        Mul(Pow(10, 23), Sym("attopa"))
    CONVERSIONS["ATTOPASCAL:CENTIBAR"] = \
        Mul(Pow(10, 21), Sym("attopa"))
    CONVERSIONS["ATTOPASCAL:CENTIPASCAL"] = \
        Mul(Pow(10, 16), Sym("attopa"))
    CONVERSIONS["ATTOPASCAL:DECAPASCAL"] = \
        Mul(Pow(10, 19), Sym("attopa"))
    CONVERSIONS["ATTOPASCAL:DECIBAR"] = \
        Mul(Pow(10, 22), Sym("attopa"))
    CONVERSIONS["ATTOPASCAL:DECIPASCAL"] = \
        Mul(Pow(10, 17), Sym("attopa"))
    CONVERSIONS["ATTOPASCAL:EXAPASCAL"] = \
        Mul(Pow(10, 36), Sym("attopa"))
    CONVERSIONS["ATTOPASCAL:FEMTOPASCAL"] = \
        Mul(Int(1000), Sym("attopa"))
    CONVERSIONS["ATTOPASCAL:GIGAPASCAL"] = \
        Mul(Pow(10, 27), Sym("attopa"))
    CONVERSIONS["ATTOPASCAL:HECTOPASCAL"] = \
        Mul(Pow(10, 20), Sym("attopa"))
    CONVERSIONS["ATTOPASCAL:KILOBAR"] = \
        Mul(Pow(10, 26), Sym("attopa"))
    CONVERSIONS["ATTOPASCAL:KILOPASCAL"] = \
        Mul(Pow(10, 21), Sym("attopa"))
    CONVERSIONS["ATTOPASCAL:MEGABAR"] = \
        Mul(Pow(10, 29), Sym("attopa"))
    CONVERSIONS["ATTOPASCAL:MEGAPASCAL"] = \
        Mul(Pow(10, 24), Sym("attopa"))
    CONVERSIONS["ATTOPASCAL:MICROPASCAL"] = \
        Mul(Pow(10, 12), Sym("attopa"))
    CONVERSIONS["ATTOPASCAL:MILLIBAR"] = \
        Mul(Pow(10, 20), Sym("attopa"))
    CONVERSIONS["ATTOPASCAL:MILLIPASCAL"] = \
        Mul(Pow(10, 15), Sym("attopa"))
    CONVERSIONS["ATTOPASCAL:MILLITORR"] = \
        Mul(Rat(Mul(Int(2533125), Pow(10, 12)), Int(19)), Sym("attopa"))
    CONVERSIONS["ATTOPASCAL:MMHG"] = \
        Mul(Mul(Int("133322387415"), Pow(10, 9)), Sym("attopa"))
    CONVERSIONS["ATTOPASCAL:NANOPASCAL"] = \
        Mul(Pow(10, 9), Sym("attopa"))
    CONVERSIONS["ATTOPASCAL:PASCAL"] = \
        Mul(Pow(10, 18), Sym("attopa"))
    CONVERSIONS["ATTOPASCAL:PETAPASCAL"] = \
        Mul(Pow(10, 33), Sym("attopa"))
    CONVERSIONS["ATTOPASCAL:PICOPASCAL"] = \
        Mul(Pow(10, 6), Sym("attopa"))
    CONVERSIONS["ATTOPASCAL:PSI"] = \
        Mul(Mul(Int("689475729316836"), Pow(10, 7)), Sym("attopa"))
    CONVERSIONS["ATTOPASCAL:TERAPASCAL"] = \
        Mul(Pow(10, 30), Sym("attopa"))
    CONVERSIONS["ATTOPASCAL:TORR"] = \
        Mul(Rat(Mul(Int(2533125), Pow(10, 15)), Int(19)), Sym("attopa"))
    CONVERSIONS["ATTOPASCAL:YOCTOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("attopa"))
    CONVERSIONS["ATTOPASCAL:YOTTAPASCAL"] = \
        Mul(Pow(10, 42), Sym("attopa"))
    CONVERSIONS["ATTOPASCAL:ZEPTOPASCAL"] = \
        Mul(Rat(Int(1), Int(1000)), Sym("attopa"))
    CONVERSIONS["ATTOPASCAL:ZETTAPASCAL"] = \
        Mul(Pow(10, 39), Sym("attopa"))
    CONVERSIONS["BAR:ATHMOSPHERE"] = \
        Mul(Rat(Int(4053), Int(4000)), Sym("bar"))
    CONVERSIONS["BAR:ATTOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 23)), Sym("bar"))
    CONVERSIONS["BAR:CENTIBAR"] = \
        Mul(Rat(Int(1), Int(100)), Sym("bar"))
    CONVERSIONS["BAR:CENTIPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 7)), Sym("bar"))
    CONVERSIONS["BAR:DECAPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 4)), Sym("bar"))
    CONVERSIONS["BAR:DECIBAR"] = \
        Mul(Rat(Int(1), Int(10)), Sym("bar"))
    CONVERSIONS["BAR:DECIPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("bar"))
    CONVERSIONS["BAR:EXAPASCAL"] = \
        Mul(Pow(10, 13), Sym("bar"))
    CONVERSIONS["BAR:FEMTOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 20)), Sym("bar"))
    CONVERSIONS["BAR:GIGAPASCAL"] = \
        Mul(Pow(10, 4), Sym("bar"))
    CONVERSIONS["BAR:HECTOPASCAL"] = \
        Mul(Rat(Int(1), Int(1000)), Sym("bar"))
    CONVERSIONS["BAR:KILOBAR"] = \
        Mul(Int(1000), Sym("bar"))
    CONVERSIONS["BAR:KILOPASCAL"] = \
        Mul(Rat(Int(1), Int(100)), Sym("bar"))
    CONVERSIONS["BAR:MEGABAR"] = \
        Mul(Pow(10, 6), Sym("bar"))
    CONVERSIONS["BAR:MEGAPASCAL"] = \
        Mul(Int(10), Sym("bar"))
    CONVERSIONS["BAR:MICROPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 11)), Sym("bar"))
    CONVERSIONS["BAR:MILLIBAR"] = \
        Mul(Rat(Int(1), Int(1000)), Sym("bar"))
    CONVERSIONS["BAR:MILLIPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 8)), Sym("bar"))
    CONVERSIONS["BAR:MILLITORR"] = \
        Mul(Rat(Int(4053), Mul(Int(304), Pow(10, 7))), Sym("bar"))
    CONVERSIONS["BAR:MMHG"] = \
        Mul(Rat(Int("26664477483"), Mul(Int(2), Pow(10, 13))), Sym("bar"))
    CONVERSIONS["BAR:NANOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 14)), Sym("bar"))
    CONVERSIONS["BAR:PASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 5)), Sym("bar"))
    CONVERSIONS["BAR:PETAPASCAL"] = \
        Mul(Pow(10, 10), Sym("bar"))
    CONVERSIONS["BAR:PICOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 17)), Sym("bar"))
    CONVERSIONS["BAR:PSI"] = \
        Mul(Rat(Int("172368932329209"), Mul(Int(25), Pow(10, 14))), Sym("bar"))
    CONVERSIONS["BAR:TERAPASCAL"] = \
        Mul(Pow(10, 7), Sym("bar"))
    CONVERSIONS["BAR:TORR"] = \
        Mul(Rat(Int(4053), Mul(Int(304), Pow(10, 4))), Sym("bar"))
    CONVERSIONS["BAR:YOCTOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 29)), Sym("bar"))
    CONVERSIONS["BAR:YOTTAPASCAL"] = \
        Mul(Pow(10, 19), Sym("bar"))
    CONVERSIONS["BAR:ZEPTOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 26)), Sym("bar"))
    CONVERSIONS["BAR:ZETTAPASCAL"] = \
        Mul(Pow(10, 16), Sym("bar"))
    CONVERSIONS["CENTIBAR:ATHMOSPHERE"] = \
        Mul(Rat(Int(4053), Int(40)), Sym("cbar"))
    CONVERSIONS["CENTIBAR:ATTOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 21)), Sym("cbar"))
    CONVERSIONS["CENTIBAR:BAR"] = \
        Mul(Int(100), Sym("cbar"))
    CONVERSIONS["CENTIBAR:CENTIPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 5)), Sym("cbar"))
    CONVERSIONS["CENTIBAR:DECAPASCAL"] = \
        Mul(Rat(Int(1), Int(100)), Sym("cbar"))
    CONVERSIONS["CENTIBAR:DECIBAR"] = \
        Mul(Int(10), Sym("cbar"))
    CONVERSIONS["CENTIBAR:DECIPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 4)), Sym("cbar"))
    CONVERSIONS["CENTIBAR:EXAPASCAL"] = \
        Mul(Pow(10, 15), Sym("cbar"))
    CONVERSIONS["CENTIBAR:FEMTOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 18)), Sym("cbar"))
    CONVERSIONS["CENTIBAR:GIGAPASCAL"] = \
        Mul(Pow(10, 6), Sym("cbar"))
    CONVERSIONS["CENTIBAR:HECTOPASCAL"] = \
        Mul(Rat(Int(1), Int(10)), Sym("cbar"))
    CONVERSIONS["CENTIBAR:KILOBAR"] = \
        Mul(Pow(10, 5), Sym("cbar"))
    CONVERSIONS["CENTIBAR:KILOPASCAL"] = \
        Sym("cbar")
    CONVERSIONS["CENTIBAR:MEGABAR"] = \
        Mul(Pow(10, 8), Sym("cbar"))
    CONVERSIONS["CENTIBAR:MEGAPASCAL"] = \
        Mul(Int(1000), Sym("cbar"))
    CONVERSIONS["CENTIBAR:MICROPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("cbar"))
    CONVERSIONS["CENTIBAR:MILLIBAR"] = \
        Mul(Rat(Int(1), Int(10)), Sym("cbar"))
    CONVERSIONS["CENTIBAR:MILLIPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("cbar"))
    CONVERSIONS["CENTIBAR:MILLITORR"] = \
        Mul(Rat(Int(4053), Mul(Int(304), Pow(10, 5))), Sym("cbar"))
    CONVERSIONS["CENTIBAR:MMHG"] = \
        Mul(Rat(Int("26664477483"), Mul(Int(2), Pow(10, 11))), Sym("cbar"))
    CONVERSIONS["CENTIBAR:NANOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 12)), Sym("cbar"))
    CONVERSIONS["CENTIBAR:PASCAL"] = \
        Mul(Rat(Int(1), Int(1000)), Sym("cbar"))
    CONVERSIONS["CENTIBAR:PETAPASCAL"] = \
        Mul(Pow(10, 12), Sym("cbar"))
    CONVERSIONS["CENTIBAR:PICOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 15)), Sym("cbar"))
    CONVERSIONS["CENTIBAR:PSI"] = \
        Mul(Rat(Int("172368932329209"), Mul(Int(25), Pow(10, 12))), Sym("cbar"))
    CONVERSIONS["CENTIBAR:TERAPASCAL"] = \
        Mul(Pow(10, 9), Sym("cbar"))
    CONVERSIONS["CENTIBAR:TORR"] = \
        Mul(Rat(Int(4053), Int(30400)), Sym("cbar"))
    CONVERSIONS["CENTIBAR:YOCTOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 27)), Sym("cbar"))
    CONVERSIONS["CENTIBAR:YOTTAPASCAL"] = \
        Mul(Pow(10, 21), Sym("cbar"))
    CONVERSIONS["CENTIBAR:ZEPTOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 24)), Sym("cbar"))
    CONVERSIONS["CENTIBAR:ZETTAPASCAL"] = \
        Mul(Pow(10, 18), Sym("cbar"))
    CONVERSIONS["CENTIPASCAL:ATHMOSPHERE"] = \
        Mul(Int(10132500), Sym("centipa"))
    CONVERSIONS["CENTIPASCAL:ATTOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 16)), Sym("centipa"))
    CONVERSIONS["CENTIPASCAL:BAR"] = \
        Mul(Pow(10, 7), Sym("centipa"))
    CONVERSIONS["CENTIPASCAL:CENTIBAR"] = \
        Mul(Pow(10, 5), Sym("centipa"))
    CONVERSIONS["CENTIPASCAL:DECAPASCAL"] = \
        Mul(Int(1000), Sym("centipa"))
    CONVERSIONS["CENTIPASCAL:DECIBAR"] = \
        Mul(Pow(10, 6), Sym("centipa"))
    CONVERSIONS["CENTIPASCAL:DECIPASCAL"] = \
        Mul(Int(10), Sym("centipa"))
    CONVERSIONS["CENTIPASCAL:EXAPASCAL"] = \
        Mul(Pow(10, 20), Sym("centipa"))
    CONVERSIONS["CENTIPASCAL:FEMTOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 13)), Sym("centipa"))
    CONVERSIONS["CENTIPASCAL:GIGAPASCAL"] = \
        Mul(Pow(10, 11), Sym("centipa"))
    CONVERSIONS["CENTIPASCAL:HECTOPASCAL"] = \
        Mul(Pow(10, 4), Sym("centipa"))
    CONVERSIONS["CENTIPASCAL:KILOBAR"] = \
        Mul(Pow(10, 10), Sym("centipa"))
    CONVERSIONS["CENTIPASCAL:KILOPASCAL"] = \
        Mul(Pow(10, 5), Sym("centipa"))
    CONVERSIONS["CENTIPASCAL:MEGABAR"] = \
        Mul(Pow(10, 13), Sym("centipa"))
    CONVERSIONS["CENTIPASCAL:MEGAPASCAL"] = \
        Mul(Pow(10, 8), Sym("centipa"))
    CONVERSIONS["CENTIPASCAL:MICROPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 4)), Sym("centipa"))
    CONVERSIONS["CENTIPASCAL:MILLIBAR"] = \
        Mul(Pow(10, 4), Sym("centipa"))
    CONVERSIONS["CENTIPASCAL:MILLIPASCAL"] = \
        Mul(Rat(Int(1), Int(10)), Sym("centipa"))
    CONVERSIONS["CENTIPASCAL:MILLITORR"] = \
        Mul(Rat(Int(4053), Int(304)), Sym("centipa"))
    CONVERSIONS["CENTIPASCAL:MMHG"] = \
        Mul(Rat(Int("26664477483"), Mul(Int(2), Pow(10, 6))), Sym("centipa"))
    CONVERSIONS["CENTIPASCAL:NANOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 7)), Sym("centipa"))
    CONVERSIONS["CENTIPASCAL:PASCAL"] = \
        Mul(Int(100), Sym("centipa"))
    CONVERSIONS["CENTIPASCAL:PETAPASCAL"] = \
        Mul(Pow(10, 17), Sym("centipa"))
    CONVERSIONS["CENTIPASCAL:PICOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 10)), Sym("centipa"))
    CONVERSIONS["CENTIPASCAL:PSI"] = \
        Mul(Rat(Int("172368932329209"), Mul(Int(25), Pow(10, 7))), Sym("centipa"))
    CONVERSIONS["CENTIPASCAL:TERAPASCAL"] = \
        Mul(Pow(10, 14), Sym("centipa"))
    CONVERSIONS["CENTIPASCAL:TORR"] = \
        Mul(Rat(Int(506625), Int(38)), Sym("centipa"))
    CONVERSIONS["CENTIPASCAL:YOCTOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 22)), Sym("centipa"))
    CONVERSIONS["CENTIPASCAL:YOTTAPASCAL"] = \
        Mul(Pow(10, 26), Sym("centipa"))
    CONVERSIONS["CENTIPASCAL:ZEPTOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 19)), Sym("centipa"))
    CONVERSIONS["CENTIPASCAL:ZETTAPASCAL"] = \
        Mul(Pow(10, 23), Sym("centipa"))
    CONVERSIONS["DECAPASCAL:ATHMOSPHERE"] = \
        Mul(Rat(Int(20265), Int(2)), Sym("decapa"))
    CONVERSIONS["DECAPASCAL:ATTOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 19)), Sym("decapa"))
    CONVERSIONS["DECAPASCAL:BAR"] = \
        Mul(Pow(10, 4), Sym("decapa"))
    CONVERSIONS["DECAPASCAL:CENTIBAR"] = \
        Mul(Int(100), Sym("decapa"))
    CONVERSIONS["DECAPASCAL:CENTIPASCAL"] = \
        Mul(Rat(Int(1), Int(1000)), Sym("decapa"))
    CONVERSIONS["DECAPASCAL:DECIBAR"] = \
        Mul(Int(1000), Sym("decapa"))
    CONVERSIONS["DECAPASCAL:DECIPASCAL"] = \
        Mul(Rat(Int(1), Int(100)), Sym("decapa"))
    CONVERSIONS["DECAPASCAL:EXAPASCAL"] = \
        Mul(Pow(10, 17), Sym("decapa"))
    CONVERSIONS["DECAPASCAL:FEMTOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 16)), Sym("decapa"))
    CONVERSIONS["DECAPASCAL:GIGAPASCAL"] = \
        Mul(Pow(10, 8), Sym("decapa"))
    CONVERSIONS["DECAPASCAL:HECTOPASCAL"] = \
        Mul(Int(10), Sym("decapa"))
    CONVERSIONS["DECAPASCAL:KILOBAR"] = \
        Mul(Pow(10, 7), Sym("decapa"))
    CONVERSIONS["DECAPASCAL:KILOPASCAL"] = \
        Mul(Int(100), Sym("decapa"))
    CONVERSIONS["DECAPASCAL:MEGABAR"] = \
        Mul(Pow(10, 10), Sym("decapa"))
    CONVERSIONS["DECAPASCAL:MEGAPASCAL"] = \
        Mul(Pow(10, 5), Sym("decapa"))
    CONVERSIONS["DECAPASCAL:MICROPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 7)), Sym("decapa"))
    CONVERSIONS["DECAPASCAL:MILLIBAR"] = \
        Mul(Int(10), Sym("decapa"))
    CONVERSIONS["DECAPASCAL:MILLIPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 4)), Sym("decapa"))
    CONVERSIONS["DECAPASCAL:MILLITORR"] = \
        Mul(Rat(Int(4053), Int(304000)), Sym("decapa"))
    CONVERSIONS["DECAPASCAL:MMHG"] = \
        Mul(Rat(Int("26664477483"), Mul(Int(2), Pow(10, 9))), Sym("decapa"))
    CONVERSIONS["DECAPASCAL:NANOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 10)), Sym("decapa"))
    CONVERSIONS["DECAPASCAL:PASCAL"] = \
        Mul(Rat(Int(1), Int(10)), Sym("decapa"))
    CONVERSIONS["DECAPASCAL:PETAPASCAL"] = \
        Mul(Pow(10, 14), Sym("decapa"))
    CONVERSIONS["DECAPASCAL:PICOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 13)), Sym("decapa"))
    CONVERSIONS["DECAPASCAL:PSI"] = \
        Mul(Rat(Int("172368932329209"), Mul(Int(25), Pow(10, 10))), Sym("decapa"))
    CONVERSIONS["DECAPASCAL:TERAPASCAL"] = \
        Mul(Pow(10, 11), Sym("decapa"))
    CONVERSIONS["DECAPASCAL:TORR"] = \
        Mul(Rat(Int(4053), Int(304)), Sym("decapa"))
    CONVERSIONS["DECAPASCAL:YOCTOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 25)), Sym("decapa"))
    CONVERSIONS["DECAPASCAL:YOTTAPASCAL"] = \
        Mul(Pow(10, 23), Sym("decapa"))
    CONVERSIONS["DECAPASCAL:ZEPTOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 22)), Sym("decapa"))
    CONVERSIONS["DECAPASCAL:ZETTAPASCAL"] = \
        Mul(Pow(10, 20), Sym("decapa"))
    CONVERSIONS["DECIBAR:ATHMOSPHERE"] = \
        Mul(Rat(Int(4053), Int(400)), Sym("dbar"))
    CONVERSIONS["DECIBAR:ATTOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 22)), Sym("dbar"))
    CONVERSIONS["DECIBAR:BAR"] = \
        Mul(Int(10), Sym("dbar"))
    CONVERSIONS["DECIBAR:CENTIBAR"] = \
        Mul(Rat(Int(1), Int(10)), Sym("dbar"))
    CONVERSIONS["DECIBAR:CENTIPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("dbar"))
    CONVERSIONS["DECIBAR:DECAPASCAL"] = \
        Mul(Rat(Int(1), Int(1000)), Sym("dbar"))
    CONVERSIONS["DECIBAR:DECIPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 5)), Sym("dbar"))
    CONVERSIONS["DECIBAR:EXAPASCAL"] = \
        Mul(Pow(10, 14), Sym("dbar"))
    CONVERSIONS["DECIBAR:FEMTOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 19)), Sym("dbar"))
    CONVERSIONS["DECIBAR:GIGAPASCAL"] = \
        Mul(Pow(10, 5), Sym("dbar"))
    CONVERSIONS["DECIBAR:HECTOPASCAL"] = \
        Mul(Rat(Int(1), Int(100)), Sym("dbar"))
    CONVERSIONS["DECIBAR:KILOBAR"] = \
        Mul(Pow(10, 4), Sym("dbar"))
    CONVERSIONS["DECIBAR:KILOPASCAL"] = \
        Mul(Rat(Int(1), Int(10)), Sym("dbar"))
    CONVERSIONS["DECIBAR:MEGABAR"] = \
        Mul(Pow(10, 7), Sym("dbar"))
    CONVERSIONS["DECIBAR:MEGAPASCAL"] = \
        Mul(Int(100), Sym("dbar"))
    CONVERSIONS["DECIBAR:MICROPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 10)), Sym("dbar"))
    CONVERSIONS["DECIBAR:MILLIBAR"] = \
        Mul(Rat(Int(1), Int(100)), Sym("dbar"))
    CONVERSIONS["DECIBAR:MILLIPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 7)), Sym("dbar"))
    CONVERSIONS["DECIBAR:MILLITORR"] = \
        Mul(Rat(Int(4053), Mul(Int(304), Pow(10, 6))), Sym("dbar"))
    CONVERSIONS["DECIBAR:MMHG"] = \
        Mul(Rat(Int("26664477483"), Mul(Int(2), Pow(10, 12))), Sym("dbar"))
    CONVERSIONS["DECIBAR:NANOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 13)), Sym("dbar"))
    CONVERSIONS["DECIBAR:PASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 4)), Sym("dbar"))
    CONVERSIONS["DECIBAR:PETAPASCAL"] = \
        Mul(Pow(10, 11), Sym("dbar"))
    CONVERSIONS["DECIBAR:PICOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 16)), Sym("dbar"))
    CONVERSIONS["DECIBAR:PSI"] = \
        Mul(Rat(Int("172368932329209"), Mul(Int(25), Pow(10, 13))), Sym("dbar"))
    CONVERSIONS["DECIBAR:TERAPASCAL"] = \
        Mul(Pow(10, 8), Sym("dbar"))
    CONVERSIONS["DECIBAR:TORR"] = \
        Mul(Rat(Int(4053), Int(304000)), Sym("dbar"))
    CONVERSIONS["DECIBAR:YOCTOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 28)), Sym("dbar"))
    CONVERSIONS["DECIBAR:YOTTAPASCAL"] = \
        Mul(Pow(10, 20), Sym("dbar"))
    CONVERSIONS["DECIBAR:ZEPTOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 25)), Sym("dbar"))
    CONVERSIONS["DECIBAR:ZETTAPASCAL"] = \
        Mul(Pow(10, 17), Sym("dbar"))
    CONVERSIONS["DECIPASCAL:ATHMOSPHERE"] = \
        Mul(Int(1013250), Sym("decipa"))
    CONVERSIONS["DECIPASCAL:ATTOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 17)), Sym("decipa"))
    CONVERSIONS["DECIPASCAL:BAR"] = \
        Mul(Pow(10, 6), Sym("decipa"))
    CONVERSIONS["DECIPASCAL:CENTIBAR"] = \
        Mul(Pow(10, 4), Sym("decipa"))
    CONVERSIONS["DECIPASCAL:CENTIPASCAL"] = \
        Mul(Rat(Int(1), Int(10)), Sym("decipa"))
    CONVERSIONS["DECIPASCAL:DECAPASCAL"] = \
        Mul(Int(100), Sym("decipa"))
    CONVERSIONS["DECIPASCAL:DECIBAR"] = \
        Mul(Pow(10, 5), Sym("decipa"))
    CONVERSIONS["DECIPASCAL:EXAPASCAL"] = \
        Mul(Pow(10, 19), Sym("decipa"))
    CONVERSIONS["DECIPASCAL:FEMTOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 14)), Sym("decipa"))
    CONVERSIONS["DECIPASCAL:GIGAPASCAL"] = \
        Mul(Pow(10, 10), Sym("decipa"))
    CONVERSIONS["DECIPASCAL:HECTOPASCAL"] = \
        Mul(Int(1000), Sym("decipa"))
    CONVERSIONS["DECIPASCAL:KILOBAR"] = \
        Mul(Pow(10, 9), Sym("decipa"))
    CONVERSIONS["DECIPASCAL:KILOPASCAL"] = \
        Mul(Pow(10, 4), Sym("decipa"))
    CONVERSIONS["DECIPASCAL:MEGABAR"] = \
        Mul(Pow(10, 12), Sym("decipa"))
    CONVERSIONS["DECIPASCAL:MEGAPASCAL"] = \
        Mul(Pow(10, 7), Sym("decipa"))
    CONVERSIONS["DECIPASCAL:MICROPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 5)), Sym("decipa"))
    CONVERSIONS["DECIPASCAL:MILLIBAR"] = \
        Mul(Int(1000), Sym("decipa"))
    CONVERSIONS["DECIPASCAL:MILLIPASCAL"] = \
        Mul(Rat(Int(1), Int(100)), Sym("decipa"))
    CONVERSIONS["DECIPASCAL:MILLITORR"] = \
        Mul(Rat(Int(4053), Int(3040)), Sym("decipa"))
    CONVERSIONS["DECIPASCAL:MMHG"] = \
        Mul(Rat(Int("26664477483"), Mul(Int(2), Pow(10, 7))), Sym("decipa"))
    CONVERSIONS["DECIPASCAL:NANOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 8)), Sym("decipa"))
    CONVERSIONS["DECIPASCAL:PASCAL"] = \
        Mul(Int(10), Sym("decipa"))
    CONVERSIONS["DECIPASCAL:PETAPASCAL"] = \
        Mul(Pow(10, 16), Sym("decipa"))
    CONVERSIONS["DECIPASCAL:PICOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 11)), Sym("decipa"))
    CONVERSIONS["DECIPASCAL:PSI"] = \
        Mul(Rat(Int("172368932329209"), Mul(Int(25), Pow(10, 8))), Sym("decipa"))
    CONVERSIONS["DECIPASCAL:TERAPASCAL"] = \
        Mul(Pow(10, 13), Sym("decipa"))
    CONVERSIONS["DECIPASCAL:TORR"] = \
        Mul(Rat(Int(101325), Int(76)), Sym("decipa"))
    CONVERSIONS["DECIPASCAL:YOCTOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 23)), Sym("decipa"))
    CONVERSIONS["DECIPASCAL:YOTTAPASCAL"] = \
        Mul(Pow(10, 25), Sym("decipa"))
    CONVERSIONS["DECIPASCAL:ZEPTOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 20)), Sym("decipa"))
    CONVERSIONS["DECIPASCAL:ZETTAPASCAL"] = \
        Mul(Pow(10, 22), Sym("decipa"))
    CONVERSIONS["EXAPASCAL:ATHMOSPHERE"] = \
        Mul(Rat(Int(4053), Mul(Int(4), Pow(10, 16))), Sym("exapa"))
    CONVERSIONS["EXAPASCAL:ATTOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 36)), Sym("exapa"))
    CONVERSIONS["EXAPASCAL:BAR"] = \
        Mul(Rat(Int(1), Pow(10, 13)), Sym("exapa"))
    CONVERSIONS["EXAPASCAL:CENTIBAR"] = \
        Mul(Rat(Int(1), Pow(10, 15)), Sym("exapa"))
    CONVERSIONS["EXAPASCAL:CENTIPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 20)), Sym("exapa"))
    CONVERSIONS["EXAPASCAL:DECAPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 17)), Sym("exapa"))
    CONVERSIONS["EXAPASCAL:DECIBAR"] = \
        Mul(Rat(Int(1), Pow(10, 14)), Sym("exapa"))
    CONVERSIONS["EXAPASCAL:DECIPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 19)), Sym("exapa"))
    CONVERSIONS["EXAPASCAL:FEMTOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 33)), Sym("exapa"))
    CONVERSIONS["EXAPASCAL:GIGAPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("exapa"))
    CONVERSIONS["EXAPASCAL:HECTOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 16)), Sym("exapa"))
    CONVERSIONS["EXAPASCAL:KILOBAR"] = \
        Mul(Rat(Int(1), Pow(10, 10)), Sym("exapa"))
    CONVERSIONS["EXAPASCAL:KILOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 15)), Sym("exapa"))
    CONVERSIONS["EXAPASCAL:MEGABAR"] = \
        Mul(Rat(Int(1), Pow(10, 7)), Sym("exapa"))
    CONVERSIONS["EXAPASCAL:MEGAPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 12)), Sym("exapa"))
    CONVERSIONS["EXAPASCAL:MICROPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 24)), Sym("exapa"))
    CONVERSIONS["EXAPASCAL:MILLIBAR"] = \
        Mul(Rat(Int(1), Pow(10, 16)), Sym("exapa"))
    CONVERSIONS["EXAPASCAL:MILLIPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 21)), Sym("exapa"))
    CONVERSIONS["EXAPASCAL:MILLITORR"] = \
        Mul(Rat(Int(4053), Mul(Int(304), Pow(10, 20))), Sym("exapa"))
    CONVERSIONS["EXAPASCAL:MMHG"] = \
        Mul(Rat(Int("26664477483"), Mul(Int(2), Pow(10, 26))), Sym("exapa"))
    CONVERSIONS["EXAPASCAL:NANOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 27)), Sym("exapa"))
    CONVERSIONS["EXAPASCAL:PASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 18)), Sym("exapa"))
    CONVERSIONS["EXAPASCAL:PETAPASCAL"] = \
        Mul(Rat(Int(1), Int(1000)), Sym("exapa"))
    CONVERSIONS["EXAPASCAL:PICOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 30)), Sym("exapa"))
    CONVERSIONS["EXAPASCAL:PSI"] = \
        Mul(Rat(Int("172368932329209"), Mul(Int(25), Pow(10, 27))), Sym("exapa"))
    CONVERSIONS["EXAPASCAL:TERAPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("exapa"))
    CONVERSIONS["EXAPASCAL:TORR"] = \
        Mul(Rat(Int(4053), Mul(Int(304), Pow(10, 17))), Sym("exapa"))
    CONVERSIONS["EXAPASCAL:YOCTOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 42)), Sym("exapa"))
    CONVERSIONS["EXAPASCAL:YOTTAPASCAL"] = \
        Mul(Pow(10, 6), Sym("exapa"))
    CONVERSIONS["EXAPASCAL:ZEPTOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 39)), Sym("exapa"))
    CONVERSIONS["EXAPASCAL:ZETTAPASCAL"] = \
        Mul(Int(1000), Sym("exapa"))
    CONVERSIONS["FEMTOPASCAL:ATHMOSPHERE"] = \
        Mul(Mul(Int(101325), Pow(10, 15)), Sym("femtopa"))
    CONVERSIONS["FEMTOPASCAL:ATTOPASCAL"] = \
        Mul(Rat(Int(1), Int(1000)), Sym("femtopa"))
    CONVERSIONS["FEMTOPASCAL:BAR"] = \
        Mul(Pow(10, 20), Sym("femtopa"))
    CONVERSIONS["FEMTOPASCAL:CENTIBAR"] = \
        Mul(Pow(10, 18), Sym("femtopa"))
    CONVERSIONS["FEMTOPASCAL:CENTIPASCAL"] = \
        Mul(Pow(10, 13), Sym("femtopa"))
    CONVERSIONS["FEMTOPASCAL:DECAPASCAL"] = \
        Mul(Pow(10, 16), Sym("femtopa"))
    CONVERSIONS["FEMTOPASCAL:DECIBAR"] = \
        Mul(Pow(10, 19), Sym("femtopa"))
    CONVERSIONS["FEMTOPASCAL:DECIPASCAL"] = \
        Mul(Pow(10, 14), Sym("femtopa"))
    CONVERSIONS["FEMTOPASCAL:EXAPASCAL"] = \
        Mul(Pow(10, 33), Sym("femtopa"))
    CONVERSIONS["FEMTOPASCAL:GIGAPASCAL"] = \
        Mul(Pow(10, 24), Sym("femtopa"))
    CONVERSIONS["FEMTOPASCAL:HECTOPASCAL"] = \
        Mul(Pow(10, 17), Sym("femtopa"))
    CONVERSIONS["FEMTOPASCAL:KILOBAR"] = \
        Mul(Pow(10, 23), Sym("femtopa"))
    CONVERSIONS["FEMTOPASCAL:KILOPASCAL"] = \
        Mul(Pow(10, 18), Sym("femtopa"))
    CONVERSIONS["FEMTOPASCAL:MEGABAR"] = \
        Mul(Pow(10, 26), Sym("femtopa"))
    CONVERSIONS["FEMTOPASCAL:MEGAPASCAL"] = \
        Mul(Pow(10, 21), Sym("femtopa"))
    CONVERSIONS["FEMTOPASCAL:MICROPASCAL"] = \
        Mul(Pow(10, 9), Sym("femtopa"))
    CONVERSIONS["FEMTOPASCAL:MILLIBAR"] = \
        Mul(Pow(10, 17), Sym("femtopa"))
    CONVERSIONS["FEMTOPASCAL:MILLIPASCAL"] = \
        Mul(Pow(10, 12), Sym("femtopa"))
    CONVERSIONS["FEMTOPASCAL:MILLITORR"] = \
        Mul(Rat(Mul(Int(2533125), Pow(10, 9)), Int(19)), Sym("femtopa"))
    CONVERSIONS["FEMTOPASCAL:MMHG"] = \
        Mul(Mul(Int("133322387415"), Pow(10, 6)), Sym("femtopa"))
    CONVERSIONS["FEMTOPASCAL:NANOPASCAL"] = \
        Mul(Pow(10, 6), Sym("femtopa"))
    CONVERSIONS["FEMTOPASCAL:PASCAL"] = \
        Mul(Pow(10, 15), Sym("femtopa"))
    CONVERSIONS["FEMTOPASCAL:PETAPASCAL"] = \
        Mul(Pow(10, 30), Sym("femtopa"))
    CONVERSIONS["FEMTOPASCAL:PICOPASCAL"] = \
        Mul(Int(1000), Sym("femtopa"))
    CONVERSIONS["FEMTOPASCAL:PSI"] = \
        Mul(Mul(Int("689475729316836"), Pow(10, 4)), Sym("femtopa"))
    CONVERSIONS["FEMTOPASCAL:TERAPASCAL"] = \
        Mul(Pow(10, 27), Sym("femtopa"))
    CONVERSIONS["FEMTOPASCAL:TORR"] = \
        Mul(Rat(Mul(Int(2533125), Pow(10, 12)), Int(19)), Sym("femtopa"))
    CONVERSIONS["FEMTOPASCAL:YOCTOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("femtopa"))
    CONVERSIONS["FEMTOPASCAL:YOTTAPASCAL"] = \
        Mul(Pow(10, 39), Sym("femtopa"))
    CONVERSIONS["FEMTOPASCAL:ZEPTOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("femtopa"))
    CONVERSIONS["FEMTOPASCAL:ZETTAPASCAL"] = \
        Mul(Pow(10, 36), Sym("femtopa"))
    CONVERSIONS["GIGAPASCAL:ATHMOSPHERE"] = \
        Mul(Rat(Int(4053), Mul(Int(4), Pow(10, 7))), Sym("gigapa"))
    CONVERSIONS["GIGAPASCAL:ATTOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 27)), Sym("gigapa"))
    CONVERSIONS["GIGAPASCAL:BAR"] = \
        Mul(Rat(Int(1), Pow(10, 4)), Sym("gigapa"))
    CONVERSIONS["GIGAPASCAL:CENTIBAR"] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("gigapa"))
    CONVERSIONS["GIGAPASCAL:CENTIPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 11)), Sym("gigapa"))
    CONVERSIONS["GIGAPASCAL:DECAPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 8)), Sym("gigapa"))
    CONVERSIONS["GIGAPASCAL:DECIBAR"] = \
        Mul(Rat(Int(1), Pow(10, 5)), Sym("gigapa"))
    CONVERSIONS["GIGAPASCAL:DECIPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 10)), Sym("gigapa"))
    CONVERSIONS["GIGAPASCAL:EXAPASCAL"] = \
        Mul(Pow(10, 9), Sym("gigapa"))
    CONVERSIONS["GIGAPASCAL:FEMTOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 24)), Sym("gigapa"))
    CONVERSIONS["GIGAPASCAL:HECTOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 7)), Sym("gigapa"))
    CONVERSIONS["GIGAPASCAL:KILOBAR"] = \
        Mul(Rat(Int(1), Int(10)), Sym("gigapa"))
    CONVERSIONS["GIGAPASCAL:KILOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("gigapa"))
    CONVERSIONS["GIGAPASCAL:MEGABAR"] = \
        Mul(Int(100), Sym("gigapa"))
    CONVERSIONS["GIGAPASCAL:MEGAPASCAL"] = \
        Mul(Rat(Int(1), Int(1000)), Sym("gigapa"))
    CONVERSIONS["GIGAPASCAL:MICROPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 15)), Sym("gigapa"))
    CONVERSIONS["GIGAPASCAL:MILLIBAR"] = \
        Mul(Rat(Int(1), Pow(10, 7)), Sym("gigapa"))
    CONVERSIONS["GIGAPASCAL:MILLIPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 12)), Sym("gigapa"))
    CONVERSIONS["GIGAPASCAL:MILLITORR"] = \
        Mul(Rat(Int(4053), Mul(Int(304), Pow(10, 11))), Sym("gigapa"))
    CONVERSIONS["GIGAPASCAL:MMHG"] = \
        Mul(Rat(Int("26664477483"), Mul(Int(2), Pow(10, 17))), Sym("gigapa"))
    CONVERSIONS["GIGAPASCAL:NANOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 18)), Sym("gigapa"))
    CONVERSIONS["GIGAPASCAL:PASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("gigapa"))
    CONVERSIONS["GIGAPASCAL:PETAPASCAL"] = \
        Mul(Pow(10, 6), Sym("gigapa"))
    CONVERSIONS["GIGAPASCAL:PICOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 21)), Sym("gigapa"))
    CONVERSIONS["GIGAPASCAL:PSI"] = \
        Mul(Rat(Int("172368932329209"), Mul(Int(25), Pow(10, 18))), Sym("gigapa"))
    CONVERSIONS["GIGAPASCAL:TERAPASCAL"] = \
        Mul(Int(1000), Sym("gigapa"))
    CONVERSIONS["GIGAPASCAL:TORR"] = \
        Mul(Rat(Int(4053), Mul(Int(304), Pow(10, 8))), Sym("gigapa"))
    CONVERSIONS["GIGAPASCAL:YOCTOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 33)), Sym("gigapa"))
    CONVERSIONS["GIGAPASCAL:YOTTAPASCAL"] = \
        Mul(Pow(10, 15), Sym("gigapa"))
    CONVERSIONS["GIGAPASCAL:ZEPTOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 30)), Sym("gigapa"))
    CONVERSIONS["GIGAPASCAL:ZETTAPASCAL"] = \
        Mul(Pow(10, 12), Sym("gigapa"))
    CONVERSIONS["HECTOPASCAL:ATHMOSPHERE"] = \
        Mul(Rat(Int(4053), Int(4)), Sym("hectopa"))
    CONVERSIONS["HECTOPASCAL:ATTOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 20)), Sym("hectopa"))
    CONVERSIONS["HECTOPASCAL:BAR"] = \
        Mul(Int(1000), Sym("hectopa"))
    CONVERSIONS["HECTOPASCAL:CENTIBAR"] = \
        Mul(Int(10), Sym("hectopa"))
    CONVERSIONS["HECTOPASCAL:CENTIPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 4)), Sym("hectopa"))
    CONVERSIONS["HECTOPASCAL:DECAPASCAL"] = \
        Mul(Rat(Int(1), Int(10)), Sym("hectopa"))
    CONVERSIONS["HECTOPASCAL:DECIBAR"] = \
        Mul(Int(100), Sym("hectopa"))
    CONVERSIONS["HECTOPASCAL:DECIPASCAL"] = \
        Mul(Rat(Int(1), Int(1000)), Sym("hectopa"))
    CONVERSIONS["HECTOPASCAL:EXAPASCAL"] = \
        Mul(Pow(10, 16), Sym("hectopa"))
    CONVERSIONS["HECTOPASCAL:FEMTOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 17)), Sym("hectopa"))
    CONVERSIONS["HECTOPASCAL:GIGAPASCAL"] = \
        Mul(Pow(10, 7), Sym("hectopa"))
    CONVERSIONS["HECTOPASCAL:KILOBAR"] = \
        Mul(Pow(10, 6), Sym("hectopa"))
    CONVERSIONS["HECTOPASCAL:KILOPASCAL"] = \
        Mul(Int(10), Sym("hectopa"))
    CONVERSIONS["HECTOPASCAL:MEGABAR"] = \
        Mul(Pow(10, 9), Sym("hectopa"))
    CONVERSIONS["HECTOPASCAL:MEGAPASCAL"] = \
        Mul(Pow(10, 4), Sym("hectopa"))
    CONVERSIONS["HECTOPASCAL:MICROPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 8)), Sym("hectopa"))
    CONVERSIONS["HECTOPASCAL:MILLIBAR"] = \
        Sym("hectopa")
    CONVERSIONS["HECTOPASCAL:MILLIPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 5)), Sym("hectopa"))
    CONVERSIONS["HECTOPASCAL:MILLITORR"] = \
        Mul(Rat(Int(4053), Mul(Int(304), Pow(10, 4))), Sym("hectopa"))
    CONVERSIONS["HECTOPASCAL:MMHG"] = \
        Mul(Rat(Int("26664477483"), Mul(Int(2), Pow(10, 10))), Sym("hectopa"))
    CONVERSIONS["HECTOPASCAL:NANOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 11)), Sym("hectopa"))
    CONVERSIONS["HECTOPASCAL:PASCAL"] = \
        Mul(Rat(Int(1), Int(100)), Sym("hectopa"))
    CONVERSIONS["HECTOPASCAL:PETAPASCAL"] = \
        Mul(Pow(10, 13), Sym("hectopa"))
    CONVERSIONS["HECTOPASCAL:PICOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 14)), Sym("hectopa"))
    CONVERSIONS["HECTOPASCAL:PSI"] = \
        Mul(Rat(Int("172368932329209"), Mul(Int(25), Pow(10, 11))), Sym("hectopa"))
    CONVERSIONS["HECTOPASCAL:TERAPASCAL"] = \
        Mul(Pow(10, 10), Sym("hectopa"))
    CONVERSIONS["HECTOPASCAL:TORR"] = \
        Mul(Rat(Int(4053), Int(3040)), Sym("hectopa"))
    CONVERSIONS["HECTOPASCAL:YOCTOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 26)), Sym("hectopa"))
    CONVERSIONS["HECTOPASCAL:YOTTAPASCAL"] = \
        Mul(Pow(10, 22), Sym("hectopa"))
    CONVERSIONS["HECTOPASCAL:ZEPTOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 23)), Sym("hectopa"))
    CONVERSIONS["HECTOPASCAL:ZETTAPASCAL"] = \
        Mul(Pow(10, 19), Sym("hectopa"))
    CONVERSIONS["KILOBAR:ATHMOSPHERE"] = \
        Mul(Rat(Int(4053), Mul(Int(4), Pow(10, 6))), Sym("kbar"))
    CONVERSIONS["KILOBAR:ATTOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 26)), Sym("kbar"))
    CONVERSIONS["KILOBAR:BAR"] = \
        Mul(Rat(Int(1), Int(1000)), Sym("kbar"))
    CONVERSIONS["KILOBAR:CENTIBAR"] = \
        Mul(Rat(Int(1), Pow(10, 5)), Sym("kbar"))
    CONVERSIONS["KILOBAR:CENTIPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 10)), Sym("kbar"))
    CONVERSIONS["KILOBAR:DECAPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 7)), Sym("kbar"))
    CONVERSIONS["KILOBAR:DECIBAR"] = \
        Mul(Rat(Int(1), Pow(10, 4)), Sym("kbar"))
    CONVERSIONS["KILOBAR:DECIPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("kbar"))
    CONVERSIONS["KILOBAR:EXAPASCAL"] = \
        Mul(Pow(10, 10), Sym("kbar"))
    CONVERSIONS["KILOBAR:FEMTOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 23)), Sym("kbar"))
    CONVERSIONS["KILOBAR:GIGAPASCAL"] = \
        Mul(Int(10), Sym("kbar"))
    CONVERSIONS["KILOBAR:HECTOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("kbar"))
    CONVERSIONS["KILOBAR:KILOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 5)), Sym("kbar"))
    CONVERSIONS["KILOBAR:MEGABAR"] = \
        Mul(Int(1000), Sym("kbar"))
    CONVERSIONS["KILOBAR:MEGAPASCAL"] = \
        Mul(Rat(Int(1), Int(100)), Sym("kbar"))
    CONVERSIONS["KILOBAR:MICROPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 14)), Sym("kbar"))
    CONVERSIONS["KILOBAR:MILLIBAR"] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("kbar"))
    CONVERSIONS["KILOBAR:MILLIPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 11)), Sym("kbar"))
    CONVERSIONS["KILOBAR:MILLITORR"] = \
        Mul(Rat(Int(4053), Mul(Int(304), Pow(10, 10))), Sym("kbar"))
    CONVERSIONS["KILOBAR:MMHG"] = \
        Mul(Rat(Int("26664477483"), Mul(Int(2), Pow(10, 16))), Sym("kbar"))
    CONVERSIONS["KILOBAR:NANOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 17)), Sym("kbar"))
    CONVERSIONS["KILOBAR:PASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 8)), Sym("kbar"))
    CONVERSIONS["KILOBAR:PETAPASCAL"] = \
        Mul(Pow(10, 7), Sym("kbar"))
    CONVERSIONS["KILOBAR:PICOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 20)), Sym("kbar"))
    CONVERSIONS["KILOBAR:PSI"] = \
        Mul(Rat(Int("172368932329209"), Mul(Int(25), Pow(10, 17))), Sym("kbar"))
    CONVERSIONS["KILOBAR:TERAPASCAL"] = \
        Mul(Pow(10, 4), Sym("kbar"))
    CONVERSIONS["KILOBAR:TORR"] = \
        Mul(Rat(Int(4053), Mul(Int(304), Pow(10, 7))), Sym("kbar"))
    CONVERSIONS["KILOBAR:YOCTOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 32)), Sym("kbar"))
    CONVERSIONS["KILOBAR:YOTTAPASCAL"] = \
        Mul(Pow(10, 16), Sym("kbar"))
    CONVERSIONS["KILOBAR:ZEPTOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 29)), Sym("kbar"))
    CONVERSIONS["KILOBAR:ZETTAPASCAL"] = \
        Mul(Pow(10, 13), Sym("kbar"))
    CONVERSIONS["KILOPASCAL:ATHMOSPHERE"] = \
        Mul(Rat(Int(4053), Int(40)), Sym("kilopa"))
    CONVERSIONS["KILOPASCAL:ATTOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 21)), Sym("kilopa"))
    CONVERSIONS["KILOPASCAL:BAR"] = \
        Mul(Int(100), Sym("kilopa"))
    CONVERSIONS["KILOPASCAL:CENTIBAR"] = \
        Sym("kilopa")
    CONVERSIONS["KILOPASCAL:CENTIPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 5)), Sym("kilopa"))
    CONVERSIONS["KILOPASCAL:DECAPASCAL"] = \
        Mul(Rat(Int(1), Int(100)), Sym("kilopa"))
    CONVERSIONS["KILOPASCAL:DECIBAR"] = \
        Mul(Int(10), Sym("kilopa"))
    CONVERSIONS["KILOPASCAL:DECIPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 4)), Sym("kilopa"))
    CONVERSIONS["KILOPASCAL:EXAPASCAL"] = \
        Mul(Pow(10, 15), Sym("kilopa"))
    CONVERSIONS["KILOPASCAL:FEMTOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 18)), Sym("kilopa"))
    CONVERSIONS["KILOPASCAL:GIGAPASCAL"] = \
        Mul(Pow(10, 6), Sym("kilopa"))
    CONVERSIONS["KILOPASCAL:HECTOPASCAL"] = \
        Mul(Rat(Int(1), Int(10)), Sym("kilopa"))
    CONVERSIONS["KILOPASCAL:KILOBAR"] = \
        Mul(Pow(10, 5), Sym("kilopa"))
    CONVERSIONS["KILOPASCAL:MEGABAR"] = \
        Mul(Pow(10, 8), Sym("kilopa"))
    CONVERSIONS["KILOPASCAL:MEGAPASCAL"] = \
        Mul(Int(1000), Sym("kilopa"))
    CONVERSIONS["KILOPASCAL:MICROPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("kilopa"))
    CONVERSIONS["KILOPASCAL:MILLIBAR"] = \
        Mul(Rat(Int(1), Int(10)), Sym("kilopa"))
    CONVERSIONS["KILOPASCAL:MILLIPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("kilopa"))
    CONVERSIONS["KILOPASCAL:MILLITORR"] = \
        Mul(Rat(Int(4053), Mul(Int(304), Pow(10, 5))), Sym("kilopa"))
    CONVERSIONS["KILOPASCAL:MMHG"] = \
        Mul(Rat(Int("26664477483"), Mul(Int(2), Pow(10, 11))), Sym("kilopa"))
    CONVERSIONS["KILOPASCAL:NANOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 12)), Sym("kilopa"))
    CONVERSIONS["KILOPASCAL:PASCAL"] = \
        Mul(Rat(Int(1), Int(1000)), Sym("kilopa"))
    CONVERSIONS["KILOPASCAL:PETAPASCAL"] = \
        Mul(Pow(10, 12), Sym("kilopa"))
    CONVERSIONS["KILOPASCAL:PICOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 15)), Sym("kilopa"))
    CONVERSIONS["KILOPASCAL:PSI"] = \
        Mul(Rat(Int("172368932329209"), Mul(Int(25), Pow(10, 12))), Sym("kilopa"))
    CONVERSIONS["KILOPASCAL:TERAPASCAL"] = \
        Mul(Pow(10, 9), Sym("kilopa"))
    CONVERSIONS["KILOPASCAL:TORR"] = \
        Mul(Rat(Int(4053), Int(30400)), Sym("kilopa"))
    CONVERSIONS["KILOPASCAL:YOCTOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 27)), Sym("kilopa"))
    CONVERSIONS["KILOPASCAL:YOTTAPASCAL"] = \
        Mul(Pow(10, 21), Sym("kilopa"))
    CONVERSIONS["KILOPASCAL:ZEPTOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 24)), Sym("kilopa"))
    CONVERSIONS["KILOPASCAL:ZETTAPASCAL"] = \
        Mul(Pow(10, 18), Sym("kilopa"))
    CONVERSIONS["MEGABAR:ATHMOSPHERE"] = \
        Mul(Rat(Int(4053), Mul(Int(4), Pow(10, 9))), Sym("megabar"))
    CONVERSIONS["MEGABAR:ATTOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 29)), Sym("megabar"))
    CONVERSIONS["MEGABAR:BAR"] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("megabar"))
    CONVERSIONS["MEGABAR:CENTIBAR"] = \
        Mul(Rat(Int(1), Pow(10, 8)), Sym("megabar"))
    CONVERSIONS["MEGABAR:CENTIPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 13)), Sym("megabar"))
    CONVERSIONS["MEGABAR:DECAPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 10)), Sym("megabar"))
    CONVERSIONS["MEGABAR:DECIBAR"] = \
        Mul(Rat(Int(1), Pow(10, 7)), Sym("megabar"))
    CONVERSIONS["MEGABAR:DECIPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 12)), Sym("megabar"))
    CONVERSIONS["MEGABAR:EXAPASCAL"] = \
        Mul(Pow(10, 7), Sym("megabar"))
    CONVERSIONS["MEGABAR:FEMTOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 26)), Sym("megabar"))
    CONVERSIONS["MEGABAR:GIGAPASCAL"] = \
        Mul(Rat(Int(1), Int(100)), Sym("megabar"))
    CONVERSIONS["MEGABAR:HECTOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("megabar"))
    CONVERSIONS["MEGABAR:KILOBAR"] = \
        Mul(Rat(Int(1), Int(1000)), Sym("megabar"))
    CONVERSIONS["MEGABAR:KILOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 8)), Sym("megabar"))
    CONVERSIONS["MEGABAR:MEGAPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 5)), Sym("megabar"))
    CONVERSIONS["MEGABAR:MICROPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 17)), Sym("megabar"))
    CONVERSIONS["MEGABAR:MILLIBAR"] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("megabar"))
    CONVERSIONS["MEGABAR:MILLIPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 14)), Sym("megabar"))
    CONVERSIONS["MEGABAR:MILLITORR"] = \
        Mul(Rat(Int(4053), Mul(Int(304), Pow(10, 13))), Sym("megabar"))
    CONVERSIONS["MEGABAR:MMHG"] = \
        Mul(Rat(Int("26664477483"), Mul(Int(2), Pow(10, 19))), Sym("megabar"))
    CONVERSIONS["MEGABAR:NANOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 20)), Sym("megabar"))
    CONVERSIONS["MEGABAR:PASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 11)), Sym("megabar"))
    CONVERSIONS["MEGABAR:PETAPASCAL"] = \
        Mul(Pow(10, 4), Sym("megabar"))
    CONVERSIONS["MEGABAR:PICOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 23)), Sym("megabar"))
    CONVERSIONS["MEGABAR:PSI"] = \
        Mul(Rat(Int("172368932329209"), Mul(Int(25), Pow(10, 20))), Sym("megabar"))
    CONVERSIONS["MEGABAR:TERAPASCAL"] = \
        Mul(Int(10), Sym("megabar"))
    CONVERSIONS["MEGABAR:TORR"] = \
        Mul(Rat(Int(4053), Mul(Int(304), Pow(10, 10))), Sym("megabar"))
    CONVERSIONS["MEGABAR:YOCTOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 35)), Sym("megabar"))
    CONVERSIONS["MEGABAR:YOTTAPASCAL"] = \
        Mul(Pow(10, 13), Sym("megabar"))
    CONVERSIONS["MEGABAR:ZEPTOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 32)), Sym("megabar"))
    CONVERSIONS["MEGABAR:ZETTAPASCAL"] = \
        Mul(Pow(10, 10), Sym("megabar"))
    CONVERSIONS["MEGAPASCAL:ATHMOSPHERE"] = \
        Mul(Rat(Int(4053), Mul(Int(4), Pow(10, 4))), Sym("megapa"))
    CONVERSIONS["MEGAPASCAL:ATTOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 24)), Sym("megapa"))
    CONVERSIONS["MEGAPASCAL:BAR"] = \
        Mul(Rat(Int(1), Int(10)), Sym("megapa"))
    CONVERSIONS["MEGAPASCAL:CENTIBAR"] = \
        Mul(Rat(Int(1), Int(1000)), Sym("megapa"))
    CONVERSIONS["MEGAPASCAL:CENTIPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 8)), Sym("megapa"))
    CONVERSIONS["MEGAPASCAL:DECAPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 5)), Sym("megapa"))
    CONVERSIONS["MEGAPASCAL:DECIBAR"] = \
        Mul(Rat(Int(1), Int(100)), Sym("megapa"))
    CONVERSIONS["MEGAPASCAL:DECIPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 7)), Sym("megapa"))
    CONVERSIONS["MEGAPASCAL:EXAPASCAL"] = \
        Mul(Pow(10, 12), Sym("megapa"))
    CONVERSIONS["MEGAPASCAL:FEMTOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 21)), Sym("megapa"))
    CONVERSIONS["MEGAPASCAL:GIGAPASCAL"] = \
        Mul(Int(1000), Sym("megapa"))
    CONVERSIONS["MEGAPASCAL:HECTOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 4)), Sym("megapa"))
    CONVERSIONS["MEGAPASCAL:KILOBAR"] = \
        Mul(Int(100), Sym("megapa"))
    CONVERSIONS["MEGAPASCAL:KILOPASCAL"] = \
        Mul(Rat(Int(1), Int(1000)), Sym("megapa"))
    CONVERSIONS["MEGAPASCAL:MEGABAR"] = \
        Mul(Pow(10, 5), Sym("megapa"))
    CONVERSIONS["MEGAPASCAL:MICROPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 12)), Sym("megapa"))
    CONVERSIONS["MEGAPASCAL:MILLIBAR"] = \
        Mul(Rat(Int(1), Pow(10, 4)), Sym("megapa"))
    CONVERSIONS["MEGAPASCAL:MILLIPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("megapa"))
    CONVERSIONS["MEGAPASCAL:MILLITORR"] = \
        Mul(Rat(Int(4053), Mul(Int(304), Pow(10, 8))), Sym("megapa"))
    CONVERSIONS["MEGAPASCAL:MMHG"] = \
        Mul(Rat(Int("26664477483"), Mul(Int(2), Pow(10, 14))), Sym("megapa"))
    CONVERSIONS["MEGAPASCAL:NANOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 15)), Sym("megapa"))
    CONVERSIONS["MEGAPASCAL:PASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("megapa"))
    CONVERSIONS["MEGAPASCAL:PETAPASCAL"] = \
        Mul(Pow(10, 9), Sym("megapa"))
    CONVERSIONS["MEGAPASCAL:PICOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 18)), Sym("megapa"))
    CONVERSIONS["MEGAPASCAL:PSI"] = \
        Mul(Rat(Int("172368932329209"), Mul(Int(25), Pow(10, 15))), Sym("megapa"))
    CONVERSIONS["MEGAPASCAL:TERAPASCAL"] = \
        Mul(Pow(10, 6), Sym("megapa"))
    CONVERSIONS["MEGAPASCAL:TORR"] = \
        Mul(Rat(Int(4053), Mul(Int(304), Pow(10, 5))), Sym("megapa"))
    CONVERSIONS["MEGAPASCAL:YOCTOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 30)), Sym("megapa"))
    CONVERSIONS["MEGAPASCAL:YOTTAPASCAL"] = \
        Mul(Pow(10, 18), Sym("megapa"))
    CONVERSIONS["MEGAPASCAL:ZEPTOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 27)), Sym("megapa"))
    CONVERSIONS["MEGAPASCAL:ZETTAPASCAL"] = \
        Mul(Pow(10, 15), Sym("megapa"))
    CONVERSIONS["MICROPASCAL:ATHMOSPHERE"] = \
        Mul(Mul(Int(101325), Pow(10, 6)), Sym("micropa"))
    CONVERSIONS["MICROPASCAL:ATTOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 12)), Sym("micropa"))
    CONVERSIONS["MICROPASCAL:BAR"] = \
        Mul(Pow(10, 11), Sym("micropa"))
    CONVERSIONS["MICROPASCAL:CENTIBAR"] = \
        Mul(Pow(10, 9), Sym("micropa"))
    CONVERSIONS["MICROPASCAL:CENTIPASCAL"] = \
        Mul(Pow(10, 4), Sym("micropa"))
    CONVERSIONS["MICROPASCAL:DECAPASCAL"] = \
        Mul(Pow(10, 7), Sym("micropa"))
    CONVERSIONS["MICROPASCAL:DECIBAR"] = \
        Mul(Pow(10, 10), Sym("micropa"))
    CONVERSIONS["MICROPASCAL:DECIPASCAL"] = \
        Mul(Pow(10, 5), Sym("micropa"))
    CONVERSIONS["MICROPASCAL:EXAPASCAL"] = \
        Mul(Pow(10, 24), Sym("micropa"))
    CONVERSIONS["MICROPASCAL:FEMTOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("micropa"))
    CONVERSIONS["MICROPASCAL:GIGAPASCAL"] = \
        Mul(Pow(10, 15), Sym("micropa"))
    CONVERSIONS["MICROPASCAL:HECTOPASCAL"] = \
        Mul(Pow(10, 8), Sym("micropa"))
    CONVERSIONS["MICROPASCAL:KILOBAR"] = \
        Mul(Pow(10, 14), Sym("micropa"))
    CONVERSIONS["MICROPASCAL:KILOPASCAL"] = \
        Mul(Pow(10, 9), Sym("micropa"))
    CONVERSIONS["MICROPASCAL:MEGABAR"] = \
        Mul(Pow(10, 17), Sym("micropa"))
    CONVERSIONS["MICROPASCAL:MEGAPASCAL"] = \
        Mul(Pow(10, 12), Sym("micropa"))
    CONVERSIONS["MICROPASCAL:MILLIBAR"] = \
        Mul(Pow(10, 8), Sym("micropa"))
    CONVERSIONS["MICROPASCAL:MILLIPASCAL"] = \
        Mul(Int(1000), Sym("micropa"))
    CONVERSIONS["MICROPASCAL:MILLITORR"] = \
        Mul(Rat(Int(2533125), Int(19)), Sym("micropa"))
    CONVERSIONS["MICROPASCAL:MMHG"] = \
        Mul(Rat(Int("26664477483"), Int(200)), Sym("micropa"))
    CONVERSIONS["MICROPASCAL:NANOPASCAL"] = \
        Mul(Rat(Int(1), Int(1000)), Sym("micropa"))
    CONVERSIONS["MICROPASCAL:PASCAL"] = \
        Mul(Pow(10, 6), Sym("micropa"))
    CONVERSIONS["MICROPASCAL:PETAPASCAL"] = \
        Mul(Pow(10, 21), Sym("micropa"))
    CONVERSIONS["MICROPASCAL:PICOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("micropa"))
    CONVERSIONS["MICROPASCAL:PSI"] = \
        Mul(Rat(Int("172368932329209"), Int(25000)), Sym("micropa"))
    CONVERSIONS["MICROPASCAL:TERAPASCAL"] = \
        Mul(Pow(10, 18), Sym("micropa"))
    CONVERSIONS["MICROPASCAL:TORR"] = \
        Mul(Rat(Int("2533125000"), Int(19)), Sym("micropa"))
    CONVERSIONS["MICROPASCAL:YOCTOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 18)), Sym("micropa"))
    CONVERSIONS["MICROPASCAL:YOTTAPASCAL"] = \
        Mul(Pow(10, 30), Sym("micropa"))
    CONVERSIONS["MICROPASCAL:ZEPTOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 15)), Sym("micropa"))
    CONVERSIONS["MICROPASCAL:ZETTAPASCAL"] = \
        Mul(Pow(10, 27), Sym("micropa"))
    CONVERSIONS["MILLIBAR:ATHMOSPHERE"] = \
        Mul(Rat(Int(4053), Int(4)), Sym("mbar"))
    CONVERSIONS["MILLIBAR:ATTOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 20)), Sym("mbar"))
    CONVERSIONS["MILLIBAR:BAR"] = \
        Mul(Int(1000), Sym("mbar"))
    CONVERSIONS["MILLIBAR:CENTIBAR"] = \
        Mul(Int(10), Sym("mbar"))
    CONVERSIONS["MILLIBAR:CENTIPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 4)), Sym("mbar"))
    CONVERSIONS["MILLIBAR:DECAPASCAL"] = \
        Mul(Rat(Int(1), Int(10)), Sym("mbar"))
    CONVERSIONS["MILLIBAR:DECIBAR"] = \
        Mul(Int(100), Sym("mbar"))
    CONVERSIONS["MILLIBAR:DECIPASCAL"] = \
        Mul(Rat(Int(1), Int(1000)), Sym("mbar"))
    CONVERSIONS["MILLIBAR:EXAPASCAL"] = \
        Mul(Pow(10, 16), Sym("mbar"))
    CONVERSIONS["MILLIBAR:FEMTOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 17)), Sym("mbar"))
    CONVERSIONS["MILLIBAR:GIGAPASCAL"] = \
        Mul(Pow(10, 7), Sym("mbar"))
    CONVERSIONS["MILLIBAR:HECTOPASCAL"] = \
        Sym("mbar")
    CONVERSIONS["MILLIBAR:KILOBAR"] = \
        Mul(Pow(10, 6), Sym("mbar"))
    CONVERSIONS["MILLIBAR:KILOPASCAL"] = \
        Mul(Int(10), Sym("mbar"))
    CONVERSIONS["MILLIBAR:MEGABAR"] = \
        Mul(Pow(10, 9), Sym("mbar"))
    CONVERSIONS["MILLIBAR:MEGAPASCAL"] = \
        Mul(Pow(10, 4), Sym("mbar"))
    CONVERSIONS["MILLIBAR:MICROPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 8)), Sym("mbar"))
    CONVERSIONS["MILLIBAR:MILLIPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 5)), Sym("mbar"))
    CONVERSIONS["MILLIBAR:MILLITORR"] = \
        Mul(Rat(Int(4053), Mul(Int(304), Pow(10, 4))), Sym("mbar"))
    CONVERSIONS["MILLIBAR:MMHG"] = \
        Mul(Rat(Int("26664477483"), Mul(Int(2), Pow(10, 10))), Sym("mbar"))
    CONVERSIONS["MILLIBAR:NANOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 11)), Sym("mbar"))
    CONVERSIONS["MILLIBAR:PASCAL"] = \
        Mul(Rat(Int(1), Int(100)), Sym("mbar"))
    CONVERSIONS["MILLIBAR:PETAPASCAL"] = \
        Mul(Pow(10, 13), Sym("mbar"))
    CONVERSIONS["MILLIBAR:PICOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 14)), Sym("mbar"))
    CONVERSIONS["MILLIBAR:PSI"] = \
        Mul(Rat(Int("172368932329209"), Mul(Int(25), Pow(10, 11))), Sym("mbar"))
    CONVERSIONS["MILLIBAR:TERAPASCAL"] = \
        Mul(Pow(10, 10), Sym("mbar"))
    CONVERSIONS["MILLIBAR:TORR"] = \
        Mul(Rat(Int(4053), Int(3040)), Sym("mbar"))
    CONVERSIONS["MILLIBAR:YOCTOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 26)), Sym("mbar"))
    CONVERSIONS["MILLIBAR:YOTTAPASCAL"] = \
        Mul(Pow(10, 22), Sym("mbar"))
    CONVERSIONS["MILLIBAR:ZEPTOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 23)), Sym("mbar"))
    CONVERSIONS["MILLIBAR:ZETTAPASCAL"] = \
        Mul(Pow(10, 19), Sym("mbar"))
    CONVERSIONS["MILLIPASCAL:ATHMOSPHERE"] = \
        Mul(Int(101325000), Sym("millipa"))
    CONVERSIONS["MILLIPASCAL:ATTOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 15)), Sym("millipa"))
    CONVERSIONS["MILLIPASCAL:BAR"] = \
        Mul(Pow(10, 8), Sym("millipa"))
    CONVERSIONS["MILLIPASCAL:CENTIBAR"] = \
        Mul(Pow(10, 6), Sym("millipa"))
    CONVERSIONS["MILLIPASCAL:CENTIPASCAL"] = \
        Mul(Int(10), Sym("millipa"))
    CONVERSIONS["MILLIPASCAL:DECAPASCAL"] = \
        Mul(Pow(10, 4), Sym("millipa"))
    CONVERSIONS["MILLIPASCAL:DECIBAR"] = \
        Mul(Pow(10, 7), Sym("millipa"))
    CONVERSIONS["MILLIPASCAL:DECIPASCAL"] = \
        Mul(Int(100), Sym("millipa"))
    CONVERSIONS["MILLIPASCAL:EXAPASCAL"] = \
        Mul(Pow(10, 21), Sym("millipa"))
    CONVERSIONS["MILLIPASCAL:FEMTOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 12)), Sym("millipa"))
    CONVERSIONS["MILLIPASCAL:GIGAPASCAL"] = \
        Mul(Pow(10, 12), Sym("millipa"))
    CONVERSIONS["MILLIPASCAL:HECTOPASCAL"] = \
        Mul(Pow(10, 5), Sym("millipa"))
    CONVERSIONS["MILLIPASCAL:KILOBAR"] = \
        Mul(Pow(10, 11), Sym("millipa"))
    CONVERSIONS["MILLIPASCAL:KILOPASCAL"] = \
        Mul(Pow(10, 6), Sym("millipa"))
    CONVERSIONS["MILLIPASCAL:MEGABAR"] = \
        Mul(Pow(10, 14), Sym("millipa"))
    CONVERSIONS["MILLIPASCAL:MEGAPASCAL"] = \
        Mul(Pow(10, 9), Sym("millipa"))
    CONVERSIONS["MILLIPASCAL:MICROPASCAL"] = \
        Mul(Rat(Int(1), Int(1000)), Sym("millipa"))
    CONVERSIONS["MILLIPASCAL:MILLIBAR"] = \
        Mul(Pow(10, 5), Sym("millipa"))
    CONVERSIONS["MILLIPASCAL:MILLITORR"] = \
        Mul(Rat(Int(20265), Int(152)), Sym("millipa"))
    CONVERSIONS["MILLIPASCAL:MMHG"] = \
        Mul(Rat(Int("26664477483"), Mul(Int(2), Pow(10, 5))), Sym("millipa"))
    CONVERSIONS["MILLIPASCAL:NANOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("millipa"))
    CONVERSIONS["MILLIPASCAL:PASCAL"] = \
        Mul(Int(1000), Sym("millipa"))
    CONVERSIONS["MILLIPASCAL:PETAPASCAL"] = \
        Mul(Pow(10, 18), Sym("millipa"))
    CONVERSIONS["MILLIPASCAL:PICOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("millipa"))
    CONVERSIONS["MILLIPASCAL:PSI"] = \
        Mul(Rat(Int("172368932329209"), Mul(Int(25), Pow(10, 6))), Sym("millipa"))
    CONVERSIONS["MILLIPASCAL:TERAPASCAL"] = \
        Mul(Pow(10, 15), Sym("millipa"))
    CONVERSIONS["MILLIPASCAL:TORR"] = \
        Mul(Rat(Int(2533125), Int(19)), Sym("millipa"))
    CONVERSIONS["MILLIPASCAL:YOCTOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 21)), Sym("millipa"))
    CONVERSIONS["MILLIPASCAL:YOTTAPASCAL"] = \
        Mul(Pow(10, 27), Sym("millipa"))
    CONVERSIONS["MILLIPASCAL:ZEPTOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 18)), Sym("millipa"))
    CONVERSIONS["MILLIPASCAL:ZETTAPASCAL"] = \
        Mul(Pow(10, 24), Sym("millipa"))
    CONVERSIONS["MILLITORR:ATHMOSPHERE"] = \
        Mul(Mul(Int(76), Pow(10, 4)), Sym("mtorr"))
    CONVERSIONS["MILLITORR:ATTOPASCAL"] = \
        Mul(Rat(Int(19), Mul(Int(2533125), Pow(10, 12))), Sym("mtorr"))
    CONVERSIONS["MILLITORR:BAR"] = \
        Mul(Rat(Mul(Int(304), Pow(10, 7)), Int(4053)), Sym("mtorr"))
    CONVERSIONS["MILLITORR:CENTIBAR"] = \
        Mul(Rat(Mul(Int(304), Pow(10, 5)), Int(4053)), Sym("mtorr"))
    CONVERSIONS["MILLITORR:CENTIPASCAL"] = \
        Mul(Rat(Int(304), Int(4053)), Sym("mtorr"))
    CONVERSIONS["MILLITORR:DECAPASCAL"] = \
        Mul(Rat(Int(304000), Int(4053)), Sym("mtorr"))
    CONVERSIONS["MILLITORR:DECIBAR"] = \
        Mul(Rat(Mul(Int(304), Pow(10, 6)), Int(4053)), Sym("mtorr"))
    CONVERSIONS["MILLITORR:DECIPASCAL"] = \
        Mul(Rat(Int(3040), Int(4053)), Sym("mtorr"))
    CONVERSIONS["MILLITORR:EXAPASCAL"] = \
        Mul(Rat(Mul(Int(304), Pow(10, 20)), Int(4053)), Sym("mtorr"))
    CONVERSIONS["MILLITORR:FEMTOPASCAL"] = \
        Mul(Rat(Int(19), Mul(Int(2533125), Pow(10, 9))), Sym("mtorr"))
    CONVERSIONS["MILLITORR:GIGAPASCAL"] = \
        Mul(Rat(Mul(Int(304), Pow(10, 11)), Int(4053)), Sym("mtorr"))
    CONVERSIONS["MILLITORR:HECTOPASCAL"] = \
        Mul(Rat(Mul(Int(304), Pow(10, 4)), Int(4053)), Sym("mtorr"))
    CONVERSIONS["MILLITORR:KILOBAR"] = \
        Mul(Rat(Mul(Int(304), Pow(10, 10)), Int(4053)), Sym("mtorr"))
    CONVERSIONS["MILLITORR:KILOPASCAL"] = \
        Mul(Rat(Mul(Int(304), Pow(10, 5)), Int(4053)), Sym("mtorr"))
    CONVERSIONS["MILLITORR:MEGABAR"] = \
        Mul(Rat(Mul(Int(304), Pow(10, 13)), Int(4053)), Sym("mtorr"))
    CONVERSIONS["MILLITORR:MEGAPASCAL"] = \
        Mul(Rat(Mul(Int(304), Pow(10, 8)), Int(4053)), Sym("mtorr"))
    CONVERSIONS["MILLITORR:MICROPASCAL"] = \
        Mul(Rat(Int(19), Int(2533125)), Sym("mtorr"))
    CONVERSIONS["MILLITORR:MILLIBAR"] = \
        Mul(Rat(Mul(Int(304), Pow(10, 4)), Int(4053)), Sym("mtorr"))
    CONVERSIONS["MILLITORR:MILLIPASCAL"] = \
        Mul(Rat(Int(152), Int(20265)), Sym("mtorr"))
    CONVERSIONS["MILLITORR:MMHG"] = \
        Mul(Rat(Int("24125003437"), Int(24125000)), Sym("mtorr"))
    CONVERSIONS["MILLITORR:NANOPASCAL"] = \
        Mul(Rat(Int(19), Int("2533125000")), Sym("mtorr"))
    CONVERSIONS["MILLITORR:PASCAL"] = \
        Mul(Rat(Int(30400), Int(4053)), Sym("mtorr"))
    CONVERSIONS["MILLITORR:PETAPASCAL"] = \
        Mul(Rat(Mul(Int(304), Pow(10, 17)), Int(4053)), Sym("mtorr"))
    CONVERSIONS["MILLITORR:PICOPASCAL"] = \
        Mul(Rat(Int(19), Mul(Int(2533125), Pow(10, 6))), Sym("mtorr"))
    CONVERSIONS["MILLITORR:PSI"] = \
        Mul(Rat(Int("155952843535951"), Int("3015625000")), Sym("mtorr"))
    CONVERSIONS["MILLITORR:TERAPASCAL"] = \
        Mul(Rat(Mul(Int(304), Pow(10, 14)), Int(4053)), Sym("mtorr"))
    CONVERSIONS["MILLITORR:TORR"] = \
        Mul(Int(1000), Sym("mtorr"))
    CONVERSIONS["MILLITORR:YOCTOPASCAL"] = \
        Mul(Rat(Int(19), Mul(Int(2533125), Pow(10, 18))), Sym("mtorr"))
    CONVERSIONS["MILLITORR:YOTTAPASCAL"] = \
        Mul(Rat(Mul(Int(304), Pow(10, 26)), Int(4053)), Sym("mtorr"))
    CONVERSIONS["MILLITORR:ZEPTOPASCAL"] = \
        Mul(Rat(Int(19), Mul(Int(2533125), Pow(10, 15))), Sym("mtorr"))
    CONVERSIONS["MILLITORR:ZETTAPASCAL"] = \
        Mul(Rat(Mul(Int(304), Pow(10, 23)), Int(4053)), Sym("mtorr"))
    CONVERSIONS["MMHG:ATHMOSPHERE"] = \
        Mul(Rat(Mul(Int(965), Pow(10, 9)), Int(1269737023)), Sym("mmhg"))
    CONVERSIONS["MMHG:ATTOPASCAL"] = \
        Mul(Rat(Int(1), Mul(Int("133322387415"), Pow(10, 9))), Sym("mmhg"))
    CONVERSIONS["MMHG:BAR"] = \
        Mul(Rat(Mul(Int(2), Pow(10, 13)), Int("26664477483")), Sym("mmhg"))
    CONVERSIONS["MMHG:CENTIBAR"] = \
        Mul(Rat(Mul(Int(2), Pow(10, 11)), Int("26664477483")), Sym("mmhg"))
    CONVERSIONS["MMHG:CENTIPASCAL"] = \
        Mul(Rat(Mul(Int(2), Pow(10, 6)), Int("26664477483")), Sym("mmhg"))
    CONVERSIONS["MMHG:DECAPASCAL"] = \
        Mul(Rat(Mul(Int(2), Pow(10, 9)), Int("26664477483")), Sym("mmhg"))
    CONVERSIONS["MMHG:DECIBAR"] = \
        Mul(Rat(Mul(Int(2), Pow(10, 12)), Int("26664477483")), Sym("mmhg"))
    CONVERSIONS["MMHG:DECIPASCAL"] = \
        Mul(Rat(Mul(Int(2), Pow(10, 7)), Int("26664477483")), Sym("mmhg"))
    CONVERSIONS["MMHG:EXAPASCAL"] = \
        Mul(Rat(Mul(Int(2), Pow(10, 26)), Int("26664477483")), Sym("mmhg"))
    CONVERSIONS["MMHG:FEMTOPASCAL"] = \
        Mul(Rat(Int(1), Mul(Int("133322387415"), Pow(10, 6))), Sym("mmhg"))
    CONVERSIONS["MMHG:GIGAPASCAL"] = \
        Mul(Rat(Mul(Int(2), Pow(10, 17)), Int("26664477483")), Sym("mmhg"))
    CONVERSIONS["MMHG:HECTOPASCAL"] = \
        Mul(Rat(Mul(Int(2), Pow(10, 10)), Int("26664477483")), Sym("mmhg"))
    CONVERSIONS["MMHG:KILOBAR"] = \
        Mul(Rat(Mul(Int(2), Pow(10, 16)), Int("26664477483")), Sym("mmhg"))
    CONVERSIONS["MMHG:KILOPASCAL"] = \
        Mul(Rat(Mul(Int(2), Pow(10, 11)), Int("26664477483")), Sym("mmhg"))
    CONVERSIONS["MMHG:MEGABAR"] = \
        Mul(Rat(Mul(Int(2), Pow(10, 19)), Int("26664477483")), Sym("mmhg"))
    CONVERSIONS["MMHG:MEGAPASCAL"] = \
        Mul(Rat(Mul(Int(2), Pow(10, 14)), Int("26664477483")), Sym("mmhg"))
    CONVERSIONS["MMHG:MICROPASCAL"] = \
        Mul(Rat(Int(200), Int("26664477483")), Sym("mmhg"))
    CONVERSIONS["MMHG:MILLIBAR"] = \
        Mul(Rat(Mul(Int(2), Pow(10, 10)), Int("26664477483")), Sym("mmhg"))
    CONVERSIONS["MMHG:MILLIPASCAL"] = \
        Mul(Rat(Mul(Int(2), Pow(10, 5)), Int("26664477483")), Sym("mmhg"))
    CONVERSIONS["MMHG:MILLITORR"] = \
        Mul(Rat(Int(24125000), Int("24125003437")), Sym("mmhg"))
    CONVERSIONS["MMHG:NANOPASCAL"] = \
        Mul(Rat(Int(1), Int("133322387415")), Sym("mmhg"))
    CONVERSIONS["MMHG:PASCAL"] = \
        Mul(Rat(Mul(Int(2), Pow(10, 8)), Int("26664477483")), Sym("mmhg"))
    CONVERSIONS["MMHG:PETAPASCAL"] = \
        Mul(Rat(Mul(Int(2), Pow(10, 23)), Int("26664477483")), Sym("mmhg"))
    CONVERSIONS["MMHG:PICOPASCAL"] = \
        Mul(Rat(Int(1), Int("133322387415000")), Sym("mmhg"))
    CONVERSIONS["MMHG:PSI"] = \
        Mul(Rat(Int("8208044396629"), Int("158717127875")), Sym("mmhg"))
    CONVERSIONS["MMHG:TERAPASCAL"] = \
        Mul(Rat(Mul(Int(2), Pow(10, 20)), Int("26664477483")), Sym("mmhg"))
    CONVERSIONS["MMHG:TORR"] = \
        Mul(Rat(Mul(Int(24125), Pow(10, 6)), Int("24125003437")), Sym("mmhg"))
    CONVERSIONS["MMHG:YOCTOPASCAL"] = \
        Mul(Rat(Int(1), Mul(Int("133322387415"), Pow(10, 15))), Sym("mmhg"))
    CONVERSIONS["MMHG:YOTTAPASCAL"] = \
        Mul(Rat(Mul(Int(2), Pow(10, 32)), Int("26664477483")), Sym("mmhg"))
    CONVERSIONS["MMHG:ZEPTOPASCAL"] = \
        Mul(Rat(Int(1), Mul(Int("133322387415"), Pow(10, 12))), Sym("mmhg"))
    CONVERSIONS["MMHG:ZETTAPASCAL"] = \
        Mul(Rat(Mul(Int(2), Pow(10, 29)), Int("26664477483")), Sym("mmhg"))
    CONVERSIONS["NANOPASCAL:ATHMOSPHERE"] = \
        Mul(Mul(Int(101325), Pow(10, 9)), Sym("nanopa"))
    CONVERSIONS["NANOPASCAL:ATTOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("nanopa"))
    CONVERSIONS["NANOPASCAL:BAR"] = \
        Mul(Pow(10, 14), Sym("nanopa"))
    CONVERSIONS["NANOPASCAL:CENTIBAR"] = \
        Mul(Pow(10, 12), Sym("nanopa"))
    CONVERSIONS["NANOPASCAL:CENTIPASCAL"] = \
        Mul(Pow(10, 7), Sym("nanopa"))
    CONVERSIONS["NANOPASCAL:DECAPASCAL"] = \
        Mul(Pow(10, 10), Sym("nanopa"))
    CONVERSIONS["NANOPASCAL:DECIBAR"] = \
        Mul(Pow(10, 13), Sym("nanopa"))
    CONVERSIONS["NANOPASCAL:DECIPASCAL"] = \
        Mul(Pow(10, 8), Sym("nanopa"))
    CONVERSIONS["NANOPASCAL:EXAPASCAL"] = \
        Mul(Pow(10, 27), Sym("nanopa"))
    CONVERSIONS["NANOPASCAL:FEMTOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("nanopa"))
    CONVERSIONS["NANOPASCAL:GIGAPASCAL"] = \
        Mul(Pow(10, 18), Sym("nanopa"))
    CONVERSIONS["NANOPASCAL:HECTOPASCAL"] = \
        Mul(Pow(10, 11), Sym("nanopa"))
    CONVERSIONS["NANOPASCAL:KILOBAR"] = \
        Mul(Pow(10, 17), Sym("nanopa"))
    CONVERSIONS["NANOPASCAL:KILOPASCAL"] = \
        Mul(Pow(10, 12), Sym("nanopa"))
    CONVERSIONS["NANOPASCAL:MEGABAR"] = \
        Mul(Pow(10, 20), Sym("nanopa"))
    CONVERSIONS["NANOPASCAL:MEGAPASCAL"] = \
        Mul(Pow(10, 15), Sym("nanopa"))
    CONVERSIONS["NANOPASCAL:MICROPASCAL"] = \
        Mul(Int(1000), Sym("nanopa"))
    CONVERSIONS["NANOPASCAL:MILLIBAR"] = \
        Mul(Pow(10, 11), Sym("nanopa"))
    CONVERSIONS["NANOPASCAL:MILLIPASCAL"] = \
        Mul(Pow(10, 6), Sym("nanopa"))
    CONVERSIONS["NANOPASCAL:MILLITORR"] = \
        Mul(Rat(Int("2533125000"), Int(19)), Sym("nanopa"))
    CONVERSIONS["NANOPASCAL:MMHG"] = \
        Mul(Int("133322387415"), Sym("nanopa"))
    CONVERSIONS["NANOPASCAL:PASCAL"] = \
        Mul(Pow(10, 9), Sym("nanopa"))
    CONVERSIONS["NANOPASCAL:PETAPASCAL"] = \
        Mul(Pow(10, 24), Sym("nanopa"))
    CONVERSIONS["NANOPASCAL:PICOPASCAL"] = \
        Mul(Rat(Int(1), Int(1000)), Sym("nanopa"))
    CONVERSIONS["NANOPASCAL:PSI"] = \
        Mul(Rat(Int("172368932329209"), Int(25)), Sym("nanopa"))
    CONVERSIONS["NANOPASCAL:TERAPASCAL"] = \
        Mul(Pow(10, 21), Sym("nanopa"))
    CONVERSIONS["NANOPASCAL:TORR"] = \
        Mul(Rat(Mul(Int(2533125), Pow(10, 6)), Int(19)), Sym("nanopa"))
    CONVERSIONS["NANOPASCAL:YOCTOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 15)), Sym("nanopa"))
    CONVERSIONS["NANOPASCAL:YOTTAPASCAL"] = \
        Mul(Pow(10, 33), Sym("nanopa"))
    CONVERSIONS["NANOPASCAL:ZEPTOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 12)), Sym("nanopa"))
    CONVERSIONS["NANOPASCAL:ZETTAPASCAL"] = \
        Mul(Pow(10, 30), Sym("nanopa"))
    CONVERSIONS["PASCAL:ATHMOSPHERE"] = \
        Mul(Int(101325), Sym("pa"))
    CONVERSIONS["PASCAL:ATTOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 18)), Sym("pa"))
    CONVERSIONS["PASCAL:BAR"] = \
        Mul(Pow(10, 5), Sym("pa"))
    CONVERSIONS["PASCAL:CENTIBAR"] = \
        Mul(Int(1000), Sym("pa"))
    CONVERSIONS["PASCAL:CENTIPASCAL"] = \
        Mul(Rat(Int(1), Int(100)), Sym("pa"))
    CONVERSIONS["PASCAL:DECAPASCAL"] = \
        Mul(Int(10), Sym("pa"))
    CONVERSIONS["PASCAL:DECIBAR"] = \
        Mul(Pow(10, 4), Sym("pa"))
    CONVERSIONS["PASCAL:DECIPASCAL"] = \
        Mul(Rat(Int(1), Int(10)), Sym("pa"))
    CONVERSIONS["PASCAL:EXAPASCAL"] = \
        Mul(Pow(10, 18), Sym("pa"))
    CONVERSIONS["PASCAL:FEMTOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 15)), Sym("pa"))
    CONVERSIONS["PASCAL:GIGAPASCAL"] = \
        Mul(Pow(10, 9), Sym("pa"))
    CONVERSIONS["PASCAL:HECTOPASCAL"] = \
        Mul(Int(100), Sym("pa"))
    CONVERSIONS["PASCAL:KILOBAR"] = \
        Mul(Pow(10, 8), Sym("pa"))
    CONVERSIONS["PASCAL:KILOPASCAL"] = \
        Mul(Int(1000), Sym("pa"))
    CONVERSIONS["PASCAL:MEGABAR"] = \
        Mul(Pow(10, 11), Sym("pa"))
    CONVERSIONS["PASCAL:MEGAPASCAL"] = \
        Mul(Pow(10, 6), Sym("pa"))
    CONVERSIONS["PASCAL:MICROPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("pa"))
    CONVERSIONS["PASCAL:MILLIBAR"] = \
        Mul(Int(100), Sym("pa"))
    CONVERSIONS["PASCAL:MILLIPASCAL"] = \
        Mul(Rat(Int(1), Int(1000)), Sym("pa"))
    CONVERSIONS["PASCAL:MILLITORR"] = \
        Mul(Rat(Int(4053), Int(30400)), Sym("pa"))
    CONVERSIONS["PASCAL:MMHG"] = \
        Mul(Rat(Int("26664477483"), Mul(Int(2), Pow(10, 8))), Sym("pa"))
    CONVERSIONS["PASCAL:NANOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("pa"))
    CONVERSIONS["PASCAL:PETAPASCAL"] = \
        Mul(Pow(10, 15), Sym("pa"))
    CONVERSIONS["PASCAL:PICOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 12)), Sym("pa"))
    CONVERSIONS["PASCAL:PSI"] = \
        Mul(Rat(Int("172368932329209"), Mul(Int(25), Pow(10, 9))), Sym("pa"))
    CONVERSIONS["PASCAL:TERAPASCAL"] = \
        Mul(Pow(10, 12), Sym("pa"))
    CONVERSIONS["PASCAL:TORR"] = \
        Mul(Rat(Int(20265), Int(152)), Sym("pa"))
    CONVERSIONS["PASCAL:YOCTOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 24)), Sym("pa"))
    CONVERSIONS["PASCAL:YOTTAPASCAL"] = \
        Mul(Pow(10, 24), Sym("pa"))
    CONVERSIONS["PASCAL:ZEPTOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 21)), Sym("pa"))
    CONVERSIONS["PASCAL:ZETTAPASCAL"] = \
        Mul(Pow(10, 21), Sym("pa"))
    CONVERSIONS["PETAPASCAL:ATHMOSPHERE"] = \
        Mul(Rat(Int(4053), Mul(Int(4), Pow(10, 13))), Sym("petapa"))
    CONVERSIONS["PETAPASCAL:ATTOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 33)), Sym("petapa"))
    CONVERSIONS["PETAPASCAL:BAR"] = \
        Mul(Rat(Int(1), Pow(10, 10)), Sym("petapa"))
    CONVERSIONS["PETAPASCAL:CENTIBAR"] = \
        Mul(Rat(Int(1), Pow(10, 12)), Sym("petapa"))
    CONVERSIONS["PETAPASCAL:CENTIPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 17)), Sym("petapa"))
    CONVERSIONS["PETAPASCAL:DECAPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 14)), Sym("petapa"))
    CONVERSIONS["PETAPASCAL:DECIBAR"] = \
        Mul(Rat(Int(1), Pow(10, 11)), Sym("petapa"))
    CONVERSIONS["PETAPASCAL:DECIPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 16)), Sym("petapa"))
    CONVERSIONS["PETAPASCAL:EXAPASCAL"] = \
        Mul(Int(1000), Sym("petapa"))
    CONVERSIONS["PETAPASCAL:FEMTOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 30)), Sym("petapa"))
    CONVERSIONS["PETAPASCAL:GIGAPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("petapa"))
    CONVERSIONS["PETAPASCAL:HECTOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 13)), Sym("petapa"))
    CONVERSIONS["PETAPASCAL:KILOBAR"] = \
        Mul(Rat(Int(1), Pow(10, 7)), Sym("petapa"))
    CONVERSIONS["PETAPASCAL:KILOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 12)), Sym("petapa"))
    CONVERSIONS["PETAPASCAL:MEGABAR"] = \
        Mul(Rat(Int(1), Pow(10, 4)), Sym("petapa"))
    CONVERSIONS["PETAPASCAL:MEGAPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("petapa"))
    CONVERSIONS["PETAPASCAL:MICROPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 21)), Sym("petapa"))
    CONVERSIONS["PETAPASCAL:MILLIBAR"] = \
        Mul(Rat(Int(1), Pow(10, 13)), Sym("petapa"))
    CONVERSIONS["PETAPASCAL:MILLIPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 18)), Sym("petapa"))
    CONVERSIONS["PETAPASCAL:MILLITORR"] = \
        Mul(Rat(Int(4053), Mul(Int(304), Pow(10, 17))), Sym("petapa"))
    CONVERSIONS["PETAPASCAL:MMHG"] = \
        Mul(Rat(Int("26664477483"), Mul(Int(2), Pow(10, 23))), Sym("petapa"))
    CONVERSIONS["PETAPASCAL:NANOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 24)), Sym("petapa"))
    CONVERSIONS["PETAPASCAL:PASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 15)), Sym("petapa"))
    CONVERSIONS["PETAPASCAL:PICOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 27)), Sym("petapa"))
    CONVERSIONS["PETAPASCAL:PSI"] = \
        Mul(Rat(Int("172368932329209"), Mul(Int(25), Pow(10, 24))), Sym("petapa"))
    CONVERSIONS["PETAPASCAL:TERAPASCAL"] = \
        Mul(Rat(Int(1), Int(1000)), Sym("petapa"))
    CONVERSIONS["PETAPASCAL:TORR"] = \
        Mul(Rat(Int(4053), Mul(Int(304), Pow(10, 14))), Sym("petapa"))
    CONVERSIONS["PETAPASCAL:YOCTOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 39)), Sym("petapa"))
    CONVERSIONS["PETAPASCAL:YOTTAPASCAL"] = \
        Mul(Pow(10, 9), Sym("petapa"))
    CONVERSIONS["PETAPASCAL:ZEPTOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 36)), Sym("petapa"))
    CONVERSIONS["PETAPASCAL:ZETTAPASCAL"] = \
        Mul(Pow(10, 6), Sym("petapa"))
    CONVERSIONS["PICOPASCAL:ATHMOSPHERE"] = \
        Mul(Mul(Int(101325), Pow(10, 12)), Sym("picopa"))
    CONVERSIONS["PICOPASCAL:ATTOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("picopa"))
    CONVERSIONS["PICOPASCAL:BAR"] = \
        Mul(Pow(10, 17), Sym("picopa"))
    CONVERSIONS["PICOPASCAL:CENTIBAR"] = \
        Mul(Pow(10, 15), Sym("picopa"))
    CONVERSIONS["PICOPASCAL:CENTIPASCAL"] = \
        Mul(Pow(10, 10), Sym("picopa"))
    CONVERSIONS["PICOPASCAL:DECAPASCAL"] = \
        Mul(Pow(10, 13), Sym("picopa"))
    CONVERSIONS["PICOPASCAL:DECIBAR"] = \
        Mul(Pow(10, 16), Sym("picopa"))
    CONVERSIONS["PICOPASCAL:DECIPASCAL"] = \
        Mul(Pow(10, 11), Sym("picopa"))
    CONVERSIONS["PICOPASCAL:EXAPASCAL"] = \
        Mul(Pow(10, 30), Sym("picopa"))
    CONVERSIONS["PICOPASCAL:FEMTOPASCAL"] = \
        Mul(Rat(Int(1), Int(1000)), Sym("picopa"))
    CONVERSIONS["PICOPASCAL:GIGAPASCAL"] = \
        Mul(Pow(10, 21), Sym("picopa"))
    CONVERSIONS["PICOPASCAL:HECTOPASCAL"] = \
        Mul(Pow(10, 14), Sym("picopa"))
    CONVERSIONS["PICOPASCAL:KILOBAR"] = \
        Mul(Pow(10, 20), Sym("picopa"))
    CONVERSIONS["PICOPASCAL:KILOPASCAL"] = \
        Mul(Pow(10, 15), Sym("picopa"))
    CONVERSIONS["PICOPASCAL:MEGABAR"] = \
        Mul(Pow(10, 23), Sym("picopa"))
    CONVERSIONS["PICOPASCAL:MEGAPASCAL"] = \
        Mul(Pow(10, 18), Sym("picopa"))
    CONVERSIONS["PICOPASCAL:MICROPASCAL"] = \
        Mul(Pow(10, 6), Sym("picopa"))
    CONVERSIONS["PICOPASCAL:MILLIBAR"] = \
        Mul(Pow(10, 14), Sym("picopa"))
    CONVERSIONS["PICOPASCAL:MILLIPASCAL"] = \
        Mul(Pow(10, 9), Sym("picopa"))
    CONVERSIONS["PICOPASCAL:MILLITORR"] = \
        Mul(Rat(Mul(Int(2533125), Pow(10, 6)), Int(19)), Sym("picopa"))
    CONVERSIONS["PICOPASCAL:MMHG"] = \
        Mul(Int("133322387415000"), Sym("picopa"))
    CONVERSIONS["PICOPASCAL:NANOPASCAL"] = \
        Mul(Int(1000), Sym("picopa"))
    CONVERSIONS["PICOPASCAL:PASCAL"] = \
        Mul(Pow(10, 12), Sym("picopa"))
    CONVERSIONS["PICOPASCAL:PETAPASCAL"] = \
        Mul(Pow(10, 27), Sym("picopa"))
    CONVERSIONS["PICOPASCAL:PSI"] = \
        Mul(Int("6894757293168360"), Sym("picopa"))
    CONVERSIONS["PICOPASCAL:TERAPASCAL"] = \
        Mul(Pow(10, 24), Sym("picopa"))
    CONVERSIONS["PICOPASCAL:TORR"] = \
        Mul(Rat(Mul(Int(2533125), Pow(10, 9)), Int(19)), Sym("picopa"))
    CONVERSIONS["PICOPASCAL:YOCTOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 12)), Sym("picopa"))
    CONVERSIONS["PICOPASCAL:YOTTAPASCAL"] = \
        Mul(Pow(10, 36), Sym("picopa"))
    CONVERSIONS["PICOPASCAL:ZEPTOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("picopa"))
    CONVERSIONS["PICOPASCAL:ZETTAPASCAL"] = \
        Mul(Pow(10, 33), Sym("picopa"))
    CONVERSIONS["PSI:ATHMOSPHERE"] = \
        Mul(Rat(Mul(Int(120625), Pow(10, 9)), Int("8208044396629")), Sym("psi"))
    CONVERSIONS["PSI:ATTOPASCAL"] = \
        Mul(Rat(Int(1), Mul(Int("689475729316836"), Pow(10, 7))), Sym("psi"))
    CONVERSIONS["PSI:BAR"] = \
        Mul(Rat(Mul(Int(25), Pow(10, 14)), Int("172368932329209")), Sym("psi"))
    CONVERSIONS["PSI:CENTIBAR"] = \
        Mul(Rat(Mul(Int(25), Pow(10, 12)), Int("172368932329209")), Sym("psi"))
    CONVERSIONS["PSI:CENTIPASCAL"] = \
        Mul(Rat(Mul(Int(25), Pow(10, 7)), Int("172368932329209")), Sym("psi"))
    CONVERSIONS["PSI:DECAPASCAL"] = \
        Mul(Rat(Mul(Int(25), Pow(10, 10)), Int("172368932329209")), Sym("psi"))
    CONVERSIONS["PSI:DECIBAR"] = \
        Mul(Rat(Mul(Int(25), Pow(10, 13)), Int("172368932329209")), Sym("psi"))
    CONVERSIONS["PSI:DECIPASCAL"] = \
        Mul(Rat(Mul(Int(25), Pow(10, 8)), Int("172368932329209")), Sym("psi"))
    CONVERSIONS["PSI:EXAPASCAL"] = \
        Mul(Rat(Mul(Int(25), Pow(10, 27)), Int("172368932329209")), Sym("psi"))
    CONVERSIONS["PSI:FEMTOPASCAL"] = \
        Mul(Rat(Int(1), Mul(Int("689475729316836"), Pow(10, 4))), Sym("psi"))
    CONVERSIONS["PSI:GIGAPASCAL"] = \
        Mul(Rat(Mul(Int(25), Pow(10, 18)), Int("172368932329209")), Sym("psi"))
    CONVERSIONS["PSI:HECTOPASCAL"] = \
        Mul(Rat(Mul(Int(25), Pow(10, 11)), Int("172368932329209")), Sym("psi"))
    CONVERSIONS["PSI:KILOBAR"] = \
        Mul(Rat(Mul(Int(25), Pow(10, 17)), Int("172368932329209")), Sym("psi"))
    CONVERSIONS["PSI:KILOPASCAL"] = \
        Mul(Rat(Mul(Int(25), Pow(10, 12)), Int("172368932329209")), Sym("psi"))
    CONVERSIONS["PSI:MEGABAR"] = \
        Mul(Rat(Mul(Int(25), Pow(10, 20)), Int("172368932329209")), Sym("psi"))
    CONVERSIONS["PSI:MEGAPASCAL"] = \
        Mul(Rat(Mul(Int(25), Pow(10, 15)), Int("172368932329209")), Sym("psi"))
    CONVERSIONS["PSI:MICROPASCAL"] = \
        Mul(Rat(Int(25000), Int("172368932329209")), Sym("psi"))
    CONVERSIONS["PSI:MILLIBAR"] = \
        Mul(Rat(Mul(Int(25), Pow(10, 11)), Int("172368932329209")), Sym("psi"))
    CONVERSIONS["PSI:MILLIPASCAL"] = \
        Mul(Rat(Mul(Int(25), Pow(10, 6)), Int("172368932329209")), Sym("psi"))
    CONVERSIONS["PSI:MILLITORR"] = \
        Mul(Rat(Int("3015625000"), Int("155952843535951")), Sym("psi"))
    CONVERSIONS["PSI:MMHG"] = \
        Mul(Rat(Int("158717127875"), Int("8208044396629")), Sym("psi"))
    CONVERSIONS["PSI:NANOPASCAL"] = \
        Mul(Rat(Int(25), Int("172368932329209")), Sym("psi"))
    CONVERSIONS["PSI:PASCAL"] = \
        Mul(Rat(Mul(Int(25), Pow(10, 9)), Int("172368932329209")), Sym("psi"))
    CONVERSIONS["PSI:PETAPASCAL"] = \
        Mul(Rat(Mul(Int(25), Pow(10, 24)), Int("172368932329209")), Sym("psi"))
    CONVERSIONS["PSI:PICOPASCAL"] = \
        Mul(Rat(Int(1), Int("6894757293168360")), Sym("psi"))
    CONVERSIONS["PSI:TERAPASCAL"] = \
        Mul(Rat(Mul(Int(25), Pow(10, 21)), Int("172368932329209")), Sym("psi"))
    CONVERSIONS["PSI:TORR"] = \
        Mul(Rat(Mul(Int(3015625), Pow(10, 6)), Int("155952843535951")), Sym("psi"))
    CONVERSIONS["PSI:YOCTOPASCAL"] = \
        Mul(Rat(Int(1), Mul(Int("689475729316836"), Pow(10, 13))), Sym("psi"))
    CONVERSIONS["PSI:YOTTAPASCAL"] = \
        Mul(Rat(Mul(Int(25), Pow(10, 33)), Int("172368932329209")), Sym("psi"))
    CONVERSIONS["PSI:ZEPTOPASCAL"] = \
        Mul(Rat(Int(1), Mul(Int("689475729316836"), Pow(10, 10))), Sym("psi"))
    CONVERSIONS["PSI:ZETTAPASCAL"] = \
        Mul(Rat(Mul(Int(25), Pow(10, 30)), Int("172368932329209")), Sym("psi"))
    CONVERSIONS["TERAPASCAL:ATHMOSPHERE"] = \
        Mul(Rat(Int(4053), Mul(Int(4), Pow(10, 10))), Sym("terapa"))
    CONVERSIONS["TERAPASCAL:ATTOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 30)), Sym("terapa"))
    CONVERSIONS["TERAPASCAL:BAR"] = \
        Mul(Rat(Int(1), Pow(10, 7)), Sym("terapa"))
    CONVERSIONS["TERAPASCAL:CENTIBAR"] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("terapa"))
    CONVERSIONS["TERAPASCAL:CENTIPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 14)), Sym("terapa"))
    CONVERSIONS["TERAPASCAL:DECAPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 11)), Sym("terapa"))
    CONVERSIONS["TERAPASCAL:DECIBAR"] = \
        Mul(Rat(Int(1), Pow(10, 8)), Sym("terapa"))
    CONVERSIONS["TERAPASCAL:DECIPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 13)), Sym("terapa"))
    CONVERSIONS["TERAPASCAL:EXAPASCAL"] = \
        Mul(Pow(10, 6), Sym("terapa"))
    CONVERSIONS["TERAPASCAL:FEMTOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 27)), Sym("terapa"))
    CONVERSIONS["TERAPASCAL:GIGAPASCAL"] = \
        Mul(Rat(Int(1), Int(1000)), Sym("terapa"))
    CONVERSIONS["TERAPASCAL:HECTOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 10)), Sym("terapa"))
    CONVERSIONS["TERAPASCAL:KILOBAR"] = \
        Mul(Rat(Int(1), Pow(10, 4)), Sym("terapa"))
    CONVERSIONS["TERAPASCAL:KILOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("terapa"))
    CONVERSIONS["TERAPASCAL:MEGABAR"] = \
        Mul(Rat(Int(1), Int(10)), Sym("terapa"))
    CONVERSIONS["TERAPASCAL:MEGAPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("terapa"))
    CONVERSIONS["TERAPASCAL:MICROPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 18)), Sym("terapa"))
    CONVERSIONS["TERAPASCAL:MILLIBAR"] = \
        Mul(Rat(Int(1), Pow(10, 10)), Sym("terapa"))
    CONVERSIONS["TERAPASCAL:MILLIPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 15)), Sym("terapa"))
    CONVERSIONS["TERAPASCAL:MILLITORR"] = \
        Mul(Rat(Int(4053), Mul(Int(304), Pow(10, 14))), Sym("terapa"))
    CONVERSIONS["TERAPASCAL:MMHG"] = \
        Mul(Rat(Int("26664477483"), Mul(Int(2), Pow(10, 20))), Sym("terapa"))
    CONVERSIONS["TERAPASCAL:NANOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 21)), Sym("terapa"))
    CONVERSIONS["TERAPASCAL:PASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 12)), Sym("terapa"))
    CONVERSIONS["TERAPASCAL:PETAPASCAL"] = \
        Mul(Int(1000), Sym("terapa"))
    CONVERSIONS["TERAPASCAL:PICOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 24)), Sym("terapa"))
    CONVERSIONS["TERAPASCAL:PSI"] = \
        Mul(Rat(Int("172368932329209"), Mul(Int(25), Pow(10, 21))), Sym("terapa"))
    CONVERSIONS["TERAPASCAL:TORR"] = \
        Mul(Rat(Int(4053), Mul(Int(304), Pow(10, 11))), Sym("terapa"))
    CONVERSIONS["TERAPASCAL:YOCTOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 36)), Sym("terapa"))
    CONVERSIONS["TERAPASCAL:YOTTAPASCAL"] = \
        Mul(Pow(10, 12), Sym("terapa"))
    CONVERSIONS["TERAPASCAL:ZEPTOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 33)), Sym("terapa"))
    CONVERSIONS["TERAPASCAL:ZETTAPASCAL"] = \
        Mul(Pow(10, 9), Sym("terapa"))
    CONVERSIONS["TORR:ATHMOSPHERE"] = \
        Mul(Int(760), Sym("torr"))
    CONVERSIONS["TORR:ATTOPASCAL"] = \
        Mul(Rat(Int(19), Mul(Int(2533125), Pow(10, 15))), Sym("torr"))
    CONVERSIONS["TORR:BAR"] = \
        Mul(Rat(Mul(Int(304), Pow(10, 4)), Int(4053)), Sym("torr"))
    CONVERSIONS["TORR:CENTIBAR"] = \
        Mul(Rat(Int(30400), Int(4053)), Sym("torr"))
    CONVERSIONS["TORR:CENTIPASCAL"] = \
        Mul(Rat(Int(38), Int(506625)), Sym("torr"))
    CONVERSIONS["TORR:DECAPASCAL"] = \
        Mul(Rat(Int(304), Int(4053)), Sym("torr"))
    CONVERSIONS["TORR:DECIBAR"] = \
        Mul(Rat(Int(304000), Int(4053)), Sym("torr"))
    CONVERSIONS["TORR:DECIPASCAL"] = \
        Mul(Rat(Int(76), Int(101325)), Sym("torr"))
    CONVERSIONS["TORR:EXAPASCAL"] = \
        Mul(Rat(Mul(Int(304), Pow(10, 17)), Int(4053)), Sym("torr"))
    CONVERSIONS["TORR:FEMTOPASCAL"] = \
        Mul(Rat(Int(19), Mul(Int(2533125), Pow(10, 12))), Sym("torr"))
    CONVERSIONS["TORR:GIGAPASCAL"] = \
        Mul(Rat(Mul(Int(304), Pow(10, 8)), Int(4053)), Sym("torr"))
    CONVERSIONS["TORR:HECTOPASCAL"] = \
        Mul(Rat(Int(3040), Int(4053)), Sym("torr"))
    CONVERSIONS["TORR:KILOBAR"] = \
        Mul(Rat(Mul(Int(304), Pow(10, 7)), Int(4053)), Sym("torr"))
    CONVERSIONS["TORR:KILOPASCAL"] = \
        Mul(Rat(Int(30400), Int(4053)), Sym("torr"))
    CONVERSIONS["TORR:MEGABAR"] = \
        Mul(Rat(Mul(Int(304), Pow(10, 10)), Int(4053)), Sym("torr"))
    CONVERSIONS["TORR:MEGAPASCAL"] = \
        Mul(Rat(Mul(Int(304), Pow(10, 5)), Int(4053)), Sym("torr"))
    CONVERSIONS["TORR:MICROPASCAL"] = \
        Mul(Rat(Int(19), Int("2533125000")), Sym("torr"))
    CONVERSIONS["TORR:MILLIBAR"] = \
        Mul(Rat(Int(3040), Int(4053)), Sym("torr"))
    CONVERSIONS["TORR:MILLIPASCAL"] = \
        Mul(Rat(Int(19), Int(2533125)), Sym("torr"))
    CONVERSIONS["TORR:MILLITORR"] = \
        Mul(Rat(Int(1), Int(1000)), Sym("torr"))
    CONVERSIONS["TORR:MMHG"] = \
        Mul(Rat(Int("24125003437"), Mul(Int(24125), Pow(10, 6))), Sym("torr"))
    CONVERSIONS["TORR:NANOPASCAL"] = \
        Mul(Rat(Int(19), Mul(Int(2533125), Pow(10, 6))), Sym("torr"))
    CONVERSIONS["TORR:PASCAL"] = \
        Mul(Rat(Int(152), Int(20265)), Sym("torr"))
    CONVERSIONS["TORR:PETAPASCAL"] = \
        Mul(Rat(Mul(Int(304), Pow(10, 14)), Int(4053)), Sym("torr"))
    CONVERSIONS["TORR:PICOPASCAL"] = \
        Mul(Rat(Int(19), Mul(Int(2533125), Pow(10, 9))), Sym("torr"))
    CONVERSIONS["TORR:PSI"] = \
        Mul(Rat(Int("155952843535951"), Mul(Int(3015625), Pow(10, 6))), Sym("torr"))
    CONVERSIONS["TORR:TERAPASCAL"] = \
        Mul(Rat(Mul(Int(304), Pow(10, 11)), Int(4053)), Sym("torr"))
    CONVERSIONS["TORR:YOCTOPASCAL"] = \
        Mul(Rat(Int(19), Mul(Int(2533125), Pow(10, 21))), Sym("torr"))
    CONVERSIONS["TORR:YOTTAPASCAL"] = \
        Mul(Rat(Mul(Int(304), Pow(10, 23)), Int(4053)), Sym("torr"))
    CONVERSIONS["TORR:ZEPTOPASCAL"] = \
        Mul(Rat(Int(19), Mul(Int(2533125), Pow(10, 18))), Sym("torr"))
    CONVERSIONS["TORR:ZETTAPASCAL"] = \
        Mul(Rat(Mul(Int(304), Pow(10, 20)), Int(4053)), Sym("torr"))
    CONVERSIONS["YOCTOPASCAL:ATHMOSPHERE"] = \
        Mul(Mul(Int(101325), Pow(10, 24)), Sym("yoctopa"))
    CONVERSIONS["YOCTOPASCAL:ATTOPASCAL"] = \
        Mul(Pow(10, 6), Sym("yoctopa"))
    CONVERSIONS["YOCTOPASCAL:BAR"] = \
        Mul(Pow(10, 29), Sym("yoctopa"))
    CONVERSIONS["YOCTOPASCAL:CENTIBAR"] = \
        Mul(Pow(10, 27), Sym("yoctopa"))
    CONVERSIONS["YOCTOPASCAL:CENTIPASCAL"] = \
        Mul(Pow(10, 22), Sym("yoctopa"))
    CONVERSIONS["YOCTOPASCAL:DECAPASCAL"] = \
        Mul(Pow(10, 25), Sym("yoctopa"))
    CONVERSIONS["YOCTOPASCAL:DECIBAR"] = \
        Mul(Pow(10, 28), Sym("yoctopa"))
    CONVERSIONS["YOCTOPASCAL:DECIPASCAL"] = \
        Mul(Pow(10, 23), Sym("yoctopa"))
    CONVERSIONS["YOCTOPASCAL:EXAPASCAL"] = \
        Mul(Pow(10, 42), Sym("yoctopa"))
    CONVERSIONS["YOCTOPASCAL:FEMTOPASCAL"] = \
        Mul(Pow(10, 9), Sym("yoctopa"))
    CONVERSIONS["YOCTOPASCAL:GIGAPASCAL"] = \
        Mul(Pow(10, 33), Sym("yoctopa"))
    CONVERSIONS["YOCTOPASCAL:HECTOPASCAL"] = \
        Mul(Pow(10, 26), Sym("yoctopa"))
    CONVERSIONS["YOCTOPASCAL:KILOBAR"] = \
        Mul(Pow(10, 32), Sym("yoctopa"))
    CONVERSIONS["YOCTOPASCAL:KILOPASCAL"] = \
        Mul(Pow(10, 27), Sym("yoctopa"))
    CONVERSIONS["YOCTOPASCAL:MEGABAR"] = \
        Mul(Pow(10, 35), Sym("yoctopa"))
    CONVERSIONS["YOCTOPASCAL:MEGAPASCAL"] = \
        Mul(Pow(10, 30), Sym("yoctopa"))
    CONVERSIONS["YOCTOPASCAL:MICROPASCAL"] = \
        Mul(Pow(10, 18), Sym("yoctopa"))
    CONVERSIONS["YOCTOPASCAL:MILLIBAR"] = \
        Mul(Pow(10, 26), Sym("yoctopa"))
    CONVERSIONS["YOCTOPASCAL:MILLIPASCAL"] = \
        Mul(Pow(10, 21), Sym("yoctopa"))
    CONVERSIONS["YOCTOPASCAL:MILLITORR"] = \
        Mul(Rat(Mul(Int(2533125), Pow(10, 18)), Int(19)), Sym("yoctopa"))
    CONVERSIONS["YOCTOPASCAL:MMHG"] = \
        Mul(Mul(Int("133322387415"), Pow(10, 15)), Sym("yoctopa"))
    CONVERSIONS["YOCTOPASCAL:NANOPASCAL"] = \
        Mul(Pow(10, 15), Sym("yoctopa"))
    CONVERSIONS["YOCTOPASCAL:PASCAL"] = \
        Mul(Pow(10, 24), Sym("yoctopa"))
    CONVERSIONS["YOCTOPASCAL:PETAPASCAL"] = \
        Mul(Pow(10, 39), Sym("yoctopa"))
    CONVERSIONS["YOCTOPASCAL:PICOPASCAL"] = \
        Mul(Pow(10, 12), Sym("yoctopa"))
    CONVERSIONS["YOCTOPASCAL:PSI"] = \
        Mul(Mul(Int("689475729316836"), Pow(10, 13)), Sym("yoctopa"))
    CONVERSIONS["YOCTOPASCAL:TERAPASCAL"] = \
        Mul(Pow(10, 36), Sym("yoctopa"))
    CONVERSIONS["YOCTOPASCAL:TORR"] = \
        Mul(Rat(Mul(Int(2533125), Pow(10, 21)), Int(19)), Sym("yoctopa"))
    CONVERSIONS["YOCTOPASCAL:YOTTAPASCAL"] = \
        Mul(Pow(10, 48), Sym("yoctopa"))
    CONVERSIONS["YOCTOPASCAL:ZEPTOPASCAL"] = \
        Mul(Int(1000), Sym("yoctopa"))
    CONVERSIONS["YOCTOPASCAL:ZETTAPASCAL"] = \
        Mul(Pow(10, 45), Sym("yoctopa"))
    CONVERSIONS["YOTTAPASCAL:ATHMOSPHERE"] = \
        Mul(Rat(Int(4053), Mul(Int(4), Pow(10, 22))), Sym("yottapa"))
    CONVERSIONS["YOTTAPASCAL:ATTOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 42)), Sym("yottapa"))
    CONVERSIONS["YOTTAPASCAL:BAR"] = \
        Mul(Rat(Int(1), Pow(10, 19)), Sym("yottapa"))
    CONVERSIONS["YOTTAPASCAL:CENTIBAR"] = \
        Mul(Rat(Int(1), Pow(10, 21)), Sym("yottapa"))
    CONVERSIONS["YOTTAPASCAL:CENTIPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 26)), Sym("yottapa"))
    CONVERSIONS["YOTTAPASCAL:DECAPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 23)), Sym("yottapa"))
    CONVERSIONS["YOTTAPASCAL:DECIBAR"] = \
        Mul(Rat(Int(1), Pow(10, 20)), Sym("yottapa"))
    CONVERSIONS["YOTTAPASCAL:DECIPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 25)), Sym("yottapa"))
    CONVERSIONS["YOTTAPASCAL:EXAPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("yottapa"))
    CONVERSIONS["YOTTAPASCAL:FEMTOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 39)), Sym("yottapa"))
    CONVERSIONS["YOTTAPASCAL:GIGAPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 15)), Sym("yottapa"))
    CONVERSIONS["YOTTAPASCAL:HECTOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 22)), Sym("yottapa"))
    CONVERSIONS["YOTTAPASCAL:KILOBAR"] = \
        Mul(Rat(Int(1), Pow(10, 16)), Sym("yottapa"))
    CONVERSIONS["YOTTAPASCAL:KILOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 21)), Sym("yottapa"))
    CONVERSIONS["YOTTAPASCAL:MEGABAR"] = \
        Mul(Rat(Int(1), Pow(10, 13)), Sym("yottapa"))
    CONVERSIONS["YOTTAPASCAL:MEGAPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 18)), Sym("yottapa"))
    CONVERSIONS["YOTTAPASCAL:MICROPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 30)), Sym("yottapa"))
    CONVERSIONS["YOTTAPASCAL:MILLIBAR"] = \
        Mul(Rat(Int(1), Pow(10, 22)), Sym("yottapa"))
    CONVERSIONS["YOTTAPASCAL:MILLIPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 27)), Sym("yottapa"))
    CONVERSIONS["YOTTAPASCAL:MILLITORR"] = \
        Mul(Rat(Int(4053), Mul(Int(304), Pow(10, 26))), Sym("yottapa"))
    CONVERSIONS["YOTTAPASCAL:MMHG"] = \
        Mul(Rat(Int("26664477483"), Mul(Int(2), Pow(10, 32))), Sym("yottapa"))
    CONVERSIONS["YOTTAPASCAL:NANOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 33)), Sym("yottapa"))
    CONVERSIONS["YOTTAPASCAL:PASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 24)), Sym("yottapa"))
    CONVERSIONS["YOTTAPASCAL:PETAPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("yottapa"))
    CONVERSIONS["YOTTAPASCAL:PICOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 36)), Sym("yottapa"))
    CONVERSIONS["YOTTAPASCAL:PSI"] = \
        Mul(Rat(Int("172368932329209"), Mul(Int(25), Pow(10, 33))), Sym("yottapa"))
    CONVERSIONS["YOTTAPASCAL:TERAPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 12)), Sym("yottapa"))
    CONVERSIONS["YOTTAPASCAL:TORR"] = \
        Mul(Rat(Int(4053), Mul(Int(304), Pow(10, 23))), Sym("yottapa"))
    CONVERSIONS["YOTTAPASCAL:YOCTOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 48)), Sym("yottapa"))
    CONVERSIONS["YOTTAPASCAL:ZEPTOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 45)), Sym("yottapa"))
    CONVERSIONS["YOTTAPASCAL:ZETTAPASCAL"] = \
        Mul(Rat(Int(1), Int(1000)), Sym("yottapa"))
    CONVERSIONS["ZEPTOPASCAL:ATHMOSPHERE"] = \
        Mul(Mul(Int(101325), Pow(10, 21)), Sym("zeptopa"))
    CONVERSIONS["ZEPTOPASCAL:ATTOPASCAL"] = \
        Mul(Int(1000), Sym("zeptopa"))
    CONVERSIONS["ZEPTOPASCAL:BAR"] = \
        Mul(Pow(10, 26), Sym("zeptopa"))
    CONVERSIONS["ZEPTOPASCAL:CENTIBAR"] = \
        Mul(Pow(10, 24), Sym("zeptopa"))
    CONVERSIONS["ZEPTOPASCAL:CENTIPASCAL"] = \
        Mul(Pow(10, 19), Sym("zeptopa"))
    CONVERSIONS["ZEPTOPASCAL:DECAPASCAL"] = \
        Mul(Pow(10, 22), Sym("zeptopa"))
    CONVERSIONS["ZEPTOPASCAL:DECIBAR"] = \
        Mul(Pow(10, 25), Sym("zeptopa"))
    CONVERSIONS["ZEPTOPASCAL:DECIPASCAL"] = \
        Mul(Pow(10, 20), Sym("zeptopa"))
    CONVERSIONS["ZEPTOPASCAL:EXAPASCAL"] = \
        Mul(Pow(10, 39), Sym("zeptopa"))
    CONVERSIONS["ZEPTOPASCAL:FEMTOPASCAL"] = \
        Mul(Pow(10, 6), Sym("zeptopa"))
    CONVERSIONS["ZEPTOPASCAL:GIGAPASCAL"] = \
        Mul(Pow(10, 30), Sym("zeptopa"))
    CONVERSIONS["ZEPTOPASCAL:HECTOPASCAL"] = \
        Mul(Pow(10, 23), Sym("zeptopa"))
    CONVERSIONS["ZEPTOPASCAL:KILOBAR"] = \
        Mul(Pow(10, 29), Sym("zeptopa"))
    CONVERSIONS["ZEPTOPASCAL:KILOPASCAL"] = \
        Mul(Pow(10, 24), Sym("zeptopa"))
    CONVERSIONS["ZEPTOPASCAL:MEGABAR"] = \
        Mul(Pow(10, 32), Sym("zeptopa"))
    CONVERSIONS["ZEPTOPASCAL:MEGAPASCAL"] = \
        Mul(Pow(10, 27), Sym("zeptopa"))
    CONVERSIONS["ZEPTOPASCAL:MICROPASCAL"] = \
        Mul(Pow(10, 15), Sym("zeptopa"))
    CONVERSIONS["ZEPTOPASCAL:MILLIBAR"] = \
        Mul(Pow(10, 23), Sym("zeptopa"))
    CONVERSIONS["ZEPTOPASCAL:MILLIPASCAL"] = \
        Mul(Pow(10, 18), Sym("zeptopa"))
    CONVERSIONS["ZEPTOPASCAL:MILLITORR"] = \
        Mul(Rat(Mul(Int(2533125), Pow(10, 15)), Int(19)), Sym("zeptopa"))
    CONVERSIONS["ZEPTOPASCAL:MMHG"] = \
        Mul(Mul(Int("133322387415"), Pow(10, 12)), Sym("zeptopa"))
    CONVERSIONS["ZEPTOPASCAL:NANOPASCAL"] = \
        Mul(Pow(10, 12), Sym("zeptopa"))
    CONVERSIONS["ZEPTOPASCAL:PASCAL"] = \
        Mul(Pow(10, 21), Sym("zeptopa"))
    CONVERSIONS["ZEPTOPASCAL:PETAPASCAL"] = \
        Mul(Pow(10, 36), Sym("zeptopa"))
    CONVERSIONS["ZEPTOPASCAL:PICOPASCAL"] = \
        Mul(Pow(10, 9), Sym("zeptopa"))
    CONVERSIONS["ZEPTOPASCAL:PSI"] = \
        Mul(Mul(Int("689475729316836"), Pow(10, 10)), Sym("zeptopa"))
    CONVERSIONS["ZEPTOPASCAL:TERAPASCAL"] = \
        Mul(Pow(10, 33), Sym("zeptopa"))
    CONVERSIONS["ZEPTOPASCAL:TORR"] = \
        Mul(Rat(Mul(Int(2533125), Pow(10, 18)), Int(19)), Sym("zeptopa"))
    CONVERSIONS["ZEPTOPASCAL:YOCTOPASCAL"] = \
        Mul(Rat(Int(1), Int(1000)), Sym("zeptopa"))
    CONVERSIONS["ZEPTOPASCAL:YOTTAPASCAL"] = \
        Mul(Pow(10, 45), Sym("zeptopa"))
    CONVERSIONS["ZEPTOPASCAL:ZETTAPASCAL"] = \
        Mul(Pow(10, 42), Sym("zeptopa"))
    CONVERSIONS["ZETTAPASCAL:ATHMOSPHERE"] = \
        Mul(Rat(Int(4053), Mul(Int(4), Pow(10, 19))), Sym("zettapa"))
    CONVERSIONS["ZETTAPASCAL:ATTOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 39)), Sym("zettapa"))
    CONVERSIONS["ZETTAPASCAL:BAR"] = \
        Mul(Rat(Int(1), Pow(10, 16)), Sym("zettapa"))
    CONVERSIONS["ZETTAPASCAL:CENTIBAR"] = \
        Mul(Rat(Int(1), Pow(10, 18)), Sym("zettapa"))
    CONVERSIONS["ZETTAPASCAL:CENTIPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 23)), Sym("zettapa"))
    CONVERSIONS["ZETTAPASCAL:DECAPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 20)), Sym("zettapa"))
    CONVERSIONS["ZETTAPASCAL:DECIBAR"] = \
        Mul(Rat(Int(1), Pow(10, 17)), Sym("zettapa"))
    CONVERSIONS["ZETTAPASCAL:DECIPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 22)), Sym("zettapa"))
    CONVERSIONS["ZETTAPASCAL:EXAPASCAL"] = \
        Mul(Rat(Int(1), Int(1000)), Sym("zettapa"))
    CONVERSIONS["ZETTAPASCAL:FEMTOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 36)), Sym("zettapa"))
    CONVERSIONS["ZETTAPASCAL:GIGAPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 12)), Sym("zettapa"))
    CONVERSIONS["ZETTAPASCAL:HECTOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 19)), Sym("zettapa"))
    CONVERSIONS["ZETTAPASCAL:KILOBAR"] = \
        Mul(Rat(Int(1), Pow(10, 13)), Sym("zettapa"))
    CONVERSIONS["ZETTAPASCAL:KILOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 18)), Sym("zettapa"))
    CONVERSIONS["ZETTAPASCAL:MEGABAR"] = \
        Mul(Rat(Int(1), Pow(10, 10)), Sym("zettapa"))
    CONVERSIONS["ZETTAPASCAL:MEGAPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 15)), Sym("zettapa"))
    CONVERSIONS["ZETTAPASCAL:MICROPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 27)), Sym("zettapa"))
    CONVERSIONS["ZETTAPASCAL:MILLIBAR"] = \
        Mul(Rat(Int(1), Pow(10, 19)), Sym("zettapa"))
    CONVERSIONS["ZETTAPASCAL:MILLIPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 24)), Sym("zettapa"))
    CONVERSIONS["ZETTAPASCAL:MILLITORR"] = \
        Mul(Rat(Int(4053), Mul(Int(304), Pow(10, 23))), Sym("zettapa"))
    CONVERSIONS["ZETTAPASCAL:MMHG"] = \
        Mul(Rat(Int("26664477483"), Mul(Int(2), Pow(10, 29))), Sym("zettapa"))
    CONVERSIONS["ZETTAPASCAL:NANOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 30)), Sym("zettapa"))
    CONVERSIONS["ZETTAPASCAL:PASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 21)), Sym("zettapa"))
    CONVERSIONS["ZETTAPASCAL:PETAPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("zettapa"))
    CONVERSIONS["ZETTAPASCAL:PICOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 33)), Sym("zettapa"))
    CONVERSIONS["ZETTAPASCAL:PSI"] = \
        Mul(Rat(Int("172368932329209"), Mul(Int(25), Pow(10, 30))), Sym("zettapa"))
    CONVERSIONS["ZETTAPASCAL:TERAPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("zettapa"))
    CONVERSIONS["ZETTAPASCAL:TORR"] = \
        Mul(Rat(Int(4053), Mul(Int(304), Pow(10, 20))), Sym("zettapa"))
    CONVERSIONS["ZETTAPASCAL:YOCTOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 45)), Sym("zettapa"))
    CONVERSIONS["ZETTAPASCAL:YOTTAPASCAL"] = \
        Mul(Int(1000), Sym("zettapa"))
    CONVERSIONS["ZETTAPASCAL:ZEPTOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 42)), Sym("zettapa"))

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
    SYMBOLS["KILOBAR"] = "kBar"
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
        if isinstance(value, _omero_model.PressureI):
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
                self.setUnit(getattr(UnitsPressure, str(target)))
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
        return PressureI.SYMBOLS.get(str(unit))

    def setUnit(self, unit, current=None):
        self._unit = unit

    def setValue(self, value, current=None):
        self._value = value

    def __str__(self):
        return self._base_string(self.getValue(), self.getUnit())

_omero_model.PressureI = PressureI
