/*
 * ome.tools.hibernate.SecurityFilter
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

package ome.tools.hibernate;

// Java imports
import java.util.Properties;

// Third-party libraries
import org.springframework.beans.factory.FactoryBean;
import org.springframework.orm.hibernate3.FilterDefinitionFactoryBean;

// Application-internal dependencies
import ome.model.internal.Permissions;
import ome.model.internal.Permissions.Right;
import ome.model.internal.Permissions.Role;
import ome.security.SecuritySystem;
import static ome.model.internal.Permissions.Role.*;
import static ome.model.internal.Permissions.Right.*;

/** overrides {@link FilterDefinitionFactoryBean} in order to construct our
 * security filter in code and not in XML. This allows us to make use of the 
 * knowledge within {@link Permissions}
 *
 * @author Josh Moore &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:josh.moore@gmx.de">josh.moore@gmx.de</a>
 * @version 3.0 <small> (<b>Internal version:</b> $Rev$ $Date$) </small>
 * @since 3.0
 * @see https://trac.openmicroscopy.org.uk/omero/ticket/117 
 */
public class SecurityFilter 
extends FilterDefinitionFactoryBean
{

	static public final String current_user = "current_user";
	static public final String current_groups = "current_groups";
	static public final String leader_of_groups = "leader_of_groups"; 
	
	static public final String filterName = "securityFilter";
	static private Properties parameterTypes = new Properties();
	static private String defaultFilterCondition;
	static {
		parameterTypes.setProperty(current_user,"long");
		parameterTypes.setProperty(current_groups,"long");
		parameterTypes.setProperty(leader_of_groups,"long");
	}
	
	protected SecuritySystem secSys;
	
	/** default constructor which calls all the necessary setters for this
	 * {@link FactoryBean}.
	 * @see FilterDefinitionFactoryBean#setFilterName(String)
	 * @see FilterDefinitionFactoryBean#setParameterTypes(Properties)
	 * @see FilterDefinitionFactoryBean#setDefaultFilterCondition(String)
	 */
	public SecurityFilter(SecuritySystem securitySystem)
	{
		this.secSys = securitySystem;
		this.setFilterName(filterName);
		this.setParameterTypes(parameterTypes);
		
		// This can't be done statically because we need the securitySystem.
		defaultFilterCondition = String.format(
				"\n("+
				"\n (:current_user = %s) OR "                  + // 1st arg root
				"\n (group_id in (:leader_of_groups)) OR "     +
				"\n (owner_id = :current_user AND %s) OR "     + // 2nd arg  U
				"\n (group_id in (:current_groups) AND %s) OR "+ // 3rd arg  G
				"\n (%s)" +                                      // 4th arg  W
				"\n)\n",
				secSys.getRootId(),
				isGranted(USER,READ),
				isGranted(GROUP,READ),
				isGranted(WORLD,READ));
		
		this.setDefaultFilterCondition(defaultFilterCondition);
	}
	
	// ~ Helpers
	// =========================================================================
	
	protected static String isGranted( Role role, Right right )
	{
		String bit = "" + Permissions.bit(role, right);
		String isGranted = String.format(
		"(cast(permissions as bit(64)) & cast(%s as bit(64))) = cast(%s as bit(64))",
		bit,bit);
		return isGranted;
	}
		
}
