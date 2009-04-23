/*
 * ome.ij.dm.TreeViewerFactory 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2009 University of Dundee. All rights reserved.
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
package ome.ij.dm;


//Java imports
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;



//Third-party libraries
import ij.IJ;

//Application-internal dependencies
import ome.ij.data.ServicesFactory;

/** 
 * Recycles or creates a new {@link Viewer}.
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
public class TreeViewerFactory
	implements PropertyChangeListener
{

	/** The sole instance. */
	private static final TreeViewerFactory singleton = new TreeViewerFactory();

	/**
	 * Returns the tree viewer.
	 * 
	 * @return See above.
	 */
	public static TreeViewer getTreeViewer()
	{
		if (singleton.viewer != null) return singleton.viewer;
		return singleton.createViewer();
	}
	
	/** The tracked viewer. */
	private TreeViewer viewer;
	
	/** Creates a new instance. */
	private TreeViewerFactory()
	{
		viewer = null;
		IJ.getInstance().addPropertyChangeListener(
				ServicesFactory.DISCONNECT_PROPERTY, this);
	}
	
	/** 
	 * Creates the viewer.
	 * 
	 * @return See above.
	 */
	private TreeViewer createViewer()
	{
		viewer = new TreeViewer();
		viewer.addPropertyChangeListener(TreeViewer.CLOSE_MANAGER_PROPERTY, 
				this);
		return viewer;
	}

	/**
	 * Reacts to property fired by the viewer.
	 * @see PropertyChangeListener#propertyChange(PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent evt)
	{
		String name = evt.getPropertyName();
		if (TreeViewer.CLOSE_MANAGER_PROPERTY.equals(name) ||
			ServicesFactory.DISCONNECT_PROPERTY.equals(name)) {
			if (viewer != null) {
				viewer.discard();
				viewer = null;
				ServicesFactory.getInstance().exitPlugin();
			}
		}
	}
	
}
