/*
 * org.openmicroscopy.shoola.agents.chainbuilder.piccolo.ParameterNode
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

//Java Imports
import edu.umd.cs.piccolo.PNode;

//Third-party libraries

//Application-internal dependencies
/** 
 *
 * This is a convenience class that essentially serves as a type-checking aide,
 * insuring that children of a certain node are all instnaces of 
 * {@link FormalParameter}. This is used in {@link PModule} to define a 
 * node that is the parent of all of the {@link PFormalParamater} nodes, and to
 * simplify related event processing. 
 * 
 * * @author  Harry Hochheiser &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:hsh@nih.gov">hsh@nih.gov</a>
 *
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small> 
 */
public class ParameterNode extends PNode {
	


	public ParameterNode() {
		super();
	}
	
	public void addChild(FormalParameter p) {
		super.addChild(p);
	}
}