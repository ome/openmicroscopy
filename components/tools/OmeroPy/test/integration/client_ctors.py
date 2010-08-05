#!/usr/bin/env python

"""
   Tests of the omero.client constructors

   Copyright 2008 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

import unittest, os
import integration.library as lib
import omero, Ice

here = os.path.abspath( os.path.dirname(__file__) )

class TestClientConstructors(unittest.TestCase):

    def setUp(self):
        c = omero.client(pmap=['--Ice.Config='+(os.environ.get("ICE_CONFIG"))])
        self.host = c.ic.getProperties().getProperty('omero.host')
        self.port = int(c.ic.getProperties().getProperty('omero.port'))
        self.rootpasswd = c.ic.getProperties().getProperty('omero.rootpass')
        self.user = c.ic.getProperties().getProperty('omero.user')
        self.passwd = c.ic.getProperties().getProperty('omero.pass')
        
    def testHostConstructor(self):
        c = omero.client(host=self.host, port=self.port)
        c.createSession("root", self.rootpasswd)
        c.closeSession()
        c.createSession("root", self.rootpasswd)

    def testInitializationDataConstructor(self):
        id = Ice.InitializationData()
        id.properties = Ice.createProperties()
        id.properties.setProperty("omero.host", self.host)
        id.properties.setProperty("omero.user", "root")
        id.properties.setProperty("omero.pass", self.rootpasswd)
        c = omero.client(id = id)
        c.createSession()
        c.closeSession()
        c.createSession()
        c.closeSession()

    def testMainArgsConstructor(self):
        args = ["--omero.host="+self.host,"--omero.user=root", "--omero.pass="+self.rootpasswd]
        c = omero.client(args)
        c.createSession()
        c.closeSession()
        c.createSession()
        c.closeSession()

    def testMapConstructor(self):
        p = {}
        p["omero.host"] = self.host
        p["omero.user"] = "root"
        p["omero.pass"] = self.rootpasswd
        c = omero.client(pmap = p)
        c.createSession()
        c.closeSession()
        c.createSession()
        c.closeSession()

    def testMainArgsGetsIcePrefix(self):
        args = ["--omero.host="+self.host,"--omero.user=root", "--omero.pass="+self.rootpasswd]
        args.append("--Ice.MessageSizeMax=10")
        c = omero.client(args)
        c.createSession()
        self.assertEquals("10", c.getProperty("Ice.MessageSizeMax"))
        c.closeSession()

    def testMainArgsGetsIceConfig(self):
        cfg = os.path.join(here, "client_ctors.cfg")
        if not os.path.exists(cfg):
            self.fail(cfg + " does not exist")
        args = ["--Ice.Config=" + cfg,"--omero.host=unimportant"]
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
        c2 = omero.client(host=self.host, port=self.port)
        c2.createSession(self.user, self.passwd)
        c2.closeSession()

    def testPorts(self):
        c = omero.client("localhost", 1111)
        self.assertEquals("1111",c.ic.getProperties().getProperty("omero.port"))
        c = omero.client("localhost",["--omero.port=2222"])
        self.assertEquals("2222",c.ic.getProperties().getProperty("omero.port"))
        #c = omero.client("localhost")
        #self.assertEquals(str(omero.constants.GLACIER2PORT),c.ic.getProperties().getProperty("omero.port"))

    def testPythonCtorRepair(self):
        #c = omero.client(self.host, omero.constants.GLACIER2PORT)
        c = omero.client(self.host, self.port)
        c.createSession("root", self.rootpasswd)
        c.closeSession()

if __name__ == '__main__':
    unittest.main()
