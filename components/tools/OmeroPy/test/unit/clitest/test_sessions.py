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

    def testStoreSessionDir(self, tmpdir, monkeypatch):
        from argparse import Namespace
        from omero.util import get_user_dir
        from path import path

        # Default store sessions dir is under user dir
        store = self.cli.controls['sessions'].store(None)
        assert store.dir == path(get_user_dir()) / 'omero' / 'sessions'

        # args.session_dir sets the sessions dir
        args = Namespace()
        args.session_dir = tmpdir / 'session_dir'
        store = self.cli.controls['sessions'].store(args)
        assert store.dir == path(args.session_dir)

        # OMERO_SESSION_DIR with no args.session_dir sets the sessions dir
        monkeypatch.setenv("OMERO_SESSION_DIR", tmpdir / 'envvar')
        store = self.cli.controls['sessions'].store(None)
        assert store.dir == path(tmpdir) / 'envvar'

        # args.session_dir overrides OMERO_SESSION_DIR
        store = self.cli.controls['sessions'].store(args)
        assert store.dir == path(args.session_dir)
