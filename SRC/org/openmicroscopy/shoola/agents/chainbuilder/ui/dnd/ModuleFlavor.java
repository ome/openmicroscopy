/*
 * org.openmicroscopy.shoola.agents.chainbuilder.ui.dnd.ModuleFlavor
 *
 *------------------------------------------------------------------------------
 *
 *  Copyright (C) 2003 Open Microscopy Environment
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
 * Written by:    Harry Hochheiser <hsh@nih.gov>
 *
 *------------------------------------------------------------------------------
 */
 
package org.openmicroscopy.shoola.agents.chainbuilder.ui.dnd;

//Java imports

import java.awt.datatransfer.DataFlavor;

//Third-party libraries

//Application-internal dependencies


/**
 * A DataTransfer Flavor for {@link CModule} objects. This one encodes the 
 * module id as a string, rather than as an Integer. It's a bit of a hack,
 * but it seems necessary if we're going to allow drag/drop of both chains and
 * modules based on a single id - they must have separate class names in the 
 * {@link DataFlavor}, and using {@link Integer} and {@link String} is easier 
 * than serializing either {@link Chain} or {@link Module}
 * (any class used in a Flavor must support serialization...).
 * 
 * @author  Harry Hochheiser &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:hsh@nih.gov">hsh@nih.gov</a>
 *
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 */ 
public class ModuleFlavor extends DataFlavor{
 	
 	

	public ModuleFlavor() {
		super(java.lang.String.class, "Unicode String");
	}
		
	
 	public static final ModuleFlavor moduleFlavor = new ModuleFlavor();
 }
 		
