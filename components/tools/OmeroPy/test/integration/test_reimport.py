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
import test.integration.library as lib

from omero.callbacks import CmdCallbackI
from omero.cmd import Delete
from omero.model import OriginalFileI
from omero.model import PixelsI
from omero.model import PixelsOriginalFileMapI
from omero.rtypes import rint
from omero.rtypes import unwrap
from omero.sys import ParametersI
from omero.util.temp_files import create_path

# Module level marker
pytestmark = pytest.mark.fs_suite


class TestReimportAttachedFiles(lib.ITest):

    def setup_method(self, method):
        super(TestReimportAttachedFiles, self).setup_method(method)
        self.pixels = self.client.sf.getPixelsService()
        self.query = self.client.sf.getQueryService()
        self.update = self.client.sf.getUpdateService()

    def duplicateMIF(self, orig_img):
        """
        Use copyAndResizeImage to create a "synthetic" image
        (one without a fileset)
        """
        new_img = self.pixels.copyAndResizeImage(
            orig_img, rint(16), rint(16), rint(1), rint(1),
            [0], None, True).val
        pix_id = unwrap(self.query.projection(
            "select p.id from Image i join i.pixels p where i.id = :id",
            ParametersI().addId(new_img)))[0][0]
        new_pix = PixelsI(pix_id, False)
        return new_img, new_pix

    def copyPixels(self, orig_img, new_img):
        orig_source = self.client.sf.createRawPixelsStore()
        new_sink = self.client.sf.createRawPixelsStore()

        try:
            orig_source.setPixelsId(orig_img, False)
            new_sink.setPixelsId(new_img, False)
            buf = orig_source.getPlane(0, 0, 0)
            new_sink.setPlane(buf, 0, 0, 0)
        finally:
            orig_source.close()
            new_sink.close()

    def copyFiles(self, orig_img, new_img, new_pix):
        # Then attach a copy of each of the used files in the fileset
        # to the synthetic image
        params = ParametersI()
        params.addId(orig_img)
        file_ids = unwrap(self.query.projection((
            "select f.id from Image i join i.fileset fs join fs.usedFiles uf "
            "join uf.originalFile f where i.id = :id"), params))
        for row in file_ids:
            file_id = row[0]
            target = create_path()
            src = OriginalFileI(file_id, False)
            self.client.download(ofile=src, filename=str(target))
            copy = self.client.upload(filename=str(target))
            link = PixelsOriginalFileMapI()
            link.parent = copy.proxy()
            link.child = new_pix
            self.update.saveObject(link)

    def createSynthetic(self):
        """ Create a image with archived files (i.e. pre-FS) """

        # Produce an FS image as our template
        orig_img = self.importMIF(name="reimport", sizeX=16, sizeY=16)
        orig_img = orig_img[0].id.val

        try:
            new_img, new_pix = self.duplicateMIF(orig_img)
            self.copyPixels(orig_img, new_img)
            self.copyFiles(orig_img, new_img, new_pix)
            return new_img
        finally:
            delete = Delete("/Image", orig_img)
            handle = self.client.sf.submit(delete)
            cb = CmdCallbackI(self.client, handle)
            try:
                cb.loop(50, 1000)
            finally:
                cb.close(True)

    def attachedFiles(self, img):
        files = \
            self.client.sf.getQueryService().project((
                "select o.id from Image i join i.pixels p "
                "join p.originalFileMap m join m.parent o "
                "where i.id = :id"), ParametersI().addId(img))
        return [x[0] for x in unwrap(files)]

    @pytestmark
    def testConvertSynthetic(self):
        """ Convert a pre-FS file to FS """

        new_img = self.createSynthetic()
        files = self.attached_Files(new_img)
        new_fs = self.uploadFileset(files)
        # Now link image to fileset.
        # How to delete Pixel files?!
