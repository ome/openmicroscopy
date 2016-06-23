/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2016 University of Dundee. All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */
package lut;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import ome.model.core.OriginalFile;


/**
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @since 5.3
 */
public class LutReader {

    /** The lookup table file. */
    private OriginalFile file;

    private byte[] reds = new byte[256];
    private byte[] greens = new byte[256];
    private byte[] blues = new byte[256];

    private void interpolate(byte[] reds, byte[] greens, byte[] blues, int nColors) {
        byte[] r = new byte[nColors]; 
        byte[] g = new byte[nColors]; 
        byte[] b = new byte[nColors];
        System.arraycopy(reds, 0, r, 0, nColors);
        System.arraycopy(greens, 0, g, 0, nColors);
        System.arraycopy(blues, 0, b, 0, nColors);
        double scale = nColors/256.0;
        int i1, i2;
        double fraction;
        for (int i=0; i<256; i++) {
            i1 = (int)(i*scale);
            i2 = i1+1;
            if (i2==nColors) i2 = nColors-1;
            fraction = i*scale - i1;
            reds[i] = (byte)((1.0-fraction)*(r[i1]&255) + fraction*(r[i2]&255));
            greens[i] = (byte)((1.0-fraction)*(g[i1]&255) + fraction*(g[i2]&255));
            blues[i] = (byte)((1.0-fraction)*(b[i1]&255) + fraction*(b[i2]&255));
        }
    }

    /** Opens an NIH Image LUT or a 768 byte binary LUT. */
    private int openBinaryLut(File file, boolean raw) throws IOException {
        InputStream is = new FileInputStream(file.getAbsolutePath());
        DataInputStream f = new DataInputStream(is);
        int nColors = 256;
        if (!raw) {
            // attempt to read 32 byte NIH Image LUT header
            int id = f.readInt();
            if (id != 1229147980) { // 'ICOL'
                f.close();
                return 0;
            }
            int version = f.readShort();
            nColors = f.readShort();
            int start = f.readShort();
            int end = f.readShort();
            long fill1 = f.readLong();
            long fill2 = f.readLong();
            int filler = f.readInt();
        }
        f.read(reds, 0, nColors);
        f.read(greens, 0, nColors);
        f.read(blues, 0, nColors);
        if (nColors < 256)
            interpolate(reds, greens, blues, nColors);
        f.close();
        return 256;
    }

    /**
     * Creates a new instance.
     *
     * @param file The lookup table file
     */
    public LutReader(OriginalFile file)
    {
        this.file = file;
    }

    public void read()
        throws Exception
    {
        File f = new File(file.getPath(), file.getName());
        long length = f.length();
        int size = 0;
        if (length > 768)
            size = openBinaryLut(f, false); // attempt to read NIH Image LUT
        if (size == 0 && (length == 0 || length == 768 || length == 970))
            size = openBinaryLut(f, true); // otherwise read raw LUT
        if (size == 0 && length > 768)
            //size = openTextLut(f);
        if (size ==0 ) {
            throw new Exception("Cannot read the lookup table.");
        }
    }

    public byte getRed(int value)
    {
        return reds[value];
    }

    public byte getGreen(int value)
    {
        return greens[value];
    }

    public byte getBlue(int value)
    {
        return blues[value];
    }

    public byte[] getReds()
    {
        return reds;
    }
    

    public byte[] getGreens()
    {
        return greens;
    }
    
    public byte[] getBlues()
    {
        return blues;
    }

}
