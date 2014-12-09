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


def noconversion(cfrom, cto):
    raise Exception(("Unsupported conversion: "
                     "%s:%s") % cfrom, cto)


class PowerI(_omero_model.Power, UnitBase):

    CONVERSIONS = dict()
    CONVERSIONS["ATTOWATT:CENTIWATT"] = \
        lambda value: (10 ** -16) * value
    CONVERSIONS["ATTOWATT:DECIWATT"] = \
        lambda value: (10 ** -17) * value
    CONVERSIONS["ATTOWATT:DEKAWATT"] = \
        lambda value: (10 ** -19) * value
    CONVERSIONS["ATTOWATT:EXAWATT"] = \
        lambda value: (10 ** -36) * value
    CONVERSIONS["ATTOWATT:FEMTOWATT"] = \
        lambda value: (10 ** -3) * value
    CONVERSIONS["ATTOWATT:GIGAWATT"] = \
        lambda value: (10 ** -27) * value
    CONVERSIONS["ATTOWATT:HECTOWATT"] = \
        lambda value: (10 ** -20) * value
    CONVERSIONS["ATTOWATT:KILOWATT"] = \
        lambda value: (10 ** -21) * value
    CONVERSIONS["ATTOWATT:MEGAWATT"] = \
        lambda value: (10 ** -24) * value
    CONVERSIONS["ATTOWATT:MICROWATT"] = \
        lambda value: (10 ** -12) * value
    CONVERSIONS["ATTOWATT:MILLIWATT"] = \
        lambda value: (10 ** -15) * value
    CONVERSIONS["ATTOWATT:NANOWATT"] = \
        lambda value: (10 ** -9) * value
    CONVERSIONS["ATTOWATT:PETAWATT"] = \
        lambda value: (10 ** -33) * value
    CONVERSIONS["ATTOWATT:PICOWATT"] = \
        lambda value: (10 ** -6) * value
    CONVERSIONS["ATTOWATT:TERAWATT"] = \
        lambda value: (10 ** -30) * value
    CONVERSIONS["ATTOWATT:WATT"] = \
        lambda value: (10 ** -18) * value
    CONVERSIONS["ATTOWATT:YOCTOWATT"] = \
        lambda value: (10 ** 6) * value
    CONVERSIONS["ATTOWATT:YOTTAWATT"] = \
        lambda value: (10 ** -42) * value
    CONVERSIONS["ATTOWATT:ZEPTOWATT"] = \
        lambda value: (10 ** 3) * value
    CONVERSIONS["ATTOWATT:ZETTAWATT"] = \
        lambda value: (10 ** -39) * value
    CONVERSIONS["CENTIWATT:ATTOWATT"] = \
        lambda value: (10 ** 16) * value
    CONVERSIONS["CENTIWATT:DECIWATT"] = \
        lambda value: (10 ** -1) * value
    CONVERSIONS["CENTIWATT:DEKAWATT"] = \
        lambda value: (10 ** -3) * value
    CONVERSIONS["CENTIWATT:EXAWATT"] = \
        lambda value: (10 ** -20) * value
    CONVERSIONS["CENTIWATT:FEMTOWATT"] = \
        lambda value: (10 ** 13) * value
    CONVERSIONS["CENTIWATT:GIGAWATT"] = \
        lambda value: (10 ** -11) * value
    CONVERSIONS["CENTIWATT:HECTOWATT"] = \
        lambda value: (10 ** -4) * value
    CONVERSIONS["CENTIWATT:KILOWATT"] = \
        lambda value: (10 ** -5) * value
    CONVERSIONS["CENTIWATT:MEGAWATT"] = \
        lambda value: (10 ** -8) * value
    CONVERSIONS["CENTIWATT:MICROWATT"] = \
        lambda value: (10 ** 4) * value
    CONVERSIONS["CENTIWATT:MILLIWATT"] = \
        lambda value: 10 * value
    CONVERSIONS["CENTIWATT:NANOWATT"] = \
        lambda value: (10 ** 7) * value
    CONVERSIONS["CENTIWATT:PETAWATT"] = \
        lambda value: (10 ** -17) * value
    CONVERSIONS["CENTIWATT:PICOWATT"] = \
        lambda value: (10 ** 10) * value
    CONVERSIONS["CENTIWATT:TERAWATT"] = \
        lambda value: (10 ** -14) * value
    CONVERSIONS["CENTIWATT:WATT"] = \
        lambda value: (10 ** -2) * value
    CONVERSIONS["CENTIWATT:YOCTOWATT"] = \
        lambda value: (10 ** 22) * value
    CONVERSIONS["CENTIWATT:YOTTAWATT"] = \
        lambda value: (10 ** -26) * value
    CONVERSIONS["CENTIWATT:ZEPTOWATT"] = \
        lambda value: (10 ** 19) * value
    CONVERSIONS["CENTIWATT:ZETTAWATT"] = \
        lambda value: (10 ** -23) * value
    CONVERSIONS["DECIWATT:ATTOWATT"] = \
        lambda value: (10 ** 17) * value
    CONVERSIONS["DECIWATT:CENTIWATT"] = \
        lambda value: 10 * value
    CONVERSIONS["DECIWATT:DEKAWATT"] = \
        lambda value: (10 ** -2) * value
    CONVERSIONS["DECIWATT:EXAWATT"] = \
        lambda value: (10 ** -19) * value
    CONVERSIONS["DECIWATT:FEMTOWATT"] = \
        lambda value: (10 ** 14) * value
    CONVERSIONS["DECIWATT:GIGAWATT"] = \
        lambda value: (10 ** -10) * value
    CONVERSIONS["DECIWATT:HECTOWATT"] = \
        lambda value: (10 ** -3) * value
    CONVERSIONS["DECIWATT:KILOWATT"] = \
        lambda value: (10 ** -4) * value
    CONVERSIONS["DECIWATT:MEGAWATT"] = \
        lambda value: (10 ** -7) * value
    CONVERSIONS["DECIWATT:MICROWATT"] = \
        lambda value: (10 ** 5) * value
    CONVERSIONS["DECIWATT:MILLIWATT"] = \
        lambda value: (10 ** 2) * value
    CONVERSIONS["DECIWATT:NANOWATT"] = \
        lambda value: (10 ** 8) * value
    CONVERSIONS["DECIWATT:PETAWATT"] = \
        lambda value: (10 ** -16) * value
    CONVERSIONS["DECIWATT:PICOWATT"] = \
        lambda value: (10 ** 11) * value
    CONVERSIONS["DECIWATT:TERAWATT"] = \
        lambda value: (10 ** -13) * value
    CONVERSIONS["DECIWATT:WATT"] = \
        lambda value: (10 ** -1) * value
    CONVERSIONS["DECIWATT:YOCTOWATT"] = \
        lambda value: (10 ** 23) * value
    CONVERSIONS["DECIWATT:YOTTAWATT"] = \
        lambda value: (10 ** -25) * value
    CONVERSIONS["DECIWATT:ZEPTOWATT"] = \
        lambda value: (10 ** 20) * value
    CONVERSIONS["DECIWATT:ZETTAWATT"] = \
        lambda value: (10 ** -22) * value
    CONVERSIONS["DEKAWATT:ATTOWATT"] = \
        lambda value: (10 ** 19) * value
    CONVERSIONS["DEKAWATT:CENTIWATT"] = \
        lambda value: (10 ** 3) * value
    CONVERSIONS["DEKAWATT:DECIWATT"] = \
        lambda value: (10 ** 2) * value
    CONVERSIONS["DEKAWATT:EXAWATT"] = \
        lambda value: (10 ** -17) * value
    CONVERSIONS["DEKAWATT:FEMTOWATT"] = \
        lambda value: (10 ** 16) * value
    CONVERSIONS["DEKAWATT:GIGAWATT"] = \
        lambda value: (10 ** -8) * value
    CONVERSIONS["DEKAWATT:HECTOWATT"] = \
        lambda value: (10 ** -1) * value
    CONVERSIONS["DEKAWATT:KILOWATT"] = \
        lambda value: (10 ** -2) * value
    CONVERSIONS["DEKAWATT:MEGAWATT"] = \
        lambda value: (10 ** -5) * value
    CONVERSIONS["DEKAWATT:MICROWATT"] = \
        lambda value: (10 ** 7) * value
    CONVERSIONS["DEKAWATT:MILLIWATT"] = \
        lambda value: (10 ** 4) * value
    CONVERSIONS["DEKAWATT:NANOWATT"] = \
        lambda value: (10 ** 10) * value
    CONVERSIONS["DEKAWATT:PETAWATT"] = \
        lambda value: (10 ** -14) * value
    CONVERSIONS["DEKAWATT:PICOWATT"] = \
        lambda value: (10 ** 13) * value
    CONVERSIONS["DEKAWATT:TERAWATT"] = \
        lambda value: (10 ** -11) * value
    CONVERSIONS["DEKAWATT:WATT"] = \
        lambda value: 10 * value
    CONVERSIONS["DEKAWATT:YOCTOWATT"] = \
        lambda value: (10 ** 25) * value
    CONVERSIONS["DEKAWATT:YOTTAWATT"] = \
        lambda value: (10 ** -23) * value
    CONVERSIONS["DEKAWATT:ZEPTOWATT"] = \
        lambda value: (10 ** 22) * value
    CONVERSIONS["DEKAWATT:ZETTAWATT"] = \
        lambda value: (10 ** -20) * value
    CONVERSIONS["EXAWATT:ATTOWATT"] = \
        lambda value: (10 ** 36) * value
    CONVERSIONS["EXAWATT:CENTIWATT"] = \
        lambda value: (10 ** 20) * value
    CONVERSIONS["EXAWATT:DECIWATT"] = \
        lambda value: (10 ** 19) * value
    CONVERSIONS["EXAWATT:DEKAWATT"] = \
        lambda value: (10 ** 17) * value
    CONVERSIONS["EXAWATT:FEMTOWATT"] = \
        lambda value: (10 ** 33) * value
    CONVERSIONS["EXAWATT:GIGAWATT"] = \
        lambda value: (10 ** 9) * value
    CONVERSIONS["EXAWATT:HECTOWATT"] = \
        lambda value: (10 ** 16) * value
    CONVERSIONS["EXAWATT:KILOWATT"] = \
        lambda value: (10 ** 15) * value
    CONVERSIONS["EXAWATT:MEGAWATT"] = \
        lambda value: (10 ** 12) * value
    CONVERSIONS["EXAWATT:MICROWATT"] = \
        lambda value: (10 ** 24) * value
    CONVERSIONS["EXAWATT:MILLIWATT"] = \
        lambda value: (10 ** 21) * value
    CONVERSIONS["EXAWATT:NANOWATT"] = \
        lambda value: (10 ** 27) * value
    CONVERSIONS["EXAWATT:PETAWATT"] = \
        lambda value: (10 ** 3) * value
    CONVERSIONS["EXAWATT:PICOWATT"] = \
        lambda value: (10 ** 30) * value
    CONVERSIONS["EXAWATT:TERAWATT"] = \
        lambda value: (10 ** 6) * value
    CONVERSIONS["EXAWATT:WATT"] = \
        lambda value: (10 ** 18) * value
    CONVERSIONS["EXAWATT:YOCTOWATT"] = \
        lambda value: (10 ** 42) * value
    CONVERSIONS["EXAWATT:YOTTAWATT"] = \
        lambda value: (10 ** -6) * value
    CONVERSIONS["EXAWATT:ZEPTOWATT"] = \
        lambda value: (10 ** 39) * value
    CONVERSIONS["EXAWATT:ZETTAWATT"] = \
        lambda value: (10 ** -3) * value
    CONVERSIONS["FEMTOWATT:ATTOWATT"] = \
        lambda value: (10 ** 3) * value
    CONVERSIONS["FEMTOWATT:CENTIWATT"] = \
        lambda value: (10 ** -13) * value
    CONVERSIONS["FEMTOWATT:DECIWATT"] = \
        lambda value: (10 ** -14) * value
    CONVERSIONS["FEMTOWATT:DEKAWATT"] = \
        lambda value: (10 ** -16) * value
    CONVERSIONS["FEMTOWATT:EXAWATT"] = \
        lambda value: (10 ** -33) * value
    CONVERSIONS["FEMTOWATT:GIGAWATT"] = \
        lambda value: (10 ** -24) * value
    CONVERSIONS["FEMTOWATT:HECTOWATT"] = \
        lambda value: (10 ** -17) * value
    CONVERSIONS["FEMTOWATT:KILOWATT"] = \
        lambda value: (10 ** -18) * value
    CONVERSIONS["FEMTOWATT:MEGAWATT"] = \
        lambda value: (10 ** -21) * value
    CONVERSIONS["FEMTOWATT:MICROWATT"] = \
        lambda value: (10 ** -9) * value
    CONVERSIONS["FEMTOWATT:MILLIWATT"] = \
        lambda value: (10 ** -12) * value
    CONVERSIONS["FEMTOWATT:NANOWATT"] = \
        lambda value: (10 ** -6) * value
    CONVERSIONS["FEMTOWATT:PETAWATT"] = \
        lambda value: (10 ** -30) * value
    CONVERSIONS["FEMTOWATT:PICOWATT"] = \
        lambda value: (10 ** -3) * value
    CONVERSIONS["FEMTOWATT:TERAWATT"] = \
        lambda value: (10 ** -27) * value
    CONVERSIONS["FEMTOWATT:WATT"] = \
        lambda value: (10 ** -15) * value
    CONVERSIONS["FEMTOWATT:YOCTOWATT"] = \
        lambda value: (10 ** 9) * value
    CONVERSIONS["FEMTOWATT:YOTTAWATT"] = \
        lambda value: (10 ** -39) * value
    CONVERSIONS["FEMTOWATT:ZEPTOWATT"] = \
        lambda value: (10 ** 6) * value
    CONVERSIONS["FEMTOWATT:ZETTAWATT"] = \
        lambda value: (10 ** -36) * value
    CONVERSIONS["GIGAWATT:ATTOWATT"] = \
        lambda value: (10 ** 27) * value
    CONVERSIONS["GIGAWATT:CENTIWATT"] = \
        lambda value: (10 ** 11) * value
    CONVERSIONS["GIGAWATT:DECIWATT"] = \
        lambda value: (10 ** 10) * value
    CONVERSIONS["GIGAWATT:DEKAWATT"] = \
        lambda value: (10 ** 8) * value
    CONVERSIONS["GIGAWATT:EXAWATT"] = \
        lambda value: (10 ** -9) * value
    CONVERSIONS["GIGAWATT:FEMTOWATT"] = \
        lambda value: (10 ** 24) * value
    CONVERSIONS["GIGAWATT:HECTOWATT"] = \
        lambda value: (10 ** 7) * value
    CONVERSIONS["GIGAWATT:KILOWATT"] = \
        lambda value: (10 ** 6) * value
    CONVERSIONS["GIGAWATT:MEGAWATT"] = \
        lambda value: (10 ** 3) * value
    CONVERSIONS["GIGAWATT:MICROWATT"] = \
        lambda value: (10 ** 15) * value
    CONVERSIONS["GIGAWATT:MILLIWATT"] = \
        lambda value: (10 ** 12) * value
    CONVERSIONS["GIGAWATT:NANOWATT"] = \
        lambda value: (10 ** 18) * value
    CONVERSIONS["GIGAWATT:PETAWATT"] = \
        lambda value: (10 ** -6) * value
    CONVERSIONS["GIGAWATT:PICOWATT"] = \
        lambda value: (10 ** 21) * value
    CONVERSIONS["GIGAWATT:TERAWATT"] = \
        lambda value: (10 ** -3) * value
    CONVERSIONS["GIGAWATT:WATT"] = \
        lambda value: (10 ** 9) * value
    CONVERSIONS["GIGAWATT:YOCTOWATT"] = \
        lambda value: (10 ** 33) * value
    CONVERSIONS["GIGAWATT:YOTTAWATT"] = \
        lambda value: (10 ** -15) * value
    CONVERSIONS["GIGAWATT:ZEPTOWATT"] = \
        lambda value: (10 ** 30) * value
    CONVERSIONS["GIGAWATT:ZETTAWATT"] = \
        lambda value: (10 ** -12) * value
    CONVERSIONS["HECTOWATT:ATTOWATT"] = \
        lambda value: (10 ** 20) * value
    CONVERSIONS["HECTOWATT:CENTIWATT"] = \
        lambda value: (10 ** 4) * value
    CONVERSIONS["HECTOWATT:DECIWATT"] = \
        lambda value: (10 ** 3) * value
    CONVERSIONS["HECTOWATT:DEKAWATT"] = \
        lambda value: 10 * value
    CONVERSIONS["HECTOWATT:EXAWATT"] = \
        lambda value: (10 ** -16) * value
    CONVERSIONS["HECTOWATT:FEMTOWATT"] = \
        lambda value: (10 ** 17) * value
    CONVERSIONS["HECTOWATT:GIGAWATT"] = \
        lambda value: (10 ** -7) * value
    CONVERSIONS["HECTOWATT:KILOWATT"] = \
        lambda value: (10 ** -1) * value
    CONVERSIONS["HECTOWATT:MEGAWATT"] = \
        lambda value: (10 ** -4) * value
    CONVERSIONS["HECTOWATT:MICROWATT"] = \
        lambda value: (10 ** 8) * value
    CONVERSIONS["HECTOWATT:MILLIWATT"] = \
        lambda value: (10 ** 5) * value
    CONVERSIONS["HECTOWATT:NANOWATT"] = \
        lambda value: (10 ** 11) * value
    CONVERSIONS["HECTOWATT:PETAWATT"] = \
        lambda value: (10 ** -13) * value
    CONVERSIONS["HECTOWATT:PICOWATT"] = \
        lambda value: (10 ** 14) * value
    CONVERSIONS["HECTOWATT:TERAWATT"] = \
        lambda value: (10 ** -10) * value
    CONVERSIONS["HECTOWATT:WATT"] = \
        lambda value: (10 ** 2) * value
    CONVERSIONS["HECTOWATT:YOCTOWATT"] = \
        lambda value: (10 ** 26) * value
    CONVERSIONS["HECTOWATT:YOTTAWATT"] = \
        lambda value: (10 ** -22) * value
    CONVERSIONS["HECTOWATT:ZEPTOWATT"] = \
        lambda value: (10 ** 23) * value
    CONVERSIONS["HECTOWATT:ZETTAWATT"] = \
        lambda value: (10 ** -19) * value
    CONVERSIONS["KILOWATT:ATTOWATT"] = \
        lambda value: (10 ** 21) * value
    CONVERSIONS["KILOWATT:CENTIWATT"] = \
        lambda value: (10 ** 5) * value
    CONVERSIONS["KILOWATT:DECIWATT"] = \
        lambda value: (10 ** 4) * value
    CONVERSIONS["KILOWATT:DEKAWATT"] = \
        lambda value: (10 ** 2) * value
    CONVERSIONS["KILOWATT:EXAWATT"] = \
        lambda value: (10 ** -15) * value
    CONVERSIONS["KILOWATT:FEMTOWATT"] = \
        lambda value: (10 ** 18) * value
    CONVERSIONS["KILOWATT:GIGAWATT"] = \
        lambda value: (10 ** -6) * value
    CONVERSIONS["KILOWATT:HECTOWATT"] = \
        lambda value: 10 * value
    CONVERSIONS["KILOWATT:MEGAWATT"] = \
        lambda value: (10 ** -3) * value
    CONVERSIONS["KILOWATT:MICROWATT"] = \
        lambda value: (10 ** 9) * value
    CONVERSIONS["KILOWATT:MILLIWATT"] = \
        lambda value: (10 ** 6) * value
    CONVERSIONS["KILOWATT:NANOWATT"] = \
        lambda value: (10 ** 12) * value
    CONVERSIONS["KILOWATT:PETAWATT"] = \
        lambda value: (10 ** -12) * value
    CONVERSIONS["KILOWATT:PICOWATT"] = \
        lambda value: (10 ** 15) * value
    CONVERSIONS["KILOWATT:TERAWATT"] = \
        lambda value: (10 ** -9) * value
    CONVERSIONS["KILOWATT:WATT"] = \
        lambda value: (10 ** 3) * value
    CONVERSIONS["KILOWATT:YOCTOWATT"] = \
        lambda value: (10 ** 27) * value
    CONVERSIONS["KILOWATT:YOTTAWATT"] = \
        lambda value: (10 ** -21) * value
    CONVERSIONS["KILOWATT:ZEPTOWATT"] = \
        lambda value: (10 ** 24) * value
    CONVERSIONS["KILOWATT:ZETTAWATT"] = \
        lambda value: (10 ** -18) * value
    CONVERSIONS["MEGAWATT:ATTOWATT"] = \
        lambda value: (10 ** 24) * value
    CONVERSIONS["MEGAWATT:CENTIWATT"] = \
        lambda value: (10 ** 8) * value
    CONVERSIONS["MEGAWATT:DECIWATT"] = \
        lambda value: (10 ** 7) * value
    CONVERSIONS["MEGAWATT:DEKAWATT"] = \
        lambda value: (10 ** 5) * value
    CONVERSIONS["MEGAWATT:EXAWATT"] = \
        lambda value: (10 ** -12) * value
    CONVERSIONS["MEGAWATT:FEMTOWATT"] = \
        lambda value: (10 ** 21) * value
    CONVERSIONS["MEGAWATT:GIGAWATT"] = \
        lambda value: (10 ** -3) * value
    CONVERSIONS["MEGAWATT:HECTOWATT"] = \
        lambda value: (10 ** 4) * value
    CONVERSIONS["MEGAWATT:KILOWATT"] = \
        lambda value: (10 ** 3) * value
    CONVERSIONS["MEGAWATT:MICROWATT"] = \
        lambda value: (10 ** 12) * value
    CONVERSIONS["MEGAWATT:MILLIWATT"] = \
        lambda value: (10 ** 9) * value
    CONVERSIONS["MEGAWATT:NANOWATT"] = \
        lambda value: (10 ** 15) * value
    CONVERSIONS["MEGAWATT:PETAWATT"] = \
        lambda value: (10 ** -9) * value
    CONVERSIONS["MEGAWATT:PICOWATT"] = \
        lambda value: (10 ** 18) * value
    CONVERSIONS["MEGAWATT:TERAWATT"] = \
        lambda value: (10 ** -6) * value
    CONVERSIONS["MEGAWATT:WATT"] = \
        lambda value: (10 ** 6) * value
    CONVERSIONS["MEGAWATT:YOCTOWATT"] = \
        lambda value: (10 ** 30) * value
    CONVERSIONS["MEGAWATT:YOTTAWATT"] = \
        lambda value: (10 ** -18) * value
    CONVERSIONS["MEGAWATT:ZEPTOWATT"] = \
        lambda value: (10 ** 27) * value
    CONVERSIONS["MEGAWATT:ZETTAWATT"] = \
        lambda value: (10 ** -15) * value
    CONVERSIONS["MICROWATT:ATTOWATT"] = \
        lambda value: (10 ** 12) * value
    CONVERSIONS["MICROWATT:CENTIWATT"] = \
        lambda value: (10 ** -4) * value
    CONVERSIONS["MICROWATT:DECIWATT"] = \
        lambda value: (10 ** -5) * value
    CONVERSIONS["MICROWATT:DEKAWATT"] = \
        lambda value: (10 ** -7) * value
    CONVERSIONS["MICROWATT:EXAWATT"] = \
        lambda value: (10 ** -24) * value
    CONVERSIONS["MICROWATT:FEMTOWATT"] = \
        lambda value: (10 ** 9) * value
    CONVERSIONS["MICROWATT:GIGAWATT"] = \
        lambda value: (10 ** -15) * value
    CONVERSIONS["MICROWATT:HECTOWATT"] = \
        lambda value: (10 ** -8) * value
    CONVERSIONS["MICROWATT:KILOWATT"] = \
        lambda value: (10 ** -9) * value
    CONVERSIONS["MICROWATT:MEGAWATT"] = \
        lambda value: (10 ** -12) * value
    CONVERSIONS["MICROWATT:MILLIWATT"] = \
        lambda value: (10 ** -3) * value
    CONVERSIONS["MICROWATT:NANOWATT"] = \
        lambda value: (10 ** 3) * value
    CONVERSIONS["MICROWATT:PETAWATT"] = \
        lambda value: (10 ** -21) * value
    CONVERSIONS["MICROWATT:PICOWATT"] = \
        lambda value: (10 ** 6) * value
    CONVERSIONS["MICROWATT:TERAWATT"] = \
        lambda value: (10 ** -18) * value
    CONVERSIONS["MICROWATT:WATT"] = \
        lambda value: (10 ** -6) * value
    CONVERSIONS["MICROWATT:YOCTOWATT"] = \
        lambda value: (10 ** 18) * value
    CONVERSIONS["MICROWATT:YOTTAWATT"] = \
        lambda value: (10 ** -30) * value
    CONVERSIONS["MICROWATT:ZEPTOWATT"] = \
        lambda value: (10 ** 15) * value
    CONVERSIONS["MICROWATT:ZETTAWATT"] = \
        lambda value: (10 ** -27) * value
    CONVERSIONS["MILLIWATT:ATTOWATT"] = \
        lambda value: (10 ** 15) * value
    CONVERSIONS["MILLIWATT:CENTIWATT"] = \
        lambda value: (10 ** -1) * value
    CONVERSIONS["MILLIWATT:DECIWATT"] = \
        lambda value: (10 ** -2) * value
    CONVERSIONS["MILLIWATT:DEKAWATT"] = \
        lambda value: (10 ** -4) * value
    CONVERSIONS["MILLIWATT:EXAWATT"] = \
        lambda value: (10 ** -21) * value
    CONVERSIONS["MILLIWATT:FEMTOWATT"] = \
        lambda value: (10 ** 12) * value
    CONVERSIONS["MILLIWATT:GIGAWATT"] = \
        lambda value: (10 ** -12) * value
    CONVERSIONS["MILLIWATT:HECTOWATT"] = \
        lambda value: (10 ** -5) * value
    CONVERSIONS["MILLIWATT:KILOWATT"] = \
        lambda value: (10 ** -6) * value
    CONVERSIONS["MILLIWATT:MEGAWATT"] = \
        lambda value: (10 ** -9) * value
    CONVERSIONS["MILLIWATT:MICROWATT"] = \
        lambda value: (10 ** 3) * value
    CONVERSIONS["MILLIWATT:NANOWATT"] = \
        lambda value: (10 ** 6) * value
    CONVERSIONS["MILLIWATT:PETAWATT"] = \
        lambda value: (10 ** -18) * value
    CONVERSIONS["MILLIWATT:PICOWATT"] = \
        lambda value: (10 ** 9) * value
    CONVERSIONS["MILLIWATT:TERAWATT"] = \
        lambda value: (10 ** -15) * value
    CONVERSIONS["MILLIWATT:WATT"] = \
        lambda value: (10 ** -3) * value
    CONVERSIONS["MILLIWATT:YOCTOWATT"] = \
        lambda value: (10 ** 21) * value
    CONVERSIONS["MILLIWATT:YOTTAWATT"] = \
        lambda value: (10 ** -27) * value
    CONVERSIONS["MILLIWATT:ZEPTOWATT"] = \
        lambda value: (10 ** 18) * value
    CONVERSIONS["MILLIWATT:ZETTAWATT"] = \
        lambda value: (10 ** -24) * value
    CONVERSIONS["NANOWATT:ATTOWATT"] = \
        lambda value: (10 ** 9) * value
    CONVERSIONS["NANOWATT:CENTIWATT"] = \
        lambda value: (10 ** -7) * value
    CONVERSIONS["NANOWATT:DECIWATT"] = \
        lambda value: (10 ** -8) * value
    CONVERSIONS["NANOWATT:DEKAWATT"] = \
        lambda value: (10 ** -10) * value
    CONVERSIONS["NANOWATT:EXAWATT"] = \
        lambda value: (10 ** -27) * value
    CONVERSIONS["NANOWATT:FEMTOWATT"] = \
        lambda value: (10 ** 6) * value
    CONVERSIONS["NANOWATT:GIGAWATT"] = \
        lambda value: (10 ** -18) * value
    CONVERSIONS["NANOWATT:HECTOWATT"] = \
        lambda value: (10 ** -11) * value
    CONVERSIONS["NANOWATT:KILOWATT"] = \
        lambda value: (10 ** -12) * value
    CONVERSIONS["NANOWATT:MEGAWATT"] = \
        lambda value: (10 ** -15) * value
    CONVERSIONS["NANOWATT:MICROWATT"] = \
        lambda value: (10 ** -3) * value
    CONVERSIONS["NANOWATT:MILLIWATT"] = \
        lambda value: (10 ** -6) * value
    CONVERSIONS["NANOWATT:PETAWATT"] = \
        lambda value: (10 ** -24) * value
    CONVERSIONS["NANOWATT:PICOWATT"] = \
        lambda value: (10 ** 3) * value
    CONVERSIONS["NANOWATT:TERAWATT"] = \
        lambda value: (10 ** -21) * value
    CONVERSIONS["NANOWATT:WATT"] = \
        lambda value: (10 ** -9) * value
    CONVERSIONS["NANOWATT:YOCTOWATT"] = \
        lambda value: (10 ** 15) * value
    CONVERSIONS["NANOWATT:YOTTAWATT"] = \
        lambda value: (10 ** -33) * value
    CONVERSIONS["NANOWATT:ZEPTOWATT"] = \
        lambda value: (10 ** 12) * value
    CONVERSIONS["NANOWATT:ZETTAWATT"] = \
        lambda value: (10 ** -30) * value
    CONVERSIONS["PETAWATT:ATTOWATT"] = \
        lambda value: (10 ** 33) * value
    CONVERSIONS["PETAWATT:CENTIWATT"] = \
        lambda value: (10 ** 17) * value
    CONVERSIONS["PETAWATT:DECIWATT"] = \
        lambda value: (10 ** 16) * value
    CONVERSIONS["PETAWATT:DEKAWATT"] = \
        lambda value: (10 ** 14) * value
    CONVERSIONS["PETAWATT:EXAWATT"] = \
        lambda value: (10 ** -3) * value
    CONVERSIONS["PETAWATT:FEMTOWATT"] = \
        lambda value: (10 ** 30) * value
    CONVERSIONS["PETAWATT:GIGAWATT"] = \
        lambda value: (10 ** 6) * value
    CONVERSIONS["PETAWATT:HECTOWATT"] = \
        lambda value: (10 ** 13) * value
    CONVERSIONS["PETAWATT:KILOWATT"] = \
        lambda value: (10 ** 12) * value
    CONVERSIONS["PETAWATT:MEGAWATT"] = \
        lambda value: (10 ** 9) * value
    CONVERSIONS["PETAWATT:MICROWATT"] = \
        lambda value: (10 ** 21) * value
    CONVERSIONS["PETAWATT:MILLIWATT"] = \
        lambda value: (10 ** 18) * value
    CONVERSIONS["PETAWATT:NANOWATT"] = \
        lambda value: (10 ** 24) * value
    CONVERSIONS["PETAWATT:PICOWATT"] = \
        lambda value: (10 ** 27) * value
    CONVERSIONS["PETAWATT:TERAWATT"] = \
        lambda value: (10 ** 3) * value
    CONVERSIONS["PETAWATT:WATT"] = \
        lambda value: (10 ** 15) * value
    CONVERSIONS["PETAWATT:YOCTOWATT"] = \
        lambda value: (10 ** 39) * value
    CONVERSIONS["PETAWATT:YOTTAWATT"] = \
        lambda value: (10 ** -9) * value
    CONVERSIONS["PETAWATT:ZEPTOWATT"] = \
        lambda value: (10 ** 36) * value
    CONVERSIONS["PETAWATT:ZETTAWATT"] = \
        lambda value: (10 ** -6) * value
    CONVERSIONS["PICOWATT:ATTOWATT"] = \
        lambda value: (10 ** 6) * value
    CONVERSIONS["PICOWATT:CENTIWATT"] = \
        lambda value: (10 ** -10) * value
    CONVERSIONS["PICOWATT:DECIWATT"] = \
        lambda value: (10 ** -11) * value
    CONVERSIONS["PICOWATT:DEKAWATT"] = \
        lambda value: (10 ** -13) * value
    CONVERSIONS["PICOWATT:EXAWATT"] = \
        lambda value: (10 ** -30) * value
    CONVERSIONS["PICOWATT:FEMTOWATT"] = \
        lambda value: (10 ** 3) * value
    CONVERSIONS["PICOWATT:GIGAWATT"] = \
        lambda value: (10 ** -21) * value
    CONVERSIONS["PICOWATT:HECTOWATT"] = \
        lambda value: (10 ** -14) * value
    CONVERSIONS["PICOWATT:KILOWATT"] = \
        lambda value: (10 ** -15) * value
    CONVERSIONS["PICOWATT:MEGAWATT"] = \
        lambda value: (10 ** -18) * value
    CONVERSIONS["PICOWATT:MICROWATT"] = \
        lambda value: (10 ** -6) * value
    CONVERSIONS["PICOWATT:MILLIWATT"] = \
        lambda value: (10 ** -9) * value
    CONVERSIONS["PICOWATT:NANOWATT"] = \
        lambda value: (10 ** -3) * value
    CONVERSIONS["PICOWATT:PETAWATT"] = \
        lambda value: (10 ** -27) * value
    CONVERSIONS["PICOWATT:TERAWATT"] = \
        lambda value: (10 ** -24) * value
    CONVERSIONS["PICOWATT:WATT"] = \
        lambda value: (10 ** -12) * value
    CONVERSIONS["PICOWATT:YOCTOWATT"] = \
        lambda value: (10 ** 12) * value
    CONVERSIONS["PICOWATT:YOTTAWATT"] = \
        lambda value: (10 ** -36) * value
    CONVERSIONS["PICOWATT:ZEPTOWATT"] = \
        lambda value: (10 ** 9) * value
    CONVERSIONS["PICOWATT:ZETTAWATT"] = \
        lambda value: (10 ** -33) * value
    CONVERSIONS["TERAWATT:ATTOWATT"] = \
        lambda value: (10 ** 30) * value
    CONVERSIONS["TERAWATT:CENTIWATT"] = \
        lambda value: (10 ** 14) * value
    CONVERSIONS["TERAWATT:DECIWATT"] = \
        lambda value: (10 ** 13) * value
    CONVERSIONS["TERAWATT:DEKAWATT"] = \
        lambda value: (10 ** 11) * value
    CONVERSIONS["TERAWATT:EXAWATT"] = \
        lambda value: (10 ** -6) * value
    CONVERSIONS["TERAWATT:FEMTOWATT"] = \
        lambda value: (10 ** 27) * value
    CONVERSIONS["TERAWATT:GIGAWATT"] = \
        lambda value: (10 ** 3) * value
    CONVERSIONS["TERAWATT:HECTOWATT"] = \
        lambda value: (10 ** 10) * value
    CONVERSIONS["TERAWATT:KILOWATT"] = \
        lambda value: (10 ** 9) * value
    CONVERSIONS["TERAWATT:MEGAWATT"] = \
        lambda value: (10 ** 6) * value
    CONVERSIONS["TERAWATT:MICROWATT"] = \
        lambda value: (10 ** 18) * value
    CONVERSIONS["TERAWATT:MILLIWATT"] = \
        lambda value: (10 ** 15) * value
    CONVERSIONS["TERAWATT:NANOWATT"] = \
        lambda value: (10 ** 21) * value
    CONVERSIONS["TERAWATT:PETAWATT"] = \
        lambda value: (10 ** -3) * value
    CONVERSIONS["TERAWATT:PICOWATT"] = \
        lambda value: (10 ** 24) * value
    CONVERSIONS["TERAWATT:WATT"] = \
        lambda value: (10 ** 12) * value
    CONVERSIONS["TERAWATT:YOCTOWATT"] = \
        lambda value: (10 ** 36) * value
    CONVERSIONS["TERAWATT:YOTTAWATT"] = \
        lambda value: (10 ** -12) * value
    CONVERSIONS["TERAWATT:ZEPTOWATT"] = \
        lambda value: (10 ** 33) * value
    CONVERSIONS["TERAWATT:ZETTAWATT"] = \
        lambda value: (10 ** -9) * value
    CONVERSIONS["WATT:ATTOWATT"] = \
        lambda value: (10 ** 18) * value
    CONVERSIONS["WATT:CENTIWATT"] = \
        lambda value: (10 ** 2) * value
    CONVERSIONS["WATT:DECIWATT"] = \
        lambda value: 10 * value
    CONVERSIONS["WATT:DEKAWATT"] = \
        lambda value: (10 ** -1) * value
    CONVERSIONS["WATT:EXAWATT"] = \
        lambda value: (10 ** -18) * value
    CONVERSIONS["WATT:FEMTOWATT"] = \
        lambda value: (10 ** 15) * value
    CONVERSIONS["WATT:GIGAWATT"] = \
        lambda value: (10 ** -9) * value
    CONVERSIONS["WATT:HECTOWATT"] = \
        lambda value: (10 ** -2) * value
    CONVERSIONS["WATT:KILOWATT"] = \
        lambda value: (10 ** -3) * value
    CONVERSIONS["WATT:MEGAWATT"] = \
        lambda value: (10 ** -6) * value
    CONVERSIONS["WATT:MICROWATT"] = \
        lambda value: (10 ** 6) * value
    CONVERSIONS["WATT:MILLIWATT"] = \
        lambda value: (10 ** 3) * value
    CONVERSIONS["WATT:NANOWATT"] = \
        lambda value: (10 ** 9) * value
    CONVERSIONS["WATT:PETAWATT"] = \
        lambda value: (10 ** -15) * value
    CONVERSIONS["WATT:PICOWATT"] = \
        lambda value: (10 ** 12) * value
    CONVERSIONS["WATT:TERAWATT"] = \
        lambda value: (10 ** -12) * value
    CONVERSIONS["WATT:YOCTOWATT"] = \
        lambda value: (10 ** 24) * value
    CONVERSIONS["WATT:YOTTAWATT"] = \
        lambda value: (10 ** -24) * value
    CONVERSIONS["WATT:ZEPTOWATT"] = \
        lambda value: (10 ** 21) * value
    CONVERSIONS["WATT:ZETTAWATT"] = \
        lambda value: (10 ** -21) * value
    CONVERSIONS["YOCTOWATT:ATTOWATT"] = \
        lambda value: (10 ** -6) * value
    CONVERSIONS["YOCTOWATT:CENTIWATT"] = \
        lambda value: (10 ** -22) * value
    CONVERSIONS["YOCTOWATT:DECIWATT"] = \
        lambda value: (10 ** -23) * value
    CONVERSIONS["YOCTOWATT:DEKAWATT"] = \
        lambda value: (10 ** -25) * value
    CONVERSIONS["YOCTOWATT:EXAWATT"] = \
        lambda value: (10 ** -42) * value
    CONVERSIONS["YOCTOWATT:FEMTOWATT"] = \
        lambda value: (10 ** -9) * value
    CONVERSIONS["YOCTOWATT:GIGAWATT"] = \
        lambda value: (10 ** -33) * value
    CONVERSIONS["YOCTOWATT:HECTOWATT"] = \
        lambda value: (10 ** -26) * value
    CONVERSIONS["YOCTOWATT:KILOWATT"] = \
        lambda value: (10 ** -27) * value
    CONVERSIONS["YOCTOWATT:MEGAWATT"] = \
        lambda value: (10 ** -30) * value
    CONVERSIONS["YOCTOWATT:MICROWATT"] = \
        lambda value: (10 ** -18) * value
    CONVERSIONS["YOCTOWATT:MILLIWATT"] = \
        lambda value: (10 ** -21) * value
    CONVERSIONS["YOCTOWATT:NANOWATT"] = \
        lambda value: (10 ** -15) * value
    CONVERSIONS["YOCTOWATT:PETAWATT"] = \
        lambda value: (10 ** -39) * value
    CONVERSIONS["YOCTOWATT:PICOWATT"] = \
        lambda value: (10 ** -12) * value
    CONVERSIONS["YOCTOWATT:TERAWATT"] = \
        lambda value: (10 ** -36) * value
    CONVERSIONS["YOCTOWATT:WATT"] = \
        lambda value: (10 ** -24) * value
    CONVERSIONS["YOCTOWATT:YOTTAWATT"] = \
        lambda value: (10 ** -48) * value
    CONVERSIONS["YOCTOWATT:ZEPTOWATT"] = \
        lambda value: (10 ** -3) * value
    CONVERSIONS["YOCTOWATT:ZETTAWATT"] = \
        lambda value: (10 ** -45) * value
    CONVERSIONS["YOTTAWATT:ATTOWATT"] = \
        lambda value: (10 ** 42) * value
    CONVERSIONS["YOTTAWATT:CENTIWATT"] = \
        lambda value: (10 ** 26) * value
    CONVERSIONS["YOTTAWATT:DECIWATT"] = \
        lambda value: (10 ** 25) * value
    CONVERSIONS["YOTTAWATT:DEKAWATT"] = \
        lambda value: (10 ** 23) * value
    CONVERSIONS["YOTTAWATT:EXAWATT"] = \
        lambda value: (10 ** 6) * value
    CONVERSIONS["YOTTAWATT:FEMTOWATT"] = \
        lambda value: (10 ** 39) * value
    CONVERSIONS["YOTTAWATT:GIGAWATT"] = \
        lambda value: (10 ** 15) * value
    CONVERSIONS["YOTTAWATT:HECTOWATT"] = \
        lambda value: (10 ** 22) * value
    CONVERSIONS["YOTTAWATT:KILOWATT"] = \
        lambda value: (10 ** 21) * value
    CONVERSIONS["YOTTAWATT:MEGAWATT"] = \
        lambda value: (10 ** 18) * value
    CONVERSIONS["YOTTAWATT:MICROWATT"] = \
        lambda value: (10 ** 30) * value
    CONVERSIONS["YOTTAWATT:MILLIWATT"] = \
        lambda value: (10 ** 27) * value
    CONVERSIONS["YOTTAWATT:NANOWATT"] = \
        lambda value: (10 ** 33) * value
    CONVERSIONS["YOTTAWATT:PETAWATT"] = \
        lambda value: (10 ** 9) * value
    CONVERSIONS["YOTTAWATT:PICOWATT"] = \
        lambda value: (10 ** 36) * value
    CONVERSIONS["YOTTAWATT:TERAWATT"] = \
        lambda value: (10 ** 12) * value
    CONVERSIONS["YOTTAWATT:WATT"] = \
        lambda value: (10 ** 24) * value
    CONVERSIONS["YOTTAWATT:YOCTOWATT"] = \
        lambda value: (10 ** 48) * value
    CONVERSIONS["YOTTAWATT:ZEPTOWATT"] = \
        lambda value: (10 ** 45) * value
    CONVERSIONS["YOTTAWATT:ZETTAWATT"] = \
        lambda value: (10 ** 3) * value
    CONVERSIONS["ZEPTOWATT:ATTOWATT"] = \
        lambda value: (10 ** -3) * value
    CONVERSIONS["ZEPTOWATT:CENTIWATT"] = \
        lambda value: (10 ** -19) * value
    CONVERSIONS["ZEPTOWATT:DECIWATT"] = \
        lambda value: (10 ** -20) * value
    CONVERSIONS["ZEPTOWATT:DEKAWATT"] = \
        lambda value: (10 ** -22) * value
    CONVERSIONS["ZEPTOWATT:EXAWATT"] = \
        lambda value: (10 ** -39) * value
    CONVERSIONS["ZEPTOWATT:FEMTOWATT"] = \
        lambda value: (10 ** -6) * value
    CONVERSIONS["ZEPTOWATT:GIGAWATT"] = \
        lambda value: (10 ** -30) * value
    CONVERSIONS["ZEPTOWATT:HECTOWATT"] = \
        lambda value: (10 ** -23) * value
    CONVERSIONS["ZEPTOWATT:KILOWATT"] = \
        lambda value: (10 ** -24) * value
    CONVERSIONS["ZEPTOWATT:MEGAWATT"] = \
        lambda value: (10 ** -27) * value
    CONVERSIONS["ZEPTOWATT:MICROWATT"] = \
        lambda value: (10 ** -15) * value
    CONVERSIONS["ZEPTOWATT:MILLIWATT"] = \
        lambda value: (10 ** -18) * value
    CONVERSIONS["ZEPTOWATT:NANOWATT"] = \
        lambda value: (10 ** -12) * value
    CONVERSIONS["ZEPTOWATT:PETAWATT"] = \
        lambda value: (10 ** -36) * value
    CONVERSIONS["ZEPTOWATT:PICOWATT"] = \
        lambda value: (10 ** -9) * value
    CONVERSIONS["ZEPTOWATT:TERAWATT"] = \
        lambda value: (10 ** -33) * value
    CONVERSIONS["ZEPTOWATT:WATT"] = \
        lambda value: (10 ** -21) * value
    CONVERSIONS["ZEPTOWATT:YOCTOWATT"] = \
        lambda value: (10 ** 3) * value
    CONVERSIONS["ZEPTOWATT:YOTTAWATT"] = \
        lambda value: (10 ** -45) * value
    CONVERSIONS["ZEPTOWATT:ZETTAWATT"] = \
        lambda value: (10 ** -42) * value
    CONVERSIONS["ZETTAWATT:ATTOWATT"] = \
        lambda value: (10 ** 39) * value
    CONVERSIONS["ZETTAWATT:CENTIWATT"] = \
        lambda value: (10 ** 23) * value
    CONVERSIONS["ZETTAWATT:DECIWATT"] = \
        lambda value: (10 ** 22) * value
    CONVERSIONS["ZETTAWATT:DEKAWATT"] = \
        lambda value: (10 ** 20) * value
    CONVERSIONS["ZETTAWATT:EXAWATT"] = \
        lambda value: (10 ** 3) * value
    CONVERSIONS["ZETTAWATT:FEMTOWATT"] = \
        lambda value: (10 ** 36) * value
    CONVERSIONS["ZETTAWATT:GIGAWATT"] = \
        lambda value: (10 ** 12) * value
    CONVERSIONS["ZETTAWATT:HECTOWATT"] = \
        lambda value: (10 ** 19) * value
    CONVERSIONS["ZETTAWATT:KILOWATT"] = \
        lambda value: (10 ** 18) * value
    CONVERSIONS["ZETTAWATT:MEGAWATT"] = \
        lambda value: (10 ** 15) * value
    CONVERSIONS["ZETTAWATT:MICROWATT"] = \
        lambda value: (10 ** 27) * value
    CONVERSIONS["ZETTAWATT:MILLIWATT"] = \
        lambda value: (10 ** 24) * value
    CONVERSIONS["ZETTAWATT:NANOWATT"] = \
        lambda value: (10 ** 30) * value
    CONVERSIONS["ZETTAWATT:PETAWATT"] = \
        lambda value: (10 ** 6) * value
    CONVERSIONS["ZETTAWATT:PICOWATT"] = \
        lambda value: (10 ** 33) * value
    CONVERSIONS["ZETTAWATT:TERAWATT"] = \
        lambda value: (10 ** 9) * value
    CONVERSIONS["ZETTAWATT:WATT"] = \
        lambda value: (10 ** 21) * value
    CONVERSIONS["ZETTAWATT:YOCTOWATT"] = \
        lambda value: (10 ** 45) * value
    CONVERSIONS["ZETTAWATT:YOTTAWATT"] = \
        lambda value: (10 ** -3) * value
    CONVERSIONS["ZETTAWATT:ZEPTOWATT"] = \
        lambda value: (10 ** 42) * value

    SYMBOLS = dict()
    SYMBOLS["ATTOWATT"] = "aW"
    SYMBOLS["CENTIWATT"] = "cW"
    SYMBOLS["DECIWATT"] = "dW"
    SYMBOLS["DEKAWATT"] = "daW"
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
