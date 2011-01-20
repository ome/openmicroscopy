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
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.sql.Timestamp;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.data.model.ProjectionParam;
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
	
	/** The view index. */
	private int					index;
	
	/** The projection ref.*/
	private ProjectionParam		ref;
	
	/** The time point used for the projection preview. */
	private int					defaultT;
	
	/** The original highlight color associated to the node. */
	private Color				originalColor;
	
	/**
	 * Creates a new instance.
	 * 
	 * @param settings 	Object constaining the rendering settings.
	 * @param thumbnail	The image rendered using the rendering settings.
	 * @param title		The title of the item.
	 */
	public HistoryItem(RndProxyDef settings, BufferedImage thumbnail, String
			title)
	{
		if (settings == null)
			throw new IllegalArgumentException("No rnd settings specified.");
		if (thumbnail == null)
			throw new IllegalArgumentException("No rendered image specified.");
		this.settings = settings;
		this.thumbnail = thumbnail;
		time = UIUtilities.getDefaultTimestamp();
		//noDecoration();
		if (title == null || title.trim().length() == 0)
			title = UIUtilities.formatShortDateTime(time);
		setToolTipText(UIUtilities.formatShortDateTime(time));
		setTitle(title);
		allowClose(true);
		setTitleBarType(SMALL_TITLE_BAR);
		setListenToBorder(false);
		canvas = new HistoryItemCanvas(this);
        getInternalDesktop().add(canvas);
        int w = thumbnail.getWidth();
        int h = thumbnail.getHeight();
        canvas.setBounds(0, 0, w, h);
        getInternalDesktop().setSize(w, h);
        getInternalDesktop().setPreferredSize(new Dimension(w, h));
        ref = null;
	}
	
	/**
	 * Sets the projection ref.
	 * 
	 * @param ref The object hosting the projection parameters.
	 */
	public void setProjectionRef(ProjectionParam ref)
	{
		this.ref = ref;
	}
	
	/**
	 * Sets the timepoint used for the projection preview.
	 * 
	 * @param defaultT The value to set.
	 */
	public void setDefaultT(int defaultT) { this.defaultT = defaultT; }
	
	/** 
	 * Returns the timepoint used for the projection preview.
	 * 
	 * @return See above.
	 */
	public int getDefaultT() { return defaultT; }
	
	/**
	 * Returns the object hosting the projection parameters.
	 * 
	 * @return See above.
	 */
	public ProjectionParam getProjectionRef() { return ref; }
	 
	/**
	 * Returns the view index.
	 * 
	 * @return See above.
	 */
	public int getIndex() { return index; }
	
	/**
	 * Sets the view index.
	 * 
	 * @param index The value to set.
	 */
	public void setIndex(int index) { this.index = index; }
	
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
	 * Sets the color.
	 * 
	 * @param originalColor The value to set.
	 */
	public void setOriginalColor(Color originalColor)
	{
		this.originalColor = originalColor;
	}
	
	/**
	 * Returns the original color.
	 * 
	 * @return See above.
	 */
	public Color getOriginalColor() { return originalColor; }
	
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
