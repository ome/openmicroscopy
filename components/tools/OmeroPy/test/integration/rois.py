#!/usr/bin/env python

"""
   Tests for the IRois interface

   Copyright 2009 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""
import unittest
import integration.library as lib
import omero
from omero.rtypes import *

class TestRois(lib.ITest):

    def teststats1(self):
        rois = self.client.sf.getRoiService()

    def test3703(self):
        """
        Checks for NPE when a shape is null
        """
        img = self.new_image("#3703")
        roi = omero.model.RoiI()
        roi.addShape(omero.model.RectI())
        roi.addShape(omero.model.RectI())
        roi.setShape(0, None)
        img.addRoi(roi)

        img = self.update.saveAndReturnObject(img)

        svc = self.client.sf.getRoiService()
        res = svc.findByImage(img.id.val, None)
    
    def testGetROICountNoROI(self):
        """
        Test no ROI couting method
        """
        
        img = self.new_image("")
        img = self.update.saveAndReturnObject(img)

        from omero.gateway import ImageWrapper, BlitzGateway

        conn = BlitzGateway(client_obj = self.client)
        wrapper = ImageWrapper(conn, img)
        self.assertEqual(wrapper.getROICount(),0)
    
    def testGetROICountShape(self):
        """
        Test shape filter in ROI counting method
        """
        
        img = self.new_image("")
        img = self.update.saveAndReturnObject(img)
        
        roi1 = omero.model.RoiI()
        roi1.addShape(omero.model.RectI())
        roi1.addShape(omero.model.RectI())
        roi1.setImage(img)
        roi1  = self.update.saveAndReturnObject(roi1)
        roi2 = omero.model.RoiI()
        roi2.addShape(omero.model.RectI())
        roi2.addShape(omero.model.EllipseI())
        roi2.setImage(img)
        roi2  = self.update.saveAndReturnObject(roi2)        
        
        from omero.gateway import ImageWrapper, BlitzGateway
        
        conn = BlitzGateway(client_obj = self.client)
        wrapper = ImageWrapper(conn, img)
        
        self.assertEqual(wrapper.getROICount(),2)
        self.assertEqual(wrapper.getROICount("Rect"),2)
        self.assertEqual(wrapper.getROICount("Ellipse"),1)
        self.assertEqual(wrapper.getROICount("Line"),0)
        self.assertEqual(wrapper.getROICount(["Rect","Ellipse"]),2)
    
    def testGetROICountPermission(self):
        """
        Test permission filter for ROI counting
        """
        
        # Create group with two member and one image
        group = self.new_group(perms="rwrw--")
        owner = self.new_client(group=group) # Owner of share
        member = self.new_client(group=group) # Member of group
        img = self.createTestImage(session=owner.sf)

        # ROI 1 (rectangle) created by owner      
        roi1 = omero.model.RoiI()
        roi1.addShape(omero.model.RectI())
        roi1.setImage(img)
        roi1  = owner.sf.getUpdateService().saveAndReturnObject(roi1)

        # ROI 2 (ellipse) created by member 
        roi2 = omero.model.RoiI()
        roi2.addShape(omero.model.EllipseI())
        roi2.setImage(img)
        roi2  = member.sf.getUpdateService().saveAndReturnObject(roi2)

        from omero.gateway import ImageWrapper, BlitzGateway

        # Owner gateway
        conn = BlitzGateway(client_obj = owner)
        wrapper = ImageWrapper(conn, img)
        
        self.assertEqual(wrapper.getROICount("Ellipse"),1)
        self.assertEqual(wrapper.getROICount("Ellipse",None),1)
        self.assertEqual(wrapper.getROICount("Ellipse",1),0)
        self.assertEqual(wrapper.getROICount("Rect"),1)
        self.assertEqual(wrapper.getROICount("Rect",None),1)
        self.assertEqual(wrapper.getROICount("Rect",1),1)
        
        # Member gateway
        conn = BlitzGateway(client_obj = member)
        wrapper = ImageWrapper(conn, img)
            
        self.assertEqual(wrapper.getROICount("Ellipse"),1)
        self.assertEqual(wrapper.getROICount("Ellipse",1),1)
        self.assertEqual(wrapper.getROICount("Rect"),1)
        self.assertEqual(wrapper.getROICount("Rect",1),0)

    def test8990(self):
        # RoiOptions.userId

        group = self.new_group(perms="rwrw--")
        class Fixture(object):
            def __init__(this):
                this.client, this.user = self.new_client_and_user(group=group)
                this.userid = this.user.id.val
                this.up = this.client.sf.getUpdateService()
                this.roi = this.client.sf.getRoiService()
            def save(this, obj):
                return this.up.saveAndReturnObject(obj)

        fix1 = Fixture()
        fix2 = Fixture()

        img = self.new_image()
        img = fix1.save(img)
        img.unload()
        iid = img.id.val

        class Roi(object):
            def __init__(self, fix, ns, z, t):
                self.fix = fix
                self.ns = ns
                self.z = z
                self.t = t
                self.shape = omero.model.RectI()
                self.shape.setTheZ(omero.rtypes.rint(z))
                self.shape.setTheT(omero.rtypes.rint(t))
                self.obj = omero.model.RoiI()
                self.obj.namespaces = [ns]
                self.obj.setImage(img)
                self.obj.addShape(self.shape)
                self.obj = fix.save(self.obj)
                self.id = self.obj.id.val

        r1 = Roi(fix1, "A", 0, 0)
        r2 = Roi(fix1, "B", 1, 1)
        r3 = Roi(fix2, "A", 0, 0)
        r4 = Roi(fix2, "B", 1, 1)

        def assertRois(fix, userid, ns, z, t,\
                byimage, byrois, byns, byplane):

                roiOptions = omero.api.RoiOptions()
                if userid is not None:
                    roiOptions.userId = omero.rtypes.rlong(userid)
                if ns is not None:
                    roiOptions.namespace = omero.rtypes.rstring(ns)

                svc = fix.roi

                r = svc.findByImage(iid, roiOptions)
                self.assertEquals(byimage, len(r.rois))

                count = 0
                for r in (r1, r2, r3, r4):
                    r = svc.findByRoi(r.id, roiOptions)
                    count += len(r.rois)
                self.assertEquals(byrois, count)

                r = svc.findByPlane(iid, z, t, roiOptions)
                self.assertEquals(byplane, len(r.rois))

        for x, y in ((fix1, fix1), (fix1, fix2), (fix2, fix1), (fix2, fix2)):
            assertRois(x, y.userid, None, 0, 0,   2, 2, 2, 1)
            assertRois(x, y.userid, None, 0, 1,   2, 2, 2, 0) #DNE
            assertRois(x, y.userid, "A",  0, 0,   1, 2, 1, 1)
            assertRois(x, y.userid, "B",  0, 0,   1, 2, 1, 1)

        for x in (fix1, fix2):
            assertRois(x, None,     None,  0, 0,   4, 4, 4, 2)
            assertRois(x, None,     None,  0, 1,   4, 4, 4, 0) #DNE
            assertRois(x, None,     "A",   0, 0,   2, 4, 2, 2)
            assertRois(x, None,     "B",   0, 0,   2, 4, 2, 2)
            assertRois(x, None,     "B",   0, 0,   2, 4, 2, 2)



if __name__ == '__main__':
    unittest.main()