/*
 * ome.io.bdb.ByteUtils
 *
 *------------------------------------------------------------------------------
 *
 *  Copyright (C) 2005 Open Microscopy Environment
 *      Massachusetts Institute of Technology,
 *      National Institutes of Health,
 *      University of Dundee
 *
 *
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *------------------------------------------------------------------------------
 */

/*------------------------------------------------------------------------------
 *
 * Written by:    Josh Moore <josh.moore@gmx.de>
 *
 *------------------------------------------------------------------------------
 */

package ome.io.bdb;

// Java imports

// Third-party libraries

// Application-internal dependencies

/** 
 * mostly borrow from Data{Input/Output}Stream
 * @author Josh Moore, <a href="mailto:josh.moore@gmx.de">josh.moore@gmx.de</a>
 * @version 1.0 <small> (<b>Internal version:</b> $Rev$ $Date$) </small>
 * @since OMERO 3.0
 */
public class ByteUtils
{

    public static byte[] intToByteArray(final int v)
    {
        byte[] out = new byte[4];
        out[0] = (byte) ((v >>> 24) & 0xFF);
        out[1] = (byte) ((v >>> 16) & 0xFF);
        out[2] = (byte) ((v >>> 8) & 0xFF);
        out[3] = (byte) ((v >>> 0) & 0xFF);
        return out;
    }

    public static int byteArrayToInt(final byte[] v, final int offset)
    {
        int ch1 = v[0+offset] & 0xFF;
        int ch2 = v[1+offset] & 0xFF;
        int ch3 = v[2+offset] & 0xFF;
        int ch4 = v[3+offset] & 0xFF;
        return ((ch1 << 24) + (ch2 << 16) + (ch3 << 8) + (ch4 << 0));
    }

    public static long byteArrayToLong(final byte[] v, final int offset)
    {
        return (((long) v[0+offset] << 56) + ((long) (v[1+offset] & 255) << 48)
                + ((long) (v[2+offset] & 255) << 40) + ((long) (v[3+offset] & 255) << 32)
                + ((long) (v[4+offset] & 255) << 24) + ((v[5+offset] & 255) << 16)
                + ((v[6+offset] & 255) << 8) + ((v[7+offset] & 255) << 0));
    }

    public static byte[] longToByteArray(long v)
    {
        byte[] out = new byte[8];
        out[0] = (byte) (v >>> 56);
        out[1] = (byte) (v >>> 48);
        out[2] = (byte) (v >>> 40);
        out[3] = (byte) (v >>> 32);
        out[4] = (byte) (v >>> 24);
        out[5] = (byte) (v >>> 16);
        out[6] = (byte) (v >>> 8);
        out[7] = (byte) (v >>> 0);
        return out;
    }
    
    public static byte[] planeToKey(long id, int z, int c, int t)
    {
        byte[] key = new byte[20];
        System.arraycopy(ByteUtils.longToByteArray(id), 0, key, 0, 8);
        System.arraycopy(ByteUtils.intToByteArray(z), 0, key, 8, 4);
        System.arraycopy(ByteUtils.intToByteArray(c), 0, key, 12, 4);
        System.arraycopy(ByteUtils.intToByteArray(t), 0, key, 16, 4);
        return key;
    }

    public static String keyToPlane(byte[] key)
    {
        long id;
        int z, c, t;

        id = ByteUtils.byteArrayToLong(key, 0);
        z = ByteUtils.byteArrayToInt(key, 8);
        c = ByteUtils.byteArrayToInt(key, 12);
        t = ByteUtils.byteArrayToInt(key, 16);

        StringBuilder sb = new StringBuilder(24);
        sb.append("id");
        sb.append(Long.toString(id));
        sb.append("-z");
        sb.append(Integer.toString(z));
        sb.append("-c");
        sb.append(Integer.toString(c));
        sb.append("-t");
        sb.append(Integer.toString(t));
        return sb.toString();
    }

}
