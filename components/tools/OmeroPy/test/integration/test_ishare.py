#!/usr/bin/env python
# -*- coding: utf-8 -*-

"""
   Integration test focused on the omero.api.IShare interface
   a running server.

   Copyright 2008-2014 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""
import time
from omero.testlib import ITest
import pytest
import omero
import Glacier2

from omero.rtypes import rtime, rlong, rlist, rint
from omero.gateway import BlitzGateway

from test.integration.helpers import createTestImage


class TestIShare(ITest):

    def test_that_permissions_are_default_private(self):
        i = self.make_image()
        assert not i.details.permissions.isGroupRead()
        assert not i.details.permissions.isGroupWrite()
        assert not i.details.permissions.isWorldRead()

    def test_basic_usage(self):

        test_user = self.new_user()
        # create share
        share = self.client.sf.getShareService()
        share_id = self.create_share(
            description="my description", experimenters=[test_user],
            guests=["ident@emaildomain.com"])

        assert 0 == len(share.getContents(share_id))

        d = self.make_dataset()
        share.addObjects(share_id, [d])
        assert len(share.getContents(share_id)) == 1

        ds = self.create_datasets(4, "Dataset")
        share.addObjects(share_id, ds)
        assert share.getContentSize(share_id) == 5
        assert len(share.getAllUsers(share_id)) == 2

        # check access by a member to see the content
        client_guest_read_only = self.new_client(user=test_user)
        try:

            # get dataset - not allowed
            query = client_guest_read_only.sf.getQueryService()
            try:
                query.find("Dataset", d.id.val)
            except Exception:
                pass

            share_read_only = client_guest_read_only.sf.getShareService()
            share_read_only.activate(share_id)
            share_read_only.getContents(share_id)
            assert share_read_only.getContentSize(share_id) == 5
        finally:
            client_guest_read_only.__del__()

        # check access by a member to add comments
        client_guest = self.new_client(user=test_user)
        try:
            share_guest = client_guest.sf.getShareService()
            share_guest.addComment(share_id, "comment for share %i" % share_id)
            assert 1 == len(share_guest.getComments(share_id))
        finally:
            client_guest.__del__()

        # get share key and join directly
        s = share.getShare(share_id)

        client_share = self.new_client(session=s.uuid)
        try:
            share1 = client_share.sf.getShareService()
            assert 1 == len(share1.getOwnShares(True))
        finally:
            client_share.__del__()

    @pytest.mark.parametrize('func', ['canEdit', 'canAnnotate', 'canDelete',
                                      'canLink'])
    def test_canDoAction(self, func):
        """
        Test if canEdit returns appropriate flag
        """

        client, user = self.new_client_and_user()

        image = self.make_image()
        share_id = self.create_share(
            objects=[image], description="description", experimenters=[user])
        share = self.sf.getShareService()
        assert len(share.getContents(share_id)) == 1

        # test action by member
        user_conn = BlitzGateway(client_obj=client)
        # user CANNOT see image if not in share
        assert None == user_conn.getObject("Image", image.id.val)
        # activate share
        user_conn.SERVICE_OPTS.setOmeroShare(share_id)
        assert False == getattr(user_conn.getObject("Image",
                                                    image.id.val), func)()

        # test action by owner
        owner_conn = BlitzGateway(client_obj=self.client)
        # owner CAN do action on the object when not in share
        assert True == getattr(owner_conn.getObject("Image",
                                                    image.id.val), func)()
        # activate share
        owner_conn.SERVICE_OPTS.setOmeroShare(share_id)
        # owner CANNOT do action on the object when in share
        assert False == getattr(owner_conn.getObject("Image",
                                                     image.id.val), func)()

    def test8118(self):
        uuid = self.root.sf.getAdminService().getEventContext().sessionUuid
        share_serv = self.root.sf.getShareService()

        # create user
        user1 = self.new_user()

        # create image
        img = self.make_image(name='test8118-img-%s' % uuid)

        # create share
        description = "my description"
        timeout = None
        objects = [img]
        experimenters = [user1]
        guests = []
        enabled = True
        sid = share_serv.createShare(description, timeout, objects,
                                     experimenters, guests, enabled)
        suuid = share_serv.getShare(sid).uuid

        assert 1 == len(share_serv.getContents(sid))

        # join share
        user1_client = omero.client()
        try:
            user1_client.createSession(suuid, suuid)
            user1_share = user1_client.sf.getShareService()
            user1_share.activate(sid)
            assert 1 == len(user1_share.getContents(sid))
        finally:
            user1_client.__del__()

    def test1154(self):
        uuid = self.root.sf.getAdminService().getEventContext().sessionUuid

        # create two users in one group
        client_share1, user1 = self.new_client_and_user()
        client_share2, user2 = self.new_client_and_user()

        # login as user1
        share1 = client_share1.sf.getShareService()

        # create image
        img = self.make_image(name='test1154-img-%s' % uuid)

        # create share
        description = "my description"
        timeout = None
        objects = [img]
        experimenters = [user2]
        guests = []
        enabled = True
        sid = share1.createShare(description, timeout, objects,
                                 experimenters, guests, enabled)

        assert 1 == len(share1.getContents(sid))

        # login as user2
        share2 = client_share2.sf.getShareService()
        query2 = client_share2.sf.getQueryService()

        share2.getContents(sid)
        assert 1 == len(share2.getContents(sid))

        # get shared image when share is activated
        share2.activate(sid)

        p = omero.sys.Parameters()
        p.map = {"ids": rlist([rlong(img.id.val)])}
        sql = "select im from Image im where im.id in (:ids) order by im.name"
        res = query2.findAllByQuery(sql, p)
        assert 1 == len(res)
        for e in res:
            assert e.id.val == img.id.val

    def testCanAnnotate(self):

        # Users in Private and Read-annotate groups
        private_g = self.new_group(perms="rw----")
        readann_g = self.new_group(perms="rwra--")

        # User 1 is only in private group
        client1, user1 = self.new_client_and_user(group=private_g)

        # User2 is in read-ann group (default) AND private group
        user2 = self.new_user(group=readann_g)
        self.add_groups(user2, [private_g])
        client2 = self.new_client(user=user2)

        # User 1 creates image in Private group...
        img = self.make_image(name='ishare_testCanAnnotate', client=client1)
        assert not img.details.permissions.isGroupRead()
        assert not img.details.permissions.isGroupAnnotate()

        # ...Adds it to share
        description = "my description"
        timeout = None
        objects = [img]
        experimenters = [user2]
        guests = []
        enabled = True
        share1 = client1.sf.getShareService()
        sid = share1.createShare(description, timeout, objects,
                                 experimenters, guests, enabled)

        # User 2 logs in, gets image from share
        query2 = client2.sf.getQueryService()
        p = omero.sys.Parameters()
        p.map = {"ids": rlist([rlong(img.id.val)])}
        sql = "select im from Image im where im.id in (:ids) order by im.name"
        res = query2.findAllByQuery(sql, p, {'omero.share': str(sid)})
        assert 1 == len(res)

        # User should not be able to annotate private image.
        for e in res:
            canAnn = e.getDetails().getPermissions().canAnnotate()
            assert not canAnn
            assert e.id.val == img.id.val

    def test1157(self):
        uuid = self.root.sf.getAdminService().getEventContext().sessionUuid
        share = self.root.sf.getShareService()

        # create two users in one group
        client_share1, user1 = self.new_client_and_user()
        client_share2, user2 = self.new_client_and_user()

        # login as user1
        share1 = client_share1.sf.getShareService()

        # create image
        img = self.make_image(name='test1154-img-%s' % uuid)
        img.unload()

        # create share
        sid = self.create_share(
            description="my description", objects=[img],
            experimenters=[user2], client=client_share1)
        assert len(share1.getContents(sid)) == 1
        # add comment by the owner
        share.addComment(sid, 'test comment by the owner %s' % uuid)

        # login as user2
        share2 = client_share2.sf.getShareService()

        # add comment by the member
        share2.addComment(sid, 'test comment by the member %s' % uuid)

        # get comments
        # by user1
        c1 = len(share.getComments(sid))
        assert 2 == c1
        # by user2
        c2 = len(share2.getComments(sid))
        assert 2 == c2

    @pytest.mark.broken(reason="shares are image-centric for now")
    def test1172(self):
        uuid = self.root.sf.getAdminService().getEventContext().sessionUuid
        share = self.root.sf.getShareService()
        query = self.root.sf.getQueryService()

        # create user
        client_share1, user1 = self.new_client_and_user()

        # create dataset with image
        ds = self.make_dataset(name="dataset-%s" % uuid, client=self.root)
        img = self.new_image(name='test-img in dataset-%s' % uuid)
        self.link(ds, img, client=self.root)

        items = list()
        p = omero.sys.Parameters()
        p.map = {"oid": ds.id}
        sql = (
            "select ds from Dataset ds "
            "join fetch ds.details.owner "
            "join fetch ds.details.group "
            "left outer join fetch ds.imageLinks dil "
            "left outer join fetch dil.child i "
            "where ds.id=:oid")
        items.extend(query.findAllByQuery(sql, p))
        assert 1 == len(items)

        # members
        p.map["eid"] = rlong(user1.id.val)
        sql = ("select e from Experimenter e "
               "where e.id =:eid order by e.omeName")
        ms = query.findAllByQuery(sql, p)
        sid = share.createShare(("test-share-%s" % uuid),
                                rtime(long(time.time() * 1000 + 86400)),
                                items, ms, [], True)

        # USER RETRIEVAL
        # login as user1
        share1 = client_share1.sf.getShareService()
        query1 = client_share1.sf.getQueryService()

        content = share1.getContents(sid)
        # Content now contains just the dataset with nothing loaded
        assert 1 == len(content)

        # get shared dataset and image when share is activated
        share1.activate(sid)

        # retrieve dataset
        p = omero.sys.Parameters()
        p.map = {"ids": rlist([ds.id])}
        sql = (
            "select ds from Dataset ds "
            "join fetch ds.details.owner "
            "join fetch ds.details.group "
            "left outer join fetch ds.imageLinks dil "
            "left outer join fetch dil.child i "
            "where ds.id in (:ids) order by ds.name")
        try:
            res1 = query1.findAllByQuery(sql, p)
            assert False, "This should throw an exception"
        except:
            pass

        # Now we add all the other elements to the share to prevent
        # the security violation
        #
        # Not working imgs = cntar.getImages("Dataset",[ds.id.val], None)
        img = query.findByQuery(
            "select i from Image i join fetch i.datasetLinks dil "
            "join dil.parent d where d.id = %s " % ds.id.val, None)
        assert img
        share.addObject(sid, img)
        share.addObjects(sid, img.copyDatasetLinks())
        assert 3 == len(share.getContents(sid))

        # And try again to load them
        share1.activate(sid)
        res1 = query1.findAllByQuery(sql, p)
        assert len(res1) == 1
        for e in res1:
            assert e.id.val == ds.id.val

        # retrieve only image
        p = omero.sys.Parameters()
        p.map = {"oid": rlong(img.id)}
        sql = "select im from Image im " \
              "where im.id=:oid order by im.name"
        res2 = query1.findByQuery(sql, p)
        assert res2.id.val == img.id.val

    # Test that in a image not in a share, the thumbnail store can be used
    # to retrieve the thumbnail. The image has been viewed by owner.
    def test1179(self):
        createTestImage(self.root.sf)
        rdefs = self.root.sf.getQueryService().findAll("RenderingDef", None)
        if len(rdefs) == 0:
            raise Exception("Must have at least one rendering def")
        id = rdefs[0].pixels.id.val
        # make sure thumbnail is viewed by owner
        tb = self.root.sf.createThumbnailStore()
        try:
            tb.setPixelsId(id)
            s = tb.getThumbnail(rint(16), rint(16))
            assert len(s) > 0
        finally:
            tb.close()

        share = self.root.sf.getShareService()
        sid = share.createShare("", None, [], [], [], True)
        share.activate(sid)
        # Share is active: we are in the security context of the share
        tb = self.root.sf.createThumbnailStore()
        try:
            tb.setPixelsId(id)
            s = tb.getThumbnail(rint(16), rint(16))
            assert len(s) > 0
        except omero.SecurityViolation:
            assert False, "Pixels was not in share"
        finally:
            tb.close()
            share.deactivate()

    def test1201(self):
        admin = self.client.sf.getAdminService()

        # create two users in one group
        group = self.new_group()
        client_user1, user1 = self.new_client_and_user(group)
        client_user2, user2 = self.new_client_and_user(group)
        assert admin.getMemberOfGroupIds(user1) == admin\
            .getMemberOfGroupIds(user2)

        # create share
        share1 = client_user1.sf.getShareService()
        description = "my description"
        timeout = None
        objects = []
        experimenters = [user2]
        guests = ["ident@emaildomain.com"]
        enabled = True
        sid = share1.createShare(description, timeout, objects,
                                 experimenters, guests, enabled)

        # check that owner and member can access share
        self.assert_access(client_user1, sid)
        self.assert_access(client_user2, sid)

        share2 = client_user2.sf.getShareService()
        new_description = "new description"
        share1.setDescription(sid, new_description)

        try:
            assert share2.getShare(sid).message.val == new_description
        except omero.ValidationException:
            pass  # This user can't see the share

        assert share1.getShare(sid).message.val == new_description

        expiration = long(time.time() * 1000) + 86400
        share1.setExpiration(sid, rtime(expiration))
        self.assert_expiration(expiration, share1.getShare(sid))

        share1.setActive(sid, False)
        assert share1.getShare(sid).active.val is False

        owned = share1.getOwnShares(False)
        assert 1 == len(owned)
        return client_user1, sid, expiration

    def test1201b(self):
        new_group = self.new_group()
        new_client, new_user = self.new_client_and_user(new_group)
        share = new_client.sf.getShareService()
        # create share
        description = "my description"
        timeout = None
        objects = []
        experimenters = []
        guests = ["ident@emaildomain.com"]
        enabled = True
        sid = share.createShare(description, timeout, objects,
                                experimenters, guests, enabled)

        assert share.getShare(sid).active.val is True
        share.setActive(sid, False)
        assert share.getShare(sid).active.val is False
        owned = share.getOwnShares(False)
        assert 1 == len(owned)

    def test1207(self):
        # create two users in one group
        new_gr1 = self.new_group()
        client_share1, new_exp = self.new_client_and_user(new_gr1)
        client_share3, new_exp3 = self.new_client_and_user(new_gr1)

        share1 = client_share1.sf.getShareService()
        share3 = client_share3.sf.getShareService()

        test_user = self.new_user()

        # create share
        description = "my description"
        timeout = None
        objects = []
        experimenters = [test_user]
        guests = ["ident@emaildomain.com"]
        enabled = True
        sid = share1.createShare(description, timeout, objects,
                                 experimenters, guests, enabled)

        try:
            share3.getShare(sid)
            assert False, "Share returned to non-member"
        except:
            pass

    def test1227(self):
        share = self.client.sf.getShareService()

        test_user = self.new_user()
        # create share
        description = "my description"
        timeout = None
        objects = []
        experimenters = [test_user]
        guests = ["ident@emaildomain.com"]
        enabled = True
        share_id = share.createShare(description, timeout, objects,
                                     experimenters, guests, enabled)

        share.addComment(share_id, "comment for share %i" % share_id)
        assert 1 == len(share.getComments(share_id))

        assert 1 == share.getCommentCount([share_id])[share_id]

        # create second share
        description = "my second description"
        timeout = None
        objects = []
        experimenters = [test_user]
        guests = ["ident@emaildomain.com"]
        enabled = True
        share_id2 = share.createShare(description, timeout, objects,
                                      experimenters, guests, enabled)

        assert 0 == share.getCommentCount([share_id, share_id2])[share_id2]
        share.addComment(share_id2, "comment for share %i" % share_id2)
        assert 1 == share.getCommentCount([share_id, share_id2])[share_id2]

    def test2327(self):
        # create two users in two groups
        client_share1, user1 = self.new_client_and_user()
        client_share2, user2 = self.new_client_and_user()

        # create image
        img = self.make_image(name='test2327', client=client_share1)

        # create share
        description = "my description"
        timeout = None
        objects = [img]
        experimenters = [user2]
        guests = []
        enabled = True
        share1 = client_share1.sf.getShareService()
        sid = share1.createShare(description, timeout, objects,
                                 experimenters, guests, enabled)
        assert len(share1.getContents(sid)) == 1
        # add comment by the owner
        share1.addComment(sid, 'test comment by the owner %s' % user1.id.val)

        # login as user2
        share2 = client_share2.sf.getShareService()
        l = share2.getMemberShares(False)
        assert 1 == len(l)

        # add comment by the member
        share2.addComment(sid, 'test comment by the member %s' % user2.id.val)

        # get comments
        # by user1
        c1 = len(share1.getComments(sid))
        assert 2 == c1
        # by user2
        c2 = len(share2.getComments(sid))
        assert 2 == c2

    def test2733(self):
        # create two users in two groups
        client_share1, user1 = self.new_client_and_user()
        client_share2, user2 = self.new_client_and_user()

        # login as user1
        share1 = client_share1.sf.getShareService()
        update1 = client_share1.sf.getUpdateService()

        # create image
        img = self.new_image("test2733")
        img = update1.saveAndReturnObject(img)
        img.unload()

        # create share
        description = "my description"
        timeout = None
        objects = [img]
        experimenters = [user2]
        guests = []
        enabled = True
        sid = share1.createShare(description, timeout, objects,
                                 experimenters, guests, enabled)

        share2 = client_share2.sf.getShareService()
        share2.getShare(sid)

    def test2733Access(self):
        """
        The solution for getting test2733 was to
        open up access to share in ProxyCleanupHandler.

        This test makes sure it is not too open.
        """

        # create three users in three groups
        group = self.new_group(perms="rwrw--")

        smember, smember_obj = self.new_client_and_user()  # Member of share

        # Owner of share
        owner = self.new_client(group=group)
        # Member of user1's group
        gmember = self.new_client(group=group)
        # Owner of user1's group
        gowner = self.new_client(group=group, owner=True)
        # Admin of a different group
        oowner = self.new_client(owner=True)

        # login as user1
        share1 = owner.sf.getShareService()

        # create image
        img = self.make_image("test2733Access")
        img.unload()

        # create share
        description = "my description"
        timeout = None
        objects = [img]
        experimenters = [smember_obj]
        guests = []
        enabled = True
        sid = share1.createShare(description, timeout, objects,
                                 experimenters, guests, enabled)

        self.assert_access(owner, sid)
        self.assert_access(smember, sid)
        self.assert_access(gmember, sid, False)
        self.assert_access(gowner, sid, False)
        self.assert_access(oowner, sid, False)
        self.assert_access(self.root, sid)

    def test3214(self):
        """
        The solution for 2733 returned too many
        unloaded shares.
        """

        # create three users in three groups
        group = self.new_group(perms="rwrw--")

        # Member of share
        smember, smember_obj = self.new_client_and_user()

        # Owner of share
        owner = self.new_client(group=group)
        # Member of user1's group
        self.new_client(group=group)

        # login as user1
        share1 = owner.sf.getShareService()

        # create image
        img = self.make_image(name="test2733Access", client=owner)
        img.unload()

        # create share
        description = "my description"
        timeout = None
        objects = [img]
        experimenters = [smember_obj]
        guests = []
        enabled = True
        share1.createShare(
            description, timeout, objects, experimenters, guests, enabled)

        shares = share1.getOwnShares(True)
        assert 1 == len(shares)
        assert shares[0].isLoaded()

        shares = smember.sf.getShareService().getMemberShares(True)
        assert 1 == len(shares)
        assert shares[0].isLoaded()

    def test5711(self):
        """
        Recent changes have caused shares to be disabled.
        """
        share = self.client.sf.getShareService()
        admin = self.client.sf.getAdminService()

        share_id = share.createShare("", None, [], [], [], True)
        self.client.sf.setSecurityContext(omero.model.ShareI(share_id, False))
        ec = admin.getEventContext()
        assert share_id == ec.shareId

    def test5756Raw(self):
        """
        Accessing deleted image in share seems to have changed.
        This tests what happens using the raw API.
        """
        new_group = self.new_group()
        new_client, new_user = self.new_client_and_user(new_group)
        share = new_client.sf.getShareService()
        query = new_client.sf.getQueryService()
        update = new_client.sf.getUpdateService()

        image = self.make_image(client=new_client)
        objects = [image]

        share_id = share.createShare("", None, objects, [], [], True)
        new_context = omero.model.ShareI(share_id, False)
        old_context = new_client.sf.setSecurityContext(new_context)
        query.get("Image", image.id.val)

        new_client.sf.setSecurityContext(old_context)
        update.deleteObject(image)
        new_client.sf.setSecurityContext(new_context)

        with pytest.raises(omero.ValidationException):
            query.get("Image", image.id.val)

    def test5756Wrapped(self):
        """
        Accessing deleted image in share seems to have changed.
        This tests what happens using BlitzGateway wrappers.
        """
        new_group = self.new_group()
        new_client, new_user = self.new_client_and_user(new_group)
        share = new_client.sf.getShareService()
        query = new_client.sf.getQueryService()
        update = new_client.sf.getUpdateService()

        image = self.make_image(client=new_client)
        objects = [image]

        share_id = share.createShare("", None, objects, [], [], True)
        new_context = omero.model.ShareI(share_id, False)
        old_context = new_client.sf.setSecurityContext(new_context)
        image = query.get("Image", image.id.val)

        from omero.gateway import ImageWrapper, BlitzGateway

        conn = BlitzGateway(client_obj=new_client)
        wrapper = ImageWrapper(conn=conn, obj=image)

        new_client.sf.setSecurityContext(old_context)
        update.deleteObject(image)
        new_client.sf.setSecurityContext(new_context)

        with pytest.raises(IndexError):
            wrapper.__loadedHotSwap__()

    def test5851(self):
        """
        Expiration is being lost. This test tries to simulate
        a share that was created some time ago and is being
        accessed again.
        """
        client, sid, expiration = self.test1201()
        admin = client.sf.getAdminService()
        share = client.sf.getShareService()
        session = client.sf.getSessionService()

        # Regular reloading
        share.setActive(sid, True)
        share.activate(sid)
        admin.getEventContext()  # Refreshes
        share.deactivate()
        admin.getEventContext()  # Refreshes
        self.assert_expiration(expiration, share.getShare(sid))

        # Forced closing
        assert -2 == session.closeSession(share.getShare(sid))
        share.activate(sid)
        admin.getEventContext()  # Refreshes
        self.assert_expiration(expiration, share.getShare(sid))

    def test2513(self):
        """
        Test a few NPE scenarios in IShare
        """
        shares = self.client.sf.getShareService()

        # Create a bad screen
        with pytest.raises(omero.ValidationException):
            shares.createShare("my description", None, [omero.model.ScreenI()],
                               [], [], True)

    def test_OS_regular_user(self):
        # test regular user can activate a share
        # Owner of share
        owner, owner_obj = self.new_client_and_user(perms="rw----")
        # Different group!
        member, member_obj = self.new_client_and_user(perms="rw----")

        # create image and share
        img = self.make_image("testOSRegularUser", client=owner)
        img.unload()
        sid = self.create_share(
            objects=[img], experimenters=[owner_obj, member_obj], client=owner)

        self.assert_access(owner, sid)
        self.assert_access(member, sid)
        # But the user won't be able to just access it plainly
        member_query = member.sf.getQueryService()
        with pytest.raises(omero.SecurityViolation):
            member_query.get("Image", img.id.val)

        # But if we let the user pass omero.share it should work.
        member_query.get("Image", img.id.val, {"omero.share": "%s" % sid})

        return img, sid

    def test_OS_non_member(self):
        # Non-members should not be able to use this method
        # Run setup
        img, sid = self.test_OS_regular_user()
        non_member = self.new_client(perms="rw----")
        non_member_query = non_member.sf.getQueryService()

        # Try to access direct
        with pytest.raises(omero.SecurityViolation):
            non_member_query.get("Image", img.id.val)

        # Now try to access via omero.share
        with pytest.raises(omero.SecurityViolation):
            non_member_query.get("Image", img.id.val,
                                 {"omero.share": "%s" % sid})

    def test_OS_admin_user(self):
        # Admin should be able to log into any share
        img, sid = self.test_OS_regular_user()
        root_query = self.root.sf.getQueryService()

        # Try to access direct (in wrong group)
        with pytest.raises(omero.SecurityViolation):
            root_query.get("Image", img.id.val)

        # Now try to access via omero.share
        root_query.get("Image", img.id.val, {"omero.share": "%s" % sid})

    def test_bad_share(self):
        # Try to access a non-extant share
        # Since the security violation is thrown
        # first, we no longer get a validation exc.
        with pytest.raises(omero.SecurityViolation):
            self.client.sf.getQueryService().get("Image", -1,
                                                 {"omero.share": "-100"})

    def test8513(self):
        # Owner of share
        owner, owner_obj = self.new_client_and_user(perms="rw----")
        # Different group!
        member, member_obj = self.new_client_and_user(perms="rw----")

        member_suuid = \
            member.sf.getAdminService().getEventContext().sessionUuid
        owner_suuid = \
            owner.sf.getAdminService().getEventContext().sessionUuid

        member.sf.getAdminService().getEventContext().groupId
        owner.sf.getAdminService().getEventContext().groupId

        # just in case
        assert member_suuid != owner_suuid

        # just in case
        assert owner_obj.id.val != member_obj.id.val

        image = self.make_image(client=owner)
        image2 = self.make_image(client=member)

        sid = self.create_share(
            objects=[image], experimenters=[member_obj], client=owner)

        self.assert_access(owner, sid)
        self.assert_access(member, sid)

        member_share = member.sf.getShareService()
        member_share.getShare(sid)
        # Activation shouldn't be needed any more as
        # we pass {'omero.share': <sid>}
        # member_share.activate(long(sid))

        # And the member should be able to use omero.share:sid
        member_query = member.sf.getQueryService()

        try:
            rv = member.sf.getQueryService().find("Image", image.id.val, None)
        except omero.SecurityViolation:
            pass
        else:
            assert False, "Error: Member shouldn't access image in share!"

        rv = member_query.find("Image", image.id.val,
                               {'omero.share': str(sid),
                                'omero.group':
                                    str(image.details.group.id.val)})
        assert image.id.val == rv.id.val

        # join share
        user_client = self.new_client(session=member_suuid)
        try:
            # Deactivation shouldn't be needed any more as
            # we pass {'omero.share': <sid>}
            # user_client.sf.getShareService().deactivate()
            user_query = user_client.sf.getQueryService()
            rv = user_query.find("Image", image2.id.val,
                                 {'omero.group':
                                  str(image2.details.group.id.val)})
            assert image2.id.val == rv.id.val
        finally:
            user_client.__del__()

            # Note: The following fails with a security violation since
            # it is expected that the user first check the contents of
            # the share and then load those values.
            # rv = member_query.findAll("Image", None,
            #                           {"omero.share":"%s" % sid})
            # assert 0 ==  len(rv)

    def test8704(self):
        # Owner of share
        owner, owner_obj = self.new_client_and_user(perms="rw----")
        # Different group!
        member, member_obj = self.new_client_and_user(perms="rw----")

        member_suuid = \
            member.sf.getAdminService().getEventContext().sessionUuid
        owner_suuid = \
            owner.sf.getAdminService().getEventContext().sessionUuid

        member.sf.getAdminService().getEventContext().groupId
        owner_groupId = owner.sf.getAdminService().getEventContext().groupId

        # just in case
        assert member_suuid != owner_suuid

        # just in case
        assert owner_obj.id.val != member_obj.id.val

        # create image by owner
        owner.sf.getUpdateService()
        image_id = createTestImage(owner.sf)

        p = omero.sys.Parameters()
        p.map = {"id": rlong(long(image_id))}
        sql = "select im from Image im join fetch im.details.owner " \
              "join fetch im.details.group where im.id=:id order by im.name"
        image = owner.sf.getQueryService().findAllByQuery(
            sql, p, {'omero.group': str(owner_groupId)})[0]

        rdefs = owner.sf.getQueryService().findAll("RenderingDef", None)

        # create image by member
        image2 = self.make_image(client=member)

        sid = self.create_share(
            objects=[image], experimenters=[member_obj], client=owner)

        self.assert_access(owner, sid)
        self.assert_access(member, sid)

        member_share = member.sf.getShareService()
        member_share.getShare(sid)
        # Activation shouldn't be needed any more as
        # we pass {'omero.share': <sid>}
        # member_share.activate(long(sid))

        # And the member should be able to use omero.share:sid
        member_query = member.sf.getQueryService()

        try:
            rv = member.sf.getQueryService().find("Image", image.id.val, None)
        except omero.SecurityViolation:
            pass
        else:
            assert False, "Error: Member shouldn't access image in share!"

        rv = member_query.find("Image", image.id.val,
                               {'omero.share': str(sid)})
        # Not sure which group to set
        # 'omero.group':str(image.details.group.id.val)
        # or 'omero.group':str(member_groupId)

        assert image.id.val == rv.id.val

        member_tb = member.sf.createThumbnailStore()
        try:
            member_tb.setPixelsId(rdefs[0].pixels.id.val,
                                  {'omero.share': str(sid)})
        finally:
            member_tb.close()
        # join share
        user_client = self.new_client(session=member_suuid)
        try:
            # Deactivation shouldn't be needed any more
            # as we pass {'omero.share': <sid>}
            # user_client.sf.getShareService().deactivate()
            user_query = user_client.sf.getQueryService()
            rv = user_query.find("Image", image2.id.val,
                                 {'omero.group':
                                  str(image2.details.group.id.val)})
            assert image2.id.val == rv.id.val
        finally:
            user_client.__del__()

    def test13018(self):
        """
        Test that image in share is unavailable when share
        is inactive or expired
        """
        owner = self.new_client()
        member, mobj = self.new_client_and_user()

        createTestImage(owner.sf)
        image = owner.sf.getQueryService().findAll("Image", None)[0]

        o_share = owner.sf.getShareService()
        sid = o_share.createShare("", None, [image], [mobj], [], True)

        m_share = member.sf.getShareService()
        m_share.activate(sid)

        o_share.setActive(sid, False)
        with pytest.raises(omero.ValidationException):
            m_share.activate(sid)

        with pytest.raises(omero.ValidationException):
            obj = omero.model.ShareI(sid, False)
            member.sf.setSecurityContext(obj)

        # test inactive share, if member has no access to the image
        s = o_share.getShare(sid)
        with pytest.raises(Glacier2.PermissionDeniedException):
            self.new_client(session=s.uuid)

        # activate again
        o_share.setActive(sid, True)

        # test that the image is now loadable again.
        t_conn = self.new_client(session=s.uuid)
        t_conn.sf.getQueryService().find("Image", image.id.val)

        # test expired share, if member has no access to the image
        expiration = long(time.time() * 1000) + 500
        o_share.setExpiration(sid, rtime(expiration))
        self.assert_expiration(expiration, o_share.getShare(sid))
        time.sleep(0.5)

        # Forced closing
        o_session = owner.sf.getSessionService()
        o_session.closeSession(o_share.getShare(sid))

        with pytest.raises(Glacier2.PermissionDeniedException):
            self.new_client(session=s.uuid)

    # Helpers

    def assert_access(self, client, sid, success=True):
        share = client.sf.getShareService()
        query = client.sf.getQueryService()

        share_from_ishare = share.getShare(sid)
        assert success == (share_from_ishare is not None)

        # For the moment, preventing all non-IShare download.
        share_from_iquery = query.get("Share", sid)
        assert not share_from_iquery.isLoaded()

    def assert_expiration(self, expiration, share):
        assert expiration == (share.started.val + share.timeToLive.val)
