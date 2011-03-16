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
        
    def testGetExperimenter(self):
        self.loginAsAdmin()
        e = self.gateway.findExperimenter(self.USER.name)
        exp = self.gateway.getObject("Experimenter", e.id) # uses iQuery
        experimenter = self.gateway.getExperimenter(e.id)  # uses IAdmin
        
        # groupExperimenterMap not loaded for exp
        #for groupExpMap in exp.copyGroupExperimenterMap():
            #self.assertEqual(e.id, groupExpMap.child.id.val)
            
        for groupExpMap in experimenter.copyGroupExperimenterMap():
            self.assertEqual(e.id, groupExpMap.child.id.val)
            
            
        self.assertEqual(exp.getDetails().getOwner().omeName, experimenter.getDetails().getOwner().omeName)
        
    def testGetAnnotations(self):
        
        obj = self.TESTIMG
        ns = "omero.gateway.test_wrapper.test_get_annotations"
        
        # create Comment
        ann = omero.gateway.CommentAnnotationWrapper(self.gateway)
        ann.setNs(ns)
        ann.setValue("Test Comment")
        obj.linkAnnotation(ann)
        ann = obj.getAnnotation(ns)
        # create Tag
        tag = omero.gateway.TagAnnotationWrapper(self.gateway)
        tag.setNs(ns)
        tag.setValue("Test Tag")
        tag = obj.linkAnnotation(tag)
        
        # get the Comment 
        a = self.gateway.getAnnotation(ann.id)
        annotation = self.gateway.getObject("Annotation", ann.id)
        self.assertEqual(a.id, annotation.id)
        self.assertEqual(a.ns, annotation.ns)
        self.assertEqual("Test Comment", annotation.textValue)
        self.assertEqual(a.OMERO_TYPE, annotation.OMERO_TYPE)
        self.assertEqual(ann.OMERO_TYPE, annotation.OMERO_TYPE)
        
        # get the Comment and Tag
        annGen = self.gateway.getObjects("Annotation", [ann.id, tag.id])
        anns = list(annGen)
        self.assertEqual(len(anns), 2)
        self.assertEqual(anns[0].ns, ns)
        self.assertEqual(anns[1].ns, ns)
        self.assertNotEqual(anns[0].OMERO_TYPE, anns[1].OMERO_TYPE)
        
        
    def testGetImage (self):
        testImage = self.TESTIMG
        # This should return image wrapper
        image = self.gateway.getObject("Image", testImage.id)
        pr = image.getProject()
        ds = image.getDataset()
        
        # test a few methods that involve lazy loading, rendering etc. 
        self.assertEqual(image.getWidth(), testImage.getWidth())
        self.assertEqual(image.getHeight(), testImage.getHeight())
        image.isGreyscaleRenderingModel()       # loads rendering engine
        testImage.isGreyscaleRenderingModel()
        self.assertEqual(image._re.getDefaultZ(), testImage._re.getDefaultZ())
        self.assertEqual(image._re.getDefaultT(), testImage._re.getDefaultT())
        self.assertEqual(image.getOwnerOmeName, testImage.getOwnerOmeName)
        
        
    def testGetProject (self):
        self.loginAsAuthor()
        testProj = self.getTestProject()
        p = self.gateway.getObject("Project", 12334334)
        self.assertEqual(testProj.getName(), p.getName())
        self.assertEqual(testProj.getDescription(), p.getDescription())
        self.assertEqual(testProj.getId(), p.getId())
        self.assertEqual(testProj.OMERO_CLASS, p.OMERO_CLASS)
        self.assertEqual(testProj.countChildren_cached(), p.countChildren_cached())
        self.assertEqual(testProj.getOwnerOmeName, p.getOwnerOmeName)
        
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
        self.loginAsAdmin()
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
        pm = d.listParents(single=True).simpleMarshal()
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
        pm_multi = d.listParents(single=False)
        self.assertEqual([d.listParents(single=True)], pm_multi)

    def testExperimenterWrapper (self):
        self.loginAsAdmin()
        e = self.gateway.lookupExperimenter(self.USER.name)
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
