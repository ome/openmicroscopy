#!/usr/bin/env python
# -*- coding: utf-8 -*-

"""
   Integration test demonstrating various script creation methods

   Copyright 2010 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

import test.integration.library as lib
import pytest
import os, sys

import omero


class TestCoverage(lib.ITest):

    def setup_method(self, method):
        """
        getScripts returns official scripts, several of which are shipped with OMERO.
        """
        lib.ITest.setup_method(self, method)
        self.rs = self.root.sf.getScriptService()
        self.us = self.client.sf.getScriptService()
        assert len(self.rs.getScripts()) > 0
        assert len(self.us.getScripts()) > 0
        assert len(self.us.getUserScripts([])) == 0 # New user. No scripts

    def testGetScriptWithDetails(self):
        scriptList = self.us.getScripts()
        script = scriptList[0]
        scriptMap = self.us.getScriptWithDetails(script.id.val)

        assert len(scriptMap) == 1
        scriptText = scriptMap.keys()[0]
        scriptObj = scriptMap.values()[0]

    def testUploadAndScript(self):
        scriptID = self.us.uploadScript("/OME/Foo.py", """if True:
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
            self.us.uploadOfficialScript( "/%s/fails.py" % self.uuid(),\
            """if True:
            import omero
            """)
