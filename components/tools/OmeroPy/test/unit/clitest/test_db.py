#!/usr/bin/env python
# -*- coding: utf-8 -*-

"""
   Test of the omero db control.

   Copyright 2009-2013 Glencoe Software, Inc. All rights reserved.
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

    @pytest.mark.parametrize('no_salt', ['', '--no-salt'])
    @pytest.mark.parametrize('user_id', ['', '0', '1'])
    @pytest.mark.parametrize('password', ['', 'ome'])
    def testPassword(self, user_id, password, no_salt, capsys):
        args = ""
        if user_id:
            args += "--user-id=%s " % user_id
        if no_salt:
            args += "%s " % no_salt
        if password:
            args += "%s" % password
        else:
            self.expectPassword("ome", id=user_id)
            self.expectConfirmation("ome", id=user_id)
            self.mox.ReplayAll()
        self.password(args)
        if no_salt:
            out, err = capsys.readouterr()
            assert out.strip() == self.password_output(user_id)

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
        if id and id != '0':
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

    def password_output(self, user_id):
        update_msg = "UPDATE password SET hash = 'vvFwuczAmpyoRC0Nsv8FCw=='" \
            " WHERE experimenter_id  = %s;"
        if user_id:
            return update_msg % user_id
        else:
            return update_msg % "0"
