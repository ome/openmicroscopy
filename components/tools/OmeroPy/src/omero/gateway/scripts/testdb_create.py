#!/usr/bin/env python

"""
   Library for gateway tests

   Copyright 2009 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

import unittest, os
import omero

from omero.gateway.scripts import dbhelpers

dbhelpers.USERS = {
    'user': dbhelpers.UserEntry('weblitz_test_user','foobar', 'User', 'Weblitz'),
    'author': dbhelpers.UserEntry('weblitz_test_author','foobar', 'Author', 'Weblitz'),
    }

dbhelpers.PROJECTS = {
    'testpr1' : dbhelpers.ProjectEntry('weblitz_test_priv_project', 'author'),
    'testpr2' : dbhelpers.ProjectEntry('weblitz_test_priv_project2', 'author'),
}

dbhelpers.DATASETS = {
    'testds1' : dbhelpers.DatasetEntry('weblitz_test_priv_dataset', 'testpr1'),
    'testds2' : dbhelpers.DatasetEntry('weblitz_test_priv_dataset2', 'testpr1'),
    'testds3' : dbhelpers.DatasetEntry('weblitz_test_priv_dataset3', 'testpr2'),
}

dbhelpers.IMAGES = {
    'testimg1' : dbhelpers.ImageEntry('weblitz_test_priv_image', 'imgs/CHOBI_d3d.dv', 'testds1'),
    'testimg2' : dbhelpers.ImageEntry('weblitz_test_priv_image2', 'imgs/CHOBI_d3d.dv', 'testds1'),
    'tinyimg' : dbhelpers.ImageEntry('weblitz_test_priv_image_tiny', 'imgs/tinyTest.d3d.dv', 'testds1'),
    'badimg' : dbhelpers.ImageEntry('weblitz_test_priv_image_bad', False, 'testds1'),
    'tinyimg2' : dbhelpers.ImageEntry('weblitz_test_priv_image_tiny2', 'imgs/tinyTest.d3d.dv', 'testds2'),
    'tinyimg3' : dbhelpers.ImageEntry('weblitz_test_priv_image_tiny3', 'imgs/tinyTest.d3d.dv', 'testds3'),
    'bigimg' : dbhelpers.ImageEntry('weblitz_test_priv_image_big', 'imgs/big.tiff', 'testds3'),
}


class GTest(unittest.TestCase):

    def setUp(self, skipTestDB=False):
        self.tmpfiles = []
        self._has_connected = False
        self.doDisconnect()
        self.USER = dbhelpers.USERS['user']
        self.AUTHOR = dbhelpers.USERS['author']
        self.ADMIN = dbhelpers.ROOT
        gateway = omero.client_wrapper()
        try:
            rp = gateway.getProperty('omero.rootpass')
            if rp:
                dbhelpers.ROOT.passwd = rp
        finally:
            gateway.seppuku()

        if not skipTestDB:
            self.prepTestDB()
            self.doDisconnect()

    def doConnect (self):
        if not self._has_connected:
            self.gateway.connect()
            self._has_connected = True
        self.assert_(self.gateway.isConnected(), 'Can not connect')
        self.failUnless(self.gateway.keepAlive(), 'Could not send keepAlive to connection')
    
    def doDisconnect(self):
        if self._has_connected and self.gateway:
            self.doConnect()
            self.gateway.seppuku()
            self.assert_(not self.gateway.isConnected(), 'Can not disconnect')
        self.gateway = None
        self._has_connected = False

    def doLogin (self, user=None, groupname=None):
        self.doDisconnect()
        if user:
            self.gateway = dbhelpers.login(user, groupname)
        else:
            self.gateway = dbhelpers.loginAsPublic()
        self.doConnect()

    def loginAsAdmin (self):
        self.doLogin(self.ADMIN)

    def loginAsAuthor (self):
        self.doLogin(self.AUTHOR)

    def loginAsUser (self):
        self.doLogin(self.USER)

    def loginAsPublic (self):
        self.doLogin()

    def tearDown(self):

        try:
            if self.gateway is not None:
                self.gateway.seppuku()
        finally:
            failure = False
            for tmpfile in self.tmpfiles:
                try:
                    tmpfile.close()
                except:
                    print "Error closing:"+tmpfile
        if failure:
           raise exceptions.Exception("Exception on client.closeSession")

    def getTestProject (self):
        return dbhelpers.getProject(self.gateway, 'testpr1')

    def getTestProject2 (self):
        return dbhelpers.getProject(self.gateway, 'testpr2')

    def getTestDataset (self, project=None):
        return dbhelpers.getDataset(self.gateway, 'testds1', project)

    def getTestDataset2 (self, project=None):
        return dbhelpers.getDataset(self.gateway, 'testds2', project)

    def getTestImage (self, dataset=None):
        return dbhelpers.getImage(self.gateway, 'testimg1', dataset)

    def getTestImage2 (self, dataset=None):
        return dbhelpers.getImage(self.gateway, 'testimg2', dataset)

    def getBadTestImage (self, dataset=None):
        return dbhelpers.getImage(self.gateway, 'badimg', dataset)

    def getTinyTestImage (self, dataset=None):
        return dbhelpers.getImage(self.gateway, 'tinyimg', dataset)

    def getTinyTestImage2 (self, dataset=None):
        return dbhelpers.getImage(self.gateway, 'tinyimg2', dataset)

    def getBigTestImage (self, dataset=None):
        return dbhelpers.getImage(self.gateway, 'bigimg', dataset)

    def prepTestDB (self):
        dbhelpers.bootstrap()

    def waitOnCmd(self, client, handle):
        callback = omero.callbacks.CmdCallbackI(client, handle)
        callback.loop(10, 500) # throws on timeout
        rsp = callback.getResponse()
        self.assert_(isinstance(rsp, omero.cmd.OK))
        return callback
        
        
