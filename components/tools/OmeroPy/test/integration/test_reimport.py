#!/usr/bin/env python
# -*- coding: utf-8 -*-

#
# Copyright (C) 2014-2015 Glencoe Software, Inc. All Rights Reserved.
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


import library as lib

from omero.callbacks import CmdCallbackI
from omero.cmd import Delete2
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


class TestReimportArchivedFiles(lib.ITest):

    def setup_method(self, method):
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
        rows = unwrap(self.query.projection((
            "select f.id, f.name from Image i "
            "join i.fileset fs join fs.usedFiles uf "
            "join uf.originalFile f where i.id = :id"), params))
        for row in rows:
            file_id = row[0]
            file_name = row[1]
            target = create_path()
            src = OriginalFileI(file_id, False)
            self.client.download(ofile=src, filename=str(target))
            copy = self.client.upload(filename=str(target),
                                      name=file_name)
            link = PixelsOriginalFileMapI()
            link.parent = copy.proxy()
            link.child = new_pix
            self.update.saveObject(link)

    def delete(self, obj_type, obj):
        delete = Delete2(targetObjects={obj_type: [obj.id.val]})
        return self.submit(delete)

    def submit(self, req):
        import omero
        handle = self.client.sf.submit(req)
        cb = CmdCallbackI(self.client, handle)
        try:
            cb.loop(50, 1000)
            rsp = cb.getResponse()
            if isinstance(rsp, omero.cmd.ERR):
                raise Exception(rsp)
            return rsp
        finally:
            cb.close(True)

    def createSynthetic(self):
        """ Create a image with archived files (i.e. pre-FS) """

        from omero.sys import ParametersI

        # Produce an FS image as our template
        orig_img = self.importMIF(name="reimport", sizeX=16, sizeY=16,
                                  with_companion=True, skip=None)
        orig_img = orig_img[0]
        orig_pix = self.query.findByQuery(
            "select p from Pixels p where p.image.id = :id",
            ParametersI().addId(orig_img.id.val))
        orig_fs = self.query.findByQuery(
            "select f from Image i join i.fileset f where i.id = :id",
            ParametersI().addId(orig_img.id.val))

        try:
            new_img, new_pix = self.duplicateMIF(orig_img)
            self.copyPixels(orig_pix, new_pix)
            self.copyFiles(orig_img, new_img, new_pix)
            return new_img
        finally:
            self.delete("Fileset", orig_fs)

    def archivedFiles(self, img_obj):
        return \
            self.client.sf.getQueryService().findAllByQuery((
                "select o from Image i join i.pixels p "
                "join p.pixelsFileMaps m join m.parent o "
                "where i.id = :id"),
                ParametersI().addId(img_obj.id.val))

    def startUpload(self, files):
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
        return mrepo.importFileset(fs, settings)

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
            "Image", new_img.id.val, {"omero.group": "-1"})
        new_img.setFileset(fs.proxy())
        self.client.sf.getUpdateService().saveObject(new_img)

    def imageBinaries(self, imageId,
                      togglePixels=False,
                      deletePyramid=False):

        import omero
        req = omero.cmd.ManageImageBinaries()
        req.imageId = imageId
        req.togglePixels = togglePixels
        req.deletePyramid = deletePyramid
        return self.submit(req)

    def assertManageImageBinaries(self, rsp, lenArchived=2,
                                  pixelSize=256, archivedSize=0,
                                  pyramidSize=0, thumbnailSize=0,
                                  pixelsPresent=True, pyramidPresent=False):

        assert lenArchived == len(rsp.archivedFiles)
        assert pixelsPresent == rsp.pixelsPresent
        assert pyramidPresent == rsp.pyramidPresent
        assert archivedSize == rsp.archivedSize
        assert pixelSize == rsp.pixelSize
        assert pyramidSize == rsp.pyramidSize
        assert thumbnailSize == rsp.thumbnailSize

    def testConvertSynthetic(self):
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
        readme_obj = self.client.upload(str(readme_path),
                                        name="README.txt")

        new_img = self.createSynthetic()
        binaries = self.imageBinaries(new_img.id.val)
        self.assertManageImageBinaries(binaries)

        files = self.archivedFiles(new_img)
        files.insert(0, readme_obj)
        proc = self.startUpload(files)
        handle = self.uploadFileset(proc, files)
        try:
            fs = handle.getRequest().activity.parent
            self.linkImageToFileset(new_img, fs)
            fs = self.client.sf.getQueryService().findByQuery((
                "select fs from Fileset fs "
                "join fetch fs.usedFiles "
                "where fs.id = :id"), ParametersI().addId(fs.id.val))
            used = fs.copyUsedFiles()
            fs.clearUsedFiles()
            for idx in range(1, 3):  # omit readme
                fs.addFilesetEntry(used[idx])
                used[idx].originalFile.unload()
            self.client.sf.getUpdateService().saveObject(fs)
            for file in files:
                self.delete("OriginalFile", file)
            binaries = self.imageBinaries(new_img.id.val)
            self.assertManageImageBinaries(binaries, lenArchived=0)
        finally:
            handle.close()

        binaries = self.imageBinaries(
            new_img.id.val, togglePixels=True)

        self.assertManageImageBinaries(
            binaries, lenArchived=0, pixelsPresent=False)
