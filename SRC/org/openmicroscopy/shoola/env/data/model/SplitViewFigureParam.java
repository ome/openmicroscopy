/*
 * org.openmicroscopy.shoola.env.data.model.SplitViewFigureParam 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2009 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.env.data.model;

//Java imports
import java.awt.Color;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

//Third-party libraries

//Application-internal dependencies

/** 
 * Hosts the parameters needed for the creation of a split view figure.
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
public class SplitViewFigureParam
{

    /** Indicates to use the image's name as the name a row. */
	public static final int		IMAGE_NAME = 0;
    
    /** Indicates to use the dataset's name as the name a row. */
	public static final int		DATASET_NAME = 1;
    
    /** Indicates to use the tag's name as the name a row. */
	public static final int		TAG_NAME = 2;
    
	/** Identify the <code>JPEG</code> format. */
	public static final int		JPEG = 0;
	
	/** Identify the <code>PNG</code> format. */
	public static final int		PNG = 1;

	/** The supported formats. */
	public static final Map<Integer, String> 	FORMATS;

	static {
		FORMATS = new LinkedHashMap<Integer, String>(2);
		FORMATS.put(JPEG, "JPEG");
		FORMATS.put(PNG, "PNG");
	}
	
	/** One of the format constants defined by this class. */
	private int 	format;
	
	/** One of the projection constants defined by this class. */
	private int 	projectionType;
	
	/** The lower bound of the projection range. */
	private int 	zStart;
	
	/** The lower bound of the projection range. */
	private int 	zEnd;
	
	/** The stepping used while projecting. Default is <code>1</code>. */
	private int		stepping;
	
	/** The name of the file. */
	private String  name;
	
	/** The scale bar if displayed. */
	private int 	scaleBar;
	
	/** The color of the scale bar. */
	private int		color;
	
	/** The width of an image. */
	private int		width;
	
	/** The height of an image. */
	private int		height;
	
	/** 
	 * Flag indicating to either have the images in the split view
	 * as greyScale or color.
	 */
	private boolean	splitGrey;
	
	/** 
	 * The label associated to a row. 
	 * One of the constants defined by this class.
	 */
	private int		label;
	
	/** Channels composing the split view. */
	private Map<Integer, String> splitChannels;
	
	/** Channels composing the merge image. */
	private Map<Integer, Integer> mergeChannels;
	
	/** Sets the default value. */
	private void setDefault()
	{
		label = IMAGE_NAME;
		format = JPEG;
		projectionType = ProjectionParam.MAXIMUM_INTENSITY;
		stepping = 1;
		scaleBar = -1;
		height = -1;
		width = -1;
		zStart = 0;
		zEnd = 0;
		splitGrey = false;
	}
	
	/**
	 * Checks the format.
	 * 
	 * @param format The value to set.
	 */
	private void checkFormat(int format)
	{
		switch (format) {
			case JPEG:
			case PNG:
				this.format = format;
				break;
			default:
				this.format = JPEG;
		}
	}
	
	/**
	 * Sets the type of label. One of the constants defined by this class.
	 * 
	 * @param label The value to set.
	 */
	private void checkLabel(int label)
	{
		switch (format) {
			case IMAGE_NAME:
			case DATASET_NAME:
			case TAG_NAME:
				this.label = label;
				break;
			default:
				this.label = IMAGE_NAME;
		}
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param format The format of the image. One of the constants defined by
	 * 				 this class.
	 * @param name   The name of the image. 
	 * @param splitChannels The channels composing the split view.
	 * @param channels The channels composing the merge image.
	 * @param label  One of the constants defined by this class.
	 */
	public SplitViewFigureParam(int format, String name, Map<Integer, String>
		splitChannels, Map<Integer, Color> channels, int label)
	{
		setDefault();
		if (channels == null)
			throw new IllegalArgumentException("No channels");
		if (splitChannels == null)
			throw new IllegalArgumentException("No splitChannels.");
		this.splitChannels = splitChannels;
		checkFormat(format);
		checkLabel(label);
		this.name = name;
		Iterator<Integer> i = channels.keySet().iterator();
		Color c;
		Integer index;
		mergeChannels = new LinkedHashMap<Integer, Integer>(channels.size());
		int value;
		while (i.hasNext()) {
			index = i.next();
			c = (Color) channels.get(index);
			value = c.getRGB() & 0x00ffffff;
			System.err.println(new Color(value));
			mergeChannels.put(index, value);
		}
	}
	
	/**
	 * Returns the channels composing the merge image.
	 * 
	 * @return See above.
	 */
	public Map<Integer, Integer> getMergeChannels() { return mergeChannels; }
	
	/**
	 * Returns the channels composing the split image.
	 * 
	 * @return See above.
	 */
	public Map<Integer, String> getSplitChannels() { return splitChannels; }
	
	
	/**
	 * Sets the width of an image composing the display.
	 * 
	 * @param width The value to set.
	 */
	public void setWidth(int width) { this.width = width; }
	
	/**
	 * Sets the height of an image composing the display.
	 * 
	 * @param height The value to set.
	 */
	public void setHeight(int height) { this.height = height; }
	
	/**
	 * Sets the length of the scale bar.
	 * 
	 * @param scaleBar The value to set.
	 */
	public void setScaleBar(int scaleBar) { this.scaleBar = scaleBar; }
	
	/**
	 * Sets the projection.
	 * 
	 * @param type  The value to set.
	 */
	public void setProjectionType(int type)
	{
		ProjectionParam.checkProjectionAlgorithm(type);
		projectionType = type;
	}
	
	/**
	 * Passes <code>true</code> to display each image composing the split
	 * as greyScale, <code>false</code> to display as color.
	 * 
	 * @param splitGrey The value to set.
	 */
	public void setSplitGrey(boolean splitGrey) { this.splitGrey = splitGrey; }
	
	/**
	 * Returns <code>true</code> if the images composing the split view
	 * are greyScale, <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	public boolean isSplitGrey() { return splitGrey; }
	
	/**
	 * Sets the first z-section to project. 
	 * 
	 * @param zStart The value to set.
	 */
	public void setZStart(int zStart) { this.zStart = zStart; }
	
	/**
	 * Sets the last z-section to project. 
	 * 
	 * @param zEnd The value to set.
	 */
	public void setZEnd(int zEnd) { this.zEnd = zEnd; }
	
	/**
	 * Sets the projection stepping.
	 * 
	 * @param stepping The value to set.
	 */
	public void setStepping(int stepping)
	{
		if (stepping < 1) stepping = 1;
		this.stepping = stepping;
	}
	
 	/**
	 * Returns the width of an image.
	 * 
	 * @return See above.
	 */
	public int getWidth() { return width; }
	
	/**
	 * Returns the height of an image.
	 * 
	 * @return See above.
	 */
	public int getHeight() { return height; }
	
	/**
	 * Returns the name of the file.
	 * 
	 * @return See above.
	 */
	public String getName() { return name; }
	
	/**
	 * Returns the scale Bar.
	 * 
	 * @return See above.
	 */
	public int getScaleBar() { return scaleBar; }
	
	/**
	 * Returns <code>true</code> if the scale bar is visible, <code>false</code>
	 * otherwise.
	 * 
	 * @return See above.
	 */
	public boolean isScaleBarVisible() { return scaleBar <= 0; }
	
	/**
	 * Returns the lower bound of the z-section interval.
	 * 
	 * @return See above.
	 */
	public int getStartZ() { return zStart; }
	
	/**
	 * Returns the upper bound of the z-section interval.
	 * 
	 * @return See above.
	 */
	public int getEndZ() { return zEnd; }
	
	/**
	 * Returns the format. One of the constants defined by this class.
	 * 
	 * @return See above.
	 */
	public int getFormat() { return format; }
	
	/**
	 * Sets the color of the scale bar.
	 * 
	 * @param c The value to set.
	 */
	public void setColor(Color c)
	{ 
		if (c == null) return;
		this.color = c.getRGB() & 0x00ffffff;
	}
	
	/**
	 * Returns the color of the scale bar.
	 * 
	 * @return See above.
	 */
	public int getColor() { return color; }
	
	/**
	 * Returns the projection's algorithm. 
	 * 
	 * @return See above.
	 */
	public int getProjectionType() { return projectionType; }
	
	/**
	 * Returns the gap between each step. Default is <code>1</code>.
	 * 
	 * @return See above.
	 */
	public int getStepping() { return stepping; }
	
	/**
	 * Returns the label as a string.
	 * 
	 * @return See above.
	 */
	public String getLabelAsString()
	{
		switch (label) {
			default:
			case IMAGE_NAME: return "IMAGENAME";
			case DATASET_NAME: return "DATASETS";
			case TAG_NAME: return "TAGS";
		}
	}
	
	/**
	 * Returns the format as a string.
	 * 
	 * @return See above.
	 */
	public String getFormatAsString()
	{
		switch (format) {
			default:
			case JPEG: return "image/jpeg";
			case PNG: return "image/png";
		}
	}
	
	/**
	 * Returns the projection type as a string.
	 * 
	 * @return See above.
	 */
	public String getProjectionTypeAsString()
	{
		if (projectionType == ProjectionParam.MAXIMUM_INTENSITY)
			return "MAXIMUMINTENSITY";
		else if (projectionType == ProjectionParam.MEAN_INTENSITY)
			return "MEANINTENSITY";
		return "";
	}
	
}
