#!/usr/bin/env python
"""
   prefs plugin

   Plugin read by omero.cli.Cli during initialization. The method(s)
   defined here will be added to the Cli class for later use.

   The pref plugin makes use of prefs.class from the common component.

   Copyright 2007 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

from omero.cli import BaseControl
from omero_ext.strings import shlex
import omero.java

class PrefsControl(BaseControl):

    def _name(self): return "prefs"

    def help(self):
        self.ctx.out( """
Syntax: %(program_name)s prefs
       Access to java properties
       """ )

    def __call__(self, args):
        self.ctx.out(omero.java.run(["prefs"]+shlex(args)))

c = PrefsControl()
try:
    register(c)
except NameError:
    c._main()
