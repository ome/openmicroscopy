#!/usr/bin/env python
# -*- coding: utf-8 -*-

#
# Copyright (C) 2014-2015 University of Dundee & Open Microscopy Environment.
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

from omero.cli import CLI
from omero.plugins.sessions import SessionsControl
import pytest


class TestSessions(object):

    def setup_method(self, method):
        self.cli = CLI()
        self.cli.register("sessions", SessionsControl, "TEST")
        self.args = ["sessions"]

    def testHelp(self):
        self.args += ["-h"]
        self.cli.invoke(self.args, strict=True)

    @pytest.mark.parametrize(
        "subcommand", SessionsControl().get_subcommands())
    def testSubcommandHelp(self, subcommand):
        self.args += [subcommand, "-h"]
        self.cli.invoke(self.args, strict=True)

    def testDefaultSessionsDir(self):
        from omero.util import get_user_dir
        from path import path

        # Default store sessions dir is under user dir
        store = self.cli.controls['sessions'].store(None)
        assert store.dir == path(get_user_dir()) / 'omero' / 'sessions'

    @pytest.mark.parametrize('environment', (
        [], ["OMERO_SESSIONS_DIR"], ["OMERO_SESSIONDIR"],
        ["OMERO_SESSIONS_DIR", "OMERO_SESSIONDIR"]))
    @pytest.mark.parametrize('session_args', [None, 'session_dir'])
    def testCustomSessionsDir(
            self, tmpdir, monkeypatch, environment,
            session_args):
        from argparse import Namespace
        from omero.util import get_user_dir
        from path import path

        for envvar in environment:
            monkeypatch.setenv(envvar, tmpdir / envvar)

        # args.session_dir sets the sessions dir
        args = Namespace()
        if session_args:
            setattr(args, session_args, tmpdir / session_args)

        store = self.cli.controls['sessions'].store(args)
        # By order of precedence
        if 'OMERO_SESSIONDIR' in environment:
            assert store.dir == path(tmpdir) / 'OMERO_SESSIONDIR'
        elif 'OMERO_SESSION_DIR' in environment:
            assert store.dir == (
                path(tmpdir) / 'OMERO_SESSIONS_DIR' / 'omero' / 'sessions')
        elif session_args:
            assert store.dir == (
                path(getattr(args, session_args) / 'omero' / 'sessions'))
        else:
            assert store.dir == path(get_user_dir()) / 'omero' / 'sessions'
