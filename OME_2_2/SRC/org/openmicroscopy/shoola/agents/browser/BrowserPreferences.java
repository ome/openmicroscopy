/*
 * org.openmicroscopy.shoola.agents.browser.BrowserPreferences
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

import org.openmicroscopy.shoola.env.config.Registry;

/**
 * A collection of browser preferences that can be passed to any component.  Allows
 * multiple agents to either retrieve or set the information for a particular
 * browser.  Separate from the browser model in that the browser model contains
 * backing information, whereas the browser preferences control how that
 * information is displayed.
 * 
 * @author Jeff Mellen, <a href="mailto:jeffm@alum.mit.edu">jeffm@alum.mit.edu</a><br>
 * <b>Internal version:</b> $Revision$ $Date$
 * @version 2.2.1
 * @since OME2.2.1
 */
public class BrowserPreferences
{
    // whether or not to dynamically generate (false) or use the thumbnails
    // saved in OMEIS (true), including the specified size.  Default is true.
    private boolean useServerThumbs = true;
    
    // the maximum width of each thumbnail in the browser.  backup default (in the
    // case that the browser application context record is not present) is 64.
    private int thumbnailWidth = 64;
    
    // the maximum height of each thumbnail in the browser.  backup default (in the
    // case that the browser application context record is not present) is 64.
    private int thumbnailHeight = 64;
    
    // the maximum width of the magnifier.  backup default (in the case that the
    // browser application context record is not present) is 150.
    private int magnifierWidth = 150;
    
    // the maximum height of the magnifier.  backup default (in the case that the
    // browser application context record is not present) is 150.
    private int magnifierHeight = 150;
    
    /**
     * The XML key for getting the desired thumbnail extraction mode.
     * (server or composite)
     */
    public static final String THUMBNAIL_MODE_KEY =
        "/agents/browser/config/useServerThumbs";
    
    /**
     * The XML key for getting the composite mode thumbnail width.
     */
    public static final String THUMBNAIL_WIDTH_KEY =
        "/agents/browser/config/thumbnailWidth";
    
    /**
     * The XML key for getting the composite mode thumbnail height.
     */
    public static final String THUMBNAIL_HEIGHT_KEY =
        "/agents/browser/config/thumbnailHeight";
    
    /**
     * The default (maximum) width for the magnifier.
     */
    public static final String MAGNIFIER_WIDTH_KEY =
        "/agents/browser/config/semanticWidth";    
    
    /**
     * The default (maximum) height for the magnifier.
     */
    public static final String MAGNIFIER_HEIGHT_KEY =
        "/agents/browser/config/semanticHeight";
    
    
    /**
     * Constructs the browser preferences without consulting the application
     * context-- just using the backup default values stored at compile-time.
     * 
     * Not recommended unless the registry is not present for some reason.
     */
    public BrowserPreferences()
    {
        // do nothing
    }
    
    /**
     * Constructs a browser preferences object from the application context.
     * @param registry The context to receive the default browser preference
     *                 values from.
     * @throws IllegalArgumentException If the registry object is null.
     */
    public BrowserPreferences(Registry registry)
        throws IllegalArgumentException
    {
        if(registry == null)
        {
            throw new IllegalArgumentException("Null registry");
        }
        
        try
        {
            Boolean extractionMode = (Boolean)registry.lookup(THUMBNAIL_MODE_KEY);
            this.useServerThumbs = extractionMode.booleanValue();
        }
        catch(NullPointerException npe)
        {
            // do nothing; revert to backup default (already set)
        }
        
        try
        {
            Integer thumbWidth = (Integer)registry.lookup(THUMBNAIL_WIDTH_KEY);
            this.thumbnailWidth = thumbWidth.intValue();
        }
        catch(NullPointerException npe)
        {
            // do nothing; revert to backup default (already set)
        }
        
        try
        {
            Integer thumbHeight = (Integer)registry.lookup(THUMBNAIL_HEIGHT_KEY);
            this.thumbnailHeight = thumbHeight.intValue();
        }
        catch(NullPointerException npe)
        {
            // do nothing; revert to backup default (already set)
        }
        
        try
        {
            Integer magWidth = (Integer)registry.lookup(MAGNIFIER_WIDTH_KEY);
            this.magnifierWidth = magWidth.intValue();
        }
        catch(NullPointerException npe)
        {
            // do nothing; revert to backup default (already set)
        }
        
        try
        {
            Integer magHeight = (Integer)registry.lookup(MAGNIFIER_HEIGHT_KEY);
            this.magnifierHeight = magHeight.intValue();
        }
        catch(NullPointerException npe)
        {
            // do nothing; revert to backup default (already set)
        }
    }
     
    /**
     * @return Returns the magnifier height.
     */
    public int getMagnifierHeight()
    {
        return magnifierHeight;
    }
    
    /**
     * @param magnifierHeight The magnifier height to set.
     */
    public void setMagnifierHeight(int magnifierHeight)
    {
        this.magnifierHeight = magnifierHeight;
    }
    
    /**
     * @return Returns the magnifier width.
     */
    public int getMagnifierWidth()
    {
        return magnifierWidth;
    }
    
    /**
     * @param magnifierWidth The magnifier width to set.
     */
    public void setMagnifierWidth(int magnifierWidth)
    {
        this.magnifierWidth = magnifierWidth;
    }
    
    /**
     * @return Returns the thumbnail height.
     */
    public int getThumbnailHeight()
    {
        return thumbnailHeight;
    }
    
    /**
     * @param thumbnailHeight The thumbnail height to set.
     */
    public void setThumbnailHeight(int thumbnailHeight)
    {
        this.thumbnailHeight = thumbnailHeight;
    }
    
    /**
     * @return Returns the thumbnail width.
     */
    public int getThumbnailWidth()
    {
        return thumbnailWidth;
    }
    
    /**
     * @param thumbnailWidth The thumbnail width to set.
     */
    public void setThumbnailWidth(int thumbnailWidth)
    {
        this.thumbnailWidth = thumbnailWidth;
    }
    
    /**
     * Returns whether or not the browser should load thumbnails without resizing.
     * @return See above.
     */
    public boolean useServerThumbs()
    {
        return useServerThumbs;
    }
    
    /**
     * Sets whether or not the browser should specify a custom thumbnail size.
     * @param useServerThumbs See above.
     */
    public void setUseServerThumbs(boolean useServerThumbs)
    {
        this.useServerThumbs = useServerThumbs;
    }
}
