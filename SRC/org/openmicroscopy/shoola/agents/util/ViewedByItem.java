/*
 * org.openmicroscopy.shoola.agents.util.ViewedByItem 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2010 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.agents.util;



//Java imports
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import javax.swing.ImageIcon;
import javax.swing.JMenuItem;


//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.util.ui.RollOverThumbnailManager;
import org.openmicroscopy.shoola.env.rnd.RndProxyDef;
import org.openmicroscopy.shoola.util.image.geom.Factory;
import pojos.ExperimenterData;

/** 
 * Displays the name of the experimenter who viewed the image
 * and how he/she viewed it.
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
public class ViewedByItem
	extends JMenuItem
	implements ActionListener
{

	/** Bound property indicating to apply the rendering settings. */
	public static final String VIEWED_BY_PROPERTY = "viewedBy";
	
	/** The experimenter who viewed the image. */
	private ExperimenterData experimenter;
	
	/** The rendering settings.  */
	private RndProxyDef 	 rndDef;

	/** The image with the rendering settings. */
	private BufferedImage	image;

	/** Displays the image. */
	private void rollOver()
	{
		if (image == null) return;
		RollOverThumbnailManager.rollOverDisplay(image, 
				getBounds(), getLocationOnScreen(), "");
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param experimenter The experimenter who viewed the image.
	 * @param rndDef 	   The rendering settings.
	 * @param model		   Reference to the model.	
	 */
	public ViewedByItem(ExperimenterData experimenter, RndProxyDef rndDef)
	{
		this.experimenter = experimenter;
		this.rndDef = rndDef;
		setText(EditorUtil.formatExperimenter(experimenter));
		addActionListener(this);
	}
	
	/**
	 * Returns the identifier of the experimenter.
	 * 
	 * @return See above.
	 */
	public long getExperimenterID() { return experimenter.getId(); }
	
	/** 
	 * Sets the image as viewed by the experimenter. 
	 * 
	 * @param image The value to set.
	 */
	public void setImage(BufferedImage image)
	{
		this.image = image;
		if (image == null) return;
		setIcon(new ImageIcon(Factory.scaleBufferedImage(image, 24)));
		addMouseListener(new MouseAdapter() {
			
			/**
			 * Removes the zooming window from the display.
			 * @see MouseAdapter#mouseExited(MouseEvent)
			 */
			public void mouseExited(MouseEvent e)
			{
				//RollOverThumbnailManager.stopOverDisplay();
			}
		
			/**
			 * Zooms the image.
			 * @see MouseAdapter#mouseExited(MouseEvent)
			 */
			public void mouseEntered(MouseEvent e) { 
				//rollOver(); 
			}
		
		});
		repaint();
	}

	/**
	 * Selects the rendering settings.
	 * @see ActionListener#actionPerformed(ActionEvent)
	 */
	public void actionPerformed(ActionEvent e)
	{
		firePropertyChange(VIEWED_BY_PROPERTY, null, rndDef);
	}
	
}
