/*
 * ome.conditions.ACLViolation
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
package ome.conditions.acl;

//Java imports
import javax.ejb.ApplicationException;

import ome.model.internal.Permissions;

//Third-party libraries

//Application-internal dependencies

/** 
 * User has attempted an action which is not permitted by the {@link Permissions}
 * of a given instance.
 * 
 * @author  Josh Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:josh.moore@gmx.de">josh.moore@gmx.de</a>
 * @version 2.5 
 * <small>
 * (<b>Internal version:</b> $Rev$ $Date$)
 * </small>
 * @since 2.5
 */
@ApplicationException
public abstract class ACLViolation extends ome.conditions.SecurityViolation
{

	private Class klass;
	
	private Long id;
	
	public ACLViolation(Class klass, Long id, String msg){
		super(msg);
		this.klass = klass;
		this.id = id;
	}

	@Override
	public String getMessage() {
		
		String s = super.getMessage();
		if ( s == null ) s = "";
		
		String k = klass == null ? "No class" : klass.getName();
		
		String i = id == null ? "No id" : id.toString();
		
		int size = s.length() + k.length() + i.length();
		
		StringBuilder sb = new StringBuilder( size + 16 );
		sb.append( k );
		sb.append(":");
		sb.append( i );
		sb.append(" -- ");
		sb.append( s );
		return sb.toString();
	}
}
