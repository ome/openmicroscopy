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
    CONVERSIONS["ATOOSECOND:CENTISECOND"] = \
        lambda value: (10 ** -16) * value
    CONVERSIONS["ATOOSECOND:DAY"] = \
        lambda: noconversion("ATOOSECOND", "DAY")
    CONVERSIONS["ATOOSECOND:DECASECOND"] = \
        lambda value: (10 ** -19) * value
    CONVERSIONS["ATOOSECOND:DECISECOND"] = \
        lambda value: (10 ** -17) * value
    CONVERSIONS["ATOOSECOND:EXASECOND"] = \
        lambda value: (10 ** -36) * value
    CONVERSIONS["ATOOSECOND:FEMTOSECOND"] = \
        lambda value: (10 ** -3) * value
    CONVERSIONS["ATOOSECOND:GIGASECOND"] = \
        lambda value: (10 ** -27) * value
    CONVERSIONS["ATOOSECOND:HECTOSECOND"] = \
        lambda value: (10 ** -20) * value
    CONVERSIONS["ATOOSECOND:HOUR"] = \
        lambda: noconversion("ATOOSECOND", "HOUR")
    CONVERSIONS["ATOOSECOND:KILOSECOND"] = \
        lambda value: (10 ** -21) * value
    CONVERSIONS["ATOOSECOND:MEGASECOND"] = \
        lambda value: (10 ** -24) * value
    CONVERSIONS["ATOOSECOND:MICROSECOND"] = \
        lambda value: (10 ** -12) * value
    CONVERSIONS["ATOOSECOND:MILLISECOND"] = \
        lambda value: (10 ** -15) * value
    CONVERSIONS["ATOOSECOND:MINUTE"] = \
        lambda: noconversion("ATOOSECOND", "MINUTE")
    CONVERSIONS["ATOOSECOND:NANOSECOND"] = \
        lambda value: (10 ** -9) * value
    CONVERSIONS["ATOOSECOND:PETASECOND"] = \
        lambda value: (10 ** -33) * value
    CONVERSIONS["ATOOSECOND:PICOSECOND"] = \
        lambda value: (10 ** -6) * value
    CONVERSIONS["ATOOSECOND:SECOND"] = \
        lambda value: (10 ** -18) * value
    CONVERSIONS["ATOOSECOND:TERASECOND"] = \
        lambda value: (10 ** -30) * value
    CONVERSIONS["ATOOSECOND:YOCTOSECOND"] = \
        lambda value: (10 ** 6) * value
    CONVERSIONS["ATOOSECOND:YOTTASECOND"] = \
        lambda value: (10 ** -42) * value
    CONVERSIONS["ATOOSECOND:ZEPTOSECOND"] = \
        lambda value: (10 ** 3) * value
    CONVERSIONS["ATOOSECOND:ZETTASECOND"] = \
        lambda value: (10 ** -39) * value
    CONVERSIONS["CENTISECOND:ATOOSECOND"] = \
        lambda value: (10 ** 16) * value
    CONVERSIONS["CENTISECOND:DAY"] = \
        lambda: noconversion("CENTISECOND", "DAY")
    CONVERSIONS["CENTISECOND:DECASECOND"] = \
        lambda value: (10 ** -3) * value
    CONVERSIONS["CENTISECOND:DECISECOND"] = \
        lambda value: (10 ** -1) * value
    CONVERSIONS["CENTISECOND:EXASECOND"] = \
        lambda value: (10 ** -20) * value
    CONVERSIONS["CENTISECOND:FEMTOSECOND"] = \
        lambda value: (10 ** 13) * value
    CONVERSIONS["CENTISECOND:GIGASECOND"] = \
        lambda value: (10 ** -11) * value
    CONVERSIONS["CENTISECOND:HECTOSECOND"] = \
        lambda value: (10 ** -4) * value
    CONVERSIONS["CENTISECOND:HOUR"] = \
        lambda: noconversion("CENTISECOND", "HOUR")
    CONVERSIONS["CENTISECOND:KILOSECOND"] = \
        lambda value: (10 ** -5) * value
    CONVERSIONS["CENTISECOND:MEGASECOND"] = \
        lambda value: (10 ** -8) * value
    CONVERSIONS["CENTISECOND:MICROSECOND"] = \
        lambda value: (10 ** 4) * value
    CONVERSIONS["CENTISECOND:MILLISECOND"] = \
        lambda value: 10 * value
    CONVERSIONS["CENTISECOND:MINUTE"] = \
        lambda: noconversion("CENTISECOND", "MINUTE")
    CONVERSIONS["CENTISECOND:NANOSECOND"] = \
        lambda value: (10 ** 7) * value
    CONVERSIONS["CENTISECOND:PETASECOND"] = \
        lambda value: (10 ** -17) * value
    CONVERSIONS["CENTISECOND:PICOSECOND"] = \
        lambda value: (10 ** 10) * value
    CONVERSIONS["CENTISECOND:SECOND"] = \
        lambda value: (10 ** -2) * value
    CONVERSIONS["CENTISECOND:TERASECOND"] = \
        lambda value: (10 ** -14) * value
    CONVERSIONS["CENTISECOND:YOCTOSECOND"] = \
        lambda value: (10 ** 22) * value
    CONVERSIONS["CENTISECOND:YOTTASECOND"] = \
        lambda value: (10 ** -26) * value
    CONVERSIONS["CENTISECOND:ZEPTOSECOND"] = \
        lambda value: (10 ** 19) * value
    CONVERSIONS["CENTISECOND:ZETTASECOND"] = \
        lambda value: (10 ** -23) * value
    CONVERSIONS["DAY:ATOOSECOND"] = \
        lambda: noconversion("DAY", "ATOOSECOND")
    CONVERSIONS["DAY:CENTISECOND"] = \
        lambda: noconversion("DAY", "CENTISECOND")
    CONVERSIONS["DAY:DECASECOND"] = \
        lambda: noconversion("DAY", "DECASECOND")
    CONVERSIONS["DAY:DECISECOND"] = \
        lambda: noconversion("DAY", "DECISECOND")
    CONVERSIONS["DAY:EXASECOND"] = \
        lambda: noconversion("DAY", "EXASECOND")
    CONVERSIONS["DAY:FEMTOSECOND"] = \
        lambda: noconversion("DAY", "FEMTOSECOND")
    CONVERSIONS["DAY:GIGASECOND"] = \
        lambda: noconversion("DAY", "GIGASECOND")
    CONVERSIONS["DAY:HECTOSECOND"] = \
        lambda: noconversion("DAY", "HECTOSECOND")
    CONVERSIONS["DAY:HOUR"] = \
        lambda: noconversion("DAY", "HOUR")
    CONVERSIONS["DAY:KILOSECOND"] = \
        lambda: noconversion("DAY", "KILOSECOND")
    CONVERSIONS["DAY:MEGASECOND"] = \
        lambda: noconversion("DAY", "MEGASECOND")
    CONVERSIONS["DAY:MICROSECOND"] = \
        lambda: noconversion("DAY", "MICROSECOND")
    CONVERSIONS["DAY:MILLISECOND"] = \
        lambda: noconversion("DAY", "MILLISECOND")
    CONVERSIONS["DAY:MINUTE"] = \
        lambda: noconversion("DAY", "MINUTE")
    CONVERSIONS["DAY:NANOSECOND"] = \
        lambda: noconversion("DAY", "NANOSECOND")
    CONVERSIONS["DAY:PETASECOND"] = \
        lambda: noconversion("DAY", "PETASECOND")
    CONVERSIONS["DAY:PICOSECOND"] = \
        lambda: noconversion("DAY", "PICOSECOND")
    CONVERSIONS["DAY:SECOND"] = \
        lambda: noconversion("DAY", "SECOND")
    CONVERSIONS["DAY:TERASECOND"] = \
        lambda: noconversion("DAY", "TERASECOND")
    CONVERSIONS["DAY:YOCTOSECOND"] = \
        lambda: noconversion("DAY", "YOCTOSECOND")
    CONVERSIONS["DAY:YOTTASECOND"] = \
        lambda: noconversion("DAY", "YOTTASECOND")
    CONVERSIONS["DAY:ZEPTOSECOND"] = \
        lambda: noconversion("DAY", "ZEPTOSECOND")
    CONVERSIONS["DAY:ZETTASECOND"] = \
        lambda: noconversion("DAY", "ZETTASECOND")
    CONVERSIONS["DECASECOND:ATOOSECOND"] = \
        lambda value: (10 ** 19) * value
    CONVERSIONS["DECASECOND:CENTISECOND"] = \
        lambda value: (10 ** 3) * value
    CONVERSIONS["DECASECOND:DAY"] = \
        lambda: noconversion("DECASECOND", "DAY")
    CONVERSIONS["DECASECOND:DECISECOND"] = \
        lambda value: (10 ** 2) * value
    CONVERSIONS["DECASECOND:EXASECOND"] = \
        lambda value: (10 ** -17) * value
    CONVERSIONS["DECASECOND:FEMTOSECOND"] = \
        lambda value: (10 ** 16) * value
    CONVERSIONS["DECASECOND:GIGASECOND"] = \
        lambda value: (10 ** -8) * value
    CONVERSIONS["DECASECOND:HECTOSECOND"] = \
        lambda value: (10 ** -1) * value
    CONVERSIONS["DECASECOND:HOUR"] = \
        lambda: noconversion("DECASECOND", "HOUR")
    CONVERSIONS["DECASECOND:KILOSECOND"] = \
        lambda value: (10 ** -2) * value
    CONVERSIONS["DECASECOND:MEGASECOND"] = \
        lambda value: (10 ** -5) * value
    CONVERSIONS["DECASECOND:MICROSECOND"] = \
        lambda value: (10 ** 7) * value
    CONVERSIONS["DECASECOND:MILLISECOND"] = \
        lambda value: (10 ** 4) * value
    CONVERSIONS["DECASECOND:MINUTE"] = \
        lambda: noconversion("DECASECOND", "MINUTE")
    CONVERSIONS["DECASECOND:NANOSECOND"] = \
        lambda value: (10 ** 10) * value
    CONVERSIONS["DECASECOND:PETASECOND"] = \
        lambda value: (10 ** -14) * value
    CONVERSIONS["DECASECOND:PICOSECOND"] = \
        lambda value: (10 ** 13) * value
    CONVERSIONS["DECASECOND:SECOND"] = \
        lambda value: 10 * value
    CONVERSIONS["DECASECOND:TERASECOND"] = \
        lambda value: (10 ** -11) * value
    CONVERSIONS["DECASECOND:YOCTOSECOND"] = \
        lambda value: (10 ** 25) * value
    CONVERSIONS["DECASECOND:YOTTASECOND"] = \
        lambda value: (10 ** -23) * value
    CONVERSIONS["DECASECOND:ZEPTOSECOND"] = \
        lambda value: (10 ** 22) * value
    CONVERSIONS["DECASECOND:ZETTASECOND"] = \
        lambda value: (10 ** -20) * value
    CONVERSIONS["DECISECOND:ATOOSECOND"] = \
        lambda value: (10 ** 17) * value
    CONVERSIONS["DECISECOND:CENTISECOND"] = \
        lambda value: 10 * value
    CONVERSIONS["DECISECOND:DAY"] = \
        lambda: noconversion("DECISECOND", "DAY")
    CONVERSIONS["DECISECOND:DECASECOND"] = \
        lambda value: (10 ** -2) * value
    CONVERSIONS["DECISECOND:EXASECOND"] = \
        lambda value: (10 ** -19) * value
    CONVERSIONS["DECISECOND:FEMTOSECOND"] = \
        lambda value: (10 ** 14) * value
    CONVERSIONS["DECISECOND:GIGASECOND"] = \
        lambda value: (10 ** -10) * value
    CONVERSIONS["DECISECOND:HECTOSECOND"] = \
        lambda value: (10 ** -3) * value
    CONVERSIONS["DECISECOND:HOUR"] = \
        lambda: noconversion("DECISECOND", "HOUR")
    CONVERSIONS["DECISECOND:KILOSECOND"] = \
        lambda value: (10 ** -4) * value
    CONVERSIONS["DECISECOND:MEGASECOND"] = \
        lambda value: (10 ** -7) * value
    CONVERSIONS["DECISECOND:MICROSECOND"] = \
        lambda value: (10 ** 5) * value
    CONVERSIONS["DECISECOND:MILLISECOND"] = \
        lambda value: (10 ** 2) * value
    CONVERSIONS["DECISECOND:MINUTE"] = \
        lambda: noconversion("DECISECOND", "MINUTE")
    CONVERSIONS["DECISECOND:NANOSECOND"] = \
        lambda value: (10 ** 8) * value
    CONVERSIONS["DECISECOND:PETASECOND"] = \
        lambda value: (10 ** -16) * value
    CONVERSIONS["DECISECOND:PICOSECOND"] = \
        lambda value: (10 ** 11) * value
    CONVERSIONS["DECISECOND:SECOND"] = \
        lambda value: (10 ** -1) * value
    CONVERSIONS["DECISECOND:TERASECOND"] = \
        lambda value: (10 ** -13) * value
    CONVERSIONS["DECISECOND:YOCTOSECOND"] = \
        lambda value: (10 ** 23) * value
    CONVERSIONS["DECISECOND:YOTTASECOND"] = \
        lambda value: (10 ** -25) * value
    CONVERSIONS["DECISECOND:ZEPTOSECOND"] = \
        lambda value: (10 ** 20) * value
    CONVERSIONS["DECISECOND:ZETTASECOND"] = \
        lambda value: (10 ** -22) * value
    CONVERSIONS["EXASECOND:ATOOSECOND"] = \
        lambda value: (10 ** 36) * value
    CONVERSIONS["EXASECOND:CENTISECOND"] = \
        lambda value: (10 ** 20) * value
    CONVERSIONS["EXASECOND:DAY"] = \
        lambda: noconversion("EXASECOND", "DAY")
    CONVERSIONS["EXASECOND:DECASECOND"] = \
        lambda value: (10 ** 17) * value
    CONVERSIONS["EXASECOND:DECISECOND"] = \
        lambda value: (10 ** 19) * value
    CONVERSIONS["EXASECOND:FEMTOSECOND"] = \
        lambda value: (10 ** 33) * value
    CONVERSIONS["EXASECOND:GIGASECOND"] = \
        lambda value: (10 ** 9) * value
    CONVERSIONS["EXASECOND:HECTOSECOND"] = \
        lambda value: (10 ** 16) * value
    CONVERSIONS["EXASECOND:HOUR"] = \
        lambda: noconversion("EXASECOND", "HOUR")
    CONVERSIONS["EXASECOND:KILOSECOND"] = \
        lambda value: (10 ** 15) * value
    CONVERSIONS["EXASECOND:MEGASECOND"] = \
        lambda value: (10 ** 12) * value
    CONVERSIONS["EXASECOND:MICROSECOND"] = \
        lambda value: (10 ** 24) * value
    CONVERSIONS["EXASECOND:MILLISECOND"] = \
        lambda value: (10 ** 21) * value
    CONVERSIONS["EXASECOND:MINUTE"] = \
        lambda: noconversion("EXASECOND", "MINUTE")
    CONVERSIONS["EXASECOND:NANOSECOND"] = \
        lambda value: (10 ** 27) * value
    CONVERSIONS["EXASECOND:PETASECOND"] = \
        lambda value: (10 ** 3) * value
    CONVERSIONS["EXASECOND:PICOSECOND"] = \
        lambda value: (10 ** 30) * value
    CONVERSIONS["EXASECOND:SECOND"] = \
        lambda value: (10 ** 18) * value
    CONVERSIONS["EXASECOND:TERASECOND"] = \
        lambda value: (10 ** 6) * value
    CONVERSIONS["EXASECOND:YOCTOSECOND"] = \
        lambda value: (10 ** 42) * value
    CONVERSIONS["EXASECOND:YOTTASECOND"] = \
        lambda value: (10 ** -6) * value
    CONVERSIONS["EXASECOND:ZEPTOSECOND"] = \
        lambda value: (10 ** 39) * value
    CONVERSIONS["EXASECOND:ZETTASECOND"] = \
        lambda value: (10 ** -3) * value
    CONVERSIONS["FEMTOSECOND:ATOOSECOND"] = \
        lambda value: (10 ** 3) * value
    CONVERSIONS["FEMTOSECOND:CENTISECOND"] = \
        lambda value: (10 ** -13) * value
    CONVERSIONS["FEMTOSECOND:DAY"] = \
        lambda: noconversion("FEMTOSECOND", "DAY")
    CONVERSIONS["FEMTOSECOND:DECASECOND"] = \
        lambda value: (10 ** -16) * value
    CONVERSIONS["FEMTOSECOND:DECISECOND"] = \
        lambda value: (10 ** -14) * value
    CONVERSIONS["FEMTOSECOND:EXASECOND"] = \
        lambda value: (10 ** -33) * value
    CONVERSIONS["FEMTOSECOND:GIGASECOND"] = \
        lambda value: (10 ** -24) * value
    CONVERSIONS["FEMTOSECOND:HECTOSECOND"] = \
        lambda value: (10 ** -17) * value
    CONVERSIONS["FEMTOSECOND:HOUR"] = \
        lambda: noconversion("FEMTOSECOND", "HOUR")
    CONVERSIONS["FEMTOSECOND:KILOSECOND"] = \
        lambda value: (10 ** -18) * value
    CONVERSIONS["FEMTOSECOND:MEGASECOND"] = \
        lambda value: (10 ** -21) * value
    CONVERSIONS["FEMTOSECOND:MICROSECOND"] = \
        lambda value: (10 ** -9) * value
    CONVERSIONS["FEMTOSECOND:MILLISECOND"] = \
        lambda value: (10 ** -12) * value
    CONVERSIONS["FEMTOSECOND:MINUTE"] = \
        lambda: noconversion("FEMTOSECOND", "MINUTE")
    CONVERSIONS["FEMTOSECOND:NANOSECOND"] = \
        lambda value: (10 ** -6) * value
    CONVERSIONS["FEMTOSECOND:PETASECOND"] = \
        lambda value: (10 ** -30) * value
    CONVERSIONS["FEMTOSECOND:PICOSECOND"] = \
        lambda value: (10 ** -3) * value
    CONVERSIONS["FEMTOSECOND:SECOND"] = \
        lambda value: (10 ** -15) * value
    CONVERSIONS["FEMTOSECOND:TERASECOND"] = \
        lambda value: (10 ** -27) * value
    CONVERSIONS["FEMTOSECOND:YOCTOSECOND"] = \
        lambda value: (10 ** 9) * value
    CONVERSIONS["FEMTOSECOND:YOTTASECOND"] = \
        lambda value: (10 ** -39) * value
    CONVERSIONS["FEMTOSECOND:ZEPTOSECOND"] = \
        lambda value: (10 ** 6) * value
    CONVERSIONS["FEMTOSECOND:ZETTASECOND"] = \
        lambda value: (10 ** -36) * value
    CONVERSIONS["GIGASECOND:ATOOSECOND"] = \
        lambda value: (10 ** 27) * value
    CONVERSIONS["GIGASECOND:CENTISECOND"] = \
        lambda value: (10 ** 11) * value
    CONVERSIONS["GIGASECOND:DAY"] = \
        lambda: noconversion("GIGASECOND", "DAY")
    CONVERSIONS["GIGASECOND:DECASECOND"] = \
        lambda value: (10 ** 8) * value
    CONVERSIONS["GIGASECOND:DECISECOND"] = \
        lambda value: (10 ** 10) * value
    CONVERSIONS["GIGASECOND:EXASECOND"] = \
        lambda value: (10 ** -9) * value
    CONVERSIONS["GIGASECOND:FEMTOSECOND"] = \
        lambda value: (10 ** 24) * value
    CONVERSIONS["GIGASECOND:HECTOSECOND"] = \
        lambda value: (10 ** 7) * value
    CONVERSIONS["GIGASECOND:HOUR"] = \
        lambda: noconversion("GIGASECOND", "HOUR")
    CONVERSIONS["GIGASECOND:KILOSECOND"] = \
        lambda value: (10 ** 6) * value
    CONVERSIONS["GIGASECOND:MEGASECOND"] = \
        lambda value: (10 ** 3) * value
    CONVERSIONS["GIGASECOND:MICROSECOND"] = \
        lambda value: (10 ** 15) * value
    CONVERSIONS["GIGASECOND:MILLISECOND"] = \
        lambda value: (10 ** 12) * value
    CONVERSIONS["GIGASECOND:MINUTE"] = \
        lambda: noconversion("GIGASECOND", "MINUTE")
    CONVERSIONS["GIGASECOND:NANOSECOND"] = \
        lambda value: (10 ** 18) * value
    CONVERSIONS["GIGASECOND:PETASECOND"] = \
        lambda value: (10 ** -6) * value
    CONVERSIONS["GIGASECOND:PICOSECOND"] = \
        lambda value: (10 ** 21) * value
    CONVERSIONS["GIGASECOND:SECOND"] = \
        lambda value: (10 ** 9) * value
    CONVERSIONS["GIGASECOND:TERASECOND"] = \
        lambda value: (10 ** -3) * value
    CONVERSIONS["GIGASECOND:YOCTOSECOND"] = \
        lambda value: (10 ** 33) * value
    CONVERSIONS["GIGASECOND:YOTTASECOND"] = \
        lambda value: (10 ** -15) * value
    CONVERSIONS["GIGASECOND:ZEPTOSECOND"] = \
        lambda value: (10 ** 30) * value
    CONVERSIONS["GIGASECOND:ZETTASECOND"] = \
        lambda value: (10 ** -12) * value
    CONVERSIONS["HECTOSECOND:ATOOSECOND"] = \
        lambda value: (10 ** 20) * value
    CONVERSIONS["HECTOSECOND:CENTISECOND"] = \
        lambda value: (10 ** 4) * value
    CONVERSIONS["HECTOSECOND:DAY"] = \
        lambda: noconversion("HECTOSECOND", "DAY")
    CONVERSIONS["HECTOSECOND:DECASECOND"] = \
        lambda value: 10 * value
    CONVERSIONS["HECTOSECOND:DECISECOND"] = \
        lambda value: (10 ** 3) * value
    CONVERSIONS["HECTOSECOND:EXASECOND"] = \
        lambda value: (10 ** -16) * value
    CONVERSIONS["HECTOSECOND:FEMTOSECOND"] = \
        lambda value: (10 ** 17) * value
    CONVERSIONS["HECTOSECOND:GIGASECOND"] = \
        lambda value: (10 ** -7) * value
    CONVERSIONS["HECTOSECOND:HOUR"] = \
        lambda: noconversion("HECTOSECOND", "HOUR")
    CONVERSIONS["HECTOSECOND:KILOSECOND"] = \
        lambda value: (10 ** -1) * value
    CONVERSIONS["HECTOSECOND:MEGASECOND"] = \
        lambda value: (10 ** -4) * value
    CONVERSIONS["HECTOSECOND:MICROSECOND"] = \
        lambda value: (10 ** 8) * value
    CONVERSIONS["HECTOSECOND:MILLISECOND"] = \
        lambda value: (10 ** 5) * value
    CONVERSIONS["HECTOSECOND:MINUTE"] = \
        lambda: noconversion("HECTOSECOND", "MINUTE")
    CONVERSIONS["HECTOSECOND:NANOSECOND"] = \
        lambda value: (10 ** 11) * value
    CONVERSIONS["HECTOSECOND:PETASECOND"] = \
        lambda value: (10 ** -13) * value
    CONVERSIONS["HECTOSECOND:PICOSECOND"] = \
        lambda value: (10 ** 14) * value
    CONVERSIONS["HECTOSECOND:SECOND"] = \
        lambda value: (10 ** 2) * value
    CONVERSIONS["HECTOSECOND:TERASECOND"] = \
        lambda value: (10 ** -10) * value
    CONVERSIONS["HECTOSECOND:YOCTOSECOND"] = \
        lambda value: (10 ** 26) * value
    CONVERSIONS["HECTOSECOND:YOTTASECOND"] = \
        lambda value: (10 ** -22) * value
    CONVERSIONS["HECTOSECOND:ZEPTOSECOND"] = \
        lambda value: (10 ** 23) * value
    CONVERSIONS["HECTOSECOND:ZETTASECOND"] = \
        lambda value: (10 ** -19) * value
    CONVERSIONS["HOUR:ATOOSECOND"] = \
        lambda: noconversion("HOUR", "ATOOSECOND")
    CONVERSIONS["HOUR:CENTISECOND"] = \
        lambda: noconversion("HOUR", "CENTISECOND")
    CONVERSIONS["HOUR:DAY"] = \
        lambda: noconversion("HOUR", "DAY")
    CONVERSIONS["HOUR:DECASECOND"] = \
        lambda: noconversion("HOUR", "DECASECOND")
    CONVERSIONS["HOUR:DECISECOND"] = \
        lambda: noconversion("HOUR", "DECISECOND")
    CONVERSIONS["HOUR:EXASECOND"] = \
        lambda: noconversion("HOUR", "EXASECOND")
    CONVERSIONS["HOUR:FEMTOSECOND"] = \
        lambda: noconversion("HOUR", "FEMTOSECOND")
    CONVERSIONS["HOUR:GIGASECOND"] = \
        lambda: noconversion("HOUR", "GIGASECOND")
    CONVERSIONS["HOUR:HECTOSECOND"] = \
        lambda: noconversion("HOUR", "HECTOSECOND")
    CONVERSIONS["HOUR:KILOSECOND"] = \
        lambda: noconversion("HOUR", "KILOSECOND")
    CONVERSIONS["HOUR:MEGASECOND"] = \
        lambda: noconversion("HOUR", "MEGASECOND")
    CONVERSIONS["HOUR:MICROSECOND"] = \
        lambda: noconversion("HOUR", "MICROSECOND")
    CONVERSIONS["HOUR:MILLISECOND"] = \
        lambda: noconversion("HOUR", "MILLISECOND")
    CONVERSIONS["HOUR:MINUTE"] = \
        lambda: noconversion("HOUR", "MINUTE")
    CONVERSIONS["HOUR:NANOSECOND"] = \
        lambda: noconversion("HOUR", "NANOSECOND")
    CONVERSIONS["HOUR:PETASECOND"] = \
        lambda: noconversion("HOUR", "PETASECOND")
    CONVERSIONS["HOUR:PICOSECOND"] = \
        lambda: noconversion("HOUR", "PICOSECOND")
    CONVERSIONS["HOUR:SECOND"] = \
        lambda: noconversion("HOUR", "SECOND")
    CONVERSIONS["HOUR:TERASECOND"] = \
        lambda: noconversion("HOUR", "TERASECOND")
    CONVERSIONS["HOUR:YOCTOSECOND"] = \
        lambda: noconversion("HOUR", "YOCTOSECOND")
    CONVERSIONS["HOUR:YOTTASECOND"] = \
        lambda: noconversion("HOUR", "YOTTASECOND")
    CONVERSIONS["HOUR:ZEPTOSECOND"] = \
        lambda: noconversion("HOUR", "ZEPTOSECOND")
    CONVERSIONS["HOUR:ZETTASECOND"] = \
        lambda: noconversion("HOUR", "ZETTASECOND")
    CONVERSIONS["KILOSECOND:ATOOSECOND"] = \
        lambda value: (10 ** 21) * value
    CONVERSIONS["KILOSECOND:CENTISECOND"] = \
        lambda value: (10 ** 5) * value
    CONVERSIONS["KILOSECOND:DAY"] = \
        lambda: noconversion("KILOSECOND", "DAY")
    CONVERSIONS["KILOSECOND:DECASECOND"] = \
        lambda value: (10 ** 2) * value
    CONVERSIONS["KILOSECOND:DECISECOND"] = \
        lambda value: (10 ** 4) * value
    CONVERSIONS["KILOSECOND:EXASECOND"] = \
        lambda value: (10 ** -15) * value
    CONVERSIONS["KILOSECOND:FEMTOSECOND"] = \
        lambda value: (10 ** 18) * value
    CONVERSIONS["KILOSECOND:GIGASECOND"] = \
        lambda value: (10 ** -6) * value
    CONVERSIONS["KILOSECOND:HECTOSECOND"] = \
        lambda value: 10 * value
    CONVERSIONS["KILOSECOND:HOUR"] = \
        lambda: noconversion("KILOSECOND", "HOUR")
    CONVERSIONS["KILOSECOND:MEGASECOND"] = \
        lambda value: (10 ** -3) * value
    CONVERSIONS["KILOSECOND:MICROSECOND"] = \
        lambda value: (10 ** 9) * value
    CONVERSIONS["KILOSECOND:MILLISECOND"] = \
        lambda value: (10 ** 6) * value
    CONVERSIONS["KILOSECOND:MINUTE"] = \
        lambda: noconversion("KILOSECOND", "MINUTE")
    CONVERSIONS["KILOSECOND:NANOSECOND"] = \
        lambda value: (10 ** 12) * value
    CONVERSIONS["KILOSECOND:PETASECOND"] = \
        lambda value: (10 ** -12) * value
    CONVERSIONS["KILOSECOND:PICOSECOND"] = \
        lambda value: (10 ** 15) * value
    CONVERSIONS["KILOSECOND:SECOND"] = \
        lambda value: (10 ** 3) * value
    CONVERSIONS["KILOSECOND:TERASECOND"] = \
        lambda value: (10 ** -9) * value
    CONVERSIONS["KILOSECOND:YOCTOSECOND"] = \
        lambda value: (10 ** 27) * value
    CONVERSIONS["KILOSECOND:YOTTASECOND"] = \
        lambda value: (10 ** -21) * value
    CONVERSIONS["KILOSECOND:ZEPTOSECOND"] = \
        lambda value: (10 ** 24) * value
    CONVERSIONS["KILOSECOND:ZETTASECOND"] = \
        lambda value: (10 ** -18) * value
    CONVERSIONS["MEGASECOND:ATOOSECOND"] = \
        lambda value: (10 ** 24) * value
    CONVERSIONS["MEGASECOND:CENTISECOND"] = \
        lambda value: (10 ** 8) * value
    CONVERSIONS["MEGASECOND:DAY"] = \
        lambda: noconversion("MEGASECOND", "DAY")
    CONVERSIONS["MEGASECOND:DECASECOND"] = \
        lambda value: (10 ** 5) * value
    CONVERSIONS["MEGASECOND:DECISECOND"] = \
        lambda value: (10 ** 7) * value
    CONVERSIONS["MEGASECOND:EXASECOND"] = \
        lambda value: (10 ** -12) * value
    CONVERSIONS["MEGASECOND:FEMTOSECOND"] = \
        lambda value: (10 ** 21) * value
    CONVERSIONS["MEGASECOND:GIGASECOND"] = \
        lambda value: (10 ** -3) * value
    CONVERSIONS["MEGASECOND:HECTOSECOND"] = \
        lambda value: (10 ** 4) * value
    CONVERSIONS["MEGASECOND:HOUR"] = \
        lambda: noconversion("MEGASECOND", "HOUR")
    CONVERSIONS["MEGASECOND:KILOSECOND"] = \
        lambda value: (10 ** 3) * value
    CONVERSIONS["MEGASECOND:MICROSECOND"] = \
        lambda value: (10 ** 12) * value
    CONVERSIONS["MEGASECOND:MILLISECOND"] = \
        lambda value: (10 ** 9) * value
    CONVERSIONS["MEGASECOND:MINUTE"] = \
        lambda: noconversion("MEGASECOND", "MINUTE")
    CONVERSIONS["MEGASECOND:NANOSECOND"] = \
        lambda value: (10 ** 15) * value
    CONVERSIONS["MEGASECOND:PETASECOND"] = \
        lambda value: (10 ** -9) * value
    CONVERSIONS["MEGASECOND:PICOSECOND"] = \
        lambda value: (10 ** 18) * value
    CONVERSIONS["MEGASECOND:SECOND"] = \
        lambda value: (10 ** 6) * value
    CONVERSIONS["MEGASECOND:TERASECOND"] = \
        lambda value: (10 ** -6) * value
    CONVERSIONS["MEGASECOND:YOCTOSECOND"] = \
        lambda value: (10 ** 30) * value
    CONVERSIONS["MEGASECOND:YOTTASECOND"] = \
        lambda value: (10 ** -18) * value
    CONVERSIONS["MEGASECOND:ZEPTOSECOND"] = \
        lambda value: (10 ** 27) * value
    CONVERSIONS["MEGASECOND:ZETTASECOND"] = \
        lambda value: (10 ** -15) * value
    CONVERSIONS["MICROSECOND:ATOOSECOND"] = \
        lambda value: (10 ** 12) * value
    CONVERSIONS["MICROSECOND:CENTISECOND"] = \
        lambda value: (10 ** -4) * value
    CONVERSIONS["MICROSECOND:DAY"] = \
        lambda: noconversion("MICROSECOND", "DAY")
    CONVERSIONS["MICROSECOND:DECASECOND"] = \
        lambda value: (10 ** -7) * value
    CONVERSIONS["MICROSECOND:DECISECOND"] = \
        lambda value: (10 ** -5) * value
    CONVERSIONS["MICROSECOND:EXASECOND"] = \
        lambda value: (10 ** -24) * value
    CONVERSIONS["MICROSECOND:FEMTOSECOND"] = \
        lambda value: (10 ** 9) * value
    CONVERSIONS["MICROSECOND:GIGASECOND"] = \
        lambda value: (10 ** -15) * value
    CONVERSIONS["MICROSECOND:HECTOSECOND"] = \
        lambda value: (10 ** -8) * value
    CONVERSIONS["MICROSECOND:HOUR"] = \
        lambda: noconversion("MICROSECOND", "HOUR")
    CONVERSIONS["MICROSECOND:KILOSECOND"] = \
        lambda value: (10 ** -9) * value
    CONVERSIONS["MICROSECOND:MEGASECOND"] = \
        lambda value: (10 ** -12) * value
    CONVERSIONS["MICROSECOND:MILLISECOND"] = \
        lambda value: (10 ** -3) * value
    CONVERSIONS["MICROSECOND:MINUTE"] = \
        lambda: noconversion("MICROSECOND", "MINUTE")
    CONVERSIONS["MICROSECOND:NANOSECOND"] = \
        lambda value: (10 ** 3) * value
    CONVERSIONS["MICROSECOND:PETASECOND"] = \
        lambda value: (10 ** -21) * value
    CONVERSIONS["MICROSECOND:PICOSECOND"] = \
        lambda value: (10 ** 6) * value
    CONVERSIONS["MICROSECOND:SECOND"] = \
        lambda value: (10 ** -6) * value
    CONVERSIONS["MICROSECOND:TERASECOND"] = \
        lambda value: (10 ** -18) * value
    CONVERSIONS["MICROSECOND:YOCTOSECOND"] = \
        lambda value: (10 ** 18) * value
    CONVERSIONS["MICROSECOND:YOTTASECOND"] = \
        lambda value: (10 ** -30) * value
    CONVERSIONS["MICROSECOND:ZEPTOSECOND"] = \
        lambda value: (10 ** 15) * value
    CONVERSIONS["MICROSECOND:ZETTASECOND"] = \
        lambda value: (10 ** -27) * value
    CONVERSIONS["MILLISECOND:ATOOSECOND"] = \
        lambda value: (10 ** 15) * value
    CONVERSIONS["MILLISECOND:CENTISECOND"] = \
        lambda value: (10 ** -1) * value
    CONVERSIONS["MILLISECOND:DAY"] = \
        lambda: noconversion("MILLISECOND", "DAY")
    CONVERSIONS["MILLISECOND:DECASECOND"] = \
        lambda value: (10 ** -4) * value
    CONVERSIONS["MILLISECOND:DECISECOND"] = \
        lambda value: (10 ** -2) * value
    CONVERSIONS["MILLISECOND:EXASECOND"] = \
        lambda value: (10 ** -21) * value
    CONVERSIONS["MILLISECOND:FEMTOSECOND"] = \
        lambda value: (10 ** 12) * value
    CONVERSIONS["MILLISECOND:GIGASECOND"] = \
        lambda value: (10 ** -12) * value
    CONVERSIONS["MILLISECOND:HECTOSECOND"] = \
        lambda value: (10 ** -5) * value
    CONVERSIONS["MILLISECOND:HOUR"] = \
        lambda: noconversion("MILLISECOND", "HOUR")
    CONVERSIONS["MILLISECOND:KILOSECOND"] = \
        lambda value: (10 ** -6) * value
    CONVERSIONS["MILLISECOND:MEGASECOND"] = \
        lambda value: (10 ** -9) * value
    CONVERSIONS["MILLISECOND:MICROSECOND"] = \
        lambda value: (10 ** 3) * value
    CONVERSIONS["MILLISECOND:MINUTE"] = \
        lambda: noconversion("MILLISECOND", "MINUTE")
    CONVERSIONS["MILLISECOND:NANOSECOND"] = \
        lambda value: (10 ** 6) * value
    CONVERSIONS["MILLISECOND:PETASECOND"] = \
        lambda value: (10 ** -18) * value
    CONVERSIONS["MILLISECOND:PICOSECOND"] = \
        lambda value: (10 ** 9) * value
    CONVERSIONS["MILLISECOND:SECOND"] = \
        lambda value: (10 ** -3) * value
    CONVERSIONS["MILLISECOND:TERASECOND"] = \
        lambda value: (10 ** -15) * value
    CONVERSIONS["MILLISECOND:YOCTOSECOND"] = \
        lambda value: (10 ** 21) * value
    CONVERSIONS["MILLISECOND:YOTTASECOND"] = \
        lambda value: (10 ** -27) * value
    CONVERSIONS["MILLISECOND:ZEPTOSECOND"] = \
        lambda value: (10 ** 18) * value
    CONVERSIONS["MILLISECOND:ZETTASECOND"] = \
        lambda value: (10 ** -24) * value
    CONVERSIONS["MINUTE:ATOOSECOND"] = \
        lambda: noconversion("MINUTE", "ATOOSECOND")
    CONVERSIONS["MINUTE:CENTISECOND"] = \
        lambda: noconversion("MINUTE", "CENTISECOND")
    CONVERSIONS["MINUTE:DAY"] = \
        lambda: noconversion("MINUTE", "DAY")
    CONVERSIONS["MINUTE:DECASECOND"] = \
        lambda: noconversion("MINUTE", "DECASECOND")
    CONVERSIONS["MINUTE:DECISECOND"] = \
        lambda: noconversion("MINUTE", "DECISECOND")
    CONVERSIONS["MINUTE:EXASECOND"] = \
        lambda: noconversion("MINUTE", "EXASECOND")
    CONVERSIONS["MINUTE:FEMTOSECOND"] = \
        lambda: noconversion("MINUTE", "FEMTOSECOND")
    CONVERSIONS["MINUTE:GIGASECOND"] = \
        lambda: noconversion("MINUTE", "GIGASECOND")
    CONVERSIONS["MINUTE:HECTOSECOND"] = \
        lambda: noconversion("MINUTE", "HECTOSECOND")
    CONVERSIONS["MINUTE:HOUR"] = \
        lambda: noconversion("MINUTE", "HOUR")
    CONVERSIONS["MINUTE:KILOSECOND"] = \
        lambda: noconversion("MINUTE", "KILOSECOND")
    CONVERSIONS["MINUTE:MEGASECOND"] = \
        lambda: noconversion("MINUTE", "MEGASECOND")
    CONVERSIONS["MINUTE:MICROSECOND"] = \
        lambda: noconversion("MINUTE", "MICROSECOND")
    CONVERSIONS["MINUTE:MILLISECOND"] = \
        lambda: noconversion("MINUTE", "MILLISECOND")
    CONVERSIONS["MINUTE:NANOSECOND"] = \
        lambda: noconversion("MINUTE", "NANOSECOND")
    CONVERSIONS["MINUTE:PETASECOND"] = \
        lambda: noconversion("MINUTE", "PETASECOND")
    CONVERSIONS["MINUTE:PICOSECOND"] = \
        lambda: noconversion("MINUTE", "PICOSECOND")
    CONVERSIONS["MINUTE:SECOND"] = \
        lambda: noconversion("MINUTE", "SECOND")
    CONVERSIONS["MINUTE:TERASECOND"] = \
        lambda: noconversion("MINUTE", "TERASECOND")
    CONVERSIONS["MINUTE:YOCTOSECOND"] = \
        lambda: noconversion("MINUTE", "YOCTOSECOND")
    CONVERSIONS["MINUTE:YOTTASECOND"] = \
        lambda: noconversion("MINUTE", "YOTTASECOND")
    CONVERSIONS["MINUTE:ZEPTOSECOND"] = \
        lambda: noconversion("MINUTE", "ZEPTOSECOND")
    CONVERSIONS["MINUTE:ZETTASECOND"] = \
        lambda: noconversion("MINUTE", "ZETTASECOND")
    CONVERSIONS["NANOSECOND:ATOOSECOND"] = \
        lambda value: (10 ** 9) * value
    CONVERSIONS["NANOSECOND:CENTISECOND"] = \
        lambda value: (10 ** -7) * value
    CONVERSIONS["NANOSECOND:DAY"] = \
        lambda: noconversion("NANOSECOND", "DAY")
    CONVERSIONS["NANOSECOND:DECASECOND"] = \
        lambda value: (10 ** -10) * value
    CONVERSIONS["NANOSECOND:DECISECOND"] = \
        lambda value: (10 ** -8) * value
    CONVERSIONS["NANOSECOND:EXASECOND"] = \
        lambda value: (10 ** -27) * value
    CONVERSIONS["NANOSECOND:FEMTOSECOND"] = \
        lambda value: (10 ** 6) * value
    CONVERSIONS["NANOSECOND:GIGASECOND"] = \
        lambda value: (10 ** -18) * value
    CONVERSIONS["NANOSECOND:HECTOSECOND"] = \
        lambda value: (10 ** -11) * value
    CONVERSIONS["NANOSECOND:HOUR"] = \
        lambda: noconversion("NANOSECOND", "HOUR")
    CONVERSIONS["NANOSECOND:KILOSECOND"] = \
        lambda value: (10 ** -12) * value
    CONVERSIONS["NANOSECOND:MEGASECOND"] = \
        lambda value: (10 ** -15) * value
    CONVERSIONS["NANOSECOND:MICROSECOND"] = \
        lambda value: (10 ** -3) * value
    CONVERSIONS["NANOSECOND:MILLISECOND"] = \
        lambda value: (10 ** -6) * value
    CONVERSIONS["NANOSECOND:MINUTE"] = \
        lambda: noconversion("NANOSECOND", "MINUTE")
    CONVERSIONS["NANOSECOND:PETASECOND"] = \
        lambda value: (10 ** -24) * value
    CONVERSIONS["NANOSECOND:PICOSECOND"] = \
        lambda value: (10 ** 3) * value
    CONVERSIONS["NANOSECOND:SECOND"] = \
        lambda value: (10 ** -9) * value
    CONVERSIONS["NANOSECOND:TERASECOND"] = \
        lambda value: (10 ** -21) * value
    CONVERSIONS["NANOSECOND:YOCTOSECOND"] = \
        lambda value: (10 ** 15) * value
    CONVERSIONS["NANOSECOND:YOTTASECOND"] = \
        lambda value: (10 ** -33) * value
    CONVERSIONS["NANOSECOND:ZEPTOSECOND"] = \
        lambda value: (10 ** 12) * value
    CONVERSIONS["NANOSECOND:ZETTASECOND"] = \
        lambda value: (10 ** -30) * value
    CONVERSIONS["PETASECOND:ATOOSECOND"] = \
        lambda value: (10 ** 33) * value
    CONVERSIONS["PETASECOND:CENTISECOND"] = \
        lambda value: (10 ** 17) * value
    CONVERSIONS["PETASECOND:DAY"] = \
        lambda: noconversion("PETASECOND", "DAY")
    CONVERSIONS["PETASECOND:DECASECOND"] = \
        lambda value: (10 ** 14) * value
    CONVERSIONS["PETASECOND:DECISECOND"] = \
        lambda value: (10 ** 16) * value
    CONVERSIONS["PETASECOND:EXASECOND"] = \
        lambda value: (10 ** -3) * value
    CONVERSIONS["PETASECOND:FEMTOSECOND"] = \
        lambda value: (10 ** 30) * value
    CONVERSIONS["PETASECOND:GIGASECOND"] = \
        lambda value: (10 ** 6) * value
    CONVERSIONS["PETASECOND:HECTOSECOND"] = \
        lambda value: (10 ** 13) * value
    CONVERSIONS["PETASECOND:HOUR"] = \
        lambda: noconversion("PETASECOND", "HOUR")
    CONVERSIONS["PETASECOND:KILOSECOND"] = \
        lambda value: (10 ** 12) * value
    CONVERSIONS["PETASECOND:MEGASECOND"] = \
        lambda value: (10 ** 9) * value
    CONVERSIONS["PETASECOND:MICROSECOND"] = \
        lambda value: (10 ** 21) * value
    CONVERSIONS["PETASECOND:MILLISECOND"] = \
        lambda value: (10 ** 18) * value
    CONVERSIONS["PETASECOND:MINUTE"] = \
        lambda: noconversion("PETASECOND", "MINUTE")
    CONVERSIONS["PETASECOND:NANOSECOND"] = \
        lambda value: (10 ** 24) * value
    CONVERSIONS["PETASECOND:PICOSECOND"] = \
        lambda value: (10 ** 27) * value
    CONVERSIONS["PETASECOND:SECOND"] = \
        lambda value: (10 ** 15) * value
    CONVERSIONS["PETASECOND:TERASECOND"] = \
        lambda value: (10 ** 3) * value
    CONVERSIONS["PETASECOND:YOCTOSECOND"] = \
        lambda value: (10 ** 39) * value
    CONVERSIONS["PETASECOND:YOTTASECOND"] = \
        lambda value: (10 ** -9) * value
    CONVERSIONS["PETASECOND:ZEPTOSECOND"] = \
        lambda value: (10 ** 36) * value
    CONVERSIONS["PETASECOND:ZETTASECOND"] = \
        lambda value: (10 ** -6) * value
    CONVERSIONS["PICOSECOND:ATOOSECOND"] = \
        lambda value: (10 ** 6) * value
    CONVERSIONS["PICOSECOND:CENTISECOND"] = \
        lambda value: (10 ** -10) * value
    CONVERSIONS["PICOSECOND:DAY"] = \
        lambda: noconversion("PICOSECOND", "DAY")
    CONVERSIONS["PICOSECOND:DECASECOND"] = \
        lambda value: (10 ** -13) * value
    CONVERSIONS["PICOSECOND:DECISECOND"] = \
        lambda value: (10 ** -11) * value
    CONVERSIONS["PICOSECOND:EXASECOND"] = \
        lambda value: (10 ** -30) * value
    CONVERSIONS["PICOSECOND:FEMTOSECOND"] = \
        lambda value: (10 ** 3) * value
    CONVERSIONS["PICOSECOND:GIGASECOND"] = \
        lambda value: (10 ** -21) * value
    CONVERSIONS["PICOSECOND:HECTOSECOND"] = \
        lambda value: (10 ** -14) * value
    CONVERSIONS["PICOSECOND:HOUR"] = \
        lambda: noconversion("PICOSECOND", "HOUR")
    CONVERSIONS["PICOSECOND:KILOSECOND"] = \
        lambda value: (10 ** -15) * value
    CONVERSIONS["PICOSECOND:MEGASECOND"] = \
        lambda value: (10 ** -18) * value
    CONVERSIONS["PICOSECOND:MICROSECOND"] = \
        lambda value: (10 ** -6) * value
    CONVERSIONS["PICOSECOND:MILLISECOND"] = \
        lambda value: (10 ** -9) * value
    CONVERSIONS["PICOSECOND:MINUTE"] = \
        lambda: noconversion("PICOSECOND", "MINUTE")
    CONVERSIONS["PICOSECOND:NANOSECOND"] = \
        lambda value: (10 ** -3) * value
    CONVERSIONS["PICOSECOND:PETASECOND"] = \
        lambda value: (10 ** -27) * value
    CONVERSIONS["PICOSECOND:SECOND"] = \
        lambda value: (10 ** -12) * value
    CONVERSIONS["PICOSECOND:TERASECOND"] = \
        lambda value: (10 ** -24) * value
    CONVERSIONS["PICOSECOND:YOCTOSECOND"] = \
        lambda value: (10 ** 12) * value
    CONVERSIONS["PICOSECOND:YOTTASECOND"] = \
        lambda value: (10 ** -36) * value
    CONVERSIONS["PICOSECOND:ZEPTOSECOND"] = \
        lambda value: (10 ** 9) * value
    CONVERSIONS["PICOSECOND:ZETTASECOND"] = \
        lambda value: (10 ** -33) * value
    CONVERSIONS["SECOND:ATOOSECOND"] = \
        lambda value: (10 ** 18) * value
    CONVERSIONS["SECOND:CENTISECOND"] = \
        lambda value: (10 ** 2) * value
    CONVERSIONS["SECOND:DAY"] = \
        lambda: noconversion("SECOND", "DAY")
    CONVERSIONS["SECOND:DECASECOND"] = \
        lambda value: (10 ** -1) * value
    CONVERSIONS["SECOND:DECISECOND"] = \
        lambda value: 10 * value
    CONVERSIONS["SECOND:EXASECOND"] = \
        lambda value: (10 ** -18) * value
    CONVERSIONS["SECOND:FEMTOSECOND"] = \
        lambda value: (10 ** 15) * value
    CONVERSIONS["SECOND:GIGASECOND"] = \
        lambda value: (10 ** -9) * value
    CONVERSIONS["SECOND:HECTOSECOND"] = \
        lambda value: (10 ** -2) * value
    CONVERSIONS["SECOND:HOUR"] = \
        lambda: noconversion("SECOND", "HOUR")
    CONVERSIONS["SECOND:KILOSECOND"] = \
        lambda value: (10 ** -3) * value
    CONVERSIONS["SECOND:MEGASECOND"] = \
        lambda value: (10 ** -6) * value
    CONVERSIONS["SECOND:MICROSECOND"] = \
        lambda value: (10 ** 6) * value
    CONVERSIONS["SECOND:MILLISECOND"] = \
        lambda value: (10 ** 3) * value
    CONVERSIONS["SECOND:MINUTE"] = \
        lambda: noconversion("SECOND", "MINUTE")
    CONVERSIONS["SECOND:NANOSECOND"] = \
        lambda value: (10 ** 9) * value
    CONVERSIONS["SECOND:PETASECOND"] = \
        lambda value: (10 ** -15) * value
    CONVERSIONS["SECOND:PICOSECOND"] = \
        lambda value: (10 ** 12) * value
    CONVERSIONS["SECOND:TERASECOND"] = \
        lambda value: (10 ** -12) * value
    CONVERSIONS["SECOND:YOCTOSECOND"] = \
        lambda value: (10 ** 24) * value
    CONVERSIONS["SECOND:YOTTASECOND"] = \
        lambda value: (10 ** -24) * value
    CONVERSIONS["SECOND:ZEPTOSECOND"] = \
        lambda value: (10 ** 21) * value
    CONVERSIONS["SECOND:ZETTASECOND"] = \
        lambda value: (10 ** -21) * value
    CONVERSIONS["TERASECOND:ATOOSECOND"] = \
        lambda value: (10 ** 30) * value
    CONVERSIONS["TERASECOND:CENTISECOND"] = \
        lambda value: (10 ** 14) * value
    CONVERSIONS["TERASECOND:DAY"] = \
        lambda: noconversion("TERASECOND", "DAY")
    CONVERSIONS["TERASECOND:DECASECOND"] = \
        lambda value: (10 ** 11) * value
    CONVERSIONS["TERASECOND:DECISECOND"] = \
        lambda value: (10 ** 13) * value
    CONVERSIONS["TERASECOND:EXASECOND"] = \
        lambda value: (10 ** -6) * value
    CONVERSIONS["TERASECOND:FEMTOSECOND"] = \
        lambda value: (10 ** 27) * value
    CONVERSIONS["TERASECOND:GIGASECOND"] = \
        lambda value: (10 ** 3) * value
    CONVERSIONS["TERASECOND:HECTOSECOND"] = \
        lambda value: (10 ** 10) * value
    CONVERSIONS["TERASECOND:HOUR"] = \
        lambda: noconversion("TERASECOND", "HOUR")
    CONVERSIONS["TERASECOND:KILOSECOND"] = \
        lambda value: (10 ** 9) * value
    CONVERSIONS["TERASECOND:MEGASECOND"] = \
        lambda value: (10 ** 6) * value
    CONVERSIONS["TERASECOND:MICROSECOND"] = \
        lambda value: (10 ** 18) * value
    CONVERSIONS["TERASECOND:MILLISECOND"] = \
        lambda value: (10 ** 15) * value
    CONVERSIONS["TERASECOND:MINUTE"] = \
        lambda: noconversion("TERASECOND", "MINUTE")
    CONVERSIONS["TERASECOND:NANOSECOND"] = \
        lambda value: (10 ** 21) * value
    CONVERSIONS["TERASECOND:PETASECOND"] = \
        lambda value: (10 ** -3) * value
    CONVERSIONS["TERASECOND:PICOSECOND"] = \
        lambda value: (10 ** 24) * value
    CONVERSIONS["TERASECOND:SECOND"] = \
        lambda value: (10 ** 12) * value
    CONVERSIONS["TERASECOND:YOCTOSECOND"] = \
        lambda value: (10 ** 36) * value
    CONVERSIONS["TERASECOND:YOTTASECOND"] = \
        lambda value: (10 ** -12) * value
    CONVERSIONS["TERASECOND:ZEPTOSECOND"] = \
        lambda value: (10 ** 33) * value
    CONVERSIONS["TERASECOND:ZETTASECOND"] = \
        lambda value: (10 ** -9) * value
    CONVERSIONS["YOCTOSECOND:ATOOSECOND"] = \
        lambda value: (10 ** -6) * value
    CONVERSIONS["YOCTOSECOND:CENTISECOND"] = \
        lambda value: (10 ** -22) * value
    CONVERSIONS["YOCTOSECOND:DAY"] = \
        lambda: noconversion("YOCTOSECOND", "DAY")
    CONVERSIONS["YOCTOSECOND:DECASECOND"] = \
        lambda value: (10 ** -25) * value
    CONVERSIONS["YOCTOSECOND:DECISECOND"] = \
        lambda value: (10 ** -23) * value
    CONVERSIONS["YOCTOSECOND:EXASECOND"] = \
        lambda value: (10 ** -42) * value
    CONVERSIONS["YOCTOSECOND:FEMTOSECOND"] = \
        lambda value: (10 ** -9) * value
    CONVERSIONS["YOCTOSECOND:GIGASECOND"] = \
        lambda value: (10 ** -33) * value
    CONVERSIONS["YOCTOSECOND:HECTOSECOND"] = \
        lambda value: (10 ** -26) * value
    CONVERSIONS["YOCTOSECOND:HOUR"] = \
        lambda: noconversion("YOCTOSECOND", "HOUR")
    CONVERSIONS["YOCTOSECOND:KILOSECOND"] = \
        lambda value: (10 ** -27) * value
    CONVERSIONS["YOCTOSECOND:MEGASECOND"] = \
        lambda value: (10 ** -30) * value
    CONVERSIONS["YOCTOSECOND:MICROSECOND"] = \
        lambda value: (10 ** -18) * value
    CONVERSIONS["YOCTOSECOND:MILLISECOND"] = \
        lambda value: (10 ** -21) * value
    CONVERSIONS["YOCTOSECOND:MINUTE"] = \
        lambda: noconversion("YOCTOSECOND", "MINUTE")
    CONVERSIONS["YOCTOSECOND:NANOSECOND"] = \
        lambda value: (10 ** -15) * value
    CONVERSIONS["YOCTOSECOND:PETASECOND"] = \
        lambda value: (10 ** -39) * value
    CONVERSIONS["YOCTOSECOND:PICOSECOND"] = \
        lambda value: (10 ** -12) * value
    CONVERSIONS["YOCTOSECOND:SECOND"] = \
        lambda value: (10 ** -24) * value
    CONVERSIONS["YOCTOSECOND:TERASECOND"] = \
        lambda value: (10 ** -36) * value
    CONVERSIONS["YOCTOSECOND:YOTTASECOND"] = \
        lambda value: (10 ** -48) * value
    CONVERSIONS["YOCTOSECOND:ZEPTOSECOND"] = \
        lambda value: (10 ** -3) * value
    CONVERSIONS["YOCTOSECOND:ZETTASECOND"] = \
        lambda value: (10 ** -45) * value
    CONVERSIONS["YOTTASECOND:ATOOSECOND"] = \
        lambda value: (10 ** 42) * value
    CONVERSIONS["YOTTASECOND:CENTISECOND"] = \
        lambda value: (10 ** 26) * value
    CONVERSIONS["YOTTASECOND:DAY"] = \
        lambda: noconversion("YOTTASECOND", "DAY")
    CONVERSIONS["YOTTASECOND:DECASECOND"] = \
        lambda value: (10 ** 23) * value
    CONVERSIONS["YOTTASECOND:DECISECOND"] = \
        lambda value: (10 ** 25) * value
    CONVERSIONS["YOTTASECOND:EXASECOND"] = \
        lambda value: (10 ** 6) * value
    CONVERSIONS["YOTTASECOND:FEMTOSECOND"] = \
        lambda value: (10 ** 39) * value
    CONVERSIONS["YOTTASECOND:GIGASECOND"] = \
        lambda value: (10 ** 15) * value
    CONVERSIONS["YOTTASECOND:HECTOSECOND"] = \
        lambda value: (10 ** 22) * value
    CONVERSIONS["YOTTASECOND:HOUR"] = \
        lambda: noconversion("YOTTASECOND", "HOUR")
    CONVERSIONS["YOTTASECOND:KILOSECOND"] = \
        lambda value: (10 ** 21) * value
    CONVERSIONS["YOTTASECOND:MEGASECOND"] = \
        lambda value: (10 ** 18) * value
    CONVERSIONS["YOTTASECOND:MICROSECOND"] = \
        lambda value: (10 ** 30) * value
    CONVERSIONS["YOTTASECOND:MILLISECOND"] = \
        lambda value: (10 ** 27) * value
    CONVERSIONS["YOTTASECOND:MINUTE"] = \
        lambda: noconversion("YOTTASECOND", "MINUTE")
    CONVERSIONS["YOTTASECOND:NANOSECOND"] = \
        lambda value: (10 ** 33) * value
    CONVERSIONS["YOTTASECOND:PETASECOND"] = \
        lambda value: (10 ** 9) * value
    CONVERSIONS["YOTTASECOND:PICOSECOND"] = \
        lambda value: (10 ** 36) * value
    CONVERSIONS["YOTTASECOND:SECOND"] = \
        lambda value: (10 ** 24) * value
    CONVERSIONS["YOTTASECOND:TERASECOND"] = \
        lambda value: (10 ** 12) * value
    CONVERSIONS["YOTTASECOND:YOCTOSECOND"] = \
        lambda value: (10 ** 48) * value
    CONVERSIONS["YOTTASECOND:ZEPTOSECOND"] = \
        lambda value: (10 ** 45) * value
    CONVERSIONS["YOTTASECOND:ZETTASECOND"] = \
        lambda value: (10 ** 3) * value
    CONVERSIONS["ZEPTOSECOND:ATOOSECOND"] = \
        lambda value: (10 ** -3) * value
    CONVERSIONS["ZEPTOSECOND:CENTISECOND"] = \
        lambda value: (10 ** -19) * value
    CONVERSIONS["ZEPTOSECOND:DAY"] = \
        lambda: noconversion("ZEPTOSECOND", "DAY")
    CONVERSIONS["ZEPTOSECOND:DECASECOND"] = \
        lambda value: (10 ** -22) * value
    CONVERSIONS["ZEPTOSECOND:DECISECOND"] = \
        lambda value: (10 ** -20) * value
    CONVERSIONS["ZEPTOSECOND:EXASECOND"] = \
        lambda value: (10 ** -39) * value
    CONVERSIONS["ZEPTOSECOND:FEMTOSECOND"] = \
        lambda value: (10 ** -6) * value
    CONVERSIONS["ZEPTOSECOND:GIGASECOND"] = \
        lambda value: (10 ** -30) * value
    CONVERSIONS["ZEPTOSECOND:HECTOSECOND"] = \
        lambda value: (10 ** -23) * value
    CONVERSIONS["ZEPTOSECOND:HOUR"] = \
        lambda: noconversion("ZEPTOSECOND", "HOUR")
    CONVERSIONS["ZEPTOSECOND:KILOSECOND"] = \
        lambda value: (10 ** -24) * value
    CONVERSIONS["ZEPTOSECOND:MEGASECOND"] = \
        lambda value: (10 ** -27) * value
    CONVERSIONS["ZEPTOSECOND:MICROSECOND"] = \
        lambda value: (10 ** -15) * value
    CONVERSIONS["ZEPTOSECOND:MILLISECOND"] = \
        lambda value: (10 ** -18) * value
    CONVERSIONS["ZEPTOSECOND:MINUTE"] = \
        lambda: noconversion("ZEPTOSECOND", "MINUTE")
    CONVERSIONS["ZEPTOSECOND:NANOSECOND"] = \
        lambda value: (10 ** -12) * value
    CONVERSIONS["ZEPTOSECOND:PETASECOND"] = \
        lambda value: (10 ** -36) * value
    CONVERSIONS["ZEPTOSECOND:PICOSECOND"] = \
        lambda value: (10 ** -9) * value
    CONVERSIONS["ZEPTOSECOND:SECOND"] = \
        lambda value: (10 ** -21) * value
    CONVERSIONS["ZEPTOSECOND:TERASECOND"] = \
        lambda value: (10 ** -33) * value
    CONVERSIONS["ZEPTOSECOND:YOCTOSECOND"] = \
        lambda value: (10 ** 3) * value
    CONVERSIONS["ZEPTOSECOND:YOTTASECOND"] = \
        lambda value: (10 ** -45) * value
    CONVERSIONS["ZEPTOSECOND:ZETTASECOND"] = \
        lambda value: (10 ** -42) * value
    CONVERSIONS["ZETTASECOND:ATOOSECOND"] = \
        lambda value: (10 ** 39) * value
    CONVERSIONS["ZETTASECOND:CENTISECOND"] = \
        lambda value: (10 ** 23) * value
    CONVERSIONS["ZETTASECOND:DAY"] = \
        lambda: noconversion("ZETTASECOND", "DAY")
    CONVERSIONS["ZETTASECOND:DECASECOND"] = \
        lambda value: (10 ** 20) * value
    CONVERSIONS["ZETTASECOND:DECISECOND"] = \
        lambda value: (10 ** 22) * value
    CONVERSIONS["ZETTASECOND:EXASECOND"] = \
        lambda value: (10 ** 3) * value
    CONVERSIONS["ZETTASECOND:FEMTOSECOND"] = \
        lambda value: (10 ** 36) * value
    CONVERSIONS["ZETTASECOND:GIGASECOND"] = \
        lambda value: (10 ** 12) * value
    CONVERSIONS["ZETTASECOND:HECTOSECOND"] = \
        lambda value: (10 ** 19) * value
    CONVERSIONS["ZETTASECOND:HOUR"] = \
        lambda: noconversion("ZETTASECOND", "HOUR")
    CONVERSIONS["ZETTASECOND:KILOSECOND"] = \
        lambda value: (10 ** 18) * value
    CONVERSIONS["ZETTASECOND:MEGASECOND"] = \
        lambda value: (10 ** 15) * value
    CONVERSIONS["ZETTASECOND:MICROSECOND"] = \
        lambda value: (10 ** 27) * value
    CONVERSIONS["ZETTASECOND:MILLISECOND"] = \
        lambda value: (10 ** 24) * value
    CONVERSIONS["ZETTASECOND:MINUTE"] = \
        lambda: noconversion("ZETTASECOND", "MINUTE")
    CONVERSIONS["ZETTASECOND:NANOSECOND"] = \
        lambda value: (10 ** 30) * value
    CONVERSIONS["ZETTASECOND:PETASECOND"] = \
        lambda value: (10 ** 6) * value
    CONVERSIONS["ZETTASECOND:PICOSECOND"] = \
        lambda value: (10 ** 33) * value
    CONVERSIONS["ZETTASECOND:SECOND"] = \
        lambda value: (10 ** 21) * value
    CONVERSIONS["ZETTASECOND:TERASECOND"] = \
        lambda value: (10 ** 9) * value
    CONVERSIONS["ZETTASECOND:YOCTOSECOND"] = \
        lambda value: (10 ** 45) * value
    CONVERSIONS["ZETTASECOND:YOTTASECOND"] = \
        lambda value: (10 ** -3) * value
    CONVERSIONS["ZETTASECOND:ZEPTOSECOND"] = \
        lambda value: (10 ** 42) * value

    SYMBOLS = dict()
    SYMBOLS["ATOOSECOND"] = "as"
    SYMBOLS["CENTISECOND"] = "cs"
    SYMBOLS["DAY"] = "d"
    SYMBOLS["DECASECOND"] = "das"
    SYMBOLS["DECISECOND"] = "ds"
    SYMBOLS["EXASECOND"] = "Es"
    SYMBOLS["FEMTOSECOND"] = "fs"
    SYMBOLS["GIGASECOND"] = "Gs"
    SYMBOLS["HECTOSECOND"] = "hs"
    SYMBOLS["HOUR"] = "h"
    SYMBOLS["KILOSECOND"] = "ks"
    SYMBOLS["MEGASECOND"] = "Ms"
    SYMBOLS["MICROSECOND"] = "µs"
    SYMBOLS["MILLISECOND"] = "ms"
    SYMBOLS["MINUTE"] = "min"
    SYMBOLS["NANOSECOND"] = "ns"
    SYMBOLS["PETASECOND"] = "Ps"
    SYMBOLS["PICOSECOND"] = "ps"
    SYMBOLS["SECOND"] = "s"
    SYMBOLS["TERASECOND"] = "Ts"
    SYMBOLS["YOCTOSECOND"] = "ys"
    SYMBOLS["YOTTASECOND"] = "Ys"
    SYMBOLS["ZEPTOSECOND"] = "zs"
    SYMBOLS["ZETTASECOND"] = "Zs"

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
