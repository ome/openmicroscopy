/*
 * ome.io.nio.AbstractFileSystemService
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
package ome.io.nio;

import java.io.File;
import java.util.Formatter;


/**
 * @author callan
 *
 */
public class AbstractFileSystemService
{

    public final static String ROOT_DEFAULT = File.separator + "OME" + File.separator + "OMEIS" + File.separator;
    
    public final static String PIXELS_PATH =  "Pixels" + File.separator;
    
    public final static String FILES_PATH = "Files" + File.separator;
    
    private final String root;

    public AbstractFileSystemService(String path)
    {
        this.root = path;
        
        File rootDirectory = new File(this.root);
        if (
                ! rootDirectory.isDirectory() ||
                ! rootDirectory.canRead() || 
                ! rootDirectory.canWrite()
            )
            throw new IllegalArgumentException("Invalid directory specified for file system service.");
    }
    
    public String getPixelsPath(Long id)
    {
        return getPath(PIXELS_PATH, id);
    }
    
    public String getFilesPath(Long id)
    {
        return getPath(FILES_PATH, id);
    }
    
    private String getPath(String prefix, Long id)
    {
        String suffix = "";
        Long remaining = id;
        Long dirno = 0L;
        
        if (id == null)
            throw new NullPointerException("Expecting a not-null id.");

        while (remaining > 999)
        {
            System.out.println("Remaining: " + remaining);
            remaining /= 1000;
            
            if (remaining > 0)
            {
                Formatter formatter = new Formatter();
                dirno = remaining % 1000;
                suffix = formatter.format("Dir-%03d/%s", dirno, suffix)
                                  .out().toString();
                System.out.println("Suffix: " + suffix);
            }
        }
        
        return root + prefix + suffix + id;
    }
}
