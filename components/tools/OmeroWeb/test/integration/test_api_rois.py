#!/usr/bin/env python
# -*- coding: utf-8 -*-

# Copyright (C) 2017-2021 University of Dundee & Open Microscopy Environment.
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

from omeroweb.testlib import IWebTest, get_json, delete_json
from django.urls import reverse
from omeroweb.api import api_settings
import pytest
from test_api_projects import get_update_service, \
    get_connection
from test_api_images import assert_objects
from omero.model import EllipseI, \
    ImageI, \
    LengthI, \
    LineI, \
    MaskI, \
    PointI, \
    PolygonI, \
    RectangleI, \
    RoiI

from omero.model.enums import UnitsLength
from omero.rtypes import rstring, rint, rdouble
from omero import ValidationException


def rgba_to_int(red, green, blue, alpha=255):
    """Return the color as an Integer in RGBA encoding."""
    return int.from_bytes([red, green, blue, alpha],
                          byteorder='big', signed=True)


def build_url(client, url_name, url_kwargs):
    """Build an absolute url using client response url."""
    response = client.request()
    # http://testserver/webclient/
    webclient_url = response.url
    url = reverse(url_name, kwargs=url_kwargs)
    url = webclient_url.replace('/webclient/', url)
    return url


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
        # Only save theT, not theZ
        rect.theT = rint(0)
        rect.textValue = rstring("test-Rectangle")
        rect.fillColor = rint(rgba_to_int(255, 255, 255, 255))
        rect.strokeColor = rint(rgba_to_int(255, 255, 0, 255))

        # ellipse without saving theZ & theT
        ellipse = EllipseI()
        ellipse.x = rdouble(33)
        ellipse.y = rdouble(44)
        ellipse.radiusX = rdouble(55)
        ellipse.radiusY = rdouble(66)
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

        mask = MaskI()
        mask.setTheC(rint(0))
        mask.setTheZ(rint(0))
        mask.setTheT(rint(0))
        mask.setX(rdouble(100))
        mask.setY(rdouble(100))
        mask.setWidth(rdouble(500))
        mask.setHeight(rdouble(500))
        mask.setTextValue(rstring("test-Mask"))
        mask.setBytes(None)

        return [rect, ellipse, line, point, polygon, mask]

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
        rsp = get_json(client, rois_url)
        assert_objects(conn, rsp['data'], rois, dtype="Roi",
                       opts={'load_shapes': True})

        # ROIs on the image
        rsp = get_json(client, rois_url, {'image': image.id.val})
        assert_objects(conn, rsp['data'], rois[:2], dtype="Roi",
                       opts={'load_shapes': True})

    def test_roi_delete(self, user1, image_rois):
        """Test GET and DELETE of a single ROI."""
        image, rois = image_rois
        conn = get_connection(user1)
        user_name = conn.getUser().getName()
        client = self.new_django_client(user_name, user_name)
        version = api_settings.API_VERSIONS[-1]

        # Before delete, we can read
        roi_id = rois[0].id.val
        roi_url = reverse('api_roi', kwargs={'api_version': version,
                                             'object_id': roi_id})
        rsp = get_json(client, roi_url)
        assert_objects(conn, [rsp['data']], rois[:1], dtype="Roi",
                       opts={'load_shapes': True})
        shape_json = rsp['data']['shapes'][0]
        shape_id = shape_json['@id']
        shape_class = shape_json['@type'].split('#')[1]     # e.g. Ellipse
        shape = conn.getQueryService().get(shape_class, shape_id)
        assert shape.id.val == shape_id

        # Delete
        delete_json(client, roi_url, {})
        # Get should now return 404
        rsp = get_json(client, roi_url, status_code=404)
        assert rsp['message'] == 'Roi %s not found' % roi_id

        # Check that Shape has also been deleted
        with pytest.raises(ValidationException):
            shape = conn.getQueryService().get(shape_class, shape_id)

    def test_shapes(self, user1, image_rois, shapes):
        """Test listing Shapes"""
        image, rois = image_rois
        conn = get_connection(user1)
        user_name = conn.getUser().getName()
        client = self.new_django_client(user_name, user_name)
        version = api_settings.API_VERSIONS[-1]

        # List ALL shapes
        shapes_url = reverse('api_shapes', kwargs={'api_version': version})
        rsp = get_json(client, shapes_url)
        shapes_json = rsp['data']
        shape_ids = [shape['@id'] for shape in shapes_json]
        assert len(shape_ids) == len(shapes)
        assert_objects(conn, shapes_json, shape_ids, dtype="Shape")

        for shape_id in shape_ids:
            # Get a single Shape
            url = reverse('api_shape', kwargs={'api_version': version,
                                               'object_id': shape_id})
            rsp = get_json(client, url, {'image': image.id.val})
            assert_objects(conn, [rsp['data']], [shape_id], dtype="Shape")
