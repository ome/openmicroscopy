#!/usr/bin/env python
# -*- coding: utf-8 -*-

#
# Copyright (C) 2013 University of Dundee & Open Microscopy Environment.
# All rights reserved.
#
# This program is free software; you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation; either version 2 of the License, or
# (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License along
# with this program; if not, write to the Free Software Foundation, Inc.,
# 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.

"""
   Integration test for getting thumbnails between members of groups.
   Testing permissions and thumbnail service on a running server. 

   dir(self)
   'assertAlmostEqual', 'assertAlmostEquals', 'assertEqual', 'assertEquals', 'assertFalse', 'assertNotAlmostEqual', 'assertNotAlmostEquals', 
   'assertNotEqual', 'assertNotEquals', 'assertRaises', 'assertTrue', 'assert_', 'client', 'countTestCases', 'debug', 'defaultTestResult', 
   'fail', 'failIf', 'failIfAlmostEqual', 'failIfEqual', 'failUnless', 'failUnlessAlmostEqual', 'failUnlessEqual', 'failUnlessRaises', 
   'failureException', 'id', 'login_args', 'new_user', 'query', 'root', 'run', 'setUp', 'sf', 'shortDescription', 'tearDown', 'testfoo', 
   'tmpfile', 'tmpfiles', 'update'
   
   ** Run from OmeroPy **
   PYTHONPATH=$PYTHONPATH:.:test:build/lib ICE_CONFIG=/Users/will/Documents/workspace/Omero/etc/ice.config python test/integration/thumbnailPerms.py
   
   
"""
import time
import test.integration.library as lib
import pytest
import omero
from omero.rtypes import rtime, rlong, rstring, rlist, rint
from omero_model_ExperimenterI import ExperimenterI
from omero_model_ExperimenterGroupI import ExperimenterGroupI
from omero_model_PermissionsI import PermissionsI

from test.integration.helpers import createTestImage

class TestThumbnailPerms(lib.ITest):


    def testThumbs(self):

        # root session is root.sf
        uuid = self.root.sf.getAdminService().getEventContext().sessionUuid
        admin = self.root.sf.getAdminService()

        group1name = "private_%s" % uuid
        group2name = "read-only_%s" % uuid
        group3name = "collaborative_%s" % uuid
        ownerName = "owner_%s" % uuid
        user1name = "user1_%s" % uuid
        user2name = "user2_%s" % uuid
        
        ### create three users in 3 groups
        listOfGroups = list()
        listOfGroups.append(admin.lookupGroup("user"))  # all users need to be in 'user' group to do anything! 
        
        #group1 - private
        new_gr1 = ExperimenterGroupI()
        new_gr1.name = rstring(group1name)
        p = PermissionsI('rw----')
        new_gr1.details.permissions = p
        gid = admin.createGroup(new_gr1)
        privateGroup = admin.getGroup(gid)
        assert 'rw----' ==  str(privateGroup.details.permissions)
        listOfGroups.append(privateGroup)
        
        #group2 - read-only
        new_gr2 = ExperimenterGroupI()
        new_gr2.name = rstring(group2name)
        p2 = PermissionsI('rwr---')
        new_gr2.details.permissions = p2
        gid2 = admin.createGroup(new_gr2)
        readOnlyGroup = admin.getGroup(gid2)
        assert 'rwr---' ==  str(readOnlyGroup.details.permissions)
        listOfGroups.append(readOnlyGroup)
        
        #group3 - read-annotate
        new_gr3 = ExperimenterGroupI()
        new_gr3.name = rstring(group3name)
        p = PermissionsI('rwra--')
        new_gr3.details.permissions = p
        gid3 = admin.createGroup(new_gr3)
        collaborativeGroup = admin.getGroup(gid3)
        assert 'rwra--' ==  str(collaborativeGroup.details.permissions)
        listOfGroups.append(collaborativeGroup)
        
        #new user (group owner)
        owner = ExperimenterI()
        owner.omeName = rstring(ownerName)
        owner.firstName = rstring("Group")
        owner.lastName = rstring("Owner")
        owner.email = rstring("owner@emaildomain.com")
        
        ownerId = admin.createExperimenterWithPassword(owner, rstring("ome"), privateGroup, listOfGroups)
        newOwner = admin.getExperimenter(ownerId) 
        admin.setGroupOwner(privateGroup, newOwner) 
        admin.setGroupOwner(readOnlyGroup, newOwner) 
        admin.setGroupOwner(collaborativeGroup, newOwner) 
        
        #new user1
        new_exp = ExperimenterI()
        new_exp.omeName = rstring(user1name)
        new_exp.firstName = rstring("Will")
        new_exp.lastName = rstring("Moore")
        new_exp.email = rstring("newtest@emaildomain.com")
        
        eid = admin.createExperimenterWithPassword(new_exp, rstring("ome"), privateGroup, listOfGroups)
        
        #new user2
        new_exp2 = ExperimenterI()
        new_exp2.omeName = rstring(user2name)
        new_exp2.firstName = rstring("User")
        new_exp2.lastName = rstring("Test2")
        new_exp2.email = rstring("newtest2@emaildomain.com")
        
        eid2 = admin.createExperimenterWithPassword(new_exp2, rstring("ome"), privateGroup, listOfGroups)
        
        ## get users
        user1 = admin.getExperimenter(eid)
        user2 = admin.getExperimenter(eid2)
        
        ## login as user1 (into their default group)
        client_share1 = self.new_client(user=user1, password="ome")

        print len(client_share1.sf.activeServices())
        
        # create image in private group
        privateImageId = createTestImage(client_share1.sf)
        print len(client_share1.sf.activeServices())
        
        self.getThumbnail(client_share1.sf, privateImageId)    # if we don't get thumbnail, test fails when another user does
        print len(client_share1.sf.activeServices())
        
        # change user into read-only group. Use object Ids for this, NOT objects from a different context
        a = client_share1.sf.getAdminService()
        me = a.getExperimenter(a.getEventContext().userId)
        a.setDefaultGroup(me, omero.model.ExperimenterGroupI(gid2, False))

        self.set_context(client_share1, gid2)
        #print a.getEventContext()
        
        # create image and get thumbnail (in read-only group)
        readOnlyImageId = createTestImage(client_share1.sf)
        self.getThumbnail(client_share1.sf, readOnlyImageId)
        
        # change user into collaborative group. Use object Ids for this, NOT objects from a different context
        a.setDefaultGroup(me, omero.model.ExperimenterGroupI(gid3, False))
        self.set_context(client_share1, gid3)
        
        # create image and get thumbnail (in collaborative group)
        collaborativeImageId = createTestImage(client_share1.sf)
        self.getThumbnail(client_share1.sf, collaborativeImageId)
        
        # check that we can't get thumbnails for images in other groups
        assert self.getThumbnail(client_share1.sf, privateImageId) is None
        assert self.getThumbnail(client_share1.sf, readOnlyImageId) is None


        # now check that the 'owner' of each group can see all 3 thumbnails.
        ## login as owner (into private group)
        owner_client = self.new_client(user=newOwner, password="ome")
        
        self.getThumbnail(owner_client.sf, privateImageId)
        # check that we can't get thumbnails for images in other groups
        assert self.getThumbnail(owner_client.sf, readOnlyImageId) is None
        assert self.getThumbnail(owner_client.sf, collaborativeImageId) is None

        # change owner into read-only group.
        o = client_share1.sf.getAdminService()
        me = o.getExperimenter(o.getEventContext().userId)
        o.setDefaultGroup(me, omero.model.ExperimenterGroupI(gid2, False))
        self.set_context(owner_client, gid2)

        self.getThumbnail(owner_client.sf, readOnlyImageId)
        # check that we can't get thumbnails for images in other groups
        assert self.getThumbnail(owner_client.sf, privateImageId) is None
        assert self.getThumbnail(owner_client.sf, collaborativeImageId) is None

        # change owner into collaborative group.
        o.setDefaultGroup(me, omero.model.ExperimenterGroupI(gid3, False))
        self.set_context(owner_client, gid3)
        
        self.getThumbnail(owner_client.sf, collaborativeImageId)
        # check that we can't get thumbnails for images in other groups
        assert self.getThumbnail(owner_client.sf, privateImageId) is None
        assert self.getThumbnail(owner_client.sf, readOnlyImageId) is None


        # now check that the 'user2' of each group can see all thumbnails except private.
        ## login as user2 (into private group)
        user2_client = self.new_client(user=user2, password="ome")
        
        # check that we can't get thumbnails for any images in private group
        assert self.getThumbnail(user2_client.sf, privateImageId) is None
        assert self.getThumbnail(user2_client.sf, readOnlyImageId) is None
        assert self.getThumbnail(user2_client.sf, collaborativeImageId) is None

        # change owner into read-only group.
        u = user2_client.sf.getAdminService()
        me = u.getExperimenter(u.getEventContext().userId)
        u.setDefaultGroup(me, omero.model.ExperimenterGroupI(gid2, False))
        self.set_context(user2_client, gid2)

        self.getThumbnail(user2_client.sf, readOnlyImageId)
        # check that we can't get thumbnails for images in other groups
        assert self.getThumbnail(user2_client.sf, privateImageId) is None
        assert self.getThumbnail(user2_client.sf, collaborativeImageId) is None

        # change owner into collaborative group.
        u.setDefaultGroup(me, omero.model.ExperimenterGroupI(gid3, False))
        self.set_context(user2_client, gid3)
        
        self.getThumbnail(user2_client.sf, collaborativeImageId)
        # check that we can't get thumbnails for images in other groups
        assert self.getThumbnail(user2_client.sf, privateImageId) is None
        assert self.getThumbnail(user2_client.sf, readOnlyImageId) is None

    def test9070(self):

        # Create private group with two member and one image
        group = self.new_group(perms="rw__--")
        owner = self.new_client(group=group, admin=True) # Owner of group
        member = self.new_client(group=group) # Member of group
        privateImage = self.createTestImage(session=member.sf)
        pId = privateImage.getPrimaryPixels().getId().getValue()

        ## using owner session access thumbnailStore
        thumbnailStore = owner.sf.createThumbnailStore()
        s = thumbnailStore.getThumbnailByLongestSideSet(rint(16), [pId])
        assert s[pId] != ''

        s = thumbnailStore.getThumbnailSet(rint(16), rint(16), [pId])
        assert s[pId] != ''

    def getThumbnail(self, session, imageId, *ctx):

        thumbnailStore = session.createThumbnailStore()
    
        image = session.getQueryService().findByQuery(
            "select i from Image as i " \
            "join fetch i.pixels where i.id = '%d'" % imageId, None)
        if image is None:
            return None
        pId = image.getPrimaryPixels().getId().getValue()
    
        pixelsIds = [pId]
        s = thumbnailStore.getThumbnailByLongestSideSet(rint(16), pixelsIds)
        assert 1 == len(s)
        s = thumbnailStore.getThumbnailSet(rint(16), rint(16), pixelsIds)
        assert 1 == len(s)

        thumbnailStore.setPixelsId(pId)
        t = thumbnailStore.getThumbnail(rint(16),rint(16), *ctx)
        assert t
        t = thumbnailStore.getThumbnailByLongestSide(rint(16))
        assert t

        thumbnailStore.close()
        return t

    def assert10618(self, group, tester, preview, *ctx):

        # Create user in group with one image
        owner = self.new_client(group=group)
        privateImage = self.createTestImage(session=owner.sf)
        pId = privateImage.getPrimaryPixels().getId().getValue()

        if preview:
            # As the user, load the thumbnails
            owner_tb_prx = owner.sf.createThumbnailStore()
            s = owner_tb_prx.getThumbnailByLongestSideSet(rint(16), [pId])
            assert s[pId] != ''

        # As the tester, try to get a thumbnail
        tb_prx = tester.sf.createThumbnailStore()

        s = tb_prx.getThumbnailByLongestSideSet(rint(16), [pId], *ctx)
        assert s[pId] != ''

        s = tb_prx.getThumbnailSet(rint(16), rint(16), [pId], *ctx)
        assert s[pId] != ''

    def testPrivate10618RootWithGrpCtx(self):
        group = self.new_group(perms="rw----")
        grp_ctx = {"omero.group": str(group.id.val)}
        self.assert10618(group, self.root, True, grp_ctx)

    def testPrivate10618RootWithGrpCtxButNoLoad(self):
        group = self.new_group(perms="rw----")
        grp_ctx = {"omero.group": str(group.id.val)}
        self.assert10618(group, self.root, False, grp_ctx)

    def testReadOnly10618RootWithGrpCtx(self):
        group = self.new_group(perms="rwr---")
        grp_ctx = {"omero.group": str(group.id.val)}
        self.assert10618(group, self.root, True, grp_ctx)

    def testReadOnly10618RootWithGrpCtxButNoLoad(self):
        group = self.new_group(perms="rwr---")
        grp_ctx = {"omero.group": str(group.id.val)}
        self.assert10618(group, self.root, False, grp_ctx)

    def testReadOnly10618MemberWithGrpCtxButNoLoad(self):
        group = self.new_group(perms="rwr---")
        member = self.new_client(group=group)
        grp_ctx = {"omero.group": str(group.id.val)}
        self.assert10618(group, member, False, grp_ctx)

    @pytest.mark.xfail(reason="requires thumbnail work")
    def testPrivate10618RootWithNoCtx(self):
        """
        This would require the server to try omero.group=-1
        for the user.
        """
        group = self.new_group(perms="rw----")
        self.assert10618(group, self.root, True)
