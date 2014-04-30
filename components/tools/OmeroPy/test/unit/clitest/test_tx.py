#!/usr/bin/env python
# -*- coding: utf-8 -*-

#
# Copyright (C) 2013 Glencoe Software, Inc. All Rights Reserved.
# Use is subject to license terms supplied in LICENSE.txt
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

"""
Test of the omero/plugins/tx.py module
"""

from omero.cli import CLI
from omero.plugins.tx import NewObjectTxAction
from omero.plugins.tx import TxControl
from omero.plugins.tx import TxState
from omero_ext.mox import Mox


class TestNewObjectTxAction(object):

    def test_unknown_class(self):
        self.cli = CLI()
        state = TxState()
        action = NewObjectTxAction(state, ["new", "foo"])
        action.go(self.cli.ctx)

class TestTxControl(object):

    def setup_method(self, method):
        self.cli = CLI()
        self.cli.register("tx", TxControl, "TEST")
        self.mox = Mox()

    def teardown_method(self, method):
        self.mox.UnsetStubs()
        self.mox.VerifyAll()

    def test_simple_usage(self):
        self.cli.invoke("tx new Project name=foo", strict=True)

    def test_here_doc_usage(self):
        pass

    def test_multiline_usage(self):
        pass

    def test_file_usage(self):
        pass
