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

"""

import pytest

import test.integration.library as lib

import omero

from omero.rtypes import rint

from test.integration.helpers import createTestImage


class TestThumbnailPerms(lib.ITest):

    def testThumbs(self):

        privateGroup = self.new_group(perms='rw----')
        readOnlyGroup = self.new_group(perms='rwr---')
        collaborativeGroup = self.new_group(perms='rwra--')

        #new user (group owner)
        newOwner = self.new_user(group=privateGroup)
        self.add_groups(newOwner, [readOnlyGroup, collaborativeGroup],
                        owner=True)

        #new user1
        user1 = self.new_user(group=privateGroup)
        self.add_groups(user1, [readOnlyGroup, collaborativeGroup])

        #new user2
        user2 = self.new_user(group=privateGroup)
        self.add_groups(user2, [readOnlyGroup, collaborativeGroup])

        ## login as user1 (into their default group)
        # create image in private group
        client_share1 = self.new_client(user=user1, password="ome")
        privateImageId = createTestImage(client_share1.sf)

        # if we don't get thumbnail, test fails when another user does
        self.getThumbnail(client_share1.sf, privateImageId)

        # change user into read-only group. Use object Ids for this,
        # NOT objects from a different context
        a = client_share1.sf.getAdminService()
        me = a.getExperimenter(a.getEventContext().userId)
        a.setDefaultGroup(me, omero.model.ExperimenterGroupI(readOnlyGroup.id.val, False))
        self.set_context(client_share1, readOnlyGroup.id.val)

        # create image and get thumbnail (in read-only group)
        readOnlyImageId = createTestImage(client_share1.sf)
        self.getThumbnail(client_share1.sf, readOnlyImageId)

        # change user into collaborative group. Use object Ids for this, NOT objects from a different context
        a.setDefaultGroup(me, omero.model.ExperimenterGroupI(collaborativeGroup.id.val, False))
        self.set_context(client_share1, collaborativeGroup.id.val)

        # create image and get thumbnail (in collaborative group)
        collaborativeImageId = createTestImage(client_share1.sf)
        self.getThumbnail(client_share1.sf, collaborativeImageId)

        # check that we can't get thumbnails for images in other groups
        assert self.getThumbnail(client_share1.sf, privateImageId) is None
        assert self.getThumbnail(client_share1.sf, readOnlyImageId) is None


        # now check that the 'owner' of each group can see all 3 thumbnails.
        ## login as owner (into private group)
        owner_client = self.new_client(user=newOwner, password="ome")

        group_ctx = {"omero.group": str(privateGroup)}
        self.getThumbnail(owner_client.sf, privateImageId, *group_ctx)
        # check that we can't get thumbnails for images in other groups
        assert self.getThumbnail(owner_client.sf, readOnlyImageId) is None
        assert self.getThumbnail(owner_client.sf, collaborativeImageId) is None

        # change owner into read-only group.
        o = client_share1.sf.getAdminService()
        me = o.getExperimenter(o.getEventContext().userId)
        o.setDefaultGroup(me, omero.model.ExperimenterGroupI(readOnlyGroup.id.val, False))
        self.set_context(owner_client, readOnlyGroup.id.val)

        self.getThumbnail(owner_client.sf, readOnlyImageId)
        # check that we can't get thumbnails for images in other groups
        assert self.getThumbnail(owner_client.sf, privateImageId) is None
        assert self.getThumbnail(owner_client.sf, collaborativeImageId) is None

        # change owner into collaborative group.
        o.setDefaultGroup(me, omero.model.ExperimenterGroupI(collaborativeGroup.id.val, False))
        self.set_context(owner_client, collaborativeGroup.id.val)

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
        u.setDefaultGroup(me, omero.model.ExperimenterGroupI(readOnlyGroup.id.val, False))
        self.set_context(user2_client, readOnlyGroup.id.val)

        self.getThumbnail(user2_client.sf, readOnlyImageId)
        # check that we can't get thumbnails for images in other groups
        assert self.getThumbnail(user2_client.sf, privateImageId) is None
        assert self.getThumbnail(user2_client.sf, collaborativeImageId) is None

        # change owner into collaborative group.
        u.setDefaultGroup(me, omero.model.ExperimenterGroupI(collaborativeGroup.id.val, False))
        self.set_context(user2_client, collaborativeGroup.id.val)

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

    def test12145ShareSettings(self):
        """
        Rendering settings should be shared when possible.
        Rather than regenerating the min/max per viewer,
        these should be used unless requested otherwise.
        """
        group = self.new_group(perms="rwra--")
        owner = self.new_client(group=group)
        other = self.new_client(group=group)

        def assert_exists(for_owner, for_other):
            for sf, exists in ((owner.sf, for_owner), (other.sf, for_other)):
                if exists:
                    assert sf.getPixelsService().retrieveRndSettings(pixels)
                else:
                    assert not sf.getPixelsService().retrieveRndSettings(pixels)

        # creation generates a first rendering image
        image = self.createTestImage(session=owner.sf)
        pixels = image.getPrimaryPixels().getId().getValue()

        owner_prx = owner.sf.createThumbnailStore()
        other_prx = other.sf.createThumbnailStore()

        # Before thumbnailing there should be no rendering settings
        assert_exists(True, False)

        owner_tb = owner_prx.getThumbnailByLongestSideSet(rint(16), [pixels])
        assert owner_tb[pixels] != ''

        other_tb = other_prx.getThumbnailByLongestSideSet(rint(16), [pixels])
        assert other_tb[pixels] != ''

        # After thumbnailing there should be no rendering settings
        assert_exists(True, False)
