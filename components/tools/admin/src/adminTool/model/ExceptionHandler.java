/*
 * adminTool.model.ExceptionHandler 
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
package src.adminTool.model;

//Java imports

//Third-party libraries

//Application-internal dependencies

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
public class ExceptionHandler
{
	private static ExceptionHandler ref;

	public void catchException(Exception e) 
			throws IAdminException, UnknownException, PermissionsException
	{
		if(e instanceof ome.conditions.ApiUsageException)
			throw new IAdminException(e);
		if( e instanceof ome.conditions.ValidationException)
			throw new IAdminException(e);
		else if(e instanceof javax.ejb.EJBAccessException)
			throw new PermissionsException(e);
		else if(e instanceof java.lang.SecurityException)
			throw new PermissionsException(e);
		else if(e instanceof ome.conditions.SecurityViolation)
			throw new PermissionsException(e);
		else 
			throw new UnknownException(e);
	}
	
	
	private ExceptionHandler()
	{
	
	}

	public static ExceptionHandler get()
	{
		if (ref == null)
	    	ref = new ExceptionHandler();		
    	return ref;
	}

	public Object clone()
		throws CloneNotSupportedException
	{
		throw new CloneNotSupportedException(); 
	}

	
}


