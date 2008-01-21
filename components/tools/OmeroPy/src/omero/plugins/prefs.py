#!/usr/bin/env python
"""
   prefs plugin

   Plugin read by omero.cli.Cli during initialization. The method(s)
   defined here will be added to the Cli class for later use.

   The pref plugin makes use of prefs.class from 

   Copyright 2007 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""


def do_prefs(self, arg):
    import omero.java, shlex
    print omero.java.run(["prefs"]+shlex.split(arg))

CLI.do_prefs = do_prefs

def help_prefs(self):
    print "syntax: prefs",
    print "-- access to java properties"
