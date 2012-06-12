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

        r1 = omero.model.RectI()
        roi1 = omero.model.RoiI()
        roi1.setImage(img)
        roi1.addShape(r1)
        roi1 = fix1.save(roi1)

        r2 = omero.model.RectI()
        roi2 = omero.model.RoiI()
        roi2.setImage(img)
        roi2.addShape(r2)
        roi2 = fix2.save(roi2)

        roiOptions = omero.api.RoiOptions()
        for conn in (fix1, fix2):

            roiOptions.userId = omero.rtypes.rlong(fix1.userid)
            r = conn.roi.findByImage(iid, roiOptions)
            self.assertEquals(1, len(r.rois))

            roiOptions.userId = omero.rtypes.rlong(fix2.userid)
            r = conn.roi.findByImage(iid, roiOptions)
            self.assertEquals(1, len(r.rois))

            r = conn.roi.findByImage(iid, None)
            self.assertEquals(2, len(r.rois))


if __name__ == '__main__':
    unittest.main()