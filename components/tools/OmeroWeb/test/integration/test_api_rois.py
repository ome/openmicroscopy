#!/usr/bin/env python
# -*- coding: utf-8 -*-

# Copyright (C) 2017 University of Dundee & Open Microscopy Environment.
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

"""Tests querying of ROIs and Shapes with json api."""

from omeroweb.testlib import IWebTest, _get_response_json
from django.core.urlresolvers import reverse
from omeroweb.api import api_settings
import pytest
from test_api_projects import get_update_service, \
    get_connection
from test_api_images import assert_objects
from omero.model import EllipseI, \
    ImageI, \
    LengthI, \
    LineI, \
    PointI, \
    PolygonI, \
    RectangleI, \
    RoiI

from omero.model.enums import UnitsLength
from omero.rtypes import rstring, rint, rdouble


def build_url(client, url_name, url_kwargs):
    """Build an absolute url using client response url."""
    response = client.request()
    # http://testserver/webclient/
    webclient_url = response.url
    url = reverse(url_name, kwargs=url_kwargs)
    url = webclient_url.replace('/webclient/', url)
    return url


def rgba_to_int(red, green, blue, alpha=255):
    """Return the color as an Integer in RGBA encoding."""
    r = red << 24
    g = green << 16
    b = blue << 8
    a = alpha
    rgba_int = r+g+b+a
    if (rgba_int > (2**31-1)):       # convert to signed 32-bit int
        rgba_int = rgba_int - 2**32
    return rgba_int


class TestContainers(IWebTest):
    """Tests querying & editing Datasets, Screens etc."""

    @pytest.fixture()
    def user1(self):
        """Return a new user in a read-annotate group."""
        group = self.new_group(perms='rwra--')
        user = self.new_client_and_user(group=group)
        return user

    @pytest.fixture()
    def shapes(self):
        """Create a bunch of unsaved Shapes."""
        rect = RectangleI()
        rect.x = rdouble(10)
        rect.y = rdouble(20)
        rect.width = rdouble(30)
        rect.height = rdouble(40)
        rect.theZ = rint(0)
        rect.theT = rint(0)
        rect.textValue = rstring("test-Rectangle")
        rect.fillColor = rint(rgba_to_int(255, 255, 255, 255))
        rect.strokeColor = rint(rgba_to_int(255, 255, 0, 255))

        ellipse = EllipseI()
        ellipse.x = rdouble(33)
        ellipse.y = rdouble(44)
        ellipse.radiusX = rdouble(55)
        ellipse.radiusY = rdouble(66)
        ellipse.theZ = rint(1)
        ellipse.theT = rint(20)
        ellipse.textValue = rstring("test-Ellipse")

        line = LineI()
        line.x1 = rdouble(200)
        line.x2 = rdouble(300)
        line.y1 = rdouble(400)
        line.y2 = rdouble(500)
        line.textValue = rstring("test-Line")

        point = PointI()
        point.x = rdouble(1)
        point.y = rdouble(1)
        point.theZ = rint(1)
        point.theT = rint(1)
        point.textValue = rstring("test-Point")

        polygon = PolygonI()
        polygon.theZ = rint(5)
        polygon.theT = rint(5)
        polygon.fillColor = rint(rgba_to_int(255, 0, 255, 50))
        polygon.strokeColor = rint(rgba_to_int(255, 255, 0))
        polygon.strokeWidth = LengthI(10, UnitsLength.PIXEL)
        points = "10,20, 50,150, 200,200, 250,75"
        polygon.points = rstring(points)

        return [rect, ellipse, line, point, polygon]

    @pytest.fixture()
    def image_rois(self, user1, shapes):
        """Return Image with ROIs."""
        image = ImageI()
        image.name = rstring('Image for ROIs')
        image = get_update_service(user1).saveAndReturnObject(image)

        # ROI with all but one shapes
        rois = []
        roi = RoiI()
        for shape in shapes[:-1]:
            roi.addShape(shape)
        roi.setImage(image)
        rois.append(roi)

        # roi without shapes
        roi = RoiI()
        roi.setImage(image)
        rois.append(roi)

        # roi without image
        roi = RoiI()
        roi.addShape(shapes[-1])
        rois.append(roi)

        rois = get_update_service(user1).saveAndReturnArray(rois)
        rois.sort(key=lambda x: x.id.val)
        return image, rois

    def test_image_rois(self, user1, image_rois):
        """Test listing ROIs and filtering by Image."""
        image, rois = image_rois
        conn = get_connection(user1)
        user_name = conn.getUser().getName()
        client = self.new_django_client(user_name, user_name)
        version = api_settings.API_VERSIONS[-1]

        # List ALL rois
        rois_url = reverse('api_rois', kwargs={'api_version': version})
        rsp = _get_response_json(client, rois_url, {})
        assert_objects(conn, rsp['data'], rois, dtype="Roi",
                       opts={'load_shapes': True})

        # ROIs on the image
        rsp = _get_response_json(client, rois_url, {'image': image.id.val})
        assert_objects(conn, rsp['data'], rois[:2], dtype="Roi",
                       opts={'load_shapes': True})
