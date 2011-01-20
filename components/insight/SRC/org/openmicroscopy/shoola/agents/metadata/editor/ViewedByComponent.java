/*
 * org.openmicroscopy.shoola.agents.metadata.editor.ViewedByComponent 
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
import java.awt.FlowLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import javax.swing.JLabel;
import javax.swing.JPanel;


//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.util.ui.RollOverThumbnailManager;
import org.openmicroscopy.shoola.env.data.util.ViewedByDef;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

/** 
 * Object hosting the rendering settings set by an user for the 
 * currently edited image.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta3
 */
class ViewedByComponent 
	extends JPanel
{

	/** 
	 * The object hosting details about user who viewed the image and
	 * the rendering setting set.
	 */
	private ViewedByDef 	def;
	
	/** The thumbnail related to that node. */
	private BufferedImage 	thumbnail;
	
	/** Reference to the model. */
	private EditorModel		model;
	
	/** The label hosting the name. */
	private JLabel			nameLabel;
	
	/** Display thumbnail. */
	private void rollOver()
	{
		if (thumbnail == null) return;
		RollOverThumbnailManager.rollOverDisplay(thumbnail, 
				nameLabel.getBounds(), nameLabel.getLocationOnScreen(), "");
	}
	
	/** Initialises the component. */
	private void initialise()
	{
		setBorder(null);
		setBackground(UIUtilities.BACKGROUND_COLOR);
		nameLabel = new JLabel();
		nameLabel.setBackground(UIUtilities.BACKGROUND_COLOR);
		nameLabel.setForeground(UIUtilities.DEFAULT_FONT_COLOR);
		nameLabel.setToolTipText("Click on name to launch a Viewer.");
		nameLabel.setText(def.toString());
		nameLabel.addMouseListener(new MouseAdapter() {
		
			/**
			 * Views the image.
			 * @see MouseAdapter#mouseExited(MouseEvent)
			 */
			public void mouseReleased(MouseEvent e) {
				model.viewImage(def);
			}
		
			/**
			 * Removes the zooming window from the display.
			 * @see MouseAdapter#mouseExited(MouseEvent)
			 */
			public void mouseExited(MouseEvent e)
			{
				RollOverThumbnailManager.stopOverDisplay();
			}
		
			/**
			 * Zooms the image.
			 * @see MouseAdapter#mouseExited(MouseEvent)
			 */
			public void mouseEntered(MouseEvent e) { rollOver(); }
		
		});
		setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
		add(nameLabel);
	}
	
	/**
	 * Creates a new instance,
	 * 
	 * @param def	Object hosting user and rendering details. 
	 * 				Mustn't be <code>null</code>.
	 * @param model Reference to the model. Mustn't be <code>null</code>.
	 */
	ViewedByComponent(ViewedByDef def, EditorModel model)
	{
		if (def == null)
			throw new IllegalArgumentException("No definition object.");
		if (model == null)
			throw new IllegalArgumentException("No Model.");
		this.model = model;
		this.def = def;
		initialise();
	}

	/**
	 * Sets the thumbnail related to that image.
	 * 
	 * @param thumbnail The value to set.
	 */
	void setThumbnail(BufferedImage thumbnail) { this.thumbnail = thumbnail; }
	
}
