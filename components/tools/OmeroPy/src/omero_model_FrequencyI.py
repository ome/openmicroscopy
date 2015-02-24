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

from omero.model.conversions import Add
from omero.model.conversions import Int
from omero.model.conversions import Mul
from omero.model.conversions import Pow
from omero.model.conversions import Rat
from omero.model.conversions import Sym


class FrequencyI(_omero_model.Frequency, UnitBase):

    CONVERSIONS = dict()
    CONVERSIONS["ATTOHERTZ:CENTIHERTZ"] = \
        Mul(Pow(10, 16), Sym("attohz"))
    CONVERSIONS["ATTOHERTZ:DECAHERTZ"] = \
        Mul(Pow(10, 19), Sym("attohz"))
    CONVERSIONS["ATTOHERTZ:DECIHERTZ"] = \
        Mul(Pow(10, 17), Sym("attohz"))
    CONVERSIONS["ATTOHERTZ:EXAHERTZ"] = \
        Mul(Pow(10, 36), Sym("attohz"))
    CONVERSIONS["ATTOHERTZ:FEMTOHERTZ"] = \
        Mul(Int(1000), Sym("attohz"))
    CONVERSIONS["ATTOHERTZ:GIGAHERTZ"] = \
        Mul(Pow(10, 27), Sym("attohz"))
    CONVERSIONS["ATTOHERTZ:HECTOHERTZ"] = \
        Mul(Pow(10, 20), Sym("attohz"))
    CONVERSIONS["ATTOHERTZ:HERTZ"] = \
        Mul(Pow(10, 18), Sym("attohz"))
    CONVERSIONS["ATTOHERTZ:KILOHERTZ"] = \
        Mul(Pow(10, 21), Sym("attohz"))
    CONVERSIONS["ATTOHERTZ:MEGAHERTZ"] = \
        Mul(Pow(10, 24), Sym("attohz"))
    CONVERSIONS["ATTOHERTZ:MICROHERTZ"] = \
        Mul(Pow(10, 12), Sym("attohz"))
    CONVERSIONS["ATTOHERTZ:MILLIHERTZ"] = \
        Mul(Pow(10, 15), Sym("attohz"))
    CONVERSIONS["ATTOHERTZ:NANOHERTZ"] = \
        Mul(Pow(10, 9), Sym("attohz"))
    CONVERSIONS["ATTOHERTZ:PETAHERTZ"] = \
        Mul(Pow(10, 33), Sym("attohz"))
    CONVERSIONS["ATTOHERTZ:PICOHERTZ"] = \
        Mul(Pow(10, 6), Sym("attohz"))
    CONVERSIONS["ATTOHERTZ:TERAHERTZ"] = \
        Mul(Pow(10, 30), Sym("attohz"))
    CONVERSIONS["ATTOHERTZ:YOCTOHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("attohz"))
    CONVERSIONS["ATTOHERTZ:YOTTAHERTZ"] = \
        Mul(Pow(10, 42), Sym("attohz"))
    CONVERSIONS["ATTOHERTZ:ZEPTOHERTZ"] = \
        Mul(Rat(Int(1), Int(1000)), Sym("attohz"))
    CONVERSIONS["ATTOHERTZ:ZETTAHERTZ"] = \
        Mul(Pow(10, 39), Sym("attohz"))
    CONVERSIONS["CENTIHERTZ:ATTOHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 16)), Sym("centihz"))
    CONVERSIONS["CENTIHERTZ:DECAHERTZ"] = \
        Mul(Int(1000), Sym("centihz"))
    CONVERSIONS["CENTIHERTZ:DECIHERTZ"] = \
        Mul(Int(10), Sym("centihz"))
    CONVERSIONS["CENTIHERTZ:EXAHERTZ"] = \
        Mul(Pow(10, 20), Sym("centihz"))
    CONVERSIONS["CENTIHERTZ:FEMTOHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 13)), Sym("centihz"))
    CONVERSIONS["CENTIHERTZ:GIGAHERTZ"] = \
        Mul(Pow(10, 11), Sym("centihz"))
    CONVERSIONS["CENTIHERTZ:HECTOHERTZ"] = \
        Mul(Pow(10, 4), Sym("centihz"))
    CONVERSIONS["CENTIHERTZ:HERTZ"] = \
        Mul(Int(100), Sym("centihz"))
    CONVERSIONS["CENTIHERTZ:KILOHERTZ"] = \
        Mul(Pow(10, 5), Sym("centihz"))
    CONVERSIONS["CENTIHERTZ:MEGAHERTZ"] = \
        Mul(Pow(10, 8), Sym("centihz"))
    CONVERSIONS["CENTIHERTZ:MICROHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 4)), Sym("centihz"))
    CONVERSIONS["CENTIHERTZ:MILLIHERTZ"] = \
        Mul(Rat(Int(1), Int(10)), Sym("centihz"))
    CONVERSIONS["CENTIHERTZ:NANOHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 7)), Sym("centihz"))
    CONVERSIONS["CENTIHERTZ:PETAHERTZ"] = \
        Mul(Pow(10, 17), Sym("centihz"))
    CONVERSIONS["CENTIHERTZ:PICOHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 10)), Sym("centihz"))
    CONVERSIONS["CENTIHERTZ:TERAHERTZ"] = \
        Mul(Pow(10, 14), Sym("centihz"))
    CONVERSIONS["CENTIHERTZ:YOCTOHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 22)), Sym("centihz"))
    CONVERSIONS["CENTIHERTZ:YOTTAHERTZ"] = \
        Mul(Pow(10, 26), Sym("centihz"))
    CONVERSIONS["CENTIHERTZ:ZEPTOHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 19)), Sym("centihz"))
    CONVERSIONS["CENTIHERTZ:ZETTAHERTZ"] = \
        Mul(Pow(10, 23), Sym("centihz"))
    CONVERSIONS["DECAHERTZ:ATTOHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 19)), Sym("decahz"))
    CONVERSIONS["DECAHERTZ:CENTIHERTZ"] = \
        Mul(Rat(Int(1), Int(1000)), Sym("decahz"))
    CONVERSIONS["DECAHERTZ:DECIHERTZ"] = \
        Mul(Rat(Int(1), Int(100)), Sym("decahz"))
    CONVERSIONS["DECAHERTZ:EXAHERTZ"] = \
        Mul(Pow(10, 17), Sym("decahz"))
    CONVERSIONS["DECAHERTZ:FEMTOHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 16)), Sym("decahz"))
    CONVERSIONS["DECAHERTZ:GIGAHERTZ"] = \
        Mul(Pow(10, 8), Sym("decahz"))
    CONVERSIONS["DECAHERTZ:HECTOHERTZ"] = \
        Mul(Int(10), Sym("decahz"))
    CONVERSIONS["DECAHERTZ:HERTZ"] = \
        Mul(Rat(Int(1), Int(10)), Sym("decahz"))
    CONVERSIONS["DECAHERTZ:KILOHERTZ"] = \
        Mul(Int(100), Sym("decahz"))
    CONVERSIONS["DECAHERTZ:MEGAHERTZ"] = \
        Mul(Pow(10, 5), Sym("decahz"))
    CONVERSIONS["DECAHERTZ:MICROHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 7)), Sym("decahz"))
    CONVERSIONS["DECAHERTZ:MILLIHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 4)), Sym("decahz"))
    CONVERSIONS["DECAHERTZ:NANOHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 10)), Sym("decahz"))
    CONVERSIONS["DECAHERTZ:PETAHERTZ"] = \
        Mul(Pow(10, 14), Sym("decahz"))
    CONVERSIONS["DECAHERTZ:PICOHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 13)), Sym("decahz"))
    CONVERSIONS["DECAHERTZ:TERAHERTZ"] = \
        Mul(Pow(10, 11), Sym("decahz"))
    CONVERSIONS["DECAHERTZ:YOCTOHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 25)), Sym("decahz"))
    CONVERSIONS["DECAHERTZ:YOTTAHERTZ"] = \
        Mul(Pow(10, 23), Sym("decahz"))
    CONVERSIONS["DECAHERTZ:ZEPTOHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 22)), Sym("decahz"))
    CONVERSIONS["DECAHERTZ:ZETTAHERTZ"] = \
        Mul(Pow(10, 20), Sym("decahz"))
    CONVERSIONS["DECIHERTZ:ATTOHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 17)), Sym("decihz"))
    CONVERSIONS["DECIHERTZ:CENTIHERTZ"] = \
        Mul(Rat(Int(1), Int(10)), Sym("decihz"))
    CONVERSIONS["DECIHERTZ:DECAHERTZ"] = \
        Mul(Int(100), Sym("decihz"))
    CONVERSIONS["DECIHERTZ:EXAHERTZ"] = \
        Mul(Pow(10, 19), Sym("decihz"))
    CONVERSIONS["DECIHERTZ:FEMTOHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 14)), Sym("decihz"))
    CONVERSIONS["DECIHERTZ:GIGAHERTZ"] = \
        Mul(Pow(10, 10), Sym("decihz"))
    CONVERSIONS["DECIHERTZ:HECTOHERTZ"] = \
        Mul(Int(1000), Sym("decihz"))
    CONVERSIONS["DECIHERTZ:HERTZ"] = \
        Mul(Int(10), Sym("decihz"))
    CONVERSIONS["DECIHERTZ:KILOHERTZ"] = \
        Mul(Pow(10, 4), Sym("decihz"))
    CONVERSIONS["DECIHERTZ:MEGAHERTZ"] = \
        Mul(Pow(10, 7), Sym("decihz"))
    CONVERSIONS["DECIHERTZ:MICROHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 5)), Sym("decihz"))
    CONVERSIONS["DECIHERTZ:MILLIHERTZ"] = \
        Mul(Rat(Int(1), Int(100)), Sym("decihz"))
    CONVERSIONS["DECIHERTZ:NANOHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 8)), Sym("decihz"))
    CONVERSIONS["DECIHERTZ:PETAHERTZ"] = \
        Mul(Pow(10, 16), Sym("decihz"))
    CONVERSIONS["DECIHERTZ:PICOHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 11)), Sym("decihz"))
    CONVERSIONS["DECIHERTZ:TERAHERTZ"] = \
        Mul(Pow(10, 13), Sym("decihz"))
    CONVERSIONS["DECIHERTZ:YOCTOHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 23)), Sym("decihz"))
    CONVERSIONS["DECIHERTZ:YOTTAHERTZ"] = \
        Mul(Pow(10, 25), Sym("decihz"))
    CONVERSIONS["DECIHERTZ:ZEPTOHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 20)), Sym("decihz"))
    CONVERSIONS["DECIHERTZ:ZETTAHERTZ"] = \
        Mul(Pow(10, 22), Sym("decihz"))
    CONVERSIONS["EXAHERTZ:ATTOHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 36)), Sym("exahz"))
    CONVERSIONS["EXAHERTZ:CENTIHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 20)), Sym("exahz"))
    CONVERSIONS["EXAHERTZ:DECAHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 17)), Sym("exahz"))
    CONVERSIONS["EXAHERTZ:DECIHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 19)), Sym("exahz"))
    CONVERSIONS["EXAHERTZ:FEMTOHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 33)), Sym("exahz"))
    CONVERSIONS["EXAHERTZ:GIGAHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("exahz"))
    CONVERSIONS["EXAHERTZ:HECTOHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 16)), Sym("exahz"))
    CONVERSIONS["EXAHERTZ:HERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 18)), Sym("exahz"))
    CONVERSIONS["EXAHERTZ:KILOHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 15)), Sym("exahz"))
    CONVERSIONS["EXAHERTZ:MEGAHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 12)), Sym("exahz"))
    CONVERSIONS["EXAHERTZ:MICROHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 24)), Sym("exahz"))
    CONVERSIONS["EXAHERTZ:MILLIHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 21)), Sym("exahz"))
    CONVERSIONS["EXAHERTZ:NANOHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 27)), Sym("exahz"))
    CONVERSIONS["EXAHERTZ:PETAHERTZ"] = \
        Mul(Rat(Int(1), Int(1000)), Sym("exahz"))
    CONVERSIONS["EXAHERTZ:PICOHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 30)), Sym("exahz"))
    CONVERSIONS["EXAHERTZ:TERAHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("exahz"))
    CONVERSIONS["EXAHERTZ:YOCTOHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 42)), Sym("exahz"))
    CONVERSIONS["EXAHERTZ:YOTTAHERTZ"] = \
        Mul(Pow(10, 6), Sym("exahz"))
    CONVERSIONS["EXAHERTZ:ZEPTOHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 39)), Sym("exahz"))
    CONVERSIONS["EXAHERTZ:ZETTAHERTZ"] = \
        Mul(Int(1000), Sym("exahz"))
    CONVERSIONS["FEMTOHERTZ:ATTOHERTZ"] = \
        Mul(Rat(Int(1), Int(1000)), Sym("femtohz"))
    CONVERSIONS["FEMTOHERTZ:CENTIHERTZ"] = \
        Mul(Pow(10, 13), Sym("femtohz"))
    CONVERSIONS["FEMTOHERTZ:DECAHERTZ"] = \
        Mul(Pow(10, 16), Sym("femtohz"))
    CONVERSIONS["FEMTOHERTZ:DECIHERTZ"] = \
        Mul(Pow(10, 14), Sym("femtohz"))
    CONVERSIONS["FEMTOHERTZ:EXAHERTZ"] = \
        Mul(Pow(10, 33), Sym("femtohz"))
    CONVERSIONS["FEMTOHERTZ:GIGAHERTZ"] = \
        Mul(Pow(10, 24), Sym("femtohz"))
    CONVERSIONS["FEMTOHERTZ:HECTOHERTZ"] = \
        Mul(Pow(10, 17), Sym("femtohz"))
    CONVERSIONS["FEMTOHERTZ:HERTZ"] = \
        Mul(Pow(10, 15), Sym("femtohz"))
    CONVERSIONS["FEMTOHERTZ:KILOHERTZ"] = \
        Mul(Pow(10, 18), Sym("femtohz"))
    CONVERSIONS["FEMTOHERTZ:MEGAHERTZ"] = \
        Mul(Pow(10, 21), Sym("femtohz"))
    CONVERSIONS["FEMTOHERTZ:MICROHERTZ"] = \
        Mul(Pow(10, 9), Sym("femtohz"))
    CONVERSIONS["FEMTOHERTZ:MILLIHERTZ"] = \
        Mul(Pow(10, 12), Sym("femtohz"))
    CONVERSIONS["FEMTOHERTZ:NANOHERTZ"] = \
        Mul(Pow(10, 6), Sym("femtohz"))
    CONVERSIONS["FEMTOHERTZ:PETAHERTZ"] = \
        Mul(Pow(10, 30), Sym("femtohz"))
    CONVERSIONS["FEMTOHERTZ:PICOHERTZ"] = \
        Mul(Int(1000), Sym("femtohz"))
    CONVERSIONS["FEMTOHERTZ:TERAHERTZ"] = \
        Mul(Pow(10, 27), Sym("femtohz"))
    CONVERSIONS["FEMTOHERTZ:YOCTOHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("femtohz"))
    CONVERSIONS["FEMTOHERTZ:YOTTAHERTZ"] = \
        Mul(Pow(10, 39), Sym("femtohz"))
    CONVERSIONS["FEMTOHERTZ:ZEPTOHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("femtohz"))
    CONVERSIONS["FEMTOHERTZ:ZETTAHERTZ"] = \
        Mul(Pow(10, 36), Sym("femtohz"))
    CONVERSIONS["GIGAHERTZ:ATTOHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 27)), Sym("gigahz"))
    CONVERSIONS["GIGAHERTZ:CENTIHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 11)), Sym("gigahz"))
    CONVERSIONS["GIGAHERTZ:DECAHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 8)), Sym("gigahz"))
    CONVERSIONS["GIGAHERTZ:DECIHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 10)), Sym("gigahz"))
    CONVERSIONS["GIGAHERTZ:EXAHERTZ"] = \
        Mul(Pow(10, 9), Sym("gigahz"))
    CONVERSIONS["GIGAHERTZ:FEMTOHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 24)), Sym("gigahz"))
    CONVERSIONS["GIGAHERTZ:HECTOHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 7)), Sym("gigahz"))
    CONVERSIONS["GIGAHERTZ:HERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("gigahz"))
    CONVERSIONS["GIGAHERTZ:KILOHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("gigahz"))
    CONVERSIONS["GIGAHERTZ:MEGAHERTZ"] = \
        Mul(Rat(Int(1), Int(1000)), Sym("gigahz"))
    CONVERSIONS["GIGAHERTZ:MICROHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 15)), Sym("gigahz"))
    CONVERSIONS["GIGAHERTZ:MILLIHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 12)), Sym("gigahz"))
    CONVERSIONS["GIGAHERTZ:NANOHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 18)), Sym("gigahz"))
    CONVERSIONS["GIGAHERTZ:PETAHERTZ"] = \
        Mul(Pow(10, 6), Sym("gigahz"))
    CONVERSIONS["GIGAHERTZ:PICOHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 21)), Sym("gigahz"))
    CONVERSIONS["GIGAHERTZ:TERAHERTZ"] = \
        Mul(Int(1000), Sym("gigahz"))
    CONVERSIONS["GIGAHERTZ:YOCTOHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 33)), Sym("gigahz"))
    CONVERSIONS["GIGAHERTZ:YOTTAHERTZ"] = \
        Mul(Pow(10, 15), Sym("gigahz"))
    CONVERSIONS["GIGAHERTZ:ZEPTOHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 30)), Sym("gigahz"))
    CONVERSIONS["GIGAHERTZ:ZETTAHERTZ"] = \
        Mul(Pow(10, 12), Sym("gigahz"))
    CONVERSIONS["HECTOHERTZ:ATTOHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 20)), Sym("hectohz"))
    CONVERSIONS["HECTOHERTZ:CENTIHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 4)), Sym("hectohz"))
    CONVERSIONS["HECTOHERTZ:DECAHERTZ"] = \
        Mul(Rat(Int(1), Int(10)), Sym("hectohz"))
    CONVERSIONS["HECTOHERTZ:DECIHERTZ"] = \
        Mul(Rat(Int(1), Int(1000)), Sym("hectohz"))
    CONVERSIONS["HECTOHERTZ:EXAHERTZ"] = \
        Mul(Pow(10, 16), Sym("hectohz"))
    CONVERSIONS["HECTOHERTZ:FEMTOHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 17)), Sym("hectohz"))
    CONVERSIONS["HECTOHERTZ:GIGAHERTZ"] = \
        Mul(Pow(10, 7), Sym("hectohz"))
    CONVERSIONS["HECTOHERTZ:HERTZ"] = \
        Mul(Rat(Int(1), Int(100)), Sym("hectohz"))
    CONVERSIONS["HECTOHERTZ:KILOHERTZ"] = \
        Mul(Int(10), Sym("hectohz"))
    CONVERSIONS["HECTOHERTZ:MEGAHERTZ"] = \
        Mul(Pow(10, 4), Sym("hectohz"))
    CONVERSIONS["HECTOHERTZ:MICROHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 8)), Sym("hectohz"))
    CONVERSIONS["HECTOHERTZ:MILLIHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 5)), Sym("hectohz"))
    CONVERSIONS["HECTOHERTZ:NANOHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 11)), Sym("hectohz"))
    CONVERSIONS["HECTOHERTZ:PETAHERTZ"] = \
        Mul(Pow(10, 13), Sym("hectohz"))
    CONVERSIONS["HECTOHERTZ:PICOHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 14)), Sym("hectohz"))
    CONVERSIONS["HECTOHERTZ:TERAHERTZ"] = \
        Mul(Pow(10, 10), Sym("hectohz"))
    CONVERSIONS["HECTOHERTZ:YOCTOHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 26)), Sym("hectohz"))
    CONVERSIONS["HECTOHERTZ:YOTTAHERTZ"] = \
        Mul(Pow(10, 22), Sym("hectohz"))
    CONVERSIONS["HECTOHERTZ:ZEPTOHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 23)), Sym("hectohz"))
    CONVERSIONS["HECTOHERTZ:ZETTAHERTZ"] = \
        Mul(Pow(10, 19), Sym("hectohz"))
    CONVERSIONS["HERTZ:ATTOHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 18)), Sym("hz"))
    CONVERSIONS["HERTZ:CENTIHERTZ"] = \
        Mul(Rat(Int(1), Int(100)), Sym("hz"))
    CONVERSIONS["HERTZ:DECAHERTZ"] = \
        Mul(Int(10), Sym("hz"))
    CONVERSIONS["HERTZ:DECIHERTZ"] = \
        Mul(Rat(Int(1), Int(10)), Sym("hz"))
    CONVERSIONS["HERTZ:EXAHERTZ"] = \
        Mul(Pow(10, 18), Sym("hz"))
    CONVERSIONS["HERTZ:FEMTOHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 15)), Sym("hz"))
    CONVERSIONS["HERTZ:GIGAHERTZ"] = \
        Mul(Pow(10, 9), Sym("hz"))
    CONVERSIONS["HERTZ:HECTOHERTZ"] = \
        Mul(Int(100), Sym("hz"))
    CONVERSIONS["HERTZ:KILOHERTZ"] = \
        Mul(Int(1000), Sym("hz"))
    CONVERSIONS["HERTZ:MEGAHERTZ"] = \
        Mul(Pow(10, 6), Sym("hz"))
    CONVERSIONS["HERTZ:MICROHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("hz"))
    CONVERSIONS["HERTZ:MILLIHERTZ"] = \
        Mul(Rat(Int(1), Int(1000)), Sym("hz"))
    CONVERSIONS["HERTZ:NANOHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("hz"))
    CONVERSIONS["HERTZ:PETAHERTZ"] = \
        Mul(Pow(10, 15), Sym("hz"))
    CONVERSIONS["HERTZ:PICOHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 12)), Sym("hz"))
    CONVERSIONS["HERTZ:TERAHERTZ"] = \
        Mul(Pow(10, 12), Sym("hz"))
    CONVERSIONS["HERTZ:YOCTOHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 24)), Sym("hz"))
    CONVERSIONS["HERTZ:YOTTAHERTZ"] = \
        Mul(Pow(10, 24), Sym("hz"))
    CONVERSIONS["HERTZ:ZEPTOHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 21)), Sym("hz"))
    CONVERSIONS["HERTZ:ZETTAHERTZ"] = \
        Mul(Pow(10, 21), Sym("hz"))
    CONVERSIONS["KILOHERTZ:ATTOHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 21)), Sym("kilohz"))
    CONVERSIONS["KILOHERTZ:CENTIHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 5)), Sym("kilohz"))
    CONVERSIONS["KILOHERTZ:DECAHERTZ"] = \
        Mul(Rat(Int(1), Int(100)), Sym("kilohz"))
    CONVERSIONS["KILOHERTZ:DECIHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 4)), Sym("kilohz"))
    CONVERSIONS["KILOHERTZ:EXAHERTZ"] = \
        Mul(Pow(10, 15), Sym("kilohz"))
    CONVERSIONS["KILOHERTZ:FEMTOHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 18)), Sym("kilohz"))
    CONVERSIONS["KILOHERTZ:GIGAHERTZ"] = \
        Mul(Pow(10, 6), Sym("kilohz"))
    CONVERSIONS["KILOHERTZ:HECTOHERTZ"] = \
        Mul(Rat(Int(1), Int(10)), Sym("kilohz"))
    CONVERSIONS["KILOHERTZ:HERTZ"] = \
        Mul(Rat(Int(1), Int(1000)), Sym("kilohz"))
    CONVERSIONS["KILOHERTZ:MEGAHERTZ"] = \
        Mul(Int(1000), Sym("kilohz"))
    CONVERSIONS["KILOHERTZ:MICROHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("kilohz"))
    CONVERSIONS["KILOHERTZ:MILLIHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("kilohz"))
    CONVERSIONS["KILOHERTZ:NANOHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 12)), Sym("kilohz"))
    CONVERSIONS["KILOHERTZ:PETAHERTZ"] = \
        Mul(Pow(10, 12), Sym("kilohz"))
    CONVERSIONS["KILOHERTZ:PICOHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 15)), Sym("kilohz"))
    CONVERSIONS["KILOHERTZ:TERAHERTZ"] = \
        Mul(Pow(10, 9), Sym("kilohz"))
    CONVERSIONS["KILOHERTZ:YOCTOHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 27)), Sym("kilohz"))
    CONVERSIONS["KILOHERTZ:YOTTAHERTZ"] = \
        Mul(Pow(10, 21), Sym("kilohz"))
    CONVERSIONS["KILOHERTZ:ZEPTOHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 24)), Sym("kilohz"))
    CONVERSIONS["KILOHERTZ:ZETTAHERTZ"] = \
        Mul(Pow(10, 18), Sym("kilohz"))
    CONVERSIONS["MEGAHERTZ:ATTOHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 24)), Sym("megahz"))
    CONVERSIONS["MEGAHERTZ:CENTIHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 8)), Sym("megahz"))
    CONVERSIONS["MEGAHERTZ:DECAHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 5)), Sym("megahz"))
    CONVERSIONS["MEGAHERTZ:DECIHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 7)), Sym("megahz"))
    CONVERSIONS["MEGAHERTZ:EXAHERTZ"] = \
        Mul(Pow(10, 12), Sym("megahz"))
    CONVERSIONS["MEGAHERTZ:FEMTOHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 21)), Sym("megahz"))
    CONVERSIONS["MEGAHERTZ:GIGAHERTZ"] = \
        Mul(Int(1000), Sym("megahz"))
    CONVERSIONS["MEGAHERTZ:HECTOHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 4)), Sym("megahz"))
    CONVERSIONS["MEGAHERTZ:HERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("megahz"))
    CONVERSIONS["MEGAHERTZ:KILOHERTZ"] = \
        Mul(Rat(Int(1), Int(1000)), Sym("megahz"))
    CONVERSIONS["MEGAHERTZ:MICROHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 12)), Sym("megahz"))
    CONVERSIONS["MEGAHERTZ:MILLIHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("megahz"))
    CONVERSIONS["MEGAHERTZ:NANOHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 15)), Sym("megahz"))
    CONVERSIONS["MEGAHERTZ:PETAHERTZ"] = \
        Mul(Pow(10, 9), Sym("megahz"))
    CONVERSIONS["MEGAHERTZ:PICOHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 18)), Sym("megahz"))
    CONVERSIONS["MEGAHERTZ:TERAHERTZ"] = \
        Mul(Pow(10, 6), Sym("megahz"))
    CONVERSIONS["MEGAHERTZ:YOCTOHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 30)), Sym("megahz"))
    CONVERSIONS["MEGAHERTZ:YOTTAHERTZ"] = \
        Mul(Pow(10, 18), Sym("megahz"))
    CONVERSIONS["MEGAHERTZ:ZEPTOHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 27)), Sym("megahz"))
    CONVERSIONS["MEGAHERTZ:ZETTAHERTZ"] = \
        Mul(Pow(10, 15), Sym("megahz"))
    CONVERSIONS["MICROHERTZ:ATTOHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 12)), Sym("microhz"))
    CONVERSIONS["MICROHERTZ:CENTIHERTZ"] = \
        Mul(Pow(10, 4), Sym("microhz"))
    CONVERSIONS["MICROHERTZ:DECAHERTZ"] = \
        Mul(Pow(10, 7), Sym("microhz"))
    CONVERSIONS["MICROHERTZ:DECIHERTZ"] = \
        Mul(Pow(10, 5), Sym("microhz"))
    CONVERSIONS["MICROHERTZ:EXAHERTZ"] = \
        Mul(Pow(10, 24), Sym("microhz"))
    CONVERSIONS["MICROHERTZ:FEMTOHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("microhz"))
    CONVERSIONS["MICROHERTZ:GIGAHERTZ"] = \
        Mul(Pow(10, 15), Sym("microhz"))
    CONVERSIONS["MICROHERTZ:HECTOHERTZ"] = \
        Mul(Pow(10, 8), Sym("microhz"))
    CONVERSIONS["MICROHERTZ:HERTZ"] = \
        Mul(Pow(10, 6), Sym("microhz"))
    CONVERSIONS["MICROHERTZ:KILOHERTZ"] = \
        Mul(Pow(10, 9), Sym("microhz"))
    CONVERSIONS["MICROHERTZ:MEGAHERTZ"] = \
        Mul(Pow(10, 12), Sym("microhz"))
    CONVERSIONS["MICROHERTZ:MILLIHERTZ"] = \
        Mul(Int(1000), Sym("microhz"))
    CONVERSIONS["MICROHERTZ:NANOHERTZ"] = \
        Mul(Rat(Int(1), Int(1000)), Sym("microhz"))
    CONVERSIONS["MICROHERTZ:PETAHERTZ"] = \
        Mul(Pow(10, 21), Sym("microhz"))
    CONVERSIONS["MICROHERTZ:PICOHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("microhz"))
    CONVERSIONS["MICROHERTZ:TERAHERTZ"] = \
        Mul(Pow(10, 18), Sym("microhz"))
    CONVERSIONS["MICROHERTZ:YOCTOHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 18)), Sym("microhz"))
    CONVERSIONS["MICROHERTZ:YOTTAHERTZ"] = \
        Mul(Pow(10, 30), Sym("microhz"))
    CONVERSIONS["MICROHERTZ:ZEPTOHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 15)), Sym("microhz"))
    CONVERSIONS["MICROHERTZ:ZETTAHERTZ"] = \
        Mul(Pow(10, 27), Sym("microhz"))
    CONVERSIONS["MILLIHERTZ:ATTOHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 15)), Sym("millihz"))
    CONVERSIONS["MILLIHERTZ:CENTIHERTZ"] = \
        Mul(Int(10), Sym("millihz"))
    CONVERSIONS["MILLIHERTZ:DECAHERTZ"] = \
        Mul(Pow(10, 4), Sym("millihz"))
    CONVERSIONS["MILLIHERTZ:DECIHERTZ"] = \
        Mul(Int(100), Sym("millihz"))
    CONVERSIONS["MILLIHERTZ:EXAHERTZ"] = \
        Mul(Pow(10, 21), Sym("millihz"))
    CONVERSIONS["MILLIHERTZ:FEMTOHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 12)), Sym("millihz"))
    CONVERSIONS["MILLIHERTZ:GIGAHERTZ"] = \
        Mul(Pow(10, 12), Sym("millihz"))
    CONVERSIONS["MILLIHERTZ:HECTOHERTZ"] = \
        Mul(Pow(10, 5), Sym("millihz"))
    CONVERSIONS["MILLIHERTZ:HERTZ"] = \
        Mul(Int(1000), Sym("millihz"))
    CONVERSIONS["MILLIHERTZ:KILOHERTZ"] = \
        Mul(Pow(10, 6), Sym("millihz"))
    CONVERSIONS["MILLIHERTZ:MEGAHERTZ"] = \
        Mul(Pow(10, 9), Sym("millihz"))
    CONVERSIONS["MILLIHERTZ:MICROHERTZ"] = \
        Mul(Rat(Int(1), Int(1000)), Sym("millihz"))
    CONVERSIONS["MILLIHERTZ:NANOHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("millihz"))
    CONVERSIONS["MILLIHERTZ:PETAHERTZ"] = \
        Mul(Pow(10, 18), Sym("millihz"))
    CONVERSIONS["MILLIHERTZ:PICOHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("millihz"))
    CONVERSIONS["MILLIHERTZ:TERAHERTZ"] = \
        Mul(Pow(10, 15), Sym("millihz"))
    CONVERSIONS["MILLIHERTZ:YOCTOHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 21)), Sym("millihz"))
    CONVERSIONS["MILLIHERTZ:YOTTAHERTZ"] = \
        Mul(Pow(10, 27), Sym("millihz"))
    CONVERSIONS["MILLIHERTZ:ZEPTOHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 18)), Sym("millihz"))
    CONVERSIONS["MILLIHERTZ:ZETTAHERTZ"] = \
        Mul(Pow(10, 24), Sym("millihz"))
    CONVERSIONS["NANOHERTZ:ATTOHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("nanohz"))
    CONVERSIONS["NANOHERTZ:CENTIHERTZ"] = \
        Mul(Pow(10, 7), Sym("nanohz"))
    CONVERSIONS["NANOHERTZ:DECAHERTZ"] = \
        Mul(Pow(10, 10), Sym("nanohz"))
    CONVERSIONS["NANOHERTZ:DECIHERTZ"] = \
        Mul(Pow(10, 8), Sym("nanohz"))
    CONVERSIONS["NANOHERTZ:EXAHERTZ"] = \
        Mul(Pow(10, 27), Sym("nanohz"))
    CONVERSIONS["NANOHERTZ:FEMTOHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("nanohz"))
    CONVERSIONS["NANOHERTZ:GIGAHERTZ"] = \
        Mul(Pow(10, 18), Sym("nanohz"))
    CONVERSIONS["NANOHERTZ:HECTOHERTZ"] = \
        Mul(Pow(10, 11), Sym("nanohz"))
    CONVERSIONS["NANOHERTZ:HERTZ"] = \
        Mul(Pow(10, 9), Sym("nanohz"))
    CONVERSIONS["NANOHERTZ:KILOHERTZ"] = \
        Mul(Pow(10, 12), Sym("nanohz"))
    CONVERSIONS["NANOHERTZ:MEGAHERTZ"] = \
        Mul(Pow(10, 15), Sym("nanohz"))
    CONVERSIONS["NANOHERTZ:MICROHERTZ"] = \
        Mul(Int(1000), Sym("nanohz"))
    CONVERSIONS["NANOHERTZ:MILLIHERTZ"] = \
        Mul(Pow(10, 6), Sym("nanohz"))
    CONVERSIONS["NANOHERTZ:PETAHERTZ"] = \
        Mul(Pow(10, 24), Sym("nanohz"))
    CONVERSIONS["NANOHERTZ:PICOHERTZ"] = \
        Mul(Rat(Int(1), Int(1000)), Sym("nanohz"))
    CONVERSIONS["NANOHERTZ:TERAHERTZ"] = \
        Mul(Pow(10, 21), Sym("nanohz"))
    CONVERSIONS["NANOHERTZ:YOCTOHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 15)), Sym("nanohz"))
    CONVERSIONS["NANOHERTZ:YOTTAHERTZ"] = \
        Mul(Pow(10, 33), Sym("nanohz"))
    CONVERSIONS["NANOHERTZ:ZEPTOHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 12)), Sym("nanohz"))
    CONVERSIONS["NANOHERTZ:ZETTAHERTZ"] = \
        Mul(Pow(10, 30), Sym("nanohz"))
    CONVERSIONS["PETAHERTZ:ATTOHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 33)), Sym("petahz"))
    CONVERSIONS["PETAHERTZ:CENTIHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 17)), Sym("petahz"))
    CONVERSIONS["PETAHERTZ:DECAHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 14)), Sym("petahz"))
    CONVERSIONS["PETAHERTZ:DECIHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 16)), Sym("petahz"))
    CONVERSIONS["PETAHERTZ:EXAHERTZ"] = \
        Mul(Int(1000), Sym("petahz"))
    CONVERSIONS["PETAHERTZ:FEMTOHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 30)), Sym("petahz"))
    CONVERSIONS["PETAHERTZ:GIGAHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("petahz"))
    CONVERSIONS["PETAHERTZ:HECTOHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 13)), Sym("petahz"))
    CONVERSIONS["PETAHERTZ:HERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 15)), Sym("petahz"))
    CONVERSIONS["PETAHERTZ:KILOHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 12)), Sym("petahz"))
    CONVERSIONS["PETAHERTZ:MEGAHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("petahz"))
    CONVERSIONS["PETAHERTZ:MICROHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 21)), Sym("petahz"))
    CONVERSIONS["PETAHERTZ:MILLIHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 18)), Sym("petahz"))
    CONVERSIONS["PETAHERTZ:NANOHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 24)), Sym("petahz"))
    CONVERSIONS["PETAHERTZ:PICOHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 27)), Sym("petahz"))
    CONVERSIONS["PETAHERTZ:TERAHERTZ"] = \
        Mul(Rat(Int(1), Int(1000)), Sym("petahz"))
    CONVERSIONS["PETAHERTZ:YOCTOHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 39)), Sym("petahz"))
    CONVERSIONS["PETAHERTZ:YOTTAHERTZ"] = \
        Mul(Pow(10, 9), Sym("petahz"))
    CONVERSIONS["PETAHERTZ:ZEPTOHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 36)), Sym("petahz"))
    CONVERSIONS["PETAHERTZ:ZETTAHERTZ"] = \
        Mul(Pow(10, 6), Sym("petahz"))
    CONVERSIONS["PICOHERTZ:ATTOHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("picohz"))
    CONVERSIONS["PICOHERTZ:CENTIHERTZ"] = \
        Mul(Pow(10, 10), Sym("picohz"))
    CONVERSIONS["PICOHERTZ:DECAHERTZ"] = \
        Mul(Pow(10, 13), Sym("picohz"))
    CONVERSIONS["PICOHERTZ:DECIHERTZ"] = \
        Mul(Pow(10, 11), Sym("picohz"))
    CONVERSIONS["PICOHERTZ:EXAHERTZ"] = \
        Mul(Pow(10, 30), Sym("picohz"))
    CONVERSIONS["PICOHERTZ:FEMTOHERTZ"] = \
        Mul(Rat(Int(1), Int(1000)), Sym("picohz"))
    CONVERSIONS["PICOHERTZ:GIGAHERTZ"] = \
        Mul(Pow(10, 21), Sym("picohz"))
    CONVERSIONS["PICOHERTZ:HECTOHERTZ"] = \
        Mul(Pow(10, 14), Sym("picohz"))
    CONVERSIONS["PICOHERTZ:HERTZ"] = \
        Mul(Pow(10, 12), Sym("picohz"))
    CONVERSIONS["PICOHERTZ:KILOHERTZ"] = \
        Mul(Pow(10, 15), Sym("picohz"))
    CONVERSIONS["PICOHERTZ:MEGAHERTZ"] = \
        Mul(Pow(10, 18), Sym("picohz"))
    CONVERSIONS["PICOHERTZ:MICROHERTZ"] = \
        Mul(Pow(10, 6), Sym("picohz"))
    CONVERSIONS["PICOHERTZ:MILLIHERTZ"] = \
        Mul(Pow(10, 9), Sym("picohz"))
    CONVERSIONS["PICOHERTZ:NANOHERTZ"] = \
        Mul(Int(1000), Sym("picohz"))
    CONVERSIONS["PICOHERTZ:PETAHERTZ"] = \
        Mul(Pow(10, 27), Sym("picohz"))
    CONVERSIONS["PICOHERTZ:TERAHERTZ"] = \
        Mul(Pow(10, 24), Sym("picohz"))
    CONVERSIONS["PICOHERTZ:YOCTOHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 12)), Sym("picohz"))
    CONVERSIONS["PICOHERTZ:YOTTAHERTZ"] = \
        Mul(Pow(10, 36), Sym("picohz"))
    CONVERSIONS["PICOHERTZ:ZEPTOHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("picohz"))
    CONVERSIONS["PICOHERTZ:ZETTAHERTZ"] = \
        Mul(Pow(10, 33), Sym("picohz"))
    CONVERSIONS["TERAHERTZ:ATTOHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 30)), Sym("terahz"))
    CONVERSIONS["TERAHERTZ:CENTIHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 14)), Sym("terahz"))
    CONVERSIONS["TERAHERTZ:DECAHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 11)), Sym("terahz"))
    CONVERSIONS["TERAHERTZ:DECIHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 13)), Sym("terahz"))
    CONVERSIONS["TERAHERTZ:EXAHERTZ"] = \
        Mul(Pow(10, 6), Sym("terahz"))
    CONVERSIONS["TERAHERTZ:FEMTOHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 27)), Sym("terahz"))
    CONVERSIONS["TERAHERTZ:GIGAHERTZ"] = \
        Mul(Rat(Int(1), Int(1000)), Sym("terahz"))
    CONVERSIONS["TERAHERTZ:HECTOHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 10)), Sym("terahz"))
    CONVERSIONS["TERAHERTZ:HERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 12)), Sym("terahz"))
    CONVERSIONS["TERAHERTZ:KILOHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("terahz"))
    CONVERSIONS["TERAHERTZ:MEGAHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("terahz"))
    CONVERSIONS["TERAHERTZ:MICROHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 18)), Sym("terahz"))
    CONVERSIONS["TERAHERTZ:MILLIHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 15)), Sym("terahz"))
    CONVERSIONS["TERAHERTZ:NANOHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 21)), Sym("terahz"))
    CONVERSIONS["TERAHERTZ:PETAHERTZ"] = \
        Mul(Int(1000), Sym("terahz"))
    CONVERSIONS["TERAHERTZ:PICOHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 24)), Sym("terahz"))
    CONVERSIONS["TERAHERTZ:YOCTOHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 36)), Sym("terahz"))
    CONVERSIONS["TERAHERTZ:YOTTAHERTZ"] = \
        Mul(Pow(10, 12), Sym("terahz"))
    CONVERSIONS["TERAHERTZ:ZEPTOHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 33)), Sym("terahz"))
    CONVERSIONS["TERAHERTZ:ZETTAHERTZ"] = \
        Mul(Pow(10, 9), Sym("terahz"))
    CONVERSIONS["YOCTOHERTZ:ATTOHERTZ"] = \
        Mul(Pow(10, 6), Sym("yoctohz"))
    CONVERSIONS["YOCTOHERTZ:CENTIHERTZ"] = \
        Mul(Pow(10, 22), Sym("yoctohz"))
    CONVERSIONS["YOCTOHERTZ:DECAHERTZ"] = \
        Mul(Pow(10, 25), Sym("yoctohz"))
    CONVERSIONS["YOCTOHERTZ:DECIHERTZ"] = \
        Mul(Pow(10, 23), Sym("yoctohz"))
    CONVERSIONS["YOCTOHERTZ:EXAHERTZ"] = \
        Mul(Pow(10, 42), Sym("yoctohz"))
    CONVERSIONS["YOCTOHERTZ:FEMTOHERTZ"] = \
        Mul(Pow(10, 9), Sym("yoctohz"))
    CONVERSIONS["YOCTOHERTZ:GIGAHERTZ"] = \
        Mul(Pow(10, 33), Sym("yoctohz"))
    CONVERSIONS["YOCTOHERTZ:HECTOHERTZ"] = \
        Mul(Pow(10, 26), Sym("yoctohz"))
    CONVERSIONS["YOCTOHERTZ:HERTZ"] = \
        Mul(Pow(10, 24), Sym("yoctohz"))
    CONVERSIONS["YOCTOHERTZ:KILOHERTZ"] = \
        Mul(Pow(10, 27), Sym("yoctohz"))
    CONVERSIONS["YOCTOHERTZ:MEGAHERTZ"] = \
        Mul(Pow(10, 30), Sym("yoctohz"))
    CONVERSIONS["YOCTOHERTZ:MICROHERTZ"] = \
        Mul(Pow(10, 18), Sym("yoctohz"))
    CONVERSIONS["YOCTOHERTZ:MILLIHERTZ"] = \
        Mul(Pow(10, 21), Sym("yoctohz"))
    CONVERSIONS["YOCTOHERTZ:NANOHERTZ"] = \
        Mul(Pow(10, 15), Sym("yoctohz"))
    CONVERSIONS["YOCTOHERTZ:PETAHERTZ"] = \
        Mul(Pow(10, 39), Sym("yoctohz"))
    CONVERSIONS["YOCTOHERTZ:PICOHERTZ"] = \
        Mul(Pow(10, 12), Sym("yoctohz"))
    CONVERSIONS["YOCTOHERTZ:TERAHERTZ"] = \
        Mul(Pow(10, 36), Sym("yoctohz"))
    CONVERSIONS["YOCTOHERTZ:YOTTAHERTZ"] = \
        Mul(Pow(10, 48), Sym("yoctohz"))
    CONVERSIONS["YOCTOHERTZ:ZEPTOHERTZ"] = \
        Mul(Int(1000), Sym("yoctohz"))
    CONVERSIONS["YOCTOHERTZ:ZETTAHERTZ"] = \
        Mul(Pow(10, 45), Sym("yoctohz"))
    CONVERSIONS["YOTTAHERTZ:ATTOHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 42)), Sym("yottahz"))
    CONVERSIONS["YOTTAHERTZ:CENTIHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 26)), Sym("yottahz"))
    CONVERSIONS["YOTTAHERTZ:DECAHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 23)), Sym("yottahz"))
    CONVERSIONS["YOTTAHERTZ:DECIHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 25)), Sym("yottahz"))
    CONVERSIONS["YOTTAHERTZ:EXAHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("yottahz"))
    CONVERSIONS["YOTTAHERTZ:FEMTOHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 39)), Sym("yottahz"))
    CONVERSIONS["YOTTAHERTZ:GIGAHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 15)), Sym("yottahz"))
    CONVERSIONS["YOTTAHERTZ:HECTOHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 22)), Sym("yottahz"))
    CONVERSIONS["YOTTAHERTZ:HERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 24)), Sym("yottahz"))
    CONVERSIONS["YOTTAHERTZ:KILOHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 21)), Sym("yottahz"))
    CONVERSIONS["YOTTAHERTZ:MEGAHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 18)), Sym("yottahz"))
    CONVERSIONS["YOTTAHERTZ:MICROHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 30)), Sym("yottahz"))
    CONVERSIONS["YOTTAHERTZ:MILLIHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 27)), Sym("yottahz"))
    CONVERSIONS["YOTTAHERTZ:NANOHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 33)), Sym("yottahz"))
    CONVERSIONS["YOTTAHERTZ:PETAHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("yottahz"))
    CONVERSIONS["YOTTAHERTZ:PICOHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 36)), Sym("yottahz"))
    CONVERSIONS["YOTTAHERTZ:TERAHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 12)), Sym("yottahz"))
    CONVERSIONS["YOTTAHERTZ:YOCTOHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 48)), Sym("yottahz"))
    CONVERSIONS["YOTTAHERTZ:ZEPTOHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 45)), Sym("yottahz"))
    CONVERSIONS["YOTTAHERTZ:ZETTAHERTZ"] = \
        Mul(Rat(Int(1), Int(1000)), Sym("yottahz"))
    CONVERSIONS["ZEPTOHERTZ:ATTOHERTZ"] = \
        Mul(Int(1000), Sym("zeptohz"))
    CONVERSIONS["ZEPTOHERTZ:CENTIHERTZ"] = \
        Mul(Pow(10, 19), Sym("zeptohz"))
    CONVERSIONS["ZEPTOHERTZ:DECAHERTZ"] = \
        Mul(Pow(10, 22), Sym("zeptohz"))
    CONVERSIONS["ZEPTOHERTZ:DECIHERTZ"] = \
        Mul(Pow(10, 20), Sym("zeptohz"))
    CONVERSIONS["ZEPTOHERTZ:EXAHERTZ"] = \
        Mul(Pow(10, 39), Sym("zeptohz"))
    CONVERSIONS["ZEPTOHERTZ:FEMTOHERTZ"] = \
        Mul(Pow(10, 6), Sym("zeptohz"))
    CONVERSIONS["ZEPTOHERTZ:GIGAHERTZ"] = \
        Mul(Pow(10, 30), Sym("zeptohz"))
    CONVERSIONS["ZEPTOHERTZ:HECTOHERTZ"] = \
        Mul(Pow(10, 23), Sym("zeptohz"))
    CONVERSIONS["ZEPTOHERTZ:HERTZ"] = \
        Mul(Pow(10, 21), Sym("zeptohz"))
    CONVERSIONS["ZEPTOHERTZ:KILOHERTZ"] = \
        Mul(Pow(10, 24), Sym("zeptohz"))
    CONVERSIONS["ZEPTOHERTZ:MEGAHERTZ"] = \
        Mul(Pow(10, 27), Sym("zeptohz"))
    CONVERSIONS["ZEPTOHERTZ:MICROHERTZ"] = \
        Mul(Pow(10, 15), Sym("zeptohz"))
    CONVERSIONS["ZEPTOHERTZ:MILLIHERTZ"] = \
        Mul(Pow(10, 18), Sym("zeptohz"))
    CONVERSIONS["ZEPTOHERTZ:NANOHERTZ"] = \
        Mul(Pow(10, 12), Sym("zeptohz"))
    CONVERSIONS["ZEPTOHERTZ:PETAHERTZ"] = \
        Mul(Pow(10, 36), Sym("zeptohz"))
    CONVERSIONS["ZEPTOHERTZ:PICOHERTZ"] = \
        Mul(Pow(10, 9), Sym("zeptohz"))
    CONVERSIONS["ZEPTOHERTZ:TERAHERTZ"] = \
        Mul(Pow(10, 33), Sym("zeptohz"))
    CONVERSIONS["ZEPTOHERTZ:YOCTOHERTZ"] = \
        Mul(Rat(Int(1), Int(1000)), Sym("zeptohz"))
    CONVERSIONS["ZEPTOHERTZ:YOTTAHERTZ"] = \
        Mul(Pow(10, 45), Sym("zeptohz"))
    CONVERSIONS["ZEPTOHERTZ:ZETTAHERTZ"] = \
        Mul(Pow(10, 42), Sym("zeptohz"))
    CONVERSIONS["ZETTAHERTZ:ATTOHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 39)), Sym("zettahz"))
    CONVERSIONS["ZETTAHERTZ:CENTIHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 23)), Sym("zettahz"))
    CONVERSIONS["ZETTAHERTZ:DECAHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 20)), Sym("zettahz"))
    CONVERSIONS["ZETTAHERTZ:DECIHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 22)), Sym("zettahz"))
    CONVERSIONS["ZETTAHERTZ:EXAHERTZ"] = \
        Mul(Rat(Int(1), Int(1000)), Sym("zettahz"))
    CONVERSIONS["ZETTAHERTZ:FEMTOHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 36)), Sym("zettahz"))
    CONVERSIONS["ZETTAHERTZ:GIGAHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 12)), Sym("zettahz"))
    CONVERSIONS["ZETTAHERTZ:HECTOHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 19)), Sym("zettahz"))
    CONVERSIONS["ZETTAHERTZ:HERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 21)), Sym("zettahz"))
    CONVERSIONS["ZETTAHERTZ:KILOHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 18)), Sym("zettahz"))
    CONVERSIONS["ZETTAHERTZ:MEGAHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 15)), Sym("zettahz"))
    CONVERSIONS["ZETTAHERTZ:MICROHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 27)), Sym("zettahz"))
    CONVERSIONS["ZETTAHERTZ:MILLIHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 24)), Sym("zettahz"))
    CONVERSIONS["ZETTAHERTZ:NANOHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 30)), Sym("zettahz"))
    CONVERSIONS["ZETTAHERTZ:PETAHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("zettahz"))
    CONVERSIONS["ZETTAHERTZ:PICOHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 33)), Sym("zettahz"))
    CONVERSIONS["ZETTAHERTZ:TERAHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("zettahz"))
    CONVERSIONS["ZETTAHERTZ:YOCTOHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 45)), Sym("zettahz"))
    CONVERSIONS["ZETTAHERTZ:YOTTAHERTZ"] = \
        Mul(Int(1000), Sym("zettahz"))
    CONVERSIONS["ZETTAHERTZ:ZEPTOHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 42)), Sym("zettahz"))

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
                self.setUnit(getattr(UnitsFrequency, str(target)))
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
