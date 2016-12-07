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


class TestBuildQuery(object):

    @classmethod
    @pytest.fixture(autouse=True)
    def setup_class(cls, tmpdir, monkeypatch):
        ice_config = tmpdir / "ice.config"
        ice_config.write("omero.host=localhost\nomero.port=4064")
        monkeypatch.setenv("ICE_CONFIG", ice_config)
        cls.g = _BlitzGateway()

    @pytest.mark.parametrize("dtype", KNOWN_WRAPPERS.keys())
    def test_no_clauses(self, dtype):
        """Expect a query with no 'where' clauses."""
        result = self.g.buildQuery(dtype)
        query, params, wrapper = result
        assert isinstance(query, str)
        assert isinstance(params, Parameters)
        assert isinstance(wrapper(), BlitzObjectWrapper)
        assert query.startswith("select ")
        assert "where" not in query

    @pytest.mark.parametrize("dtype", KNOWN_WRAPPERS.keys())
    def test_filter_by_owner(self, dtype):
        """Query should filter by owner."""
        p = ParametersI()
        p.theFilter = Filter()
        p.theFilter.ownerId = wrap(2)
        # Test using 'params' argument
        with_params = self.g.buildQuery(dtype, params=p)
        # Test using 'opts' dictionary
        with_opts = self.g.buildQuery(dtype, opts={'owner': 1})
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
    def test_pagination(self, dtype):
        """Query should paginate."""
        offset = 1
        limit = 100
        p = ParametersI()
        p.page(offset, limit)
        # Test using 'params' argument
        with_params = self.g.buildQuery(dtype, params=p)
        # Test using 'opts' dictionary
        opts = {'offset': offset, 'limit': limit}
        with_opts = self.g.buildQuery(dtype, opts=opts)
        for result in [with_params, with_opts]:
            query, params, wrapper = result
            assert isinstance(query, str)
            assert isinstance(params, Parameters)
            assert isinstance(wrapper(), BlitzObjectWrapper)
            assert params.theFilter.offset.val == offset
            assert params.theFilter.limit.val == limit
