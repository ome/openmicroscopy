/*
 * org.openmicroscopy.shoola.agents.chainbuilder.piccolo.FormalInput
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
import java.util.List;

//Third-party libraries
import edu.umd.cs.piccolo.util.PBounds;

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.chainbuilder.data.ChainFormalInputData;
import org.openmicroscopy.shoola.agents.chainbuilder.data.ChainFormalOutputData;
import org.openmicroscopy.shoola.env.data.model.SemanticTypeData;
import org.openmicroscopy.shoola.util.ui.Constants;

/**
 * Nodes for displaying Formal Inputs to OME Modules
 * 
 * @author  Harry Hochheiser &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:hsh@nih.gov">hsh@nih.gov</a>
 *
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 */
public class FormalInput extends FormalParameter {
	
	/**
	 * Layout for the type onde
	 */
	public static final int TYPE_NODE_HORIZ_OFFSET = 0;
	
	public FormalInput(ModuleView node,ChainFormalInputData param) {
		super(node,param);
		
		// if I have a semantic type, add it to the lists of inputs with
		// this semantic type.
		param.addFormalInput(this);
		
		//if (param.getSemanticType()!=null)
			//connection.addInput(param.getSemanticType(),this);
	
		if (typeNode != null)
			typeNode.setOffset(TYPE_NODE_HORIZ_OFFSET,
				FormalParameter.TYPE_NODE_VERTICAL_OFFSET);	
	
		addTarget();
		updateBounds();
	}
	
	protected float getLinkTargetX() {
		PBounds b = labelNode.getFullBoundsReference();
		return (float) (b.getX() -Constants.LINK_TARGET_SIZE);
	}
	
	
	/**
	 * For inputs, the corresponding list is a list of ModuleOutputs.
	 * Find the semantic type of the parameter associated with this widget,
	 * and then ask the canvas for the list of outputs with that semantic type.
	 * 
	 * @return a list of ModuleOutputs with the same semantic type as param.  
	 */
	public List getCorresponding() {
		SemanticTypeData type = param.getSemanticType();
		return ChainFormalOutputData.getOutputsForType(type);
	}
	
	/**
	 * 
	 * any given input can only be connected to one output 
	 * (can't have values coming to an input from multiple places).
	 *
	 * @return true if this parameter is linked to anything at all. 
	 */
	public boolean isLinkedTo(FormalParameter param) {
		return (linkedTo.size() > 0);
	}
	
	/**
	 * An input can only be an origin if there's noting linked to it.
	 * @return true if this paramter can be the origin of a new link. 
	 */ 
	public boolean canBeLinkOrigin() {
		return (linkedTo.size() == 0);
	}
}