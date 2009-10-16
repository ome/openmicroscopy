/*
 * org.openmicroscopy.shoola.env.data.model.MovieExportParam 
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
import java.util.LinkedHashMap;
import java.util.Map;

//Third-party libraries

//Application-internal dependencies

/** 
 * Hosts the parameters needed for the creation of the movie.
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
public class MovieExportParam
{

	/** Movie across z-section. */
	public static final int		Z_MOVIE = 0;
	
	/** Movie across time. */
	public static final int		T_MOVIE = 1;
	
	/** Movie across time. */
	public static final int		ZT_MOVIE = 2;
	
	/** Identify the <code>MPEG</code> format. */
	public static final int		MPEG = 2;
	
	/** Identify the <code>Quick time</code> format. */
	public static final int		QT = 1;

	/** Identify the <code>WMV</code> format. */
	public static final int		WMV = 0;
	
	/** The supported formats. */
	public static final Map<Integer, String> FORMATS;
	
	/** The default number of frames per second. */
	public static final int 	DEFAULT_FPS  = 25;

	/** The extension corresponding to the {@link #MPEG} movie. */
	private static final String MPEG_EXTENSION = ".avi";

	/** The extension corresponding to the {@link #WMV} movie. */
	private static final String WMV_EXTENSION = ".wmv";
		
	/** The extension corresponding to the {@link #QT} movie. */
	private static final String QT_EXTENSION = ".avi";
	
	static {
		FORMATS = new LinkedHashMap<Integer, String>(2);
		FORMATS.put(WMV, "WindowsMediaPlayer");
		FORMATS.put(QT, "QuickTime");
		FORMATS.put(MPEG, "MPEG");
	}
	
	/** The name of the image. */
	private String 	name;
	
	/** The number of frame per second. */
	private int 	fps;
	
	/** The selected format. */
	private int 	format;
	
	/** The scale bar if displayed. */
	private int 	scaleBar;
	
	/** The lower bound of the time interval. */
	private int		startT;
	
	/** The upper bound of the time interval. */
	private int		endT;
	
	/** The lower bound of the z-section interval. */
	private int		startZ;
	
	/** The upper bound of the z-section interval. */
	private int		endZ;
	
	/** Movie either across time of z-section. */
	private int		type;
	
	/** Flag indicating to display or not the real time and or z-section. */
	private boolean labelVisible;
	
	/** The color of the scale bar. */
	private int		color;
	
	/** 
	 * Controls if the passed type is supported.
	 * 
	 * @param type The value to check.
	 */
	private void checkType(int type)
	{
		switch (type) {
			case Z_MOVIE:
			case T_MOVIE:
			case ZT_MOVIE:
				break;
			default:
				throw new IllegalArgumentException("Type not supported.");
		}
	}
	
	/** 
	 * Controls if the passed format is supported.
	 * Returns the name with the extension corresponding to the selected format.
	 * 
	 * @param value The value to check.
	 * @param name  The name of the movie.
	 * @return See above.
	 */
	private String checkFormat(int value, String name)
	{
		switch (value) {
			case MPEG:
				if (name.endsWith(MPEG_EXTENSION)) 
					name = name.substring(0, 
							name.length()-MPEG_EXTENSION.length());
				return name;
			case WMV:
				if (name.endsWith(WMV_EXTENSION)) 
					name = name.substring(0, 
							name.length()-WMV_EXTENSION.length());
				return name;

			case QT:
				if (name.endsWith(QT_EXTENSION)) 
					name = name.substring(0, 
							name.length()-QT_EXTENSION.length());
				return name;
			default:
				throw new IllegalArgumentException("Format not supported.");
		}
	}
	
	/** Initializes the time and z-section intervals. */
	private void initialize()
	{
		startT = -1;
		startZ = -1;
		endT = -1;
		endZ = -1;
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param name		The name of the file.
	 * @param fps		The number of frames per second.
	 * @param format 	The selected format.
	 * @param scaleBar	The scale bar. Set to <code>0</code> 
	 * 					if the scale bar is not visible.
	 * @param type		Movie either across time or z-section.
	 */
	public MovieExportParam(String name, int fps, int format, int scaleBar, 
			int type)
	{
		if (name == null || name.trim().length() == 0)
			throw new IllegalArgumentException("No name specified.");
		checkType(type);
		this.name = checkFormat(format, name);
		this.type = type;
		if (fps <= 0) fps = DEFAULT_FPS;
		this.fps = fps;
		this.format = format;
		if (scaleBar < 0) scaleBar = 0;
		this.scaleBar = scaleBar;
		labelVisible = false;
		color = Color.LIGHT_GRAY.getRGB();
		initialize();
	}
	
	/**
	 * Sets the time interval.
	 * 
	 * @param startT The lower bound of the time interval.
	 * @param endT	The upper bound of the time interval.
	 */
	public void setTimeInterval(int startT, int endT)
	{
		this.startT = startT;
		this.endT = endT;
	}
	
	/**
	 * Sets the z-section interval.
	 * 
	 * @param startZ The lower bound of the time interval.
	 * @param endZ	The upper bound of the time interval.
	 */
	public void setZsectionInterval(int startZ, int endZ)
	{
		this.startZ = startZ;
		this.endZ = endZ;
	}
	
	/**
	 * Returns <code>true</code> if the z-section has been set,
	 * <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	public boolean isZSectionSet()
	{
		return !(startZ == -1 && endZ == -1);
	}
	
	/**
	 * Returns <code>true</code> if the time-point has been set,
	 * <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	public boolean isTimeIntervalSet()
	{
		return !(startT == -1 && endT == -1);
	}
	
	/**
	 * Returns the name of the file.
	 * 
	 * @return See above.
	 */
	public String getName() { return name; }
	
	/**
	 * Returns the number of frame per second.
	 * 
	 * @return See above.
	 */
	public int getFps() { return fps; }
	
	/**
	 * Returns the format. One of the constants defined by this class.
	 * 
	 * @return See above.
	 */
	public int getFormat() { return format; }
	
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
	 * Returns the lower bound of the time interval.
	 * 
	 * @return See above.
	 */
	public int getStartT() { return startT; }
	
	/**
	 * Returns the upper bound of the time interval.
	 * 
	 * @return See above.
	 */
	public int getEndT() { return endT; }
	
	/**
	 * Returns the lower bound of the z-section interval.
	 * 
	 * @return See above.
	 */
	public int getStartZ() { return startZ; }
	
	/**
	 * Returns the upper bound of the z-section interval.
	 * 
	 * @return See above.
	 */
	public int getEndZ() { return endZ; }
	
	/**
	 * Returns the type of movie to create.
	 * One of the following constants: {@link #Z_MOVIE}, {@link #T_MOVIE}
	 * and {@link #ZT_MOVIE}.
	 * 
	 * @return See above.
	 */
	public int getType() { return type; }
	
	/**
	 * Returns the format as a string.
	 * 
	 * @return See above.
	 */
	public String getFormatAsString()
	{
		switch (format) {
			case MPEG: return "video/mpeg";
			case QT: return "video/quicktime";
			case WMV: return "video/wmv";
			default:
				return "";
		}
	}
	
	/**
	 * Returns <code>true</code> if the real time is displayed, 
	 * <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	public boolean isLabelVisible() { return labelVisible; }
	
	/**
	 * Sets the {@link #labelVisible} flag.
	 * <code>false</code> otherwise.
	 * 
	 * @param labelVisible The value to set.
	 */
	public void setLabelVisible(boolean labelVisible)
	{ 
		this.labelVisible = labelVisible; 
	}
	
	/**
	 * Sets the color of the scale bar.
	 * 
	 * @param color The value to set.
	 */
	public void setColor(Color color)
	{ 
		if (color == null) return;
		this.color = color.getRGB() & 0x00ffffff;
	}
	
	/**
	 * Returns the color of the scale bar.
	 * 
	 * @return See above.
	 */
	public int getColor() { return color; }
	
}
