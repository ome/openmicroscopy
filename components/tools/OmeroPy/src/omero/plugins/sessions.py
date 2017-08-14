#!/usr/bin/env python
# -*- coding: utf-8 -*-

#
# Copyright (C) 2008-2014 Glencoe Software, Inc. All rights reserved.
#
# This program is free software; you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation; either version 2 of the License, or
# (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License along
# with this program; if not, write to the Free Software Foundation, Inc.,
# 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.

"""
   Plugin for viewing and controlling active sessions for a local user.

   Plugin read by omero.cli.Cli during initialization. The method(s)
   defined here will be added to the Cli class for later use.
"""

import os
import sys
import Ice
import IceImport
import time
import traceback
import warnings
import omero.java

IceImport.load("Glacier2_Router_ice")

from Glacier2 import PermissionDeniedException

from omero.rtypes import rlong
from omero.rtypes import unwrap
from omero.util import get_user
from omero.util.sessions import SessionsStore
from omero.cli import BaseControl, CLI
from omero_ext.argparse import SUPPRESS

HELP = """Control and create user sessions

Sessions are stored locally on disk. Several can
be active simultaneously, but only one will be used
for a single invocation of bin/omero.

"""

LONGHELP = """
Uses the login parameters from %(prog)s to login.

To list these options, use "%(prog)s -h"

Options for logging in:

    # Provide all values interactively
    $ bin/omero sessions login
    Server: [localhost:4064]
    Username: [root]
    Password:

    # Pass values as the target
    $ bin/omero sessions login user@omero.example.com
    Password:
    $ bin/omero sessions login user@omero.example.com:24064
    Password:

    # Pass some values via arguments
    $ bin/omero -s localhost sessions login
    Username: [user]
    Password:
    $ bin/omero -p 24064 sessions login
    Server: [localhost:24064]
    Username: [user]
    Password:

    # Pass all non-password values via arguments
    $ bin/omero -s localhost -u john sessions login
    Password:

    # Use a session ID to login without a password
    $ bin/omero -s localhost -k 8afe443f-19fc-4cc4-bf4a-850ec94f4650 \
    sessions login

    # Arguments can also go earlier
    $ bin/omero -k 8afe443f-19fc-4cc4-bf4a-850ec94f4650 sessions login

    # The *last* "@" symbol is used
    $ bin/omero sessions login my.email@example.com@omero.example.com
    Password:

    # System administrators can use "--sudo" to login as others
    $ bin/omero sessions login --sudo=root example@localhost
    Password for root:

Other sessions commands:

    # Logging out of the currently active sessions
    $ bin/omero sessions logout

    # List all locally available sessions (purging the expired ones)
    $ bin/omero sessions list

    # List all local sessions
    $ bin/omero sessions list --no-purge

    # List all active server sessions
    $ bin/omero sessions who

    # List or change the group for the session
    $ bin/omero sessions group
    $ bin/omero sessions group mygroup
    $ bin/omero sessions group 123

    # List or change the timeToLive for the session
    $ bin/omero sessions timeout
    $ bin/omero sessions timeout 300.0 # Seconds
    $ bin/omero sessions timeout 300.0 --session=$UUID

Custom sessions directory:

    # Specify a custom session directory using OMERO_SESSIONDIR
    $ export OMERO_SESSIONDIR=/tmp/my_sessions
    # Create a new session stored under OMERO_SESSIONDIR
    $ bin/omero sessions login
    $ bin/omero sessions file
    $ bin/omero sessions list
"""

LISTHELP = """
By default, inactive sessions are purged from the local sessions store and
removed from the listing. To list all sessions stored locally independently of
their status, use the --no-purge argument.
"""

GROUPHELP = """
If any current services are open, the command will fail.
"""

WHOHELP = """
Administrators will receive a list of all active sessions
along with critical information on last activity. This is
useful for determining whether or not the server can be
restarted.

Other users will only see a list of names, i.e. users who
can be considered "online".
"""


class SessionsControl(BaseControl):

    FACTORY = SessionsStore

    def store(self, args):
        try:
            # Read base directory from deprecated --session-dir argument
            base_dir = getattr(args, "session_dir", None)
            if base_dir:
                warnings.warn(
                    "--session-dir is deprecated. Use OMERO_SESSIONDIR"
                    " instead.", DeprecationWarning)

            # Read base directory from deprecated OMERO_SESSION_DIR envvar
            base_dir = os.environ.get('OMERO_SESSION_DIR', base_dir)
            if 'OMERO_SESSION_DIR' in os.environ:
                warnings.warn(
                    "OMERO_SESSION_DIR is deprecated. Use OMERO_SESSIONDIR"
                    " instead.", DeprecationWarning)

            # Read sessions directory from OMERO_SESSIONDIR envvar
            session_dir = None
            if base_dir:
                from path import path
                session_dir = path(base_dir) / "omero" / "sessions"
            sessions_dir = os.environ.get('OMERO_SESSIONDIR', session_dir)

            return self.FACTORY(sessions_dir)
        except OSError, ose:
            filename = getattr(ose, "filename", sessions_dir)
            self.ctx.die(155, "Could not access session dir: %s" % filename)

    def _configure(self, parser):
        parser.add_login_arguments()
        sub = parser.sub()
        parser.add(sub, self.help, "Extended help")
        login = parser.add(
            sub, self.login, self.login.__doc__)
        logout = parser.add(
            sub, self.logout, "Logout and remove current session key")
        self._configure_login(login)

        group = parser.add(
            sub, self.group,
            "Set the group of the given session by id or name" + GROUPHELP)
        group.add_argument(
            "target",
            nargs="?",
            help="Id or name of the group to switch this session to")

        timeout = parser.add(
            sub, self.timeout,
            "Query or set the timeToIdle for the given session")
        timeout.add_argument(
            "seconds",
            nargs="?",
            type=long,
            help="Number of seconds to set the timeToIdle value to")
        timeout.add_argument(
            "--session",
            help="Session other than the current to update")

        list = parser.add(sub, self.list, (
            "List all available sessions stored locally\n\n" + LISTHELP))
        list.add_argument(
            "--no-purge", dest="purge", action="store_false",
            help="Do not remove inactive sessions")

        parser.add(sub, self.who, (
            "List all active server sessions\n\n" + WHOHELP))

        keepalive = parser.add(
            sub, self.keepalive, "Keeps the current session alive")
        keepalive.add_argument(
            "-f", "--frequency", type=int, default=60,
            help="Time in seconds between keep alive calls", metavar="SECS")

        clear = parser.add(
            sub, self.clear, "Close and remove locally stored sessions")
        clear.add_argument(
            "--all", action="store_true",
            help="Remove all locally stored sessions not just inactive ones")

        file = parser.add(
            sub, self.file, "Print the path to the current session file")

        key = parser.add(
            sub, self.key, "Print the key of the current active session")

        for x in (file, key, logout, keepalive, list, clear, group):
            self._configure_dir(x)

    def _configure_login(self, login):
        login.add_login_arguments()
        login.add_argument(
            "-t", "--timeout", type=long,
            help="Timeout for session. After this many inactive seconds, the"
            " session will be closed")
        login.add_argument(
            "connection", nargs="?",
            help="Connection string. See extended help for examples")
        self._configure_dir(login)

    def _configure_dir(self, parser):
        parser.add_argument("--session-dir", help=SUPPRESS)

    def help(self, args):
        self.ctx.out(LONGHELP % {"prog": args.prog})

    def login(self, args):
        ("Login to a given server, and store session key locally.\n\n"
         "USER, HOST, and PORT are set as args or in a ssh-style "
         "connection string.\n"
         "PASSWORD can be entered interactively, or passed via "
         "-w (insecure!).\n"
         "Alternatively, a session KEY can be passed with '-k'.\n"
         "Admin users can use --sudo=ADMINUSER to login for others.\n\n"
         "Examples:\n"
         "  bin/omero login example.com\n"
         "  bin/omero login user@example.com\n"
         "  bin/omero login user@example.com:24064\n"
         "  bin/omero login -k SESSIONKEY example.com\n"
         "  bin/omero login --sudo=root user@example\n"
         "\n")

        """
        Goals:
        If server and key, then don't ask any questions.
        If nothing requested, and something's active, use it. (i.e. don't
        require port number)
        Reconnect if possible (assuming parameters are the same)
        """

        if self.ctx.conn() and not self.ctx.isquiet:
            self.ctx.err("Active client found")
            return  # EARLY EXIT

        create = getattr(args, "create", None)
        store = self.store(args)
        previous = store.get_current()

        # Basic props, don't get fiddled with
        props = {}
        if args.group:
            props["omero.group"] = args.group

        #
        # Retrieving the parameters as set by the user
        # If these are set and different from the current
        # connection, then a new one may be created.
        #
        # May be called by another plugin
        server = getattr(args, "connection", None)
        name = None
        port = None

        if args.server:
            if server:
                self.ctx.die(3, "Server specified twice: %s and %s"
                             % (server, args.server))
            else:
                server = args.server

        if server:
            server, name, port = self._parse_conn(server, name)

        if args.user:
            if name:
                self.ctx.die(4, "Username specified twice: %s and %s"
                             % (name, args.user))
            else:
                name = args.user

        if args.port:
            if port:
                self.ctx.die(5, "Port specified twice: %s and %s"
                             % (port, args.port))
            else:
                port = args.port

        #
        # If a key is provided, then that takes precedence.
        # Unless the key is bad, there's no reason to ask
        # the user for any more input.
        #
        pasw = args.password
        if args.key:
            if name and not self.ctx.isquiet:
                self.ctx.err("Overriding name since session key set")
            name = args.key
            if args.group and not self.ctx.isquiet:
                self.ctx.err("Ignoring group since session key set")
            if args.password and not self.ctx.isquiet:
                self.ctx.err("Ignoring password since session key set")
            pasw = args.key
        #
        # If no key provided, then we check the last used connection
        # by default. The only requirement is that the user can't
        # have requested a differente server / name or conflicting
        # props (group / port)
        #
        elif previous[0] and previous[1]:

                server_differs = (server is not None and server != previous[0])
                name_differs = (name is not None and name != previous[1])
                port_differs = (port is not None and port != previous[3])

                if not create and not server_differs and not name_differs \
                        and not port_differs:
                    try:
                        if previous[2] is not None:
                            # Missing session uuid file. Deleted? See #4199
                            conflicts = store.conflicts(
                                previous[0], previous[1], previous[2], props,
                                True)
                            if conflicts:
                                self.ctx.dbg("Not attaching because of"
                                             " conflicts: %s" % conflicts)
                            else:
                                rv = store.attach(*previous[:-1])
                                return self.handle(rv, "Using")
                        if not self.ctx.isquiet:
                            self.ctx.out("Previously logged in to %s:%s as %s"
                                         % (previous[0], previous[3],
                                            previous[1]))
                    except Exception, e:
                        self.ctx.out("Previous session expired for %s on"
                                     " %s:%s" % (previous[1], previous[0],
                                                 previous[3]))
                        self.ctx.dbg("Exception on attach: %s"
                                     % traceback.format_exc(e))
                        try:
                            store.remove(*previous[:-1])
                        except OSError, ose:
                            self.ctx.dbg("Session file missing: %s" % ose)
                        except:
                            self.ctx.dbg("Exception on remove: %s"
                                         % traceback.format_exc(e))
                            # Could tell user to manually clear here and then
                            # self.ctx.die()
                            self.ctx.err("Failed to remove session: %s" % e)

        #
        # If we've reached here, then the user either does not have
        # an active session or has requested another (different options)
        # If they've omitted some required value, we must ask for it.
        #
        if not server:
            server, name, port = self._get_server(store, name, port)
        if not name:
            name = self._get_username(previous[1])

        props["omero.host"] = server
        props["omero.user"] = name
        if port:
            props["omero.port"] = port
        if "timeout" in args and args.timeout:
            props["omero.timeout"] = args.timeout

        rv = None
        #
        # For session key access, we now need to lookup the stored_name
        # since otherwise, all access to the directory under ~/omero/sessions
        # will fail. Then, if no session can be found, we exit immediately
        # rather than asking for a password. See #4223
        #
        if args.key:
            stored_name = store.find_name_by_key(server, args.key)
            if not stored_name:
                # ticket:5975 : If this is the case, then this session key
                # did not come from a CLI login, and so we're not going to
                # modify the value returned by store.get_current()
                self.ctx.dbg("No local session file found for %s." % args.key)
                rv = self.attach(store, server, args.key, args.key, props,
                                 False, set_current=False)
            else:
                rv = self.check_and_attach(store, server, stored_name,
                                           args.key, props, check_group=False)
            action = "Joined"
            if not rv:
                if port:
                    msg = "Cannot join %s on %s:%s." % (args.key, server, port)
                else:
                    msg = "Cannot join %s on %s." % (args.key, server)
                self.ctx.die(523, "Bad session key. %s" % msg)
        elif not create:
            available = store.available(server, name)
            for uuid in available:
                rv = self.check_and_attach(store, server, name, uuid, props,
                                           check_group=True)
                action = "Reconnected to"

        if not rv:

            if not pasw:
                # Note: duplicating the `not pasw` check here
                # for an overall nicer error message.
                self._require_tty("cannot request password")

            tries = 3
            while True:
                try:
                    if not pasw:
                        if args.sudo:
                            prompt = "Password for %s:" % args.sudo
                        else:
                            prompt = "Password:"
                        pasw = self.ctx.input(prompt, hidden=True,
                                              required=True)
                    rv = store.create(name, pasw, props, sudo=args.sudo)
                    break
                except PermissionDeniedException, pde:
                    tries -= 1
                    if not tries:
                        self.ctx.die(524, "3 incorrect password attempts")
                    else:
                        self.ctx.err(pde.reason)
                        pasw = None
                except omero.RemovedSessionException, rse:
                    self.ctx.die(525, "User account error: %s." % rse.message)
                except Ice.ConnectionRefusedException:
                    if port:
                        self.ctx.die(554, "Ice.ConnectionRefusedException:"
                                     " %s:%s isn't running" % (server, port))
                    else:
                        self.ctx.die(554, "Ice.ConnectionRefusedException: %s"
                                     " isn't running" % server)
                except Ice.DNSException:
                    self.ctx.die(555, "Ice.DNSException: bad host name: '%s'"
                                 % server)
                except omero.SecurityViolation, sv:
                    self.ctx.die(557, "SecurityViolation: %s" % sv.message)
                except Exception, e:
                    exc = traceback.format_exc()
                    self.ctx.dbg(exc)
                    self.ctx.die(556, "InternalException: Failed to connect:"
                                 " %s" % e)
            action = "Created"

        return self.handle(rv, action)

    def check_and_attach(self, store, server, name, uuid, props,
                         check_group=False):
        """
        Checks for conflicts in the settings for this session,
        and if there are none, then attempts an "attach()". If
        that fails, the session is removed.
        """

        exists = store.exists(server, name, uuid)

        if exists:
            conflicts = store.conflicts(server, name, uuid, props,
                                        check_group=check_group)
            if conflicts:
                if "omero.port" in conflicts:
                    self.ctx.dbg("Skipping session %s due to mismatching"
                                 " ports: %s " % (uuid, conflicts))
                elif not self.ctx.isquiet:
                    self.ctx.err("Skipped session %s due to property"
                                 " conflicts: %s" % (uuid, conflicts))
                return None

        return self.attach(store, server, name, uuid, props, exists)

    def attach(self, store, server, name, uuid, props, exists,
               set_current=True):
        rv = None
        try:
            if exists:
                rv = store.attach(server, name, uuid, set_current=set_current)
            else:
                rv = store.create(name, name, props, set_current=set_current)
        except Exception, e:
            self.ctx.dbg("Removing %s: %s" % (uuid, e))
            store.clear(server, name, uuid)
        return rv

    def handle(self, rv, action):
        """
        Handles a new connection
        """
        client, uuid, idle, live = rv
        sf = client.sf

        # detachOnDestroy called by omero.util.sessions
        client.enableKeepAlive(300)
        ec = sf.getAdminService().getEventContext()
        self.ctx.set_event_context(ec)
        self.ctx.set_client(client)

        host = client.getProperty("omero.host")
        port = client.getProperty("omero.port")

        msg = "%s session %s (%s@%s:%s)." \
            % (action, uuid, ec.userName, host, port)
        msg += self._parse_timeout(idle, " Idle timeout: ")
        msg += self._parse_timeout(live, " Expires in : ")

        msg += (" Current group: %s" % ec.groupName)

        if not self.ctx.isquiet:
            self.ctx.err(msg)

    def _parse_timeout(self, timeout, msg=""):
        timeout = unwrap(timeout)
        if not timeout:
            return ""

        unit = "min."
        val = float(timeout) / 60 / 1000
        if val < 5:
            unit = "s."
            val = val * 60
        return "%s%.f %s" % (msg, val, unit)

    def logout(self, args):
        store = self.store(args)
        previous = store.get_current()

        try:
            rv = store.attach(*previous[:-1])
            rv[0].killSession()
        except Exception, e:
            self.ctx.dbg("Exception on logout: %s" % e)
        store.remove(*previous[:-1])
        # Last is still useful. Not resetting.
        # store.set_current("", "", "")

    def group(self, args):
        client = self.ctx.conn(args)
        sf = client.sf
        admin = sf.getAdminService()

        if args.target is None:
            ec = self.ctx.get_event_context()
            self.ctx.out("ExperimenterGroup:%s" % ec.groupId)
            return ec.groupName

        try:
            group_id = long(args.target)
            group_name = admin.getGroup(group_id).name.val
        except ValueError:
            group_name = args.target
            group_id = admin.lookupGroup(group_name).id.val

        ec = self.ctx.get_event_context()  # 5711
        old_id = ec.groupId
        old_name = ec.groupName
        if old_id == group_id and not self.ctx.isquiet:
            self.ctx.err("Group '%s' (id=%s) is already active"
                         % (group_name, group_id))
        else:
            try:
                sf.setSecurityContext(omero.model.ExperimenterGroupI(
                    group_id, False))
                self.ctx.set_event_context(
                    sf.getAdminService().getEventContext())
                self.ctx.out("Group '%s' (id=%s) switched to '%s' (id=%s)" % (
                    old_name, old_id, group_name, group_id))
            except omero.SecurityViolation, sv:
                    self.ctx.die(564, "SecurityViolation: %s" % sv.message)

    def timeout(self, args):
        client = self.ctx.conn(args)
        svc = client.sf.getSessionService()

        uuid = args.session
        if uuid is None:
            uuid = self.ctx.get_event_context().sessionUuid
        try:
            obj = svc.getSession(uuid)
        except:
            self.ctx.dbg(traceback.format_exc())
            self.ctx.die(557, "cannot get session: %s" % uuid)

        if args.seconds is None:
            # Query only
            secs = unwrap(obj.timeToIdle)/1000.0
            self.ctx.out(secs)
            return secs

        req = omero.cmd.UpdateSessionTimeoutRequest()
        req.session = uuid
        req.timeToIdle = rlong(args.seconds * 1000)
        try:
            cb = client.submit(req)  # Response is "OK"
            cb.close(True)
        except omero.CmdError, ce:
            self.ctx.dbg(str(ce.err))
            self.ctx.die(558, "CmdError: %s" % ce.err.name)
        except:
            self.ctx.dbg(traceback.format_exc())
            self.ctx.die(559, "cannot update timeout for %s" % uuid)

    def list(self, args):
        store = self.store(args)
        s = store.contents()
        previous = store.get_current()

        # fmt = "%-16.16s\t%-12.12s\t%-12.12s\t%-40.40s\t%-30.30s\t%s"
        # self.ctx.out(fmt % ("Server","User","Group", "Session","Active",
        # "Started"))
        # self.ctx.out("-"*136)
        headers = ("Server", "User", "Group", "Session", "Active", "Started")
        results = dict([(x, []) for x in headers])
        for server, names in s.items():
            for name, sessions in names.items():
                for uuid, props in sessions.items():
                    rv = None
                    msg = "True"
                    grp = "Unknown"
                    started = "Unknown"
                    port = None
                    try:
                        if props:
                            port = props.get("omero.port", port)
                        rv = store.attach(server, name, uuid)
                        try:
                            a_s = rv[0].sf.getAdminService()
                            grp = a_s.getEventContext().groupName
                            s_s = rv[0].sf.getSessionService()
                            started = s_s.getSession(uuid).started.val
                            started = time.ctime(started / 1000.0)
                        finally:
                            if rv:
                                rv[0].closeSession()
                    except PermissionDeniedException, pde:
                        msg = pde.reason
                    except Exception, e:
                        self.ctx.dbg("Exception on attach: %s" % e)
                        msg = "Unknown exception"

                    if rv is None and args.purge:
                        try:
                            self.ctx.dbg("Purging %s / %s / %s"
                                         % (server, name, uuid))
                            store.remove(server, name, uuid)
                            continue
                        except IOError, ioe:
                            self.ctx.dbg("Aborting session purging. %s" % ioe)
                            break

                    if server == previous[0] and name == previous[1] and \
                            uuid == previous[2]:
                        msg = "Logged in"

                    if port:
                        results["Server"].append("%s:%s" % (server, port))
                    else:
                        results["Server"].append(server)

                    results["User"].append(name)
                    results["Group"].append(grp)
                    results["Session"].append(uuid)
                    results["Active"].append(msg)
                    results["Started"].append(started)

        from omero.util.text import Table, Column
        columns = tuple([Column(x, results[x]) for x in headers])
        self.ctx.out(str(Table(*columns)))

    def who(self, args):
        client = self.ctx.conn(args)
        uuid = self.ctx.get_event_context().sessionUuid
        req = omero.cmd.CurrentSessionsRequest()
        try:
            cb = client.submit(req)
            try:
                rsp = cb.getResponse()
            finally:
                cb.close(True)

            headers = ["name", "group", "logged in", "agent", "timeout"]
            extra = set()
            results = {"name": [], "group": [],
                       "logged in": [], "agent": [],
                       "timeout": []}

            # Preparse data to find extra columns
            for idx, s in enumerate(rsp.sessions):
                for k in rsp.data[idx].keys():
                    extra.add(k)
            for add in sorted(extra):
                headers.append(add)
                results[add] = []

            for idx, s in enumerate(rsp.sessions):
                ec = rsp.contexts[idx]
                data = unwrap(rsp.data[idx])
                # Handle missing keys
                for k in extra:
                    if k not in data.keys():
                        results[k].append("---")
                for k, v in sorted(data.items()):
                    try:
                        if k.endswith("Time"):
                            t = v / 1000.0
                            t = time.localtime(t)
                            v = time.strftime('%Y-%m-%d %H:%M:%S', t)
                    except:
                        pass
                    results[k].append(v)
                results["name"].append(ec.userName)
                results["group"].append(ec.groupName)
                if s is not None:
                    t = s.started.val / 1000.0
                    t = time.localtime(t)
                    t = time.strftime("%Y-%m-%d %H:%M:%S", t)
                    if uuid == ec.sessionUuid:
                        t = t + " (*)"
                    results["logged in"].append(t)
                    results["agent"].append(unwrap(s.userAgent))
                    results["timeout"].append(
                        self._parse_timeout(s.timeToIdle))
                else:
                    # Insufficient privileges. The EventContext
                    # will be missing fields as well.
                    msg = "---"
                    results["logged in"].append(msg)
                    results["agent"].append(msg)
                    results["timeout"].append(msg)

            from omero.util.text import Table, Column
            columns = tuple([Column(x, results[x]) for x in headers])
            self.ctx.out(str(Table(*columns)))
        except omero.CmdError, ce:
            self.ctx.dbg(str(ce.err))
            self.ctx.die(560, "CmdError: %s" % ce.err.name)
        except omero.ClientError, ce:
            if ce.message == "Null handle":
                v = client.sf.getConfigService().getVersion()
                self.ctx.die(561,
                             "Operation unsupported. Server version: %s" % v)
            else:
                exc = traceback.format_exc()
                self.ctx.dbg(exc)
                self.ctx.die(562, "ClientError: %s" % ce.err.name)
        except omero.LockTimeout:
            exc = traceback.format_exc()
            self.ctx.dbg(exc)
            self.ctx.die(563, "LockTimeout: operation took too long")

    def clear(self, args):
        store = self.store(args)
        count = store.count()
        store.clear()
        self.ctx.out("%s session(s) cleared" % count)

    def keepalive(self, args):
        import threading
        from omero.util.concurrency import get_event as get_event

        class T(threading.Thread):
            def run(self):
                while self.client:
                    try:
                        self.client.sf.keepAlive(None)
                        self.event.wait(args.frequency)
                    except Exception, e:
                        self.err("Keep alive failed: %s" % str(e))
                        return
        t = T()
        t.client = self.ctx.conn(args)
        t.err = self.ctx.err
        t.event = get_event(name="keepalive")
        t.start()
        try:
            self.ctx.out("Running keep alive every %s seconds"
                         % args.frequency)
            self.ctx.input("Press enter to cancel.")
        finally:
            t.client = None
            t.event.set()

    def file(self, args):
        """Return the file associated with the current active session"""
        store = self.store(args)
        srv, usr, uuid, port = store.get_current()
        if srv and usr and uuid:
            self.ctx.out(str(store.dir / srv / usr / uuid))

    def key(self, args):
        """Return the key associated with the current active session"""
        store = self.store(args)
        srv, usr, uuid, port = store.get_current()
        if uuid:
            self.ctx.out(uuid)

    def conn(self, properties=None, profile=None, args=None):
        """
        Either creates or returns the exiting omero.client instance.
        Uses the comm() method with the same signature.
        """

        if properties is None:
            properties = {}

        if self.get_client():
            return self.get_client()

        import omero
        try:
            data = self.initData(properties)
            self.set_client(omero.client(sys.argv, id=data))
            self.get_client().setAgent("OMERO.cli")
            self.get_client().createSession()
            return self.get_client()
        except Exception:
            self.set_client(None)
            raise

    #
    # Private methods
    #

    @staticmethod
    def _parse_conn(server, default_name):
        """Parse a connection string of form (user@)server(:port)"""

        import re
        pat = '^((?P<name>.+)@)?(?P<server>.*?)(:(?P<port>\d{1,5}))?$'
        match = re.match(pat, server)
        server = match.group('server')
        name = match.group('name')
        port = match.group('port')
        if not name:
            name = default_name
        return server, name, port

    def _get_server(self, store, name, port):
        defserver = store.last_host()
        if not port:
            port = str(omero.constants.GLACIER2PORT)
        self._require_tty("cannot request server")
        rv = self.ctx.input("Server: [%s:%s]" % (defserver, port))
        if not rv:
            return defserver, name, port
        else:
            return self._parse_conn(rv, name)

    def _get_username(self, defuser):
        if defuser is None:
            defuser = get_user("root")
        self._require_tty("cannot request username")
        rv = self.ctx.input("Username: [%s]" % defuser)
        if not rv:
            return defuser
        else:
            return rv

    def _require_tty(self, msg):
        if sys.stdin.isatty():
            return
        self.ctx.die(564, "stdin is not a terminal: %s" % msg)


try:
    register("sessions", SessionsControl, HELP)
except NameError:
    if __name__ == "__main__":
        cli = CLI()
        cli.register("sessions", SessionsControl, HELP)
        cli.invoke(sys.argv[1:])
