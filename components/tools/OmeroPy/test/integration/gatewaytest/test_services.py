#!/usr/bin/env python
# -*- coding: utf-8 -*-

"""
   gateway tests - Wrapped service methods

   Copyright 2009-2015 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

   pytest fixtures used as defined in conftest.py:
   - gatewaywrapper
   - author_testimg_generated

"""

import omero
from omero.grid import StringColumn


class TestServices (object):

    TESTANN_NS = 'omero.gateway.test_services'

    def testDeleteServiceAuthor(self, author_testimg_generated):
        author_testimg_generated.removeAnnotations(self.TESTANN_NS)
        assert author_testimg_generated.getAnnotation(self.TESTANN_NS) is None
        # Create new, link and check
        ann = omero.gateway.CommentAnnotationWrapper(
            author_testimg_generated._conn)
        ann.setNs(self.TESTANN_NS)
        ann.setValue(self.TESTANN_NS)
        author_testimg_generated.linkAnnotation(ann)
        ann = author_testimg_generated.getAnnotation(self.TESTANN_NS)
        assert ann.getNs() == self.TESTANN_NS
        assert ann.getValue() == self.TESTANN_NS
        # Delete, verify it is gone
        author_testimg_generated.removeAnnotations(self.TESTANN_NS)
        assert author_testimg_generated.getAnnotation(self.TESTANN_NS) is None

    def testDeleteServiceAdmin(self, gatewaywrapper,
                               author_testimg_generated):
        imgid = author_testimg_generated.getId()
        author_testimg_generated.removeAnnotations(self.TESTANN_NS)
        assert author_testimg_generated.getAnnotation(self.TESTANN_NS) is None
        # Create new as author, link and check
        ann = omero.gateway.CommentAnnotationWrapper(gatewaywrapper)
        ann.setNs(self.TESTANN_NS)
        ann.setValue(self.TESTANN_NS)
        author_testimg_generated.linkAnnotation(ann)
        ann = author_testimg_generated.getAnnotation(self.TESTANN_NS)
        assert ann.getNs() == self.TESTANN_NS
        assert ann.getValue() == self.TESTANN_NS
        # Verify it as admin user
        gatewaywrapper.loginAsAdmin()
        gatewaywrapper.gateway.SERVICE_OPTS.setOmeroGroup('-1')
        img = gatewaywrapper.gateway.getObject('image', imgid)
        assert img.getId() == author_testimg_generated.getId()
        ann = img.getAnnotation(self.TESTANN_NS)
        assert ann.getNs() == self.TESTANN_NS
        assert ann.getValue() == self.TESTANN_NS
        # Delete, verify it is gone
        img.removeAnnotations(self.TESTANN_NS)
        assert img.getAnnotation(self.TESTANN_NS) is None
        # Create as Admin linked to Author's image
        ann = omero.gateway.CommentAnnotationWrapper(gatewaywrapper.gateway)
        ann.setNs(self.TESTANN_NS)
        ann.setValue(self.TESTANN_NS)
        img.linkAnnotation(ann, sameOwner=False)
        ann = img.getAnnotation(self.TESTANN_NS)
        assert ann.getNs() == self.TESTANN_NS
        assert ann.getValue() == self.TESTANN_NS
        try:
            # Make the group writable so Author can delete the annotation
            g = img.details.group
            perms = str(img.details.permissions)
            chmod = omero.cmd.Chmod(
                type="/ExperimenterGroup", id=g.id.val, permissions='rwrw--')
            gatewaywrapper.gateway.c.submit(chmod)
            img = gatewaywrapper.gateway.getObject('image', imgid)
            g = img.details.group
            assert g.details.permissions.isGroupWrite()
            # Verify it as author user
            gatewaywrapper.loginAsAuthor()
            img = gatewaywrapper.gateway.getObject('image', imgid)
            assert img.getId() == author_testimg_generated.getId()
            ann = img.getAnnotation(self.TESTANN_NS)
            assert ann.getNs() == self.TESTANN_NS
            assert ann.getValue() == self.TESTANN_NS
            # Delete, verify it is gone
            img.removeAnnotations(self.TESTANN_NS)
            assert img.getAnnotation(self.TESTANN_NS) is None
        finally:
            gatewaywrapper.loginAsAdmin()
            gatewaywrapper.gateway.SERVICE_OPTS.setOmeroGroup('-1')
            # it might be that the test failed and we're stuck with an
            # annotation that author can't delete, so kill is as admin
            img = gatewaywrapper.gateway.getObject('image', imgid)
            img.removeAnnotations(self.TESTANN_NS)
            # Revert group permissions and remove user from group
            chmod = omero.cmd.Chmod(
                type="/ExperimenterGroup", id=g.id.val, permissions=perms)
            gatewaywrapper.gateway.c.submit(chmod)


class TestTables (object):

    TESTANN_NS = 'omero.gateway.test_services'

    def testTableRead(self, gatewaywrapper):
        gatewaywrapper.loginAsAuthor()
        # we are Author
        pr = gatewaywrapper.getTestProject()
        assert pr is not None
        sr = gatewaywrapper.gateway.getSharedResources()
        name = 'bulk_annotations'
        table = sr.newTable(1, name)
        data = [StringColumn('col1', '', 2, ['A1', 'B1', 'C1'])]
        original_file = table.getOriginalFile()
        assert table is not None
        table.initialize(data)
        table.addData(data)
        file_annotation = omero.gateway.FileAnnotationWrapper(
            gatewaywrapper.gateway)
        file_annotation.setNs('openmicroscopy.org/omero/bulk_annotations')
        file_annotation.setDescription(name)
        file_annotation.setFile(original_file)
        pr.linkAnnotation(file_annotation)
        # table created, can we read it back?
        pr = gatewaywrapper.getTestProject()
        assert pr is not None
        file_annotation = pr.getAnnotation(
            ns='openmicroscopy.org/omero/bulk_annotations')
        assert file_annotation is not None
        table = sr.openTable(file_annotation._obj.file)
        assert table is not None
        # now as Admin
        gatewaywrapper.loginAsAdmin()
        sr = gatewaywrapper.gateway.getSharedResources()
        gatewaywrapper.gateway.SERVICE_OPTS.setOmeroGroup('-1')
        pr = gatewaywrapper.getTestProject()
        assert pr is not None
        file_annotation = pr.getAnnotation(
            ns='openmicroscopy.org/omero/bulk_annotations')
        assert file_annotation is not None
        table = sr.openTable(
            file_annotation._obj.file, gatewaywrapper.gateway.SERVICE_OPTS)
        assert table is not None
