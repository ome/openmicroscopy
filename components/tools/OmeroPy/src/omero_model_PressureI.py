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


def noconversion(cfrom, cto):
    raise Exception(("Unsupported conversion: "
                     "%s:%s") % cfrom, cto)


class PressureI(_omero_model.Pressure, UnitBase):

    CONVERSIONS = dict()
    CONVERSIONS["ATMOSPHERE:ATTOPASCAL"] = \
        lambda: noconversion("ATMOSPHERE", "ATTOPASCAL")
    CONVERSIONS["ATMOSPHERE:BAR"] = \
        lambda: noconversion("ATMOSPHERE", "BAR")
    CONVERSIONS["ATMOSPHERE:CENTIBAR"] = \
        lambda: noconversion("ATMOSPHERE", "CENTIBAR")
    CONVERSIONS["ATMOSPHERE:CENTIPASCAL"] = \
        lambda: noconversion("ATMOSPHERE", "CENTIPASCAL")
    CONVERSIONS["ATMOSPHERE:DECAPASCAL"] = \
        lambda: noconversion("ATMOSPHERE", "DECAPASCAL")
    CONVERSIONS["ATMOSPHERE:DECIBAR"] = \
        lambda: noconversion("ATMOSPHERE", "DECIBAR")
    CONVERSIONS["ATMOSPHERE:DECIPASCAL"] = \
        lambda: noconversion("ATMOSPHERE", "DECIPASCAL")
    CONVERSIONS["ATMOSPHERE:EXAPASCAL"] = \
        lambda: noconversion("ATMOSPHERE", "EXAPASCAL")
    CONVERSIONS["ATMOSPHERE:FEMTOPASCAL"] = \
        lambda: noconversion("ATMOSPHERE", "FEMTOPASCAL")
    CONVERSIONS["ATMOSPHERE:GIGAPASCAL"] = \
        lambda: noconversion("ATMOSPHERE", "GIGAPASCAL")
    CONVERSIONS["ATMOSPHERE:HECTOPASCAL"] = \
        lambda: noconversion("ATMOSPHERE", "HECTOPASCAL")
    CONVERSIONS["ATMOSPHERE:KILOBAR"] = \
        lambda: noconversion("ATMOSPHERE", "KILOBAR")
    CONVERSIONS["ATMOSPHERE:KILOPASCAL"] = \
        lambda: noconversion("ATMOSPHERE", "KILOPASCAL")
    CONVERSIONS["ATMOSPHERE:MEGABAR"] = \
        lambda: noconversion("ATMOSPHERE", "MEGABAR")
    CONVERSIONS["ATMOSPHERE:MEGAPASCAL"] = \
        lambda: noconversion("ATMOSPHERE", "MEGAPASCAL")
    CONVERSIONS["ATMOSPHERE:MICROPASCAL"] = \
        lambda: noconversion("ATMOSPHERE", "MICROPASCAL")
    CONVERSIONS["ATMOSPHERE:MILLIBAR"] = \
        lambda: noconversion("ATMOSPHERE", "MILLIBAR")
    CONVERSIONS["ATMOSPHERE:MILLIPASCAL"] = \
        lambda: noconversion("ATMOSPHERE", "MILLIPASCAL")
    CONVERSIONS["ATMOSPHERE:MILLITORR"] = \
        lambda: noconversion("ATMOSPHERE", "MILLITORR")
    CONVERSIONS["ATMOSPHERE:MMHG"] = \
        lambda: noconversion("ATMOSPHERE", "MMHG")
    CONVERSIONS["ATMOSPHERE:NANOPASCAL"] = \
        lambda: noconversion("ATMOSPHERE", "NANOPASCAL")
    CONVERSIONS["ATMOSPHERE:PETAPASCAL"] = \
        lambda: noconversion("ATMOSPHERE", "PETAPASCAL")
    CONVERSIONS["ATMOSPHERE:PICOPASCAL"] = \
        lambda: noconversion("ATMOSPHERE", "PICOPASCAL")
    CONVERSIONS["ATMOSPHERE:PSI"] = \
        lambda: noconversion("ATMOSPHERE", "PSI")
    CONVERSIONS["ATMOSPHERE:Pascal"] = \
        lambda: noconversion("ATMOSPHERE", "Pascal")
    CONVERSIONS["ATMOSPHERE:TERAPASCAL"] = \
        lambda: noconversion("ATMOSPHERE", "TERAPASCAL")
    CONVERSIONS["ATMOSPHERE:TORR"] = \
        lambda: noconversion("ATMOSPHERE", "TORR")
    CONVERSIONS["ATMOSPHERE:YOCTOPASCAL"] = \
        lambda: noconversion("ATMOSPHERE", "YOCTOPASCAL")
    CONVERSIONS["ATMOSPHERE:YOTTAPASCAL"] = \
        lambda: noconversion("ATMOSPHERE", "YOTTAPASCAL")
    CONVERSIONS["ATMOSPHERE:ZEPTOPASCAL"] = \
        lambda: noconversion("ATMOSPHERE", "ZEPTOPASCAL")
    CONVERSIONS["ATMOSPHERE:ZETTAPASCAL"] = \
        lambda: noconversion("ATMOSPHERE", "ZETTAPASCAL")
    CONVERSIONS["ATTOPASCAL:ATMOSPHERE"] = \
        lambda: noconversion("ATTOPASCAL", "ATMOSPHERE")
    CONVERSIONS["ATTOPASCAL:BAR"] = \
        lambda: noconversion("ATTOPASCAL", "BAR")
    CONVERSIONS["ATTOPASCAL:CENTIBAR"] = \
        lambda: noconversion("ATTOPASCAL", "CENTIBAR")
    CONVERSIONS["ATTOPASCAL:CENTIPASCAL"] = \
        lambda value: (10 ** -16) * value
    CONVERSIONS["ATTOPASCAL:DECAPASCAL"] = \
        lambda value: (10 ** -19) * value
    CONVERSIONS["ATTOPASCAL:DECIBAR"] = \
        lambda: noconversion("ATTOPASCAL", "DECIBAR")
    CONVERSIONS["ATTOPASCAL:DECIPASCAL"] = \
        lambda value: (10 ** -17) * value
    CONVERSIONS["ATTOPASCAL:EXAPASCAL"] = \
        lambda value: (10 ** -36) * value
    CONVERSIONS["ATTOPASCAL:FEMTOPASCAL"] = \
        lambda value: (10 ** -3) * value
    CONVERSIONS["ATTOPASCAL:GIGAPASCAL"] = \
        lambda value: (10 ** -27) * value
    CONVERSIONS["ATTOPASCAL:HECTOPASCAL"] = \
        lambda value: (10 ** -20) * value
    CONVERSIONS["ATTOPASCAL:KILOBAR"] = \
        lambda: noconversion("ATTOPASCAL", "KILOBAR")
    CONVERSIONS["ATTOPASCAL:KILOPASCAL"] = \
        lambda value: (10 ** -21) * value
    CONVERSIONS["ATTOPASCAL:MEGABAR"] = \
        lambda: noconversion("ATTOPASCAL", "MEGABAR")
    CONVERSIONS["ATTOPASCAL:MEGAPASCAL"] = \
        lambda value: (10 ** -24) * value
    CONVERSIONS["ATTOPASCAL:MICROPASCAL"] = \
        lambda value: (10 ** -12) * value
    CONVERSIONS["ATTOPASCAL:MILLIBAR"] = \
        lambda: noconversion("ATTOPASCAL", "MILLIBAR")
    CONVERSIONS["ATTOPASCAL:MILLIPASCAL"] = \
        lambda value: (10 ** -15) * value
    CONVERSIONS["ATTOPASCAL:MILLITORR"] = \
        lambda: noconversion("ATTOPASCAL", "MILLITORR")
    CONVERSIONS["ATTOPASCAL:MMHG"] = \
        lambda: noconversion("ATTOPASCAL", "MMHG")
    CONVERSIONS["ATTOPASCAL:NANOPASCAL"] = \
        lambda value: (10 ** -9) * value
    CONVERSIONS["ATTOPASCAL:PETAPASCAL"] = \
        lambda value: (10 ** -33) * value
    CONVERSIONS["ATTOPASCAL:PICOPASCAL"] = \
        lambda value: (10 ** -6) * value
    CONVERSIONS["ATTOPASCAL:PSI"] = \
        lambda: noconversion("ATTOPASCAL", "PSI")
    CONVERSIONS["ATTOPASCAL:Pascal"] = \
        lambda value: (10 ** -18) * value
    CONVERSIONS["ATTOPASCAL:TERAPASCAL"] = \
        lambda value: (10 ** -30) * value
    CONVERSIONS["ATTOPASCAL:TORR"] = \
        lambda: noconversion("ATTOPASCAL", "TORR")
    CONVERSIONS["ATTOPASCAL:YOCTOPASCAL"] = \
        lambda value: (10 ** 6) * value
    CONVERSIONS["ATTOPASCAL:YOTTAPASCAL"] = \
        lambda value: (10 ** -42) * value
    CONVERSIONS["ATTOPASCAL:ZEPTOPASCAL"] = \
        lambda value: (10 ** 3) * value
    CONVERSIONS["ATTOPASCAL:ZETTAPASCAL"] = \
        lambda value: (10 ** -39) * value
    CONVERSIONS["BAR:ATMOSPHERE"] = \
        lambda: noconversion("BAR", "ATMOSPHERE")
    CONVERSIONS["BAR:ATTOPASCAL"] = \
        lambda: noconversion("BAR", "ATTOPASCAL")
    CONVERSIONS["BAR:CENTIBAR"] = \
        lambda: noconversion("BAR", "CENTIBAR")
    CONVERSIONS["BAR:CENTIPASCAL"] = \
        lambda: noconversion("BAR", "CENTIPASCAL")
    CONVERSIONS["BAR:DECAPASCAL"] = \
        lambda: noconversion("BAR", "DECAPASCAL")
    CONVERSIONS["BAR:DECIBAR"] = \
        lambda: noconversion("BAR", "DECIBAR")
    CONVERSIONS["BAR:DECIPASCAL"] = \
        lambda: noconversion("BAR", "DECIPASCAL")
    CONVERSIONS["BAR:EXAPASCAL"] = \
        lambda: noconversion("BAR", "EXAPASCAL")
    CONVERSIONS["BAR:FEMTOPASCAL"] = \
        lambda: noconversion("BAR", "FEMTOPASCAL")
    CONVERSIONS["BAR:GIGAPASCAL"] = \
        lambda: noconversion("BAR", "GIGAPASCAL")
    CONVERSIONS["BAR:HECTOPASCAL"] = \
        lambda: noconversion("BAR", "HECTOPASCAL")
    CONVERSIONS["BAR:KILOBAR"] = \
        lambda: noconversion("BAR", "KILOBAR")
    CONVERSIONS["BAR:KILOPASCAL"] = \
        lambda: noconversion("BAR", "KILOPASCAL")
    CONVERSIONS["BAR:MEGABAR"] = \
        lambda: noconversion("BAR", "MEGABAR")
    CONVERSIONS["BAR:MEGAPASCAL"] = \
        lambda: noconversion("BAR", "MEGAPASCAL")
    CONVERSIONS["BAR:MICROPASCAL"] = \
        lambda: noconversion("BAR", "MICROPASCAL")
    CONVERSIONS["BAR:MILLIBAR"] = \
        lambda: noconversion("BAR", "MILLIBAR")
    CONVERSIONS["BAR:MILLIPASCAL"] = \
        lambda: noconversion("BAR", "MILLIPASCAL")
    CONVERSIONS["BAR:MILLITORR"] = \
        lambda: noconversion("BAR", "MILLITORR")
    CONVERSIONS["BAR:MMHG"] = \
        lambda: noconversion("BAR", "MMHG")
    CONVERSIONS["BAR:NANOPASCAL"] = \
        lambda: noconversion("BAR", "NANOPASCAL")
    CONVERSIONS["BAR:PETAPASCAL"] = \
        lambda: noconversion("BAR", "PETAPASCAL")
    CONVERSIONS["BAR:PICOPASCAL"] = \
        lambda: noconversion("BAR", "PICOPASCAL")
    CONVERSIONS["BAR:PSI"] = \
        lambda: noconversion("BAR", "PSI")
    CONVERSIONS["BAR:Pascal"] = \
        lambda: noconversion("BAR", "Pascal")
    CONVERSIONS["BAR:TERAPASCAL"] = \
        lambda: noconversion("BAR", "TERAPASCAL")
    CONVERSIONS["BAR:TORR"] = \
        lambda: noconversion("BAR", "TORR")
    CONVERSIONS["BAR:YOCTOPASCAL"] = \
        lambda: noconversion("BAR", "YOCTOPASCAL")
    CONVERSIONS["BAR:YOTTAPASCAL"] = \
        lambda: noconversion("BAR", "YOTTAPASCAL")
    CONVERSIONS["BAR:ZEPTOPASCAL"] = \
        lambda: noconversion("BAR", "ZEPTOPASCAL")
    CONVERSIONS["BAR:ZETTAPASCAL"] = \
        lambda: noconversion("BAR", "ZETTAPASCAL")
    CONVERSIONS["CENTIBAR:ATMOSPHERE"] = \
        lambda: noconversion("CENTIBAR", "ATMOSPHERE")
    CONVERSIONS["CENTIBAR:ATTOPASCAL"] = \
        lambda: noconversion("CENTIBAR", "ATTOPASCAL")
    CONVERSIONS["CENTIBAR:BAR"] = \
        lambda: noconversion("CENTIBAR", "BAR")
    CONVERSIONS["CENTIBAR:CENTIPASCAL"] = \
        lambda: noconversion("CENTIBAR", "CENTIPASCAL")
    CONVERSIONS["CENTIBAR:DECAPASCAL"] = \
        lambda: noconversion("CENTIBAR", "DECAPASCAL")
    CONVERSIONS["CENTIBAR:DECIBAR"] = \
        lambda: noconversion("CENTIBAR", "DECIBAR")
    CONVERSIONS["CENTIBAR:DECIPASCAL"] = \
        lambda: noconversion("CENTIBAR", "DECIPASCAL")
    CONVERSIONS["CENTIBAR:EXAPASCAL"] = \
        lambda: noconversion("CENTIBAR", "EXAPASCAL")
    CONVERSIONS["CENTIBAR:FEMTOPASCAL"] = \
        lambda: noconversion("CENTIBAR", "FEMTOPASCAL")
    CONVERSIONS["CENTIBAR:GIGAPASCAL"] = \
        lambda: noconversion("CENTIBAR", "GIGAPASCAL")
    CONVERSIONS["CENTIBAR:HECTOPASCAL"] = \
        lambda: noconversion("CENTIBAR", "HECTOPASCAL")
    CONVERSIONS["CENTIBAR:KILOBAR"] = \
        lambda: noconversion("CENTIBAR", "KILOBAR")
    CONVERSIONS["CENTIBAR:KILOPASCAL"] = \
        lambda: noconversion("CENTIBAR", "KILOPASCAL")
    CONVERSIONS["CENTIBAR:MEGABAR"] = \
        lambda: noconversion("CENTIBAR", "MEGABAR")
    CONVERSIONS["CENTIBAR:MEGAPASCAL"] = \
        lambda: noconversion("CENTIBAR", "MEGAPASCAL")
    CONVERSIONS["CENTIBAR:MICROPASCAL"] = \
        lambda: noconversion("CENTIBAR", "MICROPASCAL")
    CONVERSIONS["CENTIBAR:MILLIBAR"] = \
        lambda: noconversion("CENTIBAR", "MILLIBAR")
    CONVERSIONS["CENTIBAR:MILLIPASCAL"] = \
        lambda: noconversion("CENTIBAR", "MILLIPASCAL")
    CONVERSIONS["CENTIBAR:MILLITORR"] = \
        lambda: noconversion("CENTIBAR", "MILLITORR")
    CONVERSIONS["CENTIBAR:MMHG"] = \
        lambda: noconversion("CENTIBAR", "MMHG")
    CONVERSIONS["CENTIBAR:NANOPASCAL"] = \
        lambda: noconversion("CENTIBAR", "NANOPASCAL")
    CONVERSIONS["CENTIBAR:PETAPASCAL"] = \
        lambda: noconversion("CENTIBAR", "PETAPASCAL")
    CONVERSIONS["CENTIBAR:PICOPASCAL"] = \
        lambda: noconversion("CENTIBAR", "PICOPASCAL")
    CONVERSIONS["CENTIBAR:PSI"] = \
        lambda: noconversion("CENTIBAR", "PSI")
    CONVERSIONS["CENTIBAR:Pascal"] = \
        lambda: noconversion("CENTIBAR", "Pascal")
    CONVERSIONS["CENTIBAR:TERAPASCAL"] = \
        lambda: noconversion("CENTIBAR", "TERAPASCAL")
    CONVERSIONS["CENTIBAR:TORR"] = \
        lambda: noconversion("CENTIBAR", "TORR")
    CONVERSIONS["CENTIBAR:YOCTOPASCAL"] = \
        lambda: noconversion("CENTIBAR", "YOCTOPASCAL")
    CONVERSIONS["CENTIBAR:YOTTAPASCAL"] = \
        lambda: noconversion("CENTIBAR", "YOTTAPASCAL")
    CONVERSIONS["CENTIBAR:ZEPTOPASCAL"] = \
        lambda: noconversion("CENTIBAR", "ZEPTOPASCAL")
    CONVERSIONS["CENTIBAR:ZETTAPASCAL"] = \
        lambda: noconversion("CENTIBAR", "ZETTAPASCAL")
    CONVERSIONS["CENTIPASCAL:ATMOSPHERE"] = \
        lambda: noconversion("CENTIPASCAL", "ATMOSPHERE")
    CONVERSIONS["CENTIPASCAL:ATTOPASCAL"] = \
        lambda value: (10 ** 16) * value
    CONVERSIONS["CENTIPASCAL:BAR"] = \
        lambda: noconversion("CENTIPASCAL", "BAR")
    CONVERSIONS["CENTIPASCAL:CENTIBAR"] = \
        lambda: noconversion("CENTIPASCAL", "CENTIBAR")
    CONVERSIONS["CENTIPASCAL:DECAPASCAL"] = \
        lambda value: (10 ** -3) * value
    CONVERSIONS["CENTIPASCAL:DECIBAR"] = \
        lambda: noconversion("CENTIPASCAL", "DECIBAR")
    CONVERSIONS["CENTIPASCAL:DECIPASCAL"] = \
        lambda value: (10 ** -1) * value
    CONVERSIONS["CENTIPASCAL:EXAPASCAL"] = \
        lambda value: (10 ** -20) * value
    CONVERSIONS["CENTIPASCAL:FEMTOPASCAL"] = \
        lambda value: (10 ** 13) * value
    CONVERSIONS["CENTIPASCAL:GIGAPASCAL"] = \
        lambda value: (10 ** -11) * value
    CONVERSIONS["CENTIPASCAL:HECTOPASCAL"] = \
        lambda value: (10 ** -4) * value
    CONVERSIONS["CENTIPASCAL:KILOBAR"] = \
        lambda: noconversion("CENTIPASCAL", "KILOBAR")
    CONVERSIONS["CENTIPASCAL:KILOPASCAL"] = \
        lambda value: (10 ** -5) * value
    CONVERSIONS["CENTIPASCAL:MEGABAR"] = \
        lambda: noconversion("CENTIPASCAL", "MEGABAR")
    CONVERSIONS["CENTIPASCAL:MEGAPASCAL"] = \
        lambda value: (10 ** -8) * value
    CONVERSIONS["CENTIPASCAL:MICROPASCAL"] = \
        lambda value: (10 ** 4) * value
    CONVERSIONS["CENTIPASCAL:MILLIBAR"] = \
        lambda: noconversion("CENTIPASCAL", "MILLIBAR")
    CONVERSIONS["CENTIPASCAL:MILLIPASCAL"] = \
        lambda value: 10 * value
    CONVERSIONS["CENTIPASCAL:MILLITORR"] = \
        lambda: noconversion("CENTIPASCAL", "MILLITORR")
    CONVERSIONS["CENTIPASCAL:MMHG"] = \
        lambda: noconversion("CENTIPASCAL", "MMHG")
    CONVERSIONS["CENTIPASCAL:NANOPASCAL"] = \
        lambda value: (10 ** 7) * value
    CONVERSIONS["CENTIPASCAL:PETAPASCAL"] = \
        lambda value: (10 ** -17) * value
    CONVERSIONS["CENTIPASCAL:PICOPASCAL"] = \
        lambda value: (10 ** 10) * value
    CONVERSIONS["CENTIPASCAL:PSI"] = \
        lambda: noconversion("CENTIPASCAL", "PSI")
    CONVERSIONS["CENTIPASCAL:Pascal"] = \
        lambda value: (10 ** -2) * value
    CONVERSIONS["CENTIPASCAL:TERAPASCAL"] = \
        lambda value: (10 ** -14) * value
    CONVERSIONS["CENTIPASCAL:TORR"] = \
        lambda: noconversion("CENTIPASCAL", "TORR")
    CONVERSIONS["CENTIPASCAL:YOCTOPASCAL"] = \
        lambda value: (10 ** 22) * value
    CONVERSIONS["CENTIPASCAL:YOTTAPASCAL"] = \
        lambda value: (10 ** -26) * value
    CONVERSIONS["CENTIPASCAL:ZEPTOPASCAL"] = \
        lambda value: (10 ** 19) * value
    CONVERSIONS["CENTIPASCAL:ZETTAPASCAL"] = \
        lambda value: (10 ** -23) * value
    CONVERSIONS["DECAPASCAL:ATMOSPHERE"] = \
        lambda: noconversion("DECAPASCAL", "ATMOSPHERE")
    CONVERSIONS["DECAPASCAL:ATTOPASCAL"] = \
        lambda value: (10 ** 19) * value
    CONVERSIONS["DECAPASCAL:BAR"] = \
        lambda: noconversion("DECAPASCAL", "BAR")
    CONVERSIONS["DECAPASCAL:CENTIBAR"] = \
        lambda: noconversion("DECAPASCAL", "CENTIBAR")
    CONVERSIONS["DECAPASCAL:CENTIPASCAL"] = \
        lambda value: (10 ** 3) * value
    CONVERSIONS["DECAPASCAL:DECIBAR"] = \
        lambda: noconversion("DECAPASCAL", "DECIBAR")
    CONVERSIONS["DECAPASCAL:DECIPASCAL"] = \
        lambda value: (10 ** 2) * value
    CONVERSIONS["DECAPASCAL:EXAPASCAL"] = \
        lambda value: (10 ** -17) * value
    CONVERSIONS["DECAPASCAL:FEMTOPASCAL"] = \
        lambda value: (10 ** 16) * value
    CONVERSIONS["DECAPASCAL:GIGAPASCAL"] = \
        lambda value: (10 ** -8) * value
    CONVERSIONS["DECAPASCAL:HECTOPASCAL"] = \
        lambda value: (10 ** -1) * value
    CONVERSIONS["DECAPASCAL:KILOBAR"] = \
        lambda: noconversion("DECAPASCAL", "KILOBAR")
    CONVERSIONS["DECAPASCAL:KILOPASCAL"] = \
        lambda value: (10 ** -2) * value
    CONVERSIONS["DECAPASCAL:MEGABAR"] = \
        lambda: noconversion("DECAPASCAL", "MEGABAR")
    CONVERSIONS["DECAPASCAL:MEGAPASCAL"] = \
        lambda value: (10 ** -5) * value
    CONVERSIONS["DECAPASCAL:MICROPASCAL"] = \
        lambda value: (10 ** 7) * value
    CONVERSIONS["DECAPASCAL:MILLIBAR"] = \
        lambda: noconversion("DECAPASCAL", "MILLIBAR")
    CONVERSIONS["DECAPASCAL:MILLIPASCAL"] = \
        lambda value: (10 ** 4) * value
    CONVERSIONS["DECAPASCAL:MILLITORR"] = \
        lambda: noconversion("DECAPASCAL", "MILLITORR")
    CONVERSIONS["DECAPASCAL:MMHG"] = \
        lambda: noconversion("DECAPASCAL", "MMHG")
    CONVERSIONS["DECAPASCAL:NANOPASCAL"] = \
        lambda value: (10 ** 10) * value
    CONVERSIONS["DECAPASCAL:PETAPASCAL"] = \
        lambda value: (10 ** -14) * value
    CONVERSIONS["DECAPASCAL:PICOPASCAL"] = \
        lambda value: (10 ** 13) * value
    CONVERSIONS["DECAPASCAL:PSI"] = \
        lambda: noconversion("DECAPASCAL", "PSI")
    CONVERSIONS["DECAPASCAL:Pascal"] = \
        lambda value: 10 * value
    CONVERSIONS["DECAPASCAL:TERAPASCAL"] = \
        lambda value: (10 ** -11) * value
    CONVERSIONS["DECAPASCAL:TORR"] = \
        lambda: noconversion("DECAPASCAL", "TORR")
    CONVERSIONS["DECAPASCAL:YOCTOPASCAL"] = \
        lambda value: (10 ** 25) * value
    CONVERSIONS["DECAPASCAL:YOTTAPASCAL"] = \
        lambda value: (10 ** -23) * value
    CONVERSIONS["DECAPASCAL:ZEPTOPASCAL"] = \
        lambda value: (10 ** 22) * value
    CONVERSIONS["DECAPASCAL:ZETTAPASCAL"] = \
        lambda value: (10 ** -20) * value
    CONVERSIONS["DECIBAR:ATMOSPHERE"] = \
        lambda: noconversion("DECIBAR", "ATMOSPHERE")
    CONVERSIONS["DECIBAR:ATTOPASCAL"] = \
        lambda: noconversion("DECIBAR", "ATTOPASCAL")
    CONVERSIONS["DECIBAR:BAR"] = \
        lambda: noconversion("DECIBAR", "BAR")
    CONVERSIONS["DECIBAR:CENTIBAR"] = \
        lambda: noconversion("DECIBAR", "CENTIBAR")
    CONVERSIONS["DECIBAR:CENTIPASCAL"] = \
        lambda: noconversion("DECIBAR", "CENTIPASCAL")
    CONVERSIONS["DECIBAR:DECAPASCAL"] = \
        lambda: noconversion("DECIBAR", "DECAPASCAL")
    CONVERSIONS["DECIBAR:DECIPASCAL"] = \
        lambda: noconversion("DECIBAR", "DECIPASCAL")
    CONVERSIONS["DECIBAR:EXAPASCAL"] = \
        lambda: noconversion("DECIBAR", "EXAPASCAL")
    CONVERSIONS["DECIBAR:FEMTOPASCAL"] = \
        lambda: noconversion("DECIBAR", "FEMTOPASCAL")
    CONVERSIONS["DECIBAR:GIGAPASCAL"] = \
        lambda: noconversion("DECIBAR", "GIGAPASCAL")
    CONVERSIONS["DECIBAR:HECTOPASCAL"] = \
        lambda: noconversion("DECIBAR", "HECTOPASCAL")
    CONVERSIONS["DECIBAR:KILOBAR"] = \
        lambda: noconversion("DECIBAR", "KILOBAR")
    CONVERSIONS["DECIBAR:KILOPASCAL"] = \
        lambda: noconversion("DECIBAR", "KILOPASCAL")
    CONVERSIONS["DECIBAR:MEGABAR"] = \
        lambda: noconversion("DECIBAR", "MEGABAR")
    CONVERSIONS["DECIBAR:MEGAPASCAL"] = \
        lambda: noconversion("DECIBAR", "MEGAPASCAL")
    CONVERSIONS["DECIBAR:MICROPASCAL"] = \
        lambda: noconversion("DECIBAR", "MICROPASCAL")
    CONVERSIONS["DECIBAR:MILLIBAR"] = \
        lambda: noconversion("DECIBAR", "MILLIBAR")
    CONVERSIONS["DECIBAR:MILLIPASCAL"] = \
        lambda: noconversion("DECIBAR", "MILLIPASCAL")
    CONVERSIONS["DECIBAR:MILLITORR"] = \
        lambda: noconversion("DECIBAR", "MILLITORR")
    CONVERSIONS["DECIBAR:MMHG"] = \
        lambda: noconversion("DECIBAR", "MMHG")
    CONVERSIONS["DECIBAR:NANOPASCAL"] = \
        lambda: noconversion("DECIBAR", "NANOPASCAL")
    CONVERSIONS["DECIBAR:PETAPASCAL"] = \
        lambda: noconversion("DECIBAR", "PETAPASCAL")
    CONVERSIONS["DECIBAR:PICOPASCAL"] = \
        lambda: noconversion("DECIBAR", "PICOPASCAL")
    CONVERSIONS["DECIBAR:PSI"] = \
        lambda: noconversion("DECIBAR", "PSI")
    CONVERSIONS["DECIBAR:Pascal"] = \
        lambda: noconversion("DECIBAR", "Pascal")
    CONVERSIONS["DECIBAR:TERAPASCAL"] = \
        lambda: noconversion("DECIBAR", "TERAPASCAL")
    CONVERSIONS["DECIBAR:TORR"] = \
        lambda: noconversion("DECIBAR", "TORR")
    CONVERSIONS["DECIBAR:YOCTOPASCAL"] = \
        lambda: noconversion("DECIBAR", "YOCTOPASCAL")
    CONVERSIONS["DECIBAR:YOTTAPASCAL"] = \
        lambda: noconversion("DECIBAR", "YOTTAPASCAL")
    CONVERSIONS["DECIBAR:ZEPTOPASCAL"] = \
        lambda: noconversion("DECIBAR", "ZEPTOPASCAL")
    CONVERSIONS["DECIBAR:ZETTAPASCAL"] = \
        lambda: noconversion("DECIBAR", "ZETTAPASCAL")
    CONVERSIONS["DECIPASCAL:ATMOSPHERE"] = \
        lambda: noconversion("DECIPASCAL", "ATMOSPHERE")
    CONVERSIONS["DECIPASCAL:ATTOPASCAL"] = \
        lambda value: (10 ** 17) * value
    CONVERSIONS["DECIPASCAL:BAR"] = \
        lambda: noconversion("DECIPASCAL", "BAR")
    CONVERSIONS["DECIPASCAL:CENTIBAR"] = \
        lambda: noconversion("DECIPASCAL", "CENTIBAR")
    CONVERSIONS["DECIPASCAL:CENTIPASCAL"] = \
        lambda value: 10 * value
    CONVERSIONS["DECIPASCAL:DECAPASCAL"] = \
        lambda value: (10 ** -2) * value
    CONVERSIONS["DECIPASCAL:DECIBAR"] = \
        lambda: noconversion("DECIPASCAL", "DECIBAR")
    CONVERSIONS["DECIPASCAL:EXAPASCAL"] = \
        lambda value: (10 ** -19) * value
    CONVERSIONS["DECIPASCAL:FEMTOPASCAL"] = \
        lambda value: (10 ** 14) * value
    CONVERSIONS["DECIPASCAL:GIGAPASCAL"] = \
        lambda value: (10 ** -10) * value
    CONVERSIONS["DECIPASCAL:HECTOPASCAL"] = \
        lambda value: (10 ** -3) * value
    CONVERSIONS["DECIPASCAL:KILOBAR"] = \
        lambda: noconversion("DECIPASCAL", "KILOBAR")
    CONVERSIONS["DECIPASCAL:KILOPASCAL"] = \
        lambda value: (10 ** -4) * value
    CONVERSIONS["DECIPASCAL:MEGABAR"] = \
        lambda: noconversion("DECIPASCAL", "MEGABAR")
    CONVERSIONS["DECIPASCAL:MEGAPASCAL"] = \
        lambda value: (10 ** -7) * value
    CONVERSIONS["DECIPASCAL:MICROPASCAL"] = \
        lambda value: (10 ** 5) * value
    CONVERSIONS["DECIPASCAL:MILLIBAR"] = \
        lambda: noconversion("DECIPASCAL", "MILLIBAR")
    CONVERSIONS["DECIPASCAL:MILLIPASCAL"] = \
        lambda value: (10 ** 2) * value
    CONVERSIONS["DECIPASCAL:MILLITORR"] = \
        lambda: noconversion("DECIPASCAL", "MILLITORR")
    CONVERSIONS["DECIPASCAL:MMHG"] = \
        lambda: noconversion("DECIPASCAL", "MMHG")
    CONVERSIONS["DECIPASCAL:NANOPASCAL"] = \
        lambda value: (10 ** 8) * value
    CONVERSIONS["DECIPASCAL:PETAPASCAL"] = \
        lambda value: (10 ** -16) * value
    CONVERSIONS["DECIPASCAL:PICOPASCAL"] = \
        lambda value: (10 ** 11) * value
    CONVERSIONS["DECIPASCAL:PSI"] = \
        lambda: noconversion("DECIPASCAL", "PSI")
    CONVERSIONS["DECIPASCAL:Pascal"] = \
        lambda value: (10 ** -1) * value
    CONVERSIONS["DECIPASCAL:TERAPASCAL"] = \
        lambda value: (10 ** -13) * value
    CONVERSIONS["DECIPASCAL:TORR"] = \
        lambda: noconversion("DECIPASCAL", "TORR")
    CONVERSIONS["DECIPASCAL:YOCTOPASCAL"] = \
        lambda value: (10 ** 23) * value
    CONVERSIONS["DECIPASCAL:YOTTAPASCAL"] = \
        lambda value: (10 ** -25) * value
    CONVERSIONS["DECIPASCAL:ZEPTOPASCAL"] = \
        lambda value: (10 ** 20) * value
    CONVERSIONS["DECIPASCAL:ZETTAPASCAL"] = \
        lambda value: (10 ** -22) * value
    CONVERSIONS["EXAPASCAL:ATMOSPHERE"] = \
        lambda: noconversion("EXAPASCAL", "ATMOSPHERE")
    CONVERSIONS["EXAPASCAL:ATTOPASCAL"] = \
        lambda value: (10 ** 36) * value
    CONVERSIONS["EXAPASCAL:BAR"] = \
        lambda: noconversion("EXAPASCAL", "BAR")
    CONVERSIONS["EXAPASCAL:CENTIBAR"] = \
        lambda: noconversion("EXAPASCAL", "CENTIBAR")
    CONVERSIONS["EXAPASCAL:CENTIPASCAL"] = \
        lambda value: (10 ** 20) * value
    CONVERSIONS["EXAPASCAL:DECAPASCAL"] = \
        lambda value: (10 ** 17) * value
    CONVERSIONS["EXAPASCAL:DECIBAR"] = \
        lambda: noconversion("EXAPASCAL", "DECIBAR")
    CONVERSIONS["EXAPASCAL:DECIPASCAL"] = \
        lambda value: (10 ** 19) * value
    CONVERSIONS["EXAPASCAL:FEMTOPASCAL"] = \
        lambda value: (10 ** 33) * value
    CONVERSIONS["EXAPASCAL:GIGAPASCAL"] = \
        lambda value: (10 ** 9) * value
    CONVERSIONS["EXAPASCAL:HECTOPASCAL"] = \
        lambda value: (10 ** 16) * value
    CONVERSIONS["EXAPASCAL:KILOBAR"] = \
        lambda: noconversion("EXAPASCAL", "KILOBAR")
    CONVERSIONS["EXAPASCAL:KILOPASCAL"] = \
        lambda value: (10 ** 15) * value
    CONVERSIONS["EXAPASCAL:MEGABAR"] = \
        lambda: noconversion("EXAPASCAL", "MEGABAR")
    CONVERSIONS["EXAPASCAL:MEGAPASCAL"] = \
        lambda value: (10 ** 12) * value
    CONVERSIONS["EXAPASCAL:MICROPASCAL"] = \
        lambda value: (10 ** 24) * value
    CONVERSIONS["EXAPASCAL:MILLIBAR"] = \
        lambda: noconversion("EXAPASCAL", "MILLIBAR")
    CONVERSIONS["EXAPASCAL:MILLIPASCAL"] = \
        lambda value: (10 ** 21) * value
    CONVERSIONS["EXAPASCAL:MILLITORR"] = \
        lambda: noconversion("EXAPASCAL", "MILLITORR")
    CONVERSIONS["EXAPASCAL:MMHG"] = \
        lambda: noconversion("EXAPASCAL", "MMHG")
    CONVERSIONS["EXAPASCAL:NANOPASCAL"] = \
        lambda value: (10 ** 27) * value
    CONVERSIONS["EXAPASCAL:PETAPASCAL"] = \
        lambda value: (10 ** 3) * value
    CONVERSIONS["EXAPASCAL:PICOPASCAL"] = \
        lambda value: (10 ** 30) * value
    CONVERSIONS["EXAPASCAL:PSI"] = \
        lambda: noconversion("EXAPASCAL", "PSI")
    CONVERSIONS["EXAPASCAL:Pascal"] = \
        lambda value: (10 ** 18) * value
    CONVERSIONS["EXAPASCAL:TERAPASCAL"] = \
        lambda value: (10 ** 6) * value
    CONVERSIONS["EXAPASCAL:TORR"] = \
        lambda: noconversion("EXAPASCAL", "TORR")
    CONVERSIONS["EXAPASCAL:YOCTOPASCAL"] = \
        lambda value: (10 ** 42) * value
    CONVERSIONS["EXAPASCAL:YOTTAPASCAL"] = \
        lambda value: (10 ** -6) * value
    CONVERSIONS["EXAPASCAL:ZEPTOPASCAL"] = \
        lambda value: (10 ** 39) * value
    CONVERSIONS["EXAPASCAL:ZETTAPASCAL"] = \
        lambda value: (10 ** -3) * value
    CONVERSIONS["FEMTOPASCAL:ATMOSPHERE"] = \
        lambda: noconversion("FEMTOPASCAL", "ATMOSPHERE")
    CONVERSIONS["FEMTOPASCAL:ATTOPASCAL"] = \
        lambda value: (10 ** 3) * value
    CONVERSIONS["FEMTOPASCAL:BAR"] = \
        lambda: noconversion("FEMTOPASCAL", "BAR")
    CONVERSIONS["FEMTOPASCAL:CENTIBAR"] = \
        lambda: noconversion("FEMTOPASCAL", "CENTIBAR")
    CONVERSIONS["FEMTOPASCAL:CENTIPASCAL"] = \
        lambda value: (10 ** -13) * value
    CONVERSIONS["FEMTOPASCAL:DECAPASCAL"] = \
        lambda value: (10 ** -16) * value
    CONVERSIONS["FEMTOPASCAL:DECIBAR"] = \
        lambda: noconversion("FEMTOPASCAL", "DECIBAR")
    CONVERSIONS["FEMTOPASCAL:DECIPASCAL"] = \
        lambda value: (10 ** -14) * value
    CONVERSIONS["FEMTOPASCAL:EXAPASCAL"] = \
        lambda value: (10 ** -33) * value
    CONVERSIONS["FEMTOPASCAL:GIGAPASCAL"] = \
        lambda value: (10 ** -24) * value
    CONVERSIONS["FEMTOPASCAL:HECTOPASCAL"] = \
        lambda value: (10 ** -17) * value
    CONVERSIONS["FEMTOPASCAL:KILOBAR"] = \
        lambda: noconversion("FEMTOPASCAL", "KILOBAR")
    CONVERSIONS["FEMTOPASCAL:KILOPASCAL"] = \
        lambda value: (10 ** -18) * value
    CONVERSIONS["FEMTOPASCAL:MEGABAR"] = \
        lambda: noconversion("FEMTOPASCAL", "MEGABAR")
    CONVERSIONS["FEMTOPASCAL:MEGAPASCAL"] = \
        lambda value: (10 ** -21) * value
    CONVERSIONS["FEMTOPASCAL:MICROPASCAL"] = \
        lambda value: (10 ** -9) * value
    CONVERSIONS["FEMTOPASCAL:MILLIBAR"] = \
        lambda: noconversion("FEMTOPASCAL", "MILLIBAR")
    CONVERSIONS["FEMTOPASCAL:MILLIPASCAL"] = \
        lambda value: (10 ** -12) * value
    CONVERSIONS["FEMTOPASCAL:MILLITORR"] = \
        lambda: noconversion("FEMTOPASCAL", "MILLITORR")
    CONVERSIONS["FEMTOPASCAL:MMHG"] = \
        lambda: noconversion("FEMTOPASCAL", "MMHG")
    CONVERSIONS["FEMTOPASCAL:NANOPASCAL"] = \
        lambda value: (10 ** -6) * value
    CONVERSIONS["FEMTOPASCAL:PETAPASCAL"] = \
        lambda value: (10 ** -30) * value
    CONVERSIONS["FEMTOPASCAL:PICOPASCAL"] = \
        lambda value: (10 ** -3) * value
    CONVERSIONS["FEMTOPASCAL:PSI"] = \
        lambda: noconversion("FEMTOPASCAL", "PSI")
    CONVERSIONS["FEMTOPASCAL:Pascal"] = \
        lambda value: (10 ** -15) * value
    CONVERSIONS["FEMTOPASCAL:TERAPASCAL"] = \
        lambda value: (10 ** -27) * value
    CONVERSIONS["FEMTOPASCAL:TORR"] = \
        lambda: noconversion("FEMTOPASCAL", "TORR")
    CONVERSIONS["FEMTOPASCAL:YOCTOPASCAL"] = \
        lambda value: (10 ** 9) * value
    CONVERSIONS["FEMTOPASCAL:YOTTAPASCAL"] = \
        lambda value: (10 ** -39) * value
    CONVERSIONS["FEMTOPASCAL:ZEPTOPASCAL"] = \
        lambda value: (10 ** 6) * value
    CONVERSIONS["FEMTOPASCAL:ZETTAPASCAL"] = \
        lambda value: (10 ** -36) * value
    CONVERSIONS["GIGAPASCAL:ATMOSPHERE"] = \
        lambda: noconversion("GIGAPASCAL", "ATMOSPHERE")
    CONVERSIONS["GIGAPASCAL:ATTOPASCAL"] = \
        lambda value: (10 ** 27) * value
    CONVERSIONS["GIGAPASCAL:BAR"] = \
        lambda: noconversion("GIGAPASCAL", "BAR")
    CONVERSIONS["GIGAPASCAL:CENTIBAR"] = \
        lambda: noconversion("GIGAPASCAL", "CENTIBAR")
    CONVERSIONS["GIGAPASCAL:CENTIPASCAL"] = \
        lambda value: (10 ** 11) * value
    CONVERSIONS["GIGAPASCAL:DECAPASCAL"] = \
        lambda value: (10 ** 8) * value
    CONVERSIONS["GIGAPASCAL:DECIBAR"] = \
        lambda: noconversion("GIGAPASCAL", "DECIBAR")
    CONVERSIONS["GIGAPASCAL:DECIPASCAL"] = \
        lambda value: (10 ** 10) * value
    CONVERSIONS["GIGAPASCAL:EXAPASCAL"] = \
        lambda value: (10 ** -9) * value
    CONVERSIONS["GIGAPASCAL:FEMTOPASCAL"] = \
        lambda value: (10 ** 24) * value
    CONVERSIONS["GIGAPASCAL:HECTOPASCAL"] = \
        lambda value: (10 ** 7) * value
    CONVERSIONS["GIGAPASCAL:KILOBAR"] = \
        lambda: noconversion("GIGAPASCAL", "KILOBAR")
    CONVERSIONS["GIGAPASCAL:KILOPASCAL"] = \
        lambda value: (10 ** 6) * value
    CONVERSIONS["GIGAPASCAL:MEGABAR"] = \
        lambda: noconversion("GIGAPASCAL", "MEGABAR")
    CONVERSIONS["GIGAPASCAL:MEGAPASCAL"] = \
        lambda value: (10 ** 3) * value
    CONVERSIONS["GIGAPASCAL:MICROPASCAL"] = \
        lambda value: (10 ** 15) * value
    CONVERSIONS["GIGAPASCAL:MILLIBAR"] = \
        lambda: noconversion("GIGAPASCAL", "MILLIBAR")
    CONVERSIONS["GIGAPASCAL:MILLIPASCAL"] = \
        lambda value: (10 ** 12) * value
    CONVERSIONS["GIGAPASCAL:MILLITORR"] = \
        lambda: noconversion("GIGAPASCAL", "MILLITORR")
    CONVERSIONS["GIGAPASCAL:MMHG"] = \
        lambda: noconversion("GIGAPASCAL", "MMHG")
    CONVERSIONS["GIGAPASCAL:NANOPASCAL"] = \
        lambda value: (10 ** 18) * value
    CONVERSIONS["GIGAPASCAL:PETAPASCAL"] = \
        lambda value: (10 ** -6) * value
    CONVERSIONS["GIGAPASCAL:PICOPASCAL"] = \
        lambda value: (10 ** 21) * value
    CONVERSIONS["GIGAPASCAL:PSI"] = \
        lambda: noconversion("GIGAPASCAL", "PSI")
    CONVERSIONS["GIGAPASCAL:Pascal"] = \
        lambda value: (10 ** 9) * value
    CONVERSIONS["GIGAPASCAL:TERAPASCAL"] = \
        lambda value: (10 ** -3) * value
    CONVERSIONS["GIGAPASCAL:TORR"] = \
        lambda: noconversion("GIGAPASCAL", "TORR")
    CONVERSIONS["GIGAPASCAL:YOCTOPASCAL"] = \
        lambda value: (10 ** 33) * value
    CONVERSIONS["GIGAPASCAL:YOTTAPASCAL"] = \
        lambda value: (10 ** -15) * value
    CONVERSIONS["GIGAPASCAL:ZEPTOPASCAL"] = \
        lambda value: (10 ** 30) * value
    CONVERSIONS["GIGAPASCAL:ZETTAPASCAL"] = \
        lambda value: (10 ** -12) * value
    CONVERSIONS["HECTOPASCAL:ATMOSPHERE"] = \
        lambda: noconversion("HECTOPASCAL", "ATMOSPHERE")
    CONVERSIONS["HECTOPASCAL:ATTOPASCAL"] = \
        lambda value: (10 ** 20) * value
    CONVERSIONS["HECTOPASCAL:BAR"] = \
        lambda: noconversion("HECTOPASCAL", "BAR")
    CONVERSIONS["HECTOPASCAL:CENTIBAR"] = \
        lambda: noconversion("HECTOPASCAL", "CENTIBAR")
    CONVERSIONS["HECTOPASCAL:CENTIPASCAL"] = \
        lambda value: (10 ** 4) * value
    CONVERSIONS["HECTOPASCAL:DECAPASCAL"] = \
        lambda value: 10 * value
    CONVERSIONS["HECTOPASCAL:DECIBAR"] = \
        lambda: noconversion("HECTOPASCAL", "DECIBAR")
    CONVERSIONS["HECTOPASCAL:DECIPASCAL"] = \
        lambda value: (10 ** 3) * value
    CONVERSIONS["HECTOPASCAL:EXAPASCAL"] = \
        lambda value: (10 ** -16) * value
    CONVERSIONS["HECTOPASCAL:FEMTOPASCAL"] = \
        lambda value: (10 ** 17) * value
    CONVERSIONS["HECTOPASCAL:GIGAPASCAL"] = \
        lambda value: (10 ** -7) * value
    CONVERSIONS["HECTOPASCAL:KILOBAR"] = \
        lambda: noconversion("HECTOPASCAL", "KILOBAR")
    CONVERSIONS["HECTOPASCAL:KILOPASCAL"] = \
        lambda value: (10 ** -1) * value
    CONVERSIONS["HECTOPASCAL:MEGABAR"] = \
        lambda: noconversion("HECTOPASCAL", "MEGABAR")
    CONVERSIONS["HECTOPASCAL:MEGAPASCAL"] = \
        lambda value: (10 ** -4) * value
    CONVERSIONS["HECTOPASCAL:MICROPASCAL"] = \
        lambda value: (10 ** 8) * value
    CONVERSIONS["HECTOPASCAL:MILLIBAR"] = \
        lambda: noconversion("HECTOPASCAL", "MILLIBAR")
    CONVERSIONS["HECTOPASCAL:MILLIPASCAL"] = \
        lambda value: (10 ** 5) * value
    CONVERSIONS["HECTOPASCAL:MILLITORR"] = \
        lambda: noconversion("HECTOPASCAL", "MILLITORR")
    CONVERSIONS["HECTOPASCAL:MMHG"] = \
        lambda: noconversion("HECTOPASCAL", "MMHG")
    CONVERSIONS["HECTOPASCAL:NANOPASCAL"] = \
        lambda value: (10 ** 11) * value
    CONVERSIONS["HECTOPASCAL:PETAPASCAL"] = \
        lambda value: (10 ** -13) * value
    CONVERSIONS["HECTOPASCAL:PICOPASCAL"] = \
        lambda value: (10 ** 14) * value
    CONVERSIONS["HECTOPASCAL:PSI"] = \
        lambda: noconversion("HECTOPASCAL", "PSI")
    CONVERSIONS["HECTOPASCAL:Pascal"] = \
        lambda value: (10 ** 2) * value
    CONVERSIONS["HECTOPASCAL:TERAPASCAL"] = \
        lambda value: (10 ** -10) * value
    CONVERSIONS["HECTOPASCAL:TORR"] = \
        lambda: noconversion("HECTOPASCAL", "TORR")
    CONVERSIONS["HECTOPASCAL:YOCTOPASCAL"] = \
        lambda value: (10 ** 26) * value
    CONVERSIONS["HECTOPASCAL:YOTTAPASCAL"] = \
        lambda value: (10 ** -22) * value
    CONVERSIONS["HECTOPASCAL:ZEPTOPASCAL"] = \
        lambda value: (10 ** 23) * value
    CONVERSIONS["HECTOPASCAL:ZETTAPASCAL"] = \
        lambda value: (10 ** -19) * value
    CONVERSIONS["KILOBAR:ATMOSPHERE"] = \
        lambda: noconversion("KILOBAR", "ATMOSPHERE")
    CONVERSIONS["KILOBAR:ATTOPASCAL"] = \
        lambda: noconversion("KILOBAR", "ATTOPASCAL")
    CONVERSIONS["KILOBAR:BAR"] = \
        lambda: noconversion("KILOBAR", "BAR")
    CONVERSIONS["KILOBAR:CENTIBAR"] = \
        lambda: noconversion("KILOBAR", "CENTIBAR")
    CONVERSIONS["KILOBAR:CENTIPASCAL"] = \
        lambda: noconversion("KILOBAR", "CENTIPASCAL")
    CONVERSIONS["KILOBAR:DECAPASCAL"] = \
        lambda: noconversion("KILOBAR", "DECAPASCAL")
    CONVERSIONS["KILOBAR:DECIBAR"] = \
        lambda: noconversion("KILOBAR", "DECIBAR")
    CONVERSIONS["KILOBAR:DECIPASCAL"] = \
        lambda: noconversion("KILOBAR", "DECIPASCAL")
    CONVERSIONS["KILOBAR:EXAPASCAL"] = \
        lambda: noconversion("KILOBAR", "EXAPASCAL")
    CONVERSIONS["KILOBAR:FEMTOPASCAL"] = \
        lambda: noconversion("KILOBAR", "FEMTOPASCAL")
    CONVERSIONS["KILOBAR:GIGAPASCAL"] = \
        lambda: noconversion("KILOBAR", "GIGAPASCAL")
    CONVERSIONS["KILOBAR:HECTOPASCAL"] = \
        lambda: noconversion("KILOBAR", "HECTOPASCAL")
    CONVERSIONS["KILOBAR:KILOPASCAL"] = \
        lambda: noconversion("KILOBAR", "KILOPASCAL")
    CONVERSIONS["KILOBAR:MEGABAR"] = \
        lambda: noconversion("KILOBAR", "MEGABAR")
    CONVERSIONS["KILOBAR:MEGAPASCAL"] = \
        lambda: noconversion("KILOBAR", "MEGAPASCAL")
    CONVERSIONS["KILOBAR:MICROPASCAL"] = \
        lambda: noconversion("KILOBAR", "MICROPASCAL")
    CONVERSIONS["KILOBAR:MILLIBAR"] = \
        lambda: noconversion("KILOBAR", "MILLIBAR")
    CONVERSIONS["KILOBAR:MILLIPASCAL"] = \
        lambda: noconversion("KILOBAR", "MILLIPASCAL")
    CONVERSIONS["KILOBAR:MILLITORR"] = \
        lambda: noconversion("KILOBAR", "MILLITORR")
    CONVERSIONS["KILOBAR:MMHG"] = \
        lambda: noconversion("KILOBAR", "MMHG")
    CONVERSIONS["KILOBAR:NANOPASCAL"] = \
        lambda: noconversion("KILOBAR", "NANOPASCAL")
    CONVERSIONS["KILOBAR:PETAPASCAL"] = \
        lambda: noconversion("KILOBAR", "PETAPASCAL")
    CONVERSIONS["KILOBAR:PICOPASCAL"] = \
        lambda: noconversion("KILOBAR", "PICOPASCAL")
    CONVERSIONS["KILOBAR:PSI"] = \
        lambda: noconversion("KILOBAR", "PSI")
    CONVERSIONS["KILOBAR:Pascal"] = \
        lambda: noconversion("KILOBAR", "Pascal")
    CONVERSIONS["KILOBAR:TERAPASCAL"] = \
        lambda: noconversion("KILOBAR", "TERAPASCAL")
    CONVERSIONS["KILOBAR:TORR"] = \
        lambda: noconversion("KILOBAR", "TORR")
    CONVERSIONS["KILOBAR:YOCTOPASCAL"] = \
        lambda: noconversion("KILOBAR", "YOCTOPASCAL")
    CONVERSIONS["KILOBAR:YOTTAPASCAL"] = \
        lambda: noconversion("KILOBAR", "YOTTAPASCAL")
    CONVERSIONS["KILOBAR:ZEPTOPASCAL"] = \
        lambda: noconversion("KILOBAR", "ZEPTOPASCAL")
    CONVERSIONS["KILOBAR:ZETTAPASCAL"] = \
        lambda: noconversion("KILOBAR", "ZETTAPASCAL")
    CONVERSIONS["KILOPASCAL:ATMOSPHERE"] = \
        lambda: noconversion("KILOPASCAL", "ATMOSPHERE")
    CONVERSIONS["KILOPASCAL:ATTOPASCAL"] = \
        lambda value: (10 ** 21) * value
    CONVERSIONS["KILOPASCAL:BAR"] = \
        lambda: noconversion("KILOPASCAL", "BAR")
    CONVERSIONS["KILOPASCAL:CENTIBAR"] = \
        lambda: noconversion("KILOPASCAL", "CENTIBAR")
    CONVERSIONS["KILOPASCAL:CENTIPASCAL"] = \
        lambda value: (10 ** 5) * value
    CONVERSIONS["KILOPASCAL:DECAPASCAL"] = \
        lambda value: (10 ** 2) * value
    CONVERSIONS["KILOPASCAL:DECIBAR"] = \
        lambda: noconversion("KILOPASCAL", "DECIBAR")
    CONVERSIONS["KILOPASCAL:DECIPASCAL"] = \
        lambda value: (10 ** 4) * value
    CONVERSIONS["KILOPASCAL:EXAPASCAL"] = \
        lambda value: (10 ** -15) * value
    CONVERSIONS["KILOPASCAL:FEMTOPASCAL"] = \
        lambda value: (10 ** 18) * value
    CONVERSIONS["KILOPASCAL:GIGAPASCAL"] = \
        lambda value: (10 ** -6) * value
    CONVERSIONS["KILOPASCAL:HECTOPASCAL"] = \
        lambda value: 10 * value
    CONVERSIONS["KILOPASCAL:KILOBAR"] = \
        lambda: noconversion("KILOPASCAL", "KILOBAR")
    CONVERSIONS["KILOPASCAL:MEGABAR"] = \
        lambda: noconversion("KILOPASCAL", "MEGABAR")
    CONVERSIONS["KILOPASCAL:MEGAPASCAL"] = \
        lambda value: (10 ** -3) * value
    CONVERSIONS["KILOPASCAL:MICROPASCAL"] = \
        lambda value: (10 ** 9) * value
    CONVERSIONS["KILOPASCAL:MILLIBAR"] = \
        lambda: noconversion("KILOPASCAL", "MILLIBAR")
    CONVERSIONS["KILOPASCAL:MILLIPASCAL"] = \
        lambda value: (10 ** 6) * value
    CONVERSIONS["KILOPASCAL:MILLITORR"] = \
        lambda: noconversion("KILOPASCAL", "MILLITORR")
    CONVERSIONS["KILOPASCAL:MMHG"] = \
        lambda: noconversion("KILOPASCAL", "MMHG")
    CONVERSIONS["KILOPASCAL:NANOPASCAL"] = \
        lambda value: (10 ** 12) * value
    CONVERSIONS["KILOPASCAL:PETAPASCAL"] = \
        lambda value: (10 ** -12) * value
    CONVERSIONS["KILOPASCAL:PICOPASCAL"] = \
        lambda value: (10 ** 15) * value
    CONVERSIONS["KILOPASCAL:PSI"] = \
        lambda: noconversion("KILOPASCAL", "PSI")
    CONVERSIONS["KILOPASCAL:Pascal"] = \
        lambda value: (10 ** 3) * value
    CONVERSIONS["KILOPASCAL:TERAPASCAL"] = \
        lambda value: (10 ** -9) * value
    CONVERSIONS["KILOPASCAL:TORR"] = \
        lambda: noconversion("KILOPASCAL", "TORR")
    CONVERSIONS["KILOPASCAL:YOCTOPASCAL"] = \
        lambda value: (10 ** 27) * value
    CONVERSIONS["KILOPASCAL:YOTTAPASCAL"] = \
        lambda value: (10 ** -21) * value
    CONVERSIONS["KILOPASCAL:ZEPTOPASCAL"] = \
        lambda value: (10 ** 24) * value
    CONVERSIONS["KILOPASCAL:ZETTAPASCAL"] = \
        lambda value: (10 ** -18) * value
    CONVERSIONS["MEGABAR:ATMOSPHERE"] = \
        lambda: noconversion("MEGABAR", "ATMOSPHERE")
    CONVERSIONS["MEGABAR:ATTOPASCAL"] = \
        lambda: noconversion("MEGABAR", "ATTOPASCAL")
    CONVERSIONS["MEGABAR:BAR"] = \
        lambda: noconversion("MEGABAR", "BAR")
    CONVERSIONS["MEGABAR:CENTIBAR"] = \
        lambda: noconversion("MEGABAR", "CENTIBAR")
    CONVERSIONS["MEGABAR:CENTIPASCAL"] = \
        lambda: noconversion("MEGABAR", "CENTIPASCAL")
    CONVERSIONS["MEGABAR:DECAPASCAL"] = \
        lambda: noconversion("MEGABAR", "DECAPASCAL")
    CONVERSIONS["MEGABAR:DECIBAR"] = \
        lambda: noconversion("MEGABAR", "DECIBAR")
    CONVERSIONS["MEGABAR:DECIPASCAL"] = \
        lambda: noconversion("MEGABAR", "DECIPASCAL")
    CONVERSIONS["MEGABAR:EXAPASCAL"] = \
        lambda: noconversion("MEGABAR", "EXAPASCAL")
    CONVERSIONS["MEGABAR:FEMTOPASCAL"] = \
        lambda: noconversion("MEGABAR", "FEMTOPASCAL")
    CONVERSIONS["MEGABAR:GIGAPASCAL"] = \
        lambda: noconversion("MEGABAR", "GIGAPASCAL")
    CONVERSIONS["MEGABAR:HECTOPASCAL"] = \
        lambda: noconversion("MEGABAR", "HECTOPASCAL")
    CONVERSIONS["MEGABAR:KILOBAR"] = \
        lambda: noconversion("MEGABAR", "KILOBAR")
    CONVERSIONS["MEGABAR:KILOPASCAL"] = \
        lambda: noconversion("MEGABAR", "KILOPASCAL")
    CONVERSIONS["MEGABAR:MEGAPASCAL"] = \
        lambda: noconversion("MEGABAR", "MEGAPASCAL")
    CONVERSIONS["MEGABAR:MICROPASCAL"] = \
        lambda: noconversion("MEGABAR", "MICROPASCAL")
    CONVERSIONS["MEGABAR:MILLIBAR"] = \
        lambda: noconversion("MEGABAR", "MILLIBAR")
    CONVERSIONS["MEGABAR:MILLIPASCAL"] = \
        lambda: noconversion("MEGABAR", "MILLIPASCAL")
    CONVERSIONS["MEGABAR:MILLITORR"] = \
        lambda: noconversion("MEGABAR", "MILLITORR")
    CONVERSIONS["MEGABAR:MMHG"] = \
        lambda: noconversion("MEGABAR", "MMHG")
    CONVERSIONS["MEGABAR:NANOPASCAL"] = \
        lambda: noconversion("MEGABAR", "NANOPASCAL")
    CONVERSIONS["MEGABAR:PETAPASCAL"] = \
        lambda: noconversion("MEGABAR", "PETAPASCAL")
    CONVERSIONS["MEGABAR:PICOPASCAL"] = \
        lambda: noconversion("MEGABAR", "PICOPASCAL")
    CONVERSIONS["MEGABAR:PSI"] = \
        lambda: noconversion("MEGABAR", "PSI")
    CONVERSIONS["MEGABAR:Pascal"] = \
        lambda: noconversion("MEGABAR", "Pascal")
    CONVERSIONS["MEGABAR:TERAPASCAL"] = \
        lambda: noconversion("MEGABAR", "TERAPASCAL")
    CONVERSIONS["MEGABAR:TORR"] = \
        lambda: noconversion("MEGABAR", "TORR")
    CONVERSIONS["MEGABAR:YOCTOPASCAL"] = \
        lambda: noconversion("MEGABAR", "YOCTOPASCAL")
    CONVERSIONS["MEGABAR:YOTTAPASCAL"] = \
        lambda: noconversion("MEGABAR", "YOTTAPASCAL")
    CONVERSIONS["MEGABAR:ZEPTOPASCAL"] = \
        lambda: noconversion("MEGABAR", "ZEPTOPASCAL")
    CONVERSIONS["MEGABAR:ZETTAPASCAL"] = \
        lambda: noconversion("MEGABAR", "ZETTAPASCAL")
    CONVERSIONS["MEGAPASCAL:ATMOSPHERE"] = \
        lambda: noconversion("MEGAPASCAL", "ATMOSPHERE")
    CONVERSIONS["MEGAPASCAL:ATTOPASCAL"] = \
        lambda value: (10 ** 24) * value
    CONVERSIONS["MEGAPASCAL:BAR"] = \
        lambda: noconversion("MEGAPASCAL", "BAR")
    CONVERSIONS["MEGAPASCAL:CENTIBAR"] = \
        lambda: noconversion("MEGAPASCAL", "CENTIBAR")
    CONVERSIONS["MEGAPASCAL:CENTIPASCAL"] = \
        lambda value: (10 ** 8) * value
    CONVERSIONS["MEGAPASCAL:DECAPASCAL"] = \
        lambda value: (10 ** 5) * value
    CONVERSIONS["MEGAPASCAL:DECIBAR"] = \
        lambda: noconversion("MEGAPASCAL", "DECIBAR")
    CONVERSIONS["MEGAPASCAL:DECIPASCAL"] = \
        lambda value: (10 ** 7) * value
    CONVERSIONS["MEGAPASCAL:EXAPASCAL"] = \
        lambda value: (10 ** -12) * value
    CONVERSIONS["MEGAPASCAL:FEMTOPASCAL"] = \
        lambda value: (10 ** 21) * value
    CONVERSIONS["MEGAPASCAL:GIGAPASCAL"] = \
        lambda value: (10 ** -3) * value
    CONVERSIONS["MEGAPASCAL:HECTOPASCAL"] = \
        lambda value: (10 ** 4) * value
    CONVERSIONS["MEGAPASCAL:KILOBAR"] = \
        lambda: noconversion("MEGAPASCAL", "KILOBAR")
    CONVERSIONS["MEGAPASCAL:KILOPASCAL"] = \
        lambda value: (10 ** 3) * value
    CONVERSIONS["MEGAPASCAL:MEGABAR"] = \
        lambda: noconversion("MEGAPASCAL", "MEGABAR")
    CONVERSIONS["MEGAPASCAL:MICROPASCAL"] = \
        lambda value: (10 ** 12) * value
    CONVERSIONS["MEGAPASCAL:MILLIBAR"] = \
        lambda: noconversion("MEGAPASCAL", "MILLIBAR")
    CONVERSIONS["MEGAPASCAL:MILLIPASCAL"] = \
        lambda value: (10 ** 9) * value
    CONVERSIONS["MEGAPASCAL:MILLITORR"] = \
        lambda: noconversion("MEGAPASCAL", "MILLITORR")
    CONVERSIONS["MEGAPASCAL:MMHG"] = \
        lambda: noconversion("MEGAPASCAL", "MMHG")
    CONVERSIONS["MEGAPASCAL:NANOPASCAL"] = \
        lambda value: (10 ** 15) * value
    CONVERSIONS["MEGAPASCAL:PETAPASCAL"] = \
        lambda value: (10 ** -9) * value
    CONVERSIONS["MEGAPASCAL:PICOPASCAL"] = \
        lambda value: (10 ** 18) * value
    CONVERSIONS["MEGAPASCAL:PSI"] = \
        lambda: noconversion("MEGAPASCAL", "PSI")
    CONVERSIONS["MEGAPASCAL:Pascal"] = \
        lambda value: (10 ** 6) * value
    CONVERSIONS["MEGAPASCAL:TERAPASCAL"] = \
        lambda value: (10 ** -6) * value
    CONVERSIONS["MEGAPASCAL:TORR"] = \
        lambda: noconversion("MEGAPASCAL", "TORR")
    CONVERSIONS["MEGAPASCAL:YOCTOPASCAL"] = \
        lambda value: (10 ** 30) * value
    CONVERSIONS["MEGAPASCAL:YOTTAPASCAL"] = \
        lambda value: (10 ** -18) * value
    CONVERSIONS["MEGAPASCAL:ZEPTOPASCAL"] = \
        lambda value: (10 ** 27) * value
    CONVERSIONS["MEGAPASCAL:ZETTAPASCAL"] = \
        lambda value: (10 ** -15) * value
    CONVERSIONS["MICROPASCAL:ATMOSPHERE"] = \
        lambda: noconversion("MICROPASCAL", "ATMOSPHERE")
    CONVERSIONS["MICROPASCAL:ATTOPASCAL"] = \
        lambda value: (10 ** 12) * value
    CONVERSIONS["MICROPASCAL:BAR"] = \
        lambda: noconversion("MICROPASCAL", "BAR")
    CONVERSIONS["MICROPASCAL:CENTIBAR"] = \
        lambda: noconversion("MICROPASCAL", "CENTIBAR")
    CONVERSIONS["MICROPASCAL:CENTIPASCAL"] = \
        lambda value: (10 ** -4) * value
    CONVERSIONS["MICROPASCAL:DECAPASCAL"] = \
        lambda value: (10 ** -7) * value
    CONVERSIONS["MICROPASCAL:DECIBAR"] = \
        lambda: noconversion("MICROPASCAL", "DECIBAR")
    CONVERSIONS["MICROPASCAL:DECIPASCAL"] = \
        lambda value: (10 ** -5) * value
    CONVERSIONS["MICROPASCAL:EXAPASCAL"] = \
        lambda value: (10 ** -24) * value
    CONVERSIONS["MICROPASCAL:FEMTOPASCAL"] = \
        lambda value: (10 ** 9) * value
    CONVERSIONS["MICROPASCAL:GIGAPASCAL"] = \
        lambda value: (10 ** -15) * value
    CONVERSIONS["MICROPASCAL:HECTOPASCAL"] = \
        lambda value: (10 ** -8) * value
    CONVERSIONS["MICROPASCAL:KILOBAR"] = \
        lambda: noconversion("MICROPASCAL", "KILOBAR")
    CONVERSIONS["MICROPASCAL:KILOPASCAL"] = \
        lambda value: (10 ** -9) * value
    CONVERSIONS["MICROPASCAL:MEGABAR"] = \
        lambda: noconversion("MICROPASCAL", "MEGABAR")
    CONVERSIONS["MICROPASCAL:MEGAPASCAL"] = \
        lambda value: (10 ** -12) * value
    CONVERSIONS["MICROPASCAL:MILLIBAR"] = \
        lambda: noconversion("MICROPASCAL", "MILLIBAR")
    CONVERSIONS["MICROPASCAL:MILLIPASCAL"] = \
        lambda value: (10 ** -3) * value
    CONVERSIONS["MICROPASCAL:MILLITORR"] = \
        lambda: noconversion("MICROPASCAL", "MILLITORR")
    CONVERSIONS["MICROPASCAL:MMHG"] = \
        lambda: noconversion("MICROPASCAL", "MMHG")
    CONVERSIONS["MICROPASCAL:NANOPASCAL"] = \
        lambda value: (10 ** 3) * value
    CONVERSIONS["MICROPASCAL:PETAPASCAL"] = \
        lambda value: (10 ** -21) * value
    CONVERSIONS["MICROPASCAL:PICOPASCAL"] = \
        lambda value: (10 ** 6) * value
    CONVERSIONS["MICROPASCAL:PSI"] = \
        lambda: noconversion("MICROPASCAL", "PSI")
    CONVERSIONS["MICROPASCAL:Pascal"] = \
        lambda value: (10 ** -6) * value
    CONVERSIONS["MICROPASCAL:TERAPASCAL"] = \
        lambda value: (10 ** -18) * value
    CONVERSIONS["MICROPASCAL:TORR"] = \
        lambda: noconversion("MICROPASCAL", "TORR")
    CONVERSIONS["MICROPASCAL:YOCTOPASCAL"] = \
        lambda value: (10 ** 18) * value
    CONVERSIONS["MICROPASCAL:YOTTAPASCAL"] = \
        lambda value: (10 ** -30) * value
    CONVERSIONS["MICROPASCAL:ZEPTOPASCAL"] = \
        lambda value: (10 ** 15) * value
    CONVERSIONS["MICROPASCAL:ZETTAPASCAL"] = \
        lambda value: (10 ** -27) * value
    CONVERSIONS["MILLIBAR:ATMOSPHERE"] = \
        lambda: noconversion("MILLIBAR", "ATMOSPHERE")
    CONVERSIONS["MILLIBAR:ATTOPASCAL"] = \
        lambda: noconversion("MILLIBAR", "ATTOPASCAL")
    CONVERSIONS["MILLIBAR:BAR"] = \
        lambda: noconversion("MILLIBAR", "BAR")
    CONVERSIONS["MILLIBAR:CENTIBAR"] = \
        lambda: noconversion("MILLIBAR", "CENTIBAR")
    CONVERSIONS["MILLIBAR:CENTIPASCAL"] = \
        lambda: noconversion("MILLIBAR", "CENTIPASCAL")
    CONVERSIONS["MILLIBAR:DECAPASCAL"] = \
        lambda: noconversion("MILLIBAR", "DECAPASCAL")
    CONVERSIONS["MILLIBAR:DECIBAR"] = \
        lambda: noconversion("MILLIBAR", "DECIBAR")
    CONVERSIONS["MILLIBAR:DECIPASCAL"] = \
        lambda: noconversion("MILLIBAR", "DECIPASCAL")
    CONVERSIONS["MILLIBAR:EXAPASCAL"] = \
        lambda: noconversion("MILLIBAR", "EXAPASCAL")
    CONVERSIONS["MILLIBAR:FEMTOPASCAL"] = \
        lambda: noconversion("MILLIBAR", "FEMTOPASCAL")
    CONVERSIONS["MILLIBAR:GIGAPASCAL"] = \
        lambda: noconversion("MILLIBAR", "GIGAPASCAL")
    CONVERSIONS["MILLIBAR:HECTOPASCAL"] = \
        lambda: noconversion("MILLIBAR", "HECTOPASCAL")
    CONVERSIONS["MILLIBAR:KILOBAR"] = \
        lambda: noconversion("MILLIBAR", "KILOBAR")
    CONVERSIONS["MILLIBAR:KILOPASCAL"] = \
        lambda: noconversion("MILLIBAR", "KILOPASCAL")
    CONVERSIONS["MILLIBAR:MEGABAR"] = \
        lambda: noconversion("MILLIBAR", "MEGABAR")
    CONVERSIONS["MILLIBAR:MEGAPASCAL"] = \
        lambda: noconversion("MILLIBAR", "MEGAPASCAL")
    CONVERSIONS["MILLIBAR:MICROPASCAL"] = \
        lambda: noconversion("MILLIBAR", "MICROPASCAL")
    CONVERSIONS["MILLIBAR:MILLIPASCAL"] = \
        lambda: noconversion("MILLIBAR", "MILLIPASCAL")
    CONVERSIONS["MILLIBAR:MILLITORR"] = \
        lambda: noconversion("MILLIBAR", "MILLITORR")
    CONVERSIONS["MILLIBAR:MMHG"] = \
        lambda: noconversion("MILLIBAR", "MMHG")
    CONVERSIONS["MILLIBAR:NANOPASCAL"] = \
        lambda: noconversion("MILLIBAR", "NANOPASCAL")
    CONVERSIONS["MILLIBAR:PETAPASCAL"] = \
        lambda: noconversion("MILLIBAR", "PETAPASCAL")
    CONVERSIONS["MILLIBAR:PICOPASCAL"] = \
        lambda: noconversion("MILLIBAR", "PICOPASCAL")
    CONVERSIONS["MILLIBAR:PSI"] = \
        lambda: noconversion("MILLIBAR", "PSI")
    CONVERSIONS["MILLIBAR:Pascal"] = \
        lambda: noconversion("MILLIBAR", "Pascal")
    CONVERSIONS["MILLIBAR:TERAPASCAL"] = \
        lambda: noconversion("MILLIBAR", "TERAPASCAL")
    CONVERSIONS["MILLIBAR:TORR"] = \
        lambda: noconversion("MILLIBAR", "TORR")
    CONVERSIONS["MILLIBAR:YOCTOPASCAL"] = \
        lambda: noconversion("MILLIBAR", "YOCTOPASCAL")
    CONVERSIONS["MILLIBAR:YOTTAPASCAL"] = \
        lambda: noconversion("MILLIBAR", "YOTTAPASCAL")
    CONVERSIONS["MILLIBAR:ZEPTOPASCAL"] = \
        lambda: noconversion("MILLIBAR", "ZEPTOPASCAL")
    CONVERSIONS["MILLIBAR:ZETTAPASCAL"] = \
        lambda: noconversion("MILLIBAR", "ZETTAPASCAL")
    CONVERSIONS["MILLIPASCAL:ATMOSPHERE"] = \
        lambda: noconversion("MILLIPASCAL", "ATMOSPHERE")
    CONVERSIONS["MILLIPASCAL:ATTOPASCAL"] = \
        lambda value: (10 ** 15) * value
    CONVERSIONS["MILLIPASCAL:BAR"] = \
        lambda: noconversion("MILLIPASCAL", "BAR")
    CONVERSIONS["MILLIPASCAL:CENTIBAR"] = \
        lambda: noconversion("MILLIPASCAL", "CENTIBAR")
    CONVERSIONS["MILLIPASCAL:CENTIPASCAL"] = \
        lambda value: (10 ** -1) * value
    CONVERSIONS["MILLIPASCAL:DECAPASCAL"] = \
        lambda value: (10 ** -4) * value
    CONVERSIONS["MILLIPASCAL:DECIBAR"] = \
        lambda: noconversion("MILLIPASCAL", "DECIBAR")
    CONVERSIONS["MILLIPASCAL:DECIPASCAL"] = \
        lambda value: (10 ** -2) * value
    CONVERSIONS["MILLIPASCAL:EXAPASCAL"] = \
        lambda value: (10 ** -21) * value
    CONVERSIONS["MILLIPASCAL:FEMTOPASCAL"] = \
        lambda value: (10 ** 12) * value
    CONVERSIONS["MILLIPASCAL:GIGAPASCAL"] = \
        lambda value: (10 ** -12) * value
    CONVERSIONS["MILLIPASCAL:HECTOPASCAL"] = \
        lambda value: (10 ** -5) * value
    CONVERSIONS["MILLIPASCAL:KILOBAR"] = \
        lambda: noconversion("MILLIPASCAL", "KILOBAR")
    CONVERSIONS["MILLIPASCAL:KILOPASCAL"] = \
        lambda value: (10 ** -6) * value
    CONVERSIONS["MILLIPASCAL:MEGABAR"] = \
        lambda: noconversion("MILLIPASCAL", "MEGABAR")
    CONVERSIONS["MILLIPASCAL:MEGAPASCAL"] = \
        lambda value: (10 ** -9) * value
    CONVERSIONS["MILLIPASCAL:MICROPASCAL"] = \
        lambda value: (10 ** 3) * value
    CONVERSIONS["MILLIPASCAL:MILLIBAR"] = \
        lambda: noconversion("MILLIPASCAL", "MILLIBAR")
    CONVERSIONS["MILLIPASCAL:MILLITORR"] = \
        lambda: noconversion("MILLIPASCAL", "MILLITORR")
    CONVERSIONS["MILLIPASCAL:MMHG"] = \
        lambda: noconversion("MILLIPASCAL", "MMHG")
    CONVERSIONS["MILLIPASCAL:NANOPASCAL"] = \
        lambda value: (10 ** 6) * value
    CONVERSIONS["MILLIPASCAL:PETAPASCAL"] = \
        lambda value: (10 ** -18) * value
    CONVERSIONS["MILLIPASCAL:PICOPASCAL"] = \
        lambda value: (10 ** 9) * value
    CONVERSIONS["MILLIPASCAL:PSI"] = \
        lambda: noconversion("MILLIPASCAL", "PSI")
    CONVERSIONS["MILLIPASCAL:Pascal"] = \
        lambda value: (10 ** -3) * value
    CONVERSIONS["MILLIPASCAL:TERAPASCAL"] = \
        lambda value: (10 ** -15) * value
    CONVERSIONS["MILLIPASCAL:TORR"] = \
        lambda: noconversion("MILLIPASCAL", "TORR")
    CONVERSIONS["MILLIPASCAL:YOCTOPASCAL"] = \
        lambda value: (10 ** 21) * value
    CONVERSIONS["MILLIPASCAL:YOTTAPASCAL"] = \
        lambda value: (10 ** -27) * value
    CONVERSIONS["MILLIPASCAL:ZEPTOPASCAL"] = \
        lambda value: (10 ** 18) * value
    CONVERSIONS["MILLIPASCAL:ZETTAPASCAL"] = \
        lambda value: (10 ** -24) * value
    CONVERSIONS["MILLITORR:ATMOSPHERE"] = \
        lambda: noconversion("MILLITORR", "ATMOSPHERE")
    CONVERSIONS["MILLITORR:ATTOPASCAL"] = \
        lambda: noconversion("MILLITORR", "ATTOPASCAL")
    CONVERSIONS["MILLITORR:BAR"] = \
        lambda: noconversion("MILLITORR", "BAR")
    CONVERSIONS["MILLITORR:CENTIBAR"] = \
        lambda: noconversion("MILLITORR", "CENTIBAR")
    CONVERSIONS["MILLITORR:CENTIPASCAL"] = \
        lambda: noconversion("MILLITORR", "CENTIPASCAL")
    CONVERSIONS["MILLITORR:DECAPASCAL"] = \
        lambda: noconversion("MILLITORR", "DECAPASCAL")
    CONVERSIONS["MILLITORR:DECIBAR"] = \
        lambda: noconversion("MILLITORR", "DECIBAR")
    CONVERSIONS["MILLITORR:DECIPASCAL"] = \
        lambda: noconversion("MILLITORR", "DECIPASCAL")
    CONVERSIONS["MILLITORR:EXAPASCAL"] = \
        lambda: noconversion("MILLITORR", "EXAPASCAL")
    CONVERSIONS["MILLITORR:FEMTOPASCAL"] = \
        lambda: noconversion("MILLITORR", "FEMTOPASCAL")
    CONVERSIONS["MILLITORR:GIGAPASCAL"] = \
        lambda: noconversion("MILLITORR", "GIGAPASCAL")
    CONVERSIONS["MILLITORR:HECTOPASCAL"] = \
        lambda: noconversion("MILLITORR", "HECTOPASCAL")
    CONVERSIONS["MILLITORR:KILOBAR"] = \
        lambda: noconversion("MILLITORR", "KILOBAR")
    CONVERSIONS["MILLITORR:KILOPASCAL"] = \
        lambda: noconversion("MILLITORR", "KILOPASCAL")
    CONVERSIONS["MILLITORR:MEGABAR"] = \
        lambda: noconversion("MILLITORR", "MEGABAR")
    CONVERSIONS["MILLITORR:MEGAPASCAL"] = \
        lambda: noconversion("MILLITORR", "MEGAPASCAL")
    CONVERSIONS["MILLITORR:MICROPASCAL"] = \
        lambda: noconversion("MILLITORR", "MICROPASCAL")
    CONVERSIONS["MILLITORR:MILLIBAR"] = \
        lambda: noconversion("MILLITORR", "MILLIBAR")
    CONVERSIONS["MILLITORR:MILLIPASCAL"] = \
        lambda: noconversion("MILLITORR", "MILLIPASCAL")
    CONVERSIONS["MILLITORR:MMHG"] = \
        lambda: noconversion("MILLITORR", "MMHG")
    CONVERSIONS["MILLITORR:NANOPASCAL"] = \
        lambda: noconversion("MILLITORR", "NANOPASCAL")
    CONVERSIONS["MILLITORR:PETAPASCAL"] = \
        lambda: noconversion("MILLITORR", "PETAPASCAL")
    CONVERSIONS["MILLITORR:PICOPASCAL"] = \
        lambda: noconversion("MILLITORR", "PICOPASCAL")
    CONVERSIONS["MILLITORR:PSI"] = \
        lambda: noconversion("MILLITORR", "PSI")
    CONVERSIONS["MILLITORR:Pascal"] = \
        lambda: noconversion("MILLITORR", "Pascal")
    CONVERSIONS["MILLITORR:TERAPASCAL"] = \
        lambda: noconversion("MILLITORR", "TERAPASCAL")
    CONVERSIONS["MILLITORR:TORR"] = \
        lambda: noconversion("MILLITORR", "TORR")
    CONVERSIONS["MILLITORR:YOCTOPASCAL"] = \
        lambda: noconversion("MILLITORR", "YOCTOPASCAL")
    CONVERSIONS["MILLITORR:YOTTAPASCAL"] = \
        lambda: noconversion("MILLITORR", "YOTTAPASCAL")
    CONVERSIONS["MILLITORR:ZEPTOPASCAL"] = \
        lambda: noconversion("MILLITORR", "ZEPTOPASCAL")
    CONVERSIONS["MILLITORR:ZETTAPASCAL"] = \
        lambda: noconversion("MILLITORR", "ZETTAPASCAL")
    CONVERSIONS["MMHG:ATMOSPHERE"] = \
        lambda: noconversion("MMHG", "ATMOSPHERE")
    CONVERSIONS["MMHG:ATTOPASCAL"] = \
        lambda: noconversion("MMHG", "ATTOPASCAL")
    CONVERSIONS["MMHG:BAR"] = \
        lambda: noconversion("MMHG", "BAR")
    CONVERSIONS["MMHG:CENTIBAR"] = \
        lambda: noconversion("MMHG", "CENTIBAR")
    CONVERSIONS["MMHG:CENTIPASCAL"] = \
        lambda: noconversion("MMHG", "CENTIPASCAL")
    CONVERSIONS["MMHG:DECAPASCAL"] = \
        lambda: noconversion("MMHG", "DECAPASCAL")
    CONVERSIONS["MMHG:DECIBAR"] = \
        lambda: noconversion("MMHG", "DECIBAR")
    CONVERSIONS["MMHG:DECIPASCAL"] = \
        lambda: noconversion("MMHG", "DECIPASCAL")
    CONVERSIONS["MMHG:EXAPASCAL"] = \
        lambda: noconversion("MMHG", "EXAPASCAL")
    CONVERSIONS["MMHG:FEMTOPASCAL"] = \
        lambda: noconversion("MMHG", "FEMTOPASCAL")
    CONVERSIONS["MMHG:GIGAPASCAL"] = \
        lambda: noconversion("MMHG", "GIGAPASCAL")
    CONVERSIONS["MMHG:HECTOPASCAL"] = \
        lambda: noconversion("MMHG", "HECTOPASCAL")
    CONVERSIONS["MMHG:KILOBAR"] = \
        lambda: noconversion("MMHG", "KILOBAR")
    CONVERSIONS["MMHG:KILOPASCAL"] = \
        lambda: noconversion("MMHG", "KILOPASCAL")
    CONVERSIONS["MMHG:MEGABAR"] = \
        lambda: noconversion("MMHG", "MEGABAR")
    CONVERSIONS["MMHG:MEGAPASCAL"] = \
        lambda: noconversion("MMHG", "MEGAPASCAL")
    CONVERSIONS["MMHG:MICROPASCAL"] = \
        lambda: noconversion("MMHG", "MICROPASCAL")
    CONVERSIONS["MMHG:MILLIBAR"] = \
        lambda: noconversion("MMHG", "MILLIBAR")
    CONVERSIONS["MMHG:MILLIPASCAL"] = \
        lambda: noconversion("MMHG", "MILLIPASCAL")
    CONVERSIONS["MMHG:MILLITORR"] = \
        lambda: noconversion("MMHG", "MILLITORR")
    CONVERSIONS["MMHG:NANOPASCAL"] = \
        lambda: noconversion("MMHG", "NANOPASCAL")
    CONVERSIONS["MMHG:PETAPASCAL"] = \
        lambda: noconversion("MMHG", "PETAPASCAL")
    CONVERSIONS["MMHG:PICOPASCAL"] = \
        lambda: noconversion("MMHG", "PICOPASCAL")
    CONVERSIONS["MMHG:PSI"] = \
        lambda: noconversion("MMHG", "PSI")
    CONVERSIONS["MMHG:Pascal"] = \
        lambda: noconversion("MMHG", "Pascal")
    CONVERSIONS["MMHG:TERAPASCAL"] = \
        lambda: noconversion("MMHG", "TERAPASCAL")
    CONVERSIONS["MMHG:TORR"] = \
        lambda: noconversion("MMHG", "TORR")
    CONVERSIONS["MMHG:YOCTOPASCAL"] = \
        lambda: noconversion("MMHG", "YOCTOPASCAL")
    CONVERSIONS["MMHG:YOTTAPASCAL"] = \
        lambda: noconversion("MMHG", "YOTTAPASCAL")
    CONVERSIONS["MMHG:ZEPTOPASCAL"] = \
        lambda: noconversion("MMHG", "ZEPTOPASCAL")
    CONVERSIONS["MMHG:ZETTAPASCAL"] = \
        lambda: noconversion("MMHG", "ZETTAPASCAL")
    CONVERSIONS["NANOPASCAL:ATMOSPHERE"] = \
        lambda: noconversion("NANOPASCAL", "ATMOSPHERE")
    CONVERSIONS["NANOPASCAL:ATTOPASCAL"] = \
        lambda value: (10 ** 9) * value
    CONVERSIONS["NANOPASCAL:BAR"] = \
        lambda: noconversion("NANOPASCAL", "BAR")
    CONVERSIONS["NANOPASCAL:CENTIBAR"] = \
        lambda: noconversion("NANOPASCAL", "CENTIBAR")
    CONVERSIONS["NANOPASCAL:CENTIPASCAL"] = \
        lambda value: (10 ** -7) * value
    CONVERSIONS["NANOPASCAL:DECAPASCAL"] = \
        lambda value: (10 ** -10) * value
    CONVERSIONS["NANOPASCAL:DECIBAR"] = \
        lambda: noconversion("NANOPASCAL", "DECIBAR")
    CONVERSIONS["NANOPASCAL:DECIPASCAL"] = \
        lambda value: (10 ** -8) * value
    CONVERSIONS["NANOPASCAL:EXAPASCAL"] = \
        lambda value: (10 ** -27) * value
    CONVERSIONS["NANOPASCAL:FEMTOPASCAL"] = \
        lambda value: (10 ** 6) * value
    CONVERSIONS["NANOPASCAL:GIGAPASCAL"] = \
        lambda value: (10 ** -18) * value
    CONVERSIONS["NANOPASCAL:HECTOPASCAL"] = \
        lambda value: (10 ** -11) * value
    CONVERSIONS["NANOPASCAL:KILOBAR"] = \
        lambda: noconversion("NANOPASCAL", "KILOBAR")
    CONVERSIONS["NANOPASCAL:KILOPASCAL"] = \
        lambda value: (10 ** -12) * value
    CONVERSIONS["NANOPASCAL:MEGABAR"] = \
        lambda: noconversion("NANOPASCAL", "MEGABAR")
    CONVERSIONS["NANOPASCAL:MEGAPASCAL"] = \
        lambda value: (10 ** -15) * value
    CONVERSIONS["NANOPASCAL:MICROPASCAL"] = \
        lambda value: (10 ** -3) * value
    CONVERSIONS["NANOPASCAL:MILLIBAR"] = \
        lambda: noconversion("NANOPASCAL", "MILLIBAR")
    CONVERSIONS["NANOPASCAL:MILLIPASCAL"] = \
        lambda value: (10 ** -6) * value
    CONVERSIONS["NANOPASCAL:MILLITORR"] = \
        lambda: noconversion("NANOPASCAL", "MILLITORR")
    CONVERSIONS["NANOPASCAL:MMHG"] = \
        lambda: noconversion("NANOPASCAL", "MMHG")
    CONVERSIONS["NANOPASCAL:PETAPASCAL"] = \
        lambda value: (10 ** -24) * value
    CONVERSIONS["NANOPASCAL:PICOPASCAL"] = \
        lambda value: (10 ** 3) * value
    CONVERSIONS["NANOPASCAL:PSI"] = \
        lambda: noconversion("NANOPASCAL", "PSI")
    CONVERSIONS["NANOPASCAL:Pascal"] = \
        lambda value: (10 ** -9) * value
    CONVERSIONS["NANOPASCAL:TERAPASCAL"] = \
        lambda value: (10 ** -21) * value
    CONVERSIONS["NANOPASCAL:TORR"] = \
        lambda: noconversion("NANOPASCAL", "TORR")
    CONVERSIONS["NANOPASCAL:YOCTOPASCAL"] = \
        lambda value: (10 ** 15) * value
    CONVERSIONS["NANOPASCAL:YOTTAPASCAL"] = \
        lambda value: (10 ** -33) * value
    CONVERSIONS["NANOPASCAL:ZEPTOPASCAL"] = \
        lambda value: (10 ** 12) * value
    CONVERSIONS["NANOPASCAL:ZETTAPASCAL"] = \
        lambda value: (10 ** -30) * value
    CONVERSIONS["PETAPASCAL:ATMOSPHERE"] = \
        lambda: noconversion("PETAPASCAL", "ATMOSPHERE")
    CONVERSIONS["PETAPASCAL:ATTOPASCAL"] = \
        lambda value: (10 ** 33) * value
    CONVERSIONS["PETAPASCAL:BAR"] = \
        lambda: noconversion("PETAPASCAL", "BAR")
    CONVERSIONS["PETAPASCAL:CENTIBAR"] = \
        lambda: noconversion("PETAPASCAL", "CENTIBAR")
    CONVERSIONS["PETAPASCAL:CENTIPASCAL"] = \
        lambda value: (10 ** 17) * value
    CONVERSIONS["PETAPASCAL:DECAPASCAL"] = \
        lambda value: (10 ** 14) * value
    CONVERSIONS["PETAPASCAL:DECIBAR"] = \
        lambda: noconversion("PETAPASCAL", "DECIBAR")
    CONVERSIONS["PETAPASCAL:DECIPASCAL"] = \
        lambda value: (10 ** 16) * value
    CONVERSIONS["PETAPASCAL:EXAPASCAL"] = \
        lambda value: (10 ** -3) * value
    CONVERSIONS["PETAPASCAL:FEMTOPASCAL"] = \
        lambda value: (10 ** 30) * value
    CONVERSIONS["PETAPASCAL:GIGAPASCAL"] = \
        lambda value: (10 ** 6) * value
    CONVERSIONS["PETAPASCAL:HECTOPASCAL"] = \
        lambda value: (10 ** 13) * value
    CONVERSIONS["PETAPASCAL:KILOBAR"] = \
        lambda: noconversion("PETAPASCAL", "KILOBAR")
    CONVERSIONS["PETAPASCAL:KILOPASCAL"] = \
        lambda value: (10 ** 12) * value
    CONVERSIONS["PETAPASCAL:MEGABAR"] = \
        lambda: noconversion("PETAPASCAL", "MEGABAR")
    CONVERSIONS["PETAPASCAL:MEGAPASCAL"] = \
        lambda value: (10 ** 9) * value
    CONVERSIONS["PETAPASCAL:MICROPASCAL"] = \
        lambda value: (10 ** 21) * value
    CONVERSIONS["PETAPASCAL:MILLIBAR"] = \
        lambda: noconversion("PETAPASCAL", "MILLIBAR")
    CONVERSIONS["PETAPASCAL:MILLIPASCAL"] = \
        lambda value: (10 ** 18) * value
    CONVERSIONS["PETAPASCAL:MILLITORR"] = \
        lambda: noconversion("PETAPASCAL", "MILLITORR")
    CONVERSIONS["PETAPASCAL:MMHG"] = \
        lambda: noconversion("PETAPASCAL", "MMHG")
    CONVERSIONS["PETAPASCAL:NANOPASCAL"] = \
        lambda value: (10 ** 24) * value
    CONVERSIONS["PETAPASCAL:PICOPASCAL"] = \
        lambda value: (10 ** 27) * value
    CONVERSIONS["PETAPASCAL:PSI"] = \
        lambda: noconversion("PETAPASCAL", "PSI")
    CONVERSIONS["PETAPASCAL:Pascal"] = \
        lambda value: (10 ** 15) * value
    CONVERSIONS["PETAPASCAL:TERAPASCAL"] = \
        lambda value: (10 ** 3) * value
    CONVERSIONS["PETAPASCAL:TORR"] = \
        lambda: noconversion("PETAPASCAL", "TORR")
    CONVERSIONS["PETAPASCAL:YOCTOPASCAL"] = \
        lambda value: (10 ** 39) * value
    CONVERSIONS["PETAPASCAL:YOTTAPASCAL"] = \
        lambda value: (10 ** -9) * value
    CONVERSIONS["PETAPASCAL:ZEPTOPASCAL"] = \
        lambda value: (10 ** 36) * value
    CONVERSIONS["PETAPASCAL:ZETTAPASCAL"] = \
        lambda value: (10 ** -6) * value
    CONVERSIONS["PICOPASCAL:ATMOSPHERE"] = \
        lambda: noconversion("PICOPASCAL", "ATMOSPHERE")
    CONVERSIONS["PICOPASCAL:ATTOPASCAL"] = \
        lambda value: (10 ** 6) * value
    CONVERSIONS["PICOPASCAL:BAR"] = \
        lambda: noconversion("PICOPASCAL", "BAR")
    CONVERSIONS["PICOPASCAL:CENTIBAR"] = \
        lambda: noconversion("PICOPASCAL", "CENTIBAR")
    CONVERSIONS["PICOPASCAL:CENTIPASCAL"] = \
        lambda value: (10 ** -10) * value
    CONVERSIONS["PICOPASCAL:DECAPASCAL"] = \
        lambda value: (10 ** -13) * value
    CONVERSIONS["PICOPASCAL:DECIBAR"] = \
        lambda: noconversion("PICOPASCAL", "DECIBAR")
    CONVERSIONS["PICOPASCAL:DECIPASCAL"] = \
        lambda value: (10 ** -11) * value
    CONVERSIONS["PICOPASCAL:EXAPASCAL"] = \
        lambda value: (10 ** -30) * value
    CONVERSIONS["PICOPASCAL:FEMTOPASCAL"] = \
        lambda value: (10 ** 3) * value
    CONVERSIONS["PICOPASCAL:GIGAPASCAL"] = \
        lambda value: (10 ** -21) * value
    CONVERSIONS["PICOPASCAL:HECTOPASCAL"] = \
        lambda value: (10 ** -14) * value
    CONVERSIONS["PICOPASCAL:KILOBAR"] = \
        lambda: noconversion("PICOPASCAL", "KILOBAR")
    CONVERSIONS["PICOPASCAL:KILOPASCAL"] = \
        lambda value: (10 ** -15) * value
    CONVERSIONS["PICOPASCAL:MEGABAR"] = \
        lambda: noconversion("PICOPASCAL", "MEGABAR")
    CONVERSIONS["PICOPASCAL:MEGAPASCAL"] = \
        lambda value: (10 ** -18) * value
    CONVERSIONS["PICOPASCAL:MICROPASCAL"] = \
        lambda value: (10 ** -6) * value
    CONVERSIONS["PICOPASCAL:MILLIBAR"] = \
        lambda: noconversion("PICOPASCAL", "MILLIBAR")
    CONVERSIONS["PICOPASCAL:MILLIPASCAL"] = \
        lambda value: (10 ** -9) * value
    CONVERSIONS["PICOPASCAL:MILLITORR"] = \
        lambda: noconversion("PICOPASCAL", "MILLITORR")
    CONVERSIONS["PICOPASCAL:MMHG"] = \
        lambda: noconversion("PICOPASCAL", "MMHG")
    CONVERSIONS["PICOPASCAL:NANOPASCAL"] = \
        lambda value: (10 ** -3) * value
    CONVERSIONS["PICOPASCAL:PETAPASCAL"] = \
        lambda value: (10 ** -27) * value
    CONVERSIONS["PICOPASCAL:PSI"] = \
        lambda: noconversion("PICOPASCAL", "PSI")
    CONVERSIONS["PICOPASCAL:Pascal"] = \
        lambda value: (10 ** -12) * value
    CONVERSIONS["PICOPASCAL:TERAPASCAL"] = \
        lambda value: (10 ** -24) * value
    CONVERSIONS["PICOPASCAL:TORR"] = \
        lambda: noconversion("PICOPASCAL", "TORR")
    CONVERSIONS["PICOPASCAL:YOCTOPASCAL"] = \
        lambda value: (10 ** 12) * value
    CONVERSIONS["PICOPASCAL:YOTTAPASCAL"] = \
        lambda value: (10 ** -36) * value
    CONVERSIONS["PICOPASCAL:ZEPTOPASCAL"] = \
        lambda value: (10 ** 9) * value
    CONVERSIONS["PICOPASCAL:ZETTAPASCAL"] = \
        lambda value: (10 ** -33) * value
    CONVERSIONS["PSI:ATMOSPHERE"] = \
        lambda: noconversion("PSI", "ATMOSPHERE")
    CONVERSIONS["PSI:ATTOPASCAL"] = \
        lambda: noconversion("PSI", "ATTOPASCAL")
    CONVERSIONS["PSI:BAR"] = \
        lambda: noconversion("PSI", "BAR")
    CONVERSIONS["PSI:CENTIBAR"] = \
        lambda: noconversion("PSI", "CENTIBAR")
    CONVERSIONS["PSI:CENTIPASCAL"] = \
        lambda: noconversion("PSI", "CENTIPASCAL")
    CONVERSIONS["PSI:DECAPASCAL"] = \
        lambda: noconversion("PSI", "DECAPASCAL")
    CONVERSIONS["PSI:DECIBAR"] = \
        lambda: noconversion("PSI", "DECIBAR")
    CONVERSIONS["PSI:DECIPASCAL"] = \
        lambda: noconversion("PSI", "DECIPASCAL")
    CONVERSIONS["PSI:EXAPASCAL"] = \
        lambda: noconversion("PSI", "EXAPASCAL")
    CONVERSIONS["PSI:FEMTOPASCAL"] = \
        lambda: noconversion("PSI", "FEMTOPASCAL")
    CONVERSIONS["PSI:GIGAPASCAL"] = \
        lambda: noconversion("PSI", "GIGAPASCAL")
    CONVERSIONS["PSI:HECTOPASCAL"] = \
        lambda: noconversion("PSI", "HECTOPASCAL")
    CONVERSIONS["PSI:KILOBAR"] = \
        lambda: noconversion("PSI", "KILOBAR")
    CONVERSIONS["PSI:KILOPASCAL"] = \
        lambda: noconversion("PSI", "KILOPASCAL")
    CONVERSIONS["PSI:MEGABAR"] = \
        lambda: noconversion("PSI", "MEGABAR")
    CONVERSIONS["PSI:MEGAPASCAL"] = \
        lambda: noconversion("PSI", "MEGAPASCAL")
    CONVERSIONS["PSI:MICROPASCAL"] = \
        lambda: noconversion("PSI", "MICROPASCAL")
    CONVERSIONS["PSI:MILLIBAR"] = \
        lambda: noconversion("PSI", "MILLIBAR")
    CONVERSIONS["PSI:MILLIPASCAL"] = \
        lambda: noconversion("PSI", "MILLIPASCAL")
    CONVERSIONS["PSI:MILLITORR"] = \
        lambda: noconversion("PSI", "MILLITORR")
    CONVERSIONS["PSI:MMHG"] = \
        lambda: noconversion("PSI", "MMHG")
    CONVERSIONS["PSI:NANOPASCAL"] = \
        lambda: noconversion("PSI", "NANOPASCAL")
    CONVERSIONS["PSI:PETAPASCAL"] = \
        lambda: noconversion("PSI", "PETAPASCAL")
    CONVERSIONS["PSI:PICOPASCAL"] = \
        lambda: noconversion("PSI", "PICOPASCAL")
    CONVERSIONS["PSI:Pascal"] = \
        lambda: noconversion("PSI", "Pascal")
    CONVERSIONS["PSI:TERAPASCAL"] = \
        lambda: noconversion("PSI", "TERAPASCAL")
    CONVERSIONS["PSI:TORR"] = \
        lambda: noconversion("PSI", "TORR")
    CONVERSIONS["PSI:YOCTOPASCAL"] = \
        lambda: noconversion("PSI", "YOCTOPASCAL")
    CONVERSIONS["PSI:YOTTAPASCAL"] = \
        lambda: noconversion("PSI", "YOTTAPASCAL")
    CONVERSIONS["PSI:ZEPTOPASCAL"] = \
        lambda: noconversion("PSI", "ZEPTOPASCAL")
    CONVERSIONS["PSI:ZETTAPASCAL"] = \
        lambda: noconversion("PSI", "ZETTAPASCAL")
    CONVERSIONS["Pascal:ATMOSPHERE"] = \
        lambda: noconversion("Pascal", "ATMOSPHERE")
    CONVERSIONS["Pascal:ATTOPASCAL"] = \
        lambda value: (10 ** 18) * value
    CONVERSIONS["Pascal:BAR"] = \
        lambda: noconversion("Pascal", "BAR")
    CONVERSIONS["Pascal:CENTIBAR"] = \
        lambda: noconversion("Pascal", "CENTIBAR")
    CONVERSIONS["Pascal:CENTIPASCAL"] = \
        lambda value: (10 ** 2) * value
    CONVERSIONS["Pascal:DECAPASCAL"] = \
        lambda value: (10 ** -1) * value
    CONVERSIONS["Pascal:DECIBAR"] = \
        lambda: noconversion("Pascal", "DECIBAR")
    CONVERSIONS["Pascal:DECIPASCAL"] = \
        lambda value: 10 * value
    CONVERSIONS["Pascal:EXAPASCAL"] = \
        lambda value: (10 ** -18) * value
    CONVERSIONS["Pascal:FEMTOPASCAL"] = \
        lambda value: (10 ** 15) * value
    CONVERSIONS["Pascal:GIGAPASCAL"] = \
        lambda value: (10 ** -9) * value
    CONVERSIONS["Pascal:HECTOPASCAL"] = \
        lambda value: (10 ** -2) * value
    CONVERSIONS["Pascal:KILOBAR"] = \
        lambda: noconversion("Pascal", "KILOBAR")
    CONVERSIONS["Pascal:KILOPASCAL"] = \
        lambda value: (10 ** -3) * value
    CONVERSIONS["Pascal:MEGABAR"] = \
        lambda: noconversion("Pascal", "MEGABAR")
    CONVERSIONS["Pascal:MEGAPASCAL"] = \
        lambda value: (10 ** -6) * value
    CONVERSIONS["Pascal:MICROPASCAL"] = \
        lambda value: (10 ** 6) * value
    CONVERSIONS["Pascal:MILLIBAR"] = \
        lambda: noconversion("Pascal", "MILLIBAR")
    CONVERSIONS["Pascal:MILLIPASCAL"] = \
        lambda value: (10 ** 3) * value
    CONVERSIONS["Pascal:MILLITORR"] = \
        lambda: noconversion("Pascal", "MILLITORR")
    CONVERSIONS["Pascal:MMHG"] = \
        lambda: noconversion("Pascal", "MMHG")
    CONVERSIONS["Pascal:NANOPASCAL"] = \
        lambda value: (10 ** 9) * value
    CONVERSIONS["Pascal:PETAPASCAL"] = \
        lambda value: (10 ** -15) * value
    CONVERSIONS["Pascal:PICOPASCAL"] = \
        lambda value: (10 ** 12) * value
    CONVERSIONS["Pascal:PSI"] = \
        lambda: noconversion("Pascal", "PSI")
    CONVERSIONS["Pascal:TERAPASCAL"] = \
        lambda value: (10 ** -12) * value
    CONVERSIONS["Pascal:TORR"] = \
        lambda: noconversion("Pascal", "TORR")
    CONVERSIONS["Pascal:YOCTOPASCAL"] = \
        lambda value: (10 ** 24) * value
    CONVERSIONS["Pascal:YOTTAPASCAL"] = \
        lambda value: (10 ** -24) * value
    CONVERSIONS["Pascal:ZEPTOPASCAL"] = \
        lambda value: (10 ** 21) * value
    CONVERSIONS["Pascal:ZETTAPASCAL"] = \
        lambda value: (10 ** -21) * value
    CONVERSIONS["TERAPASCAL:ATMOSPHERE"] = \
        lambda: noconversion("TERAPASCAL", "ATMOSPHERE")
    CONVERSIONS["TERAPASCAL:ATTOPASCAL"] = \
        lambda value: (10 ** 30) * value
    CONVERSIONS["TERAPASCAL:BAR"] = \
        lambda: noconversion("TERAPASCAL", "BAR")
    CONVERSIONS["TERAPASCAL:CENTIBAR"] = \
        lambda: noconversion("TERAPASCAL", "CENTIBAR")
    CONVERSIONS["TERAPASCAL:CENTIPASCAL"] = \
        lambda value: (10 ** 14) * value
    CONVERSIONS["TERAPASCAL:DECAPASCAL"] = \
        lambda value: (10 ** 11) * value
    CONVERSIONS["TERAPASCAL:DECIBAR"] = \
        lambda: noconversion("TERAPASCAL", "DECIBAR")
    CONVERSIONS["TERAPASCAL:DECIPASCAL"] = \
        lambda value: (10 ** 13) * value
    CONVERSIONS["TERAPASCAL:EXAPASCAL"] = \
        lambda value: (10 ** -6) * value
    CONVERSIONS["TERAPASCAL:FEMTOPASCAL"] = \
        lambda value: (10 ** 27) * value
    CONVERSIONS["TERAPASCAL:GIGAPASCAL"] = \
        lambda value: (10 ** 3) * value
    CONVERSIONS["TERAPASCAL:HECTOPASCAL"] = \
        lambda value: (10 ** 10) * value
    CONVERSIONS["TERAPASCAL:KILOBAR"] = \
        lambda: noconversion("TERAPASCAL", "KILOBAR")
    CONVERSIONS["TERAPASCAL:KILOPASCAL"] = \
        lambda value: (10 ** 9) * value
    CONVERSIONS["TERAPASCAL:MEGABAR"] = \
        lambda: noconversion("TERAPASCAL", "MEGABAR")
    CONVERSIONS["TERAPASCAL:MEGAPASCAL"] = \
        lambda value: (10 ** 6) * value
    CONVERSIONS["TERAPASCAL:MICROPASCAL"] = \
        lambda value: (10 ** 18) * value
    CONVERSIONS["TERAPASCAL:MILLIBAR"] = \
        lambda: noconversion("TERAPASCAL", "MILLIBAR")
    CONVERSIONS["TERAPASCAL:MILLIPASCAL"] = \
        lambda value: (10 ** 15) * value
    CONVERSIONS["TERAPASCAL:MILLITORR"] = \
        lambda: noconversion("TERAPASCAL", "MILLITORR")
    CONVERSIONS["TERAPASCAL:MMHG"] = \
        lambda: noconversion("TERAPASCAL", "MMHG")
    CONVERSIONS["TERAPASCAL:NANOPASCAL"] = \
        lambda value: (10 ** 21) * value
    CONVERSIONS["TERAPASCAL:PETAPASCAL"] = \
        lambda value: (10 ** -3) * value
    CONVERSIONS["TERAPASCAL:PICOPASCAL"] = \
        lambda value: (10 ** 24) * value
    CONVERSIONS["TERAPASCAL:PSI"] = \
        lambda: noconversion("TERAPASCAL", "PSI")
    CONVERSIONS["TERAPASCAL:Pascal"] = \
        lambda value: (10 ** 12) * value
    CONVERSIONS["TERAPASCAL:TORR"] = \
        lambda: noconversion("TERAPASCAL", "TORR")
    CONVERSIONS["TERAPASCAL:YOCTOPASCAL"] = \
        lambda value: (10 ** 36) * value
    CONVERSIONS["TERAPASCAL:YOTTAPASCAL"] = \
        lambda value: (10 ** -12) * value
    CONVERSIONS["TERAPASCAL:ZEPTOPASCAL"] = \
        lambda value: (10 ** 33) * value
    CONVERSIONS["TERAPASCAL:ZETTAPASCAL"] = \
        lambda value: (10 ** -9) * value
    CONVERSIONS["TORR:ATMOSPHERE"] = \
        lambda: noconversion("TORR", "ATMOSPHERE")
    CONVERSIONS["TORR:ATTOPASCAL"] = \
        lambda: noconversion("TORR", "ATTOPASCAL")
    CONVERSIONS["TORR:BAR"] = \
        lambda: noconversion("TORR", "BAR")
    CONVERSIONS["TORR:CENTIBAR"] = \
        lambda: noconversion("TORR", "CENTIBAR")
    CONVERSIONS["TORR:CENTIPASCAL"] = \
        lambda: noconversion("TORR", "CENTIPASCAL")
    CONVERSIONS["TORR:DECAPASCAL"] = \
        lambda: noconversion("TORR", "DECAPASCAL")
    CONVERSIONS["TORR:DECIBAR"] = \
        lambda: noconversion("TORR", "DECIBAR")
    CONVERSIONS["TORR:DECIPASCAL"] = \
        lambda: noconversion("TORR", "DECIPASCAL")
    CONVERSIONS["TORR:EXAPASCAL"] = \
        lambda: noconversion("TORR", "EXAPASCAL")
    CONVERSIONS["TORR:FEMTOPASCAL"] = \
        lambda: noconversion("TORR", "FEMTOPASCAL")
    CONVERSIONS["TORR:GIGAPASCAL"] = \
        lambda: noconversion("TORR", "GIGAPASCAL")
    CONVERSIONS["TORR:HECTOPASCAL"] = \
        lambda: noconversion("TORR", "HECTOPASCAL")
    CONVERSIONS["TORR:KILOBAR"] = \
        lambda: noconversion("TORR", "KILOBAR")
    CONVERSIONS["TORR:KILOPASCAL"] = \
        lambda: noconversion("TORR", "KILOPASCAL")
    CONVERSIONS["TORR:MEGABAR"] = \
        lambda: noconversion("TORR", "MEGABAR")
    CONVERSIONS["TORR:MEGAPASCAL"] = \
        lambda: noconversion("TORR", "MEGAPASCAL")
    CONVERSIONS["TORR:MICROPASCAL"] = \
        lambda: noconversion("TORR", "MICROPASCAL")
    CONVERSIONS["TORR:MILLIBAR"] = \
        lambda: noconversion("TORR", "MILLIBAR")
    CONVERSIONS["TORR:MILLIPASCAL"] = \
        lambda: noconversion("TORR", "MILLIPASCAL")
    CONVERSIONS["TORR:MILLITORR"] = \
        lambda: noconversion("TORR", "MILLITORR")
    CONVERSIONS["TORR:MMHG"] = \
        lambda: noconversion("TORR", "MMHG")
    CONVERSIONS["TORR:NANOPASCAL"] = \
        lambda: noconversion("TORR", "NANOPASCAL")
    CONVERSIONS["TORR:PETAPASCAL"] = \
        lambda: noconversion("TORR", "PETAPASCAL")
    CONVERSIONS["TORR:PICOPASCAL"] = \
        lambda: noconversion("TORR", "PICOPASCAL")
    CONVERSIONS["TORR:PSI"] = \
        lambda: noconversion("TORR", "PSI")
    CONVERSIONS["TORR:Pascal"] = \
        lambda: noconversion("TORR", "Pascal")
    CONVERSIONS["TORR:TERAPASCAL"] = \
        lambda: noconversion("TORR", "TERAPASCAL")
    CONVERSIONS["TORR:YOCTOPASCAL"] = \
        lambda: noconversion("TORR", "YOCTOPASCAL")
    CONVERSIONS["TORR:YOTTAPASCAL"] = \
        lambda: noconversion("TORR", "YOTTAPASCAL")
    CONVERSIONS["TORR:ZEPTOPASCAL"] = \
        lambda: noconversion("TORR", "ZEPTOPASCAL")
    CONVERSIONS["TORR:ZETTAPASCAL"] = \
        lambda: noconversion("TORR", "ZETTAPASCAL")
    CONVERSIONS["YOCTOPASCAL:ATMOSPHERE"] = \
        lambda: noconversion("YOCTOPASCAL", "ATMOSPHERE")
    CONVERSIONS["YOCTOPASCAL:ATTOPASCAL"] = \
        lambda value: (10 ** -6) * value
    CONVERSIONS["YOCTOPASCAL:BAR"] = \
        lambda: noconversion("YOCTOPASCAL", "BAR")
    CONVERSIONS["YOCTOPASCAL:CENTIBAR"] = \
        lambda: noconversion("YOCTOPASCAL", "CENTIBAR")
    CONVERSIONS["YOCTOPASCAL:CENTIPASCAL"] = \
        lambda value: (10 ** -22) * value
    CONVERSIONS["YOCTOPASCAL:DECAPASCAL"] = \
        lambda value: (10 ** -25) * value
    CONVERSIONS["YOCTOPASCAL:DECIBAR"] = \
        lambda: noconversion("YOCTOPASCAL", "DECIBAR")
    CONVERSIONS["YOCTOPASCAL:DECIPASCAL"] = \
        lambda value: (10 ** -23) * value
    CONVERSIONS["YOCTOPASCAL:EXAPASCAL"] = \
        lambda value: (10 ** -42) * value
    CONVERSIONS["YOCTOPASCAL:FEMTOPASCAL"] = \
        lambda value: (10 ** -9) * value
    CONVERSIONS["YOCTOPASCAL:GIGAPASCAL"] = \
        lambda value: (10 ** -33) * value
    CONVERSIONS["YOCTOPASCAL:HECTOPASCAL"] = \
        lambda value: (10 ** -26) * value
    CONVERSIONS["YOCTOPASCAL:KILOBAR"] = \
        lambda: noconversion("YOCTOPASCAL", "KILOBAR")
    CONVERSIONS["YOCTOPASCAL:KILOPASCAL"] = \
        lambda value: (10 ** -27) * value
    CONVERSIONS["YOCTOPASCAL:MEGABAR"] = \
        lambda: noconversion("YOCTOPASCAL", "MEGABAR")
    CONVERSIONS["YOCTOPASCAL:MEGAPASCAL"] = \
        lambda value: (10 ** -30) * value
    CONVERSIONS["YOCTOPASCAL:MICROPASCAL"] = \
        lambda value: (10 ** -18) * value
    CONVERSIONS["YOCTOPASCAL:MILLIBAR"] = \
        lambda: noconversion("YOCTOPASCAL", "MILLIBAR")
    CONVERSIONS["YOCTOPASCAL:MILLIPASCAL"] = \
        lambda value: (10 ** -21) * value
    CONVERSIONS["YOCTOPASCAL:MILLITORR"] = \
        lambda: noconversion("YOCTOPASCAL", "MILLITORR")
    CONVERSIONS["YOCTOPASCAL:MMHG"] = \
        lambda: noconversion("YOCTOPASCAL", "MMHG")
    CONVERSIONS["YOCTOPASCAL:NANOPASCAL"] = \
        lambda value: (10 ** -15) * value
    CONVERSIONS["YOCTOPASCAL:PETAPASCAL"] = \
        lambda value: (10 ** -39) * value
    CONVERSIONS["YOCTOPASCAL:PICOPASCAL"] = \
        lambda value: (10 ** -12) * value
    CONVERSIONS["YOCTOPASCAL:PSI"] = \
        lambda: noconversion("YOCTOPASCAL", "PSI")
    CONVERSIONS["YOCTOPASCAL:Pascal"] = \
        lambda value: (10 ** -24) * value
    CONVERSIONS["YOCTOPASCAL:TERAPASCAL"] = \
        lambda value: (10 ** -36) * value
    CONVERSIONS["YOCTOPASCAL:TORR"] = \
        lambda: noconversion("YOCTOPASCAL", "TORR")
    CONVERSIONS["YOCTOPASCAL:YOTTAPASCAL"] = \
        lambda value: (10 ** -48) * value
    CONVERSIONS["YOCTOPASCAL:ZEPTOPASCAL"] = \
        lambda value: (10 ** -3) * value
    CONVERSIONS["YOCTOPASCAL:ZETTAPASCAL"] = \
        lambda value: (10 ** -45) * value
    CONVERSIONS["YOTTAPASCAL:ATMOSPHERE"] = \
        lambda: noconversion("YOTTAPASCAL", "ATMOSPHERE")
    CONVERSIONS["YOTTAPASCAL:ATTOPASCAL"] = \
        lambda value: (10 ** 42) * value
    CONVERSIONS["YOTTAPASCAL:BAR"] = \
        lambda: noconversion("YOTTAPASCAL", "BAR")
    CONVERSIONS["YOTTAPASCAL:CENTIBAR"] = \
        lambda: noconversion("YOTTAPASCAL", "CENTIBAR")
    CONVERSIONS["YOTTAPASCAL:CENTIPASCAL"] = \
        lambda value: (10 ** 26) * value
    CONVERSIONS["YOTTAPASCAL:DECAPASCAL"] = \
        lambda value: (10 ** 23) * value
    CONVERSIONS["YOTTAPASCAL:DECIBAR"] = \
        lambda: noconversion("YOTTAPASCAL", "DECIBAR")
    CONVERSIONS["YOTTAPASCAL:DECIPASCAL"] = \
        lambda value: (10 ** 25) * value
    CONVERSIONS["YOTTAPASCAL:EXAPASCAL"] = \
        lambda value: (10 ** 6) * value
    CONVERSIONS["YOTTAPASCAL:FEMTOPASCAL"] = \
        lambda value: (10 ** 39) * value
    CONVERSIONS["YOTTAPASCAL:GIGAPASCAL"] = \
        lambda value: (10 ** 15) * value
    CONVERSIONS["YOTTAPASCAL:HECTOPASCAL"] = \
        lambda value: (10 ** 22) * value
    CONVERSIONS["YOTTAPASCAL:KILOBAR"] = \
        lambda: noconversion("YOTTAPASCAL", "KILOBAR")
    CONVERSIONS["YOTTAPASCAL:KILOPASCAL"] = \
        lambda value: (10 ** 21) * value
    CONVERSIONS["YOTTAPASCAL:MEGABAR"] = \
        lambda: noconversion("YOTTAPASCAL", "MEGABAR")
    CONVERSIONS["YOTTAPASCAL:MEGAPASCAL"] = \
        lambda value: (10 ** 18) * value
    CONVERSIONS["YOTTAPASCAL:MICROPASCAL"] = \
        lambda value: (10 ** 30) * value
    CONVERSIONS["YOTTAPASCAL:MILLIBAR"] = \
        lambda: noconversion("YOTTAPASCAL", "MILLIBAR")
    CONVERSIONS["YOTTAPASCAL:MILLIPASCAL"] = \
        lambda value: (10 ** 27) * value
    CONVERSIONS["YOTTAPASCAL:MILLITORR"] = \
        lambda: noconversion("YOTTAPASCAL", "MILLITORR")
    CONVERSIONS["YOTTAPASCAL:MMHG"] = \
        lambda: noconversion("YOTTAPASCAL", "MMHG")
    CONVERSIONS["YOTTAPASCAL:NANOPASCAL"] = \
        lambda value: (10 ** 33) * value
    CONVERSIONS["YOTTAPASCAL:PETAPASCAL"] = \
        lambda value: (10 ** 9) * value
    CONVERSIONS["YOTTAPASCAL:PICOPASCAL"] = \
        lambda value: (10 ** 36) * value
    CONVERSIONS["YOTTAPASCAL:PSI"] = \
        lambda: noconversion("YOTTAPASCAL", "PSI")
    CONVERSIONS["YOTTAPASCAL:Pascal"] = \
        lambda value: (10 ** 24) * value
    CONVERSIONS["YOTTAPASCAL:TERAPASCAL"] = \
        lambda value: (10 ** 12) * value
    CONVERSIONS["YOTTAPASCAL:TORR"] = \
        lambda: noconversion("YOTTAPASCAL", "TORR")
    CONVERSIONS["YOTTAPASCAL:YOCTOPASCAL"] = \
        lambda value: (10 ** 48) * value
    CONVERSIONS["YOTTAPASCAL:ZEPTOPASCAL"] = \
        lambda value: (10 ** 45) * value
    CONVERSIONS["YOTTAPASCAL:ZETTAPASCAL"] = \
        lambda value: (10 ** 3) * value
    CONVERSIONS["ZEPTOPASCAL:ATMOSPHERE"] = \
        lambda: noconversion("ZEPTOPASCAL", "ATMOSPHERE")
    CONVERSIONS["ZEPTOPASCAL:ATTOPASCAL"] = \
        lambda value: (10 ** -3) * value
    CONVERSIONS["ZEPTOPASCAL:BAR"] = \
        lambda: noconversion("ZEPTOPASCAL", "BAR")
    CONVERSIONS["ZEPTOPASCAL:CENTIBAR"] = \
        lambda: noconversion("ZEPTOPASCAL", "CENTIBAR")
    CONVERSIONS["ZEPTOPASCAL:CENTIPASCAL"] = \
        lambda value: (10 ** -19) * value
    CONVERSIONS["ZEPTOPASCAL:DECAPASCAL"] = \
        lambda value: (10 ** -22) * value
    CONVERSIONS["ZEPTOPASCAL:DECIBAR"] = \
        lambda: noconversion("ZEPTOPASCAL", "DECIBAR")
    CONVERSIONS["ZEPTOPASCAL:DECIPASCAL"] = \
        lambda value: (10 ** -20) * value
    CONVERSIONS["ZEPTOPASCAL:EXAPASCAL"] = \
        lambda value: (10 ** -39) * value
    CONVERSIONS["ZEPTOPASCAL:FEMTOPASCAL"] = \
        lambda value: (10 ** -6) * value
    CONVERSIONS["ZEPTOPASCAL:GIGAPASCAL"] = \
        lambda value: (10 ** -30) * value
    CONVERSIONS["ZEPTOPASCAL:HECTOPASCAL"] = \
        lambda value: (10 ** -23) * value
    CONVERSIONS["ZEPTOPASCAL:KILOBAR"] = \
        lambda: noconversion("ZEPTOPASCAL", "KILOBAR")
    CONVERSIONS["ZEPTOPASCAL:KILOPASCAL"] = \
        lambda value: (10 ** -24) * value
    CONVERSIONS["ZEPTOPASCAL:MEGABAR"] = \
        lambda: noconversion("ZEPTOPASCAL", "MEGABAR")
    CONVERSIONS["ZEPTOPASCAL:MEGAPASCAL"] = \
        lambda value: (10 ** -27) * value
    CONVERSIONS["ZEPTOPASCAL:MICROPASCAL"] = \
        lambda value: (10 ** -15) * value
    CONVERSIONS["ZEPTOPASCAL:MILLIBAR"] = \
        lambda: noconversion("ZEPTOPASCAL", "MILLIBAR")
    CONVERSIONS["ZEPTOPASCAL:MILLIPASCAL"] = \
        lambda value: (10 ** -18) * value
    CONVERSIONS["ZEPTOPASCAL:MILLITORR"] = \
        lambda: noconversion("ZEPTOPASCAL", "MILLITORR")
    CONVERSIONS["ZEPTOPASCAL:MMHG"] = \
        lambda: noconversion("ZEPTOPASCAL", "MMHG")
    CONVERSIONS["ZEPTOPASCAL:NANOPASCAL"] = \
        lambda value: (10 ** -12) * value
    CONVERSIONS["ZEPTOPASCAL:PETAPASCAL"] = \
        lambda value: (10 ** -36) * value
    CONVERSIONS["ZEPTOPASCAL:PICOPASCAL"] = \
        lambda value: (10 ** -9) * value
    CONVERSIONS["ZEPTOPASCAL:PSI"] = \
        lambda: noconversion("ZEPTOPASCAL", "PSI")
    CONVERSIONS["ZEPTOPASCAL:Pascal"] = \
        lambda value: (10 ** -21) * value
    CONVERSIONS["ZEPTOPASCAL:TERAPASCAL"] = \
        lambda value: (10 ** -33) * value
    CONVERSIONS["ZEPTOPASCAL:TORR"] = \
        lambda: noconversion("ZEPTOPASCAL", "TORR")
    CONVERSIONS["ZEPTOPASCAL:YOCTOPASCAL"] = \
        lambda value: (10 ** 3) * value
    CONVERSIONS["ZEPTOPASCAL:YOTTAPASCAL"] = \
        lambda value: (10 ** -45) * value
    CONVERSIONS["ZEPTOPASCAL:ZETTAPASCAL"] = \
        lambda value: (10 ** -42) * value
    CONVERSIONS["ZETTAPASCAL:ATMOSPHERE"] = \
        lambda: noconversion("ZETTAPASCAL", "ATMOSPHERE")
    CONVERSIONS["ZETTAPASCAL:ATTOPASCAL"] = \
        lambda value: (10 ** 39) * value
    CONVERSIONS["ZETTAPASCAL:BAR"] = \
        lambda: noconversion("ZETTAPASCAL", "BAR")
    CONVERSIONS["ZETTAPASCAL:CENTIBAR"] = \
        lambda: noconversion("ZETTAPASCAL", "CENTIBAR")
    CONVERSIONS["ZETTAPASCAL:CENTIPASCAL"] = \
        lambda value: (10 ** 23) * value
    CONVERSIONS["ZETTAPASCAL:DECAPASCAL"] = \
        lambda value: (10 ** 20) * value
    CONVERSIONS["ZETTAPASCAL:DECIBAR"] = \
        lambda: noconversion("ZETTAPASCAL", "DECIBAR")
    CONVERSIONS["ZETTAPASCAL:DECIPASCAL"] = \
        lambda value: (10 ** 22) * value
    CONVERSIONS["ZETTAPASCAL:EXAPASCAL"] = \
        lambda value: (10 ** 3) * value
    CONVERSIONS["ZETTAPASCAL:FEMTOPASCAL"] = \
        lambda value: (10 ** 36) * value
    CONVERSIONS["ZETTAPASCAL:GIGAPASCAL"] = \
        lambda value: (10 ** 12) * value
    CONVERSIONS["ZETTAPASCAL:HECTOPASCAL"] = \
        lambda value: (10 ** 19) * value
    CONVERSIONS["ZETTAPASCAL:KILOBAR"] = \
        lambda: noconversion("ZETTAPASCAL", "KILOBAR")
    CONVERSIONS["ZETTAPASCAL:KILOPASCAL"] = \
        lambda value: (10 ** 18) * value
    CONVERSIONS["ZETTAPASCAL:MEGABAR"] = \
        lambda: noconversion("ZETTAPASCAL", "MEGABAR")
    CONVERSIONS["ZETTAPASCAL:MEGAPASCAL"] = \
        lambda value: (10 ** 15) * value
    CONVERSIONS["ZETTAPASCAL:MICROPASCAL"] = \
        lambda value: (10 ** 27) * value
    CONVERSIONS["ZETTAPASCAL:MILLIBAR"] = \
        lambda: noconversion("ZETTAPASCAL", "MILLIBAR")
    CONVERSIONS["ZETTAPASCAL:MILLIPASCAL"] = \
        lambda value: (10 ** 24) * value
    CONVERSIONS["ZETTAPASCAL:MILLITORR"] = \
        lambda: noconversion("ZETTAPASCAL", "MILLITORR")
    CONVERSIONS["ZETTAPASCAL:MMHG"] = \
        lambda: noconversion("ZETTAPASCAL", "MMHG")
    CONVERSIONS["ZETTAPASCAL:NANOPASCAL"] = \
        lambda value: (10 ** 30) * value
    CONVERSIONS["ZETTAPASCAL:PETAPASCAL"] = \
        lambda value: (10 ** 6) * value
    CONVERSIONS["ZETTAPASCAL:PICOPASCAL"] = \
        lambda value: (10 ** 33) * value
    CONVERSIONS["ZETTAPASCAL:PSI"] = \
        lambda: noconversion("ZETTAPASCAL", "PSI")
    CONVERSIONS["ZETTAPASCAL:Pascal"] = \
        lambda value: (10 ** 21) * value
    CONVERSIONS["ZETTAPASCAL:TERAPASCAL"] = \
        lambda value: (10 ** 9) * value
    CONVERSIONS["ZETTAPASCAL:TORR"] = \
        lambda: noconversion("ZETTAPASCAL", "TORR")
    CONVERSIONS["ZETTAPASCAL:YOCTOPASCAL"] = \
        lambda value: (10 ** 45) * value
    CONVERSIONS["ZETTAPASCAL:YOTTAPASCAL"] = \
        lambda value: (10 ** -3) * value
    CONVERSIONS["ZETTAPASCAL:ZEPTOPASCAL"] = \
        lambda value: (10 ** 42) * value

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
