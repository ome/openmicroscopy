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
    
if __name__ == '__main__':
    unittest.main()