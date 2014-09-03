#!/usr/bin/env python
# -*- coding: utf-8 -*-

"""
   Simple integration test which makes various calls on the
   a running server.

   Copyright 2008-2014 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

import test.integration.library as lib


class TestSimple(lib.ITest):

    def testCurrentUser(self):
        admin = self.client.sf.getAdminService()
        ec = admin.getEventContext()
        assert ec
