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
    CONVERSIONS["AV:CV"] = \
        lambda value: (10 ** -16) * value
    CONVERSIONS["AV:DAV"] = \
        lambda value: (10 ** -19) * value
    CONVERSIONS["AV:DV"] = \
        lambda value: (10 ** -17) * value
    CONVERSIONS["AV:EXAV"] = \
        lambda value: (10 ** -36) * value
    CONVERSIONS["AV:FV"] = \
        lambda value: (10 ** -3) * value
    CONVERSIONS["AV:GIGAV"] = \
        lambda value: (10 ** -27) * value
    CONVERSIONS["AV:HV"] = \
        lambda value: (10 ** -20) * value
    CONVERSIONS["AV:KV"] = \
        lambda value: (10 ** -21) * value
    CONVERSIONS["AV:MEGAV"] = \
        lambda value: (10 ** -24) * value
    CONVERSIONS["AV:MICROV"] = \
        lambda value: (10 ** -12) * value
    CONVERSIONS["AV:MV"] = \
        lambda value: (10 ** -15) * value
    CONVERSIONS["AV:NV"] = \
        lambda value: (10 ** -9) * value
    CONVERSIONS["AV:PETAV"] = \
        lambda value: (10 ** -33) * value
    CONVERSIONS["AV:PV"] = \
        lambda value: (10 ** -6) * value
    CONVERSIONS["AV:TERAV"] = \
        lambda value: (10 ** -30) * value
    CONVERSIONS["AV:V"] = \
        lambda value: (10 ** -18) * value
    CONVERSIONS["AV:YOTTAV"] = \
        lambda value: (10 ** -42) * value
    CONVERSIONS["AV:YV"] = \
        lambda value: (10 ** 6) * value
    CONVERSIONS["AV:ZETTAV"] = \
        lambda value: (10 ** -39) * value
    CONVERSIONS["AV:ZV"] = \
        lambda value: (10 ** 3) * value
    CONVERSIONS["CV:AV"] = \
        lambda value: (10 ** 16) * value
    CONVERSIONS["CV:DAV"] = \
        lambda value: (10 ** -3) * value
    CONVERSIONS["CV:DV"] = \
        lambda value: (10 ** -1) * value
    CONVERSIONS["CV:EXAV"] = \
        lambda value: (10 ** -20) * value
    CONVERSIONS["CV:FV"] = \
        lambda value: (10 ** 13) * value
    CONVERSIONS["CV:GIGAV"] = \
        lambda value: (10 ** -11) * value
    CONVERSIONS["CV:HV"] = \
        lambda value: (10 ** -4) * value
    CONVERSIONS["CV:KV"] = \
        lambda value: (10 ** -5) * value
    CONVERSIONS["CV:MEGAV"] = \
        lambda value: (10 ** -8) * value
    CONVERSIONS["CV:MICROV"] = \
        lambda value: (10 ** 4) * value
    CONVERSIONS["CV:MV"] = \
        lambda value: 10 * value
    CONVERSIONS["CV:NV"] = \
        lambda value: (10 ** 7) * value
    CONVERSIONS["CV:PETAV"] = \
        lambda value: (10 ** -17) * value
    CONVERSIONS["CV:PV"] = \
        lambda value: (10 ** 10) * value
    CONVERSIONS["CV:TERAV"] = \
        lambda value: (10 ** -14) * value
    CONVERSIONS["CV:V"] = \
        lambda value: (10 ** -2) * value
    CONVERSIONS["CV:YOTTAV"] = \
        lambda value: (10 ** -26) * value
    CONVERSIONS["CV:YV"] = \
        lambda value: (10 ** 22) * value
    CONVERSIONS["CV:ZETTAV"] = \
        lambda value: (10 ** -23) * value
    CONVERSIONS["CV:ZV"] = \
        lambda value: (10 ** 19) * value
    CONVERSIONS["DAV:AV"] = \
        lambda value: (10 ** 19) * value
    CONVERSIONS["DAV:CV"] = \
        lambda value: (10 ** 3) * value
    CONVERSIONS["DAV:DV"] = \
        lambda value: (10 ** 2) * value
    CONVERSIONS["DAV:EXAV"] = \
        lambda value: (10 ** -17) * value
    CONVERSIONS["DAV:FV"] = \
        lambda value: (10 ** 16) * value
    CONVERSIONS["DAV:GIGAV"] = \
        lambda value: (10 ** -8) * value
    CONVERSIONS["DAV:HV"] = \
        lambda value: (10 ** -1) * value
    CONVERSIONS["DAV:KV"] = \
        lambda value: (10 ** -2) * value
    CONVERSIONS["DAV:MEGAV"] = \
        lambda value: (10 ** -5) * value
    CONVERSIONS["DAV:MICROV"] = \
        lambda value: (10 ** 7) * value
    CONVERSIONS["DAV:MV"] = \
        lambda value: (10 ** 4) * value
    CONVERSIONS["DAV:NV"] = \
        lambda value: (10 ** 10) * value
    CONVERSIONS["DAV:PETAV"] = \
        lambda value: (10 ** -14) * value
    CONVERSIONS["DAV:PV"] = \
        lambda value: (10 ** 13) * value
    CONVERSIONS["DAV:TERAV"] = \
        lambda value: (10 ** -11) * value
    CONVERSIONS["DAV:V"] = \
        lambda value: 10 * value
    CONVERSIONS["DAV:YOTTAV"] = \
        lambda value: (10 ** -23) * value
    CONVERSIONS["DAV:YV"] = \
        lambda value: (10 ** 25) * value
    CONVERSIONS["DAV:ZETTAV"] = \
        lambda value: (10 ** -20) * value
    CONVERSIONS["DAV:ZV"] = \
        lambda value: (10 ** 22) * value
    CONVERSIONS["DV:AV"] = \
        lambda value: (10 ** 17) * value
    CONVERSIONS["DV:CV"] = \
        lambda value: 10 * value
    CONVERSIONS["DV:DAV"] = \
        lambda value: (10 ** -2) * value
    CONVERSIONS["DV:EXAV"] = \
        lambda value: (10 ** -19) * value
    CONVERSIONS["DV:FV"] = \
        lambda value: (10 ** 14) * value
    CONVERSIONS["DV:GIGAV"] = \
        lambda value: (10 ** -10) * value
    CONVERSIONS["DV:HV"] = \
        lambda value: (10 ** -3) * value
    CONVERSIONS["DV:KV"] = \
        lambda value: (10 ** -4) * value
    CONVERSIONS["DV:MEGAV"] = \
        lambda value: (10 ** -7) * value
    CONVERSIONS["DV:MICROV"] = \
        lambda value: (10 ** 5) * value
    CONVERSIONS["DV:MV"] = \
        lambda value: (10 ** 2) * value
    CONVERSIONS["DV:NV"] = \
        lambda value: (10 ** 8) * value
    CONVERSIONS["DV:PETAV"] = \
        lambda value: (10 ** -16) * value
    CONVERSIONS["DV:PV"] = \
        lambda value: (10 ** 11) * value
    CONVERSIONS["DV:TERAV"] = \
        lambda value: (10 ** -13) * value
    CONVERSIONS["DV:V"] = \
        lambda value: (10 ** -1) * value
    CONVERSIONS["DV:YOTTAV"] = \
        lambda value: (10 ** -25) * value
    CONVERSIONS["DV:YV"] = \
        lambda value: (10 ** 23) * value
    CONVERSIONS["DV:ZETTAV"] = \
        lambda value: (10 ** -22) * value
    CONVERSIONS["DV:ZV"] = \
        lambda value: (10 ** 20) * value
    CONVERSIONS["EXAV:AV"] = \
        lambda value: (10 ** 36) * value
    CONVERSIONS["EXAV:CV"] = \
        lambda value: (10 ** 20) * value
    CONVERSIONS["EXAV:DAV"] = \
        lambda value: (10 ** 17) * value
    CONVERSIONS["EXAV:DV"] = \
        lambda value: (10 ** 19) * value
    CONVERSIONS["EXAV:FV"] = \
        lambda value: (10 ** 33) * value
    CONVERSIONS["EXAV:GIGAV"] = \
        lambda value: (10 ** 9) * value
    CONVERSIONS["EXAV:HV"] = \
        lambda value: (10 ** 16) * value
    CONVERSIONS["EXAV:KV"] = \
        lambda value: (10 ** 15) * value
    CONVERSIONS["EXAV:MEGAV"] = \
        lambda value: (10 ** 12) * value
    CONVERSIONS["EXAV:MICROV"] = \
        lambda value: (10 ** 24) * value
    CONVERSIONS["EXAV:MV"] = \
        lambda value: (10 ** 21) * value
    CONVERSIONS["EXAV:NV"] = \
        lambda value: (10 ** 27) * value
    CONVERSIONS["EXAV:PETAV"] = \
        lambda value: (10 ** 3) * value
    CONVERSIONS["EXAV:PV"] = \
        lambda value: (10 ** 30) * value
    CONVERSIONS["EXAV:TERAV"] = \
        lambda value: (10 ** 6) * value
    CONVERSIONS["EXAV:V"] = \
        lambda value: (10 ** 18) * value
    CONVERSIONS["EXAV:YOTTAV"] = \
        lambda value: (10 ** -6) * value
    CONVERSIONS["EXAV:YV"] = \
        lambda value: (10 ** 42) * value
    CONVERSIONS["EXAV:ZETTAV"] = \
        lambda value: (10 ** -3) * value
    CONVERSIONS["EXAV:ZV"] = \
        lambda value: (10 ** 39) * value
    CONVERSIONS["FV:AV"] = \
        lambda value: (10 ** 3) * value
    CONVERSIONS["FV:CV"] = \
        lambda value: (10 ** -13) * value
    CONVERSIONS["FV:DAV"] = \
        lambda value: (10 ** -16) * value
    CONVERSIONS["FV:DV"] = \
        lambda value: (10 ** -14) * value
    CONVERSIONS["FV:EXAV"] = \
        lambda value: (10 ** -33) * value
    CONVERSIONS["FV:GIGAV"] = \
        lambda value: (10 ** -24) * value
    CONVERSIONS["FV:HV"] = \
        lambda value: (10 ** -17) * value
    CONVERSIONS["FV:KV"] = \
        lambda value: (10 ** -18) * value
    CONVERSIONS["FV:MEGAV"] = \
        lambda value: (10 ** -21) * value
    CONVERSIONS["FV:MICROV"] = \
        lambda value: (10 ** -9) * value
    CONVERSIONS["FV:MV"] = \
        lambda value: (10 ** -12) * value
    CONVERSIONS["FV:NV"] = \
        lambda value: (10 ** -6) * value
    CONVERSIONS["FV:PETAV"] = \
        lambda value: (10 ** -30) * value
    CONVERSIONS["FV:PV"] = \
        lambda value: (10 ** -3) * value
    CONVERSIONS["FV:TERAV"] = \
        lambda value: (10 ** -27) * value
    CONVERSIONS["FV:V"] = \
        lambda value: (10 ** -15) * value
    CONVERSIONS["FV:YOTTAV"] = \
        lambda value: (10 ** -39) * value
    CONVERSIONS["FV:YV"] = \
        lambda value: (10 ** 9) * value
    CONVERSIONS["FV:ZETTAV"] = \
        lambda value: (10 ** -36) * value
    CONVERSIONS["FV:ZV"] = \
        lambda value: (10 ** 6) * value
    CONVERSIONS["GIGAV:AV"] = \
        lambda value: (10 ** 27) * value
    CONVERSIONS["GIGAV:CV"] = \
        lambda value: (10 ** 11) * value
    CONVERSIONS["GIGAV:DAV"] = \
        lambda value: (10 ** 8) * value
    CONVERSIONS["GIGAV:DV"] = \
        lambda value: (10 ** 10) * value
    CONVERSIONS["GIGAV:EXAV"] = \
        lambda value: (10 ** -9) * value
    CONVERSIONS["GIGAV:FV"] = \
        lambda value: (10 ** 24) * value
    CONVERSIONS["GIGAV:HV"] = \
        lambda value: (10 ** 7) * value
    CONVERSIONS["GIGAV:KV"] = \
        lambda value: (10 ** 6) * value
    CONVERSIONS["GIGAV:MEGAV"] = \
        lambda value: (10 ** 3) * value
    CONVERSIONS["GIGAV:MICROV"] = \
        lambda value: (10 ** 15) * value
    CONVERSIONS["GIGAV:MV"] = \
        lambda value: (10 ** 12) * value
    CONVERSIONS["GIGAV:NV"] = \
        lambda value: (10 ** 18) * value
    CONVERSIONS["GIGAV:PETAV"] = \
        lambda value: (10 ** -6) * value
    CONVERSIONS["GIGAV:PV"] = \
        lambda value: (10 ** 21) * value
    CONVERSIONS["GIGAV:TERAV"] = \
        lambda value: (10 ** -3) * value
    CONVERSIONS["GIGAV:V"] = \
        lambda value: (10 ** 9) * value
    CONVERSIONS["GIGAV:YOTTAV"] = \
        lambda value: (10 ** -15) * value
    CONVERSIONS["GIGAV:YV"] = \
        lambda value: (10 ** 33) * value
    CONVERSIONS["GIGAV:ZETTAV"] = \
        lambda value: (10 ** -12) * value
    CONVERSIONS["GIGAV:ZV"] = \
        lambda value: (10 ** 30) * value
    CONVERSIONS["HV:AV"] = \
        lambda value: (10 ** 20) * value
    CONVERSIONS["HV:CV"] = \
        lambda value: (10 ** 4) * value
    CONVERSIONS["HV:DAV"] = \
        lambda value: 10 * value
    CONVERSIONS["HV:DV"] = \
        lambda value: (10 ** 3) * value
    CONVERSIONS["HV:EXAV"] = \
        lambda value: (10 ** -16) * value
    CONVERSIONS["HV:FV"] = \
        lambda value: (10 ** 17) * value
    CONVERSIONS["HV:GIGAV"] = \
        lambda value: (10 ** -7) * value
    CONVERSIONS["HV:KV"] = \
        lambda value: (10 ** -1) * value
    CONVERSIONS["HV:MEGAV"] = \
        lambda value: (10 ** -4) * value
    CONVERSIONS["HV:MICROV"] = \
        lambda value: (10 ** 8) * value
    CONVERSIONS["HV:MV"] = \
        lambda value: (10 ** 5) * value
    CONVERSIONS["HV:NV"] = \
        lambda value: (10 ** 11) * value
    CONVERSIONS["HV:PETAV"] = \
        lambda value: (10 ** -13) * value
    CONVERSIONS["HV:PV"] = \
        lambda value: (10 ** 14) * value
    CONVERSIONS["HV:TERAV"] = \
        lambda value: (10 ** -10) * value
    CONVERSIONS["HV:V"] = \
        lambda value: (10 ** 2) * value
    CONVERSIONS["HV:YOTTAV"] = \
        lambda value: (10 ** -22) * value
    CONVERSIONS["HV:YV"] = \
        lambda value: (10 ** 26) * value
    CONVERSIONS["HV:ZETTAV"] = \
        lambda value: (10 ** -19) * value
    CONVERSIONS["HV:ZV"] = \
        lambda value: (10 ** 23) * value
    CONVERSIONS["KV:AV"] = \
        lambda value: (10 ** 21) * value
    CONVERSIONS["KV:CV"] = \
        lambda value: (10 ** 5) * value
    CONVERSIONS["KV:DAV"] = \
        lambda value: (10 ** 2) * value
    CONVERSIONS["KV:DV"] = \
        lambda value: (10 ** 4) * value
    CONVERSIONS["KV:EXAV"] = \
        lambda value: (10 ** -15) * value
    CONVERSIONS["KV:FV"] = \
        lambda value: (10 ** 18) * value
    CONVERSIONS["KV:GIGAV"] = \
        lambda value: (10 ** -6) * value
    CONVERSIONS["KV:HV"] = \
        lambda value: 10 * value
    CONVERSIONS["KV:MEGAV"] = \
        lambda value: (10 ** -3) * value
    CONVERSIONS["KV:MICROV"] = \
        lambda value: (10 ** 9) * value
    CONVERSIONS["KV:MV"] = \
        lambda value: (10 ** 6) * value
    CONVERSIONS["KV:NV"] = \
        lambda value: (10 ** 12) * value
    CONVERSIONS["KV:PETAV"] = \
        lambda value: (10 ** -12) * value
    CONVERSIONS["KV:PV"] = \
        lambda value: (10 ** 15) * value
    CONVERSIONS["KV:TERAV"] = \
        lambda value: (10 ** -9) * value
    CONVERSIONS["KV:V"] = \
        lambda value: (10 ** 3) * value
    CONVERSIONS["KV:YOTTAV"] = \
        lambda value: (10 ** -21) * value
    CONVERSIONS["KV:YV"] = \
        lambda value: (10 ** 27) * value
    CONVERSIONS["KV:ZETTAV"] = \
        lambda value: (10 ** -18) * value
    CONVERSIONS["KV:ZV"] = \
        lambda value: (10 ** 24) * value
    CONVERSIONS["MEGAV:AV"] = \
        lambda value: (10 ** 24) * value
    CONVERSIONS["MEGAV:CV"] = \
        lambda value: (10 ** 8) * value
    CONVERSIONS["MEGAV:DAV"] = \
        lambda value: (10 ** 5) * value
    CONVERSIONS["MEGAV:DV"] = \
        lambda value: (10 ** 7) * value
    CONVERSIONS["MEGAV:EXAV"] = \
        lambda value: (10 ** -12) * value
    CONVERSIONS["MEGAV:FV"] = \
        lambda value: (10 ** 21) * value
    CONVERSIONS["MEGAV:GIGAV"] = \
        lambda value: (10 ** -3) * value
    CONVERSIONS["MEGAV:HV"] = \
        lambda value: (10 ** 4) * value
    CONVERSIONS["MEGAV:KV"] = \
        lambda value: (10 ** 3) * value
    CONVERSIONS["MEGAV:MICROV"] = \
        lambda value: (10 ** 12) * value
    CONVERSIONS["MEGAV:MV"] = \
        lambda value: (10 ** 9) * value
    CONVERSIONS["MEGAV:NV"] = \
        lambda value: (10 ** 15) * value
    CONVERSIONS["MEGAV:PETAV"] = \
        lambda value: (10 ** -9) * value
    CONVERSIONS["MEGAV:PV"] = \
        lambda value: (10 ** 18) * value
    CONVERSIONS["MEGAV:TERAV"] = \
        lambda value: (10 ** -6) * value
    CONVERSIONS["MEGAV:V"] = \
        lambda value: (10 ** 6) * value
    CONVERSIONS["MEGAV:YOTTAV"] = \
        lambda value: (10 ** -18) * value
    CONVERSIONS["MEGAV:YV"] = \
        lambda value: (10 ** 30) * value
    CONVERSIONS["MEGAV:ZETTAV"] = \
        lambda value: (10 ** -15) * value
    CONVERSIONS["MEGAV:ZV"] = \
        lambda value: (10 ** 27) * value
    CONVERSIONS["MICROV:AV"] = \
        lambda value: (10 ** 12) * value
    CONVERSIONS["MICROV:CV"] = \
        lambda value: (10 ** -4) * value
    CONVERSIONS["MICROV:DAV"] = \
        lambda value: (10 ** -7) * value
    CONVERSIONS["MICROV:DV"] = \
        lambda value: (10 ** -5) * value
    CONVERSIONS["MICROV:EXAV"] = \
        lambda value: (10 ** -24) * value
    CONVERSIONS["MICROV:FV"] = \
        lambda value: (10 ** 9) * value
    CONVERSIONS["MICROV:GIGAV"] = \
        lambda value: (10 ** -15) * value
    CONVERSIONS["MICROV:HV"] = \
        lambda value: (10 ** -8) * value
    CONVERSIONS["MICROV:KV"] = \
        lambda value: (10 ** -9) * value
    CONVERSIONS["MICROV:MEGAV"] = \
        lambda value: (10 ** -12) * value
    CONVERSIONS["MICROV:MV"] = \
        lambda value: (10 ** -3) * value
    CONVERSIONS["MICROV:NV"] = \
        lambda value: (10 ** 3) * value
    CONVERSIONS["MICROV:PETAV"] = \
        lambda value: (10 ** -21) * value
    CONVERSIONS["MICROV:PV"] = \
        lambda value: (10 ** 6) * value
    CONVERSIONS["MICROV:TERAV"] = \
        lambda value: (10 ** -18) * value
    CONVERSIONS["MICROV:V"] = \
        lambda value: (10 ** -6) * value
    CONVERSIONS["MICROV:YOTTAV"] = \
        lambda value: (10 ** -30) * value
    CONVERSIONS["MICROV:YV"] = \
        lambda value: (10 ** 18) * value
    CONVERSIONS["MICROV:ZETTAV"] = \
        lambda value: (10 ** -27) * value
    CONVERSIONS["MICROV:ZV"] = \
        lambda value: (10 ** 15) * value
    CONVERSIONS["MV:AV"] = \
        lambda value: (10 ** 15) * value
    CONVERSIONS["MV:CV"] = \
        lambda value: (10 ** -1) * value
    CONVERSIONS["MV:DAV"] = \
        lambda value: (10 ** -4) * value
    CONVERSIONS["MV:DV"] = \
        lambda value: (10 ** -2) * value
    CONVERSIONS["MV:EXAV"] = \
        lambda value: (10 ** -21) * value
    CONVERSIONS["MV:FV"] = \
        lambda value: (10 ** 12) * value
    CONVERSIONS["MV:GIGAV"] = \
        lambda value: (10 ** -12) * value
    CONVERSIONS["MV:HV"] = \
        lambda value: (10 ** -5) * value
    CONVERSIONS["MV:KV"] = \
        lambda value: (10 ** -6) * value
    CONVERSIONS["MV:MEGAV"] = \
        lambda value: (10 ** -9) * value
    CONVERSIONS["MV:MICROV"] = \
        lambda value: (10 ** 3) * value
    CONVERSIONS["MV:NV"] = \
        lambda value: (10 ** 6) * value
    CONVERSIONS["MV:PETAV"] = \
        lambda value: (10 ** -18) * value
    CONVERSIONS["MV:PV"] = \
        lambda value: (10 ** 9) * value
    CONVERSIONS["MV:TERAV"] = \
        lambda value: (10 ** -15) * value
    CONVERSIONS["MV:V"] = \
        lambda value: (10 ** -3) * value
    CONVERSIONS["MV:YOTTAV"] = \
        lambda value: (10 ** -27) * value
    CONVERSIONS["MV:YV"] = \
        lambda value: (10 ** 21) * value
    CONVERSIONS["MV:ZETTAV"] = \
        lambda value: (10 ** -24) * value
    CONVERSIONS["MV:ZV"] = \
        lambda value: (10 ** 18) * value
    CONVERSIONS["NV:AV"] = \
        lambda value: (10 ** 9) * value
    CONVERSIONS["NV:CV"] = \
        lambda value: (10 ** -7) * value
    CONVERSIONS["NV:DAV"] = \
        lambda value: (10 ** -10) * value
    CONVERSIONS["NV:DV"] = \
        lambda value: (10 ** -8) * value
    CONVERSIONS["NV:EXAV"] = \
        lambda value: (10 ** -27) * value
    CONVERSIONS["NV:FV"] = \
        lambda value: (10 ** 6) * value
    CONVERSIONS["NV:GIGAV"] = \
        lambda value: (10 ** -18) * value
    CONVERSIONS["NV:HV"] = \
        lambda value: (10 ** -11) * value
    CONVERSIONS["NV:KV"] = \
        lambda value: (10 ** -12) * value
    CONVERSIONS["NV:MEGAV"] = \
        lambda value: (10 ** -15) * value
    CONVERSIONS["NV:MICROV"] = \
        lambda value: (10 ** -3) * value
    CONVERSIONS["NV:MV"] = \
        lambda value: (10 ** -6) * value
    CONVERSIONS["NV:PETAV"] = \
        lambda value: (10 ** -24) * value
    CONVERSIONS["NV:PV"] = \
        lambda value: (10 ** 3) * value
    CONVERSIONS["NV:TERAV"] = \
        lambda value: (10 ** -21) * value
    CONVERSIONS["NV:V"] = \
        lambda value: (10 ** -9) * value
    CONVERSIONS["NV:YOTTAV"] = \
        lambda value: (10 ** -33) * value
    CONVERSIONS["NV:YV"] = \
        lambda value: (10 ** 15) * value
    CONVERSIONS["NV:ZETTAV"] = \
        lambda value: (10 ** -30) * value
    CONVERSIONS["NV:ZV"] = \
        lambda value: (10 ** 12) * value
    CONVERSIONS["PETAV:AV"] = \
        lambda value: (10 ** 33) * value
    CONVERSIONS["PETAV:CV"] = \
        lambda value: (10 ** 17) * value
    CONVERSIONS["PETAV:DAV"] = \
        lambda value: (10 ** 14) * value
    CONVERSIONS["PETAV:DV"] = \
        lambda value: (10 ** 16) * value
    CONVERSIONS["PETAV:EXAV"] = \
        lambda value: (10 ** -3) * value
    CONVERSIONS["PETAV:FV"] = \
        lambda value: (10 ** 30) * value
    CONVERSIONS["PETAV:GIGAV"] = \
        lambda value: (10 ** 6) * value
    CONVERSIONS["PETAV:HV"] = \
        lambda value: (10 ** 13) * value
    CONVERSIONS["PETAV:KV"] = \
        lambda value: (10 ** 12) * value
    CONVERSIONS["PETAV:MEGAV"] = \
        lambda value: (10 ** 9) * value
    CONVERSIONS["PETAV:MICROV"] = \
        lambda value: (10 ** 21) * value
    CONVERSIONS["PETAV:MV"] = \
        lambda value: (10 ** 18) * value
    CONVERSIONS["PETAV:NV"] = \
        lambda value: (10 ** 24) * value
    CONVERSIONS["PETAV:PV"] = \
        lambda value: (10 ** 27) * value
    CONVERSIONS["PETAV:TERAV"] = \
        lambda value: (10 ** 3) * value
    CONVERSIONS["PETAV:V"] = \
        lambda value: (10 ** 15) * value
    CONVERSIONS["PETAV:YOTTAV"] = \
        lambda value: (10 ** -9) * value
    CONVERSIONS["PETAV:YV"] = \
        lambda value: (10 ** 39) * value
    CONVERSIONS["PETAV:ZETTAV"] = \
        lambda value: (10 ** -6) * value
    CONVERSIONS["PETAV:ZV"] = \
        lambda value: (10 ** 36) * value
    CONVERSIONS["PV:AV"] = \
        lambda value: (10 ** 6) * value
    CONVERSIONS["PV:CV"] = \
        lambda value: (10 ** -10) * value
    CONVERSIONS["PV:DAV"] = \
        lambda value: (10 ** -13) * value
    CONVERSIONS["PV:DV"] = \
        lambda value: (10 ** -11) * value
    CONVERSIONS["PV:EXAV"] = \
        lambda value: (10 ** -30) * value
    CONVERSIONS["PV:FV"] = \
        lambda value: (10 ** 3) * value
    CONVERSIONS["PV:GIGAV"] = \
        lambda value: (10 ** -21) * value
    CONVERSIONS["PV:HV"] = \
        lambda value: (10 ** -14) * value
    CONVERSIONS["PV:KV"] = \
        lambda value: (10 ** -15) * value
    CONVERSIONS["PV:MEGAV"] = \
        lambda value: (10 ** -18) * value
    CONVERSIONS["PV:MICROV"] = \
        lambda value: (10 ** -6) * value
    CONVERSIONS["PV:MV"] = \
        lambda value: (10 ** -9) * value
    CONVERSIONS["PV:NV"] = \
        lambda value: (10 ** -3) * value
    CONVERSIONS["PV:PETAV"] = \
        lambda value: (10 ** -27) * value
    CONVERSIONS["PV:TERAV"] = \
        lambda value: (10 ** -24) * value
    CONVERSIONS["PV:V"] = \
        lambda value: (10 ** -12) * value
    CONVERSIONS["PV:YOTTAV"] = \
        lambda value: (10 ** -36) * value
    CONVERSIONS["PV:YV"] = \
        lambda value: (10 ** 12) * value
    CONVERSIONS["PV:ZETTAV"] = \
        lambda value: (10 ** -33) * value
    CONVERSIONS["PV:ZV"] = \
        lambda value: (10 ** 9) * value
    CONVERSIONS["TERAV:AV"] = \
        lambda value: (10 ** 30) * value
    CONVERSIONS["TERAV:CV"] = \
        lambda value: (10 ** 14) * value
    CONVERSIONS["TERAV:DAV"] = \
        lambda value: (10 ** 11) * value
    CONVERSIONS["TERAV:DV"] = \
        lambda value: (10 ** 13) * value
    CONVERSIONS["TERAV:EXAV"] = \
        lambda value: (10 ** -6) * value
    CONVERSIONS["TERAV:FV"] = \
        lambda value: (10 ** 27) * value
    CONVERSIONS["TERAV:GIGAV"] = \
        lambda value: (10 ** 3) * value
    CONVERSIONS["TERAV:HV"] = \
        lambda value: (10 ** 10) * value
    CONVERSIONS["TERAV:KV"] = \
        lambda value: (10 ** 9) * value
    CONVERSIONS["TERAV:MEGAV"] = \
        lambda value: (10 ** 6) * value
    CONVERSIONS["TERAV:MICROV"] = \
        lambda value: (10 ** 18) * value
    CONVERSIONS["TERAV:MV"] = \
        lambda value: (10 ** 15) * value
    CONVERSIONS["TERAV:NV"] = \
        lambda value: (10 ** 21) * value
    CONVERSIONS["TERAV:PETAV"] = \
        lambda value: (10 ** -3) * value
    CONVERSIONS["TERAV:PV"] = \
        lambda value: (10 ** 24) * value
    CONVERSIONS["TERAV:V"] = \
        lambda value: (10 ** 12) * value
    CONVERSIONS["TERAV:YOTTAV"] = \
        lambda value: (10 ** -12) * value
    CONVERSIONS["TERAV:YV"] = \
        lambda value: (10 ** 36) * value
    CONVERSIONS["TERAV:ZETTAV"] = \
        lambda value: (10 ** -9) * value
    CONVERSIONS["TERAV:ZV"] = \
        lambda value: (10 ** 33) * value
    CONVERSIONS["V:AV"] = \
        lambda value: (10 ** 18) * value
    CONVERSIONS["V:CV"] = \
        lambda value: (10 ** 2) * value
    CONVERSIONS["V:DAV"] = \
        lambda value: (10 ** -1) * value
    CONVERSIONS["V:DV"] = \
        lambda value: 10 * value
    CONVERSIONS["V:EXAV"] = \
        lambda value: (10 ** -18) * value
    CONVERSIONS["V:FV"] = \
        lambda value: (10 ** 15) * value
    CONVERSIONS["V:GIGAV"] = \
        lambda value: (10 ** -9) * value
    CONVERSIONS["V:HV"] = \
        lambda value: (10 ** -2) * value
    CONVERSIONS["V:KV"] = \
        lambda value: (10 ** -3) * value
    CONVERSIONS["V:MEGAV"] = \
        lambda value: (10 ** -6) * value
    CONVERSIONS["V:MICROV"] = \
        lambda value: (10 ** 6) * value
    CONVERSIONS["V:MV"] = \
        lambda value: (10 ** 3) * value
    CONVERSIONS["V:NV"] = \
        lambda value: (10 ** 9) * value
    CONVERSIONS["V:PETAV"] = \
        lambda value: (10 ** -15) * value
    CONVERSIONS["V:PV"] = \
        lambda value: (10 ** 12) * value
    CONVERSIONS["V:TERAV"] = \
        lambda value: (10 ** -12) * value
    CONVERSIONS["V:YOTTAV"] = \
        lambda value: (10 ** -24) * value
    CONVERSIONS["V:YV"] = \
        lambda value: (10 ** 24) * value
    CONVERSIONS["V:ZETTAV"] = \
        lambda value: (10 ** -21) * value
    CONVERSIONS["V:ZV"] = \
        lambda value: (10 ** 21) * value
    CONVERSIONS["YOTTAV:AV"] = \
        lambda value: (10 ** 42) * value
    CONVERSIONS["YOTTAV:CV"] = \
        lambda value: (10 ** 26) * value
    CONVERSIONS["YOTTAV:DAV"] = \
        lambda value: (10 ** 23) * value
    CONVERSIONS["YOTTAV:DV"] = \
        lambda value: (10 ** 25) * value
    CONVERSIONS["YOTTAV:EXAV"] = \
        lambda value: (10 ** 6) * value
    CONVERSIONS["YOTTAV:FV"] = \
        lambda value: (10 ** 39) * value
    CONVERSIONS["YOTTAV:GIGAV"] = \
        lambda value: (10 ** 15) * value
    CONVERSIONS["YOTTAV:HV"] = \
        lambda value: (10 ** 22) * value
    CONVERSIONS["YOTTAV:KV"] = \
        lambda value: (10 ** 21) * value
    CONVERSIONS["YOTTAV:MEGAV"] = \
        lambda value: (10 ** 18) * value
    CONVERSIONS["YOTTAV:MICROV"] = \
        lambda value: (10 ** 30) * value
    CONVERSIONS["YOTTAV:MV"] = \
        lambda value: (10 ** 27) * value
    CONVERSIONS["YOTTAV:NV"] = \
        lambda value: (10 ** 33) * value
    CONVERSIONS["YOTTAV:PETAV"] = \
        lambda value: (10 ** 9) * value
    CONVERSIONS["YOTTAV:PV"] = \
        lambda value: (10 ** 36) * value
    CONVERSIONS["YOTTAV:TERAV"] = \
        lambda value: (10 ** 12) * value
    CONVERSIONS["YOTTAV:V"] = \
        lambda value: (10 ** 24) * value
    CONVERSIONS["YOTTAV:YV"] = \
        lambda value: (10 ** 48) * value
    CONVERSIONS["YOTTAV:ZETTAV"] = \
        lambda value: (10 ** 3) * value
    CONVERSIONS["YOTTAV:ZV"] = \
        lambda value: (10 ** 45) * value
    CONVERSIONS["YV:AV"] = \
        lambda value: (10 ** -6) * value
    CONVERSIONS["YV:CV"] = \
        lambda value: (10 ** -22) * value
    CONVERSIONS["YV:DAV"] = \
        lambda value: (10 ** -25) * value
    CONVERSIONS["YV:DV"] = \
        lambda value: (10 ** -23) * value
    CONVERSIONS["YV:EXAV"] = \
        lambda value: (10 ** -42) * value
    CONVERSIONS["YV:FV"] = \
        lambda value: (10 ** -9) * value
    CONVERSIONS["YV:GIGAV"] = \
        lambda value: (10 ** -33) * value
    CONVERSIONS["YV:HV"] = \
        lambda value: (10 ** -26) * value
    CONVERSIONS["YV:KV"] = \
        lambda value: (10 ** -27) * value
    CONVERSIONS["YV:MEGAV"] = \
        lambda value: (10 ** -30) * value
    CONVERSIONS["YV:MICROV"] = \
        lambda value: (10 ** -18) * value
    CONVERSIONS["YV:MV"] = \
        lambda value: (10 ** -21) * value
    CONVERSIONS["YV:NV"] = \
        lambda value: (10 ** -15) * value
    CONVERSIONS["YV:PETAV"] = \
        lambda value: (10 ** -39) * value
    CONVERSIONS["YV:PV"] = \
        lambda value: (10 ** -12) * value
    CONVERSIONS["YV:TERAV"] = \
        lambda value: (10 ** -36) * value
    CONVERSIONS["YV:V"] = \
        lambda value: (10 ** -24) * value
    CONVERSIONS["YV:YOTTAV"] = \
        lambda value: (10 ** -48) * value
    CONVERSIONS["YV:ZETTAV"] = \
        lambda value: (10 ** -45) * value
    CONVERSIONS["YV:ZV"] = \
        lambda value: (10 ** -3) * value
    CONVERSIONS["ZETTAV:AV"] = \
        lambda value: (10 ** 39) * value
    CONVERSIONS["ZETTAV:CV"] = \
        lambda value: (10 ** 23) * value
    CONVERSIONS["ZETTAV:DAV"] = \
        lambda value: (10 ** 20) * value
    CONVERSIONS["ZETTAV:DV"] = \
        lambda value: (10 ** 22) * value
    CONVERSIONS["ZETTAV:EXAV"] = \
        lambda value: (10 ** 3) * value
    CONVERSIONS["ZETTAV:FV"] = \
        lambda value: (10 ** 36) * value
    CONVERSIONS["ZETTAV:GIGAV"] = \
        lambda value: (10 ** 12) * value
    CONVERSIONS["ZETTAV:HV"] = \
        lambda value: (10 ** 19) * value
    CONVERSIONS["ZETTAV:KV"] = \
        lambda value: (10 ** 18) * value
    CONVERSIONS["ZETTAV:MEGAV"] = \
        lambda value: (10 ** 15) * value
    CONVERSIONS["ZETTAV:MICROV"] = \
        lambda value: (10 ** 27) * value
    CONVERSIONS["ZETTAV:MV"] = \
        lambda value: (10 ** 24) * value
    CONVERSIONS["ZETTAV:NV"] = \
        lambda value: (10 ** 30) * value
    CONVERSIONS["ZETTAV:PETAV"] = \
        lambda value: (10 ** 6) * value
    CONVERSIONS["ZETTAV:PV"] = \
        lambda value: (10 ** 33) * value
    CONVERSIONS["ZETTAV:TERAV"] = \
        lambda value: (10 ** 9) * value
    CONVERSIONS["ZETTAV:V"] = \
        lambda value: (10 ** 21) * value
    CONVERSIONS["ZETTAV:YOTTAV"] = \
        lambda value: (10 ** -3) * value
    CONVERSIONS["ZETTAV:YV"] = \
        lambda value: (10 ** 45) * value
    CONVERSIONS["ZETTAV:ZV"] = \
        lambda value: (10 ** 42) * value
    CONVERSIONS["ZV:AV"] = \
        lambda value: (10 ** -3) * value
    CONVERSIONS["ZV:CV"] = \
        lambda value: (10 ** -19) * value
    CONVERSIONS["ZV:DAV"] = \
        lambda value: (10 ** -22) * value
    CONVERSIONS["ZV:DV"] = \
        lambda value: (10 ** -20) * value
    CONVERSIONS["ZV:EXAV"] = \
        lambda value: (10 ** -39) * value
    CONVERSIONS["ZV:FV"] = \
        lambda value: (10 ** -6) * value
    CONVERSIONS["ZV:GIGAV"] = \
        lambda value: (10 ** -30) * value
    CONVERSIONS["ZV:HV"] = \
        lambda value: (10 ** -23) * value
    CONVERSIONS["ZV:KV"] = \
        lambda value: (10 ** -24) * value
    CONVERSIONS["ZV:MEGAV"] = \
        lambda value: (10 ** -27) * value
    CONVERSIONS["ZV:MICROV"] = \
        lambda value: (10 ** -15) * value
    CONVERSIONS["ZV:MV"] = \
        lambda value: (10 ** -18) * value
    CONVERSIONS["ZV:NV"] = \
        lambda value: (10 ** -12) * value
    CONVERSIONS["ZV:PETAV"] = \
        lambda value: (10 ** -36) * value
    CONVERSIONS["ZV:PV"] = \
        lambda value: (10 ** -9) * value
    CONVERSIONS["ZV:TERAV"] = \
        lambda value: (10 ** -33) * value
    CONVERSIONS["ZV:V"] = \
        lambda value: (10 ** -21) * value
    CONVERSIONS["ZV:YOTTAV"] = \
        lambda value: (10 ** -45) * value
    CONVERSIONS["ZV:YV"] = \
        lambda value: (10 ** 3) * value
    CONVERSIONS["ZV:ZETTAV"] = \
        lambda value: (10 ** -42) * value

    SYMBOLS = dict()
    SYMBOLS["AV"] = "aV"
    SYMBOLS["CV"] = "cV"
    SYMBOLS["DAV"] = "daV"
    SYMBOLS["DV"] = "dV"
    SYMBOLS["EXAV"] = "EV"
    SYMBOLS["FV"] = "fV"
    SYMBOLS["GIGAV"] = "GV"
    SYMBOLS["HV"] = "hV"
    SYMBOLS["KV"] = "kV"
    SYMBOLS["MEGAV"] = "MV"
    SYMBOLS["MICROV"] = "µV"
    SYMBOLS["MV"] = "mV"
    SYMBOLS["NV"] = "nV"
    SYMBOLS["PETAV"] = "PV"
    SYMBOLS["PV"] = "pV"
    SYMBOLS["TERAV"] = "TV"
    SYMBOLS["V"] = "V"
    SYMBOLS["YOTTAV"] = "YV"
    SYMBOLS["YV"] = "yV"
    SYMBOLS["ZETTAV"] = "ZV"
    SYMBOLS["ZV"] = "zV"

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
