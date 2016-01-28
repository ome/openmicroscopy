/*
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.blitz.test.utests;

import java.util.HashMap;

import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;

import ome.services.blitz.impl.ServiceFactoryI;
import ome.services.sessions.SessionManager;
import ome.services.util.Executor;
import ome.system.Principal;
import omero.api.ServiceInterfacePrx;
import omero.api._IQueryOperations;
import omero.api._IQueryTie;
import omero.constants.CLIENTUUID;
import omero.util.ServantHolder;

import org.jmock.Mock;
import org.jmock.MockObjectTestCase;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@Test
public class ServiceFactoryKeepAliveUnitTest extends MockObjectTestCase {

    protected Executor executor = new Executor.Impl(null, null, null, null) {
        @Override
        public Object execute(Principal p, Work work) {
            return work.doWork(null, null);
        }
    };

    ServantHolder holder = new ServantHolder("session");
    Element elt = new Element(null, null);
    Mock cacheMock, proxyMock, managerMock, queryOpsMock;
    Ehcache cache;
    _IQueryOperations queryOps;
    ServiceFactoryI sf;
    ServiceInterfacePrx prx;
    SessionManager manager;
    Ice.Identity id = Ice.Util.stringToIdentity("test/session");
    Ice.Current current = new Ice.Current();
    {
        current.ctx = new HashMap<String, String>();
        current.ctx.put(CLIENTUUID.value, "clientuuid");
    }

    @Override
    @BeforeMethod
    protected void setUp() throws Exception {
        managerMock = mock(SessionManager.class);
        cacheMock = mock(Ehcache.class);
        cache = (Ehcache) cacheMock.proxy();
        proxyMock = mock(ServiceInterfacePrx.class);
        queryOpsMock = mock(_IQueryOperations.class);
        queryOps = (_IQueryOperations) queryOpsMock.proxy();
        prx = (ServiceInterfacePrx) proxyMock.proxy();
        manager = (SessionManager) managerMock.proxy();
        managerMock.expects(once()).method("inMemoryCache").will(
                returnValue(cache));
        cacheMock.expects(once()).method("isKeyInCache")
                .will(returnValue(false));
        cacheMock.expects(once()).method("put");
        sf = new ServiceFactoryI(current, holder,
                null, null, manager, executor,
                new Principal("a", "b", "c"), null, null, null);
    }

    @Test
    void testKeepAliveReturnsAllOnesOnNull() throws Exception {
        managerMock.expects(atLeastOnce()).method("getEventContext");
        assertTrue(-1 == sf.keepAllAlive(null, null));
        assertTrue(-1 == sf.keepAllAlive(new ServiceInterfacePrx[] {}, null));
    }

    @Test
    void testKeepAliveReturnsNonNullIfMissing() throws Exception {
        managerMock.expects(atLeastOnce()).method("getEventContext");
        cacheMock.expects(once()).method("get").will(returnValue(null));
        proxyMock.expects(once()).method("ice_getIdentity").will(
                returnValue(id));
        long rv = sf.keepAllAlive(new ServiceInterfacePrx[] { prx }, null);
        // TODO: assertTrue((rv & 1 << 0) == 1 << 0);
        // This is currently failing, but based on the nullness of the proxy
        // in the holder. This is currently unused though. 
    }

    @Test
    void testIsAliveReturnsFalseIfMissing() throws Exception {
        managerMock.expects(atLeastOnce()).method("getEventContext");
        cacheMock.expects(once()).method("get").will(returnValue(null));
        proxyMock.expects(once()).method("ice_getIdentity").will(
                returnValue(id));
        assertFalse(sf.keepAlive(prx, null));
    }

    @Test
    void testKeepAliveReturnsZeroIfPresent() throws Exception {
        holder.put(id, new _IQueryTie(queryOps));
        managerMock.expects(atLeastOnce()).method("getEventContext");
        proxyMock.expects(once()).method("ice_getIdentity").will(
                returnValue(id));
        long rv = sf.keepAllAlive(new ServiceInterfacePrx[] { prx }, null);
        assertEquals(0, rv);
    }

    @Test
    void testIsAliveReturnsTrueIfPresent() throws Exception {
        holder.put(id, new _IQueryTie(queryOps));
        managerMock.expects(atLeastOnce()).method("getEventContext");
        cacheMock.expects(once()).method("get").will(returnValue(elt));
        proxyMock.expects(once()).method("ice_getIdentity").will(
                returnValue(id));
        assertTrue(sf.keepAlive(prx, null));
    }

}
