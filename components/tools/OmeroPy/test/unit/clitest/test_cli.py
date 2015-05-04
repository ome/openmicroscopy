#!/usr/bin/env python
# -*- coding: utf-8 -*-

"""
   Test of the omero cli base class including line parsing.

   Copyright 2009 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

import pytest

from omero.cli import CLI, NonZeroReturnCode
from omero.plugins.basics import LoadControl


class TestCli(object):

    def testMultipleLoad(self):
        """
        In DropBox, the loading of multiple CLIs seems to
        lead to the wrong context being assigned to some
        controls.

        See #4749
        """
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

        assert len(threads) == len(set([t.cli for t in threads]))
        assert len(threads) == len(set([t.con for t in threads]))
        assert len(threads) == len(set([t.cmp for t in threads]))

    def testLoad(self, tmpdir):
        tmpfile = tmpdir.join('test')
        tmpfile.write("foo")
        self.cli = CLI()
        self.cli.register("load", LoadControl, "help")

        with pytest.raises(NonZeroReturnCode):
            self.cli.invoke("load %s" % tmpfile, strict=True)

        self.cli.invoke("load -k %s" % tmpfile, strict=True)
        self.cli.invoke("load --keep-going %s" % tmpfile, strict=True)
