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
from omero.constants.namespaces import NSFSRENAME
from omero.plugins.fs import contents
from omero.plugins.fs import prep_directory
from omero.plugins.fs import rename_fileset
from omero.sys import ParametersI
from omero import SecurityViolation


# Module level marker
pytestmark = pytest.mark.fs_suite


class TestRename(AbstractRepoTest):

    def setup_method(self, method):
        self.pixels = self.client.sf.getPixelsService()
        self.query = self.client.sf.getQueryService()
        self.update = self.client.sf.getUpdateService()
        self.mrepo = self.client.getManagedRepository()

    def assert_rename(self, fileset, new_dir,
                      client=None, mrepo=None, ctx=None):
        """
        Change the path entry for the files contained
        in the orig_dir and then verify that they will
        only be listed as belonging to the new_dir. The
        files on disk ARE NOT MOVED.
        """
        if client is None:
            client = self.client
        if mrepo is None:
            mrepo = self.mrepo

        orig_dir = fileset.templatePrefix.val

        # Before the move the new location should be empty
        assert 3 == len(list(contents(mrepo, orig_dir, ctx)))
        assert 0 == len(list(contents(mrepo, new_dir, ctx)))

        rv = rename_fileset(client, mrepo, fileset, new_dir, ctx=ctx)

        # After the move, the old location should be empty
        assert 0 == len(list(contents(mrepo, orig_dir, ctx)))
        assert 3 == len(list(contents(mrepo, new_dir, ctx)))

        return rv

    def fake_move(self, tomove):
        """
        This methods uses an admin-only backdoor in order to
        perform the desired move. Sysadmins would move likely
        just perform the move manually via OS commands:

            mv old_dir/* new_dir/

        """
        for source, target in tomove:
            cb = self.raw("mv", [source, target], client=self.root)
            self.assertPasses(cb)

    @pytest.mark.parametrize("data", (
        ("user1", "user1", "rw----", True),
        ("user1", "user2", "rwra--", False),
        ("user1", "user2", "rwrw--", True),
        ("user1", "root", "rwra--", True),
        ("root", "root", "rwra--", True),
    ))
    def test_rename_permissions(self, data):
        owner, renamer, perms, allowed = data
        group = self.new_group(perms=perms)
        clients = {
            "user1": self.new_client(group=group),
            "user2": self.new_client(group=group),
            "root": self.root,
        }
        orig_img = self.importMIF(name="rename",
                                  sizeX=16, sizeY=16,
                                  with_companion=True,
                                  client=clients[owner])[0]
        orig_fs = self.get_fileset([orig_img], clients[owner])

        uid = orig_fs.details.owner.id.val
        gid = orig_fs.details.group.id.val

        client = clients[renamer]
        mrepo = client.getManagedRepository()
        ctx = client.getContext(group=gid)
        if renamer == "root":
            ctx["omero.user"] = str(uid)

        new_dir = prep_directory(client, mrepo)
        try:
            tomove = self.assert_rename(
                orig_fs, new_dir, client=client, mrepo=mrepo, ctx=ctx)
            assert allowed
        except SecurityViolation:
            assert not allowed

        if renamer == "root":
            self.fake_move(tomove)

    def test_rename_annotation(self):
        ns = NSFSRENAME
        mrepo = self.client.getManagedRepository()
        orig_img = self.importMIF(with_companion=True)
        orig_fs = self.get_fileset(orig_img)
        new_dir = prep_directory(self.client, mrepo)
        self.assert_rename(orig_fs, new_dir)
        ann = self.query.projection((
            "select a.id from FilesetAnnotationLink l "
            "join l.child as a where l.parent.id = :id "
            "and a.ns = :ns"),
            ParametersI().addId(orig_fs.id).addString("ns", ns))
        assert ann

    def test_prep_and_delete(self):
        mrepo = self.client.getManagedRepository()
        new_dir = prep_directory(self.client, mrepo)
        tree = list(contents(mrepo, new_dir))
        assert 0 == len(tree)
