#!/usr/bin/env python
# -*- coding: utf-8 -*-

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

"""Tests rendering of thumbnails."""

from future import standard_library
from builtins import range
import base64
import json
from omero.model import EllipseI, LengthI, LineI, \
    PointI, PolygonI, PolylineI, RectangleI, RoiI
from omero.model.enums import UnitsLength
from omero.rtypes import rstring, rint, rdouble
from omeroweb.testlib import IWebTest
from omeroweb.testlib import get

from io import BytesIO
import pytest
from django.urls import reverse
try:
    from PIL import Image
except Exception:
    import Image
standard_library.install_aliases()


def rgba_to_int(red, green, blue, alpha=255):
    """Return the color as an Integer in RGBA encoding."""
    return int.from_bytes([red, green, blue, alpha],
                          byteorder='big', signed=True)


class TestThumbnails(IWebTest):
    """Tests loading of thumbnails."""

    @pytest.mark.parametrize("size", [None, 100])
    def test_default_thumb_size(self, size):
        """
        Test that the default size of thumbnails is correct.

        Default size is 96.
        """
        # Create a square image
        iId = self.create_test_image(size_x=125, size_y=125,
                                     session=self.sf).getId().getValue()
        args = [iId]
        if size is not None:
            args.append(size)
        request_url = reverse('webgateway_render_thumbnail', args=args)
        rsp = get(self.django_client, request_url)

        thumb = Image.open(BytesIO(rsp.content))
        # Should be 96 on both sides
        if size is None:
            assert thumb.size == (96, 96)
        else:
            assert thumb.size == (size, size)

    @pytest.mark.parametrize("size", [None, 100])
    def test_base64_thumb(self, size):
        """
        Test base64 encoded retrival of single thumbnail
        """
        # Create a square image
        iid = self.create_test_image(size_x=256, size_y=256,
                                     session=self.sf).id.val
        args = [iid]
        if size is not None:
            args.append(size)
        request_url = reverse('webgateway_render_thumbnail', args=args)
        rsp = get(self.django_client, request_url)
        thumb = json.dumps(
            "data:image/jpeg;base64,%s" %
            base64.b64encode(rsp.content).decode("utf-8"))

        request_url = reverse('webgateway_get_thumbnail_json',
                              args=args)
        b64rsp = get(self.django_client, request_url).content.decode("utf-8")
        assert thumb == b64rsp

    def test_base64_thumb_set(self):
        """
        Test base64 encoded retrival of thumbnails in a batch
        """
        # Create a square image
        images = []
        for i in range(2, 5):
            iid = self.create_test_image(size_x=64*i, size_y=64*i,
                                         session=self.sf).id.val
            images.append(iid)

        expected_thumbs = {}
        for i in images:
            request_url = reverse('webgateway_render_thumbnail',
                                  args=[i])
            rsp = get(self.django_client, request_url)

            expected_thumbs[i] = \
                "data:image/jpeg;base64,%s" % \
                base64.b64encode(rsp.content).decode("utf-8")

        iids = {'id': images}
        request_url = reverse('webgateway_get_thumbnails_json')
        b64rsp = get(self.django_client, request_url, iids).content

        json_data = json.loads(b64rsp)
        for i in images:
            assert json_data[str(i)] == expected_thumbs[i]


class TestRoiThumbnails(IWebTest):

    def shapes(self):
        """Create a bunch of unsaved Shapes."""
        rect = RectangleI()
        rect.x = rdouble(10)
        rect.y = rdouble(20)
        rect.width = rdouble(30)
        rect.height = rdouble(40)
        rect.textValue = rstring("test-Rectangle")
        rect.fillColor = rint(rgba_to_int(255, 255, 255, 255))
        rect.strokeColor = rint(rgba_to_int(255, 255, 0, 255))

        ellipse = EllipseI()
        ellipse.x = rdouble(33)
        ellipse.y = rdouble(44)
        ellipse.radiusX = rdouble(55)
        ellipse.radiusY = rdouble(66)
        ellipse.textValue = rstring("test-Ellipse")

        line = LineI()
        line.x1 = rdouble(20)
        line.x2 = rdouble(30)
        line.y1 = rdouble(40)
        line.y2 = rdouble(50)
        line.textValue = rstring("test-Line")

        point = PointI()
        point.x = rdouble(50)
        point.y = rdouble(50)
        point.textValue = rstring("test-Point")

        polygon = PolygonI()
        polygon.fillColor = rint(rgba_to_int(255, 0, 255, 50))
        polygon.strokeColor = rint(rgba_to_int(255, 255, 0))
        polygon.strokeWidth = LengthI(10, UnitsLength.PIXEL)
        points = "10,20, 50,150, 100,100, 150,75"
        polygon.points = rstring(points)

        polyline = PolylineI()
        polyline.points = rstring(points)

        return [rect, ellipse, line, point, polygon, polyline]

    @pytest.mark.parametrize("theT", [1, 0])
    @pytest.mark.parametrize("theZ", [0, 1])
    def test_roi_thumbnail(self, theT, theZ):
        update_service = self.sf.getUpdateService()
        img = self.create_test_image(size_x=125, size_y=125, size_z=2,
                                     size_t=2, session=self.sf)

        for s in self.shapes():
            if theT is not None:
                s.theT = rint(theT)
            if theZ is not None:
                s.theZ = rint(theZ)
            roi = RoiI()
            roi.addShape(s)
            roi.setImage(img)
            roi = update_service.saveAndReturnObject(roi)
            shape = roi.copyShapes()[0]

            # Test ROI thumbnail...
            request_url = reverse('webgateway_render_roi_thumbnail',
                                  kwargs={'roiId': roi.id.val})
            rsp = get(self.django_client, request_url)
            thumb_bytes = BytesIO(rsp.content)
            try:
                thumb = Image.open(thumb_bytes)
            finally:
                thumb_bytes.close()
            assert thumb.size == (250, 166)

            # and Shape thumbnail...
            request_url = reverse('webgateway_render_shape_thumbnail',
                                  kwargs={'shapeId': shape.id.val})
            rsp = get(self.django_client, request_url)
            thumb_bytes = BytesIO(rsp.content)
            try:
                thumb = Image.open(thumb_bytes)
            finally:
                thumb_bytes.close()
            assert thumb.size == (250, 166)
