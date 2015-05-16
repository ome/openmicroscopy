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
from library import ITest

from omero.model import ImageI, ChannelI, LogicalChannelI, LengthI
from omero.rtypes import rstring, rtime
from datetime import datetime


@pytest.fixture(scope='module')
def itest(request):
    """
    Returns a new L{library.ITest} instance. With attached
    finalizer so that pytest will clean it up.
    """
    class ImageWrapperITest(ITest):
        """
        This class emulates py.test scoping semantics when the xunit style
        is in use.
        """
        pass
    ImageWrapperITest.setup_class()

    def finalizer():
        ImageWrapperITest.teardown_class()
    request.addfinalizer(finalizer)
    return ImageWrapperITest()


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


@pytest.fixture()
def image_channel_factory(itest, gatewaywrapper):

    def make_image_channel(channel):
        gatewaywrapper.loginAsAuthor()
        gw = gatewaywrapper.gateway
        update_service = gw.getUpdateService()
        pixels = itest.pix(client=gw.c)
        pixels.addChannel(channel)
        pixels = update_service.saveAndReturnObject(pixels)
        return gw.getObject('Image', pixels.image.id)
    return make_image_channel



@pytest.fixture()
def labeled_channel(image_channel_factory):
    channel = ChannelI()
    lchannel = LogicalChannelI()
    lchannel.name = rstring('a channel')
    channel.logicalChannel = lchannel
    return image_channel_factory(channel)


@pytest.fixture()
def emissionWave_channel(image_channel_factory):
    channel = ChannelI()
    lchannel = LogicalChannelI()
    lchannel.emissionWave = LengthI(123.0, 'NANOMETER')
    channel.logicalChannel = lchannel
    return image_channel_factory(channel)


@pytest.fixture()
def unlabeled_channel(image_channel_factory):
    channel = ChannelI()
    lchannel = LogicalChannelI()
    channel.logicalChannel = lchannel
    return image_channel_factory(channel)


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

    def testChannelLabel(self, labeled_channel):
        labels = labeled_channel.getChannelLabels()
        channels = labeled_channel.getChannels()
        assert labels
        assert channels
        assert len(labels) == len(channels)

        for channel in channels:
            assert channel.getLabel() in labels

    def testChannelEmissionWaveLabel(self, emissionWave_channel):
        labels = emissionWave_channel.getChannelLabels()
        channels = emissionWave_channel.getChannels()
        assert labels
        assert channels
        assert len(labels) == len(channels)

        for channel in channels:
            assert channel.getLabel() in labels

    def testChannelNoLabel(self, unlabeled_channel):
        labels = unlabeled_channel.getChannelLabels()
        channels = unlabeled_channel.getChannels()
        assert labels
        assert channels
        assert len(labels) == len(channels)

        for channel in channels:
            assert channel.getLabel() in labels
