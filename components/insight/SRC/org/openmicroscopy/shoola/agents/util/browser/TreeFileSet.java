/*
 * org.openmicroscopy.shoola.agents.treeviewer.browser.TreeFileSet 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2015 University of Dundee. All rights reserved.
 *
 *
 * 	This program is free software; you can redistribute it and/or modify
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
package org.openmicroscopy.shoola.agents.util.browser;



//Java imports
import java.util.Iterator;

import org.openmicroscopy.shoola.agents.dataBrowser.DataBrowserAgent;
import org.openmicroscopy.shoola.env.LookupNames;
import org.openmicroscopy.shoola.env.config.Registry;

//Third-party libraries

//Application-internal dependencies

/** 
 * Nodes hosting <code>FileAnnotationData</code> objects.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
public class TreeFileSet 
	extends TreeImageSet
{

	/** Indicates that the node should host movie files. */
	public static final int MOVIE = 2;
	
	/** Indicates that the node should host all the other types of files. */
	public static final int OTHER = 3;
	
	/** 
	 * Indicates that the node should host all tags not owned by an
	 * experimenter but used by him/her. 
	 */
	public static final int TAG = 4;

	/** Indicates that the node should host the orphaned images */
	public static final int ORPHANED_IMAGES = 5;
	
	/**
	 * Returns the value corresponding to the passed index.
	 * 
	 * @param type The type to handle;
	 * @return See above.
	 */
	private static String getTypeName(int type)
	{
		Registry reg = DataBrowserAgent.getRegistry();
		
		switch (type) {
			case MOVIE: return "Movies";
			case ORPHANED_IMAGES: 
			    return (String) reg.lookup(LookupNames.ORPHANED_IMAGE_NAME);
			case TAG:
				return "Tags used not owned";
			case OTHER:
			default:
				return "Other files";
		}
	}
	
	/** One of the constants defined by this class. */
	private int type;
	
	/**
	 * Creates a new instance.
	 * 
	 * @param type One of the constants defined by this class.
	 */
	public TreeFileSet(int type)
	{
		super(getTypeName(type));
		switch (type) {
			case MOVIE:
			case TAG:
			case ORPHANED_IMAGES:
				this.type = type;
				break;
			case OTHER:
			default:
				this.type = OTHER;
		}
	}
	
	/**
	 * Returns the type. One of the constants defined by this class.
	 * 
	 * @return See above.
	 */
	public int getType() { return type; }
	
	/**
     * Makes a copy of the node.
     * @see TreeImageDisplay#copy()
     */
    public TreeImageDisplay copy()
    {
    	TreeImageSet copy = new TreeFileSet(this.getType());
        copy.setChildrenLoaded(Boolean.valueOf(this.isChildrenLoaded()));
        copy.setNumberItems(this.getNumberItems());
        copy.setHighLight(this.getHighLight());
        copy.setToolTip(this.getToolTip());
        copy.setExpanded(this.isExpanded());
        Iterator i = this.getChildrenDisplay().iterator();
        while (i.hasNext()) {
            copy.addChildDisplay(((TreeImageDisplay) i.next()).copy());
        }
        return copy;
    }
    
}
