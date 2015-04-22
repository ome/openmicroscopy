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


"""
Test of various things under omero.gateway
"""

import Ice
import pytest

from omero.gateway import BlitzGateway, ImageWrapper
from omero.model import ImageI, PixelsI, ExperimenterI, EventI
from omero.rtypes import rstring, rtime, rlong, rint


class MockQueryService(object):

    def findByQuery(self, query, params, _ctx=None):
        experimenter = ExperimenterI()
        experimenter.firstName = rstring('first_name')
        experimenter.lastName = rstring('last_name')
        return experimenter


class MockConnection(object):

    SERVICE_OPTS = dict()

    def getQueryService(self):
        return MockQueryService()

    def getMaxPlaneSize(self):
        return (64, 64)


@pytest.fixture(scope='function')
def wrapped_image():
    image = ImageI()
    image.id = rlong(1L)
    image.description = rstring('description')
    image.name = rstring('name')
    image.acquisitionDate = rtime(1000)  # In milliseconds
    image.details.owner = ExperimenterI(1L, False)
    creation_event = EventI()
    creation_event.time = rtime(2000)  # In milliseconds
    image.details.creationEvent = creation_event
    return ImageWrapper(conn=MockConnection(), obj=image)


class TestBlitzGatewayUnicode(object):
    """
    Tests to ensure that unicode encoding of usernames and passwords are
    performed successfully.  `gateway.connect()` will not even attempt to
    perform a connection and just return `False` if the encoding fails.
    """

    def test_unicode_username(self):
        with pytest.raises(Ice.ConnectionRefusedException):
            gateway = BlitzGateway(
                username=u'ążźćółę', passwd='secret',
                host='localhost', port=65535
            )
            gateway.connect()

    def test_unicode_password(self):
        with pytest.raises(Ice.ConnectionRefusedException):
            gateway = BlitzGateway(
                username='user', passwd=u'ążźćółę',
                host='localhost', port=65535
            )
            gateway.connect()


class TestBlitzGatewayImageWrapper(object):
    """Tests for various methods associated with the `ImageWrapper`."""

    def assert_data(self, data):
        assert data['description'] == 'description'
        assert data['author'] == 'first_name last_name'
        assert data['date'] == 1.0  # In seconds
        assert data['type'] == 'Image'
        assert data['id'] == 1L
        assert data['name'] == 'name'

    def test_simple_marshal(self, wrapped_image):
        self.assert_data(wrapped_image.simpleMarshal())

    def test_simple_marshal_tiled(self, wrapped_image):
        image = wrapped_image._obj
        pixels = PixelsI()
        pixels.sizeX = rint(65)
        pixels.sizeY = rint(65)
        image.addPixels(pixels)
        data = wrapped_image.simpleMarshal(xtra={'tiled': True})
        self.assert_data(data)
        assert data['tiled'] is True

    def test_simple_marshal_not_tiled(self, wrapped_image):
        data = wrapped_image.simpleMarshal(xtra={'tiled': True})
        self.assert_data(data)
        assert data['tiled'] is False
