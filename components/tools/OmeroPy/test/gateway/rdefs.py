#!/usr/bin/env python

"""
   gateway tests - Rendering Defaults

   Copyright 2009 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

import unittest
from types import StringTypes
import omero

import test.gateway.library as lib


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

    def testDefault (self):
        # Change the color for the rendering defs
        self.channels = self.image.getChannels()
        self.assertNotEqual(self.c0color, 'F0F000')
        self.assertNotEqual(self.c1color, '000F0F')
        self.image.setActiveChannels([1, 2],[[292.0, 1631.0], [409.0, 5015.0]],[u'F0F000', u'000F0F'])
        self.channels = self.image.getChannels()
        self.assert_(len(self.channels) == 2, 'bad channel count on image #%d' % self.TESTIMG_ID)
        self.assertEqual(self.channels[0].getColor().getHtml(), 'F0F000')
        self.assertEqual(self.channels[1].getColor().getHtml(), '000F0F')
        # Save it as default
        self.assert_(self.image.saveDefaults(), 'Failed saveDefaults')
        # Verify that it comes back as default
        self.image._re = None
        self.channels = self.image.getChannels()
        self.assert_(len(self.channels) == 2, 'bad channel count on image #%d' % self.TESTIMG_ID)
        self.assertEqual(self.channels[0].getColor().getHtml(), 'F0F000')
        self.assertEqual(self.channels[1].getColor().getHtml(), '000F0F')
        # weblitz#82 Changing default colors doesn't work correctly
        # the customizations weren't global, each user had its own
        self.loginAsAdmin()
        self.image = self.getTestImage()
        self.assertNotEqual(self.image, None, 'No test image found on database')
        self.channels = self.image.getChannels()
        self.assert_(len(self.channels) == 2, 'bad channel count on image #%d' % self.TESTIMG_ID)
        self.assertEqual(self.channels[0].getColor().getHtml(), 'F0F000')
        self.assertEqual(self.channels[1].getColor().getHtml(), '000F0F')
        # so root sees the changes, but do root's changes get seen by author?
        self.image.clearDefaults() # remove author's version to make sure root creates a new one as author
        self.image = self.getTestImage()
        self.image.setActiveChannels([1, 2],[[292.0, 1631.0], [409.0, 5015.0]],[u'000F0F', u'F0F000'])
        self.assert_(self.image.saveDefaults(), 'Failed saveDefaults')
        self.loginAsEditor()
        self.image = self.getTestImage()
        self.assertNotEqual(self.image, None, 'No test image found on database')
        self.assert_(isinstance(self.image.getThumbnail(), StringTypes))
        self.channels = self.image.getChannels()
        self.assert_(len(self.channels) == 2, 'bad channel count on image #%d' % self.TESTIMG_ID)
        self.assertEqual(self.channels[0].getColor().getHtml(), '000F0F')
        self.assertEqual(self.channels[1].getColor().getHtml(), 'F0F000')
        # webliz#82 ends, back to AUTHOR
        # Clean the customized default
        self.loginAsAuthor()
        self.image = self.getTestImage()
        self.image.clearDefaults()
        self.image = self.getTestImage()
        self.channels = self.image.getChannels()
        # Verify we got back to the original state
        self.assert_(len(self.channels) == 2, 'bad channel count on image #%d' % self.TESTIMG_ID)
        self.assertEqual(self.channels[0].getColor().getHtml(), self.c0color)
        self.assertEqual(self.channels[1].getColor().getHtml(), self.c1color)

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
            self.assert_(min < start < end < max)
            channel.setWindowStart(min)
            self.assertEqual(channel.getWindowStart(), min)
            channel.setWindowEnd(max)
            self.assertEqual(channel.getWindowEnd(), max)
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

if __name__ == '__main__':
    unittest.main()
