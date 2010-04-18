#!/usr/bin/env python

"""
   Tests of the admin service

   Copyright 2008 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

import unittest
import integration.library as lib
import omero
from omero_model_PixelsI import PixelsI
from omero_model_ImageI import ImageI
from omero_model_DatasetI import DatasetI
from omero_model_ExperimenterI import ExperimenterI
from omero_model_ExperimenterGroupI import ExperimenterGroupI
from omero_model_GroupExperimenterMapI import GroupExperimenterMapI
from omero_model_DatasetImageLinkI import DatasetImageLinkI
from omero.rtypes import *

class TestAdmin(lib.ITest):

    def testGetGroup(self):
        a = self.client.getSession().getAdminService()
        l = a.lookupGroups()
        g = a.getGroup(l[0].getId().val)
        d = g.getDetails()
        o = d.owner
        self.assert_( 0 != g.sizeOfGroupExperimenterMap() )
        self.assert_( 0 != o.sizeOfGroupExperimenterMap() )

    def testSetGroup(self):
        a = self.client.getSession().getAdminService()
        e = a.getExperimenter(100)
        g = a.getGroup(200)
        print g.name

        a.setDefaultGroup(e,g)
        a = self.client.sf.getAdminService()
        self.assert_(a.getEventContext() != None)

    def testThumbnail(self):
        q = self.client.getSession().getQueryService()

        # Filter to only get one possible pixels
        f = omero.sys.Filter()
        f.offset = rint(0)
        f.limit  = rint(1)
        p = omero.sys.Parameters()
        p.theFilter = f

        pixel = q.findByQuery("select p from Pixels p join fetch p.thumbnails t", p)
        tstore = self.client.getSession().createThumbnailStore()
        if not tstore.setPixelsId(pixel.id.val):
            tstore.resetDefaults()
            tstore.setPixelsId(pixel.id.val)
        tstore.getThumbnail(rint(16), rint(16))

if __name__ == '__main__':
    unittest.main()
