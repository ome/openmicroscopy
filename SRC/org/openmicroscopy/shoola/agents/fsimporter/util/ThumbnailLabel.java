/*
 * org.openmicroscopy.shoola.agents.fsimporter.util.ThumbnailLabel 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2011 University of Dundee. All rights reserved.
 *
 *
 * 	This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */
package org.openmicroscopy.shoola.agents.fsimporter.util;


//Java imports
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.border.Border;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.events.iviewer.ViewImage;
import org.openmicroscopy.shoola.agents.fsimporter.ImporterAgent;
import org.openmicroscopy.shoola.env.data.model.ThumbnailData;
import org.openmicroscopy.shoola.env.event.EventBus;
import org.openmicroscopy.shoola.util.image.geom.Factory;

/** 
 * Component displaying the thumbnail.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
class ThumbnailLabel 
	extends JLabel
{

	/** The border of the thumbnail label. */
	private static final Border		LABEL_BORDER = 
							BorderFactory.createLineBorder(Color.black, 1);
	
	/** The text displayed in the tool tip when the image has been imported. */
	private static final String		IMAGE_LABEL_TOOLTIP = 
		"Click on thumbnail to launch the viewer.";
	
	/** The thumbnail to host. */
	private ThumbnailData data;
	
	/** Posts an event to view the image. */
	private void viewImage()
	{
		EventBus bus = ImporterAgent.getRegistry().getEventBus();
		ViewImage evt = new ViewImage(data.getImage(), null);
		bus.post(evt);
	}
	
	/**  
	 * Creates a new instance. 
	 * 
	 * @param icon The icon to display.
	 */
	ThumbnailLabel(Icon icon)
	{
		super(icon);
	}
	
	/** Creates a default new instance. */
	ThumbnailLabel() {}
	
	/** 
	 * Sets the thumbnail to view. 
	 * 
	 * @param data The value to set.
	 */
	void setThumbnail(ThumbnailData data)
	{
		if (data == null) return;
		this.data = data;
		ImageIcon icon = new ImageIcon(Factory.magnifyImage(0.25, 
				data.getThumbnail()));
		setToolTipText(IMAGE_LABEL_TOOLTIP);
		setIcon(icon);
		setBorder(LABEL_BORDER);
		if (icon != null)
			setPreferredSize(new Dimension(icon.getIconWidth(), 
					icon.getIconHeight()));
		
		addMouseListener(new MouseAdapter() {
			
			/**
			 * Views the image.
			 * @see ActionListener#actionPerformed(ActionEvent)
			 */
			public void mousePressed(MouseEvent e) { viewImage(); }
		});
	}
	
}
