/*
 * org.openmicroscopy.shoola.agents.imviewer.actions.ClassifyAction 
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
package org.openmicroscopy.shoola.agents.imviewer.actions;


//Java imports
import java.awt.Component;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.Action;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.imviewer.IconManager;
import org.openmicroscopy.shoola.agents.imviewer.view.ImViewer;
import org.openmicroscopy.shoola.util.ui.UIUtilities;


/** 
 * Brings up the <code>Category</code> menu.
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
public class ClassifyAction 
	extends ViewerAction
	implements MouseListener
{

	/** The name of the action. */
    private static final String NAME = "Tag";
    
    /** The description of the action. */
    private static final String DESCRIPTION = "Display tags.";
    
    /**
     * Creates a new instance.
     * 
     * @param model Reference to the model. Mustn't be <code>null</code>.
     */
	public ClassifyAction(ImViewer model)
	{
		super(model, NAME);
		putValue(Action.SHORT_DESCRIPTION, 
                UIUtilities.formatToolTipText(DESCRIPTION));
        IconManager icons = IconManager.getInstance();
        putValue(Action.SMALL_ICON, icons.getIcon(IconManager.CATEGORY));
	}

	/** 
	 * Brings up the menu displaying categories.
	 * @see MouseListener#mousePressed(MouseEvent)
	 */
	public void mousePressed(MouseEvent me)
	{
		Object source = me.getSource();
		if (source instanceof Component)
			model.retrieveTags((Component) source, me.getPoint());
	}
	
	/**
	 * Brings up the menu displaying categories.
	 * @see MouseListener#mouseReleased(MouseEvent)
	 */
	public void mouseReleased(MouseEvent me)
	{
		Object source = me.getSource();
		if (source instanceof Component) 
			model.showMenu(ImViewer.CATEGORY_MENU, (Component) source, 
							me.getPoint());
	}
	
	/**
	 * Required by the {@link MouseListener} I/F but no-op implementation 
	 * in our case.
	 * @see MouseListener#mouseClicked(MouseEvent)
	 */
	public void mouseClicked(MouseEvent me) {}

	/**
	 * Required by the {@link MouseListener} I/F but no-op implementation 
	 * in our case.
	 * @see MouseListener#mouseEntered(MouseEvent)
	 */
	public void mouseEntered(MouseEvent me) {}

	/**
	 * Required by the {@link MouseListener} I/F but no-op implementation 
	 * in our case.
	 * @see MouseListener#mouseExited(MouseEvent)
	 */
	public void mouseExited(MouseEvent me) {}

	
	
}
