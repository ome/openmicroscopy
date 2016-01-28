/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2013 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.agents.imviewer.util.proj;

import java.util.ArrayList;
import java.util.List;

import omero.gateway.model.DatasetData;
import omero.gateway.model.ProjectData;

/** 
 * Utility class storing the projection's parameters.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * @since 3.0-Beta3
 */
public class ProjectionRef
{

	/** The first timepoint to project. */
	private int               	startT;
	
	/** The last timepoint to project. */
	private int               	endT;
	
	/** The first z-section to project. */
	private int               	startZ;
	
	/** The last z-section to project. */
	private int               	endZ;
	
	/** The stepping used while projecting. Default is <code>1</code>. */
	private int               	stepping;
	
	/** The type of projection. */
	private int				  	type;
	
	/** The type of projection. */
	private String            	pixelsType;
	
	/** The collection of datasets where to store the projected image. */
	private List<DatasetData> 	datasets;
	
	/** The name of the projected image. */
	private String            	name;
	
	/** The description of the projected image. */
	private String			  	description;
	
	/** The collection of channels to project.*/
	private List<Integer>	  	channels;
	
	/** 
	 * Flag indicating to apply the rendering settings of the original image 
	 * to the projected one. 
	 */
	private boolean				applySettings;
	
	/** The project to add the dataset to if a new one is created.*/
	private ProjectData			refProject;
	
	/** Creates a new instance.  */
	ProjectionRef()
	{
		setStepping(1);
		channels = new ArrayList<Integer>();
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param startZ The first z-section to project.
	 * @param endZ   The last z-section to project.
	 * @param type   The type of projection.
	 */
	ProjectionRef(int startZ, int endZ, int type)
	{
		this(startZ, endZ, 1, type);
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param startZ The first z-section to project.
	 * @param endZ The last z-section to project.
	 * @param frequence The frequence between each step.
	 *                  Default is <code>1</code>.
	 * @param type The type of projection.
	 */
	ProjectionRef(int startZ, int endZ, int frequence, int type)
	{
		setZInterval(startZ, endZ);
		setStepping(frequence);
		setType(type);
		channels = new ArrayList<Integer>();
	}

	/**
	 * Sets the stepping.
	 * 
	 * @param stepping The stepping between each step.
	 * 					Default is <code>1</code>.
	 */
	void setStepping(int stepping)
	{
		if (stepping < 1) stepping = 1;
		this.stepping = stepping;
	}
	
	/**
	 * Sets the interval to project.
	 * 
	 * @param startZ The first z-section to project.
	 * @param endZ The last z-section to project.
	 */
	void setZInterval(int startZ, int endZ)
	{
		if (startZ > endZ)
			throw new IllegalArgumentException("Interval not valid.");
		this.startZ = startZ;
		this.endZ = endZ;
	}
	
	/**
	 * Sets the interval to project.
	 * 
	 * @param startT The first timepoint to project.
	 * @param endT The last timepoint to project.
	 */
	void setTInterval(int startT, int endT)
	{
		if (startT > endT)
			throw new IllegalArgumentException("Interval not valid.");
		this.startT = startT;
		this.endT = endT;
	}
	
	/**
	 * Sets the pixels type of the destination set.
	 * 
	 * @param pixelsType The value to set.
	 */
	void setPixelsType(String pixelsType)
	{
		this.pixelsType = pixelsType;
	}
	
	/**
	 * Sets the type of projection.
	 * 
	 * @param type The type of projection.
	 */
	void setType(int type) { this.type = type; }
	
	/**
	 * Sets the name of the projected image.
	 * 
	 * @param name The value to set.
	 */
	void setImageName(String name) { this.name = name; }
	
	/**
	 * Sets the description of the projected image.
	 * 
	 * @param description The value to set.
	 */
	void setImageDescription(String description)
	{ 
		this.description = description;
	}
	
	/**
	 * Sets the flag indicating to apply the rendering settings of
	 * the original image to the projected one.
	 * 
	 * @param applySettings Pass <code>true</code> to set the rendering settings
	 * 						of the original image to the new pixels set,
	 * 						<code>false</code> otherwise.
	 */
	void setApplySettings(boolean applySettings)
	{
		this.applySettings = applySettings;
	}
	
	/**
	 * Sets the collection of datasets to add the image to.
	 * 
	 * @param datasets The collection to set.
	 */
	void setDatasets(List<DatasetData> datasets) { this.datasets = datasets; }
	
	/**
	 * Sets the collection of channels to project.
	 * 
	 * @param channels The value to set.
	 */
	void setChannels(List<Integer> channels) { this.channels = channels; }
	
	/**
	 * Returns the name of the projected image.
	 * 
	 * @return See above.
	 */
	public String getImageName() { return name; }
	
	/**
	 * Returns the description of the projected image.
	 * 
	 * @return See above.
	 */
	public String getImageDescription() { return description; }
	
	/**
	 * Returns the collection of datasets to add the image to.
	 * 
	 * @return See above.
	 */
	public List<DatasetData> getDatasets() { return datasets; }
	
	/**
	 * Returns the collection of channels to project/preview.
	 * 
	 * @return See above.
	 */
	public List<Integer> getChannels() { return channels; }
	
	/**
	 * Returns the first z-section to project.
	 * 
	 * @return See above.
	 */
	public int getStartZ() { return startZ; }
	
	/**
	 * Returns the last z-section to project.
	 * 
	 * @return See above.
	 */
	public int getEndZ() { return endZ; }
	
	/**
	 * Returns the first timepoint to project.
	 * 
	 * @return See above.
	 */
	public int getStartT() { return startT; }
	
	/**
	 * Returns the last timepoint to project.
	 * 
	 * @return See above.
	 */
	public int getEndT() { return endT; }
	
	/**
	 * Returns the freqence between each step. Default is <code>1</code>.
	 * 
	 * @return See above.
	 */
	public int getStepping() { return stepping; }
	
	/**
	 * Returns the type of projection.
	 * 
	 * @return See above.
	 */
	public int getType() { return type; }
	
	/**
	 * Returns the pixels type.
	 * 
	 * @return See above.
	 */
	public String getPixelsType() { return pixelsType; }
	
	/**
	 * Returns <code>true</code> if the rendering settings of the original 
	 * image are applied to the projected one, <code>false</code> 
	 * otherwise.
	 * 
	 * @return See above.
	 */
	public boolean isApplySettings() { return applySettings; }
	
	/**
	 * Sets the project of reference.
	 * 
	 * @param project The project to set.
	 */
	public void setProject(ProjectData project) { refProject = project; }
	
	/**
	 * Returns the project to add the newly created dataset to.
	 * 
	 * @return See above.
	 */
	public ProjectData getProject() { return refProject; }

}
