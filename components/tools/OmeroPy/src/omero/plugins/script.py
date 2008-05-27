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
from omero_ext.strings import shlex

class ScriptControl(BaseControl):

    def _name(self): return "script"

    def help(self):
        self.ctx.out("""
Syntax: %(program_name)s script file [configuration parameters]
        Executes a file as a script. Can be used to test scripts
        for later deployment on the grid.
        """)

    def __call__(self, arg):
        if hasattr(self, "secure"):
            self.ctx.err("Secure cli cannot execture python scripts")
        args = shlex(arg)
        if len(args) < 1:
            self.ctx.err("No file given")
        env = os.environ
        env["PYTHONPATH"] = self.ctx.pythonpath()
        p = subprocess.Popen(args,env=os.environ)
        p.wait()
        if p.poll() != 0:
            self.ctx.die(p.poll(), "Execution failed.")

c = ScriptControl()
try:
    register(c)
except NameError:
    c._main()
