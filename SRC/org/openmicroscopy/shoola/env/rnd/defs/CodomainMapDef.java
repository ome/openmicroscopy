/*
 * org.openmicroscopy.shoola.env.rnd.defs.CodomainMapDef
 *
 *------------------------------------------------------------------------------
 *
 *  Copyright (C) 2004 Open Microscopy Environment
 *      Massachusetts Institute of Technology,
 *      National Institutes of Health,
 *      University of Dundee
 *
 *
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *------------------------------------------------------------------------------
 */

package org.openmicroscopy.shoola.env.rnd.defs;


//Java imports
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

//Third-party libraries

//Application-internal dependencies

/** 
 * Defines a transformation in the spatial domain (which is the codomain of
 * the quantum map i.e. a sub-interval of [0, 255]. 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:a.falconi@dundee.ac.uk">
 * 					a.falconi@dundee.ac.uk</a>
 * @version 2.2 
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
public class CodomainMapDef
{
	/** Constrast stretching transformation. */
	public static final int		CONTRAST_STRETCHING = 0;
	
	/** Plane slicing transformation. */
	public static final int		PLANE_SLICING = 1;
	
	/** Reverse intensity transformation. */
	public static final int		REVERSE_INTENSITY = 2;
	
	/** Identifies a transformation. One of the constants defined above. */
	private int					type;
	
	private Map					params;
	
	public CodomainMapDef(int type)
	{
		verifyType(type);
		this.type = type;
		params = new HashMap();
	}
	
	public void addParameter(String key, Integer value) 
	{
		if (key == null || value == null) 
			throw new IllegalArgumentException("Cannot be null");
		params.put(key, value);
	}
	
	public void addParameter(String key, Boolean value) 
	{
		if (key == null || value == null) 
			throw new IllegalArgumentException("Cannot be null");
		params.put(key, value);
	}
	
	public void addParameter(String key, Double value) 
	{
		if (key == null || value == null) 
			throw new IllegalArgumentException("Cannot be null");
		params.put(key, value);
	}
	
	public void addParameter(String key, Float value) 
	{
		if (key == null || value == null) 
			throw new IllegalArgumentException("Cannot be null");
		params.put(key, value);
	}
	
	/** Private empty constructor. */
	private CodomainMapDef() {}
	
	/** Overrides the equals method. */
	public boolean equals(Object object)
	{
		boolean b = false;
		if (object instanceof CodomainMapDef) {
			CodomainMapDef cmd = (CodomainMapDef) object;
			b = (cmd.type == type) ;
		}
		return b;
	}
	
	public Map getParams()
	{
		return params;
	}

	public int getType()
	{
		return type;
	}
	
	CodomainMapDef copy()
	{
		CodomainMapDef cmd = new CodomainMapDef();
		cmd.type = this.type;
		Iterator i = params.keySet().iterator();
		Map p = new HashMap();
		while (i.hasNext()) {
			String key = (String) i.next();
			p.put(key, params.get(key));
		}
		cmd.params = p;
		return cmd;
	}
	
	/** Verify if the type is one of the constants defined previously. */
	private void verifyType(int t)
	{
		if (t != CONTRAST_STRETCHING && t != PLANE_SLICING && 
			t != PLANE_SLICING)  
			throw new IllegalArgumentException("Unsupported codomain type");
	}


}
