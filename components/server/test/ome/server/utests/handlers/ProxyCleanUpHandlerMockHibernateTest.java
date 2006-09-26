/*
 * ome.server.utests.handlers.SessionHandlerMockHibernateTest
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
package ome.server.utests.handlers;

// Java imports
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

// Third-party libraries
import org.hibernate.collection.PersistentBag;
import org.hibernate.collection.PersistentIdentifierBag;
import org.hibernate.collection.PersistentList;
import org.hibernate.collection.PersistentMap;
import org.hibernate.collection.PersistentSet;
import org.hibernate.engine.PersistenceContext;
import org.hibernate.engine.SessionImplementor;
import org.jmock.Mock;
import org.jmock.MockObjectTestCase;
import org.testng.annotations.*;

// Application-internal dependencies
import ome.tools.hibernate.ProxyCleanupFilter;

/**
 * @author Josh Moore &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:josh.moore@gmx.de">josh.moore@gmx.de</a>
 * @version 1.0 <small> (<b>Internal version:</b> $Rev$ $Date$) </small>
 * @since Omero 2.0
 */
@Test( groups = {"ticket:61","hibernate","priority"} )
public class ProxyCleanUpHandlerMockHibernateTest extends MockObjectTestCase
{

	ProxyCleanupFilter filter;
	SessionImplementor session;
	PersistenceContext ctx;
	Mock mockSession, mockCtx;

    @Configuration(beforeTestMethod = true)
    protected void setUp() throws Exception
    {
        super.setUp();
        filter = new ProxyCleanupFilter();
        mockSession = mock(SessionImplementor.class);
        mockCtx = mock(PersistenceContext.class);
        session = (SessionImplementor) mockSession.proxy();
        ctx = (PersistenceContext) mockCtx.proxy();
    }

    @Configuration(afterTestMethod = true)
    protected void tearDown() throws Exception
    {
    	super.verify();
        super.tearDown();
    }

    // ~ Tests
    // =========================================================================

    @Test
    public void testPersistentMap() throws Exception {
    	Map m = new PersistentMap(session, new HashMap());
    	m = filter.filter(null, m);
    	assertFalse( m instanceof PersistentMap );
	}
    
    @Test
    public void testPersistentSet() throws Exception {
    	Set s = new PersistentSet();
    	s = (Set) filter.filter(null, s);
    	assertFalse( s instanceof PersistentSet );
	}
    
    @Test
    public void testPersistentList() throws Exception {
    	List l = new PersistentList();
    	l = (List) filter.filter(null, l);
    	assertFalse( l instanceof PersistentList );
	}
    
    @Test
    public void testPersistentBag() throws Exception {
    	List l = new PersistentBag();
    	l = (List) filter.filter(null, l);
    	assertFalse( l instanceof PersistentBag );
	}
    
    @Test
    public void testPersistentIdBag() throws Exception {
    	List l = new PersistentIdentifierBag();
    	l = (List) filter.filter(null, l);
    	assertFalse( l instanceof PersistentIdentifierBag );
	}
    
    /* exist also as subclasses of AbstractPersistentCollection
     *  - PersistentArrayHolder -- deprecated
     *  - PersistentElementHolder -- ??
     *  - PersistentIndexedElementHolder -- ??
     *  - subclasses of the already tested items
     */


}
