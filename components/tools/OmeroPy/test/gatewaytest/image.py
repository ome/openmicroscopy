#!/usr/bin/env python

"""
   gateway tests - Image Wrapper

   Copyright 2009 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

import unittest
from cStringIO import StringIO
import omero

try:
    from PIL import Image, ImageDraw # see ticket:2597
except ImportError:
    try:
        import Image, ImageDraw # see ticket:2597
    except ImportError:
        print "PIL not installed"

import gatewaytest.library as lib


class ImageTest (lib.GTest):
    def setUp (self):
        super(ImageTest, self).setUp()
        self.loginAsAuthor()
        self.image = self.getTestImage()

    def testThumbnail (self):
        thumb = self.image.getThumbnail()
        tfile = StringIO(thumb)
        thumb = Image.open(tfile) # Raises if invalid
        thumb.verify() # Raises if invalid
        self.assertEqual(thumb.format, 'JPEG')
        self.assertEqual(thumb.size, (64,64))
        thumb = self.image.getThumbnail(96)
        tfile = StringIO(thumb)
        thumb = Image.open(tfile) # Raises if invalid
        thumb.verify() # Raises if invalid
        self.assertEqual(thumb.size, (96,96))
        thumb = self.image.getThumbnail((128, 96))
        tfile = StringIO(thumb)
        thumb = Image.open(tfile) # Raises if invalid
        thumb.verify() # Raises if invalid
        self.assertEqual(thumb.size, (128,96))
        badimage = self.getBadTestImage() # no pixels
        self.assertEqual(badimage.getThumbnail(), None)

    def testRenderingModels (self):
        # default is color model
        cimg = self.image.renderJpeg(0,0)
        ifile = StringIO(cimg)
        img = Image.open(ifile)
        extrema = img.getextrema()
        self.assert_(extrema[0] != extrema [1] or extrema[0] != extrema[2], 'Looks like a greyscale image')
        # Explicitely set the color model
        self.image.setColorRenderingModel()
        self.assertEqual(cimg, self.image.renderJpeg(0,0))
        # Now for greyscale
        self.image.setGreyscaleRenderingModel()
        ifile = StringIO(self.image.renderJpeg(0,0))
        img = Image.open(ifile)
        extrema = img.getextrema()
        self.assert_(extrema[0] == extrema [1] and extrema[0] == extrema[2], 'Looks like a color image')

    def testSplitChannel (self):
        cdims = self.image.splitChannelDims(border=4)
        # Verify border attribute works
        self.assert_(self.image.splitChannelDims(border=2)['c']['width']<cdims['c']['width'])
        # Default is color model, we have 2 channels
        self.assertEqual(cdims['c']['gridx'], 2)
        self.assertEqual(cdims['c']['gridy'], 2)
        # Render the view
        ifile = StringIO(self.image.renderSplitChannel(0,0,border=4))
        img = Image.open(ifile)
        self.assertEqual(img.size[0], cdims['c']['width'])
        self.assertEqual(img.size[1], cdims['c']['height'])
        # Same dance in greyscale
        self.assertEqual(cdims['g']['gridx'], 2)
        self.assertEqual(cdims['g']['gridy'], 1)
        # Render the view
        self.image.setGreyscaleRenderingModel()
        ifile = StringIO(self.image.renderSplitChannel(0,0,border=4))
        img = Image.open(ifile)
        self.assertEqual(img.size[0], cdims['g']['width'])
        self.assertEqual(img.size[1], cdims['g']['height'])
        # Make really sure the grid calculation works as expected
        g = ((1,1),(2,1),(2,2),(2,2),(3,2),(3,2),(3,3),(3,3),(3,3),(4,3),(4,3),(4,3),(4,4),(4,4),(4,4),(4,4))
        def c_count2 ():
            return i
        self.image.c_count = c_count2
        for i in range(1,len(g)): # 1..15
            dims = self.image.splitChannelDims()
            self.assertEqual((dims['g']['gridx'], dims['g']['gridy']), g[i-1]) 
            self.assertEqual((dims['c']['gridx'], dims['c']['gridy']), g[i]) 

    def testLinePlots (self):
        """ Verify requesting lineplots give out images matching size with the original. """
        # Vertical plot
        gif = StringIO(self.image.renderColLinePlotGif (z=0, t=0, x=1))
        img = Image.open(gif)
        img.verify() # Raises if invalid
        self.assertEqual(img.format, 'GIF')
        self.assertEqual(img.size, (self.image.getWidth(), self.image.getHeight()))
        # Horizontal plot
        gif = StringIO(self.image.renderRowLinePlotGif (z=0, t=0, y=1))
        img = Image.open(gif)
        img.verify() # Raises if invalid
        self.assertEqual(img.format, 'GIF')
        self.assertEqual(img.size, (self.image.getWidth(), self.image.getHeight()))
        badimage = self.getBadTestImage() # no pixels
        self.assertEqual(badimage.getCol(z=0, t=0, x=1), None)
        self.assertEqual(badimage.getRow(z=0, t=0, y=1), None)
        self.assertEqual(badimage.renderColLinePlotGif(z=0, t=0, x=1), None)
        self.assertEqual(badimage.renderRowLinePlotGif(z=0, t=0, y=1), None)

    def testProjections (self):
        """ Test image projections """
        for p in self.image.getProjections():
            self.image.setProjection(p)
            ifile = StringIO(self.image.renderJpeg(0,0))
            img = Image.open(ifile) # Raises if invalid
            img.verify() # Raises if invalid
            self.assertEqual(img.format, 'JPEG')
            self.assertEqual(img.size, (self.image.getWidth(), self.image.getHeight()))

    def testProperties (self):
        """ Tests the property getters that are not exercised implicitly on other tests. """
        self.assertEqual(self.image.getZ(), 0)
        self.assertEqual(self.image.getT(), 0)
        # Make sure methods fail with none if no pixels are found
        self.assertNotEqual(self.image.getPixelSizeX(), None)
        badimage = self.getBadTestImage() # no pixels
        self.assertEqual(badimage.getPixelSizeX(), None)
        self.assertEqual(badimage.getChannels(), None)
            
    def testShortname (self):
        """ Test the shortname method """
        name = self.image.name
        l = len(self.image.name)
        self.assertEqual(self.image.shortname(length=l+4, hist=5), self.image.name)
        self.assertEqual(self.image.shortname(length=l-4, hist=5), self.image.name)
        self.assertEqual(self.image.shortname(length=l-5, hist=5), '...'+self.image.name[-l+5:])
        self.image.name = ''
        self.assertEqual(self.image.shortname(length=20, hist=5), '')
        self.image.name = name

    def testSimpleMarshal (self):
        """ Test the call to simpleMarhal """
        m = self.image.simpleMarshal()
        self.assertEqual(m['name'], self.image.getName())
        self.assertEqual(m['description'], self.image.getDescription())
        self.assertEqual(m['id'], self.image.getId())
        self.assertEqual(m['type'], self.image.OMERO_CLASS)
        self.assertEqual(m['author'], self.AUTHOR.fullname())
        self.assert_('parents' not in m)
        self.assert_('date' in m)
        parents = map(lambda x: x.simpleMarshal(), self.image.getAncestry())
        m = self.image.simpleMarshal(parents=True)
        self.assertEqual(m['name'], self.image.getName())
        self.assertEqual(m['description'], self.image.getDescription())
        self.assertEqual(m['id'], self.image.getId())
        self.assertEqual(m['type'], self.image.OMERO_CLASS)
        self.assertEqual(m['author'], self.AUTHOR.fullname())
        self.assert_('date' in m)
        self.assertEqual(m['parents'], parents)

if __name__ == '__main__':
    unittest.main()
