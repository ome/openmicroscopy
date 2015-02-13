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


//Java imports
import ij.IJ;
import ij.ImagePlus;
import ij.io.FileInfo;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import loci.formats.codec.CompressionType;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

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
     * Flag indicating if the file is generated or not.
     */
    private boolean generated;

    /** 
     * List of associated files. Mainly for imageJ.
     */
    private List<FileObject> associatedFiles;

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
     * Add the associated file if any.
     * 
     * @param file The file to add.
     */
    public void addAssociatedFile(FileObject file)
    {
        if (associatedFiles == null) {
            associatedFiles = new ArrayList<FileObject>();
        }
        if (file != null) {
            associatedFiles.add(file);
        }
    }

    /**
     * Returns the associated files if any or <code>null</code>.
     * 
     * @preturn See above.
     */
    public List<FileObject> getAssociatedFiles()
    {
        return associatedFiles;
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
            return img.getTitle();
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
            return ((File) file).getAbsolutePath();
        } else if (file instanceof ImagePlus) {
            File f = getTrueFile();
            if (f != null) return f.getAbsolutePath();
            return ((ImagePlus) file).getTitle();
        }
        return "";
    }

    /**
     * Returns the file to import.
     * @return See above.
     */
    public File getFileToImport()
    {
        File f = getTrueFile();
        if (f != null) return f;
        if (file instanceof ImagePlus) {
            //prepare command
            ImagePlus img = (ImagePlus) file;
            generated = true;
            
            try {
                //name w/o extension
                String baseName = FilenameUtils.getBaseName(
                        FilenameUtils.removeExtension(img.getTitle()));
                baseName = StringUtils.deleteWhitespace(baseName);
                String n = baseName+".ome.tif";
                f = File.createTempFile(img.getTitle(), ".ome.tif");
                File p = f.getParentFile();
                File[] list = p.listFiles();
                if (list != null) {
                    File toDelete = null;
                    for (int i = 0; i < list.length; i++) {
                        if (list[i].getName().equals(n)) {
                            toDelete = list[i];
                            break;
                        }
                    }
                    if (toDelete != null) {
                        toDelete.delete();
                    }
                }
                f = new File(p, n);
                f.deleteOnExit();
            } catch (Exception e) {
                return null;
            }
            StringBuffer buffer = new StringBuffer();
            buffer.append("outfile="+f.getAbsolutePath());
            buffer.append(" splitz=false");
            buffer.append(" splitc=false");
            buffer.append(" splitt=false");
            buffer.append(" saveroi=true");
            buffer.append(" compression="+CompressionType.UNCOMPRESSED.getCompression());
            buffer.append(" imageid="+img.getID()+" ");
            IJ.runPlugIn("loci.plugins.LociExporter", buffer.toString());
            return f;
        }
        return null;
    }

    /**
     * Returns <code>true</code> if the file has been generated,
     * <code>false</code> otherwise.
     *
     * @return See above.
     */
    public boolean isGenerated() { return generated; }

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
            if (!img.changes) {
                FileInfo info = img.getOriginalFileInfo();
                if (info != null && info.directory != null &&
                        info.fileName != null)
                    return new File(info.directory, info.fileName);
            }
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
        File f;
        if (file instanceof File) {
            f = (File) file;
            if (f.isFile()) return f.length();
            return FileUtils.sizeOfDirectory(f);
        } else if (file instanceof ImagePlus) {
            f = getTrueFile();
            if (f != null) return f.length();
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

    /**
     * Returns <code>true</code> if it is an image from ImageJ,
     * <code>false</code> otherwise.
     *
     * @return See above.
     */
    public boolean isImagePlus() { return file instanceof ImagePlus; }

    /**
     * Returns the index of the image if it is an image plus.
     *
     * @return
     */
    public int getIndex()
    {
        if (!isImagePlus()) return -1;
        ImagePlus image = (ImagePlus) file;
        Object value = image.getProperty("Series");
        if (value != null && value instanceof Integer)
            return ((Integer) value).intValue();
        return -1;
    }
}
