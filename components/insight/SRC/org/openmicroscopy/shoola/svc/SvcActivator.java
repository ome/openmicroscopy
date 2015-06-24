/*
 * org.openmicroscopy.shoola.svc.SvcActivator 
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

//Third-party libraries

//Application-internal dependencies

/** 
 * Defines the functionality of a Factory for creating components
 * that provide specified services.
 *
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * @since OME3.0
 */
public interface SvcActivator
{

    /**
     * Creates or recycles a component that provides the specified service.
     * This method should <i>never</i> return <code>null</code>. If the
     * specified service can't be instantiated or recycled, then an exception
     * should be thrown that details the error.
     *   
     * @param service Specifies which service to activate.
     * @return An object implementing the requested service interface.
     * @throws Exception If the specified service couldn't be instantiated or
     *                   recycled.
     */
    public Object activate(SvcDescriptor service)
        throws Exception;

    /**
     * Cleans up any cached/pooled components and ensures any external resource
     * still held by the components is released.
     */
    public void deactivate();

}
