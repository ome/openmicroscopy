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
package omeis.providers.re.lut;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;


/**
 * Binary lookup table reader. After code from ImageJ source.
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @since 5.3
 */
class BinaryLutReader
    extends BasicLutReader
{

    private void interpolate(byte[] reds, byte[] greens, byte[] blues, int nColors) {
        byte[] r = new byte[nColors]; 
        byte[] g = new byte[nColors]; 
        byte[] b = new byte[nColors];
        System.arraycopy(reds, 0, r, 0, nColors);
        System.arraycopy(greens, 0, g, 0, nColors);
        System.arraycopy(blues, 0, b, 0, nColors);
        double scale = nColors/new Double(SIZE);
        int i1, i2;
        double fraction;
        int v = SIZE-1;
        for (int i = 0; i< SIZE; i++) {
            i1 = (int)(i*scale);
            i2 = i1+1;
            if (i2 == nColors) {
                i2 = nColors-1;
            }
            fraction = i*scale - i1;
            reds[i] = (byte)((1.0-fraction)*(r[i1]&v) + fraction*(r[i2]&v));
            greens[i] = (byte)((1.0-fraction)*(g[i1]&v) + fraction*(g[i2]&v));
            blues[i] = (byte)((1.0-fraction)*(b[i1]&v) + fraction*(b[i2]&v));
        }
    }

    /**
     * Reads the binary lut.
     */
    @Override
    int read(File file, boolean raw) throws Exception {
        InputStream is = new FileInputStream(file.getAbsolutePath());
        DataInputStream f = new DataInputStream(is);
        int nColors = SIZE;
        if (!raw) {
            // attempt to read 32 byte NIH Image LUT header
            int id = f.readInt();
            if (id != 1229147980) { // 'ICOL'
                f.close();
                return 0;
            }
            f.readShort(); //version
            nColors = f.readShort();
            f.readShort(); //start
            f.readShort(); //end
            f.readLong(); //fill 1
            f.readLong(); //fill 2
            f.readInt(); // filler
        }
        f.read(reds, 0, nColors);
        f.read(greens, 0, nColors);
        f.read(blues, 0, nColors);
        if (nColors < SIZE) {
            interpolate(reds, greens, blues, nColors);
        }
        f.close();
        return SIZE;
    }

}
