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
import omero


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
        self.image._closeRE()

    def testDefault(self, gatewaywrapper):
        # Clean potentially customized default
        self.image.clearDefaults()
        self.image._closeRE()
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

        self.channels = self.image.getChannels()
        assert len(self.channels) == 2, 'bad channel count on image #%d' \
            % self.TESTIMG_ID
        assert self.channels[0].getColor().getHtml() == 'F0F000'
        assert self.channels[1].getColor().getHtml() == '000F0F'
        assert self.channels[0].getWindowStart() == 0
        self.image.clearDefaults()
        self.image._closeRE()
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
        self.image._closeRE()
        g = gatewaywrapper.gateway
        assert not g._assert_unregistered("testDefault")

    def testCustomized(self, gatewaywrapper):
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
        self.image._closeRE()
        g = gatewaywrapper.gateway
        assert not g._assert_unregistered("testCustomized")

    def testChannelWindows(self, gatewaywrapper):
        """ Verify getters and setter related to channel window settings """
        self.channels = self.image.getChannels()
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
        self.image._closeRE()
        g = gatewaywrapper.gateway
        assert not g._assert_unregistered("testChannelWindows")

    def testFloatDefaultMinMax(self, gatewaywrapper, author_testimg_32float):
        """ Test the default min/max values for 32bit float images """
        channels = author_testimg_32float.getChannels()
        for channel in channels:
            assert channel.getWindowMin() == -2147483648
            assert channel.getWindowMax() == 2147483647
        author_testimg_32float._closeRE()
        g = gatewaywrapper.gateway
        assert not g._assert_unregistered("testFloatDefaultMinMax")

    def testEmissionWave(self, gatewaywrapper, author_testimg_tiny):
        """ """
        assert self.channels[0].getEmissionWave() == 457
        assert self.channels[1].getEmissionWave() == 528
        # Tiny image does not have emission wave set on the channel, ~should
        # get channel index~
        # not channel index anymore, now get default wavelengths (first is
        # 500)
        tiny = author_testimg_tiny.getChannels()
        assert tiny[0].getEmissionWave() == 500
        author_testimg_tiny._closeRE()
        g = gatewaywrapper.gateway
        assert not g._assert_unregistered("testEmissionWave")

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
            i1._closeRE()
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
            i2._closeRE()
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
            i1._closeRE()
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
            i2._closeRE()
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
            i1._closeRE()
            i2._closeRE()
        g = gatewaywrapper.gateway
        assert not g._assert_unregistered("testBatchCopy")

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
        self.image._closeRE()
        g = gatewaywrapper.gateway
        assert not g._assert_unregistered("testGroupBasedPermissions")

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
        self.image.setActiveChannels([1], colors=['cool.lut'])
        self.image.saveDefaults()
        self.image._closeRE()
        # Author saves Rdef (color)
        gatewaywrapper.loginAsAuthor()
        authorId = gatewaywrapper.gateway.getUserId()
        self.image = gatewaywrapper.getTestImage()
        self.image.setColorRenderingModel()
        self.image.setActiveChannels([1], colors=['FF0000'])
        self.image.saveDefaults()
        self.image._closeRE()
        rdefs = self.image.getAllRenderingDefs()
        assert len(rdefs) == 2

        adminRdefId = None
        authorRdefId = None
        for r in rdefs:
            if r['owner']['id'] == adminId:
                adminRdefId = r['id']
                assert r['model'] == 'greyscale'
                assert r['c'][0]['lut'] == 'cool.lut'
            elif r['owner']['id'] == authorId:
                authorRdefId = r['id']
                assert r['model'] == 'rgb'
                assert 'lut' not in r['c'][0]
                assert r['c'][0]['color'] == 'FF0000'

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
        tb.close()
        # Test thumbnail store init
        tb = self.image._prepareTB(rdefId=authorRdefId)
        assert tb.getRenderingDefId() == authorRdefId
        tb.close()
        g = gatewaywrapper.gateway
        assert not g._assert_unregistered("testGetRdefs")

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
        image._closeRE()
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
        i._closeRE()
        g = gatewaywrapper.gateway
        assert not g._assert_unregistered("testResetDefaults")

    def testQuantizationSettings(self, gatewaywrapper):
        """
        Tests whether quantization settings are properly applied
        """
        self.image = gatewaywrapper.getTestImage()
        channels = self.image.getChannels()

        # change settings looping over all families
        families = self.image.getFamilies().values()
        for fam in families:
            i = 0
            # change and assert for new value per channel
            for c in channels:
                coef = 0.5
                self.image.setQuantizationMap(i, fam.getValue(), coef)
                assert c.getFamily().getValue() == fam.getValue()
                if fam.getValue() == "linear":
                    coef = 1
                assert c.getCoefficient() == coef
                i += 1
        self.image._closeRE()
        g = gatewaywrapper.gateway
        assert not g._assert_unregistered("testQuantizationSettings")

    def testQuantizationSettingsInvalid(self, gatewaywrapper):
        """
        Tests that invalid quantization values throw ApiUsageException
        """
        self.image = gatewaywrapper.getTestImage()
        with pytest.raises(AttributeError):
            self.image.setQuantizationMap(0, None, 1)
        with pytest.raises(AttributeError):
            self.image.setQuantizationMap(0, "no_good_family", 1)
        with pytest.raises(omero.ApiUsageException):
            self.image.setQuantizationMap(0, "polynomial", -0.5)
        self.image._closeRE()
        g = gatewaywrapper.gateway
        assert not g._assert_unregistered("testQuantizationSettings")

    def testQuantizationSettingsBulk(self, gatewaywrapper):
        """
        Tests whether quantization settings are properly applied
        using the 'bulk' method
        """
        self.image = gatewaywrapper.getTestImage()

        test_cases = [{"family": "exponential",
                       "coefficient": 0.3},
                      {"family": "polynomial",
                       "coefficient": 0.1}]
        self.image.setQuantizationMaps(test_cases)
        channels = self.image.getChannels()
        for t, ch in enumerate(test_cases):
            assert ch == {
                "family": channels[t].getFamily().getValue(),
                "coefficient": channels[t].getCoefficient()
            }
        self.image._closeRE()
        g = gatewaywrapper.gateway
        assert not g._assert_unregistered("testQuantizationSettingsBulk")

    def testGetChannelsNoRE(self, gatewaywrapper):
        """
        Tests that the color, window start are not available
        if noRE is True
        """
        self.image = gatewaywrapper.getTestImage()
        channels = self.image.getChannels(noRE=True)
        for c in channels:
            assert c.getColor() is None
            assert c.getWindowStart() is None
        g = gatewaywrapper.gateway
        assert not g._assert_unregistered("testGetChannelsNoRE")

    def testSetActiveChannelsNoRE(self, gatewaywrapper):
        """
        Tests set_active_channels method without rendering engine
        """
        # Clean potentially customized default
        self.image.clearDefaults()
        self.image._closeRE()
        self.image = gatewaywrapper.getTestImage()
        c0wmin = self.image.getChannels()[0].getWindowMin()
        # Change the color for the rendering defs
        # For #126, also verify channels, and specifically setting min to 0
        self.channels = self.image.getChannels()
        assert self.c0color != 'F0F000'
        assert self.c1color != '000F0F'
        assert c0wmin != 0
        self.image._closeRE()
        self.image.set_active_channels(
            [1, 2], [[0.0, 1631.0], [409.0, 5015.0]], [u'F0F000', u'000F0F'],
            noRE=True)
        self.channels = self.image.getChannels()
        assert len(self.channels) == 2, 'bad channel count on image #%d' \
            % self.TESTIMG_ID
        assert self.channels[0].getColor().getHtml() == 'F0F000'
        assert self.channels[1].getColor().getHtml() == '000F0F'
        assert self.channels[0].getWindowStart() == 0
        self.image._closeRE()
        g = gatewaywrapper.gateway
        assert not g._assert_unregistered("testSetActiveChannelsNoRE")

    def testSetActiveChannelsWithRE(self, gatewaywrapper):
        """
        Tests set_active_channels method with rendering engine
        """
        # Clean potentially customized default
        self.image.clearDefaults()
        self.image._closeRE()
        self.image = gatewaywrapper.getTestImage()
        c0wmin = self.image.getChannels()[0].getWindowMin()
        # Change the color for the rendering defs
        # For #126, also verify channels, and specifically setting min to 0
        self.channels = self.image.getChannels()
        assert self.c0color != 'F0F000'
        assert self.c1color != '000F0F'
        assert c0wmin != 0
        self.image.set_active_channels(
            [1, 2], [[0.0, 1631.0], [409.0, 5015.0]], [u'F0F000', u'000F0F'])
        self.channels = self.image.getChannels()
        assert len(self.channels) == 2, 'bad channel count on image #%d' \
            % self.TESTIMG_ID
        assert self.channels[0].getColor().getHtml() == 'F0F000'
        assert self.channels[1].getColor().getHtml() == '000F0F'
        assert self.channels[0].getWindowStart() == 0
        self.image._closeRE()
        g = gatewaywrapper.gateway
        assert not g._assert_unregistered("testSetActiveChannelsWithRE")

    def testUnregisterService(self, gatewaywrapper):
        """
        Tests if the service is unregistered after closing the
        rendering engine
        """
        image = gatewaywrapper.getTestImage()
        self.channels = image.getChannels()
        g = gatewaywrapper.gateway
        count = g._assert_unregistered("testUnregisteredService")
        image._closeRE()
        count_close = g._assert_unregistered("testUnregisteredService")
        assert count_close == (count - 1)
        g = gatewaywrapper.gateway
        assert not g._assert_unregistered("testUnregisterService")

    def testRegisterService(self, gatewaywrapper):
        """
        Tests if the service is registered when opening the
        rendering engine
        """
        g = gatewaywrapper.gateway
        image = gatewaywrapper.getTestImage()
        count = g._assert_unregistered("testRegisterService")
        self.channels = image.getChannels()
        count_after = g._assert_unregistered("testRegisterService")
        assert count_after == (count + 1)
        image._closeRE()
        g = gatewaywrapper.gateway
        assert not g._assert_unregistered("testRegisterService")

    def testCloseRE(self, gatewaywrapper):
        """
        Tests if the rendering engine is closed
        """
        image = gatewaywrapper.getTestImage()
        image.getChannels()
        image._closeRE()
        assert image._re is None
        g = gatewaywrapper.gateway
        assert not g._assert_unregistered("testCloseRE")
