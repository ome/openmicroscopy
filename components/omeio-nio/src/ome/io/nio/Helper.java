/*
 * ome.io.nio.Helper
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


/**
 * @author callan
 *
 */
public class Helper
{
    /* FIXME: This is a *hack*, it needs to be replaced with real
     * configuration option. -Chris
     */
    private static final String root = "/OME/OMEIS/";

    public static String getPixelsPath(Integer id)
    {
        return getPath("Pixels/", id);
    }
    
    public static String getFilesPath(Integer id)
    {
        return getPath("Files/", id);
    }
    
    public static Integer getNextPixelsId()
    {
        // FIXME: *HACK* This really needs to be stored in the database and
        // have its own type.
        return 100;
    }
    
    public static Integer getNextFilesId()
    {
        // FIXME: *HACK* This really needs to be stored in the database and
        // have its own type.
        return 100;
    }
    
    private static String getPath(String prefix, Integer id)
    {
        String suffix = "";
        Integer remaining = id;
        Integer dirno = 0;
        Integer i = 0;
        
        if (id == null)
            throw new NullPointerException("Expecting a not-null id.");

        while (remaining > 999)
        {
            if (i > 0)
            {
                dirno = remaining % 1000;
                suffix = dirno + "/" + suffix;
            }

            remaining /= 1000;
            i++;
        }
        
        return root + prefix + suffix + id;
    }
}
