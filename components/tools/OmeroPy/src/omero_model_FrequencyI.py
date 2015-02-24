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

from omero.model.conversions import Add  # nopep8
from omero.model.conversions import Int  # nopep8
from omero.model.conversions import Mul  # nopep8
from omero.model.conversions import Pow  # nopep8
from omero.model.conversions import Rat  # nopep8
from omero.model.conversions import Sym  # nopep8


class FrequencyI(_omero_model.Frequency, UnitBase):

    CONVERSIONS = dict()
    CONVERSIONS["ATTOHERTZ:CENTIHERTZ"] = \
        Mul(Pow(10, 16), Sym("attohz"))  # nopep8
    CONVERSIONS["ATTOHERTZ:DECAHERTZ"] = \
        Mul(Pow(10, 19), Sym("attohz"))  # nopep8
    CONVERSIONS["ATTOHERTZ:DECIHERTZ"] = \
        Mul(Pow(10, 17), Sym("attohz"))  # nopep8
    CONVERSIONS["ATTOHERTZ:EXAHERTZ"] = \
        Mul(Pow(10, 36), Sym("attohz"))  # nopep8
    CONVERSIONS["ATTOHERTZ:FEMTOHERTZ"] = \
        Mul(Int(1000), Sym("attohz"))  # nopep8
    CONVERSIONS["ATTOHERTZ:GIGAHERTZ"] = \
        Mul(Pow(10, 27), Sym("attohz"))  # nopep8
    CONVERSIONS["ATTOHERTZ:HECTOHERTZ"] = \
        Mul(Pow(10, 20), Sym("attohz"))  # nopep8
    CONVERSIONS["ATTOHERTZ:HERTZ"] = \
        Mul(Pow(10, 18), Sym("attohz"))  # nopep8
    CONVERSIONS["ATTOHERTZ:KILOHERTZ"] = \
        Mul(Pow(10, 21), Sym("attohz"))  # nopep8
    CONVERSIONS["ATTOHERTZ:MEGAHERTZ"] = \
        Mul(Pow(10, 24), Sym("attohz"))  # nopep8
    CONVERSIONS["ATTOHERTZ:MICROHERTZ"] = \
        Mul(Pow(10, 12), Sym("attohz"))  # nopep8
    CONVERSIONS["ATTOHERTZ:MILLIHERTZ"] = \
        Mul(Pow(10, 15), Sym("attohz"))  # nopep8
    CONVERSIONS["ATTOHERTZ:NANOHERTZ"] = \
        Mul(Pow(10, 9), Sym("attohz"))  # nopep8
    CONVERSIONS["ATTOHERTZ:PETAHERTZ"] = \
        Mul(Pow(10, 33), Sym("attohz"))  # nopep8
    CONVERSIONS["ATTOHERTZ:PICOHERTZ"] = \
        Mul(Pow(10, 6), Sym("attohz"))  # nopep8
    CONVERSIONS["ATTOHERTZ:TERAHERTZ"] = \
        Mul(Pow(10, 30), Sym("attohz"))  # nopep8
    CONVERSIONS["ATTOHERTZ:YOCTOHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("attohz"))  # nopep8
    CONVERSIONS["ATTOHERTZ:YOTTAHERTZ"] = \
        Mul(Pow(10, 42), Sym("attohz"))  # nopep8
    CONVERSIONS["ATTOHERTZ:ZEPTOHERTZ"] = \
        Mul(Rat(Int(1), Int(1000)), Sym("attohz"))  # nopep8
    CONVERSIONS["ATTOHERTZ:ZETTAHERTZ"] = \
        Mul(Pow(10, 39), Sym("attohz"))  # nopep8
    CONVERSIONS["CENTIHERTZ:ATTOHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 16)), Sym("centihz"))  # nopep8
    CONVERSIONS["CENTIHERTZ:DECAHERTZ"] = \
        Mul(Int(1000), Sym("centihz"))  # nopep8
    CONVERSIONS["CENTIHERTZ:DECIHERTZ"] = \
        Mul(Int(10), Sym("centihz"))  # nopep8
    CONVERSIONS["CENTIHERTZ:EXAHERTZ"] = \
        Mul(Pow(10, 20), Sym("centihz"))  # nopep8
    CONVERSIONS["CENTIHERTZ:FEMTOHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 13)), Sym("centihz"))  # nopep8
    CONVERSIONS["CENTIHERTZ:GIGAHERTZ"] = \
        Mul(Pow(10, 11), Sym("centihz"))  # nopep8
    CONVERSIONS["CENTIHERTZ:HECTOHERTZ"] = \
        Mul(Pow(10, 4), Sym("centihz"))  # nopep8
    CONVERSIONS["CENTIHERTZ:HERTZ"] = \
        Mul(Int(100), Sym("centihz"))  # nopep8
    CONVERSIONS["CENTIHERTZ:KILOHERTZ"] = \
        Mul(Pow(10, 5), Sym("centihz"))  # nopep8
    CONVERSIONS["CENTIHERTZ:MEGAHERTZ"] = \
        Mul(Pow(10, 8), Sym("centihz"))  # nopep8
    CONVERSIONS["CENTIHERTZ:MICROHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 4)), Sym("centihz"))  # nopep8
    CONVERSIONS["CENTIHERTZ:MILLIHERTZ"] = \
        Mul(Rat(Int(1), Int(10)), Sym("centihz"))  # nopep8
    CONVERSIONS["CENTIHERTZ:NANOHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 7)), Sym("centihz"))  # nopep8
    CONVERSIONS["CENTIHERTZ:PETAHERTZ"] = \
        Mul(Pow(10, 17), Sym("centihz"))  # nopep8
    CONVERSIONS["CENTIHERTZ:PICOHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 10)), Sym("centihz"))  # nopep8
    CONVERSIONS["CENTIHERTZ:TERAHERTZ"] = \
        Mul(Pow(10, 14), Sym("centihz"))  # nopep8
    CONVERSIONS["CENTIHERTZ:YOCTOHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 22)), Sym("centihz"))  # nopep8
    CONVERSIONS["CENTIHERTZ:YOTTAHERTZ"] = \
        Mul(Pow(10, 26), Sym("centihz"))  # nopep8
    CONVERSIONS["CENTIHERTZ:ZEPTOHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 19)), Sym("centihz"))  # nopep8
    CONVERSIONS["CENTIHERTZ:ZETTAHERTZ"] = \
        Mul(Pow(10, 23), Sym("centihz"))  # nopep8
    CONVERSIONS["DECAHERTZ:ATTOHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 19)), Sym("decahz"))  # nopep8
    CONVERSIONS["DECAHERTZ:CENTIHERTZ"] = \
        Mul(Rat(Int(1), Int(1000)), Sym("decahz"))  # nopep8
    CONVERSIONS["DECAHERTZ:DECIHERTZ"] = \
        Mul(Rat(Int(1), Int(100)), Sym("decahz"))  # nopep8
    CONVERSIONS["DECAHERTZ:EXAHERTZ"] = \
        Mul(Pow(10, 17), Sym("decahz"))  # nopep8
    CONVERSIONS["DECAHERTZ:FEMTOHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 16)), Sym("decahz"))  # nopep8
    CONVERSIONS["DECAHERTZ:GIGAHERTZ"] = \
        Mul(Pow(10, 8), Sym("decahz"))  # nopep8
    CONVERSIONS["DECAHERTZ:HECTOHERTZ"] = \
        Mul(Int(10), Sym("decahz"))  # nopep8
    CONVERSIONS["DECAHERTZ:HERTZ"] = \
        Mul(Rat(Int(1), Int(10)), Sym("decahz"))  # nopep8
    CONVERSIONS["DECAHERTZ:KILOHERTZ"] = \
        Mul(Int(100), Sym("decahz"))  # nopep8
    CONVERSIONS["DECAHERTZ:MEGAHERTZ"] = \
        Mul(Pow(10, 5), Sym("decahz"))  # nopep8
    CONVERSIONS["DECAHERTZ:MICROHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 7)), Sym("decahz"))  # nopep8
    CONVERSIONS["DECAHERTZ:MILLIHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 4)), Sym("decahz"))  # nopep8
    CONVERSIONS["DECAHERTZ:NANOHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 10)), Sym("decahz"))  # nopep8
    CONVERSIONS["DECAHERTZ:PETAHERTZ"] = \
        Mul(Pow(10, 14), Sym("decahz"))  # nopep8
    CONVERSIONS["DECAHERTZ:PICOHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 13)), Sym("decahz"))  # nopep8
    CONVERSIONS["DECAHERTZ:TERAHERTZ"] = \
        Mul(Pow(10, 11), Sym("decahz"))  # nopep8
    CONVERSIONS["DECAHERTZ:YOCTOHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 25)), Sym("decahz"))  # nopep8
    CONVERSIONS["DECAHERTZ:YOTTAHERTZ"] = \
        Mul(Pow(10, 23), Sym("decahz"))  # nopep8
    CONVERSIONS["DECAHERTZ:ZEPTOHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 22)), Sym("decahz"))  # nopep8
    CONVERSIONS["DECAHERTZ:ZETTAHERTZ"] = \
        Mul(Pow(10, 20), Sym("decahz"))  # nopep8
    CONVERSIONS["DECIHERTZ:ATTOHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 17)), Sym("decihz"))  # nopep8
    CONVERSIONS["DECIHERTZ:CENTIHERTZ"] = \
        Mul(Rat(Int(1), Int(10)), Sym("decihz"))  # nopep8
    CONVERSIONS["DECIHERTZ:DECAHERTZ"] = \
        Mul(Int(100), Sym("decihz"))  # nopep8
    CONVERSIONS["DECIHERTZ:EXAHERTZ"] = \
        Mul(Pow(10, 19), Sym("decihz"))  # nopep8
    CONVERSIONS["DECIHERTZ:FEMTOHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 14)), Sym("decihz"))  # nopep8
    CONVERSIONS["DECIHERTZ:GIGAHERTZ"] = \
        Mul(Pow(10, 10), Sym("decihz"))  # nopep8
    CONVERSIONS["DECIHERTZ:HECTOHERTZ"] = \
        Mul(Int(1000), Sym("decihz"))  # nopep8
    CONVERSIONS["DECIHERTZ:HERTZ"] = \
        Mul(Int(10), Sym("decihz"))  # nopep8
    CONVERSIONS["DECIHERTZ:KILOHERTZ"] = \
        Mul(Pow(10, 4), Sym("decihz"))  # nopep8
    CONVERSIONS["DECIHERTZ:MEGAHERTZ"] = \
        Mul(Pow(10, 7), Sym("decihz"))  # nopep8
    CONVERSIONS["DECIHERTZ:MICROHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 5)), Sym("decihz"))  # nopep8
    CONVERSIONS["DECIHERTZ:MILLIHERTZ"] = \
        Mul(Rat(Int(1), Int(100)), Sym("decihz"))  # nopep8
    CONVERSIONS["DECIHERTZ:NANOHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 8)), Sym("decihz"))  # nopep8
    CONVERSIONS["DECIHERTZ:PETAHERTZ"] = \
        Mul(Pow(10, 16), Sym("decihz"))  # nopep8
    CONVERSIONS["DECIHERTZ:PICOHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 11)), Sym("decihz"))  # nopep8
    CONVERSIONS["DECIHERTZ:TERAHERTZ"] = \
        Mul(Pow(10, 13), Sym("decihz"))  # nopep8
    CONVERSIONS["DECIHERTZ:YOCTOHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 23)), Sym("decihz"))  # nopep8
    CONVERSIONS["DECIHERTZ:YOTTAHERTZ"] = \
        Mul(Pow(10, 25), Sym("decihz"))  # nopep8
    CONVERSIONS["DECIHERTZ:ZEPTOHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 20)), Sym("decihz"))  # nopep8
    CONVERSIONS["DECIHERTZ:ZETTAHERTZ"] = \
        Mul(Pow(10, 22), Sym("decihz"))  # nopep8
    CONVERSIONS["EXAHERTZ:ATTOHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 36)), Sym("exahz"))  # nopep8
    CONVERSIONS["EXAHERTZ:CENTIHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 20)), Sym("exahz"))  # nopep8
    CONVERSIONS["EXAHERTZ:DECAHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 17)), Sym("exahz"))  # nopep8
    CONVERSIONS["EXAHERTZ:DECIHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 19)), Sym("exahz"))  # nopep8
    CONVERSIONS["EXAHERTZ:FEMTOHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 33)), Sym("exahz"))  # nopep8
    CONVERSIONS["EXAHERTZ:GIGAHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("exahz"))  # nopep8
    CONVERSIONS["EXAHERTZ:HECTOHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 16)), Sym("exahz"))  # nopep8
    CONVERSIONS["EXAHERTZ:HERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 18)), Sym("exahz"))  # nopep8
    CONVERSIONS["EXAHERTZ:KILOHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 15)), Sym("exahz"))  # nopep8
    CONVERSIONS["EXAHERTZ:MEGAHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 12)), Sym("exahz"))  # nopep8
    CONVERSIONS["EXAHERTZ:MICROHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 24)), Sym("exahz"))  # nopep8
    CONVERSIONS["EXAHERTZ:MILLIHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 21)), Sym("exahz"))  # nopep8
    CONVERSIONS["EXAHERTZ:NANOHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 27)), Sym("exahz"))  # nopep8
    CONVERSIONS["EXAHERTZ:PETAHERTZ"] = \
        Mul(Rat(Int(1), Int(1000)), Sym("exahz"))  # nopep8
    CONVERSIONS["EXAHERTZ:PICOHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 30)), Sym("exahz"))  # nopep8
    CONVERSIONS["EXAHERTZ:TERAHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("exahz"))  # nopep8
    CONVERSIONS["EXAHERTZ:YOCTOHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 42)), Sym("exahz"))  # nopep8
    CONVERSIONS["EXAHERTZ:YOTTAHERTZ"] = \
        Mul(Pow(10, 6), Sym("exahz"))  # nopep8
    CONVERSIONS["EXAHERTZ:ZEPTOHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 39)), Sym("exahz"))  # nopep8
    CONVERSIONS["EXAHERTZ:ZETTAHERTZ"] = \
        Mul(Int(1000), Sym("exahz"))  # nopep8
    CONVERSIONS["FEMTOHERTZ:ATTOHERTZ"] = \
        Mul(Rat(Int(1), Int(1000)), Sym("femtohz"))  # nopep8
    CONVERSIONS["FEMTOHERTZ:CENTIHERTZ"] = \
        Mul(Pow(10, 13), Sym("femtohz"))  # nopep8
    CONVERSIONS["FEMTOHERTZ:DECAHERTZ"] = \
        Mul(Pow(10, 16), Sym("femtohz"))  # nopep8
    CONVERSIONS["FEMTOHERTZ:DECIHERTZ"] = \
        Mul(Pow(10, 14), Sym("femtohz"))  # nopep8
    CONVERSIONS["FEMTOHERTZ:EXAHERTZ"] = \
        Mul(Pow(10, 33), Sym("femtohz"))  # nopep8
    CONVERSIONS["FEMTOHERTZ:GIGAHERTZ"] = \
        Mul(Pow(10, 24), Sym("femtohz"))  # nopep8
    CONVERSIONS["FEMTOHERTZ:HECTOHERTZ"] = \
        Mul(Pow(10, 17), Sym("femtohz"))  # nopep8
    CONVERSIONS["FEMTOHERTZ:HERTZ"] = \
        Mul(Pow(10, 15), Sym("femtohz"))  # nopep8
    CONVERSIONS["FEMTOHERTZ:KILOHERTZ"] = \
        Mul(Pow(10, 18), Sym("femtohz"))  # nopep8
    CONVERSIONS["FEMTOHERTZ:MEGAHERTZ"] = \
        Mul(Pow(10, 21), Sym("femtohz"))  # nopep8
    CONVERSIONS["FEMTOHERTZ:MICROHERTZ"] = \
        Mul(Pow(10, 9), Sym("femtohz"))  # nopep8
    CONVERSIONS["FEMTOHERTZ:MILLIHERTZ"] = \
        Mul(Pow(10, 12), Sym("femtohz"))  # nopep8
    CONVERSIONS["FEMTOHERTZ:NANOHERTZ"] = \
        Mul(Pow(10, 6), Sym("femtohz"))  # nopep8
    CONVERSIONS["FEMTOHERTZ:PETAHERTZ"] = \
        Mul(Pow(10, 30), Sym("femtohz"))  # nopep8
    CONVERSIONS["FEMTOHERTZ:PICOHERTZ"] = \
        Mul(Int(1000), Sym("femtohz"))  # nopep8
    CONVERSIONS["FEMTOHERTZ:TERAHERTZ"] = \
        Mul(Pow(10, 27), Sym("femtohz"))  # nopep8
    CONVERSIONS["FEMTOHERTZ:YOCTOHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("femtohz"))  # nopep8
    CONVERSIONS["FEMTOHERTZ:YOTTAHERTZ"] = \
        Mul(Pow(10, 39), Sym("femtohz"))  # nopep8
    CONVERSIONS["FEMTOHERTZ:ZEPTOHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("femtohz"))  # nopep8
    CONVERSIONS["FEMTOHERTZ:ZETTAHERTZ"] = \
        Mul(Pow(10, 36), Sym("femtohz"))  # nopep8
    CONVERSIONS["GIGAHERTZ:ATTOHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 27)), Sym("gigahz"))  # nopep8
    CONVERSIONS["GIGAHERTZ:CENTIHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 11)), Sym("gigahz"))  # nopep8
    CONVERSIONS["GIGAHERTZ:DECAHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 8)), Sym("gigahz"))  # nopep8
    CONVERSIONS["GIGAHERTZ:DECIHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 10)), Sym("gigahz"))  # nopep8
    CONVERSIONS["GIGAHERTZ:EXAHERTZ"] = \
        Mul(Pow(10, 9), Sym("gigahz"))  # nopep8
    CONVERSIONS["GIGAHERTZ:FEMTOHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 24)), Sym("gigahz"))  # nopep8
    CONVERSIONS["GIGAHERTZ:HECTOHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 7)), Sym("gigahz"))  # nopep8
    CONVERSIONS["GIGAHERTZ:HERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("gigahz"))  # nopep8
    CONVERSIONS["GIGAHERTZ:KILOHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("gigahz"))  # nopep8
    CONVERSIONS["GIGAHERTZ:MEGAHERTZ"] = \
        Mul(Rat(Int(1), Int(1000)), Sym("gigahz"))  # nopep8
    CONVERSIONS["GIGAHERTZ:MICROHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 15)), Sym("gigahz"))  # nopep8
    CONVERSIONS["GIGAHERTZ:MILLIHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 12)), Sym("gigahz"))  # nopep8
    CONVERSIONS["GIGAHERTZ:NANOHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 18)), Sym("gigahz"))  # nopep8
    CONVERSIONS["GIGAHERTZ:PETAHERTZ"] = \
        Mul(Pow(10, 6), Sym("gigahz"))  # nopep8
    CONVERSIONS["GIGAHERTZ:PICOHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 21)), Sym("gigahz"))  # nopep8
    CONVERSIONS["GIGAHERTZ:TERAHERTZ"] = \
        Mul(Int(1000), Sym("gigahz"))  # nopep8
    CONVERSIONS["GIGAHERTZ:YOCTOHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 33)), Sym("gigahz"))  # nopep8
    CONVERSIONS["GIGAHERTZ:YOTTAHERTZ"] = \
        Mul(Pow(10, 15), Sym("gigahz"))  # nopep8
    CONVERSIONS["GIGAHERTZ:ZEPTOHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 30)), Sym("gigahz"))  # nopep8
    CONVERSIONS["GIGAHERTZ:ZETTAHERTZ"] = \
        Mul(Pow(10, 12), Sym("gigahz"))  # nopep8
    CONVERSIONS["HECTOHERTZ:ATTOHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 20)), Sym("hectohz"))  # nopep8
    CONVERSIONS["HECTOHERTZ:CENTIHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 4)), Sym("hectohz"))  # nopep8
    CONVERSIONS["HECTOHERTZ:DECAHERTZ"] = \
        Mul(Rat(Int(1), Int(10)), Sym("hectohz"))  # nopep8
    CONVERSIONS["HECTOHERTZ:DECIHERTZ"] = \
        Mul(Rat(Int(1), Int(1000)), Sym("hectohz"))  # nopep8
    CONVERSIONS["HECTOHERTZ:EXAHERTZ"] = \
        Mul(Pow(10, 16), Sym("hectohz"))  # nopep8
    CONVERSIONS["HECTOHERTZ:FEMTOHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 17)), Sym("hectohz"))  # nopep8
    CONVERSIONS["HECTOHERTZ:GIGAHERTZ"] = \
        Mul(Pow(10, 7), Sym("hectohz"))  # nopep8
    CONVERSIONS["HECTOHERTZ:HERTZ"] = \
        Mul(Rat(Int(1), Int(100)), Sym("hectohz"))  # nopep8
    CONVERSIONS["HECTOHERTZ:KILOHERTZ"] = \
        Mul(Int(10), Sym("hectohz"))  # nopep8
    CONVERSIONS["HECTOHERTZ:MEGAHERTZ"] = \
        Mul(Pow(10, 4), Sym("hectohz"))  # nopep8
    CONVERSIONS["HECTOHERTZ:MICROHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 8)), Sym("hectohz"))  # nopep8
    CONVERSIONS["HECTOHERTZ:MILLIHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 5)), Sym("hectohz"))  # nopep8
    CONVERSIONS["HECTOHERTZ:NANOHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 11)), Sym("hectohz"))  # nopep8
    CONVERSIONS["HECTOHERTZ:PETAHERTZ"] = \
        Mul(Pow(10, 13), Sym("hectohz"))  # nopep8
    CONVERSIONS["HECTOHERTZ:PICOHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 14)), Sym("hectohz"))  # nopep8
    CONVERSIONS["HECTOHERTZ:TERAHERTZ"] = \
        Mul(Pow(10, 10), Sym("hectohz"))  # nopep8
    CONVERSIONS["HECTOHERTZ:YOCTOHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 26)), Sym("hectohz"))  # nopep8
    CONVERSIONS["HECTOHERTZ:YOTTAHERTZ"] = \
        Mul(Pow(10, 22), Sym("hectohz"))  # nopep8
    CONVERSIONS["HECTOHERTZ:ZEPTOHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 23)), Sym("hectohz"))  # nopep8
    CONVERSIONS["HECTOHERTZ:ZETTAHERTZ"] = \
        Mul(Pow(10, 19), Sym("hectohz"))  # nopep8
    CONVERSIONS["HERTZ:ATTOHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 18)), Sym("hz"))  # nopep8
    CONVERSIONS["HERTZ:CENTIHERTZ"] = \
        Mul(Rat(Int(1), Int(100)), Sym("hz"))  # nopep8
    CONVERSIONS["HERTZ:DECAHERTZ"] = \
        Mul(Int(10), Sym("hz"))  # nopep8
    CONVERSIONS["HERTZ:DECIHERTZ"] = \
        Mul(Rat(Int(1), Int(10)), Sym("hz"))  # nopep8
    CONVERSIONS["HERTZ:EXAHERTZ"] = \
        Mul(Pow(10, 18), Sym("hz"))  # nopep8
    CONVERSIONS["HERTZ:FEMTOHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 15)), Sym("hz"))  # nopep8
    CONVERSIONS["HERTZ:GIGAHERTZ"] = \
        Mul(Pow(10, 9), Sym("hz"))  # nopep8
    CONVERSIONS["HERTZ:HECTOHERTZ"] = \
        Mul(Int(100), Sym("hz"))  # nopep8
    CONVERSIONS["HERTZ:KILOHERTZ"] = \
        Mul(Int(1000), Sym("hz"))  # nopep8
    CONVERSIONS["HERTZ:MEGAHERTZ"] = \
        Mul(Pow(10, 6), Sym("hz"))  # nopep8
    CONVERSIONS["HERTZ:MICROHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("hz"))  # nopep8
    CONVERSIONS["HERTZ:MILLIHERTZ"] = \
        Mul(Rat(Int(1), Int(1000)), Sym("hz"))  # nopep8
    CONVERSIONS["HERTZ:NANOHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("hz"))  # nopep8
    CONVERSIONS["HERTZ:PETAHERTZ"] = \
        Mul(Pow(10, 15), Sym("hz"))  # nopep8
    CONVERSIONS["HERTZ:PICOHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 12)), Sym("hz"))  # nopep8
    CONVERSIONS["HERTZ:TERAHERTZ"] = \
        Mul(Pow(10, 12), Sym("hz"))  # nopep8
    CONVERSIONS["HERTZ:YOCTOHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 24)), Sym("hz"))  # nopep8
    CONVERSIONS["HERTZ:YOTTAHERTZ"] = \
        Mul(Pow(10, 24), Sym("hz"))  # nopep8
    CONVERSIONS["HERTZ:ZEPTOHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 21)), Sym("hz"))  # nopep8
    CONVERSIONS["HERTZ:ZETTAHERTZ"] = \
        Mul(Pow(10, 21), Sym("hz"))  # nopep8
    CONVERSIONS["KILOHERTZ:ATTOHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 21)), Sym("kilohz"))  # nopep8
    CONVERSIONS["KILOHERTZ:CENTIHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 5)), Sym("kilohz"))  # nopep8
    CONVERSIONS["KILOHERTZ:DECAHERTZ"] = \
        Mul(Rat(Int(1), Int(100)), Sym("kilohz"))  # nopep8
    CONVERSIONS["KILOHERTZ:DECIHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 4)), Sym("kilohz"))  # nopep8
    CONVERSIONS["KILOHERTZ:EXAHERTZ"] = \
        Mul(Pow(10, 15), Sym("kilohz"))  # nopep8
    CONVERSIONS["KILOHERTZ:FEMTOHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 18)), Sym("kilohz"))  # nopep8
    CONVERSIONS["KILOHERTZ:GIGAHERTZ"] = \
        Mul(Pow(10, 6), Sym("kilohz"))  # nopep8
    CONVERSIONS["KILOHERTZ:HECTOHERTZ"] = \
        Mul(Rat(Int(1), Int(10)), Sym("kilohz"))  # nopep8
    CONVERSIONS["KILOHERTZ:HERTZ"] = \
        Mul(Rat(Int(1), Int(1000)), Sym("kilohz"))  # nopep8
    CONVERSIONS["KILOHERTZ:MEGAHERTZ"] = \
        Mul(Int(1000), Sym("kilohz"))  # nopep8
    CONVERSIONS["KILOHERTZ:MICROHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("kilohz"))  # nopep8
    CONVERSIONS["KILOHERTZ:MILLIHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("kilohz"))  # nopep8
    CONVERSIONS["KILOHERTZ:NANOHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 12)), Sym("kilohz"))  # nopep8
    CONVERSIONS["KILOHERTZ:PETAHERTZ"] = \
        Mul(Pow(10, 12), Sym("kilohz"))  # nopep8
    CONVERSIONS["KILOHERTZ:PICOHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 15)), Sym("kilohz"))  # nopep8
    CONVERSIONS["KILOHERTZ:TERAHERTZ"] = \
        Mul(Pow(10, 9), Sym("kilohz"))  # nopep8
    CONVERSIONS["KILOHERTZ:YOCTOHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 27)), Sym("kilohz"))  # nopep8
    CONVERSIONS["KILOHERTZ:YOTTAHERTZ"] = \
        Mul(Pow(10, 21), Sym("kilohz"))  # nopep8
    CONVERSIONS["KILOHERTZ:ZEPTOHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 24)), Sym("kilohz"))  # nopep8
    CONVERSIONS["KILOHERTZ:ZETTAHERTZ"] = \
        Mul(Pow(10, 18), Sym("kilohz"))  # nopep8
    CONVERSIONS["MEGAHERTZ:ATTOHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 24)), Sym("megahz"))  # nopep8
    CONVERSIONS["MEGAHERTZ:CENTIHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 8)), Sym("megahz"))  # nopep8
    CONVERSIONS["MEGAHERTZ:DECAHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 5)), Sym("megahz"))  # nopep8
    CONVERSIONS["MEGAHERTZ:DECIHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 7)), Sym("megahz"))  # nopep8
    CONVERSIONS["MEGAHERTZ:EXAHERTZ"] = \
        Mul(Pow(10, 12), Sym("megahz"))  # nopep8
    CONVERSIONS["MEGAHERTZ:FEMTOHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 21)), Sym("megahz"))  # nopep8
    CONVERSIONS["MEGAHERTZ:GIGAHERTZ"] = \
        Mul(Int(1000), Sym("megahz"))  # nopep8
    CONVERSIONS["MEGAHERTZ:HECTOHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 4)), Sym("megahz"))  # nopep8
    CONVERSIONS["MEGAHERTZ:HERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("megahz"))  # nopep8
    CONVERSIONS["MEGAHERTZ:KILOHERTZ"] = \
        Mul(Rat(Int(1), Int(1000)), Sym("megahz"))  # nopep8
    CONVERSIONS["MEGAHERTZ:MICROHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 12)), Sym("megahz"))  # nopep8
    CONVERSIONS["MEGAHERTZ:MILLIHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("megahz"))  # nopep8
    CONVERSIONS["MEGAHERTZ:NANOHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 15)), Sym("megahz"))  # nopep8
    CONVERSIONS["MEGAHERTZ:PETAHERTZ"] = \
        Mul(Pow(10, 9), Sym("megahz"))  # nopep8
    CONVERSIONS["MEGAHERTZ:PICOHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 18)), Sym("megahz"))  # nopep8
    CONVERSIONS["MEGAHERTZ:TERAHERTZ"] = \
        Mul(Pow(10, 6), Sym("megahz"))  # nopep8
    CONVERSIONS["MEGAHERTZ:YOCTOHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 30)), Sym("megahz"))  # nopep8
    CONVERSIONS["MEGAHERTZ:YOTTAHERTZ"] = \
        Mul(Pow(10, 18), Sym("megahz"))  # nopep8
    CONVERSIONS["MEGAHERTZ:ZEPTOHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 27)), Sym("megahz"))  # nopep8
    CONVERSIONS["MEGAHERTZ:ZETTAHERTZ"] = \
        Mul(Pow(10, 15), Sym("megahz"))  # nopep8
    CONVERSIONS["MICROHERTZ:ATTOHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 12)), Sym("microhz"))  # nopep8
    CONVERSIONS["MICROHERTZ:CENTIHERTZ"] = \
        Mul(Pow(10, 4), Sym("microhz"))  # nopep8
    CONVERSIONS["MICROHERTZ:DECAHERTZ"] = \
        Mul(Pow(10, 7), Sym("microhz"))  # nopep8
    CONVERSIONS["MICROHERTZ:DECIHERTZ"] = \
        Mul(Pow(10, 5), Sym("microhz"))  # nopep8
    CONVERSIONS["MICROHERTZ:EXAHERTZ"] = \
        Mul(Pow(10, 24), Sym("microhz"))  # nopep8
    CONVERSIONS["MICROHERTZ:FEMTOHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("microhz"))  # nopep8
    CONVERSIONS["MICROHERTZ:GIGAHERTZ"] = \
        Mul(Pow(10, 15), Sym("microhz"))  # nopep8
    CONVERSIONS["MICROHERTZ:HECTOHERTZ"] = \
        Mul(Pow(10, 8), Sym("microhz"))  # nopep8
    CONVERSIONS["MICROHERTZ:HERTZ"] = \
        Mul(Pow(10, 6), Sym("microhz"))  # nopep8
    CONVERSIONS["MICROHERTZ:KILOHERTZ"] = \
        Mul(Pow(10, 9), Sym("microhz"))  # nopep8
    CONVERSIONS["MICROHERTZ:MEGAHERTZ"] = \
        Mul(Pow(10, 12), Sym("microhz"))  # nopep8
    CONVERSIONS["MICROHERTZ:MILLIHERTZ"] = \
        Mul(Int(1000), Sym("microhz"))  # nopep8
    CONVERSIONS["MICROHERTZ:NANOHERTZ"] = \
        Mul(Rat(Int(1), Int(1000)), Sym("microhz"))  # nopep8
    CONVERSIONS["MICROHERTZ:PETAHERTZ"] = \
        Mul(Pow(10, 21), Sym("microhz"))  # nopep8
    CONVERSIONS["MICROHERTZ:PICOHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("microhz"))  # nopep8
    CONVERSIONS["MICROHERTZ:TERAHERTZ"] = \
        Mul(Pow(10, 18), Sym("microhz"))  # nopep8
    CONVERSIONS["MICROHERTZ:YOCTOHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 18)), Sym("microhz"))  # nopep8
    CONVERSIONS["MICROHERTZ:YOTTAHERTZ"] = \
        Mul(Pow(10, 30), Sym("microhz"))  # nopep8
    CONVERSIONS["MICROHERTZ:ZEPTOHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 15)), Sym("microhz"))  # nopep8
    CONVERSIONS["MICROHERTZ:ZETTAHERTZ"] = \
        Mul(Pow(10, 27), Sym("microhz"))  # nopep8
    CONVERSIONS["MILLIHERTZ:ATTOHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 15)), Sym("millihz"))  # nopep8
    CONVERSIONS["MILLIHERTZ:CENTIHERTZ"] = \
        Mul(Int(10), Sym("millihz"))  # nopep8
    CONVERSIONS["MILLIHERTZ:DECAHERTZ"] = \
        Mul(Pow(10, 4), Sym("millihz"))  # nopep8
    CONVERSIONS["MILLIHERTZ:DECIHERTZ"] = \
        Mul(Int(100), Sym("millihz"))  # nopep8
    CONVERSIONS["MILLIHERTZ:EXAHERTZ"] = \
        Mul(Pow(10, 21), Sym("millihz"))  # nopep8
    CONVERSIONS["MILLIHERTZ:FEMTOHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 12)), Sym("millihz"))  # nopep8
    CONVERSIONS["MILLIHERTZ:GIGAHERTZ"] = \
        Mul(Pow(10, 12), Sym("millihz"))  # nopep8
    CONVERSIONS["MILLIHERTZ:HECTOHERTZ"] = \
        Mul(Pow(10, 5), Sym("millihz"))  # nopep8
    CONVERSIONS["MILLIHERTZ:HERTZ"] = \
        Mul(Int(1000), Sym("millihz"))  # nopep8
    CONVERSIONS["MILLIHERTZ:KILOHERTZ"] = \
        Mul(Pow(10, 6), Sym("millihz"))  # nopep8
    CONVERSIONS["MILLIHERTZ:MEGAHERTZ"] = \
        Mul(Pow(10, 9), Sym("millihz"))  # nopep8
    CONVERSIONS["MILLIHERTZ:MICROHERTZ"] = \
        Mul(Rat(Int(1), Int(1000)), Sym("millihz"))  # nopep8
    CONVERSIONS["MILLIHERTZ:NANOHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("millihz"))  # nopep8
    CONVERSIONS["MILLIHERTZ:PETAHERTZ"] = \
        Mul(Pow(10, 18), Sym("millihz"))  # nopep8
    CONVERSIONS["MILLIHERTZ:PICOHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("millihz"))  # nopep8
    CONVERSIONS["MILLIHERTZ:TERAHERTZ"] = \
        Mul(Pow(10, 15), Sym("millihz"))  # nopep8
    CONVERSIONS["MILLIHERTZ:YOCTOHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 21)), Sym("millihz"))  # nopep8
    CONVERSIONS["MILLIHERTZ:YOTTAHERTZ"] = \
        Mul(Pow(10, 27), Sym("millihz"))  # nopep8
    CONVERSIONS["MILLIHERTZ:ZEPTOHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 18)), Sym("millihz"))  # nopep8
    CONVERSIONS["MILLIHERTZ:ZETTAHERTZ"] = \
        Mul(Pow(10, 24), Sym("millihz"))  # nopep8
    CONVERSIONS["NANOHERTZ:ATTOHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("nanohz"))  # nopep8
    CONVERSIONS["NANOHERTZ:CENTIHERTZ"] = \
        Mul(Pow(10, 7), Sym("nanohz"))  # nopep8
    CONVERSIONS["NANOHERTZ:DECAHERTZ"] = \
        Mul(Pow(10, 10), Sym("nanohz"))  # nopep8
    CONVERSIONS["NANOHERTZ:DECIHERTZ"] = \
        Mul(Pow(10, 8), Sym("nanohz"))  # nopep8
    CONVERSIONS["NANOHERTZ:EXAHERTZ"] = \
        Mul(Pow(10, 27), Sym("nanohz"))  # nopep8
    CONVERSIONS["NANOHERTZ:FEMTOHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("nanohz"))  # nopep8
    CONVERSIONS["NANOHERTZ:GIGAHERTZ"] = \
        Mul(Pow(10, 18), Sym("nanohz"))  # nopep8
    CONVERSIONS["NANOHERTZ:HECTOHERTZ"] = \
        Mul(Pow(10, 11), Sym("nanohz"))  # nopep8
    CONVERSIONS["NANOHERTZ:HERTZ"] = \
        Mul(Pow(10, 9), Sym("nanohz"))  # nopep8
    CONVERSIONS["NANOHERTZ:KILOHERTZ"] = \
        Mul(Pow(10, 12), Sym("nanohz"))  # nopep8
    CONVERSIONS["NANOHERTZ:MEGAHERTZ"] = \
        Mul(Pow(10, 15), Sym("nanohz"))  # nopep8
    CONVERSIONS["NANOHERTZ:MICROHERTZ"] = \
        Mul(Int(1000), Sym("nanohz"))  # nopep8
    CONVERSIONS["NANOHERTZ:MILLIHERTZ"] = \
        Mul(Pow(10, 6), Sym("nanohz"))  # nopep8
    CONVERSIONS["NANOHERTZ:PETAHERTZ"] = \
        Mul(Pow(10, 24), Sym("nanohz"))  # nopep8
    CONVERSIONS["NANOHERTZ:PICOHERTZ"] = \
        Mul(Rat(Int(1), Int(1000)), Sym("nanohz"))  # nopep8
    CONVERSIONS["NANOHERTZ:TERAHERTZ"] = \
        Mul(Pow(10, 21), Sym("nanohz"))  # nopep8
    CONVERSIONS["NANOHERTZ:YOCTOHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 15)), Sym("nanohz"))  # nopep8
    CONVERSIONS["NANOHERTZ:YOTTAHERTZ"] = \
        Mul(Pow(10, 33), Sym("nanohz"))  # nopep8
    CONVERSIONS["NANOHERTZ:ZEPTOHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 12)), Sym("nanohz"))  # nopep8
    CONVERSIONS["NANOHERTZ:ZETTAHERTZ"] = \
        Mul(Pow(10, 30), Sym("nanohz"))  # nopep8
    CONVERSIONS["PETAHERTZ:ATTOHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 33)), Sym("petahz"))  # nopep8
    CONVERSIONS["PETAHERTZ:CENTIHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 17)), Sym("petahz"))  # nopep8
    CONVERSIONS["PETAHERTZ:DECAHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 14)), Sym("petahz"))  # nopep8
    CONVERSIONS["PETAHERTZ:DECIHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 16)), Sym("petahz"))  # nopep8
    CONVERSIONS["PETAHERTZ:EXAHERTZ"] = \
        Mul(Int(1000), Sym("petahz"))  # nopep8
    CONVERSIONS["PETAHERTZ:FEMTOHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 30)), Sym("petahz"))  # nopep8
    CONVERSIONS["PETAHERTZ:GIGAHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("petahz"))  # nopep8
    CONVERSIONS["PETAHERTZ:HECTOHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 13)), Sym("petahz"))  # nopep8
    CONVERSIONS["PETAHERTZ:HERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 15)), Sym("petahz"))  # nopep8
    CONVERSIONS["PETAHERTZ:KILOHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 12)), Sym("petahz"))  # nopep8
    CONVERSIONS["PETAHERTZ:MEGAHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("petahz"))  # nopep8
    CONVERSIONS["PETAHERTZ:MICROHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 21)), Sym("petahz"))  # nopep8
    CONVERSIONS["PETAHERTZ:MILLIHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 18)), Sym("petahz"))  # nopep8
    CONVERSIONS["PETAHERTZ:NANOHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 24)), Sym("petahz"))  # nopep8
    CONVERSIONS["PETAHERTZ:PICOHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 27)), Sym("petahz"))  # nopep8
    CONVERSIONS["PETAHERTZ:TERAHERTZ"] = \
        Mul(Rat(Int(1), Int(1000)), Sym("petahz"))  # nopep8
    CONVERSIONS["PETAHERTZ:YOCTOHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 39)), Sym("petahz"))  # nopep8
    CONVERSIONS["PETAHERTZ:YOTTAHERTZ"] = \
        Mul(Pow(10, 9), Sym("petahz"))  # nopep8
    CONVERSIONS["PETAHERTZ:ZEPTOHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 36)), Sym("petahz"))  # nopep8
    CONVERSIONS["PETAHERTZ:ZETTAHERTZ"] = \
        Mul(Pow(10, 6), Sym("petahz"))  # nopep8
    CONVERSIONS["PICOHERTZ:ATTOHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("picohz"))  # nopep8
    CONVERSIONS["PICOHERTZ:CENTIHERTZ"] = \
        Mul(Pow(10, 10), Sym("picohz"))  # nopep8
    CONVERSIONS["PICOHERTZ:DECAHERTZ"] = \
        Mul(Pow(10, 13), Sym("picohz"))  # nopep8
    CONVERSIONS["PICOHERTZ:DECIHERTZ"] = \
        Mul(Pow(10, 11), Sym("picohz"))  # nopep8
    CONVERSIONS["PICOHERTZ:EXAHERTZ"] = \
        Mul(Pow(10, 30), Sym("picohz"))  # nopep8
    CONVERSIONS["PICOHERTZ:FEMTOHERTZ"] = \
        Mul(Rat(Int(1), Int(1000)), Sym("picohz"))  # nopep8
    CONVERSIONS["PICOHERTZ:GIGAHERTZ"] = \
        Mul(Pow(10, 21), Sym("picohz"))  # nopep8
    CONVERSIONS["PICOHERTZ:HECTOHERTZ"] = \
        Mul(Pow(10, 14), Sym("picohz"))  # nopep8
    CONVERSIONS["PICOHERTZ:HERTZ"] = \
        Mul(Pow(10, 12), Sym("picohz"))  # nopep8
    CONVERSIONS["PICOHERTZ:KILOHERTZ"] = \
        Mul(Pow(10, 15), Sym("picohz"))  # nopep8
    CONVERSIONS["PICOHERTZ:MEGAHERTZ"] = \
        Mul(Pow(10, 18), Sym("picohz"))  # nopep8
    CONVERSIONS["PICOHERTZ:MICROHERTZ"] = \
        Mul(Pow(10, 6), Sym("picohz"))  # nopep8
    CONVERSIONS["PICOHERTZ:MILLIHERTZ"] = \
        Mul(Pow(10, 9), Sym("picohz"))  # nopep8
    CONVERSIONS["PICOHERTZ:NANOHERTZ"] = \
        Mul(Int(1000), Sym("picohz"))  # nopep8
    CONVERSIONS["PICOHERTZ:PETAHERTZ"] = \
        Mul(Pow(10, 27), Sym("picohz"))  # nopep8
    CONVERSIONS["PICOHERTZ:TERAHERTZ"] = \
        Mul(Pow(10, 24), Sym("picohz"))  # nopep8
    CONVERSIONS["PICOHERTZ:YOCTOHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 12)), Sym("picohz"))  # nopep8
    CONVERSIONS["PICOHERTZ:YOTTAHERTZ"] = \
        Mul(Pow(10, 36), Sym("picohz"))  # nopep8
    CONVERSIONS["PICOHERTZ:ZEPTOHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("picohz"))  # nopep8
    CONVERSIONS["PICOHERTZ:ZETTAHERTZ"] = \
        Mul(Pow(10, 33), Sym("picohz"))  # nopep8
    CONVERSIONS["TERAHERTZ:ATTOHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 30)), Sym("terahz"))  # nopep8
    CONVERSIONS["TERAHERTZ:CENTIHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 14)), Sym("terahz"))  # nopep8
    CONVERSIONS["TERAHERTZ:DECAHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 11)), Sym("terahz"))  # nopep8
    CONVERSIONS["TERAHERTZ:DECIHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 13)), Sym("terahz"))  # nopep8
    CONVERSIONS["TERAHERTZ:EXAHERTZ"] = \
        Mul(Pow(10, 6), Sym("terahz"))  # nopep8
    CONVERSIONS["TERAHERTZ:FEMTOHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 27)), Sym("terahz"))  # nopep8
    CONVERSIONS["TERAHERTZ:GIGAHERTZ"] = \
        Mul(Rat(Int(1), Int(1000)), Sym("terahz"))  # nopep8
    CONVERSIONS["TERAHERTZ:HECTOHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 10)), Sym("terahz"))  # nopep8
    CONVERSIONS["TERAHERTZ:HERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 12)), Sym("terahz"))  # nopep8
    CONVERSIONS["TERAHERTZ:KILOHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("terahz"))  # nopep8
    CONVERSIONS["TERAHERTZ:MEGAHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("terahz"))  # nopep8
    CONVERSIONS["TERAHERTZ:MICROHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 18)), Sym("terahz"))  # nopep8
    CONVERSIONS["TERAHERTZ:MILLIHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 15)), Sym("terahz"))  # nopep8
    CONVERSIONS["TERAHERTZ:NANOHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 21)), Sym("terahz"))  # nopep8
    CONVERSIONS["TERAHERTZ:PETAHERTZ"] = \
        Mul(Int(1000), Sym("terahz"))  # nopep8
    CONVERSIONS["TERAHERTZ:PICOHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 24)), Sym("terahz"))  # nopep8
    CONVERSIONS["TERAHERTZ:YOCTOHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 36)), Sym("terahz"))  # nopep8
    CONVERSIONS["TERAHERTZ:YOTTAHERTZ"] = \
        Mul(Pow(10, 12), Sym("terahz"))  # nopep8
    CONVERSIONS["TERAHERTZ:ZEPTOHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 33)), Sym("terahz"))  # nopep8
    CONVERSIONS["TERAHERTZ:ZETTAHERTZ"] = \
        Mul(Pow(10, 9), Sym("terahz"))  # nopep8
    CONVERSIONS["YOCTOHERTZ:ATTOHERTZ"] = \
        Mul(Pow(10, 6), Sym("yoctohz"))  # nopep8
    CONVERSIONS["YOCTOHERTZ:CENTIHERTZ"] = \
        Mul(Pow(10, 22), Sym("yoctohz"))  # nopep8
    CONVERSIONS["YOCTOHERTZ:DECAHERTZ"] = \
        Mul(Pow(10, 25), Sym("yoctohz"))  # nopep8
    CONVERSIONS["YOCTOHERTZ:DECIHERTZ"] = \
        Mul(Pow(10, 23), Sym("yoctohz"))  # nopep8
    CONVERSIONS["YOCTOHERTZ:EXAHERTZ"] = \
        Mul(Pow(10, 42), Sym("yoctohz"))  # nopep8
    CONVERSIONS["YOCTOHERTZ:FEMTOHERTZ"] = \
        Mul(Pow(10, 9), Sym("yoctohz"))  # nopep8
    CONVERSIONS["YOCTOHERTZ:GIGAHERTZ"] = \
        Mul(Pow(10, 33), Sym("yoctohz"))  # nopep8
    CONVERSIONS["YOCTOHERTZ:HECTOHERTZ"] = \
        Mul(Pow(10, 26), Sym("yoctohz"))  # nopep8
    CONVERSIONS["YOCTOHERTZ:HERTZ"] = \
        Mul(Pow(10, 24), Sym("yoctohz"))  # nopep8
    CONVERSIONS["YOCTOHERTZ:KILOHERTZ"] = \
        Mul(Pow(10, 27), Sym("yoctohz"))  # nopep8
    CONVERSIONS["YOCTOHERTZ:MEGAHERTZ"] = \
        Mul(Pow(10, 30), Sym("yoctohz"))  # nopep8
    CONVERSIONS["YOCTOHERTZ:MICROHERTZ"] = \
        Mul(Pow(10, 18), Sym("yoctohz"))  # nopep8
    CONVERSIONS["YOCTOHERTZ:MILLIHERTZ"] = \
        Mul(Pow(10, 21), Sym("yoctohz"))  # nopep8
    CONVERSIONS["YOCTOHERTZ:NANOHERTZ"] = \
        Mul(Pow(10, 15), Sym("yoctohz"))  # nopep8
    CONVERSIONS["YOCTOHERTZ:PETAHERTZ"] = \
        Mul(Pow(10, 39), Sym("yoctohz"))  # nopep8
    CONVERSIONS["YOCTOHERTZ:PICOHERTZ"] = \
        Mul(Pow(10, 12), Sym("yoctohz"))  # nopep8
    CONVERSIONS["YOCTOHERTZ:TERAHERTZ"] = \
        Mul(Pow(10, 36), Sym("yoctohz"))  # nopep8
    CONVERSIONS["YOCTOHERTZ:YOTTAHERTZ"] = \
        Mul(Pow(10, 48), Sym("yoctohz"))  # nopep8
    CONVERSIONS["YOCTOHERTZ:ZEPTOHERTZ"] = \
        Mul(Int(1000), Sym("yoctohz"))  # nopep8
    CONVERSIONS["YOCTOHERTZ:ZETTAHERTZ"] = \
        Mul(Pow(10, 45), Sym("yoctohz"))  # nopep8
    CONVERSIONS["YOTTAHERTZ:ATTOHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 42)), Sym("yottahz"))  # nopep8
    CONVERSIONS["YOTTAHERTZ:CENTIHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 26)), Sym("yottahz"))  # nopep8
    CONVERSIONS["YOTTAHERTZ:DECAHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 23)), Sym("yottahz"))  # nopep8
    CONVERSIONS["YOTTAHERTZ:DECIHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 25)), Sym("yottahz"))  # nopep8
    CONVERSIONS["YOTTAHERTZ:EXAHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("yottahz"))  # nopep8
    CONVERSIONS["YOTTAHERTZ:FEMTOHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 39)), Sym("yottahz"))  # nopep8
    CONVERSIONS["YOTTAHERTZ:GIGAHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 15)), Sym("yottahz"))  # nopep8
    CONVERSIONS["YOTTAHERTZ:HECTOHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 22)), Sym("yottahz"))  # nopep8
    CONVERSIONS["YOTTAHERTZ:HERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 24)), Sym("yottahz"))  # nopep8
    CONVERSIONS["YOTTAHERTZ:KILOHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 21)), Sym("yottahz"))  # nopep8
    CONVERSIONS["YOTTAHERTZ:MEGAHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 18)), Sym("yottahz"))  # nopep8
    CONVERSIONS["YOTTAHERTZ:MICROHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 30)), Sym("yottahz"))  # nopep8
    CONVERSIONS["YOTTAHERTZ:MILLIHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 27)), Sym("yottahz"))  # nopep8
    CONVERSIONS["YOTTAHERTZ:NANOHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 33)), Sym("yottahz"))  # nopep8
    CONVERSIONS["YOTTAHERTZ:PETAHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("yottahz"))  # nopep8
    CONVERSIONS["YOTTAHERTZ:PICOHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 36)), Sym("yottahz"))  # nopep8
    CONVERSIONS["YOTTAHERTZ:TERAHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 12)), Sym("yottahz"))  # nopep8
    CONVERSIONS["YOTTAHERTZ:YOCTOHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 48)), Sym("yottahz"))  # nopep8
    CONVERSIONS["YOTTAHERTZ:ZEPTOHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 45)), Sym("yottahz"))  # nopep8
    CONVERSIONS["YOTTAHERTZ:ZETTAHERTZ"] = \
        Mul(Rat(Int(1), Int(1000)), Sym("yottahz"))  # nopep8
    CONVERSIONS["ZEPTOHERTZ:ATTOHERTZ"] = \
        Mul(Int(1000), Sym("zeptohz"))  # nopep8
    CONVERSIONS["ZEPTOHERTZ:CENTIHERTZ"] = \
        Mul(Pow(10, 19), Sym("zeptohz"))  # nopep8
    CONVERSIONS["ZEPTOHERTZ:DECAHERTZ"] = \
        Mul(Pow(10, 22), Sym("zeptohz"))  # nopep8
    CONVERSIONS["ZEPTOHERTZ:DECIHERTZ"] = \
        Mul(Pow(10, 20), Sym("zeptohz"))  # nopep8
    CONVERSIONS["ZEPTOHERTZ:EXAHERTZ"] = \
        Mul(Pow(10, 39), Sym("zeptohz"))  # nopep8
    CONVERSIONS["ZEPTOHERTZ:FEMTOHERTZ"] = \
        Mul(Pow(10, 6), Sym("zeptohz"))  # nopep8
    CONVERSIONS["ZEPTOHERTZ:GIGAHERTZ"] = \
        Mul(Pow(10, 30), Sym("zeptohz"))  # nopep8
    CONVERSIONS["ZEPTOHERTZ:HECTOHERTZ"] = \
        Mul(Pow(10, 23), Sym("zeptohz"))  # nopep8
    CONVERSIONS["ZEPTOHERTZ:HERTZ"] = \
        Mul(Pow(10, 21), Sym("zeptohz"))  # nopep8
    CONVERSIONS["ZEPTOHERTZ:KILOHERTZ"] = \
        Mul(Pow(10, 24), Sym("zeptohz"))  # nopep8
    CONVERSIONS["ZEPTOHERTZ:MEGAHERTZ"] = \
        Mul(Pow(10, 27), Sym("zeptohz"))  # nopep8
    CONVERSIONS["ZEPTOHERTZ:MICROHERTZ"] = \
        Mul(Pow(10, 15), Sym("zeptohz"))  # nopep8
    CONVERSIONS["ZEPTOHERTZ:MILLIHERTZ"] = \
        Mul(Pow(10, 18), Sym("zeptohz"))  # nopep8
    CONVERSIONS["ZEPTOHERTZ:NANOHERTZ"] = \
        Mul(Pow(10, 12), Sym("zeptohz"))  # nopep8
    CONVERSIONS["ZEPTOHERTZ:PETAHERTZ"] = \
        Mul(Pow(10, 36), Sym("zeptohz"))  # nopep8
    CONVERSIONS["ZEPTOHERTZ:PICOHERTZ"] = \
        Mul(Pow(10, 9), Sym("zeptohz"))  # nopep8
    CONVERSIONS["ZEPTOHERTZ:TERAHERTZ"] = \
        Mul(Pow(10, 33), Sym("zeptohz"))  # nopep8
    CONVERSIONS["ZEPTOHERTZ:YOCTOHERTZ"] = \
        Mul(Rat(Int(1), Int(1000)), Sym("zeptohz"))  # nopep8
    CONVERSIONS["ZEPTOHERTZ:YOTTAHERTZ"] = \
        Mul(Pow(10, 45), Sym("zeptohz"))  # nopep8
    CONVERSIONS["ZEPTOHERTZ:ZETTAHERTZ"] = \
        Mul(Pow(10, 42), Sym("zeptohz"))  # nopep8
    CONVERSIONS["ZETTAHERTZ:ATTOHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 39)), Sym("zettahz"))  # nopep8
    CONVERSIONS["ZETTAHERTZ:CENTIHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 23)), Sym("zettahz"))  # nopep8
    CONVERSIONS["ZETTAHERTZ:DECAHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 20)), Sym("zettahz"))  # nopep8
    CONVERSIONS["ZETTAHERTZ:DECIHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 22)), Sym("zettahz"))  # nopep8
    CONVERSIONS["ZETTAHERTZ:EXAHERTZ"] = \
        Mul(Rat(Int(1), Int(1000)), Sym("zettahz"))  # nopep8
    CONVERSIONS["ZETTAHERTZ:FEMTOHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 36)), Sym("zettahz"))  # nopep8
    CONVERSIONS["ZETTAHERTZ:GIGAHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 12)), Sym("zettahz"))  # nopep8
    CONVERSIONS["ZETTAHERTZ:HECTOHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 19)), Sym("zettahz"))  # nopep8
    CONVERSIONS["ZETTAHERTZ:HERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 21)), Sym("zettahz"))  # nopep8
    CONVERSIONS["ZETTAHERTZ:KILOHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 18)), Sym("zettahz"))  # nopep8
    CONVERSIONS["ZETTAHERTZ:MEGAHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 15)), Sym("zettahz"))  # nopep8
    CONVERSIONS["ZETTAHERTZ:MICROHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 27)), Sym("zettahz"))  # nopep8
    CONVERSIONS["ZETTAHERTZ:MILLIHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 24)), Sym("zettahz"))  # nopep8
    CONVERSIONS["ZETTAHERTZ:NANOHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 30)), Sym("zettahz"))  # nopep8
    CONVERSIONS["ZETTAHERTZ:PETAHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 6)), Sym("zettahz"))  # nopep8
    CONVERSIONS["ZETTAHERTZ:PICOHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 33)), Sym("zettahz"))  # nopep8
    CONVERSIONS["ZETTAHERTZ:TERAHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 9)), Sym("zettahz"))  # nopep8
    CONVERSIONS["ZETTAHERTZ:YOCTOHERTZ"] = \
        Mul(Rat(Int(1), Pow(10, 45)), Sym("zettahz"))  # nopep8
    CONVERSIONS["ZETTAHERTZ:YOTTAHERTZ"] = \
        Mul(Int(1000), Sym("zettahz"))  # nopep8
    CONVERSIONS["ZETTAHERTZ:ZEPTOHERTZ"] = \
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
