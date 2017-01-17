#!/usr/bin/env python
# -*- coding: utf-8 -*-

"""
   Simple integration test which makes various calls on the
   a running server.

   Copyright 2008-2013 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

from omero.testlib import ITest


class TestSimple(ITest):

    def testCurrentUser(self):
        admin = self.client.sf.getAdminService()
        ec = admin.getEventContext()
        assert ec

    def testImport(self):
        image = self.import_single_image()
        assert image.id.val
