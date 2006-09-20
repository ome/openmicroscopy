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
import java.util.Collection;
import java.util.Properties;

// Third-party libraries
import org.springframework.beans.factory.FactoryBean;
import org.springframework.orm.hibernate3.FilterDefinitionFactoryBean;

// Application-internal dependencies
import ome.conditions.InternalException;
import ome.model.internal.Details;
import ome.model.internal.Permissions;
import ome.model.internal.Permissions.Right;
import ome.model.internal.Permissions.Role;
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
 * @see <a href="https://trac.openmicroscopy.org.uk/omero/ticket/117">ticket117</a> 
 */
public class SecurityFilter 
extends FilterDefinitionFactoryBean
{

	static public final String is_admin = "is_admin";
	static public final String current_user = "current_user";
	static public final String current_groups = "current_groups";
	static public final String leader_of_groups = "leader_of_groups"; 
	
	static public final String filterName = "securityFilter";
	static private final Properties parameterTypes = new Properties();
	static private String defaultFilterCondition;
	static {
		parameterTypes.setProperty(is_admin, "java.lang.Boolean");
		parameterTypes.setProperty(current_user,"long");
		parameterTypes.setProperty(current_groups,"long");
		parameterTypes.setProperty(leader_of_groups,"long");
		// This can't be done statically because we need the securitySystem.
		defaultFilterCondition = String.format(
				"\n( "+
				"\n :is_admin OR "                             + 
				"\n (group_id in (:leader_of_groups)) OR "     +
				"\n (owner_id = :current_user AND %s) OR "     + // 1st arg  U
				"\n (group_id in (:current_groups) AND %s) OR "+ // 2nd arg  G
				"\n (%s) " +                                     // 3rd arg  W
				"\n)\n",
				isGranted(USER,READ),
				isGranted(GROUP,READ),
				isGranted(WORLD,READ));
	}
	
	/** default constructor which calls all the necessary setters for this
	 * {@link FactoryBean}. Also constructs the {@link #defaultFilterCondition }
	 * This query clause must be kept in sync with 
	 * {@link #passesFilter(Details, Long, Collection, Collection, boolean)}
	 * 
	 * @see #passesFilter(Details, Long, Collection, Collection, boolean)
	 * @see FilterDefinitionFactoryBean#setFilterName(String)
	 * @see FilterDefinitionFactoryBean#setParameterTypes(Properties)
	 * @see FilterDefinitionFactoryBean#setDefaultFilterCondition(String)
	 */
	public SecurityFilter()
	{
		this.setFilterName(filterName);
		this.setParameterTypes(parameterTypes);
		this.setDefaultFilterCondition(defaultFilterCondition);
	}
	
	/** tests that the {@link Details} argument passes the security test that
	 * this filter defines. The two must be kept in sync. This will be used
	 * mostly by the 
	 * {@link OmeroInterceptor#onLoad(Object, java.io.Serializable, Object[], String[], org.hibernate.type.Type[])}
	 * method.
	 * 
	 * @param d Details instance. If null (or if its {@link Permissions} are null
	 * 		all {@link Right rights} will be assumed.
	 * @return true if the object to which this 
	 */
	public static boolean passesFilter( Details d,
			Long currentUserId, 
			Collection<Long> memberOfGroups,
			Collection<Long> leaderOfGroups,
			boolean admin)
	{
		if ( d == null || d.getPermissions() == null )
		{
			throw new InternalException( "Details/Permissions null! " +
					"Security system failure -- refusing to continue. " +
					"The Permissions should be set to a default value.");			
		}
		
		Permissions p = d.getPermissions();
				
		Long o = d.getOwner().getId();
		Long g = d.getGroup().getId();
	
		// most likely and fastest first
		if ( p.isGranted(WORLD, READ)) return true;
		
		if ( currentUserId.equals( o ) 
				&& p.isGranted(USER, READ)) return true;
		
		if ( memberOfGroups.contains( g )
				&& d.getPermissions().isGranted(GROUP, READ)) return true;
		
		if ( admin ) return true;
		
		if ( leaderOfGroups.contains(g)) return true;
		
		return false;
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
