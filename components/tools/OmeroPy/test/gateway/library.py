#!/usr/bin/env python

"""
   Library for gateway tests

   Copyright 2009 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

import unittest, os
import omero

from omero.rtypes import rstring, rtime

print omero

USER=('weblitz_test_user','foobar')
USER_NAME=('User', 'Weblitz')
AUTHOR=('weblitz_test_author','foobar')
AUTHOR_NAME=('Author', 'Weblitz')
EDITOR=('weblitz_test_editor','foobar')
EDITOR_NAME=('Editor', 'Weblitz')

PUBLIC_PREFIX='weblitz_test_pub'
PRIVATE_PREFIX='weblitz_test_priv'

class GTest(unittest.TestCase):

    def setUp(self):
        self.tmpfiles = []
        self._has_connected = False
        self.doDisconnect()
        self.USER = USER
        self.AUTHOR = AUTHOR
        self.ADMIN = ('root', self.gateway.getProperty('omero.rootpass'))
        self.prepTestDB()
        self.doDisconnect()
#        # Create a client for lookup
#        lookup = omero.client({"Ice.Default.Locator":"tcp"})
#        rootpass = lookup.getProperty("omero.rootpass")
#        if rootpass:
#            self.root = omero.client()
#            self.root.createSession("root",rootpass)
#            newuser = self.new_user()
#            self.client = omero.client()
#            self.sf = self.client.createSession(newuser.omeName.val, "1")
#        else:
#            self.root = None
#            self.client = omero.client()
#            self.sf = self.client.createSession()
#        self.update = self.sf.getUpdateService()
#        self.query = self.sf.getQueryService()

#    def tmpfile(self):
#        tmpfile = tempfile.NamedTemporaryFile(mode='w+t')
#        self.tmpfiles.append(tmpfile)
#        return tmpfile
#
#    def new_user(self, group = None):
#
#        if not self.root:
#            raise exceptions.Exception("No root client. Cannot create user")
#
#        admin = self.root.getSession().getAdminService()
#        name = str(uuid())
#
#        # Create group if necessary
#        if not group:
#            group = name
#            g = omero.model.ExperimenterGroupI()
#            g.name = rstring(group)
#            gid = admin.createGroup(g)
#            g = omero.model.ExperimenterGroupI(gid, False)
#
#        # Create user
#        e = omero.model.ExperimenterI()
#        e.omeName = rstring(name)
#        e.firstName = rstring(name)
#        e.lastName = rstring(name)
#        uid = admin.createUser(e, group)
#        return admin.getExperimenter(uid)

    def doConnect (self):
        if not self._has_connected:
            self.gateway.connect()
            self._has_connected = True
        self.assert_(self.gateway.isConnected(), 'Can not connect')
        self.failUnless(self.gateway.keepAlive(), 'Could not send keepAlive to connection')
    
    def doLogin (self, user, passwd):
        if self._has_connected:
            self.doDisconnect()
        self.gateway.setIdentity(user, passwd)
        self.assertEqual(self.gateway._ic_props[omero.constants.USERNAME], user)
        self.assertEqual(self.gateway._ic_props[omero.constants.PASSWORD], passwd)
        self.doConnect()
    
    def doDisconnect(self):
        if self._has_connected:
            self.doConnect()
            self.gateway.seppuku()
            self.assert_(not self.gateway.isConnected(), 'Can not disconnect')
        self.gateway = omero.client_wrapper(group='system', try_super=True)
        self.assert_(self.gateway, 'Can not get gateway from connection')
        self._has_connected = False

    def loginAsAdmin (self):
        self.doLogin(*self.ADMIN)

    def loginAsAuthor (self):
        self.doLogin(*AUTHOR)

    def loginAsEditor (self):
        self.doLogin(*EDITOR)

    def loginAsUser (self):
        self.doLogin(*USER)

    def tearDown(self):
        failure = False
#        try:
#            self.client.closeSession()
#        except:
#            traceback.print_exc()
#            failure = True
#        if self.root:
#            try:
#                self.root.closeSession()
#            except:
#                traceback.print_exc()
#                failure = True
        for tmpfile in self.tmpfiles:
            try:
                tmpfile.close()
            except:
                print "Error closing:"+tmpfile
        if failure:
           raise exceptions.Exception("Exception on client.closeSession")

    def _createGroup (self, name):
        a = self.gateway.getAdminService()
        g = omero.model.ExperimenterGroupI()
        g.setName(rstring(name))
        a.createGroup(g)
        return a.lookupGroup(name)

    def _createUser (self, omename, firstname, lastname, passwd, groupname, system=False):
        a = self.gateway.getAdminService()
        try:
            a.lookupExperimenter(omename)
            return
        except:
            pass
        try:
            g = a.lookupGroup(groupname)
        except:
            g = omero.model.ExperimenterGroupI()
            g.setName(rstring(groupname))
            a.createGroup(g)
            g = a.lookupGroup(groupname)
        u = omero.model.ExperimenterI()
        u.setOmeName(rstring(omename))
        u.setFirstName(rstring(firstname))
        u.setLastName(rstring(lastname))
        a.createUser(u, g.getName().val)
        if system:
            u =a.lookupExperimenter(omename)
            a.addGroups(u,(a.lookupGroup("system"),))
        a.changeUserPassword(u.getOmeName().val, rstring(passwd))
        print "Created user: %s" % omename

    def _testProjectName (self, public=False):
        return (public and PUBLIC_PREFIX or PRIVATE_PREFIX) + '_project'

    def _testDatasetName (self, public=False):
        return (public and PUBLIC_PREFIX or PRIVATE_PREFIX) + '_dataset'

    def _testDataset2Name (self, public=False):
        return self._testDatasetName(public) + '2'

    def _testImageName (self, public=False):
        return (public and PUBLIC_PREFIX or PRIVATE_PREFIX) + '_image'

    def _testImageDescription (self, public=False):
        return self._testImageName(public) + ' by ' + ' '.join(AUTHOR_NAME)

    def getTestProject (self, public=False):
        name = self._testProjectName(public)
        for p in self.gateway.listProjects():
            if p.getName() == name:
                return p
        return None

    def _getTestDataset (self, name, project=None, public=False):
        if project is None:
            project = self.getTestProject(public=public)
        for d in project.listChildren():
            if d.getName() == name:
                return d
        return None

    def getTestDataset (self, project=None, public=False):
        return self._getTestDataset(self._testDatasetName(), project, public)

    def getTestDataset2 (self, project=None, public=False):
        return self._getTestDataset(self._testDataset2Name(), project, public)

    def _getTestImage (self, name, dataset=None, public=False):
        if dataset is None:
            dataset = self.getTestDataset(public=public)
        for i in dataset.listChildren():
            if i.getName() == name:
                return i
        return None

    def getTestImage (self, dataset=None, public=False):
        return self._getTestImage(self._testImageName(public), dataset, public)

    def getTestImage2 (self, dataset=None, public=False):
        return self._getTestImage(self._testImageName(public)+'2', dataset, public)

    def getBadTestImage (self, dataset=None, public=False):
        return self._getTestImage(self._testImageName(public) + '_bad', dataset, public)

    def getTinyTestImage (self, dataset=None, public=False):
        return self._getTestImage(self._testImageName(public) + '_tiny', dataset, public)

    def assertTestGraph (self, public=False):
        p = self.getTestProject(public=public)
        if p is None:
            name = self._testProjectName(public)
            p = omero.model.ProjectI(loaded=True)
            p.setName(rstring(name))
            p.setDescription(rstring(name))
            p = omero.gateway.ProjectWrapper(self.gateway, self.gateway.getUpdateService().saveAndReturnObject(p))
            print "created project #%i" % p.id
        else:
            p.__loadedHotSwap__()
        d = self.getTestDataset(p, public=public)
        if d is None:
            name = self._testDatasetName(public)
            d = omero.model.DatasetI(loaded=True)
            d.setName(rstring(name))
            d.setDescription(rstring(name))
            p.linkDataset(d)
            p.save()
            d = self.getTestDataset(p, public=public)
            print "created dataset #%i" % d.id
        d2 = self.getTestDataset2(p, public=public)
        if d2 is None:
            name = self._testDataset2Name(public)
            d2 = omero.model.DatasetI(loaded=True)
            d2.setName(rstring(name))
            d2.setDescription(rstring(name))
            p.linkDataset(d2)
            p.save()
            d2 = self.getTestDataset2(p, public=public)
            print "created dataset2 #%i" % d2.id
        return (p,d,d2)

    def _putTestImage (self, name, filename, dataset=None, public=False):
        if dataset is None:
            project, dataset, d2 = self.assertTestGraph(public)
        img = self._getTestImage(name, dataset, public=public)
        if img is None:
            fpath = os.path.join(os.path.dirname(os.path.abspath(__file__)), 'testimgs', filename)
            if not os.path.exists(fpath):
                raise IOError('No such file %s' % fpath)
            port = self.gateway.getProperty('omero.port') or '4063'
            exe = 'PATH=$PATH:../bin omero import -s localhost -u %s -w %s -d %i -p %s -n %s %s' % (AUTHOR[0], AUTHOR[1], dataset.getId(), port, name, fpath)
            os.system(exe)
            img = self._getTestImage(name, dataset, public=public)
            img.setDescription(rstring(self._testImageDescription(public)))
        return img

    def putTestImage (self, dataset=None, public=False):
        self._putTestImage(self._testImageName(public), 'CHOBI_d3d.dv', dataset, public)

    def putTestImage2 (self, dataset=None, public=False):
        img = self._putTestImage(self._testImageName(public)+'2', 'CHOBI_d3d.dv', dataset, public)
        img._loadPixels()
        p = img.getPrimaryPixels()
        if p._physicalSizeX is not None:
            p._physicalSizeX=None
            p._physicalSizeY=None
            p._physicalSizeZ=None
        img.save()

    def putTinyTestImage (self, dataset=None, public=False):
        self._putTestImage(self._testImageName(public) + '_tiny', 'tinyTest.d3d.dv', dataset, public)

    def putBadTestImage (self, dataset=None, public=False):
        if dataset is None:
            project, dataset, d2 = self.assertTestGraph(public)
        img = self.getBadTestImage(dataset, public=public)
        if img is None:
            name = self._testImageName(public) + '_bad'
            img = omero.model.ImageI()
            img.setName(rstring(name))
            img.setAcquisitionDate(rtime(0))
            if not dataset.imageLinksLoaded:
                dataset._obj._imageLinksSeq = []
                dataset._obj._imageLinksLoaded = True;
            #    dataset._toggleCollectionsLoaded(True)
            dataset.linkImage(img)
            dataset.save()
            img = self.getBadTestImage(dataset, public=public)
            print "created bad image #%i" % img.id

    def prepTestDB (self):
        self.loginAsAdmin()
        self._createUser(USER[0], USER_NAME[0], USER_NAME[1], USER[1], '%s_group' % USER[0], system=False)
        self._createUser(AUTHOR[0], AUTHOR_NAME[0], AUTHOR_NAME[1], AUTHOR[1], '%s_group' % AUTHOR[0], system=False)
        self._createUser(EDITOR[0], EDITOR_NAME[0], EDITOR_NAME[1], EDITOR[1], '%s_group' % EDITOR[0], system=True)
        self.loginAsAuthor()
        p,d,d2 = self.assertTestGraph(public=False)
        self.putBadTestImage(dataset=d, public=False)
        self.putTestImage(dataset=d, public=False)
        self.putTinyTestImage(dataset=d, public=False)
        self.putTestImage2(dataset=d, public=False)
        self.putTinyTestImage(dataset=d2, public=False)
        
        
