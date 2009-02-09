#!/usr/bin/env python

"""
   Tests of the omero.client constructors

   Copyright 2008 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

import unittest, os
import test.integration.library as lib
import omero, Ice

here = os.path.abspath( os.path.dirname(__file__) )

class TestClientConstructors(unittest.TestCase):

    def testHostConstructor(self):
        c = omero.client( host = "localhost")
        c.createSession("root", "ome")
        c.closeSession()
        c.createSession("root", "ome")

    def testInitializationDataConstructor(self):
        id = Ice.InitializationData()
        id.properties = Ice.createProperties()
        id.properties.setProperty("omero.host", "localhost")
        id.properties.setProperty("omero.user", "root")
        id.properties.setProperty("omero.pass", "ome")
        c = omero.client(id = id)
        c.createSession()
        c.closeSession()
        c.createSession()
        c.closeSession()

    def testMainArgsConstructor(self):
        args = ["--omero.host=localhost","--omero.user=root", "--omero.pass=ome"]
        c = omero.client(args)
        c.createSession()
        c.closeSession()
        c.createSession()
        c.closeSession()

    def testMapConstructor(self):
        p = {}
        p["omero.host"] = "localhost"
        p["omero.user"] = "root"
        p["omero.pass"] = "ome"
        c = omero.client(pmap = p)
        c.createSession()
        c.closeSession()
        c.createSession()
        c.closeSession()

    def testMainArgsGetsIcePrefix(self):
        args = ["--omero.host=localhost","--omero.user=root", "--omero.pass=ome"]
        args.append("--Ice.MessageSizeMax=10")
        c = omero.client(args)
        c.createSession()
        self.assertEquals("10", c.getProperty("Ice.MessageSizeMax"))
        c.closeSession()

    def testMainArgsGetsIceConfig(self):
        cfg = os.path.join(here, "client_ctors.cfg")
        if not os.path.exists(cfg):
            self.fail(cfg + " does not exist")
        args = ["--Ice.Config=" + cfg]
        c = omero.client(args)
        self.assertEquals("true",c.getProperty("in.ice.config"))
        #c.createSession()
        #c.closeSession()

    def testTwoDifferentHosts(self):
        try:
            c1 = omero.client(host="foo")
            c1.createSession()
            c1.closeSession()
        except:
            print "foo failed appropriately"
        c2 = omero.client(host="localhost")
        c2.createSession()
        c2.closeSession()


    def testPythonCtorRepair(self):
        c = omero.client("localhost", 4063)
        c.createSession("root", "ome")
        c.closeSession()

if __name__ == '__main__':
    unittest.main()
