/*
 * org.openmicroscopy.shoola.util.roi.model.AnnotationKeys 
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
public class AnnotationKeys {
	
	/** This is the text of the figure object. */
	public static final AnnotationKey<String> TEXT =
		new AnnotationKey<String>("basicTextAnnotation", "");
	
	/** The area of the figure. */
	public static final AnnotationKey<Double> AREA = 
		new AnnotationKey<Double>("measurementArea", 0.0);
	
	/** The perimeter of the figure. */
	public static final AnnotationKey<Double> PERIMETER = 
		new AnnotationKey<Double>("measurementPerimeter", 0.0);
	
	/** The volume of the figure. */
	public static final AnnotationKey<Double> VOLUME = 
		new AnnotationKey<Double>("measurementVolume", 0.0);
	
	/** A list of angles in the figure, used for bezier, line and 
	 * line connection figures which can have a number of elbows. 
	 */
	public  static final AnnotationKey<List<Double>> ANGLE = 
		new AnnotationKey<List<Double>>("measurementAngle", null);
	
	/** A list of lenghts in the figure, used for bezier, line and 
	 * line connection figures which can have a number of elbows. 
	 */
	public static final AnnotationKey<List<Double>> LENGTH = 
		new AnnotationKey<List<Double>>("measurementLength", null);
	
	/** A list of X coords in the figure, used for bezier, line and 
	 * line connection figures which can have a number of elbows, this is 
	 * the x coord of the eablow and start and end point. 
	 */
	public static final AnnotationKey<List<Double>> POINTARRAYX= 
		new AnnotationKey<List<Double>>("measurementPointsX", null);
	
	/** A list of Y coords in the figure, used for bezier, line and 
	 * line connection figures which can have a number of elbows, this is 
	 * the y coord of the eablow and start and end point. 
	 */
	public static final AnnotationKey<List<Double>> POINTARRAYY= 
		new AnnotationKey<List<Double>>("measurementPointsY", null);
	
	/** The X coord of the centre of the object. */
	public static final AnnotationKey<Double> CENTREX= 
		new AnnotationKey<Double>("measurementCentreX", 0.0);

	/** The Y coord of the centre of the object. */
	public static final AnnotationKey<Double> CENTREY= 
		new AnnotationKey<Double>("measurementCentreY", 0.0);

	/** The X coord of the start of the object, this is used for line,
	 * lineconnection and bezier figures. */
	public static final AnnotationKey<Double> STARTPOINTX= 
		new AnnotationKey<Double>("measurementStartPointX", 0.0);

	/** The X coord of the start of the object, this is used for line,
	 * lineconnection and bezier figures. */
	public static final AnnotationKey<Double> STARTPOINTY= 
		new AnnotationKey<Double>("measurementStartPointY", 0.0);
	
	/** The X coord of the end of the object, this is used for line,
	 * lineconnection and bezier figures. */
	public static final AnnotationKey<Double> ENDPOINTX= 
		new AnnotationKey<Double>("measurementEndPointX", 0.0);
	
	/** The Y coord of the end of the object, this is used for line,
	 * lineconnection and bezier figures. */
	public static final AnnotationKey<Double> ENDPOINTY= 
		new AnnotationKey<Double>("measurementEndPointY", 0.0);
	
	/** The width of the figure. */
	public static final AnnotationKey<Double> WIDTH = 
		new AnnotationKey<Double>("measurementWidth", 0.0);
	
	/** The height of the figure.*/
	public static final AnnotationKey<Double> HEIGHT = 
		new AnnotationKey<Double>("measurementHeight", 0.0);
//	public static final AnnotationKey<Long> ROIID = 
//		new AnnotationKey<Long>("regionOfInterestID", null);
//	public static final AnnotationKey<String> FIGURETYPE = 
//		new AnnotationKey<String>("figureType", null);
	
	
	 /**
     * A set with all attributes defined by this class.
     */
    public final static Set<AnnotationKey> 			supportedAnnotations;
    
    /** The map of the supported annotations. */
    public final static Map<String, AnnotationKey> 	supportedAnnotationMap;
    
    static 
    {
        HashSet<AnnotationKey> as = new HashSet<AnnotationKey>();
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


