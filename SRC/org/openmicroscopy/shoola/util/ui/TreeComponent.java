/*
 * org.openmicroscopy.shoola.util.ui.TreeComponent
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2008 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.util.ui;


//Java imports
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JPanel;

//Third-party libraries

//Application-internal dependencies

/** 
 * Component laying out the {@link TreeComponentNode}s.
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
public class TreeComponent 
	extends JPanel
	implements PropertyChangeListener
{

	/** Indicates to lay out the components horizontally. */
	public static final int 	HORIZONTAL = 0;
	
	/** Indicates to lay out the components vertically. */
	public static final int 	VERTICAL = 1;
	
	/** Bound property indicating the displayed component. */
	public static final String	EXPANDED_PROPERTY = "expanded";
	
	/** The icon displayed when the {@link #collapse} is selected. */
	private Icon 		collapseIcon;
	
	/** The icon displayed when the {@link #elapse} is selected. */
	private Icon 		elapseIcon;
	
	/** One of the orientation constants defined by this class. */
	private int			orientation;
	
	/** Initializes the components.  */
	private void initialize()
	{
		IconManager icons = IconManager.getInstance();
		
		switch (orientation) {
			case VERTICAL:
				collapseIcon = icons.getIcon(IconManager.RIGHT_ARROW_DISABLED);
				elapseIcon = icons.getIcon(IconManager.DOWN_ARROW_DISABLED);
				setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
				break;
			case HORIZONTAL:
				elapseIcon = icons.getIcon(IconManager.RIGHT_ARROW_DISABLED);
				collapseIcon = icons.getIcon(IconManager.DOWN_ARROW_DISABLED);
				setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		}
	}
	
	/** Creates a new instance. */
	public TreeComponent()
	{
		this(VERTICAL);
	}
	
	/** 
	 * Creates a new instance. 
	 * 
	 * @param orientation 	One of the orientation constants defined by this 
	 * 						class.
	 */
	public TreeComponent(int orientation)
	{
		this.orientation = orientation;
		initialize();
	}
	
	/**
	 * Returns the orientation.
	 * 
	 * @return See above.
	 */
	int getOrientation() { return orientation; }
	
	/**
	 * Inserts a new node and expands it.
	 * 
	 * @param elapse	The component to display when the node is expanded.
	 * @param collapse	The component to display when the node is collapsed.
	 */
	public void insertNode(JComponent elapse, JComponent collapse)
	{
		insertNode(elapse, collapse, true);
	}
	
	/**
	 * Inserts a new node, collapses it if the passed flag is <code>true</code>
	 * expands it otherwise.
	 * 
	 * @param elapse	The component to display when the node is expanded.
	 * @param collapse	The component to display when the node is collapsed.
	 * @param expanded	Pass <code>true</code> to expand the node, 
	 * 					<code>false</code> to collapse it.
	 */
	public void insertNode(JComponent elapse, JComponent collapse, 
							boolean expanded)
	{
		TreeComponentNode node = new TreeComponentNode(elapse, collapse, 
														expanded);
		node.setIcons(collapseIcon, elapseIcon);
		node.addPropertyChangeListener(this);
		add(node);
	}
	
	/**
	 * Sets the collapse icon.
	 * 
	 * @param icon	The icon to set.
	 */
	public void setCollapseIcon(Icon icon) { collapseIcon = icon; }
	
	/**
	 * Sets the elapse icon.
	 * 
	 * @param icon	The icon to set.
	 */
	public void setElapseIcon(Icon icon) { elapseIcon = icon; }
	
	/**
	 * Sets the icons.
	 * 
	 * @param collapseIcon The collapse icon.
	 * @param elapseIcon	The elapse icon.
	 */
	public void setIcons(Icon collapseIcon, Icon elapseIcon)
	{
		this.collapseIcon = collapseIcon;
		this.elapseIcon = elapseIcon;
	}

	/**
	 * Revalidates the component and fires a property change.
	 * @see PropertyChangeListener#propertyChange(PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent evt)
	{
		revalidate();
		firePropertyChange(EXPANDED_PROPERTY, Boolean.FALSE, Boolean.TRUE);
	}
	
}
