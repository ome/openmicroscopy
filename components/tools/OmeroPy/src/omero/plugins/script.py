#!/usr/bin/env python
"""
   script plugin

   Plugin read by omero.cli.Cli during initialization. The method(s)
   defined here will be added to the Cli class for later use.

   The script plugin is used to run arbitrary blitz scripts which
   take as their sole input Ice configuration arguments, including
   --Ice.Config=file1,file2.

   The first parameter, the script itself, should be natively executable
   on a given platform. I.e. invokable by subprocess.call([file,...])

   Copyright 2008 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

import subprocess, os, sys
from omero.cli import BaseControl

class ScriptControl(BaseControl):

    def _name(self):
        return "script"

    def do_script(self, arg):
        """
        syntax: script file [configuration parameters]
        """
        if hasattr(self, "secure"):
            self.throw("Secure cli cannot execture python scripts")
        args = self.shlex(arg)
        if len(args) < 1:
            self.throw("No file given")
        env = os.environ
        env["PYTHONPATH"] = self.pythonpath()
        p = subprocess.Popen(args,env=os.environ)
        p.wait()
        if p.poll() != 0:
            self.throw("Execution failed.")

c = ScriptControl(None, None)
try:
    register(c)
except NameError:
    c._main()
