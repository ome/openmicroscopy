#!/usr/bin/env python
# -*- coding: utf-8 -*-

"""
   Test of the omero db control.

   Copyright 2009 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

import pytest
from path import path
from omero.plugins.db import DatabaseControl
from omero.util.temp_files import create_path
from omero.cli import NonZeroReturnCode
from mocks import MockCLI


class TestDatabase(object):

    def setup_method(self, method):
        self.cli = MockCLI()
        self.cli.register("db", DatabaseControl, "TEST")

        dir = path(__file__) / ".." / ".." / ".." / ".." / ".." / ".." /\
            ".." / "dist"  # FIXME: should not be hard-coded
        dir = dir.abspath()
        cfg = dir / "etc" / "omero.properties"
        cfg = cfg.abspath()
        self.cli.dir = dir

        self.data = {}
        for line in cfg.text().split("\n"):
            line = line.strip()
            for x in ("version", "patch"):
                key = "omero.db." + x
                if line.startswith(key):
                    self.data[x] = line[len(key)+1:]

        self.file = create_path()

    def teardown_method(self, method):
        self.file.remove()

    def script(self, string, strict=True):
        string = string % self.data
        self.cli.invoke("db script -f %s %s" % (self.file, string),
                        strict=strict)

    def password(self, string, strict=True):
        self.cli.invoke("db password " + string % self.data, strict=strict)

    def testBadVersionDies(self):
        self.expectPassword("pw")
        self.expectConfirmation("pw")
        pytest.raises(NonZeroReturnCode, self.script, "NONE NONE pw")

    def testPasswordIsAskedForAgainIfDiffer(self):
        self.expectPassword("ome")
        self.expectConfirmation("bad")
        self.expectPassword("ome")
        self.expectConfirmation("ome")
        self.script("'' ''")

    def testPasswordIsAskedForAgainIfEmpty(self):
        self.expectPassword("")
        self.expectPassword("ome")
        self.expectConfirmation("ome")
        self.script("%(version)s %(patch)s")

    def testPassword(self):
        self.expectPassword("ome")
        self.expectConfirmation("ome")
        self.password("")

    def testAutomatedPassword(self):
        self.password("ome")

    def testUserPassword(self):
        self.expectPassword("ome", id="1")
        self.expectConfirmation("ome", id="1")
        self.password("--user-id=1")

    def testAutomatedUserPassword(self):
        self.password("--user-id=1 ome")

    def testScript(self):
        self.expectVersion(self.data["version"])
        self.expectPatch(self.data["patch"])
        self.expectPassword("ome")
        self.expectConfirmation("ome")
        self.script("")

    def testAutomatedScript1(self):

        # This should not be asked for, but ignoring for the moment
        self.expectVersion(self.data["version"])

        self.expectPatch(self.data["patch"])
        self.expectPassword("ome")
        self.expectConfirmation("ome")
        self.script("%(version)s")

    def testAutomatedScript2(self):
        self.expectPassword("ome")
        self.expectConfirmation("ome")
        self.script("%(version)s %(patch)s")

    def testAutomatedScript3(self):
        self.script("%(version)s %(patch)s ome")

    def password_ending(self, user, id):
        if id is not None:
            rv = "user %s: " % id
        else:
            rv = "%s user: " % user
        return "password for OMERO " + rv

    def expectPassword(self, pw, user="root", id=None):
        self.cli.expect("Please enter %s" % self.password_ending(user, id),
                        pw)

    def expectConfirmation(self, pw, user="root", id=None):
        self.cli.expect("Please re-enter %s" % self.password_ending(user, id),
                        pw)

    def expectVersion(self, version):
        self.cli.expect("Please enter omero.db.version [%s]: " %
                        self.data["version"], version)

    def expectPatch(self, patch):
        self.cli.expect("Please enter omero.db.patch [%s]: " %
                        self.data["patch"], patch)
