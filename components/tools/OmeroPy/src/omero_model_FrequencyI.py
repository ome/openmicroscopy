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
Code-generated omero.model.Frequency implementation,
based on omero.model.PermissionsI
"""


import Ice
import IceImport
IceImport.load("omero_model_Frequency_ice")
_omero = Ice.openModule("omero")
_omero_model = Ice.openModule("omero.model")
__name__ = "omero.model"

from omero_model_UnitBase import UnitBase
from omero.model.enums import UnitsFrequency


def noconversion(cfrom, cto):
    raise Exception(("Unsupported conversion: "
                     "%s:%s") % cfrom, cto)


class FrequencyI(_omero_model.Frequency, UnitBase):

    CONVERSIONS = dict()
    CONVERSIONS["ATTOHERTZ:CENTIHERTZ"] = \
        lambda value: (10 ** -16) * value
    CONVERSIONS["ATTOHERTZ:DECAHERTZ"] = \
        lambda value: (10 ** -19) * value
    CONVERSIONS["ATTOHERTZ:DECIHERTZ"] = \
        lambda value: (10 ** -17) * value
    CONVERSIONS["ATTOHERTZ:EXAHERTZ"] = \
        lambda value: (10 ** -36) * value
    CONVERSIONS["ATTOHERTZ:FEMTOHERTZ"] = \
        lambda value: (10 ** -3) * value
    CONVERSIONS["ATTOHERTZ:GIGAHERTZ"] = \
        lambda value: (10 ** -27) * value
    CONVERSIONS["ATTOHERTZ:HECTOHERTZ"] = \
        lambda value: (10 ** -20) * value
    CONVERSIONS["ATTOHERTZ:HERTZ"] = \
        lambda value: (10 ** -18) * value
    CONVERSIONS["ATTOHERTZ:KILOHERTZ"] = \
        lambda value: (10 ** -21) * value
    CONVERSIONS["ATTOHERTZ:MEGAHERTZ"] = \
        lambda value: (10 ** -24) * value
    CONVERSIONS["ATTOHERTZ:MICROHERTZ"] = \
        lambda value: (10 ** -12) * value
    CONVERSIONS["ATTOHERTZ:MILLIHERTZ"] = \
        lambda value: (10 ** -15) * value
    CONVERSIONS["ATTOHERTZ:NANOHERTZ"] = \
        lambda value: (10 ** -9) * value
    CONVERSIONS["ATTOHERTZ:PETAHERTZ"] = \
        lambda value: (10 ** -33) * value
    CONVERSIONS["ATTOHERTZ:PICOHERTZ"] = \
        lambda value: (10 ** -6) * value
    CONVERSIONS["ATTOHERTZ:TERAHERTZ"] = \
        lambda value: (10 ** -30) * value
    CONVERSIONS["ATTOHERTZ:YOCTOHERTZ"] = \
        lambda value: (10 ** 6) * value
    CONVERSIONS["ATTOHERTZ:YOTTAHERTZ"] = \
        lambda value: (10 ** -42) * value
    CONVERSIONS["ATTOHERTZ:ZEPTOHERTZ"] = \
        lambda value: (10 ** 3) * value
    CONVERSIONS["ATTOHERTZ:ZETTAHERTZ"] = \
        lambda value: (10 ** -39) * value
    CONVERSIONS["CENTIHERTZ:ATTOHERTZ"] = \
        lambda value: (10 ** 16) * value
    CONVERSIONS["CENTIHERTZ:DECAHERTZ"] = \
        lambda value: (10 ** -3) * value
    CONVERSIONS["CENTIHERTZ:DECIHERTZ"] = \
        lambda value: (10 ** -1) * value
    CONVERSIONS["CENTIHERTZ:EXAHERTZ"] = \
        lambda value: (10 ** -20) * value
    CONVERSIONS["CENTIHERTZ:FEMTOHERTZ"] = \
        lambda value: (10 ** 13) * value
    CONVERSIONS["CENTIHERTZ:GIGAHERTZ"] = \
        lambda value: (10 ** -11) * value
    CONVERSIONS["CENTIHERTZ:HECTOHERTZ"] = \
        lambda value: (10 ** -4) * value
    CONVERSIONS["CENTIHERTZ:HERTZ"] = \
        lambda value: (10 ** -2) * value
    CONVERSIONS["CENTIHERTZ:KILOHERTZ"] = \
        lambda value: (10 ** -5) * value
    CONVERSIONS["CENTIHERTZ:MEGAHERTZ"] = \
        lambda value: (10 ** -8) * value
    CONVERSIONS["CENTIHERTZ:MICROHERTZ"] = \
        lambda value: (10 ** 4) * value
    CONVERSIONS["CENTIHERTZ:MILLIHERTZ"] = \
        lambda value: 10 * value
    CONVERSIONS["CENTIHERTZ:NANOHERTZ"] = \
        lambda value: (10 ** 7) * value
    CONVERSIONS["CENTIHERTZ:PETAHERTZ"] = \
        lambda value: (10 ** -17) * value
    CONVERSIONS["CENTIHERTZ:PICOHERTZ"] = \
        lambda value: (10 ** 10) * value
    CONVERSIONS["CENTIHERTZ:TERAHERTZ"] = \
        lambda value: (10 ** -14) * value
    CONVERSIONS["CENTIHERTZ:YOCTOHERTZ"] = \
        lambda value: (10 ** 22) * value
    CONVERSIONS["CENTIHERTZ:YOTTAHERTZ"] = \
        lambda value: (10 ** -26) * value
    CONVERSIONS["CENTIHERTZ:ZEPTOHERTZ"] = \
        lambda value: (10 ** 19) * value
    CONVERSIONS["CENTIHERTZ:ZETTAHERTZ"] = \
        lambda value: (10 ** -23) * value
    CONVERSIONS["DECAHERTZ:ATTOHERTZ"] = \
        lambda value: (10 ** 19) * value
    CONVERSIONS["DECAHERTZ:CENTIHERTZ"] = \
        lambda value: (10 ** 3) * value
    CONVERSIONS["DECAHERTZ:DECIHERTZ"] = \
        lambda value: (10 ** 2) * value
    CONVERSIONS["DECAHERTZ:EXAHERTZ"] = \
        lambda value: (10 ** -17) * value
    CONVERSIONS["DECAHERTZ:FEMTOHERTZ"] = \
        lambda value: (10 ** 16) * value
    CONVERSIONS["DECAHERTZ:GIGAHERTZ"] = \
        lambda value: (10 ** -8) * value
    CONVERSIONS["DECAHERTZ:HECTOHERTZ"] = \
        lambda value: (10 ** -1) * value
    CONVERSIONS["DECAHERTZ:HERTZ"] = \
        lambda value: 10 * value
    CONVERSIONS["DECAHERTZ:KILOHERTZ"] = \
        lambda value: (10 ** -2) * value
    CONVERSIONS["DECAHERTZ:MEGAHERTZ"] = \
        lambda value: (10 ** -5) * value
    CONVERSIONS["DECAHERTZ:MICROHERTZ"] = \
        lambda value: (10 ** 7) * value
    CONVERSIONS["DECAHERTZ:MILLIHERTZ"] = \
        lambda value: (10 ** 4) * value
    CONVERSIONS["DECAHERTZ:NANOHERTZ"] = \
        lambda value: (10 ** 10) * value
    CONVERSIONS["DECAHERTZ:PETAHERTZ"] = \
        lambda value: (10 ** -14) * value
    CONVERSIONS["DECAHERTZ:PICOHERTZ"] = \
        lambda value: (10 ** 13) * value
    CONVERSIONS["DECAHERTZ:TERAHERTZ"] = \
        lambda value: (10 ** -11) * value
    CONVERSIONS["DECAHERTZ:YOCTOHERTZ"] = \
        lambda value: (10 ** 25) * value
    CONVERSIONS["DECAHERTZ:YOTTAHERTZ"] = \
        lambda value: (10 ** -23) * value
    CONVERSIONS["DECAHERTZ:ZEPTOHERTZ"] = \
        lambda value: (10 ** 22) * value
    CONVERSIONS["DECAHERTZ:ZETTAHERTZ"] = \
        lambda value: (10 ** -20) * value
    CONVERSIONS["DECIHERTZ:ATTOHERTZ"] = \
        lambda value: (10 ** 17) * value
    CONVERSIONS["DECIHERTZ:CENTIHERTZ"] = \
        lambda value: 10 * value
    CONVERSIONS["DECIHERTZ:DECAHERTZ"] = \
        lambda value: (10 ** -2) * value
    CONVERSIONS["DECIHERTZ:EXAHERTZ"] = \
        lambda value: (10 ** -19) * value
    CONVERSIONS["DECIHERTZ:FEMTOHERTZ"] = \
        lambda value: (10 ** 14) * value
    CONVERSIONS["DECIHERTZ:GIGAHERTZ"] = \
        lambda value: (10 ** -10) * value
    CONVERSIONS["DECIHERTZ:HECTOHERTZ"] = \
        lambda value: (10 ** -3) * value
    CONVERSIONS["DECIHERTZ:HERTZ"] = \
        lambda value: (10 ** -1) * value
    CONVERSIONS["DECIHERTZ:KILOHERTZ"] = \
        lambda value: (10 ** -4) * value
    CONVERSIONS["DECIHERTZ:MEGAHERTZ"] = \
        lambda value: (10 ** -7) * value
    CONVERSIONS["DECIHERTZ:MICROHERTZ"] = \
        lambda value: (10 ** 5) * value
    CONVERSIONS["DECIHERTZ:MILLIHERTZ"] = \
        lambda value: (10 ** 2) * value
    CONVERSIONS["DECIHERTZ:NANOHERTZ"] = \
        lambda value: (10 ** 8) * value
    CONVERSIONS["DECIHERTZ:PETAHERTZ"] = \
        lambda value: (10 ** -16) * value
    CONVERSIONS["DECIHERTZ:PICOHERTZ"] = \
        lambda value: (10 ** 11) * value
    CONVERSIONS["DECIHERTZ:TERAHERTZ"] = \
        lambda value: (10 ** -13) * value
    CONVERSIONS["DECIHERTZ:YOCTOHERTZ"] = \
        lambda value: (10 ** 23) * value
    CONVERSIONS["DECIHERTZ:YOTTAHERTZ"] = \
        lambda value: (10 ** -25) * value
    CONVERSIONS["DECIHERTZ:ZEPTOHERTZ"] = \
        lambda value: (10 ** 20) * value
    CONVERSIONS["DECIHERTZ:ZETTAHERTZ"] = \
        lambda value: (10 ** -22) * value
    CONVERSIONS["EXAHERTZ:ATTOHERTZ"] = \
        lambda value: (10 ** 36) * value
    CONVERSIONS["EXAHERTZ:CENTIHERTZ"] = \
        lambda value: (10 ** 20) * value
    CONVERSIONS["EXAHERTZ:DECAHERTZ"] = \
        lambda value: (10 ** 17) * value
    CONVERSIONS["EXAHERTZ:DECIHERTZ"] = \
        lambda value: (10 ** 19) * value
    CONVERSIONS["EXAHERTZ:FEMTOHERTZ"] = \
        lambda value: (10 ** 33) * value
    CONVERSIONS["EXAHERTZ:GIGAHERTZ"] = \
        lambda value: (10 ** 9) * value
    CONVERSIONS["EXAHERTZ:HECTOHERTZ"] = \
        lambda value: (10 ** 16) * value
    CONVERSIONS["EXAHERTZ:HERTZ"] = \
        lambda value: (10 ** 18) * value
    CONVERSIONS["EXAHERTZ:KILOHERTZ"] = \
        lambda value: (10 ** 15) * value
    CONVERSIONS["EXAHERTZ:MEGAHERTZ"] = \
        lambda value: (10 ** 12) * value
    CONVERSIONS["EXAHERTZ:MICROHERTZ"] = \
        lambda value: (10 ** 24) * value
    CONVERSIONS["EXAHERTZ:MILLIHERTZ"] = \
        lambda value: (10 ** 21) * value
    CONVERSIONS["EXAHERTZ:NANOHERTZ"] = \
        lambda value: (10 ** 27) * value
    CONVERSIONS["EXAHERTZ:PETAHERTZ"] = \
        lambda value: (10 ** 3) * value
    CONVERSIONS["EXAHERTZ:PICOHERTZ"] = \
        lambda value: (10 ** 30) * value
    CONVERSIONS["EXAHERTZ:TERAHERTZ"] = \
        lambda value: (10 ** 6) * value
    CONVERSIONS["EXAHERTZ:YOCTOHERTZ"] = \
        lambda value: (10 ** 42) * value
    CONVERSIONS["EXAHERTZ:YOTTAHERTZ"] = \
        lambda value: (10 ** -6) * value
    CONVERSIONS["EXAHERTZ:ZEPTOHERTZ"] = \
        lambda value: (10 ** 39) * value
    CONVERSIONS["EXAHERTZ:ZETTAHERTZ"] = \
        lambda value: (10 ** -3) * value
    CONVERSIONS["FEMTOHERTZ:ATTOHERTZ"] = \
        lambda value: (10 ** 3) * value
    CONVERSIONS["FEMTOHERTZ:CENTIHERTZ"] = \
        lambda value: (10 ** -13) * value
    CONVERSIONS["FEMTOHERTZ:DECAHERTZ"] = \
        lambda value: (10 ** -16) * value
    CONVERSIONS["FEMTOHERTZ:DECIHERTZ"] = \
        lambda value: (10 ** -14) * value
    CONVERSIONS["FEMTOHERTZ:EXAHERTZ"] = \
        lambda value: (10 ** -33) * value
    CONVERSIONS["FEMTOHERTZ:GIGAHERTZ"] = \
        lambda value: (10 ** -24) * value
    CONVERSIONS["FEMTOHERTZ:HECTOHERTZ"] = \
        lambda value: (10 ** -17) * value
    CONVERSIONS["FEMTOHERTZ:HERTZ"] = \
        lambda value: (10 ** -15) * value
    CONVERSIONS["FEMTOHERTZ:KILOHERTZ"] = \
        lambda value: (10 ** -18) * value
    CONVERSIONS["FEMTOHERTZ:MEGAHERTZ"] = \
        lambda value: (10 ** -21) * value
    CONVERSIONS["FEMTOHERTZ:MICROHERTZ"] = \
        lambda value: (10 ** -9) * value
    CONVERSIONS["FEMTOHERTZ:MILLIHERTZ"] = \
        lambda value: (10 ** -12) * value
    CONVERSIONS["FEMTOHERTZ:NANOHERTZ"] = \
        lambda value: (10 ** -6) * value
    CONVERSIONS["FEMTOHERTZ:PETAHERTZ"] = \
        lambda value: (10 ** -30) * value
    CONVERSIONS["FEMTOHERTZ:PICOHERTZ"] = \
        lambda value: (10 ** -3) * value
    CONVERSIONS["FEMTOHERTZ:TERAHERTZ"] = \
        lambda value: (10 ** -27) * value
    CONVERSIONS["FEMTOHERTZ:YOCTOHERTZ"] = \
        lambda value: (10 ** 9) * value
    CONVERSIONS["FEMTOHERTZ:YOTTAHERTZ"] = \
        lambda value: (10 ** -39) * value
    CONVERSIONS["FEMTOHERTZ:ZEPTOHERTZ"] = \
        lambda value: (10 ** 6) * value
    CONVERSIONS["FEMTOHERTZ:ZETTAHERTZ"] = \
        lambda value: (10 ** -36) * value
    CONVERSIONS["GIGAHERTZ:ATTOHERTZ"] = \
        lambda value: (10 ** 27) * value
    CONVERSIONS["GIGAHERTZ:CENTIHERTZ"] = \
        lambda value: (10 ** 11) * value
    CONVERSIONS["GIGAHERTZ:DECAHERTZ"] = \
        lambda value: (10 ** 8) * value
    CONVERSIONS["GIGAHERTZ:DECIHERTZ"] = \
        lambda value: (10 ** 10) * value
    CONVERSIONS["GIGAHERTZ:EXAHERTZ"] = \
        lambda value: (10 ** -9) * value
    CONVERSIONS["GIGAHERTZ:FEMTOHERTZ"] = \
        lambda value: (10 ** 24) * value
    CONVERSIONS["GIGAHERTZ:HECTOHERTZ"] = \
        lambda value: (10 ** 7) * value
    CONVERSIONS["GIGAHERTZ:HERTZ"] = \
        lambda value: (10 ** 9) * value
    CONVERSIONS["GIGAHERTZ:KILOHERTZ"] = \
        lambda value: (10 ** 6) * value
    CONVERSIONS["GIGAHERTZ:MEGAHERTZ"] = \
        lambda value: (10 ** 3) * value
    CONVERSIONS["GIGAHERTZ:MICROHERTZ"] = \
        lambda value: (10 ** 15) * value
    CONVERSIONS["GIGAHERTZ:MILLIHERTZ"] = \
        lambda value: (10 ** 12) * value
    CONVERSIONS["GIGAHERTZ:NANOHERTZ"] = \
        lambda value: (10 ** 18) * value
    CONVERSIONS["GIGAHERTZ:PETAHERTZ"] = \
        lambda value: (10 ** -6) * value
    CONVERSIONS["GIGAHERTZ:PICOHERTZ"] = \
        lambda value: (10 ** 21) * value
    CONVERSIONS["GIGAHERTZ:TERAHERTZ"] = \
        lambda value: (10 ** -3) * value
    CONVERSIONS["GIGAHERTZ:YOCTOHERTZ"] = \
        lambda value: (10 ** 33) * value
    CONVERSIONS["GIGAHERTZ:YOTTAHERTZ"] = \
        lambda value: (10 ** -15) * value
    CONVERSIONS["GIGAHERTZ:ZEPTOHERTZ"] = \
        lambda value: (10 ** 30) * value
    CONVERSIONS["GIGAHERTZ:ZETTAHERTZ"] = \
        lambda value: (10 ** -12) * value
    CONVERSIONS["HECTOHERTZ:ATTOHERTZ"] = \
        lambda value: (10 ** 20) * value
    CONVERSIONS["HECTOHERTZ:CENTIHERTZ"] = \
        lambda value: (10 ** 4) * value
    CONVERSIONS["HECTOHERTZ:DECAHERTZ"] = \
        lambda value: 10 * value
    CONVERSIONS["HECTOHERTZ:DECIHERTZ"] = \
        lambda value: (10 ** 3) * value
    CONVERSIONS["HECTOHERTZ:EXAHERTZ"] = \
        lambda value: (10 ** -16) * value
    CONVERSIONS["HECTOHERTZ:FEMTOHERTZ"] = \
        lambda value: (10 ** 17) * value
    CONVERSIONS["HECTOHERTZ:GIGAHERTZ"] = \
        lambda value: (10 ** -7) * value
    CONVERSIONS["HECTOHERTZ:HERTZ"] = \
        lambda value: (10 ** 2) * value
    CONVERSIONS["HECTOHERTZ:KILOHERTZ"] = \
        lambda value: (10 ** -1) * value
    CONVERSIONS["HECTOHERTZ:MEGAHERTZ"] = \
        lambda value: (10 ** -4) * value
    CONVERSIONS["HECTOHERTZ:MICROHERTZ"] = \
        lambda value: (10 ** 8) * value
    CONVERSIONS["HECTOHERTZ:MILLIHERTZ"] = \
        lambda value: (10 ** 5) * value
    CONVERSIONS["HECTOHERTZ:NANOHERTZ"] = \
        lambda value: (10 ** 11) * value
    CONVERSIONS["HECTOHERTZ:PETAHERTZ"] = \
        lambda value: (10 ** -13) * value
    CONVERSIONS["HECTOHERTZ:PICOHERTZ"] = \
        lambda value: (10 ** 14) * value
    CONVERSIONS["HECTOHERTZ:TERAHERTZ"] = \
        lambda value: (10 ** -10) * value
    CONVERSIONS["HECTOHERTZ:YOCTOHERTZ"] = \
        lambda value: (10 ** 26) * value
    CONVERSIONS["HECTOHERTZ:YOTTAHERTZ"] = \
        lambda value: (10 ** -22) * value
    CONVERSIONS["HECTOHERTZ:ZEPTOHERTZ"] = \
        lambda value: (10 ** 23) * value
    CONVERSIONS["HECTOHERTZ:ZETTAHERTZ"] = \
        lambda value: (10 ** -19) * value
    CONVERSIONS["HERTZ:ATTOHERTZ"] = \
        lambda value: (10 ** 18) * value
    CONVERSIONS["HERTZ:CENTIHERTZ"] = \
        lambda value: (10 ** 2) * value
    CONVERSIONS["HERTZ:DECAHERTZ"] = \
        lambda value: (10 ** -1) * value
    CONVERSIONS["HERTZ:DECIHERTZ"] = \
        lambda value: 10 * value
    CONVERSIONS["HERTZ:EXAHERTZ"] = \
        lambda value: (10 ** -18) * value
    CONVERSIONS["HERTZ:FEMTOHERTZ"] = \
        lambda value: (10 ** 15) * value
    CONVERSIONS["HERTZ:GIGAHERTZ"] = \
        lambda value: (10 ** -9) * value
    CONVERSIONS["HERTZ:HECTOHERTZ"] = \
        lambda value: (10 ** -2) * value
    CONVERSIONS["HERTZ:KILOHERTZ"] = \
        lambda value: (10 ** -3) * value
    CONVERSIONS["HERTZ:MEGAHERTZ"] = \
        lambda value: (10 ** -6) * value
    CONVERSIONS["HERTZ:MICROHERTZ"] = \
        lambda value: (10 ** 6) * value
    CONVERSIONS["HERTZ:MILLIHERTZ"] = \
        lambda value: (10 ** 3) * value
    CONVERSIONS["HERTZ:NANOHERTZ"] = \
        lambda value: (10 ** 9) * value
    CONVERSIONS["HERTZ:PETAHERTZ"] = \
        lambda value: (10 ** -15) * value
    CONVERSIONS["HERTZ:PICOHERTZ"] = \
        lambda value: (10 ** 12) * value
    CONVERSIONS["HERTZ:TERAHERTZ"] = \
        lambda value: (10 ** -12) * value
    CONVERSIONS["HERTZ:YOCTOHERTZ"] = \
        lambda value: (10 ** 24) * value
    CONVERSIONS["HERTZ:YOTTAHERTZ"] = \
        lambda value: (10 ** -24) * value
    CONVERSIONS["HERTZ:ZEPTOHERTZ"] = \
        lambda value: (10 ** 21) * value
    CONVERSIONS["HERTZ:ZETTAHERTZ"] = \
        lambda value: (10 ** -21) * value
    CONVERSIONS["KILOHERTZ:ATTOHERTZ"] = \
        lambda value: (10 ** 21) * value
    CONVERSIONS["KILOHERTZ:CENTIHERTZ"] = \
        lambda value: (10 ** 5) * value
    CONVERSIONS["KILOHERTZ:DECAHERTZ"] = \
        lambda value: (10 ** 2) * value
    CONVERSIONS["KILOHERTZ:DECIHERTZ"] = \
        lambda value: (10 ** 4) * value
    CONVERSIONS["KILOHERTZ:EXAHERTZ"] = \
        lambda value: (10 ** -15) * value
    CONVERSIONS["KILOHERTZ:FEMTOHERTZ"] = \
        lambda value: (10 ** 18) * value
    CONVERSIONS["KILOHERTZ:GIGAHERTZ"] = \
        lambda value: (10 ** -6) * value
    CONVERSIONS["KILOHERTZ:HECTOHERTZ"] = \
        lambda value: 10 * value
    CONVERSIONS["KILOHERTZ:HERTZ"] = \
        lambda value: (10 ** 3) * value
    CONVERSIONS["KILOHERTZ:MEGAHERTZ"] = \
        lambda value: (10 ** -3) * value
    CONVERSIONS["KILOHERTZ:MICROHERTZ"] = \
        lambda value: (10 ** 9) * value
    CONVERSIONS["KILOHERTZ:MILLIHERTZ"] = \
        lambda value: (10 ** 6) * value
    CONVERSIONS["KILOHERTZ:NANOHERTZ"] = \
        lambda value: (10 ** 12) * value
    CONVERSIONS["KILOHERTZ:PETAHERTZ"] = \
        lambda value: (10 ** -12) * value
    CONVERSIONS["KILOHERTZ:PICOHERTZ"] = \
        lambda value: (10 ** 15) * value
    CONVERSIONS["KILOHERTZ:TERAHERTZ"] = \
        lambda value: (10 ** -9) * value
    CONVERSIONS["KILOHERTZ:YOCTOHERTZ"] = \
        lambda value: (10 ** 27) * value
    CONVERSIONS["KILOHERTZ:YOTTAHERTZ"] = \
        lambda value: (10 ** -21) * value
    CONVERSIONS["KILOHERTZ:ZEPTOHERTZ"] = \
        lambda value: (10 ** 24) * value
    CONVERSIONS["KILOHERTZ:ZETTAHERTZ"] = \
        lambda value: (10 ** -18) * value
    CONVERSIONS["MEGAHERTZ:ATTOHERTZ"] = \
        lambda value: (10 ** 24) * value
    CONVERSIONS["MEGAHERTZ:CENTIHERTZ"] = \
        lambda value: (10 ** 8) * value
    CONVERSIONS["MEGAHERTZ:DECAHERTZ"] = \
        lambda value: (10 ** 5) * value
    CONVERSIONS["MEGAHERTZ:DECIHERTZ"] = \
        lambda value: (10 ** 7) * value
    CONVERSIONS["MEGAHERTZ:EXAHERTZ"] = \
        lambda value: (10 ** -12) * value
    CONVERSIONS["MEGAHERTZ:FEMTOHERTZ"] = \
        lambda value: (10 ** 21) * value
    CONVERSIONS["MEGAHERTZ:GIGAHERTZ"] = \
        lambda value: (10 ** -3) * value
    CONVERSIONS["MEGAHERTZ:HECTOHERTZ"] = \
        lambda value: (10 ** 4) * value
    CONVERSIONS["MEGAHERTZ:HERTZ"] = \
        lambda value: (10 ** 6) * value
    CONVERSIONS["MEGAHERTZ:KILOHERTZ"] = \
        lambda value: (10 ** 3) * value
    CONVERSIONS["MEGAHERTZ:MICROHERTZ"] = \
        lambda value: (10 ** 12) * value
    CONVERSIONS["MEGAHERTZ:MILLIHERTZ"] = \
        lambda value: (10 ** 9) * value
    CONVERSIONS["MEGAHERTZ:NANOHERTZ"] = \
        lambda value: (10 ** 15) * value
    CONVERSIONS["MEGAHERTZ:PETAHERTZ"] = \
        lambda value: (10 ** -9) * value
    CONVERSIONS["MEGAHERTZ:PICOHERTZ"] = \
        lambda value: (10 ** 18) * value
    CONVERSIONS["MEGAHERTZ:TERAHERTZ"] = \
        lambda value: (10 ** -6) * value
    CONVERSIONS["MEGAHERTZ:YOCTOHERTZ"] = \
        lambda value: (10 ** 30) * value
    CONVERSIONS["MEGAHERTZ:YOTTAHERTZ"] = \
        lambda value: (10 ** -18) * value
    CONVERSIONS["MEGAHERTZ:ZEPTOHERTZ"] = \
        lambda value: (10 ** 27) * value
    CONVERSIONS["MEGAHERTZ:ZETTAHERTZ"] = \
        lambda value: (10 ** -15) * value
    CONVERSIONS["MICROHERTZ:ATTOHERTZ"] = \
        lambda value: (10 ** 12) * value
    CONVERSIONS["MICROHERTZ:CENTIHERTZ"] = \
        lambda value: (10 ** -4) * value
    CONVERSIONS["MICROHERTZ:DECAHERTZ"] = \
        lambda value: (10 ** -7) * value
    CONVERSIONS["MICROHERTZ:DECIHERTZ"] = \
        lambda value: (10 ** -5) * value
    CONVERSIONS["MICROHERTZ:EXAHERTZ"] = \
        lambda value: (10 ** -24) * value
    CONVERSIONS["MICROHERTZ:FEMTOHERTZ"] = \
        lambda value: (10 ** 9) * value
    CONVERSIONS["MICROHERTZ:GIGAHERTZ"] = \
        lambda value: (10 ** -15) * value
    CONVERSIONS["MICROHERTZ:HECTOHERTZ"] = \
        lambda value: (10 ** -8) * value
    CONVERSIONS["MICROHERTZ:HERTZ"] = \
        lambda value: (10 ** -6) * value
    CONVERSIONS["MICROHERTZ:KILOHERTZ"] = \
        lambda value: (10 ** -9) * value
    CONVERSIONS["MICROHERTZ:MEGAHERTZ"] = \
        lambda value: (10 ** -12) * value
    CONVERSIONS["MICROHERTZ:MILLIHERTZ"] = \
        lambda value: (10 ** -3) * value
    CONVERSIONS["MICROHERTZ:NANOHERTZ"] = \
        lambda value: (10 ** 3) * value
    CONVERSIONS["MICROHERTZ:PETAHERTZ"] = \
        lambda value: (10 ** -21) * value
    CONVERSIONS["MICROHERTZ:PICOHERTZ"] = \
        lambda value: (10 ** 6) * value
    CONVERSIONS["MICROHERTZ:TERAHERTZ"] = \
        lambda value: (10 ** -18) * value
    CONVERSIONS["MICROHERTZ:YOCTOHERTZ"] = \
        lambda value: (10 ** 18) * value
    CONVERSIONS["MICROHERTZ:YOTTAHERTZ"] = \
        lambda value: (10 ** -30) * value
    CONVERSIONS["MICROHERTZ:ZEPTOHERTZ"] = \
        lambda value: (10 ** 15) * value
    CONVERSIONS["MICROHERTZ:ZETTAHERTZ"] = \
        lambda value: (10 ** -27) * value
    CONVERSIONS["MILLIHERTZ:ATTOHERTZ"] = \
        lambda value: (10 ** 15) * value
    CONVERSIONS["MILLIHERTZ:CENTIHERTZ"] = \
        lambda value: (10 ** -1) * value
    CONVERSIONS["MILLIHERTZ:DECAHERTZ"] = \
        lambda value: (10 ** -4) * value
    CONVERSIONS["MILLIHERTZ:DECIHERTZ"] = \
        lambda value: (10 ** -2) * value
    CONVERSIONS["MILLIHERTZ:EXAHERTZ"] = \
        lambda value: (10 ** -21) * value
    CONVERSIONS["MILLIHERTZ:FEMTOHERTZ"] = \
        lambda value: (10 ** 12) * value
    CONVERSIONS["MILLIHERTZ:GIGAHERTZ"] = \
        lambda value: (10 ** -12) * value
    CONVERSIONS["MILLIHERTZ:HECTOHERTZ"] = \
        lambda value: (10 ** -5) * value
    CONVERSIONS["MILLIHERTZ:HERTZ"] = \
        lambda value: (10 ** -3) * value
    CONVERSIONS["MILLIHERTZ:KILOHERTZ"] = \
        lambda value: (10 ** -6) * value
    CONVERSIONS["MILLIHERTZ:MEGAHERTZ"] = \
        lambda value: (10 ** -9) * value
    CONVERSIONS["MILLIHERTZ:MICROHERTZ"] = \
        lambda value: (10 ** 3) * value
    CONVERSIONS["MILLIHERTZ:NANOHERTZ"] = \
        lambda value: (10 ** 6) * value
    CONVERSIONS["MILLIHERTZ:PETAHERTZ"] = \
        lambda value: (10 ** -18) * value
    CONVERSIONS["MILLIHERTZ:PICOHERTZ"] = \
        lambda value: (10 ** 9) * value
    CONVERSIONS["MILLIHERTZ:TERAHERTZ"] = \
        lambda value: (10 ** -15) * value
    CONVERSIONS["MILLIHERTZ:YOCTOHERTZ"] = \
        lambda value: (10 ** 21) * value
    CONVERSIONS["MILLIHERTZ:YOTTAHERTZ"] = \
        lambda value: (10 ** -27) * value
    CONVERSIONS["MILLIHERTZ:ZEPTOHERTZ"] = \
        lambda value: (10 ** 18) * value
    CONVERSIONS["MILLIHERTZ:ZETTAHERTZ"] = \
        lambda value: (10 ** -24) * value
    CONVERSIONS["NANOHERTZ:ATTOHERTZ"] = \
        lambda value: (10 ** 9) * value
    CONVERSIONS["NANOHERTZ:CENTIHERTZ"] = \
        lambda value: (10 ** -7) * value
    CONVERSIONS["NANOHERTZ:DECAHERTZ"] = \
        lambda value: (10 ** -10) * value
    CONVERSIONS["NANOHERTZ:DECIHERTZ"] = \
        lambda value: (10 ** -8) * value
    CONVERSIONS["NANOHERTZ:EXAHERTZ"] = \
        lambda value: (10 ** -27) * value
    CONVERSIONS["NANOHERTZ:FEMTOHERTZ"] = \
        lambda value: (10 ** 6) * value
    CONVERSIONS["NANOHERTZ:GIGAHERTZ"] = \
        lambda value: (10 ** -18) * value
    CONVERSIONS["NANOHERTZ:HECTOHERTZ"] = \
        lambda value: (10 ** -11) * value
    CONVERSIONS["NANOHERTZ:HERTZ"] = \
        lambda value: (10 ** -9) * value
    CONVERSIONS["NANOHERTZ:KILOHERTZ"] = \
        lambda value: (10 ** -12) * value
    CONVERSIONS["NANOHERTZ:MEGAHERTZ"] = \
        lambda value: (10 ** -15) * value
    CONVERSIONS["NANOHERTZ:MICROHERTZ"] = \
        lambda value: (10 ** -3) * value
    CONVERSIONS["NANOHERTZ:MILLIHERTZ"] = \
        lambda value: (10 ** -6) * value
    CONVERSIONS["NANOHERTZ:PETAHERTZ"] = \
        lambda value: (10 ** -24) * value
    CONVERSIONS["NANOHERTZ:PICOHERTZ"] = \
        lambda value: (10 ** 3) * value
    CONVERSIONS["NANOHERTZ:TERAHERTZ"] = \
        lambda value: (10 ** -21) * value
    CONVERSIONS["NANOHERTZ:YOCTOHERTZ"] = \
        lambda value: (10 ** 15) * value
    CONVERSIONS["NANOHERTZ:YOTTAHERTZ"] = \
        lambda value: (10 ** -33) * value
    CONVERSIONS["NANOHERTZ:ZEPTOHERTZ"] = \
        lambda value: (10 ** 12) * value
    CONVERSIONS["NANOHERTZ:ZETTAHERTZ"] = \
        lambda value: (10 ** -30) * value
    CONVERSIONS["PETAHERTZ:ATTOHERTZ"] = \
        lambda value: (10 ** 33) * value
    CONVERSIONS["PETAHERTZ:CENTIHERTZ"] = \
        lambda value: (10 ** 17) * value
    CONVERSIONS["PETAHERTZ:DECAHERTZ"] = \
        lambda value: (10 ** 14) * value
    CONVERSIONS["PETAHERTZ:DECIHERTZ"] = \
        lambda value: (10 ** 16) * value
    CONVERSIONS["PETAHERTZ:EXAHERTZ"] = \
        lambda value: (10 ** -3) * value
    CONVERSIONS["PETAHERTZ:FEMTOHERTZ"] = \
        lambda value: (10 ** 30) * value
    CONVERSIONS["PETAHERTZ:GIGAHERTZ"] = \
        lambda value: (10 ** 6) * value
    CONVERSIONS["PETAHERTZ:HECTOHERTZ"] = \
        lambda value: (10 ** 13) * value
    CONVERSIONS["PETAHERTZ:HERTZ"] = \
        lambda value: (10 ** 15) * value
    CONVERSIONS["PETAHERTZ:KILOHERTZ"] = \
        lambda value: (10 ** 12) * value
    CONVERSIONS["PETAHERTZ:MEGAHERTZ"] = \
        lambda value: (10 ** 9) * value
    CONVERSIONS["PETAHERTZ:MICROHERTZ"] = \
        lambda value: (10 ** 21) * value
    CONVERSIONS["PETAHERTZ:MILLIHERTZ"] = \
        lambda value: (10 ** 18) * value
    CONVERSIONS["PETAHERTZ:NANOHERTZ"] = \
        lambda value: (10 ** 24) * value
    CONVERSIONS["PETAHERTZ:PICOHERTZ"] = \
        lambda value: (10 ** 27) * value
    CONVERSIONS["PETAHERTZ:TERAHERTZ"] = \
        lambda value: (10 ** 3) * value
    CONVERSIONS["PETAHERTZ:YOCTOHERTZ"] = \
        lambda value: (10 ** 39) * value
    CONVERSIONS["PETAHERTZ:YOTTAHERTZ"] = \
        lambda value: (10 ** -9) * value
    CONVERSIONS["PETAHERTZ:ZEPTOHERTZ"] = \
        lambda value: (10 ** 36) * value
    CONVERSIONS["PETAHERTZ:ZETTAHERTZ"] = \
        lambda value: (10 ** -6) * value
    CONVERSIONS["PICOHERTZ:ATTOHERTZ"] = \
        lambda value: (10 ** 6) * value
    CONVERSIONS["PICOHERTZ:CENTIHERTZ"] = \
        lambda value: (10 ** -10) * value
    CONVERSIONS["PICOHERTZ:DECAHERTZ"] = \
        lambda value: (10 ** -13) * value
    CONVERSIONS["PICOHERTZ:DECIHERTZ"] = \
        lambda value: (10 ** -11) * value
    CONVERSIONS["PICOHERTZ:EXAHERTZ"] = \
        lambda value: (10 ** -30) * value
    CONVERSIONS["PICOHERTZ:FEMTOHERTZ"] = \
        lambda value: (10 ** 3) * value
    CONVERSIONS["PICOHERTZ:GIGAHERTZ"] = \
        lambda value: (10 ** -21) * value
    CONVERSIONS["PICOHERTZ:HECTOHERTZ"] = \
        lambda value: (10 ** -14) * value
    CONVERSIONS["PICOHERTZ:HERTZ"] = \
        lambda value: (10 ** -12) * value
    CONVERSIONS["PICOHERTZ:KILOHERTZ"] = \
        lambda value: (10 ** -15) * value
    CONVERSIONS["PICOHERTZ:MEGAHERTZ"] = \
        lambda value: (10 ** -18) * value
    CONVERSIONS["PICOHERTZ:MICROHERTZ"] = \
        lambda value: (10 ** -6) * value
    CONVERSIONS["PICOHERTZ:MILLIHERTZ"] = \
        lambda value: (10 ** -9) * value
    CONVERSIONS["PICOHERTZ:NANOHERTZ"] = \
        lambda value: (10 ** -3) * value
    CONVERSIONS["PICOHERTZ:PETAHERTZ"] = \
        lambda value: (10 ** -27) * value
    CONVERSIONS["PICOHERTZ:TERAHERTZ"] = \
        lambda value: (10 ** -24) * value
    CONVERSIONS["PICOHERTZ:YOCTOHERTZ"] = \
        lambda value: (10 ** 12) * value
    CONVERSIONS["PICOHERTZ:YOTTAHERTZ"] = \
        lambda value: (10 ** -36) * value
    CONVERSIONS["PICOHERTZ:ZEPTOHERTZ"] = \
        lambda value: (10 ** 9) * value
    CONVERSIONS["PICOHERTZ:ZETTAHERTZ"] = \
        lambda value: (10 ** -33) * value
    CONVERSIONS["TERAHERTZ:ATTOHERTZ"] = \
        lambda value: (10 ** 30) * value
    CONVERSIONS["TERAHERTZ:CENTIHERTZ"] = \
        lambda value: (10 ** 14) * value
    CONVERSIONS["TERAHERTZ:DECAHERTZ"] = \
        lambda value: (10 ** 11) * value
    CONVERSIONS["TERAHERTZ:DECIHERTZ"] = \
        lambda value: (10 ** 13) * value
    CONVERSIONS["TERAHERTZ:EXAHERTZ"] = \
        lambda value: (10 ** -6) * value
    CONVERSIONS["TERAHERTZ:FEMTOHERTZ"] = \
        lambda value: (10 ** 27) * value
    CONVERSIONS["TERAHERTZ:GIGAHERTZ"] = \
        lambda value: (10 ** 3) * value
    CONVERSIONS["TERAHERTZ:HECTOHERTZ"] = \
        lambda value: (10 ** 10) * value
    CONVERSIONS["TERAHERTZ:HERTZ"] = \
        lambda value: (10 ** 12) * value
    CONVERSIONS["TERAHERTZ:KILOHERTZ"] = \
        lambda value: (10 ** 9) * value
    CONVERSIONS["TERAHERTZ:MEGAHERTZ"] = \
        lambda value: (10 ** 6) * value
    CONVERSIONS["TERAHERTZ:MICROHERTZ"] = \
        lambda value: (10 ** 18) * value
    CONVERSIONS["TERAHERTZ:MILLIHERTZ"] = \
        lambda value: (10 ** 15) * value
    CONVERSIONS["TERAHERTZ:NANOHERTZ"] = \
        lambda value: (10 ** 21) * value
    CONVERSIONS["TERAHERTZ:PETAHERTZ"] = \
        lambda value: (10 ** -3) * value
    CONVERSIONS["TERAHERTZ:PICOHERTZ"] = \
        lambda value: (10 ** 24) * value
    CONVERSIONS["TERAHERTZ:YOCTOHERTZ"] = \
        lambda value: (10 ** 36) * value
    CONVERSIONS["TERAHERTZ:YOTTAHERTZ"] = \
        lambda value: (10 ** -12) * value
    CONVERSIONS["TERAHERTZ:ZEPTOHERTZ"] = \
        lambda value: (10 ** 33) * value
    CONVERSIONS["TERAHERTZ:ZETTAHERTZ"] = \
        lambda value: (10 ** -9) * value
    CONVERSIONS["YOCTOHERTZ:ATTOHERTZ"] = \
        lambda value: (10 ** -6) * value
    CONVERSIONS["YOCTOHERTZ:CENTIHERTZ"] = \
        lambda value: (10 ** -22) * value
    CONVERSIONS["YOCTOHERTZ:DECAHERTZ"] = \
        lambda value: (10 ** -25) * value
    CONVERSIONS["YOCTOHERTZ:DECIHERTZ"] = \
        lambda value: (10 ** -23) * value
    CONVERSIONS["YOCTOHERTZ:EXAHERTZ"] = \
        lambda value: (10 ** -42) * value
    CONVERSIONS["YOCTOHERTZ:FEMTOHERTZ"] = \
        lambda value: (10 ** -9) * value
    CONVERSIONS["YOCTOHERTZ:GIGAHERTZ"] = \
        lambda value: (10 ** -33) * value
    CONVERSIONS["YOCTOHERTZ:HECTOHERTZ"] = \
        lambda value: (10 ** -26) * value
    CONVERSIONS["YOCTOHERTZ:HERTZ"] = \
        lambda value: (10 ** -24) * value
    CONVERSIONS["YOCTOHERTZ:KILOHERTZ"] = \
        lambda value: (10 ** -27) * value
    CONVERSIONS["YOCTOHERTZ:MEGAHERTZ"] = \
        lambda value: (10 ** -30) * value
    CONVERSIONS["YOCTOHERTZ:MICROHERTZ"] = \
        lambda value: (10 ** -18) * value
    CONVERSIONS["YOCTOHERTZ:MILLIHERTZ"] = \
        lambda value: (10 ** -21) * value
    CONVERSIONS["YOCTOHERTZ:NANOHERTZ"] = \
        lambda value: (10 ** -15) * value
    CONVERSIONS["YOCTOHERTZ:PETAHERTZ"] = \
        lambda value: (10 ** -39) * value
    CONVERSIONS["YOCTOHERTZ:PICOHERTZ"] = \
        lambda value: (10 ** -12) * value
    CONVERSIONS["YOCTOHERTZ:TERAHERTZ"] = \
        lambda value: (10 ** -36) * value
    CONVERSIONS["YOCTOHERTZ:YOTTAHERTZ"] = \
        lambda value: (10 ** -48) * value
    CONVERSIONS["YOCTOHERTZ:ZEPTOHERTZ"] = \
        lambda value: (10 ** -3) * value
    CONVERSIONS["YOCTOHERTZ:ZETTAHERTZ"] = \
        lambda value: (10 ** -45) * value
    CONVERSIONS["YOTTAHERTZ:ATTOHERTZ"] = \
        lambda value: (10 ** 42) * value
    CONVERSIONS["YOTTAHERTZ:CENTIHERTZ"] = \
        lambda value: (10 ** 26) * value
    CONVERSIONS["YOTTAHERTZ:DECAHERTZ"] = \
        lambda value: (10 ** 23) * value
    CONVERSIONS["YOTTAHERTZ:DECIHERTZ"] = \
        lambda value: (10 ** 25) * value
    CONVERSIONS["YOTTAHERTZ:EXAHERTZ"] = \
        lambda value: (10 ** 6) * value
    CONVERSIONS["YOTTAHERTZ:FEMTOHERTZ"] = \
        lambda value: (10 ** 39) * value
    CONVERSIONS["YOTTAHERTZ:GIGAHERTZ"] = \
        lambda value: (10 ** 15) * value
    CONVERSIONS["YOTTAHERTZ:HECTOHERTZ"] = \
        lambda value: (10 ** 22) * value
    CONVERSIONS["YOTTAHERTZ:HERTZ"] = \
        lambda value: (10 ** 24) * value
    CONVERSIONS["YOTTAHERTZ:KILOHERTZ"] = \
        lambda value: (10 ** 21) * value
    CONVERSIONS["YOTTAHERTZ:MEGAHERTZ"] = \
        lambda value: (10 ** 18) * value
    CONVERSIONS["YOTTAHERTZ:MICROHERTZ"] = \
        lambda value: (10 ** 30) * value
    CONVERSIONS["YOTTAHERTZ:MILLIHERTZ"] = \
        lambda value: (10 ** 27) * value
    CONVERSIONS["YOTTAHERTZ:NANOHERTZ"] = \
        lambda value: (10 ** 33) * value
    CONVERSIONS["YOTTAHERTZ:PETAHERTZ"] = \
        lambda value: (10 ** 9) * value
    CONVERSIONS["YOTTAHERTZ:PICOHERTZ"] = \
        lambda value: (10 ** 36) * value
    CONVERSIONS["YOTTAHERTZ:TERAHERTZ"] = \
        lambda value: (10 ** 12) * value
    CONVERSIONS["YOTTAHERTZ:YOCTOHERTZ"] = \
        lambda value: (10 ** 48) * value
    CONVERSIONS["YOTTAHERTZ:ZEPTOHERTZ"] = \
        lambda value: (10 ** 45) * value
    CONVERSIONS["YOTTAHERTZ:ZETTAHERTZ"] = \
        lambda value: (10 ** 3) * value
    CONVERSIONS["ZEPTOHERTZ:ATTOHERTZ"] = \
        lambda value: (10 ** -3) * value
    CONVERSIONS["ZEPTOHERTZ:CENTIHERTZ"] = \
        lambda value: (10 ** -19) * value
    CONVERSIONS["ZEPTOHERTZ:DECAHERTZ"] = \
        lambda value: (10 ** -22) * value
    CONVERSIONS["ZEPTOHERTZ:DECIHERTZ"] = \
        lambda value: (10 ** -20) * value
    CONVERSIONS["ZEPTOHERTZ:EXAHERTZ"] = \
        lambda value: (10 ** -39) * value
    CONVERSIONS["ZEPTOHERTZ:FEMTOHERTZ"] = \
        lambda value: (10 ** -6) * value
    CONVERSIONS["ZEPTOHERTZ:GIGAHERTZ"] = \
        lambda value: (10 ** -30) * value
    CONVERSIONS["ZEPTOHERTZ:HECTOHERTZ"] = \
        lambda value: (10 ** -23) * value
    CONVERSIONS["ZEPTOHERTZ:HERTZ"] = \
        lambda value: (10 ** -21) * value
    CONVERSIONS["ZEPTOHERTZ:KILOHERTZ"] = \
        lambda value: (10 ** -24) * value
    CONVERSIONS["ZEPTOHERTZ:MEGAHERTZ"] = \
        lambda value: (10 ** -27) * value
    CONVERSIONS["ZEPTOHERTZ:MICROHERTZ"] = \
        lambda value: (10 ** -15) * value
    CONVERSIONS["ZEPTOHERTZ:MILLIHERTZ"] = \
        lambda value: (10 ** -18) * value
    CONVERSIONS["ZEPTOHERTZ:NANOHERTZ"] = \
        lambda value: (10 ** -12) * value
    CONVERSIONS["ZEPTOHERTZ:PETAHERTZ"] = \
        lambda value: (10 ** -36) * value
    CONVERSIONS["ZEPTOHERTZ:PICOHERTZ"] = \
        lambda value: (10 ** -9) * value
    CONVERSIONS["ZEPTOHERTZ:TERAHERTZ"] = \
        lambda value: (10 ** -33) * value
    CONVERSIONS["ZEPTOHERTZ:YOCTOHERTZ"] = \
        lambda value: (10 ** 3) * value
    CONVERSIONS["ZEPTOHERTZ:YOTTAHERTZ"] = \
        lambda value: (10 ** -45) * value
    CONVERSIONS["ZEPTOHERTZ:ZETTAHERTZ"] = \
        lambda value: (10 ** -42) * value
    CONVERSIONS["ZETTAHERTZ:ATTOHERTZ"] = \
        lambda value: (10 ** 39) * value
    CONVERSIONS["ZETTAHERTZ:CENTIHERTZ"] = \
        lambda value: (10 ** 23) * value
    CONVERSIONS["ZETTAHERTZ:DECAHERTZ"] = \
        lambda value: (10 ** 20) * value
    CONVERSIONS["ZETTAHERTZ:DECIHERTZ"] = \
        lambda value: (10 ** 22) * value
    CONVERSIONS["ZETTAHERTZ:EXAHERTZ"] = \
        lambda value: (10 ** 3) * value
    CONVERSIONS["ZETTAHERTZ:FEMTOHERTZ"] = \
        lambda value: (10 ** 36) * value
    CONVERSIONS["ZETTAHERTZ:GIGAHERTZ"] = \
        lambda value: (10 ** 12) * value
    CONVERSIONS["ZETTAHERTZ:HECTOHERTZ"] = \
        lambda value: (10 ** 19) * value
    CONVERSIONS["ZETTAHERTZ:HERTZ"] = \
        lambda value: (10 ** 21) * value
    CONVERSIONS["ZETTAHERTZ:KILOHERTZ"] = \
        lambda value: (10 ** 18) * value
    CONVERSIONS["ZETTAHERTZ:MEGAHERTZ"] = \
        lambda value: (10 ** 15) * value
    CONVERSIONS["ZETTAHERTZ:MICROHERTZ"] = \
        lambda value: (10 ** 27) * value
    CONVERSIONS["ZETTAHERTZ:MILLIHERTZ"] = \
        lambda value: (10 ** 24) * value
    CONVERSIONS["ZETTAHERTZ:NANOHERTZ"] = \
        lambda value: (10 ** 30) * value
    CONVERSIONS["ZETTAHERTZ:PETAHERTZ"] = \
        lambda value: (10 ** 6) * value
    CONVERSIONS["ZETTAHERTZ:PICOHERTZ"] = \
        lambda value: (10 ** 33) * value
    CONVERSIONS["ZETTAHERTZ:TERAHERTZ"] = \
        lambda value: (10 ** 9) * value
    CONVERSIONS["ZETTAHERTZ:YOCTOHERTZ"] = \
        lambda value: (10 ** 45) * value
    CONVERSIONS["ZETTAHERTZ:YOTTAHERTZ"] = \
        lambda value: (10 ** -3) * value
    CONVERSIONS["ZETTAHERTZ:ZEPTOHERTZ"] = \
        lambda value: (10 ** 42) * value

    SYMBOLS = dict()
    SYMBOLS["ATTOHERTZ"] = "aHz"
    SYMBOLS["CENTIHERTZ"] = "cHz"
    SYMBOLS["DECAHERTZ"] = "daHz"
    SYMBOLS["DECIHERTZ"] = "dHz"
    SYMBOLS["EXAHERTZ"] = "EHz"
    SYMBOLS["FEMTOHERTZ"] = "fHz"
    SYMBOLS["GIGAHERTZ"] = "GHz"
    SYMBOLS["HECTOHERTZ"] = "hHz"
    SYMBOLS["HERTZ"] = "Hz"
    SYMBOLS["KILOHERTZ"] = "kHz"
    SYMBOLS["MEGAHERTZ"] = "MHz"
    SYMBOLS["MICROHERTZ"] = "µHz"
    SYMBOLS["MILLIHERTZ"] = "mHz"
    SYMBOLS["NANOHERTZ"] = "nHz"
    SYMBOLS["PETAHERTZ"] = "PHz"
    SYMBOLS["PICOHERTZ"] = "pHz"
    SYMBOLS["TERAHERTZ"] = "THz"
    SYMBOLS["YOCTOHERTZ"] = "yHz"
    SYMBOLS["YOTTAHERTZ"] = "YHz"
    SYMBOLS["ZEPTOHERTZ"] = "zHz"
    SYMBOLS["ZETTAHERTZ"] = "ZHz"

    def __init__(self, value=None, unit=None):
        _omero_model.Frequency.__init__(self)
        if isinstance(value, _omero_model.FrequencyI):
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
                self.setUnit(getattr(UnitsFrequency, str(target)))
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
        return FrequencyI.SYMBOLS.get(str(unit))

    def setUnit(self, unit, current=None):
        self._unit = unit

    def setValue(self, value, current=None):
        self._value = value

    def __str__(self):
        return self._base_string(self.getValue(), self.getUnit())

_omero_model.FrequencyI = FrequencyI
