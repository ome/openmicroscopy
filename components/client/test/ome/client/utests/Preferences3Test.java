/*
 *   $Id$
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.client.utests;

// Java imports
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import junit.framework.TestCase;
import ome.system.Login;
import ome.system.OmeroContext;
import ome.system.Principal;
import ome.system.Server;
import ome.system.ServiceFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.beans.factory.config.PreferencesPlaceholderConfigurer;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.StaticApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.testng.annotations.ExpectedExceptions;
import org.testng.annotations.Test;

/**
 * 
 * @author Josh Moore &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:josh.moore@gmx.de">josh.moore@gmx.de</a>
 * @version 1.0 <small> (<b>Internal version:</b> $Rev$ $Date$) </small>
 * @since 1.0
 */
public class Preferences3Test extends TestCase {

    private static Logger log = LoggerFactory.getLogger(Preferences3Test.class);

    static Properties props;
    static {
        props = new Properties();
        props.setProperty("omero.user", "bar");
        System.getProperties().setProperty("omero.user", "foo");
    }

    protected String[] getConfigLocations() {
        return new String[] { "ome/client/spring.xml" };
    }

    // public void testArePreferencesSet(){
    // PropertyPlaceholderConfigurer ppc =(PropertyPlaceholderConfigurer)
    // applicationContext.getBean("placeholderConfig");
    //        
    // Principal principal = (Principal)
    // applicationContext.getBean("principal");
    //        
    // assertTrue( "foo".equals(principal.getName()));
    // }

    public void xxxtestPreferencesAreUpdated() {

        log.info("CONTEXT");
        ConfigurableApplicationContext applicationContext = OmeroContext
                .getClientContext();

        // A)
        // PropertyPlaceholderConfigurer ppc =(PropertyPlaceholderConfigurer)
        // applicationContext.getBean("placeholderConfig");
        // ppc.setProperties( props );
        // 1
        // ppc.postProcessBeanFactory( applicationContext.getBeanFactory() );
        // 2
        // applicationContext.refresh();
        // applicationContext.getBeanFactory().
        // 3
        // ConfigurableListableBeanFactory bf =
        // applicationContext.getBeanFactory();
        // ppc.postProcessBeanFactory( bf );
        // while (bf.getParentBeanFactory() != null)
        // {
        // bf = (ConfigurableListableBeanFactory) bf.getParentBeanFactory();
        // ppc.postProcessBeanFactory( bf );
        // }
        // 4

        // B)
        log.info("OWN BFPP");
        BeanFactoryPostProcessor bfpp = new BeanFactoryPostProcessor() {
            public void postProcessBeanFactory(
                    ConfigurableListableBeanFactory beanFactory)
                    throws BeansException {
                PropertyPlaceholderConfigurer ppc = (PropertyPlaceholderConfigurer) beanFactory
                        .getBean("placeholderConfig");
                ppc.setProperties(props);
            }
        };
        log.info("ADDING BFPP");
        applicationContext.addBeanFactoryPostProcessor(bfpp);

        log.info("GETTING PRINCIPAL");
        Principal principal = (Principal) applicationContext
                .getBean("principal");

        assertTrue(principal.getName(), "bar".equals(principal.getName()));
    }

    @Test
    public void test_makeOurOwnDefault() throws Exception {
        // Others:
        // new ManagedMap();
        // BeanWrapper bw = new BeanWrapperImpl( defaultMap );

        Map defaultMap = new HashMap();
        defaultMap.put("omero.user", "foo");
        ConstructorArgumentValues cav = new ConstructorArgumentValues();
        cav.addGenericArgumentValue(defaultMap);
        BeanDefinition def = new RootBeanDefinition(HashMap.class, cav, null);
        StaticApplicationContext ac = new StaticApplicationContext();
        ac.registerBeanDefinition("map", def);
        ac.refresh();

        ConstructorArgumentValues testCav = new ConstructorArgumentValues();
        testCav.addGenericArgumentValue(new RuntimeBeanReference("map"));
        BeanDefinition testDef = new RootBeanDefinition(HashMap.class, testCav,
                null);
        StaticApplicationContext defaultTest = new StaticApplicationContext(ac);
        defaultTest.registerBeanDefinition("test", testDef);
        defaultTest.refresh();
        assertTrue("foo".equals(((Map) defaultTest.getBean("test"))
                .get("omero.user")));

    }

    @Test
    public void test_makeOurOwnRuntime() throws Exception {

        // use properties
        // if no Properties given, then is static (global)

        Map runtimeMap = new HashMap();
        runtimeMap.put("omero.user", "bar");
        ConstructorArgumentValues cav2 = new ConstructorArgumentValues();
        cav2.addGenericArgumentValue(runtimeMap);
        BeanDefinition def2 = new RootBeanDefinition(HashMap.class, cav2, null);
        StaticApplicationContext ac2 = new StaticApplicationContext();
        ac2.registerBeanDefinition("map", def2);
        ac2.refresh();

        ConstructorArgumentValues testCav2 = new ConstructorArgumentValues();
        testCav2.addGenericArgumentValue(new RuntimeBeanReference("map"));
        BeanDefinition testDef2 = new RootBeanDefinition(HashMap.class,
                testCav2, null);
        StaticApplicationContext defaultTest2 = new StaticApplicationContext(
                ac2);
        defaultTest2.registerBeanDefinition("test", testDef2);
        defaultTest2.refresh();
        assertTrue("bar".equals(((Map) defaultTest2.getBean("test"))
                .get("omero.user")));

    }

    @Test(groups = "ticket:148")
    public void test_using_OmeroContext() throws Exception {
        OmeroContext defaultCtx = OmeroContext.getClientContext();
        OmeroContext runtimeCtx = OmeroContext.getClientContext(props);

        Principal defaultPrincipal = (Principal) defaultCtx
                .getBean("principal");
        Principal runtimePrincipal = (Principal) runtimeCtx
                .getBean("principal");

        // It's difficult to know what the real user name is.
        assertFalse(defaultPrincipal.getName().equals("bar"));
        assertTrue(runtimePrincipal.getName().equals("bar"));
    }

    @Test(groups = "ticket:148")
    public void test_using_ServiceFactory() throws Exception {
        ServiceFactory basicSF = new ServiceFactory();
        ServiceFactory loginSF = new ServiceFactory(new Login("bar", "ome"));
        Properties p = new Properties();
        p.setProperty(Login.OMERO_USER, "bax");
        ServiceFactory propsSF = new ServiceFactory(p);

        Principal basicP = (Principal) basicSF.getContext()
                .getBean("principal");
        Principal loginP = (Principal) loginSF.getContext()
                .getBean("principal");
        Principal propsP = (Principal) propsSF.getContext()
                .getBean("principal");

        assertTrue(loginP.getName().equals("bar"));
        assertTrue(propsP.getName().equals("bax"));
        // It's difficult to know what the real user name is.
        assertFalse(basicP.getName().equals("bar"));
        assertFalse(basicP.getName().equals("bax"));

    }

    @Test(groups = "ticket:62")
    @ExpectedExceptions(BeanInitializationException.class)
    public void test_missingLoadProperties() {
        StaticApplicationContext ac = new StaticApplicationContext();
        PreferencesPlaceholderConfigurer ppc = new PreferencesPlaceholderConfigurer();
        ppc.setLocation(new ClassPathResource("DOES--NOT--EXIST"));

        ac.addBeanFactoryPostProcessor(ppc);
        ac.refresh();
    }

    @Test(groups = "ticket:62")
    public void test_missingLoadPropertiesIgnore() {
        StaticApplicationContext ac = new StaticApplicationContext();
        PreferencesPlaceholderConfigurer ppc = new PreferencesPlaceholderConfigurer();
        ppc.setLocation(new ClassPathResource("DOES--NOT--EXIST"));
        ppc.setIgnoreResourceNotFound(true);

        ac.addBeanFactoryPostProcessor(ppc);
        ac.refresh();

    }

    @Test(groups = "ticket:1058")
    public void testOmeroUserIsProperlySetWithSpring2_5_5Direct() {

        Server s = new Server("localhost", 1099);
        Login l = new Login("me", "password");
        Properties pl = l.asProperties();
        pl.setProperty("test.system.value",
                "This is like omero.user without local.properties");
        Properties ps = s.asProperties();
        ps.putAll(pl);

        OmeroContext c = OmeroContext.getContext(ps, "ome.client.test2");
        c.getBean("list");
    }

    @Test(groups = "ticket:1058")
    public void testOmeroUserIsProperlySetWithSpring2_5_5Manual() {

        Server s = new Server("localhost", 1099);
        Login l = new Login("me", "password");
        Properties p = s.asProperties();
        p.putAll(l.asProperties());

        // This is copied from OmeroContext. This is the parent context which
        // should contain the properties;
        Properties copy = new Properties(p);
        ConstructorArgumentValues ctorArg1 = new ConstructorArgumentValues();
        ctorArg1.addGenericArgumentValue(copy);
        BeanDefinition definition1 = new RootBeanDefinition(Properties.class,
                ctorArg1, null);
        StaticApplicationContext staticContext = new StaticApplicationContext();
        staticContext.registerBeanDefinition("properties", definition1);
        staticContext.refresh();

        // This is the child context and contains a definition of a
        // PlaceHolderConfigurer
        // as well as a user of
        StaticApplicationContext childContext = new StaticApplicationContext();

        MutablePropertyValues mpv2 = new MutablePropertyValues();
        mpv2.addPropertyValue("properties", new RuntimeBeanReference(
                "properties"));
        mpv2.addPropertyValue("systemPropertiesModeName",
                "SYSTEM_PROPERTIES_MODE_FALLBACK");
        mpv2.addPropertyValue("localOverride", "true");
        BeanDefinition definitionConfigurer = new RootBeanDefinition(
                PreferencesPlaceholderConfigurer.class, null, mpv2);
        childContext.registerBeanDefinition("propertiesPlaceholderConfigurer",
                definitionConfigurer);

        ConstructorArgumentValues cav2 = new ConstructorArgumentValues();
        cav2.addGenericArgumentValue("${omero.user}");
        BeanDefinition definitionTest = new RootBeanDefinition(String.class,
                cav2, null);
        childContext.registerBeanDefinition("test", definitionTest);

        childContext.setParent(staticContext);
        childContext.refresh();

        String test = (String) childContext.getBean("test");
        assertEquals(test, "me");

    }

}
