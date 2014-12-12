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
Code-generated omero.model.ElectricPotential implementation,
based on omero.model.PermissionsI
"""


import Ice
import IceImport
IceImport.load("omero_model_ElectricPotential_ice")
_omero = Ice.openModule("omero")
_omero_model = Ice.openModule("omero.model")
__name__ = "omero.model"

from omero_model_UnitBase import UnitBase
from omero.model.enums import UnitsElectricPotential


def noconversion(cfrom, cto):
    raise Exception(("Unsupported conversion: "
                     "%s:%s") % cfrom, cto)


class ElectricPotentialI(_omero_model.ElectricPotential, UnitBase):

    CONVERSIONS = dict()
    CONVERSIONS["ATTOVOLT:CENTIVOLT"] = \
        lambda value: (10 ** -16) * value
    CONVERSIONS["ATTOVOLT:DECAVOLT"] = \
        lambda value: (10 ** -19) * value
    CONVERSIONS["ATTOVOLT:DECIVOLT"] = \
        lambda value: (10 ** -17) * value
    CONVERSIONS["ATTOVOLT:EXAVOLT"] = \
        lambda value: (10 ** -36) * value
    CONVERSIONS["ATTOVOLT:FEMTOVOLT"] = \
        lambda value: (10 ** -3) * value
    CONVERSIONS["ATTOVOLT:GIGAVOLT"] = \
        lambda value: (10 ** -27) * value
    CONVERSIONS["ATTOVOLT:HECTOVOLT"] = \
        lambda value: (10 ** -20) * value
    CONVERSIONS["ATTOVOLT:KILOVOLT"] = \
        lambda value: (10 ** -21) * value
    CONVERSIONS["ATTOVOLT:MEGAVOLT"] = \
        lambda value: (10 ** -24) * value
    CONVERSIONS["ATTOVOLT:MICROVOLT"] = \
        lambda value: (10 ** -12) * value
    CONVERSIONS["ATTOVOLT:MILLIVOLT"] = \
        lambda value: (10 ** -15) * value
    CONVERSIONS["ATTOVOLT:NANOVOLT"] = \
        lambda value: (10 ** -9) * value
    CONVERSIONS["ATTOVOLT:PETAVOLT"] = \
        lambda value: (10 ** -33) * value
    CONVERSIONS["ATTOVOLT:PICOVOLT"] = \
        lambda value: (10 ** -6) * value
    CONVERSIONS["ATTOVOLT:TERAVOLT"] = \
        lambda value: (10 ** -30) * value
    CONVERSIONS["ATTOVOLT:VOLT"] = \
        lambda value: (10 ** -18) * value
    CONVERSIONS["ATTOVOLT:YOCTOVOLT"] = \
        lambda value: (10 ** 6) * value
    CONVERSIONS["ATTOVOLT:YOTTAVOLT"] = \
        lambda value: (10 ** -42) * value
    CONVERSIONS["ATTOVOLT:ZEPTOVOLT"] = \
        lambda value: (10 ** 3) * value
    CONVERSIONS["ATTOVOLT:ZETTAVOLT"] = \
        lambda value: (10 ** -39) * value
    CONVERSIONS["CENTIVOLT:ATTOVOLT"] = \
        lambda value: (10 ** 16) * value
    CONVERSIONS["CENTIVOLT:DECAVOLT"] = \
        lambda value: (10 ** -3) * value
    CONVERSIONS["CENTIVOLT:DECIVOLT"] = \
        lambda value: (10 ** -1) * value
    CONVERSIONS["CENTIVOLT:EXAVOLT"] = \
        lambda value: (10 ** -20) * value
    CONVERSIONS["CENTIVOLT:FEMTOVOLT"] = \
        lambda value: (10 ** 13) * value
    CONVERSIONS["CENTIVOLT:GIGAVOLT"] = \
        lambda value: (10 ** -11) * value
    CONVERSIONS["CENTIVOLT:HECTOVOLT"] = \
        lambda value: (10 ** -4) * value
    CONVERSIONS["CENTIVOLT:KILOVOLT"] = \
        lambda value: (10 ** -5) * value
    CONVERSIONS["CENTIVOLT:MEGAVOLT"] = \
        lambda value: (10 ** -8) * value
    CONVERSIONS["CENTIVOLT:MICROVOLT"] = \
        lambda value: (10 ** 4) * value
    CONVERSIONS["CENTIVOLT:MILLIVOLT"] = \
        lambda value: 10 * value
    CONVERSIONS["CENTIVOLT:NANOVOLT"] = \
        lambda value: (10 ** 7) * value
    CONVERSIONS["CENTIVOLT:PETAVOLT"] = \
        lambda value: (10 ** -17) * value
    CONVERSIONS["CENTIVOLT:PICOVOLT"] = \
        lambda value: (10 ** 10) * value
    CONVERSIONS["CENTIVOLT:TERAVOLT"] = \
        lambda value: (10 ** -14) * value
    CONVERSIONS["CENTIVOLT:VOLT"] = \
        lambda value: (10 ** -2) * value
    CONVERSIONS["CENTIVOLT:YOCTOVOLT"] = \
        lambda value: (10 ** 22) * value
    CONVERSIONS["CENTIVOLT:YOTTAVOLT"] = \
        lambda value: (10 ** -26) * value
    CONVERSIONS["CENTIVOLT:ZEPTOVOLT"] = \
        lambda value: (10 ** 19) * value
    CONVERSIONS["CENTIVOLT:ZETTAVOLT"] = \
        lambda value: (10 ** -23) * value
    CONVERSIONS["DECAVOLT:ATTOVOLT"] = \
        lambda value: (10 ** 19) * value
    CONVERSIONS["DECAVOLT:CENTIVOLT"] = \
        lambda value: (10 ** 3) * value
    CONVERSIONS["DECAVOLT:DECIVOLT"] = \
        lambda value: (10 ** 2) * value
    CONVERSIONS["DECAVOLT:EXAVOLT"] = \
        lambda value: (10 ** -17) * value
    CONVERSIONS["DECAVOLT:FEMTOVOLT"] = \
        lambda value: (10 ** 16) * value
    CONVERSIONS["DECAVOLT:GIGAVOLT"] = \
        lambda value: (10 ** -8) * value
    CONVERSIONS["DECAVOLT:HECTOVOLT"] = \
        lambda value: (10 ** -1) * value
    CONVERSIONS["DECAVOLT:KILOVOLT"] = \
        lambda value: (10 ** -2) * value
    CONVERSIONS["DECAVOLT:MEGAVOLT"] = \
        lambda value: (10 ** -5) * value
    CONVERSIONS["DECAVOLT:MICROVOLT"] = \
        lambda value: (10 ** 7) * value
    CONVERSIONS["DECAVOLT:MILLIVOLT"] = \
        lambda value: (10 ** 4) * value
    CONVERSIONS["DECAVOLT:NANOVOLT"] = \
        lambda value: (10 ** 10) * value
    CONVERSIONS["DECAVOLT:PETAVOLT"] = \
        lambda value: (10 ** -14) * value
    CONVERSIONS["DECAVOLT:PICOVOLT"] = \
        lambda value: (10 ** 13) * value
    CONVERSIONS["DECAVOLT:TERAVOLT"] = \
        lambda value: (10 ** -11) * value
    CONVERSIONS["DECAVOLT:VOLT"] = \
        lambda value: 10 * value
    CONVERSIONS["DECAVOLT:YOCTOVOLT"] = \
        lambda value: (10 ** 25) * value
    CONVERSIONS["DECAVOLT:YOTTAVOLT"] = \
        lambda value: (10 ** -23) * value
    CONVERSIONS["DECAVOLT:ZEPTOVOLT"] = \
        lambda value: (10 ** 22) * value
    CONVERSIONS["DECAVOLT:ZETTAVOLT"] = \
        lambda value: (10 ** -20) * value
    CONVERSIONS["DECIVOLT:ATTOVOLT"] = \
        lambda value: (10 ** 17) * value
    CONVERSIONS["DECIVOLT:CENTIVOLT"] = \
        lambda value: 10 * value
    CONVERSIONS["DECIVOLT:DECAVOLT"] = \
        lambda value: (10 ** -2) * value
    CONVERSIONS["DECIVOLT:EXAVOLT"] = \
        lambda value: (10 ** -19) * value
    CONVERSIONS["DECIVOLT:FEMTOVOLT"] = \
        lambda value: (10 ** 14) * value
    CONVERSIONS["DECIVOLT:GIGAVOLT"] = \
        lambda value: (10 ** -10) * value
    CONVERSIONS["DECIVOLT:HECTOVOLT"] = \
        lambda value: (10 ** -3) * value
    CONVERSIONS["DECIVOLT:KILOVOLT"] = \
        lambda value: (10 ** -4) * value
    CONVERSIONS["DECIVOLT:MEGAVOLT"] = \
        lambda value: (10 ** -7) * value
    CONVERSIONS["DECIVOLT:MICROVOLT"] = \
        lambda value: (10 ** 5) * value
    CONVERSIONS["DECIVOLT:MILLIVOLT"] = \
        lambda value: (10 ** 2) * value
    CONVERSIONS["DECIVOLT:NANOVOLT"] = \
        lambda value: (10 ** 8) * value
    CONVERSIONS["DECIVOLT:PETAVOLT"] = \
        lambda value: (10 ** -16) * value
    CONVERSIONS["DECIVOLT:PICOVOLT"] = \
        lambda value: (10 ** 11) * value
    CONVERSIONS["DECIVOLT:TERAVOLT"] = \
        lambda value: (10 ** -13) * value
    CONVERSIONS["DECIVOLT:VOLT"] = \
        lambda value: (10 ** -1) * value
    CONVERSIONS["DECIVOLT:YOCTOVOLT"] = \
        lambda value: (10 ** 23) * value
    CONVERSIONS["DECIVOLT:YOTTAVOLT"] = \
        lambda value: (10 ** -25) * value
    CONVERSIONS["DECIVOLT:ZEPTOVOLT"] = \
        lambda value: (10 ** 20) * value
    CONVERSIONS["DECIVOLT:ZETTAVOLT"] = \
        lambda value: (10 ** -22) * value
    CONVERSIONS["EXAVOLT:ATTOVOLT"] = \
        lambda value: (10 ** 36) * value
    CONVERSIONS["EXAVOLT:CENTIVOLT"] = \
        lambda value: (10 ** 20) * value
    CONVERSIONS["EXAVOLT:DECAVOLT"] = \
        lambda value: (10 ** 17) * value
    CONVERSIONS["EXAVOLT:DECIVOLT"] = \
        lambda value: (10 ** 19) * value
    CONVERSIONS["EXAVOLT:FEMTOVOLT"] = \
        lambda value: (10 ** 33) * value
    CONVERSIONS["EXAVOLT:GIGAVOLT"] = \
        lambda value: (10 ** 9) * value
    CONVERSIONS["EXAVOLT:HECTOVOLT"] = \
        lambda value: (10 ** 16) * value
    CONVERSIONS["EXAVOLT:KILOVOLT"] = \
        lambda value: (10 ** 15) * value
    CONVERSIONS["EXAVOLT:MEGAVOLT"] = \
        lambda value: (10 ** 12) * value
    CONVERSIONS["EXAVOLT:MICROVOLT"] = \
        lambda value: (10 ** 24) * value
    CONVERSIONS["EXAVOLT:MILLIVOLT"] = \
        lambda value: (10 ** 21) * value
    CONVERSIONS["EXAVOLT:NANOVOLT"] = \
        lambda value: (10 ** 27) * value
    CONVERSIONS["EXAVOLT:PETAVOLT"] = \
        lambda value: (10 ** 3) * value
    CONVERSIONS["EXAVOLT:PICOVOLT"] = \
        lambda value: (10 ** 30) * value
    CONVERSIONS["EXAVOLT:TERAVOLT"] = \
        lambda value: (10 ** 6) * value
    CONVERSIONS["EXAVOLT:VOLT"] = \
        lambda value: (10 ** 18) * value
    CONVERSIONS["EXAVOLT:YOCTOVOLT"] = \
        lambda value: (10 ** 42) * value
    CONVERSIONS["EXAVOLT:YOTTAVOLT"] = \
        lambda value: (10 ** -6) * value
    CONVERSIONS["EXAVOLT:ZEPTOVOLT"] = \
        lambda value: (10 ** 39) * value
    CONVERSIONS["EXAVOLT:ZETTAVOLT"] = \
        lambda value: (10 ** -3) * value
    CONVERSIONS["FEMTOVOLT:ATTOVOLT"] = \
        lambda value: (10 ** 3) * value
    CONVERSIONS["FEMTOVOLT:CENTIVOLT"] = \
        lambda value: (10 ** -13) * value
    CONVERSIONS["FEMTOVOLT:DECAVOLT"] = \
        lambda value: (10 ** -16) * value
    CONVERSIONS["FEMTOVOLT:DECIVOLT"] = \
        lambda value: (10 ** -14) * value
    CONVERSIONS["FEMTOVOLT:EXAVOLT"] = \
        lambda value: (10 ** -33) * value
    CONVERSIONS["FEMTOVOLT:GIGAVOLT"] = \
        lambda value: (10 ** -24) * value
    CONVERSIONS["FEMTOVOLT:HECTOVOLT"] = \
        lambda value: (10 ** -17) * value
    CONVERSIONS["FEMTOVOLT:KILOVOLT"] = \
        lambda value: (10 ** -18) * value
    CONVERSIONS["FEMTOVOLT:MEGAVOLT"] = \
        lambda value: (10 ** -21) * value
    CONVERSIONS["FEMTOVOLT:MICROVOLT"] = \
        lambda value: (10 ** -9) * value
    CONVERSIONS["FEMTOVOLT:MILLIVOLT"] = \
        lambda value: (10 ** -12) * value
    CONVERSIONS["FEMTOVOLT:NANOVOLT"] = \
        lambda value: (10 ** -6) * value
    CONVERSIONS["FEMTOVOLT:PETAVOLT"] = \
        lambda value: (10 ** -30) * value
    CONVERSIONS["FEMTOVOLT:PICOVOLT"] = \
        lambda value: (10 ** -3) * value
    CONVERSIONS["FEMTOVOLT:TERAVOLT"] = \
        lambda value: (10 ** -27) * value
    CONVERSIONS["FEMTOVOLT:VOLT"] = \
        lambda value: (10 ** -15) * value
    CONVERSIONS["FEMTOVOLT:YOCTOVOLT"] = \
        lambda value: (10 ** 9) * value
    CONVERSIONS["FEMTOVOLT:YOTTAVOLT"] = \
        lambda value: (10 ** -39) * value
    CONVERSIONS["FEMTOVOLT:ZEPTOVOLT"] = \
        lambda value: (10 ** 6) * value
    CONVERSIONS["FEMTOVOLT:ZETTAVOLT"] = \
        lambda value: (10 ** -36) * value
    CONVERSIONS["GIGAVOLT:ATTOVOLT"] = \
        lambda value: (10 ** 27) * value
    CONVERSIONS["GIGAVOLT:CENTIVOLT"] = \
        lambda value: (10 ** 11) * value
    CONVERSIONS["GIGAVOLT:DECAVOLT"] = \
        lambda value: (10 ** 8) * value
    CONVERSIONS["GIGAVOLT:DECIVOLT"] = \
        lambda value: (10 ** 10) * value
    CONVERSIONS["GIGAVOLT:EXAVOLT"] = \
        lambda value: (10 ** -9) * value
    CONVERSIONS["GIGAVOLT:FEMTOVOLT"] = \
        lambda value: (10 ** 24) * value
    CONVERSIONS["GIGAVOLT:HECTOVOLT"] = \
        lambda value: (10 ** 7) * value
    CONVERSIONS["GIGAVOLT:KILOVOLT"] = \
        lambda value: (10 ** 6) * value
    CONVERSIONS["GIGAVOLT:MEGAVOLT"] = \
        lambda value: (10 ** 3) * value
    CONVERSIONS["GIGAVOLT:MICROVOLT"] = \
        lambda value: (10 ** 15) * value
    CONVERSIONS["GIGAVOLT:MILLIVOLT"] = \
        lambda value: (10 ** 12) * value
    CONVERSIONS["GIGAVOLT:NANOVOLT"] = \
        lambda value: (10 ** 18) * value
    CONVERSIONS["GIGAVOLT:PETAVOLT"] = \
        lambda value: (10 ** -6) * value
    CONVERSIONS["GIGAVOLT:PICOVOLT"] = \
        lambda value: (10 ** 21) * value
    CONVERSIONS["GIGAVOLT:TERAVOLT"] = \
        lambda value: (10 ** -3) * value
    CONVERSIONS["GIGAVOLT:VOLT"] = \
        lambda value: (10 ** 9) * value
    CONVERSIONS["GIGAVOLT:YOCTOVOLT"] = \
        lambda value: (10 ** 33) * value
    CONVERSIONS["GIGAVOLT:YOTTAVOLT"] = \
        lambda value: (10 ** -15) * value
    CONVERSIONS["GIGAVOLT:ZEPTOVOLT"] = \
        lambda value: (10 ** 30) * value
    CONVERSIONS["GIGAVOLT:ZETTAVOLT"] = \
        lambda value: (10 ** -12) * value
    CONVERSIONS["HECTOVOLT:ATTOVOLT"] = \
        lambda value: (10 ** 20) * value
    CONVERSIONS["HECTOVOLT:CENTIVOLT"] = \
        lambda value: (10 ** 4) * value
    CONVERSIONS["HECTOVOLT:DECAVOLT"] = \
        lambda value: 10 * value
    CONVERSIONS["HECTOVOLT:DECIVOLT"] = \
        lambda value: (10 ** 3) * value
    CONVERSIONS["HECTOVOLT:EXAVOLT"] = \
        lambda value: (10 ** -16) * value
    CONVERSIONS["HECTOVOLT:FEMTOVOLT"] = \
        lambda value: (10 ** 17) * value
    CONVERSIONS["HECTOVOLT:GIGAVOLT"] = \
        lambda value: (10 ** -7) * value
    CONVERSIONS["HECTOVOLT:KILOVOLT"] = \
        lambda value: (10 ** -1) * value
    CONVERSIONS["HECTOVOLT:MEGAVOLT"] = \
        lambda value: (10 ** -4) * value
    CONVERSIONS["HECTOVOLT:MICROVOLT"] = \
        lambda value: (10 ** 8) * value
    CONVERSIONS["HECTOVOLT:MILLIVOLT"] = \
        lambda value: (10 ** 5) * value
    CONVERSIONS["HECTOVOLT:NANOVOLT"] = \
        lambda value: (10 ** 11) * value
    CONVERSIONS["HECTOVOLT:PETAVOLT"] = \
        lambda value: (10 ** -13) * value
    CONVERSIONS["HECTOVOLT:PICOVOLT"] = \
        lambda value: (10 ** 14) * value
    CONVERSIONS["HECTOVOLT:TERAVOLT"] = \
        lambda value: (10 ** -10) * value
    CONVERSIONS["HECTOVOLT:VOLT"] = \
        lambda value: (10 ** 2) * value
    CONVERSIONS["HECTOVOLT:YOCTOVOLT"] = \
        lambda value: (10 ** 26) * value
    CONVERSIONS["HECTOVOLT:YOTTAVOLT"] = \
        lambda value: (10 ** -22) * value
    CONVERSIONS["HECTOVOLT:ZEPTOVOLT"] = \
        lambda value: (10 ** 23) * value
    CONVERSIONS["HECTOVOLT:ZETTAVOLT"] = \
        lambda value: (10 ** -19) * value
    CONVERSIONS["KILOVOLT:ATTOVOLT"] = \
        lambda value: (10 ** 21) * value
    CONVERSIONS["KILOVOLT:CENTIVOLT"] = \
        lambda value: (10 ** 5) * value
    CONVERSIONS["KILOVOLT:DECAVOLT"] = \
        lambda value: (10 ** 2) * value
    CONVERSIONS["KILOVOLT:DECIVOLT"] = \
        lambda value: (10 ** 4) * value
    CONVERSIONS["KILOVOLT:EXAVOLT"] = \
        lambda value: (10 ** -15) * value
    CONVERSIONS["KILOVOLT:FEMTOVOLT"] = \
        lambda value: (10 ** 18) * value
    CONVERSIONS["KILOVOLT:GIGAVOLT"] = \
        lambda value: (10 ** -6) * value
    CONVERSIONS["KILOVOLT:HECTOVOLT"] = \
        lambda value: 10 * value
    CONVERSIONS["KILOVOLT:MEGAVOLT"] = \
        lambda value: (10 ** -3) * value
    CONVERSIONS["KILOVOLT:MICROVOLT"] = \
        lambda value: (10 ** 9) * value
    CONVERSIONS["KILOVOLT:MILLIVOLT"] = \
        lambda value: (10 ** 6) * value
    CONVERSIONS["KILOVOLT:NANOVOLT"] = \
        lambda value: (10 ** 12) * value
    CONVERSIONS["KILOVOLT:PETAVOLT"] = \
        lambda value: (10 ** -12) * value
    CONVERSIONS["KILOVOLT:PICOVOLT"] = \
        lambda value: (10 ** 15) * value
    CONVERSIONS["KILOVOLT:TERAVOLT"] = \
        lambda value: (10 ** -9) * value
    CONVERSIONS["KILOVOLT:VOLT"] = \
        lambda value: (10 ** 3) * value
    CONVERSIONS["KILOVOLT:YOCTOVOLT"] = \
        lambda value: (10 ** 27) * value
    CONVERSIONS["KILOVOLT:YOTTAVOLT"] = \
        lambda value: (10 ** -21) * value
    CONVERSIONS["KILOVOLT:ZEPTOVOLT"] = \
        lambda value: (10 ** 24) * value
    CONVERSIONS["KILOVOLT:ZETTAVOLT"] = \
        lambda value: (10 ** -18) * value
    CONVERSIONS["MEGAVOLT:ATTOVOLT"] = \
        lambda value: (10 ** 24) * value
    CONVERSIONS["MEGAVOLT:CENTIVOLT"] = \
        lambda value: (10 ** 8) * value
    CONVERSIONS["MEGAVOLT:DECAVOLT"] = \
        lambda value: (10 ** 5) * value
    CONVERSIONS["MEGAVOLT:DECIVOLT"] = \
        lambda value: (10 ** 7) * value
    CONVERSIONS["MEGAVOLT:EXAVOLT"] = \
        lambda value: (10 ** -12) * value
    CONVERSIONS["MEGAVOLT:FEMTOVOLT"] = \
        lambda value: (10 ** 21) * value
    CONVERSIONS["MEGAVOLT:GIGAVOLT"] = \
        lambda value: (10 ** -3) * value
    CONVERSIONS["MEGAVOLT:HECTOVOLT"] = \
        lambda value: (10 ** 4) * value
    CONVERSIONS["MEGAVOLT:KILOVOLT"] = \
        lambda value: (10 ** 3) * value
    CONVERSIONS["MEGAVOLT:MICROVOLT"] = \
        lambda value: (10 ** 12) * value
    CONVERSIONS["MEGAVOLT:MILLIVOLT"] = \
        lambda value: (10 ** 9) * value
    CONVERSIONS["MEGAVOLT:NANOVOLT"] = \
        lambda value: (10 ** 15) * value
    CONVERSIONS["MEGAVOLT:PETAVOLT"] = \
        lambda value: (10 ** -9) * value
    CONVERSIONS["MEGAVOLT:PICOVOLT"] = \
        lambda value: (10 ** 18) * value
    CONVERSIONS["MEGAVOLT:TERAVOLT"] = \
        lambda value: (10 ** -6) * value
    CONVERSIONS["MEGAVOLT:VOLT"] = \
        lambda value: (10 ** 6) * value
    CONVERSIONS["MEGAVOLT:YOCTOVOLT"] = \
        lambda value: (10 ** 30) * value
    CONVERSIONS["MEGAVOLT:YOTTAVOLT"] = \
        lambda value: (10 ** -18) * value
    CONVERSIONS["MEGAVOLT:ZEPTOVOLT"] = \
        lambda value: (10 ** 27) * value
    CONVERSIONS["MEGAVOLT:ZETTAVOLT"] = \
        lambda value: (10 ** -15) * value
    CONVERSIONS["MICROVOLT:ATTOVOLT"] = \
        lambda value: (10 ** 12) * value
    CONVERSIONS["MICROVOLT:CENTIVOLT"] = \
        lambda value: (10 ** -4) * value
    CONVERSIONS["MICROVOLT:DECAVOLT"] = \
        lambda value: (10 ** -7) * value
    CONVERSIONS["MICROVOLT:DECIVOLT"] = \
        lambda value: (10 ** -5) * value
    CONVERSIONS["MICROVOLT:EXAVOLT"] = \
        lambda value: (10 ** -24) * value
    CONVERSIONS["MICROVOLT:FEMTOVOLT"] = \
        lambda value: (10 ** 9) * value
    CONVERSIONS["MICROVOLT:GIGAVOLT"] = \
        lambda value: (10 ** -15) * value
    CONVERSIONS["MICROVOLT:HECTOVOLT"] = \
        lambda value: (10 ** -8) * value
    CONVERSIONS["MICROVOLT:KILOVOLT"] = \
        lambda value: (10 ** -9) * value
    CONVERSIONS["MICROVOLT:MEGAVOLT"] = \
        lambda value: (10 ** -12) * value
    CONVERSIONS["MICROVOLT:MILLIVOLT"] = \
        lambda value: (10 ** -3) * value
    CONVERSIONS["MICROVOLT:NANOVOLT"] = \
        lambda value: (10 ** 3) * value
    CONVERSIONS["MICROVOLT:PETAVOLT"] = \
        lambda value: (10 ** -21) * value
    CONVERSIONS["MICROVOLT:PICOVOLT"] = \
        lambda value: (10 ** 6) * value
    CONVERSIONS["MICROVOLT:TERAVOLT"] = \
        lambda value: (10 ** -18) * value
    CONVERSIONS["MICROVOLT:VOLT"] = \
        lambda value: (10 ** -6) * value
    CONVERSIONS["MICROVOLT:YOCTOVOLT"] = \
        lambda value: (10 ** 18) * value
    CONVERSIONS["MICROVOLT:YOTTAVOLT"] = \
        lambda value: (10 ** -30) * value
    CONVERSIONS["MICROVOLT:ZEPTOVOLT"] = \
        lambda value: (10 ** 15) * value
    CONVERSIONS["MICROVOLT:ZETTAVOLT"] = \
        lambda value: (10 ** -27) * value
    CONVERSIONS["MILLIVOLT:ATTOVOLT"] = \
        lambda value: (10 ** 15) * value
    CONVERSIONS["MILLIVOLT:CENTIVOLT"] = \
        lambda value: (10 ** -1) * value
    CONVERSIONS["MILLIVOLT:DECAVOLT"] = \
        lambda value: (10 ** -4) * value
    CONVERSIONS["MILLIVOLT:DECIVOLT"] = \
        lambda value: (10 ** -2) * value
    CONVERSIONS["MILLIVOLT:EXAVOLT"] = \
        lambda value: (10 ** -21) * value
    CONVERSIONS["MILLIVOLT:FEMTOVOLT"] = \
        lambda value: (10 ** 12) * value
    CONVERSIONS["MILLIVOLT:GIGAVOLT"] = \
        lambda value: (10 ** -12) * value
    CONVERSIONS["MILLIVOLT:HECTOVOLT"] = \
        lambda value: (10 ** -5) * value
    CONVERSIONS["MILLIVOLT:KILOVOLT"] = \
        lambda value: (10 ** -6) * value
    CONVERSIONS["MILLIVOLT:MEGAVOLT"] = \
        lambda value: (10 ** -9) * value
    CONVERSIONS["MILLIVOLT:MICROVOLT"] = \
        lambda value: (10 ** 3) * value
    CONVERSIONS["MILLIVOLT:NANOVOLT"] = \
        lambda value: (10 ** 6) * value
    CONVERSIONS["MILLIVOLT:PETAVOLT"] = \
        lambda value: (10 ** -18) * value
    CONVERSIONS["MILLIVOLT:PICOVOLT"] = \
        lambda value: (10 ** 9) * value
    CONVERSIONS["MILLIVOLT:TERAVOLT"] = \
        lambda value: (10 ** -15) * value
    CONVERSIONS["MILLIVOLT:VOLT"] = \
        lambda value: (10 ** -3) * value
    CONVERSIONS["MILLIVOLT:YOCTOVOLT"] = \
        lambda value: (10 ** 21) * value
    CONVERSIONS["MILLIVOLT:YOTTAVOLT"] = \
        lambda value: (10 ** -27) * value
    CONVERSIONS["MILLIVOLT:ZEPTOVOLT"] = \
        lambda value: (10 ** 18) * value
    CONVERSIONS["MILLIVOLT:ZETTAVOLT"] = \
        lambda value: (10 ** -24) * value
    CONVERSIONS["NANOVOLT:ATTOVOLT"] = \
        lambda value: (10 ** 9) * value
    CONVERSIONS["NANOVOLT:CENTIVOLT"] = \
        lambda value: (10 ** -7) * value
    CONVERSIONS["NANOVOLT:DECAVOLT"] = \
        lambda value: (10 ** -10) * value
    CONVERSIONS["NANOVOLT:DECIVOLT"] = \
        lambda value: (10 ** -8) * value
    CONVERSIONS["NANOVOLT:EXAVOLT"] = \
        lambda value: (10 ** -27) * value
    CONVERSIONS["NANOVOLT:FEMTOVOLT"] = \
        lambda value: (10 ** 6) * value
    CONVERSIONS["NANOVOLT:GIGAVOLT"] = \
        lambda value: (10 ** -18) * value
    CONVERSIONS["NANOVOLT:HECTOVOLT"] = \
        lambda value: (10 ** -11) * value
    CONVERSIONS["NANOVOLT:KILOVOLT"] = \
        lambda value: (10 ** -12) * value
    CONVERSIONS["NANOVOLT:MEGAVOLT"] = \
        lambda value: (10 ** -15) * value
    CONVERSIONS["NANOVOLT:MICROVOLT"] = \
        lambda value: (10 ** -3) * value
    CONVERSIONS["NANOVOLT:MILLIVOLT"] = \
        lambda value: (10 ** -6) * value
    CONVERSIONS["NANOVOLT:PETAVOLT"] = \
        lambda value: (10 ** -24) * value
    CONVERSIONS["NANOVOLT:PICOVOLT"] = \
        lambda value: (10 ** 3) * value
    CONVERSIONS["NANOVOLT:TERAVOLT"] = \
        lambda value: (10 ** -21) * value
    CONVERSIONS["NANOVOLT:VOLT"] = \
        lambda value: (10 ** -9) * value
    CONVERSIONS["NANOVOLT:YOCTOVOLT"] = \
        lambda value: (10 ** 15) * value
    CONVERSIONS["NANOVOLT:YOTTAVOLT"] = \
        lambda value: (10 ** -33) * value
    CONVERSIONS["NANOVOLT:ZEPTOVOLT"] = \
        lambda value: (10 ** 12) * value
    CONVERSIONS["NANOVOLT:ZETTAVOLT"] = \
        lambda value: (10 ** -30) * value
    CONVERSIONS["PETAVOLT:ATTOVOLT"] = \
        lambda value: (10 ** 33) * value
    CONVERSIONS["PETAVOLT:CENTIVOLT"] = \
        lambda value: (10 ** 17) * value
    CONVERSIONS["PETAVOLT:DECAVOLT"] = \
        lambda value: (10 ** 14) * value
    CONVERSIONS["PETAVOLT:DECIVOLT"] = \
        lambda value: (10 ** 16) * value
    CONVERSIONS["PETAVOLT:EXAVOLT"] = \
        lambda value: (10 ** -3) * value
    CONVERSIONS["PETAVOLT:FEMTOVOLT"] = \
        lambda value: (10 ** 30) * value
    CONVERSIONS["PETAVOLT:GIGAVOLT"] = \
        lambda value: (10 ** 6) * value
    CONVERSIONS["PETAVOLT:HECTOVOLT"] = \
        lambda value: (10 ** 13) * value
    CONVERSIONS["PETAVOLT:KILOVOLT"] = \
        lambda value: (10 ** 12) * value
    CONVERSIONS["PETAVOLT:MEGAVOLT"] = \
        lambda value: (10 ** 9) * value
    CONVERSIONS["PETAVOLT:MICROVOLT"] = \
        lambda value: (10 ** 21) * value
    CONVERSIONS["PETAVOLT:MILLIVOLT"] = \
        lambda value: (10 ** 18) * value
    CONVERSIONS["PETAVOLT:NANOVOLT"] = \
        lambda value: (10 ** 24) * value
    CONVERSIONS["PETAVOLT:PICOVOLT"] = \
        lambda value: (10 ** 27) * value
    CONVERSIONS["PETAVOLT:TERAVOLT"] = \
        lambda value: (10 ** 3) * value
    CONVERSIONS["PETAVOLT:VOLT"] = \
        lambda value: (10 ** 15) * value
    CONVERSIONS["PETAVOLT:YOCTOVOLT"] = \
        lambda value: (10 ** 39) * value
    CONVERSIONS["PETAVOLT:YOTTAVOLT"] = \
        lambda value: (10 ** -9) * value
    CONVERSIONS["PETAVOLT:ZEPTOVOLT"] = \
        lambda value: (10 ** 36) * value
    CONVERSIONS["PETAVOLT:ZETTAVOLT"] = \
        lambda value: (10 ** -6) * value
    CONVERSIONS["PICOVOLT:ATTOVOLT"] = \
        lambda value: (10 ** 6) * value
    CONVERSIONS["PICOVOLT:CENTIVOLT"] = \
        lambda value: (10 ** -10) * value
    CONVERSIONS["PICOVOLT:DECAVOLT"] = \
        lambda value: (10 ** -13) * value
    CONVERSIONS["PICOVOLT:DECIVOLT"] = \
        lambda value: (10 ** -11) * value
    CONVERSIONS["PICOVOLT:EXAVOLT"] = \
        lambda value: (10 ** -30) * value
    CONVERSIONS["PICOVOLT:FEMTOVOLT"] = \
        lambda value: (10 ** 3) * value
    CONVERSIONS["PICOVOLT:GIGAVOLT"] = \
        lambda value: (10 ** -21) * value
    CONVERSIONS["PICOVOLT:HECTOVOLT"] = \
        lambda value: (10 ** -14) * value
    CONVERSIONS["PICOVOLT:KILOVOLT"] = \
        lambda value: (10 ** -15) * value
    CONVERSIONS["PICOVOLT:MEGAVOLT"] = \
        lambda value: (10 ** -18) * value
    CONVERSIONS["PICOVOLT:MICROVOLT"] = \
        lambda value: (10 ** -6) * value
    CONVERSIONS["PICOVOLT:MILLIVOLT"] = \
        lambda value: (10 ** -9) * value
    CONVERSIONS["PICOVOLT:NANOVOLT"] = \
        lambda value: (10 ** -3) * value
    CONVERSIONS["PICOVOLT:PETAVOLT"] = \
        lambda value: (10 ** -27) * value
    CONVERSIONS["PICOVOLT:TERAVOLT"] = \
        lambda value: (10 ** -24) * value
    CONVERSIONS["PICOVOLT:VOLT"] = \
        lambda value: (10 ** -12) * value
    CONVERSIONS["PICOVOLT:YOCTOVOLT"] = \
        lambda value: (10 ** 12) * value
    CONVERSIONS["PICOVOLT:YOTTAVOLT"] = \
        lambda value: (10 ** -36) * value
    CONVERSIONS["PICOVOLT:ZEPTOVOLT"] = \
        lambda value: (10 ** 9) * value
    CONVERSIONS["PICOVOLT:ZETTAVOLT"] = \
        lambda value: (10 ** -33) * value
    CONVERSIONS["TERAVOLT:ATTOVOLT"] = \
        lambda value: (10 ** 30) * value
    CONVERSIONS["TERAVOLT:CENTIVOLT"] = \
        lambda value: (10 ** 14) * value
    CONVERSIONS["TERAVOLT:DECAVOLT"] = \
        lambda value: (10 ** 11) * value
    CONVERSIONS["TERAVOLT:DECIVOLT"] = \
        lambda value: (10 ** 13) * value
    CONVERSIONS["TERAVOLT:EXAVOLT"] = \
        lambda value: (10 ** -6) * value
    CONVERSIONS["TERAVOLT:FEMTOVOLT"] = \
        lambda value: (10 ** 27) * value
    CONVERSIONS["TERAVOLT:GIGAVOLT"] = \
        lambda value: (10 ** 3) * value
    CONVERSIONS["TERAVOLT:HECTOVOLT"] = \
        lambda value: (10 ** 10) * value
    CONVERSIONS["TERAVOLT:KILOVOLT"] = \
        lambda value: (10 ** 9) * value
    CONVERSIONS["TERAVOLT:MEGAVOLT"] = \
        lambda value: (10 ** 6) * value
    CONVERSIONS["TERAVOLT:MICROVOLT"] = \
        lambda value: (10 ** 18) * value
    CONVERSIONS["TERAVOLT:MILLIVOLT"] = \
        lambda value: (10 ** 15) * value
    CONVERSIONS["TERAVOLT:NANOVOLT"] = \
        lambda value: (10 ** 21) * value
    CONVERSIONS["TERAVOLT:PETAVOLT"] = \
        lambda value: (10 ** -3) * value
    CONVERSIONS["TERAVOLT:PICOVOLT"] = \
        lambda value: (10 ** 24) * value
    CONVERSIONS["TERAVOLT:VOLT"] = \
        lambda value: (10 ** 12) * value
    CONVERSIONS["TERAVOLT:YOCTOVOLT"] = \
        lambda value: (10 ** 36) * value
    CONVERSIONS["TERAVOLT:YOTTAVOLT"] = \
        lambda value: (10 ** -12) * value
    CONVERSIONS["TERAVOLT:ZEPTOVOLT"] = \
        lambda value: (10 ** 33) * value
    CONVERSIONS["TERAVOLT:ZETTAVOLT"] = \
        lambda value: (10 ** -9) * value
    CONVERSIONS["VOLT:ATTOVOLT"] = \
        lambda value: (10 ** 18) * value
    CONVERSIONS["VOLT:CENTIVOLT"] = \
        lambda value: (10 ** 2) * value
    CONVERSIONS["VOLT:DECAVOLT"] = \
        lambda value: (10 ** -1) * value
    CONVERSIONS["VOLT:DECIVOLT"] = \
        lambda value: 10 * value
    CONVERSIONS["VOLT:EXAVOLT"] = \
        lambda value: (10 ** -18) * value
    CONVERSIONS["VOLT:FEMTOVOLT"] = \
        lambda value: (10 ** 15) * value
    CONVERSIONS["VOLT:GIGAVOLT"] = \
        lambda value: (10 ** -9) * value
    CONVERSIONS["VOLT:HECTOVOLT"] = \
        lambda value: (10 ** -2) * value
    CONVERSIONS["VOLT:KILOVOLT"] = \
        lambda value: (10 ** -3) * value
    CONVERSIONS["VOLT:MEGAVOLT"] = \
        lambda value: (10 ** -6) * value
    CONVERSIONS["VOLT:MICROVOLT"] = \
        lambda value: (10 ** 6) * value
    CONVERSIONS["VOLT:MILLIVOLT"] = \
        lambda value: (10 ** 3) * value
    CONVERSIONS["VOLT:NANOVOLT"] = \
        lambda value: (10 ** 9) * value
    CONVERSIONS["VOLT:PETAVOLT"] = \
        lambda value: (10 ** -15) * value
    CONVERSIONS["VOLT:PICOVOLT"] = \
        lambda value: (10 ** 12) * value
    CONVERSIONS["VOLT:TERAVOLT"] = \
        lambda value: (10 ** -12) * value
    CONVERSIONS["VOLT:YOCTOVOLT"] = \
        lambda value: (10 ** 24) * value
    CONVERSIONS["VOLT:YOTTAVOLT"] = \
        lambda value: (10 ** -24) * value
    CONVERSIONS["VOLT:ZEPTOVOLT"] = \
        lambda value: (10 ** 21) * value
    CONVERSIONS["VOLT:ZETTAVOLT"] = \
        lambda value: (10 ** -21) * value
    CONVERSIONS["YOCTOVOLT:ATTOVOLT"] = \
        lambda value: (10 ** -6) * value
    CONVERSIONS["YOCTOVOLT:CENTIVOLT"] = \
        lambda value: (10 ** -22) * value
    CONVERSIONS["YOCTOVOLT:DECAVOLT"] = \
        lambda value: (10 ** -25) * value
    CONVERSIONS["YOCTOVOLT:DECIVOLT"] = \
        lambda value: (10 ** -23) * value
    CONVERSIONS["YOCTOVOLT:EXAVOLT"] = \
        lambda value: (10 ** -42) * value
    CONVERSIONS["YOCTOVOLT:FEMTOVOLT"] = \
        lambda value: (10 ** -9) * value
    CONVERSIONS["YOCTOVOLT:GIGAVOLT"] = \
        lambda value: (10 ** -33) * value
    CONVERSIONS["YOCTOVOLT:HECTOVOLT"] = \
        lambda value: (10 ** -26) * value
    CONVERSIONS["YOCTOVOLT:KILOVOLT"] = \
        lambda value: (10 ** -27) * value
    CONVERSIONS["YOCTOVOLT:MEGAVOLT"] = \
        lambda value: (10 ** -30) * value
    CONVERSIONS["YOCTOVOLT:MICROVOLT"] = \
        lambda value: (10 ** -18) * value
    CONVERSIONS["YOCTOVOLT:MILLIVOLT"] = \
        lambda value: (10 ** -21) * value
    CONVERSIONS["YOCTOVOLT:NANOVOLT"] = \
        lambda value: (10 ** -15) * value
    CONVERSIONS["YOCTOVOLT:PETAVOLT"] = \
        lambda value: (10 ** -39) * value
    CONVERSIONS["YOCTOVOLT:PICOVOLT"] = \
        lambda value: (10 ** -12) * value
    CONVERSIONS["YOCTOVOLT:TERAVOLT"] = \
        lambda value: (10 ** -36) * value
    CONVERSIONS["YOCTOVOLT:VOLT"] = \
        lambda value: (10 ** -24) * value
    CONVERSIONS["YOCTOVOLT:YOTTAVOLT"] = \
        lambda value: (10 ** -48) * value
    CONVERSIONS["YOCTOVOLT:ZEPTOVOLT"] = \
        lambda value: (10 ** -3) * value
    CONVERSIONS["YOCTOVOLT:ZETTAVOLT"] = \
        lambda value: (10 ** -45) * value
    CONVERSIONS["YOTTAVOLT:ATTOVOLT"] = \
        lambda value: (10 ** 42) * value
    CONVERSIONS["YOTTAVOLT:CENTIVOLT"] = \
        lambda value: (10 ** 26) * value
    CONVERSIONS["YOTTAVOLT:DECAVOLT"] = \
        lambda value: (10 ** 23) * value
    CONVERSIONS["YOTTAVOLT:DECIVOLT"] = \
        lambda value: (10 ** 25) * value
    CONVERSIONS["YOTTAVOLT:EXAVOLT"] = \
        lambda value: (10 ** 6) * value
    CONVERSIONS["YOTTAVOLT:FEMTOVOLT"] = \
        lambda value: (10 ** 39) * value
    CONVERSIONS["YOTTAVOLT:GIGAVOLT"] = \
        lambda value: (10 ** 15) * value
    CONVERSIONS["YOTTAVOLT:HECTOVOLT"] = \
        lambda value: (10 ** 22) * value
    CONVERSIONS["YOTTAVOLT:KILOVOLT"] = \
        lambda value: (10 ** 21) * value
    CONVERSIONS["YOTTAVOLT:MEGAVOLT"] = \
        lambda value: (10 ** 18) * value
    CONVERSIONS["YOTTAVOLT:MICROVOLT"] = \
        lambda value: (10 ** 30) * value
    CONVERSIONS["YOTTAVOLT:MILLIVOLT"] = \
        lambda value: (10 ** 27) * value
    CONVERSIONS["YOTTAVOLT:NANOVOLT"] = \
        lambda value: (10 ** 33) * value
    CONVERSIONS["YOTTAVOLT:PETAVOLT"] = \
        lambda value: (10 ** 9) * value
    CONVERSIONS["YOTTAVOLT:PICOVOLT"] = \
        lambda value: (10 ** 36) * value
    CONVERSIONS["YOTTAVOLT:TERAVOLT"] = \
        lambda value: (10 ** 12) * value
    CONVERSIONS["YOTTAVOLT:VOLT"] = \
        lambda value: (10 ** 24) * value
    CONVERSIONS["YOTTAVOLT:YOCTOVOLT"] = \
        lambda value: (10 ** 48) * value
    CONVERSIONS["YOTTAVOLT:ZEPTOVOLT"] = \
        lambda value: (10 ** 45) * value
    CONVERSIONS["YOTTAVOLT:ZETTAVOLT"] = \
        lambda value: (10 ** 3) * value
    CONVERSIONS["ZEPTOVOLT:ATTOVOLT"] = \
        lambda value: (10 ** -3) * value
    CONVERSIONS["ZEPTOVOLT:CENTIVOLT"] = \
        lambda value: (10 ** -19) * value
    CONVERSIONS["ZEPTOVOLT:DECAVOLT"] = \
        lambda value: (10 ** -22) * value
    CONVERSIONS["ZEPTOVOLT:DECIVOLT"] = \
        lambda value: (10 ** -20) * value
    CONVERSIONS["ZEPTOVOLT:EXAVOLT"] = \
        lambda value: (10 ** -39) * value
    CONVERSIONS["ZEPTOVOLT:FEMTOVOLT"] = \
        lambda value: (10 ** -6) * value
    CONVERSIONS["ZEPTOVOLT:GIGAVOLT"] = \
        lambda value: (10 ** -30) * value
    CONVERSIONS["ZEPTOVOLT:HECTOVOLT"] = \
        lambda value: (10 ** -23) * value
    CONVERSIONS["ZEPTOVOLT:KILOVOLT"] = \
        lambda value: (10 ** -24) * value
    CONVERSIONS["ZEPTOVOLT:MEGAVOLT"] = \
        lambda value: (10 ** -27) * value
    CONVERSIONS["ZEPTOVOLT:MICROVOLT"] = \
        lambda value: (10 ** -15) * value
    CONVERSIONS["ZEPTOVOLT:MILLIVOLT"] = \
        lambda value: (10 ** -18) * value
    CONVERSIONS["ZEPTOVOLT:NANOVOLT"] = \
        lambda value: (10 ** -12) * value
    CONVERSIONS["ZEPTOVOLT:PETAVOLT"] = \
        lambda value: (10 ** -36) * value
    CONVERSIONS["ZEPTOVOLT:PICOVOLT"] = \
        lambda value: (10 ** -9) * value
    CONVERSIONS["ZEPTOVOLT:TERAVOLT"] = \
        lambda value: (10 ** -33) * value
    CONVERSIONS["ZEPTOVOLT:VOLT"] = \
        lambda value: (10 ** -21) * value
    CONVERSIONS["ZEPTOVOLT:YOCTOVOLT"] = \
        lambda value: (10 ** 3) * value
    CONVERSIONS["ZEPTOVOLT:YOTTAVOLT"] = \
        lambda value: (10 ** -45) * value
    CONVERSIONS["ZEPTOVOLT:ZETTAVOLT"] = \
        lambda value: (10 ** -42) * value
    CONVERSIONS["ZETTAVOLT:ATTOVOLT"] = \
        lambda value: (10 ** 39) * value
    CONVERSIONS["ZETTAVOLT:CENTIVOLT"] = \
        lambda value: (10 ** 23) * value
    CONVERSIONS["ZETTAVOLT:DECAVOLT"] = \
        lambda value: (10 ** 20) * value
    CONVERSIONS["ZETTAVOLT:DECIVOLT"] = \
        lambda value: (10 ** 22) * value
    CONVERSIONS["ZETTAVOLT:EXAVOLT"] = \
        lambda value: (10 ** 3) * value
    CONVERSIONS["ZETTAVOLT:FEMTOVOLT"] = \
        lambda value: (10 ** 36) * value
    CONVERSIONS["ZETTAVOLT:GIGAVOLT"] = \
        lambda value: (10 ** 12) * value
    CONVERSIONS["ZETTAVOLT:HECTOVOLT"] = \
        lambda value: (10 ** 19) * value
    CONVERSIONS["ZETTAVOLT:KILOVOLT"] = \
        lambda value: (10 ** 18) * value
    CONVERSIONS["ZETTAVOLT:MEGAVOLT"] = \
        lambda value: (10 ** 15) * value
    CONVERSIONS["ZETTAVOLT:MICROVOLT"] = \
        lambda value: (10 ** 27) * value
    CONVERSIONS["ZETTAVOLT:MILLIVOLT"] = \
        lambda value: (10 ** 24) * value
    CONVERSIONS["ZETTAVOLT:NANOVOLT"] = \
        lambda value: (10 ** 30) * value
    CONVERSIONS["ZETTAVOLT:PETAVOLT"] = \
        lambda value: (10 ** 6) * value
    CONVERSIONS["ZETTAVOLT:PICOVOLT"] = \
        lambda value: (10 ** 33) * value
    CONVERSIONS["ZETTAVOLT:TERAVOLT"] = \
        lambda value: (10 ** 9) * value
    CONVERSIONS["ZETTAVOLT:VOLT"] = \
        lambda value: (10 ** 21) * value
    CONVERSIONS["ZETTAVOLT:YOCTOVOLT"] = \
        lambda value: (10 ** 45) * value
    CONVERSIONS["ZETTAVOLT:YOTTAVOLT"] = \
        lambda value: (10 ** -3) * value
    CONVERSIONS["ZETTAVOLT:ZEPTOVOLT"] = \
        lambda value: (10 ** 42) * value

    SYMBOLS = dict()
    SYMBOLS["ATTOVOLT"] = "aV"
    SYMBOLS["CENTIVOLT"] = "cV"
    SYMBOLS["DECAVOLT"] = "daV"
    SYMBOLS["DECIVOLT"] = "dV"
    SYMBOLS["EXAVOLT"] = "EV"
    SYMBOLS["FEMTOVOLT"] = "fV"
    SYMBOLS["GIGAVOLT"] = "GV"
    SYMBOLS["HECTOVOLT"] = "hV"
    SYMBOLS["KILOVOLT"] = "kV"
    SYMBOLS["MEGAVOLT"] = "MV"
    SYMBOLS["MICROVOLT"] = "µV"
    SYMBOLS["MILLIVOLT"] = "mV"
    SYMBOLS["NANOVOLT"] = "nV"
    SYMBOLS["PETAVOLT"] = "PV"
    SYMBOLS["PICOVOLT"] = "pV"
    SYMBOLS["TERAVOLT"] = "TV"
    SYMBOLS["VOLT"] = "V"
    SYMBOLS["YOCTOVOLT"] = "yV"
    SYMBOLS["YOTTAVOLT"] = "YV"
    SYMBOLS["ZEPTOVOLT"] = "zV"
    SYMBOLS["ZETTAVOLT"] = "ZV"

    def __init__(self, value=None, unit=None):
        _omero_model.ElectricPotential.__init__(self)
        if isinstance(value, _omero_model.ElectricPotentialI):
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
                self.setUnit(getattr(UnitsElectricPotential, str(target)))
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
        return ElectricPotentialI.SYMBOLS.get(str(unit))

    def setUnit(self, unit, current=None):
        self._unit = unit

    def setValue(self, value, current=None):
        self._value = value

    def __str__(self):
        return self._base_string(self.getValue(), self.getUnit())

_omero_model.ElectricPotentialI = ElectricPotentialI
