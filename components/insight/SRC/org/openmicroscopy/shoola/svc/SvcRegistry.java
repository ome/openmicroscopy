/*
 * org.openmicroscopy.shoola.svc.SvcRegistry 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2014 University of Dundee. All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
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
package org.openmicroscopy.shoola.svc;



//Java imports
import java.util.HashMap;
import java.util.Map;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.svc.communicator.Communicator;
import org.openmicroscopy.shoola.svc.communicator.CommunicatorDescriptor;
import org.openmicroscopy.shoola.svc.communicator.CommunicatorFactory;

/** 
 * Initializes the services identified by their <code>SvcDescriptor</code>.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * @since OME3.0
 */
public class SvcRegistry
{

    /** Maps a {@link SvcDescriptor}' class to a {@link SvcActivator} object. */
    protected static final Map<Class<?>, Class<?>> 
    activationMap = new HashMap<Class<?>, Class<?>>();

    static {
        activationMap.put(CommunicatorDescriptor.class,
                CommunicatorFactory.class);
    }

    /**
     * Creates the activator corresponding to the specified type.
     * 
     * @param type The type identifying the activator.
     * @return See above
     * @throws SvcActivationException If the activator cannot be initialized.
     */
    private static SvcActivator createActivator(Object type)
            throws SvcActivationException
            {
        if (type == null)
            throw new SvcActivationException(
                    "No activator defined for the requested service.");
        SvcActivator activator = null;

        try {
            Class<?> activatorClass = (Class<?>) type;
            activator = (SvcActivator) activatorClass.newInstance();
        } catch (InstantiationException ie) {
            throw new SvcActivationException(
                    "Couldn't instantiate activator for the requested service.",
                    ie);
        } catch (IllegalAccessException iae) {
            throw new SvcActivationException(
                    "Couldn't instantiate activator for the requested service.",
                    iae);
        } catch (ClassCastException cce) {
            throw new SvcActivationException(
                    "Invalid activator bound to the requested service: "+
                            type+" doesn't implement ServiceActivator.");
        }
        return activator;
            }

    /**
     * Retrieves or creates a {@link Communicator}.
     * 
     * @param desc The service descriptor
     * @return A {@link Communicator} or null if none was created.
     * @throws SvcActivationException If an error occurred while creating the
     *                                service.
     */
    public static Communicator getCommunicator(SvcDescriptor desc)
            throws SvcActivationException
    {
        if (desc == null)
            throw new NullPointerException("No service descriptor.");
        //See if a factory has been registered for the requested service.
        Object factory = activationMap.get(desc.getClass());
        Object service = null;
        if (factory != null) {  //Yes, we know this service.
            try {
                SvcActivator activator = createActivator(factory);
                service = ((CommunicatorFactory) activator).activate(desc);
            } catch (Exception e) {
                throw new SvcActivationException("Descriptor not recognized", 
                        e);
            }
        } else {  //Somebody is attempting to retrieve a service for which
            //no factory has been registered.
            String msg = "Unknown service: "+desc.getSvcName()+".";
            System.err.println("WARNING: SvcRegistry");
            System.err.print(msg);
        }

        return (Communicator) service;
    }

}
