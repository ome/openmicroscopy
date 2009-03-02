#!/usr/bin/env python

"""
   Integration test focused on the omero.api.IShare interface
   a running server.

   Copyright 2008 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""
import time
import datetime
import unittest
import test.integration.library as lib
import omero, uuid
import omero_Constants_ice
from omero.rtypes import *

class TestITimeline(lib.ITest):

    def testGeneral(self):
        user = self.new_user().omeName.val
        client = omero.client()
        sf = client.createSession(user, "")

        uuid = sf.getAdminService().getEventContext().sessionUuid
        admin = sf.getAdminService()
        update = sf.getUpdateService()
        timeline = sf.getTimelineService()
        
        im_ids = dict()
        for i in range(0,10):
            # create image
            acquired = long(time.time()*1000)
            img = omero.model.ImageI()
            img.setName(rstring('test-img-%s' % (uuid)))
            img.setAcquisitionDate(rtime(acquired))
            
            # permission 'rw----':
            img.details.permissions.setUserRead(True)
            img.details.permissions.setUserWrite(True)
            img.details.permissions.setGroupRead(False)
            img.details.permissions.setGroupWrite(False)
            img.details.permissions.setWorldRead(False)
            img.details.permissions.setWorldWrite(False)
            img = update.saveAndReturnObject(img)
            img.unload()
            
            im_ids[i] = [img.id.val, acquired]
        
        
        
        # Here we assume that this test is not run within the last 1 second
        start = acquired - 86400
        end = acquired + 1
        
        p = omero.sys.Parameters()
        p.map = {}
        f = omero.sys.Filter()
        f.ownerId = rlong(admin.getEventContext().userId)
        f.groupId = rlong(admin.getEventContext().groupId)
        p.theFilter = f
        
        counter = timeline.countByPeriod(['Image'], rtime(long(start)), rtime(long(end)), p)
        self.assertEquals(counter['Image'], 10)
        
        p2 = omero.sys.Parameters()
        p2.map = {}
        f2 = omero.sys.Filter()
        f2.ownerId = rlong(admin.getEventContext().userId)
        f2.groupId = rlong(admin.getEventContext().groupId)
        f2.limit = rint(5)
        p2.theFilter = f2

        #p.map["start"] = rtime(long(start))
        #p.map["end"] = rtime(long(end))

        res = timeline.getMostRecentObjects(['Image'], p2, False)["Image"]
        self.assertEquals(5, len(res))

        # 1st element should be the 9th from the im_ids
        self.assertEquals(im_ids[9][0], res[0].id.val)
        # 2nd element should be the 8th from the im_ids
        self.assertEquals(im_ids[8][0], res[1].id.val)
        # 3rd element should be the 7th from the im_ids
        self.assertEquals(im_ids[7][0], res[2].id.val)
        # 4th element should be the 6th from the im_ids
        self.assertEquals(im_ids[6][0], res[3].id.val)
        # 5th element should be the 5th from the im_ids
        self.assertEquals(im_ids[5][0], res[4].id.val)


    def test1173(self):
        uuid = self.root.sf.getAdminService().getEventContext().sessionUuid
        update = self.root.sf.getUpdateService()
        timeline = self.root.sf.getTimelineService()
        
        # create image
        ds = omero.model.DatasetI()
        ds.setName(rstring('test1154-ds-%s' % (uuid)))
        ds = update.saveAndReturnObject(ds)
        ds.unload()
        
        # Here we assume that this test is not run within the last 1 second
        start = long(time.time()*1000 - 86400) 
        end = long(time.time()*1000 + 86400) 
        
        p = omero.sys.Parameters()
        p.map = {}
        f = omero.sys.Filter()
        f.ownerId = rlong(self.new_user().id.val)
        p.theFilter = f
        self.assert_(timeline.getEventLogsByPeriod(rtime(long(start)), rtime(long(end)), p) > 0)
        
        self.root.sf.closeOnDestroy()
    
    def test1175(self):
        uuid = self.root.sf.getAdminService().getEventContext().sessionUuid
        update = self.root.sf.getUpdateService()
        timeline = self.root.sf.getTimelineService()
        
        # create dataset
        ds = omero.model.DatasetI()
        ds.setName(rstring('test1154-ds-%s' % (uuid)))
        ds = update.saveAndReturnObject(ds)
        ds.unload()
        
        # create tag
        ann = omero.model.TagAnnotationI()
        ann.textValue = rstring('tag-%s' % (uuid))
        ann.setDescription(rstring('tag-%s' % (uuid)))
        t_ann = omero.model.DatasetAnnotationLinkI()
        t_ann.setParent(ds)
        t_ann.setChild(ann)
        update.saveObject(t_ann)
        
        p = omero.sys.Parameters()
        p.map = {}
        f = omero.sys.Filter()
        f.ownerId = rlong(0)
        f.limit = rint(10)
        p.theFilter = f
        res = timeline.getMostRecentAnnotationLinks(None, ['TagAnnotation'], None, p)
        self.assert_(len(res) > 0)
        
        self.root.sf.closeOnDestroy()
    
if __name__ == '__main__':
    unittest.main()
