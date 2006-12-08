/*
 * org.openmicroscopy.shoola.env.ui.UserNotifierManager 
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
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307  USA
 *
 *------------------------------------------------------------------------------
 */
package org.openmicroscopy.shoola.env.ui;


//Java imports
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.JOptionPane;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.Container;
import org.openmicroscopy.shoola.env.LookupNames;
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.svc.SvcRegistry;
import org.openmicroscopy.shoola.svc.communicator.Communicator;
import org.openmicroscopy.shoola.svc.communicator.CommunicatorDescriptor;
import org.openmicroscopy.shoola.svc.transport.HttpChannel;
import org.openmicroscopy.shoola.util.ui.MessengerDetails;
import org.openmicroscopy.shoola.util.ui.MessengerDialog;

/** 
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
class UserNotifierManager
	implements PropertyChangeListener
{
    
	/** The default message if an error occured while tranfering data. */
	private static final String	MESSAGE = "Sorry, but due to an error we " +
								"were not able to automatically \n" +
								"send your debug information. \n\n" +
								"You can still send us the error message by " +
								"clicking on the \n" +
								"error message tab, copying the error " +
								"message to the clipboard, \n" +
								"and sending it to ";
								
    /** Reference to the container. */
	private Container		container;
	
	/**
	 * Sends a message.
	 * 
	 * @param source	The source of the message.
	 * @param details 	The values to send.
	 */
	private void handleSendMessage(MessengerDialog source, 
								MessengerDetails details)
	{
		Registry reg = container.getRegistry();
		String url = (String) reg.lookup(LookupNames.DEBUG_URL);
		String teamAddress = (String) reg.lookup(LookupNames.DEBUG_EMAIL);
		CommunicatorDescriptor desc = new CommunicatorDescriptor
						(HttpChannel.CONNECTION_PER_REQUEST, url, -1);
		try {
			Communicator c = SvcRegistry.getCommunicator(desc);
			String error = details.getError();
			String reply = "";
			if (error == null || error.length() == 0)
				c.submitComment(details.getEmail(), details.getComment(), 
								details.getExtra(), reply);
			else c.submitError(details.getEmail(), details.getComment(), 
					details.getExtra(), error, reply);
			
			JOptionPane.showMessageDialog(source, reply);
		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(source, MESSAGE+teamAddress+".");
		}
		source.setVisible(false);
		source.dispose();
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param c Reference to the container.
	 */
	UserNotifierManager(Container c)
	{
		container = c;
	}

	/**
	 * Reacts to property changes fired by dialogs.
	 * @see PropertyChangeListener#propertyChange(PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent pce)
	{
		String name = pce.getPropertyName();
		if (name == null) return;
		if (name.equals(MessengerDialog.SEND_PROPERTY)) {
			if (container == null) return;
			MessengerDialog source = (MessengerDialog) pce.getSource();
			
			handleSendMessage(source, (MessengerDetails) pce.getNewValue());
		}
	}
	
}
