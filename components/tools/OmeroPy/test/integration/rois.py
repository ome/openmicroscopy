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
        pix = self.pix(name="#3703")
        img = pix.getImage()
        roi = omero.model.ROII()
        rect1 = self.new_rectangle()
        roi.addShape(rect1)
        rect2 = self.new_rectangle()
        roi.addShape(rect2)
        #roi._shapesSeq[0] = None
        #print roi._getShapes()[0]
        shapes = roi._getShapes()
        shapes[0] = None
        roi._setShapes(shapes)
        #roi.setShape(0,None) # ie roi[0] = None
        img.linkROI(roi)

        pix = self.update.saveAndReturnObject(pix)

        svc = self.client.sf.getRoiService()
        res = svc.findByImage(img.id.val, None)
    
    def testGetROICountNoROI(self):
        """
        Test no ROI couting method
        """
        
        img = self.pix().image

        from omero.gateway import ImageWrapper, BlitzGateway

        conn = BlitzGateway(client_obj = self.client)
        wrapper = ImageWrapper(conn, img)
        self.assertEqual(wrapper.getROICount(),0)
    
    def testGetROICountShape(self):
        """
        Test shape filter in ROI counting method
        """
        
        img = self.pix().image
        
        roi1 = omero.model.ROII()
        roi1.addShape(self.new_rectangle())
        roi1.addShape(self.new_rectangle())
        img.linkROI(roi1)
        roi1  = self.update.saveAndReturnObject(roi1)
        roi2 = omero.model.ROII()
        roi2.addShape(self.new_rectangle())
        roi2.addShape(self.new_ellipse())
        img.linkROI(roi2)
        roi2  = self.update.saveAndReturnObject(roi2)        
        
        from omero.gateway import ImageWrapper, BlitzGateway
        
        conn = BlitzGateway(client_obj = self.client)
        wrapper = ImageWrapper(conn, img)
        
        self.assertEqual(wrapper.getROICount(),2)
        self.assertEqual(wrapper.getROICount("Rectangle"),2)
        self.assertEqual(wrapper.getROICount("Ellipse"),1)
        self.assertEqual(wrapper.getROICount("Line"),0)
        self.assertEqual(wrapper.getROICount(["Rectangle","Ellipse"]),2)
    
    def testGetROICountPermission(self):
        """
        Test permission filter for ROI counting
        """
        
        # Create group with two member and one image
        group = self.new_group(perms="rwrw--")
        owner = self.new_client(group=group) # Owner of share
        member = self.new_client(group=group) # Member of group
        img = self.pix(client=owner).image

        # ROI 1 (rectangle) created by owner      
        roi1 = omero.model.ROII()
        roi1.addShape(self.new_rectangle())
        img.linkROI(roi1)
        roi1  = owner.sf.getUpdateService().saveAndReturnObject(roi1)

        # ROI 2 (ellipse) created by member 
        roi2 = omero.model.ROII()
        roi2.addShape(self.new_ellipse())
        img.linkROI(roi2)
        roi2  = member.sf.getUpdateService().saveAndReturnObject(roi2)

        from omero.gateway import ImageWrapper, BlitzGateway

        # Owner gateway
        conn = BlitzGateway(client_obj = owner)
        wrapper = ImageWrapper(conn, img)
        
        self.assertEqual(wrapper.getROICount("Ellipse"),1)
        self.assertEqual(wrapper.getROICount("Ellipse",None),1)
        self.assertEqual(wrapper.getROICount("Ellipse",1),0)
        self.assertEqual(wrapper.getROICount("Rectangle"),1)
        self.assertEqual(wrapper.getROICount("Rectangle",None),1)
        self.assertEqual(wrapper.getROICount("Rectangle",1),1)
        
        # Member gateway
        conn = BlitzGateway(client_obj = member)
        wrapper = ImageWrapper(conn, img)
            
        self.assertEqual(wrapper.getROICount("Ellipse"),1)
        self.assertEqual(wrapper.getROICount("Ellipse",1),1)
        self.assertEqual(wrapper.getROICount("Rectangle"),1)
        self.assertEqual(wrapper.getROICount("Rectangle",1),0)
    
if __name__ == '__main__':
    unittest.main()
