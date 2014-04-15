#!/usr/bin/env python
# -*- coding: utf-8 -*-
"""
 :author: Josh Moore, josh at glencoesoftware.com

 OMERO Grid admin controller

 This is a python wrapper around icegridregistry/icegridnode for master
 and various other tools needed for administration.

 Copyright 2008-14 Glencoe Software, Inc.  All Rights Reserved.
 Use is subject to license terms supplied in LICENSE.txt

"""

import re
import os
import sys
import stat
import platform
import portalocker

from path import path

import omero
import omero.config

from omero.cli import CLI
from omero.cli import BaseControl
from omero.cli import DirectoryType
from omero.cli import NonZeroReturnCode
from omero.cli import VERSION

from omero.plugins.prefs import with_config

from omero_ext.which import whichall
from omero_version import ice_compatibility

try:
    import pywintypes
    import win32service
    import win32evtlogutil
    import win32api
    import win32security
    has_win32 = True
except ImportError:
    has_win32 = False

DEFAULT_WAIT = 300

HELP = """Administrative tools including starting/stopping OMERO.

Environment variables:
 OMERO_MASTER
 OMERO_NODE

Configuration properties:
 omero.windows.user
 omero.windows.pass

""" + "\n" + "="*50 + "\n"


class AdminControl(BaseControl):

    def _complete(self, text, line, begidx, endidx):
        """
        Returns a file after "deploy", "start", or "startasync"
        and otherwise delegates to the BaseControl
        """
        for s in (" deploy ", " start ", " startasync "):
            l = len(s)
            i = line.find(s)
            if i >= 0:
                f = line[i+l:]
                return self._complete_file(f)
        return BaseControl._complete(self, text, line, begidx, endidx)

    def _configure(self, parser):
        sub = parser.sub()
        self.actions = {}

        class Action(object):
            def __init__(this, name, help, wait=False):
                this.parser = sub.add_parser(name, help=help,
                                             description=help)
                this.parser.set_defaults(func=getattr(self, name))
                self.actions[name] = this.parser
                if wait:
                    this.parser.add_argument(
                        "--wait", type=float, default=DEFAULT_WAIT,
                        help="Seconds to wait for operation")

        Action(
            "start",
            """Start icegridnode daemon and waits for required components to \
come up, i.e. status == 0

If the first argument can be found as a file, it will be deployed as the
application descriptor rather than etc/grid/default.xml. All other arguments
will be used as targets to enable optional sections of the descriptor""",
            wait=True)

        Action("startasync", "The same as start but returns immediately",)

        Action("restart", "stop && start", wait=True)

        Action(
            "restartasync", """The same as restart but returns as soon as \
starting has begun.""",
            wait=True)

        Action("status", """Status of server

Returns with 0 status if a node ping is successful and if some SessionManager
returns an OMERO-specific exception on a bad login. This can be used in shell
scripts, e.g.:

    $ omero admin status && echo "server started"
            """)

        Action(
            "stop",
            """Initiates node shutdown and waits for status to return a \
non-0 value""",
            wait=True)

        Action("stopasync", "The same as stop but returns immediately")

        Action(
            "deploy",
            """Deploy the given deployment descriptor. See etc/grid/*.xml

If the first argument is not a file path, etc/grid/default.xml will be
deployed by default. Same functionality as start, but requires that the node
already be running. This may automatically restart some server components.""")

        Action(
            "ice", "Drop user into icegridadmin console or execute arguments")

        fixpyramids = Action(
            "fixpyramids", "Remove empty pyramid pixels files").parser
        # See cleanse options below

        Action(
            "diagnostics",
            "Run a set of checks on the current, preferably active server")

        Action(
            "waitup",
            "Used by start after calling startasync to wait on status==0",
            wait=True)

        Action(
            "waitdown",
            "Used by stop after calling stopasync to wait on status!=0",
            wait=True)

        reindex = Action(
            "reindex",
            """Re-index the Lucene index

Command-line tool for re-index the database. This command must be run on the
machine where /OMERO/FullText is located.

Examples:
  bin/omero admin reindex --full                                             \
          # All objects
  bin/omero admin reindex --class ome.model.core.Image                       \
          # Only images
  JAVA_OPTS="-Dlogback.configurationFile=stderr.xml" \
  bin/omero admin reindex --full\
  # Passing arguments to Java


LIMITATION: omero.db.pass values do not currently get passed to the Java
            process. You will need to all passwordless login to PostgreSQL. In
            fact, only the following properties are passed:

            omero.data.dir
            omero.search.*
            omero.db.* (excluding pass)

""").parser
        reindex.add_argument(
            "--jdwp", help="Activate remote debugging")
        group = reindex.add_mutually_exclusive_group()
        group.add_argument(
            "--full", action="store_true",
            help="Reindexes all non-excluded tables sequentially")
        group.add_argument(
            "--events", action="store_true",
            help="Reindexes all non-excluded event logs chronologically")
        group.add_argument(
            "--class", nargs="+",
            help="Reindexes the given classes sequentially")

        ports = Action(
            "ports",
            """Allows modifying the ports from a standard OMERO install

To have two OMERO's running on the same machine, several ports must be
modified from their default values.
Internally, this command uses the omero.install.change_ports module.
Changing  the ports on a running server is usually not what you want and
will be prevented. Use --skipcheck to change the ports anyway.

Examples:

    %(prog)s --prefix=1                             # sets ports to: 14061, \
14063, 14064
    %(prog)s --prefix=1 --revert                    # sets ports back to: \
4061, 4063, 4064
    %(prog)s --registry=4444 --tcp=5555 --ssl=6666  # sets ports to: 4444 \
5555  6666

""").parser
        ports.add_argument(
            "--prefix",
            help="Adds a prefix to each port ON TOP OF any other settings")
        ports.add_argument(
            "--registry", default="4061",
            help="Registry port. (default: %(default)s)")
        ports.add_argument(
            "--tcp", default="4063",
            help="The tcp port to be used by Glacier2 (default: %(default)s)")
        ports.add_argument(
            "--ssl", default="4064",
            help="The ssl port to be used by Glacier2 (default: %(default)s)")
        ports.add_argument(
            "--revert", action="store_true",
            help="Used to rollback from the given settings to the defaults")
        ports.add_argument(
            "--skipcheck", action="store_true",
            help="Skips the check if the server is already running")

        sessionlist = Action(
            "sessionlist", "List currently running sessions").parser
        sessionlist.add_login_arguments()

        cleanse = Action("cleanse", """Remove binary data files from OMERO

Deleting an object from OMERO currently does not remove the binary data. Use
this command either manually or in a cron job periodically to remove Pixels
and other data.

This is done by checking that for all the files in the given directory, a
matching entry exists on the server. THE /OMERO DIRECTORY MUST MATCH THE
DATABASE YOU ARE RUNNING AGAINST.

This command must be run on the machine where, for example, /OMERO/ is
located.

Examples:
  bin/omero admin cleanse --dry-run /OMERO      # Lists files that will be \
deleted
  bin/omero admin cleanse /OMERO                # Actually delete them.
  bin/omero admin cleanse /volumes/data/OMERO   # Delete from a standard \
location.

""").parser

        for x in (cleanse, fixpyramids):
            x.add_argument(
                "--dry-run", action="store_true",
                help="Print out which files would be deleted")
            x.add_argument(
                "data_dir", type=DirectoryType(),
                help="omero.data.dir directory value (e.g. /OMERO")
            x.add_login_arguments()

        Action("checkwindows", "Run simple check of the local installation "
               "(Windows-only)")
        Action("checkice", "Run simple check of the Ice installation")

        Action("events", "Print event log (Windows-only)")

        self.actions["ice"].add_argument(
            "argument", nargs="*",
            help="""Arguments joined together to make an Ice command. If not \
present, the user will enter a console""")

        self.actions["status"].add_argument(
            "node", nargs="?", default="master")
        self.actions["status"].add_argument(
            "--nodeonly", action="store_true",
            help="If set, then only tests if the icegridnode is running")

        for name in ("start", "startasync"):
            self.actions[name].add_argument(
                "-u", "--user",
                help="Service Log On As user name. If none given, the value"
                " of omero.windows.user will be used. (Windows-only)")
            self.actions[name].add_argument(
                "-w", "--password",
                help="Service Log On As user password. If none given, the"
                " value of omero.windows.pass will be used. (Windows-only)")

        for k in ("start", "startasync", "deploy", "restart", "restartasync"):
            self.actions[k].add_argument(
                "file", nargs="?",
                help="Application descriptor. If not provided, a default"
                " will be used")
            self.actions[k].add_argument(
                "targets", nargs="*",
                help="Targets within the application descriptor which "
                " should  be activated. Common values are: \"debug\", "
                "\"trace\" ")

        # DISABLED = """ see: http://www.zeroc.com/forums/bug-reports/\
        # 4237-sporadic-freeze-errors-concurrent-icegridnode-access.html
        #   restart [filename] [targets]      : Calls stop followed by start \
        #   args
        #   restartasync [filename] [targets] : Calls stop followed by \
        #   startasync args
        # """

    #
    # Windows utility methods
    #
    if has_win32:
        def _query_service(unused, svc_name):
            hscm = win32service.OpenSCManager(
                None, None, win32service.SC_MANAGER_ALL_ACCESS)
            try:
                try:
                    hs = win32service.OpenService(
                        hscm, svc_name, win32service.SERVICE_ALL_ACCESS)
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
            self.ctx.die(
                666, "Could not import win32service and/or win32evtlogutil")

        def _query_service(self, svc_name):
            self.ctx.die(
                666, "Could not import win32service and/or win32evtlogutil")

    #
    # End Windows Methods
    #

    def _node(self, omero_node=None):
        """
        Overrides the regular node() logic to return the value of
        OMERO_MASTER or "master"
        """
        if omero_node is not None:
            os.environ["OMERO_MASTER"] = omero_node

        if "OMERO_MASTER" in os.environ:
            return os.environ["OMERO_MASTER"]
        else:
            return "master"

    def _cmd(self, *command_arguments):
        """
        Used to generate an icegridadmin command line argument list
        """
        command = ["icegridadmin", self._intcfg()]
        command.extend(command_arguments)
        return command

    def _descript(self, args):
        if args.file is not None:
            # Relative to cwd
            descript = path(args.file).abspath()
            if not descript.exists():
                self.ctx.dbg("No such file: %s -- Using as target" % descript)
                args.targets.insert(0, args.file)
                descript = None
        else:
            descript = None

        if descript is None:
            __d__ = "default.xml"
            if self._isWindows():
                __d__ = "windefault.xml"
            descript = self.ctx.dir / "etc" / "grid" / __d__
            self.ctx.err("No descriptor given. Using %s"
                         % os.path.sep.join(["etc", "grid", __d__]))
        return descript

    def checkwindows(self, args):
        """
        Checks that the templates file as defined in etc\Windows.cfg
        can be found.
        """
        self.check_access(os.R_OK)
        if not self._isWindows():
            self.ctx.die(123, "Not Windows")

        import Ice
        key = "IceGrid.Node.Data"
        properties = Ice.createProperties([self._icecfg()])
        nodedata = properties.getProperty(key)
        if not nodedata:
            self.ctx.die(300,
                         "Bad configuration: No IceGrid.Node.Data property")
        nodepath = path(nodedata)
        pp = nodepath.parpath(self.ctx.dir)
        if pp:
            return
        if nodepath == r"c:\omero_dist\var\master":
            self.ctx.out("Found default value: %s" % nodepath)
            self.ctx.out("Attempting to correct...")
            from omero.install.win_set_path import win_set_path
            count = win_set_path(dir=self.ctx.dir)
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

    @with_config
    def startasync(self, args, config):
        """
        First checks for a valid installation, then checks the grid,
        then registers the action: "node HOST start"
        """

        self.check_access(config=config)
        self.checkice()
        self.check_node(args)
        if self._isWindows():
            self.checkwindows(args)

        if 0 == self.status(args, node_only=True):
            self.ctx.die(876, "Server already running")

        self._initDir()
        # Do a check to see if we've started before.
        self._regdata()
        self.check([])

        user = args.user
        pasw = args.password
        descript = self._descript(args)

        if self._isWindows():
            svc_name = "OMERO.%s" % args.node
            output = self._query_service(svc_name)

            # Now check if the server exists
            if 0 <= output.find("DOESNOTEXIST"):
                binpath = """icegridnode.exe "%s" --deploy "%s" --service\
                %s""" % (self._icecfg(), descript, svc_name)

                # By default: "NT Authority\Local System"
                if not user:
                    try:
                        user = config.as_map()["omero.windows.user"]
                    except KeyError:
                        user = None
                if user is not None and len(user) > 0:
                    if "\\" not in user:
                        computername = win32api.GetComputerName()
                        user = "\\".join([computername, user])
                    try:
                        # See #9967, code based on http://mail.python.org/\
                        # pipermail/python-win32/2010-October/010791.html
                        self.ctx.out("Granting SeServiceLogonRight to service"
                                     " user \"%s\"" % user)
                        policy_handle = win32security.LsaOpenPolicy(
                            None, win32security.POLICY_ALL_ACCESS)
                        sid_obj, domain, tmp = \
                            win32security.LookupAccountName(None, user)
                        win32security.LsaAddAccountRights(
                            policy_handle, sid_obj, ('SeServiceLogonRight',))
                        win32security.LsaClose(policy_handle)
                    except pywintypes.error, details:
                        self.ctx.die(200, "Error during service user set up:"
                                     " (%s) %s" % (details[0], details[2]))
                    if not pasw:
                        try:
                            pasw = config.as_map()["omero.windows.pass"]
                        except KeyError:
                            pasw = self._ask_for_password(
                                " for service user \"%s\"" % user)
                else:
                    pasw = None

                hscm = win32service.OpenSCManager(
                    None, None, win32service.SC_MANAGER_ALL_ACCESS)
                try:
                    self.ctx.out("Installing %s Windows service." % svc_name)
                    hs = win32service.CreateService(
                        hscm, svc_name, svc_name,
                        win32service.SERVICE_ALL_ACCESS,
                        win32service.SERVICE_WIN32_OWN_PROCESS,
                        win32service.SERVICE_AUTO_START,
                        win32service.SERVICE_ERROR_NORMAL, binpath, None, 0,
                        None, user, pasw)
                    self.ctx.out("Successfully installed %s Windows service."
                                 % svc_name)
                    win32service.CloseServiceHandle(hs)
                finally:
                    win32service.CloseServiceHandle(hscm)

            # Then check if the server is already running
            if 0 <= output.find("RUNNING"):
                self.ctx.die(201, "%s is already running. Use stop first"
                             % svc_name)

            # Finally, try to start the service - delete if startup fails
            hscm = win32service.OpenSCManager(
                None, None, win32service.SC_MANAGER_ALL_ACCESS)
            try:
                try:
                    hs = win32service.OpenService(
                        hscm, svc_name, win32service.SC_MANAGER_ALL_ACCESS)
                    win32service.StartService(hs, None)
                    self.ctx.out("Starting %s Windows service." % svc_name)
                except pywintypes.error, details:
                    self.ctx.out("%s service startup failed: (%s) %s"
                                 % (svc_name, details[0], details[2]))
                    win32service.DeleteService(hs)
                    self.ctx.die(202, "%s service deleted." % svc_name)
            finally:
                win32service.CloseServiceHandle(hs)
                win32service.CloseServiceHandle(hscm)

        else:
            command = [
                "icegridnode", "--daemon", "--pidfile", str(self._pid()),
                "--nochdir", self._icecfg(), "--deploy", str(descript)
                ] + args.targets
            self.ctx.rv = self.ctx.call(command)

    @with_config
    def start(self, args, config):
        self.startasync(args, config)
        try:
            self.waitup(args)
        except NonZeroReturnCode, nzrc:
            # stop() may itself throw,
            # if it does not, then we rethrow
            # the original
            self.ctx.err('Calling "stop" on remaining components')
            self.stop(args, config)
            raise nzrc

    @with_config
    def deploy(self, args, config):
        self.check_access()
        self.checkice()
        descript = self._descript(args)

        # TODO : Doesn't properly handle whitespace
        # Though users can workaround with something like:
        # bin/omero admin deploy etc/grid/a\\\\ b.xml
        command = ["icegridadmin", self._intcfg(), "-e",
                   " ".join(["application", "update", str(descript)] +
                            args.targets)]
        self.ctx.call(command)

    def status(self, args, node_only=False):
        self.check_node(args)
        command = self._cmd("-e", "node ping %s" % self._node())
        self.ctx.rv = self.ctx.popen(command).wait()  # popen

        # node_only implies that "up" need not check for all
        # of blitz to be accessible but just that if the node
        # is running.
        if not node_only:
            node_only = getattr(args, "nodeonly", False)

        if self.ctx.rv == 0 and not node_only:
            try:
                import Ice
                ic = Ice.initialize([self._intcfg()])
                try:
                    sm = self.session_manager(ic)
                    try:
                        sm.create("####### STATUS CHECK ########", None)
                        # Not adding "omero.client.uuid"
                    except omero.WrappedCreateSessionException:
                        # Only the server will throw one of these
                        self.ctx.dbg("Server reachable")
                        self.ctx.rv = 0
                finally:
                    ic.destroy()
            except Exception, exc:
                self.ctx.rv = 1
                self.ctx.dbg("Server not reachable: "+str(exc))

        return self.ctx.rv

    def wait_for_icedb(self, args, config):
        """
        Since the stop and start are separately protected by
        the lock on config.xml, we need to wait for some time
        to hopefully let the icegridnode process release the
        file locks.
        """
        self.ctx.sleep(1)  # put in sleep to try to prevent "db locked" (#7325)

    @with_config
    def restart(self, args, config):
        if not self.stop(args, config):
            self.ctx.die(54, "Failed to shutdown")
        self.wait_for_icedb(args, config)
        self.start(args, config)

    @with_config
    def restartasync(self, args, config):
        if not self.stop(args, config):
            self.ctx.die(54, "Failed to shutdown")
        self.wait_for_icedb(args, config)
        self.startasync(args, config)

    def waitup(self, args):
        """
        Loops 30 times with 10 second pauses waiting for status()
        to return 0. If it does not, then ctx.die() is called.
        """
        self.check_access(os.R_OK)
        self.ctx.out("Waiting on startup. Use CTRL-C to exit")
        count, loop_secs, time_msg = self.loops_and_wait(args)
        while True:
            count = count - 1
            if count == 0:
                self.ctx.die(43, "\nFailed to startup some components after"
                             " %s" % time_msg)
            elif 0 == self.status(args, node_only=False):
                break
            else:
                self.ctx.out(".", newline=False)
                self.ctx.sleep(loop_secs)

    def waitdown(self, args):
        """
        Returns true if the server went down
        """
        self.check_access(os.R_OK)
        self.ctx.out("Waiting on shutdown. Use CTRL-C to exit")
        count, loop_secs, time_msg = self.loops_and_wait(args)
        while True:
            count = count - 1
            if count == 0:
                self.ctx.die(44, "\nFailed to shutdown some components after"
                             " %s" % time_msg)
                return False
            elif 0 != self.status(args, node_only=True):
                break
            else:
                self.ctx.out(".", newline=False)
                self.ctx.sleep(loop_secs)
        self.ctx.rv = 0
        return True

    def loops_and_wait(self, args):
        """
        If present, get the wait time from the args argument
        and calculate the number of loops and the wait time
        needed. If not present in args, use a default value.
        """

        if not hasattr(args, "wait"):
            # This might happen if a new command starts using
            # waitup/waitdown without setting wait=True for
            # Action()
            args.wait = DEFAULT_WAIT

        total_secs = args.wait
        loop_secs = total_secs / 30.0
        return 30, loop_secs, "%s seconds" % total_secs

    @with_config
    def stopasync(self, args, config):
        """
        Returns true if the server was already stopped
        """
        self.check_node(args)
        if 0 != self.status(args, node_only=True):
            self.ctx.err("Server not running")
            return True
        elif self._isWindows():
            svc_name = "OMERO.%s" % args.node
            output = self._query_service(svc_name)
            if 0 <= output.find("DOESNOTEXIST"):
                self.ctx.die(203, "%s does not exist. Use 'start' first."
                             % svc_name)
            hscm = win32service.OpenSCManager(
                None, None, win32service.SC_MANAGER_ALL_ACCESS)
            try:
                hs = win32service.OpenService(
                    hscm, svc_name, win32service.SC_MANAGER_ALL_ACCESS)
                win32service.ControlService(
                    hs, win32service.SERVICE_CONTROL_STOP)
                win32service.DeleteService(hs)
                self.ctx.out("%s service deleted." % svc_name)
            finally:
                win32service.CloseServiceHandle(hs)
                win32service.CloseServiceHandle(hscm)
        else:
            command = self._cmd("-e", "node shutdown %s" % self._node())
            try:
                self.ctx.call(command)
            except NonZeroReturnCode, nzrc:
                self.ctx.rv = nzrc.rv
                self.ctx.out("Was the server already stopped?")

    @with_config
    def stop(self, args, config):
        if not self.stopasync(args, config):
            return self.waitdown(args)
        return True

    def check(self, args):
        # print "Check db. Have a way to load the db control"
        pass

    def ice(self, args):
        self.check_access()
        command = self._cmd()
        if len(args.argument) > 0:
            command.extend(["-e", " ".join(args.argument)])
            return self.ctx.call(command)
        else:
            self.ctx.call(command)

    @with_config
    def fixpyramids(self, args, config):
        self.check_access()
        from omero.util.cleanse import fixpyramids
        client = self.ctx.conn(args)
        client.getSessionId()
        fixpyramids(data_dir=args.data_dir, dry_run=args.dry_run,
                    query_service=client.sf.getQueryService(),
                    config_service=client.sf.getConfigService())

    @with_config
    def diagnostics(self, args, config):
        self.check_access()
        config = config.as_map()
        omero_data_dir = '/OMERO'
        try:
            omero_data_dir = config['omero.data.dir']
        except KeyError:
            pass

        from omero.util.temp_files import gettempdir
        # gettempdir returns ~/omero/tmp/omero_%NAME/%PROCESS
        # To find something more generally useful for calculating
        # size, we go up two directories
        omero_temp_dir = gettempdir()
        omero_temp_dir = os.path.abspath(
            os.path.join(omero_temp_dir, os.path.pardir, os.path.pardir))

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
                        # ensure errors/warnings search is case-insensitive
                        lcl = l.lower()
                        found_err = lcl.find("error") >= 0
                        found_warn = lcl.find("warn") >= 0

                        if found_err:
                            err += 1
                        elif found_warn:
                            warn += 1
                    msg = ""
                    if warn or err:
                        msg = " errors=%-4s warnings=%-4s" % (err, warn)
                    self.ctx.out("%-12s %s" % (sz_str(p.size), msg))

        def version(cmd):
            """
            Returns a true response only
            if a valid version was found.
            """
            item("Commands", "%s" % " ".join(cmd))
            try:
                p = self.ctx.popen(cmd)
            except OSError:
                self.ctx.err("not found")
                return False

            p.wait()
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
                        where = "unknown"
                    else:
                        where = where[0]
                        if sz > 1:
                            where += " -- %s others" % sz

                except:
                    where = "unknown"
                self.ctx.out("(%s)" % where)
                return True
            except Exception, e:
                self.ctx.err("error:%s" % e)
                return False

        import logging
        logging.basicConfig()
        from omero.util.upgrade_check import UpgradeCheck
        check = UpgradeCheck("diagnostics")
        check.run()
        if check.isUpgradeNeeded():
            self.ctx.out("")

        version(["java",         "-version"])
        version(["python",       "-V"])
        version(["icegridnode",  "--version"])
        iga = version(["icegridadmin", "--version"])
        version(["psql",         "--version"])

        def get_ports(input):
            router_lines = [line for line in input.split("\n")
                            if line.find("ROUTER") >= 0]

            ssl_port = None
            tcp_port = None
            for line in router_lines:
                if not ssl_port and line.find("ROUTERPORT") >= 0:
                    m = re.match(".*?(\d+).*?$", line)
                    if m:
                        ssl_port = m.group(1)

                if not tcp_port and line.find("INSECUREROUTER") >= 0:
                    m = re.match("^.*?-p (\d+).*?$", line)
                    if m:
                        tcp_port = m.group(1)
            return ssl_port, tcp_port

        self.ctx.out("")
        if not iga:
            self.ctx.out(
                "No icegridadmin available: Cannot check server list")
        else:
            item("Server", "icegridnode")
            p = self.ctx.popen(self._cmd("-e", "server list"))  # popen
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
                    p2 = self.ctx.popen(
                        self._cmd("-e", "server state %s" % s))  # popen
                    p2.wait()
                    io2 = p2.communicate()
                    if io2[1]:
                        self.ctx.err(io2[1].strip())
                    elif io2[0]:
                        self.ctx.out(io2[0].strip())
                    else:
                        self.ctx.err("UNKNOWN!")
            if self._isWindows():
                # Print the OMERO server Windows service details
                hscm = win32service.OpenSCManager(
                    None, None, win32service.SC_MANAGER_ALL_ACCESS)
                services = win32service.EnumServicesStatus(hscm)
                omesvcs = tuple((sname, fname) for sname, fname, status
                                in services if "OMERO" in fname)
                for sname, fname in omesvcs:
                    item("Server", fname)
                    hsc = win32service.OpenService(
                        hscm, sname, win32service.SC_MANAGER_ALL_ACCESS)
                    logonuser = win32service.QueryServiceConfig(hsc)[7]
                    if win32service.QueryServiceStatus(hsc)[1] == \
                            win32service.SERVICE_RUNNING:
                        self.ctx.out("active (running as %s)" % logonuser)
                    else:
                        self.ctx.out("inactive")
                    win32service.CloseServiceHandle(hsc)
                win32service.CloseServiceHandle(hscm)

            # List SSL & TCP ports of deployed applications
            self.ctx.out("")
            p = self.ctx.popen(self._cmd("-e", "application list"))  # popen
            rv = p.wait()
            io = p.communicate()
            if rv != 0:
                self.ctx.out("Cannot list deployed applications.")
                self.ctx.dbg("""
                Stdout:\n%s
                Stderr:\n%s
                """ % io)
            else:
                applications = io[0].split()
                applications.sort()
                for s in applications:
                    p2 = self.ctx.popen(
                        self._cmd("-e", "application describe %s" % s))
                    io2 = p2.communicate()
                    if io2[1]:
                        self.ctx.err(io2[1].strip())
                    elif io2[0]:
                        ssl_port, tcp_port = get_ports(io2[0])
                        item("%s" % s, "SSL port")
                        if not ssl_port:
                            self.ctx.err("Not found")
                        else:
                            self.ctx.out("%s" % ssl_port)

                        item("%s" % s, "TCP port")
                        if not tcp_port:
                            self.ctx.err("Not found")
                        else:
                            self.ctx.out("%s" % tcp_port)
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

        log_dir(self.ctx.dir / "var" / "log", "Log dir", "Log files",
                ["Blitz-0.log", "Tables-0.log", "Processor-0.log",
                 "Indexer-0.log", "FileServer.log", "MonitorServer.log",
                 "DropBox.log", "TestDropBox.log", "OMEROweb.log"])

        # Parsing well known issues
        self.ctx.out("")
        ready = re.compile(".*?ome.services.util.ServerVersionCheck\
        .*OMERO.Version.*Ready..*?")
        db_ready = re.compile(".*?Did.you.create.your.database[?].*?")
        data_dir = re.compile(".*?Unable.to.initialize:.FullText.*?")
        pg_password = re.compile(".*?org.postgresql.util.PSQLException:\
        .FATAL:.password.*?authentication.failed.for.user.*?")
        pg_user = re.compile(""".*?org.postgresql.util.PSQLException:\
        .FATAL:.role.".*?".does.not.exist.*?""")
        pg_conn = re.compile(""".*?org.postgresql.util.PSQLException:\
        .Connection.refused.""")

        issues = {
            ready: "=> Server restarted <=",
            db_ready: "Your database configuration is invalid",
            data_dir: "Did you create your omero.data.dir? E.g. /OMERO",
            pg_password: "Your postgres password seems to be invalid",
            pg_user: "Your postgres user is invalid",
            pg_conn: "Your postgres hostname and/or port is invalid"
        }

        try:
            for file in ('Blitz-0.log',):

                p = self.ctx.dir / "var" / "log" / file
                import fileinput
                for line in fileinput.input([str(p)]):
                    lno = fileinput.filelineno()
                    for k, v in issues.items():
                        if k.match(line):
                            item('Parsing %s' % file, "[line:%s] %s"
                                 % (lno, v))
                            self.ctx.out("")
                            break
        except:
            self.ctx.err("Error while parsing logs")

        self.ctx.out("")

        def env_val(val):
            item("Environment", "%s=%s"
                 % (val, os.environ.get(val, "(unset)")))
            self.ctx.out("")
        env_val("OMERO_HOME")
        env_val("OMERO_NODE")
        env_val("OMERO_MASTER")
        env_val("OMERO_TEMPDIR")
        env_val("PATH")
        env_val("PYTHONPATH")
        env_val("ICE_HOME")
        env_val("LD_LIBRARY_PATH")
        env_val("DYLD_LIBRARY_PATH")

        self.ctx.out("")
        for dir_name, dir_path, dir_size in (
                ("data", omero_data_dir, ""),
                ("temp", omero_temp_dir, True)):
            dir_path_exists = os.path.exists(dir_path)
            is_writable = os.access(dir_path, os.R_OK | os.W_OK)
            if dir_size and dir_path_exists:
                dir_size = self.getdirsize(omero_temp_dir)
                dir_size = "   (Size: %s)" % dir_size
            self.ctx.out("OMERO %s dir: '%s'\tExists? %s\tIs writable? %s%s" %
                         (dir_name, dir_path, dir_path_exists, is_writable,
                          dir_size))

        from omero.plugins.web import WebControl
        try:
            WebControl().status(args)
        except:
            self.ctx.out("OMERO.web not installed!")

    def getdirsize(self, directory):
        total = 0
        for values in os.walk(directory):
            for filename in values[2]:
                total += os.path.getsize(os.path.join(values[0], filename))
        return total

    def session_manager(self, communicator):
        import IceGrid
        import Glacier2
        iq = communicator.stringToProxy("IceGrid/Query")
        iq = IceGrid.QueryPrx.checkedCast(iq)
        sm = iq.findAllObjectsByType("::Glacier2::SessionManager")[0]
        sm = Glacier2.SessionManagerPrx.checkedCast(sm)
        return sm

    def can_access(self, filepath, mask=os.R_OK | os.W_OK):
        """
        Check that the given path belongs to
        or is accessible by the current user
        on Linux systems.
        """

        if "Windows" == platform.system():
            return

        pathobj = path(filepath)

        if not pathobj.exists():
            self.ctx.die(8, "FATAL: OMERO directory does not exist: %s"
                         % pathobj)

        owner = os.stat(filepath)[stat.ST_UID]
        if owner == 0:
            msg = ""
            msg += "FATAL: OMERO directory which needs to be writeable"\
                " belongs to root: %s\n" % filepath
            msg += "Please use \"chown -R NEWUSER %s\" and run as then"\
                " run %s as NEWUSER" % (filepath, sys.argv[0])
            self.ctx.die(9, msg)
        else:
            if not os.access(filepath, mask):
                self.ctx.die(10, "FATAL: Cannot access %s, a required"
                             " file/directory for OMERO" % filepath)

    def check_access(self, mask=os.R_OK | os.W_OK, config=None):
        """Check that 'var' is accessible by the current user."""

        var = self.ctx.dir / 'var'
        if not os.path.exists(var):
            self.ctx.out("Creating directory %s" % var)
            os.makedirs(var, 0700)
        else:
            self.can_access(var, mask)

        if config is not None:
            omero_data_dir = '/OMERO'
            config = config.as_map()
            try:
                omero_data_dir = config['omero.data.dir']
            except KeyError:
                pass
            self.can_access(omero_data_dir)
        for p in os.listdir(var):
            subpath = os.path.join(var, p)
            if os.path.isdir(subpath):
                self.can_access(subpath, mask)

    def check_node(self, args):
        """
        If the args argparse.Namespace argument has no "node" attribute,
        then assign one.
        """
        if not hasattr(args, "node"):
            args.node = self._node()

    def checkice(self, args=None):
        """
        Checks for Ice version 3.4

        See ticket:2514, ticket:1260
        """

        def _check(msg, vers):
            compat = ice_compatibility.split(".")
            vers = vers.split(".")
            if compat[0:2] != vers[0:2]:
                self.ctx.die(164, "%s is not compatible with %s: %s"
                             % (msg, ".".join(compat), ".".join(vers)))

        import Ice
        vers = Ice.stringVersion()
        _check("IcePy version", vers)

        popen = self.ctx.popen(["icegridnode", "--version"])
        vers = popen.communicate()[1]
        _check("icegridnode version", vers)

    def open_config(self, unused):
        """
        Callers are responsible for closing the
        returned ConfigXml object.
        """
        cfg_xml = self.ctx.dir / "etc" / "grid" / "config.xml"
        cfg_tmp = self.ctx.dir / "etc" / "grid" / "config.xml.tmp"
        grid_dir = self.ctx.dir / "etc" / "grid"
        if not cfg_xml.exists() and self.can_access(grid_dir):
            if cfg_tmp.exists() and self.can_access(cfg_tmp):
                self.ctx.dbg("Removing old config.xml.tmp")
                cfg_tmp.remove()
            config = omero.config.ConfigXml(str(cfg_tmp))
            try:
                self.ctx.controls["config"].upgrade(None, config)
            finally:
                config.close()
            self.ctx.err("Creating %s" % cfg_xml)
            cfg_tmp.rename(str(cfg_xml))

        try:
            try:
                config = omero.config.ConfigXml(str(cfg_xml))
            except Exception, e:
                self.ctx.die(577, str(e))
            if config.save_on_close:
                config.save()
            else:
                self.ctx.err("%s read-only" % cfg_xml)
        except portalocker.LockException:
            try:
                config.close()
            except:
                pass
            self.ctx.die(111, "Could not acquire lock on %s" % cfg_xml)

        return config

    @with_config
    def reindex(self, args, config):
        self.check_access(config=config)
        import omero.java
        server_dir = self.ctx.dir / "lib" / "server"
        log_config_file = self.ctx.dir / "etc" / "logback-indexing-cli.xml"
        logback = "-Dlogback.configurationFile=%s" % log_config_file
        classpath = [file.abspath() for file in server_dir.files("*.jar")]
        xargs = [logback, "-Xmx1024M", "-cp", os.pathsep.join(classpath)]

        cfg = config.as_map()
        config.close()  # Early close. See #9800
        for x in ("name", "user", "host", "port"):
            # NOT passing password on command-line
            k = "omero.db.%s" % x
            if k in cfg:
                v = cfg[k]
                xargs.append("-D%s=%s" % (k, v))
        if "omero.data.dir" in cfg:
            xargs.append("-Domero.data.dir=%s" % cfg["omero.data.dir"])
        for k, v in cfg.items():
            if k.startswith("omero.search"):
                xargs.append("-D%s=%s" % (k, cfg[k]))

        cmd = ["ome.services.fulltext.Main"]

        if args.full:
            cmd.append("full")
        elif args.events:
            cmd.append("events")
        elif getattr(args, "class"):
            cmd.append("reindex")
            cmd.extend(getattr(args, "class"))
        else:
            self.ctx.die(502, "No valid action: %s" % args)

        debug = False
        if getattr(args, "jdwp"):
            debug = True

        self.ctx.dbg(
            "Launching Java: %s, debug=%s, xargs=%s" % (cmd, debug, xargs))
        p = omero.java.popen(
            cmd, debug=debug, xargs=xargs, stdout=sys.stdout,
            stderr=sys.stderr)  # FIXME. Shouldn't use std{out,err}
        self.ctx.rv = p.wait()

    def ports(self, args):
        self.check_access()
        from omero.install.change_ports import change_ports
        if not args.skipcheck:
            if 0 == self.status(args, node_only=True):
                self.ctx.die(
                    100, "Can't change ports while the server is running!")

            # Resetting return value.
            self.ctx.rv = 0

        if args.prefix:
            for x in ("registry", "tcp", "ssl"):
                setattr(args, x, "%s%s" % (args.prefix, getattr(args, x)))
        change_ports(
            args.ssl, args.tcp, args.registry, args.revert, dir=self.ctx.dir)

    def cleanse(self, args):
        self.check_access()
        from omero.util.cleanse import cleanse
        client = self.ctx.conn(args)
        cleanse(data_dir=args.data_dir, dry_run=args.dry_run,
                query_service=client.sf.getQueryService(),
                config_service=client.sf.getConfigService())

    def sessionlist(self, args):
        client = self.ctx.conn(args)
        service = client.sf.getQueryService()
        params = omero.sys.ParametersI()
        query = "select s from Session s join fetch s.node n join fetch"\
            " s.owner o where s.closed is null and n.id != 0"
        results = service.findAllByQuery(query, params)
        mapped = list()
        for s in results:
            rv = list()
            mapped.append(rv)
            if not s.isLoaded():
                rv.append("")
                rv.append("id=%s" % s.id.val)
                rv.append("")
                rv.append("")
                rv.append("")
                rv.append("insufficient privileges")
            else:
                rv.append(s.node.id)
                rv.append(s.uuid)
                rv.append(s.started)
                rv.append(s.owner.omeName)
                if s.userAgent is None:
                    rv.append("")
                else:
                    rv.append(s.userAgent)
                if client.getSessionId() == s.uuid.val:
                    rv.append("current session")
                else:
                    rv.append("")
        self.ctx.controls["hql"].display(
            mapped, ("node", "session", "started", "owner", "agent", "notes"))
try:
    register("admin", AdminControl, HELP)
except NameError:
    if __name__ == "__main__":
        cli = CLI()
        cli.register("admin", AdminControl, HELP)
        cli.invoke(sys.argv[1:])
