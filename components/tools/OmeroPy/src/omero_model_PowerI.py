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
    CONVERSIONS["AW:CW"] = \
        lambda value: (10 ** -16) * value
    CONVERSIONS["AW:DAW"] = \
        lambda value: (10 ** -19) * value
    CONVERSIONS["AW:DW"] = \
        lambda value: (10 ** -17) * value
    CONVERSIONS["AW:EXAW"] = \
        lambda value: (10 ** -36) * value
    CONVERSIONS["AW:FW"] = \
        lambda value: (10 ** -3) * value
    CONVERSIONS["AW:GIGAW"] = \
        lambda value: (10 ** -27) * value
    CONVERSIONS["AW:HW"] = \
        lambda value: (10 ** -20) * value
    CONVERSIONS["AW:KW"] = \
        lambda value: (10 ** -21) * value
    CONVERSIONS["AW:MEGAW"] = \
        lambda value: (10 ** -24) * value
    CONVERSIONS["AW:MICROW"] = \
        lambda value: (10 ** -12) * value
    CONVERSIONS["AW:MW"] = \
        lambda value: (10 ** -15) * value
    CONVERSIONS["AW:NW"] = \
        lambda value: (10 ** -9) * value
    CONVERSIONS["AW:PETAW"] = \
        lambda value: (10 ** -33) * value
    CONVERSIONS["AW:PW"] = \
        lambda value: (10 ** -6) * value
    CONVERSIONS["AW:TERAW"] = \
        lambda value: (10 ** -30) * value
    CONVERSIONS["AW:W"] = \
        lambda value: (10 ** -18) * value
    CONVERSIONS["AW:YOTTAW"] = \
        lambda value: (10 ** -42) * value
    CONVERSIONS["AW:YW"] = \
        lambda value: (10 ** 6) * value
    CONVERSIONS["AW:ZETTAW"] = \
        lambda value: (10 ** -39) * value
    CONVERSIONS["AW:ZW"] = \
        lambda value: (10 ** 3) * value
    CONVERSIONS["CW:AW"] = \
        lambda value: (10 ** 16) * value
    CONVERSIONS["CW:DAW"] = \
        lambda value: (10 ** -3) * value
    CONVERSIONS["CW:DW"] = \
        lambda value: (10 ** -1) * value
    CONVERSIONS["CW:EXAW"] = \
        lambda value: (10 ** -20) * value
    CONVERSIONS["CW:FW"] = \
        lambda value: (10 ** 13) * value
    CONVERSIONS["CW:GIGAW"] = \
        lambda value: (10 ** -11) * value
    CONVERSIONS["CW:HW"] = \
        lambda value: (10 ** -4) * value
    CONVERSIONS["CW:KW"] = \
        lambda value: (10 ** -5) * value
    CONVERSIONS["CW:MEGAW"] = \
        lambda value: (10 ** -8) * value
    CONVERSIONS["CW:MICROW"] = \
        lambda value: (10 ** 4) * value
    CONVERSIONS["CW:MW"] = \
        lambda value: 10 * value
    CONVERSIONS["CW:NW"] = \
        lambda value: (10 ** 7) * value
    CONVERSIONS["CW:PETAW"] = \
        lambda value: (10 ** -17) * value
    CONVERSIONS["CW:PW"] = \
        lambda value: (10 ** 10) * value
    CONVERSIONS["CW:TERAW"] = \
        lambda value: (10 ** -14) * value
    CONVERSIONS["CW:W"] = \
        lambda value: (10 ** -2) * value
    CONVERSIONS["CW:YOTTAW"] = \
        lambda value: (10 ** -26) * value
    CONVERSIONS["CW:YW"] = \
        lambda value: (10 ** 22) * value
    CONVERSIONS["CW:ZETTAW"] = \
        lambda value: (10 ** -23) * value
    CONVERSIONS["CW:ZW"] = \
        lambda value: (10 ** 19) * value
    CONVERSIONS["DAW:AW"] = \
        lambda value: (10 ** 19) * value
    CONVERSIONS["DAW:CW"] = \
        lambda value: (10 ** 3) * value
    CONVERSIONS["DAW:DW"] = \
        lambda value: (10 ** 2) * value
    CONVERSIONS["DAW:EXAW"] = \
        lambda value: (10 ** -17) * value
    CONVERSIONS["DAW:FW"] = \
        lambda value: (10 ** 16) * value
    CONVERSIONS["DAW:GIGAW"] = \
        lambda value: (10 ** -8) * value
    CONVERSIONS["DAW:HW"] = \
        lambda value: (10 ** -1) * value
    CONVERSIONS["DAW:KW"] = \
        lambda value: (10 ** -2) * value
    CONVERSIONS["DAW:MEGAW"] = \
        lambda value: (10 ** -5) * value
    CONVERSIONS["DAW:MICROW"] = \
        lambda value: (10 ** 7) * value
    CONVERSIONS["DAW:MW"] = \
        lambda value: (10 ** 4) * value
    CONVERSIONS["DAW:NW"] = \
        lambda value: (10 ** 10) * value
    CONVERSIONS["DAW:PETAW"] = \
        lambda value: (10 ** -14) * value
    CONVERSIONS["DAW:PW"] = \
        lambda value: (10 ** 13) * value
    CONVERSIONS["DAW:TERAW"] = \
        lambda value: (10 ** -11) * value
    CONVERSIONS["DAW:W"] = \
        lambda value: 10 * value
    CONVERSIONS["DAW:YOTTAW"] = \
        lambda value: (10 ** -23) * value
    CONVERSIONS["DAW:YW"] = \
        lambda value: (10 ** 25) * value
    CONVERSIONS["DAW:ZETTAW"] = \
        lambda value: (10 ** -20) * value
    CONVERSIONS["DAW:ZW"] = \
        lambda value: (10 ** 22) * value
    CONVERSIONS["DW:AW"] = \
        lambda value: (10 ** 17) * value
    CONVERSIONS["DW:CW"] = \
        lambda value: 10 * value
    CONVERSIONS["DW:DAW"] = \
        lambda value: (10 ** -2) * value
    CONVERSIONS["DW:EXAW"] = \
        lambda value: (10 ** -19) * value
    CONVERSIONS["DW:FW"] = \
        lambda value: (10 ** 14) * value
    CONVERSIONS["DW:GIGAW"] = \
        lambda value: (10 ** -10) * value
    CONVERSIONS["DW:HW"] = \
        lambda value: (10 ** -3) * value
    CONVERSIONS["DW:KW"] = \
        lambda value: (10 ** -4) * value
    CONVERSIONS["DW:MEGAW"] = \
        lambda value: (10 ** -7) * value
    CONVERSIONS["DW:MICROW"] = \
        lambda value: (10 ** 5) * value
    CONVERSIONS["DW:MW"] = \
        lambda value: (10 ** 2) * value
    CONVERSIONS["DW:NW"] = \
        lambda value: (10 ** 8) * value
    CONVERSIONS["DW:PETAW"] = \
        lambda value: (10 ** -16) * value
    CONVERSIONS["DW:PW"] = \
        lambda value: (10 ** 11) * value
    CONVERSIONS["DW:TERAW"] = \
        lambda value: (10 ** -13) * value
    CONVERSIONS["DW:W"] = \
        lambda value: (10 ** -1) * value
    CONVERSIONS["DW:YOTTAW"] = \
        lambda value: (10 ** -25) * value
    CONVERSIONS["DW:YW"] = \
        lambda value: (10 ** 23) * value
    CONVERSIONS["DW:ZETTAW"] = \
        lambda value: (10 ** -22) * value
    CONVERSIONS["DW:ZW"] = \
        lambda value: (10 ** 20) * value
    CONVERSIONS["EXAW:AW"] = \
        lambda value: (10 ** 36) * value
    CONVERSIONS["EXAW:CW"] = \
        lambda value: (10 ** 20) * value
    CONVERSIONS["EXAW:DAW"] = \
        lambda value: (10 ** 17) * value
    CONVERSIONS["EXAW:DW"] = \
        lambda value: (10 ** 19) * value
    CONVERSIONS["EXAW:FW"] = \
        lambda value: (10 ** 33) * value
    CONVERSIONS["EXAW:GIGAW"] = \
        lambda value: (10 ** 9) * value
    CONVERSIONS["EXAW:HW"] = \
        lambda value: (10 ** 16) * value
    CONVERSIONS["EXAW:KW"] = \
        lambda value: (10 ** 15) * value
    CONVERSIONS["EXAW:MEGAW"] = \
        lambda value: (10 ** 12) * value
    CONVERSIONS["EXAW:MICROW"] = \
        lambda value: (10 ** 24) * value
    CONVERSIONS["EXAW:MW"] = \
        lambda value: (10 ** 21) * value
    CONVERSIONS["EXAW:NW"] = \
        lambda value: (10 ** 27) * value
    CONVERSIONS["EXAW:PETAW"] = \
        lambda value: (10 ** 3) * value
    CONVERSIONS["EXAW:PW"] = \
        lambda value: (10 ** 30) * value
    CONVERSIONS["EXAW:TERAW"] = \
        lambda value: (10 ** 6) * value
    CONVERSIONS["EXAW:W"] = \
        lambda value: (10 ** 18) * value
    CONVERSIONS["EXAW:YOTTAW"] = \
        lambda value: (10 ** -6) * value
    CONVERSIONS["EXAW:YW"] = \
        lambda value: (10 ** 42) * value
    CONVERSIONS["EXAW:ZETTAW"] = \
        lambda value: (10 ** -3) * value
    CONVERSIONS["EXAW:ZW"] = \
        lambda value: (10 ** 39) * value
    CONVERSIONS["FW:AW"] = \
        lambda value: (10 ** 3) * value
    CONVERSIONS["FW:CW"] = \
        lambda value: (10 ** -13) * value
    CONVERSIONS["FW:DAW"] = \
        lambda value: (10 ** -16) * value
    CONVERSIONS["FW:DW"] = \
        lambda value: (10 ** -14) * value
    CONVERSIONS["FW:EXAW"] = \
        lambda value: (10 ** -33) * value
    CONVERSIONS["FW:GIGAW"] = \
        lambda value: (10 ** -24) * value
    CONVERSIONS["FW:HW"] = \
        lambda value: (10 ** -17) * value
    CONVERSIONS["FW:KW"] = \
        lambda value: (10 ** -18) * value
    CONVERSIONS["FW:MEGAW"] = \
        lambda value: (10 ** -21) * value
    CONVERSIONS["FW:MICROW"] = \
        lambda value: (10 ** -9) * value
    CONVERSIONS["FW:MW"] = \
        lambda value: (10 ** -12) * value
    CONVERSIONS["FW:NW"] = \
        lambda value: (10 ** -6) * value
    CONVERSIONS["FW:PETAW"] = \
        lambda value: (10 ** -30) * value
    CONVERSIONS["FW:PW"] = \
        lambda value: (10 ** -3) * value
    CONVERSIONS["FW:TERAW"] = \
        lambda value: (10 ** -27) * value
    CONVERSIONS["FW:W"] = \
        lambda value: (10 ** -15) * value
    CONVERSIONS["FW:YOTTAW"] = \
        lambda value: (10 ** -39) * value
    CONVERSIONS["FW:YW"] = \
        lambda value: (10 ** 9) * value
    CONVERSIONS["FW:ZETTAW"] = \
        lambda value: (10 ** -36) * value
    CONVERSIONS["FW:ZW"] = \
        lambda value: (10 ** 6) * value
    CONVERSIONS["GIGAW:AW"] = \
        lambda value: (10 ** 27) * value
    CONVERSIONS["GIGAW:CW"] = \
        lambda value: (10 ** 11) * value
    CONVERSIONS["GIGAW:DAW"] = \
        lambda value: (10 ** 8) * value
    CONVERSIONS["GIGAW:DW"] = \
        lambda value: (10 ** 10) * value
    CONVERSIONS["GIGAW:EXAW"] = \
        lambda value: (10 ** -9) * value
    CONVERSIONS["GIGAW:FW"] = \
        lambda value: (10 ** 24) * value
    CONVERSIONS["GIGAW:HW"] = \
        lambda value: (10 ** 7) * value
    CONVERSIONS["GIGAW:KW"] = \
        lambda value: (10 ** 6) * value
    CONVERSIONS["GIGAW:MEGAW"] = \
        lambda value: (10 ** 3) * value
    CONVERSIONS["GIGAW:MICROW"] = \
        lambda value: (10 ** 15) * value
    CONVERSIONS["GIGAW:MW"] = \
        lambda value: (10 ** 12) * value
    CONVERSIONS["GIGAW:NW"] = \
        lambda value: (10 ** 18) * value
    CONVERSIONS["GIGAW:PETAW"] = \
        lambda value: (10 ** -6) * value
    CONVERSIONS["GIGAW:PW"] = \
        lambda value: (10 ** 21) * value
    CONVERSIONS["GIGAW:TERAW"] = \
        lambda value: (10 ** -3) * value
    CONVERSIONS["GIGAW:W"] = \
        lambda value: (10 ** 9) * value
    CONVERSIONS["GIGAW:YOTTAW"] = \
        lambda value: (10 ** -15) * value
    CONVERSIONS["GIGAW:YW"] = \
        lambda value: (10 ** 33) * value
    CONVERSIONS["GIGAW:ZETTAW"] = \
        lambda value: (10 ** -12) * value
    CONVERSIONS["GIGAW:ZW"] = \
        lambda value: (10 ** 30) * value
    CONVERSIONS["HW:AW"] = \
        lambda value: (10 ** 20) * value
    CONVERSIONS["HW:CW"] = \
        lambda value: (10 ** 4) * value
    CONVERSIONS["HW:DAW"] = \
        lambda value: 10 * value
    CONVERSIONS["HW:DW"] = \
        lambda value: (10 ** 3) * value
    CONVERSIONS["HW:EXAW"] = \
        lambda value: (10 ** -16) * value
    CONVERSIONS["HW:FW"] = \
        lambda value: (10 ** 17) * value
    CONVERSIONS["HW:GIGAW"] = \
        lambda value: (10 ** -7) * value
    CONVERSIONS["HW:KW"] = \
        lambda value: (10 ** -1) * value
    CONVERSIONS["HW:MEGAW"] = \
        lambda value: (10 ** -4) * value
    CONVERSIONS["HW:MICROW"] = \
        lambda value: (10 ** 8) * value
    CONVERSIONS["HW:MW"] = \
        lambda value: (10 ** 5) * value
    CONVERSIONS["HW:NW"] = \
        lambda value: (10 ** 11) * value
    CONVERSIONS["HW:PETAW"] = \
        lambda value: (10 ** -13) * value
    CONVERSIONS["HW:PW"] = \
        lambda value: (10 ** 14) * value
    CONVERSIONS["HW:TERAW"] = \
        lambda value: (10 ** -10) * value
    CONVERSIONS["HW:W"] = \
        lambda value: (10 ** 2) * value
    CONVERSIONS["HW:YOTTAW"] = \
        lambda value: (10 ** -22) * value
    CONVERSIONS["HW:YW"] = \
        lambda value: (10 ** 26) * value
    CONVERSIONS["HW:ZETTAW"] = \
        lambda value: (10 ** -19) * value
    CONVERSIONS["HW:ZW"] = \
        lambda value: (10 ** 23) * value
    CONVERSIONS["KW:AW"] = \
        lambda value: (10 ** 21) * value
    CONVERSIONS["KW:CW"] = \
        lambda value: (10 ** 5) * value
    CONVERSIONS["KW:DAW"] = \
        lambda value: (10 ** 2) * value
    CONVERSIONS["KW:DW"] = \
        lambda value: (10 ** 4) * value
    CONVERSIONS["KW:EXAW"] = \
        lambda value: (10 ** -15) * value
    CONVERSIONS["KW:FW"] = \
        lambda value: (10 ** 18) * value
    CONVERSIONS["KW:GIGAW"] = \
        lambda value: (10 ** -6) * value
    CONVERSIONS["KW:HW"] = \
        lambda value: 10 * value
    CONVERSIONS["KW:MEGAW"] = \
        lambda value: (10 ** -3) * value
    CONVERSIONS["KW:MICROW"] = \
        lambda value: (10 ** 9) * value
    CONVERSIONS["KW:MW"] = \
        lambda value: (10 ** 6) * value
    CONVERSIONS["KW:NW"] = \
        lambda value: (10 ** 12) * value
    CONVERSIONS["KW:PETAW"] = \
        lambda value: (10 ** -12) * value
    CONVERSIONS["KW:PW"] = \
        lambda value: (10 ** 15) * value
    CONVERSIONS["KW:TERAW"] = \
        lambda value: (10 ** -9) * value
    CONVERSIONS["KW:W"] = \
        lambda value: (10 ** 3) * value
    CONVERSIONS["KW:YOTTAW"] = \
        lambda value: (10 ** -21) * value
    CONVERSIONS["KW:YW"] = \
        lambda value: (10 ** 27) * value
    CONVERSIONS["KW:ZETTAW"] = \
        lambda value: (10 ** -18) * value
    CONVERSIONS["KW:ZW"] = \
        lambda value: (10 ** 24) * value
    CONVERSIONS["MEGAW:AW"] = \
        lambda value: (10 ** 24) * value
    CONVERSIONS["MEGAW:CW"] = \
        lambda value: (10 ** 8) * value
    CONVERSIONS["MEGAW:DAW"] = \
        lambda value: (10 ** 5) * value
    CONVERSIONS["MEGAW:DW"] = \
        lambda value: (10 ** 7) * value
    CONVERSIONS["MEGAW:EXAW"] = \
        lambda value: (10 ** -12) * value
    CONVERSIONS["MEGAW:FW"] = \
        lambda value: (10 ** 21) * value
    CONVERSIONS["MEGAW:GIGAW"] = \
        lambda value: (10 ** -3) * value
    CONVERSIONS["MEGAW:HW"] = \
        lambda value: (10 ** 4) * value
    CONVERSIONS["MEGAW:KW"] = \
        lambda value: (10 ** 3) * value
    CONVERSIONS["MEGAW:MICROW"] = \
        lambda value: (10 ** 12) * value
    CONVERSIONS["MEGAW:MW"] = \
        lambda value: (10 ** 9) * value
    CONVERSIONS["MEGAW:NW"] = \
        lambda value: (10 ** 15) * value
    CONVERSIONS["MEGAW:PETAW"] = \
        lambda value: (10 ** -9) * value
    CONVERSIONS["MEGAW:PW"] = \
        lambda value: (10 ** 18) * value
    CONVERSIONS["MEGAW:TERAW"] = \
        lambda value: (10 ** -6) * value
    CONVERSIONS["MEGAW:W"] = \
        lambda value: (10 ** 6) * value
    CONVERSIONS["MEGAW:YOTTAW"] = \
        lambda value: (10 ** -18) * value
    CONVERSIONS["MEGAW:YW"] = \
        lambda value: (10 ** 30) * value
    CONVERSIONS["MEGAW:ZETTAW"] = \
        lambda value: (10 ** -15) * value
    CONVERSIONS["MEGAW:ZW"] = \
        lambda value: (10 ** 27) * value
    CONVERSIONS["MICROW:AW"] = \
        lambda value: (10 ** 12) * value
    CONVERSIONS["MICROW:CW"] = \
        lambda value: (10 ** -4) * value
    CONVERSIONS["MICROW:DAW"] = \
        lambda value: (10 ** -7) * value
    CONVERSIONS["MICROW:DW"] = \
        lambda value: (10 ** -5) * value
    CONVERSIONS["MICROW:EXAW"] = \
        lambda value: (10 ** -24) * value
    CONVERSIONS["MICROW:FW"] = \
        lambda value: (10 ** 9) * value
    CONVERSIONS["MICROW:GIGAW"] = \
        lambda value: (10 ** -15) * value
    CONVERSIONS["MICROW:HW"] = \
        lambda value: (10 ** -8) * value
    CONVERSIONS["MICROW:KW"] = \
        lambda value: (10 ** -9) * value
    CONVERSIONS["MICROW:MEGAW"] = \
        lambda value: (10 ** -12) * value
    CONVERSIONS["MICROW:MW"] = \
        lambda value: (10 ** -3) * value
    CONVERSIONS["MICROW:NW"] = \
        lambda value: (10 ** 3) * value
    CONVERSIONS["MICROW:PETAW"] = \
        lambda value: (10 ** -21) * value
    CONVERSIONS["MICROW:PW"] = \
        lambda value: (10 ** 6) * value
    CONVERSIONS["MICROW:TERAW"] = \
        lambda value: (10 ** -18) * value
    CONVERSIONS["MICROW:W"] = \
        lambda value: (10 ** -6) * value
    CONVERSIONS["MICROW:YOTTAW"] = \
        lambda value: (10 ** -30) * value
    CONVERSIONS["MICROW:YW"] = \
        lambda value: (10 ** 18) * value
    CONVERSIONS["MICROW:ZETTAW"] = \
        lambda value: (10 ** -27) * value
    CONVERSIONS["MICROW:ZW"] = \
        lambda value: (10 ** 15) * value
    CONVERSIONS["MW:AW"] = \
        lambda value: (10 ** 15) * value
    CONVERSIONS["MW:CW"] = \
        lambda value: (10 ** -1) * value
    CONVERSIONS["MW:DAW"] = \
        lambda value: (10 ** -4) * value
    CONVERSIONS["MW:DW"] = \
        lambda value: (10 ** -2) * value
    CONVERSIONS["MW:EXAW"] = \
        lambda value: (10 ** -21) * value
    CONVERSIONS["MW:FW"] = \
        lambda value: (10 ** 12) * value
    CONVERSIONS["MW:GIGAW"] = \
        lambda value: (10 ** -12) * value
    CONVERSIONS["MW:HW"] = \
        lambda value: (10 ** -5) * value
    CONVERSIONS["MW:KW"] = \
        lambda value: (10 ** -6) * value
    CONVERSIONS["MW:MEGAW"] = \
        lambda value: (10 ** -9) * value
    CONVERSIONS["MW:MICROW"] = \
        lambda value: (10 ** 3) * value
    CONVERSIONS["MW:NW"] = \
        lambda value: (10 ** 6) * value
    CONVERSIONS["MW:PETAW"] = \
        lambda value: (10 ** -18) * value
    CONVERSIONS["MW:PW"] = \
        lambda value: (10 ** 9) * value
    CONVERSIONS["MW:TERAW"] = \
        lambda value: (10 ** -15) * value
    CONVERSIONS["MW:W"] = \
        lambda value: (10 ** -3) * value
    CONVERSIONS["MW:YOTTAW"] = \
        lambda value: (10 ** -27) * value
    CONVERSIONS["MW:YW"] = \
        lambda value: (10 ** 21) * value
    CONVERSIONS["MW:ZETTAW"] = \
        lambda value: (10 ** -24) * value
    CONVERSIONS["MW:ZW"] = \
        lambda value: (10 ** 18) * value
    CONVERSIONS["NW:AW"] = \
        lambda value: (10 ** 9) * value
    CONVERSIONS["NW:CW"] = \
        lambda value: (10 ** -7) * value
    CONVERSIONS["NW:DAW"] = \
        lambda value: (10 ** -10) * value
    CONVERSIONS["NW:DW"] = \
        lambda value: (10 ** -8) * value
    CONVERSIONS["NW:EXAW"] = \
        lambda value: (10 ** -27) * value
    CONVERSIONS["NW:FW"] = \
        lambda value: (10 ** 6) * value
    CONVERSIONS["NW:GIGAW"] = \
        lambda value: (10 ** -18) * value
    CONVERSIONS["NW:HW"] = \
        lambda value: (10 ** -11) * value
    CONVERSIONS["NW:KW"] = \
        lambda value: (10 ** -12) * value
    CONVERSIONS["NW:MEGAW"] = \
        lambda value: (10 ** -15) * value
    CONVERSIONS["NW:MICROW"] = \
        lambda value: (10 ** -3) * value
    CONVERSIONS["NW:MW"] = \
        lambda value: (10 ** -6) * value
    CONVERSIONS["NW:PETAW"] = \
        lambda value: (10 ** -24) * value
    CONVERSIONS["NW:PW"] = \
        lambda value: (10 ** 3) * value
    CONVERSIONS["NW:TERAW"] = \
        lambda value: (10 ** -21) * value
    CONVERSIONS["NW:W"] = \
        lambda value: (10 ** -9) * value
    CONVERSIONS["NW:YOTTAW"] = \
        lambda value: (10 ** -33) * value
    CONVERSIONS["NW:YW"] = \
        lambda value: (10 ** 15) * value
    CONVERSIONS["NW:ZETTAW"] = \
        lambda value: (10 ** -30) * value
    CONVERSIONS["NW:ZW"] = \
        lambda value: (10 ** 12) * value
    CONVERSIONS["PETAW:AW"] = \
        lambda value: (10 ** 33) * value
    CONVERSIONS["PETAW:CW"] = \
        lambda value: (10 ** 17) * value
    CONVERSIONS["PETAW:DAW"] = \
        lambda value: (10 ** 14) * value
    CONVERSIONS["PETAW:DW"] = \
        lambda value: (10 ** 16) * value
    CONVERSIONS["PETAW:EXAW"] = \
        lambda value: (10 ** -3) * value
    CONVERSIONS["PETAW:FW"] = \
        lambda value: (10 ** 30) * value
    CONVERSIONS["PETAW:GIGAW"] = \
        lambda value: (10 ** 6) * value
    CONVERSIONS["PETAW:HW"] = \
        lambda value: (10 ** 13) * value
    CONVERSIONS["PETAW:KW"] = \
        lambda value: (10 ** 12) * value
    CONVERSIONS["PETAW:MEGAW"] = \
        lambda value: (10 ** 9) * value
    CONVERSIONS["PETAW:MICROW"] = \
        lambda value: (10 ** 21) * value
    CONVERSIONS["PETAW:MW"] = \
        lambda value: (10 ** 18) * value
    CONVERSIONS["PETAW:NW"] = \
        lambda value: (10 ** 24) * value
    CONVERSIONS["PETAW:PW"] = \
        lambda value: (10 ** 27) * value
    CONVERSIONS["PETAW:TERAW"] = \
        lambda value: (10 ** 3) * value
    CONVERSIONS["PETAW:W"] = \
        lambda value: (10 ** 15) * value
    CONVERSIONS["PETAW:YOTTAW"] = \
        lambda value: (10 ** -9) * value
    CONVERSIONS["PETAW:YW"] = \
        lambda value: (10 ** 39) * value
    CONVERSIONS["PETAW:ZETTAW"] = \
        lambda value: (10 ** -6) * value
    CONVERSIONS["PETAW:ZW"] = \
        lambda value: (10 ** 36) * value
    CONVERSIONS["PW:AW"] = \
        lambda value: (10 ** 6) * value
    CONVERSIONS["PW:CW"] = \
        lambda value: (10 ** -10) * value
    CONVERSIONS["PW:DAW"] = \
        lambda value: (10 ** -13) * value
    CONVERSIONS["PW:DW"] = \
        lambda value: (10 ** -11) * value
    CONVERSIONS["PW:EXAW"] = \
        lambda value: (10 ** -30) * value
    CONVERSIONS["PW:FW"] = \
        lambda value: (10 ** 3) * value
    CONVERSIONS["PW:GIGAW"] = \
        lambda value: (10 ** -21) * value
    CONVERSIONS["PW:HW"] = \
        lambda value: (10 ** -14) * value
    CONVERSIONS["PW:KW"] = \
        lambda value: (10 ** -15) * value
    CONVERSIONS["PW:MEGAW"] = \
        lambda value: (10 ** -18) * value
    CONVERSIONS["PW:MICROW"] = \
        lambda value: (10 ** -6) * value
    CONVERSIONS["PW:MW"] = \
        lambda value: (10 ** -9) * value
    CONVERSIONS["PW:NW"] = \
        lambda value: (10 ** -3) * value
    CONVERSIONS["PW:PETAW"] = \
        lambda value: (10 ** -27) * value
    CONVERSIONS["PW:TERAW"] = \
        lambda value: (10 ** -24) * value
    CONVERSIONS["PW:W"] = \
        lambda value: (10 ** -12) * value
    CONVERSIONS["PW:YOTTAW"] = \
        lambda value: (10 ** -36) * value
    CONVERSIONS["PW:YW"] = \
        lambda value: (10 ** 12) * value
    CONVERSIONS["PW:ZETTAW"] = \
        lambda value: (10 ** -33) * value
    CONVERSIONS["PW:ZW"] = \
        lambda value: (10 ** 9) * value
    CONVERSIONS["TERAW:AW"] = \
        lambda value: (10 ** 30) * value
    CONVERSIONS["TERAW:CW"] = \
        lambda value: (10 ** 14) * value
    CONVERSIONS["TERAW:DAW"] = \
        lambda value: (10 ** 11) * value
    CONVERSIONS["TERAW:DW"] = \
        lambda value: (10 ** 13) * value
    CONVERSIONS["TERAW:EXAW"] = \
        lambda value: (10 ** -6) * value
    CONVERSIONS["TERAW:FW"] = \
        lambda value: (10 ** 27) * value
    CONVERSIONS["TERAW:GIGAW"] = \
        lambda value: (10 ** 3) * value
    CONVERSIONS["TERAW:HW"] = \
        lambda value: (10 ** 10) * value
    CONVERSIONS["TERAW:KW"] = \
        lambda value: (10 ** 9) * value
    CONVERSIONS["TERAW:MEGAW"] = \
        lambda value: (10 ** 6) * value
    CONVERSIONS["TERAW:MICROW"] = \
        lambda value: (10 ** 18) * value
    CONVERSIONS["TERAW:MW"] = \
        lambda value: (10 ** 15) * value
    CONVERSIONS["TERAW:NW"] = \
        lambda value: (10 ** 21) * value
    CONVERSIONS["TERAW:PETAW"] = \
        lambda value: (10 ** -3) * value
    CONVERSIONS["TERAW:PW"] = \
        lambda value: (10 ** 24) * value
    CONVERSIONS["TERAW:W"] = \
        lambda value: (10 ** 12) * value
    CONVERSIONS["TERAW:YOTTAW"] = \
        lambda value: (10 ** -12) * value
    CONVERSIONS["TERAW:YW"] = \
        lambda value: (10 ** 36) * value
    CONVERSIONS["TERAW:ZETTAW"] = \
        lambda value: (10 ** -9) * value
    CONVERSIONS["TERAW:ZW"] = \
        lambda value: (10 ** 33) * value
    CONVERSIONS["W:AW"] = \
        lambda value: (10 ** 18) * value
    CONVERSIONS["W:CW"] = \
        lambda value: (10 ** 2) * value
    CONVERSIONS["W:DAW"] = \
        lambda value: (10 ** -1) * value
    CONVERSIONS["W:DW"] = \
        lambda value: 10 * value
    CONVERSIONS["W:EXAW"] = \
        lambda value: (10 ** -18) * value
    CONVERSIONS["W:FW"] = \
        lambda value: (10 ** 15) * value
    CONVERSIONS["W:GIGAW"] = \
        lambda value: (10 ** -9) * value
    CONVERSIONS["W:HW"] = \
        lambda value: (10 ** -2) * value
    CONVERSIONS["W:KW"] = \
        lambda value: (10 ** -3) * value
    CONVERSIONS["W:MEGAW"] = \
        lambda value: (10 ** -6) * value
    CONVERSIONS["W:MICROW"] = \
        lambda value: (10 ** 6) * value
    CONVERSIONS["W:MW"] = \
        lambda value: (10 ** 3) * value
    CONVERSIONS["W:NW"] = \
        lambda value: (10 ** 9) * value
    CONVERSIONS["W:PETAW"] = \
        lambda value: (10 ** -15) * value
    CONVERSIONS["W:PW"] = \
        lambda value: (10 ** 12) * value
    CONVERSIONS["W:TERAW"] = \
        lambda value: (10 ** -12) * value
    CONVERSIONS["W:YOTTAW"] = \
        lambda value: (10 ** -24) * value
    CONVERSIONS["W:YW"] = \
        lambda value: (10 ** 24) * value
    CONVERSIONS["W:ZETTAW"] = \
        lambda value: (10 ** -21) * value
    CONVERSIONS["W:ZW"] = \
        lambda value: (10 ** 21) * value
    CONVERSIONS["YOTTAW:AW"] = \
        lambda value: (10 ** 42) * value
    CONVERSIONS["YOTTAW:CW"] = \
        lambda value: (10 ** 26) * value
    CONVERSIONS["YOTTAW:DAW"] = \
        lambda value: (10 ** 23) * value
    CONVERSIONS["YOTTAW:DW"] = \
        lambda value: (10 ** 25) * value
    CONVERSIONS["YOTTAW:EXAW"] = \
        lambda value: (10 ** 6) * value
    CONVERSIONS["YOTTAW:FW"] = \
        lambda value: (10 ** 39) * value
    CONVERSIONS["YOTTAW:GIGAW"] = \
        lambda value: (10 ** 15) * value
    CONVERSIONS["YOTTAW:HW"] = \
        lambda value: (10 ** 22) * value
    CONVERSIONS["YOTTAW:KW"] = \
        lambda value: (10 ** 21) * value
    CONVERSIONS["YOTTAW:MEGAW"] = \
        lambda value: (10 ** 18) * value
    CONVERSIONS["YOTTAW:MICROW"] = \
        lambda value: (10 ** 30) * value
    CONVERSIONS["YOTTAW:MW"] = \
        lambda value: (10 ** 27) * value
    CONVERSIONS["YOTTAW:NW"] = \
        lambda value: (10 ** 33) * value
    CONVERSIONS["YOTTAW:PETAW"] = \
        lambda value: (10 ** 9) * value
    CONVERSIONS["YOTTAW:PW"] = \
        lambda value: (10 ** 36) * value
    CONVERSIONS["YOTTAW:TERAW"] = \
        lambda value: (10 ** 12) * value
    CONVERSIONS["YOTTAW:W"] = \
        lambda value: (10 ** 24) * value
    CONVERSIONS["YOTTAW:YW"] = \
        lambda value: (10 ** 48) * value
    CONVERSIONS["YOTTAW:ZETTAW"] = \
        lambda value: (10 ** 3) * value
    CONVERSIONS["YOTTAW:ZW"] = \
        lambda value: (10 ** 45) * value
    CONVERSIONS["YW:AW"] = \
        lambda value: (10 ** -6) * value
    CONVERSIONS["YW:CW"] = \
        lambda value: (10 ** -22) * value
    CONVERSIONS["YW:DAW"] = \
        lambda value: (10 ** -25) * value
    CONVERSIONS["YW:DW"] = \
        lambda value: (10 ** -23) * value
    CONVERSIONS["YW:EXAW"] = \
        lambda value: (10 ** -42) * value
    CONVERSIONS["YW:FW"] = \
        lambda value: (10 ** -9) * value
    CONVERSIONS["YW:GIGAW"] = \
        lambda value: (10 ** -33) * value
    CONVERSIONS["YW:HW"] = \
        lambda value: (10 ** -26) * value
    CONVERSIONS["YW:KW"] = \
        lambda value: (10 ** -27) * value
    CONVERSIONS["YW:MEGAW"] = \
        lambda value: (10 ** -30) * value
    CONVERSIONS["YW:MICROW"] = \
        lambda value: (10 ** -18) * value
    CONVERSIONS["YW:MW"] = \
        lambda value: (10 ** -21) * value
    CONVERSIONS["YW:NW"] = \
        lambda value: (10 ** -15) * value
    CONVERSIONS["YW:PETAW"] = \
        lambda value: (10 ** -39) * value
    CONVERSIONS["YW:PW"] = \
        lambda value: (10 ** -12) * value
    CONVERSIONS["YW:TERAW"] = \
        lambda value: (10 ** -36) * value
    CONVERSIONS["YW:W"] = \
        lambda value: (10 ** -24) * value
    CONVERSIONS["YW:YOTTAW"] = \
        lambda value: (10 ** -48) * value
    CONVERSIONS["YW:ZETTAW"] = \
        lambda value: (10 ** -45) * value
    CONVERSIONS["YW:ZW"] = \
        lambda value: (10 ** -3) * value
    CONVERSIONS["ZETTAW:AW"] = \
        lambda value: (10 ** 39) * value
    CONVERSIONS["ZETTAW:CW"] = \
        lambda value: (10 ** 23) * value
    CONVERSIONS["ZETTAW:DAW"] = \
        lambda value: (10 ** 20) * value
    CONVERSIONS["ZETTAW:DW"] = \
        lambda value: (10 ** 22) * value
    CONVERSIONS["ZETTAW:EXAW"] = \
        lambda value: (10 ** 3) * value
    CONVERSIONS["ZETTAW:FW"] = \
        lambda value: (10 ** 36) * value
    CONVERSIONS["ZETTAW:GIGAW"] = \
        lambda value: (10 ** 12) * value
    CONVERSIONS["ZETTAW:HW"] = \
        lambda value: (10 ** 19) * value
    CONVERSIONS["ZETTAW:KW"] = \
        lambda value: (10 ** 18) * value
    CONVERSIONS["ZETTAW:MEGAW"] = \
        lambda value: (10 ** 15) * value
    CONVERSIONS["ZETTAW:MICROW"] = \
        lambda value: (10 ** 27) * value
    CONVERSIONS["ZETTAW:MW"] = \
        lambda value: (10 ** 24) * value
    CONVERSIONS["ZETTAW:NW"] = \
        lambda value: (10 ** 30) * value
    CONVERSIONS["ZETTAW:PETAW"] = \
        lambda value: (10 ** 6) * value
    CONVERSIONS["ZETTAW:PW"] = \
        lambda value: (10 ** 33) * value
    CONVERSIONS["ZETTAW:TERAW"] = \
        lambda value: (10 ** 9) * value
    CONVERSIONS["ZETTAW:W"] = \
        lambda value: (10 ** 21) * value
    CONVERSIONS["ZETTAW:YOTTAW"] = \
        lambda value: (10 ** -3) * value
    CONVERSIONS["ZETTAW:YW"] = \
        lambda value: (10 ** 45) * value
    CONVERSIONS["ZETTAW:ZW"] = \
        lambda value: (10 ** 42) * value
    CONVERSIONS["ZW:AW"] = \
        lambda value: (10 ** -3) * value
    CONVERSIONS["ZW:CW"] = \
        lambda value: (10 ** -19) * value
    CONVERSIONS["ZW:DAW"] = \
        lambda value: (10 ** -22) * value
    CONVERSIONS["ZW:DW"] = \
        lambda value: (10 ** -20) * value
    CONVERSIONS["ZW:EXAW"] = \
        lambda value: (10 ** -39) * value
    CONVERSIONS["ZW:FW"] = \
        lambda value: (10 ** -6) * value
    CONVERSIONS["ZW:GIGAW"] = \
        lambda value: (10 ** -30) * value
    CONVERSIONS["ZW:HW"] = \
        lambda value: (10 ** -23) * value
    CONVERSIONS["ZW:KW"] = \
        lambda value: (10 ** -24) * value
    CONVERSIONS["ZW:MEGAW"] = \
        lambda value: (10 ** -27) * value
    CONVERSIONS["ZW:MICROW"] = \
        lambda value: (10 ** -15) * value
    CONVERSIONS["ZW:MW"] = \
        lambda value: (10 ** -18) * value
    CONVERSIONS["ZW:NW"] = \
        lambda value: (10 ** -12) * value
    CONVERSIONS["ZW:PETAW"] = \
        lambda value: (10 ** -36) * value
    CONVERSIONS["ZW:PW"] = \
        lambda value: (10 ** -9) * value
    CONVERSIONS["ZW:TERAW"] = \
        lambda value: (10 ** -33) * value
    CONVERSIONS["ZW:W"] = \
        lambda value: (10 ** -21) * value
    CONVERSIONS["ZW:YOTTAW"] = \
        lambda value: (10 ** -45) * value
    CONVERSIONS["ZW:YW"] = \
        lambda value: (10 ** 3) * value
    CONVERSIONS["ZW:ZETTAW"] = \
        lambda value: (10 ** -42) * value

    SYMBOLS = dict()
    SYMBOLS["AW"] = "aW"
    SYMBOLS["CW"] = "cW"
    SYMBOLS["DAW"] = "daW"
    SYMBOLS["DW"] = "dW"
    SYMBOLS["EXAW"] = "EW"
    SYMBOLS["FW"] = "fW"
    SYMBOLS["GIGAW"] = "GW"
    SYMBOLS["HW"] = "hW"
    SYMBOLS["KW"] = "kW"
    SYMBOLS["MEGAW"] = "MW"
    SYMBOLS["MICROW"] = "µW"
    SYMBOLS["MW"] = "mW"
    SYMBOLS["NW"] = "nW"
    SYMBOLS["PETAW"] = "PW"
    SYMBOLS["PW"] = "pW"
    SYMBOLS["TERAW"] = "TW"
    SYMBOLS["W"] = "W"
    SYMBOLS["YOTTAW"] = "YW"
    SYMBOLS["YW"] = "yW"
    SYMBOLS["ZETTAW"] = "ZW"
    SYMBOLS["ZW"] = "zW"

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

    def getSymbol(self):
        return self.SYMBOLS.get(str(self.getUnit()))

    def setUnit(self, unit, current=None):
        self._unit = unit

    def setValue(self, value, current=None):
        self._value = value

    def __str__(self):
        return self._base_string(self.getValue(), self.getUnit())

_omero_model.PowerI = PowerI
