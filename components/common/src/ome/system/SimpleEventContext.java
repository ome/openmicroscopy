/*
 * ome.system.SimpleEventContext
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

package ome.system;

import java.io.Serializable;
import java.util.List;

import ome.annotations.RevisionDate;
import ome.annotations.RevisionNumber;
import ome.system.Principal;


//Java imports

//Third-party libraries

//Application-internal dependencies

/**
 * simple, non-thread-safe {@link ome.system.EventContext}
 * 
 * @author Josh Moore, josh.moore at gmx.de
 * @version $Revision$, $Date$
 * @see EventContext
 * @since 3.0
 */
@RevisionDate("$Date$")
@RevisionNumber("$Revision$") 
public class SimpleEventContext implements EventContext, Serializable
{

	private static final long serialVersionUID = -3918201598642847439L;

	private Long cgId;
	private Long cuId;
	private Long ceId;
	private String cgName;
	private String cuName;
	private String ceType;
	private boolean isAdmin;
	private boolean isReadyOnly;
	private List<Long> memberOfGroups;
	private List<Long> leaderOfGroups;
	
	public Long getCurrentGroupId() 
	{
		return cgId;
	}

	public String getCurrentGroupName() 
	{
		return cgName;
	}

	public Long getCurrentUserId()
	{
		return cuId;
	}

	public String getCurrentUserName()
	{
		return cuName;
	}

	public boolean isCurrentUserAdmin()
	{
		return isAdmin;
	}

	public boolean isReadyOnly()
	{
		return isReadyOnly;
	}

	public List<Long> getMemberOfGroupsList() {
		return memberOfGroups;
	}
    
	public List<Long> getLeaderOfGroupsList() {
		return leaderOfGroups;
	}
	
	public Long getCurrentEventId() {
		return ceId;
	}
	
	public String getCurrentEventType() {
		return ceType;
	}
}
