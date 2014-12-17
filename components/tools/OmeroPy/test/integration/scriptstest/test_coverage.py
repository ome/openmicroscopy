#!/usr/bin/env python
# -*- coding: utf-8 -*-

#
# Copyright (C) 2010-2014 Glencoe Software, Inc. All Rights Reserved.
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
   Integration test demonstrating various script creation methods

"""

import library as lib
import pytest

import omero


class TestCoverage(lib.ITest):

    def setup_method(self, method):
        """
        getScripts returns official scripts,
        several of which are shipped with OMERO.
        """
        lib.ITest.setup_method(self, method)
        self.rs = self.root.sf.getScriptService()
        self.us = self.client.sf.getScriptService()
        assert len(self.rs.getScripts()) > 0
        assert len(self.us.getScripts()) > 0
        assert len(self.us.getUserScripts([])) == 0  # New user. No scripts

    def testGetScriptWithDetails(self):
        scriptList = self.us.getScripts()
        script = scriptList[0]
        scriptMap = self.us.getScriptWithDetails(script.id.val)

        assert len(scriptMap) == 1

    def testUploadAndScript(self):
        scriptID = self.us.uploadScript(
            "/OME/Foo.py",
            """if True:
            import omero
            import omero.grid as OG
            import omero.rtypes as OR
            import omero.scripts as OS
            client = OS.client("testUploadScript")
            print "done"
            """)
        return scriptID

    def testUserCantUploadOfficalScript(self):
        with pytest.raises(omero.SecurityViolation):
            self.us.uploadOfficialScript(
                "/%s/fails.py" % self.uuid(),
                """if True:
                import omero
                """)
