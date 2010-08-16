#!/usr/bin/env python

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
import unittest, time
import integration.library as lib
import omero
from omero.rtypes import rtime, rlong, rstring, rlist, rint
from omero_model_ExperimenterI import ExperimenterI
from omero_model_ExperimenterGroupI import ExperimenterGroupI
from omero_model_PermissionsI import PermissionsI
import omero_api_Gateway_ice

from integration.helpers import createTestImage

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
        p = PermissionsI()
        p.setUserRead(True)
        p.setUserWrite(True)
        p.setGroupRead(False)
        p.setGroupWrite(False)
        p.setWorldRead(False)
        p.setWorldWrite(False)
        new_gr1.details.permissions = p
        gid = admin.createGroup(new_gr1)
        privateGroup = admin.getGroup(gid)
        self.assertEquals('rw----', str(privateGroup.details.permissions))
        listOfGroups.append(privateGroup)
        
        #group2 - read-only
        new_gr2 = ExperimenterGroupI()
        new_gr2.name = rstring(group2name)
        p2 = PermissionsI()
        p2.setUserRead(True)
        p2.setUserWrite(True)
        p2.setGroupRead(True)
        p2.setGroupWrite(False)
        p2.setWorldRead(False)
        p2.setWorldWrite(False)
        new_gr2.details.permissions = p2
        gid2 = admin.createGroup(new_gr2)
        readOnlyGroup = admin.getGroup(gid2)
        self.assertEquals('rwr---', str(readOnlyGroup.details.permissions))
        listOfGroups.append(readOnlyGroup)
        
        #group3 - collaborative
        new_gr3 = ExperimenterGroupI()
        new_gr3.name = rstring(group3name)
        p = PermissionsI()
        p.setUserRead(True)
        p.setUserWrite(True)
        p.setGroupRead(True)
        p.setGroupWrite(True)
        p.setWorldRead(False)
        p.setWorldWrite(False)
        new_gr3.details.permissions = p
        gid3 = admin.createGroup(new_gr3)
        collaborativeGroup = admin.getGroup(gid3)
        self.assertEquals('rwrw--', str(collaborativeGroup.details.permissions))
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
        client_share1 = omero.client()
        client_share1.createSession(user1.omeName.val,"ome")
        
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
        client_share1.sf.setSecurityContext(omero.model.ExperimenterGroupI(gid2, False))
        #print a.getEventContext()
        
        # create image and get thumbnail (in read-only group)
        readOnlyImageId = createTestImage(client_share1.sf)
        self.getThumbnail(client_share1.sf, readOnlyImageId)
        
        # change user into collaborative group. Use object Ids for this, NOT objects from a different context
        a.setDefaultGroup(me, omero.model.ExperimenterGroupI(gid3, False))
        client_share1.sf.setSecurityContext(omero.model.ExperimenterGroupI(gid3, False))
        
        # create image and get thumbnail (in collaborative group)
        collaborativeImageId = createTestImage(client_share1.sf)
        self.getThumbnail(client_share1.sf, collaborativeImageId)
        
        # check that we can't get thumbnails for images in other groups
        self.assertEquals(None, self.getThumbnail(client_share1.sf, privateImageId))
        self.assertEquals(None, self.getThumbnail(client_share1.sf, readOnlyImageId))
        
        
        # now check that the 'owner' of each group can see all 3 thumbnails.
        ## login as owner (into private group)
        owner_client = omero.client()
        owner_client.createSession(newOwner.omeName.val,"ome")
        
        self.getThumbnail(owner_client.sf, privateImageId)
        # check that we can't get thumbnails for images in other groups
        self.assertEquals(None, self.getThumbnail(owner_client.sf, readOnlyImageId))
        self.assertEquals(None, self.getThumbnail(owner_client.sf, collaborativeImageId))
        
        # change owner into read-only group.
        o = client_share1.sf.getAdminService()
        me = o.getExperimenter(o.getEventContext().userId)
        o.setDefaultGroup(me, omero.model.ExperimenterGroupI(gid2, False))
        owner_client.sf.setSecurityContext(omero.model.ExperimenterGroupI(gid2, False))

        self.getThumbnail(owner_client.sf, readOnlyImageId)
        # check that we can't get thumbnails for images in other groups
        self.assertEquals(None, self.getThumbnail(owner_client.sf, privateImageId))
        self.assertEquals(None, self.getThumbnail(owner_client.sf, collaborativeImageId))
        
        # change owner into collaborative group.
        o.setDefaultGroup(me, omero.model.ExperimenterGroupI(gid3, False))
        owner_client.sf.setSecurityContext(omero.model.ExperimenterGroupI(gid3, False))
        
        self.getThumbnail(owner_client.sf, collaborativeImageId)
        # check that we can't get thumbnails for images in other groups
        self.assertEquals(None, self.getThumbnail(owner_client.sf, privateImageId))
        self.assertEquals(None, self.getThumbnail(owner_client.sf, readOnlyImageId))
        
        
        # now check that the 'user2' of each group can see all thumbnails except private.
        ## login as user2 (into private group)
        user2_client = omero.client()
        user2_client.createSession(user2.omeName.val,"ome")
        
        # check that we can't get thumbnails for any images in private group
        self.assertEquals(None, self.getThumbnail(user2_client.sf, privateImageId))
        self.assertEquals(None, self.getThumbnail(user2_client.sf, readOnlyImageId))
        self.assertEquals(None, self.getThumbnail(user2_client.sf, collaborativeImageId))
        
        # change owner into read-only group.
        u = user2_client.sf.getAdminService()
        me = u.getExperimenter(u.getEventContext().userId)
        u.setDefaultGroup(me, omero.model.ExperimenterGroupI(gid2, False))
        user2_client.sf.setSecurityContext(omero.model.ExperimenterGroupI(gid2, False))

        self.getThumbnail(user2_client.sf, readOnlyImageId)
        # check that we can't get thumbnails for images in other groups
        self.assertEquals(None, self.getThumbnail(user2_client.sf, privateImageId))
        self.assertEquals(None, self.getThumbnail(user2_client.sf, collaborativeImageId))
        
        # change owner into collaborative group.
        u.setDefaultGroup(me, omero.model.ExperimenterGroupI(gid3, False))
        user2_client.sf.setSecurityContext(omero.model.ExperimenterGroupI(gid3, False))
        
        self.getThumbnail(user2_client.sf, collaborativeImageId)
        # check that we can't get thumbnails for images in other groups
        self.assertEquals(None, self.getThumbnail(user2_client.sf, privateImageId))
        self.assertEquals(None, self.getThumbnail(user2_client.sf, readOnlyImageId))
        
        
    def getThumbnail(self, session, imageId):
    
        gateway = session.createGateway()
        thumbnailStore = session.createThumbnailStore()
    
        image = gateway.getImage(imageId)
        if image is None:
            return None
        pId = image.getPrimaryPixels().getId().getValue()
    
        pixelsIds = [pId]
        s = thumbnailStore.getThumbnailByLongestSideSet(rint(16), pixelsIds)
        self.assertEqual(1, len(s))
        s = thumbnailStore.getThumbnailSet(rint(16), rint(16), pixelsIds)
        self.assertEqual(1, len(s))
    
        thumbnailStore.setPixelsId(pId)
        t = thumbnailStore.getThumbnail(rint(16),rint(16))
        self.assertNotEqual(None, t)
        t = thumbnailStore.getThumbnailByLongestSide(rint(16))
        self.assertNotEqual(None, t)
    
        thumbnailStore.close()
        gateway.close()
        return t
        
if __name__ == '__main__':
    unittest.main()
