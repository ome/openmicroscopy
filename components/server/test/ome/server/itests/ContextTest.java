/*
 *   $Id$
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.server.itests;

import java.util.Arrays;

import junit.framework.TestCase;
import ome.api.IPixels;
import ome.api.IQuery;
import ome.services.RenderingBean;
import ome.services.jboss.OmeroContextHook;
import ome.system.OmeroContext;
import ome.system.ServiceFactory;
import omeis.providers.re.RenderingEngine;

import org.springframework.aop.framework.Advised;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.testng.annotations.Configuration;
import org.testng.annotations.Test;

@Test(groups = "integration")
public class ContextTest extends TestCase {

    protected RE re;

    static class RE extends RenderingBean {
        /**
         * 
         */
        private static final long serialVersionUID = -6011918575014582969L;
        public boolean pdCalled = false, pmCalled = false;

        @Override
        public void setPixelsData(ome.io.nio.PixelsService arg0) {
            pdCalled = true;
        };

        @Override
        public void setPixelsMetadata(IPixels arg0) {
            pmCalled = true;
        }
    }

    @Override
    @Configuration(beforeTestMethod = true)
    protected void setUp() throws Exception {
        re = new RE();
    }

    @Test
    public void testListBeans() throws Exception {

        assertTrue(0 < Arrays.asList(
                BeanFactoryUtils
                        .beanNamesForTypeIncludingAncestors(OmeroContext
                                .getManagedServerContext().getBeanFactory(),
                                Object.class, true, true)).size());
    }

    @Test
    public void testManagedContext() throws Exception {
        OmeroContext ctx = OmeroContext.getManagedServerContext();
        onContext(ctx);
    }

    protected void onContext(OmeroContext ctx) {
        ServiceFactory sf = new ServiceFactory(ctx);
        IQuery q = sf.getQueryService();
        assertTrue(Advised.class.isAssignableFrom(q.getClass()));
    }

    @Test
    public void testConfigureBean() throws Exception {

        OmeroContext ctx = OmeroContext.getManagedServerContext();
        ctx.applyBeanPropertyValues(re, RenderingEngine.class);
        assertTrue(re.pdCalled);
        assertTrue(re.pmCalled);
    }

    @Test
    public void testSelfConfigureBean() throws Exception {
        re.selfConfigure();
        assertTrue(re.pdCalled);
        assertTrue(re.pmCalled);
    }

    @Test
    public void testStartupHook() throws Exception {
        OmeroContextHook hook = new OmeroContextHook();
        hook.start();
    }

}
