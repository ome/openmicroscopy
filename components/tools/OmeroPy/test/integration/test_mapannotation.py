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
from omero.rtypes import rstring

class TestMapAnnotation(lib.ITest):

    def testMapStringField(self):
        uuid = self.uuid()
        queryService = self.root.getSession().getQueryService()
        updateService = self.root.getSession().getUpdateService()
        group = ExperimenterGroupI()
        group.setName(rstring(uuid));
        group.setConfig(dict())
        group.getConfig()["language"] = "python"
        group = updateService.saveAndReturnObject(group);
        group = queryService.findByQuery(
                ("select g from ExperimenterGroup g join fetch g.config "
                "where g.id = %s" % group.getId().getValue()), None);
        assert "python" == group.getConfig().get("language")
