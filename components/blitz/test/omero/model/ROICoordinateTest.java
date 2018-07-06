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

import org.junit.Test;
import org.testng.Assert;

import omero.gateway.model.ROICoordinate;

public class ROICoordinateTest {

    public ROICoordinateTest() {
    }

    @Test
    public void testComparison() {
        ROICoordinate c1 = new ROICoordinate(2, 10);
        ROICoordinate c2 = new ROICoordinate(1, 11);
        ROICoordinate c3 = new ROICoordinate(2, 11);
        ROICoordinate c4 = new ROICoordinate(2, 11);
        
        Assert.assertTrue(c1.compare(c1, c2) == -1);
        Assert.assertTrue(c1.compare(c2, c1) == 1);
        
        Assert.assertTrue(c1.compare(c2, c3) == -1);
        Assert.assertTrue(c1.compare(c3, c2) == 1);
        
        Assert.assertTrue(c1.compare(c3, c4) == 0);
        Assert.assertTrue(c1.compare(c4, c3) == 0);
        
        
        ROICoordinate c5 = new ROICoordinate(-1, -1);
        ROICoordinate c6 = new ROICoordinate(-1, -1);
        
        Assert.assertTrue(c1.compare(c5, c6) == 0);
        Assert.assertTrue(c1.compare(c6, c5) == 0);
        
        Assert.assertTrue(c1.compare(c5, c1) == -1);
        Assert.assertTrue(c1.compare(c1, c5) == 1);
    }
    
    @Test
    public void testHashEquals() {
        ROICoordinate c1 = new ROICoordinate(4, 5);
        ROICoordinate c2 = new ROICoordinate(4, 6);
        ROICoordinate c3 = new ROICoordinate(3, 5);
        ROICoordinate c4 = new ROICoordinate(3, 5);
        
        Assert.assertFalse(c1.equals(c2));
        Assert.assertFalse(c2.equals(c1));
        Assert.assertFalse(c1.hashCode() == c2.hashCode());
        
        Assert.assertFalse(c1.equals(c3));
        Assert.assertFalse(c3.equals(c1));
        Assert.assertFalse(c3.hashCode() == c1.hashCode());
        
        Assert.assertTrue(c3.equals(c4));
        Assert.assertTrue(c4.equals(c3));
        Assert.assertTrue(c3.hashCode() == c4.hashCode());
        
        ROICoordinate c5 = new ROICoordinate(-1, -1);
        ROICoordinate c6 = new ROICoordinate(-1, -1);
        
        Assert.assertTrue(c5.equals(c6));
        Assert.assertTrue(c6.equals(c5));
        Assert.assertTrue(c5.hashCode() == c6.hashCode());
        
        Assert.assertFalse(c5.equals(c4));
        Assert.assertFalse(c4.equals(c5));
        Assert.assertFalse(c5.hashCode() == c4.hashCode());
    }
    
    
}
