#!/usr/bin/env python

"""
   Test of the sessions plugin

   Copyright 2009 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

import unittest, os, subprocess, StringIO, exceptions
import Ice
import Glacier2
import omero
import omero.all
import omero_ext.uuid as uuid # see ticket:3774

from path import path
from omero.cli import Context, BaseControl, CLI, NonZeroReturnCode
from omero.util import get_user
from omero.util.sessions import SessionsStore
from omero.util.temp_files import create_path
from omero.plugins.sessions import SessionsControl

omeroDir = path(os.getcwd()) / "build"

testsess = "testsess"
testuser = "testuser"
testhost = "testhost"


class MyStore(SessionsStore):

    def __init__(self, *args, **kwargs):
        SessionsStore.__init__(self, *args, **kwargs)
        self.clients = []
        self.exceptions = []

    def create(self, name, pasw, props, new = True, set_current = True):

        if not isinstance(props, dict):
            raise exceptions.Exception("Bad type")

        if self.exceptions:
            raise self.exceptions.pop(0)

        cb = getattr(self, "create_callback", None)
        if cb:
            cb(*args, **kwargs)
            self.create_callback = None
        return_tuple, add_tuple, should_be_new = self.clients.pop(0)

        assert should_be_new == new, ("should_be_new=%s wasn't!" % should_be_new)

        if new:
            self.add(*add_tuple)

        if set_current:
            self.set_current(*add_tuple[0:3])

        return return_tuple

    def __del__(self):
        assert len(self.clients) == 0, ("clients not empty! %s" % self.clients)
        assert len(self.exceptions) == 0, ("exceptions not empty! %s" % self.exceptions)


class MyClient(object):

    def __init__(self, user, group, props):
        self.sf = self
        self.userName = user
        self.groupName = group
        self.props = {"omero.port":"4064"} # Fix after #3883
        self.props.update(props)

    def __del__(self, *args):
        pass

    def enableKeepAlive(self, *args):
        pass

    def getSession(self):
        return self

    def keepAlive(self, prx):
        pass

    def closeSession(self):
        pass

    def getAdminService(self):
        return self

    def getEventContext(self):
        return self

    def getProperty(self, key):
        return self.props[key]

class MyCLI(CLI):

    def __init__(self, *args, **kwargs):
        CLI.__init__(self, *args, **kwargs)
        self.DIR = create_path(folder=True)
        self.REQRESP = {}
        self.STORE = MyStore(self.DIR)
        self.STORE.clear(testhost, testuser)
        self.register("s", SessionsControl, "TEST")
        self.controls["s"].FACTORY = lambda ignore: self.STORE
        assert self.STORE.count(testhost, testuser) == 0

    def __del__(self):
        del self.STORE

    def creates_client(self, name="testuser", host="testhost", sess="sess_id", port=None, group=None, new=True):
        props = dict()
        if port: props["omero.port"] = port
        if group:
            props["omero.group"] = group
        else:
            group = "mygroup" # For use via IAdmin.EventContext

        #props = {"omero.group":group, "omero.port":port}
        return_tuple = (MyClient(name, group, {"omero.host":host}), sess, 0, 0)
        add_tuple = (host, name, sess, props)
        self.STORE.clients.append((return_tuple, add_tuple, new))

    def throw_on_create(self, e):
        self.STORE.exceptions.append(e)

    def requests_host(self, host = "testhost"):
        self.REQRESP["Server: [localhost]"] = host

    def requests_user(self, user = 'testuser'):
        self.REQRESP["Username: [%s]" % get_user("Unknown")] = user

    def requests_pass(self, pasw = "pasw"):
        self.REQRESP["Password:"] = pasw

    def requests_size(self):
        return len(self.REQRESP)

    def assertReqSize(self, test, size):
        test.assertEquals(size, self.requests_size(), "size!=%s: %s" % (size, self.REQRESP))

    def input(self, prompt, hidden=False, required=False):
        if prompt not in self.REQRESP:
            raise exceptions.Exception("Missing prompt: '%s'" % prompt)
        return self.REQRESP.pop(prompt)

    def invoke(self, *args):
        CLI.invoke(self, *args, strict = True)


class TestStore(unittest.TestCase):

    def store(self):
        p = create_path(folder=True)
        return MyStore(p)

    def testReport(self):
        s = self.store()
        s.report()

    def testAdd(self):
        s = self.store()
        s.add("srv", "usr", "uuid", {})
        self.assertEquals(1, len(s.available("srv", "usr")))

    def testDefaults(self):
        s = self.store()
        self.assertEquals("localhost", s.last_host())

    def testCurrent(self):
        s = self.store()
        s.set_current("srv", "usr", "uuid")
        # Using last_* methods
        self.assertEquals("srv", s.last_host())
        # Using helprs
        self.assertEquals("uuid", s.sess_file("srv", "usr").text().strip())
        self.assertEquals("usr", s.user_file("srv").text().strip())
        self.assertEquals("srv", s.host_file().text().strip())

    def testContents(self):
        s = self.store()
        s.add("a", "a", "a", {})
        s.add("b", "b", "b", {})
        rv = s.contents()
        self.assertEquals(2, len(rv))
        self.assertTrue("a" in rv)
        self.assertTrue("a" in rv["a"])
        self.assertTrue("a" in rv["a"]["a"])
        self.assertTrue("b" in rv)
        self.assertTrue("b" in rv["b"])
        self.assertTrue("b" in rv["b"]["b"])

    def testCount(self):
        s = self.store()
        self.assertEquals(0, s.count())
        s.add("a","a","a",{})
        self.assertEquals(1, s.count())
        s.remove("a","a","a")
        self.assertEquals(0, s.count())

    def testGet(self):
        s = self.store()
        s.add("a","b","c", {"foo":"1"})
        rv = s.get("a","b","c")
        expect = {
            "foo":"1",
            "omero.host":"a",
            "omero.user":"b",
            "omero.sess":"c"
        }
        self.assertEquals(expect, rv)

    def testConflicts(self):
        s = self.store()
        s.add("a", "b", "c", {"omero.group":"1"})
        conflicts = s.conflicts("a", "b", "c", {})
        self.assertNotEqual("", conflicts)
        conflicts = s.conflicts("a", "b", "c", {"omero.group":"2"})
        self.assertNotEqual("", conflicts)


class TestSessions(unittest.TestCase):

    def testLoginWithNoArgumentsRequests(self):
        cli = MyCLI()
        cli.requests_host()
        cli.requests_user()
        cli.requests_pass()
        cli.creates_client()
        cli.invoke(["s","login"])
        self.assertEquals(0, cli.rv)

    def test2(self):
        cli = MyCLI()
        cli.requests_pass()
        cli.creates_client(name="user")
        cli.invoke(["s","login","user@host"])
        self.assertEquals(0, cli.rv)

    def test3(self):
        cli = MyCLI()
        cli.creates_client(name="user")
        cli.invoke(["-s", "localhost","-u", "user", "-w", "pasw", "s", "login"])
        self.assertEquals(0, cli.rv)

    def test4(self):
        cli = MyCLI()
        cli.STORE.add("testhost","testuser","key", {})
        cli.creates_client(sess="key", new=False)
        cli.invoke(["-s", "testuser@testhost","-k", "key", "s", "login"])
        self.assertEquals(0, cli.rv)

    def testReuseWorks(self):
        cli = MyCLI()
        cli.STORE.add("testhost","testuser","testsessid", {})
        cli.creates_client(new=False)
        cli.invoke("-s testhost -u testuser s login".split())
        self.assert_(cli._client is not None)

    def testReuseFromDifferentGroupDoesntWork(self):
        cli = MyCLI()
        cli.STORE.add("testhost","testuser","testsessid", {})
        cli.requests_pass()
        cli.assertReqSize(self, 1)
        cli.creates_client(group="mygroup2")
        cli.invoke("-s testhost -u testuser -g mygroup2 s login".split())
        cli.assertReqSize(self, 0)

    def testReuseFromSameGroupDoesWork(self):
        cli = MyCLI()
        cli.STORE.add("testhost","testuser","testsessid", {"omero.group":"mygroup"})
        cli.assertReqSize(self, 0)
        cli.creates_client(group="mygroup", new=False)
        cli.invoke("-s testhost -u testuser -g mygroup s login".split())
        cli.assertReqSize(self, 0)

    def testReuseFromDifferentPortDoesntWork(self):
        cli = MyCLI()
        cli.STORE.add("testhost","testuser","testsessid", {})
        cli.requests_pass()
        cli.assertReqSize(self, 1)
        cli.creates_client(port="4444")
        cli.invoke("-s testhost -u testuser -p 4444 s login".split())
        cli.assertReqSize(self, 0)

    def testReuseFromSamePortDoesWork(self):
        cli = MyCLI()
        cli.STORE.add("testhost","testuser","testsessid", {"omero.port":"4444"})
        cli.assertReqSize(self, 0)
        cli.creates_client(port="4444", new=False)
        cli.invoke("-s testhost -u testuser -p 4444 s login".split())
        cli.assertReqSize(self, 0)

    def testLogicOfConflictsOnNoLocalhostRequested(self):
        cli = MyCLI()
        cli.creates_client()
        cli.invoke("-s testhost -u testuser -w testpass s login")
        cli.invoke("s login") # Should work. No conflict
        cli.invoke("-p 4444 s login")

    def testPortThenNothingShouldReuse(self):
        cli = MyCLI()
        cli.creates_client(port="4444")
        cli.requests_host()
        cli.requests_user()
        cli.requests_pass()
        cli.invoke("-p 4444 s login")
        cli.assertReqSize(self, 0) # All were requested
        cli._client = None # Forcing new instance
        cli.creates_client(port="4444", new=False)
        cli.invoke("s login") # Should work. No conflict
        del cli

    def testBadSessionKeyDies(self):
        """
        As seen in ticket 4223, when a bad session is
        provided, a password shouldn't be asked for.
        """
        cli = MyCLI()

        MOCKKEY = "MOCKKEY"

        # First, successful login
        cli.creates_client(sess=MOCKKEY)
        cli.requests_pass()
        cli.invoke("-s testuser@testhost s login")
        cli.assertReqSize(self, 0) # All were requested
        cli._client = None # Forcing new instance

        key_login = "-s testuser@testhost -k %s s login" % MOCKKEY

        # Now try with session when it's still available
        cli.creates_client(sess=MOCKKEY, new=False)
        cli.invoke(key_login)
        cli._client = None # Forcing new instance

        # Don't do creates_client, so the session key
        # is now bad.
        cli.throw_on_create(Glacier2.PermissionDeniedException("MOCKKEY EXPIRED"))
        try:
            cli.invoke(key_login)
            self.fail("This must throw 'Bad session key'")
        except NonZeroReturnCode:
            pass
        cli._client = None # Forcing new instance

        del cli

    def testCopiedSessionWorks(self):
        """
        Found by Colin while using a session key from
        a non-CLI-source.
        """
        cli = MyCLI()

        MOCKKEY = "MOCKKEY%s" % uuid.uuid4()

        key_login = "-s testuser@testhost -k %s s login" % MOCKKEY

        # Try with session when it's still available
        cli.creates_client(sess=MOCKKEY, new=True)
        cli.invoke(key_login)
        cli._client = None # Forcing new instance

    def assert5975(self, key, cli):
        host, name, uuid = cli.STORE.get_current()
        self.assert_(key != name)

    def test5975(self):
        """
        Runs various tests which try to force the stored user name
        to be a session uuid (which should never happen)
        """
        cli = MyCLI()
        key = str(uuid.uuid4())
        key_login = "-s testuser@testhost -k %s s login" % key
        cli.creates_client(sess=key, new=True)
        cli.invoke(key_login)
        self.assert5975(key, cli)

        cli.invoke("s logout")
        self.assert5975(key, cli)

if __name__ == '__main__':
    unittest.main()
