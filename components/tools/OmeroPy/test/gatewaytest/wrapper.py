#!/usr/bin/env python

"""
   gateway tests - Object Wrappers

   Copyright 2009 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

import unittest
import omero

import gatewaytest.library as lib

class WrapperTest (lib.GTest):
    
    def setUp (self):
        super(WrapperTest, self).setUp()
        self.loginAsAuthor()
        self.TESTIMG = self.getTestImage()

    def testAllObjectsWrapped (self):
        """ Blitz object wrapper should ensure that all values returned are also wrapped (or are primative values) """

        image = self.TESTIMG
        pixels = image.getPrimaryPixels()
        instrument = image.getInstrument()
        self.assertTrue(isinstance(instrument, omero.gateway.BlitzObjectWrapper))
        self.assertFalse(hasattr(image.getArchived(), 'val'), "Shouldn't return rtype")
        self.assertFalse(hasattr(image.getAcquisitionDate(), 'val'), "Shouldn't return rtype")
        self.assertFalse(hasattr(image.sizeOfPixels(), 'val'), "Non 'get' methods shouldn't return rtype either")
        self.assertTrue(isinstance(image.getInstrument(), omero.gateway.InstrumentWrapper), "Should return InstrumentWrapper")
        self.assertTrue(isinstance(pixels, omero.gateway.BlitzObjectWrapper), "Should return a BlitzObjectWrapper")

        # 'get' methods should wrap model objects in BlitzObjectWrapper - allowing lazy loading
        self.assertTrue(isinstance(image.getFormat(), omero.gateway.BlitzObjectWrapper), "Should return a BlitzObjectWrapper")
        format = image.getFormat()
        self.assertEqual(format.value, "Deltavision", "BlitzObjectWrapper should lazy-load the value")
        self.assertFalse(hasattr(format.value, 'val'), "Shouldn't return rtype")
        self.assertFalse(hasattr(format.getValue(), 'val'), "Shouldn't return rtype")
        # direct access of the same model object shouldn't wrap
        self.assertTrue(isinstance(image.format, omero.model.FormatI), "Shouldn't wrap directly-accessed objects")
        self.assertTrue(hasattr(image.format.id, 'val'), "Model object access")
        # 'get' methods where there isn't a similarly-named attribute also shouldn't wrap
        self.assertTrue(isinstance(image.getPixels(0), omero.model.PixelsI), "Shouldn't wrap: No 'pixels' attribute")
        # Don't accidentally wrap data structures
        self.assertTrue(isinstance(image.copyPixels(), list), "Shouldn't wrap lists")


    def testProjectWrapper (self):
        self.loginAsAuthor()
        p = self.getTestProject()
        m = p.simpleMarshal()
        self.assertEqual(m['name'], p.getName())
        self.assertEqual(m['description'], p.getDescription())
        self.assertEqual(m['id'], p.getId())
        self.assertEqual(m['type'], p.OMERO_CLASS)
        self.assert_('parents' not in m)
        m = p.simpleMarshal(parents=True)
        self.assertEqual(m['name'], p.getName())
        self.assertEqual(m['description'], p.getDescription())
        self.assertEqual(m['id'], p.getId())
        self.assertEqual(m['type'], p.OMERO_CLASS)
        self.assertEqual(m['parents'], [])
        m = p.simpleMarshal(xtra={'childCount':None})
        self.assertEqual(m['name'], p.getName())
        self.assertEqual(m['description'], p.getDescription())
        self.assertEqual(m['id'], p.getId())
        self.assertEqual(m['type'], p.OMERO_CLASS)
        self.assert_('parents' not in m)
        self.assertEqual(m['child_count'], p.countChildren_cached())
        # Verify canOwnerWrite
        # self.loginAsAdmin()
        p = self.getTestProject()
        self.assertEqual(p.canOwnerWrite(), True)
        p.getDetails().permissions.setUserWrite(False)
        self.assertEqual(p.canOwnerWrite(), False)
        # we did not save, but revert anyway
        p.getDetails().permissions.setUserWrite(True)
        self.assertEqual(p.canOwnerWrite(), True)

    def testDatasetWrapper (self):
        self.loginAsAuthor()
        d = self.getTestDataset()
        # first call to count_cached should calculate and store
        self.assertEqual(d.countChildren_cached(), 4)
        pm = d.getParent().simpleMarshal()
        m = d.simpleMarshal()
        self.assertEqual(m['name'], d.getName())
        self.assertEqual(m['description'], d.getDescription())
        self.assertEqual(m['id'], d.getId())
        self.assertEqual(m['type'], d.OMERO_CLASS)
        self.assert_('parents' not in m)
        m = d.simpleMarshal(parents=True)
        self.assertEqual(m['name'], d.getName())
        self.assertEqual(m['description'], d.getDescription())
        self.assertEqual(m['id'], d.getId())
        self.assertEqual(m['type'], d.OMERO_CLASS)
        self.assertEqual(m['parents'], [pm])
        m = d.simpleMarshal(xtra={'childCount':None})
        self.assertEqual(m['name'], d.getName())
        self.assertEqual(m['description'], d.getDescription())
        self.assertEqual(m['id'], d.getId())
        self.assertEqual(m['type'], d.OMERO_CLASS)
        self.assert_('parents' not in m)
        self.assertEqual(m['child_count'], d.countChildren_cached())
        # Do an extra check on listParents
        pm_multi = d.getParent()
        self.assertEqual(d.listParents()[0], pm_multi)

    def testExperimenterWrapper (self):
        self.loginAsAdmin()
        e = self.gateway.findExperimenter(self.USER.name)
        self.assertEqual(e.getDetails().getOwner().omeName, self.USER.name)

    def testDetailsWrapper (self):
        self.loginAsAuthor()
        img = self.getTestImage()
        d = img.getDetails()
        self.assertEqual(d.getOwner().omeName, self.AUTHOR.name)
        self.assertEqual(d.getGroup().name, img.getProject().getDetails().getGroup().name)

    def testSetters (self):
        """ verify the setters that coerce values into blitz friendly rtypes."""
        self.loginAsAuthor()
        p = self.getTestProject()
        n = p.getName()
        p.setName('some name')
        self.assertEqual(p.getName(), 'some name')
        # we have not saved, but just in case revert it
        p.setName(n)
        self.assertEqual(p.getName(), n)
        # Trying for something that does not exist must raise
        self.assertRaises(AttributeError, getattr, self, 'something_wild_that_does_not_exist')
        
    def testOther (self):
        p = omero.gateway.ProjectWrapper()
        self.assertNotEqual(repr(p), None)

if __name__ == '__main__':
    unittest.main()
