#!/usr/bin/env python
# -*- coding: utf-8 -*-

#
# Copyright (C) 2013 University of Dundee & Open Microscopy Environment.
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

"""
   gateway tests - Testing the gateway image wrapper

   pytest fixtures used as defined in conftest.py:
   - gatewaywrapper

"""

import pytest

from omero.model import ImageI, PixelsI, FilesetI, FilesetEntryI, \
    OriginalFileI, DimensionOrderI, PixelsTypeI, CommentAnnotationI, \
    LongAnnotationI
from omero.rtypes import rstring, rlong, rint, rtime
from uuid import uuid4


def uuid():
    return str(uuid4())


def create_image(image_index):
    image = ImageI()
    image.name = rstring('%s_%d' % (uuid(), image_index))
    image.acquisitionDate = rtime(0)
    pixels = PixelsI()
    pixels.sha1 = rstring('')
    pixels.sizeX = rint(1)
    pixels.sizeY = rint(1)
    pixels.sizeZ = rint(1)
    pixels.sizeC = rint(1)
    pixels.sizeT = rint(1)
    pixels.dimensionOrder = DimensionOrderI(1L, False)  # XYZCT
    pixels.pixelsType = PixelsTypeI(1L, False)  # bit
    image.addPixels(pixels)
    return image


@pytest.fixture()
def images_with_original_files(request, gatewaywrapper):
    """Creates Images with associated OriginalFiles."""
    gatewaywrapper.loginAsAuthor()
    gw = gatewaywrapper.gateway
    update_service = gw.getUpdateService()
    original_files = list()
    for original_file_index in range(2):
        original_file = OriginalFileI()
        original_file.name = rstring(
            'filename_%d.ext' % original_file_index
        )
        original_file.path = rstring('/server/path/')
        original_file.size = rlong(50L)
        original_files.append(original_file)
    images = list()
    for image_index in range(2):
        image = create_image(image_index)
        for original_file in original_files:
            image.getPrimaryPixels().linkOriginalFile(original_file)
        images.append(image)
    image_ids = update_service.saveAndReturnIds(images)
    return [gw.getObject('Image', image_id) for image_id in image_ids]


@pytest.fixture()
def fileset_with_images(request, gatewaywrapper):
    """Creates and returns a Fileset with associated Images."""
    gatewaywrapper.loginAsAuthor()
    update_service = gatewaywrapper.gateway.getUpdateService()
    fileset = FilesetI()
    fileset.templatePrefix = rstring('')
    for image_index in range(2):
        image = create_image(image_index)
        for fileset_index in range(2):
            fileset_entry = FilesetEntryI()
            fileset_entry.clientPath = rstring(
                '/client/path/filename_%d.ext' % fileset_index
            )
            original_file = OriginalFileI()
            original_file.name = rstring('filename_%d.ext' % fileset_index)
            original_file.path = rstring('/server/path/')
            original_file.size = rlong(50L)
            fileset_entry.originalFile = original_file
            fileset.addFilesetEntry(fileset_entry)
        fileset.addImage(image)
    comment_annotation = CommentAnnotationI()
    comment_annotation.ns = rstring('comment_annotation')
    comment_annotation.textValue = rstring('textValue')
    long_annotation = LongAnnotationI()
    long_annotation.ns = rstring('long_annotation')
    long_annotation.longValue = rlong(1L)
    fileset.linkAnnotation(comment_annotation)
    fileset.linkAnnotation(long_annotation)
    fileset = update_service.saveAndReturnObject(fileset)
    return gatewaywrapper.gateway.getObject('Fileset', fileset.id.val)


class TestFileset(object):

    def assertFilesetFilesInfo(self, files_info):
        assert files_info['fileset'] is True
        assert files_info['count'] == 4
        assert files_info['size'] == 200
        assert len(files_info['annotations']) == 2
        for annotation in files_info['annotations']:
            ns = annotation['ns']
            if ns == 'comment_annotation':
                assert annotation['value'] == 'textValue'
            elif ns == 'long_annotation':
                pass
            else:
                pytest.fail('Unexpected namespace: %r' % ns)

    def testCountArchivedFiles(self, gatewaywrapper, fileset_with_images):
        for image in fileset_with_images.copyImages():
            assert image.countArchivedFiles() == 0

    def testCountFilesetFiles(self, gatewaywrapper, fileset_with_images):
        for image in fileset_with_images.copyImages():
            assert image.countFilesetFiles() == 4

    def testCountImportedImageFiles(self, gatewaywrapper, fileset_with_images):
        for image in fileset_with_images.copyImages():
            assert image.countImportedImageFiles() == 4

    def testGetImportedFilesInfo(self, gatewaywrapper, fileset_with_images):
        for image in fileset_with_images.copyImages():
            files_info = image.getImportedFilesInfo()
            self.assertFilesetFilesInfo(files_info)

    def testGetArchivedFiles(self, gatewaywrapper, fileset_with_images):
        for image in fileset_with_images.copyImages():
            len(list(image.getArchivedFiles())) == 4

    def testGetImportedImageFiles(self, gatewaywrapper, fileset_with_images):
        for image in fileset_with_images.copyImages():
            len(list(image.getImportedImageFiles())) == 4

    def testGetArchivedFilesInfo(self, gatewaywrapper, fileset_with_images):
        gw = gatewaywrapper.gateway
        for image in fileset_with_images.copyImages():
            files_info = gw.getArchivedFilesInfo([image.id])
            assert files_info == {'fileset': False, 'count': 0, 'size': 0}

    def testGetFilesetFilesInfo(self, gatewaywrapper, fileset_with_images):
        gw = gatewaywrapper.gateway
        for image in fileset_with_images.copyImages():
            files_info = gw.getFilesetFilesInfo([image.id])
            self.assertFilesetFilesInfo(files_info)

    def testGetFilesetFilesInfoMultiple(
            self, gatewaywrapper, fileset_with_images):
        gw = gatewaywrapper.gateway
        image_ids = [v.id for v in fileset_with_images.copyImages()]
        files_info = gw.getFilesetFilesInfo(image_ids)
        self.assertFilesetFilesInfo(files_info)

    def testGetFileset(self, gatewaywrapper, fileset_with_images):
        for image in fileset_with_images.copyImages():
            assert image.getFileset() is not None

    def testGetImportedImageFiles(self, gatewaywrapper, fileset_with_images):
        for image in fileset_with_images.copyImages():
            files = list(image.getImportedImageFiles())
            assert len(files) == 4

    def testGetImportedImageFilePaths(
            self, gatewaywrapper, fileset_with_images):
        for image in fileset_with_images.copyImages():
            paths = image.getImportedImageFilePaths()
            paths['server_paths'].sort()
            assert paths['server_paths'] == [
                '/server/path/filename_0.ext',
                '/server/path/filename_0.ext',
                '/server/path/filename_1.ext',
                '/server/path/filename_1.ext'
            ]
            paths['client_paths'].sort()
            assert paths['client_paths'] == [
                '/client/path/filename_0.ext',
                '/client/path/filename_0.ext',
                '/client/path/filename_1.ext',
                '/client/path/filename_1.ext'
            ]

class TestArchivedOriginalFiles(object):

    def assertArchivedFilesInfo(self, files_info):
        assert files_info['fileset'] is False
        assert files_info['count'] == 2
        assert files_info['size'] == 100

    def testCountArchivedFiles(
            self, gatewaywrapper, images_with_original_files):
        for image in images_with_original_files:
            assert image.countArchivedFiles() == 2

    def testCountFilesetFiles(
            self, gatewaywrapper, images_with_original_files):
        for image in images_with_original_files:
            assert image.countFilesetFiles() == 0

    def testCountImportedImageFiles(
            self, gatewaywrapper, images_with_original_files):
        for image in images_with_original_files:
            assert image.countImportedImageFiles() == 2

    def testGetImportedFilesInfo(
            self, gatewaywrapper, images_with_original_files):
        for image in images_with_original_files:
            files_info = image.getImportedFilesInfo()
            self.assertArchivedFilesInfo(files_info)

    def testGetArchivedFiles(
            self, gatewaywrapper, images_with_original_files):
        for image in images_with_original_files:
            len(list(image.getArchivedFiles())) == 2

    def testGetImportedImageFiles(
            self, gatewaywrapper, images_with_original_files):
        for image in images_with_original_files:
            len(list(image.getImportedImageFiles())) == 2

    def testGetArchivedFilesInfo(
            self, gatewaywrapper, images_with_original_files):
        gw = gatewaywrapper.gateway
        for image in images_with_original_files:
            files_info = gw.getArchivedFilesInfo([image.id])
            self.assertArchivedFilesInfo(files_info)

    def testGetFilesetFilesInfo(
            self, gatewaywrapper, images_with_original_files):
        gw = gatewaywrapper.gateway
        for image in images_with_original_files:
            files_info = gw.getFilesetFilesInfo([image.id])
            assert files_info == {
                'annotations': list(), 'fileset': True, 'count': 0, 'size': 0
            }

    def testGetFilesetFilesInfoMultiple(
            self, gatewaywrapper, images_with_original_files):
        gw = gatewaywrapper.gateway
        image_ids = [v.id for v in images_with_original_files]
        files_info = gw.getFilesetFilesInfo(image_ids)
        assert files_info == {
            'annotations': list(), 'fileset': True, 'count': 0, 'size': 0
        }

    def testGetFileset(self, gatewaywrapper, images_with_original_files):
        for image in images_with_original_files:
            assert image.getFileset() is None

    def testGetImportedImageFiles(
            self, gatewaywrapper, images_with_original_files):
        for image in images_with_original_files:
            files = list(image.getImportedImageFiles())
            assert len(files) == 2

    def testGetImportedImageFilePaths(
            self, gatewaywrapper, images_with_original_files):
        for image in images_with_original_files:
            paths = image.getImportedImageFilePaths()
            paths['server_paths'].sort()
            assert paths['server_paths'] == [
                '/server/path/filename_0.ext',
                '/server/path/filename_1.ext',
            ]
            assert len(paths['client_paths']) == 0
