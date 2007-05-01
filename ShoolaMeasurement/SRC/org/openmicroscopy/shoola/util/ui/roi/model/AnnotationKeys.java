/*
 * org.openmicroscopy.shoola.util.ui.roi.model.AnnotationKeys 
 *
  *------------------------------------------------------------------------------
 *  Copyright (C) 2006 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.util.ui.roi.model;


//Java imports
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
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
	public static final AnnotationKey<String> BASIC_TEXT = 
		new AnnotationKey<String>("BasicTextAnnotation", "");
	
	 /**
     * A set with all attributes defined by this class.
     */
    public final static Set<AnnotationKey> 			supportedAnnotations;
    public final static Map<String, AnnotationKey> 	supportedAnnotationMap;
    
    static 
    {
        HashSet<AnnotationKey> as = new HashSet<AnnotationKey>();
        as.addAll(Arrays.asList(new AnnotationKey[] 
                                                  {
        		BASIC_TEXT
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


