#!/usr/bin/env python

"""
   Test of the sessions plugin

   Copyright 2009 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

import unittest, os, subprocess, StringIO, getpass, exceptions
from path import path
from omero.cli import Context, BaseControl, CLI
from omero.util.sessions import SessionsStore
from omero.util.temp_files import create_path
from omero.plugins.sessions import SessionsControl

omeroDir = path(os.getcwd()) / "build"

testsess = "testsess"
testuser = "testuser"
testhost = "testhost"


class MyStore(SessionsStore):

    def create(self, *args, **kwargs):
        cb = getattr(self, "create_callback", None)
        if cb:
            cb(*args, **kwargs)
            self.create_callback = None
        return MyClient(), "sess-id", 0, 0


class MyClient(object):

    def __del__(self, *args):
        pass

    def closeSession(self, *args):
        pass


class MyCLI(CLI):

    def __init__(self, *args, **kwargs):
        CLI.__init__(self, *args, **kwargs)
        self.DIR = create_path(folder=True)
        self.REQRESP = {}
        self.STORE = MyStore(self.DIR)
        self.STORE.clear(testhost, testuser)
        self.register("s", SessionsControl, "TEST")
        self.controls["s"].FACTORY = lambda: self.STORE
        assert self.STORE.count(testhost, testuser) == 0

    def requests_host(self, host = "localhost"):
        self.STORE.current("localhost")
        self.REQRESP["Server: [localhost]"] = host

    def requests_user(self, user = 'user'):
        self.REQRESP["Username: [%s]" % getpass.getuser()] = user

    def requests_pass(self, pasw = "pasw"):
        self.REQRESP["Password:"] = pasw

    def requests_size(self):
        return len(self.REQRESP)

    def input(self, prompt, hidden=False, required=False):
        if prompt not in self.REQRESP:
            raise exceptions.Exception("Missing prompt: '%s'" % prompt)
        return self.REQRESP.pop(prompt)


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
        s.current("srv", "usr", "uuid")
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
        cli.invoke(["s","login"])
        self.assertEquals(0, cli.rv)

    def test2(self):
        cli = MyCLI()
        cli.requests_pass()
        cli.invoke(["s","login","user@host"])
        self.assertEquals(0, cli.rv)

    def test3(self):
        cli = MyCLI()
        cli.invoke(["-s", "localhost","-u", "user", "-w", "pasw", "s", "login"])
        self.assertEquals(0, cli.rv)

    def test4(self):
        cli = MyCLI()
        cli.invoke(["-s", "localhost","-k", "key", "s", "login"])
        self.assertEquals(0, cli.rv)

    def testReuseWorks(self):
        cli = MyCLI()
        cli.STORE.add("testhost","testuser","testsessid", {})
        cli.invoke("-s testhost -u testuser s login".split())
        self.assert_(cli._client is not None)

    def testReuseFromDifferentGroupDoesntWork(self):
        cli = MyCLI()
        cli.STORE.add("testhost","testuser","testsessid", {})
        cli.requests_pass()
        self.assertEquals(1, cli.requests_size())
        cli.invoke("-s testhost -u testuser -g mygroup s login".split())
        self.assertEquals(0, cli.requests_size())

    def testReuseFromSameGroupDoesWork(self):
        cli = MyCLI()
        cli.STORE.add("testhost","testuser","testsessid", {"omero.group":"mygroup"})
        self.assertEquals(0, cli.requests_size())
        cli.invoke("-s testhost -u testuser -g mygroup s login".split())
        self.assertEquals(0, cli.requests_size())

    def testReuseFromDifferentPortDoesntWork(self):
        cli = MyCLI()
        cli.STORE.add("testhost","testuser","testsessid", {})
        cli.requests_pass()
        self.assertEquals(1, cli.requests_size())
        cli.invoke("-s testhost -u testuser -p 4444 s login".split())
        self.assertEquals(0, cli.requests_size())

    def testReuseFromSamePortDoesWork(self):
        cli = MyCLI()
        cli.STORE.add("testhost","testuser","testsessid", {"omero.port":"4444"})
        self.assertEquals(0, cli.requests_size())
        cli.invoke("-s testhost -u testuser -p 4444 s login".split())
        self.assertEquals(0, cli.requests_size())

    def testLoginArgsDiesOnLastWithoutPWorKeyself(self):
        cli = MyCLI()
        def test(dies, args):
            try:
                cli.invoke(args)
                if dies:
                    self.fail("Didn't die: %s" % args)
            except (exceptions.Exception, SystemExit), e:
                if not dies:
                    raise
                    #self.fail("Died: %s (%s)" % (args, e))
        test(True, "-L s login")
        test(True, "--last s login")
        test(False, "-L -k KEY s login")
        test(False, "-L -w PASS s login")
        test(False, "--last -k KEY s login")
        test(False, "--last -w PASS s login")

if __name__ == '__main__':
    unittest.main()
