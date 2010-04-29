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
from omero.cli import Arguments, BaseControl, VERSION
from path import path

class SessionsControl(BaseControl):

    def help(self, args = None):
        self.ctx.out("login  --   login to a given server, and store session key")
        self.ctx.out("logout --   logout and remove current session key")
        self.ctx.out("info   --   list info on the current session")
        self.ctx.out("list   --   list all stored sessions")
        self.ctx.out("clear  --   close and remove all stored sessions")
        self.ctx.out("""

        TODO logout v. kill ???
        TODO append default loginArguments help here

        Options:
        -d Use a different sessions directory (Default: $HOME/omero/sessions)
        -p Use a different port (Default: 4063)
        -t Timeout

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

        """)

    def login(self, *args):

        if self.ctx.conn():
            self.ctx.dbg("Active client found")
            return # EARLY EXIT

        args = Arguments(args, shortopts="t:d:", longopts=["timeout","dir"])

        server, other = args.firstOther()
        if len(other) > 0:
            self.ctx.die(2, "Unknown argument: %s" % other)

        if server and args.get_server():
            self.ctx.die(3, "Server specified twice: %s and %s" % (server, args.get_server()))

        if not server:
            server = args.get_server()

        store = SessionsStore()
        server, name = self._server(store, server, args.is_quiet())

        if name and args.get_user():
            self.ctx.die(4, "Username specified twice: %s and %s" % (name, args.get_user()))

        if args.get_user():
            name = args.get_user()

        pasw = args.get_password()
        if args.get_key():

            if name:
                self.ctx.err("Overriding name since session set")
            name = args.get_key()

            if args.get_password():
                self.ctx.err("Ignoring password since password set")
            pasw = args.get_key()

        if not name:
            name = self._username(args.is_quiet())

        props = {}
        props["omero.host"] = server
        if args.get_port():
            props["omero.port"] = args.get_port()

        rv = None
        if not args.is_create():
            available = store.available(server, name)
            for uuid in available:
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
                if args.is_quiet():
                    self.ctx.die(394,"A password or key must be provided when 'quiet' is set")
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

    def logout(self, *args):
        old_sess = self._savesession(None)
        client = omero.client()
        client.joinSession(old_session)
        print "Do until 0"
        client.closeSession()

    def list(self, *args):
        store = SessionsStore()
        s = store.contents()
        fmt = "%-25.25s\t%-16.16s\t%-40.40s\t%-8.8s"
        self.ctx.out(fmt % ("Server","User","Session","Active"))
        self.ctx.out("-"*120)
        for server, names in s.items():
            for name, sessions in names.items():
                for id, props in sessions.items():
                    self.ctx.out(fmt % (server, name, id, props["active"]))

    def clear(self, *args):
        store = SessionsStore()
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
    def _server(self, store, server, quiet):
        if not server:
            defserver = store.last_host()
            if quiet:
                self.ctx.out("Using server: %s" % defserver)
                return defserver
            else:
                rv = self.ctx.input("Server: [%s]" % defserver)
                if not rv:
                    return defserver, None # Prevents loop
                else:
                    return self._server(store, rv, quiet)
        else:
            try:
                idx = server.rindex("@")
                return  server[idx+1:], server[0:idx] # server, user which may also contain an @
            except ValueError:
                return server, None

    def _username(self, quiet):
        defuser = getpass.getuser()
        if quiet:
            slf.ctx.out("Using username: %s" % defuser)
            return defuser
        else:
            rv = self.ctx.input("Username: [%s]" % defuser)
            if not rv:
                return defuser
            else:
                return rv


try:
    register("sessions", SessionsControl)
except NameError:
    SessionsControl()._main()
