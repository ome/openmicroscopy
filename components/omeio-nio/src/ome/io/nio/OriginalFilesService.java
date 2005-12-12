/*
 * ome.io.nio.OriginalFilesService
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

import java.io.FileNotFoundException;

import ome.model.core.OriginalFile;


/**
 * @author callan
 *
 */
public class OriginalFilesService
{
    private static OriginalFilesService soleInstance;
    
    public static OriginalFilesService getInstance()
    {
        if (soleInstance == null)
            soleInstance = new OriginalFilesService();
        
        return soleInstance;
    }
    
    public FileBuffer createFileBuffer(OriginalFile file)
        throws FileNotFoundException
    {
        file.setId(Helper.getNextFilesId());
        return new FileBuffer(file, "rw");
    }

    public FileBuffer getReadOnlyFileBuffer(OriginalFile file)
        throws FileNotFoundException
    {
        return new FileBuffer(file, "r");
    }
    
    public FileBuffer getReadWriteFileBuffer(OriginalFile file)
        throws FileNotFoundException
    {
        return new FileBuffer(file, "rw");
    }
    
    
}
