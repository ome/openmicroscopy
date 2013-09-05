#!/usr/bin/env python
# -*- coding: utf-8 -*-

"""
   gateway tests - Users

   Copyright 2009-2013 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

   pytest fixtures used as defined in conftest.py:
   - gatewaywrapper

"""

import omero

from omero.gateway.scripts import dbhelpers

class TestUser (object):
    def testUsers (self, gatewaywrapper):
        gatewaywrapper.loginAsUser()
        # Try reconnecting without disconnect
        gatewaywrapper._has_connected = False
        gatewaywrapper.doConnect()
        gatewaywrapper.loginAsAuthor()
        gatewaywrapper.loginAsAdmin()

    def testSaveAs (self, gatewaywrapper):
        for u in (gatewaywrapper.AUTHOR, gatewaywrapper.ADMIN):
            # Test image should be owned by author
            gatewaywrapper.loginAsAuthor()
            image = gatewaywrapper.getTestImage(autocreate=True)
            ownername = image.getOwnerOmeName()
            # Now login as author or admin
            gatewaywrapper.doLogin(u)
            gatewaywrapper.gateway.SERVICE_OPTS.setOmeroGroup('-1')
            image = gatewaywrapper.getTestImage()
            assert ownername ==  gatewaywrapper.AUTHOR.name
            # Create some object
            param = omero.sys.Parameters()
            param.map = {'ns': omero.rtypes.rstring('weblitz.UserTest.testSaveAs')}
            anns = gatewaywrapper.gateway.getQueryService().findAllByQuery('from CommentAnnotation as a where a.ns=:ns', param)
            assert len(anns) ==  0
            gatewaywrapper.gateway.SERVICE_OPTS.setOmeroGroup()
            ann = omero.gateway.CommentAnnotationWrapper(conn=gatewaywrapper.gateway)
            ann.setNs(param.map['ns'].val)
            ann.setValue('foo')
            ann.saveAs(image.getDetails())

            # Annotations are owned by author
            gatewaywrapper.loginAsAuthor()
            try:
                anns = gatewaywrapper.gateway.getQueryService().findAllByQuery('from CommentAnnotation as a where a.ns=:ns', param)
                assert len(anns) ==  1
                assert omero.gateway.CommentAnnotationWrapper(gatewaywrapper.gateway, anns[0]).getOwnerOmeName(), gatewaywrapper.AUTHOR.name
            finally:
                gatewaywrapper.gateway.getUpdateService().deleteObject(ann._obj)
                anns = gatewaywrapper.gateway.getQueryService().findAllByQuery('from CommentAnnotation as a where a.ns=:ns', param)
                assert len(anns) ==  0

    def testCrossGroupSave (self, gatewaywrapper):
        gatewaywrapper.loginAsUser()
        uid = gatewaywrapper.gateway.getUserId()
        gatewaywrapper.loginAsAdmin()
        gatewaywrapper.gateway.SERVICE_OPTS.setOmeroGroup('-1')
        d = gatewaywrapper.getTestDataset()
        did = d.getId()
        g = d.getDetails().getGroup()
        admin = gatewaywrapper.gateway.getAdminService()
        admin.addGroups(omero.model.ExperimenterI(uid, False), [g._obj])
        gatewaywrapper.gateway.SERVICE_OPTS.setOmeroGroup('-1')
        # make sure the group is groupwrite enabled
        perms = str(d.getDetails().getGroup().getDetails().permissions)
        admin.changePermissions(g._obj, omero.model.PermissionsI('rwrw--'))
        d = gatewaywrapper.getTestDataset()
        g = d.getDetails().getGroup()
        assert g.getDetails().permissions.isGroupWrite()

        gatewaywrapper.loginAsUser()
        # User is now a member of the group to which testDataset belongs, which has groupWrite==True
        # But the default group for User is diferent
        try:
            gatewaywrapper.gateway.SERVICE_OPTS.setOmeroGroup('-1')
            d = gatewaywrapper.getTestDataset()
            did = d.getId()
            n = d.getName()
            d.setName(n+'_1')
            d.save()
            d = gatewaywrapper.gateway.getObject('dataset', did)
            assert d.getName() ==  n+'_1'
            d.setName(n)
            d.save()
            d = gatewaywrapper.gateway.getObject('dataset', did)
            assert d.getName() ==  n
        finally:
            gatewaywrapper.loginAsAdmin()
            admin = gatewaywrapper.gateway.getAdminService()
            # Revert group permissions and remove user from group
            admin.changePermissions(g._obj, omero.model.PermissionsI(perms))
            admin.removeGroups(omero.model.ExperimenterI(uid, False), [g._obj])

    def testCrossGroupRead (self, gatewaywrapper):
        gatewaywrapper.loginAsAuthor()
        u = gatewaywrapper.gateway.getUpdateService()
        p = gatewaywrapper.getTestProject()
        assert str(p.getDetails().permissions)[4] ==  '-'
        d = p.getDetails()
        g = d.getGroup()
        gatewaywrapper.loginAsUser()
        gatewaywrapper.gateway.SERVICE_OPTS.setOmeroGroup('-1')
        assert not g.getId() in gatewaywrapper.gateway.getEventContext().memberOfGroups
        assert gatewaywrapper.gateway.getObject('project', p.getId()) is None

    def testGroupOverObjPermissions (self, gatewaywrapper):
        """ Object accesss must be dependent only of group permissions """
        ns = 'omero.test.ns'
        # Author
        gatewaywrapper.loginAsAuthor()
        # create group with rw----
        # create project and annotation in that group
        p = dbhelpers.ProjectEntry('testAnnotationPermissions', None, create_group='testAnnotationPermissions', group_perms='rw----')
        try:
            p = p.create(gatewaywrapper.gateway)
        except dbhelpers.BadGroupPermissionsException:
            gatewaywrapper.loginAsAdmin()
            admin = gatewaywrapper.gateway.getAdminService()
            admin.changePermissions(admin.lookupGroup('testAnnotationPermissions'), omero.model.PermissionsI('rw----'))
            gatewaywrapper.loginAsAuthor()
            p = p.create(gatewaywrapper.gateway)
        pid = p.getId()
        g = p.getDetails().getGroup()._obj
        try:
            # Admin
            # add User to group
            gatewaywrapper.loginAsUser()
            uid = gatewaywrapper.gateway.getUserId()
            gatewaywrapper.loginAsAdmin()
            admin = gatewaywrapper.gateway.getAdminService()
            admin.addGroups(omero.model.ExperimenterI(uid, False), [g])
            # User
            # try to read project and annotation, which fails
            gatewaywrapper.loginAsUser()
            gatewaywrapper.gateway.SERVICE_OPTS.setOmeroGroup('-1')
            assert gatewaywrapper.gateway.getObject('project', pid) is None
            # Admin
            # Chmod project to rwrw--
            gatewaywrapper.loginAsAdmin()
            admin = gatewaywrapper.gateway.getAdminService()
            admin.changePermissions(g, omero.model.PermissionsI('rwrw--'))
            # Author
            # check project has proper permissions
            gatewaywrapper.loginAsAuthor()
            gatewaywrapper.gateway.SERVICE_OPTS.setOmeroGroup('-1')
            pa = gatewaywrapper.gateway.getObject('project', pid)
            assert pa !=  None
            # User
            # read project and annotation
            gatewaywrapper.loginAsUser()
            gatewaywrapper.gateway.SERVICE_OPTS.setOmeroGroup('-1')
            assert gatewaywrapper.gateway.getObject('project',  pid) is not None
        finally:
            gatewaywrapper.loginAsAuthor()
            handle = gatewaywrapper.gateway.deleteObjects('Project', [p.getId()], deleteAnns=True, deleteChildren=True)
            gatewaywrapper.waitOnCmd(gatewaywrapper.gateway.c, handle)

        
