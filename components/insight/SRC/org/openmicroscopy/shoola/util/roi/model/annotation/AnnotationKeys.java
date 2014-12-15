/*
 * org.openmicroscopy.shoola.util.roi.model.AnnotationKeys 
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
package org.openmicroscopy.shoola.util.roi.model.annotation;


//Java imports
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

//Third-party libraries

//Application-internal dependencies
import omero.model.Length;
import omero.model.LengthI;
import omero.model.enums.UnitsLength;
import pojos.WorkflowData;

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
public class AnnotationKeys 
{	
	/** This is the text of the figure object. */
	public static final AnnotationKey<String> TEXT =
		new AnnotationKey<String>("basicTextAnnotation", "");
	
	/** The area of the figure. */
	public static final AnnotationKey<Length> AREA = 
		new AnnotationKey<Length>("measurementArea", new LengthI(0, UnitsLength.PIXEL));
	
	/** The perimeter of the figure. */
	public static final AnnotationKey<Length> PERIMETER = 
		new AnnotationKey<Length>("measurementPerimeter", new LengthI(0, UnitsLength.PIXEL));
	
	/** The volume of the figure. */
	public static final AnnotationKey<Length> VOLUME = 
		new AnnotationKey<Length>("measurementVolume", new LengthI(0, UnitsLength.PIXEL));
	
	/** A list of angles in the figure, used for bezier, line and 
	 * line connection figures which can have a number of elbows. 
	 */
	public  static final AnnotationKey<List<Double>> ANGLE = 
		new AnnotationKey<List<Double>>("measurementAngle", null);
	
	/** A list of lenghts in the figure, used for bezier, line and 
	 * line connection figures which can have a number of elbows. 
	 */
	public static final AnnotationKey<List<Length>> LENGTH = 
		new AnnotationKey<List<Length>>("measurementLength", null);
	
	/** A list of X coords in the figure, used for bezier, line and 
	 * line connection figures which can have a number of elbows, this is 
	 * the x coord of the eablow and start and end point. 
	 */
	public static final AnnotationKey<List<Length>> POINTARRAYX= 
		new AnnotationKey<List<Length>>("measurementPointsX", null);
	
	/** A list of Y coords in the figure, used for bezier, line and 
	 * line connection figures which can have a number of elbows, this is 
	 * the y coord of the eablow and start and end point. 
	 */
	public static final AnnotationKey<List<Length>> POINTARRAYY= 
		new AnnotationKey<List<Length>>("measurementPointsY", null);
	
	/** The X coord of the centre of the object. */
	public static final AnnotationKey<Length> CENTREX= 
		new AnnotationKey<Length>("measurementCentreX", new LengthI(0, UnitsLength.PIXEL));

	/** The Y coord of the centre of the object. */
	public static final AnnotationKey<Length> CENTREY= 
		new AnnotationKey<Length>("measurementCentreY", new LengthI(0, UnitsLength.PIXEL));

	/** The X coord of the start of the object, this is used for line,
	 * lineconnection and bezier figures. */
	public static final AnnotationKey<Length> STARTPOINTX= 
		new AnnotationKey<Length>("measurementStartPointX", new LengthI(0, UnitsLength.PIXEL));

	/** The X coord of the start of the object, this is used for line,
	 * lineconnection and bezier figures. */
	public static final AnnotationKey<Length> STARTPOINTY= 
		new AnnotationKey<Length>("measurementStartPointY", new LengthI(0, UnitsLength.PIXEL));
	
	/** The X coord of the end of the object, this is used for line,
	 * lineconnection and bezier figures. */
	public static final AnnotationKey<Length> ENDPOINTX= 
		new AnnotationKey<Length>("measurementEndPointX", new LengthI(0, UnitsLength.PIXEL));
	
	/** The Y coord of the end of the object, this is used for line,
	 * lineconnection and bezier figures. */
	public static final AnnotationKey<Length> ENDPOINTY= 
		new AnnotationKey<Length>("measurementEndPointY", new LengthI(0, UnitsLength.PIXEL));
	
	/** The width of the figure. */
	public static final AnnotationKey<Length> WIDTH = 
		new AnnotationKey<Length>("measurementWidth", new LengthI(0, UnitsLength.PIXEL));
	
	/** The height of the figure.*/
	public static final AnnotationKey<Length> HEIGHT = 
		new AnnotationKey<Length>("measurementHeight", new LengthI(0, UnitsLength.PIXEL));
	
	
	/**
	 * The namespace associated with the ROI.
	 */
	public static final AnnotationKey<String> NAMESPACE = 
		new AnnotationKey<String>("Namespace", WorkflowData.DEFAULTWORKFLOW);
	
	/**
	 * The keywords associated with the ROI.
	 */
	public static final AnnotationKey<String> KEYWORDS = 
		new AnnotationKey<String>("Keywords", "");
	 /**
     * A set with all attributes defined by this class.
     */
    public final static Set<AnnotationKey> 			supportedAnnotations;
    
    /** The map of the supported annotations. */
    public final static Map<String, AnnotationKey> 	supportedAnnotationMap;
    
    static 
    {
        Set<AnnotationKey> as = new HashSet<AnnotationKey>();
        as.addAll(Arrays.asList(new AnnotationKey[] 
                                                  {
        		TEXT,
        		AREA,
        		PERIMETER, 
        		VOLUME, 
        		ANGLE,
        		LENGTH, 
        		POINTARRAYX, 
        		POINTARRAYY, 
        		CENTREX, 
        		CENTREY, 
        		STARTPOINTX, 
        		STARTPOINTY, 
        		ENDPOINTX, 
        		ENDPOINTY, 
        		WIDTH,
        		HEIGHT, 
        		NAMESPACE,
        		KEYWORDS
        //		ROIID, 
        //		FIGURETYPE
        		
        }));
        
        supportedAnnotations = Collections.unmodifiableSet(as);
        
        HashMap<String,AnnotationKey> am = new HashMap<String,AnnotationKey>();
        for (AnnotationKey a: as) 
        {
            am.put(a.getKey(), a);
        }
        
        supportedAnnotationMap = Collections.unmodifiableMap(am);
    }

}


