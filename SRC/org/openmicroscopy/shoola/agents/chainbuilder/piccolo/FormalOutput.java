/*
 * org.openmicroscopy.shoola.agents.chainbuilder.piccolo.FormalOutput
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


/** 
 * Nodes for displaying Formal Outputs
 * 
 * @author  Harry Hochheiser &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:hsh@nih.gov">hsh@nih.gov</a>
 *
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 */

public class FormalOutput extends FormalParameter {
	
	public FormalOutput(ModuleView node,ChainFormalOutputData param) {
		this(node,param,null);
	}
	
	public FormalOutput(ModuleView node,ChainFormalOutputData param,
			FormalParameterMouseDelegate delegate) {
		super(node,param,delegate);
		
		param.addFormalOutput(this);
		addTarget();
		updateBounds();  
	}
		
	/**
	 * Overrides call in {@link PNode} to right-align
	 * the text node and the type node
	 */	
	protected void layoutChildren() {
		if (typeNode != null) {
			//set type node offset. 
			PBounds typeBounds = typeNode.getFullBounds();
			//typeNode.localToParent(typeBounds);
			PBounds textBounds = textNode.getFullBounds();
			//textNode.localToParent(textBounds);
			
			if (textBounds.getWidth() > typeBounds.getWidth()) {
				double right = textBounds.getX()+textBounds.getWidth();
				int left = (int) (right - typeBounds.getWidth());
				typeNode.setOffset(left,TYPE_NODE_VERTICAL_OFFSET);
				textNode.setOffset(0,0);
			}
			else { // type is wider
				double right = typeBounds.getX()+typeBounds.getWidth();
				int left  = (int) (right- textBounds.getWidth());
				textNode.setOffset(left,0);
				typeNode.setOffset(0,TYPE_NODE_VERTICAL_OFFSET);
			}
		}
		setTargetPosition();
		updateBounds();
	}
	
	protected float getLinkTargetX() {
		PBounds b = labelNode.getFullBoundsReference();
		return (float) (b.getX()+b.getWidth());
	}
	
	/**
	 * For outputs, the corresponding list is a list of ModuleInputs.
	 * Find the semantic type of the parameter associated with this widget,
	 * and then ask the canvas for the list of inputs with that semantic type.
	 * 
	 * @return a list of ModuleInputs with the same semantic type as param.  
	 */
 	public List getCorresponding() {
		SemanticTypeData type = param.getSemanticType();
		return ChainFormalInputData.getInputsForType(type);
 	}
 	
 	/**
 	 * An output is linked to another parameter if  there is a direct link
 	 * or, if param (an input) has anthing coming in
 	 * 
 	 * @param the second parameter 
 	 * @return true if this parameter is linked to param
 	 */
 	public boolean isLinkedTo(FormalParameter param) {
 		
 		boolean link = super.isLinkedTo(param);
 		boolean inputLinked = param.isLinkedTo(this);
 		return (link || inputLinked);
 	}
}