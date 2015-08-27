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
import datetime

from glob import glob
from path import path

import omero
import omero.config

from omero.cli import CLI
from omero.cli import DirectoryType
from omero.cli import NonZeroReturnCode
from omero.cli import VERSION
from omero.cli import UserGroupControl

from omero.plugins.prefs import WriteableConfigControl, with_config

from omero_ext import portalocker
from omero_ext.which import whichall
from omero_ext.argparse import FileType
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
 omero.windows.servicename
 omero.web.application_server.port
 omero.web.server_list

""" + "\n" + "="*50 + "\n"


class AdminControl(WriteableConfigControl, UserGroupControl):

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
        return WriteableConfigControl._complete(
            self, text, line, begidx, endidx)

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
will be used as targets to enable optional sections of the descriptor.

On Windows, two arguments (-u and -w) specify the Windows service Log On As
user credentials. If not specified, omero.windows.user and omero.windows.pass
will be used.""",
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

        diagnostics = Action(
            "diagnostics",
            ("Run a set of checks on the current, "
             "preferably active server")).parser
        diagnostics.add_argument(
            "--no-logs", action="store_true",
            help="Skip log parsing")

        email = Action(
            "email",
            """Send administrative emails to users.

Administrators can contact OMERO users and groups of
users based on configured email settings. A subject
and some text are required. If no text is passed on
the command-line or if "-" is passed, then text will
be read from the standard input.

Examples:

  # Send the contents of a file to everyone
  # except inactive users.
  bin/omero admin email --everyone Subject < some_file.text

  # Include inactive users in the email
  bin/omero admin email --everyone --inactive ...

  # Contact a single group
  bin/omero admin email --group-name system \\
                        Subject short message

  # Contact a list of users
  bin/omero admin email --user-id 10 \\
                        --user-name ralph \\
                        Subject ...

            """).parser
        email.add_argument(
            "subject",
            help="Required subject for the mail")
        email.add_argument(
            "text", nargs="*",
            help=("All further arguments are combined "
                  "to form the body. stdin if none or '-' "
                  "is given."))
        email.add_argument(
            "--everyone", action="store_true",
            help=("Contact everyone in the system regardless "
                  "of other arguments."))
        email.add_argument(
            "--inactive", action="store_true",
            help="Do not filter inactive users.")
        self.add_user_and_group_arguments(email,
                                          action="append",
                                          exclusive=False)

        Action(
            "jvmcfg",
            "Reset configuration settings based on the current system")

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

Command-line tool for re-indexing the database. This command must be run on the
machine where the FullText directory is located. In most cases, you will want
to disable the background indexer before running most of these commands.

See http://www.openmicroscopy.org/site/support/omero/sysadmins/search.html
for more information.

Examples:

  # 1. Reset the 'last indexed' counter. Defaults to 0
  bin/omero admin reindex --reset 0

  # 2. Delete the contents of a corrupt FullText directory
  bin/omero admin reindex --wipe

  # 3. Run indexer in the foreground. Disable the background first
  bin/omero admin reindex --foreground

Other commands (usually unnecessary):

  # Index all objects in the database.
  bin/omero admin reindex --full

  # Index one specific class
  bin/omero admin reindex --class ome.model.core.Image

  # Passing arguments to Java
  JAVA_OPTS="-Dlogback.configurationFile=stderr.xml" \\
  bin/omero admin reindex --foreground

  JAVA_OPTS="-Xdebug -Xrunjdwp:server=y,transport=\
dt_socket,address=8787,suspend=y" \\
  bin/omero admin reindex --foreground

""").parser

        reindex.add_argument(
            "--jdwp", action="store_true",
            help="Activate remote debugging")
        reindex.add_argument(
            "--mem", default="1024m",
            help="Heap size to use")
        reindex.add_argument(
            "--batch", default="500",
            help="Number of items to index before reporting status")
        reindex.add_argument(
            "--merge-factor", "--merge_factor", default="100",
            help=("Higher means merge less frequently. "
                  "Faster but needs more RAM"))
        reindex.add_argument(
            "--ram-buffer-size", "--ram_buffer_size", default="1000",
            help=("Number of MBs to use for the indexing. "
                  "Higher is faster."))
        reindex.add_argument(
            "--lock-factory", "--lock_factory", default="native",
            help=("Choose Lucene lock factory by class or "
                  "'native', 'simple', 'none'"))

        group = reindex.add_mutually_exclusive_group()
        group.add_argument(
            "--prepare", action="store_true",
            help="Disables the background indexer in preparation for indexing")
        group.add_argument(
            "--full", action="store_true",
            help="Reindexes all non-excluded tables sequentially")
        group.add_argument(
            "--events", action="store_true",
            help="Reindexes all non-excluded event logs chronologically")
        group.add_argument(
            "--reset", default=None,
            help="Reset the index counter")
        group.add_argument(
            "--dryrun", action="store_true",
            help=("Run through all events, incrementing the counter. "
                  "NO INDEXING OCCURS"))
        group.add_argument(
            "--foreground", action="store_true",
            help=("Run indexer in the foreground (suggested)"))
        group.add_argument(
            "--class", nargs="+",
            help="Reindexes the given classes sequentially")
        group.add_argument(
            "--wipe", action="store_true",
            help="Delete the existing index files")
        group.add_argument(
            "--finish", action="store_true",
            help="Re-enables the background indexer after for indexing")

        sessionlist = Action(
            "sessionlist", "List currently running sessions").parser
        sessionlist.add_login_arguments()

        cleanse = Action("cleanse", """Remove binary data files from OMERO

Deleting an object from OMERO currently may not remove all the binary data.
Use this command either manually or in a cron job periodically to remove
Pixels, empty directories, and other data.

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

        for name in ("start", "restart"):
            self.actions[name].add_argument(
                "--foreground", action="store_true",
                help="Start server in foreground mode (no daemon/service)")

        for name in ("start", "startasync", "restart", "restartasync"):
            self.actions[name].add_argument(
                "-u", "--user",
                help="Windows Service Log On As user name.")
            self.actions[name].add_argument(
                "-w", "--password", metavar="PW",
                help="Windows Service Log On As user password.")

        for name in ("start", "startasync", "deploy", "restart",
                     "restartasync"):
            self.actions[name].add_argument(
                "file", nargs="?",
                help="Application descriptor. If not provided, a default"
                " will be used")
            self.actions[name].add_argument(
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
        def _get_service_name(unused, config):
            try:
                return config.as_map()["omero.windows.servicename"]
            except KeyError:
                return 'OMERO'

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

        def _stop_service(self, svc_name):
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

        def _start_service(self, config, descript, svc_name, pasw, user):
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

        def _start_service(self, config, descript, svc_name, pasw, user):
            self.ctx.die(
                666, "Could not import win32service and/or win32evtlogutil")

        def _stop_service(self, svc_name):
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

    def _get_etc_dir(self):
        """Return path to directory containing configuration files"""
        return self.ctx.dir / "etc"

    def _get_grid_dir(self):
        """Return path to directory containing Gridconfiguration files"""
        return self._get_etc_dir() / "grid"

    def _get_templates_dir(self):
        """Return path to directory containing templates"""
        return self.ctx.dir / "etc" / "templates"

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
            descript = self._get_grid_dir() / __d__
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
        self.regenerate_templates(args, config)
        self.check_access(config=config)
        self.checkice()
        self.check_node(args)
        if self._isWindows():
            self.checkwindows(args)

        if 0 == self.status(args, node_only=True):
            self.ctx.die(876, "Server already running")

        self.check_lock(config)

        self._initDir()
        # Do a check to see if we've started before.
        self._regdata()
        self.check([])

        command = None
        descript = self._descript(args)
        foreground = hasattr(args, "foreground") and args.foreground

        if self._isWindows():
            if foreground:
                command = """icegridnode.exe "%s" --deploy "%s" %s\
                """ % (self._icecfg(), descript, args.targets)
            else:
                user = args.user
                pasw = args.password
                svc_name = "%s.%s" % (
                    self._get_service_name(config), args.node)
                self._start_service(config, descript, svc_name, pasw, user)
        else:
            if foreground:
                command = ["icegridnode", "--nochdir", self._icecfg(),
                           "--deploy", str(descript)] + args.targets
            else:
                command = ["icegridnode", "--daemon", "--pidfile",
                           str(self._pid()), "--nochdir", self._icecfg(),
                           "--deploy", str(descript)] + args.targets

        if command is not None:
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
        self.regenerate_templates(args, config)
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
            svc_name = "%s.%s" % (self._get_service_name(config), args.node)
            output = self._query_service(svc_name)
            if 0 <= output.find("DOESNOTEXIST"):
                self.ctx.die(203, "%s does not exist. Use 'start' first."
                             % svc_name)
            self._stop_service(svc_name)
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
    def jvmcfg(self, args, config):
        rv = self.regenerate_templates(args, config)

        self.ctx.out("JVM Settings:")
        self.ctx.out("============")
        for k, v in sorted(rv.items()):
            settings = v.pop(0)
            sb = " ".join([str(x) for x in v])
            if str(settings) != "Settings()":
                sb += " # %s" % settings
            self.ctx.out("%s=%s" % (k, sb))

    def regenerate_templates(self, args, config, force_rewrite=False):
        """Internal function in termers"""
        from xml.etree.ElementTree import XML
        from omero.install.jvmcfg import adjust_settings

        if not force_rewrite:
            if 0 == self.status(args, node_only=True):
                self.ctx.die(
                    100, "Can't regenerate templates the server is running!")
            # Reset return value
            self.ctx.rv = 0

        # JVM configuration regeneration
        templates = self._get_templates_dir() / "grid" / "templates.xml"
        generated = self._get_grid_dir() / "templates.xml"
        if generated.exists():
            generated.remove()
        config2 = omero.config.ConfigXml(str(generated))
        template_xml = XML(templates.text())
        try:
            rv = adjust_settings(config, template_xml)
        except Exception, e:
            self.ctx.die(11, 'Cannot adjust memory settings in %s.\n%s'
                         % (templates, e))

        def clear_tail(elem):
            elem.tail = ""
            if elem.text is not None and not elem.text.strip():
                elem.text = ""
            for child in elem.getchildren():
                clear_tail(child)

        clear_tail(template_xml)
        config2.write_element(template_xml)
        config2.XML = None  # Prevent re-saving
        config2.close()
        config.save()

        # Define substitution dictionary for template files
        config = config.as_map()
        substitutions = {
            '@omero.ports.prefix@': config.get('omero.ports.prefix', ''),
            '@omero.ports.ssl@': config.get('omero.ports.ssl', '4064'),
            '@omero.ports.tcp@': config.get('omero.ports.tcp', '4063'),
            '@omero.ports.registry@': config.get(
                'omero.ports.registry', '4061'),
            }

        def copy_template(input_file, output_dir):
            """Replace templates"""

            with open(input_file) as template:
                data = template.read()
            output_file = path(output_dir / os.path.basename(input_file))
            if output_file.exists():
                output_file.remove()
            with open(output_file, 'w') as f:
                for key, value in substitutions.iteritems():
                    data = re.sub(key, value, data)
                f.write(data)

        # Regenerate various configuration files from templates
        for cfg_file in glob(self._get_templates_dir() / "*.cfg"):
            copy_template(cfg_file, self._get_etc_dir())
        for xml_file in glob(
                self._get_templates_dir() / "grid" / "*default.xml"):
            copy_template(xml_file, self._get_etc_dir() / "grid")
        ice_config = self._get_templates_dir() / "ice.config"
        copy_template(ice_config, self._get_etc_dir())

        return rv

    @with_config
    def diagnostics(self, args, config):
        self.check_access(os.R_OK)
        memory = self.regenerate_templates(args, config)
        omero_data_dir = self._get_data_dir(config)

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

        if not args.no_logs:

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

            log_dir(
                self.ctx.dir / "var" / "log", "Log dir", "Log files",
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
                                item('Parsing %s' % file,
                                     "[line:%s] %s" % (lno, v))
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
        env_val("OMERO_USERDIR")
        env_val("OMERO_TMPDIR")
        env_val("PATH")
        env_val("PYTHONPATH")
        env_val("ICE_HOME")
        env_val("LD_LIBRARY_PATH")
        env_val("DYLD_LIBRARY_PATH")

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
                def port_val(port_type, value):
                    item("%s %s port" % (s, port_type),
                         "%s" % value or "Not found")
                    self.ctx.out("")
                p2 = self.ctx.popen(
                    self._cmd("-e", "application describe %s" % s))
                io2 = p2.communicate()
                if io2[1]:
                    self.ctx.err(io2[1].strip())
                elif io2[0]:
                    ssl_port, tcp_port = get_ports(io2[0])
                    port_val("SSL", ssl_port)
                    port_val("TCP", tcp_port)
                else:
                    self.ctx.err("UNKNOWN!")

        for dir_name, dir_path, dir_size in (
                ("data", omero_data_dir, ""),
                ("temp", omero_temp_dir, True)):
            dir_path_exists = os.path.exists(dir_path)
            is_writable = os.access(dir_path, os.R_OK | os.W_OK)
            if dir_size and dir_path_exists:
                dir_size = self.getdirsize(omero_temp_dir)
                dir_size = "   (Size: %s)" % dir_size
            item("OMERO %s dir" % dir_name, "'%s'" % dir_path)
            self.ctx.out("Exists? %s\tIs writable? %s%s" %
                         (dir_path_exists, is_writable,
                          dir_size))

        # JVM settings
        self.ctx.out("")
        for k, v in sorted(memory.items()):
            settings = v.pop(0)
            sb = " ".join([str(x) for x in v])
            if str(settings) != "Settings()":
                sb += " # %s" % settings
            item("JVM settings", " %s" % (k[0].upper() + k[1:]))
            self.ctx.out("%s" % sb)

        # OMERO.web diagnostics
        self.ctx.out("")
        from omero.plugins.web import WebControl
        try:
            WebControl().status(args)
        except:
            self.ctx.out("OMERO.web not installed!")

    def email(self, args):
        client = self.ctx.conn(args)
        iadmin = client.sf.getAdminService()
        users, groups = self.get_users_groups(args, iadmin)

        if not args.text:
            args.text = ("-")

        text = " ".join(args.text)
        if text == "-":
            stdin = FileType("r")("-")
            text = stdin.read()

        if args.everyone:
            if users or groups:
                self.ctx.err("Warning: users and groups ignored")

        req = omero.cmd.SendEmailRequest(
            subject=args.subject,
            body=text,
            userIds=users,
            groupIds=groups,
            everyone=args.everyone,
            inactive=args.inactive)

        try:
            cb = client.submit(
                req, loops=10, ms=500,
                failonerror=True, failontimeout=True)
        except omero.CmdError, ce:
            err = ce.err
            if err.name == "no-body" and err.parameters:
                sb = err.parameters.items()
                sb = ["%s:%s" % (k, v) for k, v in sb]
                sb = "\n".join(sb)
                self.ctx.die(12, sb)
            self.ctx.die(13, "Failed to send emails:\n%s" % err)

        try:
            rsp = cb.getResponse()
            self.ctx.out(
                "Successfully sent %s of %s emails" % (
                    rsp.success, rsp.total
                ))
            if rsp.invalidusers:
                self.ctx.out(
                    "%s users had no email address" % len(
                        rsp.invalidusers)
                )
            if rsp.invalidemails:
                self.ctx.out(
                    "%s email addresses were invalid" % len(
                        rsp.invalidemails)
                )
        finally:
            cb.close(True)

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
            os.makedirs(var)
        else:
            self.can_access(var, mask)

        if config is not None:
            omero_data_dir = self._get_data_dir(config)
            self.can_access(omero_data_dir)

        for p in os.listdir(var):
            subpath = os.path.join(var, p)
            if os.path.isdir(subpath):
                self.can_access(subpath, mask)

    def check_lock(self, config):
        """
        Issue a warning if any of the top ".omero" directories
        contain a lock file. This isn't a conclusive test this
        we don't have access to the DB to get the UUID
        for this instance. Usually there should only be one
        though.
        """
        omero_data_dir = self._get_data_dir(config)
        lock_files = os.path.join(
            omero_data_dir, ".omero", "repository",
            "*", ".lock")
        lock_files = glob(lock_files)
        if lock_files:
            self.ctx.err("WARNING: lock files in %s" %
                         omero_data_dir)
            self.ctx.err("-"*40)
            for lock_file in lock_files:
                self.ctx.err(lock_file)
            self.ctx.err("-"*40)
            self.ctx.err((
                "\n"
                "You may want to stop all server processes and remove\n"
                "these files manually. Lock files can remain after an\n"
                "abrupt server outage and are especially frequent on\n"
                "remotely mounted filesystems like NFS.\n"))

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

        # See ticket #10051
        popen = self.ctx.popen(["icegridnode", "--version"])
        env = self.ctx._env()
        ice_config = env.get("ICE_CONFIG")
        if ice_config is not None and not os.path.exists(ice_config):
            popen = self.ctx.popen(["icegridnode", "--version"],
                                   **{'ICE_CONFIG': ''})

        vers = popen.communicate()[1]
        _check("icegridnode version", vers)

    def open_config(self, unused):
        """
        Callers are responsible for closing the
        returned ConfigXml object.
        """
        cfg_xml = self._get_grid_dir() / "config.xml"
        cfg_tmp = self._get_grid_dir() / "config.xml.tmp"
        grid_dir = self._get_grid_dir()
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
        xargs = [logback, "-cp", os.pathsep.join(classpath)]
        # See etc/grid/templates.xml
        for v in (("warn", "3600000"), ("error", "86400000")):
            xargs.append("-Domero.throttling.method_time.%s=%s" % v)

        cfg = config.as_map()
        omero_data_dir = self._get_data_dir(config)
        config.close()  # Early close. See #9800
        for x in ("name", "user", "host", "port"):
            # NOT passing password on command-line
            k = "omero.db.%s" % x
            if k in cfg:
                v = cfg[k]
                xargs.append("-D%s=%s" % (k, v))

        xargs.append("-Domero.data.dir=%s" % omero_data_dir)
        for k, v in cfg.items():
            if k.startswith("omero.search"):
                xargs.append("-D%s=%s" % (k, cfg[k]))

        locks = {"native": "org.apache.lucene.store.NativeFSLockFactory",
                 "simple": "org.apache.lucene.store.SimpleFSLockFactory",
                 "none": "org.apache.lucene.store.NoLockFactory"}
        lock = locks.get(args.lock_factory, args.lock_factory)

        year2 = datetime.datetime.now().year + 2
        factory_class = "org.apache.lucene.store.FSDirectoryLockFactoryClass"
        xargs2 = ["-Xmx%s" % args.mem,
                  "-Domero.search.cron=1 1 1 1 1 ? %s" % year2,
                  "-Domero.search.batch=%s" % args.batch,
                  "-Domero.search.merge_factor=%s" % args.merge_factor,
                  "-Domero.search.ram_buffer_size=%s" % args.ram_buffer_size,
                  "-D%s=%s" % (factory_class, lock)]
        xargs.extend(xargs2)

        cmd = ["ome.services.fulltext.Main"]

        # Python actions
        early_exit = False
        if args.wipe:
            early_exit = True
            self.can_access(omero_data_dir)
            from os.path import sep
            pattern = sep.join([omero_data_dir, "FullText", "*"])
            files = glob(pattern)
            total = 0
            self.ctx.err("Wiping %s files matching %s" % (len(files), pattern))
            for file in files:
                size = os.path.getsize(file)
                total += 0
                print file, size
            print "Total:", size
            yes = self.ctx.input("Enter 'y' to continue:")
            if not yes.lower().startswith("y"):
                return
            else:
                for file in files:
                    try:
                        os.remove(file)
                    except:
                        self.ctx.err("Failed to remove: %s", file)

        elif args.prepare:
            early_exit = True
            self.stop_service("Indexer-0")
        elif args.finish:
            early_exit = True
            self.start_service("Indexer-0")

        if early_exit:
            return  # Early exit!

        if self.check_service("Indexer-0") and not args.prepare:
            self.ctx.die(578, "Indexer-0 is running")

        # Java actions
        if args.full:
            cmd.append("full")
        elif args.dryrun:
            cmd.append("dryrun")
        elif args.foreground:
            cmd.append("foreground")
        elif args.reset is not None:
            cmd.append("reset")
            cmd.append(args.reset)
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

        # Pass omero.db.pass using JAVA_OPTS environment variable
        if "omero.db.pass" in cfg:
            dbpassargs = "-Domero.db.pass=%s" % cfg["omero.db.pass"]
            if "JAVA_OPTS" not in os.environ:
                os.environ['JAVA_OPTS'] = dbpassargs
            else:
                os.environ['JAVA_OPTS'] = "%s %s" % (
                    os.environ.get('JAVA_OPTS'), dbpassargs)

        self.ctx.dbg(
            "Launching Java: %s, debug=%s, xargs=%s" % (cmd, debug, xargs))
        p = omero.java.run(cmd,
                           use_exec=True, debug=debug, xargs=xargs,
                           stdout=sys.stdout, stderr=sys.stderr)
        self.ctx.rv = p.wait()

    def cleanse(self, args):
        self.check_access()
        from omero.util.cleanse import cleanse
        cleanse(data_dir=args.data_dir, client=self.ctx.conn(args),
                dry_run=args.dry_run)

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

    def check_service(self, name):
        command = self._cmd()
        command.extend(["-e", "server pid %s" % name])
        p = self.ctx.popen(command)  # popen
        rc = p.wait()
        return rc == 0

    def start_service(self, name):
        command = self._cmd()
        command.extend(["-e", "server enable %s" % name])
        rc = self.ctx.call(command)
        if rc != 0:
            self.ctx.err("%s could not be enabled" % name)
        else:
            self.ctx.err("%s restarted" % name)

    def stop_service(self, name):
        command = self._cmd()
        command.extend(["-e", "server disable %s" % name])
        rc = self.ctx.call(command)
        if rc != 0:
            self.ctx.err("%s may already be disabled" % name)
        else:
            command = self._cmd()
            command.extend(["-e", "server stop %s" % name])
            rc = self.ctx.call(command)
            if rc != 0:
                self.ctx.err("'server stop %s' failed" % name)
            else:
                self.ctx.err("%s stopped" % name)

    def _get_data_dir(self, config):
        config = config.as_map()
        return config.get("omero.data.dir", "/OMERO")

try:
    register("admin", AdminControl, HELP)
except NameError:
    if __name__ == "__main__":
        cli = CLI()
        cli.register("admin", AdminControl, HELP)
        cli.invoke(sys.argv[1:])
