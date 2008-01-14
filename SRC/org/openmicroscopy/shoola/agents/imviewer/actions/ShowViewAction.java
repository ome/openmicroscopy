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

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.imviewer.IconManager;
import org.openmicroscopy.shoola.agents.imviewer.view.ImViewer;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

/** 
 * 
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
	public static final int	ANNOTATION = ImViewer.ANNOTATOR_INDEX;
	
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
				putValue(Action.NAME, "View");
				putValue(Action.SMALL_ICON, 
						icons.getIcon(IconManager.VIEWER));
				break;
			case ANNOTATION:
				putValue(Action.NAME, "Annotation");
				putValue(Action.SMALL_ICON, 
						icons.getIcon(IconManager.ANNOTATION));
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
    public void actionPerformed(ActionEvent e)
    {
    	model.showView(index);
    }
    
}
