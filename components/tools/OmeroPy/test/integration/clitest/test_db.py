#!/usr/bin/env python
# -*- coding: utf-8 -*-

"""
   Test of the omero db control.

   Copyright 2009-2013 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

from future import standard_library

from builtins import str
from builtins import object
import pytest
import os
from omero.plugins.db import DatabaseControl
from omero.util.temp_files import create_path
from omero.cli import NonZeroReturnCode
from omero.cli import CLI
from mox3 import mox
import getpass
import builtins
import re
standard_library.install_aliases()  # noqa


OMERODIR = False
if 'OMERODIR' in os.environ:
    OMERODIR = os.environ.get('OMERODIR')

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
        self.args = ["db"]

        self.file = create_path()

        self.mox = mox.Mox()
        self.mox.StubOutWithMock(getpass, 'getpass')
        try:
            self.mox.StubOutWithMock(__builtins__, "raw_input")
        except AttributeError:
            # Python 3
            self.mox.StubOutWithMock(builtins, "input")

    def teardown_method(self, method):
        self.file.remove()
        self.mox.UnsetStubs()
        self.mox.VerifyAll()

    @pytest.mark.skipif(OMERODIR is False, reason="Needs omero.db.profile")
    def testBadVersionDies(self):
        with pytest.raises(NonZeroReturnCode):
            self.cli.invoke("db script NONE NONE pw", strict=True)

    @pytest.mark.skipif(OMERODIR is False, reason="db password fails")
    def testPasswordIsAskedForAgainIfDiffer(self):
        self.expectPassword("ome")
        self.expectConfirmation("bad")
        self.expectPassword("ome")
        self.expectConfirmation("ome")
        self.mox.ReplayAll()
        self.cli.invoke("db password", strict=True)

    @pytest.mark.skipif(OMERODIR is False, reason="db password fails")
    def testPasswordIsAskedForAgainIfEmpty(self):
        self.expectPassword("")
        self.expectPassword("ome")
        self.expectConfirmation("ome")
        self.mox.ReplayAll()
        self.cli.invoke("db password", strict=True)

    @pytest.mark.xfail(reason="https://github.com/ome/omero-py/issues/112")
    @pytest.mark.skipif(OMERODIR is False, reason="self.password() fails")
    @pytest.mark.parametrize('no_salt', ['', '--no-salt'])
    @pytest.mark.parametrize('user_id', ['', '0', '1'])
    @pytest.mark.parametrize('password', ['', 'ome'])
    def testPassword(self, user_id, password, no_salt, capsys):
        args = "db password"
        if user_id:
            args += " --user-id=%s " % user_id
        if no_salt:
            args += " %s" % no_salt
        if password:
            args += " %s" % password
        else:
            self.expectPassword("ome", id=user_id)
            self.expectConfirmation("ome", id=user_id)
            self.mox.ReplayAll()
        self.cli.invoke(args, strict=True)

        self.password(args)
        out, err = capsys.readouterr()
        assert out.strip() == self.password_output(user_id, no_salt)

    @pytest.mark.skipif(OMERODIR is False, reason="self.script() fails")
    @pytest.mark.parametrize('file_arg', ['', '-f', '--file'])
    @pytest.mark.parametrize('no_salt', ['', '--no-salt'])
    @pytest.mark.parametrize('password', ['', '--password ome'])
    def testScript(self, no_salt, file_arg, password, capsys):
        """
        Recommended usage of db script
        """
        args = "db script " + password
        if no_salt:
            args += " %s" % no_salt
        if file_arg:
            args += " %s %s" % (file_arg, str(self.file))

        if not password:
            self.expectPassword("ome")
            self.expectConfirmation("ome")
        self.mox.ReplayAll()

        self.cli.invoke(args, strict=True)

        out, err = capsys.readouterr()
        errlines = err.split('\n')
        m1 = re.match(r'Using OMERO(\d.\d) for version', errlines[0])
        assert m1 is not None
        m2 = re.match(r'Using (\d) for patch', errlines[1])
        assert m2 is not None
        if password:
            assert re.match('Using password from commandline', errlines[2])

        if file_arg:
            output = self.file
        else:
            output = "OMERO%s__%s.sql" % (m1.group(1), m2.group(1))

        with open(output) as f:
            lines = f.readlines()
            for line in lines:
                if line.startswith('insert into password values (0'):
                    assert line.strip() == self.script_output(no_salt)
        if not file_arg:
            os.remove(output)

    @pytest.mark.skipif(OMERODIR is False, reason="Needs omero.db.profile")
    @pytest.mark.parametrize('file_arg', ['', '-f', '--file'])
    @pytest.mark.parametrize('no_salt', ['', '--no-salt'])
    @pytest.mark.parametrize('pos_args', [
        '%s %s %s', '--version %s --patch %s --password %s'])
    def testScriptDeveloperArgs(self, pos_args, no_salt, file_arg, capsys):
        """
        Deprecated and developer usage of db script
        """
        arg_values = ('VERSION', 'PATCH', 'PASSWORD')
        args = "db script " + pos_args % arg_values
        if no_salt:
            args += " %s" % no_salt
        if file_arg:
            args += " %s %s" % (file_arg, str(self.file))
        self.mox.ReplayAll()

        with pytest.raises(NonZeroReturnCode):
            self.cli.invoke(args, strict=True)

        out, err = capsys.readouterr()

        assert 'Using %s for version' % (arg_values[0]) in err
        assert 'Using %s for patch' % (arg_values[1]) in err
        assert 'Using password from commandline' in err
        assert 'Invalid Database version/patch' in err

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

    def password_output(self, user_id, no_salt):
        update_msg = "UPDATE password SET hash = \'%s\'" \
            " WHERE experimenter_id = %s;"
        if not user_id:
            user_id = "0"
        return update_msg % (hash_map[(user_id, no_salt)], user_id)

    def script_output(self, no_salt):
        root_password_msg = "insert into password values (0,\'%s\');"
        return root_password_msg % (hash_map[("0", no_salt)])
