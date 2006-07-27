/*
 * ome.conditions.ValidationException
 *
 *------------------------------------------------------------------------------
 *
 *  Copyright (C) 2005 Open Microscopy Environment
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
package ome.conditions;

//Java imports
import javax.ejb.ApplicationException;

//Third-party libraries

//Application-internal dependencies

/** 
 * More specific {@link ome.conditions.ApiUsageException ApiUsageException}, in 
 * that the specification of your data as outlined in the OME specification is 
 * incorrect. 
 * 
 * <p>
 * Examples include:
 * <ul>
 *  <li>a {@link ome.model.containers.Project Project} name with invalid 
 *  characters</li>
 *  <li>{@link ome.model.display.Color Color} values out-of-range</li>
 *  <li>{@link ome.model.core.Image Image} linked to two distinct
 *  {@link ome.model.containers.Category Categories} in a single 
 *  (mutually-exclusive) {@link ome.model.containers.CategoryGroup CategoryGroup}
 *  </li>    
 * </ul>
 * </p>
 * 
 * @author  Josh Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:josh.moore@gmx.de">josh.moore@gmx.de</a>
 * @version 3.0 
 * <small>
 * (<b>Internal version:</b> $Rev$ $Date$)
 * </small>
 * @since 3.0
 */
@ApplicationException
public class ValidationException extends ApiUsageException{
	
	public ValidationException(String msg){
		super(msg);
	}
	
}
