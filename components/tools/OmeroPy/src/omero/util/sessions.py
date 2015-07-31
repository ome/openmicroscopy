#!/usr/bin/env python
# -*- coding: utf-8 -*-

#
# Copyright (C) 2010-2014 Glencoe Software, Inc.
# All rights reserved.
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
Library for managing user sessions.
"""

import omero.constants
from omero.util import get_user_dir, make_logname
from path import path

import logging

"""
 * Track last used
 * provide single library (with lock) which does all of this
   - save session
   - clear session
   - check session # detachOnDestroy
   - list previous sessions
 * Use an environment variable for changing directories

import subprocess, optparse, os, sys
import getpass, pickle
import omero.java
from omero.cli import Arguments, BaseControl, VERSION
from path import path

"""


class SessionsStore(object):

    """
    The store is a file-based repository of user sessions.
    By default, stores use $HOME/omero/sessions as their
    repository path.

    Use add() to add items to the repository
    """

    def __init__(self, dir=None):
        """
        """
        self.logger = logging.getLogger(make_logname(self))
        if dir is None:
            self.dir = path(get_user_dir()) / "omero" / "sessions"
        else:
            self.dir = path(dir)
        if not self.dir.exists():
            self.dir.makedirs()
        try:
            self.dir.chmod(0700)
        except:
            print "WARN: failed to chmod %s" % self.dir

    #
    # File-only methods
    #

    def report(self):
        """
        Simple dump utility
        """
        for host in self.dir.dirs():
            print "[%s]" % host
            for name in host.dirs():
                print " -> %s : " % name
                for sess in name.files():
                    print "    %s" % sess

    def add(self, host, name, id, props, sudo=None):
        """
        Stores a file containing the properties at
        REPO/host/name/id
        """

        props["omero.host"] = host
        props["omero.user"] = name
        props["omero.sess"] = id
        if sudo is not None:
            props["omero.sudo"] = sudo

        lines = []
        for k, v in props.items():
            lines.append("%s=%s" % (k, v))

        dhn = self.dir / host / name
        if not dhn.exists():
            dhn.makedirs()

        (dhn / id).write_lines(lines)

    def conflicts(self, host, name, id, new_props, ignore_nulls=False,
                  check_group=True):
        """
        Compares if the passed properties are compatible with
        with those for the host, name, id tuple

        If ignore_nulls is True, then a null in new_props means matches
        anything.
        """
        conflicts = ""
        old_props = self.get(host, name, id)
        default_port = str(omero.constants.GLACIER2PORT)
        keys = ["omero.port"]
        if check_group:
            keys.append("omero.group")

        for key in keys:
            old = old_props.get(key, None)
            new = new_props.get(key, None)
            if ignore_nulls and new is None:
                continue
            elif (key == "omero.port" and
                  set((old, new)) == set((None, default_port))):
                continue
            elif old != new:
                if conflicts != "":
                    conflicts += "; "
                conflicts += "%s: %s!=%s" % (key, old, new)
        return conflicts

    def remove(self, host, name, uuid):
        """
        Removes the given session file from the store
        and removes the sess_file() if it is equal to
        the session we just removed.
        """
        if uuid is None:
            self.logger.debug("No uuid provided")
            return
        d = self.dir / host / name
        if d.exists():
            f = d / uuid
            if f.exists():
                f.remove()
                self.logger.debug("Removed %s" % f)
            s = self.sess_file(host, name)
            if s and s.exists() and s.text().strip() == uuid:
                s.remove()
                self.logger.debug("Removed %s" % s)

    def exists(self, host, name, uuid):
        """
        Checks if the given file exists.
        """
        d = self.dir
        for x in (host, name, uuid):
            d = d / x
            if not d.exists():
                return False
        return True

    def get(self, host, name, uuid):
        """
        Returns the properties stored in the given session file
        """
        return self.props(self.dir / host / name / uuid)

    def available(self, host, name):
        """
        Returns the path to property files which are stored.
        Internal accounting files are not returned.
        """
        d = self.dir / host / name
        if not d.exists():
            return []
        return [x.basename() for x in self.non_dot(d)]

    def set_current(self, host, name=None, uuid=None, props=None):
        """
        Sets the current session, user, and host files
        These are used as defaults by other methods.
        """
        if host is not None:
            self.host_file().write_text(host)
        if props is not None:
            port = props.get('omero.port', str(omero.constants.GLACIER2PORT))
            self.port_file().write_text(port)
        if name is not None:
            self.user_file(host).write_text(name)
            if uuid is not None:
                self.sess_file(host, name).write_text(uuid)

    def get_current(self):
        host = None
        name = None
        uuid = None
        if self.host_file().exists():
            host = self.host_file().text().strip()
        if host:
            try:
                name = self.user_file(host).text().strip()
            except IOError:
                pass
        if name:
            try:
                uuid = self.sess_file(host, name).text().strip()
            except IOError:
                pass
        return (host, name, uuid, self.last_port())

    def last_host(self):
        """
        Prints either the last saved host (see get_current())
        or "localhost"
        """
        f = self.host_file()
        if not f.exists():
            return "localhost"
        text = f.text().strip()
        if not text:
            return "localhost"
        return text

    def last_port(self):
        """
        Prints either the last saved port (see get_current())
        or "4064"
        """
        f = self.port_file()
        if not f.exists():
            return str(omero.constants.GLACIER2PORT)
        port = f.text().strip()
        if not port:
            return str(omero.constants.GLACIER2PORT)
        return port

    def find_name_by_key(self, server, uuid):
        """
        Returns the name of a user for which the
        session key exists. This value is taken
        from the path rather than from the properties
        file since that value may have been overwritten.
        An exception is raised if there is more than one
        name since keys should be UUIDs. A None may be
        returned.
        """
        s = self.dir / server
        if not s.exists():
            return None
        else:
            n = [x.basename() for x in s.dirs() if (x / uuid).exists()]
            if not n:
                return None
            elif len(n) == 1:
                return n[0]
            else:
                raise Exception(
                    "Multiple names found for uuid=%s: %s"
                    % (uuid, ", ".join(n)))

    def contents(self):
        """
        Returns a map of maps with all the contents
        of the store. Internal accounting files are
        skipped.
        """
        rv = {}
        Dhosts = self.dir.dirs()
        for Dhost in Dhosts:
            host = str(Dhost.basename())
            if host not in rv:
                rv[host] = {}
            Dnames = Dhost.dirs()
            for Dname in Dnames:
                name = str(Dname.basename())
                if name not in rv[host]:
                    rv[host][name] = {}
                Dids = self.non_dot(Dname)
                for Did in Dids:
                    id = str(Did.basename())
                    props = self.props(Did)
                    props["active"] = "unknown"
                    rv[host][name][id] = props
        return rv

    def count(self, host=None, name=None):
        """
        Returns the sum of all files visited by walk()
        """
        def f(h, n, s):
            f.i += 1
        f.i = 0
        self.walk(f, host, name)
        return f.i

    def walk(self, func, host=None, name=None, sess=None):
        """
        Applies func to all host, name, and session path-objects.
        """
        for h in self.dir.dirs():
            if host is None or str(h.basename()) == host:
                for n in h.dirs():
                    if name is None or str(n.basename()) == name:
                        for s in self.non_dot(n):
                            if sess is None or str(s.basename()) == sess:
                                func(h, n, s)

    #
    # Server-requiring methods
    #

    def attach(self, server, name, sess, set_current=True):
        """
        Simple helper. Delegates to create() using the session
        as both the username and the password. This reproduces
        the logic of client.joinSession()
        """
        props = self.get(server, name, sess)
        return self.create(sess, sess, props, new=False,
                           set_current=set_current)

    def create(self, name, pasw, props, new=True, set_current=True, sudo=None):
        """
        Creates a new omero.client object, and returns:
        (cilent, session_id, timeToIdle, timeToLive)
        """
        import omero.clients
        props = dict(props)
        host = props["omero.host"]
        client = omero.client(props)
        client.setAgent("OMERO.sessions")

        if sudo is not None:
            sf = client.createSession(sudo, pasw)
            principal = omero.sys.Principal()
            principal.name = name
            principal.group = props.get("omero.group", None)
            principal.eventType = "User"
            sess = sf.getSessionService().createSessionWithTimeouts(
                principal, 0, 0)
            client.closeSession()
            sf = client.joinSession(sess.getUuid().getValue())
        else:
            sf = client.createSession(name, pasw)

        ec = sf.getAdminService().getEventContext()
        uuid = sf.ice_getIdentity().name
        sf.detachOnDestroy()
        sess = sf.getSessionService().getSession(uuid)
        timeToIdle = sess.getTimeToIdle().getValue()
        timeToLive = sess.getTimeToLive().getValue()
        if new:
            self.add(host, ec.userName, uuid, props, sudo=sudo)
        if set_current:
            self.set_current(host, ec.userName, uuid, props)

        return client, uuid, timeToIdle, timeToLive

    def clear(self, host=None, name=None, sess=None):
        """
        Walks through all sessions and calls killSession.
        Regardless of exceptions, it will remove the session files
        from the store.
        """
        removed = []

        def f(h, n, s):
            hS = str(h.basename())
            nS = str(n.basename())
            sS = str(s.basename())
            try:
                client = self.attach(hS, nS, sS)
                client.killSession()
            except Exception, e:
                self.logger.debug("Exception on killSession: %s" % e)
            s.remove()
            removed.append(s)
        self.walk(f, host, name, sess)
        return removed

    ##
    # Helpers. Do not modify or rely on mutable state.
    ##

    def host_file(self):
        """ Returns the path-object which stores the last active host """
        return self.dir / "._LASTHOST_"

    def port_file(self):
        """ Returns the path-object which stores the last active port """
        return self.dir / "._LASTPORT_"

    def user_file(self, host):
        """ Returns the path-object which stores the last active user """
        d = self.dir / host
        if not d.exists():
            d.makedirs()
        return d / "._LASTUSER_"

    def sess_file(self, host, user):
        """ Returns the path-object which stores the last active session """
        d = self.dir / host / user
        if not d.exists():
            d.makedirs()
        return d / "._LASTSESS_"

    def non_dot(self, d):
        """
        Only returns the files (not directories)
        contained in d that don't start with a dot
        """
        return [f for f in d.files("*")
                if not str(f.basename()).startswith(".")]

    def props(self, f):
        """
        Parses the path-object into properties
        """
        txt = f.text()
        lines = txt.split("\n")
        props = {}
        for line in lines:
            if not line:
                continue
            parts = line.split("=", 1)
            if len(parts) == 1:
                parts.append("")
            props[parts[0]] = parts[1]
        return props

if __name__ == "__main__":
    SessionsStore().report()
