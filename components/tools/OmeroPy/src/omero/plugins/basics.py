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
                self.invoke.ctx(line)


class ShellControl(BaseControl):

    def __call__(self, args):
        """
        Copied from IPython embed-short example
        """
        from IPython.Shell import IPShellEmbed
        ipshell = IPShellEmbed(args)
        ipshell()


class HelpControl(BaseControl):
    """
    Defined here since the background loading might be too
    slow to have all help available
    """

    def _configure(self, parser):
        parser.set_defaults(func=self.__call__)
        parser.add_argument("topic", nargs="?", help="Topic for more information")

    def _complete(self, text, line, begidx, endidx):
        """
        This is something of a hack. This should either be a part
        of the context interface, or we should put it somewhere
        in a utility. FIXME.
        """
        return self.ctx.completenames(text, line, begidx, endidx)

    def __call__(self, args):

        self.ctx.waitForPlugins()
        controls = sorted(self.ctx.controls)

        if not args.topic:
            self.ctx.invoke("-h")
            print """
Usage: %(program_name)s <command> [options] args
See 'help <command>' or '<command> -h' for more information on syntax
Type 'quit' to exit

Available commands:
""" % {"program_name":sys.argv[0],"version":VERSION}

            for name in controls:
                print """ %s""" % name
            print """
For additional information, see http://trac.openmicroscopy.org.uk/omero/wiki/OmeroCli
Report bugs to <ome-users@openmicroscopy.org.uk>"""

        else:
            try:
                c = self.ctx.controls[args.topic]
                self.ctx.invoke("%s -h" % args.topic)
            except KeyError, ke:
                self.ctx.unknown_command(args.topic)

controls = {
    "help": (HelpControl, "Syntax help for all commands"),
    "load": (LoadControl, "Load file as if it were sent on standard in. File tab-completion %s" % readline_status),
    "quit": (QuitControl, "Quit application"),
    "shell": (ShellControl, "Starts an IPython interpreter session"),
    "version": (VersionControl, "Version number"),
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
