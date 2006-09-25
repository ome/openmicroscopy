/*
 * ome.server.itests.details.UpdateEventsTest
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

//Third-party libraries
import org.testng.annotations.Test;

//Application-internal dependencies
import ome.conditions.SecurityViolation;
import ome.model.containers.Dataset;
import ome.model.core.Image;
import ome.model.internal.Permissions;
import ome.model.meta.Event;
import ome.parameters.Filter;
import ome.parameters.Parameters;
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
public class UpdateEventsTest extends AbstractManagedContextTest
{

	@Test
	public void testNoOneCanChangeUpdateEvent() throws Exception {
		
		loginRoot();
		
		Image i = new Image();
		i.setName("immutable creation");
		i = iUpdate.saveAndReturnObject(i);
		
		Event oldEvent = i.getDetails().getUpdateEvent();
		Event newEvent = iQuery.findByQuery("select e from Event e where id != :id",
				new Parameters( new Filter().page(0,1) ).addId(oldEvent.getId()));
		
		i.getDetails().setUpdateEvent(newEvent);
		
		try {
			iUpdate.saveObject(i);
			fail("secvio!");
		} catch (SecurityViolation sv) {
			// ok
		}

	}
	
	@Test
	public void testInterceptorRevertsChanges() throws Exception {
		
		loginNewUser();
	
		Image i = new Image();
		i.setName("revert changes");
		i = iUpdate.saveAndReturnObject(i);
		Event modification = i.getDetails().getUpdateEvent();
		
		i.getDetails().setOwner(null);
		i = iUpdate.saveAndReturnObject(i);
		Event test = i.getDetails().getUpdateEvent();
		
		assertEquals(
				"When interceptor reverts changes, " +
				"there should be no modification",
				modification.getId(),test.getId());
	}
	
	@Test
	public void testButARealChangeGetsANewModification() throws Exception {
		
		loginNewUser();
	
		Image i = new Image();
		i.setName("new mod");
		i = iUpdate.saveAndReturnObject(i);
		Event modification = i.getDetails().getUpdateEvent();
		
		i.setName("new mod, take 2");
		i = iUpdate.saveAndReturnObject(i);
		Event test = i.getDetails().getUpdateEvent();
		
		assertFalse(
				"On a real change, the modification must increment",
				modification.getId().equals( test.getId() ));
	}

	@Test
	public void testLockingCanUpdateInTheBackground() throws Exception {
		
		loginNewUser();
		
		Image i = new Image();
		i.setName("lock suprise");
		i = iUpdate.saveAndReturnObject(i);
		Event modification = i.getDetails().getUpdateEvent();
		
		Dataset d = new Dataset();
		d.setName( "contains i" );
		d.linkImage(i);
		
		d = iUpdate.saveAndReturnObject(d);
		i = iQuery.get(i.getClass(),i.getId());
		Event test = i.getDetails().getUpdateEvent();
		
		assertFalse(
			"Locking must also change the update event",
			modification.getId().equals( test.getId())
			);
	}	
    
}
