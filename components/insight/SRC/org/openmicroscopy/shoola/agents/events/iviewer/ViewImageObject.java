/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2011 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.agents.events.iviewer;

import org.openmicroscopy.shoola.env.rnd.RndProxyDef;
import omero.gateway.model.DataObject;
import omero.gateway.model.ImageData;
import omero.gateway.model.WellSampleData;

/** 
 * Helper class indicating the object to view.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * @since 3.0-Beta4
 */
public class ViewImageObject
{

    /** The image to view. */
    private DataObject image;

    /** Rendering settings to set if any. */
    private RndProxyDef settings;

    /** The id of the user who set the rendering settings. */
    private long selectedUserID;

    /** The parent of the image or <code>null</code> if no context specified. */
    private DataObject parent;

    /** 
     * The grandparent of the image or <code>null</code> if no 
     * context specified. 
     */
    private DataObject grandParent;

    /** The id of the image to view. Used when no <code>ImageData</code>set. */
    private long imageID;

    /** The id of the selected rendering object.*/
    private long rndDefID;

    /**
     * Creates a new instance.
     *
     * @param imageID The id of the image to view.
     */
    public ViewImageObject(long imageID)
    {
        if (imageID < 0l) 
            throw new IllegalArgumentException("Image ID not valid.");
        this.imageID = imageID;
        selectedUserID = -1;
        rndDefID = -1;
    }

    /**
     * Creates a new instance.
     *
     * @param image The image to view.
     */
    public ViewImageObject(DataObject image)
    {
        if (image == null) 
            throw new IllegalArgumentException("Image not null.");
        if (!(image instanceof ImageData || image instanceof WellSampleData))
            throw new IllegalArgumentException("Object can either be a " +
                    "WellSample or an Image.");
        this.image = image;
        selectedUserID = -1;
        imageID = -1;
        rndDefID = -1;
    }

    /**
     * Sets the identifier of the rendering settings, this should only be used
     * for settings used under "Users settings".
     *
     * @param rndDefID The value to set.
     */
    public void setSelectedRndDef(long rndDefID)
    {
        this.rndDefID = rndDefID;
    }

    /**
     * Returns the identifier of the rendering settings.
     * @return See above.
     */
    public long getSelectedRndDef() { return rndDefID; }

    /**
     * Sets the context of the node.
     * 
     * @param parent The parent of the image or <code>null</code>
     *               if no context specified.
     * @param grandParent The grandparent of the image or <code>null</code>
     *                    if no context specified.
     */
    public void setContext(DataObject parent, DataObject grandParent)
    {
        this.parent = parent;
        this.grandParent = grandParent;
    }

    /**
     * Returns the id of the image to view. 
     * 
     * @return See above.
     */
    public long getImageID() { return imageID; }

    /**
     * Returns the parent of the image or <code>null</code> 
     * if no context specified.
     *
     * @return See above.
     */
    public DataObject getParent() { return parent; }

    /**
     * Returns the grandparent of the image or <code>null</code> 
     * if no context specified.
     *
     * @return See above.
     */
    public DataObject getGrandParent() { return grandParent; }

    /**
     * Sets the rendering settings set by the selected user.
     * 
     * @param settings The settings to set.
     * @param selectedUserID The id of the user who set the
     *                       the rendering settings.
     */
    public void setSettings(RndProxyDef settings, long selectedUserID)
    {
        this.settings = settings;
        this.selectedUserID = selectedUserID;
    }

    /**
     * Returns the rendering settings set by the specified user.
     *
     * @return See above.
     */
    public RndProxyDef getSettings() { return settings; }

    /**
     * Returns the ID of the user the settings are related to.
     *
     * @return See above.
     */
    public long getSelectedUserID() { return selectedUserID; }

    /**
     * Returns the image or well sample.
     *
     * @return See above.
     */
    public DataObject getImage() { return image; }

}
