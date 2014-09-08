/*
 * org.openmicroscopy.shoola.agents.imviewer.view.ViewerPreferences 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2014 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.agents.imviewer.view;


//Java imports
import java.awt.Color;
import java.awt.Rectangle;
import java.util.HashMap;
import java.util.Map;

//Third-party libraries

//Application-internal dependencies

/** 
 * Utility class used to store the user preferences for the viewer.
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
public class ViewerPreferences
{

	/** Field identifying the renderer. */
	public static final String RENDERER = "Renderer";
	
	/** Field identifying the window bounds. */
	public static final String WINDOWS_BOUNDS = "Viewer Bounds";
	
	/** Field identifying the history. */
	public static final String HISTORY = "History";
	
	/** Field identifying the zoom factor. */
	public static final String ZOOM_FACTOR = "Zoom factor";
	
	/** Field identifying the scale bar. */
	public static final String SCALE_BAR = "Scale bar settings";
	
	/** Field identifying the background color. */
	public static final String BG_COLOR = "Background color";
	
	/** Flag indicating if the renderer is turned on/off. */
	private boolean 				renderer;
	
	/** Flag indicating if the history is turned on/off. */
	private boolean 				history;
	
	/** The bounds of the viewer. */
	private Rectangle 				viewerBounds;
	
	/** The background color. */
	private Color					bgColor;

	/** The selected zoom index. */
	private int						zoomIndex;

	/** The index the scale bar. */
	private int						scaleBarIndex;
	
	/** The color the scale bar. */
	private Color					scaleBarColor;
	
	/** The fields to set. */
	private Map<String, Boolean> 	fields;
	
	/** Flag indicating if interpolation is used */
	private boolean interpolation = true;
	
	/** Creates a new instance. */
	public ViewerPreferences()
	{
		zoomIndex = -2;
		scaleBarIndex = -1;
		fields = new HashMap<String, Boolean>(5);
		fields.put(RENDERER, true);
		fields.put(HISTORY, true);
		fields.put(SCALE_BAR, true);
		fields.put(BG_COLOR, true);
		fields.put(ZOOM_FACTOR, true);
	}

	/**
	 * Sets the selected fields.
	 * 
	 * @param fields The fields to set.
	 */
	void setSelectedFields(Map<String, Boolean> fields)
	{
		if (fields == null)
			return;
		this.fields = fields;
	}

	/**
	 * Returns <code>true</code> if the history is visible,
	 * <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	public boolean isHistory() { return history; }
	
	/**
	 * Returns <code>true</code> if the renderer is visible,
	 * <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	public boolean isRenderer() { return renderer; }

	/**
	 * Sets the passed flag.
	 * 
	 * @param renderer Pass <code>true</code> to have the renderer visible,
	 * 				   <code>false</code> otherwise.
	 */
	public void setRenderer(boolean renderer)
	{ 
		if (isFieldSelected(RENDERER))
			this.renderer = renderer; 
	}

	/**
	 * Sets the passed flag.
	 * 
	 * @param history Pass <code>true</code> to have the history visible,
	 * 				   <code>false</code> otherwise.
	 */
	public void setHistory(boolean history)
	{ 
		if (isFieldSelected(HISTORY))
			this.history = history; 
	}
	
	/**
	 * Returns the bounds of the viewer.
	 * 
	 * @return See above.
	 */
	public Rectangle getViewerBounds() { return viewerBounds; }

	/**
	 * Sets the bounds of the viewer.
	 * 
	 * @param viewerBounds The valur to set.
	 */
	public void setViewerBounds(Rectangle viewerBounds)
	{
		this.viewerBounds = viewerBounds;
	}
	
	/** 
	 * Returns the selected zoom index.
	 * 
	 * @return See above.
	 */
	public int getZoomIndex() { return zoomIndex; }

	/**
	 * Sets the preferred zooming index.
	 * 
	 * @param zoomIndex The value to set.
	 */
	public void setZoomIndex(int zoomIndex)
	{ 
		if (isFieldSelected(ZOOM_FACTOR))
			this.zoomIndex = zoomIndex; 
	}
	
	/**
	 * Returns the background color.
	 * 
	 * @return See above.
	 */
	public Color getBackgroundColor() { return bgColor; }
	
	/**
	 * Sets the background color.
	 * 
	 * @param bgColor The color to set.
	 */
	public void setBackgroundColor(Color bgColor)
	{ 
		if (isFieldSelected(BG_COLOR))
			this.bgColor = bgColor; 
	}
	
	/**
	 * Sets the index of the scale bar.
	 * 
	 * @param index The value to set.
	 */
	public void setScaleBarIndex(int index)
	{ 
		if (isFieldSelected(SCALE_BAR))
			scaleBarIndex = index; 
	}
	
	/**
	 * Sets the color of the scale bar.
	 * 
	 * @param color The value to set.
	 */
	public void setScaleBarColor(Color color)
	{ 
		if (isFieldSelected(SCALE_BAR))
			scaleBarColor = color; 
	}
	
	/**
	 * Returns the index of the scale bar.
	 * 
	 * @return See above.
	 */
	public int getScaleBarIndex() { return scaleBarIndex; }
	
	/**
	 * Returns the color of the scale bar.
	 * 
	 * @return See above.
	 */
	public Color getScaleBarColor() { return scaleBarColor; }
	
	/**
	 * Returns <code>true</code> if field is selected,
	 * <code>false</code> otherwise.
	 * 
	 * @param field The field to handle
	 * @return See above.
	 */
	public boolean isFieldSelected(String field)
	{
		if (fields == null) return false;
		Boolean value = fields.get(field);
		if (value == null) return false;
		return value.booleanValue();
	}

        /**
         * Returns if interpolation is enabled or not
         * 
         * @return
         */
        boolean isInterpolation() {
            return interpolation;
        }
    
        /**
         * En-/Disables interpolation
         * 
         * @param interpolation
         */
        void setInterpolation(boolean interpolation) {
            this.interpolation = interpolation;
        }
	
}
