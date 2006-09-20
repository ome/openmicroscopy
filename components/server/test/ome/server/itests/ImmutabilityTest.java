/*
 * ome.server.itests.ImmutabilityTest
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
package ome.server.itests;

//Java imports

//Third-party libraries
import org.testng.annotations.Test;

//Application-internal dependencies
import ome.model.core.Image;
import ome.model.meta.Event;
import ome.parameters.Filter;
import ome.parameters.Parameters;

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
public class ImmutabilityTest extends AbstractManagedContextTest 
{

	@Test
	public void testCreationEventWillBeSilentlyUnchanged() throws Exception {
		
		loginRoot();
		
		Image i = new Image();
		i.setName("immutable creation");
		i = iUpdate.saveAndReturnObject(i);
		
		Event oldEvent = i.getDetails().getCreationEvent();
		Event newEvent = iQuery.findByQuery("select e from Event e where id != :id",
				new Parameters( new Filter().page(0,1) ).addId(oldEvent.getId()));
		
		i.getDetails().setCreationEvent(newEvent);
		
		// This fails because it gets silently copied to our new instance. See:
		// https://trac.openmicroscopy.org.uk/omero/ticket/346
		// i = iUpdate.saveAndReturnObject(i);
		//assertEquals( i.getDetails().getCreationEvent().getId(), oldEvent.getId());
		
		// Saving and reacquiring to be sure.
		iUpdate.saveObject(i);
		i = iQuery.refresh(i);
		assertEquals( i.getDetails().getCreationEvent().getId(), oldEvent.getId());
			
	}

    
}
