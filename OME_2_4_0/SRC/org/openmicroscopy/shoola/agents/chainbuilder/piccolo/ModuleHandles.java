/*
 * org.openmicroscopy.shoola.agents.chainbuilder.piccolo.ModuleHandles
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

package org.openmicroscopy.shoola.agents.chainbuilder.piccolo;
 
//Java imports

//Third-party libraries
import edu.umd.cs.piccolox.handles.PBoundsHandle;
import edu.umd.cs.piccolox.util.PBoundsLocator;
 
//Applcation-internal dependencies

/** 
 * Selection handles for {@link ModuleView} objects. This revised type of handle
 * is needed to override the behavior of the default {@link PBoundsHandles}, 
 * which includes a variety of handlers that we don't want in this context.
 * So, we override the code for configuring event handlers, making it do 
 * do nothing. The result is a handle that works for selection only.
 * 
 * @author  Harry Hochheiser &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:hsh@nih.gov">hsh@nih.gov</a>
 *
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 */
 public class ModuleHandles extends PBoundsHandle {
 	
 	public ModuleHandles(PBoundsLocator locator) {
 		super(locator);
 	}
 
 	/**
 	 * To override the event handler from {@link PBoundsHandles}, we simply
 	 * modify the installation routin to install nothing. As a result, any 
 	 * handling of events must now be done at a higher-level - perhaps on the 
 	 * node in question, or on the canvas containing the node.
 	 */	
 	protected void installHandleEventHandlers() {
 	}
 }
 	