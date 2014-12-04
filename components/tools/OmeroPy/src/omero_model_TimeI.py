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
Code-generated omero.model.Time implementation,
based on omero.model.PermissionsI
"""


import Ice
import IceImport
IceImport.load("omero_model_Time_ice")
_omero = Ice.openModule("omero")
_omero_model = Ice.openModule("omero.model")
__name__ = "omero.model"

from omero_model_UnitBase import UnitBase
from omero.model.enums import UnitsTime


def noconversion(cfrom, cto):
    raise Exception(("Unsupported conversion: "
                     "%s:%s") % cfrom, cto)


class TimeI(_omero_model.Time, UnitBase):

    CONVERSIONS = dict()
    CONVERSIONS["AS:CS"] = \
        lambda value: (10 ** -16) * value
    CONVERSIONS["AS:D"] = \
        lambda: noconversion("AS", "D")
    CONVERSIONS["AS:DAS"] = \
        lambda value: (10 ** -19) * value
    CONVERSIONS["AS:DS"] = \
        lambda value: (10 ** -17) * value
    CONVERSIONS["AS:EXAS"] = \
        lambda value: (10 ** -36) * value
    CONVERSIONS["AS:FS"] = \
        lambda value: (10 ** -3) * value
    CONVERSIONS["AS:GIGAS"] = \
        lambda value: (10 ** -27) * value
    CONVERSIONS["AS:H"] = \
        lambda: noconversion("AS", "H")
    CONVERSIONS["AS:HS"] = \
        lambda value: (10 ** -20) * value
    CONVERSIONS["AS:KS"] = \
        lambda value: (10 ** -21) * value
    CONVERSIONS["AS:MEGAS"] = \
        lambda value: (10 ** -24) * value
    CONVERSIONS["AS:MICROS"] = \
        lambda value: (10 ** -12) * value
    CONVERSIONS["AS:MIN"] = \
        lambda: noconversion("AS", "MIN")
    CONVERSIONS["AS:MS"] = \
        lambda value: (10 ** -15) * value
    CONVERSIONS["AS:NS"] = \
        lambda value: (10 ** -9) * value
    CONVERSIONS["AS:PETAS"] = \
        lambda value: (10 ** -33) * value
    CONVERSIONS["AS:PS"] = \
        lambda value: (10 ** -6) * value
    CONVERSIONS["AS:S"] = \
        lambda value: (10 ** -18) * value
    CONVERSIONS["AS:TERAS"] = \
        lambda value: (10 ** -30) * value
    CONVERSIONS["AS:YOTTAS"] = \
        lambda value: (10 ** -42) * value
    CONVERSIONS["AS:YS"] = \
        lambda value: (10 ** 6) * value
    CONVERSIONS["AS:ZETTAS"] = \
        lambda value: (10 ** -39) * value
    CONVERSIONS["AS:ZS"] = \
        lambda value: (10 ** 3) * value
    CONVERSIONS["CS:AS"] = \
        lambda value: (10 ** 16) * value
    CONVERSIONS["CS:D"] = \
        lambda: noconversion("CS", "D")
    CONVERSIONS["CS:DAS"] = \
        lambda value: (10 ** -3) * value
    CONVERSIONS["CS:DS"] = \
        lambda value: (10 ** -1) * value
    CONVERSIONS["CS:EXAS"] = \
        lambda value: (10 ** -20) * value
    CONVERSIONS["CS:FS"] = \
        lambda value: (10 ** 13) * value
    CONVERSIONS["CS:GIGAS"] = \
        lambda value: (10 ** -11) * value
    CONVERSIONS["CS:H"] = \
        lambda: noconversion("CS", "H")
    CONVERSIONS["CS:HS"] = \
        lambda value: (10 ** -4) * value
    CONVERSIONS["CS:KS"] = \
        lambda value: (10 ** -5) * value
    CONVERSIONS["CS:MEGAS"] = \
        lambda value: (10 ** -8) * value
    CONVERSIONS["CS:MICROS"] = \
        lambda value: (10 ** 4) * value
    CONVERSIONS["CS:MIN"] = \
        lambda: noconversion("CS", "MIN")
    CONVERSIONS["CS:MS"] = \
        lambda value: 10 * value
    CONVERSIONS["CS:NS"] = \
        lambda value: (10 ** 7) * value
    CONVERSIONS["CS:PETAS"] = \
        lambda value: (10 ** -17) * value
    CONVERSIONS["CS:PS"] = \
        lambda value: (10 ** 10) * value
    CONVERSIONS["CS:S"] = \
        lambda value: (10 ** -2) * value
    CONVERSIONS["CS:TERAS"] = \
        lambda value: (10 ** -14) * value
    CONVERSIONS["CS:YOTTAS"] = \
        lambda value: (10 ** -26) * value
    CONVERSIONS["CS:YS"] = \
        lambda value: (10 ** 22) * value
    CONVERSIONS["CS:ZETTAS"] = \
        lambda value: (10 ** -23) * value
    CONVERSIONS["CS:ZS"] = \
        lambda value: (10 ** 19) * value
    CONVERSIONS["D:AS"] = \
        lambda: noconversion("D", "AS")
    CONVERSIONS["D:CS"] = \
        lambda: noconversion("D", "CS")
    CONVERSIONS["D:DAS"] = \
        lambda: noconversion("D", "DAS")
    CONVERSIONS["D:DS"] = \
        lambda: noconversion("D", "DS")
    CONVERSIONS["D:EXAS"] = \
        lambda: noconversion("D", "EXAS")
    CONVERSIONS["D:FS"] = \
        lambda: noconversion("D", "FS")
    CONVERSIONS["D:GIGAS"] = \
        lambda: noconversion("D", "GIGAS")
    CONVERSIONS["D:H"] = \
        lambda: noconversion("D", "H")
    CONVERSIONS["D:HS"] = \
        lambda: noconversion("D", "HS")
    CONVERSIONS["D:KS"] = \
        lambda: noconversion("D", "KS")
    CONVERSIONS["D:MEGAS"] = \
        lambda: noconversion("D", "MEGAS")
    CONVERSIONS["D:MICROS"] = \
        lambda: noconversion("D", "MICROS")
    CONVERSIONS["D:MIN"] = \
        lambda: noconversion("D", "MIN")
    CONVERSIONS["D:MS"] = \
        lambda: noconversion("D", "MS")
    CONVERSIONS["D:NS"] = \
        lambda: noconversion("D", "NS")
    CONVERSIONS["D:PETAS"] = \
        lambda: noconversion("D", "PETAS")
    CONVERSIONS["D:PS"] = \
        lambda: noconversion("D", "PS")
    CONVERSIONS["D:S"] = \
        lambda: noconversion("D", "S")
    CONVERSIONS["D:TERAS"] = \
        lambda: noconversion("D", "TERAS")
    CONVERSIONS["D:YOTTAS"] = \
        lambda: noconversion("D", "YOTTAS")
    CONVERSIONS["D:YS"] = \
        lambda: noconversion("D", "YS")
    CONVERSIONS["D:ZETTAS"] = \
        lambda: noconversion("D", "ZETTAS")
    CONVERSIONS["D:ZS"] = \
        lambda: noconversion("D", "ZS")
    CONVERSIONS["DAS:AS"] = \
        lambda value: (10 ** 19) * value
    CONVERSIONS["DAS:CS"] = \
        lambda value: (10 ** 3) * value
    CONVERSIONS["DAS:D"] = \
        lambda: noconversion("DAS", "D")
    CONVERSIONS["DAS:DS"] = \
        lambda value: (10 ** 2) * value
    CONVERSIONS["DAS:EXAS"] = \
        lambda value: (10 ** -17) * value
    CONVERSIONS["DAS:FS"] = \
        lambda value: (10 ** 16) * value
    CONVERSIONS["DAS:GIGAS"] = \
        lambda value: (10 ** -8) * value
    CONVERSIONS["DAS:H"] = \
        lambda: noconversion("DAS", "H")
    CONVERSIONS["DAS:HS"] = \
        lambda value: (10 ** -1) * value
    CONVERSIONS["DAS:KS"] = \
        lambda value: (10 ** -2) * value
    CONVERSIONS["DAS:MEGAS"] = \
        lambda value: (10 ** -5) * value
    CONVERSIONS["DAS:MICROS"] = \
        lambda value: (10 ** 7) * value
    CONVERSIONS["DAS:MIN"] = \
        lambda: noconversion("DAS", "MIN")
    CONVERSIONS["DAS:MS"] = \
        lambda value: (10 ** 4) * value
    CONVERSIONS["DAS:NS"] = \
        lambda value: (10 ** 10) * value
    CONVERSIONS["DAS:PETAS"] = \
        lambda value: (10 ** -14) * value
    CONVERSIONS["DAS:PS"] = \
        lambda value: (10 ** 13) * value
    CONVERSIONS["DAS:S"] = \
        lambda value: 10 * value
    CONVERSIONS["DAS:TERAS"] = \
        lambda value: (10 ** -11) * value
    CONVERSIONS["DAS:YOTTAS"] = \
        lambda value: (10 ** -23) * value
    CONVERSIONS["DAS:YS"] = \
        lambda value: (10 ** 25) * value
    CONVERSIONS["DAS:ZETTAS"] = \
        lambda value: (10 ** -20) * value
    CONVERSIONS["DAS:ZS"] = \
        lambda value: (10 ** 22) * value
    CONVERSIONS["DS:AS"] = \
        lambda value: (10 ** 17) * value
    CONVERSIONS["DS:CS"] = \
        lambda value: 10 * value
    CONVERSIONS["DS:D"] = \
        lambda: noconversion("DS", "D")
    CONVERSIONS["DS:DAS"] = \
        lambda value: (10 ** -2) * value
    CONVERSIONS["DS:EXAS"] = \
        lambda value: (10 ** -19) * value
    CONVERSIONS["DS:FS"] = \
        lambda value: (10 ** 14) * value
    CONVERSIONS["DS:GIGAS"] = \
        lambda value: (10 ** -10) * value
    CONVERSIONS["DS:H"] = \
        lambda: noconversion("DS", "H")
    CONVERSIONS["DS:HS"] = \
        lambda value: (10 ** -3) * value
    CONVERSIONS["DS:KS"] = \
        lambda value: (10 ** -4) * value
    CONVERSIONS["DS:MEGAS"] = \
        lambda value: (10 ** -7) * value
    CONVERSIONS["DS:MICROS"] = \
        lambda value: (10 ** 5) * value
    CONVERSIONS["DS:MIN"] = \
        lambda: noconversion("DS", "MIN")
    CONVERSIONS["DS:MS"] = \
        lambda value: (10 ** 2) * value
    CONVERSIONS["DS:NS"] = \
        lambda value: (10 ** 8) * value
    CONVERSIONS["DS:PETAS"] = \
        lambda value: (10 ** -16) * value
    CONVERSIONS["DS:PS"] = \
        lambda value: (10 ** 11) * value
    CONVERSIONS["DS:S"] = \
        lambda value: (10 ** -1) * value
    CONVERSIONS["DS:TERAS"] = \
        lambda value: (10 ** -13) * value
    CONVERSIONS["DS:YOTTAS"] = \
        lambda value: (10 ** -25) * value
    CONVERSIONS["DS:YS"] = \
        lambda value: (10 ** 23) * value
    CONVERSIONS["DS:ZETTAS"] = \
        lambda value: (10 ** -22) * value
    CONVERSIONS["DS:ZS"] = \
        lambda value: (10 ** 20) * value
    CONVERSIONS["EXAS:AS"] = \
        lambda value: (10 ** 36) * value
    CONVERSIONS["EXAS:CS"] = \
        lambda value: (10 ** 20) * value
    CONVERSIONS["EXAS:D"] = \
        lambda: noconversion("EXAS", "D")
    CONVERSIONS["EXAS:DAS"] = \
        lambda value: (10 ** 17) * value
    CONVERSIONS["EXAS:DS"] = \
        lambda value: (10 ** 19) * value
    CONVERSIONS["EXAS:FS"] = \
        lambda value: (10 ** 33) * value
    CONVERSIONS["EXAS:GIGAS"] = \
        lambda value: (10 ** 9) * value
    CONVERSIONS["EXAS:H"] = \
        lambda: noconversion("EXAS", "H")
    CONVERSIONS["EXAS:HS"] = \
        lambda value: (10 ** 16) * value
    CONVERSIONS["EXAS:KS"] = \
        lambda value: (10 ** 15) * value
    CONVERSIONS["EXAS:MEGAS"] = \
        lambda value: (10 ** 12) * value
    CONVERSIONS["EXAS:MICROS"] = \
        lambda value: (10 ** 24) * value
    CONVERSIONS["EXAS:MIN"] = \
        lambda: noconversion("EXAS", "MIN")
    CONVERSIONS["EXAS:MS"] = \
        lambda value: (10 ** 21) * value
    CONVERSIONS["EXAS:NS"] = \
        lambda value: (10 ** 27) * value
    CONVERSIONS["EXAS:PETAS"] = \
        lambda value: (10 ** 3) * value
    CONVERSIONS["EXAS:PS"] = \
        lambda value: (10 ** 30) * value
    CONVERSIONS["EXAS:S"] = \
        lambda value: (10 ** 18) * value
    CONVERSIONS["EXAS:TERAS"] = \
        lambda value: (10 ** 6) * value
    CONVERSIONS["EXAS:YOTTAS"] = \
        lambda value: (10 ** -6) * value
    CONVERSIONS["EXAS:YS"] = \
        lambda value: (10 ** 42) * value
    CONVERSIONS["EXAS:ZETTAS"] = \
        lambda value: (10 ** -3) * value
    CONVERSIONS["EXAS:ZS"] = \
        lambda value: (10 ** 39) * value
    CONVERSIONS["FS:AS"] = \
        lambda value: (10 ** 3) * value
    CONVERSIONS["FS:CS"] = \
        lambda value: (10 ** -13) * value
    CONVERSIONS["FS:D"] = \
        lambda: noconversion("FS", "D")
    CONVERSIONS["FS:DAS"] = \
        lambda value: (10 ** -16) * value
    CONVERSIONS["FS:DS"] = \
        lambda value: (10 ** -14) * value
    CONVERSIONS["FS:EXAS"] = \
        lambda value: (10 ** -33) * value
    CONVERSIONS["FS:GIGAS"] = \
        lambda value: (10 ** -24) * value
    CONVERSIONS["FS:H"] = \
        lambda: noconversion("FS", "H")
    CONVERSIONS["FS:HS"] = \
        lambda value: (10 ** -17) * value
    CONVERSIONS["FS:KS"] = \
        lambda value: (10 ** -18) * value
    CONVERSIONS["FS:MEGAS"] = \
        lambda value: (10 ** -21) * value
    CONVERSIONS["FS:MICROS"] = \
        lambda value: (10 ** -9) * value
    CONVERSIONS["FS:MIN"] = \
        lambda: noconversion("FS", "MIN")
    CONVERSIONS["FS:MS"] = \
        lambda value: (10 ** -12) * value
    CONVERSIONS["FS:NS"] = \
        lambda value: (10 ** -6) * value
    CONVERSIONS["FS:PETAS"] = \
        lambda value: (10 ** -30) * value
    CONVERSIONS["FS:PS"] = \
        lambda value: (10 ** -3) * value
    CONVERSIONS["FS:S"] = \
        lambda value: (10 ** -15) * value
    CONVERSIONS["FS:TERAS"] = \
        lambda value: (10 ** -27) * value
    CONVERSIONS["FS:YOTTAS"] = \
        lambda value: (10 ** -39) * value
    CONVERSIONS["FS:YS"] = \
        lambda value: (10 ** 9) * value
    CONVERSIONS["FS:ZETTAS"] = \
        lambda value: (10 ** -36) * value
    CONVERSIONS["FS:ZS"] = \
        lambda value: (10 ** 6) * value
    CONVERSIONS["GIGAS:AS"] = \
        lambda value: (10 ** 27) * value
    CONVERSIONS["GIGAS:CS"] = \
        lambda value: (10 ** 11) * value
    CONVERSIONS["GIGAS:D"] = \
        lambda: noconversion("GIGAS", "D")
    CONVERSIONS["GIGAS:DAS"] = \
        lambda value: (10 ** 8) * value
    CONVERSIONS["GIGAS:DS"] = \
        lambda value: (10 ** 10) * value
    CONVERSIONS["GIGAS:EXAS"] = \
        lambda value: (10 ** -9) * value
    CONVERSIONS["GIGAS:FS"] = \
        lambda value: (10 ** 24) * value
    CONVERSIONS["GIGAS:H"] = \
        lambda: noconversion("GIGAS", "H")
    CONVERSIONS["GIGAS:HS"] = \
        lambda value: (10 ** 7) * value
    CONVERSIONS["GIGAS:KS"] = \
        lambda value: (10 ** 6) * value
    CONVERSIONS["GIGAS:MEGAS"] = \
        lambda value: (10 ** 3) * value
    CONVERSIONS["GIGAS:MICROS"] = \
        lambda value: (10 ** 15) * value
    CONVERSIONS["GIGAS:MIN"] = \
        lambda: noconversion("GIGAS", "MIN")
    CONVERSIONS["GIGAS:MS"] = \
        lambda value: (10 ** 12) * value
    CONVERSIONS["GIGAS:NS"] = \
        lambda value: (10 ** 18) * value
    CONVERSIONS["GIGAS:PETAS"] = \
        lambda value: (10 ** -6) * value
    CONVERSIONS["GIGAS:PS"] = \
        lambda value: (10 ** 21) * value
    CONVERSIONS["GIGAS:S"] = \
        lambda value: (10 ** 9) * value
    CONVERSIONS["GIGAS:TERAS"] = \
        lambda value: (10 ** -3) * value
    CONVERSIONS["GIGAS:YOTTAS"] = \
        lambda value: (10 ** -15) * value
    CONVERSIONS["GIGAS:YS"] = \
        lambda value: (10 ** 33) * value
    CONVERSIONS["GIGAS:ZETTAS"] = \
        lambda value: (10 ** -12) * value
    CONVERSIONS["GIGAS:ZS"] = \
        lambda value: (10 ** 30) * value
    CONVERSIONS["H:AS"] = \
        lambda: noconversion("H", "AS")
    CONVERSIONS["H:CS"] = \
        lambda: noconversion("H", "CS")
    CONVERSIONS["H:D"] = \
        lambda: noconversion("H", "D")
    CONVERSIONS["H:DAS"] = \
        lambda: noconversion("H", "DAS")
    CONVERSIONS["H:DS"] = \
        lambda: noconversion("H", "DS")
    CONVERSIONS["H:EXAS"] = \
        lambda: noconversion("H", "EXAS")
    CONVERSIONS["H:FS"] = \
        lambda: noconversion("H", "FS")
    CONVERSIONS["H:GIGAS"] = \
        lambda: noconversion("H", "GIGAS")
    CONVERSIONS["H:HS"] = \
        lambda: noconversion("H", "HS")
    CONVERSIONS["H:KS"] = \
        lambda: noconversion("H", "KS")
    CONVERSIONS["H:MEGAS"] = \
        lambda: noconversion("H", "MEGAS")
    CONVERSIONS["H:MICROS"] = \
        lambda: noconversion("H", "MICROS")
    CONVERSIONS["H:MIN"] = \
        lambda: noconversion("H", "MIN")
    CONVERSIONS["H:MS"] = \
        lambda: noconversion("H", "MS")
    CONVERSIONS["H:NS"] = \
        lambda: noconversion("H", "NS")
    CONVERSIONS["H:PETAS"] = \
        lambda: noconversion("H", "PETAS")
    CONVERSIONS["H:PS"] = \
        lambda: noconversion("H", "PS")
    CONVERSIONS["H:S"] = \
        lambda: noconversion("H", "S")
    CONVERSIONS["H:TERAS"] = \
        lambda: noconversion("H", "TERAS")
    CONVERSIONS["H:YOTTAS"] = \
        lambda: noconversion("H", "YOTTAS")
    CONVERSIONS["H:YS"] = \
        lambda: noconversion("H", "YS")
    CONVERSIONS["H:ZETTAS"] = \
        lambda: noconversion("H", "ZETTAS")
    CONVERSIONS["H:ZS"] = \
        lambda: noconversion("H", "ZS")
    CONVERSIONS["HS:AS"] = \
        lambda value: (10 ** 20) * value
    CONVERSIONS["HS:CS"] = \
        lambda value: (10 ** 4) * value
    CONVERSIONS["HS:D"] = \
        lambda: noconversion("HS", "D")
    CONVERSIONS["HS:DAS"] = \
        lambda value: 10 * value
    CONVERSIONS["HS:DS"] = \
        lambda value: (10 ** 3) * value
    CONVERSIONS["HS:EXAS"] = \
        lambda value: (10 ** -16) * value
    CONVERSIONS["HS:FS"] = \
        lambda value: (10 ** 17) * value
    CONVERSIONS["HS:GIGAS"] = \
        lambda value: (10 ** -7) * value
    CONVERSIONS["HS:H"] = \
        lambda: noconversion("HS", "H")
    CONVERSIONS["HS:KS"] = \
        lambda value: (10 ** -1) * value
    CONVERSIONS["HS:MEGAS"] = \
        lambda value: (10 ** -4) * value
    CONVERSIONS["HS:MICROS"] = \
        lambda value: (10 ** 8) * value
    CONVERSIONS["HS:MIN"] = \
        lambda: noconversion("HS", "MIN")
    CONVERSIONS["HS:MS"] = \
        lambda value: (10 ** 5) * value
    CONVERSIONS["HS:NS"] = \
        lambda value: (10 ** 11) * value
    CONVERSIONS["HS:PETAS"] = \
        lambda value: (10 ** -13) * value
    CONVERSIONS["HS:PS"] = \
        lambda value: (10 ** 14) * value
    CONVERSIONS["HS:S"] = \
        lambda value: (10 ** 2) * value
    CONVERSIONS["HS:TERAS"] = \
        lambda value: (10 ** -10) * value
    CONVERSIONS["HS:YOTTAS"] = \
        lambda value: (10 ** -22) * value
    CONVERSIONS["HS:YS"] = \
        lambda value: (10 ** 26) * value
    CONVERSIONS["HS:ZETTAS"] = \
        lambda value: (10 ** -19) * value
    CONVERSIONS["HS:ZS"] = \
        lambda value: (10 ** 23) * value
    CONVERSIONS["KS:AS"] = \
        lambda value: (10 ** 21) * value
    CONVERSIONS["KS:CS"] = \
        lambda value: (10 ** 5) * value
    CONVERSIONS["KS:D"] = \
        lambda: noconversion("KS", "D")
    CONVERSIONS["KS:DAS"] = \
        lambda value: (10 ** 2) * value
    CONVERSIONS["KS:DS"] = \
        lambda value: (10 ** 4) * value
    CONVERSIONS["KS:EXAS"] = \
        lambda value: (10 ** -15) * value
    CONVERSIONS["KS:FS"] = \
        lambda value: (10 ** 18) * value
    CONVERSIONS["KS:GIGAS"] = \
        lambda value: (10 ** -6) * value
    CONVERSIONS["KS:H"] = \
        lambda: noconversion("KS", "H")
    CONVERSIONS["KS:HS"] = \
        lambda value: 10 * value
    CONVERSIONS["KS:MEGAS"] = \
        lambda value: (10 ** -3) * value
    CONVERSIONS["KS:MICROS"] = \
        lambda value: (10 ** 9) * value
    CONVERSIONS["KS:MIN"] = \
        lambda: noconversion("KS", "MIN")
    CONVERSIONS["KS:MS"] = \
        lambda value: (10 ** 6) * value
    CONVERSIONS["KS:NS"] = \
        lambda value: (10 ** 12) * value
    CONVERSIONS["KS:PETAS"] = \
        lambda value: (10 ** -12) * value
    CONVERSIONS["KS:PS"] = \
        lambda value: (10 ** 15) * value
    CONVERSIONS["KS:S"] = \
        lambda value: (10 ** 3) * value
    CONVERSIONS["KS:TERAS"] = \
        lambda value: (10 ** -9) * value
    CONVERSIONS["KS:YOTTAS"] = \
        lambda value: (10 ** -21) * value
    CONVERSIONS["KS:YS"] = \
        lambda value: (10 ** 27) * value
    CONVERSIONS["KS:ZETTAS"] = \
        lambda value: (10 ** -18) * value
    CONVERSIONS["KS:ZS"] = \
        lambda value: (10 ** 24) * value
    CONVERSIONS["MEGAS:AS"] = \
        lambda value: (10 ** 24) * value
    CONVERSIONS["MEGAS:CS"] = \
        lambda value: (10 ** 8) * value
    CONVERSIONS["MEGAS:D"] = \
        lambda: noconversion("MEGAS", "D")
    CONVERSIONS["MEGAS:DAS"] = \
        lambda value: (10 ** 5) * value
    CONVERSIONS["MEGAS:DS"] = \
        lambda value: (10 ** 7) * value
    CONVERSIONS["MEGAS:EXAS"] = \
        lambda value: (10 ** -12) * value
    CONVERSIONS["MEGAS:FS"] = \
        lambda value: (10 ** 21) * value
    CONVERSIONS["MEGAS:GIGAS"] = \
        lambda value: (10 ** -3) * value
    CONVERSIONS["MEGAS:H"] = \
        lambda: noconversion("MEGAS", "H")
    CONVERSIONS["MEGAS:HS"] = \
        lambda value: (10 ** 4) * value
    CONVERSIONS["MEGAS:KS"] = \
        lambda value: (10 ** 3) * value
    CONVERSIONS["MEGAS:MICROS"] = \
        lambda value: (10 ** 12) * value
    CONVERSIONS["MEGAS:MIN"] = \
        lambda: noconversion("MEGAS", "MIN")
    CONVERSIONS["MEGAS:MS"] = \
        lambda value: (10 ** 9) * value
    CONVERSIONS["MEGAS:NS"] = \
        lambda value: (10 ** 15) * value
    CONVERSIONS["MEGAS:PETAS"] = \
        lambda value: (10 ** -9) * value
    CONVERSIONS["MEGAS:PS"] = \
        lambda value: (10 ** 18) * value
    CONVERSIONS["MEGAS:S"] = \
        lambda value: (10 ** 6) * value
    CONVERSIONS["MEGAS:TERAS"] = \
        lambda value: (10 ** -6) * value
    CONVERSIONS["MEGAS:YOTTAS"] = \
        lambda value: (10 ** -18) * value
    CONVERSIONS["MEGAS:YS"] = \
        lambda value: (10 ** 30) * value
    CONVERSIONS["MEGAS:ZETTAS"] = \
        lambda value: (10 ** -15) * value
    CONVERSIONS["MEGAS:ZS"] = \
        lambda value: (10 ** 27) * value
    CONVERSIONS["MICROS:AS"] = \
        lambda value: (10 ** 12) * value
    CONVERSIONS["MICROS:CS"] = \
        lambda value: (10 ** -4) * value
    CONVERSIONS["MICROS:D"] = \
        lambda: noconversion("MICROS", "D")
    CONVERSIONS["MICROS:DAS"] = \
        lambda value: (10 ** -7) * value
    CONVERSIONS["MICROS:DS"] = \
        lambda value: (10 ** -5) * value
    CONVERSIONS["MICROS:EXAS"] = \
        lambda value: (10 ** -24) * value
    CONVERSIONS["MICROS:FS"] = \
        lambda value: (10 ** 9) * value
    CONVERSIONS["MICROS:GIGAS"] = \
        lambda value: (10 ** -15) * value
    CONVERSIONS["MICROS:H"] = \
        lambda: noconversion("MICROS", "H")
    CONVERSIONS["MICROS:HS"] = \
        lambda value: (10 ** -8) * value
    CONVERSIONS["MICROS:KS"] = \
        lambda value: (10 ** -9) * value
    CONVERSIONS["MICROS:MEGAS"] = \
        lambda value: (10 ** -12) * value
    CONVERSIONS["MICROS:MIN"] = \
        lambda: noconversion("MICROS", "MIN")
    CONVERSIONS["MICROS:MS"] = \
        lambda value: (10 ** -3) * value
    CONVERSIONS["MICROS:NS"] = \
        lambda value: (10 ** 3) * value
    CONVERSIONS["MICROS:PETAS"] = \
        lambda value: (10 ** -21) * value
    CONVERSIONS["MICROS:PS"] = \
        lambda value: (10 ** 6) * value
    CONVERSIONS["MICROS:S"] = \
        lambda value: (10 ** -6) * value
    CONVERSIONS["MICROS:TERAS"] = \
        lambda value: (10 ** -18) * value
    CONVERSIONS["MICROS:YOTTAS"] = \
        lambda value: (10 ** -30) * value
    CONVERSIONS["MICROS:YS"] = \
        lambda value: (10 ** 18) * value
    CONVERSIONS["MICROS:ZETTAS"] = \
        lambda value: (10 ** -27) * value
    CONVERSIONS["MICROS:ZS"] = \
        lambda value: (10 ** 15) * value
    CONVERSIONS["MIN:AS"] = \
        lambda: noconversion("MIN", "AS")
    CONVERSIONS["MIN:CS"] = \
        lambda: noconversion("MIN", "CS")
    CONVERSIONS["MIN:D"] = \
        lambda: noconversion("MIN", "D")
    CONVERSIONS["MIN:DAS"] = \
        lambda: noconversion("MIN", "DAS")
    CONVERSIONS["MIN:DS"] = \
        lambda: noconversion("MIN", "DS")
    CONVERSIONS["MIN:EXAS"] = \
        lambda: noconversion("MIN", "EXAS")
    CONVERSIONS["MIN:FS"] = \
        lambda: noconversion("MIN", "FS")
    CONVERSIONS["MIN:GIGAS"] = \
        lambda: noconversion("MIN", "GIGAS")
    CONVERSIONS["MIN:H"] = \
        lambda: noconversion("MIN", "H")
    CONVERSIONS["MIN:HS"] = \
        lambda: noconversion("MIN", "HS")
    CONVERSIONS["MIN:KS"] = \
        lambda: noconversion("MIN", "KS")
    CONVERSIONS["MIN:MEGAS"] = \
        lambda: noconversion("MIN", "MEGAS")
    CONVERSIONS["MIN:MICROS"] = \
        lambda: noconversion("MIN", "MICROS")
    CONVERSIONS["MIN:MS"] = \
        lambda: noconversion("MIN", "MS")
    CONVERSIONS["MIN:NS"] = \
        lambda: noconversion("MIN", "NS")
    CONVERSIONS["MIN:PETAS"] = \
        lambda: noconversion("MIN", "PETAS")
    CONVERSIONS["MIN:PS"] = \
        lambda: noconversion("MIN", "PS")
    CONVERSIONS["MIN:S"] = \
        lambda: noconversion("MIN", "S")
    CONVERSIONS["MIN:TERAS"] = \
        lambda: noconversion("MIN", "TERAS")
    CONVERSIONS["MIN:YOTTAS"] = \
        lambda: noconversion("MIN", "YOTTAS")
    CONVERSIONS["MIN:YS"] = \
        lambda: noconversion("MIN", "YS")
    CONVERSIONS["MIN:ZETTAS"] = \
        lambda: noconversion("MIN", "ZETTAS")
    CONVERSIONS["MIN:ZS"] = \
        lambda: noconversion("MIN", "ZS")
    CONVERSIONS["MS:AS"] = \
        lambda value: (10 ** 15) * value
    CONVERSIONS["MS:CS"] = \
        lambda value: (10 ** -1) * value
    CONVERSIONS["MS:D"] = \
        lambda: noconversion("MS", "D")
    CONVERSIONS["MS:DAS"] = \
        lambda value: (10 ** -4) * value
    CONVERSIONS["MS:DS"] = \
        lambda value: (10 ** -2) * value
    CONVERSIONS["MS:EXAS"] = \
        lambda value: (10 ** -21) * value
    CONVERSIONS["MS:FS"] = \
        lambda value: (10 ** 12) * value
    CONVERSIONS["MS:GIGAS"] = \
        lambda value: (10 ** -12) * value
    CONVERSIONS["MS:H"] = \
        lambda: noconversion("MS", "H")
    CONVERSIONS["MS:HS"] = \
        lambda value: (10 ** -5) * value
    CONVERSIONS["MS:KS"] = \
        lambda value: (10 ** -6) * value
    CONVERSIONS["MS:MEGAS"] = \
        lambda value: (10 ** -9) * value
    CONVERSIONS["MS:MICROS"] = \
        lambda value: (10 ** 3) * value
    CONVERSIONS["MS:MIN"] = \
        lambda: noconversion("MS", "MIN")
    CONVERSIONS["MS:NS"] = \
        lambda value: (10 ** 6) * value
    CONVERSIONS["MS:PETAS"] = \
        lambda value: (10 ** -18) * value
    CONVERSIONS["MS:PS"] = \
        lambda value: (10 ** 9) * value
    CONVERSIONS["MS:S"] = \
        lambda value: (10 ** -3) * value
    CONVERSIONS["MS:TERAS"] = \
        lambda value: (10 ** -15) * value
    CONVERSIONS["MS:YOTTAS"] = \
        lambda value: (10 ** -27) * value
    CONVERSIONS["MS:YS"] = \
        lambda value: (10 ** 21) * value
    CONVERSIONS["MS:ZETTAS"] = \
        lambda value: (10 ** -24) * value
    CONVERSIONS["MS:ZS"] = \
        lambda value: (10 ** 18) * value
    CONVERSIONS["NS:AS"] = \
        lambda value: (10 ** 9) * value
    CONVERSIONS["NS:CS"] = \
        lambda value: (10 ** -7) * value
    CONVERSIONS["NS:D"] = \
        lambda: noconversion("NS", "D")
    CONVERSIONS["NS:DAS"] = \
        lambda value: (10 ** -10) * value
    CONVERSIONS["NS:DS"] = \
        lambda value: (10 ** -8) * value
    CONVERSIONS["NS:EXAS"] = \
        lambda value: (10 ** -27) * value
    CONVERSIONS["NS:FS"] = \
        lambda value: (10 ** 6) * value
    CONVERSIONS["NS:GIGAS"] = \
        lambda value: (10 ** -18) * value
    CONVERSIONS["NS:H"] = \
        lambda: noconversion("NS", "H")
    CONVERSIONS["NS:HS"] = \
        lambda value: (10 ** -11) * value
    CONVERSIONS["NS:KS"] = \
        lambda value: (10 ** -12) * value
    CONVERSIONS["NS:MEGAS"] = \
        lambda value: (10 ** -15) * value
    CONVERSIONS["NS:MICROS"] = \
        lambda value: (10 ** -3) * value
    CONVERSIONS["NS:MIN"] = \
        lambda: noconversion("NS", "MIN")
    CONVERSIONS["NS:MS"] = \
        lambda value: (10 ** -6) * value
    CONVERSIONS["NS:PETAS"] = \
        lambda value: (10 ** -24) * value
    CONVERSIONS["NS:PS"] = \
        lambda value: (10 ** 3) * value
    CONVERSIONS["NS:S"] = \
        lambda value: (10 ** -9) * value
    CONVERSIONS["NS:TERAS"] = \
        lambda value: (10 ** -21) * value
    CONVERSIONS["NS:YOTTAS"] = \
        lambda value: (10 ** -33) * value
    CONVERSIONS["NS:YS"] = \
        lambda value: (10 ** 15) * value
    CONVERSIONS["NS:ZETTAS"] = \
        lambda value: (10 ** -30) * value
    CONVERSIONS["NS:ZS"] = \
        lambda value: (10 ** 12) * value
    CONVERSIONS["PETAS:AS"] = \
        lambda value: (10 ** 33) * value
    CONVERSIONS["PETAS:CS"] = \
        lambda value: (10 ** 17) * value
    CONVERSIONS["PETAS:D"] = \
        lambda: noconversion("PETAS", "D")
    CONVERSIONS["PETAS:DAS"] = \
        lambda value: (10 ** 14) * value
    CONVERSIONS["PETAS:DS"] = \
        lambda value: (10 ** 16) * value
    CONVERSIONS["PETAS:EXAS"] = \
        lambda value: (10 ** -3) * value
    CONVERSIONS["PETAS:FS"] = \
        lambda value: (10 ** 30) * value
    CONVERSIONS["PETAS:GIGAS"] = \
        lambda value: (10 ** 6) * value
    CONVERSIONS["PETAS:H"] = \
        lambda: noconversion("PETAS", "H")
    CONVERSIONS["PETAS:HS"] = \
        lambda value: (10 ** 13) * value
    CONVERSIONS["PETAS:KS"] = \
        lambda value: (10 ** 12) * value
    CONVERSIONS["PETAS:MEGAS"] = \
        lambda value: (10 ** 9) * value
    CONVERSIONS["PETAS:MICROS"] = \
        lambda value: (10 ** 21) * value
    CONVERSIONS["PETAS:MIN"] = \
        lambda: noconversion("PETAS", "MIN")
    CONVERSIONS["PETAS:MS"] = \
        lambda value: (10 ** 18) * value
    CONVERSIONS["PETAS:NS"] = \
        lambda value: (10 ** 24) * value
    CONVERSIONS["PETAS:PS"] = \
        lambda value: (10 ** 27) * value
    CONVERSIONS["PETAS:S"] = \
        lambda value: (10 ** 15) * value
    CONVERSIONS["PETAS:TERAS"] = \
        lambda value: (10 ** 3) * value
    CONVERSIONS["PETAS:YOTTAS"] = \
        lambda value: (10 ** -9) * value
    CONVERSIONS["PETAS:YS"] = \
        lambda value: (10 ** 39) * value
    CONVERSIONS["PETAS:ZETTAS"] = \
        lambda value: (10 ** -6) * value
    CONVERSIONS["PETAS:ZS"] = \
        lambda value: (10 ** 36) * value
    CONVERSIONS["PS:AS"] = \
        lambda value: (10 ** 6) * value
    CONVERSIONS["PS:CS"] = \
        lambda value: (10 ** -10) * value
    CONVERSIONS["PS:D"] = \
        lambda: noconversion("PS", "D")
    CONVERSIONS["PS:DAS"] = \
        lambda value: (10 ** -13) * value
    CONVERSIONS["PS:DS"] = \
        lambda value: (10 ** -11) * value
    CONVERSIONS["PS:EXAS"] = \
        lambda value: (10 ** -30) * value
    CONVERSIONS["PS:FS"] = \
        lambda value: (10 ** 3) * value
    CONVERSIONS["PS:GIGAS"] = \
        lambda value: (10 ** -21) * value
    CONVERSIONS["PS:H"] = \
        lambda: noconversion("PS", "H")
    CONVERSIONS["PS:HS"] = \
        lambda value: (10 ** -14) * value
    CONVERSIONS["PS:KS"] = \
        lambda value: (10 ** -15) * value
    CONVERSIONS["PS:MEGAS"] = \
        lambda value: (10 ** -18) * value
    CONVERSIONS["PS:MICROS"] = \
        lambda value: (10 ** -6) * value
    CONVERSIONS["PS:MIN"] = \
        lambda: noconversion("PS", "MIN")
    CONVERSIONS["PS:MS"] = \
        lambda value: (10 ** -9) * value
    CONVERSIONS["PS:NS"] = \
        lambda value: (10 ** -3) * value
    CONVERSIONS["PS:PETAS"] = \
        lambda value: (10 ** -27) * value
    CONVERSIONS["PS:S"] = \
        lambda value: (10 ** -12) * value
    CONVERSIONS["PS:TERAS"] = \
        lambda value: (10 ** -24) * value
    CONVERSIONS["PS:YOTTAS"] = \
        lambda value: (10 ** -36) * value
    CONVERSIONS["PS:YS"] = \
        lambda value: (10 ** 12) * value
    CONVERSIONS["PS:ZETTAS"] = \
        lambda value: (10 ** -33) * value
    CONVERSIONS["PS:ZS"] = \
        lambda value: (10 ** 9) * value
    CONVERSIONS["S:AS"] = \
        lambda value: (10 ** 18) * value
    CONVERSIONS["S:CS"] = \
        lambda value: (10 ** 2) * value
    CONVERSIONS["S:D"] = \
        lambda: noconversion("S", "D")
    CONVERSIONS["S:DAS"] = \
        lambda value: (10 ** -1) * value
    CONVERSIONS["S:DS"] = \
        lambda value: 10 * value
    CONVERSIONS["S:EXAS"] = \
        lambda value: (10 ** -18) * value
    CONVERSIONS["S:FS"] = \
        lambda value: (10 ** 15) * value
    CONVERSIONS["S:GIGAS"] = \
        lambda value: (10 ** -9) * value
    CONVERSIONS["S:H"] = \
        lambda: noconversion("S", "H")
    CONVERSIONS["S:HS"] = \
        lambda value: (10 ** -2) * value
    CONVERSIONS["S:KS"] = \
        lambda value: (10 ** -3) * value
    CONVERSIONS["S:MEGAS"] = \
        lambda value: (10 ** -6) * value
    CONVERSIONS["S:MICROS"] = \
        lambda value: (10 ** 6) * value
    CONVERSIONS["S:MIN"] = \
        lambda: noconversion("S", "MIN")
    CONVERSIONS["S:MS"] = \
        lambda value: (10 ** 3) * value
    CONVERSIONS["S:NS"] = \
        lambda value: (10 ** 9) * value
    CONVERSIONS["S:PETAS"] = \
        lambda value: (10 ** -15) * value
    CONVERSIONS["S:PS"] = \
        lambda value: (10 ** 12) * value
    CONVERSIONS["S:TERAS"] = \
        lambda value: (10 ** -12) * value
    CONVERSIONS["S:YOTTAS"] = \
        lambda value: (10 ** -24) * value
    CONVERSIONS["S:YS"] = \
        lambda value: (10 ** 24) * value
    CONVERSIONS["S:ZETTAS"] = \
        lambda value: (10 ** -21) * value
    CONVERSIONS["S:ZS"] = \
        lambda value: (10 ** 21) * value
    CONVERSIONS["TERAS:AS"] = \
        lambda value: (10 ** 30) * value
    CONVERSIONS["TERAS:CS"] = \
        lambda value: (10 ** 14) * value
    CONVERSIONS["TERAS:D"] = \
        lambda: noconversion("TERAS", "D")
    CONVERSIONS["TERAS:DAS"] = \
        lambda value: (10 ** 11) * value
    CONVERSIONS["TERAS:DS"] = \
        lambda value: (10 ** 13) * value
    CONVERSIONS["TERAS:EXAS"] = \
        lambda value: (10 ** -6) * value
    CONVERSIONS["TERAS:FS"] = \
        lambda value: (10 ** 27) * value
    CONVERSIONS["TERAS:GIGAS"] = \
        lambda value: (10 ** 3) * value
    CONVERSIONS["TERAS:H"] = \
        lambda: noconversion("TERAS", "H")
    CONVERSIONS["TERAS:HS"] = \
        lambda value: (10 ** 10) * value
    CONVERSIONS["TERAS:KS"] = \
        lambda value: (10 ** 9) * value
    CONVERSIONS["TERAS:MEGAS"] = \
        lambda value: (10 ** 6) * value
    CONVERSIONS["TERAS:MICROS"] = \
        lambda value: (10 ** 18) * value
    CONVERSIONS["TERAS:MIN"] = \
        lambda: noconversion("TERAS", "MIN")
    CONVERSIONS["TERAS:MS"] = \
        lambda value: (10 ** 15) * value
    CONVERSIONS["TERAS:NS"] = \
        lambda value: (10 ** 21) * value
    CONVERSIONS["TERAS:PETAS"] = \
        lambda value: (10 ** -3) * value
    CONVERSIONS["TERAS:PS"] = \
        lambda value: (10 ** 24) * value
    CONVERSIONS["TERAS:S"] = \
        lambda value: (10 ** 12) * value
    CONVERSIONS["TERAS:YOTTAS"] = \
        lambda value: (10 ** -12) * value
    CONVERSIONS["TERAS:YS"] = \
        lambda value: (10 ** 36) * value
    CONVERSIONS["TERAS:ZETTAS"] = \
        lambda value: (10 ** -9) * value
    CONVERSIONS["TERAS:ZS"] = \
        lambda value: (10 ** 33) * value
    CONVERSIONS["YOTTAS:AS"] = \
        lambda value: (10 ** 42) * value
    CONVERSIONS["YOTTAS:CS"] = \
        lambda value: (10 ** 26) * value
    CONVERSIONS["YOTTAS:D"] = \
        lambda: noconversion("YOTTAS", "D")
    CONVERSIONS["YOTTAS:DAS"] = \
        lambda value: (10 ** 23) * value
    CONVERSIONS["YOTTAS:DS"] = \
        lambda value: (10 ** 25) * value
    CONVERSIONS["YOTTAS:EXAS"] = \
        lambda value: (10 ** 6) * value
    CONVERSIONS["YOTTAS:FS"] = \
        lambda value: (10 ** 39) * value
    CONVERSIONS["YOTTAS:GIGAS"] = \
        lambda value: (10 ** 15) * value
    CONVERSIONS["YOTTAS:H"] = \
        lambda: noconversion("YOTTAS", "H")
    CONVERSIONS["YOTTAS:HS"] = \
        lambda value: (10 ** 22) * value
    CONVERSIONS["YOTTAS:KS"] = \
        lambda value: (10 ** 21) * value
    CONVERSIONS["YOTTAS:MEGAS"] = \
        lambda value: (10 ** 18) * value
    CONVERSIONS["YOTTAS:MICROS"] = \
        lambda value: (10 ** 30) * value
    CONVERSIONS["YOTTAS:MIN"] = \
        lambda: noconversion("YOTTAS", "MIN")
    CONVERSIONS["YOTTAS:MS"] = \
        lambda value: (10 ** 27) * value
    CONVERSIONS["YOTTAS:NS"] = \
        lambda value: (10 ** 33) * value
    CONVERSIONS["YOTTAS:PETAS"] = \
        lambda value: (10 ** 9) * value
    CONVERSIONS["YOTTAS:PS"] = \
        lambda value: (10 ** 36) * value
    CONVERSIONS["YOTTAS:S"] = \
        lambda value: (10 ** 24) * value
    CONVERSIONS["YOTTAS:TERAS"] = \
        lambda value: (10 ** 12) * value
    CONVERSIONS["YOTTAS:YS"] = \
        lambda value: (10 ** 48) * value
    CONVERSIONS["YOTTAS:ZETTAS"] = \
        lambda value: (10 ** 3) * value
    CONVERSIONS["YOTTAS:ZS"] = \
        lambda value: (10 ** 45) * value
    CONVERSIONS["YS:AS"] = \
        lambda value: (10 ** -6) * value
    CONVERSIONS["YS:CS"] = \
        lambda value: (10 ** -22) * value
    CONVERSIONS["YS:D"] = \
        lambda: noconversion("YS", "D")
    CONVERSIONS["YS:DAS"] = \
        lambda value: (10 ** -25) * value
    CONVERSIONS["YS:DS"] = \
        lambda value: (10 ** -23) * value
    CONVERSIONS["YS:EXAS"] = \
        lambda value: (10 ** -42) * value
    CONVERSIONS["YS:FS"] = \
        lambda value: (10 ** -9) * value
    CONVERSIONS["YS:GIGAS"] = \
        lambda value: (10 ** -33) * value
    CONVERSIONS["YS:H"] = \
        lambda: noconversion("YS", "H")
    CONVERSIONS["YS:HS"] = \
        lambda value: (10 ** -26) * value
    CONVERSIONS["YS:KS"] = \
        lambda value: (10 ** -27) * value
    CONVERSIONS["YS:MEGAS"] = \
        lambda value: (10 ** -30) * value
    CONVERSIONS["YS:MICROS"] = \
        lambda value: (10 ** -18) * value
    CONVERSIONS["YS:MIN"] = \
        lambda: noconversion("YS", "MIN")
    CONVERSIONS["YS:MS"] = \
        lambda value: (10 ** -21) * value
    CONVERSIONS["YS:NS"] = \
        lambda value: (10 ** -15) * value
    CONVERSIONS["YS:PETAS"] = \
        lambda value: (10 ** -39) * value
    CONVERSIONS["YS:PS"] = \
        lambda value: (10 ** -12) * value
    CONVERSIONS["YS:S"] = \
        lambda value: (10 ** -24) * value
    CONVERSIONS["YS:TERAS"] = \
        lambda value: (10 ** -36) * value
    CONVERSIONS["YS:YOTTAS"] = \
        lambda value: (10 ** -48) * value
    CONVERSIONS["YS:ZETTAS"] = \
        lambda value: (10 ** -45) * value
    CONVERSIONS["YS:ZS"] = \
        lambda value: (10 ** -3) * value
    CONVERSIONS["ZETTAS:AS"] = \
        lambda value: (10 ** 39) * value
    CONVERSIONS["ZETTAS:CS"] = \
        lambda value: (10 ** 23) * value
    CONVERSIONS["ZETTAS:D"] = \
        lambda: noconversion("ZETTAS", "D")
    CONVERSIONS["ZETTAS:DAS"] = \
        lambda value: (10 ** 20) * value
    CONVERSIONS["ZETTAS:DS"] = \
        lambda value: (10 ** 22) * value
    CONVERSIONS["ZETTAS:EXAS"] = \
        lambda value: (10 ** 3) * value
    CONVERSIONS["ZETTAS:FS"] = \
        lambda value: (10 ** 36) * value
    CONVERSIONS["ZETTAS:GIGAS"] = \
        lambda value: (10 ** 12) * value
    CONVERSIONS["ZETTAS:H"] = \
        lambda: noconversion("ZETTAS", "H")
    CONVERSIONS["ZETTAS:HS"] = \
        lambda value: (10 ** 19) * value
    CONVERSIONS["ZETTAS:KS"] = \
        lambda value: (10 ** 18) * value
    CONVERSIONS["ZETTAS:MEGAS"] = \
        lambda value: (10 ** 15) * value
    CONVERSIONS["ZETTAS:MICROS"] = \
        lambda value: (10 ** 27) * value
    CONVERSIONS["ZETTAS:MIN"] = \
        lambda: noconversion("ZETTAS", "MIN")
    CONVERSIONS["ZETTAS:MS"] = \
        lambda value: (10 ** 24) * value
    CONVERSIONS["ZETTAS:NS"] = \
        lambda value: (10 ** 30) * value
    CONVERSIONS["ZETTAS:PETAS"] = \
        lambda value: (10 ** 6) * value
    CONVERSIONS["ZETTAS:PS"] = \
        lambda value: (10 ** 33) * value
    CONVERSIONS["ZETTAS:S"] = \
        lambda value: (10 ** 21) * value
    CONVERSIONS["ZETTAS:TERAS"] = \
        lambda value: (10 ** 9) * value
    CONVERSIONS["ZETTAS:YOTTAS"] = \
        lambda value: (10 ** -3) * value
    CONVERSIONS["ZETTAS:YS"] = \
        lambda value: (10 ** 45) * value
    CONVERSIONS["ZETTAS:ZS"] = \
        lambda value: (10 ** 42) * value
    CONVERSIONS["ZS:AS"] = \
        lambda value: (10 ** -3) * value
    CONVERSIONS["ZS:CS"] = \
        lambda value: (10 ** -19) * value
    CONVERSIONS["ZS:D"] = \
        lambda: noconversion("ZS", "D")
    CONVERSIONS["ZS:DAS"] = \
        lambda value: (10 ** -22) * value
    CONVERSIONS["ZS:DS"] = \
        lambda value: (10 ** -20) * value
    CONVERSIONS["ZS:EXAS"] = \
        lambda value: (10 ** -39) * value
    CONVERSIONS["ZS:FS"] = \
        lambda value: (10 ** -6) * value
    CONVERSIONS["ZS:GIGAS"] = \
        lambda value: (10 ** -30) * value
    CONVERSIONS["ZS:H"] = \
        lambda: noconversion("ZS", "H")
    CONVERSIONS["ZS:HS"] = \
        lambda value: (10 ** -23) * value
    CONVERSIONS["ZS:KS"] = \
        lambda value: (10 ** -24) * value
    CONVERSIONS["ZS:MEGAS"] = \
        lambda value: (10 ** -27) * value
    CONVERSIONS["ZS:MICROS"] = \
        lambda value: (10 ** -15) * value
    CONVERSIONS["ZS:MIN"] = \
        lambda: noconversion("ZS", "MIN")
    CONVERSIONS["ZS:MS"] = \
        lambda value: (10 ** -18) * value
    CONVERSIONS["ZS:NS"] = \
        lambda value: (10 ** -12) * value
    CONVERSIONS["ZS:PETAS"] = \
        lambda value: (10 ** -36) * value
    CONVERSIONS["ZS:PS"] = \
        lambda value: (10 ** -9) * value
    CONVERSIONS["ZS:S"] = \
        lambda value: (10 ** -21) * value
    CONVERSIONS["ZS:TERAS"] = \
        lambda value: (10 ** -33) * value
    CONVERSIONS["ZS:YOTTAS"] = \
        lambda value: (10 ** -45) * value
    CONVERSIONS["ZS:YS"] = \
        lambda value: (10 ** 3) * value
    CONVERSIONS["ZS:ZETTAS"] = \
        lambda value: (10 ** -42) * value

    SYMBOLS = dict()
    SYMBOLS["AS"] = "as"
    SYMBOLS["CS"] = "cs"
    SYMBOLS["D"] = "d"
    SYMBOLS["DAS"] = "das"
    SYMBOLS["DS"] = "ds"
    SYMBOLS["EXAS"] = "Es"
    SYMBOLS["FS"] = "fs"
    SYMBOLS["GIGAS"] = "Gs"
    SYMBOLS["H"] = "h"
    SYMBOLS["HS"] = "hs"
    SYMBOLS["KS"] = "ks"
    SYMBOLS["MEGAS"] = "Ms"
    SYMBOLS["MICROS"] = "µs"
    SYMBOLS["MIN"] = "min"
    SYMBOLS["MS"] = "ms"
    SYMBOLS["NS"] = "ns"
    SYMBOLS["PETAS"] = "Ps"
    SYMBOLS["PS"] = "ps"
    SYMBOLS["S"] = "s"
    SYMBOLS["TERAS"] = "Ts"
    SYMBOLS["YOTTAS"] = "Ys"
    SYMBOLS["YS"] = "ys"
    SYMBOLS["ZETTAS"] = "Zs"
    SYMBOLS["ZS"] = "zs"

    def __init__(self, value=None, unit=None):
        _omero_model.Time.__init__(self)
        if isinstance(value, _omero_model.TimeI):
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
                self.setUnit(getattr(UnitsTime, str(target)))
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
        return TimeI.SYMBOLS.get(str(unit))

    def setUnit(self, unit, current=None):
        self._unit = unit

    def setValue(self, value, current=None):
        self._value = value

    def __str__(self):
        return self._base_string(self.getValue(), self.getUnit())

_omero_model.TimeI = TimeI
