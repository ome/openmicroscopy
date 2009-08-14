/*
 * org.openmicroscopy.shoola.agents.dataBrowser.visitor.MagnificationVisitor 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2008 University of Dundee. All rights reserved.
 *
 *
 * 	This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */
package org.openmicroscopy.shoola.agents.dataBrowser.visitor;

//Java imports

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.dataBrowser.browser.ImageDisplayVisitor;
import org.openmicroscopy.shoola.agents.dataBrowser.browser.ImageNode;
import org.openmicroscopy.shoola.agents.dataBrowser.browser.ImageSet;
import org.openmicroscopy.shoola.agents.dataBrowser.browser.Thumbnail;

/** 
 * Magnifies the {@link ImageNode}s contained in the selected {@link ImageSet}.
 * This visitor is accepted by an {@link ImageSet} not by the browser.
 *
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class MagnificationVisitor
	implements ImageDisplayVisitor
{

	/** The magnification factor. */
	private double factor;
	
	/**
	 * Creates a new instance.
	 * 
	 * @param factor The magnification factor.
	 */
	public MagnificationVisitor(double factor)
	{
		if (factor > Thumbnail.MAX_SCALING_FACTOR) 
			factor = Thumbnail.MAX_SCALING_FACTOR;
		else if (factor < Thumbnail.MIN_SCALING_FACTOR)
			factor = Thumbnail.MIN_SCALING_FACTOR;
		this.factor = factor;
	}
	
	/** 
     * Implemented as specified by {@link ImageDisplayVisitor}. 
     * @see ImageDisplayVisitor#visit(ImageNode)
     */
	public void visit(ImageNode node)
	{
		 Thumbnail th = node.getThumbnail();
		 if (th != null) {
			 double sf = th.getScalingFactor();
		     if (sf != factor) th.scale(factor); 
		 }
	}

    /** 
     * Implemented as specified by {@link ImageDisplayVisitor}. 
     * @see ImageDisplayVisitor#visit(ImageSet)
     */
	public void visit(ImageSet node) {}
	
}
