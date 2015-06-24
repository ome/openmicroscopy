#!/usr/bin/env python
# -*- coding: utf-8 -*-

"""
   gateway tests - Rendering Defaults

   Copyright 2009-2013 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

   pytest fixtures used as defined in conftest.py:
   - gatewaywrapper
   - author_testimg_tiny
   - author_testimg_tiny2

"""

import pytest

from cStringIO import StringIO
from PIL import Image, ImageChops


class TestRDefs (object):
    @pytest.fixture(autouse=True)
    def setUp(self, author_testimg):
        self.image = author_testimg
        assert self.image is not None, 'No test image found on database'
        self.TESTIMG_ID = self.image.getId()
        self.channels = self.image.getChannels()
        assert len(self.channels) == 2, 'bad channel count on image #%d' \
            % self.TESTIMG_ID
        self.c0color = self.channels[0].getColor().getHtml()
        self.c1color = self.channels[1].getColor().getHtml()

    def testDefault(self, gatewaywrapper):
        # Clean potentially customized default
        self.image.clearDefaults()
        self.image = gatewaywrapper.getTestImage()
        c0wmin = self.image.getChannels()[0].getWindowMin()
        # Change the color for the rendering defs
        # For #126, also verify channels, and specifically setting min to 0
        self.channels = self.image.getChannels()
        assert self.c0color != 'F0F000'
        assert self.c1color != '000F0F'
        assert c0wmin != 0
        self.image.setActiveChannels(
            [1, 2], [[0.0, 1631.0], [409.0, 5015.0]], [u'F0F000', u'000F0F'])
        self.channels = self.image.getChannels()
        assert len(self.channels) == 2, 'bad channel count on image #%d' \
            % self.TESTIMG_ID
        assert self.channels[0].getColor().getHtml() == 'F0F000'
        assert self.channels[1].getColor().getHtml() == '000F0F'
        assert self.channels[0].getWindowStart() == 0
        # Save it as default
        assert self.image.saveDefaults(), 'Failed saveDefaults'
        # Verify that it comes back as default
        self.image._re = None
        self.channels = self.image.getChannels()
        assert len(self.channels) == 2, 'bad channel count on image #%d' \
            % self.TESTIMG_ID
        assert self.channels[0].getColor().getHtml() == 'F0F000'
        assert self.channels[1].getColor().getHtml() == '000F0F'
        assert self.channels[0].getWindowStart() == 0
        self.image.clearDefaults()
        self.image = gatewaywrapper.getTestImage()
        self.image.clearDefaults()
        self.channels = self.image.getChannels()
        # Verify we got back to the original state
        assert len(self.channels) == 2, 'bad channel count on image #%d' \
            % self.TESTIMG_ID
        assert self.channels[0].getColor().getHtml() == self.c0color
        assert self.channels[1].getColor().getHtml() == self.c1color
        assert self.channels[0].getWindowMin() == c0wmin
        # Check we can set any channel(s) independently - #8670
        self.image.setActiveChannels([2], [[409.0, 5015.0]], [u'F0F0F0'])
        assert self.channels[1].getColor().getHtml() == 'F0F0F0', \
            "Channel 2 colour should be changed"
        assert self.channels[0].getColor().getHtml() == self.c0color, \
            "Channel 1 colour should NOT be changed"
        assert self.channels[0].isActive() is False, \
            "Channel 1 should be Inactive"
        assert self.channels[1].isActive() is True, \
            "Channel 2 should be Active"

    def testCustomized(self):
        self.image.setActiveChannels(
            [1, 2], [[292.0, 1631.0], [409.0, 5015.0]],
            [u'FF0000', u'0000FF'])
        self.channels = self.image.getChannels()
        assert len(self.channels) == 2, 'bad channel count on image #%d' \
            % self.TESTIMG_ID
        assert self.channels[0].getColor().getHtml() == 'FF0000'
        assert self.channels[1].getColor().getHtml() == '0000FF'

        self.image.setActiveChannels(
            [1, 2], [[292.0, 1631.0], [409.0, 5015.0]],
            [u'F0F000', u'000F0F'])
        self.channels = self.image.getChannels()
        assert len(self.channels) == 2, 'bad channel count on image #%d' \
            % self.TESTIMG_ID
        assert self.channels[0].getColor().getHtml() == 'F0F000'
        assert self.channels[1].getColor().getHtml() == '000F0F'

    def testChannelWindows(self):
        """ Verify getters and setter related to channel window settings """
        for channel in self.channels:
            max = channel.getWindowMax()
            min = channel.getWindowMin()
            start = channel.getWindowStart()
            end = channel.getWindowEnd()
            assert min <= start < end <= max
            channel.setWindowStart(min-1)
            assert channel.getWindowStart() == min-1
            channel.setWindowEnd(max+1)
            assert channel.getWindowEnd() == max+1
            channel.setWindow(start, end)
            assert channel.getWindowStart() == start
            assert channel.getWindowEnd() == end

    def testEmissionWave(self, author_testimg_tiny):
        """ """
        assert self.channels[0].getEmissionWave() == 457
        assert self.channels[1].getEmissionWave() == 528
        # Tiny image does not have emission wave set on the channel, ~should
        # get channel index~
        # not channel index anymore, now get default wavelengths (first is
        # 500)
        tiny = author_testimg_tiny.getChannels()
        assert tiny[0].getEmissionWave() == 500

    def testBatchCopy(self, gatewaywrapper, author_testimg_tiny,
                      author_testimg_tiny2):
        """
        tests that we can copy rendering settings from one image to a set of
        targets
        """
        i1 = author_testimg_tiny
        i1c = i1.getChannels()
        i1gid = i1.getDetails().getGroup().getId()
        i1oid = i1.getOwner().getId()
        i2 = author_testimg_tiny2
        i2c = i2.getChannels()
        i2id = i2.getId()
        t = i1c[0].getWindowStart()
        assert t == i2c[0].getWindowStart()
        try:
            i1c[0].setWindowStart(t+1)
            assert i1c[0].getWindowStart() != i2c[0].getWindowStart()
            i1.saveDefaults()
            i1 = gatewaywrapper.getTinyTestImage()
            i1c = i1.getChannels()
            assert i1c[0].getWindowStart() == t+1

            frompid = i1.getPixelsId()
            toids = [i2.getId()]
            rsettings = gatewaywrapper.gateway.getRenderingSettingsService()
            rv = rsettings.applySettingsToImages(frompid, list(toids))
            err = "FAIL: rsettings.applySettingsToImages(%i, (%i,)) -> %s" \
                % (i1.getId(), i2.getId(), rv)
            assert rv[True] == [i2.getId()], err
            i2 = gatewaywrapper.getTinyTestImage2()
            i2c = i2.getChannels()
            assert i2c[0].getWindowStart() == t+1
            # Change source image back
            i1c[0].setWindowStart(t)
            assert i1c[0].getWindowStart() != i2c[0].getWindowStart()
            i1.saveDefaults()
            i1 = gatewaywrapper.getTinyTestImage()
            i1c = i1.getChannels()
            assert i1c[0].getWindowStart() == t
            # Try the propagation as admin
            gatewaywrapper.loginAsAdmin()
            rsettings = gatewaywrapper.gateway.getRenderingSettingsService()
            gatewaywrapper.gateway.SERVICE_OPTS.setOmeroGroup(str(i1gid))
            gatewaywrapper.gateway.SERVICE_OPTS.setOmeroUser(str(i1oid))
            rv = rsettings.applySettingsToImages(
                frompid, list(toids), gatewaywrapper.gateway.SERVICE_OPTS)
            err = "FAIL: rsettings.applySettingsToImages(%i, (%i,)) -> %s" \
                % (i1.getId(), i2.getId(), rv)
            assert rv[True] == [i2id], err
            i2 = gatewaywrapper.getTinyTestImage2()
            i2c = i2.getChannels()
            assert i2c[0].getWindowStart() == t

        finally:
            gatewaywrapper.loginAsAuthor()
            i1 = gatewaywrapper.getTinyTestImage()
            i1c = i1.getChannels()
            i2 = gatewaywrapper.getTinyTestImage2()
            i2c = i2.getChannels()
            i1c[0].setWindowStart(t)
            i1.saveDefaults()
            i2c[0].setWindowStart(t)
            i2.saveDefaults()

    def testGroupBasedPermissions(self, gatewaywrapper):
        """
        Test that images belonging to experimenters on collaborative rw group
        can be reset and rdef created by admin and then edited by owner of
        image.
        """
        gatewaywrapper.loginAsAuthor()
        aobj = gatewaywrapper.gateway.getUser()._obj
        gatewaywrapper.loginAsAdmin()
        gatewaywrapper.gateway.CONFIG.IMG_RDEFNS = \
            'omeropy.gatewaytest.img_rdefns'
        gatewaywrapper.gateway.SERVICE_OPTS.setOmeroGroup('-1')
        self.image = gatewaywrapper.getTestImage()
        assert self.image.resetRDefs()
        assert self.image.saveDefaults()
        admin = gatewaywrapper.gateway.getAdminService()
        admin.setGroupOwner(self.image.getDetails().getGroup()._obj, aobj)
        gatewaywrapper.loginAsAuthor()
        try:
            gatewaywrapper.gateway.CONFIG.IMG_RDEFNS = \
                'omeropy.gatewaytest.img_rdefns'
            gatewaywrapper.gateway.SERVICE_OPTS.setOmeroGroup('-1')
            self.image = gatewaywrapper.getTestImage()
            assert self.image.resetRDefs()
            assert self.image.saveDefaults()
        finally:
            gatewaywrapper.loginAsAdmin()
            admin = gatewaywrapper.gateway.getAdminService()
            gatewaywrapper.gateway.SERVICE_OPTS.setOmeroGroup('-1')
            self.image = gatewaywrapper.getTestImage()
            admin.unsetGroupOwner(self.image.getDetails().getGroup()._obj,
                                  aobj)

    def testGetRdefs(self, gatewaywrapper):
        """
        Test we can list rdefs for an image and they correspond to the
        rdefs we've set.
        """

        # Admin saves Rdef (greyscale)
        gatewaywrapper.loginAsAdmin()
        adminId = gatewaywrapper.gateway.getUserId()
        gatewaywrapper.gateway.SERVICE_OPTS.setOmeroGroup('-1')
        self.image = gatewaywrapper.getTestImage()
        self.image.setGreyscaleRenderingModel()
        self.image.saveDefaults()

        # Author saves Rdef (color)
        gatewaywrapper.loginAsAuthor()
        authorId = gatewaywrapper.gateway.getUserId()
        self.image = gatewaywrapper.getTestImage()
        self.image.setColorRenderingModel()
        self.image.saveDefaults()

        rdefs = self.image.getAllRenderingDefs()
        assert len(rdefs) == 2

        adminRdefId = None
        authorRdefId = None
        for r in rdefs:
            if r['owner']['id'] == adminId:
                adminRdefId = r['id']
                assert r['model'] == 'greyscale'
            elif r['owner']['id'] == authorId:
                authorRdefId = r['id']
                assert r['model'] == 'rgb'

        assert adminRdefId is not None
        assert authorRdefId is not None

        # Test getting different thumbnails
        defaultThumb = self.image.getThumbnail()
        authorThumb = self.image.getThumbnail(rdefId=authorRdefId)
        adminThumb = self.image.getThumbnail(rdefId=adminRdefId)
        # convert to PIL images
        defaultThumb = Image.open(StringIO(defaultThumb))
        authorThumb = Image.open(StringIO(authorThumb))
        adminThumb = Image.open(StringIO(adminThumb))

        # Assert that default thumb and author thumb are same
        diff = ImageChops.difference(defaultThumb, authorThumb)
        extrema = diff.convert("L").getextrema()    # min/max of greyscale
        assert extrema == (0, 0)
        # Assert that author thumb and admin thumb are different
        diff = ImageChops.difference(authorThumb, adminThumb)
        extrema = diff.convert("L").getextrema()
        assert extrema != (0, 0)

        # Test thumbnail store init
        tb = self.image._prepareTB(rdefId=adminRdefId)
        assert tb.getRenderingDefId() == adminRdefId
        # Test thumbnail store init
        tb = self.image._prepareTB(rdefId=authorRdefId)
        assert tb.getRenderingDefId() == authorRdefId

    def testResetDefaults(self, gatewaywrapper):
        """
        Test we can resetDefaultSettings with or without saving.
        """
        gatewaywrapper.loginAsAuthor()
        userId = gatewaywrapper.gateway.getUser().getId()
        # Admin creates a new group with user
        gatewaywrapper.loginAsAdmin()
        uuid = gatewaywrapper.gateway.getEventContext().sessionUuid
        gid = gatewaywrapper.gateway.createGroup(
            "testResetDefaults-%s" % uuid, member_Ids=[userId], perms='rw----')

        # login as Author again (into 'default' group)
        gatewaywrapper.loginAsAuthor()
        conn = gatewaywrapper.gateway
        # Try to create image in another group
        conn.SERVICE_OPTS.setOmeroGroup(gid)

        # Author saves Rdef (greyscale)
        image = gatewaywrapper.createTestImage()
        # test image is greyscale initially
        assert image.isGreyscaleRenderingModel()
        image.setColorRenderingModel()
        image.saveDefaults()
        iid = image.getId()

        # resetDefaults without saving - should be greyscale
        image.resetDefaults(save=False)
        assert image.isGreyscaleRenderingModel()

        # retrieve the image again... Should still be color
        image = gatewaywrapper.gateway.getObject("Image", iid)
        assert not image.isGreyscaleRenderingModel()
        # Then reset and Save
        image.resetDefaults()

        # retrieve the image again... Should be greyscale
        image = gatewaywrapper.gateway.getObject("Image", iid)
        assert image.isGreyscaleRenderingModel()
        # Finally save as Color again, so Admin can test reset
        image.setColorRenderingModel()
        image.saveDefaults()

        # Login as Admin and try resetting...
        gatewaywrapper.loginAsAdmin()
        gatewaywrapper.gateway.SERVICE_OPTS.setOmeroGroup('-1')
        i = gatewaywrapper.gateway.getObject("Image", iid)
        # will be color (owner's settings by default)
        assert not i.isGreyscaleRenderingModel()
        # Shouldn't be able to annotate image in Private group
        assert not i.canAnnotate()
        # Try resetting (save=True will be ignored, but reset will work)
        i.resetDefaults(save=True)
        assert i.isGreyscaleRenderingModel()
