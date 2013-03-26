#!/usr/bin/env python
# -*- coding: utf-8 -*-

"""
   Test of the omero cli base class including line parsing.

   Copyright 2009 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""
import unittest, os, subprocess, StringIO
from exceptions import Exception as Exc
from path import path
from omero.cli import Context, BaseControl, CLI

omeroDir = path(os.getcwd()) / "build"

class TestCli(unittest.TestCase):

    def testMultipleLoad(self):
        """
        In DropBox, the loading of multiple CLIs seems to
        lead to the wrong context being assigned to some
        controls.

        See #4749
        """
        import logging
        import random
        from threading import Thread, Event

        event = Event()
        class T(Thread):
            def run(self, *args):
                pause = random.random()
                event.wait(pause)
                self.cli = CLI()
                self.cli.loadplugins()
                self.con = self.cli.controls["admin"]
                self.cmp = self.con.ctx

        threads = [T() for x in range(20)]
        for t in threads:
            t.start()
        event.set()
        for t in threads:
            t.join()

        self.assertEquals(len(threads), len(set([t.cli for t in threads])))
        self.assertEquals(len(threads), len(set([t.con for t in threads])))
        self.assertEquals(len(threads), len(set([t.cmp for t in threads])))

if __name__ == '__main__':
    unittest.main()
