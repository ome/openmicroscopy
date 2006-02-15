/*
 * ome.conditions.Policy
 *
 *------------------------------------------------------------------------------
 *
 *  Copyright (C) 2005 Open Microscopy Environment
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
package ome.conditions;

//Java imports
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

//Third-party libraries

//Application-internal dependencies

/**
 * centralization of exception policy for the all components. 
 *  
 * @author  Josh Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:josh.moore@gmx.de">josh.moore@gmx.de</a>
 * @version 2.5 
 * <small>
 * (<b>Internal version:</b> $Rev$ $Date$)
 * </small>
 * @since 2.5
 */
public abstract class Policy {
	
	public final static Set DECLARED_SERVER_EXCEPTIONS = new HashSet();

	public final static Set ROOT_SERVER_EXCEPTIONS = new HashSet();
	
	static{
		DECLARED_SERVER_EXCEPTIONS.add(IllegalArgumentException.class);
		ROOT_SERVER_EXCEPTIONS.add(RootException.class);
	}
	
    public static boolean thrownByServer(Throwable t){
    	
    	if (null == t){
    		return false;
    	}
    	
    	if (DECLARED_SERVER_EXCEPTIONS.contains(t.getClass())) {
			return true;
    	}
    	
    	boolean knownSubclass = false;
    	
    	for (Iterator it = ROOT_SERVER_EXCEPTIONS.iterator(); it.hasNext();) {
			Class c = (Class) it.next();
    		if (c.isAssignableFrom(t.getClass())){
    			knownSubclass = true;
    			break;
			}
    	}
    	
    	return knownSubclass;
		
    }
	
}
