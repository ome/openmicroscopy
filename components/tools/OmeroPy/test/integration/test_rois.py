#!/usr/bin/env python
# -*- coding: utf-8 -*-

"""
   Tests for the IRois interface

   Copyright 2009-2014 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""
import library as lib
import omero


class TestRois(lib.ITest):

    def teststats1(self):
        self.client.sf.getRoiService()

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
        svc.findByImage(img.id.val, None)

    def testGetROICount(self):
        """
        Test no ROI couting method
        """

        # Create group with two member and one image
        group = self.new_group(perms="rwrw--")
        owner = self.new_client(group=group)  # Owner of share
        member = self.new_client(group=group)  # Member of group
        img = self.createTestImage(session=owner.sf)

        from omero.gateway import ImageWrapper, BlitzGateway
        conn = BlitzGateway(client_obj=owner)

        # Test no ROI count
        wrapper = ImageWrapper(conn, img)
        assert wrapper.getROICount() == 0

        # Test ROI shape
        roi1 = omero.model.RoiI()
        roi1.addShape(omero.model.RectI())
        roi1.addShape(omero.model.RectI())
        roi1.setImage(img)
        roi1 = owner.sf.getUpdateService().saveAndReturnObject(roi1)

        roi2 = omero.model.RoiI()
        roi2.addShape(omero.model.RectI())
        roi2.addShape(omero.model.EllipseI())
        roi2.setImage(img)
        roi2 = owner.sf.getUpdateService().saveAndReturnObject(roi2)

        wrapper = ImageWrapper(conn, img)
        assert wrapper.getROICount() == 2
        assert wrapper.getROICount("Rect") == 2
        assert wrapper.getROICount("Ellipse") == 1
        assert wrapper.getROICount("Line") == 0
        assert wrapper.getROICount(["Rect", "Ellipse"]) == 2

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
        assert wrapper.getROICount("Rect") == 2
        assert wrapper.getROICount("Rect", None) == 2
        assert wrapper.getROICount("Rect", 1) == 2
        assert wrapper.getROICount("Rect", True) == 2

        # Member gateway
        conn = BlitzGateway(client_obj=member)
        wrapper = ImageWrapper(conn, img)
        assert wrapper.getROICount() == 3
        assert wrapper.getROICount(filterByCurrentUser=True) == 1
        assert wrapper.getROICount("Ellipse") == 2
        assert wrapper.getROICount("Ellipse", None) == 2
        assert wrapper.getROICount("Ellipse", 1) == 1
        assert wrapper.getROICount("Ellipse", True) == 1
        assert wrapper.getROICount("Rect") == 2
        assert wrapper.getROICount("Rect", None) == 2
        assert wrapper.getROICount("Rect", 1) == 0
        assert wrapper.getROICount("Rect", True) == 0

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

        def assertRois(fix, userid, ns, z, t,
                       byimage, byrois, byns, byplane):

            roiOptions = omero.api.RoiOptions()
            if userid is not None:
                roiOptions.userId = omero.rtypes.rlong(userid)
            if ns is not None:
                roiOptions.namespace = omero.rtypes.rstring(ns)

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
            assertRois(x, y.userid, None, 0, 0,   2, 2, 2, 1)
            assertRois(x, y.userid, None, 0, 1,   2, 2, 2, 0)  # DNE
            assertRois(x, y.userid, "A",  0, 0,   1, 2, 1, 1)
            assertRois(x, y.userid, "B",  0, 0,   1, 2, 1, 1)

        for x in (fix1, fix2):
            assertRois(x, None,     None,  0, 0,   4, 4, 4, 2)
            assertRois(x, None,     None,  0, 1,   4, 4, 4, 0)  # DNE
            assertRois(x, None,     "A",   0, 0,   2, 4, 2, 2)
            assertRois(x, None,     "B",   0, 0,   2, 4, 2, 2)
            assertRois(x, None,     "B",   0, 0,   2, 4, 2, 2)
