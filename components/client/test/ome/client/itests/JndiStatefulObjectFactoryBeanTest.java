/*
 *   $Id$
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.client.itests;

// Java imports
import java.util.Hashtable;

import junit.framework.TestCase;
import ome.api.RawPixelsStore;
import ome.client.ConfigurableJndiObjectFactoryBean;
import ome.system.ServiceFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.support.StaticApplicationContext;
import org.testng.annotations.Test;

/**
 * 
 * @author Josh Moore &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:josh.moore@gmx.de">josh.moore@gmx.de</a>
 * @version 1.0 <small> (<b>Internal version:</b> $Rev$ $Date$) </small>
 * @since 1.0
 */
public class JndiStatefulObjectFactoryBeanTest extends TestCase {

    private static Log log = LogFactory
            .getLog(JndiStatefulObjectFactoryBeanTest.class);

    @Test
    public void testDoCallsReturnDifferentObjects() throws Exception {
        ServiceFactory sf = new ServiceFactory();
        Hashtable ht = (Hashtable) sf.getContext().getBean("env");
        MutablePropertyValues mpv = new MutablePropertyValues();
        mpv.addPropertyValue("jndiEnvironment", ht);
        mpv.addPropertyValue("lookupOnStartup", Boolean.FALSE);
        mpv.addPropertyValue("jndiName", "omero/remote/ome.api.RawPixelStore");
        mpv.addPropertyValue("proxyInterface", RawPixelsStore.class.getName());
        mpv.addPropertyValue("stateful", "true");
        BeanDefinition def = new RootBeanDefinition(
                ConfigurableJndiObjectFactoryBean.class, null, mpv);
        StaticApplicationContext context = new StaticApplicationContext();
        context.registerBeanDefinition("jndi", def);
        context.refresh();

        RawPixelsStore raw_1 = (RawPixelsStore) context.getBean("jndi");
        RawPixelsStore raw_2 = (RawPixelsStore) context.getBean("jndi");
        assertTrue(raw_1 != raw_2);

    }

}
