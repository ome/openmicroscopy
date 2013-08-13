#!/usr/bin/env python
# -*- coding: utf-8 -*-

"""
   Copyright 2009-2013 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

   pytest fixtures used as defined in conftest.py:
   - gatewaywrapper
   - author_testimg

"""

import omero
import Ice
from omero.gateway.scripts import dbhelpers
import pytest

class TestConnectionMethods (object):

    def testMultiProcessSession (self, gatewaywrapper):
        #120 amongst other things trying to getSession() twice for the same session dies. Also in separate processes.
        # we mimic this by calling setGroupForSession, which calls sessionservice.getSession, 2 times on cloned connections
        gatewaywrapper.loginAsAuthor()
        assert gatewaywrapper.gateway.getSession() !=  None
        c2 = gatewaywrapper.gateway.clone()
        assert c2.connect(sUuid=gatewaywrapper.gateway._sessionUuid)
        assert c2.getSession() !=  None
        a = c2.getAdminService()
        g = omero.gateway.ExperimenterGroupWrapper(c2, a.containedGroups(c2.getUserId())[-1])
        c2.setGroupForSession(g)
        c3 = gatewaywrapper.gateway.clone()
        assert c3.connect(sUuid=gatewaywrapper.gateway._sessionUuid)
        assert c3.getSession() !=  None
        a = c3.getAdminService()
        g = omero.gateway.ExperimenterGroupWrapper(c3, a.containedGroups(c3.getUserId())[1])
        c3.setGroupForSession(g)

    def testSeppuku (self, gatewaywrapper, author_testimg):
        # author_testimg in args to make sure the image has been imported
        gatewaywrapper.loginAsAuthor()
        assert gatewaywrapper.getTestImage() !=  None
        gatewaywrapper.gateway.seppuku()
        pytest.raises(Ice.ConnectionLostException, gatewaywrapper.getTestImage)
        gatewaywrapper._has_connected = False
        gatewaywrapper.doDisconnect()
        gatewaywrapper.loginAsAuthor()
        assert gatewaywrapper.getTestImage() !=  None
        gatewaywrapper.gateway.seppuku(softclose=False)
        pytest.raises(Ice.ConnectionLostException, gatewaywrapper.getTestImage)
        gatewaywrapper._has_connected = False
        gatewaywrapper.doDisconnect()
        # Also make sure softclose does the right thing
        gatewaywrapper.loginAsAuthor()
        g2 = gatewaywrapper.gateway.clone()
        def g2_getTestImage():
            return dbhelpers.getImage(g2, 'testimg1')
        assert g2.connect(gatewaywrapper.gateway._sessionUuid)
        assert gatewaywrapper.getTestImage() !=  None
        assert g2_getTestImage() !=  None
        g2.seppuku(softclose=True)
        pytest.raises(Ice.ConnectionLostException, g2_getTestImage)
        assert gatewaywrapper.getTestImage() !=  None
        g2 = gatewaywrapper.gateway.clone()
        assert g2.connect(gatewaywrapper.gateway._sessionUuid)
        assert gatewaywrapper.getTestImage() !=  None
        assert g2_getTestImage() !=  None
        g2.seppuku(softclose=False)
        pytest.raises(Ice.ConnectionLostException, g2_getTestImage)
        pytest.raises(Ice.ObjectNotExistException, gatewaywrapper.getTestImage)
        gatewaywrapper._has_connected = False
        gatewaywrapper.doDisconnect()

    def testTopLevelObjects (self, gatewaywrapper, author_testimg):
        ##
        # Test listProjects as root (sees, does not own)
        parents = author_testimg.getAncestry()
        project_id = parents[-1].getId()
        # Original (4.1) test fails since 'admin' is logged into group 0, but the project
        # created above is in new group.
        # gatewaywrapper.loginAsAdmin()   # test passes if we remain logged in as Author
        ids = map(lambda x: x.getId(), gatewaywrapper.gateway.listProjects())
        assert project_id in ids
        gatewaywrapper.loginAsAdmin()   # test passes if we NOW log in as Admin (different group)
        ids = map(lambda x: x.getId(), gatewaywrapper.gateway.listProjects())
        assert project_id not in ids
        ##
        # Test listProjects as author (sees, owns)
        gatewaywrapper.loginAsAuthor()
        ids = map(lambda x: x.getId(), gatewaywrapper.gateway.listProjects())
        assert project_id in ids
        ids = map(lambda x: x.getId(), gatewaywrapper.gateway.listProjects())
        assert project_id in ids
        ##
        # Test listProjects as guest (does not see, does not own)
        gatewaywrapper.doLogin(gatewaywrapper.USER)
        ids = map(lambda x: x.getId(), gatewaywrapper.gateway.listProjects())
        assert project_id not in ids
        ids = map(lambda x: x.getId(), gatewaywrapper.gateway.listProjects())
        assert project_id not in ids
        ##
        # Test getProject
        gatewaywrapper.loginAsAuthor()
        assert gatewaywrapper.gateway.getObject("Project", project_id).getId() == project_id
        ##
        # Test getDataset
        dataset_id = parents[0].getId()
        assert gatewaywrapper.gateway.getObject("Dataset", dataset_id).getId() == dataset_id
        ##
        # Test listExperimenters
        #exps = map(lambda x: x.omeName, gatewaywrapper.gateway.listExperimenters())  # removed from blitz gateway
        exps = map(lambda x: x.omeName, gatewaywrapper.gateway.getObjects("Experimenter"))
        for omeName in (gatewaywrapper.USER.name, gatewaywrapper.AUTHOR.name, gatewaywrapper.ADMIN.name.decode('utf-8')):
            assert omeName in exps
            assert len(list(gatewaywrapper.gateway.getObjects("Experimenter", attributes={'omeName':omeName}))) > 0
        comboName = gatewaywrapper.USER.name+gatewaywrapper.AUTHOR.name+gatewaywrapper.ADMIN.name
        assert len(list(gatewaywrapper.gateway.getObjects("Experimenter", attributes={'omeName':comboName}))) ==  0
        ##
        # Test lookupExperimenter
        assert gatewaywrapper.gateway.getObject("Experimenter", attributes={'omeName':gatewaywrapper.USER.name}).omeName == gatewaywrapper.USER.name
        assert gatewaywrapper.gateway.getObject("Experimenter", attributes={'omeName':comboName}) is None
        ##
        # still logged in as Author, test listImages(ns)
        def listImages(ns=None):
            imageAnnLinks = gatewaywrapper.gateway.getAnnotationLinks("Image", ns=ns)
            return [omero.gateway.ImageWrapper(gatewaywrapper.gateway, link.parent) for link in imageAnnLinks]
        ns = 'weblitz.test_annotation'
        obj = gatewaywrapper.getTestImage()
        # Make sure it doesn't yet exist
        obj.removeAnnotations(ns)
        assert obj.getAnnotation(ns) ==  None
        # Check without the ann
        assert len(listImages(ns=ns)) ==  0
        annclass = omero.gateway.CommentAnnotationWrapper
        # createAndLink
        annclass.createAndLink(target=obj, ns=ns, val='foo')
        imgs = listImages(ns=ns)
        assert len(imgs) ==  1
        assert imgs[0] ==  obj
        # and clean up
        obj.removeAnnotations(ns)
        assert obj.getAnnotation(ns) ==  None

    def testCloseSession (self, gatewaywrapper):
        #74 the failed connection for a user not in the system group does not get closed
        gatewaywrapper.gateway.setIdentity(gatewaywrapper.USER.name, gatewaywrapper.USER.passwd)
        setprop = gatewaywrapper.gateway.c.ic.getProperties().setProperty
        map(lambda x: setprop(x[0],str(x[1])), gatewaywrapper.gateway._ic_props.items())
        gatewaywrapper.gateway.c.ic.getImplicitContext().put(omero.constants.GROUP, gatewaywrapper.gateway.group)
        # I'm not certain the following assertion is as intended.
        # This should be reviewed, see ticket #6037
        #assert gatewaywrapper.gateway._sessionUuid ==  None
        pytest.raises(omero.ClientError, gatewaywrapper.gateway._createSession)
        assert gatewaywrapper.gateway._sessionUuid !=  None
        #74 bug found while fixing this, the uuid passed to closeSession was not wrapped in rtypes, so logout didn't
        gatewaywrapper.gateway._closeSession() # was raising ValueError
        gatewaywrapper.gateway = None

    def testMiscellaneous (self, gatewaywrapper):
        gatewaywrapper.loginAsUser()
        assert gatewaywrapper.gateway.getUser().omeName ==  gatewaywrapper.USER.name

