/*
 * org.openmicroscopy.shoola.omeis.OMEISRegistry
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

package org.openmicroscopy.shoola.omeis;


//Java imports
import java.util.HashMap;
import java.util.Map;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.omeis.services.PixelsReader;

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
public class OMEISRegistry
{

    protected static final Map activationMap = new HashMap();
    static {
        activationMap.put(PixelsReader.class, PixelsReaderActivator.class);
    }
    
    
    private static ServiceActivator createActivator(Object type) 
        throws ActivationException
    {
        if (type == null)
            throw new ActivationException(
                    "No activator defined for the requested service.");
        ServiceActivator activator = null;
        try {
            Class activatorClass = (Class) type;
            activator = (ServiceActivator) activatorClass.newInstance();
        } catch (InstantiationException ie) {
            throw new ActivationException(
                "Couldn't instantiate activator for the requested service.", 
                ie);
        } catch (IllegalAccessException iae) {
            throw new ActivationException(
                    "Couldn't instantiate activator for the requested service.", 
                    iae);
        } catch (ClassCastException cce) {
            throw new ActivationException(
                    "Invalid activator bound to the requested service: "+
                    type+" doesn't implement ServiceActivator.");
        }
        return activator;
    }
    
    public static PixelsReader getPixelsReader(ServiceDescriptor srvDesc) 
        throws ActivationException
    {
        if (srvDesc == null)
            throw new NullPointerException("No service descriptor.");
        Object type = activationMap.get(PixelsReader.class);
        ServiceActivator pixelsReaderActivator = createActivator(type);
        return (PixelsReader) pixelsReaderActivator.activate(srvDesc);
    }

}
