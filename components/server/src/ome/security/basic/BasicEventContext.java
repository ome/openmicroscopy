/*
 * ome.security.basic.BasicEventContext
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

package ome.security.basic;

//Java imports
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

//Third-party libraries

//Application-internal dependencies
import ome.model.IObject;
import ome.model.internal.Details;
import ome.model.internal.Permissions;
import ome.model.meta.Event;
import ome.model.meta.EventLog;
import ome.model.meta.Experimenter;
import ome.model.meta.ExperimenterGroup;
import ome.system.EventContext;

/** 
 * 
 */
class BasicEventContext implements EventContext
{
	Details details;
	Permissions umask;
	boolean isAdmin = false;
	boolean isReadyOnly = false;
	Collection<Long> memberOfGroups;
	Collection<Long> leaderOfGroups;
	Set<String> disabledSubsystems;
	Set<IObject> lockCandidates;
	Map<Class,Map<String,EventLog>> logs;

    // ~ EventContext interface
	// =========================================================================
    
	public Long getCurrentEventId() {
		Event e = this.details.getCreationEvent();
		return e == null ? null : e.getId();
	}

	public String getCurrentEventType() {
		Event e = this.details.getCreationEvent();
		return e == null ? null : e.getType() == null ? null : e.getType().getValue(); 
	}

	public Long getCurrentGroupId() {
		ExperimenterGroup g = this.details.getGroup();
		return g == null ? null : g.getId();
	}

	public String getCurrentGroupName() {
		ExperimenterGroup g = this.details.getGroup();
		return g == null ? null : g.getName();
	}

	public Long getCurrentUserId() {
		Experimenter e = this.details.getOwner();
		return e == null ? null : e.getId();
	}

	public String getCurrentUserName() {
		Experimenter e = this.details.getOwner();
		return e == null ? null : e.getOmeName();
	}

	public List<Long> getLeaderOfGroupsList() {
		Collection<Long> l = this.leaderOfGroups;
		if (l == null) return Collections.emptyList();
		return new ArrayList<Long>(l);
	}

	public List<Long> getMemberOfGroupsList() {
		Collection<Long> l = this.memberOfGroups;
		if (l == null) return Collections.emptyList();
		return new ArrayList<Long>(l);
	}

	public boolean isCurrentUserAdmin() {
		return this.isAdmin;
	}

	public boolean isReadyOnly() {
		return this.isReadyOnly;
	}
    
} 
