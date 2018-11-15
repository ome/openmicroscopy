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

import static org.testng.Assert.assertEquals;

import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

import omero.gateway.model.MaskData;
import omero.gateway.util.Mask;

/**
 * Unit tests for the Mask utility class
 * 
 * @author Dominik Lindner &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:d.lindner@dundee.ac.uk">d.lindner@dundee.ac.uk</a>
 *
 */
@Test
public class MaskTest {
    
    @Test
    public void testCreateCroppedMask() {
         // Test the following scenario:
         //
         // Mask:
         // 0000000000
         // 0000000000
         // 0000000000
         // 0000000000
         // 0000000000
         // 0000000000
         // 0000000000
         // 0000000000
         // 0000000000
         // 0000100000
         // 0000100000
         // 0001100000
         // 0000100000
         // 0000000000
         // 0000000000
         //
         // Cropped mask:
         // 01
         // 01
         // 11
         // 01
         //
         // x = 3
         // y = 2 
         // width = 2
         // height = 4
        
        int w = 10;
        int h = 15;
        
        boolean[][] binMask = new boolean[w][h];
        
        binMask[3][3] = true;
        binMask[4][3] = true;
        binMask[4][4] = true;
        binMask[4][5] = true;
        binMask[4][2] = true;
        
        MaskData mask = Mask.createCroppedMask(binMask);
        
        int[][] got = mask.getMaskAsBinaryArray();
        
        Assert.assertEquals((int)mask.getX(), 3);
        Assert.assertEquals((int)mask.getY(), 2);
        Assert.assertEquals((int)mask.getWidth(), 2);
        Assert.assertEquals((int)mask.getHeight(), 4);

        for (int i=0; i<got.length; i++) {
            for (int j=0; j<got[i].length; j++) {
                assertEquals(got[i][j] == 1 ? true : false, binMask[(int)(i+mask.getX())][(int)(j+mask.getY())], i+","+j);
            }
        }
    }
    
    @Test
    public void testCreateCroppedMasks() {
         // Test the following scenario:
         //
         // Mask:
         // 0000000000
         // 0000000000
         // 0000000000
         // 0000000000
         // 0000000000
         // 0000000000
         // 0000000000
         // 0022200000
         // 0000000000
         // 0000100000
         // 0000100000
         // 0001100000
         // 0000100000
         // 0000000000
         // 0000000000
         //
         // Cropped masks:
         // 01
         // 01
         // 11
         // 01
         //
         // x = 3
         // y = 2 
         // width = 2
         // height = 4
         //
         // 111
         // x = 2
         // y = 7 
         // width = 3
         // height = 1
        
        
        int w = 10;
        int h = 15;
        
        int[][] binMask = new int[w][h];
        
        binMask[3][3] = 1;
        binMask[4][3] = 1;
        binMask[4][4] = 1;
        binMask[4][5] = 1;
        binMask[4][2] = 1;
        
        binMask[2][7] = 2;
        binMask[3][7] = 2;
        binMask[4][7] = 2;
        
        List<MaskData> masks = Mask.createCroppedMasks(binMask);
        Assert.assertEquals(masks.size(), 2);
        
        for (MaskData mask : masks) {
            boolean found = (mask.getX() == 3 && mask.getY() == 2 && mask.getWidth() == 2 && mask.getHeight() == 4) ||
                    (mask.getX() == 2 && mask.getY() == 7 && mask.getWidth() == 3 && mask.getHeight() == 1);
            Assert.assertTrue(found);
        }
    }
    
}
