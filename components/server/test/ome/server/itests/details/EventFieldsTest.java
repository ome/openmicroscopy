/*
 * ome.server.itests.details.EventFieldsTest
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
package ome.server.itests.details;

//Java imports
import java.sql.Timestamp;

//Third-party libraries
import org.testng.annotations.Test;

//Application-internal dependencies
import ome.model.core.Image;
import ome.model.enums.EventType;
import ome.model.meta.Event;
import ome.model.meta.Experimenter;
import ome.model.meta.ExperimenterGroup;
import ome.server.itests.AbstractManagedContextTest;

/** 
 * 
 * @author  Josh Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:josh.moore@gmx.de">josh.moore@gmx.de</a>
 * @version 1.0 
 * <small>
 * (<b>Internal version:</b> $Rev$ $Date$)
 * </small>
 * @since 1.0
 */
@Test( groups = "ticket:82" )
public class EventFieldsTest extends AbstractManagedContextTest
{

	public void testContainersWork() throws Exception {
		
		loginRoot();
		
		Event e1 = makeEvent();
		e1 = iUpdate.saveAndReturnObject(e1);
	
		Event e2 = makeEvent();
		e2.setContainingEvent(e1);	
		e2 = iUpdate.saveAndReturnObject(e2);
		
		assertTrue( e2.getContainingEvent().getId().equals( e1.getId() ));
		
	}
    
	public void testExperimenterAndGroupFilled() throws Exception {
		
		Image dummy = new Image();
		dummy.setName("dummy");
		dummy = iUpdate.saveAndReturnObject(dummy);
		
		assertNotNull(dummy.getDetails());
		assertNotNull(dummy.getDetails().getCreationEvent());
		assertNotNull(dummy.getDetails().getCreationEvent().getExperimenter());
		assertNotNull(dummy.getDetails().getCreationEvent().getExperimenterGroup());
		assertNotNull(dummy.getDetails().getUpdateEvent());
		assertNotNull(dummy.getDetails().getUpdateEvent().getExperimenter());
		assertNotNull(dummy.getDetails().getUpdateEvent().getExperimenterGroup());

	}

	// ~ Helpers
	// =========================================================================

	private Event makeEvent() {
		EventType t = new EventType();
		t.setValue("test");
		Event e = new Event();
		e.setType(t);
		e.setTime(new Timestamp(System.currentTimeMillis()));
		e.setExperimenter(new Experimenter(0L,false));
		e.setExperimenterGroup(new ExperimenterGroup(0L,false));
		return e;
	}
	
}
