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
    CONVERSIONS["APA:ATM"] = \
        lambda: noconversion("APA", "ATM")
    CONVERSIONS["APA:BAR"] = \
        lambda: noconversion("APA", "BAR")
    CONVERSIONS["APA:CBAR"] = \
        lambda: noconversion("APA", "CBAR")
    CONVERSIONS["APA:CPA"] = \
        lambda value: (10 ** -16) * value
    CONVERSIONS["APA:DAPA"] = \
        lambda value: (10 ** -19) * value
    CONVERSIONS["APA:DBAR"] = \
        lambda: noconversion("APA", "DBAR")
    CONVERSIONS["APA:DPA"] = \
        lambda value: (10 ** -17) * value
    CONVERSIONS["APA:EXAPA"] = \
        lambda value: (10 ** -36) * value
    CONVERSIONS["APA:FPA"] = \
        lambda value: (10 ** -3) * value
    CONVERSIONS["APA:GIGAPA"] = \
        lambda value: (10 ** -27) * value
    CONVERSIONS["APA:HPA"] = \
        lambda value: (10 ** -20) * value
    CONVERSIONS["APA:KBAR"] = \
        lambda: noconversion("APA", "KBAR")
    CONVERSIONS["APA:KPA"] = \
        lambda value: (10 ** -21) * value
    CONVERSIONS["APA:MBAR"] = \
        lambda: noconversion("APA", "MBAR")
    CONVERSIONS["APA:MEGABAR"] = \
        lambda: noconversion("APA", "MEGABAR")
    CONVERSIONS["APA:MEGAPA"] = \
        lambda value: (10 ** -24) * value
    CONVERSIONS["APA:MICROPA"] = \
        lambda value: (10 ** -12) * value
    CONVERSIONS["APA:MMHG"] = \
        lambda: noconversion("APA", "MMHG")
    CONVERSIONS["APA:MPA"] = \
        lambda value: (10 ** -15) * value
    CONVERSIONS["APA:MTORR"] = \
        lambda: noconversion("APA", "MTORR")
    CONVERSIONS["APA:NPA"] = \
        lambda value: (10 ** -9) * value
    CONVERSIONS["APA:PA"] = \
        lambda value: (10 ** -18) * value
    CONVERSIONS["APA:PETAPA"] = \
        lambda value: (10 ** -33) * value
    CONVERSIONS["APA:PPA"] = \
        lambda value: (10 ** -6) * value
    CONVERSIONS["APA:PSI"] = \
        lambda: noconversion("APA", "PSI")
    CONVERSIONS["APA:TERAPA"] = \
        lambda value: (10 ** -30) * value
    CONVERSIONS["APA:TORR"] = \
        lambda: noconversion("APA", "TORR")
    CONVERSIONS["APA:YOTTAPA"] = \
        lambda value: (10 ** -42) * value
    CONVERSIONS["APA:YPA"] = \
        lambda value: (10 ** 6) * value
    CONVERSIONS["APA:ZETTAPA"] = \
        lambda value: (10 ** -39) * value
    CONVERSIONS["APA:ZPA"] = \
        lambda value: (10 ** 3) * value
    CONVERSIONS["ATM:APA"] = \
        lambda: noconversion("ATM", "APA")
    CONVERSIONS["ATM:BAR"] = \
        lambda: noconversion("ATM", "BAR")
    CONVERSIONS["ATM:CBAR"] = \
        lambda: noconversion("ATM", "CBAR")
    CONVERSIONS["ATM:CPA"] = \
        lambda: noconversion("ATM", "CPA")
    CONVERSIONS["ATM:DAPA"] = \
        lambda: noconversion("ATM", "DAPA")
    CONVERSIONS["ATM:DBAR"] = \
        lambda: noconversion("ATM", "DBAR")
    CONVERSIONS["ATM:DPA"] = \
        lambda: noconversion("ATM", "DPA")
    CONVERSIONS["ATM:EXAPA"] = \
        lambda: noconversion("ATM", "EXAPA")
    CONVERSIONS["ATM:FPA"] = \
        lambda: noconversion("ATM", "FPA")
    CONVERSIONS["ATM:GIGAPA"] = \
        lambda: noconversion("ATM", "GIGAPA")
    CONVERSIONS["ATM:HPA"] = \
        lambda: noconversion("ATM", "HPA")
    CONVERSIONS["ATM:KBAR"] = \
        lambda: noconversion("ATM", "KBAR")
    CONVERSIONS["ATM:KPA"] = \
        lambda: noconversion("ATM", "KPA")
    CONVERSIONS["ATM:MBAR"] = \
        lambda: noconversion("ATM", "MBAR")
    CONVERSIONS["ATM:MEGABAR"] = \
        lambda: noconversion("ATM", "MEGABAR")
    CONVERSIONS["ATM:MEGAPA"] = \
        lambda: noconversion("ATM", "MEGAPA")
    CONVERSIONS["ATM:MICROPA"] = \
        lambda: noconversion("ATM", "MICROPA")
    CONVERSIONS["ATM:MMHG"] = \
        lambda: noconversion("ATM", "MMHG")
    CONVERSIONS["ATM:MPA"] = \
        lambda: noconversion("ATM", "MPA")
    CONVERSIONS["ATM:MTORR"] = \
        lambda: noconversion("ATM", "MTORR")
    CONVERSIONS["ATM:NPA"] = \
        lambda: noconversion("ATM", "NPA")
    CONVERSIONS["ATM:PA"] = \
        lambda: noconversion("ATM", "PA")
    CONVERSIONS["ATM:PETAPA"] = \
        lambda: noconversion("ATM", "PETAPA")
    CONVERSIONS["ATM:PPA"] = \
        lambda: noconversion("ATM", "PPA")
    CONVERSIONS["ATM:PSI"] = \
        lambda: noconversion("ATM", "PSI")
    CONVERSIONS["ATM:TERAPA"] = \
        lambda: noconversion("ATM", "TERAPA")
    CONVERSIONS["ATM:TORR"] = \
        lambda: noconversion("ATM", "TORR")
    CONVERSIONS["ATM:YOTTAPA"] = \
        lambda: noconversion("ATM", "YOTTAPA")
    CONVERSIONS["ATM:YPA"] = \
        lambda: noconversion("ATM", "YPA")
    CONVERSIONS["ATM:ZETTAPA"] = \
        lambda: noconversion("ATM", "ZETTAPA")
    CONVERSIONS["ATM:ZPA"] = \
        lambda: noconversion("ATM", "ZPA")
    CONVERSIONS["BAR:APA"] = \
        lambda: noconversion("BAR", "APA")
    CONVERSIONS["BAR:ATM"] = \
        lambda: noconversion("BAR", "ATM")
    CONVERSIONS["BAR:CBAR"] = \
        lambda: noconversion("BAR", "CBAR")
    CONVERSIONS["BAR:CPA"] = \
        lambda: noconversion("BAR", "CPA")
    CONVERSIONS["BAR:DAPA"] = \
        lambda: noconversion("BAR", "DAPA")
    CONVERSIONS["BAR:DBAR"] = \
        lambda: noconversion("BAR", "DBAR")
    CONVERSIONS["BAR:DPA"] = \
        lambda: noconversion("BAR", "DPA")
    CONVERSIONS["BAR:EXAPA"] = \
        lambda: noconversion("BAR", "EXAPA")
    CONVERSIONS["BAR:FPA"] = \
        lambda: noconversion("BAR", "FPA")
    CONVERSIONS["BAR:GIGAPA"] = \
        lambda: noconversion("BAR", "GIGAPA")
    CONVERSIONS["BAR:HPA"] = \
        lambda: noconversion("BAR", "HPA")
    CONVERSIONS["BAR:KBAR"] = \
        lambda: noconversion("BAR", "KBAR")
    CONVERSIONS["BAR:KPA"] = \
        lambda: noconversion("BAR", "KPA")
    CONVERSIONS["BAR:MBAR"] = \
        lambda: noconversion("BAR", "MBAR")
    CONVERSIONS["BAR:MEGABAR"] = \
        lambda: noconversion("BAR", "MEGABAR")
    CONVERSIONS["BAR:MEGAPA"] = \
        lambda: noconversion("BAR", "MEGAPA")
    CONVERSIONS["BAR:MICROPA"] = \
        lambda: noconversion("BAR", "MICROPA")
    CONVERSIONS["BAR:MMHG"] = \
        lambda: noconversion("BAR", "MMHG")
    CONVERSIONS["BAR:MPA"] = \
        lambda: noconversion("BAR", "MPA")
    CONVERSIONS["BAR:MTORR"] = \
        lambda: noconversion("BAR", "MTORR")
    CONVERSIONS["BAR:NPA"] = \
        lambda: noconversion("BAR", "NPA")
    CONVERSIONS["BAR:PA"] = \
        lambda: noconversion("BAR", "PA")
    CONVERSIONS["BAR:PETAPA"] = \
        lambda: noconversion("BAR", "PETAPA")
    CONVERSIONS["BAR:PPA"] = \
        lambda: noconversion("BAR", "PPA")
    CONVERSIONS["BAR:PSI"] = \
        lambda: noconversion("BAR", "PSI")
    CONVERSIONS["BAR:TERAPA"] = \
        lambda: noconversion("BAR", "TERAPA")
    CONVERSIONS["BAR:TORR"] = \
        lambda: noconversion("BAR", "TORR")
    CONVERSIONS["BAR:YOTTAPA"] = \
        lambda: noconversion("BAR", "YOTTAPA")
    CONVERSIONS["BAR:YPA"] = \
        lambda: noconversion("BAR", "YPA")
    CONVERSIONS["BAR:ZETTAPA"] = \
        lambda: noconversion("BAR", "ZETTAPA")
    CONVERSIONS["BAR:ZPA"] = \
        lambda: noconversion("BAR", "ZPA")
    CONVERSIONS["CBAR:APA"] = \
        lambda: noconversion("CBAR", "APA")
    CONVERSIONS["CBAR:ATM"] = \
        lambda: noconversion("CBAR", "ATM")
    CONVERSIONS["CBAR:BAR"] = \
        lambda: noconversion("CBAR", "BAR")
    CONVERSIONS["CBAR:CPA"] = \
        lambda: noconversion("CBAR", "CPA")
    CONVERSIONS["CBAR:DAPA"] = \
        lambda: noconversion("CBAR", "DAPA")
    CONVERSIONS["CBAR:DBAR"] = \
        lambda: noconversion("CBAR", "DBAR")
    CONVERSIONS["CBAR:DPA"] = \
        lambda: noconversion("CBAR", "DPA")
    CONVERSIONS["CBAR:EXAPA"] = \
        lambda: noconversion("CBAR", "EXAPA")
    CONVERSIONS["CBAR:FPA"] = \
        lambda: noconversion("CBAR", "FPA")
    CONVERSIONS["CBAR:GIGAPA"] = \
        lambda: noconversion("CBAR", "GIGAPA")
    CONVERSIONS["CBAR:HPA"] = \
        lambda: noconversion("CBAR", "HPA")
    CONVERSIONS["CBAR:KBAR"] = \
        lambda: noconversion("CBAR", "KBAR")
    CONVERSIONS["CBAR:KPA"] = \
        lambda: noconversion("CBAR", "KPA")
    CONVERSIONS["CBAR:MBAR"] = \
        lambda: noconversion("CBAR", "MBAR")
    CONVERSIONS["CBAR:MEGABAR"] = \
        lambda: noconversion("CBAR", "MEGABAR")
    CONVERSIONS["CBAR:MEGAPA"] = \
        lambda: noconversion("CBAR", "MEGAPA")
    CONVERSIONS["CBAR:MICROPA"] = \
        lambda: noconversion("CBAR", "MICROPA")
    CONVERSIONS["CBAR:MMHG"] = \
        lambda: noconversion("CBAR", "MMHG")
    CONVERSIONS["CBAR:MPA"] = \
        lambda: noconversion("CBAR", "MPA")
    CONVERSIONS["CBAR:MTORR"] = \
        lambda: noconversion("CBAR", "MTORR")
    CONVERSIONS["CBAR:NPA"] = \
        lambda: noconversion("CBAR", "NPA")
    CONVERSIONS["CBAR:PA"] = \
        lambda: noconversion("CBAR", "PA")
    CONVERSIONS["CBAR:PETAPA"] = \
        lambda: noconversion("CBAR", "PETAPA")
    CONVERSIONS["CBAR:PPA"] = \
        lambda: noconversion("CBAR", "PPA")
    CONVERSIONS["CBAR:PSI"] = \
        lambda: noconversion("CBAR", "PSI")
    CONVERSIONS["CBAR:TERAPA"] = \
        lambda: noconversion("CBAR", "TERAPA")
    CONVERSIONS["CBAR:TORR"] = \
        lambda: noconversion("CBAR", "TORR")
    CONVERSIONS["CBAR:YOTTAPA"] = \
        lambda: noconversion("CBAR", "YOTTAPA")
    CONVERSIONS["CBAR:YPA"] = \
        lambda: noconversion("CBAR", "YPA")
    CONVERSIONS["CBAR:ZETTAPA"] = \
        lambda: noconversion("CBAR", "ZETTAPA")
    CONVERSIONS["CBAR:ZPA"] = \
        lambda: noconversion("CBAR", "ZPA")
    CONVERSIONS["CPA:APA"] = \
        lambda value: (10 ** 16) * value
    CONVERSIONS["CPA:ATM"] = \
        lambda: noconversion("CPA", "ATM")
    CONVERSIONS["CPA:BAR"] = \
        lambda: noconversion("CPA", "BAR")
    CONVERSIONS["CPA:CBAR"] = \
        lambda: noconversion("CPA", "CBAR")
    CONVERSIONS["CPA:DAPA"] = \
        lambda value: (10 ** -3) * value
    CONVERSIONS["CPA:DBAR"] = \
        lambda: noconversion("CPA", "DBAR")
    CONVERSIONS["CPA:DPA"] = \
        lambda value: (10 ** -1) * value
    CONVERSIONS["CPA:EXAPA"] = \
        lambda value: (10 ** -20) * value
    CONVERSIONS["CPA:FPA"] = \
        lambda value: (10 ** 13) * value
    CONVERSIONS["CPA:GIGAPA"] = \
        lambda value: (10 ** -11) * value
    CONVERSIONS["CPA:HPA"] = \
        lambda value: (10 ** -4) * value
    CONVERSIONS["CPA:KBAR"] = \
        lambda: noconversion("CPA", "KBAR")
    CONVERSIONS["CPA:KPA"] = \
        lambda value: (10 ** -5) * value
    CONVERSIONS["CPA:MBAR"] = \
        lambda: noconversion("CPA", "MBAR")
    CONVERSIONS["CPA:MEGABAR"] = \
        lambda: noconversion("CPA", "MEGABAR")
    CONVERSIONS["CPA:MEGAPA"] = \
        lambda value: (10 ** -8) * value
    CONVERSIONS["CPA:MICROPA"] = \
        lambda value: (10 ** 4) * value
    CONVERSIONS["CPA:MMHG"] = \
        lambda: noconversion("CPA", "MMHG")
    CONVERSIONS["CPA:MPA"] = \
        lambda value: 10 * value
    CONVERSIONS["CPA:MTORR"] = \
        lambda: noconversion("CPA", "MTORR")
    CONVERSIONS["CPA:NPA"] = \
        lambda value: (10 ** 7) * value
    CONVERSIONS["CPA:PA"] = \
        lambda value: (10 ** -2) * value
    CONVERSIONS["CPA:PETAPA"] = \
        lambda value: (10 ** -17) * value
    CONVERSIONS["CPA:PPA"] = \
        lambda value: (10 ** 10) * value
    CONVERSIONS["CPA:PSI"] = \
        lambda: noconversion("CPA", "PSI")
    CONVERSIONS["CPA:TERAPA"] = \
        lambda value: (10 ** -14) * value
    CONVERSIONS["CPA:TORR"] = \
        lambda: noconversion("CPA", "TORR")
    CONVERSIONS["CPA:YOTTAPA"] = \
        lambda value: (10 ** -26) * value
    CONVERSIONS["CPA:YPA"] = \
        lambda value: (10 ** 22) * value
    CONVERSIONS["CPA:ZETTAPA"] = \
        lambda value: (10 ** -23) * value
    CONVERSIONS["CPA:ZPA"] = \
        lambda value: (10 ** 19) * value
    CONVERSIONS["DAPA:APA"] = \
        lambda value: (10 ** 19) * value
    CONVERSIONS["DAPA:ATM"] = \
        lambda: noconversion("DAPA", "ATM")
    CONVERSIONS["DAPA:BAR"] = \
        lambda: noconversion("DAPA", "BAR")
    CONVERSIONS["DAPA:CBAR"] = \
        lambda: noconversion("DAPA", "CBAR")
    CONVERSIONS["DAPA:CPA"] = \
        lambda value: (10 ** 3) * value
    CONVERSIONS["DAPA:DBAR"] = \
        lambda: noconversion("DAPA", "DBAR")
    CONVERSIONS["DAPA:DPA"] = \
        lambda value: (10 ** 2) * value
    CONVERSIONS["DAPA:EXAPA"] = \
        lambda value: (10 ** -17) * value
    CONVERSIONS["DAPA:FPA"] = \
        lambda value: (10 ** 16) * value
    CONVERSIONS["DAPA:GIGAPA"] = \
        lambda value: (10 ** -8) * value
    CONVERSIONS["DAPA:HPA"] = \
        lambda value: (10 ** -1) * value
    CONVERSIONS["DAPA:KBAR"] = \
        lambda: noconversion("DAPA", "KBAR")
    CONVERSIONS["DAPA:KPA"] = \
        lambda value: (10 ** -2) * value
    CONVERSIONS["DAPA:MBAR"] = \
        lambda: noconversion("DAPA", "MBAR")
    CONVERSIONS["DAPA:MEGABAR"] = \
        lambda: noconversion("DAPA", "MEGABAR")
    CONVERSIONS["DAPA:MEGAPA"] = \
        lambda value: (10 ** -5) * value
    CONVERSIONS["DAPA:MICROPA"] = \
        lambda value: (10 ** 7) * value
    CONVERSIONS["DAPA:MMHG"] = \
        lambda: noconversion("DAPA", "MMHG")
    CONVERSIONS["DAPA:MPA"] = \
        lambda value: (10 ** 4) * value
    CONVERSIONS["DAPA:MTORR"] = \
        lambda: noconversion("DAPA", "MTORR")
    CONVERSIONS["DAPA:NPA"] = \
        lambda value: (10 ** 10) * value
    CONVERSIONS["DAPA:PA"] = \
        lambda value: 10 * value
    CONVERSIONS["DAPA:PETAPA"] = \
        lambda value: (10 ** -14) * value
    CONVERSIONS["DAPA:PPA"] = \
        lambda value: (10 ** 13) * value
    CONVERSIONS["DAPA:PSI"] = \
        lambda: noconversion("DAPA", "PSI")
    CONVERSIONS["DAPA:TERAPA"] = \
        lambda value: (10 ** -11) * value
    CONVERSIONS["DAPA:TORR"] = \
        lambda: noconversion("DAPA", "TORR")
    CONVERSIONS["DAPA:YOTTAPA"] = \
        lambda value: (10 ** -23) * value
    CONVERSIONS["DAPA:YPA"] = \
        lambda value: (10 ** 25) * value
    CONVERSIONS["DAPA:ZETTAPA"] = \
        lambda value: (10 ** -20) * value
    CONVERSIONS["DAPA:ZPA"] = \
        lambda value: (10 ** 22) * value
    CONVERSIONS["DBAR:APA"] = \
        lambda: noconversion("DBAR", "APA")
    CONVERSIONS["DBAR:ATM"] = \
        lambda: noconversion("DBAR", "ATM")
    CONVERSIONS["DBAR:BAR"] = \
        lambda: noconversion("DBAR", "BAR")
    CONVERSIONS["DBAR:CBAR"] = \
        lambda: noconversion("DBAR", "CBAR")
    CONVERSIONS["DBAR:CPA"] = \
        lambda: noconversion("DBAR", "CPA")
    CONVERSIONS["DBAR:DAPA"] = \
        lambda: noconversion("DBAR", "DAPA")
    CONVERSIONS["DBAR:DPA"] = \
        lambda: noconversion("DBAR", "DPA")
    CONVERSIONS["DBAR:EXAPA"] = \
        lambda: noconversion("DBAR", "EXAPA")
    CONVERSIONS["DBAR:FPA"] = \
        lambda: noconversion("DBAR", "FPA")
    CONVERSIONS["DBAR:GIGAPA"] = \
        lambda: noconversion("DBAR", "GIGAPA")
    CONVERSIONS["DBAR:HPA"] = \
        lambda: noconversion("DBAR", "HPA")
    CONVERSIONS["DBAR:KBAR"] = \
        lambda: noconversion("DBAR", "KBAR")
    CONVERSIONS["DBAR:KPA"] = \
        lambda: noconversion("DBAR", "KPA")
    CONVERSIONS["DBAR:MBAR"] = \
        lambda: noconversion("DBAR", "MBAR")
    CONVERSIONS["DBAR:MEGABAR"] = \
        lambda: noconversion("DBAR", "MEGABAR")
    CONVERSIONS["DBAR:MEGAPA"] = \
        lambda: noconversion("DBAR", "MEGAPA")
    CONVERSIONS["DBAR:MICROPA"] = \
        lambda: noconversion("DBAR", "MICROPA")
    CONVERSIONS["DBAR:MMHG"] = \
        lambda: noconversion("DBAR", "MMHG")
    CONVERSIONS["DBAR:MPA"] = \
        lambda: noconversion("DBAR", "MPA")
    CONVERSIONS["DBAR:MTORR"] = \
        lambda: noconversion("DBAR", "MTORR")
    CONVERSIONS["DBAR:NPA"] = \
        lambda: noconversion("DBAR", "NPA")
    CONVERSIONS["DBAR:PA"] = \
        lambda: noconversion("DBAR", "PA")
    CONVERSIONS["DBAR:PETAPA"] = \
        lambda: noconversion("DBAR", "PETAPA")
    CONVERSIONS["DBAR:PPA"] = \
        lambda: noconversion("DBAR", "PPA")
    CONVERSIONS["DBAR:PSI"] = \
        lambda: noconversion("DBAR", "PSI")
    CONVERSIONS["DBAR:TERAPA"] = \
        lambda: noconversion("DBAR", "TERAPA")
    CONVERSIONS["DBAR:TORR"] = \
        lambda: noconversion("DBAR", "TORR")
    CONVERSIONS["DBAR:YOTTAPA"] = \
        lambda: noconversion("DBAR", "YOTTAPA")
    CONVERSIONS["DBAR:YPA"] = \
        lambda: noconversion("DBAR", "YPA")
    CONVERSIONS["DBAR:ZETTAPA"] = \
        lambda: noconversion("DBAR", "ZETTAPA")
    CONVERSIONS["DBAR:ZPA"] = \
        lambda: noconversion("DBAR", "ZPA")
    CONVERSIONS["DPA:APA"] = \
        lambda value: (10 ** 17) * value
    CONVERSIONS["DPA:ATM"] = \
        lambda: noconversion("DPA", "ATM")
    CONVERSIONS["DPA:BAR"] = \
        lambda: noconversion("DPA", "BAR")
    CONVERSIONS["DPA:CBAR"] = \
        lambda: noconversion("DPA", "CBAR")
    CONVERSIONS["DPA:CPA"] = \
        lambda value: 10 * value
    CONVERSIONS["DPA:DAPA"] = \
        lambda value: (10 ** -2) * value
    CONVERSIONS["DPA:DBAR"] = \
        lambda: noconversion("DPA", "DBAR")
    CONVERSIONS["DPA:EXAPA"] = \
        lambda value: (10 ** -19) * value
    CONVERSIONS["DPA:FPA"] = \
        lambda value: (10 ** 14) * value
    CONVERSIONS["DPA:GIGAPA"] = \
        lambda value: (10 ** -10) * value
    CONVERSIONS["DPA:HPA"] = \
        lambda value: (10 ** -3) * value
    CONVERSIONS["DPA:KBAR"] = \
        lambda: noconversion("DPA", "KBAR")
    CONVERSIONS["DPA:KPA"] = \
        lambda value: (10 ** -4) * value
    CONVERSIONS["DPA:MBAR"] = \
        lambda: noconversion("DPA", "MBAR")
    CONVERSIONS["DPA:MEGABAR"] = \
        lambda: noconversion("DPA", "MEGABAR")
    CONVERSIONS["DPA:MEGAPA"] = \
        lambda value: (10 ** -7) * value
    CONVERSIONS["DPA:MICROPA"] = \
        lambda value: (10 ** 5) * value
    CONVERSIONS["DPA:MMHG"] = \
        lambda: noconversion("DPA", "MMHG")
    CONVERSIONS["DPA:MPA"] = \
        lambda value: (10 ** 2) * value
    CONVERSIONS["DPA:MTORR"] = \
        lambda: noconversion("DPA", "MTORR")
    CONVERSIONS["DPA:NPA"] = \
        lambda value: (10 ** 8) * value
    CONVERSIONS["DPA:PA"] = \
        lambda value: (10 ** -1) * value
    CONVERSIONS["DPA:PETAPA"] = \
        lambda value: (10 ** -16) * value
    CONVERSIONS["DPA:PPA"] = \
        lambda value: (10 ** 11) * value
    CONVERSIONS["DPA:PSI"] = \
        lambda: noconversion("DPA", "PSI")
    CONVERSIONS["DPA:TERAPA"] = \
        lambda value: (10 ** -13) * value
    CONVERSIONS["DPA:TORR"] = \
        lambda: noconversion("DPA", "TORR")
    CONVERSIONS["DPA:YOTTAPA"] = \
        lambda value: (10 ** -25) * value
    CONVERSIONS["DPA:YPA"] = \
        lambda value: (10 ** 23) * value
    CONVERSIONS["DPA:ZETTAPA"] = \
        lambda value: (10 ** -22) * value
    CONVERSIONS["DPA:ZPA"] = \
        lambda value: (10 ** 20) * value
    CONVERSIONS["EXAPA:APA"] = \
        lambda value: (10 ** 36) * value
    CONVERSIONS["EXAPA:ATM"] = \
        lambda: noconversion("EXAPA", "ATM")
    CONVERSIONS["EXAPA:BAR"] = \
        lambda: noconversion("EXAPA", "BAR")
    CONVERSIONS["EXAPA:CBAR"] = \
        lambda: noconversion("EXAPA", "CBAR")
    CONVERSIONS["EXAPA:CPA"] = \
        lambda value: (10 ** 20) * value
    CONVERSIONS["EXAPA:DAPA"] = \
        lambda value: (10 ** 17) * value
    CONVERSIONS["EXAPA:DBAR"] = \
        lambda: noconversion("EXAPA", "DBAR")
    CONVERSIONS["EXAPA:DPA"] = \
        lambda value: (10 ** 19) * value
    CONVERSIONS["EXAPA:FPA"] = \
        lambda value: (10 ** 33) * value
    CONVERSIONS["EXAPA:GIGAPA"] = \
        lambda value: (10 ** 9) * value
    CONVERSIONS["EXAPA:HPA"] = \
        lambda value: (10 ** 16) * value
    CONVERSIONS["EXAPA:KBAR"] = \
        lambda: noconversion("EXAPA", "KBAR")
    CONVERSIONS["EXAPA:KPA"] = \
        lambda value: (10 ** 15) * value
    CONVERSIONS["EXAPA:MBAR"] = \
        lambda: noconversion("EXAPA", "MBAR")
    CONVERSIONS["EXAPA:MEGABAR"] = \
        lambda: noconversion("EXAPA", "MEGABAR")
    CONVERSIONS["EXAPA:MEGAPA"] = \
        lambda value: (10 ** 12) * value
    CONVERSIONS["EXAPA:MICROPA"] = \
        lambda value: (10 ** 24) * value
    CONVERSIONS["EXAPA:MMHG"] = \
        lambda: noconversion("EXAPA", "MMHG")
    CONVERSIONS["EXAPA:MPA"] = \
        lambda value: (10 ** 21) * value
    CONVERSIONS["EXAPA:MTORR"] = \
        lambda: noconversion("EXAPA", "MTORR")
    CONVERSIONS["EXAPA:NPA"] = \
        lambda value: (10 ** 27) * value
    CONVERSIONS["EXAPA:PA"] = \
        lambda value: (10 ** 18) * value
    CONVERSIONS["EXAPA:PETAPA"] = \
        lambda value: (10 ** 3) * value
    CONVERSIONS["EXAPA:PPA"] = \
        lambda value: (10 ** 30) * value
    CONVERSIONS["EXAPA:PSI"] = \
        lambda: noconversion("EXAPA", "PSI")
    CONVERSIONS["EXAPA:TERAPA"] = \
        lambda value: (10 ** 6) * value
    CONVERSIONS["EXAPA:TORR"] = \
        lambda: noconversion("EXAPA", "TORR")
    CONVERSIONS["EXAPA:YOTTAPA"] = \
        lambda value: (10 ** -6) * value
    CONVERSIONS["EXAPA:YPA"] = \
        lambda value: (10 ** 42) * value
    CONVERSIONS["EXAPA:ZETTAPA"] = \
        lambda value: (10 ** -3) * value
    CONVERSIONS["EXAPA:ZPA"] = \
        lambda value: (10 ** 39) * value
    CONVERSIONS["FPA:APA"] = \
        lambda value: (10 ** 3) * value
    CONVERSIONS["FPA:ATM"] = \
        lambda: noconversion("FPA", "ATM")
    CONVERSIONS["FPA:BAR"] = \
        lambda: noconversion("FPA", "BAR")
    CONVERSIONS["FPA:CBAR"] = \
        lambda: noconversion("FPA", "CBAR")
    CONVERSIONS["FPA:CPA"] = \
        lambda value: (10 ** -13) * value
    CONVERSIONS["FPA:DAPA"] = \
        lambda value: (10 ** -16) * value
    CONVERSIONS["FPA:DBAR"] = \
        lambda: noconversion("FPA", "DBAR")
    CONVERSIONS["FPA:DPA"] = \
        lambda value: (10 ** -14) * value
    CONVERSIONS["FPA:EXAPA"] = \
        lambda value: (10 ** -33) * value
    CONVERSIONS["FPA:GIGAPA"] = \
        lambda value: (10 ** -24) * value
    CONVERSIONS["FPA:HPA"] = \
        lambda value: (10 ** -17) * value
    CONVERSIONS["FPA:KBAR"] = \
        lambda: noconversion("FPA", "KBAR")
    CONVERSIONS["FPA:KPA"] = \
        lambda value: (10 ** -18) * value
    CONVERSIONS["FPA:MBAR"] = \
        lambda: noconversion("FPA", "MBAR")
    CONVERSIONS["FPA:MEGABAR"] = \
        lambda: noconversion("FPA", "MEGABAR")
    CONVERSIONS["FPA:MEGAPA"] = \
        lambda value: (10 ** -21) * value
    CONVERSIONS["FPA:MICROPA"] = \
        lambda value: (10 ** -9) * value
    CONVERSIONS["FPA:MMHG"] = \
        lambda: noconversion("FPA", "MMHG")
    CONVERSIONS["FPA:MPA"] = \
        lambda value: (10 ** -12) * value
    CONVERSIONS["FPA:MTORR"] = \
        lambda: noconversion("FPA", "MTORR")
    CONVERSIONS["FPA:NPA"] = \
        lambda value: (10 ** -6) * value
    CONVERSIONS["FPA:PA"] = \
        lambda value: (10 ** -15) * value
    CONVERSIONS["FPA:PETAPA"] = \
        lambda value: (10 ** -30) * value
    CONVERSIONS["FPA:PPA"] = \
        lambda value: (10 ** -3) * value
    CONVERSIONS["FPA:PSI"] = \
        lambda: noconversion("FPA", "PSI")
    CONVERSIONS["FPA:TERAPA"] = \
        lambda value: (10 ** -27) * value
    CONVERSIONS["FPA:TORR"] = \
        lambda: noconversion("FPA", "TORR")
    CONVERSIONS["FPA:YOTTAPA"] = \
        lambda value: (10 ** -39) * value
    CONVERSIONS["FPA:YPA"] = \
        lambda value: (10 ** 9) * value
    CONVERSIONS["FPA:ZETTAPA"] = \
        lambda value: (10 ** -36) * value
    CONVERSIONS["FPA:ZPA"] = \
        lambda value: (10 ** 6) * value
    CONVERSIONS["GIGAPA:APA"] = \
        lambda value: (10 ** 27) * value
    CONVERSIONS["GIGAPA:ATM"] = \
        lambda: noconversion("GIGAPA", "ATM")
    CONVERSIONS["GIGAPA:BAR"] = \
        lambda: noconversion("GIGAPA", "BAR")
    CONVERSIONS["GIGAPA:CBAR"] = \
        lambda: noconversion("GIGAPA", "CBAR")
    CONVERSIONS["GIGAPA:CPA"] = \
        lambda value: (10 ** 11) * value
    CONVERSIONS["GIGAPA:DAPA"] = \
        lambda value: (10 ** 8) * value
    CONVERSIONS["GIGAPA:DBAR"] = \
        lambda: noconversion("GIGAPA", "DBAR")
    CONVERSIONS["GIGAPA:DPA"] = \
        lambda value: (10 ** 10) * value
    CONVERSIONS["GIGAPA:EXAPA"] = \
        lambda value: (10 ** -9) * value
    CONVERSIONS["GIGAPA:FPA"] = \
        lambda value: (10 ** 24) * value
    CONVERSIONS["GIGAPA:HPA"] = \
        lambda value: (10 ** 7) * value
    CONVERSIONS["GIGAPA:KBAR"] = \
        lambda: noconversion("GIGAPA", "KBAR")
    CONVERSIONS["GIGAPA:KPA"] = \
        lambda value: (10 ** 6) * value
    CONVERSIONS["GIGAPA:MBAR"] = \
        lambda: noconversion("GIGAPA", "MBAR")
    CONVERSIONS["GIGAPA:MEGABAR"] = \
        lambda: noconversion("GIGAPA", "MEGABAR")
    CONVERSIONS["GIGAPA:MEGAPA"] = \
        lambda value: (10 ** 3) * value
    CONVERSIONS["GIGAPA:MICROPA"] = \
        lambda value: (10 ** 15) * value
    CONVERSIONS["GIGAPA:MMHG"] = \
        lambda: noconversion("GIGAPA", "MMHG")
    CONVERSIONS["GIGAPA:MPA"] = \
        lambda value: (10 ** 12) * value
    CONVERSIONS["GIGAPA:MTORR"] = \
        lambda: noconversion("GIGAPA", "MTORR")
    CONVERSIONS["GIGAPA:NPA"] = \
        lambda value: (10 ** 18) * value
    CONVERSIONS["GIGAPA:PA"] = \
        lambda value: (10 ** 9) * value
    CONVERSIONS["GIGAPA:PETAPA"] = \
        lambda value: (10 ** -6) * value
    CONVERSIONS["GIGAPA:PPA"] = \
        lambda value: (10 ** 21) * value
    CONVERSIONS["GIGAPA:PSI"] = \
        lambda: noconversion("GIGAPA", "PSI")
    CONVERSIONS["GIGAPA:TERAPA"] = \
        lambda value: (10 ** -3) * value
    CONVERSIONS["GIGAPA:TORR"] = \
        lambda: noconversion("GIGAPA", "TORR")
    CONVERSIONS["GIGAPA:YOTTAPA"] = \
        lambda value: (10 ** -15) * value
    CONVERSIONS["GIGAPA:YPA"] = \
        lambda value: (10 ** 33) * value
    CONVERSIONS["GIGAPA:ZETTAPA"] = \
        lambda value: (10 ** -12) * value
    CONVERSIONS["GIGAPA:ZPA"] = \
        lambda value: (10 ** 30) * value
    CONVERSIONS["HPA:APA"] = \
        lambda value: (10 ** 20) * value
    CONVERSIONS["HPA:ATM"] = \
        lambda: noconversion("HPA", "ATM")
    CONVERSIONS["HPA:BAR"] = \
        lambda: noconversion("HPA", "BAR")
    CONVERSIONS["HPA:CBAR"] = \
        lambda: noconversion("HPA", "CBAR")
    CONVERSIONS["HPA:CPA"] = \
        lambda value: (10 ** 4) * value
    CONVERSIONS["HPA:DAPA"] = \
        lambda value: 10 * value
    CONVERSIONS["HPA:DBAR"] = \
        lambda: noconversion("HPA", "DBAR")
    CONVERSIONS["HPA:DPA"] = \
        lambda value: (10 ** 3) * value
    CONVERSIONS["HPA:EXAPA"] = \
        lambda value: (10 ** -16) * value
    CONVERSIONS["HPA:FPA"] = \
        lambda value: (10 ** 17) * value
    CONVERSIONS["HPA:GIGAPA"] = \
        lambda value: (10 ** -7) * value
    CONVERSIONS["HPA:KBAR"] = \
        lambda: noconversion("HPA", "KBAR")
    CONVERSIONS["HPA:KPA"] = \
        lambda value: (10 ** -1) * value
    CONVERSIONS["HPA:MBAR"] = \
        lambda: noconversion("HPA", "MBAR")
    CONVERSIONS["HPA:MEGABAR"] = \
        lambda: noconversion("HPA", "MEGABAR")
    CONVERSIONS["HPA:MEGAPA"] = \
        lambda value: (10 ** -4) * value
    CONVERSIONS["HPA:MICROPA"] = \
        lambda value: (10 ** 8) * value
    CONVERSIONS["HPA:MMHG"] = \
        lambda: noconversion("HPA", "MMHG")
    CONVERSIONS["HPA:MPA"] = \
        lambda value: (10 ** 5) * value
    CONVERSIONS["HPA:MTORR"] = \
        lambda: noconversion("HPA", "MTORR")
    CONVERSIONS["HPA:NPA"] = \
        lambda value: (10 ** 11) * value
    CONVERSIONS["HPA:PA"] = \
        lambda value: (10 ** 2) * value
    CONVERSIONS["HPA:PETAPA"] = \
        lambda value: (10 ** -13) * value
    CONVERSIONS["HPA:PPA"] = \
        lambda value: (10 ** 14) * value
    CONVERSIONS["HPA:PSI"] = \
        lambda: noconversion("HPA", "PSI")
    CONVERSIONS["HPA:TERAPA"] = \
        lambda value: (10 ** -10) * value
    CONVERSIONS["HPA:TORR"] = \
        lambda: noconversion("HPA", "TORR")
    CONVERSIONS["HPA:YOTTAPA"] = \
        lambda value: (10 ** -22) * value
    CONVERSIONS["HPA:YPA"] = \
        lambda value: (10 ** 26) * value
    CONVERSIONS["HPA:ZETTAPA"] = \
        lambda value: (10 ** -19) * value
    CONVERSIONS["HPA:ZPA"] = \
        lambda value: (10 ** 23) * value
    CONVERSIONS["KBAR:APA"] = \
        lambda: noconversion("KBAR", "APA")
    CONVERSIONS["KBAR:ATM"] = \
        lambda: noconversion("KBAR", "ATM")
    CONVERSIONS["KBAR:BAR"] = \
        lambda: noconversion("KBAR", "BAR")
    CONVERSIONS["KBAR:CBAR"] = \
        lambda: noconversion("KBAR", "CBAR")
    CONVERSIONS["KBAR:CPA"] = \
        lambda: noconversion("KBAR", "CPA")
    CONVERSIONS["KBAR:DAPA"] = \
        lambda: noconversion("KBAR", "DAPA")
    CONVERSIONS["KBAR:DBAR"] = \
        lambda: noconversion("KBAR", "DBAR")
    CONVERSIONS["KBAR:DPA"] = \
        lambda: noconversion("KBAR", "DPA")
    CONVERSIONS["KBAR:EXAPA"] = \
        lambda: noconversion("KBAR", "EXAPA")
    CONVERSIONS["KBAR:FPA"] = \
        lambda: noconversion("KBAR", "FPA")
    CONVERSIONS["KBAR:GIGAPA"] = \
        lambda: noconversion("KBAR", "GIGAPA")
    CONVERSIONS["KBAR:HPA"] = \
        lambda: noconversion("KBAR", "HPA")
    CONVERSIONS["KBAR:KPA"] = \
        lambda: noconversion("KBAR", "KPA")
    CONVERSIONS["KBAR:MBAR"] = \
        lambda: noconversion("KBAR", "MBAR")
    CONVERSIONS["KBAR:MEGABAR"] = \
        lambda: noconversion("KBAR", "MEGABAR")
    CONVERSIONS["KBAR:MEGAPA"] = \
        lambda: noconversion("KBAR", "MEGAPA")
    CONVERSIONS["KBAR:MICROPA"] = \
        lambda: noconversion("KBAR", "MICROPA")
    CONVERSIONS["KBAR:MMHG"] = \
        lambda: noconversion("KBAR", "MMHG")
    CONVERSIONS["KBAR:MPA"] = \
        lambda: noconversion("KBAR", "MPA")
    CONVERSIONS["KBAR:MTORR"] = \
        lambda: noconversion("KBAR", "MTORR")
    CONVERSIONS["KBAR:NPA"] = \
        lambda: noconversion("KBAR", "NPA")
    CONVERSIONS["KBAR:PA"] = \
        lambda: noconversion("KBAR", "PA")
    CONVERSIONS["KBAR:PETAPA"] = \
        lambda: noconversion("KBAR", "PETAPA")
    CONVERSIONS["KBAR:PPA"] = \
        lambda: noconversion("KBAR", "PPA")
    CONVERSIONS["KBAR:PSI"] = \
        lambda: noconversion("KBAR", "PSI")
    CONVERSIONS["KBAR:TERAPA"] = \
        lambda: noconversion("KBAR", "TERAPA")
    CONVERSIONS["KBAR:TORR"] = \
        lambda: noconversion("KBAR", "TORR")
    CONVERSIONS["KBAR:YOTTAPA"] = \
        lambda: noconversion("KBAR", "YOTTAPA")
    CONVERSIONS["KBAR:YPA"] = \
        lambda: noconversion("KBAR", "YPA")
    CONVERSIONS["KBAR:ZETTAPA"] = \
        lambda: noconversion("KBAR", "ZETTAPA")
    CONVERSIONS["KBAR:ZPA"] = \
        lambda: noconversion("KBAR", "ZPA")
    CONVERSIONS["KPA:APA"] = \
        lambda value: (10 ** 21) * value
    CONVERSIONS["KPA:ATM"] = \
        lambda: noconversion("KPA", "ATM")
    CONVERSIONS["KPA:BAR"] = \
        lambda: noconversion("KPA", "BAR")
    CONVERSIONS["KPA:CBAR"] = \
        lambda: noconversion("KPA", "CBAR")
    CONVERSIONS["KPA:CPA"] = \
        lambda value: (10 ** 5) * value
    CONVERSIONS["KPA:DAPA"] = \
        lambda value: (10 ** 2) * value
    CONVERSIONS["KPA:DBAR"] = \
        lambda: noconversion("KPA", "DBAR")
    CONVERSIONS["KPA:DPA"] = \
        lambda value: (10 ** 4) * value
    CONVERSIONS["KPA:EXAPA"] = \
        lambda value: (10 ** -15) * value
    CONVERSIONS["KPA:FPA"] = \
        lambda value: (10 ** 18) * value
    CONVERSIONS["KPA:GIGAPA"] = \
        lambda value: (10 ** -6) * value
    CONVERSIONS["KPA:HPA"] = \
        lambda value: 10 * value
    CONVERSIONS["KPA:KBAR"] = \
        lambda: noconversion("KPA", "KBAR")
    CONVERSIONS["KPA:MBAR"] = \
        lambda: noconversion("KPA", "MBAR")
    CONVERSIONS["KPA:MEGABAR"] = \
        lambda: noconversion("KPA", "MEGABAR")
    CONVERSIONS["KPA:MEGAPA"] = \
        lambda value: (10 ** -3) * value
    CONVERSIONS["KPA:MICROPA"] = \
        lambda value: (10 ** 9) * value
    CONVERSIONS["KPA:MMHG"] = \
        lambda: noconversion("KPA", "MMHG")
    CONVERSIONS["KPA:MPA"] = \
        lambda value: (10 ** 6) * value
    CONVERSIONS["KPA:MTORR"] = \
        lambda: noconversion("KPA", "MTORR")
    CONVERSIONS["KPA:NPA"] = \
        lambda value: (10 ** 12) * value
    CONVERSIONS["KPA:PA"] = \
        lambda value: (10 ** 3) * value
    CONVERSIONS["KPA:PETAPA"] = \
        lambda value: (10 ** -12) * value
    CONVERSIONS["KPA:PPA"] = \
        lambda value: (10 ** 15) * value
    CONVERSIONS["KPA:PSI"] = \
        lambda: noconversion("KPA", "PSI")
    CONVERSIONS["KPA:TERAPA"] = \
        lambda value: (10 ** -9) * value
    CONVERSIONS["KPA:TORR"] = \
        lambda: noconversion("KPA", "TORR")
    CONVERSIONS["KPA:YOTTAPA"] = \
        lambda value: (10 ** -21) * value
    CONVERSIONS["KPA:YPA"] = \
        lambda value: (10 ** 27) * value
    CONVERSIONS["KPA:ZETTAPA"] = \
        lambda value: (10 ** -18) * value
    CONVERSIONS["KPA:ZPA"] = \
        lambda value: (10 ** 24) * value
    CONVERSIONS["MBAR:APA"] = \
        lambda: noconversion("MBAR", "APA")
    CONVERSIONS["MBAR:ATM"] = \
        lambda: noconversion("MBAR", "ATM")
    CONVERSIONS["MBAR:BAR"] = \
        lambda: noconversion("MBAR", "BAR")
    CONVERSIONS["MBAR:CBAR"] = \
        lambda: noconversion("MBAR", "CBAR")
    CONVERSIONS["MBAR:CPA"] = \
        lambda: noconversion("MBAR", "CPA")
    CONVERSIONS["MBAR:DAPA"] = \
        lambda: noconversion("MBAR", "DAPA")
    CONVERSIONS["MBAR:DBAR"] = \
        lambda: noconversion("MBAR", "DBAR")
    CONVERSIONS["MBAR:DPA"] = \
        lambda: noconversion("MBAR", "DPA")
    CONVERSIONS["MBAR:EXAPA"] = \
        lambda: noconversion("MBAR", "EXAPA")
    CONVERSIONS["MBAR:FPA"] = \
        lambda: noconversion("MBAR", "FPA")
    CONVERSIONS["MBAR:GIGAPA"] = \
        lambda: noconversion("MBAR", "GIGAPA")
    CONVERSIONS["MBAR:HPA"] = \
        lambda: noconversion("MBAR", "HPA")
    CONVERSIONS["MBAR:KBAR"] = \
        lambda: noconversion("MBAR", "KBAR")
    CONVERSIONS["MBAR:KPA"] = \
        lambda: noconversion("MBAR", "KPA")
    CONVERSIONS["MBAR:MEGABAR"] = \
        lambda: noconversion("MBAR", "MEGABAR")
    CONVERSIONS["MBAR:MEGAPA"] = \
        lambda: noconversion("MBAR", "MEGAPA")
    CONVERSIONS["MBAR:MICROPA"] = \
        lambda: noconversion("MBAR", "MICROPA")
    CONVERSIONS["MBAR:MMHG"] = \
        lambda: noconversion("MBAR", "MMHG")
    CONVERSIONS["MBAR:MPA"] = \
        lambda: noconversion("MBAR", "MPA")
    CONVERSIONS["MBAR:MTORR"] = \
        lambda: noconversion("MBAR", "MTORR")
    CONVERSIONS["MBAR:NPA"] = \
        lambda: noconversion("MBAR", "NPA")
    CONVERSIONS["MBAR:PA"] = \
        lambda: noconversion("MBAR", "PA")
    CONVERSIONS["MBAR:PETAPA"] = \
        lambda: noconversion("MBAR", "PETAPA")
    CONVERSIONS["MBAR:PPA"] = \
        lambda: noconversion("MBAR", "PPA")
    CONVERSIONS["MBAR:PSI"] = \
        lambda: noconversion("MBAR", "PSI")
    CONVERSIONS["MBAR:TERAPA"] = \
        lambda: noconversion("MBAR", "TERAPA")
    CONVERSIONS["MBAR:TORR"] = \
        lambda: noconversion("MBAR", "TORR")
    CONVERSIONS["MBAR:YOTTAPA"] = \
        lambda: noconversion("MBAR", "YOTTAPA")
    CONVERSIONS["MBAR:YPA"] = \
        lambda: noconversion("MBAR", "YPA")
    CONVERSIONS["MBAR:ZETTAPA"] = \
        lambda: noconversion("MBAR", "ZETTAPA")
    CONVERSIONS["MBAR:ZPA"] = \
        lambda: noconversion("MBAR", "ZPA")
    CONVERSIONS["MEGABAR:APA"] = \
        lambda: noconversion("MEGABAR", "APA")
    CONVERSIONS["MEGABAR:ATM"] = \
        lambda: noconversion("MEGABAR", "ATM")
    CONVERSIONS["MEGABAR:BAR"] = \
        lambda: noconversion("MEGABAR", "BAR")
    CONVERSIONS["MEGABAR:CBAR"] = \
        lambda: noconversion("MEGABAR", "CBAR")
    CONVERSIONS["MEGABAR:CPA"] = \
        lambda: noconversion("MEGABAR", "CPA")
    CONVERSIONS["MEGABAR:DAPA"] = \
        lambda: noconversion("MEGABAR", "DAPA")
    CONVERSIONS["MEGABAR:DBAR"] = \
        lambda: noconversion("MEGABAR", "DBAR")
    CONVERSIONS["MEGABAR:DPA"] = \
        lambda: noconversion("MEGABAR", "DPA")
    CONVERSIONS["MEGABAR:EXAPA"] = \
        lambda: noconversion("MEGABAR", "EXAPA")
    CONVERSIONS["MEGABAR:FPA"] = \
        lambda: noconversion("MEGABAR", "FPA")
    CONVERSIONS["MEGABAR:GIGAPA"] = \
        lambda: noconversion("MEGABAR", "GIGAPA")
    CONVERSIONS["MEGABAR:HPA"] = \
        lambda: noconversion("MEGABAR", "HPA")
    CONVERSIONS["MEGABAR:KBAR"] = \
        lambda: noconversion("MEGABAR", "KBAR")
    CONVERSIONS["MEGABAR:KPA"] = \
        lambda: noconversion("MEGABAR", "KPA")
    CONVERSIONS["MEGABAR:MBAR"] = \
        lambda: noconversion("MEGABAR", "MBAR")
    CONVERSIONS["MEGABAR:MEGAPA"] = \
        lambda: noconversion("MEGABAR", "MEGAPA")
    CONVERSIONS["MEGABAR:MICROPA"] = \
        lambda: noconversion("MEGABAR", "MICROPA")
    CONVERSIONS["MEGABAR:MMHG"] = \
        lambda: noconversion("MEGABAR", "MMHG")
    CONVERSIONS["MEGABAR:MPA"] = \
        lambda: noconversion("MEGABAR", "MPA")
    CONVERSIONS["MEGABAR:MTORR"] = \
        lambda: noconversion("MEGABAR", "MTORR")
    CONVERSIONS["MEGABAR:NPA"] = \
        lambda: noconversion("MEGABAR", "NPA")
    CONVERSIONS["MEGABAR:PA"] = \
        lambda: noconversion("MEGABAR", "PA")
    CONVERSIONS["MEGABAR:PETAPA"] = \
        lambda: noconversion("MEGABAR", "PETAPA")
    CONVERSIONS["MEGABAR:PPA"] = \
        lambda: noconversion("MEGABAR", "PPA")
    CONVERSIONS["MEGABAR:PSI"] = \
        lambda: noconversion("MEGABAR", "PSI")
    CONVERSIONS["MEGABAR:TERAPA"] = \
        lambda: noconversion("MEGABAR", "TERAPA")
    CONVERSIONS["MEGABAR:TORR"] = \
        lambda: noconversion("MEGABAR", "TORR")
    CONVERSIONS["MEGABAR:YOTTAPA"] = \
        lambda: noconversion("MEGABAR", "YOTTAPA")
    CONVERSIONS["MEGABAR:YPA"] = \
        lambda: noconversion("MEGABAR", "YPA")
    CONVERSIONS["MEGABAR:ZETTAPA"] = \
        lambda: noconversion("MEGABAR", "ZETTAPA")
    CONVERSIONS["MEGABAR:ZPA"] = \
        lambda: noconversion("MEGABAR", "ZPA")
    CONVERSIONS["MEGAPA:APA"] = \
        lambda value: (10 ** 24) * value
    CONVERSIONS["MEGAPA:ATM"] = \
        lambda: noconversion("MEGAPA", "ATM")
    CONVERSIONS["MEGAPA:BAR"] = \
        lambda: noconversion("MEGAPA", "BAR")
    CONVERSIONS["MEGAPA:CBAR"] = \
        lambda: noconversion("MEGAPA", "CBAR")
    CONVERSIONS["MEGAPA:CPA"] = \
        lambda value: (10 ** 8) * value
    CONVERSIONS["MEGAPA:DAPA"] = \
        lambda value: (10 ** 5) * value
    CONVERSIONS["MEGAPA:DBAR"] = \
        lambda: noconversion("MEGAPA", "DBAR")
    CONVERSIONS["MEGAPA:DPA"] = \
        lambda value: (10 ** 7) * value
    CONVERSIONS["MEGAPA:EXAPA"] = \
        lambda value: (10 ** -12) * value
    CONVERSIONS["MEGAPA:FPA"] = \
        lambda value: (10 ** 21) * value
    CONVERSIONS["MEGAPA:GIGAPA"] = \
        lambda value: (10 ** -3) * value
    CONVERSIONS["MEGAPA:HPA"] = \
        lambda value: (10 ** 4) * value
    CONVERSIONS["MEGAPA:KBAR"] = \
        lambda: noconversion("MEGAPA", "KBAR")
    CONVERSIONS["MEGAPA:KPA"] = \
        lambda value: (10 ** 3) * value
    CONVERSIONS["MEGAPA:MBAR"] = \
        lambda: noconversion("MEGAPA", "MBAR")
    CONVERSIONS["MEGAPA:MEGABAR"] = \
        lambda: noconversion("MEGAPA", "MEGABAR")
    CONVERSIONS["MEGAPA:MICROPA"] = \
        lambda value: (10 ** 12) * value
    CONVERSIONS["MEGAPA:MMHG"] = \
        lambda: noconversion("MEGAPA", "MMHG")
    CONVERSIONS["MEGAPA:MPA"] = \
        lambda value: (10 ** 9) * value
    CONVERSIONS["MEGAPA:MTORR"] = \
        lambda: noconversion("MEGAPA", "MTORR")
    CONVERSIONS["MEGAPA:NPA"] = \
        lambda value: (10 ** 15) * value
    CONVERSIONS["MEGAPA:PA"] = \
        lambda value: (10 ** 6) * value
    CONVERSIONS["MEGAPA:PETAPA"] = \
        lambda value: (10 ** -9) * value
    CONVERSIONS["MEGAPA:PPA"] = \
        lambda value: (10 ** 18) * value
    CONVERSIONS["MEGAPA:PSI"] = \
        lambda: noconversion("MEGAPA", "PSI")
    CONVERSIONS["MEGAPA:TERAPA"] = \
        lambda value: (10 ** -6) * value
    CONVERSIONS["MEGAPA:TORR"] = \
        lambda: noconversion("MEGAPA", "TORR")
    CONVERSIONS["MEGAPA:YOTTAPA"] = \
        lambda value: (10 ** -18) * value
    CONVERSIONS["MEGAPA:YPA"] = \
        lambda value: (10 ** 30) * value
    CONVERSIONS["MEGAPA:ZETTAPA"] = \
        lambda value: (10 ** -15) * value
    CONVERSIONS["MEGAPA:ZPA"] = \
        lambda value: (10 ** 27) * value
    CONVERSIONS["MICROPA:APA"] = \
        lambda value: (10 ** 12) * value
    CONVERSIONS["MICROPA:ATM"] = \
        lambda: noconversion("MICROPA", "ATM")
    CONVERSIONS["MICROPA:BAR"] = \
        lambda: noconversion("MICROPA", "BAR")
    CONVERSIONS["MICROPA:CBAR"] = \
        lambda: noconversion("MICROPA", "CBAR")
    CONVERSIONS["MICROPA:CPA"] = \
        lambda value: (10 ** -4) * value
    CONVERSIONS["MICROPA:DAPA"] = \
        lambda value: (10 ** -7) * value
    CONVERSIONS["MICROPA:DBAR"] = \
        lambda: noconversion("MICROPA", "DBAR")
    CONVERSIONS["MICROPA:DPA"] = \
        lambda value: (10 ** -5) * value
    CONVERSIONS["MICROPA:EXAPA"] = \
        lambda value: (10 ** -24) * value
    CONVERSIONS["MICROPA:FPA"] = \
        lambda value: (10 ** 9) * value
    CONVERSIONS["MICROPA:GIGAPA"] = \
        lambda value: (10 ** -15) * value
    CONVERSIONS["MICROPA:HPA"] = \
        lambda value: (10 ** -8) * value
    CONVERSIONS["MICROPA:KBAR"] = \
        lambda: noconversion("MICROPA", "KBAR")
    CONVERSIONS["MICROPA:KPA"] = \
        lambda value: (10 ** -9) * value
    CONVERSIONS["MICROPA:MBAR"] = \
        lambda: noconversion("MICROPA", "MBAR")
    CONVERSIONS["MICROPA:MEGABAR"] = \
        lambda: noconversion("MICROPA", "MEGABAR")
    CONVERSIONS["MICROPA:MEGAPA"] = \
        lambda value: (10 ** -12) * value
    CONVERSIONS["MICROPA:MMHG"] = \
        lambda: noconversion("MICROPA", "MMHG")
    CONVERSIONS["MICROPA:MPA"] = \
        lambda value: (10 ** -3) * value
    CONVERSIONS["MICROPA:MTORR"] = \
        lambda: noconversion("MICROPA", "MTORR")
    CONVERSIONS["MICROPA:NPA"] = \
        lambda value: (10 ** 3) * value
    CONVERSIONS["MICROPA:PA"] = \
        lambda value: (10 ** -6) * value
    CONVERSIONS["MICROPA:PETAPA"] = \
        lambda value: (10 ** -21) * value
    CONVERSIONS["MICROPA:PPA"] = \
        lambda value: (10 ** 6) * value
    CONVERSIONS["MICROPA:PSI"] = \
        lambda: noconversion("MICROPA", "PSI")
    CONVERSIONS["MICROPA:TERAPA"] = \
        lambda value: (10 ** -18) * value
    CONVERSIONS["MICROPA:TORR"] = \
        lambda: noconversion("MICROPA", "TORR")
    CONVERSIONS["MICROPA:YOTTAPA"] = \
        lambda value: (10 ** -30) * value
    CONVERSIONS["MICROPA:YPA"] = \
        lambda value: (10 ** 18) * value
    CONVERSIONS["MICROPA:ZETTAPA"] = \
        lambda value: (10 ** -27) * value
    CONVERSIONS["MICROPA:ZPA"] = \
        lambda value: (10 ** 15) * value
    CONVERSIONS["MMHG:APA"] = \
        lambda: noconversion("MMHG", "APA")
    CONVERSIONS["MMHG:ATM"] = \
        lambda: noconversion("MMHG", "ATM")
    CONVERSIONS["MMHG:BAR"] = \
        lambda: noconversion("MMHG", "BAR")
    CONVERSIONS["MMHG:CBAR"] = \
        lambda: noconversion("MMHG", "CBAR")
    CONVERSIONS["MMHG:CPA"] = \
        lambda: noconversion("MMHG", "CPA")
    CONVERSIONS["MMHG:DAPA"] = \
        lambda: noconversion("MMHG", "DAPA")
    CONVERSIONS["MMHG:DBAR"] = \
        lambda: noconversion("MMHG", "DBAR")
    CONVERSIONS["MMHG:DPA"] = \
        lambda: noconversion("MMHG", "DPA")
    CONVERSIONS["MMHG:EXAPA"] = \
        lambda: noconversion("MMHG", "EXAPA")
    CONVERSIONS["MMHG:FPA"] = \
        lambda: noconversion("MMHG", "FPA")
    CONVERSIONS["MMHG:GIGAPA"] = \
        lambda: noconversion("MMHG", "GIGAPA")
    CONVERSIONS["MMHG:HPA"] = \
        lambda: noconversion("MMHG", "HPA")
    CONVERSIONS["MMHG:KBAR"] = \
        lambda: noconversion("MMHG", "KBAR")
    CONVERSIONS["MMHG:KPA"] = \
        lambda: noconversion("MMHG", "KPA")
    CONVERSIONS["MMHG:MBAR"] = \
        lambda: noconversion("MMHG", "MBAR")
    CONVERSIONS["MMHG:MEGABAR"] = \
        lambda: noconversion("MMHG", "MEGABAR")
    CONVERSIONS["MMHG:MEGAPA"] = \
        lambda: noconversion("MMHG", "MEGAPA")
    CONVERSIONS["MMHG:MICROPA"] = \
        lambda: noconversion("MMHG", "MICROPA")
    CONVERSIONS["MMHG:MPA"] = \
        lambda: noconversion("MMHG", "MPA")
    CONVERSIONS["MMHG:MTORR"] = \
        lambda: noconversion("MMHG", "MTORR")
    CONVERSIONS["MMHG:NPA"] = \
        lambda: noconversion("MMHG", "NPA")
    CONVERSIONS["MMHG:PA"] = \
        lambda: noconversion("MMHG", "PA")
    CONVERSIONS["MMHG:PETAPA"] = \
        lambda: noconversion("MMHG", "PETAPA")
    CONVERSIONS["MMHG:PPA"] = \
        lambda: noconversion("MMHG", "PPA")
    CONVERSIONS["MMHG:PSI"] = \
        lambda: noconversion("MMHG", "PSI")
    CONVERSIONS["MMHG:TERAPA"] = \
        lambda: noconversion("MMHG", "TERAPA")
    CONVERSIONS["MMHG:TORR"] = \
        lambda: noconversion("MMHG", "TORR")
    CONVERSIONS["MMHG:YOTTAPA"] = \
        lambda: noconversion("MMHG", "YOTTAPA")
    CONVERSIONS["MMHG:YPA"] = \
        lambda: noconversion("MMHG", "YPA")
    CONVERSIONS["MMHG:ZETTAPA"] = \
        lambda: noconversion("MMHG", "ZETTAPA")
    CONVERSIONS["MMHG:ZPA"] = \
        lambda: noconversion("MMHG", "ZPA")
    CONVERSIONS["MPA:APA"] = \
        lambda value: (10 ** 15) * value
    CONVERSIONS["MPA:ATM"] = \
        lambda: noconversion("MPA", "ATM")
    CONVERSIONS["MPA:BAR"] = \
        lambda: noconversion("MPA", "BAR")
    CONVERSIONS["MPA:CBAR"] = \
        lambda: noconversion("MPA", "CBAR")
    CONVERSIONS["MPA:CPA"] = \
        lambda value: (10 ** -1) * value
    CONVERSIONS["MPA:DAPA"] = \
        lambda value: (10 ** -4) * value
    CONVERSIONS["MPA:DBAR"] = \
        lambda: noconversion("MPA", "DBAR")
    CONVERSIONS["MPA:DPA"] = \
        lambda value: (10 ** -2) * value
    CONVERSIONS["MPA:EXAPA"] = \
        lambda value: (10 ** -21) * value
    CONVERSIONS["MPA:FPA"] = \
        lambda value: (10 ** 12) * value
    CONVERSIONS["MPA:GIGAPA"] = \
        lambda value: (10 ** -12) * value
    CONVERSIONS["MPA:HPA"] = \
        lambda value: (10 ** -5) * value
    CONVERSIONS["MPA:KBAR"] = \
        lambda: noconversion("MPA", "KBAR")
    CONVERSIONS["MPA:KPA"] = \
        lambda value: (10 ** -6) * value
    CONVERSIONS["MPA:MBAR"] = \
        lambda: noconversion("MPA", "MBAR")
    CONVERSIONS["MPA:MEGABAR"] = \
        lambda: noconversion("MPA", "MEGABAR")
    CONVERSIONS["MPA:MEGAPA"] = \
        lambda value: (10 ** -9) * value
    CONVERSIONS["MPA:MICROPA"] = \
        lambda value: (10 ** 3) * value
    CONVERSIONS["MPA:MMHG"] = \
        lambda: noconversion("MPA", "MMHG")
    CONVERSIONS["MPA:MTORR"] = \
        lambda: noconversion("MPA", "MTORR")
    CONVERSIONS["MPA:NPA"] = \
        lambda value: (10 ** 6) * value
    CONVERSIONS["MPA:PA"] = \
        lambda value: (10 ** -3) * value
    CONVERSIONS["MPA:PETAPA"] = \
        lambda value: (10 ** -18) * value
    CONVERSIONS["MPA:PPA"] = \
        lambda value: (10 ** 9) * value
    CONVERSIONS["MPA:PSI"] = \
        lambda: noconversion("MPA", "PSI")
    CONVERSIONS["MPA:TERAPA"] = \
        lambda value: (10 ** -15) * value
    CONVERSIONS["MPA:TORR"] = \
        lambda: noconversion("MPA", "TORR")
    CONVERSIONS["MPA:YOTTAPA"] = \
        lambda value: (10 ** -27) * value
    CONVERSIONS["MPA:YPA"] = \
        lambda value: (10 ** 21) * value
    CONVERSIONS["MPA:ZETTAPA"] = \
        lambda value: (10 ** -24) * value
    CONVERSIONS["MPA:ZPA"] = \
        lambda value: (10 ** 18) * value
    CONVERSIONS["MTORR:APA"] = \
        lambda: noconversion("MTORR", "APA")
    CONVERSIONS["MTORR:ATM"] = \
        lambda: noconversion("MTORR", "ATM")
    CONVERSIONS["MTORR:BAR"] = \
        lambda: noconversion("MTORR", "BAR")
    CONVERSIONS["MTORR:CBAR"] = \
        lambda: noconversion("MTORR", "CBAR")
    CONVERSIONS["MTORR:CPA"] = \
        lambda: noconversion("MTORR", "CPA")
    CONVERSIONS["MTORR:DAPA"] = \
        lambda: noconversion("MTORR", "DAPA")
    CONVERSIONS["MTORR:DBAR"] = \
        lambda: noconversion("MTORR", "DBAR")
    CONVERSIONS["MTORR:DPA"] = \
        lambda: noconversion("MTORR", "DPA")
    CONVERSIONS["MTORR:EXAPA"] = \
        lambda: noconversion("MTORR", "EXAPA")
    CONVERSIONS["MTORR:FPA"] = \
        lambda: noconversion("MTORR", "FPA")
    CONVERSIONS["MTORR:GIGAPA"] = \
        lambda: noconversion("MTORR", "GIGAPA")
    CONVERSIONS["MTORR:HPA"] = \
        lambda: noconversion("MTORR", "HPA")
    CONVERSIONS["MTORR:KBAR"] = \
        lambda: noconversion("MTORR", "KBAR")
    CONVERSIONS["MTORR:KPA"] = \
        lambda: noconversion("MTORR", "KPA")
    CONVERSIONS["MTORR:MBAR"] = \
        lambda: noconversion("MTORR", "MBAR")
    CONVERSIONS["MTORR:MEGABAR"] = \
        lambda: noconversion("MTORR", "MEGABAR")
    CONVERSIONS["MTORR:MEGAPA"] = \
        lambda: noconversion("MTORR", "MEGAPA")
    CONVERSIONS["MTORR:MICROPA"] = \
        lambda: noconversion("MTORR", "MICROPA")
    CONVERSIONS["MTORR:MMHG"] = \
        lambda: noconversion("MTORR", "MMHG")
    CONVERSIONS["MTORR:MPA"] = \
        lambda: noconversion("MTORR", "MPA")
    CONVERSIONS["MTORR:NPA"] = \
        lambda: noconversion("MTORR", "NPA")
    CONVERSIONS["MTORR:PA"] = \
        lambda: noconversion("MTORR", "PA")
    CONVERSIONS["MTORR:PETAPA"] = \
        lambda: noconversion("MTORR", "PETAPA")
    CONVERSIONS["MTORR:PPA"] = \
        lambda: noconversion("MTORR", "PPA")
    CONVERSIONS["MTORR:PSI"] = \
        lambda: noconversion("MTORR", "PSI")
    CONVERSIONS["MTORR:TERAPA"] = \
        lambda: noconversion("MTORR", "TERAPA")
    CONVERSIONS["MTORR:TORR"] = \
        lambda: noconversion("MTORR", "TORR")
    CONVERSIONS["MTORR:YOTTAPA"] = \
        lambda: noconversion("MTORR", "YOTTAPA")
    CONVERSIONS["MTORR:YPA"] = \
        lambda: noconversion("MTORR", "YPA")
    CONVERSIONS["MTORR:ZETTAPA"] = \
        lambda: noconversion("MTORR", "ZETTAPA")
    CONVERSIONS["MTORR:ZPA"] = \
        lambda: noconversion("MTORR", "ZPA")
    CONVERSIONS["NPA:APA"] = \
        lambda value: (10 ** 9) * value
    CONVERSIONS["NPA:ATM"] = \
        lambda: noconversion("NPA", "ATM")
    CONVERSIONS["NPA:BAR"] = \
        lambda: noconversion("NPA", "BAR")
    CONVERSIONS["NPA:CBAR"] = \
        lambda: noconversion("NPA", "CBAR")
    CONVERSIONS["NPA:CPA"] = \
        lambda value: (10 ** -7) * value
    CONVERSIONS["NPA:DAPA"] = \
        lambda value: (10 ** -10) * value
    CONVERSIONS["NPA:DBAR"] = \
        lambda: noconversion("NPA", "DBAR")
    CONVERSIONS["NPA:DPA"] = \
        lambda value: (10 ** -8) * value
    CONVERSIONS["NPA:EXAPA"] = \
        lambda value: (10 ** -27) * value
    CONVERSIONS["NPA:FPA"] = \
        lambda value: (10 ** 6) * value
    CONVERSIONS["NPA:GIGAPA"] = \
        lambda value: (10 ** -18) * value
    CONVERSIONS["NPA:HPA"] = \
        lambda value: (10 ** -11) * value
    CONVERSIONS["NPA:KBAR"] = \
        lambda: noconversion("NPA", "KBAR")
    CONVERSIONS["NPA:KPA"] = \
        lambda value: (10 ** -12) * value
    CONVERSIONS["NPA:MBAR"] = \
        lambda: noconversion("NPA", "MBAR")
    CONVERSIONS["NPA:MEGABAR"] = \
        lambda: noconversion("NPA", "MEGABAR")
    CONVERSIONS["NPA:MEGAPA"] = \
        lambda value: (10 ** -15) * value
    CONVERSIONS["NPA:MICROPA"] = \
        lambda value: (10 ** -3) * value
    CONVERSIONS["NPA:MMHG"] = \
        lambda: noconversion("NPA", "MMHG")
    CONVERSIONS["NPA:MPA"] = \
        lambda value: (10 ** -6) * value
    CONVERSIONS["NPA:MTORR"] = \
        lambda: noconversion("NPA", "MTORR")
    CONVERSIONS["NPA:PA"] = \
        lambda value: (10 ** -9) * value
    CONVERSIONS["NPA:PETAPA"] = \
        lambda value: (10 ** -24) * value
    CONVERSIONS["NPA:PPA"] = \
        lambda value: (10 ** 3) * value
    CONVERSIONS["NPA:PSI"] = \
        lambda: noconversion("NPA", "PSI")
    CONVERSIONS["NPA:TERAPA"] = \
        lambda value: (10 ** -21) * value
    CONVERSIONS["NPA:TORR"] = \
        lambda: noconversion("NPA", "TORR")
    CONVERSIONS["NPA:YOTTAPA"] = \
        lambda value: (10 ** -33) * value
    CONVERSIONS["NPA:YPA"] = \
        lambda value: (10 ** 15) * value
    CONVERSIONS["NPA:ZETTAPA"] = \
        lambda value: (10 ** -30) * value
    CONVERSIONS["NPA:ZPA"] = \
        lambda value: (10 ** 12) * value
    CONVERSIONS["PA:APA"] = \
        lambda value: (10 ** 18) * value
    CONVERSIONS["PA:ATM"] = \
        lambda: noconversion("PA", "ATM")
    CONVERSIONS["PA:BAR"] = \
        lambda: noconversion("PA", "BAR")
    CONVERSIONS["PA:CBAR"] = \
        lambda: noconversion("PA", "CBAR")
    CONVERSIONS["PA:CPA"] = \
        lambda value: (10 ** 2) * value
    CONVERSIONS["PA:DAPA"] = \
        lambda value: (10 ** -1) * value
    CONVERSIONS["PA:DBAR"] = \
        lambda: noconversion("PA", "DBAR")
    CONVERSIONS["PA:DPA"] = \
        lambda value: 10 * value
    CONVERSIONS["PA:EXAPA"] = \
        lambda value: (10 ** -18) * value
    CONVERSIONS["PA:FPA"] = \
        lambda value: (10 ** 15) * value
    CONVERSIONS["PA:GIGAPA"] = \
        lambda value: (10 ** -9) * value
    CONVERSIONS["PA:HPA"] = \
        lambda value: (10 ** -2) * value
    CONVERSIONS["PA:KBAR"] = \
        lambda: noconversion("PA", "KBAR")
    CONVERSIONS["PA:KPA"] = \
        lambda value: (10 ** -3) * value
    CONVERSIONS["PA:MBAR"] = \
        lambda: noconversion("PA", "MBAR")
    CONVERSIONS["PA:MEGABAR"] = \
        lambda: noconversion("PA", "MEGABAR")
    CONVERSIONS["PA:MEGAPA"] = \
        lambda value: (10 ** -6) * value
    CONVERSIONS["PA:MICROPA"] = \
        lambda value: (10 ** 6) * value
    CONVERSIONS["PA:MMHG"] = \
        lambda: noconversion("PA", "MMHG")
    CONVERSIONS["PA:MPA"] = \
        lambda value: (10 ** 3) * value
    CONVERSIONS["PA:MTORR"] = \
        lambda: noconversion("PA", "MTORR")
    CONVERSIONS["PA:NPA"] = \
        lambda value: (10 ** 9) * value
    CONVERSIONS["PA:PETAPA"] = \
        lambda value: (10 ** -15) * value
    CONVERSIONS["PA:PPA"] = \
        lambda value: (10 ** 12) * value
    CONVERSIONS["PA:PSI"] = \
        lambda: noconversion("PA", "PSI")
    CONVERSIONS["PA:TERAPA"] = \
        lambda value: (10 ** -12) * value
    CONVERSIONS["PA:TORR"] = \
        lambda: noconversion("PA", "TORR")
    CONVERSIONS["PA:YOTTAPA"] = \
        lambda value: (10 ** -24) * value
    CONVERSIONS["PA:YPA"] = \
        lambda value: (10 ** 24) * value
    CONVERSIONS["PA:ZETTAPA"] = \
        lambda value: (10 ** -21) * value
    CONVERSIONS["PA:ZPA"] = \
        lambda value: (10 ** 21) * value
    CONVERSIONS["PETAPA:APA"] = \
        lambda value: (10 ** 33) * value
    CONVERSIONS["PETAPA:ATM"] = \
        lambda: noconversion("PETAPA", "ATM")
    CONVERSIONS["PETAPA:BAR"] = \
        lambda: noconversion("PETAPA", "BAR")
    CONVERSIONS["PETAPA:CBAR"] = \
        lambda: noconversion("PETAPA", "CBAR")
    CONVERSIONS["PETAPA:CPA"] = \
        lambda value: (10 ** 17) * value
    CONVERSIONS["PETAPA:DAPA"] = \
        lambda value: (10 ** 14) * value
    CONVERSIONS["PETAPA:DBAR"] = \
        lambda: noconversion("PETAPA", "DBAR")
    CONVERSIONS["PETAPA:DPA"] = \
        lambda value: (10 ** 16) * value
    CONVERSIONS["PETAPA:EXAPA"] = \
        lambda value: (10 ** -3) * value
    CONVERSIONS["PETAPA:FPA"] = \
        lambda value: (10 ** 30) * value
    CONVERSIONS["PETAPA:GIGAPA"] = \
        lambda value: (10 ** 6) * value
    CONVERSIONS["PETAPA:HPA"] = \
        lambda value: (10 ** 13) * value
    CONVERSIONS["PETAPA:KBAR"] = \
        lambda: noconversion("PETAPA", "KBAR")
    CONVERSIONS["PETAPA:KPA"] = \
        lambda value: (10 ** 12) * value
    CONVERSIONS["PETAPA:MBAR"] = \
        lambda: noconversion("PETAPA", "MBAR")
    CONVERSIONS["PETAPA:MEGABAR"] = \
        lambda: noconversion("PETAPA", "MEGABAR")
    CONVERSIONS["PETAPA:MEGAPA"] = \
        lambda value: (10 ** 9) * value
    CONVERSIONS["PETAPA:MICROPA"] = \
        lambda value: (10 ** 21) * value
    CONVERSIONS["PETAPA:MMHG"] = \
        lambda: noconversion("PETAPA", "MMHG")
    CONVERSIONS["PETAPA:MPA"] = \
        lambda value: (10 ** 18) * value
    CONVERSIONS["PETAPA:MTORR"] = \
        lambda: noconversion("PETAPA", "MTORR")
    CONVERSIONS["PETAPA:NPA"] = \
        lambda value: (10 ** 24) * value
    CONVERSIONS["PETAPA:PA"] = \
        lambda value: (10 ** 15) * value
    CONVERSIONS["PETAPA:PPA"] = \
        lambda value: (10 ** 27) * value
    CONVERSIONS["PETAPA:PSI"] = \
        lambda: noconversion("PETAPA", "PSI")
    CONVERSIONS["PETAPA:TERAPA"] = \
        lambda value: (10 ** 3) * value
    CONVERSIONS["PETAPA:TORR"] = \
        lambda: noconversion("PETAPA", "TORR")
    CONVERSIONS["PETAPA:YOTTAPA"] = \
        lambda value: (10 ** -9) * value
    CONVERSIONS["PETAPA:YPA"] = \
        lambda value: (10 ** 39) * value
    CONVERSIONS["PETAPA:ZETTAPA"] = \
        lambda value: (10 ** -6) * value
    CONVERSIONS["PETAPA:ZPA"] = \
        lambda value: (10 ** 36) * value
    CONVERSIONS["PPA:APA"] = \
        lambda value: (10 ** 6) * value
    CONVERSIONS["PPA:ATM"] = \
        lambda: noconversion("PPA", "ATM")
    CONVERSIONS["PPA:BAR"] = \
        lambda: noconversion("PPA", "BAR")
    CONVERSIONS["PPA:CBAR"] = \
        lambda: noconversion("PPA", "CBAR")
    CONVERSIONS["PPA:CPA"] = \
        lambda value: (10 ** -10) * value
    CONVERSIONS["PPA:DAPA"] = \
        lambda value: (10 ** -13) * value
    CONVERSIONS["PPA:DBAR"] = \
        lambda: noconversion("PPA", "DBAR")
    CONVERSIONS["PPA:DPA"] = \
        lambda value: (10 ** -11) * value
    CONVERSIONS["PPA:EXAPA"] = \
        lambda value: (10 ** -30) * value
    CONVERSIONS["PPA:FPA"] = \
        lambda value: (10 ** 3) * value
    CONVERSIONS["PPA:GIGAPA"] = \
        lambda value: (10 ** -21) * value
    CONVERSIONS["PPA:HPA"] = \
        lambda value: (10 ** -14) * value
    CONVERSIONS["PPA:KBAR"] = \
        lambda: noconversion("PPA", "KBAR")
    CONVERSIONS["PPA:KPA"] = \
        lambda value: (10 ** -15) * value
    CONVERSIONS["PPA:MBAR"] = \
        lambda: noconversion("PPA", "MBAR")
    CONVERSIONS["PPA:MEGABAR"] = \
        lambda: noconversion("PPA", "MEGABAR")
    CONVERSIONS["PPA:MEGAPA"] = \
        lambda value: (10 ** -18) * value
    CONVERSIONS["PPA:MICROPA"] = \
        lambda value: (10 ** -6) * value
    CONVERSIONS["PPA:MMHG"] = \
        lambda: noconversion("PPA", "MMHG")
    CONVERSIONS["PPA:MPA"] = \
        lambda value: (10 ** -9) * value
    CONVERSIONS["PPA:MTORR"] = \
        lambda: noconversion("PPA", "MTORR")
    CONVERSIONS["PPA:NPA"] = \
        lambda value: (10 ** -3) * value
    CONVERSIONS["PPA:PA"] = \
        lambda value: (10 ** -12) * value
    CONVERSIONS["PPA:PETAPA"] = \
        lambda value: (10 ** -27) * value
    CONVERSIONS["PPA:PSI"] = \
        lambda: noconversion("PPA", "PSI")
    CONVERSIONS["PPA:TERAPA"] = \
        lambda value: (10 ** -24) * value
    CONVERSIONS["PPA:TORR"] = \
        lambda: noconversion("PPA", "TORR")
    CONVERSIONS["PPA:YOTTAPA"] = \
        lambda value: (10 ** -36) * value
    CONVERSIONS["PPA:YPA"] = \
        lambda value: (10 ** 12) * value
    CONVERSIONS["PPA:ZETTAPA"] = \
        lambda value: (10 ** -33) * value
    CONVERSIONS["PPA:ZPA"] = \
        lambda value: (10 ** 9) * value
    CONVERSIONS["PSI:APA"] = \
        lambda: noconversion("PSI", "APA")
    CONVERSIONS["PSI:ATM"] = \
        lambda: noconversion("PSI", "ATM")
    CONVERSIONS["PSI:BAR"] = \
        lambda: noconversion("PSI", "BAR")
    CONVERSIONS["PSI:CBAR"] = \
        lambda: noconversion("PSI", "CBAR")
    CONVERSIONS["PSI:CPA"] = \
        lambda: noconversion("PSI", "CPA")
    CONVERSIONS["PSI:DAPA"] = \
        lambda: noconversion("PSI", "DAPA")
    CONVERSIONS["PSI:DBAR"] = \
        lambda: noconversion("PSI", "DBAR")
    CONVERSIONS["PSI:DPA"] = \
        lambda: noconversion("PSI", "DPA")
    CONVERSIONS["PSI:EXAPA"] = \
        lambda: noconversion("PSI", "EXAPA")
    CONVERSIONS["PSI:FPA"] = \
        lambda: noconversion("PSI", "FPA")
    CONVERSIONS["PSI:GIGAPA"] = \
        lambda: noconversion("PSI", "GIGAPA")
    CONVERSIONS["PSI:HPA"] = \
        lambda: noconversion("PSI", "HPA")
    CONVERSIONS["PSI:KBAR"] = \
        lambda: noconversion("PSI", "KBAR")
    CONVERSIONS["PSI:KPA"] = \
        lambda: noconversion("PSI", "KPA")
    CONVERSIONS["PSI:MBAR"] = \
        lambda: noconversion("PSI", "MBAR")
    CONVERSIONS["PSI:MEGABAR"] = \
        lambda: noconversion("PSI", "MEGABAR")
    CONVERSIONS["PSI:MEGAPA"] = \
        lambda: noconversion("PSI", "MEGAPA")
    CONVERSIONS["PSI:MICROPA"] = \
        lambda: noconversion("PSI", "MICROPA")
    CONVERSIONS["PSI:MMHG"] = \
        lambda: noconversion("PSI", "MMHG")
    CONVERSIONS["PSI:MPA"] = \
        lambda: noconversion("PSI", "MPA")
    CONVERSIONS["PSI:MTORR"] = \
        lambda: noconversion("PSI", "MTORR")
    CONVERSIONS["PSI:NPA"] = \
        lambda: noconversion("PSI", "NPA")
    CONVERSIONS["PSI:PA"] = \
        lambda: noconversion("PSI", "PA")
    CONVERSIONS["PSI:PETAPA"] = \
        lambda: noconversion("PSI", "PETAPA")
    CONVERSIONS["PSI:PPA"] = \
        lambda: noconversion("PSI", "PPA")
    CONVERSIONS["PSI:TERAPA"] = \
        lambda: noconversion("PSI", "TERAPA")
    CONVERSIONS["PSI:TORR"] = \
        lambda: noconversion("PSI", "TORR")
    CONVERSIONS["PSI:YOTTAPA"] = \
        lambda: noconversion("PSI", "YOTTAPA")
    CONVERSIONS["PSI:YPA"] = \
        lambda: noconversion("PSI", "YPA")
    CONVERSIONS["PSI:ZETTAPA"] = \
        lambda: noconversion("PSI", "ZETTAPA")
    CONVERSIONS["PSI:ZPA"] = \
        lambda: noconversion("PSI", "ZPA")
    CONVERSIONS["TERAPA:APA"] = \
        lambda value: (10 ** 30) * value
    CONVERSIONS["TERAPA:ATM"] = \
        lambda: noconversion("TERAPA", "ATM")
    CONVERSIONS["TERAPA:BAR"] = \
        lambda: noconversion("TERAPA", "BAR")
    CONVERSIONS["TERAPA:CBAR"] = \
        lambda: noconversion("TERAPA", "CBAR")
    CONVERSIONS["TERAPA:CPA"] = \
        lambda value: (10 ** 14) * value
    CONVERSIONS["TERAPA:DAPA"] = \
        lambda value: (10 ** 11) * value
    CONVERSIONS["TERAPA:DBAR"] = \
        lambda: noconversion("TERAPA", "DBAR")
    CONVERSIONS["TERAPA:DPA"] = \
        lambda value: (10 ** 13) * value
    CONVERSIONS["TERAPA:EXAPA"] = \
        lambda value: (10 ** -6) * value
    CONVERSIONS["TERAPA:FPA"] = \
        lambda value: (10 ** 27) * value
    CONVERSIONS["TERAPA:GIGAPA"] = \
        lambda value: (10 ** 3) * value
    CONVERSIONS["TERAPA:HPA"] = \
        lambda value: (10 ** 10) * value
    CONVERSIONS["TERAPA:KBAR"] = \
        lambda: noconversion("TERAPA", "KBAR")
    CONVERSIONS["TERAPA:KPA"] = \
        lambda value: (10 ** 9) * value
    CONVERSIONS["TERAPA:MBAR"] = \
        lambda: noconversion("TERAPA", "MBAR")
    CONVERSIONS["TERAPA:MEGABAR"] = \
        lambda: noconversion("TERAPA", "MEGABAR")
    CONVERSIONS["TERAPA:MEGAPA"] = \
        lambda value: (10 ** 6) * value
    CONVERSIONS["TERAPA:MICROPA"] = \
        lambda value: (10 ** 18) * value
    CONVERSIONS["TERAPA:MMHG"] = \
        lambda: noconversion("TERAPA", "MMHG")
    CONVERSIONS["TERAPA:MPA"] = \
        lambda value: (10 ** 15) * value
    CONVERSIONS["TERAPA:MTORR"] = \
        lambda: noconversion("TERAPA", "MTORR")
    CONVERSIONS["TERAPA:NPA"] = \
        lambda value: (10 ** 21) * value
    CONVERSIONS["TERAPA:PA"] = \
        lambda value: (10 ** 12) * value
    CONVERSIONS["TERAPA:PETAPA"] = \
        lambda value: (10 ** -3) * value
    CONVERSIONS["TERAPA:PPA"] = \
        lambda value: (10 ** 24) * value
    CONVERSIONS["TERAPA:PSI"] = \
        lambda: noconversion("TERAPA", "PSI")
    CONVERSIONS["TERAPA:TORR"] = \
        lambda: noconversion("TERAPA", "TORR")
    CONVERSIONS["TERAPA:YOTTAPA"] = \
        lambda value: (10 ** -12) * value
    CONVERSIONS["TERAPA:YPA"] = \
        lambda value: (10 ** 36) * value
    CONVERSIONS["TERAPA:ZETTAPA"] = \
        lambda value: (10 ** -9) * value
    CONVERSIONS["TERAPA:ZPA"] = \
        lambda value: (10 ** 33) * value
    CONVERSIONS["TORR:APA"] = \
        lambda: noconversion("TORR", "APA")
    CONVERSIONS["TORR:ATM"] = \
        lambda: noconversion("TORR", "ATM")
    CONVERSIONS["TORR:BAR"] = \
        lambda: noconversion("TORR", "BAR")
    CONVERSIONS["TORR:CBAR"] = \
        lambda: noconversion("TORR", "CBAR")
    CONVERSIONS["TORR:CPA"] = \
        lambda: noconversion("TORR", "CPA")
    CONVERSIONS["TORR:DAPA"] = \
        lambda: noconversion("TORR", "DAPA")
    CONVERSIONS["TORR:DBAR"] = \
        lambda: noconversion("TORR", "DBAR")
    CONVERSIONS["TORR:DPA"] = \
        lambda: noconversion("TORR", "DPA")
    CONVERSIONS["TORR:EXAPA"] = \
        lambda: noconversion("TORR", "EXAPA")
    CONVERSIONS["TORR:FPA"] = \
        lambda: noconversion("TORR", "FPA")
    CONVERSIONS["TORR:GIGAPA"] = \
        lambda: noconversion("TORR", "GIGAPA")
    CONVERSIONS["TORR:HPA"] = \
        lambda: noconversion("TORR", "HPA")
    CONVERSIONS["TORR:KBAR"] = \
        lambda: noconversion("TORR", "KBAR")
    CONVERSIONS["TORR:KPA"] = \
        lambda: noconversion("TORR", "KPA")
    CONVERSIONS["TORR:MBAR"] = \
        lambda: noconversion("TORR", "MBAR")
    CONVERSIONS["TORR:MEGABAR"] = \
        lambda: noconversion("TORR", "MEGABAR")
    CONVERSIONS["TORR:MEGAPA"] = \
        lambda: noconversion("TORR", "MEGAPA")
    CONVERSIONS["TORR:MICROPA"] = \
        lambda: noconversion("TORR", "MICROPA")
    CONVERSIONS["TORR:MMHG"] = \
        lambda: noconversion("TORR", "MMHG")
    CONVERSIONS["TORR:MPA"] = \
        lambda: noconversion("TORR", "MPA")
    CONVERSIONS["TORR:MTORR"] = \
        lambda: noconversion("TORR", "MTORR")
    CONVERSIONS["TORR:NPA"] = \
        lambda: noconversion("TORR", "NPA")
    CONVERSIONS["TORR:PA"] = \
        lambda: noconversion("TORR", "PA")
    CONVERSIONS["TORR:PETAPA"] = \
        lambda: noconversion("TORR", "PETAPA")
    CONVERSIONS["TORR:PPA"] = \
        lambda: noconversion("TORR", "PPA")
    CONVERSIONS["TORR:PSI"] = \
        lambda: noconversion("TORR", "PSI")
    CONVERSIONS["TORR:TERAPA"] = \
        lambda: noconversion("TORR", "TERAPA")
    CONVERSIONS["TORR:YOTTAPA"] = \
        lambda: noconversion("TORR", "YOTTAPA")
    CONVERSIONS["TORR:YPA"] = \
        lambda: noconversion("TORR", "YPA")
    CONVERSIONS["TORR:ZETTAPA"] = \
        lambda: noconversion("TORR", "ZETTAPA")
    CONVERSIONS["TORR:ZPA"] = \
        lambda: noconversion("TORR", "ZPA")
    CONVERSIONS["YOTTAPA:APA"] = \
        lambda value: (10 ** 42) * value
    CONVERSIONS["YOTTAPA:ATM"] = \
        lambda: noconversion("YOTTAPA", "ATM")
    CONVERSIONS["YOTTAPA:BAR"] = \
        lambda: noconversion("YOTTAPA", "BAR")
    CONVERSIONS["YOTTAPA:CBAR"] = \
        lambda: noconversion("YOTTAPA", "CBAR")
    CONVERSIONS["YOTTAPA:CPA"] = \
        lambda value: (10 ** 26) * value
    CONVERSIONS["YOTTAPA:DAPA"] = \
        lambda value: (10 ** 23) * value
    CONVERSIONS["YOTTAPA:DBAR"] = \
        lambda: noconversion("YOTTAPA", "DBAR")
    CONVERSIONS["YOTTAPA:DPA"] = \
        lambda value: (10 ** 25) * value
    CONVERSIONS["YOTTAPA:EXAPA"] = \
        lambda value: (10 ** 6) * value
    CONVERSIONS["YOTTAPA:FPA"] = \
        lambda value: (10 ** 39) * value
    CONVERSIONS["YOTTAPA:GIGAPA"] = \
        lambda value: (10 ** 15) * value
    CONVERSIONS["YOTTAPA:HPA"] = \
        lambda value: (10 ** 22) * value
    CONVERSIONS["YOTTAPA:KBAR"] = \
        lambda: noconversion("YOTTAPA", "KBAR")
    CONVERSIONS["YOTTAPA:KPA"] = \
        lambda value: (10 ** 21) * value
    CONVERSIONS["YOTTAPA:MBAR"] = \
        lambda: noconversion("YOTTAPA", "MBAR")
    CONVERSIONS["YOTTAPA:MEGABAR"] = \
        lambda: noconversion("YOTTAPA", "MEGABAR")
    CONVERSIONS["YOTTAPA:MEGAPA"] = \
        lambda value: (10 ** 18) * value
    CONVERSIONS["YOTTAPA:MICROPA"] = \
        lambda value: (10 ** 30) * value
    CONVERSIONS["YOTTAPA:MMHG"] = \
        lambda: noconversion("YOTTAPA", "MMHG")
    CONVERSIONS["YOTTAPA:MPA"] = \
        lambda value: (10 ** 27) * value
    CONVERSIONS["YOTTAPA:MTORR"] = \
        lambda: noconversion("YOTTAPA", "MTORR")
    CONVERSIONS["YOTTAPA:NPA"] = \
        lambda value: (10 ** 33) * value
    CONVERSIONS["YOTTAPA:PA"] = \
        lambda value: (10 ** 24) * value
    CONVERSIONS["YOTTAPA:PETAPA"] = \
        lambda value: (10 ** 9) * value
    CONVERSIONS["YOTTAPA:PPA"] = \
        lambda value: (10 ** 36) * value
    CONVERSIONS["YOTTAPA:PSI"] = \
        lambda: noconversion("YOTTAPA", "PSI")
    CONVERSIONS["YOTTAPA:TERAPA"] = \
        lambda value: (10 ** 12) * value
    CONVERSIONS["YOTTAPA:TORR"] = \
        lambda: noconversion("YOTTAPA", "TORR")
    CONVERSIONS["YOTTAPA:YPA"] = \
        lambda value: (10 ** 48) * value
    CONVERSIONS["YOTTAPA:ZETTAPA"] = \
        lambda value: (10 ** 3) * value
    CONVERSIONS["YOTTAPA:ZPA"] = \
        lambda value: (10 ** 45) * value
    CONVERSIONS["YPA:APA"] = \
        lambda value: (10 ** -6) * value
    CONVERSIONS["YPA:ATM"] = \
        lambda: noconversion("YPA", "ATM")
    CONVERSIONS["YPA:BAR"] = \
        lambda: noconversion("YPA", "BAR")
    CONVERSIONS["YPA:CBAR"] = \
        lambda: noconversion("YPA", "CBAR")
    CONVERSIONS["YPA:CPA"] = \
        lambda value: (10 ** -22) * value
    CONVERSIONS["YPA:DAPA"] = \
        lambda value: (10 ** -25) * value
    CONVERSIONS["YPA:DBAR"] = \
        lambda: noconversion("YPA", "DBAR")
    CONVERSIONS["YPA:DPA"] = \
        lambda value: (10 ** -23) * value
    CONVERSIONS["YPA:EXAPA"] = \
        lambda value: (10 ** -42) * value
    CONVERSIONS["YPA:FPA"] = \
        lambda value: (10 ** -9) * value
    CONVERSIONS["YPA:GIGAPA"] = \
        lambda value: (10 ** -33) * value
    CONVERSIONS["YPA:HPA"] = \
        lambda value: (10 ** -26) * value
    CONVERSIONS["YPA:KBAR"] = \
        lambda: noconversion("YPA", "KBAR")
    CONVERSIONS["YPA:KPA"] = \
        lambda value: (10 ** -27) * value
    CONVERSIONS["YPA:MBAR"] = \
        lambda: noconversion("YPA", "MBAR")
    CONVERSIONS["YPA:MEGABAR"] = \
        lambda: noconversion("YPA", "MEGABAR")
    CONVERSIONS["YPA:MEGAPA"] = \
        lambda value: (10 ** -30) * value
    CONVERSIONS["YPA:MICROPA"] = \
        lambda value: (10 ** -18) * value
    CONVERSIONS["YPA:MMHG"] = \
        lambda: noconversion("YPA", "MMHG")
    CONVERSIONS["YPA:MPA"] = \
        lambda value: (10 ** -21) * value
    CONVERSIONS["YPA:MTORR"] = \
        lambda: noconversion("YPA", "MTORR")
    CONVERSIONS["YPA:NPA"] = \
        lambda value: (10 ** -15) * value
    CONVERSIONS["YPA:PA"] = \
        lambda value: (10 ** -24) * value
    CONVERSIONS["YPA:PETAPA"] = \
        lambda value: (10 ** -39) * value
    CONVERSIONS["YPA:PPA"] = \
        lambda value: (10 ** -12) * value
    CONVERSIONS["YPA:PSI"] = \
        lambda: noconversion("YPA", "PSI")
    CONVERSIONS["YPA:TERAPA"] = \
        lambda value: (10 ** -36) * value
    CONVERSIONS["YPA:TORR"] = \
        lambda: noconversion("YPA", "TORR")
    CONVERSIONS["YPA:YOTTAPA"] = \
        lambda value: (10 ** -48) * value
    CONVERSIONS["YPA:ZETTAPA"] = \
        lambda value: (10 ** -45) * value
    CONVERSIONS["YPA:ZPA"] = \
        lambda value: (10 ** -3) * value
    CONVERSIONS["ZETTAPA:APA"] = \
        lambda value: (10 ** 39) * value
    CONVERSIONS["ZETTAPA:ATM"] = \
        lambda: noconversion("ZETTAPA", "ATM")
    CONVERSIONS["ZETTAPA:BAR"] = \
        lambda: noconversion("ZETTAPA", "BAR")
    CONVERSIONS["ZETTAPA:CBAR"] = \
        lambda: noconversion("ZETTAPA", "CBAR")
    CONVERSIONS["ZETTAPA:CPA"] = \
        lambda value: (10 ** 23) * value
    CONVERSIONS["ZETTAPA:DAPA"] = \
        lambda value: (10 ** 20) * value
    CONVERSIONS["ZETTAPA:DBAR"] = \
        lambda: noconversion("ZETTAPA", "DBAR")
    CONVERSIONS["ZETTAPA:DPA"] = \
        lambda value: (10 ** 22) * value
    CONVERSIONS["ZETTAPA:EXAPA"] = \
        lambda value: (10 ** 3) * value
    CONVERSIONS["ZETTAPA:FPA"] = \
        lambda value: (10 ** 36) * value
    CONVERSIONS["ZETTAPA:GIGAPA"] = \
        lambda value: (10 ** 12) * value
    CONVERSIONS["ZETTAPA:HPA"] = \
        lambda value: (10 ** 19) * value
    CONVERSIONS["ZETTAPA:KBAR"] = \
        lambda: noconversion("ZETTAPA", "KBAR")
    CONVERSIONS["ZETTAPA:KPA"] = \
        lambda value: (10 ** 18) * value
    CONVERSIONS["ZETTAPA:MBAR"] = \
        lambda: noconversion("ZETTAPA", "MBAR")
    CONVERSIONS["ZETTAPA:MEGABAR"] = \
        lambda: noconversion("ZETTAPA", "MEGABAR")
    CONVERSIONS["ZETTAPA:MEGAPA"] = \
        lambda value: (10 ** 15) * value
    CONVERSIONS["ZETTAPA:MICROPA"] = \
        lambda value: (10 ** 27) * value
    CONVERSIONS["ZETTAPA:MMHG"] = \
        lambda: noconversion("ZETTAPA", "MMHG")
    CONVERSIONS["ZETTAPA:MPA"] = \
        lambda value: (10 ** 24) * value
    CONVERSIONS["ZETTAPA:MTORR"] = \
        lambda: noconversion("ZETTAPA", "MTORR")
    CONVERSIONS["ZETTAPA:NPA"] = \
        lambda value: (10 ** 30) * value
    CONVERSIONS["ZETTAPA:PA"] = \
        lambda value: (10 ** 21) * value
    CONVERSIONS["ZETTAPA:PETAPA"] = \
        lambda value: (10 ** 6) * value
    CONVERSIONS["ZETTAPA:PPA"] = \
        lambda value: (10 ** 33) * value
    CONVERSIONS["ZETTAPA:PSI"] = \
        lambda: noconversion("ZETTAPA", "PSI")
    CONVERSIONS["ZETTAPA:TERAPA"] = \
        lambda value: (10 ** 9) * value
    CONVERSIONS["ZETTAPA:TORR"] = \
        lambda: noconversion("ZETTAPA", "TORR")
    CONVERSIONS["ZETTAPA:YOTTAPA"] = \
        lambda value: (10 ** -3) * value
    CONVERSIONS["ZETTAPA:YPA"] = \
        lambda value: (10 ** 45) * value
    CONVERSIONS["ZETTAPA:ZPA"] = \
        lambda value: (10 ** 42) * value
    CONVERSIONS["ZPA:APA"] = \
        lambda value: (10 ** -3) * value
    CONVERSIONS["ZPA:ATM"] = \
        lambda: noconversion("ZPA", "ATM")
    CONVERSIONS["ZPA:BAR"] = \
        lambda: noconversion("ZPA", "BAR")
    CONVERSIONS["ZPA:CBAR"] = \
        lambda: noconversion("ZPA", "CBAR")
    CONVERSIONS["ZPA:CPA"] = \
        lambda value: (10 ** -19) * value
    CONVERSIONS["ZPA:DAPA"] = \
        lambda value: (10 ** -22) * value
    CONVERSIONS["ZPA:DBAR"] = \
        lambda: noconversion("ZPA", "DBAR")
    CONVERSIONS["ZPA:DPA"] = \
        lambda value: (10 ** -20) * value
    CONVERSIONS["ZPA:EXAPA"] = \
        lambda value: (10 ** -39) * value
    CONVERSIONS["ZPA:FPA"] = \
        lambda value: (10 ** -6) * value
    CONVERSIONS["ZPA:GIGAPA"] = \
        lambda value: (10 ** -30) * value
    CONVERSIONS["ZPA:HPA"] = \
        lambda value: (10 ** -23) * value
    CONVERSIONS["ZPA:KBAR"] = \
        lambda: noconversion("ZPA", "KBAR")
    CONVERSIONS["ZPA:KPA"] = \
        lambda value: (10 ** -24) * value
    CONVERSIONS["ZPA:MBAR"] = \
        lambda: noconversion("ZPA", "MBAR")
    CONVERSIONS["ZPA:MEGABAR"] = \
        lambda: noconversion("ZPA", "MEGABAR")
    CONVERSIONS["ZPA:MEGAPA"] = \
        lambda value: (10 ** -27) * value
    CONVERSIONS["ZPA:MICROPA"] = \
        lambda value: (10 ** -15) * value
    CONVERSIONS["ZPA:MMHG"] = \
        lambda: noconversion("ZPA", "MMHG")
    CONVERSIONS["ZPA:MPA"] = \
        lambda value: (10 ** -18) * value
    CONVERSIONS["ZPA:MTORR"] = \
        lambda: noconversion("ZPA", "MTORR")
    CONVERSIONS["ZPA:NPA"] = \
        lambda value: (10 ** -12) * value
    CONVERSIONS["ZPA:PA"] = \
        lambda value: (10 ** -21) * value
    CONVERSIONS["ZPA:PETAPA"] = \
        lambda value: (10 ** -36) * value
    CONVERSIONS["ZPA:PPA"] = \
        lambda value: (10 ** -9) * value
    CONVERSIONS["ZPA:PSI"] = \
        lambda: noconversion("ZPA", "PSI")
    CONVERSIONS["ZPA:TERAPA"] = \
        lambda value: (10 ** -33) * value
    CONVERSIONS["ZPA:TORR"] = \
        lambda: noconversion("ZPA", "TORR")
    CONVERSIONS["ZPA:YOTTAPA"] = \
        lambda value: (10 ** -45) * value
    CONVERSIONS["ZPA:YPA"] = \
        lambda value: (10 ** 3) * value
    CONVERSIONS["ZPA:ZETTAPA"] = \
        lambda value: (10 ** -42) * value

    SYMBOLS = dict()
    SYMBOLS["APA"] = "aPa"
    SYMBOLS["ATM"] = "atm"
    SYMBOLS["BAR"] = "bar"
    SYMBOLS["CBAR"] = "cbar"
    SYMBOLS["CPA"] = "cPa"
    SYMBOLS["DAPA"] = "daPa"
    SYMBOLS["DBAR"] = "dbar"
    SYMBOLS["DPA"] = "dPa"
    SYMBOLS["EXAPA"] = "EPa"
    SYMBOLS["FPA"] = "fPa"
    SYMBOLS["GIGAPA"] = "GPa"
    SYMBOLS["HPA"] = "hPa"
    SYMBOLS["KBAR"] = "kBar"
    SYMBOLS["KPA"] = "kPa"
    SYMBOLS["MBAR"] = "mbar"
    SYMBOLS["MEGABAR"] = "Mbar"
    SYMBOLS["MEGAPA"] = "MPa"
    SYMBOLS["MICROPA"] = "µPa"
    SYMBOLS["MMHG"] = "mm Hg"
    SYMBOLS["MPA"] = "mPa"
    SYMBOLS["MTORR"] = "mTorr"
    SYMBOLS["NPA"] = "nPa"
    SYMBOLS["PA"] = "Pa"
    SYMBOLS["PETAPA"] = "PPa"
    SYMBOLS["PPA"] = "pPa"
    SYMBOLS["PSI"] = "psi"
    SYMBOLS["TERAPA"] = "TPa"
    SYMBOLS["TORR"] = "Torr"
    SYMBOLS["YOTTAPA"] = "YPa"
    SYMBOLS["YPA"] = "yPa"
    SYMBOLS["ZETTAPA"] = "ZPa"
    SYMBOLS["ZPA"] = "zPa"

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

    def getSymbol(self):
        return self.SYMBOLS.get(str(self.getUnit()))

    def setUnit(self, unit, current=None):
        self._unit = unit

    def setValue(self, value, current=None):
        self._value = value

    def __str__(self):
        return self._base_string(self.getValue(), self.getUnit())

_omero_model.PressureI = PressureI
