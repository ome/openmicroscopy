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
from omero.cli import NonZeroReturnCode
from omero_ext import pysys

class AdminControl(BaseControl):

    def help(self, args = None):
        self.ctx.out( """
Syntax: %(program_name)s admin  [ start | update | stop | status ]

                                          : No argument opens a command shell

           start [hostname]               : Start server daemon and return immediately.

           update filename [ targets ]    : Deploy the given deployment descriptor. See etc/grid/*.xml
                                          : If the first argument is not a file path, etc/grid/default.xml
                                          : will be deployed by default.

           stop                           : Send server stop command and return immediately.

           status                         : Status of server

           ice [arg1 arg2 ...]            : Drop user into icegridadmin console or execute arguments

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
                    # Relative to cwd
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

    def _cmd(self, *args):
        command = ["icegridadmin", self._intcfg() ]
        command.extend(args)
        return command
    ##############################################
    #
    # Commands
    #

    def _descript(self, first, other):
        if first != None and len(first) > 0:
            # Relative to cwd
            descript = path(first).abspath()
            if not descript.exists():
                self.ctx.dbg("No such file: %s -- Using as target" % descript)
                other.insert(0, first)
                descript = None
        else:
            descript = None

        if descript == None:
            descript = self.dir / "etc" / "grid" / "default.xml"
            self.ctx.err("No descriptor given. Using etc/grid/default.xml")
        return descript

    def start(self, args):
        """
        First checks for a valid installation, then checks the grid,
        then registers the action: "node HOST start"
        """
        self._initDir()
        props = self._properties()
        # Do a check to see if we've started before.
        self._regdata()
        self.check([])

        args = Arguments(args)
        first, other = args.firstOther()
        descript = self._descript(first, other)

        if self._isWindows():
            svc_name = "OMERO.%s" % self._node()
            command = ["sc", "query", svc_name]
            # Required to check the stdout since
            # rcode is not non-0
            popen = self.ctx.popen(command, stdout = True)
            output = popen.communicate()[0]
            if -1 < output.find("does not exist"):
                 print "%s service not found" % svc_name
                 command = [
                       "sc", "create", svc_name,
                       "binPath=","""C:\\Ice-3.3.0\\bin\\icegridnode.exe "%s" --deploy "%s" --service %s""" % (self._icecfg(), descript, svc_name),
                       "DisplayName=", svc_name,
                       "start=","auto"]
                       #'obj="NT Authority\LocalService"',
                       #'password=""']
                 print self.ctx.popen(command)
            else:
                print "NYI: just starting service"
        else:
            # TODO : This won't work for Windows. Must refactor.
            command = ["icegridnode","--daemon","--pidfile",str(self._pid()),"--nochdir",self._icecfg(),"--deploy",str(descript)] + other
            self.ctx.popen(command)

    def startandwait(self, args):
        self.start(args)
        self.ctx.out("Waiting on servers to start (Ctrl-C to cancel)")
        self.ctx.rv = 1
        while self.ctx.rv != 0:
            try:
                self.status([])
            except KeyboardInterrupt, ki:
                self.ctx.out("Cancelled")
                break

    def deploy(self, args):
        args = Arguments(args)
        first, other = args.firstOther()
        descript = self._descript(first, other)

        # TODO : Doesn't properly handle whitespace
        command = ["icegridadmin",self._intcfg(),"-e"," ".join(["application","update", str(descript)] + other)]
        self.ctx.popen(command)

    def status(self, args):
        args = Arguments(args)
        first,other = args.firstOther()
        if first == None:
            first = "master"

        command = self._cmd("-e","node ping %s" % first)
        self.ctx.rv = self.ctx.popen(command, False)

        if self.ctx.rv == 0 and first == "master":
            # If we're checking master, then we also need to be sure that it's all the way up
            self.ctx.rv = 1
            try:
                client = self.ctx.conn()
                self.ctx.rv = 0
            except Exc, exc:
                self.ctx.dbg(str(exc))
                self.ctx.err("Server not reachable")
        return self.ctx.rv

    def stop(self, args):
        command = self._cmd("-e","node shutdown master")
        try:
            self.ctx.popen(command)
        except NonZeroReturnCode, nzrc:
            self.ctx.rv = nzrc.rv
            self.ctx.out("Was the server already stopped?")

    def check(self, args):
        # print "Check db. Have a way to load the db control"
        pass

    def ice(self, args):
        args = Arguments(args)
        command = self._cmd()
        if len(args) > 0:
            command.extend(["-e",args.join(" ")])
            return self.ctx.popen(command)
        else:
            rv = self.ctx.popen(command, False)

try:
    register("admin", AdminControl)
except NameError:
    AdminControl()._main()
