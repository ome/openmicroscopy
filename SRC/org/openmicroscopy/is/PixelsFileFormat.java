/*
 * org.openmicroscopy.is.PixelsFileFormat
 *
 *------------------------------------------------------------------------------
 *
 *  Copyright (C) 2003 Open Microscopy Environment
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
 * Written by:    Douglas Creager <dcreager@alum.mit.edu>
 *
 *------------------------------------------------------------------------------
 */




package org.openmicroscopy.is;

public class PixelsFileFormat
{
    /**
     * The size of the pixel file's X dimension.
     */
    protected int sizeX;

    /**
     * The size of the pixel file's Y dimension.
     */
    protected int sizeY;

    /**
     * The size of the pixel file's Z dimension.
     */
    protected int sizeZ;

    /**
     * The size of the pixel file's C dimension.
     */
    protected int sizeC;

    /**
     * The size of the pixel file's T dimension.
     */
    protected int sizeT;

    /**
     * The number of bytes used to store a single pixel.
     */
    protected int bytesPerPixel;

    /**
     * Whether the value of each pixel is signed or not.
     */
    protected boolean isSigned;

    /**
     * Whether the value of each pixel is a float or an integer.
     */
    protected boolean isFloat;

    /**
     * Creates a new, empty <code>PixelsFileFormat</code> object.  It
     * will not be very useful until its fields are filled in with the
     * <code>set*</code> methods.
     *
     * @return a new, empty <code>PixelsFileFormat</code> object
     */
    public PixelsFileFormat()
    {
        super();
        sizeX = 0;
        sizeY = 0;
        sizeZ = 0;
        sizeC = 0;
        sizeT = 0;
        bytesPerPixel = 0;
        isSigned = false;
        isFloat = false;
    }

    /**
     * Creates a new <code>PixelsFileFormat</code> object with the
     * values provided.
     *
     * @param sizeX the size (in pixels) of the image's X dimension
     * @param sizeY the size (in pixels) of the image's Y dimension
     * @param sizeZ the size (in pixels) of the image's Z dimension
     * @param sizeC the size (in pixels) of the image's C dimension
     * @param sizeT the size (in pixels) of the image's T dimension
     * @param bytesPerPixel the number of bytes used to store a single
     * pixel
     * @param isSigned whether the value of each pixel is signed or
     * not
     * @param isFloat whether the value of each pixel is a float or an
     * integer
     * @return a new <code>PixelsFileFormat</code> object
     */
    public PixelsFileFormat(int sizeX,
                            int sizeY,
                            int sizeZ,
                            int sizeC,
                            int sizeT,
                            int bytesPerPixel,
                            boolean isSigned,
                            boolean isFloat)
    {
        super();
        this.sizeX = sizeX;
        this.sizeY = sizeY;
        this.sizeZ = sizeZ;
        this.sizeC = sizeC;
        this.sizeT = sizeT;
        this.bytesPerPixel = bytesPerPixel;
        this.isSigned = isSigned;
        this.isFloat = isFloat;
    }

    /**
     * Returns a {@link String} representation of this object.
     */
    public String toString()
    {
        String result =
            "("+sizeX+","+sizeY+","+sizeZ+","+sizeC+","+sizeT+") ";

        result += isSigned? "signed ": "unsigned ";
        result += isFloat? "float ": "int ";
        result += Integer.toString(bytesPerPixel*8);

        return result;
    }

    /**
     * Returns the size of the image's X dimension
     * @return the size of the image's X dimension
     */
    public int getSizeX() { return sizeX; }

    /**
     * Sets the size of the image's X dimension
     * @param sizeX the size of the image's X dimension
     */
    public void setSizeX(int sizeX) { this.sizeX = sizeX; }

    /**
     * Returns the size of the image's Y dimension
     * @return the size of the image's Y dimension
     */
    public int getSizeY() { return sizeY; }

    /**
     * Sets the size of the image's Y dimension
     * @param sizeY the size of the image's Y dimension
     */
    public void setSizeY(int sizeY) { this.sizeY = sizeY; }

    /**
     * Returns the size of the image's Z dimension
     * @return the size of the image's Z dimension
     */
    public int getSizeZ() { return sizeZ; }

    /**
     * Sets the size of the image's Z dimension
     * @param sizeZ the size of the image's Z dimension
     */
    public void setSizeZ(int sizeZ) { this.sizeZ = sizeZ; }

    /**
     * Returns the size of the image's C dimension
     * @return the size of the image's C dimension
     */
    public int getSizeC() { return sizeC; }

    /**
     * Sets the size of the image's C dimension
     * @param sizeC the size of the image's C dimension
     */
    public void setSizeC(int sizeC) { this.sizeC = sizeC; }

    /**
     * Returns the size of the image's T dimension
     * @return the size of the image's T dimension
     */
    public int getSizeT() { return sizeT; }

    /**
     * Sets the size of the image's T dimension
     * @param sizeT the size of the image's T dimension
     */
    public void setSizeT(int sizeT) { this.sizeT = sizeT; }

    /**
     * Returns the number of bytes used to store a single pixel
     * @return the number of bytes used to store a single pixel
     */
    public int getBytesPerPixel() { return bytesPerPixel; }

    /**
     * Sets the number of bytes used to store a single pixel
     * @param bytesPerPixel the number of bytes used to store a single
     * pixel
     */
    public void setBytesPerPixel(int bytesPerPixel)
    { this.bytesPerPixel = bytesPerPixel; }

    /**
     * Returns whether the value of each pixel is signed or not
     * @return whether the value of each pixel is signed or not
     */
    public boolean isSigned() { return isSigned; }

    /**
     * Sets whether the value of each pixel is signed or not
     * @param isSigned whether the value of each pixel is signed or
     * not
     */
    public void setIsSigned(boolean isSigned) 
    { this.isSigned = isSigned; }

    /**
     * Returns whether the value of each pixel is a float or an
     * integer
     * @return whether the value of each pixel is a float or an
     * integer
     */
    public boolean isFloat() { return isFloat; }

    /**
     * Sets whether the value of each pixel is a float or an integer
     * @param isFloat whether the value of each pixel is a float or an
     * integer
     */
    public void setIsFloat(boolean isFloat)
    { this.isFloat = isFloat; }

}