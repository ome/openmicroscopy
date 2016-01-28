/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2009 University of Dundee. All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
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

import java.awt.Color;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;



/** 
 * Hosts the parameters needed for the creation of a figure, either a 
 * split figure or a ROI split figure.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * @since 3.0-Beta4
 */
public class FigureParam
{

	/** Identifies the <code>ROI Figure</code> script. */
	public static final String ROI_SCRIPT = 
		ScriptObject.FIGURE_PATH+"ROI_Split_Figure.py";
	
	/** Identifies the <code>Thumbnail Figure</code> script. */
	public static final String THUMBNAIL_SCRIPT = 
		ScriptObject.FIGURE_PATH+"Thumbnail_Figure.py";
	
	/** Identifies the <code>Movie Figure</code> script. */
	public static final String MOVIE_SCRIPT = 
		ScriptObject.FIGURE_PATH+"Movie_Figure.py";
	
	/** Identifies the <code>Split View Figure</code> script. */
	public static final String SPLIT_VIEW_SCRIPT = 
		ScriptObject.FIGURE_PATH+"Split_View_Figure.py";
	
	/** The default text for the merged image. */
	public static final String	MERGED_TEXT = "Merged";
	
	/** Indicates to create a split view figure. */
	public static final int		SPLIT_VIEW = 0;
	
	/** Indicates to create a ROI split view figure. */
	public static final int		SPLIT_VIEW_ROI = 1;
	
	/** Indicates to create a Thumbnails figure. */
	public static final int		THUMBNAILS = 2;
	
	/** Indicates to create a Movie figure. */
	public static final int		MOVIE = 3;
	
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

	/** Identify the <code>TIFF</code> format. */
	public static final int TIFF = 2;
	
	/** Identify the default format. */
	public static final int DEFAULT_FORMAT = JPEG;
	
	/** Identify the <code>Seconds</code> format. */
	public static final int		TIME_SECS = 0; 
	
	/** Identify the <code>Minutes</code> format. */
	public static final int		TIME_MINS = 1; 
	
	/** Identify the <code>Hours</code> format. */
	public static final int		TIME_HOURS = 2; 
	
	/** Identify the <code>Minutes and Seconds</code> format. */
	public static final int		TIME_MINS_SECS = 3; 
	
	/** Identify the <code>Hours and minutes</code> format. */
	public static final int		TIME_HOURS_MINS = 4; 
	
	/** The supported formats. */
	public static final Map<Integer, String> 	FORMATS;

	/** The supported time formats. */
	public static final Map<Integer, String>	TIMES;
	
	static {
		FORMATS = new LinkedHashMap<Integer, String>(3);
		FORMATS.put(JPEG, "JPEG");
		FORMATS.put(PNG, "PNG");
		FORMATS.put(TIFF, "TIFF");
		TIMES = new LinkedHashMap<Integer, String>(6);
		TIMES.put(TIME_SECS, "Seconds (e.g. 2)");
		TIMES.put(TIME_MINS, "Minutes");
		TIMES.put(TIME_HOURS, "Hours");
		TIMES.put(TIME_MINS_SECS, "Minutes and Seconds");
		TIMES.put(TIME_HOURS_MINS, "Hours and Minutes");
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
	
	/** The color of the overlay. */
	private String	color;
	
	/** The width of an image. */
	private int		width;
	
	/** The height of an image. */
	private int		height;
	
	/** 
	 * Flag indicating to either have the images in the split view
	 * as greyScale or color.
	 */
	private boolean					splitGrey;
	
	/** 
	 * The label associated to a row. 
	 * One of the constants defined by this class.
	 */
	private int						label;
	
	/** Channels composing the split view. */
	private Map<Integer, String> 	splitChannels;
	
	/** Channels composing the merge image. */
	private Map<Integer, Integer> 	mergeChannels;
	
	/** Collection of active channels for the split view. */
	private List<Integer>			splitActive;
	
	/** Collection of tags to sort the thumbnails by. */
	private List<Long> 				tags;
	
	/** The type of figure. */
	private int 					index;
	
	/** The magnification used the ROI figure. */
	private double					magnificationFactor;
	
	/** The selected time points. */
	private List<Integer>			timepoints;
	
	/** 
	 * Set to <code>true</code> to indicate that the selected objects will
	 * compose the figure, <code>false</code> to indicate that the displayed
	 * objects will compose the figure.
	 */
	private boolean 				selectedObjects;
	
	/** Identifies the time selected. */
	private int						time;
	
	/** 
	 * Flag indicating to display the name of the channels or the default
	 * text. 
	 */ 
	private boolean 				mergedLabel;
	
	/** 
	 * Flag indicates to include the images w/o tags if 
	 * set to <code>true</code>, <code>false</code> otherwise.
	 */
	private boolean 				includeUntagged;
	
	/** The data object the figure is attached to. */
	private omero.gateway.model.DataObject 		anchor;
	
	/** The maximum number of images per columns.*/
	private int maxPerColumn;
	
	/** Sets the default value. */
	private void setDefault()
	{
		time = TIME_SECS;
		label = IMAGE_NAME;
		format = DEFAULT_FORMAT;
		projectionType = ProjectionParam.MAXIMUM_INTENSITY;
		stepping = 1;
		scaleBar = 1;
		height = -1;
		width = -1;
		zStart = 0;
		zEnd = 0;
		splitGrey = false;
		index = SPLIT_VIEW;
		selectedObjects = true;
		tags = null;
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
			case TIFF:
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
		switch (label) {
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
	 * @param label  One of the constants defined by this class.
	 */
	public FigureParam(int format, String name, int label)
	{
		this(format, name, new HashMap<Integer, String>(),
				new HashMap<Integer, Color>(), label);
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param format The format of the image. One of the constants defined by
	 * 				 this class.
	 * @param name   The name of the image. 
	 */
	public FigureParam(int format, String name)
	{
		this(format, name, new HashMap<Integer, String>(),
				new HashMap<Integer, Color>(), IMAGE_NAME);
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
	public FigureParam(int format, String name, Map<Integer, String>
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
		Color c;
		mergeChannels = new LinkedHashMap<Integer, Integer>(channels.size());
		int value;
		Entry<Integer, Color> entry;
		Iterator<Entry<Integer, Color>> i = channels.entrySet().iterator();
		while (i.hasNext()) {
			entry = i.next();
			c = entry.getValue();
			value = ((c.getAlpha() & 0xFF) << 24) |
            	((c.getRed() & 0xFF) << 16) |
            ((c.getGreen() & 0xFF) << 8) |
            ((c.getBlue() & 0xFF) << 0);
			mergeChannels.put(entry.getKey(), value);
		}
	}
	
	/**
	 * Sets the collection of channels active in the split view.
	 * 
	 * @param splitActive The collection to set.
	 */
	public void setSplitActive(List<Integer> splitActive)
	{
		this.splitActive = splitActive;
	}
	
	/**
	 * Returns the collection of active channels.
	 * 
	 * @return See above.
	 */
	public List<Integer> getSplitActive() { return splitActive; }
	
	/**
	 * Sets to <code>true</code> if the names of the channels are merged 
	 * and displayed next to the image, to <code>false</code> to display
	 * the default name.
	 * 
	 * @param mergedLabel The value to set.
	 */
	public void setMergedLabel(boolean mergedLabel)
	{
		this.mergedLabel = mergedLabel;
	}
	
	/**
	 * Returns <code>true</code> if the names of the channels are merged 
	 * and displayed next to the image, <code>false</code> to display
	 * the default name.
	 * 
	 * @return See above.
	 */
	public boolean getMergedLabel() { return mergedLabel; }
	
	/**
	 * Sets the time.
	 * 
	 * @param time The value to set.
	 */
	public void setTime(int time)
	{
		switch (time) {
			case TIME_SECS:
			case TIME_MINS:
			case TIME_HOURS:
			case TIME_MINS_SECS:
			case TIME_HOURS_MINS:
				this.time = time;
				break;
			default:
				this.time = TIME_SECS;
		}
	}
	
	/**
	 * Returns the time as a string.
	 * 
	 * @return See above.
	 */
	public String getTimeAsString()
	{
		switch (time) {
			default:
			case TIME_SECS: return "SECS";
			case TIME_MINS: return "MINS";
			case TIME_HOURS: return "HOURS";
			case TIME_MINS_SECS: return "MINS SECS";
			case TIME_HOURS_MINS: return "HOURS MINS";
		}
	}
	
	/**
	 * Sets the index.
	 * 
	 * @param index The value to set.
	 */
	public void setIndex(int index)
	{
		switch (index) {
			case THUMBNAILS:
			case SPLIT_VIEW_ROI:
			case MOVIE:
				this.index = index;
				break;
			case SPLIT_VIEW:
			default:
				this.index = SPLIT_VIEW;
		}
	}
	
	/**
	 * Returns the index.
	 * 
	 * @return See above.
	 */
	public int getIndex() { return index; }
	
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
	public void setZStart(int zStart)
	{ 
		this.zStart = zStart; 
	}
	
	/**
	 * Sets the last z-section to project. 
	 * 
	 * @param zEnd The value to set.
	 */
	public void setZEnd(int zEnd)
	{ 
		this.zEnd = zEnd; 
	}
	
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
	public void setColor(String c)
	{ 
		if (c == null || c.trim().length() == 0) return;
		this.color = c;
	}
	
	/**
	 * Returns the color of the scale bar.
	 * 
	 * @return See above.
	 */
	public String getColor() { return color; }
	
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
			case IMAGE_NAME: return "Image Name";
			case DATASET_NAME: return "Datasets";
			case TAG_NAME: return "Tags";
		}
	}
	
	/**
	 * Returns the format as a string.
	 * 
	 * @return See above.
	 */
	public String getFormatAsString() { return FORMATS.get(format); }
	
	/**
	 * Returns the projection type as a string.
	 * 
	 * @return See above.
	 */
	public String getProjectionTypeAsString()
	{
		if (projectionType == ProjectionParam.MAXIMUM_INTENSITY)
			return "Maximum Intensity";
		else if (projectionType == ProjectionParam.MEAN_INTENSITY)
			return "Mean Intensity";
		return "";
	}
	
	/**
	 * Sets the magnification factor.
	 * 
	 * @param magnificationFactor The value to set.
	 */
	public void setMagnificationFactor(double magnificationFactor)
	{
		this.magnificationFactor = magnificationFactor;
	}

	/**
	 * Returns the magnification factor.
	 * 
	 * @return See above.
	 */
	public double getMagnificationFactor()
	{
		return magnificationFactor;
	}
	
	/**
	 * Returns <code>true</code> to indicate that the selected objects will
	 * compose the figure, <code>false</code> to indicate that the displayed
	 * objects will compose the figure.
	 * 
	 * @return See above.
	 */
	public boolean isSelectedObjects() { return selectedObjects; }
	
	/**
	 * Sets to <code>true</code> to indicate that the selected objects will
	 * compose the figure, <code>false</code> to indicate that the displayed
	 * objects will compose the figure.
	 * 
	 * @param value The value to set.
	 */
	public void setSelectedObjects(boolean value) { selectedObjects = value; }
	
	/**
	 * Returns the tags.
	 * 
	 * @return See above.
	 */
	public List<Long> getTags() { return tags; }
	
	/**
	 * Sets the tags.
	 * 
	 * @param tags The value to set.
	 */
	public void setTags(List<Long> tags) { this.tags = tags; }
	
	/**
	 * Sets the selected timepoints.
	 * 
	 * @param timepoints The selected values.
	 */
	public void setTimepoints(List<Integer> timepoints)
	{ 
		this.timepoints = timepoints; 
	}
	
	/**
	 * Returns the selected timepoints.
	 * 
	 * @return See above.
	 */
	public List<Integer> getTimepoints() { return timepoints; }
	
	/**
	 * Sets to <code>true</code> if the images w/o tags are included into the
	 * figure, to <code>false</code> otherwise.
	 * 
	 * @param includeUntagged 	Pass <code>true</code> to include, 
	 * 							<code>false</code> to exclude.
	 */
	public void setIncludeUntagged(boolean includeUntagged)
	{ 
		this.includeUntagged = includeUntagged; 
	} 
	
	/**
	 * Returns to <code>true</code> if the images w/o tags are included into the
	 * figure, to <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	public boolean isIncludeUntagged() { return includeUntagged; }
	
	/**
	 * Sets the <code>DataObject</code> the figure will be attached to.
	 * 
	 * @param anchor The value to set.
	 */
	public void setAnchor(omero.gateway.model.DataObject anchor) { this.anchor = anchor; }
	
	/**
	 * Returns the <code>DataObject</code> the figure will be attached to.
	 * 
	 * @return See above.
	 */
	public omero.gateway.model.DataObject getAnchor() { return anchor; }
	
	/**
	 * Returns the name of the script associated to the index or 
	 * <code>null</code> if index not set.
	 * 
	 * @return See above.
	 */
	public String getScriptName()
	{
		switch (getIndex()) {
			case FigureParam.SPLIT_VIEW_ROI:
				return ROI_SCRIPT;
			case FigureParam.THUMBNAILS:
				return THUMBNAIL_SCRIPT;
			case FigureParam.MOVIE:
				return MOVIE_SCRIPT;
			default:
				return SPLIT_VIEW_SCRIPT;
		}
	}

	/**
	 * Returns the maximum number of items per column.
	 * 
	 * @return See above.
	 */
	public int getMaxPerColumn() { return maxPerColumn; }

	/**
	 * Sets the maximum of items per columns.
	 * 
	 * @param maxPerColumn The value to set.
	 */
	public void setMaxPerColumn(int maxPerColumn)
	{
		this.maxPerColumn = maxPerColumn;
	}

}
