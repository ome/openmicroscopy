#!/usr/bin/env python
# -*- coding: utf-8 -*-

#
# Copyright (C) 2015 University of Dundee & Open Microscopy Environment.
# All rights reserved.
#
# This program is free software: you can redistribute it and/or modify
# it under the terms of the GNU Affero General Public License as
# published by the Free Software Foundation, either version 3 of the
# License, or (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU Affero General Public License for more details.
#
# You should have received a copy of the GNU Affero General Public License
# along with this program.  If not, see <http://www.gnu.org/licenses/>.
#
"""
Simple unit tests for the "webclient_utils" module.
"""

from omeroweb.webclient.webclient_utils import formatPercentFraction


class TestUtil(object):
    """
    Tests various util methods
    """

    def test_format_percent_fraction(self):

        assert formatPercentFraction(1) == "100"
        assert formatPercentFraction(0.805) == "81"
        assert formatPercentFraction(0.2) == "20"
        assert formatPercentFraction(0.01) == "1"
        assert formatPercentFraction(0.005) == "0.5"
        assert formatPercentFraction(0.005) == "0.5"
        assert formatPercentFraction(0.0025) == "0.3"
        assert formatPercentFraction(0.00) == "0.0"
