/*
 * ome.system.SimpleEventContext
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.system;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import ome.annotations.RevisionDate;
import ome.annotations.RevisionNumber;
import ome.system.Principal;


//Java imports

//Third-party libraries

//Application-internal dependencies

/**
 * simple, non-thread-safe, serializable {@link ome.system.EventContext}
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
	private boolean isReadOnly;
	private List<Long> memberOfGroups;
	private List<Long> leaderOfGroups;
	
	/** copy constructor. Makes defensive copies where necessary */
	public SimpleEventContext( EventContext ec )
	{
		if ( ec == null )
			throw new IllegalArgumentException("Argument cannot be null.");
		
		cgId = ec.getCurrentGroupId();
		cuId = ec.getCurrentUserId();
		ceId = ec.getCurrentEventId();
		cgName = ec.getCurrentGroupName();
		cuName = ec.getCurrentUserName();
		ceType = ec.getCurrentEventType();
		isAdmin = ec.isCurrentUserAdmin();
		isReadOnly = ec.isReadOnly();
		memberOfGroups = new ArrayList<Long>(ec.getMemberOfGroupsList());
		leaderOfGroups = new ArrayList<Long>(ec.getLeaderOfGroupsList());
	}
	
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

	public boolean isReadOnly()
	{
		return isReadOnly;
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
