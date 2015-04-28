#!/usr/bin/env python
# -*- coding: utf-8 -*-

#
# Copyright (C) 2013-2015 University of Dundee & Open Microscopy Environment.
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

from omero.model import ImageI
from omero.rtypes import rstring, rint, rtime
from datetime import datetime
from uuid import uuid4


@pytest.fixture()
def image(request, gatewaywrapper):
    """Creates an Image."""
    gatewaywrapper.loginAsAuthor()
    gw = gatewaywrapper.gateway
    update_service = gw.getUpdateService()
    image = ImageI()
    image.name = rstring('an image')
    # 2015-04-21 01:15:00
    image.acquisitionDate = rtime(1429578900000L)
    image_id, = update_service.saveAndReturnIds([image])
    return gw.getObject('Image', image_id)


@pytest.fixture()
def image_no_acquisition_date(request, gatewaywrapper):
    """Creates an Image."""
    gatewaywrapper.loginAsAuthor()
    gw = gatewaywrapper.gateway
    update_service = gw.getUpdateService()
    image = ImageI()
    image.name = rstring('an image')
    image.acquisitionDate = rtime(0L)
    image_id, = update_service.saveAndReturnIds([image])
    return gw.getObject('Image', image_id)


class TestImageWrapper(object):

    def testGetDate(self, gatewaywrapper, image):
        date = image.getDate()
        assert date == datetime.fromtimestamp(1429578900L)

    def testGetDateNoAcquisitionDate(
            self, gatewaywrapper, image_no_acquisition_date):
        date = image_no_acquisition_date.getDate()
        creation_event_date = image_no_acquisition_date.creationEventDate()
        assert date == creation_event_date


    def testSimpleMarshal(self, gatewaywrapper, image):
        marshalled = image.simpleMarshal()
        assert marshalled == {
            'description': '',
            'author': 'Author ',
            'date': 1429578900.0,
            'type': 'Image',
            'id': image.getId(),
            'name': 'an image'
        }
