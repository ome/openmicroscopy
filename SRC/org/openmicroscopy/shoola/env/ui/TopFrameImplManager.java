/*
 * org.openmicroscopy.shoola.env.ui.TopFrameImplManager
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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.AbstractButton;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.Container;
import org.openmicroscopy.shoola.env.data.events.ServiceActivationRequest;

/** 
 * 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:a.falconi@dundee.ac.uk">
 * 					a.falconi@dundee.ac.uk</a>
 * @version 2.2 
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
class TopFrameImplManager
	implements ActionListener
{
	/** 
	 * ID to handle action command and position the menu Item 
	 * in the connectMenu.
	 */ 
	static final int   				OMEDS = TopFrame.OMEDS;
	static final int        		OMEIS = TopFrame.OMEIS;
	
	/** Action command ID. */ 
	static final int        		EXIT_APP = 10;
	static final int				HELPME = 11;
	
	private TopFrameImpl 			view;
	private Container 				container;
	
	TopFrameImplManager(TopFrameImpl view, Container container)
	{
		this.view = view;
		this.container = container;
	}

	/** Attach a listener to a JButton or JMenuItem. */
	void attachComponentListener(AbstractButton ab, int id)
	{
		ab.setActionCommand(""+id);
		ab.addActionListener(this);
	}

	/** 
	 * Handles the <code>EXIT<code> event fired by the fileMenu.
	 * 
	 * Required by the ActionListener interface. 
	 */ 
	public void actionPerformed(ActionEvent e)
	{
		try {
			int cmd = Integer.parseInt(e.getActionCommand());
			// just in case we need to handle other events
			switch (cmd) {
				case EXIT_APP:
					System.exit(0);	//TODO: shut down container.
					break;
				case OMEDS:
					connectToOMEDS();	break;
				case OMEIS:
					connectToOMEIS(); break;
				case HELPME:
					help(); break;
				default: break;
			}        
		} catch(NumberFormatException nfe) {
			 container.getRegistry().getLogger().warn(this, 
			 		"Unexpected NumberFormatException: "+nfe.getMessage());
		}
	}
	
	/** Post an event to connect to OMEDS. */
	private void connectToOMEDS()
	{
		ServiceActivationRequest request = new ServiceActivationRequest(
									ServiceActivationRequest.DATA_SERVICES);
		container.getRegistry().getEventBus().post(request);
	}

	/** Connect to OMEIS. */
	private void connectToOMEIS()
	{
		//LoginOMEIS loginIS = new LoginOMEIS(container);
		//showLogin(loginIS);
		//TODO: post an event
	}

	/** Bring up the help window. */
	private void help()
	{
		UserNotifier un = container.getRegistry().getUserNotifier();
		un.notifyInfo("Help....", 
						"Sorry so far, we can't.");
	}
	
}
