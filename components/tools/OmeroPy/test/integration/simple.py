#!/usr/bin/env python

"""
   Simple integration test which makes various calls on the
   a running server.

   Copyright 2008 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

import unittest
import omero
from omero_model_PixelsI import PixelsI
from omero_model_ImageI import ImageI
from omero_model_DatasetI import DatasetI
from omero_model_ExperimenterI import ExperimenterI
from omero_model_ExperimenterGroupI import ExperimenterGroupI
from omero_model_GroupExperimenterMapI import GroupExperimenterMapI
from omero_model_DatasetImageLinkI import DatasetImageLinkI

class TestSimple(unittest.TestCase):

    def testCurrentUser(self):
        client = omero.client()
        client.createSession()
        try:
            admin = client.sf.getAdminService()
            ec = admin.getEventContext()
            self.assert_(ec)
        finally:
            client.closeSession()

if __name__ == '__main__':
    unittest.main()
