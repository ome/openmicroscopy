/*
 * org.openmicroscopy.shoola.env.data.RemoteCaller
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
 * Written by:    Douglas Creager <dcreager@alum.mit.edu>
 *
 *------------------------------------------------------------------------------
 */


package org.openmicroscopy.shoola.env.data;

/**
 * @author Douglas Creager (dcreager@alum.mit.edu)
 * @version 2.2
 * @since OME2.2
 */

public interface RemoteCaller
{
    /**
     * Invoke an arbitrary remote procedure.
     */
    public Object invoke(String procedure, Object[] params);

    /**
     * Invoke a remote method via the <code>dispatch</code> procedure.
     * The method should not expect any parameters.
     */
    public Object dispatch(Object target, String method);

    /**
     * Invoke a remote method via the <code>dispatch</code> procedure.
     * The method should not expect exactly one parameter.
     */
    public Object dispatch(Object target, String method, Object param1);

    /**
     * Invoke a remote method via the <code>dispatch</code> procedure.
     * The method can receive an arbitrary number of parameters.
     */
    public Object dispatch(Object target, String method, Object[] params);

}
