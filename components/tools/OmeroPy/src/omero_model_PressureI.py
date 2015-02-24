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

from omero.model.conversions import Add  # nopep8
from omero.model.conversions import Int  # nopep8
from omero.model.conversions import Mul  # nopep8
from omero.model.conversions import Pow  # nopep8
from omero.model.conversions import Rat  # nopep8
from omero.model.conversions import Sym  # nopep8


class PressureI(_omero_model.Pressure, UnitBase):

    CONVERSIONS = dict()
    CONVERSIONS["ATHMOSPHERE:ATTOPASCAL"] = \
        Mul(Rat(Int(1), Mul(Int(101325), Pow(10, 18))), Sym("atm"))  # nopep8
    CONVERSIONS["ATHMOSPHERE:BAR"] = \
        Mul(Rat(Int(4000), Int(4053)), Sym("atm"))  # nopep8
    CONVERSIONS["ATHMOSPHERE:CENTIBAR"] = \
        Mul(Rat(Int(40), Int(4053)), Sym("atm"))  # nopep8
    CONVERSIONS["ATHMOSPHERE:CENTIPASCAL"] = \
        Mul(Rat(Int(1), Int(10132500)), Sym("atm"))  # nopep8
    CONVERSIONS["ATHMOSPHERE:DECAPASCAL"] = \
        Mul(Rat(Int(2), Int(20265)), Sym("atm"))  # nopep8
    CONVERSIONS["ATHMOSPHERE:DECIBAR"] = \
        Mul(Rat(Int(400), Int(4053)), Sym("atm"))  # nopep8
    CONVERSIONS["ATHMOSPHERE:DECIPASCAL"] = \
        Mul(Rat(Int(1), Int(1013250)), Sym("atm"))  # nopep8
    CONVERSIONS["ATHMOSPHERE:EXAPASCAL"] = \
        Mul(Rat(Mul(Int(4), Pow(10, 16)), Int(4053)), Sym("atm"))  # nopep8
    CONVERSIONS["ATHMOSPHERE:FEMTOPASCAL"] = \
        Mul(Rat(Int(1), Mul(Int(101325), Pow(10, 15))), Sym("atm"))  # nopep8
    CONVERSIONS["ATHMOSPHERE:GIGAPASCAL"] = \
        Mul(Rat(Mul(Int(4), Pow(10, 7)), Int(4053)), Sym("atm"))  # nopep8
    CONVERSIONS["ATHMOSPHERE:HECTOPASCAL"] = \
        Mul(Rat(Int(4), Int(4053)), Sym("atm"))  # nopep8
    CONVERSIONS["ATHMOSPHERE:KILOBAR"] = \
        Mul(Rat(Mul(Int(4), Pow(10, 6)), Int(4053)), Sym("atm"))  # nopep8
    CONVERSIONS["ATHMOSPHERE:KILOPASCAL"] = \
        Mul(Rat(Int(40), Int(4053)), Sym("atm"))  # nopep8
    CONVERSIONS["ATHMOSPHERE:MEGABAR"] = \
        Mul(Rat(Mul(Int(4), Pow(10, 9)), Int(4053)), Sym("atm"))  # nopep8
    CONVERSIONS["ATHMOSPHERE:MEGAPASCAL"] = \
        Mul(Rat(Mul(Int(4), Pow(10, 4)), Int(4053)), Sym("atm"))  # nopep8
    CONVERSIONS["ATHMOSPHERE:MICROPASCAL"] = \
        Mul(Rat(Int(1), Mul(Int(101325), Pow(10, 6))), Sym("atm"))  # nopep8
    CONVERSIONS["ATHMOSPHERE:MILLIBAR"] = \
        Mul(Rat(Int(4), Int(4053)), Sym("atm"))  # nopep8
    CONVERSIONS["ATHMOSPHERE:MILLIPASCAL"] = \
        Mul(Rat(Int(1), Int(101325000)), Sym("atm"))  # nopep8
    CONVERSIONS["ATHMOSPHERE:MILLITORR"] = \
        Mul(Rat(Int(1), Mul(Int(76), Pow(10, 4))), Sym("atm"))  # nopep8
    CONVERSIONS["ATHMOSPHERE:MMHG"] = \
        Mul(Rat(Int(1269737023), Mul(Int(965), Pow(10, 9))), Sym("atm"))  # nopep8
    CONVERSIONS["ATHMOSPHERE:NANOPASCAL"] = \
        Mul(Rat(Int(1), Mul(Int(101325), Pow(10, 9))), Sym("atm"))  # nopep8
    CONVERSIONS["ATHMOSPHERE:PASCAL"] = \
        Mul(Rat(Int(1), Int(101325)), Sym("atm"))  # nopep8
    CONVERSIONS["ATHMOSPHERE:PETAPASCAL"] = \
        Mul(Rat(Mul(Int(4), Pow(10, 13)), Int(4053)), Sym("atm"))  # nopep8
    CONVERSIONS["ATHMOSPHERE:PICOPASCAL"] = \
        Mul(Rat(Int(1), Mul(Int(101325), Pow(10, 12))), Sym("atm"))  # nopep8
    CONVERSIONS["ATHMOSPHERE:PSI"] = \
        Mul(Rat(Int("8208044396629"), Mul(Int(120625), Pow(10, 9))), Sym("atm"))  # nopep8
    CONVERSIONS["ATHMOSPHERE:TERAPASCAL"] = \
        Mul(Rat(Mul(Int(4), Pow(10, 10)), Int(4053)), Sym("atm"))  # nopep8
    CONVERSIONS["ATHMOSPHERE:TORR"] = \
        Mul(Rat(Int(1), Int(760)), Sym("atm"))  # nopep8
    CONVERSIONS["ATHMOSPHERE:YOCTOPASCAL"] = \
        Mul(Rat(Int(1), Mul(Int(101325), Pow(10, 24))), Sym("atm"))  # nopep8
    CONVERSIONS["ATHMOSPHERE:YOTTAPASCAL"] = \
        Mul(Rat(Mul(Int(4), Pow(10, 22)), Int(4053)), Sym("atm"))  # nopep8
    CONVERSIONS["ATHMOSPHERE:ZEPTOPASCAL"] = \
        Mul(Rat(Int(1), Mul(Int(101325), Pow(10, 21))), Sym("atm"))  # nopep8
    CONVERSIONS["ATHMOSPHERE:ZETTAPASCAL"] = \
        Mul(Rat(Mul(Int(4), Pow(10, 19)), Int(4053)), Sym("atm"))  # nopep8
    CONVERSIONS["ATTOPASCAL:ATHMOSPHERE"] = \
        Mul(Mul(Int(101325), Pow(10, 18)), Sym("attopa"))  # nopep8
    CONVERSIONS["ATTOPASCAL:BAR"] = \
        Mul(Pow(10, 23), Sym("attopa"))  # nopep8
    CONVERSIONS["ATTOPASCAL:CENTIBAR"] = \
        Mul(Pow(10, 21), Sym("attopa"))  # nopep8
    CONVERSIONS["ATTOPASCAL:CENTIPASCAL"] = \
        Mul(Pow(10, 16), Sym("attopa"))  # nopep8
    CONVERSIONS["ATTOPASCAL:DECAPASCAL"] = \
        Mul(Pow(10, 19), Sym("attopa"))  # nopep8
    CONVERSIONS["ATTOPASCAL:DECIBAR"] = \
        Mul(Pow(10, 22), Sym("attopa"))  # nopep8
    CONVERSIONS["ATTOPASCAL:DECIPASCAL"] = \
        Mul(Pow(10, 17), Sym("attopa"))  # nopep8
    CONVERSIONS["ATTOPASCAL:EXAPASCAL"] = \
        Mul(Pow(10, 36), Sym("attopa"))  # nopep8
    CONVERSIONS["ATTOPASCAL:FEMTOPASCAL"] = \
        Mul(Int(1000), Sym("attopa"))  # nopep8
    CONVERSIONS["ATTOPASCAL:GIGAPASCAL"] = \
        Mul(Pow(10, 27), Sym("attopa"))  # nopep8
    CONVERSIONS["ATTOPASCAL:HECTOPASCAL"] = \
        Mul(Pow(10, 20), Sym("attopa"))  # nopep8
    CONVERSIONS["ATTOPASCAL:KILOBAR"] = \
        Mul(Pow(10, 26), Sym("attopa"))  # nopep8
    CONVERSIONS["ATTOPASCAL:KILOPASCAL"] = \
        Mul(Pow(10, 21), Sym("attopa"))  # nopep8
    CONVERSIONS["ATTOPASCAL:MEGABAR"] = \
        Mul(Pow(10, 29), Sym("attopa"))  # nopep8
    CONVERSIONS["ATTOPASCAL:MEGAPASCAL"] = \
        Mul(Pow(10, 24), Sym("attopa"))  # nopep8
    CONVERSIONS["ATTOPASCAL:MICROPASCAL"] = \
        Mul(Pow(10, 12), Sym("attopa"))  # nopep8
    CONVERSIONS["ATTOPASCAL:MILLIBAR"] = \
        Mul(Pow(10, 20), Sym("attopa"))  # nopep8
    CONVERSIONS["ATTOPASCAL:MILLIPASCAL"] = \
        Mul(Pow(10, 15), Sym("attopa"))  # nopep8
    CONVERSIONS["ATTOPASCAL:MILLITORR"] = \
        Mul(Rat(Mul(Int(2533125), Pow(10, 12)), Int(19)), Sym("attopa"))  # nopep8
    CONVERSIONS["ATTOPASCAL:MMHG"] = \
        Mul(Mul(Int("133322387415"), Pow(10, 9)), Sym("attopa"))  # nopep8
    CONVERSIONS["ATTOPASCAL:NANOPASCAL"] = \
        Mul(Pow(10, 9), Sym("attopa"))  # nopep8
    CONVERSIONS["ATTOPASCAL:PASCAL"] = \
        Mul(Pow(10, 18), Sym("attopa"))  # nopep8
    CONVERSIONS["ATTOPASCAL:PETAPASCAL"] = \
        Mul(Pow(10, 33), Sym("attopa"))  # nopep8
    CONVERSIONS["ATTOPASCAL:PICOPASCAL"] = \
        Mul(Pow(10, 6), Sym("attopa"))  # nopep8
    CONVERSIONS["ATTOPASCAL:PSI"] = \
        Mul(Mul(Int("689475729316836"), Pow(10, 7)), Sym("attopa"))  # nopep8
    CONVERSIONS["ATTOPASCAL:TERAPASCAL"] = \
        Mul(Pow(10, 30), Sym("attopa"))  # nopep8
    CONVERSIONS["ATTOPASCAL:TORR"] = \
        Mul(Rat(Mul(Int(2533125), Pow(10, 15)), Int(19)), Sym("attopa"))  # nopep8
    CONVERSIONS["ATTOPASCAL:YOCTOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("attopa"))  # nopep8
    CONVERSIONS["ATTOPASCAL:YOTTAPASCAL"] = \
        Mul(Pow(10, 42), Sym("attopa"))  # nopep8
    CONVERSIONS["ATTOPASCAL:ZEPTOPASCAL"] = \
        Mul(Rat(Int(1), Int(1000)), Sym("attopa"))  # nopep8
    CONVERSIONS["ATTOPASCAL:ZETTAPASCAL"] = \
        Mul(Pow(10, 39), Sym("attopa"))  # nopep8
    CONVERSIONS["BAR:ATHMOSPHERE"] = \
        Mul(Rat(Int(4053), Int(4000)), Sym("bar"))  # nopep8
    CONVERSIONS["BAR:ATTOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 23)), Sym("bar"))  # nopep8
    CONVERSIONS["BAR:CENTIBAR"] = \
        Mul(Rat(Int(1), Int(100)), Sym("bar"))  # nopep8
    CONVERSIONS["BAR:CENTIPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 7)), Sym("bar"))  # nopep8
    CONVERSIONS["BAR:DECAPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 4)), Sym("bar"))  # nopep8
    CONVERSIONS["BAR:DECIBAR"] = \
        Mul(Rat(Int(1), Int(10)), Sym("bar"))  # nopep8
    CONVERSIONS["BAR:DECIPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("bar"))  # nopep8
    CONVERSIONS["BAR:EXAPASCAL"] = \
        Mul(Pow(10, 13), Sym("bar"))  # nopep8
    CONVERSIONS["BAR:FEMTOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 20)), Sym("bar"))  # nopep8
    CONVERSIONS["BAR:GIGAPASCAL"] = \
        Mul(Pow(10, 4), Sym("bar"))  # nopep8
    CONVERSIONS["BAR:HECTOPASCAL"] = \
        Mul(Rat(Int(1), Int(1000)), Sym("bar"))  # nopep8
    CONVERSIONS["BAR:KILOBAR"] = \
        Mul(Int(1000), Sym("bar"))  # nopep8
    CONVERSIONS["BAR:KILOPASCAL"] = \
        Mul(Rat(Int(1), Int(100)), Sym("bar"))  # nopep8
    CONVERSIONS["BAR:MEGABAR"] = \
        Mul(Pow(10, 6), Sym("bar"))  # nopep8
    CONVERSIONS["BAR:MEGAPASCAL"] = \
        Mul(Int(10), Sym("bar"))  # nopep8
    CONVERSIONS["BAR:MICROPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 11)), Sym("bar"))  # nopep8
    CONVERSIONS["BAR:MILLIBAR"] = \
        Mul(Rat(Int(1), Int(1000)), Sym("bar"))  # nopep8
    CONVERSIONS["BAR:MILLIPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 8)), Sym("bar"))  # nopep8
    CONVERSIONS["BAR:MILLITORR"] = \
        Mul(Rat(Int(4053), Mul(Int(304), Pow(10, 7))), Sym("bar"))  # nopep8
    CONVERSIONS["BAR:MMHG"] = \
        Mul(Rat(Int("26664477483"), Mul(Int(2), Pow(10, 13))), Sym("bar"))  # nopep8
    CONVERSIONS["BAR:NANOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 14)), Sym("bar"))  # nopep8
    CONVERSIONS["BAR:PASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 5)), Sym("bar"))  # nopep8
    CONVERSIONS["BAR:PETAPASCAL"] = \
        Mul(Pow(10, 10), Sym("bar"))  # nopep8
    CONVERSIONS["BAR:PICOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 17)), Sym("bar"))  # nopep8
    CONVERSIONS["BAR:PSI"] = \
        Mul(Rat(Int("172368932329209"), Mul(Int(25), Pow(10, 14))), Sym("bar"))  # nopep8
    CONVERSIONS["BAR:TERAPASCAL"] = \
        Mul(Pow(10, 7), Sym("bar"))  # nopep8
    CONVERSIONS["BAR:TORR"] = \
        Mul(Rat(Int(4053), Mul(Int(304), Pow(10, 4))), Sym("bar"))  # nopep8
    CONVERSIONS["BAR:YOCTOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 29)), Sym("bar"))  # nopep8
    CONVERSIONS["BAR:YOTTAPASCAL"] = \
        Mul(Pow(10, 19), Sym("bar"))  # nopep8
    CONVERSIONS["BAR:ZEPTOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 26)), Sym("bar"))  # nopep8
    CONVERSIONS["BAR:ZETTAPASCAL"] = \
        Mul(Pow(10, 16), Sym("bar"))  # nopep8
    CONVERSIONS["CENTIBAR:ATHMOSPHERE"] = \
        Mul(Rat(Int(4053), Int(40)), Sym("cbar"))  # nopep8
    CONVERSIONS["CENTIBAR:ATTOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 21)), Sym("cbar"))  # nopep8
    CONVERSIONS["CENTIBAR:BAR"] = \
        Mul(Int(100), Sym("cbar"))  # nopep8
    CONVERSIONS["CENTIBAR:CENTIPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 5)), Sym("cbar"))  # nopep8
    CONVERSIONS["CENTIBAR:DECAPASCAL"] = \
        Mul(Rat(Int(1), Int(100)), Sym("cbar"))  # nopep8
    CONVERSIONS["CENTIBAR:DECIBAR"] = \
        Mul(Int(10), Sym("cbar"))  # nopep8
    CONVERSIONS["CENTIBAR:DECIPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 4)), Sym("cbar"))  # nopep8
    CONVERSIONS["CENTIBAR:EXAPASCAL"] = \
        Mul(Pow(10, 15), Sym("cbar"))  # nopep8
    CONVERSIONS["CENTIBAR:FEMTOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 18)), Sym("cbar"))  # nopep8
    CONVERSIONS["CENTIBAR:GIGAPASCAL"] = \
        Mul(Pow(10, 6), Sym("cbar"))  # nopep8
    CONVERSIONS["CENTIBAR:HECTOPASCAL"] = \
        Mul(Rat(Int(1), Int(10)), Sym("cbar"))  # nopep8
    CONVERSIONS["CENTIBAR:KILOBAR"] = \
        Mul(Pow(10, 5), Sym("cbar"))  # nopep8
    CONVERSIONS["CENTIBAR:KILOPASCAL"] = \
        Sym("cbar")  # nopep8
    CONVERSIONS["CENTIBAR:MEGABAR"] = \
        Mul(Pow(10, 8), Sym("cbar"))  # nopep8
    CONVERSIONS["CENTIBAR:MEGAPASCAL"] = \
        Mul(Int(1000), Sym("cbar"))  # nopep8
    CONVERSIONS["CENTIBAR:MICROPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("cbar"))  # nopep8
    CONVERSIONS["CENTIBAR:MILLIBAR"] = \
        Mul(Rat(Int(1), Int(10)), Sym("cbar"))  # nopep8
    CONVERSIONS["CENTIBAR:MILLIPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("cbar"))  # nopep8
    CONVERSIONS["CENTIBAR:MILLITORR"] = \
        Mul(Rat(Int(4053), Mul(Int(304), Pow(10, 5))), Sym("cbar"))  # nopep8
    CONVERSIONS["CENTIBAR:MMHG"] = \
        Mul(Rat(Int("26664477483"), Mul(Int(2), Pow(10, 11))), Sym("cbar"))  # nopep8
    CONVERSIONS["CENTIBAR:NANOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 12)), Sym("cbar"))  # nopep8
    CONVERSIONS["CENTIBAR:PASCAL"] = \
        Mul(Rat(Int(1), Int(1000)), Sym("cbar"))  # nopep8
    CONVERSIONS["CENTIBAR:PETAPASCAL"] = \
        Mul(Pow(10, 12), Sym("cbar"))  # nopep8
    CONVERSIONS["CENTIBAR:PICOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 15)), Sym("cbar"))  # nopep8
    CONVERSIONS["CENTIBAR:PSI"] = \
        Mul(Rat(Int("172368932329209"), Mul(Int(25), Pow(10, 12))), Sym("cbar"))  # nopep8
    CONVERSIONS["CENTIBAR:TERAPASCAL"] = \
        Mul(Pow(10, 9), Sym("cbar"))  # nopep8
    CONVERSIONS["CENTIBAR:TORR"] = \
        Mul(Rat(Int(4053), Int(30400)), Sym("cbar"))  # nopep8
    CONVERSIONS["CENTIBAR:YOCTOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 27)), Sym("cbar"))  # nopep8
    CONVERSIONS["CENTIBAR:YOTTAPASCAL"] = \
        Mul(Pow(10, 21), Sym("cbar"))  # nopep8
    CONVERSIONS["CENTIBAR:ZEPTOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 24)), Sym("cbar"))  # nopep8
    CONVERSIONS["CENTIBAR:ZETTAPASCAL"] = \
        Mul(Pow(10, 18), Sym("cbar"))  # nopep8
    CONVERSIONS["CENTIPASCAL:ATHMOSPHERE"] = \
        Mul(Int(10132500), Sym("centipa"))  # nopep8
    CONVERSIONS["CENTIPASCAL:ATTOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 16)), Sym("centipa"))  # nopep8
    CONVERSIONS["CENTIPASCAL:BAR"] = \
        Mul(Pow(10, 7), Sym("centipa"))  # nopep8
    CONVERSIONS["CENTIPASCAL:CENTIBAR"] = \
        Mul(Pow(10, 5), Sym("centipa"))  # nopep8
    CONVERSIONS["CENTIPASCAL:DECAPASCAL"] = \
        Mul(Int(1000), Sym("centipa"))  # nopep8
    CONVERSIONS["CENTIPASCAL:DECIBAR"] = \
        Mul(Pow(10, 6), Sym("centipa"))  # nopep8
    CONVERSIONS["CENTIPASCAL:DECIPASCAL"] = \
        Mul(Int(10), Sym("centipa"))  # nopep8
    CONVERSIONS["CENTIPASCAL:EXAPASCAL"] = \
        Mul(Pow(10, 20), Sym("centipa"))  # nopep8
    CONVERSIONS["CENTIPASCAL:FEMTOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 13)), Sym("centipa"))  # nopep8
    CONVERSIONS["CENTIPASCAL:GIGAPASCAL"] = \
        Mul(Pow(10, 11), Sym("centipa"))  # nopep8
    CONVERSIONS["CENTIPASCAL:HECTOPASCAL"] = \
        Mul(Pow(10, 4), Sym("centipa"))  # nopep8
    CONVERSIONS["CENTIPASCAL:KILOBAR"] = \
        Mul(Pow(10, 10), Sym("centipa"))  # nopep8
    CONVERSIONS["CENTIPASCAL:KILOPASCAL"] = \
        Mul(Pow(10, 5), Sym("centipa"))  # nopep8
    CONVERSIONS["CENTIPASCAL:MEGABAR"] = \
        Mul(Pow(10, 13), Sym("centipa"))  # nopep8
    CONVERSIONS["CENTIPASCAL:MEGAPASCAL"] = \
        Mul(Pow(10, 8), Sym("centipa"))  # nopep8
    CONVERSIONS["CENTIPASCAL:MICROPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 4)), Sym("centipa"))  # nopep8
    CONVERSIONS["CENTIPASCAL:MILLIBAR"] = \
        Mul(Pow(10, 4), Sym("centipa"))  # nopep8
    CONVERSIONS["CENTIPASCAL:MILLIPASCAL"] = \
        Mul(Rat(Int(1), Int(10)), Sym("centipa"))  # nopep8
    CONVERSIONS["CENTIPASCAL:MILLITORR"] = \
        Mul(Rat(Int(4053), Int(304)), Sym("centipa"))  # nopep8
    CONVERSIONS["CENTIPASCAL:MMHG"] = \
        Mul(Rat(Int("26664477483"), Mul(Int(2), Pow(10, 6))), Sym("centipa"))  # nopep8
    CONVERSIONS["CENTIPASCAL:NANOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 7)), Sym("centipa"))  # nopep8
    CONVERSIONS["CENTIPASCAL:PASCAL"] = \
        Mul(Int(100), Sym("centipa"))  # nopep8
    CONVERSIONS["CENTIPASCAL:PETAPASCAL"] = \
        Mul(Pow(10, 17), Sym("centipa"))  # nopep8
    CONVERSIONS["CENTIPASCAL:PICOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 10)), Sym("centipa"))  # nopep8
    CONVERSIONS["CENTIPASCAL:PSI"] = \
        Mul(Rat(Int("172368932329209"), Mul(Int(25), Pow(10, 7))), Sym("centipa"))  # nopep8
    CONVERSIONS["CENTIPASCAL:TERAPASCAL"] = \
        Mul(Pow(10, 14), Sym("centipa"))  # nopep8
    CONVERSIONS["CENTIPASCAL:TORR"] = \
        Mul(Rat(Int(506625), Int(38)), Sym("centipa"))  # nopep8
    CONVERSIONS["CENTIPASCAL:YOCTOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 22)), Sym("centipa"))  # nopep8
    CONVERSIONS["CENTIPASCAL:YOTTAPASCAL"] = \
        Mul(Pow(10, 26), Sym("centipa"))  # nopep8
    CONVERSIONS["CENTIPASCAL:ZEPTOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 19)), Sym("centipa"))  # nopep8
    CONVERSIONS["CENTIPASCAL:ZETTAPASCAL"] = \
        Mul(Pow(10, 23), Sym("centipa"))  # nopep8
    CONVERSIONS["DECAPASCAL:ATHMOSPHERE"] = \
        Mul(Rat(Int(20265), Int(2)), Sym("decapa"))  # nopep8
    CONVERSIONS["DECAPASCAL:ATTOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 19)), Sym("decapa"))  # nopep8
    CONVERSIONS["DECAPASCAL:BAR"] = \
        Mul(Pow(10, 4), Sym("decapa"))  # nopep8
    CONVERSIONS["DECAPASCAL:CENTIBAR"] = \
        Mul(Int(100), Sym("decapa"))  # nopep8
    CONVERSIONS["DECAPASCAL:CENTIPASCAL"] = \
        Mul(Rat(Int(1), Int(1000)), Sym("decapa"))  # nopep8
    CONVERSIONS["DECAPASCAL:DECIBAR"] = \
        Mul(Int(1000), Sym("decapa"))  # nopep8
    CONVERSIONS["DECAPASCAL:DECIPASCAL"] = \
        Mul(Rat(Int(1), Int(100)), Sym("decapa"))  # nopep8
    CONVERSIONS["DECAPASCAL:EXAPASCAL"] = \
        Mul(Pow(10, 17), Sym("decapa"))  # nopep8
    CONVERSIONS["DECAPASCAL:FEMTOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 16)), Sym("decapa"))  # nopep8
    CONVERSIONS["DECAPASCAL:GIGAPASCAL"] = \
        Mul(Pow(10, 8), Sym("decapa"))  # nopep8
    CONVERSIONS["DECAPASCAL:HECTOPASCAL"] = \
        Mul(Int(10), Sym("decapa"))  # nopep8
    CONVERSIONS["DECAPASCAL:KILOBAR"] = \
        Mul(Pow(10, 7), Sym("decapa"))  # nopep8
    CONVERSIONS["DECAPASCAL:KILOPASCAL"] = \
        Mul(Int(100), Sym("decapa"))  # nopep8
    CONVERSIONS["DECAPASCAL:MEGABAR"] = \
        Mul(Pow(10, 10), Sym("decapa"))  # nopep8
    CONVERSIONS["DECAPASCAL:MEGAPASCAL"] = \
        Mul(Pow(10, 5), Sym("decapa"))  # nopep8
    CONVERSIONS["DECAPASCAL:MICROPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 7)), Sym("decapa"))  # nopep8
    CONVERSIONS["DECAPASCAL:MILLIBAR"] = \
        Mul(Int(10), Sym("decapa"))  # nopep8
    CONVERSIONS["DECAPASCAL:MILLIPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 4)), Sym("decapa"))  # nopep8
    CONVERSIONS["DECAPASCAL:MILLITORR"] = \
        Mul(Rat(Int(4053), Int(304000)), Sym("decapa"))  # nopep8
    CONVERSIONS["DECAPASCAL:MMHG"] = \
        Mul(Rat(Int("26664477483"), Mul(Int(2), Pow(10, 9))), Sym("decapa"))  # nopep8
    CONVERSIONS["DECAPASCAL:NANOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 10)), Sym("decapa"))  # nopep8
    CONVERSIONS["DECAPASCAL:PASCAL"] = \
        Mul(Rat(Int(1), Int(10)), Sym("decapa"))  # nopep8
    CONVERSIONS["DECAPASCAL:PETAPASCAL"] = \
        Mul(Pow(10, 14), Sym("decapa"))  # nopep8
    CONVERSIONS["DECAPASCAL:PICOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 13)), Sym("decapa"))  # nopep8
    CONVERSIONS["DECAPASCAL:PSI"] = \
        Mul(Rat(Int("172368932329209"), Mul(Int(25), Pow(10, 10))), Sym("decapa"))  # nopep8
    CONVERSIONS["DECAPASCAL:TERAPASCAL"] = \
        Mul(Pow(10, 11), Sym("decapa"))  # nopep8
    CONVERSIONS["DECAPASCAL:TORR"] = \
        Mul(Rat(Int(4053), Int(304)), Sym("decapa"))  # nopep8
    CONVERSIONS["DECAPASCAL:YOCTOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 25)), Sym("decapa"))  # nopep8
    CONVERSIONS["DECAPASCAL:YOTTAPASCAL"] = \
        Mul(Pow(10, 23), Sym("decapa"))  # nopep8
    CONVERSIONS["DECAPASCAL:ZEPTOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 22)), Sym("decapa"))  # nopep8
    CONVERSIONS["DECAPASCAL:ZETTAPASCAL"] = \
        Mul(Pow(10, 20), Sym("decapa"))  # nopep8
    CONVERSIONS["DECIBAR:ATHMOSPHERE"] = \
        Mul(Rat(Int(4053), Int(400)), Sym("dbar"))  # nopep8
    CONVERSIONS["DECIBAR:ATTOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 22)), Sym("dbar"))  # nopep8
    CONVERSIONS["DECIBAR:BAR"] = \
        Mul(Int(10), Sym("dbar"))  # nopep8
    CONVERSIONS["DECIBAR:CENTIBAR"] = \
        Mul(Rat(Int(1), Int(10)), Sym("dbar"))  # nopep8
    CONVERSIONS["DECIBAR:CENTIPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("dbar"))  # nopep8
    CONVERSIONS["DECIBAR:DECAPASCAL"] = \
        Mul(Rat(Int(1), Int(1000)), Sym("dbar"))  # nopep8
    CONVERSIONS["DECIBAR:DECIPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 5)), Sym("dbar"))  # nopep8
    CONVERSIONS["DECIBAR:EXAPASCAL"] = \
        Mul(Pow(10, 14), Sym("dbar"))  # nopep8
    CONVERSIONS["DECIBAR:FEMTOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 19)), Sym("dbar"))  # nopep8
    CONVERSIONS["DECIBAR:GIGAPASCAL"] = \
        Mul(Pow(10, 5), Sym("dbar"))  # nopep8
    CONVERSIONS["DECIBAR:HECTOPASCAL"] = \
        Mul(Rat(Int(1), Int(100)), Sym("dbar"))  # nopep8
    CONVERSIONS["DECIBAR:KILOBAR"] = \
        Mul(Pow(10, 4), Sym("dbar"))  # nopep8
    CONVERSIONS["DECIBAR:KILOPASCAL"] = \
        Mul(Rat(Int(1), Int(10)), Sym("dbar"))  # nopep8
    CONVERSIONS["DECIBAR:MEGABAR"] = \
        Mul(Pow(10, 7), Sym("dbar"))  # nopep8
    CONVERSIONS["DECIBAR:MEGAPASCAL"] = \
        Mul(Int(100), Sym("dbar"))  # nopep8
    CONVERSIONS["DECIBAR:MICROPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 10)), Sym("dbar"))  # nopep8
    CONVERSIONS["DECIBAR:MILLIBAR"] = \
        Mul(Rat(Int(1), Int(100)), Sym("dbar"))  # nopep8
    CONVERSIONS["DECIBAR:MILLIPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 7)), Sym("dbar"))  # nopep8
    CONVERSIONS["DECIBAR:MILLITORR"] = \
        Mul(Rat(Int(4053), Mul(Int(304), Pow(10, 6))), Sym("dbar"))  # nopep8
    CONVERSIONS["DECIBAR:MMHG"] = \
        Mul(Rat(Int("26664477483"), Mul(Int(2), Pow(10, 12))), Sym("dbar"))  # nopep8
    CONVERSIONS["DECIBAR:NANOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 13)), Sym("dbar"))  # nopep8
    CONVERSIONS["DECIBAR:PASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 4)), Sym("dbar"))  # nopep8
    CONVERSIONS["DECIBAR:PETAPASCAL"] = \
        Mul(Pow(10, 11), Sym("dbar"))  # nopep8
    CONVERSIONS["DECIBAR:PICOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 16)), Sym("dbar"))  # nopep8
    CONVERSIONS["DECIBAR:PSI"] = \
        Mul(Rat(Int("172368932329209"), Mul(Int(25), Pow(10, 13))), Sym("dbar"))  # nopep8
    CONVERSIONS["DECIBAR:TERAPASCAL"] = \
        Mul(Pow(10, 8), Sym("dbar"))  # nopep8
    CONVERSIONS["DECIBAR:TORR"] = \
        Mul(Rat(Int(4053), Int(304000)), Sym("dbar"))  # nopep8
    CONVERSIONS["DECIBAR:YOCTOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 28)), Sym("dbar"))  # nopep8
    CONVERSIONS["DECIBAR:YOTTAPASCAL"] = \
        Mul(Pow(10, 20), Sym("dbar"))  # nopep8
    CONVERSIONS["DECIBAR:ZEPTOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 25)), Sym("dbar"))  # nopep8
    CONVERSIONS["DECIBAR:ZETTAPASCAL"] = \
        Mul(Pow(10, 17), Sym("dbar"))  # nopep8
    CONVERSIONS["DECIPASCAL:ATHMOSPHERE"] = \
        Mul(Int(1013250), Sym("decipa"))  # nopep8
    CONVERSIONS["DECIPASCAL:ATTOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 17)), Sym("decipa"))  # nopep8
    CONVERSIONS["DECIPASCAL:BAR"] = \
        Mul(Pow(10, 6), Sym("decipa"))  # nopep8
    CONVERSIONS["DECIPASCAL:CENTIBAR"] = \
        Mul(Pow(10, 4), Sym("decipa"))  # nopep8
    CONVERSIONS["DECIPASCAL:CENTIPASCAL"] = \
        Mul(Rat(Int(1), Int(10)), Sym("decipa"))  # nopep8
    CONVERSIONS["DECIPASCAL:DECAPASCAL"] = \
        Mul(Int(100), Sym("decipa"))  # nopep8
    CONVERSIONS["DECIPASCAL:DECIBAR"] = \
        Mul(Pow(10, 5), Sym("decipa"))  # nopep8
    CONVERSIONS["DECIPASCAL:EXAPASCAL"] = \
        Mul(Pow(10, 19), Sym("decipa"))  # nopep8
    CONVERSIONS["DECIPASCAL:FEMTOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 14)), Sym("decipa"))  # nopep8
    CONVERSIONS["DECIPASCAL:GIGAPASCAL"] = \
        Mul(Pow(10, 10), Sym("decipa"))  # nopep8
    CONVERSIONS["DECIPASCAL:HECTOPASCAL"] = \
        Mul(Int(1000), Sym("decipa"))  # nopep8
    CONVERSIONS["DECIPASCAL:KILOBAR"] = \
        Mul(Pow(10, 9), Sym("decipa"))  # nopep8
    CONVERSIONS["DECIPASCAL:KILOPASCAL"] = \
        Mul(Pow(10, 4), Sym("decipa"))  # nopep8
    CONVERSIONS["DECIPASCAL:MEGABAR"] = \
        Mul(Pow(10, 12), Sym("decipa"))  # nopep8
    CONVERSIONS["DECIPASCAL:MEGAPASCAL"] = \
        Mul(Pow(10, 7), Sym("decipa"))  # nopep8
    CONVERSIONS["DECIPASCAL:MICROPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 5)), Sym("decipa"))  # nopep8
    CONVERSIONS["DECIPASCAL:MILLIBAR"] = \
        Mul(Int(1000), Sym("decipa"))  # nopep8
    CONVERSIONS["DECIPASCAL:MILLIPASCAL"] = \
        Mul(Rat(Int(1), Int(100)), Sym("decipa"))  # nopep8
    CONVERSIONS["DECIPASCAL:MILLITORR"] = \
        Mul(Rat(Int(4053), Int(3040)), Sym("decipa"))  # nopep8
    CONVERSIONS["DECIPASCAL:MMHG"] = \
        Mul(Rat(Int("26664477483"), Mul(Int(2), Pow(10, 7))), Sym("decipa"))  # nopep8
    CONVERSIONS["DECIPASCAL:NANOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 8)), Sym("decipa"))  # nopep8
    CONVERSIONS["DECIPASCAL:PASCAL"] = \
        Mul(Int(10), Sym("decipa"))  # nopep8
    CONVERSIONS["DECIPASCAL:PETAPASCAL"] = \
        Mul(Pow(10, 16), Sym("decipa"))  # nopep8
    CONVERSIONS["DECIPASCAL:PICOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 11)), Sym("decipa"))  # nopep8
    CONVERSIONS["DECIPASCAL:PSI"] = \
        Mul(Rat(Int("172368932329209"), Mul(Int(25), Pow(10, 8))), Sym("decipa"))  # nopep8
    CONVERSIONS["DECIPASCAL:TERAPASCAL"] = \
        Mul(Pow(10, 13), Sym("decipa"))  # nopep8
    CONVERSIONS["DECIPASCAL:TORR"] = \
        Mul(Rat(Int(101325), Int(76)), Sym("decipa"))  # nopep8
    CONVERSIONS["DECIPASCAL:YOCTOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 23)), Sym("decipa"))  # nopep8
    CONVERSIONS["DECIPASCAL:YOTTAPASCAL"] = \
        Mul(Pow(10, 25), Sym("decipa"))  # nopep8
    CONVERSIONS["DECIPASCAL:ZEPTOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 20)), Sym("decipa"))  # nopep8
    CONVERSIONS["DECIPASCAL:ZETTAPASCAL"] = \
        Mul(Pow(10, 22), Sym("decipa"))  # nopep8
    CONVERSIONS["EXAPASCAL:ATHMOSPHERE"] = \
        Mul(Rat(Int(4053), Mul(Int(4), Pow(10, 16))), Sym("exapa"))  # nopep8
    CONVERSIONS["EXAPASCAL:ATTOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 36)), Sym("exapa"))  # nopep8
    CONVERSIONS["EXAPASCAL:BAR"] = \
        Mul(Rat(Int(1), Pow(10, 13)), Sym("exapa"))  # nopep8
    CONVERSIONS["EXAPASCAL:CENTIBAR"] = \
        Mul(Rat(Int(1), Pow(10, 15)), Sym("exapa"))  # nopep8
    CONVERSIONS["EXAPASCAL:CENTIPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 20)), Sym("exapa"))  # nopep8
    CONVERSIONS["EXAPASCAL:DECAPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 17)), Sym("exapa"))  # nopep8
    CONVERSIONS["EXAPASCAL:DECIBAR"] = \
        Mul(Rat(Int(1), Pow(10, 14)), Sym("exapa"))  # nopep8
    CONVERSIONS["EXAPASCAL:DECIPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 19)), Sym("exapa"))  # nopep8
    CONVERSIONS["EXAPASCAL:FEMTOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 33)), Sym("exapa"))  # nopep8
    CONVERSIONS["EXAPASCAL:GIGAPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("exapa"))  # nopep8
    CONVERSIONS["EXAPASCAL:HECTOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 16)), Sym("exapa"))  # nopep8
    CONVERSIONS["EXAPASCAL:KILOBAR"] = \
        Mul(Rat(Int(1), Pow(10, 10)), Sym("exapa"))  # nopep8
    CONVERSIONS["EXAPASCAL:KILOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 15)), Sym("exapa"))  # nopep8
    CONVERSIONS["EXAPASCAL:MEGABAR"] = \
        Mul(Rat(Int(1), Pow(10, 7)), Sym("exapa"))  # nopep8
    CONVERSIONS["EXAPASCAL:MEGAPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 12)), Sym("exapa"))  # nopep8
    CONVERSIONS["EXAPASCAL:MICROPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 24)), Sym("exapa"))  # nopep8
    CONVERSIONS["EXAPASCAL:MILLIBAR"] = \
        Mul(Rat(Int(1), Pow(10, 16)), Sym("exapa"))  # nopep8
    CONVERSIONS["EXAPASCAL:MILLIPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 21)), Sym("exapa"))  # nopep8
    CONVERSIONS["EXAPASCAL:MILLITORR"] = \
        Mul(Rat(Int(4053), Mul(Int(304), Pow(10, 20))), Sym("exapa"))  # nopep8
    CONVERSIONS["EXAPASCAL:MMHG"] = \
        Mul(Rat(Int("26664477483"), Mul(Int(2), Pow(10, 26))), Sym("exapa"))  # nopep8
    CONVERSIONS["EXAPASCAL:NANOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 27)), Sym("exapa"))  # nopep8
    CONVERSIONS["EXAPASCAL:PASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 18)), Sym("exapa"))  # nopep8
    CONVERSIONS["EXAPASCAL:PETAPASCAL"] = \
        Mul(Rat(Int(1), Int(1000)), Sym("exapa"))  # nopep8
    CONVERSIONS["EXAPASCAL:PICOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 30)), Sym("exapa"))  # nopep8
    CONVERSIONS["EXAPASCAL:PSI"] = \
        Mul(Rat(Int("172368932329209"), Mul(Int(25), Pow(10, 27))), Sym("exapa"))  # nopep8
    CONVERSIONS["EXAPASCAL:TERAPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("exapa"))  # nopep8
    CONVERSIONS["EXAPASCAL:TORR"] = \
        Mul(Rat(Int(4053), Mul(Int(304), Pow(10, 17))), Sym("exapa"))  # nopep8
    CONVERSIONS["EXAPASCAL:YOCTOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 42)), Sym("exapa"))  # nopep8
    CONVERSIONS["EXAPASCAL:YOTTAPASCAL"] = \
        Mul(Pow(10, 6), Sym("exapa"))  # nopep8
    CONVERSIONS["EXAPASCAL:ZEPTOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 39)), Sym("exapa"))  # nopep8
    CONVERSIONS["EXAPASCAL:ZETTAPASCAL"] = \
        Mul(Int(1000), Sym("exapa"))  # nopep8
    CONVERSIONS["FEMTOPASCAL:ATHMOSPHERE"] = \
        Mul(Mul(Int(101325), Pow(10, 15)), Sym("femtopa"))  # nopep8
    CONVERSIONS["FEMTOPASCAL:ATTOPASCAL"] = \
        Mul(Rat(Int(1), Int(1000)), Sym("femtopa"))  # nopep8
    CONVERSIONS["FEMTOPASCAL:BAR"] = \
        Mul(Pow(10, 20), Sym("femtopa"))  # nopep8
    CONVERSIONS["FEMTOPASCAL:CENTIBAR"] = \
        Mul(Pow(10, 18), Sym("femtopa"))  # nopep8
    CONVERSIONS["FEMTOPASCAL:CENTIPASCAL"] = \
        Mul(Pow(10, 13), Sym("femtopa"))  # nopep8
    CONVERSIONS["FEMTOPASCAL:DECAPASCAL"] = \
        Mul(Pow(10, 16), Sym("femtopa"))  # nopep8
    CONVERSIONS["FEMTOPASCAL:DECIBAR"] = \
        Mul(Pow(10, 19), Sym("femtopa"))  # nopep8
    CONVERSIONS["FEMTOPASCAL:DECIPASCAL"] = \
        Mul(Pow(10, 14), Sym("femtopa"))  # nopep8
    CONVERSIONS["FEMTOPASCAL:EXAPASCAL"] = \
        Mul(Pow(10, 33), Sym("femtopa"))  # nopep8
    CONVERSIONS["FEMTOPASCAL:GIGAPASCAL"] = \
        Mul(Pow(10, 24), Sym("femtopa"))  # nopep8
    CONVERSIONS["FEMTOPASCAL:HECTOPASCAL"] = \
        Mul(Pow(10, 17), Sym("femtopa"))  # nopep8
    CONVERSIONS["FEMTOPASCAL:KILOBAR"] = \
        Mul(Pow(10, 23), Sym("femtopa"))  # nopep8
    CONVERSIONS["FEMTOPASCAL:KILOPASCAL"] = \
        Mul(Pow(10, 18), Sym("femtopa"))  # nopep8
    CONVERSIONS["FEMTOPASCAL:MEGABAR"] = \
        Mul(Pow(10, 26), Sym("femtopa"))  # nopep8
    CONVERSIONS["FEMTOPASCAL:MEGAPASCAL"] = \
        Mul(Pow(10, 21), Sym("femtopa"))  # nopep8
    CONVERSIONS["FEMTOPASCAL:MICROPASCAL"] = \
        Mul(Pow(10, 9), Sym("femtopa"))  # nopep8
    CONVERSIONS["FEMTOPASCAL:MILLIBAR"] = \
        Mul(Pow(10, 17), Sym("femtopa"))  # nopep8
    CONVERSIONS["FEMTOPASCAL:MILLIPASCAL"] = \
        Mul(Pow(10, 12), Sym("femtopa"))  # nopep8
    CONVERSIONS["FEMTOPASCAL:MILLITORR"] = \
        Mul(Rat(Mul(Int(2533125), Pow(10, 9)), Int(19)), Sym("femtopa"))  # nopep8
    CONVERSIONS["FEMTOPASCAL:MMHG"] = \
        Mul(Mul(Int("133322387415"), Pow(10, 6)), Sym("femtopa"))  # nopep8
    CONVERSIONS["FEMTOPASCAL:NANOPASCAL"] = \
        Mul(Pow(10, 6), Sym("femtopa"))  # nopep8
    CONVERSIONS["FEMTOPASCAL:PASCAL"] = \
        Mul(Pow(10, 15), Sym("femtopa"))  # nopep8
    CONVERSIONS["FEMTOPASCAL:PETAPASCAL"] = \
        Mul(Pow(10, 30), Sym("femtopa"))  # nopep8
    CONVERSIONS["FEMTOPASCAL:PICOPASCAL"] = \
        Mul(Int(1000), Sym("femtopa"))  # nopep8
    CONVERSIONS["FEMTOPASCAL:PSI"] = \
        Mul(Mul(Int("689475729316836"), Pow(10, 4)), Sym("femtopa"))  # nopep8
    CONVERSIONS["FEMTOPASCAL:TERAPASCAL"] = \
        Mul(Pow(10, 27), Sym("femtopa"))  # nopep8
    CONVERSIONS["FEMTOPASCAL:TORR"] = \
        Mul(Rat(Mul(Int(2533125), Pow(10, 12)), Int(19)), Sym("femtopa"))  # nopep8
    CONVERSIONS["FEMTOPASCAL:YOCTOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("femtopa"))  # nopep8
    CONVERSIONS["FEMTOPASCAL:YOTTAPASCAL"] = \
        Mul(Pow(10, 39), Sym("femtopa"))  # nopep8
    CONVERSIONS["FEMTOPASCAL:ZEPTOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("femtopa"))  # nopep8
    CONVERSIONS["FEMTOPASCAL:ZETTAPASCAL"] = \
        Mul(Pow(10, 36), Sym("femtopa"))  # nopep8
    CONVERSIONS["GIGAPASCAL:ATHMOSPHERE"] = \
        Mul(Rat(Int(4053), Mul(Int(4), Pow(10, 7))), Sym("gigapa"))  # nopep8
    CONVERSIONS["GIGAPASCAL:ATTOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 27)), Sym("gigapa"))  # nopep8
    CONVERSIONS["GIGAPASCAL:BAR"] = \
        Mul(Rat(Int(1), Pow(10, 4)), Sym("gigapa"))  # nopep8
    CONVERSIONS["GIGAPASCAL:CENTIBAR"] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("gigapa"))  # nopep8
    CONVERSIONS["GIGAPASCAL:CENTIPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 11)), Sym("gigapa"))  # nopep8
    CONVERSIONS["GIGAPASCAL:DECAPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 8)), Sym("gigapa"))  # nopep8
    CONVERSIONS["GIGAPASCAL:DECIBAR"] = \
        Mul(Rat(Int(1), Pow(10, 5)), Sym("gigapa"))  # nopep8
    CONVERSIONS["GIGAPASCAL:DECIPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 10)), Sym("gigapa"))  # nopep8
    CONVERSIONS["GIGAPASCAL:EXAPASCAL"] = \
        Mul(Pow(10, 9), Sym("gigapa"))  # nopep8
    CONVERSIONS["GIGAPASCAL:FEMTOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 24)), Sym("gigapa"))  # nopep8
    CONVERSIONS["GIGAPASCAL:HECTOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 7)), Sym("gigapa"))  # nopep8
    CONVERSIONS["GIGAPASCAL:KILOBAR"] = \
        Mul(Rat(Int(1), Int(10)), Sym("gigapa"))  # nopep8
    CONVERSIONS["GIGAPASCAL:KILOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("gigapa"))  # nopep8
    CONVERSIONS["GIGAPASCAL:MEGABAR"] = \
        Mul(Int(100), Sym("gigapa"))  # nopep8
    CONVERSIONS["GIGAPASCAL:MEGAPASCAL"] = \
        Mul(Rat(Int(1), Int(1000)), Sym("gigapa"))  # nopep8
    CONVERSIONS["GIGAPASCAL:MICROPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 15)), Sym("gigapa"))  # nopep8
    CONVERSIONS["GIGAPASCAL:MILLIBAR"] = \
        Mul(Rat(Int(1), Pow(10, 7)), Sym("gigapa"))  # nopep8
    CONVERSIONS["GIGAPASCAL:MILLIPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 12)), Sym("gigapa"))  # nopep8
    CONVERSIONS["GIGAPASCAL:MILLITORR"] = \
        Mul(Rat(Int(4053), Mul(Int(304), Pow(10, 11))), Sym("gigapa"))  # nopep8
    CONVERSIONS["GIGAPASCAL:MMHG"] = \
        Mul(Rat(Int("26664477483"), Mul(Int(2), Pow(10, 17))), Sym("gigapa"))  # nopep8
    CONVERSIONS["GIGAPASCAL:NANOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 18)), Sym("gigapa"))  # nopep8
    CONVERSIONS["GIGAPASCAL:PASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("gigapa"))  # nopep8
    CONVERSIONS["GIGAPASCAL:PETAPASCAL"] = \
        Mul(Pow(10, 6), Sym("gigapa"))  # nopep8
    CONVERSIONS["GIGAPASCAL:PICOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 21)), Sym("gigapa"))  # nopep8
    CONVERSIONS["GIGAPASCAL:PSI"] = \
        Mul(Rat(Int("172368932329209"), Mul(Int(25), Pow(10, 18))), Sym("gigapa"))  # nopep8
    CONVERSIONS["GIGAPASCAL:TERAPASCAL"] = \
        Mul(Int(1000), Sym("gigapa"))  # nopep8
    CONVERSIONS["GIGAPASCAL:TORR"] = \
        Mul(Rat(Int(4053), Mul(Int(304), Pow(10, 8))), Sym("gigapa"))  # nopep8
    CONVERSIONS["GIGAPASCAL:YOCTOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 33)), Sym("gigapa"))  # nopep8
    CONVERSIONS["GIGAPASCAL:YOTTAPASCAL"] = \
        Mul(Pow(10, 15), Sym("gigapa"))  # nopep8
    CONVERSIONS["GIGAPASCAL:ZEPTOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 30)), Sym("gigapa"))  # nopep8
    CONVERSIONS["GIGAPASCAL:ZETTAPASCAL"] = \
        Mul(Pow(10, 12), Sym("gigapa"))  # nopep8
    CONVERSIONS["HECTOPASCAL:ATHMOSPHERE"] = \
        Mul(Rat(Int(4053), Int(4)), Sym("hectopa"))  # nopep8
    CONVERSIONS["HECTOPASCAL:ATTOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 20)), Sym("hectopa"))  # nopep8
    CONVERSIONS["HECTOPASCAL:BAR"] = \
        Mul(Int(1000), Sym("hectopa"))  # nopep8
    CONVERSIONS["HECTOPASCAL:CENTIBAR"] = \
        Mul(Int(10), Sym("hectopa"))  # nopep8
    CONVERSIONS["HECTOPASCAL:CENTIPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 4)), Sym("hectopa"))  # nopep8
    CONVERSIONS["HECTOPASCAL:DECAPASCAL"] = \
        Mul(Rat(Int(1), Int(10)), Sym("hectopa"))  # nopep8
    CONVERSIONS["HECTOPASCAL:DECIBAR"] = \
        Mul(Int(100), Sym("hectopa"))  # nopep8
    CONVERSIONS["HECTOPASCAL:DECIPASCAL"] = \
        Mul(Rat(Int(1), Int(1000)), Sym("hectopa"))  # nopep8
    CONVERSIONS["HECTOPASCAL:EXAPASCAL"] = \
        Mul(Pow(10, 16), Sym("hectopa"))  # nopep8
    CONVERSIONS["HECTOPASCAL:FEMTOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 17)), Sym("hectopa"))  # nopep8
    CONVERSIONS["HECTOPASCAL:GIGAPASCAL"] = \
        Mul(Pow(10, 7), Sym("hectopa"))  # nopep8
    CONVERSIONS["HECTOPASCAL:KILOBAR"] = \
        Mul(Pow(10, 6), Sym("hectopa"))  # nopep8
    CONVERSIONS["HECTOPASCAL:KILOPASCAL"] = \
        Mul(Int(10), Sym("hectopa"))  # nopep8
    CONVERSIONS["HECTOPASCAL:MEGABAR"] = \
        Mul(Pow(10, 9), Sym("hectopa"))  # nopep8
    CONVERSIONS["HECTOPASCAL:MEGAPASCAL"] = \
        Mul(Pow(10, 4), Sym("hectopa"))  # nopep8
    CONVERSIONS["HECTOPASCAL:MICROPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 8)), Sym("hectopa"))  # nopep8
    CONVERSIONS["HECTOPASCAL:MILLIBAR"] = \
        Sym("hectopa")  # nopep8
    CONVERSIONS["HECTOPASCAL:MILLIPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 5)), Sym("hectopa"))  # nopep8
    CONVERSIONS["HECTOPASCAL:MILLITORR"] = \
        Mul(Rat(Int(4053), Mul(Int(304), Pow(10, 4))), Sym("hectopa"))  # nopep8
    CONVERSIONS["HECTOPASCAL:MMHG"] = \
        Mul(Rat(Int("26664477483"), Mul(Int(2), Pow(10, 10))), Sym("hectopa"))  # nopep8
    CONVERSIONS["HECTOPASCAL:NANOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 11)), Sym("hectopa"))  # nopep8
    CONVERSIONS["HECTOPASCAL:PASCAL"] = \
        Mul(Rat(Int(1), Int(100)), Sym("hectopa"))  # nopep8
    CONVERSIONS["HECTOPASCAL:PETAPASCAL"] = \
        Mul(Pow(10, 13), Sym("hectopa"))  # nopep8
    CONVERSIONS["HECTOPASCAL:PICOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 14)), Sym("hectopa"))  # nopep8
    CONVERSIONS["HECTOPASCAL:PSI"] = \
        Mul(Rat(Int("172368932329209"), Mul(Int(25), Pow(10, 11))), Sym("hectopa"))  # nopep8
    CONVERSIONS["HECTOPASCAL:TERAPASCAL"] = \
        Mul(Pow(10, 10), Sym("hectopa"))  # nopep8
    CONVERSIONS["HECTOPASCAL:TORR"] = \
        Mul(Rat(Int(4053), Int(3040)), Sym("hectopa"))  # nopep8
    CONVERSIONS["HECTOPASCAL:YOCTOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 26)), Sym("hectopa"))  # nopep8
    CONVERSIONS["HECTOPASCAL:YOTTAPASCAL"] = \
        Mul(Pow(10, 22), Sym("hectopa"))  # nopep8
    CONVERSIONS["HECTOPASCAL:ZEPTOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 23)), Sym("hectopa"))  # nopep8
    CONVERSIONS["HECTOPASCAL:ZETTAPASCAL"] = \
        Mul(Pow(10, 19), Sym("hectopa"))  # nopep8
    CONVERSIONS["KILOBAR:ATHMOSPHERE"] = \
        Mul(Rat(Int(4053), Mul(Int(4), Pow(10, 6))), Sym("kbar"))  # nopep8
    CONVERSIONS["KILOBAR:ATTOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 26)), Sym("kbar"))  # nopep8
    CONVERSIONS["KILOBAR:BAR"] = \
        Mul(Rat(Int(1), Int(1000)), Sym("kbar"))  # nopep8
    CONVERSIONS["KILOBAR:CENTIBAR"] = \
        Mul(Rat(Int(1), Pow(10, 5)), Sym("kbar"))  # nopep8
    CONVERSIONS["KILOBAR:CENTIPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 10)), Sym("kbar"))  # nopep8
    CONVERSIONS["KILOBAR:DECAPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 7)), Sym("kbar"))  # nopep8
    CONVERSIONS["KILOBAR:DECIBAR"] = \
        Mul(Rat(Int(1), Pow(10, 4)), Sym("kbar"))  # nopep8
    CONVERSIONS["KILOBAR:DECIPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("kbar"))  # nopep8
    CONVERSIONS["KILOBAR:EXAPASCAL"] = \
        Mul(Pow(10, 10), Sym("kbar"))  # nopep8
    CONVERSIONS["KILOBAR:FEMTOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 23)), Sym("kbar"))  # nopep8
    CONVERSIONS["KILOBAR:GIGAPASCAL"] = \
        Mul(Int(10), Sym("kbar"))  # nopep8
    CONVERSIONS["KILOBAR:HECTOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("kbar"))  # nopep8
    CONVERSIONS["KILOBAR:KILOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 5)), Sym("kbar"))  # nopep8
    CONVERSIONS["KILOBAR:MEGABAR"] = \
        Mul(Int(1000), Sym("kbar"))  # nopep8
    CONVERSIONS["KILOBAR:MEGAPASCAL"] = \
        Mul(Rat(Int(1), Int(100)), Sym("kbar"))  # nopep8
    CONVERSIONS["KILOBAR:MICROPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 14)), Sym("kbar"))  # nopep8
    CONVERSIONS["KILOBAR:MILLIBAR"] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("kbar"))  # nopep8
    CONVERSIONS["KILOBAR:MILLIPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 11)), Sym("kbar"))  # nopep8
    CONVERSIONS["KILOBAR:MILLITORR"] = \
        Mul(Rat(Int(4053), Mul(Int(304), Pow(10, 10))), Sym("kbar"))  # nopep8
    CONVERSIONS["KILOBAR:MMHG"] = \
        Mul(Rat(Int("26664477483"), Mul(Int(2), Pow(10, 16))), Sym("kbar"))  # nopep8
    CONVERSIONS["KILOBAR:NANOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 17)), Sym("kbar"))  # nopep8
    CONVERSIONS["KILOBAR:PASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 8)), Sym("kbar"))  # nopep8
    CONVERSIONS["KILOBAR:PETAPASCAL"] = \
        Mul(Pow(10, 7), Sym("kbar"))  # nopep8
    CONVERSIONS["KILOBAR:PICOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 20)), Sym("kbar"))  # nopep8
    CONVERSIONS["KILOBAR:PSI"] = \
        Mul(Rat(Int("172368932329209"), Mul(Int(25), Pow(10, 17))), Sym("kbar"))  # nopep8
    CONVERSIONS["KILOBAR:TERAPASCAL"] = \
        Mul(Pow(10, 4), Sym("kbar"))  # nopep8
    CONVERSIONS["KILOBAR:TORR"] = \
        Mul(Rat(Int(4053), Mul(Int(304), Pow(10, 7))), Sym("kbar"))  # nopep8
    CONVERSIONS["KILOBAR:YOCTOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 32)), Sym("kbar"))  # nopep8
    CONVERSIONS["KILOBAR:YOTTAPASCAL"] = \
        Mul(Pow(10, 16), Sym("kbar"))  # nopep8
    CONVERSIONS["KILOBAR:ZEPTOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 29)), Sym("kbar"))  # nopep8
    CONVERSIONS["KILOBAR:ZETTAPASCAL"] = \
        Mul(Pow(10, 13), Sym("kbar"))  # nopep8
    CONVERSIONS["KILOPASCAL:ATHMOSPHERE"] = \
        Mul(Rat(Int(4053), Int(40)), Sym("kilopa"))  # nopep8
    CONVERSIONS["KILOPASCAL:ATTOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 21)), Sym("kilopa"))  # nopep8
    CONVERSIONS["KILOPASCAL:BAR"] = \
        Mul(Int(100), Sym("kilopa"))  # nopep8
    CONVERSIONS["KILOPASCAL:CENTIBAR"] = \
        Sym("kilopa")  # nopep8
    CONVERSIONS["KILOPASCAL:CENTIPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 5)), Sym("kilopa"))  # nopep8
    CONVERSIONS["KILOPASCAL:DECAPASCAL"] = \
        Mul(Rat(Int(1), Int(100)), Sym("kilopa"))  # nopep8
    CONVERSIONS["KILOPASCAL:DECIBAR"] = \
        Mul(Int(10), Sym("kilopa"))  # nopep8
    CONVERSIONS["KILOPASCAL:DECIPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 4)), Sym("kilopa"))  # nopep8
    CONVERSIONS["KILOPASCAL:EXAPASCAL"] = \
        Mul(Pow(10, 15), Sym("kilopa"))  # nopep8
    CONVERSIONS["KILOPASCAL:FEMTOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 18)), Sym("kilopa"))  # nopep8
    CONVERSIONS["KILOPASCAL:GIGAPASCAL"] = \
        Mul(Pow(10, 6), Sym("kilopa"))  # nopep8
    CONVERSIONS["KILOPASCAL:HECTOPASCAL"] = \
        Mul(Rat(Int(1), Int(10)), Sym("kilopa"))  # nopep8
    CONVERSIONS["KILOPASCAL:KILOBAR"] = \
        Mul(Pow(10, 5), Sym("kilopa"))  # nopep8
    CONVERSIONS["KILOPASCAL:MEGABAR"] = \
        Mul(Pow(10, 8), Sym("kilopa"))  # nopep8
    CONVERSIONS["KILOPASCAL:MEGAPASCAL"] = \
        Mul(Int(1000), Sym("kilopa"))  # nopep8
    CONVERSIONS["KILOPASCAL:MICROPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("kilopa"))  # nopep8
    CONVERSIONS["KILOPASCAL:MILLIBAR"] = \
        Mul(Rat(Int(1), Int(10)), Sym("kilopa"))  # nopep8
    CONVERSIONS["KILOPASCAL:MILLIPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("kilopa"))  # nopep8
    CONVERSIONS["KILOPASCAL:MILLITORR"] = \
        Mul(Rat(Int(4053), Mul(Int(304), Pow(10, 5))), Sym("kilopa"))  # nopep8
    CONVERSIONS["KILOPASCAL:MMHG"] = \
        Mul(Rat(Int("26664477483"), Mul(Int(2), Pow(10, 11))), Sym("kilopa"))  # nopep8
    CONVERSIONS["KILOPASCAL:NANOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 12)), Sym("kilopa"))  # nopep8
    CONVERSIONS["KILOPASCAL:PASCAL"] = \
        Mul(Rat(Int(1), Int(1000)), Sym("kilopa"))  # nopep8
    CONVERSIONS["KILOPASCAL:PETAPASCAL"] = \
        Mul(Pow(10, 12), Sym("kilopa"))  # nopep8
    CONVERSIONS["KILOPASCAL:PICOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 15)), Sym("kilopa"))  # nopep8
    CONVERSIONS["KILOPASCAL:PSI"] = \
        Mul(Rat(Int("172368932329209"), Mul(Int(25), Pow(10, 12))), Sym("kilopa"))  # nopep8
    CONVERSIONS["KILOPASCAL:TERAPASCAL"] = \
        Mul(Pow(10, 9), Sym("kilopa"))  # nopep8
    CONVERSIONS["KILOPASCAL:TORR"] = \
        Mul(Rat(Int(4053), Int(30400)), Sym("kilopa"))  # nopep8
    CONVERSIONS["KILOPASCAL:YOCTOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 27)), Sym("kilopa"))  # nopep8
    CONVERSIONS["KILOPASCAL:YOTTAPASCAL"] = \
        Mul(Pow(10, 21), Sym("kilopa"))  # nopep8
    CONVERSIONS["KILOPASCAL:ZEPTOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 24)), Sym("kilopa"))  # nopep8
    CONVERSIONS["KILOPASCAL:ZETTAPASCAL"] = \
        Mul(Pow(10, 18), Sym("kilopa"))  # nopep8
    CONVERSIONS["MEGABAR:ATHMOSPHERE"] = \
        Mul(Rat(Int(4053), Mul(Int(4), Pow(10, 9))), Sym("megabar"))  # nopep8
    CONVERSIONS["MEGABAR:ATTOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 29)), Sym("megabar"))  # nopep8
    CONVERSIONS["MEGABAR:BAR"] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("megabar"))  # nopep8
    CONVERSIONS["MEGABAR:CENTIBAR"] = \
        Mul(Rat(Int(1), Pow(10, 8)), Sym("megabar"))  # nopep8
    CONVERSIONS["MEGABAR:CENTIPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 13)), Sym("megabar"))  # nopep8
    CONVERSIONS["MEGABAR:DECAPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 10)), Sym("megabar"))  # nopep8
    CONVERSIONS["MEGABAR:DECIBAR"] = \
        Mul(Rat(Int(1), Pow(10, 7)), Sym("megabar"))  # nopep8
    CONVERSIONS["MEGABAR:DECIPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 12)), Sym("megabar"))  # nopep8
    CONVERSIONS["MEGABAR:EXAPASCAL"] = \
        Mul(Pow(10, 7), Sym("megabar"))  # nopep8
    CONVERSIONS["MEGABAR:FEMTOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 26)), Sym("megabar"))  # nopep8
    CONVERSIONS["MEGABAR:GIGAPASCAL"] = \
        Mul(Rat(Int(1), Int(100)), Sym("megabar"))  # nopep8
    CONVERSIONS["MEGABAR:HECTOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("megabar"))  # nopep8
    CONVERSIONS["MEGABAR:KILOBAR"] = \
        Mul(Rat(Int(1), Int(1000)), Sym("megabar"))  # nopep8
    CONVERSIONS["MEGABAR:KILOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 8)), Sym("megabar"))  # nopep8
    CONVERSIONS["MEGABAR:MEGAPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 5)), Sym("megabar"))  # nopep8
    CONVERSIONS["MEGABAR:MICROPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 17)), Sym("megabar"))  # nopep8
    CONVERSIONS["MEGABAR:MILLIBAR"] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("megabar"))  # nopep8
    CONVERSIONS["MEGABAR:MILLIPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 14)), Sym("megabar"))  # nopep8
    CONVERSIONS["MEGABAR:MILLITORR"] = \
        Mul(Rat(Int(4053), Mul(Int(304), Pow(10, 13))), Sym("megabar"))  # nopep8
    CONVERSIONS["MEGABAR:MMHG"] = \
        Mul(Rat(Int("26664477483"), Mul(Int(2), Pow(10, 19))), Sym("megabar"))  # nopep8
    CONVERSIONS["MEGABAR:NANOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 20)), Sym("megabar"))  # nopep8
    CONVERSIONS["MEGABAR:PASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 11)), Sym("megabar"))  # nopep8
    CONVERSIONS["MEGABAR:PETAPASCAL"] = \
        Mul(Pow(10, 4), Sym("megabar"))  # nopep8
    CONVERSIONS["MEGABAR:PICOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 23)), Sym("megabar"))  # nopep8
    CONVERSIONS["MEGABAR:PSI"] = \
        Mul(Rat(Int("172368932329209"), Mul(Int(25), Pow(10, 20))), Sym("megabar"))  # nopep8
    CONVERSIONS["MEGABAR:TERAPASCAL"] = \
        Mul(Int(10), Sym("megabar"))  # nopep8
    CONVERSIONS["MEGABAR:TORR"] = \
        Mul(Rat(Int(4053), Mul(Int(304), Pow(10, 10))), Sym("megabar"))  # nopep8
    CONVERSIONS["MEGABAR:YOCTOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 35)), Sym("megabar"))  # nopep8
    CONVERSIONS["MEGABAR:YOTTAPASCAL"] = \
        Mul(Pow(10, 13), Sym("megabar"))  # nopep8
    CONVERSIONS["MEGABAR:ZEPTOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 32)), Sym("megabar"))  # nopep8
    CONVERSIONS["MEGABAR:ZETTAPASCAL"] = \
        Mul(Pow(10, 10), Sym("megabar"))  # nopep8
    CONVERSIONS["MEGAPASCAL:ATHMOSPHERE"] = \
        Mul(Rat(Int(4053), Mul(Int(4), Pow(10, 4))), Sym("megapa"))  # nopep8
    CONVERSIONS["MEGAPASCAL:ATTOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 24)), Sym("megapa"))  # nopep8
    CONVERSIONS["MEGAPASCAL:BAR"] = \
        Mul(Rat(Int(1), Int(10)), Sym("megapa"))  # nopep8
    CONVERSIONS["MEGAPASCAL:CENTIBAR"] = \
        Mul(Rat(Int(1), Int(1000)), Sym("megapa"))  # nopep8
    CONVERSIONS["MEGAPASCAL:CENTIPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 8)), Sym("megapa"))  # nopep8
    CONVERSIONS["MEGAPASCAL:DECAPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 5)), Sym("megapa"))  # nopep8
    CONVERSIONS["MEGAPASCAL:DECIBAR"] = \
        Mul(Rat(Int(1), Int(100)), Sym("megapa"))  # nopep8
    CONVERSIONS["MEGAPASCAL:DECIPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 7)), Sym("megapa"))  # nopep8
    CONVERSIONS["MEGAPASCAL:EXAPASCAL"] = \
        Mul(Pow(10, 12), Sym("megapa"))  # nopep8
    CONVERSIONS["MEGAPASCAL:FEMTOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 21)), Sym("megapa"))  # nopep8
    CONVERSIONS["MEGAPASCAL:GIGAPASCAL"] = \
        Mul(Int(1000), Sym("megapa"))  # nopep8
    CONVERSIONS["MEGAPASCAL:HECTOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 4)), Sym("megapa"))  # nopep8
    CONVERSIONS["MEGAPASCAL:KILOBAR"] = \
        Mul(Int(100), Sym("megapa"))  # nopep8
    CONVERSIONS["MEGAPASCAL:KILOPASCAL"] = \
        Mul(Rat(Int(1), Int(1000)), Sym("megapa"))  # nopep8
    CONVERSIONS["MEGAPASCAL:MEGABAR"] = \
        Mul(Pow(10, 5), Sym("megapa"))  # nopep8
    CONVERSIONS["MEGAPASCAL:MICROPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 12)), Sym("megapa"))  # nopep8
    CONVERSIONS["MEGAPASCAL:MILLIBAR"] = \
        Mul(Rat(Int(1), Pow(10, 4)), Sym("megapa"))  # nopep8
    CONVERSIONS["MEGAPASCAL:MILLIPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("megapa"))  # nopep8
    CONVERSIONS["MEGAPASCAL:MILLITORR"] = \
        Mul(Rat(Int(4053), Mul(Int(304), Pow(10, 8))), Sym("megapa"))  # nopep8
    CONVERSIONS["MEGAPASCAL:MMHG"] = \
        Mul(Rat(Int("26664477483"), Mul(Int(2), Pow(10, 14))), Sym("megapa"))  # nopep8
    CONVERSIONS["MEGAPASCAL:NANOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 15)), Sym("megapa"))  # nopep8
    CONVERSIONS["MEGAPASCAL:PASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("megapa"))  # nopep8
    CONVERSIONS["MEGAPASCAL:PETAPASCAL"] = \
        Mul(Pow(10, 9), Sym("megapa"))  # nopep8
    CONVERSIONS["MEGAPASCAL:PICOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 18)), Sym("megapa"))  # nopep8
    CONVERSIONS["MEGAPASCAL:PSI"] = \
        Mul(Rat(Int("172368932329209"), Mul(Int(25), Pow(10, 15))), Sym("megapa"))  # nopep8
    CONVERSIONS["MEGAPASCAL:TERAPASCAL"] = \
        Mul(Pow(10, 6), Sym("megapa"))  # nopep8
    CONVERSIONS["MEGAPASCAL:TORR"] = \
        Mul(Rat(Int(4053), Mul(Int(304), Pow(10, 5))), Sym("megapa"))  # nopep8
    CONVERSIONS["MEGAPASCAL:YOCTOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 30)), Sym("megapa"))  # nopep8
    CONVERSIONS["MEGAPASCAL:YOTTAPASCAL"] = \
        Mul(Pow(10, 18), Sym("megapa"))  # nopep8
    CONVERSIONS["MEGAPASCAL:ZEPTOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 27)), Sym("megapa"))  # nopep8
    CONVERSIONS["MEGAPASCAL:ZETTAPASCAL"] = \
        Mul(Pow(10, 15), Sym("megapa"))  # nopep8
    CONVERSIONS["MICROPASCAL:ATHMOSPHERE"] = \
        Mul(Mul(Int(101325), Pow(10, 6)), Sym("micropa"))  # nopep8
    CONVERSIONS["MICROPASCAL:ATTOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 12)), Sym("micropa"))  # nopep8
    CONVERSIONS["MICROPASCAL:BAR"] = \
        Mul(Pow(10, 11), Sym("micropa"))  # nopep8
    CONVERSIONS["MICROPASCAL:CENTIBAR"] = \
        Mul(Pow(10, 9), Sym("micropa"))  # nopep8
    CONVERSIONS["MICROPASCAL:CENTIPASCAL"] = \
        Mul(Pow(10, 4), Sym("micropa"))  # nopep8
    CONVERSIONS["MICROPASCAL:DECAPASCAL"] = \
        Mul(Pow(10, 7), Sym("micropa"))  # nopep8
    CONVERSIONS["MICROPASCAL:DECIBAR"] = \
        Mul(Pow(10, 10), Sym("micropa"))  # nopep8
    CONVERSIONS["MICROPASCAL:DECIPASCAL"] = \
        Mul(Pow(10, 5), Sym("micropa"))  # nopep8
    CONVERSIONS["MICROPASCAL:EXAPASCAL"] = \
        Mul(Pow(10, 24), Sym("micropa"))  # nopep8
    CONVERSIONS["MICROPASCAL:FEMTOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("micropa"))  # nopep8
    CONVERSIONS["MICROPASCAL:GIGAPASCAL"] = \
        Mul(Pow(10, 15), Sym("micropa"))  # nopep8
    CONVERSIONS["MICROPASCAL:HECTOPASCAL"] = \
        Mul(Pow(10, 8), Sym("micropa"))  # nopep8
    CONVERSIONS["MICROPASCAL:KILOBAR"] = \
        Mul(Pow(10, 14), Sym("micropa"))  # nopep8
    CONVERSIONS["MICROPASCAL:KILOPASCAL"] = \
        Mul(Pow(10, 9), Sym("micropa"))  # nopep8
    CONVERSIONS["MICROPASCAL:MEGABAR"] = \
        Mul(Pow(10, 17), Sym("micropa"))  # nopep8
    CONVERSIONS["MICROPASCAL:MEGAPASCAL"] = \
        Mul(Pow(10, 12), Sym("micropa"))  # nopep8
    CONVERSIONS["MICROPASCAL:MILLIBAR"] = \
        Mul(Pow(10, 8), Sym("micropa"))  # nopep8
    CONVERSIONS["MICROPASCAL:MILLIPASCAL"] = \
        Mul(Int(1000), Sym("micropa"))  # nopep8
    CONVERSIONS["MICROPASCAL:MILLITORR"] = \
        Mul(Rat(Int(2533125), Int(19)), Sym("micropa"))  # nopep8
    CONVERSIONS["MICROPASCAL:MMHG"] = \
        Mul(Rat(Int("26664477483"), Int(200)), Sym("micropa"))  # nopep8
    CONVERSIONS["MICROPASCAL:NANOPASCAL"] = \
        Mul(Rat(Int(1), Int(1000)), Sym("micropa"))  # nopep8
    CONVERSIONS["MICROPASCAL:PASCAL"] = \
        Mul(Pow(10, 6), Sym("micropa"))  # nopep8
    CONVERSIONS["MICROPASCAL:PETAPASCAL"] = \
        Mul(Pow(10, 21), Sym("micropa"))  # nopep8
    CONVERSIONS["MICROPASCAL:PICOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("micropa"))  # nopep8
    CONVERSIONS["MICROPASCAL:PSI"] = \
        Mul(Rat(Int("172368932329209"), Int(25000)), Sym("micropa"))  # nopep8
    CONVERSIONS["MICROPASCAL:TERAPASCAL"] = \
        Mul(Pow(10, 18), Sym("micropa"))  # nopep8
    CONVERSIONS["MICROPASCAL:TORR"] = \
        Mul(Rat(Int("2533125000"), Int(19)), Sym("micropa"))  # nopep8
    CONVERSIONS["MICROPASCAL:YOCTOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 18)), Sym("micropa"))  # nopep8
    CONVERSIONS["MICROPASCAL:YOTTAPASCAL"] = \
        Mul(Pow(10, 30), Sym("micropa"))  # nopep8
    CONVERSIONS["MICROPASCAL:ZEPTOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 15)), Sym("micropa"))  # nopep8
    CONVERSIONS["MICROPASCAL:ZETTAPASCAL"] = \
        Mul(Pow(10, 27), Sym("micropa"))  # nopep8
    CONVERSIONS["MILLIBAR:ATHMOSPHERE"] = \
        Mul(Rat(Int(4053), Int(4)), Sym("mbar"))  # nopep8
    CONVERSIONS["MILLIBAR:ATTOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 20)), Sym("mbar"))  # nopep8
    CONVERSIONS["MILLIBAR:BAR"] = \
        Mul(Int(1000), Sym("mbar"))  # nopep8
    CONVERSIONS["MILLIBAR:CENTIBAR"] = \
        Mul(Int(10), Sym("mbar"))  # nopep8
    CONVERSIONS["MILLIBAR:CENTIPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 4)), Sym("mbar"))  # nopep8
    CONVERSIONS["MILLIBAR:DECAPASCAL"] = \
        Mul(Rat(Int(1), Int(10)), Sym("mbar"))  # nopep8
    CONVERSIONS["MILLIBAR:DECIBAR"] = \
        Mul(Int(100), Sym("mbar"))  # nopep8
    CONVERSIONS["MILLIBAR:DECIPASCAL"] = \
        Mul(Rat(Int(1), Int(1000)), Sym("mbar"))  # nopep8
    CONVERSIONS["MILLIBAR:EXAPASCAL"] = \
        Mul(Pow(10, 16), Sym("mbar"))  # nopep8
    CONVERSIONS["MILLIBAR:FEMTOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 17)), Sym("mbar"))  # nopep8
    CONVERSIONS["MILLIBAR:GIGAPASCAL"] = \
        Mul(Pow(10, 7), Sym("mbar"))  # nopep8
    CONVERSIONS["MILLIBAR:HECTOPASCAL"] = \
        Sym("mbar")  # nopep8
    CONVERSIONS["MILLIBAR:KILOBAR"] = \
        Mul(Pow(10, 6), Sym("mbar"))  # nopep8
    CONVERSIONS["MILLIBAR:KILOPASCAL"] = \
        Mul(Int(10), Sym("mbar"))  # nopep8
    CONVERSIONS["MILLIBAR:MEGABAR"] = \
        Mul(Pow(10, 9), Sym("mbar"))  # nopep8
    CONVERSIONS["MILLIBAR:MEGAPASCAL"] = \
        Mul(Pow(10, 4), Sym("mbar"))  # nopep8
    CONVERSIONS["MILLIBAR:MICROPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 8)), Sym("mbar"))  # nopep8
    CONVERSIONS["MILLIBAR:MILLIPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 5)), Sym("mbar"))  # nopep8
    CONVERSIONS["MILLIBAR:MILLITORR"] = \
        Mul(Rat(Int(4053), Mul(Int(304), Pow(10, 4))), Sym("mbar"))  # nopep8
    CONVERSIONS["MILLIBAR:MMHG"] = \
        Mul(Rat(Int("26664477483"), Mul(Int(2), Pow(10, 10))), Sym("mbar"))  # nopep8
    CONVERSIONS["MILLIBAR:NANOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 11)), Sym("mbar"))  # nopep8
    CONVERSIONS["MILLIBAR:PASCAL"] = \
        Mul(Rat(Int(1), Int(100)), Sym("mbar"))  # nopep8
    CONVERSIONS["MILLIBAR:PETAPASCAL"] = \
        Mul(Pow(10, 13), Sym("mbar"))  # nopep8
    CONVERSIONS["MILLIBAR:PICOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 14)), Sym("mbar"))  # nopep8
    CONVERSIONS["MILLIBAR:PSI"] = \
        Mul(Rat(Int("172368932329209"), Mul(Int(25), Pow(10, 11))), Sym("mbar"))  # nopep8
    CONVERSIONS["MILLIBAR:TERAPASCAL"] = \
        Mul(Pow(10, 10), Sym("mbar"))  # nopep8
    CONVERSIONS["MILLIBAR:TORR"] = \
        Mul(Rat(Int(4053), Int(3040)), Sym("mbar"))  # nopep8
    CONVERSIONS["MILLIBAR:YOCTOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 26)), Sym("mbar"))  # nopep8
    CONVERSIONS["MILLIBAR:YOTTAPASCAL"] = \
        Mul(Pow(10, 22), Sym("mbar"))  # nopep8
    CONVERSIONS["MILLIBAR:ZEPTOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 23)), Sym("mbar"))  # nopep8
    CONVERSIONS["MILLIBAR:ZETTAPASCAL"] = \
        Mul(Pow(10, 19), Sym("mbar"))  # nopep8
    CONVERSIONS["MILLIPASCAL:ATHMOSPHERE"] = \
        Mul(Int(101325000), Sym("millipa"))  # nopep8
    CONVERSIONS["MILLIPASCAL:ATTOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 15)), Sym("millipa"))  # nopep8
    CONVERSIONS["MILLIPASCAL:BAR"] = \
        Mul(Pow(10, 8), Sym("millipa"))  # nopep8
    CONVERSIONS["MILLIPASCAL:CENTIBAR"] = \
        Mul(Pow(10, 6), Sym("millipa"))  # nopep8
    CONVERSIONS["MILLIPASCAL:CENTIPASCAL"] = \
        Mul(Int(10), Sym("millipa"))  # nopep8
    CONVERSIONS["MILLIPASCAL:DECAPASCAL"] = \
        Mul(Pow(10, 4), Sym("millipa"))  # nopep8
    CONVERSIONS["MILLIPASCAL:DECIBAR"] = \
        Mul(Pow(10, 7), Sym("millipa"))  # nopep8
    CONVERSIONS["MILLIPASCAL:DECIPASCAL"] = \
        Mul(Int(100), Sym("millipa"))  # nopep8
    CONVERSIONS["MILLIPASCAL:EXAPASCAL"] = \
        Mul(Pow(10, 21), Sym("millipa"))  # nopep8
    CONVERSIONS["MILLIPASCAL:FEMTOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 12)), Sym("millipa"))  # nopep8
    CONVERSIONS["MILLIPASCAL:GIGAPASCAL"] = \
        Mul(Pow(10, 12), Sym("millipa"))  # nopep8
    CONVERSIONS["MILLIPASCAL:HECTOPASCAL"] = \
        Mul(Pow(10, 5), Sym("millipa"))  # nopep8
    CONVERSIONS["MILLIPASCAL:KILOBAR"] = \
        Mul(Pow(10, 11), Sym("millipa"))  # nopep8
    CONVERSIONS["MILLIPASCAL:KILOPASCAL"] = \
        Mul(Pow(10, 6), Sym("millipa"))  # nopep8
    CONVERSIONS["MILLIPASCAL:MEGABAR"] = \
        Mul(Pow(10, 14), Sym("millipa"))  # nopep8
    CONVERSIONS["MILLIPASCAL:MEGAPASCAL"] = \
        Mul(Pow(10, 9), Sym("millipa"))  # nopep8
    CONVERSIONS["MILLIPASCAL:MICROPASCAL"] = \
        Mul(Rat(Int(1), Int(1000)), Sym("millipa"))  # nopep8
    CONVERSIONS["MILLIPASCAL:MILLIBAR"] = \
        Mul(Pow(10, 5), Sym("millipa"))  # nopep8
    CONVERSIONS["MILLIPASCAL:MILLITORR"] = \
        Mul(Rat(Int(20265), Int(152)), Sym("millipa"))  # nopep8
    CONVERSIONS["MILLIPASCAL:MMHG"] = \
        Mul(Rat(Int("26664477483"), Mul(Int(2), Pow(10, 5))), Sym("millipa"))  # nopep8
    CONVERSIONS["MILLIPASCAL:NANOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("millipa"))  # nopep8
    CONVERSIONS["MILLIPASCAL:PASCAL"] = \
        Mul(Int(1000), Sym("millipa"))  # nopep8
    CONVERSIONS["MILLIPASCAL:PETAPASCAL"] = \
        Mul(Pow(10, 18), Sym("millipa"))  # nopep8
    CONVERSIONS["MILLIPASCAL:PICOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("millipa"))  # nopep8
    CONVERSIONS["MILLIPASCAL:PSI"] = \
        Mul(Rat(Int("172368932329209"), Mul(Int(25), Pow(10, 6))), Sym("millipa"))  # nopep8
    CONVERSIONS["MILLIPASCAL:TERAPASCAL"] = \
        Mul(Pow(10, 15), Sym("millipa"))  # nopep8
    CONVERSIONS["MILLIPASCAL:TORR"] = \
        Mul(Rat(Int(2533125), Int(19)), Sym("millipa"))  # nopep8
    CONVERSIONS["MILLIPASCAL:YOCTOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 21)), Sym("millipa"))  # nopep8
    CONVERSIONS["MILLIPASCAL:YOTTAPASCAL"] = \
        Mul(Pow(10, 27), Sym("millipa"))  # nopep8
    CONVERSIONS["MILLIPASCAL:ZEPTOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 18)), Sym("millipa"))  # nopep8
    CONVERSIONS["MILLIPASCAL:ZETTAPASCAL"] = \
        Mul(Pow(10, 24), Sym("millipa"))  # nopep8
    CONVERSIONS["MILLITORR:ATHMOSPHERE"] = \
        Mul(Mul(Int(76), Pow(10, 4)), Sym("mtorr"))  # nopep8
    CONVERSIONS["MILLITORR:ATTOPASCAL"] = \
        Mul(Rat(Int(19), Mul(Int(2533125), Pow(10, 12))), Sym("mtorr"))  # nopep8
    CONVERSIONS["MILLITORR:BAR"] = \
        Mul(Rat(Mul(Int(304), Pow(10, 7)), Int(4053)), Sym("mtorr"))  # nopep8
    CONVERSIONS["MILLITORR:CENTIBAR"] = \
        Mul(Rat(Mul(Int(304), Pow(10, 5)), Int(4053)), Sym("mtorr"))  # nopep8
    CONVERSIONS["MILLITORR:CENTIPASCAL"] = \
        Mul(Rat(Int(304), Int(4053)), Sym("mtorr"))  # nopep8
    CONVERSIONS["MILLITORR:DECAPASCAL"] = \
        Mul(Rat(Int(304000), Int(4053)), Sym("mtorr"))  # nopep8
    CONVERSIONS["MILLITORR:DECIBAR"] = \
        Mul(Rat(Mul(Int(304), Pow(10, 6)), Int(4053)), Sym("mtorr"))  # nopep8
    CONVERSIONS["MILLITORR:DECIPASCAL"] = \
        Mul(Rat(Int(3040), Int(4053)), Sym("mtorr"))  # nopep8
    CONVERSIONS["MILLITORR:EXAPASCAL"] = \
        Mul(Rat(Mul(Int(304), Pow(10, 20)), Int(4053)), Sym("mtorr"))  # nopep8
    CONVERSIONS["MILLITORR:FEMTOPASCAL"] = \
        Mul(Rat(Int(19), Mul(Int(2533125), Pow(10, 9))), Sym("mtorr"))  # nopep8
    CONVERSIONS["MILLITORR:GIGAPASCAL"] = \
        Mul(Rat(Mul(Int(304), Pow(10, 11)), Int(4053)), Sym("mtorr"))  # nopep8
    CONVERSIONS["MILLITORR:HECTOPASCAL"] = \
        Mul(Rat(Mul(Int(304), Pow(10, 4)), Int(4053)), Sym("mtorr"))  # nopep8
    CONVERSIONS["MILLITORR:KILOBAR"] = \
        Mul(Rat(Mul(Int(304), Pow(10, 10)), Int(4053)), Sym("mtorr"))  # nopep8
    CONVERSIONS["MILLITORR:KILOPASCAL"] = \
        Mul(Rat(Mul(Int(304), Pow(10, 5)), Int(4053)), Sym("mtorr"))  # nopep8
    CONVERSIONS["MILLITORR:MEGABAR"] = \
        Mul(Rat(Mul(Int(304), Pow(10, 13)), Int(4053)), Sym("mtorr"))  # nopep8
    CONVERSIONS["MILLITORR:MEGAPASCAL"] = \
        Mul(Rat(Mul(Int(304), Pow(10, 8)), Int(4053)), Sym("mtorr"))  # nopep8
    CONVERSIONS["MILLITORR:MICROPASCAL"] = \
        Mul(Rat(Int(19), Int(2533125)), Sym("mtorr"))  # nopep8
    CONVERSIONS["MILLITORR:MILLIBAR"] = \
        Mul(Rat(Mul(Int(304), Pow(10, 4)), Int(4053)), Sym("mtorr"))  # nopep8
    CONVERSIONS["MILLITORR:MILLIPASCAL"] = \
        Mul(Rat(Int(152), Int(20265)), Sym("mtorr"))  # nopep8
    CONVERSIONS["MILLITORR:MMHG"] = \
        Mul(Rat(Int("24125003437"), Int(24125000)), Sym("mtorr"))  # nopep8
    CONVERSIONS["MILLITORR:NANOPASCAL"] = \
        Mul(Rat(Int(19), Int("2533125000")), Sym("mtorr"))  # nopep8
    CONVERSIONS["MILLITORR:PASCAL"] = \
        Mul(Rat(Int(30400), Int(4053)), Sym("mtorr"))  # nopep8
    CONVERSIONS["MILLITORR:PETAPASCAL"] = \
        Mul(Rat(Mul(Int(304), Pow(10, 17)), Int(4053)), Sym("mtorr"))  # nopep8
    CONVERSIONS["MILLITORR:PICOPASCAL"] = \
        Mul(Rat(Int(19), Mul(Int(2533125), Pow(10, 6))), Sym("mtorr"))  # nopep8
    CONVERSIONS["MILLITORR:PSI"] = \
        Mul(Rat(Int("155952843535951"), Int("3015625000")), Sym("mtorr"))  # nopep8
    CONVERSIONS["MILLITORR:TERAPASCAL"] = \
        Mul(Rat(Mul(Int(304), Pow(10, 14)), Int(4053)), Sym("mtorr"))  # nopep8
    CONVERSIONS["MILLITORR:TORR"] = \
        Mul(Int(1000), Sym("mtorr"))  # nopep8
    CONVERSIONS["MILLITORR:YOCTOPASCAL"] = \
        Mul(Rat(Int(19), Mul(Int(2533125), Pow(10, 18))), Sym("mtorr"))  # nopep8
    CONVERSIONS["MILLITORR:YOTTAPASCAL"] = \
        Mul(Rat(Mul(Int(304), Pow(10, 26)), Int(4053)), Sym("mtorr"))  # nopep8
    CONVERSIONS["MILLITORR:ZEPTOPASCAL"] = \
        Mul(Rat(Int(19), Mul(Int(2533125), Pow(10, 15))), Sym("mtorr"))  # nopep8
    CONVERSIONS["MILLITORR:ZETTAPASCAL"] = \
        Mul(Rat(Mul(Int(304), Pow(10, 23)), Int(4053)), Sym("mtorr"))  # nopep8
    CONVERSIONS["MMHG:ATHMOSPHERE"] = \
        Mul(Rat(Mul(Int(965), Pow(10, 9)), Int(1269737023)), Sym("mmhg"))  # nopep8
    CONVERSIONS["MMHG:ATTOPASCAL"] = \
        Mul(Rat(Int(1), Mul(Int("133322387415"), Pow(10, 9))), Sym("mmhg"))  # nopep8
    CONVERSIONS["MMHG:BAR"] = \
        Mul(Rat(Mul(Int(2), Pow(10, 13)), Int("26664477483")), Sym("mmhg"))  # nopep8
    CONVERSIONS["MMHG:CENTIBAR"] = \
        Mul(Rat(Mul(Int(2), Pow(10, 11)), Int("26664477483")), Sym("mmhg"))  # nopep8
    CONVERSIONS["MMHG:CENTIPASCAL"] = \
        Mul(Rat(Mul(Int(2), Pow(10, 6)), Int("26664477483")), Sym("mmhg"))  # nopep8
    CONVERSIONS["MMHG:DECAPASCAL"] = \
        Mul(Rat(Mul(Int(2), Pow(10, 9)), Int("26664477483")), Sym("mmhg"))  # nopep8
    CONVERSIONS["MMHG:DECIBAR"] = \
        Mul(Rat(Mul(Int(2), Pow(10, 12)), Int("26664477483")), Sym("mmhg"))  # nopep8
    CONVERSIONS["MMHG:DECIPASCAL"] = \
        Mul(Rat(Mul(Int(2), Pow(10, 7)), Int("26664477483")), Sym("mmhg"))  # nopep8
    CONVERSIONS["MMHG:EXAPASCAL"] = \
        Mul(Rat(Mul(Int(2), Pow(10, 26)), Int("26664477483")), Sym("mmhg"))  # nopep8
    CONVERSIONS["MMHG:FEMTOPASCAL"] = \
        Mul(Rat(Int(1), Mul(Int("133322387415"), Pow(10, 6))), Sym("mmhg"))  # nopep8
    CONVERSIONS["MMHG:GIGAPASCAL"] = \
        Mul(Rat(Mul(Int(2), Pow(10, 17)), Int("26664477483")), Sym("mmhg"))  # nopep8
    CONVERSIONS["MMHG:HECTOPASCAL"] = \
        Mul(Rat(Mul(Int(2), Pow(10, 10)), Int("26664477483")), Sym("mmhg"))  # nopep8
    CONVERSIONS["MMHG:KILOBAR"] = \
        Mul(Rat(Mul(Int(2), Pow(10, 16)), Int("26664477483")), Sym("mmhg"))  # nopep8
    CONVERSIONS["MMHG:KILOPASCAL"] = \
        Mul(Rat(Mul(Int(2), Pow(10, 11)), Int("26664477483")), Sym("mmhg"))  # nopep8
    CONVERSIONS["MMHG:MEGABAR"] = \
        Mul(Rat(Mul(Int(2), Pow(10, 19)), Int("26664477483")), Sym("mmhg"))  # nopep8
    CONVERSIONS["MMHG:MEGAPASCAL"] = \
        Mul(Rat(Mul(Int(2), Pow(10, 14)), Int("26664477483")), Sym("mmhg"))  # nopep8
    CONVERSIONS["MMHG:MICROPASCAL"] = \
        Mul(Rat(Int(200), Int("26664477483")), Sym("mmhg"))  # nopep8
    CONVERSIONS["MMHG:MILLIBAR"] = \
        Mul(Rat(Mul(Int(2), Pow(10, 10)), Int("26664477483")), Sym("mmhg"))  # nopep8
    CONVERSIONS["MMHG:MILLIPASCAL"] = \
        Mul(Rat(Mul(Int(2), Pow(10, 5)), Int("26664477483")), Sym("mmhg"))  # nopep8
    CONVERSIONS["MMHG:MILLITORR"] = \
        Mul(Rat(Int(24125000), Int("24125003437")), Sym("mmhg"))  # nopep8
    CONVERSIONS["MMHG:NANOPASCAL"] = \
        Mul(Rat(Int(1), Int("133322387415")), Sym("mmhg"))  # nopep8
    CONVERSIONS["MMHG:PASCAL"] = \
        Mul(Rat(Mul(Int(2), Pow(10, 8)), Int("26664477483")), Sym("mmhg"))  # nopep8
    CONVERSIONS["MMHG:PETAPASCAL"] = \
        Mul(Rat(Mul(Int(2), Pow(10, 23)), Int("26664477483")), Sym("mmhg"))  # nopep8
    CONVERSIONS["MMHG:PICOPASCAL"] = \
        Mul(Rat(Int(1), Int("133322387415000")), Sym("mmhg"))  # nopep8
    CONVERSIONS["MMHG:PSI"] = \
        Mul(Rat(Int("8208044396629"), Int("158717127875")), Sym("mmhg"))  # nopep8
    CONVERSIONS["MMHG:TERAPASCAL"] = \
        Mul(Rat(Mul(Int(2), Pow(10, 20)), Int("26664477483")), Sym("mmhg"))  # nopep8
    CONVERSIONS["MMHG:TORR"] = \
        Mul(Rat(Mul(Int(24125), Pow(10, 6)), Int("24125003437")), Sym("mmhg"))  # nopep8
    CONVERSIONS["MMHG:YOCTOPASCAL"] = \
        Mul(Rat(Int(1), Mul(Int("133322387415"), Pow(10, 15))), Sym("mmhg"))  # nopep8
    CONVERSIONS["MMHG:YOTTAPASCAL"] = \
        Mul(Rat(Mul(Int(2), Pow(10, 32)), Int("26664477483")), Sym("mmhg"))  # nopep8
    CONVERSIONS["MMHG:ZEPTOPASCAL"] = \
        Mul(Rat(Int(1), Mul(Int("133322387415"), Pow(10, 12))), Sym("mmhg"))  # nopep8
    CONVERSIONS["MMHG:ZETTAPASCAL"] = \
        Mul(Rat(Mul(Int(2), Pow(10, 29)), Int("26664477483")), Sym("mmhg"))  # nopep8
    CONVERSIONS["NANOPASCAL:ATHMOSPHERE"] = \
        Mul(Mul(Int(101325), Pow(10, 9)), Sym("nanopa"))  # nopep8
    CONVERSIONS["NANOPASCAL:ATTOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("nanopa"))  # nopep8
    CONVERSIONS["NANOPASCAL:BAR"] = \
        Mul(Pow(10, 14), Sym("nanopa"))  # nopep8
    CONVERSIONS["NANOPASCAL:CENTIBAR"] = \
        Mul(Pow(10, 12), Sym("nanopa"))  # nopep8
    CONVERSIONS["NANOPASCAL:CENTIPASCAL"] = \
        Mul(Pow(10, 7), Sym("nanopa"))  # nopep8
    CONVERSIONS["NANOPASCAL:DECAPASCAL"] = \
        Mul(Pow(10, 10), Sym("nanopa"))  # nopep8
    CONVERSIONS["NANOPASCAL:DECIBAR"] = \
        Mul(Pow(10, 13), Sym("nanopa"))  # nopep8
    CONVERSIONS["NANOPASCAL:DECIPASCAL"] = \
        Mul(Pow(10, 8), Sym("nanopa"))  # nopep8
    CONVERSIONS["NANOPASCAL:EXAPASCAL"] = \
        Mul(Pow(10, 27), Sym("nanopa"))  # nopep8
    CONVERSIONS["NANOPASCAL:FEMTOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("nanopa"))  # nopep8
    CONVERSIONS["NANOPASCAL:GIGAPASCAL"] = \
        Mul(Pow(10, 18), Sym("nanopa"))  # nopep8
    CONVERSIONS["NANOPASCAL:HECTOPASCAL"] = \
        Mul(Pow(10, 11), Sym("nanopa"))  # nopep8
    CONVERSIONS["NANOPASCAL:KILOBAR"] = \
        Mul(Pow(10, 17), Sym("nanopa"))  # nopep8
    CONVERSIONS["NANOPASCAL:KILOPASCAL"] = \
        Mul(Pow(10, 12), Sym("nanopa"))  # nopep8
    CONVERSIONS["NANOPASCAL:MEGABAR"] = \
        Mul(Pow(10, 20), Sym("nanopa"))  # nopep8
    CONVERSIONS["NANOPASCAL:MEGAPASCAL"] = \
        Mul(Pow(10, 15), Sym("nanopa"))  # nopep8
    CONVERSIONS["NANOPASCAL:MICROPASCAL"] = \
        Mul(Int(1000), Sym("nanopa"))  # nopep8
    CONVERSIONS["NANOPASCAL:MILLIBAR"] = \
        Mul(Pow(10, 11), Sym("nanopa"))  # nopep8
    CONVERSIONS["NANOPASCAL:MILLIPASCAL"] = \
        Mul(Pow(10, 6), Sym("nanopa"))  # nopep8
    CONVERSIONS["NANOPASCAL:MILLITORR"] = \
        Mul(Rat(Int("2533125000"), Int(19)), Sym("nanopa"))  # nopep8
    CONVERSIONS["NANOPASCAL:MMHG"] = \
        Mul(Int("133322387415"), Sym("nanopa"))  # nopep8
    CONVERSIONS["NANOPASCAL:PASCAL"] = \
        Mul(Pow(10, 9), Sym("nanopa"))  # nopep8
    CONVERSIONS["NANOPASCAL:PETAPASCAL"] = \
        Mul(Pow(10, 24), Sym("nanopa"))  # nopep8
    CONVERSIONS["NANOPASCAL:PICOPASCAL"] = \
        Mul(Rat(Int(1), Int(1000)), Sym("nanopa"))  # nopep8
    CONVERSIONS["NANOPASCAL:PSI"] = \
        Mul(Rat(Int("172368932329209"), Int(25)), Sym("nanopa"))  # nopep8
    CONVERSIONS["NANOPASCAL:TERAPASCAL"] = \
        Mul(Pow(10, 21), Sym("nanopa"))  # nopep8
    CONVERSIONS["NANOPASCAL:TORR"] = \
        Mul(Rat(Mul(Int(2533125), Pow(10, 6)), Int(19)), Sym("nanopa"))  # nopep8
    CONVERSIONS["NANOPASCAL:YOCTOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 15)), Sym("nanopa"))  # nopep8
    CONVERSIONS["NANOPASCAL:YOTTAPASCAL"] = \
        Mul(Pow(10, 33), Sym("nanopa"))  # nopep8
    CONVERSIONS["NANOPASCAL:ZEPTOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 12)), Sym("nanopa"))  # nopep8
    CONVERSIONS["NANOPASCAL:ZETTAPASCAL"] = \
        Mul(Pow(10, 30), Sym("nanopa"))  # nopep8
    CONVERSIONS["PASCAL:ATHMOSPHERE"] = \
        Mul(Int(101325), Sym("pa"))  # nopep8
    CONVERSIONS["PASCAL:ATTOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 18)), Sym("pa"))  # nopep8
    CONVERSIONS["PASCAL:BAR"] = \
        Mul(Pow(10, 5), Sym("pa"))  # nopep8
    CONVERSIONS["PASCAL:CENTIBAR"] = \
        Mul(Int(1000), Sym("pa"))  # nopep8
    CONVERSIONS["PASCAL:CENTIPASCAL"] = \
        Mul(Rat(Int(1), Int(100)), Sym("pa"))  # nopep8
    CONVERSIONS["PASCAL:DECAPASCAL"] = \
        Mul(Int(10), Sym("pa"))  # nopep8
    CONVERSIONS["PASCAL:DECIBAR"] = \
        Mul(Pow(10, 4), Sym("pa"))  # nopep8
    CONVERSIONS["PASCAL:DECIPASCAL"] = \
        Mul(Rat(Int(1), Int(10)), Sym("pa"))  # nopep8
    CONVERSIONS["PASCAL:EXAPASCAL"] = \
        Mul(Pow(10, 18), Sym("pa"))  # nopep8
    CONVERSIONS["PASCAL:FEMTOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 15)), Sym("pa"))  # nopep8
    CONVERSIONS["PASCAL:GIGAPASCAL"] = \
        Mul(Pow(10, 9), Sym("pa"))  # nopep8
    CONVERSIONS["PASCAL:HECTOPASCAL"] = \
        Mul(Int(100), Sym("pa"))  # nopep8
    CONVERSIONS["PASCAL:KILOBAR"] = \
        Mul(Pow(10, 8), Sym("pa"))  # nopep8
    CONVERSIONS["PASCAL:KILOPASCAL"] = \
        Mul(Int(1000), Sym("pa"))  # nopep8
    CONVERSIONS["PASCAL:MEGABAR"] = \
        Mul(Pow(10, 11), Sym("pa"))  # nopep8
    CONVERSIONS["PASCAL:MEGAPASCAL"] = \
        Mul(Pow(10, 6), Sym("pa"))  # nopep8
    CONVERSIONS["PASCAL:MICROPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("pa"))  # nopep8
    CONVERSIONS["PASCAL:MILLIBAR"] = \
        Mul(Int(100), Sym("pa"))  # nopep8
    CONVERSIONS["PASCAL:MILLIPASCAL"] = \
        Mul(Rat(Int(1), Int(1000)), Sym("pa"))  # nopep8
    CONVERSIONS["PASCAL:MILLITORR"] = \
        Mul(Rat(Int(4053), Int(30400)), Sym("pa"))  # nopep8
    CONVERSIONS["PASCAL:MMHG"] = \
        Mul(Rat(Int("26664477483"), Mul(Int(2), Pow(10, 8))), Sym("pa"))  # nopep8
    CONVERSIONS["PASCAL:NANOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("pa"))  # nopep8
    CONVERSIONS["PASCAL:PETAPASCAL"] = \
        Mul(Pow(10, 15), Sym("pa"))  # nopep8
    CONVERSIONS["PASCAL:PICOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 12)), Sym("pa"))  # nopep8
    CONVERSIONS["PASCAL:PSI"] = \
        Mul(Rat(Int("172368932329209"), Mul(Int(25), Pow(10, 9))), Sym("pa"))  # nopep8
    CONVERSIONS["PASCAL:TERAPASCAL"] = \
        Mul(Pow(10, 12), Sym("pa"))  # nopep8
    CONVERSIONS["PASCAL:TORR"] = \
        Mul(Rat(Int(20265), Int(152)), Sym("pa"))  # nopep8
    CONVERSIONS["PASCAL:YOCTOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 24)), Sym("pa"))  # nopep8
    CONVERSIONS["PASCAL:YOTTAPASCAL"] = \
        Mul(Pow(10, 24), Sym("pa"))  # nopep8
    CONVERSIONS["PASCAL:ZEPTOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 21)), Sym("pa"))  # nopep8
    CONVERSIONS["PASCAL:ZETTAPASCAL"] = \
        Mul(Pow(10, 21), Sym("pa"))  # nopep8
    CONVERSIONS["PETAPASCAL:ATHMOSPHERE"] = \
        Mul(Rat(Int(4053), Mul(Int(4), Pow(10, 13))), Sym("petapa"))  # nopep8
    CONVERSIONS["PETAPASCAL:ATTOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 33)), Sym("petapa"))  # nopep8
    CONVERSIONS["PETAPASCAL:BAR"] = \
        Mul(Rat(Int(1), Pow(10, 10)), Sym("petapa"))  # nopep8
    CONVERSIONS["PETAPASCAL:CENTIBAR"] = \
        Mul(Rat(Int(1), Pow(10, 12)), Sym("petapa"))  # nopep8
    CONVERSIONS["PETAPASCAL:CENTIPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 17)), Sym("petapa"))  # nopep8
    CONVERSIONS["PETAPASCAL:DECAPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 14)), Sym("petapa"))  # nopep8
    CONVERSIONS["PETAPASCAL:DECIBAR"] = \
        Mul(Rat(Int(1), Pow(10, 11)), Sym("petapa"))  # nopep8
    CONVERSIONS["PETAPASCAL:DECIPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 16)), Sym("petapa"))  # nopep8
    CONVERSIONS["PETAPASCAL:EXAPASCAL"] = \
        Mul(Int(1000), Sym("petapa"))  # nopep8
    CONVERSIONS["PETAPASCAL:FEMTOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 30)), Sym("petapa"))  # nopep8
    CONVERSIONS["PETAPASCAL:GIGAPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("petapa"))  # nopep8
    CONVERSIONS["PETAPASCAL:HECTOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 13)), Sym("petapa"))  # nopep8
    CONVERSIONS["PETAPASCAL:KILOBAR"] = \
        Mul(Rat(Int(1), Pow(10, 7)), Sym("petapa"))  # nopep8
    CONVERSIONS["PETAPASCAL:KILOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 12)), Sym("petapa"))  # nopep8
    CONVERSIONS["PETAPASCAL:MEGABAR"] = \
        Mul(Rat(Int(1), Pow(10, 4)), Sym("petapa"))  # nopep8
    CONVERSIONS["PETAPASCAL:MEGAPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("petapa"))  # nopep8
    CONVERSIONS["PETAPASCAL:MICROPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 21)), Sym("petapa"))  # nopep8
    CONVERSIONS["PETAPASCAL:MILLIBAR"] = \
        Mul(Rat(Int(1), Pow(10, 13)), Sym("petapa"))  # nopep8
    CONVERSIONS["PETAPASCAL:MILLIPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 18)), Sym("petapa"))  # nopep8
    CONVERSIONS["PETAPASCAL:MILLITORR"] = \
        Mul(Rat(Int(4053), Mul(Int(304), Pow(10, 17))), Sym("petapa"))  # nopep8
    CONVERSIONS["PETAPASCAL:MMHG"] = \
        Mul(Rat(Int("26664477483"), Mul(Int(2), Pow(10, 23))), Sym("petapa"))  # nopep8
    CONVERSIONS["PETAPASCAL:NANOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 24)), Sym("petapa"))  # nopep8
    CONVERSIONS["PETAPASCAL:PASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 15)), Sym("petapa"))  # nopep8
    CONVERSIONS["PETAPASCAL:PICOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 27)), Sym("petapa"))  # nopep8
    CONVERSIONS["PETAPASCAL:PSI"] = \
        Mul(Rat(Int("172368932329209"), Mul(Int(25), Pow(10, 24))), Sym("petapa"))  # nopep8
    CONVERSIONS["PETAPASCAL:TERAPASCAL"] = \
        Mul(Rat(Int(1), Int(1000)), Sym("petapa"))  # nopep8
    CONVERSIONS["PETAPASCAL:TORR"] = \
        Mul(Rat(Int(4053), Mul(Int(304), Pow(10, 14))), Sym("petapa"))  # nopep8
    CONVERSIONS["PETAPASCAL:YOCTOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 39)), Sym("petapa"))  # nopep8
    CONVERSIONS["PETAPASCAL:YOTTAPASCAL"] = \
        Mul(Pow(10, 9), Sym("petapa"))  # nopep8
    CONVERSIONS["PETAPASCAL:ZEPTOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 36)), Sym("petapa"))  # nopep8
    CONVERSIONS["PETAPASCAL:ZETTAPASCAL"] = \
        Mul(Pow(10, 6), Sym("petapa"))  # nopep8
    CONVERSIONS["PICOPASCAL:ATHMOSPHERE"] = \
        Mul(Mul(Int(101325), Pow(10, 12)), Sym("picopa"))  # nopep8
    CONVERSIONS["PICOPASCAL:ATTOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("picopa"))  # nopep8
    CONVERSIONS["PICOPASCAL:BAR"] = \
        Mul(Pow(10, 17), Sym("picopa"))  # nopep8
    CONVERSIONS["PICOPASCAL:CENTIBAR"] = \
        Mul(Pow(10, 15), Sym("picopa"))  # nopep8
    CONVERSIONS["PICOPASCAL:CENTIPASCAL"] = \
        Mul(Pow(10, 10), Sym("picopa"))  # nopep8
    CONVERSIONS["PICOPASCAL:DECAPASCAL"] = \
        Mul(Pow(10, 13), Sym("picopa"))  # nopep8
    CONVERSIONS["PICOPASCAL:DECIBAR"] = \
        Mul(Pow(10, 16), Sym("picopa"))  # nopep8
    CONVERSIONS["PICOPASCAL:DECIPASCAL"] = \
        Mul(Pow(10, 11), Sym("picopa"))  # nopep8
    CONVERSIONS["PICOPASCAL:EXAPASCAL"] = \
        Mul(Pow(10, 30), Sym("picopa"))  # nopep8
    CONVERSIONS["PICOPASCAL:FEMTOPASCAL"] = \
        Mul(Rat(Int(1), Int(1000)), Sym("picopa"))  # nopep8
    CONVERSIONS["PICOPASCAL:GIGAPASCAL"] = \
        Mul(Pow(10, 21), Sym("picopa"))  # nopep8
    CONVERSIONS["PICOPASCAL:HECTOPASCAL"] = \
        Mul(Pow(10, 14), Sym("picopa"))  # nopep8
    CONVERSIONS["PICOPASCAL:KILOBAR"] = \
        Mul(Pow(10, 20), Sym("picopa"))  # nopep8
    CONVERSIONS["PICOPASCAL:KILOPASCAL"] = \
        Mul(Pow(10, 15), Sym("picopa"))  # nopep8
    CONVERSIONS["PICOPASCAL:MEGABAR"] = \
        Mul(Pow(10, 23), Sym("picopa"))  # nopep8
    CONVERSIONS["PICOPASCAL:MEGAPASCAL"] = \
        Mul(Pow(10, 18), Sym("picopa"))  # nopep8
    CONVERSIONS["PICOPASCAL:MICROPASCAL"] = \
        Mul(Pow(10, 6), Sym("picopa"))  # nopep8
    CONVERSIONS["PICOPASCAL:MILLIBAR"] = \
        Mul(Pow(10, 14), Sym("picopa"))  # nopep8
    CONVERSIONS["PICOPASCAL:MILLIPASCAL"] = \
        Mul(Pow(10, 9), Sym("picopa"))  # nopep8
    CONVERSIONS["PICOPASCAL:MILLITORR"] = \
        Mul(Rat(Mul(Int(2533125), Pow(10, 6)), Int(19)), Sym("picopa"))  # nopep8
    CONVERSIONS["PICOPASCAL:MMHG"] = \
        Mul(Int("133322387415000"), Sym("picopa"))  # nopep8
    CONVERSIONS["PICOPASCAL:NANOPASCAL"] = \
        Mul(Int(1000), Sym("picopa"))  # nopep8
    CONVERSIONS["PICOPASCAL:PASCAL"] = \
        Mul(Pow(10, 12), Sym("picopa"))  # nopep8
    CONVERSIONS["PICOPASCAL:PETAPASCAL"] = \
        Mul(Pow(10, 27), Sym("picopa"))  # nopep8
    CONVERSIONS["PICOPASCAL:PSI"] = \
        Mul(Int("6894757293168360"), Sym("picopa"))  # nopep8
    CONVERSIONS["PICOPASCAL:TERAPASCAL"] = \
        Mul(Pow(10, 24), Sym("picopa"))  # nopep8
    CONVERSIONS["PICOPASCAL:TORR"] = \
        Mul(Rat(Mul(Int(2533125), Pow(10, 9)), Int(19)), Sym("picopa"))  # nopep8
    CONVERSIONS["PICOPASCAL:YOCTOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 12)), Sym("picopa"))  # nopep8
    CONVERSIONS["PICOPASCAL:YOTTAPASCAL"] = \
        Mul(Pow(10, 36), Sym("picopa"))  # nopep8
    CONVERSIONS["PICOPASCAL:ZEPTOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("picopa"))  # nopep8
    CONVERSIONS["PICOPASCAL:ZETTAPASCAL"] = \
        Mul(Pow(10, 33), Sym("picopa"))  # nopep8
    CONVERSIONS["PSI:ATHMOSPHERE"] = \
        Mul(Rat(Mul(Int(120625), Pow(10, 9)), Int("8208044396629")), Sym("psi"))  # nopep8
    CONVERSIONS["PSI:ATTOPASCAL"] = \
        Mul(Rat(Int(1), Mul(Int("689475729316836"), Pow(10, 7))), Sym("psi"))  # nopep8
    CONVERSIONS["PSI:BAR"] = \
        Mul(Rat(Mul(Int(25), Pow(10, 14)), Int("172368932329209")), Sym("psi"))  # nopep8
    CONVERSIONS["PSI:CENTIBAR"] = \
        Mul(Rat(Mul(Int(25), Pow(10, 12)), Int("172368932329209")), Sym("psi"))  # nopep8
    CONVERSIONS["PSI:CENTIPASCAL"] = \
        Mul(Rat(Mul(Int(25), Pow(10, 7)), Int("172368932329209")), Sym("psi"))  # nopep8
    CONVERSIONS["PSI:DECAPASCAL"] = \
        Mul(Rat(Mul(Int(25), Pow(10, 10)), Int("172368932329209")), Sym("psi"))  # nopep8
    CONVERSIONS["PSI:DECIBAR"] = \
        Mul(Rat(Mul(Int(25), Pow(10, 13)), Int("172368932329209")), Sym("psi"))  # nopep8
    CONVERSIONS["PSI:DECIPASCAL"] = \
        Mul(Rat(Mul(Int(25), Pow(10, 8)), Int("172368932329209")), Sym("psi"))  # nopep8
    CONVERSIONS["PSI:EXAPASCAL"] = \
        Mul(Rat(Mul(Int(25), Pow(10, 27)), Int("172368932329209")), Sym("psi"))  # nopep8
    CONVERSIONS["PSI:FEMTOPASCAL"] = \
        Mul(Rat(Int(1), Mul(Int("689475729316836"), Pow(10, 4))), Sym("psi"))  # nopep8
    CONVERSIONS["PSI:GIGAPASCAL"] = \
        Mul(Rat(Mul(Int(25), Pow(10, 18)), Int("172368932329209")), Sym("psi"))  # nopep8
    CONVERSIONS["PSI:HECTOPASCAL"] = \
        Mul(Rat(Mul(Int(25), Pow(10, 11)), Int("172368932329209")), Sym("psi"))  # nopep8
    CONVERSIONS["PSI:KILOBAR"] = \
        Mul(Rat(Mul(Int(25), Pow(10, 17)), Int("172368932329209")), Sym("psi"))  # nopep8
    CONVERSIONS["PSI:KILOPASCAL"] = \
        Mul(Rat(Mul(Int(25), Pow(10, 12)), Int("172368932329209")), Sym("psi"))  # nopep8
    CONVERSIONS["PSI:MEGABAR"] = \
        Mul(Rat(Mul(Int(25), Pow(10, 20)), Int("172368932329209")), Sym("psi"))  # nopep8
    CONVERSIONS["PSI:MEGAPASCAL"] = \
        Mul(Rat(Mul(Int(25), Pow(10, 15)), Int("172368932329209")), Sym("psi"))  # nopep8
    CONVERSIONS["PSI:MICROPASCAL"] = \
        Mul(Rat(Int(25000), Int("172368932329209")), Sym("psi"))  # nopep8
    CONVERSIONS["PSI:MILLIBAR"] = \
        Mul(Rat(Mul(Int(25), Pow(10, 11)), Int("172368932329209")), Sym("psi"))  # nopep8
    CONVERSIONS["PSI:MILLIPASCAL"] = \
        Mul(Rat(Mul(Int(25), Pow(10, 6)), Int("172368932329209")), Sym("psi"))  # nopep8
    CONVERSIONS["PSI:MILLITORR"] = \
        Mul(Rat(Int("3015625000"), Int("155952843535951")), Sym("psi"))  # nopep8
    CONVERSIONS["PSI:MMHG"] = \
        Mul(Rat(Int("158717127875"), Int("8208044396629")), Sym("psi"))  # nopep8
    CONVERSIONS["PSI:NANOPASCAL"] = \
        Mul(Rat(Int(25), Int("172368932329209")), Sym("psi"))  # nopep8
    CONVERSIONS["PSI:PASCAL"] = \
        Mul(Rat(Mul(Int(25), Pow(10, 9)), Int("172368932329209")), Sym("psi"))  # nopep8
    CONVERSIONS["PSI:PETAPASCAL"] = \
        Mul(Rat(Mul(Int(25), Pow(10, 24)), Int("172368932329209")), Sym("psi"))  # nopep8
    CONVERSIONS["PSI:PICOPASCAL"] = \
        Mul(Rat(Int(1), Int("6894757293168360")), Sym("psi"))  # nopep8
    CONVERSIONS["PSI:TERAPASCAL"] = \
        Mul(Rat(Mul(Int(25), Pow(10, 21)), Int("172368932329209")), Sym("psi"))  # nopep8
    CONVERSIONS["PSI:TORR"] = \
        Mul(Rat(Mul(Int(3015625), Pow(10, 6)), Int("155952843535951")), Sym("psi"))  # nopep8
    CONVERSIONS["PSI:YOCTOPASCAL"] = \
        Mul(Rat(Int(1), Mul(Int("689475729316836"), Pow(10, 13))), Sym("psi"))  # nopep8
    CONVERSIONS["PSI:YOTTAPASCAL"] = \
        Mul(Rat(Mul(Int(25), Pow(10, 33)), Int("172368932329209")), Sym("psi"))  # nopep8
    CONVERSIONS["PSI:ZEPTOPASCAL"] = \
        Mul(Rat(Int(1), Mul(Int("689475729316836"), Pow(10, 10))), Sym("psi"))  # nopep8
    CONVERSIONS["PSI:ZETTAPASCAL"] = \
        Mul(Rat(Mul(Int(25), Pow(10, 30)), Int("172368932329209")), Sym("psi"))  # nopep8
    CONVERSIONS["TERAPASCAL:ATHMOSPHERE"] = \
        Mul(Rat(Int(4053), Mul(Int(4), Pow(10, 10))), Sym("terapa"))  # nopep8
    CONVERSIONS["TERAPASCAL:ATTOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 30)), Sym("terapa"))  # nopep8
    CONVERSIONS["TERAPASCAL:BAR"] = \
        Mul(Rat(Int(1), Pow(10, 7)), Sym("terapa"))  # nopep8
    CONVERSIONS["TERAPASCAL:CENTIBAR"] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("terapa"))  # nopep8
    CONVERSIONS["TERAPASCAL:CENTIPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 14)), Sym("terapa"))  # nopep8
    CONVERSIONS["TERAPASCAL:DECAPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 11)), Sym("terapa"))  # nopep8
    CONVERSIONS["TERAPASCAL:DECIBAR"] = \
        Mul(Rat(Int(1), Pow(10, 8)), Sym("terapa"))  # nopep8
    CONVERSIONS["TERAPASCAL:DECIPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 13)), Sym("terapa"))  # nopep8
    CONVERSIONS["TERAPASCAL:EXAPASCAL"] = \
        Mul(Pow(10, 6), Sym("terapa"))  # nopep8
    CONVERSIONS["TERAPASCAL:FEMTOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 27)), Sym("terapa"))  # nopep8
    CONVERSIONS["TERAPASCAL:GIGAPASCAL"] = \
        Mul(Rat(Int(1), Int(1000)), Sym("terapa"))  # nopep8
    CONVERSIONS["TERAPASCAL:HECTOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 10)), Sym("terapa"))  # nopep8
    CONVERSIONS["TERAPASCAL:KILOBAR"] = \
        Mul(Rat(Int(1), Pow(10, 4)), Sym("terapa"))  # nopep8
    CONVERSIONS["TERAPASCAL:KILOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("terapa"))  # nopep8
    CONVERSIONS["TERAPASCAL:MEGABAR"] = \
        Mul(Rat(Int(1), Int(10)), Sym("terapa"))  # nopep8
    CONVERSIONS["TERAPASCAL:MEGAPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("terapa"))  # nopep8
    CONVERSIONS["TERAPASCAL:MICROPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 18)), Sym("terapa"))  # nopep8
    CONVERSIONS["TERAPASCAL:MILLIBAR"] = \
        Mul(Rat(Int(1), Pow(10, 10)), Sym("terapa"))  # nopep8
    CONVERSIONS["TERAPASCAL:MILLIPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 15)), Sym("terapa"))  # nopep8
    CONVERSIONS["TERAPASCAL:MILLITORR"] = \
        Mul(Rat(Int(4053), Mul(Int(304), Pow(10, 14))), Sym("terapa"))  # nopep8
    CONVERSIONS["TERAPASCAL:MMHG"] = \
        Mul(Rat(Int("26664477483"), Mul(Int(2), Pow(10, 20))), Sym("terapa"))  # nopep8
    CONVERSIONS["TERAPASCAL:NANOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 21)), Sym("terapa"))  # nopep8
    CONVERSIONS["TERAPASCAL:PASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 12)), Sym("terapa"))  # nopep8
    CONVERSIONS["TERAPASCAL:PETAPASCAL"] = \
        Mul(Int(1000), Sym("terapa"))  # nopep8
    CONVERSIONS["TERAPASCAL:PICOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 24)), Sym("terapa"))  # nopep8
    CONVERSIONS["TERAPASCAL:PSI"] = \
        Mul(Rat(Int("172368932329209"), Mul(Int(25), Pow(10, 21))), Sym("terapa"))  # nopep8
    CONVERSIONS["TERAPASCAL:TORR"] = \
        Mul(Rat(Int(4053), Mul(Int(304), Pow(10, 11))), Sym("terapa"))  # nopep8
    CONVERSIONS["TERAPASCAL:YOCTOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 36)), Sym("terapa"))  # nopep8
    CONVERSIONS["TERAPASCAL:YOTTAPASCAL"] = \
        Mul(Pow(10, 12), Sym("terapa"))  # nopep8
    CONVERSIONS["TERAPASCAL:ZEPTOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 33)), Sym("terapa"))  # nopep8
    CONVERSIONS["TERAPASCAL:ZETTAPASCAL"] = \
        Mul(Pow(10, 9), Sym("terapa"))  # nopep8
    CONVERSIONS["TORR:ATHMOSPHERE"] = \
        Mul(Int(760), Sym("torr"))  # nopep8
    CONVERSIONS["TORR:ATTOPASCAL"] = \
        Mul(Rat(Int(19), Mul(Int(2533125), Pow(10, 15))), Sym("torr"))  # nopep8
    CONVERSIONS["TORR:BAR"] = \
        Mul(Rat(Mul(Int(304), Pow(10, 4)), Int(4053)), Sym("torr"))  # nopep8
    CONVERSIONS["TORR:CENTIBAR"] = \
        Mul(Rat(Int(30400), Int(4053)), Sym("torr"))  # nopep8
    CONVERSIONS["TORR:CENTIPASCAL"] = \
        Mul(Rat(Int(38), Int(506625)), Sym("torr"))  # nopep8
    CONVERSIONS["TORR:DECAPASCAL"] = \
        Mul(Rat(Int(304), Int(4053)), Sym("torr"))  # nopep8
    CONVERSIONS["TORR:DECIBAR"] = \
        Mul(Rat(Int(304000), Int(4053)), Sym("torr"))  # nopep8
    CONVERSIONS["TORR:DECIPASCAL"] = \
        Mul(Rat(Int(76), Int(101325)), Sym("torr"))  # nopep8
    CONVERSIONS["TORR:EXAPASCAL"] = \
        Mul(Rat(Mul(Int(304), Pow(10, 17)), Int(4053)), Sym("torr"))  # nopep8
    CONVERSIONS["TORR:FEMTOPASCAL"] = \
        Mul(Rat(Int(19), Mul(Int(2533125), Pow(10, 12))), Sym("torr"))  # nopep8
    CONVERSIONS["TORR:GIGAPASCAL"] = \
        Mul(Rat(Mul(Int(304), Pow(10, 8)), Int(4053)), Sym("torr"))  # nopep8
    CONVERSIONS["TORR:HECTOPASCAL"] = \
        Mul(Rat(Int(3040), Int(4053)), Sym("torr"))  # nopep8
    CONVERSIONS["TORR:KILOBAR"] = \
        Mul(Rat(Mul(Int(304), Pow(10, 7)), Int(4053)), Sym("torr"))  # nopep8
    CONVERSIONS["TORR:KILOPASCAL"] = \
        Mul(Rat(Int(30400), Int(4053)), Sym("torr"))  # nopep8
    CONVERSIONS["TORR:MEGABAR"] = \
        Mul(Rat(Mul(Int(304), Pow(10, 10)), Int(4053)), Sym("torr"))  # nopep8
    CONVERSIONS["TORR:MEGAPASCAL"] = \
        Mul(Rat(Mul(Int(304), Pow(10, 5)), Int(4053)), Sym("torr"))  # nopep8
    CONVERSIONS["TORR:MICROPASCAL"] = \
        Mul(Rat(Int(19), Int("2533125000")), Sym("torr"))  # nopep8
    CONVERSIONS["TORR:MILLIBAR"] = \
        Mul(Rat(Int(3040), Int(4053)), Sym("torr"))  # nopep8
    CONVERSIONS["TORR:MILLIPASCAL"] = \
        Mul(Rat(Int(19), Int(2533125)), Sym("torr"))  # nopep8
    CONVERSIONS["TORR:MILLITORR"] = \
        Mul(Rat(Int(1), Int(1000)), Sym("torr"))  # nopep8
    CONVERSIONS["TORR:MMHG"] = \
        Mul(Rat(Int("24125003437"), Mul(Int(24125), Pow(10, 6))), Sym("torr"))  # nopep8
    CONVERSIONS["TORR:NANOPASCAL"] = \
        Mul(Rat(Int(19), Mul(Int(2533125), Pow(10, 6))), Sym("torr"))  # nopep8
    CONVERSIONS["TORR:PASCAL"] = \
        Mul(Rat(Int(152), Int(20265)), Sym("torr"))  # nopep8
    CONVERSIONS["TORR:PETAPASCAL"] = \
        Mul(Rat(Mul(Int(304), Pow(10, 14)), Int(4053)), Sym("torr"))  # nopep8
    CONVERSIONS["TORR:PICOPASCAL"] = \
        Mul(Rat(Int(19), Mul(Int(2533125), Pow(10, 9))), Sym("torr"))  # nopep8
    CONVERSIONS["TORR:PSI"] = \
        Mul(Rat(Int("155952843535951"), Mul(Int(3015625), Pow(10, 6))), Sym("torr"))  # nopep8
    CONVERSIONS["TORR:TERAPASCAL"] = \
        Mul(Rat(Mul(Int(304), Pow(10, 11)), Int(4053)), Sym("torr"))  # nopep8
    CONVERSIONS["TORR:YOCTOPASCAL"] = \
        Mul(Rat(Int(19), Mul(Int(2533125), Pow(10, 21))), Sym("torr"))  # nopep8
    CONVERSIONS["TORR:YOTTAPASCAL"] = \
        Mul(Rat(Mul(Int(304), Pow(10, 23)), Int(4053)), Sym("torr"))  # nopep8
    CONVERSIONS["TORR:ZEPTOPASCAL"] = \
        Mul(Rat(Int(19), Mul(Int(2533125), Pow(10, 18))), Sym("torr"))  # nopep8
    CONVERSIONS["TORR:ZETTAPASCAL"] = \
        Mul(Rat(Mul(Int(304), Pow(10, 20)), Int(4053)), Sym("torr"))  # nopep8
    CONVERSIONS["YOCTOPASCAL:ATHMOSPHERE"] = \
        Mul(Mul(Int(101325), Pow(10, 24)), Sym("yoctopa"))  # nopep8
    CONVERSIONS["YOCTOPASCAL:ATTOPASCAL"] = \
        Mul(Pow(10, 6), Sym("yoctopa"))  # nopep8
    CONVERSIONS["YOCTOPASCAL:BAR"] = \
        Mul(Pow(10, 29), Sym("yoctopa"))  # nopep8
    CONVERSIONS["YOCTOPASCAL:CENTIBAR"] = \
        Mul(Pow(10, 27), Sym("yoctopa"))  # nopep8
    CONVERSIONS["YOCTOPASCAL:CENTIPASCAL"] = \
        Mul(Pow(10, 22), Sym("yoctopa"))  # nopep8
    CONVERSIONS["YOCTOPASCAL:DECAPASCAL"] = \
        Mul(Pow(10, 25), Sym("yoctopa"))  # nopep8
    CONVERSIONS["YOCTOPASCAL:DECIBAR"] = \
        Mul(Pow(10, 28), Sym("yoctopa"))  # nopep8
    CONVERSIONS["YOCTOPASCAL:DECIPASCAL"] = \
        Mul(Pow(10, 23), Sym("yoctopa"))  # nopep8
    CONVERSIONS["YOCTOPASCAL:EXAPASCAL"] = \
        Mul(Pow(10, 42), Sym("yoctopa"))  # nopep8
    CONVERSIONS["YOCTOPASCAL:FEMTOPASCAL"] = \
        Mul(Pow(10, 9), Sym("yoctopa"))  # nopep8
    CONVERSIONS["YOCTOPASCAL:GIGAPASCAL"] = \
        Mul(Pow(10, 33), Sym("yoctopa"))  # nopep8
    CONVERSIONS["YOCTOPASCAL:HECTOPASCAL"] = \
        Mul(Pow(10, 26), Sym("yoctopa"))  # nopep8
    CONVERSIONS["YOCTOPASCAL:KILOBAR"] = \
        Mul(Pow(10, 32), Sym("yoctopa"))  # nopep8
    CONVERSIONS["YOCTOPASCAL:KILOPASCAL"] = \
        Mul(Pow(10, 27), Sym("yoctopa"))  # nopep8
    CONVERSIONS["YOCTOPASCAL:MEGABAR"] = \
        Mul(Pow(10, 35), Sym("yoctopa"))  # nopep8
    CONVERSIONS["YOCTOPASCAL:MEGAPASCAL"] = \
        Mul(Pow(10, 30), Sym("yoctopa"))  # nopep8
    CONVERSIONS["YOCTOPASCAL:MICROPASCAL"] = \
        Mul(Pow(10, 18), Sym("yoctopa"))  # nopep8
    CONVERSIONS["YOCTOPASCAL:MILLIBAR"] = \
        Mul(Pow(10, 26), Sym("yoctopa"))  # nopep8
    CONVERSIONS["YOCTOPASCAL:MILLIPASCAL"] = \
        Mul(Pow(10, 21), Sym("yoctopa"))  # nopep8
    CONVERSIONS["YOCTOPASCAL:MILLITORR"] = \
        Mul(Rat(Mul(Int(2533125), Pow(10, 18)), Int(19)), Sym("yoctopa"))  # nopep8
    CONVERSIONS["YOCTOPASCAL:MMHG"] = \
        Mul(Mul(Int("133322387415"), Pow(10, 15)), Sym("yoctopa"))  # nopep8
    CONVERSIONS["YOCTOPASCAL:NANOPASCAL"] = \
        Mul(Pow(10, 15), Sym("yoctopa"))  # nopep8
    CONVERSIONS["YOCTOPASCAL:PASCAL"] = \
        Mul(Pow(10, 24), Sym("yoctopa"))  # nopep8
    CONVERSIONS["YOCTOPASCAL:PETAPASCAL"] = \
        Mul(Pow(10, 39), Sym("yoctopa"))  # nopep8
    CONVERSIONS["YOCTOPASCAL:PICOPASCAL"] = \
        Mul(Pow(10, 12), Sym("yoctopa"))  # nopep8
    CONVERSIONS["YOCTOPASCAL:PSI"] = \
        Mul(Mul(Int("689475729316836"), Pow(10, 13)), Sym("yoctopa"))  # nopep8
    CONVERSIONS["YOCTOPASCAL:TERAPASCAL"] = \
        Mul(Pow(10, 36), Sym("yoctopa"))  # nopep8
    CONVERSIONS["YOCTOPASCAL:TORR"] = \
        Mul(Rat(Mul(Int(2533125), Pow(10, 21)), Int(19)), Sym("yoctopa"))  # nopep8
    CONVERSIONS["YOCTOPASCAL:YOTTAPASCAL"] = \
        Mul(Pow(10, 48), Sym("yoctopa"))  # nopep8
    CONVERSIONS["YOCTOPASCAL:ZEPTOPASCAL"] = \
        Mul(Int(1000), Sym("yoctopa"))  # nopep8
    CONVERSIONS["YOCTOPASCAL:ZETTAPASCAL"] = \
        Mul(Pow(10, 45), Sym("yoctopa"))  # nopep8
    CONVERSIONS["YOTTAPASCAL:ATHMOSPHERE"] = \
        Mul(Rat(Int(4053), Mul(Int(4), Pow(10, 22))), Sym("yottapa"))  # nopep8
    CONVERSIONS["YOTTAPASCAL:ATTOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 42)), Sym("yottapa"))  # nopep8
    CONVERSIONS["YOTTAPASCAL:BAR"] = \
        Mul(Rat(Int(1), Pow(10, 19)), Sym("yottapa"))  # nopep8
    CONVERSIONS["YOTTAPASCAL:CENTIBAR"] = \
        Mul(Rat(Int(1), Pow(10, 21)), Sym("yottapa"))  # nopep8
    CONVERSIONS["YOTTAPASCAL:CENTIPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 26)), Sym("yottapa"))  # nopep8
    CONVERSIONS["YOTTAPASCAL:DECAPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 23)), Sym("yottapa"))  # nopep8
    CONVERSIONS["YOTTAPASCAL:DECIBAR"] = \
        Mul(Rat(Int(1), Pow(10, 20)), Sym("yottapa"))  # nopep8
    CONVERSIONS["YOTTAPASCAL:DECIPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 25)), Sym("yottapa"))  # nopep8
    CONVERSIONS["YOTTAPASCAL:EXAPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("yottapa"))  # nopep8
    CONVERSIONS["YOTTAPASCAL:FEMTOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 39)), Sym("yottapa"))  # nopep8
    CONVERSIONS["YOTTAPASCAL:GIGAPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 15)), Sym("yottapa"))  # nopep8
    CONVERSIONS["YOTTAPASCAL:HECTOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 22)), Sym("yottapa"))  # nopep8
    CONVERSIONS["YOTTAPASCAL:KILOBAR"] = \
        Mul(Rat(Int(1), Pow(10, 16)), Sym("yottapa"))  # nopep8
    CONVERSIONS["YOTTAPASCAL:KILOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 21)), Sym("yottapa"))  # nopep8
    CONVERSIONS["YOTTAPASCAL:MEGABAR"] = \
        Mul(Rat(Int(1), Pow(10, 13)), Sym("yottapa"))  # nopep8
    CONVERSIONS["YOTTAPASCAL:MEGAPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 18)), Sym("yottapa"))  # nopep8
    CONVERSIONS["YOTTAPASCAL:MICROPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 30)), Sym("yottapa"))  # nopep8
    CONVERSIONS["YOTTAPASCAL:MILLIBAR"] = \
        Mul(Rat(Int(1), Pow(10, 22)), Sym("yottapa"))  # nopep8
    CONVERSIONS["YOTTAPASCAL:MILLIPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 27)), Sym("yottapa"))  # nopep8
    CONVERSIONS["YOTTAPASCAL:MILLITORR"] = \
        Mul(Rat(Int(4053), Mul(Int(304), Pow(10, 26))), Sym("yottapa"))  # nopep8
    CONVERSIONS["YOTTAPASCAL:MMHG"] = \
        Mul(Rat(Int("26664477483"), Mul(Int(2), Pow(10, 32))), Sym("yottapa"))  # nopep8
    CONVERSIONS["YOTTAPASCAL:NANOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 33)), Sym("yottapa"))  # nopep8
    CONVERSIONS["YOTTAPASCAL:PASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 24)), Sym("yottapa"))  # nopep8
    CONVERSIONS["YOTTAPASCAL:PETAPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("yottapa"))  # nopep8
    CONVERSIONS["YOTTAPASCAL:PICOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 36)), Sym("yottapa"))  # nopep8
    CONVERSIONS["YOTTAPASCAL:PSI"] = \
        Mul(Rat(Int("172368932329209"), Mul(Int(25), Pow(10, 33))), Sym("yottapa"))  # nopep8
    CONVERSIONS["YOTTAPASCAL:TERAPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 12)), Sym("yottapa"))  # nopep8
    CONVERSIONS["YOTTAPASCAL:TORR"] = \
        Mul(Rat(Int(4053), Mul(Int(304), Pow(10, 23))), Sym("yottapa"))  # nopep8
    CONVERSIONS["YOTTAPASCAL:YOCTOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 48)), Sym("yottapa"))  # nopep8
    CONVERSIONS["YOTTAPASCAL:ZEPTOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 45)), Sym("yottapa"))  # nopep8
    CONVERSIONS["YOTTAPASCAL:ZETTAPASCAL"] = \
        Mul(Rat(Int(1), Int(1000)), Sym("yottapa"))  # nopep8
    CONVERSIONS["ZEPTOPASCAL:ATHMOSPHERE"] = \
        Mul(Mul(Int(101325), Pow(10, 21)), Sym("zeptopa"))  # nopep8
    CONVERSIONS["ZEPTOPASCAL:ATTOPASCAL"] = \
        Mul(Int(1000), Sym("zeptopa"))  # nopep8
    CONVERSIONS["ZEPTOPASCAL:BAR"] = \
        Mul(Pow(10, 26), Sym("zeptopa"))  # nopep8
    CONVERSIONS["ZEPTOPASCAL:CENTIBAR"] = \
        Mul(Pow(10, 24), Sym("zeptopa"))  # nopep8
    CONVERSIONS["ZEPTOPASCAL:CENTIPASCAL"] = \
        Mul(Pow(10, 19), Sym("zeptopa"))  # nopep8
    CONVERSIONS["ZEPTOPASCAL:DECAPASCAL"] = \
        Mul(Pow(10, 22), Sym("zeptopa"))  # nopep8
    CONVERSIONS["ZEPTOPASCAL:DECIBAR"] = \
        Mul(Pow(10, 25), Sym("zeptopa"))  # nopep8
    CONVERSIONS["ZEPTOPASCAL:DECIPASCAL"] = \
        Mul(Pow(10, 20), Sym("zeptopa"))  # nopep8
    CONVERSIONS["ZEPTOPASCAL:EXAPASCAL"] = \
        Mul(Pow(10, 39), Sym("zeptopa"))  # nopep8
    CONVERSIONS["ZEPTOPASCAL:FEMTOPASCAL"] = \
        Mul(Pow(10, 6), Sym("zeptopa"))  # nopep8
    CONVERSIONS["ZEPTOPASCAL:GIGAPASCAL"] = \
        Mul(Pow(10, 30), Sym("zeptopa"))  # nopep8
    CONVERSIONS["ZEPTOPASCAL:HECTOPASCAL"] = \
        Mul(Pow(10, 23), Sym("zeptopa"))  # nopep8
    CONVERSIONS["ZEPTOPASCAL:KILOBAR"] = \
        Mul(Pow(10, 29), Sym("zeptopa"))  # nopep8
    CONVERSIONS["ZEPTOPASCAL:KILOPASCAL"] = \
        Mul(Pow(10, 24), Sym("zeptopa"))  # nopep8
    CONVERSIONS["ZEPTOPASCAL:MEGABAR"] = \
        Mul(Pow(10, 32), Sym("zeptopa"))  # nopep8
    CONVERSIONS["ZEPTOPASCAL:MEGAPASCAL"] = \
        Mul(Pow(10, 27), Sym("zeptopa"))  # nopep8
    CONVERSIONS["ZEPTOPASCAL:MICROPASCAL"] = \
        Mul(Pow(10, 15), Sym("zeptopa"))  # nopep8
    CONVERSIONS["ZEPTOPASCAL:MILLIBAR"] = \
        Mul(Pow(10, 23), Sym("zeptopa"))  # nopep8
    CONVERSIONS["ZEPTOPASCAL:MILLIPASCAL"] = \
        Mul(Pow(10, 18), Sym("zeptopa"))  # nopep8
    CONVERSIONS["ZEPTOPASCAL:MILLITORR"] = \
        Mul(Rat(Mul(Int(2533125), Pow(10, 15)), Int(19)), Sym("zeptopa"))  # nopep8
    CONVERSIONS["ZEPTOPASCAL:MMHG"] = \
        Mul(Mul(Int("133322387415"), Pow(10, 12)), Sym("zeptopa"))  # nopep8
    CONVERSIONS["ZEPTOPASCAL:NANOPASCAL"] = \
        Mul(Pow(10, 12), Sym("zeptopa"))  # nopep8
    CONVERSIONS["ZEPTOPASCAL:PASCAL"] = \
        Mul(Pow(10, 21), Sym("zeptopa"))  # nopep8
    CONVERSIONS["ZEPTOPASCAL:PETAPASCAL"] = \
        Mul(Pow(10, 36), Sym("zeptopa"))  # nopep8
    CONVERSIONS["ZEPTOPASCAL:PICOPASCAL"] = \
        Mul(Pow(10, 9), Sym("zeptopa"))  # nopep8
    CONVERSIONS["ZEPTOPASCAL:PSI"] = \
        Mul(Mul(Int("689475729316836"), Pow(10, 10)), Sym("zeptopa"))  # nopep8
    CONVERSIONS["ZEPTOPASCAL:TERAPASCAL"] = \
        Mul(Pow(10, 33), Sym("zeptopa"))  # nopep8
    CONVERSIONS["ZEPTOPASCAL:TORR"] = \
        Mul(Rat(Mul(Int(2533125), Pow(10, 18)), Int(19)), Sym("zeptopa"))  # nopep8
    CONVERSIONS["ZEPTOPASCAL:YOCTOPASCAL"] = \
        Mul(Rat(Int(1), Int(1000)), Sym("zeptopa"))  # nopep8
    CONVERSIONS["ZEPTOPASCAL:YOTTAPASCAL"] = \
        Mul(Pow(10, 45), Sym("zeptopa"))  # nopep8
    CONVERSIONS["ZEPTOPASCAL:ZETTAPASCAL"] = \
        Mul(Pow(10, 42), Sym("zeptopa"))  # nopep8
    CONVERSIONS["ZETTAPASCAL:ATHMOSPHERE"] = \
        Mul(Rat(Int(4053), Mul(Int(4), Pow(10, 19))), Sym("zettapa"))  # nopep8
    CONVERSIONS["ZETTAPASCAL:ATTOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 39)), Sym("zettapa"))  # nopep8
    CONVERSIONS["ZETTAPASCAL:BAR"] = \
        Mul(Rat(Int(1), Pow(10, 16)), Sym("zettapa"))  # nopep8
    CONVERSIONS["ZETTAPASCAL:CENTIBAR"] = \
        Mul(Rat(Int(1), Pow(10, 18)), Sym("zettapa"))  # nopep8
    CONVERSIONS["ZETTAPASCAL:CENTIPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 23)), Sym("zettapa"))  # nopep8
    CONVERSIONS["ZETTAPASCAL:DECAPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 20)), Sym("zettapa"))  # nopep8
    CONVERSIONS["ZETTAPASCAL:DECIBAR"] = \
        Mul(Rat(Int(1), Pow(10, 17)), Sym("zettapa"))  # nopep8
    CONVERSIONS["ZETTAPASCAL:DECIPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 22)), Sym("zettapa"))  # nopep8
    CONVERSIONS["ZETTAPASCAL:EXAPASCAL"] = \
        Mul(Rat(Int(1), Int(1000)), Sym("zettapa"))  # nopep8
    CONVERSIONS["ZETTAPASCAL:FEMTOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 36)), Sym("zettapa"))  # nopep8
    CONVERSIONS["ZETTAPASCAL:GIGAPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 12)), Sym("zettapa"))  # nopep8
    CONVERSIONS["ZETTAPASCAL:HECTOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 19)), Sym("zettapa"))  # nopep8
    CONVERSIONS["ZETTAPASCAL:KILOBAR"] = \
        Mul(Rat(Int(1), Pow(10, 13)), Sym("zettapa"))  # nopep8
    CONVERSIONS["ZETTAPASCAL:KILOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 18)), Sym("zettapa"))  # nopep8
    CONVERSIONS["ZETTAPASCAL:MEGABAR"] = \
        Mul(Rat(Int(1), Pow(10, 10)), Sym("zettapa"))  # nopep8
    CONVERSIONS["ZETTAPASCAL:MEGAPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 15)), Sym("zettapa"))  # nopep8
    CONVERSIONS["ZETTAPASCAL:MICROPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 27)), Sym("zettapa"))  # nopep8
    CONVERSIONS["ZETTAPASCAL:MILLIBAR"] = \
        Mul(Rat(Int(1), Pow(10, 19)), Sym("zettapa"))  # nopep8
    CONVERSIONS["ZETTAPASCAL:MILLIPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 24)), Sym("zettapa"))  # nopep8
    CONVERSIONS["ZETTAPASCAL:MILLITORR"] = \
        Mul(Rat(Int(4053), Mul(Int(304), Pow(10, 23))), Sym("zettapa"))  # nopep8
    CONVERSIONS["ZETTAPASCAL:MMHG"] = \
        Mul(Rat(Int("26664477483"), Mul(Int(2), Pow(10, 29))), Sym("zettapa"))  # nopep8
    CONVERSIONS["ZETTAPASCAL:NANOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 30)), Sym("zettapa"))  # nopep8
    CONVERSIONS["ZETTAPASCAL:PASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 21)), Sym("zettapa"))  # nopep8
    CONVERSIONS["ZETTAPASCAL:PETAPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("zettapa"))  # nopep8
    CONVERSIONS["ZETTAPASCAL:PICOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 33)), Sym("zettapa"))  # nopep8
    CONVERSIONS["ZETTAPASCAL:PSI"] = \
        Mul(Rat(Int("172368932329209"), Mul(Int(25), Pow(10, 30))), Sym("zettapa"))  # nopep8
    CONVERSIONS["ZETTAPASCAL:TERAPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("zettapa"))  # nopep8
    CONVERSIONS["ZETTAPASCAL:TORR"] = \
        Mul(Rat(Int(4053), Mul(Int(304), Pow(10, 20))), Sym("zettapa"))  # nopep8
    CONVERSIONS["ZETTAPASCAL:YOCTOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 45)), Sym("zettapa"))  # nopep8
    CONVERSIONS["ZETTAPASCAL:YOTTAPASCAL"] = \
        Mul(Int(1000), Sym("zettapa"))  # nopep8
    CONVERSIONS["ZETTAPASCAL:ZEPTOPASCAL"] = \
        Mul(Rat(Int(1), Pow(10, 42)), Sym("zettapa"))  # nopep8

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
