/*
 * integration.XMLMockObjects 
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
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */
package integration;



//Java imports

//Third-party libraries

//Application-internal dependencies
import ome.xml.model.Arc;
import ome.xml.model.BooleanAnnotation;
import ome.xml.model.CommentAnnotation;
import ome.xml.model.Ellipse;
import ome.xml.model.Filament;
import ome.xml.model.Instrument;
import ome.xml.model.Laser;
import ome.xml.model.LightEmittingDiode;
import ome.xml.model.Line;
import ome.xml.model.LongAnnotation;
import ome.xml.model.Mask;
import ome.xml.model.OME;
import ome.xml.model.Polyline;
import ome.xml.model.Point;
import ome.xml.model.Rectangle;
import ome.xml.model.TagAnnotation;
import ome.xml.model.TermAnnotation;
import ome.xml.model.enums.ArcType;
import ome.xml.model.enums.Binning;
import ome.xml.model.enums.Correction;
import ome.xml.model.enums.DetectorType;
import ome.xml.model.enums.DimensionOrder;
import ome.xml.model.enums.ExperimentType;
import ome.xml.model.enums.FilamentType;
import ome.xml.model.enums.FilterType;
import ome.xml.model.enums.Immersion;
import ome.xml.model.enums.LaserType;
import ome.xml.model.enums.Medium;
import ome.xml.model.enums.MicrobeamManipulationType;
import ome.xml.model.enums.MicroscopeType;
import ome.xml.model.enums.NamingConvention;
import ome.xml.model.enums.PixelType;

/** 
 * Constants that the other classes defining XML objects will require.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
public abstract class XMLMockObjects 
{

	/** The default power of a light source. */
	public static final Double LIGHTSOURCE_POWER = 200.0;

	/** The default model of a component of a microscope. */
	public static final String COMPONENT_MODEL = "Model";
	
	/** The default manufacturer of a component of a microscope. */
	public static final String COMPONENT_MANUFACTURER = "Manufacturer";
	
	/** The default serial number of a component of a microscope. */
	public static final String COMPONENT_SERIAL_NUMBER = "0123456789";
	
	/** The default lot number of a component of a microscope. */
	public static final String COMPONENT_LOT_NUMBER = "9876543210";
	
	/** The default type of a laser. */
	public static final LaserType LASER_TYPE = LaserType.DYE;
	
	/** The default type of an arc. */
	public static final ArcType ARC_TYPE = ArcType.HGXE;
	
	/** The default type of a filament. */
	public static final FilamentType FILAMENT_TYPE = FilamentType.HALOGEN;
	
	/** The default type of a detector. */
	public static final DetectorType DETECTOR_TYPE = DetectorType.CCD;
	
	/** The default objective's correction. */
	public static final Correction CORRECTION = Correction.UV;
	
	/** The default objective's immersion. */
	public static final Immersion IMMERSION = Immersion.OIL;
	
	/** The default objective's immersion. */
	public static final FilterType FILTER_TYPE = FilterType.LONGPASS;
	
	/** The default type of a microscope. */
	public static final MicroscopeType MICROSCOPE_TYPE = MicroscopeType.UPRIGHT;
	
	/** The default type of a microscope. */
	public static final ExperimentType EXPERIMENT_TYPE = ExperimentType.FISH;
	
	/** The default type of a microbeam manipulation. */
	public static final MicrobeamManipulationType 
		MICROBEAM_MANIPULATION_TYPE = MicrobeamManipulationType.FLIP;
	
	/** The default binning value. */
	public static final Binning BINNING = Binning.TWOXTWO;
	
	/** The default medium for the objective. */
	public static final Medium MEDIUM = Medium.AIR;
	
	/** The default number of pixels along the X-axis. */
	public static final Integer SIZE_X = 24;
	
	/** The default number of pixels along the Y-axis. */
	public static final Integer SIZE_Y = 24;
	
	/** The default number of z-sections. */
	public static final Integer SIZE_Z = 1;
	
	/** The default number of channels. */
	public static final Integer SIZE_C = 3;
	
	/** The default number of time-points. */
	public static final Integer SIZE_T = 1;
	
	/** The number of bytes per pixels. */
	public static final Integer BYTES_PER_PIXEL = 2;
	
	/** The default number of rows for a plate. */
	public static final int    ROWS = 16;
	
	/** The default number of columns for a plate. */
	public static final int    COLUMNS = 24;
	
	/** The default number of fields for a well. */
	public static final int    FIELDS = 3;
	
	/** The light sources to handle. */
	public static final String[] LIGHT_SOURCES = {Laser.class.getName(), 
		Arc.class.getName(), Filament.class.getName(), 
		LightEmittingDiode.class.getName(), Laser.class.getName()};
	
	/** The shapes to handle. */
	public static final String[] SHAPES = {Line.class.getName(), 
		Point.class.getName(), Rectangle.class.getName(), 
		Ellipse.class.getName(), Polyline.class.getName(),
		Mask.class.getName()};
	 
	/** The supported types of annotations. */
	public static final String[] ANNOTATIONS = {
		BooleanAnnotation.class.getName(), CommentAnnotation.class.getName(),
		LongAnnotation.class.getName(), TermAnnotation.class.getName(),
		TagAnnotation.class.getName() };
	
	/** The default naming convention for rows. */
	public static final NamingConvention ROW_NAMING_CONVENTION = 
		NamingConvention.LETTER;
	
	/** The default naming convention for columns. */
	public static final NamingConvention COLUMN_NAMING_CONVENTION = 
		NamingConvention.NUMBER;
	
	/** The default dimension order. */
	public static final DimensionOrder DIMENSION_ORDER = DimensionOrder.XYZCT;
	
	/** The default pixels type. */
	public static final PixelType PIXEL_TYPE = PixelType.UINT16;

	/** The number of detectors created. */
	public static final int NUMBER_OF_DECTECTORS = 1;
	
	/** The number of objectives created. */
	public static final int NUMBER_OF_OBJECTIVES = 1;
	
	/** The number of filters created. */
	public static final int NUMBER_OF_FILTERS = 2;
	
	/** The number of dichroics created. */
	public static final int NUMBER_OF_DICHROICS = 1;
	
	/** Points used to create Polyline and Polygon shape. */
	public static final String POINTS = "0,0 10,10";
	
	/** The default time. */
	public static final String TIME = "2006-05-04T18:13:51.0Z";
	
	/** The default cut-in. */
	public static final int CUT_IN = 200;
	
	/** The default cut-out. */
	public static final int CUT_OUT = 300;

	/** Root of the file. */
	protected OME ome;

	/** The instrument used for the metadata. */
	protected Instrument instrument;

	/** Creates a new instance. */
	public XMLMockObjects()
	{
		ome = new OME();
	}
	
	/** 
	 * Returns the root of the XML file.
	 * 
	 * @return See above.
	 */
	public OME getRoot() { return ome; }
	
	/**
	 * Creates and returns the root element.
	 * 
	 * @return See above.
	 */
	public abstract OME createImage();
	
}
