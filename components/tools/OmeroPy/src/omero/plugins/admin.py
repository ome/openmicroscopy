#!/usr/bin/env python
"""
 :author: Josh Moore, josh at glencoesoftware.com

 OMERO Grid admin controller

 This is a python wrapper around icegridregistry/icegridnode for master
 and various other tools needed for administration.

 Copyright 2008 Glencoe Software, Inc.  All Rights Reserved.
 Use is subject to license terms supplied in LICENSE.txt

"""

import os
from path import path
from omero.cli import BaseControl
from omero_ext import pysys

class Control(BaseControl):

    def _name(self):
        return "admin"

    def help(self):
        self.event.out( """
Syntax: %(program_name)s admin  [ check | adduser | start | stop | status ]
                       --    No argument opens a command shell
           check       --
           adduser     --
           start       --
           stop        --
           status      --
        """)

    def _node(self):
        """ Overrides the regular node() logic to return the value of OMERO_MASTER or "master" """
        if os.environ.has_key("OMERO_MASTER"):
            return os.environ["OMERO_MASTER"]
        else:
            return "master"

    def start(self):
        """ 
        First checks for a valid installation, then checks the grid,
        then registers the action: "node HOST start"
        """
        props = self._properties()
        nodedata = path(props["IceGrid.Node.Data"])
        regdata = path(props["IceGrid.Registry.Data"])
        logdata = path(props["Ice.StdOut"]).dirname()
        if not nodedata.exists() or not regdata.exists() or not logdata.exists():
            if not nodedata.exists(): self.event.err("Missing %s" % nodedata)
            if not regdata.exists(): self.event.err("Missing %s" % regdata)
            if not logdata.exists(): self.event.err("Missing %s" % logdata)
            self.event.out("""Master directories not all present. You may need to run "admin deploy" """)
        self.check()
        self.event.pub(["node", self._node(), "start"])

    def stop(self):
        self.event.pub(["node", self._node(), "stop"])

    def check(self):
        print "Check db. Have a way to load the db control"

c = Control(path(os.getcwd()), None)
try:
    register(c)
except NameError:
    c._main()
