#!/usr/bin/env python
# -*- coding: utf-8 -*-

#
# Copyright (C) 2012 University of Dundee & Open Microscopy Environment.
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
#
import pytest
import omero
import omero.clients
from omero.rtypes import rlong, rstring, rdouble
from omeroweb.webgateway.marshal import shapeMarshal


@pytest.fixture(scope='module')
def default_id():
    return TestShapeMarshal.DEFAULT_ID


@pytest.fixture(scope='function')
def basic_line(default_id):
    shape = omero.model.LineI()
    shape.id = rlong(default_id)
    shape.x1 = rdouble(0.0)
    shape.y1 = rdouble(1.0)
    shape.x2 = rdouble(2.0)
    shape.y2 = rdouble(3.0)
    return shape


@pytest.fixture(scope='function')
def basic_arrow(basic_line):
    basic_line.markerStart = rstring('Arrow')
    basic_line.markerEnd = rstring('Arrow')
    return basic_line


@pytest.fixture(scope='function', params=[
    # OME-XML version of the points
    '1,2 2,3 4,5',
    # OMERO.insight version of the points
    'points[1,2 2,3 4,5] points1[1,2 2,3 4,5] '
    'points2[1,2 2,3 4,5] mask[0,0,0]'
])
def basic_polyline(request, default_id):
    points = request.param
    shape = omero.model.PolylineI()
    shape.id = rlong(default_id)
    shape.points = rstring(points)
    return shape


@pytest.fixture(scope='function', params=[
    # OME-XML version of the points
    '1.5,2.5 2,3 4.1,5.1',
    # OMERO.insight version of the points
    'points[1.5,2.5 2,3 4.1,5.1] points1[1.5,2.5 2,3 4.1,5.1] '
    'points2[1.5,2.5 2,3 4.1,5.1] mask[0,0,0]'
])
@pytest.fixture(scope='function')
def float_polyline(request, default_id):
    points = request.param
    shape = omero.model.PolylineI()
    shape.id = rlong(default_id)
    shape.points = rstring(points)
    return shape


@pytest.fixture(scope='function', params=[
    # OME-XML version of the points
    '1,2 2,3 4,5',
    # OMERO.insight version of the points
    'points[1,2 2,3 4,5] points1[1,2 2,3 4,5] '
    'points2[1,2 2,3 4,5] mask[0,0,0]'
])
@pytest.fixture(scope='function')
def basic_polygon(request, default_id):
    points = request.param
    shape = omero.model.PolygonI()
    shape.id = rlong(default_id)
    shape.points = rstring(points)
    return shape


@pytest.fixture(scope='function')
def empty_polygon(default_id):
    shape = omero.model.PolygonI()
    shape.id = rlong(default_id)
    shape.points = rstring('')
    return shape


@pytest.fixture(scope='function')
def basic_point(default_id):
    shape = omero.model.PointI()
    shape.id = rlong(default_id)
    shape.x = rdouble(0.0)
    shape.y = rdouble(.1)
    return shape


@pytest.fixture(scope='function')
def basic_ellipse(default_id):
    shape = omero.model.EllipseI()
    shape.id = rlong(default_id)
    shape.x = rdouble(0.0)
    shape.y = rdouble(.1)
    shape.radiusX = rdouble(1.0)
    shape.radiusY = rdouble(.5)
    return shape


class TestShapeMarshal(object):
    """
    Tests to ensure that OME-XML model and OMERO.insight shape point
    parsing are supported correctly.
    """

    DEFAULT_ID = 1L

    def assert_marshal(self, marshaled, type):
        assert marshaled['type'] == type
        assert marshaled['id'] == self.DEFAULT_ID

    def test_line_marshal(self, basic_line):
        marshaled = shapeMarshal(basic_line)
        self.assert_marshal(marshaled, 'Line')
        assert marshaled['x1'] == 0
        assert marshaled['y1'] == 1
        assert marshaled['x2'] == 2
        assert marshaled['y2'] == 3

    def test_arrow_marshal(self, basic_arrow):
        marshaled = shapeMarshal(basic_arrow)
        self.assert_marshal(marshaled, 'Line')
        assert marshaled['markerStart'] == 'Arrow'
        assert marshaled['markerEnd'] == 'Arrow'

    def test_polyline_marshal(self, basic_polyline):
        marshaled = shapeMarshal(basic_polyline)
        self.assert_marshal(marshaled, 'PolyLine')
        assert 'M 1 2 L 2 3 L 4 5' == marshaled['points']

    def test_polyline_float_marshal(self, float_polyline):
        marshaled = shapeMarshal(float_polyline)
        self.assert_marshal(marshaled, 'PolyLine')
        assert 'M 1.5 2.5 L 2 3 L 4.1 5.1' == marshaled['points']

    def test_polygon_marshal(self, basic_polygon):
        marshaled = shapeMarshal(basic_polygon)
        self.assert_marshal(marshaled, 'Polygon')
        assert 'M 1 2 L 2 3 L 4 5 z' == marshaled['points']

    def test_unrecognised_roi_shape_points_string(self, empty_polygon):
        marshaled = shapeMarshal(empty_polygon)
        assert ' z' == marshaled['points']

    def test_point_marshal(self, basic_point):
        marshaled = shapeMarshal(basic_point)
        self.assert_marshal(marshaled, 'Point')
        assert 0.0 == marshaled['x']
        assert 0.1 == marshaled['y']

    def test_ellipse_marshal(self, basic_ellipse):
        marshaled = shapeMarshal(basic_ellipse)
        self.assert_marshal(marshaled, 'Ellipse')
        assert 0.0 == marshaled['x']
        assert 0.1 == marshaled['y']
        assert 1.0 == marshaled['radiusX']
        assert 0.5 == marshaled['radiusY']
