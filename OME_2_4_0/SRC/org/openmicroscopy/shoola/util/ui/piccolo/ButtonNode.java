/*
 * org.openmicroscopy.shoola.agents.zoombrowser.piccolo.ButtonNode
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

package org.openmicroscopy.shoola.util.ui.piccolo;

//Java imports
import java.awt.Image;

//Third-party libraries
import edu.umd.cs.piccolo.PCanvas;
import edu.umd.cs.piccolo.nodes.PImage;
import edu.umd.cs.piccolo.event.PInputEvent;


/** 
 * A subclass of {@link PPath} that is used to provide a colored background
 * to various widgets in the Chain builder
 * 
 *   
 * @author  Harry Hochheiser &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:hsh@nih.gov">hsh@nih.gov</a>
 *
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 */
public abstract class ButtonNode extends PImage implements MouseableNode {

	private boolean  enabled=true;
	
	private Image enabledImage;
	private Image disabledImage;
	
	public ButtonNode() {
		super();
	}
	
	public ButtonNode(Image image) {
		super(image);
		enabledImage = image;
	}

	public ButtonNode(Image enabled,Image disabled) {
		super(enabled);
		enabledImage = enabled;
		disabledImage = disabled;
	}
	public void mouseEntered(GenericEventHandler handler,PInputEvent e) {
	}
	
	public void mouseExited(GenericEventHandler handler,PInputEvent e) {
	}
	
	public void mouseClicked(GenericEventHandler handler,PInputEvent e) {
		if (enabled == true) {
			doClick();
		}
	}
	
	public void mouseDoubleClicked(GenericEventHandler handler,PInputEvent e) {
		if (enabled == true)  {
			doClick();
		}
	}
	
	public void mousePopup(GenericEventHandler handler,PInputEvent e) {
		if (enabled == true) {
			doPopup();
		}
	}
	
	public void setEnabled(boolean b) {
		enabled =b;
		if (b == true) {
			setImage(enabledImage);
		}
		else if (disabledImage != null)
			setImage(disabledImage);
	}
	
	public void addToCanvas(PCanvas canvas ) {
		canvas.getCamera().addChild(this);
	}
	
	public abstract void doClick();
	
	public abstract void doPopup();
	 
}