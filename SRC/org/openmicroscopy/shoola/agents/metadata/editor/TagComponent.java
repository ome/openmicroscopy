/*
 * org.openmicroscopy.shoola.agents.metadata.editor.TagComponent 
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
package org.openmicroscopy.shoola.agents.metadata.editor;

//Java imports
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.JLabel;

//Third-party libraries

//Application-internal dependencies
import pojos.AnnotationData;
import pojos.TagAnnotationData;

/** 
 * UI component hosting a tag annotation.
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
class TagComponent
	extends JLabel
	implements MouseListener
{

	/** The annotation hosted by this component. */
	private AnnotationData 	data;
	
	/** Reference to the view. */
	private TagsUI			uiDelegate;
	
	/** Builds and lays out the UI. */
	private void buildGUI()
	{
		TagAnnotationData tag = (TagAnnotationData) data;
		setText(tag.getTagValue());
		//setToolTipText(tag.getTagDescription());
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param uiDelegate	The ui delegate.
	 * @param data			The annotation to handle.
	 */
	TagComponent(TagsUI uiDelegate, AnnotationData data)
	{
		if (data == null)
			throw new IllegalArgumentException("No tag specified.");
		if (uiDelegate == null)
			throw new IllegalArgumentException("No view.");
		this.data = data;
		this.uiDelegate = uiDelegate;
		addMouseListener(this);
		buildGUI();
	}

	/**
	 * Edits the annotation if the user clicks twice on the annotation.
	 * Displays the menu if right click occurs.
	 * @see MouseListener#mousePressed(MouseEvent)
	 */
	public void mousePressed(MouseEvent e)
	{
		if (e.getClickCount() == 2) {
			uiDelegate.editAnnotation(data);
		} else uiDelegate.setSelectedTag(e.isShiftDown(), data);
		
		if (e.isPopupTrigger()) { 
			uiDelegate.showMenu(e.getPoint(), e.getComponent());
		}
	}

	/**
	 * Displays the menu if right click occurs.
	 * @see MouseListener#mouseReleased(MouseEvent)
	 */
	public void mouseReleased(MouseEvent e)
	{
		if (e.isPopupTrigger()) 
			uiDelegate.showMenu(e.getPoint(), e.getComponent());
	}
	
	/**
	 * Required by the {@link MouseListener} I/F but no-op implementation
	 * in our case.
	 * @see MouseListener#mouseClicked(MouseEvent)
	 */
	public void mouseClicked(MouseEvent e) {}

	/**
	 * Required by the {@link MouseListener} I/F but no-op implementation
	 * in our case.
	 * @see MouseListener#mouseEntered(MouseEvent)
	 */
	public void mouseEntered(MouseEvent e) {}

	/**
	 * Required by the {@link MouseListener} I/F but no-op implementation
	 * in our case.
	 * @see MouseListener#mouseExited(MouseEvent)
	 */
	public void mouseExited(MouseEvent e) {}

}
