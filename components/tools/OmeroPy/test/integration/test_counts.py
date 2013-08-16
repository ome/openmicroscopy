#!/usr/bin/env python
# -*- coding: utf-8 -*-

"""
   Integration test for accessing countsPerOwner.

   Copyright 2008 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

import unittest
import test.integration.library as lib
import omero
from omero_model_ImageI import ImageI
from omero_model_TagAnnotationI import TagAnnotationI
from omero.rtypes import rstring, rtime

class TestCounts(lib.ITest):

    def testBasicUsage(self):
        usr = self.client.sf.getAdminService().getEventContext().userId

        img = ImageI()
        img.name = rstring("name")
        img.acquisitionDate = rtime(0)
        tag = TagAnnotationI()
        img.linkAnnotation( tag )

        img = self.client.sf.getUpdateService().saveAndReturnObject( img )

        img = self.client.sf.getQueryService().findByQuery(
        """
        select img from Image img
        join fetch img.annotationLinksCountPerOwner
        where img.id = %s
        """ % (img.id.val), None
        )
        self.assert_(img)
        self.assert_(img.getAnnotationLinksCountPerOwner()[usr] > 0)

if __name__ == '__main__':
    unittest.main()
