/*
 * org.openmicroscopy.is.FileInfo
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

public class FileInfo
{
    /**
     * The original filename of the uploaded file.
     */
    protected String originalName;

    /**
     * The length of the file in bytes.
     */
    protected long length;

    /**
     * Creates a new, empty <code>PixelsFileFormat</code> object.  It
     * will not be very useful until its fields are filled in with the
     * <code>set*</code> methods.
     *
     * @return a new, empty <code>PixelsFileFormat</code> object
     */
    public FileInfo()
    {
        super();
        this.originalName = null;
        this.length = -1;
    }

    /**
     * Creates a new <code>PixelsFileFormat</code> object with the
     * values provided.
     *
     * @param originalName the original filename of the uploaded file
     * @param length the length of the file in bytes
     * @return a new <code>PixelsFileFormat</code> object
     */
    public FileInfo(String originalName, long length)
    {
        super();
        this.originalName = originalName;
        this.length = length;
    }

    /**
     * Returns a {@link String} representation of this object.
     */
    public String toString()
    {
        return originalName+" ("+length+")";
    }

    /**
     * Returns the original filename of the uploaded file.
     * @return the original filename of the uploaded file.
     */
    public String getOriginalName() { return originalName; }

    /**
     * Sets the original filename of the uploaded file.
     * @param name the original filename of the uploaded file.
     */
    public void setOriginalName(String originalName)
    { this.originalName = originalName; }

    /**
     * Returns the length of the file in bytes.
     * @return the length of the file in bytes.
     */
    public long getLength() { return length; }

    /**
     * Sets the length of the file in bytes.
     * @param length the length of the file in bytes.
     */
    public void setLength(long length) { this.length = length; }
}