#!/usr/bin/env python
# -*- coding: utf-8 -*-

#
# Copyright (C) 2013-2014 University of Dundee & Open Microscopy Environment.
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

import Ice
import pytest
import library as lib
import omero

from omero.model import ExperimenterGroupI
from omero.rtypes import rint
from test.integration.helpers import createTestImage


class TestThumbnailPerms(lib.ITest):

    def testThumbs(self):

        privateGroup = self.new_group(perms='rw----')
        readOnlyGroup = self.new_group(perms='rwr---')
        collaborativeGroup = self.new_group(perms='rwra--')

        # new user (group owner)
        newOwner = self.new_user(group=privateGroup)
        self.add_groups(newOwner, [readOnlyGroup, collaborativeGroup],
                        owner=True)

        # new user1
        user1 = self.new_user(group=privateGroup)
        self.add_groups(user1, [readOnlyGroup, collaborativeGroup])

        # new user2
        user2 = self.new_user(group=privateGroup)
        self.add_groups(user2, [readOnlyGroup, collaborativeGroup])

        # login as user1 (into their default group)
        # create image in private group
        client_share1 = self.new_client(
            user=user1, password=user1.omeName.val)
        privateImageId = createTestImage(client_share1.sf)

        # if we don't get thumbnail, test fails when another user does
        self.getThumbnail(client_share1.sf, privateImageId)

        # change user into read-only group. Use object Ids for this,
        # NOT objects from a different context
        a = client_share1.sf.getAdminService()
        me = a.getExperimenter(a.getEventContext().userId)
        a.setDefaultGroup(me, ExperimenterGroupI(readOnlyGroup.id.val, False))
        self.set_context(client_share1, readOnlyGroup.id.val)

        # create image and get thumbnail (in read-only group)
        readOnlyImageId = createTestImage(client_share1.sf)
        self.getThumbnail(client_share1.sf, readOnlyImageId)

        # change user into collaborative group. Use object Ids for this, NOT
        # objects from a different context
        a.setDefaultGroup(
            me, ExperimenterGroupI(collaborativeGroup.id.val, False))
        self.set_context(client_share1, collaborativeGroup.id.val)

        # create image and get thumbnail (in collaborative group)
        collaborativeImageId = createTestImage(client_share1.sf)
        self.getThumbnail(client_share1.sf, collaborativeImageId)

        # check that we can't get thumbnails for images in other groups
        assert self.getThumbnail(client_share1.sf, privateImageId) is None
        assert self.getThumbnail(client_share1.sf, readOnlyImageId) is None

        # now check that the 'owner' of each group can see all 3 thumbnails.
        # login as owner (into private group)
        owner_client = self.new_client(
            user=newOwner, password=newOwner.omeName.val)

        group_ctx = {"omero.group": str(privateGroup)}
        self.getThumbnail(owner_client.sf, privateImageId, *group_ctx)
        # check that we can't get thumbnails for images in other groups
        assert self.getThumbnail(owner_client.sf, readOnlyImageId) is None
        assert self.getThumbnail(owner_client.sf, collaborativeImageId) is None

        # change owner into read-only group.
        o = client_share1.sf.getAdminService()
        me = o.getExperimenter(o.getEventContext().userId)
        o.setDefaultGroup(
            me, omero.model.ExperimenterGroupI(readOnlyGroup.id.val, False))
        self.set_context(owner_client, readOnlyGroup.id.val)

        self.getThumbnail(owner_client.sf, readOnlyImageId)
        # check that we can't get thumbnails for images in other groups
        assert self.getThumbnail(owner_client.sf, privateImageId) is None
        assert self.getThumbnail(owner_client.sf, collaborativeImageId) is None

        # change owner into collaborative group.
        o.setDefaultGroup(
            me,
            omero.model.ExperimenterGroupI(collaborativeGroup.id.val, False))
        self.set_context(owner_client, collaborativeGroup.id.val)

        self.getThumbnail(owner_client.sf, collaborativeImageId)
        # check that we can't get thumbnails for images in other groups
        assert self.getThumbnail(owner_client.sf, privateImageId) is None
        assert self.getThumbnail(owner_client.sf, readOnlyImageId) is None

        # now check that the 'user2' of each group can see all thumbnails
        # except private. login as user2 (into private group)
        user2_client = self.new_client(user=user2, password=user2.omeName.val)

        # check that we can't get thumbnails for any images in private group
        assert self.getThumbnail(user2_client.sf, privateImageId) is None
        assert self.getThumbnail(user2_client.sf, readOnlyImageId) is None
        assert self.getThumbnail(user2_client.sf, collaborativeImageId) is None

        # change owner into read-only group.
        u = user2_client.sf.getAdminService()
        me = u.getExperimenter(u.getEventContext().userId)
        u.setDefaultGroup(
            me, ExperimenterGroupI(readOnlyGroup.id.val, False))
        self.set_context(user2_client, readOnlyGroup.id.val)

        self.getThumbnail(user2_client.sf, readOnlyImageId)
        # check that we can't get thumbnails for images in other groups
        assert self.getThumbnail(user2_client.sf, privateImageId) is None
        assert self.getThumbnail(user2_client.sf, collaborativeImageId) is None

        # change owner into collaborative group.
        u.setDefaultGroup(
            me, ExperimenterGroupI(collaborativeGroup.id.val, False))
        self.set_context(user2_client, collaborativeGroup.id.val)

        self.getThumbnail(user2_client.sf, collaborativeImageId)
        # check that we can't get thumbnails for images in other groups
        assert self.getThumbnail(user2_client.sf, privateImageId) is None
        assert self.getThumbnail(user2_client.sf, readOnlyImageId) is None

    def test9070(self):

        # Create private group with two member and one image
        group = self.new_group(perms="rw__--")
        owner = self.new_client(group=group, owner=True)  # Owner of group
        member = self.new_client(group=group)  # Member of group
        privateImage = self.createTestImage(session=member.sf)
        pId = privateImage.getPrimaryPixels().getId().getValue()

        # using owner session access thumbnailStore
        thumbnailStore = owner.sf.createThumbnailStore()
        s = thumbnailStore.getThumbnailByLongestSideSet(rint(16), [pId])
        assert s[pId] != ''

        s = thumbnailStore.getThumbnailSet(rint(16), rint(16), [pId])
        assert s[pId] != ''

    def getThumbnail(self, session, imageId, *ctx):

        thumbnailStore = session.createThumbnailStore()

        image = session.getQueryService().findByQuery(
            "select i from Image as i "
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
        t = thumbnailStore.getThumbnail(rint(16), rint(16), *ctx)
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

    @pytest.mark.broken(reason="requires thumbnail work")
    def testPrivate10618RootWithNoCtx(self):
        """
        This would require the server to try omero.group=-1
        for the user.
        """
        group = self.new_group(perms="rw----")
        self.assert10618(group, self.root, True)

    def test12145ShareSettingsThumbs(self):
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
                    id = sf.getAdminService().getEventContext().userId
                    rnd = sf.getPixelsService().retrieveRndSettingsFor(
                        pixels, id)
                    assert rnd is not None
                else:
                    id = sf.getAdminService().getEventContext().userId
                    rnd = sf.getPixelsService().retrieveRndSettingsFor(
                        pixels, id)
                    assert rnd is None

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

    def test12145ShareSettingsThumbsReadOnly(self):
        """
        Rendering settings should be shared when possible.
        Rather than regenerating the min/max per viewer,
        these should be used unless requested otherwise.
        """
        group = self.new_group(perms="rwr---")
        groupOwner = self.new_user(group=group, owner=True)
        owner = self.new_client(group=group)
        other = self.new_client(user=groupOwner, group=group)

        def assert_exists(for_owner, for_other):
            for sf, exists in ((owner.sf, for_owner), (other.sf, for_other)):
                if exists:
                    id = sf.getAdminService().getEventContext().userId
                    rnd = sf.getPixelsService().retrieveRndSettingsFor(
                        pixels, id)
                    assert rnd is not None
                else:
                    id = sf.getAdminService().getEventContext().userId
                    rnd = sf.getPixelsService().retrieveRndSettingsFor(
                        pixels, id)
                    assert rnd is None

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

    @pytest.mark.parametrize(
        "method", (
            "saveCurrent", "saveAs",
            "request", "resetDefault",
            "resetDefaultNoSave"))
    @pytest.mark.parametrize(
        "perms", ("readOnly", "readAnnotate", "readWrite"))
    @pytest.mark.parametrize("roles", ("owner", "admin"))
    def test12145ShareSettingsRnd(self, method, perms, roles):
        """
        Rendering settings should be shared when possible.
        Rather than regenerating the min/max per viewer,
        these should be used unless requested otherwise.
        """
        if perms == "readOnly":
            group = self.new_group(perms="rwr---")
            owner = self.new_client(group=group)
            if roles == "owner":
                user = self.new_user(group=group, owner=True)
                other = self.new_client(user=user, group=group)
            elif roles == "admin":
                user = self.new_user(group=group, system=True)
                other = self.new_client(user=user, group=group)
        elif perms == "readAnnotate":
            group = self.new_group(perms="rwra--")
            owner = self.new_client(group=group)
            if roles == "owner":
                user = self.new_user(group=group, owner=True)
                other = self.new_client(user=user, group=group)
            elif roles == "admin":
                user = self.new_user(group=group, system=True)
                other = self.new_client(user=user, group=group)
        elif perms == "readWrite":
            group = self.new_group(perms="rwrw--")
            owner = self.new_client(group=group)
            other = self.new_client(group=group)

        # creation generates a first rendering image
        image = self.createTestImage(session=owner.sf)
        pixels = image.getPrimaryPixels().getId().getValue()

        def assert_rdef(sf=None, prx=None):
            if prx is None:
                prx = sf.createRenderingEngine()
            prx.lookupPixels(pixels)
            assert prx.lookupRenderingDef(pixels)
            return prx, prx.getRenderingDefId()

        # The owner has a rdef, and other
        # users see the same value
        a_prx, a_rdef = assert_rdef(owner.sf)
        b_prx, b_rdef = assert_rdef(other.sf)
        assert a_rdef == b_rdef

        if method == "saveCurrent":
            # If the other users try to save with
            # that prx though, they'll create a new rdef
            b_prx.saveCurrentSettings()
            c_rdef = b_prx.getRenderingDefId()
            assert c_rdef != b_rdef

        elif method == "saveAs":
            # But other users can create new rdefs
            # with new ids using the new method
            try:
                c_rdef = b_prx.saveAsNewSettings()
                ignore, d_rdef = assert_rdef(prx=b_prx)
                assert a_rdef != c_rdef
                assert c_rdef == d_rdef
            except Ice.OperationNotExistException:
                # Not supported by this server
                pass

        elif method == "request":
            # If a user explicitly requests a rdef
            # then it will *not* saveAs and the rdefs
            # should match.
            b_prx.loadRenderingDef(b_rdef)
            try:
                b_prx.saveCurrentSettings()
            except omero.SecurityViolation:
                pass  # You can't do this!
            c_rdef = b_prx.getRenderingDefId()
            assert c_rdef == b_rdef

        elif method == "resetDefault":
            # If the other users try to save with
            # that prx though, they'll create a new rdef
            b_prx.load()
            b_prx.resetDefaultSettings(save=True)
            c_rdef = b_prx.getRenderingDefId()
            b_prx.close()
            assert c_rdef != b_rdef

        elif method == "resetDefaultNoSave":
            # If the other users try to save with
            # that prx though, they'll create a new rdef
            b_prx.load()
            b_prx.resetDefaultSettings(save=False)
            c_rdef = b_prx.getRenderingDefId()
            b_prx.close()
            assert c_rdef == b_rdef

        # But they won't have a thumbnail generated
        tb = other.sf.createThumbnailStore()
        tb.setPixelsId(pixels)
        tb.setRenderingDefId(c_rdef)
        assert not tb.thumbnailExists(rint(96), rint(96))

    @pytest.mark.parametrize("roles", ("owner", "admin"))
    @pytest.mark.parametrize(
        "perms", ("readOnly", "readAnnotate", "readWrite"))
    def test12145ShareSettingsGetThumbnail(self, perms, roles):
        """
        Check that a new thumbnail is created when new
        settings are created.
        """
        if perms == "readOnly":
            group = self.new_group(perms="rwr---")
            owner = self.new_client(group=group)
            if roles == "owner":
                user = self.new_user(group=group, owner=True)
                other = self.new_client(user=user, group=group)
            elif roles == "admin":
                user = self.new_user(group=group, system=True)
                other = self.new_client(user=user, group=group)
        elif perms == "readAnnotate":
            group = self.new_group(perms="rwra--")
            owner = self.new_client(group=group)
            if roles == "owner":
                user = self.new_user(group=group, owner=True)
                other = self.new_client(user=user, group=group)
            elif roles == "admin":
                user = self.new_user(group=group, system=True)
                other = self.new_client(user=user, group=group)
        elif perms == "readWrite":
            group = self.new_group(perms="rwrw--")
            owner = self.new_client(group=group)
            other = self.new_client(group=group)

        # creation generates a first rendering image
        image = self.createTestImage(session=owner.sf)
        pixels = image.getPrimaryPixels().getId().getValue()
        # create thumbnail for image owner 16x16
        tb = owner.sf.createThumbnailStore()
        tb.setPixelsId(pixels)
        tb.getThumbnail(rint(16), rint(16))
        assert tb.thumbnailExists(rint(16), rint(16))

        def assert_rdef(sf=None, prx=None):
            if prx is None:
                prx = sf.createRenderingEngine()
            prx.lookupPixels(pixels)
            assert prx.lookupRenderingDef(pixels)
            return prx, prx.getRenderingDefId()

        # The owner has a rdef, and other
        # users see the same value
        a_prx, a_rdef = assert_rdef(owner.sf)
        b_prx, b_rdef = assert_rdef(other.sf)
        assert a_rdef == b_rdef

        # save settings for group onwer.
        b_prx.saveCurrentSettings()

        # retrieve thumbnail and check that it is created.
        tb = other.sf.createThumbnailStore()
        tb.setPixelsId(pixels)
        tb.getThumbnail(rint(16), rint(16))
        query = other.sf.getQueryService()
        p = omero.sys.Parameters()
        p.map = {}
        p.map["oid"] = omero.rtypes.rlong(
            other.sf.getAdminService().getEventContext().userId)
        p.map["pid"] = omero.rtypes.rlong(pixels)
        thumbs = query.findAllByQuery(
            "select t from Thumbnail t join t.pixels p where \
            t.details.owner.id = :oid and p.id = :pid", p)
        # check that one has been created for the group owner.
        assert 1 == len(thumbs)
        # check that a thum
        tb.close()

    @pytest.mark.parametrize("roles", ("owner", "admin"))
    @pytest.mark.parametrize(
        "perms", ("readOnly", "readAnnotate", "readWrite"))
    def test12145ShareSettingsSetRnd(self, perms, roles):
        """
        Check that a new thumbnail is created when new
        settings are created.
        """
        if perms == "readOnly":
            group = self.new_group(perms="rwr---")
            owner = self.new_client(group=group)
            if roles == "owner":
                user = self.new_user(group=group, owner=True)
                other = self.new_client(user=user, group=group)
            elif roles == "admin":
                user = self.new_user(group=group, system=True)
                other = self.new_client(user=user, group=group)
        elif perms == "readAnnotate":
            group = self.new_group(perms="rwra--")
            owner = self.new_client(group=group)
            if roles == "owner":
                user = self.new_user(group=group, owner=True)
                other = self.new_client(user=user, group=group)
            elif roles == "admin":
                user = self.new_user(group=group, system=True)
                other = self.new_client(user=user, group=group)
        elif perms == "readWrite":
            group = self.new_group(perms="rwrw--")
            owner = self.new_client(group=group)
            other = self.new_client(group=group)

        # creation generates a first rendering image
        image = self.createTestImage(session=owner.sf)
        pixels = image.getPrimaryPixels().getId().getValue()
        # create thumbnail for image owner 16x16
        tb = owner.sf.createThumbnailStore()
        tb.setPixelsId(pixels)
        tb.getThumbnail(rint(16), rint(16))
        assert tb.thumbnailExists(rint(16), rint(16))

        # get thumbnail version
        query = owner.sf.getQueryService()
        p = omero.sys.Parameters()
        p.map = {}
        p.map["oid"] = omero.rtypes.rlong(
            owner.sf.getAdminService().getEventContext().userId)
        p.map["pid"] = omero.rtypes.rlong(pixels)
        thumbs = query.findAllByQuery(
            "select t from Thumbnail t join t.pixels p where \
            t.details.owner.id = :oid and p.id = :pid", p)
        assert 1 == len(thumbs)
        v_thumb = thumbs[0].getVersion().getValue()

        def assert_rdef(sf=None, prx=None):
            if prx is None:
                prx = sf.createRenderingEngine()
            prx.lookupPixels(pixels)
            assert prx.lookupRenderingDef(pixels)
            return prx, prx.getRenderingDefId()

        ownerId = owner.sf.getAdminService().getEventContext().userId
        # Make sure the rendering settings are updated.
        a_prx, a_rdef = assert_rdef(owner.sf)
        settings = owner.sf.getPixelsService().loadRndSettings(a_rdef)
        v_def = settings.getVersion().getValue()
        a_prx.load()
        a_prx.setActive(0, True)
        a_prx.saveCurrentSettings()
        a_rdef = a_prx.getRenderingDefId()
        settings = owner.sf.getPixelsService().loadRndSettings(a_rdef)
        v_def_new = settings.getVersion().getValue()
        assert v_def_new == v_def + 1
        assert settings.getDetails().getOwner().getId().getValue() == ownerId

        # retrieve thumbnail and check that it is not created.
        tb = other.sf.createThumbnailStore()
        tb.setPixelsId(pixels)
        tb.setRenderingDefId(a_rdef)
        tb.getThumbnail(rint(16), rint(16))
        query = other.sf.getQueryService()
        p = omero.sys.Parameters()
        p.map = {}
        p.map["oid"] = omero.rtypes.rlong(
            other.sf.getAdminService().getEventContext().userId)
        p.map["pid"] = omero.rtypes.rlong(pixels)
        thumbs = query.findAllByQuery(
            "select t from Thumbnail t join t.pixels p where \
            t.details.owner.id = :oid and p.id = :pid", p)
        # check that one has been created for the group owner.
        assert 0 == len(thumbs)
        # check that a thum
        tb.close()
        query = owner.sf.getQueryService()
        p = omero.sys.Parameters()
        p.map = {}
        p.map["pid"] = omero.rtypes.rlong(pixels)
        thumbs = query.findAllByQuery(
            "select t from Thumbnail t join t.pixels p where p.id = :pid", p)
        assert 1 == len(thumbs)
        v_thumb_new = thumbs[0].getVersion().getValue()
        assert v_thumb_new == v_thumb + 1
