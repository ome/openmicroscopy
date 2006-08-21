/*
 * pojos.PermissionData
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
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *------------------------------------------------------------------------------
 */

package pojos;

//Java imports

//Third-party libraries

//Application-internal dependencies
import ome.model.internal.Permissions;
import ome.model.internal.Permissions.Right;
import ome.model.internal.Permissions.Role;
import static ome.model.internal.Permissions.Right.*;
import static ome.model.internal.Permissions.Role.*;

/** 
 * Simple data object to wrap a {@link Permissions} instance.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 *  @author  <br>Josh Moore &nbsp;&nbsp;&nbsp;&nbsp;
 *              <a href="mailto:josh.more@gmx.de">
 *                  josh.moore@gmx.de</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $ $Date: $)
 * </small>
 * @since 3.0-M3
 */
public class PermissionData
{
	
		private Permissions p;
	
		public PermissionData( )
		{
			this.p = new Permissions();
		}
	
	    public PermissionData( Permissions permissions )
	    {
	    	this.p = permissions;
	    }
	    
		/** Indicates if the group has read access.
		 * @return the groupRead
		 */
		public boolean isGroupRead() {
			return p.isGranted(GROUP,READ);
		}

		/** Indicates if the group has read access.
		 * @return the groupUse
		 */
		public boolean isGroupUse() {
			return p.isGranted(GROUP, USE);
		}

		/** Indicates if the group has write access.
		 * @return the groupWrite
		 */
		public boolean isGroupWrite() {
			return p.isGranted(GROUP, WRITE);
		}

		/** Indicates if the user has read access.
		 * @return the userRead
		 */
		public boolean isUserRead() {
			return p.isGranted(USER, READ);
		}

		/** Indicates if the user has use access.
		 * @return the userUse
		 */
		public boolean isUserUse() {
			return p.isGranted(USER, USE);
		}

		/** Indicates if the user has write access.
		 * @return the userWrite
		 */
		public boolean isUserWrite() {
			return p.isGranted(USER, WRITE);
		}

		/** Indicates if the world has read access.
		 * @return the worldRead
		 */
		public boolean isWorldRead() {
			return p.isGranted(WORLD, READ);
		}

		/** Indicates if the world has use access.
		 * @return the worldUse
		 */
		public boolean isWorldUse() {
			return p.isGranted(WORLD, USE);
		}

		/** Indicates if the world has write access.
		 * @return the worldWrite
		 */
		public boolean isWorldWrite() {
			return p.isGranted(WORLD, WRITE);
		}

		/**
		 * @param groupRead the groupRead to set
		 */
		public void setGroupRead(boolean groupRead) {
			set(groupRead,GROUP,READ);
		}

		/**
		 * @param groupUse the groupUse to set
		 */
		public void setGroupUse(boolean groupUse) {
			set(groupUse,GROUP,USE);
		}

		/**
		 * @param groupWrite the groupWrite to set
		 */
		public void setGroupWrite(boolean groupWrite) {
			set(groupWrite, GROUP, WRITE);
		}

		/**
		 * @param userRead the userRead to set
		 */
		public void setUserRead(boolean userRead) {
			set(userRead,USER,READ);
		}

		/**
		 * @param userUse the userUse to set
		 */
		public void setUserUse(boolean userUse) {
			set(userUse,USER,USE);
		}

		/**
		 * @param userWrite the userWrite to set
		 */
		public void setUserWrite(boolean userWrite) {
			set(userWrite,USER,WRITE);
		}

		/**
		 * @param worldRead the worldRead to set
		 */
		public void setWorldRead(boolean worldRead) {
			set(worldRead,WORLD,READ);
		}

		/**
		 * @param worldUse the worldUse to set
		 */
		public void setWorldUse(boolean worldUse) {
			set(worldUse,WORLD,USE);
		}

		/**
		 * @param worldWrite the worldWrite to set
		 */
		public void setWorldWrite(boolean worldWrite) {
			set(worldWrite,WORLD,WRITE);
		}
		
		// ~ Helpers
		// =========================================================================
    
		private void set(boolean grant, Role role, Right right)
		{
			if (grant) 
			{
				p.grant(role, right);
			} else {
				p.revoke(role, right);
			}
		}
}
