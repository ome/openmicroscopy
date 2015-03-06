#!/usr/bin/env python
# -*- coding: utf-8 -*-

#
# Copyright (C) 2015 Glencoe Software, Inc. All Rights Reserved.
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
   Integration test focused on the omero.api.IConfig interface.

"""

import omero
import pytest
import library as lib


class TestConfig(lib.ITest):

    EXPECTS = (
        (".*", ("omero.router.insecure",)),
        ("^omero.*", ("omero.router.insecure",)),
        (".*router.*", ("omero.router.insecure",)),
        ("omero.db", ("omero.db.authority",)),
    )

    @pytest.mark.parametrize("data", EXPECTS)
    def testValuesRegex(self, data):
        regex, contains = data
        cfg = self.sf.getConfigService()
        rv = cfg.getConfigValues(regex)
        for c in contains:
            assert c in rv

    def testDefaults(self):
        cfg = self.sf.getConfigService()
        with pytest.raises(omero.SecurityViolation):
            defs = cfg.getConfigDefaults()

    def testRootDefaults(self):
        cfg = self.root.sf.getConfigService()
        defs = cfg.getConfigDefaults()
        for x in (
            "omero.version",
            "omero.db.name",
        ):
            assert x in defs

    def testClientDefaults(self):
        cfg = self.sf.getConfigService()
        defs = cfg.getClientConfigDefaults()
        assert "omero.client.ui.menu.dropdown.colleagues" in defs

    def testClientValues(self):
        # Not sure what's in this so just calling
        cfg = self.sf.getConfigService()
        defs = cfg.getClientConfigValues()
        assert defs is not None
