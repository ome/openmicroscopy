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
        uuid = self.root.sf.getAdminService().getEventContext().sessionUuid
        admin = self.root.sf.getAdminService()
        update = self.root.sf.getUpdateService()
        timeline = self.root.sf.getTimelineService()
        
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
        img = update.saveAndReturnObject(img)
        img.unload()
        
        dt = datetime.datetime.utcnow()
        t = time.mktime(dt.timetuple())
        start = t-86400
        end = t
        
        counter = timeline.countByPeriod(['Image'], rtime(long(start)), rtime(long(end)))
        self.assert_(counter['Image'] > 0)
        
        p = omero.sys.Parameters()
        p.map = {}
        p.map["experimenter"] = rlong(admin.getEventContext().userId)
        p.map["start"] = rtime(long(start))
        p.map["end"] = rtime(long(end))

        res = timeline.getMostRecentObjects(['Image'], p, False)
        self.assert_(len(res)>0)
        
        self.assert_(counter['Image'] == len(res))
        
        self.root.sf.closeOnDestroy()
    
if __name__ == '__main__':
    unittest.main()
