/*
 * org.openmicroscopy.shoola.agents.zoombrowser.piccolo.
 * 	GenericEventHandler
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

package org.openmicroscopy.shoola.agents.zoombrowser.piccolo;


//java imports
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import javax.swing.Timer;

//Third-party libraries
import edu.umd.cs.piccolo.event.PBasicInputEventHandler;
import edu.umd.cs.piccolo.event.PInputEvent;
import edu.umd.cs.piccolo.PNode;

//Application-internal dependencies


/** 
 * An event handler for {@link ProjectSelectionCanvas} canvases.
 *
 * @author  Harry Hochheiser &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:hsh@nih.gov">hsh@nih.gov</a>
 *
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 */

public class GenericEventHandler extends PBasicInputEventHandler 
	implements ActionListener {
	
	private int leftButtonMask = MouseEvent.BUTTON1_MASK;
	private final Timer timer =new Timer(300,this);
	private PInputEvent cachedEvent;
	
	/** is this event right after a popup */
	protected boolean postPopup = false;
	
	GenericEventHandler() {
		super();
		
	}
	
	public void mouseEntered(PInputEvent e) {
		PNode n = e.getPickedNode();
		if (n instanceof MouseableNode) 
			((MouseableNode) n).mouseEntered();
		else 
			defaultMouseEntered(e);
		e.setHandled(true);

	}
	
	public void defaultMouseEntered(PInputEvent e) {
		
	}
	
	public void mouseExited(PInputEvent e) {
		PNode n = e.getPickedNode();
		if (n instanceof MouseableNode) 
			((MouseableNode) n).mouseExited();
		e.setHandled(true);
	}
	
	public void mouseClicked(PInputEvent e) {
		
		if ((e.getModifiers() & leftButtonMask) !=
				leftButtonMask)
			return;
		if (timer.isRunning()) {
			timer.stop();
			doMouseDoubleClicked(e);
		}
		else {
			timer.restart();
			cachedEvent = e;
		}
	}
	
	public void actionPerformed(ActionEvent e) {
		if (cachedEvent != null)
			doMouseClicked(cachedEvent);
		cachedEvent = null;
		timer.stop();
	}
	
	public void doMouseClicked(PInputEvent e) {
		PNode n = e.getPickedNode();
		if (n instanceof MouseableNode) {
			((MouseableNode) n).mouseClicked(); 
		}
	}
	
	protected boolean isPostPopup(PInputEvent e) {
		if (postPopup == true) {
			postPopup = false;		
			e.setHandled(true);
			return true;
		}
		else 
			return false;
	}
	
	public void doMouseDoubleClicked(PInputEvent e) {
		PNode n = e.getPickedNode();
		if (n instanceof MouseableNode) 
			((MouseableNode) n).mouseDoubleClicked();
		e.setHandled(true);
	}
    
	public void mouseReleased(PInputEvent e) {
		if (e.isPopupTrigger()) {
			e.setHandled(true);
			handlePopup(e);
		}
	}
	
	public void mousePressed(PInputEvent e) {
		mouseReleased(e);
	}
	
	public void handlePopup(PInputEvent e) {
		PNode n = e.getPickedNode();
		if (n instanceof MouseableNode) 
			((MouseableNode) n).mousePopup();
	}
 
}