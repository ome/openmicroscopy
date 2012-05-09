#!/usr/bin/env python

"""
   gateway tests - Wrapped sercice methods

   Copyright 2009 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

import unittest
import omero
import Ice
import gatewaytest.library as lib
from omero.gateway.scripts import dbhelpers
from omero.grid import StringColumn


class ServicesTest (lib.GTest):

    TESTANN_NS = 'omero.gateway.test_services'
    def setUp (self):
        super(ServicesTest, self).setUp()
        self.loginAsAuthor()
        self.TESTIMG = self.getTestImage()
        self.assertNotEqual(self.TESTIMG, None, 'No test image found on database')

    def testDeleteServiceAuthor (self):
        self.TESTIMG.removeAnnotations(self.TESTANN_NS)
        self.assertEqual(self.TESTIMG.getAnnotation(self.TESTANN_NS), None)
        # Create new, link and check
        ann = omero.gateway.CommentAnnotationWrapper(self.gateway)
        ann.setNs(self.TESTANN_NS)
        ann.setValue(self.TESTANN_NS)
        self.TESTIMG.linkAnnotation(ann)
        ann = self.TESTIMG.getAnnotation(self.TESTANN_NS)
        self.assertEqual(ann.getNs(), self.TESTANN_NS)
        self.assertEqual(ann.getValue(), self.TESTANN_NS)
        # Delete, verify it is gone
        self.TESTIMG.removeAnnotations(self.TESTANN_NS)
        self.assertEqual(self.TESTIMG.getAnnotation(self.TESTANN_NS), None)

    def testDeleteServiceAdmin (self):
        self.TESTIMG.removeAnnotations(self.TESTANN_NS)
        self.assertEqual(self.TESTIMG.getAnnotation(self.TESTANN_NS), None)
        # Create new as author, link and check
        ann = omero.gateway.CommentAnnotationWrapper(self.gateway)
        ann.setNs(self.TESTANN_NS)
        ann.setValue(self.TESTANN_NS)
        self.TESTIMG.linkAnnotation(ann)
        ann = self.TESTIMG.getAnnotation(self.TESTANN_NS)
        self.assertEqual(ann.getNs(), self.TESTANN_NS)
        self.assertEqual(ann.getValue(), self.TESTANN_NS)
        # Verify it as admin user
        self.loginAsAdmin()
        sopts = dict(self.gateway.CONFIG['SERVICE_OPTS'] or {})
        sopts['omero.group'] = '-1'
        self.gateway.CONFIG['SERVICE_OPTS'] = sopts
        img = self.getTestImage()
        self.assertEqual(img.getId(), self.TESTIMG.getId())
        ann = img.getAnnotation(self.TESTANN_NS)
        self.assertEqual(ann.getNs(), self.TESTANN_NS)
        self.assertEqual(ann.getValue(), self.TESTANN_NS)
        # Delete, verify it is gone
        img.removeAnnotations(self.TESTANN_NS)
        self.assertEqual(img.getAnnotation(self.TESTANN_NS), None)





class TablesTest (lib.GTest):

    TESTANN_NS = 'omero.gateway.test_services'
    def setUp (self):
        super(TablesTest, self).setUp()
        self.loginAsAuthor()
        self.TESTIMG = self.getTestImage()
        self.assertNotEqual(self.TESTIMG, None, 'No test image found on database')

    def testTableRead(self):
        # we are Author
        pr = self.getTestProject()
        self.assertNotEqual(pr, None)
        sr = self.gateway.getSharedResources()
        name = 'bulk_annotations'
        table = sr.newTable(1, name)
        data = [StringColumn('col1', '', 1, ['A1','B1','C1'])]
        original_file = table.getOriginalFile()
        self.assertNotEqual(table, None)
        table.initialize(data)
        table.addData(data)
        file_annotation = omero.gateway.FileAnnotationWrapper(self.gateway)
        file_annotation.setNs('openmicroscopy.org/omero/bulk_annotations')
        file_annotation.setDescription(name)
        file_annotation.setFile(original_file)
        pr.linkAnnotation(file_annotation)
        # table created, can we read it back?
        file_annotation = pr.getAnnotation(ns='openmicroscopy.org/omero/bulk_annotations')
        self.assertNotEqual(file_annotation, None)
        table = sr.openTable(file_annotation._obj.file)
        self.assertNotEqual(table, None)
        # now as Admin
        self.loginAsAdmin()
        sr = self.gateway.getSharedResources()
        sopts = dict(self.gateway.CONFIG['SERVICE_OPTS'] or {})
        sopts['omero.group'] = '-1'
        self.gateway.CONFIG['SERVICE_OPTS'] = sopts
        pr = self.getTestProject()
        self.assertNotEqual(pr, None)
        file_annotation = pr.getAnnotation(ns='openmicroscopy.org/omero/bulk_annotations')
        self.assertNotEqual(file_annotation, None)
        table = sr.openTable(file_annotation._obj.file, sopts)
        self.assertNotEqual(table, None)



if __name__ == '__main__':
    unittest.main()
