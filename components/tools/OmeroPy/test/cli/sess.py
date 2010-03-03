#!/usr/bin/env python

"""
   Test of the sessions plugin

   Copyright 2009 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

import unittest, os, subprocess, StringIO, getpass
from path import path
from omero.cli import Context, BaseControl, CLI
from omero.util.sessions import SessionsStore
from omero.plugins.sessions import SessionsControl

omeroDir = path(os.getcwd()) / "build"

testsess = "testsess"
testuser = "testuser"
testhost = "testhost"

def prep(self):
    self.add(testhost, testuser, testsess)
SessionsStore.prep = prep
def create(self, *args, **kwargs):
    return object(), "sess-id", 0, 0
def check(self, id): # Does this need host/name? (not name)
    assert False
SessionsStore.create = create

class MyCLI(CLI):

    def __init__(self, *args, **kwargs):
        CLI.__init__(self, *args, **kwargs)
        self.REQRESP = {}
        self.STORE = SessionsStore()
        self.STORE.clear(testhost, testuser)
        assert self.STORE.count(testhost, testuser) == 0

    def requests_host(self, host = "localhost"):
        self.STORE.last_host("localhost")
        self.REQRESP["Server: [localhost]"] = host

    def requests_user(self, user = 'user'):
        self.REQRESP["Username: [%s]" % getpass.getuser()] = user

    def requests_pass(self, pasw = "pasw"):
        self.REQRESP["Password:"] = pasw

    def input(self, prompt, hidden=False, required=False):
        if not prompt in self.REQRESP:
            print "Missing:", prompt
        return self.REQRESP.pop(prompt)


class TestSessions(unittest.TestCase):

    def cli(self):
        cli = MyCLI()
        cli.register("s", SessionsControl)
        return cli

    def testLoginWithNoArgumentsRequests(self):
        cli = self.cli()
        cli.requests_host()
        cli.requests_user()
        cli.requests_pass()
        cli.invoke(["s","login"])
        self.assertEquals(0, cli.rv)

    def test2(self):
        cli = self.cli()
        cli.requests_pass()
        cli.invoke(["s","login","user@host"])
        self.assertEquals(0, cli.rv)

    def test3(self):
        cli = self.cli()
        cli.invoke(["s","login","-s", "localhost","-u", "user", "-w", "pasw"])
        self.assertEquals(0, cli.rv)

    def test4(self):
        cli = self.cli()
        cli.invoke(["s","login","-s", "localhost","-k", "key"])
        self.assertEquals(0, cli.rv)

    def testReuse(self):
        cli = self.cli()
        cli.STORE.add("testhost","testuser","testsessid", {})
        cli.invoke("s login -s localhost -u root".split())
        self.assert_(cli._client is not None)

if __name__ == '__main__':
    unittest.main()
