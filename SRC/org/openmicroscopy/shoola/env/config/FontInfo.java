/*
 * org.openmicroscopy.shoola.env.config.FontInfo
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

package org.openmicroscopy.shoola.env.config;

/** Creates an Object which contains the informations on the font
 *
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 *              <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 *              <a href="mailto:a.falconi@dundee.ac.uk">
 *              a.falconi@dundee.ac.uk</a>
 * <b>Internal version:</b> $Revision$  $Date$
 * @version 2.2
 * @since OME2.2
 */

class FontInfo
{
    
    String              family, style;
    Integer             size;
    static final String FAMILY = "family", SIZE = "size", STYLE = "style"; 

/* 
 * @param value
 * @param tag
 */
    void setValue(String value, String tag)
    {
        try {
            if (tag.equals(FAMILY)) family = value;
            else if (tag.equals(SIZE)) size = new Integer(value);
            else if (tag.equals(STYLE)) style = value;
        } catch (Exception ex) { throw new RuntimeException(ex); }
    }
/* return the value of the <code>family</code>
 *
 * @return String
 */
    String getFamily()
    {
        return family;
    }
/*  return the value of the <code>size</code>
 *
 * @return Integer
 */
    Integer getSize()
    {
        return size;
    }
/* return the value of the <code>style</code> 
 *
 * @return String
 */
    String getStyle()
    {
        return style;
    }
  
    
}
