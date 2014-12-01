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
Code-generated omero.model.Length implementation,
based on omero.model.PermissionsI
"""


import Ice
import IceImport
IceImport.load("omero_model_Length_ice")
_omero = Ice.openModule("omero")
_omero_model = Ice.openModule("omero.model")
__name__ = "omero.model"

from omero_model_UnitBase import UnitBase
from omero.model.enums import UnitsLength


def noconversion(cfrom, cto):
    raise Exception(("Unsupported conversion: "
                     "%s:%s") % cfrom, cto)


class LengthI(_omero_model.Length, UnitBase):

    CONVERSIONS = dict()
    CONVERSIONS["AM:ANGSTROM"] = \
        lambda value: (10 ** -8) * value
    CONVERSIONS["AM:CM"] = \
        lambda value: (10 ** -16) * value
    CONVERSIONS["AM:DAM"] = \
        lambda value: (10 ** -19) * value
    CONVERSIONS["AM:DM"] = \
        lambda value: (10 ** -17) * value
    CONVERSIONS["AM:EXAM"] = \
        lambda value: (10 ** -36) * value
    CONVERSIONS["AM:FM"] = \
        lambda value: (10 ** -3) * value
    CONVERSIONS["AM:FT"] = \
        lambda: noconversion("AM", "FT")
    CONVERSIONS["AM:GIGAM"] = \
        lambda value: (10 ** -27) * value
    CONVERSIONS["AM:HM"] = \
        lambda value: (10 ** -20) * value
    CONVERSIONS["AM:IN"] = \
        lambda: noconversion("AM", "IN")
    CONVERSIONS["AM:KM"] = \
        lambda value: (10 ** -21) * value
    CONVERSIONS["AM:LI"] = \
        lambda: noconversion("AM", "LI")
    CONVERSIONS["AM:LY"] = \
        lambda: noconversion("AM", "LY")
    CONVERSIONS["AM:M"] = \
        lambda value: (10 ** -18) * value
    CONVERSIONS["AM:MEGAM"] = \
        lambda value: (10 ** -24) * value
    CONVERSIONS["AM:MI"] = \
        lambda: noconversion("AM", "MI")
    CONVERSIONS["AM:MICROM"] = \
        lambda value: (10 ** -12) * value
    CONVERSIONS["AM:MM"] = \
        lambda value: (10 ** -15) * value
    CONVERSIONS["AM:NM"] = \
        lambda value: (10 ** -9) * value
    CONVERSIONS["AM:PC"] = \
        lambda: noconversion("AM", "PC")
    CONVERSIONS["AM:PETAM"] = \
        lambda value: (10 ** -33) * value
    CONVERSIONS["AM:PIXEL"] = \
        lambda: noconversion("AM", "PIXEL")
    CONVERSIONS["AM:PM"] = \
        lambda value: (10 ** -6) * value
    CONVERSIONS["AM:PT"] = \
        lambda: noconversion("AM", "PT")
    CONVERSIONS["AM:REFERENCEFRAME"] = \
        lambda: noconversion("AM", "REFERENCEFRAME")
    CONVERSIONS["AM:TERAM"] = \
        lambda value: (10 ** -30) * value
    CONVERSIONS["AM:THOU"] = \
        lambda: noconversion("AM", "THOU")
    CONVERSIONS["AM:UA"] = \
        lambda: noconversion("AM", "UA")
    CONVERSIONS["AM:YD"] = \
        lambda: noconversion("AM", "YD")
    CONVERSIONS["AM:YM"] = \
        lambda value: (10 ** 6) * value
    CONVERSIONS["AM:YOTTAM"] = \
        lambda value: (10 ** -42) * value
    CONVERSIONS["AM:ZETTAM"] = \
        lambda value: (10 ** -39) * value
    CONVERSIONS["AM:ZM"] = \
        lambda value: (10 ** 3) * value
    CONVERSIONS["ANGSTROM:AM"] = \
        lambda value: (10 ** 8) * value
    CONVERSIONS["ANGSTROM:CM"] = \
        lambda value: (10 ** -8) * value
    CONVERSIONS["ANGSTROM:DAM"] = \
        lambda value: (10 ** -11) * value
    CONVERSIONS["ANGSTROM:DM"] = \
        lambda value: (10 ** -9) * value
    CONVERSIONS["ANGSTROM:EXAM"] = \
        lambda value: (10 ** -28) * value
    CONVERSIONS["ANGSTROM:FM"] = \
        lambda value: (10 ** 5) * value
    CONVERSIONS["ANGSTROM:FT"] = \
        lambda: noconversion("ANGSTROM", "FT")
    CONVERSIONS["ANGSTROM:GIGAM"] = \
        lambda value: (10 ** -19) * value
    CONVERSIONS["ANGSTROM:HM"] = \
        lambda value: (10 ** -12) * value
    CONVERSIONS["ANGSTROM:IN"] = \
        lambda: noconversion("ANGSTROM", "IN")
    CONVERSIONS["ANGSTROM:KM"] = \
        lambda value: (10 ** -13) * value
    CONVERSIONS["ANGSTROM:LI"] = \
        lambda: noconversion("ANGSTROM", "LI")
    CONVERSIONS["ANGSTROM:LY"] = \
        lambda: noconversion("ANGSTROM", "LY")
    CONVERSIONS["ANGSTROM:M"] = \
        lambda value: (10 ** -10) * value
    CONVERSIONS["ANGSTROM:MEGAM"] = \
        lambda value: (10 ** -16) * value
    CONVERSIONS["ANGSTROM:MI"] = \
        lambda: noconversion("ANGSTROM", "MI")
    CONVERSIONS["ANGSTROM:MICROM"] = \
        lambda value: (10 ** -4) * value
    CONVERSIONS["ANGSTROM:MM"] = \
        lambda value: (10 ** -7) * value
    CONVERSIONS["ANGSTROM:NM"] = \
        lambda value: (10 ** -1) * value
    CONVERSIONS["ANGSTROM:PC"] = \
        lambda: noconversion("ANGSTROM", "PC")
    CONVERSIONS["ANGSTROM:PETAM"] = \
        lambda value: (10 ** -25) * value
    CONVERSIONS["ANGSTROM:PIXEL"] = \
        lambda: noconversion("ANGSTROM", "PIXEL")
    CONVERSIONS["ANGSTROM:PM"] = \
        lambda value: (10 ** 2) * value
    CONVERSIONS["ANGSTROM:PT"] = \
        lambda: noconversion("ANGSTROM", "PT")
    CONVERSIONS["ANGSTROM:REFERENCEFRAME"] = \
        lambda: noconversion("ANGSTROM", "REFERENCEFRAME")
    CONVERSIONS["ANGSTROM:TERAM"] = \
        lambda value: (10 ** -22) * value
    CONVERSIONS["ANGSTROM:THOU"] = \
        lambda: noconversion("ANGSTROM", "THOU")
    CONVERSIONS["ANGSTROM:UA"] = \
        lambda: noconversion("ANGSTROM", "UA")
    CONVERSIONS["ANGSTROM:YD"] = \
        lambda: noconversion("ANGSTROM", "YD")
    CONVERSIONS["ANGSTROM:YM"] = \
        lambda value: (10 ** 14) * value
    CONVERSIONS["ANGSTROM:YOTTAM"] = \
        lambda value: (10 ** -34) * value
    CONVERSIONS["ANGSTROM:ZETTAM"] = \
        lambda value: (10 ** -31) * value
    CONVERSIONS["ANGSTROM:ZM"] = \
        lambda value: (10 ** 11) * value
    CONVERSIONS["CM:AM"] = \
        lambda value: (10 ** 16) * value
    CONVERSIONS["CM:ANGSTROM"] = \
        lambda value: (10 ** 8) * value
    CONVERSIONS["CM:DAM"] = \
        lambda value: (10 ** -3) * value
    CONVERSIONS["CM:DM"] = \
        lambda value: (10 ** -1) * value
    CONVERSIONS["CM:EXAM"] = \
        lambda value: (10 ** -20) * value
    CONVERSIONS["CM:FM"] = \
        lambda value: (10 ** 13) * value
    CONVERSIONS["CM:FT"] = \
        lambda: noconversion("CM", "FT")
    CONVERSIONS["CM:GIGAM"] = \
        lambda value: (10 ** -11) * value
    CONVERSIONS["CM:HM"] = \
        lambda value: (10 ** -4) * value
    CONVERSIONS["CM:IN"] = \
        lambda: noconversion("CM", "IN")
    CONVERSIONS["CM:KM"] = \
        lambda value: (10 ** -5) * value
    CONVERSIONS["CM:LI"] = \
        lambda: noconversion("CM", "LI")
    CONVERSIONS["CM:LY"] = \
        lambda: noconversion("CM", "LY")
    CONVERSIONS["CM:M"] = \
        lambda value: (10 ** -2) * value
    CONVERSIONS["CM:MEGAM"] = \
        lambda value: (10 ** -8) * value
    CONVERSIONS["CM:MI"] = \
        lambda: noconversion("CM", "MI")
    CONVERSIONS["CM:MICROM"] = \
        lambda value: (10 ** 4) * value
    CONVERSIONS["CM:MM"] = \
        lambda value: 10 * value
    CONVERSIONS["CM:NM"] = \
        lambda value: (10 ** 7) * value
    CONVERSIONS["CM:PC"] = \
        lambda: noconversion("CM", "PC")
    CONVERSIONS["CM:PETAM"] = \
        lambda value: (10 ** -17) * value
    CONVERSIONS["CM:PIXEL"] = \
        lambda: noconversion("CM", "PIXEL")
    CONVERSIONS["CM:PM"] = \
        lambda value: (10 ** 10) * value
    CONVERSIONS["CM:PT"] = \
        lambda: noconversion("CM", "PT")
    CONVERSIONS["CM:REFERENCEFRAME"] = \
        lambda: noconversion("CM", "REFERENCEFRAME")
    CONVERSIONS["CM:TERAM"] = \
        lambda value: (10 ** -14) * value
    CONVERSIONS["CM:THOU"] = \
        lambda: noconversion("CM", "THOU")
    CONVERSIONS["CM:UA"] = \
        lambda: noconversion("CM", "UA")
    CONVERSIONS["CM:YD"] = \
        lambda: noconversion("CM", "YD")
    CONVERSIONS["CM:YM"] = \
        lambda value: (10 ** 22) * value
    CONVERSIONS["CM:YOTTAM"] = \
        lambda value: (10 ** -26) * value
    CONVERSIONS["CM:ZETTAM"] = \
        lambda value: (10 ** -23) * value
    CONVERSIONS["CM:ZM"] = \
        lambda value: (10 ** 19) * value
    CONVERSIONS["DAM:AM"] = \
        lambda value: (10 ** 19) * value
    CONVERSIONS["DAM:ANGSTROM"] = \
        lambda value: (10 ** 11) * value
    CONVERSIONS["DAM:CM"] = \
        lambda value: (10 ** 3) * value
    CONVERSIONS["DAM:DM"] = \
        lambda value: (10 ** 2) * value
    CONVERSIONS["DAM:EXAM"] = \
        lambda value: (10 ** -17) * value
    CONVERSIONS["DAM:FM"] = \
        lambda value: (10 ** 16) * value
    CONVERSIONS["DAM:FT"] = \
        lambda: noconversion("DAM", "FT")
    CONVERSIONS["DAM:GIGAM"] = \
        lambda value: (10 ** -8) * value
    CONVERSIONS["DAM:HM"] = \
        lambda value: (10 ** -1) * value
    CONVERSIONS["DAM:IN"] = \
        lambda: noconversion("DAM", "IN")
    CONVERSIONS["DAM:KM"] = \
        lambda value: (10 ** -2) * value
    CONVERSIONS["DAM:LI"] = \
        lambda: noconversion("DAM", "LI")
    CONVERSIONS["DAM:LY"] = \
        lambda: noconversion("DAM", "LY")
    CONVERSIONS["DAM:M"] = \
        lambda value: 10 * value
    CONVERSIONS["DAM:MEGAM"] = \
        lambda value: (10 ** -5) * value
    CONVERSIONS["DAM:MI"] = \
        lambda: noconversion("DAM", "MI")
    CONVERSIONS["DAM:MICROM"] = \
        lambda value: (10 ** 7) * value
    CONVERSIONS["DAM:MM"] = \
        lambda value: (10 ** 4) * value
    CONVERSIONS["DAM:NM"] = \
        lambda value: (10 ** 10) * value
    CONVERSIONS["DAM:PC"] = \
        lambda: noconversion("DAM", "PC")
    CONVERSIONS["DAM:PETAM"] = \
        lambda value: (10 ** -14) * value
    CONVERSIONS["DAM:PIXEL"] = \
        lambda: noconversion("DAM", "PIXEL")
    CONVERSIONS["DAM:PM"] = \
        lambda value: (10 ** 13) * value
    CONVERSIONS["DAM:PT"] = \
        lambda: noconversion("DAM", "PT")
    CONVERSIONS["DAM:REFERENCEFRAME"] = \
        lambda: noconversion("DAM", "REFERENCEFRAME")
    CONVERSIONS["DAM:TERAM"] = \
        lambda value: (10 ** -11) * value
    CONVERSIONS["DAM:THOU"] = \
        lambda: noconversion("DAM", "THOU")
    CONVERSIONS["DAM:UA"] = \
        lambda: noconversion("DAM", "UA")
    CONVERSIONS["DAM:YD"] = \
        lambda: noconversion("DAM", "YD")
    CONVERSIONS["DAM:YM"] = \
        lambda value: (10 ** 25) * value
    CONVERSIONS["DAM:YOTTAM"] = \
        lambda value: (10 ** -23) * value
    CONVERSIONS["DAM:ZETTAM"] = \
        lambda value: (10 ** -20) * value
    CONVERSIONS["DAM:ZM"] = \
        lambda value: (10 ** 22) * value
    CONVERSIONS["DM:AM"] = \
        lambda value: (10 ** 17) * value
    CONVERSIONS["DM:ANGSTROM"] = \
        lambda value: (10 ** 9) * value
    CONVERSIONS["DM:CM"] = \
        lambda value: 10 * value
    CONVERSIONS["DM:DAM"] = \
        lambda value: (10 ** -2) * value
    CONVERSIONS["DM:EXAM"] = \
        lambda value: (10 ** -19) * value
    CONVERSIONS["DM:FM"] = \
        lambda value: (10 ** 14) * value
    CONVERSIONS["DM:FT"] = \
        lambda: noconversion("DM", "FT")
    CONVERSIONS["DM:GIGAM"] = \
        lambda value: (10 ** -10) * value
    CONVERSIONS["DM:HM"] = \
        lambda value: (10 ** -3) * value
    CONVERSIONS["DM:IN"] = \
        lambda: noconversion("DM", "IN")
    CONVERSIONS["DM:KM"] = \
        lambda value: (10 ** -4) * value
    CONVERSIONS["DM:LI"] = \
        lambda: noconversion("DM", "LI")
    CONVERSIONS["DM:LY"] = \
        lambda: noconversion("DM", "LY")
    CONVERSIONS["DM:M"] = \
        lambda value: (10 ** -1) * value
    CONVERSIONS["DM:MEGAM"] = \
        lambda value: (10 ** -7) * value
    CONVERSIONS["DM:MI"] = \
        lambda: noconversion("DM", "MI")
    CONVERSIONS["DM:MICROM"] = \
        lambda value: (10 ** 5) * value
    CONVERSIONS["DM:MM"] = \
        lambda value: (10 ** 2) * value
    CONVERSIONS["DM:NM"] = \
        lambda value: (10 ** 8) * value
    CONVERSIONS["DM:PC"] = \
        lambda: noconversion("DM", "PC")
    CONVERSIONS["DM:PETAM"] = \
        lambda value: (10 ** -16) * value
    CONVERSIONS["DM:PIXEL"] = \
        lambda: noconversion("DM", "PIXEL")
    CONVERSIONS["DM:PM"] = \
        lambda value: (10 ** 11) * value
    CONVERSIONS["DM:PT"] = \
        lambda: noconversion("DM", "PT")
    CONVERSIONS["DM:REFERENCEFRAME"] = \
        lambda: noconversion("DM", "REFERENCEFRAME")
    CONVERSIONS["DM:TERAM"] = \
        lambda value: (10 ** -13) * value
    CONVERSIONS["DM:THOU"] = \
        lambda: noconversion("DM", "THOU")
    CONVERSIONS["DM:UA"] = \
        lambda: noconversion("DM", "UA")
    CONVERSIONS["DM:YD"] = \
        lambda: noconversion("DM", "YD")
    CONVERSIONS["DM:YM"] = \
        lambda value: (10 ** 23) * value
    CONVERSIONS["DM:YOTTAM"] = \
        lambda value: (10 ** -25) * value
    CONVERSIONS["DM:ZETTAM"] = \
        lambda value: (10 ** -22) * value
    CONVERSIONS["DM:ZM"] = \
        lambda value: (10 ** 20) * value
    CONVERSIONS["EXAM:AM"] = \
        lambda value: (10 ** 36) * value
    CONVERSIONS["EXAM:ANGSTROM"] = \
        lambda value: (10 ** 28) * value
    CONVERSIONS["EXAM:CM"] = \
        lambda value: (10 ** 20) * value
    CONVERSIONS["EXAM:DAM"] = \
        lambda value: (10 ** 17) * value
    CONVERSIONS["EXAM:DM"] = \
        lambda value: (10 ** 19) * value
    CONVERSIONS["EXAM:FM"] = \
        lambda value: (10 ** 33) * value
    CONVERSIONS["EXAM:FT"] = \
        lambda: noconversion("EXAM", "FT")
    CONVERSIONS["EXAM:GIGAM"] = \
        lambda value: (10 ** 9) * value
    CONVERSIONS["EXAM:HM"] = \
        lambda value: (10 ** 16) * value
    CONVERSIONS["EXAM:IN"] = \
        lambda: noconversion("EXAM", "IN")
    CONVERSIONS["EXAM:KM"] = \
        lambda value: (10 ** 15) * value
    CONVERSIONS["EXAM:LI"] = \
        lambda: noconversion("EXAM", "LI")
    CONVERSIONS["EXAM:LY"] = \
        lambda: noconversion("EXAM", "LY")
    CONVERSIONS["EXAM:M"] = \
        lambda value: (10 ** 18) * value
    CONVERSIONS["EXAM:MEGAM"] = \
        lambda value: (10 ** 12) * value
    CONVERSIONS["EXAM:MI"] = \
        lambda: noconversion("EXAM", "MI")
    CONVERSIONS["EXAM:MICROM"] = \
        lambda value: (10 ** 24) * value
    CONVERSIONS["EXAM:MM"] = \
        lambda value: (10 ** 21) * value
    CONVERSIONS["EXAM:NM"] = \
        lambda value: (10 ** 27) * value
    CONVERSIONS["EXAM:PC"] = \
        lambda: noconversion("EXAM", "PC")
    CONVERSIONS["EXAM:PETAM"] = \
        lambda value: (10 ** 3) * value
    CONVERSIONS["EXAM:PIXEL"] = \
        lambda: noconversion("EXAM", "PIXEL")
    CONVERSIONS["EXAM:PM"] = \
        lambda value: (10 ** 30) * value
    CONVERSIONS["EXAM:PT"] = \
        lambda: noconversion("EXAM", "PT")
    CONVERSIONS["EXAM:REFERENCEFRAME"] = \
        lambda: noconversion("EXAM", "REFERENCEFRAME")
    CONVERSIONS["EXAM:TERAM"] = \
        lambda value: (10 ** 6) * value
    CONVERSIONS["EXAM:THOU"] = \
        lambda: noconversion("EXAM", "THOU")
    CONVERSIONS["EXAM:UA"] = \
        lambda: noconversion("EXAM", "UA")
    CONVERSIONS["EXAM:YD"] = \
        lambda: noconversion("EXAM", "YD")
    CONVERSIONS["EXAM:YM"] = \
        lambda value: (10 ** 42) * value
    CONVERSIONS["EXAM:YOTTAM"] = \
        lambda value: (10 ** -6) * value
    CONVERSIONS["EXAM:ZETTAM"] = \
        lambda value: (10 ** -3) * value
    CONVERSIONS["EXAM:ZM"] = \
        lambda value: (10 ** 39) * value
    CONVERSIONS["FM:AM"] = \
        lambda value: (10 ** 3) * value
    CONVERSIONS["FM:ANGSTROM"] = \
        lambda value: (10 ** -5) * value
    CONVERSIONS["FM:CM"] = \
        lambda value: (10 ** -13) * value
    CONVERSIONS["FM:DAM"] = \
        lambda value: (10 ** -16) * value
    CONVERSIONS["FM:DM"] = \
        lambda value: (10 ** -14) * value
    CONVERSIONS["FM:EXAM"] = \
        lambda value: (10 ** -33) * value
    CONVERSIONS["FM:FT"] = \
        lambda: noconversion("FM", "FT")
    CONVERSIONS["FM:GIGAM"] = \
        lambda value: (10 ** -24) * value
    CONVERSIONS["FM:HM"] = \
        lambda value: (10 ** -17) * value
    CONVERSIONS["FM:IN"] = \
        lambda: noconversion("FM", "IN")
    CONVERSIONS["FM:KM"] = \
        lambda value: (10 ** -18) * value
    CONVERSIONS["FM:LI"] = \
        lambda: noconversion("FM", "LI")
    CONVERSIONS["FM:LY"] = \
        lambda: noconversion("FM", "LY")
    CONVERSIONS["FM:M"] = \
        lambda value: (10 ** -15) * value
    CONVERSIONS["FM:MEGAM"] = \
        lambda value: (10 ** -21) * value
    CONVERSIONS["FM:MI"] = \
        lambda: noconversion("FM", "MI")
    CONVERSIONS["FM:MICROM"] = \
        lambda value: (10 ** -9) * value
    CONVERSIONS["FM:MM"] = \
        lambda value: (10 ** -12) * value
    CONVERSIONS["FM:NM"] = \
        lambda value: (10 ** -6) * value
    CONVERSIONS["FM:PC"] = \
        lambda: noconversion("FM", "PC")
    CONVERSIONS["FM:PETAM"] = \
        lambda value: (10 ** -30) * value
    CONVERSIONS["FM:PIXEL"] = \
        lambda: noconversion("FM", "PIXEL")
    CONVERSIONS["FM:PM"] = \
        lambda value: (10 ** -3) * value
    CONVERSIONS["FM:PT"] = \
        lambda: noconversion("FM", "PT")
    CONVERSIONS["FM:REFERENCEFRAME"] = \
        lambda: noconversion("FM", "REFERENCEFRAME")
    CONVERSIONS["FM:TERAM"] = \
        lambda value: (10 ** -27) * value
    CONVERSIONS["FM:THOU"] = \
        lambda: noconversion("FM", "THOU")
    CONVERSIONS["FM:UA"] = \
        lambda: noconversion("FM", "UA")
    CONVERSIONS["FM:YD"] = \
        lambda: noconversion("FM", "YD")
    CONVERSIONS["FM:YM"] = \
        lambda value: (10 ** 9) * value
    CONVERSIONS["FM:YOTTAM"] = \
        lambda value: (10 ** -39) * value
    CONVERSIONS["FM:ZETTAM"] = \
        lambda value: (10 ** -36) * value
    CONVERSIONS["FM:ZM"] = \
        lambda value: (10 ** 6) * value
    CONVERSIONS["FT:AM"] = \
        lambda: noconversion("FT", "AM")
    CONVERSIONS["FT:ANGSTROM"] = \
        lambda: noconversion("FT", "ANGSTROM")
    CONVERSIONS["FT:CM"] = \
        lambda: noconversion("FT", "CM")
    CONVERSIONS["FT:DAM"] = \
        lambda: noconversion("FT", "DAM")
    CONVERSIONS["FT:DM"] = \
        lambda: noconversion("FT", "DM")
    CONVERSIONS["FT:EXAM"] = \
        lambda: noconversion("FT", "EXAM")
    CONVERSIONS["FT:FM"] = \
        lambda: noconversion("FT", "FM")
    CONVERSIONS["FT:GIGAM"] = \
        lambda: noconversion("FT", "GIGAM")
    CONVERSIONS["FT:HM"] = \
        lambda: noconversion("FT", "HM")
    CONVERSIONS["FT:IN"] = \
        lambda: noconversion("FT", "IN")
    CONVERSIONS["FT:KM"] = \
        lambda: noconversion("FT", "KM")
    CONVERSIONS["FT:LI"] = \
        lambda: noconversion("FT", "LI")
    CONVERSIONS["FT:LY"] = \
        lambda: noconversion("FT", "LY")
    CONVERSIONS["FT:M"] = \
        lambda: noconversion("FT", "M")
    CONVERSIONS["FT:MEGAM"] = \
        lambda: noconversion("FT", "MEGAM")
    CONVERSIONS["FT:MI"] = \
        lambda: noconversion("FT", "MI")
    CONVERSIONS["FT:MICROM"] = \
        lambda: noconversion("FT", "MICROM")
    CONVERSIONS["FT:MM"] = \
        lambda: noconversion("FT", "MM")
    CONVERSIONS["FT:NM"] = \
        lambda: noconversion("FT", "NM")
    CONVERSIONS["FT:PC"] = \
        lambda: noconversion("FT", "PC")
    CONVERSIONS["FT:PETAM"] = \
        lambda: noconversion("FT", "PETAM")
    CONVERSIONS["FT:PIXEL"] = \
        lambda: noconversion("FT", "PIXEL")
    CONVERSIONS["FT:PM"] = \
        lambda: noconversion("FT", "PM")
    CONVERSIONS["FT:PT"] = \
        lambda: noconversion("FT", "PT")
    CONVERSIONS["FT:REFERENCEFRAME"] = \
        lambda: noconversion("FT", "REFERENCEFRAME")
    CONVERSIONS["FT:TERAM"] = \
        lambda: noconversion("FT", "TERAM")
    CONVERSIONS["FT:THOU"] = \
        lambda: noconversion("FT", "THOU")
    CONVERSIONS["FT:UA"] = \
        lambda: noconversion("FT", "UA")
    CONVERSIONS["FT:YD"] = \
        lambda: noconversion("FT", "YD")
    CONVERSIONS["FT:YM"] = \
        lambda: noconversion("FT", "YM")
    CONVERSIONS["FT:YOTTAM"] = \
        lambda: noconversion("FT", "YOTTAM")
    CONVERSIONS["FT:ZETTAM"] = \
        lambda: noconversion("FT", "ZETTAM")
    CONVERSIONS["FT:ZM"] = \
        lambda: noconversion("FT", "ZM")
    CONVERSIONS["GIGAM:AM"] = \
        lambda value: (10 ** 27) * value
    CONVERSIONS["GIGAM:ANGSTROM"] = \
        lambda value: (10 ** 19) * value
    CONVERSIONS["GIGAM:CM"] = \
        lambda value: (10 ** 11) * value
    CONVERSIONS["GIGAM:DAM"] = \
        lambda value: (10 ** 8) * value
    CONVERSIONS["GIGAM:DM"] = \
        lambda value: (10 ** 10) * value
    CONVERSIONS["GIGAM:EXAM"] = \
        lambda value: (10 ** -9) * value
    CONVERSIONS["GIGAM:FM"] = \
        lambda value: (10 ** 24) * value
    CONVERSIONS["GIGAM:FT"] = \
        lambda: noconversion("GIGAM", "FT")
    CONVERSIONS["GIGAM:HM"] = \
        lambda value: (10 ** 7) * value
    CONVERSIONS["GIGAM:IN"] = \
        lambda: noconversion("GIGAM", "IN")
    CONVERSIONS["GIGAM:KM"] = \
        lambda value: (10 ** 6) * value
    CONVERSIONS["GIGAM:LI"] = \
        lambda: noconversion("GIGAM", "LI")
    CONVERSIONS["GIGAM:LY"] = \
        lambda: noconversion("GIGAM", "LY")
    CONVERSIONS["GIGAM:M"] = \
        lambda value: (10 ** 9) * value
    CONVERSIONS["GIGAM:MEGAM"] = \
        lambda value: (10 ** 3) * value
    CONVERSIONS["GIGAM:MI"] = \
        lambda: noconversion("GIGAM", "MI")
    CONVERSIONS["GIGAM:MICROM"] = \
        lambda value: (10 ** 15) * value
    CONVERSIONS["GIGAM:MM"] = \
        lambda value: (10 ** 12) * value
    CONVERSIONS["GIGAM:NM"] = \
        lambda value: (10 ** 18) * value
    CONVERSIONS["GIGAM:PC"] = \
        lambda: noconversion("GIGAM", "PC")
    CONVERSIONS["GIGAM:PETAM"] = \
        lambda value: (10 ** -6) * value
    CONVERSIONS["GIGAM:PIXEL"] = \
        lambda: noconversion("GIGAM", "PIXEL")
    CONVERSIONS["GIGAM:PM"] = \
        lambda value: (10 ** 21) * value
    CONVERSIONS["GIGAM:PT"] = \
        lambda: noconversion("GIGAM", "PT")
    CONVERSIONS["GIGAM:REFERENCEFRAME"] = \
        lambda: noconversion("GIGAM", "REFERENCEFRAME")
    CONVERSIONS["GIGAM:TERAM"] = \
        lambda value: (10 ** -3) * value
    CONVERSIONS["GIGAM:THOU"] = \
        lambda: noconversion("GIGAM", "THOU")
    CONVERSIONS["GIGAM:UA"] = \
        lambda: noconversion("GIGAM", "UA")
    CONVERSIONS["GIGAM:YD"] = \
        lambda: noconversion("GIGAM", "YD")
    CONVERSIONS["GIGAM:YM"] = \
        lambda value: (10 ** 33) * value
    CONVERSIONS["GIGAM:YOTTAM"] = \
        lambda value: (10 ** -15) * value
    CONVERSIONS["GIGAM:ZETTAM"] = \
        lambda value: (10 ** -12) * value
    CONVERSIONS["GIGAM:ZM"] = \
        lambda value: (10 ** 30) * value
    CONVERSIONS["HM:AM"] = \
        lambda value: (10 ** 20) * value
    CONVERSIONS["HM:ANGSTROM"] = \
        lambda value: (10 ** 12) * value
    CONVERSIONS["HM:CM"] = \
        lambda value: (10 ** 4) * value
    CONVERSIONS["HM:DAM"] = \
        lambda value: 10 * value
    CONVERSIONS["HM:DM"] = \
        lambda value: (10 ** 3) * value
    CONVERSIONS["HM:EXAM"] = \
        lambda value: (10 ** -16) * value
    CONVERSIONS["HM:FM"] = \
        lambda value: (10 ** 17) * value
    CONVERSIONS["HM:FT"] = \
        lambda: noconversion("HM", "FT")
    CONVERSIONS["HM:GIGAM"] = \
        lambda value: (10 ** -7) * value
    CONVERSIONS["HM:IN"] = \
        lambda: noconversion("HM", "IN")
    CONVERSIONS["HM:KM"] = \
        lambda value: (10 ** -1) * value
    CONVERSIONS["HM:LI"] = \
        lambda: noconversion("HM", "LI")
    CONVERSIONS["HM:LY"] = \
        lambda: noconversion("HM", "LY")
    CONVERSIONS["HM:M"] = \
        lambda value: (10 ** 2) * value
    CONVERSIONS["HM:MEGAM"] = \
        lambda value: (10 ** -4) * value
    CONVERSIONS["HM:MI"] = \
        lambda: noconversion("HM", "MI")
    CONVERSIONS["HM:MICROM"] = \
        lambda value: (10 ** 8) * value
    CONVERSIONS["HM:MM"] = \
        lambda value: (10 ** 5) * value
    CONVERSIONS["HM:NM"] = \
        lambda value: (10 ** 11) * value
    CONVERSIONS["HM:PC"] = \
        lambda: noconversion("HM", "PC")
    CONVERSIONS["HM:PETAM"] = \
        lambda value: (10 ** -13) * value
    CONVERSIONS["HM:PIXEL"] = \
        lambda: noconversion("HM", "PIXEL")
    CONVERSIONS["HM:PM"] = \
        lambda value: (10 ** 14) * value
    CONVERSIONS["HM:PT"] = \
        lambda: noconversion("HM", "PT")
    CONVERSIONS["HM:REFERENCEFRAME"] = \
        lambda: noconversion("HM", "REFERENCEFRAME")
    CONVERSIONS["HM:TERAM"] = \
        lambda value: (10 ** -10) * value
    CONVERSIONS["HM:THOU"] = \
        lambda: noconversion("HM", "THOU")
    CONVERSIONS["HM:UA"] = \
        lambda: noconversion("HM", "UA")
    CONVERSIONS["HM:YD"] = \
        lambda: noconversion("HM", "YD")
    CONVERSIONS["HM:YM"] = \
        lambda value: (10 ** 26) * value
    CONVERSIONS["HM:YOTTAM"] = \
        lambda value: (10 ** -22) * value
    CONVERSIONS["HM:ZETTAM"] = \
        lambda value: (10 ** -19) * value
    CONVERSIONS["HM:ZM"] = \
        lambda value: (10 ** 23) * value
    CONVERSIONS["IN:AM"] = \
        lambda: noconversion("IN", "AM")
    CONVERSIONS["IN:ANGSTROM"] = \
        lambda: noconversion("IN", "ANGSTROM")
    CONVERSIONS["IN:CM"] = \
        lambda: noconversion("IN", "CM")
    CONVERSIONS["IN:DAM"] = \
        lambda: noconversion("IN", "DAM")
    CONVERSIONS["IN:DM"] = \
        lambda: noconversion("IN", "DM")
    CONVERSIONS["IN:EXAM"] = \
        lambda: noconversion("IN", "EXAM")
    CONVERSIONS["IN:FM"] = \
        lambda: noconversion("IN", "FM")
    CONVERSIONS["IN:FT"] = \
        lambda: noconversion("IN", "FT")
    CONVERSIONS["IN:GIGAM"] = \
        lambda: noconversion("IN", "GIGAM")
    CONVERSIONS["IN:HM"] = \
        lambda: noconversion("IN", "HM")
    CONVERSIONS["IN:KM"] = \
        lambda: noconversion("IN", "KM")
    CONVERSIONS["IN:LI"] = \
        lambda: noconversion("IN", "LI")
    CONVERSIONS["IN:LY"] = \
        lambda: noconversion("IN", "LY")
    CONVERSIONS["IN:M"] = \
        lambda: noconversion("IN", "M")
    CONVERSIONS["IN:MEGAM"] = \
        lambda: noconversion("IN", "MEGAM")
    CONVERSIONS["IN:MI"] = \
        lambda: noconversion("IN", "MI")
    CONVERSIONS["IN:MICROM"] = \
        lambda: noconversion("IN", "MICROM")
    CONVERSIONS["IN:MM"] = \
        lambda: noconversion("IN", "MM")
    CONVERSIONS["IN:NM"] = \
        lambda: noconversion("IN", "NM")
    CONVERSIONS["IN:PC"] = \
        lambda: noconversion("IN", "PC")
    CONVERSIONS["IN:PETAM"] = \
        lambda: noconversion("IN", "PETAM")
    CONVERSIONS["IN:PIXEL"] = \
        lambda: noconversion("IN", "PIXEL")
    CONVERSIONS["IN:PM"] = \
        lambda: noconversion("IN", "PM")
    CONVERSIONS["IN:PT"] = \
        lambda: noconversion("IN", "PT")
    CONVERSIONS["IN:REFERENCEFRAME"] = \
        lambda: noconversion("IN", "REFERENCEFRAME")
    CONVERSIONS["IN:TERAM"] = \
        lambda: noconversion("IN", "TERAM")
    CONVERSIONS["IN:THOU"] = \
        lambda: noconversion("IN", "THOU")
    CONVERSIONS["IN:UA"] = \
        lambda: noconversion("IN", "UA")
    CONVERSIONS["IN:YD"] = \
        lambda: noconversion("IN", "YD")
    CONVERSIONS["IN:YM"] = \
        lambda: noconversion("IN", "YM")
    CONVERSIONS["IN:YOTTAM"] = \
        lambda: noconversion("IN", "YOTTAM")
    CONVERSIONS["IN:ZETTAM"] = \
        lambda: noconversion("IN", "ZETTAM")
    CONVERSIONS["IN:ZM"] = \
        lambda: noconversion("IN", "ZM")
    CONVERSIONS["KM:AM"] = \
        lambda value: (10 ** 21) * value
    CONVERSIONS["KM:ANGSTROM"] = \
        lambda value: (10 ** 13) * value
    CONVERSIONS["KM:CM"] = \
        lambda value: (10 ** 5) * value
    CONVERSIONS["KM:DAM"] = \
        lambda value: (10 ** 2) * value
    CONVERSIONS["KM:DM"] = \
        lambda value: (10 ** 4) * value
    CONVERSIONS["KM:EXAM"] = \
        lambda value: (10 ** -15) * value
    CONVERSIONS["KM:FM"] = \
        lambda value: (10 ** 18) * value
    CONVERSIONS["KM:FT"] = \
        lambda: noconversion("KM", "FT")
    CONVERSIONS["KM:GIGAM"] = \
        lambda value: (10 ** -6) * value
    CONVERSIONS["KM:HM"] = \
        lambda value: 10 * value
    CONVERSIONS["KM:IN"] = \
        lambda: noconversion("KM", "IN")
    CONVERSIONS["KM:LI"] = \
        lambda: noconversion("KM", "LI")
    CONVERSIONS["KM:LY"] = \
        lambda: noconversion("KM", "LY")
    CONVERSIONS["KM:M"] = \
        lambda value: (10 ** 3) * value
    CONVERSIONS["KM:MEGAM"] = \
        lambda value: (10 ** -3) * value
    CONVERSIONS["KM:MI"] = \
        lambda: noconversion("KM", "MI")
    CONVERSIONS["KM:MICROM"] = \
        lambda value: (10 ** 9) * value
    CONVERSIONS["KM:MM"] = \
        lambda value: (10 ** 6) * value
    CONVERSIONS["KM:NM"] = \
        lambda value: (10 ** 12) * value
    CONVERSIONS["KM:PC"] = \
        lambda: noconversion("KM", "PC")
    CONVERSIONS["KM:PETAM"] = \
        lambda value: (10 ** -12) * value
    CONVERSIONS["KM:PIXEL"] = \
        lambda: noconversion("KM", "PIXEL")
    CONVERSIONS["KM:PM"] = \
        lambda value: (10 ** 15) * value
    CONVERSIONS["KM:PT"] = \
        lambda: noconversion("KM", "PT")
    CONVERSIONS["KM:REFERENCEFRAME"] = \
        lambda: noconversion("KM", "REFERENCEFRAME")
    CONVERSIONS["KM:TERAM"] = \
        lambda value: (10 ** -9) * value
    CONVERSIONS["KM:THOU"] = \
        lambda: noconversion("KM", "THOU")
    CONVERSIONS["KM:UA"] = \
        lambda: noconversion("KM", "UA")
    CONVERSIONS["KM:YD"] = \
        lambda: noconversion("KM", "YD")
    CONVERSIONS["KM:YM"] = \
        lambda value: (10 ** 27) * value
    CONVERSIONS["KM:YOTTAM"] = \
        lambda value: (10 ** -21) * value
    CONVERSIONS["KM:ZETTAM"] = \
        lambda value: (10 ** -18) * value
    CONVERSIONS["KM:ZM"] = \
        lambda value: (10 ** 24) * value
    CONVERSIONS["LI:AM"] = \
        lambda: noconversion("LI", "AM")
    CONVERSIONS["LI:ANGSTROM"] = \
        lambda: noconversion("LI", "ANGSTROM")
    CONVERSIONS["LI:CM"] = \
        lambda: noconversion("LI", "CM")
    CONVERSIONS["LI:DAM"] = \
        lambda: noconversion("LI", "DAM")
    CONVERSIONS["LI:DM"] = \
        lambda: noconversion("LI", "DM")
    CONVERSIONS["LI:EXAM"] = \
        lambda: noconversion("LI", "EXAM")
    CONVERSIONS["LI:FM"] = \
        lambda: noconversion("LI", "FM")
    CONVERSIONS["LI:FT"] = \
        lambda: noconversion("LI", "FT")
    CONVERSIONS["LI:GIGAM"] = \
        lambda: noconversion("LI", "GIGAM")
    CONVERSIONS["LI:HM"] = \
        lambda: noconversion("LI", "HM")
    CONVERSIONS["LI:IN"] = \
        lambda: noconversion("LI", "IN")
    CONVERSIONS["LI:KM"] = \
        lambda: noconversion("LI", "KM")
    CONVERSIONS["LI:LY"] = \
        lambda: noconversion("LI", "LY")
    CONVERSIONS["LI:M"] = \
        lambda: noconversion("LI", "M")
    CONVERSIONS["LI:MEGAM"] = \
        lambda: noconversion("LI", "MEGAM")
    CONVERSIONS["LI:MI"] = \
        lambda: noconversion("LI", "MI")
    CONVERSIONS["LI:MICROM"] = \
        lambda: noconversion("LI", "MICROM")
    CONVERSIONS["LI:MM"] = \
        lambda: noconversion("LI", "MM")
    CONVERSIONS["LI:NM"] = \
        lambda: noconversion("LI", "NM")
    CONVERSIONS["LI:PC"] = \
        lambda: noconversion("LI", "PC")
    CONVERSIONS["LI:PETAM"] = \
        lambda: noconversion("LI", "PETAM")
    CONVERSIONS["LI:PIXEL"] = \
        lambda: noconversion("LI", "PIXEL")
    CONVERSIONS["LI:PM"] = \
        lambda: noconversion("LI", "PM")
    CONVERSIONS["LI:PT"] = \
        lambda: noconversion("LI", "PT")
    CONVERSIONS["LI:REFERENCEFRAME"] = \
        lambda: noconversion("LI", "REFERENCEFRAME")
    CONVERSIONS["LI:TERAM"] = \
        lambda: noconversion("LI", "TERAM")
    CONVERSIONS["LI:THOU"] = \
        lambda: noconversion("LI", "THOU")
    CONVERSIONS["LI:UA"] = \
        lambda: noconversion("LI", "UA")
    CONVERSIONS["LI:YD"] = \
        lambda: noconversion("LI", "YD")
    CONVERSIONS["LI:YM"] = \
        lambda: noconversion("LI", "YM")
    CONVERSIONS["LI:YOTTAM"] = \
        lambda: noconversion("LI", "YOTTAM")
    CONVERSIONS["LI:ZETTAM"] = \
        lambda: noconversion("LI", "ZETTAM")
    CONVERSIONS["LI:ZM"] = \
        lambda: noconversion("LI", "ZM")
    CONVERSIONS["LY:AM"] = \
        lambda: noconversion("LY", "AM")
    CONVERSIONS["LY:ANGSTROM"] = \
        lambda: noconversion("LY", "ANGSTROM")
    CONVERSIONS["LY:CM"] = \
        lambda: noconversion("LY", "CM")
    CONVERSIONS["LY:DAM"] = \
        lambda: noconversion("LY", "DAM")
    CONVERSIONS["LY:DM"] = \
        lambda: noconversion("LY", "DM")
    CONVERSIONS["LY:EXAM"] = \
        lambda: noconversion("LY", "EXAM")
    CONVERSIONS["LY:FM"] = \
        lambda: noconversion("LY", "FM")
    CONVERSIONS["LY:FT"] = \
        lambda: noconversion("LY", "FT")
    CONVERSIONS["LY:GIGAM"] = \
        lambda: noconversion("LY", "GIGAM")
    CONVERSIONS["LY:HM"] = \
        lambda: noconversion("LY", "HM")
    CONVERSIONS["LY:IN"] = \
        lambda: noconversion("LY", "IN")
    CONVERSIONS["LY:KM"] = \
        lambda: noconversion("LY", "KM")
    CONVERSIONS["LY:LI"] = \
        lambda: noconversion("LY", "LI")
    CONVERSIONS["LY:M"] = \
        lambda: noconversion("LY", "M")
    CONVERSIONS["LY:MEGAM"] = \
        lambda: noconversion("LY", "MEGAM")
    CONVERSIONS["LY:MI"] = \
        lambda: noconversion("LY", "MI")
    CONVERSIONS["LY:MICROM"] = \
        lambda: noconversion("LY", "MICROM")
    CONVERSIONS["LY:MM"] = \
        lambda: noconversion("LY", "MM")
    CONVERSIONS["LY:NM"] = \
        lambda: noconversion("LY", "NM")
    CONVERSIONS["LY:PC"] = \
        lambda: noconversion("LY", "PC")
    CONVERSIONS["LY:PETAM"] = \
        lambda: noconversion("LY", "PETAM")
    CONVERSIONS["LY:PIXEL"] = \
        lambda: noconversion("LY", "PIXEL")
    CONVERSIONS["LY:PM"] = \
        lambda: noconversion("LY", "PM")
    CONVERSIONS["LY:PT"] = \
        lambda: noconversion("LY", "PT")
    CONVERSIONS["LY:REFERENCEFRAME"] = \
        lambda: noconversion("LY", "REFERENCEFRAME")
    CONVERSIONS["LY:TERAM"] = \
        lambda: noconversion("LY", "TERAM")
    CONVERSIONS["LY:THOU"] = \
        lambda: noconversion("LY", "THOU")
    CONVERSIONS["LY:UA"] = \
        lambda: noconversion("LY", "UA")
    CONVERSIONS["LY:YD"] = \
        lambda: noconversion("LY", "YD")
    CONVERSIONS["LY:YM"] = \
        lambda: noconversion("LY", "YM")
    CONVERSIONS["LY:YOTTAM"] = \
        lambda: noconversion("LY", "YOTTAM")
    CONVERSIONS["LY:ZETTAM"] = \
        lambda: noconversion("LY", "ZETTAM")
    CONVERSIONS["LY:ZM"] = \
        lambda: noconversion("LY", "ZM")
    CONVERSIONS["M:AM"] = \
        lambda value: (10 ** 18) * value
    CONVERSIONS["M:ANGSTROM"] = \
        lambda value: (10 ** 10) * value
    CONVERSIONS["M:CM"] = \
        lambda value: (10 ** 2) * value
    CONVERSIONS["M:DAM"] = \
        lambda value: (10 ** -1) * value
    CONVERSIONS["M:DM"] = \
        lambda value: 10 * value
    CONVERSIONS["M:EXAM"] = \
        lambda value: (10 ** -18) * value
    CONVERSIONS["M:FM"] = \
        lambda value: (10 ** 15) * value
    CONVERSIONS["M:FT"] = \
        lambda: noconversion("M", "FT")
    CONVERSIONS["M:GIGAM"] = \
        lambda value: (10 ** -9) * value
    CONVERSIONS["M:HM"] = \
        lambda value: (10 ** -2) * value
    CONVERSIONS["M:IN"] = \
        lambda: noconversion("M", "IN")
    CONVERSIONS["M:KM"] = \
        lambda value: (10 ** -3) * value
    CONVERSIONS["M:LI"] = \
        lambda: noconversion("M", "LI")
    CONVERSIONS["M:LY"] = \
        lambda: noconversion("M", "LY")
    CONVERSIONS["M:MEGAM"] = \
        lambda value: (10 ** -6) * value
    CONVERSIONS["M:MI"] = \
        lambda: noconversion("M", "MI")
    CONVERSIONS["M:MICROM"] = \
        lambda value: (10 ** 6) * value
    CONVERSIONS["M:MM"] = \
        lambda value: (10 ** 3) * value
    CONVERSIONS["M:NM"] = \
        lambda value: (10 ** 9) * value
    CONVERSIONS["M:PC"] = \
        lambda: noconversion("M", "PC")
    CONVERSIONS["M:PETAM"] = \
        lambda value: (10 ** -15) * value
    CONVERSIONS["M:PIXEL"] = \
        lambda: noconversion("M", "PIXEL")
    CONVERSIONS["M:PM"] = \
        lambda value: (10 ** 12) * value
    CONVERSIONS["M:PT"] = \
        lambda: noconversion("M", "PT")
    CONVERSIONS["M:REFERENCEFRAME"] = \
        lambda: noconversion("M", "REFERENCEFRAME")
    CONVERSIONS["M:TERAM"] = \
        lambda value: (10 ** -12) * value
    CONVERSIONS["M:THOU"] = \
        lambda: noconversion("M", "THOU")
    CONVERSIONS["M:UA"] = \
        lambda: noconversion("M", "UA")
    CONVERSIONS["M:YD"] = \
        lambda: noconversion("M", "YD")
    CONVERSIONS["M:YM"] = \
        lambda value: (10 ** 24) * value
    CONVERSIONS["M:YOTTAM"] = \
        lambda value: (10 ** -24) * value
    CONVERSIONS["M:ZETTAM"] = \
        lambda value: (10 ** -21) * value
    CONVERSIONS["M:ZM"] = \
        lambda value: (10 ** 21) * value
    CONVERSIONS["MEGAM:AM"] = \
        lambda value: (10 ** 24) * value
    CONVERSIONS["MEGAM:ANGSTROM"] = \
        lambda value: (10 ** 16) * value
    CONVERSIONS["MEGAM:CM"] = \
        lambda value: (10 ** 8) * value
    CONVERSIONS["MEGAM:DAM"] = \
        lambda value: (10 ** 5) * value
    CONVERSIONS["MEGAM:DM"] = \
        lambda value: (10 ** 7) * value
    CONVERSIONS["MEGAM:EXAM"] = \
        lambda value: (10 ** -12) * value
    CONVERSIONS["MEGAM:FM"] = \
        lambda value: (10 ** 21) * value
    CONVERSIONS["MEGAM:FT"] = \
        lambda: noconversion("MEGAM", "FT")
    CONVERSIONS["MEGAM:GIGAM"] = \
        lambda value: (10 ** -3) * value
    CONVERSIONS["MEGAM:HM"] = \
        lambda value: (10 ** 4) * value
    CONVERSIONS["MEGAM:IN"] = \
        lambda: noconversion("MEGAM", "IN")
    CONVERSIONS["MEGAM:KM"] = \
        lambda value: (10 ** 3) * value
    CONVERSIONS["MEGAM:LI"] = \
        lambda: noconversion("MEGAM", "LI")
    CONVERSIONS["MEGAM:LY"] = \
        lambda: noconversion("MEGAM", "LY")
    CONVERSIONS["MEGAM:M"] = \
        lambda value: (10 ** 6) * value
    CONVERSIONS["MEGAM:MI"] = \
        lambda: noconversion("MEGAM", "MI")
    CONVERSIONS["MEGAM:MICROM"] = \
        lambda value: (10 ** 12) * value
    CONVERSIONS["MEGAM:MM"] = \
        lambda value: (10 ** 9) * value
    CONVERSIONS["MEGAM:NM"] = \
        lambda value: (10 ** 15) * value
    CONVERSIONS["MEGAM:PC"] = \
        lambda: noconversion("MEGAM", "PC")
    CONVERSIONS["MEGAM:PETAM"] = \
        lambda value: (10 ** -9) * value
    CONVERSIONS["MEGAM:PIXEL"] = \
        lambda: noconversion("MEGAM", "PIXEL")
    CONVERSIONS["MEGAM:PM"] = \
        lambda value: (10 ** 18) * value
    CONVERSIONS["MEGAM:PT"] = \
        lambda: noconversion("MEGAM", "PT")
    CONVERSIONS["MEGAM:REFERENCEFRAME"] = \
        lambda: noconversion("MEGAM", "REFERENCEFRAME")
    CONVERSIONS["MEGAM:TERAM"] = \
        lambda value: (10 ** -6) * value
    CONVERSIONS["MEGAM:THOU"] = \
        lambda: noconversion("MEGAM", "THOU")
    CONVERSIONS["MEGAM:UA"] = \
        lambda: noconversion("MEGAM", "UA")
    CONVERSIONS["MEGAM:YD"] = \
        lambda: noconversion("MEGAM", "YD")
    CONVERSIONS["MEGAM:YM"] = \
        lambda value: (10 ** 30) * value
    CONVERSIONS["MEGAM:YOTTAM"] = \
        lambda value: (10 ** -18) * value
    CONVERSIONS["MEGAM:ZETTAM"] = \
        lambda value: (10 ** -15) * value
    CONVERSIONS["MEGAM:ZM"] = \
        lambda value: (10 ** 27) * value
    CONVERSIONS["MI:AM"] = \
        lambda: noconversion("MI", "AM")
    CONVERSIONS["MI:ANGSTROM"] = \
        lambda: noconversion("MI", "ANGSTROM")
    CONVERSIONS["MI:CM"] = \
        lambda: noconversion("MI", "CM")
    CONVERSIONS["MI:DAM"] = \
        lambda: noconversion("MI", "DAM")
    CONVERSIONS["MI:DM"] = \
        lambda: noconversion("MI", "DM")
    CONVERSIONS["MI:EXAM"] = \
        lambda: noconversion("MI", "EXAM")
    CONVERSIONS["MI:FM"] = \
        lambda: noconversion("MI", "FM")
    CONVERSIONS["MI:FT"] = \
        lambda: noconversion("MI", "FT")
    CONVERSIONS["MI:GIGAM"] = \
        lambda: noconversion("MI", "GIGAM")
    CONVERSIONS["MI:HM"] = \
        lambda: noconversion("MI", "HM")
    CONVERSIONS["MI:IN"] = \
        lambda: noconversion("MI", "IN")
    CONVERSIONS["MI:KM"] = \
        lambda: noconversion("MI", "KM")
    CONVERSIONS["MI:LI"] = \
        lambda: noconversion("MI", "LI")
    CONVERSIONS["MI:LY"] = \
        lambda: noconversion("MI", "LY")
    CONVERSIONS["MI:M"] = \
        lambda: noconversion("MI", "M")
    CONVERSIONS["MI:MEGAM"] = \
        lambda: noconversion("MI", "MEGAM")
    CONVERSIONS["MI:MICROM"] = \
        lambda: noconversion("MI", "MICROM")
    CONVERSIONS["MI:MM"] = \
        lambda: noconversion("MI", "MM")
    CONVERSIONS["MI:NM"] = \
        lambda: noconversion("MI", "NM")
    CONVERSIONS["MI:PC"] = \
        lambda: noconversion("MI", "PC")
    CONVERSIONS["MI:PETAM"] = \
        lambda: noconversion("MI", "PETAM")
    CONVERSIONS["MI:PIXEL"] = \
        lambda: noconversion("MI", "PIXEL")
    CONVERSIONS["MI:PM"] = \
        lambda: noconversion("MI", "PM")
    CONVERSIONS["MI:PT"] = \
        lambda: noconversion("MI", "PT")
    CONVERSIONS["MI:REFERENCEFRAME"] = \
        lambda: noconversion("MI", "REFERENCEFRAME")
    CONVERSIONS["MI:TERAM"] = \
        lambda: noconversion("MI", "TERAM")
    CONVERSIONS["MI:THOU"] = \
        lambda: noconversion("MI", "THOU")
    CONVERSIONS["MI:UA"] = \
        lambda: noconversion("MI", "UA")
    CONVERSIONS["MI:YD"] = \
        lambda: noconversion("MI", "YD")
    CONVERSIONS["MI:YM"] = \
        lambda: noconversion("MI", "YM")
    CONVERSIONS["MI:YOTTAM"] = \
        lambda: noconversion("MI", "YOTTAM")
    CONVERSIONS["MI:ZETTAM"] = \
        lambda: noconversion("MI", "ZETTAM")
    CONVERSIONS["MI:ZM"] = \
        lambda: noconversion("MI", "ZM")
    CONVERSIONS["MICROM:AM"] = \
        lambda value: (10 ** 12) * value
    CONVERSIONS["MICROM:ANGSTROM"] = \
        lambda value: (10 ** 4) * value
    CONVERSIONS["MICROM:CM"] = \
        lambda value: (10 ** -4) * value
    CONVERSIONS["MICROM:DAM"] = \
        lambda value: (10 ** -7) * value
    CONVERSIONS["MICROM:DM"] = \
        lambda value: (10 ** -5) * value
    CONVERSIONS["MICROM:EXAM"] = \
        lambda value: (10 ** -24) * value
    CONVERSIONS["MICROM:FM"] = \
        lambda value: (10 ** 9) * value
    CONVERSIONS["MICROM:FT"] = \
        lambda: noconversion("MICROM", "FT")
    CONVERSIONS["MICROM:GIGAM"] = \
        lambda value: (10 ** -15) * value
    CONVERSIONS["MICROM:HM"] = \
        lambda value: (10 ** -8) * value
    CONVERSIONS["MICROM:IN"] = \
        lambda: noconversion("MICROM", "IN")
    CONVERSIONS["MICROM:KM"] = \
        lambda value: (10 ** -9) * value
    CONVERSIONS["MICROM:LI"] = \
        lambda: noconversion("MICROM", "LI")
    CONVERSIONS["MICROM:LY"] = \
        lambda: noconversion("MICROM", "LY")
    CONVERSIONS["MICROM:M"] = \
        lambda value: (10 ** -6) * value
    CONVERSIONS["MICROM:MEGAM"] = \
        lambda value: (10 ** -12) * value
    CONVERSIONS["MICROM:MI"] = \
        lambda: noconversion("MICROM", "MI")
    CONVERSIONS["MICROM:MM"] = \
        lambda value: (10 ** -3) * value
    CONVERSIONS["MICROM:NM"] = \
        lambda value: (10 ** 3) * value
    CONVERSIONS["MICROM:PC"] = \
        lambda: noconversion("MICROM", "PC")
    CONVERSIONS["MICROM:PETAM"] = \
        lambda value: (10 ** -21) * value
    CONVERSIONS["MICROM:PIXEL"] = \
        lambda: noconversion("MICROM", "PIXEL")
    CONVERSIONS["MICROM:PM"] = \
        lambda value: (10 ** 6) * value
    CONVERSIONS["MICROM:PT"] = \
        lambda: noconversion("MICROM", "PT")
    CONVERSIONS["MICROM:REFERENCEFRAME"] = \
        lambda: noconversion("MICROM", "REFERENCEFRAME")
    CONVERSIONS["MICROM:TERAM"] = \
        lambda value: (10 ** -18) * value
    CONVERSIONS["MICROM:THOU"] = \
        lambda: noconversion("MICROM", "THOU")
    CONVERSIONS["MICROM:UA"] = \
        lambda: noconversion("MICROM", "UA")
    CONVERSIONS["MICROM:YD"] = \
        lambda: noconversion("MICROM", "YD")
    CONVERSIONS["MICROM:YM"] = \
        lambda value: (10 ** 18) * value
    CONVERSIONS["MICROM:YOTTAM"] = \
        lambda value: (10 ** -30) * value
    CONVERSIONS["MICROM:ZETTAM"] = \
        lambda value: (10 ** -27) * value
    CONVERSIONS["MICROM:ZM"] = \
        lambda value: (10 ** 15) * value
    CONVERSIONS["MM:AM"] = \
        lambda value: (10 ** 15) * value
    CONVERSIONS["MM:ANGSTROM"] = \
        lambda value: (10 ** 7) * value
    CONVERSIONS["MM:CM"] = \
        lambda value: (10 ** -1) * value
    CONVERSIONS["MM:DAM"] = \
        lambda value: (10 ** -4) * value
    CONVERSIONS["MM:DM"] = \
        lambda value: (10 ** -2) * value
    CONVERSIONS["MM:EXAM"] = \
        lambda value: (10 ** -21) * value
    CONVERSIONS["MM:FM"] = \
        lambda value: (10 ** 12) * value
    CONVERSIONS["MM:FT"] = \
        lambda: noconversion("MM", "FT")
    CONVERSIONS["MM:GIGAM"] = \
        lambda value: (10 ** -12) * value
    CONVERSIONS["MM:HM"] = \
        lambda value: (10 ** -5) * value
    CONVERSIONS["MM:IN"] = \
        lambda: noconversion("MM", "IN")
    CONVERSIONS["MM:KM"] = \
        lambda value: (10 ** -6) * value
    CONVERSIONS["MM:LI"] = \
        lambda: noconversion("MM", "LI")
    CONVERSIONS["MM:LY"] = \
        lambda: noconversion("MM", "LY")
    CONVERSIONS["MM:M"] = \
        lambda value: (10 ** -3) * value
    CONVERSIONS["MM:MEGAM"] = \
        lambda value: (10 ** -9) * value
    CONVERSIONS["MM:MI"] = \
        lambda: noconversion("MM", "MI")
    CONVERSIONS["MM:MICROM"] = \
        lambda value: (10 ** 3) * value
    CONVERSIONS["MM:NM"] = \
        lambda value: (10 ** 6) * value
    CONVERSIONS["MM:PC"] = \
        lambda: noconversion("MM", "PC")
    CONVERSIONS["MM:PETAM"] = \
        lambda value: (10 ** -18) * value
    CONVERSIONS["MM:PIXEL"] = \
        lambda: noconversion("MM", "PIXEL")
    CONVERSIONS["MM:PM"] = \
        lambda value: (10 ** 9) * value
    CONVERSIONS["MM:PT"] = \
        lambda: noconversion("MM", "PT")
    CONVERSIONS["MM:REFERENCEFRAME"] = \
        lambda: noconversion("MM", "REFERENCEFRAME")
    CONVERSIONS["MM:TERAM"] = \
        lambda value: (10 ** -15) * value
    CONVERSIONS["MM:THOU"] = \
        lambda: noconversion("MM", "THOU")
    CONVERSIONS["MM:UA"] = \
        lambda: noconversion("MM", "UA")
    CONVERSIONS["MM:YD"] = \
        lambda: noconversion("MM", "YD")
    CONVERSIONS["MM:YM"] = \
        lambda value: (10 ** 21) * value
    CONVERSIONS["MM:YOTTAM"] = \
        lambda value: (10 ** -27) * value
    CONVERSIONS["MM:ZETTAM"] = \
        lambda value: (10 ** -24) * value
    CONVERSIONS["MM:ZM"] = \
        lambda value: (10 ** 18) * value
    CONVERSIONS["NM:AM"] = \
        lambda value: (10 ** 9) * value
    CONVERSIONS["NM:ANGSTROM"] = \
        lambda value: 10 * value
    CONVERSIONS["NM:CM"] = \
        lambda value: (10 ** -7) * value
    CONVERSIONS["NM:DAM"] = \
        lambda value: (10 ** -10) * value
    CONVERSIONS["NM:DM"] = \
        lambda value: (10 ** -8) * value
    CONVERSIONS["NM:EXAM"] = \
        lambda value: (10 ** -27) * value
    CONVERSIONS["NM:FM"] = \
        lambda value: (10 ** 6) * value
    CONVERSIONS["NM:FT"] = \
        lambda: noconversion("NM", "FT")
    CONVERSIONS["NM:GIGAM"] = \
        lambda value: (10 ** -18) * value
    CONVERSIONS["NM:HM"] = \
        lambda value: (10 ** -11) * value
    CONVERSIONS["NM:IN"] = \
        lambda: noconversion("NM", "IN")
    CONVERSIONS["NM:KM"] = \
        lambda value: (10 ** -12) * value
    CONVERSIONS["NM:LI"] = \
        lambda: noconversion("NM", "LI")
    CONVERSIONS["NM:LY"] = \
        lambda: noconversion("NM", "LY")
    CONVERSIONS["NM:M"] = \
        lambda value: (10 ** -9) * value
    CONVERSIONS["NM:MEGAM"] = \
        lambda value: (10 ** -15) * value
    CONVERSIONS["NM:MI"] = \
        lambda: noconversion("NM", "MI")
    CONVERSIONS["NM:MICROM"] = \
        lambda value: (10 ** -3) * value
    CONVERSIONS["NM:MM"] = \
        lambda value: (10 ** -6) * value
    CONVERSIONS["NM:PC"] = \
        lambda: noconversion("NM", "PC")
    CONVERSIONS["NM:PETAM"] = \
        lambda value: (10 ** -24) * value
    CONVERSIONS["NM:PIXEL"] = \
        lambda: noconversion("NM", "PIXEL")
    CONVERSIONS["NM:PM"] = \
        lambda value: (10 ** 3) * value
    CONVERSIONS["NM:PT"] = \
        lambda: noconversion("NM", "PT")
    CONVERSIONS["NM:REFERENCEFRAME"] = \
        lambda: noconversion("NM", "REFERENCEFRAME")
    CONVERSIONS["NM:TERAM"] = \
        lambda value: (10 ** -21) * value
    CONVERSIONS["NM:THOU"] = \
        lambda: noconversion("NM", "THOU")
    CONVERSIONS["NM:UA"] = \
        lambda: noconversion("NM", "UA")
    CONVERSIONS["NM:YD"] = \
        lambda: noconversion("NM", "YD")
    CONVERSIONS["NM:YM"] = \
        lambda value: (10 ** 15) * value
    CONVERSIONS["NM:YOTTAM"] = \
        lambda value: (10 ** -33) * value
    CONVERSIONS["NM:ZETTAM"] = \
        lambda value: (10 ** -30) * value
    CONVERSIONS["NM:ZM"] = \
        lambda value: (10 ** 12) * value
    CONVERSIONS["PC:AM"] = \
        lambda: noconversion("PC", "AM")
    CONVERSIONS["PC:ANGSTROM"] = \
        lambda: noconversion("PC", "ANGSTROM")
    CONVERSIONS["PC:CM"] = \
        lambda: noconversion("PC", "CM")
    CONVERSIONS["PC:DAM"] = \
        lambda: noconversion("PC", "DAM")
    CONVERSIONS["PC:DM"] = \
        lambda: noconversion("PC", "DM")
    CONVERSIONS["PC:EXAM"] = \
        lambda: noconversion("PC", "EXAM")
    CONVERSIONS["PC:FM"] = \
        lambda: noconversion("PC", "FM")
    CONVERSIONS["PC:FT"] = \
        lambda: noconversion("PC", "FT")
    CONVERSIONS["PC:GIGAM"] = \
        lambda: noconversion("PC", "GIGAM")
    CONVERSIONS["PC:HM"] = \
        lambda: noconversion("PC", "HM")
    CONVERSIONS["PC:IN"] = \
        lambda: noconversion("PC", "IN")
    CONVERSIONS["PC:KM"] = \
        lambda: noconversion("PC", "KM")
    CONVERSIONS["PC:LI"] = \
        lambda: noconversion("PC", "LI")
    CONVERSIONS["PC:LY"] = \
        lambda: noconversion("PC", "LY")
    CONVERSIONS["PC:M"] = \
        lambda: noconversion("PC", "M")
    CONVERSIONS["PC:MEGAM"] = \
        lambda: noconversion("PC", "MEGAM")
    CONVERSIONS["PC:MI"] = \
        lambda: noconversion("PC", "MI")
    CONVERSIONS["PC:MICROM"] = \
        lambda: noconversion("PC", "MICROM")
    CONVERSIONS["PC:MM"] = \
        lambda: noconversion("PC", "MM")
    CONVERSIONS["PC:NM"] = \
        lambda: noconversion("PC", "NM")
    CONVERSIONS["PC:PETAM"] = \
        lambda: noconversion("PC", "PETAM")
    CONVERSIONS["PC:PIXEL"] = \
        lambda: noconversion("PC", "PIXEL")
    CONVERSIONS["PC:PM"] = \
        lambda: noconversion("PC", "PM")
    CONVERSIONS["PC:PT"] = \
        lambda: noconversion("PC", "PT")
    CONVERSIONS["PC:REFERENCEFRAME"] = \
        lambda: noconversion("PC", "REFERENCEFRAME")
    CONVERSIONS["PC:TERAM"] = \
        lambda: noconversion("PC", "TERAM")
    CONVERSIONS["PC:THOU"] = \
        lambda: noconversion("PC", "THOU")
    CONVERSIONS["PC:UA"] = \
        lambda: noconversion("PC", "UA")
    CONVERSIONS["PC:YD"] = \
        lambda: noconversion("PC", "YD")
    CONVERSIONS["PC:YM"] = \
        lambda: noconversion("PC", "YM")
    CONVERSIONS["PC:YOTTAM"] = \
        lambda: noconversion("PC", "YOTTAM")
    CONVERSIONS["PC:ZETTAM"] = \
        lambda: noconversion("PC", "ZETTAM")
    CONVERSIONS["PC:ZM"] = \
        lambda: noconversion("PC", "ZM")
    CONVERSIONS["PETAM:AM"] = \
        lambda value: (10 ** 33) * value
    CONVERSIONS["PETAM:ANGSTROM"] = \
        lambda value: (10 ** 25) * value
    CONVERSIONS["PETAM:CM"] = \
        lambda value: (10 ** 17) * value
    CONVERSIONS["PETAM:DAM"] = \
        lambda value: (10 ** 14) * value
    CONVERSIONS["PETAM:DM"] = \
        lambda value: (10 ** 16) * value
    CONVERSIONS["PETAM:EXAM"] = \
        lambda value: (10 ** -3) * value
    CONVERSIONS["PETAM:FM"] = \
        lambda value: (10 ** 30) * value
    CONVERSIONS["PETAM:FT"] = \
        lambda: noconversion("PETAM", "FT")
    CONVERSIONS["PETAM:GIGAM"] = \
        lambda value: (10 ** 6) * value
    CONVERSIONS["PETAM:HM"] = \
        lambda value: (10 ** 13) * value
    CONVERSIONS["PETAM:IN"] = \
        lambda: noconversion("PETAM", "IN")
    CONVERSIONS["PETAM:KM"] = \
        lambda value: (10 ** 12) * value
    CONVERSIONS["PETAM:LI"] = \
        lambda: noconversion("PETAM", "LI")
    CONVERSIONS["PETAM:LY"] = \
        lambda: noconversion("PETAM", "LY")
    CONVERSIONS["PETAM:M"] = \
        lambda value: (10 ** 15) * value
    CONVERSIONS["PETAM:MEGAM"] = \
        lambda value: (10 ** 9) * value
    CONVERSIONS["PETAM:MI"] = \
        lambda: noconversion("PETAM", "MI")
    CONVERSIONS["PETAM:MICROM"] = \
        lambda value: (10 ** 21) * value
    CONVERSIONS["PETAM:MM"] = \
        lambda value: (10 ** 18) * value
    CONVERSIONS["PETAM:NM"] = \
        lambda value: (10 ** 24) * value
    CONVERSIONS["PETAM:PC"] = \
        lambda: noconversion("PETAM", "PC")
    CONVERSIONS["PETAM:PIXEL"] = \
        lambda: noconversion("PETAM", "PIXEL")
    CONVERSIONS["PETAM:PM"] = \
        lambda value: (10 ** 27) * value
    CONVERSIONS["PETAM:PT"] = \
        lambda: noconversion("PETAM", "PT")
    CONVERSIONS["PETAM:REFERENCEFRAME"] = \
        lambda: noconversion("PETAM", "REFERENCEFRAME")
    CONVERSIONS["PETAM:TERAM"] = \
        lambda value: (10 ** 3) * value
    CONVERSIONS["PETAM:THOU"] = \
        lambda: noconversion("PETAM", "THOU")
    CONVERSIONS["PETAM:UA"] = \
        lambda: noconversion("PETAM", "UA")
    CONVERSIONS["PETAM:YD"] = \
        lambda: noconversion("PETAM", "YD")
    CONVERSIONS["PETAM:YM"] = \
        lambda value: (10 ** 39) * value
    CONVERSIONS["PETAM:YOTTAM"] = \
        lambda value: (10 ** -9) * value
    CONVERSIONS["PETAM:ZETTAM"] = \
        lambda value: (10 ** -6) * value
    CONVERSIONS["PETAM:ZM"] = \
        lambda value: (10 ** 36) * value
    CONVERSIONS["PIXEL:AM"] = \
        lambda: noconversion("PIXEL", "AM")
    CONVERSIONS["PIXEL:ANGSTROM"] = \
        lambda: noconversion("PIXEL", "ANGSTROM")
    CONVERSIONS["PIXEL:CM"] = \
        lambda: noconversion("PIXEL", "CM")
    CONVERSIONS["PIXEL:DAM"] = \
        lambda: noconversion("PIXEL", "DAM")
    CONVERSIONS["PIXEL:DM"] = \
        lambda: noconversion("PIXEL", "DM")
    CONVERSIONS["PIXEL:EXAM"] = \
        lambda: noconversion("PIXEL", "EXAM")
    CONVERSIONS["PIXEL:FM"] = \
        lambda: noconversion("PIXEL", "FM")
    CONVERSIONS["PIXEL:FT"] = \
        lambda: noconversion("PIXEL", "FT")
    CONVERSIONS["PIXEL:GIGAM"] = \
        lambda: noconversion("PIXEL", "GIGAM")
    CONVERSIONS["PIXEL:HM"] = \
        lambda: noconversion("PIXEL", "HM")
    CONVERSIONS["PIXEL:IN"] = \
        lambda: noconversion("PIXEL", "IN")
    CONVERSIONS["PIXEL:KM"] = \
        lambda: noconversion("PIXEL", "KM")
    CONVERSIONS["PIXEL:LI"] = \
        lambda: noconversion("PIXEL", "LI")
    CONVERSIONS["PIXEL:LY"] = \
        lambda: noconversion("PIXEL", "LY")
    CONVERSIONS["PIXEL:M"] = \
        lambda: noconversion("PIXEL", "M")
    CONVERSIONS["PIXEL:MEGAM"] = \
        lambda: noconversion("PIXEL", "MEGAM")
    CONVERSIONS["PIXEL:MI"] = \
        lambda: noconversion("PIXEL", "MI")
    CONVERSIONS["PIXEL:MICROM"] = \
        lambda: noconversion("PIXEL", "MICROM")
    CONVERSIONS["PIXEL:MM"] = \
        lambda: noconversion("PIXEL", "MM")
    CONVERSIONS["PIXEL:NM"] = \
        lambda: noconversion("PIXEL", "NM")
    CONVERSIONS["PIXEL:PC"] = \
        lambda: noconversion("PIXEL", "PC")
    CONVERSIONS["PIXEL:PETAM"] = \
        lambda: noconversion("PIXEL", "PETAM")
    CONVERSIONS["PIXEL:PM"] = \
        lambda: noconversion("PIXEL", "PM")
    CONVERSIONS["PIXEL:PT"] = \
        lambda: noconversion("PIXEL", "PT")
    CONVERSIONS["PIXEL:REFERENCEFRAME"] = \
        lambda: noconversion("PIXEL", "REFERENCEFRAME")
    CONVERSIONS["PIXEL:TERAM"] = \
        lambda: noconversion("PIXEL", "TERAM")
    CONVERSIONS["PIXEL:THOU"] = \
        lambda: noconversion("PIXEL", "THOU")
    CONVERSIONS["PIXEL:UA"] = \
        lambda: noconversion("PIXEL", "UA")
    CONVERSIONS["PIXEL:YD"] = \
        lambda: noconversion("PIXEL", "YD")
    CONVERSIONS["PIXEL:YM"] = \
        lambda: noconversion("PIXEL", "YM")
    CONVERSIONS["PIXEL:YOTTAM"] = \
        lambda: noconversion("PIXEL", "YOTTAM")
    CONVERSIONS["PIXEL:ZETTAM"] = \
        lambda: noconversion("PIXEL", "ZETTAM")
    CONVERSIONS["PIXEL:ZM"] = \
        lambda: noconversion("PIXEL", "ZM")
    CONVERSIONS["PM:AM"] = \
        lambda value: (10 ** 6) * value
    CONVERSIONS["PM:ANGSTROM"] = \
        lambda value: (10 ** -2) * value
    CONVERSIONS["PM:CM"] = \
        lambda value: (10 ** -10) * value
    CONVERSIONS["PM:DAM"] = \
        lambda value: (10 ** -13) * value
    CONVERSIONS["PM:DM"] = \
        lambda value: (10 ** -11) * value
    CONVERSIONS["PM:EXAM"] = \
        lambda value: (10 ** -30) * value
    CONVERSIONS["PM:FM"] = \
        lambda value: (10 ** 3) * value
    CONVERSIONS["PM:FT"] = \
        lambda: noconversion("PM", "FT")
    CONVERSIONS["PM:GIGAM"] = \
        lambda value: (10 ** -21) * value
    CONVERSIONS["PM:HM"] = \
        lambda value: (10 ** -14) * value
    CONVERSIONS["PM:IN"] = \
        lambda: noconversion("PM", "IN")
    CONVERSIONS["PM:KM"] = \
        lambda value: (10 ** -15) * value
    CONVERSIONS["PM:LI"] = \
        lambda: noconversion("PM", "LI")
    CONVERSIONS["PM:LY"] = \
        lambda: noconversion("PM", "LY")
    CONVERSIONS["PM:M"] = \
        lambda value: (10 ** -12) * value
    CONVERSIONS["PM:MEGAM"] = \
        lambda value: (10 ** -18) * value
    CONVERSIONS["PM:MI"] = \
        lambda: noconversion("PM", "MI")
    CONVERSIONS["PM:MICROM"] = \
        lambda value: (10 ** -6) * value
    CONVERSIONS["PM:MM"] = \
        lambda value: (10 ** -9) * value
    CONVERSIONS["PM:NM"] = \
        lambda value: (10 ** -3) * value
    CONVERSIONS["PM:PC"] = \
        lambda: noconversion("PM", "PC")
    CONVERSIONS["PM:PETAM"] = \
        lambda value: (10 ** -27) * value
    CONVERSIONS["PM:PIXEL"] = \
        lambda: noconversion("PM", "PIXEL")
    CONVERSIONS["PM:PT"] = \
        lambda: noconversion("PM", "PT")
    CONVERSIONS["PM:REFERENCEFRAME"] = \
        lambda: noconversion("PM", "REFERENCEFRAME")
    CONVERSIONS["PM:TERAM"] = \
        lambda value: (10 ** -24) * value
    CONVERSIONS["PM:THOU"] = \
        lambda: noconversion("PM", "THOU")
    CONVERSIONS["PM:UA"] = \
        lambda: noconversion("PM", "UA")
    CONVERSIONS["PM:YD"] = \
        lambda: noconversion("PM", "YD")
    CONVERSIONS["PM:YM"] = \
        lambda value: (10 ** 12) * value
    CONVERSIONS["PM:YOTTAM"] = \
        lambda value: (10 ** -36) * value
    CONVERSIONS["PM:ZETTAM"] = \
        lambda value: (10 ** -33) * value
    CONVERSIONS["PM:ZM"] = \
        lambda value: (10 ** 9) * value
    CONVERSIONS["PT:AM"] = \
        lambda: noconversion("PT", "AM")
    CONVERSIONS["PT:ANGSTROM"] = \
        lambda: noconversion("PT", "ANGSTROM")
    CONVERSIONS["PT:CM"] = \
        lambda: noconversion("PT", "CM")
    CONVERSIONS["PT:DAM"] = \
        lambda: noconversion("PT", "DAM")
    CONVERSIONS["PT:DM"] = \
        lambda: noconversion("PT", "DM")
    CONVERSIONS["PT:EXAM"] = \
        lambda: noconversion("PT", "EXAM")
    CONVERSIONS["PT:FM"] = \
        lambda: noconversion("PT", "FM")
    CONVERSIONS["PT:FT"] = \
        lambda: noconversion("PT", "FT")
    CONVERSIONS["PT:GIGAM"] = \
        lambda: noconversion("PT", "GIGAM")
    CONVERSIONS["PT:HM"] = \
        lambda: noconversion("PT", "HM")
    CONVERSIONS["PT:IN"] = \
        lambda: noconversion("PT", "IN")
    CONVERSIONS["PT:KM"] = \
        lambda: noconversion("PT", "KM")
    CONVERSIONS["PT:LI"] = \
        lambda: noconversion("PT", "LI")
    CONVERSIONS["PT:LY"] = \
        lambda: noconversion("PT", "LY")
    CONVERSIONS["PT:M"] = \
        lambda: noconversion("PT", "M")
    CONVERSIONS["PT:MEGAM"] = \
        lambda: noconversion("PT", "MEGAM")
    CONVERSIONS["PT:MI"] = \
        lambda: noconversion("PT", "MI")
    CONVERSIONS["PT:MICROM"] = \
        lambda: noconversion("PT", "MICROM")
    CONVERSIONS["PT:MM"] = \
        lambda: noconversion("PT", "MM")
    CONVERSIONS["PT:NM"] = \
        lambda: noconversion("PT", "NM")
    CONVERSIONS["PT:PC"] = \
        lambda: noconversion("PT", "PC")
    CONVERSIONS["PT:PETAM"] = \
        lambda: noconversion("PT", "PETAM")
    CONVERSIONS["PT:PIXEL"] = \
        lambda: noconversion("PT", "PIXEL")
    CONVERSIONS["PT:PM"] = \
        lambda: noconversion("PT", "PM")
    CONVERSIONS["PT:REFERENCEFRAME"] = \
        lambda: noconversion("PT", "REFERENCEFRAME")
    CONVERSIONS["PT:TERAM"] = \
        lambda: noconversion("PT", "TERAM")
    CONVERSIONS["PT:THOU"] = \
        lambda: noconversion("PT", "THOU")
    CONVERSIONS["PT:UA"] = \
        lambda: noconversion("PT", "UA")
    CONVERSIONS["PT:YD"] = \
        lambda: noconversion("PT", "YD")
    CONVERSIONS["PT:YM"] = \
        lambda: noconversion("PT", "YM")
    CONVERSIONS["PT:YOTTAM"] = \
        lambda: noconversion("PT", "YOTTAM")
    CONVERSIONS["PT:ZETTAM"] = \
        lambda: noconversion("PT", "ZETTAM")
    CONVERSIONS["PT:ZM"] = \
        lambda: noconversion("PT", "ZM")
    CONVERSIONS["REFERENCEFRAME:AM"] = \
        lambda: noconversion("REFERENCEFRAME", "AM")
    CONVERSIONS["REFERENCEFRAME:ANGSTROM"] = \
        lambda: noconversion("REFERENCEFRAME", "ANGSTROM")
    CONVERSIONS["REFERENCEFRAME:CM"] = \
        lambda: noconversion("REFERENCEFRAME", "CM")
    CONVERSIONS["REFERENCEFRAME:DAM"] = \
        lambda: noconversion("REFERENCEFRAME", "DAM")
    CONVERSIONS["REFERENCEFRAME:DM"] = \
        lambda: noconversion("REFERENCEFRAME", "DM")
    CONVERSIONS["REFERENCEFRAME:EXAM"] = \
        lambda: noconversion("REFERENCEFRAME", "EXAM")
    CONVERSIONS["REFERENCEFRAME:FM"] = \
        lambda: noconversion("REFERENCEFRAME", "FM")
    CONVERSIONS["REFERENCEFRAME:FT"] = \
        lambda: noconversion("REFERENCEFRAME", "FT")
    CONVERSIONS["REFERENCEFRAME:GIGAM"] = \
        lambda: noconversion("REFERENCEFRAME", "GIGAM")
    CONVERSIONS["REFERENCEFRAME:HM"] = \
        lambda: noconversion("REFERENCEFRAME", "HM")
    CONVERSIONS["REFERENCEFRAME:IN"] = \
        lambda: noconversion("REFERENCEFRAME", "IN")
    CONVERSIONS["REFERENCEFRAME:KM"] = \
        lambda: noconversion("REFERENCEFRAME", "KM")
    CONVERSIONS["REFERENCEFRAME:LI"] = \
        lambda: noconversion("REFERENCEFRAME", "LI")
    CONVERSIONS["REFERENCEFRAME:LY"] = \
        lambda: noconversion("REFERENCEFRAME", "LY")
    CONVERSIONS["REFERENCEFRAME:M"] = \
        lambda: noconversion("REFERENCEFRAME", "M")
    CONVERSIONS["REFERENCEFRAME:MEGAM"] = \
        lambda: noconversion("REFERENCEFRAME", "MEGAM")
    CONVERSIONS["REFERENCEFRAME:MI"] = \
        lambda: noconversion("REFERENCEFRAME", "MI")
    CONVERSIONS["REFERENCEFRAME:MICROM"] = \
        lambda: noconversion("REFERENCEFRAME", "MICROM")
    CONVERSIONS["REFERENCEFRAME:MM"] = \
        lambda: noconversion("REFERENCEFRAME", "MM")
    CONVERSIONS["REFERENCEFRAME:NM"] = \
        lambda: noconversion("REFERENCEFRAME", "NM")
    CONVERSIONS["REFERENCEFRAME:PC"] = \
        lambda: noconversion("REFERENCEFRAME", "PC")
    CONVERSIONS["REFERENCEFRAME:PETAM"] = \
        lambda: noconversion("REFERENCEFRAME", "PETAM")
    CONVERSIONS["REFERENCEFRAME:PIXEL"] = \
        lambda: noconversion("REFERENCEFRAME", "PIXEL")
    CONVERSIONS["REFERENCEFRAME:PM"] = \
        lambda: noconversion("REFERENCEFRAME", "PM")
    CONVERSIONS["REFERENCEFRAME:PT"] = \
        lambda: noconversion("REFERENCEFRAME", "PT")
    CONVERSIONS["REFERENCEFRAME:TERAM"] = \
        lambda: noconversion("REFERENCEFRAME", "TERAM")
    CONVERSIONS["REFERENCEFRAME:THOU"] = \
        lambda: noconversion("REFERENCEFRAME", "THOU")
    CONVERSIONS["REFERENCEFRAME:UA"] = \
        lambda: noconversion("REFERENCEFRAME", "UA")
    CONVERSIONS["REFERENCEFRAME:YD"] = \
        lambda: noconversion("REFERENCEFRAME", "YD")
    CONVERSIONS["REFERENCEFRAME:YM"] = \
        lambda: noconversion("REFERENCEFRAME", "YM")
    CONVERSIONS["REFERENCEFRAME:YOTTAM"] = \
        lambda: noconversion("REFERENCEFRAME", "YOTTAM")
    CONVERSIONS["REFERENCEFRAME:ZETTAM"] = \
        lambda: noconversion("REFERENCEFRAME", "ZETTAM")
    CONVERSIONS["REFERENCEFRAME:ZM"] = \
        lambda: noconversion("REFERENCEFRAME", "ZM")
    CONVERSIONS["TERAM:AM"] = \
        lambda value: (10 ** 30) * value
    CONVERSIONS["TERAM:ANGSTROM"] = \
        lambda value: (10 ** 22) * value
    CONVERSIONS["TERAM:CM"] = \
        lambda value: (10 ** 14) * value
    CONVERSIONS["TERAM:DAM"] = \
        lambda value: (10 ** 11) * value
    CONVERSIONS["TERAM:DM"] = \
        lambda value: (10 ** 13) * value
    CONVERSIONS["TERAM:EXAM"] = \
        lambda value: (10 ** -6) * value
    CONVERSIONS["TERAM:FM"] = \
        lambda value: (10 ** 27) * value
    CONVERSIONS["TERAM:FT"] = \
        lambda: noconversion("TERAM", "FT")
    CONVERSIONS["TERAM:GIGAM"] = \
        lambda value: (10 ** 3) * value
    CONVERSIONS["TERAM:HM"] = \
        lambda value: (10 ** 10) * value
    CONVERSIONS["TERAM:IN"] = \
        lambda: noconversion("TERAM", "IN")
    CONVERSIONS["TERAM:KM"] = \
        lambda value: (10 ** 9) * value
    CONVERSIONS["TERAM:LI"] = \
        lambda: noconversion("TERAM", "LI")
    CONVERSIONS["TERAM:LY"] = \
        lambda: noconversion("TERAM", "LY")
    CONVERSIONS["TERAM:M"] = \
        lambda value: (10 ** 12) * value
    CONVERSIONS["TERAM:MEGAM"] = \
        lambda value: (10 ** 6) * value
    CONVERSIONS["TERAM:MI"] = \
        lambda: noconversion("TERAM", "MI")
    CONVERSIONS["TERAM:MICROM"] = \
        lambda value: (10 ** 18) * value
    CONVERSIONS["TERAM:MM"] = \
        lambda value: (10 ** 15) * value
    CONVERSIONS["TERAM:NM"] = \
        lambda value: (10 ** 21) * value
    CONVERSIONS["TERAM:PC"] = \
        lambda: noconversion("TERAM", "PC")
    CONVERSIONS["TERAM:PETAM"] = \
        lambda value: (10 ** -3) * value
    CONVERSIONS["TERAM:PIXEL"] = \
        lambda: noconversion("TERAM", "PIXEL")
    CONVERSIONS["TERAM:PM"] = \
        lambda value: (10 ** 24) * value
    CONVERSIONS["TERAM:PT"] = \
        lambda: noconversion("TERAM", "PT")
    CONVERSIONS["TERAM:REFERENCEFRAME"] = \
        lambda: noconversion("TERAM", "REFERENCEFRAME")
    CONVERSIONS["TERAM:THOU"] = \
        lambda: noconversion("TERAM", "THOU")
    CONVERSIONS["TERAM:UA"] = \
        lambda: noconversion("TERAM", "UA")
    CONVERSIONS["TERAM:YD"] = \
        lambda: noconversion("TERAM", "YD")
    CONVERSIONS["TERAM:YM"] = \
        lambda value: (10 ** 36) * value
    CONVERSIONS["TERAM:YOTTAM"] = \
        lambda value: (10 ** -12) * value
    CONVERSIONS["TERAM:ZETTAM"] = \
        lambda value: (10 ** -9) * value
    CONVERSIONS["TERAM:ZM"] = \
        lambda value: (10 ** 33) * value
    CONVERSIONS["THOU:AM"] = \
        lambda: noconversion("THOU", "AM")
    CONVERSIONS["THOU:ANGSTROM"] = \
        lambda: noconversion("THOU", "ANGSTROM")
    CONVERSIONS["THOU:CM"] = \
        lambda: noconversion("THOU", "CM")
    CONVERSIONS["THOU:DAM"] = \
        lambda: noconversion("THOU", "DAM")
    CONVERSIONS["THOU:DM"] = \
        lambda: noconversion("THOU", "DM")
    CONVERSIONS["THOU:EXAM"] = \
        lambda: noconversion("THOU", "EXAM")
    CONVERSIONS["THOU:FM"] = \
        lambda: noconversion("THOU", "FM")
    CONVERSIONS["THOU:FT"] = \
        lambda: noconversion("THOU", "FT")
    CONVERSIONS["THOU:GIGAM"] = \
        lambda: noconversion("THOU", "GIGAM")
    CONVERSIONS["THOU:HM"] = \
        lambda: noconversion("THOU", "HM")
    CONVERSIONS["THOU:IN"] = \
        lambda: noconversion("THOU", "IN")
    CONVERSIONS["THOU:KM"] = \
        lambda: noconversion("THOU", "KM")
    CONVERSIONS["THOU:LI"] = \
        lambda: noconversion("THOU", "LI")
    CONVERSIONS["THOU:LY"] = \
        lambda: noconversion("THOU", "LY")
    CONVERSIONS["THOU:M"] = \
        lambda: noconversion("THOU", "M")
    CONVERSIONS["THOU:MEGAM"] = \
        lambda: noconversion("THOU", "MEGAM")
    CONVERSIONS["THOU:MI"] = \
        lambda: noconversion("THOU", "MI")
    CONVERSIONS["THOU:MICROM"] = \
        lambda: noconversion("THOU", "MICROM")
    CONVERSIONS["THOU:MM"] = \
        lambda: noconversion("THOU", "MM")
    CONVERSIONS["THOU:NM"] = \
        lambda: noconversion("THOU", "NM")
    CONVERSIONS["THOU:PC"] = \
        lambda: noconversion("THOU", "PC")
    CONVERSIONS["THOU:PETAM"] = \
        lambda: noconversion("THOU", "PETAM")
    CONVERSIONS["THOU:PIXEL"] = \
        lambda: noconversion("THOU", "PIXEL")
    CONVERSIONS["THOU:PM"] = \
        lambda: noconversion("THOU", "PM")
    CONVERSIONS["THOU:PT"] = \
        lambda: noconversion("THOU", "PT")
    CONVERSIONS["THOU:REFERENCEFRAME"] = \
        lambda: noconversion("THOU", "REFERENCEFRAME")
    CONVERSIONS["THOU:TERAM"] = \
        lambda: noconversion("THOU", "TERAM")
    CONVERSIONS["THOU:UA"] = \
        lambda: noconversion("THOU", "UA")
    CONVERSIONS["THOU:YD"] = \
        lambda: noconversion("THOU", "YD")
    CONVERSIONS["THOU:YM"] = \
        lambda: noconversion("THOU", "YM")
    CONVERSIONS["THOU:YOTTAM"] = \
        lambda: noconversion("THOU", "YOTTAM")
    CONVERSIONS["THOU:ZETTAM"] = \
        lambda: noconversion("THOU", "ZETTAM")
    CONVERSIONS["THOU:ZM"] = \
        lambda: noconversion("THOU", "ZM")
    CONVERSIONS["UA:AM"] = \
        lambda: noconversion("UA", "AM")
    CONVERSIONS["UA:ANGSTROM"] = \
        lambda: noconversion("UA", "ANGSTROM")
    CONVERSIONS["UA:CM"] = \
        lambda: noconversion("UA", "CM")
    CONVERSIONS["UA:DAM"] = \
        lambda: noconversion("UA", "DAM")
    CONVERSIONS["UA:DM"] = \
        lambda: noconversion("UA", "DM")
    CONVERSIONS["UA:EXAM"] = \
        lambda: noconversion("UA", "EXAM")
    CONVERSIONS["UA:FM"] = \
        lambda: noconversion("UA", "FM")
    CONVERSIONS["UA:FT"] = \
        lambda: noconversion("UA", "FT")
    CONVERSIONS["UA:GIGAM"] = \
        lambda: noconversion("UA", "GIGAM")
    CONVERSIONS["UA:HM"] = \
        lambda: noconversion("UA", "HM")
    CONVERSIONS["UA:IN"] = \
        lambda: noconversion("UA", "IN")
    CONVERSIONS["UA:KM"] = \
        lambda: noconversion("UA", "KM")
    CONVERSIONS["UA:LI"] = \
        lambda: noconversion("UA", "LI")
    CONVERSIONS["UA:LY"] = \
        lambda: noconversion("UA", "LY")
    CONVERSIONS["UA:M"] = \
        lambda: noconversion("UA", "M")
    CONVERSIONS["UA:MEGAM"] = \
        lambda: noconversion("UA", "MEGAM")
    CONVERSIONS["UA:MI"] = \
        lambda: noconversion("UA", "MI")
    CONVERSIONS["UA:MICROM"] = \
        lambda: noconversion("UA", "MICROM")
    CONVERSIONS["UA:MM"] = \
        lambda: noconversion("UA", "MM")
    CONVERSIONS["UA:NM"] = \
        lambda: noconversion("UA", "NM")
    CONVERSIONS["UA:PC"] = \
        lambda: noconversion("UA", "PC")
    CONVERSIONS["UA:PETAM"] = \
        lambda: noconversion("UA", "PETAM")
    CONVERSIONS["UA:PIXEL"] = \
        lambda: noconversion("UA", "PIXEL")
    CONVERSIONS["UA:PM"] = \
        lambda: noconversion("UA", "PM")
    CONVERSIONS["UA:PT"] = \
        lambda: noconversion("UA", "PT")
    CONVERSIONS["UA:REFERENCEFRAME"] = \
        lambda: noconversion("UA", "REFERENCEFRAME")
    CONVERSIONS["UA:TERAM"] = \
        lambda: noconversion("UA", "TERAM")
    CONVERSIONS["UA:THOU"] = \
        lambda: noconversion("UA", "THOU")
    CONVERSIONS["UA:YD"] = \
        lambda: noconversion("UA", "YD")
    CONVERSIONS["UA:YM"] = \
        lambda: noconversion("UA", "YM")
    CONVERSIONS["UA:YOTTAM"] = \
        lambda: noconversion("UA", "YOTTAM")
    CONVERSIONS["UA:ZETTAM"] = \
        lambda: noconversion("UA", "ZETTAM")
    CONVERSIONS["UA:ZM"] = \
        lambda: noconversion("UA", "ZM")
    CONVERSIONS["YD:AM"] = \
        lambda: noconversion("YD", "AM")
    CONVERSIONS["YD:ANGSTROM"] = \
        lambda: noconversion("YD", "ANGSTROM")
    CONVERSIONS["YD:CM"] = \
        lambda: noconversion("YD", "CM")
    CONVERSIONS["YD:DAM"] = \
        lambda: noconversion("YD", "DAM")
    CONVERSIONS["YD:DM"] = \
        lambda: noconversion("YD", "DM")
    CONVERSIONS["YD:EXAM"] = \
        lambda: noconversion("YD", "EXAM")
    CONVERSIONS["YD:FM"] = \
        lambda: noconversion("YD", "FM")
    CONVERSIONS["YD:FT"] = \
        lambda: noconversion("YD", "FT")
    CONVERSIONS["YD:GIGAM"] = \
        lambda: noconversion("YD", "GIGAM")
    CONVERSIONS["YD:HM"] = \
        lambda: noconversion("YD", "HM")
    CONVERSIONS["YD:IN"] = \
        lambda: noconversion("YD", "IN")
    CONVERSIONS["YD:KM"] = \
        lambda: noconversion("YD", "KM")
    CONVERSIONS["YD:LI"] = \
        lambda: noconversion("YD", "LI")
    CONVERSIONS["YD:LY"] = \
        lambda: noconversion("YD", "LY")
    CONVERSIONS["YD:M"] = \
        lambda: noconversion("YD", "M")
    CONVERSIONS["YD:MEGAM"] = \
        lambda: noconversion("YD", "MEGAM")
    CONVERSIONS["YD:MI"] = \
        lambda: noconversion("YD", "MI")
    CONVERSIONS["YD:MICROM"] = \
        lambda: noconversion("YD", "MICROM")
    CONVERSIONS["YD:MM"] = \
        lambda: noconversion("YD", "MM")
    CONVERSIONS["YD:NM"] = \
        lambda: noconversion("YD", "NM")
    CONVERSIONS["YD:PC"] = \
        lambda: noconversion("YD", "PC")
    CONVERSIONS["YD:PETAM"] = \
        lambda: noconversion("YD", "PETAM")
    CONVERSIONS["YD:PIXEL"] = \
        lambda: noconversion("YD", "PIXEL")
    CONVERSIONS["YD:PM"] = \
        lambda: noconversion("YD", "PM")
    CONVERSIONS["YD:PT"] = \
        lambda: noconversion("YD", "PT")
    CONVERSIONS["YD:REFERENCEFRAME"] = \
        lambda: noconversion("YD", "REFERENCEFRAME")
    CONVERSIONS["YD:TERAM"] = \
        lambda: noconversion("YD", "TERAM")
    CONVERSIONS["YD:THOU"] = \
        lambda: noconversion("YD", "THOU")
    CONVERSIONS["YD:UA"] = \
        lambda: noconversion("YD", "UA")
    CONVERSIONS["YD:YM"] = \
        lambda: noconversion("YD", "YM")
    CONVERSIONS["YD:YOTTAM"] = \
        lambda: noconversion("YD", "YOTTAM")
    CONVERSIONS["YD:ZETTAM"] = \
        lambda: noconversion("YD", "ZETTAM")
    CONVERSIONS["YD:ZM"] = \
        lambda: noconversion("YD", "ZM")
    CONVERSIONS["YM:AM"] = \
        lambda value: (10 ** -6) * value
    CONVERSIONS["YM:ANGSTROM"] = \
        lambda value: (10 ** -14) * value
    CONVERSIONS["YM:CM"] = \
        lambda value: (10 ** -22) * value
    CONVERSIONS["YM:DAM"] = \
        lambda value: (10 ** -25) * value
    CONVERSIONS["YM:DM"] = \
        lambda value: (10 ** -23) * value
    CONVERSIONS["YM:EXAM"] = \
        lambda value: (10 ** -42) * value
    CONVERSIONS["YM:FM"] = \
        lambda value: (10 ** -9) * value
    CONVERSIONS["YM:FT"] = \
        lambda: noconversion("YM", "FT")
    CONVERSIONS["YM:GIGAM"] = \
        lambda value: (10 ** -33) * value
    CONVERSIONS["YM:HM"] = \
        lambda value: (10 ** -26) * value
    CONVERSIONS["YM:IN"] = \
        lambda: noconversion("YM", "IN")
    CONVERSIONS["YM:KM"] = \
        lambda value: (10 ** -27) * value
    CONVERSIONS["YM:LI"] = \
        lambda: noconversion("YM", "LI")
    CONVERSIONS["YM:LY"] = \
        lambda: noconversion("YM", "LY")
    CONVERSIONS["YM:M"] = \
        lambda value: (10 ** -24) * value
    CONVERSIONS["YM:MEGAM"] = \
        lambda value: (10 ** -30) * value
    CONVERSIONS["YM:MI"] = \
        lambda: noconversion("YM", "MI")
    CONVERSIONS["YM:MICROM"] = \
        lambda value: (10 ** -18) * value
    CONVERSIONS["YM:MM"] = \
        lambda value: (10 ** -21) * value
    CONVERSIONS["YM:NM"] = \
        lambda value: (10 ** -15) * value
    CONVERSIONS["YM:PC"] = \
        lambda: noconversion("YM", "PC")
    CONVERSIONS["YM:PETAM"] = \
        lambda value: (10 ** -39) * value
    CONVERSIONS["YM:PIXEL"] = \
        lambda: noconversion("YM", "PIXEL")
    CONVERSIONS["YM:PM"] = \
        lambda value: (10 ** -12) * value
    CONVERSIONS["YM:PT"] = \
        lambda: noconversion("YM", "PT")
    CONVERSIONS["YM:REFERENCEFRAME"] = \
        lambda: noconversion("YM", "REFERENCEFRAME")
    CONVERSIONS["YM:TERAM"] = \
        lambda value: (10 ** -36) * value
    CONVERSIONS["YM:THOU"] = \
        lambda: noconversion("YM", "THOU")
    CONVERSIONS["YM:UA"] = \
        lambda: noconversion("YM", "UA")
    CONVERSIONS["YM:YD"] = \
        lambda: noconversion("YM", "YD")
    CONVERSIONS["YM:YOTTAM"] = \
        lambda value: (10 ** -48) * value
    CONVERSIONS["YM:ZETTAM"] = \
        lambda value: (10 ** -45) * value
    CONVERSIONS["YM:ZM"] = \
        lambda value: (10 ** -3) * value
    CONVERSIONS["YOTTAM:AM"] = \
        lambda value: (10 ** 42) * value
    CONVERSIONS["YOTTAM:ANGSTROM"] = \
        lambda value: (10 ** 34) * value
    CONVERSIONS["YOTTAM:CM"] = \
        lambda value: (10 ** 26) * value
    CONVERSIONS["YOTTAM:DAM"] = \
        lambda value: (10 ** 23) * value
    CONVERSIONS["YOTTAM:DM"] = \
        lambda value: (10 ** 25) * value
    CONVERSIONS["YOTTAM:EXAM"] = \
        lambda value: (10 ** 6) * value
    CONVERSIONS["YOTTAM:FM"] = \
        lambda value: (10 ** 39) * value
    CONVERSIONS["YOTTAM:FT"] = \
        lambda: noconversion("YOTTAM", "FT")
    CONVERSIONS["YOTTAM:GIGAM"] = \
        lambda value: (10 ** 15) * value
    CONVERSIONS["YOTTAM:HM"] = \
        lambda value: (10 ** 22) * value
    CONVERSIONS["YOTTAM:IN"] = \
        lambda: noconversion("YOTTAM", "IN")
    CONVERSIONS["YOTTAM:KM"] = \
        lambda value: (10 ** 21) * value
    CONVERSIONS["YOTTAM:LI"] = \
        lambda: noconversion("YOTTAM", "LI")
    CONVERSIONS["YOTTAM:LY"] = \
        lambda: noconversion("YOTTAM", "LY")
    CONVERSIONS["YOTTAM:M"] = \
        lambda value: (10 ** 24) * value
    CONVERSIONS["YOTTAM:MEGAM"] = \
        lambda value: (10 ** 18) * value
    CONVERSIONS["YOTTAM:MI"] = \
        lambda: noconversion("YOTTAM", "MI")
    CONVERSIONS["YOTTAM:MICROM"] = \
        lambda value: (10 ** 30) * value
    CONVERSIONS["YOTTAM:MM"] = \
        lambda value: (10 ** 27) * value
    CONVERSIONS["YOTTAM:NM"] = \
        lambda value: (10 ** 33) * value
    CONVERSIONS["YOTTAM:PC"] = \
        lambda: noconversion("YOTTAM", "PC")
    CONVERSIONS["YOTTAM:PETAM"] = \
        lambda value: (10 ** 9) * value
    CONVERSIONS["YOTTAM:PIXEL"] = \
        lambda: noconversion("YOTTAM", "PIXEL")
    CONVERSIONS["YOTTAM:PM"] = \
        lambda value: (10 ** 36) * value
    CONVERSIONS["YOTTAM:PT"] = \
        lambda: noconversion("YOTTAM", "PT")
    CONVERSIONS["YOTTAM:REFERENCEFRAME"] = \
        lambda: noconversion("YOTTAM", "REFERENCEFRAME")
    CONVERSIONS["YOTTAM:TERAM"] = \
        lambda value: (10 ** 12) * value
    CONVERSIONS["YOTTAM:THOU"] = \
        lambda: noconversion("YOTTAM", "THOU")
    CONVERSIONS["YOTTAM:UA"] = \
        lambda: noconversion("YOTTAM", "UA")
    CONVERSIONS["YOTTAM:YD"] = \
        lambda: noconversion("YOTTAM", "YD")
    CONVERSIONS["YOTTAM:YM"] = \
        lambda value: (10 ** 48) * value
    CONVERSIONS["YOTTAM:ZETTAM"] = \
        lambda value: (10 ** 3) * value
    CONVERSIONS["YOTTAM:ZM"] = \
        lambda value: (10 ** 45) * value
    CONVERSIONS["ZETTAM:AM"] = \
        lambda value: (10 ** 39) * value
    CONVERSIONS["ZETTAM:ANGSTROM"] = \
        lambda value: (10 ** 31) * value
    CONVERSIONS["ZETTAM:CM"] = \
        lambda value: (10 ** 23) * value
    CONVERSIONS["ZETTAM:DAM"] = \
        lambda value: (10 ** 20) * value
    CONVERSIONS["ZETTAM:DM"] = \
        lambda value: (10 ** 22) * value
    CONVERSIONS["ZETTAM:EXAM"] = \
        lambda value: (10 ** 3) * value
    CONVERSIONS["ZETTAM:FM"] = \
        lambda value: (10 ** 36) * value
    CONVERSIONS["ZETTAM:FT"] = \
        lambda: noconversion("ZETTAM", "FT")
    CONVERSIONS["ZETTAM:GIGAM"] = \
        lambda value: (10 ** 12) * value
    CONVERSIONS["ZETTAM:HM"] = \
        lambda value: (10 ** 19) * value
    CONVERSIONS["ZETTAM:IN"] = \
        lambda: noconversion("ZETTAM", "IN")
    CONVERSIONS["ZETTAM:KM"] = \
        lambda value: (10 ** 18) * value
    CONVERSIONS["ZETTAM:LI"] = \
        lambda: noconversion("ZETTAM", "LI")
    CONVERSIONS["ZETTAM:LY"] = \
        lambda: noconversion("ZETTAM", "LY")
    CONVERSIONS["ZETTAM:M"] = \
        lambda value: (10 ** 21) * value
    CONVERSIONS["ZETTAM:MEGAM"] = \
        lambda value: (10 ** 15) * value
    CONVERSIONS["ZETTAM:MI"] = \
        lambda: noconversion("ZETTAM", "MI")
    CONVERSIONS["ZETTAM:MICROM"] = \
        lambda value: (10 ** 27) * value
    CONVERSIONS["ZETTAM:MM"] = \
        lambda value: (10 ** 24) * value
    CONVERSIONS["ZETTAM:NM"] = \
        lambda value: (10 ** 30) * value
    CONVERSIONS["ZETTAM:PC"] = \
        lambda: noconversion("ZETTAM", "PC")
    CONVERSIONS["ZETTAM:PETAM"] = \
        lambda value: (10 ** 6) * value
    CONVERSIONS["ZETTAM:PIXEL"] = \
        lambda: noconversion("ZETTAM", "PIXEL")
    CONVERSIONS["ZETTAM:PM"] = \
        lambda value: (10 ** 33) * value
    CONVERSIONS["ZETTAM:PT"] = \
        lambda: noconversion("ZETTAM", "PT")
    CONVERSIONS["ZETTAM:REFERENCEFRAME"] = \
        lambda: noconversion("ZETTAM", "REFERENCEFRAME")
    CONVERSIONS["ZETTAM:TERAM"] = \
        lambda value: (10 ** 9) * value
    CONVERSIONS["ZETTAM:THOU"] = \
        lambda: noconversion("ZETTAM", "THOU")
    CONVERSIONS["ZETTAM:UA"] = \
        lambda: noconversion("ZETTAM", "UA")
    CONVERSIONS["ZETTAM:YD"] = \
        lambda: noconversion("ZETTAM", "YD")
    CONVERSIONS["ZETTAM:YM"] = \
        lambda value: (10 ** 45) * value
    CONVERSIONS["ZETTAM:YOTTAM"] = \
        lambda value: (10 ** -3) * value
    CONVERSIONS["ZETTAM:ZM"] = \
        lambda value: (10 ** 42) * value
    CONVERSIONS["ZM:AM"] = \
        lambda value: (10 ** -3) * value
    CONVERSIONS["ZM:ANGSTROM"] = \
        lambda value: (10 ** -11) * value
    CONVERSIONS["ZM:CM"] = \
        lambda value: (10 ** -19) * value
    CONVERSIONS["ZM:DAM"] = \
        lambda value: (10 ** -22) * value
    CONVERSIONS["ZM:DM"] = \
        lambda value: (10 ** -20) * value
    CONVERSIONS["ZM:EXAM"] = \
        lambda value: (10 ** -39) * value
    CONVERSIONS["ZM:FM"] = \
        lambda value: (10 ** -6) * value
    CONVERSIONS["ZM:FT"] = \
        lambda: noconversion("ZM", "FT")
    CONVERSIONS["ZM:GIGAM"] = \
        lambda value: (10 ** -30) * value
    CONVERSIONS["ZM:HM"] = \
        lambda value: (10 ** -23) * value
    CONVERSIONS["ZM:IN"] = \
        lambda: noconversion("ZM", "IN")
    CONVERSIONS["ZM:KM"] = \
        lambda value: (10 ** -24) * value
    CONVERSIONS["ZM:LI"] = \
        lambda: noconversion("ZM", "LI")
    CONVERSIONS["ZM:LY"] = \
        lambda: noconversion("ZM", "LY")
    CONVERSIONS["ZM:M"] = \
        lambda value: (10 ** -21) * value
    CONVERSIONS["ZM:MEGAM"] = \
        lambda value: (10 ** -27) * value
    CONVERSIONS["ZM:MI"] = \
        lambda: noconversion("ZM", "MI")
    CONVERSIONS["ZM:MICROM"] = \
        lambda value: (10 ** -15) * value
    CONVERSIONS["ZM:MM"] = \
        lambda value: (10 ** -18) * value
    CONVERSIONS["ZM:NM"] = \
        lambda value: (10 ** -12) * value
    CONVERSIONS["ZM:PC"] = \
        lambda: noconversion("ZM", "PC")
    CONVERSIONS["ZM:PETAM"] = \
        lambda value: (10 ** -36) * value
    CONVERSIONS["ZM:PIXEL"] = \
        lambda: noconversion("ZM", "PIXEL")
    CONVERSIONS["ZM:PM"] = \
        lambda value: (10 ** -9) * value
    CONVERSIONS["ZM:PT"] = \
        lambda: noconversion("ZM", "PT")
    CONVERSIONS["ZM:REFERENCEFRAME"] = \
        lambda: noconversion("ZM", "REFERENCEFRAME")
    CONVERSIONS["ZM:TERAM"] = \
        lambda value: (10 ** -33) * value
    CONVERSIONS["ZM:THOU"] = \
        lambda: noconversion("ZM", "THOU")
    CONVERSIONS["ZM:UA"] = \
        lambda: noconversion("ZM", "UA")
    CONVERSIONS["ZM:YD"] = \
        lambda: noconversion("ZM", "YD")
    CONVERSIONS["ZM:YM"] = \
        lambda value: (10 ** 3) * value
    CONVERSIONS["ZM:YOTTAM"] = \
        lambda value: (10 ** -45) * value
    CONVERSIONS["ZM:ZETTAM"] = \
        lambda value: (10 ** -42) * value

    def __init__(self, value=None, unit=None):
        _omero_model.Length.__init__(self)
        if isinstance(value, _omero_model.LengthI):
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
                self.setUnit(UnitsLength.valueOf(target))
        else:
            self.setValue(value)
            self.setUnit(unit)

    def getUnit(self, current=None):
        return self._unit

    def getValue(self, current=None):
        return self._value

    def setUnit(self, unit, current=None):
        self._unit = unit

    def setValue(self, value, current=None):
        self._value = value

    def __str__(self):
        return self._base_string(self.getValue(), self.getUnit())

_omero_model.LengthI = LengthI
