import unittest

import omero
import omero.gateway

class HelperObjectsTest (unittest.TestCase):
    def testColorHolder (self):
        ColorHolder = omero.gateway.ColorHolder
        c1 = ColorHolder()
        self.assertEqual(c1._color, {'red': 0, 'green': 0,'blue': 0, 'alpha': 255})
        c1 = ColorHolder('blue')
        self.assertEqual(c1.getHtml(), '0000FF')
        self.assertEqual(c1.getCss(), 'rgba(0,0,255,1.000)')
        self.assertEqual(c1.getRGB(), (0,0,255))
        c1.setRed(0xF0)
        self.assertEqual(c1.getCss(), 'rgba(240,0,255,1.000)')
        c1.setGreen(0x0F)
        self.assertEqual(c1.getCss(), 'rgba(240,15,255,1.000)')
        c1.setBlue(0)
        self.assertEqual(c1.getCss(), 'rgba(240,15,0,1.000)')
        c1.setAlpha(0x7F)
        self.assertEqual(c1.getCss(), 'rgba(240,15,0,0.498)')
        c1 = ColorHolder.fromRGBA(50,100,200,300)
        self.assertEqual(c1.getCss(), 'rgba(50,100,200,1.000)')

    def testOmeroType (self):
        omero_type = omero.gateway.omero_type
        self.assert_(isinstance(omero_type('rstring'), omero.RString))
        self.assert_(isinstance(omero_type(u'rstring'), omero.RString))
        self.assert_(isinstance(omero_type(1), omero.RInt))
        self.assert_(isinstance(omero_type(1L), omero.RLong))
        self.assert_(not isinstance(omero_type((1,2,'a')), omero.RType))

    def testSplitHTMLColor (self):
        splitHTMLColor = omero.gateway.splitHTMLColor
        self.assertEqual(splitHTMLColor('abc'), [0xAA, 0xBB, 0xCC, 0xFF])
        self.assertEqual(splitHTMLColor('abcd'), [0xAA, 0xBB, 0xCC, 0xDD])
        self.assertEqual(splitHTMLColor('abbccd'), [0xAB, 0xBC, 0xCD, 0xFF])
        self.assertEqual(splitHTMLColor('abbccdde'), [0xAB, 0xBC, 0xCD, 0xDE])
        self.assertEqual(splitHTMLColor('#$%&%'), None)

if __name__ == '__main__':
    unittest.main()
