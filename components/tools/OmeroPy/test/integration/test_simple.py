#!/usr/bin/env python
# -*- coding: utf-8 -*-

"""
   Simple integration test which makes various calls on the
   a running server.

   Copyright 2008 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

import unittest
import test.integration.library as lib
import omero
from omero_model_PixelsI import PixelsI
from omero_model_ImageI import ImageI
from omero_model_DatasetI import DatasetI
from omero_model_ExperimenterI import ExperimenterI
from omero_model_ExperimenterGroupI import ExperimenterGroupI
from omero_model_GroupExperimenterMapI import GroupExperimenterMapI
from omero_model_DatasetImageLinkI import DatasetImageLinkI

class TestSimple(lib.ITest):

    def testCurrentUser(self):
        admin = self.client.sf.getAdminService()
        ec = admin.getEventContext()
        self.assert_(ec)

if __name__ == '__main__':
    unittest.main()
