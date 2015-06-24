#!/usr/bin/env python
# -*- coding: utf-8 -*-

#
# Copyright (C) 2015 University of Dundee & Open Microscopy Environment.
# All rights reserved.
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
Simple tests of the new conversions used for units.
"""

from pytest import assertAlmostEqual
from omero.conversions import Add
from omero.conversions import Int
from omero.conversions import Mul
from omero.conversions import Pow
from omero.conversions import Rat
from omero.conversions import Sym


class TestConversions(object):

    def assertEquals(self, a, b, places=6):
        assertAlmostEqual(a, b, places=places)

    def testSimpleAdd(self):
        add = Add(Rat(1, 2), Rat(1, 2))
        assert "(1 + 2)", str(add)
        whole = add(-1)  # -1 is ignored
        self.assertEquals(1.0, whole)

    def testSimpleMul(self):
        mul = Mul(Int(1000000), Sym("megas"))
        assert "(1000000 * x)" == str(mul)
        seconds = mul(5.0)
        self.assertEquals(5000000.0, seconds)

    def testSimpleInt(self):
        i = Int(123)
        assert "123" == str(i)
        x = i(-1)  # -1 is ignored
        self.assertEquals(123.0, x)

    def testBigInt(self):
        big = "123456789012345678901234567890"
        big = big * 5
        i = Mul(Int(big), Int(big))
        x = i(-1)  # -1 is ignored
        target = float(big) * float(big)
        self.assertEquals(target, x)

    def testSimplePow(self):
        p = Pow(3, 2)
        assert "(3 ** 2)" == str(p)
        x = p(-1)  # -1 is ignored
        self.assertEquals(9.0, x)

    def testSimpleRat(self):
        r = Rat(1, 3)
        assert "(1 / 3)" == str(r)
        x = r(-1)  # -1 is ignored
        self.assertEquals(0.333333, x)

    def testDelayedRat(self):
        r = Rat(Int(1), Int(3))
        x = r(-1)  # -1 is ignored
        self.assertEquals(0.333333, x)

    def testSimpleSym(self):
        s = Sym("s")
        assert "x" == str(s)
        x = s(5.0)
        self.assertEquals(5.0, x)

    def testFahrenheitCelsius(self):
        ftoc = Add(Mul(Rat(5, 9), Sym("f")), Rat(-160, 9))
        assert "(((5 / 9) * x) + (-160 / 9))" == str(ftoc)
        self.assertEquals(0.0, ftoc(32.0))
        self.assertEquals(100.0, ftoc(212.0))
        self.assertEquals(-40.0, ftoc(-40.0))
