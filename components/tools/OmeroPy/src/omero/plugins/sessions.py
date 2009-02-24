#!/usr/bin/env python
"""
   Plugin for viewing and controlling active sessions via the
   jgroups-based ome.services.blitz.fire.Ring class

   Plugin read by omero.cli.Cli during initialization. The method(s)
   defined here will be added to the Cli class for later use.

   Copyright 2008 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

import subprocess, optparse, os, sys
import getpass, pickle
import omero.java
from omero.cli import Arguments, BaseControl, VERSION
from path import path

class SessionsControl(BaseControl):

    def help(self, args = None):
        self.ctx.out("table -- print table of session data")

    def table(self, args):
        args = Arguments(args)
        first, other = args.firstOther()
        files = os.listdir("blitz")
        classpath = os.path.pathsep.join(files)
        omero.java.run(["-cp",classpath,SessionsControl.RING, first], debug=False, xargs=[], use_exec = True)

    def isactive(self, *args):
        args = Arguments(*args)

    def login(self, *args):
        args = Arguments(*args)
        name, other = args.firstOther()
        if not name:
            name = self._username()
        pasw = self._password()
        sess = self._session(name, pasw, other)
        self._savesession(sess)

    def logout(self, *args):
        old_sess = self._savesession(None)
        client = omero.client()
        client.joinSession(old_session)
        print "Do until 0"
        client.closeSession()

    def closeall(self, *args):
        print "closeall"

    #
    # Private methods
    #
    def _username(self):
       defuser = getpass.getuser()
       return raw_input("Username: [%s]" % defuser)

    def _password(self):
        return getpass.getpass()

    def _session(self, username, password, other):
        client = self.ctx.conn({"omero.user":username,"omero.pass":password})
        session_id = client.sf.ice_getIdentity().name
        return session_id

    def _savesession(self, sess):
        sess_file = self.ctx.userdir() / "sessionid"
        if sess:
            f = open(str(sess_file), "w")
            try:
                f.write(str(sess))
                print "Created session: %s" % sess
            finally:
                f.close()
            return True
        else:
            if sess_file.exists():
                f = open(str(sess_file), "r")
                try:
                    old_sess = f.readline()
                finally:
                    f.close()
                sess_file.remove()
                self.ctx.out("Cleared session: %s" % old_sess)
                return old_sess
            else:
                self.ctx.out("No active session")
                return None

try:
    register("sessions", SessionsControl)
except NameError:
    SessionsControl()._main()
