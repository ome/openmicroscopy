/*
 * org.openmicroscopy.shoola.agents.chainbuilder.piccolo.CategoryBox
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

//Third-party libraries
import edu.umd.cs.piccolo.event.PInputEvent;
import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.util.PBounds;

//Application-internal dependencies
import org.openmicroscopy.shoola.env.data.model.ModuleCategoryData;
import org.openmicroscopy.shoola.util.ui.Constants;
import org.openmicroscopy.shoola.util.ui.piccolo.BufferedObject;
import org.openmicroscopy.shoola.util.ui.piccolo.GenericBox;
import org.openmicroscopy.shoola.util.ui.piccolo.GenericEventHandler;
import org.openmicroscopy.shoola.util.ui.piccolo.MouseableNode;



/** 
 * A {@link Generic Box} with a name
 * @author  Harry Hochheiser &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:hsh@nih.gov">hsh@nih.gov</a>
 *
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 */
public class CategoryBox extends GenericBox implements MouseableNode {
	
	private ModuleCategoryData category;
	
	public CategoryBox(ModuleCategoryData category) {
		super();
		this.category = category;
	}
	
	public boolean isSameCategory(ModuleCategoryData other) {
		if (other == null && category == null)
			return true;
		else if (other != null && category != null && other.getID() == category.getID())
			return true;
		else return false;
	}
	 
	public void mouseEntered(GenericEventHandler handler,PInputEvent e) {
		((ModulePaletteEventHandler) handler).setLastCategoryBox(this);
		setHighlighted(true);
	}
	
	public void mouseExited(GenericEventHandler handler,PInputEvent e) {
		((ModulePaletteEventHandler) handler).setLastCategoryBox(null);
		setHighlighted(false);
	}
	
	public void mouseClicked(GenericEventHandler handler,PInputEvent e) {
		((ModuleNodeEventHandler) handler).animateToNode(this);
	}
	
	public void mouseDoubleClicked(GenericEventHandler handler,PInputEvent e) {
		
	}
	
	public void mousePopup(GenericEventHandler handler,PInputEvent e) {
		PNode p = getParent();
		if (p instanceof BufferedObject) {
			((ModuleNodeEventHandler) handler).animateToNode(p);		
		} else {
			((ModuleNodeEventHandler) handler).handleBackgroundClick();
		}
	}
	
	public PBounds getBufferedBounds() {
		PBounds b = getGlobalFullBounds();
		
		PBounds p=  new PBounds(b.getX()-2*Constants.BORDER,
				b.getY()-2*Constants.BORDER,
				b.getWidth()+4*Constants.BORDER,
				b.getHeight()+4*Constants.BORDER);
		
		return p;
	}
}

	