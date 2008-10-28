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
import pojos.ChannelData;

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
	
	/** The Channel's mode method options. */
	public static final String[] MODE;
	
	/** The Detector's binning options. */
	public static final String[] BINNING;
	
	/** The Detector's type options. */
	public static final String[] DETECTOR_TYPE;
	
	static {
		IMMERSIONS = new String[7];
		IMMERSIONS[0] = "Oil";
		IMMERSIONS[1] = "Water";
		IMMERSIONS[2] = "Water Dipping";
		IMMERSIONS[3] = "Air";
		IMMERSIONS[4] = "Multi";
		IMMERSIONS[5] = "Glycerol";
		IMMERSIONS[6] = "Other";
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
		ILLUMINATION = new String[3];
		ILLUMINATION[0] = "Transmitted";
		ILLUMINATION[1] = "Epifluorescence";
		ILLUMINATION[2] = "Oblique";
		CONSTRAST_METHOD = new String[8];
		CONSTRAST_METHOD[0] = "Brightfield";
		CONSTRAST_METHOD[1] = "Phase";
		CONSTRAST_METHOD[2] = "DIC";
		CONSTRAST_METHOD[3] = "Hoffman Modulation";
		CONSTRAST_METHOD[4] = "Oblique Illumination";
		CONSTRAST_METHOD[5] = "Polarized Light";
		CONSTRAST_METHOD[6] = "Darkfield";
		CONSTRAST_METHOD[7] = "Fluorescence";
		MODE = new String[14];
		MODE[0] = "Wide-field";
		MODE[1] = "Laser Scanning Microscopy";
		MODE[2] = "Laser Scanning Confocal";
		MODE[3] = "Spinning Disk Confocal";
		MODE[4] = "SlitScan Confocal";
		MODE[5] = "Multi Photon Microscopy";
		MODE[6] = "Structured Illumination";
		MODE[7] = "Single Molecule Imaging";
		MODE[8] = "Total Internal Reflection";
		MODE[9] = "Fluorescence Lifetime";
		MODE[10] = "Spectral Imaging";
		MODE[11] = "Fluorescence Correlation Spectroscopy";
		MODE[12] = "Near Field Scanning Optical Microscopy";
		MODE[13] = "Second Harmonic Generation Imaging";
		BINNING = new String[4];
		BINNING[0] = "1x1";
		BINNING[1] = "2x2";
		BINNING[2] = "3x3";
		BINNING[3] = "4x4";
		DETECTOR_TYPE = new String[9];
		DETECTOR_TYPE[0] = "CCD";
		DETECTOR_TYPE[1] = "Intensified-CCD";
		DETECTOR_TYPE[2] = "Analog-Video";
		DETECTOR_TYPE[3] = "PMT";
		DETECTOR_TYPE[4] = "Photodiode";
		DETECTOR_TYPE[5] = "Spectroscopy";
		DETECTOR_TYPE[6] = "Life-time-Imaging";
		DETECTOR_TYPE[7] = "Correlation-Spectroscoypy";
		DETECTOR_TYPE[8] = "FTIR";
	}
	
}
