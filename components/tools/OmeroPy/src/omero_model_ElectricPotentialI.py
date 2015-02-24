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

from omero.model.conversions import Add  # nopep8
from omero.model.conversions import Int  # nopep8
from omero.model.conversions import Mul  # nopep8
from omero.model.conversions import Pow  # nopep8
from omero.model.conversions import Rat  # nopep8
from omero.model.conversions import Sym  # nopep8


class ElectricPotentialI(_omero_model.ElectricPotential, UnitBase):

    CONVERSIONS = dict()
    CONVERSIONS["ATTOVOLT:CENTIVOLT"] = \
        Mul(Pow(10, 16), Sym("attov"))  # nopep8
    CONVERSIONS["ATTOVOLT:DECAVOLT"] = \
        Mul(Pow(10, 19), Sym("attov"))  # nopep8
    CONVERSIONS["ATTOVOLT:DECIVOLT"] = \
        Mul(Pow(10, 17), Sym("attov"))  # nopep8
    CONVERSIONS["ATTOVOLT:EXAVOLT"] = \
        Mul(Pow(10, 36), Sym("attov"))  # nopep8
    CONVERSIONS["ATTOVOLT:FEMTOVOLT"] = \
        Mul(Int(1000), Sym("attov"))  # nopep8
    CONVERSIONS["ATTOVOLT:GIGAVOLT"] = \
        Mul(Pow(10, 27), Sym("attov"))  # nopep8
    CONVERSIONS["ATTOVOLT:HECTOVOLT"] = \
        Mul(Pow(10, 20), Sym("attov"))  # nopep8
    CONVERSIONS["ATTOVOLT:KILOVOLT"] = \
        Mul(Pow(10, 21), Sym("attov"))  # nopep8
    CONVERSIONS["ATTOVOLT:MEGAVOLT"] = \
        Mul(Pow(10, 24), Sym("attov"))  # nopep8
    CONVERSIONS["ATTOVOLT:MICROVOLT"] = \
        Mul(Pow(10, 12), Sym("attov"))  # nopep8
    CONVERSIONS["ATTOVOLT:MILLIVOLT"] = \
        Mul(Pow(10, 15), Sym("attov"))  # nopep8
    CONVERSIONS["ATTOVOLT:NANOVOLT"] = \
        Mul(Pow(10, 9), Sym("attov"))  # nopep8
    CONVERSIONS["ATTOVOLT:PETAVOLT"] = \
        Mul(Pow(10, 33), Sym("attov"))  # nopep8
    CONVERSIONS["ATTOVOLT:PICOVOLT"] = \
        Mul(Pow(10, 6), Sym("attov"))  # nopep8
    CONVERSIONS["ATTOVOLT:TERAVOLT"] = \
        Mul(Pow(10, 30), Sym("attov"))  # nopep8
    CONVERSIONS["ATTOVOLT:VOLT"] = \
        Mul(Pow(10, 18), Sym("attov"))  # nopep8
    CONVERSIONS["ATTOVOLT:YOCTOVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("attov"))  # nopep8
    CONVERSIONS["ATTOVOLT:YOTTAVOLT"] = \
        Mul(Pow(10, 42), Sym("attov"))  # nopep8
    CONVERSIONS["ATTOVOLT:ZEPTOVOLT"] = \
        Mul(Rat(Int(1), Int(1000)), Sym("attov"))  # nopep8
    CONVERSIONS["ATTOVOLT:ZETTAVOLT"] = \
        Mul(Pow(10, 39), Sym("attov"))  # nopep8
    CONVERSIONS["CENTIVOLT:ATTOVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 16)), Sym("centiv"))  # nopep8
    CONVERSIONS["CENTIVOLT:DECAVOLT"] = \
        Mul(Int(1000), Sym("centiv"))  # nopep8
    CONVERSIONS["CENTIVOLT:DECIVOLT"] = \
        Mul(Int(10), Sym("centiv"))  # nopep8
    CONVERSIONS["CENTIVOLT:EXAVOLT"] = \
        Mul(Pow(10, 20), Sym("centiv"))  # nopep8
    CONVERSIONS["CENTIVOLT:FEMTOVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 13)), Sym("centiv"))  # nopep8
    CONVERSIONS["CENTIVOLT:GIGAVOLT"] = \
        Mul(Pow(10, 11), Sym("centiv"))  # nopep8
    CONVERSIONS["CENTIVOLT:HECTOVOLT"] = \
        Mul(Pow(10, 4), Sym("centiv"))  # nopep8
    CONVERSIONS["CENTIVOLT:KILOVOLT"] = \
        Mul(Pow(10, 5), Sym("centiv"))  # nopep8
    CONVERSIONS["CENTIVOLT:MEGAVOLT"] = \
        Mul(Pow(10, 8), Sym("centiv"))  # nopep8
    CONVERSIONS["CENTIVOLT:MICROVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 4)), Sym("centiv"))  # nopep8
    CONVERSIONS["CENTIVOLT:MILLIVOLT"] = \
        Mul(Rat(Int(1), Int(10)), Sym("centiv"))  # nopep8
    CONVERSIONS["CENTIVOLT:NANOVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 7)), Sym("centiv"))  # nopep8
    CONVERSIONS["CENTIVOLT:PETAVOLT"] = \
        Mul(Pow(10, 17), Sym("centiv"))  # nopep8
    CONVERSIONS["CENTIVOLT:PICOVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 10)), Sym("centiv"))  # nopep8
    CONVERSIONS["CENTIVOLT:TERAVOLT"] = \
        Mul(Pow(10, 14), Sym("centiv"))  # nopep8
    CONVERSIONS["CENTIVOLT:VOLT"] = \
        Mul(Int(100), Sym("centiv"))  # nopep8
    CONVERSIONS["CENTIVOLT:YOCTOVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 22)), Sym("centiv"))  # nopep8
    CONVERSIONS["CENTIVOLT:YOTTAVOLT"] = \
        Mul(Pow(10, 26), Sym("centiv"))  # nopep8
    CONVERSIONS["CENTIVOLT:ZEPTOVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 19)), Sym("centiv"))  # nopep8
    CONVERSIONS["CENTIVOLT:ZETTAVOLT"] = \
        Mul(Pow(10, 23), Sym("centiv"))  # nopep8
    CONVERSIONS["DECAVOLT:ATTOVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 19)), Sym("decav"))  # nopep8
    CONVERSIONS["DECAVOLT:CENTIVOLT"] = \
        Mul(Rat(Int(1), Int(1000)), Sym("decav"))  # nopep8
    CONVERSIONS["DECAVOLT:DECIVOLT"] = \
        Mul(Rat(Int(1), Int(100)), Sym("decav"))  # nopep8
    CONVERSIONS["DECAVOLT:EXAVOLT"] = \
        Mul(Pow(10, 17), Sym("decav"))  # nopep8
    CONVERSIONS["DECAVOLT:FEMTOVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 16)), Sym("decav"))  # nopep8
    CONVERSIONS["DECAVOLT:GIGAVOLT"] = \
        Mul(Pow(10, 8), Sym("decav"))  # nopep8
    CONVERSIONS["DECAVOLT:HECTOVOLT"] = \
        Mul(Int(10), Sym("decav"))  # nopep8
    CONVERSIONS["DECAVOLT:KILOVOLT"] = \
        Mul(Int(100), Sym("decav"))  # nopep8
    CONVERSIONS["DECAVOLT:MEGAVOLT"] = \
        Mul(Pow(10, 5), Sym("decav"))  # nopep8
    CONVERSIONS["DECAVOLT:MICROVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 7)), Sym("decav"))  # nopep8
    CONVERSIONS["DECAVOLT:MILLIVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 4)), Sym("decav"))  # nopep8
    CONVERSIONS["DECAVOLT:NANOVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 10)), Sym("decav"))  # nopep8
    CONVERSIONS["DECAVOLT:PETAVOLT"] = \
        Mul(Pow(10, 14), Sym("decav"))  # nopep8
    CONVERSIONS["DECAVOLT:PICOVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 13)), Sym("decav"))  # nopep8
    CONVERSIONS["DECAVOLT:TERAVOLT"] = \
        Mul(Pow(10, 11), Sym("decav"))  # nopep8
    CONVERSIONS["DECAVOLT:VOLT"] = \
        Mul(Rat(Int(1), Int(10)), Sym("decav"))  # nopep8
    CONVERSIONS["DECAVOLT:YOCTOVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 25)), Sym("decav"))  # nopep8
    CONVERSIONS["DECAVOLT:YOTTAVOLT"] = \
        Mul(Pow(10, 23), Sym("decav"))  # nopep8
    CONVERSIONS["DECAVOLT:ZEPTOVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 22)), Sym("decav"))  # nopep8
    CONVERSIONS["DECAVOLT:ZETTAVOLT"] = \
        Mul(Pow(10, 20), Sym("decav"))  # nopep8
    CONVERSIONS["DECIVOLT:ATTOVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 17)), Sym("deciv"))  # nopep8
    CONVERSIONS["DECIVOLT:CENTIVOLT"] = \
        Mul(Rat(Int(1), Int(10)), Sym("deciv"))  # nopep8
    CONVERSIONS["DECIVOLT:DECAVOLT"] = \
        Mul(Int(100), Sym("deciv"))  # nopep8
    CONVERSIONS["DECIVOLT:EXAVOLT"] = \
        Mul(Pow(10, 19), Sym("deciv"))  # nopep8
    CONVERSIONS["DECIVOLT:FEMTOVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 14)), Sym("deciv"))  # nopep8
    CONVERSIONS["DECIVOLT:GIGAVOLT"] = \
        Mul(Pow(10, 10), Sym("deciv"))  # nopep8
    CONVERSIONS["DECIVOLT:HECTOVOLT"] = \
        Mul(Int(1000), Sym("deciv"))  # nopep8
    CONVERSIONS["DECIVOLT:KILOVOLT"] = \
        Mul(Pow(10, 4), Sym("deciv"))  # nopep8
    CONVERSIONS["DECIVOLT:MEGAVOLT"] = \
        Mul(Pow(10, 7), Sym("deciv"))  # nopep8
    CONVERSIONS["DECIVOLT:MICROVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 5)), Sym("deciv"))  # nopep8
    CONVERSIONS["DECIVOLT:MILLIVOLT"] = \
        Mul(Rat(Int(1), Int(100)), Sym("deciv"))  # nopep8
    CONVERSIONS["DECIVOLT:NANOVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 8)), Sym("deciv"))  # nopep8
    CONVERSIONS["DECIVOLT:PETAVOLT"] = \
        Mul(Pow(10, 16), Sym("deciv"))  # nopep8
    CONVERSIONS["DECIVOLT:PICOVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 11)), Sym("deciv"))  # nopep8
    CONVERSIONS["DECIVOLT:TERAVOLT"] = \
        Mul(Pow(10, 13), Sym("deciv"))  # nopep8
    CONVERSIONS["DECIVOLT:VOLT"] = \
        Mul(Int(10), Sym("deciv"))  # nopep8
    CONVERSIONS["DECIVOLT:YOCTOVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 23)), Sym("deciv"))  # nopep8
    CONVERSIONS["DECIVOLT:YOTTAVOLT"] = \
        Mul(Pow(10, 25), Sym("deciv"))  # nopep8
    CONVERSIONS["DECIVOLT:ZEPTOVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 20)), Sym("deciv"))  # nopep8
    CONVERSIONS["DECIVOLT:ZETTAVOLT"] = \
        Mul(Pow(10, 22), Sym("deciv"))  # nopep8
    CONVERSIONS["EXAVOLT:ATTOVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 36)), Sym("exav"))  # nopep8
    CONVERSIONS["EXAVOLT:CENTIVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 20)), Sym("exav"))  # nopep8
    CONVERSIONS["EXAVOLT:DECAVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 17)), Sym("exav"))  # nopep8
    CONVERSIONS["EXAVOLT:DECIVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 19)), Sym("exav"))  # nopep8
    CONVERSIONS["EXAVOLT:FEMTOVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 33)), Sym("exav"))  # nopep8
    CONVERSIONS["EXAVOLT:GIGAVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("exav"))  # nopep8
    CONVERSIONS["EXAVOLT:HECTOVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 16)), Sym("exav"))  # nopep8
    CONVERSIONS["EXAVOLT:KILOVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 15)), Sym("exav"))  # nopep8
    CONVERSIONS["EXAVOLT:MEGAVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 12)), Sym("exav"))  # nopep8
    CONVERSIONS["EXAVOLT:MICROVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 24)), Sym("exav"))  # nopep8
    CONVERSIONS["EXAVOLT:MILLIVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 21)), Sym("exav"))  # nopep8
    CONVERSIONS["EXAVOLT:NANOVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 27)), Sym("exav"))  # nopep8
    CONVERSIONS["EXAVOLT:PETAVOLT"] = \
        Mul(Rat(Int(1), Int(1000)), Sym("exav"))  # nopep8
    CONVERSIONS["EXAVOLT:PICOVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 30)), Sym("exav"))  # nopep8
    CONVERSIONS["EXAVOLT:TERAVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("exav"))  # nopep8
    CONVERSIONS["EXAVOLT:VOLT"] = \
        Mul(Rat(Int(1), Pow(10, 18)), Sym("exav"))  # nopep8
    CONVERSIONS["EXAVOLT:YOCTOVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 42)), Sym("exav"))  # nopep8
    CONVERSIONS["EXAVOLT:YOTTAVOLT"] = \
        Mul(Pow(10, 6), Sym("exav"))  # nopep8
    CONVERSIONS["EXAVOLT:ZEPTOVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 39)), Sym("exav"))  # nopep8
    CONVERSIONS["EXAVOLT:ZETTAVOLT"] = \
        Mul(Int(1000), Sym("exav"))  # nopep8
    CONVERSIONS["FEMTOVOLT:ATTOVOLT"] = \
        Mul(Rat(Int(1), Int(1000)), Sym("femtov"))  # nopep8
    CONVERSIONS["FEMTOVOLT:CENTIVOLT"] = \
        Mul(Pow(10, 13), Sym("femtov"))  # nopep8
    CONVERSIONS["FEMTOVOLT:DECAVOLT"] = \
        Mul(Pow(10, 16), Sym("femtov"))  # nopep8
    CONVERSIONS["FEMTOVOLT:DECIVOLT"] = \
        Mul(Pow(10, 14), Sym("femtov"))  # nopep8
    CONVERSIONS["FEMTOVOLT:EXAVOLT"] = \
        Mul(Pow(10, 33), Sym("femtov"))  # nopep8
    CONVERSIONS["FEMTOVOLT:GIGAVOLT"] = \
        Mul(Pow(10, 24), Sym("femtov"))  # nopep8
    CONVERSIONS["FEMTOVOLT:HECTOVOLT"] = \
        Mul(Pow(10, 17), Sym("femtov"))  # nopep8
    CONVERSIONS["FEMTOVOLT:KILOVOLT"] = \
        Mul(Pow(10, 18), Sym("femtov"))  # nopep8
    CONVERSIONS["FEMTOVOLT:MEGAVOLT"] = \
        Mul(Pow(10, 21), Sym("femtov"))  # nopep8
    CONVERSIONS["FEMTOVOLT:MICROVOLT"] = \
        Mul(Pow(10, 9), Sym("femtov"))  # nopep8
    CONVERSIONS["FEMTOVOLT:MILLIVOLT"] = \
        Mul(Pow(10, 12), Sym("femtov"))  # nopep8
    CONVERSIONS["FEMTOVOLT:NANOVOLT"] = \
        Mul(Pow(10, 6), Sym("femtov"))  # nopep8
    CONVERSIONS["FEMTOVOLT:PETAVOLT"] = \
        Mul(Pow(10, 30), Sym("femtov"))  # nopep8
    CONVERSIONS["FEMTOVOLT:PICOVOLT"] = \
        Mul(Int(1000), Sym("femtov"))  # nopep8
    CONVERSIONS["FEMTOVOLT:TERAVOLT"] = \
        Mul(Pow(10, 27), Sym("femtov"))  # nopep8
    CONVERSIONS["FEMTOVOLT:VOLT"] = \
        Mul(Pow(10, 15), Sym("femtov"))  # nopep8
    CONVERSIONS["FEMTOVOLT:YOCTOVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("femtov"))  # nopep8
    CONVERSIONS["FEMTOVOLT:YOTTAVOLT"] = \
        Mul(Pow(10, 39), Sym("femtov"))  # nopep8
    CONVERSIONS["FEMTOVOLT:ZEPTOVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("femtov"))  # nopep8
    CONVERSIONS["FEMTOVOLT:ZETTAVOLT"] = \
        Mul(Pow(10, 36), Sym("femtov"))  # nopep8
    CONVERSIONS["GIGAVOLT:ATTOVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 27)), Sym("gigav"))  # nopep8
    CONVERSIONS["GIGAVOLT:CENTIVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 11)), Sym("gigav"))  # nopep8
    CONVERSIONS["GIGAVOLT:DECAVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 8)), Sym("gigav"))  # nopep8
    CONVERSIONS["GIGAVOLT:DECIVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 10)), Sym("gigav"))  # nopep8
    CONVERSIONS["GIGAVOLT:EXAVOLT"] = \
        Mul(Pow(10, 9), Sym("gigav"))  # nopep8
    CONVERSIONS["GIGAVOLT:FEMTOVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 24)), Sym("gigav"))  # nopep8
    CONVERSIONS["GIGAVOLT:HECTOVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 7)), Sym("gigav"))  # nopep8
    CONVERSIONS["GIGAVOLT:KILOVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("gigav"))  # nopep8
    CONVERSIONS["GIGAVOLT:MEGAVOLT"] = \
        Mul(Rat(Int(1), Int(1000)), Sym("gigav"))  # nopep8
    CONVERSIONS["GIGAVOLT:MICROVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 15)), Sym("gigav"))  # nopep8
    CONVERSIONS["GIGAVOLT:MILLIVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 12)), Sym("gigav"))  # nopep8
    CONVERSIONS["GIGAVOLT:NANOVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 18)), Sym("gigav"))  # nopep8
    CONVERSIONS["GIGAVOLT:PETAVOLT"] = \
        Mul(Pow(10, 6), Sym("gigav"))  # nopep8
    CONVERSIONS["GIGAVOLT:PICOVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 21)), Sym("gigav"))  # nopep8
    CONVERSIONS["GIGAVOLT:TERAVOLT"] = \
        Mul(Int(1000), Sym("gigav"))  # nopep8
    CONVERSIONS["GIGAVOLT:VOLT"] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("gigav"))  # nopep8
    CONVERSIONS["GIGAVOLT:YOCTOVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 33)), Sym("gigav"))  # nopep8
    CONVERSIONS["GIGAVOLT:YOTTAVOLT"] = \
        Mul(Pow(10, 15), Sym("gigav"))  # nopep8
    CONVERSIONS["GIGAVOLT:ZEPTOVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 30)), Sym("gigav"))  # nopep8
    CONVERSIONS["GIGAVOLT:ZETTAVOLT"] = \
        Mul(Pow(10, 12), Sym("gigav"))  # nopep8
    CONVERSIONS["HECTOVOLT:ATTOVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 20)), Sym("hectov"))  # nopep8
    CONVERSIONS["HECTOVOLT:CENTIVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 4)), Sym("hectov"))  # nopep8
    CONVERSIONS["HECTOVOLT:DECAVOLT"] = \
        Mul(Rat(Int(1), Int(10)), Sym("hectov"))  # nopep8
    CONVERSIONS["HECTOVOLT:DECIVOLT"] = \
        Mul(Rat(Int(1), Int(1000)), Sym("hectov"))  # nopep8
    CONVERSIONS["HECTOVOLT:EXAVOLT"] = \
        Mul(Pow(10, 16), Sym("hectov"))  # nopep8
    CONVERSIONS["HECTOVOLT:FEMTOVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 17)), Sym("hectov"))  # nopep8
    CONVERSIONS["HECTOVOLT:GIGAVOLT"] = \
        Mul(Pow(10, 7), Sym("hectov"))  # nopep8
    CONVERSIONS["HECTOVOLT:KILOVOLT"] = \
        Mul(Int(10), Sym("hectov"))  # nopep8
    CONVERSIONS["HECTOVOLT:MEGAVOLT"] = \
        Mul(Pow(10, 4), Sym("hectov"))  # nopep8
    CONVERSIONS["HECTOVOLT:MICROVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 8)), Sym("hectov"))  # nopep8
    CONVERSIONS["HECTOVOLT:MILLIVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 5)), Sym("hectov"))  # nopep8
    CONVERSIONS["HECTOVOLT:NANOVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 11)), Sym("hectov"))  # nopep8
    CONVERSIONS["HECTOVOLT:PETAVOLT"] = \
        Mul(Pow(10, 13), Sym("hectov"))  # nopep8
    CONVERSIONS["HECTOVOLT:PICOVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 14)), Sym("hectov"))  # nopep8
    CONVERSIONS["HECTOVOLT:TERAVOLT"] = \
        Mul(Pow(10, 10), Sym("hectov"))  # nopep8
    CONVERSIONS["HECTOVOLT:VOLT"] = \
        Mul(Rat(Int(1), Int(100)), Sym("hectov"))  # nopep8
    CONVERSIONS["HECTOVOLT:YOCTOVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 26)), Sym("hectov"))  # nopep8
    CONVERSIONS["HECTOVOLT:YOTTAVOLT"] = \
        Mul(Pow(10, 22), Sym("hectov"))  # nopep8
    CONVERSIONS["HECTOVOLT:ZEPTOVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 23)), Sym("hectov"))  # nopep8
    CONVERSIONS["HECTOVOLT:ZETTAVOLT"] = \
        Mul(Pow(10, 19), Sym("hectov"))  # nopep8
    CONVERSIONS["KILOVOLT:ATTOVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 21)), Sym("kilov"))  # nopep8
    CONVERSIONS["KILOVOLT:CENTIVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 5)), Sym("kilov"))  # nopep8
    CONVERSIONS["KILOVOLT:DECAVOLT"] = \
        Mul(Rat(Int(1), Int(100)), Sym("kilov"))  # nopep8
    CONVERSIONS["KILOVOLT:DECIVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 4)), Sym("kilov"))  # nopep8
    CONVERSIONS["KILOVOLT:EXAVOLT"] = \
        Mul(Pow(10, 15), Sym("kilov"))  # nopep8
    CONVERSIONS["KILOVOLT:FEMTOVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 18)), Sym("kilov"))  # nopep8
    CONVERSIONS["KILOVOLT:GIGAVOLT"] = \
        Mul(Pow(10, 6), Sym("kilov"))  # nopep8
    CONVERSIONS["KILOVOLT:HECTOVOLT"] = \
        Mul(Rat(Int(1), Int(10)), Sym("kilov"))  # nopep8
    CONVERSIONS["KILOVOLT:MEGAVOLT"] = \
        Mul(Int(1000), Sym("kilov"))  # nopep8
    CONVERSIONS["KILOVOLT:MICROVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("kilov"))  # nopep8
    CONVERSIONS["KILOVOLT:MILLIVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("kilov"))  # nopep8
    CONVERSIONS["KILOVOLT:NANOVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 12)), Sym("kilov"))  # nopep8
    CONVERSIONS["KILOVOLT:PETAVOLT"] = \
        Mul(Pow(10, 12), Sym("kilov"))  # nopep8
    CONVERSIONS["KILOVOLT:PICOVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 15)), Sym("kilov"))  # nopep8
    CONVERSIONS["KILOVOLT:TERAVOLT"] = \
        Mul(Pow(10, 9), Sym("kilov"))  # nopep8
    CONVERSIONS["KILOVOLT:VOLT"] = \
        Mul(Rat(Int(1), Int(1000)), Sym("kilov"))  # nopep8
    CONVERSIONS["KILOVOLT:YOCTOVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 27)), Sym("kilov"))  # nopep8
    CONVERSIONS["KILOVOLT:YOTTAVOLT"] = \
        Mul(Pow(10, 21), Sym("kilov"))  # nopep8
    CONVERSIONS["KILOVOLT:ZEPTOVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 24)), Sym("kilov"))  # nopep8
    CONVERSIONS["KILOVOLT:ZETTAVOLT"] = \
        Mul(Pow(10, 18), Sym("kilov"))  # nopep8
    CONVERSIONS["MEGAVOLT:ATTOVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 24)), Sym("megav"))  # nopep8
    CONVERSIONS["MEGAVOLT:CENTIVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 8)), Sym("megav"))  # nopep8
    CONVERSIONS["MEGAVOLT:DECAVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 5)), Sym("megav"))  # nopep8
    CONVERSIONS["MEGAVOLT:DECIVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 7)), Sym("megav"))  # nopep8
    CONVERSIONS["MEGAVOLT:EXAVOLT"] = \
        Mul(Pow(10, 12), Sym("megav"))  # nopep8
    CONVERSIONS["MEGAVOLT:FEMTOVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 21)), Sym("megav"))  # nopep8
    CONVERSIONS["MEGAVOLT:GIGAVOLT"] = \
        Mul(Int(1000), Sym("megav"))  # nopep8
    CONVERSIONS["MEGAVOLT:HECTOVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 4)), Sym("megav"))  # nopep8
    CONVERSIONS["MEGAVOLT:KILOVOLT"] = \
        Mul(Rat(Int(1), Int(1000)), Sym("megav"))  # nopep8
    CONVERSIONS["MEGAVOLT:MICROVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 12)), Sym("megav"))  # nopep8
    CONVERSIONS["MEGAVOLT:MILLIVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("megav"))  # nopep8
    CONVERSIONS["MEGAVOLT:NANOVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 15)), Sym("megav"))  # nopep8
    CONVERSIONS["MEGAVOLT:PETAVOLT"] = \
        Mul(Pow(10, 9), Sym("megav"))  # nopep8
    CONVERSIONS["MEGAVOLT:PICOVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 18)), Sym("megav"))  # nopep8
    CONVERSIONS["MEGAVOLT:TERAVOLT"] = \
        Mul(Pow(10, 6), Sym("megav"))  # nopep8
    CONVERSIONS["MEGAVOLT:VOLT"] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("megav"))  # nopep8
    CONVERSIONS["MEGAVOLT:YOCTOVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 30)), Sym("megav"))  # nopep8
    CONVERSIONS["MEGAVOLT:YOTTAVOLT"] = \
        Mul(Pow(10, 18), Sym("megav"))  # nopep8
    CONVERSIONS["MEGAVOLT:ZEPTOVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 27)), Sym("megav"))  # nopep8
    CONVERSIONS["MEGAVOLT:ZETTAVOLT"] = \
        Mul(Pow(10, 15), Sym("megav"))  # nopep8
    CONVERSIONS["MICROVOLT:ATTOVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 12)), Sym("microv"))  # nopep8
    CONVERSIONS["MICROVOLT:CENTIVOLT"] = \
        Mul(Pow(10, 4), Sym("microv"))  # nopep8
    CONVERSIONS["MICROVOLT:DECAVOLT"] = \
        Mul(Pow(10, 7), Sym("microv"))  # nopep8
    CONVERSIONS["MICROVOLT:DECIVOLT"] = \
        Mul(Pow(10, 5), Sym("microv"))  # nopep8
    CONVERSIONS["MICROVOLT:EXAVOLT"] = \
        Mul(Pow(10, 24), Sym("microv"))  # nopep8
    CONVERSIONS["MICROVOLT:FEMTOVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("microv"))  # nopep8
    CONVERSIONS["MICROVOLT:GIGAVOLT"] = \
        Mul(Pow(10, 15), Sym("microv"))  # nopep8
    CONVERSIONS["MICROVOLT:HECTOVOLT"] = \
        Mul(Pow(10, 8), Sym("microv"))  # nopep8
    CONVERSIONS["MICROVOLT:KILOVOLT"] = \
        Mul(Pow(10, 9), Sym("microv"))  # nopep8
    CONVERSIONS["MICROVOLT:MEGAVOLT"] = \
        Mul(Pow(10, 12), Sym("microv"))  # nopep8
    CONVERSIONS["MICROVOLT:MILLIVOLT"] = \
        Mul(Int(1000), Sym("microv"))  # nopep8
    CONVERSIONS["MICROVOLT:NANOVOLT"] = \
        Mul(Rat(Int(1), Int(1000)), Sym("microv"))  # nopep8
    CONVERSIONS["MICROVOLT:PETAVOLT"] = \
        Mul(Pow(10, 21), Sym("microv"))  # nopep8
    CONVERSIONS["MICROVOLT:PICOVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("microv"))  # nopep8
    CONVERSIONS["MICROVOLT:TERAVOLT"] = \
        Mul(Pow(10, 18), Sym("microv"))  # nopep8
    CONVERSIONS["MICROVOLT:VOLT"] = \
        Mul(Pow(10, 6), Sym("microv"))  # nopep8
    CONVERSIONS["MICROVOLT:YOCTOVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 18)), Sym("microv"))  # nopep8
    CONVERSIONS["MICROVOLT:YOTTAVOLT"] = \
        Mul(Pow(10, 30), Sym("microv"))  # nopep8
    CONVERSIONS["MICROVOLT:ZEPTOVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 15)), Sym("microv"))  # nopep8
    CONVERSIONS["MICROVOLT:ZETTAVOLT"] = \
        Mul(Pow(10, 27), Sym("microv"))  # nopep8
    CONVERSIONS["MILLIVOLT:ATTOVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 15)), Sym("milliv"))  # nopep8
    CONVERSIONS["MILLIVOLT:CENTIVOLT"] = \
        Mul(Int(10), Sym("milliv"))  # nopep8
    CONVERSIONS["MILLIVOLT:DECAVOLT"] = \
        Mul(Pow(10, 4), Sym("milliv"))  # nopep8
    CONVERSIONS["MILLIVOLT:DECIVOLT"] = \
        Mul(Int(100), Sym("milliv"))  # nopep8
    CONVERSIONS["MILLIVOLT:EXAVOLT"] = \
        Mul(Pow(10, 21), Sym("milliv"))  # nopep8
    CONVERSIONS["MILLIVOLT:FEMTOVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 12)), Sym("milliv"))  # nopep8
    CONVERSIONS["MILLIVOLT:GIGAVOLT"] = \
        Mul(Pow(10, 12), Sym("milliv"))  # nopep8
    CONVERSIONS["MILLIVOLT:HECTOVOLT"] = \
        Mul(Pow(10, 5), Sym("milliv"))  # nopep8
    CONVERSIONS["MILLIVOLT:KILOVOLT"] = \
        Mul(Pow(10, 6), Sym("milliv"))  # nopep8
    CONVERSIONS["MILLIVOLT:MEGAVOLT"] = \
        Mul(Pow(10, 9), Sym("milliv"))  # nopep8
    CONVERSIONS["MILLIVOLT:MICROVOLT"] = \
        Mul(Rat(Int(1), Int(1000)), Sym("milliv"))  # nopep8
    CONVERSIONS["MILLIVOLT:NANOVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("milliv"))  # nopep8
    CONVERSIONS["MILLIVOLT:PETAVOLT"] = \
        Mul(Pow(10, 18), Sym("milliv"))  # nopep8
    CONVERSIONS["MILLIVOLT:PICOVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("milliv"))  # nopep8
    CONVERSIONS["MILLIVOLT:TERAVOLT"] = \
        Mul(Pow(10, 15), Sym("milliv"))  # nopep8
    CONVERSIONS["MILLIVOLT:VOLT"] = \
        Mul(Int(1000), Sym("milliv"))  # nopep8
    CONVERSIONS["MILLIVOLT:YOCTOVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 21)), Sym("milliv"))  # nopep8
    CONVERSIONS["MILLIVOLT:YOTTAVOLT"] = \
        Mul(Pow(10, 27), Sym("milliv"))  # nopep8
    CONVERSIONS["MILLIVOLT:ZEPTOVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 18)), Sym("milliv"))  # nopep8
    CONVERSIONS["MILLIVOLT:ZETTAVOLT"] = \
        Mul(Pow(10, 24), Sym("milliv"))  # nopep8
    CONVERSIONS["NANOVOLT:ATTOVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("nanov"))  # nopep8
    CONVERSIONS["NANOVOLT:CENTIVOLT"] = \
        Mul(Pow(10, 7), Sym("nanov"))  # nopep8
    CONVERSIONS["NANOVOLT:DECAVOLT"] = \
        Mul(Pow(10, 10), Sym("nanov"))  # nopep8
    CONVERSIONS["NANOVOLT:DECIVOLT"] = \
        Mul(Pow(10, 8), Sym("nanov"))  # nopep8
    CONVERSIONS["NANOVOLT:EXAVOLT"] = \
        Mul(Pow(10, 27), Sym("nanov"))  # nopep8
    CONVERSIONS["NANOVOLT:FEMTOVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("nanov"))  # nopep8
    CONVERSIONS["NANOVOLT:GIGAVOLT"] = \
        Mul(Pow(10, 18), Sym("nanov"))  # nopep8
    CONVERSIONS["NANOVOLT:HECTOVOLT"] = \
        Mul(Pow(10, 11), Sym("nanov"))  # nopep8
    CONVERSIONS["NANOVOLT:KILOVOLT"] = \
        Mul(Pow(10, 12), Sym("nanov"))  # nopep8
    CONVERSIONS["NANOVOLT:MEGAVOLT"] = \
        Mul(Pow(10, 15), Sym("nanov"))  # nopep8
    CONVERSIONS["NANOVOLT:MICROVOLT"] = \
        Mul(Int(1000), Sym("nanov"))  # nopep8
    CONVERSIONS["NANOVOLT:MILLIVOLT"] = \
        Mul(Pow(10, 6), Sym("nanov"))  # nopep8
    CONVERSIONS["NANOVOLT:PETAVOLT"] = \
        Mul(Pow(10, 24), Sym("nanov"))  # nopep8
    CONVERSIONS["NANOVOLT:PICOVOLT"] = \
        Mul(Rat(Int(1), Int(1000)), Sym("nanov"))  # nopep8
    CONVERSIONS["NANOVOLT:TERAVOLT"] = \
        Mul(Pow(10, 21), Sym("nanov"))  # nopep8
    CONVERSIONS["NANOVOLT:VOLT"] = \
        Mul(Pow(10, 9), Sym("nanov"))  # nopep8
    CONVERSIONS["NANOVOLT:YOCTOVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 15)), Sym("nanov"))  # nopep8
    CONVERSIONS["NANOVOLT:YOTTAVOLT"] = \
        Mul(Pow(10, 33), Sym("nanov"))  # nopep8
    CONVERSIONS["NANOVOLT:ZEPTOVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 12)), Sym("nanov"))  # nopep8
    CONVERSIONS["NANOVOLT:ZETTAVOLT"] = \
        Mul(Pow(10, 30), Sym("nanov"))  # nopep8
    CONVERSIONS["PETAVOLT:ATTOVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 33)), Sym("petav"))  # nopep8
    CONVERSIONS["PETAVOLT:CENTIVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 17)), Sym("petav"))  # nopep8
    CONVERSIONS["PETAVOLT:DECAVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 14)), Sym("petav"))  # nopep8
    CONVERSIONS["PETAVOLT:DECIVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 16)), Sym("petav"))  # nopep8
    CONVERSIONS["PETAVOLT:EXAVOLT"] = \
        Mul(Int(1000), Sym("petav"))  # nopep8
    CONVERSIONS["PETAVOLT:FEMTOVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 30)), Sym("petav"))  # nopep8
    CONVERSIONS["PETAVOLT:GIGAVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("petav"))  # nopep8
    CONVERSIONS["PETAVOLT:HECTOVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 13)), Sym("petav"))  # nopep8
    CONVERSIONS["PETAVOLT:KILOVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 12)), Sym("petav"))  # nopep8
    CONVERSIONS["PETAVOLT:MEGAVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("petav"))  # nopep8
    CONVERSIONS["PETAVOLT:MICROVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 21)), Sym("petav"))  # nopep8
    CONVERSIONS["PETAVOLT:MILLIVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 18)), Sym("petav"))  # nopep8
    CONVERSIONS["PETAVOLT:NANOVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 24)), Sym("petav"))  # nopep8
    CONVERSIONS["PETAVOLT:PICOVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 27)), Sym("petav"))  # nopep8
    CONVERSIONS["PETAVOLT:TERAVOLT"] = \
        Mul(Rat(Int(1), Int(1000)), Sym("petav"))  # nopep8
    CONVERSIONS["PETAVOLT:VOLT"] = \
        Mul(Rat(Int(1), Pow(10, 15)), Sym("petav"))  # nopep8
    CONVERSIONS["PETAVOLT:YOCTOVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 39)), Sym("petav"))  # nopep8
    CONVERSIONS["PETAVOLT:YOTTAVOLT"] = \
        Mul(Pow(10, 9), Sym("petav"))  # nopep8
    CONVERSIONS["PETAVOLT:ZEPTOVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 36)), Sym("petav"))  # nopep8
    CONVERSIONS["PETAVOLT:ZETTAVOLT"] = \
        Mul(Pow(10, 6), Sym("petav"))  # nopep8
    CONVERSIONS["PICOVOLT:ATTOVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("picov"))  # nopep8
    CONVERSIONS["PICOVOLT:CENTIVOLT"] = \
        Mul(Pow(10, 10), Sym("picov"))  # nopep8
    CONVERSIONS["PICOVOLT:DECAVOLT"] = \
        Mul(Pow(10, 13), Sym("picov"))  # nopep8
    CONVERSIONS["PICOVOLT:DECIVOLT"] = \
        Mul(Pow(10, 11), Sym("picov"))  # nopep8
    CONVERSIONS["PICOVOLT:EXAVOLT"] = \
        Mul(Pow(10, 30), Sym("picov"))  # nopep8
    CONVERSIONS["PICOVOLT:FEMTOVOLT"] = \
        Mul(Rat(Int(1), Int(1000)), Sym("picov"))  # nopep8
    CONVERSIONS["PICOVOLT:GIGAVOLT"] = \
        Mul(Pow(10, 21), Sym("picov"))  # nopep8
    CONVERSIONS["PICOVOLT:HECTOVOLT"] = \
        Mul(Pow(10, 14), Sym("picov"))  # nopep8
    CONVERSIONS["PICOVOLT:KILOVOLT"] = \
        Mul(Pow(10, 15), Sym("picov"))  # nopep8
    CONVERSIONS["PICOVOLT:MEGAVOLT"] = \
        Mul(Pow(10, 18), Sym("picov"))  # nopep8
    CONVERSIONS["PICOVOLT:MICROVOLT"] = \
        Mul(Pow(10, 6), Sym("picov"))  # nopep8
    CONVERSIONS["PICOVOLT:MILLIVOLT"] = \
        Mul(Pow(10, 9), Sym("picov"))  # nopep8
    CONVERSIONS["PICOVOLT:NANOVOLT"] = \
        Mul(Int(1000), Sym("picov"))  # nopep8
    CONVERSIONS["PICOVOLT:PETAVOLT"] = \
        Mul(Pow(10, 27), Sym("picov"))  # nopep8
    CONVERSIONS["PICOVOLT:TERAVOLT"] = \
        Mul(Pow(10, 24), Sym("picov"))  # nopep8
    CONVERSIONS["PICOVOLT:VOLT"] = \
        Mul(Pow(10, 12), Sym("picov"))  # nopep8
    CONVERSIONS["PICOVOLT:YOCTOVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 12)), Sym("picov"))  # nopep8
    CONVERSIONS["PICOVOLT:YOTTAVOLT"] = \
        Mul(Pow(10, 36), Sym("picov"))  # nopep8
    CONVERSIONS["PICOVOLT:ZEPTOVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("picov"))  # nopep8
    CONVERSIONS["PICOVOLT:ZETTAVOLT"] = \
        Mul(Pow(10, 33), Sym("picov"))  # nopep8
    CONVERSIONS["TERAVOLT:ATTOVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 30)), Sym("terav"))  # nopep8
    CONVERSIONS["TERAVOLT:CENTIVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 14)), Sym("terav"))  # nopep8
    CONVERSIONS["TERAVOLT:DECAVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 11)), Sym("terav"))  # nopep8
    CONVERSIONS["TERAVOLT:DECIVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 13)), Sym("terav"))  # nopep8
    CONVERSIONS["TERAVOLT:EXAVOLT"] = \
        Mul(Pow(10, 6), Sym("terav"))  # nopep8
    CONVERSIONS["TERAVOLT:FEMTOVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 27)), Sym("terav"))  # nopep8
    CONVERSIONS["TERAVOLT:GIGAVOLT"] = \
        Mul(Rat(Int(1), Int(1000)), Sym("terav"))  # nopep8
    CONVERSIONS["TERAVOLT:HECTOVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 10)), Sym("terav"))  # nopep8
    CONVERSIONS["TERAVOLT:KILOVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("terav"))  # nopep8
    CONVERSIONS["TERAVOLT:MEGAVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("terav"))  # nopep8
    CONVERSIONS["TERAVOLT:MICROVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 18)), Sym("terav"))  # nopep8
    CONVERSIONS["TERAVOLT:MILLIVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 15)), Sym("terav"))  # nopep8
    CONVERSIONS["TERAVOLT:NANOVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 21)), Sym("terav"))  # nopep8
    CONVERSIONS["TERAVOLT:PETAVOLT"] = \
        Mul(Int(1000), Sym("terav"))  # nopep8
    CONVERSIONS["TERAVOLT:PICOVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 24)), Sym("terav"))  # nopep8
    CONVERSIONS["TERAVOLT:VOLT"] = \
        Mul(Rat(Int(1), Pow(10, 12)), Sym("terav"))  # nopep8
    CONVERSIONS["TERAVOLT:YOCTOVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 36)), Sym("terav"))  # nopep8
    CONVERSIONS["TERAVOLT:YOTTAVOLT"] = \
        Mul(Pow(10, 12), Sym("terav"))  # nopep8
    CONVERSIONS["TERAVOLT:ZEPTOVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 33)), Sym("terav"))  # nopep8
    CONVERSIONS["TERAVOLT:ZETTAVOLT"] = \
        Mul(Pow(10, 9), Sym("terav"))  # nopep8
    CONVERSIONS["VOLT:ATTOVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 18)), Sym("v"))  # nopep8
    CONVERSIONS["VOLT:CENTIVOLT"] = \
        Mul(Rat(Int(1), Int(100)), Sym("v"))  # nopep8
    CONVERSIONS["VOLT:DECAVOLT"] = \
        Mul(Int(10), Sym("v"))  # nopep8
    CONVERSIONS["VOLT:DECIVOLT"] = \
        Mul(Rat(Int(1), Int(10)), Sym("v"))  # nopep8
    CONVERSIONS["VOLT:EXAVOLT"] = \
        Mul(Pow(10, 18), Sym("v"))  # nopep8
    CONVERSIONS["VOLT:FEMTOVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 15)), Sym("v"))  # nopep8
    CONVERSIONS["VOLT:GIGAVOLT"] = \
        Mul(Pow(10, 9), Sym("v"))  # nopep8
    CONVERSIONS["VOLT:HECTOVOLT"] = \
        Mul(Int(100), Sym("v"))  # nopep8
    CONVERSIONS["VOLT:KILOVOLT"] = \
        Mul(Int(1000), Sym("v"))  # nopep8
    CONVERSIONS["VOLT:MEGAVOLT"] = \
        Mul(Pow(10, 6), Sym("v"))  # nopep8
    CONVERSIONS["VOLT:MICROVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("v"))  # nopep8
    CONVERSIONS["VOLT:MILLIVOLT"] = \
        Mul(Rat(Int(1), Int(1000)), Sym("v"))  # nopep8
    CONVERSIONS["VOLT:NANOVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("v"))  # nopep8
    CONVERSIONS["VOLT:PETAVOLT"] = \
        Mul(Pow(10, 15), Sym("v"))  # nopep8
    CONVERSIONS["VOLT:PICOVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 12)), Sym("v"))  # nopep8
    CONVERSIONS["VOLT:TERAVOLT"] = \
        Mul(Pow(10, 12), Sym("v"))  # nopep8
    CONVERSIONS["VOLT:YOCTOVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 24)), Sym("v"))  # nopep8
    CONVERSIONS["VOLT:YOTTAVOLT"] = \
        Mul(Pow(10, 24), Sym("v"))  # nopep8
    CONVERSIONS["VOLT:ZEPTOVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 21)), Sym("v"))  # nopep8
    CONVERSIONS["VOLT:ZETTAVOLT"] = \
        Mul(Pow(10, 21), Sym("v"))  # nopep8
    CONVERSIONS["YOCTOVOLT:ATTOVOLT"] = \
        Mul(Pow(10, 6), Sym("yoctov"))  # nopep8
    CONVERSIONS["YOCTOVOLT:CENTIVOLT"] = \
        Mul(Pow(10, 22), Sym("yoctov"))  # nopep8
    CONVERSIONS["YOCTOVOLT:DECAVOLT"] = \
        Mul(Pow(10, 25), Sym("yoctov"))  # nopep8
    CONVERSIONS["YOCTOVOLT:DECIVOLT"] = \
        Mul(Pow(10, 23), Sym("yoctov"))  # nopep8
    CONVERSIONS["YOCTOVOLT:EXAVOLT"] = \
        Mul(Pow(10, 42), Sym("yoctov"))  # nopep8
    CONVERSIONS["YOCTOVOLT:FEMTOVOLT"] = \
        Mul(Pow(10, 9), Sym("yoctov"))  # nopep8
    CONVERSIONS["YOCTOVOLT:GIGAVOLT"] = \
        Mul(Pow(10, 33), Sym("yoctov"))  # nopep8
    CONVERSIONS["YOCTOVOLT:HECTOVOLT"] = \
        Mul(Pow(10, 26), Sym("yoctov"))  # nopep8
    CONVERSIONS["YOCTOVOLT:KILOVOLT"] = \
        Mul(Pow(10, 27), Sym("yoctov"))  # nopep8
    CONVERSIONS["YOCTOVOLT:MEGAVOLT"] = \
        Mul(Pow(10, 30), Sym("yoctov"))  # nopep8
    CONVERSIONS["YOCTOVOLT:MICROVOLT"] = \
        Mul(Pow(10, 18), Sym("yoctov"))  # nopep8
    CONVERSIONS["YOCTOVOLT:MILLIVOLT"] = \
        Mul(Pow(10, 21), Sym("yoctov"))  # nopep8
    CONVERSIONS["YOCTOVOLT:NANOVOLT"] = \
        Mul(Pow(10, 15), Sym("yoctov"))  # nopep8
    CONVERSIONS["YOCTOVOLT:PETAVOLT"] = \
        Mul(Pow(10, 39), Sym("yoctov"))  # nopep8
    CONVERSIONS["YOCTOVOLT:PICOVOLT"] = \
        Mul(Pow(10, 12), Sym("yoctov"))  # nopep8
    CONVERSIONS["YOCTOVOLT:TERAVOLT"] = \
        Mul(Pow(10, 36), Sym("yoctov"))  # nopep8
    CONVERSIONS["YOCTOVOLT:VOLT"] = \
        Mul(Pow(10, 24), Sym("yoctov"))  # nopep8
    CONVERSIONS["YOCTOVOLT:YOTTAVOLT"] = \
        Mul(Pow(10, 48), Sym("yoctov"))  # nopep8
    CONVERSIONS["YOCTOVOLT:ZEPTOVOLT"] = \
        Mul(Int(1000), Sym("yoctov"))  # nopep8
    CONVERSIONS["YOCTOVOLT:ZETTAVOLT"] = \
        Mul(Pow(10, 45), Sym("yoctov"))  # nopep8
    CONVERSIONS["YOTTAVOLT:ATTOVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 42)), Sym("yottav"))  # nopep8
    CONVERSIONS["YOTTAVOLT:CENTIVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 26)), Sym("yottav"))  # nopep8
    CONVERSIONS["YOTTAVOLT:DECAVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 23)), Sym("yottav"))  # nopep8
    CONVERSIONS["YOTTAVOLT:DECIVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 25)), Sym("yottav"))  # nopep8
    CONVERSIONS["YOTTAVOLT:EXAVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("yottav"))  # nopep8
    CONVERSIONS["YOTTAVOLT:FEMTOVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 39)), Sym("yottav"))  # nopep8
    CONVERSIONS["YOTTAVOLT:GIGAVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 15)), Sym("yottav"))  # nopep8
    CONVERSIONS["YOTTAVOLT:HECTOVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 22)), Sym("yottav"))  # nopep8
    CONVERSIONS["YOTTAVOLT:KILOVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 21)), Sym("yottav"))  # nopep8
    CONVERSIONS["YOTTAVOLT:MEGAVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 18)), Sym("yottav"))  # nopep8
    CONVERSIONS["YOTTAVOLT:MICROVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 30)), Sym("yottav"))  # nopep8
    CONVERSIONS["YOTTAVOLT:MILLIVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 27)), Sym("yottav"))  # nopep8
    CONVERSIONS["YOTTAVOLT:NANOVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 33)), Sym("yottav"))  # nopep8
    CONVERSIONS["YOTTAVOLT:PETAVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("yottav"))  # nopep8
    CONVERSIONS["YOTTAVOLT:PICOVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 36)), Sym("yottav"))  # nopep8
    CONVERSIONS["YOTTAVOLT:TERAVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 12)), Sym("yottav"))  # nopep8
    CONVERSIONS["YOTTAVOLT:VOLT"] = \
        Mul(Rat(Int(1), Pow(10, 24)), Sym("yottav"))  # nopep8
    CONVERSIONS["YOTTAVOLT:YOCTOVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 48)), Sym("yottav"))  # nopep8
    CONVERSIONS["YOTTAVOLT:ZEPTOVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 45)), Sym("yottav"))  # nopep8
    CONVERSIONS["YOTTAVOLT:ZETTAVOLT"] = \
        Mul(Rat(Int(1), Int(1000)), Sym("yottav"))  # nopep8
    CONVERSIONS["ZEPTOVOLT:ATTOVOLT"] = \
        Mul(Int(1000), Sym("zeptov"))  # nopep8
    CONVERSIONS["ZEPTOVOLT:CENTIVOLT"] = \
        Mul(Pow(10, 19), Sym("zeptov"))  # nopep8
    CONVERSIONS["ZEPTOVOLT:DECAVOLT"] = \
        Mul(Pow(10, 22), Sym("zeptov"))  # nopep8
    CONVERSIONS["ZEPTOVOLT:DECIVOLT"] = \
        Mul(Pow(10, 20), Sym("zeptov"))  # nopep8
    CONVERSIONS["ZEPTOVOLT:EXAVOLT"] = \
        Mul(Pow(10, 39), Sym("zeptov"))  # nopep8
    CONVERSIONS["ZEPTOVOLT:FEMTOVOLT"] = \
        Mul(Pow(10, 6), Sym("zeptov"))  # nopep8
    CONVERSIONS["ZEPTOVOLT:GIGAVOLT"] = \
        Mul(Pow(10, 30), Sym("zeptov"))  # nopep8
    CONVERSIONS["ZEPTOVOLT:HECTOVOLT"] = \
        Mul(Pow(10, 23), Sym("zeptov"))  # nopep8
    CONVERSIONS["ZEPTOVOLT:KILOVOLT"] = \
        Mul(Pow(10, 24), Sym("zeptov"))  # nopep8
    CONVERSIONS["ZEPTOVOLT:MEGAVOLT"] = \
        Mul(Pow(10, 27), Sym("zeptov"))  # nopep8
    CONVERSIONS["ZEPTOVOLT:MICROVOLT"] = \
        Mul(Pow(10, 15), Sym("zeptov"))  # nopep8
    CONVERSIONS["ZEPTOVOLT:MILLIVOLT"] = \
        Mul(Pow(10, 18), Sym("zeptov"))  # nopep8
    CONVERSIONS["ZEPTOVOLT:NANOVOLT"] = \
        Mul(Pow(10, 12), Sym("zeptov"))  # nopep8
    CONVERSIONS["ZEPTOVOLT:PETAVOLT"] = \
        Mul(Pow(10, 36), Sym("zeptov"))  # nopep8
    CONVERSIONS["ZEPTOVOLT:PICOVOLT"] = \
        Mul(Pow(10, 9), Sym("zeptov"))  # nopep8
    CONVERSIONS["ZEPTOVOLT:TERAVOLT"] = \
        Mul(Pow(10, 33), Sym("zeptov"))  # nopep8
    CONVERSIONS["ZEPTOVOLT:VOLT"] = \
        Mul(Pow(10, 21), Sym("zeptov"))  # nopep8
    CONVERSIONS["ZEPTOVOLT:YOCTOVOLT"] = \
        Mul(Rat(Int(1), Int(1000)), Sym("zeptov"))  # nopep8
    CONVERSIONS["ZEPTOVOLT:YOTTAVOLT"] = \
        Mul(Pow(10, 45), Sym("zeptov"))  # nopep8
    CONVERSIONS["ZEPTOVOLT:ZETTAVOLT"] = \
        Mul(Pow(10, 42), Sym("zeptov"))  # nopep8
    CONVERSIONS["ZETTAVOLT:ATTOVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 39)), Sym("zettav"))  # nopep8
    CONVERSIONS["ZETTAVOLT:CENTIVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 23)), Sym("zettav"))  # nopep8
    CONVERSIONS["ZETTAVOLT:DECAVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 20)), Sym("zettav"))  # nopep8
    CONVERSIONS["ZETTAVOLT:DECIVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 22)), Sym("zettav"))  # nopep8
    CONVERSIONS["ZETTAVOLT:EXAVOLT"] = \
        Mul(Rat(Int(1), Int(1000)), Sym("zettav"))  # nopep8
    CONVERSIONS["ZETTAVOLT:FEMTOVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 36)), Sym("zettav"))  # nopep8
    CONVERSIONS["ZETTAVOLT:GIGAVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 12)), Sym("zettav"))  # nopep8
    CONVERSIONS["ZETTAVOLT:HECTOVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 19)), Sym("zettav"))  # nopep8
    CONVERSIONS["ZETTAVOLT:KILOVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 18)), Sym("zettav"))  # nopep8
    CONVERSIONS["ZETTAVOLT:MEGAVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 15)), Sym("zettav"))  # nopep8
    CONVERSIONS["ZETTAVOLT:MICROVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 27)), Sym("zettav"))  # nopep8
    CONVERSIONS["ZETTAVOLT:MILLIVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 24)), Sym("zettav"))  # nopep8
    CONVERSIONS["ZETTAVOLT:NANOVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 30)), Sym("zettav"))  # nopep8
    CONVERSIONS["ZETTAVOLT:PETAVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("zettav"))  # nopep8
    CONVERSIONS["ZETTAVOLT:PICOVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 33)), Sym("zettav"))  # nopep8
    CONVERSIONS["ZETTAVOLT:TERAVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("zettav"))  # nopep8
    CONVERSIONS["ZETTAVOLT:VOLT"] = \
        Mul(Rat(Int(1), Pow(10, 21)), Sym("zettav"))  # nopep8
    CONVERSIONS["ZETTAVOLT:YOCTOVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 45)), Sym("zettav"))  # nopep8
    CONVERSIONS["ZETTAVOLT:YOTTAVOLT"] = \
        Mul(Int(1000), Sym("zettav"))  # nopep8
    CONVERSIONS["ZETTAVOLT:ZEPTOVOLT"] = \
        Mul(Rat(Int(1), Pow(10, 42)), Sym("zettav"))  # nopep8

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
