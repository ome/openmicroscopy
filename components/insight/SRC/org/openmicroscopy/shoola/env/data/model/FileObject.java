/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2015 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.env.data.model;


import ij.ImagePlus;
import ij.io.FileInfo;

import java.io.File;

import org.apache.commons.io.FileUtils;



//Java imports
import omero.IllegalArgumentException;

/**
 * Object hosting the information about the "file" to import.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @since 5.0
 */
public class FileObject
{

    /** The file to import.
     * This could be a file on disk or an ImageJ object for example.
     */
    private Object file;

    /**
     * Creates a new instance.
     *
     * @param file The file to import.
     */
    public FileObject(Object file)
    {
        if (file == null)
            throw new IllegalArgumentException("No object to import");
        this.file = file;
    }

    /**
     * Returns the object to import.
     *
     * @return See above
     */
    public Object getFile() { return file; }

    /**
     * Returns the name of the object to import.
     *
     * @return See above.
     */
    public String getName()
    {
        if (file instanceof File) {
            return ((File) file).getName();
        } else if (file instanceof ImagePlus) {
            ImagePlus img = (ImagePlus) file;
            FileInfo info = img.getOriginalFileInfo();
            if (info != null) return info.fileName;
        }
        return null;
    }

    /**
     * Returns the absolute path to the file.
     *
     * @return See above.
     */
    public String getAbsolutePath()
    {
        if (file instanceof File) {
            return ((File) file).getName();
        } else if (file instanceof ImagePlus) {
            ImagePlus img = (ImagePlus) file;
            
        }
        return "";
    }

    /**
     * Returns the file to import.
     *
     * @return See above.
     */
    public File getTrueFile()
    {
        if (file instanceof File) {
            return (File) file;
        } else if (file instanceof ImagePlus) {
            //to be modified.
            ImagePlus img = (ImagePlus) file;
            FileInfo info = img.getOriginalFileInfo();
            return new File(info.directory, info.fileName);
        }
        return null;
    }

    /**
     * Returns the size of the file
     * 
     * @return See above.
     */
    public long getLength()
    {
        if (file instanceof File) {
            File f = (File) file;
            if (f.isFile()) return f.length();
            return FileUtils.sizeOfDirectory(f);
        }
        return 0;
    }

    /**
     * Returns <code>true</code> if it is a directory, <code>false</code>
     * otherwise.
     *
     * @return See above.
     */
    public boolean isDirectory()
    {
        if (file instanceof File) {
            File f = (File) file;
            return f.isDirectory();
        }
        return false;
    }

    /**
     * Returns <code>true</code> if it is a file, <code>false</code>
     * otherwise.
     *
     * @return See above.
     */
    public boolean isFile()
    {
        if (file instanceof File) {
            File f = (File) file;
            return f.isFile();
        }
        return true;
    }
}
