/*
 * blitzgateway.util.ServiceUtilities 
 *
  *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2007 University of Dundee. All rights reserved.
 *
 *
 * 	This program is free software; you can redistribute it and/or modify
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
package ome.services.blitz.gateway;



//Java imports
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import omero.ApiUsageException;
import omero.ValidationException;
import omero.api.ContainerClass;
import omero.model.IObject;
import Glacier2.PermissionDeniedException;

/** 
 * 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 	<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author	Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * 	<a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class ServiceUtilities
{	
	/** 
	 * Helper method for the conversion of base types in containers(normally 
	 * of type IObject) to a concrete type.  
	 * @param <T> new type.
	 * @param klass new type class.
	 * @param list container.
	 * @return see above.
	 */
	public static <T extends IObject> List<T> 
    collectionCast(Class<T> klass, List<IObject> list)
    {
        List<T> newList = new ArrayList<T>(list.size());
        for (IObject o : list)
            newList.add((T) o);
        return newList;
    }
	
	/**
	 * Converts the specified POJO into the corresponding model.
	 *  
	 * @param nodeType The POJO class.
	 * @return The corresponding class.
	 */
	public static String convertPojos(ContainerClass nodeType)
	{
		return nodeType.name();
	}
	/**
	 * Helper method to handle exceptions thrown by the connection library.
	 * Methods in this class are required to fill in a meaningful context
	 * message.
	 * This method is not supposed to be used in this class' constructor or in
	 * the login/logout methods.
	 *  
	 * @param t     	The exception.
	 * @param message	The context message.    
	 * @throws DSOutOfServiceException  A connection problem.
	 * @throws DSAccessException    A server-side error.
	 */
	public static void handleException(Throwable t, String message) 
		throws omero.ServerError
	{
		Throwable cause = t.getCause();
		if (cause instanceof omero.ServerError) {
		    throw (omero.ServerError) cause;
		} else {
		    omero.InternalException ie = new omero.InternalException();
		    ie.message = message;
		    ie.serverStackTrace = cause(cause);
		    ie.serverExceptionClass = cause.getClass().getName();
		    throw ie;
		}
	}
	
	public static String cause(Throwable t) {
	    StringWriter sw = new StringWriter();
	    PrintWriter pw = new PrintWriter(sw);
	    t.printStackTrace(pw);
	    pw.close();
	    return sw.toString();
	}
	/**
	 * Utility method to print the error message
	 * 
	 * @param e The exception to handle.
	 * @return  See above.
	 */
	public static String printErrorText(Throwable e) 
	{
		if (e == null) return "";
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		e.printStackTrace(pw);
		return sw.toString();
	}
	
}


