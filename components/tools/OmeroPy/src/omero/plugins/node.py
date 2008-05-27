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
import os, sys, subprocess, signal
from exceptions import Exception as Exc

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

    def start(self):
        props = self._properties()
        command = ["icegridnode", self._icecfg()]
        command = command + ["--daemon", "--pidfile", self._pid(),"--nochdir"]
        self.event.popen(command)

    def stop(self):
        pid = open(self._pid(),"r").readline()
        os.kill(int(pid), signal.SIGQUIT)

    def kill(self):
        pid = open(self._pid(),"r").readline()
        os.kill(int(pid), signal.SIGKILL)

c = NodeControl(None, None)
try:
    register(c)
except NameError:
    c._main()
