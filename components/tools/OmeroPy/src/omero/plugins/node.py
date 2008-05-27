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

    def _name(self):
        return "node"

    def help(self):
        return\
        """
        Syntax: node [start|status|stop|kill] [nodename]
        Node name defaults to "default" configuration if not defined.
        Configurations are defined in the etc/ directory of the install.
        """

    def _likes(self, args):
        return RE.match(" ".join(args)) and True or False

    def __call__(self, *args):
        if len(args) == 0 or len(args[0]) == 0:
            return self(["help"])

        string = " ".join(args[0])
        self.ctx.dbg(string)

        try:
            omero_node, command, wait = RE.match(string).groups()
            if omero_node != None:
                os.environ["OMERO_NODE"] = omero_node
            cmd = getattr(self, command)
            cmd()
        except Exc, ex:
            self.ctx.dbg(str(ex))
            self.ctx.die("Bad argument string:"+string)


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
