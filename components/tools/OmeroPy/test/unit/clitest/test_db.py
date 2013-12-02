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
from omero.cli import CLI
from omero_ext.mox import Mox
import getpass
import __builtin__


class TestDatabase(object):

    def setup_method(self, method):
        self.cli = CLI()
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

        self.mox = Mox()
        self.mox.StubOutWithMock(getpass, 'getpass')
        self.mox.StubOutWithMock(__builtin__, "raw_input")

    def teardown_method(self, method):
        self.file.remove()
        self.mox.UnsetStubs()
        self.mox.VerifyAll()

    def script(self, string, strict=True):
        string = string % self.data
        self.cli.invoke("db script -f %s %s" % (str(self.file), string),
                        strict=strict)

    def password(self, string, strict=True):
        self.cli.invoke("db password " + string % self.data, strict=strict)

    def testBadVersionDies(self):
        with pytest.raises(NonZeroReturnCode):
            self.script("NONE NONE pw")

    def testPasswordIsAskedForAgainIfDiffer(self):
        self.expectPassword("ome")
        self.expectConfirmation("bad")
        self.expectPassword("ome")
        self.expectConfirmation("ome")
        self.mox.ReplayAll()
        self.script("'' ''")

    def testPasswordIsAskedForAgainIfEmpty(self):
        self.expectPassword("")
        self.expectPassword("ome")
        self.expectConfirmation("ome")
        self.mox.ReplayAll()
        self.script("%(version)s %(patch)s")

    @pytest.mark.parametrize('id', [None, '1'])
    def testPassword(self, id):
        self.expectPassword("ome", id=id)
        self.expectConfirmation("ome", id=id)
        self.mox.ReplayAll()
        if id:
            self.password("--user-id=%s" % id)
        else:
            self.password("")

    @pytest.mark.parametrize('id', [None, '1'])
    def testAutomatedPassword(self, id):
        if id:
            self.password("ome --user-id=%s" % id)
        else:
            self.password("ome")

    @pytest.mark.parametrize(
        'script_input', ["", "%(version)s", "%(version)s %(patch)s",
                         "%(version)s %(patch)s ome"])
    def testAutomatedScript(self, script_input):
        if "version" not in script_input or "patch" not in script_input:
            self.expectVersion(self.data["version"])
            self.expectPatch(self.data["patch"])
        if "ome" not in script_input:
            self.expectPassword("ome")
            self.expectConfirmation("ome")
            self.mox.ReplayAll()
        self.script(script_input)

    def password_ending(self, user, id):
        if id is not None:
            rv = "user %s: " % id
        else:
            rv = "%s user: " % user
        return "password for OMERO " + rv

    def expectPassword(self, pw, user="root", id=None):
        getpass.getpass("Please enter %s" %
                        self.password_ending(user, id)).AndReturn(pw)

    def expectConfirmation(self, pw, user="root", id=None):
        getpass.getpass("Please re-enter %s" %
                        self.password_ending(user, id)).AndReturn(pw)

    def expectVersion(self, version):
        raw_input("Please enter omero.db.version [%s]: " %
                  self.data["version"]).AndReturn(version)

    def expectPatch(self, patch):
        raw_input("Please enter omero.db.patch [%s]: " %
                  self.data["patch"]).AndReturn(patch)
