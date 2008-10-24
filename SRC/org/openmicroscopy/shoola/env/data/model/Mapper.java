/*
 * org.openmicroscopy.shoola.env.data.model.Mapper 
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
package org.openmicroscopy.shoola.env.data.model;




//Java imports

//Third-party libraries

//Application-internal dependencies
import omero.RDouble;
import omero.RFloat;
import omero.RInt;
import omero.RString;
import omero.model.Channel;
import omero.model.LogicalChannel;
import omero.model.StatsInfo;

/** 
 * Helper class used to map core object to 
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
public class Mapper
{

	/** The Objective's immersion options. */
	public static final String[] IMMERSIONS;
	
	/** The Objective's coating options. */
	public static final String[] COATING;
	
	/** The Objective's medium options. */
	public static final String[] MEDIUM;
	
	/** The Channel's illumination options. */
	public static final String[] ILLUMINATION;
	
	/** The Channel's constrast method options. */
	public static final String[] CONSTRAST_METHOD;
	
	/** The Channel's constrast method options. */
	public static final String[] MODE;
	
	static {
		IMMERSIONS = new String[6];
		IMMERSIONS[0] = "Oil";
		IMMERSIONS[1] = "Water";
		IMMERSIONS[2] = "WaterDipping";
		IMMERSIONS[3] = "Air";
		IMMERSIONS[4] = "Multi";
		IMMERSIONS[5] = "Other";
		COATING = new String[4];
		COATING[0] = "UV";
		COATING[1] = "PlanApo";
		COATING[2] = "PlanFluor";
		COATING[3] = "SuperFluor";
		MEDIUM = new String[4];
		MEDIUM[0] = "Air";
		MEDIUM[1] = "Oil";
		MEDIUM[2] = "Water";
		MEDIUM[3] = "Glycerol";
		ILLUMINATION = new String[4];
		ILLUMINATION[0] = "Transmitted";
		ILLUMINATION[1] = "Epifluorescence";
		ILLUMINATION[2] = "Oblique";
		ILLUMINATION[3] = "NonLinear";
		CONSTRAST_METHOD = new String[8];
		CONSTRAST_METHOD[0] = "Brightfield";
		CONSTRAST_METHOD[1] = "Phase";
		CONSTRAST_METHOD[2] = "DIC";
		CONSTRAST_METHOD[3] = "Hoffman Modulation";
		CONSTRAST_METHOD[4] = "Oblique Illumination";
		CONSTRAST_METHOD[5] = "Polarized Light";
		CONSTRAST_METHOD[6] = "Darkfield";
		CONSTRAST_METHOD[7] = "Fluorescence";
		MODE = new String[13];
	}
	
	/**
	 * Turns the specified {@link Channel} object into {@link ChannelMetadata}
	 * object.
	 * 
	 * @param index		The index of the channel.	
	 * @param channel	The channel to transform.
	 * @return See above.
	 */
	public static ChannelMetadata mapChannel(int index, Channel channel)
	{
		StatsInfo stats = channel.getStatsInfo();
		double min = 0;
		double max = 1;
		if (stats != null) {
			RDouble object = stats.getGlobalMin();
			if (object != null) min = object.val;
			object = stats.getGlobalMax();
			if (object != null) max = object.val;
		}
		ChannelMetadata cm = new ChannelMetadata(index, min, max);
		LogicalChannel lc = channel.getLogicalChannel();
		RInt value  = lc.getEmissionWave();
		if (value != null) cm.setEmissionWavelength(value.val);
		value = lc.getExcitationWave();
		if (value != null) cm.setExcitationWavelength(value.val);
		value = lc.getPinHoleSize();
		if (value != null) cm.setPinHoleSize(value.val);
		RFloat f = lc.getNdFilter();
		if (f != null) cm.setNDFilter(f.val);
		RString s = lc.getName();
		if (s != null) cm.setName(s.val);
		s = lc.getFluor();
		if (s != null) cm.setFluor(s.val); 
		return cm;
	}
	
}
