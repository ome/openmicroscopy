#!/usr/bin/env python
# -*- coding: utf-8 -*-

#
# Copyright (C) 2014 Glencoe Software, Inc. All Rights Reserved.
# Use is subject to license terms supplied in LICENSE.txt
#
# This program is free software; you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation; either version 2 of the License, or
# (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License along
# with this program; if not, write to the Free Software Foundation, Inc.,
# 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.

"""
Basic tests for additions/changes to the 5.1 model.
"""

import library as lib
import pytest
import omero
import omero.model

from omero.model import NamedValue as NV
from omero.rtypes import unwrap


class TestModel51(lib.ITest):

    def testExposureTime(self):
        img = self.importMIF(name="testExposureTime", exposureTime=1.2)[0]
        plane_info = self.query.findByQuery((
            "select pi from PlaneInfo pi "
            "join fetch pi.exposureTime "
            "join pi.pixels as pix join pix.image as img "
            "where img.id = :id"), omero.sys.ParametersI().addId(img.id.val))
        exposure = plane_info.getExposureTime()
        unit = exposure.getUnit()
        assert omero.model.enums.UnitsTime.SECOND == unit

        micros = omero.model.enums.UnitsTime.MICROSECOND

        exposure.setUnit(micros)
        plane_info = self.update.saveAndReturnObject(plane_info)
        exposure = plane_info.getExposureTime()
        unit = exposure.getUnit()
        assert omero.model.enums.UnitsTime.MICROSECOND == unit

    def testPhysicalSize(self):
        img = self.importMIF(name="testPhysicalSize", physicalSizeZ=2.0)[0]
        pixels = self.query.findByQuery((
            "select pix from Pixels pix "
            "join fetch pix.physicalSizeZ "
            "where pix.image.id = :id"),
            omero.sys.ParametersI().addId(img.id.val))
        sizeZ = pixels.getPhysicalSizeZ()
        unit = sizeZ.getUnit()
        assert omero.model.enums.UnitsLength.MICROMETER == unit

        mm = omero.model.enums.UnitsLength.MILLIMETER

        sizeZ.setUnit(mm)
        pixels = self.update.saveAndReturnObject(pixels)
        sizeZ = pixels.getPhysicalSizeZ()
        unit = sizeZ.getUnit()
        assert omero.model.enums.UnitsLength.MILLIMETER == unit

    UL = omero.model.enums.UnitsLength
    try:
        UL = sorted(UL._enumerators.values())
    except:
        # TODO: this occurs on Ice 3.4 and can be removed
        # once it has been dropped.
        UL = [getattr(UL, x) for x in sorted(UL._names)]

    @pytest.mark.parametrize("ul", UL, ids=[str(x) for x in UL])
    def testAllLengths(self, ul):
        one = omero.model.LengthI()
        one.setValue(1.0)
        one.setUnit(ul)
        roi = omero.model.RoiI()
        line = omero.model.LineI()
        line.setStrokeWidth(one)
        roi.addShape(line)
        roi = self.update.saveAndReturnObject(roi)
        line = roi.copyShapes()[0]
        stroke = line.getStrokeWidth()
        assert ul == stroke.getUnit()

    def testAsMapMethod(self):
        g = omero.model.ExperimenterGroupI()
        g.setConfig(
            [NV("foo", "bar")]
        )
        m = g.getConfigAsMap()
        assert m["foo"] == "bar"

    def assertMapAnnotation(self, anns, mid):
        m = None
        for a in anns:
            if isinstance(a, omero.model.MapAnnotationI):
                if a.id.val == mid:
                    m = a
        assert m
        assert "foo" == m.getMapValue()[0].name
        assert "bar" == m.getMapValue()[0].value

    def testMapEagerFetch(self):
        m = omero.model.MapAnnotationI()
        m.setMapValue(
            [NV("foo", "bar")]
        )
        m = self.update.saveAndReturnObject(m)
        anns = self.query.findAllByQuery(
            "select m from MapAnnotation m ",
            None)
        self.assertMapAnnotation(anns, m.id.val)

        # Add a second annotation and query both
        c = omero.model.CommentAnnotationI()
        c = self.update.saveAndReturnObject(c)
        anns = self.query.findAllByQuery(
            "select m from Annotation m ",
            None)
        self.assertMapAnnotation(anns, m.id.val)

        # Now place both on an image and retry
        i = omero.model.ImageI()
        i.setName(omero.rtypes.rstring("testMapEagerFetch"))
        i.linkAnnotation(m)
        i.linkAnnotation(c)
        i = self.update.saveAndReturnObject(i)
        imgs = self.query.findByQuery(
            ("select i from Image i join fetch "
             "i.annotationLinks l join fetch l.child "
             "where i.id = :id"),
            omero.sys.ParametersI().addId(i.id.val))
        anns = imgs.linkedAnnotationList()
        self.assertMapAnnotation(anns, m.id.val)

        # And now load via IMetadata
        meta = self.client.sf.getMetadataService()
        anns = meta.loadAnnotations(
            "omero.model.Image",
            [i.id.val],
            [],  # Supported Annotation types
            [],  # Annotator IDs
            None)
        self.assertMapAnnotation(anns[i.id.val], m.id.val)

    def testMapSecurity(self):
        c1 = self.new_client(perms="rwr---")
        u1 = c1.sf.getUpdateService()
        a1 = c1.sf.getAdminService()
        g1 = a1.getEventContext().groupName
        m1 = omero.model.MapAnnotationI()
        m1.setMapValue(
            [NV("foo", "bar")]
        )
        m1 = u1.saveAndReturnObject(m1)

        # Now create another user and try to edit
        c2 = self.new_client(group=g1)
        q2 = c2.sf.getQueryService()
        u2 = c2.sf.getUpdateService()
        m2 = q2.get("MapAnnotation", m1.id.val)
        assert not m2.details.permissions.canEdit()

        # Additions fail
        m2.getMapValue().append(
            NV("edited-by", str(c2)))
        with pytest.raises(omero.SecurityViolation):
            u2.saveAndReturnObject(m2)

        # Removals fail
        m2.setMapValue([])
        with pytest.raises(omero.SecurityViolation):
            u2.saveAndReturnObject(m2)

        # Also via a None
        m2.setMapValue(None)
        with pytest.raises(omero.SecurityViolation):
            u2.saveAndReturnObject(m2)

        # Alterations fail
        m2.setMapValue(
            [NV("foo", "WRONG")])
        with pytest.raises(omero.SecurityViolation):
            u2.saveAndReturnObject(m2)

    def testUnitProjections(self):
        img = self.importMIF(name="testUnitProjections", exposureTime=1.2)[0]

        as_map = self.query.projection((
            "select pi.exposureTime from PlaneInfo pi "
            "join pi.pixels as pix join pix.image as img "
            "where img.id = :id"),
            omero.sys.ParametersI().addId(img.id.val))[0][0]
        as_map = unwrap(as_map)
        pytest.assertAlmostEqual(1.2, as_map.get("value"))
        assert "SECOND" == as_map.get("unit")
        assert "s" == as_map.get("symbol")

        as_objs = self.query.projection((
            "select pi.exposureTime.value, "
            "pi.exposureTime.unit, "
            "cast(pi.exposureTime.unit as text) from PlaneInfo pi "
            "join pi.pixels as pix join pix.image as img "
            "where img.id = :id"),
            omero.sys.ParametersI().addId(img.id.val))[0]
        as_objs = unwrap(as_objs)
        pytest.assertAlmostEqual(1.2, as_objs[0])
        assert "SECOND" == as_objs[1]
        assert "s" == as_objs[2]
