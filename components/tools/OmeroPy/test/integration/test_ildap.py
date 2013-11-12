#!/usr/bin/env python
# -*- coding: utf-8 -*-

"""
   Integration test focused on the omero.api.ILdap interface.

   Copyright 2008-2013 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""
import test.integration.library as lib
import omero

class TestILdap(lib.ITest):

    def testLookupLdapExperimentersViaAdmin(self):
        admin = self.client.sf.getAdminService()
        admin.lookupLdapAuthExperimenters()

