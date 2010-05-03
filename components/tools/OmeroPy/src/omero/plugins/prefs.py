#!/usr/bin/env python
"""
   prefs plugin

   Plugin read by omero.cli.Cli during initialization. The method(s)
   defined here will be added to the Cli class for later use.

   The pref plugin makes use of prefs.class from the common component.

   Copyright 2007 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

import sys, os
from exceptions import Exception
from omero.cli import BaseControl
from omero_ext.strings import shlex
from omero.util import edit_path
import omero.java

def getprefs(args, dir):
    if not isinstance(args,list):
        raise Exception("Not a list")
    cmd = ["prefs"]+list(args)
    return omero.java.run(cmd, chdir=dir)

class PrefsControl(BaseControl):

    def help(self, args = None):
        self.ctx.out( """
Syntax: %(program_name)s prefs
       Access to java properties
       """ )

    def __call__(self, *args):
        args = Arguments(args)
        first, other = args.firstOther()
        if first == 'edit':
            from omero.util.temp_files import create_path, remove_path
            start_text = "# Edit your preferences below. Comments are ignored\n"
            start_text += getprefs(["get"], str(self.ctx.dir / "lib"))
            temp_file = create_path()
            edit_path(temp_file, start_text)
            getprefs(["drop"], str(self.ctx.dir / "lib"))
            getprefs(["load_nowarn", "%s" % str(temp_file)], str(self.ctx.dir / "lib"))
            remove_path(temp_file)
        else:
            dir = self.ctx.dir / "lib"
            self.ctx.out(getprefs(args.args, str(dir)))

try:
    register("config", PrefsControl)
except NameError:
    PrefsControl()._main()
