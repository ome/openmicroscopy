/*
 * org.openmicroscopy.xdoc.navig.IconFactory
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

package org.openmicroscopy.xdoc.navig;


//Java imports
import java.net.URL;
import javax.swing.Icon;
import javax.swing.ImageIcon;

//Third-party libraries

//Application-internal dependencies

/** 
 * Helper class to make the icons for the applet UI.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:a.falconi@dundee.ac.uk">
 * 					a.falconi@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
class IconFactory
{
    
    /** 
     * The maximum ID used for the icon IDs.
     * Allows to correctly build arrays for direct indexing. 
     */
    private static int      MAX_ID = 4;
    
    /** Paths of the icon files. */
    private static String[] relPaths = new String[MAX_ID+1];
    
    /** Location of the directory containing the icons. */
    private static String   location = "/org/openmicroscopy/xdoc/navig/graphx";
    
    /** Identifies the open icon of the toc node. */
    static final int    TOC_OPEN = 0;
    
    /** Identifies the closed icon of the toc node. */
    static final int    TOC_CLOSED = 1;
    
    /** Identifies the open icon of a section node. */
    static final int    SECTION_OPEN = 2;
    
    /** Identifies the closed icon of a section node. */
    static final int    SECTION_CLOSED = 3;
    
    /** 
     * Identifies the icon of a sub-section node.
     * This is the icon that associated to any leaf node.  In particular, if
     * a section has no sub-sections, this is the icon that is going to get. 
     */
    static final int    SUB_SECTION = 4;
    
    static {
        relPaths[TOC_OPEN] = "eclipse_toc_open.png";
        relPaths[TOC_CLOSED] = "eclipse_toc_closed.png";
        relPaths[SECTION_OPEN] = "eclipse_container_obj.png";
        relPaths[SECTION_CLOSED] =  "eclipse_e_show_all.png";
        relPaths[SUB_SECTION] = "eclipse_topic.png";
    }
    
    /** 
     * Retrieves the icon specified by <code>id</code>.
     * If the icon can't be retrieved, then this method will return 
     * <code>null</code>.
     *
     * @param id    The index of the file name in the array of file names 
     *              specified to this class' constructor.
     * @return  An {@link Icon} object created from the image file.  The return
     *          value will be <code>null</code> if the file couldn't be found
     *          or an image icon couldn't be created from that file.
     */ 
    static Icon getIcon(int id)
    {
        if (id < 0 || relPaths.length <= id) return null;
        return getIcon(relPaths[id]);
    }
    
    /** 
     * Retrieves the icon specified by <code>name</code>.
     * If the icon can't be retrieved, then this method will return 
     * <code>null</code>.
     *
     * @param name    Must be one a valid icon file name within the directory
     *                  used by the {@link IconFactory} instance specified via
     *                  this class' constructor.
     * @return  An {@link Icon} object created from the image file.  The return
     *          value will be <code>null</code> if the file couldn't be found
     *          or an image icon couldn't be created from that file.
     */ 
    static Icon getIcon(String name)
    {
        ImageIcon icon = null;
        try {
            String path = getResourcePathname(name);
            URL url = IconFactory.class.getResource(path);
            icon = new ImageIcon(url);
        } catch (Exception e) {}
        return icon;
    }

    /**
     * Returns the pathname of the specified file.
     * The returned pathname is relative to the application classpath.
     *  
     * @param iconFileName  The file name.
     * @return See above.
     */
    private static String getResourcePathname(String iconFileName)
    {
        return location+"/"+iconFileName;
    }
    
}
