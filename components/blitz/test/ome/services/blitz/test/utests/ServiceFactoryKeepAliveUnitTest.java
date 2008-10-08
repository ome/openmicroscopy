/*
 *   $Id$
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.services.blitz.test.utests;

import java.util.HashMap;
import java.util.Map;

import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import ome.services.blitz.impl.ServiceFactoryI;
import ome.services.sessions.SessionManager;
import ome.services.util.Executor;
import ome.system.Principal;
import omero.api.ServiceInterfacePrx;
import omero.api._IQueryTie;
import omero.constants.CLIENTUUID;

import org.jmock.Mock;
import org.jmock.MockObjectTestCase;
import org.testng.annotations.Configuration;
import org.testng.annotations.Test;

@Test
public class ServiceFactoryKeepAliveUnitTest extends MockObjectTestCase {

    protected Executor executor = new Executor.Impl(null, null, null, null) {
        @Override
        public Object execute(Principal p, Work work) {
            return work.doWork(null, null, null);
        }
    };

    Element elt = new Element(null, null);
    Mock cacheMock, proxyMock, managerMock;
    Ehcache cache;
    ServiceFactoryI sf;
    ServiceInterfacePrx prx;
    SessionManager manager;
    Ice.Identity id = Ice.Util.stringToIdentity("test");
    Map<String, Ice.Object> map = new HashMap<String, Ice.Object>();
    Ice.Current current = new Ice.Current();
    {
        current.ctx = new HashMap<String, String>();
        current.ctx.put(CLIENTUUID.value, "clientuuid");
    }

    @Override
    @Configuration(beforeTestMethod = true)
    protected void setUp() throws Exception {
        managerMock = mock(SessionManager.class);
        cacheMock = mock(Ehcache.class);
        cache = (Ehcache) cacheMock.proxy();
        proxyMock = mock(ServiceInterfacePrx.class);
        prx = (ServiceInterfacePrx) proxyMock.proxy();
        manager = (SessionManager) managerMock.proxy();
        managerMock.expects(once()).method("inMemoryCache").will(
                returnValue(cache));
        cacheMock.expects(once()).method("isKeyInCache")
                .will(returnValue(true));
        cacheMock.expects(once()).method("get").will(
                returnValue(new Element("activeServants", map)));
        sf = new ServiceFactoryI(current, null, manager, executor,
                new Principal("a", "b", "c"), null);
    }

    @Test
    void testKeepAliveReturnsAllOnesOnNull() {
        managerMock.expects(atLeastOnce()).method("getEventContext");
        assertTrue(-1 == sf.keepAllAlive(null, null));
        assertTrue(-1 == sf.keepAllAlive(new ServiceInterfacePrx[] {}));
    }

    @Test
    void testKeepAliveReturnsNonNullIfMissing() {
        map.remove(Ice.Util.identityToString(id));
        managerMock.expects(atLeastOnce()).method("getEventContext");
        cacheMock.expects(once()).method("get").will(returnValue(null));
        proxyMock.expects(once()).method("ice_getIdentity").will(
                returnValue(id));
        long rv = sf.keepAllAlive(new ServiceInterfacePrx[] { prx });
        assertTrue((rv & 1 << 0) == 1 << 0);
    }

    @Test
    void testIsAliveReturnsFalseIfMissing() {
        map.remove(Ice.Util.identityToString(id));
        managerMock.expects(atLeastOnce()).method("getEventContext");
        cacheMock.expects(once()).method("get").will(returnValue(null));
        proxyMock.expects(once()).method("ice_getIdentity").will(
                returnValue(id));
        assertFalse(sf.keepAlive(prx));
    }

    @Test
    void testKeepAliveReturnsZeroIfPresent() {
        map.put(Ice.Util.identityToString(id), new _IQueryTie());
        managerMock.expects(atLeastOnce()).method("getEventContext");
        proxyMock.expects(once()).method("ice_getIdentity").will(
                returnValue(id));
        long rv = sf.keepAllAlive(new ServiceInterfacePrx[] { prx });
        assertEquals(0, rv);
    }

    @Test
    void testIsAliveReturnsTrueIfPresent() {
        map.put(Ice.Util.identityToString(id), new _IQueryTie());
        managerMock.expects(atLeastOnce()).method("getEventContext");
        cacheMock.expects(once()).method("get").will(returnValue(elt));
        proxyMock.expects(once()).method("ice_getIdentity").will(
                returnValue(id));
        assertTrue(sf.keepAlive(prx));
    }

}
