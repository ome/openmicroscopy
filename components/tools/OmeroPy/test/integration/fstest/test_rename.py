#!/usr/bin/env python
# -*- coding: utf-8 -*-

#
# Copyright (C) 2014 Glencoe Software, Inc. All Rights Reserved.
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


import pytest

from test.integration.test_repository import AbstractRepoTest
from omero.plugins.fs import prep_directory
from omero.plugins.fs import rename_fileset
from omero.sys import ParametersI


# Module level marker
pytestmark = pytest.mark.fs_suite


class TestRename(AbstractRepoTest):

    def setup_method(self, method):
        super(TestRename, self).setup_method(method)
        self.pixels = self.client.sf.getPixelsService()
        self.query = self.client.sf.getQueryService()
        self.update = self.client.sf.getUpdateService()
        self.mrepo = self.client.getManagedRepository()

    def assert_rename(self, orig_dir, new_dir):
        """
        Change the path entry for the files contained
        in the orig_dir and then verify that they will
        only be listed as belonging to the new_dir. The
        files on disk ARE NOT MOVED.
        """

        # Before the move the new location should be empty
        assert 3 == len(list(self.contents(orig_dir)))
        assert 1 == len(list(self.contents(new_dir)))

        rv = rename_fileset(self.client, self.mrepo, orig_dir, new_dir)

        # After the move, the old location should be empty
        assert 1 == len(list(self.contents(orig_dir)))
        assert 3 == len(list(self.contents(new_dir)))

        return rv

    def fake_move(self, to_move, new_dir):
        """
        This methods uses an admin-only backdoor in order to
        perform the desired move. Sysadmins would move likely
        just perform the move manually via OS commands:

            mv old_dir/* new_dir/

        """
        for source in to_move:
            cb = self.raw("mv", [source, new_dir], client=self.root)
            self.assertPasses(cb)

    def test_rename(self):
        orig_img = self.importMIF(name="rename",
                                  sizeX=16, sizeY=16,
                                  with_companion=True)[0]

        orig_fs = self.query.findByQuery((
            "select fs from Fileset fs "
            "join fetch fs.usedFiles uf "
            "join fetch uf.originalFile f "
            "join fs.images img where img.id = :id"
        ), ParametersI().addId(orig_img.id.val))
        orig_dir = orig_fs.templatePrefix.val
        new_dir = prep_directory(self.client, self.mrepo)
        to_move = self.assert_rename(orig_dir, new_dir)
        self.fake_move(to_move, new_dir)
