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
   gateway tests - argument errors in gateway methods

"""

from omero.gateway import _BlitzGateway
import pytest


class TestArgumentErrors(object):

    @classmethod
    @pytest.fixture(autouse=True)
    def setup_class(cls, tmpdir, monkeypatch):
        ice_config = tmpdir / "ice.config"
        ice_config.write("omero.host=localhost\nomero.port=4064")
        monkeypatch.setenv("ICE_CONFIG", ice_config)
        cls.g = _BlitzGateway()

    def test_graphspec_with_plus(self):
        """
        The graph_spec Name+Qualifier is no longer supported.
        """
        with pytest.raises(AttributeError):
            self.g.deleteObjects("Image+Only", ["1"])
        with pytest.raises(AttributeError):
            self.g.chgrpObjects("Image+Only", ["1"], 1L)

    @pytest.mark.parametrize("object_ids", ["1", [], None])
    def test_bad_object_ids(self, object_ids):
        """
        object_ids must be a non-zero length list
        """
        with pytest.raises(AttributeError):
            self.g.deleteObjects("Image", object_ids)
        with pytest.raises(AttributeError):
            self.g.chgrpObjects("Image", object_ids, 1L)
