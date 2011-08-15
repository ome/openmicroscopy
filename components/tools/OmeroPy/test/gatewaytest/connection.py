#!/usr/bin/env python

"""
   gateway tests - Connection methods

   Copyright 2009 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

import unittest
import omero
import Ice
import gatewaytest.library as lib
from omero.gateway.scripts import dbhelpers

class ConnectionMethodsTest (lib.GTest):

    def setUp (self):
        super(ConnectionMethodsTest, self).setUp()
        self.loginAsAuthor()
        self.TESTIMG = self.getTestImage()
        self.assertNotEqual(self.TESTIMG, None, 'No test image found on database')

    def testMultiProcessSession (self):
        #120 amongst other things trying to getSession() twice for the same session dies. Also in separate processes.
        # we mimic this by calling setGroupForSession, which calls sessionservice.getSession, 2 times on cloned connections
        self.loginAsAuthor()
        self.assertNotEqual(self.gateway._session, None)
        c2 = self.gateway.clone()
        self.assert_(c2.connect(sUuid=self.gateway._sessionUuid))
        self.assertEqual(c2._session, None)
        a = c2.getAdminService()
        g = omero.gateway.ExperimenterGroupWrapper(c2, a.containedGroups(c2._userid)[-1])
        self.assertEqual(g.name, c2.getEventContext().groupName)
        c2.setGroupForSession(g)
        c3 = self.gateway.clone()
        self.assert_(c3.connect(sUuid=self.gateway._sessionUuid))
        self.assertEqual(c3._session, None)
        a = c3.getAdminService()
        g = omero.gateway.ExperimenterGroupWrapper(c3, a.containedGroups(c3._userid)[1])
        self.assertEqual(g.name, c3.getEventContext().groupName)
        c3.setGroupForSession(g)

    def testSeppuku (self):
        self.loginAsAuthor()
        self.assertNotEqual(self.getTestImage(), None)
        self.gateway.seppuku()
        self.assertRaises(Ice.ConnectionLostException, self.getTestImage)
        self._has_connected = False
        self.doDisconnect()
        self.loginAsAuthor()
        self.assertNotEqual(self.getTestImage(), None)
        self.gateway.seppuku(softclose=False)
        self.assertRaises(Ice.ConnectionLostException, self.getTestImage)
        self._has_connected = False
        self.doDisconnect()
        # Also make sure softclose does the right thing
        self.loginAsAuthor()
        g2 = self.gateway.clone()
        def g2_getTestImage():
            return dbhelpers.getImage(g2, 'testimg1')
        self.assert_(g2.connect(self.gateway._sessionUuid))
        self.assertNotEqual(self.getTestImage(), None)
        self.assertNotEqual(g2_getTestImage(), None)
        g2.seppuku(softclose=True)
        self.assertRaises(Ice.ConnectionLostException, g2_getTestImage)
        self.assertNotEqual(self.getTestImage(), None)
        g2 = self.gateway.clone()
        self.assert_(g2.connect(self.gateway._sessionUuid))
        self.assertNotEqual(self.getTestImage(), None)
        self.assertNotEqual(g2_getTestImage(), None)
        g2.seppuku(softclose=False)
        self.assertRaises(Ice.ConnectionLostException, g2_getTestImage)
        self.assertRaises(Ice.ConnectionLostException, self.getTestImage)
        self._has_connected = False
        self.doDisconnect()

    def testTopLevelObjects (self):
        ##
        # Test listProjects as root (sees, does not own)
        parents = self.TESTIMG.getAncestry()
        project_id = parents[-1].getId()
        # Original (4.1) test fails since 'admin' is logged into group 0, but the project
        # created above is in new group.
        # self.loginAsAdmin()   # test passes if we remain logged in as Author
        ids = map(lambda x: x.getId(), self.gateway.listProjects(only_owned=False))
        self.assert_(project_id in ids)
        self.loginAsAdmin()   # test passes if we NOW log in as Admin (different group)
        ids = map(lambda x: x.getId(), self.gateway.listProjects(only_owned=True))
        self.assert_(project_id not in ids)
        ##
        # Test listProjects as author (sees, owns)
        self.loginAsAuthor()
        ids = map(lambda x: x.getId(), self.gateway.listProjects(only_owned=False))
        self.assert_(project_id in ids)
        ids = map(lambda x: x.getId(), self.gateway.listProjects(only_owned=True))
        self.assert_(project_id in ids)
        ##
        # Test listProjects as guest (does not see, does not own)
        self.doLogin(self.USER)
        ids = map(lambda x: x.getId(), self.gateway.listProjects(only_owned=False))
        self.assert_(project_id not in ids)
        ids = map(lambda x: x.getId(), self.gateway.listProjects(only_owned=True))
        self.assert_(project_id not in ids)
        ##
        # Test getProject
        self.loginAsAuthor()
        self.assertEqual(self.gateway.getObject("Project", project_id).getId(), project_id)
        ##
        # Test getDataset
        dataset_id = parents[0].getId()
        self.assertEqual(self.gateway.getObject("Dataset", dataset_id).getId(), dataset_id)
        ##
        # Test listExperimenters
        #exps = map(lambda x: x.omeName, self.gateway.listExperimenters())  # removed from blitz gateway
        exps = map(lambda x: x.omeName, self.gateway.getObjects("Experimenter"))
        for omeName in (self.USER.name, self.AUTHOR.name, self.ADMIN.name.decode('utf-8')):
            self.assert_(omeName in exps)
            self.assert_(len(list(self.gateway.getObjects("Experimenter", attributes={'omeName':omeName}))) > 0)
        comboName = self.USER.name+self.AUTHOR.name+self.ADMIN.name
        self.assert_(len(list(self.gateway.getObjects("Experimenter", attributes={'omeName':comboName}))) ==  0)
        ##
        # Test lookupExperimenter
        self.assertEqual(self.gateway.getObject("Experimenter", attributes={'omeName':self.USER.name}).omeName, self.USER.name)
        self.assertEqual(self.gateway.getObject("Experimenter", attributes={'omeName':comboName}), None)
        ##
        # still logged in as Author, test listImages(ns)
        def listImages(ns=None):
            imageAnnLinks = self.gateway.getAnnotationLinks("Image", ns=ns)
            return [omero.gateway.ImageWrapper(self.gateway, link.parent) for link in imageAnnLinks]
        ns = 'weblitz.test_annotation'
        obj = self.getTestImage()
        # Make sure it doesn't yet exist
        obj.removeAnnotations(ns)
        self.assertEqual(obj.getAnnotation(ns), None)
        # Check without the ann
        self.assertEqual(len(listImages(ns=ns)), 0)
        annclass = omero.gateway.CommentAnnotationWrapper
        # createAndLink
        annclass.createAndLink(target=obj, ns=ns, val='foo')
        imgs = listImages(ns=ns)
        self.assertEqual(len(imgs), 1)
        self.assertEqual(imgs[0], obj)
        # and clean up
        obj.removeAnnotations(ns)
        self.assertEqual(obj.getAnnotation(ns), None)

    def testCloseSession (self):
        #74 the failed connection for a user not in the system group does not get closed
        self.gateway.setIdentity(self.USER.name, self.USER.passwd)
        setprop = self.gateway.c.ic.getProperties().setProperty
        map(lambda x: setprop(x[0],str(x[1])), self.gateway._ic_props.items())
        self.gateway.c.ic.getImplicitContext().put(omero.constants.GROUP, self.gateway.group)
        self.assertEqual(self.gateway._sessionUuid, None)
        self.assertRaises(omero.SecurityViolation, self.gateway._createSession)
        self.assertNotEqual(self.gateway._sessionUuid, None)
        #74 bug found while fixing this, the uuid passed to closeSession was not wrapped in rtypes, so logout didn't
        self.gateway._closeSession() # was raising ValueError

        
    def testMiscellaneous (self):
        self.loginAsUser()
        self.assertEqual(self.gateway.getUser().omeName, self.USER.name)

if __name__ == '__main__':
    unittest.main()
