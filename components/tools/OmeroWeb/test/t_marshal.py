#!/usr/bin/env python
# -*- coding: utf-8 -*-

#
# Copyright (C) 2012 University of Dundee & Open Microscopy Environment.
# All rights reserved.
#
# This program is free software: you can redistribute it and/or modify
# it under the terms of the GNU Affero General Public License as
# published by the Free Software Foundation, either version 3 of the
# License, or (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU Affero General Public License for more details.
#
# You should have received a copy of the GNU Affero General Public License
# along with this program.  If not, see <http://www.gnu.org/licenses/>.
#
import unittest
import omero
import omero.clients
from omero.rtypes import rlong, rstring
from omeroweb.webgateway.marshal import shapeMarshal


class ShapeMarshalTest(unittest.TestCase):
    """
    Tests to ensure that OME-XML model and OMERO.insight shape point
    parsing are supported correctly.
    """

    DEFAULT_ID = 1L

    def testOmeXmlPolyLineMarshal(self):
        shape = omero.model.PolylineI()
        shape.id = rlong(self.DEFAULT_ID)
        shape.points = rstring('1,2 2,3 4,5')
        marshaled = shapeMarshal(shape)
        self.assertEqual('PolyLine', marshaled['type'])
        self.assertEqual(self.DEFAULT_ID, marshaled['id'])
        self.assertEquals('M 1 2 L 2 3 L 4 5' , marshaled['points'])

    def testOmeXmlPolyLineFloatMarshal(self):
        shape = omero.model.PolylineI()
        shape.id = rlong(self.DEFAULT_ID)
        shape.points = rstring('1.5,2.5 2,3 4.1,5.1')
        marshaled = shapeMarshal(shape)
        self.assertEqual('PolyLine', marshaled['type'])
        self.assertEqual(self.DEFAULT_ID, marshaled['id'])
        self.assertEquals('M 1.5 2.5 L 2 3 L 4.1 5.1' , marshaled['points'])

    def testOmeXmlPolygonMarshal(self):
        shape = omero.model.PolygonI()
        shape.id = rlong(self.DEFAULT_ID)
        shape.points = rstring('1,2 2,3 4,5')
        marshaled = shapeMarshal(shape)
        self.assertEqual('Polygon', marshaled['type'])
        self.assertEqual(self.DEFAULT_ID, marshaled['id'])
        self.assertEquals('M 1 2 L 2 3 L 4 5 z' , marshaled['points'])

    def testInsightPolyLineMarshal(self):
        shape = omero.model.PolylineI()
        shape.id = rlong(self.DEFAULT_ID)
        shape.points = rstring('points[1,2 2,3 4,5] points1[1,2 2,3 4,5] points2[1,2 2,3 4,5] mask[0,0,0]')
        marshaled = shapeMarshal(shape)
        self.assertEqual('PolyLine', marshaled['type'])
        self.assertEqual(self.DEFAULT_ID, marshaled['id'])
        self.assertEquals('M 1 2 L 2 3 L 4 5' , marshaled['points'])

    def testInsightPolyLineFloatMarshal(self):
        shape = omero.model.PolylineI()
        shape.id = rlong(self.DEFAULT_ID)
        shape.points = rstring('points[1.5,2.5 2,3 4.1,5.1] points1[1.5,2.5 2,3 4.1,5.1] points2[1.5,2.5 2,3 4.1,5.1] mask[0,0,0]')
        marshaled = shapeMarshal(shape)
        self.assertEqual('PolyLine', marshaled['type'])
        self.assertEqual(self.DEFAULT_ID, marshaled['id'])
        self.assertEquals('M 1.5 2.5 L 2 3 L 4.1 5.1' , marshaled['points'])

    def testInsightPolygonMarshal(self):
        shape = omero.model.PolygonI()
        shape.id = rlong(self.DEFAULT_ID)
        shape.points = rstring('points[1,2 2,3 4,5] points1[1,2 2,3 4,5] points2[1,2 2,3 4,5] mask[0,0,0]')
        marshaled = shapeMarshal(shape)
        self.assertEqual('Polygon', marshaled['type'])
        self.assertEqual(self.DEFAULT_ID, marshaled['id'])
        self.assertEquals('M 1 2 L 2 3 L 4 5 z' , marshaled['points'])

if __name__ == '__main__':
    unittest.main()

