#!/usr/bin/env python
# -*- coding: utf-8 -*-

"""
   Test of the scripts plugin

   Copyright 2010 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

import unittest, os, subprocess, StringIO
from path import path
from omero.cli import Context, BaseControl, CLI
from omero.plugins.script import ScriptControl
from omero.plugins.sessions import SessionsControl
from omero.plugins.upload import UploadControl
from omero.util.temp_files import create_path
from integration.library import ITest

omeroDir = path(os.getcwd()) / "build"


class TestScript(ITest):

    def cli(self):
        cli = CLI()
        cli.register("upload", UploadControl, "TEST")
        cli.register("sessions", SessionsControl, "TEST")
        cli.register("s", ScriptControl, "TEST")
        return cli

    def test1(self):
        cli = self.cli()
        cmd = self.login_args() + ["s", "list"]
        cli.invoke(cmd, strict=True) # Throws NonZeroReturnCode

    def testFullSession(self):
        cli = self.cli()
        p = create_path(suffix=".py")
        p.write_text("""
import omero, omero.scripts as s
from omero.rtypes import *

client = s.client("testFullSession", "simple ping script", s.Long("a").inout(), s.String("b").inout())
client.setOutput("a", rlong(0))
client.setOutput("b", rstring("c"))
client.closeSession()
""")
        args = self.login_args() + ["s"]
        cli.invoke(args + ["upload", str(p)], strict=True) # Sets current script
        cli.invoke(args + ["list", "user"], strict=True)
        #cli.invoke(args + ["serve", "user", "requests=1", "timeout=1", "background=true"], strict=True)
        #cli.invoke(args + ["launch"], strict=True) # Uses current script
        
    
    def testReplace(self):
        cli = self.cli()
        p = create_path(suffix=".py")
        p.write_text("""
import omero, omero.scripts as s
from omero.rtypes import *

client = s.client("testFullSession", "simple ping script", s.Long("a").inout(), s.String("b").inout())
client.setOutput("a", rlong(0))
client.setOutput("b", rstring("c"))
client.closeSession()
""")
        args = self.login_args() + ["s"]
        
        # test replace with user script (not official)
        cli.invoke(args + ["upload", str(p)], strict=True) # Sets current script
        newId = cli.get("script.file.id")
        cli.invoke(args + ["list", "user"], strict=True)
        replaceArgs = args + ["replace", str(newId), str(p)]
        print replaceArgs
        cli.invoke(replaceArgs, strict=True)
        
    
    def testReplaceOfficial(self):
        cli = self.cli()
        p = create_path(suffix=".py")
        p.write_text("""
import omero, omero.scripts as s
from omero.rtypes import *

client = s.client("testFullSession", "simple ping script", s.Long("a").inout(), s.String("b").inout())
client.setOutput("a", rlong(0))
client.setOutput("b", rstring("c"))
client.closeSession()
""")
        args = self.root_login_args() + ["s"]
        
        # test replace with official script 
        uploadArgs = args + ["upload", str(p), "--official"]
        # print uploadArgs
        cli.invoke(uploadArgs, strict=True) # Sets current script
        newId = cli.get("script.file.id")
        cli.invoke(args + ["list"], strict=True)
        replaceArgs = args + ["replace", str(newId), str(p)]
        # print replaceArgs
        cli.invoke(replaceArgs, strict=True)
        
if __name__ == '__main__':
    unittest.main()
