#!/usr/bin/env python
"""
 :author: Josh Moore, josh at glencoesoftware.com

 OMERO Grid admin controller

 This is a python wrapper around icegridregistry/icegridnode for master
 and various other tools needed for administration.

 Copyright 2008 Glencoe Software, Inc.  All Rights Reserved.
 Use is subject to license terms supplied in LICENSE.txt

"""

import re
import os
import time
import exceptions

from path import path
from which import whichall

import omero
import omero_ServerErrors_ice
from omero.cli import Arguments
from omero.cli import BaseControl
from omero.cli import NonZeroReturnCode
from omero.cli import VERSION

try:
    import win32service
    import win32evtlogutil
    has_win32 = True
except ImportError:
    has_win32 = False


class AdminControl(BaseControl):

    def help(self, args = None):
        self.ctx.out( """
Syntax: %(program_name)s admin  [ start | update | stop | status ]

                                             : No argument opens a command shell

           Main Commands
           -------------

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
                                             : a bad login. This can be used in shell scripts, e.g.:
                                             :
                                             :     omero admin status && echo "server started"
                                             :

           ice [arg1 arg2 ...]               : Drop user into icegridadmin console or execute arguments


           Other Commands
           --------------

           diagnostics                       : Run a set of checks on the current, preferably active server

           waitup                            : Used by start after calling startasync to wait on status==0

           waitdown                          : Used by stop after calling stopasync to wait on status!=0

           events                            : Print event log (Windows-only)

        """)
        DISABLED = """ see: http://www.zeroc.com/forums/bug-reports/4237-sporadic-freeze-errors-concurrent-icegridnode-access.html
           restart [filename] [targets]      : Calls stop followed by start args
           restartasync [filename] [targets] : Calls stop followed by startasync args
        """

    #
    # Windows utility methods
    #
    if has_win32:
        def _query_service(unused, svc_name):
            hscm = win32service.OpenSCManager(None, None, win32service.SC_MANAGER_ALL_ACCESS)
            try:
                try:
                    hs = win32service.OpenService(hscm, svc_name, win32service.SERVICE_ALL_ACCESS)
                except:
                    return "DOESNOTEXIST"
                try:
                    q = win32service.QueryServiceStatus(hs)
                    type, state, ctrl, err, svcerr, svccp, svcwh = q
                    if state == win32service.SERVICE_STOPPED:
                        return "STOPPED"
                    else:
                        return "unknown"
                finally:
                    win32service.CloseServiceHandle(hs)
            finally:
                win32service.CloseServiceHandle(hscm)

        def events(self, svc_name):
            def DumpRecord(record):
                if str(record.SourceName) == svc_name:
                    self.ctx.out("Time: %s" % record.TimeWritten)
                    self.ctx.out("Rec:  %s" % record.RecordNumber)
                    for si in record.StringInserts:
                        self.ctx.out(si)
                    self.ctx.out("="*20)
            win32evtlogutil.FeedEventLogRecords(DumpRecord)

    else:

        def events(self, svc_name):
            self.ctx.die(666, "Could not import win32service and/or win32evtlogutil")

        def _query_service(self, svc_name):
            """
            Query the service
            Required to check the stdout since
            rcode is not non-0
            """
            command = ["sc", "query", svc_name]
            popen = self.ctx.popen(command) # popen
            output = popen.communicate()[0]
            if 0 <= output.find("1060"):
                return "DOESNOTEXIST"
            else:
                return output

    #
    # End Windows Methods
    #

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
            self.ctx.err("No descriptor given. Using %s" % os.path.sep.join(["etc","grid",__d__]))
        return descript

    def checkwindows(self, *args):
        """
        Checks that the templates file as defined in etc\Windows.cfg
        can be found.
        """

        if not self._isWindows():
            self.ctx.die(123, "Not Windows")

        args = Arguments(*args)
        import Ice
        key = "IceGrid.Node.Data"
        properties = Ice.createProperties([self._icecfg()])
        nodedata = properties.getProperty(key)
        if not nodedata:
            self.ctx.die(300, "Bad configuration: No IceGrid.Node.Data property")
        nodepath = path(nodedata)
        pp = nodepath.parpath(self.ctx.dir)
        if pp:
            return
        if nodepath == r"c:\omero_dist\var\master":
            self.ctx.out("Found default value: %s" % nodepath)
            self.ctx.out("Attempting to correct...")
            from omero.install.win_set_path import win_set_path
            count = win_set_path()
            if count:
                return
        self.ctx.die(400, """

            %s is not in this directory. Aborting...

            Please see the installation instructions on modifying
            the files for your installation (%s)
            with bin\winconfig.bat

            """ % (nodedata, self.ctx.dir))

    ##############################################
    #
    # Commands
    #

    def startasync(self, args):
        """
        First checks for a valid installation, then checks the grid,
        then registers the action: "node HOST start"
        """

        if self._isWindows():
            self.checkwindows()

        ##if 0 != self.status(args):
        ##    self.ctx.err("Server already running")

        self._initDir()
        props = self._properties()
        # Do a check to see if we've started before.
        self._regdata()
        self.check([])

        user = None
        args = Arguments(args)
        first, other = args.firstOther()
        if first == "-u":
            user = first
            args = Arguments(other)
            first, other = args.firstOther()
        descript = self._descript(first, other)

        if self._isWindows():
            svc_name = "OMERO.%s" % self._node()
            output = self._query_service(svc_name)

            # Now check if the server exists
            if 0 <= output.find("DOESNOTEXIST"):
                command = [
                   "sc", "create", svc_name,
                   "binPath=","""icegridnode.exe "%s" --deploy "%s" --service %s""" % (self._icecfg(), descript, svc_name),
                   "DisplayName=", svc_name,
                   "start=","auto"]

                # By default: "NT Authority\LocalService"
                if user:
                    user = self.ctx.input("User account:", False)
                if not user:
                    user = self.ctx.initData().properties.getProperty("omero.windows.user")
                if len(user) > 0:
                    command.append("obj=")
                    command.append(user)
                    self.ctx.out(self.ctx.popen(["ntrights","+r","SeServiceLogonRight","-u",user]).communicate()[0]) # popen
                    pasw = self.ctx.initData().properties.getProperty("omero.windows.pass")
                    pasw = self._ask_for_password(" for service user: %s" % user, pasw)
                    command.append("password=")
                    command.append(pasw)
                self.ctx.out(self.ctx.popen(command).communicate()[0]) # popen

            # Then check if the server is already running
            if 0 <= output.find("RUNNING"):
                 self.ctx.die(201, "%s is already running. Use stop first" % svc_name)

            # Finally start the service
            output = self.ctx.popen(["sc","start",svc_name]).communicate()[0] # popen
            self.ctx.out(output)
        else:
            command = ["icegridnode","--daemon","--pidfile",str(self._pid()),"--nochdir",self._icecfg(),"--deploy",str(descript)] + other
            self.ctx.call(command)

    def start(self, args):
        self.startasync(args)
        self.waitup(args)

    def deploy(self, args):
        args = Arguments(args)
        first, other = args.firstOther()
        descript = self._descript(first, other)

        # TODO : Doesn't properly handle whitespace
        command = ["icegridadmin",self._intcfg(),"-e"," ".join(["application","update", str(descript)] + other)]
        self.ctx.call(command)

    def status(self, args):
        args = Arguments(args)
        first,other = args.firstOther()
        if first == None:
            first = "master"

        command = self._cmd("-e","node ping %s" % first)
        self.ctx.rv = self.ctx.popen(command).wait() # popen

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
            output = self._query_service(svc_name)
            if 0 <= output.find("DOESNOTEXIST"):
                self.ctx.die(203, "%s does not exist. Use 'start' first." % svc_name)
            self.ctx.out(self.ctx.popen(["sc","stop",svc_name]).communicate()[0]) # popen
            self.ctx.out(self.ctx.popen(["sc","delete",svc_name]).communicate()[0]) # popen
        else:
            command = self._cmd("-e","node shutdown master")
            try:
                self.ctx.call(command)
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
            return self.ctx.call(command)
        else:
            rv = self.ctx.call(command)

    def diagnostics(self, args):
        args = Arguments(args)
        self.ctx.out("""
%s
OMERO Diagnostics %s
%s
        """ % ("="*80, VERSION, "="*80))

        def sz_str(sz):
            for x in ["KB", "MB", "GB"]:
                sz /= 1000
                if sz < 1000:
                    break
            sz = "%.1f %s" % (sz, x)
            return sz

        def item(cat, msg):
            cat = cat + ":"
            cat = "%-12s" % cat
            self.ctx.out(cat, False)
            msg = "%-30s " % msg
            self.ctx.out(msg, False)

        def exists(p):
            if p.isdir():
                if not p.exists():
                    self.ctx.out("doesn't exist")
                else:
                    self.ctx.out("exists")
            else:
                if not p.exists():
                    self.ctx.out("n/a")
                else:
                    warn = 0
                    err = 0
                    for l in p.lines():
                        if l.find("ERROR") >= 0:
                            err += 1
                        elif l.find("WARN") >= 0:
                            warn += 1
                    msg = ""
                    if warn or err:
                        msg = " errors=%-4s warnings=%-4s" % (err, warn)
                    self.ctx.out("%-12s %s" % (sz_str(p.size), msg))

        def version(cmd):
            item("Commands","%s" % " ".join(cmd))
            try:
                p = self.ctx.popen(cmd)
            except OSError:
                self.ctx.err("not found")
                return
            rv = p.wait()
            io = p.communicate()
            try:
                v = io[0].split()
                v.extend(io[1].split())
                v = "".join(v)
                m = re.match("^\D*(\d[.\d]+\d)\D?.*$", v)
                v = "%-10s" % m.group(1)
                self.ctx.out(v, False)
                try:
                    where = whichall(cmd[0])
                    sz = len(where)
                    if sz == 0:
                        where = unknown
                    else:
                        where = where[0]
                        if sz > 1:
                            where += " -- %s others" % sz

                except:
                    where = "unknown"
                self.ctx.out("(%s)" % where)
            except exceptions.Exception, e:
                self.ctx.err("error:%s" % e)

        version(["java",         "-version"])
        version(["python",       "-V"])
        version(["icegridnode",  "--version"])
        version(["icegridadmin", "--version"])
        version(["psql",         "--version"])


        self.ctx.out("")
        item("Server", "icegridnode")
        p = self.ctx.popen(self._cmd() + ["-e", "server list"]) # popen
        rv = p.wait()
        io = p.communicate()
        if rv != 0:
            self.ctx.out("not started")
            self.ctx.dbg("""
            Stdout:\n%s
            Stderr:\n%s
            """ % io)
        else:
            self.ctx.out("running")
            servers = io[0].split()
            servers.sort()
            for s in servers:
                item("Server", "%s" % s)
                p2 = self.ctx.popen(self._cmd() + ["-e", "server state %s" % s]) # popen
                rv2 = p2.wait()
                io2 = p2.communicate()
                if io2[1]:
                    self.ctx.err(io2[1].strip())
                elif io2[0]:
                    self.ctx.out(io2[0].strip())
                else:
                    self.ctx.err("UNKNOWN!")

        def log_dir(log, cat, cat2, knownfiles):
            self.ctx.out("")
            item(cat, "%s" % log.abspath())
            exists(log)
            self.ctx.out("")

            if log.exists():
                files = log.files()
                files = set([x.basename() for x in files])
                # Adding known names just in case
                for x in knownfiles:
                    files.add(x)
                files = list(files)
                files.sort()
                for x in files:
                    item(cat2, x)
                    exists(log / x)
                item(cat2, "Total size")
                sz = 0
                for x in log.walkfiles():
                    sz += x.size
                self.ctx.out("%-.2f MB" % (float(sz)/1000000.0))

        log_dir(self.ctx.dir / "var" / "log", "Log dir", "Log files",\
            ["Blitz-0.log", "Tables-0.log", "Processor-0.log", "Indexer-0.log", "FileServer.log", "MonitorServer.log", "DropBox.log", "TestDropBox.log", "OMEROweb.log"])

        self.ctx.out("")
        def env_val(val):
            item("Environment","%s=%s" % (val, os.environ.get(val, "(unset)")))
            self.ctx.out("")
        env_val("OMERO_HOME")
        env_val("OMERO_NODE")
        env_val("OMERO_MASTER")
        env_val("PATH")
        env_val("ICE_HOME")
        env_val("LD_LIBRARY_PATH")
        env_val("DYLD_LIBRARY_PATH")
try:
    register("admin", AdminControl)
except NameError:
    AdminControl()._main()
