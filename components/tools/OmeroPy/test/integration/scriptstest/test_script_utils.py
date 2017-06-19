#!/usr/bin/env python
# -*- coding: utf-8 -*-
#
# Copyright (C) 2016 University of Dundee & Open Microscopy Environment.
# All rights reserved.
#
# This program is free software: you can redistribute it and/or modify
# it under the terms of the GNU Affero General Public License as
# published by the Free Software Foundation, either version 3 of the
# License, or (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU Affero General Public License for more details.
#
# You should have received a copy of the GNU Affero General Public License
# along with this program.  If not, see <http://www.gnu.org/licenses/>.

"""
   Integration test which checks the various methods from script_utils

"""

import pytest
from omero.testlib import ITest
import omero.util.script_utils as scriptUtil
from omero.gateway import BlitzGateway
import tempfile
import shutil
from os import listdir, remove
from os.path import isfile, join, exists
from numpy import fromfunction, int16, int32, uint8
from omero.util.temp_files import create_path
from omero.model.enums import PixelsTypeint16

try:
    from PIL import Image  # see ticket:2597
except:  # pragma: nocover
    import Image  # see ticket:2597


class TestScriptUtils(ITest):

    def test_split_image(self):
        dir = tempfile.mkdtemp()
        client = self.new_client()
        image = self.create_test_image(100, 100, 1, 2, 1, client.getSession())
        id = image.id.val
        conn = BlitzGateway(client_obj=client)
        image = conn.getObject("Image", id)
        pixels = image.getPrimaryPixels()
        size_z = pixels.getSizeZ()
        size_c = pixels.getSizeC()
        size_t = pixels.getSizeT()
        # split the image into file
        scriptUtil.split_image(client, id, dir)
        files = [f for f in listdir(dir) if isfile(join(dir, f))]
        shutil.rmtree(dir)
        assert size_z*size_c*size_t == len(files)

    def test_numpy_to_image(self):
        client = self.new_client()
        image = self.create_test_image(100, 100, 2, 3, 4, client.getSession())
        conn = BlitzGateway(client_obj=client)
        image = conn.getObject("Image", image.id.val)
        pixels = image.getPrimaryPixels()
        channel_min_max = []
        for c in image.getChannels():
            min_c = c.getWindowMin()
            max_c = c.getWindowMax()
            channel_min_max.append((min_c, max_c))
        z = image.getSizeZ() / 2
        t = 0
        c = 0
        try:
            for min_max in channel_min_max:
                plane = pixels.getPlane(z, c, t)
                i = scriptUtil.numpy_to_image(plane, min_max, int32)
                assert i is not None
                try:
                    # check if the image can be handled.
                    i.load()
                    assert True
                except IOError:
                    assert False
                c += 1
        finally:
            conn.close()

    def test_convert_numpy_array(self):
        client = self.new_client()
        image = self.create_test_image(100, 100, 2, 3, 4, client.getSession())
        # session is closed during teardown
        conn = BlitzGateway(client_obj=client)
        image = conn.getObject("Image",  image.id.val)
        pixels = image.getPrimaryPixels()
        channel_min_max = []
        for c in image.getChannels():
            min_c = c.getWindowMin()
            max_c = c.getWindowMax()
            channel_min_max.append((min_c, max_c))
        z = image.getSizeZ() / 2
        t = 0
        c = 0
        try:
            for min_max in channel_min_max:
                plane = pixels.getPlane(z, c, t)
                i = scriptUtil.convert_numpy_array(plane, min_max, uint8)
                assert i is not None
                c += 1
        finally:
            conn.close()

    @pytest.mark.parametrize('format', ['tiff', 'foo'])
    @pytest.mark.parametrize('is_file', [True, False])
    def test_numpy_save_as_image(self, format, is_file):
        client = self.new_client()
        image = self.create_test_image(100, 100, 2, 3, 4, client.getSession())
        # session is closed during teardown
        conn = BlitzGateway(client_obj=client)
        image = conn.getObject("Image", image.id.val)
        pixels = image.getPrimaryPixels()
        channel_min_max = []
        for c in image.getChannels():
            min_c = c.getWindowMin()
            max_c = c.getWindowMax()
            channel_min_max.append((min_c, max_c))
        z = image.getSizeZ() / 2
        t = 0
        c = 0

        try:
            for min_max in channel_min_max:
                plane = pixels.getPlane(z, c, t)
                suffix = ".%s" % format
                name = None
                if is_file is True:
                    # create a temporary file
                    tf = tempfile.NamedTemporaryFile(mode='r+b', suffix=suffix)
                    name = tf.name
                else:
                    name = "test%s.%s" % (c, format)
                scriptUtil.numpy_save_as_image(plane, min_max, int32, name)
                # try to open the image
                try:
                    Image.open(name)
                    assert format == "tiff"
                    # delete it since to handle case where it is not a tmp file
                    remove(name)
                except IOError:
                    # error expected for "foo"
                    assert format != "tiff"
                    # file should have been deleted
                    assert exists(name) is False
                c += 1
        finally:
            conn.close()

    @pytest.mark.parametrize('mimetype', ['', 'text/x-python'])
    def test_create_file(self, mimetype):
        f = create_path()
        f.write_text("""Test test_create_file %s """ % self.uuid())
        update_service = self.client.sf.getUpdateService()
        of = scriptUtil.create_file(update_service, str(f),
                                    mimetype=mimetype)
        assert of is not None
        assert of.getId().getValue()

    def test_calc_sha1(self):
        f = create_path()
        f.write_text("""Test calc_sha1 %s """ % self.uuid())
        hash_value = scriptUtil.calc_sha1(str(f))
        assert hash_value is not None

    def test_upload_file(self):
        """Test the upload of the file, the creation is tested above"""
        f = create_path()
        f.write_text("""Test test_upload_file %s """ % self.uuid())
        update_service = self.client.sf.getUpdateService()
        of = scriptUtil.create_file(update_service, str(f))
        store = self.client.sf.createRawFileStore()
        try:
            scriptUtil.upload_file(store, of, f)
        finally:
            store.close()

    def test_download_file(self):
        """Test the download of the file, the upload is tested above"""
        f = create_path()
        f.write_text("""Test test_download_file %s """ % self.uuid())
        update_service = self.client.sf.getUpdateService()
        of = scriptUtil.create_file(update_service, str(f))
        store = self.client.sf.createRawFileStore()
        try:
            scriptUtil.upload_file(store, of, f)
            r = scriptUtil.download_file(store, of)
            assert r is not None
        finally:
            store.close()

    def test_get_objects(self):
        client = self.new_client()
        image = self.create_test_image(100, 100, 1, 1, 1, client.getSession())
        conn = BlitzGateway(client_obj=client)
        params = {}
        params["Data_Type"] = "Image"
        params["IDs"] = [image.id.val]
        objects, message = scriptUtil.get_objects(conn, params)
        assert objects[0].id == image.id.val
        assert message is ''
        conn.close()

    def test_download_plane(self):
        """Test the download of the plane"""
        client = self.new_client()
        image = self.create_test_image(100, 100, 1, 1, 1, client.getSession())
        id = image.getId().getValue()
        session = client.getSession()
        query_service = session.getQueryService()
        query_string = "select p from Pixels p join fetch p.image " \
                       "as i join fetch p.pixelsType where i.id='%s'" % id
        pixels = query_service.findByQuery(query_string, None)
        store = client.sf.createRawPixelsStore()
        try:
            store.setPixelsId(pixels.getId().getValue(), True)
            plane = scriptUtil.download_plane(store, pixels, 0, 0, 0)
            assert plane is not None
        finally:
            store.close()

    def test_upload_plane_by_row(self):
        """Test the upload of the plane by row."""
        client = self.new_client()
        # create an image
        channel_list = range(2)
        session = client.getSession()
        pixels_service = session.getPixelsService()
        query_service = session.getQueryService()

        def f1(x, y):
            return y

        def f2(x, y):
            return (x + y) / 2

        def f3(x, y):
            return x

        p_type = PixelsTypeint16
        pixels_type = query_service.findByQuery(
            "from PixelsType as p where p.value='%s'" % p_type, None)
        iid = pixels_service.createImage(100, 100, 1, 1, channel_list,
                                         pixels_type,
                                         "uploaded_image", "test")
        id = iid.getValue()
        query_string = "select p from Pixels p join fetch p.image " \
                       "as i join fetch p.pixelsType where i.id='%s'" % id
        pixels = query_service.findByQuery(query_string, None)
        store = client.sf.createRawPixelsStore()
        try:
            store.setPixelsId(pixels.getId().getValue(), True)
            f_list = [f1, f2, f3]
            for the_c in range(len(channel_list)):
                f = f_list[the_c % len(f_list)]
                plane = fromfunction(f, (100, 100), dtype=int16)
                scriptUtil.upload_plane_by_row(store, plane, 0, the_c, 0)
        finally:
            store.close()

    def test_upload_plane(self):
        """Test the upload of the plane."""
        client = self.new_client()
        # create an image
        channel_list = range(2)
        session = client.getSession()
        pixels_service = session.getPixelsService()
        query_service = session.getQueryService()

        def f1(x, y):
            return y

        def f2(x, y):
            return (x + y) / 2

        def f3(x, y):
            return x

        p_type = PixelsTypeint16
        pixels_type = query_service.findByQuery(
            "from PixelsType as p where p.value='%s'" % p_type, None)
        iid = pixels_service.createImage(100, 100, 1, 1, channel_list,
                                         pixels_type,
                                         "uploaded_image", "test")
        id = iid.getValue()
        query_string = "select p from Pixels p join fetch p.image " \
                       "as i join fetch p.pixelsType where i.id='%s'" % id
        pixels = query_service.findByQuery(query_string, None)
        store = client.sf.createRawPixelsStore()
        try:
            store.setPixelsId(pixels.getId().getValue(), True)
            f_list = [f1, f2, f3]
            for the_c in range(len(channel_list)):
                f = f_list[the_c % len(f_list)]
                plane = fromfunction(f, (100, 100), dtype=int16)
                scriptUtil.upload_plane(store, plane, 0, the_c, 0)
        finally:
            store.close()
