#!/usr/bin/env python
# -*- coding: utf-8 -*-

"""
   Test of the omero db control.

   Copyright 2009-2013 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

import pytest
import os
from path import path
from omero.plugins.db import DatabaseControl
from omero.util.temp_files import create_path
from omero.cli import NonZeroReturnCode
from omero.cli import CLI
from omero_ext.mox import Mox
import getpass
import __builtin__

hash_map = {
    ('0', ''): 'PJueOtwuTPHB8Nq/1rFVxg==',
    ('0', '--no-salt'): 'vvFwuczAmpyoRC0Nsv8FCw==',
    ('1', ''): 'pvL5Tyr9tCD2esF938sHEQ==',
    ('1', '--no-salt'): 'vvFwuczAmpyoRC0Nsv8FCw==',
}


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
        self.script_file = "%(version)s__%(patch)s.sql" % self.data
        if os.path.isfile(self.script_file):
            os.rename(self.script_file, self.script_file + '.bak')
        assert not os.path.isfile(self.script_file)

        self.mox = Mox()
        self.mox.StubOutWithMock(getpass, 'getpass')
        self.mox.StubOutWithMock(__builtin__, "raw_input")

    def teardown_method(self, method):
        self.file.remove()
        if os.path.isfile(self.script_file):
            os.remove(self.script_file)
        if os.path.isfile(self.script_file + '.bak'):
            os.rename(self.script_file + '.bak', self.script_file)

        self.mox.UnsetStubs()
        self.mox.VerifyAll()

    def password(self, string, strict=True):
        self.cli.invoke("db password " + string % self.data, strict=strict)

    def testBadVersionDies(self):
        with pytest.raises(NonZeroReturnCode):
            self.cli.invoke("db script NONE NONE pw", strict=True)

    def testPasswordIsAskedForAgainIfDiffer(self):
        self.expectPassword("ome")
        self.expectConfirmation("bad")
        self.expectPassword("ome")
        self.expectConfirmation("ome")
        self.mox.ReplayAll()
        self.password("")

    def testPasswordIsAskedForAgainIfEmpty(self):
        self.expectPassword("")
        self.expectPassword("ome")
        self.expectConfirmation("ome")
        self.mox.ReplayAll()
        self.password("")

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
        out, err = capsys.readouterr()
        assert out.strip() == self.password_output(user_id, no_salt)

    @pytest.mark.parametrize('file_arg', ['', '-f', '--file'])
    @pytest.mark.parametrize('no_salt', ['', '--no-salt'])
    @pytest.mark.parametrize(
        'pos_input', ["", "%(version)s", "%(version)s %(patch)s",
                      "%(version)s %(patch)s ome"])
    def testScript(self, pos_input, no_salt, file_arg):
        args = "db script "
        args += pos_input
        if no_salt:
            args += " %s" % no_salt
        if file_arg:
            args += " %s %s" % (file_arg, str(self.file))
            output = self.file
        else:
            output = self.script_file
        if "version" not in pos_input or "patch" not in pos_input:
            self.expectVersion(self.data["version"])
            self.expectPatch(self.data["patch"])
        if "ome" not in pos_input:
            self.expectPassword("ome")
            self.expectConfirmation("ome")
            self.mox.ReplayAll()
        self.cli.invoke(args % self.data, strict=True)

        with open(output) as f:
            lines = f.readlines()
            for line in lines:
                if line.startswith('insert into password values (0'):
                    assert line.strip() == self.script_output(no_salt)

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

    def password_output(self, user_id, no_salt):
        update_msg = "UPDATE password SET hash = \'%s\'" \
            " WHERE experimenter_id  = %s;"
        if not user_id:
            user_id = "0"
        return update_msg % (hash_map[(user_id, no_salt)], user_id)

    def script_output(self, no_salt):
        root_password_msg = "insert into password values (0,\'%s\');"
        return root_password_msg % (hash_map[("0", no_salt)])
