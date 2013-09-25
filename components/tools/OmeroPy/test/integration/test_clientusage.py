#!/usr/bin/env python
# -*- coding: utf-8 -*-

"""
   Tests for the demonstrating client usage

   Copyright 2010 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

import omero
import unittest
import test.integration.library as lib

from omero.rtypes import rstring, rlong
from omero.util.concurrency import get_event

class TestClientUsage(lib.ITest):
    """
    Note: this is the only test which should use 'omero.client()'
    All others should use the new_client(user=) syntax from lib.ITest
    """

    def testClientClosedAutomatically(self):
        client = omero.client()
        client.createSession();
        client.getSession().closeOnDestroy()

    def testClientClosedManually(self):
        client = omero.client()
        client.createSession();
        client.getSession().closeOnDestroy();
        client.closeSession();

    def testUseSharedMemory(self):
        client = omero.client()
        client.createSession();

        self.assertEquals(0, len(client.getInputKeys()))
        client.setInput("a", rstring("b"));
        self.assertEquals(1, len(client.getInputKeys()))
        self.assertTrue("a" in client.getInputKeys())
        self.assertEquals("b", client.getInput("a").getValue());

        client.closeSession();

    def testCreateInsecureClientTicket2099(self):
        secure = omero.client();
        self.assert_(secure.isSecure())
        try:
            secure.createSession().getAdminService().getEventContext();
            insecure = secure.createClient(False);
            try:
                insecure.getSession().getAdminService().getEventContext();
                self.assert_( not insecure.isSecure());
            finally:
                insecure.closeSession();
        finally:
            secure.closeSession();

    def testGetStatefulServices(self):
        root = self.root
        sf = root.sf
        sf.setSecurityContext(omero.model.ExperimenterGroupI(0, False))
        sf.createRenderingEngine()
        srvs = root.getStatefulServices()
        self.assertEquals(1, len(srvs))
        try:
            sf.setSecurityContext(omero.model.ExperimenterGroupI(1, False))
            self.fail("Should not be allowed")
        except:
            pass # good
        srvs[0].close()
        srvs = root.getStatefulServices()
        self.assertEquals(0, len(srvs))
        sf.setSecurityContext(omero.model.ExperimenterGroupI(1, False))

if __name__ == '__main__':
    unittest.main()

