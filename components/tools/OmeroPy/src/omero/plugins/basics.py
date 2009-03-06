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

class DebugControl(BaseControl):
    def help(self, args = None):
        self.ctx.out("Run command with debug")
    def __call__(self, *args):
        args = Arguments(*args)
        self.ctx.setdebug()
        self.ctx.pub(args)

class ProfileControl(BaseControl):
    def help(self, args = None):
        self.ctx.out("Run command with profiling")

    def __call__(self, *args):
        args = Arguments(*args)
        import hotshot
        from hotshot import stats
        prof = hotshot.Profile("hotshot_edi_stats")
        rv = prof.runcall( lambda: self.ctx.pub(args) )
        prof.close()
        s = stats.load("hotshot_edi_stats")
        s.sort_stats("time").print_stats()

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

class ShellControl(BaseControl):

    def help(self, args = None):
        self.ctx.out("Starts an IPython interpreter session")

    def __call__(self, *args):
        """
        Copied from IPython embed-short example
        """
        args = Arguments(*args)
        from IPython.Shell import IPShellEmbed
        ipshell = IPShellEmbed(args.args)
        ipshell()

try:
    register("load", LoadControl)
    register("quit", QuitControl)
    register("shell", ShellControl)
    register("version", VersionControl)
    register("debug", DebugControl)
    register("profile", ProfileControl)
except NameError:
    VersionControl()._main()
