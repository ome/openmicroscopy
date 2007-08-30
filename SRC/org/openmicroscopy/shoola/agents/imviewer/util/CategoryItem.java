/*
 * org.openmicroscopy.shoola.agents.imviewer.util.CategoryItem 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2007 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.agents.imviewer.util;


//Java imports
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.Icon;
import javax.swing.JMenuItem;

//Third-party libraries

//Application-internal dependencies
import pojos.CategoryData;

/** 
 * Utility class hosting a category.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class CategoryItem
	extends JMenuItem
{

	/** Bounds property indicating to remove the image from the category. */
	public static final String REMOVE_PROPERTY = "remove";
	
	/** Bounds property indicating to browse the category. */
	public static final String BROWSE_PROPERTY = "browse";
	
	/** The category hosted by the component. */
	private CategoryData data;
	
	/**
	 * Handles the mouse pressed event.
	 * 
	 * @param p The location of the mouse pressed.
	 */
	private void handleMousePressed(Point p)
	{
		Icon icon = getIcon();
		int h = icon.getIconHeight();
		int w = icon.getIconWidth();
		Rectangle bounds = getBounds();
		Rectangle r = new Rectangle(bounds.x+w, 0, w, h);
		Rectangle rText = new Rectangle(r.x+r.width, r.y, 
							bounds.width-r.x-r.width, h);
		if (r.contains(p)) {
			firePropertyChange(REMOVE_PROPERTY, null, this);
		} else if (rText.contains(p)) {
			firePropertyChange(BROWSE_PROPERTY, null, this);
		}
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param data	The category hosting by this node.
	 */
	public CategoryItem(CategoryData data)
	{
		if (data == null)
			throw new IllegalArgumentException("No category specified.");
		this.data = data;
		setText(data.getName());
		setToolTipText(data.getDescription());
	}

	/**
	 * Overridden to browse or declassify depending on the location
	 * of the mouse pressed.
	 * @see JMenuItem#setIcon(Icon)
	 */
	public void setIcon(Icon icon)
	{
		super.setIcon(icon);
		addMouseListener(new MouseAdapter() {
		
			/** 
			 * Browses or declassifies.
			 * @see MouseAdapter#mousePressed(MouseEvent)
			 */
			public void mousePressed(MouseEvent e) {
				handleMousePressed(e.getPoint());
			}
		
		});
	}
	
	/**
	 * Returns the description of the category.
	 * 
	 * @return See above.
	 */
	public String getObjectDescription() { return data.getDescription(); }
	
	/**
	 * Returns the name of the category.
	 * 
	 * @return See above.
	 */
	public String getObjectName() { return data.getName(); }
	
	/**
	 * Returns the id of the category owner.
	 * 
	 * @return See above.
	 */
	public long getOwnerID() { return data.getOwner().getId(); }
	
	/**
	 * Returns the id of the object hosted by this component.
	 * 
	 * @return See above.
	 */
	public long getObjectID() { return data.getId(); }
	
	/**
	 * Overridden to return the name of the data object.
	 * @see Object#toString()
	 */
	public String toString() { return data.getName(); }

}
