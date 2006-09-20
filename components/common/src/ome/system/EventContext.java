/*
 * ome.system.EventContext
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

//Java imports
import java.util.List;

//Third-party libraries

//Application-internal dependencies
import ome.annotations.RevisionDate;
import ome.annotations.RevisionNumber;

/**
 * manages authenticated principals and other context for a given event. Just
 * as all API method calls take place in a transaction and a session (in that 
 * order), they also take place within an Event.  
 * 
 * @author Josh Moore, josh.moore at gmx.de
 * @version $Revision$, $Date$
 * @see ome.model.meta.Experimenter
 * @see ome.model.meta.ExperimenterGroup
 * @since 3.0
 */
@RevisionDate("$Date$")
@RevisionNumber("$Revision$")
public interface EventContext 
{

    Long getCurrentUserId();
    String getCurrentUserName();
    Long getCurrentGroupId();
    String getCurrentGroupName();
    boolean isCurrentUserAdmin();
    boolean isReadyOnly();
    
    Long getCurrentEventId();
    String getCurrentEventType();
    
    List<Long> getMemberOfGroupsList();
    List<Long> getLeaderOfGroupsList();
    
}
