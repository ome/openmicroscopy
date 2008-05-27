#!/usr/bin/env python
"""
 :author: Josh Moore, josh at glencoesoftware.com

 OMERO Grid node controller

 This is a python wrapper around icegridnode.

 Copyright 2008 Glencoe Software, Inc.  All Rights Reserved.
 Use is subject to license terms supplied in LICENSE.txt

"""

from omero.cli import BaseControl
from omero_ext.strings import shlex
import re, os, sys, subprocess, signal
from exceptions import Exception as Exc
from path import path

RE=re.compile("^\s*(\S*)\s*(start|stop|restart|status)\s*(\S*)\s*$")

class NodeControl(BaseControl):

    def help(self, args = None):
        self.ctx.out( """
Syntax: %(program_name)s node [node-name ] [sync] [ start | stop | status | restart ]
           start       -- Start the node via icegridnode. With sync doesn't return until reachable.
           stop        -- Stop the node via icegridadmin. With sync doesn't return until stopped.
           status      -- Prints a status message. Return code is non-zero if there is a problem.
           restart     -- Calls "sync start" then "stop" ("sync stop" if sync is specified)

        node-name cannot be "start", "stop", "restart", "status", or "sync".
        """ )

    def _likes(self, args):
        args = Arguments(args)
        first, other = args.firstOther()
        return hasattr(self,first) or RE.match(args.join(" ")) and True or False

    def _noargs(self):
        self.help()

    def __call__(self, *args):
        args = Arguments(*args)
        first, other = args.firstOther()
        try:
            name = self._node()
            sync = False
            acts = []

            if first == "sync":
                # No master specified
                sync = True
                name = self._node()
                acts.extend(other)
            elif first == "start" or first == "stop" or first =="stop" or first == "kill" or first == "restart":
                # Neither master nor sync specified. Defaults in effect
                acts.append(first)
                acts.extend(other)
            else:
                # Otherwise, command is name of master
                name = first
                # Check for sync
                if len(other) > 0 and other[0] == "sync":
                    sync = True
                    other.pop(0)
                acts.extend(other)

            self._node(name)
            if len(acts) == 0:
                self.help()
            else:
                for act in acts:
                    c = getattr(self, act)
                    c(name, sync)

        except Exc, ex:
            import traceback
            traceback.print_exc()
            self.ctx.dbg(str(ex))
            self.ctx.die(100, "Bad argument: "+ str(first) + ", " + ", ".join(other))

    ##############################################
    #
    # Commands : Since node plugin implements its own
    # __call__() method, the pattern for the following
    # commands is somewhat different.
    #

    def start(self, name = None, sync = False):

        if name == None:
            name = self._node()

        props = self._properties()
        nodedata = self._nodedata()
        logdata = path(props["Ice.StdOut"]).dirname()
        if not logdata.exists():
            self.ctx.out("Initializing %s" % logdata)
            logdata.makedirs()

        props = self._properties()
        command = ["icegridnode", self._icecfg()]
        command = command + ["--daemon", "--pidfile", str(self._pid()),"--nochdir"]
        self.ctx.popen(command)

    def status(self, name = None):

        if name == None:
            name = self._node()

        self.ctx.pub(["admin","status",name])

    def stop(self, name = None, sync = False):
        if name == None:
            name = self._node()
        pid = open(self._pid(),"r").readline()
        os.kill(int(pid), signal.SIGQUIT)

    def kill(self, name = None, sync = False):
        if name == None:
            name = self._node()
        pid = open(self._pid(),"r").readline()
        os.kill(int(pid), signal.SIGKILL)

try:
    register("node", NodeControl)
except NameError:
    NodeControl()._main()
