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
import ome.model.meta.ExternalInfo;
import ome.parameters.Filter;
import ome.parameters.Parameters;
import ome.server.itests.AbstractManagedContextTest;

@Test( groups = "ticket:371" )
public class ExternalInfoTest extends AbstractManagedContextTest
{

	@Test
	public void testNullIsOk() throws Exception {
		
		loginNewUser();

		Image i = new Image();
		i.setName("ticket:371");
		assertNull( i.getDetails().getExternalInfo() );
		
		iUpdate.saveObject(i);

	}
	
	@Test( groups = "MAYCHANGE" )
	public void testLSIDCurrentlyRequired() throws Exception {
		
		loginNewUser();
		
		Image i = new Image();
		i.setName("ticket:371");
		i = iUpdate.saveAndReturnObject(i);
		
		ExternalInfo info = new ExternalInfo();
		info.setEntityType(i.getClass().getName());
		info.setEntityId(i.getId());
		info.setLsid("urn:lsid:example.com:image:1");
		i.getDetails().setExternalInfo(info);
		
		i = iUpdate.saveAndReturnObject(i);
		
		assertNotNull(i.getDetails().getExternalInfo());
	}
	
	@Test
	public void testImmutableAlsoForRoot() throws Exception {
		
	}
}
