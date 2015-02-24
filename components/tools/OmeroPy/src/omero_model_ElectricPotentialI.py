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

from omero.model.conversions import Add
from omero.model.conversions import Int
from omero.model.conversions import Mul
from omero.model.conversions import Pow
from omero.model.conversions import Rat
from omero.model.conversions import Sym


class ElectricPotentialI(_omero_model.ElectricPotential, UnitBase):

    CONVERSIONS = dict()
    CONVERSIONS["ATTOVOLT:CENTIVOLT"] = \
        Mul(Pow(10, 16), Sym("attov"))
    CONVERSIONS["ATTOVOLT:DECAVOLT"] = \
        Mul(Pow(10, 19), Sym("attov"))
    CONVERSIONS["ATTOVOLT:DECIVOLT"] = \
        Mul(Pow(10, 17), Sym("attov"))
    CONVERSIONS["ATTOVOLT:EXAVOLT"] = \
        Mul(Pow(10, 36), Sym("attov"))
    CONVERSIONS["ATTOVOLT:FEMTOVOLT"] = \
        Mul(Int(1000), Sym("attov"))
    CONVERSIONS["ATTOVOLT:GIGAVOLT"] = \
        Mul(Pow(10, 27), Sym("attov"))
    CONVERSIONS["ATTOVOLT:HECTOVOLT"] = \
        Mul(Pow(10, 20), Sym("attov"))
    CONVERSIONS["ATTOVOLT:KILOVOLT"] = \
        Mul(Pow(10, 21), Sym("attov"))
    CONVERSIONS["ATTOVOLT:MEGAVOLT"] = \
        Mul(Pow(10, 24), Sym("attov"))
    CONVERSIONS["ATTOVOLT:MICROVOLT"] = \
        Mul(Pow(10, 12), Sym("attov"))
    CONVERSIONS["ATTOVOLT:MILLIVOLT"] = \
        Mul(Pow(10, 15), Sym("attov"))
    CONVERSIONS["ATTOVOLT:NANOVOLT"] = \
        Mul(Pow(10, 9), Sym("attov"))
    CONVERSIONS["ATTOVOLT:PETAVOLT"] = \
        Mul(Pow(10, 33), Sym("attov"))
    CONVERSIONS["ATTOVOLT:PICOVOLT"] = \
        Mul(Pow(10, 6), Sym("attov"))
    CONVERSIONS["ATTOVOLT:TERAVOLT"] = \
        Mul(Pow(10, 30), Sym("attov"))
    CONVERSIONS["ATTOVOLT:VOLT"] = \
        Mul(Pow(10, 18), Sym("attov"))
    CONVERSIONS["ATTOVOLT:YOCTOVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("attov"))
    CONVERSIONS["ATTOVOLT:YOTTAVOLT"] = \
        Mul(Pow(10, 42), Sym("attov"))
    CONVERSIONS["ATTOVOLT:ZEPTOVOLT"] = \
        Mul(Rat(Int(1), Int(1000)), Sym("attov"))
    CONVERSIONS["ATTOVOLT:ZETTAVOLT"] = \
        Mul(Pow(10, 39), Sym("attov"))
    CONVERSIONS["CENTIVOLT:ATTOVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 16)), Sym("centiv"))
    CONVERSIONS["CENTIVOLT:DECAVOLT"] = \
        Mul(Int(1000), Sym("centiv"))
    CONVERSIONS["CENTIVOLT:DECIVOLT"] = \
        Mul(Int(10), Sym("centiv"))
    CONVERSIONS["CENTIVOLT:EXAVOLT"] = \
        Mul(Pow(10, 20), Sym("centiv"))
    CONVERSIONS["CENTIVOLT:FEMTOVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 13)), Sym("centiv"))
    CONVERSIONS["CENTIVOLT:GIGAVOLT"] = \
        Mul(Pow(10, 11), Sym("centiv"))
    CONVERSIONS["CENTIVOLT:HECTOVOLT"] = \
        Mul(Pow(10, 4), Sym("centiv"))
    CONVERSIONS["CENTIVOLT:KILOVOLT"] = \
        Mul(Pow(10, 5), Sym("centiv"))
    CONVERSIONS["CENTIVOLT:MEGAVOLT"] = \
        Mul(Pow(10, 8), Sym("centiv"))
    CONVERSIONS["CENTIVOLT:MICROVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 4)), Sym("centiv"))
    CONVERSIONS["CENTIVOLT:MILLIVOLT"] = \
        Mul(Rat(Int(1), Int(10)), Sym("centiv"))
    CONVERSIONS["CENTIVOLT:NANOVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 7)), Sym("centiv"))
    CONVERSIONS["CENTIVOLT:PETAVOLT"] = \
        Mul(Pow(10, 17), Sym("centiv"))
    CONVERSIONS["CENTIVOLT:PICOVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 10)), Sym("centiv"))
    CONVERSIONS["CENTIVOLT:TERAVOLT"] = \
        Mul(Pow(10, 14), Sym("centiv"))
    CONVERSIONS["CENTIVOLT:VOLT"] = \
        Mul(Int(100), Sym("centiv"))
    CONVERSIONS["CENTIVOLT:YOCTOVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 22)), Sym("centiv"))
    CONVERSIONS["CENTIVOLT:YOTTAVOLT"] = \
        Mul(Pow(10, 26), Sym("centiv"))
    CONVERSIONS["CENTIVOLT:ZEPTOVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 19)), Sym("centiv"))
    CONVERSIONS["CENTIVOLT:ZETTAVOLT"] = \
        Mul(Pow(10, 23), Sym("centiv"))
    CONVERSIONS["DECAVOLT:ATTOVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 19)), Sym("decav"))
    CONVERSIONS["DECAVOLT:CENTIVOLT"] = \
        Mul(Rat(Int(1), Int(1000)), Sym("decav"))
    CONVERSIONS["DECAVOLT:DECIVOLT"] = \
        Mul(Rat(Int(1), Int(100)), Sym("decav"))
    CONVERSIONS["DECAVOLT:EXAVOLT"] = \
        Mul(Pow(10, 17), Sym("decav"))
    CONVERSIONS["DECAVOLT:FEMTOVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 16)), Sym("decav"))
    CONVERSIONS["DECAVOLT:GIGAVOLT"] = \
        Mul(Pow(10, 8), Sym("decav"))
    CONVERSIONS["DECAVOLT:HECTOVOLT"] = \
        Mul(Int(10), Sym("decav"))
    CONVERSIONS["DECAVOLT:KILOVOLT"] = \
        Mul(Int(100), Sym("decav"))
    CONVERSIONS["DECAVOLT:MEGAVOLT"] = \
        Mul(Pow(10, 5), Sym("decav"))
    CONVERSIONS["DECAVOLT:MICROVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 7)), Sym("decav"))
    CONVERSIONS["DECAVOLT:MILLIVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 4)), Sym("decav"))
    CONVERSIONS["DECAVOLT:NANOVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 10)), Sym("decav"))
    CONVERSIONS["DECAVOLT:PETAVOLT"] = \
        Mul(Pow(10, 14), Sym("decav"))
    CONVERSIONS["DECAVOLT:PICOVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 13)), Sym("decav"))
    CONVERSIONS["DECAVOLT:TERAVOLT"] = \
        Mul(Pow(10, 11), Sym("decav"))
    CONVERSIONS["DECAVOLT:VOLT"] = \
        Mul(Rat(Int(1), Int(10)), Sym("decav"))
    CONVERSIONS["DECAVOLT:YOCTOVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 25)), Sym("decav"))
    CONVERSIONS["DECAVOLT:YOTTAVOLT"] = \
        Mul(Pow(10, 23), Sym("decav"))
    CONVERSIONS["DECAVOLT:ZEPTOVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 22)), Sym("decav"))
    CONVERSIONS["DECAVOLT:ZETTAVOLT"] = \
        Mul(Pow(10, 20), Sym("decav"))
    CONVERSIONS["DECIVOLT:ATTOVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 17)), Sym("deciv"))
    CONVERSIONS["DECIVOLT:CENTIVOLT"] = \
        Mul(Rat(Int(1), Int(10)), Sym("deciv"))
    CONVERSIONS["DECIVOLT:DECAVOLT"] = \
        Mul(Int(100), Sym("deciv"))
    CONVERSIONS["DECIVOLT:EXAVOLT"] = \
        Mul(Pow(10, 19), Sym("deciv"))
    CONVERSIONS["DECIVOLT:FEMTOVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 14)), Sym("deciv"))
    CONVERSIONS["DECIVOLT:GIGAVOLT"] = \
        Mul(Pow(10, 10), Sym("deciv"))
    CONVERSIONS["DECIVOLT:HECTOVOLT"] = \
        Mul(Int(1000), Sym("deciv"))
    CONVERSIONS["DECIVOLT:KILOVOLT"] = \
        Mul(Pow(10, 4), Sym("deciv"))
    CONVERSIONS["DECIVOLT:MEGAVOLT"] = \
        Mul(Pow(10, 7), Sym("deciv"))
    CONVERSIONS["DECIVOLT:MICROVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 5)), Sym("deciv"))
    CONVERSIONS["DECIVOLT:MILLIVOLT"] = \
        Mul(Rat(Int(1), Int(100)), Sym("deciv"))
    CONVERSIONS["DECIVOLT:NANOVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 8)), Sym("deciv"))
    CONVERSIONS["DECIVOLT:PETAVOLT"] = \
        Mul(Pow(10, 16), Sym("deciv"))
    CONVERSIONS["DECIVOLT:PICOVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 11)), Sym("deciv"))
    CONVERSIONS["DECIVOLT:TERAVOLT"] = \
        Mul(Pow(10, 13), Sym("deciv"))
    CONVERSIONS["DECIVOLT:VOLT"] = \
        Mul(Int(10), Sym("deciv"))
    CONVERSIONS["DECIVOLT:YOCTOVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 23)), Sym("deciv"))
    CONVERSIONS["DECIVOLT:YOTTAVOLT"] = \
        Mul(Pow(10, 25), Sym("deciv"))
    CONVERSIONS["DECIVOLT:ZEPTOVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 20)), Sym("deciv"))
    CONVERSIONS["DECIVOLT:ZETTAVOLT"] = \
        Mul(Pow(10, 22), Sym("deciv"))
    CONVERSIONS["EXAVOLT:ATTOVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 36)), Sym("exav"))
    CONVERSIONS["EXAVOLT:CENTIVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 20)), Sym("exav"))
    CONVERSIONS["EXAVOLT:DECAVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 17)), Sym("exav"))
    CONVERSIONS["EXAVOLT:DECIVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 19)), Sym("exav"))
    CONVERSIONS["EXAVOLT:FEMTOVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 33)), Sym("exav"))
    CONVERSIONS["EXAVOLT:GIGAVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("exav"))
    CONVERSIONS["EXAVOLT:HECTOVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 16)), Sym("exav"))
    CONVERSIONS["EXAVOLT:KILOVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 15)), Sym("exav"))
    CONVERSIONS["EXAVOLT:MEGAVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 12)), Sym("exav"))
    CONVERSIONS["EXAVOLT:MICROVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 24)), Sym("exav"))
    CONVERSIONS["EXAVOLT:MILLIVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 21)), Sym("exav"))
    CONVERSIONS["EXAVOLT:NANOVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 27)), Sym("exav"))
    CONVERSIONS["EXAVOLT:PETAVOLT"] = \
        Mul(Rat(Int(1), Int(1000)), Sym("exav"))
    CONVERSIONS["EXAVOLT:PICOVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 30)), Sym("exav"))
    CONVERSIONS["EXAVOLT:TERAVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("exav"))
    CONVERSIONS["EXAVOLT:VOLT"] = \
        Mul(Rat(Int(1), Pow(10, 18)), Sym("exav"))
    CONVERSIONS["EXAVOLT:YOCTOVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 42)), Sym("exav"))
    CONVERSIONS["EXAVOLT:YOTTAVOLT"] = \
        Mul(Pow(10, 6), Sym("exav"))
    CONVERSIONS["EXAVOLT:ZEPTOVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 39)), Sym("exav"))
    CONVERSIONS["EXAVOLT:ZETTAVOLT"] = \
        Mul(Int(1000), Sym("exav"))
    CONVERSIONS["FEMTOVOLT:ATTOVOLT"] = \
        Mul(Rat(Int(1), Int(1000)), Sym("femtov"))
    CONVERSIONS["FEMTOVOLT:CENTIVOLT"] = \
        Mul(Pow(10, 13), Sym("femtov"))
    CONVERSIONS["FEMTOVOLT:DECAVOLT"] = \
        Mul(Pow(10, 16), Sym("femtov"))
    CONVERSIONS["FEMTOVOLT:DECIVOLT"] = \
        Mul(Pow(10, 14), Sym("femtov"))
    CONVERSIONS["FEMTOVOLT:EXAVOLT"] = \
        Mul(Pow(10, 33), Sym("femtov"))
    CONVERSIONS["FEMTOVOLT:GIGAVOLT"] = \
        Mul(Pow(10, 24), Sym("femtov"))
    CONVERSIONS["FEMTOVOLT:HECTOVOLT"] = \
        Mul(Pow(10, 17), Sym("femtov"))
    CONVERSIONS["FEMTOVOLT:KILOVOLT"] = \
        Mul(Pow(10, 18), Sym("femtov"))
    CONVERSIONS["FEMTOVOLT:MEGAVOLT"] = \
        Mul(Pow(10, 21), Sym("femtov"))
    CONVERSIONS["FEMTOVOLT:MICROVOLT"] = \
        Mul(Pow(10, 9), Sym("femtov"))
    CONVERSIONS["FEMTOVOLT:MILLIVOLT"] = \
        Mul(Pow(10, 12), Sym("femtov"))
    CONVERSIONS["FEMTOVOLT:NANOVOLT"] = \
        Mul(Pow(10, 6), Sym("femtov"))
    CONVERSIONS["FEMTOVOLT:PETAVOLT"] = \
        Mul(Pow(10, 30), Sym("femtov"))
    CONVERSIONS["FEMTOVOLT:PICOVOLT"] = \
        Mul(Int(1000), Sym("femtov"))
    CONVERSIONS["FEMTOVOLT:TERAVOLT"] = \
        Mul(Pow(10, 27), Sym("femtov"))
    CONVERSIONS["FEMTOVOLT:VOLT"] = \
        Mul(Pow(10, 15), Sym("femtov"))
    CONVERSIONS["FEMTOVOLT:YOCTOVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("femtov"))
    CONVERSIONS["FEMTOVOLT:YOTTAVOLT"] = \
        Mul(Pow(10, 39), Sym("femtov"))
    CONVERSIONS["FEMTOVOLT:ZEPTOVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("femtov"))
    CONVERSIONS["FEMTOVOLT:ZETTAVOLT"] = \
        Mul(Pow(10, 36), Sym("femtov"))
    CONVERSIONS["GIGAVOLT:ATTOVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 27)), Sym("gigav"))
    CONVERSIONS["GIGAVOLT:CENTIVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 11)), Sym("gigav"))
    CONVERSIONS["GIGAVOLT:DECAVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 8)), Sym("gigav"))
    CONVERSIONS["GIGAVOLT:DECIVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 10)), Sym("gigav"))
    CONVERSIONS["GIGAVOLT:EXAVOLT"] = \
        Mul(Pow(10, 9), Sym("gigav"))
    CONVERSIONS["GIGAVOLT:FEMTOVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 24)), Sym("gigav"))
    CONVERSIONS["GIGAVOLT:HECTOVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 7)), Sym("gigav"))
    CONVERSIONS["GIGAVOLT:KILOVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("gigav"))
    CONVERSIONS["GIGAVOLT:MEGAVOLT"] = \
        Mul(Rat(Int(1), Int(1000)), Sym("gigav"))
    CONVERSIONS["GIGAVOLT:MICROVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 15)), Sym("gigav"))
    CONVERSIONS["GIGAVOLT:MILLIVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 12)), Sym("gigav"))
    CONVERSIONS["GIGAVOLT:NANOVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 18)), Sym("gigav"))
    CONVERSIONS["GIGAVOLT:PETAVOLT"] = \
        Mul(Pow(10, 6), Sym("gigav"))
    CONVERSIONS["GIGAVOLT:PICOVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 21)), Sym("gigav"))
    CONVERSIONS["GIGAVOLT:TERAVOLT"] = \
        Mul(Int(1000), Sym("gigav"))
    CONVERSIONS["GIGAVOLT:VOLT"] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("gigav"))
    CONVERSIONS["GIGAVOLT:YOCTOVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 33)), Sym("gigav"))
    CONVERSIONS["GIGAVOLT:YOTTAVOLT"] = \
        Mul(Pow(10, 15), Sym("gigav"))
    CONVERSIONS["GIGAVOLT:ZEPTOVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 30)), Sym("gigav"))
    CONVERSIONS["GIGAVOLT:ZETTAVOLT"] = \
        Mul(Pow(10, 12), Sym("gigav"))
    CONVERSIONS["HECTOVOLT:ATTOVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 20)), Sym("hectov"))
    CONVERSIONS["HECTOVOLT:CENTIVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 4)), Sym("hectov"))
    CONVERSIONS["HECTOVOLT:DECAVOLT"] = \
        Mul(Rat(Int(1), Int(10)), Sym("hectov"))
    CONVERSIONS["HECTOVOLT:DECIVOLT"] = \
        Mul(Rat(Int(1), Int(1000)), Sym("hectov"))
    CONVERSIONS["HECTOVOLT:EXAVOLT"] = \
        Mul(Pow(10, 16), Sym("hectov"))
    CONVERSIONS["HECTOVOLT:FEMTOVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 17)), Sym("hectov"))
    CONVERSIONS["HECTOVOLT:GIGAVOLT"] = \
        Mul(Pow(10, 7), Sym("hectov"))
    CONVERSIONS["HECTOVOLT:KILOVOLT"] = \
        Mul(Int(10), Sym("hectov"))
    CONVERSIONS["HECTOVOLT:MEGAVOLT"] = \
        Mul(Pow(10, 4), Sym("hectov"))
    CONVERSIONS["HECTOVOLT:MICROVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 8)), Sym("hectov"))
    CONVERSIONS["HECTOVOLT:MILLIVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 5)), Sym("hectov"))
    CONVERSIONS["HECTOVOLT:NANOVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 11)), Sym("hectov"))
    CONVERSIONS["HECTOVOLT:PETAVOLT"] = \
        Mul(Pow(10, 13), Sym("hectov"))
    CONVERSIONS["HECTOVOLT:PICOVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 14)), Sym("hectov"))
    CONVERSIONS["HECTOVOLT:TERAVOLT"] = \
        Mul(Pow(10, 10), Sym("hectov"))
    CONVERSIONS["HECTOVOLT:VOLT"] = \
        Mul(Rat(Int(1), Int(100)), Sym("hectov"))
    CONVERSIONS["HECTOVOLT:YOCTOVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 26)), Sym("hectov"))
    CONVERSIONS["HECTOVOLT:YOTTAVOLT"] = \
        Mul(Pow(10, 22), Sym("hectov"))
    CONVERSIONS["HECTOVOLT:ZEPTOVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 23)), Sym("hectov"))
    CONVERSIONS["HECTOVOLT:ZETTAVOLT"] = \
        Mul(Pow(10, 19), Sym("hectov"))
    CONVERSIONS["KILOVOLT:ATTOVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 21)), Sym("kilov"))
    CONVERSIONS["KILOVOLT:CENTIVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 5)), Sym("kilov"))
    CONVERSIONS["KILOVOLT:DECAVOLT"] = \
        Mul(Rat(Int(1), Int(100)), Sym("kilov"))
    CONVERSIONS["KILOVOLT:DECIVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 4)), Sym("kilov"))
    CONVERSIONS["KILOVOLT:EXAVOLT"] = \
        Mul(Pow(10, 15), Sym("kilov"))
    CONVERSIONS["KILOVOLT:FEMTOVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 18)), Sym("kilov"))
    CONVERSIONS["KILOVOLT:GIGAVOLT"] = \
        Mul(Pow(10, 6), Sym("kilov"))
    CONVERSIONS["KILOVOLT:HECTOVOLT"] = \
        Mul(Rat(Int(1), Int(10)), Sym("kilov"))
    CONVERSIONS["KILOVOLT:MEGAVOLT"] = \
        Mul(Int(1000), Sym("kilov"))
    CONVERSIONS["KILOVOLT:MICROVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("kilov"))
    CONVERSIONS["KILOVOLT:MILLIVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("kilov"))
    CONVERSIONS["KILOVOLT:NANOVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 12)), Sym("kilov"))
    CONVERSIONS["KILOVOLT:PETAVOLT"] = \
        Mul(Pow(10, 12), Sym("kilov"))
    CONVERSIONS["KILOVOLT:PICOVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 15)), Sym("kilov"))
    CONVERSIONS["KILOVOLT:TERAVOLT"] = \
        Mul(Pow(10, 9), Sym("kilov"))
    CONVERSIONS["KILOVOLT:VOLT"] = \
        Mul(Rat(Int(1), Int(1000)), Sym("kilov"))
    CONVERSIONS["KILOVOLT:YOCTOVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 27)), Sym("kilov"))
    CONVERSIONS["KILOVOLT:YOTTAVOLT"] = \
        Mul(Pow(10, 21), Sym("kilov"))
    CONVERSIONS["KILOVOLT:ZEPTOVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 24)), Sym("kilov"))
    CONVERSIONS["KILOVOLT:ZETTAVOLT"] = \
        Mul(Pow(10, 18), Sym("kilov"))
    CONVERSIONS["MEGAVOLT:ATTOVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 24)), Sym("megav"))
    CONVERSIONS["MEGAVOLT:CENTIVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 8)), Sym("megav"))
    CONVERSIONS["MEGAVOLT:DECAVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 5)), Sym("megav"))
    CONVERSIONS["MEGAVOLT:DECIVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 7)), Sym("megav"))
    CONVERSIONS["MEGAVOLT:EXAVOLT"] = \
        Mul(Pow(10, 12), Sym("megav"))
    CONVERSIONS["MEGAVOLT:FEMTOVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 21)), Sym("megav"))
    CONVERSIONS["MEGAVOLT:GIGAVOLT"] = \
        Mul(Int(1000), Sym("megav"))
    CONVERSIONS["MEGAVOLT:HECTOVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 4)), Sym("megav"))
    CONVERSIONS["MEGAVOLT:KILOVOLT"] = \
        Mul(Rat(Int(1), Int(1000)), Sym("megav"))
    CONVERSIONS["MEGAVOLT:MICROVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 12)), Sym("megav"))
    CONVERSIONS["MEGAVOLT:MILLIVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("megav"))
    CONVERSIONS["MEGAVOLT:NANOVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 15)), Sym("megav"))
    CONVERSIONS["MEGAVOLT:PETAVOLT"] = \
        Mul(Pow(10, 9), Sym("megav"))
    CONVERSIONS["MEGAVOLT:PICOVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 18)), Sym("megav"))
    CONVERSIONS["MEGAVOLT:TERAVOLT"] = \
        Mul(Pow(10, 6), Sym("megav"))
    CONVERSIONS["MEGAVOLT:VOLT"] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("megav"))
    CONVERSIONS["MEGAVOLT:YOCTOVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 30)), Sym("megav"))
    CONVERSIONS["MEGAVOLT:YOTTAVOLT"] = \
        Mul(Pow(10, 18), Sym("megav"))
    CONVERSIONS["MEGAVOLT:ZEPTOVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 27)), Sym("megav"))
    CONVERSIONS["MEGAVOLT:ZETTAVOLT"] = \
        Mul(Pow(10, 15), Sym("megav"))
    CONVERSIONS["MICROVOLT:ATTOVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 12)), Sym("microv"))
    CONVERSIONS["MICROVOLT:CENTIVOLT"] = \
        Mul(Pow(10, 4), Sym("microv"))
    CONVERSIONS["MICROVOLT:DECAVOLT"] = \
        Mul(Pow(10, 7), Sym("microv"))
    CONVERSIONS["MICROVOLT:DECIVOLT"] = \
        Mul(Pow(10, 5), Sym("microv"))
    CONVERSIONS["MICROVOLT:EXAVOLT"] = \
        Mul(Pow(10, 24), Sym("microv"))
    CONVERSIONS["MICROVOLT:FEMTOVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("microv"))
    CONVERSIONS["MICROVOLT:GIGAVOLT"] = \
        Mul(Pow(10, 15), Sym("microv"))
    CONVERSIONS["MICROVOLT:HECTOVOLT"] = \
        Mul(Pow(10, 8), Sym("microv"))
    CONVERSIONS["MICROVOLT:KILOVOLT"] = \
        Mul(Pow(10, 9), Sym("microv"))
    CONVERSIONS["MICROVOLT:MEGAVOLT"] = \
        Mul(Pow(10, 12), Sym("microv"))
    CONVERSIONS["MICROVOLT:MILLIVOLT"] = \
        Mul(Int(1000), Sym("microv"))
    CONVERSIONS["MICROVOLT:NANOVOLT"] = \
        Mul(Rat(Int(1), Int(1000)), Sym("microv"))
    CONVERSIONS["MICROVOLT:PETAVOLT"] = \
        Mul(Pow(10, 21), Sym("microv"))
    CONVERSIONS["MICROVOLT:PICOVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("microv"))
    CONVERSIONS["MICROVOLT:TERAVOLT"] = \
        Mul(Pow(10, 18), Sym("microv"))
    CONVERSIONS["MICROVOLT:VOLT"] = \
        Mul(Pow(10, 6), Sym("microv"))
    CONVERSIONS["MICROVOLT:YOCTOVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 18)), Sym("microv"))
    CONVERSIONS["MICROVOLT:YOTTAVOLT"] = \
        Mul(Pow(10, 30), Sym("microv"))
    CONVERSIONS["MICROVOLT:ZEPTOVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 15)), Sym("microv"))
    CONVERSIONS["MICROVOLT:ZETTAVOLT"] = \
        Mul(Pow(10, 27), Sym("microv"))
    CONVERSIONS["MILLIVOLT:ATTOVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 15)), Sym("milliv"))
    CONVERSIONS["MILLIVOLT:CENTIVOLT"] = \
        Mul(Int(10), Sym("milliv"))
    CONVERSIONS["MILLIVOLT:DECAVOLT"] = \
        Mul(Pow(10, 4), Sym("milliv"))
    CONVERSIONS["MILLIVOLT:DECIVOLT"] = \
        Mul(Int(100), Sym("milliv"))
    CONVERSIONS["MILLIVOLT:EXAVOLT"] = \
        Mul(Pow(10, 21), Sym("milliv"))
    CONVERSIONS["MILLIVOLT:FEMTOVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 12)), Sym("milliv"))
    CONVERSIONS["MILLIVOLT:GIGAVOLT"] = \
        Mul(Pow(10, 12), Sym("milliv"))
    CONVERSIONS["MILLIVOLT:HECTOVOLT"] = \
        Mul(Pow(10, 5), Sym("milliv"))
    CONVERSIONS["MILLIVOLT:KILOVOLT"] = \
        Mul(Pow(10, 6), Sym("milliv"))
    CONVERSIONS["MILLIVOLT:MEGAVOLT"] = \
        Mul(Pow(10, 9), Sym("milliv"))
    CONVERSIONS["MILLIVOLT:MICROVOLT"] = \
        Mul(Rat(Int(1), Int(1000)), Sym("milliv"))
    CONVERSIONS["MILLIVOLT:NANOVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("milliv"))
    CONVERSIONS["MILLIVOLT:PETAVOLT"] = \
        Mul(Pow(10, 18), Sym("milliv"))
    CONVERSIONS["MILLIVOLT:PICOVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("milliv"))
    CONVERSIONS["MILLIVOLT:TERAVOLT"] = \
        Mul(Pow(10, 15), Sym("milliv"))
    CONVERSIONS["MILLIVOLT:VOLT"] = \
        Mul(Int(1000), Sym("milliv"))
    CONVERSIONS["MILLIVOLT:YOCTOVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 21)), Sym("milliv"))
    CONVERSIONS["MILLIVOLT:YOTTAVOLT"] = \
        Mul(Pow(10, 27), Sym("milliv"))
    CONVERSIONS["MILLIVOLT:ZEPTOVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 18)), Sym("milliv"))
    CONVERSIONS["MILLIVOLT:ZETTAVOLT"] = \
        Mul(Pow(10, 24), Sym("milliv"))
    CONVERSIONS["NANOVOLT:ATTOVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("nanov"))
    CONVERSIONS["NANOVOLT:CENTIVOLT"] = \
        Mul(Pow(10, 7), Sym("nanov"))
    CONVERSIONS["NANOVOLT:DECAVOLT"] = \
        Mul(Pow(10, 10), Sym("nanov"))
    CONVERSIONS["NANOVOLT:DECIVOLT"] = \
        Mul(Pow(10, 8), Sym("nanov"))
    CONVERSIONS["NANOVOLT:EXAVOLT"] = \
        Mul(Pow(10, 27), Sym("nanov"))
    CONVERSIONS["NANOVOLT:FEMTOVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("nanov"))
    CONVERSIONS["NANOVOLT:GIGAVOLT"] = \
        Mul(Pow(10, 18), Sym("nanov"))
    CONVERSIONS["NANOVOLT:HECTOVOLT"] = \
        Mul(Pow(10, 11), Sym("nanov"))
    CONVERSIONS["NANOVOLT:KILOVOLT"] = \
        Mul(Pow(10, 12), Sym("nanov"))
    CONVERSIONS["NANOVOLT:MEGAVOLT"] = \
        Mul(Pow(10, 15), Sym("nanov"))
    CONVERSIONS["NANOVOLT:MICROVOLT"] = \
        Mul(Int(1000), Sym("nanov"))
    CONVERSIONS["NANOVOLT:MILLIVOLT"] = \
        Mul(Pow(10, 6), Sym("nanov"))
    CONVERSIONS["NANOVOLT:PETAVOLT"] = \
        Mul(Pow(10, 24), Sym("nanov"))
    CONVERSIONS["NANOVOLT:PICOVOLT"] = \
        Mul(Rat(Int(1), Int(1000)), Sym("nanov"))
    CONVERSIONS["NANOVOLT:TERAVOLT"] = \
        Mul(Pow(10, 21), Sym("nanov"))
    CONVERSIONS["NANOVOLT:VOLT"] = \
        Mul(Pow(10, 9), Sym("nanov"))
    CONVERSIONS["NANOVOLT:YOCTOVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 15)), Sym("nanov"))
    CONVERSIONS["NANOVOLT:YOTTAVOLT"] = \
        Mul(Pow(10, 33), Sym("nanov"))
    CONVERSIONS["NANOVOLT:ZEPTOVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 12)), Sym("nanov"))
    CONVERSIONS["NANOVOLT:ZETTAVOLT"] = \
        Mul(Pow(10, 30), Sym("nanov"))
    CONVERSIONS["PETAVOLT:ATTOVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 33)), Sym("petav"))
    CONVERSIONS["PETAVOLT:CENTIVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 17)), Sym("petav"))
    CONVERSIONS["PETAVOLT:DECAVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 14)), Sym("petav"))
    CONVERSIONS["PETAVOLT:DECIVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 16)), Sym("petav"))
    CONVERSIONS["PETAVOLT:EXAVOLT"] = \
        Mul(Int(1000), Sym("petav"))
    CONVERSIONS["PETAVOLT:FEMTOVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 30)), Sym("petav"))
    CONVERSIONS["PETAVOLT:GIGAVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("petav"))
    CONVERSIONS["PETAVOLT:HECTOVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 13)), Sym("petav"))
    CONVERSIONS["PETAVOLT:KILOVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 12)), Sym("petav"))
    CONVERSIONS["PETAVOLT:MEGAVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("petav"))
    CONVERSIONS["PETAVOLT:MICROVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 21)), Sym("petav"))
    CONVERSIONS["PETAVOLT:MILLIVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 18)), Sym("petav"))
    CONVERSIONS["PETAVOLT:NANOVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 24)), Sym("petav"))
    CONVERSIONS["PETAVOLT:PICOVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 27)), Sym("petav"))
    CONVERSIONS["PETAVOLT:TERAVOLT"] = \
        Mul(Rat(Int(1), Int(1000)), Sym("petav"))
    CONVERSIONS["PETAVOLT:VOLT"] = \
        Mul(Rat(Int(1), Pow(10, 15)), Sym("petav"))
    CONVERSIONS["PETAVOLT:YOCTOVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 39)), Sym("petav"))
    CONVERSIONS["PETAVOLT:YOTTAVOLT"] = \
        Mul(Pow(10, 9), Sym("petav"))
    CONVERSIONS["PETAVOLT:ZEPTOVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 36)), Sym("petav"))
    CONVERSIONS["PETAVOLT:ZETTAVOLT"] = \
        Mul(Pow(10, 6), Sym("petav"))
    CONVERSIONS["PICOVOLT:ATTOVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("picov"))
    CONVERSIONS["PICOVOLT:CENTIVOLT"] = \
        Mul(Pow(10, 10), Sym("picov"))
    CONVERSIONS["PICOVOLT:DECAVOLT"] = \
        Mul(Pow(10, 13), Sym("picov"))
    CONVERSIONS["PICOVOLT:DECIVOLT"] = \
        Mul(Pow(10, 11), Sym("picov"))
    CONVERSIONS["PICOVOLT:EXAVOLT"] = \
        Mul(Pow(10, 30), Sym("picov"))
    CONVERSIONS["PICOVOLT:FEMTOVOLT"] = \
        Mul(Rat(Int(1), Int(1000)), Sym("picov"))
    CONVERSIONS["PICOVOLT:GIGAVOLT"] = \
        Mul(Pow(10, 21), Sym("picov"))
    CONVERSIONS["PICOVOLT:HECTOVOLT"] = \
        Mul(Pow(10, 14), Sym("picov"))
    CONVERSIONS["PICOVOLT:KILOVOLT"] = \
        Mul(Pow(10, 15), Sym("picov"))
    CONVERSIONS["PICOVOLT:MEGAVOLT"] = \
        Mul(Pow(10, 18), Sym("picov"))
    CONVERSIONS["PICOVOLT:MICROVOLT"] = \
        Mul(Pow(10, 6), Sym("picov"))
    CONVERSIONS["PICOVOLT:MILLIVOLT"] = \
        Mul(Pow(10, 9), Sym("picov"))
    CONVERSIONS["PICOVOLT:NANOVOLT"] = \
        Mul(Int(1000), Sym("picov"))
    CONVERSIONS["PICOVOLT:PETAVOLT"] = \
        Mul(Pow(10, 27), Sym("picov"))
    CONVERSIONS["PICOVOLT:TERAVOLT"] = \
        Mul(Pow(10, 24), Sym("picov"))
    CONVERSIONS["PICOVOLT:VOLT"] = \
        Mul(Pow(10, 12), Sym("picov"))
    CONVERSIONS["PICOVOLT:YOCTOVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 12)), Sym("picov"))
    CONVERSIONS["PICOVOLT:YOTTAVOLT"] = \
        Mul(Pow(10, 36), Sym("picov"))
    CONVERSIONS["PICOVOLT:ZEPTOVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("picov"))
    CONVERSIONS["PICOVOLT:ZETTAVOLT"] = \
        Mul(Pow(10, 33), Sym("picov"))
    CONVERSIONS["TERAVOLT:ATTOVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 30)), Sym("terav"))
    CONVERSIONS["TERAVOLT:CENTIVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 14)), Sym("terav"))
    CONVERSIONS["TERAVOLT:DECAVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 11)), Sym("terav"))
    CONVERSIONS["TERAVOLT:DECIVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 13)), Sym("terav"))
    CONVERSIONS["TERAVOLT:EXAVOLT"] = \
        Mul(Pow(10, 6), Sym("terav"))
    CONVERSIONS["TERAVOLT:FEMTOVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 27)), Sym("terav"))
    CONVERSIONS["TERAVOLT:GIGAVOLT"] = \
        Mul(Rat(Int(1), Int(1000)), Sym("terav"))
    CONVERSIONS["TERAVOLT:HECTOVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 10)), Sym("terav"))
    CONVERSIONS["TERAVOLT:KILOVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("terav"))
    CONVERSIONS["TERAVOLT:MEGAVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("terav"))
    CONVERSIONS["TERAVOLT:MICROVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 18)), Sym("terav"))
    CONVERSIONS["TERAVOLT:MILLIVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 15)), Sym("terav"))
    CONVERSIONS["TERAVOLT:NANOVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 21)), Sym("terav"))
    CONVERSIONS["TERAVOLT:PETAVOLT"] = \
        Mul(Int(1000), Sym("terav"))
    CONVERSIONS["TERAVOLT:PICOVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 24)), Sym("terav"))
    CONVERSIONS["TERAVOLT:VOLT"] = \
        Mul(Rat(Int(1), Pow(10, 12)), Sym("terav"))
    CONVERSIONS["TERAVOLT:YOCTOVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 36)), Sym("terav"))
    CONVERSIONS["TERAVOLT:YOTTAVOLT"] = \
        Mul(Pow(10, 12), Sym("terav"))
    CONVERSIONS["TERAVOLT:ZEPTOVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 33)), Sym("terav"))
    CONVERSIONS["TERAVOLT:ZETTAVOLT"] = \
        Mul(Pow(10, 9), Sym("terav"))
    CONVERSIONS["VOLT:ATTOVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 18)), Sym("v"))
    CONVERSIONS["VOLT:CENTIVOLT"] = \
        Mul(Rat(Int(1), Int(100)), Sym("v"))
    CONVERSIONS["VOLT:DECAVOLT"] = \
        Mul(Int(10), Sym("v"))
    CONVERSIONS["VOLT:DECIVOLT"] = \
        Mul(Rat(Int(1), Int(10)), Sym("v"))
    CONVERSIONS["VOLT:EXAVOLT"] = \
        Mul(Pow(10, 18), Sym("v"))
    CONVERSIONS["VOLT:FEMTOVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 15)), Sym("v"))
    CONVERSIONS["VOLT:GIGAVOLT"] = \
        Mul(Pow(10, 9), Sym("v"))
    CONVERSIONS["VOLT:HECTOVOLT"] = \
        Mul(Int(100), Sym("v"))
    CONVERSIONS["VOLT:KILOVOLT"] = \
        Mul(Int(1000), Sym("v"))
    CONVERSIONS["VOLT:MEGAVOLT"] = \
        Mul(Pow(10, 6), Sym("v"))
    CONVERSIONS["VOLT:MICROVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("v"))
    CONVERSIONS["VOLT:MILLIVOLT"] = \
        Mul(Rat(Int(1), Int(1000)), Sym("v"))
    CONVERSIONS["VOLT:NANOVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("v"))
    CONVERSIONS["VOLT:PETAVOLT"] = \
        Mul(Pow(10, 15), Sym("v"))
    CONVERSIONS["VOLT:PICOVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 12)), Sym("v"))
    CONVERSIONS["VOLT:TERAVOLT"] = \
        Mul(Pow(10, 12), Sym("v"))
    CONVERSIONS["VOLT:YOCTOVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 24)), Sym("v"))
    CONVERSIONS["VOLT:YOTTAVOLT"] = \
        Mul(Pow(10, 24), Sym("v"))
    CONVERSIONS["VOLT:ZEPTOVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 21)), Sym("v"))
    CONVERSIONS["VOLT:ZETTAVOLT"] = \
        Mul(Pow(10, 21), Sym("v"))
    CONVERSIONS["YOCTOVOLT:ATTOVOLT"] = \
        Mul(Pow(10, 6), Sym("yoctov"))
    CONVERSIONS["YOCTOVOLT:CENTIVOLT"] = \
        Mul(Pow(10, 22), Sym("yoctov"))
    CONVERSIONS["YOCTOVOLT:DECAVOLT"] = \
        Mul(Pow(10, 25), Sym("yoctov"))
    CONVERSIONS["YOCTOVOLT:DECIVOLT"] = \
        Mul(Pow(10, 23), Sym("yoctov"))
    CONVERSIONS["YOCTOVOLT:EXAVOLT"] = \
        Mul(Pow(10, 42), Sym("yoctov"))
    CONVERSIONS["YOCTOVOLT:FEMTOVOLT"] = \
        Mul(Pow(10, 9), Sym("yoctov"))
    CONVERSIONS["YOCTOVOLT:GIGAVOLT"] = \
        Mul(Pow(10, 33), Sym("yoctov"))
    CONVERSIONS["YOCTOVOLT:HECTOVOLT"] = \
        Mul(Pow(10, 26), Sym("yoctov"))
    CONVERSIONS["YOCTOVOLT:KILOVOLT"] = \
        Mul(Pow(10, 27), Sym("yoctov"))
    CONVERSIONS["YOCTOVOLT:MEGAVOLT"] = \
        Mul(Pow(10, 30), Sym("yoctov"))
    CONVERSIONS["YOCTOVOLT:MICROVOLT"] = \
        Mul(Pow(10, 18), Sym("yoctov"))
    CONVERSIONS["YOCTOVOLT:MILLIVOLT"] = \
        Mul(Pow(10, 21), Sym("yoctov"))
    CONVERSIONS["YOCTOVOLT:NANOVOLT"] = \
        Mul(Pow(10, 15), Sym("yoctov"))
    CONVERSIONS["YOCTOVOLT:PETAVOLT"] = \
        Mul(Pow(10, 39), Sym("yoctov"))
    CONVERSIONS["YOCTOVOLT:PICOVOLT"] = \
        Mul(Pow(10, 12), Sym("yoctov"))
    CONVERSIONS["YOCTOVOLT:TERAVOLT"] = \
        Mul(Pow(10, 36), Sym("yoctov"))
    CONVERSIONS["YOCTOVOLT:VOLT"] = \
        Mul(Pow(10, 24), Sym("yoctov"))
    CONVERSIONS["YOCTOVOLT:YOTTAVOLT"] = \
        Mul(Pow(10, 48), Sym("yoctov"))
    CONVERSIONS["YOCTOVOLT:ZEPTOVOLT"] = \
        Mul(Int(1000), Sym("yoctov"))
    CONVERSIONS["YOCTOVOLT:ZETTAVOLT"] = \
        Mul(Pow(10, 45), Sym("yoctov"))
    CONVERSIONS["YOTTAVOLT:ATTOVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 42)), Sym("yottav"))
    CONVERSIONS["YOTTAVOLT:CENTIVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 26)), Sym("yottav"))
    CONVERSIONS["YOTTAVOLT:DECAVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 23)), Sym("yottav"))
    CONVERSIONS["YOTTAVOLT:DECIVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 25)), Sym("yottav"))
    CONVERSIONS["YOTTAVOLT:EXAVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("yottav"))
    CONVERSIONS["YOTTAVOLT:FEMTOVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 39)), Sym("yottav"))
    CONVERSIONS["YOTTAVOLT:GIGAVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 15)), Sym("yottav"))
    CONVERSIONS["YOTTAVOLT:HECTOVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 22)), Sym("yottav"))
    CONVERSIONS["YOTTAVOLT:KILOVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 21)), Sym("yottav"))
    CONVERSIONS["YOTTAVOLT:MEGAVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 18)), Sym("yottav"))
    CONVERSIONS["YOTTAVOLT:MICROVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 30)), Sym("yottav"))
    CONVERSIONS["YOTTAVOLT:MILLIVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 27)), Sym("yottav"))
    CONVERSIONS["YOTTAVOLT:NANOVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 33)), Sym("yottav"))
    CONVERSIONS["YOTTAVOLT:PETAVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("yottav"))
    CONVERSIONS["YOTTAVOLT:PICOVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 36)), Sym("yottav"))
    CONVERSIONS["YOTTAVOLT:TERAVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 12)), Sym("yottav"))
    CONVERSIONS["YOTTAVOLT:VOLT"] = \
        Mul(Rat(Int(1), Pow(10, 24)), Sym("yottav"))
    CONVERSIONS["YOTTAVOLT:YOCTOVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 48)), Sym("yottav"))
    CONVERSIONS["YOTTAVOLT:ZEPTOVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 45)), Sym("yottav"))
    CONVERSIONS["YOTTAVOLT:ZETTAVOLT"] = \
        Mul(Rat(Int(1), Int(1000)), Sym("yottav"))
    CONVERSIONS["ZEPTOVOLT:ATTOVOLT"] = \
        Mul(Int(1000), Sym("zeptov"))
    CONVERSIONS["ZEPTOVOLT:CENTIVOLT"] = \
        Mul(Pow(10, 19), Sym("zeptov"))
    CONVERSIONS["ZEPTOVOLT:DECAVOLT"] = \
        Mul(Pow(10, 22), Sym("zeptov"))
    CONVERSIONS["ZEPTOVOLT:DECIVOLT"] = \
        Mul(Pow(10, 20), Sym("zeptov"))
    CONVERSIONS["ZEPTOVOLT:EXAVOLT"] = \
        Mul(Pow(10, 39), Sym("zeptov"))
    CONVERSIONS["ZEPTOVOLT:FEMTOVOLT"] = \
        Mul(Pow(10, 6), Sym("zeptov"))
    CONVERSIONS["ZEPTOVOLT:GIGAVOLT"] = \
        Mul(Pow(10, 30), Sym("zeptov"))
    CONVERSIONS["ZEPTOVOLT:HECTOVOLT"] = \
        Mul(Pow(10, 23), Sym("zeptov"))
    CONVERSIONS["ZEPTOVOLT:KILOVOLT"] = \
        Mul(Pow(10, 24), Sym("zeptov"))
    CONVERSIONS["ZEPTOVOLT:MEGAVOLT"] = \
        Mul(Pow(10, 27), Sym("zeptov"))
    CONVERSIONS["ZEPTOVOLT:MICROVOLT"] = \
        Mul(Pow(10, 15), Sym("zeptov"))
    CONVERSIONS["ZEPTOVOLT:MILLIVOLT"] = \
        Mul(Pow(10, 18), Sym("zeptov"))
    CONVERSIONS["ZEPTOVOLT:NANOVOLT"] = \
        Mul(Pow(10, 12), Sym("zeptov"))
    CONVERSIONS["ZEPTOVOLT:PETAVOLT"] = \
        Mul(Pow(10, 36), Sym("zeptov"))
    CONVERSIONS["ZEPTOVOLT:PICOVOLT"] = \
        Mul(Pow(10, 9), Sym("zeptov"))
    CONVERSIONS["ZEPTOVOLT:TERAVOLT"] = \
        Mul(Pow(10, 33), Sym("zeptov"))
    CONVERSIONS["ZEPTOVOLT:VOLT"] = \
        Mul(Pow(10, 21), Sym("zeptov"))
    CONVERSIONS["ZEPTOVOLT:YOCTOVOLT"] = \
        Mul(Rat(Int(1), Int(1000)), Sym("zeptov"))
    CONVERSIONS["ZEPTOVOLT:YOTTAVOLT"] = \
        Mul(Pow(10, 45), Sym("zeptov"))
    CONVERSIONS["ZEPTOVOLT:ZETTAVOLT"] = \
        Mul(Pow(10, 42), Sym("zeptov"))
    CONVERSIONS["ZETTAVOLT:ATTOVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 39)), Sym("zettav"))
    CONVERSIONS["ZETTAVOLT:CENTIVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 23)), Sym("zettav"))
    CONVERSIONS["ZETTAVOLT:DECAVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 20)), Sym("zettav"))
    CONVERSIONS["ZETTAVOLT:DECIVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 22)), Sym("zettav"))
    CONVERSIONS["ZETTAVOLT:EXAVOLT"] = \
        Mul(Rat(Int(1), Int(1000)), Sym("zettav"))
    CONVERSIONS["ZETTAVOLT:FEMTOVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 36)), Sym("zettav"))
    CONVERSIONS["ZETTAVOLT:GIGAVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 12)), Sym("zettav"))
    CONVERSIONS["ZETTAVOLT:HECTOVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 19)), Sym("zettav"))
    CONVERSIONS["ZETTAVOLT:KILOVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 18)), Sym("zettav"))
    CONVERSIONS["ZETTAVOLT:MEGAVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 15)), Sym("zettav"))
    CONVERSIONS["ZETTAVOLT:MICROVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 27)), Sym("zettav"))
    CONVERSIONS["ZETTAVOLT:MILLIVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 24)), Sym("zettav"))
    CONVERSIONS["ZETTAVOLT:NANOVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 30)), Sym("zettav"))
    CONVERSIONS["ZETTAVOLT:PETAVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("zettav"))
    CONVERSIONS["ZETTAVOLT:PICOVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 33)), Sym("zettav"))
    CONVERSIONS["ZETTAVOLT:TERAVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("zettav"))
    CONVERSIONS["ZETTAVOLT:VOLT"] = \
        Mul(Rat(Int(1), Pow(10, 21)), Sym("zettav"))
    CONVERSIONS["ZETTAVOLT:YOCTOVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 45)), Sym("zettav"))
    CONVERSIONS["ZETTAVOLT:YOTTAVOLT"] = \
        Mul(Int(1000), Sym("zettav"))
    CONVERSIONS["ZETTAVOLT:ZEPTOVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 42)), Sym("zettav"))

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
