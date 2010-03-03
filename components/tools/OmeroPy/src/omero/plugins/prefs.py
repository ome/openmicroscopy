#!/usr/bin/env python
"""
   prefs plugin

   Plugin read by omero.cli.Cli during initialization. The method(s)
   defined here will be added to the Cli class for later use.

   The pref plugin makes use of prefs.class from the common component.

   Copyright 2007 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

import sys, os, tempfile
from exceptions import Exception
from omero.cli import BaseControl
from omero_ext.strings import shlex
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
            self.__edit()
        else:
            dir = self.ctx.dir / "lib"
            self.ctx.out(getprefs(args.args, str(dir)))

    def __edit(self):
        editor = os.getenv("VISUAL") or os.getenv("EDITOR")
        if not editor:
            if sys.platform == "windows":
                editor = "Notepad.exe"
            else:
                editor = "vi"
        temp_fd, temp_file = tempfile.mkstemp(text=True)
        os.write(temp_fd, getprefs(["config get"], str(self.ctx.dir / "lib")))
        os.close(temp_fd)
        pid = os.spawnlp(os.P_WAIT, editor, editor, temp_file)
        if pid:
            raise RuntimeError("Couldn't spawn editor: %s" % editor)
        new_text = open(temp_file).read()
        os.unlink(temp_file)
        print new_text
try:
    register("config", PrefsControl)
except NameError:
    PrefsControl()._main()
