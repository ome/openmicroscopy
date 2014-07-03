#!/usr/bin/env python
# -*- coding: utf-8 -*-

"""
   Copyright 2013 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

import omero
import omero.gateway

class TestHelperObjects (object):
    def testColorHolder (self):
        ColorHolder = omero.gateway.ColorHolder
        c1 = ColorHolder()
        assert c1._color ==  {'red': 0, 'green': 0,'blue': 0, 'alpha': 255}
        c1 = ColorHolder('blue')
        assert c1.getHtml() ==  '0000FF'
        assert c1.getCss() ==  'rgba(0,0,255,1.000)'
        assert c1.getRGB() ==  (0,0,255)
        c1.setRed(0xF0)
        assert c1.getCss() ==  'rgba(240,0,255,1.000)'
        c1.setGreen(0x0F)
        assert c1.getCss() ==  'rgba(240,15,255,1.000)'
        c1.setBlue(0)
        assert c1.getCss() ==  'rgba(240,15,0,1.000)'
        c1.setAlpha(0x7F)
        assert c1.getCss() ==  'rgba(240,15,0,0.498)'
        c1 = ColorHolder.fromRGBA(50,100,200,300)
        assert c1.getCss() ==  'rgba(50,100,200,1.000)'

    def testOmeroType (self):
        omero_type = omero.gateway.omero_type
        assert isinstance(omero_type('rstring'), omero.RString)
        assert isinstance(omero_type(u'rstring'), omero.RString)
        assert isinstance(omero_type(1), omero.RInt)
        assert isinstance(omero_type(1L), omero.RLong)
        assert not isinstance(omero_type((1,2,'a')), omero.RType)

    def testSplitHTMLColor (self):
        splitHTMLColor = omero.gateway.splitHTMLColor
        assert splitHTMLColor('abc') ==  [0xAA, 0xBB, 0xCC, 0xFF]
        assert splitHTMLColor('abcd') ==  [0xAA, 0xBB, 0xCC, 0xDD]
        assert splitHTMLColor('abbccd') ==  [0xAB, 0xBC, 0xCD, 0xFF]
        assert splitHTMLColor('abbccdde') ==  [0xAB, 0xBC, 0xCD, 0xDE]
        assert splitHTMLColor('#$%&%') ==  None

