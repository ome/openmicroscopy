#!/usr/bin/env python
# -*- coding: utf-8 -*-

#
# Copyright (C) 2011 Glencoe Software, Inc. All Rights Reserved.
# Use is subject to license terms supplied in LICENSE.txt
#

"""
Integration test focused on the omero.api.IUpdate interface.
"""

import unittest
import integration.library as lib
import omero
from omero.rtypes import rstring, rtime

class TestIUpdate(lib.ITest):
    """
    Basic test of all IUpdate functionality
    """

    def tags(self, count=3):
        return [omero.model.TagAnnotationI() for x in range(count)]

    def testSaveArray(self):
        """
        See ticket:6870
        """
        tags = self.tags()
        self.update.saveArray(tags)

    def testSaveCollection(self):
        """
        See ticket:6870
        """
        tags = self.tags()
        self.update.saveCollection(tags)

    def testProxying(self):
        import omero

        dataset = omero.model.DatasetI()
        dataset.name = rstring("test dataset")
        dataset_saved = self.update.saveAndReturnObject(dataset)

        image = omero.model.ImageI()
        image.name = rstring("test image")
        image.acquisitionDate = rtime(0L)
        image_saved = self.update.saveAndReturnObject(image)

        fileset = omero.model.FilesetI()
        fileset_saved = self.update.saveAndReturnObject(fileset)

        dil = omero.model.DatasetImageLinkI()
        dil.parent = dataset_saved.proxy()
        dil.child = image_saved.proxy()
        dil_saved = self.update.saveAndReturnObject(dil)

        # The following line makes this test pass, but it should not be needed.
        # image_saved = self.query.get('Image', image_saved.id.val)

        image_saved.fileset = fileset_saved.proxy()
        self.update.saveAndReturnObject(image_saved)

        image_retrieved = self.query.get('Image', image_saved.id.val)

        if image_retrieved.fileset.id != fileset_saved.id:
            raise Exception("image should be associated with fileset")

        dil_retrieved = self.query.get('DatasetImageLink', dil_saved.id.val)

        if dil_retrieved.parent.id != dataset_saved.id or dil_retrieved.child.id  != image_saved.id:
            raise Exception("image should be associated with dataset")

if __name__ == '__main__':
    unittest.main()
