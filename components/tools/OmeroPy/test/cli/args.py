#!/usr/bin/env python

"""
   Test of the omero cli argument passing logic. It is assumed that
   all dispatchable methods, i.e. all methods on a control object
   which are callable by the user (typically these are the methods
   which start with an alpha character), will take a single argument
   of type string list. This is what the default __call__ assumes.

   Individual controls are allowed to override this logic.

   Copyright 2008 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""
import unittest, os, subprocess, StringIO
from exceptions import Exception as Exc
from path import path
from omero.cli import Context, BaseControl, Arguments

omeroDir = path(os.getcwd()) / "build"

class TestArgs(unittest.TestCase):

    def testArgumentsCtors(self):
        Arguments()
        Arguments("a string")
        Arguments("a list of strings".split())
        Arguments((["list2"],))

        # Failing calls
        self.assertRaises(Exc, lambda: Arguments({"a":"map"}))

    def testPop(self):

        # None
        self.assertRaises(Exc, lambda: Arguments().popFirst())

        # One
        a = Arguments(["1"])
        a.popFirst()
        self.assertRaises(Exc, lambda: a.popFirst())

    def testCmdArgs(self):

        a = Arguments("a")
        cmd, args = a.firstOther()
        self.assert_( cmd == "a" )
        self.assert_( args == [] )

        a = Arguments(["a","b"])
        cmd, args = a.firstOther()
        self.assert_( cmd == "a" )
        self.assert_( args == ["b"] )

        a = Arguments("a b c")
        cmd, args = a.firstOther()
        self.assert_( cmd == "a" )
        self.assert_( args == ["b", "c"] )

    def testIteration(self):
        a = Arguments("a b c")
        to_find = ["a","b","c"]
        for f in a:
            to_find.remove(f)
        self.assert_(len(to_find) == 0, str(f))

    def testLength(self):
        a = Arguments(" a b c ")
        self.assert_(len(a) == 3)

    def testUnicode(self):
        a = Arguments([unicode("unicode")])
        self.assert_(len(a) == 1)

    def testLoginArgs(self):
        a = Arguments(" -s localhost -u foo -w pass ")

    def testLoginArgsIncompatible(self):
        self.assertRaises(Exc, Arguments, "-C -L")

    def testLoginArgsCallsLoginWithCreate(self):
        class ctx(object):
            def __init__(this):
                this.called = False
            def pub(this, *args):
                this.called = True
            def test(this, *args):
                a1 = Arguments(args)
                a1.acquire(this)
                self.assert_(this.called)
        ctx().test("-C")
        ctx().test("--create")

    def testLoginArgsDiesOnQuietWithoutPWorKeyself(self):
        class ctx(object):
            def __init__(this):
                this.called = False
            def pub(self, *args):
                pass
            def die(this, *args):
                this.called = True
            def test(this, dies, *args):
                a1 = Arguments(args)
                a1.acquire(this)
                self.assertEquals(dies, this.called)
        ctx().test(True, "-q")
        ctx().test(True, "--quiet")
        ctx().test(False, "-q -k KEY")
        ctx().test(False, "-q -w PASS")
        ctx().test(False, "--quiet -k KEY")
        ctx().test(False, "--quiet -w PASS")

if __name__ == '__main__':
    unittest.main()
