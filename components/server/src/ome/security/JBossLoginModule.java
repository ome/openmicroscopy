/*
 * ome.security.JBossLoginModule
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
import javax.security.auth.login.LoginException;

import org.jboss.security.auth.spi.DatabaseServerLoginModule;

//Application-internal dependencies

/** 
 * various tools needed throughout Omero. 
 * 
 * @author  Josh Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:josh.moore@gmx.de">josh.moore@gmx.de</a>
 * @version 1.0 
 * <small>
 * (<b>Internal version:</b> $Rev$ $Date$)
 * </small>
 * @since 1.0
 */
public class JBossLoginModule extends DatabaseServerLoginModule 
{

	/** overrides password creation for testing purposes */
	@Override
	protected String createPasswordHash(String arg0, String arg1, String arg2) 
	throws LoginException {
		String retVal = super.createPasswordHash(arg0, arg1, arg2);
		return retVal;
	}

	/** overrides the standard behavior of returning false (bad match) for 
	 * all differing passwords. Here, we allow stored passwords to be empty
	 * which signifies that anyone can use the account, regardless of password.
	 */
	@Override
	protected boolean validatePassword(
			String inputPassword, String expectedPassword) {
		
		if ( null!=expectedPassword && 
				expectedPassword.trim().length()<=0 ) 
		{
			return true;
		}
		return super.validatePassword(inputPassword, expectedPassword);
	}
}
