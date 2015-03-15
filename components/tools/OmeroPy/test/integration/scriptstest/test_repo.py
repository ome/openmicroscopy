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
   Integration test focused on the registering of official
   scripts in the ScriptRepo. (see ticket:#2073)

"""

import library as lib
import pytest
import omero
import omero.all


class TestScriptRepo(lib.ITest):

    def testScriptRepo(self):
        sr = self.client.sf.sharedResources()
        repo = sr.getScriptRepository()
        assert repo

    def testGetOfficialScripts(self):
        scriptService = self.sf.getScriptService()
        officialScripts = scriptService.getScripts()
        count = len(officialScripts)
        assert count > 0

    def testGetUserScripts(self):
        scriptService = self.sf.getScriptService()
        myUserScripts = scriptService.getUserScripts([])
        sid = scriptService.uploadScript(
            "/test/foo.py",
            """if True:
            import omero, omero.scripts as OS
            OS.client("name")
            """)

        myUserScripts = scriptService.getUserScripts([])
        assert sid in [x.id.val for x in myUserScripts]

        myUserScripts = scriptService.getUserScripts(
            [omero.model.ExperimenterI(self.user.id.val, False)])
        assert sid in [x.id.val for x in myUserScripts]

    @pytest.mark.broken(ticket="11494")
    def testGetGroupScripts(self):
        scriptService = self.sf.getScriptService()
        client = self.new_client(self.group)

        sid = client.sf.getScriptService().uploadScript(
            "/test/otheruser.py",
            """if True:
            import omero, omero.scripts as OS
            OS.client("testGetGroupScripts")
            """)

        myGroupScripts = scriptService.getUserScripts(
            [omero.model.ExperimenterGroupI(self.group.id.val, False)])
        assert sid in [x.id.val for x in myGroupScripts]

    def testCantUndulyLoadScriptRepoFromUuid(self):
        pass

    def testMultipleScriptPathsNotSupported(self):
        pass

    def testUploadingViaOfficialScriptShowsUpInRepo(self):
        pass

    def testUploadingViaNonOfficialScriptDoesntShowUpInRepo(self):
        pass
