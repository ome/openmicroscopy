/*
 * org.openmicroscopy.shoola.env.data.login.LoginManager
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

package org.openmicroscopy.shoola.env.data.login;


//Java imports
import java.awt.Rectangle;
import javax.swing.JDialog;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.Container;
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.data.DataServicesFactory;
import org.openmicroscopy.shoola.env.data.events.ServiceActivationRequest;
import org.openmicroscopy.shoola.env.data.events.ServiceActivationResponse;
import org.openmicroscopy.shoola.env.event.AgentEvent;
import org.openmicroscopy.shoola.env.event.AgentEventListener;
import org.openmicroscopy.shoola.env.ui.LoginOMEDS;

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
public class LoginManager
	implements AgentEventListener
{
	private static LoginManager		singleton;
	
	//NB: this can't be called outside of container b/c agents have no refs
	//to the singleton container. So we can be sure this method is going to
	//create services just once.
	public static LoginManager getInstance(Container c)
	{
		if (c == null)
			throw new NullPointerException();  //An agent called this method?
		if (singleton == null)	singleton = new LoginManager(c);
		return singleton;
	}

	private Registry			registry;
	private DataServicesFactory	dsf;
	
	private LoginManager(Container c)
	{
		registry = c.getRegistry();
		dsf = DataServicesFactory.getInstance(c);
	}
	
	private void handleSARequest(ServiceActivationRequest request) 
	{
		//We can pop up the dialog without problems b/c run within 
		//the Swing thread
		LoginOMEDS editor = new LoginOMEDS(registry, dsf);
		showDialog(editor);
		boolean result = editor.getManager().isActivationSuccessful();
		ServiceActivationResponse 
		response = new ServiceActivationResponse(request, result);
		registry.getEventBus().post(response);
	}
	
	public void activate()
	{
		registry.getEventBus().register(this, ServiceActivationRequest.class);
	}
	
	public void terminate()
	{
		//TODO: implement.
	}

	public void eventFired(AgentEvent e) 
	{
		//We have only registered for this event.
		handleSARequest((ServiceActivationRequest) e);
	}
	
	/** 
	 * Sizes, centers and brings up the specified editor dialog.
	 *
	 * @param   editor	The editor dialog.
	 */
	private void showDialog(JDialog editor)
	{
		Rectangle tfB = registry.getTopFrame().getFrame().getBounds(), 
					psB = editor.getBounds();
		int offsetX = (tfB.width-psB.width)/2, 
			offsetY = (tfB.height-psB.height)/2;
		if (offsetX < 0)   offsetX = 0;
		if (offsetY < 0)   offsetY = 0;
		editor.setLocation(tfB.x+offsetX, tfB.y+offsetY);
		editor.setVisible(true);
	}
	
}
