#!/usr/bin/env python
"""
   Library for managing user sessions.

   Copyright 2010 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

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

from omero.util import get_user_dir
from path import path

class SessionsStore(object):
    """

    """

    def __init__(self, dir = None):
        """
        """
        if dir == None:
            dir = get_user_dir()
        self.dir = path(dir) / "omero" / "sessions"
        if not self.dir.exists():
            self.dir.makedirs()
        try:
            self.dir.chmod(0700)
        except:
            print "WARN: failed to chmod %s" % self.dir

    def report(self):
        for host in self.dir.dirs():
            print "[%s]" % host
            for name in host.dirs():
                print " -> %s : " % name
                for sess in name.files():
                    print "    %s" % sess

    def add(self, host, name, id, props):
        lines = []
        for k,v in props.items():
            lines.append("%s=%s" % (k, v))
        dhn = self.dir / host / name
        if not dhn.exists():
            dhn.makedirs()
        (dhn / id).write_lines(lines)
        self.current(host, name, id)

    def available(self, host, name):
        d = self.dir / host / name
        if not d.exists():
            return []
        return [x.basename() for x in self.non_dot(d)]

    def current(self, host, name, id):
        self.sess_file(host, name).write_text(id)
        self.user_file(host).write_text(name)
        self.host_file().write_text(host)

    def check(self, host, name, id):
        props = {"omero.host" : host}
        try:
            client, id, timeToIdle, timeToLive = self.create(id, id, props)
            return client
        except exceptions.Exception, e:
            return false

    def clear(self, host, name, id = None):
        d = self.dir / host / name
        removed = []
        if d.exists():
            if id is not None:
                f = d / id
                f.remove()
                removed.append(f)
            else:
                for f in self.non_dot(d):
                    removed.append(f)
                    f.remove()
        return removed

    def attach(self, server, name, id):
        props = {}
        props["omero.host"] = server
        return self.create(id, id, props, new=False)

    def create(self, name, pasw, props, new=True):
        import omero.clients
        props = dict(props)
        client = omero.client(props)
        sf = client.createSession(name, pasw)
        id = sf.ice_getIdentity().name
        sf.detachOnDestroy()
        sess = sf.getSessionService().getSession(id)
        timeToIdle = sess.getTimeToIdle().getValue()
        timeToLive = sess.getTimeToLive().getValue()
        if new:
            self.add(props["omero.host"], name, id, props)
        return client, id, timeToIdle, timeToLive

    def contents(self):
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
                Dids = Dname.files()
                for Did in Dids:
                    id = str(Did.basename())
                    props = self.props(Did)
                    props["active"] = "unknown"
                    rv[host][name][id] = props
        return rv

    def count(self, host, name):
        d = self.dir / host / name
        if not d.exists():
            return 0
        return len(self.non_dot(d))

    def destroy(self, id):
        assert False

    def last_host(self, host = None):
        if host: # Setter
            (self.dir / "lasthost").write_text(str(host))
        else: # Getter
            if not (self.dir / "lasthost").exists():
                return "localhost"
            return (self.dir / "lasthost").text().strip()

    def last_sess(self, *args):
        if not (self.dir / "lastsess").exists():
            return ""
        return (self.dir / "lastsess").text().strip()

    def non_dot(self, d):
        return [f for f in d.files("*") if not str(f.basename()).startswith(".")]

    def props(self, f):
        txt = f.text()
        lines = txt.split("\n")
        props = {}
        for line in lines:
            parts = line.split("=",1)
            if len(parts) == 1:
                parts.append("")
            props[parts[0]] = parts[1]
        return props

    def remove(self, host, name, id):
        (self.dir / host / name / id).remove()

    ##
    ## Helpers
    ##

    def host_file(self):
        return self.dir / "._LASTHOST_"

    def user_file(self, host):
        d = self.dir / host
        if not d.exists():
            d.makedirs()
        return d / "._LASTUSER_"

    def sess_file(self, host, user):
        d = self.dir / host / user
        if not d.exists():
            d.makedirs()
        return d / "._LASTSESS_"

if __name__ == "__main__":
    SessionsStore().report()
