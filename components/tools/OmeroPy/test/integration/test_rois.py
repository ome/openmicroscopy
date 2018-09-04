#!/usr/bin/env python
# -*- coding: utf-8 -*-

"""
   Tests for the IRois interface

   Copyright 2009-2014 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""
from omero.testlib import ITest
import omero
from omero.rtypes import rdouble, rstring, rint
import pytest
from omero.gateway import ColorHolder


class TestRois(ITest):

    def teststats1(self):
        self.client.sf.getRoiService()

    def test3703(self):
        """
        Checks for NPE when a shape is null
        """
        img = self.new_image("#3703")
        roi = omero.model.RoiI()
        roi.addShape(omero.model.RectangleI())
        roi.addShape(omero.model.RectangleI())
        roi.setShape(0, None)
        img.addRoi(roi)

        img = self.update.saveAndReturnObject(img)

        svc = self.client.sf.getRoiService()
        svc.findByImage(img.id.val, None)

    def testGetROICount(self):
        """
        Test no ROI couting method
        """

        # Create group with two member and one image
        group = self.new_group(perms="rwrw--")
        owner = self.new_client(group=group)  # Owner of share
        member = self.new_client(group=group)  # Member of group
        img = self.create_test_image(session=owner.sf)

        from omero.gateway import ImageWrapper, BlitzGateway
        conn = BlitzGateway(client_obj=owner)

        # Test no ROI count
        wrapper = ImageWrapper(conn, img)
        assert wrapper.getROICount() == 0

        # Test ROI shape
        roi1 = omero.model.RoiI()
        roi1.addShape(omero.model.RectangleI())
        roi1.addShape(omero.model.RectangleI())
        roi1.setImage(img)
        roi1 = owner.sf.getUpdateService().saveAndReturnObject(roi1)

        roi2 = omero.model.RoiI()
        roi2.addShape(omero.model.RectangleI())
        roi2.addShape(omero.model.EllipseI())
        roi2.setImage(img)
        roi2 = owner.sf.getUpdateService().saveAndReturnObject(roi2)

        wrapper = ImageWrapper(conn, img)
        assert wrapper.getROICount() == 2
        assert wrapper.getROICount("Rectangle") == 2
        assert wrapper.getROICount("Ellipse") == 1
        assert wrapper.getROICount("Line") == 0
        assert wrapper.getROICount(["Rectangle", "Ellipse"]) == 2

        # Test ROI permissions
        roi3 = omero.model.RoiI()
        roi3.addShape(omero.model.EllipseI())
        roi3.setImage(img)
        roi3 = member.sf.getUpdateService().saveAndReturnObject(roi3)
        assert wrapper.getROICount() == 3
        assert wrapper.getROICount(filterByCurrentUser=True) == 2
        assert wrapper.getROICount("Ellipse") == 2
        assert wrapper.getROICount("Ellipse", None) == 2
        assert wrapper.getROICount("Ellipse", 1) == 1
        assert wrapper.getROICount("Ellipse", True) == 1
        assert wrapper.getROICount("Rectangle") == 2
        assert wrapper.getROICount("Rectangle", None) == 2
        assert wrapper.getROICount("Rectangle", 1) == 2
        assert wrapper.getROICount("Rectangle", True) == 2

        # Member gateway
        conn = BlitzGateway(client_obj=member)
        wrapper = ImageWrapper(conn, img)
        assert wrapper.getROICount() == 3
        assert wrapper.getROICount(filterByCurrentUser=True) == 1
        assert wrapper.getROICount("Ellipse") == 2
        assert wrapper.getROICount("Ellipse", None) == 2
        assert wrapper.getROICount("Ellipse", 1) == 1
        assert wrapper.getROICount("Ellipse", True) == 1
        assert wrapper.getROICount("Rectangle") == 2
        assert wrapper.getROICount("Rectangle", None) == 2
        assert wrapper.getROICount("Rectangle", 1) == 0
        assert wrapper.getROICount("Rectangle", True) == 0

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

            def __init__(self, fix, z, t):
                self.fix = fix
                self.z = z
                self.t = t
                self.shape = omero.model.RectangleI()
                self.shape.setTheZ(omero.rtypes.rint(z))
                self.shape.setTheT(omero.rtypes.rint(t))
                self.obj = omero.model.RoiI()
                self.obj.setImage(img)
                self.obj.addShape(self.shape)
                self.obj = fix.save(self.obj)
                self.id = self.obj.id.val

        r1 = Roi(fix1, 0, 0)
        r2 = Roi(fix1, 1, 1)
        r3 = Roi(fix2, 0, 0)
        r4 = Roi(fix2, 1, 1)

        def assertRois(fix, userid, z, t,
                       byimage, byrois, byplane):

            roiOptions = omero.api.RoiOptions()
            if userid is not None:
                roiOptions.userId = omero.rtypes.rlong(userid)

            svc = fix.roi

            r = svc.findByImage(iid, roiOptions)
            assert byimage == len(r.rois)

            count = 0
            for r in (r1, r2, r3, r4):
                r = svc.findByRoi(r.id, roiOptions)
                count += len(r.rois)
            assert byrois == count

            r = svc.findByPlane(iid, z, t, roiOptions)
            assert byplane == len(r.rois)

        for x, y in ((fix1, fix1), (fix1, fix2), (fix2, fix1), (fix2, fix2)):
            assertRois(x, y.userid, 0, 0,   2, 2, 1)
            assertRois(x, y.userid, 0, 1,   2, 2, 0)  # DNE

        for x in (fix1, fix2):
            assertRois(x, None,     0, 0,   4, 4, 2)
            assertRois(x, None,     0, 1,   4, 4, 0)  # DNE

    @pytest.mark.parametrize("color", [
        (255, 0, 0, 255, -16776961),     # Red
        (0, 255, 0, 255, 16711935),      # Green
        (0, 0, 255, 255, 65535),         # Blue
        (0, 255, 255, 255, 16777215),    # Cyan
        (255, 0, 255, 255, -16711681),   # Magenta
        (255, 255, 0, 255, -65281),      # Yellow
        (0, 0, 0, 255, 255),             # Black
        (255, 255, 255, 255, -1),        # White
        (0, 0, 0, 127, 127),             # Transparent black
        (127, 127, 127, 127, 2139062143)])  # Grey
    def testShapeColors(self, color):
        """Test create an ROI with various shapes & colors set."""

        color_holder = ColorHolder()
        color_holder.setRed(color[0])
        color_holder.setGreen(color[1])
        color_holder.setBlue(color[2])
        color_holder.setAlpha(color[3])
        colorInt = color_holder.getInt()
        assert colorInt == color[4]

        img = self.new_image("testCreateRois")
        img = self.update.saveAndReturnObject(img)

        roi = omero.model.RoiI()
        roi.setImage(img)

        rect = omero.model.RectangleI()
        rect.x = rdouble(5)
        rect.y = rdouble(5)
        rect.width = rdouble(100)
        rect.height = rdouble(100)
        rect.fillColor = rint(colorInt)
        rect.strokeColor = rint(colorInt)
        rect.theZ = rint(0)
        rect.theT = rint(0)
        rect.textValue = rstring("test-Rectangle")
        roi.addShape(rect)

        ellipse = omero.model.EllipseI()
        ellipse.x = rdouble(50.0)
        ellipse.y = rdouble(35.5)
        ellipse.radiusX = rdouble(2000)
        ellipse.radiusY = rdouble(300.04)
        ellipse.fillColor = rint(colorInt)
        ellipse.strokeColor = rint(colorInt)
        ellipse.theZ = rint(1)
        ellipse.theT = rint(2)
        ellipse.textValue = rstring("test-Ellipse")
        roi.addShape(ellipse)

        line = omero.model.LineI()
        line.x1 = rdouble(0)
        line.x2 = rdouble(500.9)
        line.y1 = rdouble(-100)
        line.y2 = rdouble(201.0)
        line.theZ = rint(-1)
        line.theT = rint(1)
        line.strokeColor = rint(colorInt)
        line.fillColor = rint(colorInt)
        line.textValue = rstring("test-Line")
        roi.addShape(line)

        point = omero.model.PointI()
        point.x = rdouble(1000)
        point.y = rdouble(0)
        point.strokeColor = rint(colorInt)
        point.fillColor = rint(colorInt)
        point.textValue = rstring("test-Point")
        roi.addShape(point)

        new_roi = self.update.saveAndReturnObject(roi)

        roi_service = self.client.sf.getRoiService()
        result = roi_service.findByImage(img.id.val, None)
        assert result is not None
        shapeCount = 0
        for roi in result.rois:
            assert roi.id.val == new_roi.id.val
            for s in roi.copyShapes():
                assert s.getFillColor().val == colorInt
                assert s.getStrokeColor().val == colorInt
                shapeCount += 1
        # Check we found 4 shapes
        assert shapeCount == 4

    def testGetShapeStatsRestricted(self):
        """Test ROI intensity stats."""
        img = self.create_test_image(100, 100, 1, 1, 1, self.client.sf)
        pixid1 = img.getPrimaryPixels().getId().getValue()
        print img.id.val, pixid1

        roi = omero.model.RoiI()
        roi.setImage(img)

        rect = omero.model.RectangleI()
        rect.x = rdouble(5)
        rect.y = rdouble(5)
        rect.width = rdouble(50)
        rect.height = rdouble(50)
        rect.theZ = rint(0)
        rect.theT = rint(0)
        rect.textValue = rstring("test-Rectangle")
        roi.addShape(rect)

        ellipse = omero.model.EllipseI()
        ellipse.x = rdouble(50.0)
        ellipse.y = rdouble(35.5)
        ellipse.radiusX = rdouble(10)
        ellipse.radiusY = rdouble(10)
        ellipse.textValue = rstring("test-Ellipse")
        roi.addShape(ellipse)

        new_roi = self.update.saveAndReturnObject(roi)

        roi_service = self.client.sf.getRoiService()
        result = roi_service.findByImage(img.id.val, None)
        assert result is not None
        shape_ids = []
        for roi in result.rois:
            assert roi.id.val == new_roi.id.val
            for s in roi.copyShapes():
                shape_ids.append(s.id.val)

        print "shape_ids", shape_ids

        stats = roi_service.getShapeStatsRestricted(shape_ids, 0, 0, [0])
        print stats
        assert len(stats) == len(shape_ids)
        for s in stats:
            assert s.min[0] < s.mean[0]
            assert s.mean[0] < s.max[0]
            assert s.max[0] < s.sum[0]
