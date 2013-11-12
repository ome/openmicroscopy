#!/usr/bin/env python
# -*- coding: utf-8 -*-

"""
   Integration test focused on the registering of official
   scripts in the ScriptRepo. (see ticket:#2073)

   Copyright 2010-2013 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

import test.integration.library as lib
import pytest
import omero
import omero.all
from omero_model_ScriptJobI import ScriptJobI
from omero.rtypes import *

class TestScriptRepo(lib.ITest):

    def testScriptRepo(self):
        sr = self.client.sf.sharedResources()
        repo = sr.getScriptRepository()
        assert repo

    def scriptPrx(self):
        return self.client.sf.getScriptService()

    def testGetOfficialScripts(self):
        prx = self.scriptPrx()
        officialScripts = prx.getScripts()
        count = len(officialScripts)
        assert count > 0

    def testGetUserScripts(self):
        prx = self.scriptPrx()
        myUserScripts = prx.getUserScripts([])
        sid = prx.uploadScript("/test/foo.py", """if True:
        import omero, omero.scripts as OS
        OS.client("name")
        """)
        myUserScripts = prx.getUserScripts([])
        assert sid in [x.id.val for x in myUserScripts]

        admin = self.client.sf.getAdminService()
        oid = admin.getEventContext().userId
        myUserScripts = prx.getUserScripts([omero.model.ExperimenterI(oid, False)])
        assert sid in [x.id.val for x in myUserScripts]

    @pytest.mark.xfail(reason="ticket 11494")
    def testGetGroupScripts(self):
        prx = self.scriptPrx()
        admin = self.client.sf.getAdminService()
        gid = admin.getEventContext().groupId
        gname = admin.getEventContext().groupName
        grp = omero.model.ExperimenterGroupI(gid, False)
        client = self.new_client(gname)

        sid = client.sf.getScriptService().uploadScript("/test/otheruser.py", """if True:
        import omero, omero.scripts as OS
        OS.client("testGetGroupScripts")""")

        myGroupScripts = prx.getUserScripts([grp])
        assert sid in [x.id.val for x in myGroupScripts]

    def testCantUndulyLoadScriptRepoFromUuid(self):
        pass

    def testMultipleScriptPathsNotSupported(self):
        pass

    def testUploadingViaOfficialScriptShowsUpInRepo(self):
        pass

    def testUploadingViaNonOfficialScriptDoesntShowUpInRepo(self):
        pass
