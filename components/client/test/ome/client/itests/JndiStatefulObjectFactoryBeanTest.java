package ome.client.itests;


//Java imports
import org.testng.annotations.*;

import java.io.File;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Properties;

import junit.framework.TestCase;

//Third-party libraries
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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

//Application-internal dependencies
import ome.api.RawPixelsStore;
import ome.client.ConfigurableJndiObjectFactoryBean;
import ome.system.Login;
import ome.system.OmeroContext;
import ome.system.Principal;
import ome.system.ServiceFactory;


/**
 * 
 * @author  Josh Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:josh.moore@gmx.de">josh.moore@gmx.de</a>
 * @version 1.0 
 * <small>
 * (<b>Internal version:</b> $Rev$ $Date$)
 * </small>
 * @since 1.0
 */
public class JndiStatefulObjectFactoryBeanTest extends TestCase {

    private static Log log = LogFactory.getLog(JndiStatefulObjectFactoryBeanTest.class);
    
    @Test
    public void testDoCallsReturnDifferentObjects() throws Exception
    {
        ServiceFactory sf = new ServiceFactory();
        Hashtable ht = (Hashtable) sf.getContext().getBean("env");
        MutablePropertyValues mpv = new MutablePropertyValues();
        mpv.addPropertyValue("jndiEnvironment",ht);
        mpv.addPropertyValue("lookupOnStartup",Boolean.FALSE);
        mpv.addPropertyValue("jndiName","omero/remote/ome.api.RawPixelStore");
        mpv.addPropertyValue("proxyInterface",RawPixelsStore.class.getName());
        mpv.addPropertyValue("principal",new Principal("a","b","c"));
        mpv.addPropertyValue("credentials","password");
        mpv.addPropertyValue("stateful","true");
        BeanDefinition def = new RootBeanDefinition(
                ConfigurableJndiObjectFactoryBean.class, null, mpv);
        StaticApplicationContext context = new StaticApplicationContext();
        context.registerBeanDefinition( "jndi", def );
        context.refresh();
        
        RawPixelsStore raw_1 = (RawPixelsStore) context.getBean("jndi");
        RawPixelsStore raw_2 = (RawPixelsStore) context.getBean("jndi");
        assertTrue( raw_1 != raw_2 );

    }
	
}

