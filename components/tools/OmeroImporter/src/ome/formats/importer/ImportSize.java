/*
 *   $Id$
 *
 *   Copyright 2009 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.formats.importer;

import omero.model.IObject;
import omero.model.Pixels;

/**
 * Utility class which configures the Import.
 * 
 * @since Beta4.1
 */

public class ImportSize {
    
    final String fileName;
    final Pixels pixels;
    final String dimOrder;
    int sizeX, sizeY, sizeZ, sizeC, sizeT, imageCount;
    int zSize, wSize, tSize;
    public ImportSize(String fileName, Pixels pixels, String dimOrder) {
        this.fileName = fileName;
        this.pixels = pixels;
        this.dimOrder = dimOrder;
    }
    

    /**
     * Calculates and returns the number of planes in this pixels set. Also 
     * sets the offset info.
     * 
     * @param fileName filename for use in {@link #setOffsetInfo(String)}
     * @param pixels Pixels set for which to calculate the plane count.
     * @return the number of planes in this image (z * c * t)
     */
    protected void calculateImageCount(String fileName, Pixels pixels)
    {
        sizeZ = pixels.getSizeZ().getValue();
        sizeC = pixels.getSizeC().getValue();
        sizeT = pixels.getSizeT().getValue();
        sizeX = pixels.getSizeX().getValue();
        sizeY = pixels.getSizeY().getValue();
        imageCount = sizeZ * sizeC * sizeT;
        setOffsetInfo(fileName);
    }
    

    private void setOffsetInfo(String fileName)
    {
        int order = 0;
        order = getSequenceNumber(reader.getDimensionOrder());
        setOffsetInfo(order, sizeZ, sizeC, sizeT);
    }

    /**
     * This method calculates the size of a w, t, z section depending on which
     * sequence is being used (either ZTW, WZT, or ZWT)
     * 
     * @param imgSequence
     * @param numZSections
     * @param numWaves
     * @param numTimes
     */
    private void setOffsetInfo(int imgSequence, int numZSections, int numWaves,
            int numTimes)
    {
        int smallOffset = 1;
        switch (imgSequence)
        {
            // ZTW sequence
            case 0:
                zSize = smallOffset;
                tSize = zSize * numZSections;
                wSize = tSize * numTimes;
                break;
            // WZT sequence
            case 1:
                wSize = smallOffset;
                zSize = wSize * numWaves;
                tSize = zSize * numZSections;
                break;
            // ZWT sequence
            case 2:
                zSize = smallOffset;
                wSize = zSize * numZSections;
                tSize = wSize * numWaves;
                break;
            // TWZ sequence
            case 3:
                tSize = smallOffset;
                wSize = tSize * numTimes;
                zSize = wSize * numWaves;
                break;
            // WTZ sequence
            case 4:
                wSize = smallOffset;
                tSize = wSize * numWaves;
                zSize = tSize * numTimes;
                break;
            //TZW
            case 5:
                tSize = smallOffset;
                zSize = wSize * numTimes;
                wSize = tSize * numZSections;
                
        }
    }
    

    private int getSequenceNumber(String dimOrder)
    {
        if (dimOrder.equals("XYZTC")) return 0;
        if (dimOrder.equals("XYCZT")) return 1;
        if (dimOrder.equals("XYZCT")) return 2;
        if (dimOrder.equals("XYTCZ")) return 3;
        if (dimOrder.equals("XYCTZ")) return 4;
        if (dimOrder.equals("XYTZC")) return 5;
        throw new RuntimeException(dimOrder + " not represented in " +
                "getSequenceNumber");
    }

}
