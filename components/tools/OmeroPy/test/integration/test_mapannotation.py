#!/usr/bin/env python
# -*- coding: utf-8 -*-

#
# Copyright (C) 2014 University of Dundee & Open Microscopy Environment.
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
Tests for the MapAnnotation and related base types
introduced in 5.1.
"""

import test.integration.library as lib
import pytest
import omero

from omero_model_ExperimenterGroupI import ExperimenterGroupI
from omero.rtypes import rbool, rstring
from omero.rtypes import unwrap


class TestMapAnnotation(lib.ITest):

    def testMapStringField(self):
        uuid = self.uuid()
        queryService = self.root.getSession().getQueryService()
        updateService = self.root.getSession().getUpdateService()
        group = ExperimenterGroupI()
        group.setName(rstring(uuid))
        group.setLdap(rbool(False))
        group.setConfig(dict())
        group.getConfig()["language"] = rstring("python")
        group = updateService.saveAndReturnObject(group)
        group = queryService.findByQuery(
            ("select g from ExperimenterGroup g join fetch g.config "
             "where g.id = %s" % group.getId().getValue()), None)
        assert "python" == group.getConfig().get("language").val

    @pytest.mark.parametrize("data", (
        ({"a": rstring("")}, {"a": ""}),
        ({"a": None}, {}),
        ({"a": rstring("b")}, {"a": "b"}),
    ))
    def testGroupConfig(self, data):

        save_value, expect_value = data

        LOAD = ("select g from ExperimenterGroup g "
                "left outer join fetch g.config where g.id = :id")

        queryService = self.root.sf.getQueryService()
        updateService = self.root.sf.getUpdateService()
        params = omero.sys.ParametersI()

        def load_group(id):
            params.addId(id)
            return queryService.findByQuery(LOAD, params)

        group = self.new_group()
        gid = group.id.val
        group.config = save_value

        updateService.saveObject(group)
        group = load_group(gid)
        config = unwrap(group.config)

        assert expect_value == config

        print queryService.projection(
            """
            select m from ExperimenterGroup g
            left outer join g.config m where index(m) = 'a'
            """, None)
