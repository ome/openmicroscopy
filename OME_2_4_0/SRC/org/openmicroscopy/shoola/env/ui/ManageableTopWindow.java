/*
 * org.openmicroscopy.shoola.env.ui.ManageableTopWindow
 *
 *------------------------------------------------------------------------------
 *
 *  Copyright (C) 2004 Open Microscopy Environment
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

package org.openmicroscopy.shoola.env.ui;


//Java imports
import javax.swing.JFrame;

//Third-party libraries

//Application-internal dependencies
/** 
 * A superclass for windows that might be controlled by a {@link TopWindowManager}.
 * @see org.openmicroscopy.shoola.env.ui.TopWindowManager
 * 
 * @author  Harry Hochheiser &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:hsh@nih.gov">hsh@nih.gov</a>
 * 
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
public abstract class ManageableTopWindow
	extends JFrame
{
	
	public ManageableTopWindow() {
		super();
	}
	
	public ManageableTopWindow(String s) {
		super(s);
	}
	
	/** 
	 * Anything that needs to be done before display of window should be done here,
	 *  after which the manager should be told to continue with displaying the window
	 * @param manager
	 */
	public void preHandleDisplay(TopWindowManager manager) {
		manager.continueHandleDisplay();
	}
	
	public void postHandleDisplay() {	
	}
}