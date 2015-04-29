# -*- coding: utf-8 -*-

# Copyright (C) 2015 Glencoe Software, Inc.
# All rights reserved.
#
# Use is subject to license terms supplied in LICENSE.txt

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
