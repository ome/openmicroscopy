#!/usr/bin/env python
# -*- coding: utf-8 -*-
#
# Copyright (C) 2015 University of Dundee & Open Microscopy Environment.
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
Test download of data.
"""

from io import BytesIO
from PIL import Image
from omero.model import PlateI, WellI, WellSampleI
from omero.rtypes import rstring

import pytest
from django.core.urlresolvers import reverse

from omeroweb.testlib import IWebTest, get


class TestDownload(IWebTest):
    """
    Tests to check download is disabled where specified.
    """

    @pytest.fixture
    def image_well_plate(self):
        """
        Returns a new OMERO Project, linked Dataset and linked Image populated
        by an L{test.integration.library.ITest} instance with required fields
        set.
        """
        plate = PlateI()
        plate.name = rstring(self.uuid())
        plate = self.update.saveAndReturnObject(plate)

        well = WellI()
        well.plate = plate
        well = self.update.saveAndReturnObject(well)

        image = self.new_image(name=self.uuid())

        ws = WellSampleI()
        ws.image = image
        ws.well = well
        well.addWellSample(ws)
        ws = self.update.saveAndReturnObject(ws)
        return plate, well, ws.image

    def test_spw_download(self, image_well_plate):
        """
        Download of an Image that is part of a plate should be disabled,
        and return a 404 response.
        """

        plate, well, image = image_well_plate
        # download archived files
        request_url = reverse('archived_files')
        data = {
            "image": image.id.val
        }
        get(self.django_client, request_url, data, status_code=404)

    def test_orphaned_image_direct_download(self):
        """
        Download of archived files for a non-SPW orphaned Image.
        """

        images = self.import_fake_file()
        image = images[0]
        # download archived files
        request_url = reverse('archived_files',
                              args=[image.id.val])
        get(self.django_client, request_url)

    def test_orphaned_image_download(self):
        """
        Download of archived files for a non-SPW orphaned Image.
        """

        images = self.import_fake_file()
        image = images[0]

        # download archived files
        request_url = reverse('archived_files')
        data = {
            "image": image.id.val
        }
        get(self.django_client, request_url, data)

    def test_image_in_dataset_download(self):
        """
        Download of archived files for a non-SPW Image in Dataset.
        """

        images = self.import_fake_file()
        image = images[0]
        ds = self.make_dataset()
        self.link(ds, image)

        # download archived files
        request_url = reverse('archived_files')
        data = {
            "image": image.id.val
        }
        get(self.django_client, request_url, data)

    def test_image_in_dataset_in_project_download(self):
        """
        Download of archived files for a non-SPW Image in Dataset in Project.
        """

        images = self.import_fake_file()
        image = images[0]
        ds = self.make_dataset()
        pr = self.make_project()

        self.link(pr, ds)
        self.link(ds, image)

        # download archived files
        request_url = reverse('archived_files')
        data = {
            "image": image.id.val
        }
        get(self.django_client, request_url, data)

    def test_well_download(self, image_well_plate):
        """
        Download of archived files for a SPW Well.
        """

        plate, well, image = image_well_plate
        # download archived files
        request_url = reverse('archived_files')
        data = {
            "well": well.id.val
        }
        get(self.django_client, request_url, data, status_code=404)

    def test_attachment_download(self):
        """
        Download of attachment.
        """

        images = self.import_fake_file()
        image = images[0]
        fa = self.make_file_annotation()
        self.link(image, fa)

        # download archived files
        request_url = reverse('download_annotation',
                              args=[fa.id.val])
        get(self.django_client, request_url)


class TestDownloadAsPng(IWebTest):
    """
    Tests to check download of Image(s) as PNG.
    """

    @pytest.mark.parametrize("format", ['jpeg', 'png', 'tif'])
    def test_download_image_as(self, format):
        """Download a single image as png etc and open it."""
        width = 100
        height = 50
        name = "test_download"
        image = self.create_test_image(name=name, size_x=width, size_y=height,
                                       session=self.sf)
        request_url = reverse('web_render_image_download',
                              kwargs={'iid': image.id.val})
        data = {'format': format}
        rsp = get(self.django_client, request_url, data)
        assert rsp.content is not None
        assert rsp.get('Content-Disposition') == \
            "attachment; filename=%s.%s" % (name, format)
        # Open the image and check it is the expected size
        img = Image.open(BytesIO(rsp.content))
        assert img.size == (width, height)

    def test_download_images_as_zip(self, format='png'):
        """Test we can download a zip with multiple images."""
        name = "test_download_zip"
        image1 = self.create_test_image(name=name, session=self.sf)
        image2 = self.create_test_image(name=name, session=self.sf)

        # test download placeholder html
        request_url = reverse('download_placeholder')
        data = {'ids': 'image-%s|image-%s' % (image1.id.val, image2.id.val),
                'format': format}
        rsp = get(self.django_client, request_url, data)
        html = rsp.content.decode('utf-8')
        assert "You have chosen to export 2 images" in html
        assert ("Export_as_%s.zip" % format) in html

        # download zip
        zipName = 'Export_as_%s.zip' % format
        request_url = reverse('download_as')
        data = {'image': [image1.id.val, image2.id.val],
                'zipname': zipName,
                'format': format}
        rsp = get(self.django_client, request_url, data)
        assert rsp.get('Content-Disposition') == \
            "attachment; filename=%s" % zipName
        assert len(rsp.content) > 0
