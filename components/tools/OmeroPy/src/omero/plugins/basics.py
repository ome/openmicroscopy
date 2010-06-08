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

from omero.cli import BaseControl
from omero.cli import CLI
from omero.cli import VERSION

readline_status = "enabled"
try:
    import readline
except:
    readline_status = "disabled"


class DebugControl(BaseControl):

    def __call__(self, args):
        self.ctx.setdebug()
        self.ctx.pub(args)


class TraceControl(BaseControl):

    def __call__(self, args):
        import trace
        tracer = trace.Trace()
        tracer.runfunc(self.ctx.pub, args)


class ProfileControl(BaseControl):

    def __call__(self, args):
        import hotshot
        from hotshot import stats
        prof = hotshot.Profile("hotshot_edi_stats")
        rv = prof.runcall( lambda: self.ctx.pub(args) )
        prof.close()
        s = stats.load("hotshot_edi_stats")
        s.sort_stats("time").print_stats()


class QuitControl(BaseControl):

    def __call__(self, args):
        self.ctx.exit("")


class VersionControl(BaseControl):

    def __call__(self, args):
        self.ctx.out(VERSION)


class LoadControl(BaseControl):

    def __call__(self, args):
        for arg in args:
            file = open(arg,'r')
            self.ctx.dbg("Loading file %s" % arg)
            for line in file:
                self.pub.ctx(line)


class ShellControl(BaseControl):

    def __call__(self, args):
        """
        Copied from IPython embed-short example
        """
        from IPython.Shell import IPShellEmbed
        ipshell = IPShellEmbed(args)
        ipshell()

controls = {
    "load": (LoadControl, "Load file as if it were sent on standard in. File tab-completion %s" % readline_status),
    "quit": (QuitControl, "Quit application"),
    "shell": (ShellControl, "Starts an IPython interpreter session"),
    "version": (VersionControl, "Version number"),
    "debug": (DebugControl, "Run command with debug"),
    "profile": (ProfileControl, "Run command with profiling"),
    "trace": (TraceControl, "Run command with tracing turned on")
}

try:
    for k, v in controls.items():
        register(k, v[0], v[1])
except NameError:
    if __name__ == "__main__":
        cli = CLI()
        for k, v in controls.items():
            cli.register(k, v[0], v[1])
        cli.invoke(sys.argv[1:])
