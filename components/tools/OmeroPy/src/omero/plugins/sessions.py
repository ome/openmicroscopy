#!/usr/bin/env python
"""
   Plugin for viewing and controlling active sessions for a local user.

   Plugin read by omero.cli.Cli during initialization. The method(s)
   defined here will be added to the Cli class for later use.

   Copyright 2008 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""


import os
import sys
import Ice, Glacier2, IceImport
import time
import traceback
import exceptions
import subprocess
import getpass
import omero.java

IceImport.load("Glacier2_Router_ice")

from omero.util import get_user
from omero.util.sessions import SessionsStore
from omero.cli import BaseControl, CLI
from path import path

HELP = """Control and create user sessions

Sessions are stored locally on disk. Several can
be active simultaneously, but only one will be used
for a single invocation of bin/omero.

"""

LONGHELP = """
    Uses the login parameters from %(prog)s to login.

    To list these options, use "%(prog)s -h"

    Sample session:
        $ bin/omero -s localhost sessions login
        Username:
        Password:
        $ bin/omero -s localhost -u john sessions login
        Password
        $ bin/omero -s localhost -k 8afe443f-19fc-4cc4-bf4a-850ec94f4650 sessions login
        $ bin/omero sessions login
        Server:
        Username:
        Password:
        $ bin/omero sessions login user@omero.example.com
        Password:
        $ bin/omero sessions logout
        $ bin/omero sessions login
        Reuse current session? [Y/n]
        $ bin/omero sessions list
        $ bin/omero sessions logout
        $ bin/omero sessions login omero.example.com
        Username:
        Password:
        $ bin/omero sessions logout
        $ bin/omero -p 24064 sessions login
        Server:
        Username:
        Password:
        $ bin/omero sessions login my.email@example.com@omero.example.com
        Password:
        $ bin/omero -k 8afe443f-19fc-4cc4-bf4a-850ec94f4650 sessions login
        $ bin/omero sessions clear
        $ bin/omero sessions list --session-dir=/tmp
"""

class SessionsControl(BaseControl):

    FACTORY = SessionsStore

    def store(self, args):
        try:
            dirpath = getattr(args, "session_dir", None)
            return self.FACTORY(dirpath)
        except OSError, ose:
            filename = getattr(ose, "filename", dirpath)
            self.ctx.die(155, "Could not access session dir: %s" % filename)

    def _configure(self, parser):
        sub = parser.sub()
        help = parser.add(sub, self.help, "Extended help")
        login = parser.add(sub, self.login, "Login to a given server, and store session key locally")
        logout = parser.add(sub, self.logout, "Logout and remove current session key")
        self._configure_login(login, logout)
        group = parser.add(sub, self.group, "Set the group of the current session by id or name")
        group.add_argument("target", help="Id or name of the group to switch this session to")
        list = parser.add(sub, self.list, "List all locally stored sessions")
        purge = list.add_mutually_exclusive_group()
        purge.add_argument("--purge", action="store_true", default = True, help="Remove inactive sessions")
        purge.add_argument("--no-purge", dest="purge", action="store_false", help="Do not remove inactive sessions")
        keepalive = parser.add(sub, self.keepalive, "Keeps the current session alive")
        keepalive.add_argument("-f", "--frequency", type=int, default=60, help="Time in seconds between keep alive calls", metavar="SECS")
        clear = parser.add(sub, self.clear, "Close and remove locally stored sessions")
        clear.add_argument("--all", action="store_true", help="Remove all locally stored sessions not just inactive ones")
        file = parser.add(sub, self.file, "Print the path to the current session file")

        for x in (file, logout, keepalive, list, clear, group):
            self._configure_dir(x)

    def _configure_login(self, login, logout = None):
        login.add_argument("-t", "--timeout", help="Timeout for session. After this many inactive seconds, the session will be closed")
        login.add_argument("connection", nargs="?", help="Connection string. See extended help for examples")
        self._configure_dir(login)

    def _configure_dir(self, parser):
        parser.add_argument("--session-dir", help="Use a different sessions directory (Default: $HOME/omero/sessions)")

    def help(self, args):
        self.ctx.err(LONGHELP % {"prog":args.prog})

    def login(self, args):
        """
        Goals:
        If server and key, then don't ask any questions.
        If nothing requested, and something's active, use it. (i.e. don't require port number)
        Reconnect if possible (assuming parameters are the same)
        """

        if self.ctx.conn():
            self.ctx.err("Active client found")
            return # EARLY EXIT

        create = getattr(args, "create", None)
        store = self.store(args)
        previous = store.get_current()
        try:
            previous_props = store.get(*previous)
            previous_port = previous_props.get("omero.port", str(omero.constants.GLACIER2PORT))
        except:
            previous_port = str(omero.constants.GLACIER2PORT)

        # Basic props, don't get fiddled with
        props = {}
        if args.port:
            props["omero.port"] = args.port
        if args.group:
            props["omero.group"] = args.group

        #
        # Retrieving the parameters as set by the user
        # If these are set and different from the current
        # connection, then a new one may be created.
        #
        server = getattr(args, "connection", None) # May be called by another plugin
        name = None

        if args.server:
            if server:
                self.ctx.die(3, "Server specified twice: %s and %s" % (server, args.server))
            else:
                server = args.server

        if server: server, name = self._parse_conn(server)

        if args.user:
            if name:
                self.ctx.die(4, "Username specified twice: %s and %s" % (name, args.user))
            else:
                name = args.user

        #
        # If a key is provided, then that takes precedence.
        # Unless the key is bad, there's no reason to ask
        # the user for any more input.
        #
        pasw = args.password
        if args.key:
            if name:
                self.ctx.err("Overriding name since session set")
            name = args.key
            if args.password:
                self.ctx.err("Ignoring password since key set")
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

                if not create and not server_differs and not name_differs:
                    try:
                        if previous[2] is not None: # Missing session uuid file. Deleted? See #4199
                            conflicts = store.conflicts(previous[0], previous[1], previous[2], props, True)
                            if conflicts:
                                self.ctx.dbg("Not attaching because of conflicts: %s" % conflicts)
                            else:
                                rv = store.attach(*previous)
                                return self.handle(rv, "Using")
                        self.ctx.out("Previously logged in to %s:%s as %s" % (previous[0], previous_port, previous[1]))
                    except exceptions.Exception, e:
                        self.ctx.out("Previous session expired for %s on %s:%s" % (previous[1], previous[0], previous_port))
                        self.ctx.dbg("Exception on attach: %s" % traceback.format_exc(e))
                        try:
                            store.remove(*previous)
                        except OSError, ose:
                            self.ctx.dbg("Session file missing: %s" % ose)
                        except:
                            self.ctx.dbg("Exception on remove: %s" % traceback.format_exc(e))
                            # Could tell user to manually clear here and then self.ctx.die()
                            self.ctx.err("Failed to remove session: %s" % e)


        #
        # If we've reached here, then the user either does not have
        # an active session or has requested another (different options)
        # If they've omitted some required value, we must ask for it.
        #
        if not server: server, name = self._get_server(store)
        if not name: name = self._get_username(previous[1])

        props["omero.host"] = server
        props["omero.user"] = name

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
                self.ctx.dbg("No name found for %s." % args.key)
                rv = self.attach(store, server, args.key, args.key, props,\
                        False, set_current = False)
            else:
                rv = self.check_and_attach(store, server, stored_name, args.key, props)
            action = "Joined"
            if not rv:
                self.ctx.die(523, "Bad session key")
        elif not create:
            available = store.available(server, name)
            for uuid in available:
                rv = self.check_and_attach(store, server, name, uuid, props)
                action = "Reconnected to"

        if not rv:
            tries = 3
            while True:
                try:
                    if not pasw:
                        pasw = self.ctx.input("Password:", hidden = True, required = True)
                    rv = store.create(name, pasw, props)
                    break
                except Glacier2.PermissionDeniedException, pde:
                    tries -= 1
                    if not tries:
                        self.ctx.die(524, "3 incorrect password attempts")
                    else:
                        self.ctx.err(pde.reason)
                        pasw = None
                except Ice.ConnectionRefusedException:
                    self.ctx.die(554, "Ice.ConnectionRefusedException: %s isn't running" % server)
                except Ice.DNSException:
                    self.ctx.die(555, "Ice.DNSException: bad host name: '%s'" % server)
                except exceptions.Exception, e:
                    exc = traceback.format_exc()
                    self.ctx.dbg(exc)
                    self.ctx.die(556, "InternalException: Failed to connect: %s" % e)
            action = "Created"

        return self.handle(rv, action)

    def check_and_attach(self, store, server, name, uuid, props):
        """
        Checks for conflicts in the settings for this session,
        and if there are none, then attempts an "attach()". If
        that fails, the session is removed.
        """

        exists = store.exists(server, name, uuid)

        if exists:
            conflicts = store.conflicts(server, name, uuid, props)
            if conflicts:
                self.ctx.dbg("Skipping %s due to conflicts: %s" % (uuid, conflicts))
                return None

        return self.attach(store, server, name, uuid, props, exists)

    def attach(self, store, server, name, uuid, props, exists, set_current = True):
        rv = None
        try:
            if exists:
                rv = store.attach(server, name, uuid, set_current = set_current)
            else:
                rv = store.create(name, name, props, set_current = set_current)
        except exceptions.Exception, e:
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
        self.ctx._event_context = ec
        self.ctx._client = client

        host = client.getProperty("omero.host")
        port = client.getProperty("omero.port")

        msg = "%s session %s (%s@%s:%s)." % (action, uuid, ec.userName, host, port)
        if idle:
            msg = msg + " Idle timeout: %s min." % (float(idle)/60/1000)
        if live:
            msg = msg + " Expires in %s min." % (float(live)/60/1000)

        msg += (" Current group: %s" % ec.groupName)

        self.ctx.err(msg)

    def logout(self, args):
        store = self.store(args)
        previous = store.get_current()

        import Glacier2
        try:
            rv = store.attach(*previous)
            rv[0].killSession()
        except exceptions.Exception, e:
            self.ctx.dbg("Exception on logout: %s" % e)
        store.remove(*previous)
        # Last is still useful. Not resetting.
        # store.set_current("", "", "")

    def group(self, args):
        store = self.store(args)
        client = self.ctx.conn(args)
        sf = client.sf
        admin = sf.getAdminService()

        try:
            group_id = long(args.target)
            group_name = admin.getGroup(group_id).name.val
        except ValueError, ve:
            group_name = args.target
            group_id = admin.lookupGroup(group_name).id.val

        ec = self.ctx._event_context # 5711
        old_id = ec.groupId
        old_name = ec.groupName
        if old_id == group_id:
            self.ctx.err("Group '%s' (id=%s) is already active" % (group_name, group_id))
        else:
            sf.setSecurityContext(omero.model.ExperimenterGroupI(group_id, False))
            self.ctx.out("Group '%s' (id=%s) switched to '%s' (id=%s)" % (old_name, old_id, group_name, group_id))

    def list(self, args):
        import Glacier2
        store = self.store(args)
        s = store.contents()
        previous = store.get_current()

        #fmt = "%-16.16s\t%-12.12s\t%-12.12s\t%-40.40s\t%-30.30s\t%s"
        #self.ctx.out(fmt % ("Server","User","Group", "Session","Active", "Started"))
        #self.ctx.out("-"*136)
        headers = ("Server", "User", "Group", "Session", "Active", "Started")
        results = dict([(x,[]) for x in headers])
        for server, names in s.items():
            for name, sessions in names.items():
                for uuid, props in sessions.items():
                    rv = None
                    msg = "True"
                    grp = "Unknown"
                    started = "Unknown"
                    port = None
                    try:
                        if props: port = props.get("omero.port", port)
                        rv = store.attach(server, name, uuid)
                        grp = rv[0].sf.getAdminService().getEventContext().groupName
                        started = rv[0].sf.getSessionService().getSession(uuid).started.val
                        started = time.ctime(started / 1000.0)
                        rv[0].closeSession()
                    except Glacier2.PermissionDeniedException, pde:
                        msg = pde.reason
                    except exceptions.Exception, e:
                        self.ctx.dbg("Exception on attach: %s" % e)
                        msg = "Unknown exception"

                    if rv is None and args.purge:
                        self.ctx.dbg("Purging %s / %s / %s" % (server, name, uuid))
                        store.remove(server, name, uuid)

                    if server == previous[0] and name == previous[1] and uuid == previous[2]:
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
                    except exceptions.Exception, e:
                        self.err("Keep alive failed: %s" % str(e))
                        return
        t = T()
        t.client = self.ctx.conn(args)
        t.event = get_event(name="keepalive")
        t.start()
        try:
            self.ctx.out("Running keep alive every %s seconds" % args.frequency)
            self.ctx.input("Press enter to cancel.")
        finally:
            t.client = None
            t.event.set()

    def file(self, args):
        store = self.store(args)
        srv, usr, uuid = store.get_current()
        if srv and usr and uuid:
            self.ctx.out(str(store.dir / srv / usr / uuid))

    def conn(self, properties=None, profile=None, args=None):
        """
        Either creates or returns the exiting omero.client instance.
        Uses the comm() method with the same signature.
        """

        if properties is None: properties = {}

        if self._client:
            return self._client

        import omero
        try:
            data = self.initData(properties)
            self._client = omero.client(sys.argv, id = data)
            self._client.setAgent("OMERO.cli")
            self._client.createSession()
            return self._client
        except exceptions.Exception, exc:
            self._client = None
            raise

    #
    # Private methods
    #

    def _parse_conn(self, server):
        try:
            idx = server.rindex("@")
            return  server[idx+1:], server[0:idx] # server, user which may also contain an @
        except ValueError:
            return server, None

    def _get_server(self, store):
        defserver = store.last_host()
        rv = self.ctx.input("Server: [%s]" % defserver)
        if not rv:
            return defserver, None
        else:
            return self._parse_conn(rv)

    def _get_username(self, defuser):
        if defuser is None:
            defuser = get_user("root")
        rv = self.ctx.input("Username: [%s]" % defuser)
        if not rv:
            return defuser
        else:
            return rv


try:
    register("sessions", SessionsControl, HELP)
except NameError:
    if __name__ == "__main__":
        cli = CLI()
        cli.register("sessions", SessionsControl, HELP)
        cli.invoke(sys.argv[1:])
