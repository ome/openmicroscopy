/*
 * ome.security.AdminAction
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

package ome.security;

//Java imports

//Third-party libraries

//Application-internal dependencies
import ome.annotations.RevisionDate;
import ome.annotations.RevisionNumber;


/** 
 * action for passing to {@link SecuritySystem#runAsAdmin(AdminAction)}. All 
 * external input should be <em>carefully</em> checked or even better copied 
 * before being passed to this method. A common idiom would be:
 * <code>
 *   public void someApiMethod(IObject target, String someValue)
 *   {
 *         	AdminAction action = new AdminAction(){
 *   		public void runAsAdmin() {
 *   	    	IObject copy = iQuery.get( iObject.getClass(), iObject.getId() );
 *   	    	copy.setValue( someValue );
 *   	    	iUpdate.saveObject(copy);    
 *   		}
 *   	};
 *   }
 * </code>
 * 
 * @author  Josh Moore, josh.moore at gmx.de
 * @version $Revision$, $Date$
 * @see     SecuritySystem#runAsAdmin(AdminAction)
 * @since   3.0-M3
 */
@RevisionDate("$Date$")
@RevisionNumber("$Revision$")
public interface AdminAction
{	
	/** executes with special privilegs within the {@link SecuritySystem}. 
	 * @see SecuritySystem#runAsAdmin(AdminAction)
	 */
	void runAsAdmin( );

}
