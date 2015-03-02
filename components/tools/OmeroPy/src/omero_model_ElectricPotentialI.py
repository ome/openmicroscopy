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

from omero.conversions import Add  # nopep8
from omero.conversions import Int  # nopep8
from omero.conversions import Mul  # nopep8
from omero.conversions import Pow  # nopep8
from omero.conversions import Rat  # nopep8
from omero.conversions import Sym  # nopep8


class ElectricPotentialI(_omero_model.ElectricPotential, UnitBase):

    try:
        UNIT_VALUES = sorted(UnitsElectricPotential._enumerators.values())
    except:
        # TODO: this occurs on Ice 3.4 and can be removed
        # once it has been dropped.
        UNIT_VALUES = [x for x in sorted(UnitsElectricPotential._names)]
        UNIT_VALUES = [getattr(UnitsElectricPotential, x) for x in UNIT_VALUES]
    CONVERSIONS = dict()
    for val in UNIT_VALUES:
        CONVERSIONS[val] = dict()
    CONVERSIONS[UnitsElectricPotential.ATTOVOLT][UnitsElectricPotential.CENTIVOLT] = \
        Mul(Pow(10, 16), Sym("attov"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.ATTOVOLT][UnitsElectricPotential.DECAVOLT] = \
        Mul(Pow(10, 19), Sym("attov"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.ATTOVOLT][UnitsElectricPotential.DECIVOLT] = \
        Mul(Pow(10, 17), Sym("attov"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.ATTOVOLT][UnitsElectricPotential.EXAVOLT] = \
        Mul(Pow(10, 36), Sym("attov"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.ATTOVOLT][UnitsElectricPotential.FEMTOVOLT] = \
        Mul(Int(1000), Sym("attov"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.ATTOVOLT][UnitsElectricPotential.GIGAVOLT] = \
        Mul(Pow(10, 27), Sym("attov"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.ATTOVOLT][UnitsElectricPotential.HECTOVOLT] = \
        Mul(Pow(10, 20), Sym("attov"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.ATTOVOLT][UnitsElectricPotential.KILOVOLT] = \
        Mul(Pow(10, 21), Sym("attov"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.ATTOVOLT][UnitsElectricPotential.MEGAVOLT] = \
        Mul(Pow(10, 24), Sym("attov"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.ATTOVOLT][UnitsElectricPotential.MICROVOLT] = \
        Mul(Pow(10, 12), Sym("attov"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.ATTOVOLT][UnitsElectricPotential.MILLIVOLT] = \
        Mul(Pow(10, 15), Sym("attov"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.ATTOVOLT][UnitsElectricPotential.NANOVOLT] = \
        Mul(Pow(10, 9), Sym("attov"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.ATTOVOLT][UnitsElectricPotential.PETAVOLT] = \
        Mul(Pow(10, 33), Sym("attov"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.ATTOVOLT][UnitsElectricPotential.PICOVOLT] = \
        Mul(Pow(10, 6), Sym("attov"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.ATTOVOLT][UnitsElectricPotential.TERAVOLT] = \
        Mul(Pow(10, 30), Sym("attov"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.ATTOVOLT][UnitsElectricPotential.VOLT] = \
        Mul(Pow(10, 18), Sym("attov"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.ATTOVOLT][UnitsElectricPotential.YOCTOVOLT] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("attov"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.ATTOVOLT][UnitsElectricPotential.YOTTAVOLT] = \
        Mul(Pow(10, 42), Sym("attov"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.ATTOVOLT][UnitsElectricPotential.ZEPTOVOLT] = \
        Mul(Rat(Int(1), Int(1000)), Sym("attov"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.ATTOVOLT][UnitsElectricPotential.ZETTAVOLT] = \
        Mul(Pow(10, 39), Sym("attov"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.CENTIVOLT][UnitsElectricPotential.ATTOVOLT] = \
        Mul(Rat(Int(1), Pow(10, 16)), Sym("centiv"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.CENTIVOLT][UnitsElectricPotential.DECAVOLT] = \
        Mul(Int(1000), Sym("centiv"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.CENTIVOLT][UnitsElectricPotential.DECIVOLT] = \
        Mul(Int(10), Sym("centiv"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.CENTIVOLT][UnitsElectricPotential.EXAVOLT] = \
        Mul(Pow(10, 20), Sym("centiv"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.CENTIVOLT][UnitsElectricPotential.FEMTOVOLT] = \
        Mul(Rat(Int(1), Pow(10, 13)), Sym("centiv"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.CENTIVOLT][UnitsElectricPotential.GIGAVOLT] = \
        Mul(Pow(10, 11), Sym("centiv"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.CENTIVOLT][UnitsElectricPotential.HECTOVOLT] = \
        Mul(Pow(10, 4), Sym("centiv"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.CENTIVOLT][UnitsElectricPotential.KILOVOLT] = \
        Mul(Pow(10, 5), Sym("centiv"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.CENTIVOLT][UnitsElectricPotential.MEGAVOLT] = \
        Mul(Pow(10, 8), Sym("centiv"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.CENTIVOLT][UnitsElectricPotential.MICROVOLT] = \
        Mul(Rat(Int(1), Pow(10, 4)), Sym("centiv"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.CENTIVOLT][UnitsElectricPotential.MILLIVOLT] = \
        Mul(Rat(Int(1), Int(10)), Sym("centiv"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.CENTIVOLT][UnitsElectricPotential.NANOVOLT] = \
        Mul(Rat(Int(1), Pow(10, 7)), Sym("centiv"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.CENTIVOLT][UnitsElectricPotential.PETAVOLT] = \
        Mul(Pow(10, 17), Sym("centiv"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.CENTIVOLT][UnitsElectricPotential.PICOVOLT] = \
        Mul(Rat(Int(1), Pow(10, 10)), Sym("centiv"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.CENTIVOLT][UnitsElectricPotential.TERAVOLT] = \
        Mul(Pow(10, 14), Sym("centiv"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.CENTIVOLT][UnitsElectricPotential.VOLT] = \
        Mul(Int(100), Sym("centiv"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.CENTIVOLT][UnitsElectricPotential.YOCTOVOLT] = \
        Mul(Rat(Int(1), Pow(10, 22)), Sym("centiv"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.CENTIVOLT][UnitsElectricPotential.YOTTAVOLT] = \
        Mul(Pow(10, 26), Sym("centiv"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.CENTIVOLT][UnitsElectricPotential.ZEPTOVOLT] = \
        Mul(Rat(Int(1), Pow(10, 19)), Sym("centiv"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.CENTIVOLT][UnitsElectricPotential.ZETTAVOLT] = \
        Mul(Pow(10, 23), Sym("centiv"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.DECAVOLT][UnitsElectricPotential.ATTOVOLT] = \
        Mul(Rat(Int(1), Pow(10, 19)), Sym("decav"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.DECAVOLT][UnitsElectricPotential.CENTIVOLT] = \
        Mul(Rat(Int(1), Int(1000)), Sym("decav"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.DECAVOLT][UnitsElectricPotential.DECIVOLT] = \
        Mul(Rat(Int(1), Int(100)), Sym("decav"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.DECAVOLT][UnitsElectricPotential.EXAVOLT] = \
        Mul(Pow(10, 17), Sym("decav"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.DECAVOLT][UnitsElectricPotential.FEMTOVOLT] = \
        Mul(Rat(Int(1), Pow(10, 16)), Sym("decav"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.DECAVOLT][UnitsElectricPotential.GIGAVOLT] = \
        Mul(Pow(10, 8), Sym("decav"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.DECAVOLT][UnitsElectricPotential.HECTOVOLT] = \
        Mul(Int(10), Sym("decav"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.DECAVOLT][UnitsElectricPotential.KILOVOLT] = \
        Mul(Int(100), Sym("decav"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.DECAVOLT][UnitsElectricPotential.MEGAVOLT] = \
        Mul(Pow(10, 5), Sym("decav"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.DECAVOLT][UnitsElectricPotential.MICROVOLT] = \
        Mul(Rat(Int(1), Pow(10, 7)), Sym("decav"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.DECAVOLT][UnitsElectricPotential.MILLIVOLT] = \
        Mul(Rat(Int(1), Pow(10, 4)), Sym("decav"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.DECAVOLT][UnitsElectricPotential.NANOVOLT] = \
        Mul(Rat(Int(1), Pow(10, 10)), Sym("decav"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.DECAVOLT][UnitsElectricPotential.PETAVOLT] = \
        Mul(Pow(10, 14), Sym("decav"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.DECAVOLT][UnitsElectricPotential.PICOVOLT] = \
        Mul(Rat(Int(1), Pow(10, 13)), Sym("decav"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.DECAVOLT][UnitsElectricPotential.TERAVOLT] = \
        Mul(Pow(10, 11), Sym("decav"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.DECAVOLT][UnitsElectricPotential.VOLT] = \
        Mul(Rat(Int(1), Int(10)), Sym("decav"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.DECAVOLT][UnitsElectricPotential.YOCTOVOLT] = \
        Mul(Rat(Int(1), Pow(10, 25)), Sym("decav"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.DECAVOLT][UnitsElectricPotential.YOTTAVOLT] = \
        Mul(Pow(10, 23), Sym("decav"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.DECAVOLT][UnitsElectricPotential.ZEPTOVOLT] = \
        Mul(Rat(Int(1), Pow(10, 22)), Sym("decav"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.DECAVOLT][UnitsElectricPotential.ZETTAVOLT] = \
        Mul(Pow(10, 20), Sym("decav"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.DECIVOLT][UnitsElectricPotential.ATTOVOLT] = \
        Mul(Rat(Int(1), Pow(10, 17)), Sym("deciv"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.DECIVOLT][UnitsElectricPotential.CENTIVOLT] = \
        Mul(Rat(Int(1), Int(10)), Sym("deciv"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.DECIVOLT][UnitsElectricPotential.DECAVOLT] = \
        Mul(Int(100), Sym("deciv"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.DECIVOLT][UnitsElectricPotential.EXAVOLT] = \
        Mul(Pow(10, 19), Sym("deciv"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.DECIVOLT][UnitsElectricPotential.FEMTOVOLT] = \
        Mul(Rat(Int(1), Pow(10, 14)), Sym("deciv"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.DECIVOLT][UnitsElectricPotential.GIGAVOLT] = \
        Mul(Pow(10, 10), Sym("deciv"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.DECIVOLT][UnitsElectricPotential.HECTOVOLT] = \
        Mul(Int(1000), Sym("deciv"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.DECIVOLT][UnitsElectricPotential.KILOVOLT] = \
        Mul(Pow(10, 4), Sym("deciv"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.DECIVOLT][UnitsElectricPotential.MEGAVOLT] = \
        Mul(Pow(10, 7), Sym("deciv"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.DECIVOLT][UnitsElectricPotential.MICROVOLT] = \
        Mul(Rat(Int(1), Pow(10, 5)), Sym("deciv"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.DECIVOLT][UnitsElectricPotential.MILLIVOLT] = \
        Mul(Rat(Int(1), Int(100)), Sym("deciv"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.DECIVOLT][UnitsElectricPotential.NANOVOLT] = \
        Mul(Rat(Int(1), Pow(10, 8)), Sym("deciv"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.DECIVOLT][UnitsElectricPotential.PETAVOLT] = \
        Mul(Pow(10, 16), Sym("deciv"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.DECIVOLT][UnitsElectricPotential.PICOVOLT] = \
        Mul(Rat(Int(1), Pow(10, 11)), Sym("deciv"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.DECIVOLT][UnitsElectricPotential.TERAVOLT] = \
        Mul(Pow(10, 13), Sym("deciv"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.DECIVOLT][UnitsElectricPotential.VOLT] = \
        Mul(Int(10), Sym("deciv"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.DECIVOLT][UnitsElectricPotential.YOCTOVOLT] = \
        Mul(Rat(Int(1), Pow(10, 23)), Sym("deciv"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.DECIVOLT][UnitsElectricPotential.YOTTAVOLT] = \
        Mul(Pow(10, 25), Sym("deciv"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.DECIVOLT][UnitsElectricPotential.ZEPTOVOLT] = \
        Mul(Rat(Int(1), Pow(10, 20)), Sym("deciv"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.DECIVOLT][UnitsElectricPotential.ZETTAVOLT] = \
        Mul(Pow(10, 22), Sym("deciv"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.EXAVOLT][UnitsElectricPotential.ATTOVOLT] = \
        Mul(Rat(Int(1), Pow(10, 36)), Sym("exav"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.EXAVOLT][UnitsElectricPotential.CENTIVOLT] = \
        Mul(Rat(Int(1), Pow(10, 20)), Sym("exav"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.EXAVOLT][UnitsElectricPotential.DECAVOLT] = \
        Mul(Rat(Int(1), Pow(10, 17)), Sym("exav"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.EXAVOLT][UnitsElectricPotential.DECIVOLT] = \
        Mul(Rat(Int(1), Pow(10, 19)), Sym("exav"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.EXAVOLT][UnitsElectricPotential.FEMTOVOLT] = \
        Mul(Rat(Int(1), Pow(10, 33)), Sym("exav"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.EXAVOLT][UnitsElectricPotential.GIGAVOLT] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("exav"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.EXAVOLT][UnitsElectricPotential.HECTOVOLT] = \
        Mul(Rat(Int(1), Pow(10, 16)), Sym("exav"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.EXAVOLT][UnitsElectricPotential.KILOVOLT] = \
        Mul(Rat(Int(1), Pow(10, 15)), Sym("exav"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.EXAVOLT][UnitsElectricPotential.MEGAVOLT] = \
        Mul(Rat(Int(1), Pow(10, 12)), Sym("exav"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.EXAVOLT][UnitsElectricPotential.MICROVOLT] = \
        Mul(Rat(Int(1), Pow(10, 24)), Sym("exav"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.EXAVOLT][UnitsElectricPotential.MILLIVOLT] = \
        Mul(Rat(Int(1), Pow(10, 21)), Sym("exav"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.EXAVOLT][UnitsElectricPotential.NANOVOLT] = \
        Mul(Rat(Int(1), Pow(10, 27)), Sym("exav"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.EXAVOLT][UnitsElectricPotential.PETAVOLT] = \
        Mul(Rat(Int(1), Int(1000)), Sym("exav"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.EXAVOLT][UnitsElectricPotential.PICOVOLT] = \
        Mul(Rat(Int(1), Pow(10, 30)), Sym("exav"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.EXAVOLT][UnitsElectricPotential.TERAVOLT] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("exav"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.EXAVOLT][UnitsElectricPotential.VOLT] = \
        Mul(Rat(Int(1), Pow(10, 18)), Sym("exav"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.EXAVOLT][UnitsElectricPotential.YOCTOVOLT] = \
        Mul(Rat(Int(1), Pow(10, 42)), Sym("exav"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.EXAVOLT][UnitsElectricPotential.YOTTAVOLT] = \
        Mul(Pow(10, 6), Sym("exav"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.EXAVOLT][UnitsElectricPotential.ZEPTOVOLT] = \
        Mul(Rat(Int(1), Pow(10, 39)), Sym("exav"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.EXAVOLT][UnitsElectricPotential.ZETTAVOLT] = \
        Mul(Int(1000), Sym("exav"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.FEMTOVOLT][UnitsElectricPotential.ATTOVOLT] = \
        Mul(Rat(Int(1), Int(1000)), Sym("femtov"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.FEMTOVOLT][UnitsElectricPotential.CENTIVOLT] = \
        Mul(Pow(10, 13), Sym("femtov"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.FEMTOVOLT][UnitsElectricPotential.DECAVOLT] = \
        Mul(Pow(10, 16), Sym("femtov"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.FEMTOVOLT][UnitsElectricPotential.DECIVOLT] = \
        Mul(Pow(10, 14), Sym("femtov"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.FEMTOVOLT][UnitsElectricPotential.EXAVOLT] = \
        Mul(Pow(10, 33), Sym("femtov"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.FEMTOVOLT][UnitsElectricPotential.GIGAVOLT] = \
        Mul(Pow(10, 24), Sym("femtov"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.FEMTOVOLT][UnitsElectricPotential.HECTOVOLT] = \
        Mul(Pow(10, 17), Sym("femtov"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.FEMTOVOLT][UnitsElectricPotential.KILOVOLT] = \
        Mul(Pow(10, 18), Sym("femtov"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.FEMTOVOLT][UnitsElectricPotential.MEGAVOLT] = \
        Mul(Pow(10, 21), Sym("femtov"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.FEMTOVOLT][UnitsElectricPotential.MICROVOLT] = \
        Mul(Pow(10, 9), Sym("femtov"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.FEMTOVOLT][UnitsElectricPotential.MILLIVOLT] = \
        Mul(Pow(10, 12), Sym("femtov"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.FEMTOVOLT][UnitsElectricPotential.NANOVOLT] = \
        Mul(Pow(10, 6), Sym("femtov"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.FEMTOVOLT][UnitsElectricPotential.PETAVOLT] = \
        Mul(Pow(10, 30), Sym("femtov"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.FEMTOVOLT][UnitsElectricPotential.PICOVOLT] = \
        Mul(Int(1000), Sym("femtov"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.FEMTOVOLT][UnitsElectricPotential.TERAVOLT] = \
        Mul(Pow(10, 27), Sym("femtov"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.FEMTOVOLT][UnitsElectricPotential.VOLT] = \
        Mul(Pow(10, 15), Sym("femtov"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.FEMTOVOLT][UnitsElectricPotential.YOCTOVOLT] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("femtov"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.FEMTOVOLT][UnitsElectricPotential.YOTTAVOLT] = \
        Mul(Pow(10, 39), Sym("femtov"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.FEMTOVOLT][UnitsElectricPotential.ZEPTOVOLT] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("femtov"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.FEMTOVOLT][UnitsElectricPotential.ZETTAVOLT] = \
        Mul(Pow(10, 36), Sym("femtov"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.GIGAVOLT][UnitsElectricPotential.ATTOVOLT] = \
        Mul(Rat(Int(1), Pow(10, 27)), Sym("gigav"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.GIGAVOLT][UnitsElectricPotential.CENTIVOLT] = \
        Mul(Rat(Int(1), Pow(10, 11)), Sym("gigav"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.GIGAVOLT][UnitsElectricPotential.DECAVOLT] = \
        Mul(Rat(Int(1), Pow(10, 8)), Sym("gigav"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.GIGAVOLT][UnitsElectricPotential.DECIVOLT] = \
        Mul(Rat(Int(1), Pow(10, 10)), Sym("gigav"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.GIGAVOLT][UnitsElectricPotential.EXAVOLT] = \
        Mul(Pow(10, 9), Sym("gigav"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.GIGAVOLT][UnitsElectricPotential.FEMTOVOLT] = \
        Mul(Rat(Int(1), Pow(10, 24)), Sym("gigav"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.GIGAVOLT][UnitsElectricPotential.HECTOVOLT] = \
        Mul(Rat(Int(1), Pow(10, 7)), Sym("gigav"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.GIGAVOLT][UnitsElectricPotential.KILOVOLT] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("gigav"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.GIGAVOLT][UnitsElectricPotential.MEGAVOLT] = \
        Mul(Rat(Int(1), Int(1000)), Sym("gigav"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.GIGAVOLT][UnitsElectricPotential.MICROVOLT] = \
        Mul(Rat(Int(1), Pow(10, 15)), Sym("gigav"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.GIGAVOLT][UnitsElectricPotential.MILLIVOLT] = \
        Mul(Rat(Int(1), Pow(10, 12)), Sym("gigav"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.GIGAVOLT][UnitsElectricPotential.NANOVOLT] = \
        Mul(Rat(Int(1), Pow(10, 18)), Sym("gigav"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.GIGAVOLT][UnitsElectricPotential.PETAVOLT] = \
        Mul(Pow(10, 6), Sym("gigav"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.GIGAVOLT][UnitsElectricPotential.PICOVOLT] = \
        Mul(Rat(Int(1), Pow(10, 21)), Sym("gigav"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.GIGAVOLT][UnitsElectricPotential.TERAVOLT] = \
        Mul(Int(1000), Sym("gigav"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.GIGAVOLT][UnitsElectricPotential.VOLT] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("gigav"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.GIGAVOLT][UnitsElectricPotential.YOCTOVOLT] = \
        Mul(Rat(Int(1), Pow(10, 33)), Sym("gigav"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.GIGAVOLT][UnitsElectricPotential.YOTTAVOLT] = \
        Mul(Pow(10, 15), Sym("gigav"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.GIGAVOLT][UnitsElectricPotential.ZEPTOVOLT] = \
        Mul(Rat(Int(1), Pow(10, 30)), Sym("gigav"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.GIGAVOLT][UnitsElectricPotential.ZETTAVOLT] = \
        Mul(Pow(10, 12), Sym("gigav"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.HECTOVOLT][UnitsElectricPotential.ATTOVOLT] = \
        Mul(Rat(Int(1), Pow(10, 20)), Sym("hectov"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.HECTOVOLT][UnitsElectricPotential.CENTIVOLT] = \
        Mul(Rat(Int(1), Pow(10, 4)), Sym("hectov"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.HECTOVOLT][UnitsElectricPotential.DECAVOLT] = \
        Mul(Rat(Int(1), Int(10)), Sym("hectov"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.HECTOVOLT][UnitsElectricPotential.DECIVOLT] = \
        Mul(Rat(Int(1), Int(1000)), Sym("hectov"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.HECTOVOLT][UnitsElectricPotential.EXAVOLT] = \
        Mul(Pow(10, 16), Sym("hectov"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.HECTOVOLT][UnitsElectricPotential.FEMTOVOLT] = \
        Mul(Rat(Int(1), Pow(10, 17)), Sym("hectov"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.HECTOVOLT][UnitsElectricPotential.GIGAVOLT] = \
        Mul(Pow(10, 7), Sym("hectov"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.HECTOVOLT][UnitsElectricPotential.KILOVOLT] = \
        Mul(Int(10), Sym("hectov"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.HECTOVOLT][UnitsElectricPotential.MEGAVOLT] = \
        Mul(Pow(10, 4), Sym("hectov"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.HECTOVOLT][UnitsElectricPotential.MICROVOLT] = \
        Mul(Rat(Int(1), Pow(10, 8)), Sym("hectov"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.HECTOVOLT][UnitsElectricPotential.MILLIVOLT] = \
        Mul(Rat(Int(1), Pow(10, 5)), Sym("hectov"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.HECTOVOLT][UnitsElectricPotential.NANOVOLT] = \
        Mul(Rat(Int(1), Pow(10, 11)), Sym("hectov"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.HECTOVOLT][UnitsElectricPotential.PETAVOLT] = \
        Mul(Pow(10, 13), Sym("hectov"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.HECTOVOLT][UnitsElectricPotential.PICOVOLT] = \
        Mul(Rat(Int(1), Pow(10, 14)), Sym("hectov"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.HECTOVOLT][UnitsElectricPotential.TERAVOLT] = \
        Mul(Pow(10, 10), Sym("hectov"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.HECTOVOLT][UnitsElectricPotential.VOLT] = \
        Mul(Rat(Int(1), Int(100)), Sym("hectov"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.HECTOVOLT][UnitsElectricPotential.YOCTOVOLT] = \
        Mul(Rat(Int(1), Pow(10, 26)), Sym("hectov"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.HECTOVOLT][UnitsElectricPotential.YOTTAVOLT] = \
        Mul(Pow(10, 22), Sym("hectov"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.HECTOVOLT][UnitsElectricPotential.ZEPTOVOLT] = \
        Mul(Rat(Int(1), Pow(10, 23)), Sym("hectov"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.HECTOVOLT][UnitsElectricPotential.ZETTAVOLT] = \
        Mul(Pow(10, 19), Sym("hectov"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.KILOVOLT][UnitsElectricPotential.ATTOVOLT] = \
        Mul(Rat(Int(1), Pow(10, 21)), Sym("kilov"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.KILOVOLT][UnitsElectricPotential.CENTIVOLT] = \
        Mul(Rat(Int(1), Pow(10, 5)), Sym("kilov"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.KILOVOLT][UnitsElectricPotential.DECAVOLT] = \
        Mul(Rat(Int(1), Int(100)), Sym("kilov"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.KILOVOLT][UnitsElectricPotential.DECIVOLT] = \
        Mul(Rat(Int(1), Pow(10, 4)), Sym("kilov"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.KILOVOLT][UnitsElectricPotential.EXAVOLT] = \
        Mul(Pow(10, 15), Sym("kilov"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.KILOVOLT][UnitsElectricPotential.FEMTOVOLT] = \
        Mul(Rat(Int(1), Pow(10, 18)), Sym("kilov"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.KILOVOLT][UnitsElectricPotential.GIGAVOLT] = \
        Mul(Pow(10, 6), Sym("kilov"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.KILOVOLT][UnitsElectricPotential.HECTOVOLT] = \
        Mul(Rat(Int(1), Int(10)), Sym("kilov"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.KILOVOLT][UnitsElectricPotential.MEGAVOLT] = \
        Mul(Int(1000), Sym("kilov"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.KILOVOLT][UnitsElectricPotential.MICROVOLT] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("kilov"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.KILOVOLT][UnitsElectricPotential.MILLIVOLT] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("kilov"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.KILOVOLT][UnitsElectricPotential.NANOVOLT] = \
        Mul(Rat(Int(1), Pow(10, 12)), Sym("kilov"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.KILOVOLT][UnitsElectricPotential.PETAVOLT] = \
        Mul(Pow(10, 12), Sym("kilov"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.KILOVOLT][UnitsElectricPotential.PICOVOLT] = \
        Mul(Rat(Int(1), Pow(10, 15)), Sym("kilov"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.KILOVOLT][UnitsElectricPotential.TERAVOLT] = \
        Mul(Pow(10, 9), Sym("kilov"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.KILOVOLT][UnitsElectricPotential.VOLT] = \
        Mul(Rat(Int(1), Int(1000)), Sym("kilov"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.KILOVOLT][UnitsElectricPotential.YOCTOVOLT] = \
        Mul(Rat(Int(1), Pow(10, 27)), Sym("kilov"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.KILOVOLT][UnitsElectricPotential.YOTTAVOLT] = \
        Mul(Pow(10, 21), Sym("kilov"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.KILOVOLT][UnitsElectricPotential.ZEPTOVOLT] = \
        Mul(Rat(Int(1), Pow(10, 24)), Sym("kilov"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.KILOVOLT][UnitsElectricPotential.ZETTAVOLT] = \
        Mul(Pow(10, 18), Sym("kilov"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.MEGAVOLT][UnitsElectricPotential.ATTOVOLT] = \
        Mul(Rat(Int(1), Pow(10, 24)), Sym("megav"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.MEGAVOLT][UnitsElectricPotential.CENTIVOLT] = \
        Mul(Rat(Int(1), Pow(10, 8)), Sym("megav"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.MEGAVOLT][UnitsElectricPotential.DECAVOLT] = \
        Mul(Rat(Int(1), Pow(10, 5)), Sym("megav"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.MEGAVOLT][UnitsElectricPotential.DECIVOLT] = \
        Mul(Rat(Int(1), Pow(10, 7)), Sym("megav"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.MEGAVOLT][UnitsElectricPotential.EXAVOLT] = \
        Mul(Pow(10, 12), Sym("megav"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.MEGAVOLT][UnitsElectricPotential.FEMTOVOLT] = \
        Mul(Rat(Int(1), Pow(10, 21)), Sym("megav"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.MEGAVOLT][UnitsElectricPotential.GIGAVOLT] = \
        Mul(Int(1000), Sym("megav"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.MEGAVOLT][UnitsElectricPotential.HECTOVOLT] = \
        Mul(Rat(Int(1), Pow(10, 4)), Sym("megav"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.MEGAVOLT][UnitsElectricPotential.KILOVOLT] = \
        Mul(Rat(Int(1), Int(1000)), Sym("megav"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.MEGAVOLT][UnitsElectricPotential.MICROVOLT] = \
        Mul(Rat(Int(1), Pow(10, 12)), Sym("megav"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.MEGAVOLT][UnitsElectricPotential.MILLIVOLT] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("megav"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.MEGAVOLT][UnitsElectricPotential.NANOVOLT] = \
        Mul(Rat(Int(1), Pow(10, 15)), Sym("megav"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.MEGAVOLT][UnitsElectricPotential.PETAVOLT] = \
        Mul(Pow(10, 9), Sym("megav"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.MEGAVOLT][UnitsElectricPotential.PICOVOLT] = \
        Mul(Rat(Int(1), Pow(10, 18)), Sym("megav"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.MEGAVOLT][UnitsElectricPotential.TERAVOLT] = \
        Mul(Pow(10, 6), Sym("megav"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.MEGAVOLT][UnitsElectricPotential.VOLT] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("megav"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.MEGAVOLT][UnitsElectricPotential.YOCTOVOLT] = \
        Mul(Rat(Int(1), Pow(10, 30)), Sym("megav"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.MEGAVOLT][UnitsElectricPotential.YOTTAVOLT] = \
        Mul(Pow(10, 18), Sym("megav"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.MEGAVOLT][UnitsElectricPotential.ZEPTOVOLT] = \
        Mul(Rat(Int(1), Pow(10, 27)), Sym("megav"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.MEGAVOLT][UnitsElectricPotential.ZETTAVOLT] = \
        Mul(Pow(10, 15), Sym("megav"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.MICROVOLT][UnitsElectricPotential.ATTOVOLT] = \
        Mul(Rat(Int(1), Pow(10, 12)), Sym("microv"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.MICROVOLT][UnitsElectricPotential.CENTIVOLT] = \
        Mul(Pow(10, 4), Sym("microv"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.MICROVOLT][UnitsElectricPotential.DECAVOLT] = \
        Mul(Pow(10, 7), Sym("microv"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.MICROVOLT][UnitsElectricPotential.DECIVOLT] = \
        Mul(Pow(10, 5), Sym("microv"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.MICROVOLT][UnitsElectricPotential.EXAVOLT] = \
        Mul(Pow(10, 24), Sym("microv"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.MICROVOLT][UnitsElectricPotential.FEMTOVOLT] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("microv"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.MICROVOLT][UnitsElectricPotential.GIGAVOLT] = \
        Mul(Pow(10, 15), Sym("microv"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.MICROVOLT][UnitsElectricPotential.HECTOVOLT] = \
        Mul(Pow(10, 8), Sym("microv"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.MICROVOLT][UnitsElectricPotential.KILOVOLT] = \
        Mul(Pow(10, 9), Sym("microv"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.MICROVOLT][UnitsElectricPotential.MEGAVOLT] = \
        Mul(Pow(10, 12), Sym("microv"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.MICROVOLT][UnitsElectricPotential.MILLIVOLT] = \
        Mul(Int(1000), Sym("microv"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.MICROVOLT][UnitsElectricPotential.NANOVOLT] = \
        Mul(Rat(Int(1), Int(1000)), Sym("microv"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.MICROVOLT][UnitsElectricPotential.PETAVOLT] = \
        Mul(Pow(10, 21), Sym("microv"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.MICROVOLT][UnitsElectricPotential.PICOVOLT] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("microv"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.MICROVOLT][UnitsElectricPotential.TERAVOLT] = \
        Mul(Pow(10, 18), Sym("microv"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.MICROVOLT][UnitsElectricPotential.VOLT] = \
        Mul(Pow(10, 6), Sym("microv"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.MICROVOLT][UnitsElectricPotential.YOCTOVOLT] = \
        Mul(Rat(Int(1), Pow(10, 18)), Sym("microv"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.MICROVOLT][UnitsElectricPotential.YOTTAVOLT] = \
        Mul(Pow(10, 30), Sym("microv"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.MICROVOLT][UnitsElectricPotential.ZEPTOVOLT] = \
        Mul(Rat(Int(1), Pow(10, 15)), Sym("microv"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.MICROVOLT][UnitsElectricPotential.ZETTAVOLT] = \
        Mul(Pow(10, 27), Sym("microv"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.MILLIVOLT][UnitsElectricPotential.ATTOVOLT] = \
        Mul(Rat(Int(1), Pow(10, 15)), Sym("milliv"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.MILLIVOLT][UnitsElectricPotential.CENTIVOLT] = \
        Mul(Int(10), Sym("milliv"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.MILLIVOLT][UnitsElectricPotential.DECAVOLT] = \
        Mul(Pow(10, 4), Sym("milliv"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.MILLIVOLT][UnitsElectricPotential.DECIVOLT] = \
        Mul(Int(100), Sym("milliv"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.MILLIVOLT][UnitsElectricPotential.EXAVOLT] = \
        Mul(Pow(10, 21), Sym("milliv"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.MILLIVOLT][UnitsElectricPotential.FEMTOVOLT] = \
        Mul(Rat(Int(1), Pow(10, 12)), Sym("milliv"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.MILLIVOLT][UnitsElectricPotential.GIGAVOLT] = \
        Mul(Pow(10, 12), Sym("milliv"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.MILLIVOLT][UnitsElectricPotential.HECTOVOLT] = \
        Mul(Pow(10, 5), Sym("milliv"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.MILLIVOLT][UnitsElectricPotential.KILOVOLT] = \
        Mul(Pow(10, 6), Sym("milliv"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.MILLIVOLT][UnitsElectricPotential.MEGAVOLT] = \
        Mul(Pow(10, 9), Sym("milliv"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.MILLIVOLT][UnitsElectricPotential.MICROVOLT] = \
        Mul(Rat(Int(1), Int(1000)), Sym("milliv"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.MILLIVOLT][UnitsElectricPotential.NANOVOLT] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("milliv"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.MILLIVOLT][UnitsElectricPotential.PETAVOLT] = \
        Mul(Pow(10, 18), Sym("milliv"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.MILLIVOLT][UnitsElectricPotential.PICOVOLT] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("milliv"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.MILLIVOLT][UnitsElectricPotential.TERAVOLT] = \
        Mul(Pow(10, 15), Sym("milliv"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.MILLIVOLT][UnitsElectricPotential.VOLT] = \
        Mul(Int(1000), Sym("milliv"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.MILLIVOLT][UnitsElectricPotential.YOCTOVOLT] = \
        Mul(Rat(Int(1), Pow(10, 21)), Sym("milliv"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.MILLIVOLT][UnitsElectricPotential.YOTTAVOLT] = \
        Mul(Pow(10, 27), Sym("milliv"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.MILLIVOLT][UnitsElectricPotential.ZEPTOVOLT] = \
        Mul(Rat(Int(1), Pow(10, 18)), Sym("milliv"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.MILLIVOLT][UnitsElectricPotential.ZETTAVOLT] = \
        Mul(Pow(10, 24), Sym("milliv"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.NANOVOLT][UnitsElectricPotential.ATTOVOLT] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("nanov"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.NANOVOLT][UnitsElectricPotential.CENTIVOLT] = \
        Mul(Pow(10, 7), Sym("nanov"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.NANOVOLT][UnitsElectricPotential.DECAVOLT] = \
        Mul(Pow(10, 10), Sym("nanov"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.NANOVOLT][UnitsElectricPotential.DECIVOLT] = \
        Mul(Pow(10, 8), Sym("nanov"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.NANOVOLT][UnitsElectricPotential.EXAVOLT] = \
        Mul(Pow(10, 27), Sym("nanov"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.NANOVOLT][UnitsElectricPotential.FEMTOVOLT] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("nanov"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.NANOVOLT][UnitsElectricPotential.GIGAVOLT] = \
        Mul(Pow(10, 18), Sym("nanov"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.NANOVOLT][UnitsElectricPotential.HECTOVOLT] = \
        Mul(Pow(10, 11), Sym("nanov"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.NANOVOLT][UnitsElectricPotential.KILOVOLT] = \
        Mul(Pow(10, 12), Sym("nanov"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.NANOVOLT][UnitsElectricPotential.MEGAVOLT] = \
        Mul(Pow(10, 15), Sym("nanov"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.NANOVOLT][UnitsElectricPotential.MICROVOLT] = \
        Mul(Int(1000), Sym("nanov"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.NANOVOLT][UnitsElectricPotential.MILLIVOLT] = \
        Mul(Pow(10, 6), Sym("nanov"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.NANOVOLT][UnitsElectricPotential.PETAVOLT] = \
        Mul(Pow(10, 24), Sym("nanov"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.NANOVOLT][UnitsElectricPotential.PICOVOLT] = \
        Mul(Rat(Int(1), Int(1000)), Sym("nanov"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.NANOVOLT][UnitsElectricPotential.TERAVOLT] = \
        Mul(Pow(10, 21), Sym("nanov"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.NANOVOLT][UnitsElectricPotential.VOLT] = \
        Mul(Pow(10, 9), Sym("nanov"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.NANOVOLT][UnitsElectricPotential.YOCTOVOLT] = \
        Mul(Rat(Int(1), Pow(10, 15)), Sym("nanov"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.NANOVOLT][UnitsElectricPotential.YOTTAVOLT] = \
        Mul(Pow(10, 33), Sym("nanov"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.NANOVOLT][UnitsElectricPotential.ZEPTOVOLT] = \
        Mul(Rat(Int(1), Pow(10, 12)), Sym("nanov"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.NANOVOLT][UnitsElectricPotential.ZETTAVOLT] = \
        Mul(Pow(10, 30), Sym("nanov"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.PETAVOLT][UnitsElectricPotential.ATTOVOLT] = \
        Mul(Rat(Int(1), Pow(10, 33)), Sym("petav"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.PETAVOLT][UnitsElectricPotential.CENTIVOLT] = \
        Mul(Rat(Int(1), Pow(10, 17)), Sym("petav"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.PETAVOLT][UnitsElectricPotential.DECAVOLT] = \
        Mul(Rat(Int(1), Pow(10, 14)), Sym("petav"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.PETAVOLT][UnitsElectricPotential.DECIVOLT] = \
        Mul(Rat(Int(1), Pow(10, 16)), Sym("petav"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.PETAVOLT][UnitsElectricPotential.EXAVOLT] = \
        Mul(Int(1000), Sym("petav"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.PETAVOLT][UnitsElectricPotential.FEMTOVOLT] = \
        Mul(Rat(Int(1), Pow(10, 30)), Sym("petav"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.PETAVOLT][UnitsElectricPotential.GIGAVOLT] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("petav"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.PETAVOLT][UnitsElectricPotential.HECTOVOLT] = \
        Mul(Rat(Int(1), Pow(10, 13)), Sym("petav"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.PETAVOLT][UnitsElectricPotential.KILOVOLT] = \
        Mul(Rat(Int(1), Pow(10, 12)), Sym("petav"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.PETAVOLT][UnitsElectricPotential.MEGAVOLT] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("petav"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.PETAVOLT][UnitsElectricPotential.MICROVOLT] = \
        Mul(Rat(Int(1), Pow(10, 21)), Sym("petav"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.PETAVOLT][UnitsElectricPotential.MILLIVOLT] = \
        Mul(Rat(Int(1), Pow(10, 18)), Sym("petav"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.PETAVOLT][UnitsElectricPotential.NANOVOLT] = \
        Mul(Rat(Int(1), Pow(10, 24)), Sym("petav"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.PETAVOLT][UnitsElectricPotential.PICOVOLT] = \
        Mul(Rat(Int(1), Pow(10, 27)), Sym("petav"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.PETAVOLT][UnitsElectricPotential.TERAVOLT] = \
        Mul(Rat(Int(1), Int(1000)), Sym("petav"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.PETAVOLT][UnitsElectricPotential.VOLT] = \
        Mul(Rat(Int(1), Pow(10, 15)), Sym("petav"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.PETAVOLT][UnitsElectricPotential.YOCTOVOLT] = \
        Mul(Rat(Int(1), Pow(10, 39)), Sym("petav"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.PETAVOLT][UnitsElectricPotential.YOTTAVOLT] = \
        Mul(Pow(10, 9), Sym("petav"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.PETAVOLT][UnitsElectricPotential.ZEPTOVOLT] = \
        Mul(Rat(Int(1), Pow(10, 36)), Sym("petav"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.PETAVOLT][UnitsElectricPotential.ZETTAVOLT] = \
        Mul(Pow(10, 6), Sym("petav"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.PICOVOLT][UnitsElectricPotential.ATTOVOLT] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("picov"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.PICOVOLT][UnitsElectricPotential.CENTIVOLT] = \
        Mul(Pow(10, 10), Sym("picov"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.PICOVOLT][UnitsElectricPotential.DECAVOLT] = \
        Mul(Pow(10, 13), Sym("picov"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.PICOVOLT][UnitsElectricPotential.DECIVOLT] = \
        Mul(Pow(10, 11), Sym("picov"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.PICOVOLT][UnitsElectricPotential.EXAVOLT] = \
        Mul(Pow(10, 30), Sym("picov"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.PICOVOLT][UnitsElectricPotential.FEMTOVOLT] = \
        Mul(Rat(Int(1), Int(1000)), Sym("picov"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.PICOVOLT][UnitsElectricPotential.GIGAVOLT] = \
        Mul(Pow(10, 21), Sym("picov"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.PICOVOLT][UnitsElectricPotential.HECTOVOLT] = \
        Mul(Pow(10, 14), Sym("picov"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.PICOVOLT][UnitsElectricPotential.KILOVOLT] = \
        Mul(Pow(10, 15), Sym("picov"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.PICOVOLT][UnitsElectricPotential.MEGAVOLT] = \
        Mul(Pow(10, 18), Sym("picov"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.PICOVOLT][UnitsElectricPotential.MICROVOLT] = \
        Mul(Pow(10, 6), Sym("picov"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.PICOVOLT][UnitsElectricPotential.MILLIVOLT] = \
        Mul(Pow(10, 9), Sym("picov"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.PICOVOLT][UnitsElectricPotential.NANOVOLT] = \
        Mul(Int(1000), Sym("picov"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.PICOVOLT][UnitsElectricPotential.PETAVOLT] = \
        Mul(Pow(10, 27), Sym("picov"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.PICOVOLT][UnitsElectricPotential.TERAVOLT] = \
        Mul(Pow(10, 24), Sym("picov"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.PICOVOLT][UnitsElectricPotential.VOLT] = \
        Mul(Pow(10, 12), Sym("picov"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.PICOVOLT][UnitsElectricPotential.YOCTOVOLT] = \
        Mul(Rat(Int(1), Pow(10, 12)), Sym("picov"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.PICOVOLT][UnitsElectricPotential.YOTTAVOLT] = \
        Mul(Pow(10, 36), Sym("picov"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.PICOVOLT][UnitsElectricPotential.ZEPTOVOLT] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("picov"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.PICOVOLT][UnitsElectricPotential.ZETTAVOLT] = \
        Mul(Pow(10, 33), Sym("picov"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.TERAVOLT][UnitsElectricPotential.ATTOVOLT] = \
        Mul(Rat(Int(1), Pow(10, 30)), Sym("terav"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.TERAVOLT][UnitsElectricPotential.CENTIVOLT] = \
        Mul(Rat(Int(1), Pow(10, 14)), Sym("terav"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.TERAVOLT][UnitsElectricPotential.DECAVOLT] = \
        Mul(Rat(Int(1), Pow(10, 11)), Sym("terav"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.TERAVOLT][UnitsElectricPotential.DECIVOLT] = \
        Mul(Rat(Int(1), Pow(10, 13)), Sym("terav"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.TERAVOLT][UnitsElectricPotential.EXAVOLT] = \
        Mul(Pow(10, 6), Sym("terav"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.TERAVOLT][UnitsElectricPotential.FEMTOVOLT] = \
        Mul(Rat(Int(1), Pow(10, 27)), Sym("terav"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.TERAVOLT][UnitsElectricPotential.GIGAVOLT] = \
        Mul(Rat(Int(1), Int(1000)), Sym("terav"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.TERAVOLT][UnitsElectricPotential.HECTOVOLT] = \
        Mul(Rat(Int(1), Pow(10, 10)), Sym("terav"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.TERAVOLT][UnitsElectricPotential.KILOVOLT] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("terav"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.TERAVOLT][UnitsElectricPotential.MEGAVOLT] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("terav"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.TERAVOLT][UnitsElectricPotential.MICROVOLT] = \
        Mul(Rat(Int(1), Pow(10, 18)), Sym("terav"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.TERAVOLT][UnitsElectricPotential.MILLIVOLT] = \
        Mul(Rat(Int(1), Pow(10, 15)), Sym("terav"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.TERAVOLT][UnitsElectricPotential.NANOVOLT] = \
        Mul(Rat(Int(1), Pow(10, 21)), Sym("terav"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.TERAVOLT][UnitsElectricPotential.PETAVOLT] = \
        Mul(Int(1000), Sym("terav"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.TERAVOLT][UnitsElectricPotential.PICOVOLT] = \
        Mul(Rat(Int(1), Pow(10, 24)), Sym("terav"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.TERAVOLT][UnitsElectricPotential.VOLT] = \
        Mul(Rat(Int(1), Pow(10, 12)), Sym("terav"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.TERAVOLT][UnitsElectricPotential.YOCTOVOLT] = \
        Mul(Rat(Int(1), Pow(10, 36)), Sym("terav"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.TERAVOLT][UnitsElectricPotential.YOTTAVOLT] = \
        Mul(Pow(10, 12), Sym("terav"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.TERAVOLT][UnitsElectricPotential.ZEPTOVOLT] = \
        Mul(Rat(Int(1), Pow(10, 33)), Sym("terav"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.TERAVOLT][UnitsElectricPotential.ZETTAVOLT] = \
        Mul(Pow(10, 9), Sym("terav"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.VOLT][UnitsElectricPotential.ATTOVOLT] = \
        Mul(Rat(Int(1), Pow(10, 18)), Sym("v"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.VOLT][UnitsElectricPotential.CENTIVOLT] = \
        Mul(Rat(Int(1), Int(100)), Sym("v"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.VOLT][UnitsElectricPotential.DECAVOLT] = \
        Mul(Int(10), Sym("v"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.VOLT][UnitsElectricPotential.DECIVOLT] = \
        Mul(Rat(Int(1), Int(10)), Sym("v"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.VOLT][UnitsElectricPotential.EXAVOLT] = \
        Mul(Pow(10, 18), Sym("v"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.VOLT][UnitsElectricPotential.FEMTOVOLT] = \
        Mul(Rat(Int(1), Pow(10, 15)), Sym("v"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.VOLT][UnitsElectricPotential.GIGAVOLT] = \
        Mul(Pow(10, 9), Sym("v"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.VOLT][UnitsElectricPotential.HECTOVOLT] = \
        Mul(Int(100), Sym("v"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.VOLT][UnitsElectricPotential.KILOVOLT] = \
        Mul(Int(1000), Sym("v"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.VOLT][UnitsElectricPotential.MEGAVOLT] = \
        Mul(Pow(10, 6), Sym("v"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.VOLT][UnitsElectricPotential.MICROVOLT] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("v"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.VOLT][UnitsElectricPotential.MILLIVOLT] = \
        Mul(Rat(Int(1), Int(1000)), Sym("v"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.VOLT][UnitsElectricPotential.NANOVOLT] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("v"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.VOLT][UnitsElectricPotential.PETAVOLT] = \
        Mul(Pow(10, 15), Sym("v"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.VOLT][UnitsElectricPotential.PICOVOLT] = \
        Mul(Rat(Int(1), Pow(10, 12)), Sym("v"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.VOLT][UnitsElectricPotential.TERAVOLT] = \
        Mul(Pow(10, 12), Sym("v"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.VOLT][UnitsElectricPotential.YOCTOVOLT] = \
        Mul(Rat(Int(1), Pow(10, 24)), Sym("v"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.VOLT][UnitsElectricPotential.YOTTAVOLT] = \
        Mul(Pow(10, 24), Sym("v"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.VOLT][UnitsElectricPotential.ZEPTOVOLT] = \
        Mul(Rat(Int(1), Pow(10, 21)), Sym("v"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.VOLT][UnitsElectricPotential.ZETTAVOLT] = \
        Mul(Pow(10, 21), Sym("v"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.YOCTOVOLT][UnitsElectricPotential.ATTOVOLT] = \
        Mul(Pow(10, 6), Sym("yoctov"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.YOCTOVOLT][UnitsElectricPotential.CENTIVOLT] = \
        Mul(Pow(10, 22), Sym("yoctov"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.YOCTOVOLT][UnitsElectricPotential.DECAVOLT] = \
        Mul(Pow(10, 25), Sym("yoctov"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.YOCTOVOLT][UnitsElectricPotential.DECIVOLT] = \
        Mul(Pow(10, 23), Sym("yoctov"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.YOCTOVOLT][UnitsElectricPotential.EXAVOLT] = \
        Mul(Pow(10, 42), Sym("yoctov"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.YOCTOVOLT][UnitsElectricPotential.FEMTOVOLT] = \
        Mul(Pow(10, 9), Sym("yoctov"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.YOCTOVOLT][UnitsElectricPotential.GIGAVOLT] = \
        Mul(Pow(10, 33), Sym("yoctov"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.YOCTOVOLT][UnitsElectricPotential.HECTOVOLT] = \
        Mul(Pow(10, 26), Sym("yoctov"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.YOCTOVOLT][UnitsElectricPotential.KILOVOLT] = \
        Mul(Pow(10, 27), Sym("yoctov"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.YOCTOVOLT][UnitsElectricPotential.MEGAVOLT] = \
        Mul(Pow(10, 30), Sym("yoctov"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.YOCTOVOLT][UnitsElectricPotential.MICROVOLT] = \
        Mul(Pow(10, 18), Sym("yoctov"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.YOCTOVOLT][UnitsElectricPotential.MILLIVOLT] = \
        Mul(Pow(10, 21), Sym("yoctov"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.YOCTOVOLT][UnitsElectricPotential.NANOVOLT] = \
        Mul(Pow(10, 15), Sym("yoctov"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.YOCTOVOLT][UnitsElectricPotential.PETAVOLT] = \
        Mul(Pow(10, 39), Sym("yoctov"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.YOCTOVOLT][UnitsElectricPotential.PICOVOLT] = \
        Mul(Pow(10, 12), Sym("yoctov"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.YOCTOVOLT][UnitsElectricPotential.TERAVOLT] = \
        Mul(Pow(10, 36), Sym("yoctov"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.YOCTOVOLT][UnitsElectricPotential.VOLT] = \
        Mul(Pow(10, 24), Sym("yoctov"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.YOCTOVOLT][UnitsElectricPotential.YOTTAVOLT] = \
        Mul(Pow(10, 48), Sym("yoctov"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.YOCTOVOLT][UnitsElectricPotential.ZEPTOVOLT] = \
        Mul(Int(1000), Sym("yoctov"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.YOCTOVOLT][UnitsElectricPotential.ZETTAVOLT] = \
        Mul(Pow(10, 45), Sym("yoctov"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.YOTTAVOLT][UnitsElectricPotential.ATTOVOLT] = \
        Mul(Rat(Int(1), Pow(10, 42)), Sym("yottav"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.YOTTAVOLT][UnitsElectricPotential.CENTIVOLT] = \
        Mul(Rat(Int(1), Pow(10, 26)), Sym("yottav"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.YOTTAVOLT][UnitsElectricPotential.DECAVOLT] = \
        Mul(Rat(Int(1), Pow(10, 23)), Sym("yottav"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.YOTTAVOLT][UnitsElectricPotential.DECIVOLT] = \
        Mul(Rat(Int(1), Pow(10, 25)), Sym("yottav"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.YOTTAVOLT][UnitsElectricPotential.EXAVOLT] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("yottav"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.YOTTAVOLT][UnitsElectricPotential.FEMTOVOLT] = \
        Mul(Rat(Int(1), Pow(10, 39)), Sym("yottav"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.YOTTAVOLT][UnitsElectricPotential.GIGAVOLT] = \
        Mul(Rat(Int(1), Pow(10, 15)), Sym("yottav"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.YOTTAVOLT][UnitsElectricPotential.HECTOVOLT] = \
        Mul(Rat(Int(1), Pow(10, 22)), Sym("yottav"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.YOTTAVOLT][UnitsElectricPotential.KILOVOLT] = \
        Mul(Rat(Int(1), Pow(10, 21)), Sym("yottav"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.YOTTAVOLT][UnitsElectricPotential.MEGAVOLT] = \
        Mul(Rat(Int(1), Pow(10, 18)), Sym("yottav"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.YOTTAVOLT][UnitsElectricPotential.MICROVOLT] = \
        Mul(Rat(Int(1), Pow(10, 30)), Sym("yottav"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.YOTTAVOLT][UnitsElectricPotential.MILLIVOLT] = \
        Mul(Rat(Int(1), Pow(10, 27)), Sym("yottav"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.YOTTAVOLT][UnitsElectricPotential.NANOVOLT] = \
        Mul(Rat(Int(1), Pow(10, 33)), Sym("yottav"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.YOTTAVOLT][UnitsElectricPotential.PETAVOLT] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("yottav"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.YOTTAVOLT][UnitsElectricPotential.PICOVOLT] = \
        Mul(Rat(Int(1), Pow(10, 36)), Sym("yottav"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.YOTTAVOLT][UnitsElectricPotential.TERAVOLT] = \
        Mul(Rat(Int(1), Pow(10, 12)), Sym("yottav"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.YOTTAVOLT][UnitsElectricPotential.VOLT] = \
        Mul(Rat(Int(1), Pow(10, 24)), Sym("yottav"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.YOTTAVOLT][UnitsElectricPotential.YOCTOVOLT] = \
        Mul(Rat(Int(1), Pow(10, 48)), Sym("yottav"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.YOTTAVOLT][UnitsElectricPotential.ZEPTOVOLT] = \
        Mul(Rat(Int(1), Pow(10, 45)), Sym("yottav"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.YOTTAVOLT][UnitsElectricPotential.ZETTAVOLT] = \
        Mul(Rat(Int(1), Int(1000)), Sym("yottav"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.ZEPTOVOLT][UnitsElectricPotential.ATTOVOLT] = \
        Mul(Int(1000), Sym("zeptov"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.ZEPTOVOLT][UnitsElectricPotential.CENTIVOLT] = \
        Mul(Pow(10, 19), Sym("zeptov"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.ZEPTOVOLT][UnitsElectricPotential.DECAVOLT] = \
        Mul(Pow(10, 22), Sym("zeptov"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.ZEPTOVOLT][UnitsElectricPotential.DECIVOLT] = \
        Mul(Pow(10, 20), Sym("zeptov"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.ZEPTOVOLT][UnitsElectricPotential.EXAVOLT] = \
        Mul(Pow(10, 39), Sym("zeptov"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.ZEPTOVOLT][UnitsElectricPotential.FEMTOVOLT] = \
        Mul(Pow(10, 6), Sym("zeptov"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.ZEPTOVOLT][UnitsElectricPotential.GIGAVOLT] = \
        Mul(Pow(10, 30), Sym("zeptov"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.ZEPTOVOLT][UnitsElectricPotential.HECTOVOLT] = \
        Mul(Pow(10, 23), Sym("zeptov"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.ZEPTOVOLT][UnitsElectricPotential.KILOVOLT] = \
        Mul(Pow(10, 24), Sym("zeptov"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.ZEPTOVOLT][UnitsElectricPotential.MEGAVOLT] = \
        Mul(Pow(10, 27), Sym("zeptov"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.ZEPTOVOLT][UnitsElectricPotential.MICROVOLT] = \
        Mul(Pow(10, 15), Sym("zeptov"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.ZEPTOVOLT][UnitsElectricPotential.MILLIVOLT] = \
        Mul(Pow(10, 18), Sym("zeptov"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.ZEPTOVOLT][UnitsElectricPotential.NANOVOLT] = \
        Mul(Pow(10, 12), Sym("zeptov"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.ZEPTOVOLT][UnitsElectricPotential.PETAVOLT] = \
        Mul(Pow(10, 36), Sym("zeptov"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.ZEPTOVOLT][UnitsElectricPotential.PICOVOLT] = \
        Mul(Pow(10, 9), Sym("zeptov"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.ZEPTOVOLT][UnitsElectricPotential.TERAVOLT] = \
        Mul(Pow(10, 33), Sym("zeptov"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.ZEPTOVOLT][UnitsElectricPotential.VOLT] = \
        Mul(Pow(10, 21), Sym("zeptov"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.ZEPTOVOLT][UnitsElectricPotential.YOCTOVOLT] = \
        Mul(Rat(Int(1), Int(1000)), Sym("zeptov"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.ZEPTOVOLT][UnitsElectricPotential.YOTTAVOLT] = \
        Mul(Pow(10, 45), Sym("zeptov"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.ZEPTOVOLT][UnitsElectricPotential.ZETTAVOLT] = \
        Mul(Pow(10, 42), Sym("zeptov"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.ZETTAVOLT][UnitsElectricPotential.ATTOVOLT] = \
        Mul(Rat(Int(1), Pow(10, 39)), Sym("zettav"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.ZETTAVOLT][UnitsElectricPotential.CENTIVOLT] = \
        Mul(Rat(Int(1), Pow(10, 23)), Sym("zettav"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.ZETTAVOLT][UnitsElectricPotential.DECAVOLT] = \
        Mul(Rat(Int(1), Pow(10, 20)), Sym("zettav"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.ZETTAVOLT][UnitsElectricPotential.DECIVOLT] = \
        Mul(Rat(Int(1), Pow(10, 22)), Sym("zettav"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.ZETTAVOLT][UnitsElectricPotential.EXAVOLT] = \
        Mul(Rat(Int(1), Int(1000)), Sym("zettav"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.ZETTAVOLT][UnitsElectricPotential.FEMTOVOLT] = \
        Mul(Rat(Int(1), Pow(10, 36)), Sym("zettav"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.ZETTAVOLT][UnitsElectricPotential.GIGAVOLT] = \
        Mul(Rat(Int(1), Pow(10, 12)), Sym("zettav"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.ZETTAVOLT][UnitsElectricPotential.HECTOVOLT] = \
        Mul(Rat(Int(1), Pow(10, 19)), Sym("zettav"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.ZETTAVOLT][UnitsElectricPotential.KILOVOLT] = \
        Mul(Rat(Int(1), Pow(10, 18)), Sym("zettav"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.ZETTAVOLT][UnitsElectricPotential.MEGAVOLT] = \
        Mul(Rat(Int(1), Pow(10, 15)), Sym("zettav"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.ZETTAVOLT][UnitsElectricPotential.MICROVOLT] = \
        Mul(Rat(Int(1), Pow(10, 27)), Sym("zettav"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.ZETTAVOLT][UnitsElectricPotential.MILLIVOLT] = \
        Mul(Rat(Int(1), Pow(10, 24)), Sym("zettav"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.ZETTAVOLT][UnitsElectricPotential.NANOVOLT] = \
        Mul(Rat(Int(1), Pow(10, 30)), Sym("zettav"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.ZETTAVOLT][UnitsElectricPotential.PETAVOLT] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("zettav"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.ZETTAVOLT][UnitsElectricPotential.PICOVOLT] = \
        Mul(Rat(Int(1), Pow(10, 33)), Sym("zettav"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.ZETTAVOLT][UnitsElectricPotential.TERAVOLT] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("zettav"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.ZETTAVOLT][UnitsElectricPotential.VOLT] = \
        Mul(Rat(Int(1), Pow(10, 21)), Sym("zettav"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.ZETTAVOLT][UnitsElectricPotential.YOCTOVOLT] = \
        Mul(Rat(Int(1), Pow(10, 45)), Sym("zettav"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.ZETTAVOLT][UnitsElectricPotential.YOTTAVOLT] = \
        Mul(Int(1000), Sym("zettav"))  # nopep8
    CONVERSIONS[UnitsElectricPotential.ZETTAVOLT][UnitsElectricPotential.ZEPTOVOLT] = \
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
            targetUnit = getattr(UnitsElectricPotential, str(target))
            sourceUnit = value.getUnit()
            source = str(sourceUnit)
            if target == source:
                self.setValue(value.getValue())
                self.setUnit(value.getUnit())
            else:
                c = self.CONVERSIONS.get(targetUnit).get(sourceUnit)
                if c is None:
                    t = (value.getValue(), value.getUnit(), target)
                    msg = "%s %s cannot be converted to %s" % t
                    raise Exception(msg)
                self.setValue(c(value.getValue()))
                self.setUnit(targetUnit)
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
