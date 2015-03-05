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
Code-generated omero.model.Temperature implementation,
based on omero.model.PermissionsI
"""


import Ice
import IceImport
IceImport.load("omero_model_Temperature_ice")
_omero = Ice.openModule("omero")
_omero_model = Ice.openModule("omero.model")
__name__ = "omero.model"

from omero_model_UnitBase import UnitBase
from omero.model.enums import UnitsTemperature

from omero.conversions import Add  # nopep8
from omero.conversions import Int  # nopep8
from omero.conversions import Mul  # nopep8
from omero.conversions import Pow  # nopep8
from omero.conversions import Rat  # nopep8
from omero.conversions import Sym  # nopep8


class TemperatureI(_omero_model.Temperature, UnitBase):

    try:
        UNIT_VALUES = sorted(UnitsTemperature._enumerators.values())
    except:
        # TODO: this occurs on Ice 3.4 and can be removed
        # once it has been dropped.
        UNIT_VALUES = [x for x in sorted(UnitsTemperature._names)]
        UNIT_VALUES = [getattr(UnitsTemperature, x) for x in UNIT_VALUES]
    CONVERSIONS = dict()
    for val in UNIT_VALUES:
        CONVERSIONS[val] = dict()
    CONVERSIONS[UnitsTemperature.CELSIUS][UnitsTemperature.FAHRENHEIT] = \
        Add(Mul(Rat(Int(9), Int(5)), Sym("c")), Int(32))  # nopep8
    CONVERSIONS[UnitsTemperature.CELSIUS][UnitsTemperature.KELVIN] = \
        Add(Sym("c"), Rat(Int(5463), Int(20)))  # nopep8
    CONVERSIONS[UnitsTemperature.CELSIUS][UnitsTemperature.RANKINE] = \
        Add(Mul(Rat(Int(9), Int(5)), Sym("c")), Rat(Int(49167), Int(100)))  # nopep8
    CONVERSIONS[UnitsTemperature.FAHRENHEIT][UnitsTemperature.CELSIUS] = \
        Add(Mul(Rat(Int(5), Int(9)), Sym("f")), Rat(Int(-160), Int(9)))  # nopep8
    CONVERSIONS[UnitsTemperature.FAHRENHEIT][UnitsTemperature.KELVIN] = \
        Add(Mul(Rat(Int(5), Int(9)), Sym("f")), Rat(Int(45967), Int(180)))  # nopep8
    CONVERSIONS[UnitsTemperature.FAHRENHEIT][UnitsTemperature.RANKINE] = \
        Add(Sym("f"), Rat(Int(45967), Int(100)))  # nopep8
    CONVERSIONS[UnitsTemperature.KELVIN][UnitsTemperature.CELSIUS] = \
        Add(Sym("k"), Rat(Int(-5463), Int(20)))  # nopep8
    CONVERSIONS[UnitsTemperature.KELVIN][UnitsTemperature.FAHRENHEIT] = \
        Add(Mul(Rat(Int(9), Int(5)), Sym("k")), Rat(Int(-45967), Int(100)))  # nopep8
    CONVERSIONS[UnitsTemperature.KELVIN][UnitsTemperature.RANKINE] = \
        Mul(Rat(Int(9), Int(5)), Sym("k"))  # nopep8
    CONVERSIONS[UnitsTemperature.RANKINE][UnitsTemperature.CELSIUS] = \
        Add(Mul(Rat(Int(5), Int(9)), Sym("r")), Rat(Int(-5463), Int(20)))  # nopep8
    CONVERSIONS[UnitsTemperature.RANKINE][UnitsTemperature.FAHRENHEIT] = \
        Add(Sym("r"), Rat(Int(-45967), Int(100)))  # nopep8
    CONVERSIONS[UnitsTemperature.RANKINE][UnitsTemperature.KELVIN] = \
        Mul(Rat(Int(5), Int(9)), Sym("r"))  # nopep8

    SYMBOLS = dict()
    SYMBOLS["CELSIUS"] = "°C"
    SYMBOLS["FAHRENHEIT"] = "°F"
    SYMBOLS["KELVIN"] = "K"
    SYMBOLS["RANKINE"] = "°R"

    def __init__(self, value=None, unit=None):
        _omero_model.Temperature.__init__(self)
        if isinstance(value, _omero_model.TemperatureI):
            # This is a copy-constructor call.
            target = str(unit)
            targetUnit = getattr(UnitsTemperature, str(target))
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
        return TemperatureI.SYMBOLS.get(str(unit))

    def setUnit(self, unit, current=None):
        self._unit = unit

    def setValue(self, value, current=None):
        self._value = value

    def __str__(self):
        return self._base_string(self.getValue(), self.getUnit())

_omero_model.TemperatureI = TemperatureI
