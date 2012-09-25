#!/usr/bin/env python

"""
   gateway tests - Users

   Copyright 2009 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

import unittest
import omero

import gatewaytest.library as lib
from omero.gateway.scripts import dbhelpers

class UserTest (lib.GTest):
    def testUsers (self):
        self.loginAsUser()
        # Try reconnecting without disconnect
        self._has_connected = False
        self.doConnect()
        self.loginAsAuthor()
        self.loginAsAdmin()

    def testSaveAs (self):
        for u in (self.AUTHOR, self.ADMIN):
            # Test image should be owned by author
            self.loginAsAuthor()
            image = self.getTestImage()
            ownername = image.getOwnerOmeName()
            # Now login as author or admin
            self.doLogin(u)
            self.gateway.SERVICE_OPTS.setOmeroGroup('-1')
            image = self.getTestImage()
            self.assertEqual(ownername, self.AUTHOR.name)
            # Create some object
            param = omero.sys.Parameters()
            param.map = {'ns': omero.rtypes.rstring('weblitz.UserTest.testSaveAs')}
            anns = self.gateway.getQueryService().findAllByQuery('from CommentAnnotation as a where a.ns=:ns', param)
            self.assertEqual(len(anns), 0)
            self.gateway.SERVICE_OPTS.setOmeroGroup()
            ann = omero.gateway.CommentAnnotationWrapper(conn=self.gateway)
            ann.setNs(param.map['ns'].val)
            ann.setValue('foo')
            ann.saveAs(image.getDetails())

            # Annotations are owned by author
            self.loginAsAuthor()
            try:
                anns = self.gateway.getQueryService().findAllByQuery('from CommentAnnotation as a where a.ns=:ns', param)
                self.assertEqual(len(anns), 1)
                self.assertEqual(omero.gateway.CommentAnnotationWrapper(self.gateway, anns[0]).getOwnerOmeName(), self.AUTHOR.name)
            finally:
                self.gateway.getUpdateService().deleteObject(ann._obj)
                anns = self.gateway.getQueryService().findAllByQuery('from CommentAnnotation as a where a.ns=:ns', param)
                self.assertEqual(len(anns), 0)

    def testCrossGroupSave (self):
        self.loginAsUser()
        uid = self.gateway.getUserId()
        self.loginAsAdmin()
        self.gateway.SERVICE_OPTS.setOmeroGroup('-1')
        d = self.getTestDataset()
        did = d.getId()
        g = d.getDetails().getGroup()
        admin = self.gateway.getAdminService()
        admin.addGroups(omero.model.ExperimenterI(uid, False), [g._obj])
        self.gateway.SERVICE_OPTS.setOmeroGroup('-1')
        # make sure the group is groupwrite enabled
        perms = str(d.getDetails().getGroup().getDetails().permissions)
        admin.changePermissions(g._obj, omero.model.PermissionsI('rwrw--'))
        d = self.getTestDataset()
        g = d.getDetails().getGroup()
        self.assert_(g.getDetails().permissions.isGroupWrite())

        self.loginAsUser()
        # User is now a member of the group to which testDataset belongs, which has groupWrite==True
        # But the default group for User is diferent
        try:
            self.gateway.SERVICE_OPTS.setOmeroGroup('-1')
            d = self.getTestDataset()
            did = d.getId()
            n = d.getName()
            d.setName(n+'_1')
            d.save()
            d = self.gateway.getObject('dataset', did)
            self.assertEqual(d.getName(), n+'_1')
            d.setName(n)
            d.save()
            d = self.gateway.getObject('dataset', did)
            self.assertEqual(d.getName(), n)
        finally:
            self.loginAsAdmin()
            admin = self.gateway.getAdminService()
            # Revert group permissions and remove user from group
            admin.changePermissions(g._obj, omero.model.PermissionsI(perms))
            admin.removeGroups(omero.model.ExperimenterI(uid, False), [g._obj])

    def testCrossGroupRead (self):
        self.loginAsAuthor()
        u = self.gateway.getUpdateService()
        p = self.getTestProject()
        self.assertEqual(str(p.getDetails().permissions)[4], '-')
        d = p.getDetails()
        g = d.getGroup()
        self.loginAsUser()
        self.gateway.SERVICE_OPTS.setOmeroGroup('-1')
        self.assert_(not g.getId() in self.gateway.getEventContext().memberOfGroups)
        self.assertEqual(self.gateway.getObject('project', p.getId()), None)

    def testGroupOverObjPermissions (self):
        """ Object accesss must be dependent only of group permissions """
        ns = 'omero.test.ns'
        # Author
        self.loginAsAuthor()
        # create group with rw----
        # create project and annotation in that group
        p = dbhelpers.ProjectEntry('testAnnotationPermissions', None, create_group='testAnnotationPermissions', group_perms='rw----')
        try:
            p = p.create(self.gateway)
        except dbhelpers.BadGroupPermissionsException:
            self.loginAsAdmin()
            admin = self.gateway.getAdminService()
            admin.changePermissions(admin.lookupGroup('testAnnotationPermissions'), omero.model.PermissionsI('rw----'))
            self.loginAsAuthor()
            p = p.create(self.gateway)
        pid = p.getId()
        g = p.getDetails().getGroup()._obj
        try:
            # Admin
            # add User to group
            self.loginAsUser()
            uid = self.gateway.getUserId()
            self.loginAsAdmin()
            admin = self.gateway.getAdminService()
            admin.addGroups(omero.model.ExperimenterI(uid, False), [g])
            # User
            # try to read project and annotation, which fails
            self.loginAsUser()
            self.gateway.SERVICE_OPTS.setOmeroGroup('-1')
            self.assertEqual(self.gateway.getObject('project', pid), None)
            # Admin
            # Chmod project to rwrw--
            self.loginAsAdmin()
            admin = self.gateway.getAdminService()
            admin.changePermissions(g, omero.model.PermissionsI('rwrw--'))
            # Author
            # check project has proper permissions
            self.loginAsAuthor()
            self.gateway.SERVICE_OPTS.setOmeroGroup('-1')
            pa = self.gateway.getObject('project', pid)
            self.assertNotEqual(pa, None)
            # User
            # read project and annotation
            self.loginAsUser()
            self.gateway.SERVICE_OPTS.setOmeroGroup('-1')
            self.assertNotEqual(self.gateway.getObject('project', pid), None)
        finally:
            self.loginAsAuthor()
            handle = self.gateway.deleteObjects('Project', [p.getId()], deleteAnns=True, deleteChildren=True)
            self.waitOnCmd(self.gateway.c, handle)

        
if __name__ == '__main__':
    unittest.main()
