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

    def _onearg(self, cmd):
        self._someargs(cmd,[])

    def _someargs(self, args):
        args = Arguments(args)
        first, other = args.firstOther()
        try:
            node = self._node()
            sync = False
            acts = []

            if cmd == "sync":
                # No master specified
                sync = True
                node = self._node()
                acts.extend(other)
            elif cmd == "start" or cmd == "stop" or cmd =="stop" or cmd == "kill" or cmd == "restart":
                # Neither master nor sync specified. Defaults in effect
                acts.append(cmd)
                acts.extend(other)
            else:
                # Otherwise, command is name of master
                node = cmd
                # Check for sync
                if len(other) > 0 and other[0] == "sync":
                    sync = True
                    other.pop(0)
                acts.extend(other)

            self._node(node)
            if len(acts) == 0:
                self.help()
            else:
                for act in acts:
                    c = getattr(self, act)
                    c(node, sync)

        except Exc, ex:
            self.ctx.dbg(str(ex))
            self.ctx.die(100, "Bad argument: "+ cmd + ", " + ", ".join(other))

    ##############################################
    #
    # Commands
    #

    def start(self, node = None, sync = False):
        if node == None:
            node = self._node()

        props = self._properties()
        nodedata = path(props["IceGrid.Node.Data"])
        logdata = path(props["Ice.StdOut"]).dirname()
        if not nodedata.exists():
            self.ctx.out("Initializing %s" % nodedata)
            nodedata.makedirs()
        if not logdata.exists():
            self.ctx.out("Initializing %s" % logdata)
            logdata.makedirs()
        props = self._properties()
        command = ["icegridnode", self._icecfg()]
        command = command + ["--daemon", "--pidfile", self._pid(),"--nochdir"]
        self.ctx.popen(command)

    def stop(self, node = None, sync = False):
        if node == None:
            node = self._node()
        pid = open(self._pid(),"r").readline()
        os.kill(int(pid), signal.SIGQUIT)

    def kill(self, node = None, sync = False):
        if node == None:
            node = self._node()
        pid = open(self._pid(),"r").readline()
        os.kill(int(pid), signal.SIGKILL)

try:
    register("node", NodeControl)
except NameError:
    NodeControl()._main()
