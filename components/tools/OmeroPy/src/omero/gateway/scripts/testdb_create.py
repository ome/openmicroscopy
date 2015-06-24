#!/usr/bin/env python
# -*- coding: utf-8 -*-

"""
   Library for gateway tests

   Copyright 2009 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

import omero
from omero.rtypes import rstring

from omero.gateway.scripts import dbhelpers

dbhelpers.USERS = {
    'user': dbhelpers.UserEntry(
        'weblitz_test_user', 'foobar', 'User', 'Weblitz'),
    'author': dbhelpers.UserEntry(
        'weblitz_test_author', 'foobar', 'Author', 'Weblitz'),
}

dbhelpers.PROJECTS = {
    'testpr1': dbhelpers.ProjectEntry('weblitz_test_priv_project', 'author'),
    'testpr2': dbhelpers.ProjectEntry('weblitz_test_priv_project2', 'author'),
}

dbhelpers.DATASETS = {
    'testds1': dbhelpers.DatasetEntry('weblitz_test_priv_dataset', 'testpr1'),
    'testds2': dbhelpers.DatasetEntry('weblitz_test_priv_dataset2', 'testpr1'),
    'testds3': dbhelpers.DatasetEntry('weblitz_test_priv_dataset3', 'testpr2'),
}

dbhelpers.IMAGES = {
    'testimg1': dbhelpers.ImageEntry(
        'weblitz_test_priv_image', 'CHOBI_d3d.dv', 'testds1'),
    'testimg2': dbhelpers.ImageEntry(
        'weblitz_test_priv_image2', 'CHOBI_d3d.dv', 'testds1'),
    'tinyimg': dbhelpers.ImageEntry(
        'weblitz_test_priv_image_tiny', 'tinyTest.d3d.dv', 'testds1'),
    'badimg': dbhelpers.ImageEntry(
        'weblitz_test_priv_image_bad', False, 'testds1'),
    'tinyimg2': dbhelpers.ImageEntry(
        'weblitz_test_priv_image_tiny2', 'tinyTest.d3d.dv', 'testds2'),
    'tinyimg3': dbhelpers.ImageEntry(
        'weblitz_test_priv_image_tiny3', 'tinyTest.d3d.dv', 'testds3'),
    'bigimg': dbhelpers.ImageEntry(
        'weblitz_test_priv_image_big', 'big.tiff', 'testds3'),
}


class TestDBHelper(object):

    def setUp(self, skipTestDB=False, skipTestImages=True):
        self.tmpfiles = []
        self._has_connected = False
        self._last_login = None
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

        self.prepTestDB(onlyUsers=skipTestDB, skipImages=skipTestImages)
        self.doDisconnect()

    def doConnect(self):
        if not self._has_connected:
            self.gateway.connect()
            self._has_connected = True
        assert self.gateway.isConnected(), 'Can not connect'
        assert self.gateway.keepAlive(
        ), 'Could not send keepAlive to connection'
        self.gateway.setGroupForSession(
            self.gateway.getEventContext().memberOfGroups[0])

    def doDisconnect(self):
        if self._has_connected and self.gateway:
            self.doConnect()
            self.gateway.seppuku()
            assert not self.gateway.isConnected(), 'Can not disconnect'
        self.gateway = None
        self._has_connected = False
        self._last_login = None

    def doLogin(self, user=None, groupname=None):
        l = (user, groupname)
        if self._has_connected and self._last_login == l:
            return self.doConnect()
        self.doDisconnect()
        if user:
            self.gateway = dbhelpers.login(user, groupname)
        else:
            self.gateway = dbhelpers.loginAsPublic()
        self.doConnect()
        self._last_login = l

    def loginAsAdmin(self):
        self.doLogin(self.ADMIN)

    def loginAsAuthor(self):
        self.doLogin(self.AUTHOR)

    def loginAsUser(self):
        self.doLogin(self.USER)

    def loginAsPublic(self):
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
                    print "Error closing:" + tmpfile
        if failure:
            raise Exception("Exception on client.closeSession")

    def getTestProject(self):
        return dbhelpers.getProject(self.gateway, 'testpr1')

    def getTestProject2(self):
        return dbhelpers.getProject(self.gateway, 'testpr2')

    def getTestDataset(self, project=None):
        return dbhelpers.getDataset(self.gateway, 'testds1', project)

    def getTestDataset2(self, project=None):
        return dbhelpers.getDataset(self.gateway, 'testds2', project)

    def getTestImage(self, dataset=None, autocreate=False):
        return dbhelpers.getImage(self.gateway, 'testimg1', forceds=dataset,
                                  autocreate=autocreate)

    def getTestImage2(self, dataset=None):
        return dbhelpers.getImage(self.gateway, 'testimg2', dataset)

    def getBadTestImage(self, dataset=None, autocreate=False):
        return dbhelpers.getImage(self.gateway, 'badimg', forceds=dataset,
                                  autocreate=autocreate)

    def getTinyTestImage(self, dataset=None, autocreate=False):
        return dbhelpers.getImage(self.gateway, 'tinyimg', forceds=dataset,
                                  autocreate=autocreate)

    def getTinyTestImage2(self, dataset=None, autocreate=False):
        return dbhelpers.getImage(self.gateway, 'tinyimg2', forceds=dataset,
                                  autocreate=autocreate)

    def getTinyTestImage3(self, dataset=None, autocreate=False):
        return dbhelpers.getImage(self.gateway, 'tinyimg3', forceds=dataset,
                                  autocreate=autocreate)

    def getBigTestImage(self, dataset=None, autocreate=False):
        return dbhelpers.getImage(self.gateway, 'bigimg', forceds=dataset,
                                  autocreate=autocreate)

    def prepTestDB(self, onlyUsers=False, skipImages=True):
        dbhelpers.bootstrap(onlyUsers=onlyUsers, skipImages=skipImages)

    def waitOnCmd(self, client, handle):
        callback = omero.callbacks.CmdCallbackI(client, handle)
        callback.loop(10, 500)  # throws on timeout
        rsp = callback.getResponse()
        assert isinstance(rsp, omero.cmd.OK)
        return callback

    def createPDTree(self, project=None, dataset=None):
        """
        Create/link a Project and/or Dataset (link them if both are specified)
        Existing objects can be parsed as an omero.model object(s) or blitz
        Wrapper objects. Otherwise new objects will be created with name
        str(project) or str(dataset). If project OR dataset is specified, the
        ProjectWrapper or DatasetWrapper is returned. If both project and
        dataset are specified, they will be linked and the PD-link is returned
        as a BlitzObjectWrapper.

        @param project:     omero.model.ProjectDatasetLinkI
                            OR omero.gateway.ProjectWrapper
                            or name (string)
        @param dataset:     omero.model.DatasetI
                            OR omero.gateway.DatasetWrapper
                            or name (string)
        """
        dsId = ds = None
        prId = pr = None
        returnVal = None
        if dataset is not None:
            try:
                dsId = dataset.id
                dsId = dsId.val
            except:
                ds = omero.model.DatasetI()
                ds.name = rstring(str(dataset))
                ds = self.gateway.getUpdateService().saveAndReturnObject(ds)
                returnVal = omero.gateway.DatasetWrapper(self.gateway, ds)
                dsId = ds.id.val
        if project is not None:
            try:
                prId = project.id
                prId = prId.val
            except:
                pr = omero.model.ProjectI()
                pr.name = rstring(str(project))
                pr = self.gateway.getUpdateService().saveAndReturnObject(pr)
                returnVal = omero.gateway.ProjectWrapper(self.gateway, pr)
                prId = pr.id.val
        if dsId and prId:
            link = omero.model.ProjectDatasetLinkI()
            link.setParent(omero.model.ProjectI(prId, False))
            link.setChild(omero.model.DatasetI(dsId, False))
            link = self.gateway.getUpdateService().saveAndReturnObject(link)
            returnVal = omero.gateway.BlitzObjectWrapper(self.gateway, link)

        return returnVal

    def createTestImage(self, imageName="testImage", dataset=None, sizeX=16,
                        sizeY=16, sizeZ=1, sizeC=1, sizeT=1):
        """
        Creates a test image of the required dimensions, where each pixel
        value is set to the average value of x & y. If dataset (obj or name)
        is specified, will be linked to image. If project (obj or name) is
        specified, will be created/linked to dataset (if dataset not None)

        @param dataset:     omero.model.DatasetI
                            OR DatasetWrapper
                            OR dataset ID
        """
        from numpy import fromfunction, int16

        def f(x, y):
            return x

        def planeGen():
            for p in range(sizeZ * sizeC * sizeT):
                yield fromfunction(f, (sizeY, sizeX), dtype=int16)

        ds = None
        if dataset is not None:
            if hasattr(dataset, "_obj"):
                dataset = dataset._obj
            if isinstance(dataset, omero.model.DatasetI):
                ds = dataset
            else:
                try:
                    dsId = long(dataset)
                    ds = omero.model.DatasetI(dsId, False)
                except:
                    pass

        image = self.gateway.createImageFromNumpySeq(
            planeGen(), imageName, sizeZ=sizeZ, sizeC=sizeC, sizeT=sizeT,
            dataset=ds)
        return image
