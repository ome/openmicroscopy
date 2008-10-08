/*
 *   $Id$
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.services.blitz.test.utests;

import junit.framework.TestCase;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;

import org.springframework.cache.ehcache.EhCacheFactoryBean;
import org.testng.annotations.Test;

public class EhCacheTest extends TestCase {

    @Test
    public void testFactoryBean() throws Exception {

        EhCacheFactoryBean fb = new EhCacheFactoryBean();
        fb.setBeanName("test");
        fb.setOverflowToDisk(false);
        fb.afterPropertiesSet();
        Ehcache cache = (Ehcache) fb.getObject();
        cache.put(new Element("foo","bar"));
        String test = (String) cache.get("foo").getObjectValue();
        assertTrue("bar".equals(test));
        
    }
}
