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
Code-generated omero.model.Frequency implementation,
based on omero.model.PermissionsI
"""


import Ice
import IceImport
IceImport.load("omero_model_Frequency_ice")
_omero = Ice.openModule("omero")
_omero_model = Ice.openModule("omero.model")
__name__ = "omero.model"

from omero_model_UnitBase import UnitBase
from omero.model.enums import UnitsFrequency

from omero.conversions import Add  # nopep8
from omero.conversions import Int  # nopep8
from omero.conversions import Mul  # nopep8
from omero.conversions import Pow  # nopep8
from omero.conversions import Rat  # nopep8
from omero.conversions import Sym  # nopep8


class FrequencyI(_omero_model.Frequency, UnitBase):

    try:
        UNIT_VALUES = sorted(UnitsFrequency._enumerators.values())
    except:
        # TODO: this occurs on Ice 3.4 and can be removed
        # once it has been dropped.
        UNIT_VALUES = [x for x in sorted(UnitsFrequency._names)]
        UNIT_VALUES = [getattr(UnitsFrequency, x) for x in UNIT_VALUES]
    CONVERSIONS = dict()
    for val in UNIT_VALUES:
        CONVERSIONS[val] = dict()
    CONVERSIONS[UnitsFrequency.ATTOHERTZ][UnitsFrequency.CENTIHERTZ] = \
        Mul(Pow(10, 16), Sym("attohz"))  # nopep8
    CONVERSIONS[UnitsFrequency.ATTOHERTZ][UnitsFrequency.DECAHERTZ] = \
        Mul(Pow(10, 19), Sym("attohz"))  # nopep8
    CONVERSIONS[UnitsFrequency.ATTOHERTZ][UnitsFrequency.DECIHERTZ] = \
        Mul(Pow(10, 17), Sym("attohz"))  # nopep8
    CONVERSIONS[UnitsFrequency.ATTOHERTZ][UnitsFrequency.EXAHERTZ] = \
        Mul(Pow(10, 36), Sym("attohz"))  # nopep8
    CONVERSIONS[UnitsFrequency.ATTOHERTZ][UnitsFrequency.FEMTOHERTZ] = \
        Mul(Int(1000), Sym("attohz"))  # nopep8
    CONVERSIONS[UnitsFrequency.ATTOHERTZ][UnitsFrequency.GIGAHERTZ] = \
        Mul(Pow(10, 27), Sym("attohz"))  # nopep8
    CONVERSIONS[UnitsFrequency.ATTOHERTZ][UnitsFrequency.HECTOHERTZ] = \
        Mul(Pow(10, 20), Sym("attohz"))  # nopep8
    CONVERSIONS[UnitsFrequency.ATTOHERTZ][UnitsFrequency.HERTZ] = \
        Mul(Pow(10, 18), Sym("attohz"))  # nopep8
    CONVERSIONS[UnitsFrequency.ATTOHERTZ][UnitsFrequency.KILOHERTZ] = \
        Mul(Pow(10, 21), Sym("attohz"))  # nopep8
    CONVERSIONS[UnitsFrequency.ATTOHERTZ][UnitsFrequency.MEGAHERTZ] = \
        Mul(Pow(10, 24), Sym("attohz"))  # nopep8
    CONVERSIONS[UnitsFrequency.ATTOHERTZ][UnitsFrequency.MICROHERTZ] = \
        Mul(Pow(10, 12), Sym("attohz"))  # nopep8
    CONVERSIONS[UnitsFrequency.ATTOHERTZ][UnitsFrequency.MILLIHERTZ] = \
        Mul(Pow(10, 15), Sym("attohz"))  # nopep8
    CONVERSIONS[UnitsFrequency.ATTOHERTZ][UnitsFrequency.NANOHERTZ] = \
        Mul(Pow(10, 9), Sym("attohz"))  # nopep8
    CONVERSIONS[UnitsFrequency.ATTOHERTZ][UnitsFrequency.PETAHERTZ] = \
        Mul(Pow(10, 33), Sym("attohz"))  # nopep8
    CONVERSIONS[UnitsFrequency.ATTOHERTZ][UnitsFrequency.PICOHERTZ] = \
        Mul(Pow(10, 6), Sym("attohz"))  # nopep8
    CONVERSIONS[UnitsFrequency.ATTOHERTZ][UnitsFrequency.TERAHERTZ] = \
        Mul(Pow(10, 30), Sym("attohz"))  # nopep8
    CONVERSIONS[UnitsFrequency.ATTOHERTZ][UnitsFrequency.YOCTOHERTZ] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("attohz"))  # nopep8
    CONVERSIONS[UnitsFrequency.ATTOHERTZ][UnitsFrequency.YOTTAHERTZ] = \
        Mul(Pow(10, 42), Sym("attohz"))  # nopep8
    CONVERSIONS[UnitsFrequency.ATTOHERTZ][UnitsFrequency.ZEPTOHERTZ] = \
        Mul(Rat(Int(1), Int(1000)), Sym("attohz"))  # nopep8
    CONVERSIONS[UnitsFrequency.ATTOHERTZ][UnitsFrequency.ZETTAHERTZ] = \
        Mul(Pow(10, 39), Sym("attohz"))  # nopep8
    CONVERSIONS[UnitsFrequency.CENTIHERTZ][UnitsFrequency.ATTOHERTZ] = \
        Mul(Rat(Int(1), Pow(10, 16)), Sym("centihz"))  # nopep8
    CONVERSIONS[UnitsFrequency.CENTIHERTZ][UnitsFrequency.DECAHERTZ] = \
        Mul(Int(1000), Sym("centihz"))  # nopep8
    CONVERSIONS[UnitsFrequency.CENTIHERTZ][UnitsFrequency.DECIHERTZ] = \
        Mul(Int(10), Sym("centihz"))  # nopep8
    CONVERSIONS[UnitsFrequency.CENTIHERTZ][UnitsFrequency.EXAHERTZ] = \
        Mul(Pow(10, 20), Sym("centihz"))  # nopep8
    CONVERSIONS[UnitsFrequency.CENTIHERTZ][UnitsFrequency.FEMTOHERTZ] = \
        Mul(Rat(Int(1), Pow(10, 13)), Sym("centihz"))  # nopep8
    CONVERSIONS[UnitsFrequency.CENTIHERTZ][UnitsFrequency.GIGAHERTZ] = \
        Mul(Pow(10, 11), Sym("centihz"))  # nopep8
    CONVERSIONS[UnitsFrequency.CENTIHERTZ][UnitsFrequency.HECTOHERTZ] = \
        Mul(Pow(10, 4), Sym("centihz"))  # nopep8
    CONVERSIONS[UnitsFrequency.CENTIHERTZ][UnitsFrequency.HERTZ] = \
        Mul(Int(100), Sym("centihz"))  # nopep8
    CONVERSIONS[UnitsFrequency.CENTIHERTZ][UnitsFrequency.KILOHERTZ] = \
        Mul(Pow(10, 5), Sym("centihz"))  # nopep8
    CONVERSIONS[UnitsFrequency.CENTIHERTZ][UnitsFrequency.MEGAHERTZ] = \
        Mul(Pow(10, 8), Sym("centihz"))  # nopep8
    CONVERSIONS[UnitsFrequency.CENTIHERTZ][UnitsFrequency.MICROHERTZ] = \
        Mul(Rat(Int(1), Pow(10, 4)), Sym("centihz"))  # nopep8
    CONVERSIONS[UnitsFrequency.CENTIHERTZ][UnitsFrequency.MILLIHERTZ] = \
        Mul(Rat(Int(1), Int(10)), Sym("centihz"))  # nopep8
    CONVERSIONS[UnitsFrequency.CENTIHERTZ][UnitsFrequency.NANOHERTZ] = \
        Mul(Rat(Int(1), Pow(10, 7)), Sym("centihz"))  # nopep8
    CONVERSIONS[UnitsFrequency.CENTIHERTZ][UnitsFrequency.PETAHERTZ] = \
        Mul(Pow(10, 17), Sym("centihz"))  # nopep8
    CONVERSIONS[UnitsFrequency.CENTIHERTZ][UnitsFrequency.PICOHERTZ] = \
        Mul(Rat(Int(1), Pow(10, 10)), Sym("centihz"))  # nopep8
    CONVERSIONS[UnitsFrequency.CENTIHERTZ][UnitsFrequency.TERAHERTZ] = \
        Mul(Pow(10, 14), Sym("centihz"))  # nopep8
    CONVERSIONS[UnitsFrequency.CENTIHERTZ][UnitsFrequency.YOCTOHERTZ] = \
        Mul(Rat(Int(1), Pow(10, 22)), Sym("centihz"))  # nopep8
    CONVERSIONS[UnitsFrequency.CENTIHERTZ][UnitsFrequency.YOTTAHERTZ] = \
        Mul(Pow(10, 26), Sym("centihz"))  # nopep8
    CONVERSIONS[UnitsFrequency.CENTIHERTZ][UnitsFrequency.ZEPTOHERTZ] = \
        Mul(Rat(Int(1), Pow(10, 19)), Sym("centihz"))  # nopep8
    CONVERSIONS[UnitsFrequency.CENTIHERTZ][UnitsFrequency.ZETTAHERTZ] = \
        Mul(Pow(10, 23), Sym("centihz"))  # nopep8
    CONVERSIONS[UnitsFrequency.DECAHERTZ][UnitsFrequency.ATTOHERTZ] = \
        Mul(Rat(Int(1), Pow(10, 19)), Sym("decahz"))  # nopep8
    CONVERSIONS[UnitsFrequency.DECAHERTZ][UnitsFrequency.CENTIHERTZ] = \
        Mul(Rat(Int(1), Int(1000)), Sym("decahz"))  # nopep8
    CONVERSIONS[UnitsFrequency.DECAHERTZ][UnitsFrequency.DECIHERTZ] = \
        Mul(Rat(Int(1), Int(100)), Sym("decahz"))  # nopep8
    CONVERSIONS[UnitsFrequency.DECAHERTZ][UnitsFrequency.EXAHERTZ] = \
        Mul(Pow(10, 17), Sym("decahz"))  # nopep8
    CONVERSIONS[UnitsFrequency.DECAHERTZ][UnitsFrequency.FEMTOHERTZ] = \
        Mul(Rat(Int(1), Pow(10, 16)), Sym("decahz"))  # nopep8
    CONVERSIONS[UnitsFrequency.DECAHERTZ][UnitsFrequency.GIGAHERTZ] = \
        Mul(Pow(10, 8), Sym("decahz"))  # nopep8
    CONVERSIONS[UnitsFrequency.DECAHERTZ][UnitsFrequency.HECTOHERTZ] = \
        Mul(Int(10), Sym("decahz"))  # nopep8
    CONVERSIONS[UnitsFrequency.DECAHERTZ][UnitsFrequency.HERTZ] = \
        Mul(Rat(Int(1), Int(10)), Sym("decahz"))  # nopep8
    CONVERSIONS[UnitsFrequency.DECAHERTZ][UnitsFrequency.KILOHERTZ] = \
        Mul(Int(100), Sym("decahz"))  # nopep8
    CONVERSIONS[UnitsFrequency.DECAHERTZ][UnitsFrequency.MEGAHERTZ] = \
        Mul(Pow(10, 5), Sym("decahz"))  # nopep8
    CONVERSIONS[UnitsFrequency.DECAHERTZ][UnitsFrequency.MICROHERTZ] = \
        Mul(Rat(Int(1), Pow(10, 7)), Sym("decahz"))  # nopep8
    CONVERSIONS[UnitsFrequency.DECAHERTZ][UnitsFrequency.MILLIHERTZ] = \
        Mul(Rat(Int(1), Pow(10, 4)), Sym("decahz"))  # nopep8
    CONVERSIONS[UnitsFrequency.DECAHERTZ][UnitsFrequency.NANOHERTZ] = \
        Mul(Rat(Int(1), Pow(10, 10)), Sym("decahz"))  # nopep8
    CONVERSIONS[UnitsFrequency.DECAHERTZ][UnitsFrequency.PETAHERTZ] = \
        Mul(Pow(10, 14), Sym("decahz"))  # nopep8
    CONVERSIONS[UnitsFrequency.DECAHERTZ][UnitsFrequency.PICOHERTZ] = \
        Mul(Rat(Int(1), Pow(10, 13)), Sym("decahz"))  # nopep8
    CONVERSIONS[UnitsFrequency.DECAHERTZ][UnitsFrequency.TERAHERTZ] = \
        Mul(Pow(10, 11), Sym("decahz"))  # nopep8
    CONVERSIONS[UnitsFrequency.DECAHERTZ][UnitsFrequency.YOCTOHERTZ] = \
        Mul(Rat(Int(1), Pow(10, 25)), Sym("decahz"))  # nopep8
    CONVERSIONS[UnitsFrequency.DECAHERTZ][UnitsFrequency.YOTTAHERTZ] = \
        Mul(Pow(10, 23), Sym("decahz"))  # nopep8
    CONVERSIONS[UnitsFrequency.DECAHERTZ][UnitsFrequency.ZEPTOHERTZ] = \
        Mul(Rat(Int(1), Pow(10, 22)), Sym("decahz"))  # nopep8
    CONVERSIONS[UnitsFrequency.DECAHERTZ][UnitsFrequency.ZETTAHERTZ] = \
        Mul(Pow(10, 20), Sym("decahz"))  # nopep8
    CONVERSIONS[UnitsFrequency.DECIHERTZ][UnitsFrequency.ATTOHERTZ] = \
        Mul(Rat(Int(1), Pow(10, 17)), Sym("decihz"))  # nopep8
    CONVERSIONS[UnitsFrequency.DECIHERTZ][UnitsFrequency.CENTIHERTZ] = \
        Mul(Rat(Int(1), Int(10)), Sym("decihz"))  # nopep8
    CONVERSIONS[UnitsFrequency.DECIHERTZ][UnitsFrequency.DECAHERTZ] = \
        Mul(Int(100), Sym("decihz"))  # nopep8
    CONVERSIONS[UnitsFrequency.DECIHERTZ][UnitsFrequency.EXAHERTZ] = \
        Mul(Pow(10, 19), Sym("decihz"))  # nopep8
    CONVERSIONS[UnitsFrequency.DECIHERTZ][UnitsFrequency.FEMTOHERTZ] = \
        Mul(Rat(Int(1), Pow(10, 14)), Sym("decihz"))  # nopep8
    CONVERSIONS[UnitsFrequency.DECIHERTZ][UnitsFrequency.GIGAHERTZ] = \
        Mul(Pow(10, 10), Sym("decihz"))  # nopep8
    CONVERSIONS[UnitsFrequency.DECIHERTZ][UnitsFrequency.HECTOHERTZ] = \
        Mul(Int(1000), Sym("decihz"))  # nopep8
    CONVERSIONS[UnitsFrequency.DECIHERTZ][UnitsFrequency.HERTZ] = \
        Mul(Int(10), Sym("decihz"))  # nopep8
    CONVERSIONS[UnitsFrequency.DECIHERTZ][UnitsFrequency.KILOHERTZ] = \
        Mul(Pow(10, 4), Sym("decihz"))  # nopep8
    CONVERSIONS[UnitsFrequency.DECIHERTZ][UnitsFrequency.MEGAHERTZ] = \
        Mul(Pow(10, 7), Sym("decihz"))  # nopep8
    CONVERSIONS[UnitsFrequency.DECIHERTZ][UnitsFrequency.MICROHERTZ] = \
        Mul(Rat(Int(1), Pow(10, 5)), Sym("decihz"))  # nopep8
    CONVERSIONS[UnitsFrequency.DECIHERTZ][UnitsFrequency.MILLIHERTZ] = \
        Mul(Rat(Int(1), Int(100)), Sym("decihz"))  # nopep8
    CONVERSIONS[UnitsFrequency.DECIHERTZ][UnitsFrequency.NANOHERTZ] = \
        Mul(Rat(Int(1), Pow(10, 8)), Sym("decihz"))  # nopep8
    CONVERSIONS[UnitsFrequency.DECIHERTZ][UnitsFrequency.PETAHERTZ] = \
        Mul(Pow(10, 16), Sym("decihz"))  # nopep8
    CONVERSIONS[UnitsFrequency.DECIHERTZ][UnitsFrequency.PICOHERTZ] = \
        Mul(Rat(Int(1), Pow(10, 11)), Sym("decihz"))  # nopep8
    CONVERSIONS[UnitsFrequency.DECIHERTZ][UnitsFrequency.TERAHERTZ] = \
        Mul(Pow(10, 13), Sym("decihz"))  # nopep8
    CONVERSIONS[UnitsFrequency.DECIHERTZ][UnitsFrequency.YOCTOHERTZ] = \
        Mul(Rat(Int(1), Pow(10, 23)), Sym("decihz"))  # nopep8
    CONVERSIONS[UnitsFrequency.DECIHERTZ][UnitsFrequency.YOTTAHERTZ] = \
        Mul(Pow(10, 25), Sym("decihz"))  # nopep8
    CONVERSIONS[UnitsFrequency.DECIHERTZ][UnitsFrequency.ZEPTOHERTZ] = \
        Mul(Rat(Int(1), Pow(10, 20)), Sym("decihz"))  # nopep8
    CONVERSIONS[UnitsFrequency.DECIHERTZ][UnitsFrequency.ZETTAHERTZ] = \
        Mul(Pow(10, 22), Sym("decihz"))  # nopep8
    CONVERSIONS[UnitsFrequency.EXAHERTZ][UnitsFrequency.ATTOHERTZ] = \
        Mul(Rat(Int(1), Pow(10, 36)), Sym("exahz"))  # nopep8
    CONVERSIONS[UnitsFrequency.EXAHERTZ][UnitsFrequency.CENTIHERTZ] = \
        Mul(Rat(Int(1), Pow(10, 20)), Sym("exahz"))  # nopep8
    CONVERSIONS[UnitsFrequency.EXAHERTZ][UnitsFrequency.DECAHERTZ] = \
        Mul(Rat(Int(1), Pow(10, 17)), Sym("exahz"))  # nopep8
    CONVERSIONS[UnitsFrequency.EXAHERTZ][UnitsFrequency.DECIHERTZ] = \
        Mul(Rat(Int(1), Pow(10, 19)), Sym("exahz"))  # nopep8
    CONVERSIONS[UnitsFrequency.EXAHERTZ][UnitsFrequency.FEMTOHERTZ] = \
        Mul(Rat(Int(1), Pow(10, 33)), Sym("exahz"))  # nopep8
    CONVERSIONS[UnitsFrequency.EXAHERTZ][UnitsFrequency.GIGAHERTZ] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("exahz"))  # nopep8
    CONVERSIONS[UnitsFrequency.EXAHERTZ][UnitsFrequency.HECTOHERTZ] = \
        Mul(Rat(Int(1), Pow(10, 16)), Sym("exahz"))  # nopep8
    CONVERSIONS[UnitsFrequency.EXAHERTZ][UnitsFrequency.HERTZ] = \
        Mul(Rat(Int(1), Pow(10, 18)), Sym("exahz"))  # nopep8
    CONVERSIONS[UnitsFrequency.EXAHERTZ][UnitsFrequency.KILOHERTZ] = \
        Mul(Rat(Int(1), Pow(10, 15)), Sym("exahz"))  # nopep8
    CONVERSIONS[UnitsFrequency.EXAHERTZ][UnitsFrequency.MEGAHERTZ] = \
        Mul(Rat(Int(1), Pow(10, 12)), Sym("exahz"))  # nopep8
    CONVERSIONS[UnitsFrequency.EXAHERTZ][UnitsFrequency.MICROHERTZ] = \
        Mul(Rat(Int(1), Pow(10, 24)), Sym("exahz"))  # nopep8
    CONVERSIONS[UnitsFrequency.EXAHERTZ][UnitsFrequency.MILLIHERTZ] = \
        Mul(Rat(Int(1), Pow(10, 21)), Sym("exahz"))  # nopep8
    CONVERSIONS[UnitsFrequency.EXAHERTZ][UnitsFrequency.NANOHERTZ] = \
        Mul(Rat(Int(1), Pow(10, 27)), Sym("exahz"))  # nopep8
    CONVERSIONS[UnitsFrequency.EXAHERTZ][UnitsFrequency.PETAHERTZ] = \
        Mul(Rat(Int(1), Int(1000)), Sym("exahz"))  # nopep8
    CONVERSIONS[UnitsFrequency.EXAHERTZ][UnitsFrequency.PICOHERTZ] = \
        Mul(Rat(Int(1), Pow(10, 30)), Sym("exahz"))  # nopep8
    CONVERSIONS[UnitsFrequency.EXAHERTZ][UnitsFrequency.TERAHERTZ] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("exahz"))  # nopep8
    CONVERSIONS[UnitsFrequency.EXAHERTZ][UnitsFrequency.YOCTOHERTZ] = \
        Mul(Rat(Int(1), Pow(10, 42)), Sym("exahz"))  # nopep8
    CONVERSIONS[UnitsFrequency.EXAHERTZ][UnitsFrequency.YOTTAHERTZ] = \
        Mul(Pow(10, 6), Sym("exahz"))  # nopep8
    CONVERSIONS[UnitsFrequency.EXAHERTZ][UnitsFrequency.ZEPTOHERTZ] = \
        Mul(Rat(Int(1), Pow(10, 39)), Sym("exahz"))  # nopep8
    CONVERSIONS[UnitsFrequency.EXAHERTZ][UnitsFrequency.ZETTAHERTZ] = \
        Mul(Int(1000), Sym("exahz"))  # nopep8
    CONVERSIONS[UnitsFrequency.FEMTOHERTZ][UnitsFrequency.ATTOHERTZ] = \
        Mul(Rat(Int(1), Int(1000)), Sym("femtohz"))  # nopep8
    CONVERSIONS[UnitsFrequency.FEMTOHERTZ][UnitsFrequency.CENTIHERTZ] = \
        Mul(Pow(10, 13), Sym("femtohz"))  # nopep8
    CONVERSIONS[UnitsFrequency.FEMTOHERTZ][UnitsFrequency.DECAHERTZ] = \
        Mul(Pow(10, 16), Sym("femtohz"))  # nopep8
    CONVERSIONS[UnitsFrequency.FEMTOHERTZ][UnitsFrequency.DECIHERTZ] = \
        Mul(Pow(10, 14), Sym("femtohz"))  # nopep8
    CONVERSIONS[UnitsFrequency.FEMTOHERTZ][UnitsFrequency.EXAHERTZ] = \
        Mul(Pow(10, 33), Sym("femtohz"))  # nopep8
    CONVERSIONS[UnitsFrequency.FEMTOHERTZ][UnitsFrequency.GIGAHERTZ] = \
        Mul(Pow(10, 24), Sym("femtohz"))  # nopep8
    CONVERSIONS[UnitsFrequency.FEMTOHERTZ][UnitsFrequency.HECTOHERTZ] = \
        Mul(Pow(10, 17), Sym("femtohz"))  # nopep8
    CONVERSIONS[UnitsFrequency.FEMTOHERTZ][UnitsFrequency.HERTZ] = \
        Mul(Pow(10, 15), Sym("femtohz"))  # nopep8
    CONVERSIONS[UnitsFrequency.FEMTOHERTZ][UnitsFrequency.KILOHERTZ] = \
        Mul(Pow(10, 18), Sym("femtohz"))  # nopep8
    CONVERSIONS[UnitsFrequency.FEMTOHERTZ][UnitsFrequency.MEGAHERTZ] = \
        Mul(Pow(10, 21), Sym("femtohz"))  # nopep8
    CONVERSIONS[UnitsFrequency.FEMTOHERTZ][UnitsFrequency.MICROHERTZ] = \
        Mul(Pow(10, 9), Sym("femtohz"))  # nopep8
    CONVERSIONS[UnitsFrequency.FEMTOHERTZ][UnitsFrequency.MILLIHERTZ] = \
        Mul(Pow(10, 12), Sym("femtohz"))  # nopep8
    CONVERSIONS[UnitsFrequency.FEMTOHERTZ][UnitsFrequency.NANOHERTZ] = \
        Mul(Pow(10, 6), Sym("femtohz"))  # nopep8
    CONVERSIONS[UnitsFrequency.FEMTOHERTZ][UnitsFrequency.PETAHERTZ] = \
        Mul(Pow(10, 30), Sym("femtohz"))  # nopep8
    CONVERSIONS[UnitsFrequency.FEMTOHERTZ][UnitsFrequency.PICOHERTZ] = \
        Mul(Int(1000), Sym("femtohz"))  # nopep8
    CONVERSIONS[UnitsFrequency.FEMTOHERTZ][UnitsFrequency.TERAHERTZ] = \
        Mul(Pow(10, 27), Sym("femtohz"))  # nopep8
    CONVERSIONS[UnitsFrequency.FEMTOHERTZ][UnitsFrequency.YOCTOHERTZ] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("femtohz"))  # nopep8
    CONVERSIONS[UnitsFrequency.FEMTOHERTZ][UnitsFrequency.YOTTAHERTZ] = \
        Mul(Pow(10, 39), Sym("femtohz"))  # nopep8
    CONVERSIONS[UnitsFrequency.FEMTOHERTZ][UnitsFrequency.ZEPTOHERTZ] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("femtohz"))  # nopep8
    CONVERSIONS[UnitsFrequency.FEMTOHERTZ][UnitsFrequency.ZETTAHERTZ] = \
        Mul(Pow(10, 36), Sym("femtohz"))  # nopep8
    CONVERSIONS[UnitsFrequency.GIGAHERTZ][UnitsFrequency.ATTOHERTZ] = \
        Mul(Rat(Int(1), Pow(10, 27)), Sym("gigahz"))  # nopep8
    CONVERSIONS[UnitsFrequency.GIGAHERTZ][UnitsFrequency.CENTIHERTZ] = \
        Mul(Rat(Int(1), Pow(10, 11)), Sym("gigahz"))  # nopep8
    CONVERSIONS[UnitsFrequency.GIGAHERTZ][UnitsFrequency.DECAHERTZ] = \
        Mul(Rat(Int(1), Pow(10, 8)), Sym("gigahz"))  # nopep8
    CONVERSIONS[UnitsFrequency.GIGAHERTZ][UnitsFrequency.DECIHERTZ] = \
        Mul(Rat(Int(1), Pow(10, 10)), Sym("gigahz"))  # nopep8
    CONVERSIONS[UnitsFrequency.GIGAHERTZ][UnitsFrequency.EXAHERTZ] = \
        Mul(Pow(10, 9), Sym("gigahz"))  # nopep8
    CONVERSIONS[UnitsFrequency.GIGAHERTZ][UnitsFrequency.FEMTOHERTZ] = \
        Mul(Rat(Int(1), Pow(10, 24)), Sym("gigahz"))  # nopep8
    CONVERSIONS[UnitsFrequency.GIGAHERTZ][UnitsFrequency.HECTOHERTZ] = \
        Mul(Rat(Int(1), Pow(10, 7)), Sym("gigahz"))  # nopep8
    CONVERSIONS[UnitsFrequency.GIGAHERTZ][UnitsFrequency.HERTZ] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("gigahz"))  # nopep8
    CONVERSIONS[UnitsFrequency.GIGAHERTZ][UnitsFrequency.KILOHERTZ] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("gigahz"))  # nopep8
    CONVERSIONS[UnitsFrequency.GIGAHERTZ][UnitsFrequency.MEGAHERTZ] = \
        Mul(Rat(Int(1), Int(1000)), Sym("gigahz"))  # nopep8
    CONVERSIONS[UnitsFrequency.GIGAHERTZ][UnitsFrequency.MICROHERTZ] = \
        Mul(Rat(Int(1), Pow(10, 15)), Sym("gigahz"))  # nopep8
    CONVERSIONS[UnitsFrequency.GIGAHERTZ][UnitsFrequency.MILLIHERTZ] = \
        Mul(Rat(Int(1), Pow(10, 12)), Sym("gigahz"))  # nopep8
    CONVERSIONS[UnitsFrequency.GIGAHERTZ][UnitsFrequency.NANOHERTZ] = \
        Mul(Rat(Int(1), Pow(10, 18)), Sym("gigahz"))  # nopep8
    CONVERSIONS[UnitsFrequency.GIGAHERTZ][UnitsFrequency.PETAHERTZ] = \
        Mul(Pow(10, 6), Sym("gigahz"))  # nopep8
    CONVERSIONS[UnitsFrequency.GIGAHERTZ][UnitsFrequency.PICOHERTZ] = \
        Mul(Rat(Int(1), Pow(10, 21)), Sym("gigahz"))  # nopep8
    CONVERSIONS[UnitsFrequency.GIGAHERTZ][UnitsFrequency.TERAHERTZ] = \
        Mul(Int(1000), Sym("gigahz"))  # nopep8
    CONVERSIONS[UnitsFrequency.GIGAHERTZ][UnitsFrequency.YOCTOHERTZ] = \
        Mul(Rat(Int(1), Pow(10, 33)), Sym("gigahz"))  # nopep8
    CONVERSIONS[UnitsFrequency.GIGAHERTZ][UnitsFrequency.YOTTAHERTZ] = \
        Mul(Pow(10, 15), Sym("gigahz"))  # nopep8
    CONVERSIONS[UnitsFrequency.GIGAHERTZ][UnitsFrequency.ZEPTOHERTZ] = \
        Mul(Rat(Int(1), Pow(10, 30)), Sym("gigahz"))  # nopep8
    CONVERSIONS[UnitsFrequency.GIGAHERTZ][UnitsFrequency.ZETTAHERTZ] = \
        Mul(Pow(10, 12), Sym("gigahz"))  # nopep8
    CONVERSIONS[UnitsFrequency.HECTOHERTZ][UnitsFrequency.ATTOHERTZ] = \
        Mul(Rat(Int(1), Pow(10, 20)), Sym("hectohz"))  # nopep8
    CONVERSIONS[UnitsFrequency.HECTOHERTZ][UnitsFrequency.CENTIHERTZ] = \
        Mul(Rat(Int(1), Pow(10, 4)), Sym("hectohz"))  # nopep8
    CONVERSIONS[UnitsFrequency.HECTOHERTZ][UnitsFrequency.DECAHERTZ] = \
        Mul(Rat(Int(1), Int(10)), Sym("hectohz"))  # nopep8
    CONVERSIONS[UnitsFrequency.HECTOHERTZ][UnitsFrequency.DECIHERTZ] = \
        Mul(Rat(Int(1), Int(1000)), Sym("hectohz"))  # nopep8
    CONVERSIONS[UnitsFrequency.HECTOHERTZ][UnitsFrequency.EXAHERTZ] = \
        Mul(Pow(10, 16), Sym("hectohz"))  # nopep8
    CONVERSIONS[UnitsFrequency.HECTOHERTZ][UnitsFrequency.FEMTOHERTZ] = \
        Mul(Rat(Int(1), Pow(10, 17)), Sym("hectohz"))  # nopep8
    CONVERSIONS[UnitsFrequency.HECTOHERTZ][UnitsFrequency.GIGAHERTZ] = \
        Mul(Pow(10, 7), Sym("hectohz"))  # nopep8
    CONVERSIONS[UnitsFrequency.HECTOHERTZ][UnitsFrequency.HERTZ] = \
        Mul(Rat(Int(1), Int(100)), Sym("hectohz"))  # nopep8
    CONVERSIONS[UnitsFrequency.HECTOHERTZ][UnitsFrequency.KILOHERTZ] = \
        Mul(Int(10), Sym("hectohz"))  # nopep8
    CONVERSIONS[UnitsFrequency.HECTOHERTZ][UnitsFrequency.MEGAHERTZ] = \
        Mul(Pow(10, 4), Sym("hectohz"))  # nopep8
    CONVERSIONS[UnitsFrequency.HECTOHERTZ][UnitsFrequency.MICROHERTZ] = \
        Mul(Rat(Int(1), Pow(10, 8)), Sym("hectohz"))  # nopep8
    CONVERSIONS[UnitsFrequency.HECTOHERTZ][UnitsFrequency.MILLIHERTZ] = \
        Mul(Rat(Int(1), Pow(10, 5)), Sym("hectohz"))  # nopep8
    CONVERSIONS[UnitsFrequency.HECTOHERTZ][UnitsFrequency.NANOHERTZ] = \
        Mul(Rat(Int(1), Pow(10, 11)), Sym("hectohz"))  # nopep8
    CONVERSIONS[UnitsFrequency.HECTOHERTZ][UnitsFrequency.PETAHERTZ] = \
        Mul(Pow(10, 13), Sym("hectohz"))  # nopep8
    CONVERSIONS[UnitsFrequency.HECTOHERTZ][UnitsFrequency.PICOHERTZ] = \
        Mul(Rat(Int(1), Pow(10, 14)), Sym("hectohz"))  # nopep8
    CONVERSIONS[UnitsFrequency.HECTOHERTZ][UnitsFrequency.TERAHERTZ] = \
        Mul(Pow(10, 10), Sym("hectohz"))  # nopep8
    CONVERSIONS[UnitsFrequency.HECTOHERTZ][UnitsFrequency.YOCTOHERTZ] = \
        Mul(Rat(Int(1), Pow(10, 26)), Sym("hectohz"))  # nopep8
    CONVERSIONS[UnitsFrequency.HECTOHERTZ][UnitsFrequency.YOTTAHERTZ] = \
        Mul(Pow(10, 22), Sym("hectohz"))  # nopep8
    CONVERSIONS[UnitsFrequency.HECTOHERTZ][UnitsFrequency.ZEPTOHERTZ] = \
        Mul(Rat(Int(1), Pow(10, 23)), Sym("hectohz"))  # nopep8
    CONVERSIONS[UnitsFrequency.HECTOHERTZ][UnitsFrequency.ZETTAHERTZ] = \
        Mul(Pow(10, 19), Sym("hectohz"))  # nopep8
    CONVERSIONS[UnitsFrequency.HERTZ][UnitsFrequency.ATTOHERTZ] = \
        Mul(Rat(Int(1), Pow(10, 18)), Sym("hz"))  # nopep8
    CONVERSIONS[UnitsFrequency.HERTZ][UnitsFrequency.CENTIHERTZ] = \
        Mul(Rat(Int(1), Int(100)), Sym("hz"))  # nopep8
    CONVERSIONS[UnitsFrequency.HERTZ][UnitsFrequency.DECAHERTZ] = \
        Mul(Int(10), Sym("hz"))  # nopep8
    CONVERSIONS[UnitsFrequency.HERTZ][UnitsFrequency.DECIHERTZ] = \
        Mul(Rat(Int(1), Int(10)), Sym("hz"))  # nopep8
    CONVERSIONS[UnitsFrequency.HERTZ][UnitsFrequency.EXAHERTZ] = \
        Mul(Pow(10, 18), Sym("hz"))  # nopep8
    CONVERSIONS[UnitsFrequency.HERTZ][UnitsFrequency.FEMTOHERTZ] = \
        Mul(Rat(Int(1), Pow(10, 15)), Sym("hz"))  # nopep8
    CONVERSIONS[UnitsFrequency.HERTZ][UnitsFrequency.GIGAHERTZ] = \
        Mul(Pow(10, 9), Sym("hz"))  # nopep8
    CONVERSIONS[UnitsFrequency.HERTZ][UnitsFrequency.HECTOHERTZ] = \
        Mul(Int(100), Sym("hz"))  # nopep8
    CONVERSIONS[UnitsFrequency.HERTZ][UnitsFrequency.KILOHERTZ] = \
        Mul(Int(1000), Sym("hz"))  # nopep8
    CONVERSIONS[UnitsFrequency.HERTZ][UnitsFrequency.MEGAHERTZ] = \
        Mul(Pow(10, 6), Sym("hz"))  # nopep8
    CONVERSIONS[UnitsFrequency.HERTZ][UnitsFrequency.MICROHERTZ] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("hz"))  # nopep8
    CONVERSIONS[UnitsFrequency.HERTZ][UnitsFrequency.MILLIHERTZ] = \
        Mul(Rat(Int(1), Int(1000)), Sym("hz"))  # nopep8
    CONVERSIONS[UnitsFrequency.HERTZ][UnitsFrequency.NANOHERTZ] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("hz"))  # nopep8
    CONVERSIONS[UnitsFrequency.HERTZ][UnitsFrequency.PETAHERTZ] = \
        Mul(Pow(10, 15), Sym("hz"))  # nopep8
    CONVERSIONS[UnitsFrequency.HERTZ][UnitsFrequency.PICOHERTZ] = \
        Mul(Rat(Int(1), Pow(10, 12)), Sym("hz"))  # nopep8
    CONVERSIONS[UnitsFrequency.HERTZ][UnitsFrequency.TERAHERTZ] = \
        Mul(Pow(10, 12), Sym("hz"))  # nopep8
    CONVERSIONS[UnitsFrequency.HERTZ][UnitsFrequency.YOCTOHERTZ] = \
        Mul(Rat(Int(1), Pow(10, 24)), Sym("hz"))  # nopep8
    CONVERSIONS[UnitsFrequency.HERTZ][UnitsFrequency.YOTTAHERTZ] = \
        Mul(Pow(10, 24), Sym("hz"))  # nopep8
    CONVERSIONS[UnitsFrequency.HERTZ][UnitsFrequency.ZEPTOHERTZ] = \
        Mul(Rat(Int(1), Pow(10, 21)), Sym("hz"))  # nopep8
    CONVERSIONS[UnitsFrequency.HERTZ][UnitsFrequency.ZETTAHERTZ] = \
        Mul(Pow(10, 21), Sym("hz"))  # nopep8
    CONVERSIONS[UnitsFrequency.KILOHERTZ][UnitsFrequency.ATTOHERTZ] = \
        Mul(Rat(Int(1), Pow(10, 21)), Sym("kilohz"))  # nopep8
    CONVERSIONS[UnitsFrequency.KILOHERTZ][UnitsFrequency.CENTIHERTZ] = \
        Mul(Rat(Int(1), Pow(10, 5)), Sym("kilohz"))  # nopep8
    CONVERSIONS[UnitsFrequency.KILOHERTZ][UnitsFrequency.DECAHERTZ] = \
        Mul(Rat(Int(1), Int(100)), Sym("kilohz"))  # nopep8
    CONVERSIONS[UnitsFrequency.KILOHERTZ][UnitsFrequency.DECIHERTZ] = \
        Mul(Rat(Int(1), Pow(10, 4)), Sym("kilohz"))  # nopep8
    CONVERSIONS[UnitsFrequency.KILOHERTZ][UnitsFrequency.EXAHERTZ] = \
        Mul(Pow(10, 15), Sym("kilohz"))  # nopep8
    CONVERSIONS[UnitsFrequency.KILOHERTZ][UnitsFrequency.FEMTOHERTZ] = \
        Mul(Rat(Int(1), Pow(10, 18)), Sym("kilohz"))  # nopep8
    CONVERSIONS[UnitsFrequency.KILOHERTZ][UnitsFrequency.GIGAHERTZ] = \
        Mul(Pow(10, 6), Sym("kilohz"))  # nopep8
    CONVERSIONS[UnitsFrequency.KILOHERTZ][UnitsFrequency.HECTOHERTZ] = \
        Mul(Rat(Int(1), Int(10)), Sym("kilohz"))  # nopep8
    CONVERSIONS[UnitsFrequency.KILOHERTZ][UnitsFrequency.HERTZ] = \
        Mul(Rat(Int(1), Int(1000)), Sym("kilohz"))  # nopep8
    CONVERSIONS[UnitsFrequency.KILOHERTZ][UnitsFrequency.MEGAHERTZ] = \
        Mul(Int(1000), Sym("kilohz"))  # nopep8
    CONVERSIONS[UnitsFrequency.KILOHERTZ][UnitsFrequency.MICROHERTZ] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("kilohz"))  # nopep8
    CONVERSIONS[UnitsFrequency.KILOHERTZ][UnitsFrequency.MILLIHERTZ] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("kilohz"))  # nopep8
    CONVERSIONS[UnitsFrequency.KILOHERTZ][UnitsFrequency.NANOHERTZ] = \
        Mul(Rat(Int(1), Pow(10, 12)), Sym("kilohz"))  # nopep8
    CONVERSIONS[UnitsFrequency.KILOHERTZ][UnitsFrequency.PETAHERTZ] = \
        Mul(Pow(10, 12), Sym("kilohz"))  # nopep8
    CONVERSIONS[UnitsFrequency.KILOHERTZ][UnitsFrequency.PICOHERTZ] = \
        Mul(Rat(Int(1), Pow(10, 15)), Sym("kilohz"))  # nopep8
    CONVERSIONS[UnitsFrequency.KILOHERTZ][UnitsFrequency.TERAHERTZ] = \
        Mul(Pow(10, 9), Sym("kilohz"))  # nopep8
    CONVERSIONS[UnitsFrequency.KILOHERTZ][UnitsFrequency.YOCTOHERTZ] = \
        Mul(Rat(Int(1), Pow(10, 27)), Sym("kilohz"))  # nopep8
    CONVERSIONS[UnitsFrequency.KILOHERTZ][UnitsFrequency.YOTTAHERTZ] = \
        Mul(Pow(10, 21), Sym("kilohz"))  # nopep8
    CONVERSIONS[UnitsFrequency.KILOHERTZ][UnitsFrequency.ZEPTOHERTZ] = \
        Mul(Rat(Int(1), Pow(10, 24)), Sym("kilohz"))  # nopep8
    CONVERSIONS[UnitsFrequency.KILOHERTZ][UnitsFrequency.ZETTAHERTZ] = \
        Mul(Pow(10, 18), Sym("kilohz"))  # nopep8
    CONVERSIONS[UnitsFrequency.MEGAHERTZ][UnitsFrequency.ATTOHERTZ] = \
        Mul(Rat(Int(1), Pow(10, 24)), Sym("megahz"))  # nopep8
    CONVERSIONS[UnitsFrequency.MEGAHERTZ][UnitsFrequency.CENTIHERTZ] = \
        Mul(Rat(Int(1), Pow(10, 8)), Sym("megahz"))  # nopep8
    CONVERSIONS[UnitsFrequency.MEGAHERTZ][UnitsFrequency.DECAHERTZ] = \
        Mul(Rat(Int(1), Pow(10, 5)), Sym("megahz"))  # nopep8
    CONVERSIONS[UnitsFrequency.MEGAHERTZ][UnitsFrequency.DECIHERTZ] = \
        Mul(Rat(Int(1), Pow(10, 7)), Sym("megahz"))  # nopep8
    CONVERSIONS[UnitsFrequency.MEGAHERTZ][UnitsFrequency.EXAHERTZ] = \
        Mul(Pow(10, 12), Sym("megahz"))  # nopep8
    CONVERSIONS[UnitsFrequency.MEGAHERTZ][UnitsFrequency.FEMTOHERTZ] = \
        Mul(Rat(Int(1), Pow(10, 21)), Sym("megahz"))  # nopep8
    CONVERSIONS[UnitsFrequency.MEGAHERTZ][UnitsFrequency.GIGAHERTZ] = \
        Mul(Int(1000), Sym("megahz"))  # nopep8
    CONVERSIONS[UnitsFrequency.MEGAHERTZ][UnitsFrequency.HECTOHERTZ] = \
        Mul(Rat(Int(1), Pow(10, 4)), Sym("megahz"))  # nopep8
    CONVERSIONS[UnitsFrequency.MEGAHERTZ][UnitsFrequency.HERTZ] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("megahz"))  # nopep8
    CONVERSIONS[UnitsFrequency.MEGAHERTZ][UnitsFrequency.KILOHERTZ] = \
        Mul(Rat(Int(1), Int(1000)), Sym("megahz"))  # nopep8
    CONVERSIONS[UnitsFrequency.MEGAHERTZ][UnitsFrequency.MICROHERTZ] = \
        Mul(Rat(Int(1), Pow(10, 12)), Sym("megahz"))  # nopep8
    CONVERSIONS[UnitsFrequency.MEGAHERTZ][UnitsFrequency.MILLIHERTZ] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("megahz"))  # nopep8
    CONVERSIONS[UnitsFrequency.MEGAHERTZ][UnitsFrequency.NANOHERTZ] = \
        Mul(Rat(Int(1), Pow(10, 15)), Sym("megahz"))  # nopep8
    CONVERSIONS[UnitsFrequency.MEGAHERTZ][UnitsFrequency.PETAHERTZ] = \
        Mul(Pow(10, 9), Sym("megahz"))  # nopep8
    CONVERSIONS[UnitsFrequency.MEGAHERTZ][UnitsFrequency.PICOHERTZ] = \
        Mul(Rat(Int(1), Pow(10, 18)), Sym("megahz"))  # nopep8
    CONVERSIONS[UnitsFrequency.MEGAHERTZ][UnitsFrequency.TERAHERTZ] = \
        Mul(Pow(10, 6), Sym("megahz"))  # nopep8
    CONVERSIONS[UnitsFrequency.MEGAHERTZ][UnitsFrequency.YOCTOHERTZ] = \
        Mul(Rat(Int(1), Pow(10, 30)), Sym("megahz"))  # nopep8
    CONVERSIONS[UnitsFrequency.MEGAHERTZ][UnitsFrequency.YOTTAHERTZ] = \
        Mul(Pow(10, 18), Sym("megahz"))  # nopep8
    CONVERSIONS[UnitsFrequency.MEGAHERTZ][UnitsFrequency.ZEPTOHERTZ] = \
        Mul(Rat(Int(1), Pow(10, 27)), Sym("megahz"))  # nopep8
    CONVERSIONS[UnitsFrequency.MEGAHERTZ][UnitsFrequency.ZETTAHERTZ] = \
        Mul(Pow(10, 15), Sym("megahz"))  # nopep8
    CONVERSIONS[UnitsFrequency.MICROHERTZ][UnitsFrequency.ATTOHERTZ] = \
        Mul(Rat(Int(1), Pow(10, 12)), Sym("microhz"))  # nopep8
    CONVERSIONS[UnitsFrequency.MICROHERTZ][UnitsFrequency.CENTIHERTZ] = \
        Mul(Pow(10, 4), Sym("microhz"))  # nopep8
    CONVERSIONS[UnitsFrequency.MICROHERTZ][UnitsFrequency.DECAHERTZ] = \
        Mul(Pow(10, 7), Sym("microhz"))  # nopep8
    CONVERSIONS[UnitsFrequency.MICROHERTZ][UnitsFrequency.DECIHERTZ] = \
        Mul(Pow(10, 5), Sym("microhz"))  # nopep8
    CONVERSIONS[UnitsFrequency.MICROHERTZ][UnitsFrequency.EXAHERTZ] = \
        Mul(Pow(10, 24), Sym("microhz"))  # nopep8
    CONVERSIONS[UnitsFrequency.MICROHERTZ][UnitsFrequency.FEMTOHERTZ] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("microhz"))  # nopep8
    CONVERSIONS[UnitsFrequency.MICROHERTZ][UnitsFrequency.GIGAHERTZ] = \
        Mul(Pow(10, 15), Sym("microhz"))  # nopep8
    CONVERSIONS[UnitsFrequency.MICROHERTZ][UnitsFrequency.HECTOHERTZ] = \
        Mul(Pow(10, 8), Sym("microhz"))  # nopep8
    CONVERSIONS[UnitsFrequency.MICROHERTZ][UnitsFrequency.HERTZ] = \
        Mul(Pow(10, 6), Sym("microhz"))  # nopep8
    CONVERSIONS[UnitsFrequency.MICROHERTZ][UnitsFrequency.KILOHERTZ] = \
        Mul(Pow(10, 9), Sym("microhz"))  # nopep8
    CONVERSIONS[UnitsFrequency.MICROHERTZ][UnitsFrequency.MEGAHERTZ] = \
        Mul(Pow(10, 12), Sym("microhz"))  # nopep8
    CONVERSIONS[UnitsFrequency.MICROHERTZ][UnitsFrequency.MILLIHERTZ] = \
        Mul(Int(1000), Sym("microhz"))  # nopep8
    CONVERSIONS[UnitsFrequency.MICROHERTZ][UnitsFrequency.NANOHERTZ] = \
        Mul(Rat(Int(1), Int(1000)), Sym("microhz"))  # nopep8
    CONVERSIONS[UnitsFrequency.MICROHERTZ][UnitsFrequency.PETAHERTZ] = \
        Mul(Pow(10, 21), Sym("microhz"))  # nopep8
    CONVERSIONS[UnitsFrequency.MICROHERTZ][UnitsFrequency.PICOHERTZ] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("microhz"))  # nopep8
    CONVERSIONS[UnitsFrequency.MICROHERTZ][UnitsFrequency.TERAHERTZ] = \
        Mul(Pow(10, 18), Sym("microhz"))  # nopep8
    CONVERSIONS[UnitsFrequency.MICROHERTZ][UnitsFrequency.YOCTOHERTZ] = \
        Mul(Rat(Int(1), Pow(10, 18)), Sym("microhz"))  # nopep8
    CONVERSIONS[UnitsFrequency.MICROHERTZ][UnitsFrequency.YOTTAHERTZ] = \
        Mul(Pow(10, 30), Sym("microhz"))  # nopep8
    CONVERSIONS[UnitsFrequency.MICROHERTZ][UnitsFrequency.ZEPTOHERTZ] = \
        Mul(Rat(Int(1), Pow(10, 15)), Sym("microhz"))  # nopep8
    CONVERSIONS[UnitsFrequency.MICROHERTZ][UnitsFrequency.ZETTAHERTZ] = \
        Mul(Pow(10, 27), Sym("microhz"))  # nopep8
    CONVERSIONS[UnitsFrequency.MILLIHERTZ][UnitsFrequency.ATTOHERTZ] = \
        Mul(Rat(Int(1), Pow(10, 15)), Sym("millihz"))  # nopep8
    CONVERSIONS[UnitsFrequency.MILLIHERTZ][UnitsFrequency.CENTIHERTZ] = \
        Mul(Int(10), Sym("millihz"))  # nopep8
    CONVERSIONS[UnitsFrequency.MILLIHERTZ][UnitsFrequency.DECAHERTZ] = \
        Mul(Pow(10, 4), Sym("millihz"))  # nopep8
    CONVERSIONS[UnitsFrequency.MILLIHERTZ][UnitsFrequency.DECIHERTZ] = \
        Mul(Int(100), Sym("millihz"))  # nopep8
    CONVERSIONS[UnitsFrequency.MILLIHERTZ][UnitsFrequency.EXAHERTZ] = \
        Mul(Pow(10, 21), Sym("millihz"))  # nopep8
    CONVERSIONS[UnitsFrequency.MILLIHERTZ][UnitsFrequency.FEMTOHERTZ] = \
        Mul(Rat(Int(1), Pow(10, 12)), Sym("millihz"))  # nopep8
    CONVERSIONS[UnitsFrequency.MILLIHERTZ][UnitsFrequency.GIGAHERTZ] = \
        Mul(Pow(10, 12), Sym("millihz"))  # nopep8
    CONVERSIONS[UnitsFrequency.MILLIHERTZ][UnitsFrequency.HECTOHERTZ] = \
        Mul(Pow(10, 5), Sym("millihz"))  # nopep8
    CONVERSIONS[UnitsFrequency.MILLIHERTZ][UnitsFrequency.HERTZ] = \
        Mul(Int(1000), Sym("millihz"))  # nopep8
    CONVERSIONS[UnitsFrequency.MILLIHERTZ][UnitsFrequency.KILOHERTZ] = \
        Mul(Pow(10, 6), Sym("millihz"))  # nopep8
    CONVERSIONS[UnitsFrequency.MILLIHERTZ][UnitsFrequency.MEGAHERTZ] = \
        Mul(Pow(10, 9), Sym("millihz"))  # nopep8
    CONVERSIONS[UnitsFrequency.MILLIHERTZ][UnitsFrequency.MICROHERTZ] = \
        Mul(Rat(Int(1), Int(1000)), Sym("millihz"))  # nopep8
    CONVERSIONS[UnitsFrequency.MILLIHERTZ][UnitsFrequency.NANOHERTZ] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("millihz"))  # nopep8
    CONVERSIONS[UnitsFrequency.MILLIHERTZ][UnitsFrequency.PETAHERTZ] = \
        Mul(Pow(10, 18), Sym("millihz"))  # nopep8
    CONVERSIONS[UnitsFrequency.MILLIHERTZ][UnitsFrequency.PICOHERTZ] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("millihz"))  # nopep8
    CONVERSIONS[UnitsFrequency.MILLIHERTZ][UnitsFrequency.TERAHERTZ] = \
        Mul(Pow(10, 15), Sym("millihz"))  # nopep8
    CONVERSIONS[UnitsFrequency.MILLIHERTZ][UnitsFrequency.YOCTOHERTZ] = \
        Mul(Rat(Int(1), Pow(10, 21)), Sym("millihz"))  # nopep8
    CONVERSIONS[UnitsFrequency.MILLIHERTZ][UnitsFrequency.YOTTAHERTZ] = \
        Mul(Pow(10, 27), Sym("millihz"))  # nopep8
    CONVERSIONS[UnitsFrequency.MILLIHERTZ][UnitsFrequency.ZEPTOHERTZ] = \
        Mul(Rat(Int(1), Pow(10, 18)), Sym("millihz"))  # nopep8
    CONVERSIONS[UnitsFrequency.MILLIHERTZ][UnitsFrequency.ZETTAHERTZ] = \
        Mul(Pow(10, 24), Sym("millihz"))  # nopep8
    CONVERSIONS[UnitsFrequency.NANOHERTZ][UnitsFrequency.ATTOHERTZ] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("nanohz"))  # nopep8
    CONVERSIONS[UnitsFrequency.NANOHERTZ][UnitsFrequency.CENTIHERTZ] = \
        Mul(Pow(10, 7), Sym("nanohz"))  # nopep8
    CONVERSIONS[UnitsFrequency.NANOHERTZ][UnitsFrequency.DECAHERTZ] = \
        Mul(Pow(10, 10), Sym("nanohz"))  # nopep8
    CONVERSIONS[UnitsFrequency.NANOHERTZ][UnitsFrequency.DECIHERTZ] = \
        Mul(Pow(10, 8), Sym("nanohz"))  # nopep8
    CONVERSIONS[UnitsFrequency.NANOHERTZ][UnitsFrequency.EXAHERTZ] = \
        Mul(Pow(10, 27), Sym("nanohz"))  # nopep8
    CONVERSIONS[UnitsFrequency.NANOHERTZ][UnitsFrequency.FEMTOHERTZ] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("nanohz"))  # nopep8
    CONVERSIONS[UnitsFrequency.NANOHERTZ][UnitsFrequency.GIGAHERTZ] = \
        Mul(Pow(10, 18), Sym("nanohz"))  # nopep8
    CONVERSIONS[UnitsFrequency.NANOHERTZ][UnitsFrequency.HECTOHERTZ] = \
        Mul(Pow(10, 11), Sym("nanohz"))  # nopep8
    CONVERSIONS[UnitsFrequency.NANOHERTZ][UnitsFrequency.HERTZ] = \
        Mul(Pow(10, 9), Sym("nanohz"))  # nopep8
    CONVERSIONS[UnitsFrequency.NANOHERTZ][UnitsFrequency.KILOHERTZ] = \
        Mul(Pow(10, 12), Sym("nanohz"))  # nopep8
    CONVERSIONS[UnitsFrequency.NANOHERTZ][UnitsFrequency.MEGAHERTZ] = \
        Mul(Pow(10, 15), Sym("nanohz"))  # nopep8
    CONVERSIONS[UnitsFrequency.NANOHERTZ][UnitsFrequency.MICROHERTZ] = \
        Mul(Int(1000), Sym("nanohz"))  # nopep8
    CONVERSIONS[UnitsFrequency.NANOHERTZ][UnitsFrequency.MILLIHERTZ] = \
        Mul(Pow(10, 6), Sym("nanohz"))  # nopep8
    CONVERSIONS[UnitsFrequency.NANOHERTZ][UnitsFrequency.PETAHERTZ] = \
        Mul(Pow(10, 24), Sym("nanohz"))  # nopep8
    CONVERSIONS[UnitsFrequency.NANOHERTZ][UnitsFrequency.PICOHERTZ] = \
        Mul(Rat(Int(1), Int(1000)), Sym("nanohz"))  # nopep8
    CONVERSIONS[UnitsFrequency.NANOHERTZ][UnitsFrequency.TERAHERTZ] = \
        Mul(Pow(10, 21), Sym("nanohz"))  # nopep8
    CONVERSIONS[UnitsFrequency.NANOHERTZ][UnitsFrequency.YOCTOHERTZ] = \
        Mul(Rat(Int(1), Pow(10, 15)), Sym("nanohz"))  # nopep8
    CONVERSIONS[UnitsFrequency.NANOHERTZ][UnitsFrequency.YOTTAHERTZ] = \
        Mul(Pow(10, 33), Sym("nanohz"))  # nopep8
    CONVERSIONS[UnitsFrequency.NANOHERTZ][UnitsFrequency.ZEPTOHERTZ] = \
        Mul(Rat(Int(1), Pow(10, 12)), Sym("nanohz"))  # nopep8
    CONVERSIONS[UnitsFrequency.NANOHERTZ][UnitsFrequency.ZETTAHERTZ] = \
        Mul(Pow(10, 30), Sym("nanohz"))  # nopep8
    CONVERSIONS[UnitsFrequency.PETAHERTZ][UnitsFrequency.ATTOHERTZ] = \
        Mul(Rat(Int(1), Pow(10, 33)), Sym("petahz"))  # nopep8
    CONVERSIONS[UnitsFrequency.PETAHERTZ][UnitsFrequency.CENTIHERTZ] = \
        Mul(Rat(Int(1), Pow(10, 17)), Sym("petahz"))  # nopep8
    CONVERSIONS[UnitsFrequency.PETAHERTZ][UnitsFrequency.DECAHERTZ] = \
        Mul(Rat(Int(1), Pow(10, 14)), Sym("petahz"))  # nopep8
    CONVERSIONS[UnitsFrequency.PETAHERTZ][UnitsFrequency.DECIHERTZ] = \
        Mul(Rat(Int(1), Pow(10, 16)), Sym("petahz"))  # nopep8
    CONVERSIONS[UnitsFrequency.PETAHERTZ][UnitsFrequency.EXAHERTZ] = \
        Mul(Int(1000), Sym("petahz"))  # nopep8
    CONVERSIONS[UnitsFrequency.PETAHERTZ][UnitsFrequency.FEMTOHERTZ] = \
        Mul(Rat(Int(1), Pow(10, 30)), Sym("petahz"))  # nopep8
    CONVERSIONS[UnitsFrequency.PETAHERTZ][UnitsFrequency.GIGAHERTZ] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("petahz"))  # nopep8
    CONVERSIONS[UnitsFrequency.PETAHERTZ][UnitsFrequency.HECTOHERTZ] = \
        Mul(Rat(Int(1), Pow(10, 13)), Sym("petahz"))  # nopep8
    CONVERSIONS[UnitsFrequency.PETAHERTZ][UnitsFrequency.HERTZ] = \
        Mul(Rat(Int(1), Pow(10, 15)), Sym("petahz"))  # nopep8
    CONVERSIONS[UnitsFrequency.PETAHERTZ][UnitsFrequency.KILOHERTZ] = \
        Mul(Rat(Int(1), Pow(10, 12)), Sym("petahz"))  # nopep8
    CONVERSIONS[UnitsFrequency.PETAHERTZ][UnitsFrequency.MEGAHERTZ] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("petahz"))  # nopep8
    CONVERSIONS[UnitsFrequency.PETAHERTZ][UnitsFrequency.MICROHERTZ] = \
        Mul(Rat(Int(1), Pow(10, 21)), Sym("petahz"))  # nopep8
    CONVERSIONS[UnitsFrequency.PETAHERTZ][UnitsFrequency.MILLIHERTZ] = \
        Mul(Rat(Int(1), Pow(10, 18)), Sym("petahz"))  # nopep8
    CONVERSIONS[UnitsFrequency.PETAHERTZ][UnitsFrequency.NANOHERTZ] = \
        Mul(Rat(Int(1), Pow(10, 24)), Sym("petahz"))  # nopep8
    CONVERSIONS[UnitsFrequency.PETAHERTZ][UnitsFrequency.PICOHERTZ] = \
        Mul(Rat(Int(1), Pow(10, 27)), Sym("petahz"))  # nopep8
    CONVERSIONS[UnitsFrequency.PETAHERTZ][UnitsFrequency.TERAHERTZ] = \
        Mul(Rat(Int(1), Int(1000)), Sym("petahz"))  # nopep8
    CONVERSIONS[UnitsFrequency.PETAHERTZ][UnitsFrequency.YOCTOHERTZ] = \
        Mul(Rat(Int(1), Pow(10, 39)), Sym("petahz"))  # nopep8
    CONVERSIONS[UnitsFrequency.PETAHERTZ][UnitsFrequency.YOTTAHERTZ] = \
        Mul(Pow(10, 9), Sym("petahz"))  # nopep8
    CONVERSIONS[UnitsFrequency.PETAHERTZ][UnitsFrequency.ZEPTOHERTZ] = \
        Mul(Rat(Int(1), Pow(10, 36)), Sym("petahz"))  # nopep8
    CONVERSIONS[UnitsFrequency.PETAHERTZ][UnitsFrequency.ZETTAHERTZ] = \
        Mul(Pow(10, 6), Sym("petahz"))  # nopep8
    CONVERSIONS[UnitsFrequency.PICOHERTZ][UnitsFrequency.ATTOHERTZ] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("picohz"))  # nopep8
    CONVERSIONS[UnitsFrequency.PICOHERTZ][UnitsFrequency.CENTIHERTZ] = \
        Mul(Pow(10, 10), Sym("picohz"))  # nopep8
    CONVERSIONS[UnitsFrequency.PICOHERTZ][UnitsFrequency.DECAHERTZ] = \
        Mul(Pow(10, 13), Sym("picohz"))  # nopep8
    CONVERSIONS[UnitsFrequency.PICOHERTZ][UnitsFrequency.DECIHERTZ] = \
        Mul(Pow(10, 11), Sym("picohz"))  # nopep8
    CONVERSIONS[UnitsFrequency.PICOHERTZ][UnitsFrequency.EXAHERTZ] = \
        Mul(Pow(10, 30), Sym("picohz"))  # nopep8
    CONVERSIONS[UnitsFrequency.PICOHERTZ][UnitsFrequency.FEMTOHERTZ] = \
        Mul(Rat(Int(1), Int(1000)), Sym("picohz"))  # nopep8
    CONVERSIONS[UnitsFrequency.PICOHERTZ][UnitsFrequency.GIGAHERTZ] = \
        Mul(Pow(10, 21), Sym("picohz"))  # nopep8
    CONVERSIONS[UnitsFrequency.PICOHERTZ][UnitsFrequency.HECTOHERTZ] = \
        Mul(Pow(10, 14), Sym("picohz"))  # nopep8
    CONVERSIONS[UnitsFrequency.PICOHERTZ][UnitsFrequency.HERTZ] = \
        Mul(Pow(10, 12), Sym("picohz"))  # nopep8
    CONVERSIONS[UnitsFrequency.PICOHERTZ][UnitsFrequency.KILOHERTZ] = \
        Mul(Pow(10, 15), Sym("picohz"))  # nopep8
    CONVERSIONS[UnitsFrequency.PICOHERTZ][UnitsFrequency.MEGAHERTZ] = \
        Mul(Pow(10, 18), Sym("picohz"))  # nopep8
    CONVERSIONS[UnitsFrequency.PICOHERTZ][UnitsFrequency.MICROHERTZ] = \
        Mul(Pow(10, 6), Sym("picohz"))  # nopep8
    CONVERSIONS[UnitsFrequency.PICOHERTZ][UnitsFrequency.MILLIHERTZ] = \
        Mul(Pow(10, 9), Sym("picohz"))  # nopep8
    CONVERSIONS[UnitsFrequency.PICOHERTZ][UnitsFrequency.NANOHERTZ] = \
        Mul(Int(1000), Sym("picohz"))  # nopep8
    CONVERSIONS[UnitsFrequency.PICOHERTZ][UnitsFrequency.PETAHERTZ] = \
        Mul(Pow(10, 27), Sym("picohz"))  # nopep8
    CONVERSIONS[UnitsFrequency.PICOHERTZ][UnitsFrequency.TERAHERTZ] = \
        Mul(Pow(10, 24), Sym("picohz"))  # nopep8
    CONVERSIONS[UnitsFrequency.PICOHERTZ][UnitsFrequency.YOCTOHERTZ] = \
        Mul(Rat(Int(1), Pow(10, 12)), Sym("picohz"))  # nopep8
    CONVERSIONS[UnitsFrequency.PICOHERTZ][UnitsFrequency.YOTTAHERTZ] = \
        Mul(Pow(10, 36), Sym("picohz"))  # nopep8
    CONVERSIONS[UnitsFrequency.PICOHERTZ][UnitsFrequency.ZEPTOHERTZ] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("picohz"))  # nopep8
    CONVERSIONS[UnitsFrequency.PICOHERTZ][UnitsFrequency.ZETTAHERTZ] = \
        Mul(Pow(10, 33), Sym("picohz"))  # nopep8
    CONVERSIONS[UnitsFrequency.TERAHERTZ][UnitsFrequency.ATTOHERTZ] = \
        Mul(Rat(Int(1), Pow(10, 30)), Sym("terahz"))  # nopep8
    CONVERSIONS[UnitsFrequency.TERAHERTZ][UnitsFrequency.CENTIHERTZ] = \
        Mul(Rat(Int(1), Pow(10, 14)), Sym("terahz"))  # nopep8
    CONVERSIONS[UnitsFrequency.TERAHERTZ][UnitsFrequency.DECAHERTZ] = \
        Mul(Rat(Int(1), Pow(10, 11)), Sym("terahz"))  # nopep8
    CONVERSIONS[UnitsFrequency.TERAHERTZ][UnitsFrequency.DECIHERTZ] = \
        Mul(Rat(Int(1), Pow(10, 13)), Sym("terahz"))  # nopep8
    CONVERSIONS[UnitsFrequency.TERAHERTZ][UnitsFrequency.EXAHERTZ] = \
        Mul(Pow(10, 6), Sym("terahz"))  # nopep8
    CONVERSIONS[UnitsFrequency.TERAHERTZ][UnitsFrequency.FEMTOHERTZ] = \
        Mul(Rat(Int(1), Pow(10, 27)), Sym("terahz"))  # nopep8
    CONVERSIONS[UnitsFrequency.TERAHERTZ][UnitsFrequency.GIGAHERTZ] = \
        Mul(Rat(Int(1), Int(1000)), Sym("terahz"))  # nopep8
    CONVERSIONS[UnitsFrequency.TERAHERTZ][UnitsFrequency.HECTOHERTZ] = \
        Mul(Rat(Int(1), Pow(10, 10)), Sym("terahz"))  # nopep8
    CONVERSIONS[UnitsFrequency.TERAHERTZ][UnitsFrequency.HERTZ] = \
        Mul(Rat(Int(1), Pow(10, 12)), Sym("terahz"))  # nopep8
    CONVERSIONS[UnitsFrequency.TERAHERTZ][UnitsFrequency.KILOHERTZ] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("terahz"))  # nopep8
    CONVERSIONS[UnitsFrequency.TERAHERTZ][UnitsFrequency.MEGAHERTZ] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("terahz"))  # nopep8
    CONVERSIONS[UnitsFrequency.TERAHERTZ][UnitsFrequency.MICROHERTZ] = \
        Mul(Rat(Int(1), Pow(10, 18)), Sym("terahz"))  # nopep8
    CONVERSIONS[UnitsFrequency.TERAHERTZ][UnitsFrequency.MILLIHERTZ] = \
        Mul(Rat(Int(1), Pow(10, 15)), Sym("terahz"))  # nopep8
    CONVERSIONS[UnitsFrequency.TERAHERTZ][UnitsFrequency.NANOHERTZ] = \
        Mul(Rat(Int(1), Pow(10, 21)), Sym("terahz"))  # nopep8
    CONVERSIONS[UnitsFrequency.TERAHERTZ][UnitsFrequency.PETAHERTZ] = \
        Mul(Int(1000), Sym("terahz"))  # nopep8
    CONVERSIONS[UnitsFrequency.TERAHERTZ][UnitsFrequency.PICOHERTZ] = \
        Mul(Rat(Int(1), Pow(10, 24)), Sym("terahz"))  # nopep8
    CONVERSIONS[UnitsFrequency.TERAHERTZ][UnitsFrequency.YOCTOHERTZ] = \
        Mul(Rat(Int(1), Pow(10, 36)), Sym("terahz"))  # nopep8
    CONVERSIONS[UnitsFrequency.TERAHERTZ][UnitsFrequency.YOTTAHERTZ] = \
        Mul(Pow(10, 12), Sym("terahz"))  # nopep8
    CONVERSIONS[UnitsFrequency.TERAHERTZ][UnitsFrequency.ZEPTOHERTZ] = \
        Mul(Rat(Int(1), Pow(10, 33)), Sym("terahz"))  # nopep8
    CONVERSIONS[UnitsFrequency.TERAHERTZ][UnitsFrequency.ZETTAHERTZ] = \
        Mul(Pow(10, 9), Sym("terahz"))  # nopep8
    CONVERSIONS[UnitsFrequency.YOCTOHERTZ][UnitsFrequency.ATTOHERTZ] = \
        Mul(Pow(10, 6), Sym("yoctohz"))  # nopep8
    CONVERSIONS[UnitsFrequency.YOCTOHERTZ][UnitsFrequency.CENTIHERTZ] = \
        Mul(Pow(10, 22), Sym("yoctohz"))  # nopep8
    CONVERSIONS[UnitsFrequency.YOCTOHERTZ][UnitsFrequency.DECAHERTZ] = \
        Mul(Pow(10, 25), Sym("yoctohz"))  # nopep8
    CONVERSIONS[UnitsFrequency.YOCTOHERTZ][UnitsFrequency.DECIHERTZ] = \
        Mul(Pow(10, 23), Sym("yoctohz"))  # nopep8
    CONVERSIONS[UnitsFrequency.YOCTOHERTZ][UnitsFrequency.EXAHERTZ] = \
        Mul(Pow(10, 42), Sym("yoctohz"))  # nopep8
    CONVERSIONS[UnitsFrequency.YOCTOHERTZ][UnitsFrequency.FEMTOHERTZ] = \
        Mul(Pow(10, 9), Sym("yoctohz"))  # nopep8
    CONVERSIONS[UnitsFrequency.YOCTOHERTZ][UnitsFrequency.GIGAHERTZ] = \
        Mul(Pow(10, 33), Sym("yoctohz"))  # nopep8
    CONVERSIONS[UnitsFrequency.YOCTOHERTZ][UnitsFrequency.HECTOHERTZ] = \
        Mul(Pow(10, 26), Sym("yoctohz"))  # nopep8
    CONVERSIONS[UnitsFrequency.YOCTOHERTZ][UnitsFrequency.HERTZ] = \
        Mul(Pow(10, 24), Sym("yoctohz"))  # nopep8
    CONVERSIONS[UnitsFrequency.YOCTOHERTZ][UnitsFrequency.KILOHERTZ] = \
        Mul(Pow(10, 27), Sym("yoctohz"))  # nopep8
    CONVERSIONS[UnitsFrequency.YOCTOHERTZ][UnitsFrequency.MEGAHERTZ] = \
        Mul(Pow(10, 30), Sym("yoctohz"))  # nopep8
    CONVERSIONS[UnitsFrequency.YOCTOHERTZ][UnitsFrequency.MICROHERTZ] = \
        Mul(Pow(10, 18), Sym("yoctohz"))  # nopep8
    CONVERSIONS[UnitsFrequency.YOCTOHERTZ][UnitsFrequency.MILLIHERTZ] = \
        Mul(Pow(10, 21), Sym("yoctohz"))  # nopep8
    CONVERSIONS[UnitsFrequency.YOCTOHERTZ][UnitsFrequency.NANOHERTZ] = \
        Mul(Pow(10, 15), Sym("yoctohz"))  # nopep8
    CONVERSIONS[UnitsFrequency.YOCTOHERTZ][UnitsFrequency.PETAHERTZ] = \
        Mul(Pow(10, 39), Sym("yoctohz"))  # nopep8
    CONVERSIONS[UnitsFrequency.YOCTOHERTZ][UnitsFrequency.PICOHERTZ] = \
        Mul(Pow(10, 12), Sym("yoctohz"))  # nopep8
    CONVERSIONS[UnitsFrequency.YOCTOHERTZ][UnitsFrequency.TERAHERTZ] = \
        Mul(Pow(10, 36), Sym("yoctohz"))  # nopep8
    CONVERSIONS[UnitsFrequency.YOCTOHERTZ][UnitsFrequency.YOTTAHERTZ] = \
        Mul(Pow(10, 48), Sym("yoctohz"))  # nopep8
    CONVERSIONS[UnitsFrequency.YOCTOHERTZ][UnitsFrequency.ZEPTOHERTZ] = \
        Mul(Int(1000), Sym("yoctohz"))  # nopep8
    CONVERSIONS[UnitsFrequency.YOCTOHERTZ][UnitsFrequency.ZETTAHERTZ] = \
        Mul(Pow(10, 45), Sym("yoctohz"))  # nopep8
    CONVERSIONS[UnitsFrequency.YOTTAHERTZ][UnitsFrequency.ATTOHERTZ] = \
        Mul(Rat(Int(1), Pow(10, 42)), Sym("yottahz"))  # nopep8
    CONVERSIONS[UnitsFrequency.YOTTAHERTZ][UnitsFrequency.CENTIHERTZ] = \
        Mul(Rat(Int(1), Pow(10, 26)), Sym("yottahz"))  # nopep8
    CONVERSIONS[UnitsFrequency.YOTTAHERTZ][UnitsFrequency.DECAHERTZ] = \
        Mul(Rat(Int(1), Pow(10, 23)), Sym("yottahz"))  # nopep8
    CONVERSIONS[UnitsFrequency.YOTTAHERTZ][UnitsFrequency.DECIHERTZ] = \
        Mul(Rat(Int(1), Pow(10, 25)), Sym("yottahz"))  # nopep8
    CONVERSIONS[UnitsFrequency.YOTTAHERTZ][UnitsFrequency.EXAHERTZ] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("yottahz"))  # nopep8
    CONVERSIONS[UnitsFrequency.YOTTAHERTZ][UnitsFrequency.FEMTOHERTZ] = \
        Mul(Rat(Int(1), Pow(10, 39)), Sym("yottahz"))  # nopep8
    CONVERSIONS[UnitsFrequency.YOTTAHERTZ][UnitsFrequency.GIGAHERTZ] = \
        Mul(Rat(Int(1), Pow(10, 15)), Sym("yottahz"))  # nopep8
    CONVERSIONS[UnitsFrequency.YOTTAHERTZ][UnitsFrequency.HECTOHERTZ] = \
        Mul(Rat(Int(1), Pow(10, 22)), Sym("yottahz"))  # nopep8
    CONVERSIONS[UnitsFrequency.YOTTAHERTZ][UnitsFrequency.HERTZ] = \
        Mul(Rat(Int(1), Pow(10, 24)), Sym("yottahz"))  # nopep8
    CONVERSIONS[UnitsFrequency.YOTTAHERTZ][UnitsFrequency.KILOHERTZ] = \
        Mul(Rat(Int(1), Pow(10, 21)), Sym("yottahz"))  # nopep8
    CONVERSIONS[UnitsFrequency.YOTTAHERTZ][UnitsFrequency.MEGAHERTZ] = \
        Mul(Rat(Int(1), Pow(10, 18)), Sym("yottahz"))  # nopep8
    CONVERSIONS[UnitsFrequency.YOTTAHERTZ][UnitsFrequency.MICROHERTZ] = \
        Mul(Rat(Int(1), Pow(10, 30)), Sym("yottahz"))  # nopep8
    CONVERSIONS[UnitsFrequency.YOTTAHERTZ][UnitsFrequency.MILLIHERTZ] = \
        Mul(Rat(Int(1), Pow(10, 27)), Sym("yottahz"))  # nopep8
    CONVERSIONS[UnitsFrequency.YOTTAHERTZ][UnitsFrequency.NANOHERTZ] = \
        Mul(Rat(Int(1), Pow(10, 33)), Sym("yottahz"))  # nopep8
    CONVERSIONS[UnitsFrequency.YOTTAHERTZ][UnitsFrequency.PETAHERTZ] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("yottahz"))  # nopep8
    CONVERSIONS[UnitsFrequency.YOTTAHERTZ][UnitsFrequency.PICOHERTZ] = \
        Mul(Rat(Int(1), Pow(10, 36)), Sym("yottahz"))  # nopep8
    CONVERSIONS[UnitsFrequency.YOTTAHERTZ][UnitsFrequency.TERAHERTZ] = \
        Mul(Rat(Int(1), Pow(10, 12)), Sym("yottahz"))  # nopep8
    CONVERSIONS[UnitsFrequency.YOTTAHERTZ][UnitsFrequency.YOCTOHERTZ] = \
        Mul(Rat(Int(1), Pow(10, 48)), Sym("yottahz"))  # nopep8
    CONVERSIONS[UnitsFrequency.YOTTAHERTZ][UnitsFrequency.ZEPTOHERTZ] = \
        Mul(Rat(Int(1), Pow(10, 45)), Sym("yottahz"))  # nopep8
    CONVERSIONS[UnitsFrequency.YOTTAHERTZ][UnitsFrequency.ZETTAHERTZ] = \
        Mul(Rat(Int(1), Int(1000)), Sym("yottahz"))  # nopep8
    CONVERSIONS[UnitsFrequency.ZEPTOHERTZ][UnitsFrequency.ATTOHERTZ] = \
        Mul(Int(1000), Sym("zeptohz"))  # nopep8
    CONVERSIONS[UnitsFrequency.ZEPTOHERTZ][UnitsFrequency.CENTIHERTZ] = \
        Mul(Pow(10, 19), Sym("zeptohz"))  # nopep8
    CONVERSIONS[UnitsFrequency.ZEPTOHERTZ][UnitsFrequency.DECAHERTZ] = \
        Mul(Pow(10, 22), Sym("zeptohz"))  # nopep8
    CONVERSIONS[UnitsFrequency.ZEPTOHERTZ][UnitsFrequency.DECIHERTZ] = \
        Mul(Pow(10, 20), Sym("zeptohz"))  # nopep8
    CONVERSIONS[UnitsFrequency.ZEPTOHERTZ][UnitsFrequency.EXAHERTZ] = \
        Mul(Pow(10, 39), Sym("zeptohz"))  # nopep8
    CONVERSIONS[UnitsFrequency.ZEPTOHERTZ][UnitsFrequency.FEMTOHERTZ] = \
        Mul(Pow(10, 6), Sym("zeptohz"))  # nopep8
    CONVERSIONS[UnitsFrequency.ZEPTOHERTZ][UnitsFrequency.GIGAHERTZ] = \
        Mul(Pow(10, 30), Sym("zeptohz"))  # nopep8
    CONVERSIONS[UnitsFrequency.ZEPTOHERTZ][UnitsFrequency.HECTOHERTZ] = \
        Mul(Pow(10, 23), Sym("zeptohz"))  # nopep8
    CONVERSIONS[UnitsFrequency.ZEPTOHERTZ][UnitsFrequency.HERTZ] = \
        Mul(Pow(10, 21), Sym("zeptohz"))  # nopep8
    CONVERSIONS[UnitsFrequency.ZEPTOHERTZ][UnitsFrequency.KILOHERTZ] = \
        Mul(Pow(10, 24), Sym("zeptohz"))  # nopep8
    CONVERSIONS[UnitsFrequency.ZEPTOHERTZ][UnitsFrequency.MEGAHERTZ] = \
        Mul(Pow(10, 27), Sym("zeptohz"))  # nopep8
    CONVERSIONS[UnitsFrequency.ZEPTOHERTZ][UnitsFrequency.MICROHERTZ] = \
        Mul(Pow(10, 15), Sym("zeptohz"))  # nopep8
    CONVERSIONS[UnitsFrequency.ZEPTOHERTZ][UnitsFrequency.MILLIHERTZ] = \
        Mul(Pow(10, 18), Sym("zeptohz"))  # nopep8
    CONVERSIONS[UnitsFrequency.ZEPTOHERTZ][UnitsFrequency.NANOHERTZ] = \
        Mul(Pow(10, 12), Sym("zeptohz"))  # nopep8
    CONVERSIONS[UnitsFrequency.ZEPTOHERTZ][UnitsFrequency.PETAHERTZ] = \
        Mul(Pow(10, 36), Sym("zeptohz"))  # nopep8
    CONVERSIONS[UnitsFrequency.ZEPTOHERTZ][UnitsFrequency.PICOHERTZ] = \
        Mul(Pow(10, 9), Sym("zeptohz"))  # nopep8
    CONVERSIONS[UnitsFrequency.ZEPTOHERTZ][UnitsFrequency.TERAHERTZ] = \
        Mul(Pow(10, 33), Sym("zeptohz"))  # nopep8
    CONVERSIONS[UnitsFrequency.ZEPTOHERTZ][UnitsFrequency.YOCTOHERTZ] = \
        Mul(Rat(Int(1), Int(1000)), Sym("zeptohz"))  # nopep8
    CONVERSIONS[UnitsFrequency.ZEPTOHERTZ][UnitsFrequency.YOTTAHERTZ] = \
        Mul(Pow(10, 45), Sym("zeptohz"))  # nopep8
    CONVERSIONS[UnitsFrequency.ZEPTOHERTZ][UnitsFrequency.ZETTAHERTZ] = \
        Mul(Pow(10, 42), Sym("zeptohz"))  # nopep8
    CONVERSIONS[UnitsFrequency.ZETTAHERTZ][UnitsFrequency.ATTOHERTZ] = \
        Mul(Rat(Int(1), Pow(10, 39)), Sym("zettahz"))  # nopep8
    CONVERSIONS[UnitsFrequency.ZETTAHERTZ][UnitsFrequency.CENTIHERTZ] = \
        Mul(Rat(Int(1), Pow(10, 23)), Sym("zettahz"))  # nopep8
    CONVERSIONS[UnitsFrequency.ZETTAHERTZ][UnitsFrequency.DECAHERTZ] = \
        Mul(Rat(Int(1), Pow(10, 20)), Sym("zettahz"))  # nopep8
    CONVERSIONS[UnitsFrequency.ZETTAHERTZ][UnitsFrequency.DECIHERTZ] = \
        Mul(Rat(Int(1), Pow(10, 22)), Sym("zettahz"))  # nopep8
    CONVERSIONS[UnitsFrequency.ZETTAHERTZ][UnitsFrequency.EXAHERTZ] = \
        Mul(Rat(Int(1), Int(1000)), Sym("zettahz"))  # nopep8
    CONVERSIONS[UnitsFrequency.ZETTAHERTZ][UnitsFrequency.FEMTOHERTZ] = \
        Mul(Rat(Int(1), Pow(10, 36)), Sym("zettahz"))  # nopep8
    CONVERSIONS[UnitsFrequency.ZETTAHERTZ][UnitsFrequency.GIGAHERTZ] = \
        Mul(Rat(Int(1), Pow(10, 12)), Sym("zettahz"))  # nopep8
    CONVERSIONS[UnitsFrequency.ZETTAHERTZ][UnitsFrequency.HECTOHERTZ] = \
        Mul(Rat(Int(1), Pow(10, 19)), Sym("zettahz"))  # nopep8
    CONVERSIONS[UnitsFrequency.ZETTAHERTZ][UnitsFrequency.HERTZ] = \
        Mul(Rat(Int(1), Pow(10, 21)), Sym("zettahz"))  # nopep8
    CONVERSIONS[UnitsFrequency.ZETTAHERTZ][UnitsFrequency.KILOHERTZ] = \
        Mul(Rat(Int(1), Pow(10, 18)), Sym("zettahz"))  # nopep8
    CONVERSIONS[UnitsFrequency.ZETTAHERTZ][UnitsFrequency.MEGAHERTZ] = \
        Mul(Rat(Int(1), Pow(10, 15)), Sym("zettahz"))  # nopep8
    CONVERSIONS[UnitsFrequency.ZETTAHERTZ][UnitsFrequency.MICROHERTZ] = \
        Mul(Rat(Int(1), Pow(10, 27)), Sym("zettahz"))  # nopep8
    CONVERSIONS[UnitsFrequency.ZETTAHERTZ][UnitsFrequency.MILLIHERTZ] = \
        Mul(Rat(Int(1), Pow(10, 24)), Sym("zettahz"))  # nopep8
    CONVERSIONS[UnitsFrequency.ZETTAHERTZ][UnitsFrequency.NANOHERTZ] = \
        Mul(Rat(Int(1), Pow(10, 30)), Sym("zettahz"))  # nopep8
    CONVERSIONS[UnitsFrequency.ZETTAHERTZ][UnitsFrequency.PETAHERTZ] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("zettahz"))  # nopep8
    CONVERSIONS[UnitsFrequency.ZETTAHERTZ][UnitsFrequency.PICOHERTZ] = \
        Mul(Rat(Int(1), Pow(10, 33)), Sym("zettahz"))  # nopep8
    CONVERSIONS[UnitsFrequency.ZETTAHERTZ][UnitsFrequency.TERAHERTZ] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("zettahz"))  # nopep8
    CONVERSIONS[UnitsFrequency.ZETTAHERTZ][UnitsFrequency.YOCTOHERTZ] = \
        Mul(Rat(Int(1), Pow(10, 45)), Sym("zettahz"))  # nopep8
    CONVERSIONS[UnitsFrequency.ZETTAHERTZ][UnitsFrequency.YOTTAHERTZ] = \
        Mul(Int(1000), Sym("zettahz"))  # nopep8
    CONVERSIONS[UnitsFrequency.ZETTAHERTZ][UnitsFrequency.ZEPTOHERTZ] = \
        Mul(Rat(Int(1), Pow(10, 42)), Sym("zettahz"))  # nopep8

    SYMBOLS = dict()
    SYMBOLS["ATTOHERTZ"] = "aHz"
    SYMBOLS["CENTIHERTZ"] = "cHz"
    SYMBOLS["DECAHERTZ"] = "daHz"
    SYMBOLS["DECIHERTZ"] = "dHz"
    SYMBOLS["EXAHERTZ"] = "EHz"
    SYMBOLS["FEMTOHERTZ"] = "fHz"
    SYMBOLS["GIGAHERTZ"] = "GHz"
    SYMBOLS["HECTOHERTZ"] = "hHz"
    SYMBOLS["HERTZ"] = "Hz"
    SYMBOLS["KILOHERTZ"] = "kHz"
    SYMBOLS["MEGAHERTZ"] = "MHz"
    SYMBOLS["MICROHERTZ"] = "µHz"
    SYMBOLS["MILLIHERTZ"] = "mHz"
    SYMBOLS["NANOHERTZ"] = "nHz"
    SYMBOLS["PETAHERTZ"] = "PHz"
    SYMBOLS["PICOHERTZ"] = "pHz"
    SYMBOLS["TERAHERTZ"] = "THz"
    SYMBOLS["YOCTOHERTZ"] = "yHz"
    SYMBOLS["YOTTAHERTZ"] = "YHz"
    SYMBOLS["ZEPTOHERTZ"] = "zHz"
    SYMBOLS["ZETTAHERTZ"] = "ZHz"

    def __init__(self, value=None, unit=None):
        _omero_model.Frequency.__init__(self)
        if isinstance(value, _omero_model.FrequencyI):
            # This is a copy-constructor call.
            target = str(unit)
            targetUnit = getattr(UnitsFrequency, str(target))
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
        return FrequencyI.SYMBOLS.get(str(unit))

    def setUnit(self, unit, current=None):
        self._unit = unit

    def setValue(self, value, current=None):
        self._value = value

    def __str__(self):
        return self._base_string(self.getValue(), self.getUnit())

_omero_model.FrequencyI = FrequencyI
