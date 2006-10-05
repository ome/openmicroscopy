/*
 * org.openmicroscopy.shoola.env.data.login.LoginServiceImpl
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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Properties;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.Container;
import org.openmicroscopy.shoola.env.LookupNames;
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.data.DSOutOfServiceException;
import org.openmicroscopy.shoola.env.data.DataServicesFactory;
import org.openmicroscopy.shoola.env.data.events.ServiceActivationRequest;
import org.openmicroscopy.shoola.env.data.events.ServiceActivationResponse;
import org.openmicroscopy.shoola.env.event.AgentEvent;
import org.openmicroscopy.shoola.env.event.EventBus;
import org.openmicroscopy.shoola.env.log.LogMessage;
import org.openmicroscopy.shoola.env.log.Logger;
import org.openmicroscopy.shoola.env.ui.LoginOMEDS;
import org.openmicroscopy.shoola.env.ui.UserNotifier;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

/** 
 * Implements the Login Service's logic.
 * This class ignores threading issues altogether and just focuses on providing
 * the service's logic.  The {@link LoginManager} should be used to decorate an
 * instance of this class to obtain a thread-safe service.
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
public class LoginServiceImpl
    implements LoginService
{

    /** 
     * Holds one of the state flags defined by the {@link LoginService}
     * interface to indicate the current state of the service. 
     */
    private int             state;
    
    /** Reference to the runtime environment. */
    private Container       container;
    
    /** Allows to easily access the service's configuration. */
    protected LoginConfig   config;
    
    
    /**
     * Suspends execution for as many milliseconds as specified by the
     * service's configuration.
     */
    private void pause()
    {
        try {
            Thread.sleep(config.getRetryInterval());
        } catch (InterruptedException e) {}
    }
    
    /**
     * Attempts to to log onto <i>OMEDS</i> using the current user's 
     * credentials.
     * Failure or success is reported to the Log Service.
     * 
     * @return <code>true</code> on success, <code>false</code> on failure.
     */
    private boolean attempt()
    {
        UserCredentials uc = config.getCredentials();
        try {
            if (uc == null) 
                throw new DSOutOfServiceException(
                                "No user's credentials have been entered yet.");
            //NOTE: This will never happen if the Splash Screen always blocks
            //waiting for the user's credentials and the login dialog never
            //passes null credentials along.
            
            DataServicesFactory factory = DataServicesFactory.getInstance(
                                                                    container);
            if (factory.isConnected()) return true;
            factory.connect(uc);

            //Log success.
            LogMessage msg = new LogMessage();
            msg.println("Logged onto OMERO at: "+uc.getHostName());
            msg.println(uc);
            Logger logger = container.getRegistry().getLogger();
            logger.info(this, msg);
            
            //Write property.
            Properties defaultProp = new Properties();
            try {
                FileInputStream in = new FileInputStream(
                                            LookupNames.OMERO_PROPERTIES);
                defaultProp.load(in);
                in.close(); 
            } catch (Exception e) {
                // TODO: handle exception
            }
            String s = defaultProp.getProperty(LookupNames.OMERO_SERVER);
            String listOfServers = null;
            if (s == null) {
                listOfServers = uc.getHostName();
            } else {
                String[] l = s.split(LookupNames.SERVER_NAME_SEPARATOR, 0);
                if (l == null || l.length == 0) {
                    listOfServers = uc.getHostName();
                } else {
                    boolean b = false;
                    int n = l.length-1;
                    String name = uc.getHostName();
                    String host;
                    for (int index = 0; index < l.length; index++) {
                        host = l[index].trim();
                        if (name.equals(host)) b = true;
                        if (index == 0) listOfServers = host;
                        else listOfServers += host;
                        if (index != n) listOfServers += ",";
                    }
                    if (!b)
                        listOfServers += ","+uc.getHostName();
                }
            }
            Properties prop = new Properties();
            if (listOfServers != null)
                prop.setProperty(LookupNames.OMERO_SERVER, listOfServers);
            try {
                FileOutputStream out = new FileOutputStream(
                                        LookupNames.OMERO_PROPERTIES);
                prop.store(out, "");
                out.close();
            } catch (Exception e) {
                // TODO: handle exception
            }
            return true;
        } catch (DSOutOfServiceException dsose) {  //Log failure.
            LogMessage msg = new LogMessage();
            msg.println("Failed to log onto OMERO.");
            msg.println("Reason: "+dsose);
            msg.println("OMERO Address: "+uc.getHostName());
            if (uc != null) msg.println(uc);
            Logger logger = container.getRegistry().getLogger();
            logger.error(this, msg);
        }
        return false;
    }
    
    /**
     * Brings up a login dialog to let the user enter their credentials.
     * The dialog will then call the <code>login</code> method passing
     * along the new user's credentials.
     */
    protected void askForCredentials()
    {
        Registry reg = container.getRegistry();
        LoginOMEDS dialog = new LoginOMEDS(reg.getTaskBar().getFrame(), reg);
        UIUtilities.centerAndShow(dialog);
    }
    //NOTE: This method is protected so that subclasses can get rid of the
    //dependencies on Swing.  This is useful in test mode.
    
    /**
     * Creates a new instance.
     * It is assumed that the Container's configuration has already been read
     * in and that the Event Bus is available.
     * 
     * @param c Reference to the runtime environment.
     *          Mustn't be <code>null</code>.
     */
    public LoginServiceImpl(Container c)
    {
        if (c == null) throw new NullPointerException("No container.");
        config = new LoginConfig(c.getRegistry());
        this.container = c;
        EventBus bus = c.getRegistry().getEventBus();
        bus.register(this, ServiceActivationRequest.class);
        state = IDLE;
    }
    
    /**
     * Implemented as specified by the {@link LoginService} interface. 
     * @see LoginService#getState()
     */
    public int getState() { return state; }

    /**
     * Implemented as specified by the {@link LoginService} interface.
     * @see LoginService#login()
     */
    public void login()
    {
        state = ATTEMPTING_LOGIN;
        int max = config.getMaxRetry();
        while (0 < max--) {
            if (attempt()) {
                state = IDLE;
                return;
            }
            pause();
        }
        askForCredentials();
    }

    /**
     * Implemented as specified by the {@link LoginService} interface.
     * @see LoginService#login(UserCredentials)
     */
    public boolean login(UserCredentials uc)
    {
        state = ATTEMPTING_LOGIN;
        config.setCredentials(uc);
        boolean succeeded = attempt(); 
        if (!succeeded) {
            UserNotifier un = container.getRegistry().getUserNotifier();
            un.notifyError("Login Failure", 
                           "Failed to log onto OMERO. Please check your user "+
                           "name and/or password or try again later.");
        }
        state = IDLE;
        return succeeded;
    }

    /**
     * Implemented as specified by the {@link LoginService} interface.
     * @see LoginService#eventFired(AgentEvent)
     */
    public void eventFired(AgentEvent ae)
    {
        //Can only be a ServiceActivationRequest, as we only registered 
        //for that kind of event.  Check if we're connected and reply.
        ServiceActivationRequest sar = (ServiceActivationRequest) ae;
        boolean connected = false;
        try {
            DataServicesFactory factory = DataServicesFactory.getInstance(
                                                                    container);
            connected = factory.isConnected();
        } catch (DSOutOfServiceException dsose) {} 
        ServiceActivationResponse resp = new ServiceActivationResponse(
                                                                sar, connected); 
        EventBus bus = container.getRegistry().getEventBus();
        bus.post(resp);
    }

}
