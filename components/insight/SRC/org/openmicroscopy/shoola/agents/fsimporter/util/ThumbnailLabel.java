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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.border.Border;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.events.iviewer.ViewImage;
import org.openmicroscopy.shoola.agents.events.iviewer.ViewImageObject;
import org.openmicroscopy.shoola.agents.fsimporter.IconManager;
import org.openmicroscopy.shoola.agents.fsimporter.ImporterAgent;
import org.openmicroscopy.shoola.agents.util.ui.RollOverThumbnailManager;
import org.openmicroscopy.shoola.env.data.model.ThumbnailData;
import org.openmicroscopy.shoola.env.event.EventBus;
import org.openmicroscopy.shoola.util.image.geom.Factory;
import pojos.ImageData;
import pojos.PlateData;

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

	/** Bound property indicating to browse the specified plate. */
	static final String BROWSE_PLATE_PROPERTY = "browsePlate";
	
	/** The border of the thumbnail label. */
	private static final Border	LABEL_BORDER = 
							BorderFactory.createLineBorder(Color.black, 1);
	
	/** The text displayed in the tool tip when the image has been imported. */
	static final String	IMAGE_LABEL_TOOLTIP = "Click to view the image.";
	
	/** The text displayed in the tool tip when the plate has been imported. */
	static final String	PLATE_LABEL_TOOLTIP = "Click to browse the plate.";
	
	/** The thumbnail or the image to host. */
	private Object data;
	
	/** Posts an event to view the object. */
	private void view()
	{
		EventBus bus = ImporterAgent.getRegistry().getEventBus();
		if (data instanceof ThumbnailData) {
			ThumbnailData thumbnail = (ThumbnailData) data;
			if (thumbnail.getImage() != null)
				bus.post(new ViewImage(new ViewImageObject(
						thumbnail.getImage()), null));
		} else if (data instanceof ImageData) {
			ImageData image = (ImageData) data;
			bus.post(new ViewImage(new ViewImageObject(image), null));
		} else if (data instanceof PlateData) {
			firePropertyChange(BROWSE_PLATE_PROPERTY, null, data);
		}
	}
	
	/** Rolls over the node. */
	private void rollOver()
	{
		if (data instanceof ThumbnailData) {
			ThumbnailData thumbnail = (ThumbnailData) data;
			RollOverThumbnailManager.rollOverDisplay(thumbnail.getThumbnail(), 
		   			 getBounds(), getLocationOnScreen(), toString());
		} 
	}
	

	/** 
	 * Sets the thumbnail to view. 
	 * 
	 * @param data The value to set.
	 */
	private void setThumbnail(ThumbnailData data)
	{
		if (data == null) return;
		BufferedImage img  = Factory.magnifyImage(0.25, data.getThumbnail());
		ImageIcon icon = null;
		if (img != null) icon = new ImageIcon(img);
		this.data = data;
		setToolTipText(IMAGE_LABEL_TOOLTIP);
		setBorder(LABEL_BORDER);
		if (icon != null) {
			setIcon(icon);
		}
		addMouseListener(new MouseAdapter() {
			
			/**
			 * Views the image.
			 * @see MouseListener#mousePressed(MouseEvent)
			 */
			public void mousePressed(MouseEvent e)
			{
				if (e.getClickCount() == 1)
					view(); 
			}

			/**
			 * Removes the zooming window from the display.
			 * @see MouseListener#mouseExited(MouseEvent)
			 */
			public void mouseExited(MouseEvent e)
			{
				RollOverThumbnailManager.stopOverDisplay();
			}
			
			/**
			 * Zooms the thumbnail.
			 * @see MouseListener#mouseEntered(MouseEvent)
			 */
			public void mouseEntered(MouseEvent e) { rollOver(); }
		});
	}
	
	/** Creates a default new instance. */
	ThumbnailLabel() {}
	
	/**  
	 * Creates a new instance. 
	 * 
	 * @param icon The icon to display.
	 */
	ThumbnailLabel(Icon icon)
	{
		super(icon);
	}
	
	/** 
	 * Sets the object that has been imported. 
	 * 
	 * @param data The imported image.
	 */
	void setData(Object data)
	{
		if (data == null) return;
		this.data = data;
		if (data instanceof ImageData) {
			setToolTipText(IMAGE_LABEL_TOOLTIP);
		} else if (data instanceof PlateData) {
			setToolTipText(PLATE_LABEL_TOOLTIP);
			IconManager icons = IconManager.getInstance();
			setIcon(icons.getIcon(IconManager.PLATE));
		} else if (data instanceof ThumbnailData) {
			setThumbnail((ThumbnailData) data);
			return;
		}
		addMouseListener(new MouseAdapter() {
			
			/**
			 * Views the image.
			 * @see MouseListener#mousePressed(MouseEvent)
			 */
			public void mousePressed(MouseEvent e)
			{
				if (e.getClickCount() == 1)
					view(); 
			}
		});
	}
	
}
