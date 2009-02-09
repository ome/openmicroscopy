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
from omero_model_ImageI import ImageI
from omero_model_DatasetI import DatasetI
from omero_model_ProjectI import ProjectI
from omero_model_ExperimenterI import ExperimenterI
from omero_model_ExperimenterGroupI import ExperimenterGroupI
from omero_model_GroupExperimenterMapI import GroupExperimenterMapI
from omero_model_DatasetImageLinkI import DatasetImageLinkI
from omero_model_ProjectDatasetLinkI import ProjectDatasetLinkI
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
        
        # create image
        acquired = long(time.time()*1000)
        img = ImageI()
        img.setName(rstring('test1154-img-%s' % (uuid)))
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
        
        # Here we assume that this test is not run within the last 1 second
        start = acquired - 86400
        end = acquired + 1
        
        counter = timeline.countByPeriod(['Image'], rtime(long(start)), rtime(long(end)))
        self.assertEquals(counter['Image'], 1)
        
        p = omero.sys.Parameters()
        p.map = {}
        p.map["experimenter"] = rlong(admin.getEventContext().userId)
        p.map["start"] = rtime(long(start))
        p.map["end"] = rtime(long(end))

        res = timeline.getMostRecentObjects(['Image'], p, False)["Image"]
        self.assertEquals(1, len(res))
        
        self.root.sf.closeOnDestroy()
    
    def test1173(self):
        uuid = self.root.sf.getAdminService().getEventContext().sessionUuid
        update = self.root.sf.getUpdateService()
        timeline = self.root.sf.getTimelineService()
        
        # create image
        ds = DatasetI()
        ds.setName(rstring('test1154-ds-%s' % (uuid)))
        ds = update.saveAndReturnObject(ds)
        ds.unload()
        
        # Here we assume that this test is not run within the last 1 second
        start = long(time.time()*1000 - 86400) 
        end = long(time.time()*1000 + 86400) 
        
        p = omero.sys.Parameters()
        p.map = {}
        p.map["id"] = rlong(self.new_user().id.val)
        f = omero.sys.Filter()
        f.limit = rint(10)
        p.theFilter = f
        print timeline.getEventsByPeriod(rtime(long(start)), rtime(long(end)), p)
        self.assert_(timeline.getEventsByPeriod(rtime(long(start)), rtime(long(end)), p) > 0)
        
        self.root.sf.closeOnDestroy()
    
if __name__ == '__main__':
    unittest.main()
