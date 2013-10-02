#!/usr/bin/env python
# -*- coding: utf-8 -*-
"""
 :author: Josh Moore, josh at glencoesoftware.com

 OMERO Grid node controller

 This is a python wrapper around icegridnode.

 Copyright 2008 Glencoe Software, Inc.  All Rights Reserved.
 Use is subject to license terms supplied in LICENSE.txt

"""

from omero.cli import BaseControl, CLI, NonZeroReturnCode
from omero.util import tail_lines
import os
import sys
import signal
from path import path

HELP = """Control icegridnode.

           start       -- Start the node via icegridnode. With sync doesn't \
return until reachable.
           stop        -- Stop the node via icegridadmin. With sync doesn't \
return until stopped.
           status      -- Prints a status message. Return code is non-zero if\
 there is a problem.
           restart     -- Calls "sync start" then "stop" ("sync stop" if sync\
 is specified)

        node-name cannot be "start", "stop", "restart", "status", or "sync".
"""


class NodeControl(BaseControl):

    def _configure(self, parser):
        parser.add_argument(
            "name", nargs="?",
            help="Optional name of this node.", default=self._node())
        parser.add_argument(
            "sync", nargs="?", choices=("sync",),
            help="Whether or not to call wait on results")
        parser.add_argument(
            "command", nargs="+",
            choices=("start", "stop", "status", "restart"))
        parser.set_defaults(func=self.__call__)

    def __call__(self, args):
        self._node(args.name)  # Set environment value
        for act in args.command:
            c = getattr(self, act)
            c(args)

    def _handleNZRC(self, nzrc):
        """
        Set the return value from nzrc on the context, and print
        out the last two lines of any error messages if present.
        """
        props = self._properties()
        self.ctx.rv = nzrc.rv
        myoutput = self.dir / path(props["Ice.StdErr"])
        if not myoutput.exists():
            pass
        else:
            print "from %s:" % str(myoutput)
            print tail_lines(str(myoutput), 2)

    def start(self, args):

        self._initDir()

        try:
            command = ["icegridnode", self._icecfg()]
            if self._isWindows():
                command = command + ["--install", "OMERO."+args.node]
                self.ctx.call(command)
                self.ctx.call(["icegridnode", "--start", "OMERO."+args.node])
            else:
                command = command + ["--daemon", "--pidfile",
                                     str(self._pid()), "--nochdir"]
                self.ctx.call(command)
        except OSError, o:
                msg = """%s\nPossibly an error finding "icegridnode". Try \
"icegridnode -h" from the command line.""" % o
                raise Exception(msg)
        except NonZeroReturnCode, nzrc:
                self._handleNZRC(nzrc)

    def status(self, args):
        self.ctx.invoke(["admin", "status", args.name])

    def stop(self, args):
        if self._isWindows():
            try:
                command = ["icegridnode", "--stop", "OMERO."+args.name]
                self.ctx.call(command)
                command = ["icegridnode", "--uninstall", "OMERO."+args.name]
                self.ctx.call(command)
            except NonZeroReturnCode, nzrc:
                self._handleNZRC(nzrc)
        else:
                pid = open(self._pid(), "r").readline()
                os.kill(int(pid), signal.SIGTERM)
                # command = ["icegridadmin"] + [self._intcfg()] + ["-c", "node
                # shutdown %s" % args.name]
                # self.ctx.call(command)

    def kill(self, args):
        pid = open(self._pid(), "r").readline()
        os.kill(int(pid), signal.SIGKILL)

try:
    register("node", NodeControl, HELP)
except NameError:
    if __name__ == "__main__":
        cli = CLI()
        cli.loadplugins()
        cli.invoke(sys.argv[1:])
