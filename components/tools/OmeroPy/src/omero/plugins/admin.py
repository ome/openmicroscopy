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

from omero.cli import Arguments
from omero.cli import BaseControl
from omero_ext import pysys

class AdminControl(BaseControl):

    def help(self, args = None):
        self.ctx.out( """
Syntax: %(program_name)s admin  [ check | adduser | start | stop | status ]
                       --    No argument opens a command shell
           adduser     --
           check       --
           deploy      --    filename [ target1 [target2 [..] ] ]
           start       --
           stop        --
           status      --
        """)

    def _complete(self, text, line, begidx, endidx):
        s = " deploy "
        l = len(s)
        i = line.find(s)
        if i >= 0:
            f = line[i+l:]
            p = path(f)
            if p.exists() and p.isdir():
                if not f.endswith(os.sep):
                    return [p.basename()+os.sep]
                return [ str(i)[len(f):] for i in p.listdir() ]
            else:
                results = [ str(i.basename()) for i in self.dir.glob(f+"*")  ]
                if len(results) == 1:
                    maybe_dir = path(results[0])
                    if maybe_dir.exists() and maybe_dir.isdir():
                        return [ results[0] + os.sep ]
                return results
        else:
            return BaseControl._complete(self, text, line, begidx, endidx)

    def _node(self, omero_node = None):
        """ Overrides the regular node() logic to return the value of OMERO_MASTER or "master" """
        if omero_node != None:
            os.environ["OMERO_MASTER"] = omero_node

        if os.environ.has_key("OMERO_MASTER"):
            return os.environ["OMERO_MASTER"]
        else:
            return "master"

    ##############################################
    #
    # Commands
    #

    def start(self, args):
        """
        First checks for a valid installation, then checks the grid,
        then registers the action: "node HOST start"
        """
        props = self._properties()
        regdata = path(props["IceGrid.Registry.Data"])
        if not regdata.exists():
            self.ctx.out("""
  Warning:
  IceGrid.Registry.Data directory not present (%s).
  You need to run "admin deploy" after this command
  or no servers will be started.

  This warning will not be shown again.
            """ % regdata)
            regdata.makedirs()

        self.check([])
        self.ctx.pub(["node", self._node(), "start"])

    def deploy(self, args):
        args = Arguments(args)
        command = ["icegridadmin", self._icecfg()]
        first,other = args.firstOther()

        if first == None or len(first) == 0:
            self.ctx.err("No file given")
        else:
            descrpt = path(first)
            targets = " ".join(other)

            if not descrpt.exists():
                self.ctx.err("%s does not exist" % path)
            else:
                command = command + ["-e","application add %s %s" % (descrpt, targets) ]
                self.ctx.popen(command)

    def stop(self, args):
        command = ["icegridadmin", self._icecfg()]
        command = command + ["-e","node shutdown master"]
        self.ctx.popen(command)
        # Was:
        # self.ctx.pub(["node", self._node(), "stop"])

    def check(self, args):
        # print "Check db. Have a way to load the db control"
        pass

    def grid(self, args):
        args = Arguments(args)
        command = ["icegridadmin","--Ice.Config=etc/internal.cfg" ]
        if len(args) > 0:
            command.extend(["-e",args.join(" ")])
            return self.ctx.popen(command)
        else:
            rv = self.ctx.popen(command, False)

try:
    register("admin", AdminControl)
except NameError:
    AdminControl()._main()
