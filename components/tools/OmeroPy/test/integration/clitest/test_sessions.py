#!/usr/bin/env python
# -*- coding: utf-8 -*-

#
# Copyright (C) 2014 University of Dundee & Open Microscopy Environment.
# All rights reserved.
#
# This program is free software; you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation; either version 2 of the License, or
# (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License along
# with this program; if not, write to the Free Software Foundation, Inc.,
# 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.

from test.integration.clitest.cli import CLITest
from omero.cli import NonZeroReturnCode
from omero.model import Experimenter
import pytest

permissions = ["rw----", "rwr---", "rwra--", "rwrw--"]


class TestSessions(CLITest):

    def setup_method(self, method):
        super(TestSessions, self).setup_method(method)
        self.args += ["sessions"]

    def set_login_args(self, user):
        if isinstance(user, Experimenter):
            user = user.omeName.val
        else:
            user = str(user)

        host = self.root.getProperty("omero.host")
        port = self.root.getProperty("omero.port")
        self.args = ["sessions", "login"]
        self.conn_string = "%s@%s:%s" % (user, host, port)
        self.args += [self.conn_string]

    def login_as(self, user):
        self.set_login_args(user)

    def get_connection_string(self, timetoidle="10 min"):
        ec = self.cli.get_event_context()
        string = 'session %s (%s).' % (ec.sessionUuid, self.conn_string)
        if timetoidle:
            string += ' Idle timeout: %s.' % timetoidle
        else:
            string += ' Idle timeout: 10 min.'
        string += ' Current group: %s\n' % ec.groupName
        return string

    def check_session(self, uuid=None, timetoidle=None, timetolive=0):
        if not uuid:
            uuid = self.cli.get_event_context().sessionUuid
        sess = self.sf.getSessionService().getSession(uuid)
        if timetoidle:
            assert sess.getTimeToIdle().getValue() == timetoidle
        if timetolive:
            assert sess.getTimeToLive().getValue() == timetolive

    # Login subcommand
    # ========================================================================
    @pytest.mark.parametrize("quiet", [True, False])
    @pytest.mark.parametrize("timeout", [None, 300])
    def testLogin(self, capsys, quiet, timeout):
        user = self.new_user()

        # Test basic connection logic using credentials
        self.set_login_args(user)
        self.args += ["-w", user.omeName.val]
        timetoidle = None
        timetoidle_str = "10 min"
        if quiet:
            self.args += ["-q"]
        if timeout:
            self.args += ["--timeout", str(timeout)]
            timetoidle = long(timeout) * 1000
            timetoidle_str = "%.f min" % (float(timeout) / 60)
        self.cli.invoke(self.args, strict=True)

        self.check_session(timetoidle=timetoidle)
        o, e = capsys.readouterr()
        assert not o
        if quiet:
            assert not e
        else:
            assert e == 'Created ' + self.get_connection_string(
                timetoidle=timetoidle_str)

        # Test active session re-usage
        join_args = ["sessions", "login", self.conn_string]
        if quiet:
            join_args += ["-q"]
        self.cli.invoke(join_args, strict=True)

        self.check_session(timetoidle=timetoidle)
        o, e = capsys.readouterr()
        assert not o
        if quiet:
            assert not e
        else:
            assert e == 'Using ' + self.get_connection_string(
                timetoidle=timetoidle_str)

        # Test session joining by session key
        host = self.root.getProperty("omero.host")
        port = self.root.getProperty("omero.port")
        ec = self.cli.get_event_context()
        join_args = ["sessions", "login", "-k", ec.sessionUuid,
                     "%s:%s" % (host, port)]
        if quiet:
            join_args += ["-q"]
        self.cli.invoke(join_args, strict=True)

        self.check_session(timetoidle=timetoidle)
        o, e = capsys.readouterr()
        assert not o
        if quiet:
            assert not e
        else:
            assert e == 'Joined ' + self.get_connection_string(
                timetoidle=timetoidle_str)

    @pytest.mark.parametrize("perms", permissions)
    def testLoginAs(self, perms):
        """Test the login --sudo functionality"""

        group1 = self.new_group(perms=perms)
        group2 = self.new_group(perms=perms)
        user = self.new_user(group1, owner=False)  # Member of two groups
        self.root.sf.getAdminService().addGroups(user, [group2])
        member = self.new_user(group1, owner=False)  # Member of first group
        owner = self.new_user(group1, owner=True)  # Owner of first group
        admin = self.new_user(system=True)  # System administrator

        def check_sudoer(sudoer, login_group="", can_switch=False):
            self.set_login_args(user)
            self.args += ["-C", "--sudo", sudoer.omeName.val]
            self.args += ["-w", sudoer.omeName.val]
            if login_group:
                self.args += ["-g", login_group.name.val]
            else:
                login_group = group1

            if login_group == group1:
                target_group = group2
            else:
                target_group = group1

            try:
                # Check login and test group
                self.cli.invoke(self.args, strict=True)
                ec = self.cli.controls["sessions"].ctx._event_context
                assert ec.userName == user.omeName.val
                assert ec.groupName == login_group.name.val

                # Test switch group
                switch_cmd = ["sessions", "group",
                              "%s" % target_group.name.val]
                if can_switch:
                    self.cli.invoke(switch_cmd, strict=True)
                    ec = self.cli.controls["sessions"].ctx._event_context
                    assert ec.userName == user.omeName.val
                    assert ec.groupName == target_group.name.val
                else:
                    with pytest.raises(NonZeroReturnCode):
                        self.cli.invoke(switch_cmd, strict=True)
            finally:
                self.cli.invoke(["sessions", "logout"], strict=True)

        # Administrator is in the list of sudoers
        check_sudoer(admin, can_switch=True)
        check_sudoer(admin, group1, can_switch=True)
        check_sudoer(admin, group2, can_switch=True)

        # Group owner is in the list of sudoers
        check_sudoer(owner)
        check_sudoer(owner, group1)
        with pytest.raises(NonZeroReturnCode):
            check_sudoer(owner, group2)

        # Other group members are not sudoers
        with pytest.raises(NonZeroReturnCode):
            check_sudoer(member)

    @pytest.mark.parametrize('with_sudo', [True, False])
    @pytest.mark.parametrize('with_group', [True, False])
    def testLoginMultiGroup(self, with_sudo, with_group):
        group1 = self.new_group()
        client, user = self.new_client_and_user(group=group1)
        group2 = self.new_group([user])

        self.set_login_args(user)
        if with_sudo:
            self.args += ["--sudo", "root"]
            self.args += ["-w", self.root.getProperty("omero.rootpass")]
        else:
            self.args += ["-w", user.omeName.val]
        if with_group:
            self.args += ["-g", group2.name.val]
        self.cli.invoke(self.args, strict=True)
        ec = self.cli.get_event_context()
        assert ec.userName == user.omeName.val
        if with_group:
            assert ec.groupName == group2.name.val
        else:
            assert ec.groupName == group1.name.val

    # Group subcommand
    # ========================================================================
    def testGroup(self, capsys):
        group1 = self.new_group()
        client, user = self.new_client_and_user(group=group1)
        group2 = self.new_group([user])

        self.set_login_args(user)
        self.args += ["-w", user.omeName.val]
        self.cli.invoke(self.args, strict=True)
        ec = self.cli.get_event_context()
        assert ec.groupName == group1.name.val

        self.args = ["sessions", "group", group2.name.val]
        self.cli.invoke(self.args, strict=True)
        ec = self.cli.get_event_context()
        assert ec.groupName == group2.name.val

        # List current
        capsys.readouterr()  # Clear
        self.args = ["-q", "sessions", "group"]
        self.cli.invoke(self.args, strict=True)
        o, e = capsys.readouterr()
        assert o == "ExperimenterGroup:%s\n" % group2.id.val

    # Timeout subcommand
    # ========================================================================
    def testTimeout(self, capsys):
        client, user = self.new_client_and_user()

        self.set_login_args(user)
        self.args += ["-q", "-w", user.omeName.val]
        self.cli.invoke(self.args, strict=True)

        self.args = ["-q", "sessions", "timeout"]
        self.cli.invoke(self.args, strict=True)
        o, e = capsys.readouterr()
        assert o == "600.0\n"

        self.args = ["-q", "sessions", "timeout", "300"]
        self.cli.invoke(self.args, strict=True)

        self.args = ["-q", "sessions", "timeout"]
        self.cli.invoke(self.args, strict=True)
        o, e = capsys.readouterr()
        assert o == "300.0\n"

        self.args = ["-q", "sessions", "timeout", "1000000"]
        with pytest.raises(NonZeroReturnCode):
            self.cli.invoke(self.args, strict=True)

    # File subcommand
    # ========================================================================
    def testFile(self):

        self.args += ["file"]
        self.cli.invoke(self.args, strict=True)

    # Key subcommand
    # ========================================================================
    def testKey(self, capsys):

        user = self.new_user()
        self.set_login_args(user)
        self.args += ["-w", user.omeName.val]
        self.cli.invoke(self.args, strict=True)

        self.args = ["sessions", "key"]
        self.cli.invoke(self.args, strict=True)
        o, e = capsys.readouterr()
        assert o == "%s\n" % self.cli.get_event_context().sessionUuid

    # Createtoken subcommand
    # ========================================================================
    @pytest.mark.parametrize('expiration', [None, 1000])
    def testCreateToken(self, capsys, expiration):

        user = self.new_user()
        username = user.omeName.val
        host = self.root.getProperty("omero.host")
        port = self.root.getProperty("omero.port")

        # Token creation requires a password input
        self.args = ["sessions", "createtoken"]
        self.args += ["-s", host, "-p", port, "-u", username, "-w", username]
        if expiration:
            self.args += ["--expiration", "%s" % expiration]
        self.cli.invoke(self.args, strict=True)
        o, e = capsys.readouterr()
        token_key = o.split('\n')[0]

        if expiration:
            timetolive = 1000L * expiration
        else:
            timetolive = 1000L * 3600 * 24 * 30
        self.check_session(token_key, timetoidle=0, timetolive=timetolive)

    def testCreateTokenFailsWithKey(self, capsys):

        # Test basic connection logic using credentials
        self.set_login_args(self.user)
        self.args += ["-w", self.user.omeName.val]
        self.cli.invoke(self.args, strict=True)

        # Token creation requires a password input
        host = self.root.getProperty("omero.host")
        port = self.root.getProperty("omero.port")
        ec = self.cli.get_event_context()

        self.args = ["sessions", "createtoken"]
        self.args += ["-s", host, "-p", port, "-k", ec.sessionUuid]
        with pytest.raises(NonZeroReturnCode):
            self.cli.invoke(self.args, strict=True)

    # who subcommand
    # ========================================================================

    @pytest.mark.parametrize("who", ("user", "root"))
    def testWho(self, who):
        self.args = ["sessions", "login"]
        if who == "user":
            user = self.new_user()
            passwd = user.omeName.val
        else:
            user = "root"
            passwd = self.root.getProperty("omero.rootpass")

        # Login
        self.set_login_args(user)
        self.args += ["-w", passwd]
        self.cli.invoke(self.args, strict=True)

        # Attempt who
        self.args = ["sessions", "who"]
        self.cli.invoke(self.args, strict=True)
