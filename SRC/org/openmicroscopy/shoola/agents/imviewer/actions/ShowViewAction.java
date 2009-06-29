/*
 * org.openmicroscopy.shoola.agents.imviewer.actions.ShowViewAction 
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
package org.openmicroscopy.shoola.agents.imviewer.actions;



//Java imports
import java.awt.event.ActionEvent;
import javax.swing.Action;
import javax.swing.event.ChangeEvent;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.imviewer.IconManager;
import org.openmicroscopy.shoola.agents.imviewer.view.ImViewer;

/** 
 * Displays the View corresponding to the index.
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
public class ShowViewAction 
	extends ViewerAction
{

	/** Identifies the <code>View</code> pane. */
	public static final int	VIEW = ImViewer.VIEW_INDEX;
	
	/** Identifies the <code>View</code> pane. */
	public static final int	PROJECTION = ImViewer.PROJECTION_INDEX;
	
	/** Identifies the <code>View</code> pane. */
	public static final int	SPLIT = ImViewer.GRID_INDEX;
	
	/** One of the constants defined by this class .*/
	private int index;
	
	/**
	 * Checks if the index is valid.
	 * 
	 * @param value The value to handle.
	 */
	private void checkIndex(int value)
	{
		IconManager icons = IconManager.getInstance();
		switch (value) {
			case VIEW:
				putValue(Action.NAME, "Image");
				putValue(Action.SMALL_ICON, 
						icons.getIcon(IconManager.VIEWER));
				break;
			case PROJECTION:
				putValue(Action.NAME, "Projection");
				putValue(Action.SMALL_ICON, 
						icons.getIcon(IconManager.PROJECTION));
				break;
			case SPLIT:
				putValue(Action.NAME, "Split");
				putValue(Action.SMALL_ICON, 
						icons.getIcon(IconManager.GRIDVIEW));
				break;
				default:
					throw new IllegalArgumentException("Index not valid.");
		}
		this.index = value;
	}
	
	/**
     * Sets the enabled flag depending on the index and the number of 
     * z-sections.
     * 
     * @param e The event to handle.
     */
    protected void onStateChange(ChangeEvent e)
    {
    	switch (index) {
			case PROJECTION:
				if (model.getState() == ImViewer.READY) 
		    		setEnabled(model.getMaxZ() > 1);
				break;
			case SPLIT:
				if (model.getState() == ImViewer.READY) 
		    		setEnabled(model.allowSplitView());
		}	
    }
    
	/**
     * Creates a new instance.
     * 
     * @param model     Reference to the model. Mustn't be <code>null</code>.
     * @param index		One of the constants defined by this class.
     */
    public ShowViewAction(ImViewer model, int index)
    {
    	super(model);
    	checkIndex(index);
    }
    
    /** 
     * Adds the selected view to the display.
     * @see java.awt.event.ActionListener#actionPerformed(ActionEvent)
     */
    public void actionPerformed(ActionEvent e) { model.showView(index); }
    
}
