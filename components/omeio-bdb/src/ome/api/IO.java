/*
 * ome.api.IO
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

package ome.api;

// Java imports

// Third-party libraries

// Application-internal dependencies

/**
 * Provides methods for dealing with large CursorCallback, this includes files, pixels, and thumbnails.
 *
 * @author <br>
 *         Josh Moore &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:josh.moore@gmx.de"> josh.moore@gmx.de</a>
 * @version 1.0 <small> (<b>Internal version:</b> $Revision: $ $Date: $)
 *          </small>
 * @since OME3.0
 */
public interface IO {

    //
    public boolean planeExists(long id, int z, int c, int t);
    public byte[] getPlane(long id, int z, int c, int t);
    public byte[] getPlane(long id, int z, int c, int t, int offset, int length);
    public void putPlane(byte[] plane, long id, int z, int c, int t);
    public void putPlane(byte[] plane, long id, int z, int c, int t, int offset, int length);
    public void deletePlane(long id, int z, int c, int t);
    //
    public boolean pixelsExist(long id);
    public byte[][][][] getPixels(long id);
    public void putPixels(byte[][][][] pixels, long id);
    public void deletePixels(long id);
    //
    /*
	public boolean fileExists(long id);
    public boolean pixelsExist(long id);
    public int newPixels();
    */
    
}
