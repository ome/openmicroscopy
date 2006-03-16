/*
 * omeis.io.PixelsHeader
 *
 *------------------------------------------------------------------------------
 *
 *  Copyright (C) 2004 Open Microscopy Environment
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

package tmp;

import ome.model.enums.PixelsType;


/** 
 * Simple PixelType helper.
 */
public class PixelTypeHelper
{
	public static boolean in(PixelsType type, String[] strings)
	{
		String typeAsString = type.getValue();
		for (int i = 0; i < strings.length; i++)
			if (typeAsString.equals(strings[i]))
				return true;
		return false;
	}
    
    public static int bytesPerPixel(PixelsType type)
    {
        if (in(type, new String[] {"int8", "uint8" }))
            return 1;
        else if (in(type, new String[] { "int16", "uint16" }))
            return 2;
        else if (in(type, new String[] { "int32", "uint32", "float" }))
            return 4;
        else if (type.getValue().equals("double"))
            return 8;
        else
            throw new RuntimeException("Unknown pixel type: '"
                    + type.getValue() + "'");
    }
}
