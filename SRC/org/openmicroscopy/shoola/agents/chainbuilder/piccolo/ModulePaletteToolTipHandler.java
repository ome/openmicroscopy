/*
 * org.openmicroscopy.shoola.agents.chainbuilder.piccolo.ModulePaletteToolTipHandler
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
import java.awt.Font;

//Third-party libraries
import edu.umd.cs.piccolo.event.PInputEvent;
import edu.umd.cs.piccolo.nodes.PPath;
import edu.umd.cs.piccolo.nodes.PText;
import edu.umd.cs.piccolo.PCamera;
import edu.umd.cs.piccolo.PNode;

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.zoombrowser.piccolo.PConstants;
import org.openmicroscopy.shoola.agents.zoombrowser.piccolo.ToolTipHandler;
import org.openmicroscopy.shoola.env.data.model.FormalParameterData;
import org.openmicroscopy.shoola.env.data.model.SemanticTypeData;

/** 
 *
 * An event handler for tooltips on the {@link PPaletteCanvas} and the
 * {@link ChainPaletteCanvas}
 *  
 * @author Harry Hochheiser
 * @version 2.1
 * @since OME2.1
 */
public class ModulePaletteToolTipHandler extends ToolTipHandler {
	
	protected Font font = PConstants.TOOLTIP_FONT;
	
	public ModulePaletteToolTipHandler(PCamera camera) {
		super(camera);
	}
	
	/**
	 * The toolTip String is either the name of the module that the mouse
	 * is on, or it is null. If the scale is too large 
	 * (exceeding {@link ToolTipHandler.SCALE_THRESHOLD),
	 * no tooltip is shown
	 * 
	 * @param event the input event that leads to the change.
	 */
	public PNode setToolTipNode(PInputEvent event) {
		PNode p = (PNode) null;
		PNode n = event.getInputManager().getMouseOver().getPickedNode();
		double scale = camera.getViewScale();
		if (scale < ToolTipHandler.SCALE_THRESHOLD) {
			if (n instanceof ModuleView)  {
				String s = ((ModuleView) n).getModule().getName();
				if (s.compareTo("") != 0) {
					PText pt = new PText(s);
					pt.setFont(font);
					p = pt;
				}
			}
			return p;
		}
		else if (n instanceof FormalParameter)
			return getParameterToolTip((FormalParameter) n);
		else 
			return p;
	}
	
	private PNode getParameterToolTip(FormalParameter param) {
		PPath node = new PPath();
		
		FormalParameterData fp = param.getParameter();
		PText p = new PText(fp.getName());
		node.addChild(p);
		double y=0;
		p.setOffset(0,y);
		p.setFont(font);
		y += p.getHeight();
		SemanticTypeData st = fp.getSemanticType();
		if (st != null) {
			p = new PText("Type: "+st.getName());
			node.addChild(p);
			p.setFont(font);
			p.setOffset(0,y);
			y+=p.getHeight();	
		}
		node.setBounds(node.getUnionOfChildrenBounds(null));
		node.setStrokePaint(PConstants.TOOLTIP_BORDER_COLOR);
		node.setPaint(PConstants.TOOLTIP_FILL_COLOR);
		
		return node;	
	}
	
}