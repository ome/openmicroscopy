#!/usr/bin/env python
# -*- coding: utf-8 -*-

"""
   gateway tests - Image Wrapper

   Copyright 2009-2013 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

   pytest fixtures used as defined in conftest.py:
   - gatewaywrapper
   - author_testimg_bad
   - author_testimg_big

"""

import pytest
from cStringIO import StringIO
import omero

try:
    from PIL import Image  # see ticket:2597
except ImportError:
    try:
        import Image  # see ticket:2597
    except ImportError:
        print "Pillow not installed"


class TestImage (object):
    @pytest.fixture(autouse=True)
    def setUp(self, author_testimg):
        self.image = author_testimg

    def testThumbnail(self, author_testimg_bad, author_testimg_big):
        thumb = self.image.getThumbnail()
        tfile = StringIO(thumb)
        thumb = Image.open(tfile)  # Raises if invalid
        thumb.verify()  # Raises if invalid
        assert thumb.format == 'JPEG'
        assert thumb.size == (64, 64)
        thumb = self.image.getThumbnail(96)
        tfile = StringIO(thumb)
        thumb = Image.open(tfile)  # Raises if invalid
        thumb.verify()  # Raises if invalid
        assert thumb.size == (96, 96)
        thumb = self.image.getThumbnail((128, 96))
        tfile = StringIO(thumb)
        thumb = Image.open(tfile)  # Raises if invalid
        thumb.verify()  # Raises if invalid
        assert thumb.size == (128, 96)
        badimage = author_testimg_bad  # no pixels
        assert badimage.getThumbnail() is None
        # Big image (4k x 4k and up) thumb
        bigimage = author_testimg_big
        thumb = bigimage.getThumbnail()
        tfile = StringIO(thumb)
        thumb = Image.open(tfile)  # Raises if invalid
        thumb.verify()  # Raises if invalid
        assert thumb.format == 'JPEG'
        assert thumb.size == (64, 64)

    def testRenderingModels(self):
        # default is color model
        cimg = self.image.renderJpeg(0, 0)
        ifile = StringIO(cimg)
        img = Image.open(ifile)
        extrema = img.getextrema()
        assert extrema[0] != extrema[1] or extrema[0] != extrema[2], \
            'Looks like a greyscale image'
        # Explicitely set the color model
        self.image.setColorRenderingModel()
        assert cimg == self.image.renderJpeg(0, 0)
        # Now for greyscale
        self.image.setGreyscaleRenderingModel()
        ifile = StringIO(self.image.renderJpeg(0, 0))
        img = Image.open(ifile)
        extrema = img.getextrema()
        assert extrema[0] == extrema[1] and extrema[0] == extrema[2], \
            'Looks like a color image'

    def testSplitChannel(self):
        cdims = self.image.splitChannelDims(border=4)
        # Verify border attribute works
        assert self.image.splitChannelDims(border=2)['c']['width'] < \
            cdims['c']['width']
        # Default is color model, we have 2 channels
        assert cdims['c']['gridx'] == 2
        assert cdims['c']['gridy'] == 2
        # Render the view
        ifile = StringIO(self.image.renderSplitChannel(0, 0, border=4))
        img = Image.open(ifile)
        assert img.size[0] == cdims['c']['width']
        assert img.size[1] == cdims['c']['height']
        # Same dance in greyscale
        assert cdims['g']['gridx'] == 2
        assert cdims['g']['gridy'] == 1
        # Render the view
        self.image.setGreyscaleRenderingModel()
        ifile = StringIO(self.image.renderSplitChannel(0, 0, border=4))
        img = Image.open(ifile)
        assert img.size[0] == cdims['g']['width']
        assert img.size[1] == cdims['g']['height']
        # Make really sure the grid calculation works as expected
        g = ((1, 1), (2, 1), (2, 2), (2, 2), (3, 2), (3, 2), (3, 3), (3, 3),
             (3, 3), (4, 3), (4, 3), (4, 3), (4, 4), (4, 4), (4, 4), (4, 4))

        def c_count2():
            return i
        self.image.getSizeC = c_count2
        for i in range(1, len(g)):  # 1..15
            dims = self.image.splitChannelDims()
            assert (dims['g']['gridx'], dims['g']['gridy']) == g[i-1]
            assert (dims['c']['gridx'], dims['c']['gridy']) == g[i]

    def testLinePlots(self, author_testimg_bad):
        """
        Verify requesting lineplots give out images matching size with the
        original.
        """
        # Vertical plot
        gif = StringIO(self.image.renderColLinePlotGif(z=0, t=0, x=1))
        img = Image.open(gif)
        img.verify()  # Raises if invalid
        assert img.format == 'GIF'
        assert img.size == (self.image.getSizeX(), self.image.getSizeY())
        # Horizontal plot
        gif = StringIO(self.image.renderRowLinePlotGif(z=0, t=0, y=1))
        img = Image.open(gif)
        img.verify()  # Raises if invalid
        assert img.format == 'GIF'
        assert img.size == (self.image.getSizeX(), self.image.getSizeY())
        badimage = author_testimg_bad  # no pixels
        assert badimage.getCol(z=0, t=0, x=1) is None
        assert badimage.getRow(z=0, t=0, y=1) is None
        assert badimage.renderColLinePlotGif(z=0, t=0, x=1) is None
        assert badimage.renderRowLinePlotGif(z=0, t=0, y=1) is None

    def testProjections(self):
        """ Test image projections """
        for p in self.image.getProjections():
            self.image.setProjection(p)
            ifile = StringIO(self.image.renderJpeg(0, 0))
            img = Image.open(ifile)  # Raises if invalid
            img.verify()  # Raises if invalid
            assert img.format == 'JPEG'
            assert img.size == (self.image.getSizeX(), self.image.getSizeY())

    def testProperties(self, author_testimg_bad):
        """
        Tests the property getters that are not exercised implicitly on other
        tests.
        """
        assert self.image.getZ() == 0
        assert self.image.getT() == 0
        # Make sure methods fail with none if no pixels are found
        assert self.image.getPixelSizeX() is not None
        badimage = author_testimg_bad  # no pixels
        assert badimage.getPixelSizeX() is None
        assert badimage.getChannels() is None

    def assertUnits(self, result, expected):
        """ helper to compare str(Unit enum) with expected units """
        assert result.getValue() == expected[0]
        assert result.getSymbol() == expected[1]
        assert str(result.getUnit()) == expected[2]

    def testPixelSizeUnits(self, author_testimg_generated):
        """
        Tests the pixel sizes and their units
        """
        # Test pre-units behaviour
        sizeXMicrons = 0.10639449954032898
        assert self.image.getPixelSizeX() == sizeXMicrons
        self.assertUnits(self.image.getPixelSizeX(units=True),
                         (sizeXMicrons, "µm", "MICROMETER"))
        self.assertUnits(self.image.getPixelSizeX(units="NANOMETER"),
                         (sizeXMicrons * 1000, "nm", "NANOMETER"))
        self.assertUnits(self.image.getPixelSizeX(units="ANGSTROM"),
                         (sizeXMicrons * 10000, "Å", "ANGSTROM"))
        # Can't convert from Length to Seconds
        with pytest.raises(AttributeError):
            self.image.getPixelSizeX(units="S")
        # return None if no data
        assert author_testimg_generated.getPixelSizeX() is None
        assert author_testimg_generated.getPixelSizeX(units=True) is None
        assert author_testimg_generated.getPixelSizeX(
            units="NANOMETER") is None

    def testUnitsGetValue(self):
        """
        Tests that methods which return Units won't break the
        pre-units 5.0 API for Blitz Gateway (in the same way that
        this is NOT broken for omero.model objects).
        For 5.0 and 5.1, getValue() can be used to get the result.
        """
        sizeXMicrons = 0.10639449954032898
        pixels = self.image.getPrimaryPixels()
        # omero.model.pixels getPhysicalSizeX returns UnitsLengthI
        # getValue() works with 5.0 and 5.1
        sizeX = pixels._obj.getPhysicalSizeX().getValue()
        assert sizeX == sizeXMicrons
        # PixelsWrapper getPhysicalSizeX should also return UnitsLengthI
        sizeX = pixels.getPhysicalSizeX().getValue()
        assert sizeX == sizeXMicrons

    def testChannelWavelengthUnits(self, author_testimg_generated):
        """
        Tests Channel excitation / emmisssion wavelengths and units
        """
        wavelengths = [[360.0, 457.0], [490.0, 528.0]]
        for ch, waves in zip(self.image.getChannels(), wavelengths):
            assert ch.getExcitationWave() == waves[0]
            assert ch.getEmissionWave() == waves[1]
            self.assertUnits(ch.getExcitationWave(units=True),
                             (waves[0], "nm", "NANOMETER"))
            self.assertUnits(ch.getEmissionWave(units=True),
                             (waves[1], "nm", "NANOMETER"))
            self.assertUnits(ch.getExcitationWave(units="ANGSTROM"),
                             (waves[0] * 10, "Å", "ANGSTROM"))
            self.assertUnits(ch.getEmissionWave(units="ANGSTROM"),
                             (waves[1] * 10, "Å", "ANGSTROM"))
        # Check we get None for image with no data
        for ch, waves in zip(
                author_testimg_generated.getChannels(), wavelengths):
            assert ch.getExcitationWave() is None
            assert ch.getExcitationWave(units=True) is None
            assert ch.getExcitationWave(units="ANGSTROM") is None

    def testExposureTimeUnits(self):
        """
        Tests PlaneInfo ExposureTimes and units
        """
        eTime = 0.33500000834465027
        for theC in range(self.image.getSizeC()):
            pInfo = self.image.getPrimaryPixels().copyPlaneInfo(theC=0, theZ=0)
            for pi in pInfo:
                assert pi.getExposureTime() == eTime
                self.assertUnits(pi.getExposureTime(units=True),
                                 (eTime, 's', 'SECOND'))
                self.assertUnits(pi.getExposureTime(units="MILLISECOND"),
                                 (eTime * 1000, 'ms', 'MILLISECOND'))

    def testShortname(self):
        """ Test the shortname method """
        name = self.image.name
        l = len(self.image.name)
        assert self.image.shortname(length=l+4, hist=5) == self.image.name
        assert self.image.shortname(length=l-4, hist=5) == self.image.name
        assert self.image.shortname(length=l-5, hist=5) == \
            '...' + self.image.name[-l+5:]
        self.image.name = ''
        assert self.image.shortname(length=20, hist=5) == ''
        self.image.name = name

    def testSimpleMarshal(self, gatewaywrapper):
        """ Test the call to simpleMarhal """
        m = self.image.simpleMarshal()
        assert m['name'] == self.image.getName()
        assert m['description'] == self.image.getDescription()
        assert m['id'] == self.image.getId()
        assert m['type'] == self.image.OMERO_CLASS
        assert m['author'] == gatewaywrapper.AUTHOR.fullname()
        assert 'parents' not in m
        assert 'date' in m
        assert 'tiled' not in m
        assert 'size' not in m
        m = self.image.simpleMarshal(xtra={'tiled': True})
        assert m['name'] == self.image.getName()
        assert m['description'] == self.image.getDescription()
        assert m['id'] == self.image.getId()
        assert m['type'] == self.image.OMERO_CLASS
        assert m['author'] == gatewaywrapper.AUTHOR.fullname()
        assert m['tiled'] is False
        assert m['size'] == {
            'width': self.image.getSizeX(), 'height': self.image.getSizeY()}
        assert 'parents' not in m
        assert 'date' in m
        m = self.image.simpleMarshal(xtra={'tiled': False})
        assert 'tiled' not in m
        parents = map(lambda x: x.simpleMarshal(), self.image.getAncestry())
        m = self.image.simpleMarshal(parents=True)
        assert m['name'] == self.image.getName()
        assert m['description'] == self.image.getDescription()
        assert m['id'] == self.image.getId()
        assert m['type'] == self.image.OMERO_CLASS
        assert m['author'] == gatewaywrapper.AUTHOR.fullname()
        assert 'date' in m
        assert m['parents'] == parents

    def testExport(self, gatewaywrapper):
        """ Test exporting the image to ometiff """
        assert len(self.image.exportOmeTiff()) > 0
        # if we pass a bufsize we should get a generator back
        size, gen = self.image.exportOmeTiff(bufsize=16)
        assert hasattr(gen, 'next')
        assert len(gen.next()) == 16
        del gen
        # Now try the same using a different user, admin first
        gatewaywrapper.loginAsAdmin()
        gatewaywrapper.gateway.SERVICE_OPTS.setOmeroGroup('-1')
        image = gatewaywrapper.getTestImage()
        assert image.getId() == self.image.getId()
        assert len(image.exportOmeTiff()) > 0
        # what about a regular user?
        g = image.getDetails().getGroup()._obj
        gatewaywrapper.loginAsUser()
        uid = gatewaywrapper.gateway.getUserId()
        gatewaywrapper.loginAsAdmin()
        admin = gatewaywrapper.gateway.getAdminService()
        admin.addGroups(omero.model.ExperimenterI(uid, False), [g])
        gatewaywrapper.loginAsUser()
        try:
            gatewaywrapper.gateway.SERVICE_OPTS.setOmeroGroup('-1')
            image = gatewaywrapper.getTestImage()
            assert image.getId() == self.image.getId()
            assert len(image.exportOmeTiff()) > 0
        finally:
            gatewaywrapper.loginAsAdmin()
            admin = gatewaywrapper.gateway.getAdminService()
