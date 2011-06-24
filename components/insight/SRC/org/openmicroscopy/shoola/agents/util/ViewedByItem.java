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
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import javax.swing.AbstractButton;
import javax.swing.ImageIcon;
import javax.swing.JMenuItem;


//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.util.ui.RollOverThumbnailManager;
import org.openmicroscopy.shoola.env.rnd.RndProxyDef;
import org.openmicroscopy.shoola.util.image.geom.Factory;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
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

	/** The maximum width or height of the icon. */
	public static final int		MAX_ICON_SIZE = 32;
	
	/** Bound property indicating to apply the rendering settings. */
	public static final String VIEWED_BY_PROPERTY = "viewedBy";
	
	/** The experimenter who viewed the image. */
	private ExperimenterData experimenter;
	
	/** The rendering settings.  */
	private RndProxyDef 	 rndDef;

	/** The image with the rendering settings. */
	private BufferedImage	image;

	/** Flag indicating how to use the component. */
	private boolean			asMenuItem;
	
	/** Displays the image. */
	private void rollOver()
	{
		if (image == null) return;
		RollOverThumbnailManager.rollOverDisplay(image, 
				getBounds(), getLocationOnScreen(), getToolTipText());
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param experimenter The experimenter who viewed the image.
	 * @param rndDef 	   The rendering settings.
	 */
	public ViewedByItem(ExperimenterData experimenter, RndProxyDef rndDef)
	{
		this(experimenter, rndDef, true);
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param experimenter The experimenter who viewed the image.
	 * @param rndDef 	   The rendering settings.
	 * @param asMenuItem   Pass <code>true</code> true to indicate that 
	 * 					   the component will be used as a menu item, 
	 * 					   <code>false</code> otherwise.	
	 */
	public ViewedByItem(ExperimenterData experimenter, RndProxyDef rndDef, 
			boolean asMenuItem)
	{
		this.experimenter = experimenter;
		this.rndDef = rndDef;
		this.asMenuItem = asMenuItem;
		if (!asMenuItem) {
			Font f = getFont();
			setFont(f.deriveFont(f.getStyle(), f.getSize()-3));
			setVerticalTextPosition(AbstractButton.BOTTOM);
	    	setHorizontalTextPosition(AbstractButton.CENTER);
	    	setIconTextGap(0);
	    	setText(experimenter.getLastName());
		} else {
			Dimension d = new Dimension(MAX_ICON_SIZE+2, MAX_ICON_SIZE+2);
	    	setPreferredSize(d);
			setSize(d);
			setText(EditorUtil.formatExperimenter(experimenter));
		}
		List<String> l = new ArrayList<String>();
		l.add("Viewed by: "+getText());
		Timestamp time = rndDef.getLastModified();
		if (time != null) 
			l.add("Last modified: "+UIUtilities.formatShortDateTime(time));
		setToolTipText(UIUtilities.formatToolTipText(l));
		addActionListener(this);
	}
	
	/**
	 * Returns the experimenter the settings belong to.
	 * 
	 * @return See above.
	 */
	public ExperimenterData getExperimenter() { return experimenter; }
	
	/**
	 * Returns the rendering settings.
	 * 
	 * @return See above.
	 */
	public RndProxyDef getRndDef() { return rndDef; }
	
	/**
	 * Returns the identifier of the experimenter.
	 * 
	 * @return See above.
	 */
	public long getExperimenterID() { return experimenter.getId(); }
	
	/**
	 * Returns the image or <code>null</code>.
	 * 
	 * @return  See above.
	 */
	public BufferedImage getImage() { return image; }
	
	/** 
	 * Sets the image as viewed by the experimenter. 
	 * 
	 * @param image The value to set.
	 */
	public void setImage(BufferedImage image)
	{
		this.image = image;
		if (image == null) return;
		setIcon(new ImageIcon(
				Factory.scaleBufferedImage(image, MAX_ICON_SIZE)));
		
		revalidate();
		if (!asMenuItem) {
			addMouseListener(new MouseAdapter() {
				
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
				public void mouseEntered(MouseEvent e) { 
					rollOver(); 
				}
			
			});
		}
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
