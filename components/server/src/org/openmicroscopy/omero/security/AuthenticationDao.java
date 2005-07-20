/*
 * org.openmicroscopy.omero.security.AuthenticationDao
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

package org.openmicroscopy.omero.security;

//Java imports

//Third-party libraries
import java.util.List;

import org.springframework.dao.DataAccessException;

import net.sf.acegisecurity.BadCredentialsException;
import net.sf.acegisecurity.GrantedAuthority;
import net.sf.acegisecurity.UserDetails;
import net.sf.acegisecurity.providers.dao.PasswordAuthenticationDao;
import net.sf.acegisecurity.providers.dao.User;
import net.sf.acegisecurity.providers.dao.UsernameNotFoundException;
import net.sf.acegisecurity.providers.dao.jdbc.JdbcDaoImpl;

//Application-internal dependencies

/** 
 * TODO
 * 
 * @author  Josh Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:josh.moore@gmx.de">josh.moore@gmx.de</a>
 * @version 1.0 
 * <small>
 * (<b>Internal version:</b> $Rev$ $Date$)
 * </small>
 * @since 1.0
 */

public class AuthenticationDao extends JdbcDaoImpl implements
		PasswordAuthenticationDao {

	public UserDetails loadUserByUsernameAndPassword(String username, String password){
        
		UserDetails user = super.loadUserByUsername(username);

        // xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
		if (null==password){
			throw new BadCredentialsException("Password empty");
        }
		
		String crypted = user.getPassword();
		String recrypted = Crypt.crypt(crypted,password);

		if (!recrypted.equals(crypted)) {
        	throw new BadCredentialsException("Password invalid");
        }
        // xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
        
        return new User(user.getUsername(), user.getPassword(),
            user.isEnabled(), true, true, true, user.getAuthorities());

	}


}
