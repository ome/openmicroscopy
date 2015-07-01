/*
 * org.openmicroscopy.shoola.agents.measurement.util.AnnotationDescription 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2014 University of Dundee. All rights reserved.
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

package org.openmicroscopy.shoola.agents.measurement.util.model;


//Java imports
import java.util.HashMap;
import java.util.Map;

//Third-party libraries
import org.jhotdraw.draw.AttributeKey;

//Application-internal dependencies
import org.openmicroscopy.shoola.util.roi.model.annotation.AnnotationKeys;
import org.openmicroscopy.shoola.util.roi.model.annotation.MeasurementAttributes;

/** 
 * 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 	<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author	Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * 	<a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class AnnotationDescription
{	

	/** Description of the roi id. */
	public final static String ROIID_STRING = "id";
	
	/** Description of the time point. */
	public final static String TIME_STRING = "T";
	
	/** Description of the Z section. */
	public final static String ZSECTION_STRING = "Z";
	
	/** Description of the shape string. */
	public final static String SHAPE_STRING = "Type";
	
	/** 
	 * The map of annotations/attributes to text descriptions in 
	 * inspector, manager and results windows. 
	 */
	public final static Map<AttributeKey, String>	annotationDescription;
	static
	{
		annotationDescription = new HashMap<AttributeKey, String>();
		annotationDescription.put(AnnotationKeys.TEXT, "Comment");
		annotationDescription.put(AnnotationKeys.ANGLE, "Angle");
		annotationDescription.put(AnnotationKeys.AREA, "Area");
		annotationDescription.put(AnnotationKeys.CENTREX, "Center(X)");
		annotationDescription.put(AnnotationKeys.CENTREY, "Center(Y)");
		annotationDescription.put(AnnotationKeys.ENDPOINTX, "EndCoord(X)");
		annotationDescription.put(AnnotationKeys.ENDPOINTY, "EndCoord(Y)");
		annotationDescription.put(AnnotationKeys.STARTPOINTX, "StartCoord(X)");
		annotationDescription.put(AnnotationKeys.STARTPOINTY, "StartCoord(Y)");
		annotationDescription.put(AnnotationKeys.HEIGHT, "Height");
		annotationDescription.put(AnnotationKeys.WIDTH, "Width");
		annotationDescription.put(AnnotationKeys.LENGTH, "Length");
		annotationDescription.put(AnnotationKeys.PERIMETER, "Perimeter");
		annotationDescription.put(AnnotationKeys.POINTARRAYX, "Coord List(X)");
		annotationDescription.put(AnnotationKeys.POINTARRAYY, "Coord List(Y)");
		annotationDescription.put(AnnotationKeys.VOLUME, "Volume");
		annotationDescription.put(MeasurementAttributes.FILL_COLOR, "Fill Color");
		annotationDescription.put(MeasurementAttributes.FONT_SIZE, "Font Size");
		annotationDescription.put(MeasurementAttributes.STROKE_COLOR, "Line Color");
		annotationDescription.put(MeasurementAttributes.STROKE_WIDTH, "Line Width");
		annotationDescription.put(MeasurementAttributes.TEXT_COLOR, "Font Color");
		annotationDescription.put(MeasurementAttributes.MEASUREMENTTEXT_COLOUR,
														"Measurement Color");
		annotationDescription.put(MeasurementAttributes.SHOWMEASUREMENT,
														"Show Measurement");
		annotationDescription.put(MeasurementAttributes.SCALE_PROPORTIONALLY,
				"Scale Proportionally");

		annotationDescription.put(AnnotationKeys.NAMESPACE,
														"Workflow");
		annotationDescription.put(AnnotationKeys.KEYWORDS,
														"Keywords");
		annotationDescription.put(AnnotationKeys.TAG, "Tag");
		}
}


