/*
 * org.openmicroscopy.shoola.agents.metadata.viewedby.ViewedItem 
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
package org.openmicroscopy.shoola.agents.metadata.viewedby;


//Java imports
import java.awt.Dimension;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;


//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.metadata.IconManager;
import org.openmicroscopy.shoola.agents.metadata.MetadataViewerAgent;
import org.openmicroscopy.shoola.agents.metadata.ObjectTranslator;
import org.openmicroscopy.shoola.agents.metadata.util.ThumbnailView;
import org.openmicroscopy.shoola.util.ui.RatingComponent;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

import pojos.ExperimenterData;

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
class ViewedItem
	extends JPanel
{

	/** Reference to the experimenter who viewed the image. */
	private ExperimenterData 	experimenter;
	
	/** The rating object. */
	private Object				rating;
	
	/** The canvas hosting the thumbnail. */
	private ViewedItemCanvas	canvas;

	/** The rating component. */
	private RatingComponent		ratingComponent;
	
	/** Initializes the components composing the display. */
	private void initComponents()
	{
		canvas = new ViewedItemCanvas();
		canvas.setPreferredSize(new Dimension(ThumbnailView.THUMB_MAX_WIDTH, 
				ThumbnailView.THUMB_MAX_HEIGHT));
		int value = 0;
		boolean b = experimenter.getId() 
						== MetadataViewerAgent.getUserDetails().getId();
		ratingComponent = new RatingComponent(value, b);
	}
	
	/** Builds and lays out the component. */
	private void buildGUI()
	{
		UIUtilities.setBoldTitledBorder(
				ObjectTranslator.formatExperimenter(experimenter), this);
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		add(canvas);
		add(ratingComponent);
	}
	
	/**
	 * Creates a new instance. 
	 * 
	 * @param experimenter	The experimenter hosted by this node.
	 * @param rating		The rating object.
	 */
	ViewedItem(ExperimenterData	experimenter, Object rating)
	{
		if (experimenter == null)
			throw new IllegalArgumentException("No experimenter.");
		this.experimenter = experimenter;
		this.rating = rating;
		initComponents();
		buildGUI();
	}
	
	/**
	 * Sets and paint the image.
	 * 
	 * @param image The value to set.
	 */
	void setThumbnail(BufferedImage image)
	{
		if (image == null) return;
		canvas.setImage(image);
	}
	
}
