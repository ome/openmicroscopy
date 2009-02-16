#!/usr/bin/env python

"""
   Integration test focused on the omero.api.IShare interface
   a running server.

   Copyright 2008 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""
import unittest, time
import test.integration.library as lib
import omero
import omero_Constants_ice
from omero_model_PixelsI import PixelsI
from omero_model_ImageI import ImageI
from omero_model_DatasetI import DatasetI
from omero_model_ExperimenterI import ExperimenterI
from omero_model_ExperimenterGroupI import ExperimenterGroupI
from omero_model_GroupExperimenterMapI import GroupExperimenterMapI
from omero_model_DatasetImageLinkI import DatasetImageLinkI
from omero.rtypes import rtime, rlong, rstring, rlist

class TestIShare(lib.ITest):

    def testThatPermissionsAreDefaultPrivate(self):
        i = omero.model.ImageI()
        i.name = rstring("name")
        i.acquisitionDate = rtime(0)
        i = self.client.sf.getUpdateService().saveAndReturnObject(i)
        self.assert_( not i.details.permissions.isGroupRead() )
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
        self.id = share.createShare(description, timeout, objects,experimenters, guests, enabled)
        
        self.assert_(len(share.getContents(self.id)) == 0)
        
        d = omero.model.DatasetI()
        d.setName(rstring("d"))
        # set permissions RW----
        d.details.permissions.setUserRead(True)
        d.details.permissions.setUserWrite(True)
        d.details.permissions.setGroupRead(False)
        d.details.permissions.setGroupWrite(False)
        d.details.permissions.setWorldRead(False)
        d.details.permissions.setWorldWrite(False)
        d = update.saveAndReturnObject(d)
        share.addObjects(self.id, [d])

        self.assert_(len(share.getContents(self.id)) == 1)

        ds = []
        for i in range(0,4):
            ds.append(omero.model.DatasetI())
            ds[i].setName(rstring("ds%i" % i))
        ds = update.saveAndReturnArray(ds)
        share.addObjects(self.id, ds)

        self.assert_(share.getContentSize(self.id) == 5)
        
        self.assert_(len(share.getAllUsers(self.id)) == 2)
        
        #check access by a member to see the content
        client_guest_read_only = omero.client()
        client_guest_read_only.createSession(test_user.omeName.val,"ome")
        
        #get dataset - not allowed
        query = client_guest_read_only.sf.getQueryService()
        try:
            query.find("Dataset",d.id.val)
        except Exception, x:
            pass
        
        share_read_only = client_guest_read_only.sf.getShareService()
        share_read_only.activate(self.id)
        content = share_read_only.getContents(self.id)
        self.assert_(share_read_only.getContentSize(self.id) == 5)
        
        #check access by a member to add comments
        client_guest = omero.client()
        client_guest.createSession(test_user.omeName.val,"ome")
        
        share_guest = client_guest.sf.getShareService()
        share_guest.addComment(self.id,"comment for share %i" % self.id)
        
        self.assert_(len(share_guest.getComments(self.id)) == 1)
        
        # get share key and join directly
        s = share.getShare(self.id)
        
        # THIS IS NOT ALLOWED:
        client_share = omero.client()
        client_share.createSession(s.uuid,s.uuid)
        share1 = client_share.sf.getShareService()
        self.assert_(len(share1.getAllShares(True)) > 0)
        # THIS IS NOT ALLOWED: FINISH
        
        # guest looks in to the share
        guest_email = "ident@emaildomain.com"
        token =  s.uuid
        client_share_guest = omero.client()
        client_share_guest.createSession("guest","guest") # maybe there can be some verification of identity by (share_key, email) - both params could be sent to email
        
        share2 = client_share_guest.sf.getShareService()
        # Doesn't exist # share2.getAllGuestShares(guest_email)
        # Doesn't exist # self.assert_(share2.getGuestShare(token) > 0)
        share2.addComment(self.id,"guest comment for share %i" % self.id)
        self.assert_(len(share2.getComments(self.id)) == 1)

    def testRetrieval(self):
        shs = self.root.sf.getShareService()
        shs.getAllShares(True)
    
    def test1154(self):
        uuid = self.root.sf.getAdminService().getEventContext().sessionUuid
        share = self.root.sf.getShareService()
        query = self.root.sf.getQueryService()
        update = self.root.sf.getUpdateService()
        admin = self.root.sf.getAdminService()
        
        ### create two users in one group
        #group1
        new_gr1 = ExperimenterGroupI()
        new_gr1.name = rstring("group1_%s" % uuid)
        gid = admin.createGroup(new_gr1)
        
        #new user1
        new_exp = ExperimenterI()
        new_exp.omeName = rstring("user1_%s" % uuid)
        new_exp.firstName = rstring("New")
        new_exp.lastName = rstring("Test")
        new_exp.email = rstring("newtest@emaildomain.com")
        
        defaultGroup = admin.getGroup(gid)
        listOfGroups = list()
        listOfGroups.append(admin.lookupGroup("user"))
        
        eid = admin.createExperimenterWithPassword(new_exp, rstring("ome"), defaultGroup, listOfGroups)
        
        #new user2
        new_exp2 = ExperimenterI()
        new_exp2.omeName = rstring("user2_%s" % uuid)
        new_exp2.firstName = rstring("New2")
        new_exp2.lastName = rstring("Test2")
        new_exp2.email = rstring("newtest2@emaildomain.com")
        
        eid2 = admin.createExperimenterWithPassword(new_exp2, rstring("ome"), defaultGroup, listOfGroups)
        
        ## get users
        user1 = admin.getExperimenter(eid)
        user2 = admin.getExperimenter(eid2)
        
        ## login as user1 
        client_share1 = omero.client()
        client_share1.createSession(user1.omeName.val,"ome")
        share1 = client_share1.sf.getShareService()
        update1 = client_share1.sf.getUpdateService()
        
        # create image
        img = ImageI()
        img.setName(rstring('test1154-img-%s' % (uuid)))
        img.setAcquisitionDate(rtime(0))
        
        # permission 'rw----':
        img.details.permissions.setUserRead(True)
        img.details.permissions.setUserWrite(True)
        img.details.permissions.setGroupRead(False)
        img.details.permissions.setGroupWrite(False)
        img.details.permissions.setWorldRead(False)
        img.details.permissions.setWorldWrite(False)
        img = update1.saveAndReturnObject(img)
        img.unload()
        
        # create share
        description = "my description"
        timeout = None
        objects = [img]
        experimenters = [user2]
        guests = []
        enabled = True
        sid = share.createShare(description, timeout, objects,experimenters, guests, enabled)
        
        self.assert_(len(share1.getContents(sid)) == 1)
        
        ## login as user2
        client_share2 = omero.client()
        client_share2.createSession(user2.omeName.val,"ome")
        share2 = client_share2.sf.getShareService()
        query2 = client_share2.sf.getQueryService()
        
        sh = share2.getShare(sid)
        content = share2.getContents(sid)
        self.assert_(len(share2.getContents(sid)) == 1)
        
        # get shared image when share is activated
        share2.activate(sid)

        p = omero.sys.Parameters()
        p.map = {}
        p.map["ids"] = rlist([rlong(img.id.val)])
        sql = "select im from Image im where im.id in (:ids) order by im.name"
        res = query2.findAllByQuery(sql, p)
        self.assert_(len(res) == 1)
        for e in res:
            self.assert_(e.id.val == img.id.val)
        
        client_share1.sf.closeOnDestroy()
        client_share2.sf.closeOnDestroy()
        
    
    def test1157(self):
        uuid = self.root.sf.getAdminService().getEventContext().sessionUuid
        share = self.root.sf.getShareService()
        query = self.root.sf.getQueryService()
        update = self.root.sf.getUpdateService()
        admin = self.root.sf.getAdminService()
        
        ### create two users in one group
        #group1
        new_gr1 = ExperimenterGroupI()
        new_gr1.name = rstring("group1_%s" % uuid)
        gid = admin.createGroup(new_gr1)
        
        #new user1
        new_exp = ExperimenterI()
        new_exp.omeName = rstring("user1_%s" % uuid)
        new_exp.firstName = rstring("New")
        new_exp.lastName = rstring("Test")
        new_exp.email = rstring("newtest@emaildomain.com")
        
        defaultGroup = admin.getGroup(gid)
        listOfGroups = list()
        listOfGroups.append(admin.lookupGroup("user"))
        
        eid = admin.createExperimenterWithPassword(new_exp, rstring("ome"), defaultGroup, listOfGroups)
        
        #new user2
        new_exp2 = ExperimenterI()
        new_exp2.omeName = rstring("user2_%s" % uuid)
        new_exp2.firstName = rstring("New2")
        new_exp2.lastName = rstring("Test2")
        new_exp2.email = rstring("newtest2@emaildomain.com")
        
        eid2 = admin.createExperimenterWithPassword(new_exp2, rstring("ome"), defaultGroup, listOfGroups)
        
        ## get users
        user1 = admin.getExperimenter(eid)
        user2 = admin.getExperimenter(eid2)
        
        ## login as user1 
        client_share1 = omero.client()
        client_share1.createSession(user1.omeName.val,"ome")
        share1 = client_share1.sf.getShareService()
        update1 = client_share1.sf.getUpdateService()
        
        # create image
        img = ImageI()
        img.setName(rstring('test1154-img-%s' % (uuid)))
        img.setAcquisitionDate(rtime(0))
        
        # permission 'rw----':
        img.details.permissions.setUserRead(True)
        img.details.permissions.setUserWrite(True)
        img.details.permissions.setGroupRead(False)
        img.details.permissions.setGroupWrite(False)
        img.details.permissions.setWorldRead(False)
        img.details.permissions.setWorldWrite(False)
        img = update1.saveAndReturnObject(img)
        img.unload()
        
        # create share
        description = "my description"
        timeout = None
        objects = [img]
        experimenters = [user2]
        guests = []
        enabled = True
        sid = share.createShare(description, timeout, objects,experimenters, guests, enabled)
        self.assert_(len(share1.getContents(sid)) == 1)
        # add comment by the owner
        share.addComment(sid, 'test comment by the owner %s' % (uuid))
        
        ## login as user2
        client_share2 = omero.client()
        client_share2.createSession(user2.omeName.val,"ome")
        share2 = client_share2.sf.getShareService()
        query2 = client_share2.sf.getQueryService()
        
        sh = share2.getShare(sid)
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
        
        client_share1.sf.closeOnDestroy()
        client_share2.sf.closeOnDestroy()
    
    def test1172(self):
        uuid = self.root.sf.getAdminService().getEventContext().sessionUuid
        share = self.root.sf.getShareService()
        query = self.root.sf.getQueryService()
        update = self.root.sf.getUpdateService()
        admin = self.root.sf.getAdminService()
        cntar = self.root.sf.getContainerService()
        
        ### create user
        #group1
        new_gr1 = ExperimenterGroupI()
        new_gr1.name = rstring("group1_%s" % uuid)
        gid = admin.createGroup(new_gr1)
        
        #new user1
        new_exp = ExperimenterI()
        new_exp.omeName = rstring("user1_%s" % uuid)
        new_exp.firstName = rstring("New")
        new_exp.lastName = rstring("Test")
        new_exp.email = rstring("newtest@emaildomain.com")
        
        defaultGroup = admin.getGroup(gid)
        listOfGroups = list()
        listOfGroups.append(admin.lookupGroup("user"))
        
        eid = admin.createExperimenterWithPassword(new_exp, rstring("ome"), defaultGroup, listOfGroups)
        
        ## get user
        user1 = admin.getExperimenter(eid)
        
        #create dataset with image
        #dataset with image
        ds = omero.model.DatasetI()
        ds.setName(rstring("dataset-%s" % (uuid)))
        # set permissions RW----
        ds.details.permissions.setUserRead(True)
        ds.details.permissions.setUserWrite(True)
        ds.details.permissions.setGroupRead(False)
        ds.details.permissions.setGroupWrite(False)
        ds.details.permissions.setWorldRead(False)
        ds.details.permissions.setWorldWrite(False)
        ds = update.saveAndReturnObject(ds)
        ds.unload()
        
        # create image
        img = ImageI()
        img.setName(rstring('test-img in dataset-%s' % (uuid)))
        img.setAcquisitionDate(rtime(0))
        # permission 'rw----':
        img.details.permissions.setUserRead(True)
        img.details.permissions.setUserWrite(True)
        img.details.permissions.setGroupRead(False)
        img.details.permissions.setGroupWrite(False)
        img.details.permissions.setWorldRead(False)
        img.details.permissions.setWorldWrite(False)
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
        p.map["eid"] = rlong(eid)
        sql = "select e from Experimenter e where e.id =:eid order by e.omeName"
        ms = query.findAllByQuery(sql, p)
        sid = share.createShare(("test-share-%s" % uuid), rtime(long(time.time()*1000 + 86400)) , items, ms, [], True)
        
        # USER RETRIEVAL
        ## login as user1
        client_share1 = omero.client()
        client_share1.createSession(user1.omeName.val,"ome")
        share1 = client_share1.sf.getShareService()
        query1 = client_share1.sf.getQueryService()
        cntar1 = client_share1.sf.getContainerService()
        
        sh = share1.getShare(sid)
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
        except omero.SecurityViolation:
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

        client_share1.sf.closeOnDestroy()
        self.root.sf.closeOnDestroy()

    def test1179(self):
        rdefs = self.root.sf.getQueryService().findAll("RenderingDef", None)
        if len(rdefs) == 0:
            raise "Must have at least one rendering def"
        share = self.root.sf.getShareService()
        sid = share.createShare("", None, [], [], [], True)
        share.activate(sid)
        tb = self.root.sf.createThumbnailStore()
        tb.setPixelsId(rdefs[0].pixels.id.val)

if __name__ == '__main__':
    unittest.main()
