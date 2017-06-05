/*
 *   $Id$
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.server.itests;

import java.util.Arrays;

import junit.framework.TestCase;
import ome.api.IQuery;
import ome.api.local.LocalCompress;
import ome.services.ThumbnailBean;
import ome.system.OmeroContext;
import ome.system.ServiceFactory;

import org.springframework.aop.framework.Advised;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@Test(groups = "integration")
public class ContextTest extends TestCase {

    protected TB rb;

    static class TB extends ThumbnailBean {
        
        TB(){
            super(false, null);
        }
        /**
         * 
         */
        private static final long serialVersionUID = -6011918575014582969L;
        public boolean csCalled = false;

        @Override
        public void setCompressionService(LocalCompress compressionService) {
            csCalled = true;
        }
    }

    @Override
    @BeforeMethod
    protected void setUp() throws Exception {
        rb = new TB();
    }

    @Test(groups = "broken") // This only works while running standalone
    public void testFullTextContext() throws Exception {
        OmeroContext.getInstance("ome.fulltext");
    }

    @Test
    public void testListBeans() throws Exception {
        OmeroContext ctx = OmeroContext.getManagedServerContext();
        ctx.refreshAllIfNecessary();
        assertTrue(0 < Arrays.asList(
                BeanFactoryUtils.beanNamesForTypeIncludingAncestors(ctx
                        .getBeanFactory(), Object.class, true, true)).size());
    }

    @Test
    public void testManagedContext() throws Exception {
        OmeroContext ctx = OmeroContext.getManagedServerContext();
        ctx.refreshAllIfNecessary();
        onContext(ctx);
    }

    @Test
    public void testManagedContextClose() throws Exception {
        OmeroContext ctx = OmeroContext.getManagedServerContext();
        ctx.refreshAllIfNecessary();
        ctx.closeAll();
        ctx.refreshAll();
    }

    protected void onContext(OmeroContext ctx) {
        ServiceFactory sf = new ServiceFactory(ctx);
        IQuery q = sf.getQueryService();
        assertTrue(Advised.class.isAssignableFrom(q.getClass()));
    }

    @Test
    public void testConfigureBean() throws Exception {

        OmeroContext ctx = OmeroContext.getManagedServerContext();
        ctx.refreshAllIfNecessary();
        ctx.applyBeanPropertyValues(rb, "internal-ome.api.ThumbnailStore");
        assertTrue(rb.csCalled);
    }

    @Test
    public void testSelfConfigureBean() throws Exception {
        rb.selfConfigure();
        assertTrue(rb.csCalled);
    }

}
