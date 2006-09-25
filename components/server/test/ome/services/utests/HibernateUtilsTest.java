/*
 * ome.services.utests.HibernateUtilsTest
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
package ome.services.utests;

//Java imports

//Third-party libraries
import junit.framework.TestCase;

import org.hibernate.engine.SessionImplementor;
import org.hibernate.persister.entity.EntityPersister;
import org.jmock.Mock;
import org.jmock.MockObjectTestCase;
import org.testng.annotations.*;

//Application-internal dependencies
import ome.model.core.Image;
import ome.model.internal.Details;
import ome.model.internal.Permissions;
import ome.model.meta.Event;
import ome.tools.hibernate.HibernateUtils;

/**
 * @author  Josh Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:josh.moore@gmx.de">josh.moore@gmx.de</a>
 * @version 1.0 
 * <small>
 * (<b>Internal version:</b> $Rev$ $Date$)
 * </small>
 * @since Omero 3.0
 */
public class HibernateUtilsTest extends MockObjectTestCase {
	
    @Configuration(beforeTestMethod = true)
    protected void setUp() throws Exception
    {
        super.setUp();
    }
	
    @Configuration(afterTestMethod = true)
    protected void tearDown() throws Exception
    {
        super.verify();
        super.tearDown();
    }
	
	@Test
	public void testIdEquals() throws Exception {
		assertTrue(HibernateUtils.idEqual(null,null));
		assertTrue(HibernateUtils.idEqual(new Image(1L), new Image(1L)));
		assertFalse(HibernateUtils.idEqual(new Image(), new Image()));
		assertFalse(HibernateUtils.idEqual(new Image(1L), new Image(null)));
		assertFalse(HibernateUtils.idEqual(new Image(null), new Image(1L)));
		assertFalse(HibernateUtils.idEqual(new Image(null), new Image(null)));
		assertFalse(HibernateUtils.idEqual(new Image(null), null));
		assertFalse(HibernateUtils.idEqual(null, new Image(null)));
	}


	Mock mockPersister;
	EntityPersister persister;
	
	String[] names = {"details","field1","field2"};
	Event entity = new Event();
	int[] dirty = { 0 };
	
    @Test
    public void testOnlyLockedChanged() throws Exception {

    	Object[] state = { null, null, null };
    	Object[] current = { new Details(), null, null };
    	setupMocks(current);
    	
    	assertOnlyLockedChanged(state, true);
	
    	Details d = new Details(); d.setPermissions(Permissions.READ_ONLY);
    	current = new Object[] { d, null, null };
    	setupMocks(current);
    	
    	assertOnlyLockedChanged(state, false);
    	
    }
    
    // ~ Helpers
	// =========================================================================
    
    protected void setupMocks(Object[] current)
    {
    	mockPersister = mock(EntityPersister.class);
    	persister = (EntityPersister) mockPersister.proxy();
    	mockPersister.expects( once() ).method( "getPropertyValues" )
    	.will( returnValue( current ));
    	mockPersister.expects( once() ).method( "findDirty" )
    	.will( returnValue( dirty ));

    }
    
    protected void assertOnlyLockedChanged(Object[] state, boolean onlyLockChanged)
    {
    	assertTrue( onlyLockChanged ==  
    			HibernateUtils.onlyLockChanged(
    					null, persister, entity, state, names));
    }
}
