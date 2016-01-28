/*
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.server.utests;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.SessionFactory;
import org.hibernate.classic.Session;
import org.jmock.Mock;
import org.jmock.MockObjectTestCase;
import org.springframework.aop.framework.ProxyFactory;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ome.api.IQuery;
import ome.conditions.ApiUsageException;
import ome.logic.QueryImpl;
import ome.model.IObject;
import ome.model.containers.Project;
import ome.parameters.Filter;
import ome.security.basic.CurrentDetails;
import ome.server.itests.LoginInterceptor;
import ome.services.util.ServiceHandler;
import ome.system.Principal;

/**
 * @author Josh Moore &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:josh.moore@gmx.de">josh.moore@gmx.de</a>
 * @since Omero 2.0
 */
public class IQueryMockSessionTest extends MockObjectTestCase {

    protected IQuery iQuery;

    protected QueryImpl impl;

    protected Mock mockSession, mockFactory;

    @Override
    @BeforeMethod(alwaysRun=true)
    protected void setUp() throws Exception {
        super.setUp();
        impl = new QueryImpl();
        createMocks();
        ProxyFactory pf = new ProxyFactory(impl);
        CurrentDetails holder = new CurrentDetails(new TestSessionCache(this));
        LoginInterceptor login = new LoginInterceptor(holder);
        ServiceHandler serviceHandler = new ServiceHandler(holder);
        pf.addAdvice(login);
        pf.addAdvice(serviceHandler);
        login.p = new Principal("user","user","Test");
        iQuery = (IQuery) pf.getProxy();
    }

    @Override
    @AfterMethod(alwaysRun=true)
    protected void tearDown() throws Exception {
        super.verify();
        super.tearDown();
    }

    protected void createMocks() {
        mockSession = mock(Session.class);
        mockFactory = mock(SessionFactory.class);
        
        impl.setSessionFactory((SessionFactory) mockFactory.proxy());

        ome.model.meta.Session session = new ome.model.meta.Session();
        session.setStarted(new Timestamp(System.currentTimeMillis()));
        session.setTimeToLive(0L);
        session.setTimeToIdle(60000L);

    }

    protected void loadsSession() {
        mockFactory.expects(atLeastOnce()).method("getCurrentSession").will(
                returnValue(mockSession.proxy()));
    }

    protected Mock criteriaUniqueResultCall(Object obj) {
        Mock mockCriteria = mock(Criteria.class);
        mockCriteria.expects(once()).method("uniqueResult").will(
                returnValue(obj));
        return mockCriteria;
    }

    protected Mock criteriaListCall(List blank) {
        Mock mockCriteria = mock(Criteria.class);
        mockCriteria.expects(once()).method("list").will(returnValue(blank));
        return mockCriteria;
    }

    @Test
    public void test_get() throws Exception {
        loadsSession();
        mockSession.expects(once()).method("load").id("test");
        iQuery.get(IObject.class, 1L);
    }

    @Test(expectedExceptions = ApiUsageException.class)
    public void test_get_nulls() throws Exception {
        iQuery.get(null, 1L);
    }

    @Test
    public void test_find() throws Exception {
        loadsSession();
        mockSession.expects(once()).method("get");
        iQuery.find(IObject.class, 1L);
    }

    @Test(expectedExceptions = ApiUsageException.class)
    public void test_find_nulls() throws Exception {
        iQuery.find(null, 1L);
    }

    @Test
    public void test_findAll_nullfilter() {
        loadsSession();
        List blank = new ArrayList();
        Mock mockCriteria = criteriaListCall(blank);
        mockCriteria.expects(once()).method("setResultTransformer");
        mockSession.expects(once()).method("createCriteria").will(
                returnValue(mockCriteria.proxy())).id("test");
        List retVal = iQuery.findAll(Project.class, null);
        assertTrue(retVal == blank);
    }

    @Test
    public void test_findAll_realfilter() {
        loadsSession();
        Filter filter = new Filter();
        filter.page(1, 10);

        List blank = new ArrayList();
        Mock mockCriteria = criteriaListCall(blank);
        mockCriteria.expects(once()).method("setFirstResult");
        mockCriteria.expects(once()).method("setMaxResults");
        mockCriteria.expects(once()).method("setResultTransformer");
        mockSession.expects(once()).method("createCriteria").will(
                returnValue(mockCriteria.proxy())).id("test");
        List retVal = iQuery.findAll(Project.class, filter);
        assertTrue(retVal == blank);
    }

    @Test(expectedExceptions = ApiUsageException.class)
    public void test_findByExample_null() throws Exception {
        iQuery.findByExample(null);
    }

    @Test
    public void test_findByExample() throws Exception {
        loadsSession();
        Project test = new Project();
        Project dummy = new Project();
        Mock mockCriteria = criteriaUniqueResultCall(dummy);
        mockCriteria.expects(once()).method("add");
        mockSession.expects(once()).method("createCriteria").will(
                returnValue(mockCriteria.proxy())).id("test");
        Object retVal = iQuery.findByExample(test);
        assertTrue(retVal == dummy);
    }

    @Test(expectedExceptions = ApiUsageException.class)
    public void test_findAllByExample_null() throws Exception {
        iQuery.findAllByExample(null, null);
    }

    @Test(expectedExceptions = ApiUsageException.class)
    public void test_findAllByExample_null2() throws Exception {
        iQuery.findAllByExample(null, new Filter());
    }

    @Test
    public void test_findAllByExample() throws Exception {
        loadsSession();
        Project test = new Project();
        List blank = new ArrayList();
        Mock mockCriteria = criteriaListCall(blank);
        mockCriteria.expects(once()).method("add");
        mockCriteria.expects(once()).method("setFirstResult");
        mockCriteria.expects(once()).method("setMaxResults");
        mockSession.expects(once()).method("createCriteria").will(
                returnValue(mockCriteria.proxy())).id("test");
        Object retVal = iQuery.findAllByExample(test, null);
        assertTrue(retVal == blank);
    }

    @Test
    public void test_findAllByExample_filter() throws Exception {
        loadsSession();
        Project test = new Project();
        List blank = new ArrayList();
        Mock mockCriteria = criteriaListCall(blank);
        mockCriteria.expects(once()).method("add");
        mockCriteria.expects(once()).method("setFirstResult");
        mockCriteria.expects(once()).method("setMaxResults");

        mockSession.expects(once()).method("createCriteria").will(
                returnValue(mockCriteria.proxy())).id("test");
        Object retVal = iQuery.findAllByExample(test, new Filter().page(1, 10));
        assertTrue(retVal == blank);
    }

}
