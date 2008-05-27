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

RE=re.compile("^\s*(\S*)\s*(start|stop|restart|status)\s*(\S*)\s*$")

class NodeControl(BaseControl):

    def _name(self): return "node"

    def help(self):
        self.ctx.out( """
Syntax: %(program_name)s node  [node-name ] [ start | stop | status | restart ] [--wait]
           start       -- Start the node via icegridnode. With --wait doesn't return until reachable.
           stop        -- Stop the node via icegridadmin. With --wait doesn't return until stopped.
           status      -- Prints a status message. Return code is non-zero if there is a problem.
           restart     -- Calls "start --wait" then "stop" ("stop --wait" if --wait is specified"

        node-name cannot be "start", "stop", "restart", "status", or "--wait".
        """ )

    def _likes(self, args):
        return RE.match(" ".join(args)) and True or False

    def _noargs(self):
        self.help()

    def _onearg(self, cmd):
        self._someargs(cmd,[])

    def _someargs(self, cmd, args):
        try:
            omero_node, command, wait = RE.match(string).groups()
            if omero_node != None:
                os.environ["OMERO_NODE"] = omero_node
            cmd = getattr(self, command)
            cmd()
        except Exc, ex:
            self.ctx.dbg(str(ex))
            self.ctx.die(100, "Bad argument string:"+string)

    ##############################################
    #
    # Commands
    #

    def start(self):
        props = self._properties()
        command = ["icegridnode", self._icecfg()]
        command = command + ["--daemon", "--pidfile", self._pid(),"--nochdir"]
        self.ctx.popen(command)

    def stop(self):
        pid = open(self._pid(),"r").readline()
        os.kill(int(pid), signal.SIGQUIT)

    def kill(self):
        pid = open(self._pid(),"r").readline()
        os.kill(int(pid), signal.SIGKILL)

c = NodeControl()
try:
    register(c)
except NameError:
    c._main()
