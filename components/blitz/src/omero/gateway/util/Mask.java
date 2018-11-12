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
package omero.gateway.util;

import java.util.ArrayList;
import java.util.List;

import omero.gateway.model.MaskData;

/**
 * Provides some utility methods for dealing with mask ROIs.
 * 
 * @author Dominik Lindner &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:d.lindner@dundee.ac.uk">d.lindner@dundee.ac.uk</a>
 *
 */
public class Mask {

    private Mask() {}
    
    /**
     * Creates a mask ROI; automatically crop to its bounding box.
     * (sets X, Y, width and height accordingly)
     * 
     * @param mask
     *            The binary mask (int[width][height]) covering the whole
     *            image
     * @return The mask ROI
     */
    public static MaskData createCroppedMask(int[][] mask) {
        return createCroppedMask(intToBoolean(mask));
    }

    
    /**
     * Creates a mask ROI; automatically crop to its bounding box.
     * (sets X, Y, width and height accordingly)
     * 
     * @param mask
     *            The binary mask (boolean[width][height]) covering the whole
     *            image
     * @return The mask ROI
     */
    public static MaskData createCroppedMask(boolean[][] mask) {
        
        int minx = Integer.MAX_VALUE, miny = Integer.MAX_VALUE;
        int maxx = 0, maxy = 0;
        
        int width = mask.length;
        int height = mask[0].length;
        
        for (int y = height-1 ; y >= 0 ; y--)
        {
            for (int x = 0 ; x < width ; x++)
            {
                if (mask[x][y]) {
                    if ( x < minx )
                        minx = x;
                    if ( x > maxx)
                        maxx = x;
                    if (y < miny)
                        miny = y;
                    if (y > maxy)
                        maxy = y;
                }
            }
        }
        
        if (maxx == 0)
            return null;
        
        int neww = maxx - minx + 1;
        int newh = maxy - miny + 1;
        boolean[][] newmask = new boolean[neww][newh];
        for (int y = 0 ; y < newh ; y++)
        {
            for (int x = 0 ; x < neww ; x++)
            {
                newmask[x][y] = mask[x+minx][y+miny];
            }
        }
        
        MaskData result = new MaskData();
        result.setMask(newmask);
        result.setX(minx);
        result.setY(miny);
        result.setHeight(newh);
        result.setWidth(neww);
        return result;
    }
    
    /**
     * Creates mask ROIs from the given integer array where each 
     * single mask ROI is specified by a specific integer.
     * 
     * @param masks The masks (int[width][height]) covering the whole
     *            image.
     * @return The mask ROIs
     */
    public static List<MaskData> createCroppedMasks(int[][] masks) {
        int[][] copy = new int[masks.length][masks[0].length];
        for (int i = 0; i < copy.length; i++)
            for (int j = 0; j < copy[0].length; j++)
                copy[i][j] = masks[i][j];
        
        List<MaskData> res = new ArrayList<MaskData>();
        int target = 0;
        while ((target = getFirstNonZeroInt(copy)) > 0) {
            boolean[][] binMask = new boolean[copy.length][copy[0].length];
            for (int i = 0; i < binMask.length; i++)
                for (int j = 0; j < binMask[0].length; j++) {
                    if (copy[i][j] == target) {
                        binMask[i][j] = true;
                        copy[i][j] = 0;
                    }
                }
            MaskData m = createCroppedMask(binMask);
            res.add(m);
        }
        return res;
    }

    /**
     * Simply iterates over the array and returns the first non zero integer
     * found.
     * 
     * @param array
     *            The integer array
     * @return The first non zero integer found; zero if there is non.
     */
    private static int getFirstNonZeroInt(int[][] array) {
        for (int i = 0; i < array.length; i++) {
            for (int j = 0; j < array[0].length; j++) {
                if (array[i][j] > 0)
                    return array[i][j];
            }
        }
        return 0;
    }

    /**
     * Transforms an integer array to a boolean array,
     * where 0 == false and !0 == true
     * @param array The integer array
     * @return The boolean array
     */
    private static boolean[][] intToBoolean(int[][] array) {
        boolean[][] result = new boolean[array.length][array[0].length];
        for (int i=0; i<array.length; i++)
            for (int j=0; j<array[0].length; j++)
                result[i][j] = array[i][j] != 0;
        return result;
    }
    
}
