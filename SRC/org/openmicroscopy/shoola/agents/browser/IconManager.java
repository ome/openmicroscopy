/*
 * org.openmicroscopy.shoola.agents.browser.IconManager
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

/*------------------------------------------------------------------------------
 *
 * Written by:    Jeff Mellen <jeffm@alum.mit.edu>
 *
 *------------------------------------------------------------------------------
 */
 
package org.openmicroscopy.shoola.agents.browser;

import java.awt.Image;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.openmicroscopy.shoola.env.config.IconFactory;
import org.openmicroscopy.shoola.env.config.Registry;

/**
 * Contains references to all the icons used in the browser.
 *
 * @author Jeff Mellen, <a href="mailto:jeffm@alum.mit.edu">jeffm@alum.mit.edu</a><br>
 * <b>Internal version:</b> $Revision$ $Date$
 * @version 2.2
 * @since OME2.2
 */
public class IconManager
{
    /**
     * Contains small (16x16?) icon objects (with embedded images) to be retrieved via
     * icon IDs.
     */
    private Icon[] smallIcons;
    
    /**
     * Contains large (32x32?) icon objects (with embedded images) to be retrieved via
     * icon IDs.
     */
    private Icon[] largeIcons;
    
    /**
     * The main image browser icon.
     */
    public static final int BROWSER = 0;
    
    /**
     * The trigger zoom-toolbar icon.
     */
    public static final int ZOOM_BAR = 1;
    
    /**
     * The trigger options toolbar icon.
     */
    public static final int OPTIONS_BAR = 2;
    
    /**
     * The open image icon.
     */
    public static final int OPEN_IMAGE = 3;
    
    /**
     * The close (magnified) image icon.
     */
    public static final int CLOSE_IMAGE = 4;
    
    /**
     * The ST tree string icon.
     */
    public static final int ST_TREE_STRING = 5;
    
    /**
     * The ST tree numerical icon.
     */
    public static final int ST_TREE_NUMBER = 6;
    
    /**
     * The ST tree type icon.
     */
    public static final int ST_TREE_TYPE = 7;
    
    /**
     * The ST tree boolean icon.
     */
    public static final int ST_TREE_BOOLEAN = 8;
    
    /**
     * The maximum image ID (increment if you add additional icons.)
     */
    public static final int MAX_ID = 8;
    
    /**
     * The pathnames of the (smaller) icons.
     */
    private static String[] smallPaths = new String[MAX_ID+1];
    
    /**
     * The pathnames of the (larger) icons.
     */
    private static String[] largePaths = new String[MAX_ID+1];
    
    static
    {
        smallPaths[BROWSER] = "browser16.png";
        smallPaths[ZOOM_BAR] = "zoombar.png";
        smallPaths[OPTIONS_BAR] = "optionsbar.png";
        smallPaths[OPEN_IMAGE] = "openimage.png";
        smallPaths[CLOSE_IMAGE] = "closeimage.png";
        smallPaths[ST_TREE_STRING] = "st_string.png";
        smallPaths[ST_TREE_NUMBER] = "st_number.png";
        smallPaths[ST_TREE_BOOLEAN] = "st_boolean.png";
        smallPaths[ST_TREE_TYPE] = "st_type.png";
    }
    
    static
    {
        largePaths[BROWSER] = "browser24.png";
        largePaths[ZOOM_BAR] = "zoombar.png";
        largePaths[OPTIONS_BAR] = "optionsbar.png";
        largePaths[OPEN_IMAGE] = "openimage.png";
        largePaths[CLOSE_IMAGE] = "closeimage.png";
        largePaths[ST_TREE_STRING] = "st_string.png";
        largePaths[ST_TREE_NUMBER] = "st_number.png";
        largePaths[ST_TREE_BOOLEAN] = "st_boolean.png";
        largePaths[ST_TREE_TYPE] = "st_type.png";
    }
    
    private static final String ICON_FACTORY_KEY = "/resources/icons/Factory";
    
    /**
     * The lone instance of the browser icon manager.
     */
    private static IconManager singleton;
    
    private IconFactory factory;
    
    private IconManager(Registry registry)
    {
        factory = (IconFactory)registry.lookup(ICON_FACTORY_KEY);
        smallIcons = new Icon[MAX_ID+1];
        largeIcons = new Icon[MAX_ID+1];
    }
    
    /**
     * Gets an instance of this icon manager 
     * @param registry The 
     * @return
     */
    public static IconManager getInstance(Registry registry)
    {
        if(registry == null)
        {
            return null;
        }
        
        if(singleton == null)
        {
            try
            {
                singleton = new IconManager(registry);
            }
            catch(Exception e)
            {
                System.err.println("problem");
                throw new RuntimeException("Couldn't create the browser icon" +
                    " manager.");
            }
        }
        return singleton;
    }
    
    /**
     * Different semantics for getInstance().
     * @param registry The registry to set.
     */
    public static void setRegistry(Registry registry)
    {
        getInstance(registry);
    }
    
    /**
     * Gets the small icon (if available) from the manager or from disk.
     * @param ID The ID of the icon to retrieve.
     * @return The icon mapped to the specified ID.
     */
    public Icon getSmallIcon(int ID)
    {
        // kind of a weak check, but... whatever.
        if(ID < 0 || ID > MAX_ID)
        {
            return null;
        }
        
        if(smallIcons[ID] == null)
        {
            smallIcons[ID] = factory.getIcon(smallPaths[ID]);
        }
        return smallIcons[ID];
    }
    
    /**
     * Returns the image on the small icon (if available) from manager or
     * from disk.
     * @param ID The ID of the image to retrieve.
     * @return The image mapped to the specified ID.
     */
    public Image getSmallImage(int ID)
    {
        ImageIcon icon;
        if((icon = (ImageIcon)getSmallIcon(ID)) != null)
        {
            return icon.getImage();
        }
        else return null;
    }
    
    /**
     * Gets the large icon (if available) from the manager or from disk.
     * @param ID The ID of the icon to retrieve.
     * @return The icon mapped to the specified ID.
     */
    public Icon getLargeIcon(int ID)
    {
        if(ID < 0 || ID > MAX_ID)
        {
            return null;
        }
        
        if(largeIcons[ID] == null)
        {
            largeIcons[ID] = factory.getIcon(largePaths[ID]);
        }
        return largeIcons[ID];
    }
    
    /**
     * Returns the image on the large icon (if available) from manager or
     * from disk.
     * @param ID The ID of the image to retrieve.
     * @return The image mapped to the specified ID.
     */
    public Image getLargeImage(int ID)
    {
        ImageIcon icon;
        if((icon = (ImageIcon)getLargeIcon(ID)) != null)
        {
            return icon.getImage();
        }
        else return null;
    }
}
