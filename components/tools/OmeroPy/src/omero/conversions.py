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
Conversion utilities for changing between units.
"""


class Conversion(object):
    """
    Base-functor like object which can be used for preparing complex
    equations for converting from one unit to another. Primarily these
    classes and static methods are used via code-generation. Sympy-generated
    strings are placed directly into code. If the proper imports are in place,
    then a top-level Conversion (usually of type Add or
    Mul is returned from the evaluation).
    """

    def __init__(self, *conversions):
        self.conversions = conversions

    def __call__(self, original):
        raise NotImplemented()

    def join(self, sym):
        sb = sym.join([str(x) for x in self.conversions])
        return "(%s)" % sb


class Add(Conversion):
    """
    Sums all the Conversion instances which
    are passed in to the constructor.
    """

    def __call__(self, original):
        rv = 0.0
        for c in self.conversions:
            rv += c(original)
        return rv

    def __str__(self):
        return self.join(" + ")


class Int(Conversion):
    """
    Simple representation of a possibly
    very large integer.
    """

    def __init__(self, i):
        if isinstance(i, int):
            self.i = i
        else:
            self.i = float(i)  # Handles big strings

    def __call__(self, original):
        return self.i

    def __str__(self):
        return str(self.i)


class Mul(Conversion):
    """
    Multiplies all the Conversion instances which
    are passed in to the constructor.
    """

    def __call__(self, original):
        rv = 1.0
        for c in self.conversions:
            rv *= c(original)
        return rv

    def __str__(self):
        return self.join(" * ")


class Pow(Conversion):
    """
    Raises the first argument (base) to
    the power of the second (exponent).
    """

    def __init__(self, base, exp):
        self.base = base
        self.exp = exp

    def __call__(self, original):
        return self.base ** self.exp

    def __str__(self):
        return "(%s ** %s)" % (self.base, self.exp)


class Rat(Conversion):
    """
    Divides the first argument (numerator)
    by the second (denominator).
    """

    def __init__(self, n, d):
        self.n = n
        self.d = d

    def unwrap(self, x, original):
        if isinstance(x, (int, float, str)):
            return float(x)
        else:
            return x(original)

    def __call__(self, original):
        n = self.unwrap(self.n, original)
        d = self.unwrap(self.d, original)
        return float(n) / d

    def __str__(self):
        return "(%s / %s)" % (self.n, self.d)


class Sym(Conversion):
    """
    Represents the variable of the source
    unit and simply returns the original
    value passed to it.
    """

    def __init__(self, s):
        self.s = s

    def __call__(self, original):
        return float(original)

    def __str__(self):
        return "x"
