/*
 * ome.facade.PrimitiveHierarchyBrowsingImpl
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

/*------------------------------------------------------------------------------
 *
 * Written by:    Josh Moore <josh.moore@gmx.de>
 *
 *------------------------------------------------------------------------------
 */

package ome.facade;

//Java imports

//Third-party libraries
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

//Application-internal dependencies

import ome.api.OMEModel;
import ome.api.HierarchyBrowsing;
import ome.api.PrimitiveHierarchyBrowsing;



/**
 * implementation of the PrimitiveHierarchyBrowsing service. 
 * 
 * @author Josh Moore, <a href="mailto:josh.moore@gmx.de">josh.moore@gmx.de</a>
 * @version 1.0
 * <small>
 * (<b>Internal version:</b> $Rev$ $Date$)
 * </small>
 * @since OMERO 1.0
 */
public class PrimitiveHierarchyBrowsingImpl implements PrimitiveHierarchyBrowsing {

    private static Log log = LogFactory.getLog(PrimitiveHierarchyBrowsingImpl.class);

    private HierarchyBrowsing hb;

    public void setHierarchyBrowsingService(HierarchyBrowsing service) {
        this.hb = service;
    }

    /**
     * @see ome.api.PrimitiveHierarchyBrowsing#loadPDIHierarchy(java.lang.String, int)
     */
    public OMEModel loadPDIHierarchy(final String arg0, final int arg1) {
    	try {
			return hb.loadPDIHierarchy(Class.forName(arg0),arg1);
		} catch (ClassNotFoundException e) {
			return hb.loadPDIHierarchy(null,arg1); // Using exception from the original 
		}
    }

}