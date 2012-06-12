#!/usr/bin/env python

"""
   gateway tests - Annotation Wrapper

   Copyright 2009 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

import unittest
import time, datetime
import omero
import os

import gatewaytest.library as lib

class AnnotationsTest (lib.GTest):
    TESTANN_NS = 'omero.gateway.test_annotation'
    def setUp (self):
        super(AnnotationsTest, self).setUp()
        self.loginAsAuthor()
        self.TESTIMG = self.getTestImage()
        self.assertNotEqual(self.TESTIMG, None, 'No test image found on database')

    def _testAnnotation (self, obj, annclass, ns, value):
        # Make sure it doesn't yet exist
        obj.removeAnnotations(ns)
        self.assertEqual(obj.getAnnotation(ns), None)
        # Create new, link and check
        ann = annclass(self.gateway)
        ann.setNs(ns)
        ann.setValue(value)
        obj.linkAnnotation(ann)
        ann = obj.getAnnotation(ns)
        # Make sure the group for the annotation is the same as the original object. (#120)
        self.assertEqual(ann.getDetails().getGroup(), obj.getDetails().getGroup())
        tval = hasattr(value, 'val') and value.val or value
        self.assert_(ann.getValue() == value, '%s != %s' % (str(ann.getValue()), str(tval)))
        self.assert_(ann.getNs() == ns,  '%s != %s' % (str(ann.getNs()), str(ns)))
        # Remove and check
        obj.removeAnnotations(ns)
        self.assertEqual(obj.getAnnotation(ns), None)
        # Same dance, createAndLink shortcut
        annclass.createAndLink(target=obj, ns=ns, val=value)
        ann = obj.getAnnotation(ns)
        # Make sure the group for the annotation is the same as the original object. (#120)
        self.assertEqual(ann.getDetails().getGroup(), obj.getDetails().getGroup())
        tval = hasattr(value, 'val') and value.val or value
        self.assert_(ann.getValue() == value, '%s != %s' % (str(ann.getValue()), str(tval)))
        self.assert_(ann.getNs() == ns,  '%s != %s' % (str(ann.getNs()), str(ns)))
        # Remove and check
        obj.removeAnnotations(ns)
        self.assertEqual(obj.getAnnotation(ns), None)

    def testNonDefGroupAnnotation (self):
        p = self.getTestProject2()
        self._testAnnotation(p,  omero.gateway.CommentAnnotationWrapper, self.TESTANN_NS, 'some value')

    def testCommentAnnotation (self):
        self._testAnnotation(self.TESTIMG, omero.gateway.CommentAnnotationWrapper, self.TESTANN_NS, 'some value')

    def testTimestampAnnotation (self):
        now = time.time()
        t = datetime.datetime.fromtimestamp(int(now))
        self._testAnnotation(self.TESTIMG, omero.gateway.TimestampAnnotationWrapper, self.TESTANN_NS, t)
        # Now use RTime, but this one doesn't fit in the general test case
        t = omero.rtypes.rtime(int(now))
        omero.gateway.TimestampAnnotationWrapper.createAndLink(target=self.TESTIMG, ns=self.TESTANN_NS, val=t)
        t = datetime.datetime.fromtimestamp(t.val / 1000.0)
        ann = self.TESTIMG.getAnnotation(self.TESTANN_NS)
        self.assert_(ann.getValue() == t, '%s != %s' % (str(ann.getValue()), str(t)))
        self.assert_(ann.getNs() == self.TESTANN_NS,  '%s != %s' % (str(ann.getNs()), str(self.TESTANN_NS)))
        # Remove and check
        self.TESTIMG.removeAnnotations(self.TESTANN_NS)
        self.assertEqual(self.TESTIMG.getAnnotation(self.TESTANN_NS), None)
        # A simple int stating secs since the epoch, also not fitting in the general test case
        t = int(now)
        omero.gateway.TimestampAnnotationWrapper.createAndLink(target=self.TESTIMG, ns=self.TESTANN_NS, val=t)
        t = datetime.datetime.fromtimestamp(t)
        ann = self.TESTIMG.getAnnotation(self.TESTANN_NS)
        self.assert_(ann.getValue() == t, '%s != %s' % (str(ann.getValue()), str(t)))
        self.assert_(ann.getNs() == self.TESTANN_NS,  '%s != %s' % (str(ann.getNs()), str(self.TESTANN_NS)))
        # Remove and check
        self.TESTIMG.removeAnnotations(self.TESTANN_NS)
        self.assertEqual(self.TESTIMG.getAnnotation(self.TESTANN_NS), None)

    def testBooleanAnnotation (self):
        self._testAnnotation(self.TESTIMG, omero.gateway.BooleanAnnotationWrapper, self.TESTANN_NS, True)

    def testLongAnnotation (self):
        self._testAnnotation(self.TESTIMG, omero.gateway.LongAnnotationWrapper, self.TESTANN_NS, 1000L)

    def testDualLinkedAnnotation (self):
        """ Tests linking the same annotation to 2 separate objects """
        dataset = self.TESTIMG.getParent()
        self.assertNotEqual(dataset, None)
        self.TESTIMG.removeAnnotations(self.TESTANN_NS)
        self.assertEqual(self.TESTIMG.getAnnotation(self.TESTANN_NS), None)
        dataset.removeAnnotations(self.TESTANN_NS)
        self.assertEqual(dataset.getAnnotation(self.TESTANN_NS), None)
        ann = omero.gateway.CommentAnnotationWrapper(self.gateway)
        ann.setNs(self.TESTANN_NS)
        value = 'I suffer from multi link disorder'
        ann.setValue(value)
        self.TESTIMG.linkAnnotation(ann)
        dataset.linkAnnotation(ann)
        self.assertEqual(self.TESTIMG.getAnnotation(self.TESTANN_NS).getValue(), value)
        self.assertEqual(dataset.getAnnotation(self.TESTANN_NS).getValue(), value)
        self.TESTIMG.removeAnnotations(self.TESTANN_NS)
        self.assertEqual(self.TESTIMG.getAnnotation(self.TESTANN_NS), None)
        self.assertEqual(dataset.getAnnotation(self.TESTANN_NS).getValue(), value)
        dataset.removeAnnotations(self.TESTANN_NS)
        self.assertEqual(dataset.getAnnotation(self.TESTANN_NS), None)

    def testListAnnotations (self):
        """ Other small things that need to be tested """
        ns1 = self.TESTANN_NS
        ns2 = ns1 + '_2'
        obj = self.TESTIMG
        annclass = omero.gateway.CommentAnnotationWrapper
        value = 'foo'
        # Make sure it doesn't yet exist
        obj.removeAnnotations(ns1)
        obj.removeAnnotations(ns2)
        self.assertEqual(obj.getAnnotation(ns1), None)
        self.assertEqual(obj.getAnnotation(ns2), None)
        # createAndLink
        annclass.createAndLink(target=obj, ns=ns1, val=value)
        annclass.createAndLink(target=obj, ns=ns2, val=value)
        ann1 = obj.getAnnotation(ns1)
        ann2 = obj.getAnnotation(ns2)
        l = list(obj.listAnnotations())
        self.assert_(ann1 in l)
        self.assert_(ann2 in l)
        l = list(obj.listAnnotations(ns=ns1))
        self.assert_(ann1 in l)
        self.assert_(ann2 not in l)
        l = list(obj.listAnnotations(ns=ns2))
        self.assert_(ann1 not in l)
        self.assert_(ann2 in l)
        l = list(obj.listAnnotations(ns='bogusns...bogusns...'))
        self.assert_(ann1 not in l)
        self.assert_(ann2 not in l)
        # Remove and check
        obj.removeAnnotations(ns1)
        obj.removeAnnotations(ns2)
        self.assertEqual(obj.getAnnotation(ns1), None)
        self.assertEqual(obj.getAnnotation(ns2), None)

    def testFileAnnotation (self):
        """ Creates a file annotation from a local file """

        tempFileName = "tempFile"
        f = open(tempFileName, 'w')
        fileText = "Test text for writing to file for upload"
        f.write(fileText)
        f.close()
        fileSize = os.path.getsize(tempFileName)
        ns = self.TESTANN_NS
        image = self.TESTIMG
        # use the same file to create various file annotations with different namespaces
        fileAnn = self.gateway.createFileAnnfromLocalFile(tempFileName, mimetype='text/plain', ns=ns)
        image.linkAnnotation(fileAnn)
        compAnn = self.gateway.createFileAnnfromLocalFile(tempFileName, mimetype='text/plain', ns=omero.constants.namespaces.NSCOMPANIONFILE)
        image.linkAnnotation(compAnn)
        os.remove(tempFileName)

        # get user-id of another user to use below.
        self.loginAsAdmin()
        adminId = self.gateway.getUser().getId()
        self.loginAsAuthor()

        # test listing of File Annotations. Should exclude companion files by default and all files should be loaded
        eid = self.gateway.getUser().getId()
        fas = list( self.gateway.listFileAnnotations(eid=eid, toInclude=[ns]) )
        faIds = [fa.id for fa in fas]
        self.assertTrue(fileAnn.getId() in faIds)
        self.assertFalse(compAnn.getId() in faIds)
        for fa in fas:
            #print fa.id, fa.ns, fa.getFile().id, fa.getFile().name, fa._obj.file.loaded
            self.assertEqual(fa.getNs(), ns, "All files should be filtered by this namespace")
            self.assertTrue(fa._obj.file.loaded, "All file annotations should have files loaded")

        # filtering by namespace
        fas = list( self.gateway.listFileAnnotations(toInclude=["nothing.with.this.namespace"], eid=eid) )
        self.assertEqual(len(fas), 0, "No file annotations should exist with bogus namespace")

        # filtering files by a different user should not return the annotations above.
        fas = list( self.gateway.listFileAnnotations(eid=adminId) )
        faIds = [fa.id for fa in fas]
        self.assertFalse(fileAnn.getId() in faIds)
        self.assertFalse(compAnn.getId() in faIds)

        ann = image.getAnnotation(ns)
        annId = ann.getId()
        self.assertEqual(ann.OMERO_TYPE, omero.model.FileAnnotationI)
        for t in ann.getFileInChunks():
            self.assertEqual(str(t), fileText)   # we get whole text in one chunk

        # delete what we created 
        self.assertNotEqual(self.gateway.getObject("Annotation", annId), None)
        link = ann.link
        self.gateway.deleteObjectDirect(link._obj)        # delete link
        self.gateway.deleteObjectDirect(ann._obj)         # then the annotation
        self.gateway.deleteObjectDirect(ann._obj.file)    # then the file
        self.assertEqual(self.gateway.getObject("Annotation", annId), None)

    def testUnlinkAnnotation (self):
        """ Tests the use of unlinkAnnotations. See #7301 """

        # Setup test dataset
        dataset = self.TESTIMG.getParent()
        self.assertNotEqual(dataset, None)

        # Make really sure there are no annotations
        dataset.removeAnnotations(self.TESTANN_NS)
        self.assertEqual(dataset.getAnnotation(self.TESTANN_NS), None)
        dataset.removeAnnotations(self.TESTANN_NS)
        self.assertEqual(dataset.getAnnotation(self.TESTANN_NS), None)

        # Add an annotation
        ann = omero.gateway.CommentAnnotationWrapper(self.gateway)
        ann.setNs(self.TESTANN_NS)
        dataset.linkAnnotation(ann)
        self.assertEqual(dataset.getAnnotation(self.TESTANN_NS).getNs(),
                self.TESTANN_NS)

        # Unlink annotations
        dataset.unlinkAnnotations(self.TESTANN_NS)
        self.assertEqual(dataset.getAnnotation(self.TESTANN_NS), None)

if __name__ == '__main__':
    unittest.main()
