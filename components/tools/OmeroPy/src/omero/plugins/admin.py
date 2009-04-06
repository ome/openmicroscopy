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
import time
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

           start [filename] [targets]        : Start icegridnode daemon and waits for required components
                                             : to come up, i.e. status == 0
                                             :
                                             : If the first argument can be found as a file, it will
                                             : be deployed as the application descriptor rather than
                                             : etc/grid/default.xml. All other arguments will be used
                                             : as targets to enable optional sections of the descriptor

           startasync [filename] [targets]   : The same as start but returns immediately.

           deploy [filename] [targets]       : Deploy the given deployment descriptor. See etc/grid/*.xml
                                             : If the first argument is not a file path, etc/grid/default.xml
                                             : will be deployed by default. Same functionality as start, but
                                             : requires that the node already be running. This may automatically
                                             : restart some server components.

           stop                              : Initiates node shutdown and waits for status to return a non-0
                                             : value

           stopasync                         : The same as stop but returns immediately.

           status                            : Status of server. Returns with 0 status if a node ping is successful
                                             : and if some SessionManager returns an OMERO-specific exception on
                                             : a bad login.

           ice [arg1 arg2 ...]               : Drop user into icegridadmin console or execute arguments

           waitup                            : Used by start after calling startasync
           waitdown                          : Used by stop after calling stopasync

        """)
        DISABLED = """ see: http://www.zeroc.com/forums/bug-reports/4237-sporadic-freeze-errors-concurrent-icegridnode-access.html
           restart [filename] [targets]      : Calls stop followed by start args
           restartasync [filename] [targets] : Calls stop followed by startasync args
        """


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
            __d__ = "default.xml"
            if self._isWindows():
                __d__ = "windefault.xml"
            descript = self.dir / "etc" / "grid" / __d__
            self.ctx.err("No descriptor given. Using %s" os.path.sep.join(["etc","grid",__d__]))
        return descript

    def startasync(self, args):
        """
        First checks for a valid installation, then checks the grid,
        then registers the action: "node HOST start"
        """
        ##if 0 != self.status(args):
        ##    self.ctx.err("Server already running")
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
                 command = [
                       "sc", "create", svc_name,
                       "binPath=","""icegridnode.exe "%s" --deploy "%s" --service %s""" % (self._icecfg(), descript, svc_name),
                       "DisplayName=", svc_name,
                       "start=","auto"]
                       #'obj="NT Authority\LocalService"',
                       #'password=""']
                 self.ctx.out(self.ctx.popen(command, stdout = True).communicate()[0])
            self.ctx.out(self.ctx.popen(["sc","start",svc_name], stdout = True).communicate()[0])
        else:
            command = ["icegridnode","--daemon","--pidfile",str(self._pid()),"--nochdir",self._icecfg(),"--deploy",str(descript)] + other
            self.ctx.popen(command)

    def start(self, args):
        self.startasync(args)
        self.waitup(args)

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
        self.ctx.rv = self.ctx.popen(command, strict = False, stdout = True).wait()

        if self.ctx.rv == 0:
            try:
                import omero, Ice, IceGrid, Glacier2
                ic = Ice.initialize([self._intcfg()])
                try:
                    iq = ic.stringToProxy("IceGrid/Query")
                    iq = IceGrid.QueryPrx.checkedCast(iq)
                    sm = iq.findAllObjectsByType("::Glacier2::SessionManager")[0]
                    sm = Glacier2.SessionManagerPrx.checkedCast(sm)
                    try:
                        sm.create("####### STATUS CHECK ########", None) # Not adding "omero.client.uuid"
                    except omero.WrappedCreateSessionException, wcse:
                        # Only the server will throw one of these
                        self.ctx.dbg("Server reachable")
                        self.ctx.rv = 0
                finally:
                    ic.destroy()
            except Exc, exc:
                self.ctx.rv = 1
                self.ctx.dbg("Server not reachable: "+str(exc))
        return self.ctx.rv

    def __DISABLED__restart(self, args):
        self.stop(args)
        self.start(args)

    def __DISABLED__restartasync(self, args):
        self.stop(args)
        self.startasync(args)

    def waitup(self, args):
        args = Arguments(args)
        self.ctx.out("Waiting on startup. Use CTRL-C to exit")
        count = 30
        while True:
            count = count - 1
            if count == 0:
                self.ctx.die(43, "Failed to startup after 5 minutes")
            elif 0 == self.status(args):
                break
            else:
                self.ctx.out(".", newline = False)
                time.sleep(10)

    def waitdown(self, args):
        args = Arguments(args)
        self.ctx.out("Waiting on shutdown. Use CTRL-C to exit")
        count = 30
        while True:
            count = count - 1
            if count == 0:
                self.ctx.die(44, "Failed to shutdown after 5 minutes")
            elif 0 != self.status(args):
                break
            else:
                self.ctx.out(".", newline = False)
                time.sleep(10)
	self.ctx.rv = 0

    def stopasync(self, args):
        ##if 0 == self.status(args):
        ##    self.ctx.err("Server not running")
        if self._isWindows():
            svc_name = "OMERO.%s" % self._node()
            self.ctx.out(self.ctx.popen(["sc","stop",svc_name], stdout = True).communicate()[0])
            self.ctx.out(self.ctx.popen(["sc","delete",svc_name], stdout = True).communicate()[0])
        else:
            command = self._cmd("-e","node shutdown master")
            try:
                self.ctx.popen(command)
            except NonZeroReturnCode, nzrc:
                self.ctx.rv = nzrc.rv
                self.ctx.out("Was the server already stopped?")

    def stop(self, args):
        self.stopasync(args)
        self.waitdown(args)

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
