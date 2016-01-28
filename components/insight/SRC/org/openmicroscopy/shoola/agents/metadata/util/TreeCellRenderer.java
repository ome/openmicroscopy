/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2008 University of Dundee. All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */
package org.openmicroscopy.shoola.agents.metadata.util;

import java.awt.Component;
import java.awt.Font;
import javax.swing.Icon;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;

import org.openmicroscopy.shoola.agents.metadata.IconManager;
import org.openmicroscopy.shoola.agents.metadata.MetadataViewerAgent;
import org.openmicroscopy.shoola.agents.metadata.browser.TreeBrowserDisplay;
import org.openmicroscopy.shoola.env.LookupNames;
import omero.gateway.model.DatasetData;
import omero.gateway.model.ExperimenterData;
import omero.gateway.model.ImageData;
import omero.gateway.model.PlateData;
import omero.gateway.model.ProjectData;
import omero.gateway.model.ScreenData;
import omero.gateway.model.TagAnnotationData;

/** 
 * Renderer of Browser's tree.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * @since OME3.0
 */
public class TreeCellRenderer
	extends DefaultTreeCellRenderer
{

	/** Reference to the <code>Image</code> icon. */
	private static final Icon IMAGE_ICON;
	
	/** Reference to the <code>Dataset</code> icon. */
	private static final Icon DATASET_ICON;
	
	/** Reference to the <code>Project</code> icon. */
	private static final Icon PROJECT_ICON;
	
	/** Reference to the <code>Screen</code> icon. */
	private static final Icon SCREEN_ICON;
	
	/** Reference to the <code>Plate</code> icon. */
	private static final Icon PLATE_ICON;
	
	/** Reference to the <code>Tag</code> icon. */
	private static final Icon TAG_ICON;
	
	/** Reference to the <code>Tag Set</code> icon. */
	private static final Icon TAG_SET_ICON;
	
	static { 
		IconManager icons = IconManager.getInstance();
		IMAGE_ICON = icons.getIcon(IconManager.IMAGE);
		DATASET_ICON = icons.getIcon(IconManager.DATASET);
		PROJECT_ICON = icons.getIcon(IconManager.PROJECT);
		SCREEN_ICON = icons.getIcon(IconManager.SCREEN);
		PLATE_ICON = icons.getIcon(IconManager.PLATE);
		TAG_ICON = icons.getIcon(IconManager.TAG);
		TAG_SET_ICON = icons.getIcon(IconManager.TAG_SET);
	}
    
	/** The ID of the current user. */
    private long				currentUserID;
    
    /** The default font. */
    private Font				defaultFont;
    
    /**
     * Sets the icon and the text corresponding to the user's object.
     * 
     * @param usrObject The user object to handle.
     */
    private void setIcon(Object usrObject)
    {
        Icon icon = null;
        if (usrObject instanceof ProjectData) 
        	icon = PROJECT_ICON;
        else if (usrObject instanceof DatasetData) 
        	icon = DATASET_ICON;
        else if (usrObject instanceof ImageData) 
        	icon = IMAGE_ICON;
        else if (usrObject instanceof ScreenData) 
        	icon = SCREEN_ICON;
        else if (usrObject instanceof PlateData) 
        	icon = PLATE_ICON;
        else if (usrObject instanceof TagAnnotationData) {
        	TagAnnotationData tag = (TagAnnotationData) usrObject;
        	if (TagAnnotationData.INSIGHT_TAGSET_NS.equals(tag.getNameSpace()))
        		icon = TAG_SET_ICON;
        	else 
        		icon = TAG_ICON;
        }
        else if (usrObject instanceof String)
        	icon = null;
        setIcon(icon);
    }
    
	/** Creates a new instance. */
	public TreeCellRenderer()
	{
		ExperimenterData exp = 
			(ExperimenterData) MetadataViewerAgent.getRegistry().lookup(
					LookupNames.CURRENT_USER_DETAILS);
		currentUserID = exp.getId();
	}

	/**
	 * Overridden to set the icon and the text.
	 * @see DefaultTreeCellRenderer#getTreeCellRendererComponent(JTree, Object, 
	 * 								boolean, boolean, boolean, int, boolean)
	 */
    public Component getTreeCellRendererComponent(JTree tree, Object value,
                        boolean sel, boolean expanded, boolean leaf,
                        int row, boolean hasFocus)
    {
        super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, 
                                                row, hasFocus);
        
        if (!(value instanceof TreeBrowserDisplay)) return this;
        if (defaultFont == null) 
    		defaultFont = getFont();
        TreeBrowserDisplay node = (TreeBrowserDisplay) value;
        Object object = node.getUserObject();
        
        Icon icon = node.getDefaultIcon();
        if (object instanceof String) 
        	setIcon(null);
        setFont(defaultFont);
        if (icon != null) {
        	setIcon(icon);
        	if (object instanceof String) 
            	setFont(defaultFont.deriveFont(Font.ITALIC, 10));
        } else {
        	setIcon(object);
        }
        return this;
    }
    
}
