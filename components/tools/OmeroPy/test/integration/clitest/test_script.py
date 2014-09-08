#!/usr/bin/env python
# -*- coding: utf-8 -*-

"""
   Test of the scripts plugin

   Copyright 2010-2013 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

from test.integration.clitest.cli import CLITest
from omero.plugins.script import ScriptControl
from omero.util.temp_files import create_path

scriptText = """
import omero, omero.scripts as s
from omero.rtypes import *

client = s.client("testFullSession", "simple ping script", \
s.Long("a").inout(), s.String("b").inout())
client.setOutput("a", rlong(0))
client.setOutput("b", rstring("c"))
client.closeSession()
"""


class TestScript(CLITest):

    def setup_method(self, method):
        super(TestScript, self).setup_method(method)
        self.cli.register("script", ScriptControl, "TEST")
        self.args += ["script"]

    def testList(self):
        self.args += ["list"]
        self.cli.invoke(self.args, strict=True)  # Throws NonZeroReturnCode

    def testDemo(self):
        self.args += ["demo"]
        self.cli.invoke(self.args, strict=True)

    def testFullSession(self):
        p = create_path(suffix=".py")
        p.write_text(scriptText)
        # Sets current script
        self.cli.invoke(self.args + ["upload", str(p)], strict=True)
        self.cli.invoke(self.args + ["list", "user"], strict=True)

    # Replace subcommand
    # ========================================================================
    def testReplace(self):
        p = create_path(suffix=".py")
        p.write_text(scriptText)

        # test replace with user script (not official)
        # Sets current script
        self.cli.invoke(self.args + ["upload", str(p)], strict=True)
        newId = self.cli.get("script.file.id")
        self.cli.invoke(self.args + ["list", "user"], strict=True)
        replaceArgs = self.args + ["replace", str(newId), str(p)]
        print replaceArgs
        self.cli.invoke(replaceArgs, strict=True)

    def testReplaceOfficial(self):
        p = create_path(suffix=".py")
        p.write_text(scriptText)

        # test replace with official script
        self.args = self.root_login_args() + ["script"]
        uploadArgs = self.args + ["upload", str(p), "--official"]
        self.cli.invoke(uploadArgs, strict=True)  # Sets current script
        newId = self.cli.get("script.file.id")
        self.cli.invoke(self.args + ["list"], strict=True)
        replaceArgs = self.args + ["replace", str(newId), str(p)]
        self.cli.invoke(replaceArgs, strict=True)
