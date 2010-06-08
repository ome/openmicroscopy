#!/usr/bin/env python
"""
   Plugin for viewing and controlling active sessions for a local user.

   Plugin read by omero.cli.Cli during initialization. The method(s)
   defined here will be added to the Cli class for later use.

   Copyright 2008 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""


import subprocess, optparse, os, sys
import getpass, pickle
import omero.java

from omero.util.sessions import SessionsStore
from omero.cli import BaseControl, CLI
from path import path

HELP = """Control and create user sessions; login and logout"""

LONGHELP = """
    Uses the login parameters from %(prog)s to login.

    To list these options, use "%(prog)s -h"

    Sample session:
        $ login -s localhost
        Username:
        Password:
        $ login -s localhost -u john
        Password
        $ login -s localhost -k 8afe443f-19fc-4cc4-bf4a-850ec94f4650
        $ login
        Server:
        Username:
        Password:
        $ login user@omero.example.com
        Password:
        $ logout
        $ info
        $ login
        Reuse current session? [Y/n]
        $ list
        $ logout
        $ login omero.example.com
        Username:
        Password:
        $ logout
        $ login -p 24063
        Server:
        Username:
        Password:
        $ login my.email@example.com@omero.example.com
        Password:
        $ login -k 8afe443f-19fc-4cc4-bf4a-850ec94f4650
        $ clear
"""

class SessionsControl(BaseControl):

    FACTORY = SessionsStore

    def _configure(self, parser):
        sub = parser.sub()
        help = parser.add(sub, self.help, "Extended help")
        login = parser.add(sub, self.login, "Login to a given server, and store session key locally")
        login.add_argument("-t", "--timeout", help="Timeout for session. After this many inactive seconds, the session will be closed")
        login.add_argument("-d", "--dir", help="Use a different sessions directory (Default: $HOME/omero/sessions)")
        login.add_argument("connection", nargs="?", help="Connection string. See extended help for examples")
        logout = parser.add(sub, self.logout, "Logout and remove current session key")
        info = parser.add(sub, self.info, "List info on current sessions")
        list = parser.add(sub, self.list, "List all stored sessions")
        clear = parser.add(sub, self.clear, "Close and remove all stored sessions")

    def help(self, args):
        self.ctx.err(LONGHELP % {"prog":args.prog})

    def login(self, args):

        if self.ctx.conn():
            self.ctx.err("Active client found")
            return # EARLY EXIT

        server = getattr(args, "connection", None) # May be called by another plugin

        if args.server:
            if server:
                self.ctx.die(3, "Server specified twice: %s and %s" % (server, args.server))
            else:
                server = args.server

        store = self.FACTORY()
        server, name = self._server(store, server, args.last)

        if args.user:
            if name:
                self.ctx.die(4, "Username specified twice: %s and %s" % (name, args.user))
            else:
                name = args.user

        pasw = args.password
        if args.key:

            if name:
                self.ctx.err("Overriding name since session set")
            name = args.key

            if args.password:
                self.ctx.err("Ignoring password since key set")
            pasw = args.key

        if not name:
            name = self._username(args.last)

        props = {}
        props["omero.host"] = server
        props["omero.user"] = name
        if args.port:
            props["omero.port"] = args.port
        if args.group:
            props["omero.group"] = args.group

        rv = None
        if not args.create:
            available = store.available(server, name)
            for uuid in available:
                conflicts = store.conflicts(server, name, uuid, props)
                if conflicts:
                    self.ctx.dbg("Skipping %s due to conflicts: %s" % (uuid, conflicts))
                    continue
                try:
                    rv = store.attach(server, name, uuid)
                    action = "Reconnected to"
                    break
                except:
                    self.ctx.dbg("Removing %s" % uuid)
                    store.clear(server, name, uuid)
                    continue

        if not rv:
            if not pasw:
                if args.last:
                    self.ctx.die(394,"A password or key must be provided when '--last' is set")
                pasw = self.ctx.input("Password:", hidden = True, required = True)
            rv = store.create(name, pasw, props)
            action = "Created"

        self.ctx._client, id, idle, live = rv

        msg = "%s session %s." % (action, id)
        if idle:
            msg = msg + " Idle timeout: %s min." % (float(idle)/60/1000)
        if live:
            msg = msg + " Expires in %s min." % (float(live)/60/1000)

        self.ctx.out(msg)

    def logout(self, args):
        old_sess = self._savesession(None)
        client = omero.client()
        client.joinSession(old_session)
        print "Do until 0"
        client.closeSession()

    def list(self, args):
        store = self.FACTORY()
        s = store.contents()
        fmt = "%-25.25s\t%-16.16s\t%-40.40s\t%-8.8s"
        self.ctx.out(fmt % ("Server","User","Session","Active"))
        self.ctx.out("-"*120)
        for server, names in s.items():
            for name, sessions in names.items():
                for id, props in sessions.items():
                    self.ctx.out(fmt % (server, name, id, props["active"]))

    def info(self, args):
        print "info here"

    def clear(self, args):
        store = self.FACTORY()
        count = store.count()
        store.clear()
        self.ctx.out("%s session(s) cleared" % count)

    def conn(self, properties={}, profile=None, args=None):
        """
        Either creates or returns the exiting omero.client instance.
        Uses the comm() method with the same signature.
        """

        if self._client:
            return self._client

        import omero
        try:
            data = self.initData(properties)
            self._client = omero.client(sys.argv, id = data)
            self._client.setAgent("OMERO.cli")
            self._client.createSession()
            return self._client
        except Exc, exc:
            self._client = None
            raise

    #
    # Private methods
    #
    def _server(self, store, server, use_last):
        if not server:
            defserver = store.last_host()
            if use_last:
                self.ctx.out("Using server: %s" % defserver)
                return defserver, None
            else:
                rv = self.ctx.input("Server: [%s]" % defserver)
                if not rv:
                    return defserver, None # Prevents loop
                else:
                    return self._server(store, rv, use_last)
        else:
            try:
                idx = server.rindex("@")
                return  server[idx+1:], server[0:idx] # server, user which may also contain an @
            except ValueError:
                return server, None

    def _username(self, use_last):
        defuser = getpass.getuser()
        if use_last:
            self.ctx.out("Using username: %s" % defuser)
            return defuser
        else:
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
