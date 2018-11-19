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

import org.testng.Assert;
import org.testng.annotations.Test;

import omero.gateway.model.MaskData;

/**
 * Some unit tests for the MaskData class
 * 
 * @author Dominik Lindner &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:d.lindner@dundee.ac.uk">d.lindner@dundee.ac.uk</a>
 *
 */
@Test
public class MaskDataTest {

    @Test
    public void testSetGetBit() {
        int w = 10;
        int h = 15;
        byte[] data = new byte[(int)((w * h) / 8 + 1)];
        
        MaskData mask = new MaskData(); 
        mask.setMask(data);
        
        for (int i=0; i<(w*h); i++) {
           mask.setBit(data, i, 1);
           Assert.assertEquals(mask.getBit(data, i), 1, ""+i);
           
           mask.setBit(data, i, 0);
           Assert.assertEquals(mask.getBit(data, i), 0, ""+i);
        }
        
    }
    
    @Test
    public void testSetMaskAsInt() {
        MaskData mask = new MaskData();
        
        int w = 10;
        int h = 15;
        
        int[][] binMask = new int[w][h];
        int[] binMask2 = new int[w*h];
        for (int i=0; i<binMask.length; i++) {
            for (int j=0; j<binMask[i].length; j++) {
                binMask[i][j] = 0;
                binMask2[i*h+j] = 0;
            }
        }
        
        binMask[2][2] = 1;
        binMask[3][2] = 1;
        binMask[3][3] = 1;
        
        binMask2[2*h+2] = 1;
        binMask2[3*h+2] = 1;
        binMask2[3*h+3] = 1;
        
        mask.setHeight(h);
        mask.setWidth(w);
        
        mask.setMask(binMask);
        
        int[][] got = mask.getMaskAsBinaryArray();
        Assert.assertEquals(got.length, 10);
        Assert.assertEquals(got[0].length, 15);
        for (int i=0; i<binMask.length; i++) {
            for (int j=0; j<binMask[i].length; j++) {
                assertEquals(got[i][j], binMask[i][j], i+","+j);
            }
        }
        
        mask.setMask(binMask2);
        
        got = mask.getMaskAsBinaryArray();
        Assert.assertEquals(got.length, 10);
        Assert.assertEquals(got[0].length, 15);
        for (int i=0; i<binMask.length; i++) {
            for (int j=0; j<binMask[i].length; j++) {
                assertEquals(got[i][j], binMask[i][j], i+","+j);
            }
        }
    }
    
    @Test
    public void testSetMaskAsBoolean() {
        MaskData mask = new MaskData();
        
        int w = 10;
        int h = 15;
        
        boolean[][] binMask = new boolean[w][h];
        
        binMask[2][2] = true;
        binMask[3][2] = true;
        binMask[3][3] = true;
        
        mask.setHeight(h);
        mask.setWidth(w);
        mask.setMask(binMask);
        
        int[][] got = mask.getMaskAsBinaryArray();
        Assert.assertEquals(got.length, 10);
        Assert.assertEquals(got[0].length, 15);
        for (int i=0; i<binMask.length; i++) {
            for (int j=0; j<binMask[i].length; j++) {
                assertEquals(got[i][j] == 1 ? true : false, binMask[i][j], i+","+j);
            }
        }
    }
}
