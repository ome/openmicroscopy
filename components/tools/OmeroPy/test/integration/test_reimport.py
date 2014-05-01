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
from omero.grid import ImportSettings
from omero.model import ChecksumAlgorithmI
from omero.model import FilesetI
from omero.model import FilesetEntryI
from omero.model import ImageI
from omero.model import OriginalFileI
from omero.model import PixelsI
from omero.model import PixelsOriginalFileMapI
from omero.model import UploadJobI
from omero.rtypes import rint
from omero.rtypes import rstring
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
            orig_img.id.val, rint(16), rint(16), rint(1), rint(1),
            [0], None, True).val
        pix_id = unwrap(self.query.projection(
            "select p.id from Image i join i.pixels p where i.id = :id",
            ParametersI().addId(new_img)))[0][0]
        new_img = ImageI(new_img, False)
        new_pix = PixelsI(pix_id, False)
        return new_img, new_pix

    def copyPixels(self, orig_pix, new_pix):
        orig_source = self.client.sf.createRawPixelsStore()
        new_sink = self.client.sf.createRawPixelsStore()

        try:
            orig_source.setPixelsId(orig_pix.id.val, False)
            new_sink.setPixelsId(new_pix.id.val, False)
            buf = orig_source.getPlane(0, 0, 0)
            new_sink.setPlane(buf, 0, 0, 0)
        finally:
            orig_source.close()
            new_sink.close()

    def copyFiles(self, orig_img, new_img, new_pix):
        # Then attach a copy of each of the used files in the fileset
        # to the synthetic image
        params = ParametersI()
        params.addId(orig_img.id.val)
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

        from omero.rtypes import unwrap
        from omero.sys import ParametersI

        # Produce an FS image as our template
        orig_img = self.importMIF(name="reimport", sizeX=16, sizeY=16,
                                  with_companion=True)
        orig_img = orig_img[0]
        orig_pix = self.query.findByQuery(
            "select p from Pixels p where p.image.id = :id",
            ParametersI().addId(orig_img.id.val))

        try:
            new_img, new_pix = self.duplicateMIF(orig_img)
            self.copyPixels(orig_pix, new_pix)
            self.copyFiles(orig_img, new_img, new_pix)
            return new_img
        finally:
            delete = Delete("/Image", orig_img.id.val)
            handle = self.client.sf.submit(delete)
            cb = CmdCallbackI(self.client, handle)
            try:
                cb.loop(50, 1000)
            finally:
                cb.close(True)

    def attachedFiles(self, img_obj):
        return \
            self.client.sf.getQueryService().findAllByQuery((
                "select o from Image i join i.pixels p "
                "join p.pixelsFileMaps m join m.parent o "
                "where i.id = :id"), ParametersI().addId(
                    img_obj.id.val))

    def startUpload(self, files, with_import=False):
        mrepo = self.client.getManagedRepository()
        fs = FilesetI()
        fs.linkJob(UploadJobI())
        for file in files:
            entry = FilesetEntryI()
            entry.clientPath = rstring("%s/%s" % (
                file.path.val, file.name.val
            ))
            fs.addFilesetEntry(entry)
        settings = ImportSettings()
        settings.checksumAlgorithm = ChecksumAlgorithmI()
        settings.checksumAlgorithm.value = rstring("SHA1-160")
        if with_import:
            return mrepo.importFileset(fs, settings)
        else:
            return mrepo.uploadFileset(fs, settings)

    def uploadFileset(self, proc, files):
        tmp = create_path()
        hashes = []
        for idx, file in enumerate(files):
            prx = proc.getUploader(idx)
            self.client.download(file, filename=tmp)
            hashes.append(self.client.sha1(tmp))
            with open(tmp, "r") as source:
                self.client.write_stream(source, prx)
            prx.close()
        tmp.remove()
        return proc.verifyUpload(hashes)

    def linkImageToFileset(self, new_img, fs):
        new_img = self.client.sf.getQueryService().get(
            "Image", new_img.id.val, {"omero.group":"-1"})
        new_img.setFileset(fs.proxy())
        self.client.sf.getUpdateService().saveObject(new_img)

    @pytestmark
    def testCreateSynthetic(self):
        """ Convert a pre-FS file to FS """

        self.createSynthetic()

    @pytestmark
    def testConvertSynthetic(self):
        """ Convert a pre-FS file to FS """

        new_img = self.createSynthetic()
        files = self.attachedFiles(new_img)
        proc = self.startUpload(files)
        handle = self.uploadFileset(proc, files)
        assert not handle
        new_img.setFileset(fs)
        self.client.getUpdateService().saveObject(new_img)
        # How to delete Pixel files?!
        # And original files (later!)

    @pytestmark
    def testConvertSynthetic2(self):
        """
        Convert a pre-FS file to FS
        using a README.txt as the first
        in the list.
        """
        readme_path = create_path()
        readme_path.write_text(
            """
            This file has been inserted into the
            fileset in order to prevent import.
            It can be safely deleted.
            """)
        readme_obj = self.client.upload(readme_path,
                                        name="README.txt")

        new_img = self.createSynthetic()
        files = self.attachedFiles(new_img)
        files.insert(0, readme_obj)
        proc = self.startUpload(files, with_import=True)
        handle = self.uploadFileset(proc, files)
        fs = handle.getRequest().activity.parent
        self.linkImageToFileset(new_img, fs)
        # Make sure files are named ".fake"
        # How to delete Pixel files?!
        # And original files (later!) <-- easy.
