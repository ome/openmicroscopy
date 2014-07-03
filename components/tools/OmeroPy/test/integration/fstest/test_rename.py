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
from collections import namedtuple
from omero.cmd import Delete
from omero.grid import ImportSettings
from omero.model import ChecksumAlgorithmI
from omero.model import FilesetI
from omero.model import FilesetEntryI
from omero.model import UploadJobI
from omero.rtypes import rstring
from omero.rtypes import unwrap
from omero.sys import ParametersI
from omero.util.temp_files import create_path

# Module level marker
pytestmark = pytest.mark.fs_suite

Entry = namedtuple("Entry", ("level", "id", "path", "mimetype"))


class TestRename(AbstractRepoTest):

    def setup_method(self, method):
        super(TestRename, self).setup_method(method)
        self.pixels = self.client.sf.getPixelsService()
        self.query = self.client.sf.getQueryService()
        self.update = self.client.sf.getUpdateService()
        self.mrepo = self.client.getManagedRepository()

    def contents(self, path):
        """
        Yield Entry objects for each return value
        from treeList
        """
        tree = unwrap(self.mrepo.treeList(path))

        def parse(tree, level=0):
            for k, v in tree.items():
                yield Entry(level, v.get("id"),
                            k, v.get("mimetype"))
                if "files" in v:
                    for sub in parse(v.get("files"), level+1):
                        yield sub

        for entry in parse(tree):
            yield entry

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

        tomove = []
        for entry in self.contents(orig_dir):
            ofile = self.query.get("OriginalFile", entry.id)
            if entry.level == 1:
                tomove.append(ofile.path.val + ofile.name.val)
            path = ofile.path.val
            ofile.path = rstring(path.replace(orig_dir, new_dir))
            self.update.saveObject(ofile)

        # After the move, the old location should be empty
        assert 1 == len(list(self.contents(orig_dir)))
        assert 3 == len(list(self.contents(new_dir)))

        return tomove

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

    def prep_directory(self):
        """
        Create an empty FS directory by performing an import and
        then deleting the created fileset.
        """
        fs = FilesetI()
        fs.linkJob(UploadJobI())
        entry = FilesetEntryI()
        entry.clientPath = rstring("README.txt")
        fs.addFilesetEntry(entry)
        settings = ImportSettings()
        settings.checksumAlgorithm = ChecksumAlgorithmI()
        settings.checksumAlgorithm.value = rstring("SHA1-160")
        proc = self.mrepo.importFileset(fs, settings)
        try:

            tmp = create_path()
            prx = proc.getUploader(0)
            try:
                tmp.write_text("THIS IS A PLACEHOLDER")
                hash = self.client.sha1(tmp)
                with open(tmp, "r") as source:
                    self.client.write_stream(source, prx)
            finally:
                prx.close()
            tmp.remove()

            handle = proc.verifyUpload([hash])
            try:
                req = handle.getRequest()
                fs = req.activity.parent
            finally:
                handle.close()

            delete = Delete()
            delete.type = "/Fileset"
            delete.id = fs.id.val
            cb = self.client.submit(delete)
            cb.close(True)

        finally:
            proc.close()

        return fs.templatePrefix

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
        new_dir = self.prep_directory().val
        to_move = self.assert_rename(orig_dir, new_dir)
        self.fake_move(to_move, new_dir)
