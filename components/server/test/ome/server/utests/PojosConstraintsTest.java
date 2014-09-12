/*
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.server.utests;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import ome.api.IContainer;
import ome.conditions.ApiUsageException;
import ome.logic.PojosImpl;
import ome.model.containers.Dataset;
import ome.model.containers.Project;
import ome.model.core.Image;
import ome.parameters.Parameters;
import ome.security.basic.CurrentDetails;
import ome.server.itests.LoginInterceptor;
import ome.services.sessions.state.SessionCache;
import ome.services.util.ServiceHandler;
import ome.system.Principal;

import org.jmock.MockObjectTestCase;
import org.springframework.aop.framework.ProxyFactory;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * @author Josh Moore &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:josh.moore@gmx.de">josh.moore@gmx.de</a>
 * @version 1.0 <small> (<b>Internal version:</b> $Rev$ $Date$) </small>
 * @since Omero 2.0
 */
public class PojosConstraintsTest extends MockObjectTestCase {
    protected PojosImpl impl;

    protected IContainer manager;

    @Override
    @BeforeMethod
    protected void setUp() throws Exception {
        super.setUp();
        impl = new PojosImpl();
        ProxyFactory pf = new ProxyFactory(impl);
        SessionCache cache = new TestSessionCache(this);
        CurrentDetails holder = new CurrentDetails(cache);
        LoginInterceptor login = new LoginInterceptor(holder);
        ServiceHandler serviceHandler = new ServiceHandler(holder);
        pf.addAdvice(login);
        pf.addAdvice(serviceHandler);
        login.p = new Principal("user","user","Test");
        manager = (IContainer) pf.getProxy();
    }

    @Override
    @AfterMethod
    protected void tearDown() throws Exception {
        manager = null;
    }

    @Test
    public void testFindContainerHierarchies() throws Exception {
        T t = new T(ApiUsageException.class) {
            @Override
            public void doTest(Object[] arg) {
                manager.findContainerHierarchies((Class) arg[0], (Set) arg[1],
                        (Parameters) arg[2]);
            }
        };

        // param1: not null or wrong type
        t.blowup(true, null, new HashSet(), null);
        t.blowup(true, Dataset.class, new HashSet(), null);
        t.blowup(true, Image.class, new HashSet(), null);
        // FIXMEt.blowup(false,Project.class,new HashSet(), null);
        // FIXMEt.blowup(false,CategoryGroup.class,new HashSet(), new
        // HashMap());

        // param2:
        t.blowup(true, Project.class, null, null);
        // FIXMEt.blowup(false,Project.class,new HashSet(),null);

    }

    @Test
    public void testGetImages() throws Exception {
        T t = new T(ApiUsageException.class) {
            @Override
            public void doTest(Object[] arg) {
                manager.getImages((Class) arg[0], (Set) arg[1], (Parameters) arg[2]);
            }
        };

        // param1: not null
        t.blowup(true, null, new HashSet(), null);
        t.blowup(false, Dataset.class, new HashSet(), null);

    }

    @Test
    public void testGetUserImages() throws Exception {
        T t = new T(ApiUsageException.class) {
            @Override
            public void doTest(Object[] arg) {
                manager.getUserImages((Parameters) arg[0]);
            }
        };

        t.blowup(true, new Parameters().allExps());
        // TODO not in unit test t.blowup(false,new
        // PojoOptions().exp(1L).map());

    }

    @Test
    public void testLoadContainerHierary() throws Exception {
        Set ids;

        T t = new T(ApiUsageException.class) {
            @Override
            public void doTest(Object[] arg) {
                manager.loadContainerHierarchy((Class) arg[0], (Set) arg[1],
                        (Parameters) arg[2]);
            }
        };

        // param1: wrong or null class type
        ids = new HashSet<Integer>(Arrays.asList(1, 2, 3));
        t.blowup(true, null, ids, null);
        t.blowup(true, Image.class, ids, null);
        // FIXME do all blowup(false,...) belong in itests
        // t.blowup(false,Project.class,new HashSet(),options);

        // param2: not null unless there's an experimenter
        // FIXMEt.blowup(false,Project.class,null,new
        // PojoOptions().exp(1).map());
        t.blowup(true, Project.class, null, null);
        // FIXMEt.blowup(false,Project.class,new HashSet(),new
        // HashMap());//empty set is ok? TODO

        // param3: no constraints.

    }

    /**
     * part of the testing framework. Allow imlementers to specifiy a method to
     * be tested < <code>doTest</code> and then call it with an
     * {@see #blowup(boolean, Object[]) blowup}. Note: essentially a closure to
     * make calling this thing easy.
     */
    private static abstract class T {
        private Class t = null;

        public T() {
        }

        public T(Class thrown) {
            t = thrown;
        }

        public abstract void doTest(Object[] arg);

        public void setException(Class type) {
            t = type;
        }

        public void blowup(boolean exceptionExpected, Object... arg)
                throws Exception {
            boolean failed = false;
            try {
                doTest(arg);
                if (exceptionExpected) {
                    failed = true;
                    fail("Expected an exception here");
                }
            } catch (Exception e) {
                if (failed) {
                    throw e;
                }

                if (!exceptionExpected || t != null
                        && !t.isAssignableFrom(e.getClass())) {
                    throw new RuntimeException("Exception type " + e.getClass()
                            + " not expected. Rethrowing", e);
                }
            }
        }

    }

}
