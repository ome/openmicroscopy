/*
 * org.openmicroscopy.shoola.agents.imviewer.util.HistoryItem 
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
package org.openmicroscopy.shoola.agents.imviewer.util;

//Java imports
import java.awt.Dimension;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.sql.Timestamp;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.rnd.RndProxyDef;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import org.openmicroscopy.shoola.util.ui.tpane.TinyPane;

/** 
 * Element storing information used to render an image at a particular 
 * timepoint.
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
public class HistoryItem
	extends TinyPane
{

	/** The rendering settings to store. */
	private RndProxyDef 		settings;
	
	/** The image rendered using the stored settings. */
	private BufferedImage		thumbnail;
	
	/** The time at which the element is created. */
	private Timestamp			time;
	
	/** Canvas hosting the image. */
	private HistoryItemCanvas	canvas;
	
	/**
	 * Creates a new instance.
	 * 
	 * @param settings 	Object constaining the rendering settings.
	 * @param thumbnail	The image rendered using the rendering settings.
	 * @param reverse	Pass <code>true</code> to indicate to flip the image
	 * 					along the X-axis, <code>false</code> otherwise.
	 */
	public HistoryItem(RndProxyDef settings, BufferedImage thumbnail,
						boolean reverse)
	{
		if (settings == null)
			throw new IllegalArgumentException("No rnd settings specified.");
		if (thumbnail == null)
			throw new IllegalArgumentException("No rendered image specified.");
		this.settings = settings;
		this.thumbnail = thumbnail;
		time = UIUtilities.getDefaultTimestamp();
		//noDecoration();
		String title = UIUtilities.formatShortDateTime(time);;
		setToolTipText(title);
		/*
		String[] elements = title.split(" ");
		String f = "";
		int l = elements.length;
		for (int i = 0; i < l; i++) {
			if (i != 2) {
				f += elements[i];
				if (i < l-1) f += " ";
			}
		}
		setTitle(f);
		*/
		setTitle(title);
		allowClose(true);
		setTitleBarType(SMALL_TITLE_BAR);
		setListenToBorder(false);
		canvas = new HistoryItemCanvas(this, reverse);
        getInternalDesktop().add(canvas);
        int w = thumbnail.getWidth();
        int h = thumbnail.getHeight();
        canvas.setBounds(0, 0, w, h);
        getInternalDesktop().setSize(w, h);
        getInternalDesktop().setPreferredSize(new Dimension(w, h));
	}
	
	/**
	 * Returns the rendering settings used to render the stored image.
	 * 
	 * @return See above.
	 */
	public RndProxyDef getRndSettings() { return settings; }
	
	/**
	 * Returns the image rendered using the stored settings.
	 * 
	 * @return See above.
	 */
	public BufferedImage getThumbnail() { return thumbnail; }
	
	/**
     * Adds a {@link MouseListener} to the components composing the 
     * node.
     * 
     * @param listener The listener to add.
     */
    public void addMouseListenerToComponents(MouseListener listener)
    {
    	 getTitleBar().addMouseListener(listener);
    	 addMouseListener(listener);
    	 canvas.addMouseListener(listener);
    }
    
}
