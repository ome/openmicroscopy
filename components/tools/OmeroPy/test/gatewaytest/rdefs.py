#!/usr/bin/env python

"""
   gateway tests - Rendering Defaults

   Copyright 2009 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

import unittest
from types import StringTypes
import omero

import gatewaytest.library as lib

try:
    from django.utils import simplejson
    from django.http import QueryDict
except ImportError:
    pass # Oh well. The tests will fail later.

class RDefsTest (lib.GTest):
    TESTIMG_ID = None

    def setUp (self):
        super(RDefsTest, self).setUp()
        self.loginAsAuthor()
        self.image = self.getTestImage()
        self.assertNotEqual(self.image, None, 'No test image found on database')
        self.TESTIMG_ID = self.image.getId()
        self.channels = self.image.getChannels()
        self.assert_(len(self.channels) == 2, 'bad channel count on image #%d' % self.TESTIMG_ID)
        self.c0color = self.channels[0].getColor().getHtml()
        self.c1color = self.channels[1].getColor().getHtml()

    def tearDown (self):
        super(RDefsTest, self).tearDown()

    def testDefault (self):
        # Clean potentially customized default
        self.image.clearDefaults()
        self.image = self.getTestImage()
        c0wmin = self.image.getChannels()[0].getWindowMin()
        # Change the color for the rendering defs
        # For #126, also verify channels, and specifically setting min to 0
        self.channels = self.image.getChannels()
        self.assertNotEqual(self.c0color, 'F0F000')
        self.assertNotEqual(self.c1color, '000F0F')
        self.assertNotEqual(c0wmin, 0)
        self.image.setActiveChannels([1, 2],[[0.0, 1631.0], [409.0, 5015.0]],[u'F0F000', u'000F0F'])
        self.channels = self.image.getChannels()
        self.assert_(len(self.channels) == 2, 'bad channel count on image #%d' % self.TESTIMG_ID)
        self.assertEqual(self.channels[0].getColor().getHtml(), 'F0F000')
        self.assertEqual(self.channels[1].getColor().getHtml(), '000F0F')
        self.assertEqual(self.channels[0].getWindowStart(), 0)
        # Save it as default
        self.assert_(self.image.saveDefaults(), 'Failed saveDefaults')
        # Verify that it comes back as default
        self.image._re = None
        self.channels = self.image.getChannels()
        self.assert_(len(self.channels) == 2, 'bad channel count on image #%d' % self.TESTIMG_ID)
        self.assertEqual(self.channels[0].getColor().getHtml(), 'F0F000')
        self.assertEqual(self.channels[1].getColor().getHtml(), '000F0F')
        self.assertEqual(self.channels[0].getWindowStart(), 0)
        self.image.clearDefaults()
        self.image = self.getTestImage()
        self.image.clearDefaults()
        self.channels = self.image.getChannels()
        # Verify we got back to the original state
        self.assert_(len(self.channels) == 2, 'bad channel count on image #%d' % self.TESTIMG_ID)
        self.assertEqual(self.channels[0].getColor().getHtml(), self.c0color)
        self.assertEqual(self.channels[1].getColor().getHtml(), self.c1color)
        self.assertEqual(self.channels[0].getWindowMin(), c0wmin)
        ## Check that only author (or admin) can change defaults
        #self.doLogin(GUEST)
        #self.image = self.getTestImage(self.gateway, public=True)
        #self.image.setActiveChannels([1, 2],[[292.0, 1631.0], [409.0, 5015.0]],[u'F0F000', u'000F0F'])
        #self.assert_(not self.image.saveDefaults(), 'saveDefaults should have failed!')
        ## Verify we are still in the original state
        #self.channels = self.image.getChannels()
        #self.assert_(len(self.channels) == 2, 'bad channel count on image #%d' % self.TESTIMG_ID)
        #self.assertEqual(self.channels[0].getColor().getHtml(), self.c0color)
        #self.assertEqual(self.channels[1].getColor().getHtml(), self.c1color)

    def testCustomized (self):
        self.image.setActiveChannels([1, 2],[[292.0, 1631.0], [409.0, 5015.0]],[u'FF0000', u'0000FF'])
        self.channels = self.image.getChannels()
        self.assert_(len(self.channels) == 2, 'bad channel count on image #%d' % self.TESTIMG_ID)
        self.assertEqual(self.channels[0].getColor().getHtml(), 'FF0000')
        self.assertEqual(self.channels[1].getColor().getHtml(), '0000FF')

        self.image.setActiveChannels([1, 2],[[292.0, 1631.0], [409.0, 5015.0]],[u'F0F000', u'000F0F'])
        self.channels = self.image.getChannels()
        self.assert_(len(self.channels) == 2, 'bad channel count on image #%d' % self.TESTIMG_ID)
        self.assertEqual(self.channels[0].getColor().getHtml(), 'F0F000')
        self.assertEqual(self.channels[1].getColor().getHtml(), '000F0F')

    def testChannelWindows (self):
        """ Verify getters and setter related to channel window settings """
        for channel in self.channels:
            max = channel.getWindowMax()
            min = channel.getWindowMin()
            start = channel.getWindowStart()
            end = channel.getWindowEnd()
            self.assert_(min <= start < end <= max)
            channel.setWindowStart(min-1)
            self.assertEqual(channel.getWindowStart(), min-1)
            channel.setWindowEnd(max+1)
            self.assertEqual(channel.getWindowEnd(), max+1)
            channel.setWindow(start, end)
            self.assertEqual(channel.getWindowStart(), start)
            self.assertEqual(channel.getWindowEnd(), end)

    def testEmissionWave (self):
        """ """
        self.assertEqual(self.channels[0].getEmissionWave(), 457)
        self.assertEqual(self.channels[1].getEmissionWave(), 528)
        # Tiny image does not have emission wave set on the channel, ~should get channel index~
        # not channel index anymore, now get default wavelengths (first is 500)
        tiny = self.getTinyTestImage().getChannels()
        self.assertEqual(tiny[0].getEmissionWave(), 500)

    # Disabled. See #6038
    def XtestBatchCopy (self):
        """ tests that we can copy rendering settings from one image to a set of targets """
        #self.loginAsAdmin()
        self.loginAsAuthor()
        i1 = self.getTinyTestImage()
        i1c = i1.getChannels()
        i2 = self.getTinyTestImage2()
        i2c = i2.getChannels()
        t = i1c[0].getWindowStart()
        self.assertEqual(t, i2c[0].getWindowStart())
        try:
            i1c[0].setWindowStart(t+1)
            self.assertNotEqual(i1c[0].getWindowStart(), i2c[0].getWindowStart())
            i1.saveDefaults()
            i1 = self.getTinyTestImage()
            i1c = i1.getChannels()
            self.assertEqual(i1c[0].getWindowStart(), t+1)

            r = fakeRequest()
            q = QueryDict('', mutable=True)
            q.update({'fromid': i1.getId()})
            q.update({'toids': i2.getId()})
            r.REQUEST.dicts += (q,)
            rv = simplejson.loads(views.copy_image_rdef_json(r, CLIENT_BASE, _conn=self.gateway)._get_content())
            err = '''FAIL: rsettings.applySettingsToImages(%i, (%i,)) -> %s''' % (i1.getId(), i2.getId(), rv)
            self.assertEqual(rv["True"], [i2.getId()], err)
            i2 = self.getTinyTestImage2()
            i2c = i2.getChannels()
            self.assertEqual(i2c[0].getWindowStart(), t+1)
        finally:
            i1 = self.getTinyTestImage()
            i1c = i1.getChannels()
            i2 = self.getTinyTestImage2()
            i2c = i2.getChannels()
            i1c[0].setWindowStart(t)
            i1.saveDefaults()
            i2c[0].setWindowStart(t)
            i2.saveDefaults()

if __name__ == '__main__':
    unittest.main()
