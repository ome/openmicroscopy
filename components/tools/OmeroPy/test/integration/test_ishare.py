#!/usr/bin/env python
# -*- coding: utf-8 -*-

"""
   Integration test focused on the omero.api.IShare interface
   a running server.

   Copyright 2008 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""
import unittest, time
import test.integration.library as lib
import omero
from omero_model_PixelsI import PixelsI
from omero_model_ImageI import ImageI
from omero_model_DatasetI import DatasetI
from omero_model_ExperimenterI import ExperimenterI
from omero_model_ExperimenterGroupI import ExperimenterGroupI
from omero_model_GroupExperimenterMapI import GroupExperimenterMapI
from omero_model_DatasetImageLinkI import DatasetImageLinkI
from omero.rtypes import rtime, rlong, rstring, rlist

from test.integration.helpers import createTestImage


class TestIShare(lib.ITest):

    def testThatPermissionsAreDefaultPrivate(self):
        i = omero.model.ImageI()
        i.name = rstring("name")
        i.acquisitionDate = rtime(0)
        i = self.client.sf.getUpdateService().saveAndReturnObject(i)
        self.assert_( not i.details.permissions.isGroupRead() )
        self.assert_( not i.details.permissions.isGroupWrite() )
        self.assert_( not i.details.permissions.isWorldRead() )

    def testBasicUsage(self):
        share = self.client.sf.getShareService()
        update = self.client.sf.getUpdateService()
        admin = self.client.sf.getAdminService()

        test_user = self.new_user()
        # create share
        description = "my description"
        timeout = None
        objects = []
        experimenters = [test_user]
        guests = ["ident@emaildomain.com"]
        enabled = True
        self.share_id = share.createShare(description, timeout, objects,experimenters, guests, enabled)

        self.assertEquals(0, len(share.getContents(self.share_id)))

        d = omero.model.DatasetI()
        d.setName(rstring("d"))
        d = update.saveAndReturnObject(d)
        share.addObjects(self.share_id, [d])

        self.assert_(len(share.getContents(self.share_id)) == 1)

        ds = []
        for i in range(0,4):
            ds.append(omero.model.DatasetI())
            ds[i].setName(rstring("ds%i" % i))
        ds = update.saveAndReturnArray(ds)
        share.addObjects(self.share_id, ds)

        self.assert_(share.getContentSize(self.share_id) == 5)

        self.assert_(len(share.getAllUsers(self.share_id)) == 2)

        #check access by a member to see the content
        client_guest_read_only = self.new_client(user=test_user, password="ome")
        try:

            #get dataset - not allowed
            query = client_guest_read_only.sf.getQueryService()
            try:
                query.find("Dataset",d.id.val)
            except Exception, x:
                pass

            share_read_only = client_guest_read_only.sf.getShareService()
            share_read_only.activate(self.share_id)
            content = share_read_only.getContents(self.share_id)
            self.assert_(share_read_only.getContentSize(self.share_id) == 5)
        finally:
            client_guest_read_only.__del__()

        #check access by a member to add comments
        client_guest = self.new_client(user=test_user, password="ome")
        try:
            share_guest = client_guest.sf.getShareService()
            share_guest.addComment(self.share_id,"comment for share %i" % self.share_id)
            self.assertEquals(1,len(share_guest.getComments(self.share_id)))
        finally:
            client_guest.__del__()

        # get share key and join directly
        s = share.getShare(self.share_id)

        client_share = self.new_client(session=s.uuid)
        try:
            share1 = client_share.sf.getShareService()
            self.assertEquals(1, len(share1.getOwnShares(True)))
        finally:
            client_share.__del__()

## Removing test for 'guest' user. 
## This currently fails but there is some question
## as to whether we should have a guest user.
##
##        # guest looks in to the share
##        guest_email = "ident@emaildomain.com"
##        token =  s.uuid
##        client_share_guest = omero.client()
##        client_share_guest.createSession("guest","guest") # maybe there can be some verification of identity by (share_key, email) - both params could be sent to email
##
##        share2 = client_share_guest.sf.getShareService()
##        # Doesn't exist # share2.getAllGuestShares(guest_email)
##        # Doesn't exist # self.assert_(share2.getGuestShare(token) > 0)
##        share2.addComment(self.share_id,"guest comment for share %i" % self.share_id)
##        self.assertEquals(1,len(share2.getComments(self.share_id)))

    def test8118(self):
        uuid = self.root.sf.getAdminService().getEventContext().sessionUuid
        share_serv = self.root.sf.getShareService()
        update_serv = self.root.sf.getUpdateService()
        
        # create user
        user1 = self.new_user()
        
        # create image
        img = ImageI()
        img.setName(rstring('test8118-img-%s' % (uuid)))
        img.setAcquisitionDate(rtime(0))
        img = update_serv.saveAndReturnObject(img)
        img.unload()
        
        # create share
        description = "my description"
        timeout = None
        objects = [img]
        experimenters = [user1]
        guests = []
        enabled = True
        sid = share_serv.createShare(description, timeout, objects,experimenters, guests, enabled)
        suuid = share_serv.getShare(sid).uuid
        
        self.assertEquals(1,len(share_serv.getContents(sid)))
        
        # join share
        user1_client = omero.client()
        try:
            user1_client.createSession(suuid,suuid)
            user1_share = user1_client.sf.getShareService()
            user1_share.activate(sid)
            self.assertEquals(1, len(user1_share.getContents(sid)))
        finally:
            user1_client.__del__()
        
    def test1154(self):
        uuid = self.root.sf.getAdminService().getEventContext().sessionUuid

        
        ### create two users in one group
        client_share1, user1 = self.new_client_and_user()
        client_share2, user2 = self.new_client_and_user()

        ## login as user1
        share1 = client_share1.sf.getShareService()
        update1 = client_share1.sf.getUpdateService()
        
        # create image
        img = ImageI()
        img.setName(rstring('test1154-img-%s' % (uuid)))
        img.setAcquisitionDate(rtime(0))
        img = update1.saveAndReturnObject(img)
        img.unload()
        
        # create share
        description = "my description"
        timeout = None
        objects = [img]
        experimenters = [user2]
        guests = []
        enabled = True
        sid = share1.createShare(description, timeout, objects,experimenters, guests, enabled)
        
        self.assertEquals(1,len(share1.getContents(sid)))
        
        ## login as user2
        share2 = client_share2.sf.getShareService()
        query2 = client_share2.sf.getQueryService()
        
        content = share2.getContents(sid)
        self.assertEquals(1,len(share2.getContents(sid)))
        
        # get shared image when share is activated
        share2.activate(sid)

        p = omero.sys.Parameters()
        p.map = {}
        p.map["ids"] = rlist([rlong(img.id.val)])
        sql = "select im from Image im where im.id in (:ids) order by im.name"
        res = query2.findAllByQuery(sql, p)
        self.assertEquals(1,len(res))
        for e in res:
            self.assert_(e.id.val == img.id.val)

    def test1157(self):
        uuid = self.root.sf.getAdminService().getEventContext().sessionUuid
        share = self.root.sf.getShareService()
        
        ### create two users in one group
        client_share1, user1 = self.new_client_and_user()
        client_share2, user2 = self.new_client_and_user()
        
        ## login as user1 
        share1 = client_share1.sf.getShareService()
        update1 = client_share1.sf.getUpdateService()
        
        # create image
        img = ImageI()
        img.setName(rstring('test1154-img-%s' % (uuid)))
        img.setAcquisitionDate(rtime(0))
        img = update1.saveAndReturnObject(img)
        img.unload()
        
        # create share
        description = "my description"
        timeout = None
        objects = [img]
        experimenters = [user2]
        guests = []
        enabled = True
        sid = share1.createShare(description, timeout, objects,experimenters, guests, enabled)
        self.assert_(len(share1.getContents(sid)) == 1)
        # add comment by the owner
        share.addComment(sid, 'test comment by the owner %s' % (uuid))
        
        ## login as user2
        share2 = client_share2.sf.getShareService()
        query2 = client_share2.sf.getQueryService()
        
        # add comment by the member
        share2.addComment(sid, 'test comment by the member %s' % (uuid))

        # Don't have to activate share2

        #get comments
        # by user1
        c1 = len(share.getComments(sid))
        self.assertEquals(2,c1)
        # by user2
        c2 = len(share2.getComments(sid))
        self.assertEquals(2,c2)

    def test1172(self):
        uuid = self.root.sf.getAdminService().getEventContext().sessionUuid
        share = self.root.sf.getShareService()
        query = self.root.sf.getQueryService()
        update = self.root.sf.getUpdateService()
        
        ### create user
        client_share1, user1 = self.new_client_and_user()
        
        #create dataset with image
        #dataset with image
        ds = omero.model.DatasetI()
        ds.setName(rstring("dataset-%s" % (uuid)))
        ds = update.saveAndReturnObject(ds)
        ds.unload()
        
        # create image
        img = ImageI()
        img.setName(rstring('test-img in dataset-%s' % (uuid)))
        img.setAcquisitionDate(rtime(0))
        img = update.saveAndReturnObject(img)
        img.unload()
        
        dil = DatasetImageLinkI()
        dil.setParent(ds)
        dil.setChild(img)
        dil = update.saveAndReturnObject(dil)
        dil.unload()
        
        # create share by root
        items = list()
        ms = list()
        
        p = omero.sys.Parameters()
        p.map = {}
        p.map["oid"] = ds.id
        sql = "select ds from Dataset ds join fetch ds.details.owner join fetch ds.details.group " \
              "left outer join fetch ds.imageLinks dil left outer join fetch dil.child i " \
              "where ds.id=:oid"
        items.extend(query.findAllByQuery(sql, p))
        self.assertEquals(1, len(items))
        
        #members
        p.map["eid"] = rlong(user1.id.val)
        sql = "select e from Experimenter e where e.id =:eid order by e.omeName"
        ms = query.findAllByQuery(sql, p)
        sid = share.createShare(("test-share-%s" % uuid), rtime(long(time.time()*1000 + 86400)) , items, ms, [], True)
        
        # USER RETRIEVAL
        ## login as user1
        share1 = client_share1.sf.getShareService()
        query1 = client_share1.sf.getQueryService()
        cntar1 = client_share1.sf.getContainerService()
        
        content = share1.getContents(sid)
        # Content now contains just the dataset with nothing loaded
        self.assertEquals(1, len(content))

        # get shared dataset and image when share is activated
        share1.activate(sid)

        #retrieve dataset
        p = omero.sys.Parameters()
        p.map = {}
        p.map["ids"] = rlist([ds.id])
        sql = "select ds from Dataset ds join fetch ds.details.owner join fetch ds.details.group " \
              "left outer join fetch ds.imageLinks dil left outer join fetch dil.child i " \
              "where ds.id in (:ids) order by ds.name"
        try:
            res1 = query1.findAllByQuery(sql, p)
            self.fail("This should throw an exception")
        except:
            pass

        #
        # Now we add all the other elements to the share to prevent
        # the security violation
        #
        # Not working imgs = cntar.getImages("Dataset",[ds.id.val], None)
        img = query.findByQuery("select i from Image i join fetch i.datasetLinks dil join dil.parent d where d.id = %s " % ds.id.val, None)
        self.assert_(img)
        share.addObject(sid, img)
        share.addObjects(sid, img.copyDatasetLinks())
        self.assertEquals(3, len(share.getContents(sid)))

        #
        # And try again to load them
        #
        share1.activate(sid)
        res1 = query1.findAllByQuery(sql, p)
        self.assert_(len(res1) == 1)
        for e in res1:
            self.assert_(e.id.val == ds.id.val)

        # retrieve only image
        p = omero.sys.Parameters()
        p.map = {}
        p.map["oid"] = rlong(img.id)
        sql = "select im from Image im " \
              "where im.id=:oid order by im.name"
        res2 = query1.findByQuery(sql, p)
        self.assert_(res2.id.val == img.id.val)

    def test1179(self):
        createTestImage(self.root.sf)
        rdefs = self.root.sf.getQueryService().findAll("RenderingDef", None)
        if len(rdefs) == 0:
            raise Exception("Must have at least one rendering def")
        share = self.root.sf.getShareService()
        sid = share.createShare("", None, [], [], [], True)
        share.activate(sid)
        tb = self.root.sf.createThumbnailStore()
        try:
            tb.setPixelsId(rdefs[0].pixels.id.val)
        except omero.SecurityViolation:
            self.fail("Pixels was not in share")

    def test1201(self):
        uuid = self.root.sf.getAdminService().getEventContext().sessionUuid
        share = self.client.sf.getShareService()
        update = self.root.sf.getUpdateService()
        admin = self.root.sf.getAdminService()
        
        ### create two users in one group
        client_share1, user1 = self.new_client_and_user()
        share1 = client_share1.sf.getShareService()

        client_share2, test_user = self.new_client_and_user()
        # create share
        description = "my description"
        timeout = None
        objects = []
        experimenters = [test_user]
        guests = ["ident@emaildomain.com"]
        enabled = True
        sid = share1.createShare(description, timeout, objects,experimenters, guests, enabled)

        #re - login as user1
        share2 = client_share2.sf.getShareService()

        new_description = "new description"
        share1.setDescription(sid, new_description)
        try:
            self.assertEquals(share2.getShare(sid).message.val, new_description)
        except omero.ValidationException, ve:
            pass # This user can't see the share

        self.assertEquals(share1.getShare(sid).message.val, new_description)

        expiration = long(time.time()*1000)+86400
        share1.setExpiration(sid, rtime(expiration))
        self.assertExpiration(expiration, share1.getShare(sid))

        share1.setActive(sid, False)
        self.assert_(share1.getShare(sid).active.val == False)

        owned = share1.getOwnShares(False)
        self.assertEquals(1, len(owned))
        return (client_share1, sid, expiration)

    def test1201b(self):
        share = self.client.sf.getShareService()
        # create share
        description = "my description"
        timeout = None
        objects = []
        experimenters = []
        guests = ["ident@emaildomain.com"]
        enabled = True
        sid = share.createShare(description, timeout, objects,experimenters, guests, enabled)

        self.assert_(share.getShare(sid).active.val == True)
        share.setActive(sid, False)
        self.assert_(share.getShare(sid).active.val == False)
        owned = share.getOwnShares(False)
        self.assertEquals(1, len(owned))

    def test1207(self):

        ### create two users in one group
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
        sid = share1.createShare(description, timeout, objects, experimenters, guests, enabled)

        res = None
        try:
            share = share3.getShare(sid)
            self.fail("Share returned to non-member")
        except:
            pass

    def test1227(self):
        share = self.client.sf.getShareService()
        update = self.client.sf.getUpdateService()
        admin = self.client.sf.getAdminService()

        test_user = self.new_user()
        # create share
        description = "my description"
        timeout = None
        objects = []
        experimenters = [test_user]
        guests = ["ident@emaildomain.com"]
        enabled = True
        self.share_id = share.createShare(description, timeout, objects,experimenters, guests, enabled)

        share.addComment(self.share_id,"comment for share %i" % self.share_id)
        self.assertEquals(1,len(share.getComments(self.share_id)))

        self.assertEquals(1,share.getCommentCount([self.share_id])[self.share_id])

        # create second share
        description = "my second description"
        timeout = None
        objects = []
        experimenters = [test_user]
        guests = ["ident@emaildomain.com"]
        enabled = True
        self.share_id2 = share.createShare(description, timeout, objects,experimenters, guests, enabled)

        self.assertEquals(0,share.getCommentCount([self.share_id, self.share_id2])[self.share_id2])
        share.addComment(self.share_id2,"comment for share %i" % self.share_id2)
        self.assertEquals(1,share.getCommentCount([self.share_id, self.share_id2])[self.share_id2])

    def test2327(self):

        ### create two users in two groups
        client_share1, user1 = self.new_client_and_user()
        client_share2, user2 = self.new_client_and_user()

        ## login as user1
        share1 = client_share1.sf.getShareService()
        update1 = client_share1.sf.getUpdateService()

        # create image
        img = ImageI()
        img.setName(rstring('test2327'))
        img.setAcquisitionDate(rtime(0))
        img = update1.saveAndReturnObject(img)
        img.unload()

        # create share
        description = "my description"
        timeout = None
        objects = [img]
        experimenters = [user2]
        guests = []
        enabled = True
        sid = share1.createShare(description, timeout, objects, experimenters, guests, enabled)
        self.assert_(len(share1.getContents(sid)) == 1)
        # add comment by the owner
        share1.addComment(sid, 'test comment by the owner %s' % user1.id.val)

        ## login as user2
        share2 = client_share2.sf.getShareService()
        query2 = client_share2.sf.getQueryService()

        l = share2.getMemberShares(False)
        self.assertEquals(1, len(l))

        # add comment by the member
        share2.addComment(sid, 'test comment by the member %s' % (user2.id.val))

        # Don't have to activate share2

        #get comments
        # by user1
        c1 = len(share1.getComments(sid))
        self.assertEquals(2, c1)
        # by user2
        c2 = len(share2.getComments(sid))
        self.assertEquals(2, c2)

    def test2733(self):

        ### create two users in two groups
        client_share1, user1 = self.new_client_and_user()
        client_share2, user2 = self.new_client_and_user()

        ## login as user1
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
        sid = share1.createShare(description, timeout, objects, experimenters, guests, enabled)

        share2 = client_share2.sf.getShareService()
        share = share2.getShare(sid)

    def test2733Access(self):
        """
        The solution for getting test2733 was to
        open up access to share in ProxyCleanupHandler.

        This test makes sure it is not too open.
        """

        ### create three users in three groups
        group = self.new_group(perms="rwrw--")

        smember, smember_obj = self.new_client_and_user() # Member of share

        owner = self.new_client(group=group) # Owner of share
        gmember = self.new_client(group=group) # Member of user1's group
        gowner = self.new_client(group=group, admin=True) # Owner of user1's group
        oowner = self.new_client(admin = True) # Admin of a different group

        ## login as user1
        share1 = owner.sf.getShareService()
        update1 = owner.sf.getUpdateService()

        # create image
        img = self.new_image("test2733Access")
        img = update1.saveAndReturnObject(img)
        img.unload()

        # create share
        description = "my description"
        timeout = None
        objects = [img]
        experimenters = [smember_obj]
        guests = []
        enabled = True
        sid = share1.createShare(description, timeout, objects, experimenters, guests, enabled)

        self.assertAccess(owner, sid)
        self.assertAccess(smember, sid)
        self.assertAccess(gmember, sid, False)
        self.assertAccess(gowner, sid, False)
        self.assertAccess(oowner, sid, False)
        self.assertAccess(self.root, sid)

    def test3214(self):
        """
        The solution for 2733 returned too many
        unloaded shares.
        """

        ### create three users in three groups
        group = self.new_group(perms="rwrw--")

        smember, smember_obj = self.new_client_and_user() # Member of share

        owner = self.new_client(group=group) # Owner of share
        gmember = self.new_client(group=group) # Member of user1's group

        ## login as user1
        share1 = owner.sf.getShareService()
        update1 = owner.sf.getUpdateService()

        # create image
        img = self.new_image("test2733Access")
        img = update1.saveAndReturnObject(img)
        img.unload()

        # create share
        description = "my description"
        timeout = None
        objects = [img]
        experimenters = [smember_obj]
        guests = []
        enabled = True
        sid = share1.createShare(description, timeout, objects, experimenters, guests, enabled)

        shares = share1.getOwnShares(True)
        self.assertEquals(1, len(shares))
        self.assert_(shares[0].isLoaded())

        shares = smember.sf.getShareService().getMemberShares(True)
        self.assertEquals(1, len(shares))
        self.assert_(shares[0].isLoaded())

    def test5711(self):
        """
        Recent changes have caused shares to be disabled.
        """
        share = self.client.sf.getShareService()
        update = self.client.sf.getUpdateService()
        admin = self.client.sf.getAdminService()

        self.share_id = share.createShare("", None, [], [], [], True)
        self.client.sf.setSecurityContext(omero.model.ShareI(self.share_id, False))
        ec = admin.getEventContext()
        self.assertEquals(self.share_id, ec.shareId)

    def test5756Raw(self):
        """
        Accessing deleted image in share seems to have changed.
        This tests what happens using the raw API.
        """
        share = self.client.sf.getShareService()
        query = self.client.sf.getQueryService()
        update = self.client.sf.getUpdateService()

        image = self.new_image()
        image = update.saveAndReturnObject(image)
        objects = [image]

        self.share_id = share.createShare("", None, objects, [], [], True)
        new_context = omero.model.ShareI(self.share_id, False)
        old_context = self.client.sf.setSecurityContext(new_context)
        query.get("Image", image.id.val)

        self.client.sf.setSecurityContext(old_context)
        update.deleteObject(image)
        self.client.sf.setSecurityContext(new_context)

        self.assertRaises(omero.ValidationException, query.get, "Image", image.id.val)

    def test5756Wrapped(self):
        """
        Accessing deleted image in share seems to have changed.
        This tests what happens using BlitzGateway wrappers.
        """
        share = self.client.sf.getShareService()
        query = self.client.sf.getQueryService()
        update = self.client.sf.getUpdateService()

        image = self.new_image()
        image = update.saveAndReturnObject(image)
        objects = [image]

        self.share_id = share.createShare("", None, objects, [], [], True)
        new_context = omero.model.ShareI(self.share_id, False)
        old_context = self.client.sf.setSecurityContext(new_context)
        image = query.get("Image", image.id.val)

        from omero.gateway import ImageWrapper, BlitzGateway

        conn = BlitzGateway(client_obj = self.client)
        wrapper = ImageWrapper(conn = conn, obj = image)

        self.client.sf.setSecurityContext(old_context)
        update.deleteObject(image)
        self.client.sf.setSecurityContext(new_context)

        self.assertRaises(IndexError, wrapper.__loadedHotSwap__)

    def test5851(self):
        """
        Expiration is being lost. This test tries to simulate
        a share that was created some time ago and is being
        accessed again.
        """
        client, sid, expiration = self.test1201()
        adminService = client.sf.getAdminService()
        shareService = client.sf.getShareService()
        sessionService = client.sf.getSessionService()

        # Regular reloading
        shareService.setActive(sid, True)
        shareService.activate(sid)
        adminService.getEventContext() # Refreshes
        shareService.deactivate()
        adminService.getEventContext() # Refreshes
        self.assertExpiration(expiration, shareService.getShare(sid))

        # Forced closing
        self.assertEquals(-2, sessionService.closeSession(shareService.getShare(sid)))
        shareService.activate(sid)
        adminService.getEventContext() # Refreshes
        self.assertExpiration(expiration, shareService.getShare(sid))

    def test2513(self):
        """
        Test a few NPE scenarios in IShare
        """
        shares = self.client.sf.getShareService()
        bad_screen = ( shares.createShare, "my description", None,
                [omero.model.ScreenI()], [], [], True)

        self.assertRaises(omero.ValidationException, *bad_screen)

    ########################################
    # Test omero.share functionality (#3527)
    ########################################

    def create_share(self, share, description="desc", timeout=None,
            objects=None, experimenters=None, guests=None, enabled=True):
        return share.createShare(description, timeout, objects, experimenters, guests, enabled)

    def testOSRegularUser(self):
        # """ test regular user can activate a share """

        owner, owner_obj = self.new_client_and_user(perms="rw----") # Owner of share
        member, member_obj = self.new_client_and_user(perms="rw----") # Different group!
        share1 = owner.sf.getShareService()
        update1 = owner.sf.getUpdateService()

        # create image and share
        img = self.new_image("testOSRegularUser")
        img = update1.saveAndReturnObject(img)
        img.unload()
        sid = self.create_share(share1, objects=[img],
                experimenters=[owner_obj, member_obj])

        self.assertAccess(owner, sid)
        self.assertAccess(member, sid)
        # But the user won't be able to just access it plainly
        member_query = member.sf.getQueryService()
        self.assertRaises(omero.SecurityViolation, \
                member_query.get, "Image", img.id.val)

        # But if we let the user pass omero.share it should work.
        member_query.get("Image", img.id.val, {"omero.share":"%s" % sid})

        return img, sid

    def testOSNonMember(self):
        # """ Non-members should not be able to use this method """

        # Run setup
        img, sid = self.testOSRegularUser()
        non_member = self.new_client(perms="rw----")
        non_member_query = non_member.sf.getQueryService()

        # Try to access direct
        self.assertRaises(omero.SecurityViolation, \
                non_member_query.get, "Image", img.id.val)

        # Now try to access via omero.share
        self.assertRaises(omero.SecurityViolation, \
                non_member_query.get, "Image", img.id.val,
                {"omero.share":"%s" % sid})

    def testOSAdminUser(self):
        # """ Admin should be able to log into any share
        img, sid = self.testOSRegularUser()
        root_query = self.root.sf.getQueryService()

        # Try to access direct (in wrong group)
        self.assertRaises(omero.SecurityViolation, \
                root_query.get, "Image", img.id.val)

        # Now try to access via omero.share
        root_query.get("Image", img.id.val, {"omero.share":"%s" % sid})

    def testBadShare(self):
        # Try to access a non-extant share
        # Since the security violation is thrown
        # first, we no longer get a validation exc.
        self.assertRaises(omero.SecurityViolation, \
            self.client.sf.getQueryService().get, "Image", -1, {"omero.share":"-100"})

    def test8513(self):
        owner, owner_obj = self.new_client_and_user(perms="rw----") # Owner of share
        member, member_obj = self.new_client_and_user(perms="rw----") # Different group!

        member_suuid = member.sf.getAdminService().getEventContext().sessionUuid
        owner_suuid = owner.sf.getAdminService().getEventContext().sessionUuid
        
        member_groupId = member.sf.getAdminService().getEventContext().groupId
        owner_groupId = owner.sf.getAdminService().getEventContext().groupId
        
        self.assertFalse(member_suuid == owner_suuid) # just in case

        self.assertFalse(owner_obj.id.val == member_obj.id.val) # just in case

        owner_update = owner.sf.getUpdateService()
        image = self.new_image()
        image = owner_update.saveAndReturnObject(image)
        
        member_update = member.sf.getUpdateService()
        image2 = self.new_image()
        image2 = member_update.saveAndReturnObject(image2)

        share = owner.sf.getShareService()
        sid = self.create_share(share, objects=[image], experimenters=[member_obj])

        self.assertAccess(owner, sid)
        self.assertAccess(member, sid)

        member_share = member.sf.getShareService()
        share_obj = member_share.getShare(sid)
        # Activation shouldn't be needed any more as we pass {'omero.share': <sid>}
        #member_share.activate(long(sid))

        # And the member should be able to use omero.share:sid
        member_query = member.sf.getQueryService()
        
        try:
            rv = member.sf.getQueryService().find("Image", image.id.val, None)
        except omero.SecurityViolation, sv:
            pass
        else:
            self.fail("Error: Member shouldn't access image in share!")
        
        rv = member_query.find("Image", image.id.val, {'omero.share': str(sid), 'omero.group':str(image.details.group.id.val)})
        self.assertEquals(image.id.val, rv.id.val)

        # join share
        user_client = self.new_client(session=member_suuid)
        try:
            # Deactivation shouldn't be needed any more as we pass {'omero.share': <sid>}
            # user_client.sf.getShareService().deactivate()
            user_query = user_client.sf.getQueryService()
            rv = user_query.find("Image", image2.id.val, {'omero.group':str(image2.details.group.id.val)})
            self.assertEquals(image2.id.val, rv.id.val)
        finally:
            user_client.__del__()

        ### Note: The following fails with a security violation since
        ### it is expected that the user first check the contents of
        ### the share and then load those values.
        ### =========================================================
        ## rv = member_query.findAll("Image", None, {"omero.share":"%s" % sid})
        ## self.assertEquals(0, len(rv))
    
    def test8704(self):
        owner, owner_obj = self.new_client_and_user(perms="rw----") # Owner of share
        member, member_obj = self.new_client_and_user(perms="rw----") # Different group!

        member_suuid = member.sf.getAdminService().getEventContext().sessionUuid
        owner_suuid = owner.sf.getAdminService().getEventContext().sessionUuid

        member_groupId = member.sf.getAdminService().getEventContext().groupId
        owner_groupId = owner.sf.getAdminService().getEventContext().groupId

        self.assertFalse(member_suuid == owner_suuid) # just in case

        self.assertFalse(owner_obj.id.val == member_obj.id.val) # just in case

        # create image by owner
        owner_update = owner.sf.getUpdateService()
        image_id = createTestImage(owner.sf)

        p = omero.sys.Parameters()
        p.map = {"id": rlong(long(image_id))}
        sql = "select im from Image im join fetch im.details.owner join fetch im.details.group where im.id=:id order by im.name"
        image = owner.sf.getQueryService().findAllByQuery(sql, p, {'omero.group':str(owner_groupId)})[0]

        rdefs = owner.sf.getQueryService().findAll("RenderingDef", None)

        # create image by member
        member_update = member.sf.getUpdateService()
        image2 = self.new_image()
        image2 = member_update.saveAndReturnObject(image2)

        share = owner.sf.getShareService()
        sid = self.create_share(share, objects=[image], experimenters=[member_obj])

        self.assertAccess(owner, sid)
        self.assertAccess(member, sid)

        member_share = member.sf.getShareService()
        share_obj = member_share.getShare(sid)
        # Activation shouldn't be needed any more as we pass {'omero.share': <sid>}
        #member_share.activate(long(sid))

        # And the member should be able to use omero.share:sid
        member_query = member.sf.getQueryService()

        try:
            rv = member.sf.getQueryService().find("Image", image.id.val, None)
        except omero.SecurityViolation, sv:
            pass
        else:
            self.fail("Error: Member shouldn't access image in share!")

        rv = member_query.find("Image", image.id.val, {'omero.share': str(sid)}) 
        # Not sure which group to set 'omero.group':str(image.details.group.id.val)
        #  or'omero.group':str(member_groupId)

        self.assertEquals(image.id.val, rv.id.val)

        member_tb = member.sf.createThumbnailStore()
        member_tb.setPixelsId(rdefs[0].pixels.id.val, {'omero.share': str(sid)})

        # join share
        user_client = self.new_client(session=member_suuid)
        try:
            # Deactivation shouldn't be needed any more as we pass {'omero.share': <sid>}
            # user_client.sf.getShareService().deactivate()
            user_query = user_client.sf.getQueryService()
            rv = user_query.find("Image", image2.id.val, {'omero.group':str(image2.details.group.id.val)})
            self.assertEquals(image2.id.val, rv.id.val)
        finally:
            user_client.__del__()
    
    # Helpers

    def assertAccess(self, client, sid, success = True):
        share = client.sf.getShareService()
        query = client.sf.getQueryService()

        share_from_ishare = share.getShare(sid)
        self.assertEquals(success, (share_from_ishare is not None))

        # For the moment, preventing all non-IShare download.
        share_from_iquery = query.get("Share", sid)
        self.assertEquals(False, share_from_iquery.isLoaded())

    def assertExpiration(self, expiration, share):
        self.assertEquals(expiration, (share.started.val+share.timeToLive.val))


if __name__ == '__main__':
    unittest.main()
