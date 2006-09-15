/*
 * org.openmicroscopy.shoola.util.ui.IconFactory
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

package org.openmicroscopy.shoola.util.ui;

import java.net.URL;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.openmicroscopy.shoola.env.ui.IconManager;


//Java imports

//Third-party libraries

//Application-internal dependencies

/** 
 * We may not distribute the UI package with Shoola, so we have an internal
 * factory for Icon.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author	Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $ $Date: $)
 * </small>
 * @since OME2.2
 */
class IconFactory
{

    /** 
     * Points to the directory specified by the <i>location</i> tag.
     * The path is relative to the application classpath.
     */
    private String  location = "graphx/";
    
    /**
     * Returns the pathname of the specified file.
     * The returned pathname is relative to the application classpath.
     *  
     * @param iconFileName  The file name.
     * @return See above.
     */
    String getResourcePathname(String iconFileName)
    {
        return location+"/"+iconFileName;
    }
    
    /** 
     * Creates an {@link Icon} from the specified file.
     * 
     * @param name  The file name.  Must be a valid name within the location
     *              specified in the configuration file.
     * @return      An {@link Icon} object created from the image file. 
     *              The return value will be <code>null</code> if the file 
     *              couldn't be found or an image icon couldn't be created from
     *              that file.
     */
    Icon getIcon(String name)
    {
        ImageIcon icon = null;
        try {
            String path = getResourcePathname(name);
            URL url = IconFactory.class.getResource(path);
            icon = new ImageIcon(url);
        } catch (Exception e) {} 
        return icon;
    }

}
