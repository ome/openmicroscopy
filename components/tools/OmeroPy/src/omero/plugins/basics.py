#!/usr/bin/env python
"""
   load, quit, version, help plugins

   Plugin read by omero.cli.Cli during initialization. The method(s)
   defined here will be added to the Cli class for later use.

   The load plugin is used to read in files with omero cli commands
   (omitting the omero). For example,

   ./omero load some/file.osh

   The help, quit, and version plugins are self-explanatory.

   Copyright 2008 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

import subprocess, optparse, os, sys
from omero.cli import Arguments, BaseControl, VERSION

class HelpControl(BaseControl):

    def _complete(self, text, line, begidx, endidx):
        """
        This is something of a hack. This should either be a part
        of the context interface, or we should put it somewhere
        in a utility. FIXME.
        """
        return self.ctx.completenames(text, line, begidx, endidx)

    def help(self, args = None):
        self.out("Print help")

    def __call__(self, *args):

        args = Arguments(*args)
        first, other = args.firstOther()

        controls = self.ctx.controls.keys()
        controls.sort()

        if not first:
            print """OmeroCli client, version %(version)s

Usage: %(program_name)s <command> [options] args
See 'help <command>' for more information on syntax
Type 'quit' to exit

Available commands:
""" % {"program_name":pysys.argv[0],"version":VERSION}

            for name in controls:
                print """ %s""" % name
            print """
For additional information, see http://trac.openmicroscopy.org.uk/omero/wiki/OmeroCli"""

        else:
            try:
                # Throws ValueError if not present
                controls.index(first)

                event = [first, "help"]
                event.extend(other)
                self.ctx.pub(event)
            except ValueError, ve:
                self.ctx.err("Unknown command:" + first)

class QuitControl(BaseControl):

    def help(self, args = None):
        self.ctx.out("Quit application")

    def __call__(self, *args):
        self.ctx.exit("")

class VersionControl(BaseControl):

    def help(self, args = None):
        self.ctx.out("Version number")

    def __call__(self, *args):
        self.ctx.out(VERSION)

class LoadControl(BaseControl):

    def help(self = None):
        status = "enabled"
        try:
            import readline
        except:
            status = "disabled"

        self.ctx.out(
        """
Syntax: %%(program_name)s file1 file2 file3

        Load file as if it were sent on standard in. File tab-completion %s"
        """ % status )

    def __call__(self, *args):
        args = Arguments(*args)
        for arg in args:
            file = open(arg,'r')
            self.ctx.dbg("Loading file %s" % arg)
            for line in file:
                self.pub.ctx(line)


try:
    register("help", HelpControl)
    register("load", LoadControl)
    register("quit", QuitControl)
    register("version", VersionControl)
except NameError:
    VersionControl()._main()
