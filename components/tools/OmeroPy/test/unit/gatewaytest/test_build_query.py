#!/usr/bin/env python
# -*- coding: utf-8 -*-

#
# Copyright (C) 2016 University of Dundee & Open Microscopy Environment.
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

"""Gateway tests - buildQuery() as used by conn.getObjects()."""

from omero.gateway import _BlitzGateway, BlitzObjectWrapper, KNOWN_WRAPPERS
from omero.sys import Parameters, ParametersI, Filter
import pytest
from omero.rtypes import wrap


@pytest.fixture(scope='function')
def gateway():
    """Create a BlitzGateway object."""
    return _BlitzGateway()


class TestBuildQuery(object):
    """Test the conn.buildQuery() method for all Object Wrappers."""

    @pytest.mark.parametrize("dtype", KNOWN_WRAPPERS.keys())
    def test_no_clauses(self, gateway, dtype):
        """Expect a query with no 'where' clauses."""
        result = gateway.buildQuery(dtype)
        query, params, wrapper = result
        assert isinstance(query, str)
        assert isinstance(params, Parameters)
        assert isinstance(wrapper(), BlitzObjectWrapper)
        assert query.startswith("select ")
        assert "where" not in query
        assert 'None' not in query

    @pytest.mark.parametrize("dtype", KNOWN_WRAPPERS.keys())
    def test_filter_by_owner(self, gateway, dtype):
        """Query should filter by owner."""
        p = ParametersI()
        p.theFilter = Filter()
        p.theFilter.ownerId = wrap(2)
        # Test using 'params' argument
        with_params = gateway.buildQuery(dtype, params=p)
        # Test using 'opts' dictionary
        with_opts = gateway.buildQuery(dtype, opts={'owner': 1})
        for result in [with_params, with_opts]:
            query, params, wrapper = result
            assert isinstance(query, str)
            assert isinstance(params, Parameters)
            assert isinstance(wrapper(), BlitzObjectWrapper)
            if dtype not in ('experimenter', 'experimentergroup'):
                assert "where owner" in query
            else:
                assert "where owner" not in query

    @pytest.mark.parametrize("dtype", KNOWN_WRAPPERS.keys())
    def test_pagination(self, gateway, dtype):
        """Query should paginate."""
        offset = 1
        limit = 100
        p = ParametersI()
        p.page(offset, limit)
        # Test using 'params' argument
        with_params = gateway.buildQuery(dtype, params=p)
        # Test using 'opts' dictionary
        opts = {'offset': offset, 'limit': limit}
        with_opts = gateway.buildQuery(dtype, opts=opts)
        for result in [with_params, with_opts]:
            query, params, wrapper = result
            assert isinstance(query, str)
            assert isinstance(params, Parameters)
            assert isinstance(wrapper(), BlitzObjectWrapper)
            assert params.theFilter.offset.val == offset
            assert params.theFilter.limit.val == limit
