/*
 * org.openmicroscopy.shoola.agents.metadata.viewedby.ViewedItemCanvas 
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
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import javax.swing.JPanel;



//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.events.iviewer.ViewImage;
import org.openmicroscopy.shoola.agents.metadata.MetadataViewerAgent;
import org.openmicroscopy.shoola.env.data.util.ViewedByDef;
import org.openmicroscopy.shoola.env.event.EventBus;

import pojos.ImageData;

/** 
 * Customizes <code>JPanel</code> to paint the thumbnail.
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
class ThumbnailCanvas 
	extends JPanel
{

	/** The image to paint. */
	private BufferedImage 	image;
	
	/** The object storing the user's details and related settings. */
	private ViewedByDef 	def;
	
	/** Reference to the model. */
	private EditorModel		model;
	
	/** Posts an event to view the image. */
	private void viewImage()
	{
		ViewImage evt;
		if (def == null) {
			ImageData img = (ImageData) model.getRefObject();
			evt = new ViewImage(img, null);
		} else {
			ImageData img = (ImageData) model.getRefObject();
			evt = new ViewImage(img, null);
			evt.setSettings(def.getRndSettings(), 
							def.getExperimenter().getId());
		}
		
		EventBus bus = MetadataViewerAgent.getRegistry().getEventBus();
		bus.post(evt);
	}
	
	/** 
	 * Creates a new instance. 
	 * 
	 * @param model	Reference to the model. Mustn't be <code>null</code>.
	 * @param image The value to set. Mustn't be <code>null</code>.
	 */
	ThumbnailCanvas(EditorModel model, BufferedImage image)
    {
		this(model, image, null);
    }
	
	/** 
	 * Creates a new instance. 
	 * 
	 * @param model	Reference to the model. Mustn't be <code>null</code>.
	 * @param image The value to set. Mustn't be <code>null</code>.
	 * @param def	The object storing user's details and related settings.
	 */
	ThumbnailCanvas(EditorModel model, BufferedImage image, ViewedByDef def)
    {
		if (model == null)
			throw new IllegalArgumentException("No viewing details.");
		if (image == null)
			throw new IllegalArgumentException("No image. ");
		this.image = image;
		this.def = def;
		this.model = model;
        setDoubleBuffered(true);
        setPreferredSize(new Dimension(image.getWidth(), image.getHeight()));
        addMouseListener(new MouseAdapter() {

			public void mouseReleased(MouseEvent e) {
				if (e.getClickCount() == 2)
					viewImage();
			}
		
		});
    }
	
    /** 
     * Overridden to paint the image. 
     * @see javax.swing.JComponent#paintComponent(Graphics)
     */
    public void paintComponent(Graphics g)
    {
        super.paintComponent(g);
        if (image == null) return;
        ((Graphics2D) g).drawImage(image, null, 0, 0);
    }
    
}
