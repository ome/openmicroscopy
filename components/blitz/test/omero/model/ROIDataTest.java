/*
 * Copyright (C) 2018 University of Dundee & Open Microscopy Environment.
 * All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
package omero.model;

import omero.gateway.model.ROIData;
import omero.gateway.model.RectangleData;

import org.junit.Test;
import org.testng.Assert;

public class ROIDataTest {

    public ROIDataTest() {
    }

    @Test
    public void testGetShapes() {
        ROIData roi = new ROIData();

        RectangleData allZT = new RectangleData(1, 5, 10, 10);
        allZT.setT(-1);
        allZT.setZ(-1);
        roi.addShapeData(allZT);
        System.out.println("allZT: " + allZT);

        RectangleData allZ = new RectangleData(2, 5, 10, 10);
        allZ.setT(2);
        allZ.setZ(-1);
        roi.addShapeData(allZ);
        System.out.println("allZT: " + allZT);

        RectangleData allT = new RectangleData(3, 5, 10, 10);
        allT.setT(-1);
        allT.setZ(2);
        roi.addShapeData(allT);
        System.out.println("allZT: " + allZT);

        RectangleData r = new RectangleData(4, 5, 10, 10);
        r.setT(3);
        r.setZ(3);
        roi.addShapeData(r);

        // all shapes
        Assert.assertEquals(roi.getShapeCount(), 4);

        // just allZT
        Assert.assertEquals(roi.getShapes(1, 1).size(), 1);
        Assert.assertEquals(roi.getShapes(1, 1).iterator().next(), allZT);

        // allZT and allZ at t=2
        Assert.assertEquals(roi.getShapes(1, 2).size(), 2);
        Assert.assertTrue(roi.getShapes(1, 2).contains(allZT));
        Assert.assertTrue(roi.getShapes(1, 2).contains(allZ));

        // allZT and allT at z=2
        Assert.assertEquals(roi.getShapes(2, 1).size(), 2);
        Assert.assertTrue(roi.getShapes(2, 1).contains(allZT));
        Assert.assertTrue(roi.getShapes(2, 1).contains(allT));

        // r and allZT
        Assert.assertEquals(roi.getShapes(3, 3).size(), 2);
        Assert.assertTrue(roi.getShapes(3, 3).contains(r));
        Assert.assertTrue(roi.getShapes(3, 3).contains(allZT));
    }

}
